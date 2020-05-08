 package com.htb.cnk;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.Toast;
 
 import com.htb.cnk.data.Info;
 import com.htb.cnk.data.TableSetting;
 import com.htb.cnk.data.UserData;
 
 public class Cnk_orderPadActivity extends Activity {
     /** Called when the activity is first created. */
 	private ImageButton mMenuBtn;
 	private ImageButton mOrderBtn;
 	private ImageButton mSettingsBtn;
 	private ProgressDialog mpDialog;
 	private final static int UPDATE_MENU = 0;
 	private final static int LATEST_MENU = 1;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         findViews();
         setClickListeners();
         Info.setNewCustomer(true);
         syncWithServer();
     }
     
     private void findViews(){
     	mMenuBtn = (ImageButton) findViewById(R.id.menu);
     	mOrderBtn = (ImageButton) findViewById(R.id.order);
     	mSettingsBtn = (ImageButton) findViewById(R.id.settings);
     }
     
     private void setClickListeners() {
     	mMenuBtn.setOnClickListener(menuClicked);
     	mOrderBtn.setOnClickListener(orderClicked);
     	mSettingsBtn.setOnClickListener(settingsClicked);
     }
     
     private int getCurrentMenuVer() {
 		SharedPreferences sharedPre = getSharedPreferences("menuDB", 
 				Context.MODE_WORLD_WRITEABLE | Context.MODE_WORLD_READABLE);
 		return sharedPre.getInt("ver", 0);
 	}
     
     private void syncWithServer() {
     	mpDialog = new ProgressDialog(Cnk_orderPadActivity.this);  
         mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
         mpDialog.setTitle("请稍等");
         mpDialog.setMessage("正在与服务器同步...");  
         mpDialog.setIndeterminate(false);
         mpDialog.setCancelable(false);
         mpDialog.show();
         new Thread() {
 			public void run() {
 				int menuVer = getCurrentMenuVer();
 				if (UpdateMenuActivity.isUpdateNeed(menuVer)) {
 					handlerSync.sendEmptyMessage(UPDATE_MENU);
 				} else {
 					handlerSync.sendEmptyMessage(LATEST_MENU);
 				}
 			}
         }.start();
     }
     
     private OnClickListener menuClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View arg0) {
 			Intent intent = new Intent();
 			intent.setClass(Cnk_orderPadActivity.this, MenuActivity.class);
 			Info.setMode(Info.WORK_MODE_CUSTOMER);
 			startActivity(intent);
 		}
     	
     };
     
     private OnClickListener orderClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			Intent intent = new Intent();
 			intent.setClass(Cnk_orderPadActivity.this, QueryOrderActivity.class);
 			startActivity(intent);
 		}
    
     };
     
     private OnClickListener settingsClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			
 			// 点击确定转向登录对话框
 			LayoutInflater factory = LayoutInflater.from(Cnk_orderPadActivity.this);
 			// 得到自定义对话框
 			final View DialogView = factory.inflate(R.layout.setting_dialog, null);
 			// 创建对话框
 			AlertDialog dlg = new AlertDialog.Builder(Cnk_orderPadActivity.this)
 					.setTitle("登录框").setView(DialogView)// 设置自定义对话框样式
 					.setPositiveButton("确定", new DialogInterface.OnClickListener() {// 设置监听事件
 
 								@Override
 								public void onClick(DialogInterface dialog,
 										int which) {
 									EditText mUserName = (EditText)DialogView.findViewById(R.id.edit_username);
 									final String userName = mUserName.getText().toString();
 									EditText mUserPwd = (EditText)DialogView.findViewById(R.id.edit_password);
 									final String userPwd = mUserPwd.getText().toString();
 									UserData.setUserName(userName);
 									UserData.setUserPwd(userPwd);
 									Toast.makeText(getApplicationContext(),
 											userName+userPwd,
 											Toast.LENGTH_SHORT).show();
 									new Thread(new userThread()).start();
 								}
 							}).setNegativeButton("取消",// 设置取消按钮
 							new DialogInterface.OnClickListener() {
 
 								@Override
 								public void onClick(DialogInterface dialog,
 										int which) {
 									// 点击取消后退出程序
 								}
 							}).create();// 创建对话框
			if(Info.getMode() == Info.WORK_MODE_WAITER){
				Message msg = new Message();
				msg.what = 1;
				userHandle.sendMessage(msg);
			}else{
 				dlg.show();// 显示对话框
 
 			}
 		}
     	
     };
     
     class userThread implements Runnable {
 		public void run() {
 			try {
 				Message msg = new Message();						
 				int ret = UserData.ComparePwd();
 				if(ret < 0){
 					userHandle.sendEmptyMessage(ret);
 					return;
 				}
 				msg.what = ret;
 				userHandle.sendMessage(msg);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
     
     private Handler userHandle = new Handler() {
 		public void handleMessage(Message msg) {
 			if (msg.what < 0) {
 				Toast.makeText(getApplicationContext(),
 						getResources().getString(R.string.delWarning),
 						Toast.LENGTH_SHORT).show();
 			} else {
 				Intent intent = new Intent();
 	    		intent.setClass(Cnk_orderPadActivity.this, TableActivity.class);
 	    		Cnk_orderPadActivity.this.startActivity(intent);
 			}
 		}
     };
     
     private Handler handlerSync = new Handler() {
 		public void handleMessage(Message msg) {
 			if (msg.what == UPDATE_MENU) {
 				Intent intent = new Intent();
 				intent.setClass(Cnk_orderPadActivity.this, UpdateMenuActivity.class);
 				startActivity(intent);
 			}
 			mpDialog.cancel();
 		}
     };
     
 }
