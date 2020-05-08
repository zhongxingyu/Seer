 package com.teamswag.course_organizer;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 
 public class SemesterTable {
 	public static final String NAME = "semester";
 	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "name";
 	public static final String COLUMN_YEAR_ID = "year_id";
 
 	public static void onCreate(SQLiteDatabase db) {
 		db.execSQL("CREATE TABLE " + NAME + " ( " + COLUMN_ID
 				+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_NAME
 				+ " TEXT NOT NULL, " + COLUMN_YEAR_ID + " TEXT NOT NULL);");
 	}
 
 	public static void onUpgrade(SQLiteDatabase db, int oldVersion,
 			int newVersion) {
 
 		Log.w(SemesterTable.class.getName(), "Upgrading from version "
 				+ oldVersion + " to version " + newVersion
 				+ ", which will destroy all data");
 		db.execSQL("DROP TABLE IF EXISTS " + NAME);
 		onCreate(db);
 	}
 
 	public static String getId(String name, DatabaseHelper db) {
 		Cursor cursor = db.getReadableDatabase()
 				.rawQuery(
 						"SELECT " + SemesterTable.COLUMN_ID + " FROM "
 								+ SemesterTable.NAME + " WHERE "
 								+ SemesterTable.COLUMN_NAME + "=\'" + name
 								+ "\'", null);
 		cursor.moveToFirst();
 		return cursor.getString(0);
 	}
 
 	public static void add(String name, String yearId, DatabaseHelper db) {
 		ContentValues cv = new ContentValues(2);
 
 		cv.put(SemesterTable.COLUMN_NAME, name);
 		cv.put(SemesterTable.COLUMN_YEAR_ID, yearId);
 		db.getWritableDatabase().insert(SemesterTable.NAME, null, cv);
 	}
 
 	protected static void delete(String name, DatabaseHelper db) {
 		String semesterId = getId(name, db);
 		CourseTable.deleteBySemesterId(semesterId, db);
 		db.getWritableDatabase().execSQL(
 				"DELETE FROM " + SemesterTable.NAME + " WHERE "
 						+ SemesterTable.COLUMN_NAME + "=\'" + name + "\'");
 
 	}
 
 	public static void deleteByYearId(String yearId, DatabaseHelper db) {
 		CourseTable.deleteByYearId(yearId, db); //FIX ME
 		db.getWritableDatabase().execSQL(
 				"DELETE FROM " + SemesterTable.NAME + " WHERE "
 						+ SemesterTable.COLUMN_YEAR_ID + "=" + yearId);
 
 	}
 
 }
