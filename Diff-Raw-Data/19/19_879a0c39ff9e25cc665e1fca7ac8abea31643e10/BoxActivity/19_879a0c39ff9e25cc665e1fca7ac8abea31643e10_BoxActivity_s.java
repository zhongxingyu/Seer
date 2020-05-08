 package com.geniatech.client_box;
 
 import java.util.List;
 
 import android.net.NetworkInfo;
 import android.net.wifi.SupplicantState;
 import android.net.wifi.WifiConfiguration;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 
 public class BoxActivity extends Activity {
 
 	private Button mButton;
 	private TextView mTextView;
 	private IntentFilter mIntentFilter;
 	private WifiManager mWifiManager;
     private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             String action = intent.getAction();
             if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                 handleWifiStateChanged(intent.getIntExtra(
                         WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN));
             } else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
                 handleStateChanged(WifiInfo.getDetailedStateOf((SupplicantState)
                         intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE)));
             } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                 handleStateChanged(((NetworkInfo) intent.getParcelableExtra(
                         WifiManager.EXTRA_NETWORK_INFO)).getDetailedState());
             }else if(WifiManager.WIFI_AP_STATE_CHANGED_ACTION.equals(action)){
             	handleWifiApStateChanged(intent.getIntExtra(
                         WifiManager.EXTRA_WIFI_AP_STATE, WifiManager.WIFI_AP_STATE_FAILED));
             }
         }
     };
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_box);
 		mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
 		new WifiUtils(mWifiManager);
 		
 		mTextView = (TextView)findViewById(R.id.textView1);
 		
 		mIntentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
         mIntentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
         mIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
         mIntentFilter.addAction(WifiManager.WIFI_AP_STATE_CHANGED_ACTION);
         
 		//startService(new Intent(this,BoxService.class));
 		
 		mButton = (Button)findViewById(R.id.button1);
 		mButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				mButton.setText(WifiUtils.getIP());
 				Log.i("asdfadf","===============removeConfigs======3===-----");
 				getwifi();
 			}
 		});
 	}
 
 	public void getwifi(){
 		List<WifiConfiguration> confList = mWifiManager.getConfiguredNetworks();
 		Log.i("asdfadf","===============removeConfigs======3===-----"+confList.size());
 		for(WifiConfiguration conf:confList){
 			Log.i("asdfadf","===============removeConfigs======3===-----"+conf.status);
 			if(conf.SSID.contains("ChinaNet-GTL")){
 				//mTextView.setText(conf.status);
 			}
 		}
 	}
 	@Override
 	protected void onResume() {
 		super.onResume();
 		registerReceiver(mReceiver, mIntentFilter);
 	}
 	@Override
 	protected void onPause() {
 		super.onPause();
 		unregisterReceiver(mReceiver);
 	}
 	@Override
 	protected void onDestroy() {
 		// TODO Auto-generated method stub
 		super.onDestroy();
 		//stopService(new Intent(this,BoxService.class));
 	}
 	private void handleWifiStateChanged(int state) {
         switch (state) {
             case WifiManager.WIFI_STATE_ENABLING:
                 mTextView.setText(R.string.wifi_starting);
                 break;
             case WifiManager.WIFI_STATE_ENABLED:
                 mTextView.setText(null);
                 break;
             case WifiManager.WIFI_STATE_DISABLING:
             	mTextView.setText(R.string.wifi_stopping);
                 break;
             case WifiManager.WIFI_STATE_DISABLED:
             	mTextView.setText("stoped");
                 break;
             default:
             	mTextView.setText(R.string.wifi_error);
         }
     }
 
     private void handleStateChanged(NetworkInfo.DetailedState state) {
         if (state != null) {
             WifiInfo info = mWifiManager.getConnectionInfo();
             if (info != null) {
            	mTextView.setText(Summary.get(this, info.getSSID(), state));
             }
         }
     }
     private void handleWifiApStateChanged(int state) {
         switch (state) {
             case WifiManager.WIFI_AP_STATE_ENABLING:
             	mTextView.setText(R.string.wifi_starting);
                 break;
             case WifiManager.WIFI_AP_STATE_ENABLED:
             	mTextView.setText("AP enabled:" + WifiUtils.getActiveSSID());
                 break;
             case WifiManager.WIFI_AP_STATE_DISABLING:
             	mTextView.setText(R.string.wifi_stopping);
                 break;
             case WifiManager.WIFI_AP_STATE_DISABLED:
             	mTextView.setText("AP disabled.");
                 break;
             default:
             	mTextView.setText(R.string.wifi_error);
         }
     }
 }
