 package com.laskowski.simplegpstracker.db;
 
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.location.Location;
 import android.util.Log;
 
 public class DBUtils {
 
 	public static final String KEY_TRIP_ID = "id";
 	public static final String KEY_DESC = "description";
 	public static final String KEY_TIME = "time";
 	public static final String KEY_AVG_SPEED = "average_speed";
 	public static final String KEY_DIST = "distance";
 
 	public static final String KEY_LNG = "longtitude";
 	public static final String KEY_LAT = "lattitude";
 	public static final String KEY_COORD_ID = "trip_id";
 
 	private static final String TAG = "DBUtils";
 	private DatabaseHelper mDbHelper;
 	private SQLiteDatabase mDb;
 
 	private static final String TRIP_TABLE = "trip";
 	private static final String TRIP_COORDS_TABLE = "trip_coordinates";
 
 	/**
 	 * Database creation sql statement
 	 */
 	private static final String CREATE_TRIP_TABLE = "create table "
 			+ TRIP_TABLE
 			+ "  (id long primary key  not null, description text, time text not null, "
 			+ "average_speed text not null, distance text not null);";
 	private static final String CREATE_TRIP_CORDS_TABLE = "create table "
 			+ TRIP_COORDS_TABLE
 			+ " (trip_id long references trip(id), lattitude double not null, longtitude double not null );";
	private static final String FROM_CLAUSE = "trip join trip_coordinates on (trip.id = trip_coordinates.trip_id)";
 
 	private static final String DATABASE_NAME = "gpstrack.db";
 
 	private static final int DATABASE_VERSION = 2;
 
 	private final Context mCtx;
 
 	private static DBUtils instance;
 
 	private static class DatabaseHelper extends SQLiteOpenHelper {
 
 		DatabaseHelper(Context context) {
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 
 			db.execSQL(CREATE_TRIP_TABLE);
 			db.execSQL(CREATE_TRIP_CORDS_TABLE);
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
 					+ newVersion + ", which will destroy all old data");
 			db.execSQL("DROP TABLE IF EXISTS " + TRIP_TABLE);
 			db.execSQL("DROP TABLE IF EXISTS " + TRIP_COORDS_TABLE);
 			onCreate(db);
 		}
 	}
 
 	public static DBUtils getInstance(Context ctx) {
 		if (instance == null) {
 			instance = new DBUtils(ctx);
 			instance.open();
 		}
 
 		if (instance.mDb != null && !instance.mDb.isOpen()) {
 			instance.open();
 		}
 		return instance;
 	}
 
 	/**
 	 * Constructor - takes the context to allow the database to be
 	 * opened/created, private for singleton pattern usage
 	 * 
 	 * @param ctx
 	 *            the Context within which to work
 	 */
 	private DBUtils(Context ctx) {
 		this.mCtx = ctx;
 	}
 
 	/**
 	 * Open the database. If it cannot be opened, try to create a new instance
 	 * of the database. If it cannot be created, throw an exception to signal
 	 * the failure
 	 * 
 	 * @return this (self reference, allowing this to be chained in an
 	 *         initialization call)
 	 * @throws SQLException
 	 *             if the database could be neither opened or created
 	 */
 	private DBUtils open() throws SQLException {
 		mDbHelper = new DatabaseHelper(mCtx);
 		mDb = mDbHelper.getWritableDatabase();
 		return this;
 	}
 
 	public void close() {
 		mDbHelper.close();
 	}
 
 	/**
 	 * Create a new entry using the data provided. If the entry is successfully
 	 * created return the new rowId for that note, otherwise return a -1 to
 	 * indicate failure.
 	 * 
 	 */
 	public void createTripEntry(String desc, String time, String avgSpeed,
 			String distance, List<Location> coords) {
 
 		ContentValues initialValues = new ContentValues();
 		long id = System.currentTimeMillis();
 
 		if (coords != null && coords.size() != 0) {
 			for (Location ll : coords) {
 				initialValues.put(KEY_LAT, ll.getLatitude());
 				initialValues.put(KEY_LNG, ll.getLongitude());
 				initialValues.put(KEY_COORD_ID, id);
 				mDb.insert(TRIP_COORDS_TABLE, null, initialValues);
 				initialValues.clear();
 			}
 
 		} else {
 			Log.w(TAG, "empty coords collection passed");
 			return;
 		}
 
 		initialValues.clear();
 
 		initialValues.put(KEY_TRIP_ID, id);
 		initialValues.put(KEY_DESC, desc);
 		initialValues.put(KEY_TIME, time);
 		initialValues.put(KEY_AVG_SPEED, avgSpeed);
 		initialValues.put(KEY_DIST, distance);
 
 		mDb.insert(TRIP_TABLE, null, initialValues);
 	}
 
 	/**
 	 * Delete the entry with the given id
 	 * 
 	 * @param entryId
 	 *            id of trip to delete
 	 * @return true if deleted, false otherwise
 	 */
 	public boolean deleteTripEntry(long entryId) {
 
 		Cursor cur = fetchTripEntry(entryId);
 
 		if (cur != null && cur.getCount() == 1) {
 			cur = fetchCoordsForTrip(entryId);
 			if (cur != null && cur.getCount() != 0) {
 				mDb.delete(TRIP_COORDS_TABLE, KEY_COORD_ID + "= '" + entryId
 						+ "'", null);
 
 				return mDb.delete(TRIP_TABLE, KEY_TRIP_ID + "= '" + entryId
 						+ "'", null) > 0;
 			}
 
 		}
 		return false;
 
 	}
 
 	/**
 	 * Return a Cursor over the list of all trip coordinates
 	 * 
 	 * @param tripId
 	 * 
 	 * @return Cursor o over the list of all trip coordinates
 	 */
 	public Cursor fetchCoordsForTrip(long tripId) {
 
 		Cursor cur = mDb.query(TRIP_COORDS_TABLE, new String[] { KEY_COORD_ID,
 				KEY_LAT, KEY_LNG }, null, null, null, null, null, null);
 
 		return cur;
 	}
 
 	/**
 	 * Return a Cursor positioned at the entry that matches the given rowId
 	 * 
 	 * @param id
 	 *            timestamp of the trip to retrieve
 	 * @return Cursor positioned to matching entry, if found
 	 * @throws SQLException
 	 *             if entry could not be found/retrieved
 	 */
 	public Cursor fetchTripEntry(long id) throws SQLException {
 
 		Cursor cur = mDb.query(TRIP_TABLE, new String[] { KEY_TRIP_ID,
 				KEY_DESC, KEY_TIME, KEY_AVG_SPEED, KEY_DIST }, KEY_TRIP_ID
 				+ "= " + id, null, null, null, null, null);
 
 		if (cur != null) {
 			cur.moveToFirst();
 		}
 		return cur;
 
 	}
 
 	public Cursor fetchAllTrips() {
 		return mDb.query(TRIP_TABLE, new String[] { KEY_TRIP_ID, KEY_DESC,
 				KEY_TIME, KEY_AVG_SPEED, KEY_DIST }, null, null, null, null,
 				null);
 	}
 
 	/**
 	 * Update the entry using the details provided. The entry to be updated is
 	 * specified using the rowId, and it is altered to use the values passed in
 	 * 
 	 * @param entryName
 	 *            id of entry to update
 	 * @param user
 	 *            value to set entry user to
 	 * @param passwd
 	 *            value to set entry password to
 	 * @return true if the entry was successfully updated, false otherwise
 	 */
 	public boolean updateEntry(String entryName, String user, String passwd) {
 		ContentValues args = new ContentValues();
 		/*
 		 * args.put(KEY_USER, user); args.put(KEY_PASSWD, passwd);
 		 */
 
 		return mDb.update(TRIP_TABLE, args, KEY_TRIP_ID + "= '" + entryName
 				+ "'", null) > 0;
 	}
 }
