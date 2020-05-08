 package di.kdd.smartmonitor.protocol;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.Socket;
 
 import android.util.Log;
 
 public abstract class DistributedSystemNode extends Thread {
 	protected PeerData peerData = new PeerData();
 
 	/*
	 * The Master uses this thread in order to send commands to the peers
 	 * and the peers use this thread to receive commands from the Master.
 	 */
 	
 	protected Thread commandThread;
 
 	private static final String TAG = "distributed system node";
 	
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
 		Log.d(TAG, message.toString());
 		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
 		out.writeChars(message.toString());		
 	}
 	
 	/***
 	 * Give an initialized socket, it returns its input stream
 	 * @param socket The socket to get the input stream of
 	 * @return The socket's input stream
 	 * @throws IOException
 	 */
 	
 	protected static BufferedReader receive(Socket socket) throws IOException {
 		Log.d(TAG, "Receiving");
 		return new BufferedReader(new InputStreamReader(socket.getInputStream()));		
 	}
 }
