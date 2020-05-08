 package di.kdd.smartmonitor.protocol;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import android.util.Log;
 import di.kdd.smartmonitor.middleware.TimeSynchronization;
 import di.kdd.smartmonitor.protocol.ISmartMonitor.Tag;
 
 public final class PeerNode extends DistributedSystemNode implements Runnable {
 	/* The socket that the peer holds in order to get command messages from the Master */
 
 	private ServerSocket commandsServerSocket;
 
 	private TimeSynchronization timeSync = new TimeSynchronization();
 
 	private static final String TAG = "peer";
 
 	/***
 	 * Sends a JOIN message to the Master node and if it gets accepted,
 	 * starts a thread that accepts commands from the Master
 	 * @param socket The connected to the Master node socket
 	 */
 
 	public PeerNode(Socket socket) {		
 		Message message;
 
 		try {
 			/* Start command-serving thread */
 
 			new Thread(this).start();
 
 			/* The Master node was found, send the JOIN message */
 
 			message = new Message(Tag.JOIN);
 			send(socket, message);	
 		}
 		catch(Exception e) {
 			Log.e(TAG, "Failed to join the system: " + e.getMessage());
 			e.printStackTrace();
 		}
 		finally {
 			try {
 				socket.close();
 			}
 			catch(IOException e) {				
 			}
 		}
 	}
 
 	/***
 	 * Accepts a socket connection on the COMMAND_PORT and waits for commands from the Master
 	 */
 
 	@Override
 	public void run() {
 		Socket masterSocket;
 		BufferedReader in;
 
 		android.os.Debug.waitForDebugger();
 
 		Log.i(TAG, "Command-serving thread was started");
 
 		try {
 			commandsServerSocket = new ServerSocket(ISmartMonitor.COMMAND_PORT);		
 			commandsServerSocket.setReuseAddress(true);
 			masterSocket = commandsServerSocket.accept();
 
			Log.i(TAG, "Accepted command socket from " + masterSocket.getRemoteSocketAddress().toString());
 		}
 		catch(IOException e) {
 			Log.e(TAG, "Failed to accept command socket");
 			e.printStackTrace();
 
 			return;
 		}
 
 		/* Listen on MasterSocket for incoming commands from the Master */
 
 		Log.i(TAG, "Listening for commands from the Master node");
 
 		while(!this.isInterrupted()) {
 			try {		
 				Message message;
 
 				message = receive(masterSocket);
 
 				switch(message.getTag()) {
 				case PEER_DATA:
 					Log.i(TAG, "Received PEER_DATA command");
 
 					message = receive(masterSocket);
 					peerData.addPeersFromMessage(message);					
 					break;
 				case SYNC:
 					Log.i(TAG, "Received SYNC command");
 
 					timeSync.timeReference(Long.parseLong(message.getPayload()));
 					break;
 				case NEW_PEER:
 					Log.i(TAG, "Received NEW_PEER command");	
 
 					peerData.addPeerIP(message.getPayload());
 					break;
 				case START_SAMPLING:
 					Log.i(TAG, "Received START_SAMPLING command");
 
 					break;
 				case STOP_SAMPLING:
 					Log.i(TAG, "Received STOP_SAMPLING command");
 
 					break;
 				case SEND_PEAKS:
 					Log.i(TAG, "Received SEND_PEAKS command");
 					break;
 				default:
 					Log.e(TAG, "Not implemented Tag handling: " + message.getTag().toString());
 					break;
 				}
 			}
 			catch(IOException e) {
 				Log.e(TAG, "Error while listening to commands from Master node");
 				e.printStackTrace();
 			}		
 		}
 	}
 
 	@Override
 	public void disconnect() {
 		this.interrupt();
 
 		try {
 			commandsServerSocket.close();			
 		}
 		catch(IOException e) {			
 		}
 	}
 
 	@Override
 	public boolean isMaster() {
 		return false;
 	}
 }
