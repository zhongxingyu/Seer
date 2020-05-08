 package it.chalmers.tendu.network.bluetooth;
 
 import it.chalmers.tendu.gamemodel.GameStateBundle;
 import it.chalmers.tendu.network.INetworkHandler;
 import it.chalmers.tendu.network.clicklinkcompete.Connection;
 import it.chalmers.tendu.network.clicklinkcompete.Connection.OnConnectionLostListener;
 import it.chalmers.tendu.network.clicklinkcompete.Connection.OnConnectionServiceReadyListener;
 import it.chalmers.tendu.network.clicklinkcompete.Connection.OnIncomingConnectionListener;
 import it.chalmers.tendu.network.clicklinkcompete.Connection.OnMaxConnectionsReachedListener;
 import it.chalmers.tendu.network.clicklinkcompete.Connection.OnMessageReceivedListener;
 
 import java.io.Serializable;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 
 
 import android.app.AlertDialog.Builder;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.DialogInterface.OnClickListener;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.Display;
 import android.view.WindowManager;
 import android.view.WindowManager.BadTokenException;
 import android.widget.Toast;
 
 import com.badlogic.gdx.backends.android.AndroidApplication;
 
 public class BluetoothHandler implements INetworkHandler {
 	private boolean D = true; // Debug flag
 	private String TAG = "BluetoothHandler";
 
 	/** Identifying Variables */
 	public static final int REQUEST_ENABLE_BT = 666;
 	private static final String APP_NAME = "Tendu";
 	private static final int MAX_NUMBER_OF_PLAYERS = 2;
 	
 	//BluetoothGameService bgs;
 
 	Connection connection;
 	/** Context in which the handler was declared */
 	private Context context;
 	/** Connection to android bluetooth hardware */
 	private BluetoothAdapter mBluetoothAdapter;
 	/** All devices that has been discovered */
 	private Set<BluetoothDevice> devicesSet;
 	/** Connected devices */
 	private Set<BluetoothDevice> connectedDevices;
 
 	// Game state on server
 	private GameStateBundle gameState; 
 	private GameStateBundle gameStateTest = new GameStateBundle(5, "MeegaTest");
 
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
 
 		connection = new Connection(this.context, serviceReadyListener);
 		//bgs = new BluetoothGameService(context, mHandler);
 		devicesSet = new HashSet<BluetoothDevice>();
 		registerBroadcastReceiver();
 		
 		addTenduToDeviceName(false);
 	}
 
 	 private OnMessageReceivedListener dataReceivedListener = new OnMessageReceivedListener() {
 	        public void OnMessageReceived(BluetoothDevice device, String message) {
 	        	Log.d(TAG, "Received Message: " + message + " From device: " + device);
 //	            if (message.indexOf("SCORE") == 0) {
 //	                String[] scoreMessageSplit = message.split(":");
 //	                hostScore = Integer.parseInt(scoreMessageSplit[1]);
 //	                clientScore = Integer.parseInt(scoreMessageSplit[2]);
 //	                showScore();
 //	            } else {
 //	                mBall.restoreState(message);
 //	            }
 	        }
 	    };
 
 	    private OnMaxConnectionsReachedListener maxConnectionsListener = new OnMaxConnectionsReachedListener() {
 	        public void OnMaxConnectionsReached() {
 	        	Log.d(TAG, "Max connections reached");
 	        }
 	    };
 
 	    private OnIncomingConnectionListener connectedListener = new OnIncomingConnectionListener() {
 	        public void OnIncomingConnection(BluetoothDevice device) {
 	        	Log.d(TAG,"Incoming connection: " + device);
 	        	
 	        	
 //	            rivalDevice = device;
 //	            WindowManager w = getWindowManager();
 //	            Display d = w.getDefaultDisplay();
 //	            int width = d.getWidth();
 //	            int height = d.getHeight();
 //	            mBall = new Demo_Ball(true, width, height - 60);
 //	            mBall.putOnScreen(width / 2, (height / 2 + (int) (height * .05)), 0, 0, 0, 0, 0);
 	        }
 	    };
 
 	    private OnConnectionLostListener disconnectedListener = new OnConnectionLostListener() {
 	        public void OnConnectionLost(BluetoothDevice device) {
 	        	Log.d(TAG,"Connection lost: " + device);
 //	            class displayConnectionLostAlert implements Runnable {
 //	                public void run() {
 //	                    Builder connectionLostAlert = new Builder(self);
 //
 //	                    connectionLostAlert.setTitle("Connection lost");
 //	                    connectionLostAlert
 //	                            .setMessage("Your connection with the other player has been lost.");
 //
 //	                    connectionLostAlert.setPositiveButton("Ok", new OnClickListener() {
 //	                        public void onClick(DialogInterface dialog, int which) {
 //	                            finish();
 //	                        }
 //	                    });
 //	                    connectionLostAlert.setCancelable(false);
 //	                    try {
 //	                    connectionLostAlert.show();
 //	                    } catch (BadTokenException e){
 //	                        // Something really bad happened here; 
 //	                        // seems like the Activity itself went away before
 //	                        // the runnable finished.
 //	                        // Bail out gracefully here and do nothing.
 //	                    }
 //	                }
 //	            }
 //	            self.runOnUiThread(new displayConnectionLostAlert());
 	        }
 	    };
 	
 	private OnConnectionServiceReadyListener serviceReadyListener = new OnConnectionServiceReadyListener() {
 		public void OnConnectionServiceReady() {
 			Log.d(TAG,"Connection service ready");
 			//            if (mType == 0) {
 			//                mConnection.startServer(1, connectedListener, maxConnectionsListener,
 			//                        dataReceivedListener, disconnectedListener);
 			//                self.setTitle("Air Hockey: " + mConnection.getName() + "-" + mConnection.getAddress());
 			//            } else {
 			//                WindowManager w = getWindowManager();
 			//                Display d = w.getDefaultDisplay();
 			//                int width = d.getWidth();
 			//                int height = d.getHeight();
 			//                mBall = new Demo_Ball(false, width, height - 60);
 			//                Intent serverListIntent = new Intent(self, ServerListActivity.class);
 			//                startActivityForResult(serverListIntent, SERVER_LIST_RESULT_CODE);
 			//            }
 			//        }
 		}
 	};
 
 
 	@Override
 	public void hostSession() {
 		//beDiscoverable();
 		addTenduToDeviceName(true);
 		connection.startServer(MAX_NUMBER_OF_PLAYERS, connectedListener, maxConnectionsListener, dataReceivedListener, disconnectedListener);
 		//bgs.start();
 
 	}
 
 	@Override
 	public void joinGame() {
 		if (D) Log.d(TAG, "joinGame() called");
 		this.mBluetoothAdapter.startDiscovery();
 
 		// Wait awhile for the handset to discover units 
 		mHandler.postDelayed(new Runnable() {
 
 			@Override
 			public void run() {
 				BluetoothDevice bd = findAvailableServerDevice();
 				if (bd != null) {
 					Log.d(TAG, "Will now try and connect to: " + bd.getName());
 					connection.connect(bd, dataReceivedListener, disconnectedListener);
 					//bgs.connect(bd, true);
 				} else {
 					Log.d(TAG, "No device to connect to");
 				}	
 			}
 
 		}, 5000);
 		
 	}
 
 	/**
 	 * Goes through the list of discovered devices and checks if they are valid
 	 * "Tendu" players. Then adds these to a list of "team members".
 	 * 
 	 * @return list of team members
 	 * @see {@link devicesList}, {@link isDeviceValid}
 	 */
 	public Set<BluetoothDevice> searchTeam() {
 
 		Set<BluetoothDevice> devices = new HashSet<BluetoothDevice>();
 		for (BluetoothDevice d : devicesSet) { // bgs.getDevicesList()){
 			if (isDeviceValid(d)) {
 				devices.add(d);
 			}
 		}
 		return devices;
 	}
 
 	// ----------------------- HELP METHODS ------------------------
 
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
 					enableBtIntent, REQUEST_ENABLE_BT); // context is wrong
 		}
 	}
 
 	/**
 	 * Adds a the name "Tendu" as a suffix to this device name. This is needed
 	 * as identification
 	 * 
 	 * If the device has no name, it is set to "Tendu"
 	 * @param server if this device is a server device or not
 	 */
 	private void addTenduToDeviceName(boolean isServer) {
 		if (mBluetoothAdapter.getName() == null)
 			mBluetoothAdapter.setName(APP_NAME + "");
 		else {
 			String name = mBluetoothAdapter.getName();
 			if (!name.contains(APP_NAME)) {
 				if(mBluetoothAdapter.setName(name + " - " + APP_NAME)) Log.d(TAG, "Device name changed succesfully to: " + mBluetoothAdapter.getName());
 				else Log.d(TAG, "Device namechange failed: " + mBluetoothAdapter.getName());
 			}
 		}
 		if(isServer && !mBluetoothAdapter.getName().contains(APP_NAME + "S")){
 			mBluetoothAdapter.setName(mBluetoothAdapter.getName() + "S");
 		}
 	}
 
 	private void removeTenduFromDeviceName() {
 		if (mBluetoothAdapter.getName().contains(APP_NAME)) {
 			String name = mBluetoothAdapter.getName();
 			String newName = name.replace(" - " + APP_NAME, "");
 			mBluetoothAdapter.setName(newName);
 		}
 	}
 
 	/**
 	 * Checks if the given device is using "Tendu", rather than just having
 	 * Bluetooth enabled
 	 * 
 	 * @param remote
 	 *            {@link BluetoothDevice} to validate
 	 * @return <code>true</code> if valid <code>false</code> if non-valid
 	 */
 	private boolean isDeviceValid(BluetoothDevice device) {
 		if (device == null)
 			return false;
 		if (device.getName() == null)
 			return false;
 		return device.getName().contains(APP_NAME);
 	}
 	
 	private boolean isDeviceValidServer(BluetoothDevice device) {
 		if (device == null)
 			return false;
 		if (device.getName() == null)
 			return false;
		return device.getName().contains(APP_NAME + "S");
 	}
 
 	private void registerBroadcastReceiver() {
 		// Register the BroadcastReceiver
 		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
 		context.registerReceiver(mReceiver, filter); // Don't forget to
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
 				devicesSet.add(device);
 
 			}
 		}
 	};
 
 	// Temporary test method
 	private BluetoothDevice findAvailableServerDevice() {
 		//		// First look among the paired devices
 		//		Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
 		//		for (BluetoothDevice device: devices) {
 		//			if (isDeviceValid(device)) {
 		//				return device;
 		//			}
 		//		}
 		//		// Then among the ones that have been discovered
 
 		// Return the first eligible device among the available devices set
 		Iterator<BluetoothDevice> iter = devicesSet.iterator();
 		while (iter.hasNext()) {
 			BluetoothDevice device = iter.next(); 
 			if (isDeviceValidServer(device)) {
 				return device;
 			}
 		}
 		Log.d(TAG, "No eligible Servers found");
 		return null;
 	}
 	
 	private BluetoothDevice findFirstAvailableDevices() {
 		//		// First look among the paired devices
 		//		Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
 		//		for (BluetoothDevice device: devices) {
 		//			if (isDeviceValid(device)) {
 		//				return device;
 		//			}
 		//		}
 		//		// Then among the ones that have been discovered
 
 		// Return the first eligible device among the available devices set
 		Iterator<BluetoothDevice> iter = devicesSet.iterator();
 		while (iter.hasNext()) {
 			BluetoothDevice device = iter.next(); 
 			if (isDeviceValid(device)) {
 				return device;
 			}
 		}
 		Log.d(TAG, "No eligible devices found");
 		return null;
 	}
 
 	private void beDiscoverable() {
 		Intent discoverableIntent = new Intent(
 				BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
 		discoverableIntent.putExtra(
 				BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
 		context.startActivity(discoverableIntent);
 	}
 
 	//	public void sendPing() {
 	//		String testString = "Super communication skills";
 	//		bgs.write(testString.getBytes());
 	//	}
 
 	@Override
 	public void sendObject(Serializable o) {
 		//bgs.kryoWrite(o);
 
 	}
 
 	@Override
 	public void destroy() {
 		Log.d(TAG, "++++++ON DESTROY++++");
 		removeTenduFromDeviceName();
 		context.unregisterReceiver(mReceiver);
 		//bgs.stop();	
 
 	}
 
 	@Override
 	public void testStuff() {
 		testSendGameState(gameStateTest);
 	}
 
 	//@Override
 	public void testSendGameState(GameStateBundle state) {
 		sendObject(state);
 	}
 
 	// Message handler
 	private final Handler mHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			if (msg.what == BluetoothGameService.MESSAGE_READ) {
 				if (msg.obj instanceof GameStateBundle) {
 					gameState = (GameStateBundle) msg.obj;
 
 					// Ping Test
 					GameStateBundle newGameStateBundle = gameStateTest;
 					String s;
 					s = newGameStateBundle.equals(msg.obj)? "Success":"Failure";
 					Toast.makeText(context, s, Toast.LENGTH_LONG).show();
 				}
 			}
 
 		}
 	};
 
 	@Override
 	public GameStateBundle pollGameState() {
 		return gameState;
 	}
 
 	@Override
 	public int pollNetworkState() {
 		return -1;
 		//return bgs.getState();
 	}
 
 	/**
 	 * @return the connectedDevices
 	 */
 	public Set<BluetoothDevice> getConnectedDevices() {
 		return connectedDevices;
 	}
 
 	/**
 	 * @param connectedDevices the connectedDevices to set
 	 */
 	public void setConnectedDevices(Set<BluetoothDevice> connectedDevices) {
 		this.connectedDevices = connectedDevices;
 	}
 }
