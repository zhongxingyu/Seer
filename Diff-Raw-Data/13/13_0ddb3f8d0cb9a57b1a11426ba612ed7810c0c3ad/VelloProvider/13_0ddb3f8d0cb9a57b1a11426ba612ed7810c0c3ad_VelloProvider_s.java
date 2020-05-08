 package com.mili.xiaominglui.app.vello.data.provider;
 
 import android.content.ContentProvider;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.net.Uri;
 import android.provider.BaseColumns;
 import android.util.Log;
 
 import com.mili.xiaominglui.app.vello.config.VelloConfig;
 import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbWordCard;
 
 public class VelloProvider extends ContentProvider {
     private static final String TAG = VelloProvider.class.getSimpleName();
     
     protected static final String DATABASE_NAME = "vello.db";
     public static final String AUTHORITY = "com.mili.xiaominglui.app.vello.provider.VelloProvider";
     
     public static final int DATABASE_VERSION = 1;
     private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
     private enum UriType {
         DB_WORD_CARD(DbWordCard.TABLE_NAME, DbWordCard.TABLE_NAME, DbWordCard.TYPE_ELEM_TYPE),
         DB_WORD_CARD_ID_IN_LOCAL_DB(DbWordCard.TABLE_NAME + "/#", DbWordCard.TABLE_NAME, DbWordCard.TYPE_DIR_TYPE),
         DB_ALL(null, null, DbWordCard.TYPE_DIR_TYPE);
         
         private String mTableName;
         private String mType;
         
         UriType(String matchPath, String tableName, String type) {
             mTableName = tableName;
             mType = type;
             sUriMatcher.addURI(AUTHORITY, matchPath, ordinal());
         }
         
         String getTableName() {
             return mTableName;
         }
         
         String getType() {
             return mType;
         }
     }
     
     static {
         // Ensures UriType is initialized
         UriType.values();
     }
     
     private static UriType matchUri(Uri uri) {
         int match = sUriMatcher.match(uri);
         if (match < 0) {
             throw new IllegalArgumentException("Unknown URI " + uri);
         }
         return UriType.class.getEnumConstants()[match];
     }
 
     private SQLiteDatabase mDatabase;
     
     @SuppressWarnings("deprecation")
     public synchronized SQLiteDatabase getDatabase(Context context) {
         // Always return the cached database, if we've got one
         if (mDatabase == null || !mDatabase.isOpen()) {
             DatabaseHelper helper = new DatabaseHelper(context, DATABASE_NAME);
             mDatabase = helper.getWritableDatabase();
             if (mDatabase != null) {
                 mDatabase.setLockingEnabled(true);
             }
         }
 
         return mDatabase;
     }
     
     private class DatabaseHelper extends SQLiteOpenHelper {
         
         DatabaseHelper(Context context, String name) {
             super(context, name, null, DATABASE_VERSION);
         }
 
         @Override
         public void onCreate(SQLiteDatabase db) {
             Log.d(TAG, "Creating VelloProvider database");
             
             if (VelloConfig.DEBUG_SWITCH) {
                 Log.d(TAG, "DbWordCard | createTable start");
             }
             DbWordCard.createTable(db);
             if (VelloConfig.DEBUG_SWITCH) {
                 Log.d(TAG, "DbWordCard | createTable end");
             }
         }
 
         @Override
         public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
             // Upgrade all tables here; each class has its own method
             if (VelloConfig.DEBUG_SWITCH) {
                 Log.d(TAG, "DbWordCard | upgradeTable start");
             }
             DbWordCard.upgradeTable(db, oldVersion, newVersion);
             if (VelloConfig.DEBUG_SWITCH) {
                 Log.d(TAG, "DbWordCard | upgradeTable end");
             }
         }
     }
 
     @Override
     public boolean onCreate() {
         return true;
     }
 
 	@Override
 	public Cursor query(Uri uri, String[] projection, String selection,
 			String[] selectionArgs, String sortOrder) {
 		Cursor c = null;
 		Uri notificationUri = VelloContent.CONTENT_URI;
 		UriType uriType = matchUri(uri);
 		Context context = getContext();
 		SQLiteDatabase db = getDatabase(context);
 		String id;
 
 		if (VelloConfig.DEBUG_SWITCH) {
 			Log.d(TAG, "query: uri=" + uri + ", match is " + uriType.name()
 					+ ", selectionArgs is  " + selectionArgs[0]);
 		}
 
 		switch (uriType) {
 		case DB_WORD_CARD_ID_IN_LOCAL_DB:
 			id = uri.getPathSegments().get(1);
 			c = db.query(uriType.getTableName(), projection,
 					whereWithId(selection),
 					addIdToSelectionArgs(id, selectionArgs), null, null,
 					sortOrder);
 			break;
 		case DB_WORD_CARD:
 			c = db.query(uriType.getTableName(), projection, selection,
 					selectionArgs, null, null, sortOrder);
 			break;
 		}
 
 		if ((c != null) && !isTemporary()) {
 			c.setNotificationUri(getContext().getContentResolver(),
 					notificationUri);
 		}
 		return c;
 	}
 
     @Override
     public String getType(Uri uri) {
         return matchUri(uri).getType();
     }
 
     @Override
     public Uri insert(Uri uri, ContentValues values) {
         UriType uriType = matchUri(uri);
         Context context = getContext();
         
         SQLiteDatabase db = getDatabase(context);
         long id;
         
         if (VelloConfig.DEBUG_SWITCH) {
             Log.d(TAG, "insert: uri=" + uri + ", match is " + uriType.name());
         }
         
         Uri resultUri;
         
         switch (uriType) {
             case DB_WORD_CARD:
                 id = db.insert(uriType.getTableName(), "foo", values);
                 resultUri = id == -1 ? null : ContentUris.withAppendedId(uri, id);
                 break;
             default:
                 throw new IllegalArgumentException("Unknown URI " + uri);
         }
         getContext().getContentResolver().notifyChange(uri, null);
         return resultUri;
     }
 
     @Override
     public int delete(Uri uri, String selection, String[] selectionArgs) {
         UriType uriType = matchUri(uri);
         
         if (VelloConfig.DEBUG_SWITCH) {
             Log.d(TAG, "delete: uri=" + uri + ", match is " + uriType.name());
         }
         if (uri == VelloContent.CONTENT_URI) {
        	deleteDatabase();
         	return 1;
         }
         
         Context context = getContext();
         SQLiteDatabase db = getDatabase(context);
         String id;
         int result = -1;
         switch (uriType) {
             case DB_WORD_CARD_ID_IN_LOCAL_DB:
                 id = uri.getPathSegments().get(1);
                 result = db.delete(uriType.getTableName(), whereWithId(selection), addIdToSelectionArgs(id, selectionArgs));
                 break;
             case DB_WORD_CARD:
                 result = db.delete(uriType.getTableName(), selection, selectionArgs);
                 break;
             case DB_ALL:
             	break;
         }
         
 		getContext().getContentResolver().notifyChange(uri, null);
 		return result;
     }
 
     @Override
     public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
         UriType uriType = matchUri(uri);
         Context context = getContext();
 
         // Pick the correct database for this operation
         SQLiteDatabase db = getDatabase(context);
 
         if (VelloConfig.DEBUG_SWITCH) {
             Log.d(TAG, "update: uri=" + uri + ", match is " + uriType.name());
         }
 
         int result = -1;
 
         switch (uriType) {
             case DB_WORD_CARD_ID_IN_LOCAL_DB:
                 String id = uri.getPathSegments().get(1);
                 result = db.update(uriType.getTableName(), values, whereWithId(selection),
                     addIdToSelectionArgs(id, selectionArgs));
                 break;
             case DB_WORD_CARD:
                 result = db.update(uriType.getTableName(), values, selection, selectionArgs);
                 break;
         }
 
         getContext().getContentResolver().notifyChange(uri, null);
         return result;
     }
     
     private void deleteDatabase() {
     	getContext().deleteDatabase(DATABASE_NAME);
     }
     
     private String whereWithId(String selection) {
         StringBuilder sb = new StringBuilder(256);
         sb.append(BaseColumns._ID);
         sb.append(" = ?");
         if (selection != null) {
             sb.append(" AND (");
             sb.append(selection);
             sb.append(')');
         }
         return sb.toString();
     }
     
     private String[] addIdToSelectionArgs(String id, String[] selectionArgs) {
 
         if (selectionArgs == null) {
             return new String[] { id };
         }
 
         int length = selectionArgs.length;
         String[] newSelectionArgs = new String[length + 1];
         newSelectionArgs[0] = id;
         System.arraycopy(selectionArgs, 0, newSelectionArgs, 1, length);
         return newSelectionArgs;
     }
 }
