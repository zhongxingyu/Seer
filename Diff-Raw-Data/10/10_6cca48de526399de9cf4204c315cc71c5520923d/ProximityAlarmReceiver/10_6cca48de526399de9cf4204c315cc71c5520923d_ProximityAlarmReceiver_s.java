 package org.redbus;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationManager;
 
 public class ProximityAlarmReceiver extends BroadcastReceiver {
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		// are we close enough yet?
 		Location curLocation = (Location) intent.getParcelableExtra(LocationManager.KEY_LOCATION_CHANGED);	
 		if (curLocation == null) {
 			LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
 			curLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 		}
 		Location stopLocation = (Location) intent.getParcelableExtra("Location");
 		int distance = intent.getIntExtra("Distance", 0);
 		double curDistance = curLocation.distanceTo(stopLocation);
 		if (curDistance > distance)
 			return;
 
 		// cancel all current alerts
 		BusTimesActivity.cancelAlerts(context);
 
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
 
 		Notification notification = new Notification(R.drawable.tracker_24x24_masked, text, System.currentTimeMillis());
 		notification.defaults |= Notification.DEFAULT_ALL;
 		notification.flags |= Notification.FLAG_AUTO_CANCEL;
 		notification.setLatestEventInfo(context, "Bus alarm!", text, contentIntent);
 
 		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
 		nm.notify(BusTimesActivity.ALERT_NOTIFICATION_ID, notification);
 	}
 }
