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
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.redbus.R;
 import org.redbus.arrivaltime.ArrivalTime;
 import org.redbus.arrivaltime.ArrivalTimeHelper;
 import org.redbus.arrivaltime.IArrivalTimeResponseListener;
 import org.redbus.stopdb.ServiceBitmap;
 import org.redbus.stopdb.StopDbHelper;
 import org.redbus.ui.arrivaltime.ArrivalTimeActivity;
 
 import android.app.AlarmManager;
 import android.app.AlertDialog;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.SystemClock;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 public class TemporalAlert extends BroadcastReceiver implements IArrivalTimeResponseListener, View.OnClickListener, DialogInterface.OnClickListener {
 
 	private static final int ALARM_MAX_TIMEOUT_MSEC = 40 * 60 * 1000;
 	private static final String[] temporalAlarmStrings = new String[] { "Due", "5 mins away", "10 mins away", "20 mins away", "30 mins away" };
 	private static final int[] temporalAlarmTimeouts = new int[] { 0, 5 * 60, 10 *  60, 20 * 60, 30 * 60 };
 
 	// These are ONLY used during UI construction. cannot be relied on to be set to anything useful in BroadcastReceiver implementation
 	private int uiStopCode;
 	private ArrivalTimeActivity uiArrivalTimeActivity;
 	private Spinner uiTimeSpinner;
 	private Button uiServicesButton;
 	private String[] uiServices;
 	private boolean[] uiSelectedServices;
 	private String uiStopName;
 
 	// these are only used during broadcastreceiver implementation
 	private Context broadcastContext;
 	private int broadcastStopCode;
 	private String broadcastStopName;
 	private Intent broadcastIntent;
 	
 	public static void createTemporalAlert(ArrivalTimeActivity arrivalTimeActivity, int stopCode, String stopName, String selectedService) {
 		new TemporalAlert(arrivalTimeActivity, stopCode, stopName, selectedService);
 	}
 
 	/**
 	 * Empty constructor for BroadcastReceiver implementation
 	 */
 	public TemporalAlert() {
 	}
 
 	private TemporalAlert(ArrivalTimeActivity arrivalTimeActivity, int stopCode, String stopName, String selectedService) {
 		this.uiArrivalTimeActivity = arrivalTimeActivity;
 		this.uiStopCode = stopCode;
 		this.uiStopName = stopName;
 		
 		// get the list of services for this stop
 		StopDbHelper pt = StopDbHelper.Load(arrivalTimeActivity);
 		int stopNodeIdx = pt.lookupStopNodeIdxByStopCode((int) stopCode);
 		if (stopNodeIdx == -1)
 			return;
 		ServiceBitmap serviceMap = pt.lookupServiceBitmapByStopNodeIdx(stopNodeIdx);
 		
 		ArrayList<String> servicesList = pt.getServiceNames(serviceMap);
 		uiServices = servicesList.toArray(new String[servicesList.size()]);
 		uiSelectedServices = new boolean[uiServices.length];
 
 		// preselect the clicked-on service
 		if (selectedService != null) {
 			for(int i=0; i< uiServices.length; i++) {
 				if (selectedService.equalsIgnoreCase(uiServices[i])) {
 					uiSelectedServices[i] = true;
 					break;
 				}
 			}
 		} else {
 			if (uiSelectedServices.length > 0)
 				uiSelectedServices[0] = true;
 		}
 
 		// load the view
 		View dialogView = arrivalTimeActivity.getLayoutInflater().inflate(R.layout.addtemporalalert, null);		
 
 		// setup services selector
 		uiServicesButton = (Button) dialogView.findViewById(R.id.addtemporalalert_services);
 		updateServicesList(uiServicesButton, uiServices, uiSelectedServices);
 		uiServicesButton.setOnClickListener(this);
 
 		// setup time selector
 		uiTimeSpinner = (Spinner) dialogView.findViewById(R.id.addtemporalalert_time);
 		ArrayAdapter<String> timeAdapter = new ArrayAdapter<String>(arrivalTimeActivity, android.R.layout.simple_spinner_item, temporalAlarmStrings);
 		timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		uiTimeSpinner.setAdapter(timeAdapter);
 
 		// show the dialog!
 		new AlertDialog.Builder(arrivalTimeActivity)
 			.setView(dialogView)
 			.setTitle("Set alarm")
 			.setPositiveButton(android.R.string.ok, this)
 			.setNegativeButton(android.R.string.cancel, null)
 			.show();
 	}
 
 	public void onClick(View v) {
 		new AlertDialog.Builder( uiArrivalTimeActivity )
 	       .setMultiChoiceItems( uiServices, uiSelectedServices, new DialogInterface.OnMultiChoiceClickListener() {
 				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
 					uiSelectedServices[which] = isChecked;							
 				}
 	       })
 	       .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					updateServicesList(uiServicesButton, uiServices, uiSelectedServices);
 				}
 	       })
 	       .show();
 	}
 
 	public void onClick(DialogInterface dialog, int which) {
 		// cancel any current alerts
 		AlertUtils.cancelAlerts(uiArrivalTimeActivity);
 		
 		// figure out list of services
 		ArrayList<String> selectedServicesList = new ArrayList<String>();
 		for(int i=0; i< uiServices.length; i++) {
 			if (uiSelectedServices[i]) {
 				selectedServicesList.add(uiServices[i]);
 			}
 		}
 		if (selectedServicesList.size() == 0)
 			return;
 
 		// create an intent
 		Intent i = new Intent(uiArrivalTimeActivity, TemporalAlert.class);
		i.putExtra("StopCode", (long) uiStopCode);
 		i.putExtra("StopName", uiStopName);
 		i.putExtra("Services", selectedServicesList.toArray(new String[selectedServicesList.size()]));
 		i.putExtra("StartTime", System.currentTimeMillis());
 		i.putExtra("TimeoutSecs", temporalAlarmTimeouts[uiTimeSpinner.getSelectedItemPosition()]);
 		PendingIntent pi = PendingIntent.getBroadcast(uiArrivalTimeActivity, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
 
 		// schedule it in 10 seconds
 		AlarmManager am = (AlarmManager) uiArrivalTimeActivity.getSystemService(Context.ALARM_SERVICE);
 		am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 10000, pi);
 
 		AlertUtils.addOngoingNotification(uiArrivalTimeActivity);
 	
 		Toast.makeText(uiArrivalTimeActivity, "Alarm added!", Toast.LENGTH_SHORT).show();
 	}
 	
 	private void updateServicesList(Button b, String[] services, boolean[] selectedServices)
 	{
 		StringBuffer result = new StringBuffer();
 		for(int i=0; i< services.length; i++) {
 			if (selectedServices[i]) {
 				if (result.length() > 0)
 					result.append(", ");
 				result.append(services[i]);
 			}
 		}
 		b.setText(result);
 	}
 
 	
 	
 	
 	
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		this.broadcastContext = context;
 		this.broadcastIntent = intent;
 
 		broadcastStopCode = (int) intent.getLongExtra("StopCode", -1);
 		if (broadcastStopCode == -1)
 			return;
 		broadcastStopName = intent.getStringExtra("StopName");
 		if (broadcastStopName == null)
 			return;
 
 		ArrivalTimeHelper.getBusTimesAsync(broadcastStopCode, 0, null, this);
 	}
 
 	private void rescheduleAlarm() {
 		// make sure alarms don't run forever
 		long startTime = broadcastIntent.getLongExtra("StartTime", -1);
 		if ((startTime == -1) || ((startTime + ALARM_MAX_TIMEOUT_MSEC) < System.currentTimeMillis())) {
 			Intent i = new Intent(broadcastContext, ArrivalTimeActivity.class);
 			PendingIntent contentIntent = PendingIntent.getActivity(broadcastContext, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
 
 			Notification notification = new Notification(R.drawable.icon38, "Bus alarm aborted!", System.currentTimeMillis());
 			notification.defaults |= Notification.DEFAULT_ALL;
 			notification.flags |= Notification.FLAG_AUTO_CANCEL;
 			notification.setLatestEventInfo(broadcastContext, "Bus alarm aborted!", "Bus did not arrive within 40 mins; cancelling alarm", contentIntent);
 
 			NotificationManager nm = (NotificationManager) broadcastContext.getSystemService(Context.NOTIFICATION_SERVICE);
 			nm.notify(AlertUtils.ALERT_NOTIFICATION_ID, notification);
 			return;
 		}
 
 		// schedule it in 60 seconds
 		PendingIntent pi = PendingIntent.getBroadcast(broadcastContext, 0, broadcastIntent, 0);
 		AlarmManager am = (AlarmManager) broadcastContext.getSystemService(Context.ALARM_SERVICE);
 		am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 60000, pi);
 	}
 
 	public void getBusTimesError(int requestId, int code, String message) {
 		rescheduleAlarm();
 	}
 
 	public void getBusTimesSuccess(int requestId, List<ArrivalTime> busTimes) {
 		String[] requestedServices = broadcastIntent.getStringArrayExtra("Services");
 		int timeout = broadcastIntent.getIntExtra("TimeoutSecs", -1);
 		if ((requestedServices == null) || (timeout == -1))
 			return;
 		HashMap<String, Boolean> requestedServicesLookup = new HashMap<String, Boolean>();
 		for (String curService : requestedServices)
 			requestedServicesLookup.put(curService.toLowerCase(), new Boolean(true));
 
 		for (ArrivalTime curTime : busTimes) {
 			if (requestedServicesLookup.containsKey(curTime.service.toLowerCase()) && 
 					(curTime.arrivalAbsoluteTime == null) &&
 					((curTime.arrivalMinutesLeft * 60) <= timeout)) {
 
 				StringBuffer text = new StringBuffer();
 				text.append("The ");
 				text.append(curTime.service);
 				text.append(" is due");
 				if (curTime.arrivalMinutesLeft > 0) {
 					text.append(" in ");
 					text.append(curTime.arrivalMinutesLeft);
 					text.append(" minutes");
 				}
 				text.append("!");
 
 				Intent i = new Intent(broadcastContext, ArrivalTimeActivity.class);
				i.putExtra("StopCode", (long) broadcastStopCode);
 				i.putExtra("StopName", broadcastStopName);
 				PendingIntent contentIntent = PendingIntent.getActivity(broadcastContext, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
 
 				Notification notification = new Notification(R.drawable.icon38, text, System.currentTimeMillis());
 				notification.defaults |= Notification.DEFAULT_ALL;
 				notification.flags |= Notification.FLAG_AUTO_CANCEL;
 				notification.setLatestEventInfo(broadcastContext, "Bus alarm!", text, contentIntent);
 
 				NotificationManager nm = (NotificationManager) broadcastContext.getSystemService(Context.NOTIFICATION_SERVICE);
 				nm.notify(AlertUtils.ALERT_NOTIFICATION_ID, notification);
 				return;
 			}
 		}
 
 		// nothing matched, just reschedule it
 		rescheduleAlarm();
 	}
 }
