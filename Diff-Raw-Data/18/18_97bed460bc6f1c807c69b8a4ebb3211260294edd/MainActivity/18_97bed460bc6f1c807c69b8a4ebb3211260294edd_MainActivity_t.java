 package com.ageatches.routerCPR;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.nio.ByteBuffer;
 import java.util.List;
 
 import android.annotation.SuppressLint;
 import android.content.Context;
 import android.net.DhcpInfo;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.webkit.WebView;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.ageatches.routerCPR.BruteForceTask.Credential;
 import com.ageatches.routerCPR.BruteForceTask.Error;
 import com.ageatches.routerCPR.domain.Password;
 import com.ageatches.routerCPR.domain.User;
 import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
 import com.j256.ormlite.dao.RuntimeExceptionDao;
 
 public class MainActivity extends OrmLiteBaseActivity<DatabaseHelper> implements BruteForceTaskListener {
 	
 	private EditText addressText;
 	private WebView status;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         addressText = (EditText)findViewById(R.id.address);
         status = (WebView)findViewById(R.id.status);
         
         initializeStatus();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	int itemId = item.getItemId();
     	
     	if (itemId == R.id.menu_guess_gateway) {
     		String gatewayAddress = getGatewayAddress();
     		if (gatewayAddress == null) {
     			Toast.makeText(this, "Unable to retrieve gateway", Toast.LENGTH_SHORT).show();
     		} else {
     			addressText.setText(gatewayAddress);
     		}
     		
     		return true;
    	} else if (itemId == R.id.menu_debug_known) {
    		addressText.setText("mopsled.com/r");
     	}
     	
     	return super.onOptionsItemSelected(item);
     }
     
     public void recoverAction(View v) {
     	clearStatus();
    	clearSubStatus();
    	
     	String address = addressText.getText().toString();
     	appendToStatus("Starting recovery of " + address + "...");
     	
     	RuntimeExceptionDao<User, Integer> userDao = getHelper().getUserDao();
     	List<User> users = userDao.queryForAll();
     	
     	RuntimeExceptionDao<Password, Integer> passwordDao = getHelper().getPasswordDao();
     	List<Password> passwords = passwordDao.queryForAll();
     		
     	new BruteForceTask(addressText.getText().toString(), users, passwords, this).execute();
     }
 
     public void processBruteForceTaskSucceeded(Credential credentials) {
     	appendToStatus("Brute force successful!");
		String status = credentials.getUser().getUser() + "/" + credentials.getPassword().getPassword();
 		appendToStatus("Credentials: " + status);
		
		clearSubStatus();
 	}
 
     public void processBruteForceTaskFailed(Error error) {
 		if (error == Error.COULD_NOT_CONNECT) {
 			appendToStatus("Error: could not connect to address");
 		} else if (error == Error.AUTHENTICATION_UNECESSARY) {
 			appendToStatus("Error: address given does not require authentication");
 		} else if (error == Error.INVALID_URL) {
 			appendToStatus("Error: could not understand the address");
 		} else if (error == Error.UNKNOWN_RESPONSE_CODE) {
 			appendToStatus("Error: unknown reponse code returned by server");
 		} else if (error == Error.COULD_NOT_BRUTE_FORCE) {
 			appendToStatus("Brute force failed. Unable to brute force address.");
 		}
 		
 		clearSubStatus();
 	}
 
 	public void processBruteForceTaskUpdate(String update) {
 		setSubStatus(update);
 	}
 	
 	@SuppressLint("SetJavaScriptEnabled")
 	private void initializeStatus() {
         clearStatus();
         status.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
         status.getSettings().setJavaScriptEnabled(true);
 	}
 	
 	private void clearStatus() {
 		status.loadUrl("file:///android_asset/status.html");
 	}
 
     private void appendToStatus(String update) {
     	status.loadUrl("javascript:appendToStatus('" + update + "')");
     }
     
     private void setSubStatus(String message) {
     	status.loadUrl("javascript:setSubStatus('" + message + "')");
     }
     
     private void clearSubStatus() {
     	status.loadUrl("javascript:setSubStatus('')");
     }
     
     private String getGatewayAddress() {
     	WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
     	DhcpInfo info = wifiManager.getDhcpInfo();
     	
     	byte[] infoBytesAndroidOrder = ByteBuffer.allocate(4).putInt(info.gateway).array();
     	byte[] infoBytesNetworkOrder = new byte[4];
     	for (int i = 0; i < infoBytesAndroidOrder.length; i++) {
     		infoBytesNetworkOrder[infoBytesAndroidOrder.length - i - 1] = infoBytesAndroidOrder[i];
     	}
     	
     	String gateway = null;
     	
     	try {
 			gateway = InetAddress.getByAddress(infoBytesNetworkOrder).getHostAddress();
 		} catch (UnknownHostException e) {
 			Log.d(this.getClass().getName(), "Unable to turn gateway " + Integer.toString(info.gateway) + " into IP address", e);
 		}
     	
     	return gateway;
     }
     
 }
