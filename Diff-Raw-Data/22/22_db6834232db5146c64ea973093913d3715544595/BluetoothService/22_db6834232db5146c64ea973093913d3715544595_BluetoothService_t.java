 package edu.illinois.CS598rhk.services;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.UUID;
 
 import android.app.Service;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothServerSocket;
 import android.bluetooth.BluetoothSocket;
 import android.content.Intent;
 import android.os.Binder;
 import android.os.IBinder;
 import android.util.Log;
 import edu.illinois.CS598rhk.interfaces.IBluetoothService;
 import edu.illinois.CS598rhk.models.BluetoothNeighbor;
 import edu.illinois.CS598rhk.models.Neighbor;
 
 public class BluetoothService extends Service implements IBluetoothService {
	public static final String DISCOVERED_OVER_BLUETOOTH = "Wifi neighbor found over bluetooth";
 	public static final String INTENT_TO_ADD_BLUETOOTH_NEIGHBOR = "add bluetooth neighbor";
 	public static final String BLUETOOTH_NEIGHBOR_DATA = "bluetooth neighbor data";
 	
 	final IBinder mBinder = new BluetoothBinder();
     
     @Override
     public IBinder onBind(Intent arg0) {
         return mBinder;
     }
     
     public class BluetoothBinder extends Binder {
         public IBluetoothService getService() {
             return BluetoothService.this;
         }
     }
     
     private BluetoothNeighbor myContactInfo;
     
     private List<byte[]> messages;
     private Set<BluetoothDevice> neighbors;
     private Iterator<BluetoothDevice> nextNeighbor;
     private BluetoothDevice currentNeighbor;
     private boolean broadcasting;
     
     @Override
     public void onCreate() {
     	super.onCreate();
     	myContactInfo = new BluetoothNeighbor();
     }
     
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
     	// TODO: This dies if Bluetooth is not enabled at this point!
     	
     	// We could use setName() here to allow the user to change the name we use
     	
     	mStateString = "STATE_INIT";
     	
     	myContactInfo.name = BluetoothAdapter.getDefaultAdapter().getName();
     	myContactInfo.address = BluetoothAdapter.getDefaultAdapter().getAddress();
     	myContactInfo.progress = 0;
     	sendToLogger("Bluetooth name: " + myContactInfo.name + " Bluetooth address: " + myContactInfo.address + "\n");
     	
     	broadcasting = false;
     	neighbors = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
     	nextNeighbor = neighbors.iterator();
     	
     	messages = new ArrayList<byte[]>();
     	updateNeighbors();
     	return START_STICKY;
     }
     
     public void updateScheduleProgress(int progress) {
     	synchronized(myContactInfo) {
     		sendToLogger("Bluetooth received progress update from " + myContactInfo.progress + " to " + progress);
     		myContactInfo.progress = progress;
     	}
     }
     
     public void updateNeighbors() {
     	sendToLogger("Bluetooth: Begin updating neighbors\n");
     	broadcast(myContactInfo.getBytes());
     }
     
     public void broadcast(String message) {
     	sendToLogger("Bluetoooth: Received message to broadcast\n\tMessage: " + message + "\n");
     	broadcast(message.getBytes());
     }
     
     private void broadcast(byte[] message) {
     	synchronized(messages) {
 	    	messages.add(message);
 	    	if (!broadcasting) {
 	    		stop();
 				broadcasting = true;
 				processNextMessage();
 			}
 	    	else {
 	    		sendToLogger("Bluetooth: Currently broadcasting message\n\tMessage: " + messages.get(0)
 	    				+ "\n\tQueuing message: " + message + "\n");
 	    	}
     	}
     }
     
     private void processNextMessage() {
     	synchronized(messages) {
 	     	if (nextNeighbor.hasNext()) {
 				currentNeighbor = nextNeighbor.next();
 				sendToLogger("Bluetooth: Attempting to connect to Neighbor:\n\t" + currentNeighbor.getName() 
 						+ " with address " + currentNeighbor.getAddress()
 						+ "\n\tand message: " + messages.get(0) + "\n");
 				connect(currentNeighbor);
 			}
 	    	else {
 	    		messages.remove(0);
 	    		nextNeighbor = neighbors.iterator();
 	    		if (!messages.isEmpty()) {
 	    			processNextMessage();
 	    		}
 	    		else {
 	    			broadcasting = false;
 	    			sendToLogger("Bluetooth: Done Broadcasting\n");
 	    			start();
 	    		}
 	    	}
     	}
     }
     
     private void sendMessageToNeighbor(byte[] message) {
     	synchronized(messages) {
 	    	if (message == null) {
 	    		message = messages.get(0);
 	    	}
 	    	sendToLogger("Bluetooth: Connected to Neighbor:\n\t" + currentNeighbor.getName() 
 	    			+ " with address " + currentNeighbor.getAddress()
 	    			+ "\n\tSending message: " + message + "\n");
 	    	write(message);
     	}
     }
     
     public void newBluetoothNeighbor(byte[] message) {
     	Intent i = new Intent(INTENT_TO_ADD_BLUETOOTH_NEIGHBOR);
     	i.putExtra(BLUETOOTH_NEIGHBOR_DATA, message);
     	sendBroadcast(i);
     	sendToLogger("Bluetooth: Found Bluetooth Nieghbor:\n\t" + message + "\n");
     }
     
     public void newWifiNeighbor(byte[] message) {
     	Intent i = new Intent(WifiService.INTENT_TO_ADD_WIFI_NEIGHBOR);
     	i.putExtra(WifiService.WIFI_NEIGHBOR_DATA, message);
    	i.putExtra(WifiService.INTENT_TO_ADD_WIFI_NEIGHBOR_SOURCE, DISCOVERED_OVER_BLUETOOTH);
     	sendBroadcast(i);
     	sendToLogger("Bluetooth: Found Wifi Nieghbor:\n\t" + message + "\n");
     }
     
 	public void sendToLogger(String message) {
 		Intent intentToLog = new Intent(PowerManagement.ACTION_LOG_UPDATE);
 		intentToLog.putExtra(PowerManagement.LOG_MESSAGE, message);
 		sendBroadcast(intentToLog);
 	}
     
     //
     //
     //
     // Code taken BluetoothChatService API Demo
     //
     //
     //
     
  // Debugging
     private static final String TAG = "BluetoothService";
     private static final boolean D = true;
 
     // Name for the SDP record when creating server socket
     private static final String NAME = "BluetoothNeighborFinder";
 
     // Unique UUID for this application
     private static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
 
     // Member fields
     private final BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
 //    private final Handler mHandler;
     private AcceptThread mAcceptThread;
     private ConnectThread mConnectThread;
     private ConnectedThread mConnectedThread;
     private int mState;
     private String mStateString;
 
     // Constants that indicate the current connection state
     public static final int STATE_NONE = 0;       // we're doing nothing
     public static final int STATE_LISTEN = 1;     // now listening for incoming connections
     public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
     public static final int STATE_CONNECTED = 3;  // now connected to a remote device
     
     /**
      * Constructor. Prepares a new BluetoothChat session.
      * @param context  The UI Activity Context
      * @param handler  A Handler to send messages back to the UI Activity
      */
 //    public BluetoothService(Context context, Handler handler) {
 //        mAdapter = BluetoothAdapter.getDefaultAdapter();
 //        mState = STATE_NONE;
 //        mHandler = handler;
 //    }
 
     /**
      * Set the current state of the chat connection
      * @param state  An integer defining the current connection state
      */
     private synchronized void setState(int state) { 
         String log = "";
         switch(state) {
         case 0:
         	log = "setState() " + mStateString + " -> STATE_NONE";
         	mStateString = "STATE_NONE";
         	break;
         case 1:
         	log = "setState() " + mStateString + " -> STATE_LISTEN";
         	mStateString = "STATE_LISTEN";
         	break;
         case 2:
         	log = "setState() " + mStateString + " -> STATE_CONNECTING";
         	mStateString = "STATE_CONNECTING";
         	break;
         case 3:
         	log = "setState() " + mStateString + " -> STATE_CONNECTED";
         	mStateString = "STATE_CONNECTED";
         	break;
         default:
         	log = "setState() " + mStateString + " -> STATE_UNKNOWN";
         	mStateString = "STATE_UNKNOWN";
         	break;
         }
         
         mState = state;
         
         sendToLogger(log);
         // Give the new state to the Handler so the UI Activity can update
 //        mHandler.obtainMessage(BluetoothChat.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
     }
 
     /**
      * Return the current connection state. */
     public synchronized int getState() {
         return mState;
     }
 
     /**
      * Start the chat service. Specifically start AcceptThread to begin a
      * session in listening (server) mode. Called by the Activity onResume() */
     public synchronized void start() {
         if (D) Log.d(TAG, "start");
 
         // Cancel any thread attempting to make a connection
         if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
 
         // Cancel any thread currently running a connection
         if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
 
         // Start the thread to listen on a BluetoothServerSocket
         if (mAcceptThread == null) {
             mAcceptThread = new AcceptThread();
             mAcceptThread.start();
         }
         setState(STATE_LISTEN);
     }
 
     /**
      * Start the ConnectThread to initiate a connection to a remote device.
      * @param device  The BluetoothDevice to connect
      */
     public synchronized void connect(BluetoothDevice device) {
         if (D) Log.d(TAG, "connect to: " + device);
 
         // Cancel any thread attempting to make a connection
         if (mState == STATE_CONNECTING) {
             if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
         }
 
         // Cancel any thread currently running a connection
         if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
 
         // Start the thread to connect with the given device
         mConnectThread = new ConnectThread(device);
         mConnectThread.start();
         setState(STATE_CONNECTING);
     }
 
     /**
      * Start the ConnectedThread to begin managing a Bluetooth connection
      * @param socket  The BluetoothSocket on which the connection was made
      * @param device  The BluetoothDevice that has been connected
      */
     public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
         if (D) Log.d(TAG, "connected");
 
         // Cancel the thread that completed the connection
         if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
 
         // Cancel any thread currently running a connection
         if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
 
         // Cancel the accept thread because we only want to connect to one device
         if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}
 
         // Start the thread to manage the connection and perform transmissions
         mConnectedThread = new ConnectedThread(socket);
         mConnectedThread.start();
 
         setState(STATE_CONNECTED);
         if (broadcasting) {
         	sendMessageToNeighbor(null);
         }
     }
 
     /**
      * Stop all threads
      */
     public synchronized void stop() {
         if (D) Log.d(TAG, "stop");
         if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
         if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
         if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}
         setState(STATE_NONE);
     }
 
     /**
      * Write to the ConnectedThread in an unsynchronized manner
      * @param out The bytes to write
      * @see ConnectedThread#write(byte[])
      */
     public void write(byte[] out) {
         // Create temporary object
         ConnectedThread r;
         // Synchronize a copy of the ConnectedThread
         synchronized (this) {
             if (mState != STATE_CONNECTED) return;
             r = mConnectedThread;
         }
         // Perform the write unsynchronized
         r.write(out);
     }
 
     /**
      * Indicate that the connection attempt failed and notify the UI Activity.
      */
     private void connectionFailed() {
     	//setState(STATE_LISTEN);
     	setState(STATE_NONE);
         
     	if (broadcasting) {
     		processNextMessage();
     	}
     	else {
     		start();
     	}
     }
 
     /**
      * Indicate that the connection was lost and notify the UI Activity.
      */
     private void connectionLost() {
     	//setState(STATE_LISTEN);
     	setState(STATE_NONE);
         
     	if (broadcasting) {
     		processNextMessage();
     	}
     	else {
     		start();
     	}
     }
 
     /**
      * This thread runs while listening for incoming connections. It behaves
      * like a server-side client. It runs until a connection is accepted
      * (or until cancelled).
      */
     private class AcceptThread extends Thread {
         // The local server socket
         private final BluetoothServerSocket mmServerSocket;
 
         public AcceptThread() {
             BluetoothServerSocket tmp = null;
 
             // Create a new listening server socket
             try {
                 tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
             } catch (IOException e) {
                 Log.e(TAG, "listen() failed", e);
             }
             mmServerSocket = tmp;
         }
 
         public void run() {
             if (D) Log.d(TAG, "BEGIN mAcceptThread" + this);
             setName("AcceptThread");
             BluetoothSocket socket = null;
 
             // Listen to the server socket if we're not connected
             while (mState != STATE_CONNECTED) {
                 try {
                     // This is a blocking call and will only return on a
                     // successful connection or an exception
                     socket = mmServerSocket.accept();
                 } catch (IOException e) {
                     Log.e(TAG, "accept() failed", e);
                     break;
                 }
 
                 // If a connection was accepted
                 if (socket != null) {
                     synchronized (BluetoothService.this) {
                         switch (mState) {
                         case STATE_LISTEN:
                         case STATE_CONNECTING:
                             // Situation normal. Start the connected thread.
                             connected(socket, socket.getRemoteDevice());
                             break;
                         case STATE_NONE:
                         case STATE_CONNECTED:
                             // Either not ready or already connected. Terminate new socket.
                             try {
                                 socket.close();
                             } catch (IOException e) {
                                 Log.e(TAG, "Could not close unwanted socket", e);
                             }
                             break;
                         }
                     }
                 }
             }
             if (D) Log.i(TAG, "END mAcceptThread");
         }
 
         public void cancel() {
             if (D) Log.d(TAG, "cancel " + this);
             try {
                 mmServerSocket.close();
             } catch (IOException e) {
                 Log.e(TAG, "close() of server failed", e);
             }
         }
     }
 
 
     /**
      * This thread runs while attempting to make an outgoing connection
      * with a device. It runs straight through; the connection either
      * succeeds or fails.
      */
     private class ConnectThread extends Thread {
         private final BluetoothSocket mmSocket;
         private final BluetoothDevice mmDevice;
 
         public ConnectThread(BluetoothDevice device) {
             mmDevice = device;
             BluetoothSocket tmp = null;
 
             // Get a BluetoothSocket for a connection with the
             // given BluetoothDevice
             try {
                 tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
             } catch (IOException e) {
                 Log.e(TAG, "create() failed", e);
             }
             mmSocket = tmp;
         }
 
         public void run() {
             Log.i(TAG, "BEGIN mConnectThread");
             setName("ConnectThread");
 
             // Always cancel discovery because it will slow down a connection
             mAdapter.cancelDiscovery();
 
             // Make a connection to the BluetoothSocket
             try {
                 // This is a blocking call and will only return on a
                 // successful connection or an exception
                 mmSocket.connect();
             } catch (IOException e) {
                 connectionFailed();
                 // Close the socket
                 try {
                     mmSocket.close();
                 } catch (IOException e2) {
                     Log.e(TAG, "unable to close() socket during connection failure", e2);
                 }
                 // Start the service over to restart listening mode
                 //BluetoothService.this.start();
                 return;
             }
 
             // Reset the ConnectThread because we're done
             synchronized (BluetoothService.this) {
                 mConnectThread = null;
             }
 
             // Start the connected thread
             connected(mmSocket, mmDevice);
         }
 
         public void cancel() {
             try {
                 mmSocket.close();
             } catch (IOException e) {
                 Log.e(TAG, "close() of connect socket failed", e);
             }
         }
     }
 
     /**
      * This thread runs during a connection with a remote device.
      * It handles all incoming and outgoing transmissions.
      */
     private class ConnectedThread extends Thread {
         private final BluetoothSocket mmSocket;
         private final InputStream mmInStream;
         private final OutputStream mmOutStream;
         
         boolean receivingMessage;
         byte[] message;
         int messageLength;
         int bytesRead;
 
         public ConnectedThread(BluetoothSocket socket) {
             Log.d(TAG, "create ConnectedThread");
             mmSocket = socket;
             InputStream tmpIn = null;
             OutputStream tmpOut = null;
 
             receivingMessage = false;
             message = null;
             messageLength = 4;
             bytesRead = 0;
             
             // Get the BluetoothSocket input and output streams
             try {
                 tmpIn = socket.getInputStream();
                 tmpOut = socket.getOutputStream();
             } catch (IOException e) {
                 Log.e(TAG, "temp sockets not created", e);
             }
 
             mmInStream = tmpIn;
             mmOutStream = tmpOut;
         }
 
         public void run() {
             Log.i(TAG, "BEGIN mConnectedThread");
             byte[] buffer = new byte[1024];
             int bytes;
             
             // Keep listening to the InputStream while connected
             while (true) {
                 try {
                     // Read from the InputStream
                     bytes = mmInStream.read(buffer, bytesRead, messageLength-bytesRead);
 
                     if (bytes > 0) {
                     	bytesRead += bytes;
                     	if (!receivingMessage && bytesRead >= 4) {
                     		receivingMessage = true;
                     		
                     		ByteBuffer bb = ByteBuffer.wrap(buffer,0,4);
                     		messageLength = bb.getInt();
                     		message = new byte[messageLength];
                     		System.arraycopy(buffer, 0, message, 0, bytesRead);
                     	}
                     	else if (receivingMessage && bytesRead == messageLength){
                     		receivingMessage = false;
                     		bytesRead = 0;
                     		messageLength = 4;
                     		
                     		sendToLogger("Bluetooth: Received message\n\t"
                     				+ "Message: " + new String(message)
                     				+ "\n\tfrom Neighbor: " + mmSocket.getRemoteDevice().getName()
                     				+ " with address " + mmSocket.getRemoteDevice().getAddress() + "\n");
                     		
                     		switch(message[Neighbor.INDEX_OF_HEADER]) {
                     		case Neighbor.BLUETOOTH_NEIGHBOR_HEADER:
                     			newBluetoothNeighbor(message);
                     			break;
                     		case Neighbor.WIFI_NEIGHBOR_HEADER:
                     			newWifiNeighbor(message);
                     			break;
                     		}
                     		
                     		if (!broadcasting) {
                     			sendMessageToNeighbor(myContactInfo.getBytes());
                     		}
                     		else {
                     			processNextMessage();
                     		}
                     	}
                     }
                 } catch (IOException e) {
                     Log.e(TAG, "disconnected", e);
                     connectionLost();
                     break;
                 }
             }
         }
 
         /**
          * Write to the connected OutStream.
          * @param buffer  The bytes to write
          */
         public void write(byte[] buffer) {
             try {
                 mmOutStream.write(buffer);
             } catch (IOException e) {
                 Log.e(TAG, "Exception during write", e);
             }
         }
 
         public void cancel() {
             try {
                 mmSocket.close();
             } catch (IOException e) {
                 Log.e(TAG, "close() of connect socket failed", e);
             }
         }
     }
 }
