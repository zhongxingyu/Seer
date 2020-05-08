 package org.everyuse.android.util;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
import java.net.URLEncoder;
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import android.content.SearchRecentSuggestionsProvider;
 import android.util.Log;
 
 public class SearchSuggestionProvider extends SearchRecentSuggestionsProvider {
 	private static final String TAG = "ExampleApp";
 
 	public final static String AUTHORITY = "com.everyuse.SearchSuggestionProvider";
 	public final static int MODE = DATABASE_MODE_QUERIES;
 
 	public SearchSuggestionProvider() {
 		setupSuggestions(AUTHORITY, MODE);
 	}
 
 	public static ArrayList<String> autocomplete(String q, String attr) {
 		ArrayList<String> resultList = null;
 
 		HttpURLConnection conn = null;
 		StringBuilder jsonResults = new StringBuilder();
 
 		try {
			StringBuilder sb = new StringBuilder(URLHelper.QUERY_SUGGESTION_URL + URLHelper.EXT_JSON);
			sb.append("?q=" + URLEncoder.encode(q, "UTF-8"));
			sb.append("&attr=" + URLEncoder.encode(attr, "UTF-8"));

			URL url;
 			url = new URL(sb.toString());
 			conn = (HttpURLConnection) url.openConnection();
 			InputStreamReader in = new InputStreamReader(conn.getInputStream());
 
 			// Load the results into a StringBuilder
 			int read;
 			char[] buff = new char[1024];
 			while ((read = in.read(buff)) != -1) {
 				jsonResults.append(buff, 0, read);
 			}
 		} catch (MalformedURLException e) {
 			Log.e(TAG, "Error processing Query Suggestion URL", e);
 			return resultList;
 		} catch (IOException e) {
 			Log.e(TAG, "Error connecting to Query Suggestion API", e);
 			return resultList;
 		} finally {
 			if (conn != null) {
 				conn.disconnect();
 			}
 		}
 
 		try {
 			// Create a JSON object hierarchy from the results
 			JSONArray suggestionJsonArray = new JSONArray(jsonResults.toString());
 
 			// Extract the Place descriptions from the results
 			resultList = new ArrayList<String>(suggestionJsonArray.length());
 			for (int i = 0; i < suggestionJsonArray.length(); i++) {
 				resultList.add(suggestionJsonArray.getString(i));
 			}
 		} catch (JSONException e) {
 			Log.e(TAG, "Cannot process JSON results", e);
 		}
 
 		return resultList;
 	}
 
 }
