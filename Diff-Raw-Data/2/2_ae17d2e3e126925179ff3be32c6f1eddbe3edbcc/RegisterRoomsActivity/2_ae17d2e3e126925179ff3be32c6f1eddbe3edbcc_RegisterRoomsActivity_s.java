 package com.accenture.RegisterRooms;
 
 import java.util.List;
 
 import com.accenture.RegisterRooms.R;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.net.wifi.ScanResult;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * Activity for position. Needs a position handler to have any use
  * 
  * @author audun.sorheim, cecilie haugstvedt, kristin astebol
  * 
  */
 
 public class RegisterRoomsActivity extends Activity {
 
 	private String TAG = "RegisterRoomActivity";
 	private Button populatePositionDataButton, sendRoomButton,
 			getWhereIAmButton;
 	private ImageButton addNewLocationButton;
 	private TextView ssidTextView;
 	private WifiPositionHandler wifiPositionHandler;
 	private List<ScanResult> scanList, oldList;
 	private RestClient restClient;
 	private LocationHandler locationHandler;
 	private ArrayAdapter<CharSequence> adapter;
 	private Spinner spinner;
 	private RegisterRoomsActivity registerRoomsActivity = this;
 	private Spinner terrorSpinner;
 	private ArrayAdapter<CharSequence> spinnerAdapter;
 	private EditText usernameEditText, passwordEditText;
 	private Button loginButton;
 	private Dialog dialog;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		restClient = RestClient.getInstance();
 		
 		if (!restClient.hasCredentials()) {
 			loginDialog();
 		}
 
 		// Init GUI
 		addNewLocationButton = (ImageButton) findViewById(R.id.addButton);
 		ssidTextView = (TextView) findViewById(R.id.bssid);
 		populatePositionDataButton = (Button) findViewById(R.id.populateLocationButton);
 		sendRoomButton = (Button) findViewById(R.id.getRoomButton);
 		getWhereIAmButton = (Button) findViewById(R.id.getWhereAmIButton);
 
 	}
 
 	private void loginDialog() {
 		dialog = null;
 		dialog = new Dialog(RegisterRoomsActivity.this);
 
 		dialog.setContentView(R.layout.dialoglayout);
		dialog.setTitle("Custom Dialog");
 
 		usernameEditText = (EditText) dialog
 				.findViewById(R.id.usernameedittext);
 		passwordEditText = (EditText) dialog
 				.findViewById(R.id.passwordedittext);
 		loginButton = (Button) dialog.findViewById(R.id.pinadgrett);
 		loginButton.setOnClickListener(loginButton_UpdateOnClickListener);
 
 		dialog.show();
 	}
 
 	private void doStuff() {
 
 		// Checks if wifi is enabled, and pops up an alertdialog if not, asking
 		// to enable it
 		locationHandler = new LocationHandler(this);
 		wifiPositionHandler = WifiPositionHandler.getInstance();
 		wifiPositionHandler.init(this);
 		boolean wifiEnabled = wifiPositionHandler.isWifiEnabled();
 		if (wifiEnabled != true) {
 			wifiPositionHandler.setWifiState(true);
 			AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
 			alt_bld.setMessage("Applikasjonen avhenger av wifi. Skru på?")
 					.setCancelable(false)
 					.setPositiveButton("Ja",
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int id) {
 									initiateComponents();
 								}
 							})
 					.setNegativeButton("Nei",
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int id) {
 									registerRoomsActivity.finish();
 								}
 							});
 			AlertDialog alert = alt_bld.create();
 			alert.setTitle("Registrering av punkt");
 			alert.show();
 		} else {
 			initiateComponents();
 			Log.e("initcomponents", "Testing");
 		}
 	}
 
 	private Button.OnClickListener loginButton_UpdateOnClickListener = new Button.OnClickListener() {
 
 		@Override
 		public void onClick(View arg0) {
 			String username = usernameEditText.getText().toString();
 			//Log.e("Username ", username);
 			String password = passwordEditText.getText().toString();
 			// Log.e("Password ", password);
 			if (!username.equals("") && !password.equals("")) {
 				RestClient.getInstance().setCredentials(username, password);
 				dialog.dismiss();
 				doStuff();
 			} else {
 			}
 			
 		}
 	};
 
 	/**
 	 * Initiates activity components / states.
 	 */
 	private void initiateComponents() {
 		wifiPositionHandler.scanForSSID();
 
 		// Getting locations from server
 		Location[] locations = null;
 		locations = restClient.getAllLocations();
 		if (locations == null) {
 			printToScreen("Fikk ikke lokasjoner fra serveren");
 			Log.e("Locationstest", "Kunne ikke hente lokasjoner fra server");
 		} else {
 			Log.e("Locationtest", "Hentet lokasjoner fra server");
 			printToScreen("Hentet lokasjoner fra server");
 			populateSpinner(locations);
 		}
 
 		// Sets listener on buttons
 		sendRoomButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				sendSampleToServer();
 			}
 		});
 
 		populatePositionDataButton
 				.setOnClickListener(new View.OnClickListener() {
 					public void onClick(View v) {
 						updateBSSID();
 					}
 				});
 
 		getWhereIAmButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				checkWhereIAm();
 			}
 		});
 
 		addNewLocationButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				final AlertDialog.Builder alert = new AlertDialog.Builder(
 						RegisterRoomsActivity.this);
 				final EditText input = new EditText(RegisterRoomsActivity.this);
 				alert.setView(input);
 				alert.setMessage("Navn på ny lokasjon");
 				alert.setPositiveButton("Ok",
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog,
 									int whichButton) {
 								String value = input.getText().toString()
 										.trim();
 								adapter.add(value);
 							}
 						});
 
 				alert.setNegativeButton("Cancel",
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog,
 									int whichButton) {
 								dialog.cancel();
 							}
 						});
 				alert.show();
 			}
 		});
 	}
 
 	/**
 	 * Figures out which location you are at, and toasts it to the screen.
 	 */
 	public void checkWhereIAm() {
 		Location currentLocation = wifiPositionHandler.getPositionFromServer();
 		if (currentLocation != null) {
 			printToScreen(currentLocation.getlocationName() + " : "
 					+ currentLocation.getLocationId());
 		} else {
 			Log.e(TAG, "getPositionFromServer returned null");
 			printToScreen("No room found");
 		}
 	}
 
 	public void populateTerrorSpinner(List<ScanResult> scanList2) {
 		spinnerAdapter = new ArrayAdapter<CharSequence>(this,
 				android.R.layout.simple_spinner_item);
 		spinnerAdapter
 				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 
 		for (ScanResult result : scanList2) {
 			spinnerAdapter.add(result.BSSID + ":" + result.level);
 		}
 
 		terrorSpinner = (Spinner) findViewById(R.id.terrorSpinner);
 		terrorSpinner.setAdapter(spinnerAdapter);
 	}
 
 	/**
 	 * Sends a sample to the server. Pops up a alert dialog which lets the user
 	 * select which location name to be used
 	 */
 	public void sendSampleToServer() {
 		String locationName = spinner.getSelectedItem().toString();
 		String sample = getSample(locationName);
 		if (sample != null) {
 			boolean sent = restClient.sendCurrentLocation(sample);
 			if (sent == true) {
 				printToScreen("Punktet ble registrert");
 			} else {
 				printToScreen("Punktet ble ikke registrert");
 			}
 		} else {
 			printToScreen("Fant ingen sample å sende, prøv igjen en gang eller tre!");
 		}
 	}
 
 	/**
 	 * Populates the spinner with locations
 	 * 
 	 * @param locations
 	 */
 	public void populateSpinner(Location[] locations) {
 		adapter = new ArrayAdapter<CharSequence>(this,
 				android.R.layout.simple_spinner_item);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 
 		for (Location loc : locations) {
 			adapter.add(loc.getlocationName());
 		}
 
 		spinner = (Spinner) findViewById(R.id.locationSpinner);
 		spinner.setAdapter(adapter);
 	}
 
 	/**
 	 * Takes a given location name as parameter and creates / returns a sample
 	 * in json
 	 * 
 	 * @param location
 	 * @return
 	 */
 	public String getSample(String location) {
 		String jsonRoom = null;
 		jsonRoom = wifiPositionHandler.registerSampleToJSON(location);
 		return jsonRoom;
 	}
 
 	/**
 	 * Initializes a SSID search in the handler Update the (GUI) BSSID
 	 * (MAC-address for the router)
 	 */
 	public void updateBSSID() {
 		if (wifiPositionHandler.getScanList() == null) {
 			printToScreen("Der er ingen scan resultater. Scanner..");
 			wifiPositionHandler.scanForSSID();
 		} else {
 			scanList = wifiPositionHandler.getScanList();
 			populateTerrorSpinner(scanList);
 			if (oldList != scanList) {
 				oldList = scanList;
 				ssidTextView.setText("Fant ny wifi info");
 			} else {
 				ssidTextView.setText("Ikke funnet ny info enda");
 			}
 		}
 
 	}
 
 	/**
 	 * Toasts strings to the screen
 	 * 
 	 * @param text
 	 *            string to be toasted
 	 */
 	public void printToScreen(String text) {
 		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
 	}
 }
