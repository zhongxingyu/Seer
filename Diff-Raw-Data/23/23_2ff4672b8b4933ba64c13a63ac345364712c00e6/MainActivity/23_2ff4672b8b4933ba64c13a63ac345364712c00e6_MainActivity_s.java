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
 
 public class MainActivity extends MapActivity {
 	
 	private MapController mapController;
 	private PointsOverlay itemizedOverlay;
 	private LocationOverlay locationOverlay;
 	private List<Overlay> mapOverlays;
 	private GeoPoint point, point2;
 	private ArrayList<OverlayItem> timePoints;
 	private Timeline curTimeline; 
 	public MapView mapView;
 	private NavOverlay routeOverlay; 
 	
 	public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
 		
         setContentView(R.layout.activity_main);
         mapView = (MapView) findViewById(R.id.mapview);
         mapView.setBuiltInZoomControls(true);
         mapOverlays = mapView.getOverlays();
 		Drawable pinDrawable = this.getResources().getDrawable(R.drawable.map_marker);
 		Drawable locationIcon = this.getResources().getDrawable(R.drawable.currentlocation_icon);
 		if (savedInstanceState != null && savedInstanceState.containsKey("pointsOverlayList")) {
 			ArrayList<ParcelableOverlayItem> listOIs = new ArrayList<ParcelableOverlayItem>();
 			for (Parcelable p: savedInstanceState.getParcelableArrayList("pointsOverlayList")) {
 				listOIs.add((ParcelableOverlayItem)p);
 			}
 			itemizedOverlay = new PointsOverlay(listOIs,
 					((ParcelableGeoPoint)savedInstanceState.getParcelable("pointsOverlayStart")),
 					((ParcelableGeoPoint)savedInstanceState.getParcelable("pointsOverlayEnd")), pinDrawable, this, 
 					((LocationManager)getSystemService(Context.LOCATION_SERVICE)));
 		}
 		else {
 			itemizedOverlay = new PointsOverlay(pinDrawable, this, ((LocationManager)getSystemService(Context.LOCATION_SERVICE)));
 			itemizedOverlay.addOverlay(new OverlayItem(new GeoPoint(0, 0), "whoops", "you shouldn't see this"));
 			itemizedOverlay.addOverlay(new OverlayItem(new GeoPoint(0, 0), "whoops", "you shouldn't see this")); //debug code to avoid null pointer exceptions. fix later
 		}
 		mapOverlays.add(itemizedOverlay);
 		locationOverlay = new LocationOverlay(locationIcon, ((LocationManager)getSystemService(Context.LOCATION_SERVICE)), this);
 		mapOverlays.add(locationOverlay);
 		if (savedInstanceState != null && savedInstanceState.containsKey("navOverlayList")) {
 			ArrayList<ParcelableGeoPoint> listGPs = new ArrayList<ParcelableGeoPoint>();
 			for (Parcelable p: savedInstanceState.getParcelableArrayList("navOverlayList")) {
 				listGPs.add((ParcelableGeoPoint)p);
 			}
 			routeOverlay = new NavOverlay(listGPs, 
 					savedInstanceState.getDouble("navOverlayLength"));
 			mapOverlays.add(routeOverlay);
 		}
 		
 		//If Plot Route was pressed get the GeopPoints of the given addresses and plot route
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) {
			String[] addresses = extras.getStringArray("addresses");
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
     
 	private void timeToPlace() {
 		itemizedOverlay.clearTimePoints();
 		ArrayList<GeoPoint> gp = routeOverlay.getNavGPs();
 		double dist = routeOverlay.getLength();
 		int timelineSize = curTimeline.size();
 		double endTimeValue = curTimeline.getPoint(0).getTimeInBC();
 		double timeRange = curTimeline.getPoint(timelineSize-1).getTimeInBC() - endTimeValue;
 		double[] relativePos = new double[timelineSize];
 		for (int i = 0; i < timelineSize; i++) {
 			relativePos[i] = dist * ((timeRange - (curTimeline.getPoint((timelineSize-1)-i).getTimeInBC() - endTimeValue)) / timeRange);
 		}
 		int curCheck = 0;
 		double lengthSoFar = 0.0;
 		for(int i = 0; i < gp.size()-1; i++){
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
 		mapView.postInvalidate();
 		/*work out how far down route each TimePoint should be, normalised to dist, then
 		 *work out how far each geopoint is using distanceKm(), and if TimePoints should go 
 		 *between the two geopoints, and if so, for each timepoint that should go, work out how far between, and...
 		 *Need to confirm that YOURS does in fact use KM for length() though
 		*/
 	}
 	
 	/*
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
 			dialog.show();
 			return null;
 		}
 	}
 	
 	public static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
 	    int EARTH_RADIUS_KM = 6371;
 	    double lat1Rad = Math.toRadians(lat1);
 	    double lat2Rad = Math.toRadians(lat2);
 	    double deltaLonRad = Math.toRadians(lon2 - lon1);
 
 	    return Math.acos(Math.sin(lat1Rad) * Math.sin(lat2Rad) + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.cos(deltaLonRad)) * EARTH_RADIUS_KM;
 	}
 	
 	/*
 	 * Called to plot the timeline on the route from the Home Screen
 	 */
 	public void plotTimeline(Timeline timeline) {
 		curTimeline = timeline;
 		if (mapOverlays.size() == 3) {
 			timeToPlace();
 		}
 	}
 
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
 			dialog.show();
 			point = null;
 			point2 = null;
 	    	Thread nst = new Thread(new NavStartThread(this));
 			nst.start();
 	    	return true;
 	    case R.id.menu_home:
 	    	Intent intentHome = new Intent(this, Home.class);
 	    	startActivity(intentHome);
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
 	}
 	
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 	
 	private class NavStartThread implements Runnable {
 		public MainActivity ma;
 		public ProgressDialog progressDialog;
 		
 		public NavStartThread(MainActivity m) {
 			ma = m;
 		}
 		
 		public void run() {
 			itemizedOverlay.clearPoints();
 	    	itemizedOverlay.setNavMode(true);
 			while (itemizedOverlay.getEndPoint() == null) {
 			//get alerted when user has entered both points
 			//pop up alert with are these two points correct? Yes/No. If no, set navMode to false, remove both points.
 			//if yes, then set navmode to false and set the points to be used in the NavOverlay
 			//then add the two points to the navOverlay and display the route.
 			
 			//TODO: Again, really needs to be some sort  of wait here, with the following code run only after we have our two points
 				try {
 					Thread.sleep(2000);
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			itemizedOverlay.setNavMode(false);
 			
 			waitHandler.sendEmptyMessage(0); //Start the pop-up progress bar
 			
 			if(point == null || point2 == null) {
 				point = itemizedOverlay.getStartPoint();
 				point2 = itemizedOverlay.getEndPoint();
 			}
 			itemizedOverlay.setStartPointOverlay(new OverlayItem(point, "Start", "Start of TimeLine"));
 			itemizedOverlay.setEndPointOverlay(new OverlayItem(point2, "End", "End of TimeLine"));
 			//TODO: An asynctask which does the following since we can't network on main thread
 			if (mapOverlays.size() == 2) {
 				routeOverlay = new NavOverlay(point, point2);
 				mapOverlays.add(routeOverlay);
 			}
 			else if (routeOverlay != null) {
 				routeOverlay = new NavOverlay(point, point2);
 				mapOverlays.set(2, routeOverlay);
 			}
 			if(curTimeline != null) {
 				timeToPlace();
 			}
 			else {
 				mapView.postInvalidate();
 			}
 			
 			waitHandler.sendEmptyMessage(1); //Dismiss the pop-up progress bar
 			
 			//Log.i("MAP_OVERLAYS", Integer.toString(mapOverlays.size()));
 		}
 		
 		/*
 		 * Handler to display a progress pop-up while the route is being calculated
 		 */
 		private Handler waitHandler = new Handler() {
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
