 package com.aboveware.abovetracker;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.aboveware.abovetracker.TrackService.Mode;
 import com.aboveware.abovetracker.TrackService.TrackServiceListener;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteDoneException;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteStatement;
 import android.location.Location;
 import android.util.Log;
 
 public class TrackDbAdapter {
 
 	private static final String TAG = "TrackDbAdapter";
 
 	private DatabaseHelper mDbHelper;
 	private SQLiteDatabase mDb;
 
 	private static final int DATABASE_VERSION = 1;
 	public static final int NO_ID = -1;
 	public static final String ID = "_id";
 	private static final String DATABASE_NAME = "abovetrack";
 
 	// Master table TRACK
 	private static final String TABLE_TRACK_NAME = "track";
 	public static final String TRACK = "track";
 	public static final String DATE = "date";
 	private static final String TABLE_TRACK_CREATE = "CREATE TABLE "
 	    + TABLE_TRACK_NAME + " (" + ID + " INTEGER PRIMARY KEY, " + DATE
 	    + " TEXT DEFAULT CURRENT_TIMESTAMP, " + TRACK + " TEXT)";
 	private static final String INSERT_STATEMENT = "INSERT INTO "
 	    + TABLE_TRACK_NAME + " (" + TRACK + ")" + " values (?)";
 
 	private long trackId = NO_ID;
 
 	/**
 	 * @return the trackId
 	 */
 	public long getTrackId() {
 		return trackId;
 	}
 
 	private List<TrackDbAdapterListener> trackDbAdapterListeners = new ArrayList<TrackDbAdapterListener>();
 
 	/**
 	 * @param trackId
 	 *          the trackId to set
 	 */
 	public synchronized void setTrackId(long trackId) {
 		this.trackId = trackId;
 	}
 
 	// Record table TRACKRECORD
 	private static final String TABLE_TRACKRECORD_NAME = "trackrecord";
 	public final static String LAT = "latitude";
 	public final static String LON = "longitude";
 	public static final String ALTITUDE = "altitude";
 	public static final String SPEED = "speed";
 	public static final String BEARING = "bearing";
 	private static final String MASTER = "master";
 	private final static String TABLE_TRACKRECORD_CREATE = "CREATE TABLE "
 	    + TABLE_TRACKRECORD_NAME + " (" + ID + " INTEGER PRIMARY KEY, " + MASTER
 	    + " INTEGER, " + LAT + " REAL, " + LON + " REAL, " + ALTITUDE + " REAL, "
 	    + SPEED + " REAL, " + BEARING + " REAL)";
 
 	private final static String INSERT_RECORD_STATEMENT = "INSERT INTO "
 	    + TABLE_TRACKRECORD_NAME + " (" + MASTER + ", " + LAT + ", " + LON + ", "
 	    + ALTITUDE + ", " + SPEED + ", " + BEARING + ")"
 	    + " values (?, ?, ?, ?, ?, ?)";
 
 	private static final String SELECT_ALL_TRACKS_STATEMENT = "SELECT * FROM "
 	    + TABLE_TRACK_NAME;
 
 	private static final String SELECT_ALL_TRACKRECORDS_STATEMENT = "SELECT * FROM "
 	    + TABLE_TRACKRECORD_NAME
 	    + " INNER JOIN "
 	    + TABLE_TRACK_NAME
 	    + " ON "
 	    + TABLE_TRACKRECORD_NAME
 	    + "."
 	    + MASTER
 	    + " = "
 	    + TABLE_TRACK_NAME
 	    + "."
 	    + ID + " AND " + TABLE_TRACK_NAME + "." + DATE + " = ?";
 
 	private static final String SELECT_TRACKRECORDS_STATEMENT = "SELECT COUNT(*) FROM "
 	    + TABLE_TRACKRECORD_NAME + " WHERE " + MASTER + " = ?";
 
 	private Context mContext = null;
 
 	private static class DatabaseHelper extends SQLiteOpenHelper {
 
 		SQLiteStatement mInsertRecordStatement;
 		SQLiteStatement mInsertStatement;
 		SQLiteStatement mRecordCountStatement;
 
 		DatabaseHelper(Context context) {
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			db.execSQL(TABLE_TRACKRECORD_CREATE);
 			db.execSQL(TABLE_TRACK_CREATE);
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
 			    + newVersion + ", which will destroy all old data");
 			db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRACK_NAME);
 			db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRACKRECORD_NAME);
 			onCreate(db);
 		}
 
 		@Override
 		public void onOpen(SQLiteDatabase db) {
 			mInsertStatement = db.compileStatement(INSERT_STATEMENT);
 			mInsertRecordStatement = db.compileStatement(INSERT_RECORD_STATEMENT);
 			mRecordCountStatement = db
 			    .compileStatement(SELECT_TRACKRECORDS_STATEMENT);
 		}
 	}
 
 	/**
 	 * Constructor - takes the context to allow the database to be opened/created
 	 * 
 	 * @param ctx
 	 *          the Context within which to work
 	 */
 	public TrackDbAdapter(Context context) {
 		this.mContext = context;
 	}
 
 	/**
 	 * Open the tacks database. If it cannot be opened, try to create a new
 	 * instance of the database. If it cannot be created, throw an exception to
 	 * signal the failure
 	 * 
 	 * @return this (self reference, allowing this to be chained in an
 	 *         initialization call)
 	 * @throws SQLException
 	 *           if the database could be neither opened or created
 	 */
 	public TrackDbAdapter open(TrackDbAdapterListener listener) throws SQLException {
 		mDbHelper = new DatabaseHelper(mContext);
 		mDb = mDbHelper.getWritableDatabase();
 		trackDbAdapterListeners = new ArrayList<TrackDbAdapterListener>();
 		if (null != listener){
 			trackDbAdapterListeners.add(listener);
 		}
 		return this;
 	}
 
 	public TrackDbAdapter open() throws SQLException {
 		return this.open(null);
 	}
 
 	public void close(){
 		mDb.close();
 		trackDbAdapterListeners = new ArrayList<TrackDbAdapterListener>();
 	}
 	
 	private void onUpdate(){
 		for(TrackDbAdapterListener listener : trackDbAdapterListeners){
 			listener.Updated(mContext);
 		}
 	}
 	/**
 	 * We're starting a new tracking - add a row to the master table
 	 * 
 	 */
 	public void insertTrack(String track) {
 		mDbHelper.mInsertStatement.bindString(1, track);
 		setTrackId(mDbHelper.mInsertStatement.executeInsert());
 	}
 
 	/**
 	 * Insert a new record belonging to the current track
 	 * 
 	 * @param location
 	 *          - where we are, just now.
 	 */
 	public void insertRecord(Location location) {
 		mDbHelper.mInsertRecordStatement.bindLong(1, trackId);
 		mDbHelper.mInsertRecordStatement.bindDouble(2, location.getLatitude());
 		mDbHelper.mInsertRecordStatement.bindDouble(3, location.getLongitude());
 		if (location.hasAltitude()) {
 			mDbHelper.mInsertRecordStatement.bindDouble(4, location.getAltitude());
 		} else {
 			mDbHelper.mInsertRecordStatement.bindDouble(4, 0);
 		}
 		if (location.hasSpeed()) {
 			mDbHelper.mInsertRecordStatement.bindDouble(5, location.getSpeed());
 		} else {
 			mDbHelper.mInsertRecordStatement.bindDouble(5, 0);
 		}
 		if (location.hasBearing()) {
 			mDbHelper.mInsertRecordStatement.bindDouble(6, location.getBearing());
 		} else {
 			mDbHelper.mInsertRecordStatement.bindDouble(6, 0);
 		}
 		mDbHelper.mInsertRecordStatement.executeInsert();
 	}
 
 	/**
 	 * Delete the track and track records with the given date
 	 */
 	public void delete(String track) {
 		mDb.beginTransaction();
 		try {
 			Cursor cursor = selectTrackRecords(track);
 			if (null != cursor && cursor.moveToFirst()) {
 				int rowId = cursor.getInt(cursor.getColumnIndex(ID));
 				if (mDb.delete(TABLE_TRACKRECORD_NAME, MASTER + "=" + rowId, null) >= 0) {
 					if (mDb.delete(TABLE_TRACK_NAME, ID + "=" + rowId, null) >= 0) {
 						mDb.setTransactionSuccessful();
 					}
 				}
 			} else {
 				if (mDb.delete(TABLE_TRACK_NAME, DATE + "= ?", new String[] { track }) >= 0) {
 					mDb.setTransactionSuccessful();
 				}
 			}
 		} finally {
 			mDb.endTransaction();
 		}
 		onUpdate();
 	}
 
 	/**
 	 * Return a Cursor for the tracks.
 	 * 
 	 * @return Cursor over all tracks.
 	 */
 	public Cursor selectTracks() {
 		return mDb.rawQuery(SELECT_ALL_TRACKS_STATEMENT, new String[] {});
 	}
 
 	/**
 	 * Return a Cursor for the track records for the given date.
 	 * 
 	 * @return Cursor over all track records.
 	 */
 	public Cursor selectTrackRecords(String date) {
 		try {
 			return mDb.rawQuery(SELECT_ALL_TRACKRECORDS_STATEMENT,
 			    new String[] { date });
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	/**
 	 * Dump the track records for the track with the given date.
 	 * 
 	 */
 	public void export(Context context, String date) {
 		try {
 			File rootFile = context.getExternalFilesDir(null);
 			if (null == rootFile) {
 				return;
 			}
 			File file = new File(rootFile, "data.kml");
 			file.createNewFile();
 			FileWriter fileWriter = new FileWriter(file, false);
 			fileWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
 			fileWriter.write("<kml xmlns=\"http://www.opengis.net/kml/2.2\">");
 			fileWriter.write("<Document>");
 			fileWriter.write("<name>Paths</name>");
 			fileWriter.write("<Style id=\"yellowLineGreenPoly\">");
 			fileWriter.write("<LineStyle>");
 			fileWriter.write("<color>7f00ffff</color>");
 			fileWriter.write("<width>4</width> </LineStyle>");
 			fileWriter.write("<PolyStyle>");
 			fileWriter.write("<color>7f00ff00</color>");
 			fileWriter.write("</PolyStyle> </Style>");
 			fileWriter.write("<Placemark>");
 			fileWriter.write("<name>Absolute Extruded</name>");
 			fileWriter.write("<styleUrl>#yellowLineGreenPoly</styleUrl>");
 			fileWriter.write("<LineString>");
 			fileWriter.write("<extrude>1</extrude>");
 			fileWriter.write("<tessellate>1</tessellate>");
 			fileWriter.write("<altitudeMode>absolute</altitudeMode>");
 			fileWriter.write("<coordinates>");
 			Cursor cursor = selectTrackRecords(date);
 			if (null != cursor) {
 				int latIndex = cursor.getColumnIndex(LAT);
 				int lonIndex = cursor.getColumnIndex(LON);
 				cursor.moveToFirst();
 				while (!cursor.isAfterLast()) {
 					fileWriter.write(cursor.getString(lonIndex));
 					fileWriter.write(",");
 					fileWriter.write(cursor.getString(latIndex));
 					if (cursor.moveToNext())
 						fileWriter.write(",");
 				}
 				cursor.close();
 			}
 			fileWriter.write("</coordinates>");
 			fileWriter.write("</LineString>");
 			fileWriter.write("</Placemark>");
 			fileWriter.write("</Document>");
 			fileWriter.write("</kml>");
 			fileWriter.close();
 		} catch (IOException e) {
 			Log.d(TAG, e.getMessage());
 		}
 	}
 
 	/**
 	 * @param trackId
 	 *          - trackId to count
 	 * @return the number of records stored for the given trackId
 	 */
 	public long getCount(long trackId) {
 		mDbHelper.mRecordCountStatement.bindLong(1, trackId);
 		try {
 			return mDbHelper.mRecordCountStatement.simpleQueryForLong();
 		} catch (SQLiteDoneException doneException) {
 			return 0;
 		}
 	}
 
	public void rename(final String track, final String id) {
 		PromptDialog dlg = new PromptDialog(mContext, track, R.string.enter_name) {
 			@Override
 			public boolean onOkClicked(String input) {
 				if (input.length() > 0 && !track.equals(input)) {
 					TrackDbAdapter mdb = new TrackDbAdapter(getContext());
					mdb.open();
 					mdb.mDb.beginTransaction();
 					try {
 						ContentValues values = new ContentValues();
 						values.put(TRACK, input);
 						mdb.mDb.update(TABLE_TRACK_NAME, values, ID + " = ?",
 						    new String[] { id });
 						mdb.mDb.setTransactionSuccessful();
 					} finally {
 						mdb.mDb.endTransaction();
 						mdb.close();
 					}
					onUpdate();
 				}
 				return true;
 			}
 		};
 		dlg.show();
 	}
 }
