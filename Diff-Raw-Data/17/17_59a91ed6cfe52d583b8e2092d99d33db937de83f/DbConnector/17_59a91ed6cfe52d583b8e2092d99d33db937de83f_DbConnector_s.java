 package com.github.pozo.volan.db;
 
 import java.util.ArrayList;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class DbConnector {
 	private static final String ID = "id";
 	private static final String DIRECTION = "direction";
 	private static final String STOP_FROM_ID = "stop_from_id";
 	private static final String STOP_FROM_NAME = "stop_from_name";
 	private static final String STOP_TO_ID = "stop_to_id";
 	private static final String STOP_TO_NAME = "stop_to_name";
 	private static final String CITY_NAME = "city_name";
 	private static final String LINE_NAME = "line_name";
 
 	private static DbConnector intance = null;
 	
 	private final SQLiteOpenHelper databaseHelper;
 	private final SQLiteDatabase database;
 
 	private DbConnector(Context context) {
 		databaseHelper = new DatabaseHelper(context);
 		database = databaseHelper.getWritableDatabase();
 	}
 	public static synchronized DbConnector getInstance(Context context) {
 		if(intance == null || intance.isClosed()) {
 			intance = new DbConnector(context);
 		}
 		return intance;
 	}
 	public static void close() {
 		if(intance != null) {
 			intance.closeResources();
 		}		
 	}
 	private class DatabaseHelper extends SQLiteOpenHelper {
 		public static final  String DB_NAME = "favorites";
 		private static final String TABLE_NAME = "favorites";
 		
 		private static final String CREATE_SCHEMA = 
 			"CREATE TABLE ["+TABLE_NAME+"] (" +
 				"[" + ID + "] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT" +
 				",[" + LINE_NAME + "] VARCHAR (8) NULL" +
 				",[" + CITY_NAME + "] VARCHAR (8) NULL" +
 				",[" + STOP_FROM_ID + "] VARCHAR (8) NULL" +
 				",[" + STOP_FROM_NAME + "] VARCHAR (32) NULL" +
 				",[" + STOP_TO_ID + "] VARCHAR (8) NULL" +
 				",[" + STOP_TO_NAME + "] VARCHAR (32) NULL" +
 				",[" + DIRECTION + "] VARCHAR (8) NULL)";
 		
 		public DatabaseHelper(Context context) {
 			super(context, DB_NAME, null, 1);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase dataBase) {		
 			dataBase.execSQL(CREATE_SCHEMA);
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME	+ ";");
 			onCreate(db);
 		}
 	}
 	
	public long insertRow(BusLine favorite) {
 		final ContentValues values = new ContentValues();
 
 		values.put(LINE_NAME,favorite.getLineName());
 		values.put(CITY_NAME,favorite.getCityName());
 		values.put(DIRECTION,favorite.getDirection());
 		values.put(STOP_FROM_ID,favorite.getFromId());
 		values.put(STOP_FROM_NAME, favorite.getFromName());
 		values.put(STOP_TO_ID, favorite.getToId());
 		values.put(STOP_TO_NAME, favorite.getToName());
 		
 		return database.insert(DatabaseHelper.TABLE_NAME, null, values);
 	}
	public boolean isFavoriteExists(BusLine favorite) {
 		boolean retval = false;
 		final String constraint = LINE_NAME+" = \"" + favorite.getLineName()+"\" "+
 				"AND " + CITY_NAME + " = \"" + favorite.getCityName()+"\" "+
 				"AND " + STOP_FROM_ID + " = \""+favorite.getFromId()+"\" "+
 				"AND " + STOP_TO_ID + " = \""+favorite.getToId()+"\" "+
 				"AND " + DIRECTION + " = \"" + favorite.getDirection()+"\"";
 
 		final Cursor cursor = database.query(
 				DatabaseHelper.TABLE_NAME,
 				new String[]{ID},
 				constraint,
 				null, null, null, null);
 		retval = cursor.moveToFirst();
 		cursor.close();		
 		return retval;
 	}
	public ArrayList<BusLine> getAllFavorite() {
 		ArrayList<BusLine> favorites = new ArrayList<BusLine>();
 		final Cursor cursor = database.query(
 				DatabaseHelper.TABLE_NAME, 
 				new String[]{ID,LINE_NAME,CITY_NAME,STOP_FROM_ID,DIRECTION,STOP_FROM_NAME,STOP_TO_NAME,STOP_TO_ID}, 
 				null, null, null, null, null);
 		
 		if(cursor.moveToFirst()) {
 			do {
 				BusLine favorite = new BusLine();
 				favorite.setId(cursor.getLong(0));
 				favorite.setLineName(cursor.getString(1));
 				favorite.setCityName(cursor.getString(2));
 				favorite.setFromId(cursor.getString(3));
 				favorite.setDirection(cursor.getString(4));
 				favorite.setFromName(cursor.getString(5));
 				favorite.setToName(cursor.getString(6));
 				favorite.setToId(cursor.getString(7));
 				
 				favorites.add(favorite);
 			} while (cursor.moveToNext());				
 		}
 		cursor.close();
 		return favorites;
 	}
	public int deleteFavorite(long id) {
 		final String constraint = ID + " = " + id;
 
 		return database.delete(
 				DatabaseHelper.TABLE_NAME,
 				constraint, null);
 	}
	public long getFavoriteId(BusLine favorite) {
 		long retval = -1L;
 		final String constraint = LINE_NAME + " = \"" + favorite.getLineName()+"\" "+
 				"AND " + CITY_NAME + " = \"" + favorite.getCityName()+"\" "+
 				"AND " + STOP_FROM_ID + " = \""+favorite.getFromId()+"\" "+
 				"AND " + STOP_TO_ID + " = \""+favorite.getToId()+"\" "+
 				"AND " + DIRECTION + " = \"" + favorite.getDirection()+"\"";
 
 		final Cursor cursor = database.query(
 				DatabaseHelper.TABLE_NAME,
 				new String[]{ID},
 				constraint,
 				null, null, null, null);
 		if(cursor.moveToFirst()) {
 			retval = cursor.getLong(0);
 		}
 		cursor.close();
 		return retval;
 	}
	public boolean isClosed() {
 		return !database.isOpen();
 	}
 	private synchronized void closeResources() {
 		if(databaseHelper !=null){
 			databaseHelper.close();	
 		}
 		if(database != null) {
 			database.close();			
 		}		
 	}
 
 }
