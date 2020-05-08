 package com.digitalfingertip.mobisevera;
 
 import android.app.Activity;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 
 public class MobiseveraContentStore {
 	
 	private static final String TAG = "Sevedroid";
 	private static final String KEY_ROW_ID = "_id";
 	private static final String KEY_PARAM_NAME = "name";
 	private static final String KEY_PARAM_VALUE = "value";
 	
 	private static final String DATABASE_NAME = "sevedroiddatabase";
 	private static final String DATABASE_TABLE_PARAMETERS = "sevedroidparameters";
 	
 	private static final String PARAM_NAME_API_KEY = "API_KEY";
 	private static final String PARAM_NAME_USER_FNAME = "USER_FIRST_NAME";
 	private static final String PARAM_NAME_USER_GUID = "USER_GUID";
 	private static final String PARAM_NAME_USER_ISACTIVE = "USER_IS_ACTIVE";
 	
 	private static final int DATABASE_VERSION = 1;
 	
 	private static final String DATABASE_CREATE_PARAMETERS = 
 		"create table "+DATABASE_TABLE_PARAMETERS+" ("+KEY_ROW_ID+" integer primary key autoincrement, "
 		+KEY_PARAM_NAME+" integer not null,"
 		+KEY_PARAM_VALUE+" integer not null);";
 	
 	private final Context context = null;
 	private DatabaseHelper DBHelper;
 	private SQLiteDatabase db = null;
 
     private static class DatabaseHelper extends SQLiteOpenHelper {
 
 		public DatabaseHelper(Context context) {
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		}
 		
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			db.execSQL(DATABASE_CREATE_PARAMETERS);
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			Log.w(TAG, "Requested to update database from version "+oldVersion+" to "+newVersion);
 		}
 		
 	}
     
 	public MobiseveraContentStore(Activity caller) {
 				
 		DBHelper = new DatabaseHelper(caller);
 		
 	}
 	
 	public MobiseveraContentStore open() throws SQLException {
 		if(db == null || !db.isOpen()) {
 			db = DBHelper.getWritableDatabase();
 		}
 		return this;	
 	}
 	
 	/**
 	 * Insert API key to application database. Should one already exist, this method deletes the old one and 
 	 * insert only after that.
 	 * @param newKey
 	 * @return
 	 */
 	
 	public long insertApiKey(String newKey) {
 		if(db == null || !db.isOpen()) {
 			this.open();
 		}
 		db.delete(DATABASE_TABLE_PARAMETERS, KEY_PARAM_NAME+" = ?", new String[] {PARAM_NAME_API_KEY});
 		ContentValues cv = new ContentValues();
 		cv.put(KEY_PARAM_NAME, PARAM_NAME_API_KEY);
 		cv.put(KEY_PARAM_VALUE, newKey);
 		long res = db.insert(DATABASE_TABLE_PARAMETERS, null, cv);
 		this.close();
 		return res;
 	}
 	
 	/**
 	 * Fetch the saved severa API key, or NULL if API Key is not set
 	 * @return
 	 */
 	public String fetchApiKey() {
 		/**
 		 * Executes query SELECT param_value from sevedroiddatabase where param_name = 'API_KEY';
 		 */
 		String query = "SELECT "+KEY_PARAM_VALUE+" FROM "+DATABASE_TABLE_PARAMETERS+ " WHERE "+
 				KEY_PARAM_NAME+" = '"+PARAM_NAME_API_KEY+"'";
 		Log.d(TAG,"Querying for API KEY with query: "+query);
 		if(db == null || !db.isOpen()) {
 			this.open();
 		}
 		Cursor tempCursor = db.rawQuery(query,null);	
 		if(tempCursor.getCount() == 0) {
 			/* no api key in db */
 			tempCursor.close();
 			close();
 			return null; 
 		} else if(tempCursor.getCount() > 1) {
 			Log.e(TAG,"More than on row returned when querying parameter "+PARAM_NAME_API_KEY+" from Db!");
 			tempCursor.close();
 			close();
 			throw new IllegalStateException("More than one value in db from a parameter key!");
 		} else {
 			tempCursor.moveToFirst();
 			String apikey = tempCursor.getString(0);
 			Log.d(TAG,"Returning api key from DB: "+apikey);
 			tempCursor.close();
 			close();
 			return apikey;
 		}
 	}
 	
 	public void close() {
 		DBHelper.close();
 	}
 
 	public long insertUserFirstName(String firstName) {
 		if(db == null || !db.isOpen()) {
 			this.open();
 		}
 		db.delete(DATABASE_TABLE_PARAMETERS, KEY_PARAM_NAME+" = ?", new String[] {PARAM_NAME_USER_FNAME});
 		ContentValues cv = new ContentValues();
 		cv.put(KEY_PARAM_NAME, PARAM_NAME_USER_FNAME);
 		cv.put(KEY_PARAM_VALUE, firstName);
 		long res = db.insert(DATABASE_TABLE_PARAMETERS, null, cv);
 		this.close();
 		return res;
 	}
 
 	public long insertUserGUID(String userGUID) {
 		if(db == null || !db.isOpen()) {
 			this.open();
 		}
 		db.delete(DATABASE_TABLE_PARAMETERS, KEY_PARAM_NAME+" = ?", new String[] {PARAM_NAME_USER_GUID});
 		ContentValues cv = new ContentValues();
 		cv.put(KEY_PARAM_NAME, PARAM_NAME_USER_GUID);
 		cv.put(KEY_PARAM_VALUE, userGUID);
 		long res = db.insert(DATABASE_TABLE_PARAMETERS, null, cv);
 		this.close();
 		return res;
 	}
 
 	public long insertUserIsActive(String isActive) {
 		if(db == null || !db.isOpen()) {
 			this.open();
 		}
 		db.delete(DATABASE_TABLE_PARAMETERS, KEY_PARAM_NAME+" = ?", new String[] {PARAM_NAME_USER_ISACTIVE});
 		ContentValues cv = new ContentValues();
 		cv.put(KEY_PARAM_NAME, PARAM_NAME_USER_ISACTIVE);
 		cv.put(KEY_PARAM_VALUE, isActive);
 		long res = db.insert(DATABASE_TABLE_PARAMETERS, null, cv);
 		this.close();
 		return res;
 	}
 	
 	public String fetchUserGUID() {
 		/**
 		 * Executes query SELECT param_value from sevedroiddatabase where param_name = 'USER_GUID';
 		 */
 		String query = "SELECT "+KEY_PARAM_VALUE+" FROM "+DATABASE_TABLE_PARAMETERS+ " WHERE "+
 				KEY_PARAM_NAME+" = '"+PARAM_NAME_USER_GUID+"'";
 		Log.d(TAG,"Querying for USER GUID with query: "+query);
 		if(db == null || !db.isOpen()) {
 			this.open();
 		}
 		Cursor tempCursor = db.rawQuery(query,null);	
 		if(tempCursor.getCount() == 0) {
 			/* no guid in db */
 			tempCursor.close();
 			close();
 			return null; 
 		} else if(tempCursor.getCount() > 1) {
 			Log.e(TAG,"More than on row returned when querying parameter "+PARAM_NAME_USER_GUID+" from Db!");
 			tempCursor.close();
 			close();
 			throw new IllegalStateException("More than one value in db from a parameter key!");
 		} else {
 			tempCursor.moveToFirst();
 			String userGuid = tempCursor.getString(0);
 			Log.d(TAG,"Returning api key from DB: "+userGuid);
 			tempCursor.close();
 			close();
 			return userGuid;
 		}
 	}
 }
