 package com.LocateMeInc.locate;
 
 import android.content.Context;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 
 public class CurLocation implements LocationListener {
 	Location curLoc;
 
 	// Constructor
 	CurLocation() {		
 		// Get location manager
 		LocationManager lm = ((LocationManager)MainActivity.getContext().getSystemService(Context.LOCATION_SERVICE));
 		
 		// Use last known location until updates are received
 		this.curLoc = lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
 		
 		// If there is no known location, replace null with an empty location
 		if (this.curLoc == null) {
 			// Init curLoc, hopefully fixing nullpointerexceptions
 			this.curLoc = new Location(LocationManager.PASSIVE_PROVIDER);
 		}
 		
 		// Set up criteria
 		Criteria locCriterias = new Criteria();
 		locCriterias.setAccuracy(Criteria.ACCURACY_FINE);
 		locCriterias.setAltitudeRequired(true);
 		locCriterias.setVerticalAccuracy(Criteria.ACCURACY_HIGH);
 		
 		// Request regular updates
		lm.requestLocationUpdates(500, 10, locCriterias, this, null);
		
		// Request single update to get things started
		lm.requestSingleUpdate(locCriterias, this, null);
 	}
 	
     @Override
     public void onLocationChanged(Location loc) {
         this.curLoc = loc;
     }
 
     @Override
     public void onProviderDisabled(String provider) {}
 
     @Override
     public void onProviderEnabled(String provider) {}
 
     @Override
     public void onStatusChanged(String provider, int status, Bundle extras) {}
     
     public Location getLocation() {
     	return this.curLoc;
     }
     
     public void close() {
     	// Get location manager
     	LocationManager lm = ((LocationManager)MainActivity.getContext().getSystemService(Context.LOCATION_SERVICE));
     	
     	// Remove all updates associated with this object
     	lm.removeUpdates(this);
     }
 }
