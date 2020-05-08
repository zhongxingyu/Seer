 package com.whereismytrain.transitboard;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Intent;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Toast;
 
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 public class Map extends FragmentActivity {
 	private GoogleMap map;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		LatLng currentPos = null;
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_map);
 		SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager()
 				.findFragmentById(R.id.map);
 		map = fm.getMap();
 		map.setMyLocationEnabled(true);
 		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
 		Criteria criteria = new Criteria();
 		String provider = locationManager.getBestProvider(criteria, true);
 		if (provider.equals("") || provider == null) {
 			Toast toast = Toast.makeText(getApplicationContext(),
 					"no provider", Toast.LENGTH_SHORT);
 			toast.show();
 		}
 		Location location = locationManager.getLastKnownLocation(provider);
 		if (location != null) {
 			double latitude = location.getLatitude();
 			double longitude = location.getLongitude();
 			currentPos = new LatLng(latitude, longitude);
 			List<Stop> stopLocations;
 			try {
 				stopLocations = nearbyStops(currentPos);
 				for (Stop stop : stopLocations) {
 					map.addMarker(new MarkerOptions().
 							position(stop.getLatLng()).
 							title(stop.getDescription()).
 							snippet("Zone: " + stop.getZone()));
 				}
 			} catch (Exception e) {
 				Toast toast = Toast.makeText(getApplicationContext(),
 						"Could not find nearby stops.", Toast.LENGTH_SHORT);
 				toast.show();
 			}
 
 		}
 		// Move the camera instantly to my position with a zoom of 15.
 		map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPos, 15));
 	}
 
 	public List<Stop> nearbyStops(LatLng latLng) throws JSONException {
 		Jsonp jParser = new Jsonp();
 		String url = "http://deco3801-003.uqcloud.net/opia/location/"
 				+ "rest/stops-nearby/GP:" + latLng.latitude + ",%20"
 				+ latLng.longitude + "?"
 				+ "radiusM=500&useWalkingDistance=true&maxResults=20";
 		JSONObject json = jParser.getJSONFromUrl(url);
 		// Splits the JSON file in each individual stop
 		JSONArray stopsJSON = json.getJSONArray("NearbyStops");
 		List<Stop> stops = new ArrayList<Stop>();
		Stop curStop;
 		for (int i = 0; i < stopsJSON.length(); i++) {
			curStop = new Stop();
 			stops.add(i, new Stop());
 			JSONObject c = stopsJSON.getJSONObject(i);
 			// Gets the StopId in the format 'XXXXXX'
 			String id = c.getString("StopId");
 			url = "http://deco3801-003.uqcloud.net/opia/location/rest/stops?ids=SI%3A"
 					+ id;
 			JSONObject stopDetails = jParser.getJSONFromUrl(url);
 			JSONObject stopDetail = stopDetails.getJSONArray("Stops")
 					.getJSONObject(0);
 			// Gets relevant details for each stop
			curStop.setDescription(stopDetail.getString("Description"));
 			// String position = stopDetail.getString("Position");
			curStop.setZone(stopDetail.getString("Zone"));
			curStop.setId(id);
 			JSONObject position = stopDetail.getJSONObject("Position");
			curStop.setLatLng(new LatLng(position.getDouble("Lat"), position
 					.getDouble("Lng")));
			curStop.setServiceType(stopDetail.getInt("ServiceType"));
 		}
 		return stops;
 	}
 
 	// Open the directions screen
 	public void openDirections(View view) {
 		Intent i = new Intent(this, Directions.class);
 		startActivity(i);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.map, menu);
 		return true;
 	}
 
 }
