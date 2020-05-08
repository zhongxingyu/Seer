 package pl.warsjawa.android2.ui.map;
 
 import android.util.Log;
 import android.util.Pair;
 
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.model.BitmapDescriptor;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.squareup.otto.Subscribe;
 
 import javax.inject.Inject;
 
 import pl.warsjawa.android2.event.EventBus;
 import pl.warsjawa.android2.model.gmapsapi.GmapsModel;
 import pl.warsjawa.android2.model.gmapsapi.nearby.NearbyPlace;
 import pl.warsjawa.android2.model.gmapsapi.nearby.NearbyPlacesList;
 import pl.warsjawa.android2.model.meetup.Event;
 import pl.warsjawa.android2.model.meetup.EventList;
 import pl.warsjawa.android2.model.meetup.MeetupModel;
 
 public class MyEventsDisplayer {
 
     private GoogleMap map;
     @Inject
     MeetupModel model;
     @Inject
     EventBus bus;
     @Inject
     GmapsModel gmapsModel;
 
     public void setUpMap(final GoogleMap map) {
         this.map = map;
         this.map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
             @Override
             public void onInfoWindowClick(Marker marker) {
                 Log.i("tag", "marker = " + marker.getPosition());
                 NearbyPlacesList nearbyPlacesList = gmapsModel.getNearbyPlacesList(marker.getPosition());
                 if (nearbyPlacesList == null) {
                     gmapsModel.requestNearbyPlacesList(marker.getPosition());
                 }
                 else {
                     displayNearbyPlaces(marker.getPosition());
                 }
             }
         });
         displayMyEvents();
     }
 
     public void registerForMyEventsUpdate() {
         bus.register(this);
     }
 
     public void unregisterFromMyEventsUpdate() {
         bus.unregister(this);
     }
 
     @Subscribe
     public void onMyEventsUpdate(EventList myEventList) {
         displayMyEvents();
     }
 
     @Subscribe
     public void onNearbyPlacesUpdate(Pair<LatLng,NearbyPlacesList> places) {
         displayNearbyPlaces(places.first);
     }
 
     private void displayMyEvents() {
         if (map != null) {
             EventList myEvents = model.getEventList();
             if (myEvents != null) {
                 for (Event event : myEvents.getResults()) {
                     map.addMarker(new MarkerOptions().title(event.getName()).snippet(event.getGroup().getName()).position(event.getVenue().getLatLng()));
                 }
             }
         }
     }
 
     private void displayNearbyPlaces(LatLng position) {
         BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
         NearbyPlacesList nearbyPlacesList = gmapsModel.getNearbyPlacesList(position);
         if (nearbyPlacesList != null) {
             for (NearbyPlace place : nearbyPlacesList.getResults()) {
                 map.addMarker(new MarkerOptions().position(place.getLocation()).title(place.getName()).icon(icon));
             }
         }
     }
 }
