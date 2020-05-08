 package com.thoughtworks.blipit;
 
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
import com.thoughtworks.blipit.api.TransportLayer;
 import com.thoughtworks.blipit.overlays.BlipItOverlay;
 
 import java.util.List;
 
 public class BlipItActivity extends MapActivity {
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         MapView mapView = (MapView) this.findViewById(R.id.mapview);
         mapView.setBuiltInZoomControls(true);
         Drawable drawable = this.getResources().getDrawable(R.drawable.marker);
         BlipItOverlay blipItOverlay = new BlipItOverlay(drawable, mapView);
         addBlip(blipItOverlay, mapView.getController());
         List<Overlay> mapOverlays = mapView.getOverlays();
         mapOverlays.add(blipItOverlay);
     }
 
     private void addBlip(BlipItOverlay blipItOverlay, MapController mapController) {
         String title = this.getResources().getString(R.string.blip_title);
         String snippet = this.getResources().getString(R.string.blip_snippet);
         GeoPoint geoPoint = new GeoPoint(19240000, -99120000);
         blipItOverlay.addBlip(new OverlayItem(geoPoint, title, snippet));
         mapController.animateTo(geoPoint);
     }
 
     @Override
     protected boolean isRouteDisplayed() {
         return false;
     }
 }
