 package org.katta.android.gd.db;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class SQLRepository extends SQLiteOpenHelper {
 
 	public SQLRepository(Context context) {
 		super(context, DBConstants.DATABASE_NAME, null, DBConstants.VERSION);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
		db.execSQL(String.format("CREATE TABLE %s (%s TEXT, %s TEXT, %s TEXT)",
 				DBConstants.TBL_TRIP, DBConstants.COL_TRIP_NAME,
 				DBConstants.COL_TRIP_DESCRIPTION,
 				DBConstants.COL_TRIP_STARTDATE));
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 	}
 
 }
