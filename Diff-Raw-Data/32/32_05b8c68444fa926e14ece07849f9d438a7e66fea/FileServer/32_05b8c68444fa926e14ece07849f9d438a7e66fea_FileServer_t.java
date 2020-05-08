 package org.AndroidShareApp.core;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 
 import android.util.Log;
 
 public class FileServer extends Thread {
 
 	private ArrayList<String> mPermissions;
 	private int mPort;
 
 	public FileServer(int port) {
 		mPermissions = new ArrayList<String>();
 		mPort = port;
 		setName("FileServer");
 	}
 
 	@Override
 	public void run() {
 		try {
 			ServerSocket serverSocket = new ServerSocket(mPort);
 			// TODO: Colocar as portas no NetworkProtocol.
 			Socket socket;
 
 			while (!isInterrupted()) {
 				socket = serverSocket.accept();
 
 				InputStream is = socket.getInputStream();
 
 				/* First, we receive the request. */
 				byte[] buffer = new byte[1024];
 				int i = 0;
 				do {
 					is.read(buffer, i, 1);
 					System.out.println((char) buffer[i]);
 					i++;
 				} while (buffer[i - 1] != '\n');
 
 				String currentRequest = new String(buffer).substring(0, i - 1);
 				Log.i("FileServer", "Received request \"" + currentRequest
 						+ "\"");
 
 				/* Then, we see if we can accept if. */
 				synchronized (mPermissions) {
 					if (mPermissions.contains(currentRequest)) {
 						Thread t = new Thread(new FileServerThread(
 								currentRequest, socket));
 						mPermissions.remove(currentRequest);
 						Log.i("FileServer", "Removed permission \""
 								+ currentRequest + "\".");
 						t.start();
 					}
 				}
				//is.close();
 			}
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void addPermission(String deviceID, String path) {
 		synchronized (mPermissions) {
 			mPermissions.add(deviceID + " " + path);
 		}
 		Log.i("FileServer", "Added permission \"" + deviceID + " " + path
 				+ "\".");
 	}
 }
