 package com.ricex.rpi.remote.android.network;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 
 import android.util.Log;
 
 import com.ricex.rpi.common.message.IMessage;
 import com.ricex.rpi.common.message.remote.ClientListMessage;
 import com.ricex.rpi.common.message.remote.DirectoryListingMessage;
 
 
 /** Runs in the background and listens for messages received from the server
  * 
  * @author Mitchell
  *
  */
 public class ServerHandler implements Runnable {
 
 	/** The socket throught which this client is connected to the server */
 	private Socket socket;
 	
 	/** The stream in from the server */
 	private ObjectInputStream inStream;
 	
 	/** The stream out to the server */
 	private ObjectOutputStream outStream;	
 	
 	/** Boolean whether to cointinue running or not */
 	private boolean running;
 
 	
 	/** Creates a new server handle to listen on the given socket
 	 * 
 	 * @param socket The socket representing the connection to the server
 	 */
 	public ServerHandler(Socket socket) throws IOException {
 		this.socket = socket;
 		
 		inStream = new ObjectInputStream(socket.getInputStream());
		outStream = new ObjectOutputStream(socket.getOutputStream());		
 	}
 	
 	
 	/** Sends the given message to the server
 	 * 
 	 * @param message The message to send
 	 * @return True if sucessful false otherwise
 	 */
 	
 	public synchronized boolean sendMessage(IMessage message) {
 		Log.i("RPIServerHandler", "Sending message to server: " + message);
 		try {
 			outStream.writeObject(message);
 			outStream.flush();			
 		}
 		catch (IOException e) {
 			Log.e("RPIServerHandler","Failed to send message to server.", e);
 			return false; // send failed
 		}
 		return true;
 	}
 	
 	/** Runs the handler, listeners for messages from the server, and processes the message that
 	 * it received
 	 */
 	
 	public void run() {
 		//listn for messages from the server
 		
 		Object input;
 		
 		try {
 			while (running && (input = inStream.readObject()) != null) {
 				if (!(input instanceof IMessage)) {
 					Log.e("RPIServerHandler", "Received unsupported message from the server");
 				}
 				else {
 					processMessage( (IMessage) input);
 				}
 			}
 		}
 		catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		try {
 			inStream.close();
 			outStream.close();
 		}
 		catch (IOException e) {
 			Log.e("RPIServerHandler", "Error closing input and output stream", e);
 		}			
 	}
 	
 	public void close() {
 		running = false;
 	}
 	
 	/** Processes a message that has been received from the server
 	 * 
 	 * @param message The message received from the server 
 	 */
 	
 	protected synchronized void processMessage(IMessage message) {
 		if (message instanceof ClientListMessage) {
 			
 		}
 		else if (message instanceof DirectoryListingMessage) {
 			
 		}
 	}
 }
