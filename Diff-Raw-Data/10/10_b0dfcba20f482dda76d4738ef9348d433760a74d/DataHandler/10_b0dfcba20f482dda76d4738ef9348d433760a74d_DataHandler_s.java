 package com.teamdev.database;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class DataHandler extends SQLiteOpenHelper {
 	
 	private final static String DATABASE_NAME = "tasks.db";
 	private final static int DATABASE_VERSION = 1;
 	
 	private final static String DATABASE_CREATE = "create table tasks" + 
												  "(_id INTEGER primary key autoincrement," +
												  "title TEXT not null" +
												  "description TEXT" +
												  "creationDate TEXT not null" +
												  "lastChangedDate TEXT" + 
 												  "state INTEGER not null);";	
 	
 	private final static String DATABASE_UPGRADE = "DROP TABLE IF EXISTS tasks";
 	
 	public DataHandler(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		db.execSQL(DATABASE_CREATE);
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		db.execSQL(DATABASE_UPGRADE);
 		onCreate(db);
 	}
 	
 }
