 package edu.illinois.CS598rhk.services;
 
 import java.io.IOException;
 import java.io.InterruptedIOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.wifi.WifiManager;
 import android.os.Binder;
 import android.os.IBinder;
 import android.os.Looper;
 import android.util.Log;
 import edu.illinois.CS598rhk.MainActivity;
 import edu.illinois.CS598rhk.interfaces.IWifiService;
 import edu.illinois.CS598rhk.schedules.AlwaysSchedule;
 import edu.illinois.CS598rhk.schedules.DiscoverSchedule;
 import edu.illinois.CS598rhk.schedules.NeverSchedule;
 import edu.illinois.CS598rhk.schedules.SearchLightSchedule;
 
 public class WifiService extends Service implements IWifiService {
 	public static final String INTENT_TO_UPDATE_SCHEDULE_PROGRESS = "intent to update schedule progress";
 	public static final String SCHEDULE_PROGRESS_UPDATE = "schedule progress update";
 	public static final String INTENT_TO_RESUME_WIFI = "intent to update the wifi state";
 	public static final String INTENT_TO_PAUSE_WIFI = "(g\")-O";
 	public static final String INTENT_TO_ADD_WIFI_NEIGHBOR = "ASDFASDFSDFA";
 	public static final String WIFI_NEIGHBOR_DATA = "wifi neighbor data";
 	public static final String INTENT_TO_CHANGE_WIFI_ADDRESS = "WHERE ARE ALL MY WIFIS";
 	public static final String NEW_WIFI_ADDRESS = "asdfasdfasdfasdfasdf";
 	public static final String WIFI_NEIGHBOR_NAME = "phone name";
 	public static final String WIFI_IP_ADDRESS = "ip address";
 	public static final int WIFI_STATE_DISCOVERYING = 1;
 	public static final int WIFI_STATE_PAUSED = 0;
 
 	private static final int DISCOVERY_PERIOD = 5000;
 	private static final String MSG_TAG = "MyWifiService";
 	public static final String RESUME_DELAY = "O_o";
 	public static final String NEW_PHONE_NAME = "<><><><><><><><><><<";
 	public static final String INTENT_TO_UPDATE_NAME = "^^;;";
 
 	private DiscoverSchedule discoveryScheduler;
 	private boolean wifiEnabled = false;
 	private CoreTask coretask;
 
 	private WifiManager wifiManager;
 	private WifiMessageReceiver messageReceiver;
 	private WifiController wifiController;
 
 	private String myIPAddress;
 	private String myBroadcast;
 	private String myPhoneName;
 	private int timeSlice;
 	private int wifiState;
 
 	InetAddress dest;
 
 	private final IBinder mBinder = new WifiBinder();
 	public long resumeDelay;
 
 	@Override
 	public IBinder onBind(Intent arg0) {
 		return mBinder;
 	}
 
 	public class WifiBinder extends Binder {
 		public IWifiService getService() {
 			return WifiService.this;
 		}
 	}
 
 	@Override
 	public void onCreate() {
 		wifiManager = (WifiManager) this.getSystemService(WIFI_SERVICE);
 		wifiController = new WifiController();
 		myBroadcast = "192.168.1.255";
 		discoveryScheduler = new NeverSchedule();
 		coretask = new CoreTask();
 
 		try {
 			dest = InetAddress.getByName(myBroadcast);
 		} catch (UnknownHostException e) {
 			Log.d(MSG_TAG, "Broadcast address " + myBroadcast + " as unknown.");
 		}
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		IntentFilter messageFilter = new IntentFilter();
 		messageFilter.addAction(INTENT_TO_PAUSE_WIFI);
 		messageFilter.addAction(INTENT_TO_RESUME_WIFI);
 		messageFilter.addAction(INTENT_TO_CHANGE_WIFI_ADDRESS);
 		messageFilter.addAction(INTENT_TO_UPDATE_NAME);
 		messageReceiver = new WifiMessageReceiver();
 		registerReceiver(messageReceiver, messageFilter);
 
 		coretask = new CoreTask();
 		coretask.setPath(this.getApplicationContext().getFilesDir().getParent());
 		Log.d(MSG_TAG, "Current directory is "
 				+ this.getApplicationContext().getFilesDir().getParent());
 
 		if (wifiManager.isWifiEnabled())
 			wifiManager.setWifiEnabled(false);
 		
 		this.myPhoneName = intent.getStringExtra(MainActivity.NAME_KEY);
 		if(myPhoneName == null)
 			myPhoneName = "HTC Magic";
 		this.myIPAddress = intent.getStringExtra(MainActivity.ADDRESS_KEY);
 		if(myIPAddress == null || !validateIPAddress(myIPAddress))
 			myIPAddress = "192.168.1.2";
 		sendToLogger("My IP address is "+myIPAddress);
 		
 		this.coretask.runRootCommand(this.coretask.DATA_FILE_PATH
 				+ "/bin/load.sh "+myIPAddress);
 		
 		wifiState = WIFI_STATE_DISCOVERYING;
 		wifiController.start();
 
 		return START_STICKY;
 	}
 
 	public void setIPAddress(String ip) {
 		if (!validateIPAddress(ip))
 			return;
 		myBroadcast = ip.substring(0, ip.lastIndexOf(".")) + ".255";
 		this.coretask.runRootCommand("/system/bin/ifconfig tiwlan0 " + ip
 				+ " netmask 255.255.255.0");
 		try {
 			dest = InetAddress.getByName(myBroadcast);
 		} catch (UnknownHostException e) {
 			Log.d(MSG_TAG, "Broadcast address " + myBroadcast + " as unknown.");
 		}
 	}
 
 	private boolean validateIPAddress(String ipAddress) {
 		String[] parts = ipAddress.split("\\.");
 		if (parts.length != 4) {
 			return false;
 		}
 		for (String s : parts) {
 			int i = Integer.parseInt(s);
 
 			if ((i < 0) || (i > 255)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	@Override
 	public void onDestroy() {
 		unregisterReceiver(messageReceiver);
 		super.onDestroy();
 	}
 
 	public void enableWifi() {
 		this.coretask.runRootCommand(this.coretask.DATA_FILE_PATH
 				+ "/bin/up.sh " + this.myIPAddress);
 		Log.d(MSG_TAG, "Wifi Enabled");
 		wifiEnabled = true;
 	}
 
 	public void disableWifi() {
 		this.coretask.runRootCommand(this.coretask.DATA_FILE_PATH
 				+ "/bin/down.sh");
 		Log.d(MSG_TAG, "Wifi disabled");
 		wifiEnabled = false;
 	}
 
 	private class WifiMessageReceiver extends BroadcastReceiver {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			if (intent.getAction() == INTENT_TO_RESUME_WIFI) {
 				resumeWifiService(intent.getLongExtra(
 						RESUME_DELAY, 0));
 			} else if (intent.getAction() == INTENT_TO_PAUSE_WIFI) {
 				pauseWifiService();
 			} else if (intent.getAction() == INTENT_TO_CHANGE_WIFI_ADDRESS) {
 				setIPAddress(intent.getStringExtra(NEW_WIFI_ADDRESS));
 			} else if (intent.getAction() == INTENT_TO_UPDATE_NAME) {
 				myPhoneName = intent.getStringExtra(NEW_PHONE_NAME);
 			}
 
 		}
 	}
 
 	private class WifiController extends Thread {
 
 		private void sendWifiBroadcast() {
 			byte[] buf = new byte[1024];
 			String msg = "name";
 			buf = msg.getBytes();
 			DatagramSocket sock;
 			DatagramPacket pkt = new DatagramPacket(buf, buf.length, dest, 8888);
 			try {
 				// send our last one
 				sock = new DatagramSocket();
 				sock.setBroadcast(true);
 				sock.send(pkt);
 				Log.d(MSG_TAG, "Packet was sent.");
 				sendToLogger("Sent Wifi heartbeat");
 			} catch (Exception e) {
 				e.printStackTrace();
 				Log.d(MSG_TAG, "attempt to send failed");
 			}
 		}
 
 		private void listenForWifiBroadcast(long startTime) {
 			byte[] buf = new byte[1024];
 			DatagramPacket pkt = new DatagramPacket(buf, buf.length, dest, 8888);
 			DatagramSocket sock = null;
 			do {
 				try {
 					sock = new DatagramSocket(8888);
 					sock.setSoTimeout((int) (DISCOVERY_PERIOD - (System
 							.currentTimeMillis() - startTime)));
 					sock.receive(pkt);
 
 					String rcv = new String(pkt.getData(), 0, pkt.getLength());
 
 					// inform the scheduling service
 					Intent foundNewNeighbor = new Intent(
 							INTENT_TO_ADD_WIFI_NEIGHBOR);
 					foundNewNeighbor.putExtra(WIFI_NEIGHBOR_NAME, "");
 					foundNewNeighbor.putExtra(WIFI_IP_ADDRESS, "");
 					sendBroadcast(foundNewNeighbor);
 
 					sendToLogger("Found neighbor ");
 					sock.close();
 				} catch (InterruptedIOException e) {
 					// timed out, so no message was received
 					if (!sock.isClosed())
 						sock.close();
 				} catch (SocketException e) {
 					// couldn't create the socket
 					enableWifi();
 				} catch (IOException e) {
 					// error receiving the packet, try again
 				}
 			} while (System.currentTimeMillis() - startTime < DISCOVERY_PERIOD);
 
 		}
 
 		@Override
 		public void run() {
 			enableWifi();
 			int[] mySchedule = discoveryScheduler.generateScedule();
 			timeSlice = 0;
 			Looper.prepare();
 			while (true) {
 				if (wifiState == WIFI_STATE_PAUSED) {
 					while (wifiState != WIFI_STATE_DISCOVERYING)
 						try {
 							Thread.sleep(1000);
 						} catch (InterruptedException e) {
 							// interrupted from sleep
 						}
 					try {
 						Thread.sleep(resumeDelay);
 					} catch (InterruptedException e) {
 						//interrupted from sleep >=(
 					}
 					timeSlice = 0;
 
 				}
 
 				if (timeSlice == 0)
 					mySchedule = discoveryScheduler.generateScedule();
 
 				long startTime = System.currentTimeMillis();
 				if (!wifiEnabled) {
 					if (mySchedule[(timeSlice + 1) % mySchedule.length] != DiscoverSchedule.DO_NOTHING) {
 						enableWifi();
 					}
 				} else {
 					if (mySchedule[timeSlice] != DiscoverSchedule.DO_NOTHING) {
 						if (mySchedule[timeSlice] == DiscoverSchedule.TRANSMIT_N_LISTEN
 								|| mySchedule[timeSlice] == DiscoverSchedule.TRANSMIT) {
 							sendWifiBroadcast();
 						}
 
 						if (mySchedule[timeSlice] == DiscoverSchedule.TRANSMIT_N_LISTEN
 								|| mySchedule[timeSlice] == DiscoverSchedule.LISTEN) {
 							listenForWifiBroadcast(startTime);
 						}
 						if (mySchedule[timeSlice] == DiscoverSchedule.TRANSMIT_N_LISTEN
 								|| mySchedule[timeSlice] == DiscoverSchedule.TRANSMIT) {
 							sendWifiBroadcast();
 						}
 
 					}
 
 				}
 
 				if (mySchedule[(timeSlice + 1) % mySchedule.length] == DiscoverSchedule.DO_NOTHING
 						&& wifiEnabled) {
 					disableWifi();
 				}
 
 				try {
 					long sleepyTime = DISCOVERY_PERIOD
 							- (System.currentTimeMillis() - startTime);
 					if (sleepyTime < 0)
 						sleepyTime = 0;
 					Thread.sleep(sleepyTime);
 				} catch (InterruptedException e) {
 					Thread.currentThread().interrupt();
 				}
 
 				timeSlice = (timeSlice + 1) % mySchedule.length;
 
 			}
 		}
 	}
 
 	public void resumeWifiService(long delay) {
 		wifiState = WIFI_STATE_DISCOVERYING;
 		resumeDelay = delay;
 		wifiController.interrupt();
 
 	}
 
 	public void pauseWifiService() {
 		wifiState = WIFI_STATE_PAUSED;
 	}
 
 	@Override
 	public long scheduleTimeRemaining() {
 		return (discoveryScheduler.scheduleLength() - timeSlice)*DISCOVERY_PERIOD;
 	}
 
 	public void sendToLogger(String message) {
 		Intent intentToLog = new Intent(PowerManagement.ACTION_LOG_UPDATE);
 		intentToLog.putExtra(PowerManagement.LOG_MESSAGE, message);
 		sendBroadcast(intentToLog);
 	}
 }
