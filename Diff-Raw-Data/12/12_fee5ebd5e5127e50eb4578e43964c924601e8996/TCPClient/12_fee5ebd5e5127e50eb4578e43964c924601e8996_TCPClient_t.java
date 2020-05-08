 package com.anusiewicz.MCForAndroid.controllers;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Szymon Anusiewicz
  * Date: 27.06.13
  * Time: 01:50
  */
 import android.util.Log;
 import org.apache.http.util.EncodingUtils;
 import java.io.*;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.LinkedList;
 import java.util.List;
 
 public class TCPClient {
 
     private final int MAX_QUEUE_SIZE = 50;
 
     private Socket socket = null;
     private PrintWriter out = null;
     private BufferedReader in = null;
     private Thread listenThread = null, sendingThread = null;
     private boolean listening = false;
     private boolean closeFlag = false;
     private List<TcpMessageListener> listeners = new LinkedList<TcpMessageListener>();
 
     private final LinkedList<String> requestQueue = new LinkedList<String>();
     private DataOutputStream dos;
     private OnTCPConnectionLostListener onConnLostListener;
 
     public interface TcpMessageListener{
 
         public void onReceive(String request, String response);
     }
 
         public TCPClient(OnTCPConnectionLostListener listener) {
         onConnLostListener = listener;
     }
 
     public interface OnTCPConnectionLostListener {
 
         public void lostConnection();
     }
 
     public void registerListener(TcpMessageListener listener) {
         Log.i(TCPClient.class.getName(), "Registering listener: " + listener);
         listeners.add(listener);
     }
 
     public void unregisterListener(TcpMessageListener listener) {
         Log.i(TCPClient.class.getName(), "Unregistering listener: " + listener);
         listeners.remove(listener);
     }
 
     public String getRemoteHost(){
         if (isConnected()){
             return socket.getInetAddress().toString();
         } else {
             return null;
         }
     }
 
     public boolean isConnected(){
         return socket != null && socket.isConnected();
     }
 
     public boolean connect(String serverIpOrHost, int port) {
         closeFlag = false;
         try {
             socket = new Socket(serverIpOrHost, port);
             out = new PrintWriter(socket.getOutputStream(), true);
             dos = new DataOutputStream(socket.getOutputStream());
             in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             this.listenThread = new Thread(new Runnable() {
                 public void run() {
                     int charsRead = 0;
                     char[] buff = new char[4096];
                     while (listening && charsRead >= 0) {
                         try {
                             charsRead = in.read(buff);
                             if (charsRead > 0) {
                                 Log.d("TCPClient", new String(buff).trim());
                                 String input = new String(buff).trim();
 
                                 onReceivedMessage(input);
 
                             }
                         } catch (IOException e) {
                             Log.e("TCPClient", "IOException while reading input stream");
                             listening = false;
                         }
                     }
                     if (!closeFlag) {
                         connectionLost();
                     }
                 }
 
             });
             this.sendingThread = new Thread(new Runnable() {
                 @Override
                 public void run() {
                     while (TCPClient.this.isConnected()){
                         synchronized (requestQueue) {
                             if (peekQueue()!= null){
                                 Log.i("TCPClient","Sending first from request queue: " + requestQueue.toString()) ;
                                 sendMessage(peekQueue());
                                 try {
                                     Log.i("TCPClient","Sending thread idle state...") ;
                                     requestQueue.wait();
                                 } catch (InterruptedException e) {
                                     e.printStackTrace();
                                 }
                             }
                         }
 
                     }
                 }
             });
 
             this.listening = true;
             this.listenThread.setDaemon(true);
             this.listenThread.start();
             this.sendingThread.start();
 
         } catch (UnknownHostException e) {
             System.err.println("Don't know about host");
             return false;
         } catch (IOException e) {
             System.err.println("Couldn't get I/O for the connection");
             return false;
         } catch (Exception e) {
             System.err.println(e.getMessage());
             return false;
         }
         return true;
     }
 
     private void onReceivedMessage(String message){
         String request = null;
         synchronized ( requestQueue) {
             request = pollQueue();
             requestQueue.notify();
         }
         Log.i("TCPClient", "Received response: " + message);
         for (TcpMessageListener listener : listeners) {
             listener.onReceive(request,message);
         }
     }
 
     private void sendMessage(String msg) {
 
         boolean testMode = true;
         if(testMode && out!=null){
             out.println(msg);
             out.flush();
             return;
         }
         if(dos != null)
         {
             byte[] b = EncodingUtils.getAsciiBytes(msg);
             try {
                 dos.write(b);
                Log.i("TCPClient", "Sending: " + b);
                 dos.flush();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
 
     private void connectionLost() {
         onConnLostListener.lostConnection();
     }
     public void disconnect() {
         closeFlag = true;
         try {
             if(out != null){
                 out.close();
                 out = null;
             }
             if(in != null){
                 in.close();
                 in = null;
             }
             if (socket != null) {
                 socket.close();
                 socket = null;
             }
             if(this.listenThread != null){
                 this.listening = false;
                 this.listenThread.interrupt();
                 this.sendingThread.interrupt();
                 clearQueue();
             }
         } catch (IOException ioe) {
             System.err.println("I/O error in closing connection.");
         }
     }
 
     public void enqueueRequest(String request) {
 
        Log.i("TCP", "Request Queue size: " + requestQueue.size());
        if (requestQueue.size() <= MAX_QUEUE_SIZE)  {
             requestQueue.add(request);
         }
        else {
            clearQueue();
            return;
        }
     }
 
     public String peekQueue() {
         return requestQueue.peek();
     }
 
     public String pollQueue() {
         return requestQueue.poll();
     }
 
     public void clearQueue() {
         requestQueue.clear();
     }
 }
