 package edu.ucla.nesl.flowengine.device;
 
 import android.app.Notification;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.BatteryManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.PowerManager;
 import android.os.RemoteException;
 import android.util.Log;
 import edu.ucla.nesl.flowengine.SensorType;
 import edu.ucla.nesl.flowengine.aidl.DeviceAPI;
 import edu.ucla.nesl.flowengine.aidl.FlowEngineAPI;
 import edu.ucla.nesl.flowengine.device.phone.R;
 
 public class PhoneSensorDeviceService extends Service implements SensorEventListener, LocationListener {
 
 	private static final String TAG = PhoneSensorDeviceService.class.getSimpleName();
 
 	private static final int RETRY_INTERVAL = 5000; // ms
 
 	private static final String FLOW_ENGINE_SERVICE_NAME = "edu.ucla.nesl.flowengine.FlowEngine";
 
 	private static final int GPS_INTERVAL = 1000; // ms
 	private static final int GPS_LOCATION_INTERVAL = 1; // meters
 
 	private static final int MSG_KILL = 0;
 	private static final int MSG_START = 1;
 	private static final int MSG_STOP = 2;
 	private static final int MSG_START_SENSOR = 3;
 	private static final int MSG_STOP_SENSOR = 4;
 	private static final int MSG_TRY_BINDING_FLOWENGINE = 5;
 
 	private FlowEngineAPI 	mAPI;
 	private PhoneSensorDeviceService mThisService = this;
 
 	private SensorManager mSensorManager;
 	private Sensor mAccelerometer;
 	private LocationManager mLocationManager;
 
 	private int	mDeviceID;
 	private boolean mIsFlowEngineConnected = false;
 
 	private boolean mIsAccel = false;
 	private boolean mIsBattery = false;
 	private boolean mIsGPS = false;
 
 	private PowerManager.WakeLock mWakeLock;
 	//private NotificationHelper mNotification;
 
 	private BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
 				if (!mIsFlowEngineConnected) {
 					return;
 				}
 
 				int level = intent.getIntExtra("level", 0);
 				double voltage = intent.getIntExtra("voltage", 0) / 1000.0;
 				double temperature = intent.getIntExtra("temperature", 0) / 10.0;
 				String technology = intent.getStringExtra("technology");
 				int status = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
 				String strStatus;
 				switch (status) {
 				case BatteryManager.BATTERY_STATUS_CHARGING:
 					strStatus = "Charging";
 					break;
 				case BatteryManager.BATTERY_STATUS_DISCHARGING:
 					strStatus = "Discharging";
 					break;
 				case BatteryManager.BATTERY_STATUS_FULL:
 					strStatus = "Full";
 					break;
 				case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
 					strStatus = "Not charging";
 					break;
 				default:
 					strStatus = "Unknown";
 					break;
 				}
 				String data = strStatus + ", " + level + "%, " + voltage + "V, " + temperature + ", " + technology;
 				try {
 					mAPI.pushString(mDeviceID, SensorType.PHONE_BATTERY, data, data.length(), System.currentTimeMillis());
 				} catch (RemoteException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}; 
 
 	private ServiceConnection mServiceConnection = new ServiceConnection() {
 		@Override
 		public void onServiceConnected(ComponentName name, IBinder service) {
 			Log.i(TAG, "Service connection established.");
 			mAPI = FlowEngineAPI.Stub.asInterface(service);
 			try {
 				mDeviceID = mAPI.addDevice(mPhoneSensorDeviceInterface);
 				mAPI.addSensor(mDeviceID, SensorType.PHONE_ACCELEROMETER, -1);
 				mAPI.addSensor(mDeviceID, SensorType.PHONE_GPS, -1);
 				mAPI.addSensor(mDeviceID, SensorType.PHONE_BATTERY, -1);
 				mIsFlowEngineConnected = true;
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 		}
 
 		@Override
 		public void onServiceDisconnected(ComponentName name) {
 			Log.i(TAG, "Service connection closed.");
 			mIsFlowEngineConnected = false;
 			stopGPS();
 			stopAccelerometer();
 			stopBattery();
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			mAPI = null;
 			mHandler.sendMessage(mHandler.obtainMessage(MSG_TRY_BINDING_FLOWENGINE));
 		}
 	};
 
 	private DeviceAPI.Stub mPhoneSensorDeviceInterface = new DeviceAPI.Stub() {
 		@Override
 		public void start() throws RemoteException {
 			mHandler.sendMessage(mHandler.obtainMessage(MSG_START));
 		}
 		@Override
 		public void stop() throws RemoteException {
 			mHandler.sendMessage(mHandler.obtainMessage(MSG_STOP));
 		}
 		@Override
 		public void startSensor(int sensor) throws RemoteException {
 			mHandler.sendMessage(mHandler.obtainMessage(MSG_START_SENSOR, (Object)sensor));
 		}
 		@Override
 		public void stopSensor(int sensor) throws RemoteException {
 			mHandler.sendMessage(mHandler.obtainMessage(MSG_STOP_SENSOR, (Object)sensor));
 		}
 		@Override
 		public void kill() throws RemoteException {
 			mHandler.sendMessage(mHandler.obtainMessage(MSG_KILL));
 		}
 	};
 
 	private void handleTryBindingFlowEngine() {
 		if (mAPI == null) {
 			tryBindToFlowEngineService();
 		}
 	}
 
 	private Handler mHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			int sensor;
 			switch (msg.what) {
 			case MSG_TRY_BINDING_FLOWENGINE:
 				handleTryBindingFlowEngine();
 				break;
 			case MSG_KILL:
 				stopAccelerometer();
 				stopGPS();
 				stopBattery();
 				mThisService.stopSelf();
 				break;
 			case MSG_START:
 				startAccelerometer();
 				startGPS();
 				startBattery();
 				break;
 			case MSG_STOP:
 				stopAccelerometer();
 				stopGPS();
 				stopBattery();
 				break;
 			case MSG_START_SENSOR:
 				sensor = (Integer)msg.obj;
 				if (sensor == SensorType.PHONE_ACCELEROMETER) {
 					startAccelerometer();
 				} else if (sensor == SensorType.PHONE_GPS) {
 					startGPS();
 				} else if (sensor == SensorType.PHONE_BATTERY) {
 					startBattery();
 				}
 				break;
 			case MSG_STOP_SENSOR:
 				sensor = (Integer)msg.obj;
 				if (sensor == SensorType.PHONE_ACCELEROMETER) {
 					stopAccelerometer();
 				} else if (sensor == SensorType.PHONE_GPS) {
 					stopGPS();
 				} else if (sensor == SensorType.PHONE_BATTERY) {
 					stopBattery();
 				}
 				break;
 			default:
 				super.handleMessage(msg);
 			}
 		}
 	};
 
 	private void startAccelerometer() {
		mSensorManager.registerListener(mThisService, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
 		mIsAccel = true;
 		acquireWakeLock();
 	}
 
 	private void stopAccelerometer() {
 		mSensorManager.unregisterListener(mThisService, mAccelerometer);
 		mIsAccel = false;
 		checkedReleaseWakeLock();
 	}
 
 	private void startGPS() {
 		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_INTERVAL, GPS_LOCATION_INTERVAL, mThisService);
 		mIsGPS = true;
 		acquireWakeLock();
 	}
 
 	private void stopGPS() {
 		mLocationManager.removeUpdates(mThisService);
 		mIsGPS = false;
 		checkedReleaseWakeLock();
 	}
 
 	private void startBattery() {
 		this.registerReceiver(mBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
 		mIsBattery = true;
 		acquireWakeLock();
 	}
 
 	private void stopBattery() {
 		try {
 			this.unregisterReceiver(mBatteryReceiver);
 		} catch (IllegalArgumentException e) {
 		}
 		mIsBattery = false;
 		checkedReleaseWakeLock();
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {		
 		return null;
 	}
 
 	@Override
 	public void onCreate() {
 		//Debug.startMethodTracing("AccelerometerService");
 		//Debug.startAllocCounting();
 
 		//mNotification = new NotificationHelper(this, TAG, this.getClass().getName(), R.drawable.ic_launcher);
 
 		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
 		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
 		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 
 		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
 		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
 		mWakeLock.setReferenceCounted(false);
 
 		super.onCreate();
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		
 		CharSequence text = getText(R.string.foreground_service_started);
 		Notification notification = new Notification(R.drawable.ic_launcher, text, System.currentTimeMillis());
 		PendingIntent contentIntent = PendingIntent.getBroadcast(this, 0, new Intent(), 0);
 		notification.setLatestEventInfo(this, text, text, contentIntent);
 		startForeground(R.string.foreground_service_started, notification);
 
 		if (mAPI == null) {
 			mHandler.sendMessage(mHandler.obtainMessage(MSG_TRY_BINDING_FLOWENGINE));
 		}
 
 		return START_STICKY;
 	}
 
 	private void tryBindToFlowEngineService() {
 		Intent intent = new Intent(FLOW_ENGINE_SERVICE_NAME);
 
 		int numRetries = 1;
 		while (startService(intent) == null) {
 			Log.d(TAG, "Retrying to start FlowEngineService.. (" + numRetries + ")");
 			numRetries++;
 			try {
 				Thread.sleep(RETRY_INTERVAL);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 
 		// Bind to the FlowEngine service.
 		numRetries = 1;
 		while (!bindService(intent, mServiceConnection, BIND_AUTO_CREATE)) {
 			Log.d(TAG, "Retrying to bind to FlowEngineService.. (" + numRetries + ")");
 			numRetries++;
 			try {
 				Thread.sleep(RETRY_INTERVAL);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	@Override
 	public void onDestroy() {
 		//Debug.stopAllocCounting();
 		//Debug.stopMethodTracing();
 
 		stopAccelerometer();
 		stopGPS();
 		stopBattery();
 
 		try {
 			if (mAPI != null) {
 				mAPI.removeDevice(mDeviceID);
 				unbindService(mServiceConnection);
 			}
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 		
 		stopForeground(true);
 
 		super.onDestroy();
 	}
 
 	@Override
 	public void onSensorChanged(SensorEvent event) {
 		if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
 			return;
 		if (!mIsFlowEngineConnected) {
 			return;
 		}
 
 		double[] data = new double[3];
 		data[0] = event.values[0];
 		data[1] = event.values[1];
 		data[2] = event.values[2];
 
 		try {
 			mAPI.pushDoubleArray(mDeviceID, SensorType.PHONE_ACCELEROMETER, data, data.length, System.currentTimeMillis());
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void onLocationChanged(Location location) {
 		if (!mIsFlowEngineConnected) {
 			return;
 		}
 
 		long timestamp = System.currentTimeMillis(); 
 		double[] data = new double[4];
 		data[0] = location.getLatitude();
 		data[1] = location.getLongitude();
 		data[2] = location.getAltitude();
 		data[3] = location.getSpeed();
 		try {
 			mAPI.pushDoubleArray(mDeviceID, SensorType.PHONE_GPS, data, data.length, timestamp);
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) {
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 	}
 
 	private void acquireWakeLock() {
 		if (!mWakeLock.isHeld()) {
 			mWakeLock.acquire();
 		}
 	}
 
 	private void checkedReleaseWakeLock() {
 		if (!mIsAccel && !mIsGPS && !mIsBattery) {
 			releaseWakeLock();
 		}
 	}
 
 	private void releaseWakeLock() {
 		if (mWakeLock.isHeld()) {
 			mWakeLock.release();
 		}
 	}
 }
