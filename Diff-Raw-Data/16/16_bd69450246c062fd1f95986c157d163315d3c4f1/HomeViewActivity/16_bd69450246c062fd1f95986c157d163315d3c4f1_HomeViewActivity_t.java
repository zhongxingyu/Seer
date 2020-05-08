 package com.cse5236groupthirteen;
 
 import java.util.List;
 
 import com.cse5236groupthirteen.utilities.LocalRestaurant;
 import com.cse5236groupthirteen.utilities.ParseHelper;
 import com.cse5236groupthirteen.utilities.Restaurant;
 import com.cse5236groupthirteen.utilities.YumHelper;
 import com.parse.FindCallback;
 import com.parse.ParseException;
 import com.parse.ParseGeoPoint;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 
 import android.os.Bundle;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Toast;
 
 public class HomeViewActivity extends YumViewActivity {
 
 	private ListView listView;
 	
 	private ArrayAdapter<Restaurant> listAdapter;
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_home_view);
 		
 		// grab UI elements
 		listView = (ListView) findViewById(R.id.lstvw_homeView_restaurants);
 		
 		// setup ListView stuff
 		listAdapter = new ArrayAdapter<Restaurant>(this, android.R.layout.simple_list_item_1);
 		listView.setAdapter(listAdapter);
 		listView.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				
 				Restaurant r = listAdapter.getItem(position);
 				
 				Intent intent = new Intent(HomeViewActivity.this, RestaurantViewActivity.class);
 				intent.putExtra(Restaurant.R_UUID, r.getRestaurantId());
 				intent.putExtra(Restaurant.R_NAME, r.getName());
 				startActivity(intent);
 				
 			}
 
 		});
 		
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		loadDataFromParse();
 	}
	
	
	@Override
	public void onShake() {
		String text = "Refreshing Restaurants";
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
		loadDataFromParse();
	}
 
 	private void loadDataFromParse() {
 		
 		ParseQuery query = new ParseQuery(ParseHelper.CLASS_RESTAURANTS);
 		query.whereExists(Restaurant.R_UUID);
 		query.whereNear(Restaurant.R_GEOLOC, YumHelper.getLastBestLocationForParse(this));
 		query.findInBackground(new FindCallback() {
 
 			@Override
 			public void done(List<ParseObject> objects, ParseException e) {
 				if (e == null) {
 					
 					Context c = getApplicationContext();
 					ParseGeoPoint myLocation = YumHelper.getLastBestLocationForParse(c);
 					// query successful
 					listAdapter.clear();
 					for (ParseObject po: objects) {
 						Restaurant r = new LocalRestaurant(po, myLocation);
 						listAdapter.add(r);
 					}
 					listAdapter.notifyDataSetChanged();
 					
 				} else {
 					// error occurred
 					String errmsg = "There was an error loading data from Parse";
 					Toast.makeText(getApplicationContext(), errmsg, Toast.LENGTH_SHORT).show();
 					Log.e("Yum", errmsg, e);
 				}
 			}
 		});
 	}
 	
 }
