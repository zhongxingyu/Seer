 package utils;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import models.User;
 import play.Logger;
 import play.mvc.Router;
 import siena.Json;
 import controllers.Constants;
 
 public class BigBrotherHelper {
 	public static String CLIENT_ID="3HEIFZIGIX0WUJCJWPDZP1QPGQUIVVIOLNZ4ASRBYCUO3XN4";
 	public static String CLIENT_SECRET="UZ5ATEJSTJNPK2LKV0ZY11XHFV45YWYJKUHRFKGLUCX4ID4O";
 	private static String END_POINTS_URL="https://api.foursquare.com/v2/venues/search?v=20110910&oauth_token=";
 	private static String USER_DETAILS= " https://api.foursquare.com/v2/users/";
 	private static String VENUE_DETAILS= "https://api.foursquare.com/v2/venues/";
 
 	
 	
 	private static Json processEndPointSearchJson(Json raw){
 		Json venues = raw.get("response").get("venues");
 		Json processedVenues = Json.list();
 		for (Json json : venues) {
 			Json processedVenue = Json.map();
 			processedVenue.put("id", json.get("id"))
 							.put("name", json.get("name"))
 							.put("address", json.get("location").get("address"))
 							.put("count", json.get("hereNow").get("count"));
 			if (json.containsKey("categories") && !json.get("categories").isEmpty() && json.get("categories").at(0).containsKey("parents")) {
 				processedVenue.put("categories", json.get("categories").at(0).get("parents"));
 			} else {
 				processedVenue.put("categories", "[\"Home, Work, Others\"]");
 				
 			}
 			
 			if (json.containsKey("categories") && !json.get("categories").isEmpty() && json.get("categories").at(0).containsKey("icon")) {
 				processedVenue.put("image", json.get("categories").at(0).get("icon"));
 			} else {
 				processedVenue.put("image", "https://foursquare.com/img/categories/building/default.png");
 			}
 			
 			processedVenues.add(processedVenue);
 		}
 		return processedVenues;
 	}
 	
 	public static Json consume4SQCoordinates(String coordinates, String token){
 		String url = END_POINTS_URL + token + "&ll="+coordinates + "&v=20110910";
 		try {
 			Logger.info("url -> " + url);
 			Json results=URLHelper.fetchJson(url);
 			Logger.info("results " + results.toString());
 			return(results);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	public static String retrieveToken(String code){
 		String uriCB = Router.getFullUrl("controllers.Application.index");
 		String url = "https://foursquare.com/oauth2/access_token"
 			  			+"?client_id="+CLIENT_ID
 			  			+"&client_secret="+CLIENT_SECRET
 			  			+"&grant_type=authorization_code"
 			  			+"&redirect_uri="+uriCB
 			  			+"&code="+code;
 		try {
 			Json token = URLHelper.fetchJson(url);
 			return token.get("access_token").str();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	public static Json getVenues(String coordinates, String token) {
 		Json rawVenues = consume4SQCoordinates(coordinates, token);
 		Json venues = processEndPointSearchJson(rawVenues);
 		return venues;
 	}
 	
 	private static Json queryByUserId(String uid, String token){
 		Json data = Json.map();
 		String url = USER_DETAILS+uid+"?oauth_token=" + token;
 		try {
 			Json userData = URLHelper.fetchJson(url);
 			data = userData.get("response").get("user").get("contact");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return data;
 	}
 	
 	public static List<User> queryVenue(String venueId, String token) {
 		String url = VENUE_DETAILS+venueId+"?oauth_token=" + token;
 		List<User> people = new ArrayList<User>();
 		try {
 			Json details = URLHelper.fetchJson(url).get("response").get("venue").get("hereNow");
 			
 			for (Json json : details.get("groups")) {
 				String type = json.get("type").str();
 				Json items = json.get("items");
 				if(items.isEmpty() || items.isNull()) continue;
 				for(Json json2 : items){
 					String uid = json2.get("user").get("id").str();
 					User user = User.all().filter("fourSquareId", uid).get();
 					if (user == null) { 
 						Json item = json.map();
 						item.put("type", type);
 						item.put("firstName", json2.get("user").get("firstName")); 
 						item.put("lastName", json2.get("user").get("lastName"));
 						item.put("photo", json2.get("user").get("photo")); 
 
 						Json contactInfo = queryByUserId(uid, token);
 						item.put("id", uid);
 						item.put("contact", contactInfo);
 						item.put("bio", TwitterScrapper.getTwitterInfo(item.get("contact")));
 						Json services = Json.map();
 						Json tempServices = QwerlyHelper.getUserServices(item.get("contact"));
 						if(tempServices != null && !tempServices.isNull() && !tempServices.isEmpty()){
 							services = tempServices;
 						}
 						if (contactInfo.containsKey("facebook") && !services.containsKey("facebook")) {
 							services.put("facebook", Json.map().put("url", "http://www.facebook.com/" + contactInfo.get("facebook").str()).put("username", contactInfo.get("facebook").str()));
 						}
 						if (contactInfo.containsKey("twitter") && !services.containsKey("twitter")) {
 							services.put("twitter", Json.map().put("url", "http://twitter.com/" + contactInfo.get("twitter").str()).put("username", contactInfo.get("twitter").str()));
 						}
 						if (!services.containsKey("foursquare")) {
							services.put("foursquare", Json.map().put("url", "https://foursquare.com/" + uid).put("username", uid));
 						}
 						item.put("services", services);
 						user = User.createUserFromItem(item);
 						user.insert();
 						
 					}
 					
 					people.add(user);
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return people;
 	}
 	
 }
