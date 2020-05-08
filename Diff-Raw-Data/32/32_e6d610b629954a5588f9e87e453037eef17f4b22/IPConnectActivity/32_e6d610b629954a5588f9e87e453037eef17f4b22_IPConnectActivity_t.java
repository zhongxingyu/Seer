 package com.Cyberpad.Reroot;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.content.pm.ActivityInfo;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.View.OnClickListener;
 import android.widget.EditText;
 import android.widget.Button;
 import android.widget.Toast;
 
 
 public class IPConnectActivity extends Activity {
 	
 	  private BroadcastReceiver mAuthReceiver = new BroadcastReceiver() {
 	      @Override
 	      public void onReceive(Context context, Intent intent) {
 	          // TODO Auto-generated method stub
 	    	  Log.d( "QRConnectActivity", "We've authenticated.  Launching main menu." );
 	          Intent menuIntent = new Intent( IPConnectActivity.this, MainMenuActivity.class);
 	          startActivity( menuIntent );
 	          finish();
 	      }
 	  };
 	@Override
 	public void onCreate( Bundle icicle ) {
 		super.onCreate( icicle );
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView( R.layout.connect_ip_layout );
 		
 		Button connect = (Button)findViewById( R.id.connect_button );
 		connect.setOnClickListener(new OnClickListener() {
 			public void onClick(View v){
 				String security = getSecurity();
 				Log.i("Reroot", security);
 				connectIP(security);
 			}
 		});
 		
 		Button back = (Button)findViewById( R.id.ip_back_button );
 		back.setOnClickListener(new OnClickListener() {
 			public void onClick(View v){
 				IPConnectActivity.this.finish();
 			}
 		});
 		
 		super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 		
		// Register the authentication broadcast receiver 
		IntentFilter filter = new IntentFilter();
		filter.addAction( Connector.AUTH_INTENT );
		registerReceiver( mAuthReceiver, filter );
	}
	
	@Override
	public void onDestroy() {
		unregisterReceiver( mAuthReceiver );
		super.onDestroy();
 	}
 		
 	private String getSecurity() {
 		EditText ip_security = (EditText)findViewById( R.id.security_edit );
 		if (ip_security.getText().toString().equals(""))
 			return ip_security.getHint().toString();
 		return ip_security.getText().toString();
 	}
 	
 	private void connectIP(String key)
 	{
 		try	{
 			
 			String address = key.substring(0,8);
 			String security_key = key.substring(8);
 			
 			int secret_key = Utility.HexStringToInteger(security_key);
 //			Log.d("Reroot", Integer.toString(secret_key));
 			InetAddress serverAddress = InetAddress.getByAddress(Utility.HexStringToByteArray(address));
 			
 			Connector c = Connector.getInstance(this);
 			c.ConnectToServer(serverAddress, secret_key);
 		}
 		catch (UnknownHostException uhe) {
 			uhe.printStackTrace();
 		}
 	}
 }
