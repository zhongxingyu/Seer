 package com.example.transitlogger;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class TripDBHelper extends SQLiteOpenHelper {
 
     private static final int DATABASE_VERSION = 1;
     public static final String TABLE_TRIPS = "trips";
 
     public static final String COLUMN_ID = "_id";
 	public static final String COLUMN_DISTANCE = "distance";
     private static final String TRIP_TABLE_CREATE =
                 "CREATE TABLE " + TABLE_TRIPS + " ("
         		+ COLUMN_ID + " integer primary key autoincrement,"
        		+ COLUMN_DISTANCE + " float not null";
 	private static final String DATABASE_NAME = "TransitLogger";
 
     TripDBHelper(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
     }
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
         db.execSQL(TRIP_TABLE_CREATE);
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 	    Log.w(TripDBHelper.class.getName(), "Upgrading database from version "
 	            + oldVersion + " to " + newVersion
 	            + ", which will destroy all old data");
 	        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRIPS);
 	        onCreate(db);
 	}
 
 }
