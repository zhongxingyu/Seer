 /*
  * Copyright 2010 Andrew De Quincey -  adq@lidskialf.net
  * This file is part of rEdBus.
  *
  *  rEdBus is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  rEdBus is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with rEdBus.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.redbus.ui.alert;
 
 import org.redbus.R;
 import org.redbus.stopdb.StopDbHelper;
 import org.redbus.ui.arrivaltime.ArrivalTimeActivity;
 import org.redbus.ui.stopmap.StopMapActivity;
 
 import android.app.AlertDialog;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationManager;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 public class ProximityAlert extends BroadcastReceiver implements DialogInterface.OnClickListener {
 
 	private static final String[] proximityAlarmStrings = new String[] { "50  metres", "100 metres", "250 metres", "500 metres" };
 	private static final int[] proximityAlarmDistances= new int[] { 50, 100, 200, 500};
 
 	// These are ONLY used during UI construction. cannot be relied on to be set to anything useful in BroadcastReceiver implementation
 	private int uiStopCode;
 	private ArrivalTimeActivity uiArrivalTimeActivity;
 	private Spinner uiDistanceSpinner;
 
 	
 	public static void createProximityAlert(ArrivalTimeActivity arrivalTimeActivity, int stopCode) {
 		new ProximityAlert(arrivalTimeActivity, stopCode);
 	}
 	
 	/**
 	 * Empty constructor for BroadcastReceiver implementation
 	 */
 	public ProximityAlert() {
 	}
 	
 	private ProximityAlert(ArrivalTimeActivity arrivalTimeActivity, int stopCode) {
 		this.uiArrivalTimeActivity = arrivalTimeActivity;
 		this.uiStopCode = stopCode;
 
 		// load the view
 		View dialogView = arrivalTimeActivity.getLayoutInflater().inflate(R.layout.addproximityalert, null);		
 
 		// setup distance selector
 		uiDistanceSpinner = (Spinner) dialogView.findViewById(R.id.addproximityalert_distance);
 		ArrayAdapter<String> timeAdapter = new ArrayAdapter<String>(arrivalTimeActivity, android.R.layout.simple_spinner_item, proximityAlarmStrings);
 		timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		uiDistanceSpinner.setAdapter(timeAdapter);
 
 		// show the dialog!
 		new AlertDialog.Builder(arrivalTimeActivity)
 			.setView(dialogView)
 			.setTitle("Set alarm")
 			.setPositiveButton(android.R.string.ok, this)
 			.setNegativeButton(android.R.string.cancel, null)
 			.show();
 	}
 	
 	public void onClick(DialogInterface dialog, int which) {
 		StopDbHelper pt = StopDbHelper.Load(uiArrivalTimeActivity);
 		int stopNodeIdx = pt.lookupStopNodeIdxByStopCode(uiStopCode);
 		if (stopNodeIdx == -1)
 			return;
 		String stopName = pt.lookupStopNameByStopNodeIdx(stopNodeIdx);
 
 		// cancel any current alerts
 		AlertUtils.cancelAlerts(uiArrivalTimeActivity);
 
 		// stop location
 		Location location = new Location("");
 		location.setLatitude(pt.lat[stopNodeIdx] / 1E6);
 		location.setLongitude(pt.lon[stopNodeIdx] / 1E6);
 
 		// create an intent
 		Intent i = new Intent(uiArrivalTimeActivity, ProximityAlert.class);
		i.putExtra("StopCode", (long) uiStopCode);
 		i.putExtra("StopName", stopName);
 		i.putExtra("Location", location);
 		i.putExtra("Distance", proximityAlarmDistances[uiDistanceSpinner.getSelectedItemPosition()]);
 		i.putExtra("StartTime", System.currentTimeMillis());
 		PendingIntent pi = PendingIntent.getBroadcast(uiArrivalTimeActivity, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
 
 		// weird! Found I needed to add a proximity alert *first* otherwise the GPS on my phone doesn't get a lock with just the requestlocationupdates!?!
 		LocationManager lm = (LocationManager) uiArrivalTimeActivity.getSystemService(Context.LOCATION_SERVICE);
 		lm.addProximityAlert(pt.lat[stopNodeIdx], pt.lon[stopNodeIdx], 1, 0, pi);
 		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30 * 1000, 25, pi);
 
 		AlertUtils.addOngoingNotification(uiArrivalTimeActivity);
 
 		Toast.makeText(uiArrivalTimeActivity, "Alarm added!", Toast.LENGTH_SHORT).show();
 	}
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		// are we close enough yet?
 		Location curLocation = (Location) intent.getParcelableExtra(LocationManager.KEY_LOCATION_CHANGED);	
 		if (curLocation == null) {
 			LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
 			curLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 		}
 		if (curLocation == null) {
 			Toast.makeText(context, "Bus alarm cancelled; please enable your GPS!", Toast.LENGTH_LONG).show();
 			AlertUtils.cancelAlerts(context);
 			return;
 		}
 		Location stopLocation = (Location) intent.getParcelableExtra("Location");
 		if (stopLocation == null)
 			return;
 		int distance = intent.getIntExtra("Distance", 0);
 		double curDistance = curLocation.distanceTo(stopLocation);
 		if (curDistance > distance)
 			return;
 
 		// cancel all current alerts
 		AlertUtils.cancelAlerts(context);
 
 		// build text to show to user
 		String stopName = intent.getStringExtra("StopName");
 		if (stopName == null)
 			return;
 		StringBuffer text = new StringBuffer();
 		text.append("You are within ");
 		text.append(distance);
 		text.append(" metres of the bus stop \"");
 		text.append(intent.getStringExtra("StopName"));
 		text.append("\"!");
 
 		Intent i = new Intent(context, StopMapActivity.class);
 		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_ONE_SHOT);
 
 		Notification notification = new Notification(R.drawable.icon38, text, System.currentTimeMillis());
 		notification.defaults |= Notification.DEFAULT_ALL;
 		notification.flags |= Notification.FLAG_AUTO_CANCEL;
 		notification.setLatestEventInfo(context, "Bus alarm!", text, contentIntent);
 
 		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
 		nm.notify(AlertUtils.ALERT_NOTIFICATION_ID, notification);
 	}
 }
