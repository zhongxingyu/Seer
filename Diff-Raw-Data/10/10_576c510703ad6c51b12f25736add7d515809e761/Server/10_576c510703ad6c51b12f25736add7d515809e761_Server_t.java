 package server;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.HashMap;
 
 
 import common.ServerPacket;
 import common.ServerWorkflow;
 import common.Status;
 
 public class Server extends Thread{
 	public static ServerSocket socket;
 	
 	public static HashMap<String, ClientInfo> activeUsers = new HashMap<String, ClientInfo>();
 	
 	
 	public static void removeActiveUser(String username){
 		activeUsers.remove(username);
 	}
 	
 	public static void main(String[] args){
 	    try{
 	    	socket = new ServerSocket(8010);
 	      
 	    	while(true){ 		
 	    		// Accepts unique incoming connection
 	    		Socket connection  = socket.accept();
 	    		Server s = new Server( connection );
 	    		s.start();
 	    	}
 	    }catch(Exception e){
 	    	System.err.print("Gotta catch them all.");
 	    	try {
 				socket.close();
 			} catch (IOException e1) {
 				e1.printStackTrace();
 			}
 	    }
 	}
 	
 	private ServerPacket sp;
 	private Socket clientConn;
 	private String username;
 	private ObjectInputStream ois;
 	private ObjectOutputStream oos;
 	
 	public Server( Socket connection ){
 		this.clientConn = connection;
 		
 		try {
 			ois = new ObjectInputStream(clientConn.getInputStream() );
 			oos = new ObjectOutputStream( clientConn.getOutputStream() );
 			sp = (ServerPacket) ois.readObject();
 		} catch( ClassNotFoundException cnfe ){
 			cnfe.printStackTrace();
 		} catch( IOException e ) {
 			e.printStackTrace();
 		}
 		this.username = sp.getUsername();		
 		ClientInfo information;
 		if( activeUsers.containsKey( username ) ){
 			information = activeUsers.get( username );
 		}else{
 			information = new ClientInfo();
 			activeUsers.put( username, information);	
 		}
 		
 	}
 	
 	public void run(){
 		while( sp.getWorkflowType() != ServerWorkflow.statusChange || sp.getStatus() != Status.offline ){
 			switch( sp.getWorkflowType() ){
 				case getIP:
 					System.out.println("Getting IP for " + sp.getUsername());
					System.out.println("IP: " + activeUsers.get( sp.getUsername() ).getIP() );
 					sp.setIP( activeUsers.get( username ).getIP() );
 					try {
 						oos.writeObject( sp );
 					} catch (IOException e) {
 						System.err.println( e.getMessage() );
 					}
 					
 					break;
 				
 				case editBuddy:
 					if( sp.getOperation() == ServerPacket.add ){
 						System.out.println("Adding " + sp.getBuddyName() + " to " + sp.getUsername() + "'s buddy list");
 						activeUsers.get( sp.getBuddyName() ).addUserToNotifyList( sp.getUsername() );
 					}else{
 						System.out.println("Removing " + sp.getBuddyName() + " to " + sp.getUsername() + "'s buddy list");
 						activeUsers.get( sp.getBuddyName() ).removeUserFromNotifyList( sp.getUsername() );
 					}
 					break;
 					
 				case statusChange:
 					if ( sp.getStatus() == Status.online ||
 							sp.getStatus() == Status.away ){
						ClientInfo info = activeUsers.get( username );
						info.setIP( clientConn.getInetAddress() );
						activeUsers.put( username, info );
 					}
 					System.out.println("Changing " + sp.getUsername() + " status to " + sp.getStatus());
 					
 					activeUsers.get( username ).changeStatus( sp.getStatus() );
 					for( String thisUser : activeUsers.get( username ).getNotifyList() ){
 						if( activeUsers.get( thisUser ).getStatus() != Status.offline ){
 							/*try {
 								Socket toNotify = new Socket( activeUsers.get( thisUser ).getIP(), Comm.SERVER_PORT );
 								ObjectOutputStream out = new ObjectOutputStream( toNotify.getOutputStream() );
 								out.writeObject( sp );
 								out.close();
 							} catch (IOException e) {
 								System.err.println( e.getMessage() );
 							}*/
 							
 						}
 					}
 					break;
 			}
 			try {
 				//while( ois.available() == 0 ){
 				//}
 				sp = (ServerPacket) ois.readObject();
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			}
 		}
 		try {
 			clientConn.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
