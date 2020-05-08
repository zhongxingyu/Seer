 /*
  * Waker project 2013
  * 
  * folks studio
  * 
  * by caifangmao8@gmail.com
  */
 package com.cfm.waker.dao;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import com.cfm.waker.entity.Alarm;
 import com.cfm.waker.log.WLog;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class WakerDatabaseHelper extends SQLiteOpenHelper {
 	
 	private static final String TAG = "WakerDatabaseHelper";
 	
 	private static WakerDatabaseHelper instance;
 	
 	//private Context context;
 	
 	private static final String    DATABASE_NAME = "waker.db";
 	private static final String       TABLE_NAME = "alarm";
 	private static final int    DATABASE_VERSION = 3;
 	
 	public WakerDatabaseHelper(Context context){
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		//this.context = context;
 	}
 	
 	public static WakerDatabaseHelper getInstance(Context context){
 		if(null == instance){
 			instance = new WakerDatabaseHelper(context);
 		}
 		
 		return instance;
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db){
 		db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
 	                Alarm.Columns.ID + " INTEGER PRIMARY KEY," + 
 				    Alarm.Columns.HOUR + " INTEGER, " +
 	                Alarm.Columns.MINUTE + " INTEGER, " +
 	                Alarm.Columns.SNOOZE_TIME + " INTEGER, " +
 	                Alarm.Columns.ENABLED + " INTEGER, " +
 	                Alarm.Columns.VIBRATE + " INTEGER, " +
 	                Alarm.Columns.RINGTONE + " TEXT, " +
 	                Alarm.Columns.DAYS_OF_WEEK + " INTEGER, " +
 	                Alarm.Columns.MESSAGE + " TEXT);");
 	}
 	
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int currentVersion){
 		WLog.print(TAG, "Upgrade waker database from" + oldVersion + "to" + currentVersion);
 		
 		deleteAllAlarms(db);
 	}
 	
 	public void deleteAllAlarms(SQLiteDatabase db){
 		if(null == db) db = getWritableDatabase();
 		
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
 		onCreate(db);
 	}
 	
 	public Alarm getAlarm(long id, boolean is24Format){
 		SQLiteDatabase db = getReadableDatabase();
 		String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + Alarm.Columns.ID + "=?";
 		String[] selectionArgs = { id + "" };
 		Cursor cursor = db.rawQuery(sql, selectionArgs);
 		
 		Alarm alarm = null;
 		if(cursor.moveToFirst()){
 			alarm = getAlarmByCursor(cursor, is24Format);
 		}
 		
 		cursor.close();
 		return alarm;
 	}
 	
 	public List<Alarm> getAlarms(boolean is24Format){
 		SQLiteDatabase db = getReadableDatabase();
 		String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + Alarm.Columns.ID + " ASC";
 		Cursor cursor = db.rawQuery(sql, null);
 		
 		List<Alarm> alarms = null;
 		
 		if(cursor.moveToFirst()){
 			alarms = new ArrayList<Alarm>();
 			
 			do{
 				alarms.add(getAlarmByCursor(cursor, is24Format));
 			}while(cursor.moveToNext());
 			
 		}
 		
 		cursor.close();
 		return alarms;
 	}
 	
 	private Alarm getAlarmByCursor(Cursor cursor, boolean is24Format){
 		Calendar calendar = Calendar.getInstance();
 		calendar.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(Alarm.Columns.ID)));
 		Alarm alarm = new Alarm(calendar, is24Format);
 		
 		alarm.setHour(cursor.getInt(cursor.getColumnIndex(Alarm.Columns.HOUR)));
 		alarm.setMinute(cursor.getInt(cursor.getColumnIndex(Alarm.Columns.MINUTE)));
 		alarm.setSnoozeTime(cursor.getInt(cursor.getColumnIndex(Alarm.Columns.SNOOZE_TIME)));
 		alarm.setEnabled(cursor.getInt(cursor.getColumnIndex(Alarm.Columns.ENABLED)) == 1 ? true : false);
 		alarm.setVibrate(cursor.getInt(cursor.getColumnIndex(Alarm.Columns.VIBRATE)) == 1 ? true : false);
 		alarm.setRingtone(cursor.getString(cursor.getColumnIndex(Alarm.Columns.RINGTONE)));
 		alarm.setWeek(cursor.getInt(cursor.getColumnIndex(Alarm.Columns.DAYS_OF_WEEK)));
 		alarm.setMessage(cursor.getString(cursor.getColumnIndex(Alarm.Columns.MESSAGE)));
 		
 		return alarm;
 	}
 	
 	public void insertAlarm(Alarm alarm){
 		ContentValues values = createContentValues(alarm);
 		SQLiteDatabase db = getWritableDatabase();
 		
 		int hour = (Integer) values.get(Alarm.Columns.HOUR);
 		int minute = (Integer) values.get(Alarm.Columns.MINUTE);
 		
 		String sql = "SELECT " + Alarm.Columns.ID + " FROM " + TABLE_NAME + " WHERE " + Alarm.Columns.HOUR + "=? AND " + Alarm.Columns.MINUTE + "=?";
 		String[] selectionArgs = {hour + "", minute + ""};
 		
 		final Cursor cursor = db.rawQuery(sql, selectionArgs);
 		if(cursor.moveToFirst()){
 			updateAlarm(cursor.getLong(0), alarm);
 		}else{
 			db.insert(TABLE_NAME, null, values);
 		}
 		
 		cursor.close();
 	}
 	
 	public long updateAlarm(long id, Alarm alarm){
 		ContentValues values = createContentValues(alarm);
 		SQLiteDatabase db = getWritableDatabase();
 		
 		String whereClause = Alarm.Columns.ID + "=?";
 		String[] whereArgs = {Long.toString(id)};
 		
 		return db.update(TABLE_NAME, values, whereClause, whereArgs);
 	}
 	
 	public void deleteAlarm(long id){
 		SQLiteDatabase db = getWritableDatabase();
 		
 		String sql = "DELETE FROM " + TABLE_NAME + " WHERE " + Alarm.Columns.ID + "=?";
 		String[] bindArgs = {Long.toString(id)};
 		
 		db.execSQL(sql, bindArgs);
 	}
 	
 	public ContentValues createContentValues(Alarm alarm){
 		ContentValues cv = new ContentValues();
 		cv.put(Alarm.Columns.ID, alarm.getCalendar().getTimeInMillis());
 		cv.put(Alarm.Columns.HOUR, alarm.getHour());
 		cv.put(Alarm.Columns.MINUTE, alarm.getMinute());
 		cv.put(Alarm.Columns.SNOOZE_TIME, alarm.getSnoozeTime());
 		cv.put(Alarm.Columns.ENABLED, alarm.isEnabled());
 		cv.put(Alarm.Columns.VIBRATE, alarm.isVibrate());
 		cv.put(Alarm.Columns.RINGTONE, alarm.getRingtone());
 		cv.put(Alarm.Columns.DAYS_OF_WEEK, alarm.getWeek());
 		cv.put(Alarm.Columns.MESSAGE, alarm.getMessage());
 		
 		WLog.print(TAG, cv.toString());
 		
 		return cv;
 		
 	}
 	
 }
