 package dinosaur.oblig3_1;
 
 import android.content.ContentProvider;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteQueryBuilder;
 import android.net.Uri;
 
 public class DatabaseProvider extends ContentProvider {
 	private Database db;
 	
 	private static final String AUTHORITY = "dinosaur.oblig3_1.DatabaseProvider";
 	public static final int LOG = 1;
 	public static final int LOG_ENTRY_ID = 2;
 	public static final int LOG_CATEGORY = 3;
 	public static final int SETTINGS = 4;
 	private static final String LOG_BASE_PATH = "log";
 	private static final String SETTINGS_BASE_PATH = "settings";
 	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + LOG_BASE_PATH);
 	public static final Uri SETTINGS_URI = Uri.parse("content://" + AUTHORITY + "/" + SETTINGS_BASE_PATH);
 	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/oblig3";
 	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/oblig3";
 	
 	private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
 	static {
 		uriMatcher.addURI(AUTHORITY, LOG_BASE_PATH, LOG);
 		uriMatcher.addURI(AUTHORITY, LOG_BASE_PATH + "/#", LOG_ENTRY_ID);
 		uriMatcher.addURI(AUTHORITY, LOG_BASE_PATH + "/category/*", LOG_CATEGORY);
 		uriMatcher.addURI(AUTHORITY, SETTINGS_BASE_PATH, SETTINGS);
 	}
 
 	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = uriMatcher.match(uri);
		switch (uriType) {
		case LOG:
			return db.getWritableDatabase().delete("log", selection, selectionArgs);
			
		case SETTINGS:
			return db.getWritableDatabase().delete("settings", selection, selectionArgs);
		}
 		return 0;
 	}
 
 	@Override
 	public String getType(Uri arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Uri insert(Uri uri, ContentValues values) {
 		int uriType = uriMatcher.match(uri);
 		switch (uriType) {
 		case LOG:
 			db.getWritableDatabase().insert("log", "", values);
 			break;
 			
 		case SETTINGS:
 			db.getWritableDatabase().insert("settings", "", values);
 			break;
 		}
 		return uri;
 	}
 
 	@Override
 	public boolean onCreate() {
 		db = new Database(getContext());
 		return true;
 	}
 
 	@Override
 	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
 		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
 		int uriType = uriMatcher.match(uri);
 		switch (uriType) {
 		case LOG_ENTRY_ID:
			queryBuilder.setTables("log");
 			queryBuilder.appendWhere("_id=" + uri.getLastPathSegment());
 			break;
 			
 		case LOG_CATEGORY:
			queryBuilder.setTables("log");
 			queryBuilder.appendWhere("category=\"" + uri.getLastPathSegment() +"\"");
 			break;
 		}
 		Cursor cursor = queryBuilder.query(db.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
 		cursor.setNotificationUri(getContext().getContentResolver(), uri);
 		return cursor;
 	}
 
 	@Override
 	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 }
