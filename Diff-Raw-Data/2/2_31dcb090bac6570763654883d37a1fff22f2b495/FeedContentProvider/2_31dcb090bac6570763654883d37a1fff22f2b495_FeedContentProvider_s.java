 package ru.rutube.RutubeAPI.content;
 
 import android.content.ContentProvider;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteQueryBuilder;
 import android.net.Uri;
 import android.provider.BaseColumns;
 import android.util.Log;
 
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Сергей
  * Date: 05.05.13
  * Time: 13:05
  * To change this template use File | Settings | File Templates.
  */
 public class FeedContentProvider extends ContentProvider {
     private static final String DBNAME = "rutube";
     private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
     private static final int EDITORS = 1;
     private static final int EDITORS_FEEDITEM = 2;
     private static final int MY_VIDEO = 3;
     private static final int MY_VIDEO_FEEDITEM = 4;
     private static final int SUBSCRIPTION = 5;
     private static final int SUBSCRIPTION_FEEDITEM = 6;
     private static final int SEARCH_RESULTS = 7;
     private static final int SEARCH_RESULTS_FEEDITEM = 8;
     private static final int SEARCH_QUERY = 9;
     private static final int SEARCH_QUERY_ITEM = 10;
     private static final String LOG_TAG = FeedContentProvider.class.getName();
 
     public static final String AUTHORITY = FeedContentProvider.class.getName();
 
     static {
         sUriMatcher.addURI(AUTHORITY, FeedContract.Editors.CONTENT_PATH, EDITORS);
         sUriMatcher.addURI(AUTHORITY, FeedContract.Editors.CONTENT_PATH + "/#", EDITORS_FEEDITEM);
         sUriMatcher.addURI(AUTHORITY, FeedContract.MyVideo.CONTENT_PATH, MY_VIDEO);
         sUriMatcher.addURI(AUTHORITY, FeedContract.MyVideo.CONTENT_PATH + "/#", MY_VIDEO_FEEDITEM);
         sUriMatcher.addURI(AUTHORITY, FeedContract.Subscriptions.CONTENT_PATH, SUBSCRIPTION);
         sUriMatcher.addURI(AUTHORITY, FeedContract.Subscriptions.CONTENT_PATH + "/#", SUBSCRIPTION_FEEDITEM);
         sUriMatcher.addURI(AUTHORITY, FeedContract.SearchResults.CONTENT_PATH + "/#", SEARCH_RESULTS);
         sUriMatcher.addURI(AUTHORITY, FeedContract.SearchResults.CONTENT_PATH + "/#/#", SEARCH_RESULTS_FEEDITEM);
         sUriMatcher.addURI(AUTHORITY, FeedContract.SearchQuery.CONTENT_PATH, SEARCH_QUERY);
         sUriMatcher.addURI(AUTHORITY, FeedContract.SearchQuery.CONTENT_PATH + "/#", SEARCH_QUERY_ITEM);
     }
 
     private MainDatabaseHelper dbHelper;
 
     private SQLiteDatabase db;
 
 
     @Override
     public boolean onCreate() {
         dbHelper = new MainDatabaseHelper(getContext());
 
         return true;
     }
 
     @Override
     public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
         // Using SQLiteQueryBuilder instead of query() method
         SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
 
 
 
         int uriType = sUriMatcher.match(uri);
         // Check if the caller has requested a column which does not exists
         checkColumns(projection, uriType);
         switch (uriType) {
             case EDITORS:
                 queryBuilder.setTables(FeedContract.Editors.CONTENT_PATH);
                 break;
             case EDITORS_FEEDITEM:
                 queryBuilder.setTables(FeedContract.Editors.CONTENT_PATH);
                 queryBuilder.appendWhere(FeedContract.FeedColumns._ID + "="
                         + uri.getLastPathSegment());
                 break;
             case MY_VIDEO:
                 queryBuilder.setTables(FeedContract.MyVideo.CONTENT_PATH);
                 break;
             case MY_VIDEO_FEEDITEM:
                 queryBuilder.setTables(FeedContract.MyVideo.CONTENT_PATH);
                 queryBuilder.appendWhere(FeedContract.FeedColumns._ID + "="
                         + uri.getLastPathSegment());
                 break;
             case SUBSCRIPTION:
                 queryBuilder.setTables(FeedContract.Subscriptions.CONTENT_PATH);
                 break;
             case SUBSCRIPTION_FEEDITEM:
                 queryBuilder.setTables(FeedContract.Subscriptions.CONTENT_PATH);
                 queryBuilder.appendWhere(FeedContract.FeedColumns._ID + "="
                         + uri.getLastPathSegment());
                 break;
             case SEARCH_RESULTS:
                 queryBuilder.setTables(FeedContract.SearchResults.CONTENT_PATH);
                 List<String> pathSegments = uri.getPathSegments();
                 assert pathSegments != null;
                 String queryId = pathSegments.get(2);
                 assert queryId != null;
                 queryBuilder.appendWhere(FeedContract.SearchResults.QUERY_ID + "="
                         + queryId);
             case SEARCH_RESULTS_FEEDITEM:
                 queryBuilder.setTables(FeedContract.SearchResults.CONTENT_PATH);
                 queryBuilder.appendWhere(FeedContract.FeedColumns._ID + "="
                         + uri.getLastPathSegment());
                 break;
             case SEARCH_QUERY:
                 queryBuilder.setTables(FeedContract.SearchQuery.CONTENT_PATH);
                 break;
             case SEARCH_QUERY_ITEM:
                 queryBuilder.setTables(FeedContract.SearchQuery.CONTENT_PATH);
                 queryBuilder.appendWhere(BaseColumns._ID + "="
                         + uri.getLastPathSegment());
                 break;
             default:
                 throw new IllegalArgumentException("Unknown URI: " + uri);
         }
 
         SQLiteDatabase db = dbHelper.getWritableDatabase();
         if (sortOrder == null) {
             sortOrder = FeedContract.FeedColumns.CREATED + " DESC";
         }
         Log.d(LOG_TAG, "ORDER BY: " + sortOrder);
         Cursor cursor = queryBuilder.query(db, projection, selection,
                 selectionArgs, null, null, sortOrder);
         // Make sure that potential listeners are getting notified
         Log.d(LOG_TAG, "context: " + getContext().getContentResolver());
         cursor.setNotificationUri(getContext().getContentResolver(), uri);
 
         return cursor;
     }
 
     @Override
     public String getType(Uri uri) {
         int match = sUriMatcher.match(uri);
         switch (match) {
             case EDITORS:
                 return FeedContract.Editors.CONTENT_TYPE;
             case MY_VIDEO:
                 return FeedContract.MyVideo.CONTENT_TYPE;
             case SUBSCRIPTION:
                 return FeedContract.Subscriptions.CONTENT_TYPE;
             case SEARCH_RESULTS:
                 return FeedContract.SearchResults.CONTENT_TYPE;
             case SEARCH_QUERY:
                 return FeedContract.SearchQuery.CONTENT_TYPE;
             default:
                 return null;
         }
     }
 
     @Override
     public Uri insert(Uri uri, ContentValues contentValues) {
         int uriType = sUriMatcher.match(uri);
         SQLiteDatabase sqlDB = dbHelper.getWritableDatabase();
         switch (uriType) {
             case EDITORS:
                 sqlDB.replace(FeedContract.Editors.CONTENT_PATH, null, contentValues);
                 break;
             case MY_VIDEO:
                 sqlDB.replace(FeedContract.MyVideo.CONTENT_PATH, null, contentValues);
                 break;
             case SUBSCRIPTION:
                 sqlDB.replace(FeedContract.Subscriptions.CONTENT_PATH, null, contentValues);
                 break;
             case SEARCH_RESULTS:
                 sqlDB.replace(FeedContract.SearchResults.CONTENT_PATH, null, contentValues);
             case SEARCH_QUERY:
                 sqlDB.replace(FeedContract.SearchQuery.CONTENT_PATH, null, contentValues);
             default:
                 throw new IllegalArgumentException("Unknown URI: " + uri);
         }
         getContext().getContentResolver().notifyChange(uri, null);
 
         switch (uriType) {
             case SEARCH_RESULTS:
                 return Uri.withAppendedPath(FeedContract.Editors.CONTENT_URI,
                     "/" + contentValues.getAsString(FeedContract.SearchResults.QUERY_ID) +
                     "/" + contentValues.getAsString(FeedContract.FeedColumns._ID));
             default:
                 return Uri.withAppendedPath(FeedContract.Editors.CONTENT_URI,
                         "/" + contentValues.getAsString(FeedContract.FeedColumns._ID));
         }
     }
 
     @Override
     public int bulkInsert(Uri uri, ContentValues[] values) {
         Log.d(LOG_TAG, "start bulk insert");
         int numInserted = 0;
         if (values.length == 0) {
             Log.d(LOG_TAG, "empty bulk insert");
             return 0;
         }
         String table;
         int uriType = sUriMatcher.match(uri);
         checkColumns(values[0], uriType);
 
         switch (uriType) {
             case EDITORS:
                 table = FeedContract.Editors.CONTENT_PATH;
                 break;
             case MY_VIDEO:
                 table = FeedContract.MyVideo.CONTENT_PATH;
                 break;
             case SUBSCRIPTION:
                 table = FeedContract.Subscriptions.CONTENT_PATH;
                 break;
             case SEARCH_RESULTS:
                 table = FeedContract.SearchResults.CONTENT_PATH;
                 break;
             case SEARCH_QUERY:
                 table = FeedContract.SearchQuery.CONTENT_PATH;
                 break;
             default:
                 throw new IllegalArgumentException("Unknown URI: " + uri);
         }
         SQLiteDatabase sqlDB = dbHelper.getWritableDatabase();
         sqlDB.beginTransaction();
         try {
             for (ContentValues cv : values) {
                 long newID = sqlDB.replace(table, null, cv);
                 if (newID <= 0) {
                     throw new SQLException("Failed to insert row into " + uri);
                 }
             }
             sqlDB.setTransactionSuccessful();
             numInserted = values.length;
         } finally {
             sqlDB.endTransaction();
         }
         //getContext().getContentResolver().notifyChange(uri, null);
         Log.d(LOG_TAG, "end bulk insert");
         return numInserted;
     }
 
     @Override
     public int delete(Uri uri, String s, String[] strings) {
         getContext().getContentResolver().notifyChange(uri, null);
         return 0;
     }
 
     @Override
     public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
         return 0;
     }
 
     private void checkColumns(ContentValues values, int uriType) {
         // ContentValues.keySet in 2.3 just stops the whole thread without exception
         if (values != null) {
             HashSet<String> requestedColumns = new HashSet<String>();
             for (Map.Entry<String, Object> entry : values.valueSet()) {
                 requestedColumns.add(entry.getKey());
             }
             checkColumns(requestedColumns, uriType);
         }
     }
 
     private void checkColumns(HashSet<String> requestedColumns, int uriType) {
         String[] columnList = getProjection(uriType);
 
         HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(columnList));
         if (!availableColumns.containsAll(requestedColumns)) {
             throw new IllegalArgumentException("Unknown columns in projection");
         }
 
     }
 
     public static String[] getProjection(Uri contentUri) {
         int uriType = sUriMatcher.match(contentUri);
         return getProjection(uriType);
     }
     public static String[] getProjection(int uriType) {
         if (uriType == SEARCH_QUERY || uriType == SEARCH_QUERY_ITEM) {
             return new String[]{
                     BaseColumns._ID,
                     FeedContract.SearchQuery.QUERY,
                     FeedContract.SearchQuery.UPDATED
             };
         }
         String[] available = {
                 FeedContract.FeedColumns._ID,
                 FeedContract.FeedColumns.TITLE,
                 FeedContract.FeedColumns.DESCRIPTION,
                 FeedContract.FeedColumns.CREATED,
                 FeedContract.FeedColumns.THUMBNAIL_URI,
                 FeedContract.FeedColumns.AUTHOR_ID,
                 FeedContract.FeedColumns.AUTHOR_NAME,
                 FeedContract.FeedColumns.AVATAR_URI
         };
 
         ArrayList<String> columnList = new ArrayList<String>(Arrays.asList(available));
         if (uriType == MY_VIDEO || uriType == MY_VIDEO_FEEDITEM)
             columnList.add(FeedContract.MyVideo.SIGNATURE);
         if (uriType == SEARCH_RESULTS || uriType == SEARCH_RESULTS_FEEDITEM) {
             columnList.add(FeedContract.SearchResults.QUERY_ID);
             columnList.add(FeedContract.SearchResults.POSITION);
         }
 
         String[] result = new String[columnList.size()];
         return columnList.toArray(result);
     }
 
     private void checkColumns(String[] values, int uriType) {
         HashSet<String> requestedColumns = new HashSet<String>();
         if (values != null) {
             Collections.addAll(requestedColumns, values);
             checkColumns(requestedColumns, uriType);
         }
     }
 
     private static final String SEARCH_QUERY_COLUMNS_SQL =
             " _id INTEGER PRIMARY KEY AUTOINCREMENT," +
             " query VARCHAR(100)," +
             " updated DATETIME";
 
     private static final String FEED_COLUMNS_SQL =
             " _id VARCHAR(32) PRIMARY KEY," +
             " title VARCHAR(255)," +
             " description VARCHAR(1024)," +
             " thumbnail_url VARCHAR(255)," +
             " created DATETIME," +
             " author_id INTEGER NULL," +
             " author_name VARCHAR(120)," +
             " avatar_url VARCHAR(255)";
 
     private static final String MY_VIDEO_COLUMNS_SQL =
             FEED_COLUMNS_SQL + "," +
                     " signature VARCHAR(30) NULL";
 
     private static final String SEARCH_RESULTS_COLUMNS_SQL =
             FEED_COLUMNS_SQL + "," +
                     " query_id INTEGER NOT NULL," +
                     " position INTEGER NOT NULL";
 
     private static final String SQL_CREATE_VIDEO_EDITORS = "CREATE TABLE " +
             FeedContract.Editors.CONTENT_PATH + " (" +
             FEED_COLUMNS_SQL + ")";
 
     private static final String SQL_CREATE_VIDEO_MY_VIDEO = "CREATE TABLE " +
             FeedContract.MyVideo.CONTENT_PATH + " (" +
             MY_VIDEO_COLUMNS_SQL + ")";
 
     private static final String SQL_CREATE_VIDEO_SUBSCRIPTION = "CREATE TABLE " +
             FeedContract.Subscriptions.CONTENT_PATH + " (" +
             FEED_COLUMNS_SQL + ")";
 
     private static final String SQL_CREATE_SEARCH_RESULTS = "CREATE TABLE " +
             FeedContract.SearchResults.CONTENT_PATH + " (" +
             SEARCH_RESULTS_COLUMNS_SQL + ")";
 
    private static final String SQL_CREATE_SEARCH_QUERY = "CREATE TABLE" +
             FeedContract.SearchQuery.CONTENT_PATH + " (" +
             SEARCH_QUERY_COLUMNS_SQL + ")";
 
     protected static final class MainDatabaseHelper extends SQLiteOpenHelper {
 
 
         /**
          * Instantiates an open helper for the provider's SQLite data repository
          * Do not do database creation and upgrade here.
          */
         MainDatabaseHelper(Context context) {
             super(context, DBNAME, null, 1);
         }
 
         public void onCreate(SQLiteDatabase db) {
             Log.d(LOG_TAG, "Creating database");
             db.execSQL(SQL_CREATE_VIDEO_EDITORS);
             db.execSQL(SQL_CREATE_VIDEO_MY_VIDEO);
             db.execSQL(SQL_CREATE_VIDEO_SUBSCRIPTION);
             db.execSQL(SQL_CREATE_SEARCH_RESULTS);
             db.execSQL(SQL_CREATE_SEARCH_QUERY);
         }
 
         @Override
         public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
             //To change body of implemented methods use File | Settings | File Templates.
         }
     }
 
 }
