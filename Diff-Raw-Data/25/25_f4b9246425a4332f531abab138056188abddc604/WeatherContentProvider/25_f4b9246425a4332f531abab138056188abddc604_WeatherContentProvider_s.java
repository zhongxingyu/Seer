 /*
  * project		WeatherCast
  * 
  * package		com.jpennell.library
  * 
  * author		Jerry Pennell
  * 
  * date			Aug 19, 2013
  */
 package com.jpennell.library;
 
 
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

 import android.content.ContentProvider;
 import android.content.ContentValues;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.MatrixCursor;
 import android.net.Uri;
 import android.provider.BaseColumns;
 import android.util.Log;
 
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class WeatherContentProvider.
  */
 public class WeatherContentProvider extends ContentProvider {
 
     // Authority
 	 /** The Constant AUTHORITY. */
     public static final String AUTHORITY = "com.jpennell.library.weathercontentprovider";
 
     /**
      * The Class WeatherData.
      */
     public static class WeatherData implements BaseColumns {
 
         /** The Constant CONTENT_URI. */
         public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/days/");
 
         /** The Constant CONTENT_TYPE. */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/jpennell.weathercast.item";
         
         /** The Constant CONTENT_ITEM_TYPE. */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/jpennell.weathercast.item";
 
         // Define Columns
         /** The Constant DATE_COLUMN. */
         public static final String DATE_COLUMN = "date";
         
         /** The Constant DESCRIPTION_COLUMN. */
         public static final String DESCRIPTION_COLUMN = "desc";
         
         /** The Constant HI_COLUMN. */
         public static final String HI_COLUMN = "hi";
         
         /** The Constant LOW_COLUMN. */
         public static final String LOW_COLUMN = "low";
         
         /** The Constant WIND_COLUMN. */
         public static final String WIND_COLUMN = "wind";
 
         // Define Projection
         /** The Constant PROJECTION. */
         public static final String[] PROJECTION = {"_Id", DATE_COLUMN, DESCRIPTION_COLUMN, HI_COLUMN, LOW_COLUMN, WIND_COLUMN};
 
         // Constructor
         /**
          * Instantiates a new weather data.
          */
         private WeatherData() {}
     }
 
     /** The Constant FIVE_DAYS. */
     public static final int FIVE_DAYS = 1;
     
     /** The Constant ONE_DAY. */
     public static final int ONE_DAY = 2;
 
     /** The Constant uriMather. */
     private static final UriMatcher uriMather = new UriMatcher(UriMatcher.NO_MATCH);
 
     static {
         uriMather.addURI(AUTHORITY, "days/", FIVE_DAYS);
         uriMather.addURI(AUTHORITY, "days/#", ONE_DAY);
     }
 
 
     /* (non-Javadoc)
      * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String, java.lang.String[])
      */
     @Override
     public int delete(Uri uri, String selection, String[] selectionArgs) {
 
         throw new UnsupportedOperationException();
     }
 
 
     /* (non-Javadoc)
      * @see android.content.ContentProvider#getType(android.net.Uri)
      */
     @Override
     public String getType(Uri uri) {
         String type = null;
 
         switch (uriMather.match(uri)) {
             case FIVE_DAYS:
                 type = WeatherData.CONTENT_TYPE;
                 break;
 
             case ONE_DAY:
                 type =  WeatherData.CONTENT_ITEM_TYPE;
                 break;
 
                 default:
                     break;
         }
         return type;
     }
 
 
     /* (non-Javadoc)
      * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
      */
     @Override
     public Uri insert(Uri uri, ContentValues values) {
 
         throw new UnsupportedOperationException();
     }
 
 
     /* (non-Javadoc)
      * @see android.content.ContentProvider#onCreate()
      */
     @Override
     public boolean onCreate() {
 
         return false;
     }
 
 
     /* (non-Javadoc)
      * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
      */
     @Override
     public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
         MatrixCursor result = new MatrixCursor(WeatherData.PROJECTION);
 
         String readStorage = FileSystem.readStringFile(getContext(), "temp", false);
         Log.i("CONTENT URL STRING", readStorage);
 
         JSONObject json;
         JSONObject data = null;
         JSONArray fiveDay = null;
 
         try {
             json = new JSONObject(readStorage);
             data = json.getJSONObject("data");
             fiveDay = data.getJSONArray("weather");
         } catch (JSONException e) {
             e.printStackTrace();
         }
 
         if (data == null) {
             return result;
         }
 
         switch (uriMather.match(uri)) {
             case FIVE_DAYS:
                 // Display all five days
                 for (int i = 0; i < fiveDay.length(); i++) {
                     try {
                         JSONObject dayObject = fiveDay.getJSONObject(i);
                         // Get date
                         String fiveDayDate = dayObject.getString("date");
                         // Get description
                         String fiveDayDesc = dayObject.getJSONArray("weatherDesc").getJSONObject(0).getString("value");
                         // Get temp hi
                         String fiveDayHi = dayObject.getString("tempMaxF");
                         // Get temp low
                         String fiveDayLow = dayObject.getString("tempMinF");
                         // Get wind speed and direction
                         String fiveDayWind = dayObject.getString("windspeedMiles") + " " + dayObject.getString("winddir16Point");
 
                         Log.i("FIVE DAY", fiveDayDate + ", " + fiveDayDesc + ", " + fiveDayHi + ", " + fiveDayLow + ", " + fiveDayWind);
 
                         // Add to MatrixCursor
                         result.addRow(new Object[] {i + 1, fiveDayDate, fiveDayDesc, fiveDayHi, fiveDayLow, fiveDayWind});
                     } catch (JSONException e) {
                         e.printStackTrace();
                     }
                 }
                 break;
 
             case ONE_DAY:
                 // Display only requested day
                 String itemId = uri.getLastPathSegment();
 
                 int index;
                 try {
                     index = Integer.parseInt(itemId);
                 } catch (NumberFormatException e) {
                     e.printStackTrace();
                     break;
                 }
                 if (index <= 0 || index > fiveDay.length()) {
                     Log.i("QUERY", "Index out of range for " + uri.toString());
                 }
 
                 try {
                     JSONObject dayObject = fiveDay.getJSONObject(index - 1);
                     // Get date
                     String singleDayDate = dayObject.getString("date");
                     // Get description
                     String singleDayDesc = dayObject.getJSONArray("weatherDesc").getJSONObject(0).getString("value");
                     // Get temp hi
                     String singleDayHi = dayObject.getString("tempMaxF");
                     // Get temp low
                     String singleDayLow = dayObject.getString("tempMinF");
                     // Get wind speed and direction
                     String singleDayWind = dayObject.getString("windspeedMiles") + " " + dayObject.getString("winddir16Point");
 
                     Log.i("SINGLE DAY", singleDayDate + ", " + singleDayDesc + ", " + singleDayHi + ", " + singleDayLow + ", " + singleDayWind);
 
                     // Add to MatrixCursor
                     result.addRow(new Object[] {index, singleDayDate, singleDayDesc, singleDayHi, singleDayLow, singleDayWind});
                 } catch (JSONException e) {
                     e.printStackTrace();
                 }
 
                 break;
 
                 default:
                     Log.e("QUERY", "INVALID URI = " + uri.toString());
         }
         return result;
     }
 
 
     /* (non-Javadoc)
      * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues, java.lang.String, java.lang.String[])
      */
     @Override
     public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
 
         throw new UnsupportedOperationException();
     }
 }
