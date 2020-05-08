 package app.server.udpservers;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Date;
 
 import packet.AliveRequest;
 
 import app.server.RetailStoreServerImpl;
 import app.server.RetailStoreServerImpl.GroupMember;
import udp.FIFOObjectUDPServlet;
 
public class FailureDetectorObjectUDPServer extends FIFOObjectUDPServlet<RetailStoreServerImpl> {
 
   private class ServerDetails {
       private int id;
       private Timestamp timestamp; 
       private int attempts;
 
       public void resetAttempts() { attempts = 0; }
       public void increaseAttempts() { attempts += 1; }
 
       public int getId() { return id; }
       public Timestamp getTimestamp() { return timestamp; }
       public boolean hadFailed() { return attempts > 3; }
   }
 
   private final int INTERVAL = 50;
   private ArrayList<ServerDetails> servers = new ArrayList<ServerDetails>();
   private Thread receiveImAliveThread = new Thread(receiveImAlive());
   private Thread sendImAliveThread 	  = new Thread(sendImAlive());
   
 	public FailureDetectorObjectUDPServer(int port, RetailStoreServerImpl owner) {
 		super(port, owner);
 	}
 	
 	public void run() {
 		receiveImAliveThread.run();
 		sendImAliveThread.run();		
 	}
 	
 	public Runnable receiveImAlive() {
 		while (true) {
 			Object obj = receive();
 			
 			for (ServerDetails s : servers) {
 				if (s.getId() == AliveRequest.class.cast(obj).getId()) { s.resetAttempts();	}
 								
 				Date date			= new java.util.Date();
 				Timestamp timestamp = new Timestamp(date.getTime());
 				
 				if (timestamp.compareTo(s.getTimestamp()) >  INTERVAL) {
 					s.increaseAttempts();
 					if (s.hadFailed()) {
 						GroupMember groupMember =  getOwner().getGroupMap().get(s.getId());						
 						groupMember.setToFailed();
 						if (groupMember.isLeader()) {
 							// if s is leader, trigger election							
 						}
 					}
 				}
 			}
 						
 		}
 	}
 	
 	public Runnable sendImAlive() {
 		long start =  System.currentTimeMillis();
 		long end   =  System.currentTimeMillis();
 		
 		while (true) {
 			end = System.currentTimeMillis();
 			if (end - start > INTERVAL) {
 				AliveRequest request = new AliveRequest();
 				request.setId(getOwner().getId());
 				getOwner().broadcast(request);
 				start = System.currentTimeMillis();
 			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
 		}
 		
 	}
 }
