 package org.yaoha;
 
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
 import org.osmdroid.views.MapView;
 
 import android.app.Activity;
 import android.os.Bundle;
 
 public class YaohaMapActivity extends Activity {
 	MapView mapview;
	MapController mapController;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    setContentView(R.layout.mapview);
 	    mapview = (MapView) findViewById(R.id.mapview);
 	    mapview.setBuiltInZoomControls(true);
 	    mapview.setMultiTouchControls(true);
	    
	    mapController = this.mapview.getController();
        mapController.setZoom(15);
        GeoPoint myPosition = new GeoPoint(53554070, -2959520);  // versteh mal jemand diese Angaben >.> (http://code.google.com/p/osmdroid/source/browse/trunk/osmdroid-android/src/org/osmdroid/util/GeoPoint.java?r=955)
        mapController.setCenter(myPosition);
 	}
 
 }
