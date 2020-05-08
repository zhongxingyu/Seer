 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map.Entry;
 
 public class Server {
 
 	private static HashMap<ClientRunnable, String> users = new HashMap<ClientRunnable, String>();
 	private static int port;
 	private static int maxUsersOnline = 20;
 	private static String serverName;
 	
 	public static void main(String[] args) {
 
 		ServerSocket s;
 		Socket c;
 		port = 1234;
 		serverName = "Derra";
 		
 		try {
 			s = new ServerSocket(port);
 			s.setReuseAddress(true);
 			s.getInetAddress();
 			
 			String lha = InetAddress.getLocalHost().getHostAddress();
 			System.out.println("Server started at " + lha + ":" + port);
 			
 			while (true) {
 				c = s.accept();
 				ClientRunnable r = new ClientRunnable(c, serverName);
 				Thread ct = new Thread(r);
 				ct.start();
 			}
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 	public static String getServerName(){
 		return serverName;
 	}
 	
 	public static synchronized boolean addUser(ClientRunnable r, String desiredName) {
 			if(usernameExists(desiredName) || maxUsersOnline <= users.size()) {
 				return false;
 			}
 			else {
 				users.put(r, desiredName);
 				sendMessageToAll("400 user registered " + desiredName +"\r\n", serverName);
 				return true;
 			}
 	}
 	
 	public static synchronized void removeUser(ClientRunnable r){
 		sendMessageToAll("400 user gone " + users.get(r) +"\r\n", serverName);
 		users.remove(r);
 	}
 	
 	public static synchronized boolean usernameExists(String name) {
 		for(String clientName : users.values()) {
 			if(name.equalsIgnoreCase(clientName.toString()) || serverName.equalsIgnoreCase(clientName.toString())) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public static synchronized  String clientsList() {
 		String result = "";
 		if(users.size() > 0) {
 			for(String name : users.values()) {
 				result += name + " ";
 			}
			return "200 ok " + result;
 		}
 		else {
 			return "100 err server error!\r\n";
 		}
 	}
 	
 	public static boolean sendMessage(String clientName, String message, String sender){
 		if(!sender.equalsIgnoreCase(serverName)) {
 			message = "<"+sender+"> " + message + "\r\n";
 		}
 		ClientRunnable receiver;
 		Iterator<Entry<ClientRunnable, String>> it = users.entrySet().iterator();
 		while(it.hasNext()) {
 			@SuppressWarnings("rawtypes")
 			Map.Entry pair = (Map.Entry)it.next();
 			if(pair.getValue().toString().equalsIgnoreCase(clientName)) {
 				receiver = (ClientRunnable) pair.getKey();
 				receiver.sendResponse(message);
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	public static boolean sendMessageToAll(String message, String sender) {
 		boolean result = true;
 		if(!sender.equalsIgnoreCase(serverName) || !message.startsWith("400")) {
 			message = "<"+sender+"> " + message + "\r\n";
 		}
 		for(ClientRunnable receiver : users.keySet()) {
 				if(!receiver.sendResponse(message))
 					result = false;
 		}
 		return result;
 	}
 
 }
