 package com.maximsblog.blogspot.com.maks2013;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
  
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
  
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteQueryBuilder;
 
 import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;
 
 public class OpenDBHelper extends SQLiteAssetHelper {
 
 	private static final String DATABASE_NAME = "catalog";
 	private static final int DATABASE_VERSION = 1;
 
 	public OpenDBHelper(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 
 		// you can use an alternate constructor to specify a database location 
 		// (such as a folder on the sd card)
 		// you must ensure that this folder is available and you have permission
 		// to write to it
 		//super(context, DATABASE_NAME, context.getExternalFilesDir(null).getAbsolutePath(), null, DATABASE_VERSION);
 
 	}
 
 	public Cursor getEmployees() {
 
 		SQLiteDatabase db = getReadableDatabase();
 		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
 
		String [] sqlSelect = {"name", "note"}; 
 		String sqlTables = "catalog";
 
 		qb.setTables(sqlTables);
 		Cursor c = qb.query(db, sqlSelect, null, null,
 				null, null, null);
 
 		c.moveToFirst();
 		return c;
 
 	}
 
 }
