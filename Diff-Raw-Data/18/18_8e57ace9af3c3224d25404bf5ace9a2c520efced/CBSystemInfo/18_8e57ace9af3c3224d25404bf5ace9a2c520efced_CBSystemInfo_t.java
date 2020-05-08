 /**
  * @Title: CBSystemInfo.java
  * @Package: com.android.cb.source
  * @Author: Raiden Awkward<raiden.ht@gmail.com>
  * @Date: 2012-4-1
  */
 package com.android.cb.source;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 
 import android.app.Activity;
 import android.app.ActivityManager;
 import android.content.Context;
 import android.net.wifi.WifiManager;
 import android.os.Environment;
 import android.os.StatFs;
 import android.telephony.TelephonyManager;
 import android.util.DisplayMetrics;
 
 /**
  * @author raiden
  *
  * @Description class for getting system informations
  */
 public class CBSystemInfo {
 
 	public static long getRomSize() {
 		File path = Environment.getDataDirectory();
         StatFs stat = new StatFs(path.getPath());
         long blockSize = stat.getBlockSize();
         long availableBlocks = stat.getAvailableBlocks();
         return blockSize * availableBlocks;
 	}
 
 	public static long getInternalMemorySize() {
 		File path = Environment.getDataDirectory();
         StatFs stat = new StatFs(path.getPath());
         long blockSize = stat.getBlockSize();
         long totalBlocks = stat.getBlockCount();
         return totalBlocks * blockSize;
 	}
 
 	public static long getSdSize() {
         String state = Environment.getExternalStorageState();
         if (Environment.MEDIA_MOUNTED.equals(state)) {
             File sdcardDir = Environment.getExternalStorageDirectory();
             StatFs sf = new StatFs(sdcardDir.getPath());
             long bSize = sf.getBlockSize();
             long bCount = sf.getBlockCount();
 
             return bSize * bCount;
         }
         return -1;
 	}
 
 	public static String getMac(Context context) {
 		if (context == null)
 			return null;
 
 		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
 		return wifiManager.getConnectionInfo().getMacAddress();
 	}
 
 	private static String readInfoFromFile(String path) {
 		String info = "";
 		String line = new String();
 		try {
 			FileReader fr = new FileReader(path);
 			BufferedReader localBufferedReader = new BufferedReader(fr, 8192);
 			while ((line = localBufferedReader.readLine()) != null) {
 				info += "\n";
 				info += line;
 			}
			localBufferedReader.close();
 		} catch (IOException e) {
 		}
 		return info;
 	}
 
 	public static String getKernelInfo() {
 		String file = "/proc/cpuinfo";
 		return readInfoFromFile(file);
 	}
 
 	public static String getCPUInfo() {
 		String file = "/proc/cpuinfo";
 		return readInfoFromFile(file);
 	}
 
 	public static String getRamInfo() {
 		String file = "/proc/meminfo";
 		return readInfoFromFile(file);
 	}
 
 	public static long getAvailbleRamSize(Context context) {
 		if (context == null)
 			return -1;
 
 		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
         ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
         am.getMemoryInfo(mi);
         return mi.availMem;
 	}
 
 	public static String getAndroidSystemId(Context context) {
 		if (context == null)
 			return null;
 
 		String res = android.provider.Settings.System.getString(context.getContentResolver(), "android_id");
 		return res;
 	}
 
 	public static DisplayMetrics getDisplayMetrics(Context context) {
 		if (context == null)
 		return null;
 
 		DisplayMetrics res = new DisplayMetrics();
 		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(res);
 
 		return res;
 	}
 
 	public static int getHeightPixels(Context context) {
 		DisplayMetrics metrics = getDisplayMetrics(context);
 		if (metrics == null)
 			return -1;
 
 		return metrics.heightPixels;
 	}
 
 	public static int getWidthPixels(Context context) {
 		DisplayMetrics metrics = getDisplayMetrics(context);
 		if (metrics == null)
 			return -1;
 
 		return metrics.widthPixels;
 	}
 
 	public static float getScreenDensity(Context context) {
 		DisplayMetrics metrics = getDisplayMetrics(context);
 		if (metrics == null)
 			return -1;
 
 		return metrics.density;
 	}
 
 	public static int getScreenDensityDpi(Context context) {
 		DisplayMetrics metrics = getDisplayMetrics(context);
 		if (metrics == null)
 			return -1;
 
 		return metrics.densityDpi;
 	}
 
 	public static TelephonyManager getTelephonyManager(Context context) {
 		if (context == null)
 			return null;
 
 		TelephonyManager res = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
 		return res;
 	}
 
 	public static String getDeviceId(Context context) {
 		TelephonyManager manager = getTelephonyManager(context);
 		if (manager == null)
 			return null;
 
 		return manager.getDeviceId();
 	}
 
 	public static String getDeviceSoftwareVersion(Context context) {
 		TelephonyManager manager = getTelephonyManager(context);
 		if (manager == null)
 			return null;
 
 		return manager.getDeviceSoftwareVersion();
 	}
 
 	public static String getLine1Number(Context context) {
 		TelephonyManager manager = getTelephonyManager(context);
 		if (manager == null)
 			return null;
 
 		return manager.getLine1Number();
 	}
 
 	public static String getNetworkCountryIso(Context context) {
 		TelephonyManager manager = getTelephonyManager(context);
 		if (manager == null)
 			return null;
 
 		return manager.getNetworkCountryIso();
 	}
 
 	public static String getNetworkOperatorName(Context context) {
 		TelephonyManager manager = getTelephonyManager(context);
 		if (manager == null)
 			return null;
 
 		return manager.getNetworkOperatorName();
 	}
 
 	public static int getNetworkType(Context context) {
 		TelephonyManager manager = getTelephonyManager(context);
 		if (manager == null)
 			return -1;
 
 		return manager.getNetworkType();
 	}
 
 	public static int getPhoneType(Context context) {
 		TelephonyManager manager = getTelephonyManager(context);
 		if (manager == null)
 			return -1;
 
 		return manager.getPhoneType();
 	}
 
 	public static String getSimCountryIso(Context context) {
 		TelephonyManager manager = getTelephonyManager(context);
 		if (manager == null)
 			return null;
 
 		return manager.getSimCountryIso();
 	}
 
 	public static String getSimOperator(Context context) {
 		TelephonyManager manager = getTelephonyManager(context);
 		if (manager == null)
 			return null;
 
 		return manager.getSimOperator();
 	}
 
 	public static String getSimOperatorName(Context context) {
 		TelephonyManager manager = getTelephonyManager(context);
 		if (manager == null)
 			return null;
 
 		return manager.getSimOperatorName();
 	}
 
 	public static String getSimSerialNumber(Context context) {
 		TelephonyManager manager = getTelephonyManager(context);
 		if (manager == null)
 			return null;
 
 		return manager.getSimSerialNumber();
 	}
 
 	public static int getSimState(Context context) {
 		TelephonyManager manager = getTelephonyManager(context);
 		if (manager == null)
 			return -1;
 
 		return manager.getSimState();
 	}
 
 	/** IMSI */
 	public static String getSubscriberId(Context context) {
 		TelephonyManager manager = getTelephonyManager(context);
 		if (manager == null)
 			return null;
 
 		return manager.getSubscriberId();
 	}
 
 	public static String getVoiceMailNumber(Context context) {
 		TelephonyManager manager = getTelephonyManager(context);
 		if (manager == null)
 			return null;
 
 		return manager.getVoiceMailNumber();
 	}
 
 }
