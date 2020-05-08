 package org.yaoha;
 
 import org.osmdroid.views.MapView;
 
 import android.app.Activity;
 import android.os.Bundle;
 
 public class YaohaMapActivity extends Activity {
 	MapView mapview;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    setContentView(R.layout.mapview);
 	    mapview = (MapView) findViewById(R.id.mapview);
 	    mapview.setBuiltInZoomControls(true);
 	    mapview.setMultiTouchControls(true);
	    mapview.getController().setZoom(50);	    
 	}
 
 }
