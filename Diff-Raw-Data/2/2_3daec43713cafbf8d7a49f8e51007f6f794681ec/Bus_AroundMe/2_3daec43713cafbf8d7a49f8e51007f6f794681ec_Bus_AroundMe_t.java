 package org.mad.bus;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockMapActivity;
 import com.actionbarsherlock.view.MenuItem;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 public class Bus_AroundMe extends SherlockMapActivity {
 	private MapView mapView;
 	private List<Overlay> mapOverlays;
 	private Drawable drawable;
 	private Bus_StopOverlay itemizedOverlay;
 	private MapController mc;
 	private LocationManager lm;
 	private MyLocationListener ll;
 	private static final int centerLat = (int) (37.2277 * 1E6);
 	private static final int centerLng = (int) (-80.422037 * 1E6);
 	boolean gps_enabled = false;
 	boolean network_enabled = false;
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.maps);
 		ActionBar actionBar = getSupportActionBar();
 		actionBar.setDisplayHomeAsUpEnabled(true);
 		actionBar.setDisplayUseLogoEnabled(true);
 		actionBar.setLogo(R.drawable.ic_launcher);
 		actionBar.setDisplayShowHomeEnabled(true);
 		actionBar.setTitle("Around Me");
 		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		try {
 			gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
 		} catch (Exception ex) {
 			Toast.makeText(
 					this,
 					"GPS Provider not available.",
 					Toast.LENGTH_SHORT).show();
 		}
 		try {
 			network_enabled = lm
 					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
 		} catch (Exception ex) {
 			Toast.makeText(
 					this,
 					"Network Provider not available.",
 					Toast.LENGTH_SHORT).show();
 		}
 		if (gps_enabled == true || network_enabled == true)
 			refreshMap();
 		else
 			Toast.makeText(
 					this,
 					"GPS or Network Provider not available.",
 					Toast.LENGTH_SHORT).show();
 	}
 
 
 	protected void refreshMap() {
 		mapView = (MapView) findViewById(R.id.mapview);
 		mapView.setBuiltInZoomControls(true);
 		mc = mapView.getController();
 		GeoPoint mapCenter = new GeoPoint(centerLat, centerLng);
 		mc.setCenter(mapCenter);
 		mapOverlays = mapView.getOverlays();
 		mapView.getOverlays().clear();
 		drawable = this.getResources().getDrawable(R.drawable.stop);
 		itemizedOverlay = new Bus_StopOverlay(drawable, this, true);
 
 		// //Standard view of the map(map/sat)
 		// mapView.setSatellite(false);
 		// //get controller of the map for zooming in/out
 		mc = mapView.getController();
 		// // Zoom Level
 		mc.setZoom(7);
 		//
 		MyLocationOverlay myLocationOverlay = new MyLocationOverlay(this,
 				mapView);
 		myLocationOverlay.enableMyLocation();
 		mapView.getOverlays().add(myLocationOverlay);
 		List<Overlay> overlayList = mapView.getOverlays();
 		overlayList.add(myLocationOverlay);
 
 		ll = new MyLocationListener();
 		GeoPoint initGeoPoint = null;
 		if (network_enabled) {
 			lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
 					ll);
 			initGeoPoint = new GeoPoint(
 					(int) (lm.getLastKnownLocation(
 							LocationManager.NETWORK_PROVIDER).getLatitude() * 1000000),
 							(int) (lm.getLastKnownLocation(
 									LocationManager.NETWORK_PROVIDER).getLongitude() * 1000000));
 		} else if (gps_enabled) {
 			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll);
 			initGeoPoint = new GeoPoint(
 					(int) (lm
 							.getLastKnownLocation(LocationManager.GPS_PROVIDER)
 							.getLatitude() * 1000000), (int) (lm
 									.getLastKnownLocation(
											LocationManager.GPS_PROVIDER)
 											.getLongitude() * 1000000));
 		}
 		/*
 		 * 37.2552,-80.484518 37.253287,-80.343069 37.114884,-80.466322
 		 * 37.1168,-80.370878
 		 */
 		//		initGeoPoint = new GeoPoint(37227582, -80422165);
 		if (initGeoPoint.getLatitudeE6() < 37255200
 				&& initGeoPoint.getLatitudeE6() > 37114884
 				&& initGeoPoint.getLongitudeE6() > -80484518
 				&& initGeoPoint.getLongitudeE6() < -80370878) {
 			mc.setZoom(18);
 			@SuppressWarnings({ "rawtypes", "unchecked" })
 			ArrayList<Bus_Record> list = new ArrayList(
 					Bus_Constants.DB.get(initGeoPoint.getLatitudeE6(),
 							initGeoPoint.getLongitudeE6(), 4000));
 			for (Bus_Record rec : list) {
 				GeoPoint point = new GeoPoint(rec.getX(), rec.getY());
 				OverlayItem overlayitem = new OverlayItem(point,
 						"Hola, Mundo!", "I'm in Mexico City!");
 				itemizedOverlay.addOverlay(overlayitem);
 				mapOverlays.add(itemizedOverlay);
 			}
 
 		} else {
 			Toast.makeText(this, "There are no bus stops close to you!",
 					Toast.LENGTH_SHORT).show();
 		}
 		// Get the current location in start-up
 		mc.animateTo(initGeoPoint);
 		//		mapView.invalidate();
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 
 	private class MyLocationListener implements LocationListener {
 
 		public void onLocationChanged(Location argLocation) {
 			// TODO Auto-generated method stub
 			@SuppressWarnings("unused")
 			GeoPoint myGeoPoint = new GeoPoint(
 					(int) (argLocation.getLatitude() * 1000000),
 					(int) (argLocation.getLongitude() * 1000000));
 			/*
 			 * it will show a message on location change
 			 * Toast.makeText(getBaseContext(), "New location latitude ["
 			 * +argLocation.getLatitude() + "] longitude [" +
 			 * argLocation.getLongitude()+"]", Toast.LENGTH_SHORT).show();
 			 */
 
 //			 mc.animateTo(myGeoPoint);
 //			 refreshMap();
 
 		}
 
 		public void onProviderDisabled(String provider) {
 			// TODO Auto-generated method stub
 		}
 
 		public void onProviderEnabled(String provider) {
 			// TODO Auto-generated method stub
 		}
 
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 			// TODO Auto-generated method stub
 		}
 	}
 
 	/**
 	 * Handles the clicking of action bar icons.
 	 */
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			finish();
 			return true;
 		}
 		return false;
 	}
 }
