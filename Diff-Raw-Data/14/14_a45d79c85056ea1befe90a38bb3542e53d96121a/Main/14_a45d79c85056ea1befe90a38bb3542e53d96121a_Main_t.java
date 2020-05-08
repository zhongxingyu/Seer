 package my.andromap;
 
 import android.content.Context;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 
 public class Main extends MapActivity implements LocationListener {
 	
 	public LocationManager manager;
 	public MapView mapView;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
         mapView = (MapView)findViewById(R.id.mapview);
         
         // add zoom support (buttons)
         mapView.setBuiltInZoomControls(true);
        // show traffic
         mapView.setTraffic(true);
         mapView.preLoad();
         
        try {
        	manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, this);
        } catch (Exception e){}
     }
     
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 
 	@Override
 	public void onLocationChanged(Location loc) {
 //		manager.removeUpdates(this);
 		GeoPoint point = new GeoPoint(
				(int)(loc.getLatitude() * 1000000), 
				(int)(loc.getLongitude() * 1000000));
 		
 		mapView.getController().animateTo(point);
 	}
 
 
 	@Override
 	public void onProviderDisabled(String arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	@Override
 	public void onProviderEnabled(String arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	@Override
 	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	protected void onDestroy() {
 		this.manager.removeUpdates(this);
 		super.onDestroy();
 	}
 }
