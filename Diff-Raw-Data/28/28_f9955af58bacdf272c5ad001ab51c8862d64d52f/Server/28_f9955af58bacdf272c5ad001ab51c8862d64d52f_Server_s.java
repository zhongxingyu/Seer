 package com.test9.irc.engine;
 
 import java.io.IOException;
 import java.net.Socket;
 import java.net.UnknownHostException;
import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 public class Server {
 
     private final String name;
     private ServerListener listener;
     private ServerSender sender;
     private Socket socket = null;
     public boolean isConnected = false;
 
     // The server to connect to and our details.
     String server = "irc.ecsig.com";
    String botRef = "tar";
 
     String nick = this.botRef + "";
    String login = this.botRef + "-bot";
     
     /**
      * This should not be located in server, but it will for now.
      */
     public static final String RNtail = "\r\n";
 
     // The channel which the bot will join.
    private List<Channel> channels = Arrays.asList(new Channel("#jircc"), new Channel("#jared"));
 
     public Server(Boolean DEBUGGING, String name) {
         this.name = name;
 
         // Try to connect to the server at some string server address
         try {
             // Connect directly to the IRC server.
             this.socket = new Socket(this.server, 6667);
 
             // Apply socket to the sender and listener.
             this.listener = new ServerListener(this);
             this.sender = new ServerSender(this);        
 
             // Start the listener thread
             this.listener.start();
             if (DEBUGGING) {
                 System.out.println("Listener started.");
             }
 
             // Start the sender thread
             this.sender.start();
             if (DEBUGGING) {
                 System.out.println("Sender started.");
             }
 
         } catch (UnknownHostException e) {
             System.err.println("Host not recognized: " + this.server);
             System.exit(1);
         } catch (IOException e) {
             System.err.println("Couldn't get I/O for " + "the connection to: "
                     + this.server);
             System.exit(1);
         }
     }
     
     public void sendInitialMessage(){
     	
     		System.out.println("Sending Initial MESSAGE");
 			sender.run();
     }
 
     public void sendPong(String msg) {
         if (this.sender != null) {
             this.sender.ping_pong(msg);
         }
     }
 
     public List<Channel> getChannels() {
         return this.channels;
     }
 
     public String getName() {
         return this.name;
     }
 
     public Socket getSocket() {
         return this.socket;
     }
 
     public String getBotRef() {
         return this.botRef;
     }
 
     public String getNick() {
         return this.nick;
     }
 
     public String getLogin() {
         return this.login;
     }
 }
