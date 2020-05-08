 package com.example;
 import java.util.Timer;
 import java.util.TimerTask;
 import com.example.R;
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 
 public class WifiLocator extends Activity {
 	TextView WifiState;
 	ToggleButton OnOff;
 	TextView textStatus;
 	Button Scan;
 	CheckBox AutoScan;
 	Timer timer;
 	Boolean scanOnClick;
 	Button saveLabel;
 	
 	/** Called when the activity is first created. */
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.initialize();
 		
 		this.setUpInterface();
 		// Register Receivers && Listeners
 	    this.registerReceiver(this.WifiStateChangedReceiver,
 	    		new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
 	    this.registerReceiver(this.WifiScanAvailableReceiver,
 	    		new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
 		this.setUpButtonListeners();
 		
         
 	}
 	
 	private void initialize(){
  		scanOnClick = false;
 		WifiManager wifi = (WifiManager) getBaseContext().getSystemService(Context.WIFI_SERVICE);
 	    wifi.setWifiEnabled(true);
 	}
 	
 	private void setUpInterface(){
 		setContentView(R.layout.main);
 		OnOff = (ToggleButton) findViewById(R.id.onoff);
 		WifiState = (TextView)findViewById(R.id.wifistate);
 	    Scan = (Button)findViewById(R.id.scan);
  		textStatus = (TextView) findViewById(R.id.textStatus);
  		AutoScan = (CheckBox) findViewById(R.id.autoscan);
  		saveLabel = (Button) findViewById(R.id.saveLabel);
  		
 	}
 	
 	// WiFi Status
 	private BroadcastReceiver WifiStateChangedReceiver
     	= new BroadcastReceiver(){
 		@Override	
 	    public void onReceive(Context context, Intent intent) {
 			int extraWifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
 	        		WifiManager.WIFI_STATE_UNKNOWN);
 		   
 		    switch(extraWifiState){
 		    case WifiManager.WIFI_STATE_DISABLED:
 			    WifiState.setText("WIFI IS DISABLED");
 			    break;
 		    case WifiManager.WIFI_STATE_DISABLING:
 			    WifiState.setText("WIFI IS DISABLING");
 			    break;
 		    case WifiManager.WIFI_STATE_ENABLED:
 			    WifiState.setText("WIFI IS ENABLED");
 			    break;
 		    case WifiManager.WIFI_STATE_ENABLING:
 			    WifiState.setText("WIFI IS ENABLING");
 			    break;
 		    case WifiManager.WIFI_STATE_UNKNOWN:
 			    WifiState.setText("WIFI IS UNKNOWN");
 			    break;
 			default:
 				WifiState.setText("Error: Unknown extraWifiState value!!!!");
 				break;
 		    }
 		}
     };
 
    //List WiFi when Scan finishes
     private BroadcastReceiver WifiScanAvailableReceiver
     	= new BroadcastReceiver(){
     	
 		@Override
 		public void onReceive(Context context, Intent intent){
 			if (AutoScan.isChecked() || scanOnClick){
 				StringBuilder status = new StringBuilder();
 				WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
 				Fingerprint fingerprint = new Fingerprint(wifi.getScanResults());
 				
 				if (fingerprint.getSize() == 0){
 					status.append("No WiFi connection!!!");
 				} else {
 					if (AutoScan.isChecked()){
 						Scan.setEnabled(false);
 					} else {
 						Scan.setEnabled(true);
 					}
 					status.append("List of available WiFi: \n\n");
 					status.append(fingerprint.toString());
 				}
 				textStatus.setText(status);
 				scanOnClick = false;
 			}
 		}
     };
     
     private void setUpButtonListeners(){
     	// saveLabel Listener -> go to saved label activity
     	saveLabel.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 Intent myIntent = new Intent(view.getContext(), SavedLabelActivity.class);
                 startActivityForResult(myIntent, 0);
             }
         });
     	//OnOff Listener -> enable/disable WiFi
 	    OnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
 	        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 	        	WifiManager wifi;
 	        	
 	        	if (isChecked) {
 	            	wifi = (WifiManager) getBaseContext().getSystemService(Context.WIFI_SERVICE);
 	 			    wifi.setWifiEnabled(false);
 	 			    scanOnClick = false;
 	            } else {
 	            	wifi = (WifiManager) getBaseContext().getSystemService(Context.WIFI_SERVICE);
 				    wifi.setWifiEnabled(true);
 				    scanOnClick = false;
 	            }
 	        }
 	    });
 
 		//Scan Listener -> Start scanning		
 		Scan.setOnClickListener(new Button.OnClickListener(){
 			@Override
 			public void onClick(View arg0) {	   
 				StringBuilder status = new StringBuilder();
 				WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
 				
 				if (wifi.getWifiState() == WifiManager.WIFI_STATE_ENABLED){
 					if (wifi.startScan() == false){
 						status.append("Scan failed!!!") ;
 					} else {
 						Scan.setEnabled(false);
 						status.append("Scanning...");
 					}
 				} else{
 					status.append("WiFi is off, turn it on now !!! \n");
 				}
 				textStatus.setText(status);
 				scanOnClick = true;
 			}
 		});
 		
 		//Auto Listener -> Start auto scanning
 		
 		AutoScan.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				
 				if (AutoScan.isChecked()) {
 					timer = new Timer();
 				    timer.schedule(new TimerTask() {          
 				        @Override
 				        public void run() {
 				        	StringBuilder status = new StringBuilder();
 				        	WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
 							
 							if (wifi.getWifiState() == WifiManager.WIFI_STATE_ENABLED){
 								if (wifi.startScan() == false){
 									status.append("Scan failed!!!") ;
 								} else {
 									status.append("Auto Scanning...");
 								}
 							} else{
 								status.append("WiFi is off, turn it on now !!! \n");
 							}
 							//textStatus.setText(status);
 				        }
 				    }, 0, 1000);
 					Scan.setEnabled(false);
 				} else{
 					timer.cancel();
 					Scan.setEnabled(true);
 				}
 			}
 		});		
     }
     
     @Override
     public void onDestroy(){
     	super.onDestroy();
     	this.unregisterReceiver(this.WifiScanAvailableReceiver);
     	this.unregisterReceiver(this.WifiStateChangedReceiver);
     }
     
 }
