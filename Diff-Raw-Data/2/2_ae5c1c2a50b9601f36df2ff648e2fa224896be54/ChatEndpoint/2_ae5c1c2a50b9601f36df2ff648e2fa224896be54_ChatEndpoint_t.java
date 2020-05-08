 package fr.paperciv.ws;
 
 import java.io.IOException;
 
 import javax.websocket.CloseReason;
 import javax.websocket.EndpointConfig;
 import javax.websocket.OnClose;
 import javax.websocket.OnMessage;
 import javax.websocket.OnOpen;
 import javax.websocket.Session;
 import javax.websocket.server.ServerEndpoint;
 
 @ServerEndpoint("/chat/{username}")
 public class ChatEndpoint
 {
 	@OnOpen
 	public void open(Session session, EndpointConfig conf) throws IOException 
 	{ 
 		
 	}
 	
 	@OnMessage
 	public void message(Session session, String msg) throws IOException
 	{
		System.out.println("Chat WS receive the message : "+msg);
		
 		for (Session sess : session.getOpenSessions()) 
 		{
 			if(msg.indexOf("_CONNECT_CHAT")!= -1){
 				if(msg.equals( sess.getPathParameters().get("username") + "_CONNECT_CHAT")){
 					sess.getBasicRemote().sendText("Bienvenue "+sess.getPathParameters().get("username"));
 				}
 				else sess.getBasicRemote().sendText(sess.getPathParameters().get("username")+" is now connected");
 			}
 			else if(msg.indexOf("_DISCONNECT_CHAT")!= -1){
 				if(!msg.equals( sess.getPathParameters().get("username") + "_DISCONNECT_CHAT"))
 					sess.getBasicRemote().sendText(sess.getPathParameters().get("username")+" is now disconnected");
 			}
 			else sess.getBasicRemote().sendText(msg);
         }
 	}
 	
 	@OnClose
 	public void close(Session session, CloseReason closeReason) throws IOException
 	{
 	
 	}
 }
