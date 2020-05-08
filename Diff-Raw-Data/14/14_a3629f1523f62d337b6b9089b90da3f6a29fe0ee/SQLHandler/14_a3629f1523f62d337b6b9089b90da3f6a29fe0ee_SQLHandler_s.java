 package de.Psychologie.socialintelligence;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 
 public class SQLHandler extends SQLiteOpenHelper {
  
 	private static final String DATABASE_NAME = "socialintelligence.db";
 	private static final int DATABASE_VERSION = 4;
 	
 	/////////////////////////////////////////////////////////////
 	//// CREATE TABLES
 	/////////////////////////////////////////////////////////////
 	
 	private static final String tabCreateUser = "CREATE TABLE IF NOT EXISTS user ( " +
 												"ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
 												"code VARCHAR(5) NOT NULL)";
 	
 	private static final String tabCreatePoll = "CREATE TABLE IF NOT EXISTS poll ( " +
 												"ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
 												"date DATE NULL , " +
 												"alarm TIME NULL , " +
 												"answer TIME NULL , " +
 												"break CHAR NULL , " +
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
 												  "snoozeTime INTEGER NULL, " + 
 												  "startDay INTEGER NULL)";
 												  
 			
 			
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
 		// Default: ID, snooze deaktiv, snooze 10minutes, Starttag
 		ContentValues values = new ContentValues();
 		values.put("ID", 1);
 		values.put("snoozeActiv", 0);	// nicht aktiv
 		values.put("snoozeTime", 10);	// 10 Minuten
 		values.put("startDay", 0);		// Montag
 		db.insert("status", null, values);
 	}
 	
 	
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		db.execSQL("DROP TABLE IF EXISTS user");
 		db.execSQL("DROP TABLE IF EXISTS poll");
 		db.execSQL("DROP TABLE IF EXISTS time");
 		db.execSQL("DROP TABLE IF EXISTS status");
 		
 		onCreate(db);
 	}
 	
 	/////////////////////////////////////////////////////////////
 	//// Query
 	/////////////////////////////////////////////////////////////
 	
<<<<<<< HEAD
	// Wartezeit aktiv?
	public boolean getSnoozeActiv(){
=======
 	// Wartezeit auslesen 
 	// MAIN Activitiy
 	
	public boolean getSnooze(){
>>>>>>> f82993803612994feea23070f0b72f43ba544d4d
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
 	}
<<<<<<< HEAD
 	
 	// Wartezeit holen & setzen
 	public int getSnoozeTime(){
 		SQLiteDatabase db= this.getReadableDatabase();
 		Cursor c = db.rawQuery("SELECT snoozeTime FROM status WHERE ID=1",null);
 		if(c != null){
 			c.moveToFirst();
 			// prüfen, ob Snooze gesetzt
 		    return c.getInt(0);
 		} else {
 			return -1;
 		}
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
 	}
=======
>>>>>>> f82993803612994feea23070f0b72f43ba544d4d
	
 	
 	// add User Code
 	public void addUserCode(String code){
 		SQLiteDatabase db= this.getWritableDatabase();
 		
 		Log.v("code",code);
 		//content Typ, to import String code as table value
 		ContentValues cv = new ContentValues();
 		cv.put("code", code);
 		
 		db.insert("user", "code", cv);
 		db.close();
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
 		return exist;
 	}
 	
 	// add Week times
 	void addDayTime(int day,String time){
 		if(day<7 && day>=0){
 			SQLiteDatabase db= this.getWritableDatabase();
 			
 			ContentValues cv = new ContentValues();
 			cv.put("day",day);
 			//time in HH:MM:SS
 			time += ":00";
 			cv.put("time", time);
 			
 			db.insert("time", null, cv);
 			db.close();
 		}
 	}
 
 	/*
 	public Cursor getUserByID(int id){
 		 SQLiteDatabase db=this.getReadableDatabase();
 		 return db.rawQuery("SELECT ID as _id, name FROM "+tabUser+
 			" WHERE ID="+id, null);
 	}
 	*/
 	
 	
 }
 	
 /*
 
 	-- -----------------------------------------------------
 	-- Table `mydb`.`status`
 	-- -----------------------------------------------------
 	CREATE  TABLE IF NOT EXISTS `mydb`.`status` (
 	  `ID` INT NOT NULL AUTO_INCREMENT ,
 	  `lastAlarm` TIME NULL ,
 	  `snooze` CHAR NULL ,
 	  `startDay` CHAR NULL ,
 	  PRIMARY KEY (`ID`) )
 	ENGINE = InnoDB;
 
 */
 	
 
 
