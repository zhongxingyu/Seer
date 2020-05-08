 package ch.bfh.bti7301.w2013.battleship.network;
 
 import java.io.IOException;
import java.io.UnsupportedEncodingException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 public class DiscoverySocketListener extends Thread {
 
 	public DatagramSocket udpSocket;
 
 	DiscoverySocketListener(DatagramSocket socket) {
 		this.udpSocket = socket;
 		start();
 	}
 
 	public void run() {
 		
 		byte[] buf = new byte[256];
 		DatagramPacket packetIn = new DatagramPacket(buf, buf.length);
 		while (!udpSocket.isClosed()) {
 						
 			try {
 				udpSocket.receive(packetIn);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			String ip = packetIn.getAddress().getHostAddress();
			String data;
			try {
				data = new String(packetIn.getData(), packetIn.getOffset(), packetIn.getLength(), "utf-8");
				System.out.println("fresh from Incomeing Packet:" + data);
				NetworkScanner.getInstance().readUdpSocket(ip,data);
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
 		}
 		
 		udpSocket.close();
 		
 	}
 
 }
 
 
 	
