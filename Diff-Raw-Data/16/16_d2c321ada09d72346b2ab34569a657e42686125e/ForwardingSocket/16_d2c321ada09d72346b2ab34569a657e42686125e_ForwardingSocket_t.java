 package com.napthats.websocket;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.Socket;
 import java.net.UnknownHostException;
  
 /**
  * send/recv messages to/from server
  * @author napthats
  *
  */
 final class ForwardingSocket extends Thread {
     private Socket itsSocket = null;
     private BufferedReader itsReader;
     private BufferedWriter itsWriter;
     private RecvFromServer itsRecvFromServer;
     private boolean isClosed = false;
         
     public ForwardingSocket(String host, int port, RecvFromServer RFS) {
     	itsRecvFromServer = RFS;
         try {
     	    itsSocket = new Socket(host, port);
    	    itsReader = new BufferedReader(new InputStreamReader(itsSocket.getInputStream(), "ISO-8859-1"));
    	    //itsReader = new BufferedReader(new InputStreamReader(itsSocket.getInputStream()));
      		itsWriter = new BufferedWriter(new OutputStreamWriter(itsSocket.getOutputStream()));
         } catch (UnknownHostException e) {
         	close();
             e.printStackTrace();
         } catch (IOException e) {
         	close();
             e.printStackTrace();
         }
         this.start();
     }
     
     public boolean isClosed() {
     	return isClosed;
     }
     
     public void close() {
 		try {
         	if (itsReader != null) itsReader.close();
         	if (itsWriter != null) itsWriter.close();
         	if (itsSocket != null) itsSocket.close();
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 		}
 		isClosed = true;
     }
  
     @Override
     public void run() {
 			try {
 				while (true) {
 					String msg = itsReader.readLine();
 					if (msg == null) break;
 					itsRecvFromServer.recvFromServer(msg);
 				}
 			} catch(IOException e) {
 				close();
 			}
 	}
  
     public void sendToServer(String msg) {
     	try {
             if (msg.endsWith("\n")) {
 	    		itsWriter.write(msg,0,msg.length());
 		    } else {
 			    itsWriter.write(msg,0,msg.length());
 			    itsWriter.write('\n');
 		    }
 		    itsWriter.flush();
     	}
     	catch (IOException e) {
     		close();
     	}
     }
     
     public interface RecvFromServer {
     	public void recvFromServer(String msg) throws IOException;
     }
 }
