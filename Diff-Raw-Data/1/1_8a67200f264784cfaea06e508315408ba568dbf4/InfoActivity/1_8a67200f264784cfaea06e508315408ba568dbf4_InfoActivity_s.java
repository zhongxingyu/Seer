 package com.lfcinvention.RemoteVision;
 
 import android.app.Activity;
 import android.app.Service;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.text.InputFilter.LengthFilter;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class InfoActivity extends Activity {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         
         
         mBtnStart = (Button)findViewById(R.id.btnStart);
         mTextView = (TextView)findViewById(R.id.boundAddressText);
         mBtnStart.setOnClickListener(onBtnStartClicked);
     }
     
     private VideoService.ServiceChannel mBoundService = null;
     private boolean mIsBound = false; 
     private TextView mTextView = null;
     private Button  mBtnStart = null;
     
     private ServiceConnection mServiceConnection = new ServiceConnection() {
 		public void onServiceDisconnected(ComponentName name) {
 			InfoActivity.this.mBoundService = null;
 			InfoActivity.this.mIsBound = false;
 		}
 		
 		public void onServiceConnected(ComponentName name, IBinder service) {
 			VideoService.ServiceChannel s = (VideoService.ServiceChannel)service;
 			
 			if (s.getServiceState() == VideoService.State.STATE_ERROR) {
 				mTextView.setText("错误：" + s.getErrorString());
 				mBtnStart.setEnabled(false);
 				return;
 			}
 			
 			InfoActivity.this.mBoundService = s;
 			InfoActivity.this.mIsBound = true;
 		}
 	};
 	
 	private void bindService() {
 		try{
 			Intent i = new Intent();
 			i.setClass(getApplicationContext(), VideoService.class);
 			if (bindService(i, mServiceConnection,Context.BIND_AUTO_CREATE)) {
 				mIsBound = true;
 			}else{
 				Toast.makeText(this, "Bind service failed", 2).show();
 			}
 		}catch(Exception e) {
 			Toast.makeText(InfoActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
 		}
 	}
     
     
     private View.OnClickListener onBtnStartClicked = new View.OnClickListener() {
 		public void onClick(View arg0) {
 			try{
 				if (!mIsBound) {
 					bindService();
 				}
 				mBoundService.startService();
 			}catch(Exception e) {
 				Toast.makeText(InfoActivity.this, e.getMessage(), Toast.LENGTH_LONG);
 			}
 		}
 	};
 	static {
 		System.loadLibrary("RemoteVision");
		System.loadLibrary("RemoteVisionJni");
 	}
 }
