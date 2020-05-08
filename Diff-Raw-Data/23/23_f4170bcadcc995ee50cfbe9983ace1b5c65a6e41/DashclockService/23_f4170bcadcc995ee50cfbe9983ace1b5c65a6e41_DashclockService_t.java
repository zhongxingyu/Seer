 package net.homeip.tedk.dashclocknotifications;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.PendingIntent;
 import android.content.ComponentName;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.content.pm.ServiceInfo;
 import android.net.Uri;
 import android.util.Log;
 
 import com.google.android.apps.dashclock.api.DashClockExtension;
 import com.google.android.apps.dashclock.api.ExtensionData;
 
 public class DashclockService extends DashClockExtension {
 	
 	public static class NotificationInfo {
 		public String time;
 		public String app;
 		public String text;
 		public int num;
 		public Uri iconUri;
 		public PendingIntent intent;
 		public NotificationInfo(String time, String app, String text, int num, Uri iconUri, PendingIntent intent) {
 			this.time = time;
 			this.app = app;
 			this.text = text;
 			this.num = num;
 			this.iconUri = iconUri;
 			this.intent = intent;
 		}
 		@Override
 		public boolean equals(Object that) {
 			if(that == null)
 				return false;
 			if(this == that)
 				return true;
 			if(this.getClass() != that.getClass())
 				return false;
 			
 			NotificationInfo ni = (NotificationInfo) that;
 			return this.app.equals(ni.app) && this.text.equals(ni.text);
 		}
 	}
 	
 	private static List<NotificationInfo> notifications = new ArrayList<NotificationInfo>();
 	private static List<DashclockService> widgets = new ArrayList<DashclockService>();
 	
 	private static volatile int initializationCount = 0;
 	
	private synchronized static void initialize(DashclockService service) {
		widgets.add(service);
 		if(initializationCount == 0) {
			service.startService(new Intent(service, NotificationService.class));
 		}
 		++initializationCount;
 	}
 	
	private synchronized static void destroy(DashclockService service) {
 		--initializationCount;
 		if(initializationCount == 0) {
			service.stopService(new Intent(service, NotificationService.class));
 		}
 	}
 	
 	public synchronized static void addNotification(NotificationInfo ni) {
 		removeNotification(ni);
 		notifications.add(0, ni);
 		if(notifications.size() > 50) {
 			notifications.remove(50);
 		}
 		updateWidgets();
 	}
 	
 	public synchronized static void removeNotification(NotificationInfo ni) {
 		int index = notifications.indexOf(ni);
 		if(index >= 0 && index < notifications.size()) {
 			notifications.remove(index);
 		}
		updateWidgets();
 	}
 	
 	private synchronized static NotificationInfo getNotification(int num) {
 		return num >= notifications.size() ? null : notifications.get(num);
 	}
 	
 	public synchronized static void clearNotifications() {
 		notifications.clear();
 		updateWidgets();
 	}
 	
	private static synchronized void updateWidgets() {
 		for(DashclockService ds : widgets) {
 			ds.updateWidget();
 		}
 	}
 	
 	private int widgetNum = -1;
 
 	@Override
 	protected void onInitialize(boolean isReconnect) {
 		super.onInitialize(isReconnect);
 		PackageManager pm = getPackageManager();
 		try {
 			ServiceInfo si = pm.getServiceInfo(new ComponentName(this.getApplicationContext(), this.getClass()), 0);
 			String label = si.loadLabel(pm).toString();
 			widgetNum = Integer.parseInt(label.substring(label.indexOf('#') + 1)) - 1;
 		} catch (NameNotFoundException e) {
 			Log.e("DashclockService", "Could not get ServiceInfo", e);
 		}	
 		initialize(this);
 	}
 	
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		destroy(this);
 	}
 	
 	@Override
 	protected void onUpdateData(int arg0) {
 		updateWidget();
 	}
 	
 	public synchronized void updateWidget() {
 		NotificationInfo ni = getNotification(widgetNum);
 		if(ni == null) {
 			publishUpdate(new ExtensionData()
     		.visible(false));
 		} else {
 			String body = ni.text;
 			publishUpdate(new ExtensionData()
         		.visible(true)
         		.iconUri(ni.iconUri)
         		.status(ni.text)
         		.expandedTitle(ni.app)
         		.expandedBody(body));
         		//.clickIntent(new Intent(Intent.ACTION_VIEW, Uri.parse("dashclocknotifications://" + ni.intent.getCreatorPackage()))));
 		}
 	}
 	
 }
