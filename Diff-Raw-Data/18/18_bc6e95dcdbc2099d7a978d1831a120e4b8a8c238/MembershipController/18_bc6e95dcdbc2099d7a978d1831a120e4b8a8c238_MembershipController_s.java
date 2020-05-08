 import java.io.IOException;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Date;
 
 import javax.xml.bind.JAXBException;
 
 
 
 
 public class MembershipController {
 
     //private static int contactPort = 61233;
     //private static int failSeconds = 5;
 	
     public static void sendJoinGroup(String contactIP, int contactPort) throws JAXBException {
 		
         String msg = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<join></join>\n";
         
         try {
             Supplier.send(contactIP, contactPort, msg);
         } catch (IOException e) {
             e.printStackTrace();
         }    
     }
 	
     public static void sendLeaveGroup(String contactIP, int contactPort) {	
 		
         String msg = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<leave></leave>\n";
         
         try {
             Supplier.send(contactIP, contactPort, msg);
         } catch (IOException e) {
             e.printStackTrace();
         }  
 		
     }
 	
     public static void sendGossip(MembershipList list, String contactIP, int contactPort, String ownIP) throws SocketException, UnknownHostException, JAXBException {
 		
         ArrayList<MembershipEntry> memList = list.get();
						
        //Randomly pick nodes to send the gossip to. Total of n/2+1 nodes, but avoid our own IP in ownIP

         //If we're the only one in the group that we know of, let's join!
         if(memList.size() <= 1) {
             if(!contactIP.equals(ownIP)) {
                 System.out.println("Joining! " + ownIP + " -> " + contactIP);
                 sendJoinGroup(contactIP, contactPort);
             }            
             return;
        }
            
         
         for(int i = 0;i < memList.size()/2+1;i++) {
             int randNum = (int)(Math.random() * ((memList.size()-1) + 1));
 			
             if(randNum == -1)
                 return;
             
             MembershipEntry mE = memList.get(randNum);
             String selectedIP = mE.getIPAddress();
             
             if(selectedIP.equals(ownIP)) {
                 i = i-1;
                 continue;
             }
             
 			
             String marshalledMessage = DstrMarshaller.toXML(memList);
 			
             try {
                 Supplier.send(selectedIP, contactPort, marshalledMessage);
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
 	
 	
     public static void updateMembershipList(MembershipList own, ArrayList<MembershipEntry> receivedMemList) {
     	ArrayList<MembershipEntry> ownMemList = own.get();
         
     	for(int i = 0;i < receivedMemList.size();i++) {
             
             //Keep track of whether or not we're already tracking each node in the received member list.
             boolean ownMemListContainsReceived = false;
 
             for(int j = 0;j < ownMemList.size();j++) {
         
                 String receivedIP = receivedMemList.get(i).getIPAddress();
                 long receivedJoinedtstamp = receivedMemList.get(i).getJoinedtstamp();
         		
                 String ownListIP = ownMemList.get(j).getIPAddress();
                 long ownListJoinedtstamp = ownMemList.get(j).getJoinedtstamp();
         	
                 
                 
                 //If IP and joinedTsmp of the received entry are the same as in our list, the entry exists
                 // and we check if there're any updates to do.
                 if(receivedIP.equals(ownListIP) && receivedJoinedtstamp == ownListJoinedtstamp) {
 
                     ownMemListContainsReceived = true;
 
                     int recListHeartbeat = receivedMemList.get(i).getHeartbeatCounter();
                     int ownListHeartbeat = ownMemList.get(j).getHeartbeatCounter();
         			
                     //If the heartbeat of the received list is higher, we update our own list.
                     if(recListHeartbeat > ownListHeartbeat) {
                         ownMemList.get(j).setHeartbeat(recListHeartbeat);
                         long currentTime = new Date().getTime()/1000;
                         ownMemList.get(j).setLastUpdTstamp(currentTime);
                     }
                     break;
                 }
             }
             
             // If we are at the end of our own list and we didn't find an entry in our own list but it appears in the
             // received list, we add it.
             if(!ownMemListContainsReceived && !receivedMemList.get(i).getFailedFlag()) {
                 System.out.println(receivedMemList.get(i).getIPAddress() + " is not in our list. Adding.");
                 long currentTime = new Date().getTime()/1000;
                 ownMemList.add(receivedMemList.get(i));
             }
     	}	
     }
     
     public static void trackFailing(MembershipList own, int failSeconds) {
         //In this loop, we mark all nodes as failed which are older than currentTime minus failSeconds.
     	//If a node is marked as failed and the lastUpdate timestamp is older than currentTime - (failSeconds * 2) sec, it is deleted.
 
         ArrayList<MembershipEntry> ownMemList = own.get();
     	
     	for(int i = 0;i < ownMemList.size();i++) {
             long currentTime = new Date().getTime()/1000;
             long lastUpdate = ownMemList.get(i).getLastupdtstamp();
             boolean failedFlag = ownMemList.get(i).getFailedFlag();
             
             if(currentTime - (failSeconds * 2)  > lastUpdate && failedFlag == true) {
                 ownMemList.remove(i);
                 continue;
     		
             } else if(currentTime - failSeconds > lastUpdate) {
                 ownMemList.get(i).setFailedFlag(true);
             }
             System.out.print(own.get().get(i).getIPAddress() + "(" + !own.get().get(i).getFailedFlag() + ") ");
     	}
         System.out.println("");
     }
 
 }
