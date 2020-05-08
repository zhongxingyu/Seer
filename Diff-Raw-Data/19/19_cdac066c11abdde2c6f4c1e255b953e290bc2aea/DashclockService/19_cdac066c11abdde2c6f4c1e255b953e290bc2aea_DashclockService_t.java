 package net.homeip.tedk.dashclocknotifications;
 
 import java.util.ArrayList;
 import java.util.Collections;
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
 	
 	public static class NotificationInfo implements Comparable<NotificationInfo>{
 		public long time;
		public String packageName;
 		public String app;
 		public String tag;
 		public int id;
 		public int priority;
 		public String text;
 		public int num;
 		public Uri iconUri;
 		public PendingIntent intent;
		public NotificationInfo(long time, String packageName, String app, String tag, int id, int priority, String text, int num, Uri iconUri, PendingIntent intent) {
 			this.time = time;
			this.packageName = packageName;
 			this.app = app;
 			this.tag = tag;
 			this.id = id;
 			this.priority = priority;
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
			return this.packageName.equals(ni.packageName) && this.id == ni.id && (this.tag == null ? ni.tag == null : this.tag.equals(ni.tag));
 		}
 		public int compareTo(NotificationInfo that) {
		    final int BEFORE = -1;
 		    final int EQUAL = 0;
 		    final int AFTER = 1;
 		    
 		    if (this == that) return EQUAL;
 		    
 		    // these are backwards on purpose.  show higher priority first
 		    if (this.priority < that.priority) return AFTER;
 		    if (this.priority > that.priority) return BEFORE;
 		    
 		    if (this.time < that.time) return AFTER;
 		    if (this.time > that.time) return BEFORE;
 		    
 		    return EQUAL;
 		}
 	}
 	
 	private static List<NotificationInfo> notifications = new ArrayList<NotificationInfo>(50);
 	private static List<DashclockService> widgets = new ArrayList<DashclockService>(50);
 	
 	private static volatile int initializationCount = 0;
 	
 	private synchronized static void initialize(DashclockService service) {
 		widgets.add(service);
 		if(initializationCount == 0) {
 			service.startService(new Intent(service, NotificationService.class));
 		}
 		++initializationCount;
 	}
 	
 	private synchronized static void destroy(DashclockService service) {
 		widgets.remove(service);
 		--initializationCount;
 		if(initializationCount == 0) {
 			service.stopService(new Intent(service, NotificationService.class));
 		}
 	}
 	
 	public synchronized static void addNotification(NotificationInfo ni) {
 		notifications.remove(ni);
 		notifications.add(0, ni);
 		if(notifications.size() > 50) {
 			notifications.remove(50);
 		}
 		Collections.sort(notifications);
 		updateWidgets();
 	}
 	
 	public synchronized static void addNotifications(List<NotificationInfo> niList) {
 		notifications.removeAll(niList);
 		notifications.addAll(0, niList);
 		while(notifications.size() > 50) {
 			notifications.remove(50);
 		}
 		Collections.sort(notifications);
 		updateWidgets();
 	}
 	
 	public synchronized static void removeNotification(NotificationInfo ni) {
 		notifications.remove(ni);
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
 		destroy(this);
 		super.onDestroy();
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
 			ExtensionData ed = new ExtensionData();
 			ed.visible(true);
 			ed.iconUri(ni.iconUri);
 			ed.status(ni.text == null ? ni.app : ni.text);
 			ed.expandedTitle(ni.app);
 			if(ni.text != null)
 				ed.expandedBody(ni.text);
 			if(ni.intent != null)
 				ed.clickIntent(new Intent(Intent.ACTION_VIEW, Uri.parse("dashclocknotifications://" + ni.intent.getCreatorPackage())));
 			publishUpdate(ed);
 		}
 	}
 	
 }
