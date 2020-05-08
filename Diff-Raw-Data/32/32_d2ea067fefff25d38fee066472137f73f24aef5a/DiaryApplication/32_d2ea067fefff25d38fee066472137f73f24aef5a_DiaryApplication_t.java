 /*
  * TODO:
  * 		Backup manager
  * 		Search
  * 		Tags for each note
  * 			- Colored tags in list
  * 		Change password (Unencrypt + reencrypt all!)
  * 		Enclose 'startActivity' with catch for activityNotFound
  * 		Catch 'OutOfMemory' errors for bitmaps..
  */
 
 
 package com.DGSD.SecretDiary;
 
 import android.app.Application;
 import android.content.ContentValues;
 
 public class DiaryApplication extends Application{
 
 	public static final String KEY_HAS_LOGGED_IN_BEFORE = "has_logged_in_flag";
 
 	public static final String KEY_MY_PREFERENCES = "preferences";
 
 	public static final String KEY_ENCRYPTION_TEST = "encryption_test";
 	
 	public static final String KEY_PASSWORD_HINT = "password_hint";
 
 	private Database mDatabase;
 
 	private String mPassword;
 
 	private String mPasswordHint;
 	
 	@Override
 	public void onCreate() {  
 		super.onCreate();
 
 		mDatabase = new Database(this);
 	}
 
 	public boolean addEntry(String key, String value, String uris, String files, double[] location) {
 		boolean retval = true;
 
 		if(mDatabase == null) {
 			retval = false;
 		} else {
 			ContentValues values = new ContentValues();
 			try {
 				values.put(Database.C_KEY, Encryption.encrypt(mPassword, key));
 				values.put(Database.C_VALUE, Encryption.encrypt(mPassword, value));
 				values.put(Database.C_IMG_URI, Encryption.encrypt(mPassword, uris));
 				values.put(Database.C_FILES, Encryption.encrypt(mPassword, files));
 				values.put(Database.C_LAT, Encryption.encrypt(mPassword, Double.toString(location[0])));
 				values.put(Database.C_LONG, Encryption.encrypt(mPassword, Double.toString(location[1])));
 				values.put(Database.C_DATE, 
 						String.valueOf(System.currentTimeMillis()));
 			}catch(Exception e) {
 				return false;
 			}
 
 			retval = mDatabase.insert(Database.TABLE_NAME, values);
 		}
 
 		return retval;
 	}
 
 	public int updateEntry(int id, String key, String value, String uris, String files, double[] location) {
 		int retval;
 
 		if(mDatabase == null) {
 			retval = -1;
 		} else {
 			ContentValues values = new ContentValues();
 
 			try {
 				values.put(Database.C_KEY, Encryption.encrypt(mPassword, key));
 				values.put(Database.C_VALUE, Encryption.encrypt(mPassword, value));
 				values.put(Database.C_IMG_URI, Encryption.encrypt(mPassword, uris));
 				values.put(Database.C_FILES, Encryption.encrypt(mPassword, files));
 				values.put(Database.C_LAT, Encryption.encrypt(mPassword, Double.toString(location[0])));
 				values.put(Database.C_LONG, Encryption.encrypt(mPassword, Double.toString(location[1])));
 				values.put(Database.C_DATE, 
 						String.valueOf(System.currentTimeMillis()));
 			}catch(Exception e) {
 				return -1;
 			}
 
 			retval = mDatabase.update(Database.TABLE_NAME, id, values);
 		}
 
 		return retval;
 
 
 	}
 
 	public Database getDatabase() {
 		return mDatabase;
 	}
 
 	public void setPassword(String password) {
 		mPassword = password;
 	}
 	
 	public String getPassword() {
 		return mPassword;
 	}
 	
 	public void setPasswordHint(String hint) {
 		mPasswordHint = hint;
 	}
 	
 	public String getPasswordHint() {
 		return mPasswordHint;
 	}
 }
