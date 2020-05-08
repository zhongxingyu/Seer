 package com.sp.norsesquare.froyo;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import android.content.Context;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.google.android.gms.maps.CameraUpdate;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.CameraPosition;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.LatLngBounds;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 
 /**
  * This shows how to create a simple activity with a map and a marker on the map.
  * <p>
  * Notice how we deal with the possibility that the Google Play services APK is not
  * installed/enabled/updated on a user's device.
  */
 
 public class NorseSquare extends FragmentActivity 
 {
     /**
      * Note that this may be null if the Google Play services APK is not available.
      * TODO - Add dependency for Google Maps application, must be installed for Maps API to work
      */
     private GoogleMap mMap;
     private CameraUpdate cUpdate;
     boolean releaseLocation;
     private LocationManager locationManager;
     private LatLng currentLocation;
     
     public LocationListener locationListener;
     
     
     private ArrayList<MapMarker> storedMarkerList;
     
     //Get context for use in inner classes
     Context context = this;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.layout_relative_map);
         
         
         //Set up relevant services and listeners for GoogleMap
         storedMarkerList = new ArrayList<MapMarker>();
         
         locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
         
         setUpMap();
         Toast.makeText(this, "Map has been set up.", Toast.LENGTH_SHORT).show();
         
     }
 
     @Override
     protected void onResume() 
     {
         super.onResume();
         
         //Get location manager
         locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
         setUpMap();
     }
     
     
   public void onStart()
   {
 	  //Get location manager, check if wifi and gps are enabled.
 	  
   	super.onStart();
   	
   	// obtain location manager at restart of activity
   	locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
   	
   	//TODO - Determine why all providers are seen as true, all the time
   	final boolean wifiEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
   	final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
   	
   	
   	locationListener = new LocationListener() 
   	{
   		
   		
   		public void onLocationChanged(Location location) 
   		{
   			// Called when a new location is found by the network location provider.
   			//TODO - Find how often this is called, determine if it is too frequent.
   			updateLocation(location);
   			Toast.makeText(context, "Location is being updated", Toast.LENGTH_SHORT).show();
   		}
   	
 
   		@Override
   		public void onProviderDisabled(String arg0)
   		{
   			// TODO Auto-generated method stub
 		
   		}
 
   		@Override
   		public void onProviderEnabled(String arg0)
   		{
   			// TODO Auto-generated method stub
 		
   		}
 
   		@Override
   		public void onStatusChanged(String arg0, int arg1, Bundle arg2)
   		{
   			// TODO Auto-generated method stub
 		
   		}
   		
   	};
   	
   	//TODO - Add dialogfragment to force user to enable the given provider.
   	if (!wifiEnabled)
   	{
   		Toast.makeText(this, "Wifi is not enabled", Toast.LENGTH_LONG).show();
   		System.exit(0);
   	}
   	
   	if (!gpsEnabled)
   	{
   		//put alert box here, for now exit
   		//System.exit(0);
   	}
 
   }
     
    /*Functions for options menus*/  
   
     /*
     @Override
     public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) 
     {
         inflater.inflate(R.menu.menu_main_settings, menu);
         super.onCreateOptionsMenu(menu, inflater);
     } */
   
   
    
     
     @Override
     public boolean onPrepareOptionsMenu(Menu menu)
     {
     	//TODO - Figure out why the ********** this won't work
     	getMenuInflater().inflate(R.menu.menu_main_settings, menu);
     	return true;
     }
     
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         switch (item.getItemId()) 
         {
             case R.id.menu_settings_reveal_location:
                 if (item.isChecked())
                 {
                 	setReleaseLocation(false);
                 	item.setChecked(false);
                 }
                 else
                 {
                 	setReleaseLocation(true);
                 	item.setChecked(true);
                 }
                 return true;
             case R.id.menu_settings_david_duba:
             {
             	if (item.isChecked())
             	{
             		Toast toast = Toast.makeText(this, "Hi Duba!!!", Toast.LENGTH_LONG);
             		toast.show();
             	}
             }
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
     
     public void setReleaseLocation(boolean b)
     {
     	releaseLocation = b;
     }
 
     
     //Functions for GoogleMap
     
     private void setUpMap() 
     {
         // Do a null check to confirm that we have not already instantiated the map.
         if (mMap == null) 
         {
             // Try to obtain the map from the SupportMapFragment.
             mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.main_map)).getMap();
             
             
             //Set onCameraChangeListener to allow for boundaries to be used after "layout"(?)
             mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener()
 			{
 				
 				@Override
 				public void onCameraChange(CameraPosition arg0)
 				{
 					 /*Code for limiting map to Decorah area */
 					//TODO - Don't recalculate every time, only calculate decorah bounds after layout
 					//TODO - Add more precise boundaries
 					//TODO - See if possible to limit zoom capability with these boundaries.
 					
 					  /*Points with which to limit view of map:
 					   * Greater Decorah Area
 				     Southwest: Lat - 43.282454  Long - -91.827679
 				     Northeast: Lat - 43.309191  Long - -91.766739
 				     */
 					
 					
 					LatLng boundSW = new LatLng(43.282454,-91.827679);
 			        LatLng boundNE = new LatLng(43.309191,-91.766739);
 			        
 			        LatLngBounds.Builder builder = new LatLngBounds.Builder();
 			        builder.include(boundSW);
 			        builder.include(boundNE);
 			        
 			        LatLngBounds decorahBound = new LatLngBounds(boundSW,boundNE);
 			   
 			        cUpdate = CameraUpdateFactory.newLatLngBounds(decorahBound, 5);
 					
 					mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 5));
 	                mMap.setOnCameraChangeListener(null);
 					
 				}
 			});
             
          }
         
     }
 
 
     //Methods called from ControlPanel classes
     
     public void wifiLocate(View v)
     {
     	//Called from Control Panel button Wifi Locate, gets wifi location
     	//TODO - Zoom in closer on current location
     	
     	locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 25, locationListener);
     	Location coarseLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
     	
     	updateLocation(coarseLocation);
     	
     }
     
     public void findAll(View v)
     {
        //Gets list of friends and places markers for all of them
     	//TODO - Should we allow for persistent friend locations?
     	
     	/* For now, clear all friend locations before reloading from database */
     	storedMarkerList.clear();
     	
     	/* Add all found friends to database */
     	
     	//Put all found friends in the map
         placeStoredMarkers();
     }
     
     public void placeSingleMarker(View v,LatLng latlong)
     {
     	mMap.clear();
     	
     	//TODO - Make to selectively clear marker per user
     	//TODO - See if need to we do something with the passed in view
     	//TODO - Programmatically alter marker contents for a more in depth user experience
     	
     	Marker cl = mMap.addMarker(new MarkerOptions().position(currentLocation)
     			                                      .title("Current Location")			                                      
     			                                      .snippet(latlong.toString()));
     	
     
     }
     
     public void placeStoredMarkers()
     {
     	mMap.clear();
     	
     	Iterator i = storedMarkerList.iterator();
     	
     	while (i.hasNext())
     	{
     	   MapMarker m = (MapMarker) i.next();
     	   mMap.addMarker(m.getMarkerOptions());
     	}
     }
     
     public void updateLocation(Location l)
     {
     	//Primary method to update location in the map. All other methods should call this one, regardless of provider.
     	
     	//Set current location. This is called from both listeners and buttons, and is done to avoid having to get the location anew every time.
     	//TODO - See if this is lready cached and easily available, refer to location strategies
     	currentLocation = new LatLng(l.getLatitude(),l.getLongitude());
     	
     	LatLng ll = new LatLng(l.getLatitude(),l.getLongitude());
     
     	
     	placeSingleMarker(this.findViewById(R.id.RelativeMapLayout),ll);
     	
     	mMap.moveCamera(CameraUpdateFactory.newLatLng(ll));
     	
     }
     
     //Joel's classes/etc for not location related things.
 
 //	public void popUp(View v)
 //	{
 //		//pops up toast full of text from textbox.
 //		EditText text = (EditText)findViewById(R.id.text_box);
 //		String value = text.getText().toString();
 //		Toast toast = Toast.makeText(this, value, Toast.LENGTH_LONG);
 //		toast.show();
 //	}
     
     
     //Listener classes for location management
     
 
     public void pingURL(View w){
     	
     	AsyncTask<String, Void, HttpEntity> Task = new DatabaseTask().execute((String[])null);
     	try{
     		HttpEntity Hentity = Task.get();
     		String xmlString = EntityUtils.toString(Hentity);
             DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
             DocumentBuilder db = factory.newDocumentBuilder();
             InputSource inStream = new InputSource();
             inStream.setCharacterStream(new StringReader(xmlString));
             Document doc = db.parse(inStream);  
 
             String playcount = "empty";
             NodeList nlist = doc.getElementsByTagName("person");
             
             for(int i = 0; i < nlist.getLength();i++){
 	            NodeList UserInfo = nlist.item(i).getChildNodes();
 	        	String fname = UserInfo.item(0).getTextContent();
 	        	String lname = UserInfo.item(1).getTextContent();
 	        	String username = UserInfo.item(2).getTextContent();
 	        	String googleid = UserInfo.item(3).getTextContent();
 	        	String time = UserInfo.item(4).getTextContent();
 	        	Double longitude = Double.parseDouble(UserInfo.item(5).getTextContent());
 	        	Double latitude = Double.parseDouble(UserInfo.item(6).getTextContent());
 	        	
	        	LatLng locP = new LatLng(longitude,latitude);
 	        	MapMarker newmark = new MapMarker(locP, fname+" "+lname, "checked in at "+ time);
 	        	storedMarkerList.add(newmark);
             }
           
         	placeStoredMarkers();
             
     	}
     	catch(Exception e){
     		Log.i("ERROR", "error in response answer");
     	}
     	
     }
 }
  
