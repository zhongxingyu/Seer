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
 	}
 
 	@Override
 	public boolean isString() {
 		return true;
 	}
 
 }
