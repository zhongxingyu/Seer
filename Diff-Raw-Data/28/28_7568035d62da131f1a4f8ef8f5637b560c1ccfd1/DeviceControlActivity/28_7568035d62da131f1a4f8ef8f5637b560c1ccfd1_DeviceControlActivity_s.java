 package com.anusiewicz.MCForAndroid.views;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import com.anusiewicz.MCForAndroid.R;
 import com.anusiewicz.MCForAndroid.TCP.TCPClient;
 import com.anusiewicz.MCForAndroid.controllers.ConnectionManager;
 import com.anusiewicz.MCForAndroid.controllers.DeviceFactory;
 import com.anusiewicz.MCForAndroid.model.Constants;
 import com.anusiewicz.MCForAndroid.model.MCDeviceCode;
 import com.anusiewicz.MCForAndroid.model.MCRequest;
 import com.anusiewicz.MCForAndroid.model.MCResponse;
 
 import java.util.HashMap;
 import java.util.concurrent.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Szymon Anusiewicz
  */
 public class DeviceControlActivity extends ActivityWithMenu implements ConnectionManager.ConnectionListener, TCPClient.TcpMessageListener {
 
     private TCPClient mTCPClient;
     private HashMap <String, MCResponse> receivedData = new HashMap<String, MCResponse>();
     ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
     private ConnectionInfoText infoText;
     private LinearLayout controlsLayout;
     private boolean isConnected, isOnTop;
 
     private final static int REFRESH_TIME = 5;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.device_control_layout);
         infoText = (ConnectionInfoText) findViewById(R.id.infoText);
         controlsLayout = (LinearLayout) findViewById(R.id.devicesLayout);
         Button bAddControl = (Button) findViewById(R.id.buttonAdd);
         bAddControl.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 DeviceFactory.startDevicePicker(DeviceControlActivity.this);
             }
         });
         connectionManager.registerListener(this);
         executor.scheduleAtFixedRate(new UpdateItems(),0,REFRESH_TIME, TimeUnit.SECONDS);
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         isOnTop = false;
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         isOnTop = true;
         if (connectionManager.getConnectionStatus() == ConnectionManager.ConnectionStatus.OK) {
             isConnected = true;
             mTCPClient = connectionManager.getTCPClient();
         }
         if (executor.isTerminated() || executor.isShutdown()) {
             executor.scheduleAtFixedRate(new UpdateItems(),0,REFRESH_TIME, TimeUnit.SECONDS);
         }
        isOnTop = true;
     }
 
     private class UpdateItems implements Runnable {
 
         @Override
         public void run() {
            if (isConnected && isOnTop) {
                 for ( int i = 0 ; i < controlsLayout.getChildCount(); i++ ) {
                     final DeviceItem item = (DeviceItem) controlsLayout.getChildAt(i);
                     DeviceControlActivity.this.runOnUiThread(new Runnable() {
                         @Override
                         public void run() {
                         mTCPClient.enqueueRequest(MCRequest.generateStringFromRequest(item.getRequest()));
                         }
                     });
                 }
            }
 
         }
     }
 
     @Override
     public void onConnectionEstablished() {
         mTCPClient = connectionManager.getTCPClient();
         mTCPClient.registerListener(this);
         this.runOnUiThread(new Runnable() {
             public void run() {
                 infoText.update();
             }
         });
         isConnected = true;
     }
 
     @Override
     public void onConnectionLost() {
         this.runOnUiThread(new Runnable() {
             public void run() {
                 infoText.update();
             }
         });
         isConnected = false;
     }
 
     @Override
     public void onConnectionFailed() {
         this.runOnUiThread(new Runnable() {
             public void run() {
                 infoText.update();
             }
         });
         isConnected = false;
     }
 
     @Override
     public void onConnecting() {
         this.runOnUiThread(new Runnable() {
             public void run() {
                 infoText.update();
             }
         });
         isConnected = false;
     }
 
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         if (requestCode == 1) {
 
             if(resultCode == RESULT_OK){
                 final String name = data.getStringExtra(Constants.DEVICE_NAME_TAG);
                 final MCDeviceCode code = (MCDeviceCode) data.getSerializableExtra(Constants.DEVICE_TYPE_TAG);
                 final Integer number = data.getIntExtra(Constants.DEVICE_NUMBER_TAG, 0);
 
                 controlsLayout.addView(DeviceFactory.createMCDeviceItem(this,code,number,name));
             }
         }
     }
 
     @Override
     public void onReceive(String request,String response) {
 
         MCResponse mcResponse = MCResponse.parseMCResponseFromString(response);
         if (request != null && mcResponse != null) {
         receivedData.put(request,mcResponse);
         }
 
         updateControls();
 
     }
 
     private void updateControls() {

         if (isOnTop) {
 
             for ( int i = 0 ; i < controlsLayout.getChildCount(); i++ ) {
                 final DeviceItem item = (DeviceItem) controlsLayout.getChildAt(i);
                 DeviceControlActivity.this.runOnUiThread(new Runnable() {
                     @Override
                     public void run() {
                        item.updateViewFromData(receivedData);
                     }
                 });
             }
         }
     }
 }
