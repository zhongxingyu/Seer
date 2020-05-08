 
 package com.myl.springsockets;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.web.socket.CloseStatus;
 import org.springframework.web.socket.TextMessage;
 import org.springframework.web.socket.WebSocketSession;
 import org.springframework.web.socket.handler.TextWebSocketHandler;
 
 


 public class DuelWebSocketHandler extends TextWebSocketHandler {
   
   @Autowired
   private ChatService chatService;
   
  private static final Logger LOGGER = LoggerFactory.getLogger(DuelWebSocketHandler.class);
  
  
   @Override
   public void afterConnectionEstablished(WebSocketSession session) throws Exception {    
     chatService.registerOpenConnection(session);
   }
   
   @Override
   public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
     chatService.registerCloseConnection(session);
     
   }
   
   @Override
   public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
     chatService.registerCloseConnection(session);
     
   }
   
   @Override
   protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
     chatService.processMessage(session, message.getPayload());
   }
 
 }
