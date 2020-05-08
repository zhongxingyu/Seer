 package edu.kpi.asu.rduboveckij.cam.osmonitor;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.ActivityManager;
 import android.app.ActivityManager.MemoryInfo;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.BatteryManager;
 import android.os.Build;
 import android.util.Log;
 
 public class OsMonitorAPI implements IOsMonitorAPI {
 	private static final long MEGABYTE = 1048576L;
 	private MemoryInfo memoryInfo = new MemoryInfo();
 	private int level = 100;
 
 	public OsMonitorAPI(Activity activity) {
 		ActivityManager am = (ActivityManager) activity
 				.getSystemService(Context.ACTIVITY_SERVICE);
 		am.getMemoryInfo(this.memoryInfo);
 
 		IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
 		Intent intent = activity.getApplicationContext().registerReceiver(null,
 				filter);
 		double level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
 		double scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
 		this.level = (int) (level / scale * 100);
 	}
 
 	public String getOsVerison() {
 		return "API " + Build.VERSION.SDK_INT;
 	}
 
 	public String getDeviceName() {
 		return Build.MODEL;
 	}
 
 	public int getCPULoad() {
 		int result = 0;
 		try {
 			double current = SystemUtils.getCPUFrequencyCurrent();
 			double max = SystemUtils.getCPUFrequencyMax();
 			result = (int) (current / max * 100);
 		} catch (Exception e) {
 			Log.e(OsMonitorAPI.class.toString(), e.getMessage());
 		}
 		return result;
 	}
 
 	@SuppressLint("NewApi")
 	public long getTotalMemory() {
 		return memoryInfo.totalMem / MEGABYTE;
 	}
 
 	public long getFreeMemory() {
 		return memoryInfo.availMem / MEGABYTE;
 	}
 
 	public int getMemoryLoad() {
		return (int) (getFreeMemory() / getTotalMemory() * 100);
 	}
 
 	public int getEnergyLevel() {
 		return this.level;
 	}
 }
