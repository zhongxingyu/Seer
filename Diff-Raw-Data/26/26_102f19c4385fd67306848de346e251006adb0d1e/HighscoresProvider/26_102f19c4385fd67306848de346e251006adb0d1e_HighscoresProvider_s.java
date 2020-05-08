 package pl.fzymek.android.cycloneeye.database.providers;
 
 import java.util.HashMap;
 import java.util.Map;
 
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
 import android.util.Log;
 
 /**
  * Highscore provider for 'Cyclone Eye' game
  * 
  * @author zymekfil
  * 
  */
 public class HighscoresProvider extends ContentProvider {
 
 	private final static String TAG = HighscoresProvider.class.getSimpleName();
 	private final static UriMatcher uriMatcher;
 	private final static Map<String, String> databaseProjectionMap;
 	private static final int INCOMING_HIGHSCORE_COLLECTION = 1;
 	private static final int INCOMING_HIGHSCORE_ELEMENT = 2;
 	private static final int ELEMENT_SEGMENT_NUMBER = 1;
 	private DatabaseHelper dbHelper = null;
 
 	static {
 		// uri matcher rules
 		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
 		uriMatcher.addURI(Metadata.AUTHORITY, Metadata.DATABASE_TABLE_NAME,
 				INCOMING_HIGHSCORE_COLLECTION);
 		uriMatcher.addURI(Metadata.AUTHORITY, Metadata.DATABASE_TABLE_NAME
 				+ "/#", INCOMING_HIGHSCORE_ELEMENT);
 
 		// database column projections
 		databaseProjectionMap = new HashMap<String, String>();
 		databaseProjectionMap.put(TableMetadata._ID, TableMetadata._ID);
 		databaseProjectionMap.put(TableMetadata.PLAYER, TableMetadata.PLAYER);
 		databaseProjectionMap.put(TableMetadata.SCORE, TableMetadata.SCORE);
 	}
 
 	@Override
 	public boolean onCreate() {
 		Log.d(TAG, "onCreate");
 		dbHelper = new DatabaseHelper(getContext());
 		return true;
 	}
 
 	@Override
 	public String getType(Uri uri) {
 
 		Log.d(TAG, "getType for uri: " + uri);
 		String type = null;
 		switch (uriMatcher.match(uri)) {
 		case INCOMING_HIGHSCORE_ELEMENT:
 			Log.d(TAG, "Uri matched single element from highscores");
 			type = TableMetadata.CONTENT_ITEM_TYPE;
 			break;
 
 		case INCOMING_HIGHSCORE_COLLECTION:
 			Log.d(TAG, "Uri matched highscores collection");
 			type = TableMetadata.CONTENT_TYPE;
 			break;
 
 		default:
 			final String error = "Cannot recognize type from uri: " + uri;
 			Log.e(TAG, error);
 			throw new IllegalArgumentException(error);
 		}
 
 		Log.d(TAG, "getType returned: " + type);
 		return type;
 	}
 
 	@Override
 	public int delete(Uri uri, String selection, String[] selectionArgs) {
 
 		final SQLiteDatabase highscoresDatabase = dbHelper
 				.getWritableDatabase();
 
 		int count = -1;
 
 		switch (uriMatcher.match(uri)) {
 		case INCOMING_HIGHSCORE_ELEMENT:
 			Log.d(TAG, "Uri matched single element from highscores");
 			final String rowId = uri.getPathSegments().get(
 					ELEMENT_SEGMENT_NUMBER);
 			final String where = DatabaseHelper.buildWhereClausule(rowId,
 					selection);
 
 			count = highscoresDatabase.delete(TableMetadata.TABLE_NAME, where,
 					selectionArgs);
 
 			break;
 
 		case INCOMING_HIGHSCORE_COLLECTION:
 			Log.d(TAG, "Uri matched highscores collection");
 			count = highscoresDatabase.delete(TableMetadata.TABLE_NAME,
 					selection, selectionArgs);
 			break;
 
 		default:
 			final String error = "Cannot recognize element(s) from uri: " + uri;
 			Log.e(TAG, error);
 			throw new IllegalArgumentException(error);
 		}
 
 		Log.d(TAG, "Deleted " + count + " row(s)s from database.");
 		return count;
 	}
 
 	@Override
 	public Uri insert(Uri uri, ContentValues values) {
 		if (uriMatcher.match(uri) != INCOMING_HIGHSCORE_COLLECTION) {
 			throw new IllegalArgumentException("Unknown uri: " + uri);
 		}
 
 		final String defaltPlayerName = "Player";
 		final String errorQuoteMessage = "Failed to insert row, because score is empty";
 
 		final ContentValues newValues = values != null ? new ContentValues(
 				values) : new ContentValues();
 
 		if (newValues.containsKey(TableMetadata.PLAYER) == false) {
 			Log.d(TAG, "Player is empty, using default");
 			newValues.put(TableMetadata.PLAYER, defaltPlayerName);
 		}
 
 		if (newValues.containsKey(TableMetadata.SCORE) == false) {
 			Log.e(TAG, errorQuoteMessage);
 			throw new IllegalArgumentException(errorQuoteMessage);
 		}
 
 		final SQLiteDatabase highscoresDatabase = dbHelper
 				.getWritableDatabase();
 		final long rowId = highscoresDatabase.insert(TableMetadata.TABLE_NAME,
 				TableMetadata.PLAYER, newValues);
 
 		if (rowId > 0) {
 			final Uri insertedQuote = ContentUris.withAppendedId(
 					TableMetadata.CONTENT_URI, rowId);
 			getContext().getContentResolver().notifyChange(insertedQuote, null);
 			Log.d(TAG, "Highscore inserted: " + insertedQuote);
 			return insertedQuote;
 		}
 
 		Log.e(TAG, "Failed to insert a row into: " + uri);
 		return null;
 	}
 
 	@Override
 	public Cursor query(Uri uri, String[] projection, String selection,
 			String[] selectionArgs, String sortOrder) {
 		final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
 
 		queryBuilder.setTables(TableMetadata.TABLE_NAME);
 		queryBuilder.setProjectionMap(databaseProjectionMap);
 
 		switch (uriMatcher.match(uri)) {
 		case INCOMING_HIGHSCORE_COLLECTION:
 			Log.d(TAG, "Querying quote collection");
 			break;
 		case INCOMING_HIGHSCORE_ELEMENT:
 			Log.d(TAG, "Querying single quote");
 			queryBuilder.appendWhere(TableMetadata._ID + " = "
 					+ uri.getPathSegments().get(1));
 			break;
 		default:
 			final String errorMsg = "Uri not matched: " + uri;
 			Log.e(TAG, errorMsg);
 			throw new IllegalArgumentException(errorMsg);
 		}
 
 		String orderBy = null;
 
 		if (TextUtils.isEmpty(sortOrder)) {
 			orderBy = TableMetadata.DEFAULT_SORT_ORDER;
 		} else {
 			orderBy = sortOrder;
 		}
 
 		final SQLiteDatabase db = dbHelper.getReadableDatabase();
 		final Cursor c = queryBuilder.query(db, projection, selection,
 				selectionArgs, null, null, orderBy);
 
 		c.setNotificationUri(getContext().getContentResolver(), uri);
 		return c;
 	}
 
 	@Override
 	public int update(Uri uri, ContentValues values, String selection,
 			String[] selectionArgs) {
 		final SQLiteDatabase db = dbHelper.getWritableDatabase();
 
 		int count;
 
 		switch (uriMatcher.match(uri)) {
 		case INCOMING_HIGHSCORE_COLLECTION:
 			Log.d(TAG, "Updating collection");
 			count = db.update(TableMetadata.TABLE_NAME, values, selection,
 					selectionArgs);
 			break;
 
 		case INCOMING_HIGHSCORE_ELEMENT:
 
 			final String rowId = uri.getPathSegments().get(1);
 			Log.d(TAG, "Updating row with id: " + rowId);
 			final String where = DatabaseHelper.buildWhereClausule(rowId,
 					selection);
 			count = db.update(TableMetadata.TABLE_NAME, values, where,
 					selectionArgs);
 
 			break;
 
 		default:
 			final String error = "Unknown URI: " + uri;
 			Log.e(TAG, error);
 			throw new IllegalArgumentException(error);
 		}
 
 		return count;
 	}
 
 	/**
 	 * @author zymekfil
 	 * 
 	 */
 	private static final class DatabaseHelper extends SQLiteOpenHelper {
 
 		private final static String TAG = DatabaseHelper.class.getSimpleName();
 
 		public DatabaseHelper(Context context) {
 			super(context, Metadata.DATABASE_NAME, null,
 					Metadata.DATABASE_VERSION);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			Log.d(TAG, "onCreate");
 
 			final String createTable = "CREATE TABLE "
 					+ Metadata.DATABASE_TABLE_NAME + " (" + TableMetadata._ID
 					+ " INTEGER PRIMARY KEY, " + TableMetadata.PLAYER
 					+ " TEXT," + TableMetadata.SCORE + " INTEGER" + ");";
 
 			Log.d(TAG, "executing SQL : " + createTable);
 			db.execSQL(createTable);
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			Log.d(TAG, "onUpdate");
 			Log.d(TAG, "Upgrading db from '" + oldVersion + "' to '"
 					+ newVersion + "' which will remove all data");
 
 			final String dropTable = "DROP TABLE IF EXISTS "
 					+ Metadata.DATABASE_TABLE_NAME + ";";
 			Log.d(TAG, "executing SQL : " + dropTable);
 			db.execSQL(dropTable);
 		}
 
 		public static String buildWhereClausule(final String rowId,
 				final String selection) {
 			final String where = BaseColumns._ID
 					+ " = "
 					+ rowId
 					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection
 							+ ")" : "");
 			return where;
 
 		}
 
 	}
 
 	/**
 	 * Content provider metadata
 	 * 
 	 * @author zymekfil
 	 * 
 	 */
 	public static final class Metadata {
 
		public final static String AUTHORITY = "cycloneeye.HighscoresProvider";
 		public final static String DATABASE_NAME = "cycloneeye.db";
 		public final static int DATABASE_VERSION = 1;
 		public final static String DATABASE_TABLE_NAME = "scores";
 
 		private Metadata() {
 		}
 
 	}
 
 	/**
 	 * Table metadata
 	 * 
 	 * @author zymekfil
 	 * 
 	 */
 	public static final class TableMetadata implements BaseColumns {
 
 		private TableMetadata() {
 		}
 
 		public final static String TABLE_NAME = Metadata.DATABASE_TABLE_NAME;
 		public final static Uri CONTENT_URI = Uri.parse("content://"
 				+ Metadata.AUTHORITY + "/" + TABLE_NAME);
 		public final static String CONTENT_TYPE = "vnd.android.cursor.dir/cycloneeye.highscores";
 		public final static String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/cycloneeye.highscores";
 		public final static String DEFAULT_SORT_ORDER = "_id DESC";
 
 		// // additional colums ////
 
 		/** player name is string */
 		public final static String PLAYER = "player";
 
 		/** score is int */
 		public final static String SCORE = "score";
 
 	}
 
 }
