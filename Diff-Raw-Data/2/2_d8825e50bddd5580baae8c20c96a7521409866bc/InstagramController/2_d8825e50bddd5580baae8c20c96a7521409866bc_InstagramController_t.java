 /**
  * 
  */
 package controllers;
 
 import java.util.HashMap;
 
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.node.ObjectNode;
 
 
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
 
 	public static int reqID = 0;
 	public static HashMap<Integer,  WebSocket.Out<JsonNode>> requests = new HashMap<Integer, WebSocket.Out<JsonNode>>();
 	
 	public static WebSocket<JsonNode> webSocket() {
 		return new WebSocket<JsonNode>() {
 
 			// Called when the Websocket Handshake is done.
 			public void onReady(WebSocket.In<JsonNode> in, final WebSocket.Out<JsonNode> out) {
 
 				in.onMessage(new Callback<JsonNode>() {
 					public void invoke(JsonNode event) {
 						Logger.info("INCOMING MESSAGE ON INSTAGRAM WS:\n" + event.toString());
 						String messageKind = event.get("kind").asText();						
 						String displayID = event.get("displayID").asText();
 
 
 						if(messageKind.equals("appReady")){
 
 							if(!sockets.containsKey(displayID)){
 								sockets.put(displayID, new Sockets(null, null));
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
 							Logger.info("GET ITEMS");
 							ObjectNode msgForScreen = Json.newObject();
 							msgForScreen.put("kind", "getItems");
 							msgForScreen.put("reqID",reqID);
 							requests.put(reqID, out);	
 							reqID++;
 							Sockets sckts = sockets.get(displayID);
 							sckts.big.write(msgForScreen);
 						} else if(messageKind.equals("itemsOnScreen")){
 							Logger.info("ITEMS ON SCREEN");
 							int reqId = event.get("reqID").asInt();
							Logger.info("Sending req " + reqID);
 							requests.get(reqId).write(event);
 							requests.remove(reqId);
 						}
 					}
 				});
 
 
 				// When the socket is closed.
 				in.onClose(new Callback0() {
 					public void invoke() {
 
 					}
 
 
 				});
 
 			}
 
 		};
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
