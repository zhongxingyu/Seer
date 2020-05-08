 package com.marbol.marbol;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class MarbolSQLHelper extends SQLiteOpenHelper{
 	public static final String TABLE_ADVENTURE = "adventures";
 	
 	public static final String COLUMN_ID ="_id";
 	public static final String ADVENTURE_NAME = "adventure_name";
 	public static final String ADVENTURE_DISTANCE = "adventure_dist";
 	public static final String ADVENTURE_AREA = "adventure_area";
 	public static final String ADVENTURE_DATE = "adventure_date";
 	public static final String ADVENTURE_TIME = "adventure_time";
 	public static final String ADVENTURE_GPS_POINTS = "adventure_gps_points";
 	
 	private static final String DATABASE_NAME = "Marbol.db";
	private static final int DATABASE_VERSION = 1;
 
 	private static final String CREATE_DB = "create table "
 			+ TABLE_ADVENTURE + "(" + COLUMN_ID +" integer primary key autoincrement, "
 			+ ADVENTURE_NAME + " text not null,"
 			+ ADVENTURE_DISTANCE + " real not null, "
 			+ ADVENTURE_AREA + " real not null, "
 			+ ADVENTURE_DATE + " date not null, "
 			+ ADVENTURE_TIME + " integer not null, "
 			+ ADVENTURE_GPS_POINTS + " text not null"
 			+ ");";
 	
 	public MarbolSQLHelper(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase database) {
 		database.execSQL(CREATE_DB);
 		
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		Log.w(MarbolSQLHelper.class.getName(),
 		        "Upgrading database from version " + oldVersion + " to "
 		            + newVersion + ", which will destroy all old data");
 		
 		// drop the old db and re create.
 		// TODO support graceful updating later. 
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ADVENTURE);
 		onCreate(db);
 	}
 
 }
