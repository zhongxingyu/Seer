 package edu.illinois.CS598rhk.services;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.os.Binder;
 import android.os.IBinder;
 import android.os.PowerManager;
 import edu.illinois.CS598rhk.MainActivity;
 import edu.illinois.CS598rhk.interfaces.IBluetoothService;
 import edu.illinois.CS598rhk.interfaces.ISchedulerService;
 import edu.illinois.CS598rhk.interfaces.IWifiService;
 import edu.illinois.CS598rhk.models.BluetoothNeighbor;
 import edu.illinois.CS598rhk.models.WifiNeighbor;
 
 public class SchedulerService extends Service implements ISchedulerService {
 	
 	private final IBinder mBinder = new SchedulerBinder();
 	
 	private IWifiService wifiService;
 	private IBluetoothService bluetoothService;
 	
 	private MessageReceiver neighborReceiver = new MessageReceiver();
 	
 	private List<WifiNeighbor> wifiNeighbors;
 	private List<BluetoothNeighbor> bluetoothNeighbors;
 	private PowerManager pm;
 	private PowerManager.WakeLock wl;
 	
 	@Override
 	public IBinder onBind(Intent arg0) {
 		return mBinder;
 	}
 
 	public class SchedulerBinder extends Binder {
         public ISchedulerService getService() {
             return SchedulerService.this;
         }
     }
 	
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		wifiNeighbors = new ArrayList<WifiNeighbor>();
 		bluetoothNeighbors = new ArrayList<BluetoothNeighbor>();
 	}
 	
 	private ServiceConnection mWifiConnection = new ServiceConnection()
     {
 		@Override
 		public void onServiceConnected(ComponentName name, IBinder service) {
 			wifiService = ( (WifiService.WifiBinder) service ).getService();
 		}
 
 		@Override
 		public void onServiceDisconnected(ComponentName name) {
 			wifiService = null;	
 		}
     };
     
 	private ServiceConnection mBluetoothConnection = new ServiceConnection()
     {
 		@Override
 		public void onServiceConnected(ComponentName name, IBinder service) {
 			bluetoothService = ( (BluetoothService.BluetoothBinder) service ).getService();
 		}
 
 		@Override
 		public void onServiceDisconnected(ComponentName name) {
 			bluetoothService = null;
 		}
     };
 	
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		IntentFilter filter = new IntentFilter(WifiService.INTENT_TO_ADD_WIFI_NEIGHBOR);
 		registerReceiver(neighborReceiver, filter);
 		
 		filter = new IntentFilter(BluetoothService.INTENT_TO_ADD_BLUETOOTH_NEIGHBOR);
 		registerReceiver(neighborReceiver, filter);
 		
 		bindService( new Intent( SchedulerService.this, WifiService.class ), mWifiConnection, Context.BIND_AUTO_CREATE );
 		bindService( new Intent( SchedulerService.this, BluetoothService.class ), mBluetoothConnection, Context.BIND_AUTO_CREATE );
 		
 		String name = intent.getStringExtra(MainActivity.NAME_KEY);
 		String address = intent.getStringExtra(MainActivity.ADDRESS_KEY);
 		
 		Intent i = new Intent(SchedulerService.this, WifiService.class);
 		i.putExtra(MainActivity.NAME_KEY, name);
 		i.putExtra(MainActivity.ADDRESS_KEY, address);
 		startService(i);
 		
 		i = new Intent(SchedulerService.this, BluetoothService.class);
 		i.putExtra(MainActivity.NAME_KEY, name);
 		startService(i);
 		
 		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
 		wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");
 		wl.acquire();
 
 		return START_STICKY;
 	}
 	
 	public void onDestroy() {
 		wl.release();
 		unregisterReceiver(neighborReceiver);
 		unbindService(mBluetoothConnection);
 		unbindService(mWifiConnection);
 	}
 	
 	private class MessageReceiver extends BroadcastReceiver {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			if (WifiService.INTENT_TO_ADD_WIFI_NEIGHBOR.equals(intent.getAction())) {
 				WifiNeighbor neighbor = new WifiNeighbor();
 				neighbor.name = intent.getStringExtra(WifiService.WIFI_NEIGHBOR_NAME);
 				neighbor.address = intent.getStringExtra(WifiService.WIFI_IP_ADDRESS);
 				
 				if (!wifiNeighbors.contains(neighbor)) {
 					wifiNeighbors.add(neighbor);
					bluetoothService.broadcast(new String(neighbor.getBytes()));
 				}
 			}
 			else if (BluetoothService.INTENT_TO_ADD_BLUETOOTH_NEIGHBOR.equals(intent.getAction())) {
 				BluetoothNeighbor neighbor = BluetoothNeighbor.parseByteArray(intent.getByteArrayExtra(BluetoothService.BLUETOOTH_NEIGHBOR_DATA));
 				
 				if (!bluetoothNeighbors.contains(neighbor)) {
 					bluetoothNeighbors.add(neighbor);
 				}
 			}
 			else if (WifiService.INTENT_TO_UPDATE_SCHEDULE_PROGRESS.equals(intent.getAction())) {
 				bluetoothService.updateScheduleProgress(intent.getIntExtra(WifiService.SCHEDULE_PROGRESS_UPDATE, 0));
 			}
 		}
 	}
 }
