 package main;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 import main.User;
 
 
 /**
  * A custom class to handle the incoming and outgoing Packets in a Socket.  
  * There are two subclasses, ServerConnection and ClientConnection
  * which handle the Sockets on the Server and Client side respectively.  
  *
  */
 
 public class Connection {
 		protected Socket socket;
 		protected User user;
 		protected Thread readerThread;
 		protected Thread writerThread;
 		protected boolean userDisconnected = false;
 		
 		// Messages to be sent across the socket
 		protected BlockingQueue<Packet> messageQueue;
 		
 		public enum Command{ // Quit exits a channel, logout exits connection
 			LIST_USERS, LIST_CHANNELS, LOGOUT, LOGIN, MESSAGE, JOIN, QUIT,
 			REPLY_SUCCESS, REPLY_FAILURE, REPLY_ERROR, REPLY_LIST_CHANNELS, 
 			REPLY_LIST_CHANNEL_USERS, REPLY_LIST_USERS 
 		};
 		
 		/**
 		 * ConnectionReader continuously reads in the next Packet from 
 		 * the incoming stream and processes it.  
 		 *
 		 */
 		public class ConnectionReader implements Runnable {
 			private Connection connection;
 			
 			public ConnectionReader(Connection c){
 				this.connection = c;
 			}
 			@Override
 			public void run() {
 				ObjectInputStream ois = null;
 				try {
 					ois = new ObjectInputStream(socket.getInputStream());
 				} catch (IOException e1) {
 					e1.printStackTrace();
 				}
 				
 				try{
 					while(!userDisconnected && !Thread.currentThread().isInterrupted()){
 						// Make sure that we're dealing with a valid socket;
 						
 						Packet nextMessage = (Packet)ois.readObject();
 						processMessage(nextMessage);
 					}
 					if(userDisconnected){
 						processUserDisconnect();
 					}
 				}
 				catch(IOException e){
 					connection.processUserDisconnect();
 				}
 				catch(Exception e){
 					e.printStackTrace();
 				}
 			}
 			
 		}
 		
 		/**
 		 * ConnectionWriter continuously takes Packets from the messageQueue to 
 		 * write to the outgoing stream.  
 		 *
 		 */
 		public class ConnectionWriter implements Runnable {
 			private Connection connection;
 			
 			public ConnectionWriter(Connection c){
 				this.connection = c;
 			}
 			
 			@Override
 			public void run() {
 				ObjectOutputStream oos = null;
 				try {
 					oos = new ObjectOutputStream(socket.getOutputStream());
 				} catch (IOException e1) {
 					e1.printStackTrace();
 				}
 				
 				// Run until we are interrupted - read messages from the queue and write them onto our socket.
 				while(!userDisconnected && !Thread.currentThread().isInterrupted()){
 					if(!messageQueue.isEmpty()){
 						Packet m = messageQueue.poll();
 						try {
 							oos.writeObject(m);
 						} catch (IOException e) {
 							e.printStackTrace();
 						}
 					}
 					try {
 						Thread.sleep(20);
 					} catch (InterruptedException e) {
 						continue;
 					}
 				}
 			}
 		
 		}
 		
 		public Connection(Socket sock){
 			if(!sock.isConnected())
 	    		throw new RuntimeException("Socket not connected");
 			
 			this.messageQueue = new LinkedBlockingQueue<Packet>();
 			this.socket = sock;
 			ConnectionReader reader = new ConnectionReader(this);
 			ConnectionWriter writer = new ConnectionWriter(this);
 			readerThread = new Thread(reader);
 			readerThread.start();
 			writerThread = new Thread(writer);
 			writerThread.start();
 		}
 		
 		//Process a Packet; overriden by subclasses.  
 		//Usually calls sendMessage() method at the end to place a reply Packet on the queue.  
 		public void processMessage(Packet message){
 			System.out.println("Dummy processMessage message in Connection");
 		}
 		
 		//Place a Packet onto the Connection's messageQueue to eventually write out.
 		public void sendMessage(Packet output){
 			messageQueue.offer(output);
 		}
 		
 		public void closeConnection(){
 			writerThread.interrupt();
 		}
 		
 		public User getUser(){
 			return this.user;
 		}
 		
 		public void setUser(User user){
 			this.user = user;
 		}
 		
 		public void processUserDisconnect(){
 			// Overwritten in child class
 		}
 }
