 package com.zjuhjz.yacleaner;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import android.app.ActivityManager;
 import android.app.ActivityManager.MemoryInfo;
 import android.app.ActivityManager.RunningAppProcessInfo;
 import android.content.Context;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.util.Log;
 import android.view.Menu;
 import com.zjuhjz.yacleaner.tool.*;
 
 public class YAMemoryInfo {
 	public long totalMemory;
 	public long totalUsed;
 	
 	private static List<RunningAppProcessInfo> runningAppProcesses = null;
 	private static MemoryInfo mi = new MemoryInfo();
 	//static final int POPULATE_ID = Menu.FIRST;
 	//static final int CLEAR_ID = Menu.FIRST + 1;
 	Context context;
 	List<HashMap<String, String>> processInfoList;
 	List<HashMap<String, String>> servicesInfoList;
 	CMDExecute cmdExecute= new CMDExecute();
 	
 	YAMemoryInfo(Context context){
 		processInfoList = new ArrayList<HashMap<String, String>>();
 		servicesInfoList = new ArrayList<HashMap<String, String>>();
 		this.context = context;
 		refresh();
 	}
 	
 	public boolean refresh(){
 		refreshProcessInfo();
 		
 		return true;
 	}
 	
 	public int refreshProcessInfo() {
 		//ApplicationInfo ai
 		ApplicationInfo ai;
 		
 		//PackageManager pm
 		PackageManager pm = context.getPackageManager();
 		
 		/////////initialize///////////
 		//ActivityManager activityManager
 		ActivityManager activityManager = (ActivityManager) context
 				.getSystemService(Context.ACTIVITY_SERVICE);
 		//clear
 		if(runningAppProcesses!=null){
 			runningAppProcesses.clear();
 		}
 		if(processInfoList!=null){
 			processInfoList.clear();
 		}
 			
 		runningAppProcesses = activityManager.getRunningAppProcesses();
 		///////initialize end/////////
 		String result = null;
 		String[] items = null; 
 		try {
			//String[] args = { "/system/bin/top", "-n","1" };
			String[] args = { "/system/bin/ps"};
 			result = cmdExecute.run(args, ".");
 			//Log.d("yacleanerdebug", "result=" + result);
 		} catch (IOException ex) {
 			ex.printStackTrace();
 		}
 		if(result!=null&&!result.isEmpty()){
 			items = result.split("\n");
 		}
 		int i = 0;
 		String [] item2;
 		for(String item:items){
 			i++;
 			//Log.d("yacleanerdebug",Integer.toString(i)+item);
 			item2=item.trim().split("\\s+");
 			if(item2.length>6){
				Log.d("yacleanerdebug",item2[4]);
 			}
 		}
 		
 		//get Memory info
 		//activityManager.getMemoryInfo(mi);
 		
 		//add info to processInfoList
 		for (Iterator<RunningAppProcessInfo> iterator = runningAppProcesses.iterator(); iterator
 				.hasNext();) {
 			RunningAppProcessInfo procInfo = iterator.next();
 			HashMap<String, String> map = new HashMap<String, String>();
 			
 			map.put("package_name", procInfo.processName);
 			map.put("proc_id", procInfo.pid + "");
 			
 			//get app_name through pm with process name;
 			try {
 				ai = pm.getApplicationInfo(procInfo.processName, 0);
 			} catch (final NameNotFoundException e) {
 				ai = null;
 			}
 			final String applicationName = (String) (ai != null ? pm
 					.getApplicationLabel(ai) : procInfo.processName);
 			map.put("app_name", applicationName);
 			//end
 			processInfoList.add(map);
 		}
 		return runningAppProcesses.size();
 	}
 	
 	
 }
