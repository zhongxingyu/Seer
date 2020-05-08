 package com.wikispaces.es1011.gamemenu;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class Drink_OriginalList {
 
 	private class dbHelper extends SQLiteOpenHelper {
 		public dbHelper(Context c) {
 			super(c, "gamemenu", null, 1);
 		}
 
 		private final String sLoremIpsum = "Lorem ipsum dolor sit amet, "
 				+ "consectetur adipiscing elit. Aliquam adipiscing lorem eu magna "
 				+ "vestibulum sollicitudin. Sed quam tellus, cursus eget lobortis "
 				+ "volutpat, placerat vitae orci. Nunc vitae lacus magna. Praesent "
 				+ "eu semper sem. Etiam id eros lorem, vel adipiscing justo. Nunc "
 				+ "aliquam tortor porta velit tempus at fringilla sapien accumsan. "
 				+ "Vestibulum consectetur quam sit amet nisl tincidunt rhoncus "
 				+ "rhoncus dolor commodo. Nunc sagittis tempus ligula, quis laoreet "
 				+ "arcu suscipit vel. Etiam vitae orci eros, a tincidunt massa. "
 				+ "Fusce sit amet mi quam, at posuere dui. Cras vel augue turpis, "
 				+ "vel rhoncus dolor. Class aptent taciti sociosqu ad litora "
 				+ "torquent per conubia nostra, per inceptos himenaeos.";
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			db
 					.execSQL("create table drink_list (_id integer primary key, d_name text, d_price text, d_description text);");
 			// Inserire il listino di default
 			ContentValues cvVal = new ContentValues();
 			cvVal.put("d_price", "5.00");
 			cvVal.put("d_description", sLoremIpsum);
 			for (int i = 1; i < 13; i++) {
 				cvVal.put("_id", i);
 				cvVal.put("d_name", "Drink " + i);
 				db.insert("drink_list", null, cvVal);
 			}
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			db.execSQL("drop table if exists drink_list");
 			onCreate(db);
 		}
 	}
 
 	private Context mCtx;
 	private SQLiteDatabase db;
 	private dbHelper dbh;
 
 	public Drink_OriginalList(Context context) {
 		mCtx = context;
 	}
 
 	public Drink_OriginalList dbOpen() {
 		dbh = new dbHelper(mCtx);
 		db = dbh.getWritableDatabase();
 		return this;
 	}
 
 	public void dbClose() {
 		dbh.close();
 	}
 
 	public Cursor getDrinkList() {
 		return db.query("drink_list",
 				new String[] { "_id", "d_name", "d_price" }, null, null, null,
 				null, null);
 	}
 	
 	public int getListCount() {
 		Cursor mC = db.query("drink_list",
 				new String[] { "count(d_name) as d_tot" }, null, null, null, null,
 				null);
 
 		if (mC != null) {
 			mC.moveToFirst();
 			int i = mC.getInt(0);
 			mC.close();
 			return i;
 		} else
 			return 0;
 	}
 }
