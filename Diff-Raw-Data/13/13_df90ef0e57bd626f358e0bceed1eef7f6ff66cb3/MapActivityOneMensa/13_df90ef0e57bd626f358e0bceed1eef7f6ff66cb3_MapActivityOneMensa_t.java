 package com.ese2013.mensaunibe.map;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import android.annotation.SuppressLint;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v7.app.ActionBar;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewTreeObserver.OnGlobalLayoutListener;
 import android.widget.Toast;
 
 import com.ese2013.mensaunibe.R;
 import com.ese2013.mensaunibe.R.id;
 import com.ese2013.mensaunibe.R.layout;
 import com.ese2013.mensaunibe.R.menu;
 import com.ese2013.mensaunibe.R.string;
 import com.ese2013.mensaunibe.model.Model;
 import com.ese2013.mensaunibe.model.mensa.Mensa;
 import com.ese2013.mensaunibe.model.utils.AppUtils;
 import com.ese2013.mensaunibe.model.api.LanguageChanger;
 import com.ese2013.mensaunibe.model.data.ForceReloadTask;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMapOptions;
 import com.google.android.gms.maps.MapFragment;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.CameraPosition;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.LatLngBounds;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.gms.maps.model.Polyline;
 import com.google.android.gms.maps.model.PolylineOptions;
 import com.memetix.mst.language.Language;
 
 
 import android.support.v7.app.ActionBarActivity;
 
 /**
  * @author group17
  * @author Marc Dojtschinov
  */
 
 public class MapActivityOneMensa extends BaseMapActivity  {
 		private Mensa mensa;
 		private GoogleMap map;
 		private MyLocation mLocation;
 		private LatLng mLocationLatLng;
 		private boolean locationReady = false;
 		private Marker mensaMarker;
 		private int mMensaId;
 		private String  travelMode = "walking";// default
 	
 	 
 
 	@Override
 	  	protected void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    setContentView(R.layout.fragment_map);
 	    //Setup action bar
 	    ActionBar actionBar = getSupportActionBar();
 		actionBar.setDisplayHomeAsUpEnabled(true);
 		initilizeMap();
 		
 		}
 	 
 	
 	@Override
 	  public boolean onCreateOptionsMenu(Menu menu) {
 		  getMenuInflater().inflate(R.menu.actionbar_map, menu);
 	    return super.onCreateOptionsMenu(menu);
 	  }
 
 
 	public void handleGetDirectionsResult(ArrayList<LatLng> directionPoints) { 
 		
 		PolylineOptions rectLine = new PolylineOptions().width(3).color(Color.RED);		 
 		    for(int i = 0 ; i < directionPoints.size() ; i++) 
 		    {          
 		        rectLine.add(directionPoints.get(i));
 		    }
		    map.addPolyline(rectLine);	
 	}
 
 
 	@Override
 	public void findDirections(double fromPositionDoubleLat, double fromPositionDoubleLong, double toPositionDoubleLat, double toPositionDoubleLong, String mode) {
 		Map<String, String> map = new HashMap<String, String>();
 	    map.put(GetDirectionsAsyncTask.USER_CURRENT_LAT, String.valueOf(fromPositionDoubleLat));
 	    map.put(GetDirectionsAsyncTask.USER_CURRENT_LONG, String.valueOf(fromPositionDoubleLong));
 	    map.put(GetDirectionsAsyncTask.DESTINATION_LAT, String.valueOf(toPositionDoubleLat));
 	    map.put(GetDirectionsAsyncTask.DESTINATION_LONG, String.valueOf(toPositionDoubleLong));
 	    map.put(GetDirectionsAsyncTask.DIRECTIONS_MODE, mode);
 
 	    GetDirectionsAsyncTask asyncTask = new GetDirectionsAsyncTask(this);
 	    asyncTask.execute(map); 
 		
 	}
 	 
 	@Override
 	public void initilizeMap() {
 	        if (map == null) {
 	        	map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
 	            // check if map is created successfully or not
 	            if (map != null) {
 	            	setUpMap();
 	            }
 	        }
 	    }
 	
 	public void setUpMap(){
 		if(mLocation == null){
 			mLocation = MyLocation.getInstance();
 			mLocation.setActivity(this);
 		}
 		mMensaId = getIntent().getIntExtra("int_value",0);
 		mensa = Model.getInstance().getMensaById(mMensaId);
 		//canteen name as action bar title
 		setTitle(mensa.getName());
 		mLocationLatLng =new LatLng(mLocation.getLocation().getLatitude(),mLocation.getLocation().getLongitude());
 		
 		final View mapView = getSupportFragmentManager().findFragmentById(R.id.map).getView();
         if (mapView.getViewTreeObserver().isAlive()) {
             mapView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                 @Override
                 public void onGlobalLayout() {
                     map.moveCamera( CameraUpdateFactory.newLatLngZoom(new LatLng(mLocationLatLng.latitude, mLocationLatLng.longitude),14.0f) );
                     mensaMarker = map.addMarker(new MarkerOptions().position(new LatLng(mensa.getLat(),mensa.getLon()))
             				.title(mensa.getName())); 
                 	findDirections(mLocationLatLng.latitude, mLocationLatLng.longitude,
                                mensaMarker.getPosition().latitude, mensaMarker.getPosition().longitude, travelMode );
                 }
             });}
 		
 	}
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch(item.getItemId()) {
 			case R.id.action_travel_mode:				
 				if(travelMode.contentEquals("walking")) {
 					travelMode ="driveing";
 					map.clear();
 					mensaMarker = map.addMarker(new MarkerOptions().position(new LatLng(mensa.getLat(),mensa.getLon()))
             				.title(mensa.getName()));
 					findDirections(mLocationLatLng.latitude, mLocationLatLng.longitude,
                             mensaMarker.getPosition().latitude, mensaMarker.getPosition().longitude, travelMode );
 					item.setTitle(getString(R.string.action_travel_mode_walking));
 					
 					}
 				else {
 					travelMode ="walking";
 					map.clear();
 					mensaMarker = map.addMarker(new MarkerOptions().position(new LatLng(mensa.getLat(),mensa.getLon()))
             				.title(mensa.getName()));
 					findDirections(mLocationLatLng.latitude, mLocationLatLng.longitude,
                             mensaMarker.getPosition().latitude, mensaMarker.getPosition().longitude, travelMode );
 					
 					item.setTitle(getString(R.string.action_travel_mode_driveing));
 					}
 				
 				return true;
 			default:
 		           return super.onOptionsItemSelected(item);
 	    	}
 	}
	
 	@Override
 	public void onResume() {
         super.onResume();
         mLocation.callOnResume();
 	}
 	@Override
 	public void onStop() {
 		super.onStop();
 		mLocation.callOnStop();
 		
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		mLocation.callOnPause();
 		
 	}
 	@Override
 	public void onStart() {
 		super.onStart();
 		mLocation.callOnStart();
 	}
 	
 	public void locationReady(boolean b){
 		if(b){
 			locationReady = true;
 		}else locationReady = false;
 	}
 	
 
 	
 
 	
 }
