 /**
  * 
  */
 package controllers;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.codehaus.jackson.JsonFactory;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.JsonParser;
 import org.codehaus.jackson.map.ObjectMapper;
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
 public class WeatherController extends Controller {
 
 
 	/**
 	 * Hashmap that given an ID of a Display, returns 
 	 * an list of 2 websockets: one for the small view
 	 * and one for the big one.	
 	 * Position 0: small
 	 * Position 1: big
 	 */
 	public static HashMap<String, Sockets> sockets = new HashMap<String, Sockets>();
 
 	/**
 	 * Hashmap that given an ID of a Display, returns 
 	 * how many request the application can still recieve
 	 */
 	public static HashMap<String,Integer> status = new HashMap<String, Integer>();
 
 	public static HashMap<String,ArrayList<String>> activeCities = new HashMap<String, ArrayList<String>>();
 
 
 	/**
 	 * The number of maximum request must be multiplied
 	 * by two because we have a SMALL and a BIG view 
 	 */
 	public static Integer MAX_REQ = 3*2;
 
 	public static WebSocket<JsonNode> webSocket() {
 		return new WebSocket<JsonNode>() {
 
 			// Called when the Websocket Handshake is done.
 			public void onReady(WebSocket.In<JsonNode> in, final WebSocket.Out<JsonNode> out) {
 
 				in.onMessage(new Callback<JsonNode>() {
 					public void invoke(JsonNode event) {
 
 						Logger.info("MESSAGE FOR WEATHER WS");
 						Logger.info(event.toString());
 
 						String messageKind = event.get("kind").asText();						
 						String displayID = event.get("displayID").asText();
 
 						if(!sockets.containsKey(displayID)){
 							sockets.put(displayID, new Sockets(null, null));
 							status.put(displayID, MAX_REQ);
							activeCities.put(displayID, new ArrayList<String>());
 							Logger.info("DisplayID " + displayID + " was added to the system.");
 						}
 
 						if(messageKind.equals("appReady")){
 
 							// Can be either small or big
 							String size = event.get("size").asText();
 
 							if(size.equals("small")){
 								sockets.get(displayID).small = out;
 							} else if(size.equals("big")) {
 								sockets.get(displayID).big  = out;
 							}
 
 
 							Logger.info(
 									"\n ******* MESSAGE RECIEVED *******" +
 											"\n The "+ size + " view of \n" +
 											"weather app is now available on displayID: " + displayID +
 											"\n*********************************"
 									);
 
 							// TODO: look for defaults values
 
 						} else if(messageKind.equals("mobileRequest")){
 
 							Integer freeSpaces = status.get(displayID);
 							String location = event.get("preference").asText();
 							Logger.info("W FOR " + location);
 							if(freeSpaces>0 && !activeCities.get(displayID).contains(location)){								
 
 								JsonNode forecast = findForecast(location);
 								ObjectNode result = Json.newObject();
 								result.put("original_request",location);
 								result.put("forecast",forecast);
 								Logger.info(result.toString());
 								
 								Sockets displaySockets = sockets.get(displayID);
 
 								// Send the forecast to the two views of the application
 								displaySockets.small.write(result);
 								displaySockets.big.write(result);
 								Logger.info("SENT");
 								
 								Logger.info(forecast.toString());
 								status.put(displayID, freeSpaces-2);
 								activeCities.get(displayID).add(location);
 							} else {
 								Logger.info("FULL OR DUPLICATE");
 							}
 
 
 						} else if(messageKind.equals("free")){
 							Integer freeSpaces = status.get(displayID);
 							activeCities.get(displayID).remove(event.get("location").asText());
 							status.put(displayID, freeSpaces+1);
 						} else {
 							Logger.info("WTF: " + event.toString());
 						}
 					}
 				});
 
 				// When the socket is closed.
 				in.onClose(new Callback0() {
 					public void invoke() {
 						Logger.info("\n ******* MESSAGE RECIEVED *******" +
 								"\n A weather tile on " + "FILL" +
 								"\n is now disconnected." +
 								"\n*********************************"
 								);
 					}
 
 
 				});
 
 			}
 
 		};
 	}
 
 	public static JsonNode findForecast(String location){
 
 		// Language
 		String lang = "it";
 
 		// Maximum number of city to find (by proximity) 
 		String max = "5";
 
 		// AppID for http://developer.yahoo.com/
 		String appid = "QBulK6jV34F7ZJY2YP0RMtFHI7YJBE9pouDkGGBpKf9eSGzJBvDZq91dUzo60tp3XuFsjv7PvQHU";
 
 		try {
 
 			// Request for YAHOO WEATHER API
 			String request = 
 					"http://where.yahooapis.com/v1/places.q('"+ URLEncoder.encode(location,"UTF-8") + "');" +
 							"start=0;count="+ max +"&" +
 							"lang=" + lang + "?" +
 							"format=json&" +
 							"appid=" + appid;
 
 			// Extract from the generated JSON  the WOEID (if any) of the location
 			ObjectMapper mapper = new ObjectMapper();
 			JsonFactory factory = mapper.getJsonFactory();
 			JsonParser jp = factory.createJsonParser(readUrl(request));
 			JsonNode actualObj = mapper.readTree(jp);
 
 			// Check if we found any city
 			if(actualObj.get("places").get("total").asInt() == 0){
 				Logger.info("City not found");
 				// TODO: city not found!
 			} else {
 
 				// Extract the woeid from the JSON
 				String woeid = actualObj.get("places").get("place").get(0).get("woeid").asText();
 
 				String unit = "c";
 				String request2 = "http://weather.yahooapis.com/forecastjson?w=" 
 						+ woeid + "&"
 						+ "u=" + unit 
 						+ "&d=4";
 				jp = factory.createJsonParser(readUrl(request2));
 				return mapper.readTree(jp);
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 	private static String readUrl(String urlString) throws Exception {
 		BufferedReader reader = null;
 		try {
 			URL url = new URL(urlString);
 			reader = new BufferedReader(new InputStreamReader(url.openStream()));
 			StringBuffer buffer = new StringBuffer();
 			int read;
 			char[] chars = new char[2048];
 			while ((read = reader.read(chars)) != -1)
 				buffer.append(chars, 0, read); 
 
 			return buffer.toString();
 		} finally {
 			if (reader != null)
 				reader.close();
 		}
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
