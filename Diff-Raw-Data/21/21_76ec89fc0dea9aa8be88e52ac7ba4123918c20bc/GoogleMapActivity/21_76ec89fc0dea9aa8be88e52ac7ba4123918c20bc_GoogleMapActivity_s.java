 package com.ncms.ncms;
 
 import java.util.List;
 
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.MapFragment;
 //import com.google.android.maps.MyLocationOverlay;
 //import com.google.android.maps.Overlay;
 
 import android.app.Activity;
 import android.os.Bundle;
 //import android.view.Menu;
 //import android.view.MotionEvent;
 
 public class GoogleMapActivity extends Activity {
 
	GoogleMap map;
 	long start,stop;
 	//MyLocationOverlay compass;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_map);
		
 		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
 		        .getMap();
 		
 //		Touch t = new Touch();
 //		List<Overlay> overlayList = map.getOverlays();
 //		overlayList.add(t);
 	}
 
 //	@Override
 //	public boolean onCreateOptionsMenu(Menu menu) {
 //		// Inflate the menu; this adds items to the action bar if it is present.
 //		getMenuInflater().inflate(R.menu.map, menu);
 //		return true;
 //	}
 	
 //	class Touch extends Overlay{
 //		public boolean onTouchEvent(MotionEvent e, MapView m){
 //			return false;
 //		}
 //	}
 
 }
