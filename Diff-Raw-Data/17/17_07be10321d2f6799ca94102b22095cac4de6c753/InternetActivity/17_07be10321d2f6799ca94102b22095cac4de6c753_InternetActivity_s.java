 package com.example.demo;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
 import android.os.Build;
 import android.os.Bundle;
 import android.widget.Toast;
 
 public class InternetActivity extends Activity {
 
     private ConnectivityListener reciever;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         this.reciever = new ConnectivityListener();
 
         // Show the Up button in the action bar.
         setupActionBar();
     }
 
     @Override
     protected void onStart() {
         super.onStart();
 
         IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
         filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
         registerReceiver(this.reciever, filter);
     }
 
     @Override
     protected void onStop() {
         super.onStop();
         unregisterReceiver(reciever);
     }
 
     @TargetApi(Build.VERSION_CODES.HONEYCOMB)
     private void setupActionBar() {
         getActionBar().setDisplayHomeAsUpEnabled(true);
     }
 
     public class ConnectivityListener extends BroadcastReceiver {
 
         @Override
         public void onReceive(Context context, Intent intent) {
            boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            String reason = intent.getStringExtra(ConnectivityManager.EXTRA_REASON);

            if (noConnectivity) {
                Toast.makeText(context, "Not Connected: " + reason, Toast.LENGTH_LONG).show();
            } else {
                 Toast.makeText(context, "Connected", Toast.LENGTH_LONG).show();
             }
         }
 
     }
 }
