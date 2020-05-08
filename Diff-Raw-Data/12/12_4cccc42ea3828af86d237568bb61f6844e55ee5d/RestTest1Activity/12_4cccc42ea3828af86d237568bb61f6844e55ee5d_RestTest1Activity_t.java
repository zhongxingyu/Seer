 package fi.side;
 
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.sql.Timestamp;
 import java.util.Enumeration;
 
 import org.restlet.Server;
 import org.restlet.data.Form;
 import org.restlet.data.Protocol;
 import org.restlet.resource.ClientResource;
 
 import fi.side.restlet.AndroidRestlet;
 import fi.side.sensors.Sensor;
 import fi.side.sensors.SensorListener;
 import fi.soberit.ubiserv.Data.DataRecord;
 import fi.soberit.ubiserv.Data.IDataAdd;
 
  
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class RestTest1Activity extends Activity {
     /** Called when the activity is first created. */
 
 	   final String tag = "RestApp";
   
    
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
     	Button btnStart = (Button)findViewById(R.id.btnStart);
     	btnStart.setOnClickListener(onStart);
     		
     	Button btnShowIp = (Button)findViewById(R.id.btnShowIp);
     	btnShowIp.setOnClickListener(onBtnShowIp);
     	
     	Button btnSendIp = (Button)findViewById(R.id.btnSendIp);
     	btnSendIp.setOnClickListener(onBtnSendIp);
     	
     	Button btnSendHeartRate = (Button)findViewById(R.id.btnSendHeartRate);
     	btnSendHeartRate.setOnClickListener(onBtnSendHeartRate);
     	
     	registerReceivers();
     	
     	AndroidRestlet.init();
     	AndroidRestlet.setPhoneUID(getPhoneUID());
     	AndroidRestlet.ipAddress = getString(R.string.server_address);
     	
     	//TODO: move it to the class
    	//AndroidRestlet.sensor = new Sensor();
     	//AndroidRestlet.sensor.addListener(sensorListener);
     	
  
         
     } 
     
 	private View.OnClickListener onBtnShowIp = new View.OnClickListener() {
 		
 		public void onClick(View arg0) {
 			TextView tvIP = (TextView)findViewById(R.id.tvIP);
 			tvIP.setText(AndroidRestlet.getLocalIpAddress());				
 		}
 	};
 	
 	//Sending mock data to the server. Just for test purposes.
 	private View.OnClickListener onBtnSendHeartRate = new View.OnClickListener() {
 		
 		public void onClick(View v) {
 			AndroidRestlet.sensor.updateSensor("new sensor data");
 	};
 	};
 	
 
 
 	
 	//Test event for getting phone's IP not used in production
 	private View.OnClickListener onBtnSendIp = new View.OnClickListener() {
 		public void onClick(View arg0) {
 			AndroidRestlet.sendIp();
 		}
 	};
 	
 	// Used for phone's internal REST server
 	// For test only
     private View.OnClickListener onStart = new View.OnClickListener() {
 		
 		public void onClick(View v) {
 			try {
 				Server serv = new Server(Protocol.HTTP, 8190, PocketServer.class); 
 				serv.start();
 			} catch (Exception e) {
 			 
 				Log.w(tag, e.toString());
 			 
 			}
 			TextView tvHello = (TextView) findViewById(R.id.tvHello);
 			tvHello.setText("Test");
 		}
 	};
     
 
 	  
 
 	   
 	  /**
 	   * Getting phone's imei. Used as a unique identifier for a phone
 	   * @return
 	   */
 	   private String getPhoneUID() {
 		   	TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
 			String uid = telephonyManager.getDeviceId();
 			return uid;
 	   }
 	   
 	   private void registerReceivers() {    
 	       registerReceiver(mConnReceiver, 
 	           new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
 	   }
 	 
 	   
 	
     
 	   
 	   private BroadcastReceiver mConnReceiver = new BroadcastReceiver() {
 		   
 		   /**
 		    * Runs when phone's IP address has been changed
 		    */
 		   public void onReceive(Context context, Intent intent) {
 			  //no network available
 			   boolean connectivity = !intent.getBooleanExtra(ConnectivityManager. EXTRA_NO_CONNECTIVITY, false);
 			   String reason = intent.getStringExtra(ConnectivityManager.EXTRA_REASON);
 		       boolean isFailover = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false);
 		       NetworkInfo currentNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
 		       NetworkInfo otherNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO);
 		       if (connectivity) 
 		    	   try {
 		    		   AndroidRestlet.sendIp();
 				} catch (Exception e) {
 					Log.w(tag, e.toString());
 				}
 		    	 
 		   }  
 		   };
 	   
 }
