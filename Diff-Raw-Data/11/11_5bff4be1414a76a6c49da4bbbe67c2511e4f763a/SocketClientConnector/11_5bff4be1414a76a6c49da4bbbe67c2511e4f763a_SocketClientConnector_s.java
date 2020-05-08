 /**
  * @author Rohan K.M rohan.mahendroo@gmail.com
  * ConnStarted
  * com.app.project.acropolis.comm
  * ConnStarted.java
  * Created - 2013-11-01 12:35:20 PM	
  * Modified - 2013-11-01 12:35:20 PM
  * TODO
  * NOTES - 
  */
 package com.app.project.acropolis.comm;
 
import java.io.BufferedOutputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.nio.channels.ClosedByInterruptException;
 
 import com.app.project.acropolis.GlobalConstants;
 import com.app.project.acropolis.Logger;
 
 /**
  * @author CPH-iMac
  *
  */
 public class SocketClientConnector
 {
 
 	public static Socket clientSocket = null;
 	
 	public static class Connector implements Runnable 
 	{
 		public void run()
 		{
 			try 
 			{
 				InetAddress clientInet = InetAddress.getByName(GlobalConstants.ServerIP);
 				clientSocket = new Socket(clientInet,GlobalConstants.SocketClientPORT);
 //				clientSocket.setSoLinger
 //				(GlobalConstants.SocketClientLINGER,
 //						GlobalConstants.SocketClientLINGER_TIMEOUT);
 				sendMessage();
 				closeConnection();
 			} catch (UnknownHostException e1) {
 				e1.printStackTrace();
 			} catch (IOException e2) {
 				e2.printStackTrace();
 			}
 		}
 	}
 
 	public static void closeConnection()
 	{
 		try {
 			clientSocket.close();
 			Logger.Debug("connection closed");
 			if(Thread.currentThread().isAlive())
 				Thread.currentThread().interrupt();
 		} catch(UnknownHostException e1) {
 			e1.printStackTrace();
 		} catch(ClosedByInterruptException e3) {
 			e3.printStackTrace();
 			Logger.Debug("ClientConnection closed\n");
 		} catch (IOException e2) {
 			e2.printStackTrace();
 		}
 	}
 	
 	public static void sendMessage()
 	{
 		try {
 			Logger.Debug("$$$$   writing on server    $$$$");
 			PrintWriter printOUT =
					new PrintWriter( new BufferedOutputStream(clientSocket.getOutputStream()));
 			printOUT.println(DataTumblr.getSendClientData());
 		} catch(UnknownHostException e1) {
 			e1.printStackTrace();
 		} catch(ClosedByInterruptException e3) {
 			e3.printStackTrace();
 			Logger.Debug("ClientConnection closed\n");
 		} catch (IOException e2) {
 			e2.printStackTrace();
 		} 
 	}
 }
