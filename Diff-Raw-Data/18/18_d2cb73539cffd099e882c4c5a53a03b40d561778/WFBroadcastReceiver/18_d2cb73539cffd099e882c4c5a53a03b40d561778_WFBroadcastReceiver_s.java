 /*Copyright [2010] [David Van de Ven]
 
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
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 
 public class WFBroadcastReceiver extends BroadcastReceiver {
 
     private Context ctxt;
 
     // For Auth
     private static final String AUTHEXTRA = "IRRADIATED";
     private static final String AUTH_ACTION = "org.wahtod.wififixer.AUTH";
     private static final String AUTHSTRING = "31415927";
 
     private static final int AUTH_NOTIF_ID = 2934;
 
     private static void handleWidgetAction(final Context context, int i) {
 	switch (i) {
 	case 0:
 	    context.sendBroadcast(new Intent(WidgetHandler.REASSOCIATE));
 	    break;
 
 	case 1:
 	    context.sendBroadcast(new Intent(WidgetHandler.TOGGLE_WIFI));
 	    break;
 
 	case 2:
 	    Intent intent = new Intent(context, WifiFixerActivity.class);
 	    intent.putExtra(WifiFixerActivity.OPEN_NETWORK_LIST, true);
 	    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 	    context.startActivity(intent);
 	    break;
 	}
     }
 
     private static boolean isserviceDisabled(final Context context) {
 	boolean state = PrefUtil.readBoolean(context, Pref.DISABLE_KEY);
 	return state;
     }
 
     private static void setServiceEnabled(final Context context,
 	    final Boolean state) {
 	PackageManager pm = context.getPackageManager();
 	ComponentName service = new ComponentName(context,
 		WifiFixerService.class);
 	if (state)
 	    pm.setComponentEnabledSetting(service,
 		    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
 		    PackageManager.DONT_KILL_APP);
 	else
 	    pm.setComponentEnabledSetting(service,
 		    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
 		    PackageManager.DONT_KILL_APP);
     }
 
     private Handler tHandler = new Handler() {
 	@Override
 	public void handleMessage(Message message) {
 	    ServiceAlarm.setAlarm(ctxt, false);
 	}
 
     };
 
     @Override
     public void onReceive(Context context, Intent intent) {
 	// Cache context for handler
 	if (ctxt == null)
 	    ctxt = context;
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
 	    tHandler.sendEmptyMessageDelayed(0, ServiceAlarm.STARTDELAY);
 	/*
 	 * For WIFI_SERVICE_ENABLE intent, run the service if not disabled by
 	 * pref
 	 */
 	else if (action.equals(IntentConstants.ACTION_WIFI_SERVICE_ENABLE)
 		&& !isserviceDisabled(context)) {
 	    setServiceEnabled(context, true);
 	    context.startService(new Intent(context, WifiFixerService.class));
 	}
 	/*
 	 * For WIFI_SERVICE_DISABLE intent, send stop to service and unset
 	 * logging and service alarms.
 	 */
 	else if (action.equals(IntentConstants.ACTION_WIFI_SERVICE_DISABLE)) {
 	    context.stopService(new Intent(context, WifiFixerService.class));
 	    setServiceEnabled(context, false);
 	    ServiceAlarm.setLogTS(context, false, 0);
 	    ServiceAlarm.unsetAlarm(context);
 	}
 	/*
 	 * Handle Widget intent
 	 */
 	else if (action.equals(FixerWidget.W_INTENT)) {
 	    handleWidgetAction(context, Integer.valueOf(PrefUtil.readString(
 		    context, PrefConstants.WIDGET_KEY)));
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
		Log.i(this.getClass().getName(), ctxt
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
 
 }
