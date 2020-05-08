 //my failed java part 2 (sunday)
 
 package org.jbs.happysad;
 
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.OverlayItem;
 import android.widget.Button;
 import android.widget.Toast;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Random;
 
 /**
  * Creates a Global Map view with Google Maps API with everyone's HappyBottles
  * @author HappyTrack
  */
 public class GlobalMap extends MapActivity implements OnClickListener {
 	//fields
 	private static final String TAG = "GlobalMap";
 	private MapView map; 
 	int checkHappy;
 	int checkSad;
 	boolean run;
 	boolean check;
 	int streetView;
 	MyLocationOverlay userLocationOverlay;
 	private MapController controller;
 	ItemizedEmotionOverlay happyOverlay; 
 	ItemizedEmotionOverlay sadOverlay;
 	HappyData datahelper = new HappyData(this);
 	//	private volatile ArrayList<HappyBottle>  plottables;
 	int zoomLevel;
 	GeoPoint center;
 	private Handler handler;
 	Runnable latestThread;
 	ZoomPanListener zpl;
 
 	/**
 	 * Initializes Activity
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.map);
 
 		checkHappy = getIntent().getExtras().getInt("Happy");
 		checkSad = getIntent().getExtras().getInt("Sad");
 		run = getIntent().getExtras().getBoolean("Run");
 		streetView = getIntent().getExtras().getInt("Street");
 
 		//Defines the drawable items for the happy and sad overlays
 		Drawable happyface = this.getResources().getDrawable(R.drawable.pinhappy);
 		Drawable sadface = this.getResources().getDrawable(R.drawable.pinsad);
 
 		//initializes the happy and sad overlays
 		happyOverlay = new ItemizedEmotionOverlay(happyface, this);	
 		sadOverlay = new ItemizedEmotionOverlay(sadface, this);
 		//initialize and display map view and user location
 		initMapView();
 		initMyLocation();	
 
 		
 
 		//Finds the show_sad view
 		View sadButton = findViewById(R.id.showSad);
 		sadButton.setOnClickListener(this);
 
 		//Finds the show_happy view
 		View happyButton = findViewById(R.id.showHappy);
 		happyButton.setOnClickListener(this); 
 
 		//Finds the switch_view
 		View switchButton = findViewById(R.id.switchView);
 		switchButton.setOnClickListener(this); 
 
 		//Finds the chart_button view
 		View chartButton = findViewById(R.id.myTrack_button);
 		chartButton.setOnClickListener(this);  	
 		
 		View histButton = findViewById(R.id.myChart_button);
 		histButton.setOnClickListener(this);
 		
 		//Finds the my_map view
 		View myButton = findViewById(R.id.map);
 		((Button) myButton).setText("MyMap");
 		myButton.setOnClickListener(this);
 
 		center = new GeoPoint(-1,-1);
 		zoomLevel = -1;
 		handler = new Handler();
 
 	}
 
 	/**
 	 * Invoked when a view is clicked
 	 */
 	@Override
 	public void onClick(View v) {
 		switch(v.getId()){
 
 		//checks what current view is, then switches it off and starts the alternate view
 		case R.id.switchView:
 			if (streetView==0) {
 				map.setStreetView(true);
 				streetView = 1;
 				map.setSatellite(false);  
 			} else{
 				map.setStreetView(false);
 				streetView = 0;
 				map.setSatellite(true);
 			}
 			map.invalidate();
 			break;
 
 			//used to show/hide the happy faces
 		case R.id.showHappy:
 			if (checkHappy==0){ 
 				map.getOverlays().add(happyOverlay); //adds happy face overlay to visible overlays 
 				checkHappy = 1; 
 			} else{ 
 				map.getOverlays().clear(); //clears all overlays
 				if (checkSad == 1){
 					map.getOverlays().add(sadOverlay);  //if sad faces should be visible, it adds them back
 				}
 				checkHappy = 0;
 			}
 			invalidateOverlay(); //method call
 			break;
 
 			//used to show/hide the sad faces
 		case R.id.showSad:		
 			if (checkSad == 0){
 				map.getOverlays().add(sadOverlay); //adds sad face overlay to visible overlays
 				checkSad = 1; 
 			} else{
 				map.getOverlays().clear(); //clears all overlays
 				if (checkHappy==1) {
 					map.getOverlays().add(happyOverlay); //if happy faces should be visible, it adds them back
 				}
 				checkSad = 0;
 			}
 			invalidateOverlay(); //method call
 			break;
 
 		case R.id.map:
 			Intent j = new Intent(this, MyMap.class);
 			j.putExtra("Street", streetView);
 			j.putExtra("Run", false);
 			j.putExtra("Happy", checkHappy);
 			j.putExtra("Sad", checkSad);
 			startActivity(j);
 			finish();
 			break;
 
 		case R.id.myTrack_button:
 			startActivity(new Intent(this, History.class));
 			break;
 		
 		case R.id.myChart_button:
 			startActivity(new Intent(this, ChartList.class));
 			break;
 		}
 
 		map.invalidate();
 	}
 
 	//helper method for showHappy and showSad onClick cases
 	private void invalidateOverlay() {
 		map.getOverlays().add(userLocationOverlay);
 	}
 
 	//Finds and initializes the map view.
 	private void initMapView() {
 		map = (MapView) findViewById(R.id.themap);
 		controller = map.getController();
 		
 		//checks streetView
 		if (streetView == 1) {
 			map.setStreetView(true);
 			map.setSatellite(false);
 		} else {
 			map.setStreetView(false);
 			map.setSatellite(true);	
 		}
 		map.invalidate();	
 
 		//adds the sad and happy overlays to the map
 		if (checkSad == 1)
 			map.getOverlays().add(sadOverlay);
 		if (checkHappy == 1) 
 			map.getOverlays().add(happyOverlay);
 		map.setBuiltInZoomControls(false); //hides the default map zoom buttons so they don't interfere with the app buttons
 
 	}
 
 	//Starts tracking the users position on the map. 
 	private void initMyLocation() {
 		userLocationOverlay = new MyLocationOverlay(this, map);
 		userLocationOverlay.enableMyLocation();
 	}
 	
 	private void goToMyLocation() {
 		if (run == true) {
 			map.getOverlays().add(userLocationOverlay);
 			userLocationOverlay.runOnFirstFix(new Runnable() {
 				public void run() {
 					// Zoom in to current location
 					controller.animateTo(userLocationOverlay.getMyLocation());
 					controller.setZoom(15); //sets the map zoom level to 15
 				}
 			});
 		}
 		map.getOverlays().add(userLocationOverlay); //adds the users location overlay to the overlays being displayed
 	}
 
 
 	//creates an emotion overlay
 	private static synchronized void emotionOverlaySetter(int emotion, ArrayList<HappyBottle> toshow, ItemizedEmotionOverlay overlay){ 
 
 		if (toshow == null) {return; }///THIS IS A PROBLEM AND SHOULD NEVER HAPPEN
 		overlay.emptyOverlay();
 		for(HappyBottle bottle : toshow) {
 
 			if (bottle.getEmo()==emotion) { //happy or sad filter
 				int latitude = bottle.getLat();
 				int longitude = bottle.getLong();
 				GeoPoint point = new GeoPoint(latitude,longitude);
 				String S = (String) new Timestamp(bottle.getTime()).toLocaleString();
 				overlay.addToOverlay(new OverlayItem(point, S+emotion, bottle.getMsg()));
 			}
 		}
 	}
 
 	/**
 	 * This method updates the overlays for only the current the current view
 	 */
 	private ArrayList<HappyBottle> updateToView(){
 		//Log.w("updateToView", "ERROR in new method");
 		GeoPoint center = map.getMapCenter(); //gets coordinates for map view's center
 		int centerLat = center.getLatitudeE6(); //finds center's latitude
 		int centerLong = center.getLongitudeE6(); //finds center's longitude
 		int width = map.getLongitudeSpan(); //gets width of view in terms of longitudes shown on screen
 		int height = map.getLatitudeSpan(); //gets height of view in terms of latitudes shown on screen
 		int minLong = centerLong-width/2; //gets the left most longitude shown
 		int maxLong = centerLong+width/2; //gets the right most longitude shown
 		int maxLat = centerLat+height/2; //gets the top most latitude shown
 		int minLat = centerLat-height/2; //gets the bottom most latitude shown
 		Log.d(TAG, "we are now using local pins with updateToView");
 		return datahelper.getLocalRecent(minLat,maxLat,minLong,maxLong,100);
 	}
 
 	//to sync and update bottles with mapview
 	private ArrayList<HappyBottle> updater(){
 		if(isMoved()){
 			center = map.getMapCenter();
 			zoomLevel = map.getZoomLevel();
 			Log.d(TAG, "updatetoview from updater");
 			return updateToView();
 		}
 		return null;
 	}
 
 	private void stablePainter(){
 		//Creates a new runnable that stabalizes the screen
 		Runnable runnable = new Runnable(){
 			@Override
 			public void run(){
 				int maxcount = 1000;
 				for (int i = 0; i < maxcount; i++) {
 					//testing about getLatitudeSpan and getLongitudeSpan fake values
 					if (map.getLatitudeSpan() == 0 & map.getLongitudeSpan() == 360000000){
 						try{
 							//thread will sleep awhile!
 							Thread.sleep(80);
 						}catch (InterruptedException ex){
 							ex.printStackTrace();
 						}
 				//map is stabilized and if we get height and width will have be real values. 
 				} else {
 						//remove other tasks if queued for the handler
 						handler.removeCallbacks(latestThread); 
 						//new Runnable that updates the overlay
 						latestThread = new Runnable(){
 							@Override
 							public void run(){
 								Log.d(TAG, "running the thread that updates the overlays");
 								ArrayList<HappyBottle> newBottles = updater();
 								if (newBottles != null){
 									emotionOverlaySetter(1,newBottles,happyOverlay);
 									emotionOverlaySetter(0,newBottles,sadOverlay);
 								}
 							}
 						};
 						handler.postDelayed(latestThread, 10); //delay the posting of the new pins by a tiny fraction of a  second, because that way it will let you invalidate if you keep moving.
 						maxcount = 0; //exit internal loop
 						return;
 					}	};}};
 
 					new Thread(runnable).start();
 
 	}
 
 	private class ZoomPanListener extends AsyncTask<Void, Void, Void>{
 		@Override
 		protected Void doInBackground(Void... params) {
 			while(true){
 				if(isMoved()){
 					stablePainter();
 				}
 				try {
 					Thread.sleep(100);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}}}}
 
 	//Required method to make the map work
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 
 
 	public boolean isMoved() {
 		GeoPoint trueCenter =map.getMapCenter();
 		int trueZoom = map.getZoomLevel();
 
 		if(!((trueCenter.equals(center)) && (trueZoom == zoomLevel))){
 			
 			Log.d(TAG, "You moved!:" + center.toString() + " zoom: " + zoomLevel);
 			return true;
 		}
 		else{
 			return false;
 		}
 	}
 
 
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.menu, menu);
 	    return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    // Handle item selection
 	    switch (item.getItemId()) {
 	    case R.id.current_location:
 	    	run = true;
 	        goToMyLocation();
 	        return true;
 	    case R.id.new_update:
	    	startActivity(new Intent(this, GlobalMap.class));
 	        
 	    default:
 	        return super.onOptionsItemSelected(item);
 	    }
 	}
 	
 
 	//Disables MyLocation
 	@Override
 	protected void onPause() {
 		userLocationOverlay.disableMyLocation();  
 		synchronized (zpl){
 			zpl.cancel(true);}
 		super.onPause();
 	}
 
 	//Enables MyLocation 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		userLocationOverlay.enableMyLocation();
 		Random r = new Random();
 		center = new GeoPoint(-10, r.nextInt()); //fake a move so that updater thinks we've moved and populates the initial screen.
 		zpl = new ZoomPanListener();
 		zpl.execute(null);
 		
 		stablePainter();
 		
 	
 	}
 }
