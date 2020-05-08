 package com.amca.android.replace.place;
 
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
 import com.actionbarsherlock.app.SherlockListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.Toast;
 import com.amca.android.replace.R;
 import com.amca.android.replace.http.HTTPTransfer;
 import com.amca.android.replace.model.Place;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 public class PlaceSelector extends SherlockListActivity {
 
 	private Integer userId, typeId;
 	private String range, currentLat, currentLng, jsonValue, typeName, title;
 	private List<Place> placesList = new ArrayList<Place>();
 	private ProgressBar progressBar;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_place_selector);
 		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 		
 		Intent intent = getIntent();
 		this.userId = intent.getIntExtra("userId", 0);
 		this.typeId = intent.getIntExtra("typeId", 0);
 		this.range = intent.getStringExtra("range");
 		this.currentLat = intent.getStringExtra("currentLat");
 		this.currentLng = intent.getStringExtra("currentLng");
 		typeName = intent.getStringExtra("typeName");
 
 		progressBar = (ProgressBar) findViewById(R.id.progressBar1);
 
 		HashMap<String, String> data = new HashMap<String, String>();
 		data.put("userId", userId.toString());
 		data.put("typeId", typeId.toString());
 		data.put("range", range);
 		data.put("currentLat", this.currentLat);
 		data.put("currentLng", this.currentLng);
 
 		HTTPPlaceSelector http = new HTTPPlaceSelector();
 		http.setContext(PlaceSelector.this);
 		http.setData(data);
 		http.execute("place/process/");
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		Place place = placesList.get(position);
 		
 		Intent intent = new Intent(PlaceSelector.this, PlaceDetail.class);
 		intent.putExtra("userId", this.userId);
 		intent.putExtra("typeId", this.typeId);
		intent.putExtra("placeId", place.getPlaceId());
 		intent.putExtra("placeName", place.getPlaceName());
 		intent.putExtra("placeAddress", place.getPlaceAddress());
 		intent.putExtra("placeDesc", place.getPlaceDesc());
 		intent.putExtra("placeReviews", place.getPlaceReviews().toString());
 		intent.putExtra("placeDistance", place.getPlaceDistance());
 		intent.putExtra("averagePoint", place.getAveragePoint());
 
 		startActivity(intent);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuItem menuProfile = menu.add("View Map");
 		menuProfile.setIcon(R.drawable.location_map);
 		menuProfile.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
 				| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
 		menuProfile.setOnMenuItemClickListener(new OnMenuItemClickListener() {
 			@Override
 			public boolean onMenuItemClick(final MenuItem item) {
 				Intent intent = new Intent(PlaceSelector.this,
 						PlaceSelectorMap.class);
 				intent.putExtra("currentLat", Float.parseFloat(currentLat));
 				intent.putExtra("currentLng", Float.parseFloat(currentLng));
 				intent.putExtra("jsonValue", jsonValue);
 				intent.putExtra("userId", userId);
 				intent.putExtra("title", title);
 				startActivity(intent);
 				return true;
 			}
 		});
 		return true;
 	}
 
 	class HTTPPlaceSelector extends HTTPTransfer {
 		@Override
 		protected void onPostExecute(String result) {
 			super.onPostExecute(result);
 			jsonValue = result;
 			// parse json data
 			try {
 				JSONArray jArray = new JSONArray(result);
 				int nPlaces = jArray.length();
 				title = "showing " + nPlaces + " result(s)"; 
 				setTitle(title);
 				for (int i = 0; i < nPlaces; i++) {
 					JSONObject json_data = jArray.getJSONObject(i);
 
 					Place place = new Place();
 					place.setPlaceId(json_data.getInt("placeId"));
 					place.setPlaceName(json_data.getString("placeName"));
 					place.setPlaceAddress(json_data.getString("placeAddress"));
 					place.setPlaceDesc(json_data.getString("placeDesc"));
 					place.setPlaceReviews(json_data.getInt("placeReviews"));
 					place.setPlaceDistance(json_data.getDouble("placeDistance"));
 					place.setAveragePoint(json_data.getDouble("averagePoint"));
 					placesList.add(place);
 				}
 
 				PlaceSelectorArrayAdapter adapter = new PlaceSelectorArrayAdapter(
 						PlaceSelector.this, placesList);
 				setListAdapter(adapter);
 			} catch (JSONException e) {
 				Toast.makeText(getApplicationContext(),
 						"Error parsing data " + e.toString(),
 						Toast.LENGTH_SHORT).show();
 			}
 			progressBar.setVisibility(View.INVISIBLE);
 		}
 	}
 }
