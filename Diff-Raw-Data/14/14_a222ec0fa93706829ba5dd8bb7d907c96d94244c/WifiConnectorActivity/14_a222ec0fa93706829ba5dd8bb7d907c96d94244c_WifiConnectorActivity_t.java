 /* 
  * Copyright (C) 2012 Martin Helff
  * 
  * This file is part of WifiConnector.
  * 
  * WifiConnector is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * WifiConnector is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with WifiConnector.  If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 
 package net.helff.wificonnector;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.helff.wificonnector.LocationData.Location;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.net.wifi.ScanResult;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.TextView;
 
 /**
  * @author mhelff
  * 
  */
 public class WifiConnectorActivity extends Activity {
     private static WifiConnectorActivity mInst;
     private boolean autoConnect;
     private String mobileNumber;
     
     protected final String L = "WifiConnectorActivity";
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         Button b = (Button) this.findViewById(R.id.connectButton);
         b.setOnClickListener(new OnClickListener() {
 
             public void onClick(View v) {
                 new LoginTask(mobileNumber).execute(Boolean.FALSE);
             }
 
         });
         Button b1 = (Button) this.findViewById(R.id.disconnectButton);
         b1.setOnClickListener(new OnClickListener() {
 
             public void onClick(View v) {
                 new LogoutTask(mobileNumber).execute(Boolean.FALSE);
             }
 
         });
         
         final ImageButton bPos = (ImageButton) this.findViewById(R.id.buttonPositionRefresh);
        bPos.setOnClickListener(new OnClickListener() {
 
             public void onClick(View v) {
                 // just trigger WiFi-Scanning
                 bPos.setEnabled(!((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE)).startScan());
             }
 
         });
         
         
         
         
         if(autoConnect) {
             // TODO: start some background service to watch WiFi connection
         }
     }
 
     public static WifiConnectorActivity instance() {
         return mInst;
     }
 
     public void setStatus(String status, String detail) {
         Log.i(L, status + "; " + detail);
         TextView largeStatus = (TextView) this.findViewById(R.id.largeStatus);
         largeStatus.setText(status);
         TextView detailStatus = (TextView) this.findViewById(R.id.smallStatus);
         detailStatus.setText(detail);
     }
 
     @Override
     public void onStart() {
         super.onStart();
         // TODO: start location scanner
         getPrefs();
         
         // check wifi
         new LoginTask(mobileNumber).execute(Boolean.TRUE);
         
         // start a wifi scan for position update
         ((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE)).startScan();
        // immediatly show position from old scan, most times thats quite accurate
        updatePositionView();
         
         mInst = this;
     }
 
     @Override
     public void onStop() {
         mInst = null;
         // TODO: stop location scanner
         super.onStop();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         
         menu.add(0, Menu.FIRST, 1, R.string.settings)
         .setShortcut('9', 's')
         .setIcon(android.R.drawable.ic_menu_preferences);
         
         menu.add(0, Menu.FIRST+1, 1, R.string.info)
         .setShortcut('0', 'i')
         .setIcon(android.R.drawable.ic_menu_info_details);
         
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         
         case Menu.FIRST:
             Intent intent = new Intent();
             intent.setClass(this, WifiConnectorPreferences.class);
             startActivity(intent);
             return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
 
     private void getPrefs() {
         // Get the xml/preferences.xml preferences
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
         autoConnect = prefs.getBoolean("autoConnect", false);
         mobileNumber = prefs.getString("mobileNumber", "");
     }
     
     public void triggerConnection() {
         getPrefs();
         
         if(autoConnect) {
             new LoginTask(mobileNumber).execute(Boolean.FALSE);
         }
     }
     
     public void updatePositionView() {
         WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
         
         TextView position = (TextView) this.findViewById(R.id.position);
         String posText = "Unknown position";
         
         List<ScanResult> results = wifiManager.getScanResults();
         ScanResult strongest = null;
         for(ScanResult scanResult : results) {
             if("TelefonicaPublic".equals(scanResult.SSID) || "o2ZJ".equals(scanResult.SSID)) {
                 if(strongest == null || strongest.level < scanResult.level) {
                     strongest = scanResult;
                 }
             }
         }
         
         if(strongest != null) {
             Location location = LocationData.getLocation(strongest.BSSID);
             if(location != null) {
                 posText = location.getBlock() + location.getFloor() + " " + location.getPosition();  
             }
         }
         
         position.setText(posText);
         final ImageButton bPos = (ImageButton) this.findViewById(R.id.buttonPositionRefresh);
         bPos.setEnabled(true);
     }
 
     private class LoginTask extends AsyncTask<Boolean, String, String> {
 
         protected Context applicationContext;
         protected WifiManager wifiManager;
         protected HttpClient httpClient = new DefaultHttpClient();
         protected HttpContext localContext = new BasicHttpContext();
         private LoginToken loginToken = new LoginToken();
         protected String mobileNumber;
 
         public LoginTask(String mNumber) {
             this.mobileNumber = mNumber;
         }
         
         @Override
         protected void onPreExecute() {
             applicationContext = getApplicationContext();
             wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
             Button b = (Button) findViewById(R.id.connectButton);
             b.setEnabled(false);
         }
 
         protected String doInBackground(Boolean... onlyCheck) {
             // check if mobile number is set
             checkMsisdn(mobileNumber);
             if (isCancelled())
                 return null;
             
             // check if WiFi is ours
             checkWifi(wifiManager);
             if (isCancelled())
                 return null;
 
             // check if network is already unlocked
             checkConnectivity(httpClient, true);
            if (onlyCheck[0].booleanValue() || isCancelled())
                 return null;
 
             // post mobile-number to login page
             submitMSISDN(httpClient, mobileNumber);
             if (isCancelled())
                 return null;
 
             // wait for SMS to arrive and parse token
             waitForSMS();
             if (isCancelled())
                 return null;
 
             // post token to confirmation page
             submitToken(httpClient, loginToken);
             if (isCancelled())
                 return null;
 
             // check connectivity to web page
             checkConnectivity(httpClient, false);
             return loginToken.getToken();
         }
 
         protected void onProgressUpdate(String... progress) {
             setStatus(progress[0], progress[1]);
         }
 
         protected void onPostExecute(String result) {
             Button b = (Button) findViewById(R.id.connectButton);
             b.setEnabled(true);
         }
 
         @Override
         protected void onCancelled() {
             Button b = (Button) findViewById(R.id.connectButton);
             b.setEnabled(true);
         }
         
         protected void checkMsisdn(String msisdn) {
             if (msisdn == null || msisdn.trim().length() == 0) {
                 // post error
                 publishProgress("Check app settings", "Please enter your mobile phone number first");
                 cancel(false);
             }
         }
 
         protected void checkWifi(WifiManager wifiManager) {
             WifiInfo wifiInfo = wifiManager.getConnectionInfo();
             if (wifiInfo == null || !"TelefonicaPublic".equals(wifiInfo.getSSID())) {
                 // post error
                 publishProgress("Not connected", "Not connected to TelefonicaPublic WiFi");
                 cancel(false);
             } else {
                 publishProgress("TelefonicaPublic locked", "Press connect to unlock internet access");
             }
         }
 
         protected void checkConnectivity(HttpClient httpClient, boolean beforeUnlock) {
             final String mainStatus = beforeUnlock ? "Checking connectivity" : "Unlocking WiFi";
             try {
                 if(beforeUnlock) {
                     publishProgress(mainStatus, "Check if internet access is already enabled");
                 } else {
                     publishProgress(mainStatus, "Checking internet connectivity");
                 }
                 HttpGet httpGet = new HttpGet("http://www.helff.net");
                 HttpResponse response = httpClient.execute(httpGet, localContext);
                 String result = "";
 
                 BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                 String line = null;
                 while ((line = reader.readLine()) != null) {
                     result += line + "\n";
                 }
                 if (result.contains("<title>helff.net</title>")) {
                     if (beforeUnlock) {
                         publishProgress("WiFi ready", "Internet access is unlocked, start surfing!");
                         cancel(false);
                     } else {
                         publishProgress("WiFi ready", "Internet access is unlocked, start surfing!");
                     }
                 } else {
                     if (!beforeUnlock) {
                         publishProgress(mainStatus, "Network is not unlocked, something failed!");
                    } else {
                    	publishProgress("TelefonicaPublic locked", "Press connect to unlock internet access");
                     }
                 }
                 reader.close();
                 response.getEntity().consumeContent();
             } catch (ClientProtocolException e) {
                 publishProgress(mainStatus, "Error checking network status, please retry");
                 Log.e(WifiConnectorActivity.class.getName(), "Network check failed1", e);
                 cancel(false);
             } catch (IOException e) {
                 publishProgress(mainStatus, "Error checking network status, please retry");
                 Log.e(WifiConnectorActivity.class.getName(), "Network check failed2", e);
                 cancel(false);
             }
         }
 
         protected void submitMSISDN(HttpClient httpClient, String msisdn) {
             try {
                 // post mobile-number to login page
                 publishProgress("Unlocking WiFi", "Submitting mobile phone number " + mobileNumber);
                 HttpPost httpPost = new HttpPost("http://wlan.de.telefonica:8001/login.php?l=de");
                 List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                 nameValuePairs.add(new BasicNameValuePair("handynr", msisdn));
                 nameValuePairs.add(new BasicNameValuePair("login", "Token per SMS zusenden &gt;"));
                 httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                 // Execute HTTP Post Request
                 HttpResponse response = httpClient.execute(httpPost, localContext);
                 response.getEntity().consumeContent();
                publishProgress("Unlocking WiFi", "Submitted MSISDN: " + msisdn);
             } catch (ClientProtocolException e) {
                 publishProgress("Unlocking WiFi", "Could not submit mobile phone number, please retry");
                 cancel(false);
             } catch (IOException e) {
                 publishProgress("Unlocking WiFi", "Could not submit mobile phone number, please retry");
                 cancel(false);
             }
         }
 
         protected void waitForSMS() {
 
             // set up broadcast receiver
             publishProgress("Unlocking WiFi", "Waiting for login token");
             SMSReceiver receiver = new SMSReceiver(loginToken);
             IntentFilter intentFilter = new IntentFilter(SMSReceiver.ACTION);
             intentFilter.setPriority(100);
             applicationContext.registerReceiver(receiver, intentFilter);
 
             int iterations = 1;
             // loop for 10 seconds and wait for SMS arriving
             while (iterations < 20 && !this.isCancelled()) {
                 if (loginToken.isTokenSet()) {
                     break;
                 }
 
                 // sleep a second
                 try {
                     Thread.sleep(500);
                 } catch (InterruptedException e) {
                     // just finish then
                     break;
                 }
 
                 iterations++;
             }
 
             // remove broadcast receiver
             applicationContext.unregisterReceiver(receiver);
             
             if(loginToken.isTokenSet()) {
                 publishProgress("Unlocking WiFi", "Received token: " + loginToken.getToken());
             } else {
                 publishProgress("Unlocking WiFi", "Token did not arrive within 10 seconds, please retry!");
                 cancel(false);
             }
         }
 
         protected void submitToken(HttpClient httpClient, LoginToken token) {
             try {
                 // post mobile-number to login page
                 publishProgress("Unlocking WiFi", "Submitting token: " + loginToken.getToken());
                 HttpPost httpPost = new HttpPost("http://wlan.de.telefonica:8001/token.php?l=de");
                 List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                 nameValuePairs.add(new BasicNameValuePair("token", token.getToken()));
                 nameValuePairs.add(new BasicNameValuePair("submit", "Lossurfen &gt;"));
                 httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                 // Execute HTTP Post Request
                 httpClient.execute(httpPost, localContext);
                 HttpResponse response = httpClient.execute(httpPost, localContext);
                 response.getEntity().consumeContent();
                 publishProgress("Unlocking WiFi", "Submitted token: " + token.getToken());
             } catch (ClientProtocolException e) {
                 publishProgress("Unlocking WiFi", "Could not submit token, please retry");
                 cancel(false);
             } catch (IOException e) {
                 publishProgress("Unlocking WiFi", "Could not submit token, please retry");
                 cancel(false);
             }
         }
     }
 
     private class LogoutTask extends LoginTask {
 
         public LogoutTask(String mNumber) {
             super(mNumber);
         }
         
         @Override
         protected void onPreExecute() {
             applicationContext = getApplicationContext();
             wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
             Button b = (Button) findViewById(R.id.disconnectButton);
             b.setEnabled(false);
         }
 
         protected String doInBackground(Boolean... onlyCheck) {
             // check if WiFi is ours
             checkWifi(wifiManager);
             if (isCancelled())
                 return null;
 
             // check connectivity to google or other page
             logout(httpClient, "");
             return "Disconnected";
         }
 
         protected void onPostExecute(String result) {
             Button b = (Button) findViewById(R.id.disconnectButton);
             b.setEnabled(true);
         }
 
         @Override
         protected void onCancelled() {
             // remove BroadcastReceiver if left over
         }
 
         protected void logout(HttpClient httpClient, String msisdn) {
             try {
                 publishProgress("Logging off", "Logging off Telefonica WiFi internet access");
                 // post mobile-number to login page
                 HttpPost httpPost = new HttpPost("http://wlan.de.telefonica:8001/index.php?l=de");
                 List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                 nameValuePairs.add(new BasicNameValuePair("exit", "Ja, diese Sitzung jetzt beenden &gt;"));
                 httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                 // Execute HTTP Post Request
                 HttpResponse response = httpClient.execute(httpPost, localContext);
                 response.getEntity().consumeContent();
                 publishProgress("TelefonicaPublic locked", "Press connect to unlock internet access");
             } catch (ClientProtocolException e) {
                 publishProgress("Logging off", "Could not log off, please retry");
                 cancel(false);
             } catch (IOException e) {
                 publishProgress("Logging off", "Could not log off, please retry");
                 cancel(false);
             }
         }
     }
 }
