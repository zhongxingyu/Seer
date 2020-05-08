 package com.bluetext.nextapp;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 import java.sql.SQLException;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import android.os.AsyncTask;
 import android.telephony.SmsManager;
 import android.util.Log;
 import bigsky.BlueTextRequest;
 import bigsky.Contact;
 import bigsky.TextMessage;
 
 
 /**
  * First argument must be IP address<br>
  * Second argument must be the port to create the socket on<br>
  * (Both must be strings)
  * @author Andrew
  */
 public class ServerListener extends AsyncTask<String, Void, Socket>
 {
 	Socket sock;
 	int port;
 	String ipAddress;
 	static ObjectInputStream fromPC;
 	static ObjectOutputStream toPC;
 	private final String TAG = "AGG";
 	public static PostLoginActivity pla;
 	
 	protected Socket doInBackground(String... params)
 	{
 		Log.d(TAG, "Using IP: " + params[0]);
 		Log.d(TAG, "Creating socket on port: " + params[1]);
 		this.ipAddress = params[0];	
 		this.port = Integer.parseInt(params[1]);
 		
 		try{
 			sock = new Socket(this.ipAddress, this.port);
 		}catch(Exception e){
 			Log.d(TAG, "Error in serverListener ctor: " + e.getMessage());
 		}
 		
 		try{
 			fromPC = new ObjectInputStream(sock.getInputStream());
 			toPC = new ObjectOutputStream(sock.getOutputStream());
 		}catch(Exception e){
 			Log.d(TAG, "Error creating ObjectStreams ctor: " + e.getMessage());
 		}
 		
 		// Initiate the SMS listener on the phone
 		SmsListener.setServerListener(this);
 		BlueTextRequestActivity.setServerListener(this);
 		try {
 			sendContactsToPc(MainActivity.getAllContacts.get());
 		} catch (Exception e) {
 			Log.d(TAG, "Error getting all contacts inside ServerListener.");
 		}
 		
 				
 		Log.d(TAG, "Listening for messages from PC forever...");
 		
 		int sendCode = 0;
 		while(fromPC != null && sendCode == 0)
 		{
 			try {
 				Object streamObject = fromPC.readObject();
 				if(streamObject instanceof TextMessage)
 				{
 					TextMessage txtMsg = (TextMessage) streamObject;
 					if(txtMsg != null){
 						sendCode = phoneSendText(txtMsg);
 					}
 				}
 				else if(streamObject instanceof BlueTextRequest){
 					BlueTextRequest request = (BlueTextRequest) streamObject;
 					new BlueTextRequestActivity().executeOnExecutor(THREAD_POOL_EXECUTOR, request);
 				}
 				else{
 					Log.d(TAG, "Unknown class was sent through the stream: " + streamObject.toString());
 				}
 								
 			} catch (Exception e) {	
 				Log.d(TAG, "Socket was broken, closing the port");
 				// If the socket dies, set to null in Main to allow reconnecting
 				MainActivity.task = null;
 				MainActivity.sqlTask = null;
 				// If the PC side broke the stream, take us back to login window
 				if(pla != null){
 					pla.finish();
 				}
 				
 				closeStream();
 				
 				return sock;
 			}
 		}
 		return sock;
 	}
 	
 	/**
 	 * Sends any amount of contact objects to the PC
 	 * @param contacts
 	 */
 	public void sendContactsToPc(ConcurrentLinkedQueue<Contact> contacts)
 	{
 		Log.d(TAG, "Sending " + contacts.size() + " contacts to PC." );
 		
 		while(!contacts.isEmpty()){
 			sendObjectToPC(contacts.remove());
 		}
 	}
 	
 	public synchronized void sendObjectToPC(Object o)
 	{
 		try{
 			toPC.writeObject(o);
 			toPC.flush();
 		} catch(Exception e){
 			System.out.println("Got exception in ServerListener.sendObject(): " + e.getMessage());
 		}
 	}
 	
 	/**
 	 * Sends a text message to the intended receiver through the phone's default
 	 * messaging service.
 	 * 
 	 * @param msg
 	 * @return
 	 */
 	private int phoneSendText(TextMessage msg)
     {
     	try{
     		SmsManager.getDefault().sendTextMessage(msg.getReceiver().getPhoneNumber(), null, msg.getContent(), null, null);
     		Log.d(TAG, "SMS sent to " + msg.getReceiver().getFirstName() + "!");
     		return 0;
     	} catch (Exception e){
     		Log.d(TAG, "SMS delivery failed.");
     		return -1;
     	}
     }
 	
 	private void closeStream()
 	{
 		String cleanUpStatus = "Socket cleanup Status: ";
 		try{
 			cleanUpStatus += "InputStream=";
 			fromPC.close();
 			cleanUpStatus += "SUCCESS   ";
 		} catch(IOException e1){
 			cleanUpStatus += "FAILED   ";
 		}
 		try{
 			cleanUpStatus += "OutputStream=";
 			toPC.close();
 			cleanUpStatus += "SUCCESS   ";
 		} catch(IOException e1){
 			cleanUpStatus += "FAILED   ";
 		}
 		try{
 			cleanUpStatus += "Socket=";
 			sock.close();
 			cleanUpStatus += "SUCCESS";
 		} catch(IOException e1){
 			cleanUpStatus += "FAILED";
 		}			
 		Log.d(TAG, cleanUpStatus);
 	}
 }
