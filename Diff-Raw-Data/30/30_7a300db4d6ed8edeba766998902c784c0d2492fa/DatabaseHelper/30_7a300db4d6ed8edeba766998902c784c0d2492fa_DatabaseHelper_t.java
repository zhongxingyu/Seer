 package com.samsandberg.nofi;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.location.Location;
 import android.util.Log;
 import au.com.bytecode.opencsv.CSVReader;
 
 public class DatabaseHelper extends SQLiteOpenHelper {
 
 	private final String TAG = "NoFi_DatabaseHelper";
 	
 	private Context context;
 	
 	// All Static variables
 	// Database Version
 	private static final int DATABASE_VERSION = 1;
  
 	private static final String DATABASE_NAME = "nofi";
 
 	public static final int SHARE_LEVEL_PUBLIC = 0;
 	public static final int SHARE_LEVEL_FRIENDS = 1; // TODO
 	public static final int SHARE_LEVEL_PRIVATE = 2;
 	
 	private static final String TABLE_HOTSPOTS = "hotspots";
 	private static final String KEY_HOTSPOTS_ID = "id";
 	private static final String KEY_HOTSPOTS_SSID = "ssid";
 	private static final String KEY_HOTSPOTS_PASSWORD = "password";
 	private static final String KEY_HOTSPOTS_PASSWORD_PROTECTED = "passwordProtected";
 	private static final String KEY_HOTSPOTS_MAC_ADDRESS = "macAddress";
 	private static final String KEY_HOTSPOTS_LATITUDE = "latitude";
 	private static final String KEY_HOTSPOTS_LONGITUDE = "longitude";
 	private static final String KEY_HOTSPOTS_FOURSQUARE_ID = "foursquareId";
 	private static final String KEY_HOTSPOTS_FOURSQUARE_NAME = "foursquareName";
 	private static final String KEY_HOTSPOTS_FOURSQUARE_TYPE = "foursquareType";
 	private static final String KEY_HOTSPOTS_LAST_CONNECTED = "lastConnected";
 	private static final String KEY_HOTSPOTS_SHARE_LEVEL = "shareLevel";
 	private static final String KEY_HOTSPOTS_UPLOADED = "uploaded";
 
 	private static final String TABLE_NOTES = "notes";
 	private static final String KEY_NOTES_ID = "id";
 	private static final String KEY_NOTES_HOTSPOT_ID = "hotspot_id";
 	private static final String KEY_NOTES_CREATED = "created";
 	private static final String KEY_NOTES_NOTE = "note";
 
 	public DatabaseHelper(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		this.context = context;
 	}
  
 	// Creating Tables
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		String CREATE_HOTSPOTS_TABLE = "CREATE TABLE " + TABLE_HOTSPOTS + " (" +
 			KEY_HOTSPOTS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
 			KEY_HOTSPOTS_SSID + " TEXT, " +
 			KEY_HOTSPOTS_PASSWORD + " TEXT, " +
 			KEY_HOTSPOTS_PASSWORD_PROTECTED + " INTEGER, " +
 			KEY_HOTSPOTS_MAC_ADDRESS + " TEXT, " +
 			KEY_HOTSPOTS_LATITUDE + " REAL, " +
 			KEY_HOTSPOTS_LONGITUDE + " REAL, " +
 			KEY_HOTSPOTS_FOURSQUARE_ID + " TEXT, " +
 			KEY_HOTSPOTS_FOURSQUARE_NAME + " TEXT, " +
 			KEY_HOTSPOTS_FOURSQUARE_TYPE + " TEXT, " +
 			KEY_HOTSPOTS_LAST_CONNECTED + " REAL, " +
 			KEY_HOTSPOTS_SHARE_LEVEL + " INTEGER, " +
 			KEY_HOTSPOTS_UPLOADED + " REAL" + 
 		")";
 		db.execSQL(CREATE_HOTSPOTS_TABLE);
 		
 		String CREATE_NOTES_TABLE = "CREATE TABLE " + TABLE_NOTES + " (" +
 			KEY_NOTES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
 			KEY_NOTES_HOTSPOT_ID + " INTEGER, " +
 			KEY_NOTES_CREATED + " REAL, " +
 			KEY_NOTES_NOTE + " TEXT" +
 		")";
 		db.execSQL(CREATE_NOTES_TABLE);
 	}
 
 	// Upgrading database (do this if we change something)
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		
 		// TODO: figure out why we're resetting... 
 		// Wouldn't we want to modify the schema or do something else more clever instead?
 		resetDatabase();
 	}
 	
 	public void resetDatabase() {
 		SQLiteDatabase db = getWritableDatabase();
 		
 		// Drop older tables if existed
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_HOTSPOTS);
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
 
 		// Create tables again
 		onCreate(db);
 		
 		db.close();
 	}
 	
 	private ContentValues getContentValuesFromData(Hotspot hotspot, long lastConnected, int shareLevel) {
 		ContentValues values = new ContentValues();
 		values.put(KEY_HOTSPOTS_SSID, hotspot.ssid);
 		if (hotspot.passwordProtected) {
 			values.put(KEY_HOTSPOTS_PASSWORD, hotspot.password);
 			values.put(KEY_HOTSPOTS_PASSWORD_PROTECTED, 1);
 		} else {
 			values.put(KEY_HOTSPOTS_PASSWORD, "");
 			values.put(KEY_HOTSPOTS_PASSWORD_PROTECTED, 0);
 		}
 		values.put(KEY_HOTSPOTS_MAC_ADDRESS, hotspot.macAddress);
 		values.put(KEY_HOTSPOTS_LATITUDE, hotspot.getLatitude());
 		values.put(KEY_HOTSPOTS_LONGITUDE, hotspot.getLongitude());
 		values.put(KEY_HOTSPOTS_FOURSQUARE_ID, hotspot.foursquareId);
 		values.put(KEY_HOTSPOTS_FOURSQUARE_NAME, hotspot.foursquareName);
 		values.put(KEY_HOTSPOTS_FOURSQUARE_TYPE, hotspot.foursquareType);
 		values.put(KEY_HOTSPOTS_LAST_CONNECTED, lastConnected);
 		values.put(KEY_HOTSPOTS_SHARE_LEVEL, shareLevel);
 		values.put(KEY_HOTSPOTS_UPLOADED, 0);
 		
 		return values;
 	}
 	
 	public long addHotspot(Hotspot hotspot, long lastConnected, int shareLevel) {
 		ContentValues values = getContentValuesFromData(hotspot, lastConnected, shareLevel);
 		SQLiteDatabase db = getWritableDatabase();
 		long id = db.insert(TABLE_HOTSPOTS, null, values);
 		db.close();
 		return id;
 	}
 	
 	public long updateHotspot(Hotspot hotspot, long lastConnected, int shareLevel) {
 		Log.d(TAG, "updateHotspot()");
 		
 		int hotspotId = getHotspotIdBySSIDAndMacAddress(hotspot.ssid, hotspot.macAddress);
 		Log.d(TAG, "hotspotId=" + hotspotId);
 		
 		SQLiteDatabase db = getWritableDatabase();
 		ContentValues values = getContentValuesFromData(hotspot, lastConnected, shareLevel);
 		
 		// Insert
 		if (hotspotId == 0) {
 			long id = db.insert(TABLE_HOTSPOTS, null, values);
 			db.close();
 			return id;
 		}
 		
 		// Update
 		db.update(TABLE_HOTSPOTS, values, KEY_HOTSPOTS_ID + " = ?", new String[] {String.valueOf(hotspotId)});
 		db.close();
 		return hotspotId;
 	}
 	
 	private int getHotspotIdBySSIDAndMacAddress(String ssid, String macAddress) {
 		String query = "SELECT " + KEY_HOTSPOTS_ID + " FROM " + TABLE_HOTSPOTS + " " +
 			"WHERE " + KEY_HOTSPOTS_SSID + " = ? AND " + KEY_HOTSPOTS_MAC_ADDRESS + " = ?";
 		String[] selectionArgs = new String[] {ssid, macAddress};
 		SQLiteDatabase db = getReadableDatabase();
 		Cursor cursor = db.rawQuery(query, selectionArgs);
 		
 		if (cursor == null || ! cursor.moveToFirst()) {
 			db.close();
 			return 0;
 		}
 		
 		int result = cursor.getInt(0);
 		db.close();
 		return result;
 	}
 	
 	private int getHotspotIdByFoursquareId(String foursquareId) {
 		String query = "SELECT " + KEY_HOTSPOTS_ID + " FROM " + TABLE_HOTSPOTS + " " +
 			"WHERE " + KEY_HOTSPOTS_FOURSQUARE_ID + " = ?";
 		String[] selectionArgs = new String[] {foursquareId};
 		SQLiteDatabase db = getReadableDatabase();
 		Cursor cursor = db.rawQuery(query, selectionArgs);
 		
 		if (cursor == null || ! cursor.moveToFirst()) {
 			db.close();
 			return 0;
 		}
 		
 		int result = cursor.getInt(0);
 		db.close();
 		return result;
 	}
 	
 	private Hotspot getHotspotFromCursor(Cursor cursor) {
 		Hotspot hotspot = new Hotspot(cursor.getDouble(5), cursor.getDouble(6));
 		hotspot.setWifiInfo(cursor.getString(1), cursor.getString(4));
 		if (cursor.getInt(3) == 1) {
 			hotspot.setPassword(cursor.getString(2));
 		}
		hotspot.setFoursquareInfo(cursor.getString(7), cursor.getString(8), cursor.getString(9));
 		return hotspot;
 	}
 	
 	public Hotspot getHotspotBySSIDAndMacAddress(String ssid, String macAddress) {
 		String query = "SELECT * FROM " + TABLE_HOTSPOTS + " " +
 			"WHERE " + KEY_HOTSPOTS_SSID + " = ? AND " + KEY_HOTSPOTS_MAC_ADDRESS + " = ?";
 		String[] selectionArgs = new String[] {ssid, macAddress};
 		SQLiteDatabase db = getReadableDatabase();
 		Cursor cursor = db.rawQuery(query, selectionArgs);
 		
 		if (cursor == null || ! cursor.moveToFirst()) {
 			db.close();
 			return null;
 		}
 		
 		Hotspot hotspot = getHotspotFromCursor(cursor);
 		db.close();
 		return hotspot;
 	}
 	
 	public List<Hotspot> getHotspotsByLocation(Location location, int maxDistance) {
         List<Hotspot> hotspots = new ArrayList<Hotspot>();
 
 		String query = "SELECT * FROM " + TABLE_HOTSPOTS;
 		SQLiteDatabase db = getReadableDatabase();
 		Cursor cursor = db.rawQuery(query, null);
 		
 		if (cursor == null || ! cursor.moveToFirst()) {
 			db.close();
 			return hotspots;
 		}
 		
 		do {
 			Hotspot hotspot = getHotspotFromCursor(cursor);
 			
 			if (location == null || maxDistance == 0 || location.distanceTo(hotspot) <= maxDistance) {
 				List<String> notes = getNotesByHotspotId(cursor.getInt(0));
 				for (String note : notes) {
 					hotspot.addNote(note);
 				}
 				hotspots.add(hotspot);
 			}
 			
 		} while (cursor.moveToNext());
         
 		db.close();
 		return hotspots;
 	}
 	
 	public List<Hotspot> getAllHotspots() {
 		return getHotspotsByLocation(null, 0);
 	}
 	
 	public long addNote(int hotspotId, long created, String note) {
 		ContentValues values = new ContentValues();
 		values.put(KEY_NOTES_HOTSPOT_ID, hotspotId);
 		values.put(KEY_NOTES_CREATED, created);
 		values.put(KEY_NOTES_NOTE, note);
 		SQLiteDatabase db = getWritableDatabase();
 		long id = db.insert(TABLE_NOTES, null, values);
 		db.close();
 		return id;
 	}
 	
 	public List<String> getNotesByHotspotId(int hotspotId) {
 		List<String> notes = new ArrayList<String>();
 		
 		String query = "SELECT * FROM " + TABLE_NOTES + " " +
 			"WHERE " + KEY_NOTES_HOTSPOT_ID + " = ?";
 		String[] selectionArgs = new String[] {String.valueOf(hotspotId)};
 		SQLiteDatabase db = getReadableDatabase();
 		Cursor cursor = db.rawQuery(query, selectionArgs);
 		
 		if (cursor == null || ! cursor.moveToFirst()) {
 			db.close();
 			return notes;
 		}
 		
 		do {
 			notes.add(cursor.getString(3));
 		} while (cursor.moveToNext());
 		
 		db.close();
 		return notes;
 	}
 	
 	// TODO: implement this stuff
 	public List<Hotspot> getAllHotspotsForUpload(int shareLevel) {
         List<Hotspot> hotspots = new ArrayList<Hotspot>();
 
 		String query = "SELECT * FROM " + TABLE_HOTSPOTS +
 			" WHERE " + KEY_HOTSPOTS_UPLOADED + " = 0" + 
 			" AND " + KEY_HOTSPOTS_SHARE_LEVEL + " = " + shareLevel;
 		SQLiteDatabase db = getReadableDatabase();
 		Cursor cursor = db.rawQuery(query, null);
 		
 		if (cursor == null || ! cursor.moveToFirst()) {
 			db.close();
 			return hotspots;
 		}
 		
 		do {
 			hotspots.add(getHotspotFromCursor(cursor));
 		} while (cursor.moveToNext());
 		
 		db.close();
 		return hotspots;
 	}
 	
 	public void insertSampleDataWV() {
 		resetDatabase();
 
         List<Hotspot> hotspots = new ArrayList<Hotspot>();
         hotspots.add(new Hotspot(40.73479, -73.998718).setWifiInfo("6th ave and west 10th", ""));
         hotspots.add(new Hotspot(40.736602, -74.00114).setWifiInfo("7th Avenue South and Greenwich Avenue", ""));
         hotspots.add(new Hotspot(40.73434, -74.002391).setWifiInfo("7th Avenue South and West 10th", ""));
          
         Hotspot zemanta = new Hotspot(40.738795, -73.993921)
         	.setWifiInfo("NETGEAR72", "")
         	.setPassword("modernship910");
         hotspots.add(zemanta);
         
         Hotspot home = new Hotspot(40.734347,-74.001596)
         	.setWifiInfo("Komenskega Ulica", "")
     		.setPassword("tacotuesday");
         hotspots.add(home);
         
         for (Hotspot hotspot : hotspots) {
         	addHotspot(hotspot, 0, SHARE_LEVEL_PUBLIC);
         }
 	}
 	
 	public void insertSampleDataLjubljana() {
 		// tip_id,created_at,text,venue_id,venue_lat,venue_lon,venue_name,venue_category
 		InputStream raw = context.getResources().openRawResource(R.raw.ljubljana_data);
 		
 		// CSV reader tip via:
 		// http://stackoverflow.com/questions/6057695/how-to-parse-the-csv-file-in-android-application
         CSVReader reader = new CSVReader(new InputStreamReader(raw));
         
 		int counter = 0;
         while (true) {
         	String[] row;
 			try {
 				row = reader.readNext();
 			} catch (IOException e) {
 				Log.e(TAG, "IOException encountered when reading row");
 				e.printStackTrace();
 				break;
 			}
         	
 			// EOF
         	if (row == null) {
         		break;
         	}
         	counter++;
         	
         	if (counter == 1) {
         		continue;
         	}
         	
         	Log.i(TAG, "Inserting row " + counter + "...");
 
     		// tip_id,created_at,text,venue_id,venue_lat,venue_lon,venue_name,venue_category
             int hotspotId = getHotspotIdByFoursquareId(row[3]);
             
             // No venue, create it!
             if (hotspotId == 0) {
             	if (row.length != 8) {
             		Log.e(TAG, "Row length not 8..");
             		Log.d(TAG, String.valueOf(row));
             		continue;
             	}
             	
             	Hotspot hotspot = new Hotspot(Double.valueOf(row[4]), Double.valueOf(row[5]));
             	hotspot.setFoursquareInfo(row[3], row[6], row[7]);
             	hotspotId = (int) addHotspot(hotspot, 0, SHARE_LEVEL_PUBLIC);
             }
         	
             long created = Integer.valueOf(row[1]) * 1000;
         	addNote(hotspotId, created, row[2]);
         }
 
         try {
         	reader.close();
 			raw.close();
 		} catch (IOException e) {
 			Log.e(TAG, "IOException encountered when trying to close FileReader/BufferedReader");
 			e.printStackTrace();
 		}
 	}
 }
