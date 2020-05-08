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
 
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.net.wifi.WifiManager;
 import android.os.Handler;
 import android.os.Message;
 import android.widget.Toast;
 
 public class WidgetHandler {
     private static volatile WakeLock wlock;
     private static Context ctxt;
 
     /*
      * Intent Constants
      */
     protected static final String WIFI_ON = "org.wahtod.wififixer.WidgetHandler.WIFI_ON";
     protected static final String WIFI_OFF = "org.wahtod.wififixer.WidgetHandler.WIFI_OFF";
     protected static final String TOGGLE_WIFI = "org.wahtod.wififixer.WidgetHandler.WIFI_TOGGLE";
     protected static final String REASSOCIATE = "org.wahtod.wififixer.WidgetHandler.WIFI_REASSOCIATE";
 
     /*
      * Handler Constants
      */
     private static final int ON = 0;
     private static final int OFF = 1;
     private static final int WATCHDOG = 2;
     private static final int TOGGLE = 3;
 
     /*
      * Notification ID
      */
     private static final int TOGGLE_ID = 23497;
 
     /*
      * Delay Constants
      */
     private static final int TOGGLE_DELAY = 8000;
     private static final int WATCHDOG_DELAY = 11000;
     private static final int SHORT = 300;
 
     private static WifiManager wm;
 
     public class RToggleRunnable implements Runnable {
 	private Handler hWifiState = new Handler() {
 	    @Override
 	    public void handleMessage(Message msg) {
 		/*
 		 * Process MESSAGE
 		 */
 		switch (msg.what) {
 
 		case ON:
 		    setWifiState(ctxt, true);
 		    break;
 
 		case OFF:
 		    setWifiState(ctxt, false);
 		    break;
 
 		case WATCHDOG:
 		    if (!getWifiManager(ctxt).isWifiEnabled()) {
 			hWifiState.sendEmptyMessageDelayed(ON, TOGGLE_DELAY);
 			hWifiState.sendEmptyMessageDelayed(WATCHDOG,
 				WATCHDOG_DELAY);
 		    } else {
 			NotifUtil.cancel(TOGGLE_ID, ctxt);
 			PrefUtil.writeBoolean(ctxt,
 				PrefConstants.WIFI_STATE_LOCK, false);
 			/*
 			 * Release Wake Lock
 			 */
 			wlock.lock(false);
 		    }
 		    break;
 
 		case TOGGLE:
 		    if (!PrefUtil.readBoolean(ctxt,
 			    PrefConstants.WIFI_STATE_LOCK))
 			/*
 			 * Acquire Wake Lock
 			 */
 			if (wlock == null)
 			    wlock = new WakeLock(ctxt);
 		    wlock.lock(true);
 		    NotifUtil.show(ctxt,
 			    ctxt.getString(R.string.toggling_wifi), ctxt
 				    .getString(R.string.toggling_wifi),
 			    TOGGLE_ID, PendingIntent.getActivity(ctxt, 0,
 				    new Intent(), 0));
 		    PrefUtil.writeBoolean(ctxt, PrefConstants.WIFI_STATE_LOCK,
 			    true);
 		    hWifiState.sendEmptyMessageDelayed(OFF, SHORT);
 		    hWifiState.sendEmptyMessageDelayed(ON, TOGGLE_DELAY);
 		    hWifiState
 			    .sendEmptyMessageDelayed(WATCHDOG, WATCHDOG_DELAY);
 		    break;
 		}
 		super.handleMessage(msg);
 	    }
 
 	};
 
 	@Override
 	public void run() {
 	    hWifiState.sendEmptyMessage(TOGGLE);
 	}
     };
 
     private static WifiManager getWifiManager(final Context context) {
 	if (wm == null)
 	    wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
 	return wm;
     }
 
     private static void setWifiState(final Context context, final boolean state) {
 	getWifiManager(context).setWifiEnabled(state);
     }
 
     public void handleIntent(final Context context, final Intent intent) {
	ctxt = context.getApplicationContext();
 	/*
 	 * If Wifi is disabled, notify
 	 */
 	if (!getWifiManager(ctxt).isWifiEnabled()) {
 	    Toast.makeText(ctxt, ctxt.getString(R.string.wifi_is_disabled),
 		    Toast.LENGTH_LONG).show();
 	    return;
 	}
 
 	String action = intent.getAction();
 	/*
 	 * Dispatch intent commands to handler
 	 */
 
 	/*
 	 * Turn on WIFI
 	 */
 	if (action.equals(WIFI_ON))
 	    setWifiState(ctxt, true);
 	/*
 	 * Turn off Wifi
 	 */
 	else if (action.equals(WIFI_OFF))
 	    setWifiState(ctxt, false);
 	/*
 	 * Toggle Wifi
 	 */
 	else if (action.equals(TOGGLE_WIFI)) {
 	    Thread toggleThread = new Thread(new RToggleRunnable());
 	    toggleThread.start();
 	}
 	/*
 	 * Reassociate
 	 */
 	else if (action.equals(REASSOCIATE)) {
 	    Toast.makeText(ctxt, ctxt.getString(R.string.reassociating),
 		    Toast.LENGTH_LONG).show();
 	    ctxt.sendBroadcast(new Intent(WFConnection.USEREVENT));
 	    getWifiManager(ctxt).reassociate();
 	}
 
     }
 
     public WidgetHandler(final Context context) {
	ctxt = context.getApplicationContext();
     }
 
 }
