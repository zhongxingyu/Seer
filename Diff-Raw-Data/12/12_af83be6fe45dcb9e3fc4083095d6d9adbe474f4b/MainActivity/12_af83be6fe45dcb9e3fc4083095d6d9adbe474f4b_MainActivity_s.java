 package edu.uci.zotfinder;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.Toast;
 import android.support.v4.app.FragmentManager;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.view.SubMenu;
 import com.example.test2app.R;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.gms.maps.model.Polyline;
 import com.google.android.gms.maps.model.PolylineOptions;
 
 import edu.uci.zotfinder.DirectionsJSONParser;
 
 
 
 public class MainActivity extends SherlockFragmentActivity {
 	//map
 	private GoogleMap mMap;
 
 	//LatLng coordinate for default Map focus/centering 
 	static final LatLng UCI = new LatLng(33.6455843, -117.8419771);
 	
 	//coordinate for user's current location
 	//this coordinate is dynamic
 	public LatLng currentLocation;
 	protected LatLng destinationPoint;
 	Polyline polyline = null;
 	boolean directionsToggle = false;
 	private static ArrayList<Marker> emergencyAreas = new ArrayList<Marker>();
 	private static ArrayList<Marker> bluePhones = new ArrayList<Marker>();
 	private static ArrayList<Marker> restrooms = new ArrayList<Marker>();
 	//---Booleans to show or hide markers---
 	//eaShow=true - Show all Emergency Area Markers
 	//eaShow=false - Hide all Emegency Area Markers
 	protected static boolean eaShow=true;
 	//bpShow=true - Show all Blue Phone Post Markers
 	//bpShow=false - Hide all Blue Phone Post Markers
 	protected static boolean bpShow=true;
 	//rrShow=true - Show all Restroom Markers
 	//rrSHow=false - Hide all Restroom Markers
 	protected static boolean rrShow=true;
 	
 	
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
 
 		menu.add("Directions")
         .setIcon(R.drawable.ic_action_directions)
         .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
 		
 		menu.add("Search")
         .setIcon(R.drawable.ic_search)
         .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
 		SharedPreferences settings = getSharedPreferences("ZotFinder Preferences", 0);
 		SubMenu subMenu1 = menu.addSubMenu(Menu.NONE, 0, Menu.NONE, "Action Item");
         subMenu1.add(1, 1, 1, "Blue Phone Posts").setCheckable(true).setChecked(settings.getBoolean("bp", true));
         subMenu1.add(1, 2, 2, "Emergency Areas").setCheckable(true).setChecked(settings.getBoolean("ea", true));
         subMenu1.add(1, 3, 3, "Restrooms").setCheckable(true).setChecked(settings.getBoolean("rr", true));
         subMenu1.add(Menu.NONE, 4, 4, "About Us");
 
 
         MenuItem subMenu1Item = subMenu1.getItem();
         subMenu1Item.setIcon(R.drawable.ic_drawer);
         subMenu1Item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
 
 /*      SubMenu subMenu2 = menu.addSubMenu("Overflow Item");
         subMenu2.add("These");
         subMenu2.add("Are");
         subMenu2.add("Sample");
         subMenu2.add("Items");
 
         MenuItem subMenu2Item = subMenu2.getItem();
         subMenu2Item.setIcon(R.drawable.ic_compose);*/
 
         return super.onCreateOptionsMenu(menu);
     } 
 	
 	@Override
 	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
 		
 		String itemTitle = item.getTitle().toString();
 		
 		if (itemTitle.equals("Blue Phone Posts")){
 			toggleBluePhone();
 			item.setChecked(bpShow);
 			return false;
 		}
 		if (itemTitle.equals("Emergency Areas")){
 			toggleEmergencyMarker();
 			item.setChecked(eaShow);
 			return false;
 		}
 		if (itemTitle.equals("Restrooms")){
 			toggleRestroom();
 			item.setChecked(rrShow);
 			return false;
 		}
 		if (itemTitle.equals("About Us")){
 			return false;
 		}
 		if(itemTitle.equals("Search")){
 			Intent intent = new Intent(this,SearchActivity.class);
 			startActivity(intent);
 			return false;
 		}
 		if(itemTitle.equals("Directions")){
 			if(destinationPoint!=null){
 				findDirections(destinationPoint);
 			}
 			return false;
 		}
 		return false;
 	};
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		getSupportActionBar().setTitle("Map");
 		getSupportActionBar().setHomeButtonEnabled(true);
 		
 		View commonFooter = findViewById(R.id.footer);
 		Button dialerButton = (Button) commonFooter.findViewById(R.id.dialerLinkButton);
 		Button emergencyButton = (Button) commonFooter.findViewById(R.id.emergencyLinkButton);
 		Button mapButton = (Button) commonFooter.findViewById(R.id.mapLinkButton);	
 		mapButton.setBackgroundResource(R.drawable.map_icon_pressed);
 		emergencyButton.setBackgroundResource(R.drawable.emergency_icon);
 		dialerButton.setBackgroundResource(R.drawable.dialer_icon);
 	    
 	    //Initialize Map
 		//Google maps requires the map to be a fragment 
 		//SupportFragment allows older model android phones to display the map (as opposed to just a fragment)
 	    FragmentManager fragmentManager = getSupportFragmentManager();
         SupportMapFragment mapFragment =  (SupportMapFragment) fragmentManager.findFragmentById(R.id.map);
         mMap = mapFragment.getMap();
 	   
         //set UI settings
         mMap.getUiSettings().setZoomControlsEnabled(true);
         mMap.getUiSettings().setCompassEnabled(true);
         mMap.getUiSettings().setMyLocationButtonEnabled(true);
         mMap.getUiSettings().setAllGesturesEnabled(true);
         mMap.setMyLocationEnabled(true);
         
         //sets current location
         double[] d = getlocation();
 	    currentLocation = new LatLng(d[0], d[1]);
 	    
 	    //add uci marker and set zoom
 	     if (mMap!=null){
 	    	 addMarkers();
 
 	    	 if(getIntent().getExtras() != null){
 	    		 int type = getIntent().getExtras().getInt("type");
 		         if (type ==1)
 		         {
 		        	 float latitude = getIntent().getExtras().getFloat("buildingLatitude");
 		        	 float longitude = getIntent().getExtras().getFloat("buildingLongitude");
 		        	 int id = getIntent().getExtras().getInt("BUILDING_ID");
 		        	 String name = getIntent().getExtras().getString("buildingName");
 		        	 String address = getIntent().getExtras().getString("buildingAddress");
 		        	 String number = getIntent().getExtras().getString("buildingNumber");
 		        	 
 		        	 mMap.addMarker(new MarkerOptions()
 		        	 	.position(new LatLng(latitude, longitude))
 		        	 	.title(name)
 		        	 	.snippet("Address: " + address))
 		        	 	.showInfoWindow();
 		        	 mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15));
 			         mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
 			         destinationPoint = new LatLng(latitude, longitude);
 		         }
 		         else if(type==2)
 		         {
 		        	 float latitude = getIntent().getExtras().getFloat("departmentLatitude");
 		        	 float longitude = getIntent().getExtras().getFloat("departmentLongitude");
 		        	 int id = getIntent().getExtras().getInt("DEPARTMENT_ID");
 		        	 String name = getIntent().getExtras().getString("departmentName");
 		        	 String address = getIntent().getExtras().getString("departmentAddress");
 		        	 String phoneNumber = getIntent().getExtras().getString("departmentPhoneNumber");
 		        	 String website = getIntent().getExtras().getString("departmentWebsite");
 		        	 
 		        	 mMap.addMarker(new MarkerOptions()
 		        	 	.position(new LatLng(latitude, longitude))
 		        	 	.title(name)
		        	 	.snippet("Address: " + address));
 		        	 mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15));
 			         mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
 			         destinationPoint = new LatLng(latitude, longitude);
 		         }
 		         else if (type==3)
 		         {
 		        	 float latitude = getIntent().getExtras().getFloat("officeLatitude");
 		        	 float longitude = getIntent().getExtras().getFloat("officeLongitude");
 		        	 String name = getIntent().getExtras().getString("personName");
 		        	 String officeLocation = getIntent().getExtras().getString("officeAddress");
 		        	 
 		        	 mMap.addMarker(new MarkerOptions()
 		        	 	.position(new LatLng(latitude, longitude))
 		        	 	.title(name)
		        	 	.snippet("Office Location: " + officeLocation));
 		        	 	mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
 		        	 	destinationPoint = new LatLng(latitude, longitude);
 		         }
 		         else if(type==4)
 		         {
 		        	 float latitude = getIntent().getExtras().getFloat("serviceLatitude");
 		        	 float longitude = getIntent().getExtras().getFloat("serviceLongitude");
 		        	 int id = getIntent().getExtras().getInt("SERVICE_ID");
 		        	 String name = getIntent().getExtras().getString("serviceName");
 		        	 String address = getIntent().getExtras().getString("serviceAddress");
 		        	 
 		        	 mMap.addMarker(new MarkerOptions()
 		        	 	.position(new LatLng(latitude, longitude))
 		        	 	.title(name)
		        	 	.snippet("Address: " + address));
 		        	 mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15));
 			         mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
 			         destinationPoint = new LatLng(latitude, longitude);
 		         }
 	    	 }
 	    	 else
 	    	 {
 	    		 //Animates the camera to the LatLng coordinate "UCI" which acts as the center
 		         //initial zoom is set to 15 but this can be changed
 		         mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(UCI, 15));
 		         mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null); 
 	    	 }
 	     }
 	    	SharedPreferences settings = getSharedPreferences("ZotFinder Preferences", 0);
 	 		bpShow = settings.getBoolean("bp", true);
 	    	eaShow = settings.getBoolean("ea", true);
 	 		rrShow = settings.getBoolean("rr", true);
 	 		if(!bpShow)
  			{
 	 			bpShow = true;
 	 			toggleBluePhone();
  			}
 	 		if(!eaShow)
  			{
 	 			eaShow = true;
 	 			toggleEmergencyMarker();
  			}
 	 		if(!rrShow)
 	 		{
 	 			rrShow = true;
 	 			toggleRestroom();
  			}
 	}
 	
 	//displays directions on the screen from current location to given point
 	//currently the end point is hardcoded as BP2 (33.64387631680, -117.82420840800)
 	//this will be changed later to be the end destinate of whatever the user picks
 	public void findDirections(LatLng xy){
 		
 		if(!directionsToggle){
 		    // Getting URL to the Google Directions API given start point and end point
 	        String url = getDirectionsUrl(currentLocation, xy);
 	        
 	        DownloadTask downloadTask = new DownloadTask();
 	
 	        // Start downloading json data from Google Directions API
 	        downloadTask.execute(url);
 	        directionsToggle = true;
 		}
 		else {
 			polyline.remove();
 			directionsToggle = false;
 		}
 	}
 	
 	
 	//String output of directions in JSON given by Google Maps
 	//LatLng origin - coordinates of starting point
 	//LatLng dest - coordinates of ending point
     private String getDirectionsUrl(LatLng origin,LatLng dest){
  
         // Origin of route
     	// lat and long of starting point
         String str_origin = "origin="+origin.latitude+","+origin.longitude;
  
         // Destination of route
         // lat and long of destination
         String str_dest = "destination="+dest.latitude+","+dest.longitude;
  
         // Sensor enabled
         // sensor=true if currentLocation is provided by a sensor
         // sensor=false if currentLocation is not provided by a sensor
         // has no real baring on result; just for google's purposes
         String sensor = "sensor=true";
         
         // Building the parameters to the web service
         String parameters = str_origin+"&"+str_dest+"&"+sensor+"&"+"mode=walking";
  
         // Output format
         String output = "json";
  
         // Building the url to the web service
         String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;
  
         return url;
     }
  
     // A method to download json data from url
     private String downloadUrl(String strUrl) throws IOException{
     	//initializing the output string, InputStream, and HttpURLConnection used to make an http request
         String data = "";
         InputStream iStream = null;
         HttpURLConnection urlConnection = null;
         try{
         	//setting url as the url provided in the parameters of the method
             URL url = new URL(strUrl);
  
             // Creating an http connection to communicate with url
             urlConnection = (HttpURLConnection) url.openConnection();
  
             // Connecting to url
             urlConnection.connect();
  
             // Reading data from url
             iStream = urlConnection.getInputStream();
  
             // Create buffered Reader to read the output provided by InputStream
             BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
             
             //StringBuffer used to parse the bufferedReader
             StringBuffer sb = new StringBuffer();
             
             //puts each line read from the bufferedReader into a new line in the string:"data"
             String line = "";
             while( ( line = br.readLine()) != null){
                 sb.append(line);
             }
  
             data = sb.toString();
  
             br.close();
  
         }catch(Exception e){
             Log.d("Exception while downloading url", e.toString());
         }finally{
             iStream.close();
             urlConnection.disconnect();
         }
         return data;
     }
  
     // Fetches data from url passed
     private class DownloadTask extends AsyncTask<String, Void, String>{
  
         // Downloading data in non-ui thread
         @Override
         protected String doInBackground(String... url) {
  
             // For storing data from web service
             String data = "";
  
             try{
                 // Fetching the data from web service
                 data = downloadUrl(url[0]);
             }catch(Exception e){
                 Log.d("Background Task",e.toString());
             }
             return data;
         }
  
         // Executes in UI thread, after the execution of
         // doInBackground()
         @Override
         protected void onPostExecute(String result) {
             super.onPostExecute(result);
  
             ParserTask parserTask = new ParserTask();
  
             // Invokes the thread for parsing the JSON data
             parserTask.execute(result);
         }
     }
  
     // A class to parse the Google Places in JSON format
     private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{
  
         // Parsing the data in non-ui thread
         @Override
         protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
  
             JSONObject jObject;
             List<List<HashMap<String, String>>> routes = null;
  
             try{
                 jObject = new JSONObject(jsonData[0]);
                 DirectionsJSONParser parser = new DirectionsJSONParser();
  
                 // Starts parsing data
                 routes = parser.parse(jObject);
             }catch(Exception e){
                 e.printStackTrace();
             }
             return routes;
         }
  
         // Executes in UI thread, after the parsing process
         @Override
         protected void onPostExecute(List<List<HashMap<String, String>>> result) {
             ArrayList<LatLng> points = null;
             PolylineOptions lineOptions = null;
  
             // Traversing through all the routes
             for(int i=0;i<result.size();i++){
                 points = new ArrayList<LatLng>();
                 lineOptions = new PolylineOptions();
  
                 // Fetching i-th route
                 List<HashMap<String, String>> path = result.get(i);
  
                 // Fetching all the points in i-th route
                 for(int j=0;j<path.size();j++){
                     HashMap<String,String> point = path.get(j);
  
                     double lat = Double.parseDouble(point.get("lat"));
                     double lng = Double.parseDouble(point.get("lng"));
                     LatLng position = new LatLng(lat, lng);
  
                     points.add(position);
                 }
 
                 // Adding all the points in the route to LineOptions
                 lineOptions.addAll(points);
                 lineOptions.width(5);
 
                 // Changing the color polyline according to the mode
                 lineOptions.color(Color.BLUE);
             }
  
             if(result.size()<1){
                 Toast.makeText(getBaseContext(), "No Points", Toast.LENGTH_SHORT).show();
                 return;
             }
  
             // Drawing polyline in the Google Map for the i-th route
           polyline = mMap.addPolyline(lineOptions);
         }
     }
 	
 	//Toggles the Emergency Area markers on or off (depending on the state of the boolean)
 	public void toggleEaMarker(View v){
 		toggleEmergencyMarker();
 		//animates the camera back to initial center point (UCI)
 		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(UCI, 15));
 	    mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
 	}
 	
 	//Toggles the Blue Phone Post markers on or off (depending on the state of the boolean)
 	public void toggleBpMarker(View v){
 		toggleBluePhone();
 		//animates the camera back to initial center point (UCI)
 		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(UCI, 15));
 	    mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
 	}
 	
 	//Toggles the Restroom markers on or off (depending on the state of the boolean)
 	public void toggleRrMarker(View v){
 		toggleRestroom();
 		//animates the camera back to initial center point (UCI)
 		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(UCI, 15));
 	    mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
 	}
 	
 	//Finds the current location of the user
 	//sets the global LatLng coordinate:"currentLocation"
 	public void findLocation(View v){
 		double[] d = getlocation();
 	    currentLocation = new LatLng(d[0], d[1]);
         mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
 	}
 	
 	//Uses system's location service to provide an array of 2 doubles that define the Lat and Long of current location
 	public double[] getlocation() {
 	    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 	    List<String> providers = lm.getProviders(true);
 
 	    Location l = null;
 	    for (int i = 0; i < providers.size(); i++) {
 	        l = lm.getLastKnownLocation(providers.get(i));
 	        if (l != null)
 	            break;
 	    }
 	    double[] gps = new double[2];
 
 	    if (l != null) {
 	        gps[0] = l.getLatitude();
 	        gps[1] = l.getLongitude();
 	    }
 	    return gps;
 	}
 	
 	public void addMarkers()
 	{
 		InputStream emergencyIS = getResources().openRawResource(R.raw.assembly_areas);
 		InputStream bluePhoneIS = getResources().openRawResource(R.raw.blue_phones);
 		InputStream restroomIS = getResources().openRawResource(R.raw.restrooms);	
 		String line = "";
 		BufferedReader br = null;
 		try {
 			br = new BufferedReader(new InputStreamReader(emergencyIS));
 			while ((line = br.readLine()) != null)
 			{
 				String[] input = line.split(",");
 				emergencyAreas.add(mMap.addMarker(new MarkerOptions().position(new LatLng(Float.valueOf(input[2]),Float.valueOf(input[3]))).title(input[0]).icon(BitmapDescriptorFactory.fromResource(R.drawable.emergency_area_icon))));
 			}
 			emergencyIS.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		try {
 			br = new BufferedReader(new InputStreamReader(bluePhoneIS));
 			while ((line = br.readLine()) != null)
 			{
 				String[] input = line.split(",");
 				bluePhones.add(mMap.addMarker(new MarkerOptions().position(new LatLng(Float.valueOf(input[4]),Float.valueOf(input[3]))).title(input[2]).icon(BitmapDescriptorFactory.fromResource(R.drawable.blue_phone_icon))));
 			}
 			bluePhoneIS.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		try {
 			br = new BufferedReader(new InputStreamReader(restroomIS));
 			while ((line = br.readLine()) != null)
 			{
 				String[] input = line.split(",");
 				restrooms.add(mMap.addMarker(new MarkerOptions().position(new LatLng(Float.valueOf(input[2]),Float.valueOf(input[1]))).title("Restroom").icon(BitmapDescriptorFactory.fromResource(R.drawable.restroom_icon))));
 			}
 			restroomIS.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void toggleBluePhone(){
 		SharedPreferences settings = getSharedPreferences("ZotFinder Preferences", 0);
 		SharedPreferences.Editor editor = settings.edit();
 		//if bpShow=true
 		if(bpShow){
 			//hide all Blue Phone Post markers
 			for (Marker m : bluePhones){
 				m.setVisible(false);
 			}
 			bpShow = false;
 			editor.putBoolean("bp", bpShow);
 		}
 		//else if bpShow=false
 		else{
 			//show all Blue PHone Post Markers
 			for (Marker m : bluePhones){
 				m.setVisible(true);
 			}
 			bpShow = true;
 			editor.putBoolean("bp", bpShow);
 		}
 		editor.commit();
 	}
 	
 	public void toggleEmergencyMarker(){
 		SharedPreferences settings = getSharedPreferences("ZotFinder Preferences", 0);
 		SharedPreferences.Editor editor = settings.edit();
 		//if eaShow=true
 		if(eaShow){
 			//hide all the emergency area markers
 			for(Marker m :emergencyAreas){
 				m.setVisible(false);
 			}
 			eaShow = false;
 			editor.putBoolean("ea", eaShow);
 		}
 		//else if eaShow=false
 		else{
 			//show all the emergency area markers
 			for( Marker m : emergencyAreas){
 				m.setVisible(true);
 			}
 			eaShow = true;
 			editor.putBoolean("ea", eaShow);
 		}
 		editor.commit();
 	}
 	
 	public void toggleRestroom(){
 		SharedPreferences settings = getSharedPreferences("ZotFinder Preferences", 0);
 		SharedPreferences.Editor editor = settings.edit();
 		//if rrShow=true
 		if(rrShow){
 			//hide all Restroom markers
 			for (Marker m : restrooms){
 				m.setVisible(false);
 			}
 			rrShow = false;
 			editor.putBoolean("rr", rrShow);
 		}
 		//else if 
 		else{
 			//show all Restroom markers
 			for (Marker m : restrooms){
 				m.setVisible(true);
 			}
 			rrShow = true;
 			editor.putBoolean("rr", rrShow);
 		}
 		editor.commit();
 	}
 	 
 	//Footer Methods
 	
 	//method to go to activity: MainActivity
 	//creates intent used to store the information of a different activity within this activity
 	//startActivity(intent) changes the current activity to the intent activity
 	public void goToMap(View view) { 
 	}
 	 
 	//method to go to activity: EmergencyActivity
 	//creates intent used to store the information of a different activity within this activity
 	//startActivity(intent) changes the current activity to the intent activity
 	public void goToEmergencyInfo(View view) { 
 		Intent intent = new Intent(this,EmergencyActivity.class);
 		startActivity(intent);
 	}
 
 	//method to go to activity: DialerActivity
 	//creates intent used to store the information of a different activity within this activity
 	//startActivity(intent) changes the current
 	public void goToEmergencyDialer(View view) { 
 		Intent intent = new Intent(this,DialerActivity.class);
 		startActivity(intent);
 	}
 	
 	public void goToSearch(View view){
 		Intent intent = new Intent(this,SearchActivity.class);
 		startActivity(intent);
 	}
 	
 }
