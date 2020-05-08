 package com.codepath.apps.nommable.fragments;
 
 import java.util.ArrayList;
 
 import android.location.Location;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.InflateException;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
 import com.codepath.apps.nommable.R;
 import com.codepath.apps.nommable.models.Restaurant;
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
 import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
 import com.google.android.gms.location.LocationClient;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 public class MapFragment extends Fragment implements ConnectionCallbacks, OnConnectionFailedListener {
 	TextView tvRestName;
 	TextView tvRestPhone;
 	TextView tvStreetAddress;
 	TextView tvCityState;
 	GoogleMap map;
 
 	ArrayList<Restaurant> restaurants;
 	ArrayList<Marker> markers;
 	
 	private static LocationClient locationClient;
 	private static View view;
 	
 	private static final float SELECTED_MARKER_COLOR = BitmapDescriptorFactory.HUE_RED;
 	private static final float MARKER_COLOR = BitmapDescriptorFactory.HUE_AZURE;
 	
 	/**
 	 * A common pattern for creating a Fragment with arguments.
 	 * Use this for instantiating the fragment in order to set the initial restaurants.
 	 * 
 	 * @param restaurants
 	 * @return
 	 */
 	
 	public static MapFragment newInstance(ArrayList<Restaurant> restaurants) {
 		MapFragment mapFrag = new MapFragment();
 		Bundle args = new Bundle();
 		args.putSerializable("restaurants", restaurants);
 		mapFrag.setArguments(args);
 		return mapFrag;
 	}
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		locationClient = new LocationClient(getActivity(), this, this);
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		if (view != null) {
 			ViewGroup parent = (ViewGroup) view.getParent();
 			if (parent != null)
 				parent.removeView(view);
 		}
 		try {
 			view = inflater.inflate(R.layout.fragment_map, container, false);
 		} catch (InflateException e) {
 			/* map is already there, just return view as it is */
 		}
 		Log.d("DEBUG", "onCreateView in MapFragment");
 		return view;
 	}
 	
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		setup();
 	}
 	
 	private void setup() {
 		tvRestName = (TextView) getActivity().findViewById(R.id.tvRestName);
 		tvRestPhone = (TextView) getActivity().findViewById(R.id.tvRestPhone);
 		tvStreetAddress = (TextView) getActivity().findViewById(R.id.tvStreetAddress);
 		tvCityState = (TextView) getActivity().findViewById(R.id.tvCityState);
 		
 		map = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
 		map.setMyLocationEnabled(true);
 		updateRestaurants((ArrayList<Restaurant>) getArguments().getSerializable("restaurants"));
 	}
 	
 	/**
 	 * Wipe all currently displayed restaurants and update it with new restaurants.
 	 * Use this after getting a new response from FourSquare.
 	 * 
 	 * @param restaurants the new ArrayList of restaurants
 	 */
 	
 	public void updateRestaurants(ArrayList<Restaurant> restaurants) {
 		if (restaurants == null || restaurants.size() == 0){
 			Log.e("ERROR", "attempted to update restaurants with empty or null arraylist");
 			return;
 		}
 		
 		this.restaurants = restaurants;
 		
 		map.clear();
 		
 		// set our initial "selected" restaurant
 		updateRestarauntText(restaurants.get(0));
 		addRestaurantToMap(restaurants.get(0), SELECTED_MARKER_COLOR);	
 		
 		for (int i = 1; i < restaurants.size(); i++){
 			addRestaurantToMap(restaurants.get(i), MARKER_COLOR);
 		}
 	}
 	
 	/**
 	 * Add a single restaurant to the map.
 	 * 
 	 * @param restaurant
 	 */
 	
 	private void addRestaurantToMap(Restaurant rest, float color) {
 				
 		map.addMarker(new MarkerOptions()
 			.position(new LatLng(rest.getLatitude(), rest.getLongitude()))
 			.icon(BitmapDescriptorFactory.defaultMarker(color))
 		);
 		
 	}
 	
 	/**
 	 * Update TextViews with chosen restaurant's information.
 	 * 
 	 * @param restaurant
 	 */
 	
 	private void updateRestarauntText(Restaurant rest) {
 		tvRestName.setText(rest.getName());
 		tvRestPhone.setText(rest.getDisplayPhone());
 		tvStreetAddress.setText(rest.getAddress());
 		tvCityState.setText(rest.getCity() + ", " + rest.getState());
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		
         locationClient.connect();
 	}
 	
 	@Override
 	public void onPause() {
 		super.onPause();
 		
        locationClient.connect();
 	}
 	
 	@Override
 	public void onConnectionFailed(ConnectionResult result) {
 	}
 
 	@Override
 	public void onConnected(Bundle connectionHint) {
 		Location curLocation = locationClient.getLastLocation();
 		LatLng curLatLng = new LatLng(curLocation.getLatitude(), curLocation.getLongitude());		
 		map.animateCamera(CameraUpdateFactory.newLatLngZoom(curLatLng, 13.5f));
 	}
 
 	@Override
 	public void onDisconnected() {
 	}
 	
 }
