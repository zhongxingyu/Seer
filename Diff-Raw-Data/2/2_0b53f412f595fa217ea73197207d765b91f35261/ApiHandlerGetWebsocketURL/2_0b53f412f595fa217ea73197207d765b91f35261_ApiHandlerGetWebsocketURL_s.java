 package com.nexus.api.handlers;
 
 import com.nexus.api.IApiHandler;
 import com.nexus.utils.JSONPacket;
 import com.nexus.webserver.WebServerRequest;
 import com.nexus.webserver.WebServerResponse;
 import com.nexus.webserver.WebServerStatus;
 
 public class ApiHandlerGetWebsocketURL implements IApiHandler{
 	
 	@Override
 	public void Handle(WebServerRequest Request, WebServerResponse Response){
 		JSONPacket Packet = new JSONPacket();
		Packet.put("URL", "ws://" + Request.GetHeader("Host") + ":9002");
 		Response.SendHeaders(WebServerStatus.OK);
 		Response.SendData(Packet);
 		Response.Close();
 	}
 }
