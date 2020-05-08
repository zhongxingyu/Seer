 package com.nexus.client;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 
 import org.java_websocket.WebSocket;
 
 import com.nexus.NexusServer;
 
 public class ClientSendQueue {
 	
 	private NexusClient Owner;
 	
 	public EnumProtocolType Protocol;
 	
 	public String HTTPPath = "";
 	
 	public ArrayList<String> FetchData = new ArrayList<String>();
 	
 	private WebSocket WebsocketObject;
 
 	public ClientSendQueue(EnumProtocolType Protocol, NexusClient Owner){
 		this.Protocol = Protocol;
 		this.Owner = Owner;
 	}
 	
 	public void addToSendQueue(String s){
 		if(Owner.RedirectAllPackages){
 			Owner.RedirectedPacketsDestination.SendQueue.addToSendQueue("{\"Destination\":\"" + Owner.GetClientTypeName() + "\", \"Data\":" + s + "}");
 			return;
 		}
 		switch(Protocol){
 			case HTTP:
 				try {
 					URL url = new URL(HTTPPath + s);
 					URLConnection conn = url.openConnection();
 			        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 			        //String inputLine;
 			        //String ResponseString = "";
 			        //while ((inputLine = in.readLine()) != null) 
 			        //	ResponseString += inputLine;
 			        in.close();
 				} catch (Exception e) {e.printStackTrace();}
 				break;
 			case FETCH:
 				FetchData.add(s);
 				break;
 			case WebSocket:
 				try {
 					WebsocketObject.send(s);
 				}catch(Exception e){
 					WebsocketObject.close(0);
 					this.Protocol = EnumProtocolType.FETCH;
 					FetchData.add(s);
 				}
 				break;
 		}
 	}
 	
 	public void PrepareWebsocketCommunication(){
 		try{
 			WebSocket ws = NexusServer.Instance.WebsocketEngine.GetWebsocketObject(this.Owner);
 			if(ws != null){
 				this.WebsocketObject = ws;
 				this.Protocol = EnumProtocolType.WebSocket;
 				this.WebsocketObject.send("{\"error\":\"None\", \"Protocol\":\"Success\"}");
 				for(String s:FetchData){
 					this.WebsocketObject.send(s);
 				}
 				FetchData.clear();
 			}
 		}catch(Exception e){};
 	}
 	
 	public String GetFetchData(){
 		String fetchedstring = "{\"SendQueue\":[";
 		boolean Fetched = false;
 		for(String s:FetchData){
 			Fetched = true;
 			fetchedstring += s;
 			fetchedstring += ",";
 		}
 		FetchData.clear();
 		if(Fetched){
 			fetchedstring = fetchedstring.substring(0, fetchedstring.length()-1);
 		}
 		fetchedstring += "]}";
 		return fetchedstring;
 	}
 }
