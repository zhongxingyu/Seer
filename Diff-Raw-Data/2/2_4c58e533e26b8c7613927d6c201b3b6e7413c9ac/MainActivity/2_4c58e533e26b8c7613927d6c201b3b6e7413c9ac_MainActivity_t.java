 package com.bnrc.authbuptgw;
 
 import android.net.NetworkInfo;
 import android.net.wifi.WifiManager;
 import android.os.Build.VERSION;
 import java.util.Timer;
 import java.util.TimerTask;
 import android.os.Bundle;
 import android.os.StrictMode;
 import android.annotation.SuppressLint;
 import android.os.Handler;
 import android.os.Message;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 
 /**
  * 
  * @author yinqingwang@163.com
  * @param <Intent>
  * 
  */
 public class MainActivity extends Activity {
 
 	static final String PREF_NAME = "AuthBuptGW";
 	static final String USERNAME = "USERNAME";
 	static final String PASSWORD = "PASSWORD";
 	static final String AUTOLOGIN = "AUTOLOGIN";
 
 	static final int LOGIN_NUM = 3;
 	static final int ANDROID2_2 = 8;
 
 	/**
 	 * Bupt认证网关的IP地址(Host地址)
 	 */
 	String strBuptGwHost = "";
 
 	/**
 	 * Android系统版本号 2.2 -- 8
 	 */
 	int sysVersion = 8;
 
 	static final int UPDATE_UI = 0;
 	static final int TASK_DELAY = 1 ; // Seconds;
 	
 	static final int TASK_LOGIN = 1;
 	static final int TASK_WIFI = 2;
 	static final int TASK_UPDATE_UI = 4;
 	static final int LOGIN_RETRY_DELAY = 3 ; // Seconds; 重试登录的延迟时间
 	static final int LOGIN_RETRY_MAX_NUM = 20;
 	static final int LOGIN_RETRY_DEFAULT_NUM = 3;
 
 	private Timer mTimer;
 	private Handler mHandler;
 
 	/**
 	 * 是否开启自动登录功能, true为开启, false为不开启。
 	 */
 	boolean bEnable = false;
 
 	/**
 	 * 网络是否畅通
 	 */
 	boolean bNetOK = false;
 
 	/**
 	 * 是否登录成功
 	 */
 	boolean bLoginOK = false;
 
 	/**
 	 * Wifi 是否可用
 	 */
 	boolean bWifiEnable = false;
 
 	/**
 	 * 
 	 */
 	CheckBox m_chkbx = null;
 	CheckBox m_chkbx_wifi = null;
 	/**
 	 * 
 	 */
 	ToggleButton m_tgbtn = null;
 	ToggleButton m_tgbtn_wifi = null;
 
 	/**
 	 * 
 	 */
 	TextView m_msg = null;
 
 	EditText m_username = null;
 	EditText m_password = null;
 
 	/**
 	 * 
 	 */
 	protected void initTimerAndHandler() {
 
 		mTimer = new Timer();
 
 		mHandler = new MyEventHandler(this);  //事件处理类
 	}
 
 	protected void destroyTimer() {
 		if (mTimer != null) {
 			mTimer.cancel();
 			mTimer = null;
 		}
 
 		mHandler = null;
 	}
 
 	/**
 	 * 初始化函数, 在onCreate()方法中调用
 	 */
 	protected void initEvent() {
 
 		((ToggleButton) this.findViewById(R.id.btn_enable_disable)).setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				onClickEnableDisable(v);
 			}
 
 		});
 
 		((Button) this.findViewById(R.id.btn_about)).setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				onClickAbout(v);
 			}
 
 		});
 		// wifi
 		((ToggleButton) this.findViewById(R.id.btn_wifi_enable_disable)).setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				onClickWifi();
 			}
 
 		});
 		((CheckBox) this.findViewById(R.id.chkbtn_wifi)).setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				onClickWifi();
 			}
 
 		});
 
 		// ((Button) this.findViewById(R.id.btn_quit)).setOnClickListener(new
 		// View.OnClickListener() {
 		//
 		// @Override
 		// public void onClick(View v) {
 		// // TODO Auto-generated method stub
 		// onClickQuit(v);
 		// }
 		//
 		// });
 
 		((CheckBox) this.findViewById(R.id.chkbtn_enable)).setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				onClickCheckEnable(v);
 			}
 
 		});
 		// 用户-输入框
 		((EditText) this.findViewById(R.id.txt_username)).addTextChangedListener(new TextWatcher() {
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before, int count) {
 				onTextChangedUserNamePassword(s);
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
 			}
 
 			@Override
 			public void afterTextChanged(Editable s) {
 			}
 		});
 		// 密码输入框
 		((EditText) this.findViewById(R.id.txt_passwd)).addTextChangedListener(new TextWatcher() {
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before, int count) {
 				onTextChangedUserNamePassword(s);
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
 			}
 
 			@Override
 			public void afterTextChanged(Editable s) {
 			}
 		});
 
 		IntentFilter filter = new IntentFilter();
 		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
 		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
 		this.registerReceiver(new BroadcastReceiver() {
 			@Override
 			public void onReceive(Context context, android.content.Intent intent) {// Wifi状态变化
 				// TODO Auto-generated method stub
 				if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
 					NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
 					if (info.getState().equals(NetworkInfo.State.CONNECTED) || info.getState().equals(NetworkInfo.State.DISCONNECTED)) {// 如果连接可用,
 																																		// 或者连接不可用
 						scheduleTask(TASK_LOGIN);
 					} else {
 						//scheduleTask(TASK_UPDATE_UI);
 					}
 
 				} else if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
 					//scheduleTask(TASK_UPDATE_UI);
 				}
 			}
 		}, filter);
 
 	}
 
 	/**
 	 * 初始化
 	 */
 	protected void init() {
 		m_tgbtn = (ToggleButton) this.findViewById(R.id.btn_enable_disable);
 		m_chkbx = (CheckBox) this.findViewById(R.id.chkbtn_enable);
 		// wifi
 		m_tgbtn_wifi = (ToggleButton) this.findViewById(R.id.btn_wifi_enable_disable);
 		m_chkbx_wifi = (CheckBox) this.findViewById(R.id.chkbtn_wifi);
 
 		m_msg = (TextView) this.findViewById(R.id.login_msg);
 		m_username = (EditText) this.findViewById(R.id.txt_username);
 		m_password = (EditText) this.findViewById(R.id.txt_passwd);
 
 		// 装入数据
 		loadData();
 
 		initEvent(); // 初始化事件处理
 		initTimerAndHandler(); // 初始化定时器
 
 	}
 
 	/**
 	 * 
 	 */
 	@Override
 	protected void onStart() {
 		// TODO Auto-generated method stub
 		super.onStart();
 
 		scheduleTask(TASK_LOGIN);// 后台任务执行
 	}
 
 	/**
 	 * 调度后台任务执行
 	 * @param type
 	 */
 	protected void scheduleTask(int type) {
 		scheduleTask(type, TASK_DELAY); // 后台任务执行
 	}
 	
 	/**
 	 * 延时 delay 秒调度后台任务执行
 	 * @param type - 任务类型
 	 * @param delay - 延迟时间, 单位: 秒
 	 */
 	protected void scheduleTask(int type, int delay) {
 		if( delay <0 ) {
 			delay = TASK_DELAY;
 		}
 		mTimer.schedule(new TimerTaskBackgroud( type ), delay *1000 ); // 后台任务执行
 	}
 	
 	@Override
 	protected void onDestroy() {
 		// TODO Auto-generated method stub
 		super.onDestroy();
 
 		destroyTimer();
 	}
 
 	/**
 	 * 
 	 */
 	@SuppressLint("NewApi")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 
 		sysVersion = VERSION.SDK_INT;
 		if (sysVersion > ANDROID2_2) {
 			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork() // or
 																																	// .detectAll()
 																																	// for
 																																	// all
 																																	// detectable
 																																	// problems
 					.penaltyLog().build());
 			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath()
 					.build());
 		}
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		// 初始化
 		init();
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	/**
 	 * 切换Wifi状态
 	 */
 	protected void onClickWifi() {
 		
 		bWifiEnable = !bWifiEnable;
 		
 		m_tgbtn_wifi.setChecked(bWifiEnable);
 		m_chkbx_wifi.setChecked(bWifiEnable);
 
 		if (bWifiEnable) {
 			m_msg.setText(R.string.msg_wifi_change_on);
 		} else {
 			m_msg.setText(R.string.msg_wifi_change_off);
 		}
 
 		//scheduleTask(TASK_WIFI);
 		AuthUtil.changeWifiState(this, bWifiEnable);   //改变wifi状态
 	}
 
 	/**
 	 * 
 	 * @param v
 	 */
 	protected void onClickEnableDisable(View v) {
 		doLogin();
 	}
 
 	/**
 	 * 
 	 * @param v
 	 */
 	protected void onClickCheckEnable(View v) {
 		doLogin();
 	}
 
 	protected void doLogin() {
 		bEnable = !bEnable; // 切换状态
 		m_tgbtn.setChecked(bEnable);
 		m_chkbx.setChecked(bEnable);
 
 		// 保存应用数据
 		saveData();
 
 		if (bEnable && bWifiEnable) {
 			m_msg.setText(R.string.msg_login);
 		}
 
 		scheduleTask(TASK_LOGIN); // 后台任务执行
 	}
 
 	/**
 	 * 
 	 * @param v
 	 */
 	protected void onClickAbout(View v) {
 		// scheduleTask(); // 后台任务执行
 		String strTitle = this.getString(R.string.title_activity_about);
 		LayoutInflater inflater = getLayoutInflater();
 		View layout = inflater.inflate(R.layout.about_dialog, (ViewGroup) findViewById(R.id.about_dialog));
 		new AlertDialog.Builder(this).setTitle(strTitle).setView(layout).setPositiveButton("确定", null).show();
 	}
 
 	/**
 	 * 
 	 * @param v
 	 */
 	protected void onClickQuit(View v) {
 
 	}
 
 	/**
 	 * 输入的用户名/密码有变化时调用
 	 */
 	protected void onTextChangedUserNamePassword(CharSequence s) {
 		if (bEnable) {// 如果开启了自动登录, 则暂时关闭
 			bEnable = !bEnable;
 
 			m_chkbx.setChecked(bEnable); // 设置状态为不启用
 			m_tgbtn.setChecked(bEnable); // 设置状态为不启用
 		}
 	}
 
 	/**
 	 * 经查网络是否连上internet
 	 * 
 	 * @return
 	 */
 	protected boolean isNetAvailable() {
 
 		String strChkUrl = this.getString(R.string.URL_CHECK_NETWORK);
 		String strChkContent = this.getString(R.string.URL_CHECK_CONTENT);
 
 		return AuthUtil.checkUrl(strChkUrl, strChkContent);
 
 	}
 
 	/**
 	 * 切换Wifi状态
 	 */
 	protected void changeWifi() {
 		AuthUtil.changeWifiState(this, !bWifiEnable);
 	}
 
 	/**
 	 * 执行登录动作, 成功返回true， 失败返回false
 	 * @return
 	 */
 	protected boolean login(){
 		boolean bOK = false;
 		String user = m_username.getText().toString();
 		String passwd = m_password.getText().toString();
 		String strUrl = this.getString(R.string.URL_LOGIN);
 		String strUrlRelogin = this.getString(R.string.URL_RELOGIN);
 		
 		//登录的重试次数
 		int loginRetryNum = Integer.parseInt( this.getString(R.string.LOGIN_RETRY_NUMBER) );
 		if( loginRetryNum <=0 || loginRetryNum >= LOGIN_RETRY_MAX_NUM  ){  //有效性检查, 避免程序死循环!
 			loginRetryNum = LOGIN_RETRY_DEFAULT_NUM;
 		}
 		
 		for( int i=0; i<loginRetryNum ; i++ ){
 			AuthUtil.relogin(strUrlRelogin, user, passwd);
 			AuthUtil.login(strUrl, user, passwd);
 			
 			bOK = isNetAvailable();    //检查网络是否可用
 			if( bOK ) break;  //登录成功则退出。
 			
 			// ... 这里需要延时吗?
 			
 		}
 		return bOK;
 	}
 	
 	/**
 	 * 检查网络是否可用, 并根据情况进行登录
 	 */
 	protected void checkNetworkAndLogin() {
 
 		bWifiEnable = AuthUtil.isWifiEnable(this); // 检查Wifi网络
 
 		if (bWifiEnable && bEnable) { // 检查是否可登录
 			bLoginOK = login();   //进行登录操作
 		}
 
 		bNetOK = isNetAvailable(); // 测试网络连通性
 
 	}
 
 	/**
 	 * 更新界面提示信息
 	 */
 	protected void updateUI() {
 		StringBuffer sb = new StringBuffer();
 
 		// wifi
 		bWifiEnable = AuthUtil.isWifiEnable(this);
 		m_tgbtn_wifi.setChecked(bWifiEnable);
 		m_chkbx_wifi.setChecked(bWifiEnable);
 
 		if (!bWifiEnable) {// wifi状态
 			sb.append(this.getString(R.string.msg_wifi_fail));
 		}
 		if (bNetOK) {
 			sb.append(this.getString(R.string.msg_network_ok));
 		} else if (bWifiEnable) {
 			sb.append(this.getString(R.string.msg_network_fail));
 		}
 
 		if (bWifiEnable && bLoginOK) { // Wifi可用, 联网正常
 			sb.append(this.getString(R.string.msg_login_ok));
 		} else if (bWifiEnable && !bNetOK) { // Wifi可用并且不能联网
 			sb.append(this.getString(R.string.msg_login_fail));
 			
 			//延迟登录操作
			scheduleTask(TASK_LOGIN, LOGIN_RETRY_DELAY); // 后台任务执行
 		}
 
 		m_msg.setText(sb.toString()); // 设置提示信息
 	}
 
 	/**
 	 * 从存储中读取出数据并显示到界面上面
 	 */
 	protected void loadData() {
 		SharedPreferences myPref = getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
 		String userName = myPref.getString(USERNAME, "");
 		String passWord = myPref.getString(PASSWORD, "");
 
 		// bEnable = myPref.getBoolean(AUTOLOGIN, false);
 		// System.out.println("load: bEnable =" + bEnable);
 
 		// 更新界面
 		m_username.setText(userName);
 		m_password.setText(passWord);
 
 		if ((userName.length() == 0) || (passWord.length() == 0)) {
 			bEnable = false;
 		} else {
 			bEnable = true;
 		}
 		m_tgbtn.setChecked(bEnable);
 		m_chkbx.setChecked(bEnable);
 
 		// wifi
 		bWifiEnable = AuthUtil.isWifiEnable(this);
 		m_tgbtn_wifi.setChecked(bWifiEnable);
 		m_chkbx_wifi.setChecked(bWifiEnable);
 	}
 
 	/**
 	 * 存储数据到应用程序中
 	 */
 	protected void saveData() {
 		String userName = m_username.getText().toString();
 		String passWord = m_password.getText().toString();
 
 		SharedPreferences myPref = getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
 		SharedPreferences.Editor editor = myPref.edit();
 		editor.putString(USERNAME, userName);
 		editor.putString(PASSWORD, passWord);
 
 		// editor.putBoolean(AUTOLOGIN, bEnable);
 		// System.out.println("save : bEnable =" + bEnable);
 
 		// editor.apply();
 		editor.commit();
 	}
 
 	/**
 	 * 
 	 * @author wang
 	 *
 	 */
 	static class MyEventHandler extends Handler{
 		
 		MainActivity activity = null;
 		
 		public MyEventHandler(MainActivity a){
 			this.activity = a;
 		}
 		
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case UPDATE_UI:
 				activity.updateUI(); // 更新界面信息
 				break;
 			}
 		}
 	}
 	/**
 	 * Work thread and It doesn't work updating the UI inside a timer.
 	 * 
 	 * @author wang
 	 * 
 	 */
 	class TimerTaskBackgroud extends TimerTask {
 		
 		int type;
 		public TimerTaskBackgroud(int t){
 			this.type = t;
 		}
 		
 		@Override
 		public void run() {
 			// TODO Auto-generated method stub
 
 			if ((type & TASK_LOGIN) != 0) {
 				checkNetworkAndLogin(); // 检查网络, 并根据情况自动登录
 				
 				mHandler.sendEmptyMessage(UPDATE_UI);  // update ui
 				
 			} else if ((type & TASK_WIFI) != 0) {
 				changeWifi();
 				
 			} else if ((type & TASK_UPDATE_UI) != 0) {
 
 			}
 
 			
 		}
 	}
 
 }
