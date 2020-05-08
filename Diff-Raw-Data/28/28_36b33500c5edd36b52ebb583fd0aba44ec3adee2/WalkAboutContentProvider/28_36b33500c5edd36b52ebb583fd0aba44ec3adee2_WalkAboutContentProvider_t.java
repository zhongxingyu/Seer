 package com.kristyandkyle.walkabout.providers;
 
 import java.sql.SQLException;
 
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteDatabase.CursorFactory;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteQueryBuilder;
 import android.net.Uri;
 import android.provider.BaseColumns;
 import android.text.TextUtils;
 
 import com.kristyandkyle.walkabout.utils.FileHandlerFactory;
 import com.kristyandkyle.walkabout.utils.RESTfulContentProvider;
 import com.kristyandkyle.walkabout.utils.ResponseHandler;
 
 public class WalkAboutContentProvider extends RESTfulContentProvider {
 	public static final String WALKABOUT = "walkabout";
     public static final String DATABASE_NAME = WALKABOUT + ".db";
     static int DATABASE_VERSION = 6;
 
     public static final String WALKABOUTS_TABLE_NAME = "walkabouts";
     public static final String PATHS_TABLE_NAME = "paths";
     public static final String WAYPOINTS_TABLE_NAME = "waypoints";
     public static final String WALKABOUTS_WAYPOINTS_TABLE_NAME = "walkabouts_waypoints";
 	
 	private static final String FILE_CACHE_DIR = "/data/data/com.kristyandkyle.walkabout/file_cache";
 	
 	private static final int WALKABOUTS = 1;
 	private static final int SPECIFIC_WALKABOUT = 2;
 	private static final int PATHS = 3;
 	private static final int SPECIFIC_PATH = 4;
 	private static final int WAYPOINTS = 5;
	private static final int WAYPOINTS_BY_PATH = 6;
 	
 	private static UriMatcher sUriMatcher;
 	
 	static {
 		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
 		sUriMatcher.addURI(WalkAbout.AUTHORITY, WalkAbout.WalkAbouts.WALKABOUTS, WALKABOUTS);
 		sUriMatcher.addURI(WalkAbout.AUTHORITY, WalkAbout.WalkAbouts.WALKABOUT, SPECIFIC_WALKABOUT);
 		sUriMatcher.addURI(WalkAbout.AUTHORITY, WalkAbout.Paths.PATHS, PATHS);
 		sUriMatcher.addURI(WalkAbout.AUTHORITY, WalkAbout.Paths.PATH, SPECIFIC_PATH);
 		sUriMatcher.addURI(WalkAbout.AUTHORITY, WalkAbout.Waypoints.WAYPOINTS_NAME, WAYPOINTS);
 	}
 	
 	private DatabaseHelper mOpenHelper;
     private SQLiteDatabase mDb; 
     
     private static class DatabaseHelper extends SQLiteOpenHelper {
 
 		public DatabaseHelper(Context context, String name, CursorFactory factory) {
 			super(context, name, factory, DATABASE_VERSION);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			createTable(db);
 		}
 
 		private void createTable(SQLiteDatabase db) {
 			String createWalkAboutTable = "CREATE TABLE " + WALKABOUTS_TABLE_NAME + "(" + 
 					BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
 					WalkAbout.WalkAbouts.PATH_ID + " TEXT, " +
 					WalkAbout.WalkAbouts.DURATION + " TEXT, " +
 					WalkAbout.WalkAbouts.TIMESTAMP + " TEXT, " +
 					WalkAbout.WalkAbouts._DATA + " TEXT UNIQUE" + 
 					");";
 			
 			db.execSQL(createWalkAboutTable);
 			
 			String createPathsTable = "CREATE TABLE " + PATHS_TABLE_NAME + "(" +
 					BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
 					WalkAbout.Paths.ID + " TEXT, " +
 					WalkAbout.Paths.NAME + " TEXT, " +
 					WalkAbout.Paths.DISTANCE + " TEXT, " +
 					WalkAbout.Paths._DATA + " TEXT UNIQUE" + 
 					");";
 			
 			db.execSQL(createPathsTable);
 			
 			String createWaypointsTable = "CREATE TABLE " + WAYPOINTS_TABLE_NAME + "(" +
 					BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
 					WalkAbout.Waypoints.ID + " TEXT, " +
 					WalkAbout.Waypoints.PATH_ID + " TEXT, " +
 					WalkAbout.Waypoints.NAME + " TEXT, " +
 					WalkAbout.Waypoints.LATITUDE + " TEXT, " +
 					WalkAbout.Waypoints.LONGITUDE + " TEXT " + 
 					");";
 			
 			db.execSQL(createWaypointsTable);
 			
 			String createWalkAboutsWaypointsTable = "CREATE TABLE " + WALKABOUTS_WAYPOINTS_TABLE_NAME + "(" +
 					BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
 					WalkAbout.WalkAbouts_Waypoints.WALKABOUT_ID + " TEXT, " +  
 					WalkAbout.WalkAbouts_Waypoints.WAYPOINT_ID + " TEXT " +
 					");";
 			
 			db.execSQL(createWalkAboutsWaypointsTable);
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			db.execSQL("DROP TABLE IF EXISTS " + WALKABOUTS_TABLE_NAME + ";");
 			db.execSQL("DROP TABLE IF EXISTS " + PATHS_TABLE_NAME + ";");
 			db.execSQL("DROP TABLE IF EXISTS " + WAYPOINTS_TABLE_NAME + ";");
 			db.execSQL("DROP TABLE IF EXISTS " + WALKABOUTS_WAYPOINTS_TABLE_NAME + ";");
 			createTable(db);
 		}
     	
     }
 
 	public WalkAboutContentProvider() {
 		super(new FileHandlerFactory(FILE_CACHE_DIR));
 	}
 	
 	public WalkAboutContentProvider(Context context) {
         super(new FileHandlerFactory(FILE_CACHE_DIR));
         init();
     }
 	
 	@Override
 	public boolean onCreate() {
 		init();
 		return true;
 	}
 
 	private void init() {
 		mOpenHelper = new DatabaseHelper(getContext(), DATABASE_NAME, null);
 		mDb = mOpenHelper.getWritableDatabase();
 		mFileHandlerFactory = new FileHandlerFactory(FILE_CACHE_DIR);
 	}
 	
 	@Override
 	public SQLiteDatabase getDatabase() {
 		return mDb;
 	}
 	
 	@Override
 	protected ResponseHandler newResponseHandler(String requestTag) {
 		return new WalkAboutHandler(this, requestTag);
 	}
 	
 	@Override
 	public String getType(Uri uri) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	@Override
 	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
 		Cursor queryCursor;
 		
 		int match = sUriMatcher.match(uri);
 		switch (match) {
 			case WALKABOUTS:
 				if (TextUtils.isEmpty(sortOrder)) {
 					sortOrder = "_id DESC";
 				}
 				
 				SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
				
 				queryBuilder.setTables(WALKABOUTS_TABLE_NAME + " LEFT OUTER JOIN " + PATHS_TABLE_NAME + 
						" ON " + WALKABOUTS_TABLE_NAME + "." + WalkAbout.WalkAbouts.PATH_ID + " = " + PATHS_TABLE_NAME + "." + WalkAbout.Paths.ID);
 				
 				queryCursor = queryBuilder.query(mDb,
 						projection,
 						selection,
 						selectionArgs,
 						null,
 						null,
 						sortOrder);
 								
 				//queryCursor.setNotificationUri(getContext().getContentResolver(), uri);
 				//asyncQueryRequest("walkabout", "http://kristyandkyle.com/walkabout/assets/scripts/paths.json");
 				break;
 				
 			case SPECIFIC_WALKABOUT:
 				selection = "_id = " + uri.getLastPathSegment();
 				
 				queryCursor = mDb.query(WALKABOUTS_TABLE_NAME,
 						projection,
 						selection,
 						selectionArgs,
 						null,
 						null,
 						sortOrder);
 				break;
 			
 			case PATHS:
 				queryCursor = mDb.query(PATHS_TABLE_NAME,
 						projection,
 						selection,
 						selectionArgs,
 						null,
 						null,
 						sortOrder);
 				
 				queryCursor.setNotificationUri(getContext().getContentResolver(), uri);
 				asyncQueryRequest("paths", "http://kristyandkyle.com/walkabout/assets/scripts/paths.json");
 				break;
 				
 			case SPECIFIC_PATH:
				selection = "id = " + uri.getLastPathSegment(); 
 				
 				queryCursor = mDb.query(PATHS_TABLE_NAME,
 						projection,
 						selection,
 						selectionArgs,
 						null,
 						null,
 						sortOrder);
 				break;
				
			case WAYPOINTS_BY_PATH:
				selection = "id = " + uri.getLastPathSegment();
				
				queryCursor = mDb.query(WAYPOINTS_TABLE_NAME,
						projection,
						selection,
						selectionArgs,
						null,
						null,
						sortOrder);
				break;
 	
 			default:
 				throw new IllegalArgumentException("unsupported uri");
 		}
 		
 		return queryCursor;
 	}
 	
 	@Override
 	public Uri insert(Uri uri, ContentValues values) {
 		long rowId = mDb.insert(WALKABOUTS_TABLE_NAME, WalkAbout.WalkAbouts.WALKABOUTS, values);
 		
 		asyncPostRequest("walkabouts", "http://kristyandkyle.com/walkabout/somethingelse", values);
 		
 		if (rowId >= 0) {
 			Uri insertUri = ContentUris.withAppendedId(WalkAbout.WalkAbouts.CONTENT_URI, rowId);
 			return insertUri;
 		}
 		
 		return null;
 	}
 
 	@Override
 	public Uri insert(Uri uri, ContentValues values, SQLiteDatabase db) {
 		int match = sUriMatcher.match(uri);		
 		Uri returnContentUri = null;
 		
 		switch (match) {
 			case PATHS:
 				returnContentUri = insertPath(values, db);
 				break;
 				
 			case WAYPOINTS:
 				returnContentUri = insertWaypoint(values, db);
 				break;
 	
 			default:
 				break;
 		}
 		
 		return returnContentUri;
 	}
 
 	@Override
 	public int bulkInsert(Uri uri, ContentValues[] values) {
 		int match = sUriMatcher.match(uri);
 		int inserted = 0;
 		
 		switch (match) {
 			case PATHS:
 				inserted = insertPaths(uri, values, mDb);
 				break;
 				
 			case WAYPOINTS:
 				inserted = insertWaypoints(uri, values, mDb);
 				break;
 				
 			default:
 				break;
 		}
 		
 		return inserted;
 	}
 
 	@Override
 	public int delete(Uri uri, String selection, String[] selectionArgs) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public int update(Uri uri, ContentValues values, String selection,
 			String[] selectionArgs) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 	
 	private Uri insertPath(ContentValues values, SQLiteDatabase db) {
 		String itemId = (String) values.get(WalkAbout.Paths.ID);
 		Long rowID = itemExists(db, WalkAbout.Paths.ID, itemId, PATHS_TABLE_NAME);
 		
 		if (rowID == null) {
 			long rowId = db.insert(PATHS_TABLE_NAME, WalkAbout.Paths.PATHS, values);
 			
 			if (rowId >= 0) {
 				Uri insertUri = ContentUris.withAppendedId(WalkAbout.Paths.CONTENT_URI, rowId);
 				getContext().getContentResolver().notifyChange(insertUri, null);
 				return insertUri;
 			}
 		}
 		
 		return ContentUris.withAppendedId(WalkAbout.Paths.CONTENT_URI, rowID);
 	}
 	
 	private int insertPaths(Uri uri, ContentValues[] values, SQLiteDatabase db) {
 		int inserted = 0;
 		db.beginTransaction();
 		
 		try {
 			for (ContentValues value : values) {
 				String itemId = (String) value.get(WalkAbout.Paths.ID);
 				Long rowID = itemExists(db, WalkAbout.Paths.ID, itemId, PATHS_TABLE_NAME);
 				
 				if (rowID == null) {
 					long newId = db.insertOrThrow(PATHS_TABLE_NAME, null, value);
 					
 					if (newId <= 0) {
 						throw new SQLException("Failed to insert row");
 					} else {
 						Uri insertUri = ContentUris.withAppendedId(WalkAbout.Paths.CONTENT_URI, newId);
 						getContext().getContentResolver().notifyChange(insertUri, null);
 						inserted++;
 					}
 				}
 			}
 			
 			db.setTransactionSuccessful();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			db.endTransaction();
 		}
 		
 		return inserted;		
 	}
 	
 	private Uri insertWaypoint(ContentValues values, SQLiteDatabase db) {
 		String itemId = (String) values.get(WalkAbout.Waypoints.ID);
 		Long rowID = itemExists(db, WalkAbout.Waypoints.ID, itemId, WAYPOINTS_TABLE_NAME);
 		
 		if (rowID == null) {
 			long rowId = db.insert(WAYPOINTS_TABLE_NAME, WalkAbout.Waypoints.WAYPOINTS_NAME, values);
 			
 			if (rowId >= 0) {
 				Uri insertUri = ContentUris.withAppendedId(WalkAbout.Waypoints.CONTENT_URI, rowId);
 				getContext().getContentResolver().notifyChange(insertUri, null);
 				return insertUri;
 			}
 		}
 		
 		return ContentUris.withAppendedId(WalkAbout.Waypoints.CONTENT_URI, rowID);
 	}
 	
 	private int insertWaypoints(Uri uri, ContentValues[] values, SQLiteDatabase db) {
 		int inserted = 0;
 		db.beginTransaction();
 		
 		try {
 			for (ContentValues value : values) {
 				String itemId = (String) value.get(WalkAbout.Waypoints.ID);
 				Long rowID = itemExists(db, WalkAbout.Waypoints.ID, itemId, WAYPOINTS_TABLE_NAME);
 				
 				if (rowID == null) {
 					long newId = db.insertOrThrow(WAYPOINTS_TABLE_NAME, null, value);
 					
 					if (newId <= 0) {
 						throw new SQLException("Failed to insert row");
 					} else {
 						Uri insertUri = ContentUris.withAppendedId(WalkAbout.Waypoints.CONTENT_URI, newId);
 						getContext().getContentResolver().notifyChange(insertUri, null);
 						inserted++;
 					}
 				}
 			}
 			
 			db.setTransactionSuccessful();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			db.endTransaction();
 		}
 		
 		return inserted;	
 	}
 	
 	private Long itemExists(SQLiteDatabase db, String columnName, String itemId, String tableName) {
 		Cursor cursor = null;
 		Long rowId = null;
 		
 		try {
 			cursor = db.query(tableName,
 					null,
 					columnName + "= '" + itemId + "'",
 					null,
 					null,
 					null,
 					null);
 			
 			if (cursor.moveToFirst()) {
 				rowId = cursor.getLong(0);
 			} 
 		} finally {
 			if (cursor != null) {
 				cursor.close();
 			}
 		}
 		
 		return rowId;
 	}
 
 }
