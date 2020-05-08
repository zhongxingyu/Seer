 package com.nexus.client;
 
 import java.net.URL;
 import java.util.ArrayList;
 
 import org.java_websocket.WebSocket;
 
 import com.nexus.JSONPacket;
 import com.nexus.NexusServer;
 
 public class ClientSendQueue {
 
 	private NexusClient Owner;
 
 	public EnumProtocolType Protocol;
 
 	public String HTTPPath = "";
 
 	public ArrayList<JSONPacket> FetchData = new ArrayList<JSONPacket>();
 
 	private WebSocket WebsocketObject;
 
 	public ClientSendQueue(EnumProtocolType Protocol, NexusClient Owner){
 		this.Protocol = Protocol;
 		this.Owner = Owner;
 	}
 
 	public void addToSendQueue(JSONPacket IncomingPacket){
 		if(Owner.RedirectAllPackages){
 			JSONPacket RedirectedPacket = new JSONPacket();
 			RedirectedPacket.put("Destination", Owner.GetClientTypeName());
 			RedirectedPacket.put("Data", IncomingPacket);
 			Owner.RedirectedPacketsDestination.SendQueue.addToSendQueue(RedirectedPacket);
 			return;
 		}
 		switch(Protocol){
 			case HTTP:
 				try{
 					URL url = new URL(HTTPPath + IncomingPacket.toString());
 					url.openConnection();
 				}catch(Exception e) {e.printStackTrace();}
 				break;
 			case FETCH:
 				FetchData.add(IncomingPacket);
 				break;
 			case WebSocket:
 				try{
 					String s = IncomingPacket.toString();
 					System.out.println(s);
					this.WebsocketObject.send(s);
 				}catch(Exception e){
 					WebsocketObject.close(0);
 					this.Protocol = EnumProtocolType.FETCH;
 					FetchData.add(IncomingPacket);
 				}
 				break;
 			default:
 				break;
 		}
 	}
 
 	public void PrepareWebsocketCommunication(){
 		try{
 			WebSocket ws = NexusServer.Instance.WebsocketEngine.GetWebsocketObject(Owner);
 			if(ws != null){
 				JSONPacket Packet = new JSONPacket();
 				Packet.addErrorPayload("none");
 				Packet.put("Protocol", "Success");
 				this.WebsocketObject = ws;
 				this.Protocol = EnumProtocolType.WebSocket;
 				this.WebsocketObject.send(Packet.toString());
 				for(JSONPacket p:FetchData){
 					this.WebsocketObject.send(p.toString());
 				}
 				FetchData.clear();
 			}
 		}catch(Exception e){};
 	}
 
 	public JSONPacket GetFetchData(){
 		JSONPacket SendQueuePacket = new JSONPacket();
 		SendQueuePacket.put("SendQueue", this.FetchData);
 		this.FetchData.clear();
 		return SendQueuePacket;
 	}
 }
