 package com.feedme.dao;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import com.feedme.database.*;
 
 /**
  * User: root
  * Date: 1/16/12
  * Time: 1:24 PM
  */
 public class DatabaseHandler extends SQLiteOpenHelper {
 
     // Database Version
     private static final int DATABASE_VERSION = 1;
 
     // Database Name
     private static final String DATABASE_NAME = "FEED_ME";
 
     public DatabaseHandler(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
     }
 
     // Creating Tables
     @Override
     public void onCreate(SQLiteDatabase db) {
 
         String babiesTableStatement = BabyColumn.createTableStatement();
         String journalTableStatement = JournalColumn.createTableStatement();
         String napTableStatement = NapColumn.createTableStatement();
         String settingsTableStatement = SettingsColumn.createTableStatement();
         String diapersTableStatement = DiaperColumn.createTableStatement();
 
         db.execSQL(babiesTableStatement);
         db.execSQL(journalTableStatement);
         db.execSQL(napTableStatement);
         db.execSQL(settingsTableStatement);
         db.execSQL(diapersTableStatement);
     }
 
     // Upgrading database
     @Override
     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 
         // Drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + BabyColumn.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + JournalColumn.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + NapColumn.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SettingsColumn.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DiaperColumn.TABLE_NAME);
 
         // Create tables again
        onCreate(db);
     }
 }
