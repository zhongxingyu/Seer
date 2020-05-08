 /* Added by Franzi Roesner, 2013 */
 
 package com.android.systemui.acg;
 
 import com.android.systemui.R;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.util.Log;
 import android.view.View;
 import android.widget.FrameLayout;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 
 public class LocationAcg extends Activity implements LocationListener {
 	
 	public static String TAG = "LocationAcg";
 	
 	private FrameLayout contentView;
 	private ImageButton locationButton;
 	private LocationManager locationManager;
 	private String bestProvider;
 	
 	private ILocationAcgContainer containerInterface;
 	
 	@Override
     public void onStart() {
 		super.onStart();
 		
 		// Get client interface
 		Intent i = getIntent();
 		IBinder b = i.getIBinderExtra("parentBinder");
 		containerInterface = ILocationAcgContainer.Stub.asInterface(b);
 		
 		// Get the location manager
 		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
 		findBestProviderAndRegister();
 		
 		// Set up FrameLayout containing a simple location button
 		// (real location ACG UI for future work...)
 		contentView = new FrameLayout(this);
 		locationButton = new ImageButton(this);
 		locationButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
 		locationButton.setImageResource(R.drawable.location);
 		//locationButton.setText("Get Location");
 		locationButton.setOnClickListener(new View.OnClickListener() {
 	        public void onClick(View view) {
 	            // On button click, actually access location and return it to embedder
 	        	Location location = locationManager.getLastKnownLocation(bestProvider);
 				try {
 		    		containerInterface.locationAvailable(location);
 				} catch (RemoteException e) {
 					Log.e(TAG, "RemoteException trying locationAvailable: " + e.getMessage());
 				}
 	        }
 	    });
 		contentView.addView(locationButton);
 		
 		setContentView(contentView);
 	}
 	
 	/** Register for the updates when Activity is in foreground */
 	@Override
 	protected void onResume() {
 		super.onResume();
		findBestProviderAndRegister();
		//locationManager.requestLocationUpdates(bestProvider, 0, 0, this);
 	}
 
 	/** Stop the updates when Activity is paused */
 	@Override
 	protected void onPause() {
 		super.onPause();
 		locationManager.removeUpdates(this);
 	}
 
 	@Override
 	public void onLocationChanged(Location location) {
 		// Don't need to do anything, querying lastKnownLocation on button click
 		// (but need a listener for lastKnownLocation to be valid)
		if (location != null)
			Log.i(TAG, "Location changed!");
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		// Should something be done here?
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 		findBestProviderAndRegister();
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) {
 		findBestProviderAndRegister();
 	}
 	
 	private void findBestProviderAndRegister() {
 		// Unregister
 		locationManager.removeUpdates(this);
 		
 		// Find new (maybe same) best provider
 		bestProvider = locationManager.getBestProvider(new Criteria(), false);
 		
 		// (Re)register
		locationManager.requestLocationUpdates(bestProvider, 0, 0, this);

		Log.i(TAG, "Registered for location updates from " + bestProvider);
 	}	
 
 }
