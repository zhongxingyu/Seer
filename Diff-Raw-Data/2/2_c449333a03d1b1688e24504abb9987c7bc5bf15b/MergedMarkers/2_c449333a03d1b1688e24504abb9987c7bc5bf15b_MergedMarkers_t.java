 package de.hsanhalt.inf.studiappkoethen.activities.classes;
 
 import java.util.List;
 
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 import de.hsanhalt.inf.studiappkoethen.R;
 
 public class MergedMarkers
 {
 
     //private static MergedMarkers INSTANCE;
 	private Marker mergedMarker;
 	private List<Marker> MarkerList;
 	
 	
 	public MergedMarkers(Marker mergableMarker1, Marker mergableMarker2) {
 		this.MarkerList.add(mergableMarker1);
 		this.MarkerList.add(mergableMarker2);
 		mergedMarker = mergableMarker1;
 		mergeMarkers();
 	}
 	
 	/*
 	public static MergedMarkers getInstance(Marker mergableMarker1, Marker mergableMarker2)
     {
         if (INSTANCE == null)
         {
             INSTANCE = new MergedMarkers(mergableMarker1, mergableMarker2);
         }
         return INSTANCE;
     }/**/
 	
 	public Marker getMarker() {
 		return this.mergedMarker;
 	}
 	
 	public Marker addMarkerToMergedMarker(Marker addedMarker) {
 		if(!MarkerInList(addedMarker)) {
 			MarkerList.add(addedMarker);
 			mergeMarkers();
 		}
 		return mergedMarker;
 	}
 	
 	private void mergeMarkers() {
 		Marker merged = mergedMarker;
 		merged.setTitle("Sammelmarker");
		merged.setSnippet("Bitte reinzoomen fuer detailierte Ansicht.");
 		
 		double addedLat = 0.0;
 		double addedLng = 0.0;
 		for(int i = 0; i < MarkerList.size(); i++) {
 			addedLat += MarkerList.get(i).getPosition().latitude;
 			addedLng += MarkerList.get(i).getPosition().longitude;
 		}
 		addedLat = addedLat / MarkerList.size();
 		addedLng = addedLng / MarkerList.size();
 		LatLng mergedLatLng = new LatLng(addedLat, addedLng);
 		merged.setPosition(mergedLatLng);
 		
 		mergedMarker = merged;
 	}
 	
 	public boolean MarkerInList(Marker marker) {
 		return MarkerList.contains(marker);
 	}
 	
 	public MarkerOptions getMarkerOptions() {
 		MarkerOptions options = new MarkerOptions()
 			.position(mergedMarker.getPosition())
 			.title(mergedMarker.getTitle())
 			.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher))
 			.snippet(mergedMarker.getSnippet());
 		return options;
 	}
 }
