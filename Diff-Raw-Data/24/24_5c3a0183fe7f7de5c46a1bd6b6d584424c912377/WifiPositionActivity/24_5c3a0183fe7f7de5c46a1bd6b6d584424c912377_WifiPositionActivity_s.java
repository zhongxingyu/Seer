 package accenture.networkandroid;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.location.LocationManager;
 import android.net.wifi.ScanResult;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * Activity for position. Needs a position handler to have any use
  * 
  * @author audun.sorheim, cecilie haugstvedt, kristin astebol
  * 
  */
 
 public class WifiPositionActivity extends Activity {
 
 	private String TAG = "WifiPositionActivity";
 	
 	private LocationHandler locationHandler;
 	private Button showBSSIDButton, getCurrentRoomButton, getRoomUsingEmulatorButton;
 	private TextView ssidTextView;
 	private Spinner bssidSpinner;
 	private ScanResult scanResult;
 	private WifiPositionHandler wifiPositionHandler;
 	private List<ScanResult> scanList;
 
 //	// Unused variables, used for finding latitude and longitude using LocationManager
 //	private TextView longitudeTextView, latitudeTextView;
 //	private Location currentLocation = null;
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		scanList = null;
 		locationHandler = new LocationHandler(this); // needed to start scan for BSSIDs automatically
 		
 		// Init GUI
 		bssidSpinner = (Spinner) findViewById(R.id.bssidspinner);
 		ssidTextView = (TextView) findViewById(R.id.bssid);
 		showBSSIDButton = (Button) findViewById(R.id.showBSSIDButton);
 		getCurrentRoomButton = (Button) findViewById(R.id.getRoomButton);
 		getRoomUsingEmulatorButton = (Button) findViewById(R.id.getRoomOnEmulatorButton);
 		Log.e(TAG, "GUI initialized");
 		
 		// Initialize WIFI position handler and scan for BSSID
 		wifiPositionHandler = new WifiPositionHandler(this);
 		wifiPositionHandler.scanForSSID();
 		Log.e(TAG, "Started scanning for BSSIDs");
 
 		// Set listeners on buttons
 		getCurrentRoomButton.setOnClickListener(new View.OnClickListener() {
//			@Override
 			public void onClick(View v) {
 				Log.e(TAG, "Clicked on get room");
 				Room currentRoom = wifiPositionHandler.getPosition();
 				if (currentRoom != null) {
 					printToScreen(currentRoom.getroomName() + " : "
 							+ currentRoom.getroomId());
 				} else {
 					Log.e(TAG, "getPosition returned null");
 					printToScreen("No room found");
 				}
 			}
 		});
 		showBSSIDButton.setOnClickListener(new View.OnClickListener() {
//			@Override
 			public void onClick(View v) {
 				Log.e(TAG, "Clicked on show BSSID");
 				updateBSSID();
 			}
 		});
 		getRoomUsingEmulatorButton.setOnClickListener(new View.OnClickListener() {
//			@Override
 			public void onClick(View v) {
 				Log.e(TAG, "Clicked on get room using emulator");
 				Room currentRoom = wifiPositionHandler.getPositionFromServer();
 				if (currentRoom != null) {
 					printToScreen(currentRoom.getroomName() + " : "
 							+ currentRoom.getroomId());
 				} else {
 					Log.e(TAG, "getPositionFromServer returned null");
 					printToScreen("No room found");
 				}
 				
 			}
 		});
 		Log.e(TAG, "Registered listeners on buttons");
 				
 	}
 	
 	//	/**
 	//	 * Gets latitude and longitude, and sets the non existing text views.
 	//	 */
 	//	public void updateLocation() {
 	//		if (wifiPositionHandler.getCurrentLocation() == null) {
 	//			Log.e("Location", "Location is null");
 	//		} else {
 	//			currentLocation = wifiPositionHandler.getCurrentLocation();
 	//			Log.e("NetworkAndroidActivity",
 	//					"Latitude: " + currentLocation.getLatitude()
 	//							+ " Longitude: " + currentLocation.getLongitude());
 	//
 	//			longitudeTextView.setText("" + currentLocation.getLongitude());
 	//			latitudeTextView.setText("" + currentLocation.getLatitude());
 	//		}
 	//	}
 
 	/**
 	 * Initializes a SSID search in the handler
 	 * Update the (GUI) BSSID (MAC-address for the router)
 	 */
 	public void updateBSSID() {
 		if (wifiPositionHandler.getScanList() == null) {
 			Log.e("SCANRESULTS", "There are no scan results");
 			wifiPositionHandler.scanForSSID();
 			
 		} else {
 			scanList = wifiPositionHandler.getScanList();
 			Log.e("SCANRESULTS", "Oh yeah");
 
 			scanResult = scanList.get(0);
 
 			Log.e("NetworkAndroidActivity", scanResult.BSSID + " "
 					+ scanResult.level);
 			ssidTextView.setText(scanResult.BSSID);
 			
 			ArrayAdapter<ScanResult> adapter = new ArrayAdapter<ScanResult>(this, android.R.layout.simple_spinner_item, scanList);
 			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 			bssidSpinner.setAdapter(adapter);			
 		}
 
 	}
 
 	public void printToScreen(String text) {
 		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
 	}
 	
 	//	/**
 	//	 * Deserialize a jsonSignal (string) to readable data in LogCat
 	//	 * 
 	//	 * @param jsonSignal
 	//	 */
 	//	public void readFromJSON(String jsonSignal) {
 	//		Gson gson = new Gson();
 	//		Signal[] signal = gson.fromJson(jsonSignal, Signal[].class);
 	//		for (int i = 0; i < signal.length; i++) {
 	//			Log.e("From JSON ",
 	//					signal[i].getBssid() + " " + signal[i].getSignalStrength());
 	//		}
 	//	}
 
 	//	/**
 	//	 * Tests writeListToJSON and readFromJSON
 	//	 */
 	//	public void testToFromJSON() {
 	//		String json = writeListToJSON(scanList);
 	//		readFromJSON(json);
 	//		Log.e("STRINGTEST", json);
 	//	}
 
 
 }
