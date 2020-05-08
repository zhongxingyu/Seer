 package edu.aau.utzon.outdoor;
 
import java.security.spec.MGF1ParameterSpec;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.Window;
 import com.actionbarsherlock.app.SherlockMapActivity;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 import com.readystatesoftware.maps.TapControlledMapView;
 import com.readystatesoftware.maps.OnSingleTapListener;
 
 
 import edu.aau.utzon.PoiContentActivity;
 import edu.aau.utzon.PoiListActivity;
 import edu.aau.utzon.R;
 import edu.aau.utzon.augmented.AugmentedActivity;
 import edu.aau.utzon.indoor.IndoorActivity;
 import edu.aau.utzon.location.LocationHelper;
 import edu.aau.utzon.location.NearPoiPublisher;
 import edu.aau.utzon.webservice.PointModel;
 import edu.aau.utzon.webservice.ProviderContract;
 
 public class OutdoorActivity extends SherlockMapActivity implements NearPoiPublisher {
 
 	private LocationHelper mLocationHelper;
 	private Cursor mOutdoorPois;
 	private TapControlledMapView mMapView;
 	private MyLocationOverlay mMyLocationOverlay;
 
 	@Override
 	public void onResume()
 	{
 		super.onResume();
 		mMyLocationOverlay.enableMyLocation();
 		mLocationHelper.onResume();
 	}
 
 	@Override
 	public void onPause()
 	{
 		super.onPause();
 		mMyLocationOverlay.disableMyLocation();
 		mLocationHelper.onPause();
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		// Init locationHelper		
 		this.mLocationHelper = new LocationHelper(getBaseContext());
 		this.mLocationHelper.setNearPoiPublisher(this);
 		mLocationHelper.onCreate(savedInstanceState);
 
 		// Get available POIs
 		mOutdoorPois = getContentResolver().query(ProviderContract.Points.CONTENT_URI, 
 				ProviderContract.Points.PROJECTIONSTRING_ALL, 
 				null, 
 				null, 
 				null);
 
 		// Remove title bar
 		if( android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB ) {
 			requestWindowFeature(Window.FEATURE_NO_TITLE);
 		}
 
 		// Display Google maps to the user
 		setContentView(R.layout.mapview);
 
 		// Enable built-in map controls
 		mMapView = (TapControlledMapView) findViewById(R.id.mapview);
 		mMapView.setBuiltInZoomControls(true);
 
 		// Draw the user position on map
 		mMyLocationOverlay = new MyLocationOverlay(this, mMapView);
 		mMyLocationOverlay.enableMyLocation();
 		mMapView.getOverlays().add(mMyLocationOverlay);
 
 		// Draw POI
 		drawOutdoorPois(PointModel.asPointModel(mOutdoorPois));
 	}
 
 	public void userIsNearPoi(PointModel poi) {
 		Log.e("TACO", "IS NEAR POI");
 		StartPoiContentActivity();
 	}
 
 	private void StartPoiContentActivity() {
 		startActivity(new Intent(getApplicationContext(), PoiContentActivity.class));
 	}
 
 	protected void animateToLocation(Location loc)
 	{
 		// Only animate if its a valid location
 		if(loc != null){
 			MapController mc = mMapView.getController();
 			GeoPoint point =  LocationHelper.locToGeo(loc);
 			mc.animateTo(point);
 		}
 	}
 
 	public void updateOutdoorPois(List<PointModel> pois) {
 		drawOutdoorPois(pois);
 	}
 
 	private BalloonOverlay itemizedoverlay = null;
 	protected void drawOutdoorPois(List<PointModel> list)
 	{
 		//MapView mapView = (MapView) findViewById(R.id.mapview);
 
 		// Setup overlays
 		List<Overlay> mapOverlays = mMapView.getOverlays();
 		mapOverlays.clear();
 		Drawable drawable = this.getResources().getDrawable(R.drawable.androidmarker);
 		itemizedoverlay = new BalloonOverlay(drawable, mMapView);
 
 		// DEBUG
 		//list.clear();
//		PointModel pm = new PointModel(10, "MyDescriptionHere", "MyNameHere", 57.036564, 9.919659); // Aalborg
//		OverlayItem item = new OverlayItem(pm.getGeoPoint(), pm.getName(), pm.getDesc());
//		itemizedoverlay.addOverlay(item);
 		// Add POI to the overlay
 		for(PointModel p : list)
 		{
			GeoPoint gp = p.getGeoPoint();
			int id = p.getId();
			String n = p.getName();
			String d = p.getDesc();
			itemizedoverlay.addOverlay(new OverlayItem(gp, n, d));
 		}
 
 		// Balloon stuff
 		mMapView.setOnSingleTapListener(new OnSingleTapListener() {		
 			@Override
 			public boolean onSingleTap(MotionEvent e) {
 				
 				return true;
 			}
 		});
 
 		// set iOS behavior attributes for overlay (?)
 		itemizedoverlay.setShowClose(false);
 		itemizedoverlay.setShowDisclosure(true);
 		itemizedoverlay.setSnapToCenter(false);
 
 		mapOverlays.add(itemizedoverlay);
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.layout.menu_outdoor, menu);
 
 		MenuItem searchItem = menu.findItem(R.id.actionbar_search); // TODO: Implement
 
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.actionbar_center_location:
 			animateToLocation(mLocationHelper.getCurrentLocation());
 			return true;
 		case R.id.actionbar_poi_list:
 			startActivity(new Intent(this, PoiListActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
 			return true;
 		case R.id.actionbar_search:
 			// TODO: Implement
 			onSearchRequested();
 			return true;
 		case R.id.actionbar_augmented:
 			startActivity(new Intent(this, AugmentedActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
 			return true;
 		case R.id.actionbar_indoor:
 			startActivity(new Intent(this, IndoorActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	public List<PointModel> getPois() {
 		return PointModel.asPointModel(mOutdoorPois);
 	}
 }
