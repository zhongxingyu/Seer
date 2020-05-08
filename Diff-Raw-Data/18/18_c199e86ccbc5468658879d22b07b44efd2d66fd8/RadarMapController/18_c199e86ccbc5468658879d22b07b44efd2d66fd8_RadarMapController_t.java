 package com.tabbie.android.radar;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.MapView;
 
 public class RadarMapController {
 
   private final MapView mapView;
   private final View popUp;
   private final OnMarkerClickListener markerListener;
   
   private class TabbieEventMarkerCollection extends
       ItemizedOverlay<EventMarker> {
     private final List<EventMarker> markers = new ArrayList<EventMarker>();
     private long lastClickTime = -1;
 
     public TabbieEventMarkerCollection() {
       super(null); // TODO Give a default drawable here
     }
 
     public void addOverlay(EventMarker overlay) {
       markers.add(overlay);
       populate();
     }
 
     @Override
     protected EventMarker createItem(int i) {
       return markers.get(i);
     }
 
     @Override
     public int size() {
       return markers.size();
     }
 
     protected boolean onTap(int index) {
     	
     	// TODO Launch eventdetails intents here when the drawables shit is fixed
     	
     	mapView.removeView(popUp);
       final EventMarker m = markers.get(index);
       final Event e = m.getEvent();
       final MapView.LayoutParams mapParams = new MapView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
     		  ViewGroup.LayoutParams.WRAP_CONTENT,
     		  m.getPoint(),
     		  0,
     		  -35, // TODO Probably shouldn't be hard-coded, but I don't quite know how this works anyways
     		  MapView.LayoutParams.BOTTOM_CENTER);
       ((TextView) popUp.findViewById(R.id.map_event_title)).setText(e.name);
       ((TextView) popUp.findViewById(R.id.map_event_time)).setText(e.time.makeYourTime());
      setLatLon(e.lat, e.lon);
      
       popUp.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          markerListener.onMarkerClick(e);
        }
      });
      
       mapView.addView(popUp, mapParams);
       return true;
     }
     
 
     public Drawable boundDrawable(Drawable drawable) {
       return boundCenterBottom(drawable);
     }
 
     public boolean onTouchEvent(MotionEvent event, MapView mapView) {
       if (MotionEvent.ACTION_DOWN == event.getAction()) {
         if ((System.currentTimeMillis() - lastClickTime) < 500) {
           mapView.getController().zoomIn();
         } else {
         	mapView.removeView(popUp);
         }
         lastClickTime = System.currentTimeMillis();
       }
       return super.onTouchEvent(event, mapView);
     }
   }
 
   private TabbieEventMarkerCollection markersCollection;
 
   public RadarMapController(MapView mapView, Context context) {
     this.mapView = mapView;
     this.setLatLon(40.736968, -73.989183);
     this.setZoom(14);
     this.markersCollection = new TabbieEventMarkerCollection();
     markerListener = (OnMarkerClickListener) context;
     popUp = LayoutInflater.from(context).inflate(R.layout.popup, null);
   }
 
   public void setZoom(int zoom) {
     this.mapView.getController().setZoom(zoom);
   }
 
   public void setLatLon(double lat, double lon) {
    this.mapView.getController().animateTo(
         new GeoPoint((int) (lat * 1E6), (int) (lon * 1E6)));
   }
 
   public void setLatLon(GeoPoint p) {
     if (null == p) {
       return;
     }
     this.mapView.getController().setCenter(p);
   }
 
   public void addEventMarker(Event e, Drawable markerImg) {
     EventMarker marker = new EventMarker(e);
     markerImg.setBounds(0, 0, markerImg.getIntrinsicWidth(),
         markerImg.getIntrinsicHeight());
     marker.setMarker(markersCollection.boundDrawable(markerImg));
     this.markersCollection.addOverlay(marker);
   }
 
   public ItemizedOverlay<EventMarker> getItemizedOverlay() {
     return markersCollection;
   }
   
   public interface OnMarkerClickListener {
 	  public void onMarkerClick(final Event e);
   }
 }
