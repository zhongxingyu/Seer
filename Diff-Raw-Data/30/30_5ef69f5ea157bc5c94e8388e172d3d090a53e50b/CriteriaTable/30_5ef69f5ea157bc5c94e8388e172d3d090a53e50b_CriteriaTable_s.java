 package com.teamswag.course_organizer;
 
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 
 public class CriteriaTable {
 	public static final String NAME = "criteria";
 	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "name";
 	public static final String COLUMN_WEIGHT = "weight";
 	public static final String COLUMN_COURSE_ID = "course_id";
 
 	public static void onCreate(SQLiteDatabase db) {
 		db.execSQL("CREATE TABLE " + NAME + " ( " + COLUMN_ID
 				+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_NAME
 				+ " TEXT NOT NULL, " + COLUMN_WEIGHT + " REAL, "
 				+ COLUMN_COURSE_ID + " INTEGER);");
 	}
 
 	public static void onUpgrade(SQLiteDatabase db, int oldVersion,
 			int newVersion) {
 
 		Log.w(CriteriaTable.class.getName(), "Upgrading from version "
 				+ oldVersion + " to version " + newVersion
 				+ ", which will destroy all data");
 		db.execSQL("DROP TABLE IF EXISTS " + NAME);
 		onCreate(db);
 	}
 }
