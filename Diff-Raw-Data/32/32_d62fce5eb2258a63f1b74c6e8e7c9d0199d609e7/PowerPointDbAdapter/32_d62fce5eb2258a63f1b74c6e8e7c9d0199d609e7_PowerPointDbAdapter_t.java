 package sta.andswtch.db;
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
 
 import sta.andswtch.extensionLead.ExtensionLead;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.text.format.Time;
 import android.util.Log;
 	
 	
 	public class PowerPointDbAdapter {
 
     public static final String KEY_ENDTIME = "endTime";
     public static final String KEY_HOURS = "hours";
     public static final String KEY_MINUTES = "minutes";
     public static final String KEY_SECONDS = "seconds";
     public static final String KEY_ROWID = "_id";
 
     private static final String TAG = PowerPointDbAdapter.class.getName();
     private DatabaseHelper mDbHelper;
     private SQLiteDatabase mDb;
     
     /**
      * Database creation sql statement
      */
     private static final String DATABASE_CREATE =
             "create table powerPoints (_id integer primary key autoincrement, "
                     + "endTime String not null, hours integer not null," 
                     + "minutes integer not null,seconds integer not null);";
 
     private static final String DATABASE_NAME = "andswtch";
     private static final String DATABASE_TABLE = "powerPoints";
     private static final int DATABASE_VERSION = 1;
 
     private final Context mCtx;
 
     private static class DatabaseHelper extends SQLiteOpenHelper {
 
         DatabaseHelper(Context context) {
             super(context, DATABASE_NAME, null, DATABASE_VERSION);
         }
 
         @Override
         public void onCreate(SQLiteDatabase db) {
             db.execSQL(DATABASE_CREATE);
             for(int i=0; i<ExtensionLead.POWERPOINTCNT; i++){
             	Log.d(TAG, "creating database entry");
                 addRow(db, 0, 0, 0);	
             }
 
         }
 
         /**
          * Adds a row to the database with the current time, and the 3 values of hour, minute and second.
          * is only needed during initialisazion
          * 
          * @return rowId or -1 if failed
          */
         private long addRow(SQLiteDatabase mDb,int hours, int minutes, int seconds) {
             
             Time now = new Time();
             now.setToNow();
             String nowString = now.format2445();
         	
         	ContentValues initialValues = new ContentValues();
             initialValues.put(KEY_ENDTIME, nowString);
             initialValues.put(KEY_HOURS, hours);
             initialValues.put(KEY_MINUTES, hours);
             initialValues.put(KEY_SECONDS, seconds);
             
 
             
 
             return mDb.insert(DATABASE_TABLE, null, initialValues);
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
     public PowerPointDbAdapter(Context ctx) {
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
     public PowerPointDbAdapter open() throws SQLException {
         mDbHelper = new DatabaseHelper(mCtx);
         mDb = mDbHelper.getWritableDatabase();
         return this;
     }
     
     public void close() {
         mDbHelper.close();
     }
 
 
     /**
      * Return a Cursor positioned at the note that matches the given rowId
      * 
      * @param rowId id of note to retrieve
      * @return Cursor positioned to matching note, if found
      * @throws SQLException if note could not be found/retrieved
      */
     public PowerPointRow fetchPowerPointRow(long rowId) throws SQLException {
 
         Cursor mCursor =
 
                 mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_ENDTIME,
                         KEY_HOURS, KEY_MINUTES, KEY_SECONDS}, KEY_ROWID + "=" + rowId, null,
                         null, null, null, null);
         if (mCursor != null) {
             mCursor.moveToFirst();
         }
         
        Time endTime = new Time();
        endTime.parse(mCursor.getString(1));
        
         PowerPointRow row = new PowerPointRow(endTime, mCursor.getInt(2), mCursor.getInt(3), mCursor.getInt(4));
         
        mCursor.close();
        
         return row;
 
     }
 
     /**
      * Update the PowerPoint Row
      * 
      * @return true if the note was successfully updated, false otherwise
      */
     public boolean updatePowerPointRow(long rowId, int delaySeconds, int hours, int minutes, int seconds) {
         Time now = new Time();
         now.setToNow();
         Time endTime = new Time();
         endTime.set(now.toMillis(true)+delaySeconds*1000);
         String endTimeString = endTime.format2445();
     	
     	ContentValues args = new ContentValues();
 
         args.put(KEY_ENDTIME, endTimeString);
         args.put(KEY_HOURS, hours);
         args.put(KEY_MINUTES, minutes);
         args.put(KEY_SECONDS, seconds);
 
         return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
     }
 	
 }
