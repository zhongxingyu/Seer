 package com.example.therunningapp;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 import com.example.therunningapp.TrappContract.TrappEntry;
 
 public class TrappDBHelper extends SQLiteOpenHelper {
 	
 	public static final int DATABASE_VERSION = 3;
 	public static final String DATABASE_NAME = "TRAPP.db";
 	
 	//table for each workout
 	private static final String SQL_CREATE_WORKOUTLOG = "CREATE TABLE " 
 				+ TrappEntry.TABLE_NAME +
 				" (" + TrappEntry._ID + " INTEGER PRIMARY KEY, " + TrappEntry.COLUMN_NAME_DATE + " TEXT, "
 				+ TrappEntry.COLUMN_NAME_TIME + " FLOAT, " + TrappEntry.COLUMN_NAME_DISTANCE + " DOUBLE, " 
 				+ TrappEntry.COLUMN_NAME_CALORIES + " INTEGER)";
 	
 	//table for storing preferances
 	private static final String SQL_CREATE_PREF = "CREATE TABLE " 
 			+ TrappEntry.TABLE_NAMEPREF +
 			" (" + TrappEntry._ID + " INTEGER PRIMARY KEY, " + TrappEntry.COLUMN_NAME_NAME + " TEXT, "
 			+ TrappEntry.COLUMN_NAME_WEIGHT + " INTEGER, " + TrappEntry.COLUMN_NAME_HEIGHT + " INTEGER, "
 			+ TrappEntry.COLUMN_NAME_AGE + " INTEGER, " + TrappEntry.COLUMN_NAME_GENDER + " TEXT)";
 	
 	//table for storing Interval
 	private static final String SQL_CREATE_INTERVAL = "CREATE TABLE " + TrappEntry.TABLE_NAME_INTERVAL
 				+ " (" + TrappEntry._ID + " INTEGER PRIMARY KEY, " + TrappEntry.COLUMN_NAME_NAME + " TEXT, "
 				+ TrappEntry.COLUMN_NAME_RUN_TIME + " INTEGER, " + TrappEntry.COLUMN_NAME_PAUSE_TIME + " INTEGER, "
 				+ TrappEntry.COLUMN_NAME_REPETITION + " INTEGER)";
 
 	//table for storing Test
 		private static final String SQL_CREATE_TEST = "CREATE TABLE " + TrappEntry.TABLE_TESTS + 
 				" (" + TrappEntry.COLUMN_NAME_MIN + " INTEGER, " + TrappEntry.COLUMN_NAME_TEST_DISTANCE + " INTEGER, "
 				+ TrappEntry.COLUMN_NAME_SEC +" INTEGER, " + TrappEntry._ID + " INTEGER PRIMARY KEY, " 
 				+ TrappEntry.COLUMN_NAME_TEST_TYPE + " TEXT, "+ TrappEntry.COLUMN_NAME_TESTNAME + " TEXT)";
 	
 	//table for storing GPS locations
 	private static final String SQL_CREATE_LOCATIONS = "CREATE TABLE " + TrappEntry.TABLE_NAME_LOCATIONS
 				+ " (" + TrappEntry._ID + " INTEGER PRIMARY KEY, "
 				+ TrappEntry.COLUMN_NAME_WORKOUT + " INTEGER, "
 				+ TrappEntry.COLUMN_NAME_LATITUDE + " DOUBLE, "
 				+ TrappEntry.COLUMN_NAME_LONGITUDE + " DOUBLE)";
 
 		
 	//Delete table for workout
 	private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TrappEntry.TABLE_NAME;
 	//delete table for preferences
 	private static final String SQL_DELETE_ENTRIES1 = "DROP TABLE IF EXISTS " + TrappEntry.TABLE_NAMEPREF;
 	//delete table for intervals
 	private static final String SQL_DELETE_ENTRIES2 = "DROP TABLE IF EXISTS " + TrappEntry.TABLE_NAME_INTERVAL;
 	//delete table for test
 	private static final String SQL_DELETE_ENTRIES3 = "DROP TABLE IF EXISTS " + TrappEntry.TABLE_TESTS;
 	//delete table for GPS locations
 	private static final String SQL_DELETE_LOCATIONS = "DROP TABLE IF EXISTS " + TrappEntry.TABLE_NAME_LOCATIONS;
 	
 
 	public void onCreate(SQLiteDatabase db) {
 		db.execSQL(SQL_CREATE_WORKOUTLOG);
 		db.execSQL(SQL_CREATE_PREF);
 		db.execSQL(SQL_CREATE_INTERVAL);
 		db.execSQL(SQL_CREATE_TEST);
 		db.execSQL(SQL_CREATE_LOCATIONS);
 		
 		// Create a new map of values, where column names are the keys
 		ContentValues values1 = new ContentValues();
 		values1.put(TrappEntry.COLUMN_NAME_TESTNAME, "1K Test");
		values1.put(TrappEntry.COLUMN_NAME_TEST_TYPE , "Distance" );
 		values1.put(TrappEntry.COLUMN_NAME_TEST_DISTANCE, "1000");
 		values1.put(TrappEntry.COLUMN_NAME_MIN, "0");
 		values1.put(TrappEntry.COLUMN_NAME_SEC, "0");
 
 		db.insert(TrappEntry.TABLE_TESTS,
 		         null,
 		         values1);
 	
 		ContentValues values2 = new ContentValues();
 		values2.put(TrappEntry.COLUMN_NAME_TESTNAME, "3K Test");
		values2.put(TrappEntry.COLUMN_NAME_TEST_TYPE , "Distance" );
 		values2.put(TrappEntry.COLUMN_NAME_TEST_DISTANCE, "3000");
 		values2.put(TrappEntry.COLUMN_NAME_MIN, "0");
 		values2.put(TrappEntry.COLUMN_NAME_SEC, "0");
 	
 		
 		db.insert(TrappEntry.TABLE_TESTS,
 	         null,
 	         values2);
 		
 		ContentValues values3 = new ContentValues();
 		values3.put(TrappEntry.COLUMN_NAME_TESTNAME, "5K Test");
		values3.put(TrappEntry.COLUMN_NAME_TEST_TYPE , "Distance" );
 		values3.put(TrappEntry.COLUMN_NAME_TEST_DISTANCE, "5000");
 		values3.put(TrappEntry.COLUMN_NAME_MIN, "0");
 		values3.put(TrappEntry.COLUMN_NAME_SEC, "0");
 	
 	
 		db.insert(TrappEntry.TABLE_TESTS,
 	         null,
 	         values3);
 		
 		ContentValues values4 = new ContentValues();
 		values4.put(TrappEntry.COLUMN_NAME_TESTNAME, "10K Test");
		values4.put(TrappEntry.COLUMN_NAME_TEST_TYPE , "Distance" );
 		values4.put(TrappEntry.COLUMN_NAME_TEST_DISTANCE, "10000");
 		values4.put(TrappEntry.COLUMN_NAME_MIN, "0");
 		values4.put(TrappEntry.COLUMN_NAME_SEC, "0");
 	
 
 		db.insert(TrappEntry.TABLE_TESTS,
 	         null,
 	         values4);
 		
 		ContentValues values5 = new ContentValues();
 		values5.put(TrappEntry.COLUMN_NAME_TESTNAME, "Cooper Test");
 		values5.put(TrappEntry.COLUMN_NAME_TEST_TYPE , "" );
 		values5.put(TrappEntry.COLUMN_NAME_TEST_DISTANCE, "0");
 		values5.put(TrappEntry.COLUMN_NAME_MIN, "12");
 		values5.put(TrappEntry.COLUMN_NAME_SEC, "0");
 	
 		// Insert the new row, returning the primary key value of the new row
 		db.insert(TrappEntry.TABLE_TESTS,
 	         null,
 	         values5);
 		db.close();
 }
 
 
 	
 	public TrappDBHelper(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		
 	}
 	
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		db.execSQL(SQL_DELETE_ENTRIES);
 		db.execSQL(SQL_DELETE_ENTRIES1);
 		db.execSQL(SQL_DELETE_ENTRIES2);
 		db.execSQL(SQL_DELETE_ENTRIES3);
 		db.execSQL(SQL_DELETE_LOCATIONS);
 		onCreate(db);
 	}
 	
 	public void insertTests(){
 			
 			SQLiteDatabase db = getWritableDatabase();
 			
 			// Create a new map of values, where column names are the keys
 			ContentValues values = new ContentValues();
 			values.put(TrappEntry.COLUMN_NAME_NAME, "1K Test");
 			values.put(TrappEntry.COLUMN_NAME_TEST_TYPE , "distance" );
 			values.put(TrappEntry.COLUMN_NAME_DISTANCE, "1000");
 			values.put(TrappEntry.COLUMN_NAME_MIN, "0");
 			values.put(TrappEntry.COLUMN_NAME_SEC, "0");
 			
 			// Insert the new row, returning the primary key value of the new row
 			long newRowId;
 			newRowId = db.insert(
 			         TrappEntry.TABLE_NAME,
 			         null,
 			         values);
 		}
 	
 }
 
