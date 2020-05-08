 package com.example.wss.chat;
 
 import com.example.wss.chat.messages.ChatMessage;
 import com.example.wss.chat.messages.Container;
 import com.example.wss.chat.messages.ManOffline;
 import com.example.wss.chat.messages.ManOnline;
 import info.liberitas.wss.annotation.Handler;
 import info.liberitas.wss.annotation.Param;
 import info.liberitas.wss.annotation.WebSocketController;
 import info.liberitas.wss.annotation.WebSocketRequest;
 import info.liberitas.wss.api.WebSocketHandler;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Alexander <estliberitas> Makarenko
  * Date: 01.05.12
  * Time: 2:56
  */
 @WebSocketController
 @SuppressWarnings("unused")
 public class ChatController {
 
 	/**
 	 * Handles online-message and broadcasts it to others
 	 *
 	 * Sample request is (to be handled by JsonMessageParser):
 	 * <code>
 	 * 	{
	 * 		"route": "/chat/online/",
 	 * 		"request": {
 	 * 			"name: "Manny"
 	 * 	 	}
 	 * 	}
 	 * </code>
 	 *
 	 * Response:
 	 * <code>
 	 * 	{
 	 * 	    "type: "online",
 	 * 	    "name": "Manny"
 	 * 	}
 	 * </code>
 	 */
 	@WebSocketRequest("/chat/online")
 	public static void chatOnline(@Param String name, @Handler WebSocketHandler handler) {
 		/** Broadcast to all handlers, except current socket handler */
 		handler.getService().broadcast(new ManOnline(name), null);
 	}
 
 	/**
 	 * Handle offline message
 	 */
 	@WebSocketRequest("/chat/offline")
 	public static void chatOffline(@Param String name, @Handler WebSocketHandler handler) {
 		handler.getService().broadcast(new ManOffline(name), null);
 	}
 
 	/**
 	 * Get message from client and broadcast it to others.
 	 * Request:
 	 * <code>
 	 * 	{
 	 * 	    "route": "/chat/message",
 	 * 	    "request": {
 	 * 	        "name": "Manny",
 	 * 	        "message": "Hello world!"
 	 * 	    }
 	 * 	}
 	 * </code>
 	 *
 	 * Response:
 	 * <code>
 	 * 	{
 	 * 	    "type: "message",
 	 * 	    "name": "Manny",
 	 * 	    "message": "Hello world!"
 	 * 	}
 	 * </code>
 	 */
 	@WebSocketRequest("/chat/offline")
 	public static void chatMessage(@Param String name, @Param String message, @Handler WebSocketHandler handler) {
 		/**
 		 * Broadcast message to all handlers even to current handler
 		 */
 		handler.getService().broadcast(new ChatMessage(name, message), handler);
 	}
 }
