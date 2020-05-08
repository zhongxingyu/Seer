 /**
  * see main.game.net.server for other point of connection
  * this class is used by the server to store
  * information about and to control the client
  * @author simon
  */
 
 package main.game.server;
 
import main.game.net.utils.MessageType;
 
 public class Client {
 	
 	//private static final boolean debug_Mode = true;
 	
 	private boolean connected;
 	
 	private String nickname;
 	
 	public Client(){
 		connected = false;
 	}
 
 	public boolean isConnected() {
 		return connected;
 	}
 	
 	public void disconnect() {
 		
 	}
 	
 	// "bind" an incoming login to a client
 	//returning true, if nothing fails
 	public boolean bind(String nick){
 		nickname = nick;
 		System.out.println(nickname + " logging in ...");
 		
 		
 		//if we reach this point, there was/is no Problem
 		connected = true;
 		return true;
 	}
 	
 	//send a message to the client
	public int sendData(MessageType message) {
 		
 		return 0; //everything ok!
 	}
 	
 }
