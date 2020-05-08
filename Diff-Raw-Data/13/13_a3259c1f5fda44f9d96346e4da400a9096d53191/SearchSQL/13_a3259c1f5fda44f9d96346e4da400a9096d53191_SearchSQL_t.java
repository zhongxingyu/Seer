 package com.chalmers.schmaps;
 
 import java.io.*;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteDatabase.CursorFactory;
 import android.database.sqlite.SQLiteException;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class SearchSQL {
 
 	public static final String KEY_ROWID = "_id"; //raden i databasen
 	public static final String KEY_ROOM = "room_name";
 	public static final String KEY_LAT = "latitude";
 	public static final String KEY_LONG = "longitude";
 	public static final String KEY_STREET = "street_name";
 	public static final String KEY_LEVEL = "level";
 	
 	private static String TAG = "SearchSQL";
 	private static final String DATABASE_NAME = "SchmapsDB"; //namnet på vår databas
 	private static final String DATABASE_TABLE = "Salar"; //namnet på vår tabell (kan ha flera tabeller)
 	private static String DATABASE_PATH = "";
 	private static final int DATABASE_VERSION = 1;
 	
 	private MySQLiteOpenHelper ourHelper;
 	private final Context ourContext;
 	private SQLiteDatabase ourDatabase;
 	
 	public SearchSQL(Context c){
 		ourContext = c;	
 	}
 	
 	/**
 	 * Opens the database in readonly
 	 * @return
 	 */
 	public SearchSQL openRead(){
 		ourHelper = new MySQLiteOpenHelper(ourContext);
 		
 		
 		ourHelper.openDataBase();
 		ourHelper.close();
 		ourDatabase = ourHelper.getReadableDatabase();
 		return this;
 	}
 	
 	public void createDatabase() {
 		ourHelper = new MySQLiteOpenHelper(ourContext);
 		try
 		{
 			ourHelper.createDataBase();
 		}catch(Exception e){}
 		
 	}
 
 
 	public void close(){ 
 		ourHelper.close();
 	}
 	
 	/********************
 	 * 
 	 * @param query
 	 * @return
 	 * 
 	 *******************/
 	public boolean exists(String query)
 	{
 		String [] columns = new String []{KEY_ROWID, KEY_ROOM, KEY_LAT, KEY_LONG, KEY_STREET, KEY_LEVEL};
 		Cursor cursor = ourDatabase.query(DATABASE_TABLE, columns, KEY_ROOM + " LIKE ?" , new String [] { "%" + query + "%"}, null, null, null);
 		if(cursor.getCount()<=0)
 			return false;
 		else 
 			return true;
 
 	}
 
 	/***************
 	 * 
 	 * @param query
 	 * @return
 	 * 
 	 *****************/
 	public int getLat(String query) {
 		String [] columns = new String []{ KEY_ROWID, KEY_ROOM, KEY_LAT, KEY_LONG, KEY_STREET, KEY_LEVEL};
 		Cursor cursor = ourDatabase.query(DATABASE_TABLE, columns, KEY_ROOM + " LIKE ?" , new String [] { "%" + query + "%"}, null, null, null);
 		
 		if(cursor != null){
 			Log.e(TAG, "inside if Lat");
 			cursor.moveToFirst();
 			int lat = cursor.getInt(2);
 			if (cursor != null && !cursor.isClosed()) {
 	            cursor.close();
 			}
 			return lat;
 		}
 		return 0;
 	
 	}
 
 	/*****************
 	 * 
 	 * @param query
 	 * @return
 	 *
 	 *****************/
 	public int getLong(String query) {
 		String [] columns = new String []{KEY_ROWID, KEY_ROOM, KEY_LAT, KEY_LONG, KEY_STREET, KEY_LEVEL };
 		Cursor cursor = ourDatabase.query(DATABASE_TABLE, columns, KEY_ROOM + " LIKE ?" , new String [] { "%" + query + "%"}, null, null, null);
 		
 		if(cursor != null){
 			Log.e(TAG, "inside if long");
 			cursor.moveToFirst();
 			int lon = cursor.getInt(3);
 			if (cursor != null && !cursor.isClosed()) {
 	            cursor.close();
 			}
 			return lon;
 		}
 		return 0;
 	}
 
 	/************************
 	 * 
 	 * @param query
 	 * @return
 	 * 
 	 *************************/
 	public String getAddress(String query) {
 		String [] columns = new String []{KEY_ROWID, KEY_ROOM, KEY_LAT, KEY_LONG, KEY_STREET, KEY_LEVEL };
 		Cursor cursor = ourDatabase.query(DATABASE_TABLE, columns, KEY_ROOM + " LIKE ?" , new String [] { "%" + query + "%"}, null, null, null);
 		
 		if(cursor != null){
 			Log.e(TAG, "inside if address");
 			cursor.moveToFirst();
 			String address = cursor.getString(4);
 			if (cursor != null && !cursor.isClosed()) {
 	            cursor.close();
 			}
 			return address;
 		}
 		return null;
 	}
 
 	/*********************
 	 * 
 	 * @param query
 	 * @return
 	 * 
 	 *********************/
 	public String getLevel(String query) {
 		String [] columns = new String []{KEY_ROWID, KEY_ROOM, KEY_LAT, KEY_LONG, KEY_STREET, KEY_LEVEL };
 		Cursor cursor = ourDatabase.query(DATABASE_TABLE, columns, KEY_ROOM + " LIKE ?" , new String [] { "%" + query + "%"}, null, null, null);
 		
 		if(cursor != null){
 			Log.e(TAG, "inside if level");
 			cursor.moveToFirst();
 			String level = cursor.getString(5);
 			if (cursor != null && !cursor.isClosed()) {
 	            cursor.close();
 			}
 			return level;
 		}
 		return null;
 	}
 	
 	private static class MySQLiteOpenHelper extends SQLiteOpenHelper{
 
 		private final Context myContext;
 		SQLiteDatabase internDatabase;
 		
 		public MySQLiteOpenHelper(Context context) {
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 			DATABASE_PATH = "/data/data/" + context.getPackageName() + "/databases/";
 			this.myContext = context;
 			// TODO Auto-generated constructor stub
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			// TODO Auto-generated method stub
 
 		}
 		
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			// TODO Auto-generated method stub
 			db.execSQL("DROP TABLE IF EXIST " + DATABASE_TABLE);
 			
 		}
 		
 	    @Override 
 	    public synchronized void close()  
 	    { 
 	        if(internDatabase != null) 
 	            internDatabase.close(); 
 	        super.close(); 
 	    }
 		
 	    /***************************
 	     * Opens the database.
 	     * @throws SQLException
 	     * 
 	     ***************************/
 	    public void openDataBase() throws SQLException{
 	    	 
 	    	//Open the database
 	        String myPath = DATABASE_PATH + DATABASE_NAME;
 	    	internDatabase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
 	    	
 	    }
 		
 	    /*************************************************************************
 	     * Creates the database if none exists by copying an database from assets 
 	     * to /data/data/Packagename/databases
 	     * @throws IOException
 	     * 
 	     **************************************************************************/
 		public void createDataBase() throws IOException{
 			 
 	    	boolean dbExist = checkDataBase();
 	 
 	    	 if(!dbExist) 
 	    	    { 
 	    		 	//Creates an empty database to copy to
 	    	        this.getReadableDatabase(); 
 	    	        this.close();
 	    	        //Track the progress in LogCat
 	    	        Log.e(TAG, "empty database created"); 
 	    	        try  
 	    	        { 
 	    	            //Copy the database from assets 
 	    	            copyDataBase(); 
 	    	            //Track the progress in LogCat
 	    	            Log.e(TAG, "createDatabase: database created"); 
 	    	        }  
 	    	        catch (IOException dbIOException)  
 	    	        { 
 	    	            throw new Error("Error Copying Database"); 
 	    	        } 
 	    	    } 
 	 
 	    }
 		
 		/*******************************
 		 * Checks if the database exists
 		 * @return true if exists else false
 		 * 
 		 *****************************/
 		private boolean checkDataBase() 
 		{ 
 			File dataBaseFile = new File(DATABASE_PATH + DATABASE_NAME); 
  
 	        return dataBaseFile.exists(); 
 		} 
 		
 		/************************
 		 * Copying the content from database in assets to an empty database in
 		 * data/data/Packagename/databases
 		 * @throws IOException
 		 * 
 		 ************************/
 		private void copyDataBase() throws IOException{
 
 	    	//Open database in assets as the input stream
 	    	InputStream myInput = myContext.getAssets().open(DATABASE_NAME);
 	    	//To check progress in LogCat
 	    	Log.e(TAG, "inside copyDataBase");
 	    	// Path to the just created empty database
 	    	String outFileName = DATABASE_PATH + DATABASE_NAME;
 	 
 	    	//Open the empty database as the output stream
 	    	OutputStream myOutput = new FileOutputStream(outFileName);
 	 
 	    	//transfer bytes from the inputfile to the outputfile
 	    	byte[] buffer = new byte[1024];
 	    	int length;
 	    	while ((length = myInput.read(buffer))>0){
 	    		myOutput.write(buffer, 0, length);
 	    	}
 	 
 	    	//Close the streams
 	    	myOutput.flush();
 	    	myOutput.close();
 	    	myInput.close();
 	 
 	    }
 	}
 	
 }
