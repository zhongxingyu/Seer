 package net.homeip.tedk.dashclocknotifications;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Notification;
 import android.app.PendingIntent;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.net.Uri;
 import android.service.notification.NotificationListenerService;
 import android.service.notification.StatusBarNotification;
 import android.util.Log;
 
 public class NotificationService extends NotificationListenerService {
 
 //	private DateFormat dateFormat = null;
 	private PackageManager pm = null;
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		setUp();
 		super.onStartCommand(intent, flags, startId);
 		initNotifications();
 //		registerReciever();
 		return START_STICKY;
 	}
 	
 	private void initNotifications() {
 		StatusBarNotification[] events = null;
 		try {
 			events = getActiveNotifications();
 		} catch (Exception e) {
 			Log.e("NotificationListenerService", "Exception trying to get active notifications", e);
 		}
 		if(events == null)
 			return;
 		List<DashclockService.NotificationInfo> niList = new ArrayList<DashclockService.NotificationInfo>(events.length);
 		for(StatusBarNotification event : events)
 		{
 			DashclockService.NotificationInfo ni = getNotificationInfo(event);
 			if(ni != null)
 				niList.add(ni);
 		}
 		DashclockService.addNotifications(niList);
 	}
 	
 //	private void registerReciever() {
 //		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
 //		filter.addAction(Intent.ACTION_USER_PRESENT);
 //		BroadcastReceiver br = new ScreenReceiver();
 //		registerReceiver(br, filter);
 //	}
 
 	public void setUp() {
 //		dateFormat = DateFormat.getTimeInstance(DateFormat.LONG);
 		pm = getPackageManager();
 	}
 	
 	public void tearDown() {
 //		dateFormat = null;
 		pm = null;
 	}
 
 	@Override
 	public void onDestroy() {
 		tearDown();
 		super.onDestroy();
 	}
 
 	@Override
 	public void onNotificationPosted(StatusBarNotification event) {
 //		if(!ScreenReceiver.locked)
 //			return;
 		DashclockService.NotificationInfo ni = getNotificationInfo(event);
 		if(ni != null)
 			DashclockService.addNotification(ni);
 	}
 	
 	@Override
 	public void onNotificationRemoved(StatusBarNotification event) {
 		DashclockService.NotificationInfo ni = getNotificationInfo(event);
 		if(ni != null)
 			DashclockService.removeNotification(ni);
 	}
 	
 	private DashclockService.NotificationInfo getNotificationInfo(StatusBarNotification event) {
 		if (event == null)
 			return null;
 		Notification n = (Notification) event.getNotification();
 		if (n == null)
 			return null;
 
 		String text = n.tickerText == null || n.tickerText.toString().trim().length() == 0 ? null : n.tickerText.toString().trim();
 		
 //		if (text == null)
 //			return null; // ignore blank notifications (downloads, gps, keyboard, etc.)
 
 		long time = n.when;
 		String packageName = event.getPackageName().toString();
 		String appName = null;
 		try {
 			appName = pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString();
 		} catch (Exception e) {
 			Log.e("NotificationListenerService", "Could not load application name", e);
 		}
 		if (appName == null)
 			appName = packageName;
 		int num = n.number;
 //		Context c = getApplicationContext();
 //		try {
 //			c = createPackageContext(packageName, 0);
 //		} catch (NameNotFoundException e) {
 //			Log.e("NotificationListenerService", "Could not load application context", e);
 //		}
 		int icon = n.icon;
 		Uri iconUri = Uri.withAppendedPath(IconProvider.CONTENT_URI, packageName + "/" + icon);
 //		ByteArrayOutputStream baos = null;
 //		try {
 //			baos = new ByteArrayOutputStream();
 //			BitmapFactory.Options options = new BitmapFactory.Options();
 //			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
 //			Bitmap iconBitmap = BitmapFactory.decodeResource(c.getResources(), n.icon, options);
 //			Bitmap iconWithBackground = Bitmap.createBitmap(iconBitmap.getWidth(), iconBitmap.getHeight(), iconBitmap.getConfig());
 //			Canvas background = new Canvas(iconWithBackground);
 //			background.drawColor(Color.BLACK);
 //			background.drawBitmap(iconBitmap, 0.f, 0.f, null);
 //			iconWithBackground.compress(Bitmap.CompressFormat.PNG, 100, baos);
 //			baos.flush();
 //			icon = "data:image/png;base64," + Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP | Base64.URL_SAFE);
 //		} catch (Exception e) {
 //			Log.e("NotificationListenerService", "Could not load icon", e);
 //		} finally {
 //			if (baos != null)
 //				try {
 //					baos.close();
 //				} catch (Exception e) {
 //				}
 //		}
 		PendingIntent intent = n.contentIntent;
 		int priority = n.priority;
		return new DashclockService.NotificationInfo(time, appName, event.getTag(), event.getId(), priority, text, num, iconUri, intent);
 	}
 
 }
