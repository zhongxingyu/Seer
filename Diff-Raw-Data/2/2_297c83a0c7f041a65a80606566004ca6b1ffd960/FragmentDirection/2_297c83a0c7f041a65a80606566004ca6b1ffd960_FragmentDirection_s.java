 package com.csc591.view;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 import org.w3c.dom.Document;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.view.Window;
 
 import com.csc591.DAL.Destination;
 import com.csc591.utils.GMapDirectionHelper;
 import com.csc591.view.MyLocationListener.onMyLocationChangeHandler;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.MapFragment;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.gms.maps.model.PolylineOptions;
 
 // NOTE: IMP - Ideally this class should be a fragment instead of activity but 
 // because of complexity in implementation of NestedFragment (not supported in 
 // pervious version) using Activity here. Though no difference on GUI.
 public class FragmentDirection extends Activity implements onMyLocationChangeHandler {
 
 	// dummy LatLng constant - Unused
 	static final LatLng NORTHHILL = new LatLng(53.558,9.927); 
 	
 	private GoogleMap map;
 	private LatLng sourceLatLng;
 	private LatLng destinationLatLng;
 	private LocationManager myLocationManager;
 	private MyLocationListener myLocationListener;
 	private Boolean isGPSEnabled;
 	
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		// Removes title Bar
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 				
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.fragment_directions);
 		
 		try
 		{
 		this.myLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
 		this.myLocationListener = new MyLocationListener();
 		this.myLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myLocationListener);
 		}
 		catch (Exception e) {
 			// TODO: handle exception
 			//GPS not enabled exceptions;;
 		}
 		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.fragmentDirect)).getMap();
 		
 		// Get received destination object
 		Intent receivedIntent = getIntent();
 		Destination receivedDestination = (Destination)receivedIntent.getSerializableExtra("DestinationObject");
 		
 		this.destinationLatLng = new LatLng(receivedDestination.getLatitude(), receivedDestination.getLongitude());
 		
 		this.sourceLatLng = new LatLng(getCurrentLocation().getLatitude(), getCurrentLocation().getLongitude());
 		
 		// Call google api for getting direction between source and direction
 		new GetDirectionTask().execute(sourceLatLng, destinationLatLng);
 				
 		// Get current location either from GPS or Network Provider
 		this.addPlaceMarkerOnMap(sourceLatLng, null);
 				
 		// Destination place marker
 		this.addPlaceMarkerOnMap(destinationLatLng, receivedDestination);
 
 		// Following marker type should be used in case of custom marker
 		//map.addMarker(new MarkerOptions().position(destinationLatLng).title(receivedDestination.getName()).snippet(receivedDestination.getDescription()).icon(BitmapDescriptorFactory.fromResource(R.drawable.placemark)));
 
 		map.moveCamera(CameraUpdateFactory.newLatLngZoom(sourceLatLng, 15));
 		// Provide default zoom level (i.e. how much map area to bring in focus)
		map.animateCamera(CameraUpdateFactory.zoomTo(15), 300, null);
 	}
 	
 	public void onResume()
 	{
 		super.onResume();
 		// start receiving last known location
 		//this.myLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myLocationListener);
 		this.checkAndPromptGPSEnabled();
 		if(isGPSEnabled)
 		{
 			this.sourceLatLng = new LatLng(getCurrentLocation().getLatitude(), getCurrentLocation().getLongitude());
 
 		}
 	}
 	
 	public void onPause()
 	{
 		super.onPause();
 		// Stop GPS tracking when application looses visibility
 		if(isGPSEnabled)
 		{
 			// BUG: Only remove updates when GPS is enabled. Otherwise there is no listener attached at this moment
 			myLocationManager.removeUpdates(myLocationListener);
 		}
 		//myLocationManager.removeUpdates(myLocationListener);
 		isGPSEnabled = false;
 	}
 	
 	public void onDestroy() {
         super.onDestroy();
         if(this.myLocationListener != null && this.myLocationManager != null)
     		myLocationManager.removeUpdates(myLocationListener);
    }
 		
 	/*
 	 * Add placemarks on map. If destination is null then it is assumed that it is the current source location
 	 */
 	private void addPlaceMarkerOnMap(LatLng locationLatLng, Destination destination)
 	{
 		if(map== null)
 		{
 			map = ((MapFragment) getFragmentManager().findFragmentById(R.id.fragmentDirect)).getMap();
 		}
 		
 		String destinationName;
 		String destinationDescription;
 		
 		if(destination == null)
 		{
 			destinationName = "My Location";
 			destinationDescription = "You are here!!";
 		}
 		else
 		{
 			destinationName = destination.getName();
 			destinationDescription = destination.getDescription();
 		}
 		map.addMarker(new MarkerOptions().position(locationLatLng).title(destinationName).snippet(destinationDescription));
 	}
 	
 	public Location getCurrentLocation()
 	{
 		Location locationGPS = this.myLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 		Location locationNet = this.myLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 		
 	    long GPSLocationTime = 0;
 	    if (null != locationGPS) { GPSLocationTime = locationGPS.getTime(); }
 
 	    long NetLocationTime = 0;
 
 	    if (null != locationNet) {
 	        NetLocationTime = locationNet.getTime();
 	    }
 
 	    if ( 0 < GPSLocationTime - NetLocationTime ) {
 	        return locationGPS;
 	    }
 	    return locationNet;
 	}
 	
 	/*
 	 * @see com.csc591.view.MyLocationListener.onMyLocationChangeHandler#onMyLocationChanged(android.location.Location)
 	 * On location change update the 
 	 */
 	@Override
 	public void onMyLocationChanged(Location location) {
 		//this.addPlaceMarkerOnMap(new LatLng(location.getLatitude(), location.getLongitude()), null);
 	}
 
 	
 	public void checkAndPromptGPSEnabled()
 	{
 		this.isGPSEnabled = false;
 		
 		// TWO methods to check GPS is enabled or not. Need to check which one works well
 		/*String provider = Settings.Secure.getString(getContentResolver(),
 			      			Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
 		
 		if(!provider.equals("")){
 			//GPS Enabled
 			this.isGPSEnabled = true;
 		   }
 		*/
 		try
         {
             isGPSEnabled = this.myLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
         }
         catch (Exception ex) {}
 		
 		if(isGPSEnabled)
         {
             this.myLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this.myLocationListener);
         }
         else
         {
             AlertDialog.Builder builder = new AlertDialog.Builder(this);
             builder.setMessage("Your GPS is disabled. Would you like to enable it ?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
 
                              //Send user to GPS settings screen
                              Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                              
                              //dialog.dismiss();
                             // getApplicationContext().startActivity(intent);
                              //startActivityForResult(intent, 1);
                              FragmentDirection.this.startActivity(intent);
                         }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                             dialog.cancel();
                        }
                    });
             AlertDialog alert = builder.create();
             alert.show();
         }
 	}
 	
 	public void onActivityResult()
 	{
 		// TODO - add code here to check when user returns from enable GPS screen to our app
 		// then is GPS enabled or not
 	}
 	
 	// Get data from Google Direction Api and display on existing map
 	private void onBackgroundTaskDataObtained(PolylineOptions mapRectLines)
 	{
 		map.addPolyline(mapRectLines);
 	}
 	
 	// ********************************* Calling Google Map Direction API for route directions ***********
 	
 	public class GetDirectionTask extends AsyncTask<LatLng, Void, Document> {
 		
 		private Exception exception;
 
 		// Please note walking directions are hard coded, in case you need 
 		// driving direction (in future) then change mode to walking
 		protected Document doInBackground(LatLng... latlng) {
 			String url = "http://maps.googleapis.com/maps/api/directions/xml?" 
 	        		+ "origin=" + latlng[0].latitude + "," + latlng[0].longitude  
 	        		+ "&destination=" + latlng[1].latitude + "," + latlng[1].longitude 
 	        		+ "&sensor=false&units=metric&mode=walking";
 			
 	        try {
 	            HttpClient httpClient = new DefaultHttpClient();
 	            HttpContext localContext = new BasicHttpContext();
 	            HttpPost httpPost = new HttpPost(url);
 	            HttpResponse response = httpClient.execute(httpPost, localContext);
 	            InputStream in = response.getEntity().getContent();
 	            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
 	            Document doc = builder.parse(in);
 	            return doc;
 	        } catch (Exception e) {
 	            e.printStackTrace();
 	        }
 			return null;
 			
 		}
 		
 		protected void onPostExecute(Document doc) {
 	        // TODO: check this.exception 
 			parseGoogleMapDirectionDocument(doc);
 	    }
 		
 		/*
 		 * Parse the google XML result and get the required data.
 		 * Also pass the required data to Fragment Direction (using onBackground TaskData Obtained method).
 		 * Then that method will draw route on map
 		 */
 		public void parseGoogleMapDirectionDocument(Document doc)
 		{
 			GMapDirectionHelper gDirection = new GMapDirectionHelper();
 			int duration = gDirection.getDurationValue(doc);
 			String distance = gDirection.getDistanceText(doc);
 			String start_address = gDirection.getStartAddress(doc);
 			String copy_right = gDirection.getCopyRights(doc);
 
 			ArrayList<LatLng> directionPoint = gDirection.getDirection(doc);
 			PolylineOptions rectLine = new PolylineOptions().width(4).color(Color.RED);
 			
 			for(int i = 0 ; i < directionPoint.size() ; i++) {			
 				rectLine.add(directionPoint.get(i));
 			}
 			FragmentDirection.this.onBackgroundTaskDataObtained(rectLine);
 		}
 	}
 
 	
 }
