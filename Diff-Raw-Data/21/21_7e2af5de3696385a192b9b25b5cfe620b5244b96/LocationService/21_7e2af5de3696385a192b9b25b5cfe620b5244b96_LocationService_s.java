 /**
  * This file is part of Plingnote.
  * Copyright (C) 2012 David Grankvist
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
 
 package com.plingnote.service;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import com.plingnote.database.DatabaseHandler;
 import com.plingnote.database.DatabaseUpdate;
 import com.plingnote.database.Note;
 import com.plingnote.notifications.NoteNotification;
 import com.plingnote.utils.IntentExtra;
 
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.IBinder;
 
 /**
  * A service that runs in the bakground to retreive the user's location.
  * When the current location is updated, proximity alerts will be added to
  * positions stored in the database.
  * 
  * @author David Grankvist
  *
  */
 public class LocationService extends Service implements LocationListener, Observer {
 	private LocationManager locationManager;
 	private String provider;
 	private Criteria criteria;
 	private List<PendingIntent> pIntentList;
 	private static final long UPDATE_FREQUENCY_TIME = 1000 * 60; //milliseconds
 	private static final float UPDATE_FREQUENCY_DISTANCE = 10; //meters
 	private static final int ALERT_RADIUS = 100; //meters
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId){
 		super.onStartCommand(intent, flags, startId);
 		this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		this.criteria = new Criteria();
 		this.criteria.setCostAllowed(true);
 		this.criteria.setAltitudeRequired(false);
 		this.criteria.setBearingRequired(false);
 		this.criteria.setAccuracy(Criteria.ACCURACY_FINE);
 		this.provider = this.locationManager.getBestProvider(this.criteria, false);
 		this.locationManager.requestLocationUpdates(this.provider, 
 				UPDATE_FREQUENCY_TIME, UPDATE_FREQUENCY_DISTANCE, this);
 		this.pIntentList = new ArrayList<PendingIntent>();
 		DatabaseHandler.getInstance(this).addObserver(this);
 		return START_STICKY;
 	}
 
 	public void onLocationChanged(Location location){
 
 	}
 
 	/**
 	 * Called when the database is updated
 	 */
 	public void update(Observable observable, Object data) {
 		//We do not know which one was deleted
		if(data == DatabaseUpdate.UPDATED_LOCATION || data == DatabaseUpdate.DELETED_NOTE){
			this.removeAlerts();
			this.addAllAlerts();
		}
		if(data == DatabaseUpdate.NEW_NOTE)
			this.addAlertToLast();
 	}
 	
 	/**
 	 * This method adds a proximity alert to the location associated to the argument note.
 	 * When the alert is triggered, an intent to start NoteNotification is fired.
 	 * All PendingIntent objects are saved in a list in order to remove the alert later.
 	 * 
 	 * @param n The note that needs an alert to be fired
 	 */
 	private void addAlert(Note n){
 		com.plingnote.database.Location loc = n.getLocation();
 		if(loc.getLatitude() != 0.0 && loc.getLongitude() != 0.0){
 			Intent intent = new Intent(this, NoteNotification.class);
 			intent.putExtra(IntentExtra.id.toString(), n.getId());
 			PendingIntent pIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
 			this.locationManager.addProximityAlert(loc.getLatitude(), 
 					loc.getLongitude(), ALERT_RADIUS, -1, pIntent);
 			this.pIntentList.add(pIntent);
 		}
 	}
 	
 	/**
 	 * Adds an alert to the location of the note most recently inserted to the database.
 	 */
 	private void addAlertToLast(){
 		DatabaseHandler dbHandler = DatabaseHandler.getInstance(this);
 		Note n = dbHandler.getNote(dbHandler.getLastId());
 		this.addAlert(n);
 	}
 	
 	/**
 	 * Loops through the list of notes retrieved from the database to add alerts to their locations.
 	 */
 	private void addAllAlerts(){
 		DatabaseHandler dbHandler = DatabaseHandler.getInstance(this);
 		List<Note> nlist = dbHandler.getNoteList();
 		for(Note n:nlist){
 			this.addAlert(n);
 		}
 	}
 
 	/**
 	 * Loops through the list of PendingIntents to remove alerts.
 	 */
 	private void removeAlerts(){
 		for(PendingIntent pIntent:pIntentList)
 			this.locationManager.removeProximityAlert(pIntent);
 		this.pIntentList.clear();
 	}
 
 	public void onProviderDisabled(String provider) {
 
 	}
 
 	public void onProviderEnabled(String provider) {
 
 	}
 
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 }
