 package com.nexus.websocket;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketServer;
 import org.java_websocket.handshake.ClientHandshake;
 
 import com.nexus.LogLevel;
 import com.nexus.NexusServer;
 import com.nexus.client.NexusClient;
 
 public class WebsocketEngine extends WebSocketServer  {
 
 	private HashMap<WebSocket, NexusClient> ConnectedClients = new HashMap<WebSocket, NexusClient>();
 	
 	private NexusServer Server;
 	
 	public WebsocketEngine(InetSocketAddress Address, NexusServer Server) {
 		super(Address);
 		this.Server = Server;
 	}
 	
 	public void start(){
 		super.start();
 		this.Log("Starting up...", LogLevel.INFO);
 	}
 	
	public void stop() throws IOException{
		super.stop();
 		this.Log("Shutting down...", LogLevel.INFO);
 	}
 	
 	private void Log(String message, LogLevel level){
 		Server.log.write(message, "WebsocketEngine", level);
 	}
 
 	public void onClose(WebSocket arg0, int arg1, String arg2, boolean arg3) {
 		Log("Websocket Disconnected", LogLevel.INFO);
 	}
 
 	public void onError(WebSocket arg0, Exception arg1) {
 		arg1.printStackTrace();
 	}
 
 	public void onMessage(WebSocket socket, String data) {
 		try{
 			if(data.startsWith("Identify:")){
 				NexusClient client = Server.ClientManager.GetClientFromToken(data.split("Identify:")[1]);
 				if(client == null){
 					socket.send("{\"error\":\"Unknown token\", \"Identify\":\"Error\"}");
 				}else{
 					socket.send("{\"error\":\"None\", \"Identify\":\"Success\"}");
 					ConnectedClients.put(socket, client);
 				}
 			}
 		}catch(Exception e){
 			
 		}
 	}
 
 	public void onOpen(WebSocket arg0, ClientHandshake arg1) {
 		Log("Websocket Connected", LogLevel.INFO);
 	}
 	
 	public WebSocket GetWebsocketObject(NexusClient client){
         for (Entry<WebSocket, NexusClient> entry : ConnectedClients.entrySet()) {
             if (entry.getValue().GetClientID() == client.GetClientID()) {
                 return entry.getKey();
             }
         }
         return null;
 	}
 
 	public void KillAll() {
 		for (Entry<WebSocket, NexusClient> entry : ConnectedClients.entrySet()) {
             try{
                 entry.getKey().send("{\"Action\":\"Shutdown\",\"Reason\":\"Server Shutdown\"}");
 			}catch(Exception e){
 				
 			}
         }
 	}
 
 }
