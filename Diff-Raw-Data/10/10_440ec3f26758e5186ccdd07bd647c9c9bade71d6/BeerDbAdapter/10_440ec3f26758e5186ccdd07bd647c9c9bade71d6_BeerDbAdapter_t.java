 package com.example.lagerlogger;
 
 
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class BeerDbAdapter {
 
public static final String TABLE_BEER = "Beer_Table";
 	//Database Columns
 	public static final String COLUMN_ID = "_id";
 	public static final String COLUMN_NAME = "name";
 	public static final String COLUMN_BREWERY = "brewery";
 	public static final String COLUMN_COLOR = "color";
 	public static final String COLUMN_TYPE = "type";
 	public static final String COLUMN_NOTE = "note";
 	public static final String COLUMN_ABV = "abv";
 	public static final String COLUMN_OG = "og";
 	public static final String COLUMN_FG = "fg";
 	//Database SQLite create statement
 	private static final String DATABASE_CREATE = "create table " + TABLE_BEER
 			+ "(" + COLUMN_ID + " integer primary key autoincrement, "
 			+ COLUMN_NAME + " text not null, " + COLUMN_BREWERY
 			+ " text not null, " + COLUMN_COLOR + " text not null, "
 			+ COLUMN_TYPE + " text not null, " + COLUMN_NOTE
 			+ " text not null, " + COLUMN_ABV + " real not null, " + COLUMN_OG
 			+ " real not null, " + COLUMN_FG + " real not null);";
 	//String array of all columns for convenience
 	private static final String ALL_COLUMNS[] = { COLUMN_ID, COLUMN_NAME,
 			COLUMN_BREWERY, COLUMN_COLOR, COLUMN_TYPE, COLUMN_NOTE, COLUMN_ABV,
 			COLUMN_OG, COLUMN_FG };
 	//Debug Tag
 	private static final String TAG = "BeerDbAdapter";
 	//Database Info
 	private static final String DATABASE_NAME = "beer.db";
 	private static final int DATABASE_VERSION = 1;
 
 	private SQLiteDatabase mBeerDb;
 	private BeerDbHelper mBeerDbHelper;
 	private final Context mCtx;
 /**********************************************************
 	 Private Class Definitions
 ***********************************************************/
 	
 	private static class BeerDbHelper extends SQLiteOpenHelper {
 
 		BeerDbHelper(Context context) {
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			db.execSQL(DATABASE_CREATE);
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
 					+ newVersion + ", which will destroy all old data");
 			db.execSQL("DROP TABLE IF EXISTS notes");
 			onCreate(db);
 		}
 	}
 /**********************************************************
 	Methods
  **********************************************************/
 	
 	public BeerDbAdapter(Context ctx) {
 		this.mCtx = ctx;
 	}
 
 	public BeerDbAdapter open() throws SQLException {
 		mBeerDbHelper = new BeerDbHelper(mCtx);
 		mBeerDb = mBeerDbHelper.getWritableDatabase();
 		return this;
 	}
 
 	public void close() {
 		mBeerDb.close();
 	}
 	
 	public void addBeer(Beer beer){
 		ContentValues cv = new ContentValues(beer.writeCV());
 		long insertID = mBeerDb.insert(TABLE_BEER, null, cv);
 		beer.setID(insertID);
 	}
 	
 	public Cursor findBeerByID(long ID) {
 		Cursor csr = mBeerDb.query(TABLE_BEER, ALL_COLUMNS, COLUMN_ID + " = "
 				+ ID, null, null, null, null);
 		if(csr != null){
 			csr.moveToFirst();
 		}
 		return csr;
 	}
 	
 	public Cursor getBeerList() {
 		return mBeerDb.query(TABLE_BEER, new String[] {COLUMN_ID, COLUMN_NAME}, 
 				null, null, null, null, null);
 	}
 	
 	public boolean updateBeer(Beer beer) {
 		ContentValues cv = beer.writeCV();
 		return mBeerDb.update(TABLE_BEER, cv, COLUMN_ID + "=" + beer.getID(),null) > 0;
 	}
 	
 }
