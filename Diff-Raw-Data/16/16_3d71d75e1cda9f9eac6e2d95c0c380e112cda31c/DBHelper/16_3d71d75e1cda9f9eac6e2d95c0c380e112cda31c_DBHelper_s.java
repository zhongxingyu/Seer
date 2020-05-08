 package com.austindiviness.drunkfriend;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class DBHelper extends SQLiteOpenHelper {
 	private static final String DB_NAME = "contacts_info.db";
 	
 	
 	public DBHelper(Context context) {
		super(context, DB_NAME, null, 1); // dunno
 	}
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
