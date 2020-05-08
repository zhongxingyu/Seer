 package halp.com;
 
 import java.io.IOException;
 import java.util.List;
 
import com.example.android.BluetoothChat.BluetoothChatService;
 
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.wifi.ScanResult;
 import android.net.wifi.WifiConfiguration;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.app.Activity;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothServerSocket;
 import android.bluetooth.BluetoothSocket;
 import android.content.Context;
 import android.util.Log;
 import android.view.Menu;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 
 	//use for wifi
 	/*ConnectivityManager connectivity;
 	NetworkInfo wifiInfo, mobileInfo;
 	WifiManager wifiManager;*/
 	TextView fuckLogCat;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         fuckLogCat = (TextView) findViewById(R.id.tvLog);
         
         // Get local Bluetooth adapter
         BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
 
         // If the adapter is null, then Bluetooth is not supported
         if (adapter == null) {
             Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
             finish();
             return;
         }
         
         adapter.enable();
         adapter.setName("halp");
         adapter.startDiscovery();
         
         
         /*
         wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
         
      // Setup Connectivity
         connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
         wifiInfo = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
         mobileInfo = connectivity.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
 		wifiManager.setWifiEnabled(true);
 
         wifiManager.startScan();
 
         
         List<ScanResult> results = wifiManager.getScanResults();
         for(ScanResult result: results){
 
         	if (result.SSID.equals("halp")){
         		fuckLogCat.setText("\n" + result.BSSID);
         		fuckLogCat.setText("\n" + fuckLogCat.getText() + "\n" + result);
 
         		WifiConfiguration wc = new WifiConfiguration();
         		// This is must be quoted according to the documentation 
         		// http://developer.android.com/reference/android/net/wifi/WifiConfiguration.html#SSID
         		wc.SSID = "\""+result.SSID+"\"";
         		//wc.preSharedKey  = "password";
         		//wc.hiddenSSID = true;
         		wc.status = WifiConfiguration.Status.CURRENT;
         		wc.priority = 1;
         		//wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
         		//wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
         		wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
         		//wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
         		//wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
         		//wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
         		int res = wifiManager.addNetwork(wc);
         		Log.d("WifiPreference", "add Network returned " + res );
         		boolean b = wifiManager.enableNetwork(res, false);        
         		Log.d("WifiPreference", "enableNetwork returned " + b );
         	}
         }
         //fuckLogCat.setText("\n\n" + wifiManager.getScanResults());
         
         
         // print info
         //Log.d("MainActivity", "\n\n" + wifiInfo.toString());
         //Log.d("MyActivity","\n\n" + mobileInfo.toString());
         //Log.d("MyActivity", "\n\n" + wifiManager.getScanResults());*/
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
     /**
      * This thread runs while listening for incoming connections. It behaves
      * like a server-side client. It runs until a connection is accepted
      * (or until cancelled).
      */
     private class AcceptThread extends Thread {
         // The local server socket
         private final BluetoothServerSocket mmServerSocket;
         private String mSocketType;
 
         public AcceptThread(boolean secure) {
             BluetoothServerSocket tmp = null;
             mSocketType = secure ? "Secure":"Insecure";
 
             // Create a new listening server socket
             try {
                if (secure) {
                    tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE,
                        MY_UUID_SECURE);
                } else {
                    tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(
                            NAME_INSECURE, MY_UUID_INSECURE);
                }
             } catch (IOException e) {
                 Log.e(TAG, "Socket Type: " + mSocketType + "listen() failed", e);
             }
             mmServerSocket = tmp;
         }
 
         public void run() {
             if (D) Log.d(TAG, "Socket Type: " + mSocketType +
                     "BEGIN mAcceptThread" + this);
             setName("AcceptThread" + mSocketType);
 
             BluetoothSocket socket = null;
 
             // Listen to the server socket if we're not connected
             while (true) {
                 try {
                     // This is a blocking call and will only return on a
                     // successful connection or an exception
                     socket = mmServerSocket.accept();
                 } catch (IOException e) {
                     Log.e(TAG, "Socket Type: " + mSocketType + "accept() failed", e);
                     break;
                 }
 
                 // If a connection was accepted
                 if (socket != null) {
                     synchronized (BluetoothChatService.this) {
                         switch (mState) {
                         case STATE_LISTEN:
                         case STATE_CONNECTING:
                             // Situation normal. Start the connected thread.
                             connected(socket, socket.getRemoteDevice(),
                                     mSocketType);
                             break;
                         case STATE_NONE:
                         case STATE_CONNECTED:
                             // Either not ready or already connected. Terminate new socket.
                             try {
                                 socket.close();
                             } catch (IOException e) {
                                 Log.e(TAG, "Could not close unwanted socket", e);
                             }
                             break;
                         }
                     }
                 }
             }
            Log.i(TAG, "END mAcceptThread, socket Type: " + mSocketType);
 
         }
 
         public void cancel() {
            Log.d(TAG, "Socket Type" + mSocketType + "cancel " + this);
             try {
                 mmServerSocket.close();
             } catch (IOException e) {
                 Log.e(TAG, "Socket Type" + mSocketType + "close() of server failed", e);
             }
         }
     }
 }
