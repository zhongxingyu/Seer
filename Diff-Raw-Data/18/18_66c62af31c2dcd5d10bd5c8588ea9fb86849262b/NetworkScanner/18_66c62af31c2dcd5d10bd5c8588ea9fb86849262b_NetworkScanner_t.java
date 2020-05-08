 package ch.bfh.bti7301.w2013.battleship.network;
 
 import java.io.*;
 import java.net.*;
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class NetworkScanner {
 	
 	private static final String PLAYERNAME = "INITIAL";
 	private static NetworkScanner instance;
 	private static ArrayList<ScannerSocket> udpSockets;
 
 	private NetworkScanner() throws IOException {
		//getInstance();
 		setSockets();
 		sendUdpMessage(PLAYERNAME);
 	}
 	
 	public static NetworkScanner getInstance()  {
 		if (instance == null) {
 			try {
 				instance = new NetworkScanner();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		return instance;
 	}
 	
 	private void setSockets() throws IOException{
 		udpSockets = new ArrayList<ScannerSocket>();
 		for (InetAddress bc : NetworkInformation.getBroadcastAddresses()){
 			udpSockets.add(new ScannerSocket(bc));
 		}
 		
		System.out.println("udp sockets are set" + udpSockets.toString());
		
 	}
 
 	private String matchIp (String str){
 		
 		String IPADDRESS_PATTERN = 
 		        "(([2]([0-4][0-9]|[5][0-5])|[0-1]?[0-9]?[0-9])[.]){3}(([2]([0-4][0-9]|[5][0-5])|[0-1]?[0-9]?[0-9]))";
 
 		Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
 		Matcher matcher = pattern.matcher(str);
 		        if (matcher.find()) {
 		            return matcher.group();
 		        }
 		        else{
 		            return "0.0.0.0";
 		        }
 		        
 	}
 	
 	private String matchName (String str){
 		
 		String NAME_PATTERN = 
 				"-\\w*$";
 
 		Pattern pattern = Pattern.compile(NAME_PATTERN);
 		Matcher matcher = pattern.matcher(str);
 		        if (matcher.find()) {
 		            return matcher.group();
 		        }
 		        else{
 		            return "no name";
 		        }
 		        
 	}
 	
 	private boolean matchMsg (String str){
 
 		
 		String MSG_PATTERN = 
 		        "(BSG-)(([2]([0-4][0-9]|[5][0-5])|[0-1]?[0-9]?[0-9])[.]){3}(([2]([0-4][0-9]|[5][0-5])|[0-1]?[0-9]?[0-9]))(-\\w*)$";
 
 		Pattern pattern = Pattern.compile(MSG_PATTERN);
 		Matcher matcher = pattern.matcher(str);
 		 return matcher.find();
 		        
 	}
 
 	public void readUdpSocket(String in) {
 		
 		//ArrayList<String> foundOpponent = new ArrayList<>();
 		String ip, name;
 		
 		if (matchMsg(in)){		
 			ip = matchIp(in);
 			name = matchName(in);	
 			
 			Connection.getInstance().foundOpponent(ip,name);
			System.out.println("a upd message was received" + in);
 		}
 		
 	}
 
 	public static void sendUdpMessage(String name) throws IOException {
 		
 		for (ScannerSocket socken : udpSockets)
 			
 			if(NetworkInformation.bcToIp(socken.ip.getHostAddress()) != null){
 				
 				socken.sendMsg("BSG-" + NetworkInformation.bcToIp(socken.ip.getHostAddress()) + "-" + name);
 			}
   
		System.out.println("a upd message was sent");
 
 	}
 	
 	public void cleanUp(){
 		//TODO: close all open sockets.
 	}
 	
 
 }
 
