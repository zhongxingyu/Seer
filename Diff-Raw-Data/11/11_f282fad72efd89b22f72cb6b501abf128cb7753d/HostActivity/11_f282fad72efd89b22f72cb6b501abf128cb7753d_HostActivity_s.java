 package com.lastcrusade.fanclub;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothSocket;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 import com.lastcrusade.fanclub.components.IOnDialogItemClickListener;
 import com.lastcrusade.fanclub.components.MultiSelectListDialog;
 import com.lastcrusade.fanclub.net.BluetoothDeviceDialogFormatter;
 import com.lastcrusade.fanclub.net.BluetoothDiscoveryHandler;
 import com.lastcrusade.fanclub.net.BluetoothNotEnabledException;
 import com.lastcrusade.fanclub.net.BluetoothNotSupportedException;
 import com.lastcrusade.fanclub.net.ConnectThread;
 import com.lastcrusade.fanclub.net.MessageThread;
 import com.lastcrusade.fanclub.net.MessageThreadMessageDispatch;
 import com.lastcrusade.fanclub.net.MessageThreadMessageDispatch.IMessageHandler;
 import com.lastcrusade.fanclub.net.message.ConnectFansMessage;
 import com.lastcrusade.fanclub.net.message.FindNewFansMessage;
 import com.lastcrusade.fanclub.net.message.FoundFan;
 import com.lastcrusade.fanclub.net.message.FoundFansMessage;
 import com.lastcrusade.fanclub.net.message.StringMessage;
 import com.lastcrusade.fanclub.util.BluetoothUtils;
 import com.lastcrusade.fanclub.util.Toaster;
 
 /**
  * 
  * 
  * @author Jesse Rosalia
  *
  */
 public class HostActivity extends Activity {
 
     private ConnectThread connectThread;
     private List<MessageThread> messageThreads = new ArrayList<MessageThread>();
 
     private StringBuilder textBuffer = new StringBuilder();
     private Object msgMutex = new Object();
 
     private final String TAG = "Bluetooth_Host";
     private BluetoothDiscoveryHandler bluetoothDiscoveryHandler;
     private MessageThreadMessageDispatch messageDispatch;
     private BroadcastReceiver broadcastReceiver;
     private MessageThread discoveryInitiator;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_host);
         Log.w(TAG, "Create Called");
 
         // TODO consider moving this entire block into BluetoothUtils, because it's also used in FanActivity
         final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
         try {
             BluetoothUtils.checkAndEnableBluetooth(this, adapter);
         } catch (BluetoothNotEnabledException e) {
             Toaster.iToast(this, "Unable to enable bluetooth adapter");
             e.printStackTrace();
             return;
         } catch (BluetoothNotSupportedException e) {
             Toaster.eToast(this, "Device may not support bluetooth");
             e.printStackTrace();
         }
         registerReceivers(adapter);
         registerMessageHandlers();
 
         Button button = (Button) this.findViewById(R.id.button0);
         button.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 ((Button) findViewById(R.id.button0)).setEnabled(false);
                 Log.w(TAG, "Starting Discovery");
                 if (adapter == null) {
                     Toaster.iToast(HostActivity.this,
                             "Device may not support bluetooth");
                 } else {
                     adapter.startDiscovery();
                 }
             }
         });
 
         button = (Button) this.findViewById(R.id.button1);
         button.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 onHelloButtonClicked();
             }
         });
                 
     }
 
     private void registerMessageHandlers() {
         this.messageDispatch = new MessageThreadMessageDispatch();
         this.messageDispatch.registerHandler(StringMessage.class, new IMessageHandler<StringMessage>() {
 
             @Override
             public void handleMessage(int messageNo,
                     StringMessage message, String fromAddr) {
                 StringMessage sm = (StringMessage) message;
                 synchronized (msgMutex) {
                     if (textBuffer.length() > 0) {
                         textBuffer.append("\n");
                     } else {
                         startDelayedDisplayMessage();
                     }
                     textBuffer.append(sm.getString());
                 }
             }
             
         });
         this.messageDispatch.registerHandler(FindNewFansMessage.class, new IMessageHandler<FindNewFansMessage>() {
 
             @Override
             public void handleMessage(int messageNo,
                     FindNewFansMessage message, String fromAddr) {
                 handleFindNewFansMessage(fromAddr);
             }
             
         });
         this.messageDispatch.registerHandler(ConnectFansMessage.class, new IMessageHandler<ConnectFansMessage>() {
 
             @Override
             public void handleMessage(int messageNo,
                     ConnectFansMessage message, String fromAddr) {
                 for (String address : ((ConnectFansMessage) message).getAddresses()) {
                     BluetoothDevice device = BluetoothAdapter.getDefaultAdapter()
                             .getRemoteDevice(address);
                     // call the same method as if it were discovered and picked from
                     // the host device
                     onDiscoveredFan(device);
                 }
             }
         });
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         disconnectAllSockets();
         unregisterReceivers();
     }
 
     /**
      * Disconnect all connected sockets.
      * 
      */
     private void disconnectAllSockets() {
         for (MessageThread thread : this.messageThreads) {
             thread.cancel();
         }
     }
 
     private void registerReceivers(final BluetoothAdapter adapter) {
         this.bluetoothDiscoveryHandler = new BluetoothDiscoveryHandler(this, adapter);
 
         IntentFilter filter = new IntentFilter();
         filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
         filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
         filter.addAction(BluetoothDevice.ACTION_FOUND);
         filter.addAction(BluetoothDiscoveryHandler.ACTION_DISCOVERED_DEVICES);
         this.broadcastReceiver = new BroadcastReceiver() {
 
             @Override
             public void onReceive(Context context, Intent intent) {
                 if (intent.getAction().equals(
                         BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
                     bluetoothDiscoveryHandler.onDiscoveryStarted();
                 } else if (intent.getAction().equals(
                         BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                     ((Button) findViewById(R.id.button0)).setEnabled(true);
                     bluetoothDiscoveryHandler.onDiscoveryFinished();
                 } else if (intent.getAction().equals(
                         BluetoothDevice.ACTION_FOUND)) {
                     BluetoothDevice device = (BluetoothDevice)intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                     bluetoothDiscoveryHandler.onDiscoveryFound(device);
                 } else if (intent.getAction().equals(
                         BluetoothDiscoveryHandler.ACTION_DISCOVERED_DEVICES)) {
                     onDiscoveredDevices(intent);
                 }
             }
         };
         
         registerReceiver(this.broadcastReceiver, filter);
     }
 
     private void unregisterReceivers() {
         this.unregisterReceiver(this.broadcastReceiver);
     }
     protected void onHelloButtonClicked() {
         //initial test message
         Toaster.iToast(this, "Sending test message");
         String message = "Hello, Fans.  From: " + BluetoothAdapter.getDefaultAdapter().getName();
         if(this.messageThreads.isEmpty()){
             Toaster.eToast(this, "No Fans connected");
         } else {
             StringMessage sm = new StringMessage();
             sm.setString(message);
             //send to all connected fans
            broadcastMessage(sm);
         }
     }
 
     /**
     * Send a message to all connected clients
      * 
     * @param sm
      */
    private void broadcastMessage(StringMessage sm) {
         for (MessageThread thread : this.messageThreads) {
             thread.write(sm);
         }
     }
 
     private void onDiscoveredDevices(Intent intent) {
         List<BluetoothDevice> devices = intent.getParcelableArrayListExtra(BluetoothDiscoveryHandler.EXTRA_DEVICES);
         //if the discovery was initiated by a remote cient, send the discovered devices back to the remote client
         if (discoveryInitiator != null) {
 
             List<FoundFan> foundFans = new ArrayList<FoundFan>();
             for (BluetoothDevice device : devices) {
                 // send the found fans back to the client.
                 foundFans.add(new FoundFan(device.getName(), device.getAddress()));
             }
             FoundFansMessage msg = new FoundFansMessage(foundFans);
             this.discoveryInitiator.write(msg);
 
         } else {
             if (devices.isEmpty()) {
                 Toaster.iToast(this, R.string.no_fans_found);
             } else {
                 Toaster.iToast(this, R.string.found_fans);
                 new MultiSelectListDialog<BluetoothDevice>(this, R.string.select_fans, R.string.connect)
                     .setItems(devices)
                     .setOnClickListener(new IOnDialogItemClickListener<BluetoothDevice>() {
 
                         @Override
                         public void onItemClick(BluetoothDevice device) {
                             //the user selected a device...connect to it.
                             onDiscoveredFan(device);
                         }
                     })
                     .setFormatter(new BluetoothDeviceDialogFormatter())
                     .show();
             }
         }
         this.discoveryInitiator = null;
         
     }
 
     /**
      * NOTE: must be run on the UI thread.
      * @param device
      */
     protected void onDiscoveredFan(BluetoothDevice device) {
         try {
             //one thread per device found...if there are multiple devices,
             // there are multiple threads
             //TODO: asyncTask may not work....if the host discovers 4 devices, it appears to still only use 1 async task thread
             // and if the first 3 of those devices are not fanclub, itll pause for a while attempting to conenct, which will delay
             // connection of the actual fan
             this.connectThread = new ConnectThread(this, device) {
 
                 @Override
                 protected void onConnected(BluetoothSocket socket) {
                     onConnectedFan(socket);
                 }
                 
             };
             this.connectThread.execute();
         } catch (IOException e) {
             e.printStackTrace();
             Toaster.iToast(this,
                     "Unable to create ConnectThread to connect to server");
         }
     }
 
     /**
      * 
      * NOTE: must be run on the UI thread.
      * 
      * @param socket
      */
     protected void onConnectedFan(BluetoothSocket socket) {
         Log.w(TAG, "Connected to server");
 
         //create the message thread, which will be responsible for reading and writing messages
         MessageThread newMessageThread = new MessageThread(socket, this.messageDispatch);
         newMessageThread.start();
         this.messageThreads.add(newMessageThread);
     }
 
     /**
      * Handle the FindNewFansMessage, which enables discovery on this device and
      * reports the results back to the requestor.
      * 
      * @param remoteAddr
      */
     private void handleFindNewFansMessage(final String remoteAddr) {
         Toaster.iToast(this, R.string.finding_new_fans);
 
         BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
         if (adapter == null) {
             Toaster.eToast(this, "Bluetooth adapter is null");
             return;
         }
 
         // look up the message thread that manages the connection to the remote
         // device
         BluetoothDevice remoteDevice = adapter.getRemoteDevice(remoteAddr);
         MessageThread found = null;
         for (MessageThread thread : this.messageThreads) {
             if (thread.isRemoteDevice(remoteDevice)) {
                 found = thread;
                 break;
             }
         }
 
         if (found == null) {
             Toaster.eToast(this, "Unknown remote device.");
             return;
         }
 
         this.discoveryInitiator = found;
         adapter.startDiscovery();
     }
 
     private void startDelayedDisplayMessage() {
         int delayMillis = 2000; /* 2s */
         new Handler().postDelayed(new Runnable() {
 
             @Override
             public void run() {
                 synchronized (msgMutex) {
                     Toaster.iToast(HostActivity.this, textBuffer.toString());
                     textBuffer = new StringBuilder();
                 }
             }
 
         }, delayMillis);
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_host, menu);
         return true;
     }
 }
