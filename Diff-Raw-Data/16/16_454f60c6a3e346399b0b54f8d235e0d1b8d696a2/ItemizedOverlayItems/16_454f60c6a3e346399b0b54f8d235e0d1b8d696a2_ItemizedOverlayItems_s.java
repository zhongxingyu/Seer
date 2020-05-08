 package com.uoc.loadsensing.items;
 
 import java.util.ArrayList;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 
 //import com.google.android.maps.GeoPoint;
 //import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 import com.google.android.maps.OverlayItem;
 import com.uoc.loadsensing.BalloonItemizedOverlay;
 import com.uoc.loadsensing.SingleNetworkActivity;
 
 public class ItemizedOverlayItems extends BalloonItemizedOverlay<OverlayItem> {
 	private ArrayList<OverlayItem> m_overlays = new ArrayList<OverlayItem>();
 	private Context c;
 		
 	public ItemizedOverlayItems(Drawable defaultMarker, MapView mapView) {
 		super(boundCenter(defaultMarker), mapView);
 		c = mapView.getContext();
 		System.out.println("Inicializado el itemizedOverlayItems");
 	}
 
 
 	public void addOverlay(OverlayItem overlay) {
 	    m_overlays.add(overlay);
 	    populate();
 	}
 
 	@Override
 	protected OverlayItem createItem(int i) {
 		return m_overlays.get(i);
 	}
 
 	@Override
 	public int size() {
 		return m_overlays.size();
 	}
 	
 	public void Tap(int index){
 		tap_tap(index);
 	}
	/*
 	@Override
 	protected boolean onBalloonTap(int index) {
 		//GeoPoint point = m_overlays.get(index).getPoint();
 		//double lat = point.getLatitudeE6() / 1.0E6;
 		//double lon = point.getLongitudeE6() / 1.0E6;
 		//http://developer.android.com/guide/appendix/g-app-intents.html
 		Intent intent = new Intent(c, SingleNetworkActivity.class);
 		intent.putExtra("current_network", index);
 		c.startActivity(intent);
 		//c.startActivity(intent);
 		return true;
	}*/
 }
