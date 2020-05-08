 package com.codefox421.applevels;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.util.Log;
 
 public class AppLevelsDBAdapter {
 
 	// Database fields
 	private static final String LOG_TAG = "AppLevelsDBAdap";
 	private Context context;
 	private SQLiteDatabase database;
 	private AppLevelsDBHelper dbHelper;
 	
 	
 	public AppLevelsDBAdapter(Context context) {
 		this.context = context;
 	}
 	
 	
 	public AppLevelsDBAdapter open() throws SQLException {
 		dbHelper = new AppLevelsDBHelper(context);
 		database = dbHelper.getWritableDatabase();
 		return this;
 	}
 	
 	
 	public void close() {
 		dbHelper.close();
 	}
 	
 	
 	/**
 	 * VOLUME RECORDS
 	 *   package
 	 *   volume
 	 */
 	
 	public boolean updateAppVolume(String packageName, int volumeLevel) {
 		
 		// Compile values
 		ContentValues valuesToUpdate = new ContentValues();
		valuesToUpdate.put(AppLevelsDBHelper.KEY_PACKAGE, packageName);
 		valuesToUpdate.put(AppLevelsDBHelper.KEY_VOLUME, volumeLevel);
 		
 		// Write to database
 		boolean updateSucceeded = false;
 		try {
			Cursor mCursor = database.query(true, AppLevelsDBHelper.VOLUME_TABLE, new String[] { AppLevelsDBHelper.KEY_ID },
					AppLevelsDBHelper.KEY_PACKAGE + "=" + packageName, null, null, null, null, null);
			mCursor.moveToFirst();
			int _id = mCursor.getInt(mCursor.getColumnIndex(AppLevelsDBHelper.KEY_ID));
			Log.d(LOG_TAG, "ID for " + packageName + " is " + _id);
 			updateSucceeded = database.update(AppLevelsDBHelper.VOLUME_TABLE, valuesToUpdate,
					AppLevelsDBHelper.KEY_ID + "=" + _id, null) > 0;
 		} catch(SQLiteException ex_up) {
 			Log.w(LOG_TAG, "Could not update volume record for " + packageName + "\nAttempting new entry...");
 			try {
 				updateSucceeded = database.insertOrThrow(AppLevelsDBHelper.VOLUME_TABLE, null, valuesToUpdate) != -1;
 			} catch(SQLiteException ex_in) {
 				//well damn, it failed twice
 				Log.e(LOG_TAG, "SQLite error while inserting new record for " + packageName);
 			}
 		}
 		return updateSucceeded;
 	}
 	
 	
 	public int getAppVolume(String packageName) throws SQLException {
 		// Values less than 0 indicate an error
 		
 		// Query the database
 		Cursor mCursor;
 		try {
 			mCursor = database.query(true, AppLevelsDBHelper.VOLUME_TABLE, new String[] { AppLevelsDBHelper.KEY_VOLUME },
					AppLevelsDBHelper.KEY_PACKAGE + "=" + packageName, null, null, null, null, null);
 		} catch(SQLiteException ex) {
 			return -1;		//query error
 		} catch(Exception ex) {
 			return -3;		//unknown error
 		}
 		
 		// Verify cursor and extract value
 		if (mCursor != null) {
 			int value = mCursor.getInt(mCursor.getColumnIndex(AppLevelsDBHelper.KEY_VOLUME));
 			mCursor.close();
 			return value;
 		}
 		
 		return -2;		//null cursor error
 	}
 	
 	
 	public Cursor GetAppVolumes() throws SQLException {
 		
 		// Query the database
 		Cursor mCursor;
 		try {
 			mCursor = database.query(AppLevelsDBHelper.VOLUME_TABLE, new String[] { AppLevelsDBHelper.KEY_PACKAGE, AppLevelsDBHelper.KEY_VOLUME },
 					null, null, null, null, null);
 		} catch(SQLiteException ex) {
 			return null;
 		}
 		
 		// Verify cursor
 		if (mCursor != null) {
 			mCursor.moveToFirst();
 		}
 		
 		return mCursor;
 	}
 	
 	
 	public boolean deleteAppVolume(String packageName) {
 		
 		boolean deleteSucceeded = false;
 		try {
			deleteSucceeded = database.delete(AppLevelsDBHelper.VOLUME_TABLE, AppLevelsDBHelper.KEY_PACKAGE + "=" + packageName, null) > 0;
 		} catch(SQLiteException ex) {
 			
 		}
 		return deleteSucceeded;
 	}
 }
