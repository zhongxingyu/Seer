 package com.russia.meetster;
 
 import java.util.List;
 
 import android.content.ContentProvider;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteQueryBuilder;
 import android.net.Uri;
 import android.provider.BaseColumns;
 import android.text.TextUtils;
 
 abstract class YLSQLiteOpenHelper extends SQLiteOpenHelper {
 	
 	protected void createTable(SQLiteDatabase db, BaseTableContract contract) {
 		String SQLQuery = "create table " + contract.getTableName() + " ( ";
 		SQLQuery += BaseColumns._ID + " integer primary key autoincrement, ";
 		
 		String[] columns = contract.getColumnTypes();
 		String[] columnTypes = contract.getColumnTypes();
 		
 		for (int i = 0; i < columns.length; i++) {
 			SQLQuery += columns[i] + " " + columnTypes[i];
 			if (i < columns.length - 1) {
 				SQLQuery += ",";
 			}
 			SQLQuery += " ";
 		}
 		SQLQuery += ")";
 		
 		db.execSQL(SQLQuery);
 	}
 	
 	protected BaseTableContract[] contracts;
 
 	public YLSQLiteOpenHelper(Context context, String dbName, int version, BaseTableContract[] contracts) {
 		super(context, dbName, null, version);
 		this.contracts = contracts;
 	}
 	
 	public void onCreate(SQLiteDatabase db) {
 		for (BaseTableContract contract : contracts) {
 			createTable(db, contract);
 		}
 	}
 }
 
 class MeetsterDBOpenHelper extends YLSQLiteOpenHelper {
 	private static final int VERSION = 1;
 	
 	public MeetsterDBOpenHelper(Context context) {
 		super(context, "MeetsterDB", VERSION, new BaseTableContract[] {
 				MeetsterContract.Events,
 				MeetsterContract.Categories,
 				MeetsterContract.Friends,
 		});
 	}
 	
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		return;
 	}
 }
 
 public class MeetsterContentProvider extends ContentProvider {
 	
 	private void notifyURIChange(Uri uri) {
 		getContext().getContentResolver().notifyChange(uri, null);
 	}
 	
 	private static String addIdToWhere(String where, String _ID, Uri uri) {
 		return (TextUtils.isEmpty(where) ? "" : where + " and ") + BaseColumns._ID + "=" + ContentUris.parseId(uri);
 	}
 	
 	private Uri createTableURI(String tableName) {
 		return Uri.withAppendedPath(Uri.parse("content://" + AUTHORITY), tableName);
 	}
 	
 	private Uri createTableIdURI(String tableName, long id) {
 		return ContentUris.withAppendedId(createTableURI(tableName), id);
 	}
 
 	// Path types
 	private static final int EVENT = 1;
 	private static final int EVENT_ID = 2;
 	private static final int CATEGORY = 3;
 	private static final int CATEGORY_ID = 4;
 	private static final int FRIEND = 5;
 	private static final int FRIEND_ID = 6;
 
 	private final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
 	
	private void addMeetsterURI(String authority, String path, int tableCode, int tableIdCode) {
		uriMatcher.addURI(authority, path, tableCode);
		uriMatcher.addURI(authority, path + "/#", tableIdCode);
 	}
 	
 	// Non-static variables initialized in onCreate
 	private String AUTHORITY;
 	private SQLiteOpenHelper sqliteHelper;
 
 	@Override
 	public boolean onCreate() {
 		// Defining the path structure
		AUTHORITY = getContext().getString(R.string.meetster_authority);

		addMeetsterURI(AUTHORITY, MeetsterContract.Events.getTableName(), EVENT, EVENT_ID);
		addMeetsterURI(AUTHORITY, MeetsterContract.Friends.getTableName(), FRIEND, FRIEND_ID);
		addMeetsterURI(AUTHORITY, MeetsterContract.Categories.getTableName(), CATEGORY, CATEGORY_ID);
 		
 		// Setting non-static variables
 		sqliteHelper = new MeetsterDBOpenHelper(getContext());
 		return true;
 	}
 
 	@Override
 	public Cursor query(Uri uri, String[] projection, String selection,
 			String[] selectionArgs, String sortOrder) {
 		SQLiteQueryBuilder qB = new SQLiteQueryBuilder();
 		
 		switch (uriMatcher.match(uri)) {
 		case UriMatcher.NO_MATCH:
 			throw new IllegalArgumentException("Invalid URI for Meetster query: " + uri);
 		default:
 			List<String> pathSegments = uri.getPathSegments();
 			qB.setTables(pathSegments.get(0));
 			if (pathSegments.size() > 1) {
 				qB.appendWhere(BaseColumns._ID + "=" + ContentUris.parseId(uri));
 			}
 			break;
 		}
 		
 		SQLiteDatabase db = sqliteHelper.getReadableDatabase();
 		
 		Cursor c = qB.query(db, projection, selection, selectionArgs, null, null, sortOrder);
 		c.setNotificationUri(getContext().getContentResolver(), uri);
 		return c;
 	}
 	
 	@Override
 	public Uri insert(Uri uri, ContentValues values) {
 		String tableName = uri.getPathSegments().get(0);
 		
 		switch (uriMatcher.match(uri)) {
 		case EVENT:
 			break;
 		case CATEGORY:
 			break;
 		case FRIEND:
 			break;
 		default:
 			throw new IllegalArgumentException("Invalid URI for Meetster insertion: " + uri);
 		}
 		
 		SQLiteDatabase db = sqliteHelper.getWritableDatabase();
 		long newid = db.insert(tableName, null, values);
 		notifyURIChange(uri);
 		return createTableIdURI(tableName, newid);
 	}
 
 	@Override
 	public int update(Uri uri, ContentValues values, String selection,
 			String[] selectionArgs) {
 		String tableName;
 		
 		switch(uriMatcher.match(uri)) {
 		case UriMatcher.NO_MATCH:
 			throw new IllegalArgumentException("Invalid URI for Meetster update: " + uri);
 		default:
 			List<String> pathSegments = uri.getPathSegments();
 			tableName = pathSegments.get(0);
 			if (pathSegments.size() > 1) {
 				addIdToWhere(selection, BaseColumns._ID, uri);
 			}
 			break;
 		}
 		
 		SQLiteDatabase db = sqliteHelper.getWritableDatabase();
 		int updatedRows = db.update(tableName, values, selection, selectionArgs);
 		
 		notifyURIChange(uri);
 		
 		return updatedRows;
 	}
 
 	@Override
 	public int delete(Uri uri, String selection, String[] selectionArgs) {
 		String tableName;
 
 		switch(uriMatcher.match(uri)) {
 		case UriMatcher.NO_MATCH:
 			throw new IllegalArgumentException("Invalid URI for Meetster update: " + uri);
 		default:
 			List<String> pathSegments = uri.getPathSegments();
 			tableName = pathSegments.get(0);
 			if (pathSegments.size() > 1) {
 				addIdToWhere(selection, BaseColumns._ID, uri);
 			}
 			break;
 		}
 
 		SQLiteDatabase db = sqliteHelper.getWritableDatabase();
 		int deletedRows = db.delete(tableName, selection, selectionArgs);
 				
 		notifyURIChange(uri);
 
 		return deletedRows;
 	}
 
 	@Override
 	public String getType(Uri uri) {
 		return null;
 	}
 
 }
