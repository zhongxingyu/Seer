 package main.java.master;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.Collections;
 
 import org.apache.log4j.Logger;
 
 public class MulticastBeacon implements Runnable {
 
 	static Logger logger = Logger.getLogger(MulticastBeacon.class);
 	
 	boolean run = true;
 	
 	DatagramSocket socket = null;
 
 	byte[] b; // 500 byte are safe
 	DatagramPacket dgram = null;
 	InetAddress localIp = null;
 	
 	
 	public MulticastBeacon() throws UnknownHostException, SocketException {
 		socket = new DatagramSocket();
 
 		// Find my real ip (not localhost)
 		localIp = findLocalIp();
 		
 		b = localIp.getAddress();
 		
 		dgram = new DatagramPacket(b, b.length, InetAddress.getByName("235.1.1.1"), 12345);
 	}
 	
 	public void stop() {
 		run = false;
 	}
 		
 	@Override
 	public void run() {
 		while(run) {
 			
 			try {
 //				logger.info("Beacon Ping...");
 				socket.send(dgram);
 			} catch (IOException e1) {
 				logger.error(e1);
 			}			
 			
 			try {
 				Thread.sleep(2000);
 			} catch (InterruptedException e) {}
 		}
 	}
 
 	private InetAddress findLocalIp() throws SocketException {
 		for(NetworkInterface ifc : Collections.list(NetworkInterface.getNetworkInterfaces())) {
 		   if(ifc.isUp()) {
 		      for(InetAddress addr : Collections.list(ifc.getInetAddresses())) {
		    	  if (!addr.isLoopbackAddress() && addr.isSiteLocalAddress()) {
 		    		  return addr;
 		    	  }
 		      }
 		   }
 		}
 		return null;
 	}
 	
 }
