 package com.zjuhjz.yacleaner;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import android.Manifest.permission;
 import android.content.ComponentName;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.pm.PackageManager;
 import android.provider.Settings;
 import android.provider.Settings.SettingNotFoundException;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.zjuhjz.yacleaner.db.IntentFilterInfo;
 import com.zjuhjz.yacleaner.tool.Constants;
 
 //TODO optimize AutoStartInfo data structure.change 
 //     <HashMap<String, Object>intentsAppInfoList to class object
 public class AutoStartInfo {
 	// all broadcast Actions
 
 	// Intents to App
 	List<HashMap<String, Object>> intentsInfoList = null;
 	List<HashMap<String, Object>> intentsAppInfoList = null;
 	HashMap<String, Object> intentItem = null;
 	HashMap<String, Object> intentsAppInfo = null;
 
 	// App to Intents
 	List<HashMap<String, Object>> appInfoList = null;
 	List<HashMap<String, Object>> appIntentsInfoList = null;
 	HashMap<String, Object> appItem = null;
 	HashMap<String, Object> appIntentsInfo = null;
 
 	private static final String TAG = "yacleanerlog";
 	PackageManager packageManager = null;
 	Context context = null;
 	ArrayList<IntentFilterInfo> info = null;
 
 	private boolean includeSystem = false;
 	final private static String HISTORY_FILE_NAME = "blockHistory";
 	HashMap<String, String> historyList;
 
 	AutoStartInfo(Context context) {
 		this.context = context;
 		intentsInfoList = new ArrayList<HashMap<String, Object>>();
 		appInfoList = new ArrayList<HashMap<String, Object>>();
 		includeSystem = false;
 		packageManager = context.getPackageManager();
 		historyList = new HashMap<String, String>();
		loadHistory();
 		refresh();
 	}
 
 	private void loadReceiverReader() {
 		ReceiverReader receiverReader = new ReceiverReader(context, null);
 		info = receiverReader.load();
 	}
 
 	public boolean loadHistory() {
 		try {
 			FileInputStream fileInputStream = context
 					.openFileInput(HISTORY_FILE_NAME);
 			byte[] buffer = new byte[1024];
 			StringBuffer fileContent = new StringBuffer("");
 			Log.d(TAG, "file opened");
 			while (fileInputStream.read(buffer) != -1) {
 				fileContent.append(new String(buffer));
 			}
 			String data = new String(fileContent);
 			String[] dataList = data.split("\n");
 			String[] dataItem;
			
 			for (String i : dataList) {
 				dataItem = i.split(" ");
				if(dataItem.length==2){
					historyList.put(dataItem[0], dataItem[1]);
					Log.d(TAG, dataItem[0]+" "+dataItem[1]);
				}
			
 			}
//			Log.d(TAG, data);
//			for(Map.Entry<String, String> i : historyList.entrySet()){
//				Log.d(TAG, i.getKey()+" "+i.getValue());
//			}
 			fileInputStream.close();
 		} catch (FileNotFoundException e) {
 			try {
 				FileOutputStream fileOutputStream = context.openFileOutput(
 						HISTORY_FILE_NAME, 0);
 				fileOutputStream.close();
 				Log.d(TAG, "file created");
 			} catch (FileNotFoundException e1) {
 				// TODO Auto-generated catch block
 				// e1.printStackTrace();
 			} catch (IOException e2) {
 				// TODO Auto-generated catch block
 				// e.printStackTrace();
 			}
 		} catch (IOException e) {
 			// e.printStackTrace();
 		}
		
 		return true;
 	}
 
 	private boolean saveHistory() {
 		StringBuffer fileContent = new StringBuffer("");
 		for (Map.Entry<String, String> entry : historyList.entrySet()) {
 			fileContent.append(entry.getKey() + " " + entry.getValue() + "\n");
 		}
 		try {
 			FileOutputStream fileOutputStream = context.openFileOutput(
 					HISTORY_FILE_NAME, Context.MODE_PRIVATE);
 			fileOutputStream.write(fileContent.toString().getBytes());
 		} catch (FileNotFoundException e) {
 			return false;
 		} catch (IOException e) {
 			return false;
 		}
 		return true;
 	}
 
 	private void refreshListDataSource() {
 		String intentName;
 		Collections.sort(info, new Comparator<IntentFilterInfo>() {
 			@Override
 			public int compare(IntentFilterInfo lhs, IntentFilterInfo rhs) {
 				if (lhs != null && rhs != null) {
 					return lhs.componentInfo.packageInfo.packageName
 							.compareTo(rhs.componentInfo.packageInfo.packageName);
 				}
 				return 0;
 			}
 		});
 		appInfoList.clear();
 
 		String appName;
 		String lastAppName = "";
 		String historyStatus;
 		for (IntentFilterInfo intentFilterInfo : info) {
 			appName = intentFilterInfo.componentInfo.packageInfo.packageLabel == null ? intentFilterInfo.componentInfo.packageInfo.packageName
 					: intentFilterInfo.componentInfo.packageInfo.packageLabel;
 			intentName = intentFilterInfo.action;
 			if (intentFilterInfo.componentInfo.packageInfo.isSystem
 					&& (!includeSystem)) {
 				continue;
 			}
 			if (!appName.equals(lastAppName)) {
 				appItem = new HashMap<String, Object>();
 				appInfoList.add(appItem);
 				appItem.put("appName", appName);
 				appItem.put("appIcon",
 						intentFilterInfo.componentInfo.packageInfo.icon);
 				appItem.put("package_name",
 						intentFilterInfo.componentInfo.packageInfo.packageName);
 				historyStatus = historyList
 						.get(intentFilterInfo.componentInfo.packageInfo.packageName);
 				if (historyStatus == null) {
 					historyStatus = "default";
 				}
 				appItem.put("historyStatus", historyStatus);
 				historyList.put(intentFilterInfo.componentInfo.packageInfo.packageName, historyStatus);
 				appIntentsInfoList = new ArrayList<HashMap<String, Object>>();
 				appItem.put("intentInfoList", appIntentsInfoList);
 			}
 			appIntentsInfo = new HashMap<String, Object>();
 			appIntentsInfo.put("component_name",
 					intentFilterInfo.componentInfo.componentName);
 			appIntentsInfo.put("package_name",
 					intentFilterInfo.componentInfo.packageInfo.packageName);
 			appIntentsInfo.put("is_system",
 					intentFilterInfo.componentInfo.packageInfo.isSystem);
 			appIntentsInfo.put("enable_state",
 					intentFilterInfo.componentInfo.currentEnabledState == 2 ? 0
 							: 1);
 			appIntentsInfo.put("default_enabled",
 					intentFilterInfo.componentInfo.defaultEnabled);
 			appIntentsInfo.put("icon",
 					intentFilterInfo.componentInfo.packageInfo.icon);
 			appIntentsInfo.put("intentName", intentName);
 			appIntentsInfo.put("display", intentName + "\n"
 					+ intentFilterInfo.componentInfo.componentName);
 			appIntentsInfoList.add(appIntentsInfo);
 
 			lastAppName = appName;
 
 		}
 		saveHistory();
 	}
 
 	public void refresh() {
 		// load();
 		loadReceiverReader();
 		refreshListDataSource();
 	}
 
 	public void changeIncludeSystemFlag() {
 		includeSystem = !includeSystem;
 		refreshListDataSource();
 	}
 	
 	
 	public boolean unBlockAll(HashMap<String, Object> list) {
 		@SuppressWarnings("unchecked")
 		List<HashMap<String, Object>> mIntentsInfoList = (List<HashMap<String, Object>>) list
 				.get("intentInfoList");
 		List<String[]> unBlockComponentList = new ArrayList<String[]>();
 		for (HashMap<String, Object> mIntentsInfo : mIntentsInfoList) {
 			String[] unBlockComponent = new String[] {
 					(String) mIntentsInfo.get("component_name"),
 					(String) mIntentsInfo.get("package_name") };
 			unBlockComponentList.add(unBlockComponent);
 		}
 		for (String[] i : unBlockComponentList) {
 			setComponentEnable(true, i[0], i[1]);
 		}
 		
 		list.put("historyStatus", "default");
 		historyList.put((String)list.get("package_name"),"default");
 		saveHistory();
 		Toast.makeText(context, "unblock succesfully", Toast.LENGTH_SHORT)
 				.show();
 		return true;
 	}
 
 	public boolean blockCompelete(HashMap<String, Object> list) {
 		@SuppressWarnings("unchecked")
 		List<HashMap<String, Object>> mIntentsInfoList = (List<HashMap<String, Object>>) list
 				.get("intentInfoList");
 		List<String[]> blockComponentList = new ArrayList<String[]>();
 		for (HashMap<String, Object> mIntentsInfo : mIntentsInfoList) {
 			String[] blockComponent = new String[] {
 					(String) mIntentsInfo.get("component_name"),
 					(String) mIntentsInfo.get("package_name") };
 			blockComponentList.add(blockComponent);
 		}
 		for (String[] i : blockComponentList) {
 			setComponentEnable(false, i[0], i[1]);
 		}
 
 		list.put("historyStatus", "blockedCompeletely");
 		historyList.put((String)list.get("package_name"),"blockedCompeletely");
 		saveHistory();
 		Toast.makeText(context, "block succesfully", Toast.LENGTH_SHORT)
 				.show();
 		return true;
 	}
 
 	public boolean blockStrong(HashMap<String, Object> list) {
 		@SuppressWarnings("unchecked")
 		List<HashMap<String, Object>> mIntentsInfoList = (List<HashMap<String, Object>>) list
 				.get("intentInfoList");
 		List<String> blockIntentsNameList = new ArrayList<String>();
 		for (String[] i : Constants.broadcastActions) {
 			blockIntentsNameList.add(i[0]);
 		}
 		List<String[]> blockComponentList = new ArrayList<String[]>();
 		for (HashMap<String, Object> mIntentsInfo : mIntentsInfoList) {
 			if (blockIntentsNameList.contains((String) mIntentsInfo
 					.get("intentName"))) {
 				String[] blockComponent = new String[] {
 						(String) mIntentsInfo.get("component_name"),
 						(String) mIntentsInfo.get("package_name") };
 				blockComponentList.add(blockComponent);
 			}
 		}
 		for (String[] i : blockComponentList) {
 			setComponentEnable(false, i[0], i[1]);
 		}
 		
 		list.put("historyStatus", "blockedStrongly");
 		historyList.put((String)list.get("package_name"),"blockedStrongly");
 		saveHistory();
 		Toast.makeText(context, "block succesfully", Toast.LENGTH_SHORT)
 				.show();
 		return true;
 	}
 
 	public boolean blockGentle(HashMap<String, Object> list) {
 		@SuppressWarnings("unchecked")
 		List<HashMap<String, Object>> mIntentsInfoList = (List<HashMap<String, Object>>) list
 				.get("intentInfoList");
 		List<String> blockIntentsNameList = new ArrayList<String>();
 		for (String i : Constants.gentleIntentsList) {
 			blockIntentsNameList.add(i);
 		}
 		List<String[]> blockComponentList = new ArrayList<String[]>();
 		for (HashMap<String, Object> mIntentsInfo : mIntentsInfoList) {
 			if (blockIntentsNameList.contains((String) mIntentsInfo
 					.get("intentName"))) {
 				String[] blockComponent = new String[] {
 						(String) mIntentsInfo.get("component_name"),
 						(String) mIntentsInfo.get("package_name") };
 				blockComponentList.add(blockComponent);
 			}
 		}
 		for (String[] i : blockComponentList) {
 			setComponentEnable(false, i[0], i[1]);
 		}
 		list.put("historyStatus", "blockedGentlely");
 		historyList.put((String)list.get("package_name"),"blockedGentlely");
 		saveHistory();
 		Toast.makeText(context, "block succesfully", Toast.LENGTH_SHORT)
 				.show();
 		return true;
 	}
 
 	private boolean setComponentEnable(boolean enable, String componentName,
 			String packageName) {
 		ContentResolver cr = context.getContentResolver();
 		boolean adbNeedsRedisable = false;
 		boolean adbEnabled;
 
 		if (componentName == null || componentName.isEmpty()
 				|| packageName == null || packageName.isEmpty()) {
 			return false;
 		}
 		try {
 			adbEnabled = (Settings.Secure.getInt(cr,
 					Settings.Secure.ADB_ENABLED) == 1);
 		} catch (SettingNotFoundException e) {
 			// This started to happen at times on the ICS emulator
 			// (and possibly one user reported it).
 			Log.w(TAG, "Failed to read adb_enabled setting, assuming no", e);
 			adbEnabled = false;
 		}
 
 		// If adb is disabled, try to enable it, temporarily. This will
 		// make our root call go through without hanging.
 		// TODO: It seems this might no longer be required under ICS.
 		if (!adbEnabled) {
 			Log.i(TAG, "Switching ADB on for the root call");
 			if (setADBEnabledState(cr, true)) {
 				adbEnabled = true;
 				adbNeedsRedisable = true;
 				// Let's be extra sure we don't run into any timing-related
 				// hiccups.
 				sleep(1000);
 			}
 		}
 
 		try {
 			// Run the command; we have different invocations we can try, but
 			// we'll stop at the first one we succeed with.
 			//
 			// On ICS, it became necessary to set a library path (which is
 			// cleared for suid programs, for obvious reasons). It can't hurt
 			// on older versions. See also
 			// https://github.com/ChainsDD/su-binary/issues/6
 			final String libs = "LD_LIBRARY_PATH=\"$LD_LIBRARY_PATH:/system/lib\" ";
 			boolean success = false;
 			for (String[] set : new String[][] {
 					{ libs + "pm %s '%s/%s'", null },
 					{ libs + "sh /system/bin/pm %s '%s/%s'", null },
 					{
 							libs
 									+ "app_process /system/bin com.android.commands.pm.Pm %s '%s/%s'",
 							"CLASSPATH=/system/framework/pm.jar" },
 					{
 							libs
 									+ "/system/bin/app_process /system/bin com.android.commands.pm.Pm %s '%s/%s'",
 							"CLASSPATH=/system/framework/pm.jar" }, }) {
 				if (Utils.runRootCommand(String.format(set[0],
 						(enable ? "enable" : "disable"), packageName,
 						componentName),
 						(set[1] != null) ? new String[] { set[1] } : null,
 						// The timeout shouldn't really be needed ever, since
 						// we now automatically enable ADB, which should work
 						// around any freezing issue. However, in rare, hard
 						// to reproduce cases, it still occurs, and in those
 						// cases the timeout will improve the user experience.
 						25000)) {
 					success = true;
 					break;
 				}
 			}
 
 			// We are happy if both the command itself succeed (return code)...
 			if (!success)
 				return false;
 
 			context.getPackageManager();
 			new ComponentName(packageName, componentName);
 
 			// success = mComponent.isCurrentlyEnabled() == mDoEnable;
 			// if (success)
 			Log.i(TAG, "State successfully changed");
 			// else
 			// Log.i(ListActivity.TAG, "State change failed");
 			// return success;
 			return true;
 		} finally {
 			if (adbNeedsRedisable) {
 				Log.i(TAG, "Switching ADB off again");
 				setADBEnabledState(cr, false);
 				// Delay releasing the GUI for a while, there seems to
 				// be a mysterious problem of repeating this process multiple
 				// times causing it to somehow lock up, no longer work.
 				// I'm hoping this might help.
 				sleep(5000);
 			}
 		}
 	}
 
 	private boolean setADBEnabledState(ContentResolver cr, boolean enable) {
 		if (context.checkCallingOrSelfPermission(
 				permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED) {
 			Log.i(TAG, "Using secure settings API to touch ADB setting");
 			return Settings.Secure.putInt(cr, Settings.Secure.ADB_ENABLED,
 					enable ? 1 : 0);
 		} else {
 			Log.i(TAG, "Using setprop call to touch ADB setting");
 			return Utils.runRootCommand(String.format(
 					"setprop persist.service.adb.enable %s", enable ? 1 : 0),
 					null, null);
 		}
 	}
 
 	public static void sleep(long time) {
 		try {
 			Thread.sleep(time);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 }
