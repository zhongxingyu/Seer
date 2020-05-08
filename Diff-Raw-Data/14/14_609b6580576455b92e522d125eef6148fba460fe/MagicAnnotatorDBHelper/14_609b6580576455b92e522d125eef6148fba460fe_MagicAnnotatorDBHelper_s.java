 /*
  * Copyright (c) 2012 Nahuel Barrios <barrios.nahuel@gmail.com>.
  * No se reconocerá ningún tipo de garantía.
  */
 
 /**
  * Created by: Nahuel Barrios.
  * On: 02/10/12 at 11:05hs.
  */
 package com.nbempire.android.magicannotator.storage;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 import com.nbempire.android.magicannotator.storage.schema.MarketItemTable;
 import com.nbempire.android.magicannotator.storage.schema.PlayerTable;
 
 /**
  * SQLiteOpenHelper for the MagicAnnotator database. It should be used to get a database to work with.
  * <p/>
  * It also performs the create statement for creating every table for the database and the upgrade proccess to take a previous database version
  * to a newer one.
  *
  * @author Nahuel Barrios.
  * @since 10
  */
 public class MagicAnnotatorDBHelper extends SQLiteOpenHelper {
 
     /**
      * Tag for class' log.
      */
     private static final String LOG_TAG = "MagicAnnotatorDB";
 
     /**
      * Current DB version.
      */
     private static final int DB_VERSION = 2;
 
     /**
      * Name of the database file, or null for an in-memory database
      */
     private static final String DB_NAME = "com.nbempire.android.magicannotator";
 
     /**
      * Constructor method for MagicAnnotatorDBHelper type.
      *
      * @param context
      *         The context.
      */
     public MagicAnnotatorDBHelper(Context context) {
         super(context, DB_NAME, null, DB_VERSION);
     }
 
     @Override
     public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String scriptToExecute = MarketItemTable.getCreateScript() + PlayerTable.getCreateScript();
        Log.i(LOG_TAG, "Creating DB for first time from SQL script: " + scriptToExecute);
         sqLiteDatabase.execSQL(scriptToExecute);
     }
 
     @Override
     public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldDBVersion, int newDBVersion) {
         Log.w(LOG_TAG, "Upgrading database from version " + oldDBVersion + " to " + newDBVersion + ", which will destroy all old data.");
 
        sqLiteDatabase.execSQL(MarketItemTable.getDropScript() + PlayerTable.getDropScript());
         onCreate(sqLiteDatabase);
     }
 
 
 }
