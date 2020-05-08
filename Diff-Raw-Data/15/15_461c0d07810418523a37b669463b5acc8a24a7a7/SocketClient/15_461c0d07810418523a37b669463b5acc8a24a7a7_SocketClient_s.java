 package com.cs456.a2;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.net.Socket;
 
 import android.content.Context;
 
 /**
  * A socket used to connect to a server socket on another device
  * @author Janson
  *
  */
 public class SocketClient extends SocketBase {
 	private Socket socket = null;
 
 	/**
 	 * Creates a new client socket in its own thread
 	 * @param context The context to display error messages
 	 */
 	public SocketClient(Context context) {
 		this.context = context;
 	}
 	
 	@Override
 	protected Object doInBackground(Object... arg0) {
 		BufferedReader in = null;
 		BufferedWriter out = null;
 		PrintWriter pw = null;
 		String fileList = "";
 		try {			
 			// Create a new socket
 			socket = new Socket((String)arg0[0], SOCKET_PORT);
 			Logger.getInstance().log(" CONNECT: "+arg0[0].toString()+", SUCCESS");
 			// Create the input and output stream readers
 			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 			out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
 			
 			// Create a PrintWriter to use for the output stream
 			pw = new PrintWriter(out);
 			
 			String line = null;
 			
 			// Send the start connection message, and wait for the start message response
 			while (true) {
 				pw.println(START_MESSAGE);
 				pw.flush();
 				
 				line = in.readLine();
 				
 				if(line == null) {
 					throw new IOException("The server device has diconnected.  Please reconnect and request data again");
 				}
 				
 				if(START_MESSAGE.equals(line)) {
 					break;
 				}
 			}
 
 			//Send a request for the file list message
 			pw.println(SEND_FILE_LIST_MESSAGE);
 			pw.flush();
 			
 			// Continue to read in the file list until the end of file list message is received
 			while (true) {
 				
 				line = in.readLine();
 				
 				// When we get the end of file list message, send the connection exit message
 				if(END_FILE_LIST_MESSAGE.equals(line)) {
 					pw.println(EXIT_MESSAGE);
 					pw.flush();
 					break;
 				}
 				// The socket closed, break out of this
 				else if(line == null)
 					throw new IOException("The server device has diconnected.  Please reconnect and request data again");
 				// Append the new line of the file list data to a string
 				else {
 					fileList += line + "\n";
 				}
 			}
 			
 			// Wait for the exit successful message to be received.  We don't really care what this is as long as we get something
 			line = in.readLine();
 			
 		} catch (IOException e) {
 			Logger.getInstance().log(" CONNECT: "+arg0[0].toString()+", FAILURE");
 			handleError(e.getMessage());
 		}
 		// This is always run
 		finally {
 			try {
 				shouldQuit = true;
 				
 				// Close everything that was opened
 				if(in != null) in.close();
 				if(out != null) out.close();
 				if(pw != null) pw.close();
 				if(socket != null) socket.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}				
 		}
		
		Logger.getInstance().log(" FILE LIST: "+arg0[0]+","+fileList.replaceAll("\n", ", "));
 		return fileList;
 	}
 	
 	/**
 	 * If this thread is supposed to close, we close the socket so
 	 * the thread will know to exit.
 	 */
 	protected void handleKillThread() {
 		try {
 			if (socket != null) socket.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 }
