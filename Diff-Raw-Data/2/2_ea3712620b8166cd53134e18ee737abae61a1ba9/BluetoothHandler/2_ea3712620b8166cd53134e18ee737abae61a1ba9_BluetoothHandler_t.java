 package it.chalmers.tendu.network.bluetooth;
 
 import it.chalmers.tendu.defaults.Constants;
 import it.chalmers.tendu.gamemodel.Player;
 import it.chalmers.tendu.network.INetworkHandler;
 import it.chalmers.tendu.network.bluetooth.clicklinkcompete.Connection;
 import it.chalmers.tendu.network.bluetooth.clicklinkcompete.Connection.OnConnectionLostListener;
 import it.chalmers.tendu.network.bluetooth.clicklinkcompete.Connection.OnIncomingConnectionListener;
 import it.chalmers.tendu.network.bluetooth.clicklinkcompete.Connection.OnMaxConnectionsReachedListener;
 import it.chalmers.tendu.network.bluetooth.clicklinkcompete.Connection.OnMessageReceivedListener;
 import it.chalmers.tendu.tbd.C;
 import it.chalmers.tendu.tbd.C.Msg;
 import it.chalmers.tendu.tbd.C.Tag;
 import it.chalmers.tendu.tbd.EventBus;
 import it.chalmers.tendu.tbd.EventMessage;
 import it.chalmers.tendu.tbd.Listener;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import android.app.AlertDialog.Builder;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.WindowManager.BadTokenException;
 import android.widget.Toast;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.backends.android.AndroidApplication;
 
 public class BluetoothHandler implements INetworkHandler, Listener {
 	private boolean D = true; // Debug flag
 	private String TAG = "BluetoothHandler"; // Logging tag
 
 	/** Identifying Variables */
 	public static final int REQUEST_ENABLE_BT = 666;
 	private static final int MAX_NUMBER_OF_PLAYERS = 3; // Not including host
 	private static final int CONNECTION_DELAY = 5000;
 
 	// Handles the bluetooth connections
 	private Connection connection;
 	/** Context in which the handler was declared */
 	private Context context;
 	/** Connection to android bluetooth hardware */
 	private BluetoothAdapter mBluetoothAdapter;
 	/** All devices that has been discovered */
 	private Set<BluetoothDevice> availableDevices;
 	/** Connected devices */
 	private Set<BluetoothDevice> connectedDevices;
 
 	/**
 	 * Using the context provided by the class declaring this object, initiates
 	 * all parameters needed to establish both a connection to a running
 	 * bluetooth server and acting as a server itself.
 	 * 
 	 * @param <code>Context</code> in which the handler was declared
 	 */
 
 	public BluetoothHandler(Context context) {
 		this.context = context;
 
 		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
 		if (!mBluetoothAdapter.isEnabled()) {
 			enableBluetooth();
 		}
 
 		connection = new Connection(this.context);
 		availableDevices = new HashSet<BluetoothDevice>();
 		connectedDevices = new HashSet<BluetoothDevice>();
 		registerBroadcastReceiver();
 
 		// Register as listener on the eventbus
 		EventBus.INSTANCE.addListener(this);
 
 	}
 
 	private OnMessageReceivedListener dataReceivedListener = new OnMessageReceivedListener() {
 		public void OnMessageReceived(BluetoothDevice device,
 				final EventMessage message) {
 			Log.d(TAG, "Received Message: " + message + " From device: "
 					+ device);
 			// For testing
 			// OnMessageReceived is called from a network thread.
 			// Has to be added to the UI-threads message queue in order to be
 			// displayed.
 			((AndroidApplication) context).runOnUiThread(new Runnable() {
 				public void run() {
 					Toast toast = Toast.makeText(context, message.toString(),
 							Toast.LENGTH_SHORT);
 					toast.setGravity(Gravity.TOP | Gravity.LEFT, 0, 0);
 					toast.show();
 				}
 			});
 
 			sendToEventBus(message);
 		}
 	};
 
 	private OnMaxConnectionsReachedListener maxConnectionsListener = new OnMaxConnectionsReachedListener() {
 		public void OnMaxConnectionsReached() {
 			Log.d(TAG, "Max connections reached");
 
 			// Send on a list of all connected devices mac addresses
 			List<String> addresses = new ArrayList<String>();
 			for (BluetoothDevice device : connectedDevices) {
 				addresses.add(device.getAddress());
 			}
 			broadcastPlayersReadyMessage(addresses);
 		}
 	};
 	private OnIncomingConnectionListener connectedListener = new OnIncomingConnectionListener() {
 		public void OnIncomingConnection(final BluetoothDevice device) {
 			Log.d(TAG, "Incoming connection: " + device.getName());
 
 			((AndroidApplication) context).runOnUiThread(new Runnable() {
 				public void run() {
 					Toast.makeText(context,
 							"Connected to: " + device.getName(),
 							Toast.LENGTH_SHORT).show();
 				}
 			});
 			connectedDevices.add(device);
 			sendToEventBus(new EventMessage(C.Tag.CLIENT_REQUESTED,
 					C.Msg.PLAYER_CONNECTED, device.getAddress()));
 		}
 	};
 
 	private OnConnectionLostListener disconnectedListener = new OnConnectionLostListener() {
 		public void OnConnectionLost(BluetoothDevice device) {
 			Log.d(TAG, "Connection lost: " + device);
 			// Show a dialogue notifying user it got disconnected
 			class displayConnectionLostAlert implements Runnable {
 				public void run() {
 					Builder connectionLostAlert = new Builder(context);
 
 					connectionLostAlert.setTitle("Connection lost");
 					connectionLostAlert
 							.setMessage("Your connection with the other players has been lost.");
 
 					connectionLostAlert.setPositiveButton("Ok",
 							new OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int which) {
 									// TODO Let app terminate itself?
 									// finish();
 								}
 							});
 					connectionLostAlert.setCancelable(false);
 					try {
 						connectionLostAlert.show();
 					} catch (BadTokenException e) {
 						Log.e(TAG, "BadTokenException", e);
 					}
 				}
 			}
 
 			connectedDevices.remove(device);
 			if (connectedDevices.isEmpty()) {
 				// If all devices are disconnected we notify and reset the network
 				// otherwise we just broadcast that a player is gone
 				
 				// Display on UI-thread
 				((AndroidApplication) context)
 				.runOnUiThread(new displayConnectionLostAlert());
 
 				resetNetwork();
 				EventBus.INSTANCE.broadcast(new EventMessage(Tag.NETWORK_NOTIFICATION, Msg.CONNECTION_LOST));
 			} else {
 				EventBus.INSTANCE.broadcast(new EventMessage(Tag.NETWORK_NOTIFICATION, Msg.PLAYER_DISCONNECTED, device.getAddress()));				
 			}
 		}
 	};
 
 	/**
 	 * Called when a device wants to host a game session. Adds the server suffix
 	 * to the bluetooth name and starts the server
 	 * 
 	 */
 	public void hostSession() {
 		addTenduToDeviceName(true);
 		connection.startServer(MAX_NUMBER_OF_PLAYERS, connectedListener,
 				maxConnectionsListener, dataReceivedListener,
 				disconnectedListener);
 	}
 
 	/**
 	 * Called if a client wishes to find and join an established game session.
 	 * 1. Initiates a scan for all reachable bluetooth devices 2. Looks for
 	 * device running a server within the list of devices close-by 3. Attempts
 	 * to establish a connection between this device and found server device
 	 */
 	public void joinGame() {
 		((AndroidApplication) context).runOnUiThread(new Runnable() {
 			public void run() {
 				Toast.makeText(context, "Joining Game", Toast.LENGTH_SHORT)
 						.show();
 			}
 		});
 		if (D)
 			Log.d(TAG, "joinGame() called");
 		this.mBluetoothAdapter.startDiscovery();
 
 		// Wait awhile for the handset to discover units
 		mHandler.postDelayed(new Runnable() {
 
 			@Override
 			public void run() {
 				BluetoothDevice device = findAvailableServerDevice();
 				if (device != null) {
 					Log.d(TAG, "Will now try and connect to: " + device.getName());
 					connection.connect(device, dataReceivedListener,
 							disconnectedListener);
 				} else {
 					Log.d(TAG, "No device to connect to");
 				}
 			}
 		}, CONNECTION_DELAY);
 	}
 
 	// **************************** HELPER METHODS *************************
 
 	/**
 	 * Checks if bluetooth is enabled. If <code>true</code> does nothing. If
 	 * <code>false</code> prompts the user to enable bluetooth.
 	 */
 	private void enableBluetooth() {
 		Intent enableBtIntent;
 		if (!mBluetoothAdapter.isEnabled()) {
 			enableBtIntent = new Intent(
 					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
 			((AndroidApplication) context).startActivityForResult(
 					enableBtIntent, REQUEST_ENABLE_BT); // context is wrong?
 		}
 	}
 
 	/**
 	 * Adds a the name "TenduS" as a suffix to this device name. This is needed
 	 * as identification
 	 * 
 	 * If the device has no name, it is set to "Name"
 	 * 
 	 * @param server
 	 *            if this device is a server device or not
 	 */
 	private void addTenduToDeviceName(final boolean isServer) {
 		if (mBluetoothAdapter.getName() == null) {
 			mBluetoothAdapter.setName("Name");
 		}
 		
 //		String newName = "No rename occured";
 //		String oldName = mBluetoothAdapter.getName();
 		
 //		if (isServer && !oldName.contains(Constants.SERVER_NAME)) {
 //			newName = oldName + Constants.SERVER_NAME;
 //			mBluetoothAdapter.setName(newName);
 //			while (!mBluetoothAdapter.getName().equals(newName)) {
 //				// Loop while name changes
 //			}
 //		}
 		
 		// Multitestversion
 		removeTenduFromDeviceName();
 		String newName = "No rename occured";
 		String oldName = mBluetoothAdapter.getName();
 		newName = oldName + Constants.SERVER_NAME + hostNumber;
 		mBluetoothAdapter.setName(newName);
 		while (!mBluetoothAdapter.getName().equals(newName)) {
 			// Loop while name changes
 		}
 	}
 
 	/**
 	 * Removes the "TenduS" suffix from the bluetooth name
 	 */
 	private void removeTenduFromDeviceName() {
 		String oldName = mBluetoothAdapter.getName();
 		String newName = new String(oldName);
 
 		if (oldName.contains(Constants.SERVER_NAME + '1')) {
 			newName = oldName.replace(Constants.SERVER_NAME + '1', "");
 			mBluetoothAdapter.setName(newName);
 			while (!mBluetoothAdapter.getName().equals(newName)) {
 				// Loop while name changes
 			}
 		} else if (oldName.contains(Constants.SERVER_NAME + '2')) {
 			newName = oldName.replace(Constants.SERVER_NAME + '2', "");
 			mBluetoothAdapter.setName(newName);
 			while (!mBluetoothAdapter.getName().equals(newName)) {
 				// Loop while name changes
 			}
 		}
 	}
 
 	/**
 	 * Checks if device is a valid server by looking for the proper name suffix
 	 * 
 	 * @param device
 	 *            The device that will be checked
 	 * @return <code>true</code> if valid, <code>false</code> if not
 	 */
 	private boolean isDeviceValidServer(BluetoothDevice device) {
 		if (device == null)
 			return false;
 		if (device.getName() == null)
 			return false;
 		String deviceName = device.getName(); 
 		return deviceName.contains(Constants.SERVER_NAME + hostNumber);
 	}
 
 	private void registerBroadcastReceiver() {
 		// Register the BroadcastReceiver
 		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
 		context.registerReceiver(mReceiver, filter);
 		// Don't forget to
 		// unregister during
 		// onDestroy
 	}
 
 	// Create a BroadcastReceiver for ACTION_FOUND
 	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
 		public void onReceive(Context context, Intent intent) {
 			String action = intent.getAction();
 			// When discovery finds a device
 			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
 				// Get the BluetoothDevice object from the Intent
 				BluetoothDevice device = intent
 						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
 				if (D)
 					Log.v(TAG, "Device found: " + device.getName() + "Adress: "
 							+ device.getAddress());
 				// Add the device to a list
 				availableDevices.add(device);
 			}
 		}
 	};
 
 	// Temporary test method
 	/**
 	 * Goes through all available nearby devices and looks valid server devices
 	 * 
 	 * @return a valid device <code>null</code> if no available device was found
 	 */
 	private BluetoothDevice findAvailableServerDevice() {
 		// Return the first eligible device among the available devices set
 		Iterator<BluetoothDevice> iter = availableDevices.iterator();
 		while (iter.hasNext()) {
 			BluetoothDevice device = iter.next();
 			if (isDeviceValidServer(device)) {
 				return device;
 			}
 		}
 		Log.d(TAG, "No eligible Servers found");
 		return null;
 	}
 
 	/**
 	 * Quits the app. Removes the suffix from the bluetooth device name,
 	 * unregisters the receiver if the receiver exists and calls the shutdown
 	 * method
 	 */
 	@Override
 	public void destroy() {
 		Log.d(TAG, "++++++ON DESTROY++++");
 		resetNetwork();
 		
 		if (mReceiver != null) {
 			context.unregisterReceiver(mReceiver);
 		}
 		
 	}
 	
 	@Override
 	public void resetNetwork() {
 		hostNumber = 1;
 		removeTenduFromDeviceName();
 		connection.reset();
 	}
 	
 	@Override
 	public void stopAcceptingConnections() {
 		connection.stopAcceptingConnections();
 	}
 	
 	// Test Method
 	public void testStuff() {
 		connection.broadcastMessage(new EventMessage(C.Tag.TEST, C.Msg.TEST));
 	}
 
 	// Message handler - not used atmo
	private final static Handler mHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			// Do nothing for now
 		}
 	};
 
 	/**
 	 * @return the connectedDevices
 	 */
 	public Set<BluetoothDevice> getConnectedDevices() {
 		return connectedDevices;
 	}
 
 	@Override
 	public String getMacAddress() {
 		return connection.getAddress();
 	}
 
 	
 	@Override
 	public void onBroadcast(final EventMessage message) {
 		if(message.tag == C.Tag.COMMAND_AS_HOST) {
 			EventMessage changedMessage = new EventMessage(message, C.Tag.HOST_COMMANDED);
 			broadcastMessageOverNetwork(changedMessage);
 		} else if(message.tag == C.Tag.REQUEST_AS_CLIENT) {
 			EventMessage changedMessage = new EventMessage(message, C.Tag.CLIENT_REQUESTED);
 			broadcastMessageOverNetwork(changedMessage);
 			Gdx.app.log("BLATANDSMONGOT", "FUNKAR NO?");
 		}
 	}
 
 	/** Broadcast a message on the event bus */
 	private void sendToEventBus(final EventMessage message) {
 		Gdx.app.log(TAG, "recieved messag in host: " + Player.getInstance().isHost());
 		Gdx.app.postRunnable(new Runnable() {
 
 			@Override
 			public void run() {
 				EventBus.INSTANCE.broadcast(message);
 			}
 		});
 	}
 
 	/** Send the mac-addresses of all connected units to the main controller */
 	private void broadcastPlayersReadyMessage(final List<String> addresses) {
 		final EventMessage message = new EventMessage(C.Tag.COMMAND_AS_HOST,
 				C.Msg.ALL_PLAYERS_CONNECTED, addresses);
 		sendToEventBus(message);
 	}
 
 	/**
 	 * Broadcast a message over the network. If you're a client it goes to the
 	 * server, if you're a server it goes out to all clients
 	 */
 	@Override
 	public void broadcastMessageOverNetwork(EventMessage message) {
 		connection.broadcastMessage(message);
 	}
 
 	public void sendMessageToPlayer(BluetoothDevice device, EventMessage message) {
 		connection.sendMessage(device, message);
 	}
 
 	@Override
 	public void unregister() {
 		EventBus.INSTANCE.removeListener(this);
 	}
 
 	
 	private int hostNumber = 1;
 	@Override
 	public int toggleHostNumber() {
 		if (hostNumber == 1) {
 			hostNumber = 2;
 		} else {
 			hostNumber = 1;
 		}
 		
 		((AndroidApplication) context).runOnUiThread(new Runnable() {
 			public void run() {
 				Toast.makeText(context, "Host: " + hostNumber, Toast.LENGTH_SHORT)
 						.show();
 			}
 		});
 		
 		return hostNumber;
 	}
 }
