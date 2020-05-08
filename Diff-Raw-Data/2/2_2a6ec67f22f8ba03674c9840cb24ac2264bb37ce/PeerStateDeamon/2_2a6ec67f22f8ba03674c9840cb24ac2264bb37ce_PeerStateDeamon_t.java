 package com.mycompany.reservationsystem.peer.deamon;
 
 import java.net.InetAddress;
 import java.util.ArrayList;
 import java.util.Date;
 
 import com.mycompany.reservationsystem.peer.data.Peer;
 import com.mycompany.reservationsystem.peer.data.PeerTable;
 
 /*
  * Thread to maintain peer states
 
     Begin
     Connect to database
     Get all inactive ip address
     Disconnect from database
     For each ip address
         If ip address is active
         then connect to DB and set ip address to active
         Disconnect from ip address
     Loop until overall program exits
     End
  */
 public class PeerStateDeamon extends Thread{
 	private final int timeout = 1500;
 	
 	public void run(){
 		while(true){
 			PeerTable peerTable = PeerTable.getInstance();
 			peerTable.connect();
			ArrayList<Peer> peersByState = peerTable.getAllPeers();
 			peerTable.disconnect();
 			
 			for(Peer peer : peersByState){
 				try 
 				{
 					if(InetAddress.getByName(peer.getPeerIpAddress().trim()).isReachable(timeout)){
 						System.out.println("Found " + peer.getPeerIpAddress().trim());
 						peerTable.connect();
 						peer.setState(Peer.STATE.ACTIVE);
 						peer.setEpochTime(new Date().getTime());
 						peerTable.updatePeer(peer);
 						peerTable.disconnect();
 					}
 					else{
 						System.out.println("Can't find " + peer.getPeerIpAddress().trim());
 						peerTable.connect();
 						peer.setState(Peer.STATE.INACTIVE);
 						peer.setEpochTime(new Date().getTime());
 						peerTable.updatePeer(peer);
 						peerTable.disconnect();
 					}
 				} 
 				catch(NullPointerException e){
 					
 				}
 				catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 			yield();
 		}
 	}
 }
