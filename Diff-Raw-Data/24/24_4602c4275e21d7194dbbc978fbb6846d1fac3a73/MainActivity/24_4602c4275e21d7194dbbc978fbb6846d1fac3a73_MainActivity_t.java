 package com.maptime.maptime;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.Parcelable;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.location.Address;
 import android.location.Geocoder;
 import android.location.LocationManager;
 
 /**
  * The MainActivity provides the map view and activity, and as such, the base for 
  * location services and retrieving navigation data
  */
 
 public class MainActivity extends MapActivity {
 	
 	private MapController mapController; //Reference for the MapController
 	private volatile PointsOverlay itemizedOverlay; //Overlay for information pins on the map
 	private LocationOverlay locationOverlay; //Overlay for the users current location
 	private List<Overlay> mapOverlays; //List of map overlays for android. index 0 is itemizedOverlay, 1 is locationOvery and 2 is routeOverlay
 	private GeoPoint point, point2; //The two points tapped for the purposes of creating a route
 	private Timeline curTimeline;  //The current timeline loaded
 	public volatile MapView mapView; //Reference for the MapView
 	private NavOverlay routeOverlay; //Overlay for the navigation route
 	public LocationManager lMan; //LocationManager
 	public LocationUpdater locUpGPS; //Location Listener for the GPS
 	public LocationUpdater locUpNetwork; //Location Listener for location via networks
 	public boolean gps = false; // Is there an enabled GPS location system
 	public boolean network = false; // Is there an enabled system for location via networks
 	public boolean networkLoss = false; //Is there currently an internet connection
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
 		
         setContentView(R.layout.activity_main);
         mapView = (MapView) findViewById(R.id.mapview);
         mapView.setBuiltInZoomControls(true);
         mapOverlays = mapView.getOverlays();
 		Drawable pinDrawable = this.getResources().getDrawable(R.drawable.map_marker);
 		Drawable locationIcon = this.getResources().getDrawable(R.drawable.currentlocation_icon);
 		lMan = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
 		locUpGPS = new LocationUpdater();
 		locUpNetwork = new LocationUpdater();
  		if (lMan.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
  			lMan.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2500, (float) 30.0, locUpNetwork);
  			network = true;
  		}
  		if (lMan.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
  			if (network) {
  				lMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100000, (float) 500.0, locUpGPS);
  			}
  			else {
  				lMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, (float) 50.0, locUpGPS);
  			}
  			gps = true;
  		}
 		if (savedInstanceState != null && savedInstanceState.containsKey("pointsOverlayList")) { //If we have a savedInstanceState and it has
 			ArrayList<ParcelableOverlayItem> listOIs = new ArrayList<ParcelableOverlayItem>(); //info on the PointsOverlay then get it. Repeat
 			for (Parcelable p: savedInstanceState.getParcelableArrayList("pointsOverlayList")) { //for everything else in the savedInstanceState
 				
 				listOIs.add((ParcelableOverlayItem)p);
 			}
 			itemizedOverlay = new PointsOverlay(listOIs,
 					((ParcelableGeoPoint)savedInstanceState.getParcelable("pointsOverlayStart")),
 					((ParcelableGeoPoint)savedInstanceState.getParcelable("pointsOverlayEnd")), pinDrawable, this);
 		}
 		else {
 			
 			itemizedOverlay = new PointsOverlay(pinDrawable, this);
 		}
 		mapOverlays.add(itemizedOverlay);
 		locationOverlay = new LocationOverlay(locationIcon, this);
 		mapOverlays.add(locationOverlay);
 		if (savedInstanceState != null && savedInstanceState.containsKey("navOverlayList")) {
 			
 			ArrayList<ParcelableGeoPoint> listGPs = new ArrayList<ParcelableGeoPoint>();
 			for (Parcelable p: savedInstanceState.getParcelableArrayList("navOverlayList")) {
 				
 				listGPs.add((ParcelableGeoPoint)p);
 			}
 			routeOverlay = new NavOverlay(listGPs, 
 					savedInstanceState.getDouble("navOverlayLength"), this);
 			mapOverlays.add(routeOverlay);
 		}
 		if (savedInstanceState != null && savedInstanceState.containsKey("timeline")) {
 			curTimeline = savedInstanceState.getParcelable("timeline");
 		}
 		if(itemizedOverlay != null && routeOverlay != null && curTimeline != null) {
 			timeToPlace();
 		}
 		//If Plot Route was pressed get the GeopPoints of the given addresses and plot route
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) {
 			
 			String[] addresses = extras.getStringArray("EXTRA_ADDRESSES");
 			curTimeline = (Timeline)extras.get("EXTRA_TIMELINE");
 			String startAddress = addresses[0];
 			String endAddress = addresses[1];
 			point = reverseGeocoding(startAddress);
 			point2 = reverseGeocoding(endAddress);
 			
 			if(point != null && point2 != null) {
 				
 				Thread nst = new Thread(new NavStartThread(this));
 				nst.start();
 			}
 		}		
     }    
     
 	/**
 	 * Stops the GPS service.
 	 * Called whenever the activity is stopped.	 
 	 */
 	
 	public void stopLocation() {
 		if (lMan != null && itemizedOverlay.geoFence != null) {
 			
 			itemizedOverlay.stopGPS();
 		}
 		if (lMan != null && locationOverlay.locationThread != null) {
 			
 			locationOverlay.stopGPS();
 		}
 		if (lMan != null) {
 			
 			lMan.removeUpdates(locUpGPS);
 			lMan.removeUpdates(locUpNetwork);
 			locUpGPS = null;
 			locUpNetwork = null;
 			lMan = null;
 
 		}
 	}
 	
 	/**
 	 * When the activity has both a navigation route and a timeline, 
 	 * put the TimePoints on the route
 	 */
 	
 	private void timeToPlace() {
 		itemizedOverlay.clearTimePoints(); //clear existing timepoints
 		ArrayList<GeoPoint> gp = routeOverlay.getNavGPs();
 		double dist = routeOverlay.getLength();
 		int timelineSize = curTimeline.size();
 		double endTimeValue = curTimeline.getPoint(0).getTimeInBC();
 		double timeRange = curTimeline.getPoint(timelineSize-1).getTimeInBC() - endTimeValue;
 		double[] relativePos = new double[timelineSize];
 		for (int i = 0; i < timelineSize; i++) { //for each TimePoint, get the distancle along the route they should go
 			
 			relativePos[i] = dist * ((timeRange - (curTimeline.getPoint((timelineSize-1)-i).getTimeInBC() - endTimeValue)) / timeRange);
 		}
 		int curCheck = 0;
 		double lengthSoFar = 0.0;
 		for(int i = 0; i < gp.size()-1; i++){ //for each pair of GeoPoints in the route
 			
 			double length = distanceKm((double)(gp.get(i).getLatitudeE6())/(double)1000000.0,
 					(double)(gp.get(i).getLongitudeE6())/(double)1000000.0,
 					(double)(gp.get(i+1).getLatitudeE6())/(double)1000000.0,
 					(double)(gp.get(i+1).getLongitudeE6())/(double)1000000.0);
 			while(curCheck < relativePos.length && relativePos[curCheck] < length + lengthSoFar) {
 				
 				double fraction = (relativePos[curCheck]-lengthSoFar) / length;
 				int lat1 = gp.get(i).getLatitudeE6();
 				int lon1 = gp.get(i).getLongitudeE6();
 				int lat2 = gp.get(i+1).getLatitudeE6();
 				int lon2 = gp.get(i+1).getLongitudeE6();
 				int diffLat = lat2 - lat1;
 				int diffLon = lon2 - lon1;
 				double addLat = fraction * (double) diffLat;
 				double addLon = fraction * (double) diffLon;
 				itemizedOverlay.addOverlay(new OverlayItem(new GeoPoint(lat1+(int)addLat,lon1+(int)addLon), curTimeline.getPoint((curTimeline.size()-1)-curCheck).getName(), curTimeline.getPoint((curTimeline.size()-1)-curCheck).getDescription()));
 				//get microdegrees between geopoints, multiply by fraction, then add that onto the first geopoint, and add that point to our arraylist
 				curCheck++;
 			}
 			lengthSoFar += length;
 		}
 		itemizedOverlay.addOverlay(new OverlayItem(gp.get(0), curTimeline.getPoint(0).getName(), curTimeline.getPoint(0).getDescription()));
 		mapView.postInvalidate();
 		/*work out how far down route each TimePoint should be, normalised to dist, then
 		 *work out how far each geopoint is using distanceKm(), and if TimePoints should go 
 		 *between the two geopoints, and if so, for each timepoint that should go, work out how far between, and...
 		 *Need to confirm that YOURS does in fact use KM for length() though
 		*/
 	}
 	
 	/**
 	 * Get the long and lat points of an address
 	 */
 	public GeoPoint reverseGeocoding(String addressString) {
 		Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
 		GeoPoint gp = null;
 		try {
 			List<Address> addresses = geoCoder.getFromLocationName(addressString, 1);
 			for(Address address : addresses){
 	            gp = new GeoPoint((int)(address.getLatitude() * 1E6), (int)(address.getLongitude() * 1E6));
 	        }
 		    return (gp);
 		} catch (IOException e) {
 			
 			e.getStackTrace();
 			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
 			dialog.setTitle("Error");
 			dialog.setMessage("Cannot get location points of address");
 			dialog.setNegativeButton("OK", new DismissListener());
 			dialog.show();
 			return null;
 		}
 	}
 	
 	/**
 	 * Works out distance between two points using the Haversine formula
 	 * @param lat1 Latitude of the first point
 	 * @param lon1 Longitude of the first point
 	 * @param lat2 Latitude of the second point
 	 * @param lon2 Longitude of the second point
 	 * @return The distance between the two points
 	 */
 	
 	public static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
 	    int EARTH_RADIUS_KM = 6371;
 	    double lat1Rad = Math.toRadians(lat1);
 	    double lat2Rad = Math.toRadians(lat2);
 	    double deltaLonRad = Math.toRadians(lon2 - lon1);
 
 	    return Math.acos(Math.sin(lat1Rad) * Math.sin(lat2Rad) + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.cos(deltaLonRad)) * EARTH_RADIUS_KM;
 	}
 	
 	/**
 	 * Check's if there's a network connection available
 	 * @return If there is a network connection available
 	 */
 	
 	private boolean isNetworkAvailable() {
 	    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
 	    return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
 	}
 	
 	/**
 	 * Makes GPS update more often if internet access is removed, 
 	 * returns to normal when internet access resumes.
 	 */
 	
 	public void changeGPS() {
 		if (network && gps) {
 			if (!networkLoss && !isNetworkAvailable()) {
 				networkLoss = true;
 				lMan.removeUpdates(locUpGPS); //unregister current network and gps stuff
 				lMan.removeUpdates(locUpNetwork);
 				lMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, (float) 50.0, locUpGPS); //register new gps stuff
 			}
 			else if (networkLoss && isNetworkAvailable()) {
 				networkLoss = false;
 				lMan.removeUpdates(locUpGPS);//unregister new gps stuff
 				lMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100000, (float) 500.0, locUpGPS);
 				lMan.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2500, (float) 30.0, locUpGPS);//restore old stuff
 			}
 		}
 	}
 	
 	/**
 	 * Stores the timeline object from TimelineChoice
 	 */
 	
 	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		if (requestCode == 0 && resultCode == RESULT_OK) {
 			curTimeline = data.getParcelableExtra("selectedTimeline");
 			if (mapOverlays.size() == 3) {
 				timeToPlace();
 			}
 		}
 	}
 	
 	public boolean onCreateOptionsMenu(Menu menu){        
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.activity_main, menu);
 	    return true;
 	}
 	
 	public boolean onOptionsItemSelected(MenuItem item){
 	    switch(item.getItemId()){
 	    case R.id.menu_timelines:
 	        Intent intent = new Intent(this,Timelinechoice.class);
 	        startActivityForResult(intent, 0);
 	        return true;
 	    case R.id.menu_startnav:
 	    	AlertDialog.Builder dialog = new AlertDialog.Builder(this);
 			dialog.setTitle("Navigation Mode");
 			dialog.setMessage("Tap where you want to start your timeline");
 			dialog.setPositiveButton("OK", new DismissListener());
 			dialog.show();
 			point = null;
 			point2 = null;
 	    	Thread nst = new Thread(new NavStartThread(this));
 			nst.start();
 	    	return true;
 	    case R.id.menu_home:
	    	/*Intent intentHome = new Intent(this, Home.class);
	    	startActivity(intentHome);*/
	    	finish();
 	    	return true;
 	    }
 	    return false;
 	}
 	
 	protected void onSaveInstanceState(Bundle outState) {
 		// TODO Auto-generated method stub
 		super.onSaveInstanceState(outState);
 		ArrayList<OverlayItem> loi = itemizedOverlay.getMOverLays();
 		ArrayList<ParcelableOverlayItem> ploi = new ArrayList<ParcelableOverlayItem>();
 		for (OverlayItem oi : loi) {
 			ploi.add(new ParcelableOverlayItem(oi));
 		}
 		outState.putParcelableArrayList("pointsOverlayList", ploi);
 		if(itemizedOverlay.getStartPoint() != null) {
 			outState.putParcelable("pointsOverlayStart",new ParcelableGeoPoint(itemizedOverlay.getStartPoint()));
 		}
 		if(itemizedOverlay.getEndPoint() != null) {
 			outState.putParcelable("pointsOverlayEnd",new ParcelableGeoPoint(itemizedOverlay.getEndPoint()));
 		}
 		if (mapOverlays.size() > 2 && ((NavOverlay)routeOverlay).isCreated()) { //NOTE! the second check is basically a race condition!)
 			ArrayList<GeoPoint> lgp = ((NavOverlay)routeOverlay).getNavGPs();
 			ArrayList<ParcelableGeoPoint> plgp = new ArrayList<ParcelableGeoPoint>();
 			for (GeoPoint gp: lgp) {
 				plgp.add(new ParcelableGeoPoint(gp));
 			}
 			outState.putParcelableArrayList("navOverlayList", plgp);
 			outState.putDouble("navOverlayLength", ((NavOverlay)routeOverlay).getLength());
 		}
 		if (curTimeline != null) {
 			outState.putParcelable("timeline", curTimeline);
 		}
 		stopLocation();
 	}
 	
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 	
 	
 	@Override
 	protected void onRestart() { //Bring Location Services back up if necessary
 		// TODO Auto-generated method stub
 		super.onRestart();
 		if (lMan == null) {
 			lMan = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
 			locUpGPS = new LocationUpdater();
 			locUpNetwork = new LocationUpdater();
 			lMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100000, (float) 500.0, locUpGPS);
 			lMan.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, (float) 50.0, locUpNetwork);
 		}
 	}
 	
 	@Override
 	protected void onStop() {
 		
 		super.onStop();
 		stopLocation();
 	}
 	
 	/**
 	 * Runnable that retrieves navigation data from YOURS, based on user input, 
 	 * and creates an overlay showing that route, along with the Timeline if the
 	 * Timeline has already been selected.
 	 */
 	
 	private class NavStartThread implements Runnable {
 		public MainActivity ma;
 		public ProgressDialog progressDialog;
 		boolean broken = false;
 		
 		/**
 		 * General constructor
 		 * @param m The MainActivity that this thread was spawned from
 		 */
 		
 		public NavStartThread(MainActivity m) {
 			ma = m;
 		}
 		
 		
 		public void run() {
 			itemizedOverlay.clearPoints();
 	    	itemizedOverlay.setNavMode(true);
 			while (itemizedOverlay.getEndPoint() == null) { //while there isn't an endpoint, check 
 		
 
 				if(point != null && point2 != null) {
 					break;
 				}
 				try {
 					Thread.sleep(2000);
 					System.out.println("Bananas!");
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					//e.printStackTrace();
 					broken = true;
 					break;
 				}
 			}
 			
 			itemizedOverlay.setNavMode(false);
 			if(!broken){	
 				waitHandler.sendEmptyMessage(0); //Start the pop-up progress bar
 				if(point == null || point2 == null) {
 					point = itemizedOverlay.getStartPoint();
 					point2 = itemizedOverlay.getEndPoint();	
 				}
 				itemizedOverlay.setStartPointOverlay(new OverlayItem(point, "Start", "Start of TimeLine"));
 				itemizedOverlay.setEndPointOverlay(new OverlayItem(point2, "End", "End of TimeLine"));
 				//TODO: An asynctask which does the following since we can't network on main thread
 				if (mapOverlays.size() == 2) {
 					try {
 						routeOverlay = new NavOverlay(point, point2, ma);
 						mapOverlays.add(routeOverlay);
 					} catch (NoRouteException e) {
 						itemizedOverlay.clearMOverLays();
 						ma.runOnUiThread(new Runnable() {
 							public void run() {
 								AlertDialog.Builder  dialog = new AlertDialog.Builder(ma);
 								dialog.setTitle("Could not find route!");
 								dialog.setMessage("Try giving more information, or make sure there is a road connection.");
 								dialog.setNegativeButton("OK", new DismissListener());
 								dialog.show();
 							}
 						});
 					}
 				}
 				else if (routeOverlay != null) {
 					try {
 						routeOverlay = new NavOverlay(point, point2, ma);
 						mapOverlays.set(2, routeOverlay);
 					} catch (NoRouteException e) {
 						itemizedOverlay.clearMOverLays();
 						ma.runOnUiThread(new Runnable() {
 							public void run() {
 								AlertDialog.Builder  dialog = new AlertDialog.Builder(ma);
 								dialog.setTitle("Could not find route!");
 								dialog.setMessage("Try giving more information, or make sure there is a road connection.");
 								dialog.setNegativeButton("OK", new DismissListener());
 								dialog.show();
 							}
 						});
 						/*routeOverlay = null;
 						if (mapOverlays.size() == 3) {
 							mapOverlays.remove(2);
 						}*/
 					}
 				}
 				if(curTimeline != null && routeOverlay != null) {
 					timeToPlace();
 				}
 				else {
 					mapView.postInvalidate();
 				}
 			
 				waitHandler.sendEmptyMessage(1); //Dismiss the pop-up progress bar
 
 			
 			//Log.i("MAP_OVERLAYS", Integer.toString(mapOverlays.size()));
 			}
 		}
 		
 		/**
 		 * Handler to display a progress pop-up while the route is being calculated
 		 */
 		
 		private volatile Handler waitHandler = new Handler() {
             public void handleMessage(Message msg) {
             	if(msg.what == 0) {
             		String progressTitle = getString(R.string.progress_calculating);
         			String progressMessage = getString(R.string.progress_calculatingRoute);
             		progressDialog = ProgressDialog.show(ma, progressTitle, progressMessage);
             	}
             	if(msg.what == 1) {
             		progressDialog.dismiss();
             	}
             }
 		};
 		
 	}
 }
