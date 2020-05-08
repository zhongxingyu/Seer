 package net.kevxu.senselib;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.kevxu.senselib.StepDetector.StepListener;
 
 import android.content.Context;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 
 public class LocationService implements LocationListener, StepListener {
 
 	private static final String TAG = "LocationService";
 
 	private Context mContext;
 	private LocationManager mLocationManager;
 	private List<LocationServiceListener> mLocationServiceListeners;
 
 	private StepDetector mStepDetector;
 
 	public interface LocationServiceListener {
 
 		// StepDetector Forwarder
 		public void onStep();
 
 	}
 
 	public LocationService(Context context) throws SensorNotAvailableException {
 		this(context, null);
 	}
 
 	public LocationService(Context context, LocationServiceListener locationServiceListener) throws SensorNotAvailableException {
 		mContext = context;
 		mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
 
 		mLocationServiceListeners = new ArrayList<LocationServiceListener>();
 
 		if (locationServiceListener != null) {
 			mLocationServiceListeners.add(locationServiceListener);
 		}
 
 		mStepDetector = new StepDetector(mContext, this);
 	}
 
 	/**
	 * Call this when resume.
 	 */
	public void start() {
		mStepDetector.start();
 
 	}
 
 	/**
	 * Call this when pause.
 	 */
	public void stop() {
		mStepDetector.stop();
 
 	}
 
 	public void addListener(LocationServiceListener locationServiceListener) {
 		if (locationServiceListener != null) {
 			mLocationServiceListeners.add(locationServiceListener);
 		} else {
 			throw new NullPointerException("LocationServiceListener is null.");
 		}
 	}
 
 	public void removeListeners() {
 		mLocationServiceListeners.clear();
 	}
 
 	@Override
 	public void onLocationChanged(Location location) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onStep() {
 		synchronized (this) {
 			for (LocationServiceListener locationServiceListener : mLocationServiceListeners) {
 				locationServiceListener.onStep();
 			}
 		}
 	}
 
 }
