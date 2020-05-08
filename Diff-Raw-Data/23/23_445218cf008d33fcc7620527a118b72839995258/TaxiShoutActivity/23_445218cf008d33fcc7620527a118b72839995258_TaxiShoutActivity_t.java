 //Throughout the code, the default location has been set to your house
 //To reset this, search for HOMESETTING in this class. Remove and add the appropriate code
 //Also note that the current Location display always displays the current location irrespective of what I try to 
 //force it to show. So, there might not be a blinking Blue dot, if you test from a different location as of now. 
 package com.taxishout.main;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.Toast;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 public class TaxiShoutActivity extends MapActivity implements OnClickListener{
 
 	private MapController mapController;
 	private MapView mapView;
 	private LocationManager locationManager;
 	private MyOverlays itemizedoverlay;
 	private MyLocationOverlay myLocationOverlay;
 	private final Handler myHandler = new Handler();
 	private ProgressDialog Progress;
 	final Runnable updateRunnable = new Runnable() {
         public void run() {
             //call the activity method that updates the UI
         	
         	//HOMESETTING
         	//This section sets the current location to your home coordinates. Hide at final release
         	int Lat = 13039805;
         	int Lng = 77555108;
         	GeoPoint g = new GeoPoint(Lat,Lng);
         	mapView.getController().animateTo(g);
         	//Uncomment the next line 
         	//mapView.getController().animateTo(myLocationOverlay.getMyLocation());
 			setVis();
             
         }
     };
     private void doSomeHardWork()
     {
          //.... hard work
           
          //update the UI using the handler and the runnable
          myHandler.post(updateRunnable);
 
     }
 	private void setVis()
 	{
 		Button now =(Button)findViewById(R.id.now);
 		Button later =(Button)findViewById(R.id.later);
 		now.setVisibility(View.VISIBLE);
 		later.setVisibility(View.VISIBLE);
 		now.setOnClickListener(this);
 	}
 	private String getData(int lat,int lng,float rad,int limit)
 	{
 		StringBuilder builder = new StringBuilder();
 		Float Lat = (float) lat;
 		Float Lng = (float) lng;
 		Lat=(float) (Lat/1000000.0);
 		Lng=(float) (Lng/1000000.0);
 		
 		//HOMESETTING
 		//Remove these lines eventually. These lock the location to your house
 		Lat=(float) 13.039805;
 		Lng=(float) 77.555108; 
 		//End Removing
 		String url="https://api.parse.com/1/classes/taxishout";
 		String query="where=" +
 				"{\"location\":{\"$nearSphere\":{\"__type\":\"GeoPoint\"," +
 				"\"latitude\":"+Lat+"," +
 				"\"longitude\":"+Lng+"}," +
 				"\"$maxDistanceInKilometers\":"+rad+"}}";
 		try {
 			URI uri = new URI(
 			        "http", 
 			        "api.parse.com", 
 			        "/1/classes/taxishout",
 			        query,
 			        null);
 			url = uri.toASCIIString();
 		} catch (URISyntaxException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		HttpClient client = new DefaultHttpClient();
 		HttpGet httpGet = new HttpGet(url);
 		//Adding http headers
 		httpGet.setHeader("X-Parse-Application-Id", "3EN6GtbpYtprWOJyqHNaPjXrJixp66F2qTQVOS30");
 		httpGet.setHeader("X-Parse-REST-API-Key", "KTNYweSLVvp8hI2mR3ekXUCzbJW4fIqay21aQk1O");
 		httpGet.setHeader("Content-Type", "application/json");
 		try {
 			//Similar to xhr in js
 			HttpResponse response = client.execute(httpGet);
 			StatusLine statusLine = response.getStatusLine();
 			int statusCode = statusLine.getStatusCode();
 			if (statusCode == 200) {
 				HttpEntity entity = response.getEntity();
 				InputStream content = entity.getContent();
 				BufferedReader reader = new BufferedReader(new InputStreamReader(content));
 				String line;
 				while ((line = reader.readLine()) != null) {
 					builder.append(line);
 				}
 			} else {
 				return "FAIL";
 				//Returns FAIL on all errors. Else returns the JSON String
 			}
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			return "FAIL";
 		} catch (IOException e) {
 			e.printStackTrace();
 			return "FAIL";
 		}
 		return builder.toString();
 	}
 	public void onCreate(Bundle bundle) {
 		super.onCreate(bundle);
 		setContentView(R.layout.main); // bind the layout to the activity
 
 		// Configure the Map
 		mapView = (MapView) findViewById(R.id.mapview);
 		mapView.setBuiltInZoomControls(false);
 		mapView.setSatellite(true);
 		mapController = mapView.getController();
 		mapController.setZoom(15); // Zoom 15 shows locations close to the user
 		
 		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
 				0, new GeoUpdateHandler());
 		
 		myLocationOverlay = new MyLocationOverlay(this, mapView);
 		mapView.getOverlays().add(myLocationOverlay);
 		//This is the basic overlay showing the compass and current location. Zoom controls hidden; you can pinch to zoom
 
 		myLocationOverlay.runOnFirstFix(new Runnable() {
 			public void run() {
 				
 				//Runnable called when the Location is first obtained. 
 				
 				doSomeHardWork();
 				//I cannot directly change the UI from a Runnable. So, I called a handler, which does the UI changes
 			}
 		});
 		//Setting Listeners
 		Button b = (Button)findViewById(R.id.now);
 		b.setOnClickListener(this);
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		//Method required due to MapActivity. Useless otherwise
 		return false;
 	}
 
 	public class GeoUpdateHandler implements LocationListener {
 
 		@Override
 		public void onLocationChanged(Location location) {
 			@SuppressWarnings("unused")
 			int lat = (int) (location.getLatitude() * 1E6);
 			@SuppressWarnings("unused")
 			int lng = (int) (location.getLongitude() * 1E6);
 			
 			//HOMESETTING
 			//Setting current Location
         	int Lat = 13039805;
         	int Lng = 77555108;
         	GeoPoint point = new GeoPoint(Lat, Lng);
 			//Uncomment next line
 			//GeoPoint point = new GeoPoint(lat, lng);
 			mapController.animateTo(point); // When a location is passed, goes to the point
 			
 		}
 
 		@Override
 		public void onProviderDisabled(String provider) {
 		}
 
 		@Override
 		public void onProviderEnabled(String provider) {
 		}
 
 		@Override
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 		}
 	}
 
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		myLocationOverlay.enableMyLocation();
 		myLocationOverlay.enableCompass();
 	}
 
 	@Override
 	protected void onPause() {
 		super.onResume();
 		myLocationOverlay.disableMyLocation();
 		myLocationOverlay.disableCompass();
 	}
 
 	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 		if(v.getId()==R.id.now)
 		{
 			GeoPoint g = myLocationOverlay.getMyLocation();
 			Progress = new ProgressDialog(v.getContext());
 			Progress.setCancelable(false);
 			Progress.setMessage("Downloading Driver Data");
 			Progress.show();
 			String JSONStr =getData(g.getLatitudeE6(),g.getLongitudeE6(),(float) 2.0,10);
 			Progress.hide();
 			//If there is any Error in getting the data, the function returns FAIL
 			if(JSONStr!="FAIL")
 			{
 			try {
 				JSONObject jsonObject = new JSONObject(JSONStr);
 				Drawable drawable = this.getResources().getDrawable(R.drawable.point);
 				//The drawable is the image that we put on the map
 				itemizedoverlay = new MyOverlays(drawable,this);
 				List<Overlay> mapOverlays = mapView.getOverlays();
 				//Removes all overlays from the map except the main location overlay, which has the compass, current location
 				//when the user presses the get button twice, the old results are erased and new ones displayed
 				while(mapOverlays.size()>2)
 					mapOverlays.remove(1);
 				
 				JSONArray results = jsonObject.getJSONArray("results");
 				for (int i = 0; i < results.length(); i++) {
 					
 					JSONObject result = results.getJSONObject(i);
 					String Details = result.getString("driverDetails");
 					String Name = result.getString("driverName");
 					int Rating = result.getInt("driverRating");
 					boolean isAvailable = result.getBoolean("isAvailable");
 					JSONObject Location = result.getJSONObject("location");
 					int Latitude = (int) (Location.getDouble("latitude")*1000000);
 					int Longitude = (int) (Location.getDouble("longitude")*1000000);
 					GeoPoint point = new GeoPoint(Latitude, Longitude);
 					GeoPoint MyLoc = myLocationOverlay.getMyLocation();
 					//HOMESETTING
 					//comment these two lines, uncomment the other two in future
 					
 					result.put("MYLAT",13039805);
 					result.put("MYLNG",77555108);
 					
 					//result.put("MYLAT",MyLoc.getLatitudeE6());
 					//result.put("MYLNG",MyLoc.getLongitudeE6());
 					String message = "Details:"+Details+
 					"\nName:"+Name + 
 					"\nRating:"+Rating;
 					message = result.toString();
 					if(isAvailable)
 					{
 						OverlayItem overlayitem = new OverlayItem(point,Details,message);
 						itemizedoverlay.addOverlay(overlayitem);
						Log.d("JSON",itemizedoverlay.toString());
 						//Adding Point to the overlay
 							
 					}
 									
 				}
				if(itemizedoverlay.size()==0)
					Toast.makeText(getApplicationContext(), "No driver around you!", Toast.LENGTH_LONG).show();
 				
				else
				{
					mapOverlays.add(itemizedoverlay);
					Toast.makeText(getApplicationContext(), "Read your data!", Toast.LENGTH_LONG).show();
				}
 				
 				//adding overlay
				
 			} catch (Exception e) {
 				e.printStackTrace();
 				Toast.makeText(getApplicationContext(), "Error parsing JSON!", Toast.LENGTH_LONG).show();
 				Log.e("JSONERROR",e.toString());
 			}
 			}
 			else
 			{
 				Toast.makeText(getApplicationContext(), "Could not get Data\nPlease check your connection!", Toast.LENGTH_LONG).show();
 			}
 		}
 	}
 }
