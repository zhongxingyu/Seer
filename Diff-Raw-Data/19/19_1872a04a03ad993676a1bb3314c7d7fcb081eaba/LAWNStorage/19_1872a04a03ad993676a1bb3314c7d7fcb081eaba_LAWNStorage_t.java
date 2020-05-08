 package edu.gatech.cs4261.LAWN;
 
 import java.util.Calendar;
 import java.util.TimeZone;
 
 import android.content.ContentProvider;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteDatabase.CursorFactory;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteQueryBuilder;
 import android.net.Uri;
 import android.text.format.DateFormat;
 import edu.gatech.cs4261.LAWN.Log;
 
 public class LAWNStorage extends ContentProvider {
 	/* constants*/
 	private static final String TAG = "LAWNStorage";
 	private static final String DATABASE_NAME = "LAWNStorage.db";
     private static final int DATABASE_VERSION = 1;
     private static final String DEVICES_TABLE_NAME = "devices";
     private static final String DETECTIONS_TABLE_NAME = "detections";
     private static final UriMatcher um;
     private static final String AUTHORITY = "edu.gatech.cs4261.LAWN.LAWNStorage";
     
     //define the content uris
     public static final Uri CONTENT_URI = 
     		Uri.parse("content://edu.gatech.cs4261.LAWN.LAWNStorage");
     public static final Uri CONTENT_URI_DEVICES = 
     		Uri.parse("content://" + AUTHORITY + "/" + DEVICES_TABLE_NAME);
     public static final Uri CONTENT_URI_DETECTIONS = 
     		Uri.parse("content://" + AUTHORITY + "/" + DETECTIONS_TABLE_NAME);
     
     //define the match constants
     private static final int DEFAULT = 0;
     
     //set up the special constants
     static {
     	um = new UriMatcher(UriMatcher.NO_MATCH);
     	
     	//set up query matches
     	um.addURI(AUTHORITY, "", DEFAULT);
     }
 
     /* this class helps open, create, and upgrade the database file*/
     private static class DatabaseHelper extends SQLiteOpenHelper {
     	/* constructor*/
 		public DatabaseHelper(Context context) {
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			// execute the SQL to create the devices table
 			db.execSQL("CREATE TABLE " + DEVICES_TABLE_NAME + " ("
                     + "uid INTEGER PRIMARY KEY,"
                     + "mac_addr TEXT,"
                     + "protocol_found TEXT"
                     + ");");
 			
 			// execute the SQL to create the detections table
 			db.execSQL("CREATE TABLE " + DETECTIONS_TABLE_NAME + " ("
                     + "uid INTEGER,"
                     + "accuracy INTEGER,"
                     + "latitude REAL,"
                     + "longitude REAL,"
                     + "time_logged TEXT,"
                     + "weight INTEGER, "
                     + "FOREIGN KEY(uid) REFERENCES " + DEVICES_TABLE_NAME + "(uid)"
                     + ");");
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                     + newVersion + ", which will destroy all old data");
             db.execSQL("DROP TABLE IF EXISTS " + DEVICES_TABLE_NAME);
             db.execSQL("DROP TABLE IF EXISTS " + DETECTIONS_TABLE_NAME);
             onCreate(db);
 		}
     	
     }
     
     //a database helper to make things easier
     private DatabaseHelper dbHelper;
     
 	@Override
 	public int delete(Uri arg0, String arg1, String[] arg2) {
 		// TODO Auto-generated method stub
 		Log.e(TAG, "THIS METHOD SHOULD NEVER BE CALLED");
 		return 0;
 	}
 
 	@Override
 	public String getType(Uri uri) {
 		// TODO Auto-generated method stub
 		Log.e(TAG, "THIS METHOD SHOULD NEVER BE CALLED");
 		return null;
 	}
 
 	@Override
 	public Uri insert(Uri uri, ContentValues initValues) {
 		/* validate the uri*/
 		if(um.match(uri) != DEFAULT) {
 			throw new IllegalArgumentException("Unknown URI " + uri);
 		}
 		
 		/* make sure the content values aren't null*/
 		ContentValues values;
 		if(initValues != null) {
 			values = new ContentValues(initValues);
 		} else {
 			values = new ContentValues();
 		}
 		
 		/* make sure all the values are there*/
 		if(values.containsKey("mac_addr") == false) {
 			throw new IllegalArgumentException("No MAC Address given");
 		}
 		if(values.containsKey("protocol_found") == false) {
 			throw new IllegalArgumentException("No Protocol given");
 		}
 		if(values.containsKey("accuracy") == false) {
 			values.put("accuracy", 5000);
 		}
 		if(values.containsKey("latitude") == false) {
 			values.put("latitude", 33.7782987);
 			values.remove("longitude");
 			values.put("longitude", -84.3988862);
 			values.remove("accuracy");
 			values.put("accuracy", 5000);
 		}
 		if(values.containsKey("longitude") == false) {
 			values.put("longitude", -84.3988862);
 			values.remove("latitude");
 			values.put("latitude", 33.7782987);
 			values.remove("accuracy");
 			values.put("accuracy", 5000);
 		}
 		if(values.containsKey("weight") == false) {
 			values.put("weight", 1);
 		}
 		
 		//get the current datetime and convert it into the proper format
 		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
 		String now = (String) DateFormat.format("yyyy/MM/dd HH:mm:ss", cal);
 		
		Log.d(TAG, "NOW: " + now);
		
 		//get the database
 		SQLiteDatabase db = dbHelper.getWritableDatabase();
 		
 		//split off the devices table values
 		ContentValues devValues = new ContentValues();
 		devValues.put("mac_addr", values.getAsString("mac_addr"));
 		devValues.put("protocol", values.getAsString("protocol_found"));
 		
 		/* check if a matching device is already in the database and
 		 * insert the device if it isn't*/
 		long devUID = db.insertWithOnConflict(DEVICES_TABLE_NAME, null, devValues, SQLiteDatabase.CONFLICT_IGNORE);
 		
 		//split off the detections table values
 		ContentValues detValues = new ContentValues();
 		detValues.put("accuracy", values.getAsInteger("accuracy"));
 		detValues.put("latitude", values.getAsDouble("latitude"));
 		detValues.put("longitude", values.getAsDouble("longitude"));
 		detValues.put("weight", values.getAsInteger("weight"));
 		detValues.put("time_logged", now);
 		detValues.put("uid", devUID);
 		
 		//insert the detection into the database
 		long detRowId = db.insertWithOnConflict(DETECTIONS_TABLE_NAME, null, detValues, SQLiteDatabase.CONFLICT_FAIL);
 		
 		//make sure insert was successful and return proper uri
 		if(detRowId > 0) {
 			Uri ret = ContentUris.withAppendedId(CONTENT_URI_DETECTIONS, detRowId);
             getContext().getContentResolver().notifyChange(ret, null);
             return ret;
 		}
 		
 		//only reaches this if something went wrong
 		throw new SQLException("Failed to insert data into " + uri);
 	}
 	
 	/*make the custom outside facing insert for Reid*/
 	public Uri insert(String mac, String protocol, int accuracy, 
 			double lat, double lon, int weight) {
 		//create the values class to send to the insert
 		ContentValues values = new ContentValues();
 		
 		//add the values to the values class
 		if(mac != null) {
 			values.put("mac_addr", mac);
 		}
 		if(protocol != null) {
 			values.put("protocol", protocol);
 		}
 		if(accuracy > 0) {
 			values.put("accuracy", accuracy);
 		}
 		if(lat > 0) {
 			values.put("latitude", lat);
 		}
 		if(lon > 0) {
 			values.put("longitude", lon);
 		}
 		if(weight >= 0) {
 			values.put("weight", weight);
 		}
 		
 		//call the internal insert method
 		Uri ret = insert(CONTENT_URI, values);
 		
 		return ret;
 	}
 
 	@Override
 	public boolean onCreate() {
 		//initialize the database helper
 		dbHelper = new DatabaseHelper(getContext());
 		return true;
 	}
 
 	@Override
 	public Cursor query(Uri uri, String[] projection, String selection,
 			String[] selectionArgs, String sortOrder) {
 		//make the query builder
 		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
 		
 		//set the table(s) to query on
 		qb.setTables(DEVICES_TABLE_NAME + " JOIN " + DETECTIONS_TABLE_NAME + 
 				"ON (" + DEVICES_TABLE_NAME + ".uid = " + DETECTIONS_TABLE_NAME + ".uid)");
 		
 		/* TODO: check for what query it is?
 		if(um.match(uri) == SPECIFIC_MESSAGE) {
 			
 		}*/
 		
 		//TODO: make the query
 		Log.e(TAG, "THIS METHOD SHOULD NEVER BE CALLED");
 		
 		return null;
 	}
 
 	@Override
 	public int update(Uri uri, ContentValues values, String selection,
 			String[] selectionArgs) {
 		// TODO Auto-generated method stub
 		Log.e(TAG, "THIS METHOD SHOULD NEVER BE CALLED");
 		return 0;
 	}
 
 }
