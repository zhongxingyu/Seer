 package com.ksoft.android.dao;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 import com.ksoft.android.model.Location;
 import com.ksoft.android.model.Reminder;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Catalin Samarghitan EMail : catalin.samarghitan@gmail.com
  * Date: 4/17/13
  * Time: 2:40 PM
  */
 public class DatabaseHelper extends SQLiteOpenHelper {
     // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 2;
     public static final String DATABASE_NAME = "ProximityReminder.db";
 
     private static final String SQL_CREATE_LOCATION_ENTRIES = "CREATE TABLE " + Location.LOCATION_TABLE_NAME + " (" +
            Location._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
             Location.COLUMN_NAME + " TEXT UNIQUE," +
             Location.COLUMN_WIFI_NAME + " TEXT" +
             " )";
 
     private static final String SQL_CREATE_REMINDER_ENTRIES = "CREATE TABLE " + Reminder.REMINDER_TABLE_NAME + " (" +
            Reminder._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
             Reminder.REMINDER_TEXT + " TEXT," +
             Reminder.LOCATION_ID + " INTEGER," +
             Reminder.REMINDER_TIME + " INTEGER," +
             Reminder.TYPE + " INTEGER" +
             " )";
 
     private static final String SQL_DROP_LOCATION_TABLE =
             "DROP TABLE IF EXISTS " + Location.LOCATION_TABLE_NAME;
     private static final String SQL_DROP_REMINDER_TABLE =
             "DROP TABLE IF EXISTS " + Reminder.REMINDER_TABLE_NAME;
 
     public DatabaseHelper(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
     }
     public void onCreate(SQLiteDatabase db) {
         Log.d("DatabaseHelper", "Creating tables!");
         db.execSQL(SQL_CREATE_LOCATION_ENTRIES);
         db.execSQL(SQL_CREATE_REMINDER_ENTRIES);
     }
     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         // This database is only a cache for online data, so its upgrade policy is
         // to simply to discard the data and start over
         db.execSQL(SQL_DROP_LOCATION_TABLE);
         db.execSQL(SQL_DROP_REMINDER_TABLE);
         onCreate(db);
     }
     public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         onUpgrade(db, oldVersion, newVersion);
     }
 
 
 }
