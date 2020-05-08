 package com.recarlin.wiseweather;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import forecastBuilder.RequestService;
 import android.content.ContentProvider;
 import android.content.ContentValues;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.MatrixCursor;
 import android.net.Uri;
 import android.provider.BaseColumns;
 import android.util.Log;
 
 public class ForecastProvider extends ContentProvider {
 
 	public static final String AUTHORITY = "com.recarlin.wiseweather.forecastprovider";
 	
 	public static class WeatherData implements BaseColumns {
 		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/periods");
 		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.recarlin/wiseweather.periods";
 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.recarlin.wiseweather.periods";
 		
 		public static final String PERIOD_COLUMN = "title";
 		public static final String FORECAST_COLUMN = "fcttext";
 		
 		public static final String[] PROJECTION = {"id", PERIOD_COLUMN, FORECAST_COLUMN};
 		
 		private WeatherData(){};
 	}
 	
 	public static final int PERIODS = 1;
 	public static final int PERIODS_ID = 2;
 	
 	private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
 	
 	static {
 		uriMatcher.addURI(AUTHORITY, "urlpath/", PERIODS);
 		uriMatcher.addURI(AUTHORITY, "urlpath/#", PERIODS_ID);
 	}
 	
 	@Override
 	public int delete(Uri uri, String selection, String[] selectionArgs) {
 		// TODO Auto-generated method stub
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public String getType(Uri uri) {
 		switch (uriMatcher.match(uri)) {
 		case PERIODS:
 			return WeatherData.CONTENT_TYPE;
 			
 		case PERIODS_ID:
 			return WeatherData.CONTENT_ITEM_TYPE;
 		}
 		return null;
 	}
 
 	@Override
 	public Uri insert(Uri uri, ContentValues values) {
 		// TODO Auto-generated method stub
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public boolean onCreate() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
 		MatrixCursor result = new MatrixCursor(WeatherData.PROJECTION);
		String JSONString = RequestService.readFile(getContext(), RequestService.FILENAME, false);
 		JSONObject complete = null;
 		JSONObject forecastObject = null;
 		JSONObject textObject = null;
 		JSONArray forecastDayArray = null;
 		JSONObject forecastStuff = null;
 		
 		try {
 			complete = new JSONObject(JSONString);
 			forecastObject = complete.getJSONObject(RequestService.JSON_FORECAST);
 			textObject = forecastObject.getJSONObject(RequestService.JSON_TEXT);
 			forecastDayArray = textObject.getJSONArray(RequestService.JSON_FORECASTDAY);
 		} catch(JSONException e) {
 			e.printStackTrace();
 		}
 		
 		if (forecastDayArray == null) {
 			return result;
 		}
 		
 		switch (uriMatcher.match(uri)) {
 		case PERIODS:
 			for ( int i = 0;i<forecastDayArray.length();i++) {
 				try {
 					forecastStuff = forecastDayArray.getJSONObject(i);
 					result.addRow(new Object[] {i +1, forecastStuff.get(RequestService.JSON_PERIOD),
 							forecastStuff.get(RequestService.JSON_TXTFORECAST)});
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 			}
 		case PERIODS_ID:
 			String itemId = uri.getLastPathSegment();
 			int index;
 			try {
 				index = Integer.parseInt(itemId);
 			} catch (NumberFormatException e) {
 				Log.e("query", "index format error");
 				break;
 			}
 			if(index <= 0 || index > forecastDayArray.length()) {
 				Log.e("query", "index out of range for " + uri.toString());
 				break;
 			}
 			try {
 				forecastStuff = forecastDayArray.getJSONObject(index);
 				result.addRow(new Object[] {index, forecastStuff.get(RequestService.JSON_PERIOD),
 						forecastStuff.get(RequestService.JSON_TXTFORECAST)});
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 			break;
 			
 			default:
 				Log.e("query", "invald uri = " + uri.toString());
 		}
 		return result;
 	}
 
 	@Override
 	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
 		// TODO Auto-generated method stub
 		throw new UnsupportedOperationException();
 	}
 
 }
