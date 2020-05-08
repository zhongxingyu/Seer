 package com.nexus.network.handlers;
 
 import java.util.HashMap;
 
 import com.nexus.interfaces.IPacket;
 import com.nexus.network.exception.ConnectionErrorException;
 import com.nexus.network.packets.Packet;
 import com.nexus.utils.JSONPacket;
 import com.nexus.webserver.WebServerSession;
 
 
 public class NetworkHandlerServerEvent implements INetworkHandler{
 	
 	private WebServerSession WebSession;
 	
 	@Override
 	public void InjectData(HashMap<String, Object> data){
 		if(data.containsKey("session")){
 			this.WebSession = (WebServerSession) data.get("session");
 		}
 	}
 	
 	@Override
 	public void SendPacket(IPacket packet) throws Exception{
		if(this.WebSession == null) throw new ConnectionErrorException();
 		
 		JSONPacket SendingPacket = Packet.GetJSONPacket(packet);
 		this.WebSession.write("data: " + SendingPacket.toString() + "\n\n");
 	}
 	
 	@Override
 	public void Close(){
 		if(this.WebSession == null) return;
 		this.WebSession.Close();
 	}
 
 	@Override
 	public boolean SupportsMultiPackets(){
 		return true;
 	}
 }
