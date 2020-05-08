 package com.devicedump;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.os.Build;
 import android.view.View;
 import android.widget.TextView;
 import android.telephony.TelephonyManager;
 import android.provider.Settings;
 import android.content.Context;
 
 public class DeviceDump extends Activity {
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
     }
 
     public void dumpState(View button) {
         TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
 
         String imei = tManager.getDeviceId();
         TextView imei_tv = (TextView) this.findViewById(R.id.imei_textview);
         imei_tv.setText("IMEI: " + imei);
 
         String mdn = tManager.getLine1Number();
         TextView mdn_tv = (TextView) this.findViewById(R.id.mdn_textview);
         mdn_tv.setText("MDN: " + mdn);
 
         String imsi = tManager.getSubscriberId();
         TextView imsi_tv = (TextView) this.findViewById(R.id.imsi_textview);
         imsi_tv.setText("IMSI: " + imsi);
 
         String manufacturer = Build.MANUFACTURER;
         TextView manufacturer_tv = (TextView) this.findViewById(R.id.manufacturer_textview);
         manufacturer_tv.setText("Manufacturer: " + manufacturer);
 
         String device_name = Build.MODEL;
         String device_code = Build.DEVICE;
         TextView device_tv = (TextView) this.findViewById(R.id.device_textview);
         device_tv.setText("Device: " + device_name + " / " + device_code);
 
         String network_name = tManager.getNetworkOperatorName();
         String network_id = tManager.getNetworkOperator();
         TextView network_name_tv = (TextView) this.findViewById(R.id.network_name_textview);
         network_name_tv.setText("Network: " + network_name + " (" + network_id + ")");
 
         String sdk_version = Build.VERSION.RELEASE;
         TextView sdk_tv = (TextView) this.findViewById(R.id.sdk_textview);
         sdk_tv.setText("Android Version: " + sdk_version);
 
        String apn = Settings.ACTION_APN_SETTINGS;
         TextView apn_tv = (TextView) this.findViewById(R.id.apn_textview);
        apn_tv.setText("APN: " + apn);

     }
 }
