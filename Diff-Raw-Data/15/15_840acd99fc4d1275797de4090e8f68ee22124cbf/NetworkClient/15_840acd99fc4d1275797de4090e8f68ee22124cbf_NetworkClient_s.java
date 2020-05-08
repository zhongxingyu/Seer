 import java.io.*;
 import java.net.*;
 import java.util.*;
 
 public class NetworkClient {
 	
 	public static void main(String args[]){
 		NetworkClient netcli = new NetworkClient();
 		Player player = (Player) netcli.fetch();
 		if(player!=null){
 			System.out.println("Success!");
 		}
 	}
 	
 	public NetworkClient(){
 	}
 
 	public Object fetch() {
 		ObjectOutputStream oos = null;
 		ObjectInputStream ois = null;
 
 		Socket socket = null;
 
 		Player opponent = null;
 		try {
			socket = new Socket("localhost", 3000);
 
 			oos = new ObjectOutputStream(socket.getOutputStream());
 			ois = new ObjectInputStream(socket.getInputStream());
 
 			opponent = (Player) ois.readObject();
 
 			//opponent = (Player) ois.readObject();
 
 			//System.out.println("Opponent ("+opponent.getName()+") position: x: "+opponent.getX()+" y: "+opponent.getY());
 
 			oos.close();
 			ois.close();
 
 		} catch(Exception e) {
 			System.out.println(e.getMessage());
 		}
 		return opponent;
 	}
 }
