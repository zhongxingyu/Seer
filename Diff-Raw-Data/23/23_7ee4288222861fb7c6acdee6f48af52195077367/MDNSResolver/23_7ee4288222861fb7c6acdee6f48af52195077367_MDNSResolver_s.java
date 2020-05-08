 package com.flow5.framework;
 
 import java.net.DatagramPacket;
 import java.net.InetAddress;
 import java.net.MulticastSocket;
 
 import org.xbill.DNS.*;
 
 
 public class MDNSResolver {
 	
 //    android.net.wifi.WifiManager.MulticastLock lock;
 //    android.os.Handler handler = new android.os.Handler();
 
 	public MDNSResolver() {
 		
//      android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) getSystemService(android.content.Context.WIFI_SERVICE);
//      lock = wifi.createMulticastLock("mylockthereturn");
//      lock.setReferenceCounted(true);
//      lock.acquire();
 
//      lock.release();
 	}
 	
 	public String resolve(String localName) {
 		String result = null;
 		Record answer = null;
 		try {	
 			Name name = Name.fromString(localName);
 				
 			// setup the multicast socket for mDNS queries
 			MulticastSocket socket = new MulticastSocket(5353);
 			InetAddress group = InetAddress.getByName("224.0.0.251");
 			socket.joinGroup(group);
 			
 			// create a dns query
 			Record rec = Record.newRecord(name, Type.A, DClass.IN);
 			Message query = Message.newQuery(rec);
 			
 			// serialize and send
 			byte [] out = query.toWire(Message.MAXLENGTH);
 			DatagramPacket packet = new DatagramPacket(out, out.length, group, 5353);
 			socket.send(packet);
 			
 			// wait for an answer
 				while (answer == null) {
 					byte[] in = new byte[512];
 			 	    packet = new DatagramPacket(in, in.length);
 			 	    socket.receive(packet);        	    
 			 	                 		
 					Message response =  new Message(in);
 					Record records[] = response.getSectionArray(Section.ANSWER);
 					if (records != null) {
 						for (int i=0; i<records.length; ++i) {
 							if (records[i].getName().equals(name)) {
 								answer = records[i];
 							}
 						}
 					}   			
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}	
 		 
 		if (answer != null) {
 			 result = ((ARecord)answer).getAddress().getHostAddress();			
 		}
 		 
 		return result;
 	}
 	
 }
