 /*Copyright [2010-2011] [David Van de Ven]
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 
  */
 
 package org.wahtod.wififixer;
 
 import org.wahtod.wififixer.PrefConstants.Pref;
 
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 
 public class BroadcastHandler {
     private Context ctxt;
 
     // For Auth
     private static final String AUTHEXTRA = "IRRADIATED";
     private static final String AUTH_ACTION = "org.wahtod.wififixer.AUTH";
     private static final String AUTHSTRING = "31415927";
 
     private static final int AUTH_NOTIF_ID = 2934;
 
     /*
      * Thread for boot service start
      */
     private Thread tStartService = new Thread() {
 	public void run() {
 	    try {
 		Thread.sleep(ServiceAlarm.STARTDELAY);
 	    } catch (InterruptedException e) {
 		Log.i("BroadCastHandler", "Startup Thread Interrupted");
 		return;
 	    }
 
 	    /**
 	     * Start Service
 	     */
 	    ServiceAlarm.setAlarm(ctxt, false);
 	}
     };
 
     private static void handleWidgetAction(final Context context,
 	    final Intent intent) {
 	int command;
 	/*
 	 * Handle null value possible if prefs not initialized yet
 	 */
 	String action = PrefUtil.readString(context, PrefConstants.WIDGET_KEY);
 	if (action == null) {
 	    /*
 	     * Default: REASSOCIATE
 	     */
 	    command = 0;
 	} else
 	    command = Integer.valueOf(action);
 
 	switch (command) {
 	case 0:
 	    context.sendBroadcast(new Intent(WidgetHandler.REASSOCIATE));
 	    break;
 
 	case 1:
 	    context.sendBroadcast(new Intent(WidgetHandler.TOGGLE_WIFI));
 	    break;
 
 	case 2:
 	    Intent widgetintent = new Intent(context, WifiFixerActivity.class);
 	    widgetintent.putExtra(WifiFixerActivity.OPEN_NETWORK_LIST, true);
 	    widgetintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 	    context.startActivity(widgetintent);
 	    break;
 	}
     }
 
     private static boolean isserviceDisabled(final Context context) {
 	boolean state = PrefUtil.readBoolean(context, Pref.DISABLE_KEY.key());
 	return state;
     }
 
     public void handleIntent(final Context context, final Intent intent) {
 	/*
 	 * Respond to manifest intents
 	 */
 	String action = intent.getAction();
 
 	/*
 	 * For boot completed, check DISABLE_KEY if false, schedule the service
 	 * run
 	 */
 	if (action.equals(Intent.ACTION_BOOT_COMPLETED)
 		&& !isserviceDisabled(context))
 	    tStartService.start();
 	/*
 	 * For WIFI_SERVICE_ENABLE intent, set service enabled and run
 	 */
 	else if (action.equals(IntentConstants.ACTION_WIFI_SERVICE_ENABLE)) {
 	    ServiceAlarm.setServiceEnabled(context, WifiFixerService.class,
 		    true);
 	    PrefUtil.writeBoolean(context, Pref.DISABLE_KEY.key(), false);
 	    context.startService(new Intent(context, WifiFixerService.class));
 	}
 	/*
 	 * For WIFI_SERVICE_DISABLE intent, send stop to service and unset
 	 * logging and service alarms.
 	 */
 	else if (action.equals(IntentConstants.ACTION_WIFI_SERVICE_DISABLE)) {
 	    context.stopService(new Intent(context, WifiFixerService.class));
 	    ServiceAlarm.setServiceEnabled(context, WifiFixerService.class,
 		    false);
 	    PrefUtil.writeBoolean(context, Pref.DISABLE_KEY.key(), true);
 	    ServiceAlarm.unsetAlarm(context);
 	    context.stopService(new Intent(context, LogService.class));
 	} else if (action.equals(IntentConstants.ACTION_WIFI_ON)) {
 	    if (!PrefUtil.readBoolean(context, PrefConstants.WIFI_STATE_LOCK))
 		context.sendBroadcast(new Intent(WidgetHandler.WIFI_ON));
 	} else if (action.equals(IntentConstants.ACTION_WIFI_OFF)) {
 	    if (!PrefUtil.readBoolean(context, PrefConstants.WIFI_STATE_LOCK))
 		context.sendBroadcast(new Intent(WidgetHandler.WIFI_OFF));
 	} else if (action.equals(IntentConstants.ACTION_WIFI_TOGGLE)) {
 	    if (!PrefUtil.readBoolean(context, PrefConstants.WIFI_STATE_LOCK))
 		context.sendBroadcast(new Intent(WidgetHandler.TOGGLE_WIFI));
 	}
 
 	/*
 	 * Handle Widget intent
 	 */
 	else if (action.equals(FixerWidget.W_INTENT)) {
 	    handleWidgetAction(context, intent);
 	}
 	/*
 	 * Authorization code
 	 * 
 	 * Hey, if you're poking into this, and can read code, you can afford to
 	 * donate!
 	 */
 	else if (action.equals(AUTH_ACTION)) {
 	    if (intent.hasExtra(AUTHEXTRA)
 		    && intent.getStringExtra(AUTHEXTRA).contains(AUTHSTRING)) {
 		Log.i(LogService.getLogTag(ctxt), ctxt
 			.getString(R.string.authed));
 		// Ok, do the auth
 		if (!PrefUtil.readBoolean(ctxt, ctxt
 			.getString(R.string.isauthed))) {
 		    PrefUtil.writeBoolean(ctxt, ctxt
 			    .getString(R.string.isauthed), true);
 		    NotifUtil
 			    .show(
 				    ctxt,
 				    ctxt.getString(R.string.donatethanks),
 				    ctxt.getString(R.string.authorized),
 				    AUTH_NOTIF_ID,
 				    PendingIntent
 					    .getActivity(
 						    ctxt,
 						    0,
 						    new Intent(
 							    android.provider.Settings.ACTION_WIFI_SETTINGS),
 						    0));
 		}
 
 	    }
 	}
     }
 
     public BroadcastHandler(Context context) {
	ctxt = context;
     }
 
 }
