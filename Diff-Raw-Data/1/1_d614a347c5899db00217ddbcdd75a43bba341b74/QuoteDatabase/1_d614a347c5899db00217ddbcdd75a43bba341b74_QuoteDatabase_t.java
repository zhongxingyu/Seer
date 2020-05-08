 /*
  * © Copyright 2011 Thibault Jouannic <thibault@jouannic.fr>. All Rights Reserved.
  *  This file is part of OpenQOTD.
  *
  *  OpenQOTD is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  OpenQOTD is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with OpenQOTD. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package fr.miximum.qotd;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteQueryBuilder;
 import android.util.Log;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.HashMap;
 
 public class QuoteDatabase {
     /**
      * The basic db name, located is the asset directory
      */
     private static final String DB_NAME = "qotd.db";
 
     /**
      * We need to suffix the db with a dummy extension, to prevent android to compress it
      */
     private static final String DB_FILE_FAKE_EXTEN = ".mp3";
 
     /**
      * Current db version
      */
     private static final int DB_VERSION = 1;
 
     /**
      * Name of the table containing quotes
      */
     private static final String QUOTES_TABLE_NAME = "quote";
 
     /**
      * Helper to manage database creation and upgrade
      */
     private final QuoteDbHelper mDbHelper;
 
     /**
      * Column map projection
      */
     private static final HashMap<String,String> mColumnMap = buildColumnMap();
 
     /**
      * Constructor
      * @param context The Context within which to work, used to create the DB
      */
     public QuoteDatabase(Context context) {
       mDbHelper = new QuoteDbHelper(context);
       mDbHelper.initializeDatabase();
     }
 
     /**
      * Builds a map for all columns that may be requested, which will be given to the
      * SQLiteQueryBuilder. This is a good way to define aliases for column names, but must include
      * all columns, even if the value is the key. This allows the ContentProvider to request
      * columns w/o the need to know real column names and create the alias itself.
      */
     private static HashMap<String,String> buildColumnMap() {
         HashMap<String,String> map = new HashMap<String,String>();
         map.put(Quote._ID, Quote._ID);
         map.put(Quote.QUOTE, Quote.QUOTE);
         return map;
     }
 
     /**
      * Returns a Cursor positioned at the quote given id
      *
      * @param id id of quote to retrieve
      * @return Cursor positioned to matching word, or null if not found.
      */
     public Cursor getQuote(int id) {
         String selection = Quote._ID + " = ?";
         String[] selectionArgs = new String[] { String.valueOf(id) };
 
         /* This builds a query that looks like:
          *     SELECT <columns> FROM <table> WHERE rowid = <rowId>
          */
         Cursor c = query(selection, selectionArgs);
         if (!c.moveToFirst()) {
             c.close();
         }
         return c;
     }
 
     /**
      * Performs a database query.
      * @param selection The selection clause
      * @param selectionArgs Selection arguments for "?" components in the selection
      * @return A Cursor over all rows matching the query
      */
     private Cursor query(String selection, String[] selectionArgs) {
         /* The SQLiteBuilder provides a map for all possible columns requested to
          * actual columns in the database, creating a simple column alias mechanism
          * by which the ContentProvider does not need to know the real column names
          */
         SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
         builder.setTables(QUOTES_TABLE_NAME);
         builder.setProjectionMap(mColumnMap);
 
         Cursor cursor = builder.query(mDbHelper.getReadableDatabase(),
                 null, selection, selectionArgs, null, null, null);
 
         if (!cursor.moveToFirst()) {
             cursor.close();
         }
         return cursor;
     }
 
     /**
      * Get a random quote
      * @param lang Filter by lang
      */
     public Cursor getRandomQuote(String lang) {
         SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
         qb.setTables(QUOTES_TABLE_NAME);
         qb.setProjectionMap(mColumnMap);
 
         qb.appendWhere("lang == ?");
         String[] whereArgs = new String[] { lang };
 
         Cursor c = qb.query(mDbHelper.getReadableDatabase(), null, null, whereArgs, null, null, "RANDOM()", "1");
 
         if (!c.moveToFirst()) {
             c.close();
         }
         return c;
     }
 
     /** Helps create and open the database file */
     private static class QuoteDbHelper extends SQLiteOpenHelper {
 
         private Context mContext;
         private boolean createDatabase = false;
         private boolean upgradeDatabase = false;
 
         QuoteDbHelper(Context context) {
             super(context, DB_NAME, null, DB_VERSION);
             mContext = context;
         }
 
         /** Db will be created later */
         @Override
         public void onCreate(SQLiteDatabase db) {
             Log.d("QOTD", "Database needs to be created");
             createDatabase = true;
         }
 
         @Override
         public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
             Log.d("QOTD", "Database needs to be upgraded");
             upgradeDatabase = true;
         }
 
         /** This method has to be called after constructor */
         public void initializeDatabase() {
             // Make sure we request database, to initialize creation variables
             getWritableDatabase();
 
             if (createDatabase) {
                 copyDatabase();
             }
             else if (upgradeDatabase) {
                 upgradeDatabase();
             }
         }
 
         /* To create database, we copy the one provided in assets */
         public void copyDatabase() {
             Log.d("QOTD", "Creating database");
 
             // We need to close the db handle to overwrite the newly created db
             close();
 
             try {
                 // Fixtures db is in assets
                 InputStream myInput = mContext.getAssets().open(DB_NAME + DB_FILE_FAKE_EXTEN);
 
                 //Open the empty db as the output stream
                 String outFileName = mContext.getDatabasePath(DB_NAME).getAbsolutePath();
                 OutputStream myOutput = new FileOutputStream(outFileName);
 
                 //transfer bytes from the inputfile to the outputfile
                 byte[] buffer = new byte[1024];
                 int length;
                 while ((length = myInput.read(buffer)) > 0) {
                     myOutput.write(buffer, 0, length);
                 }
 
                 myOutput.flush();
                 myOutput.close();
                 myInput.close();
 
                 // Access the db to set the access timestamp (prevents a "corrupted" error)
                 getWritableDatabase();
                close();
             } catch (IOException ioe) {
                 Log.e("QOTD", "Cannot create db : " + ioe.getMessage());
             }
         }
 
         /** Just wipe out the current db, and replace it with the fixture's one */
         public void upgradeDatabase() {
             Log.d("QOTD", "Updating database.");
             copyDatabase();
         }
     }
 }
