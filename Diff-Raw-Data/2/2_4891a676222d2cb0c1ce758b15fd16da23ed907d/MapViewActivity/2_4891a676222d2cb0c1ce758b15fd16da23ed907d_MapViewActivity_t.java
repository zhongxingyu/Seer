 package com.example.yahoohackday;
 
 
 import android.app.Activity;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 import java.util.List;
 
 
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 import com.readystatesoftware.maps.OnSingleTapListener;
 import com.readystatesoftware.maps.TapControlledMapView;
 
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.view.MotionEvent;
 
 public class MapViewActivity extends MapActivity{
 	
 	TapControlledMapView mapView; // use the custom TapControlledMapView
 	List<Overlay> mapOverlays;
 	Drawable drawable;
 	Drawable drawable2;
 	SimpleItemizedOverlay itemizedOverlay;
 	SimpleItemizedOverlay itemizedOverlay2;
     
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_map_view);
         
         mapView = (TapControlledMapView) findViewById(R.id.gmap);
 		mapView.setBuiltInZoomControls(true);
 		
 		// dismiss balloon upon single tap of MapView (iOS behavior) 
 		mapView.setOnSingleTapListener(new OnSingleTapListener() {		
 			@Override
 			public boolean onSingleTap(MotionEvent e) {
 				itemizedOverlay.hideAllBalloons();
 				return true;
 			}
 		});
 		
 		mapOverlays = mapView.getOverlays();
 		
 		// first overlay
 		drawable = getResources().getDrawable(R.drawable.ic_launcher);
 		itemizedOverlay = new SimpleItemizedOverlay(drawable, mapView);
 		// set iOS behavior attributes for overlay
 		itemizedOverlay.setShowClose(false);
 		itemizedOverlay.setShowDisclosure(true);
 		itemizedOverlay.setSnapToCenter(true);
 		
 		GeoPoint point = new GeoPoint((int)(51.5174723*1E6),(int)(-0.0899537*1E6));
 		OverlayItem overlayItem = new OverlayItem(point, "Tomorrow Never Dies (1997)", 
 				"(M gives Bond his mission in Daimler car)");
 		itemizedOverlay.addOverlay(overlayItem);
 		
 		GeoPoint point2 = new GeoPoint((int)(51.515259*1E6),(int)(-0.086623*1E6));
 		OverlayItem overlayItem2 = new OverlayItem(point2, "GoldenEye (1995)", 
 				"(Interiors Russian defence ministry council chambers in St Petersburg)");		
 		itemizedOverlay.addOverlay(overlayItem2);
 		
 		mapOverlays.add(itemizedOverlay);
 		
 //		// second overlay
 //		drawable2 = getResources().getDrawable(R.drawable.ic_action_search);
 //		itemizedOverlay2 = new SimpleItemizedOverlay(drawable2, mapView);
 //		// set iOS behavior attributes for overlay
 //		itemizedOverlay2.setShowClose(false);
 //		itemizedOverlay2.setShowDisclosure(true);
 //		itemizedOverlay2.setSnapToCenter(false);
 //		
 //		GeoPoint point3 = new GeoPoint((int)(51.513329*1E6),(int)(-0.08896*1E6));
 //		OverlayItem overlayItem3 = new OverlayItem(point3, "Sliding Doors (1998)", null);
 //		itemizedOverlay2.addOverlay(overlayItem3);
 //		
 //		GeoPoint point4 = new GeoPoint((int)(51.51738*1E6),(int)(-0.08186*1E6));
 //		OverlayItem overlayItem4 = new OverlayItem(point4, "Mission: Impossible (1996)", 
 //				"(Ethan & Jim cafe meeting)");
 //		itemizedOverlay2.addOverlay(overlayItem4);
 //		
 //		mapOverlays.add(itemizedOverlay2);
 //		
 //		if (savedInstanceState == null) {
 //			
 //			final MapController mc = mapView.getController();
 //			mc.animateTo(point2);
 //			mc.setZoom(16);
 //			
 //		} else {
 //			
 //			// example restoring focused state of overlays
 //			int focused;
 //			focused = savedInstanceState.getInt("focused_1", -1);
 //			if (focused >= 0) {
 //				itemizedOverlay.setFocus(itemizedOverlay.getItem(focused));
 //			}
 //			focused = savedInstanceState.getInt("focused_2", -1);
 //			if (focused >= 0) {
 //				itemizedOverlay2.setFocus(itemizedOverlay2.getItem(focused));
 //			}
 //			
 //		} 

 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 //	@Override
 //	protected void onSaveInstanceState(Bundle outState) {
 //		
 //		// example saving focused state of overlays
 //		if (itemizedOverlay.getFocus() != null) outState.putInt("focused_1", itemizedOverlay.getLastFocusedIndex());
 //		if (itemizedOverlay2.getFocus() != null) outState.putInt("focused_2", itemizedOverlay2.getLastFocusedIndex());
 //		super.onSaveInstanceState(outState);
 //	
 //	}
 	
 	/*
 	public class MapViewActivity extends Activity {
 		private Button button;
 		@Override
 	    public void onCreate(Bundle savedInstanceState) {
 	        super.onCreate(savedInstanceState);
 	        setContentView(R.layout.activity_main);
 	        findViews();
 		}
 		
 		public void findViews() {
 			button = (Button) findViewById(R.id.button1);
 			
 			button.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View view) {
 					Intent intent = new Intent();
 					intent.putExtra("location_name", "南港展覽館1");
 					intent.setClass(MapViewActivity.this, NewsListActivity.class);
 		            startActivity(intent);
 				}
 			});
 	*/
 }
