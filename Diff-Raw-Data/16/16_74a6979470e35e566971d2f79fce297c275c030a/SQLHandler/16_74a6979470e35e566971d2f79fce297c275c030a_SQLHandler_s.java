 package de.Psychologie.socialintelligence;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class SQLHandler extends SQLiteOpenHelper {
  
 	private static final String DATABASE_NAME = "socialintelligence.db";
	private static final int DATABASE_VERSION = 2;
 	private static final int POLL_ABORT = -77;
 	
 	/////////////////////////////////////////////////////////////
 	//// CREATE TABLES
 	/////////////////////////////////////////////////////////////
 	
 	private static final String tabCreateUser = "CREATE TABLE IF NOT EXISTS user ( " +
 												"ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
 												"code VARCHAR(5) NOT NULL)";
 	
 	private static final String tabCreatePoll = "CREATE TABLE IF NOT EXISTS poll ( " +
 												"ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
 												"date TEXT NULL , " +
 												"alarm TEXT NULL , " +
 												"answer TEXT NULL , " +
 												"break INTEGER NULL , " +
 												"contact INTEGER NULL , " +
 												"hour INTEGER NULL , " +
 												"minute INTEGER NULL)";
 	
 	private static final String tabCreateTime = "CREATE TABLE IF NOT EXISTS time ( " +
 												"ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
 												"day INTEGER NULL, " +
 												"time TEXT NULL)";
 
 	private static final String tabCreateStatus = "CREATE TABLE IF NOT EXISTS status ( " +
 												  "ID INTEGER PRIMARY KEY NOT NULL, " +
 												  "snoozeActiv INTEGER NULL, " +	
 												  "lastAlarm Text NULL, " +
 												  "nextAlarm Text NULL)";
 			
 	/////////////////////////////////////////////////////////////
 	//// FIRST PROCESS
 	/////////////////////////////////////////////////////////////
 	
 	public SQLHandler(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		if(BuildConfig.DEBUG){
 			Log.v("SQL",tabCreateUser);
 			Log.v("SQL",tabCreatePoll);
 			Log.v("SQL",tabCreateTime);
 			Log.v("SQL",tabCreateStatus);
 		}
 		db.execSQL(tabCreateUser);
 		db.execSQL(tabCreatePoll);
 		db.execSQL(tabCreateTime);
 		db.execSQL(tabCreateStatus);
 		
 		// Status Default-Werte
 		ContentValues values = new ContentValues();
 		values.put("ID", 1);
 		values.put("snoozeActiv", 0);		 // nicht aktiv
 		values.put("lastAlarm", "00:00:00"); // Default keine Zeit
 		db.insert("status", null, values);
 		
 		// Times Default-Werte
 		ContentValues timeCv = new ContentValues();
 		String dayTimes[] = new String[4];
 		dayTimes[0] = "09:00:00";
 		dayTimes[1] = "13:00:00";
 		dayTimes[2] = "16:00:00";
 		dayTimes[3] = "20:00:00";
 		for(int i=0;i<7;i++){
 			for(int j=0;j<4;j++){
 				timeCv.put("day", i);
 				timeCv.put("time", dayTimes[j]);
 				db.insert("time", null, timeCv);
 			}
 		}
 	}
 	
 	
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		db.execSQL("DROP TABLE IF EXISTS user");
 		db.execSQL("DROP TABLE IF EXISTS poll");
 		db.execSQL("DROP TABLE IF EXISTS time");
 		db.execSQL("DROP TABLE IF EXISTS status");
 		
 		onCreate(db);
 	}
 	
 	// TODO: nur fr Testzwecke?
 	public void deleteDB(){
 		SQLiteDatabase db = this.getWritableDatabase();
 		db.execSQL("DROP TABLE IF EXISTS user");
 		db.execSQL("DROP TABLE IF EXISTS poll");
 		db.execSQL("DROP TABLE IF EXISTS time");
 		db.execSQL("DROP TABLE IF EXISTS status");
 		onCreate(db);
 	}
 	
 	/////////////////////////////////////////////////////////////
 	//// Query
 	/////////////////////////////////////////////////////////////
 	
 	// Wartezeit auslesen 
 	// MAIN Activitiy
 	
 	// Abbruch
 	public void setPollEntry(String date,String alarmTime){
 		setPollEntry(date,alarmTime,String.valueOf(POLL_ABORT),true,POLL_ABORT,POLL_ABORT,POLL_ABORT);
 	}
 	
 	public void setPollEntry(String date,String alarmTime, String answerTime,boolean abort,int contacts, int hour, int minute){
 		// Wurde Umfrage abgebrochen?
 		int breakup = abort?1:0;
 		SQLiteDatabase db= this.getWritableDatabase();
 		ContentValues cv = new ContentValues();
 		cv.put("date", date);
 		cv.put("alarm", alarmTime);
 		cv.put("answer", answerTime);
 		cv.put("break",breakup);
 		cv.put("contact",contacts);
 		cv.put("hour",hour);
 		cv.put("minute", minute);
 		// Daten speichern
 		db.insert("poll", null, cv);
 	}
 	
 	public Cursor getPollEntry(){
 		SQLiteDatabase db=this.getReadableDatabase();
 		Cursor cur=db.rawQuery("SELECT u.code, " +
 									  "p.date, " +
 									  "p.alarm, " +
 									  "p.answer, " +
 									  "p.break, " +
 									  "p.contact, " +
 									  "p.hour, " +
 									  "p.minute " +
 						      "FROM user u, " +
 						      "poll p",null);
 		return cur;
 	}
 	
 	public String getPollCsvContext(){
 		String context = ""; 
 		Cursor c = getPollEntry();
 		if(c != null){
 			if(c.moveToFirst()){
 				do{
 					context += c.getString(0) + ";" + c.getString(1) + ";" + c.getString(2) + ";";
 					context += c.getString(3) + ";" + String.valueOf(c.getInt(4)) + ";";
 					context += String.valueOf(c.getInt(5)) + ";" + String.valueOf(c.getInt(6)) + ";";
 					context += String.valueOf(c.getInt(7)) + "\n";
 				}while(c.moveToNext());
 			}
 		}
 		else
 			Log.i("cursor", "=null");
 		return context;
 	}
 	
 	public boolean getSnoozeActiv(){
 		SQLiteDatabase db= this.getReadableDatabase();
 		boolean snoozeActiv = false;
 		Cursor c = db.rawQuery("SELECT snoozeActiv FROM status WHERE ID=1",null);
 		if(c != null){
 			c.moveToFirst();
 			// prüfen, ob Snooze gesetzt
 		    if(c.getInt(0) == 1){
 		    	snoozeActiv = true;
 		    }
 		}
 		//db.close();
 		return snoozeActiv;
 	}
 	
 	public void setSnoozeActiv(boolean activ){
 		SQLiteDatabase db= this.getWritableDatabase();
 		// bool umwandeln
 		int value = activ?1:0;
 		// values als cv
 		ContentValues cv = new ContentValues();
 		cv.put("snoozeActiv", value);
 		// Datenbankupdate
 		db.update("status", cv, "ID = 1", null);
 		//db.close();
 	}
 	
 	// letzten Alarm setzen
 	public void setLastAlarm(String lastAlarmTime){
 		SQLiteDatabase db= this.getWritableDatabase();
 		ContentValues cv = new ContentValues();
 		cv.put("lastAlarm", lastAlarmTime);
 		db.update("status", cv, "ID = 1", null);
 		//db.close();
 	}
 	
 	// letzen Alarm auslesen
 	public String getLastAlarm() {
 		SQLiteDatabase db= this.getReadableDatabase();
 		String res = "00:00:00";
 		Cursor c = db.rawQuery("SELECT lastAlarm FROM status WHERE ID=1",null);
 		if(c != null){
 			c.moveToFirst();
 			res = c.getString(0);
 		}
 		//db.close();
 		return res;
 	}
 	
 	// nchsten Alarm setzen
 	public void setNextAlarm(String nextAlarmTime){
 		SQLiteDatabase db= this.getWritableDatabase();
 		ContentValues cv = new ContentValues();
 		cv.put("nextAlarm", nextAlarmTime);
 		db.update("status", cv, "ID = 1", null);
 		//db.close();
 	}
 	
 	// nchsten Alarm auslesen
 	public String getNextAlarm() {
 		SQLiteDatabase db= this.getReadableDatabase();
 		String res = "00:00:00";
 		Cursor c = db.rawQuery("SELECT nextAlarm FROM status WHERE ID=1",null);
 		if(c != null){
 			c.moveToFirst();
 			res = c.getString(0);
 		}
 		//db.close();
 		return res;
 	}
 	
 	/*
 	// Wartezeit holen & setzen
 	public int getSnoozeTime(){
 		SQLiteDatabase db= this.getReadableDatabase();
 		int res = -1;
 		Cursor c = db.rawQuery("SELECT snoozeTime FROM status WHERE ID=1",null);
 		if(c != null){
 			c.moveToFirst();
 			// prüfen, ob Snooze gesetzt
 			res =  c.getInt(0);
 		} 
 		//db.close();
 		return res;
 	}
 	
 	public void setSnoozeTime(int value){
 		SQLiteDatabase db= this.getWritableDatabase();
 		// nur positive Zeiten
 		value = value>0?value:15;
 		// values als cv
 		ContentValues cv = new ContentValues();
 		cv.put("snoozeTime", value);
 		// Datenbankupdate
 		db.update("status", cv, "ID = 1", null);
 		//db.close();
 	}
 	*/
 	
 	// add User Code
 	public void addUserCode(String code){
 		SQLiteDatabase db= this.getWritableDatabase();
 		
 		Log.v("code",code);
 		//content Typ, to import String code as table value
 		ContentValues cv = new ContentValues();
 		cv.put("code", code);
 		
 		db.insert("user", "code", cv);
 		//db.close();
 	}
 	
 	// get User Codes
 	public String getUserCode() {
 		SQLiteDatabase db = this.getReadableDatabase();
 		Cursor cur = db.rawQuery("SELECT code FROM user where ID=1", null);
 		String code = "";
 		if (cur != null)
 			if (cur.moveToFirst()) 
 				code = cur.getString(0);			
 		return code;
 	}
 	
 	public boolean existUserCode(){
 		SQLiteDatabase db= this.getReadableDatabase();
 		boolean exist = false;
 		Cursor c = db.rawQuery("SELECT COUNT(*) FROM user",null);
 		if(c != null){
 			c.moveToFirst();
 			if(c.getInt(0) > 0){
 				exist = true;
 			}
 		}
 		//db.close();
 		return exist;
 	}
 	
 	// add Week times
 	public void addDayTime(int day,String time){
 		if(day<7 && day>=0){
 			SQLiteDatabase db= this.getWritableDatabase();
 			
 			ContentValues cv = new ContentValues();
 			cv.put("day",day);
 			//time in HH:MM:SS
 			time += ":00";
 			cv.put("time", time);			
 			// import
 			db.insert("time", null, cv);
 			
 			//db.close();
 		}
 	}
 	
 	public void deleteDay(int day){
 		if(day<7 && day>=0){
 			SQLiteDatabase db= this.getWritableDatabase();		
 			// Tag lschen
 			db.delete("time", "day="+day, null);		
 			//db.close();
 		}
 	}
 	
 	public Cursor getDayTime(){
 		SQLiteDatabase db=this.getReadableDatabase();
 		Cursor cur=db.rawQuery("SELECT day,time from time",null);
 		//db.close();
 		return cur;
 	}
 	
 	public boolean existDayTime(int day,String time){
 		SQLiteDatabase db= this.getReadableDatabase();
 		boolean exist = false;
 		Cursor c = db.rawQuery("SELECT COUNT(*) FROM time WHERE day="+day+" AND time ='"+time+"'",null);
 		if(c != null){
 			c.moveToFirst();
 			Log.v("test",String.valueOf(c.getInt(0)));
 			if(c.getInt(0) > 0){
 				exist = true;
 			}
 		}
 		//db.close();
 		Log.v("test",String.valueOf(exist));
 		return exist;
 	}
 	
 	
 	//TODO: Fehler, bei c.getString(0) kahm eine CursorIndexOutOfBoundsException
 	// Schmiert ab, wenn keine Uhrzeit gewhlt wurde, die noch heute dran kommt.
 	public String getNextTimeFromDayTime(int day,String time){
 		String res = "00:00:00";
 //		if(existDayTime(day,time)){
 //			// SELECT time FROM time WHERE day = 0 AND ID = 1+(SELECT ID FROM time WHERE day= 0 AND time ='16:00:00')
 //			SQLiteDatabase db= this.getReadableDatabase();
 //			Cursor c = db.rawQuery("SELECT time FROM time " +
 //								   "WHERE day = "+day+" " +
 //								   "AND ID = 1+(SELECT ID " +
 //								    			 "FROM time " +
 //								    			 "WHERE day="+day+" " +
 //								    			 "AND time ='"+time+"')",null);
 //			if(c != null){
 //				c.moveToFirst();
 //				res = c.getString(0);
 //			}
 //			db.close();
 //			return res;
 //		} else {
 			// TODO: else Pfad klappt immer, if nicht notwendig!
 			// SELECT time FROM time WHERE day = 1 AND time(time) > time('18:00:00')
 			Log.v("test","DB-Zeit "+time);
 			Log.v("test","Tag "+String.valueOf(day));
 			SQLiteDatabase db = this.getReadableDatabase();
 			Cursor c = db.rawQuery("SELECT time FROM time " +
 								   "WHERE day = "+day+" " +
 								   "AND time(time) > time('"+time+"')",null);
 			if(c != null){
 				c.moveToFirst();
 				res = c.getString(0);
 			}
 			//db.close();
 			Log.v("test",String.valueOf(res));
 			return res;
 //		}
 	}
 	
 	public String getFirstTimeFromDay(int day){
 		String res = "00:00:00";
 		SQLiteDatabase db= this.getReadableDatabase();
 		Cursor c = db.rawQuery("SELECT time FROM time WHERE day='"+day+"' ORDER BY ID",null);
 		if(c != null){
 			c.moveToFirst();
 			res = c.getString(0);
 		}
 		//db.close();
 		return res;
 	}
 }
 	
 
 
