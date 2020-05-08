 package edu.pitt.designs1635.ParkIt;
 
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 
import edu.pitt.designs1635.ParkIt.R;
 
 import android.os.Bundle;
 
 public class ParkItActivity extends MapActivity {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         MapView mapView = (MapView) findViewById(R.id.mapview);
         mapView.setBuiltInZoomControls(true);
     }
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 }
