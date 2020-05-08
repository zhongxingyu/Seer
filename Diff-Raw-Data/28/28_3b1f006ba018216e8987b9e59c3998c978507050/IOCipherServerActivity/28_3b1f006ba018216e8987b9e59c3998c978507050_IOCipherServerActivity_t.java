 package info.guardianproject.iocipher.server;
 
 /*
  * Includes code from:
  * http://code.google.com/p/swiftp/source/browse/trunk/src/org/swiftp/FTPServerService.java#482
 
 Copyright 2009 David Revell
 
 This file is part of SwiFTP.
 
 SwiFTP is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 SwiFTP is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with SwiFTP.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 import info.guardianproject.iocipher.server.WebServerService.LocalBinder;
 
 import java.io.File;
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.Enumeration;
 
 
 import android.app.AlertDialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 
 public class IOCipherServerActivity extends SherlockActivity {
 	
 	private final static String TAG = "IOCipherServer";
 	
 	private final String ksFileName = "iocipher.bks";
 	private final String ksPassword = "changeme";
 	private final String ksAlias = "twjs";
 	
 	
     boolean mBound = false;
 
     private WebServerService mService;
 
     private final static int DEFAULT_PORT = 8443;
 	private int mWsPort = -1;
 	private boolean mWsUseSSL = true;
 	private boolean runOnBind = false;
 	
 
 	private MenuItem mMenuStartTop;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         setPreferences();
 
         /*
         tButton = (ToggleButton)findViewById(R.id.toggleButton1);
         tButton.setEnabled(false);
         tButton.setOnCheckedChangeListener(new OnCheckedChangeListener () {
 
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView,
 					boolean isChecked) {
 				
 				if (isChecked)
 					startWebServer();
 				else
 					stopWebServer();
 				
 			}
         	
         });
         */
         
     }
     
     private void setPreferences ()
     {
     	
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
         
         mWsUseSSL = prefs.getBoolean("useSSL", true);
         mWsPort = Integer.parseInt(prefs.getString("prefPort", "" + DEFAULT_PORT));
     }
     
     
     @Override
 	protected void onResume() {
 		super.onResume();
 		
        showStatus();
        
 		bindService();
 	    
 	}
 
     @Override
     protected void onStop() {
         super.onStop();
         // Unbind from the service
         if (mBound) {
             unbindService(mConnection);
             mBound = false;
         }
     }
 
 
 	public void bindService ()
     {
         Intent intent = new Intent(this, WebServerService.class);
         startService(intent);
         bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
 
     }
     
     public void startWebServer()
     {
     	
     	if (mService == null)
     	{
     		runOnBind = true;
     		bindService();
     		
     	}
     	else
     	{
     		try
     		{
    			mService.startServer(mWsPort, mWsUseSSL, getWifiIp(this).getHostAddress());
 
     	    	showStatus();
     		}
     		catch (Exception e)
     		{
     			Log.e(TAG, "unable to start secure server",e);
     		}
     		
     	}
     }
     
     public void stopWebServer ()
     {
     	mService.stopServer();
     	
    		clearStatus ();
     }
     
     private void postBound ()
     {
 
         
         if (runOnBind)
         {
         	startWebServer();
         }
         
         showStatus();
         
         
     }
     /** Defines callbacks for service binding, passed to bindService() */
     private ServiceConnection mConnection = new ServiceConnection() {
 
         @Override
         public void onServiceConnected(ComponentName className,
                 IBinder service) {
             // We've bound to LocalService, cast the IBinder and get LocalService instance
             LocalBinder binder = (LocalBinder) service;
             mService = binder.getService();
             mBound = true;
             
             postBound ();
             
             
         }
 
         @Override
         public void onServiceDisconnected(ComponentName arg0) {
             mBound = false;
         }
     };
     
     private void showStatus ()
     {
     	TextView tv = (TextView)findViewById(R.id.textStatus);
     	StringBuffer msg = new StringBuffer();
     	
     	String ip = getWifiIp(this).getHostAddress();
     	
     	msg.append("Wifi IP: ").append(ip);
     	msg.append("\n\n");
     	
     	if (mService != null && mService.isServerRunning())
     	{
     		if (mMenuStartTop != null)
     		mMenuStartTop.setIcon(android.R.drawable.ic_media_pause);
     		
     		String protocol = "https";
     		if (!mWsUseSSL)
     			protocol = "http";
     	
     		msg.append("Web Browser:").append('\n');
     		msg.append(protocol).append("://").append(ip).append(':').append(mWsPort).append("/public");
     		msg.append("\n\n");
     		
     		msg.append("WebDAV Share:").append('\n');
     		msg.append(protocol).append("://").append(ip).append(':').append(mWsPort).append("/sdcard");
     		msg.append("\n\n");
     		
     		String fingerprint = "";
         	
     		
         	File fileKS = new File(this.getFilesDir(),ksFileName);
     	
     		
     		CACertManager ccm = new CACertManager();
     		try {
     			ccm.load(fileKS.getAbsolutePath(), ksPassword);
     			fingerprint = ccm.getFingerprint(ccm.getCertificateChain(ksAlias)[0], "SHA1");
     			
         		msg.append("SHA1 Fingerprint").append('\n');
         		msg.append(fingerprint);
 
     		} catch (Exception e) {
     			Log.e(TAG,"error loading fingerprint",e);
     		} 
     		
     		
     	}
     	else
     	{
     		if (mMenuStartTop != null)
     		mMenuStartTop.setIcon(android.R.drawable.ic_media_play);
     		
     		msg.append("(Server deactivated)");
     		
     	}
 		
 		
 		
 		tv.setText(msg.toString());
     }
     
     private void clearStatus ()
     {
     	TextView tv = (TextView)findViewById(R.id.textStatus);
     
     	tv.setText("");
     }
     
 	@Override
 	protected void onStart() {
 		super.onStart();
 		
 		
 	}
 
 
     /**
      * Gets the IP address of the wifi connection.
      * @return The integer IP address if wifi enabled, or null if not.
      */
     public static InetAddress getWifiIp(Context myContext) {
             
             WifiManager wifiMgr = (WifiManager)myContext
                                     .getSystemService(Context.WIFI_SERVICE);
             if(isWifiEnabled(myContext)) {
                     int ipAsInt = wifiMgr.getConnectionInfo().getIpAddress();
                     if(ipAsInt == 0) {
                             return null;
                     } else {
                             return intToInet(ipAsInt);
                     }
             } else {
                     return null;
             }
     }
     
     public static byte byteOfInt(int value, int which) {
         int shift = which * 8;
         return (byte)(value >> shift); 
 }
     
     public static InetAddress intToInet(int value) {
         byte[] bytes = new byte[4];
         for(int i = 0; i<4; i++) {
                 bytes[i] = byteOfInt(value, i);
         }
         try {
                 return InetAddress.getByAddress(bytes);
         } catch (UnknownHostException e) {
                 // This only happens if the byte array has a bad length
                 return null;
         }
     }
     
     public static boolean isWifiEnabled(Context myContext) {
 
     	WifiManager wifiMgr = (WifiManager)myContext
                                     .getSystemService(Context.WIFI_SERVICE);
             if(wifiMgr.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
                     return true;
             } else {
                     return false;
             }
     }
     
 	public NetworkInterface getWifiNetworkInterface() {
 		 
 		 WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
 
 	    Enumeration<NetworkInterface> interfaces = null;
 	    try {
 	        //the WiFi network interface will be one of these.
 	        interfaces = NetworkInterface.getNetworkInterfaces();
 	    } catch (SocketException e) {
 	        return null;
 	    }
 	 
 	    //We'll use the WiFiManager's ConnectionInfo IP address and compare it with
 	    //the ips of the enumerated NetworkInterfaces to find the WiFi NetworkInterface.
 	 
 	    //Wifi manager gets a ConnectionInfo object that has the ipAdress as an int
 	    //It's endianness could be different as the one on java.net.InetAddress
 	    //maybe this varies from device to device, the android API has no documentation on this method.
 	    int wifiIP = manager.getConnectionInfo().getIpAddress();
 	 
 	    //so I keep the same IP number with the reverse endianness
 	    int reverseWifiIP = Integer.reverseBytes(wifiIP);       
 	 
 	    while (interfaces.hasMoreElements()) {
 	 
 	        NetworkInterface iface = interfaces.nextElement();
 	 
 	        //since each interface could have many InetAddresses...
 	        Enumeration<InetAddress> inetAddresses = iface.getInetAddresses();
 	        while (inetAddresses.hasMoreElements()) {
 	            InetAddress nextElement = inetAddresses.nextElement();
 	            int byteArrayToInt = byteArrayToInt(nextElement.getAddress(),0);
 	 
 	            //grab that IP in byte[] form and convert it to int, then compare it
 	            //to the IP given by the WifiManager's ConnectionInfo. We compare
 	            //in both endianness to make sure we get it.
 	            if (byteArrayToInt == wifiIP || byteArrayToInt == reverseWifiIP) {
 	                return iface;
 	            }
 	        }
 	    }
 	 
 	    return null;
 	}
 	 
 	public static final int byteArrayToInt(byte[] arr, int offset) {
 	    if (arr == null || arr.length - offset < 4)
 	        return -1;
 	 
 	    int r0 = (arr[offset] & 0xFF) << 24;
 	    int r1 = (arr[offset + 1] & 0xFF) << 16;
 	    int r2 = (arr[offset + 2] & 0xFF) << 8;
 	    int r3 = arr[offset + 3] & 0xFF;
 	    return r0 + r1 + r2 + r3;
 	}
 		
 	
 	 @Override
     public boolean onCreateOptionsMenu(Menu menu) {
       
 		 mMenuStartTop = menu.add(Menu.NONE,1,Menu.NONE,"Start");
 		 
 		 mMenuStartTop.setIcon(android.R.drawable.ic_media_play)
          .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
     
 		 
         menu.add(Menu.NONE,2,Menu.NONE,"Settings")
             .setIcon(android.R.drawable.ic_menu_preferences)
             .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
         
         
         menu.add(Menu.NONE,3,Menu.NONE,"About")
         .setIcon(android.R.drawable.ic_menu_info_details)
         .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
 
         return true;
     }
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		switch (item.getItemId())
 		{
 
 			case (1):
 			
 				if (!mService.isServerRunning())
 				{
 					startWebServer();
 					item.setIcon(android.R.drawable.ic_media_pause);
 				}
 				else
 				{
 					stopWebServer();
 					item.setIcon(android.R.drawable.ic_media_play);
 				}
 				
 			
 			break;
 			
 			case (2):
 				showPrefs();
 			break;
 			
 			case (3):
 				showAbout ();
 			break;
 			default:
 			
 		}
 		
 		return super.onOptionsItemSelected(item);
 	}
 	 
 	private void showPrefs ()
 	{
 		Intent intent = new Intent (this, IOCipherSettingsActivity.class);
 		startActivity(intent);
 	}
 	
 	private void showAbout ()
 	{
 		 new AlertDialog.Builder(this)
 	         .setTitle(getString(R.string.app_name))
 	         .setMessage(getString(R.string.about))
 	         .create().show();
 		
 	}
 	 
 }
