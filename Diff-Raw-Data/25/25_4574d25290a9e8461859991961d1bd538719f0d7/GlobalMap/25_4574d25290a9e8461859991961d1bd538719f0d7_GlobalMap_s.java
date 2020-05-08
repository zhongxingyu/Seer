 package org.jbs.happysad;
 
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.OverlayItem;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 /**
  * Creates a Global Map view with Google Maps API with everyone's HappyBottles
  * @author HappySad
  */
 public class GlobalMap extends MapActivity implements OnClickListener {
 	//fields
 	private MapView map;
 	private MapController controller;
 	int checkHappy = 1;
 	int checkSad = 1;
 	MyLocationOverlay overlay;
 	ItemizedEmotionOverlay happyOverlay;
 	ItemizedEmotionOverlay sadOverlay;
 	
 	/**
 	 * Initializes Activity
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.map);
 		Drawable drawable = this.getResources().getDrawable(R.drawable.mapsmile);
 		happyOverlay = new ItemizedEmotionOverlay(drawable, this);
 		Drawable drawable2 = this.getResources().getDrawable(R.drawable.mapfrown);
 		sadOverlay = new ItemizedEmotionOverlay(drawable2, this);
 		
 		//get all HappyBottles from HappyData
 		HappyData datahelper = new HappyData(this);
 		ArrayList<HappyBottle> plottables = datahelper.getAllHistory();
 		
 		//plot overlays
 		emotionOverlayMaker(0,plottables,sadOverlay);
 		emotionOverlayMaker(1,plottables,happyOverlay);
 		
 		//initialize map and location
 		initMapView();
 		initMyLocation();
       
 		//Add ClickListener for the button
 		View sadButton = findViewById(R.id.showSad);
 		sadButton.setOnClickListener(this);
       
 		// Add ClickListener for the button
 		View happyButton = findViewById(R.id.showHappy);
 		happyButton.setOnClickListener(this); 
       
 		// Add ClickListener for the button
 		View switchButton = findViewById(R.id.switchView);
 		switchButton.setOnClickListener(this); 
 	}
    
     /**
      * Invoked when a view is clicked
      */
 	@Override
 	public void onClick(View v) {
 		switch(v.getId()){
 		case R.id.switchView:
 			if (map.isStreetView()==false){
 				map.setStreetView(true);
 				map.setSatellite(false);  
 			} else{
 				map.setStreetView(false);
 				map.setSatellite(true);
 			}
 			break;
 			
 		case R.id.showHappy:
 			if (checkHappy==0){
 				map.getOverlays().add(happyOverlay);
 				checkHappy = 1;
 			} else{
 				map.getOverlays().clear();
 				if (checkSad == 1){
 					map.getOverlays().add(sadOverlay);
 				}
 				checkHappy = 0;
 			}
 			break;
 			
 		case R.id.showSad:		
 			if (checkSad == 0){
 				map.getOverlays().add(sadOverlay);
 				checkSad = 1;	        	   
 			} else{
 				map.getOverlays().clear();
 				if (this.checkHappy==1){
 					map.getOverlays().add(happyOverlay);
 				}
 				checkSad = 0;
 			}
 			break;
		}
		map.getOverlays().add(overlay);
		map.invalidate();       
 	}
 	
 	// Find and initialize the map view.
 	private void initMapView() {
 		map = (MapView) findViewById(R.id.map);
 		controller = map.getController();
 		map.setStreetView(true);
 		map.getOverlays().add(sadOverlay);
 		map.getOverlays().add(happyOverlay);
 		map.setBuiltInZoomControls(true);
 	}
 	
 	// Start tracking the position on the map.
 	private void initMyLocation() {
 		overlay = new MyLocationOverlay(this, map);     
 		overlay.enableMyLocation();
 		overlay.runOnFirstFix(new Runnable() {
 			public void run() {
 				// Zoom in to current location
 				controller.setZoom(15);
 				controller.animateTo(overlay.getMyLocation());
 			}
 		});
 		map.getOverlays().add(overlay);
 	}
 	
 	// Creates and returns overlay Item
 	private static void emotionOverlayMaker(int emotion, ArrayList<HappyBottle> plottables, ItemizedEmotionOverlay itemizedoverlay){
 		Iterator<HappyBottle> itr = plottables.iterator(); 
 		while(itr.hasNext()) {
 			HappyBottle element = itr.next();
 			if (element.getEmo()==emotion){
 				int latitude =  (int) (element.getLat()*1E6);
 				int longitude =  (int) (element.getLong()*1E6);
 				GeoPoint point = new GeoPoint(latitude,longitude);
 				long time = element.getTime();
 				String S ="";
 				S = S + new Timestamp(time).toLocaleString();
 				itemizedoverlay.addOverlay(new OverlayItem(point, S, element.getMsg()));		        
 			}
 		}   
 	}
 	
 	//Required method for mapView
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 	
 	//Disables MyLocation
 	@Override
 	protected void onPause() {
 		super.onPause();
 		overlay.disableMyLocation();  
 	}
 	
 	//Enables MyLocation
 	@Override
 	protected void onResume() {
 		super.onResume();
 		overlay.enableMyLocation();
 	}
 }
 
