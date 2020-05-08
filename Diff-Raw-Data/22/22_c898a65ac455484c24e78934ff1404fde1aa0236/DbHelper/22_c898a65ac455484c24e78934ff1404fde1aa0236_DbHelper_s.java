 package com.hstefan.aunctiondroid.db;
 
 import java.io.FileNotFoundException;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class DbHelper extends SQLiteOpenHelper {
 	
 	public static final String DATABASE_NAME= "aunctiondroid.db";
 	public static final int DATABASE_VERSION = 4;
 	
 	public static final String USER_TABLE = "user";
 	public static final String ITEM_TABLE = "item";
 	public static final String USER_ITEM_TABLE = "user_item";
 	public static final String AUNCTIONS_TABLE = "aunctions";
 	
 	public static final String DB_SAMPLE_DATA_FILE = ".auctiondroid_sample";
 	
 	public DbHelper(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		//eraseTableRows();
 		//insertExampleData(getWritableDatabase());
 		try {
 			context.openFileInput(DB_SAMPLE_DATA_FILE);
 		} catch (FileNotFoundException e) {
 			try {
 				context.openFileOutput(DB_SAMPLE_DATA_FILE, 0);
 			} catch (FileNotFoundException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 		}
 	}
 
 	@SuppressWarnings("unused")
 	private void eraseTableRows() {
 		SQLiteDatabase db = getWritableDatabase();
 		db.delete(USER_ITEM_TABLE, null, null);
 		Log.i("Dropping table", USER_ITEM_TABLE);
 		db.delete(ITEM_TABLE, null, null);
 		Log.i("Dropping table", ITEM_TABLE);
 		db.delete(USER_TABLE, null, null);
 		Log.i("Dropping table", USER_TABLE);
 		db.delete(AUNCTIONS_TABLE, null, null);
 		Log.i("Dropping table", AUNCTIONS_TABLE);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		if(DATABASE_VERSION >= 1) {
 			String create_sql = "CREATE TABLE " + USER_TABLE +
 					"(id INTEGER PRIMARY KEY AUTOINCREMENT," +
 					"email TEXT NOT NULL," +
 					"password TEXT NOT NULL)";
 			db.execSQL(create_sql);
 			Log.i("Creating SQLite table", USER_TABLE);
 		} 
 		if(DATABASE_VERSION >= 2) {
 			String create_sql = "CREATE TABLE " + ITEM_TABLE +
 			"(id INTEGER PRIMARY KEY AUTOINCREMENT," +
 			"name TEXT NOT NULL)";
 			db.execSQL(create_sql);
 			Log.i("Creating SQLite table", ITEM_TABLE);
 			
 			create_sql = "CREATE TABLE " + USER_ITEM_TABLE +
 			"(id_user INTEGER, id_item INTEGER, " +
 			"FOREIGN KEY(id_user) REFERENCES " + USER_TABLE + ","  + 
 			"FOREIGN KEY(id_item) REFERENCES " + ITEM_TABLE + ")";
 			db.execSQL(create_sql);
 			Log.i("Creating SQLite table", USER_ITEM_TABLE);
 		} if(DATABASE_VERSION >= 3) {
 			StringBuilder builder = new StringBuilder();
 			builder.append("ALTER TABLE ");
 			builder.append(USER_TABLE);
 			builder.append(" ADD money INTEGER");
 			db.execSQL(builder.toString());
 			Log.i("Upgrading SQLite table", USER_TABLE);
 			
 			builder.setLength(0);
 			builder.append("CREATE TABLE ");
 			builder.append(AUNCTIONS_TABLE);
 			builder.append("(id INTEGER PRIMARY KEY, id_item INTEGER, id_user INTEGER,");
 			builder.append("price INTEGER NOT NULL, ");
 			builder.append("FOREIGN KEY (id_item) REFERENCES ");
 			builder.append(ITEM_TABLE);
 			builder.append(",");
 			builder.append("FOREIGN KEY (id_user) REFERENCES ");
 			builder.append(USER_TABLE);
 			builder.append(")");
 			db.execSQL(builder.toString());
 			Log.i("Creating SQLite table", AUNCTIONS_TABLE);
 			
 			//Now Playing: Metallica - St. Anger - St. Anger - 2003 - Rock - MP3 - 320 kbps - 7:21 min
 		} if(DATABASE_VERSION >= 4) {
 			db.execSQL("DROP TABLE " + AUNCTIONS_TABLE);
 			Log.i("Dropping table", AUNCTIONS_TABLE);
 			
 			StringBuilder builder = new StringBuilder();
 			builder.setLength(0);
 			builder.append("CREATE TABLE ");
 			builder.append(AUNCTIONS_TABLE);
 			builder.append("(id INTEGER PRIMARY KEY AUTOINCREMENT, id_item INTEGER, id_user INTEGER,");
 			builder.append("price INTEGER NOT NULL, ");
 			builder.append("FOREIGN KEY (id_item) REFERENCES ");
 			builder.append(ITEM_TABLE);
 			builder.append(",");
 			builder.append("FOREIGN KEY (id_user) REFERENCES ");
 			builder.append(USER_TABLE);
 			builder.append(")");
 			db.execSQL(builder.toString());
 			Log.i("Creating SQLite table", AUNCTIONS_TABLE);
 		}
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		if(oldVersion == 1 && newVersion == 2) {
 			String create_sql = "CREATE TABLE " + ITEM_TABLE +
 			"(id INTEGER PRIMARY KEY AUTOINCREMENT," +
 			"name TEXT NOT NULL)";
 			db.execSQL(create_sql);
 			Log.i("Creating SQLite table(upgrade)", ITEM_TABLE);
 			
 			create_sql = "CREATE TABLE " + USER_ITEM_TABLE +
 			"(id_user INTEGER, id_item INTEGER, " +
 			"FOREIGN KEY(id_user) REFERENCES " + USER_TABLE + ","  + 
 			"FOREIGN KEY(id_item) REFERENCES " + ITEM_TABLE + ")";
 			db.execSQL(create_sql);
 			Log.i("Creating SQLite table(upgrade)", USER_ITEM_TABLE);
 		} if(DATABASE_VERSION >= 3 && newVersion != 4) {
 			StringBuilder builder = new StringBuilder();
 			builder.append("ALTER TABLE ");
 			builder.append(USER_TABLE);
 			builder.append(" ADD money INTEGER");
 			db.execSQL(builder.toString());
 			Log.i("Upgrading SQLite table", USER_TABLE);
 			
 			builder.setLength(0);
 			builder.append("CREATE TABLE ");
 			builder.append(AUNCTIONS_TABLE);
 			builder.append("(id INTEGER PRIMARY KEY AUTOINCREMENT, id_item INTEGER, id_user INTEGER,");
 			builder.append("price INTEGER NOT NULL, ");
 			builder.append("FOREIGN KEY (id_item) REFERENCES ");
 			builder.append(ITEM_TABLE);
 			builder.append(",");
 			builder.append("FOREIGN KEY (id_user) REFERENCES ");
 			builder.append(USER_TABLE);
 			builder.append(")");
 			db.execSQL(builder.toString());
 			Log.i("Upgrading SQLite table", AUNCTIONS_TABLE);
 		}
 	}
 	
 	private void insertExampleData(SQLiteDatabase db) {
 		if(DATABASE_VERSION >= 1) {
 			try {
 				ContentValues cv = new ContentValues();
 				cv.put("email", "hugopuhlmann@gmail.com");
 				cv.put("password", new String(PassDigester.digest("123")));
 				cv.put("money" , 1000);
 				db.insert(USER_TABLE, null, cv);
				Log.i("Example data", "inserted");
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		} 
 	}
 }
