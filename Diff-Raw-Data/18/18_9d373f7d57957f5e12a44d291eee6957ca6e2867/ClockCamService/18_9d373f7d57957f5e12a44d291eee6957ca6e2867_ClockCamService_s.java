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
import java.util.regex.Pattern;

import com.luzi82.clockcam.CameraManager.CameraParameter;
 
 import android.app.Notification;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
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
 	SharedPreferences mSharedPreferences;
 
 	// Camera mCamera;
 	// Timer mTimer;
 
 	// String mSaveDirectory = null; // sync
 	//
 	// long mNextShot = -1;
 	// String mNextFilename = null;
 	// // long mPeriod = 15 * MIN;
 	// long mPeriod;
 	// long mPrepareTime = 5 * SEC;
 
 	private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			ClockCamActivity.d("onReceive");
 			String action = intent.getAction();
 			if (action == null) {
 			} else if (action.equals(SharedPreferenceChangeBoardcast.PREFERENCE_CMD)) {
 				onPreferenceChange(intent.getStringExtra(SharedPreferenceChangeBoardcast.PREFERENCE_CMD_KEY));
 				// String param =
 				// intent.getStringExtra(SharedPreferenceChangeBoardcast.PREFERENCE_CMD_KEY);
 				// ClockCamActivity.d("PREFERENCE_CMD " + param);
 				// if (param == null) {
 				// } else if (param.equals("preference_setting_mode")) {
 				// boolean on =
 				// mSharedPreferences.getBoolean("preference_setting_mode",
 				// false);
 				// if (on) {
 				// updateForeground();
 				// startCam();
 				// } else {
 				// stopCam();
 				// updateForeground();
 				// }
 				// } else if (param.equals("preference_setting_photo_size") ||
 				// param.equals("preference_setting_photo_whitebalance")) {
 				// if (mRunCamera) {
 				// setCameraParameters();
 				// }
 				// } else if (param.equals("preference_setting_timerperiod")) {
 				// if (mRunCamera) {
 				// synchronized (ClockCamService.this) {
 				// if (mNextTask != null) {
 				// mNextTask.cancel();
 				// mNextTask = null;
 				// }
 				// nextShot();
 				// }
 				// }
 				// } else if (param.equals("preference_setting_storage_path")) {
 				// synchronized (ClockCamService.this) {
 				// mSaveDirectory =
 				// mSharedPreferences.getString("preference_setting_storage_path",
 				// null);
 				// mkdir(mSaveDirectory);
 				// }
 				// }
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
 
 		// mTimer = new Timer();
 
 		// File f = new File(LOCAL_PATH);
 		// f.mkdirs();
 
 		mCameraManager = new CameraManager();
 
 		mSharedPreferences = getSharedPreferences(ClockCamActivity.PREFERENCE_NAME, MODE_PRIVATE);
 
 		IntentFilter commandFilter = new IntentFilter();
 		commandFilter.addAction(SharedPreferenceChangeBoardcast.PREFERENCE_CMD);
 		registerReceiver(mIntentReceiver, commandFilter);
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		ClockCamActivity.d("onDestroy");
 		// if (mTimer != null) {
 		// mTimer.cancel();
 		// }
 		// if (mCamera != null) {
 		// mCamera.release();
 		// }
 		if (mCameraManager != null) {
 			mCameraManager.stopLater();
 			mCameraManager = null;
 		}
 		if (mWakeLock != null) {
 			mWakeLock.release();
 		}
 		if (mWifiLock != null) {
 			mWifiLock.release();
 		}
 		unregisterReceiver(mIntentReceiver);
 	}
 
 	// boolean mRunCamera = false;
 
 	// public synchronized void startCam() {
 	// ClockCamActivity.d("startCam");
 	// if (mRunCamera == true) {
 	// return;
 	// }
 	// ClockCamActivity.d("startCam2");
 	//
 	// mSaveDirectory =
 	// mSharedPreferences.getString("preference_setting_storage_path", null);
 	// mkdir(mSaveDirectory);
 	//
 	// mCamera = Camera.open();
 	// mRunCamera = true;
 	// mWakeLock.acquire();
 	// mWifiLock.acquire();
 	//
 	// setCameraParameters();
 	//
 	// nextShot();
 	// }
 	//
 	// public synchronized void stopCam() {
 	// ClockCamActivity.d("stopCam");
 	// if (mRunCamera == false) {
 	// return;
 	// }
 	// ClockCamActivity.d("stopCam2");
 	// mRunCamera = false;
 	// mWakeLock.release();
 	// mWifiLock.release();
 	// if (mNextTask != null) {
 	// mNextTask.cancel();
 	// mNextTask = null;
 	// }
 	// if (mCameraState == CameraState.PREVIEW) {
 	// mNextTask = new StageEndTask();
 	// mTimer.schedule(mNextTask, 0);
 	// } else {
 	// ClockCamActivity.d("mCamera.release();");
 	// mCamera.release();
 	// mCamera = null;
 	// }
 	// // if (mFtpManager != null) {
 	// // mFtpManager.stopLater();
 	// // mFtpManager = null;
 	// // }
 	// }
 
 	// enum CameraState {
 	// STOP, PREVIEW, SHOT, DONE,
 	// }
 	//
 	// CameraState mCameraState = CameraState.STOP;
 
 	// synchronized void nextShot() {
 	// if (!mRunCamera)
 	// return;
 	// String periodString =
 	// mSharedPreferences.getString("preference_setting_timerperiod", null);
 	// if (periodString != null) {
 	// mPeriod = Integer.parseInt(periodString) * SEC;
 	// } else {
 	// mPeriod = 60 * SEC;
 	// }
 	// mNextShot = (((System.currentTimeMillis()) / mPeriod) + 1) * mPeriod;
 	// mNextTask = new StagePrepareTask();
 	// mTimer.schedule(mNextTask, new Date(mNextShot - mPrepareTime));
 	// }
 
 	// TimerTask mNextTask = null;
 	//
 	// static final SimpleDateFormat FILE_FORMAT = new
 	// SimpleDateFormat("yyyyMMdd-HHmmss");
 	//
 	// class StagePrepareTask extends TimerTask {
 	// @Override
 	// public void run() {
 	// synchronized (ClockCamService.this) {
 	// ClockCamActivity.d("StagePrepareTask");
 	// mNextTask = null;
 	// if (!mRunCamera)
 	// return;
 	// mCamera.startPreview();
 	// mNextTask = new StageShotTask();
 	// Date nextShotDate = new Date(mNextShot);
 	// mTimer.schedule(mNextTask, nextShotDate);
 	// mNextFilename = FILE_FORMAT.format(nextShotDate) + ".jpg";
 	// mCameraState = CameraState.PREVIEW;
 	// }
 	// }
 	// }
 	//
 	// class StageShotTask extends TimerTask {
 	// @Override
 	// public void run() {
 	// synchronized (ClockCamService.this) {
 	// ClockCamActivity.d("StageShotTask");
 	// mNextTask = null;
 	// if (!mRunCamera)
 	// return;
 	// mCamera.takePicture(null, null, mStatePicture);
 	// mCameraState = CameraState.SHOT;
 	// }
 	// }
 	// }
 	//
 	// PictureCallback mStatePicture = new PictureCallback() {
 	// @Override
 	// public void onPictureTaken(byte[] data, Camera camera) {
 	// synchronized (ClockCamService.this) {
 	// ClockCamActivity.d("PictureCallback");
 	// if (mRunCamera && (mNextFilename != null)) {
 	// ClockCamActivity.d(String.format("onPictureTaken %d", data.length));
 	// try {
 	// BufferedOutputStream bos = new BufferedOutputStream(new
 	// FileOutputStream(new File(mSaveDirectory, mNextFilename)));
 	// bos.write(data);
 	// bos.flush();
 	// bos.close();
 	// } catch (FileNotFoundException e) {
 	// e.printStackTrace();
 	// } catch (IOException e) {
 	// e.printStackTrace();
 	// }
 	// }
 	// mNextFilename = null;
 	// mNextTask = new StageEndTask();
 	// mTimer.schedule(mNextTask, 0);
 	// mCameraState = CameraState.DONE;
 	// }
 	// }
 	// };
 	//
 	// class StageEndTask extends TimerTask {
 	// @Override
 	// public void run() {
 	// synchronized (ClockCamService.this) {
 	// ClockCamActivity.d("StageEndTask");
 	// mNextTask = null;
 	// if (mCameraState != CameraState.STOP) {
 	// mCamera.stopPreview();
 	// mCameraState = CameraState.STOP;
 	// }
 	// if (mRunCamera) {
 	// nextShot();
 	// } else {
 	// ClockCamActivity.d("mCamera.release();");
 	// mCamera.release();
 	// mCamera = null;
 	// }
 	// }
 	// }
 	// }
 
 	static long SEC = 1000;
 	static long MIN = 60 * SEC;
 
 	// void setCameraParameters() {
 	// if (mCamera == null)
 	// return;
 	// Camera.Parameters param = mCamera.getParameters();
 	//
 	// String sizeParam =
 	// mSharedPreferences.getString("preference_setting_photo_size", null);
 	// if (sizeParam != null) {
 	// String[] sizeParam2 = sizeParam.split(Pattern.quote("x"));
 	// if (sizeParam2.length == 2) {
 	// int w = Integer.parseInt(sizeParam2[0]);
 	// int h = Integer.parseInt(sizeParam2[1]);
 	// if ((w > 0) && (h > 0)) {
 	// param.setPictureSize(w, h);
 	// }
 	// }
 	// }
 	//
 	// String wbParam =
 	// mSharedPreferences.getString("preference_setting_photo_whitebalance",
 	// null);
 	// if (wbParam != null) {
 	// param.setWhiteBalance(wbParam);
 	// }
 	//
 	// mCamera.setParameters(param);
 	// }
 
 	private static void mkdir(String aPath) {
 		if (aPath == null)
 			return;
 		new File(aPath).mkdirs();
 	}
 
 	private CameraManager mCameraManager;
 
 	// private void startFtp() {
 	// if (mFtpManager != null)
 	// return;
 	// String domain =
 	// mSharedPreferences.getString("preference_setting_upload_ftp_domain",
 	// null);
 	// String portString =
 	// mSharedPreferences.getString("preference_setting_upload_ftp_port", "-1");
 	// String username =
 	// mSharedPreferences.getString("preference_setting_upload_ftp_login",
 	// null);
 	// String password =
 	// mSharedPreferences.getString("preference_setting_upload_ftp_password",
 	// null);
 	// String remotePath =
 	// mSharedPreferences.getString("preference_setting_upload_ftp_remotepath",
 	// null);
 	// int port = -1;
 	// try {
 	// port = Integer.parseInt(portString);
 	// } catch (NumberFormatException nfe) {
 	// }
 	// if ((domain == null) || (port < 0) || (username == null) || (password ==
 	// null) || (remotePath == null)) {
 	// return;
 	// }
 	// mFtpManager = new FtpManager(domain, port, username, password,
 	// remotePath, mSaveDirectory);
 	// mFtpManager.start();
 	// }
 
 	// private void stopFtpLater() {
 	// if (mFtpManager == null)
 	// return;
 	// mFtpManager.stopLater();
 	// mFtpManager = null;
 	// }
 
 	boolean mForegroundOn = false;
 
 	private void updateForeground(boolean aShouldForground) {
 		if (aShouldForground != mForegroundOn) {
 			if (aShouldForground) {
 				Notification notification = new Notification(R.drawable.ic_launcher, "Clock Cam", System.currentTimeMillis());
 				PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, ClockCamActivity.class), 0);
 				notification.setLatestEventInfo(this, "Clock Cam", "Clock Cam", contentIntent);
 				startForeground(100, notification);
 			} else {
 				stopForeground(true);
 			}
 			mForegroundOn = aShouldForground;
 		}
 	}
 
 	private void onPreferenceChange(String aKey) {
 		ClockCamPreference preference = ClockCamPreference.get(mSharedPreferences);
 		fixPreference(preference);
 
 		boolean shouldForground = preference.preference_setting_mode || preference.preference_setting_upload_ftp_enable || preference.preference_setting_upload_ntp_enable;
 		updateForeground(shouldForground);
 
 		if (preference.preference_setting_mode || preference.preference_setting_upload_ftp_enable) {
 			mkdir(preference.preference_setting_storage_path);
 		}
 
 		if (preference.preference_setting_mode) {
 			CameraParameter cp = new CameraParameter();
 			cp.mPictureWidth = preference.preference_setting_photo_size_width;
 			cp.mPictureHeight = preference.preference_setting_photo_size_height;
 			cp.whiteBalance = preference.preference_setting_photo_whitebalance;
 			mCameraManager.setCameraParameter(cp);
 			mCameraManager.setSaveDirectory(preference.preference_setting_storage_path);
 			mCameraManager.setPeriod(preference.preference_setting_timerperiod);
 			mCameraManager.start();
 		} else {
 			mCameraManager.stopLater();
 		}
 
 		if (preference.preference_setting_upload_ftp_enable) {
 			if (mFtpManager != null) {
 				boolean change = false;
 				change = change || (!mFtpManager.mServer.equals(preference.preference_setting_upload_ftp_domain));
 				change = change || (mFtpManager.mPort != preference.preference_setting_upload_ftp_port);
 				change = change || (!mFtpManager.mUsername.equals(preference.preference_setting_upload_ftp_login));
 				change = change || (!mFtpManager.mPassword.equals(preference.preference_setting_upload_ftp_password));
 				change = change || (!mFtpManager.mRemoteDirectory.equals(preference.preference_setting_upload_ftp_remotepath));
 				change = change || (!mFtpManager.mLocalDirectory.equals(preference.preference_setting_storage_path));
 				if (change) {
 					mFtpManager.stopLater();
 					mFtpManager = null;
 				}
 			}
 			if (mFtpManager == null) {
 				mFtpManager = new FtpManager(preference.preference_setting_upload_ftp_domain, preference.preference_setting_upload_ftp_port, preference.preference_setting_upload_ftp_login, preference.preference_setting_upload_ftp_password, preference.preference_setting_upload_ftp_remotepath, preference.preference_setting_storage_path);
 				mFtpManager.start();
 			}
 		} else {
 			if (mFtpManager != null) {
 				mFtpManager.stopLater();
 				mFtpManager = null;
 			}
 		}
 	}
 
 	private void fixPreference(ClockCamPreference preference) {
 		// preference_setting_timerperiod
 		if (preference.preference_setting_timerperiod < 0) {
 			preference.preference_setting_mode = false;
 		}
 		if (preference.preference_setting_photo_size_width <= 0) {
 			preference.preference_setting_mode = false;
 		}
 		if (preference.preference_setting_photo_size_height <= 0) {
 			preference.preference_setting_mode = false;
 		}
 		if (preference.preference_setting_photo_whitebalance == null) {
 			preference.preference_setting_mode = false;
 		}
 
 		if (preference.preference_setting_storage_path == null) {
 			preference.preference_setting_mode = false;
 			preference.preference_setting_upload_ftp_enable = false;
 		}
 
 		// preference_setting_upload_ftp_enable
 		if (preference.preference_setting_upload_ftp_domain == null) {
 			preference.preference_setting_upload_ftp_enable = false;
 		}
 		if (preference.preference_setting_upload_ftp_port <= 0) {
 			preference.preference_setting_upload_ftp_enable = false;
 		}
 		if (preference.preference_setting_upload_ftp_login == null) {
 			preference.preference_setting_upload_ftp_enable = false;
 		}
 		if (preference.preference_setting_upload_ftp_password == null) {
 			preference.preference_setting_upload_ftp_enable = false;
 		}
 		if (preference.preference_setting_upload_ftp_remotepath == null) {
 			preference.preference_setting_upload_ftp_enable = false;
 		}
 
 		// preference_setting_upload_ntp_enable
 		if (preference.preference_setting_upload_ntp_domain == null) {
 			preference.preference_setting_upload_ntp_enable = false;
 		}
 		if (preference.preference_setting_upload_ntp_port <= 0) {
 			preference.preference_setting_upload_ntp_enable = false;
 		}
 	}
 
 }
