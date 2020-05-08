 package com.mobileappdevelopersclub.shellp.ui;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
 import com.google.android.gms.maps.CameraUpdate;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.LatLngBounds;
 import com.mobileappdevelopersclub.shellp.Globals;
 import com.mobileappdevelopersclub.shellp.R;
 
 public class MapFragment extends Fragment {
 	
 	static String TAG = "MapFragment";
 	int testInt;
 	public static GoogleMap mMap;
 	View view;
 	
 	public static MapFragment newInstance(int testInt) {
 		MapFragment fragment = new MapFragment();
 		fragment.testInt = testInt;
 		return fragment;
 	}
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		super.onCreateView(inflater, container, savedInstanceState);
 		view = inflater.inflate(R.layout.map_fragment,  container, false);
 		//((TextView)view.findViewById(R.id.textView1)).setText("Dylan is awesome, this is the new triv frag and it swipes really cool");
 		return view;
 	}
 	
 	
 	
 	@Override
 	public void onStart() {
 		// TODO Auto-generated method stub
 		super.onStart();
 		setUpMapIfNeeded();
 	}
 
 	private void setUpMapIfNeeded() {
 		// Do a null check to confirm that we have not already instantiated the map.
 
 		Log.d(TAG, "Starting setUpMapIfNeeded");
 		//Markers not implemented yet
 //		if(markers == null) {
 //			markers = new ArrayList<Marker>();
 //		} else {
 //			markers.clear();
 //		}
 
 
 		if (mMap == null) {
 			mMap = ((SupportMapFragment) Globals.mgr.findFragmentById(R.id.map)).getMap();
 //			setUpMap();
 		} else {
 			mMap = ((SupportMapFragment) Globals.mgr.findFragmentById(R.id.map)).getMap();
 			mMap.clear();
 //			setUpMap();
 		}
 
 	}
 	
 	private void setUpMap() {
 		Log.d(TAG, "Setting up Map");
 			//Code to set up a custom info window
 //			Globals.mMap.setInfoWindowAdapter(new InfoWindowAdapter() {
 //
 //				// Use default InfoWindow frame
 //				@Override
 //				public View getInfoWindow(Marker arg0) {
 //					return null;
 //				}
 //
 //				// Defines the contents of the InfoWindow
 //				@Override
 //				public View getInfoContents(Marker arg0) {
 //
 //					// Getting view from the layout file info_window_layout
 //					View v = currentInflater.inflate(R.layout.windowlayout, null);
 //
 //					// Getting reference to the TextView to set latitude
 //					TextView title = (TextView) v.findViewById(R.id.title);
 //
 //					// Setting the latitude
 //					title.setText(arg0.getTitle());
 //
 //					// Returning the view containing InfoWindow contents
 //					return v;
 //
 //				}
 //			});
 //			}
 //		}
 
 		CameraUpdate center = null;
 		center=
 				CameraUpdateFactory.newLatLngBounds(new LatLngBounds( new LatLng(38.9806638 , -76.958377 ),
 						new LatLng(38.998875, -76.933100)), 200 );
 
 		
 		CameraUpdate zoom = CameraUpdateFactory.zoomTo(12);
 		
 
 		if (center != null) {
 			mMap.moveCamera(center);
 		}
 
 		mMap.animateCamera(zoom);
 
 	}
 
 }
