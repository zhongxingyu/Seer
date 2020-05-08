 /*
  *  AtenderClienteServidor.java
  *  Copyright (C) 2012  Diego Estévez <dgmvecuador@gmail.com>
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.educautecisystems.core.chat;
 
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.Socket;
 import java.util.ArrayList;
 import org.educautecisystems.core.Sistema;
 import org.educautecisystems.core.chat.elements.ChatConstants;
 import org.educautecisystems.core.chat.elements.ChatMessage;
 import org.educautecisystems.core.chat.elements.MessageHeaderParser;
 import org.educautecisystems.core.chat.elements.UserChat;
 
 /**
  *
  * @author dgmv
  */
 public class AtenderClienteServidor extends Thread {
 	/* Flujos del cliente */
 	private Socket clienteSocket;
 	private InputStream entrada = null;
 	private OutputStream salida = null;
 	private UserChat nuevoUsuario = null;
 	
 	/* Elmentos de comunicación */
 	private LogChatManager logChatManager;
 	private ServidorChat servidorChat;
 	private final ArrayList<ChatMessage> messages = new ArrayList<ChatMessage>();
 	
 	public static int number = 0;
 
 	public AtenderClienteServidor(Socket clienteSocket, LogChatManager logChatManager, ServidorChat servidorChat ) {
 		this.clienteSocket = clienteSocket;
 		this.logChatManager = logChatManager;
 		this.servidorChat = servidorChat;
 	}
 	
 	public boolean esCliente( int idUsuario ) {
		return true;
 	}
 
 	@Override
 	public void run() {
 		/* Evitar que problemas */
 		if ( logChatManager == null ) {
 			System.err.println("No existe interface para el log.");
 			System.exit(-1);
 			return;
 		}
 		if ( servidorChat == null ) {
 			logChatManager.logError("No existe un servidor con el cual comuinicarse.");
 			detenerCliente();
 			return;
 		}
 		
 		try {
 			if (clienteSocket == null) {
 				return;
 			}
 			
 			entrada = clienteSocket.getInputStream();
 			salida = clienteSocket.getOutputStream();
 			
 			MessageHeaderParser header = MessageHeaderParser.parseMessageHeader(entrada);
 			if (!header.getCommand().equals(ChatConstants.CHAT_HEADER_MAIN_COMMAND)) {
 				logChatManager.logError("Formato incorrecto.");
 				return;
 			}
 			
 			if (!header.getVar(ChatConstants.LABEL_COMMAND).equals(ChatConstants.COMMAND_LOGIN)) {
 				procesarRequerimiento(header);
 				return;
 			}
 			
 			String realName = header.getVar(ChatConstants.LABEL_REAL_NAME);
 			String nickName = header.getVar(ChatConstants.LABEL_NICKNAME);
 			
 			logChatManager.logInfo("Entra nuevo usuario: "+nickName+"("+realName+")");
 			
 			String token = generarToken();
 			int idUsuario = number;
 			
 			String response = "";
 			response += generateHeaderValue(ChatConstants.CHAT_HEADER_RESPONSE_COMMAND,
 					ChatConstants.RESPONSE_OK);
 			response += generateHeaderValue(ChatConstants.LABEL_USER_ID, ""+idUsuario);
 			response += generateHeaderValue(ChatConstants.LABEL_USER_TOKEN, token);
 			response += ChatConstants.CHAT_END_HEADER;
 			
 			salida.write(response.getBytes());
 			salida.flush();
 			
 			nuevoUsuario = new UserChat();
 			nuevoUsuario.setId(number);
 			nuevoUsuario.setRealName(realName);
 			nuevoUsuario.setNickName(nickName);
 			nuevoUsuario.setToken(token);
 			
 			servidorChat.insertarUsuario(nuevoUsuario);
 			
 			while (true) {
 				Thread.sleep(ChatConstants.WAIT_TIME_FOR_READ);
 				
 				if ( clienteSocket == null ) {
 					break;
 				}
 				
 				/* Evitar problemas con los hilos. */
 				synchronized ( messages ) {
 					for ( ChatMessage chatMessage:messages ) {
 						sendUserMessageReal(chatMessage);
 					}
 					messages.clear();
 				}
 			}
 			
			logChatManager.logInfo("Cliente ha sido cerrado.");
 			/* Quitar usuario de la lista si se pierde la conexión. */
 			servidorChat.quitarUsuario(nuevoUsuario);
 		} catch (Exception e) {
 			logChatManager.logError("Problema en la atención a un cliente: "+e.getMessage());
 			
 			/* Quitar usuario de la lista si se pierde la conexión. */
 			if ( nuevoUsuario != null ) {
 				servidorChat.quitarUsuario(nuevoUsuario);
 			}
 			
 			detenerCliente();
 			return;
 		}
 		
 		logChatManager.logInfo("Cerrando cliente .."+ (nuevoUsuario == null ? "" : 
 				(" ["+nuevoUsuario.getNickName()+"]")) );
 		detenerCliente();
 	}
 	
 	private void sendUserMessageReal( ChatMessage chatMessage ) throws Exception {
 		StringBuilder response = new StringBuilder();
 		response.append(generateHeaderValue(ChatConstants.CHAT_HEADER_RESPONSE_COMMAND,
 					ChatConstants.RESPONSE_OK));
 		response.append(generateHeaderValue(ChatConstants.LABEL_USER_ID, 
 				""+chatMessage.getIdUserOrigin()));
 		
 		if ( chatMessage.getMessage() == null ) {
 			logChatManager.logError("Not message found to send.");
 			return;
 		}
 		
 		response.append(generateHeaderValue(ChatConstants.LABEL_CONTENT_LENGHT, 
 				""+chatMessage.getMessage().getBytes().length));
 		response.append(ChatConstants.CHAT_END_HEADER);
 		response.append(chatMessage.getMessage());
 		
 		salida.write(response.toString().getBytes());
 		salida.flush();
 	}
 	
 	public boolean compararUsuarioToken ( String token ) {
 		synchronized ( this ) {
 			if (nuevoUsuario == null) {
 				return false;
 			}
 
 			if (nuevoUsuario.getToken().equals(token)) {
 				return true;
 			}
 			return false;
 		}	
 	}
 	
 	private String generarToken() {
 		number++;
 		return Sistema.getMD5("TOKEN-SALT"+number);
 	}
 
 	private void procesarRequerimiento(MessageHeaderParser header) throws Exception {
 		/* List of users */
 		if ( header.getVar(ChatConstants.LABEL_COMMAND).equals(ChatConstants.COMMAND_GET_USERS) ) {
 			String userToken =	header.getVar(ChatConstants.LABEL_USER_TOKEN);
 			String format =		header.getVar(ChatConstants.LABEL_FORMAT);
 			
 			/* Must be a valid token */
 			if ( !ServidorChat.testToken(userToken) ) {
 				logChatManager.logError("Un usuario no registrado a intentado acceder a información del chat.");
 				sendResponseError("User not found.");
 				return;
 			}
 			
 			/* Only XML is valid */
 			if ( !format.equals("XML") ) {
 				logChatManager.logError("No se soporta el formato: " + format);
 				sendResponseError("Format not supported.");
 				return;
 			}
 			
 			ArrayList<UserChat> users = ServidorChat.getUserList();
 			String xmlUsers = UserChat.generateXMLFromList(users);
 			long size = xmlUsers.getBytes().length;
 			System.out.println(xmlUsers);
 			
 			/* Genering response */
 			String headerResponse = "";
 			headerResponse += 
 					generateHeaderValue(ChatConstants.CHAT_HEADER_RESPONSE_COMMAND,
 					ChatConstants.RESPONSE_OK);
 			headerResponse +=
 					generateHeaderValue(ChatConstants.LABEL_CONTENT_LENGHT, ""+size);
 			headerResponse += ChatConstants.CHAT_END_HEADER;
 			headerResponse += xmlUsers;
 			
 			salida.write(headerResponse.getBytes());
 			salida.flush();
 			
 			detenerCliente();
 			return;
 		}
 		
 		if ( header.getVar(ChatConstants.LABEL_COMMAND).equals(ChatConstants.COMMAND_LOGOUT) ) {
 			String userToken = header.getVar(ChatConstants.LABEL_USER_TOKEN);
 			
 			if ( !ServidorChat.testToken(userToken) ) {
 				logChatManager.logError("Un usuario no registrado a intentado acceder a información del chat.");
 				sendResponseError("User not found.");
 				return;
 			}
 			
 			if (servidorChat.logoutCliente(userToken)) {
 				logChatManager.logInfo("A salido el usuario con el token: "+userToken);
 				sendResponseOk();
 			} else {
 				logChatManager.logWarning("Se ha intendo cerrar la sesión de un usuario que no existe.");
 				sendResponseError("User not found.");
 			}
 			return;
 		}
 		
 		if ( header.getVar(ChatConstants.LABEL_COMMAND).equals(ChatConstants.COMMAND_SEND_MESSAGE) ) {
 			/* Leyendo todos los parámetros. */
 			String toVar =			header.getVar(ChatConstants.LABEL_TO);
 			String userToken =		header.getVar(ChatConstants.LABEL_USER_TOKEN);
 			String contentLength =	header.getVar(ChatConstants.LABEL_CONTENT_LENGHT);
 			
 			if ( !ServidorChat.testToken(userToken) ) {
 				logChatManager.logError("Un usuario no registrado a intentado acceder a información del chat.");
 				sendResponseError("User not found.");
 				return;
 			}
 			
 			long contentLengthLong = -1;
 			
 			try {
 				contentLengthLong = Long.parseLong(contentLength);
 			} catch ( NumberFormatException nfe ) {
 				sendResponseError("Number format not supported.");
 				return;
 			}
 			
 			int idUserOrigin = servidorChat.getUserIdFromToken(userToken);
 			
 			if ( idUserOrigin == 0 ) {
 				sendResponseError("UserId not found.");
 				return;
 			}
 			
 			StringBuilder message = new StringBuilder();
 			int byteRead = entrada.read();
 			
 			for ( long i=0; i<contentLengthLong; i++ ) {
 				if ( byteRead != -1 ) {
 					message.append((char) byteRead);
 					byteRead = entrada.read();
 				} else {
 					sendResponseError("Not Enought bytes read.");
 					return;
 				}
 			}
 			
 			servidorChat.sendMessage(toVar, message.toString(), idUserOrigin);
 			sendResponseOk();
 			return;
 		}
 		
 		sendResponseError("Command not valid.");
 		detenerCliente();
 	}
 	
 	public void sendMessage( int idUser, String message, int idUserOrigin ) {
 		/* Filtrar usuarios. */
 		if ( nuevoUsuario == null ) {
 			return;
 		}
 		
 		if ( idUser != nuevoUsuario.getId() && idUser != 0 ) {
 			return;
 		}
 		
 		synchronized(messages) {
 			ChatMessage newMessage = new ChatMessage();
 			
 			newMessage.setIdUser(idUser);
 			newMessage.setIdUserOrigin(idUserOrigin);
 			newMessage.setMessage(message);
 			
 			messages.add(newMessage);
 		}
 	}
 	
 	private void sendResponseOk () throws Exception {
 		StringBuilder response = new StringBuilder();
 		response.append(generateHeaderValue(ChatConstants.CHAT_HEADER_RESPONSE_COMMAND,
 					ChatConstants.RESPONSE_OK));
 		response.append(ChatConstants.CHAT_END_HEADER);
 		
 		salida.write(response.toString().getBytes());
 		salida.flush();
 		
 		detenerCliente();
 	}
 	
 	private void sendResponseError ( String description ) throws Exception {
 		StringBuilder response = new StringBuilder();
 		response.append(generateHeaderValue(ChatConstants.CHAT_HEADER_RESPONSE_COMMAND,
 					ChatConstants.RESPONSE_ERROR));
 		response.append(generateHeaderValue(ChatConstants.LABEL_DESCRIPTION, description));
 		response.append(ChatConstants.CHAT_END_HEADER);
 		
 		salida.write(response.toString().getBytes());
 		salida.flush();
 		
 		detenerCliente();
 	}
 	
 	private String generateHeaderValue( String name, String val ) {
 		return name + ": " + val + "\r\n";
 	}
 
 	public void detenerCliente() {
 		/* Informar si se esta cerrado el cliente. */
 		if ( nuevoUsuario != null ) {
 			logChatManager.logInfo("Deteniendo cliente  \""+nuevoUsuario.getNickName()+"\"");
 		}
 
 		try {
//			if ( entrada != null ) {
//				entrada.close();
//				entrada = null;
//			}
//			if ( salida != null ) {
//				salida.close();
//				salida = null;
//			}
 			
 			if ( clienteSocket != null ) {
 				clienteSocket.close();
 				clienteSocket = null;
 			}
 		} catch (Exception e) {
 			logChatManager.logError("Problemas cerrando usuario: "+e);
 		}
 	}
 }
