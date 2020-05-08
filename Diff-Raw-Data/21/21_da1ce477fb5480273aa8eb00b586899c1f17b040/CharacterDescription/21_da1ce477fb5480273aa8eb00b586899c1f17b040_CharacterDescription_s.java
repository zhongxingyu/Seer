 package uw.cse403.minion;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Debug;
 
 /**
  * A wrapper class around the basic descriptions of a character. 
  * Which can be found on the top of most character sheets as the name,
  * age, alignment, ect...
  * 
  * @author Loki White (lokiw)
  */
 public class CharacterDescription {
 
 	/** The unique id for a character **/
 	public long charID;
 
 	/** Used to determine whether to populate UI with stored values **/
 	public boolean isNew; 
 
 	/** Various components the make up the basic information about a character **/
 	public String name;
 	public String player;
 	public String alignment;
 	public int level;
 	public String size;
	public Alignment firstAlign;
	public Alignment secondAlign;
 	public String deity;
 	public String homeLand;
 	public String gender;
 	public String race;
 	public int age;
 	public int height;
 	public int weight;
 	public String hair;
 	public String eyes;
 
 	/**
 	 * Initializes and populates a CharacterDescription object from the data
 	 * stored in the local database.
 	 * @param id The id of the character whose information is to be loaded
 	 */
 	public CharacterDescription(long id) {
 		charID = id;
 		// set defaults
 		name = "";
 		player = "";
 		alignment = "";
 		size = "";
		firstAlign = null;
		secondAlign = null;
 		level = 0;
 		deity = "";
 		homeLand = "";
 		gender = "";
 		race = "";
 		age = 0;
 		height = 0;
 		weight = 0;
 		hair = "";
 		eyes = "";
 
 		if (TraceControl.TRACE)
 			Debug.startMethodTracing("CharacterDescription_constructor");
 		
 		loadFromDB();
 		
 		if (TraceControl.TRACE)
 			Debug.stopMethodTracing();
 	}
 
 	/*
 	 * Testing Results:
 	 * Looking into the performance of loadFromDB, again much of it is tied up in database
 	 * operations. 1% of the entire operation is string building directly in the application,
 	 * hardly worth optimizing out with a StringBuilder vis a vis concatenation.
 	 */
 	
 	/**
 	 * Populate fields with values from DB
 	 */
 	private void loadFromDB() {
 		isNew = true;
 		// attempt to load from DB
 		Cursor cursor = SQLiteHelperBasicInfo.db.query(SQLiteHelperBasicInfo.TABLE_NAME, SQLiteHelperBasicInfo.ALL_COLUMNS, 
 				SQLiteHelperBasicInfo.COLUMN_ID + " = " + charID, null, null, null, null);
 		if (cursor.moveToFirst()) {
 			isNew = false;
 			// Columns: COLUMN_ID, COLUMN_NAME, COLUMN_ALIGNMENT, COLUMN_LEVEL, COLUMN_DEITY, 
 			// COLUMN_HOMELAND, COLUMN_RACE, COLUMN_SIZE, COLUMN_GENDER, COLUMN_AGE, 
 			// COLUMN_HEIGHT, COLUMN_WEIGHT, COLUMN_HAIR, COLUMN_EYES
 			name 		= cursor.getString(1);
 			alignment 	= cursor.getString(2);
 			level 		= cursor.getInt(3);
 			deity 		= cursor.getString(4);
 			homeLand 	= cursor.getString(5);
 			race 		= cursor.getString(6);
 			size 		= cursor.getString(7);
 			gender 		= cursor.getString(8);
 			age 		= cursor.getInt(9);
 			height 		= cursor.getInt(10);
 			weight 		= cursor.getInt(11);
 			hair 		= cursor.getString(12);
 			eyes 		= cursor.getString(13);
 		}
 		cursor.close();
 	}
 
 	/** 
 	 * Writes character description / basic info to database. SHOULD ONLY BE CALLED BY CHARACTER
 	 * @param id id of character
 	 * @param db database to write into
 	 */
 	public void writeToDB() {
 		SQLiteDatabase db = SQLiteHelperBasicInfo.db;
 
 		ContentValues values = new ContentValues();
 		values.put(SQLiteHelperBasicInfo.COLUMN_NAME, name);
 		values.put(SQLiteHelperBasicInfo.COLUMN_ID, charID);
 		values.put(SQLiteHelperBasicInfo.COLUMN_ALIGNMENT, alignment);
 		values.put(SQLiteHelperBasicInfo.COLUMN_LEVEL, level);
 		values.put(SQLiteHelperBasicInfo.COLUMN_SIZE, size);
 		values.put(SQLiteHelperBasicInfo.COLUMN_DEITY, deity);
 		values.put(SQLiteHelperBasicInfo.COLUMN_HOMELAND, homeLand);
 		values.put(SQLiteHelperBasicInfo.COLUMN_GENDER, gender);
 		values.put(SQLiteHelperBasicInfo.COLUMN_RACE, race);
 		values.put(SQLiteHelperBasicInfo.COLUMN_AGE, age);
 		values.put(SQLiteHelperBasicInfo.COLUMN_HEIGHT, height);
 		values.put(SQLiteHelperBasicInfo.COLUMN_WEIGHT, weight);
 		values.put(SQLiteHelperBasicInfo.COLUMN_HAIR, hair);
 		values.put(SQLiteHelperBasicInfo.COLUMN_EYES, eyes);
 
 		if (isNew) {
 			db.insert(SQLiteHelperBasicInfo.TABLE_NAME, null, values);
 		} else {
 			db.update(SQLiteHelperBasicInfo.TABLE_NAME, values, SQLiteHelperBasicInfo.COLUMN_ID + " = " + charID, null);
 		}
 	}
 }
