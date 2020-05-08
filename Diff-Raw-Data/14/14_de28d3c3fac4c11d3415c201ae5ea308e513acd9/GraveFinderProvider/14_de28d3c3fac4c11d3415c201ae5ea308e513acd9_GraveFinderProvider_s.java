 package pl.itiner.fetch;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import pl.itiner.model.Departed;
 
 import android.content.ContentProvider;
 import android.content.ContentResolver;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteQueryBuilder;
 import android.net.Uri;
 import android.text.TextUtils;
 
 public final class GraveFinderProvider extends ContentProvider implements
 		ResponseHandler<List<Departed>> {
 
 	private static final String TAG = "GraveFinderProvider";
 	public static final String NAME_QUERY_PARAM = "name";
 	public static final String SURENAME_QUERY_PARAM = "surename";
 	public static final String CEMENTARY_ID_QUERY_PARAM = "cm_id";
 	public static final String BIRTH_DATE_QUERY_PARAM = "birth_date";
 	public static final String DEATH_DATE_QUERY_PARAM = "death_date";
 	public static final String BURIAL_DATE_QUERY_PARAM = "burial_date";
 
 	public static final String SIMPLE_AUTHORITY = "pl.itiner.grave.GraveFinderProvider";
 	public static final String BASE_PATH = "graves";
 	public static final Uri CONTENT_URI = Uri.parse("content://"
 			+ SIMPLE_AUTHORITY + "/" + BASE_PATH);
 
 	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
 			+ "/graves";
 	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
 			+ "/grave";
 
 	// Used for the UriMacher"AA"
 	private static final int GRAVES_URI_ID = 1;
 	private static final int GRAVE_URI_ID = 2;
 
 	private static final UriMatcher sURIMatcher = new UriMatcher(
 			UriMatcher.NO_MATCH);
 
 	static {
 		sURIMatcher.addURI(SIMPLE_AUTHORITY, BASE_PATH, GRAVES_URI_ID);
 		sURIMatcher.addURI(SIMPLE_AUTHORITY, BASE_PATH + "/#", GRAVE_URI_ID);
 	}
 
 	private DepartedDB dbHelper;
 	static final SimpleDateFormat dateFormat = new SimpleDateFormat(
 			"dd-MM-yyyy");
 
 	@Override
 	public int delete(Uri uri, String selection, String[] selectionArgs) {
 		int match = sURIMatcher.match(uri);
 		SQLiteDatabase db = dbHelper.getWritableDatabase();
 		int count = 0;
 		switch (match) {
 		case GRAVES_URI_ID:
 			count = db.delete(DepartedTableHelper.TABLE_NAME, selection,
 					selectionArgs);
 			getContext().getContentResolver().notifyChange(uri, null);
 			break;
 		case GRAVE_URI_ID:
 			long departedId = ContentUris.parseId(uri);
 			count = db.delete(DepartedTableHelper.TABLE_NAME,
 					DepartedTableHelper.COLUMN_ID + " = " + departedId, null);
 			getContext().getContentResolver().notifyChange(uri, null);
 			break;
 		}
 		return count;
 	}
 
 	@Override
 	public String getType(Uri uri) {
 		int uriType = sURIMatcher.match(uri);
 		switch (uriType) {
 		case GRAVES_URI_ID:
 			return CONTENT_TYPE;
 		case GRAVE_URI_ID:
 			return CONTENT_ITEM_TYPE;
 		}
 		return null;
 	}
 
 	@Override
 	public Uri insert(Uri uri, ContentValues values) {
 		if (sURIMatcher.match(uri) == GRAVES_URI_ID) {
 			final SQLiteDatabase db = dbHelper.getWritableDatabase();
 			long id = db.insert(DepartedTableHelper.TABLE_NAME, null, values);
 			getContext().getContentResolver().notifyChange(uri, null);
 			return Uri.withAppendedPath(CONTENT_URI, id + "");
 		} else {
 			throw new IllegalArgumentException("Unknown uri: " + uri);
 		}
 	}
 
 	@Override
 	public boolean onCreate() {
 		dbHelper = new DepartedDB(getContext());
 		return true;
 	}
 
 	@Override
 	public Cursor query(Uri uri, String[] projection, String selection,
 			String[] selectionArgs, String sortOrder) {
 		int match = sURIMatcher.match(uri);
 		SQLiteDatabase db = dbHelper.getReadableDatabase();
 		Cursor c;
 		switch (match) {
 		case GRAVES_URI_ID:
 			return handleQuery(uri, projection, db);
 		case GRAVE_URI_ID:
 			long departedId = ContentUris.parseId(uri);
 			c = db.query(DepartedTableHelper.TABLE_NAME, projection,
 					DepartedTableHelper.COLUMN_ID
 							+ " = "
 							+ departedId
 							+ (!TextUtils.isEmpty(selection) ? " AND ("
 									+ selection + ')' : ""), selectionArgs,
 					null, null, sortOrder);
 
 			c.setNotificationUri(getContext().getContentResolver(), CONTENT_URI);
 			return c;
 		default:
 			throw new IllegalArgumentException("unsupported uri: " + uri);
 		}
 	}
 
 	private Cursor handleQuery(Uri uri, String[] projection, SQLiteDatabase db) {
 		try {
 			QueryParams queryParams = new QueryParams(uri);
 			SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
 			builder.setTables(DepartedTableHelper.TABLE_NAME);
 			String[] whereArgs = createWhere(uri, builder, queryParams);
 			DataHandler<List<Departed>> remoteDataHandler = PoznanGeoJSONHandler
 					.createInstance(queryParams, getContext());
 			remoteDataHandler.setResponseHandler(this);
 			new Thread(remoteDataHandler).start();
 			Cursor c = builder.query(db, projection, null, whereArgs, null,
 					null, null);
 			c.setNotificationUri(getContext().getContentResolver(), CONTENT_URI);
 			return c;
 		} catch (NumberFormatException e) {
 		} catch (ParseException e) {
 		}
 		return null;
 	}
 
 	private static String[] createWhere(Uri uri, SQLiteQueryBuilder builder,
 			QueryParams params) {
 		ArrayList<String> whereArgs = new ArrayList<String>();
 		boolean delim = putDateToWhere(builder,
 				DepartedTableHelper.COLUMN_DATE_BIRTH, false, whereArgs,
 				params.birthDate);
 		delim = putDateToWhere(builder, DepartedTableHelper.COLUMN_DATE_BURIAL,
 				delim, whereArgs, params.burialDate);
 		delim = putDateToWhere(builder, DepartedTableHelper.COLUMN_DATE_DEATH,
 				delim, whereArgs, params.deathDate);
 		delim = putStringToWhere(builder, DepartedTableHelper.COLUMN_SURENAME,
 				delim, whereArgs, params.surename);
 		delim = putStringToWhere(builder, DepartedTableHelper.COLUMN_NAME,
 				delim, whereArgs, params.name);
 		putStringToWhere(builder, DepartedTableHelper.COLUMN_CEMENTERY_ID,
 				delim, whereArgs, params.cmId);
 		return whereArgs.toArray(new String[whereArgs.size()]);
 	}
 
 	private static boolean putStringToWhere(SQLiteQueryBuilder builder,
 			String column, boolean addDelim, List<String> whereArgs,
 			Object paramValue) {
 		if (paramValue != null) {
 			String delim = addDelim ? " AND " : "";
 			builder.appendWhere(delim + column + "=?");
 			whereArgs.add(paramValue.toString());
 			return true;
 		}
 		return false;
 	}
 
 	private static boolean putDateToWhere(SQLiteQueryBuilder builder,
 			String column, boolean addDelim, List<String> whereArgs,
 			Date paramValue) {
 		if (paramValue != null) {
 			String delim = addDelim ? " AND " : "";
 			builder.appendWhere(delim + column + "=?");
 			whereArgs.add(paramValue.getTime() + "");
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public int update(Uri uri, ContentValues values, String selection,
 			String[] selectionArgs) {
 		if (sURIMatcher.match(uri) == GRAVES_URI_ID) {
 			final SQLiteDatabase db = dbHelper.getWritableDatabase();
 			getContext().getContentResolver().notifyChange(uri, null);
 			return db.update(DepartedTableHelper.TABLE_NAME, values, selection,
 					selectionArgs);
 		} else {
 			throw new IllegalArgumentException("Unknown uri: " + uri);
 		}
 	}
 
 	@Override
 	public synchronized void handleResponse(List<Departed> data) {
 		for (Departed d : data) {
 			ContentValues values = new ContentValues();
 			values.put(DepartedTableHelper.COLUMN_ID, d.getId());
			values.put(DepartedTableHelper.COLUMN_DATE_BIRTH, d.getBirthDate()
					.getTime());
 			values.put(DepartedTableHelper.COLUMN_DATE_BURIAL, d
					.getBurialDate().getTime());
			values.put(DepartedTableHelper.COLUMN_DATE_DEATH, d.getDeathDate()
					.getTime());
 			values.put(DepartedTableHelper.COLUMN_FAMILY, d.getFamily());
 			values.put(DepartedTableHelper.COLUMN_FIELD, d.getField());
 			values.put(DepartedTableHelper.COLUMN_LAT, d.getLocation()
 					.getLatitude());
 			values.put(DepartedTableHelper.COLUMN_LON, d.getLocation()
 					.getLongitude());
 			values.put(DepartedTableHelper.COLUMN_NAME, d.getName());
 			values.put(DepartedTableHelper.COLUMN_PLACE, d.getPlace());
 			values.put(DepartedTableHelper.COLUMN_QUARTER, d.getQuater());
 			values.put(DepartedTableHelper.COLUMN_ROW, d.getRow());
 			values.put(DepartedTableHelper.COLUMN_SIZE, d.getSize());
 			values.put(DepartedTableHelper.COLUMN_SURENAME, d.getSurname());
 			getContext().getContentResolver().insert(CONTENT_URI, values);
 		}
 	}
 
 }
