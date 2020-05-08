 package com.Cyberpad.Reroot;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 
 import com.Cyberpad.RerootService.RerootService;
 import com.Cyberpad.RerootService.RerootBinder;
 import com.google.zxing.integration.android.IntentIntegrator;
 import com.google.zxing.integration.android.IntentResult;
 
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
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.ImageButton;
 import android.widget.Toast;
 
 public class ConnectPickerActivity extends Activity {
 	
 	private RerootBinder mBinder;
 	
 	private ServiceConnection mConnection = new ServiceConnection() {
 
 		@Override
 		public void onServiceConnected( ComponentName className, IBinder binder) {
 			mBinder = ( RerootBinder )binder;
 			Toast.makeText( ConnectPickerActivity.this, "Connected to Service!", Toast.LENGTH_SHORT ).show();
 		}
 
 		@Override
 		public void onServiceDisconnected( ComponentName className ) {
 			Toast.makeText( ConnectPickerActivity.this, "Disconnected from Service!", Toast.LENGTH_SHORT ).show();
 			
 		}
 	};
 	
 	private BroadcastReceiver mAuthReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			// TODO Auto-generated method stub
 			Intent menuIntent = new Intent( ConnectPickerActivity.this, MainMenuActivity.class);
 			startActivity( menuIntent );
 			finish();
 		}
 	};
 	
 	@Override
 	public void onCreate( Bundle icicle ) {
 		super.onCreate( icicle );
 		
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		
 		setContentView( R.layout.connect_picker_layout );
 		
 		// TODO: This may move around.
 		//bindService( new Intent( this, RerootService.class ), mConnection, Context.BIND_AUTO_CREATE );
 		
 		ImageButton qr_button = ( ImageButton )findViewById( R.id.qr_button );
 		qr_button.setOnClickListener(new OnClickListener() {
 			public void onClick(View v){
 				try{
 					IntentIntegrator integrator = new IntentIntegrator( ConnectPickerActivity.this );
 					integrator.initiateScan();
					//Intent i = new Intent( ConnectPickerActivity.this, QRConnectActivity.class );
					//ConnectPickerActivity.this.startActivity(i);
 				}
 				catch(Exception ex){
 					Log.e( "Reroot", "Could not start the QRConnectActivity" );
 				}
 			}
 		});
 		
 		ImageButton bluetooth_button = (ImageButton)findViewById( R.id.bluetooth_button);
 		bluetooth_button.setOnClickListener(new OnClickListener(){
 			public void onClick(View v){
 				Log.i( "Reroot", "Start the Bluetooth Activity!");
 			}
 		});
 		
 		ImageButton ip_button = (ImageButton)findViewById( R.id.ip_button);
 		ip_button.setOnClickListener(new OnClickListener() {
 			public void onClick(View v){
 				try{
 					Intent i = new Intent( ConnectPickerActivity.this, IPConnectActivity.class );
 					ConnectPickerActivity.this.startActivity(i);
 				}
 				catch(Exception ex){
 					Log.e("Reroot", "Could not start the IPConnectActivity" );
 				}
 			}
 		});
 
 		super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 		
 		// Register the authentication broadcast receiver 
 		IntentFilter filter = new IntentFilter();
 		filter.addAction( Connector.AUTH_INTENT );
 		registerReceiver( mAuthReceiver, filter );
 	}
 	
 	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
 		IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
 		if( scanResult != null ) {
 			// handle scan result
 			String key = scanResult.getContents();
			try	{
 				
 				String address = key.substring(0,8);
 				String security_key = key.substring(8);
 				
 				int secret_key = Utility.HexStringToInteger(security_key);
 				InetAddress serverAddress = InetAddress.getByAddress(Utility.HexStringToByteArray(address));
 				
 				Connector c = Connector.getInstance(this);
 				c.ConnectToServer(serverAddress, secret_key);
 			}
 			catch (UnknownHostException uhe) {
 				uhe.printStackTrace();
 			}
 		}
 		// else continue with any other code you need in the method
 	}
 
 }
