 //09-06 18:41:37.330: E/Trace(1748): error opening trace file: No such file or directory (2)
 
 package com.gridimage;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.GridView;
 import android.widget.Toast;
 
 import com.loopj.android.http.AsyncHttpClient;
 import com.loopj.android.http.JsonHttpResponseHandler;
 
 @SuppressLint("ShowToast")
 public class SearchActivity extends Activity {
 	private static final View View = null;
 	EditText etQuery;
 	GridView gvResults;
 	Button btnSearch;
 	String searchQuery;
 	int pageCount = 0;
 	
 	private ImageAdapter adapter;
 	List<Image> imageResults = new ArrayList<Image>();
 	
 	private void setupViews() {
 		btnSearch = (Button) findViewById(R.id.btnSearch);
 		gvResults = (GridView) findViewById(R.id.gvResults);
 		etQuery = (EditText) findViewById(R.id.etQuery);
 		adapter = new ImageAdapter(getBaseContext(), imageResults);
 		gvResults.setAdapter(adapter);
 	}
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_search);
 		setupViews();
 		if (savedInstanceState == null) {
 			//clearSharedPreferences
 			SharedPreferences prefs = getSharedPreferences("sharedPreferences", MODE_PRIVATE);
 			prefs.edit().clear().commit();
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.seach, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		
 		if (item.getItemId() == R.id.action_settings) {
 			
 			// Launch settings Activity.
 			Intent intent = new Intent();
 			intent.setClass(this, SettingsActivity.class);
 			startActivity(intent);
 			
 			
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	//does GET request to image search API using AsyncHttpClient library
 	private void getImage(String query){
		pageCount = 0;
 		//create a http client
 		AsyncHttpClient client = new AsyncHttpClient(); 
 		//get request
 		client.get(query, new JsonHttpResponseHandler() {
 			@Override
 			public void onSuccess(JSONObject arg0) {
 				Log.d("Debug", arg0.toString());
 				//var x = responseData.results;
 				try {
 					adapter.clear();
 					//get the results array out of the JSON response
 					JSONObject rdJsonObject = arg0.getJSONObject("responseData");
 					JSONArray resultJsonObject = rdJsonObject.getJSONArray("results");
 					
 					//method defined in the class to parse the array
 					List<Image> parseJsonArray = Image.parseJsonArray(resultJsonObject);
 					//uses image adapter
 					adapter.addAll(parseJsonArray);
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 			}
 			
 			public void onFailure(Throwable error) {
 				Log.d("Debug", "NOOO request failed.");
 				Log.d("Debug", error.getMessage());
 			}
 		});
 	}
 	
 	
 	public void imageSearch(View v){
 		pageCount = 0;
 		
 		//get the queryString
 		SharedPreferences prefs = getSharedPreferences("sharedPreferences", MODE_PRIVATE);
 		String color = prefs.getString("color_filter", "");
 		String size = prefs.getString("image_size", "");
 		String type = prefs.getString("image_type", "");
 		String filter = prefs.getString("site_filter", "");
 
 		// Create the query string
 		String query = etQuery.getText().toString();
 		searchQuery = "https://ajax.googleapis.com/ajax/services/search/images?v=1.0&rsz=8&q=" + query;
 		if (!TextUtils.isEmpty(color)) {
 			searchQuery += ("&imgcolor=" + color);
 		}
 		if (!TextUtils.isEmpty(size)) {
 			searchQuery += ("&imgsz=" + size);
 		}
 		if (!TextUtils.isEmpty(type)) {
 			searchQuery += ("&imgtype=" + type);
 		}
 		if (!TextUtils.isEmpty(filter)) {
 			searchQuery += ("&as_sitesearch=" + filter);
 		}
 		Toast.makeText(this, searchQuery + "&start=" + pageCount, Toast.LENGTH_SHORT).show();
 	
 		getImage(searchQuery + "&start=" + pageCount);
 	}
 	
 	public void nextPage(View v) {
 		pageCount += 8;
 		Toast.makeText(this, "" + pageCount, Toast.LENGTH_SHORT).show();
 		getImage(searchQuery + "&start=" + pageCount);
 	}
 	
 	public void previousPage(View v){
 		if (pageCount > 0){
 			pageCount -= 8;
 		} else {
 			pageCount = 0;
 		}
 		
 		getImage(searchQuery + "&start=" + pageCount);
 	}
 }
