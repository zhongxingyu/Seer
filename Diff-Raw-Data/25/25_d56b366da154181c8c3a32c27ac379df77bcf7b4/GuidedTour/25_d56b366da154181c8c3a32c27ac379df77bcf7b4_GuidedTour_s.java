 package com.example.tourapp;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.TextView;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 public class GuidedTour extends MapActivity implements OnClickListener {
 	//map fields
 	private MapView mMapView;
 	private List<Overlay> mMapOverlays;
 	private MapController mController;
 	private Drawable mDrawable;
 	private MyItemizedOverlay mItemizedOverlay;
 	
 	//text fields
 	private TextView mTitleView;
 	private TextView mDescView;

 	public ArrayList<PlaceObject> mListOfPlaces;
 	public static int mCurrentPlaceId;
 	
 	//constants
 	private static final int BALLOON_OPACITY = 200;
 	private static final int MAP_ZOOM = 18;
 	private static final double NORMALIZE_COORDINATES = 1E6;
 	
 	//change? appears in changeTextOfDisplayedObject
 	//test arraylist
 	private ArrayList<PlaceObject> mManualListOfPlaceObjects;

 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.guidedtour);
			
 		Bundle extras = getIntent().getExtras();
 		mListOfPlaces = extras.getParcelableArrayList("com.example.tourapp.placeArrayList");
		
 		PlaceObject currentPlace = mListOfPlaces.get(mCurrentPlaceId); //initial object is Mills Hall
 			
 		//map view
 		initMapView();
 		initMyLocation();
 		mMapOverlays = mMapView.getOverlays();
 		
 		// create itemized overlay
 		mDrawable = getResources().getDrawable(R.drawable.marker);
 		mDrawable.setAlpha(BALLOON_OPACITY);
 		mItemizedOverlay = new MyItemizedOverlay(mDrawable, mMapView);
 		
 		//Create balloon overlays using test arraylist
 		for (PlaceObject obj : mListOfPlaces){
 			double lat = Double.parseDouble(obj.getLat());
 			double lon = Double.parseDouble(obj.getLon());
 			createOverlay(lat, lon, obj.getId(), obj.getName(), obj.getDirFromPrev());
 		} //for
 		mMapOverlays.add(mItemizedOverlay);
 		
 		//view stuff		
 		mTitleView = (TextView) findViewById(R.id.title_view);
 		mTitleView.setText(currentPlace.getName());
		
 		mDescView = (TextView) findViewById(R.id.desc_view);
 		mDescView.setText(currentPlace.getDescription());
		
 		findViewById(R.id.button_prev).setOnClickListener(this);
 		findViewById(R.id.button_next).setOnClickListener(this);
 	} //onCreate
 	
 	//helper methods
 	/** Find and initialize the map view. (From Hello Android tutorial)*/
 	private void initMapView() {
 		mMapView = (MapView) findViewById(R.id.mapview);
 		mController = mMapView.getController();
 		mMapView.setSatellite(true);
 		mMapView.setBuiltInZoomControls(true);
 	} //initMapView
 
 	/** Start tracking the position on the map. (From Hello Android tutorial)*/
 	private void initMyLocation() {
 		final MyLocationOverlay overlay = new MyLocationOverlay(this, mMapView);
 		overlay.enableMyLocation();
 		overlay.enableCompass(); // does not work in emulator
 		overlay.runOnFirstFix(new Runnable() {
 			public void run() {
 				// Zoom in to current location
 				mController.setZoom(MAP_ZOOM);
 				mController.animateTo(overlay.getMyLocation());
 			} //run
 		}); //runOnFirstFix
 		mMapView.getOverlays().add(overlay);
 	} //initMyLocation
 
 	/**
 	 * Creates a balloon overlay that puts a point on the map of the building specified 
 	 * in the parameters
 	 * @param lat Latitude of the building
 	 * @param lon Longitude of the building
 	 * @param name Name of the building
 	 * @param directions Directions to the building from the previous building
 	 */
 	public void createOverlay (double lat, double lon, int id, String name, String directions){
 		GeoPoint point = new GeoPoint((int)(lat*NORMALIZE_COORDINATES ),(int)(lon*NORMALIZE_COORDINATES));
 		id++; //So id starts at 1, not 0
 		OverlayItem overlayItem = new OverlayItem(point, id + ". "+ name, directions);
 		mItemizedOverlay.addOverlay(overlayItem);
 	} //createOverlay
 
 	public String getTextOfSelectedBalloon(int index){
 		PlaceObject obj = mManualListOfPlaceObjects.get(index);
 		return obj.getDescription();	
 	} //getTextOfSelectedBalloon
 	
 	/**
 	 * Specifies whether there is a route displayed
 	 * @return True is a route is displayed, false if there is not a route displayed
 	 */
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	} //isRouteDisplayed
 	
 	/**
 	 * sends the user to next or previous point on the tour
 	 */
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()){
 		case R.id.button_prev:
 			if(mCurrentPlaceId >= 1) {
 				mCurrentPlaceId--;
 			} //if
 			Intent iPrev = new Intent (this, GuidedTour.class);
 			iPrev.putParcelableArrayListExtra("com.example.tourapp.placeArrayList", mListOfPlaces);
 			startActivity(iPrev);
 			break;
 		case R.id.button_next:
 			if(mCurrentPlaceId < mListOfPlaces.size()) {
 				mCurrentPlaceId++;
 			} //if
 			Intent iNext = new Intent (this, GuidedTour.class);
 			iNext.putParcelableArrayListExtra("com.example.tourapp.placeArrayList", mListOfPlaces);
 			startActivity(iNext);
 			break;
		default:
			break;
 		} //end switch statement
 	} //end method onClick
 } //GuidedTour
