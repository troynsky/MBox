package com.home.musicbox.mbox;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class MainActivity extends Activity {

    private final static String TAG = "DEV";

    private DataBaseHelper dbHelper;
    private ListView listView;
    private EditText search;
    private ArrayList<String> list;

    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("DEV", "Start onCreate");
        listView = (ListView) findViewById(R.id.listView);
        search = (EditText) findViewById(R.id.search);
        dbHelper = new DataBaseHelper(this);

        try {
            dbHelper.createDataBase();
        } catch (IOException ioe) {
            Log.e(TAG, "Unable to create database");
        }
        try {
            SQLiteDatabase db = dbHelper.openDataBase();
            Cursor cursor = db.query("Track", new String[]{"Composer", "Name"}, null, null,
                    null, null, null);
            list = new ArrayList<>();
            if (cursor.moveToFirst()) {
                while (cursor.moveToNext()) {
                    String composer = cursor.getString(cursor.getColumnIndex("Composer"));
                    String rez = (composer == null ? "unknown" : composer) + " : " + cursor.getString(cursor.getColumnIndex("Name"));
                    list.add(rez);
                }
            }
        } catch (SQLException sqle) {
            Log.e(TAG, "Unable to open base");
        }

        if (list == null) {
            Log.e(TAG, "list with records from db is empty");
            list = new ArrayList<>();
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                MainActivity.this.adapter.getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
        Log.i(TAG, "DB connection closed");
    }

    class DataBaseHelper extends SQLiteOpenHelper {

        private static final String DB_NAME = "db";
        private static final int DB_VERSION = 1;
        private String DB_PATH = "/data/data/com.home.musicbox.mbox/databases/";
        private SQLiteDatabase myDataBase;
        private final Context myContext;

        public DataBaseHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
            this.myContext = context;
        }

        public void createDataBase() throws IOException {
            boolean dbExist = checkDataBase();
            if (dbExist) {
            } else {
                this.getReadableDatabase();
                try {
                    copyDataBase();
                } catch (IOException e) {
                    Log.e(TAG, "Error copying database");
                }
            }
        }

        private boolean checkDataBase() {
            SQLiteDatabase checkDB = null;
            try {
                String myPath = DB_PATH + DB_NAME;
                checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
            } catch (SQLiteException e) {
                Log.d(TAG, "database does't exist yet");
            }
            if (checkDB != null) {
                checkDB.close();
            }
            return checkDB != null ? true : false;
        }

        private void copyDataBase() throws IOException {

            InputStream myInput = myContext.getAssets().open(DB_NAME);
            String outFileName = DB_PATH + DB_NAME;
            OutputStream myOutput = new FileOutputStream(outFileName);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInput.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
            }
            myOutput.flush();
            myOutput.close();
            myInput.close();
        }

        public SQLiteDatabase openDataBase() throws SQLException {
            String myPath = DB_PATH + DB_NAME;
            myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
            return myDataBase;
        }

        @Override
        public synchronized void close() {
            if (myDataBase != null)
                myDataBase.close();
            super.close();
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}
