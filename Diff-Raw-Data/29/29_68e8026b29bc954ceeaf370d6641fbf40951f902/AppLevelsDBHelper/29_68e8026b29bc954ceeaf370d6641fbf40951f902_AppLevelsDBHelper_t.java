 package com.codefox421.applevels;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class AppLevelsDBHelper extends SQLiteOpenHelper {
 	
 	public static final String LOG_TAG = "AppLevelsDBHelp";
 	public static final String VOLUME_TABLE = "volume_records";
 	public static final String KEY_ID = "id";
	public static final String KEY_PACKAGE = "_id";
 	public static final String KEY_VOLUME = "volume";
 	
 	private static final String DATABASE_NAME = "applevels_appdata";
 
 	private static final int DATABASE_VERSION = 1;
 
 	
 	// Create database for volume levels
 	private static final String VOLUME_DB_CREATE = "create table " + VOLUME_TABLE + " ("
			+ KEY_PACKAGE + " text primary key, "
 			+ KEY_VOLUME + " integer not null);";
 
 
 	// Constructor
 	public AppLevelsDBHelper(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 
 
 	// Called when first created, creates databases tables if they do not exist
 	@Override
 	public void onCreate(SQLiteDatabase database) {
 		database.execSQL(VOLUME_DB_CREATE);
 		
 	}
 
 
 	// Called during an update of the database (if you increase the database version)
 	@Override
 	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
 		Log.w(AppLevelsDBHelper.class.getName(),
 				"AppLevels: Upgrading database from version " + oldVersion + " to "
 						+ newVersion + ", which may destroy data...");
 		// TODO: enact database upgrade changes
 	}
 }
