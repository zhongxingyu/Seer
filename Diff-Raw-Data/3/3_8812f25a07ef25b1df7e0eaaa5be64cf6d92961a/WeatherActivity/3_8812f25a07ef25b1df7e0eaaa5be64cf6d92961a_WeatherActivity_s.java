 package com.example.weatherforecasts;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.widget.TextView;
 import android.app.ListActivity;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 
 public class WeatherActivity extends ListActivity {
 private static String url;
 	
 	// JSON node keys
 	private static final String ID = "id";
 	private static final String NAME = "name";
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		
         // getting intent data
         Intent in = getIntent();
         
         // Get JSON values from previous intent
         String id = in.getStringExtra(ID);
         String name = in.getStringExtra(NAME);
         
         url = "http://swellcast.com.au/api/v1/locations/" + id + ".json?api_key=wBALPyzyiZAKfstMW3UF";
      
         // Hashmap for ListView
  		ArrayList<HashMap<String, String>> forecastList = new ArrayList<HashMap<String, String>>();
 
  		JSONArray forecastArray = null;
  		// Creating JSON Parser instance
  		JSONParser parser = new JSONParser();
 
  		// getting JSON string from URL
  		JSONObject json = parser.getJSONFromUrl(url);
 
 
  	         
  		try {
  		
  			// Getting Array of forecasts
  			forecastArray = json.getJSONArray("three_hourly_forecasts");
 			
// 			"":"Wednesday","local_hour":" 3am","":"4.3","":"S","swell_direction_degrees":"184","swell_period_seconds":"9","wind_speed_knots":"27.3","wind_direction_compass_point":"SSW","":"203"}
			// looping through All locations
 			for(int i = 0; i < forecastArray.length(); i++){
 				JSONObject c = forecastArray.getJSONObject(i);
 
  				// Storing each json item in variable
  				String local_day = c.getString("local_day");
  				String local_hour = c.getString("local_hour");
  				String swellheightmetres = c.getString("swell_height_metres");
  				String swelldirectioncompass_point = c.getString("swell_direction_compass_point");
  				String winddirectiondegrees = c.getString("wind_direction_degrees");
  				
  				// creating new HashMap
  				HashMap<String, String> map = new HashMap<String, String>();
  				
  				// adding each child node to HashMap key => value
  				map.put("local_day", local_day);
  				map.put("local_hour", local_hour);
 
  				// adding HashList to ArrayList
  				forecastList.add(map);
  			}
  		} catch (JSONException e) {
  			e.printStackTrace();
  		}
  		
 		/**
 		 * Updating parsed JSON data into ListView
 		 * */
 		ListAdapter adapter = new SimpleAdapter(this, forecastList,
 				R.layout.activity_weather,
 				new String[] { "local_day", "local_hour"  }, new int[] {
 						R.id.local_day, R.id.local_hour});
 
 		setListAdapter(adapter);
 		
 		// selecting single ListView item
 		ListView lv = getListView();
 		
 		// Launching new screen on Selecting Single ListItem
 		lv.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				// getting values from selected ListItem
 				String local_day = ((TextView) view.findViewById(R.id.local_day)).getText().toString();
 				String local_hour = ((TextView) view.findViewById(R.id.local_hour)).getText().toString();
 				
 				// Starting new intent
 				Intent in = new Intent(getApplicationContext(), WeatherActivity.class);
 				in.putExtra("local_day", local_day);
 				in.putExtra("local_hour", local_hour);
 
 				startActivity(in);
 
 			}
 		});
 	}
 
 
 
 }
