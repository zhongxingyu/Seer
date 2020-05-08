 package uk.ac.cam.md481.fjava.tick4;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import uk.ac.cam.cl.fjava.messages.Message;
 
 public class ChatServer {
   public static void main(String[] args){
     Integer port;
     ServerSocket socket;
     MultiMessageQueue<Message> queue = new MultiMessageQueue<Message>();
     
     try {
       if(args.length != 1) throw new IllegalArgumentException();
       port = Integer.parseInt(args[0]);
     } catch(IllegalArgumentException e){
       System.err.println("Usage: java ChatServer <port>");
       return;
     }
     
     try {
       socket = new ServerSocket(port);
       Socket client;
       while((client = socket.accept()) != null){
         new ClientHandler(client, queue);
       }
      socket.accept();
     } catch(IOException e){
       System.err.println("Cannot use port number " + port);
     }
   }
 }
