 package com.engine9;
 
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Locale;
 import java.util.Vector;
 
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesClient;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 import com.google.android.gms.location.LocationClient;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
 import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.CameraPosition;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 
 import android.app.ActionBar;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.app.SearchManager;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentSender;
 import android.database.Cursor;
 import android.location.Criteria;
 import android.location.Geocoder;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.provider.SearchRecentSuggestions;
 import android.support.v4.app.FragmentActivity;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.ProgressBar;
 import android.widget.SearchView;
 import android.widget.Toast;
 
 /**
  * This class will hole the stop information and show them on the map
  * */
 
 public class StopMapActivity extends FragmentActivity implements
 GooglePlayServicesClient.ConnectionCallbacks,
 GooglePlayServicesClient.OnConnectionFailedListener, LocationListener{
 
 	private GoogleMap mMap; //The Google Map used
 
 	private Location currentLocation; //The user's current location
 	private LocationManager mLocationManager; //Used for getting location
 	private JsonElement jData; //The Json data holding the stops
 	
 	private CountDownTimer cdt;
 	private ProgressBar pb;
 	private LatLng previousPosition;
 	
 	public Vector<Stop> stopVector = new Vector<Stop>(); //A vector for keeping stop info
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_stop_map);
 		ActionBar actionBar = getActionBar();
 		actionBar.setDisplayShowHomeEnabled(false);
 		actionBar.setDisplayShowTitleEnabled(true);
 		actionBar.setTitle("Translink");
 		actionBar.setDisplayHomeAsUpEnabled(false);
 		
 		cdt = new CountDownTimer(3000, 3000){
 
 			@Override
 			public void onFinish() {
 				//updateStops();
 				if(previousPosition != null){
 					
 					LatLng mapPosition = mMap.getCameraPosition().target;
 					//Log.e("DEBUG", String.valueOf(calcDistance(mapPosition, previousPosition)));
 					
 					Log.e("DEBUG", String.valueOf(calcDistance(mapPosition, previousPosition)));
 					if(calcDistance(mapPosition, previousPosition)> 1000){
 						
 						int radius = (int) calculateRadius();
 						if(radius > 2500){
 							radius = 2500;
 						}
 						Log.e("DEBUG", mapPosition.toString() + " radius: " + String.valueOf(radius));
 						new StopRequest2().execute("http://deco3801-005.uqcloud.net/stops-from-latlon/?lat="+ 
 								mapPosition.latitude + "&lon=" + mapPosition.longitude + "&radius=" + radius);
 					}
 				}
 				else{
 					LatLng mapPosition = mMap.getCameraPosition().target;
 					int radius = (int) calculateRadius();
 					if(radius > 2000){
 						radius = 2000;
 					}
 					Log.e("DEBUG", mapPosition.toString() + " radius: " + String.valueOf(radius));
 					new StopRequest2().execute("http://deco3801-005.uqcloud.net/stops-from-latlon/?lat="+ 
 							mapPosition.latitude + "&lon=" + mapPosition.longitude + "&radius=" + radius);
 				}
 				
 			}
 
 			@Override
 			public void onTick(long millisUntilFinished) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 		};
 		
 //		String sLocation = i.getStringExtra("location");
 //		if(sLocation != null && sLocation.length() != 0){
 //			Log.e("debug", sLocation +"derp");
 //			new StopRequest().execute("http://deco3801-005.uqcloud.net/stops-from-location/?location=" +sLocation);
 //		}
 		//Check the Google Play Service whether is connected
 		
 		
 		handleIntent(getIntent());
 		
 		if(servicesConnected()){
 			//Create new Location Manager and set up location updates
 			mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 	        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0, 0, this);
 	        
 	        //Tries to get the current location
 	        currentLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 	        
 	        //Check whether the connection is over time 
 	        if(currentLocation != null && currentLocation.getTime() > Calendar.getInstance().
 	        		getTimeInMillis() - 2 * 60 * 1000) {
 	        	
 	        	Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
 				//Converts location to address string
 				String address;
 				try {
 					address = gcd.getFromLocation(currentLocation.getLatitude(), 
 							currentLocation.getLongitude(), 1).get(0).getLocality();
 					Log.e("DEBUG", address);
 					
 					new StopRequest().execute("http://deco3801-005.uqcloud.net/stops-from-location/?location=" +address);
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				//We only need the location once, so updates are stopped
 				mLocationManager.removeUpdates(this);
 	        }
 		}
 		
 		setUpMap(new LatLng(-27, 153));
 	}
 	
 	@Override
     protected void onNewIntent(Intent intent) {
         // Because this activity has set launchMode="singleTop", the system calls this method
         // to deliver the intent if this activity is currently the foreground activity when
         // invoked again (when the user executes a search from this activity, we don't create
         // a new instance of this activity, so the system delivers the search intent here)
         handleIntent(intent);
     }
 	
 	private void handleIntent(Intent intent) {
 		
 		 if (Intent.ACTION_VIEW.equals(intent.getAction())) {
 			 
 			 //Intent wordIntent = new Intent(this, this.getClass());
 	         //wordIntent.setData(intent.getData());
 	         //Log.e("NANO-DEBUG",intent.getAction());
 	         Log.e("NANO-DEBUG",intent.getData().toString());
 	         Uri uri = getIntent().getData();
 	         Cursor cursor = getContentResolver().query(uri, null, null, null, null);
 	        // startActivity(wordIntent);
 	         
 	         if (cursor == null) {
 	             finish();
 	         } else {
 	             cursor.moveToFirst();
 	             int wIndex = cursor.getColumnIndexOrThrow(LocationDatabase.KEY_WORD);
 	             int dIndex = cursor.getColumnIndexOrThrow(LocationDatabase.KEY_DEFINITION);
 
 	             Log.e("NANO-DEBUG",cursor.getString(wIndex));
 	             Log.e("NANO-DEBUG",cursor.getString(dIndex));
 	             doMySearch(cursor.getString(wIndex));
 	         }
 			
 		 }
 		 else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
             Log.e("NANO-DEBUG","handle intent");
         	//String query=intent.getStringExtra(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
         	String query=intent.getStringExtra(SearchManager.QUERY);
         	Log.e("NANO-DEBUG","query:"+query);
             doMySearch(query);
         }
     }
 	private void doMySearch(String query) {
 		String queryString="http://deco3801-005.uqcloud.net/stops-from-location/?location="+query;
 		Log.e("NANO-DEBUG",queryString);
 		new StopRequest().execute(queryString);
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    // Inflate the menu items for use in the action bar
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.stop_map_actions, menu);
 	    //get the search manager
 	    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
             SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
             SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
             searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
             searchView.setIconifiedByDefault(true);
         }	    
 	    
 	    return super.onCreateOptionsMenu(menu);
 	}
 	
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    // Handle presses on the action bar items
 	    switch (item.getItemId()) {
 		    case R.id.search:
 	           onSearchRequested();// start the search
             return true;
 
 	        case R.id.action_favourite:
 	        	startActivity(new Intent(StopMapActivity.this, com.engine9.FavouriteActivity.class));
 	            return true;
 	        
 	        default:
 	            return super.onOptionsItemSelected(item);
 	    }
 	}
 	
 	/*
 	@Override
     protected void onStart() {
         super.onStart();
         // Connect the client.
         mLocationClient.connect();
     }
 	
 	@Override
     protected void onStop() {
 		mLocationClient.disconnect();
         super.onStart();
         // Connect the client.
         mLocationClient.connect();
     }*/
 	
 	
 	/**
 	 * Initial the map on the screen
 	 * 
 	 * @param center
 	 * 		user's current location
 	 * */
 	private void setUpMap(LatLng center) {
         if (mMap == null) {
         	mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
         	mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
         	if(center != null){
         		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 15));}
         	mMap.setOnMarkerClickListener(new OnMarkerClickListener(){
 
 				@Override
 				public boolean onMarkerClick(Marker m) {
 					for(Stop s : stopVector){
 						if(s.markerId.equals(m.getId())){
 							//Open TimeTableActivity with new intent
 							Intent i = new Intent(com.engine9.StopMapActivity.this, com.engine9.TimetableActivity.class);
 							i.putExtra("timeURL", "http://deco3801-005.uqcloud.net/cache/network/rest/stop-timetables/?stopIds=" + s.stopId);
 							i.putExtra("description", s.address);
 							startActivity(i);
 							return true;
 						}
 					}
 					return false;
 				}
         		
         	});
         	
         	mMap.setOnCameraChangeListener(new OnCameraChangeListener(){
 
 				@Override
 				public void onCameraChange(CameraPosition arg0) {
 					cdt.cancel();
 					cdt.start();	
 				}
         		
         	});
         	
         }
 	}
 	
 	/**
 	 * Return true if Google Play Services is available for the current situation, otherwise 
 	 * return false. The application need to connect to Google Play Services in order to getting
 	 * user's location and use that to find the stop or measure the route
 	 * */
 	private boolean servicesConnected() {
         // Check that Google Play services is available
         int resultCode =
                 GooglePlayServicesUtil.
                         isGooglePlayServicesAvailable(this);
         // If Google Play services is available
         if (ConnectionResult.SUCCESS == resultCode) {
             // In debug mode, log the status
             Log.d("Location Updates",
                     "Google Play services is available.");
             // Continue
             return true;
         // Google Play services was not available for some reason
         } else {
         	 Log.d("Location Updates",
                      "Google Play services is unavailable.");
             }
             return false;
         }
 
 	/**
 	 * If the user is offline or the connection is failure, try to connect the Service
 	 * again in order to solve the problem.
 	 * */
 	@Override
 	public void onConnectionFailed(ConnectionResult connectionResult) {
 		if (connectionResult.hasResolution()) {
             try {
                 // Start an Activity that tries to resolve the error
                 connectionResult.startResolutionForResult(
                         this, 9000);
                 /*
                  * Thrown if Google Play services cancelled the original
                  * PendingIntent
                  */
             } catch (IntentSender.SendIntentException e) {
                 // Log the error
                 e.printStackTrace();
             }
         } else {
             /*
              * If no resolution is available, display a dialog to the
              * user with the error.
              */
         	Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
             Log.e("Error", String.valueOf(connectionResult.getErrorCode()));
         }
 		
 		
 	}
 
 	@Override
 	public void onConnected(Bundle arg0) {
 		 Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
 		
 	}
 
 	@Override
 	public void onDisconnected() {
 		Toast.makeText(this, "Disconnected. Please re-connect.",
                 Toast.LENGTH_SHORT).show();
 		
 	}
 
 	@Override
 	public void onLocationChanged(Location location) {
 		if(location != null && currentLocation == null){
 			Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
 			//Converts location to address string
 			String address;
 			try {
 				address = gcd.getFromLocation(currentLocation.getLatitude(), 
 						currentLocation.getLongitude(), 1).get(0).getLocality();
 				Log.e("DEBUG", address);
 				
 				new StopRequest().execute("http://deco3801-005.uqcloud.net/stops-from-location/?location=" +address);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 			currentLocation = location;
 			
 			mLocationManager.removeUpdates(this);
 		}
 		
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	/**
 	 * Function that converts JsonObject (jData) into a vector (stopVector)
 	 * 
 	 * @param j 
 	 * 		A JsonObject containing stop information
 	 */
 	private void JsonToVector(JsonElement j){
 		//Casts result as JsonArray
 		JsonArray result  = j.getAsJsonArray();
 		stopVector = new Vector<Stop>();
 		//Loops through JsonObjects in result
 		for(int i = 0; i < result.size(); i++){
 			
 			JsonObject stop = result.get(i).getAsJsonObject();
 			//JsonObject route = stop.get("0").getAsJsonObject();
 			
 			//Adds new stop data to stopVector
 			Stop s = new Stop(stop.get("StopId").getAsString(),
 					stop.get("Lat").getAsDouble(),
 					stop.get("Lng").getAsDouble(),
 					stop.get("Description").getAsString(),
 					1);
 			stopVector.add(s);
 		}
 	}
 	
 	/**
 	 * Adds markers to map from stopVector
 	 * */
 	private void addStopsToMap(Boolean moveCamera){
 		for(Stop s : stopVector){
 			if(s.vehicle == 1){
 				Marker marker = mMap.addMarker(new MarkerOptions()
 						.position(new LatLng(s.lat, s.lon))
 						.title(s.address)
 						.icon(BitmapDescriptorFactory.fromResource(R.drawable.greybus)));
 				s.markerId = marker.getId();
 			}
 			else if(s.vehicle == 2){
 				Marker marker = mMap.addMarker(new MarkerOptions()
 				.position(new LatLng(s.lat, s.lon))
 				.title(s.address)
 				.icon(BitmapDescriptorFactory.fromResource(R.drawable.greytrain)));
 				s.markerId = marker.getId();
 			}
 			else if(s.vehicle == 3){
 				Marker marker = mMap.addMarker(new MarkerOptions()
 				.position(new LatLng(s.lat, s.lon))
 				.title(s.address)
 				.icon(BitmapDescriptorFactory.fromResource(R.drawable.greyferry)));
 				s.markerId = marker.getId();
 			}
 		}
 		if(moveCamera){
 		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(stopVector.get(0).lat, stopVector.get(0).lon ), 15));
 		previousPosition = mMap.getCameraPosition().target;
 		}
 	}
 	
 	/**
 	 * It extends the Request class (which handles getRequests)
 	 * the onPostExecute function is overwritten so that the returned JSON
 	 * data can be handled specifically for this activity (to get Stop info)
 	 * */
 	private class StopRequest extends Request{
 		ProgressDialog dialog;
 		@Override
 		public void onPreExecute(){
 			dialog= ProgressDialog.show(StopMapActivity.this, "Downloading stops","Please wait a moment", true);
 		}
 		
 		@Override
 		public void onPostExecute(String result) {
 			try {
 				jData = JParser2.main(result);
 				JsonToVector(jData);
 				addStopsToMap(true);
 				
 			} catch (Exception e) {
				Log.e("Error", result);
 				e.printStackTrace();
 				Toast toast = Toast.makeText(getApplicationContext(), "Error receiving request", Toast.LENGTH_SHORT);
 				toast.show();
 			}
 			dialog.dismiss();
 		}
 	}
 	
 	private class StopRequest2 extends Request{
 		
 		@Override
 		public void onPreExecute(){
 			super.onPreExecute();
 			//pb = new ProgressBar();
 		}
 		
 		@Override
 		public void onPostExecute(String result) {
 			try {
 				jData = JParser2.main(result);
 				JsonToVector(jData);
 				mMap.clear();
 				addStopsToMap(false);
 				
 			} catch (Exception e) {
 				if(result != null){
 					if(result.length() > 0){
 						Log.e("Error", result);
 						e.printStackTrace();
 						Toast toast = Toast.makeText(getApplicationContext(), "Error receiving request", Toast.LENGTH_SHORT);
 						toast.show();
 					}
 				}	
 			}
 			
 		}
 	}
 	
 	/**
 	 * The constructor to store stop info
 	 * */
 	private class Stop{
 		String markerId;
 		Double lat;
 		Double lon;
 		String stopId;
 		String address;
 		int vehicle;
 		
 		public Stop(String stopId, Double lat, Double lon, String address, int vehicle){
 			this.stopId = stopId;
 			this.lat = lat;
 			this.lon = lon;
 			this.address = address;
 			this.vehicle = vehicle;
 		}
 	}
 	
 	private double calculateRadius() {
 		double widthInPixels;
 		
 		DisplayMetrics displaymetrics = new DisplayMetrics();
 		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
 		int height = displaymetrics.heightPixels;
 		int width = displaymetrics.widthPixels;
 		if(width < height){
 			widthInPixels = height;
 		}
 		else{
 			widthInPixels = width;
 		}
 		
 	    double equatorLength = 40075004; // in meters
 	    double metersPerPixel = equatorLength / 256;
 	    for(int i = 1; i < mMap.getCameraPosition().zoom; i++){
 	    	metersPerPixel /= 2;
 	    }
 	    
 	    return (metersPerPixel * widthInPixels);
 	    
 	}
 	
 	/**
 	 * Calculate the distance (in metres) between two LatLng points
 	 * */
 	private double calcDistance(LatLng start, LatLng end){
 		Double dLng = Math.toRadians(end.longitude - start.longitude);
 		Double dLat = Math.toRadians(end.latitude - start.latitude);
 		
 		Double angle = Math.pow((Math.sin(dLat /2)), 2) + Math.cos(start.latitude) * Math.cos(end.latitude) * Math.pow((Math.sin(dLng /2)), 2);
 		Double cir = 2 * Math.atan2(Math.sqrt(angle), Math.sqrt(1-angle));
 		
 		return 6373 * cir * 1000;
 	}
 }
 
