 package com.random.randomproject;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteCursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class TKDatabase extends SQLiteOpenHelper {
 	private static final String DATABASE_NAME = "tk_db_timer_keeper";
 	private static final int DATABASE_VERSION = 2;
 	private static final String DICTIONARY_TABLE_NAME = "tk_tab";
 	private static final String DICTIONARY_TABLE_CREATE =
 		"CREATE TABLE " + DICTIONARY_TABLE_NAME + " (" +
 		"_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
 		"date VARCHAR(25), " +
 		"time VARCHAR(25), " +
 		"act_name VARCHAR(25), " +
 		"act_type VARCHAR(25));";
 	//    private static final String TYPES_TABLE_CREATE = "CREATE TABLE tk_types " + 
 	//    			"(types VARCHAR(25));";
 
 	public static final String TK_TYPES = "types";
 	private static final String TYPES_TABLE_CREATE = 
 		"CREATE TABLE tk_types (" + 
 		"_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
 		TK_TYPES + " TEXT NOT NULL);";
 
 	private static final String DUR_TABLE = "tk_duration";
 	private static final String DURATION_TABLE_CREATE = 
 		"CREATE TABLE " + DUR_TABLE + "(" +
 		"_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
 		"date TEXT NOT NULL, " +
 		"time TEXT NOT NULL," +
 		"duration TEXT NOT NULL, " +
 		"type TEXT NOT NULL);";
 
 	TKDatabase(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		try {
 			db.execSQL(DICTIONARY_TABLE_CREATE);
 			db.execSQL(TYPES_TABLE_CREATE);
 			db.execSQL(DURATION_TABLE_CREATE);
 		}
 		catch (SQLException e) {
 			Log.e("create", e.toString());
 		}
 	}
 
 	public void onUpgrade(SQLiteDatabase db, int o, int n) {
 
 	}
 
 	public long insertType(String newType) {
 		ContentValues cv = new ContentValues();
 		SQLiteDatabase db = getWritableDatabase();
 		cv.put("types", newType);
 		long ret = db.insert("tk_types", " ", cv);
 
 		db.close();
 
 		return ret;
 	}
 
 	public long insertActivity(String activityName, String activityType) {
 		ContentValues cv = new ContentValues();
 		SQLiteDatabase db = getWritableDatabase();
 
 		Date date = new Date();
 		Calendar nowCalendar = new GregorianCalendar();
 		nowCalendar.setTime(date);
 		String dateString = nowCalendar.get(Calendar.YEAR) + "-" +
 		nowCalendar.get(Calendar.MONTH) + "-" +
 		nowCalendar.get(Calendar.DAY_OF_MONTH);
 
 		Log.i("statement", DICTIONARY_TABLE_CREATE);
 
 		String timeString = nowCalendar.get(Calendar.HOUR_OF_DAY) + ":" +
 		nowCalendar.get(Calendar.MINUTE) + ":" +
 		nowCalendar.get(Calendar.SECOND);
 		Log.i("insert", 
 				dateString + " " +
 				timeString + " " +
 				activityName + " " +
 				activityType);
 
 		cv.put("date", dateString);
 		cv.put("time", timeString);
 		cv.put("act_name", activityName);
 		cv.put("act_type", activityType);
 
 		long ret = -1;
 
 		try {
 			ret = db.insertOrThrow("tk_tab", " ", cv);
 			db.close();
 		}
 		catch (SQLException e) {
 			db.close();
 			Log.e("insertActivity:insert", e.toString());
 		}
 
 		//    	db.close();
 
 		return ret;
 	}
 
 	public ArrayList<String> getActivityTypes() {
 		ArrayList<String> aTypes = new ArrayList<String>();
 		SQLiteDatabase db = getWritableDatabase();
 		SQLiteCursor cur;
 		String value;
 		String qString = "SELECT * FROM tk_types";
 
 		try {
 			cur = (SQLiteCursor) db.rawQuery(qString, null);
 
 			Integer index = new Integer(cur.getColumnIndex(TK_TYPES));
 
 			cur.moveToFirst();
 
 			do {
 				value = cur.getString(index);
 				aTypes.add(value);
 				//        		Log.i("ActivityType", value);
 				cur.moveToNext();
 			} while(cur.isAfterLast() == false);
 
 			cur.close();
 		}
 		catch (SQLException e) {
 			Log.e("Exception on rawQuery()", e.toString());
 		}
 
 //		db.close();
 
 		return aTypes;
 	}
 
 	public int deleteActivityType(String str) {
 		int n = 0;
 		try {
 			SQLiteDatabase db = getWritableDatabase();
 			ArrayList<String> l = getActivityTypes();
 
 			if(l.size() < 2) {
 				Log.w("delete", "Cannot delete last element");
 				return 0;
 			}
 
 			try {
 				n =  db.delete("tk_types", "types=?", new String[] {str});
 			}
 			catch (SQLException e) {
 				Log.e("deleteType", e.toString());
 			}
 			catch (IllegalStateException e) {
 				Log.e("deleteActivityType", e.toString());
 			}
 			db.close();
 		}
 		catch (Exception e) {
 			Log.e("getDB", e.toString());
 		}
 
 		return n;
 	}
 
 	public String logLastActivity() {
 		SQLiteDatabase db = getWritableDatabase();
 		String qString = "SELECT * FROM " + DICTIONARY_TABLE_NAME + ";";
 		SQLiteCursor cur;
 		String lastActivityString = new String();
 		String dateString   = new String();
 		String timeString   = new String();
 		String activityType = new String();
 		ContentValues cv = new ContentValues();
 
 		try {
 			cur = (SQLiteCursor) db.rawQuery(qString, null);		
 			cur.moveToLast();
 
 			int dateIndex = cur.getColumnIndex("date");
 			int timeIndex = cur.getColumnIndex("time");
 			//    		int actIndex  = cur.getColumnIndex("act_name");
 			int typeIndex = cur.getColumnIndex("act_type");
 			dateString = cur.getString(dateIndex);
 			timeString = cur.getString(timeIndex);
 			activityType = cur.getString(typeIndex);
 			//    		activityAttributeString  = cur.getString(actIndex) + ";";
 			//    		Log.i("LastActivity", lastActivityString);
 
 			cur.close();
 		}
 		catch (SQLException e) {
 			Log.e("Exception on rawQuery()", e.toString());
 		}
 
 		String [] dateBits = dateString.split("-");
 		String [] timeBits = timeString.split(":");
 
 		Log.i("dateTime", 
 				dateBits[0] + dateBits[1] + dateBits[2] +
 				timeBits[0] + timeBits[1]);
 		try {
 			GregorianCalendar oldDate = new GregorianCalendar (
 					Integer.parseInt(dateBits[0]),
 					Integer.parseInt(dateBits[1]),
 					Integer.parseInt(dateBits[2]),
 					Integer.parseInt(timeBits[0]),
 					Integer.parseInt(timeBits[1]));
 			GregorianCalendar newDate = new GregorianCalendar();
 
 			Long deltaTimeInMillis = 
 				newDate.getTimeInMillis() -
 				oldDate.getTimeInMillis();
 
			lastActivityString = 
				dateString + timeString + activityType;
 
 			cv.put("date", dateString);
 			cv.put("time", timeString);
 			cv.put("duration", deltaTimeInMillis.toString());
 			cv.put("type", activityType);
 
 			db.insertOrThrow(DUR_TABLE, " ", cv);
			lastActivityString = deltaTimeInMillis.toString() + "ms.";

 		}
 		catch (Exception e) {
 			Log.e("lastActivity", e.toString());
 		}
 
 		db.close();
 
 		return lastActivityString;
 	}
 
 	public ArrayList<String[]> getActivitiesForToday() {
 		ArrayList<String[]> timeTypePairList = new ArrayList<String[]>();
 		SQLiteDatabase db = getWritableDatabase();
 
 		GregorianCalendar today = new GregorianCalendar();
 		String todayString = 
 			today.get(Calendar.YEAR) + "-" + 
 			today.get(Calendar.MONTH) + "-" +
 			today.get(Calendar.DAY_OF_MONTH);
 
 		String qString = 
 			"SELECT duration,type FROM " +
 			DUR_TABLE + " WHERE date='" + todayString + "'";
 
 		Log.i("Query", qString);
 
 		try {
 			SQLiteCursor cursor = (SQLiteCursor) db.rawQuery(qString, null);
 
 			if(cursor.getCount() <= 0) 
 				return timeTypePairList;
 
 			Log.i("nRows", "n: " + cursor.getCount());
 
 			int durationIndex = cursor.getColumnIndex("duration");
 			int typeIndex     = cursor.getColumnIndex("type");
 
 			cursor.moveToFirst();
 			Long totalMilliSecs = new Long(0);
 
 			do {
 				String [] timeTypePair = new String[2];
 				timeTypePair[0] = cursor.getString(durationIndex);
 				timeTypePair[1] = cursor.getString(typeIndex);
 				timeTypePairList.add(timeTypePair);
 				totalMilliSecs += Long.parseLong(timeTypePair[0]);
 				Log.i(timeTypePair[1], timeTypePair[0]);
 				cursor.moveToNext();
 			} while(cursor.isAfterLast() == false);
 
 			String [] totalTime = new String[] {
 					totalMilliSecs.toString(), "---TOTAL---"};
 			timeTypePairList.add(totalTime);
 
 			cursor.close();
 		}
 		catch (Exception e) {
 			Log.e("rawQuery", e.toString());
 		}
 
 		db.close();
 
 		return timeTypePairList;
 	}
 }
 
