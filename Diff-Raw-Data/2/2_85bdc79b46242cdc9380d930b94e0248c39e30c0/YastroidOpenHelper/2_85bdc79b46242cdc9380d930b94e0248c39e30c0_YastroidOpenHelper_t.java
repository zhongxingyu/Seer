 package com.novell.android.yastroid;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 //import android.widget.Toast;
 
 public class YastroidOpenHelper extends SQLiteOpenHelper {
 
 	private static final String TAG = "YaSTroidDatabase";
 
 	private static final String DATABASE_NAME = "yastroid";
 	private static final int DATABASE_VERSION = 4;
 	static final String SERVERS_TABLE_NAME = "servers";
 	static final String SERVERS_NAME = "name";
 	static final String SERVERS_SCHEME = "scheme";
 	static final String SERVERS_HOST = "hostname";
 	static final String SERVERS_PORT = "port";
 	static final String SERVERS_USER = "user";
 	static final String SERVERS_PASS = "pass";
 	static final String SERVERS_GROUP = "grp";
 	static final String GROUP_TABLE_NAME = "groups";
 	static final String GROUP_NAME = "name";
 	static final String GROUP_DESCRIPTION = "description";
 	static final String GROUP_ICON = "icon";
 	static final int GROUP_DEFAULT_ALL = 1;
 	
 
 	private static final String CREATE_SERVER_TABLE = "CREATE TABLE "
 			+ SERVERS_TABLE_NAME + " ("
 			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
 			+ SERVERS_NAME + " TEXT, "
 			+ SERVERS_SCHEME + " TEXT, "
 			+ SERVERS_HOST + " TEXT, "
 			+ SERVERS_PORT + " INTEGER, "
 			+ SERVERS_USER + " TEXT, "
 			+ SERVERS_PASS + " TEXT, "
 			+ SERVERS_GROUP + " INTEGER);";
 	private static final String CREATE_GROUP_TABLE = "CREATE TABLE "
 			+ GROUP_TABLE_NAME + " ("
 			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
 			+ GROUP_NAME + " TEXT, "
 			+ GROUP_DESCRIPTION + " TEXT, "
 			+ GROUP_ICON + " INTEGER);";
 	
 	private static final String DEFAULT_GROUP = "INSERT INTO "
 			+ GROUP_TABLE_NAME
 			+ "(name,description,icon) VALUES ('All', 'All servers', '0');";
 
 	private static final String ADD_SERVER = "INSERT INTO "
 			+ SERVERS_TABLE_NAME
 			+ "(name,scheme,hostname,port,user,pass,grp) VALUES ('webyast1', 'http', '137.65.132.194', '4984','root','sandy','" + GROUP_DEFAULT_ALL +"');";
 
 	public YastroidOpenHelper(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		db.execSQL(CREATE_SERVER_TABLE);
 		db.execSQL(CREATE_GROUP_TABLE);
 		db.execSQL(DEFAULT_GROUP);
 		// Demo data
 		db.execSQL(ADD_SERVER);
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		Log.w(TAG, "Upgrading database from version " + oldVersion + "to "
 				+ newVersion);
 		// Any changes to the database structure should occur here.
 		// This is called if the DATABASE_VERSION installed is older
 		// than the new version.
 		// ie. db.execSQL("alter table " + TASKS_TABLE + " add column " +
 		// TASK_ADDRESS + " text");
 		db.execSQL("ALTER TABLE servers ADD COLUMN grp INTEGER;");
 		db.execSQL(CREATE_GROUP_TABLE);
 		db.execSQL(DEFAULT_GROUP);
		db.execSQL("UPDATE " + SERVERS_TABLE_NAME + " SET " + SERVERS_GROUP + "='" + GROUP_DEFAULT_ALL + "';");
 	}
 }
