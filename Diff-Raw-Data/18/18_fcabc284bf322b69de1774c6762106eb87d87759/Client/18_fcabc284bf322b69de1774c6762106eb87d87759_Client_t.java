 package network;
 
 
 
 import java.io.BufferedReader;
 import java.io.DataOutput;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 public class Client {
 	
 	/*
 	 * Packets
 	 * 1= string
 	 * 2= file num packets
 	 * 3 = packet
 	 */
 	
 	public static final int SOCKET=1238,PACKET_LENGTH=1000;
 	private InputStream in;
 	private volatile DataOutputStream out;
 	private volatile Socket socket;
 	private CommunicationHandler communicationHandler;
 	
 	public void init(String ip){
 		try {
 			socket= new Socket(ip,SOCKET);
 			in = socket.getInputStream();
 			out = new DataOutputStream(socket.getOutputStream());
 			
 			communicationHandler = new CommunicationHandler(); 
 			communicationHandler.start();
 			sendMessage("hi test");
 			
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void init(){
 		try {
 			ServerSocket serverSocket = new ServerSocket(SOCKET);
 			socket = serverSocket.accept();
 			in = socket.getInputStream();
 			out = new DataOutputStream(socket.getOutputStream());
 			
 			communicationHandler = new CommunicationHandler(); 
 			communicationHandler.start();
 		} catch (IOException e) {
 			System.out.println("Could not connect on port :"+SOCKET);
 			e.printStackTrace();
 		}
 	}
 	
 	public void sendMessage(String s){
 		byte[] stringInBytes=s.getBytes();
 		byte[] buffer= new byte[PACKET_LENGTH];
 		buffer[0]=1;
 		
 		if(stringInBytes.length<PACKET_LENGTH){
			System.arraycopy(stringInBytes, 0, buffer, 1, stringInBytes.length);
 			sendPacket(buffer);
 		}
 		else{
 			for(int i=1;i<stringInBytes.length;i+=PACKET_LENGTH-1){
				System.arraycopy(stringInBytes, i-1, buffer, 1, PACKET_LENGTH-1);
 				sendPacket(buffer);
 			}
 		}
 	}
 	
 	public void sendPacket(byte[] packet){
 		try {
 			socket.getOutputStream().write(packet);
 			socket.getOutputStream().flush();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 	
 	class CommunicationHandler extends Thread{
 		
 		public void run(){
 			byte[] buffer = new byte[PACKET_LENGTH];  
 			while(true){
 				try {
 					in.read(buffer);
 					System.out.println("got a packet");
 					if(buffer[0]==1){
 						String temp = new String(buffer,1,PACKET_LENGTH-1);
 						System.out.println(temp);
 					}
 					else if(buffer[0]==-1)
 						break;
 					
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 		
 	}
 	
 
 }
