 package com.luzi82.clockcam;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Timer;
 import java.util.TimerTask;
 
import android.app.Notification;
import android.app.PendingIntent;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.hardware.Camera;
 import android.hardware.Camera.PictureCallback;
 import android.net.wifi.WifiManager;
 import android.net.wifi.WifiManager.WifiLock;
 import android.os.IBinder;
 import android.os.PowerManager;
 
 public class ClockCamService extends Service {
 
 	PowerManager mPowerManager;
 	PowerManager.WakeLock mWakeLock;
 	FtpManager mFtpManager;
 	WifiManager mWifiManager;
 	WifiLock mWifiLock;
 
 	Camera mCamera;
 
 	Timer mTimer;
 
 	long mNextShot = -1;
 	String mNextFilename = null;
 	// long mPeriod = 15 * MIN;
 	long mPeriod = 15 * SEC;
 	long mPrepareTime = 5 * SEC;
 
 	public static final String START_CMD = "com.luzi82.clockcam.start";
 	public static final String STOP_CMD = "com.luzi82.clockcam.stop";
 	public static final String LOCAL_PATH = "/mnt/sdcard/DCIM/ClockCam/";
 
 	private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			ClockCamActivity.d("onReceive");
 			String action = intent.getAction();
 			if (action == null) {
 			} else if (action.equals(START_CMD)) {
 				try {
 					startCam();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			} else if (action.equals(STOP_CMD)) {
 				stopCam();
 			}
 		}
 	};
 
 	@Override
 	public IBinder onBind(Intent aIntent) {
 		return null;
 	}
 
 	@Override
 	public void onCreate() {
 		ClockCamActivity.d("onDestroy");
 		mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
 		mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, ClockCamActivity.TAG);
 		mWakeLock.setReferenceCounted(false);
 		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
 		mWifiLock = mWifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, ClockCamActivity.TAG);
 		mWifiLock.setReferenceCounted(false);
 
 		mTimer = new Timer();
 
 		File f = new File(LOCAL_PATH);
 		f.mkdirs();
 
 		IntentFilter commandFilter = new IntentFilter();
 		commandFilter.addAction(START_CMD);
 		commandFilter.addAction(STOP_CMD);
 		registerReceiver(mIntentReceiver, commandFilter);
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		ClockCamActivity.d("onDestroy");
 		if (mTimer != null) {
 			mTimer.cancel();
 		}
 		if (mCamera != null) {
 			mCamera.release();
 		}
 		if (mWakeLock != null) {
 			mWakeLock.release();
 		}
 		if (mWifiLock != null) {
 			mWifiLock.release();
 		}
 		unregisterReceiver(mIntentReceiver);
 	}
 
 	boolean mRunCamera = false;
 
 	public synchronized void startCam() throws IOException {
 		ClockCamActivity.d("startCam");
 		if (mRunCamera == true) {
 			return;
 		}
 		ClockCamActivity.d("startCam2");
 
 		ClockCamConfig conf = ClockCamConfig.load("/mnt/sdcard/clockcam.conf");
 
		startForeground0();

 		mCamera = Camera.open();
 		mRunCamera = true;
 		mWakeLock.acquire();
 		mWifiLock.acquire();
 
 		Camera.Parameters param = mCamera.getParameters();
 		param.setPictureSize(1024, 768);
 		mCamera.setParameters(param);
 
 		mFtpManager = new FtpManager(conf.mServer, conf.mUsername, conf.mPassword, conf.mRemoteDir, LOCAL_PATH);
 		mFtpManager.start();
 
 		nextShot();
 	}
 
 	public synchronized void stopCam() {
 		ClockCamActivity.d("stopCam");
 		if (mRunCamera == false) {
 			return;
 		}
 		ClockCamActivity.d("stopCam2");
 		mRunCamera = false;
 		mWakeLock.release();
 		mWifiLock.release();
 		if (mNextTask != null) {
 			mNextTask.cancel();
 			mNextTask = null;
 		}
 		if (mCameraState == CameraState.PREVIEW) {
 			mNextTask = new StageEndTask();
 			mTimer.schedule(mNextTask, 0);
 		} else {
 			ClockCamActivity.d("mCamera.release();");
 			mCamera.release();
 			mCamera = null;
 		}
 		if (mFtpManager != null) {
 			mFtpManager.stopLater();
 			mFtpManager = null;
 		}
		stopForeground0();
 	}
 
 	enum CameraState {
 		STOP, PREVIEW, SHOT, DONE,
 	}
 
 	CameraState mCameraState = CameraState.STOP;
 
 	// private final IBinder mBinder = new ServiceStub(this);
 	//
 	// static class ServiceStub extends IClockCamService.Stub {
 	// WeakReference<ClockCamService> mService;
 	//
 	// ServiceStub(ClockCamService aService) {
 	// mService = new WeakReference<ClockCamService>(aService);
 	// }
 	//
 	// @Override
 	// public void start() throws RemoteException {
 	// mService.get().startCam();
 	// }
 	//
 	// @Override
 	// public void stop() throws RemoteException {
 	// mService.get().stopCam();
 	// }
 	// }
 
 	synchronized void nextShot() {
 		if (!mRunCamera)
 			return;
 		mNextShot = (((System.currentTimeMillis()) / mPeriod) + 1) * mPeriod;
 		mNextTask = new StagePrepareTask();
 		mTimer.schedule(mNextTask, new Date(mNextShot - mPrepareTime));
 	}
 
 	TimerTask mNextTask = null;
 
 	static final SimpleDateFormat FILE_FORMAT = new SimpleDateFormat("yyyyMMdd-HHmmss");
 
 	class StagePrepareTask extends TimerTask {
 		@Override
 		public void run() {
 			synchronized (ClockCamService.this) {
 				ClockCamActivity.d("StagePrepareTask");
 				mNextTask = null;
 				if (!mRunCamera)
 					return;
 				mCamera.startPreview();
 				mNextTask = new StageShotTask();
 				Date nextShotDate = new Date(mNextShot);
 				mTimer.schedule(mNextTask, nextShotDate);
 				mNextFilename = "/mnt/sdcard/DCIM/ClockCam/" + FILE_FORMAT.format(nextShotDate) + ".jpg";
 				mCameraState = CameraState.PREVIEW;
 			}
 		}
 	}
 
 	class StageShotTask extends TimerTask {
 		@Override
 		public void run() {
 			synchronized (ClockCamService.this) {
 				ClockCamActivity.d("StageShotTask");
 				mNextTask = null;
 				if (!mRunCamera)
 					return;
 				mCamera.takePicture(null, null, mStatePicture);
 				mCameraState = CameraState.SHOT;
 			}
 		}
 	}
 
 	PictureCallback mStatePicture = new PictureCallback() {
 		@Override
 		public void onPictureTaken(byte[] data, Camera camera) {
 			synchronized (ClockCamService.this) {
 				ClockCamActivity.d("PictureCallback");
 				if (mRunCamera) {
 					ClockCamActivity.d(String.format("onPictureTaken %d", data.length));
 					try {
 						BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(mNextFilename));
 						bos.write(data);
 						bos.flush();
 						bos.close();
 					} catch (FileNotFoundException e) {
 						e.printStackTrace();
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 				}
 				mNextTask = new StageEndTask();
 				mTimer.schedule(mNextTask, 0);
 				mCameraState = CameraState.DONE;
 			}
 		}
 	};
 
 	class StageEndTask extends TimerTask {
 		@Override
 		public void run() {
 			synchronized (ClockCamService.this) {
 				ClockCamActivity.d("StageEndTask");
 				mNextTask = null;
 				if (mCameraState != CameraState.STOP) {
 					mCamera.stopPreview();
 					mCameraState = CameraState.STOP;
 				}
 				if (mRunCamera) {
 					nextShot();
 				} else {
 					ClockCamActivity.d("mCamera.release();");
 					mCamera.release();
 					mCamera = null;
 				}
 			}
 		}
 	}
 
 	static long SEC = 1000;
 	static long MIN = 60 * SEC;
 
	void startForeground0() {
		Notification notification = new Notification(R.drawable.ic_launcher, "Clock Cam", System.currentTimeMillis());
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, ClockCamActivity.class), 0);
		notification.setLatestEventInfo(this, "Clock Cam", "Clock Cam", contentIntent);
		startForeground(100, notification);
	}

	void stopForeground0() {
		stopForeground(true);
	}

 }
