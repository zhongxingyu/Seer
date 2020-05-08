 package org.mixare.map;
 
 import android.os.Bundle;
 import java.util.Set;
 
 import org.mixare.R;
 import org.mixare.lib.MixUtils;
 import org.mixare.marker.POIMarker;
 import org.mixare.MixListView;
 import org.mixare.MixView;
 import org.mixare.DataView;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationManager;
 import android.view.Menu;
 import android.view.MenuItem;
 
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
 import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
 import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
 import com.google.android.gms.maps.MapFragment;
 import com.google.android.gms.maps.UiSettings;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 
 
 public class GoogleMapV2 extends Activity implements OnMapClickListener, OnMapLongClickListener, OnMarkerClickListener{ 
 	
 	private static Context context;
 	private DataView dataView;
 
 	private UiSettings UISettings;
 
 	private GoogleMap myMap;
 	private int mapType = GoogleMap.MAP_TYPE_NORMAL;
 
 
 	protected void onCreate(Bundle savedInstanceState) {
 		  
 	    super.onCreate(savedInstanceState);
 	    setContentView(R.layout.googlemapsv2);
 	    
 	    context = this;
 	    dataView = MixView.getDataView();
 	    
 	    // getting the map
 	    MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
 	    myMap = mapFragment.getMap();	 
 	    
 	    // set map type
 	    myMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
 	    
 	    // showing location button
 	    UISettings = myMap.getUiSettings();
 	    UISettings.setMyLocationButtonEnabled(true);
 	    myMap.setMyLocationEnabled(true);
 	    
 	    // showing compass
 	    UISettings.setCompassEnabled(true);
 	    
 	    // zoom gestures enabled
 	    UISettings.setZoomGesturesEnabled(true);
 	    
 	    // scroll gestures enabled
 	    UISettings.setScrollGesturesEnabled(true);
 	    
 	    // rotate gestures enabled
 	    UISettings.setRotateGesturesEnabled(true);
 	    
 	    //click on map enabled
 	    myMap.setOnMapClickListener(this); 
 	    
 	    //click on marker enabled
 	    myMap.setOnMarkerClickListener(this);
 	   
 	    // creating the marker v2 based on marker v1
 	    int numberMarkersV1 = getMarkersCountV1();
 	    for (int i=0;i<numberMarkersV1; i++){
 	    	POIMarker testMarker = getMarkerV1(i);
 	        LatLng positionMarker = new LatLng(testMarker.getLatitude(), testMarker.getLongitude());
 	        String URL = testMarker.getURL();
 	    	Marker markerV1 = myMap.addMarker(new MarkerOptions()
     		.position(positionMarker)
    		.icon(BitmapDescriptorFactory.fromResource(R.drawable.glo))
     		.snippet(URL));
 	    } 
 	    
 	    // move the camera instantly to my location with a zoom of 10
 	    LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
 	    String provider = service.getBestProvider(new Criteria(), false);
 	    Location location = service.getLastKnownLocation(provider);
 	    myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()), 10));
 
 	  }
 	  
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         getMenuInflater().inflate(R.menu.map_menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);
 
         switch(item.getItemId()){
             case R.id.normal_map:
                 mapType = GoogleMap.MAP_TYPE_NORMAL;
                 break;
 
             case R.id.satellite_map:
             	mapType = GoogleMap.MAP_TYPE_SATELLITE;
                 break;
 
             case R.id.terrain_map:
             	mapType = GoogleMap.MAP_TYPE_TERRAIN;
                 break;
 
             case R.id.hybrid_map:
             	mapType = GoogleMap.MAP_TYPE_HYBRID;
                 break;
                 
             case R.id.listview:
             	Intent showListMarkers = new Intent(this, MixListView.class);
     			showListMarkers.setAction(Intent.ACTION_VIEW);
     			startActivity(showListMarkers);
         }
         
         myMap.setMapType(mapType);
         return true;
     }
 
 	public void onLocationChanged(Location location) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void onProviderDisabled(String provider) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void onProviderEnabled(String provider) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onMapClick(LatLng point) {
 		// TODO Auto-generated method stub
 	}
 	
 	@Override
 	public void onMapLongClick(LatLng point) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	// open webpage of clicked marker
 	public boolean onMarkerClick(Marker marker) {
 		String URL = marker.getSnippet().toString();
 		String newURL = MixUtils.parseAction(URL);
 				try {
 					dataView.getContext().getWebContentManager().loadWebPage(newURL, context);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 		return true;
 	}
 	
 	
 	
 	
 	// PRIVATE METHODS
 	
 	// get number of markers v1
 	private int getMarkersCountV1(){
 		return dataView.getDataHandler().getMarkerCount();
 	}
 	
 	// get list as set of markers v1
 	@SuppressWarnings("unused")
 	private Set<org.mixare.lib.marker.Marker> getMarkersListV1(){
 		Set<org.mixare.lib.marker.Marker> markersV1;
 		return markersV1 = dataView.getDataHandler().getMarkerList();
 	}
 	
 	// get single marker v1
 	@SuppressWarnings("deprecation")
 	private POIMarker getMarkerV1(int index){
 		@SuppressWarnings("unused")
 		POIMarker markerV1;
 		return markerV1 = (POIMarker) dataView.getDataHandler().getMarker(index);
 	}
 }
