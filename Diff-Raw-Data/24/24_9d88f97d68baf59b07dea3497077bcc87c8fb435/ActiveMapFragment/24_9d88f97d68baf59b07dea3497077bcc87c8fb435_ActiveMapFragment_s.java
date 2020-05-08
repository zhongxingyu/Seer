 package com.nurun.activemtl.ui.fragment;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import android.location.Location;
 import android.os.Bundle;
 
 import com.google.android.gms.location.LocationClient;
 import com.google.android.gms.maps.CameraUpdate;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
 import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.BitmapDescriptor;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.nurun.activemtl.ActiveMtlApplication;
 import com.nurun.activemtl.R;
 import com.nurun.activemtl.callback.GetEventsRequestCallbacks;
 import com.nurun.activemtl.controller.EventController;
 import com.nurun.activemtl.controller.GeofencingController;
 import com.nurun.activemtl.model.EventList;
 import com.nurun.activemtl.model.EventType;
 import com.nurun.activemtl.model.parse.Event;
 import com.nurun.activemtl.util.NavigationUtil;
 
 public class ActiveMapFragment extends SupportMapFragment {
 
     private boolean mapInitialized = false;
     private Map<Marker, Event> eventByMarker = new HashMap<Marker, Event>();
     private EventController eventController;
 
     private GeofencingController geofencingController;
     protected EventList events;
     private LocationClient locationClient;
 
     public static ActiveMapFragment newFragment() {
         return new ActiveMapFragment();
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
         eventController = (EventController) getActivity().getApplicationContext().getSystemService(ActiveMtlApplication.EVENT_CONTROLLER);
         geofencingController = new GeofencingController(getActivity().getApplicationContext());
         locationClient = (LocationClient) getActivity().getApplicationContext().getSystemService(ActiveMtlApplication.LOCATION_CLIENT);
     }
 
     @Override
     public void onStart() {
         super.onStart();
         setUpMap();
     }
 
     @Override
     public void onStop() {
         super.onStop();
         eventController.canceltasks();
     }
 
     private void setUpMap() {
         // Set listeners for marker events. See the bottom of this class for their behavior.
         if (locationClient.isConnected()) {
             Location location = locationClient.getLastLocation();
             if (location != null) {
                 double latitude = location.getLatitude();
                 double longitude = location.getLongitude();
                 getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 10));
             }
         }
         getMap().setOnMyLocationChangeListener(onMyLocationChangeListener);
         getMap().setOnInfoWindowClickListener(onInfoWindowClickListener);
         getMap().setMyLocationEnabled(true);
     }
 
     private OnInfoWindowClickListener onInfoWindowClickListener = new OnInfoWindowClickListener() {
         @Override
         public void onInfoWindowClick(Marker marker) {
             Event event = eventByMarker.get(marker);
             NavigationUtil.goToDetail(getActivity(), event.getEventId());
         }
     };
 
     protected GetEventsRequestCallbacks getEventsRequestCallbacks = new GetEventsRequestCallbacks() {
 
         @Override
         public void onGetEventsRequestFailed(RuntimeException runtimeException) {
         }
 
         @Override
         public void onGetEventsRequestComplete(EventList eventList) {
             events = eventList;
             if (getMap() == null) {
                 return;
             }
             for (Event event : eventList) {
                MarkerOptions marker = new MarkerOptions();
                marker.position(new LatLng(event.getLatLng()[0], event.getLatLng()[1]));
                marker.draggable(false);
                marker.title(event.getTitle());
                marker.icon(getIcon(event.getEventType()));
                 try {
                     eventByMarker.put(getMap().addMarker(marker), event);
                } catch (NullPointerException npe) {
                    if (getMap() == null) {
                        throw new RuntimeException("map is null");
                    } else if (event == null) {
                        throw new RuntimeException("event is null");
                    } else if (marker == null) {
                        throw new RuntimeException("marker is null");
                    }
                 }
             }
             geofencingController.addGeofences(events);
         }
     };
 
     private OnMyLocationChangeListener onMyLocationChangeListener = new OnMyLocationChangeListener() {
         @Override
         public void onMyLocationChange(Location location) {
             if (!mapInitialized && getMap() != null) {
                 double latitude = location.getLatitude();
                 double longitude = location.getLongitude();
                 CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 10);
                 getMap().animateCamera(cameraUpdate);
                 mapInitialized = true;
                 eventController.findClosestEvents(getEventsRequestCallbacks, latitude, longitude);
             }
         }
     };
 
     private BitmapDescriptor getIcon(EventType eventType) {
         switch (eventType) {
         case Alert:
             return BitmapDescriptorFactory.fromResource(R.drawable.probleme_marker);
         case Challenge:
             return BitmapDescriptorFactory.fromResource(R.drawable.defi_marker);
         case Idea:
             return BitmapDescriptorFactory.fromResource(R.drawable.idee_marker);
         }
         throw new IllegalStateException("Mauvais Event type : " + eventType);
     }
 
 }
