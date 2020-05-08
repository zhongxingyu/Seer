 package com.nexus.network.packets;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 
 import com.nexus.network.packethandlers.PacketHandlers;
 import com.nexus.utils.JSONPacket;
 
 
 public class Packet13LinkPlaylist extends Packet{
 
 	public int OID;
 	public int PLID;
 	public boolean Linked;
 	
 	public Packet13LinkPlaylist(){}
 	public Packet13LinkPlaylist(int OID, int PLID, boolean Linked){
 		this.OID = OID;
 		this.PLID = PLID;
 		this.Linked = Linked;
 	}
 	
 	@Override
 	public void ReadPacketData(DataInputStream is) throws IOException{
 		
 	}
 	
 	@Override
 	public void WritePacketData(DataOutputStream os) throws IOException{
 		
 	}
 	
 	@Override
 	public void ReadPacketDataJSON(JSONPacket json) throws Exception{
 		this.OID = (int) (Double.parseDouble(json.get("OID").toString()));
 		this.PLID = (int) (Double.parseDouble(json.get("PLID").toString()));
		this.Linked = Boolean.parseBoolean(json.get("Linked").toString());
 	}
 	
 	@Override
 	public void WritePacketDataJSON(JSONPacket json){
 		json.put("OID", this.OID);
 		json.put("PLID", this.PLID);
 		json.put("Linked", this.Linked);
 	}
 	
 	@Override
 	public int GetPacketSize(){
 		return 0;
 	}
 	
 	@Override
 	public Class<?> GetPacketHandler(){
 		return PacketHandlers.HandlerPacket13LinkPlaylist.class;
 	}
 }
