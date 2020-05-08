 package it.unibz.geospy.database;
 
 import java.util.ArrayList;
 
 import it.unibz.geospy.datamanager.IMEIProvider;
 import it.unibz.geospy.datamanager.LocationObject;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.location.Location;
 import android.util.Log;
 
 public class DBAdapter {
 
 	public static final String DATABASE_NAME = "geospydb";
 	public static final int DATABASE_VERSION = 2;
 	
 	private final static String TAG = "DBAdapter";
 	private DatabaseHelper mDbHelper;
 	private SQLiteDatabase mDb;
 	
 	private final Context mContext;
 	
 	/**
 	 * Constructor - takes the context to allow database to be created or 
 	 *   opened.
 	 *   
 	 *   @param context within which our DB should work
 	 */
 	public DBAdapter(Context context) {
 		this.mContext = context;
 	}
 	
 	/*
 	 * Method for opening DB. If DB cannot be created then SQLException will
 	 * be thrown.
 	 * 
 	 * @return this (self reference)
 	 * @throws SQLException if DB cannot be neither opened nor created
 	 */
 	public DBAdapter open() throws SQLException {
 		try {
 			mDbHelper = new DatabaseHelper(mContext);
 			mDb = mDbHelper.getWritableDatabase();
 		} catch (Exception e) {
 			// TODO: handle exception
 		}
 		
 		return this;
 	}
 	
 	/*
      * Releasing resources related to DB connection.
      */
 	public void close() {
 		mDbHelper.close();
 	}
 	
 	/**
 	 * We'll see what we can do with this static class.
 	 * In documentation it is written that SQLiteOpenHelper class usually is 
 	 * use to manage database creation and version management. 
 	 */
     private static class DatabaseHelper extends SQLiteOpenHelper {
 
         DatabaseHelper(Context context) {
             super(context, DATABASE_NAME, null, DATABASE_VERSION);
         }
 
         @Override
         /**
          * All the create table statements are executed in separate db.execSQL
          * statements.
          */
         public void onCreate(SQLiteDatabase db) {
             db.execSQL(CREATE_TABLE_LOCATION);
             // creation of other tables
         }
 
         @Override
         public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
             db.execSQL("DROP TABLE IF EXISTS " + LOCATION_TABLE_TITLE);
             // drop of other tables
             onCreate(db);
         }
     }
     
     /**************************************************************************
 	 * DEFINITIONS FOR LOCATION TABLE
 	 *************************************************************************/
 	public static final String LOCATION_TABLE_TITLE = "location";
 	public static final String LOCATION_ROWID = "rowid";
 	public static final String LOCATION_LATITUDE = "latitude";
 	public static final String LOCATION_LONGITUDE = "longitude";
 	public static final String LOCATION_TIMESTAMP = "timestamp";
 	public static final String LOCATION_IS_SYNC = "is_sync";
 	private static final String CREATE_TABLE_LOCATION =
 	        "create table " 
 	        + LOCATION_TABLE_TITLE 
 	        + " ( "
 	        + LOCATION_ROWID
 	        + " integer primary key autoincrement, "
 	        + LOCATION_IS_SYNC
 	        + " text not null, "
 	        + LOCATION_LATITUDE 
 	        + " text not null, "
 	        + LOCATION_LONGITUDE
 	        + " text not null, "
 	        + LOCATION_TIMESTAMP
 	        + " text not null); ";
 	
 	/**
 	 * Stores location of the phone in the DB.
 	 * @param pLatitude
 	 * @param pLongitude
 	 * @param pTimestamp
 	 */
 	public void storeLocation(long pLatitude,
 			long pLongitude,
 			long pTimestamp) {
 		this.open();
 		ContentValues initialValues = new ContentValues();
     	initialValues.put(LOCATION_LATITUDE, pLatitude);
     	initialValues.put(LOCATION_LONGITUDE, pLongitude);
     	initialValues.put(LOCATION_TIMESTAMP, pTimestamp);
     	initialValues.put(LOCATION_IS_SYNC, "0");
     	mDb.insert(LOCATION_TABLE_TITLE, 
     			null, 
     			initialValues);
     	this.close();
 	}
 	
 	/**
 	 * Fetches an ArrayList of not synced LocationObjects from DB.
 	 * @return
 	 */
 	public ArrayList<LocationObject> getListOfNotSyncedLocations() {
 		ArrayList<LocationObject> locations = new ArrayList<LocationObject>();
 		String anIMEI = IMEIProvider.getIMEI(mContext);
 		String[] columns = {LOCATION_LATITUDE, LOCATION_LONGITUDE, 
 				LOCATION_TIMESTAMP};
 		String[] selectionArgs = {"0"};
 		// querying
 		this.open();
 		Cursor aCursor = mDb.query(
 				LOCATION_TABLE_TITLE, 
     			columns,
     			LOCATION_IS_SYNC + " = ? ",
     			selectionArgs,
     			null, 
     			null, 
     			null);
 		// filling the ArrayList
 		if (!aCursor.moveToFirst()) {
     		aCursor.close();
         	this.close();
     		return locations;
     	}
     	do {
     		locations.add(new LocationObject(anIMEI, aCursor.getString(0), 
     				aCursor.getString(1), aCursor.getString(2)));
     	} while (aCursor.moveToNext());
     	aCursor.close();
     	this.close();
     	if (aCursor.isClosed()) {
     		Log.e("Dainius", "Cursor is closed");
     	}
     	markAllSynced();
     	return locations;
 	}
 	
 	/**
 	 * Marks all the rows that are in sync now. This method should be invoked
 	 * when all the unsynced data has been synced.
 	 */
 	private void markAllSynced() {
 		Log.e("DAINIUS", "markAllSynced");
 		ContentValues updateValues = new ContentValues();
 		String[] whereArgs = {"0"};
 		updateValues.put(LOCATION_IS_SYNC, "1");
 		this.open();
 		mDb.update(LOCATION_TABLE_TITLE,
 				updateValues, 
				LOCATION_IS_SYNC+ "=" + "0", 
				null);
 		this.close();
 	}
 }
