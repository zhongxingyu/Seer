 package net.diogomarques.wifioppish;
 
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.net.UnknownHostException;
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.wifi.ScanResult;
 import android.net.wifi.WifiConfiguration;
 import android.net.wifi.WifiManager;
 import android.net.wifi.WifiManager.MulticastLock;
 import android.util.Log;
 
 /**
  * Networking façade for interaction with softAP functionality & message passing
  * (UDP)
  * 
  * @author Diogo Marques <diogohomemmarques@gmail.com>
  * 
  *         TODO: too many concerns in one place
  */
 public class AndroidNetworkingFacade implements INetworkingFacade {
 
 	private static final String TAG = AndroidNetworkingFacade.class
 			.getSimpleName();
 
 	/**
 	 * Listener for message sending-related events.
 	 */
 	private OnSendListener mSendListener;
 
 	/**
 	 * Listener for message receiving-related events.
 	 */
 	private OnReceiveListener mReceiveListener;
 
 	/**
 	 * Listener for AP scanning events.
 	 */
 	private OnAccessPointScanListener mAccessPointScanListener;
 
 	/**
 	 * The original softAP configuration, saved to restore after execution
 	 */
 	private WifiConfiguration fOriginalApConfiguration;
 
 	/**
 	 * The original wi-fi state, saved to restore after execution
 	 */
 	private boolean fOriginalIsWifiEnabled; // true if enabled
 
 	/*
 	 * Dependencies.
 	 */
 	private Context mContext;
 	private IPreferences mPreferences;
 
 	/**
 	 * Single instance of lock
 	 */
 	private MulticastLock mLock;
 
 	/**
 	 * Single UDP socket for messaging i/o
 	 */
 	private DatagramSocket mSocket;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param context
 	 *            the caller's context
 	 * @param preferences
 	 *            a preferences repo
 	 */
 	public AndroidNetworkingFacade(Context context, IPreferences preferences) {
 		this.mContext = context;
 		this.mPreferences = preferences;
 		mSocket = null;
 		mLock = null;
 	}
 
 	@Override
 	public void clearListeners() {
 		mSendListener = null;
 		mReceiveListener = null;
 		mAccessPointScanListener = null;
 	}
 
 	@Override
 	public OnSendListener getOnSendListener() {
 		return mSendListener;
 	}
 
 	@Override
 	public void setOnSendListener(OnSendListener listener) {
 		mSendListener = listener;
 	}
 
 	@Override
 	public OnReceiveListener getOnReceiveListener() {
 		return mReceiveListener;
 	}
 
 	@Override
 	public void setOnReceiveListener(OnReceiveListener listener) {
 		mReceiveListener = listener;
 	}
 
 	/* SOFT AP STUFF MANAGEMENT */
 
 	@Override
 	public void startWifiAP() {
 		saveApConfiguration();
		setSoftAPEnabled(getWifiSoftAPConfiguration(), true);
 	}
 
 	@Override
 	public void stopWifiAP() {
 		restoreWifiState();
 	}
 
 	private void restoreWifiState() {
 		WifiManager manager = (WifiManager) mContext
 				.getSystemService(Context.WIFI_SERVICE);
 		try {
 			// Disable emergency access point
			setSoftAPEnabled(getWifiSoftAPConfiguration(), false);
 			// Reset configuration
 			setSoftAPEnabled(fOriginalApConfiguration, false); // hack
 			Method mSetWifiApConfiguration = manager.getClass().getMethod(
 					"setWifiApConfiguration", WifiConfiguration.class);
 			mSetWifiApConfiguration.invoke(manager, fOriginalApConfiguration);
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			e.printStackTrace();
 		}
 
 		// Re-enable wi-fi if it was originally enabled
 		if (fOriginalIsWifiEnabled)
 			manager.setWifiEnabled(true);
 
 	}
 
 	private void setSoftAPEnabled(WifiConfiguration cfg, boolean enable) {
 		WifiManager manager = (WifiManager) mContext
 				.getSystemService(Context.WIFI_SERVICE);
 		if (enable == true)
 			manager.setWifiEnabled(false); // Stop wi-fi station mode
 		try {
 			Method mSetWifiApEnabled = manager.getClass().getMethod(
 					"setWifiApEnabled", WifiConfiguration.class, boolean.class);
 			mSetWifiApEnabled.invoke(manager, cfg, enable);
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			e.printStackTrace();
 		}
 
 	}
 
	private WifiConfiguration getWifiSoftAPConfiguration() {
 		WifiConfiguration wc = new WifiConfiguration();
 		wc.SSID = mPreferences.getWifiSSID();
 		wc.preSharedKey = mPreferences.getWifiPassword();
 		wc.allowedGroupCiphers.clear();
 		wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
 		wc.allowedPairwiseCiphers.clear();
 		wc.allowedProtocols.clear();
 		return wc;
 	}
 
 	private void saveApConfiguration() {
 		WifiManager manager = (WifiManager) mContext
 				.getSystemService(Context.WIFI_SERVICE);
 		fOriginalIsWifiEnabled = manager.isWifiEnabled();
 		try {
 			Method mGetWifiApConfiguration = manager.getClass().getMethod(
 					"getWifiApConfiguration");
 			// Save this configuration for later
 			fOriginalApConfiguration = (WifiConfiguration) mGetWifiApConfiguration
 					.invoke(manager, (Object[]) null);
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/* Message passing UDP */
 
 	@Override
 	public void send(String msg) {
 		try {
 			DatagramPacket packet = new DatagramPacket(msg.getBytes(),
 					msg.length(), InetAddress.getByName(mPreferences
 							.getBroadcastAddress()), mPreferences.getPort());
 			getBroadcastSocket().send(packet);
 			mSendListener.onMessageSent(msg);
 		} catch (UnknownHostException e) {
 			mSendListener.onSendError("Unknown host");
 		} catch (IOException e) {
 			mSendListener.onSendError("Network unavailable");
 		}
 	}
 
 	@Override
 	public void receiveFirst(int timeoutMilis) {
 		byte[] buffer = new byte[1024];
 		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
 		try {
 			DatagramSocket socket = getBroadcastSocket();
 			socket.setSoTimeout(timeoutMilis);
 
 			// blocks for t_beac
 			socket.receive(packet);
 			Log.w(TAG,
 					"Received packet! " + new String(buffer) + " from "
 							+ packet.getAddress().getHostAddress() + ":"
 							+ packet.getPort());
 			releaseBroadcastSocket();
 			mReceiveListener.onMessageReceived(new String(buffer));
 		} catch (SocketTimeoutException e) {
 			// t_beac timed out, go to scanning
 			releaseBroadcastSocket();
 			mReceiveListener.onReceiveTimeout(false);
 		} catch (IOException e) {
 			Log.e(TAG, e.getMessage());
 		}
 	}
 
 	@Override
 	public void receive(int timeoutMilis) {
 		long now = new Date().getTime();
 		byte[] buffer = new byte[1024];
 		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
 		try {
 			while (true) {
 				// force finish, even if new connections are happening
 				if (new Date().getTime() > (now + timeoutMilis)) {
 					mReceiveListener.onReceiveTimeout(true);
 					break;
 				}
 
 				DatagramSocket socket = getBroadcastSocket();
 				socket.setSoTimeout(timeoutMilis);
 
 				// blocks for t_beac
 				socket.receive(packet);
 				Log.w(TAG,
 						"Received packet! " + new String(buffer) + " from "
 								+ packet.getAddress().getHostAddress() + ":"
 								+ packet.getPort());
 				releaseBroadcastSocket();
 				mReceiveListener.onMessageReceived(new String(buffer));
 
 			}
 		} catch (SocketTimeoutException e) {
 			// no connections received
 			releaseBroadcastSocket();
 			mReceiveListener.onReceiveTimeout(false);
 		} catch (IOException e) {
 			Log.e(TAG, e.getMessage());
 		}
 
 	}
 
 	private DatagramSocket getBroadcastSocket() {
 		// Acquire multicast lock if not already acquired
 		if (mLock == null || !mLock.isHeld()) {
 			WifiManager manager = (WifiManager) mContext
 					.getSystemService(Context.WIFI_SERVICE);
 			mLock = manager.createMulticastLock(TAG);
 			mLock.acquire();
 		}
 		// Open socket if not opened
 		if (mSocket == null || mSocket.isClosed()) {
 			try {
 				mSocket = new DatagramSocket(mPreferences.getPort());
 				mSocket.setBroadcast(true);
 			} catch (SocketException e) {
 
 				Log.e(TAG, e.getMessage());
 			}
 		}
 		return mSocket;
 	}
 
 	public void releaseBroadcastSocket() {
 		if (mSocket != null && !mSocket.isClosed())
 			mSocket.close();
 		mSocket = null;
 		if (mLock != null && mLock.isHeld())
 			mLock.release();
 		mLock = null;
 	}
 
 	@Override
 	public OnAccessPointScanListener getOnAccessPointListener() {
 		return mAccessPointScanListener;
 	}
 
 	@Override
 	public void setOnAccessPointScanListener(OnAccessPointScanListener listener) {
 		this.mAccessPointScanListener = listener;
 
 	}
 
 	@Override
 	public void scanForAP(int timeoutMilis) {
 		// TODO wake & wifi locks may be needed. check.
 		// best case, a lock WifiManager.WIFI_MODE_SCAN_ONLY will suffice
 		// worst case, set wifi to never sleep:
 		// <uses-permission android:name="android.permission.WRITE_SETTINGS"/>
 		// Settings.System.putInt(getContentResolver(),
 		// Settings.System.WIFI_SLEEP_POLICY,
 		// Settings.System.WIFI_SLEEP_POLICY_NEVER);
 
 		// control flag to prevent scan after connection
 		final AtomicBoolean connected = new AtomicBoolean(false);
 
 		// activate WiFi, otherwise other methods may behave strangely, e.g.
 		// returning nulls
 		// TODO check if this always works. enabling takes some time, but can be
 		// checked by the receiver, using the WIFI_STATE_ENABLED extra
 		final WifiManager manager = (WifiManager) mContext
 				.getSystemService(Context.WIFI_SERVICE);
 		manager.setWifiEnabled(true);
 
 		// start receiver for scans
 		BroadcastReceiver scanReceiver = new BroadcastReceiver() {
 
 			@Override
 			public void onReceive(Context context, Intent intent) {
 				List<ScanResult> results = manager.getScanResults();
 				// Best signal check:
 				// http://marakana.com/forums/android/examples/40.html
 				ScanResult bestSignal = null;
 				for (ScanResult result : results) {
 					if (result.SSID.equals(mPreferences.getWifiSSID())) {
 						Log.w(TAG, "Found " + mPreferences.getWifiSSID()
 								+ " AP, signal " + result.level);
 						if (bestSignal == null
 								|| WifiManager.compareSignalLevel(
 										bestSignal.level, result.level) < 0)
 							bestSignal = result;
 					}
 				}
 				if (bestSignal != null) {
					Log.w(TAG, "Settled for AP with signal  "
							+ bestSignal.level);
					WifiConfiguration configuration = getWifiSoftAPConfiguration();
					configuration.SSID = "\"" + configuration.SSID + "\"";
					configuration.preSharedKey = "\""
							+ configuration.preSharedKey + "\"";
					int netId = manager.addNetwork(configuration);
					Log.w(TAG, "addNetwork -> netId = " + netId);
					if (netId == -1) {
						throw new IllegalStateException("Add network error.");
					}
					boolean disconnect = manager.disconnect();
					Log.w(TAG, "disconnect -> " + disconnect);
					boolean enableNetwork = manager.enableNetwork(netId, true);
					Log.w(TAG, "enableNetwork -> " + enableNetwork);
					boolean reconnect = manager.reconnect();
					Log.w(TAG, "reconnect -> " + reconnect);
 					safeUnregisterReceiver(this);
 					connected.set(true);
 					mAccessPointScanListener.onEmergencyAPConnected();
 				}
 			}
 		};
 		;
 		mContext.registerReceiver(scanReceiver, new IntentFilter(
 				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
 		// scan immediately one time - fast reconnect
 		// "In this case nevertheless, the node will not scan for t_scan seconds (which might be very long) but immediately try to ﬁnd a new AP or becomes one within a few seconds (fast reconnect)."
 		manager.startScan();
 		// every scanPeriod, scan again
 		long startTime = new Date().getTime();
 		while (!connected.get()) {
 			long tick = new Date().getTime();
 			if (tick > startTime + timeoutMilis) {
 				Log.w("", "Scan timeout");
 				safeUnregisterReceiver(scanReceiver);
 				mAccessPointScanListener.onScanTimeout();
 				break;
 			}
 			while (true) {
 				if (new Date().getTime() > tick + mPreferences.getScanPeriod()) {
 					// Log.w("", "Scan " + countScans++);
 					manager.startScan();
 					break;
 				}
 			}
 		}
 	}
 
 	/*
 	 * There is not API for checking if a receiver is registered. This is a
 	 * workaround.
 	 */
 	private boolean safeUnregisterReceiver(BroadcastReceiver receiver) {
 		boolean sucess = false;
 		try {
 			mContext.unregisterReceiver(receiver);
 			sucess = true;
 		} catch (IllegalArgumentException exception) {
 			sucess = false;
 			Log.w("", "Trying to unregister a receiver that isn't registered.");
 		}
 		return sucess;
 	}
 
 	// // TODO: is this really needed? everyone in range is supposed to have got
 	// // it.
 	// private void relayPacket(DatagramPacket packet) {
 	// Log.w(TAG, "Relaying message");
 	// String msg = new String(packet.getData());
 	// send(msg);
 	// }
 
 }
