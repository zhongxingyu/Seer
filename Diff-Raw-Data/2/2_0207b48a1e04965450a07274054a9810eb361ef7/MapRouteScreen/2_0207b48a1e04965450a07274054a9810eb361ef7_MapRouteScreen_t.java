 package edu.upenn.cis542;
 
 import java.io.InputStream;
 import java.util.List;
 
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.View;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 import edu.upenn.cis542.route.*;
 import edu.upenn.cis542.utilities.DeviceConnector;
 
 public class MapRouteScreen extends MapActivity {
 
         private MapView mapView;
         private MapController mapController;
         private Road mRoad;
         private PlacesList mList;
         private Drawable red_marker;
         private Drawable blue_marker;
         private Drawable green_marker;
         private Drawable i_marker;
         
         private String roadInfoToC; // msg will be sent to C program
         
         /*Params that need to be passed from main program*/
         private double fromLat, fromLon, toLat, toLon;
         private RoadProvider.Mode mode;
         private String i_type;
         Road pastRoad; // contains at least the current position
 
         // params used to query pastRoad
         private double queriedFromLat, queriedFromLon, queriedToLat, queriedToLon;
         private RoadProvider.Mode queriedMode;
         private Road queriedPastRoad;
         
         // Define a listener that responds to location updates
         LocationListener locationListener = new LocationListener() {
             // Called when a new location is found by the location provider.
             public void onLocationChanged(Location location) {
                 Log.d("MapRoute, locationListener", "onLocationChanged");
                 Log.d("MapRoute, location.getLongitude()", Double.toString(location.getLongitude()));
                 Log.d("MapRoute, location.getLatitude()", Double.toString(location.getLatitude()));
                 
                 // update pastRoad if it's a new location
                 if ((location.getLongitude() != pastRoad.mPoints[pastRoad.mPoints.length - 1].mLongitude) ||
                      (location.getLatitude() != pastRoad.mPoints[pastRoad.mPoints.length - 1].mLatitude)) {
                     Log.d("MapRoute, location", "NEW location");
                     Log.d("MapRoute, OLD pastRoad.mPoints.length", Integer.toString(pastRoad.mPoints.length));
                     
                     pastRoad.mEndTime = System.currentTimeMillis();
                     
                     queriedFromLon = pastRoad.mPoints[pastRoad.mPoints.length - 1].mLongitude;
                     queriedFromLat = pastRoad.mPoints[pastRoad.mPoints.length - 1].mLatitude;
                     queriedToLon = location.getLongitude();
                     queriedToLat = location.getLatitude();
                     queriedMode = RoadProvider.Mode.WALKING; // TODO: change to default?
                     Log.d("MapRoute, queriedFromLon", Double.toString(queriedFromLon));
                     Log.d("MapRoute, queriedFromLat", Double.toString(queriedFromLat));
                     Log.d("MapRoute, queriedToLon", Double.toString(queriedToLon));
                     Log.d("MapRoute, queriedToLat", Double.toString(queriedToLat));
                     if (queriedMode == RoadProvider.Mode.WALKING) {
                         Log.d("MapRoute, queriedMode", "WALKING");
                     } else if (queriedMode == RoadProvider.Mode.BICYCLING) {
                         Log.d("MapRoute, queriedMode", "BICYCLING");
                     } else if (queriedMode == RoadProvider.Mode.DRIVING) {
                         Log.d("MapRoute, queriedMode", "DRIVING");
                     }
 
                     Thread rThread = new Thread() {
                             @Override
                             public void run() {
                                     String url = RoadProvider.getUrl(queriedFromLat, queriedFromLon, queriedToLat, queriedToLon, queriedMode);
                                     InputStream is = RoadProvider.getConnection(url);
                                     queriedPastRoad = RoadProvider.getRoute(is);
                             }
                     };
                     rThread.start();
                     try {
                         rThread.join();
                     } catch (InterruptedException e) {
                         e.printStackTrace();
                     }
                     
                     Log.d("MapRoute, queriedPastRoad.mPoints.length", Integer.toString(queriedPastRoad.mPoints.length));
                     edu.upenn.cis542.route.Point[] newPoints = new edu.upenn.cis542.route.Point[pastRoad.mPoints.length + queriedPastRoad.mPoints.length + 1];
                     // copy old points
                     for (int i = 0; i < pastRoad.mPoints.length; i++) {
                         newPoints[i] = pastRoad.mPoints[i];
                     }
                     // add queried points on the road, not included from and to points
                     for (int j = 0; j < queriedPastRoad.mPoints.length; j++) {
                         newPoints[pastRoad.mPoints.length + j] = queriedPastRoad.mPoints[j];
                         Log.d("MapRoute, queriedPastRoad.mPoints", j + ": " + queriedPastRoad.mPoints[j].mDescription);
                         Log.d("MapRoute, queriedPastRoad.mPoints", j + ": " + queriedPastRoad.mPoints[j].mLongitude + " " + queriedPastRoad.mPoints[j].mLatitude);
                     }
                     // add current(to) location
                     newPoints[pastRoad.mPoints.length + queriedPastRoad.mPoints.length] = new edu.upenn.cis542.route.Point();
                     newPoints[pastRoad.mPoints.length + queriedPastRoad.mPoints.length].mLongitude = location.getLongitude();
                     newPoints[pastRoad.mPoints.length + queriedPastRoad.mPoints.length].mLatitude = location.getLatitude();
                     
                     pastRoad.mPoints = newPoints;
                     Log.d("MapRoute, NEW pastRoad.mPoints.length", Integer.toString(pastRoad.mPoints.length));
                     
                 
                     //Setup params 
                     fromLon = pastRoad.mPoints[pastRoad.mPoints.length - 1].mLongitude;
                     fromLat = pastRoad.mPoints[pastRoad.mPoints.length - 1].mLatitude;
                     toLon = getIntent().getDoubleExtra("toLon", 0.0);
                     toLat = getIntent().getDoubleExtra("toLat", 0.0);
                     mode = (edu.upenn.cis542.route.RoadProvider.Mode) getIntent().getExtras().get("mode");
                     i_type = getIntent().getStringExtra("i_type");
                     QueryAndDraw();
                     
                     
                     // TODO: send updated message to C
                     // Send Message to Device
                     //Thread sThread = new Thread(new SendThread());
                     //sThread.start();
                 } else {
                     Log.d("location", "OLD location");
                 }
             }
     
             public void onStatusChanged(String provider, int status, Bundle extras) {
                 Log.d("MapRoute, locationListener", "onStatusChanged");
             }
     
             public void onProviderEnabled(String provider) {
                 Log.d("MapRoute, locationListener", "onProviderEnabled");
             }
     
             public void onProviderDisabled(String provider) {
                 Log.d("MapRoute, locationListener", "onProviderDisabled");
             }
         };
         
         
         // readRemoteGPS related values
         private Handler readRemoteGPSHandler = new Handler();
         private static final int UPDATE_INTERVAL = 3000;
         private boolean whetherUpdate = true; // for testing, whether the coordinates are updated periodically or not
         private Runnable readRemoteGPSTask = new Runnable() {
             public void run() {
                 try {
                     Log.d("MapRoute, readRemoteGPSTask", "Connection Start");
                     
                     DeviceConnector c = new DeviceConnector();
                     c.readData();
                     
                     Log.d("MapRoute, readRemoteGPSTask", "Connection Done");
                     
                     double new_toLon = c.getLongitude();
                     double new_toLat = c.getLatitude();
                     
                     Log.d("MapRoute, new_toLon", Double.toString(new_toLon));
                     Log.d("MapRoute, new_toLat", Double.toString(new_toLat));
                     
                     if ((new_toLat == 0) && (new_toLon == 0)) {
                         Toast.makeText(getApplicationContext(), "Can not get updated destination location, using last known location", Toast.LENGTH_SHORT).show();
                         Log.d("MapRoute, readRemoteGPSTask", "Invalid destination");
                     } else if ((new_toLon == toLon) && (new_toLat == toLat)) {
                         Log.d("MapRoute, readRemoteGPSTask", "Unchanged destination");
                     } else {
                         Log.d("MapRoute, readRemoteGPSTask", "Valid destination");
                         // new destination location is valid 
                         // update toLon & toLat
                         // these will be passed back to GPSInfoScreen when this close
                       
                         //Setup params 
                         toLon = new_toLon;
                         toLat = new_toLat;
                         QueryAndDraw();
                         
                         // TODO: send updated message to C
                         // Send Message to Device
                         //Thread sThread = new Thread(new SendThread());
                         //sThread.start();
                     }
                     
                     Log.d("MapRoute, readRemoteGPSTask", "Finished");
                     
                     if (whetherUpdate) {
                         readRemoteGPSHandler.postDelayed(readRemoteGPSTask, UPDATE_INTERVAL);
                     }
                 } catch (Exception e) {
                     Log.e("MapRoute, readRemoteGPSTask", "Exception");
                     
                     Toast.makeText(getApplicationContext(), "Can not get updated destination location, using last known location", Toast.LENGTH_SHORT).show();
                     
                     if (whetherUpdate) {
                         // wait longer time to update if got exception
                         readRemoteGPSHandler.postDelayed(readRemoteGPSTask, UPDATE_INTERVAL * 4);
                     }
                 }
             }
          };
          
         
         @Override
         public void onCreate(Bundle savedInstanceState) {
                 super.onCreate(savedInstanceState);
                 setContentView(R.layout.map_route);
                 mapView = (MapView) findViewById(R.id.mapview);
                 red_marker = getResources().getDrawable(R.drawable.marker_a);
                 blue_marker = getResources().getDrawable(R.drawable.marker_c);
                 green_marker = getResources().getDrawable(R.drawable.marker_b);
                 i_marker = getResources().getDrawable(R.drawable.heart);
                 mapView.setBuiltInZoomControls(true);
                
                 mapView.setSatellite(false);
                 mapController = mapView.getController();
                 mapController.setZoom(13);
 
                 
                 // get params from GPSInfoScreen
                 // get pastRoad, contains at least the current position coordinates, mStartTime, mEndTime
                 pastRoad = (edu.upenn.cis542.route.Road) getIntent().getExtras().get("pastRoad");
                 //fromLon = getIntent().getDoubleExtra("fromLon", 0.0);
                 //fromLat = getIntent().getDoubleExtra("fromLat", 0.0);
                 fromLon = pastRoad.mPoints[pastRoad.mPoints.length - 1].mLongitude;
                 fromLat = pastRoad.mPoints[pastRoad.mPoints.length - 1].mLatitude;
                 toLon = getIntent().getDoubleExtra("toLon", 0.0);
                 toLat = getIntent().getDoubleExtra("toLat", 0.0);
                 mode = (edu.upenn.cis542.route.RoadProvider.Mode) getIntent().getExtras().get("mode");
                 i_type = getIntent().getStringExtra("i_type");
                
                 Log.d("MapRoute, On Create, pastRoad.mPoints.length", Integer.toString(pastRoad.mPoints.length));
                 for (int i = 0; i < pastRoad.mPoints.length; i++) {
                     Log.d("MapRoute, On Create, pastRoad.mPoints", i + ": " + pastRoad.mPoints[i].mLongitude + " " + pastRoad.mPoints[i].mLatitude);
                 }
                 
                 QueryAndDraw();      		
         		// Send Message to Device
         	    Thread sThread = new Thread(new SendThread());
                 sThread.start();
         
         		
         		// Get LocationManager
                 LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                 // Register listener with Location Manager to receive updates
                 locationManager.requestLocationUpdates(
                              LocationManager.GPS_PROVIDER, 
                                 1000, // time interval
                                 0, // distance interval
                                 locationListener);
                 Log.d("MapRouteScreen", "Register locationListener");
                 
                 // start readRemoteGPSTask
                 readRemoteGPSHandler.postDelayed(readRemoteGPSTask, UPDATE_INTERVAL);
                 Log.d("MapRouteScreen", "Start readRemoteGPSTask");
         }
 
         protected void QueryAndDraw() {
         	 Log.d("MapRoute, fromLon", Double.toString(fromLon));
              Log.d("MapRoute, fromLat", Double.toString(fromLat));
              Log.d("MapRoute, toLon", Double.toString(toLon));
              Log.d("MapRoute, toLat", Double.toString(toLat));
              if (mode == RoadProvider.Mode.WALKING) {
                  Log.d("MapRoute, mode", "WALKING");
              } else if (mode == RoadProvider.Mode.BICYCLING) {
                  Log.d("MapRoute, mode", "BICYCLING");
              } else if (mode == RoadProvider.Mode.DRIVING) {
                  Log.d("MapRoute, mode", "DRIVING");
              }
              Log.d("MapRoute, i_type", i_type);
 
              Thread rThread = new Thread() {
                      @Override
                      public void run() {
                              String url = RoadProvider.getUrl(fromLat, fromLon, toLat, toLon,mode);
                              InputStream is = RoadProvider.getConnection(url);
                              mRoad = RoadProvider.getRoute(is);
                              mHandler.sendEmptyMessage(0);
                      }
              };
              rThread.start();
              try {
                  rThread.join();
              } catch (InterruptedException e) {
                  e.printStackTrace();
              }
              
              SearchPlaces search = new SearchPlaces();
              mList = search.getNearByPlaces(fromLat, fromLon, i_type);
      		
      		/*trim the result, remove the status tag*/
      		if(mList.results.size()> 0 && mList.results.get(0).toString().contains("OK"))
      		{
      			mList.results.remove(0);
      		}
      		Log.v("SearchPlaces", String.valueOf(mList.results.size()));
      		for(int i = 0; i < mList.results.size(); i++)
      		{
      			Log.v("SearchPlaces in Map", mList.results.get(i).toString());
      		}
      		
      		// parse mRoad.mDescription to get roadInfoToC
     		String[] infos = null;
     		if(mRoad.mDescription!=null)
     		{
     			infos = mRoad.mDescription.split("[ )]");
     			if (infos.length > 4) {
         		    // Known description format: "Distance: 1.0mi (about 19 mins)"
         		    roadInfoToC = infos[1] + "," + infos[3] + " " + infos[4];
         		} else {
         		    // Unknown description format
         		    roadInfoToC = mRoad.mDescription;
         		}
     			Log.d("mRoad.mDescription", mRoad.mDescription);
         		Log.d("roadInfoToC", roadInfoToC);
     		}
 			
 		}
 
 		// this handle change the description and mapview widgets
         Handler mHandler = new Handler() {
                 public void handleMessage(android.os.Message msg) {
                 		/*Set text info*/
                         TextView textView = (TextView) findViewById(R.id.description);
                         textView.setText(mRoad.mName + ", " + mRoad.mDescription);
                         /*draw suggested route info*/
                        MapOverlay mapOverlay = new MapOverlay(mRoad,mapView, blue_marker, green_marker, fromLat, fromLon, toLat,toLon, true);
                         List<Overlay> listOfOverlays = mapView.getOverlays();
                         listOfOverlays.clear();
                         Log.v("MapRoute OnCreate", "suggestedRoad size: "+ mRoad.mPoints.length);
                         listOfOverlays.add(mapOverlay);
                         /*draw past route info*/
                         Log.v("MapRoute OnCreate", "pastRoad size: "+ pastRoad.mPoints.length);
                         if(pastRoad.mPoints.length > 1)
                         {
                         	Log.v("MapRoute OnCreate", "pastRoad size: "+ pastRoad.mPoints.length);
                         	MapOverlay pastRoadOverlay = new MapOverlay(pastRoad,mapView, red_marker, blue_marker, 
                         		pastRoad.mPoints[0].mLatitude,pastRoad.mPoints[0].mLongitude,
                         		pastRoad.mPoints[pastRoad.mPoints.length-1].mLatitude,pastRoad.mPoints[pastRoad.mPoints.length-1].mLongitude,false);
                         	listOfOverlays.add(pastRoadOverlay);
                         }
                         /*draw points of interests*/
                         drawPointsofInterest();
                         /*invalidate map*/
                         mapView.invalidate();
                 };
         };
 
         @Override
         protected boolean isRouteDisplayed() {
                 return false;
         }
         
         
         protected void drawPointsofInterest() {
 			PointsOverlay pointsPos = new PointsOverlay(i_marker, mapView);
 		    List<Overlay> overlays = mapView.getOverlays();
 		    GeoPoint currentPoint = new GeoPoint((int)(fromLat * 1E6), (int)(fromLon * 1E6));
 		    pointsPos.setCurrentLocation(currentPoint);
 		    
 		    for(int i = 0; i < mList.results.size(); i++)
 	        {
 	        	 GeoPoint i_p = new GeoPoint( (int) (mList.results.get(i).latitude * 1E6), (int) (mList.results.get(i).longtitude * 1E6));
 	        	 OverlayItem overlayItem = new OverlayItem(i_p,mList.results.get(i).name, mList.results.get(i).vicinity);
 	        	 pointsPos.addOverlay(overlayItem);
 	         }
 		    overlays.add(pointsPos); 
 		}
 
 
 		public void onBackToGPSInfoButtonClick(View view) {
             // Get LocationManager
             LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
             // Remove the location updates listener
             locationManager.removeUpdates(locationListener);
             Log.d("MapRouteScreen", "Remove locationListener");
             
             // stop readRemoteGPSTask
             readRemoteGPSHandler.removeCallbacks(readRemoteGPSTask);
             Log.d("MapRouteScreen", "Stop readRemoteGPSTask");
             
             // create the Intent object to send BACK to the caller
             Intent i = new Intent();
             Log.d("MapRouteScreen, toLon", Double.toString(toLon));
             Log.d("MapRouteScreen, toLat", Double.toString(toLat));
             Log.d("MapRouteScreen, pastRoad.mPoints.length", Integer.toString(pastRoad.mPoints.length));
             i.putExtra("toLon", toLon);
             i.putExtra("toLat", toLat);
             // put the pastRoad object into the Intent
             i.putExtra("pastRoad", pastRoad);
             setResult(RESULT_OK, i);
             
             finish();
         }
         
         
         public class SendThread implements Runnable {
             public void run() {
                 try {
                     Log.d("SendThread", "Connecting");
                     
                     DeviceConnector c = new DeviceConnector();
                     c.sendMessage(roadInfoToC);
 
                     Log.d("SendThread", "Finished");
                 } catch (Exception e) {
                     Log.e("SendThread", "Exception");
                 }
             }
         }
 }
 
 
