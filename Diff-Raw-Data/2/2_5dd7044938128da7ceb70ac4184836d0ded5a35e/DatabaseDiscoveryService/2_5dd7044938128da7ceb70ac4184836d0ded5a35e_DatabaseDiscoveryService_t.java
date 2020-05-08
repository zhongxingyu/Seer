 package de.reneruck.tcd.ipp.database;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 
 import de.reneruck.tcd.datamodel.Statics;
 
 public class DatabaseDiscoveryService extends Thread {
 
 	private boolean running;
 	private DatagramSocket socket;
 
 	public DatabaseDiscoveryService() {
 		System.out.println("Starting DatabaseDiscoveryServiceHandler");
 	}
 
 	@Override
 	public void run() {
 		System.out.println("Starting broadcasting service");
 		while (this.running) {
 			if (this.socket != null) {
 				sendBroadcast();
 			} else {
 				setupSocket();
 			}
 		}
 	}
 
 	private void setupSocket() {
 		try {
 			this.socket = new DatagramSocket();
 		} catch (SocketException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void sendBroadcast() {
 		try {
 			InetAddress group = InetAddress.getByName("230.0.0.1");
			DatagramPacket packet = new DatagramPacket(new byte[100], 100, group, Statics.DISCOVERY_PORT);
 			this.socket.send(packet);
 			
 			Thread.sleep(5000);
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private InetAddress getBroadcastAdress() {
 		try {
 			InetAddress localhost = InetAddress.getLocalHost();
 			byte[] address = localhost.getAddress();
 			address[3] = -1;
 			return InetAddress.getByAddress(address);
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public boolean isRunning() {
 		return running;
 	}
 
 	public void setRunning(boolean running) {
 		this.running = running;
 	}
 }
