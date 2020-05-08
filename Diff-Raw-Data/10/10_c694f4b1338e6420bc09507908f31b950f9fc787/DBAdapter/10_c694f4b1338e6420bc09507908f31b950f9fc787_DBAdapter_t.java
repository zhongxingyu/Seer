 package com.nikog.metropolia.schedule;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 
 public class DBAdapter {
 	private SQLiteDatabase database;
 	private final DBHelper dbHelper;
 	
 	private final int widgetId;
 	
 	public DBAdapter(Context ctx, int widgetId) {
 		this.widgetId = widgetId;
 		dbHelper = new DBHelper(ctx, widgetId);
 	}
 	
 	public void open() throws SQLException {
 		database = dbHelper.getWritableDatabase();
 	}
 	
 	public void close() {
 		dbHelper.close();
 	}
 	
 	public void createTable() {
 		Log.d(WidgetProvider.TAG, "Creating table " + DBHelper.TABLE_SCHEDULE + widgetId);
 		
 		String DATABASE_CREATE = "create table if not exists "
 				+ DBHelper.TABLE_SCHEDULE + widgetId +"("
 				+ DBHelper.COLUMN_ID + " integer primary key autoincrement, "
 				+ DBHelper.COLUMN_SUBJECT + " text, "
 				+ DBHelper.COLUMN_START + " integer, "
 				+ DBHelper.COLUMN_END + " integer, "
 				+ DBHelper.COLUMN_ROOMID + " text);";
 		database.execSQL(DATABASE_CREATE);
 	}
 	
 	public void deleteOld() {
		Log.d(WidgetProvider.TAG, "Deleting cache");
 		
		database.delete(DBHelper.TABLE_SCHEDULE + widgetId, null, null);
 	}
 	
 	public void push(String subject, long start, long end, String roomId) {
 		ContentValues values = new ContentValues();
 		values.put(DBHelper.COLUMN_SUBJECT, subject);
 		values.put(DBHelper.COLUMN_START, start);
 		values.put(DBHelper.COLUMN_END, end);
 		values.put(DBHelper.COLUMN_ROOMID, roomId);
 		
 		database.insert(DBHelper.TABLE_SCHEDULE + widgetId, null, values);
 	}
 	
 	public List<Event> getUpcoming(int limit) {
 		long time = System.currentTimeMillis();
 		
 		String query = "SELECT * FROM " + DBHelper.TABLE_SCHEDULE + widgetId
 				+ " WHERE " + DBHelper.COLUMN_END + " > "  + time
				+ " ORDER BY " + DBHelper.COLUMN_START + " ASC LIMIT " + limit;
 		Cursor cursor = database.rawQuery(query, null);
 		
 		List<Event> eventList = new ArrayList<Event>();
 		
 		cursor.moveToFirst();
 		
 		if(cursor.getCount() == 0) {
 			return eventList;
 		}
 		
 		do {
 			eventList.add(new Event(cursor.getString(1), cursor.getLong(2), cursor.getLong(3), cursor.getString(4)));
 		} while(cursor.moveToNext());
 				
 		return eventList;
 	}
 	
 	public Cursor getFirstRow() {
 		String query = "SELECT * FROM " + DBHelper.TABLE_SCHEDULE 
 				+ " ORDER BY " + DBHelper.COLUMN_ID + " ASC LIMIT 1";
 		Cursor cursor = database.rawQuery(query, null);
 		
 		return cursor;
 	}
 	
 	public long first() {
 		long time = System.currentTimeMillis();
 		
 		String query = "SELECT * FROM " + DBHelper.TABLE_SCHEDULE 
 				+ " WHERE " + DBHelper.COLUMN_END + " > "  + time
 				+ " ORDER BY " + DBHelper.COLUMN_END + " ASC LIMIT 1";
 		Cursor cursor = database.rawQuery(query, null);
 		
 		return cursor.getLong(1);
 	}
 	
 	public long last() {
 		String query = "SELECT * FROM " + DBHelper.TABLE_SCHEDULE 
 				+ " ORDER BY " + DBHelper.COLUMN_END + " DESC LIMIT 1";
 		Cursor cursor = database.rawQuery(query, null);
 		
 		return cursor.getLong(1);
 	}
 	
 	public void dropTable() {
 		database.execSQL("DROP TABLE IF EXISTS " + DBHelper.TABLE_SCHEDULE + widgetId);
 	}
 }
