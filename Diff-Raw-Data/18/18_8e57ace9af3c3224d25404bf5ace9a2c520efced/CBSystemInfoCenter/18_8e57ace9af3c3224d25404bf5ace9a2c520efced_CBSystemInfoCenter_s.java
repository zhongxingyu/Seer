 package com.android.cb.systeminfocollector;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashMap;
 
 import android.app.Activity;
 import android.app.ActivityManager;
 import android.content.Context;
 import android.net.wifi.WifiManager;
 import android.os.Environment;
 import android.os.StatFs;
 import android.telephony.TelephonyManager;
 import android.util.DisplayMetrics;
 
 public class CBSystemInfoCenter {
 
 	TelephonyManager mTelephonyManager;
 	Context mContext;
 	DisplayMetrics mDisplayMetrics;
 
 	public CBSystemInfoCenter(Context context) {
 		mContext = context;
 		mDisplayMetrics = new DisplayMetrics();
 		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
 		mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
 	}
 
 	public HashMap<String, String> getInfoTable() {
 		HashMap<String, String> table = new HashMap<String, String>();
 
 		table.put("AndroidSystemId", getAndroidSystemId());
 		table.put("DeviceId", getDeviceId());
 		table.put("DeviceSoftwareVersion", getDeviceSoftwareVersion());
 		table.put("Line1Number", getLine1Number());
 		table.put("NetworkCountryIso", getNetworkCountryIso());
 		table.put("NetworkOperatorName", getNetworkOperatorName());
 		table.put("NetworkType", String.valueOf(getNetworkType()));
 		table.put("PhoneType", String.valueOf(getPhoneType()));
 		table.put("SimCountryIso", getSimCountryIso());
 		table.put("SimOperator", getSimOperator());
 		table.put("SimOperatorName", getSimOperatorName());
 		table.put("SimSerialNumber", getSimSerialNumber());
 		table.put("SimState", String.valueOf(getSimState()));
 		table.put("SubscriberId", getSubscriberId());
 		table.put("VoiceMailNumber", getVoiceMailNumber());
 		table.put("HeightPixels", String.valueOf(getHeightPixels()));
 		table.put("WidthPixels", String.valueOf(getWidthPixels()));
 		table.put("ScreenDensity", String.valueOf(getScreenDensity()));
 		table.put("ScreenDensityDpi", String.valueOf(getScreenDensityDpi()));
 		table.put("RamInfo", getRamInfo());
 		table.put("AvailbleRamSize", String.valueOf(getAvailbleRamSize()));
 		table.put("CPUInfo", getCPUInfo());
 		table.put("Mac", getMac());
 		table.put("KernelInfo", getKernelInfo());
 		table.put("SdSize", String.valueOf(getSdSize()));
 		table.put("InternalMemorySize", String.valueOf(getInternalMemorySize()));
 		table.put("RomSize", String.valueOf(getRomSize()));
 
 		return table;
 	}
 
 	public long getRomSize() {
 		File path = Environment.getDataDirectory();
         StatFs stat = new StatFs(path.getPath());
         long blockSize = stat.getBlockSize();
         long availableBlocks = stat.getAvailableBlocks();
         return blockSize * availableBlocks;
 	}
 
 	public long getInternalMemorySize() {
 		File path = Environment.getDataDirectory();
         StatFs stat = new StatFs(path.getPath());
         long blockSize = stat.getBlockSize();
         long totalBlocks = stat.getBlockCount();
         return totalBlocks * blockSize;
 	}
 
 	public long getSdSize() {
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
 
 	public String getMac() {
 		WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
 		return wifiManager.getConnectionInfo().getMacAddress();
 	}
 
 	private String readInfoFromFile(String path) {
 		String info = "";
 		String line = new String();
 		try {
 			FileReader fr = new FileReader(path);
 			BufferedReader localBufferedReader = new BufferedReader(fr, 8192);
 			while ((line = localBufferedReader.readLine()) != null) {
 				info += "\n";
 				info += line;
 			}
 		} catch (IOException e) {
 		}
 		return info;
 	}
 
 	public String getKernelInfo() {
 		String file = "/proc/cpuinfo";
 		return readInfoFromFile(file);
 	}
 
 	public String getCPUInfo() {
 		String file = "/proc/cpuinfo";
 		return readInfoFromFile(file);
 	}
 
 	public String getRamInfo() {
 		String file = "/proc/meminfo";
 		return readInfoFromFile(file);
 	}
 
 	public long getAvailbleRamSize() {
 		ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE); 
         ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo(); 
         am.getMemoryInfo(mi); 
         return mi.availMem;
 	}
 
 	public String getAndroidSystemId() {
 		String res = android.provider.Settings.System.getString(mContext.getContentResolver(), "android_id");
 		return res;
 	}
 
 	public int getHeightPixels() {
 		return mDisplayMetrics.heightPixels;
 	}
 
 	public int getWidthPixels() {
 		return mDisplayMetrics.widthPixels;
 	}
 
 	public float getScreenDensity() {
 		return mDisplayMetrics.density;
 	}
 
 	public int getScreenDensityDpi() {
 		return mDisplayMetrics.densityDpi;
 	}
 
 	public String getDeviceId() {
 		return mTelephonyManager.getDeviceId();
 	}
 
 	public String getDeviceSoftwareVersion() {
 		return mTelephonyManager.getDeviceSoftwareVersion();
 	}
 
 	public String getLine1Number() {
 		return mTelephonyManager.getLine1Number();
 	}
 
 	public String getNetworkCountryIso() {
 		return mTelephonyManager.getNetworkCountryIso();
 	}
 
 	public String getNetworkOperatorName() {
 		return mTelephonyManager.getNetworkOperatorName();
 	}
 
 	public int getNetworkType() {
 		return mTelephonyManager.getNetworkType();
 	}
 	
 	public int getPhoneType() {
 		return mTelephonyManager.getPhoneType();
 	}
 
 	public String getSimCountryIso() {
 		return mTelephonyManager.getSimCountryIso();
 	}
 
 	public String getSimOperator() {
 		return mTelephonyManager.getSimOperator();
 	}
 
 	public String getSimOperatorName() {
 		return mTelephonyManager.getSimOperatorName();
 	}
 
 	public String getSimSerialNumber() {
 		return mTelephonyManager.getSimSerialNumber();
 	}
 
 	public int getSimState() {
 		return mTelephonyManager.getSimState();
 	}
 
 	/** IMSI */
 	public String getSubscriberId() {
 		return mTelephonyManager.getSubscriberId();
 	}
 
 	public String getVoiceMailNumber() {
 		return mTelephonyManager.getVoiceMailNumber();
 	}
 }
