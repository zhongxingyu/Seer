 package com.robodex.app;
 
 import java.util.List;
 
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 import com.robodex.data.DummyData;
 import com.robodex.data.DummyData.DummyLocation;
 import com.robodex.R;
 
 public class MyMapActivity extends MapActivity {
     private static final int DEFAULT_ZOOM = 14;
     private MapView mMap;
     private List<Overlay> mOverlays;
     private Drawable mMarker;
     private MyItemizedOverlay mItemizedOverlay;
 
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_map);
 
         mMap = (MapView) findViewById(R.id.mapview);
         mMap.setBuiltInZoomControls(true);
 
         mMap.getController().setCenter(new GeoPoint(
                 (int) (DummyData.LOCATIONS[0].LATITUDE * 1E6),
                 (int) (DummyData.LOCATIONS[0].LONGITUDE * 1E6)));
         mMap.getController().setZoom(DEFAULT_ZOOM);
 
         mOverlays = mMap.getOverlays();
        mMarker = this.getResources().getDrawable(R.drawable.marker_self);
         mItemizedOverlay = new MyItemizedOverlay(mMarker, this);
 
         addLocations();
 
     }
 
     private void addLocations() {
         int index = 0;
         for (DummyLocation dl : DummyData.LOCATIONS) {
             GeoPoint point = new GeoPoint((int) (dl.LATITUDE * 1E6), (int) (dl.LONGITUDE * 1E6));
             OverlayItem overlayitem = new OverlayItem(point,"Location " + index, dl.toString());
             mItemizedOverlay.addOverlay(overlayitem);
             ++index;
         }
         mOverlays.add(mItemizedOverlay);
     }
 
     @Override
     protected boolean isRouteDisplayed() {
         // TODO Auto-generated method stub
         return false;
     }
 
 }
