 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
import java.util.TreeMap;
 
 public class WServe implements Runnable {
 
	private TreeMap<Integer, WClientHandler> Users = new TreeMap<Integer, WClientHandler>();;
 	private int id = 0;
 	private int defaultPort = 11111;
 
 	public WServe() {}
 
 	private int getNextID(){
 		return id++;
 	}
 
 	public void run() {
 		ServerSocket sock;
 
 		try {
 			sock = new ServerSocket(defaultPort);
 			System.out.println("Server started.");
 		}
 		catch (IOException e) {
 			System.err.println("Cannot open port " + defaultPort);
 			return;
 		}
 		while (true) {
 			try {
 				Socket client = sock.accept();
 				Integer id = getNextID();
 				WClientHandler handler = new WClientHandler(this, client, new WUser(id,"user"+id));
 				Users.put(id, handler);
 				new Thread(handler).start();
 			}
 			catch (Exception e) {
 				System.err.println(e.getMessage());
 			}
 		}
 	}
 
 	public void broadcast(String message) {
 		for (WClientHandler u: Users.values()) {
 			u.sendMessage(message);
 			System.out.println("Broadcast: " + message);
 		}
 	}
 	
 	public void broadcastTo(Integer id, String message) {
 		WClientHandler who = Users.get(id);
 		who.sendMessage(message);
 		System.out.println("Message to: "+who.getNick()+":"+message);
 	}
 
 	public void broadcastExcept(Integer id, String message) {
 		WClientHandler who = Users.get(id);
 		System.out.println(who.getNick() + " " + message);
 		for (WClientHandler u: Users.values()) {
 			if(u != who) {
 				u.sendMessage(message);
 			}
 		}
 	}
 
 	public void setName(WClientHandler client, String name) {
 		client.iAm(name);
 		Users.put(id,client); //putting existing id overwrites previous entry
 		broadcastExcept(id, "User joined: " + name);
 	}
 	
 	public String getUsers(){
 		String allUsers = "users ";
 		
 		for (WClientHandler u: Users.values()) {
 			allUsers = allUsers + "#" + u.getId() + " " + u.getNick();
 		}
 		
 		return allUsers;
 	}
 
 	public void removeUser(Integer id){
 		try{
 			WClientHandler user = Users.remove(id);
 			if (user == null) return;
 			user.exit();
 			broadcast("User left: " + user.getNick());
 		}
 
 		catch (Exception e) {
 			System.err.println(e.getMessage());
 		}
 	}
 
 	public static void main(String[] args){
 		System.out.println("Starting server.");
 		new Thread(new WServe()).start();
 	}
 }
 
