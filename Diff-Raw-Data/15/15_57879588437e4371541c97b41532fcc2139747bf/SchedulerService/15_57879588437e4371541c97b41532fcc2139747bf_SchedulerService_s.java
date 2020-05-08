 package edu.illinois.CS598rhk.services;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Notification;
 import android.app.Service;
 import android.bluetooth.BluetoothAdapter;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.os.Binder;
 import android.os.IBinder;
 import android.os.PowerManager;
 import android.util.Log;
 import edu.illinois.CS598rhk.MainActivity;
 import edu.illinois.CS598rhk.interfaces.IBluetoothService;
 import edu.illinois.CS598rhk.interfaces.ISchedulerService;
 import edu.illinois.CS598rhk.interfaces.IWifiService;
 import edu.illinois.CS598rhk.models.BluetoothNeighbor;
 import edu.illinois.CS598rhk.models.WifiNeighbor;
 
 public class SchedulerService extends Service implements ISchedulerService {
 	
 	private static final String TAG = "SchedulerService";
 
 	private final IBinder mBinder = new SchedulerBinder();
 	
 	private IWifiService wifiService;
 	private IBluetoothService bluetoothService;
 	
 	private MessageReceiver neighborReceiver = new MessageReceiver();
 	
 	private List<WifiNeighbor> wifiNeighbors;
 	private List<BluetoothNeighbor> bluetoothNeighbors;
 	
 	private BluetoothAdapter myDevice;
 	private long progress;
 	private String address;
 	private boolean stoppingWifi;
 	
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
     	Notification notification = new Notification();
     	notification.tickerText = "SchedulerService";
     	startForeground(1, notification);
 		
 		IntentFilter filter = new IntentFilter(WifiService.INTENT_TO_ADD_WIFI_NEIGHBOR);
 		registerReceiver(neighborReceiver, filter);
 		
 		filter = new IntentFilter(BluetoothService.INTENT_TO_ADD_BLUETOOTH_NEIGHBOR);
 		registerReceiver(neighborReceiver, filter);
 		
 		filter = new IntentFilter(WifiService.INTENT_TO_UPDATE_SCHEDULE_PROGRESS);
 		registerReceiver(neighborReceiver, filter);
 		
 		filter = new IntentFilter(BluetoothService.ACTION_ELECTED_FOR_WIFI_DISCOVERY);
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
 		
 		myDevice = BluetoothAdapter.getDefaultAdapter();
 		this.address = address;
 		stoppingWifi = false;
 		
 		return START_STICKY;
 	}
 	
 	public void onDestroy() {
 		wl.release();
 		unregisterReceiver(neighborReceiver);
 		unbindService(mBluetoothConnection);
 		unbindService(mWifiConnection);
 		stopForeground(true);
 		super.onDestroy();
 	}
 	
 	public void sendToLogger(String message) {
 		Log.d(TAG, message);
 		Intent intentToLog = new Intent(PowerManagement.ACTION_LOG_UPDATE);
 		intentToLog.putExtra(PowerManagement.LOG_MESSAGE, message);
 		sendBroadcast(intentToLog);
 	}
 	
 	private class MessageReceiver extends BroadcastReceiver {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			if (WifiService.INTENT_TO_ADD_WIFI_NEIGHBOR.equals(intent.getAction())) {
 				WifiNeighbor neighbor = new WifiNeighbor();
 				neighbor.unpack(intent.getByteArrayExtra(WifiService.WIFI_NEIGHBOR_DATA));
 				
 				sendToLogger("SchedulerService:"
 						+ "\n\tWifi neihgbor found, checking if new or not..."
 						+ "\n");
 				String sourceOfUpdate = intent.getStringExtra(WifiService.INTENT_TO_ADD_WIFI_NEIGHBOR_SOURCE);
 				if (!wifiNeighbors.contains(neighbor)) {
 					wifiNeighbors.add(neighbor);
 					if (WifiService.DISCOVERED_OVER_WIFI.equals(sourceOfUpdate)) {
 						bluetoothService.broadcast(neighbor);
 						sendToLogger("SchedulerService:"
 								+ "\n\tNew WiFi neighbor found over WiFi, broadcasting over Bluetooth"
 								+ "\n");
 					}
 				}
 			}
 			else if (BluetoothService.INTENT_TO_ADD_BLUETOOTH_NEIGHBOR.equals(intent.getAction())) {
 				BluetoothNeighbor neighbor = new BluetoothNeighbor();
 				neighbor.unpack(intent.getByteArrayExtra(BluetoothService.BLUETOOTH_NEIGHBOR_DATA));
 				
 				sendToLogger("SchedulerService:"
 						+ "\n\tReceived new Bluetooth neighbor"
 						+ "\n");
 				
 				if (neighbor.neighborCount > bluetoothNeighbors.size()) {
 					stoppingWifi = true;
 					wifiService.pauseWifiService();
 					sendToLogger("SchedulerService:"
 							+ "\n\tNew Bluetooth neighbor has " + (neighbor.neighborCount - bluetoothNeighbors.size())
 							+ " more neighbors than me. Stopping Wifi."
 							+ "\n");
 				}
 				else if (neighbor.neighborCount == bluetoothNeighbors.size()) {
					if (neighbor.progress < progress) {
 						stoppingWifi = true;
 						wifiService.pauseWifiService();
 						sendToLogger("SchedulerService:"
 								+ "\n\tNew Bluetooth neighbor has less schedule remaining. Stopping Wifi."
 								+ "\n");
					}
					else if (neighbor.address.compareTo(myDevice.getAddress()) < 0) {
 						stoppingWifi = true;
 						wifiService.pauseWifiService();
 						sendToLogger("SchedulerService:"
 								+ "\n\tNew Bluetooth neighbor has lower address. Stopping Wifi."
 								+ "\n");
 					}
 				}
 				
 				if (!bluetoothNeighbors.contains(neighbor)) {
 					bluetoothNeighbors.add(neighbor);
 					bluetoothService.updateNeighborCount(bluetoothNeighbors.size());
 					sendToLogger("SchedulerService:"
 							+ "\n\tNeighbor is a new neighbor!"
 							+ "\n\t" + neighbor
 							+ "\n");
 				}
 			}
 			else if (WifiService.INTENT_TO_UPDATE_SCHEDULE_PROGRESS.equals(intent.getAction())) {
 				progress = intent.getLongExtra(WifiService.SCHEDULE_PROGRESS_UPDATE, 0);
 				bluetoothService.updateScheduleProgress(progress);
 				if (bluetoothNeighbors.size() > 0) {
 					if (progress <= 10000 && progress > 0 && !stoppingWifi) {
 						bluetoothService.hostWifiDiscoveryElection();
 						sendToLogger("SchedulerService:"
 								+ "\n\tInitiating Wifi discovery election with remaining schedule"
 								+ String.valueOf(progress) + "\n");
 					}
 				}
 				else if (progress > 45000){
 					bluetoothService.updateNeighbors();
 					sendToLogger("SchedulerService:"
 							+ "\n\tNo Bluetooth neighbors, updating neighbors..."
 							+ "\n");
 				}
 			}
 			else if (BluetoothService.ACTION_ELECTED_FOR_WIFI_DISCOVERY.equals(intent.getAction())) {
 				Long delay = intent.getLongExtra(BluetoothService.DELY_UNTIL_STARTING_WIFI_DISCOVERY, 0);
 				
 				if (delay >= 0) {
 					stoppingWifi = false;
 					wifiService.resumeWifiService(delay);
 					sendToLogger("SchedulerService:"
 							+ "\n\tElected for WiFi Discovery in " + String.valueOf(delay)
 							+ "\n");
 				} else {
 					stoppingWifi = true;
 					wifiService.pauseWifiService();
 					sendToLogger("SchedulerService:"
 							+ "\n\tStopping discovery at end of current schedule"
 							+ "\n");
 				}
 			}
 		}
 	}
 }
