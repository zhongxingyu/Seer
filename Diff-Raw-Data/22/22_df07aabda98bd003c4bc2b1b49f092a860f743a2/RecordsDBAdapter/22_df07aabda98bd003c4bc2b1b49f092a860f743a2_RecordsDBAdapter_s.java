 /*
  * Copyright (C) 2008 Google Inc.
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
 
 package com.conica.DailyCapture;
 
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
 public class RecordsDBAdapter {
     public static final String KEY_RECORD_ROWID = "_id";
     public static final String KEY_RECORD_NAME = "name";
     public static final String KEY_RECORD_DESP = "decription";
     public static final String KEY_RECORD_ALARMON = "alarmon";
     public static final String KEY_RECORD_PERIODTYPE = "periodtype"; // alarm default period type, for repeat alarm
     public static final String KEY_RECORD_PERIOD = "period"; // alarm specific period, for repeat alarm
     public static final String KEY_RECORD_DATE = "date"; // alarm specific date, for one time alarm
     public static final String KEY_RECORD_TIME = "time";   // alarm time
     public static final String KEY_RECORD_COUNT = "piccount";
     public static final String KEY_RECORD_TILE = "tile";
     public static final String KEY_RECORD_CREATDATE = "createdate";  
     public static final String KEY_RECORD_HASTABLE = "hastable";  // corresponding photo table name
     
     public static final String KEY_PHOTO_ROWID = "_id";
     public static final String KEY_PHOTO_NAME = "name";
     public static final String KEY_PHOTO_DESP = "decription";
     public static final String KEY_PHOTO_PATH = "path";
     public static final String KEY_PHOTO_THUNBPATH = "thumbpath";
     public static final String KEY_PHOTO_CREATDATE = "createdate";  
  
 
     private static final String TAG = "capture";
     
     private static final String DATABASE_NAME = "record.db";
     private static final String RECORDLIST_TABLE_NAME = "records";
     private static final int DATABASE_VERSION = 1;
 
     private final Context mCtx;
     
     private DatabaseHelper mDbHelper;
     private SQLiteDatabase mDb;
 
     /**
      * Database creation sql statement
      */
     private static final String RECORDLIST_TABLE_CREAT = "CREATE TABLE " +  RECORDLIST_TABLE_NAME + " (" 
     	+ KEY_RECORD_ROWID + " integer primary key autoincrement, " 
     	+ KEY_RECORD_NAME + " TEXT, " 
     	+ KEY_RECORD_DESP + " TEXT, " 
     	+ KEY_RECORD_ALARMON + " NUMERIC, " 
     	+ KEY_RECORD_PERIODTYPE + " NUMERIC, " 
     	+ KEY_RECORD_PERIOD + " NUMERIC, " 
     	+ KEY_RECORD_DATE + " TEXT, " 
     	+ KEY_RECORD_TIME + " TEXT, " 
     	+ KEY_RECORD_COUNT + " NUMERIC, " 
     	+ KEY_RECORD_TILE + " TEXT, " 
     	+ KEY_RECORD_CREATDATE + " TEXT, " 
     	+ KEY_RECORD_HASTABLE + " NUMERIC);";
 
     private static class DatabaseHelper extends SQLiteOpenHelper {
 
         DatabaseHelper(Context context) {
             super(context, DATABASE_NAME, null, DATABASE_VERSION);
         }
 
         @Override
         public void onCreate(SQLiteDatabase db) {
 
             db.execSQL(RECORDLIST_TABLE_CREAT);
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
     public RecordsDBAdapter(Context ctx) {
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
     public RecordsDBAdapter open() throws SQLException {
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
      * @param title the title of the note
      * @param body the body of the note
      * @return rowId or -1 if failed
      */
     public long insertRecord(String name, String desp, boolean alarmon, int periodtype, int period, String date, String time, int count, String tile, String createdate) {
         ContentValues initialValues = new ContentValues();
         initialValues.put(KEY_RECORD_NAME, name);
         initialValues.put(KEY_RECORD_DESP, desp);
         initialValues.put(KEY_RECORD_ALARMON, alarmon);
         initialValues.put(KEY_RECORD_PERIODTYPE, periodtype);
         initialValues.put(KEY_RECORD_PERIOD, period);
         initialValues.put(KEY_RECORD_DATE, date);
         initialValues.put(KEY_RECORD_TIME, time);
         initialValues.put(KEY_RECORD_COUNT, count);
         initialValues.put(KEY_RECORD_TILE, tile);
         initialValues.put(KEY_RECORD_CREATDATE, createdate);
         initialValues.put(KEY_RECORD_HASTABLE, false);
        
         return mDb.insert(RECORDLIST_TABLE_NAME, null, initialValues);
     }
 
     /**
      * Delete the note with the given rowId
      * 
      * @param rowId id of note to delete
      * @return true if deleted, false otherwise
      */
     public boolean deleteRecord(long rowId) {
 
         return mDb.delete(RECORDLIST_TABLE_NAME, KEY_RECORD_ROWID + "=" + rowId, null) > 0;
     }
 
     /**
      * Return a Cursor over the list of all notes in the database
      * 
      * @return Cursor over all notes
      */
     public Cursor fetchAllRecords() {
 
         return mDb.query(RECORDLIST_TABLE_NAME, new String[] {KEY_RECORD_ROWID, KEY_RECORD_NAME,
                 KEY_RECORD_DESP, KEY_RECORD_ALARMON,KEY_RECORD_ALARMON,KEY_RECORD_PERIOD,KEY_RECORD_DATE,KEY_RECORD_TIME,KEY_RECORD_COUNT,KEY_RECORD_TILE,KEY_RECORD_CREATDATE}, null, null, null, null, null); 
     }
 
     /**
      * Return a Cursor positioned at the note that matches the given rowId
      * 
      * @param rowId id of note to retrieve
      * @return Cursor positioned to matching note, if found
      * @throws SQLException if note could not be found/retrieved
      */
     public Cursor fetchRecordById(long rowId) throws SQLException {
 
         Cursor mCursor = mDb.query(true, RECORDLIST_TABLE_NAME, new String[] {KEY_RECORD_ROWID, KEY_RECORD_NAME,
                     KEY_RECORD_DESP, KEY_RECORD_ALARMON,KEY_RECORD_PERIODTYPE,KEY_RECORD_PERIOD,KEY_RECORD_DATE,KEY_RECORD_TIME,KEY_RECORD_COUNT,KEY_RECORD_TILE,KEY_RECORD_CREATDATE}, KEY_RECORD_ROWID + "=" + rowId, null,
                     null, null, null, null);
         if (mCursor != null) {
             mCursor.moveToFirst();
         }
         return mCursor;
 
     }
     
     public boolean checkRecordByName(String name) throws SQLException {    	
     	Cursor mCursor = mDb.query(RECORDLIST_TABLE_NAME, new String[] {KEY_RECORD_NAME}, KEY_RECORD_NAME + "='" + name + "'", null, null, null, null, null);
         if (mCursor != null) {
         	if(mCursor.getCount() > 0){
         		return true;
         	}else{
         		return false;
         	}
     	}
         return false;
     }
 
    public boolean updateRecord(long rowId, String name, String desp, boolean alarmon, int periodtype, int period, String date, String time, int count, String tile) {
         ContentValues args = new ContentValues();      
         args.put(KEY_RECORD_NAME, name);
         args.put(KEY_RECORD_DESP, desp);
         args.put(KEY_RECORD_ALARMON, alarmon);
         args.put(KEY_RECORD_PERIODTYPE, periodtype);
         args.put(KEY_RECORD_PERIOD, period);
         args.put(KEY_RECORD_DATE, date.toString());
         args.put(KEY_RECORD_TIME, time.toString());
        args.put(KEY_RECORD_COUNT, count);
        args.put(KEY_RECORD_TILE, tile);
 
         return mDb.update(RECORDLIST_TABLE_NAME, args, KEY_RECORD_ROWID + "=" + rowId, null) > 0;
     }
     
     public void updateRecordPhotoCnt(long tableid){
     	Cursor cursor = mDb.query(getTableName(tableid), new String[] {KEY_PHOTO_ROWID, KEY_PHOTO_NAME,
         	KEY_PHOTO_DESP, KEY_PHOTO_PATH, KEY_PHOTO_THUNBPATH, KEY_PHOTO_CREATDATE}, null, null, null, null, null); 
         if(cursor== null)
         	return;
      
         // get photo count of the photo table
     	int count = cursor.getCount();   	
     	// update the record table
     	ContentValues args = new ContentValues();      
         args.put(KEY_RECORD_COUNT, count);
         mDb.update(RECORDLIST_TABLE_NAME, args, KEY_RECORD_ROWID + "=" + tableid, null);	
         
         // update tile
     	if(count == 1){
     		cursor.moveToFirst();
     		long photoid = (long)cursor.getInt(cursor.getColumnIndex(KEY_PHOTO_ROWID));
     		updateRecordPhotoTile(tableid,photoid);
     	}
     }
     
     public void updateRecordPhotoTile(long tableid, long photoid){
     	Cursor cursor = mDb.query(true, getTableName(tableid), new String[] {KEY_PHOTO_ROWID, KEY_PHOTO_NAME,
         	KEY_PHOTO_DESP, KEY_PHOTO_PATH, KEY_PHOTO_THUNBPATH, KEY_PHOTO_CREATDATE}, KEY_PHOTO_ROWID + "=" + photoid, null,
             null, null, null, null);
         if(cursor== null)
         	return;
      
         cursor.moveToFirst();
         
         // get photo count of the photo table
     	String tilepath = cursor.getString(cursor.getColumnIndex(KEY_PHOTO_THUNBPATH));  	
     	// update the record table
     	ContentValues args = new ContentValues();      
         args.put(KEY_RECORD_TILE, tilepath);
         mDb.update(RECORDLIST_TABLE_NAME, args, KEY_RECORD_ROWID + "=" + tableid, null);	
     }
 
     public boolean markHasTableFlg(long rowId, boolean hastable){
         ContentValues args = new ContentValues();      
         args.put(KEY_RECORD_HASTABLE, hastable);
         return mDb.update(RECORDLIST_TABLE_NAME, args, KEY_RECORD_ROWID + "=" + rowId, null) > 0;
     }
      
     public void createNewPhotoTable(long tableid){
         String RECORE_I_TABLE_CREAT = "CREATE TABLE " +  getTableName(tableid) 
         	+ " (" + KEY_PHOTO_ROWID + " integer primary key autoincrement, " 
         	+ KEY_PHOTO_NAME + " TEXT, " 
         	+ KEY_PHOTO_DESP + " TEXT, " 
         	+ KEY_PHOTO_PATH + " TEXT, " 
         	+ KEY_PHOTO_THUNBPATH + " TEXT, " 
         	+ KEY_PHOTO_CREATDATE + " TEXT);";
         mDb.execSQL(RECORE_I_TABLE_CREAT);
         markHasTableFlg(tableid, true);
     }
     
     public void removeOldPhotoTable(long tableid){
         String RECORE_I_TABLE_DROP = "DROP TABLE " +  getTableName(tableid);
         mDb.execSQL(RECORE_I_TABLE_DROP);
         markHasTableFlg(tableid, false);
     }
     
     public boolean IsPhotoTableExist(long tableid){
     	Cursor mCursor = mDb.query(RECORDLIST_TABLE_NAME, new String[] {KEY_RECORD_HASTABLE}, KEY_RECORD_ROWID + "=" + tableid, null, null, null, null, null);
         if (mCursor != null) {
         	mCursor.moveToFirst();
         	if(mCursor.getCount() > 0){
         		return mCursor.getInt(0)!=0;
         	}else{
         		return false;
         	}
     	}
         return false;
     }
     
     public long insertPhoto(long tableid, String photoname, String desp, String path,String thumbpath, String createdate) {
     	Log.i("h","SQLiteDatabase.findEditTable(getTableName(tableid)) = " + SQLiteDatabase.findEditTable(getTableName(tableid)) );   	
     	ContentValues initialValues = new ContentValues();
         initialValues.put(KEY_PHOTO_NAME, photoname);
         initialValues.put(KEY_PHOTO_DESP, desp);
         initialValues.put(KEY_PHOTO_PATH, path);
         initialValues.put(KEY_PHOTO_PATH, path);
         initialValues.put(KEY_PHOTO_THUNBPATH, thumbpath);
         long id = mDb.insert(getTableName(tableid), null, initialValues);
         if(id >= 0){
         	updateRecordPhotoCnt(tableid);
         }
         return id;
     }
     
     public boolean deletePhoto(long tableid, long photoid) {
         boolean result = mDb.delete(getTableName(tableid), KEY_PHOTO_ROWID + "=" + photoid, null) > 0;
         if(result){
         	updateRecordPhotoCnt(tableid);
         }
         return result;
     }
 
     public Cursor fetchAllPhoto(long tableid) {
         return mDb.query(getTableName(tableid), new String[] {KEY_PHOTO_ROWID, KEY_PHOTO_NAME,
         	KEY_PHOTO_DESP, KEY_PHOTO_PATH, KEY_PHOTO_THUNBPATH, KEY_PHOTO_CREATDATE}, null, null, null, null, null); 
     }
     
     public Cursor fetchPhotoById(long tableid, long photoid) throws SQLException {
         Cursor mCursor = mDb.query(true, getTableName(tableid), new String[] {KEY_PHOTO_ROWID, KEY_PHOTO_NAME,
         	KEY_PHOTO_DESP, KEY_PHOTO_PATH, KEY_PHOTO_THUNBPATH, KEY_PHOTO_CREATDATE}, KEY_PHOTO_ROWID + "=" + photoid, null,
                     null, null, null, null);
         if (mCursor != null) {
             mCursor.moveToFirst();
         }
         return mCursor;
     }
     
     public boolean checkPhotoByName(long tableid, String photoname) throws SQLException {    	
     	Cursor mCursor = mDb.query(getTableName(tableid), new String[] {KEY_PHOTO_NAME}, KEY_PHOTO_NAME + "='" + photoname + "'", null, null, null, null, null);
         if (mCursor != null) {
         	if(mCursor.getCount() > 0){
         		return true;
         	}else{
         		return false;
         	}
     	}
         return false;
     }
     
     public String getTableName(long tableid){
     	return "record_" + Long.toString(tableid);
     }
 
 }
