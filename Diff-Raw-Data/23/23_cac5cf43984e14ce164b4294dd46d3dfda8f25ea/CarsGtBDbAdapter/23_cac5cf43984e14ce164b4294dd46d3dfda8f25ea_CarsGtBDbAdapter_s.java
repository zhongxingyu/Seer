 /*
  * Copyright (C) 2008 Google Inc.
  * 
  * Modifications By: 
  * Copyright (C) 2012 Matthew Finkel <Matthew.Finkel@gmail.com>
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package edu.uconn.guarddogs.guardthebridge;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 /**
  * Simple notes database access helper class. Defines the basic CRUD operations
  * for the notepad example, and gives the ability to list all notes as well as
  * retrieve or modify a specific note.
  * 
  * This has been improved from the first version of this tutorial through the
  * addition of better error handling and also using returning a Cursor instead
  * of using a collection of inner classes (which is less scalable and not
  * recommended).
  */
 public class CarsGtBDbAdapter {
     public static final String KEY_ROWID = "_id";
     public static final String KEY_CARNUM = "carnum";
 
     private static final String TAG = "CarGtBDbAdapter";
     private DatabaseHelper mDbHelper;
     private SQLiteDatabase mDb;
 
     /**
      * Database creation sql statement
      */
     private static final String DATABASE_TABLE = "carnum";
     private static final String CAR_DATABASE_CREATE = "create table " + DATABASE_TABLE + "(_id integer primary key autoincrement, carnum integer);";
     private static final String CAR_DATABASE_NAME = "carnumdb";
     private static final int DATABASE_VERSION = 2;
 
    private final Context mCtx;
 
     private static class DatabaseHelper extends SQLiteOpenHelper {
 
         DatabaseHelper(Context context) {
             super(context, CAR_DATABASE_NAME, null, DATABASE_VERSION);
         }
 
         @Override
         public void onCreate(SQLiteDatabase db) {
 
             db.execSQL(CAR_DATABASE_CREATE);
         }
 
         @Override
         public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
             Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                     + newVersion + ", which will destroy all old data");
             db.execSQL("DROP TABLE IF EXISTS notes");
             onCreate(db);
         }
     }
 
     /**
      * Constructor - takes the context to allow the database to be
      * opened/created
      * 
      * @param ctx the Context within which to work
      */
     public CarsGtBDbAdapter(Context ctx) {
         this.mCtx = ctx;
     }
 
     /**
      * Open the notes database. If it cannot be opened, try to create a new
      * instance of the database. If it cannot be created, throw an exception to
      * signal the failure
      * 
      * @return this (self reference, allowing this to be chained in an
      *         initialization call)
      * @throws SQLException if the database could be neither opened or created
      */
    public CarsGtBDbAdapter open() throws SQLException {
     		mDbHelper = new DatabaseHelper(mCtx);
    		mDb = mDbHelper.getWritableDatabase();
         return this;
     }
 
     public void close() {
        mDbHelper.close();
     }
 
 
     /**
      * Create a new note using the title and body provided. If the note is
      * successfully created return the new rowId for that note, otherwise return
      * a -1 to indicate failure.
      * 
      * @param carnum the car number
      * @return rowId or -1 if failed
      */
     public long setCar(int carnum) {
         Log.v(TAG, "Rows Deleted " + this.deleteAllRows());
     	ContentValues initialValues = new ContentValues();
         initialValues.put(KEY_ROWID, 1);
     	initialValues.put(KEY_CARNUM, carnum);
         
         return mDb.insert(DATABASE_TABLE, null, initialValues);
     }
 
     /**
      * Return a Cursor over the list of all notes in the database
      * 
      * @return Cursor over all notes
      */
     public int getCar() {
 
         Cursor curse = mDb.query(DATABASE_TABLE, new String[] {KEY_CARNUM}, KEY_ROWID + "= 1", null, null, null, null, null);
         curse.moveToFirst();
        Log.v(TAG, "getCar: returned rows: " + curse.getCount() + " " + curse.getColumnCount());
         return curse.getInt(0);
     }
     
     public int getRowId(int carnum){
     	Cursor curse = mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID}, KEY_CARNUM + "=" + carnum, null, null, null, null, null);
     	return curse.getInt(0);
     }
 
     /**
      * Update the note using the details provided. The note to be updated is
      * specified using the rowId, and it is altered to use the title and body
      * values passed in
      * 
      * @param rowId id of note to update
      * @param title value to set note title to
      * @param body value to set note body to
      * @return true if the note was successfully updated, false otherwise
      */
     public boolean updateCar(String carnum) {
         ContentValues args = new ContentValues();
         args.put(KEY_CARNUM, carnum);
 
         return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "= 1", null) > 0;
     }
     
     /**
      * Delete the note with the given rowId
      * 
      * @param rowId id of note to delete
      * @return true if deleted, false otherwise
      */
     public boolean deleteAllRows() {
 
         return mDb.delete(DATABASE_TABLE, "1", null) > 0;
     }
 }
