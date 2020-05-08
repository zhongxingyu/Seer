 package failure.detector;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.SocketException;
 import java.util.*;
 
 public class ReceivePingThread extends Thread{
 	
 	private int myId;
 	private int myPort;
 	
 	public ReceivePingThread(int id, int port, List<Integer> peers){
 		this.myId = id;
 		this.myPort = port;
 	}
 	public void run(){
         DatagramSocket serverSocket = null;
 		try {
 			serverSocket = new DatagramSocket(this.myPort);
 			serverSocket.setSoTimeout(0);
 		} catch (SocketException e) {
 			// TODO Auto-generated catch block
			utils.Output.println("Receive Thread " + e.getMessage());
 		}
         while(true){
         	byte[] receiveData = new byte[1024];
         	DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
         	try {
         		try{
         			serverSocket.receive(receivePacket);
         		}
         		catch (IOException e) {
         			utils.Output.println("Fail to receive PING: " + e.getMessage());
             		continue;
             	}
         		String sentence = new String( receivePacket.getData());
         		utils.Output.println("RECEIVED: " + sentence);
         		InetAddress IPAddress = receivePacket.getAddress();
         		int port = receivePacket.getPort();
         		String ackData = "Ack " + this.myId;
         		DatagramPacket sendPacket =new DatagramPacket(ackData.getBytes(), ackData.length(), IPAddress, port);
         		try{
         			serverSocket.send(sendPacket);
             	} catch (IOException e) {
         			// Ignore client timeout
             		utils.Output.println("Fail to send ACK: " + e.getMessage());
             	}
         	} catch (Exception e) {
         		utils.Output.println("Failure in Receive Thread: " + e.getMessage());
         	}
        }
 	}
 }
