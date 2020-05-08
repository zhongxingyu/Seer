 package com.nagazuka.mobile.android.goedkooptanken.service.impl;
 
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationManager;
 import android.util.Log;
 
 import com.nagazuka.mobile.android.goedkooptanken.exception.GoedkoopTankenException;
 import com.nagazuka.mobile.android.goedkooptanken.exception.LocationException;
 import com.nagazuka.mobile.android.goedkooptanken.service.LocationService;
 
 public class AndroidLocationService implements LocationService {
 
 	private static final String TAG = null;
 
 	@Override
 	public Location getCurrentLocation(LocationManager locationManager) throws GoedkoopTankenException {
 
 		Location location = null;
 
 		try {
 			Criteria criteria = new Criteria();
 			criteria.setAccuracy(Criteria.ACCURACY_FINE);
 			String provider = locationManager.getBestProvider(criteria, true);
 			Log.d(TAG, "<< bestProvider: " + provider + ">>");
 
 			// Could be that location services are not enabled or not
 			// available
 			// on device
 			if (provider != null) {
 				location = locationManager.getLastKnownLocation(provider);
 			} else {
 				throw new Exception("Could not get location provider");
 			}
 		} catch (Exception e) {
 			Log.e(TAG,"<< Error find current location with Android Location Service >>");
 			e.printStackTrace();
			throw new LocationException("Uw huidige locatie kan niet automatisch bepaald worden",e);
 		}
 
 		return location;
 	}
 
 }
