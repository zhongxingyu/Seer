 package com.ouchadam.podcast.provider;
 
 import android.content.ContentProvider;
 import android.content.ContentValues;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteQueryBuilder;
 import android.net.Uri;
 import android.text.TextUtils;
 import com.ouchadam.podcast.database.BaseTable;
 import com.ouchadam.podcast.database.DatabaseHelper;
 
 import java.util.Arrays;
 import java.util.HashSet;
 
 public class FeedProvider extends ContentProvider {
 
     private DatabaseHelper database;
 
     private static final int FEEDS = 10;
     private static final int FEED_TABLE_ID = 20;
 
    private static final String AUTHORITY = "com.adam.podcast.provider.FeedProvider";
 
     public static final String BASE_PATH = "feeds";
     public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);
 
     private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
     static {
         sURIMatcher.addURI(AUTHORITY, BASE_PATH, FEEDS);
         sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", FEED_TABLE_ID);
     }
 
     @Override
     public boolean onCreate() {
         database = new DatabaseHelper(getContext());
         return false;
     }
 
     @Override
     public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
         SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
 
         checkIfColumnsExist(projection);
         queryBuilder.setTables(BaseTable.TABLE_FEED);
         addIdToQuery(uri, queryBuilder);
 
         SQLiteDatabase db = database.getWritableDatabase();
         Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
         cursor.setNotificationUri(getContext().getContentResolver(), uri);
 
         return cursor;
     }
 
     private void addIdToQuery(Uri uri, SQLiteQueryBuilder queryBuilder) {
         switch (sURIMatcher.match(uri)) {
             case FEEDS:
                 break;
             case FEED_TABLE_ID:
                 queryBuilder.appendWhere(BaseTable.COLUMN_ID + "=" + uri.getLastPathSegment());
                 break;
             default:
                 throw new IllegalArgumentException("Unknown URI: " + uri);
         }
     }
 
     @Override
     public String getType(Uri uri) {
         return null;
     }
 
     @Override
     public Uri insert(Uri uri, ContentValues values) {
         int uriType = sURIMatcher.match(uri);
         SQLiteDatabase sqlDB = database.getWritableDatabase();
         long id = 0;
         switch (uriType) {
             case FEEDS:
                 id = sqlDB.insert(BaseTable.TABLE_FEED, null, values);
                 break;
             default:
                 throw new IllegalArgumentException("Unknown URI: " + uri);
         }
 
         //Log.d(Log.TAG, "URI : " + uri);
 
         getContext().getContentResolver().notifyChange(uri, null);
         return Uri.parse(BASE_PATH + "/" + id);
     }
 
     @Override
     public int delete(Uri uri, String selection, String[] selectionArgs) {
         int uriType = sURIMatcher.match(uri);
         SQLiteDatabase sqlDB = database.getWritableDatabase();
         int rowsDeleted = 0;
         switch (uriType) {
             case FEEDS:
                 rowsDeleted = sqlDB.delete(BaseTable.TABLE_FEED, selection, selectionArgs);
                 break;
             case FEED_TABLE_ID:
                 String id = uri.getLastPathSegment();
                 if (TextUtils.isEmpty(selection)) {
                     rowsDeleted = sqlDB.delete(BaseTable.TABLE_FEED, BaseTable.COLUMN_ID + "=" + id, null);
                 } else {
                     rowsDeleted = sqlDB.delete(BaseTable.TABLE_FEED, BaseTable.COLUMN_ID + "=" + id + " and " + selection,
                             selectionArgs);
                 }
                 break;
             default:
                 throw new IllegalArgumentException("Unknown URI: " + uri);
         }
         getContext().getContentResolver().notifyChange(uri, null);
         return rowsDeleted;
     }
 
     @Override
     public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
 
         int uriType = sURIMatcher.match(uri);
         SQLiteDatabase sqlDB = database.getWritableDatabase();
         int rowsUpdated = 0;
         switch (uriType) {
             case FEEDS:
                 rowsUpdated = sqlDB.update(BaseTable.TABLE_FEED, values, selection, selectionArgs);
                 break;
             case FEED_TABLE_ID:
                 String id = uri.getLastPathSegment();
                 if (TextUtils.isEmpty(selection)) {
                     rowsUpdated = sqlDB.update(BaseTable.TABLE_FEED, values, BaseTable.COLUMN_ID + "=" + id, null);
                 } else {
                     rowsUpdated = sqlDB.update(BaseTable.TABLE_FEED, values, BaseTable.COLUMN_ID + "=" + id + " and "
                             + selection, selectionArgs);
                 }
                 break;
             default:
                 throw new IllegalArgumentException("Unknown URI: " + uri);
         }
         getContext().getContentResolver().notifyChange(uri, null);
         return rowsUpdated;
     }
 
     private void checkIfColumnsExist(String[] projection) {
         String[] available = {
                 BaseTable.COLUMN_ID,
                 BaseTable.COLUMN_ITEM_IMAGE_URL,
                 BaseTable.COLUMN_ITEM_TITLE,
                 BaseTable.COLUMN_ITEM_AUDIO_URL,
                 BaseTable.COLUMN_ITEM_DATE,
                 BaseTable.COLUMN_ITEM_DETAILS };
 
         if (projection != null) {
             HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
             HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
 
             if (!availableColumns.containsAll(requestedColumns)) {
                 throw new IllegalArgumentException("Unknown columns in projection");
             }
         }
     }
 
 }
