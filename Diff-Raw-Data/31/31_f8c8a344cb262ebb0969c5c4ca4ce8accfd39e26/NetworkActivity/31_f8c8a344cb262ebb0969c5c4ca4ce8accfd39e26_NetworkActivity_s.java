 package com.fleurey.android.androidremoteclient.activities;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.RemoteException;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import com.fleurey.android.androidremoteclient.R;
 import com.fleurey.android.androidremoteclient.service.NetworkHandler;
 import com.fleurey.android.androidremoteclient.service.NetworkService;
 import com.fleurey.android.androidremoteclient.service.NetworkServiceConnection;
 
 /**
  * @author Fabien Fleurey
  * @version 1.0 5/23/13
  */
 public class NetworkActivity extends Activity implements NetworkServiceConnection.NetworkServiceListener {
 
     private static final String TAG = NetworkActivity.class.getSimpleName();
     private static final String ADDRESS = "192.168.0.10";
     private static final int PORT = 5566;
 
     private NetworkServiceConnection networkServiceConnection;
     private Messenger networkMessenger;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         networkServiceConnection = new NetworkServiceConnection(this);
     }
 
     @Override
     protected void onStart() {
         super.onStart();
        bindService(new Intent(getApplicationContext(), NetworkService.class), networkServiceConnection, BIND_AUTO_CREATE);
     }
 
     @Override
     protected void onStop() {
         super.onStop();
        if (networkServiceConnection.isBound()) {
            unbindService(networkServiceConnection);
            networkServiceConnection.setBound(false);
        }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if (item.getItemId() == R.id.menu_quit) {
             disconnect();
         }
         return true;
     }
 
     @Override
     public void onServiceBound(Messenger networkMessenger) {
         this.networkMessenger = networkMessenger;
     }
 
     protected boolean connect() {
         if (!networkServiceConnection.isBound()) {
             Log.e(TAG, "Can't connect, network service is not bound");
             return false;
         }
         try {
             Message message = Message.obtain(null, NetworkHandler.MSG_CONNECT);
             Bundle data = new Bundle();
             data.putString(NetworkHandler.EXTRA_ADDRESS, ADDRESS);
             data.putInt(NetworkHandler.EXTRA_PORT, PORT);
             message.setData(data);
             networkMessenger.send(message);
             return true;
         } catch (RemoteException e) {
             Log.e(TAG, "connect error", e);
             return false;
         }
     }
 
     protected boolean sendEvent(int keyCode) {
         if (!networkServiceConnection.isBound()) {
             Log.e(TAG, "Can't send event, network service is not bound");
             return false;
         }
         try {
             networkMessenger.send(Message.obtain(null, NetworkHandler.MSG_EVENT, keyCode, 0));
             return true;
         } catch (RemoteException e) {
             Log.e(TAG, "send event error", e);
             return false;
         }
     }
 
     protected boolean disconnect() {
         if (!networkServiceConnection.isBound()) {
             Log.e(TAG, "Can't disconnect, network service is not bound");
             return false;
         }
         try {
           networkMessenger.send(Message.obtain(null, NetworkHandler.MSG_DISCONNECT));
             return true;
         } catch (RemoteException e) {
             Log.e(TAG, "disconnect error", e);
             return false;
         }
     }
 }
