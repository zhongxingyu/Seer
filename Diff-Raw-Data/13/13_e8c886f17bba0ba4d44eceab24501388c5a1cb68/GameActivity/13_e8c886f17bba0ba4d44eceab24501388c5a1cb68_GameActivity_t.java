 package de.frizzware.pacrun;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 
 import android.app.Activity;
 import android.location.Location;
 import android.os.Bundle;
 
 public class GameActivity extends MapActivity implements LocationService.LocationHandler{
 	LocationService mLocationService;
 	MapView mMap;
 	
 
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        setContentView(R.layout.game);
         
         mLocationService = new LocationService(this, this);
         mLocationService.start();
         
         // Displaying Zooming controls
         mMap = (MapView) findViewById(R.id.mapview);
         mMap.setBuiltInZoomControls(true);
         mMap.setStreetView(true); // Street View
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 
 	public void onLocationChanged(Location location) {
 		Location l = mLocationService.getCurrentLocation();
         GeoPoint point = new GeoPoint((int)(l.getLatitude()*1E6), (int)(l.getLongitude()*1E6));
         mMap.getController().animateTo(point);
        mMap.getController().setZoom(19);
 	}
 }
