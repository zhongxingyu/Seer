 package network;
 
 
 
 import java.io.BufferedReader;
 import java.io.DataOutput;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import events.EventListener;
 import events.MessageRecivedEvent;
 
 public class Client {
 	
 	/*
 	 * Packets
 	 * 1= string
 	 * 2= file num packets
 	 * 3 = packet
 	 */
 	private static final String PATTERN = 
 	        "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
 	        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
 	        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
 	        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
 	public static final int SOCKET=1238,PACKET_LENGTH=1000;
 	private InputStream in;
 	private volatile DataOutputStream out;
 	private volatile Socket socket;
 	private CommunicationHandler communicationHandler;
 	private String username="";
 	private List _listeners = new ArrayList();
 	private int timeout    = 10000;
 	private int maxTimeout = 25000;
 
 	
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
 			serverSocket.close();
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
 		s=username+": "+s;
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
 	
 	public void setUsername(String s){
 		username=s;
 	}
 	
 	public String getUsername(){
 		return username;
 	}
 	
 	
 
 	public static boolean validateIP(final String ip){          
 
 	      Pattern pattern = Pattern.compile(PATTERN);
 	      Matcher matcher = pattern.matcher(ip);
 	      return matcher.matches();             
 	}
 	
 	private synchronized void fireRecivedEvent(String s){
 		MessageRecivedEvent event = new MessageRecivedEvent(s);
 		Iterator i = _listeners.iterator();
 		while(i.hasNext())
 			((EventListener) i.next()).handleMessageRecivedEvent(event);
 	}
 	
 	public synchronized void addEventListener(EventListener e){
 		_listeners.add(e);
 	}
 	public synchronized void removeEventListener(EventListener e){
 		_listeners.remove(e);
 	}
 	
 	
 	
 	class CommunicationHandler extends Thread{
 		volatile long lastReadTime;
 		public void run(){
 			byte[] buffer = new byte[PACKET_LENGTH];  
 	   	    lastReadTime = System.currentTimeMillis();
 			while(true){
 				try {
 					in.read(buffer);
 					System.out.println("got a packet");
 					if(buffer[0]==1){
 						String temp = new String(buffer,1,PACKET_LENGTH-1);
 						fireRecivedEvent(temp);
 					}
				//	if(isConnectionAlive())
				//		break;
 					
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 		
 		public boolean isConnectionAlive() {
 		    return System.currentTimeMillis() - lastReadTime < maxTimeout;
 		}
 		
 	}
 	
 
 }
