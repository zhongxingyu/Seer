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
 
 package de.questmaster.tudmensa;
 
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
 public class MealsDbAdapter {
 
 	public static final String KEY_LOCATION = "location";
 	public static final String KEY_DATE = "date";
 	public static final String KEY_MEAL_NUM = "num";
 	public static final String KEY_COUNTER = "counter";
 	public static final String KEY_NAME = "name";
 	public static final String KEY_TYPE = "type";
 	public static final String KEY_PRICE = "price";
 	public static final String KEY_INFO = "info";
 	public static final String KEY_ROWID = "_id";
 
 	private static final String TAG = "MealsDbAdapter";
 	private DatabaseHelper mDbHelper;
 	private SQLiteDatabase mDb;
 
 	/**
 	 * Database creation sql statement
 	 */
 	private static final String DATABASE_CREATE = "create table meals (_id integer primary key autoincrement, "
 			+ "location text not null, num short not null," + "date text not null, counter text not null,"
 			+ "name text not null, type text not null," + "price text not null, info text);";
 
 	private static final String DATABASE_NAME = "data";
 	private static final String DATABASE_TABLE = "meals";
 	private static final int DATABASE_VERSION = 7;
 
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
 			Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
 					+ ", which will destroy all old data");
 			db.execSQL("DROP TABLE IF EXISTS meals");
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
 	public MealsDbAdapter(Context ctx) {
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
 	public MealsDbAdapter open() throws SQLException {
 		mDbHelper = new DatabaseHelper(mCtx);
 		mDb = mDbHelper.getWritableDatabase();
 		return this;
 	}
 
	public boolean isOpen() {
		if (mDb != null)
			return mDb.isOpen();
		
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
 	public long createMeal(String location, String date, int num, String counter, String name, String type,
 			String price, String info) {
 		ContentValues initialValues = new ContentValues();
 		initialValues.put(KEY_LOCATION, location);
 		initialValues.put(KEY_DATE, date);
 		initialValues.put(KEY_MEAL_NUM, num);
 		initialValues.put(KEY_COUNTER, counter);
 		initialValues.put(KEY_NAME, name);
 		initialValues.put(KEY_TYPE, type);
 		initialValues.put(KEY_PRICE, price);
 		initialValues.put(KEY_INFO, info);
 
 		return mDb.insert(DATABASE_TABLE, null, initialValues);
 	}
 
 	/**
 	 * Delete the note with the given rowId
 	 * 
 	 * @param rowId
 	 *            id of note to delete
 	 * @return true if deleted, false otherwise
 	 */
 	public boolean deleteMeal(long rowId) {
 
 		return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
 	}
 
 	public boolean deleteOldMeal(String date) {
 
 		return mDb.delete(DATABASE_TABLE, KEY_DATE + "<\"" + date + "\"", null) > 0;
 	}
 
 	public boolean deleteAllMeal() {
 		return mDb.delete(DATABASE_TABLE, null, null) > 0;
 	}
 
 	/**
 	 * Return a Cursor over the list of all notes in the database
 	 * 
 	 * @return Cursor over all notes
 	 */
 	public Cursor fetchAllMeals() {
 
 		return mDb.query(DATABASE_TABLE, new String[] { KEY_ROWID, KEY_LOCATION, KEY_DATE, KEY_MEAL_NUM, KEY_COUNTER,
 				KEY_NAME, KEY_TYPE, KEY_PRICE, KEY_INFO }, null, null, null, null, null);
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
 	public Cursor fetchMeal(long rowId) {
 
 		Cursor mCursor = mDb.query(true, DATABASE_TABLE, new String[] { KEY_ROWID, KEY_LOCATION, KEY_DATE,
 				KEY_MEAL_NUM, KEY_COUNTER, KEY_NAME, KEY_TYPE, KEY_PRICE, KEY_INFO }, KEY_ROWID + "=" + rowId, null,
 				null, null, null, null);
 		if (mCursor != null) {
 			mCursor.moveToFirst();
 		}
 		return mCursor;
 
 	}
 
 	public Cursor fetchMealsOfDay(String location, String date) {
 
 		Cursor mCursor = mDb.query(true, DATABASE_TABLE, new String[] { KEY_ROWID, KEY_LOCATION, KEY_DATE,
 				KEY_MEAL_NUM, KEY_COUNTER, KEY_NAME, KEY_TYPE, KEY_PRICE, KEY_INFO }, KEY_DATE + "=\"" + date
 				+ "\" AND " + KEY_LOCATION + "=\"" + location + "\"", null, null, null, null, null);
 		if (mCursor != null) {
 			mCursor.moveToFirst();
 		}
 		return mCursor;
 
 	}
 
 	public Cursor fetchMealsOfGroupDay(String location, String date, String counter) {
 
 		Cursor mCursor = mDb.query(true, DATABASE_TABLE, new String[] { KEY_ROWID, KEY_LOCATION, KEY_DATE,
 				KEY_MEAL_NUM, KEY_COUNTER, KEY_NAME, KEY_TYPE, KEY_PRICE, KEY_INFO }, KEY_DATE + "=\"" + date
 				+ "\" AND " + KEY_LOCATION + "=\"" + location + "\" AND " + KEY_COUNTER + "=\"" + counter + "\"", null,
 				null, null, null, null);
 		if (mCursor != null) {
 			mCursor.moveToFirst();
 		}
 		return mCursor;
 
 	}
 
 	public Cursor fetchGroupsOfDay(String location, String date) {
 
 		Cursor mCursor = mDb.query(true, DATABASE_TABLE,
 				new String[] { KEY_ROWID, KEY_LOCATION, KEY_DATE, KEY_COUNTER }, KEY_DATE + "=\"" + date + "\" AND "
 						+ KEY_LOCATION + "=\"" + location + "\"", null, KEY_COUNTER, null, null, null);
 		if (mCursor != null) {
 			mCursor.moveToFirst();
 		}
 		return mCursor;
 
 	}
 
 	public long fetchMealId(String location, String date, String counter, int num) {
 		long result = -1;
 
 		Cursor mCursor = mDb.query(true, DATABASE_TABLE, new String[] { KEY_ROWID }, KEY_LOCATION + "=\"" + location
 				+ "\" AND " + KEY_DATE + "=\"" + date + "\" AND " + KEY_COUNTER + "=\"" + counter + "\" AND "
 				+ KEY_MEAL_NUM + "=" + num, null, null, null, null, null);
 		if (mCursor != null) {
 			if (mCursor.moveToFirst()) {
 				result = mCursor.getLong(0);
 			}
 		}
 		mCursor.close();
 		return result;
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
 	public boolean updateMeal(long rowId, String location, String date, int num, String counter, String name,
 			String type, String price, String info) {
 		ContentValues args = new ContentValues();
 		args.put(KEY_LOCATION, location);
 		args.put(KEY_DATE, date);
 		args.put(KEY_MEAL_NUM, num);
 		args.put(KEY_COUNTER, counter);
 		args.put(KEY_NAME, name);
 		args.put(KEY_TYPE, type);
 		args.put(KEY_PRICE, price);
 		args.put(KEY_INFO, info);
 
 		return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
 	}
 }
