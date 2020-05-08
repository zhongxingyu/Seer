 package se.chalmers.project14.main;
 
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.content.Context;
 import android.view.Menu;
 
 public class CTHmaps extends MapActivity {
	private LocationManager locManager;
	private LocationListener locListener;
	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_map);
         //Enabling zooming
         MapView mapView = (MapView) findViewById(R.id.mapview);
         mapView.setBuiltInZoomControls(true);
         
         /*Using the LocationManager class to obtain GPS-location*/
        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locListener = new MyLocationListener(this);
         locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListener);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
	@Override
	public void onBackPressed(){
		super.onBackPressed();
		locManager.removeUpdates(locListener);
		//locListener = null;
	}
 }
