 package di.kdd.smartmonitor.protocol;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import android.util.Log;
 import di.kdd.smartmonitor.middleware.TimeSynchronizationMessage;
 import di.kdd.smartmonitor.protocol.ISmartMonitor.Tag;
 import di.kdd.smartmonitor.protocol.exceptions.TagException;
 
 public class JoinThread extends Thread {
 	private PeerData peerData;
 	ServerSocket joinSocket;
 
 	private static final String TAG = "JOIN listener";
 	
 	public JoinThread(PeerData peerData) {
 		this.peerData = peerData;
 	}
 		
 	@Override
 	public void run() {
 		Message message;
 		Socket connectionSocket = null;
 		
 		android.os.Debug.waitForDebugger();
 		
 		try {
 			joinSocket = new ServerSocket(ISmartMonitor.JOIN_PORT);
 			joinSocket.setReuseAddress(true);
 		} 
 		catch (IOException e) {
 			Log.e(TAG, "Could not bind socket at the knock knock port");
 			e.printStackTrace();
 			
 			return;
 		}
 
 		Log.i(TAG, "Listening on " + Integer.toString(ISmartMonitor.JOIN_PORT));
 		
 		while(!this.isInterrupted()) {
 			try {
 				connectionSocket = joinSocket.accept();
 				
 				/* Receive JOIN message */
 				
 				DistributedSystemNode.receive(connectionSocket, Tag.JOIN);
 
				/* Send the peer data to the node that wants to join the distributed system */
 				
 				message = new Message(Tag.PEER_DATA, peerData.toString());
 				DistributedSystemNode.send(connectionSocket, message);
 				
 				/* Update the peer data with the new IP address */
 				
 				peerData.addPeerIP(connectionSocket.getInetAddress().toString());
 				
 				/* Send synchronization message */
 				
 				DistributedSystemNode.send(connectionSocket, new TimeSynchronizationMessage());
 			}
 			catch(IOException e) {
 				Log.e(TAG, "Error while communicating with a peer");
 				e.printStackTrace();
 			}
 			catch(TagException e) {
 				Log.e(TAG, "Didn't receive JOIN tag");
 			}
 			finally {
 				try {
 					if(connectionSocket != null) {
 						connectionSocket.close();
 					}
 				} catch (IOException e) {
 				}
 			}
 		}
 		
 		/* Join thread was interrupted */
 		
 		try {
 			joinSocket.close();
 		}
 		catch(IOException e) {			
 		}
 
 	}	
 }
