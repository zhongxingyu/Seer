 package com.idamobile.map.google;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.idamobile.map.BalloonOverlayExtension;
 import com.idamobile.map.ItemizedOverlayBase;
 import com.idamobile.map.MyLocationOverlayBase;
 import com.idamobile.map.OverlayBase;
 import com.idamobile.map.ShadowOverlayExtension;
 
 class OverlayManager implements Iterable<OverlayBase> {
 
     private List<OverlayBase> overlays = new ArrayList<OverlayBase>();
     private MapView mapView;
     private MapViewWrapper mapViewWrapper;
 
     private Map<OverlayBase, OverlayAdapter> adoptOverlays = new HashMap<OverlayBase, OverlayAdapter>();
 
     public OverlayManager(MapViewWrapper mapViewWrapper) {
         this.mapViewWrapper = mapViewWrapper;
         this.mapView = mapViewWrapper.getView();
     }
 
     public int getOverlayCount() {
         return overlays.size();
     }
 
     public OverlayBase getOverlay(int index) {
         return overlays.get(index);
     }
 
     @SuppressWarnings({ "rawtypes", "unchecked" })
     public void addOverlay(OverlayBase overlay) {
         if (!overlays.contains(overlay)) {
             OverlayAdapter adapter = null;
             if (overlay instanceof ItemizedOverlayBase) {
                 if (overlay instanceof BalloonOverlayExtension) {
                     adapter = new BalloonOverlayAdapter(mapViewWrapper,
                             (ItemizedOverlayBase) overlay);
                 } else {
                     adapter = new ItemizedOverlayAdapter(mapViewWrapper,
                             (ItemizedOverlayBase) overlay);
                 }
             } else {
                 throw new IllegalArgumentException("Unable to process overlay class " + overlay.getClass()
                         + ". Supports only instances of " + ItemizedOverlayBase.class);
             }
 
             if (adapter != null) {
                 Overlay resultOverlay = adapter.getResultOverlay();
                 applyShadowExtension(overlay, resultOverlay);
                 mapView.getOverlays().add(resultOverlay);
                 adoptOverlays.put(overlay, adapter);
                 overlays.add(overlay);
             }
         }
     }
 
     private void applyShadowExtension(OverlayBase overlay, Overlay resultOverlay) {
         if (overlay instanceof ShadowOverlayExtension) {
             if (resultOverlay instanceof ItemListOverlay) {
                 ((ItemListOverlay) resultOverlay).setIgnoreShadow(
                        !((ShadowOverlayExtension) overlay).isShadowEnabled());
             }
         }
     }
 
     public boolean removeOverlay(OverlayBase overlay) {
         if (overlays.remove(overlay)) {
             OverlayAdapter adapter = adoptOverlays.get(overlay);
             Overlay adoptOverlay = adapter.getResultOverlay();
             mapView.getOverlays().remove(adoptOverlay);
             adoptOverlays.remove(overlay);
             adapter.release();
             return true;
         } else {
             return false;
         }
     }
 
     public void addMyLocationOverlay() {
         if (getMyLocationOverlay() == null) {
             MyLocationOverlayAdapter adapter = new MyLocationOverlayAdapter(mapViewWrapper);
             overlays.add(adapter);
             mapView.getOverlays().add(adapter.getResultOverlay());
             adoptOverlays.put(adapter, adapter);
         }
     }
 
     public MyLocationOverlayBase getMyLocationOverlay() {
         for (OverlayBase overlayBase : overlays) {
             if (overlayBase instanceof MyLocationOverlayBase) {
                 return (MyLocationOverlayBase) overlayBase;
             }
         }
         return null;
     }
 
     public void removeAllOverlays() {
         List<OverlayBase> overlays = new ArrayList<OverlayBase>(this.overlays);
         for (OverlayBase overlay : overlays) {
             removeOverlay(overlay);
         }
     }
 
     @Override
     public Iterator<OverlayBase> iterator() {
         return Collections.unmodifiableList(overlays).iterator();
     }
 
     public boolean containsOverlay(OverlayBase overlay) {
         return overlays.contains(overlay);
     }
 }
