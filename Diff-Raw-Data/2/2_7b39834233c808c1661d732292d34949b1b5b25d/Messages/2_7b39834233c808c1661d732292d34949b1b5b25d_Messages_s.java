 package Servlets;
 
 import java.io.IOException;
 import java.rmi.RemoteException;
 import java.util.Hashtable;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.catalina.comet.CometEvent;
 import org.apache.catalina.comet.CometProcessor;
 
 public class Messages extends HttpServlet implements CometProcessor {
 
 	private static final long serialVersionUID = 1L;
 
 	private static Map<String, HttpServletResponse> clients= new Hashtable<String, HttpServletResponse>();
 	
 	
 	
 	// Method called when a client is registers with the CometProcessor
 	private void addClient(String nickName, HttpServletResponse clientResponseObject) {
 		Messages.clients.put(nickName, clientResponseObject);
 		//sendMessageToAll("<i>"+nickName+" has just entered the chat room!</i>");
 	}
 
 	
 	// Method called after an Exception is thrown when the server tries to write to a client's socket.
 	private static void removeClient(String nickName, HttpServletRequest request) {
 		if (Messages.clients.remove(nickName) != null) {
 			//sendMessageToAll("<i>"+nickName+" has just left the chat room!</i>");
 		}
 	}
 
 	
 	// Main method that handles all the assynchronous calls to the servlet.
 	// Receives a CometEvent object, that might have three types of EventType:
 	// - BEGIN (when the connection starts. It is used to initialize variables and register the callback
 	// - READ (means that there is data sent by the client available to be processed.
 	// - END (happens when the connection is terminated, to clean variables and so on.
 	// - ERROR (Happens when some IOException is thrown when writing/reading the connection.
 	
 	public void event(CometEvent event) throws IOException, ServletException {
 		
 		// request and response exactly like in Servlets
 		HttpServletRequest request = event.getHttpServletRequest();
 		HttpServletResponse response = event.getHttpServletResponse();
 		
 		// Parse the something from "?type=something" in the URL.
 		String reqType = request.getParameter("type");
 		String nickName;
 		// Initialize the SESSION and Cache headers.
 		String sessionId = request.getSession().getId();
 		try{
 			nickName = ((Client) request.getSession().getAttribute("user")).getUsername();
 			System.out.println("Nick: " + nickName); 
 		} catch(NullPointerException e){
 			System.out.println("ups messages");
 			nickName = "";
 			return;
 		}
 		System.out.println("SESSION: " + sessionId);
 		response.setHeader("Pragma", "no-cache");
 		response.setHeader("Cache-control", "no-cache");
 		// Disabling the cache, means that the browser will _always_ call this code.
 
 
 		// Let's see which even is being processed right now.
 		System.out.println("Event:" + event.getEventType() + ".");
 		
 		// Since the "event" method is called for every kind of event, we have to decide what to do
 		// based on the Event type. There for we check for all 4 kinds of events: BEGIN, READ, END and ERROR
 		if (event.getEventType() == CometEvent.EventType.BEGIN) {
 			// A connection is initiliazed
 			
 			if (reqType != null) {
 				if (reqType.equalsIgnoreCase("register")) {
 					// Register will add the client HttpServletResponse to the callback array and start a streamed response.
 					
 					// This header is sent to keep the connection open, in order to send future updates.
 					response.setHeader("Content-type", "application/octet-stream");
 					// Here is where the important Comet magic happens.
 					
 					// Let's save the HttpServletResponse with the nickName key.
 					//  That response object will act as a callback to the client.
 					addClient(nickName, response);
 					
 				} else if (reqType.equalsIgnoreCase("exit")) {
 					// if the client wants to quit, we do it.					
 					removeClient(sessionId, request);
 				}
 			}
 		} else if (event.getEventType() == CometEvent.EventType.READ) {
 			// READ event indicates that input data is available
 			
 			
 			// The first line read indicates the destination user.
 			String dest = request.getReader().readLine().trim();
 			// If it is 'allusers',the message should be delivered to all users
 
 			// The second line is the message itself.
 			String msg = request.getReader().readLine().trim();
 			
 			// For debug purposes
 			System.out.println("msg = [" + msg + "] to " + dest);
 			
 			if (msg != null && !msg.isEmpty()) {
 				if (dest.equals("allusers")) {
 					try{
 						((Client) request.getSession().getAttribute("user")).sendMessageAll(msg);
 					} catch (RemoteException e) {
 						
 					}
 				} else {
 					try{
 						((Client) request.getSession().getAttribute("user")).sendMessage(dest, msg);
 					} catch (RemoteException e) {
 						
 					}
 				}
 			}
			
 		} else if (event.getEventType() == CometEvent.EventType.ERROR) {
 			// In case of any error, we terminate the connection.
 			// The connection remains in cache anyway, and it's later removed
 			// when an Exception at write-time is raised.
 			event.close();
 		} else if (event.getEventType() == CometEvent.EventType.END) {
 			// When the clients wants to finish, we do it the same way as above.
 			event.close();
 		}
 	}
 	
 	
 	
 	/*private static void sendMessageToAll(String message) {
 		// The message is for everyone.
 		synchronized (Messages.clients) {
 			Set<String> clientKeySet = Messages.clients.keySet();
 			// Let's iterate through the clients and send each one the message.
 			for (String client : clientKeySet) {
 				try {
 					HttpServletResponse resp = Messages.clients.get(client);
 					resp.getWriter().println(message + "<br/>");
 					resp.getWriter().flush();
 				} catch (IOException ex) {
 					// Trouble using the response object's writer so we remove
 					// the user and response object from the hashtable
 					removeClient(client,null);
 				}
 			}
 		}
 	}*/
 
 	public static void sendMessage(String message, String destination) {
 		// This method sends a message to a specific user
 		System.out.println("D:" + destination);
 		System.out.println(message);
 		
 		synchronized (Messages.clients) {
 			try {
 				HttpServletResponse resp = Messages.clients.get(destination);
 				if(resp!=null){
 					resp.getWriter().println(message + "<br/>");
 					resp.getWriter().flush();
 				} else {
 					
 				}
 				
 			} catch (IOException ex) {
 				// Trouble using the response object's writer so we remove
 				// the user and response object from the hashtable
 				removeClient(destination,null);
 			}
 		}
 	}
 }
 
