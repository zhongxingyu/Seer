 package nl.sense.demo;
 
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 
 import java.util.List;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 
public class Gmaps extends MapActivity{	 
 	 
 	 @Override
	 public void onCreate(final Bundle savedInstanceState) {
	     super.onCreate(savedInstanceState);
	     setContentView(R.layout.activity_main);
	     final MapView mapView = (MapView) findViewById(R.id.mapview);
	     mapView.setBuiltInZoomControls(true);
	 }	;

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
 	}
 
 }
