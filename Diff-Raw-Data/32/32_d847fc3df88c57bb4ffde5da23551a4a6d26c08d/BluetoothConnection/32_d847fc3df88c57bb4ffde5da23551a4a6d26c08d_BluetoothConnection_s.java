 /*
 * Copyright 2012 Anders Eie, Henrik Goldsack, Johan Jansen, Asbj�rn 
 * Lucassen, Emanuele Di Santo, Jonas Svarvaa, Bj�rnar H�kenstad Wold
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
 
 package no.group09.connection;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import android.app.Activity;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothSocket;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
import android.content.IntentFilter;
 import android.util.Log;
 
 /**
  * A class for any BluetoothConnection on Android. This class offers easy and useful services
  * giving a simple interface to the developer to establish a Bluetooth connection and
  * send or receive data without needing to know any low-level details. Simply create new instance
  * of this class with the remote device address and use connect() to establish the connection. 
  * This class will automatically create connection and communication threads to handle everything.
  */
 public class BluetoothConnection extends Protocol {
 
 	/** Unique requestResult ID when using startActivityForResult in the parentActivity to enable the Bluetooth Adapter*/
 	public static int REQUEST_ENABLE_BT = 374370074;
 	
 	private static final String TAG = "BT_CONNECTION";
 
 	/** The Activity that created this instance of BluetoothConnection (others could still be using this instance) */
 	private Activity parentActivity;
 
 	/** We notify this listener on any connection state changes */
 	private ConnectionListener connectionListener;
 
 	protected BufferedInputStream input;
 	protected BufferedOutputStream output;
 
 	protected BluetoothDevice device;
 	protected BluetoothSocket socket;
 	protected BluetoothAdapter bluetooth;	
 
 	private volatile ConnectionState connectionState;
 	private boolean connectionRequested = false;
 
 	/**
 	 * An enumeration describing the different connection states a BluetoothConnection can be
 	 */
 	public enum ConnectionState {
 		/** Initial state. No connection has been established. */
 		STATE_DISCONNECTED,
 
 		/** The device is trying to establish a connection. */
 		STATE_CONNECTING,
 
 		/** A valid open connection is established to the remote device. */
 		STATE_CONNECTED
 	}
 	
 	/**
 	 * Default constructor for creating a new BluetoothConnection to a remote device.
 	 * @param address The Bluetooth MAC address of the remote device
 	 * @param listener Listener that is called when changes is made on the connection
 	 * @throws ComLibException is thrown if the Android device does not support Bluetooth
 	 * @throws IllegalArgumentException is thrown if the specified address/remote device is invalid or if ConnectionListener is null
 	 */
 	public BluetoothConnection(String address, ConnectionListener listener) {
 		//Validate the address
 		if( !BluetoothAdapter.checkBluetoothAddress(address) ){
 			throw new IllegalArgumentException("The specified bluetooth address is not valid");
 		}
 
 		//Make sure there is a valid listener
 		if(listener == null){
 			throw new IllegalArgumentException("ConnectionListener cannot be null in BluetoothConnection constructor");
 		}
 
 		//Make sure this device has bluetooth
 		bluetooth = BluetoothAdapter.getDefaultAdapter();
 		if( bluetooth == null ){
 			Log.d(TAG, "No bluetooth hardware found");
 		}		
 
 		this.connectionListener = listener;
 		connectionState = ConnectionState.STATE_DISCONNECTED;
 		device = bluetooth.getRemoteDevice(address);
 
 		//Ensure that we disconnect and free resources on exit
 		Runtime.getRuntime().addShutdownHook(new Thread(){
 			@Override
 			public void run(){
 				disconnect();
 			}
 		});
 	}
 
 	/**
 	 * Ensures that we have a valid bluetooth socket
 	 */
 	private boolean validateSocket() {
 		//Already created a valid socket?
 		if(socket != null) return true;
 
 		//Bluetooth must be enabled to create a socket
 		if(bluetooth.getState() != BluetoothAdapter.STATE_ON) return false;
 
 		//Create a socket through a hidden method (normal method does not work on all devices like Samsung Galaxy SII)
 		try {
 			Method m  = device.getClass().getMethod("createRfcommSocket", new Class[] { int.class });
 			if(m == null) Log.d(TAG, "method is null!");
 			socket = (BluetoothSocket) m.invoke(device, Integer.valueOf(1));
 		}
 		catch(InvocationTargetException ex){
 			Log.d(TAG, getClass().getSimpleName()+ " Unable to create socket: " + ex.getTargetException());
 			return false;
 		}
 		catch (Exception ex){
 			Log.d(TAG, getClass().getSimpleName() + " Unable to create socket: " + ex);
 			return false;
 		}
 
 		//Get input and output streams
 		try {
 	    	output = new BufferedOutputStream(socket.getOutputStream());
 	    	input = new BufferedInputStream(socket.getInputStream());	
 		} catch (IOException ex) {
 			Log.d(TAG, getClass().getSimpleName() + " Unable to get input/output stream: " + ex.getMessage());
 			return false;
 		}	
 
 		return true;
 	}
 
 	/**
 	 * Changes the connection state of this BluetoothConnection. Package visible.
 	 * @param setState the new ConnectionState of this BluetoothConnection
 	 */
 	public void setConnectionState(ConnectionState setState) {
 		//Nothing to change?
 		if(connectionState == setState) return;
 
 		//Change the state
 		connectionState = setState;
 
 		//Tell listener about any connection changes
 		switch(setState) {
 			case STATE_CONNECTED:
 				connectionListener.onConnect(this);
 				break;
 
 			case STATE_DISCONNECTED:
 				connectionListener.onDisconnect(this);
 				break;
 
 			case STATE_CONNECTING:
 				connectionListener.onConnecting(this);
 				break;
 
 			default:
 				//dont notify listeners
 				break;
 		}
 
 	}
 
 	/**	
      * Establishes a connection to the remote device. Note that this function is asynchronous and returns
      * immediately after starting a new connection thread. Use isConnected() or getConnectionState() to
      * check when the connection has been established. disconnect() can be called to stop trying to get an 
      * active connection (STATE_CONNECTING to STATE_DISCONNECTED)
      */
 	@Override
 	public void connect() {
 
 		//Don't try to connect more than once
 		if( connectionState != ConnectionState.STATE_DISCONNECTED ) {
 			Log.d(TAG, "Trying to connect to the same device twice!");
 			return;
 		}
     	
 //		//Register broadcast receivers
 //		parentActivity.registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
 //		parentActivity.registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));    	
 //
 //		//Make sure bluetooth is enabled
 //		if( !bluetooth.isEnabled() ) {
 //			//wait until Bluetooth is enabled by the OS
 //			Log.d(TAG, "BluetoothDevice is DISABLED. Asking user to enable Bluetooth");
 //			parentActivity.startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT);
 //			connectionRequested = true;
 //			return;
 //		}
 
 		//Start an asynchronous connection and return immediately so we do not interrupt program flow
 		if(validateSocket()) new ConnectionThread(this).start();
 	}
 
 	/**
 	 * Get the Bluetooth MAC address of the remote device
 	 * @return a String representation of the MAC address. For example: "00:10:06:29:00:48"
 	 */
 	public String getAddress() {
 		return device.getAddress();
 	}
 
 	@Override
 	public String toString() {
 		return device.getName();
 	}
 
 	/**
 	 * Returns the current connection state of this BluetoothConnection to the remote device
 	 * @return STATE_CONNECTED, STATE_CONNECTING or STATE_DISCONNECTED
 	 */
 	public ConnectionState getConnectionState() {
 		return connectionState;
 	}
 
 	/**
 	 * Returns true if there is an active open and valid bluetooth connection to the
 	 * remote device. Same as calling getConnectionState() == ConnectionState.STATE_CONNECTED
 	 * @return true if there is a connection, false otherwise
 	 */
 	public boolean isConnected() {
 		return connectionState == ConnectionState.STATE_CONNECTED;
 	}
 
 	/**
 	 * Checks if this BluetoothConnection is paired or not
 	 * @return true if we are already paired (false otherwise)
 	 */
 	public boolean isPaired() {
 		return bluetooth.getBondedDevices().contains(device);
 	}
 
 	/**
 	 * Disconnects the remote device. connect() has to be called before any communication to the
 	 * remote device can be done again.
 	 */
 	@Override
 	public void disconnect() {
 		if(connectionState != ConnectionState.STATE_DISCONNECTED) Log.d(TAG, "Bluetooth connection closed: " + device.getAddress());
 
 		setConnectionState(ConnectionState.STATE_DISCONNECTED);
 
 		//Disconnect superclass
 		super.disconnect();
 
 		//Disconnect socket
 		try {
 			if(socket != null) socket.close();
 		} catch (IOException e) {
 			Log.d(TAG, "Failed to close Bluetooth socket: " + e.getMessage() + " : " + getClass().getSimpleName());
 		}
 
 	}
 
 	 // Create a BroadcastReceiver for enabling bluetooth
     private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
         @Override
 		public void onReceive(Context context, Intent intent) {
             String action = intent.getAction();
                                     
             //Device is turning on or off
             if( action.equals(BluetoothAdapter.ACTION_STATE_CHANGED) ) {
             	
             	switch(bluetooth.getState())
             	{
             		//Bluetooth is starting up
             		case BluetoothAdapter.STATE_TURNING_ON:
             			//Don't care
             	    break;
             	    
             	    //Bluetooth is shutting down or disabled
             		case BluetoothAdapter.STATE_TURNING_OFF:
             		case BluetoothAdapter.STATE_OFF:
             			//make sure socket is disconnected when Bluetooth is shutdown
 					    disconnect();
             		break;
             		
             		//Bluetooth is Enabled and ready
             		case BluetoothAdapter.STATE_ON:
             			//automatically connect if we are waiting for a connection
             			if(connectionRequested) {
             				connectionRequested = false;
             				
             				//Start an asynchronous connection and return immediately so we do not interrupt program flow
             				if(validateSocket()) new ConnectionThread(BluetoothConnection.this).start();
             			}
             		break;         		            		
             	}
             	            		
             }
             
             //Discovery mode has finished
             else if( action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED) ) {
     			//automatically connect if we are waiting for a connection
     			if(connectionRequested) {
     				connectionRequested = false;
     				//Start an asynchronous connection and return immediately so we do not interrupt program flow
     				if(validateSocket()) new ConnectionThread(BluetoothConnection.this).start();
     			}            	
             }
             
         }
         
     };
     
 //    public ConnectionState getConnectionState(){
 //    	return connectionState;
 //    }
 
 
 	@Override
 	protected void sendBytes(byte[] data) throws IOException {
 
 		//Make sure we are connected before sending data
 		if( connectionState == ConnectionState.STATE_DISCONNECTED ){
 			Log.d(TAG, "Trying to send data while Bluetooth is not connected!");
 		}
 
 		//Send the data
 		output.write(data);
 		output.flush();
 	}
 
 }
