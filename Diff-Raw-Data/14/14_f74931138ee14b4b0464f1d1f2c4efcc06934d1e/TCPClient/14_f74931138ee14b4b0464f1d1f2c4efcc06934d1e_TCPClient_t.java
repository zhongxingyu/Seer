 package com.example.wiiphone;
 
 import android.util.Log;
 import java.io.*;
 import java.net.InetAddress;
 import java.net.Socket;
  
  
 public class TCPClient {
 	public static TCPClient mTCPClient = null;
     private String mServerMessage;
    public static final String SERVERIP = "192.168.1.122"; //your computer IP address
     public static final int SERVERPORT = 10000;
     private OnMessageReceived mMessageListener = null;
     private boolean mRun = false;
     private Socket socket = null;
     private boolean mReadyToSend = false;
     PrintWriter mBufferOut = null;
     BufferedReader mBufferIn = null;
  
     /**
      *  Constructor of the class. OnMessagedReceived listens for the messages received from server
      */
     public TCPClient(OnMessageReceived listener) {
         mMessageListener = listener;
         mTCPClient = this;
     }
     public boolean GetReadyToSend()
     {
     	return mReadyToSend;
     }
     public static TCPClient getTCP()
     {
     	if(mTCPClient == null)
     		Log.e("ERROR", "TCP == null");
     	return mTCPClient;
     }
     /**
      * Sends the message entered by client to the server
      * @param message text entered by client
      */
     public void sendMessage(String message)
     {
         if (mBufferOut != null && !mBufferOut.checkError()) 
         {
         	Log.e("SEND MSG: ", "MSG: " + message);
         	mBufferOut.println(message);
         	mBufferOut.flush();
         }
     }
  
     public void stopClient()
     {
     	 mRun = false;
     	 try{
     		 if (socket != null) 
         	 {
         		 socket.close();
         		 socket = null;
              }
     	 }
     	 catch (IOException e) 
     	 {
     	 }
     	 
          if (mBufferOut != null) {
              mBufferOut.flush();
              mBufferOut.close();
          }
   
          mMessageListener = null;
          mBufferIn = null;
          mBufferOut = null;
          mServerMessage = null;
     }
  
     public void run() 
     {
  
         mRun = true;
  
         try 
         {
             //here you must put your computer's IP address.
             InetAddress serverAddr = InetAddress.getByName(SERVERIP);
  
             Log.e("TCP Client", "C: Connecting... ");
  
             //create a socket to make the connection with the server
             socket = new Socket(serverAddr, SERVERPORT);
             Log.e("TCP Client", "C: Connecting done");
             mReadyToSend = true;
             try {
  
                 //send the message to the server
             	mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
             	
             	String message1 = "GET MODE";
             	mBufferOut.println(message1);
             	
                 Log.e("TCP Client", "C: Sent. ");
  
                 
  
                 //receive the message which the server sends back
                 mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
  
                 //in this while the client listens for the messages sent by the server
                 while (mRun) {
                 	mServerMessage = mBufferIn.readLine();
                     if (mServerMessage != null && mMessageListener != null) {
                         //call the method messageReceived from MyActivity class
                         mMessageListener.messageReceived(mServerMessage);
                     }
                     mServerMessage = null;
                 }
  
  
                 Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + mServerMessage + "'");
  
  
             } catch (Exception e) {
  
                 Log.e("TCP", "S: Error", e);
  
             } finally {
                 //the socket must be closed. It is not possible to reconnect to this socket
                 // after it is closed, which means a new socket instance has to be created.
                 socket.close();
             }
  
         } catch (Exception e) {
  
             Log.e("TCP", "C: Error", e);
  
         }
  
     }
  
     //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
     //class at on asynckTask doInBackground
     public interface OnMessageReceived {
         public void messageReceived(String message);
     }
 }
