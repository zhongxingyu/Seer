 package com.team.database;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class DBHelper extends SQLiteOpenHelper {
 
 	public static final String DATABASE_NAME = "stuass.db";
 	public static final int DATABASE_VERSION = 1;
 	public static final String SCHEDULE_TABLE = "tab_schedule";
 	public static final String TIME_TABLE = "tab_time";
 	// ݿ
 	private static final String SCHEDULE_TABLE_CREATE = "CREATE TABLE "
 			+ SCHEDULE_TABLE + " (" + ScheduleColumn._ID
 			+ " integer primary key autoincrement," + ScheduleColumn.DATE + " text,"
 			+ ScheduleColumn.SUBJECTNAME + " text," + ScheduleColumn.WHICHWEEK + " text,"
 			+ ScheduleColumn.WEEK + " text," + ScheduleColumn.ADDRESS + " text,"
 			+ ScheduleColumn.WHICHLESSON + " text," + ScheduleColumn.ISSILENCE + " integer,"
 			+ ScheduleColumn.ISREMINDBYVIBRATO + " integer," + ScheduleColumn.ISREMINDBYRING
 			+ " integer" + ScheduleColumn.REMINDTIME + " text);";
 	
 	private static final String TIME_TABLE_CREATE = "CREATE TABLE "
 			+ SCHEDULE_TABLE + " (" + TimeColumn._ID
 			+ " integer primary key autoincrement," + TimeColumn.WHICHLESSON + " text,"
 			+ TimeColumn.STARTTIME + " text," + TimeColumn.ENDTIME + " text);";
 
 	public DBHelper(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 
 		db.execSQL(SCHEDULE_TABLE_CREATE);
 		db.execSQL(TIME_TABLE_CREATE);
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 
 		db.execSQL("DROP TABLE IF EXISTS " + SCHEDULE_TABLE);
 		db.execSQL("DROP TABLE IF EXISTS " + TIME_TABLE);
 		onCreate(db);
 	}
 	public long insertschedule(String date1, String subjectName1, String whichWeek1, String week1, String address1, String whichLesson1,int isSilence1,int isRemindByVibrato1,int isRemindByRing1, String remindTime1) {  
         SQLiteDatabase db = this.getWritableDatabase();  
         ContentValues cv = new ContentValues(); 
         long row = db.insert(SCHEDULE_TABLE, ScheduleColumn._ID, cv);  
         cv.put(ScheduleColumn.DATE, date1);  
         cv.put(ScheduleColumn.SUBJECTNAME, subjectName1);
         cv.put(ScheduleColumn.WHICHWEEK, whichWeek1);
         cv.put(ScheduleColumn.WEEK, week1);
         cv.put(ScheduleColumn.ADDRESS, address1);
         cv.put(ScheduleColumn.WHICHLESSON, whichLesson1);
         cv.put(ScheduleColumn.ISSILENCE, isSilence1);
         cv.put(ScheduleColumn.ISREMINDBYVIBRATO, isRemindByVibrato1);
         cv.put(ScheduleColumn.ISREMINDBYRING, isRemindByRing1);
         cv.put(ScheduleColumn.REMINDTIME, remindTime1);
         return row;  
     }
     public int deleteschedule(int id) {  
         SQLiteDatabase db = this.getWritableDatabase();  
         String where = ScheduleColumn._ID + " = ?";  
         String[] whereValue = { Integer.toString(id) };  
         return db.delete(SCHEDULE_TABLE, where, whereValue);  
     } 
     public int updateschedule(String id, String date1, String subjectName1, String whichWeek1, String week1, String address1, String whichLesson1,int isSilence1,int isRemindByVibrato1,int isRemindByRing1, String remindTime1) {  
         SQLiteDatabase db = this.getWritableDatabase();  
         String where = ScheduleColumn._ID + " = ?";  
         String[] whereValue = { id };  
         ContentValues cv = new ContentValues();  
         cv.put(ScheduleColumn.DATE, date1);  
         cv.put(ScheduleColumn.SUBJECTNAME, subjectName1);
         cv.put(ScheduleColumn.WHICHWEEK, whichWeek1);
         cv.put(ScheduleColumn.WEEK, week1);
         cv.put(ScheduleColumn.ADDRESS, address1);
         cv.put(ScheduleColumn.WHICHLESSON, whichLesson1);
         cv.put(ScheduleColumn.ISSILENCE, isSilence1);
         cv.put(ScheduleColumn.ISREMINDBYVIBRATO, isRemindByVibrato1);
         cv.put(ScheduleColumn.ISREMINDBYRING, isRemindByRing1);
         cv.put(ScheduleColumn.REMINDTIME, remindTime1);
         return db.update(SCHEDULE_TABLE, cv, where, whereValue);  
     } 
    public Cursor select() {  
         SQLiteDatabase db = this.getReadableDatabase();  
         Cursor cursor = db.query(SCHEDULE_TABLE, ScheduleColumn.PROJECTION, null, null, null, null, null);  
         return cursor;  
     }
    public Cursor getNote(String id){
 		SQLiteDatabase db = this.getReadableDatabase();
 		String where = ScheduleColumn._ID + "=?";
 		String[] whereValues = {id};
 		Cursor cursor = db.query(SCHEDULE_TABLE, null, where, whereValues, null, null, null);
 		return cursor;
 	}
 	public long inserttime(String whichLesson1,String startTime1,String endTime1){
 		SQLiteDatabase db = this.getWritableDatabase();
 		ContentValues cv = new ContentValues();
 		long row = db.insert(TIME_TABLE, ScheduleColumn._ID, cv); 
 		cv.put(TimeColumn.WHICHLESSON, whichLesson1);
 		cv.put(TimeColumn.STARTTIME, startTime1);
 		cv.put(TimeColumn.ENDTIME, endTime1);
 		return row;
 	}
 
 
 	public int deletetime(int id) {
 		SQLiteDatabase db = this.getWritableDatabase();
 		String where = TimeColumn._ID + "= ?";
 		String[] whereValue = { Integer.toString(id) };
 		return db.delete(TIME_TABLE, where, whereValue);
 	}
 	
 	public int updatetime(String id,String whichLesson1,String startTime1,String endTime1) {
 		SQLiteDatabase db = this.getWritableDatabase();
 		String where = TimeColumn._ID + "= ?";
 		String[] whereValue = { id };
 		ContentValues cv = new ContentValues();
 		cv.put(TimeColumn.WHICHLESSON, whichLesson1);
 		cv.put(TimeColumn.STARTTIME, startTime1);
 		cv.put(TimeColumn.ENDTIME, endTime1);
 		return db.update(TIME_TABLE, cv, where, whereValue);
 	}
 	
 	public Cursor gettime(String id) {  
 		SQLiteDatabase db = this.getReadableDatabase();
 		String where = ScheduleColumn._ID + "=?";
 		String[] whereValues = {id}; 
 		Cursor cursor = db.query(TIME_TABLE, TimeColumn.PROJECTION, where, whereValues, null, null, null);
         return cursor;  
     } 
 
 }
