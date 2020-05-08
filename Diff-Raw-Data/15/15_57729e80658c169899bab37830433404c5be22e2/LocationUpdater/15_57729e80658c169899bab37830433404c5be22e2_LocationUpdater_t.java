 package tb14.walkbasehackathon;
 
 import java.io.IOException;
 import java.util.List;
 
 import tb14.walkbasehackathon.DAOs.LocationDAO;
 import tb14.walkbasehackathon.DTO.Task;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.pm.PackageManager;
 import android.media.MediaPlayer;
 import android.os.PowerManager;
 import android.util.Log;
 
 import com.walkbase.location.WBLocation;
 import com.walkbase.location.WBLocationManager;
 import com.walkbase.location.listeners.WBLocationListener;
 
 public class LocationUpdater extends BroadcastReceiver implements WBLocationListener  {
 
 	final public static String TAG = "WBH BG Update";
 	final static long INTERVAL = 10000;
 	final static int range =10;
 	private WBLocationManager locationmanager;
 	private SharedPreferences prefs;
 	private Editor editor;
 	private PowerManager.WakeLock wl;
 	private LocationDAO dao;
 	private Context context;
 	@Override
 	public void onReceive(Context context, Intent arg1) {
 		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
 		wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "YOUR TAG");
 		this.context=context;
 		locationmanager = WBLocationManager.getWBLocationManager();
 		locationmanager.setApiKey("9ew2ucuohe67381nbwbfbw9sbb9");
 		locationmanager.setWBLocationListener(this);
 		dao = new LocationDAO(context);
 		dao.open();
 		 prefs = (SharedPreferences) context.getSharedPreferences("WBHPrefs", Context.MODE_PRIVATE);
 		 editor = prefs.edit();
 		
 		//Acquire the lock
 		wl.acquire();
 		locationmanager.fetchLastKnownUserLocation(context);		
 	}
 	
 	public void setAlarm(Context context){
 		AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
 		Intent intent = new Intent(context, LocationUpdater.class);
 		PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
 		
 		am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), INTERVAL , pi);
 	}
 	
 	public void cancelAlarm(Context context) {
 	        Intent intent = new Intent(context, LocationUpdater.class);
 	        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
 	        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
 	        alarmManager.cancel(sender);
 	    }
 
 	
 	@Override
 	public void lastKnownLocationWasRetrieved(WBLocation wbLocation) {
 		boolean locationNotFound=true;
 		List<Task> tasks = dao.getAllTask();
 		Log.v(TAG,"Begin Loop");
		String previous = prefs.getString("previousLocation", "");
 		for (Task task : tasks) {
 			double distance = getDistance(task.getLocation().getLatitude(), task.getLocation().getLongitude(), wbLocation.getLatitude(),wbLocation.getLongitude());
 			Log.v(TAG,task.getLocation().getName()+ " : "+ String.valueOf(distance));
			if (range > distance) {
 				locationNotFound=false;
				if (task.getLocation() != null  && !previous.equals(task.getTask())) {

					editor.putString("previousLocation", task.getTask());
					editor.commit();
					switch(task.getType()){
 					case 0 :
 						PackageManager pm = context.getPackageManager();
 						Intent appStartIntent = pm.getLaunchIntentForPackage(task.getTask());
 						if (null != appStartIntent) {
 							context.startActivity(appStartIntent);
 						}break;
 					case 1 :				
 						MediaPlayer mediaPlayer = new MediaPlayer();
 						try {
 							mediaPlayer.setDataSource(task.getTask());
 							mediaPlayer.prepare();
 							mediaPlayer.start();
 						} catch (IllegalArgumentException e) {
 							e.printStackTrace();
 						} catch (SecurityException e) {
 							e.printStackTrace();
 						} catch (IllegalStateException e) {
 							e.printStackTrace();
 						} catch (IOException e) {
 							e.printStackTrace();
 						}
 						break;
 					case 2:
 						break;
 					case 3:
 						break;
					}
 				}
 			}
 		}
 		Log.v(TAG,"End Loop");
 		if (prefs.getLong("timestamp", 1) != wbLocation.getTimestamp()) {
 			editor.putFloat("latitude", (float) wbLocation.getLatitude());
 			editor.putFloat("longitude", (float) wbLocation.getLongitude());
 			editor.putLong("accuracy", (long) wbLocation.getAccuracy());
 			editor.putLong("timestamp", (long) wbLocation.getTimestamp());
 			editor.commit();
 		}
 		if(locationNotFound){
 			editor.putString("previousLocation", "");
 			editor.commit();
 		}
 		wl.release();
 	}
 
 	
 	private double getDistance(double lat_a, double lng_a, double lat_b, double lng_b) {
 		double pk = (180 / 3.14169);
 
 		double a1 = lat_a / pk;
 		double a2 = lng_a / pk;
 		double b1 = lat_b / pk;
 		double b2 = lng_b / pk;
 		double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
 		double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
 		double t3 = Math.sin(a1) * Math.sin(b1);
 		double tt = Math.acos(t1 + t2 + t3);
 
 		return 6366000 * tt;
 	}
 
 	
 	
 	
 	@Override
 	public void errorFetchingLastKnownLocation(String arg0, int arg1) {
 		// TODO Auto-generated method stub
 		wl.release();	
 	}
 	
 	@Override
 	public void successfullyStartedTheLiveLocationFeed() {
 		// NOT FUNCTIONING = NOT USING
 	}
 
 	@Override
 	public void liveLocationFeedWasClosed() {
 		// NOT FUNCTIONING = NOT USING
 	}
 
 	@Override
 	public void liveLocationWasUpdated(WBLocation arg0) {
 		// NOT FUNCTIONING = NOT USING
 	}
 
 	@Override
 	public void errorFetchingLiveLocationFeed(String arg0, int arg1) {
 		// NOT FUNCTIONING = NOT USING
 	}
 	
 }
