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
 
 import android.app.ActivityManager;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.net.wifi.WifiManager;
 import android.widget.RemoteViews;
 
 public class NotifUtil {
     private static final int NETNOTIFID = 8236;
     private static final int STATNOTIFID = 2392;
     private static final int MAX_SSID_LENGTH = 16;
     private static int ssidColor = Color.BLACK;
     private static int lastbitmap;
     private static int choke;
     private static ActivityManager am;
     private static ActivityManager.MemoryInfo mInfo;
 
     /*
      * To clarify what I'm doing here: NotificationManager has a memory leak so
      * to preserve some semblance of battery life I'm limiting cached writes to
      * the choke threshold set by the limiter method based on available memory
      * then resetting the objects
      */
 
     private static volatile Notification statnotif;
     private static volatile RemoteViews statview;
 
     private NotifUtil() {
 
     }
 
     public static void addNetNotif(final Context context, final String ssid,
 	    final String signal) {
 	NotificationManager nm = (NotificationManager) context
 		.getSystemService(Context.NOTIFICATION_SERVICE);
 
 	if (ssid.length() > 0) {
 	    Intent intent = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
 	    PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
 		    intent, 0);
 
 	    Notification netnotif = new Notification(R.drawable.wifi_ap,
 		    context.getString(R.string.open_network_found), System
 			    .currentTimeMillis());
 	    RemoteViews contentView = new RemoteViews(context.getPackageName(),
 		    R.layout.net_notif_layout);
 	    contentView.setTextViewText(R.id.ssid, ssid);
 	    contentView.setTextViewText(R.id.signal, signal);
 	    netnotif.contentView = contentView;
 	    netnotif.contentIntent = contentIntent;
 	    netnotif.flags = Notification.FLAG_ONGOING_EVENT;
 	    netnotif.tickerText = context.getText(R.string.open_network_found);
 	    /*
 	     * Fire notification, cancel if message empty: means no open APs
 	     */
 	    nm.notify(NETNOTIFID, netnotif);
 	} else
 	    nm.cancel(NETNOTIFID);
 
     }
 
     public static void addStatNotif(final Context context, final String ssid,
 	    final String status, final int signal, final boolean flag,
 	    final int layout) {
 
 	NotificationManager nm = (NotificationManager) context
 		.getSystemService(Context.NOTIFICATION_SERVICE);
 
 	if (flag) {
 	    /*
 	     * Choke to avoid memory leak in NotificationManager
 	     */
 	    int choke_threshold = getLimiter(context);
 	    if (choke > choke_threshold) {
 		/*
 		 * Reclaim the objects
 		 */
 		statnotif = null;
 		statview = null;
 	    }
 
 	    if (statnotif == null) {
 		/*
		 * Reset Choke Recreate Notification
 		 */
 		choke = 0;
 		statnotif = new Notification(R.drawable.router32, context
 			.getString(R.string.network_status), System
 			.currentTimeMillis());
 
 		Intent intent = new Intent(context, WifiFixerActivity.class)
 			.setAction(Intent.ACTION_MAIN).setFlags(
 				Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
 
 		PendingIntent contentIntent = PendingIntent.getActivity(
 			context, 0, intent, 0);
 		statnotif.contentIntent = contentIntent;
 		statnotif.flags = Notification.FLAG_ONGOING_EVENT;
 	    }
 
 	    if (statview == null) {
 		/*
 		 * Recreate status view
 		 */
 		statview = createStatView(context, ssid, status, signal, layout);
 		statnotif.contentView = statview;
 	    } else {
 		/*
 		 * Cached and setting object values in RemoteView
 		 */
 		choke++;
 		statview.setTextViewText(R.id.ssid, truncateSSID(ssid));
 		statview.setTextViewText(R.id.status, status);
 		statview.setTextColor(R.id.ssid, ssidColor);
 		if (lastbitmap != signal)
 		    statview.setImageViewResource(R.id.signal, signal);
 	    }
 	    /*
 	     * false is cancel
 	     */
 	    nm.notify(STATNOTIFID, statnotif);
 	} else {
 	    nm.cancel(STATNOTIFID);
 	    statnotif = null;
 	    statview = null;
 	    choke = 0;
 	}
     }
 
     public static RemoteViews createStatView(final Context context,
 	    final String ssid, final String status, final int signal,
 	    final int layout) {
 
 	RemoteViews contentView = new RemoteViews(context.getPackageName(),
 		layout);
 
 	contentView.setTextViewText(R.id.ssid, truncateSSID(ssid));
 	contentView.setTextViewText(R.id.status, status);
 	contentView.setTextColor(R.id.ssid, ssidColor);
 	contentView.setImageViewResource(R.id.signal, signal);
 	lastbitmap = signal;
 	return contentView;
     }
 
     public static void setSSIDColor(final int color) {
 	ssidColor = color;
     }
 
     public static void show(final Context context, final String message,
	    final String tickerText, final int id, PendingIntent contentIntent) {

	/*
	 * If contentIntent is NULL, create valid contentIntent
	 */
	if (contentIntent == null)
	    contentIntent = PendingIntent.getActivity(context, 0, new Intent(),
		    0);
 
 	NotificationManager nm = (NotificationManager) context
 		.getSystemService(Context.NOTIFICATION_SERVICE);
 
 	CharSequence from = context.getText(R.string.app_name);
 
 	Notification notif = new Notification(R.drawable.statusicon,
 		tickerText, System.currentTimeMillis());
 
 	notif.setLatestEventInfo(context, from, message, contentIntent);
 	notif.flags = Notification.FLAG_AUTO_CANCEL;
 	// unique ID
 	nm.notify(id, notif);
 
     }
 
     public static String truncateSSID(String ssid) {
 	if (ssid.length() < MAX_SSID_LENGTH)
 	    return ssid;
 	else
 	    return ssid.substring(0, MAX_SSID_LENGTH);
 
     }
 
     public static void cancel(final int notif, final Context context) {
 	NotificationManager nm = (NotificationManager) context
 		.getSystemService(Context.NOTIFICATION_SERVICE);
 	nm.cancel(notif);
     }
 
     public static int getLimiter(final Context context) {
 	if (am == null) {
 	    am = (ActivityManager) context
 		    .getSystemService(Context.ACTIVITY_SERVICE);
 	    mInfo = new ActivityManager.MemoryInfo();
 	}
 	am.getMemoryInfo(mInfo);
 	/*
 	 * Limit choke threshold based on amount of free memory
 	 */
 	return (int) (mInfo.availMem / 20000000);
 
     }
 }
