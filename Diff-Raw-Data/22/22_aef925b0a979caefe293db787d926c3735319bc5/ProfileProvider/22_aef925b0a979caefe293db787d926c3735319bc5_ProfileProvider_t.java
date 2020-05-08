 package org.istic.nestor.profile.provider;
 
 import java.util.HashMap;
 
 import org.istic.nestor.profile.utils.Profile;
 
 import android.content.ContentProvider;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteQueryBuilder;
 import android.net.Uri;
 import android.util.Log;
 
 public class ProfileProvider extends ContentProvider {
 	// Constantes
 	private static final String TAG = "ProfileProvider";
 	private static final String DATABASE_NAME = "profiles.db";
 	private static final int DATABASE_VERSION = 9;
 	private static final String PROFILE_TABLE_NAME = "profiles";
 	private static final int PROFILES = 1;
 	private static final UriMatcher sUriMatcher;
 	private static final String SQL_CREATE_MAIN = "CREATE TABLE " + PROFILE_TABLE_NAME + " ("
 			+ Profile.PROFILE_ID
 			+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
 			+ Profile.PROFILE_NAME + " VARCHAR(255),"
 			+ Profile.PROFILE_AUDIO + " INTEGER,"
 			+ Profile.PROFILE_BLUETOOTH + " INTEGER,"
 			+ Profile.PROFILE_GPS + " INTEGER," 
 			+ Profile.PROFILE_RADIO	+ " INTEGER, " 
 			+ Profile.PROFILE_WIFI + " INTEGER, "
 			+ Profile.Monday + " INTEGER, "
 			+ Profile.Tuesday +" INTEGER, "
 			+ Profile.Wednesday +" INTEGER, "
 			+ Profile.Thursday + " INTEGER, "
 			+ Profile.Friday + " INTEGER, "
 			+ Profile.Saturday + " INTEGER, "
 			+ Profile.Sunday + " INTEGER);";
 
 	private static HashMap<String, String> profileProjectionMap;
 	private DatabaseHelper dbHelper = new DatabaseHelper(getContext());
 	// DatabaseHelper
 
 	static {
 		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
 		sUriMatcher.addURI(Profile.AUTHORITY, PROFILE_TABLE_NAME, PROFILES);
 
 		profileProjectionMap = new HashMap<String, String>();
 		profileProjectionMap.put(Profile.PROFILE_ID, Profile.PROFILE_ID);
 		profileProjectionMap.put(Profile.PROFILE_NAME, Profile.PROFILE_NAME);
 		profileProjectionMap.put(Profile.PROFILE_AUDIO, Profile.PROFILE_AUDIO);
 		profileProjectionMap.put(Profile.PROFILE_BLUETOOTH,
 				Profile.PROFILE_BLUETOOTH);
 		profileProjectionMap.put(Profile.PROFILE_GPS, Profile.PROFILE_GPS);
 		profileProjectionMap.put(Profile.PROFILE_RADIO, Profile.PROFILE_RADIO);
 		profileProjectionMap.put(Profile.PROFILE_WIFI, Profile.PROFILE_WIFI);
 		profileProjectionMap.put(Profile.Monday, Profile.Monday);
 		profileProjectionMap.put(Profile.Tuesday, Profile.Tuesday);
 		profileProjectionMap.put(Profile.Wednesday, Profile.Wednesday);
 		profileProjectionMap.put(Profile.Thursday, Profile.Thursday);
 		profileProjectionMap.put(Profile.Friday, Profile.Friday);
 		profileProjectionMap.put(Profile.Saturday, Profile.Saturday);
 		profileProjectionMap.put(Profile.Sunday, Profile.Sunday);
 	}
 
 	private static final class DatabaseHelper extends SQLiteOpenHelper {
 
 		DatabaseHelper(Context context) {
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			Log.d(TAG, "Creation de la base de donnees");
 			Log.d(TAG, "Requete de creation = "+SQL_CREATE_MAIN);
 			db.execSQL(SQL_CREATE_MAIN);
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
 					+ newVersion + ", which will destroy all old data");
 			db.execSQL("DROP TABLE IF EXISTS " + PROFILE_TABLE_NAME);
 			onCreate(db);
 		}
 	}
 
 	@Override
 	public int delete(Uri arg0, String arg1, String[] arg2) {
 		SQLiteDatabase db = dbHelper.getWritableDatabase();
 		Log.d(TAG,"Deletion "+arg1);
 		return db.delete(PROFILE_TABLE_NAME, arg1, arg2);
 		// TODO Auto-generated method stub
 		//throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public String getType(Uri arg0) {
 		return arg0.getFragment();
 	}
 
 	@Override
 	public Uri insert(Uri arg0, ContentValues values) {
 		SQLiteDatabase db = dbHelper.getWritableDatabase();
 		Log.d(TAG, "Insertion " + values.toString());
 		db.insert(PROFILE_TABLE_NAME, null, values);
		
 		return null;
 	}
 
 	@Override
 	public boolean onCreate() {
 		dbHelper = new DatabaseHelper(getContext());
 		Log.d(TAG, "ProfileProvider created");
 		return true;
 	}
 
 	@Override
 	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
 			String sortOrder) {
 		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
 		switch (sUriMatcher.match(uri)) {
 		case PROFILES:
 			qb.setTables(PROFILE_TABLE_NAME);
 			qb.setProjectionMap(profileProjectionMap);
 			break;
 		default:
 			throw new IllegalArgumentException("Unknown URI " + uri);
 		}
 		SQLiteDatabase db = dbHelper.getReadableDatabase();
 		Cursor c = qb.query(db, projection, selection, selectionArgs, null,
 				null, sortOrder);
 		c.setNotificationUri(getContext().getContentResolver(), uri);
 		return c;
 	}
 
 	public Cursor findProfileByName(String name) {
 		String query = "SELECT * FROM " + Profile.PROFILE_NAME
 				+ "WHERE name = ?";
 		Cursor cursor = dbHelper.getReadableDatabase().rawQuery(query,
 				new String[] { name });
 		return cursor;
 	}
 	
 	//	TODO A tester
 	public Cursor getAllProfiles(){
 		String[] projection = new String[]{"*"};
 		
 		Cursor cursor = query(Profile.CONTENT_URI, projection, null, null, Profile.PROFILE_NAME);
 		return cursor;
 	}
 
 	@Override
 	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
 		SQLiteDatabase db = dbHelper.getWritableDatabase();
 		Log.d(TAG, "Updating ");
 		return db.update(PROFILE_TABLE_NAME, arg1, arg2, arg3);
 	}
 
 }
