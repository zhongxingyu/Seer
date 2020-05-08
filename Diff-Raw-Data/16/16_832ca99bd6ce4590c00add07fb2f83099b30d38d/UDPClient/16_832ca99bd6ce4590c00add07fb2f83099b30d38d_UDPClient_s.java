 package devin.udp;
 
 import java.io.*;
 import java.net.*;
 
 public class UDPClient {
 	public static void main(String args[]) throws Exception {
 		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
 		DatagramSocket clientSocket = new DatagramSocket();
		//InetAddress IPAddress = InetAddress.getByName("localhost");
		InetAddress IPAddress = InetAddress.getByName("192.168.252.107");
 		
 		System.out.println("IP Address: " + IPAddress);
 		byte[] sendData = new byte[1024];
 		byte[] receiveData = new byte[1024];
 		String sentence = inFromUser.readLine();
 		System.out.println("User typed: " + sentence);
 		sendData = sentence.getBytes();
 		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 10001);
 		clientSocket.send(sendPacket);
 		System.out.println("Send data");
 		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
 		clientSocket.receive(receivePacket);
 		String modifiedSentence = new String(receivePacket.getData());
 		System.out.println("FROM SERVER:" + modifiedSentence);
 		clientSocket.close();
 	}
 }
