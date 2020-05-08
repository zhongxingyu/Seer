 package org.dyndns.pawitp.salayatrammap.map;
 
 import android.app.SearchManager;
 import android.content.ContentProvider;
 import android.content.ContentValues;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.net.Uri;
 import android.text.TextUtils;
 
 public class SearchSuggestionProvider extends ContentProvider {
 	
 	public static final String AUTHORITY = "org.dyndns.pawitp.salayatrammap.map.SearchSuggestionProvider";
 	
 	private static final int SEARCH_SUGGEST = 0;
 	private static final int SHORTCUT_REFRESH = 1;
 	private static final UriMatcher sUriMatcher = buildUriMatcher();
 	
 	private TramDbHelper mDbHelper;
 	
 	private static UriMatcher buildUriMatcher() {
 		UriMatcher matcher =  new UriMatcher(UriMatcher.NO_MATCH);
 		matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
 		matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);
 		matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT, SHORTCUT_REFRESH);
 		matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*", SHORTCUT_REFRESH);
 		return matcher;
 	}
 	
 	@Override
 	public boolean onCreate() {
 		mDbHelper = new TramDbHelper(getContext());
 		mDbHelper.open();
 		return true;
 	}
 	
 	@Override
 	public String getType(Uri uri) {
 		switch (sUriMatcher.match(uri)) {
 			case SEARCH_SUGGEST:
 				return SearchManager.SUGGEST_MIME_TYPE;
 			case SHORTCUT_REFRESH:
 				return SearchManager.SHORTCUT_MIME_TYPE;
 			default:
 				throw new IllegalArgumentException("Unknown URL " + uri);
 		}
 	}
 
 	@Override
 	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
 			String sortOrder) {
 		if (!TextUtils.isEmpty(selection)) {
 			throw new IllegalArgumentException("selection not allowed for " + uri);
 		}
 		if (selectionArgs != null && selectionArgs.length != 0) {
 			throw new IllegalArgumentException("selectionArgs not allowed for " + uri);
 		}
 		if (!TextUtils.isEmpty(sortOrder)) {
 			throw new IllegalArgumentException("sortOrder not allowed for " + uri);
 		}
 		
 		switch (sUriMatcher.match(uri)) {
 		case SEARCH_SUGGEST:
 			if (uri.getPathSegments().size() > 1) {
				String query = "*" + uri.getLastPathSegment().replace(" ", "*") + "*";
				return mDbHelper.getSuggestions(query);
 			}
 			else {
				return mDbHelper.getAllSuggestions();
 			}
			
 		case SHORTCUT_REFRESH:
 			return null;
 		default:
 			throw new IllegalArgumentException("Unknown URL " + uri);
 		}
 	}
 
 	@Override
 	public Uri insert(Uri uri, ContentValues values) {
 		throw new UnsupportedOperationException();
 	}
 	
 	@Override
 	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public int delete(Uri uri, String selection, String[] selectionArgs) {
 		throw new UnsupportedOperationException();
 	}
 
 }
