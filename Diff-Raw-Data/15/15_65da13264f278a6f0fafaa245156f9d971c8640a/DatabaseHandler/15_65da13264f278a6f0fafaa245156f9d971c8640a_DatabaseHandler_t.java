 package com.example.PrayerTimes;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class DatabaseHandler extends SQLiteOpenHelper {
 
 	// All Static variables
 	// Database Version
 	private static final int DATABASE_VERSION = 1;
 
 	// Database Name
 	private static final String DATABASE_NAME = "cityManager";
 
 	// Contacts table name
 	private static final String TABLE_CITIES = "cities";
 
 	// Contacts Table Columns names
 	private static final String KEY_ID = "id";
 	private static final String KEY_NAME = "name";
 	private static final String KEY_LAT = "latitude";
 	private static final String KEY_LONG = "longitude";
 	private static final String KEY_TZ = "timezone";
 	private static final String KEY_useGPS = "useGPS";
 	private static final String KEY_useTimezone = "useTimezone";
 
 
 	public DatabaseHandler(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 
 	// Creating Tables
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		String CREATE_CITIES_TABLE = "CREATE TABLE " + TABLE_CITIES + "("
 				+ KEY_ID + " INTEGER PRIMARY KEY,"
 				+ KEY_NAME + " TEXT,"
 				+ KEY_LAT + " TEXT," 
 				+ KEY_LONG + " TEXT," 
 				+ KEY_TZ + " TEXT," 
 				+ KEY_useGPS + " TEXT," 
 				+ KEY_useTimezone + " TEXT" + ")";
 		Log.v("MyActivity", CREATE_CITIES_TABLE);
 		db.execSQL(CREATE_CITIES_TABLE);
 	}
 
 	// Upgrading database
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		// Drop older table if existed
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CITIES);
 
 		// Create tables again
 		onCreate(db);
 	}
 	public void addProfile(Profile profile){
 		SQLiteDatabase db = this.getWritableDatabase();
 		ContentValues values = new ContentValues();
 		values.put(KEY_NAME, profile.cityName);
 		values.put(KEY_LAT, ""+profile.savedLatitude);
 		values.put(KEY_LONG, ""+profile.savedLongitude);
 		values.put(KEY_TZ, ""+profile.savedTimezone);
 		values.put(KEY_useGPS, ""+profile.useGPS);
 		values.put(KEY_useTimezone, ""+profile.useTimezone);
 
		if(profileExists(profile.cityName))
			updateProfile(profile);
		else
			db.insert(TABLE_CITIES, null, values);
 
 	}
 	public Profile getProfile(String cityName){
 		Profile profile = new Profile();
 		SQLiteDatabase db = this.getReadableDatabase();
 
 		Cursor cursor = db.query(TABLE_CITIES, new String[] { KEY_ID,
 				KEY_NAME, KEY_LAT, KEY_LONG, KEY_TZ, KEY_useGPS, KEY_useTimezone }, KEY_NAME + "=?",
 				new String[] { cityName }, null, null, null, null);
 		if (cursor != null && cursor.moveToFirst()){
 
 			profile.cityName = cursor.getString(1);
 			profile.savedLatitude = Double.parseDouble(cursor.getString(2));
 			profile.savedLongitude = Double.parseDouble(cursor.getString(3));
 			profile.savedTimezone = Integer.parseInt(cursor.getString(4));
 			profile.useGPS = cursor.getString(5).equals("true");
 			profile.useTimezone = cursor.getString(6).equals("true");
 		}
 		return profile;
 	}
 
 	public void updateProfile(Profile profile){
 		deleteProfile(profile.cityName);
 		addProfile(profile);
 	}
 	public List<Profile> getAllProfiles(){
 		List<Profile> citiesList = new ArrayList<Profile>();
 		String selectQuery = "SELECT  * FROM " + TABLE_CITIES;
 		SQLiteDatabase db = this.getWritableDatabase();
 		Cursor cursor = db.rawQuery(selectQuery, null);
 
 		// looping through all rows and adding to list
 		if (cursor.moveToFirst()) {
 			do {
 				Profile profile = new Profile();
 
 				profile.cityName = cursor.getString(1);
 				profile.savedLatitude = Double.parseDouble(cursor.getString(2));
 				profile.savedLongitude = Double.parseDouble(cursor.getString(3));
 				profile.savedTimezone = Integer.parseInt(cursor.getString(4));
 				profile.useGPS = cursor.getString(5).equals("true");
 				profile.useTimezone = cursor.getString(6).equals("true");
 
 				// Adding contact to list
 				citiesList.add(profile);
 			} while (cursor.moveToNext());
 		}
 		return citiesList;
 	}
 
 	public void deleteProfile(String cityName) {
 		SQLiteDatabase db = this.getWritableDatabase();
 		// Check if the Profile is actually there
 		Cursor cursor = db.query(TABLE_CITIES, new String[] { KEY_ID,
 				KEY_NAME, KEY_LAT, KEY_LONG, KEY_TZ, KEY_useGPS, KEY_useTimezone }, KEY_NAME + "=?",
 				new String[] { cityName }, null, null, null, null);
 		// If yes, then delete
 		if (cursor.moveToFirst()){
 			db.delete(TABLE_CITIES, KEY_NAME + " = ?",
 					new String[] { cityName });
 		}
 	}
 
 	public Boolean profileExists(String cityName){
 		SQLiteDatabase db = this.getWritableDatabase();
 		Boolean result;
 		Cursor cursor = db.query(TABLE_CITIES, new String[] { KEY_ID,
 				KEY_NAME, KEY_LAT, KEY_LONG, KEY_TZ, KEY_useGPS, KEY_useTimezone }, KEY_NAME + "=?",
 				new String[] { cityName }, null, null, null, null);

 		result = cursor!=null && cursor.moveToFirst();
 		return result;
 	}
 }
