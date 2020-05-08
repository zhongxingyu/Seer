 
 package com.myl.springsockets;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Service;
 import org.springframework.web.socket.TextMessage;
 import org.springframework.web.socket.WebSocketSession;
 
 import com.google.gson.Gson;
 import com.myl.messages.CardInfoMessage;
 import com.myl.messages.CardListInfoMessage;
 import com.myl.messages.ChatInfoMessage;
 import com.myl.messages.ConnectionInfoMessage;
 import com.myl.messages.MessageInfoMessage;
 import com.myl.messages.PhaseInfoMessage;
 import com.myl.messages.SessionInfoMessage;
 import com.myl.messages.SessionInfoMessage.TYPE;
 import com.myl.messages.StatusInfoMessage;
 import com.myl.messages.TargetInfoMessage;
 import com.myl.util.Util;
 
 
 
 @Service
 public class ChatService {
   
   private Set<WebSocketSession> conns = java.util.Collections.synchronizedSet(new HashSet<WebSocketSession>());  
   private Map<WebSocketSession, UserConnection> nickNames = java.util.Collections.synchronizedMap(new HashMap<WebSocketSession, UserConnection>());  
   private static final Logger LOGGER = LoggerFactory.getLogger(ChatService.class);
   private Gson jsonProcessor=new Gson();
   
 
   public void registerOpenConnection(WebSocketSession session) {
     conns.add(session);
   }
   
   public void registerCloseConnection(WebSocketSession session) {
 	  
     if(nickNames.get(session).getTipo().equals(TYPE.FORMAT)){
     	sendStatusInfoToOtherUsers(new StatusInfoMessage(nickNames.get(session).getUserName(), nickNames.get(session).getFormatOrUser(), StatusInfoMessage.STATUS.DISCONNECTED),session);
     }else{
     	sendMessage(nickNames.get(session).getFormatOrUser(), new StatusInfoMessage(nickNames.get(session).getUserName(), nickNames.get(session).getFormatOrUser(), StatusInfoMessage.STATUS.DISCONNECTED));
     }
     
     nickNames.remove(session);
     conns.remove(session);  
   }
   
   public void processMessage(WebSocketSession session, String message) {
     if (!nickNames.containsKey(session) && message.contains("sessionInfo")) {
   
       //Recupera los datos de la sesion del usuario
       SessionInfoMessage sessionMessage = jsonProcessor.fromJson(message, SessionInfoMessage.class);
       final UserConnection userConnection=new UserConnection(sessionMessage.getSessionInfo().getUser(), sessionMessage.getSessionInfo().getFormatOrUserTwo(),sessionMessage.getSessionInfo().getType());
       sendConnectionInfo(session,userConnection);
       nickNames.put(session,userConnection);      
       
       if(userConnection.getTipo().equals(TYPE.FORMAT)){
     	  sendStatusInfoToOtherUsers(new StatusInfoMessage(userConnection.getUserName(), userConnection.getFormatOrUser(), StatusInfoMessage.STATUS.CONNECTED),session);
       }else{
     	  sendMessage(userConnection.getFormatOrUser(), new StatusInfoMessage(userConnection.getUserName(), userConnection.getFormatOrUser(), StatusInfoMessage.STATUS.CONNECTED));
       }
            
     
     } else {
    	
     	//mensajes enviados en el chat
     	if(message.contains("messageInfo")){
     		final MessageInfoMessage msg = jsonProcessor.fromJson(message, MessageInfoMessage.class);
     		sendMessage(msg.getMessageInfo().getTo(), msg);
        	}else if(message.contains("chatInfo")){
     		final ChatInfoMessage msg = jsonProcessor.fromJson(message, ChatInfoMessage.class);        		
     		sendMessageToAll(msg,session);    	
     	}
     	//Métodos para el duel room
        	else if(message.contains("cardInfo")){
     		final CardInfoMessage msg = jsonProcessor.fromJson(message, CardInfoMessage.class);
     		sendMessage(msg.getCardInfo().getTo(), msg);
     		
    	}else if(message.contains("cardListInfo")){    		
     		final CardListInfoMessage msg = jsonProcessor.fromJson(message, CardListInfoMessage.class);
     		sendMessage(msg.getCardListInfo().getTo(), msg);
     		
     	}else if(message.contains("targetInfo")){
     		final TargetInfoMessage msg = jsonProcessor.fromJson(message, TargetInfoMessage.class);    		
     		sendMessage(msg.getTargetInfo().getTo(), msg);
     		
     	}else if(message.contains("phaseInfo")){
     		final PhaseInfoMessage msg = jsonProcessor.fromJson(message, PhaseInfoMessage.class);
     		sendMessage(msg.getPhaseInfo().getTo(), msg);
     		
     	}
     }
   }
 
   
   private void sendConnectionInfo(WebSocketSession session,UserConnection connection) {	  
       final List<String> activeUsers = getActiveUsers();
       final List<String> formats = getFormats();
       final ConnectionInfoMessage connectionInfoMessage = new ConnectionInfoMessage(connection.getUserName(),activeUsers,formats);
       try {
     	  LOGGER.info("Conexion a: Id-"+session.getId()+" user: "+connection.getUserName());
     	  session.sendMessage(new TextMessage(jsonProcessor.toJson(connectionInfoMessage)));
       } catch (IOException e) {
       	LOGGER.error("SCI: No se pudo enviar el mensaje", e);
       }
   }
   
   //Envía un mensaje a todos los usuarios
   private void sendMessageToAll(Object message,WebSocketSession session) {
 	  for(WebSocketSession sessionAux:conns){
 		  if(!sessionAux.equals(session) && sessionAux.isOpen()){
 				try {					
 					sessionAux.sendMessage(new TextMessage(jsonProcessor.toJson(message)));
 				} catch (IOException e) {
 					LOGGER.error("No se pudo enviar el mensaje de estado de la conexión", e);
 				}
 			}else{
 //				LOGGER.warn("SMA: Se está intentando enviar un mensaje a un usuario no conectado");
 			}
 	  }  	
   }
   
 //Envía un mensaje a un usuario específico
   private void sendMessage(String string,Object object) {
   	final WebSocketSession destinationConnection = getDestinationUserConnection(string);
       if (destinationConnection != null && destinationConnection.isOpen()) {          
           try {
 			destinationConnection.sendMessage(new TextMessage(jsonProcessor.toJson(object)));
 		} catch (Exception e) {
 			LOGGER.error("No se pudo enviar el mensaje", e);
 		}
       } else {
       	LOGGER.warn("SM: Se está intentando enviar un mensaje a un usuario no conectado");
       }
   }
   
   private WebSocketSession getDestinationUserConnection(String destinationUser) {
       for (UserConnection connection : nickNames.values()) {    	  
           if (destinationUser.equals(connection.getUserName())) {
         	  return Util.getKeyByValue(nickNames, connection);
           }
       }
       return null;
   }
   
   private void sendStatusInfoToOtherUsers(StatusInfoMessage message, WebSocketSession session) {
   	LOGGER.info("STATUS: "+nickNames.get(session).getUserName()+" "+message.getStatusInfo().getStatus()+". Msj enviado a los demás usuarios.");
   	WebSocketSession sessionAux;
   	for (UserConnection connection : nickNames.values()) {
   		sessionAux=Util.getKeyByValue(nickNames, connection);
   		if(connection.getTipo().equals(TYPE.FORMAT) && !sessionAux.equals(session) && sessionAux.isOpen()){
   				try {
 					sessionAux.sendMessage(new TextMessage(jsonProcessor.toJson(message)));
 				} catch (Exception e) {
 					LOGGER.error("No se pudo enviar el mensaje de estado de la conexión", e);
 				}
   			}else{
 //  				LOGGER.warn("SSU: Se está intentando enviar un mensaje a un usuario no conectado");
   			}
   		}
   } 
   
   private List<String> getActiveUsers() {
 //  	LOGGER.info("Obteniendo lista de usuarios, Total: "+nickNames.size());
       final List<String> activeUsers = new ArrayList<String>();
       for (UserConnection connection : nickNames.values()) {
     	  if(connection.getTipo().equals(TYPE.FORMAT)){
     		  activeUsers.add(connection.getUserName());
     	  }
       }
       if(activeUsers.isEmpty()){
       	LOGGER.error("GAU: No hay usuarios activos");
       }
       return activeUsers;
   }
   
   private List<String> getFormats() {
       final List<String> formats = new ArrayList<String>();
       for (UserConnection connection : nickNames.values()) {
     	  if(connection.getTipo().equals(TYPE.FORMAT)){
     		  formats.add(connection.getFormatOrUser());
     	  }
       }      
       return formats;
   }
 
 }
