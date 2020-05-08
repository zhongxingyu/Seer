 package com.jewellbiz.android.jewellbiz.data;
 
 import android.content.ContentProvider;
 import android.content.ContentResolver;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteConstraintException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteQueryBuilder;
 import android.net.Uri;
 import android.text.TextUtils;
 import android.util.Log;
 
 public class JbzProvider extends ContentProvider {
 	
 	private JbzDatabase mDB;
 	
	private static final String AUTHORITY = "com.jewellbiz.android.jewellbiztoday.data.JbzProvider";
 	
 	public static final int ARTICLES = 100;
 	public static final int ARTICLE_ID = 110;
 	
 	private static final String ARTICLE_BASE_PATH = "articles";
 	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
 			+ "/" + ARTICLE_BASE_PATH);
 	
 	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
 			+ "/jbz-articles";
 	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
 			+ "/jbz-articles";
 	
 	private static final UriMatcher sURIMatcher = new UriMatcher(
 	        UriMatcher.NO_MATCH);
 	
 	private static final String DEBUG_TAG = "JbzProvider";
 	static {
 		sURIMatcher.addURI(AUTHORITY, ARTICLE_BASE_PATH, ARTICLES);
 	    sURIMatcher.addURI(AUTHORITY, ARTICLE_BASE_PATH + "/#", ARTICLE_ID);
 	}
 	
 	@Override
 	public boolean onCreate() {
 		mDB = new JbzDatabase(getContext());
 		return true;
 	}
 	
 	@Override
 	public Cursor query(Uri uri, String[] projection, String selection,
 			String[] selectionArgs, String sortOrder) {
 		
 		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
 		queryBuilder.setTables(JbzDatabase.TABLE_ARTICLES);
 		
 		int uriType = sURIMatcher.match(uri);
 		switch (uriType) {
 		case ARTICLE_ID:
 			queryBuilder.appendWhere(JbzDatabase.ID + "=" + uri.getLastPathSegment());
 			break;
 		case ARTICLES:
 			// no filter
 			break;
 		default:
 			throw new IllegalArgumentException("Unknown URI");
 		}
 		
 		Cursor cursor = queryBuilder.query(mDB.getReadableDatabase(), 
 				projection, selection, selectionArgs, null, null, sortOrder);
 		cursor.setNotificationUri(getContext().getContentResolver(), uri);
 		
 		return cursor;
 	}
 	
 
 	@Override
 	public int delete(Uri uri, String selection, String[] selectionArgs) {
 		int uriType = sURIMatcher.match(uri);
 		SQLiteDatabase sqlDB = mDB.getWritableDatabase();
 		int rowsAffected = 0;
 		switch (uriType) {
 		case ARTICLES:
 			rowsAffected = sqlDB.delete(JbzDatabase.TABLE_ARTICLES, selection, selectionArgs);
 			break;
 		case ARTICLE_ID:
 			String id = uri.getLastPathSegment();
 			if (TextUtils.isEmpty(selection)) {
 				rowsAffected = sqlDB.delete(JbzDatabase.TABLE_ARTICLES, 
 						JbzDatabase.ID + "=" + id, null);
 			} else {
 				rowsAffected = sqlDB.delete(JbzDatabase.TABLE_ARTICLES, 
 						selection + " and " + JbzDatabase.ID + "=" + id, selectionArgs);
 			}
 			break;
 		default:
 			throw new IllegalArgumentException("Unknown or Invalid URI " + uri);
 		}
 		getContext().getContentResolver().notifyChange(uri, null);
 		return rowsAffected;
 	}
 
 	@Override
 	public String getType(Uri uri) {
 		int uriType = sURIMatcher.match(uri);
 		switch (uriType) {
 		case ARTICLES:
 			return CONTENT_TYPE;
 		case ARTICLE_ID:
 			return CONTENT_ITEM_TYPE;
 		default:
 			return null;
 		}
 	}
 
 	@Override
 	public Uri insert(Uri uri, ContentValues values) {
 		int uriType = sURIMatcher.match(uri);
 		if (uriType != ARTICLES) {
 			throw new IllegalArgumentException("Invalid URI for insert");
 		}
 		SQLiteDatabase sqlDB = mDB.getWritableDatabase();
 		try {
 			long newID = sqlDB.insertOrThrow(JbzDatabase.TABLE_ARTICLES, null, values);
 			if (newID > 0) {
 				Uri newUri = ContentUris.withAppendedId(uri, newID);
 	            getContext().getContentResolver().notifyChange(uri, null);
 	            return newUri;
 			} else {
 	            throw new SQLException("Failed to insert row into " + uri);
 	        }
 		} catch (SQLiteConstraintException e) {
 			Log.i(DEBUG_TAG, "Ignoring constraint failure.");
 		}
 		return null;
 	}
 
 	@Override
 	public int update(Uri uri, ContentValues values, String selection,
 			String[] selectionArgs) {
 		int uriType = sURIMatcher.match(uri);
 		SQLiteDatabase sqlDB = mDB.getWritableDatabase();
 		
 		int rowsAffected;
 		
 		switch (uriType) {
 		case ARTICLE_ID:
 			String id = uri.getLastPathSegment();
 			StringBuilder modSelection = new StringBuilder(JbzDatabase.ID + "=" + id);
 			
 			if (!TextUtils.isEmpty(selection)) {
 				modSelection.append(" AND " + selection);
 			}
 			
 			rowsAffected = sqlDB.update(JbzDatabase.TABLE_ARTICLES, values, modSelection.toString(), null);
 			
 			break;
 		case ARTICLES:
 			rowsAffected = sqlDB.update(JbzDatabase.TABLE_ARTICLES, values, selection, selectionArgs);
 			break;
 		default:
 			throw new IllegalArgumentException("Unknown URI");
 			
 		}
 		
 		getContext().getContentResolver().notifyChange(uri, null);
 		return rowsAffected;
 	}
 	
 	// Helper to mark all items (articles) in the table as read
 		public static void markAllItemsRead(Context context) {
 			ContentValues values = new ContentValues();
 			values.put(JbzDatabase.COL_READ, "1");
 			int updated = context.getContentResolver().update(CONTENT_URI, values, 
 					JbzDatabase.COL_READ + "='0'", null);
 			Log.d(DEBUG_TAG, "Rows updated: " + updated);
 		}
 		
 		// mark a single item, reference by Uri, as read
 		public static void markItemRead(Context context, long item) {
 			Uri viewedArticle = Uri.withAppendedPath(JbzProvider.CONTENT_URI, 
 					String.valueOf(item));
 			ContentValues values = new ContentValues();
 			values.put(JbzDatabase.COL_READ, "1");
 			int updated = context.getContentResolver().update(viewedArticle, values,
 					null, null);
 			Log.d(DEBUG_TAG, updated + " rows updated. Marked " + item
 					+ " as read.");
 		}
 	
 
 }
