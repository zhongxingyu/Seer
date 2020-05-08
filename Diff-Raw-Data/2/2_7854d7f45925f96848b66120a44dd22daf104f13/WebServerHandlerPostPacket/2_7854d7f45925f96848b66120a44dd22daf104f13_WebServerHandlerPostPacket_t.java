 package com.nexus.webserver.handlers;
 
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.nexus.NexusServer;
 import com.nexus.client.NexusClient;
 import com.nexus.event.events.PacketReceivedEvent;
 import com.nexus.logging.NexusLog;
 import com.nexus.network.exception.InvalidPacketException;
 import com.nexus.network.packethandlers.IPacketHandler;
 import com.nexus.network.packets.Packet;
 import com.nexus.utils.JSONPacket;
 import com.nexus.utils.Utils;
 import com.nexus.webserver.IWebServerHandler;
 import com.nexus.webserver.WebServerMethod;
 import com.nexus.webserver.WebServerRequest;
 import com.nexus.webserver.WebServerResponse;
 import com.nexus.webserver.WebServerStatus;
 
 public class WebServerHandlerPostPacket implements IWebServerHandler{
 	
 	public static final Logger log = NexusLog.MakeLogger("PacketReceiver");
 	
 	@Override
 	public void Handle(WebServerRequest Request, WebServerResponse Response){
 		ArrayList<Exception> ProcessingErrors = new ArrayList<Exception>();
 		int PacketID = -1;
 		NexusClient SenderClient = null;
 		Packet IncomingPacket = null;
 		try{
 			if(Request.Method != WebServerMethod.POST){
 				throw new InvalidPacketException("PostPacket must be a POST request");
 			}
 			
 			String Token = Request.GetParameter("token");
 			if(!NexusServer.Instance.AuthenticationManager.isTokenValid(Token, Request.Address)){
 				throw new InvalidPacketException("Expired session token");
 			}
 			
 			SenderClient = NexusServer.Instance.ClientManager.GetClientFromToken(Token);
 			
 			if(SenderClient == null){
 				throw new InvalidPacketException("Invalid session token");
 			}
 			
 			PacketID = Integer.parseInt(Request.GetPostObject("id"));
 			if(PacketID == -1){
 				throw new InvalidPacketException("Invalid packetid");
 			}
 
 			
 			if(Packet.PacketIDClassMap.containsKey(PacketID)){
 				if(!Packet.PacketInfo.get(PacketID).GetValue2()){
 					throw new InvalidPacketException("This packet can only be sent from server to client");
 				}
 			}else{
 				throw new InvalidPacketException("Invalid packetid");
 			}
 			IncomingPacket = Packet.GetPacketFromID(PacketID);
 			
 			String IncomingJson = Utils.URLDecode(Request.GetPostObject("data"));
 			JSONPacket Json = Utils.Gson.fromJson(IncomingJson, JSONPacket.class);
 			
 			IncomingPacket.ReadPacketDataJSON(Json);
 			
 			boolean IsCanceled = false;
 			
 			PacketReceivedEvent Event = new PacketReceivedEvent(IncomingPacket, SenderClient);
 			if(!NexusServer.EventBus.post(Event)){
 				IPacketHandler Handler = (IPacketHandler) IncomingPacket.GetPacketHandler().newInstance();
 				Handler.HandlePacket(IncomingPacket, SenderClient);
 			}else{
 				IsCanceled = true;
 			}
 			if(IsCanceled){
 				JSONPacket p = new JSONPacket();
 				p.addErrorPayload("none");
 				p.put("Canceled", true);
 				Response.SendHeaders(WebServerStatus.OK);
 				Response.SendData(p);
 				Response.Close();
 			}else{
 				JSONPacket p = new JSONPacket();
 				p.addErrorPayload("none");
 				p.put("Canceled", false);
 				Response.SendHeaders(WebServerStatus.OK);
 				Response.SendData(p);
 				Response.Close();
 			}
 		}catch(Exception e){
 			if(e instanceof NumberFormatException){
 				ProcessingErrors.add(new InvalidPacketException("Invalid packetid"));
 			}else{
 				ProcessingErrors.add(e);
 			}
 		}finally{
 			if(ProcessingErrors.size() > 0){
 				for(Exception e : ProcessingErrors){
 					if(e instanceof InvalidPacketException){
 						Response.SendHeaders(WebServerStatus.BadRequest);
 						Response.SendError(e.getMessage());
 						Response.Close();
						log.log(Level.INFO, "Received invalid packet! Error was: " + e.getMessage());
 					}else{
 						Response.SendHeaders(WebServerStatus.InternalServerError);
 						Response.SendError("Internal server error");
 						Response.Close();
 						log.log(Level.SEVERE, "Error while processing " + IncomingPacket.toString(), e);
 					}
 				}
 			}
 			ProcessingErrors.clear();
 		}
 	}
 }
