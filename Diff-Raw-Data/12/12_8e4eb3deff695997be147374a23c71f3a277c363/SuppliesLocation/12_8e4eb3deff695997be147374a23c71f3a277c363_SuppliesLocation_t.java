 package com.timetogo.activity;
 
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 
 import com.timetogo.Contants;
 
 import de.akquinet.android.androlog.Log;
 
 public class SuppliesLocation {
 
   private static final int TWO_MINUTES = 2 * 60 * 1000;
 
 
   private LocationManager locationManager;
   private Location bestLocation;
   private LocationQuality q;
 
   final int desiredAccuracy = 200;
   final int maxAge = 120;
   final int acceptedAccuracy = 400;
   int counter = 0;
 
   public Location getBestLocation() {
     return bestLocation;
   }
 
   public LocationQuality getQuality() {
     return q;
   }
 
   LocationListener locationListener = new LocationListener() {
 
     public void onLocationChanged(Location location) {
       counter++;
       Log.i(Contants.TIME_TO_GO, "@@ got new location("+counter+")"+locationToString(location));
       updateBestLocation(location);
     }
 
     public void onProviderEnabled(String provider) {
     }
 
     public void onProviderDisabled(String provider) {
     }
 
     public void onStatusChanged(String provider, int status, Bundle extras) {
 
     }
   };
 
   public SuppliesLocation(LocationManager locationManager) {
     this.locationManager = locationManager;
   }
 
   public void init() {
     boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
     boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
   }
 
    void getLocation() {
 
      Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
      Log.i(Contants.TIME_TO_GO, "@@ last knownLocation is "+locationToString(lastKnownLocation));
 
      updateBestLocation(lastKnownLocation);
 
 //    if (desiredAccuracy == 0 || getLocationQuality(desiredAccuracy, acceptedAccuracy, maxAge, bestLocation) != LocationQuality.GOOD) {
       // Define a listener that responds to location updates
 
 
       locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0f, locationListener);
 //    }
 //    if (getLocationQuality(desiredAccuracy, acceptedAccuracy, maxAge, bestLocation) != LocationQuality.BAD) {
 //      Log.i(TIME_TO_GO, "@@ got location " + locationToString(desiredAccuracy, acceptedAccuracy, maxAge, bestLocation));
 //      return bestLocation;
 //    } else {
 //      Log.w(TIME_TO_GO, "SuppliesLocation failed to get a location");
 //      return null;
 //    }
   }
 
   public void stop() {
     locationManager.removeUpdates(locationListener);
     bestLocation = null;
     q = LocationQuality.BAD;
     counter = 0;
   }
 
 
   private String locationToString( Location location) {
     if (location==null) return "null";
     StringBuilder sb = new StringBuilder();
    sb.append(String.format("qual=%s time=%d min ago prov=%s acc=%.1f lat=%f long=%f",
      getLocationQuality( location), ((System.currentTimeMillis()  - location.getTime()) / 1000) / 60 , location.getProvider(), location.getAccuracy(), location
             .getLatitude(), location.getLongitude()));
     if (location.hasBearing())
       sb.append(String.format(" bearing=%.2f", location.getBearing()));
     return sb.toString();
   }
 
   private LocationQuality getLocationQuality( Location location) {
     if (location == null) return LocationQuality.BAD;
     if (!location.hasAccuracy()) return LocationQuality.BAD;
     long currentTime = System.currentTimeMillis();
     if (currentTime - location.getTime() < maxAge * 1000
             && location.getAccuracy() <= desiredAccuracy)
       return LocationQuality.GOOD;
     if (acceptedAccuracy == -1
             || location.getAccuracy() <= acceptedAccuracy)
       return LocationQuality.ACCEPTED;
     return LocationQuality.BAD;
   }
 
   private synchronized void updateBestLocation(Location location) {
     bestLocation = getBestLocation(location, bestLocation);
     q = getLocationQuality(bestLocation);
   }
 
   protected Location getBestLocation(Location location,
                                      Location currentBestLocation) {
     Log.i(Contants.TIME_TO_GO, "@@ in getBestLocation");
     if (currentBestLocation == null) {
       // A new location is always better than no location
       return location;
     }
     if (location == null) return currentBestLocation;
     // Check whether the new location fix is newer or older
     long timeDelta = location.getTime() - currentBestLocation.getTime();
     boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
     boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
     boolean isNewer = timeDelta > 0;
     // If it's been more than two minutes since the current location, use
     // the new location because the user has likely moved
     if (isSignificantlyNewer) {
       return location;
       // If the new location is more than two minutes older, it must be
       // worse
     } else if (isSignificantlyOlder) {
       return currentBestLocation;
     }
     // Check whether the new location fix is more or less accurate
     int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation
             .getAccuracy());
     boolean isLessAccurate = accuracyDelta > 0;
     boolean isMoreAccurate = accuracyDelta < 0;
     boolean isSignificantlyLessAccurate = accuracyDelta > 200;
     // Check if the old and new location are from the same provider
     boolean isFromSameProvider = isSameProvider(location.getProvider(),
             currentBestLocation.getProvider());
     // Determine location quality using a combination of timeliness and
     // accuracy
     if (isMoreAccurate) {
       return location;
     } else if (isNewer && !isLessAccurate) {
       return location;
     } else if (isNewer && !isSignificantlyLessAccurate
             && isFromSameProvider) {
       return location;
     }
     return bestLocation;
   }
 
   /**
    * Checks whether two providers are the same
    */
   private boolean isSameProvider(String provider1, String provider2) {
     if (provider1 == null) return provider2 == null;
     return provider1.equals(provider2);
   }
 
   public int getCounter() {
     return counter;
   }
 
   public boolean canUseLocation() {
     return (bestLocation != null && q !=  SuppliesLocation.LocationQuality.BAD && counter > 0 );
   }
 
   public enum LocationQuality {
     BAD, ACCEPTED, GOOD;
 
     public String toString() {
       if (this == GOOD) return "Good";
       else if (this == ACCEPTED) return "Accepted";
       else return "Bad";
     }
   }
 }
