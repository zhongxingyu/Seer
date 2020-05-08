 package com.chalmers.schmaps;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteDatabase.CursorFactory;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class SearchSQL {
 
 	public static final String KEY_ROWID = "_id"; //raden i databasen
 	public static final String KEY_ROOM = "room_name";
 	public static final String KEY_LAT = "latitude";
 	public static final String KEY_LONG = "longitude";
 	public static final String KEY_STREET = "street_name";
 	public static final String KEY_LEVEL = "level";
 	
 	private static final String DATABASE_NAME = "SchmapsDB"; //namnet på vår databas
 	private static final String DATABASE_TABLE = "SchmapsTable"; //namnet på vår tabell (kan ha flera tabeller)
 	private static final int DATABASE_VERSION = 1;
 	
 	private MySQLiteOpenHelper ourHelper;
 	private final Context ourContext;
 	private SQLiteDatabase ourDatabase;
 	
 	public SearchSQL(Context c){
 		ourContext = c;	
 	}
 	
 	public SearchSQL openWrite(){
 		ourHelper = new MySQLiteOpenHelper(ourContext);
 		ourDatabase = ourHelper.getWritableDatabase();
 		return this;
 	}
 	
 	public SearchSQL openRead(){
 		ourHelper = new MySQLiteOpenHelper(ourContext);
 		ourDatabase = ourHelper.getReadableDatabase();
 		return this;
 	}
 	
 	public void createEntry(){ //ska lägga in med den här metoden
 		ContentValues cv = new ContentValues();
 		
 		// ett exempel på hur man lägger in
 		cv.put(KEY_ROOM, "runan");
 		cv.put(KEY_LAT, 57689262);
 		cv.put(KEY_LONG, 11973805);
 		cv.put(KEY_STREET, "Chalmersplatsen 1");
 		cv.put(KEY_LEVEL, "Plan 2");
 		ourDatabase.insert(DATABASE_TABLE, null, cv);
 		
 		cv.put(KEY_ROOM, "hej");
 		cv.put(KEY_LAT, 58070517);
 		cv.put(KEY_LONG, 11760864);
 		cv.put(KEY_STREET, "Gata 2");
 		cv.put(KEY_LEVEL, "Plan 4");
 		ourDatabase.insert(DATABASE_TABLE, null, cv);
 		
 		// tips på hur man kan mata in från fil http://stackoverflow.com/questions/8801423/how-can-i-insert-data-in-an-sqlite-table-from-a-text-file-in-android
 		
 	}
 
 	public void close(){ 
 		ourHelper.close();
 	}
 	
 	private static class MySQLiteOpenHelper extends SQLiteOpenHelper{
 
 		public MySQLiteOpenHelper(Context context) {
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 			// TODO Auto-generated constructor stub
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			// TODO Auto-generated method stub
 			db.execSQL("CREATE TABLE " + DATABASE_TABLE + " (" +
 					KEY_ROWID + " INTEGER PRIMARY KEY, " +
 					KEY_ROOM + " TEXT NOT NULL, " +
 					KEY_LAT + " INTEGER, " + 
 					KEY_LONG + " INTEGER, " +
 					KEY_STREET + " TEXT NOT NULL, " +
 					KEY_LEVEL + " TEXT NOT NULL);"		
 			);				
 		}
 		
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			// TODO Auto-generated method stub
 			db.execSQL("DROP TABLE IF EXIST " + DATABASE_TABLE);
 			onCreate(db);
 			
 		}
 		
 	}
 
 	public int getLat(String query) {
 		// TODO Auto-generated method stub
 		String [] columns = new String []{ KEY_ROWID, KEY_ROOM, KEY_LAT, KEY_LONG, KEY_STREET, KEY_LEVEL};
 		Cursor cursor = ourDatabase.query(DATABASE_TABLE, columns, KEY_ROOM + " LIKE ?" , new String [] { "%" + query + "%"}, null, null, null);
 		
 		if(cursor != null){
 			cursor.moveToFirst();
 			int lat = cursor.getInt(2);
 			if (cursor != null && !cursor.isClosed()) {
 	            cursor.close();
 			}
 			return lat;
 		}
 		return 0;
 	
 	}
 
 	public int getLong(String query) {
 		// TODO Auto-generated method stub
 		String [] columns = new String []{KEY_ROWID, KEY_ROOM, KEY_LAT, KEY_LONG, KEY_STREET, KEY_LEVEL };
 		Cursor cursor = ourDatabase.query(DATABASE_TABLE, columns, KEY_ROOM + " LIKE ?" , new String [] { "%" + query + "%"}, null, null, null);
 		
 		if(cursor != null){
 			cursor.moveToFirst();
 			int lon = cursor.getInt(3);
 			if (cursor != null && !cursor.isClosed()) {
 	            cursor.close();
 			}
 			return lon;
 		}
 		return 0;
 	}
 
 	public String getAddress(String query) {
 		// TODO Auto-generated method stub
 		String [] columns = new String []{KEY_ROWID, KEY_ROOM, KEY_LAT, KEY_LONG, KEY_STREET, KEY_LEVEL };
 		Cursor cursor = ourDatabase.query(DATABASE_TABLE, columns, KEY_ROOM + " LIKE ?" , new String [] { "%" + query + "%"}, null, null, null);
 		
 		if(cursor != null){
 			cursor.moveToFirst();
 			String address = cursor.getString(4);
 			if (cursor != null && !cursor.isClosed()) {
 	            cursor.close();
 			}
 			return address;
 		}
 		return null;
 	}
 
 	public String getLevel(String query) {
 		// TODO Auto-generated method stub
 		String [] columns = new String []{KEY_ROWID, KEY_ROOM, KEY_LAT, KEY_LONG, KEY_STREET, KEY_LEVEL };
 		Cursor cursor = ourDatabase.query(DATABASE_TABLE, columns, KEY_ROOM + " LIKE ?" , new String [] { "%" + query + "%"}, null, null, null);
 		
 		if(cursor != null){
 			cursor.moveToFirst();
 			String level = cursor.getString(5);
 			if (cursor != null && !cursor.isClosed()) {
 	            cursor.close();
 			}
 			return level;
 		}
 		return null;
 	}
 	
 }
