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
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.net.wifi.WifiManager;
 import android.util.Log;
 import android.widget.RemoteViews;
 
 public class NotifUtil {
     private static final int NETNOTIFID = 8236;
     private static final int STATNOTIFID = 2392;
     private static final int MAX_SSID_LENGTH = 10;
     public static final String CANCEL = "CANCEL";
 
     private NotifUtil() {
 
     }
 
     public static void addNetNotif(final Context context, final String ssid,
 	    final String signal) {
 	NotificationManager nm = (NotificationManager) context
 		.getSystemService(Context.NOTIFICATION_SERVICE);
 
 	Intent intent = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
 	PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
 		intent, 0);
 
 	Notification netnotif = new Notification(R.drawable.wifi_ap, context
 		.getString(R.string.open_network_found), System
 		.currentTimeMillis());
 	if (ssid.length() > 0) {
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
 
     public static RemoteViews getStatusPane(final Context context,
 	    final String ssid, final String status, final int signal,
 	    RemoteViews statnotif) {
 
 	if (statnotif == null) {
 	    statnotif = createStatView(context, ssid, status, signal);
 	}
 
 	return statnotif;
     }
 
     public static Notification addStatNotif(final Context context,
 	    final String ssid, final String status, final int signal,
 	    final boolean flag, Notification notif, RemoteViews sview) {
 	NotificationManager nm = (NotificationManager) context
 		.getSystemService(Context.NOTIFICATION_SERVICE);
 
 	PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
 		new Intent(context, WifiFixerActivity.class), 0);
 
 	notif = new Notification(R.drawable.router32, context
 		.getString(R.string.network_status), System.currentTimeMillis());
 
 	if (flag) {
 	    notif.contentView = sview;
 	    notif.contentIntent = contentIntent;
 	    notif.flags = Notification.FLAG_ONGOING_EVENT;
 	    /*
 	     * Fire notification, cancel if message empty: means no status info
 	     */
 	    nm.notify(STATNOTIFID, notif);
 	} else {
 	    nm.cancel(STATNOTIFID);
 	    return null;
 	}
 	return notif;
     }
 
     public static RemoteViews createStatView(final Context context,
 	    final String ssid, final String status, final int signal) {
 
 	RemoteViews contentView = new RemoteViews(context.getPackageName(),
 		R.layout.status_notif_layout);
 
 	contentView.setTextViewText(R.id.ssid, truncateSSID(ssid));
 	contentView.setTextViewText(R.id.status, status);
 	contentView.setTextColor(R.id.status, Color.BLACK);
 	contentView.setTextColor(R.id.ssid, Color.BLACK);
 	contentView.setImageViewResource(R.id.signal, signal);
 	return contentView;
     }
 
     public static void updateStatView(final Context context, final String ssid,
 	    final String status, final int signal, RemoteViews statnotif) {
 	if (statnotif == null)
 	    statnotif = getStatusPane(context, ssid, status, signal, statnotif);
 
 	statnotif.setTextViewText(R.id.ssid, truncateSSID(ssid));
 	statnotif.setTextViewText(R.id.status, status);
 	statnotif.setImageViewResource(R.id.signal, signal);
 
     }
 
     public static void updateStatNotif(final Context context,
 	    final String ssid, final String status, final int signal, Notification notif, RemoteViews statnotif) {
 	updateStatView(context, ssid, status, signal, statnotif);
 	
 	if (notif == null)
 	    notif = addStatNotif(context, status, status, signal, true, notif, statnotif);
 	
 	NotificationManager nm = (NotificationManager) context
 		.getSystemService(Context.NOTIFICATION_SERVICE);
 	nm.notify(STATNOTIFID, notif);
     }
 
     public static void setSSIDColor(final Context context, final int color, RemoteViews statnotif) {
 	statnotif.setTextColor(R.id.ssid, color);
     }
 
     public static void show(final Context context, final String message,
 	    final String tickerText, final int id,
 	    final PendingIntent contentIntent) {
 
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
 }
