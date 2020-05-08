 package org.weather.weatherman.content;
 
 import android.content.ContentProvider;
 import android.content.ContentValues;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteStatement;
 import android.net.Uri;
 
 public class WeatherContentProvider extends ContentProvider {
 
 	private static final UriMatcher URI_MATCHER;
 	static {
 		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
 		URI_MATCHER.addURI(Weather.AUTHORITY, Weather.SETTING_PATH, Weather.Setting.TYPE);
 		URI_MATCHER.addURI(Weather.AUTHORITY, Weather.SETTING_PATH + "/#", Weather.Setting.TYPE);
 		URI_MATCHER.addURI(Weather.AUTHORITY, Weather.REALTIME_PATH + "/#", Weather.RealtimeWeather.TYPE);
 		URI_MATCHER.addURI(Weather.AUTHORITY, Weather.FORECAST_PATH + "/#", Weather.ForecastWeather.TYPE);
 		// city
 		URI_MATCHER.addURI(Weather.AUTHORITY, Weather.CITY_PATH, Weather.City.TYPE);
 		URI_MATCHER.addURI(Weather.AUTHORITY, Weather.CITY_PATH + "/#", Weather.City.TYPE);
 	}
 
 	private DatabaseSupport databaseSupport;
 	private SettingProvider settingProvider;
 	private RealtimeProvider realtimeProvider;
 	private ForecastProvider forecastProvider;
 
 	@Override
 	public boolean onCreate() {
 		databaseSupport = new DatabaseSupport(getContext());
 		WeatherService weatherService = new WeatherService();
 		settingProvider = new SettingProvider(databaseSupport);
 		realtimeProvider = new RealtimeProvider(databaseSupport, weatherService, settingProvider);
 		forecastProvider = new ForecastProvider(databaseSupport, weatherService, settingProvider);
 		return true;
 	}
 
 	@Override
 	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
 		switch (URI_MATCHER.match(uri)) {
 		case Weather.Setting.TYPE:
 			return settingProvider.find();
 		case Weather.RealtimeWeather.TYPE:
 			String citycode = uri.getLastPathSegment();
 			return realtimeProvider.find(citycode);
 		case Weather.ForecastWeather.TYPE:
 			citycode = uri.getLastPathSegment();
 			return forecastProvider.find(citycode);
 		case Weather.City.TYPE:
 			return databaseSupport.getReadableDatabase().query(Weather.City.TABLE_NAME, null, selection, selectionArgs,
 					null, null, sortOrder);
 		default:
 			throw new IllegalArgumentException("unknown Uri " + uri);
 		}
 	}
 
 	@Override
 	public String getType(Uri uri) {
 		switch (URI_MATCHER.match(uri)) {
 		case Weather.Setting.TYPE:
 			return Weather.Setting.CONTENT_TYPE;
 		case Weather.RealtimeWeather.TYPE:
 			return Weather.RealtimeWeather.CONTENT_TYPE;
 		case Weather.ForecastWeather.TYPE:
 			return Weather.ForecastWeather.CONTENT_TYPE;
 		case Weather.City.TYPE:
 			return Weather.City.CONTENT_TYPE;
 		default:
 			throw new IllegalArgumentException("unknown Uri " + uri);
 		}
 	}
 
 	@Override
 	public int delete(Uri arg0, String arg1, String[] arg2) {
 		switch (URI_MATCHER.match(arg0)) {
 		case Weather.Setting.TYPE:
 			return 0;
 		case Weather.RealtimeWeather.TYPE:
 			return 0;
 		case Weather.ForecastWeather.TYPE:
 			return 0;
 		case Weather.City.TYPE:
 			return databaseSupport.getWritableDatabase().delete(Weather.City.TABLE_NAME, arg1, arg2);
 		default:
 			throw new IllegalArgumentException("unknown Uri " + arg0);
 		}
 	}
 
 	@Override
 	public Uri insert(Uri uri, ContentValues values) {
 		switch (URI_MATCHER.match(uri)) {
 		case Weather.Setting.TYPE:
 			return null;
 		case Weather.RealtimeWeather.TYPE:
 			return null;
 		case Weather.ForecastWeather.TYPE:
 			return null;
 		case Weather.City.TYPE:
 			String rowId = null;
 			String code = values.getAsString(Weather.City.CODE);
 			Cursor cursor = this.query(Weather.City.CONTENT_URI, null, "code=?", new String[] { code }, null);
 			if (cursor.moveToFirst()) {// update
 				rowId = cursor.getString(cursor.getColumnIndex(Weather.City.ID));
 				String where = Weather.City.ID + "=? ";
 				String[] args = new String[] { rowId };
 				databaseSupport.getWritableDatabase().update(Weather.City.TABLE_NAME, values, where, args);
 			} else {// insert
 				long id = databaseSupport.getWritableDatabase().insert(Weather.City.TABLE_NAME, null, values);
 				rowId = String.valueOf(id);
 			}
 			cursor.close();
 			return Uri.withAppendedPath(Weather.City.CONTENT_URI, rowId);
 		default:
 			throw new IllegalArgumentException("unknown Uri " + uri);
 		}
 	}
 
 	@Override
 	public int bulkInsert(Uri uri, ContentValues[] valuesArray) {
 		switch (URI_MATCHER.match(uri)) {
 		case Weather.Setting.TYPE:
 			return 0;
 		case Weather.RealtimeWeather.TYPE:
 			return 0;
 		case Weather.ForecastWeather.TYPE:
 			return 0;
 		case Weather.City.TYPE:
 			int result = 0;
 			SQLiteDatabase sqlite = databaseSupport.getWritableDatabase();
 			String sql = "insert into " + Weather.City.TABLE_NAME + "(" + Weather.City.CODE + "," + Weather.City.NAME
 					+ "," + Weather.City.PARENT + ") values(?,?,?) ";
 			SQLiteStatement stat = sqlite.compileStatement(sql);
 			sqlite.beginTransaction();
 			try {
 				for (ContentValues values : valuesArray) {
 					stat.bindString(1, values.getAsString(Weather.City.CODE));
 					stat.bindString(2, values.getAsString(Weather.City.NAME));
 					String p = values.getAsString(Weather.City.PARENT);
					if (p != null && p.length() > 0) {
 						stat.bindString(3, p);
 					} else {
 						stat.bindNull(3);
 					}
 					stat.executeInsert();
 					result++;
 				}
 				sqlite.setTransactionSuccessful();
 			} finally {
 				sqlite.endTransaction();
 			}
 			return result;
 		default:
 			throw new IllegalArgumentException("unknown Uri " + uri);
 		}
 	}
 
 	@Override
 	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
 		switch (URI_MATCHER.match(uri)) {
 		case Weather.Setting.TYPE:
 			settingProvider.update(values);
 			return 1;
 		case Weather.RealtimeWeather.TYPE:
 			return 0;
 		case Weather.ForecastWeather.TYPE:
 			return 0;
 		case Weather.City.TYPE:
 			int res = 0;
 			String code = values.getAsString(Weather.City.CODE);
 			Cursor cursor = this.query(Weather.City.CONTENT_URI, null, "code=?", new String[] { code }, null);
 			if (cursor.moveToFirst()) {
 				String rowId = cursor.getString(cursor.getColumnIndex(Weather.City.ID));
 				selection = (selection != null && selection.length() > 0 ? " and " : "") + Weather.City.ID + "=? ";
 				if (selectionArgs == null || selectionArgs.length == 0) {
 					selectionArgs = new String[1];
 				} else {
 					String[] args = new String[selectionArgs.length + 1];
 					System.arraycopy(selectionArgs, 0, args, 0, selectionArgs.length);
 					selectionArgs = args;
 				}
 				selectionArgs[selectionArgs.length - 1] = rowId;
 				res = databaseSupport.getWritableDatabase().update(Weather.City.TABLE_NAME, values, selection,
 						selectionArgs);
 			}
 			cursor.close();
 			return res;
 		default:
 			throw new IllegalArgumentException("unknown Uri " + uri);
 		}
 	}
 
 }
