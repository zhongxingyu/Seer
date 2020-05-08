 package com.ybcx.ipintu;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.ybcx.ipintu.db.Applycant;
 import com.ybcx.ipintu.db.UserCacheImpl;
 import com.ybcx.ipintu.service.UserService;
 import com.ybcx.ipintu.task.GenericTask;
 import com.ybcx.ipintu.task.RetrieveUserTask;
 import com.ybcx.ipintu.task.SendTask;
 import com.ybcx.ipintu.task.TaskAdapter;
 import com.ybcx.ipintu.task.TaskListener;
 import com.ybcx.ipintu.task.TaskParams;
 import com.ybcx.ipintu.task.TaskResult;
 import com.ybcx.ipintu.util.FileHelper;
 import com.ybcx.ipintu.util.PreferenceConst;
 
 public class IptUserListenerActivity extends Activity {
 	
 	private static final String TAG = "IptUserListenerActivity";
 	
 	private UserCacheImpl cache;
 
 	private ListView user_lv;
 	private IptUserAdapter user_adptr;
 
 	private Applycant currentUser;
 
 	// Task
 	private GenericTask mSendTask;
 	
 	// 本地设置存储
 	private SharedPreferences mPref; 
 	
 	 private GenericTask mRetrieveTask;
 	
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		// 请求系统添加进度条到标题栏
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		// 设置视图
 		setContentView(R.layout.main);
 		
 		mPref = this.getSharedPreferences(PreferenceConst.IPTCFG, 0);
 		cache = new UserCacheImpl(this.getApplicationContext());
 
 		user_lv = (ListView) findViewById(R.id.userlist);
 		user_adptr = new IptUserAdapter(this.getApplicationContext());
 		user_lv.setAdapter(user_adptr);
 		//靠，啥时候忘了加点击事件了。。。。
 		//2012/03/08
 		user_lv.setOnItemClickListener(listener);
 				
 		
 		//FIXME, just for debug...
 //		setPreferenceValue(2);
 //		UserService.schedule(getApplicationContext());
 	}	
 	
 	public void onResume() {
 		super.onResume();
 		List<Applycant> users = cache.getNewApplycants();
 		user_adptr.refresh(users);
 		//FIXME, 检查日志，分析日期，如果修改时间超过10分钟表示服务没有在运行
 		//自动重启服务，并提示服务已经重启，如果10分钟之内提示服务正在运行
 		//2012/04/02
 		detectServiceStatus();		
 	}
 	
 	private void detectServiceStatus(){
 		try {
 			File logdir = FileHelper.getBasePath();
 			File logFile = new File(logdir, UserService.LOGFILENAME);
 			
 			if(!logFile.exists()){
 				//创建文件
 				logFile.createNewFile();
 				String now = getNowString();
 				String logFilePath = logFile.getAbsolutePath();
 				//写内容
 				FileHelper.writeFileSdcard(logFilePath, ">>>file created at "+now);
 				//并重启服务
 				UserService.schedule(getApplicationContext());
 				updateProgress("service rescheduled!");
 			}else{//通过文件最后修改时间来检查服务状态
				long diffTime = new Date().getTime()-logFile.lastModified();
 				long tenMinutes = 10*60*1000;
 				if(diffTime>tenMinutes){
 					//并重启服务
 					UserService.schedule(getApplicationContext());
 					updateProgress("service rescheduled!");
 				}else{
 					updateProgress("service already running!");
 				}
 			}						
 			
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}	
 
 	private OnItemClickListener listener = new OnItemClickListener() {
 		@Override
 		public void onItemClick(AdapterView<?> parent, View view, int position,
 				long id) {
 			currentUser = (Applycant) user_adptr.getItem(position);
 			processUser();
 		}
 	};
 
 	private void processUser() {
 		if (currentUser == null)
 			return;
 
 		// 任务执行时，不许弹出对话框
 		if (mSendTask != null
 				&& mSendTask.getStatus() == GenericTask.Status.RUNNING)
 			return;
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(
 				IptUserListenerActivity.this);
 		builder.setTitle(getText(R.string.examine_user));
 		builder.setMessage(getText(R.string.current_applycants)
 				+ currentUser.account);
 		builder.setPositiveButton(getText(R.string.accept_applycants),
 				okListener);
 		builder.setNegativeButton(getText(R.string.decline_applycants),
 				cancelListener);
 
 		Dialog dialog = builder.create();
 		dialog.show();
 	}
 
 	private final DialogInterface.OnClickListener okListener = new DialogInterface.OnClickListener() {
 		@Override
 		public void onClick(DialogInterface dialog, int which) {
 			sendAccept(true);
 		}
 	};
 
 	private final DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
 		@Override
 		public void onClick(DialogInterface dialog, int which) {
 			sendAccept(false);
 			dialog.dismiss();
 		}
 	};
 
 	private void sendAccept(boolean isAccept) {
 		if (mSendTask != null
 				&& mSendTask.getStatus() == GenericTask.Status.RUNNING)
 			return;
 
 		mSendTask = new SendTask();
 		mSendTask.setListener(mSendTaskListener);
 		TaskParams params = new TaskParams();
 		params.put("account", currentUser.account);
 		if (isAccept) {
 			params.put("mode", SendTask.TYPE_ACCEPT);
 		} else {
 			params.put("mode", SendTask.TYPE_REFUSE);
 		}
 		mSendTask.execute(params);
 	}
 
 	private TaskListener mSendTaskListener = new TaskAdapter() {
 		@Override
 		public void onPreExecute(GenericTask task) {
 			setProgressBarIndeterminateVisibility(true);// 执行前使进度条可见
 		}
 
 		@Override
 		public void onPostExecute(GenericTask task, TaskResult result) {
 			setProgressBarIndeterminateVisibility(false); // 执行完后使进度条隐藏
 			if(result==TaskResult.FAILED || result==TaskResult.IO_ERROR){
 				updateProgress("remote operation failed!");
 			}
 		}
 
 		public void deliverResponseString(String response) {
 			deleteAcceptedUser();
 		}
 
 	};	
 
 	private void deleteAcceptedUser() {
 		
 		if(currentUser!=null){
 			cache.deleteAcceptApplycant(currentUser.id);
 			List<Applycant> users = cache.getNewApplycants();
 			user_adptr.refresh(users);			
 		}
 	}
 	
 	private void updateProgress(String message) {
 		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
 	}
     private static String getNowString(){
     	DateFormat df = new SimpleDateFormat("MM-dd HH:mm");
     	return df.format(new Date());
     }
 
 	//TODO,  ----------------- option menu definition --------------------------------
 	
 	protected static final int OPTIONS_MENU_ID_CLEAR = 1;
 	protected static final int OPTIONS_MENU_ID_PREFERENCES = 2;
 	protected static final int OPTIONS_MENU_ID_ABOUT = 3;
 	protected static final int OPTIONS_MENU_ID_LOG = 4;
 	protected static final int OPTIONS_MENU_ID_CHECK = 5;
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		MenuItem item;
 		//立即查询，2012/04/02
 		item = menu.add(0, OPTIONS_MENU_ID_CHECK, 0, R.string.omenu_check);
 		item.setIcon(android.R.drawable.ic_menu_search);
 		
 		//查看日志, 2012/03/21
 		item = menu.add(0, OPTIONS_MENU_ID_LOG, 0, R.string.omenu_viewlog);
 		item.setIcon(android.R.drawable.ic_menu_recent_history);
 		
 		//清除缓存
 		item = menu.add(0, OPTIONS_MENU_ID_CLEAR, 0, R.string.omenu_clear);
 		item.setIcon(android.R.drawable.ic_menu_delete);
 		
 		//轮询设置
 		item = menu.add(0, OPTIONS_MENU_ID_PREFERENCES, 0,R.string.omenu_settings);
 		item.setIcon(android.R.drawable.ic_menu_preferences);
 		
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		switch (item.getItemId()) {
 		case OPTIONS_MENU_ID_PREFERENCES:
 			createRadioGroupPopup();
 			return true;
 		case 	OPTIONS_MENU_ID_CLEAR:
 			clearAllRecordInCache();
 			return true;
 			
 		case 	OPTIONS_MENU_ID_LOG:
 			showLogResult();
 			return true;
 			
 		case OPTIONS_MENU_ID_CHECK:
 			requestNewUsers();
 			return true;
 			
 		}
 
 		return super.onOptionsItemSelected(item);
 	}
 	
 
 	
 	   private void requestNewUsers() {
 	    	
 	    	if (mRetrieveTask != null
 	                && mRetrieveTask.getStatus() == GenericTask.Status.RUNNING) {
 	            return;
 	        } else {
 	        	
 	        	DateFormat df = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
 	        	Log.d(TAG, "TO Retrieve new applycants at: "+df.format(new Date()));
 	            
 	        	//执行前先检查网络状况
 	        	if(!UserService.isNetworkAvailable(this.getApplicationContext())){
 	        		UserService.writeLogFileToSDCard("network inavailable, do not fetch data!");
 	        		return;
 	        	}
 	        	
 	            mRetrieveTask = new RetrieveUserTask();                        
 	            mRetrieveTask.setListener(mRetrieveTaskListener);
 	            mRetrieveTask.execute(new TaskParams());
 	            
 	            UserService.writeLogFileToSDCard("to Retrieve new applycant...");
 	        }
 	    	
 	    }
 	   
 	    private TaskListener mRetrieveTaskListener = new TaskAdapter() {
 			@Override
 			public void onPreExecute(GenericTask task) {
 				setProgressBarIndeterminateVisibility(true);// 执行前使进度条可见
 			}
 	    	@Override
 	        public void onPostExecute(GenericTask task, TaskResult result) { 
 	    		setProgressBarIndeterminateVisibility(false); // 执行完后使进度条隐藏
 	    		
 	        	if(result == TaskResult.OK){
 	        		Log.d(TAG, "applycants retrieved successfully...");
 	        	}else if(result == TaskResult.FAILED){
 	        		Log.e(TAG, "applycants retrieve failed!");
 	        		UserService.writeLogFileToSDCard("applycants retrieve failed!");
 	        	}else if (result == TaskResult.IO_ERROR) {
 	        		Log.e(TAG, "network is ungeilivable!");
 	        		UserService.writeLogFileToSDCard("network is ungeilivable!");
 	        	} else if (result == TaskResult.JSON_PARSE_ERROR) {
 	        		Log.e(TAG, "json data parse error!");
 	        	}
 	           
 	        }
 	        @Override
 	        public void deliverRetrievedList(List<Object> applycants){
 	        	Log.i(TAG, "New applycants numer: "+applycants.size());
 	        	UserService.writeLogFileToSDCard("New applycants numer: "+applycants.size());
 	        		        	
 	        	int num = applycants.size();
 	        	if(num>0){
 	        		cacheApplycants(applycants);
 	        		List<Applycant> users = cache.getNewApplycants();
 	        		user_adptr.refresh(users);
 	        	}
 	        }
 	       
 	    };
 	    
 	    private void cacheApplycants(List<Object> applycants){
 	    	ArrayList<Applycant> users = new ArrayList<Applycant>();
 	    	for(Object user : applycants){
 	    		users.add((Applycant) user);
 	    	}
 	    	cache.cacheNewApplycants(users);
 	    }
 	
 	private void showLogResult(){
 		Intent it = new Intent();
 		it.setClass(this, ViewLogFile.class);
 		this.startActivity(it);
 	}
 	
 	//FIXME, CLEAR ALL RECORD FOR CONVEVIENCE...
 	//2012/02/25
 	private void clearAllRecordInCache(){
 		cache.deleteAllRecord();
 		user_adptr.clear();
 		currentUser = null;
 	}
 	
 	private void createRadioGroupPopup(){
 		String tenMin = getText(R.string.ten_mins).toString();
 		String fiveMin = getText(R.string.five_mins).toString();
 		String twoMin = getText(R.string.two_mins).toString();
 		
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle(R.string.refresh_settings);
 		builder.setIcon(android.R.drawable.ic_dialog_info);
 			
 		builder.setSingleChoiceItems(
 				new String[] { tenMin, fiveMin, twoMin}, 
 				prefValueToPosition(),
 			    settingIntervalListener);
 		builder.setNegativeButton(getText(R.string.cancel), null).show();
 	}
 	
 	private final DialogInterface.OnClickListener settingIntervalListener = new DialogInterface.OnClickListener() {
 		@Override
 		public void onClick(DialogInterface dialog, int which) {
 			dialog.dismiss();
 			saveRefreshInteval(which);
 		}
 	};
 	
 	private void saveRefreshInteval(int position){
 		switch(position){
 		case 0:
 			setPreferenceValue(10);
 			break;
 			
 		case 1:
 			setPreferenceValue(5);
 			break;
 			
 		case 2:
 			setPreferenceValue(2);
 			break;
 			
 		}
 	}
 	
 	private void setPreferenceValue(int interval){
 		mPref.edit().putInt(PreferenceConst.INTERVAL, interval).commit();
 	}
 	
 	private int prefValueToPosition(){
 		int position = 0;
 		 //默认10分钟间隔
         int interval = mPref.getInt(PreferenceConst.INTERVAL, 10);
         
         switch(interval){
         case 10:
         	position = 0;
         	break;
         case 5:
         	position = 1;
         	break;
         case 2:
         	position = 2;
         	break;
         }
         
         return position;
 	}
 	
 
 	@Override
 	protected void onDestroy() {
 		Log.d(TAG, "onDestroy.");
 
 		if (mSendTask != null
 				&& mSendTask.getStatus() == GenericTask.Status.RUNNING) {
 			// Doesn't really cancel execution (we let it continue running).
 			// See the SendTask code for more details.
 			mSendTask.cancel(true);
 		}
 
 		mSendTask = null;
 
 		super.onDestroy();
 	}
 
 }
