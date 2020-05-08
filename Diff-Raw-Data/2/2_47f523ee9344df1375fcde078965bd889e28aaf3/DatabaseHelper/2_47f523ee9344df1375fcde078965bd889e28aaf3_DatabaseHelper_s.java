 package com.doLast.doGRT.database;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import android.content.Context;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 /** This class helps create and update the database
  * 
  * @author Andreas
  *
  */
 public class DatabaseHelper extends SQLiteOpenHelper {	
     // The Database from GRT database
     private static String DB_PATH = "/data/data/com.doLast.doGRT/databases/"; 
     private static String DB_NAME = "GRT_GTFS.sqlite";
    private static final int DB_VERSION = 3;
     
     private SQLiteDatabase myDataBase;
     private final Context myContext;
 	    
 	public DatabaseHelper(Context context) {
 		super(context, DB_NAME, null, DB_VERSION);
 		this.myContext = context;
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 	    if (newVersion > oldVersion)
 	        Log.v("Database Upgrade", "Database version higher than old.");
 	    myContext.deleteDatabase(DB_NAME);
 	}
 	
     @Override
 	public synchronized void close() {
     	if(myDataBase != null)
     		myDataBase.close();
     	super.close();
 	}
 	
     /**
      * Check if the database already exist to avoid re-copying the file each time you open the application.
      * return true if it exists, false if it doesn't
      */
     private boolean checkDataBase(){
 /*    	SQLiteDatabase checkDB = null;
  
 	   	try{
     		String myPath = DB_PATH + DB_NAME;
     		checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
     		// Another way of checking whether the database exist
 
     	}catch(SQLiteException e){
     		// database does't exist yet.
     		System.out.println(e.getMessage());
     	}
     	
     	if(checkDB != null){
     		checkDB.close();
     	}
     	
     	return checkDB != null ? true : false;*/
     	
     	// Another way of checking if the database exists
 		File db_file = new File(DB_PATH + DB_NAME);
 		return db_file.exists();
     }
     
     /**
      * Copies your database from your local assets-folder to the just created empty database in the
      * system folder, from where it can be accessed and handled.
      * This is done by transferring bytestream.
      * */
     private void copyDataBase() throws IOException {
     	// Open your local db as the input stream
     	InputStream myInput = myContext.getAssets().open(DB_NAME);
  
     	// Path to the just created empty db
     	String outFileName = DB_PATH + DB_NAME;
  
     	// Open the empty db as the output stream
     	OutputStream myOutput = new FileOutputStream(outFileName);
  
     	// transfer bytes from the inputfile to the outputfile
     	byte[] buffer = new byte[1024];
     	int length;
     	while ((length = myInput.read(buffer))>0){
     		myOutput.write(buffer, 0, length);
     	}
  
     	// Close the streams
     	myOutput.flush();
     	myOutput.close();
     	myInput.close();
     }
     
 	/**
      * Creates a empty database on the system and rewrites it with your own database.
      */
     public void createDataBase() throws IOException {
     	boolean dbExist = checkDataBase();
         if (dbExist) {
             Log.v("DB Exists", "db exists");
             // By calling this method here onUpgrade will be called on a
             // writeable database, but only if the version number has been
             // bumped
             this.getWritableDatabase();
         } 
 
         
         if (!dbExist) {
     		/** 
     		 * Database doesn't exist
     		 * By calling this method and empty database will be created into the default system path
     		 * of your application so we are gonna be able to overwrite that database with our database.
     		 */
         	this.getReadableDatabase();
  
         	try {
     			copyDataBase();
     		} catch (IOException e) {
         		throw new Error("Error copying database"); 
         	}
     	}
     }
    
     public void openDataBase() throws SQLException {
     	// Open the database
         String myPath = DB_PATH + DB_NAME;
         // The NO_LOCALIZED_COLLATORS flag prevent the android_metadata table error
     	myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS|SQLiteDatabase.OPEN_READONLY);
     }
 }
