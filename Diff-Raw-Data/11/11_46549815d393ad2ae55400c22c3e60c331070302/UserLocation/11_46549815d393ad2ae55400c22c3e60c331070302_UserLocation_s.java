 package se.chalmers.krogkollen.map;
 
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import com.google.android.gms.maps.model.LatLng;
 import se.chalmers.krogkollen.utils.IObservable;
 import se.chalmers.krogkollen.utils.IObserver;
 import se.chalmers.krogkollen.utils.IObserver.Status;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /*
  * This file is part of Krogkollen.
  *
  * Krogkollen is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Krogkollen is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Krogkollen.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /**
  * A class handling the phones current position.
  */
 public class UserLocation implements LocationListener, IObservable {
 
 	private static UserLocation	instance			= null;
 
 	private List<IObserver>		observers			= new ArrayList<IObserver>();
 
 	private Location			currentLocation		= null;
 	private LocationManager		locationManager;
 	private String				netLocationProvider	= LocationManager.NETWORK_PROVIDER;
 	private String				gpsLocationProvider	= LocationManager.GPS_PROVIDER;
 
 	private boolean				netState;
 	private boolean				gpsState;
 
 	// Reading interval, used to determine which reading is better.
 	private static final int	TWO_MINUTES			= 1000 * 60 * 2;
 
 	/**
 	 * Instantiates a user location object.
 	 * 
 	 * @param locationManager location manager from an activity.
 	 */
 	private UserLocation(LocationManager locationManager) {
 		this.locationManager = locationManager;
 
 		Location netLocation = this.locationManager.getLastKnownLocation(this.netLocationProvider);
 		Location gpsLocation = this.locationManager.getLastKnownLocation(this.gpsLocationProvider);
 
 		// Initiate first position of the user if either gps or net providers are enabled.
 		if (netLocation != null && gpsLocation != null) {
 			if (this.isBetterLocation(netLocation, gpsLocation)) {
 				this.currentLocation = netLocation;
 			} else {
 				this.currentLocation = gpsLocation;
 			}
 		} else if (gpsLocation != null) {
 			this.currentLocation = gpsLocation;
 		} else if (netLocation != null) {
 			this.currentLocation = netLocation;
 		}
 	}
 
 	/**
 	 * Initiates a new UserLocation object with given LocationManager.
 	 * 
 	 * @param locationManager LocationManager from an activity
 	 */
 	public static void init(LocationManager locationManager) {
 		if (instance == null) {
 			instance = new UserLocation(locationManager);
 		}
 	}
 
 	/**
 	 * Returns an instance of this class if it's been initialized.
 	 * 
 	 * @return an instance of UserLocation
 	 */
 	public static UserLocation getInstance() {
 		if (instance == null) {
 			throw new IllegalStateException("The user location hasn't been initialized!");
 		}
 		return instance;
 	}
 
 	/**
 	 * Get the current user location of the phone in latitude and longitude.
 	 * 
 	 * @return current user location.
 	 */
 	public LatLng getCurrentLatLng() {
 		if (this.currentLocation != null) {
 			return new LatLng(this.currentLocation.getLatitude(),
 					this.currentLocation.getLongitude());
 		}
 		return null;
 	}
 
 	/**
 	 * Start tracking the users location.
 	 */
 	public void startTrackingUser() {
 		this.locationManager.requestLocationUpdates(this.netLocationProvider, 0, 0, this);
 		this.locationManager.requestLocationUpdates(this.gpsLocationProvider, 0, 0, this);
 	}
 
 	@Override
 	public void onLocationChanged(Location location) {
 		Status status = Status.NORMAL_UPDATE;
 		if (this.currentLocation == null) {
 			this.currentLocation = location;
 			status = Status.FIRST_LOCATION;
 		} else if (isBetterLocation(location, this.currentLocation)) {
 			this.currentLocation = location;
 		}
 
 		for (IObserver observer : observers) {
 			observer.update(status);
 		}
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) {
 		Status status = Status.NORMAL_UPDATE;
 		if (!this.locationManager.isProviderEnabled(this.netLocationProvider)
 				&& !this.locationManager.isProviderEnabled(this.gpsLocationProvider)) {
 			status = Status.ALL_DISABLED;
 		} else if (!this.locationManager.isProviderEnabled(this.netLocationProvider)) {
 			status = Status.NET_DISABLED;
 		} else if (!this.locationManager.isProviderEnabled(this.gpsLocationProvider)) {
 			status = Status.GPS_DISABLED;
 		}
 
 		for (IObserver observer : observers) {
 			observer.update(status);
 		}
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 		Location lastKnownLocation = this.locationManager.getLastKnownLocation(provider);
 		if (this.currentLocation == null && lastKnownLocation != null) {
 			this.currentLocation = lastKnownLocation;
 			for (IObserver observer : observers) {
 				observer.update(Status.FIRST_LOCATION);
 			}
 		} else if (this.currentLocation != null && lastKnownLocation != null) {
 			if (isBetterLocation(lastKnownLocation, this.currentLocation)) {
 				this.currentLocation = lastKnownLocation;
 				for (IObserver observer : observers) {
 					observer.update(Status.NORMAL_UPDATE);
 				}
 			}
 		}
 		this.locationManager.requestLocationUpdates(provider, 0, 0, this);
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 	}
 
 	/**
 	 * "Saves" the state of the location providers when this class is paused. Should be called when
 	 * corresponding activity is paused.
 	 */
 	public void onPause() {
 		this.netState = this.locationManager.isProviderEnabled(this.netLocationProvider);
 		this.gpsState = this.locationManager.isProviderEnabled(this.gpsLocationProvider);
 		this.locationManager.removeUpdates(this);
 	}
 
 	/**
 	 * Compares the state of the location providers from what was saved in onPause, to what their
 	 * state is now and calls the appropriate methods. Should be called when corresponding activity
 	 * is resumed.
 	 */
 	public void onResume() {
 		boolean newNetState = this.locationManager.isProviderEnabled(this.netLocationProvider);
 		boolean newGpsState = this.locationManager.isProviderEnabled(this.gpsLocationProvider);
 		if (!netState && newNetState) {
 			this.onProviderEnabled(this.netLocationProvider);
 		} else if (netState && !newNetState) {
 			this.onProviderDisabled(this.netLocationProvider);
 		}
 		if (!gpsState && newGpsState) {
 			this.onProviderEnabled(this.gpsLocationProvider);
 		} else if (gpsState && !newGpsState) {
 			this.onProviderDisabled(this.gpsLocationProvider);
 		}
 		this.startTrackingUser();
 	}
 
 	/**
 	 * ** Method written by Google, found in Android training examples for locations. **
 	 * 
 	 * Determines whether one Location reading is better than the current Location fix.
 	 * 
 	 * @param location The new Location that you want to evaluate
 	 * @param currentBestLocation The current Location fix, to which you want to compare the new one
	 */ //  TODO why is this protected?
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
 		if (currentBestLocation == null) {
 			// A new location is always better than no location
 			return true;
 		}
 
 		// Check whether the new location fix is newer or older
 		long timeDelta = location.getTime() - currentBestLocation.getTime();
 		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
 		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
 		boolean isNewer = timeDelta > 0;
 
 		// If it's been more than two minutes since the current location, use the new location
 		// because the user has likely moved
 		if (isSignificantlyNewer) {
 			return true;
 			// If the new location is more than two minutes older, it must be worse
 		} else if (isSignificantlyOlder) {
 			return false;
 		}
 
 		// Check whether the new location fix is more or less accurate
 		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
 		boolean isLessAccurate = accuracyDelta > 0;
 		boolean isMoreAccurate = accuracyDelta < 0;
 		boolean isSignificantlyLessAccurate = accuracyDelta > 200;
 
 		// Check if the old and new location are from the same provider
 		boolean isFromSameProvider = isSameProvider(location.getProvider(),
 				currentBestLocation.getProvider());
 
 		// Determine location quality using a combination of timeliness and accuracy
 		if (isMoreAccurate) {
 			return true;
 		} else if (isNewer && !isLessAccurate) {
 			return true;
 		} else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
 			return true;
 		}
 		return false;
 	}
 
 	// Checks whether two providers are the same
 	private boolean isSameProvider(String provider1, String provider2) {
 		if (provider1 == null) {
 			return provider2 == null;
 		}
 		return provider1.equals(provider2);
 	}
 
 	/**
 	 * Get the current provider status. If both providers are enabled the method will return
 	 * NORMAL_UPDATE.
 	 * 
 	 * @return provider status.
 	 */
 	public Status getProviderStatus() {
 		boolean netEnabled = this.locationManager.isProviderEnabled(this.netLocationProvider);
 		boolean gpsEnabled = this.locationManager.isProviderEnabled(this.gpsLocationProvider);
 
 		if (!(netEnabled || gpsEnabled)) {
 			return Status.ALL_DISABLED;
 		} else if (!netEnabled) {
 			return Status.NET_DISABLED;
 		} else if (!gpsEnabled) {
 			return Status.GPS_DISABLED;
 		}
 		return Status.NORMAL_UPDATE;
 	}
 
 	@Override
 	public void addObserver(IObserver observer) {
 		this.observers.add(observer);
 		if (this.currentLocation != null) {
 			observer.update(Status.FIRST_LOCATION);
 		}
 	}
 
 	@Override
 	public void removeObserver(IObserver observer) {
 		this.observers.remove(observer);
 	}
 }
