 package com.example.wiiphone;
 
 import android.util.Log;
 import java.io.*;
 import java.net.InetAddress;
 import java.net.Socket;
  
  
 public class TCPClient {
  
     private String serverMessage;
    public static final String SERVERIP = "192.168.1.121"; //your computer IP address
     public static final int SERVERPORT = 10000;
     private OnMessageReceived mMessageListener = null;
     private boolean mRun = false;
     private Socket socket = null;
  
     PrintWriter out = null;
     BufferedReader in = null;
  
     /**
      *  Constructor of the class. OnMessagedReceived listens for the messages received from server
      */
     public TCPClient(OnMessageReceived listener) {
         mMessageListener = listener;
     }
  
     /**
      * Sends the message entered by client to the server
      * @param message text entered by client
      */
     public void sendMessage(String message)
     {
         if (out != null && !out.checkError()) 
         {
             out.println(message);
             out.flush();
         }
     }
  
     public void stopClient()
     {
         mRun = false;
         try 
         {
         	if(socket != null)
         	{
         		Log.e("SOCKET", "Destroy start");
         		socket.close();
         		socket = null;
         		Log.e("SOCKET", "Destroy stop");
         	}
         	if(in != null)
         	{
         		Log.e("IN", "Destroy start");
         		in.close();
         	    in = null;
         	    Log.e("IN", "Destroy stop");
         	}
         	if(out != null)
         	{
         		Log.e("OUT", "Destroy start");
         		out.flush();
         		out.close();
         		out = null;
         		Log.e("OUT", "Destroy stop");
         	}
         	serverMessage = null;
 		} catch (IOException e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     }
  
     public void run() 
     {
  
         mRun = true;
  
         try 
         {
             //here you must put your computer's IP address.
             InetAddress serverAddr = InetAddress.getByName(SERVERIP);
  
             Log.e("TCP Client", "C: Connecting...");
  
             //create a socket to make the connection with the server
             socket = new Socket(serverAddr, SERVERPORT);
             Log.e("TCP Client", "C: Connecting done");
             try {
  
                 //send the message to the server
                 out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
  
                 Log.e("TCP Client", "C: Sent.");
  
                 
  
                 //receive the message which the server sends back
                 in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
  
                 //in this while the client listens for the messages sent by the server
                 while (mRun) {
                 	Log.e("TEST", "before read line");
                     serverMessage = in.readLine();
                     Log.e("TEST", "after read line");
                     if (serverMessage != null && mMessageListener != null) {
                         //call the method messageReceived from MyActivity class
                         mMessageListener.messageReceived(serverMessage);
                     }
                     serverMessage = null;
                 }
  
  
                 Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + serverMessage + "'");
  
  
             } catch (Exception e) {
  
                 Log.e("TCP", "S: Error", e);
  
             } finally {
                 //the socket must be closed. It is not possible to reconnect to this socket
                 // after it is closed, which means a new socket instance has to be created.
                 if(socket != null)
                 {
                 	socket.close();
                 	socket = null;
                 }
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
