 package com.luzi82.clockcam;
 
 import java.util.Date;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.hardware.Camera;
 import android.hardware.Camera.PictureCallback;
 import android.os.IBinder;
 import android.os.PowerManager;
 
 public class ClockCamService extends Service {
 
 	PowerManager mPowerManager;
 	PowerManager.WakeLock mWakeLock;
 
 	Camera mCamera;
 
 	Timer mTimer;
 
 	long mNextShot = -1;
 	// long mPeriod = 15 * MIN;
 	long mPeriod = 15 * SEC;
 	long mPrepareTime = 5 * SEC;
 
 	public static final String START_CMD = "com.luzi82.clockcam.start";
 	public static final String STOP_CMD = "com.luzi82.clockcam.stop";
 
 	private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			ClockCamActivity.d("onReceive");
 			String action = intent.getAction();
 			if (action == null) {
 			} else if (action.equals(START_CMD)) {
 				startCam();
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
 		mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
 		mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, ClockCamActivity.TAG);
 
 		mTimer = new Timer();
 
 		IntentFilter commandFilter = new IntentFilter();
 		commandFilter.addAction(START_CMD);
 		commandFilter.addAction(STOP_CMD);
 		registerReceiver(mIntentReceiver, commandFilter);
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		if (mTimer != null) {
 			mTimer.cancel();
 		}
 		if (mCamera != null) {
 			mCamera.release();
 		}
 		unregisterReceiver(mIntentReceiver);
 	}
 
 	boolean mRunCamera = false;
 
 	public synchronized void startCam() {
 		ClockCamActivity.d("startCam");
 		mCamera = Camera.open();
 		mRunCamera = true;
 		mWakeLock.acquire();
 		nextShot();
 	}
 
 	public synchronized void stopCam() {
 		ClockCamActivity.d("stopCam");
 		mRunCamera = false;
 		mWakeLock.release();
 		if (mNextTask != null) {
 			mNextTask.cancel();
 			mNextTask = null;
 		}
 		if (mCameraState == CameraState.PREVIEW) {
 			mNextTask = new StageEndTask();
 			mTimer.schedule(mNextTask, 0);
 		}
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
 
 	class StagePrepareTask extends TimerTask {
 		@Override
 		public void run() {
 			synchronized (ClockCamService.this) {
 				mNextTask = null;
 				if (!mRunCamera)
 					return;
 				mCamera.startPreview();
 				mNextTask = new StageShotTask();
 				mTimer.schedule(mNextTask, new Date(mNextShot));
 				mCameraState = CameraState.PREVIEW;
 			}
 		}
 	}
 
 	class StageShotTask extends TimerTask {
 		@Override
 		public void run() {
 			synchronized (ClockCamService.this) {
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
 				if (mRunCamera) {
 					// do sth good
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
 				mNextTask = null;
 				if (mCameraState != CameraState.STOP) {
 					mCamera.stopPreview();
 					mCameraState = CameraState.STOP;
 				}
 				if (mRunCamera) {
 					nextShot();
 				}
 			}
 		}
 	}
 
 	static long SEC = 1000;
 	static long MIN = 60 * SEC;
 
 }
