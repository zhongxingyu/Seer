 package mnet;
 
 import java.io.IOException;
 import java.net.*;
 public class DHCPServer {
 	
 	private DatagramSocket socket;
 	private DatagramPacket packet;
 	private byte[] data;
 	private final static int MAX_LEN = 2048;
 	public DHCPServer() throws SocketException{
 		try{
 			socket = new DatagramSocket(67);
 			System.out.println("DHCP Server Created.\n" +
 					   "Use capturePacket to capture an incoming request.");
 		}catch(SocketException e){
 			System.out.println("\nAn error occurred creating socket on port 67.\n" + 
 					"If running under linux, try using sudo.\n");
 			throw e;
 		}
 	}
 	public void capturePacket(){
 		byte[] temp = new byte[MAX_LEN];
 		DatagramPacket packet = new DatagramPacket(temp, MAX_LEN);
 		System.out.println("Listening started on port 67...");
 		try{
 			socket.receive(packet);
 			System.out.println("Packet captured.");
 			int len = packet.getLength();
 			data = java.util.Arrays.copyOf(temp, len);
 		}catch(IOException e){e.printStackTrace();}
 	}
	public byte[] getData(){
		return data;
	}
 }
