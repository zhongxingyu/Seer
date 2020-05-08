 package com.barefoot.pocketshake.storage;
 
 import java.util.ArrayList;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteCursor;
 import android.database.sqlite.SQLiteCursorDriver;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteQuery;
 import android.util.Log;
 
 import com.barefoot.pocketshake.R;
 import com.barefoot.pocketshake.data.EarthQuake;
 import com.barefoot.pocketshake.exceptions.InvalidFeedException;
 
 public class EarthQuakeDatabase extends SQLiteOpenHelper {
 
 	private static final String DATABASE_NAME = "PocketShake";
 	private static final int DATABASE_VERSION = 1;
 	private static final String LOG_TAG = "EarthQuakeDatabase";
 	private final Context mContext;
 
 	public EarthQuakeDatabase(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		this.mContext = context;
 	}
 
 	public static class EarthquakeCursor extends SQLiteCursor {
 		/** The query for this cursor */
 		private static final String QUERY = "SELECT id, identifier, location, intensity, longitude, latitude, datetime, href FROM earthquakes where intensity > ? ORDER BY datetime desc";
 
 		/** Cursor constructor */
 		private EarthquakeCursor(SQLiteDatabase db, SQLiteCursorDriver driver,
 				String editTable, SQLiteQuery query) {
 			super(db, driver, editTable, query);
 		}
 
 		/** Private factory class necessary for rawQueryWithFactory() call */
 		private static class Factory implements SQLiteDatabase.CursorFactory {
 			@Override
 			public Cursor newCursor(SQLiteDatabase db,
 					SQLiteCursorDriver driver, String editTable,
 					SQLiteQuery query) {
 				return new EarthquakeCursor(db, driver, editTable, query);
 			}
 		}
 		
 		// accessor methods for each column.
 		public String getEarthquakeId() {
 			return getString(getColumnIndexOrThrow("identifier"));
 		}
 
 		public int getId() {
 			return getInt(getColumnIndexOrThrow("id"));
 		}
 
 		public String getLocation() {
 			return getString(getColumnIndexOrThrow("location"));
 		}
 
 		public String getIntensity() {
 			return getString(getColumnIndexOrThrow("intensity"));
 		}
 
 		public String getLongitude() {
 			return getString(getColumnIndexOrThrow("longitude"));
 		}
 
 		public String getLatitude() {
 			return getString(getColumnIndexOrThrow("latitude"));
 		}
 
 		public String getDate() {
 			return getString(getColumnIndexOrThrow("datetime"));
 		}
 
 		public String getDetailLink() {
 			return getString(getColumnIndexOrThrow("href"));
 		}
 
 		public EarthQuake getEarthQuake() {
 			EarthQuake earthquake = null;
 			try {
 				earthquake = new EarthQuake(
 						getInt(getColumnIndexOrThrow("id")),
 						getString(getColumnIndexOrThrow("identifier")), "M"
 								+ getString(getColumnIndexOrThrow("intensity"))
 								+ ", "
 								+ getString(getColumnIndexOrThrow("location")),
 						getString(getColumnIndexOrThrow("longitude")) + " "
 								+ getString(getColumnIndexOrThrow("latitude")),
 						getString(getColumnIndexOrThrow("datetime")), 
 						getString(getColumnIndexOrThrow("href")));
 			} catch (InvalidFeedException e) {
 				Log.e("Trying to return earthquake object", e.getMessage());
 			}
 			return earthquake;
 		}
 	}
 
 	private void executeManySqlStatements(SQLiteDatabase db, String[] sqls) {
 		for (String eachSql : sqls) {
 			if (eachSql.trim().length() > 0)
 				db.execSQL(eachSql);
 		}
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		String[] createSqls = mContext.getString(R.string.create_db_sql).split(
 				"\n");
 		db.beginTransaction();
 		try {
 			executeManySqlStatements(db, createSqls);
 			db.setTransactionSuccessful();
 		} catch (SQLException e) {
 			Log.e("Error creating tables and debug data", e.toString());
 		} finally {
 			db.endTransaction();
 		}
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		Log.w("EarthQuakeDBUpgrade", "Upgrading database from version "
 				+ oldVersion + " to " + newVersion
 				+ ", which will destroy all old data");
 
 		String[] createSqls = mContext.getString(R.string.create_db_sql).split(
 				"\n");
 		db.beginTransaction();
 		try {
 			executeManySqlStatements(db, createSqls);
 			db.setTransactionSuccessful();
 		} catch (SQLException e) {
 			Log.e("Error creating tables and debug data", e.toString());
 		} finally {
 			db.endTransaction();
 		}
 
 		// recreate database from scratch once again.
 		onCreate(db);
 	}
 
 	public EarthquakeCursor getEarthquakes(int intensity) {
 		Log.i(LOG_TAG, "Fetching earthquakes from database");
 		SQLiteDatabase db = getReadableDatabase();
 
 		return (EarthquakeCursor) db.rawQueryWithFactory(
 				new EarthquakeCursor.Factory(), EarthquakeCursor.QUERY, new String[]{Integer.toString(intensity)}, null);
 	}
 
 	public EarthQuake[] saveNewEarthquakesOnly(EarthQuake[] earthqaukeFeed) {
 		ArrayList<EarthQuake> newQuakes = new ArrayList<EarthQuake>();
 		for (EarthQuake eachEarthQuake : earthqaukeFeed) {
 			if (exists(eachEarthQuake)) {
 				Log.w(LOG_TAG,"Breaking creation loop as current element exists; rest of the enteries are assumed to exist");
 				//If current id exists rest would exist anyway.
 				break;
 			}
 			create(eachEarthQuake);
 			newQuakes.add(eachEarthQuake);
 		}
 		
 		return newQuakes.size() > 0 ? (EarthQuake[]) newQuakes.toArray(new EarthQuake[newQuakes.size()]) : new EarthQuake[0];
 	}
 
 	protected void create(EarthQuake eachEarthQuake) {
 		if (eachEarthQuake != null) {
 			try {
 				ContentValues dbValues = new ContentValues();
 				dbValues.put("identifier", eachEarthQuake.getIdentifier());
 				dbValues.put("intensity", eachEarthQuake.getIntensity());
 				dbValues.put("location", eachEarthQuake.getLocation());
 				dbValues.put("longitude", eachEarthQuake.getLongitude());
 				dbValues.put("latitude", eachEarthQuake.getLatitude());
 				dbValues.put("datetime", eachEarthQuake.getDate());
 				dbValues.put("href", eachEarthQuake.getDetailLink());
 				getWritableDatabase().insertOrThrow("earthquakes", "datetime", dbValues);
 			} catch (SQLException e) {
 				Log.e("Creating new earthquake", e.getMessage());
 			}
 		}
 
 	}
 
 	public boolean exists(EarthQuake eachEarthQuake) {
 		if (eachEarthQuake != null) {
 			Cursor c = null;
 			String count_query = "Select count(*) from earthquakes where identifier = ?";
 			try {
 				c = getReadableDatabase().rawQuery(count_query,
 						new String[] { eachEarthQuake.getIdentifier() });
 				if (c != null && c.moveToFirst() && c.getInt(0) > 0)
 					return true;
 
 			} catch (Exception e) {
 				Log.e("Running Count Query", e.getMessage());
 			} finally {
 				if (c != null) {
 					try {
 						c.close();
 					} catch (SQLException e) {
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	public void deleteRecordsOlderThanDays(String purgeDay) {
 		if(purgeDay != null) {
 			try {
 				int num = getWritableDatabase().delete("earthquakes", " (strftime('%J','now','UTC') - strftime('%J',datetime, 'UTC')) > " + purgeDay, null);
 				Log.i("Cleaning up old records in database", num + " records deleted successfully");
 			} catch(SQLException e) {
 				Log.e("Tried Cleaning up old data from database",e.getMessage());
 			}
 		}
 	}
 }
