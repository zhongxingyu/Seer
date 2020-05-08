 /*
  * project	SeattleCoffeeLocator
  * 
  * package	com.rbarnes.other
  * 
  * @author	Ronaldo Barnes
  * 
  * date		Mar 20, 2013
  */
 package com.rbarnes.other;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class LocationDB extends SQLiteOpenHelper{
 
 	
 	private static final String DEBUG_TAG = "LocationDatabase";
 	public static final String DB_NAME = "locationDB";
 	private static final int DB_VERSION = 1;
 	
 	public static final String TABLE_NAME = "locations";
 	public static final String ID = "_id";
 	public static final String COL_TITLE = "title";
 	public static final String COL_ADDRESS = "address";
 	public static final String COL_CITY = "city";
 	public static final String COL_STATE = "state";
 	public static final String COL_PHONE = "phone";
 	public static final String COL_COORDS = "cords";
	private static final String CREATE_TABLE = "create table " + TABLE_NAME
			+ " (" + ID + " integer primary key autoincrement, " + COL_TITLE + " text UNIQUE NOT NULL, " + COL_ADDRESS + " text UNIQUE NOT NULL, " + COL_CITY + " text UNIQUE NOT NULL, "
			+ COL_STATE + " text UNIQUE NOT NULL, " + COL_PHONE + " text UNIQUE NOT NULL, " + COL_COORDS + " text UNIQUE NOT NULL);";
 	private static final String DB_SCHEMA = CREATE_TABLE;		
 			
 			
 	public LocationDB(Context context){
 		super(context, DB_NAME, null, DB_VERSION);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db){
 		db.execSQL(DB_SCHEMA);
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
 		Log.w(DEBUG_TAG, "Upgrading database. Existing contents will be lost. ["
 	            + oldVersion + "]->[" + newVersion + "]");
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
 		onCreate(db);
 	    
 	    
 	}
 
 
 	
 }
