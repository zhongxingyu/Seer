 package ua.in.leopard.androidCoocooAfisha;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.IBinder;
 import android.widget.Toast;
 
 public class DataUpdateService extends Service {
 	//private static final String TAG = "DataUpdateService";
 	private Timer timer = new Timer();
 	private int hours_interval = 1;
 	
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 	
 	@Override
 	public void onCreate() {
 		//Toast.makeText(this, "My Service Created", Toast.LENGTH_LONG).show();
 		//Log.d(TAG, "DataUpdateService onCreate");
 		this.hours_interval = Integer.parseInt(EditPreferences.getAutoUpdateTime(this));
 		startTimer();
 	}
 
 	@Override
 	public void onDestroy() {
 		Toast.makeText(this, getString(R.string.autoupdate_service_stop), Toast.LENGTH_LONG).show();
 		//Log.d(TAG, "DataUpdateService onDestroy");
 		if (timer != null){
 			timer.cancel();
 		} 
 	}
 	
 	@Override
 	public void onStart(Intent intent, int startid) {
 		Toast.makeText(this, getString(R.string.autoupdate_service_start), Toast.LENGTH_LONG).show();
 		//Log.d(TAG, "DataUpdateService onStart");
 	}
 	
 	private void startTimer(){
		int timeout = 1000 * 3600 * this.hours_interval;
 		timer.scheduleAtFixedRate(
 			new TimerTask() {
 				public void run() {
					DataCollector dataCollectorObject = new DataCollector(getApplicationContext());
					dataCollectorObject.getTheatersData();
					dataCollectorObject.getCinemasData();
 				}
 			}, 
			timeout, timeout);
 	}
 
 }
