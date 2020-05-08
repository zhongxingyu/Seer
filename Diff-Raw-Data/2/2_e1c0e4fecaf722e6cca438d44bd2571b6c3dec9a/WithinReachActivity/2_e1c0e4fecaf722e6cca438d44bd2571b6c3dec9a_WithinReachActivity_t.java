 package org.leifolson.withinreach;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.Locale;
 import java.util.concurrent.ExecutionException;
 
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
 import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
 import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
 import com.google.android.gms.maps.LocationSource;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.CircleOptions;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.gms.maps.model.TileOverlayOptions;
 import com.google.android.gms.maps.model.TileProvider;
 import com.google.android.gms.maps.model.UrlTileProvider;
 
 
 
 import android.location.Location;
 import android.location.LocationManager;
 import android.location.LocationListener;
 
 import android.location.Location;
 import android.os.AsyncTask;
 
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.annotation.TargetApi;
 import android.support.v4.app.FragmentActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.graphics.Color;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.Toast;
 
 
 public class WithinReachActivity extends FragmentActivity implements
 	LocationListener,
 	LocationSource, 
 	OnMapLongClickListener,
 //	OnMarkerClickListener,
 	OnInfoWindowClickListener{
 	
 	// used as a handle to the map object
 	private GoogleMap mMap;
 	
 	// other private members
 	private OnLocationChangedListener mListener;
 	private LocationManager mLocationManager;
 	private Location mCurrentLocation;
 //	private OnMapLongClickListener mLongClick;
 //	private OnMarkerClickListener mClickListener;
 //	private OnInfoWindowClickListener mWindow;
 
 	
 	//marker
 	private Marker marker;
 	
 	//application resources
 	private Resources appRes;
 	
 	/* TILE TEST CODE */
 	private boolean toggleOTPATiles = false;
 	private static final String OTPA_URL_FORMAT = 
 		"http://queue.its.pdx.edu:8080/opentripplanner-api-webapp/ws/tile/%d/%d/%d.png";
 	
 	// the start latitudes/longitudes define a starting location
 	// of Portland, OR
 	private static final Double startLat = 45.5236;
 	private static final Double startLng = -122.6750;
     private static final LatLng PORTLAND = new LatLng(startLat, startLng);
 	
 
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		// used to access shared resources like strings, etc.
 		appRes = getResources();
 		
 		// inflate the UI
 		setContentView(R.layout.activity_within_reach);
 		
 		// get a location manager
 		mLocationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
 		
 		// attempt to get a provider for the location manager
 		if(mLocationManager != null){
 			if(mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
 				mLocationManager.requestLocationUpdates(
 						LocationManager.GPS_PROVIDER, 5000L, 5F, this);
 			}
 			else if(mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
 				mLocationManager.requestLocationUpdates(
 						LocationManager.NETWORK_PROVIDER, 5000L, 5F, this);
 			}
 			else{
 				// we were unable to obtain a provider
 				Toast.makeText(this, R.string.error_no_provider, Toast.LENGTH_SHORT).show();
 			}
 		}
 		else{
 			// something has gone wrong with loc manager
 			Toast.makeText(this, R.string.error_fatal_loc_mgr, Toast.LENGTH_LONG).show();
 		}
 		
 		// set up the map and settings files if necessary
 		setUpMapIfNeeded();
 		setUpSettingsFile();
 		
 		// set the starting location of the map
     	mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(PORTLAND, 14.0f));
     	
 	}
 	
 	@Override
 	protected void onStart(){
 		super.onStart();
 		
 		mMap.setMyLocationEnabled(true);
 		
 	}
 	
 	@Override
 	protected void onStop(){
 
 		super.onStop();
 	}
 	
 	@Override
 	protected void onPause(){
 		// when paused we do not want to consume resources by updating
 		// the users location
 		if(mLocationManager != null){
 			mLocationManager.removeUpdates(this);
 		}
 		super.onPause();
 	}
 	
 	@Override
 	protected void onResume(){
 		super.onResume();
 
 		setUpMapIfNeeded();
 		if(mMap != null){
 			mMap.setMyLocationEnabled(true);
 		}
 	}
 	
 	/***
 	 *  launches the MenuActivity where the user can specify:
 	 *  time constraint
 	 *  transport mode
 	 */
 	private void startMenu(){
 		Intent launchMenu = new Intent(this,MenuActivity.class);
 		startActivity(launchMenu);
 	}
 
 	/***
 	 * this method inflates the menu UI when the user presses the hardware menu key
 	 * on their android device. 
 	 */
 	// designates that the code present is supported only on targets API 11 and later
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB) 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.within_reach, menu);
 		
 		// make this menu item available in the action bar if API supports it
 		if(Build.VERSION.SDK_INT>11){
 			MenuItem settingsItem = menu.findItem(R.id.action_settings);
 			settingsItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
 			MenuItem refreshItem = menu.findItem(R.id.action_refresh);
 			refreshItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
 		}
 		return true;
 	}
 	
 
 	
 	// handles user selections from menu interface
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item){
 		super.onOptionsItemSelected(item);
 		
 		if (item.getItemId() == R.id.action_settings){
 		
 			// this is the settings menu item
 			// launch the menu activity to specify app settings
 			startMenu();
 			return true;
 		}
 		if (item.getItemId() == R.id.action_refresh)
 		{
 			invokeServerComMgr();
 			return true;
 		}
 		else return false;
 	}
 	
     /**
      * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
      * installed) and the map has not already been instantiated.. This will ensure that we only ever
      * call {@link #setUpMap()} once when {@link #mMap} is not null.
      * <p>
      * If it isn't installed {@link SupportMapFragment} (and
      * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
      * install/update the Google Play services APK on their device.
      * <p>
      * A user can return to this FragmentActivity after following the prompt and correctly
      * installing/updating/enabling the Google Play services. Since the FragmentActivity may not have been
      * completely destroyed during this process (it is likely that it would only be stopped or
      * paused), {@link #onCreate(Bundle)} may not be called again so we should call this method in
      * {@link #onResume()} to guarantee that it will be called.
      */
     private void setUpMapIfNeeded() {
         // Do a null check to confirm that we have not already instantiated the map.
         if (mMap == null) {
             // Try to obtain the map from the SupportMapFragment.
             mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                     .getMap();
             
             // Check if we were successful in obtaining the map.
             if (mMap != null) {
                 setUpMap();
                 mMap.setOnMapLongClickListener(this);
             }
             
             // set location source to track users location over time
             mMap.setLocationSource(this);
             
         }
     }
 
     /**
      * This is where we can add markers or lines, add listeners or move the camera.
      * <p>
      * This should only be called once and when we are sure that {@link #mMap} is not null.
      */
     private void setUpMap() {
     	mMap.setMyLocationEnabled(true);
     	
         // some more TILE TEST CODE  
     	if(toggleOTPATiles){
 	        TileProvider tileProvider = new UrlTileProvider(256, 256) {
 	            @Override
 	            public synchronized URL getTileUrl(int x, int y, int zoom) {
 	
 	                String s = String.format(Locale.US, OTPA_URL_FORMAT, zoom, x, y);
 	                URL url = null;
 	                try {
 	                	s += 
 	                		"?layers=traveltime&styles=color30&batch=true&mode=TRANSIT%2CWALK&" +
 	                    		"maxWalkDistance=2000&time=2013-07-10T08%3A00%3A00&"+
 	                    		"fromPlace=45.51212126820532%2C-122.62321472167969&toPlace=45.381403%2C-122.27416674999999";
 	                     url = new URL(s);
 	                } catch (MalformedURLException e) {
 	                    throw new AssertionError(e);
 	                }
 	                return url;
 	            }
 	        };
 	        
 	        
 	        TileOverlayOptions opts = new TileOverlayOptions();
 	        opts.tileProvider(tileProvider);
 	        opts.zIndex(5);
 	        
 	    	
 	        mMap.addTileOverlay(opts);
     	}
         
     }
     
     private void setUpSettingsFile()
     {
     	GregorianCalendar calendar = (GregorianCalendar)Calendar.getInstance();
 		int day = calendar.get(Calendar.DAY_OF_WEEK);
 		int month = calendar.get(Calendar.MONTH);
 		int year = calendar.get(Calendar.YEAR);
 		int time = 200;
     	JSONObject settingsJson = new JSONObject();
     	
     	double latitude = WithinReachActivity.startLat;
     	double longitude = WithinReachActivity.startLng;
 		try 
 		{
 			
 			settingsJson.put("lat", latitude);
 			settingsJson.put("long", longitude);
 			settingsJson.put("time", time);
 			settingsJson.put("day", day);
 			settingsJson.put("month", month);
 			settingsJson.put("year", year);
 			settingsJson.put("mode", 7);
 			settingsJson.put("constraint", 15);
 		} 
 		catch (JSONException e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		FileOutputStream fstream;
 		try 
 		{
 			fstream = openFileOutput("settingsJson.txt", Context.MODE_PRIVATE);
 
 			fstream.write(settingsJson.toString().getBytes());
 			
 		} 
 		catch (FileNotFoundException e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		catch (IOException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
     	
     }
 	
     //This gets called from MenuActivity when it launches the WithinReachActivity
 	public void onNewIntent(Intent t) 
 	{
 		Bundle extras = t.getExtras();
 		if (extras != null)
 		{
 			int serverDone = extras.getInt("serverCallDone");
 			if (serverDone == 1)
 			{
 				handleDataFile();		
 			}
 		}
 		
 	}
 	
 	public void handleDataFile()
 	{
 		FileInputStream fileInputStream = null;
 		try
 		{
 			fileInputStream = openFileInput("jsonResult.txt");
 		} 
 		catch (FileNotFoundException e) 
 		{
 			e.printStackTrace();
 		}
 		
 		InputStreamReader inputStreamReader = new InputStreamReader ( fileInputStream ) ;
         BufferedReader bufferedReader = new BufferedReader ( inputStreamReader ) ;
         String stringReader;
         String fullString = "";
         try 
         {
 	        while ((stringReader = bufferedReader.readLine()) != null)
 	        {
 	        	fullString += stringReader;
 	        }
 	        fileInputStream.close();
 
         }
         catch (IOException e)
         {
         	e.printStackTrace();
         	
         }
         
         
         LatLng circleLocation = null;
         
         if (marker != null)
         {
         	LatLng markerLocation = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
         	circleLocation = markerLocation;
             
         	mMap.clear();
         	
         	marker = makeMapMarker(markerLocation,appRes.getString(R.string.delete_marker));
         }
         else
         {
         	// check if we have a current location yet
         	if(mCurrentLocation != null){
         		circleLocation = new LatLng(mCurrentLocation.getLatitude(), 
         				                    mCurrentLocation.getLongitude());
         	}
         	// no location obtained...set to default
         	else{ 
         		circleLocation = PORTLAND;
         	}
         	mMap.clear();
         }
 
 
         try 
         {
 			JSONObject jsonObject = new JSONObject(fullString);
 	
 
 			
 			
 			if (jsonObject.getJSONObject("result").has("4"))
 			{
 				double lat1 = jsonObject.getJSONObject("result").getJSONObject("4").getJSONArray("coordinate").getJSONObject(0).getDouble("lat");
 				double long1 = jsonObject.getJSONObject("result").getJSONObject("4").getJSONArray("coordinate").getJSONObject(0).getDouble("long");
 				
 				double lat2 = jsonObject.getJSONObject("result").getJSONObject("4").getJSONArray("coordinate").getJSONObject(1).getDouble("lat");
 				double long2 = jsonObject.getJSONObject("result").getJSONObject("4").getJSONArray("coordinate").getJSONObject(1).getDouble("long");
 				
 				double distance = distFrom(lat1, long1, lat2, long2);
 
 				
 				
 				
 				CircleOptions options = new CircleOptions();
 		        
 		        options.center(circleLocation);
 		        options.radius(distance);
 		        options.fillColor(0x50000000);
 		        options.strokeColor(Color.TRANSPARENT);
 		        
 		        mMap.addCircle(options);
 				
 			}
 			
 			if (jsonObject.getJSONObject("result").has("2"))
 			{
 				double lat1 = jsonObject.getJSONObject("result").getJSONObject("2").getJSONArray("coordinate").getJSONObject(0).getDouble("lat");
 				double long1 = jsonObject.getJSONObject("result").getJSONObject("2").getJSONArray("coordinate").getJSONObject(0).getDouble("long");
 				
 				double lat2 = jsonObject.getJSONObject("result").getJSONObject("2").getJSONArray("coordinate").getJSONObject(1).getDouble("lat");
 				double long2 = jsonObject.getJSONObject("result").getJSONObject("2").getJSONArray("coordinate").getJSONObject(1).getDouble("long");
 				
 				double distance = distFrom(lat1, long1, lat2, long2);
 
 				
 				
 				CircleOptions options = new CircleOptions();
 		        options.center(circleLocation);
 		        options.radius(distance);
 		        options.fillColor(0x30ff0000);
 		        options.strokeColor(Color.TRANSPARENT);
 		        
 		        mMap.addCircle(options);
 			}
 			
 			if (jsonObject.getJSONObject("result").has("1"))
 			{
 				double lat1 = jsonObject.getJSONObject("result").getJSONObject("1").getJSONArray("coordinate").getJSONObject(0).getDouble("lat");
 				double long1 = jsonObject.getJSONObject("result").getJSONObject("1").getJSONArray("coordinate").getJSONObject(0).getDouble("long");
 				
 				double lat2 = jsonObject.getJSONObject("result").getJSONObject("1").getJSONArray("coordinate").getJSONObject(1).getDouble("lat");
 				double long2 = jsonObject.getJSONObject("result").getJSONObject("1").getJSONArray("coordinate").getJSONObject(1).getDouble("long");
 				
 				double distance = distFrom(lat1, long1, lat2, long2);
 	
 				
 				CircleOptions options = new CircleOptions();
 		        options.center(circleLocation);
 		        options.radius(distance);
 		        options.fillColor(0x60ffff00);
 		        options.strokeColor(Color.TRANSPARENT);
 
 		        mMap.addCircle(options);
 				
 			}
 			
 
 			
 			
 			
 		}
         catch (JSONException e) 
 		{
 
 			e.printStackTrace();
 		}
         
     
 		
 	}
 	
 
 	@Override
 	public void onLocationChanged(Location location){
 		
 		mCurrentLocation = location;
 		
 		// update map camera to current location
 		if(mListener != null && mMap != null){
 			mListener.onLocationChanged(location);
 			mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(
 					location.getLatitude(),location.getLongitude())));
 		}
 	}
 
   
 
 	@Override
 	public void activate(OnLocationChangedListener listener) {
 		// TODO Auto-generated method stub
 		mListener = listener;
 	}
 
 	@Override
 	public void deactivate() {
 		// TODO Auto-generated method stub
 		mListener = null;
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
 	
 	public  double distFrom(double lat1, double lng1, double lat2, double lng2)
 	{
 	    double earthRadius = 6378000;
 	    double dLat = Math.toRadians(lat2-lat1);
 	    double dLng = Math.toRadians(lng2-lng1);
 	    double sindLat = Math.sin(dLat / 2);
 	    double sindLng = Math.sin(dLng / 2);
 	    double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
 	            * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
 	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
 	    double dist = earthRadius * c;
 	
 	    return dist;
     }
 	
 	public void invokeServerComMgr()
 	{
 		
 		Handler asyncHandler = new Handler()
 		{
 		    public void handleMessage(Message msg){
 		        super.handleMessage(msg);
 		        //What did that async task say?
 		        switch (msg.what)
 		        {
 		            case 1:
 		                handleDataFile();
 		                break;                      
 		        }
 		    }
 		}; 
 		
 		
 		
 		FileInputStream fileInputStream = null;
 		try
 		{
 			fileInputStream = openFileInput("settingsJson.txt");
 		} 
 		catch (FileNotFoundException e) 
 		{
 			e.printStackTrace();
 		}
 		
 		InputStreamReader inputStreamReader = new InputStreamReader ( fileInputStream ) ;
         BufferedReader bufferedReader = new BufferedReader ( inputStreamReader ) ;
         String stringReader;
         String fullString = "";
         try 
         {
 	        while ((stringReader = bufferedReader.readLine()) != null)
 	        {
 	        	fullString += stringReader;
 	        }
 	        fileInputStream.close();
 
         }
         catch (IOException e)
         {
         	e.printStackTrace();
         	
         }
         
 		try
 		{
 			JSONObject settingsJson = new JSONObject(fullString);
 			double latitude = 0;
 			double longitude = 0;
 			if (marker == null)
 			{
 				latitude = mCurrentLocation.getLatitude();
 				longitude = mCurrentLocation.getLongitude();
 				
 
 			}
 			else
 			{
 				 latitude = marker.getPosition().latitude;
 				 longitude = marker.getPosition().longitude;
 			}
 			int time = 200;
 			int mode_code = settingsJson.getInt("mode");
 			int time_constraint = settingsJson.getInt("constraint");
 			int day = settingsJson.getInt("day");
 			int month = settingsJson.getInt("month");
 			int year = settingsJson.getInt("year");
 
 			settingsJson.put("lat", latitude);
 			settingsJson.put("long", longitude);
 			
 			String url = "http://withinreach.herokuapp.com/json?";
 			url += ("lat=" + latitude);
 			url += ("&long=" + longitude);
 			url += ("&time=" + time);
 			url += ("&day=" + day);
 			url += ("&month=" + month);
 			url += ("&year=" + year);
 			url += ("&mode_code=" + mode_code);
 			url += ("&constraint=" + time_constraint);
 	
 			FileOutputStream fstream;
 			try 
 			{
 				fstream = openFileOutput("settingsJson.txt", Context.MODE_PRIVATE);
 
 				fstream.write(settingsJson.toString().getBytes());
 				
 				fstream.close();
 				
 			} 
 			catch (FileNotFoundException e) 
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			catch (IOException e)
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 			new ServerComMgr(this, asyncHandler).execute(url);
 		}
 		catch (JSONException e) 
 		{
 			
 			
 		}
 				
 	}
 
 	@Override
 	public void onMapLongClick(LatLng point) {
 
 
 		// if a marker has already been created then move to new position
 
 		if (marker != null)
 			marker.setPosition(point);
 		
 		// otherwise create a new marker at the clicked on position
 		else
 		{
 			marker = mMap.addMarker(new MarkerOptions()
 	   	 	.visible(true)
 	        .position(point)
 	        .title("Delete?"));
 	        marker.setDraggable(true);
 		}
 		
 			marker = makeMapMarker(point,appRes.getString(R.string.delete_marker));   
		
 		// listen for info window clicks to delete marker
 
 		mMap.setOnInfoWindowClickListener(this);
 		
 }
 
 //	@Override
 //	public boolean onMarkerClick(Marker arg0) {
 //		// TODO Auto-generated method stub
 //		
 //		return false;
 //	}
 
 	@Override
 	// handle info window clicks by deleting the marker
 	public void onInfoWindowClick(Marker arg0) {
 		System.out.println("Info Window Click");
 		marker.remove();
 		marker = null;
 	}
 	
 	// returns a visible marker at the passed in position
 	// with the passed in title
 	private Marker makeMapMarker(LatLng point, String title){
 		return mMap.addMarker(new MarkerOptions()
 			.visible(true)
 			.position(point)
 			.title(title)
 			.draggable(true));
 	}
 	
 
 }
