 package com.group7.dragonwars.engine.Database;
 
 import android.content.Context;
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.SQLException;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Database
 {
 	public class Entry
 	{
         public Double DAMAGEDEALT;
         public Double DAMAGERECEIVED;
         public Double DISTANCETRAVELLED;
         public Integer GOLDCOLLECTED;
         public Integer UNITSKILLED;
         public Integer UNITSMADE;
 	}
 
 	private static final String DATABASE_NAME =	"dragonwars.db";
 	private static final String DATABASE_TABLE_NAME = "statistics";
 	private static final int DATABASE_VERSION = 1;
 
 	private static final String DATABASE_CREATE =
         "create table if not exists " + DATABASE_TABLE_NAME + " (" +
         " GAMETIME INT NOT NULL," +
         " DAMAGEDEALT DOUBLE NOT NULL," +
         " DAMAGERECEIVED DOUBLE NOT NULL," +
         " DISTANCETRAVELLED DOUBLE NOT NULL," +
         " GOLDCOLLECTED INT NOT NULL," +
         " UNITSKILLED INT NOT NULL," +
         " UNITSMADE INT NOT NULL," +
        " PRIMARY KEY(GAMETIME)" +
         ");";
 
 
 	private SQLiteDatabase database;
 
 	public Database(Context con)
 	{
 		//open the database if it exists else, creates a new data base with that name
 		database = con.openOrCreateDatabase(DATABASE_NAME, 0, null);
         CreateTable();
 	}
 
 	public void CreateTable()
 	{
 		//creates the high_scores table
 		database.execSQL(DATABASE_CREATE);
 	}
 
 	public void DeleteTable()
 	{
 		Close();
 	}
 
 	//should be called when the DB is no longer needed
 	public void Close()
 	{
 		database.close();
 	}
 
 	//use to add a new high score to the database
 	public void AddEntry(Double damageDealt, Double damageReceived, Double distanceTravelled,
                          Integer goldCollected, Integer unitsKilled, Integer unitsMade)
 	{
 		//create content values
 		ContentValues values = new ContentValues();
 
 		values.put("GAMETIME", System.currentTimeMillis());
         values.put("DAMAGEDEALT", damageDealt);
         values.put("DAMAGERECEIVED", damageReceived);
         values.put("DISTANCETRAVELLED", distanceTravelled);
         values.put("GOLDCOLLECTED", goldCollected);
         values.put("UNITSKILLED", unitsKilled);
         values.put("UNITSMADE", unitsMade);
 
 		//add content values as a row
 		database.insert(DATABASE_TABLE_NAME, null, values);
 	}
 
 	public List<Entry> GetEntries()
 	{
 		//gets all entries from the high scores table
 		List<Entry> entries = new ArrayList<Entry>();
 
 		//get cursor to DB from query
         String[] query = {"GAMETIME", "DAMAGEDEALT", "DAMAGERECEIVED", "DISTANCETRAVELLED",
                           "GOLDCOLLECTED", "UNITSKILLED", "UNITSMADE"};
 
 		Cursor cursor = database.query(
             DATABASE_TABLE_NAME,
             query,
             null, null, null, null, null);
 
 		//count the number of entries
 		Integer numberOfEntries = cursor.getCount();
 		cursor.moveToFirst();
 		for(Integer entry = 0; entry < numberOfEntries; entry++)
 		{
 			Entry record = new Entry();
             /* We don't care about the game time */
 			record.DAMAGEDEALT	    = cursor.getDouble(1);
 			record.DAMAGERECEIVED	= cursor.getDouble(2);
 			record.DISTANCETRAVELLED	= cursor.getDouble(3);
             record.GOLDCOLLECTED	= cursor.getInt(4);
             record.UNITSKILLED		= cursor.getInt(5);
             record.UNITSMADE        = cursor.getInt(6);
             entries.add(record);
             cursor.moveToNext();
 		}
 
         return entries;
 	}
 
     public Entry GetSummedEntries() {
         List<Entry> entries = GetEntries();
         Entry rec = new Entry();
         rec.DAMAGEDEALT	     = 0.0;;
         rec.DAMAGERECEIVED	 = 0.0;;
         rec.DISTANCETRAVELLED = 0.0;;
         rec.GOLDCOLLECTED	 = 0;
         rec.UNITSKILLED		 = 0;
         rec.UNITSMADE        = 0;
         for (Entry ent : entries) {
             rec.DAMAGEDEALT	     += ent.DAMAGEDEALT;
             rec.DAMAGERECEIVED	 += ent.DAMAGERECEIVED;
             rec.DISTANCETRAVELLED += ent.DISTANCETRAVELLED;
             rec.GOLDCOLLECTED	 += ent.GOLDCOLLECTED;
             rec.UNITSKILLED		 += ent.UNITSKILLED;
             rec.UNITSMADE        += ent.UNITSMADE;
         }
 
         return rec;
     }
 }
