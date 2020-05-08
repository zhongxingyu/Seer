 /*******************************************************************************
  * Copyright (c) 2012 rmateus.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v2.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
  ******************************************************************************/
 package cm.aptoide.pt;
 
 
 import android.content.ContentProvider;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteQueryBuilder;
 import android.net.Uri;
 
 public class ExtrasContentProvider extends ContentProvider {
 
 	// database
 	private ExtrasDbOpenHelper database;
 
 	// Used for the UriMacher
 	private static final int TODOS = 10;
 	private static final int TODO_ID = 20;
 
	private static final String AUTHORITY = "extras.contentprovider.beta";
 
 	private static final String BASE_PATH = "todos";
 	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
 			+ "/" + BASE_PATH);
 
 	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
 			+ "/todos";
 	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
 			+ "/todo";
 
 	private static final UriMatcher sURIMatcher = new UriMatcher(
 			UriMatcher.NO_MATCH);
 	static {
 		sURIMatcher.addURI(AUTHORITY, BASE_PATH, TODOS);
 		sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", TODO_ID);
 	}
 
 	@Override
 	public boolean onCreate() {
 		database = new ExtrasDbOpenHelper(getContext());
 		return false;
 	}
 	
 	
 	
 	@Override
 	public int bulkInsert(Uri uri, ContentValues[] values) {
 		int numInserted = 0;
 		 SQLiteDatabase sqlDB = database.getWritableDatabase();
 		    sqlDB.beginTransaction();
 		    try {
 		    	System.out.println("Inserted");
 		        for (ContentValues cv : values) {
 		            long newID = sqlDB.insertOrThrow(ExtrasDbOpenHelper.TABLE_COMMENTS, null, cv);
 		            sqlDB.yieldIfContendedSafely();
 		            Thread.sleep(100);
 		            if (newID <= 0) {
 		                throw new SQLException("Failed to insert row into " + uri);
 		            }
 		        }
 		        getContext().getContentResolver().notifyChange(uri, null);
 		        numInserted = values.length;
 		        }
 		        catch (Exception e) {
 		        	e.printStackTrace();				
 		    } finally {
 		    	sqlDB.setTransactionSuccessful();
 		        sqlDB.endTransaction();
 		    }
 
 		return numInserted;
 	}
 
 
 
 	@Override
 	public Cursor query(Uri uri, String[] projection, String selection,
 			String[] selectionArgs, String sortOrder) {
 
 		// Uisng SQLiteQueryBuilder instead of query() method
 		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
 
 
 		// Set the table
 		queryBuilder.setTables(ExtrasDbOpenHelper.TABLE_COMMENTS);
 
 		int uriType = sURIMatcher.match(uri);
 		switch (uriType) {
 		case TODOS:
 			break;
 		default:
 			throw new IllegalArgumentException("Unknown URI: " + uri);
 		}
 
 		SQLiteDatabase db = database.getWritableDatabase();
 		Cursor cursor = queryBuilder.query(db, projection, selection,
 				selectionArgs, null, null, sortOrder);
 		// Make sure that potential listeners are getting notified
 		cursor.setNotificationUri(getContext().getContentResolver(), uri);
 
 		return cursor;
 	}
 
 	@Override
 	public String getType(Uri uri) {
 		return null;
 	}
 
 	@Override
 	public Uri insert(Uri uri, ContentValues values) {
 		int uriType = sURIMatcher.match(uri);
 		SQLiteDatabase sqlDB = database.getWritableDatabase();
 		int rowsDeleted = 0;
 		long id = 0;
 		switch (uriType) {
 		case TODOS:
 			id = sqlDB.insert(ExtrasDbOpenHelper.TABLE_COMMENTS, null, values);
 			System.out.println("Inserted");
 			break;
 		default:
 			throw new IllegalArgumentException("Unknown URI: " + uri);
 		}
 		getContext().getContentResolver().notifyChange(uri, null);
 		return Uri.parse(BASE_PATH + "/" + id);
 	}
 
 	@Override
 	public int delete(Uri uri, String selection, String[] selectionArgs) {
 		int uriType = sURIMatcher.match(uri);
 		SQLiteDatabase sqlDB = database.getWritableDatabase();
 		int rowsDeleted = 0;
 		switch (uriType) {
 		case TODOS:
 			rowsDeleted = sqlDB.delete(ExtrasDbOpenHelper.TABLE_COMMENTS, selection,
 					selectionArgs);
 			break;
 		default:
 			throw new IllegalArgumentException("Unknown URI: " + uri);
 		}
 		getContext().getContentResolver().notifyChange(uri, null);
 		return rowsDeleted;
 	}
 
 	@Override
 	public int update(Uri uri, ContentValues values, String selection,
 			String[] selectionArgs) {
 
 		return 0;
 	}
 	
 
 
 }
