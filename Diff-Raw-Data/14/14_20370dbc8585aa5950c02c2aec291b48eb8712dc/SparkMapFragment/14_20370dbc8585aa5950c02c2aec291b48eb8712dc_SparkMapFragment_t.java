 package com.gatech.spark.fragment;
 
 import java.util.ArrayList;
 
 import android.app.Dialog;
 import android.app.Fragment;
 import android.content.Context;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.gatech.spark.R;
 import com.gatech.spark.adapter.GenericArrayAdapter;
 import com.gatech.spark.helper.*;
 import com.gatech.spark.model.Place;
 import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.MapView;
 import com.google.android.gms.maps.MapsInitializer;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.loopj.android.http.AsyncHttpResponseHandler;
 
 public class SparkMapFragment extends Fragment {
 
 	private static final String TAG = "SparkMapFragment";
 	protected static final LatLng GT = new LatLng(33.78102, -84.400363);
 	protected static final LatLng SoNo = new LatLng(33.769872,-84.384527);
 	private MapView mapView;
 	private LatLng currentLoc;
 	private LocationManager locationManager;
 	private LocationListener locationListener;
 	private boolean locationHasBeenSet = false;
 	private boolean whatsHotIsShowing = false;
 	private Button whatsHotButton;
 	private Iterable<HotSpot> hotSpotList;
 
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 	                         Bundle savedInstanceState) {
 		super.onCreateView(inflater, container, savedInstanceState);
 		View rootView = inflater.inflate(R.layout.fragment_spark_map, container, false);
 		
 		initMapFeatures();
 		mapView = (MapView) rootView.findViewById(R.id.sparkMapView);
 		mapView.onCreate(savedInstanceState);
 		setupClickListeners();
 		setupLocationListener();
 		
 		whatsHotButton = (Button) rootView.findViewById(R.id.whatsHotButton);
 		hideWhatsHot();
         whatsHotButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	toggleWhatsHot();
             }
         });
         
 		return rootView;
 	}
 
 	/**
 	 * Initialize certain maps features, e.g., CameraUpdateFactory. See also
 	 * https://developers.google.com/maps/documentation/android/reference/com/google/android/gms/maps/MapsInitializer
 	 * http://stackoverflow.com/a/13824917
 	 */
     private void initMapFeatures() {
         try {
             MapsInitializer.initialize(getActivity());
         } catch (GooglePlayServicesNotAvailableException e) {
             Log.d(TAG, "Google play services not available");
             throw(new RuntimeException("Unable to initialize map", e));
         }
     }
 	
 	private MapView getMapView() {
 		return mapView;
 	}
 	
 	private GoogleMap getMap() {
 		return getMapView().getMap();
 	}
 
 	public void setupClickListeners() {
 		final GoogleMap map = getMap();
 		map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
 
 			@Override
 			public void onMapLongClick(LatLng pos) {
 				MarkerPlacer.addDraggableMarker(map, pos);
 			}
 		});
 
         map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
             @Override
             public boolean onMarkerClick(Marker marker) {
                if(MarkerPlacer.isWhatsHotMarker(marker))
                 {
                     HttpRestClient.getPlaces(marker.getPosition().latitude, marker.getPosition().longitude, 500, new AsyncHttpResponseHandler(){
                         @Override
                         public void onSuccess(String s) {
                             SaxParser parser = new SaxParser();
                             HandlerReturnObject<ArrayList<Place>> handlerObject = parser.parsePlacesXmlResponse(s);
                             if (handlerObject.isValid())
                             {
                                 showListDialog(handlerObject.getObject());
                             }
                             else
                             {
                                 CommonHelper.showLongToast(getActivity(), "Failed to find locations.");
                             }
                         }
 
                         @Override
                         public void onFailure(Throwable throwable, String s) {
                             super.onFailure(throwable, s);
                         }
                     });
                     //return true to suppress showing the default dialog.
                     return true;
                 }
                 //return false to show the default behavior on the marker press.
                 return false;
             }
         });
 
 		map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
 
 			@Override
 			public void onMarkerDragStart(Marker arg0) {
 			}
 
 			@Override
 			public void onMarkerDragEnd(Marker marker) {
 				LatLng latlng = marker.getPosition();
 				Toast.makeText(SparkMapFragment.this.getActivity(),
 				               "new position is " + latlng, Toast.LENGTH_SHORT).show();
 			}
 
 			@Override
 			public void onMarkerDrag(Marker arg0) {
 			}
 		});
 	}
 
 	private void setupLocationListener() {
 		// Acquire a reference to the system Location Manager
 		locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
 
 		// Define a listener that responds to location updates
 		locationListener = new LocationListener() {
 			@Override
 			public void onLocationChanged(Location location) {
 				// Called when a new location is found by the network location provider.
 				Log.d(TAG, "Recieved new location: " + location.toString());
 				updateCurrentLocation(location);
 				setLocationViewIfNeeded();
 			}
 
 			@Override
 			public void onStatusChanged(String provider, int status, Bundle extras) {}
 
 			@Override
 			public void onProviderEnabled(String provider) {}
 
 			@Override
 			public void onProviderDisabled(String provider) {}
 		};
 	}
 
 	private void updateCurrentLocation(Location loc) {
 		currentLoc = new LatLng(loc.getLatitude(), loc.getLongitude());
 		Log.d(TAG, "New current location: " + currentLoc.toString());
 		setLocationViewIfNeeded();
 	}
 
 	private void setLocationViewIfNeeded() {
 		GoogleMap map = getMap();
 		if (map != null && !locationHasBeenSet) {
 			Log.d(TAG, "Updating current location view");
 			setLocationView(currentLoc);
 			MarkerPlacer.addCurrentLocationMarker(map, currentLoc);
 			locationHasBeenSet = true;
 		}
 	}
 
 	private void setLocationView(LatLng loc) {
 		getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 12));
 	}
 	
 	private void showWhatsHot() {
 		hotSpotList = getHotSpots();
 		for (HotSpot spot : hotSpotList) {
 			spot.addMarker(getMap());
 		}
 		whatsHotIsShowing = true;
 		whatsHotButton.setText("Hide What's Hot");
 	}
 	
 	private void hideWhatsHot() {
 		if (hotSpotList != null) {
 			for (HotSpot spot : hotSpotList) {
 				spot.clearMarker();
 			}
 			hotSpotList = null;
 		}
 		whatsHotIsShowing = false;
 		whatsHotButton.setText("Show What's Hot");
 	}
 	
 	private void toggleWhatsHot() {
 		if (whatsHotIsShowing) {
 			hideWhatsHot();
 		} else {
 			showWhatsHot();
 		}
 	}
 	
 	private Iterable<HotSpot> getHotSpots() {
 		ArrayList<HotSpot> hotSpotList = new ArrayList<HotSpot>();
 		
 		// TODO: get this from the server
 		LatLng[] locList = {GT, SoNo};
 		for (LatLng loc : locList) {
 			hotSpotList.add(new HotSpot(loc));
 		}
 		return hotSpotList;
 	}
 	
 	// Must forward lifecycle methods to MapView object. See
 	// https://developers.google.com/maps/documentation/android/reference/com/google/android/gms/maps/MapView
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	}
 	
 	@Override
 	public void onResume() {
 		Log.d(TAG, "...resuming");
 		super.onResume();
 		// Register the listener with the Location Manager to receive location updates
 		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
 		getMapView().onResume();
 	}
 	
 	@Override
 	public void onPause() {
 		Log.d(TAG, "pausing...");
 		super.onPause();
 		getMapView().onPause();
 		locationManager.removeUpdates(locationListener);
 	}
 	
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		getMapView().onDestroy();
 	}
 	
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		getMapView().onSaveInstanceState(outState);
 	}
 	
 	@Override
 	public void onLowMemory() {
 		super.onLowMemory();
 		getMapView().onLowMemory();
 	}
 
 
     public void showListDialog(ArrayList<Place> places)
     {
         Dialog dialog = new Dialog(getActivity());
         LayoutInflater inflater = getActivity().getLayoutInflater();
         View v = inflater.inflate(R.layout.places_list_layout, null, false);
         GenericArrayAdapter<Place> adapter = new GenericArrayAdapter<Place>(getActivity(),places);
         ListView list = (ListView)v.findViewById(R.id.placeList);
         list.setAdapter(adapter);
         dialog.setContentView(v);
         dialog.show();
 
     }
 
 }
