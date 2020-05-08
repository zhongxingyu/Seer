 package edu.gatech.cs2340.group29.spacemerchant.activity;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class DatabaseHelper extends SQLiteOpenHelper
 {
 
 		public DatabaseHelper(Context context)
 		{			
 			super(context, "spaceMerchant" , null, 1);
 			
 		}
 
 	
 		public void onCreate(SQLiteDatabase db) 
 		{
 			final String query = ""                     
 				+ "create table tb_game( "
			    + "    game integer primary key autoincrement "
				+ "    playerName string not null" 
			    + "    pilotSkillPoints integer not null "
			    + "    fighterSkillPoints integer not null"
			    + "    traderSkillPoints integer not null" 
			    + "    engineerSkillPoints integer not null "
			    + "    planet integer not null"
				+ "    difficulty integer not null";
 			
 			db.execSQL(query);
 		}
 
 
 		public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) 
 		{
 	
 			
 		}
 		
 	
 }
