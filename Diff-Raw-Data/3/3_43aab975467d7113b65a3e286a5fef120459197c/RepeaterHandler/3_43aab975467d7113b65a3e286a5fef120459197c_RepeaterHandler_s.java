 package com.rullaf.octgn.server;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Set;
 import java.util.concurrent.CopyOnWriteArraySet;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.eclipse.jetty.websocket.WebSocket;
 import org.eclipse.jetty.websocket.WebSocketHandler;
 
 import com.google.gson.*;
 
 public class RepeaterHandler extends WebSocketHandler {
 	private final ArrayList<Instance> servers = new ArrayList<Instance>();
 	private final Set<ChatWebSocket> webSockets = new CopyOnWriteArraySet<ChatWebSocket>();
 
 	public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
 		return new ChatWebSocket();
 	}
 
 	private class ChatWebSocket implements WebSocket.OnTextMessage {
 		private Connection connection;
 		private ConnectMessage connectMessage;
 		private Instance server;
 		
 		public String getServerName() {
 			if (server == null)
 				return null;
 			
 			return server.getName();
 		}
 		
 		public String getClientId() {
 			return connectMessage.getClientId();
 		}
 
 		public void onOpen(Connection connection) {
 			// Client (Browser) WebSockets has opened a connection.
 			// 1) Store the opened connection
 			this.connection = connection;
 			// 2) Add ChatWebSocket in the global list of ChatWebSocket instances instance.
 			webSockets.add(this);
 		}
 
 		public void onMessage(String data) {
 			Gson gson = new Gson();
 			Message message = gson.fromJson(data, Message.class);
 			
 			if (message.getMethod().equals("keep-alive")) {
 				//System.out.println("received keep-alive");
 				return;
 			}
 			
 			if (message.getMethod().equals("connect")) {
 				connectMessage = gson.fromJson(data, ConnectMessage.class);
 				try {
 					for (Instance instance : servers) {
 						if (instance.getName().equals(connectMessage.getInstance())) {
 							server = instance;
 							break;
 						}
 					}
 					
 					// not found
 					if (server == null) {
 						server = new Instance(connectMessage.getInstance(), connectMessage.getPassword());
 						server.add(connectMessage.getClientId());
 						servers.add(server);
 						connection.sendMessage("{\"method\": \"connect_response\", \"response\": \"created\"}");
 
 					// found and valid pass
 					} else if (server.getPassword().equals(connectMessage.getPassword())) {
 							server.add(connectMessage.getClientId());
 							connection.sendMessage("{\"method\": \"connect_response\", \"response\": \"connected\"}");
 							
 							// announce to all other clients on the same server instance
 							for (ChatWebSocket webSocket : webSockets) {				
 								try {
									if (webSocket.getServerName() != null && webSocket.getServerName().equals(server.getName())) {
 										webSocket.connection.sendMessage("{\"method\": \"announce_join\", \"count\": " + (webSockets.size() + 1) + "}");
 									}
 								} catch (IOException e) {
 									// Error was detected, close the ChatWebSocket client side
 									this.connection.disconnect();
 								}
 							}
 
 					// found and invalid pass
 					} else {
 						connection.sendMessage("{\"method\": \"connect_response\", \"response\": \"refused\"}");
 					}
 				
 					
 				} catch (IOException e) {
 					this.connection.disconnect();
 				}
 				return;
 			}
 			
 			// Loop for each instance of ChatWebSocket to send message server to each client WebSockets.
 			try {
 				for (ChatWebSocket webSocket : webSockets) {
 					// send only if clients share server
 					if (webSocket.getServerName() != null && webSocket.getServerName().equals(server.getName())
 							&& getClientId() != webSocket.getClientId()) { // do not send to sender
 						webSocket.connection.sendMessage(data);
 					}
 				}
 			} catch (IOException x) {
 				// Error was detected, close the ChatWebSocket client side
 				this.connection.disconnect();
 			}
 
 		}
 
 		public void onClose(int closeCode, String message) {
 			
 			// Tell all remaining clients that this client left
 			for (ChatWebSocket webSocket : webSockets) {				
 				try {
 					if (webSocket.getServerName() != null && webSocket.getServerName().equals(server.getName())) {
 						webSocket.connection.sendMessage("{\"method\": \"announce_leave\", \"count\": " + webSockets.size() + "}");
 					}
 				} catch (IOException e) {
 					// Error was detected, close the ChatWebSocket client side
 					this.connection.disconnect();
 				}
 			}
 			
 			// Remove ChatWebSocket in the global list of ChatWebSocket instance.
 			webSockets.remove(this);
 			
 			if (server != null) {
 				// remove client from server and destroy server if empty
 				server.remove(connectMessage.getClientId());
 				if (server.isEmpty()) {
 					System.out.println("closing server instance '" + server.getName() + "'");
 					servers.remove(server);
 				}
 			}
 		}
 	}
 }
 
 class Instance {
 	private ArrayList<String> clients = new ArrayList<String>();
 	private String name;
 	private String password;
 	
 	public Instance(String name, String password) {
 		this.name = name;
 		this.password = password;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public String getPassword() {
 		return password;
 	}
 	
 	public void add(String client) {
 		if (!has(client)) {
 			clients.add(client);
 		}
 	}
 	
 	public boolean has(String client) {
 		return clients.contains(client);
 	}
 	
 	public void remove(String client) {
 		clients.remove(client);
 	}
 	
 	public boolean isEmpty() {
 		return clients.size() == 0;
 	}
 }
 
 class Message {
 	private String method;
 	private String client_id;
 	
 	public String getMethod() {
 		return method;
 	}
 	
 	public String getClientId() {
 		return client_id;
 	}
 }
 
 class ConnectMessage extends Message {
 	private String instance;
 	private String password;
 	
 	public String getInstance() {
 		return instance;
 	}
 	
 	public String getPassword() {
 		return password;
 	}
 }
