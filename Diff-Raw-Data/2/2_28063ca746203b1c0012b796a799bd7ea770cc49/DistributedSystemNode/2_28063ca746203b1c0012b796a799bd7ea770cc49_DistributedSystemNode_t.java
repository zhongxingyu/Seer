 package di.kdd.smartmonitor.protocol;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.Socket;
 
 import di.kdd.smartmonitor.protocol.ISmartMonitor.Tag;
 import di.kdd.smartmonitor.protocol.exceptions.TagException;
 
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
 	
 	protected static Message receive(Socket socket) throws IOException {
 		Log.i(TAG, "Receiving from " + socket.getInetAddress());
 		
 		return new Message(new BufferedReader(new InputStreamReader(socket.getInputStream())));		
 	}
 	
 	/***
 	 * Receives a message from a connected socket and checks the tag of the message
 	 * @param tag The desired tag
 	 * @param socket The connected socket
 	 * @return The message that was read from the socket
 	 * @throws IOException Socket failure
 	 * @throws TagException When the desired tag is not the same with the tag of 
 	 * the received message
 	 */
 	
 	protected static Message receive(Socket socket, Tag tag) throws IOException, TagException {
 		Message message;
 		
		Log.i(TAG, "Receiving from " + socket.getInetAddress() + " with desired Tag: " + tag.toString());
 		
 		message = new Message(new BufferedReader(new InputStreamReader(socket.getInputStream())));		
 		
 		if(message.getTag() != tag) {
 			throw new TagException();
 		}
 		
 		return message;
 	}
 
 }
