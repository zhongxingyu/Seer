 import java.net.ServerSocket;
 import java.io.IOException;
 
 public class ChatServer {
     private ServerSocket socket;
     
     public ChatServer(int port) {
 	try {
 	    socket = new ServerSocket(port);
 	}
 	catch(IOException e) {
 	    System.err.println("error: " + e);
 	}
     }
 
     public void listen() {
 	while(true) {
 	    try {
		new ClientHandler(socket.accept()).start();
 	    }
 	    catch(IOException e) {
 		System.err.println("error: " + e);
 	    }
 	}
     }
 }
