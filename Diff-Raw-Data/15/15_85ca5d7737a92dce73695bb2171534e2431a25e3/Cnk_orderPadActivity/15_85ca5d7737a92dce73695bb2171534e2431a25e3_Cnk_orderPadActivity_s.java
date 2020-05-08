 package com.htb.cnk;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageButton;
 import android.widget.Toast;
 
 import com.htb.cnk.data.Info;
 import com.htb.cnk.data.UserData;
 
 public class Cnk_orderPadActivity extends Activity {
     /** Called when the activity is first created. */
 	private ImageButton mMenuBtn;
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
         Info.setTableId(-1);
         syncWithServer();
     }
     
     private void findViews(){
     	mMenuBtn = (ImageButton) findViewById(R.id.menu);
     	mSettingsBtn = (ImageButton) findViewById(R.id.settings);
     }
     
     private void setClickListeners() {
     	mMenuBtn.setOnClickListener(menuClicked);
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
 			if (Info.getTableId() < 0) {
 				Toast.makeText(getApplicationContext(),
 						getResources().getString(R.string.tableNotSet),
 						Toast.LENGTH_SHORT).show();
 			} else {
 				Intent intent = new Intent();
 				intent.setClass(Cnk_orderPadActivity.this, MenuActivity.class);
 				Info.setMode(Info.WORK_MODE_CUSTOMER);
 				startActivity(intent);
 			}
 		}
     	
     };
     
 //    private OnClickListener orderClicked = new OnClickListener() {
 //
 //		@Override
 //		public void onClick(View v) {
 //			Intent intent = new Intent();
 //			intent.setClass(Cnk_orderPadActivity.this, QueryOrderActivity.class);
 //			startActivity(intent);
 //		}
 //   
 //    };
     
     private OnClickListener settingsClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 				Intent intent = new Intent();
 	    		intent.setClass(Cnk_orderPadActivity.this, TableActivity.class);
 	    		Cnk_orderPadActivity.this.startActivity(intent);
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
 						getResources().getString(R.string.userWarning),
 						Toast.LENGTH_SHORT).show();
 			} else if (msg.what == 1){
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
