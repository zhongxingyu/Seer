 package com.main.sharemi;
 
 
 import org.apache.cordova.DroidGap;
 
 
 import com.bump.api.BumpAPIIntents;
 import com.bump.api.IBumpAPI;
 
 
 import android.os.Bundle;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.util.Log;
 
 
 
 public class MainActivity extends DroidGap
 {
 	//CordovaWebView webView;
 
 	public static MainActivity instance;
 	private Intent bumpservice;
 	private BroadcastReceiver br;
 	private ServiceConnection sc;
 	
 	public void initBump(ServiceConnection connection,BroadcastReceiver receiver){
 		br=receiver;
 		sc=connection;
 		bumpservice=new Intent(IBumpAPI.class.getName());
 		bindService(bumpservice,
                 connection, Context.BIND_AUTO_CREATE);
         Log.i("BumpService", "boot");
         IntentFilter filter = new IntentFilter();
         filter.addAction(BumpAPIIntents.CHANNEL_CONFIRMED);
         filter.addAction(BumpAPIIntents.DATA_RECEIVED);
         filter.addAction(BumpAPIIntents.NOT_MATCHED);
         filter.addAction(BumpAPIIntents.MATCHED);
         filter.addAction(BumpAPIIntents.CONNECTED);
         registerReceiver(receiver, filter);
         //stopService(bumpservice);
 	}
 	
 	  
     public void startListening(ServiceConnection connection,BroadcastReceiver receiver){
     	Log.i("BumpService", "start");
     	startService(bumpservice);
     }
     
     public void endListening(ServiceConnection connection,BroadcastReceiver receiver){
     	Log.i("BumpService", "stop");
     	stopService(bumpservice);
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	instance = this;
         super.onCreate(savedInstanceState);
         
         this.setStringProperty("errorUrl", "file:///android_asset/www/index.html");
         //setContentView(R.layout.main);
         
         //webView = (CordovaWebView) findViewById(R.id.uniappWebView);
         //webView.getSettings().setJavaScriptEnabled(true);
        super.loadUrl("http://150.212.42.251/Sharemi/thirdeye.php");
     }
     
     public void onDestory(){
     	super.onDestroy();
     	unbindService(sc);
     	unregisterReceiver(br);
     }    
 }
