 package com.nexus.network.handlers;
 
 import java.net.URL;
 import java.util.HashMap;
 
 import com.nexus.interfaces.IPacket;
 import com.nexus.network.exception.ConnectionErrorException;
 import com.nexus.network.packets.Packet;
 import com.nexus.utils.JSONPacket;
 
 
 public class NetworkHandlerHTTP implements INetworkHandler{
 
 	private String HTTPPath;
 
 	@Override
 	public void InjectData(HashMap<String, Object> data){
 		if(data.containsKey("path")){
 			this.HTTPPath = (String) data.get("path");
 		}
 	}
 	
 	@Override
 	public void SendPacket(IPacket packet) throws Exception{
 		JSONPacket SendingPacket = Packet.GetJSONPacket(packet);
 		
 		try{
 			URL url = new URL(HTTPPath + SendingPacket.toString());
 			url.openConnection();
		}catch(Exception e){throw new ConnectionErrorException();}
 	}
 
 	@Override
 	public void Close(){
 		//Nothing to close!
 	}
 
 	@Override
 	public boolean SupportsMultiPackets(){
 		return true;
 	}
 }
