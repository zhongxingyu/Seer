 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.test.websocket;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 import javax.websocket.EncodeException;
 import javax.websocket.EndpointFactory;
 import javax.websocket.Session;
 import javax.websocket.WebSocketClose;
 import javax.websocket.WebSocketEndpoint;
 import javax.websocket.WebSocketMessage;
 import javax.websocket.WebSocketOpen;
import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 /**
  *
  * @author iarivu
  */
 
 
     @WebSocketEndpoint(value = "/hello",
         encoders = {DataEncoder.class},
         decoders = {DataDecoder.class},
         factory = com.test.websocket.DummyEndpointFactory.class)
    
 public class HelloWebSocket {
         
   Logger logger = Logger.getLogger(com.test.websocket.HelloWebSocket.class);
 
     Set<Session> user = Collections.synchronizedSet(new HashSet<Session>());
     
     @WebSocketOpen
     public void onOpen(Session peer) {
         user.add(peer);
         logger.info ("WebSocket Open.");
     }
    public void doIt() {
     logger.debug("Did it again!");
   }
     @WebSocketClose
     public void onClose(Session peer) {
         user.remove(peer);
         logger.info ("WebSocket Closed.");
     }
 
 
     @WebSocketMessage
     public void boradcast(Gamedata gd, Session session) throws IOException, EncodeException {
           logger.info ("Boradcast Data:"+ gd);
         for (Session peer : user) {
             if (!peer.equals(session)) {
                 peer.getRemote().sendObject(gd);
             }
         }
     }
 } 
 
   class DummyEndpointFactory implements EndpointFactory {
 
         @Override
         public Object createEndpoint() {
             return null;
         }
     }
 
   
