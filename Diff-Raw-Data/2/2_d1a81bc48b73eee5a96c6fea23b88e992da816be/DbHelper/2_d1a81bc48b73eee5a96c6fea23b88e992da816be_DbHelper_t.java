 package com.sqlitefun;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 //This class handles all the tables in the database.
 
 public class DbHelper extends SQLiteOpenHelper{
	private static final String DATABASE_NAME = "appdata.sqlite";
 	private static final int DATABASE_VERSION = 1;
 	private static final String CREATE_TEAMS = "create table teams " +
 			"(_id integer primary key autoincrement, teamname text not null);";
 	private static final String CREATE_PLAYERS = "create table players " +
 			"(_id integer primary key autoincrement, name text not null, team integer not null);";
 	private static final String CREATE_TIMES = "create table times" + 
 			"(_id integer primary key autoincrement, time integer not null, player integer not null);";
 
 	public DbHelper(Context context){
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 	public void onCreate(SQLiteDatabase db){
 		db.execSQL(CREATE_TEAMS);
 		db.execSQL(CREATE_PLAYERS);
 		db.execSQL(CREATE_TIMES);
 	}
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		Log.w(TimeDb.class.getName(),"Upgrading database from version " + oldVersion +" to "
 				+ newVersion + ", which will destroy all old data");
 		db.execSQL("DROP TABLE IF EXISTS teams");
 		db.execSQL("DROP TABLE IF EXISTS players");
 		db.execSQL("DROP TABLE IF EXISTS times");
 		onCreate(db);
 		
 	}
 }
