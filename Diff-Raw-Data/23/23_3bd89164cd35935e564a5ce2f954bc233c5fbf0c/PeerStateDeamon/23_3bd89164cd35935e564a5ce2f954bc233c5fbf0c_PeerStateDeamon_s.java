 package com.mycompany.reservationsystem.peer.deamon;
 
 import java.net.InetAddress;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Random;
 
 import com.mycompany.reservationsystem.peer.data.Database;
 import com.mycompany.reservationsystem.peer.data.Peer;
 
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
 			Database peerTable = Database.getInstance();
 			ArrayList<Peer> peersByState = peerTable.findPeersByState(Peer.STATE.INACTIVE);
 			System.out.println("Deamon");
 			for(Peer peer : peersByState){
 				try 
 				{
 					System.out.println("IP " + peer.getPeerIpAddress() + peer.getEpochTime());
 					if(InetAddress.getByName(peer.getPeerIpAddress().trim()).isReachable(timeout)){
 						System.out.println("Found " + peer.getPeerIpAddress().trim());
 						peer.setState(Peer.STATE.ACTIVE);
 						peer.setEpochTime(new Date().getTime());
 						peerTable.updatePeer(peer);
 					}
 				} 
 				catch(NullPointerException e){
 					e.printStackTrace();
 				}
 				catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 			
 			try {
				sleep(new Random().nextInt(1000*60));
 			} 
 			catch (InterruptedException e) {
 				
 			}
 		}
 	}
 }
