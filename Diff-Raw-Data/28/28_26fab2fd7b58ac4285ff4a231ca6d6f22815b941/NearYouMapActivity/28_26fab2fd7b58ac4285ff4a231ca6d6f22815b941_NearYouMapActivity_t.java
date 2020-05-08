 package com.rogoapp;
 
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesClient;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 import com.google.android.gms.location.LocationClient;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.MapView;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
 import android.view.Menu;
 import android.widget.Toast;
 
 public class NearYouMapActivity extends Activity implements
 GooglePlayServicesClient.ConnectionCallbacks,
 GooglePlayServicesClient.OnConnectionFailedListener {
 
 	private static final int GPS_ERRORDIALOG_REQUEST = 9001;
 	GoogleMap mMap;
 	MapView mMapView;
 	LocationClient mLocationClient;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		if (servicesOK()) {
 			Toast.makeText(this, "Ready to map!", Toast.LENGTH_SHORT).show();
 			setContentView(R.layout.near_you_map);
 			
 			mMapView = (MapView)findViewById(R.id.map);
 			mMapView.onCreate(savedInstanceState);
 			// new stuff
 			mLocationClient = new LocationClient(this, this, this);
 			mLocationClient.connect();
 		}
 		else {
 	//		setContentView(R.layout.activity_main);
 		}
 		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	public boolean servicesOK() {
 		int isAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
 		
 		if (isAvailable == ConnectionResult.SUCCESS) {
 			return true;
 		}
 		else if (GooglePlayServicesUtil.isUserRecoverableError(isAvailable)) {
 			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(isAvailable, this, GPS_ERRORDIALOG_REQUEST);
 			dialog.show();
 		}
 		else {
 			Toast.makeText(this, "Can't connect to Google Play services", Toast.LENGTH_SHORT).show();
 		}
 		return false;
 	}
 	
 	
 
 	@Override
 	protected void onDestroy() {
 		// TODO Auto-generated method stub
 		super.onDestroy();
 		mMapView.onDestroy();
 	}
 
 	@Override
 	public void onLowMemory() {
 		// TODO Auto-generated method stub
 		super.onLowMemory();
 		mMapView.onLowMemory();
 	}
 
 	@Override
 	protected void onPause() {
 		// TODO Auto-generated method stub
 		super.onPause();
 		mMapView.onPause();
 	}
 
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 		mMapView.onResume();
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		// TODO Auto-generated method stub
 		super.onSaveInstanceState(outState);
 		mMapView.onSaveInstanceState(outState);
 	}
 
 	@Override
 	public void onConnectionFailed(ConnectionResult arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onConnected(Bundle arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onDisconnected() {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
