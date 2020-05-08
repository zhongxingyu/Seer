 /**
  * 
  */
 package controllers;
 
 import java.io.InputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.node.ObjectNode;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import play.Logger;
 import play.libs.F.Callback;
 import play.libs.F.Callback0;
 import play.libs.Json;
 import play.mvc.Controller;
 import play.mvc.WebSocket;
 /**
  * @author romanelm
  */
 public class WeatherController extends Controller {
 
 
 	public static Map<String, ArrayList<WebSocket.Out<JsonNode>>> freeTiles = new HashMap<String, ArrayList<WebSocket.Out<JsonNode>>>();
 	// Tells the displayID given a websocket out
 	public static Map<WebSocket.Out<JsonNode>,String> fromWStoDisplayID = new HashMap<WebSocket.Out<JsonNode>, String>();
 	// Tells the Tile specifications given a websocket out
 	public static Map<WebSocket.Out<JsonNode>,WeatherController.Tile> fromWStoTile = new HashMap<WebSocket.Out<JsonNode>,WeatherController.Tile>();
 
 	// Tiles already used for dafault requests
 	public static ArrayList<WebSocket.Out<JsonNode>> usedDefaultTiles = new ArrayList<WebSocket.Out<JsonNode>>();
 
 	public static WebSocket<JsonNode> webSocket() {
 		return new WebSocket<JsonNode>() {
 
 			// Called when the Websocket Handshake is done.
 			public void onReady(WebSocket.In<JsonNode> in, final WebSocket.Out<JsonNode> out) {
 
 				in.onMessage(new Callback<JsonNode>() {
 					public void invoke(JsonNode event) {
 						Logger.info("A FUCKING MESSAGE!");
 						Logger.info(event.toString());
 						String messageKind = event.get("kind").asText();						
 						String displayID = event.get("displayID").asText();
 
 						if(messageKind.equals("tileAvailable")){
 
 							String width = event.get("width").asText();
 							String height  = event.get("height").asText();
 
 							Logger.info(
 									"\n ******* MESSAGE RECIEVED *******" +
 											"\n New weather tile available on " + displayID +
 											"\n SIZE: (" + width + "," + height + ")" +
 											"\n*********************************"
 									);
 
 							saveTile(out, displayID, width, height);
 
 						} else if(messageKind.equals("mobileRequest")){
 							String username = event.get("username").asText();
 							String location = event.get("preference").asText();
 							processInput(displayID, username, location, false);
 						} else if(messageKind.equals("defaultRequest")){
 							String location = event.get("preference").asText();
 							processInput(displayID, "default", location, true);
 						}else {
 							Logger.info("WTF: " + event.toString());
 						}
 
 					}
 
 					private void processInput(String displayID,String username, String location, boolean isDefault) {
 						try {
 							Out<JsonNode> tileOut = null;
 							if(isDefault){
 								tileOut = findDefaultDestinationTile(displayID,0,0);
 							} else{
 								tileOut = findDestinationTile(displayID,0,0);
 							}
 
 							if (tileOut == null){
 								Logger.info("SORRY NO SPACE");
 							} 
 							else {
 								Logger.info(
 										"\n ******* MESSAGE RECIEVED *******" +
 												"\n" + username + " on " + displayID +
 												"\nrequest weather of " + location +
 												"\n*********************************"
 										);
 
 								String weatherXMLFeed = "http://www.google.com/ig/api?weather=" + location;
 
 								WeatherController.Tile tile = fromWStoTile.get(tileOut);
 
 								DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
 								DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
 
 								URL xmlUrl = new URL(weatherXMLFeed);
 								InputStream in = xmlUrl.openStream();
 								Document doc = docBuilder.parse(in);	
 								doc.getDocumentElement().normalize();
 								NodeList current = doc.getElementsByTagName("current_conditions");
 								Node currentForecast = current.item(0);
 								ArrayList<String> info = processAttributes(currentForecast, location);
 								JsonNode today = Json.toJson(info);
 								ObjectNode response = Json.newObject();
 								response.put("kind", "forecast");
 								response.put("today",today);
								Logger.info("HEHRE");
 
 								if(tile.width == 2 && tile.height == 2){
 									tileOut.write(response);
 								} else if (tile.width == 4 && tile.height == 4){
 									NodeList forecastConditions = doc.getElementsByTagName("forecast_conditions");
 									int forecastDays = forecastConditions.getLength();
 									for(int s=0; s < forecastDays ; s++){
 										Node curr = forecastConditions.item(s);
 										ArrayList<String> currentInfo = processAttributes(curr,location);
 										JsonNode dayJson = Json.toJson(currentInfo);
 										response.put("day"+s,dayJson);
 									}
 									tileOut.write(response);
 								}
 								if(!isDefault){
 									removeTileFromAvailable(tileOut);
 								}
 							}
 
 						} catch (Exception e) {
 							e.printStackTrace();
 						}
 					}
 
 					private void saveTile(final WebSocket.Out<JsonNode> out,String displayID, String width, String height) {
 						if(freeTiles.containsKey(displayID)){
 							freeTiles.get(displayID).add(out);
 						} else {
 							ArrayList<WebSocket.Out<JsonNode>> outs = new ArrayList<WebSocket.Out<JsonNode>>();
 							outs.add(out);
 							freeTiles.put(displayID, outs);
 						}
 						fromWStoDisplayID.put(out, displayID);
 						fromWStoTile.put(out, new WeatherController.Tile(new Integer(width), new Integer(height)));
 					}
 
 					private ArrayList<String> processAttributes(Node currentForecast, String location) {
 						NodeList list = currentForecast.getChildNodes(); 
 						int length = list.getLength();
 						ArrayList<String> info = new ArrayList<String>();
 						if (length > 0) { 
 							// Loop over the attributes and add them to an
 							// arraylist
 							for (int i = 0; i < length; i++){
 								Node currentInfo = list.item(i);
 								NamedNodeMap attrs = currentInfo.getAttributes();
 								Attr attribute = (Attr) attrs.item(0);
 								info.add(attribute.getValue());
 							}
 						}
 						info.add(location);
 						return info;
 					} 
 				});
 
 				// When the socket is closed.
 				in.onClose(new Callback0() {
 					public void invoke() {
 						String displayID = removeTileFromAvailable(out);
 						Logger.info("\n ******* MESSAGE RECIEVED *******" +
 								"\n A weather tile on " + displayID +
 								"\n is now disconnected." +
 								"\n*********************************"
 								);
 					}
 
 
 				});
 
 			}
 
 		};
 	}
 
 
 	/**
 	 * Find the socket of a tile within the given constraint of width and height. Later
 	 * removes it from the available tiles
 	 * @param displayID
 	 * @param minWidth
 	 * @param minHeight
 	 * @return
 	 */
 	public static WebSocket.Out<JsonNode> findDestinationTile(String displayID, Integer minWidth, Integer minHeight){
 		ArrayList<WebSocket.Out<JsonNode>> outs = freeTiles.get(displayID);
 		Iterator<WebSocket.Out<JsonNode>> it = outs.iterator();
 		while (it.hasNext()){
 			WebSocket.Out<JsonNode> out = it.next();
 			Tile currentTile = fromWStoTile.get(out);
 			if(currentTile.width >= minWidth && currentTile.height >= minHeight){
 				usedDefaultTiles.add(out);
 				return out;
 			}
 
 		}	
 		return null;
 	}
 
 	public static WebSocket.Out<JsonNode> findDefaultDestinationTile(String displayID, Integer minWidth, Integer minHeight){
 		ArrayList<WebSocket.Out<JsonNode>> outs = freeTiles.get(displayID);
 		Iterator<WebSocket.Out<JsonNode>> it = outs.iterator();
 		while (it.hasNext()){
 			WebSocket.Out<JsonNode> out = it.next();
 			if(!usedDefaultTiles.contains(out)){
 				Tile currentTile = fromWStoTile.get(out);
 				if(currentTile.width >= minWidth && currentTile.height >= minHeight){
 					usedDefaultTiles.add(out);
 					return out;
 				}
 			}
 		}	
 		return null;
 	}
 
 	public static String removeTileFromAvailable(final WebSocket.Out<JsonNode> out) {
 		String displayID = fromWStoDisplayID.get(out);
 		freeTiles.get(displayID).remove(out);
 		fromWStoDisplayID.remove(out);
 		fromWStoTile.remove(out);
 		return displayID;
 	}
 
 	public static class Tile {
 		final Integer width;
 		final Integer height;
 
 		public Tile(Integer width, Integer height) {
 			this.width = width;
 			this.height = height;
 		}		
 	} 
 
 }
