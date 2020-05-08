 package tu.kom.uhg;
 
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.CameraPosition;
 import com.google.android.gms.maps.model.LatLng;
 
 import android.content.Context;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.view.Menu;
 import android.widget.Toast;
 
 public class MapActivity extends GenericActivity implements android.location.LocationListener{
 	
 	private LocationManager locationManager;
 	private String provider;
 	private GoogleMap map;
 	
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
 		map.setMyLocationEnabled(true);
 		
 		// Get the location manager
 	    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 	    // Define the criteria how to select the locatioin provider -> use
 	    // default
 	    Criteria criteria = new Criteria();
 	    provider = locationManager.getBestProvider(criteria, false);
 	    Location location = locationManager.getLastKnownLocation(provider);
 	    if (location != null) {
 	        System.out.println("Provider " + provider + " has been selected.");
 	        onLocationChanged(location);
 	    }
 	    
 	}
 	
 	/* Request updates at startup */
 	@Override
 	protected void onResume() {
 		super.onResume();
 		locationManager.requestLocationUpdates(provider, 400, 1, this);
 	}
 	
 	@Override
 	protected void onPause() {
 	  super.onPause();
 	  locationManager.removeUpdates(this);
 	}
 
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		//getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	@Override
 	public void onLocationChanged(Location location) {
 		double lat = location.getLatitude();
 		double lon = location.getLongitude();
 		LatLng target = new LatLng(lat, lon);
 		
 		map.animateCamera(CameraUpdateFactory.newLatLng(target));
 		//TODO change the default my location marker
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 	  // TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) {
 	}
 }
