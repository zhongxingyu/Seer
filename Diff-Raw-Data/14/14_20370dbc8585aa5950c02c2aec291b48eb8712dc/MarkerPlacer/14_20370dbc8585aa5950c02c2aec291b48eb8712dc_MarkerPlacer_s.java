 package com.gatech.spark.helper;
 
 import com.gatech.spark.R;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 /**
  * Class to make customized markers
  */
 public class MarkerPlacer {
 
     public static final String WHATS_HOT_SNIPPET = "whats_hot_spippet";
 
 	public static Marker addWhatsHotMarker(GoogleMap map, LatLng position) {
 		MarkerOptions markerOptions = new MarkerOptions()
 			.position(position)
 			.title("this is hot hot hot!")
             .snippet(WHATS_HOT_SNIPPET)
 			.draggable(false)
 			.anchor(0.5f, 0.5f)
 			.icon(BitmapDescriptorFactory.fromResource(R.drawable.whats_hot));
 		return map.addMarker(markerOptions);
 	}
 
 	public static Marker addCurrentLocationMarker(GoogleMap map, LatLng position) {
 		MarkerOptions markerOptions = new MarkerOptions()
 			.position(position)
 			.title("Current Location")
 			.snippet("a snippet")
 			.draggable(false);
 		return map.addMarker(markerOptions);
 	}
 
 	public static Marker addDraggableMarker(GoogleMap map, LatLng position) {
 		return map.addMarker(new MarkerOptions().position(position).draggable(true));
 	}
 }
