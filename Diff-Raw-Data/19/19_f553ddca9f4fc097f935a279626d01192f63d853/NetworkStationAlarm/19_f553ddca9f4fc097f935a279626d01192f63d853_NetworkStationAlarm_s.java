 package com.poguico.palmabici.network.synchronizer;
 
 import java.util.ArrayList;
 import java.util.concurrent.Semaphore;
 
 import com.poguico.palmabici.MainActivity;
 import com.poguico.palmabici.R;
 import com.poguico.palmabici.util.NetworkInformation;
 
 import android.app.IntentService;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.support.v4.app.NotificationCompat;
 import android.support.v4.app.TaskStackBuilder;
 import android.util.Log;
 
 public class NetworkStationAlarm extends IntentService {
 
 	private static ArrayList<String> stationAlarms = null;
 	private static boolean active = false;
 	
 	private NetworkInformation networkInformation;
 	private static Context context;
 	private Semaphore semaphore;
 	
 	public NetworkStationAlarm() {
 		super("NetworkStationAlarm");
 		Log.i("NetworkStationAlarm", "Initializing class");
 		active = true;
 		networkInformation = NetworkInformation.getInstance(context);
 		semaphore = new Semaphore(1);
 	}
 
 	public static synchronized void addAlarm(Context context, String stationId) {
 		if (stationAlarms == null) {
 			Log.i("NetworkStationAlarm", "Initializing alarms list");
 			NetworkStationAlarm.context = context;
 			stationAlarms = new ArrayList<String>();
 		}
 		Log.i("NetworkStationAlarm", "Adding alarm for station " + stationId);
 		stationAlarms.add(stationId);
 	}
 	
 	public static synchronized void removeAlarm(String stationId) {
 		if (stationAlarms.contains(stationId)) {
 			stationAlarms.remove(stationId);
 			Log.i("NetworkStationAlarm", "Alarm for station " + stationId + " removed");
 		}
 	}
 	
 	@Override
 	protected void onHandleIntent(Intent arg0) {
 		Log.i("NetworkStationAlarm", "Starting thread");
 		while (!NetworkStationAlarm.stationAlarms.isEmpty()) {
 			Log.i("NetworkStationAlarm", "Getting network info...");
 			NetworkSynchronizerTask.synchronize(context, new NetworkSyncCallback() {
 				
 				@Override
 				public void onNetworkSynchronized(long updateTime) {
 					Log.i("NetworkStationAlarm", "Network synchronized");
 					for (String id : stationAlarms) {
 						Log.i("NetworkStationAlarm", "Station " + id + " has " + networkInformation.get(id).getBusySlots() + "bikes available");
 						if (networkInformation.get(id).getBusySlots() > 0) {
 							//notify
 							removeAlarm(id);
 						}
 					}
 					semaphore.release();
 				}
 				
 				@Override
 				public void onNetworkError(String errorCode) {
 					Log.i("NetworkStationAlarm", "Error when synchornizing");
 					semaphore.release();
 				}
 			});
 			
 			try {
 				semaphore.acquire();
 				Thread.sleep(30000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		Log.i("NetworkStationAlarm", "Finishing thread");
 		showNotification();
 	}
 
 	@Override
 	public void onDestroy() {
 		Log.i("NetworkStationAlarm", "Destroying class");
 		active = false;
 		super.onDestroy();
 	}
 	
 	public void showNotification() {
 		Bitmap bigIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.palmabici_bw);
 		Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
 		
		Intent resultIntent = new Intent(this, MainActivity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(MainActivity.class);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
 		
 		NotificationCompat.Builder mBuilder =
 		        new NotificationCompat.Builder(this)
 		        .setSmallIcon(R.drawable.bike)
 		        .setLargeIcon(bigIcon)
 		        .setContentTitle("AlarmService")
 		        .setContentText("Alarm service finished!")
 		        .setLights(0x0000ff00, 1000, 1000)
 		        .setTicker("Alarm service finished!")
		        .setSound(uri);
 		
 		NotificationManager mgr=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
 
		mBuilder.setContentIntent(resultPendingIntent);
 		mgr.notify(1234, mBuilder.build());
 	}
 
 	public static boolean isActive() {
 		return active;
 	}
 	
 	public static boolean hasAlarm(String id) {
 		return stationAlarms != null && stationAlarms.contains(id);
 	}
 }
