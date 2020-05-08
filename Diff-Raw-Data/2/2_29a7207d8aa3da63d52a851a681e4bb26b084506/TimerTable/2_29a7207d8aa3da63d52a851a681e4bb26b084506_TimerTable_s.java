 package com.cooltofu.db;
 
 import android.database.sqlite.SQLiteDatabase;
 
 public class TimerTable {
 	
	private static final String DATABASE_CREATE = "create table timer if not exist "
 								+ "(_id integer primary key autoincrement, "
 								+ "label text,"
 								+ "seconds integer not null,"
 								+ "timestamp text default \"0\","
 								+ "is_on integer default 0,"
 								+ "note text default \"\")";
 	
 	public static void onCreate(SQLiteDatabase db) {
 		db.execSQL(DATABASE_CREATE);
 	}
 	
 	
 	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		db.execSQL("drop table if exists timer");
 		onCreate(db);
 	}
 	
 }
