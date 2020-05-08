 package com.emdoor.autotest;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.Date;
 import java.text.SimpleDateFormat;
 import java.util.HashMap;
 
 import android.app.admin.DevicePolicyManager;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.media.AudioManager;
 import android.os.Environment;
 import android.os.PowerManager;
 import android.os.SystemClock;
 import android.util.Log;
 
 public class Commands {
 
 	public static final String CMD_GET_VERSION = "Test Version";
 
 	public static final String CMD_GET_WIFI_INFO = "WIFI Level and WIFI Address";
 
 	public static final String CMD_GET_BLE_INFO = "BLE Level and BLE Address";
 	public static final String CMD_OPEN_BLE = "BLE Open";
 	public static final String CMD_CLOSE_BLE = "BLE Close";
 
 	public static final String CMD_SCREEN_RED = "Screen Red";
 	public static final String CMD_SCREEN_GREEN = "Screen Green";
 	public static final String CMD_SCREEN_BLUE = "Screen Blue";
 	public static final String CMD_SCREEN_BLACK = "Screen Black";
 	public static final String CMD_SCREEN_WITHE = "Screen White";
 	public static final String CMD_SCREEN_NORMAL = "Screen Normal";
 
 	public static final String CMD_SLEEP = "Android Sleep";
 	public static final String CMD_SCREEN_OFF = "Screen Off";
 	public static final String CMD_SCREEN_ON = "Screen On";
 
 	public static final String CMD_TAKE_A_PICTURE = "Camera Catch";
 	public static final String CMD_RECODE_AUDIO = "Record Audio";
 	public static final String CMD_PLAY_AUDIO = "Play Audio";
 	public static final String CMD_SET_VOLUME = "Volume=";
 
 	public static final String CMD_GET_GSENSOR_COORDINATE = "3D XYZ";
 	public static final String CMD_SD_WRITE = "SD Write";
 	public static final String CMD_SET_TIME = "Time Setup";
 	public static final String CMD_CHECK_FILE = "Check File";
 	public static final String CMD_SN_WRITE = "SN Write";
 	public static final String CMD_SN_READ = "SN Read";
 	public static final String CMD_CLEAR_HISTORY = "Clear History";
 	public static final String CMD_FACTORY_RESET = "Factory Reset";
 	public static final String CMD_TEST_END = "Test End";
 
 	public static final HashMap<String, String> mapCmds = new HashMap<String, String>();
 
 	private static final String TAG = "Commands";
 
 	private static Commands instance;
 	private Context mContext;
 	private AudioManager am;
 	private PowerManager pm;
 	private DevicePolicyManager dpm;
 	private static String mSn;
 	private static String mDataWrite;
 	private SensorManager sensorMgr;
 	private Sensor sensor;
 	private float x;
 	private float y;
 	private float z;
 
 	private Commands(Context context) {
 		this.mContext = context;
 		this.am = (AudioManager) mContext
 				.getSystemService(Context.AUDIO_SERVICE);
 		this.pm = (PowerManager) mContext
 				.getSystemService(Context.POWER_SERVICE);
 		this.dpm = (DevicePolicyManager) mContext
 				.getSystemService(Context.DEVICE_POLICY_SERVICE);
 		sensorMgr = (SensorManager) mContext
 				.getSystemService(Context.SENSOR_SERVICE);
 		sensor = sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
 		sensorMgr.registerListener(lsn, sensor, 
 				SensorManager.SENSOR_DELAY_GAME);   
 	}
 
 	private SensorEventListener lsn = new SensorEventListener() {
 
 		public void onSensorChanged(SensorEvent e) {
 			x = e.values[SensorManager.DATA_X];
 			y = e.values[SensorManager.DATA_Y];
 			z = e.values[SensorManager.DATA_Z];
 			//Log.d(TAG,"sensor:x="+x+",y="+y+",z="+z);
 		}
 
 		public void onAccuracyChanged(Sensor s, int accuracy) {
 		}
 	};
 
 	public static Commands getInstance(Context context) {
 		if (instance == null) {
 			instance = new Commands(context);
 		}
 		return instance;
 	}
 
 	public byte[] excute(String cmd) {
 		if (cmd.toUpperCase().startsWith(CMD_GET_VERSION.toUpperCase())) {
 			return getVersion();
 		} else if (cmd.toUpperCase()
 				.startsWith(CMD_GET_WIFI_INFO.toUpperCase())) {
 			return getWifiInfo();
 		} else if (cmd.toUpperCase().startsWith(CMD_GET_BLE_INFO.toUpperCase())) {
 			return getBleInfo();
 		} else if (cmd.toUpperCase().startsWith(CMD_OPEN_BLE.toUpperCase())) {
 			return enableBle(true);
 		} else if (cmd.toUpperCase().startsWith(CMD_CLOSE_BLE.toUpperCase())) {
 			return enableBle(false);
 		} else if (cmd.toUpperCase().startsWith(CMD_SCREEN_RED.toUpperCase())) {
 			return showBlankScreen(cmd, MainActivity.COLOR_RED, true);
 		} else if (cmd.toUpperCase().startsWith(CMD_SCREEN_GREEN.toUpperCase())) {
 			return showBlankScreen(cmd, MainActivity.COLOR_GREEN, true);
 		} else if (cmd.toUpperCase().startsWith(CMD_SCREEN_BLUE.toUpperCase())) {
 			return showBlankScreen(cmd, MainActivity.COLOR_BLUE, true);
 		} else if (cmd.toUpperCase().startsWith(CMD_SCREEN_BLACK.toUpperCase())) {
 			return showBlankScreen(cmd, MainActivity.COLOR_BLACK, true);
 		} else if (cmd.toUpperCase().startsWith(CMD_SCREEN_WITHE.toUpperCase())) {
 			return showBlankScreen(cmd, MainActivity.COLOR_WHITE, true);
 		} else if (cmd.toUpperCase()
 				.startsWith(CMD_SCREEN_NORMAL.toUpperCase())) {
 			return showBlankScreen(cmd, MainActivity.COLOR_WHITE, false);
 		} else if (cmd.toUpperCase().startsWith(
 				CMD_TAKE_A_PICTURE.toUpperCase())) {
 			return takePhoto(cmd);
 		} else if (cmd.toUpperCase().startsWith(CMD_RECODE_AUDIO.toUpperCase())) {
 			return recodAudio(cmd);
 		} else if (cmd.toUpperCase().startsWith(CMD_PLAY_AUDIO.toUpperCase())) {
 			return playAudio(cmd);
 		} else if (cmd.toUpperCase().startsWith(CMD_SET_VOLUME.toUpperCase())) {
 			return setVolume(cmd);
 		} else if (cmd.toUpperCase().startsWith(
 				CMD_GET_GSENSOR_COORDINATE.toUpperCase())) {
 			return getGsensorCoordinate();
 		}
 
 		else if (cmd.toUpperCase().startsWith(CMD_SD_WRITE.toUpperCase())) {
 			return writeFileToSdcard(cmd);
 		} else if (cmd.toUpperCase().startsWith(CMD_SET_TIME.toUpperCase())) {
 			return setTime(cmd);
 		} else if (cmd.toUpperCase().startsWith(CMD_CHECK_FILE.toUpperCase())) {
 
 		} else if (cmd.toUpperCase().startsWith(CMD_SN_WRITE.toUpperCase())) {
 			return writeSN(cmd);
 		} else if (cmd.toUpperCase().startsWith(CMD_SN_READ.toUpperCase())) {
 			return readSN(cmd);
 		} else if (cmd.toUpperCase()
 				.startsWith(CMD_CLEAR_HISTORY.toUpperCase())) {
 			return clearHistory(cmd);
 		} else if (cmd.toUpperCase()
 				.startsWith(CMD_FACTORY_RESET.toUpperCase())) {
 			return factoryReset(cmd);
 		} else if (cmd.toUpperCase().startsWith(CMD_TEST_END.toUpperCase())) {
 			return testEnd(cmd);
 		} else if (cmd.toUpperCase().startsWith(CMD_SLEEP.toUpperCase())) {
 			return screenOff(cmd);
 		}
 		return null;
 	}
 
 	private byte[] getVersion() {
 		String version = "Test_Version=" + Utils.getVersion(mContext) + "\r\n";
 		return version.getBytes();
 	}
 
 	private byte[] getWifiInfo() {
 		String result = String.format("Level=%dDB and Address=%s\r\n",
 				WifiHelper.getInstance(mContext).getWifiSignal(), WifiHelper
 						.getInstance(mContext).getWifiMAC());
 		return result.getBytes();
 	}
 
 	private byte[] getBleInfo() {
 		String result = String.format("Level=%dDB and Address=%s\r\n", -50,BleHelper.getBleMAC());
 		return result.getBytes();
 	}
 
 	private byte[] enableBle(boolean enable) {
 		String result = enable ? "BLE Open ERROR\r\n" : "BLE Close ERROR\r\n";
 		return result.getBytes();
 	}
 
 	private byte[] showBlankScreen(String cmd, int color, boolean fullScreen) {
 		Intent intent = new Intent(Intents.ACTION_FULLSCREEN_STATE_CHANGE);
 		intent.putExtra("full_screen", fullScreen);
 		intent.putExtra("background_color", color);
 		mContext.sendBroadcast(intent);
 		String result = cmd + " OK\r\n";
 		return result.getBytes();
 	}
 
 	private byte[] getGsensorCoordinate() {
 
 		String result = String.format("X=%f,Y=%f,Z=%f\r\n", x, y, z);
 		return result.getBytes();
 	}
 
 	private byte[] setVolume(String cmd) {
 		String volume = cmd.substring(cmd.lastIndexOf('=') + 1);
 		String result = "";
 		try {
 			int iVolume = Integer.parseInt(volume);
 
 			Log.d(TAG, "set Volume to " + iVolume);
 			am.setStreamVolume(AudioManager.STREAM_MUSIC, iVolume,
 					AudioManager.FLAG_SHOW_UI); // tempVolume:ֵ
 			result = cmd + " OK\r\n";
 		} catch (Exception ex) {
 			result = cmd + " ERROR\r\n";
 		}
 
 		return result.getBytes();
 	}
 
 	private byte[] screenOn(String cmd) {
 		return (cmd + " OK\r\n").getBytes();
 	}
 
 	private byte[] screenOff(String cmd) {
 		try {
 			dpm.lockNow();
 		} catch (Exception ex) {
 			return (cmd + " ERROR\r\n").getBytes();
 		}
 		return (cmd + " OK\r\n").getBytes();
 	}
 
 	private byte[] recodAudio(String cmd) {
 		try {
 			Thread.sleep(5000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		return "Record Audio OK\r\n".getBytes();
 	}
 
 	private byte[] playAudio(String cmd) {
 		try {
 			Thread.sleep(5000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		return (cmd + " OK\r\n").getBytes();
 	}
 
 	private byte[] takePhoto(String cmd) {
 		InputStream is = mContext.getResources().openRawResource(
 				R.raw.photo_demo);
 		byte[] buffer = null;
 
 		try {
 			int lenght = is.available();
 			// byte
 			buffer = new byte[lenght];
 			is.read(buffer);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		// byte[] result= buff.toByteArray();
 
 		return buffer;
 	}
 
 	private byte[] writeFileToSdcard(String cmd) {
 		// File sdPatch = Environment.getExternalStorageDirectory();
 		mDataWrite = cmd.substring(cmd.lastIndexOf('=') + 1);
 		// Log.d(TAG, "write file to " + sdPatch);
 		String result = "SD Read=" + mDataWrite + "\r\n";
 		return result.getBytes();
 	}
 
 	private byte[] writeSN(String cmd) {
 		mSn = cmd.substring(cmd.lastIndexOf('=') + 1);
 
 		return (cmd + " OK\r\n").getBytes();
 	}
 
 	private byte[] readSN(String cmd) {
 		String result = "SN Read=" + mSn + "\r\n";
 		return result.getBytes();
 	}
 
 	private byte[] setTime(String cmd) {
		//boolean isSuc = SystemClock.setCurrentTimeMillis(0);	
 		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		Date curDate = new Date(System.currentTimeMillis());// ȡǰʱ
 		String str = formatter.format(curDate);
 		String result = "Date Time=" + str + "\r\n";
 		return result.getBytes();
 	}
 
 	private byte[] clearHistory(String cmd) {
 		return (cmd + " OK\r\n").getBytes();
 	}
 
 	private byte[] factoryReset(String cmd) {
 		return (cmd + " OK\r\n").getBytes();
 	}
 
 	private byte[] testEnd(String cmd) {
 		return (cmd + " OK\r\n").getBytes();
 	}
 }
