 package com.example.shutapp;
 
 import java.util.List;
 import android.os.Bundle;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 
 public class GpsActivity extends MapActivity implements LocationListener {
 	private final int zoomLevel = 17; // 1 to 21
 	private GeoPoint gP;
 	private MapController mControl;
 	private MyLocationOverlay compass;
 	private MapOverlay itemizedoverlay;
 	private List<Overlay> mapOverlays;
 	private Drawable drawable;
 	private LocationManager lm;
<<<<<<< HEAD
 	private RadiusOverlay radiusOverlay;
 
=======
 	private double lat, lon;
 	private Criteria crit;
>>>>>>> 3519cf095495d93820a94b3c00b7a8d91fa36651
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_map);
 
 		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 
 		MapView mapView = (MapView) findViewById(R.id.mapView);
 		mapView.setBuiltInZoomControls(true);   //true = being able to zoom with buttons
 		mapView.getController().setZoom(zoomLevel); //sets zoomlevel from the startup
 
 		mControl = mapView.getController();
 		mapOverlays = mapView.getOverlays();
 		compass = new MyLocationOverlay(GpsActivity.this, mapView);
 		mapOverlays.add(compass);
 		drawable = this.getResources().getDrawable(R.drawable.maparrow);
 		
 		crit = new Criteria();
         String bestProvider = lm.getBestProvider(crit, false);       
         Location startLocation = lm.getLastKnownLocation(bestProvider);
         
 		if (startLocation != null){
 			lat = startLocation.getLatitude();
 			lon = startLocation.getLongitude();
 			updatePosition(lat, lon);
 			newOverlay();
 		}	else{
 			Log.e("GPS","Unable to get startlocation");
 		}
<<<<<<< HEAD
 		float radius = 1000;
 
 		updatePosition(lat, lon);
 
 		mapOverlays = mapView.getOverlays();
 		compass = new MyLocationOverlay(GpsActivity.this, mapView);
 		drawable = this.getResources().getDrawable(R.drawable.maparrow);
 		radiusOverlay = new RadiusOverlay(this, 57.7012596130371, 11.9670495986938, radius);
 		itemizedoverlay = new MapOverlay(drawable, this);
 
 		OverlayItem overlayitem = new OverlayItem(gP, null, null);
 		itemizedoverlay.addOverlay(overlayitem);
 		mapOverlays.add(itemizedoverlay);
 		mapOverlays.add(compass);
 		mapOverlays.add(radiusOverlay);
 
=======
>>>>>>> 3519cf095495d93820a94b3c00b7a8d91fa36651
 	}
 
 	private void updatePosition(double latitude, double longitude) {
 		gP = new GeoPoint((int)(latitude*1E6), (int)(longitude*1E6)); //converting to micro-degrees with 1E6
 		mControl.animateTo(gP);
 		
 	}
 	
 	public GeoPoint getLocation(){
 		return gP;
 	}
 	
 	public void newOverlay(){
 		itemizedoverlay = new MapOverlay(drawable, this);
 		OverlayItem currentOverlay = new OverlayItem(gP,"Current Location","Here is my current location!!!");
 		itemizedoverlay.addOverlay(currentOverlay);
 		mapOverlays.add(itemizedoverlay);
 	} 
 	
 	public void onLocationChanged(Location location) {
 		lat = location.getLatitude();
 		lon = location.getLongitude();
 		Log.e("GPS", "location changed: lat="+String.valueOf(lat)+", lon="+String.valueOf(lon));
 		updatePosition(lat, lon);
 
 		if(itemizedoverlay!=null) {
 			mapOverlays.remove(itemizedoverlay);
 		} 
 		newOverlay();		
 	}
 
 	@Override
 	protected void onPause() {
 		compass.disableCompass();
 		super.onPause();
 		lm.removeUpdates(this);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		compass.enableCompass();
 		lm.requestLocationUpdates(lm.getBestProvider(crit, false), 20*1000, 20, this);        
 	}
 
 	public void onProviderDisabled(String arg0) {
 		Log.e("GPS", "provider disabled " + arg0);
 	}
 	public void onProviderEnabled(String arg0) {
 		Log.e("GPS", "provider enabled " + arg0);
 	}
 	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
 		Log.e("GPS", "status changed to " + arg0 + " [" + arg1 + "]");
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		// TODO Auto-generated method stub
 		return false;
 	}        
 
 
 	public void toChatActivity(View view){
 		Intent intentToRedirect = new Intent(this, ChatActivity.class);
 		startActivity(intentToRedirect);
 		overridePendingTransition(0, 0);
 	}
 
 
 	public void toNearbyConversationsActivity(View view){
 		Intent intentToRedirect = new Intent(this, NearbyConversationsActivity.class);
 		startActivity(intentToRedirect);
 		overridePendingTransition(0, 0);
 	}
 
 	public void toGpsActivity(View view){
 		Intent intentToRedirect = new Intent(this, GpsActivity.class);
 		startActivity(intentToRedirect);
 		overridePendingTransition(0, 0);
 	}
 
 	public void toSettingsActivity(View view){
 		Intent intent = new Intent(this, SettingsActivity.class);
 		startActivity(intent);
 		overridePendingTransition(0, 0);
 	} 
 }
