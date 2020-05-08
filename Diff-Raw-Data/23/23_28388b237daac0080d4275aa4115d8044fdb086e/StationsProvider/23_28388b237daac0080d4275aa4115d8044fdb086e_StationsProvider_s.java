 package com.papagiannis.tuberun;
 
 import android.app.SearchManager;
 import android.content.SearchRecentSuggestionsProvider;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.net.Uri;
 import android.util.Log;
 
 public class StationsProvider extends SearchRecentSuggestionsProvider {
 	public final static String AUTHORITY = "com.papagiannis.tuberun.stationsprovider";
 	public final static int MODE = DATABASE_MODE_QUERIES;// //
 															// DATABASE_MODE_2LINES;|
 	private final DatabaseHelper myDbHelper = new DatabaseHelper(getContext());
 
 	private static final UriMatcher sUriMatcher = new UriMatcher(
 			UriMatcher.NO_MATCH);
 
 	public StationsProvider() {
 		try {
			setupSuggestions(AUTHORITY, MODE);
			sUriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY+"/#", 1);
 			myDbHelper.openDataBase();
 		} catch (Exception e) {
 			Log.w("StationsProvider", e);
 		}
 	}
 
 	// Implements ContentProvider.query()
 	public Cursor query(Uri uri, String[] projection, String selection,
 			String[] selectionArgs, String sortOrder) {
 		Cursor result = null;
 		try {
 			switch (sUriMatcher.match(uri)) {
 			case 1:
 				result = myDbHelper.getStationsSuggestions(selectionArgs[0]);
 				break;
 			default:
 			}
 			result = myDbHelper.getStationsSuggestions(selectionArgs[0]);
 
 		} catch (Exception e) {
 			Log.w("StationsProvider", e);
 		} finally {
 			// myDbHelper.close();
 		}
 
 		return result;
 	}
 }
