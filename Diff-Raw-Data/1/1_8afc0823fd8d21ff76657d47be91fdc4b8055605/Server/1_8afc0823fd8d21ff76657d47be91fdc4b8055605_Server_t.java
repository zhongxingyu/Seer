 package server;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 
 import main.Connection.Command;
 import main.Message;
 import main.User;
 
 /**
  * Chat server runner.
  */
 
 public class Server{
 	private int nextId;
 	private ServerSocket server;
     private HashMap<Integer, User> userMap; // Maps usernames to users.
     private HashMap<String, Channel> roomMap; // Maps room names to rooms
     
     /**
      * Instantiate a server on the specified port.
      * @param port The port to use for our server
      */
     public Server(int port) {
     	try{
     		server = new ServerSocket(port);
     		userMap = new HashMap<Integer, User>();
     		roomMap = new HashMap<String, Channel>();
     		nextId = 0;
     	}
     	catch(Exception e){
     		e.printStackTrace();   		
     	}
     }
     
     /**
      *  Listen for connections on the port specified in the Server constructor
      */
     public void listen(){
     	try{    		
     		while(true){
 	    		Socket socket = server.accept();
 	    		String nickname = new String("Guest_" + String.valueOf(nextId));
 	    		
 	    		// Create a server connection and connect it to the appropriate user.
 	    		ServerConnection userConnection = new ServerConnection(nextId, socket, this);
 	    		User user = new User(nextId, nickname, userConnection);
 	    		userConnection.setUser(user);
 	    		
 	    		synchronized(userMap){
 	    			userMap.put(nextId, user);
 	    		}
 	    		// Send a response that connection was successful;
 	    		userConnection.sendMessage(new Message(Command.REPLY_SUCCESS, "", Calendar.getInstance(), ""));
 	    		
 	    		nextId++;
     		}
     	}
     	catch(IOException e){
     		e.printStackTrace();
     	}
     }
     
     
     /**
      * Start a chat server.
      */
     public static void main(String[] args) {
         Server server = new Server(1234);
         server.listen();
         
         // YOUR CODE HERE
         // It is not required (or recommended) to implement the server in
         // this runner class.
     }

 }
