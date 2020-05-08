 package api.festival;
 
 import android.os.Bundle;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 
 public class EventMap extends MapActivity{
 	
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    setContentView(R.layout.map);
 	    MapView mapView = (MapView) findViewById(R.id.mapview);
 	    mapView.setBuiltInZoomControls(true);
 	    
	    String longitude = (String) getIntent().getSerializableExtra("longitude");
	    String latitude = (String) getIntent().getSerializableExtra("latitude");
	    GeoPoint point = new GeoPoint(Integer.parseInt(longitude), Integer.parseInt(latitude));
 	    
 	}
 	
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 }
