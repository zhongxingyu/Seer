 package com.logtracking.lib.internal;
 
 import java.util.ArrayList;
 import java.util.List;
 import com.logtracking.lib.api.Log;
 import com.logtracking.lib.api.LogContext;
 import com.logtracking.lib.api.settings.LogSettings;
 
 import android.app.ActivityManager;
 import android.app.ActivityManager.RunningAppProcessInfo;
 import android.content.Context;
 import android.text.TextUtils;
 import android.util.SparseArray;
 
 public class LogFilter {
 
 	private static final SparseArray<Character> CODE_LEVEL_ARRAY = new SparseArray<Character>();
 	
 	static {
		CODE_LEVEL_ARRAY.put(Log.ASSERT ,  'A');
		CODE_LEVEL_ARRAY.put(Log.DEBUG  ,  'D');
		CODE_LEVEL_ARRAY.put(Log.ERROR  ,  'E');
		CODE_LEVEL_ARRAY.put(Log.INFO   ,  'I');
		CODE_LEVEL_ARRAY.put(Log.VERBOSE,  'V');
		CODE_LEVEL_ARRAY.put(Log.WARN   ,  'W');
 	}
 	
 	private static final LogFilter instance = new LogFilter();
 	
 	public static LogFilter getInstance(){
 		return instance;
 	}
 	
 	public static char getLevelSymbolByCode(int code){
 		return CODE_LEVEL_ARRAY.get(code);
 	}
 	
 	private SparseArray<String> mPidPackageNameCollector;
 	private List<Integer> mPidFilter;
 	private Integer mLevelFilter;
 	private boolean mOnlyOwnLogRecord;
 	private Context mApplicationContext;
 	private LogSettings mSettings;
 	
 	protected boolean filterAvailable(){
 		return (mPidFilter !=null && mPidFilter.size()>0) ||
 			   (mLevelFilter !=null);
 	}
 	
 	protected boolean passFilterRecord(LogModel record){
 		
 		return ( ( (mPidFilter == null || mPidFilter.size() == 0) || (mPidFilter.contains(record.getPid())) )
 				 &&
 				 ((!mOnlyOwnLogRecord) || (record.getTag().startsWith(Log.LIBRARY_FILTER_TAG) ))
 				 );
 	}
 
 	
 	public void initFilter(LogContext logContext){
 		mApplicationContext = logContext.getApplicationContext();
 		mSettings = logContext.getLogSettings();
 		mPidFilter = packageNameFilterToPIDFilter(mSettings.getApplicationPackageFilter());
 		mLevelFilter = mSettings.getLevelFilter();
 		mOnlyOwnLogRecord = mSettings.isSaveOnlyOwnRecord();
 	}
 	
 	public String packageNameByPid(int pid){
 		String packageName = mPidPackageNameCollector.get(pid);
 		if (TextUtils.isEmpty(packageName)){
 			refreshPackageFilter();
 		}
 		return mPidPackageNameCollector.get(pid);
 	}
 	
 	public int getPidByPackageName(String packageName){
 		int pid = mPidPackageNameCollector.indexOfValue(packageName);
 		if (pid<0){
 			refreshPackageFilter();
 		}
 		return mPidPackageNameCollector.indexOfValue(packageName);
 	}
 	
 	private List<Integer> packageNameFilterToPIDFilter(List<String> packageNameFilter){
 		if (packageNameFilter == null)
 			return null;
 		
 		mPidPackageNameCollector = new SparseArray<String>();
 		List<String> packageNameFilterCopy = new ArrayList<String>(packageNameFilter);
 		List<Integer> pidFilter = new ArrayList<Integer>();
 		ActivityManager manager  = (ActivityManager) mApplicationContext.getSystemService(Context.ACTIVITY_SERVICE);
 
 		for(RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()){
 			mPidPackageNameCollector.put(processInfo.pid, processInfo.processName);
 			for (String packageName : packageNameFilterCopy) {
 				 if(processInfo.processName.equals(packageName))
 					 pidFilter.add(processInfo.pid);
 				 	 packageNameFilterCopy.remove(packageName);
 				 	 break;
 				 }
 		}
 		return pidFilter;
 	}
 	
 	private void refreshPackageFilter(){
 		mPidFilter = packageNameFilterToPIDFilter(mSettings.getApplicationPackageFilter());
 	}
 }
