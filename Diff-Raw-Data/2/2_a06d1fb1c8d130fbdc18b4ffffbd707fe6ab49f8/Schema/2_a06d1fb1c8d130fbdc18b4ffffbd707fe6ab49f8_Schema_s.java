 package com.gmail.altakey.mint;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class Schema {
     public static final String DATABASE = "toodledo";
     public static final int VERSION = 1;
 
     public static class OpenHelper extends SQLiteOpenHelper {
         public OpenHelper(final Context ctx) {
             super(ctx, DATABASE, null, VERSION);
         }
 
         @Override
         public void onCreate(SQLiteDatabase db) {
             db.execSQL("CREATE TABLE IF NOT EXISTS tasks (_id INTEGER PRIMARY KEY AUTOINCREMENT, cookie VARCHAR UNIQUE, task BIGINT UNIQUE, title TEXT, note TEXT, modified BIGINT, completed TEXT, folder BIGINT, context BIGINT, priority INTEGER, star INTEGER, duedate BIGINT, duetime BIGINT, status BIGINT)");
             db.execSQL("CREATE TABLE IF NOT EXISTS folders (_id INTEGER PRIMARY KEY AUTOINCREMENT, modified BIGINT, folder BIGINT UNIQUE, name TEXT, private TEXT, archived TEXT, ord TEXT)");
             db.execSQL("CREATE TABLE IF NOT EXISTS contexts (_id INTEGER PRIMARY KEY AUTOINCREMENT, context BIGINT UNIQUE, name TEXT)");
             db.execSQL("CREATE TABLE IF NOT EXISTS statuses (_id INTEGER PRIMARY KEY AUTOINCREMENT, status BIGINT UNIQUE, name TEXT UNIQUE)");
 
             /* Status initial content */
             db.execSQL("INSERT INTO statuses (status, name) VALUES (0, 'INBOX')");
             db.execSQL("INSERT INTO statuses (status, name) VALUES (-1, 'Hotlist')"); // XXX
             db.execSQL("INSERT INTO statuses (status, name) VALUES (1, 'Next Action')");
             db.execSQL("INSERT INTO statuses (status, name) VALUES (10, 'Reference')");
            db.execSQL("INSERT INTO statuses (status, name) VALUES (5, 'Wating')");
             db.execSQL("INSERT INTO statuses (status, name) VALUES (8, 'Someday')");
         }
 
         @Override
         public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         }
     };
 }
