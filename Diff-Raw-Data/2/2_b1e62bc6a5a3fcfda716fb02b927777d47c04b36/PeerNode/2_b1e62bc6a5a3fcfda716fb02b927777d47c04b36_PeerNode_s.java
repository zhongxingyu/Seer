 package di.kdd.smartmonitor.protocol;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import android.util.Log;
 import di.kdd.smartmonitor.middleware.TimeSynchronization;
 import di.kdd.smartmonitor.protocol.ISmartMonitor.Tag;
 
 public final class PeerNode extends DistributedSystemNode implements Runnable {
 	private Socket joinSocket;
 	private ServerSocket commandsServerSocket;
 
 	private TimeSynchronization timeSync = new TimeSynchronization();
 
 	private static final String TAG = "peer";
 
 	/***
 	 * Sends a JOIN message to the Master node and if it gets accepted,
 	 * starts a thread that accepts commands from the Master
 	 * @param joinSocket The connected to the Master node socket
 	 */
 
 	public PeerNode(Socket joinSocket) {		
 		this.joinSocket = joinSocket;
 
 		/* Start command-serving thread */
 
 		new Thread(this).start();
 	}
 
 	/***
 	 * Accepts a socket connection on the COMMAND_PORT and waits for commands from the Master
 	 */
 
 	@Override
 	public void run() {
 		Message message;
 		Socket masterSocket;
 
 		android.os.Debug.waitForDebugger();
 
 		try {
 			commandsServerSocket = new ServerSocket(ISmartMonitor.COMMAND_PORT);		
 			commandsServerSocket.setReuseAddress(true);
 		}
 		catch(IOException e) {
			Log.e(TAG, "Failed to accept command socket");
 			e.printStackTrace();
 
 			return;
 		}
 
 		Log.i(TAG, "Joining the system");
 		
 		try {
 			/* The Master node was found, send the JOIN message */
 	
 			message = new Message(Tag.JOIN);
 			send(joinSocket, message);	
 	
 			/* Receive PEER_DATA */
 			
 			message = receive(joinSocket, Tag.PEER_DATA);
 			
 			/* Receive TIME_SYNC */
 	
 			message = receive(joinSocket, Tag.SYNC);
 		}
 		catch(Exception e) {
 			Log.e(TAG, "Failed to join the system: " + e.getMessage());
 			return;
 		}
 		finally {
 			try {
 				joinSocket.close();
 			}
 			catch(Exception e) {				
 			}
 		}
 		
 		Log.i(TAG, "Starting serving commands");
 
 		try {
 			masterSocket = commandsServerSocket.accept();
 		}
 		catch(IOException e) {
 			Log.e(TAG, "Failed to accept socket for command serving");
 			return;
 		}
 		
 		Log.i(TAG, "Accepted command socket from " + masterSocket.getInetAddress().toString());
 
 		/* Listen on MasterSocket for incoming commands from the Master */
 
 		Log.i(TAG, "Listening for commands from the Master node");
 
 		while(!this.isInterrupted()) {
 			try {		
 				message = receive(masterSocket);
 
 				switch(message.getTag()) {
 				case PEER_DATA:
 					Log.i(TAG, "Received PEER_DATA command");
 
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
 			catch(ClassNotFoundException e) {
 				Log.e(TAG, "Error while receiving data");
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
