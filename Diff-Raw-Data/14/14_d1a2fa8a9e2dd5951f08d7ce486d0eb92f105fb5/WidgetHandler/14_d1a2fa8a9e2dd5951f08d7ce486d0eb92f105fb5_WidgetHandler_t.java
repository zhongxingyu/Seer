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
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.net.wifi.WifiManager;
 import android.os.Handler;
 import android.os.Message;
 
 public class WidgetHandler extends BroadcastReceiver {
     private static WakeLock wlock;
     private static Context ctxt;
 
     /*
      * Intent Constants
      */
     protected static final String WIFI_ON = "org.wahtod.wififixer.WidgetHandler.WIFI_ON";
     protected static final String WIFI_OFF = "org.wahtod.wififixer.WidgetHandler.WIFI_OFF";
     protected static final String TOGGLE_WIFI = "org.wahtod.wififixer.WidgetHandler.WIFI_TOGGLE";
     protected static final String REASSOCIATE = "org.wahtod.wififixer.WidgetHandler.REASSOCIATE";
 
     /*
      * Handler Constants
      */
     private static final int ON = 0;
     private static final int OFF = 1;
     private static final int WATCHDOG = 2;
     private static final int TOGGLE = 3;
 
     /*
      * Delay Constants
      */
 
     private static final int TOGGLE_DELAY = 3000;
    private static final int WATCHDOG_DELAY = 5000;
 
     private Handler hWifiState = new Handler() {
 	@Override
 	public void handleMessage(Message message) {
 	    /*
 	     * Acquire Wake Lock
 	     */
 	    if (wlock == null)
 		wlock = new WakeLock(ctxt);
 	    wlock.wakeLock(true);
 
 	    /*
 	     * Process Message
 	     */
 	    switch (message.what) {
 
 	    case ON:
 		setWifiState(ctxt, true);
 		break;
 
 	    case OFF:
 		setWifiState(ctxt, false);
 		break;
 
 	    case WATCHDOG:
 		if (!getWifiManager(ctxt).isWifiEnabled()) {
 		    hWifiState.sendEmptyMessageDelayed(ON, TOGGLE_DELAY);
 		    hWifiState
 			    .sendEmptyMessageDelayed(WATCHDOG, WATCHDOG_DELAY);
 		}
 		break;
 
 	    case TOGGLE:
 		hWifiState.sendEmptyMessage(OFF);
 		hWifiState.sendEmptyMessageDelayed(ON, TOGGLE_DELAY);
		hWifiState.sendEmptyMessageDelayed(WATCHDOG, WATCHDOG_DELAY);
 		break;
 	    }
 	    /*
 	     * Release Wake Lock
 	     */
 	    wlock.wakeLock(false);
 	}
     };
 
     private static WifiManager getWifiManager(final Context context) {
 	return (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
     }
 
     private static void setWifiState(final Context context, final boolean b) {
 	WifiManager wm = getWifiManager(context);
 	wm.setWifiEnabled(b);
     }
 
     @Override
     public void onReceive(Context context, Intent intent) {
 	if (ctxt == null)
 	    ctxt = context;
 
 	String action = intent.getAction();
 
 	if (action.equals(WIFI_ON))
 	    setWifiState(context, true);
 	else if (action.equals(WIFI_OFF))
 	    setWifiState(context, false);
 	else if (action.equals(TOGGLE_WIFI))
 	    hWifiState.sendEmptyMessage(TOGGLE);
 	else if (action.equals(REASSOCIATE))
 	    getWifiManager(context).reassociate();
 
     }
 
 }
