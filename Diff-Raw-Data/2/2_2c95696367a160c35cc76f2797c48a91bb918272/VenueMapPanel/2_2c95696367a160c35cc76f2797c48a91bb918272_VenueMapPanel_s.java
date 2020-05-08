 package de.flower.rmt.ui.manager.page.venues.map;
 
 import de.flower.common.util.geo.LatLng;
 import de.flower.rmt.ui.common.panel.BasePanel;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import wicket.contrib.gmap3.GMap;
 import wicket.contrib.gmap3.api.GLatLng;
 import wicket.contrib.gmap3.overlay.GMarker;
 import wicket.contrib.gmap3.overlay.GMarkerOptions;
 import wicket.contrib.gmap3.overlay.GOverlayEvent;
 import wicket.contrib.gmap3.overlay.GOverlayEventHandler;
 
 import java.io.Serializable;
 
 /**
  * The model contains the latLng of the marker to display. If null no marker is displayed.
  *
  * @author flowerrrr
  */
 public class VenueMapPanel extends BasePanel {
 
     private LatLng latLng;
 
     /**
      * @param latLng position of gMarker
      */
     public VenueMapPanel(LatLng latLng, boolean draggableMarker) {
         super();
 
         GMap map = new GMap("map");
         add(map);
         map.setDoubleClickZoomEnabled(true);
 
         if (draggableMarker) {
             // put draggable marker on map.
             DraggableMarker marker = new DraggableMarker(map, latLng);
         } else {
             Marker marker = new Marker(map, latLng);
         }
         // and center map on marker
         map.setCenter(latLng);
 
         map.setZoom(14);
     }
 
     public void setMarker(final LatLng latLng) {
         throw new UnsupportedOperationException("Feature not implemented!");
     }
 
     private class Marker implements Serializable {
 
         public Marker(GMap map, GLatLng gLatLng) {
             GMarkerOptions options = new GMarkerOptions(map, gLatLng);
             GMarker gMarker = new GMarker(options);
             map.addOverlay(gMarker);
         }
     }
 
    private class DraggableMarker  {
 
         public DraggableMarker(GMap map, GLatLng gLatLng) {
             GMarkerOptions options = new GMarkerOptions(map, gLatLng);
             options = options.draggable(true);
             final GMarker gMarker = new GMarker(options);
             map.addOverlay(gMarker);
             // add drag listener
             gMarker.addListener(GOverlayEvent.DRAGEND, new GOverlayEventHandler() {
                 @Override
                 public void onEvent(AjaxRequestTarget target) {
                     onUpdateMarker(new LatLng(gMarker.getLatLng()));
                 }
             });
         }
     }
 
     /**
      * Called when gMarker on map is set or dragged around.
      *
      * @param latLng
      */
     public void onUpdateMarker(LatLng latLng) {
         ; // empty implementation, subclasses can override
     }
 }
