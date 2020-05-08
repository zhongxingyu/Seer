 /**
  * This file is part of Plingnote.
  * Copyright (C) 2012 Barnabas sapan
  * 
  * Plingnote is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.plingnote;
 
 import java.util.List;
 
 import android.content.Context;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.view.View;
import android.widget.ImageButton;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 
 /**
  * The map activity
  * @author Barnabas Sapan
  */
 public class ActivityMap extends MapActivity implements LocationListener {
 	private final int DISABLE_GPS_WITH_ACCURACY_HIGHER_THEN = 100;
 	private final int GPS_SIGNIFICANTLY_BETTER_DATA_IF = 1000 * 60 * 2;
 	
 	private MapController mc;
 	private MapView map;
 	private LocationManager locationManager;
 	private String provider;
 	private Location location;
 	private boolean isWantingAFix = false;
 	private Criteria criteria;
 	private List<Overlay> overlayList;
 	private UpdatableOverlay mapOverlayPin;
 	private UpdatableOverlay mapOverlayGPSAccuracy;
 	private UpdatableOverlay mapOverlayPinNotes;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.setContentView(R.layout.fragment_mapview);
 		this.map = (MapView) findViewById(R.id.mapview);
 		this.mc = map.getController();
 		this.overlayList =  map.getOverlays();
 		this.overlayList.clear();
 				
 		this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 
 		//Get the last known location from the network and GPS so we 
 		//quickly can zoom to the users position without waiting for a
 		//fix or loockup
 		Location lastKnownNetwork = this.locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 		Location lastKnownGPS = this.locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 		
 		//Determinate which locations is better and set the location
 		//to the most accurate one
 		if(this.isBetterLocation(lastKnownNetwork, lastKnownGPS)){
 			this.location = lastKnownNetwork;
 		} else {
 			this.location = lastKnownGPS;
 		}
 			
 		//Generate a GeoPoint from the last know location
 		GeoPoint point = new GeoPoint((int)(this.location.getLatitude() * 1E6), (int)(this.location.getLongitude() * 1E6));
 		
 		//Instantiate the overlays for the map
 		this.mapOverlayPin = new MapOverlayPinCurrentPos(this, this.map, point);
 		this.mapOverlayGPSAccuracy = new MapOverlayGPSAccuracy(point, this.location);
 		this.mapOverlayPinNotes = new MapOverlayPinNotes(this, this.getResources().getDrawable(R.drawable.map_note_marker), map);
 		
 		//Add the overlays to the list
 		this.overlayList.add((Overlay) this.mapOverlayPin);
 		this.overlayList.add((Overlay) this.mapOverlayGPSAccuracy);
 		this.overlayList.add(new MapOverlayLongpressHandler(this, map));
 		this.overlayList.add((Overlay) this.mapOverlayPinNotes);
 		
 		//Zoom to the last know position
 		this.zoomToLastKnownPosition();
 
 		//Criteria for an accurate fix, presumably GPS
 		this.criteria = new Criteria();
 		this.criteria.setCostAllowed(true);
 		this.criteria.setAltitudeRequired(false);
 		this.criteria.setBearingRequired(false);
 		this.criteria.setAccuracy(Criteria.ACCURACY_FINE);
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 
 	public void click(View view) {
 		switch (view.getId()) {
 		case R.id.button_find_my_location:
 			//If we already are waiting for a fix
 			//do not request location updates again
 			if(this.isWantingAFix == false) {
 				//Indicate that we want a GPS fix
 				this.isWantingAFix = true;
 
 				//Select the best provider for our criteria, presumably GPS 
 				this.provider = this.locationManager.getBestProvider(this.criteria, false);
 				this.locationManager.requestLocationUpdates(this.provider, 400, 1, this);
 
 				//Update our overlays as they might have changes
 				//since we are searching for fix more intensively
 				this.updateOverlays();
 			} 
 
 			//Zoom in on the new or last know position
 			this.zoomToLastKnownPosition();
 			
 			break;
 			
		case R.id.button_fit_note_markers:			
 			int minLat = Integer.MAX_VALUE;
 			int maxLat = Integer.MIN_VALUE;
 			int minLon = Integer.MAX_VALUE;
 			int maxLon = Integer.MIN_VALUE;
 
 			for (Note note :  DatabaseHandler.getInstance(this).getNoteList()) { 
 				int lat = (int)(note.getLocation().getLatitude() * 1E6);
 				int lon = (int)(note.getLocation().getLongitude() * 1E6);
 
 				maxLat = Math.max(lat, maxLat);
 				minLat = Math.min(lat, minLat);
 				maxLon = Math.max(lon, maxLon);
 				minLon = Math.min(lon, minLon);
 			}
 
 			//Formula to zoom and fit the view nicely is taken from
 			//http://stackoverflow.com/questions/5241487/
 			//android-mapview-setting-zoom-automatically-until-all-itemizedoverlays-are-visi
 			double fitFactor = 1.2;
 			mc.zoomToSpan((int)(Math.abs(maxLat - minLat) * fitFactor), (int)(Math.abs(maxLon - minLon) * fitFactor));
 			mc.animateTo(new GeoPoint((maxLat + minLat) / 2, (maxLon + minLon) / 2 )); 
 			
 			break;
 		}
 		
 	}
 
 	/**
 	 * Updates the overlays and refresh the map
 	 * sometime in the future
 	 */
 	private void updateOverlays(){
 		this.mapOverlayPin.update(this.location);
 		this.mapOverlayGPSAccuracy.update(this.location);
 
 		this.map.invalidate();
 	}
 	
 	/**
 	 * Zoom to the last know approximated position
 	 */
 	private void zoomToLastKnownPosition() {
 		GeoPoint point = new GeoPoint((int)(this.location.getLatitude() * 1E6), (int)(this.location.getLongitude() * 1E6));
 		
 		this.mc.animateTo(point);
 		this.mc.setZoom(18);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		
		//Check if there are any notes with location in the database
		boolean notesWithLocationExists = false;
		for(Note note : DatabaseHandler.getInstance(this).getNoteList()) {
			if(note.getLocation().getLongitude() != 0 && note.getLocation().getLatitude() != 0) {
				notesWithLocationExists = true;
				break;
			}
		}
		
		//Disable the fit notes to view button if there are no notes with location
		ImageButton fitToViewButton = (ImageButton) findViewById(R.id.button_fit_note_markers);
		if(notesWithLocationExists)
			fitToViewButton.setVisibility(ImageButton.VISIBLE);
		else
			fitToViewButton.setVisibility(ImageButton.GONE);
		
 		//Update our notes overlay if new note have been created with locations
 		mapOverlayPinNotes.update(null);
 		map.invalidate();
 		
 		if(this.isWantingAFix)
 			this.locationManager.requestLocationUpdates(this.provider, 400, 1, this);
 	}
 
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 		if(this.isWantingAFix)
 			this.locationManager.removeUpdates(this);
 	}
 
 
 	public void onLocationChanged(Location location) {
 		//Update our overlays if we have gotten a better location data
 		if (isBetterLocation(this.location, location)) {
 			this.location = location;
 			updateOverlays();
 		}
 
 		//If we get a fix that is accurate enough, remove subscription from location updates
 		if(location.getAccuracy() <= DISABLE_GPS_WITH_ACCURACY_HIGHER_THEN){
 			locationManager.removeUpdates(this);
 		}
 	}
 
 	public void onProviderDisabled(String provider) { }
 
 	public void onProviderEnabled(String provider) { }
 
 	public void onStatusChanged(String provider, int status, Bundle extras) { }
 
 	//Taken from Android Developers Guide examples 
 	//(http://developer.android.com/guide/topics/location/strategies.html)
 	/** Determines whether one Location reading is better than the current Location fix
 	 * @param location  The new Location that you want to evaluate
 	 * @param currentBestLocation  The current Location fix, to which you want to compare the new one
 	 */
 	private boolean isBetterLocation(Location location, Location currentBestLocation) {
 		if (currentBestLocation == null) {
 			// A new location is always better than no location
 			return true;
 		}
 
 		// Check whether the new location fix is newer or older
 		long timeDelta = location.getTime() - currentBestLocation.getTime();
 		boolean isSignificantlyNewer = timeDelta > GPS_SIGNIFICANTLY_BETTER_DATA_IF;
 		boolean isSignificantlyOlder = timeDelta < -GPS_SIGNIFICANTLY_BETTER_DATA_IF;
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
 
 	/** Checks whether two providers are the same */
 	private boolean isSameProvider(String provider1, String provider2) {
 		if (provider1 == null) {
 			return provider2 == null;
 		}
 		return provider1.equals(provider2);
 	}
 }
