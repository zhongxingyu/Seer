 package net.ihiroky.tyrus.sample;
 
 import javax.websocket.CloseReason;
 import javax.websocket.EndpointConfig;
 import javax.websocket.OnClose;
 import javax.websocket.OnError;
 import javax.websocket.OnMessage;
 import javax.websocket.OnOpen;
 import javax.websocket.Session;
 import javax.websocket.server.ServerEndpoint;
 import java.io.IOException;
 
 /**
  * Created on 13/06/24, 11:11.
  *
  * @author Hiroki Itoh
  */
 @ServerEndpoint(value = "/echo")
 public class EchoEndpoint {
 
     @OnOpen
     public void onOpen(Session session, EndpointConfig config) throws IOException {
        System.out.println("onOpen: " + session);
     }
 
     @OnClose
     public void onClose(Session session, CloseReason reason) throws IOException {
        System.out.println("onClose: " + session + ", " + reason);
     }
 
     @OnMessage
     public void onMessage(Session session, String message) {
        System.out.println("onMessage: " + session + ", " + message);
         for (Session s : session.getOpenSessions()) {
             s.getAsyncRemote().sendText(message);
         }
     }
 
     @OnError
     public void onError(Session session, Throwable t) {
        System.out.println("onError: " + session + ", " + t);
     }
 }
