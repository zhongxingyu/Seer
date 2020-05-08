 package de.Psychologie.socialintelligence;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 
 public class SQLHandler extends SQLiteOpenHelper {
  
 	private static final String DATABASE_NAME = "socialintelligence.db";
	private static final int DATABASE_VERSION = 2;
 	
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
 		}
 		db.execSQL(tabCreateUser);
 		db.execSQL(tabCreatePoll);
 	}
 	
 	
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		db.execSQL("DROP TABLE IF EXISTS user");
 		db.execSQL("DROP TABLE IF EXISTS poll");
 	}
 	
 	/////////////////////////////////////////////////////////////
 	//// Query
 	/////////////////////////////////////////////////////////////
 	
 	// add User Code
 	void addUserCode(String code){
 		SQLiteDatabase db= this.getWritableDatabase();
 		
 		//content Typ, to import String code as table value
 		ContentValues cv = new ContentValues();
 		cv.put("code", code);
 		
 		db.insert("user", "code", cv);
 		db.close();
 	}
 	
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
 
 
 	-- -----------------------------------------------------
 	-- Table `mydb`.`times`
 	-- -----------------------------------------------------
 	CREATE  TABLE IF NOT EXISTS `mydb`.`times` (
 	  `ID` INT NOT NULL AUTO_INCREMENT ,
 	  `day` CHAR NULL ,
 	  `time` TIME NULL ,
 	  PRIMARY KEY (`ID`) )
 	ENGINE = InnoDB;
 
 	USE `mydb` ;
 
 
 	SET SQL_MODE=@OLD_SQL_MODE;
 	SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
 	SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
 */
 	
 
 
