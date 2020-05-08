 package com.app.wordservant.provider;
 
 import java.util.HashMap;
 
 import android.content.ContentProvider;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteQueryBuilder;
 import android.net.Uri;
 
 public class WordServantProvider extends ContentProvider {
 
 	// Used for debugging and logging
 	private static final String TAG = "WordServantProvider";
 
 	/**
 	 * The database that the provider uses as its underlying data store
 	 */
 	private static final String DATABASE_NAME = "wordservant_db";
 
 	/**
 	 * The database version
 	 */
 	private static final int DATABASE_VERSION = 13;
 
 	/**
 	 * A UriMatcher instance
 	 */
 	private static final UriMatcher sUriMatcher;
 
 	/**
 	 * A projection map used to select columns from the database
 	 */
 	private static HashMap<String, String> sScripturesProjectionMap;
 
 
 	// The incoming URI matches the Notes URI pattern
 	private static final int SCRIPTURES = 1;
 
 	// The incoming URI matches the Note ID URI pattern
 	private static final int SCRIPTURE_ID = 2;
 	
 	// The incoming URI matches the Note ID URI pattern
 	private static final int INCREMENT_TIMES_REVIEWED = 3;
 	
 	// The incoming URI matches the Note ID URI pattern
 	private static final int DECREMENT_TIMES_REVIEWED = 4;
 
 	// Handle to a new DatabaseHelper.
 	private WordServantDatabaseHelper mOpenHelper;
 
 	/**
 	 * A block that instantiates and sets static objects
 	 */
 	static {
 
 		/*
 		 * Creates and initializes the URI matcher
 		 */
 		// Create a new instance
 		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
 
 		// Add a pattern that routes URIs terminated with "scriptures" to a SCRIPTURES operation
 		sUriMatcher.addURI(WordServantContract.AUTHORITY, "scriptures", SCRIPTURES);
 
 		// Add a pattern that routes URIs terminated with "scriptures" plus an integer
 		// to a scripture ID operation
 		sUriMatcher.addURI(WordServantContract.AUTHORITY, "scriptures/#", SCRIPTURE_ID);
 		
 		// Add a pattern that routes URIs terminated with "scriptures" plus an integer
 		// to a scripture ID operation
 		sUriMatcher.addURI(WordServantContract.AUTHORITY, "scriptures/#/INCREMENT_TIMES_REVIEWED", INCREMENT_TIMES_REVIEWED);
 		
 		// Add a pattern that routes URIs terminated with "scriptures" plus an integer
 		// to a scripture ID operation
 		sUriMatcher.addURI(WordServantContract.AUTHORITY, "scriptures/#/DECREMENT_TIMES_REVIEWED", DECREMENT_TIMES_REVIEWED);
 
 		/*
 		 * Creates and initializes a projection map that returns all columns
 		 */
 
 		// Creates a new projection map instance. The map returns a column name
 		// given a string. The two are usually equal.
 		sScripturesProjectionMap = new HashMap<String, String>();
 
 		// Maps the string "_ID" to the column name "_ID"
 		sScripturesProjectionMap.put(WordServantContract.ScriptureEntry._ID, WordServantContract.ScriptureEntry._ID);
 
 		// Map the rest of the columns.
 		sScripturesProjectionMap.put(WordServantContract.ScriptureEntry.COLUMN_NAME_REFERENCE, 
 				WordServantContract.ScriptureEntry.COLUMN_NAME_REFERENCE);
 		sScripturesProjectionMap.put(WordServantContract.ScriptureEntry.COLUMN_NAME_CORRECTLY_REVIEWED_COUNT, 
 				WordServantContract.ScriptureEntry.COLUMN_NAME_CORRECTLY_REVIEWED_COUNT);
 		sScripturesProjectionMap.put(WordServantContract.ScriptureEntry.COLUMN_NAME_CREATED_DATE, 
 				WordServantContract.ScriptureEntry.COLUMN_NAME_CREATED_DATE);
 		sScripturesProjectionMap.put(WordServantContract.ScriptureEntry.COLUMN_NAME_INCORRECTLY_REVIEWED_COUNT, 
 				WordServantContract.ScriptureEntry.COLUMN_NAME_INCORRECTLY_REVIEWED_COUNT);
 		sScripturesProjectionMap.put(WordServantContract.ScriptureEntry.COLUMN_NAME_LAST_REVIEWED_DATE, 
 				WordServantContract.ScriptureEntry.COLUMN_NAME_LAST_REVIEWED_DATE);
 		sScripturesProjectionMap.put(WordServantContract.ScriptureEntry.COLUMN_NAME_NEXT_REVIEW_DATE, 
 				WordServantContract.ScriptureEntry.COLUMN_NAME_NEXT_REVIEW_DATE);
 		sScripturesProjectionMap.put(WordServantContract.ScriptureEntry.COLUMN_NAME_SCHEDULE, 
 				WordServantContract.ScriptureEntry.COLUMN_NAME_SCHEDULE);
 		sScripturesProjectionMap.put(WordServantContract.ScriptureEntry.COLUMN_NAME_SKIPPED_REVIEW_COUNT, 
 				WordServantContract.ScriptureEntry.COLUMN_NAME_SKIPPED_REVIEW_COUNT);
 		sScripturesProjectionMap.put(WordServantContract.ScriptureEntry.COLUMN_NAME_TEXT, 
 				WordServantContract.ScriptureEntry.COLUMN_NAME_TEXT);
 		sScripturesProjectionMap.put(WordServantContract.ScriptureEntry.COLUMN_NAME_TIMES_REVIEWED, 
 				WordServantContract.ScriptureEntry.COLUMN_NAME_TIMES_REVIEWED);
 
 	}
 
 	/**
 	 *
 	 * This class helps open, create, and upgrade the database file. Set to package visibility
 	 * for testing purposes.
 	 */
 	static class WordServantDatabaseHelper extends SQLiteOpenHelper {
 
 		private static String SCRIPTURE_BANK_TABLE_CREATE = "CREATE TABLE "+ WordServantContract.ScriptureEntry.TABLE_NAME+ " ("+
 				WordServantContract.ScriptureEntry._ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
 				WordServantContract.ScriptureEntry.COLUMN_NAME_REFERENCE+" TEXT NOT NULL, "+
 				WordServantContract.ScriptureEntry.COLUMN_NAME_TEXT+" TEXT NOT NULL, "+
 				WordServantContract.ScriptureEntry.COLUMN_NAME_CREATED_DATE+" TEXT DEFAULT CURRENT_DATE, "+
 				WordServantContract.ScriptureEntry.COLUMN_NAME_SCHEDULE+" TEXT NOT NULL DEFAULT daily, "+
 				WordServantContract.ScriptureEntry.COLUMN_NAME_LAST_REVIEWED_DATE+" TEXT, "+
 				WordServantContract.ScriptureEntry.COLUMN_NAME_TIMES_REVIEWED+" INTEGER NOT NULL DEFAULT 0, "+
 				WordServantContract.ScriptureEntry.COLUMN_NAME_NEXT_REVIEW_DATE+" TEXT DEFAULT (date('now','localtime')), "+
 				WordServantContract.ScriptureEntry.COLUMN_NAME_CORRECTLY_REVIEWED_COUNT+" INTEGER NOT NULL DEFAULT 0, "+
 				WordServantContract.ScriptureEntry.COLUMN_NAME_INCORRECTLY_REVIEWED_COUNT+" INTEGER NOT NULL DEFAULT 0, "+
 				WordServantContract.ScriptureEntry.COLUMN_NAME_SKIPPED_REVIEW_COUNT+" INTEGER NOT NULL DEFAULT 0 "+
 				");";
 
 		private static String TAG_TABLE_CREATE = "CREATE TABLE "+WordServantContract.TagEntry.TABLE_NAME+" ("+
 				WordServantContract.TagEntry._ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
 				WordServantContract.TagEntry.COLUMN_NAME_TAG_NAME+" TEXT NOT NULL);";
 
 		private static String CATEGORY_TABLE_CREATE = "CREATE TABLE "+WordServantContract.CategoryEntry.TABLE_NAME+" ("+
 				WordServantContract.CategoryEntry._ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
 				WordServantContract.CategoryEntry.COLUMN_NAME_SCRIPTURE_ID+ " INTEGER NOT NULL, "+
 				WordServantContract.CategoryEntry.COLUMN_NAME_TAG_ID+ " INTEGER NOT NULL, "+
 				"FOREIGN KEY ("+WordServantContract.CategoryEntry.COLUMN_NAME_SCRIPTURE_ID+") REFERENCES "+
 				WordServantContract.ScriptureEntry.TABLE_NAME+"("+WordServantContract.ScriptureEntry._ID+"), "+
 				"FOREIGN KEY ("+WordServantContract.CategoryEntry.COLUMN_NAME_TAG_ID+") REFERENCES "+
 				WordServantContract.TagEntry.TABLE_NAME+"("+WordServantContract.TagEntry._ID+"));";
 
 		//Triggers
 		private static String TRIGGER_NAME_SCHEDULE_DAILY = "schedule_update_daily";
 		private static String TRIGGER_NAME_SCHEDULE_WEEKLY = "schedule_update_weekly";
 		private static String TRIGGER_NAME_SCHEDULE_MONTHLY = "schedule_update_monthly";
 		private static String TRIGGER_NAME_SCHEDULE_YEARLY = "schedule_update_yearly";
 		private static String TRIGGER_NAME_AUTO_UPDATE_DATE = "auto_update_date";
 
 		private static String TRIGGER_SCHEDULE_DAILY = "CREATE TRIGGER "+TRIGGER_NAME_SCHEDULE_DAILY+" AFTER UPDATE OF "+
 				WordServantContract.ScriptureEntry.COLUMN_NAME_TIMES_REVIEWED+" ON "+WordServantContract.ScriptureEntry.TABLE_NAME+
 				" BEGIN UPDATE "+WordServantContract.ScriptureEntry.TABLE_NAME+" SET "+WordServantContract.ScriptureEntry.COLUMN_NAME_SCHEDULE+
 				"='daily' WHERE "+WordServantContract.ScriptureEntry.COLUMN_NAME_TIMES_REVIEWED+"<7"+
 				" AND _id = new._id; END;";
 
 		private static String TRIGGER_SCHEDULE_WEEKLY = "CREATE TRIGGER "+TRIGGER_NAME_SCHEDULE_WEEKLY+" AFTER UPDATE OF "+
 				WordServantContract.ScriptureEntry.COLUMN_NAME_TIMES_REVIEWED+" ON "+WordServantContract.ScriptureEntry.TABLE_NAME+
 				" BEGIN UPDATE "+WordServantContract.ScriptureEntry.TABLE_NAME+" SET "+WordServantContract.ScriptureEntry.COLUMN_NAME_SCHEDULE+
 				"='weekly' WHERE "+WordServantContract.ScriptureEntry.COLUMN_NAME_TIMES_REVIEWED+">=7 AND "+
 				WordServantContract.ScriptureEntry.COLUMN_NAME_TIMES_REVIEWED+"<14"+
 				" AND _id = new._id; END;";
 
 		private static String TRIGGER_SCHEDULE_MONTHLY = "CREATE TRIGGER "+TRIGGER_NAME_SCHEDULE_MONTHLY+" AFTER UPDATE OF "+
 				WordServantContract.ScriptureEntry.COLUMN_NAME_TIMES_REVIEWED+" ON "+WordServantContract.ScriptureEntry.TABLE_NAME+
 				" BEGIN UPDATE "+WordServantContract.ScriptureEntry.TABLE_NAME+" SET "+WordServantContract.ScriptureEntry.COLUMN_NAME_SCHEDULE+
 				"='monthly' WHERE "+WordServantContract.ScriptureEntry.COLUMN_NAME_TIMES_REVIEWED+">=14 AND "+
 				WordServantContract.ScriptureEntry.COLUMN_NAME_TIMES_REVIEWED+"<21"+
 				" AND _id = new._id; END;";
 
 		private static String TRIGGER_SCHEDULE_YEARLY = "CREATE TRIGGER "+TRIGGER_NAME_SCHEDULE_YEARLY+" AFTER UPDATE OF "+
 				WordServantContract.ScriptureEntry.COLUMN_NAME_TIMES_REVIEWED+" ON "+WordServantContract.ScriptureEntry.TABLE_NAME+
 				" BEGIN UPDATE "+WordServantContract.ScriptureEntry.TABLE_NAME+" SET "+WordServantContract.ScriptureEntry.COLUMN_NAME_SCHEDULE+
 				"='yearly' WHERE "+WordServantContract.ScriptureEntry.COLUMN_NAME_TIMES_REVIEWED+">=21 AND "+
 				WordServantContract.ScriptureEntry.COLUMN_NAME_TIMES_REVIEWED+"<28"+
 				" AND _id = new._id; END;";
 //FIXME trigger issue!!
 		private static String TRIGGER_AUTO_UPDATE_DATE = "CREATE TRIGGER "+TRIGGER_NAME_AUTO_UPDATE_DATE+" AFTER UPDATE OF "+
 				WordServantContract.ScriptureEntry.COLUMN_NAME_TIMES_REVIEWED+" ON "+WordServantContract.ScriptureEntry.TABLE_NAME+
 				" BEGIN UPDATE "+WordServantContract.ScriptureEntry.TABLE_NAME+" SET "+WordServantContract.ScriptureEntry.COLUMN_NAME_NEXT_REVIEW_DATE+
 				"=date('now', 'localtime', "+
 				"case when old."+WordServantContract.ScriptureEntry.COLUMN_NAME_TIMES_REVIEWED+" < new."+
 				WordServantContract.ScriptureEntry.COLUMN_NAME_TIMES_REVIEWED+" then "+
 				"case "+WordServantContract.ScriptureEntry.COLUMN_NAME_SCHEDULE+" "+
 				"when 'daily' then '+1 day' "+
 				"when 'weekly' then '+7 day' "+
 				"when 'monthly' then '+1 month' "+
 				"when 'yearly' then '+365 days' "+
 				"end else '+0 day' end), "+WordServantContract.ScriptureEntry.COLUMN_NAME_LAST_REVIEWED_DATE+"="+
 				"case when old."+WordServantContract.ScriptureEntry.COLUMN_NAME_TIMES_REVIEWED+" < new."+
 				WordServantContract.ScriptureEntry.COLUMN_NAME_TIMES_REVIEWED+" then date('now','localtime') else "+
 				" date("+WordServantContract.ScriptureEntry.COLUMN_NAME_LAST_REVIEWED_DATE+") end"+
 				" WHERE _id = new._id; END;";
 
 		public WordServantDatabaseHelper(Context context) {
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			// Creates the scripture bank table when the database is created.
 			db.execSQL(SCRIPTURE_BANK_TABLE_CREATE);
 			db.execSQL(TAG_TABLE_CREATE);
 			db.execSQL(CATEGORY_TABLE_CREATE);
 
 			//Create triggers.
 			db.execSQL(TRIGGER_SCHEDULE_DAILY);
 			db.execSQL(TRIGGER_SCHEDULE_WEEKLY);
 			db.execSQL(TRIGGER_SCHEDULE_MONTHLY);
 			db.execSQL(TRIGGER_SCHEDULE_YEARLY);
 			db.execSQL(TRIGGER_AUTO_UPDATE_DATE);
 
 		}
 
 		public void onOpen(SQLiteDatabase db){
 			super.onOpen(db);
 			db.execSQL("PRAGMA foreign_keys=ON;");
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 
 			// Kills the table and existing data
 			//db.execSQL("DROP TABLE IF EXISTS "+ DATABASE_NAME);
 			db.execSQL("DROP TABLE IF EXISTS "+ WordServantContract.ScriptureEntry.TABLE_NAME);
 			db.execSQL("DROP TABLE IF EXISTS "+ WordServantContract.TagEntry.TABLE_NAME);
 			db.execSQL("DROP TABLE IF EXISTS "+ WordServantContract.CategoryEntry.TABLE_NAME);
 
 			// Recreates the database with a new version
 			onCreate(db);
 		}
 	}
 
 	@Override
 	public int delete(Uri uri, String where, String[] whereArgs) {
 		// Opens the database object in "write" mode.
 		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
 		String finalWhere;
 
 		int count;
 
 		// Does the delete based on the incoming URI pattern.
 		switch (sUriMatcher.match(uri)) {
 
 		// If the incoming pattern matches the general pattern for notes, does a delete
 		// based on the incoming "where" columns and arguments.
 		case SCRIPTURES:
 			count = db.delete(
 					WordServantContract.ScriptureEntry.TABLE_NAME,  // The database table name
 					where,                     // The incoming where clause column names
 					whereArgs                  // The incoming where clause values
 					);
 			break;
 
 			// If the incoming URI matches a single note ID, does the delete based on the
 			// incoming data, but modifies the where clause to restrict it to the
 			// particular note ID.
 		case SCRIPTURE_ID:
 			/*
 			 * Starts a final WHERE clause by restricting it to the
 			 * desired note ID.
 			 */
 			finalWhere =
 			WordServantContract.ScriptureEntry._ID +                              // The ID column name
 			" = " +                                          // test for equality
 			uri.getPathSegments().                           // the incoming note ID
 			get(WordServantContract.ScriptureEntry.SCRIPTURE_ID_PATH_POSITION)
 			;
 
 			// If there were additional selection criteria, append them to the final
 			// WHERE clause
 			if (where != null) {
 				finalWhere = finalWhere + " AND " + where;
 			}
 
 			// Performs the delete.
 			count = db.delete(
 					WordServantContract.ScriptureEntry.TABLE_NAME,  // The database table name.
 					finalWhere,                // The final WHERE clause
 					whereArgs                  // The incoming where clause values.
 					);
 			break;
 
 			// If the incoming pattern is invalid, throws an exception.
 		default:
 			throw new IllegalArgumentException("Unknown URI " + uri);
 		}
 
 		/*Gets a handle to the content resolver object for the current context, and notifies it
 		 * that the incoming URI changed. The object passes this along to the resolver framework,
 		 * and observers that have registered themselves for the provider are notified.
 		 */
 		getContext().getContentResolver().notifyChange(uri, null);
 
 		// Returns the number of rows deleted.
 		return count;
 	}
 
 	@Override
 	public String getType(Uri uri) {
 		return null;
 	}
 
 	@Override
 	public Uri insert(Uri uri, ContentValues initialValues) {
 
 		// Validates the incoming URI. Only the full provider URI is allowed for inserts.
 		if (sUriMatcher.match(uri) != SCRIPTURES) {
 			throw new IllegalArgumentException("Unknown URI " + uri);
 		}
 
 		// A map to hold the new record's values.
 		ContentValues values;
 
 		// If the incoming values map is not null, uses it for the new values.
 		if (initialValues != null) {
 			values = new ContentValues(initialValues);
 
 		} else {
 			// Otherwise, create a new value map
 			values = new ContentValues();
 		}
 
 		// Opens the database object in "write" mode.
 		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
 
 		// Performs the insert and returns the ID of the new note.
 		long rowId = db.insert(
 				WordServantContract.ScriptureEntry.TABLE_NAME,        // The table to insert into.
 				null,  // A hack, SQLite sets this column value to null
 				// if values is empty.
 				values                           // A map of column names, and the values to insert
 				// into the columns.
 				);
 
 		// If the insert succeeded, the row ID exists.
 		if (rowId > 0) {
 			// Creates a URI with the note ID pattern and the new row ID appended to it.
 			Uri noteUri = ContentUris.withAppendedId(WordServantContract.ScriptureEntry.CONTENT_ID_URI_BASE, rowId);
 
 			// Notifies observers registered against this provider that the data changed.
 			getContext().getContentResolver().notifyChange(noteUri, null);
 			return noteUri;
 		}
 
 		// If the insert didn't succeed, then the rowID is <= 0. Throws an exception.
 		throw new SQLException("Failed to insert row into " + uri);
 	}
 
 	@Override
 	public boolean onCreate() {
 		mOpenHelper = new WordServantDatabaseHelper(getContext());
 		mOpenHelper.getWritableDatabase();
 		return true;
 	}
 
 	@Override
 	public Cursor query(Uri uri, String[] projection, String selection,
 			String[] selectionArgs, String sortOrder) {
 		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
 		qb.setTables(WordServantContract.ScriptureEntry.TABLE_NAME);
 		qb.setProjectionMap(sScripturesProjectionMap);
		if(sUriMatcher.match(uri)==SCRIPTURE_ID || sUriMatcher.match(uri)==INCREMENT_TIMES_REVIEWED){
 			qb.appendWhere(WordServantContract.ScriptureEntry._ID+
 					"="+
 					uri.getPathSegments().get(WordServantContract.ScriptureEntry.SCRIPTURE_ID_PATH_POSITION));
 		}
 		// Opens the database object in "read" mode, since no writes need to be done.
 		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
 
 		/*
 		 * Performs the query. If no problems occur trying to read the database, then a Cursor
 		 * object is returned; otherwise, the cursor variable contains null. If no records were
 		 * selected, then the Cursor object is empty, and Cursor.getCount() returns 0.
 		 */
 		Cursor c = qb.query(
 				db,            // The database to query
 				projection,    // The columns to return from the query
 				selection,     // The columns for the where clause
 				selectionArgs, // The values for the where clause
 				null,          // don't group the rows
 				null,          // don't filter by row groups
 				null        // The sort order
 				);
 
 		// Tells the Cursor what URI to watch, so it knows when its source data changes
 		c.setNotificationUri(getContext().getContentResolver(), uri);
 		return c;
 	}
 
 	@Override
 	public int update(Uri uri, ContentValues values, String where,
 			String[] whereArgs) {
 
 		// Opens the database object in "write" mode.
 		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
 		int count;
 		String finalWhere;
 		String [] timesReviewedProjection = {
 				WordServantContract.ScriptureEntry._ID,
 				WordServantContract.ScriptureEntry.COLUMN_NAME_TIMES_REVIEWED
 		};
 
 		Cursor cursor;
 		String scriptureId;
 		// Does the update based on the incoming URI pattern
 		switch (sUriMatcher.match(uri)) {
 
 		// If the incoming URI matches the general notes pattern, does the update based on
 		// the incoming data.
 		case SCRIPTURES:
 
 			// Does the update and returns the number of rows updated.
 			count = db.update(
 					WordServantContract.ScriptureEntry.TABLE_NAME, // The database table name.
 					values,                   // A map of column names and new values to use.
 					where,                    // The where clause column names.
 					whereArgs                 // The where clause column values to select on.
 					);
 			break;
 
 			// If the incoming URI matches a single note ID, does the update based on the incoming
 			// data, but modifies the where clause to restrict it to the particular note ID.
 		case SCRIPTURE_ID:
 			// From the incoming URI, get the note ID
 			scriptureId = uri.getPathSegments().get(WordServantContract.ScriptureEntry.SCRIPTURE_ID_PATH_POSITION);
 
 			/*
 			 * Starts creating the final WHERE clause by restricting it to the incoming
 			 * note ID.
 			 */
 			finalWhere =
 					WordServantContract.ScriptureEntry._ID +                              // The ID column name
 					" = " +                                          // test for equality
 					uri.getPathSegments().                           // the incoming note ID
 					get(WordServantContract.ScriptureEntry.SCRIPTURE_ID_PATH_POSITION)
 					;
 
 			// If there were additional selection criteria, append them to the final WHERE
 			// clause
 			if (where !=null) {
 				finalWhere = finalWhere + " AND " + where;
 			}
 
 
 			// Does the update and returns the number of rows updated.
 			count = db.update(
 					WordServantContract.ScriptureEntry.TABLE_NAME, // The database table name.
 					values,                   // A map of column names and new values to use.
 					finalWhere,               // The final WHERE clause to use
 					// placeholders for whereArgs
 					whereArgs                 // The where clause column values to select on, or
 					// null if the values are in the where argument.
 					);
 			break;
 			// If the incoming pattern is invalid, throws an exception.
 			
 		case INCREMENT_TIMES_REVIEWED:
 			scriptureId = uri.getPathSegments().get(WordServantContract.ScriptureEntry.SCRIPTURE_ID_PATH_POSITION);
 
 			/*
 			 * Starts creating the final WHERE clause by restricting it to the incoming
 			 * note ID.
 			 */
 			finalWhere =
 					WordServantContract.ScriptureEntry._ID +                              // The ID column name
 					" = " +                                          // test for equality
 					uri.getPathSegments().                           // the incoming note ID
 					get(WordServantContract.ScriptureEntry.SCRIPTURE_ID_PATH_POSITION)
 					;
 
 			// If there were additional selection criteria, append them to the final WHERE
 			// clause
 			if (where !=null) {
 				finalWhere = finalWhere + " AND " + where;
 			}
 			
 			// Does the update and returns the number of rows updated.
 			cursor = this.query(uri, timesReviewedProjection, null, null, null);
 			cursor.moveToFirst();
 			values = new ContentValues();
 			values.put(WordServantContract.ScriptureEntry.COLUMN_NAME_TIMES_REVIEWED, cursor.getInt(cursor.getColumnIndex(WordServantContract.ScriptureEntry.COLUMN_NAME_TIMES_REVIEWED))+1);
 
 			count = db.update(
 					WordServantContract.ScriptureEntry.TABLE_NAME, // The database table name.
 					values,                   // A map of column names and new values to use.
 					finalWhere,               // The final WHERE clause to use
 					// placeholders for whereArgs
 					whereArgs                 // The where clause column values to select on, or
 					// null if the values are in the where argument.
 					);
 			break;
 
 			// If the incoming URI matches a single note ID, does the update based on the incoming
 			// data, but modifies the where clause to restrict it to the particular note ID.
 		case DECREMENT_TIMES_REVIEWED:
 			scriptureId = uri.getPathSegments().get(WordServantContract.ScriptureEntry.SCRIPTURE_ID_PATH_POSITION);
 
 			/*
 			 * Starts creating the final WHERE clause by restricting it to the incoming
 			 * note ID.
 			 */
 			finalWhere =
 					WordServantContract.ScriptureEntry._ID +                              // The ID column name
 					" = " +                                          // test for equality
 					uri.getPathSegments().                           // the incoming note ID
 					get(WordServantContract.ScriptureEntry.SCRIPTURE_ID_PATH_POSITION)
 					;
 
 			// If there were additional selection criteria, append them to the final WHERE
 			// clause
 			if (where !=null) {
 				finalWhere = finalWhere + " AND " + where;
 			}
 			
 			
 			// Does the update and returns the number of rows updated.
 
 			cursor = this.query(uri, timesReviewedProjection, null, null, null);
 			cursor.moveToFirst();
 			values = new ContentValues();
 			values.put(WordServantContract.ScriptureEntry.COLUMN_NAME_TIMES_REVIEWED, cursor.getInt(cursor.getColumnIndex(WordServantContract.ScriptureEntry.COLUMN_NAME_TIMES_REVIEWED))-1);
 
 			count = db.update(
 					WordServantContract.ScriptureEntry.TABLE_NAME, // The database table name.
 					values,                   // A map of column names and new values to use.
 					finalWhere,               // The final WHERE clause to use
 					// placeholders for whereArgs
 					whereArgs                 // The where clause column values to select on, or
 					// null if the values are in the where argument.
 					);
 			break;
 			// If the incoming pattern is invalid, throws an exception.
 		default:
 			throw new IllegalArgumentException("Unknown URI " + uri);
 		}
 
 		/*Gets a handle to the content resolver object for the current context, and notifies it
 		 * that the incoming URI changed. The object passes this along to the resolver framework,
 		 * and observers that have registered themselves for the provider are notified.
 		 */
 		getContext().getContentResolver().notifyChange(uri, null);
 
 		// Returns the number of rows updated.
 		return count;
 	}
 
 }
