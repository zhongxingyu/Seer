 package fi.action.wpoint;
 
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.graphics.drawable.Drawable;
 import android.location.LocationManager;
 import android.net.wifi.WifiConfiguration;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 import android.util.Log;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 import com.google.android.maps.MyLocationOverlay;
 
 
 
 public class WPointActivity extends MapActivity {
 	private MapView map;  
 	private MyLocationOverlay myLocation;
 	private LocationManager locationManager;
     private Button scanButton;
 
 	WifiManager wifi;
 	BroadcastReceiver receiver;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         // layout
         setContentView(R.layout.main);
         map = (MapView) findViewById(R.id.MapView);
         map.setBuiltInZoomControls(true); // zooming
 
         // GPS & WiFi on? - not working properly (needs to check in some kind of listener)
         locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))) {
        	showDisabledAlertToUser();
        }
 
         /// WIFI THINGS (still under dev)
 		// Setup WiFi
 		wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
 		// Get WiFi status
 		WifiInfo info = wifi.getConnectionInfo();
 		Log.d("WPoint", "WiFi Status: " + info.toString());
 		// List available networks (empty on emulator?)
 		List<WifiConfiguration> configs = wifi.getConfiguredNetworks();
 		for (WifiConfiguration config : configs) {
 			Log.d("WPoint", config.toString());
 		}
 		// Register Broadcast Receiver
 		if (receiver == null)
 			receiver = new ScanReceiver(this);
 		registerReceiver(receiver, new IntentFilter(
 				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
         
         // position Helsinki
         map.getController().setCenter(getPoint(60.17, 24.94));
         map.getController().setZoom(13);
                 
         // spots
         List<Overlay> mapOverlays = map.getOverlays();
         Drawable drawable = this.getResources().getDrawable(R.drawable.blue_dot);
         Spot spotOverlay = new Spot(drawable, this);
         
         // test
         // N+ E+, Helsinki about 60 15N 25 30E 
         GeoPoint point = getPoint(60.17, 24.94);
         OverlayItem overlayitem = new OverlayItem(point, "SSID: Tonnikalapurkki (50dB)", "[Connect]");
         
         // load spots    
         myLocation = new MyLocationOverlay(this, map);
         spotOverlay.add(overlayitem);
         mapOverlays.add(spotOverlay);
         
         // button
         scanButton = (Button)findViewById(R.id.ButtonScan);
         scanButton.setOnClickListener(new OnClickListener(){      
             public void onClick(View v) {	
     			Log.d("WPoint", "onClick() wifi.startScan()");
     			wifi.startScan();
             }
         });
     }
     	
     @Override
     public void onResume() {
       super.onResume();
       myLocation.enableCompass();
     }
     
     @Override
     public void onPause() {
       super.onPause();
       myLocation.disableCompass();
     } 
 
 	@Override
 	public void onStop() {
 		unregisterReceiver(receiver);
 	}
 
     @Override
     protected boolean isRouteDisplayed() {
         return false;
     }
     
     private GeoPoint getPoint(double lat, double lon) {
         return (new GeoPoint((int) (lat * 1000000.0), (int) (lon * 1000000.0)));
     }
     
 	private void showDisabledAlertToUser() {
 		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
 		alertDialogBuilder.setMessage("GPS or/and WiFi are disabled in your device. Would you like to enable it?")
 		.setCancelable(false)
 		.setPositiveButton("Goto Settings Page",
 		new DialogInterface.OnClickListener(){
 			public void onClick(DialogInterface dialog, int id){
 				Intent callGPSSettingIntent = new Intent(
 				android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 				startActivity(callGPSSettingIntent);
 			}
 		});
 		alertDialogBuilder.setNegativeButton("Exit",
 		new DialogInterface.OnClickListener(){
 			public void onClick(DialogInterface dialog, int id) {
 				dialog.cancel();
 				System.exit(0);
 			}
 		});
 		AlertDialog alert = alertDialogBuilder.create();
 		alert.show();
 	}
 }
