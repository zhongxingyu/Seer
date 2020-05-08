 /*
  * Copyright (C) 2010 Daniel Jacobi
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
 
 package de.questmaster.fatremote;
 
 import java.net.UnknownHostException;
 
 import de.questmaster.fatremote.datastructures.FATDevice;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
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
 public class FatDevicesDbAdapter {
 
 	public static final String KEY_NAME = "name";
 	public static final String KEY_IP = "ip";
 	public static final String KEY_PORT = "port";
 	public static final String KEY_AUTODETECTED = "auto";
 	public static final String KEY_ROWID = "_id";
 
 	private static final String TAG = "FatDbAdapter";
 	private DatabaseHelper mDbHelper = null;
 	private SQLiteDatabase mDb = null;
 
 	/**
 	 * Database creation sql statement
 	 */
 	private static final String DATABASE_CREATE = "create table fatDevices (_id integer primary key autoincrement, " + "name text not null, ip text not null,"
 			+ "port integer DEFAULT 9999, auto integer DEFAULT 0);";
 
 	private static final String DATABASE_NAME = "data";
 	private static final String DATABASE_TABLE = "fatDevices";
 	private static final int DATABASE_VERSION = 2;
 
 	private final Context mCtx;
 
 	private static class DatabaseHelper extends SQLiteOpenHelper {
 
 		DatabaseHelper(Context context) {
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			db.execSQL(DATABASE_CREATE);
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
 			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
 			onCreate(db);
 		}
 	}
 
 	/**
 	 * Constructor - takes the context to allow the database to be
 	 * opened/created
 	 * 
 	 * @param ctx
 	 *            the Context within which to work
 	 */
 	public FatDevicesDbAdapter(Context ctx) {
 		this.mCtx = ctx;
 	}
 
 	/**
 	 * Open the notes database. If it cannot be opened, try to create a new
 	 * instance of the database. If it cannot be created, throw an exception to
 	 * signal the failure
 	 * 
 	 * @return this (self reference, allowing this to be chained in an
 	 *         initialization call)
 	 * @throws SQLException
 	 *             if the database could be neither opened or created
 	 */
 	public FatDevicesDbAdapter open() {
 		mDbHelper = new DatabaseHelper(mCtx);
 		mDb = mDbHelper.getWritableDatabase();
 		return this;
 	}
 
 	public boolean isOpen() {
 		if (mDb != null) {
 			return mDb.isOpen();
 		}
 		return false;
 	}
 
 	public void close() {
 		mDbHelper.close();
 	}
 
 	/**
 	 * Create a new note using the title and body provided. If the note is
 	 * successfully created return the new rowId for that note, otherwise return
 	 * a -1 to indicate failure.
 	 * 
 	 * @param title
 	 *            the title of the note
 	 * @param body
 	 *            the body of the note
 	 * @return rowId or -1 if failed
 	 */
 	public long createFatDevice(String name, String ip, int port, boolean autodetected) {
 		ContentValues initialValues = new ContentValues();
 		initialValues.put(KEY_NAME, name);
 		initialValues.put(KEY_IP, ip);
 		initialValues.put(KEY_PORT, port);
 		initialValues.put(KEY_AUTODETECTED, autodetected ? 1 : 0);
 
 		return mDb.insert(DATABASE_TABLE, null, initialValues);
 	}
 
 	/**
 	 * Delete the note with the given rowId
 	 * 
 	 * @param rowId
 	 *            id of note to delete
 	 * @return true if deleted, false otherwise
 	 */
 	public boolean deleteFatDevice(long rowId) {
 
 		return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
 	}
 
 	public boolean deleteAllFatDevices() {
 		return mDb.delete(DATABASE_TABLE, "1", null) > 0;
 	}
 
 	/**
 	 * Return a Cursor over the list of all notes in the database
 	 * 
 	 * @return Cursor over all notes
 	 */
 	public Cursor fetchAllFatDevices() {
 
 		return mDb.query(DATABASE_TABLE, new String[] { KEY_ROWID, KEY_NAME, KEY_IP, KEY_PORT, KEY_AUTODETECTED }, null, null, null, null, null);
 	}
 
 	/**
 	 * Return a Cursor positioned at the note that matches the given rowId
 	 * 
 	 * @param rowId
 	 *            id of note to retrieve
 	 * @return Cursor positioned to matching note, if found
 	 * @throws SQLException
 	 *             if note could not be found/retrieved
 	 */
 	public FATDevice fetchFatDeviceTyp(long rowId) {
		int columnName = 1;
		int columnIp = 2;
		int columnAutodetected = 4;
 		FATDevice device = null;
 		
 		Cursor mCursor = mDb.query(true, DATABASE_TABLE, new String[] { KEY_ROWID, KEY_NAME, KEY_IP, KEY_PORT, KEY_AUTODETECTED }, KEY_ROWID + "=" + rowId, null, null, null, null, null);
 		if (mCursor != null) {
 			mCursor.moveToFirst();
 			try {
				device = new FATDevice(mCursor.getString(columnName), mCursor.getString(columnIp), mCursor.getInt(columnAutodetected) == 1);
 			} catch (UnknownHostException e) {
 				Log.e(TAG, e.getLocalizedMessage(), e);
 			}
 			mCursor.close();
 		}
 
 		return device;
 	}
 
 	public long fetchFatDeviceId(String ip) {
		int columnRowid = 0;
 		long result = -1;
 
 		Cursor mCursor = mDb.query(true, DATABASE_TABLE, new String[] { KEY_ROWID }, KEY_IP + "=\"" + ip + "\"", null, null, null, null, null);
 		if (mCursor != null) {
 			if (mCursor.moveToFirst()) {
				result = mCursor.getLong(columnRowid);
 			}
 			mCursor.close();
 		}
 		return result;
 	}
 	
 	public int getAllFatDevicesCount() {
 		int cnt = 0;
 		
 		Cursor cur = fetchAllFatDevices();
 		if (cur != null) {
 			cnt = cur.getCount();
 			cur.close();
 		}
 			
 		return cnt;
 	}
 
 	/**
 	 * Update the note using the details provided. The note to be updated is
 	 * specified using the rowId, and it is altered to use the title and body
 	 * values passed in
 	 * 
 	 * @param rowId
 	 *            id of note to update
 	 * @param title
 	 *            value to set note title to
 	 * @param body
 	 *            value to set note body to
 	 * @return true if the note was successfully updated, false otherwise
 	 */
 	public boolean updateFatDevice(long rowId, String name, String ip, int port, boolean autodetected) {
 		ContentValues args = new ContentValues();
 		args.put(KEY_NAME, name);
 		args.put(KEY_IP, ip);
 		args.put(KEY_PORT, port);
 		args.put(KEY_AUTODETECTED, autodetected);
 
 		return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
 	}
 }
