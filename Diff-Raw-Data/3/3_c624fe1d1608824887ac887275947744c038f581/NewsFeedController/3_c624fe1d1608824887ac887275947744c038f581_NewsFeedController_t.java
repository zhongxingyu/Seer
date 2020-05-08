 
 package controllers;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import net.htmlparser.jericho.Attribute;
 import net.htmlparser.jericho.Attributes;
 import net.htmlparser.jericho.HTMLElementName;
 import net.htmlparser.jericho.MasonTagTypes;
 import net.htmlparser.jericho.MicrosoftConditionalCommentTagTypes;
 import net.htmlparser.jericho.PHPTagTypes;
 import net.htmlparser.jericho.Segment;
 import net.htmlparser.jericho.Source;
 import net.htmlparser.jericho.StartTag;
 
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.node.ObjectNode;
 
 import play.Logger;
 import play.libs.F.Callback;
 import play.libs.F.Callback0;
 import play.libs.Json;
 import play.mvc.Controller;
 import play.mvc.WebSocket;
 import play.mvc.WebSocket.Out;
 /**
  * @author romanelm
  */
 public class NewsFeedController extends Controller {
 
 
 	/**
 	 * Hashmap that given an ID of a Display, returns 
 	 * an list of 2 websockets: one for the small view
 	 * and one for the big one.	
 	 * Position 0: small
 	 * Position 1: big
 	 */
 	public static HashMap<String, Sockets> sockets = 
 			new HashMap<String, Sockets>();
 
 	/**
 	 * Hashmap that given an ID of a Display, returns 
 	 * how many request the application can still recieve
 	 */
 	public static HashMap<String,Integer> status = 
 			new HashMap<String, Integer>();
 
 	/**
 	 * The number of maximum request must be multiplied
 	 * by two because we have a SMALL and a BIG view 
 	 * TEST VALUE
 	 */
 	public static Integer MAX_REQ = 1000*2;
 
 	public static WebSocket<JsonNode> webSocket() {
 		return new WebSocket<JsonNode>() {
 
 			// Called when the Websocket Handshake is done.
 			public void onReady(WebSocket.In<JsonNode> in, final WebSocket.Out<JsonNode> out) {
 
 				in.onMessage(new Callback<JsonNode>() {
 					public void invoke(JsonNode event) {
 
 						Logger.info("INCOMING MESSAGE ON NEWSFEED WS:\n" 
 								+ event.toString());
 
 						String messageKind = event.get("kind").asText();						
 						String displayID = event.get("displayID").asText();
 
 						if(!sockets.containsKey(displayID)){
 							sockets.put(displayID, new Sockets(null, null));
 							status.put(displayID, MAX_REQ);
 							Logger.info("DisplayID " + displayID + " was added to the system.");
 						}
 
 						if(messageKind.equals("appReady")){
 
 							Logger.info("Newsfeed app is ready!");
 
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
 											"newsfeed app is now available on displayID: " + displayID +
 											"\n*********************************"
 									);
 
 						} else if(messageKind.equals("mobileRequest")){
 
 							Integer freeSpaces = status.get(displayID);
 							if(freeSpaces>0){								
 
 								String username = event.get("username").asText();
 								JsonNode feeds = event.get("preference");
 								ObjectNode response = processFeeds(feeds);
 
 								Sockets displaySockets = sockets.get(displayID);
 
 								// Send the forecast to the two views of the application
 								displaySockets.small.write(response);
 								displaySockets.big.write(response);
 
 								Logger.info("JSON SENT TO THE DISPLAY!");
 								status.put(displayID, freeSpaces-2);
 
 							} else {
 								// TODO: put in queue or notify mobile
 							}
 						} else {
 							Logger.info("WTF: " + event.toString());
 						}
 
 					}
 
 				});
 
 				// When the socket is closed.
 				in.onClose(new Callback0() {
 					public void invoke() {
 						Logger.info("\n ******* MESSAGE RECIEVED *******" +
 								"\n A weather tile on " + "TODO" +
 								"\n is now disconnected." +
 								"\n*********************************"
 								);
 					}
 
 
 				});
 
 			}
 
 		};
 	}
 
 	public static JsonNode xmlToJSON(String feedURL) {
 		String baseURL = "https://ajax.googleapis.com/ajax/services/feed/load?v=1.0&q=" + feedURL + "&num=5";
 		try {
 			URL url = new URL(baseURL);
 			URLConnection connection = url.openConnection();
 
 			String line;
 			StringBuilder builder = new StringBuilder();
 			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 			while((line = reader.readLine()) != null) {
 				builder.append(line);
 			}
 
 			ObjectMapper mapper = new ObjectMapper();
 			JsonNode df = mapper.readValue(builder.toString(), JsonNode.class);
 			return df;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 
 
 	/**
 	 * Given a JSON containing a set of feeds sources,
 	 * it returns a new JSON containing the titles of the various
 	 * news.
 	 * @param json of feeds recieved from the mobile 
 	 * @return
 	 */
 	public static ObjectNode processFeeds(JsonNode feeds) {		
 		JsonNode hot = feeds.get("hot");
 		JsonNode tech = feeds.get("tech");
 		JsonNode sport = feeds.get("sport");
 		JsonNode culture = feeds.get("culture");
 
 		Logger.info
 		(
 				"FEEDS RECIEVED: \n"
 						+ "\n\n" + hot.toString() 
 						+ "\n\n" + tech.toString()  
 						+ "\n\n" + sport.toString()  
 						+ "\n\n" + culture.toString()
 				);
 
 
 		// Build the JSON that is going to be sent back
 		// to the display.
 		ObjectNode response = Json.newObject();
 		try {
 			response.put("hot", extractInformations(hot));
 			response.put("tech", extractInformations(tech));
 			response.put("sport", extractInformations(sport));
 			response.put("culture", extractInformations(culture));
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 		return response;
 	}
 
 	public static JsonNode extractInformations(JsonNode feed) throws MalformedURLException {		
 
 		ArrayList<ObjectNode> feedsTitles = new ArrayList<ObjectNode>();
 		Iterator<JsonNode> it = feed.getElements();
 
 		while(it.hasNext()){
 			JsonNode jsonFeed = xmlToJSON(it.next().asText()).get("responseData").get("feed");
 			String newsSource = jsonFeed.get("title").asText();
 			Logger.info("PROCESSING: " + newsSource);
 			Iterator<JsonNode> entries = jsonFeed.get("entries").getElements();
 			while(entries.hasNext()){
 				JsonNode currentEntry = entries.next();
 				ObjectNode currentNews = Json.newObject();
 				currentNews.put("source", newsSource);
 				String content = currentEntry.get("content").asText();
 				if(content == null){
 					continue;
 				}
 
 				String link = currentEntry.get("link").asText();
 				MicrosoftConditionalCommentTagTypes.register();
 				PHPTagTypes.register();
 				PHPTagTypes.PHP_SHORT.deregister();
 				MasonTagTypes.register();
 				Source source;
 				try {
 					source = new Source(new URL(link));
 					List<? extends Segment> elementList =source.getAllElements(HTMLElementName.IMG);
 					for (Segment segment : elementList) {
 						Attributes tagAttr = segment.getFirstStartTag().getAttributes(); 
 						final Attribute alt = tagAttr.get("alt");
 						
						if(tagAttr.getValue("width") == null || tagAttr.getValue("height") == null) 
							continue;
 						
 						final Integer width = new Integer(tagAttr.getValue("width"));
 						final Integer height = new Integer(tagAttr.getValue("height"));
 						
 						if (alt!=null && width > 100 && height > 100){
 							System.out.println("-------------------------------------------------------------------------------");
 							System.out.println(segment.getDebugInfo());
 							System.out.println("W: " + width + " H: " + height);
 							System.out.println(segment);
 						}
 					}
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 				currentNews.put("link", link);
 				currentNews.put("title", currentEntry.get("title").asText());
 				currentNews.put("content", content);
 
 
 				feedsTitles.add(currentNews);
 			}
 		}
 
 		JsonNode jsonFeedsTitles = Json.toJson(feedsTitles);
 		return jsonFeedsTitles;
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
