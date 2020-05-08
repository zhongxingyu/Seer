 package com.worthwhilegames.cardgames.player.activities;
 
 import static com.worthwhilegames.cardgames.shared.Constants.GET_PLAYER_NAME;
 import static com.worthwhilegames.cardgames.shared.Constants.PLAYER_NAME;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
import android.widget.Toast;
 
 import com.worthwhilegames.cardgames.R;
 import com.worthwhilegames.cardgames.shared.TextView;
import com.worthwhilegames.cardgames.shared.Util;
 import com.worthwhilegames.cardgames.shared.activities.DeviceListActivity;
 import com.worthwhilegames.cardgames.shared.activities.WifiConnectActivity;
 import com.worthwhilegames.cardgames.shared.connection.ConnectionClient;
 import com.worthwhilegames.cardgames.shared.connection.ConnectionConstants;
 import com.worthwhilegames.cardgames.shared.connection.ConnectionFactory;
 import com.worthwhilegames.cardgames.shared.connection.ConnectionType;
 
 /**
  * The Activity that initiates the device list, and then
  * waits for the connection to be made, and finally
  * waits for the game to begin before moving on to display
  * the user's hand.
  *
  * Activity Results:
  * 		RESULT_OK - If the user is connected and the game can begin
  * 		RESULT_CANCELLED - If the user cancelled or is not connected
  */
 public class ConnectActivity extends Activity {
 
 	/**
 	 * The request code to handle the result of the device list Activity
 	 */
 	private static int DEVICE_LIST_RESULT = Math.abs(DeviceListActivity.class.getName().hashCode());
 
 	/**
 	 * Indicates whether the game is ready to start (a connection has been established)
 	 */
 	private boolean readyToStart = false;
 
 	/**
 	 * This intent will be set to the result of this activity the player name
 	 * will be added to this intent so ShowCardsActivity can know the name
 	 */
 	private Intent returnIntent;
 
 	/**
 	 * The BroadcastReceiver that handles state change messages from the
 	 * Connection module.
 	 */
 	private BroadcastReceiver receiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			int currentState = intent.getIntExtra(ConnectionConstants.KEY_STATE_MESSAGE, -1);
 
			if (Util.isDebugBuild()) {
				Toast.makeText(ConnectActivity.this, "onReceive " + currentState, Toast.LENGTH_LONG).show();
			}

 			// If the Connection state is connected, update the message displayed,
 			// and register a new receiver to handle the game initiation message
 			if (currentState == ConnectionConstants.STATE_CONNECTED) {
 				readyToStart = true;
 
 				TextView tv = (TextView) findViewById(R.id.progressDialogText);
 				tv.setText(getResources().getString(R.string.waitingForGame));
 
 				Intent getName = new Intent(ConnectActivity.this, EnterNameActivty.class);
 				startActivityForResult(getName, GET_PLAYER_NAME);
 
 				// Register the receiver for receiving messages from Connection
 				registerReceiver(gameStartReceiver, new IntentFilter(ConnectionConstants.MESSAGE_RX_INTENT));
 			} else if (currentState == ConnectionConstants.STATE_LISTEN) {
 				// If we went back to the listen state, display the device list
 				// because we are no longer connected like we used to be
 				readyToStart = false;
 
 				ConnectionType type = ConnectionFactory.getConnectionType(ConnectActivity.this);
 
 				if (type == ConnectionType.BLUETOOTH) {
 					// Show the device list
 					Intent showDeviceList = new Intent(ConnectActivity.this, DeviceListActivity.class);
 					startActivityForResult(showDeviceList, DEVICE_LIST_RESULT);
 				} else if (type == ConnectionType.WIFI) {
 					WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
 					manager.setWifiEnabled(true);
 
 					// Show popup telling the user to enter the IP address of the tablet to connect to
 					Intent showDeviceList = new Intent(ConnectActivity.this, WifiConnectActivity.class);
 					startActivityForResult(showDeviceList, DEVICE_LIST_RESULT);
 				}
 			}
 		}
 	};
 
 	/**
 	 * The BroadcastReceiver that handles the game initiation message
 	 * from the Connection module
 	 */
 	private BroadcastReceiver gameStartReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			int messageType = intent.getIntExtra(ConnectionConstants.KEY_MESSAGE_TYPE, 0);
 
 			// If we have a connection, and this message is indicating
 			// that the game has been initiated by the tablet, start the ShowCardsActivity
 			// and finish this Activity.
 			if (readyToStart && messageType == ConnectionConstants.MSG_TYPE_INIT) {
 				// We connected just fine, so bring them to the ShowCardsActivity, and close
 				// this activity out.
 
 				ConnectActivity.this.setResult(RESULT_OK, returnIntent);
 				ConnectActivity.this.finish();
			} else {
				if (Util.isDebugBuild()) {
					Toast.makeText(ConnectActivity.this, "messageType: " + messageType, Toast.LENGTH_SHORT).show();
				}
 			}
 		}
 	};
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onCreate(android.os.Bundle)
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.progress_dialog);
 
 		// Update the text in the Progress Dialog
 		TextView tv = (TextView) findViewById(R.id.progressDialogText);
 		tv.setText(R.string.connecting);
 
 		ConnectionType type = ConnectionFactory.getConnectionType(this);
 
 		if (type == ConnectionType.BLUETOOTH) {
 			// Show the device list
 			Intent showDeviceList = new Intent(this, DeviceListActivity.class);
 			startActivityForResult(showDeviceList, DEVICE_LIST_RESULT);
 		} else if (type == ConnectionType.WIFI) {
 			WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
 			manager.setWifiEnabled(true);
 
 			// Show popup telling the user to enter the IP address of the tablet to connect to
 			Intent showDeviceList = new Intent(this, WifiConnectActivity.class);
 			startActivityForResult(showDeviceList, DEVICE_LIST_RESULT);
 		}
 
 		returnIntent = new Intent();
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onDestroy()
 	 */
 	@Override
 	protected void onDestroy() {
 		// Unregister all the receivers we may have registered
 		try {
 			unregisterReceiver(receiver);
 		} catch (IllegalArgumentException e) {
 			// We didn't get far enough to register the receiver
 		}
 
 		try {
 			unregisterReceiver(gameStartReceiver);
 		} catch (IllegalArgumentException e) {
 			// We didn't get far enough to register the receiver
 		}
 		super.onDestroy();
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
 	 */
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		ConnectionClient client = ConnectionClient.getInstance(this);
 
 		if (requestCode == DEVICE_LIST_RESULT && resultCode != RESULT_CANCELED) {
 			// We are coming back from the device list, and it wasn't cancelled, so
 			// grab the MAC address from the result intent, and start connection
 			String address = data.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
 			client.connect(address);
 
 			// Start listening for connection state changes
 			registerReceiver(receiver, new IntentFilter(ConnectionConstants.STATE_CHANGE_INTENT));
 		} else if (requestCode == GET_PLAYER_NAME && resultCode == RESULT_OK) {
 			String playerName = data.getStringExtra(PLAYER_NAME);
 			returnIntent.putExtra(PLAYER_NAME, playerName);
 			JSONObject obj = new JSONObject();
 			try {
 				obj.put(PLAYER_NAME, playerName);
 				client.write(GET_PLAYER_NAME, obj);
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 		} else {
 			// The user cancelled out of the device list, so return them to the main menu
 			setResult(RESULT_CANCELED, returnIntent);
 			finish();
 		}
 	}
 }
