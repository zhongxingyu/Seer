 package com.eyetribe;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.SocketException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.Semaphore;
 
 import android.util.Log;
 
 public class UDPInput{
 	private ReceiveMessage receiveMessage = new ReceiveMessage();
 	private Thread thread = new Thread(receiveMessage);
 	private DatagramSocket socketToServer;
 	private byte byteArray[] = new byte[50];
 	private String gazeString = "";
 	private List<Listener> listeners = new ArrayList<Listener>();
 	private int port = 8866;
 	private boolean flag = true;
 	private String x;
 	private String y;
 	private String[] gazeData;
 
   public static final String LOG_TAG = "EyeTribeBrowser";
 
 	public UDPInput(){
     thread.start();
 	}
 
   public void read() {
     receiveMessage.read();
   }
 	
 	public void pauseReading(){
     receiveMessage.pauseReading();
 	}
 
   public void close() {
     receiveMessage.stop();
   }
 	
 	public interface Listener {
 	  void handleGazeDataSet(UDPInput sender);
 	}
 	
 	public String getGazeString(){
 		return gazeString;
 	}
 	
 	public String getX(){
 		return x;
 	}
 	
 	public String getY(){
 		return y;
 	}
 	
 	public void sendSignal(){
 		for (Listener listener : listeners) {
       listener.handleGazeDataSet(this);
     }
 	}
 	
 	public class ReceiveMessage implements Runnable{
 		private volatile boolean running = true;
     private volatile boolean reading = false;
 
     private Semaphore readToken = new Semaphore(0);
 
 		public void run() {
 		  while(running){
         try {
           readToken.acquire();
           doRead();
           readToken.release();
         }
         catch(InterruptedException e) {
           // Do nothing.
         }
 			} 
 		}
 
     public void read() {
       if(!reading) {
         try {
           if(socketToServer == null) {
             socketToServer = new DatagramSocket(port);
           }
           readToken.release();
         }
         catch(SocketException e) {
           e.printStackTrace();
         }
         reading = true;
       }
     }
     
     public void pauseReading() {
       if(reading) {
         try {
           readToken.acquire();
         }
         catch(InterruptedException e) {
           // Do nothing.
         }
         if(socketToServer != null) {
           socketToServer.close();
           socketToServer = null;
         }
         reading = false;
       }
     }
 
 		public void stop(){
 			this.running = false;
 		}
 
     private void doRead() {
       try{
         DatagramPacket receivePacket = new DatagramPacket(byteArray, byteArray.length);
         socketToServer.receive(receivePacket);
         gazeString = new String(receivePacket.getData());
         gazeData = gazeString.split(" ");
         x = gazeData[2].replace(",", "."); 
         y = gazeData[3].replace(",", ".");
         sendSignal();
       }
       catch(IOException e){
         e.printStackTrace();
       }
     }
 	}
 	
 	public void addListener(Listener listener) {
 	  this.listeners.add(listener);
 	}
 		 
 	public void removeListener(Listener listener) {
 	  this.listeners.remove(listener);
 	}
 }
