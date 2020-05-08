 package com.example.tomatroid.sql;
 
 import java.util.ArrayList;
 
 import org.joda.time.DateTime;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class SQHelper extends SQLiteOpenHelper {
 
 	private static final String DATABASE_NAME = "pomodorodroid";
 	private static final int DATABASE_VERSION = 1;
 
 	// TABLE Dates
 	public static final String TABLE_DATES = "dates";
 	public static final String KEY_ROWID = "_id";
 	public static final String KEY_DATE_DAY = "day";
 	public static final String KEY_DATE_MONTH = "month";
 	public static final String KEY_DATE_YEAR = "year";
 	public static final String KEY_DATE_WEEKDAY = "weekday";
 	public static final String KEY_DATE_WEEKNUM = "weeknum";
 	public static final String KEY_DATE_START_HOUR = "starttime_hour";
 	public static final String KEY_DATE_START_MINUTE = "starttime_minute";
 	public static final String KEY_DATE_END_HOUR = "endtime_hour";
 	public static final String KEY_DATE_END_MINUTE = "endtime_minute";
 	public static final String KEY_DURATION = "duration";
 	public static final String KEY_STARTTIMEMILLIES = "starttimemillies";
 	public static final String KEY_ENDTIMEMILLIES = "endtimemillies";
 	public static final String KEY_TYPE = "type";
 	public static final String KEY_THEME = "theme";
 
 	static final String CREATE_DATES_TABLE = "create table " + TABLE_DATES
 			+ "(" + KEY_ROWID + " integer primary key autoincrement, "
 			+ KEY_DATE_DAY + " integer not null, " + KEY_DATE_MONTH
 			+ " integer not null," + KEY_DATE_YEAR + " integer not null,"
 			+ KEY_DATE_WEEKDAY + " integer not null," + KEY_DATE_WEEKNUM
 			+ " integer not null," + KEY_DATE_START_HOUR + " integer not null,"
 			+ KEY_DATE_START_MINUTE + " integer not null," + KEY_DATE_END_HOUR
 			+ " integer not null," + KEY_DATE_END_MINUTE + " integer not null,"
 			+ KEY_DURATION + " integer not null," + KEY_TYPE
 			+ " integer not null," + KEY_THEME + " integer,"
 			+ KEY_STARTTIMEMILLIES + " text not null," + KEY_ENDTIMEMILLIES
 			+ " text not null);";
 
 	// TABLE Theme
 	public static final String TABLE_THEME = "themes";
 	// ROW ID
 	public static final String KEY_NAME = "name";
 	public static final String KEY_ITEMOF = "itemof";
 	public static final String KEY_OVERALLTIME = "overalltime";
 	public static final String KEY_HIDE = "hide";
 
 	static final String CREATE_THEME_TABLE = "create table " + TABLE_THEME
 			+ "(" + KEY_ROWID + " integer primary key autoincrement, "
 			+ KEY_ITEMOF + " integer," + KEY_NAME + " text not null, "
 			+ KEY_OVERALLTIME + " integer not null, "
 			+ KEY_HIDE + " integer not null "
 			+ ");";
 
 	public static final int TYPE_POMODORO = 0;
 	public static final int TYPE_SHORTBREAK = 1;
 	public static final int TYPE_LONGBREAK = 2;
 	public static final int TYPE_TRACKING = 3;
 	public static final int TYPE_SLEEPING = 4;
 
 	SQLiteDatabase db;
 
 	public SQHelper(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase database) {
 		Log.e("SQHepler", "onCreate");
 		createTables(database);
 		db = database;
 	}
 
 	public void createTables(SQLiteDatabase database) {
 		database.execSQL(CREATE_DATES_TABLE);
 		database.execSQL(CREATE_THEME_TABLE);
 		db = database;
 		addTheme("Default", -1);
 		addTheme("Gaming", -1);
 		addTheme("Pomodoro App", -1);
 	}
 
 	public Cursor getDatesCursor() {
 		// String[] columns = new String[] { SQHelper.KEY_ROWID,
 		// SQHelper.KEY_DATE_DAY,
 		// SQHelper.KEY_DATE_MONTH, SQHelper.KEY_DATE_YEAR, SQHelper.KEY_TYPE,
 		// SQHelper.KEY_THEME };
 		openDatabase();
 		return db.query(TABLE_DATES, null, null, null, null, null, null);
 	}
 
 	public Cursor getThemeCursor() {
 		openDatabase();
 		return db.query(TABLE_THEME, null, null, null, null, null,
 				KEY_OVERALLTIME+ " DESC");
 	}
 	
 	public Cursor getThemeCursor(int hideStatus) {
 		openDatabase();
 		return db.query(TABLE_THEME, null, KEY_HIDE +" = "+hideStatus, null, null, null,
 				KEY_OVERALLTIME+ " DESC");
 	}
 
 	public int getStartUpPomodoroCount() {
 		openDatabase();
 		DateTime dt = new DateTime();
 		int day = dt.getDayOfMonth();
 		int month = dt.getMonthOfYear();
 		int year = dt.getYear();
 
 		Cursor c = db.query(TABLE_DATES, null, KEY_DATE_DAY + " = " + day
 				+ " AND " + KEY_DATE_MONTH + " = " + month + " AND "
 				+ KEY_DATE_YEAR + " = " + year + " AND " + KEY_TYPE + " = "
 				+ TYPE_POMODORO, null, null, null, null);
 		int count = c.getCount();
 		c.close();
 		return count;
 	}
 
 //	public int[] getTotalCountTimeDays(int[] types, int[] themes) {
 //		return getTotalCountTimeDays(types, themes, new int[0]);
 //	}
 
 	public int[] getTotalCountTimeDays(int[] types, int[] themes) {
 		int[] info = new int[3];
 
 		StringBuffer where = new StringBuffer();
 
 		if (types.length > 0 && themes.length > 0) {
 			where.append("(");
 		}
 
 		if (types.length > 0) {
 			for (int i = 0; i < types.length - 1; i++) {
 				where.append(KEY_TYPE + " = " + types[i] + " OR ");
 			}
 			where.append(KEY_TYPE + " = " + types[types.length - 1]);
 		}
 
 		if (types.length > 0 && themes.length > 0) {
 			where.append(") AND (");
 		}
 
 		if (themes.length > 0) {
 			for (int i = 0; i < themes.length - 1; i++) {
 				where.append(KEY_THEME + " = " + themes[i] + " OR ");
 			}
 			where.append(KEY_THEME + " = " + themes[themes.length - 1]);
 		}
 
 		if (types.length > 0 && themes.length > 0) {
 			where.append(")");
 		}
 
 		openDatabase();
 		Cursor c;
 		c = db.query(TABLE_DATES, null, where.toString(), null, null, null,
 				null);
 
 		info[0] = c.getCount();
 		info[1] = calculateDuration(c);
 		info[2] = calculateDays(c);
 		c.close();
 		return info;
 	}
 	
 	public int[] getTotalCountTimeDays(int[] types, int[] themes, int[] date) {
 		int[] info = new int[3];
 
 		StringBuffer finalwhere = new StringBuffer();
 		StringBuffer where1 = null;
 		StringBuffer where2 = null;
 		StringBuffer where3 = null;
 
 		// TYPE HERE
 		if (types.length > 0) {
 			where1 = new StringBuffer();
 			where1.append("(");
 			for (int i = 0; i < types.length - 1; i++) {
 				where1.append(KEY_TYPE + " = " + types[i] + " OR ");
 			}
 			where1.append(KEY_TYPE + " = " + types[types.length - 1]);
 			where1.append(")");
 		}
 
 		// THEME WHERE
 		if (themes.length > 0) {
 			where2 = new StringBuffer();
 			where2.append("(");
 			for (int i = 0; i < themes.length - 1; i++) {
 				where2.append(KEY_THEME + " = " + themes[i] + " OR ");
 			}
 			where2.append(KEY_THEME + " = " + themes[themes.length - 1]);
 			where2.append(")");
 		}
 
 		// DATE WHERE
 		
 		if (date.length > 0) {
 			where3 = new StringBuffer();
 			where3.append("(");
 			where3.append(KEY_DATE_DAY + " = " + date[0] + " AND ");
 			where3.append(KEY_DATE_MONTH + " = " + date[1] + " AND ");
 			where3.append(KEY_DATE_YEAR + " = " + date[2]);
 			where3.append(")");
 		}
 		
 //		if(where1 != null && where2 != null && where3 != null){
 //			finalwhere.append("(");
 //			finalwhere.append(where1.toString());
 //			finalwhere.append(") AND (");
 //			finalwhere.append(where2.toString());
 //			finalwhere.append(") AND (");
 //			finalwhere.append(where3.toString());
 //			finalwhere.append(")");
 //		}
 		
 		if(where1 != null){
 			finalwhere.append(where1.toString());
 		}
 		
 		if(where1 != null && where2 != null){
 			finalwhere.append(" AND ");
 		}
 		
 		if(where2 != null){
 			finalwhere.append(where2.toString());
 		}
 		
 		if(where3 != null && (where1 != null || where2 != null)){
 			finalwhere.append(" AND ");
 		}
 		
 		if(where3 != null){
 			finalwhere.append(where3.toString());
 		}
 		
 		openDatabase();
 		Cursor c;
 		c = db.query(TABLE_DATES, null, finalwhere.toString(), null, null, null,
 				null);
 
 		info[0] = c.getCount();
 		info[1] = calculateDuration(c);
 		info[2] = calculateDays(c);
 		
 		c.close();
 		return info;
 	}
 
 	public int getStartUpPomodoroTime() {
 		openDatabase();
 		DateTime dt = new DateTime();
 		int day = dt.getDayOfMonth();
 		int month = dt.getMonthOfYear();
 		int year = dt.getYear();
 
 		Cursor c = db.query(TABLE_DATES, new String[] { KEY_DURATION, },
 				KEY_DATE_DAY + " = " + day + " AND " + KEY_DATE_MONTH + " = "
 						+ month + " AND " + KEY_DATE_YEAR + " = " + year
 						+ " AND " + KEY_TYPE + " = " + TYPE_POMODORO, null,
 				null, null, null);
 
 		int duration = calculateDuration(c);
 
 		c.close();
 		return duration;
 	}
 
 	public int getStartUpBreakTime() {
 		openDatabase();
 		DateTime dt = new DateTime();
 		int day = dt.getDayOfMonth();
 		int month = dt.getMonthOfYear();
 		int year = dt.getYear();
 
 		Cursor c = db.query(TABLE_DATES, new String[] { KEY_DURATION, },
 				KEY_DATE_DAY + " = " + day + " AND " + KEY_DATE_MONTH + " = "
 						+ month + " AND " + KEY_DATE_YEAR + " = " + year
 						+ " AND (" + KEY_TYPE + " = " + TYPE_SHORTBREAK
 						+ " OR " + KEY_TYPE + " = " + TYPE_LONGBREAK + ")",
 				null, null, null, null);
 
 		int duration = calculateDuration(c);
 		c.close();
 		return duration;
 	}
 	
 	public int getStartUpTrackingCount() {
 		openDatabase();
 		DateTime dt = new DateTime();
 		int day = dt.getDayOfMonth();
 		int month = dt.getMonthOfYear();
 		int year = dt.getYear();
 
 		Cursor c = db.query(TABLE_DATES, new String[] { KEY_DURATION, },
 				KEY_DATE_DAY + " = " + day + " AND " + KEY_DATE_MONTH + " = "
 						+ month + " AND " + KEY_DATE_YEAR + " = " + year
 						+ " AND " + KEY_TYPE + " = " + TYPE_TRACKING,
 				null, null, null, null);
 
 		int duration = calculateDuration(c);
 		c.close();
 		return duration;
 	}
 
 	public int calculateDuration(Cursor c) {
 		int duration = 0;
 		int column = c.getColumnIndex(KEY_DURATION);
 		if (c.moveToFirst()) {
 			do {
 				duration += c.getInt(column);
 			} while (c.moveToNext());
 		}
 		return duration;
 	}
 
 	public int calculateDays(Cursor c) {
 		int days = 0;
 		int day_column = c.getColumnIndex(KEY_DATE_DAY);
 		int month_column = c.getColumnIndex(KEY_DATE_MONTH);
 
 		int day_tmp = -1;
 		int month_tmp = -1;
 		if (c.moveToFirst()) {
 			do {
 				if (c.getInt(day_column) != day_tmp
 						|| c.getInt(month_column) != month_tmp) {
 					day_tmp = c.getInt(day_column);
 					month_tmp = c.getInt(month_column);
 					days++;
 				}
 			} while (c.moveToNext());
 		}
 		return days;
 	}
 
 	public Cursor getLastXDays(int x) {
 		openDatabase();
 		DateTime dt = new DateTime();
 		int day = dt.getDayOfMonth();
 		int month = dt.getMonthOfYear();
 		int year = dt.getYear();
 
 		dt = dt.minusDays(x);
 		int old_day = dt.getDayOfMonth();
 		int old_month = dt.getMonthOfYear();
 		int old_year = dt.getYear();
 
 		return db.query(TABLE_DATES, new String[] { KEY_DATE_WEEKDAY, KEY_TYPE,
 				KEY_DURATION }, "(" + KEY_DATE_DAY + " <= " + day + " AND "
 				+ KEY_DATE_MONTH + " <= " + month + " AND " + KEY_DATE_YEAR
 				+ " <= " + year
 
 				+ " AND " + KEY_DATE_DAY + " >= " + old_day + " AND "
 				+ KEY_DATE_MONTH + " >= " + old_month + " AND " + KEY_DATE_YEAR
 				+ " >= " + old_year
 
 				+ ")", null, null, null, null);
 	}
 
 	public void addTheme(String name, int parentId) {
 		openDatabase();
 		ContentValues newContent = new ContentValues();
 		newContent.put(KEY_NAME, name);
 		newContent.put(KEY_ITEMOF, parentId);
 		newContent.put(KEY_OVERALLTIME, 0);
 		newContent.put(KEY_HIDE, 0);
 		db.insert(TABLE_THEME, null, newContent);
 	}
 	
 	public void changeTheme(int id, String name, int parentId){
 		openDatabase();
 		ContentValues newContent = new ContentValues();
 		newContent.put(KEY_NAME, name);
 		newContent.put(KEY_ITEMOF, parentId);
 		db.update(TABLE_THEME, newContent, KEY_ROWID+" = "+id, null);
 	}
 	
 	public void deleteTheme(int id, String name, int newId){
 		openDatabase();
 		ContentValues newContent = new ContentValues();
 		newContent.put(KEY_THEME, newId);
 		db.update(TABLE_DATES, newContent, KEY_THEME+" = "+id, null);		
 		db.delete(TABLE_THEME, KEY_ROWID+" = "+id, null);
 	}
 	
 	public void changeThemeStatus(int id, int hide){
 		openDatabase();
 		ContentValues newContent = new ContentValues();
 		newContent.put(KEY_HIDE, hide);		
 		db.update(TABLE_THEME, newContent, KEY_ROWID+" = "+id, null);
 	}
 	
 	public String getTheme(int id) {
 		if(id == -1) 
 			return "";
 		openDatabase();
 		Cursor c = db.query(TABLE_THEME, new String[] { KEY_NAME }, KEY_ROWID
 				+ " = ?", new String[] { id + "" }, null, null, null);
 		String name = "error";
 		if (c.moveToFirst()) {
 			name = c.getString(0);
 		}
 		// c.close();
 
 		return name;
 	}
 
 	public int getTheme(String name) {
 		openDatabase();
 		Cursor c = db.query(TABLE_THEME, new String[] { KEY_ROWID }, KEY_NAME
 				+ " = ?", new String[] { name }, null, null, null);
 		int id = -99;
 		if (c.moveToFirst()) {
 			id = c.getInt(0);
 		}
 		// c.close();
 		return id;
 	}
 	
 	public void deleteEntry(int id){
 		db.delete(TABLE_DATES, KEY_ROWID+" = "+id, null);
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		Log.w(SQHelper.class.getName(), "Upgrading database from version "
 				+ oldVersion + " to " + newVersion
 				+ ", which will destroy all old data");
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_DATES);
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_THEME);
 		onCreate(db);
 	}
 
 	public void renewTables() {
 		openDatabase();
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_DATES);
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_THEME);
 		createTables(db);
 	}
 
 	public void insertDate(int tag, int minutesPast, String theme) {
 		openDatabase();
 
 		DateTime endDate = new DateTime();
 		DateTime startDate = endDate.minusMinutes(minutesPast);
 		ContentValues newContent = new ContentValues();
 		newContent.put(KEY_DATE_DAY, startDate.getDayOfMonth());
 		newContent.put(KEY_DATE_MONTH, startDate.getMonthOfYear());
 		newContent.put(KEY_DATE_YEAR, startDate.getYear());
 		newContent.put(KEY_DATE_WEEKDAY, startDate.getDayOfWeek());
 		newContent.put(KEY_DATE_WEEKNUM, startDate.getWeekOfWeekyear());
 		newContent.put(KEY_DATE_START_HOUR, startDate.getHourOfDay());
 		newContent.put(KEY_DATE_START_MINUTE, startDate.getMinuteOfHour());
 		newContent.put(KEY_DATE_END_HOUR, endDate.getHourOfDay());
 		newContent.put(KEY_DATE_END_MINUTE, endDate.getMinuteOfHour());
 		newContent.put(KEY_TYPE, tag);
 		newContent.put(KEY_DURATION, minutesPast);
 		newContent.put(KEY_THEME, getTheme(theme));
 		newContent.put(KEY_STARTTIMEMILLIES, startDate.getMillis());
 		newContent.put(KEY_ENDTIMEMILLIES, endDate.getMillis());
 		db.insert(TABLE_DATES, null, newContent);
 		Cursor c = db.rawQuery("UPDATE " + TABLE_THEME + " SET " + KEY_OVERALLTIME + " = "
 				+ KEY_OVERALLTIME + " + " + minutesPast + " WHERE " + KEY_ROWID
 				+ "=" + getTheme(theme), null);
 		
 		if(c.moveToFirst()){
 			Log.e("SQHelper", "Update " + c.getInt(c.getColumnIndex(KEY_OVERALLTIME)));
 		} else {
 			Log.e("SQHelper", "Update cursor empty");
 		}
 	}
 
 	private void openDatabase() {
 		if (db == null)
 			db = getWritableDatabase();
 	}
 
 }
