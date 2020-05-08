 package com.drewschrauf.robotronic.database;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 
 public class DatabaseHandler {
 
 	DatabaseHelper helper;
 
 	public DatabaseHandler(Context context) {
 		helper = new DatabaseHelper(context);
 	}
 
 	public void addData(String url, String data) {
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.COLUMN_URL, url);
		values.put(DatabaseHelper.COLUMN_DATA, data);
		values.put(DatabaseHelper.COLUMN_FETCHED_DATE,
				System.currentTimeMillis());
		db.replace(DatabaseHelper.TABLE_NAME, null, values);
		db.close();
 	}
 
 	/**
 	 * Fetch the cached data for the given URL
 	 * 
 	 * @param url
 	 *            The URL to be looked up in the cache
 	 * @return The cached data from the URL or null if there is none available
 	 */
 	public String getData(String url) {
 		try {
 			SQLiteDatabase db = helper.getReadableDatabase();
 			String[] columns = { DatabaseHelper.COLUMN_DATA };
 			Cursor cursor = db.query(DatabaseHelper.TABLE_NAME, columns,
 					DatabaseHelper.COLUMN_URL + " = '" + url + "'", null, null,
 					null, "1");
 			String data = null;
 			while (cursor.moveToNext()) {
 				data = cursor.getString(0);
 			}
 			cursor.close();
 			db.close();
 			return data;
 		} catch (Exception e) {
 			// database was closed before it could be read, just skip it this
 			// time
 			Log.e("DatabaseHandler",
 					"Database could not be read, skipping cache");
 			return null;
 		}
 
 	}
 
 }
