 package org.cvpcs.android.cwiidconfig.daemon;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.bluetooth.BluetoothAdapter;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.IBinder;
 
 import org.cvpcs.android.cwiidconfig.R;
 
 import org.cvpcs.android.cwiidconfig.activity.CWiiDConfig;
 
 public class CWiiDDaemonService extends Service {
 	public static final String ACTION_INTENT = "org.cvpcs.android.cwiidconfig.CWIID_DAEMON";
 
 	private final BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
 			
 			switch(state) {
 				case BluetoothAdapter.STATE_OFF:
 				case BluetoothAdapter.STATE_TURNING_OFF:
 					// stop the deamon
 					CWiiDManager.stopDaemon();
 					
 					// now stop ourselves, because we're done
 					stopSelf();
 					break;
 				default:
 					break;
 			}
 		}
 	};
 	
     @Override
     public void onCreate() {
     	String title = getString(R.string.cwiid_daemon_service_title);
     	String text = getString(R.string.cwiid_daemon_service_text);
 
        Notification notification = new Notification(R.drawable.app_icon, text,
                System.currentTimeMillis());
 
         PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                 new Intent(this, CWiiDConfig.class), 0);
 
         notification.setLatestEventInfo(this, title, text, contentIntent);
         
     	// notification time!
         startForeground(R.string.cwiid_daemon_service_title, notification);
 
         IntentFilter filter = new IntentFilter();
         filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
         registerReceiver(mBluetoothReceiver, filter);
     }
 
     @Override
     public void onDestroy() {
     	stopForeground(true);
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         return null;
     }
 }
