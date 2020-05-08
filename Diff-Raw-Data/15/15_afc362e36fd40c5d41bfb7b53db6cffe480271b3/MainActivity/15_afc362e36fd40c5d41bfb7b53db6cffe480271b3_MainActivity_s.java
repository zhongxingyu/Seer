 package se.magnulund.android.wifilistwidget;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.wifi.ScanResult;
 import android.net.wifi.WifiConfiguration;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.widget.ArrayAdapter;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 public class MainActivity extends Activity {
 
     private static final String TAG = "MainActivity";
 
     private ListView wifiList;
     private List<WifiConfiguration> wifiConfigurations;
     ArrayList<String> listItems;
     ArrayAdapter<String> listAdapter;
     private WifiManager wifiManager;
     private static Boolean WIFI_SCAN_RECEIVER_ACTIVE = false;
 
     //WifiScanReceiver wifiScanReceiver;
     //WifiStateReceiver wifiStateReceiver;
 
     BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             updateWifiList();
         }
     };
 
     BroadcastReceiver wifiStateReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
 
             toggleWifiReceiver(wifiState);
         }
     };
 
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         wifiList = (ListView) findViewById(R.id.wifi_list);
         listItems = new ArrayList<String>();
         listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
         wifiList.setAdapter(listAdapter);
 
 
         wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
 
         wifiConfigurations = wifiManager.getConfiguredNetworks();
     }
 
     @Override
     protected void onResume() {
         super.onResume();    //To change body of overridden methods use File | Settings | File Templates.
 
         //wifiStateReceiver = new WifiStateReceiver();
         registerReceiver(wifiStateReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
 
         if (wifiManager.isWifiEnabled()) {
             //wifiScanReceiver = new WifiScanReceiver();
             registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
             WIFI_SCAN_RECEIVER_ACTIVE = true;
         }
     }
 
     public void updateWifiList ( ) {
         List<ScanResult> scanResults = wifiManager.getScanResults();
         addItem("New scan results!");
         // Kanske ska göra lite skit här......
     }
 
     public void toggleWifiReceiver(int wifiState){
         switch (wifiState) {
             case WifiManager.WIFI_STATE_ENABLED:
                 /*
                 if ( wifiScanReceiver == null) {
                     wifiScanReceiver = new WifiScanReceiver();
                 }
                 */
                 registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                 WIFI_SCAN_RECEIVER_ACTIVE = true;
                addItem("State Receiver active!");
                 break;
            case WifiManager.WIFI_STATE_DISABLED:
                 unregisterReceiver(wifiScanReceiver);
                 WIFI_SCAN_RECEIVER_ACTIVE = false;
                addItem("State Receiver inactive!");
                 break;
             default:
                 break;
         }
     }
 
     private void addItem(String text) {
         Date date = Calendar.getInstance().getTime();
         //
         // Display a date in day, month, year format
         //
         DateFormat formatter = DateFormat.getTimeInstance();
         String now = formatter.format(date);
         listItems.add(now+": "+text);
         listAdapter.notifyDataSetChanged();
     }
 
     @Override
     protected void onPause() {
         super.onDestroy();    //To change body of overridden methods use File | Settings | File Templates.
 
         unregisterReceiver(wifiStateReceiver);
 
         if (WIFI_SCAN_RECEIVER_ACTIVE) {
             unregisterReceiver(wifiScanReceiver);
         }
     }
 }
