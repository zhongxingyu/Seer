 package edu.illinois.CS598rhk.services;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.UUID;
 
 import android.app.Notification;
 import android.app.Service;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothServerSocket;
 import android.bluetooth.BluetoothSocket;
 import android.content.Intent;
 import android.os.Binder;
 import android.os.IBinder;
 import android.util.Log;
 import edu.illinois.CS598rhk.interfaces.IBluetoothMessage;
 import edu.illinois.CS598rhk.interfaces.IBluetoothService;
 import edu.illinois.CS598rhk.models.BluetoothMessage;
 import edu.illinois.CS598rhk.models.BluetoothNeighbor;
 import edu.illinois.CS598rhk.models.ElectionMessage;
 import edu.illinois.CS598rhk.models.WifiNeighbor;
 
 public class BluetoothService extends Service implements IBluetoothService {
 	private static Integer LOG_COUNTER;
 	
 	public static final String DISCOVERED_OVER_BLUETOOTH = "Wifi neighbor found over bluetooth";
 	public static final String INTENT_TO_ADD_BLUETOOTH_NEIGHBOR = "add bluetooth neighbor";
 	public static final String BLUETOOTH_NEIGHBOR_DATA = "bluetooth neighbor data";
 	
 	public static final String ACTION_ELECTED_FOR_WIFI_DISCOVERY = "action elected for wifi discovery";
 	public static final String DELY_UNTIL_STARTING_WIFI_DISCOVERY = "delay until starting wifi discovery";
 	
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
 
     private BluetoothMessageHandler messageHandler;
     private ElectionHandler electionHandler;
     
     private List<BluetoothMessage> messages;
     private Set<BluetoothDevice> neighbors;
     private Iterator<BluetoothDevice> nextNeighbor;
     private BluetoothDevice currentNeighbor;
     private boolean broadcasting;
     private boolean updatingNeighbors;
     
     private static final long TIMEOUT_INTERVAL = 3500;
     private Timer timer;
     private BluetoothResponseTimeout responseTimeout;
     
     @Override
     public void onCreate() {
     	super.onCreate();
     	myContactInfo = new BluetoothNeighbor();
     	messageHandler = new BluetoothMessageHandler();
     	electionHandler = new ElectionHandler();
     	timer = new Timer();
     }
     
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
     	// We could use setName() here to allow the user to change the device name
     	Notification notification = new Notification();
     	notification.tickerText = "BluetoothService";
     	startForeground(2, notification);
     	
        	LOG_COUNTER = new Integer(0);
     	mStateString = "STATE_INIT";
     	
     	myContactInfo.name = BluetoothAdapter.getDefaultAdapter().getName();
     	myContactInfo.address = BluetoothAdapter.getDefaultAdapter().getAddress();
     	myContactInfo.progress = 0;
     	
     	sendToLogger("BluetoothService:" 
     			+ "\n\tName: " + myContactInfo.name
     			+ "\n\tAddress: " + myContactInfo.address
     			+ "\n");
     	
     	broadcasting = false;
     	updatingNeighbors = false;
     	neighbors = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
     	nextNeighbor = neighbors.iterator();
     	
     	messages = new ArrayList<BluetoothMessage>();
     	start();
     	updateNeighbors();
     	return START_STICKY;
     }
     
     @Override
     public void onDestroy() {
     	stopForeground(true);
 		super.onDestroy();
     }
     
     public void updateScheduleProgress(long progress) {
     	synchronized(myContactInfo) {
     		sendToLogger("BluetoothService:"
     				+ "\n\tReceived progress update from " + myContactInfo.progress + " to " + progress
     				+ "\n");
     		myContactInfo.progress = progress;
     	}
     }
     
 	@Override
 	public void updateNeighborCount(int neighborCount) {
		if (!updatingNeighbors) {
			updatingNeighbors = true;
 			sendToLogger("BluetoothService:" + "Updating neighborCount to "
 					+ neighborCount + "\n");
 			synchronized (myContactInfo) {
 				myContactInfo.neighborCount = neighborCount;
 			}
		}
 	}
     
     public void updateNeighbors() {
    	sendToLogger("BluetoothService:"
    			+ "Begin updating neighbors\n");
    	broadcast(myContactInfo);
     }
     
     public void hostWifiDiscoveryElection() {
     	electionHandler.hostWifiDiscoveryElection();
     }
     
     public void broadcast(IBluetoothMessage message) {
     	sendToLogger("BluetooothService:"
     			+ "\n\tReceived message to broadcast"
     			+ "\n\tMessage:" + message
     			+ "\n");
     	broadcast(new BluetoothMessage(message));
     }
     
     private void broadcast(BluetoothMessage message) {
     	synchronized(messages) {
 	    	messages.add(message);
 	    	if (!broadcasting) {
 				broadcasting = true;
 				processNextMessage();
 			}
 	    	else {
 				if (!broadcasting) {
 					sendToLogger("BluetoothService:"
 							+ "\n\tCurrently connected to neighbor...\n");
 				} else {
 					sendToLogger("BluetoothService:"
 							+ "\n\tCurrently broadcasting message"
 							+ "\n\tMessage: " + messages.get(0)
 							+ "\n\tQueuing message: " + message + "\n");
 				}
 	    	}
     	}
     }
     
     private void processNextMessage() {
     	BluetoothMessage lastMessage = null;
     	synchronized(messages) {
 	     	if (nextNeighbor.hasNext()) {
 				currentNeighbor = nextNeighbor.next();
 				sendToLogger("BluetoothService: "
 						+ "\n\tAttempting to connect to Neighbor:"
 						+ "\n\t" + currentNeighbor
 						+ "\n\tWith message: " + messages.get(0)
 						+ "\n");
 				connect(currentNeighbor);
 			}
 	    	else {
 	    		lastMessage = messages.remove(0);
 	    		nextNeighbor = neighbors.iterator();
 	    		if (!messages.isEmpty()) {
 	    			sendToLogger("BluetoothService:"
 	    					+ "\n\t####### NOT Done Broadcasting #######"
 	    					+ "\n");
 	    			processNextMessage();
 	    		}
 	    		else {
 	    			broadcasting = false;
 	    			sendToLogger("BluetoothService:"
 	    					+ "\n\tDone Broadcasting"
 	    					+ "\n");
 	    		}
 	    	}
     	}
 		if ( lastMessage != null) {
 			switch (lastMessage.getMessageType()) {
 			case BluetoothMessage.BLUETOOTH_NEIGHBOR_HEADER:
 				updatingNeighbors = false;
 				break;
 			case BluetoothMessage.INITIATE_ELECTION_HEADER:
 				electionHandler.determineElectionWinner();
 				break;
 			case BluetoothMessage.ELECTION_WINNER_ANNOUNCEMENT_HEADER:
 				electionHandler.announceElectionWinner();
 				break;
 			default:
 				break;
 			}
 		}
     }
     
     private void sendMessage(BluetoothMessage message) {
     	synchronized(messages) {
 	    	if (message == null) {
 	    		message = messages.get(0);
 	    	}
 	    	sendToLogger("BluetoothService:"
 	    			+ "\n\tConnected to Neighbor:\n\t" + currentNeighbor
 	    			+ "\n\tSending message: " + message
 	    			+ "\n");
 	    	write(message.getMessageWithHeader());
     	}
     }
     
     public void newBluetoothNeighbor(BluetoothNeighbor neighbor) {
     	Intent i = new Intent(INTENT_TO_ADD_BLUETOOTH_NEIGHBOR);
     	i.putExtra(BLUETOOTH_NEIGHBOR_DATA, neighbor.pack());
     	sendBroadcast(i);
     	sendToLogger("BluetoothService:"
     			+ "\n\tFound Bluetooth Nieghbor:"
     			+ "\n\t" + neighbor
     			+ "\n");
     }
     
     public void newWifiNeighbor(WifiNeighbor neihgbor) {
     	sendToLogger("BluetoothService:"
     			+ "\n\tFound Wifi Nieghbor:"
     			+ "\n\t" + neihgbor
     			+ "\n");
     	Intent i = new Intent(WifiService.INTENT_TO_ADD_WIFI_NEIGHBOR);
     	i.putExtra(WifiService.WIFI_NEIGHBOR_DATA, neihgbor.pack());
     	i.putExtra(WifiService.INTENT_TO_ADD_WIFI_NEIGHBOR_SOURCE, DISCOVERED_OVER_BLUETOOTH);
     	sendBroadcast(i);
     }
     
 
 	
 	public void sendToLogger(String message) {
 		synchronized (LOG_COUNTER) {
 			Intent intentToLog = new Intent(PowerManagement.ACTION_LOG_UPDATE);
 			intentToLog.putExtra(PowerManagement.LOG_MESSAGE, message + " -- ["
 					+ new Date().toGMTString() + "] -- (" + LOG_COUNTER + ")");
 			sendBroadcast(intentToLog);
 			LOG_COUNTER++;
 		}
 	}
     
     //
     // Code taken BluetoothChatService API Demo
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
         
         sendToLogger("BluetoothService:\n\t" + log + "\n");
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
         if (D) sendToLogger("BluetoothService:"
         		+ "\n\tStart"
         		+ "\n");
 
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
         if (D) sendToLogger("BluetoothService:"
         		+ "\n\tConnect to: "
         		+ device
         		+ "\n");
 
         // Cancel any thread attempting to make a connection
         if (mState == STATE_CONNECTING) {
             if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
         }
 
         // Cancel any thread currently running a connection
         if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
 
         // Start the thread to connect with the given device
         mConnectThread = new ConnectThread(device);
         mConnectThread.start();
         responseTimeout = new BluetoothResponseTimeout();
         timer.schedule(responseTimeout, TIMEOUT_INTERVAL);
         setState(STATE_CONNECTING);
     }
 
     /**
      * Start the ConnectedThread to begin managing a Bluetooth connection
      * @param socket  The BluetoothSocket on which the connection was made
      * @param device  The BluetoothDevice that has been connected
      */
     public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
         if (responseTimeout != null) {
         	responseTimeout.cancel();
         }
     	
     	if (D) sendToLogger("BluetoothService:"
         		+ "\n\tConnected to: "
         		+ device
         		+ "\n");
 
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
         	sendToLogger("BluetoothService:"
             		+ "\n\t(In connected())"
             		+ "\n\tBroadcasting is true, sending next queued message"
             		+ "\n");
         	responseTimeout = new BluetoothResponseTimeout();
 			timer.schedule(responseTimeout, TIMEOUT_INTERVAL);
         	sendMessage(null);
         }
     }
 
     /**
      * Stop all threads
      */
     public synchronized void stop() {
         if (D) sendToLogger("BluetoothService:"
         		+ "\n\tStop()"
         		+ "\n");
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
     	sendToLogger("BluetoothService:"
         		+ "\n\t(In write())"
         		+ "\n");
         // Create temporary object
         ConnectedThread r;
         // Synchronize a copy of the ConnectedThread
         synchronized (this) {
             if (mState != STATE_CONNECTED) {
             	sendToLogger("BluetoothService:"
                 		+ "\n\t(In write())"
                 		+ "\n\tWrite called with no connection, aborting..."
                 		+ "\n");
             	return;
             }
             r = mConnectedThread;
         }
         // Perform the write unsynchronized
         r.write(out);
     }
 
     /**
      * Indicate that the connection attempt failed and notify the UI Activity.
      */
     private void connectionFailed() {
     	sendToLogger("BluetoothService:"
         		+ "\n\tConnection Failed..."
         		+ "\n");
     	setState(STATE_LISTEN);
     }
 
     /**
      * Indicate that the connection was lost and notify the UI Activity.
      */
     private void connectionLost() {
     	sendToLogger("BluetoothService:"
         		+ "\n\tConnection Lost..."
         		+ "\n");
     	if (!broadcasting) {
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
                 sendToLogger("BluetoothService:"
                 		+ "Accept failed in constructor"
                 		+ "\n");
                 Log.e(TAG, "accept() failed error message", e);
             }
             mmServerSocket = tmp;
         }
 
         public void run() {
             if (D) sendToLogger("BluetoothService:"
             		+ "\n\tBegin acceptThread"
             		+ this
             		+ "\n");
             setName("AcceptThread");
             BluetoothSocket socket = null;
 
             // Listen to the server socket if we're not connected
             while (mState != STATE_CONNECTED) {
                 try {
                     // This is a blocking call and will only return on a
                     // successful connection or an exception
                     socket = mmServerSocket.accept();
                 } catch (IOException e) {
                 	sendToLogger("BluetoothService:"
                     		+ "Accept failed in run()"
                     		+ "\n");
                     Log.e(TAG, "accept() failed error message", e);
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
                             	sendToLogger("BluetoothService:"
                             			+ "\n\tClosing unwanted socket because already connected or not ready to connect..."
                             			+ "\n");
                                 socket.close();
                             } catch (IOException e) {
                                 Log.e(TAG, "Could not close unwanted socket", e);
                             }
                             break;
                         }
                     }
                 }
             }
             if (D) sendToLogger("BluetoothService:"
             		+ "\n\tEnd of acceptThread run()"
             		+ "\n");
         }
 
         public void cancel() {
             if (D) sendToLogger("BluetoothService:"
             		+ "Cancel acceptThread..."
             		+ "\n");
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
             sendToLogger("BluetoothService:"
             		+ "Begin connectThread run()"
             		+ "\n");
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
         	sendToLogger("BluetoothService:"
             		+ "Creating connectedThread..."
             		+ "\n");
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
         	sendToLogger("BluetoothService:"
             		+ "Begin connectedThread run()"
             		+ "\n");
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
                     		messageLength = ByteBuffer.wrap(buffer,0,4).getInt();
                     		message = new byte[messageLength];
                     	}
                     	else if (receivingMessage && bytesRead == messageLength){
                     		receivingMessage = false;
                     		System.arraycopy(buffer, 0, message, 0, bytesRead);
 
                     		bytesRead = 0;
                     		messageLength = 4;
                     		
                     		IBluetoothMessage tempMessage = BluetoothMessage.parse(message);
                     		sendToLogger("BluetoothService:"
                     				+ "\n\tReceived message from Neighbor:" 
                     				+ "\n\tName: " + mmSocket.getRemoteDevice().getName()
                     				+ "\n\tAddress: " + mmSocket.getRemoteDevice().getAddress()
                     				+ "\n\tMessage: " + tempMessage
                     				+ "\n");
                     		
                     		messageHandler.handleMessage(tempMessage);
                     		this.cancel();
                     		BluetoothService.this.start();
                 			if (broadcasting) {
                 				processNextMessage();
                 			}
                 			break;
                     	}
                     }
                 } catch (IOException e) {
                 	sendToLogger("BleutoothService:"
                 			+ "Disconnected, read interrupted, calling connectionLost()..."
                 			+ "\n");
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
     
     private class BluetoothMessageHandler {
     	public void handleMessage(IBluetoothMessage message) {
     		if (message instanceof BluetoothNeighbor) {
     			if (responseTimeout != null) {
     				responseTimeout.cancel();
     			}
     			newBluetoothNeighbor((BluetoothNeighbor)message);
     			if (!broadcasting) {
 					sendMessage(new BluetoothMessage(myContactInfo));
 				}
     		}
     		else if (message instanceof WifiNeighbor) {
     			newWifiNeighbor((WifiNeighbor)message);
     		}
     		else if (message instanceof ElectionMessage) {
     			ElectionMessage electionMessage = (ElectionMessage)message;
     			switch(message.getMessageType()) {
         		case BluetoothMessage.INITIATE_ELECTION_HEADER:
         			sendMessage(new BluetoothMessage(electionHandler.getElectionResponse()));
         			break;
         		case BluetoothMessage.ELECTION_RESPONSE_HEADER:
         			if (responseTimeout != null) {
         				responseTimeout.cancel();
         			}
         			electionHandler.handleElectionResponse(electionMessage);
         			break;
         		case BluetoothMessage.ELECTION_WINNER_ANNOUNCEMENT_HEADER:
         			sendMessage(new BluetoothMessage(electionHandler.getElectionAcknowledgement(electionMessage)));
         			break;
         		case BluetoothMessage.ACKNOWLEDGE_ELECTION_WINNER:
         			if (responseTimeout != null) {
         				responseTimeout.cancel();
         			}
         			electionHandler.handleElectionAcknowledgement(electionMessage);
         			break;
         		}
     		}
     	}
     }
     
     private class ElectionHandler {
         private boolean hostingElection;
         private int myElectionResponseWindow;
         private List<Pair<BluetoothDevice, Integer>> electionResponses;
         
         private List<IBluetoothMessage> electionAnnouncements;
         private Iterator<IBluetoothMessage> nextAnnouncement;
         private boolean winnerAcknowledgedElection;
         
         public ElectionHandler() {
         	hostingElection = false;
         	myElectionResponseWindow = 1024;
         }
         
     	public void hostWifiDiscoveryElection() {
         	if (!hostingElection) {
         		sendToLogger("BluetoothService:"
         				+ "\n\tStarting new Wifi discovery election!"
         				+ "\n");
         		hostingElection = true;
         		electionResponses = new ArrayList<Pair<BluetoothDevice, Integer>>();
         		broadcast(new ElectionMessage(BluetoothMessage.INITIATE_ELECTION_HEADER));
         	}
         	else {
         		sendToLogger("BluetoothService:"
         				+ "\n\tAlready hosting election, request ignored."
         				+ "\n");
         	}
         }
     	
         public IBluetoothMessage getElectionResponse() {
         	Random rand = new Random();
         	int value = rand.nextInt(myElectionResponseWindow);
         	
         	sendToLogger("BluetoothService:"
         			+ "\n\tResponding to election with value " + String.valueOf(value)
         			+ "\n");
         	
         	return new ElectionMessage(value);
         }
         
     	public void handleElectionResponse(ElectionMessage message) {
     		sendToLogger("BluetoothService:"
     				+ "\n\tReceived value " + message.value + " from neighbor " + currentNeighbor.getAddress()
     				+ "\n");
     		synchronized(electionResponses) {
     			electionResponses.add(new Pair<BluetoothDevice, Integer>(currentNeighbor, message.value));
     		}
         }
         
         public void determineElectionWinner() {
     		sendToLogger("BluetoothService:"
     				+ "\n\tFinalizing Election...\n");
     		Collections.sort(electionResponses);
     		electionAnnouncements = new ArrayList<IBluetoothMessage>();
     		
     		long delay = myContactInfo.progress;
     		for (Pair<BluetoothDevice, Integer> response : electionResponses) {
     			electionAnnouncements.add(new ElectionMessage(response.first.getAddress(), delay));
     		}
 
     		electionResponses = null;
     		nextAnnouncement = electionAnnouncements.iterator();
     		winnerAcknowledgedElection = false;
     		announceElectionWinner();
     	}
     	
     	private void announceElectionWinner() {
     		sendToLogger("BluetoothService:"
     				+ "\n\tAnnouncing Election Results...\n");
 
     		if (!winnerAcknowledgedElection) {
 				if (nextAnnouncement.hasNext()) {
 					broadcast(nextAnnouncement.next());
 				} else {
 					winnerElected();
 				}
 			}
     		else {
     			winnerElected();
     		}
     	}
     	
         public ElectionMessage getElectionAcknowledgement(ElectionMessage result) {
         	ElectionMessage response = new ElectionMessage(BluetoothMessage.ACKNOWLEDGE_ELECTION_WINNER);
         	
         	if (myContactInfo.address.equals(result.winnerAddress)) {
         		response.value = 1;
         		myElectionResponseWindow = 1024;
         		
         		Intent i = new Intent(ACTION_ELECTED_FOR_WIFI_DISCOVERY);
     			i.putExtra(DELY_UNTIL_STARTING_WIFI_DISCOVERY, result.delayUntilWinnerStarts);
     			sendBroadcast(i);
     			
     			sendToLogger("BluetoothService:"
     					+ "\n\tResetting election window to 1024"
     					+ "\n");
     		}
     		else {
     			response.value = 0;
     			myElectionResponseWindow = Math.max(64, myElectionResponseWindow / 2);
     			
     			sendToLogger("BluetoothService:"
     					+ "\n\tDecreasing electionWindow to " + myElectionResponseWindow
     					+ "\n");
     		}
     		return response;
     	}
 
 		public void handleElectionAcknowledgement(ElectionMessage electionMessage) {
 			if (electionMessage.value == 1) {
 				winnerAcknowledgedElection = true;
 			}
 		}
     	
     	private void winnerElected() {    		
     		Intent i = new Intent(ACTION_ELECTED_FOR_WIFI_DISCOVERY);
     		long delay = 0;
     		synchronized(myContactInfo) {
     			 delay = myContactInfo.progress;
     		}
     		if (winnerAcknowledgedElection) {
     			delay *= -1;
     		}
     		i.putExtra(DELY_UNTIL_STARTING_WIFI_DISCOVERY, delay);
     		sendBroadcast(i);
     		
     		hostingElection = false;
     		electionAnnouncements = null;
     		nextAnnouncement = null;
     	}
     }
     
 	private class BluetoothResponseTimeout extends TimerTask {
 		@Override
 		public void run() {
 			sendToLogger("BluetoothService:"
 					+ "\n\tTimeout while waiting for response..."
 					+ "\n");
 			processNextMessage();
 		}
 	}
     
     private class Pair<T,E extends Comparable<E>> implements Comparable<Pair<T,E>> {
     	public T first;
     	public E second;
     	
     	public Pair(T t, E e) {
     		this.first = t;
     		this.second = e;
     	}
 
 		@Override
 		public int compareTo(Pair<T,E> another) {
 			return second.compareTo(another.second);
 		}
     }
 }
