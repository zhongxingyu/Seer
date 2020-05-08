 package us.inneract;
 
 import android.os.Bundle;
import android.widget.LinearLayout;
 
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 
 public class inneract extends MapActivity {
	LinearLayout linearLayout;
	MapView mapView;

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
 		return false;
 	}
 }
