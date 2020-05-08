 package com.voracious.dragons.client.net;
 import com.voracious.dragons.common.ConnectionManager;
 import com.voracious.dragons.common.Message;
 import com.voracious.dragons.common.Packet;
 import com.voracious.dragons.server.net.ServerConnectionManager;
 
 public class ClientChallengeRequestPacket implements Packet{
 
 	@Override
 	public boolean wasCalled(Message message) {
 		String msg = message.toString();
 		return (msg.startsWith("CRS:") || msg.startsWith("CRE:"));
 	}
 
 	@Override
 	public void process(Message message, ConnectionManager cm) {
		ClientConnectionManager ccm = (ClientConnectionManager) cm;
		String msg = message.toString();
		int sep = msg.indexOf(":");
		if(msg.startsWith("CRS:")){
			ccm.sendMessage("CHALLENGE ACCEPTED! You have begun a game with your requested opponent.");
		}
		else if(msg.startsWith("CRE:")){
			ccm.sendMessage(msg.substring(sep+1, msg.length()));
		}
		else {
			ccm.sendMessage("An error occured, but it did not return an error. Unknown error occured.");
		}
		
 	}
 
 	@Override
 	public boolean isString() {
 		return true;
 	}
 
 }
