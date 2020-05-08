 package com.kosbrother.youtubefeeder.database;
 
 import java.util.HashMap;
 
 import com.kosbrother.youtubefeeder.MainActivity;
 
 import android.content.ContentProvider;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteQueryBuilder;
 import android.net.Uri;
 import android.support.v4.database.DatabaseUtilsCompat;
 import android.text.TextUtils;
 
 
 public class DatabaseProvider extends ContentProvider {
     // A projection map used to select columns from the database, for video
     private final HashMap<String, String> mNotesProjectionMap;
     // A projection map used to select columns from the database, for video
     private final HashMap<String, String> mChannelsProjectionMap;
     // Uri matcher to decode incoming URIs.
     private final UriMatcher mUriMatcher;
     
 
     // The incoming URI matches the main table URI pattern, for Video
     private static final int MAIN = 1;
     // The incoming URI matches the main table row ID URI pattern, for Video
     private static final int MAIN_ID = 2;
     // The incoming URI matches the main table URI pattern, for Channel
     private static final int CHANNEL = 3;
     // The incoming URI matches the main table row ID URI pattern, for Channel
     private static final int CHANNEL_ID = 4;
 
     // Handle to a new DatabaseHelper.
     private DatabaseHelper mOpenHelper;
 
     /**
      * Global provider initialization.
      */
     public DatabaseProvider() {
         // Create and initialize URI matcher.
         mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
         mUriMatcher.addURI(MainActivity.AUTHORITY, VideoTable.TABLE_NAME, MAIN);
         mUriMatcher.addURI(MainActivity.AUTHORITY, VideoTable.TABLE_NAME + "/#", MAIN_ID);
         mUriMatcher.addURI(MainActivity.AUTHORITY, ChannelTable.TABLE_NAME, CHANNEL);
         mUriMatcher.addURI(MainActivity.AUTHORITY, ChannelTable.TABLE_NAME + "/#", CHANNEL_ID);
 
         // Create and initialize projection map for all columns.  This is
         // simply an identity mapping.
         mNotesProjectionMap = new HashMap<String, String>();
         mNotesProjectionMap.put(VideoTable._ID, VideoTable._ID);
         mNotesProjectionMap.put(VideoTable.COLUMN_NAME_DATA1, VideoTable.COLUMN_NAME_DATA1);
         mNotesProjectionMap.put(VideoTable.COLUMN_NAME_DATA2, VideoTable.COLUMN_NAME_DATA2);
         mNotesProjectionMap.put(VideoTable.COLUMN_NAME_DATA3, VideoTable.COLUMN_NAME_DATA3);
         mNotesProjectionMap.put(VideoTable.COLUMN_NAME_DATA4, VideoTable.COLUMN_NAME_DATA4);
         mNotesProjectionMap.put(VideoTable.COLUMN_NAME_DATA5, VideoTable.COLUMN_NAME_DATA5);
         mNotesProjectionMap.put(VideoTable.COLUMN_NAME_DATA6, VideoTable.COLUMN_NAME_DATA6);
         mNotesProjectionMap.put(VideoTable.COLUMN_NAME_DATA7, VideoTable.COLUMN_NAME_DATA7);
         mNotesProjectionMap.put(VideoTable.COLUMN_NAME_DATA8, VideoTable.COLUMN_NAME_DATA8);
         
         mChannelsProjectionMap = new HashMap<String, String>();
         mChannelsProjectionMap.put(ChannelTable._ID, ChannelTable._ID);
         mChannelsProjectionMap.put(ChannelTable.COLUMN_NAME_DATA1, ChannelTable.COLUMN_NAME_DATA1);
         mChannelsProjectionMap.put(ChannelTable.COLUMN_NAME_DATA2, ChannelTable.COLUMN_NAME_DATA2);
         mChannelsProjectionMap.put(ChannelTable.COLUMN_NAME_DATA3, ChannelTable.COLUMN_NAME_DATA3);
         mChannelsProjectionMap.put(ChannelTable.COLUMN_NAME_DATA4, ChannelTable.COLUMN_NAME_DATA4);
     }
 
     /**
      * Perform provider creation.
      */
     @Override
     public boolean onCreate() {
         mOpenHelper = new DatabaseHelper(getContext());
         // Assumes that any failures will be reported by a thrown exception.
         return true;
     }
 
     /**
      * Handle incoming queries.
      */
     @Override
     public Cursor query(Uri uri, String[] projection, String selection,
             String[] selectionArgs, String sortOrder) {
 
         // Constructs a new query builder and sets its table name
         SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
         
 
         switch (mUriMatcher.match(uri)) {
             case MAIN:
                 // If the incoming URI is for main table.
             	qb.setTables(VideoTable.TABLE_NAME);
                 qb.setProjectionMap(mNotesProjectionMap);
                 if (TextUtils.isEmpty(sortOrder)) {
                     sortOrder = VideoTable.DEFAULT_SORT_ORDER;
                 }
                 break;
 
             case MAIN_ID:
                 // The incoming URI is for a single row.
             	qb.setTables(VideoTable.TABLE_NAME);
                 qb.setProjectionMap(mNotesProjectionMap);
                 qb.appendWhere(VideoTable._ID + "=?");
                 selectionArgs = DatabaseUtilsCompat.appendSelectionArgs(selectionArgs,
                         new String[] { uri.getLastPathSegment() });
                 if (TextUtils.isEmpty(sortOrder)) {
                     sortOrder = VideoTable.DEFAULT_SORT_ORDER;
                 }
                 break;
             case CHANNEL:
             	qb.setTables(ChannelTable.TABLE_NAME);
             	qb.setProjectionMap(mChannelsProjectionMap);
             	if (TextUtils.isEmpty(sortOrder)) {
                     sortOrder = ChannelTable.DEFAULT_SORT_ORDER;
                 }
             	break;
             case CHANNEL_ID:
             	qb.setTables(ChannelTable.TABLE_NAME);
             	qb.setProjectionMap(mChannelsProjectionMap);
                 qb.appendWhere(ChannelTable._ID + "=?");
                 selectionArgs = DatabaseUtilsCompat.appendSelectionArgs(selectionArgs,
                         new String[] { uri.getLastPathSegment() });
                 if (TextUtils.isEmpty(sortOrder)) {
                     sortOrder = ChannelTable.DEFAULT_SORT_ORDER;
                 }
             	break;
 
             default:
                 throw new IllegalArgumentException("Unknown URI " + uri);
         }
 
         SQLiteDatabase db = mOpenHelper.getReadableDatabase();
 
         Cursor c = qb.query(db, projection, selection, selectionArgs,
                 null /* no group */, null /* no filter */, sortOrder);
 
         c.setNotificationUri(getContext().getContentResolver(), uri);
         return c;
     }
 
     /**
      * Return the MIME type for an known URI in the provider.
      */
     @Override
     public String getType(Uri uri) {
         switch (mUriMatcher.match(uri)) {
             case MAIN:
                 return VideoTable.CONTENT_TYPE;
             case MAIN_ID:
                 return VideoTable.CONTENT_ITEM_TYPE;
             case CHANNEL:
             	return ChannelTable.CONTENT_TYPE;
             case CHANNEL_ID:
             	return ChannelTable.CONTENT_ITEM_TYPE;
             default:
                 throw new IllegalArgumentException("Unknown URI " + uri);
         }
     }
 
     /**
      * Handler inserting new data.
      */
     @Override
     public Uri insert(Uri uri, ContentValues initialValues) {
         if (mUriMatcher.match(uri) != MAIN && mUriMatcher.match(uri) != CHANNEL) {
             // Can only insert into to main URI.
             throw new IllegalArgumentException("Unknown URI " + uri);
         }
         
         if (mUriMatcher.match(uri) == MAIN){
         	ContentValues values;
 
             if (initialValues != null) {
                 values = new ContentValues(initialValues);
             } else {
                 values = new ContentValues();
             }
             
             // 如果傳進來的 initialValues 是 null, 讓資料為 ""
             if (values.containsKey(VideoTable.COLUMN_NAME_DATA1) == false) {
                 values.put(VideoTable.COLUMN_NAME_DATA1, "");
             }
 
             SQLiteDatabase db = mOpenHelper.getWritableDatabase();
 
             long rowId = db.insert(VideoTable.TABLE_NAME, null, values);
 
             // If the insert succeeded, the row ID exists.
             if (rowId > 0) {
                 Uri noteUri = ContentUris.withAppendedId(VideoTable.CONTENT_ID_URI_BASE, rowId);
                 getContext().getContentResolver().notifyChange(noteUri, null);
                 return noteUri;
             }
         }else if(mUriMatcher.match(uri) == CHANNEL){
         	ContentValues values;
 
             if (initialValues != null) {
                 values = new ContentValues(initialValues);
             } else {
                 values = new ContentValues();
             }
             
             // 如果傳進來的 initialValues 是 null, 讓資料為 ""
             if (values.containsKey(ChannelTable.COLUMN_NAME_DATA1) == false) {
                 values.put(ChannelTable.COLUMN_NAME_DATA1, "");
             }
 
             SQLiteDatabase db = mOpenHelper.getWritableDatabase();
 
             long rowId = db.insert(ChannelTable.TABLE_NAME, null, values);
 
             // If the insert succeeded, the row ID exists.
             if (rowId > 0) {
                 Uri noteUri = ContentUris.withAppendedId(ChannelTable.CONTENT_ID_URI_BASE, rowId);
                 getContext().getContentResolver().notifyChange(noteUri, null);
                 return noteUri;
             }
         }
         
 
         throw new SQLException("Failed to insert row into " + uri);
     }
 
     /**
      * Handle deleting data.
      */
     @Override
     public int delete(Uri uri, String where, String[] whereArgs) {
         SQLiteDatabase db = mOpenHelper.getWritableDatabase();
         String finalWhere;
 
         int count;
 
         switch (mUriMatcher.match(uri)) {
             case MAIN:
                 // If URI is main table, delete uses incoming where clause and args.
                 count = db.delete(VideoTable.TABLE_NAME, where, whereArgs);
                 break;
 
                 // If the incoming URI matches a single note ID, does the delete based on the
                 // incoming data, but modifies the where clause to restrict it to the
                 // particular note ID.
             case MAIN_ID:
                 // If URI is for a particular row ID, delete is based on incoming
                 // data but modified to restrict to the given ID.
                 finalWhere = DatabaseUtilsCompat.concatenateWhere(
                 		VideoTable._ID + " = " + ContentUris.parseId(uri), where);
                 count = db.delete(VideoTable.TABLE_NAME, finalWhere, whereArgs);
                 break;
             case CHANNEL:
                 // If URI is main table, delete uses incoming where clause and args.
                 count = db.delete(ChannelTable.TABLE_NAME, where, whereArgs);
                 break;
 
                 // If the incoming URI matches a single note ID, does the delete based on the
                 // incoming data, but modifies the where clause to restrict it to the
                 // particular note ID.
             case CHANNEL_ID:
                 // If URI is for a particular row ID, delete is based on incoming
                 // data but modified to restrict to the given ID.
                 finalWhere = DatabaseUtilsCompat.concatenateWhere(
                 		VideoTable._ID + " = " + ContentUris.parseId(uri), where);
                 count = db.delete(ChannelTable.TABLE_NAME, finalWhere, whereArgs);
                 break;
 
             default:
                 throw new IllegalArgumentException("Unknown URI " + uri);
         }
 
         getContext().getContentResolver().notifyChange(uri, null);
 
         return count;
     }
 
     /**
      * Handle updating data.
      */
     @Override
     public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
         SQLiteDatabase db = mOpenHelper.getWritableDatabase();
         int count;
         String finalWhere;
 
         switch (mUriMatcher.match(uri)) {
             case MAIN:
                 // If URI is main table, update uses incoming where clause and args.
                 count = db.update(VideoTable.TABLE_NAME, values, where, whereArgs);
                 break;
 
             case MAIN_ID:
                 // If URI is for a particular row ID, update is based on incoming
                 // data but modified to restrict to the given ID.
                 finalWhere = DatabaseUtilsCompat.concatenateWhere(
                 		VideoTable._ID + " = " + ContentUris.parseId(uri), where);
                 count = db.update(VideoTable.TABLE_NAME, values, finalWhere, whereArgs);
                 break;
             case CHANNEL:
                 // If URI is main table, update uses incoming where clause and args.
                 count = db.update(ChannelTable.TABLE_NAME, values, where, whereArgs);
                 break;
 
             case CHANNEL_ID:
                 // If URI is for a particular row ID, update is based on incoming
                 // data but modified to restrict to the given ID.
                 finalWhere = DatabaseUtilsCompat.concatenateWhere(
                 		VideoTable._ID + " = " + ContentUris.parseId(uri), where);
                 count = db.update(ChannelTable.TABLE_NAME, values, finalWhere, whereArgs);
                 break;    
 
             default:
                 throw new IllegalArgumentException("Unknown URI " + uri);
         }
 
         getContext().getContentResolver().notifyChange(uri, null);
 
         return count;
     }
 }
