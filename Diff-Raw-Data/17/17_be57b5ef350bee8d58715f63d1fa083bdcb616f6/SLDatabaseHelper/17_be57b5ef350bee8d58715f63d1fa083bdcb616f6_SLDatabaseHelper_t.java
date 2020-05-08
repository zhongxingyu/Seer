 package com.aethermade.shoplist;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteDatabase.CursorFactory;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class SLDatabaseHelper extends SQLiteOpenHelper {
 
 	static final String file = "shoplist.db";
 	static final int version = 0;
 	
 	public SLDatabaseHelper(Context context, String name, CursorFactory factory, int version) {
 		super(context, file, null, version);
 		
 	}
 
 	@Override
	public void onCreate(SQLiteDatabase db) {
 		
 
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
 		
 
 	}
 
 }
