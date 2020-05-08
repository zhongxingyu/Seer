 package models;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 import org.codehaus.jackson.JsonNode;
 
 import play.Logger;
 import play.libs.F.Callback;
 import play.libs.F.Callback0;
 import play.mvc.WebSocket;
 
 /**
  * A push notification connection to a specific browser
  * @author jostein
  */
 public class NotificationConnection {
 	public List<Notification> notifications;
 	public Date lastRead;
 	public WebSocket.Out<JsonNode> websocket;
 	public Long userId;
 	public Long browserId;
 	
 	/**
 	 * Creates a notification connection for a given browser belonging to a specific user.
 	 * @param userId
 	 * @param browserId
 	 */
 	public NotificationConnection(Long userId, Long browserId) {
 		notifications = new ArrayList<Notification>();
 		lastRead = new Date();
 		this.userId = userId;
 		this.browserId = browserId;
 	}
 	
 	/**
 	 * Adds a push notification to the queue, and pushes it right away if a websocket is open.
 	 * @param notification
 	 */
 	public void push(Notification notification) {
 		notifications.add(notification);
 //		Logger.debug("added notification to user #"+userId+" + browser #"+browserId+". New size: "+notifications.size()+". Notification: "+notification);
 		flushWebSocket();
 	}
 	
 	/**
 	 * Writes all the notifications to the websocket.
 	 */
 	public void flushWebSocket() {
 		if (websocket != null) {
 			for (Notification n : notifications) {
 				JsonNode jsonNotification = n.toJson();
 //				Logger.debug("Writing to WebSocket (user:"+userId+",browser:"+browserId+"): "+jsonNotification+". New size: "+notifications.size());
 				websocket.write(jsonNotification);
 			}
 			notifications.clear();
 			lastRead = new Date();
 		}
 	}
 	
 	/**
 	 * Returns a JSON array of all the notifications waiting to be sent to the browser, and empties the notification queue.
 	 * @param userId
 	 * @param browserId
 	 * @return
 	 */
 	public JsonNode pullJson() {
 		List<JsonNode> result = new ArrayList<JsonNode>();
 		for (Notification n : notifications)
 			result.add(n.toJson());
 		JsonNode resultJson = play.libs.Json.toJson(result);
 		notifications.clear();
 		lastRead = new Date();
 //		Logger.debug("Pulling with XHR (user:"+userId+",browser:"+browserId+"): "+resultJson);
 		return resultJson;
 	}
 	
 	/**
 	 * Whether or not the browser window is still connected.
 	 * @return
 	 */
 	public boolean isAlive() {
 		return websocket != null || new Date().before(new Date(lastRead.getTime() + 60*1000));
 	}
 	
 	
 	// -- Static stuff --
 	
 	/** Key is user ID; value is a list of notification connections to each browser window. */
 	public static ConcurrentMap<Long, List<NotificationConnection>> notificationConnections;
 	
 	/**
 	 * Push a notification to all the users browser windows
 	 * @param notification
 	 */
 	public static void push(Long userId, Notification notification) {
 //		Logger.debug("pushing message to user #"+userId+": "+notification.toString());
 		synchronized (notificationConnections) {
 			if (!notificationConnections.containsKey(userId)) {
 //				Logger.debug("Can't push notification to user #"+userId+": no such user connected.");
 				return;
 			}
 			
 			for (NotificationConnection connection : notificationConnections.get(userId))
 				connection.push(notification);
 		}
 	}
 	
 	/**
 	 * Push a notification to one of the users browser windows
 	 * @param notification
 	 */
 	public static void push(Long userId, Long browserId, Notification notification) {
 //		Logger.debug("pushing message to user #"+userId+" (browser #"+browserId+"): "+notification.toString());
 		synchronized (notificationConnections) {
 			if (!notificationConnections.containsKey(userId)) {
 //				Logger.debug("Can't push notification to user #"+userId+": no such user connected.");
 				return;
 			}
 			
 			for (NotificationConnection connection : notificationConnections.get(userId)) {
 				if (connection.browserId.equals(browserId)) {
 					connection.push(notification);
 					break;
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Creates and returns a WebSocket connection for the browser window `browserId`
 	 * @return
 	 */
 	public static WebSocket<JsonNode> createWebSocket(final Long userId, final Long browserId) {
 
 		// Create WebSocket
 		WebSocket<JsonNode> ws = new WebSocket<JsonNode>() {
 			// Called when the Websocket Handshake is done.
 			public void onReady(WebSocket.In<JsonNode> in, final WebSocket.Out<JsonNode> out){
 
 				// For each event received on the socket
 				in.onMessage(new Callback<JsonNode>() {
 					public void invoke(JsonNode event) {
 						// Log events to the console
//						Logger.debug(event.asText());
 					}
 				});
 
 				// When the socket is closed
 				in.onClose(new Callback0() {
 					public void invoke() {
 						synchronized (notificationConnections) {
 							if (!notificationConnections.containsKey(userId))
 								return;
 							
 							for (NotificationConnection c : notificationConnections.get(userId)) {
 								if (c.browserId.equals(browserId))
 									c.websocket = null;
 							}
//							Logger.debug("WebSocket: user #"+userId+" disconnected websocket from window #"+browserId);
 						}
 					}
 				});
 
 				// Remember socket
 				synchronized (notificationConnections) {
 					NotificationConnection connection = createBrowserIfAbsent(userId, browserId);
 					
 					connection.websocket = out;
 					
//					Logger.debug("WebSocket: user #"+userId+" connected websocket to window #"+browserId);
 					
 					connection.flushWebSocket();
 				}
 
 			}
 		};
 
 		return ws;
 	}
 	
 	/**
 	 * Returns a JSON array of all the notifications waiting to be sent to the given browser and user (also empties the notification queue).
 	 * @param userId
 	 * @param browserId
 	 * @return
 	 */
 	public static JsonNode pullJson(Long userId, Long browserId) {
 		synchronized (notificationConnections) {
 			NotificationConnection connection = createBrowserIfAbsent(userId, browserId);
 			
 			return connection.pullJson();
 		}
 	}
 	
 	public static NotificationConnection createBrowserIfAbsent(Long userId, Long browserId) {
		if (notificationConnections == null)
			notificationConnections = new ConcurrentHashMap<Long,List<NotificationConnection>>(); // not sure why it is not initialized in Global.java...
 		synchronized (notificationConnections) {
 			notificationConnections.putIfAbsent(userId, new ArrayList<NotificationConnection>());
 			NotificationConnection connection = null;
 			for (NotificationConnection c : notificationConnections.get(userId)) {
 				if (c.browserId.equals(browserId)) {
 					connection = c;
 					break;
 				}
 			}
 			if (connection == null) {
//				Logger.debug("Creating new notification connection for user #"+userId+" + browser #"+browserId);
 				connection = new NotificationConnection(userId, browserId);
 				notificationConnections.get(userId).add(connection);
 			}
 			return connection;
 		}
 	}
 	
 }
