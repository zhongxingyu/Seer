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
 
 import play.Logger;
 import play.libs.F.Callback;
 import play.libs.F.Callback0;
 import play.mvc.Controller;
 import play.mvc.WebSocket;
 /**
  * @author romanelm
  */
 public class WeatherController extends Controller {
 
 	public static Integer MAX_REQUEST = 3;
 
 
 	/**
 	 * Hashmap that given an ID of a Display, returns 
 	 * an list of 2 websockets: one for the small view
 	 * and one for the big one.	
 	 * Position 0: small
 	 * Position 1: big
 	 */
 	public static HashMap<String, ArrayList<WebSocket.Out<JsonNode>>> sockets = 
 			new HashMap<String, ArrayList<WebSocket.Out<JsonNode>>>();
 
 	/**
 	 * Hashmap that given an ID of a Display, returns 
 	 * an Integer that represent the number of requests
 	 * that can be sent to the display.
 	 */
 	public static HashMap<String, Integer> status = new HashMap<String, Integer>();
 
 
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
 							sockets.put(displayID, new ArrayList<WebSocket.Out<JsonNode>>());
 							status.put(displayID, MAX_REQUEST);
 						}
 
 						if(messageKind.equals("appReady")){
 
 							// Can be either small or big
 							String size = event.get("size").asText();
 
 							if(size.equals("small")){
 								sockets.get(displayID).add(0, out);
 							} else {
 								sockets.get(displayID).add(1, out);
 							}
 
 							Logger.info(sockets.toString());
 
 							Logger.info(
 									"\n ******* MESSAGE RECIEVED *******" +
 											"\n The "+ size + " view of \n" +
 											"weather app is now available on displayID: " + displayID +
 											"\n*********************************"
 									);
 
 							// TODO: look for defaults values
 							findForecast("adsqw");
 						} else if(messageKind.equals("mobileRequest")){
 							Integer spacesLeft = status.get(displayID);
 							if(spacesLeft > 0){
 								// DO SOMETHING
 
 								status.put(displayID, spacesLeft-1);
 								Logger.info(status.toString());
 							} else {
 								// TODO: put in queue or notify mobile
 							}
 
 
 							//							String username = event.get("username").asText();
 							//							String location = event.get("preference").asText();
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
 			}
 			
 			String woeid = actualObj.get("places").get("place").get(0).get("woeid").asText();
 			
 			Logger.info(woeid);
 
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
 
 
 
 }
