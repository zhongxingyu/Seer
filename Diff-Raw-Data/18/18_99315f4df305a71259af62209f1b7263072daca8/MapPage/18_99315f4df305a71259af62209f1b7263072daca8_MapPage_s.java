 package lab.mpp;
 
 import java.util.List;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 import android.app.Activity;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.util.Log;
 
 public class MapPage extends MapActivity implements LocationListener {
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.map);
 		MapView map = (MapView) findViewById(R.id.mapview);
 		map.setBuiltInZoomControls(true);
 		MapController mc = map.getController();
 		mc.setZoom(20);
 
 		while (true) {
 			Log.e("log", "log");
 			LocationManager lmgr = (LocationManager) getSystemService(LOCATION_SERVICE);
 			Criteria criteria = new Criteria();
 
 			String best = lmgr.getBestProvider(criteria, true);
 			best = LocationManager.GPS_PROVIDER;
 
 			lmgr.requestLocationUpdates(best, 5000, 1, MapPage.this);
 
 			Location location = lmgr.getLastKnownLocation(best);
 			if (location == null) {
 				best = lmgr.getBestProvider(criteria, true);
 				lmgr.requestLocationUpdates(best, 5000, 1, MapPage.this);
 				location = lmgr.getLastKnownLocation(best);
 			} else if (System.currentTimeMillis() - location.getTime() > 30000) {
 				best = lmgr.getBestProvider(criteria, true);
 				lmgr.requestLocationUpdates(best, 5000, 1, MapPage.this);
 				location = lmgr.getLastKnownLocation(best);
 			}
 			if (location != null) {
 				if (System.currentTimeMillis() - location.getTime() <= 30000) {
 					// TODO Auto-generated method stub
 					double x = location.getLatitude();
 					double y = location.getLongitude();
 					GeoPoint p = new GeoPoint((int) (x * 1000000),
 							(int) (y * 1000000));
 					mc.setCenter(p);
 					List<Overlay> mapOverlays = map.getOverlays();
 					MyOverlay pin = new MyOverlay(this.getResources()
 							.getDrawable(R.drawable.icon));
 					pin.addOverlay(new OverlayItem(p, "", ""));
 
 					pin.addOverlay(new OverlayItem(new GeoPoint(
 							(int) (x * 1000000 + 200),
 							(int) (y * 1000000 + 200)), "", ""));
 					mapOverlays.add(pin);
 					break;
 				}
 			}
 		}
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		// this.getParent().getParent().setTitle("MapPage");
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void onLocationChanged(Location arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		// TODO Auto-generated method stub
 
 	}
 }
