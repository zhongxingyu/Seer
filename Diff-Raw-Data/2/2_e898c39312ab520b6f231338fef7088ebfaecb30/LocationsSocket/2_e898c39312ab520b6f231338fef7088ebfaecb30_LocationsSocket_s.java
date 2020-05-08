 package controllers.websockets;
 
 import static play.libs.F.Matcher.ClassOf;
 import static play.libs.F.Matcher.Equals;
 import static play.libs.F.Matcher.StartsWith;
 import static play.mvc.Http.WebSocketEvent.SocketClosed;
 import static play.mvc.Http.WebSocketEvent.TextFrame;
 
 import java.util.List;
 import java.util.Timer;
 
 import oauth2.CheckUserAuthentication;
 import oauth2.OAuth2Constants;
 import play.Logger;
 import play.libs.F.Either3;
 import play.libs.F.EventStream;
 import play.libs.F.Promise;
 import play.mvc.WebSocketController;
 import play.mvc.Http.WebSocketClose;
 import play.mvc.Http.WebSocketEvent;
 import utils.GsonFactory;
 
 import DTO.UserLocationDTO;
 import assemblers.UserLocationAssembler;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 
 /**
  * 
  * @author Alex Jarvis axj7@aber.ac.uk
  */
 public class LocationsSocket extends WebSocketController {	
 	
 	/**
 	 * 
 	 */
 	public static void connect() {
 		
 		// If not a valid token then disconnect the stream
 		CheckUserAuthentication userAuth = new CheckUserAuthentication();
 		if (!userAuth.validToken(params.get(OAuth2Constants.PARAM_OAUTH_TOKEN))) {
 			disconnect();
 		}
 		
 		// TODO: make multiple streams for different meetings / or users
 		LocationStream locationStream = LocationStream.getInstance();
         
         // Socket connected, join the chat room
         EventStream<LocationStream.Event> locationMessagesStream = locationStream.join("user");
         
         //
         EventStream<HeartbeatEvent> heartbeatStream = new EventStream<HeartbeatEvent>();
         
         HeartbeatTask heartbeatMonitor = new HeartbeatTask(heartbeatStream);
         Timer timer = new Timer();
         timer.scheduleAtFixedRate(heartbeatMonitor, 10000, 10000);
 		
 		// Loop while the socket is open
         while(inbound.isOpen()) {      	
         	
         	Either3<WebSocketEvent, HeartbeatEvent, LocationStream.Event> e = await(Promise.waitEither(
         		inbound.nextEvent(),
         		heartbeatStream.nextEvent(),
         		locationMessagesStream.nextEvent()
         	));
         	
         	// Case: The socket has been closed
             for(WebSocketClose closed : SocketClosed.match(e._1)) {
             	Logger.info("web socket closed");
             	locationStream.leave("user");
                 disconnect();
             }
             
             // Case: HeartbeatEvent received (from client)
             for(String text: TextFrame.and(Equals(MessageWrapper.wrap("~h~PONG"))).match(e._1)) {
             	Logger.info("HeartbeatEvent received from client");
             	heartbeatMonitor.setResponse(true);
             }
             
             // Case: HeartbeatEvent.Pulse received (from heartbeatMonitor)
             for (HeartbeatEvent event : ClassOf(HeartbeatEvent.Pulse.class).match(e._2)) {
             	Logger.info("HeartbeatEvent Pulse from timer");
             	heartbeatMonitor.setResponse(false);
            	outbound.send(MessageWrapper.unwrap("~h~PING"));
             	Logger.info("Heartbeat sent");
             }
             
             // Case: HeartbeatEvent.Dead received (from heartbeatMonitor)
             for (HeartbeatEvent event : ClassOf(HeartbeatEvent.Dead.class).match(e._2)) {
             	Logger.info("HeartbeatEvent Dead from timer");
             	disconnect();
             }
             
             // Case: update location message sent from client (only 1 type of message can be sent - location update).
             // This message is sent as a Json Array and so we can specify that it will always start with '['.
             for(String text: TextFrame.and(StartsWith("[")).match(e._1)) {
             	String message = MessageWrapper.unwrap(text);
             	JsonArray jsonArray = stringToJsonArray(message);
             	if (jsonArray != null && jsonArray.isJsonArray()) {
             		// Obtain DTOs from the JsonArray
             		List<UserLocationDTO> userLocationDTOs = UserLocationAssembler.userLocationDTOsWithJsonArray(jsonArray);
             		// Persist locations with the DTOs
             		List<UserLocationDTO> createdUserLocationDTOs = UserLocationAssembler.createUserLocations(userLocationDTOs, userAuth.getAuthorisedUser());
             		Logger.info("Created User Locations size: " +  createdUserLocationDTOs.size());
             	}
             }
             
             // Case: Someone joined the room
             for(LocationStream.Join joined: ClassOf(LocationStream.Join.class).match(e._2)) {
                 outbound.send(MessageWrapper.wrap("join:" + joined.user));
             }
             
             // Case: New message on the chat room
             for(LocationStream.Message message: ClassOf(LocationStream.Message.class).match(e._2)) {
                 outbound.send("message:%s:%s", message.user, message.text);
             }
             
             // Case: Someone left the room
             for(LocationStream.Leave left: ClassOf(LocationStream.Leave.class).match(e._2)) {
                 outbound.send("leave:%s", left.user);
             }
         } // end while socket open
         
 	}
 	
 	/**
 	 * Returns a JSON string from an Object. This is usually handled by the RenderJSON method
 	 * of play.mvc.Controller
 	 * 
 	 * There is a convenience method for WebSocketControllers (outbound.sendJson) - but this
 	 * method does not let you control the behaviour of the created Gson object (as I need to
 	 * specify the date format) and it cannot be overidden in this class because its an internal
 	 * class inside play.mvc.Http.
 	 * 
 	 * @param object
 	 * @return
 	 * @see play.mvc.Controller
 	 * @see play.mvc.Http
 	 */
 	private static String objectToJsonString(Object object) {
 		return GsonFactory.gsonBuilder().create().toJson(object);
 	}
 	
 	/**
 	 * Returns a JsonObject from a String. Usually this is done automatically by GsonObjectBinder
 	 * for a normal play.mvc.Controller class.
 	 *  
 	 * @param jsonString
 	 * @return
 	 * @see play.mvc.Controller
 	 */
 	private static JsonObject stringToJsonObject(String jsonString) {
 		return (JsonObject) new JsonParser().parse(jsonString);
 	}
 	
 	/**
 	 * Returns a JsonArray from a String. Usually this is done automatically by GsonArrayBinder
 	 * for a normal play.mvc.Controller class.
 	 *  
 	 * @param jsonArray
 	 * @return
 	 * @see play.mvc.Controller
 	 */
 	private static JsonArray stringToJsonArray(String jsonArray) {
 		return (JsonArray) new JsonParser().parse(jsonArray);
 	}
 }
 
