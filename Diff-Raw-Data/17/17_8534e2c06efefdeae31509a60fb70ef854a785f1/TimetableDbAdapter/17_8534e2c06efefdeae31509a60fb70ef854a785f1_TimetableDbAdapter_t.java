 package org.razmjenavjestina.vlakic;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.database.sqlite.SQLiteOpenHelper;
 
 
 public class TimetableDbAdapter {
 	private DataBaseHelper mDbHelper;
 	private SQLiteDatabase mDb;
 	private static String DB_NAME = "vlakic";
 	private static String STATIONS_TABLE = "Stations";
     
 	private final Context mCtx;
 
 	public class DataBaseHelper extends SQLiteOpenHelper {
 
 		// The Android's default system path of your application database.
 		private String DB_PATH = "/data/data/org.razmjenavjestina.vlakic/databases/";
 
 		private SQLiteDatabase myDataBase;
 
 		private final Context myContext;
 
 		/**
 		 * Constructor Takes and keeps a reference of the passed context in
 		 * order to access to the application assets and resources.
 		 * 
 		 * @param context
 		 */
 		public DataBaseHelper(Context context) {
 
 			super(context, DB_NAME, null, 1);
 			this.myContext = context;
 		}
 
 		/**
 		 * Creates a empty database on the system and rewrites it with your own
 		 * database.
 		 * */
 		public void createDataBase() throws IOException {
 
 			boolean dbExist = checkDataBase();
 
 			if (dbExist) {
 				// do nothing - database already exist
 			} else {
 
 				// By calling this method and empty database will be created
 				// into the default system path
 				// of your application so we are gonna be able to overwrite that
 				// database with our database.
 				this.getReadableDatabase();
 
 				try {
 
 					copyDataBase();
 
 				} catch (IOException e) {
 
 					throw new Error("Error copying database");
 
 				}
 			}
 
 		}
 
 		/**
 		 * Check if the database already exist to avoid re-copying the file each
 		 * time you open the application.
 		 * 
 		 * @return true if it exists, false if it doesn't
 		 */
 		private boolean checkDataBase() {
 
 			SQLiteDatabase checkDB = null;
 
 			try {
 				String myPath = DB_PATH + DB_NAME;
 				checkDB = SQLiteDatabase.openDatabase(myPath, null,
 						SQLiteDatabase.OPEN_READONLY);
 
 			} catch (SQLiteException e) {
 
 				// database does't exist yet.
 
 			}
 
 			if (checkDB != null) {
 				checkDB.close();
 			}
 
 			return checkDB != null ? true : false;
 		}
 
 		/**
 		 * Copies your database from your local assets-folder to the just
 		 * created empty database in the system folder, from where it can be
 		 * accessed and handled. This is done by transfering bytestream.
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
 			while ((length = myInput.read(buffer)) > 0) {
 				myOutput.write(buffer, 0, length);
 			}
 
 			// Close the streams
 			myOutput.flush();
 			myOutput.close();
 			myInput.close();
 		}
 
 		public void openDataBase() throws SQLException {
 			// Open the database
 			String myPath = DB_PATH + DB_NAME;
 			myDataBase = SQLiteDatabase.openDatabase(myPath, null,
 					SQLiteDatabase.OPEN_READONLY);
 		}
 
 		@Override
 		public synchronized void close() {
 			if (myDataBase != null)
 				myDataBase.close();
 			super.close();
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 
 		}
 
 		// Add your public helper methods to access and get content from the
 		// database.
 		// You could return cursors by doing "return myDataBase.query(....)" so
 		// it'd be easy
 		// to you to create adapters for your views.
 
 	}
 	
 	
 	   /**
      * Constructor - takes the context to allow the database to be
      * opened/created
      * 
      * @param ctx the Context within which to work
      */
     public TimetableDbAdapter(Context ctx) {
         this.mCtx = ctx;
     }
 
 	/**
 	 * Open the notes database. If it cannot be opened, try to create a new
 	 * instance of the database. If it cannot be created, throw an exception to
 	 * signal the failure
 	 * 
 	 * @return this (self reference, allowing this to be chained in an
 	 *         initialization call)
 	 * @throws SQLException
 	 *             if the database could be neither opened or created
 	 */
 	public TimetableDbAdapter open() throws SQLException {
 		mDbHelper = new DataBaseHelper(mCtx);
 
 		try {
 
 			mDbHelper.createDataBase();
 
 		} catch (IOException ioe) {
 
 			throw new Error("Unable to create database");
 
 		}
 
 		try {
 
 			mDbHelper.openDataBase();
 
 		} catch (SQLException sqle) {
 
 			throw sqle;
 
 		}
 		mDb = mDbHelper.getWritableDatabase();
 		//mDb.execSQL("create index if not exists stats_idx on StationMovables (statid);");
 		//mDb.execSQL("create index if not exists movables_idx on StationMovables (movableid);");
 		return this;
 	}
 
 	public void close() {
 		mDbHelper.close();
 	}
 
 
     /**
      * Return a Cursor over the list of all notes in the database
      * 
      * @return Cursor over all notes
      */
     public Cursor fetchAllStations() {
 
         return mDb.query(STATIONS_TABLE, new String[] {"_id", "name"}, null, null, null, null, null);
     }
     
     public Cursor fetchAllSelections() {
 
        return mDb.rawQuery("select _id, name, fromstation,tostation, count FROM ToFromSelections order by count desc",null);
     }
     
     
     public Cursor searchStations(String search) {
 
         return mDb.query(STATIONS_TABLE, new String[] {"_id", "name"}, "name LIKE '" + search.toUpperCase() + "%'", null, null, null, null);        
     }
     
     public Cursor getStationTimetable(String stationid) {
 
     	String sql = "select sm.going,s.name,sm.extratxt from stationmovables as sm, " +
    			"stations as s where sm.statid='"+stationid+"' and s._id=sm.tostation";
 //    	System.out.println(sql);
     	return mDb.rawQuery(sql,null);
  
     }
     
     public boolean hasSelections() {
    	String sql = "select count(*) FROM ToFromSelections";
     	Cursor c = mDb.rawQuery(sql, null);
     	c.moveToFirst();
     	if (Integer.parseInt(c.getString(0)) > 0) return true;
     	return false;
     }
     
     public void updateOrInsertLastToFrom(String toStation, String fromStation) {
     	
     	String sql = "select count(*) FROM ToFromSelections WHERE fromstation='" + 
    				fromStation + "' and tostation='" + toStation +"'";
     //	System.out.println(sql);
     	Cursor c = mDb.rawQuery(sql, null);
     	c.moveToFirst();
     	if (Integer.parseInt(c.getString(0)) > 0) {
     		//System.out.println("Update Selections");
     		mDb.execSQL("UPDATE ToFromSelections SET count=count+1 WHERE fromstation='" + 
    				fromStation + "' and tostation='" + toStation + "'");
     	} else {
     		String name = getStationName(fromStation) + " - " + getStationName(toStation);
     		//System.out.println("Insert Selections");
     		mDb.execSQL("INSERT INTO ToFromSelections VALUES(NULL,'0','" + 
     				fromStation + "','" + toStation + "','"+name
    				+"','1')");
     	}
     	c.close();
     }
     public ArrayList getStationToFromTimetable(String toStation, String fromStation) {
     	String econd = ""; // condition for time stuff
     	String sql = "SELECT distinct movableid,going,coming,fromstation,tostation,extratxt FROM StationMovables WHERE statid='"+ fromStation+ "' " + econd + " order by going";
     	//System.out.println(sql);
     	Cursor c = mDb.rawQuery(sql, null);
     	ArrayList retArr = new ArrayList();
     	c.moveToFirst();
     	  int i =0;
           while (c.isAfterLast() == false)         
           {
           	String movable = c.getString(0);
           	String going = c.getString(1);
           	String coming = c.getString(2);
           	String fromstation = c.getString(3);
           	String tostation = c.getString(4);
           	String extratxt = c.getString(5);
           	if(going.length() == 0)  {
           		c.moveToNext();
           		continue;          		
           	}
           	String cond = "going >'" + going + "' or coming > '" + going+"'";
           	sql = "SELECT coming,going FROM StationMovables WHERE  movableid='" + movable +"' AND statid='" + 
           			toStation + "' and (" + cond+ ")";
           //	System.out.println(sql);
           	Cursor c2 = mDb.rawQuery(sql, null);
           	if(c2.getCount() > 0) {
           		// TODO wrap time fix
           		c2.moveToFirst();
           		coming = c2.getString(0);
           		if (false) {
           			
           		} else {
           	
           			//System.out.println(sql);
 
                     	// we don't actually need 
                     	String tostr = "";
                     	String fromstr = "";
                     	if (fromstation.length() > 0) {
                     		fromstr = getStationName(fromstation);
                     	}
                     	if (tostation.length() > 0) {
                     		tostr = getStationName(tostation);
                     	}
                     	if (!tostr.equals("")) {
                     	
                     	ArrayList in = new ArrayList();
                     	in.add(going);
                     	in.add(tostr);
                     	in.add(extratxt);
                     	
                     	retArr.add(in);
                     	}
                    
           			
           		}
           	}
           	c2.close();
           	
             c.moveToNext();
             i++;
               
          
           }
           c.close();
     	//String sql = "select sm.going,s.name,sm.extratxt from stationmovables as sm, " +
     		//	"stations as s where sm.statid='"+stationid+"' and s._id=sm.tostation;";
 //    	System.out.println(sql);
     //	return mDb.rawQuery(sql,null);
     	return retArr;
     }
 
     
     
     public String getStationName(String stationid) {
     	if (Integer.parseInt(stationid) < 1) return "";
     	Cursor c =  mDb.rawQuery("SELECT name FROM Stations where _id='"+stationid+"'",null);
     	c.moveToFirst();
     	String ret = c.getString(0);
     	c.close();
     	return ret;
     	
     }
     
         
 
 
 }
