 package di.kdd.smartmonitor.protocol;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.Socket;
 
 import android.util.Log;
 
 public abstract class DistributedSystemNode extends Thread {
 	protected PeerData peerData = new PeerData();
 	
 	private static final String TAG = "node";
 	
 	public abstract void disconnect();
 	
 	public abstract boolean isMaster();
 
 	public String getMasterIP() {
 		return peerData.getLowestIP();
 	}
 	
 	/***
 	 * Given an open socket and a message, sends the message
 	 * @param socket The open socket to send the message 
 	 * @param message The message to send
 	 * @throws IOException When the socket is not open
 	 */
 	
 	protected static void send(Socket socket, Message message) throws IOException {
 		Log.i(TAG, "Sending: " + message.toString());
		
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		out.writeChars(message.toString());		
 	}
 	
 	/***
 	 * Give an initialized socket, it returns its input stream
 	 * @param socket The socket to get the input stream of
 	 * @return The socket's input stream
 	 * @throws IOException
 	 */
 	
 	//TODO Added a Tag parameter, when you want to receive a message with a specific Tag
 	
 	protected static BufferedReader receive(Socket socket) throws IOException {
 		Log.i(TAG, "Receiving from " + socket.getRemoteSocketAddress());
 		
 		return new BufferedReader(new InputStreamReader(socket.getInputStream()));		
 	}
 }
