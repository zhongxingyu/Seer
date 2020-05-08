 package com.davelabs.wakemehome;
 
 import java.util.List;
 
 import android.content.Context;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 
 import com.google.android.gms.maps.model.LatLng;
 
 public class CurrentPositionTracker implements LocationListener {
 	
 	public interface CurrentPositionListener {
 		public void onCurrentPositionChanged(LatLng newPosition);
 	}
 
 	private CurrentPositionListener _listener;
 	private Location _currentLocation;
 	private LocationManager _lm;
 	private Context _c;
 	
 	public CurrentPositionTracker(Context c, CurrentPositionListener listener) {
 		_c = c;
 		_listener = listener;
 		_lm = (LocationManager) c.getSystemService(c.LOCATION_SERVICE);
 	}
 	
 	@Override	public void onProviderDisabled(String provider) {}
 	@Override	public void onProviderEnabled(String provider) {}
 	@Override	public void onStatusChanged(String provider, int status, Bundle extras) {}
 	
 	@Override
 	public void onLocationChanged(Location newLocation) {		
 		if (_currentLocation == null) {
 			betterLocationFound(newLocation);
 		} else {
 			if (isAtLeastAsAccurate(newLocation)) {
 				betterLocationFound(newLocation);
 			} else if (doesntIntersectWithCurrentPosition(newLocation)) {
 				betterLocationFound(newLocation);
 			}
 		}
 	}
 	
 	public void track()	{
 		Criteria criteria = getLocationCriteria();
 		_lm.requestLocationUpdates(1000, 0, criteria, this, _c.getMainLooper());
 		useBestLastKnownLocation();
 	}
 
 	private Criteria getLocationCriteria() {
 		Criteria criteria = new Criteria();
 		criteria.setAccuracy(Criteria.ACCURACY_FINE);
 		return criteria;
 	}
 
 	private void useBestLastKnownLocation() {
 		Criteria criteria = getLocationCriteria();
 		List<String> providers =  _lm.getProviders(criteria, true);
 		Location bestLocation = null;
 		for (String provider : providers) {
 			Location lastKnownLocation = _lm.getLastKnownLocation(provider);
 			if (lastKnownLocation != null) {
				if (lastKnownLocation.getTime() < 60000) {
 					if (bestLocation == null || lastKnownLocation.getAccuracy() < bestLocation.getAccuracy()) {
 						bestLocation = lastKnownLocation;
 					}
 				}
 			}
 		}
 		if (bestLocation != null) {
 			onLocationChanged(bestLocation);			
 		}
 	}
 	
 	private void betterLocationFound(Location location) {
 		_currentLocation = location;
 		
 		LatLng newLatLng = new LatLng(location.getLatitude(), location.getLongitude());
 		_listener.onCurrentPositionChanged(newLatLng);
 	}
 
 	private boolean isAtLeastAsAccurate(Location newLocation) {
 		return (newLocation.getAccuracy() <= _currentLocation.getAccuracy());
 	}
 	
 	private boolean doesntIntersectWithCurrentPosition(Location newLocation) {
 		float pointDelta = _currentLocation.distanceTo(newLocation);
 		double radiusSum = newLocation.getAccuracy() + _currentLocation.getAccuracy();
 		
 		return (pointDelta > radiusSum);
 	}
 
 }
