 package com.ghostrun.overlays;
 
 import android.graphics.drawable.Drawable;
 
 import com.ghostrun.model.Dots;
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.MapView;
 import com.google.android.maps.OverlayItem;
 
 public class DotsOverlay extends ItemizedOverlay<OverlayItem>{
 
     Dots dots;
     MapView mapView;
 
     public DotsOverlay(Drawable arg0, MapView mapView, Dots dots) {
         super(boundCenterBottom(arg0));
         this.mapView = mapView;
         this.dots = dots;
     }
 
     public void refresh() {
         this.setLastFocusedIndex(-1);
         this.populate();
         this.mapView.invalidate();
     }
 
     @Override
     protected OverlayItem createItem(int i) {
        return new OverlayItem(dots.get(i), "", "");
     }
 
     @Override
     public int size() {
        return dots.remaining();
     }
 }
