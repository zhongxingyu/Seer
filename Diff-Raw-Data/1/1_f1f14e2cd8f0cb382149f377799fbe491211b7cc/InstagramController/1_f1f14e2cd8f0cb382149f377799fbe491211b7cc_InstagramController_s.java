 /**
  * 
  */
 package controllers;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.node.ObjectNode;
 
 import controllers.TwitterController.Sockets;
 
 
 import play.Logger;
 import play.libs.Json;
 import play.libs.F.Callback;
 import play.libs.F.Callback0;
 import play.mvc.Controller;
 import play.mvc.WebSocket;
 import play.mvc.WebSocket.Out;
 /**
  * @author romanelm
  */
 public class InstagramController extends Controller {
 
 	/**
 	 * Hashmap that given an ID of a Display, returns 
 	 * a Sockets object containing 2 websockets: one for the small view
 	 * and one for the big one.	
 	 */
 	public static HashMap<String, Sockets> sockets = new HashMap<String, Sockets>();
 	public static HashMap<WebSocket.Out<JsonNode>,String> socketsReverter = new HashMap<WebSocket.Out<JsonNode>, String>();
 
 	public static HashMap<Integer,  WebSocket.Out<JsonNode>> requests = new HashMap<Integer, WebSocket.Out<JsonNode>>();
 	public static HashMap<WebSocket.Out<JsonNode>,String> requestsReverter = new HashMap<WebSocket.Out<JsonNode>, String>();
 
 	public static HashMap<String,ArrayList<WebSocket.Out<JsonNode>>> mobilesConnected = new HashMap<String, ArrayList<Out<JsonNode>>>(); 
 	public static HashMap<WebSocket.Out<JsonNode>,String> reverter = new HashMap<WebSocket.Out<JsonNode>, String>();
 	
 	public static WebSocket<JsonNode> webSocket() {
 		return new WebSocket<JsonNode>() {
 
 			// Called when the Websocket Handshake is done.
 			public void onReady(WebSocket.In<JsonNode> in, final WebSocket.Out<JsonNode> out) {
 
 				in.onMessage(new Callback<JsonNode>() {
 					public void invoke(JsonNode event) {
 						String messageKind = event.get("kind").asText();						
 						Logger.info("INSTAGRAM: " + event.toString());
 
 						if(messageKind.equals("appReady")){
 							String displayID = event.get("displayID").asText();
 
 							if(!sockets.containsKey(displayID)){
 								sockets.put(displayID, new Sockets(null, null));
 								socketsReverter.put(out, displayID);
 								Logger.info("DisplayID " + displayID + " was added to the instagram app.");
 							}
 
 
 							String size = event.get("size").asText();
 							if(size.equals("small")){
 								// Set the socket
 								sockets.get(displayID).small = out;
 							} else if(size.equals("big")) {
 								sockets.get(displayID).big  = out;
 							}
 						} 
 						// mobile wants to know what's on the screen
 						else if (messageKind.equals("getItems")){
 							String displayID = event.get("displayID").asText();
 							Logger.info("GET ITEMS");
 							Integer reqID = event.get("reqID").asInt();
 							if(reqID != null){
 								ObjectNode msgForScreen = Json.newObject();
 								msgForScreen.put("kind", "getItems");
 								msgForScreen.put("reqID",reqID);
 								requests.put(reqID, out);
 								requestsReverter.put(out, displayID);
 								Sockets sckts = sockets.get(displayID);
 								sckts.big.write(msgForScreen);
 								Logger.info("INSTAGRAM: SENT BACK TO THE IFRAME SOCKET");
 							} else {
 								Logger.info("Missing reqID");
 							}
 						} else if(messageKind.equals("itemsOnScreen")){
 							Logger.info("ITEMS ON SCREEN");
 							int reqID = event.get("reqID").asInt();
 							requests.get(reqID).write(event);
 							requests.remove(reqID);
 							requestsReverter.remove(out);
 							Logger.info("SENT TO MOB");
 						} else if (messageKind.equals("mobileRequest")){
 							String displayID = event.get("displayID").asText();
 							if(! mobilesConnected.get(displayID).contains(out)){
 								reverter.put(out, displayID);
 								mobilesConnected.get(displayID).add(out);
 								int numberOfMobiles = numberOfMobiles(displayID);
 								Logger.info(numberOfMobiles + " mobiles connected");
 							}
 							String preference = event.get("preference").asText();
 							if(preference != null){
 								Sockets sctks = sockets.get(displayID);
 								ObjectNode req = Json.newObject();
 								req.put("kind", "newhashtag");
 								req.put("hashtag", preference);
 								sctks.big.write(req);
 								sctks.small.write(req);
 							}
 						}
 					}
 				});
 
 
 				// When the socket is closed.
 				in.onClose(new Callback0() {
 					public void invoke() {
 						String displayID = socketsReverter.get(out);
 						String reqID = requestsReverter.get(out);
 						if (displayID != null){
 							sockets.remove(displayID);
 							socketsReverter.remove(out);
 						} else if(reqID != null){
 							requests.get(reqID);
 							requestsReverter.get(out);
 						}
 					}
 
 
 				});
 
 			}
 
 		};
 	}
 	
 	private static int numberOfMobiles(String displayID) {
 		int numberOfMobiles = mobilesConnected.get(displayID).size() ;
 		Sockets dsock = sockets.get(displayID);	
 		ObjectNode stats = Json.newObject();
 		stats.put("kind", "stats");
 		stats.put("mobiles", numberOfMobiles);
 		dsock.big.write(stats);
 		dsock.small.write(stats);
 		return numberOfMobiles;
 	}
 
 	public static class Sockets {
 		public WebSocket.Out<JsonNode> small;
 		public WebSocket.Out<JsonNode> big;
 
 		public Sockets(Out<JsonNode> small, Out<JsonNode> big) {
 			this.small = small;
 			this.big = big;
 		}
 	}
 
 }
