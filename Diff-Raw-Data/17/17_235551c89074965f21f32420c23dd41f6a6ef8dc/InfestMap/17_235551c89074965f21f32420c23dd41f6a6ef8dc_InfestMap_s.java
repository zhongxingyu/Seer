 package com.fbrs.zombieoutbreak.pages;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.os.Bundle;
 
 import com.fbrs.zombieoutbreak.R;
 import com.fbrs.zombieoutbreak.pages.map.ZombieOverlay;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 
 public class InfestMap extends MapActivity{
 	private MapView mapView;
 	
 	public static Activity thisactivity;
 	
 	   /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	thisactivity = this;
         super.onCreate(savedInstanceState);
         setContentView(R.layout.infestmap);
         StaticContent.setupButtons(this,1);
         
         mapView = (MapView) findViewById(R.id.mapview);
         //mapView.setBuiltInZoomControls(true);
        // mapView.setClickable(false);
       // mapView.setFocusable(false);
        mapView.getController().zoomToSpan(160000000, 160000000);
       // mapView.getController().setCenter(new GeoPoint(0,-1));
         //mapView.getController().setZoom(1);
         //mapView.getController().
         
         ZombieOverlay overlay = new ZombieOverlay();
         List<Overlay> list = mapView.getOverlays();
 
         list.add(overlay);
     }
 
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 }
