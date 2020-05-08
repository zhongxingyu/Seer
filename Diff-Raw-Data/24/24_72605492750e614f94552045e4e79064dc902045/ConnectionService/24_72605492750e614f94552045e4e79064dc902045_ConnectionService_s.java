 package com.lastcrusade.fanclub.service;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.Service;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothSocket;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Binder;
 import android.os.IBinder;
 import android.util.Log;
 
 import com.lastcrusade.fanclub.R;
 import com.lastcrusade.fanclub.net.AcceptThread;
 import com.lastcrusade.fanclub.net.BluetoothDiscoveryHandler;
 import com.lastcrusade.fanclub.net.BluetoothNotEnabledException;
 import com.lastcrusade.fanclub.net.BluetoothNotSupportedException;
 import com.lastcrusade.fanclub.net.ConnectThread;
 import com.lastcrusade.fanclub.net.MessageThread;
 import com.lastcrusade.fanclub.net.MessageThreadMessageDispatch;
 import com.lastcrusade.fanclub.net.message.FindNewFansMessage;
 import com.lastcrusade.fanclub.net.message.FoundFan;
 import com.lastcrusade.fanclub.net.message.FoundFansMessage;
 import com.lastcrusade.fanclub.net.message.IMessage;
 import com.lastcrusade.fanclub.service.MessagingService.MessagingServiceBinder;
 import com.lastcrusade.fanclub.util.BluetoothUtils;
 import com.lastcrusade.fanclub.util.BroadcastRegistrar;
 import com.lastcrusade.fanclub.util.IBroadcastActionHandler;
 import com.lastcrusade.fanclub.util.Toaster;
 
 public class ConnectionService extends Service {
 
     private static final String TAG = ConnectionService.class.getName();
 
     /**
      * Action to indicate find fans action has finished.  This action is sent in response to local UI initiated
      * find.
      * 
      * This uses EXTRA_DEVICES to report the devices found.
      * 
      */
     public static final String ACTION_FIND_FINISHED        = ConnectionService.class.getName() + ".action.FindFinished";
     /**
      * Action to indicate find fans action has finished.  This action is sent in response to a remote FindNewFans message.
      * 
      * This uses EXTRA_DEVICES to report the devices found.
      * 
      */
     public static final String ACTION_REMOTE_FIND_FINISHED = ConnectionService.class.getName() + ".action.RemoteFindFinished";
     public static final String EXTRA_DEVICES               = ConnectionService.class.getName() + ".extra.Devices";
 
 
     /**
      * Class for clients to access.  Because we know this service always
      * runs in the same process as its clients, we don't need to deal with
      * IPC.
      */
     public class ConnectionServiceBinder extends Binder implements ILocalBinder<ConnectionService> {
         public ConnectionService getService() {
             return ConnectionService.this;
         }
     }
 
     private BluetoothAdapter adapter;
     private List<ConnectThread> pendingConnections = new ArrayList<ConnectThread>();
     private List<MessageThread> fans     = new ArrayList<MessageThread>();
     private MessageThreadMessageDispatch messageDispatch;
     private ServiceLocator<MessagingService> messagingServiceLocator;
     private BluetoothDiscoveryHandler bluetoothDiscoveryHandler;
 
     private MessageThread discoveryInitiator;
 
     private BroadcastRegistrar broadcastRegistrar;
 
     private MessageThread host;
 
     @Override
     public void onCreate() {
         super.onCreate();
         
         this.adapter = BluetoothAdapter.getDefaultAdapter();
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
         
         registerReceivers();
         
         this.bluetoothDiscoveryHandler = new BluetoothDiscoveryHandler(this, adapter);
         this.messagingServiceLocator = new ServiceLocator<MessagingService>(
                 this, MessagingService.class, MessagingServiceBinder.class);
         
         this.messageDispatch = new MessageThreadMessageDispatch() {
             
             @Override
             public void handleMessage(int messageNo, IMessage message,
                     String fromAddr) {
                 if (message instanceof FindNewFansMessage) {
                     
                 } else {
                     try {
                         messagingServiceLocator.getService().receiveMessage(messageNo, message, fromAddr);
                     } catch (ServiceNotBoundException e) {
                         Log.wtf(TAG, e);
                     }
                 }
             }
         };
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         return new ConnectionServiceBinder();
     }
 
     @Override
     public boolean onUnbind(Intent intent) {
         return super.onUnbind(intent);
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
         unregisterReceivers();
         this.messagingServiceLocator.unbind();
     }
 
     private void registerReceivers() {
         this.broadcastRegistrar = new BroadcastRegistrar();
         this.broadcastRegistrar
             .addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED, new IBroadcastActionHandler() {
 
                 @Override
                 public void onReceiveAction(Context context, Intent intent) {
                     bluetoothDiscoveryHandler.onDiscoveryStarted(discoveryInitiator != null);
                 }
             })
            .addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED, new IBroadcastActionHandler() {
 
                 @Override
                 public void onReceiveAction(Context context, Intent intent) {
                     bluetoothDiscoveryHandler.onDiscoveryFinished();
                 }
             })
            .addAction(BluetoothDevice.ACTION_FOUND, new IBroadcastActionHandler() {
 
                 @Override
                 public void onReceiveAction(Context context, Intent intent) {
                     bluetoothDiscoveryHandler.onDiscoveryFound(
                             (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
                 }
             })
            .addAction(ConnectionService.ACTION_REMOTE_FIND_FINISHED, new IBroadcastActionHandler() {
             
                 @Override
                 public void onReceiveAction(Context context, Intent intent) {
                     //remote initiated device discovery...we want to send the list of devices back to the client
                     List<BluetoothDevice> devices = intent.getParcelableArrayListExtra(ConnectionService.EXTRA_DEVICES);
                     List<FoundFan> foundFans = new ArrayList<FoundFan>();
                     for (BluetoothDevice device : devices) {
                         // send the found fans back to the client.
                         foundFans.add(new FoundFan(device.getName(), device.getAddress()));
                     }
                     FoundFansMessage msg = new FoundFansMessage(
                             foundFans);
                     discoveryInitiator.write(msg);
                 }
             })
            .register(this);
     }
 
     private void unregisterReceivers() {
         this.broadcastRegistrar.unregister();
     }
     
     /**
      * Handle the FindNewFansMessage, which enables discovery on this device and
      * reports the results back to the requestor.
      * 
      * @param remoteAddr
      */
     private void handleFindNewFansMessage(final String remoteAddr) {
         Toaster.iToast(this, R.string.finding_new_fans);
 
         //NOTE: we assume that the adapter is nonnull, because the activity will not
         // get past onCreate on a device w/o Bluetooth...and also, because this method is
         // called in response to a network message over Bluetooth
         BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
 
         // look up the message thread that manages the connection to the remote
         // device
         BluetoothDevice remoteDevice = adapter.getRemoteDevice(remoteAddr);
         MessageThread found = null;
         for (MessageThread thread : this.fans) {
             if (thread.isRemoteDevice(remoteDevice)) {
                 found = thread;
                 break;
             }
         }
 
         if (found == null) {
             Log.wtf(TAG, "Unknown remote device: " + remoteAddr);
             return;
         }
 
         this.discoveryInitiator = found;
         findNewFans();
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
         this.fans.add(newMessageThread);
     }
 
     public void findNewFans() {
         Log.w(TAG, "Starting Discovery");
         if (adapter == null) {
             Toaster.iToast(this,
                     "Device may not support bluetooth");
         } else {
             adapter.startDiscovery();
         }
     }
 
     public void broadcastMessageToFans(IMessage msg) {
         if (isFanConnected()) {
             for (MessageThread fan : this.fans) {
                 fan.write(msg);
             }
         } else {
             Toaster.eToast(this, "No Fans connected");
         }
     }
 
     public void sendMessageToHost(IMessage msg) {
         if (isHostConnected()) {
             this.host.write(msg);
         } else {
             Toaster.eToast(this, "Not connected to host");
         }
     }
 
    private boolean isHostConnected() {
         return this.host != null;
     }
 
     public void disconnectAllFans() {
         for (MessageThread thread : this.fans) {
             thread.cancel();
         }
     }
 
     public boolean isFanConnected() {
         return !this.fans.isEmpty();
     }
 
     /**
      * NOTE: must be run on the UI thread.
      * 
      * @param device
      */
     public void connectToFan(BluetoothDevice device) {
         try {
             //one thread per device found...if there are multiple devices,
             // there are multiple threads
             //TODO: asyncTask may not work....if the host discovers 4 devices, it appears to still only use 1 async task thread
             // and if the first 3 of those devices are not fanclub, itll pause for a while attempting to conenct, which will delay
             // connection of the actual fan
             ConnectThread connectThread = new ConnectThread(this, device) {
 
                 @Override
                 protected void onConnected(BluetoothSocket socket) {
                     pendingConnections.remove(this);
                     onConnectedFan(socket);
                 }
                 
             };
             this.pendingConnections.add(connectThread);
             connectThread.execute();
         } catch (IOException e) {
             e.printStackTrace();
             Toaster.iToast(this,
                     "Unable to create ConnectThread to connect to server");
         }
     }
 
     /**
      * NOTE: Must be called from an activity
      * 
      * TODO: this exposes an implementation detail, namely that bluetooth discoverability
      * @param activity
      */
     public void broadcastFan(Activity activity) {
         BluetoothUtils.enableDiscovery(activity);
 
         try {
             if (adapter == null) {
                 Toaster.eToast(this, "Unable to enable bluetooth adapter");
             } else {
                 AcceptThread thread = new AcceptThread(this, adapter) {
 
                     @Override
                     protected void onAccepted(BluetoothSocket socket) {
                         onAcceptedHost(socket);
                     }
                     
                 };
                 thread.execute();
             }
         } catch (IOException e) {
             Log.w(TAG, e.getStackTrace().toString());
         }
     }
     
 
     /**
      * 
      * NOTE: must be run on the UI thread.
      * 
      * @param socket
      */
     protected void onAcceptedHost(BluetoothSocket socket) {
         //disable discovery...we found our host.
         BluetoothUtils.disableDiscovery(this);
 
         //create the message thread for handling this connection
         this.host = new MessageThread(socket, this.messageDispatch);
         this.host.start();
     }
 }
