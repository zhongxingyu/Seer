 package controllers;
 
 import play.*;
 import play.modules.facebook.FbGraph;
 import play.modules.facebook.FbGraphException;
 import play.modules.facebook.Parameter;
 import play.mvc.*;
 import play.mvc.Scope.Session;
 
 import java.util.*;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 
 import models.*;
 
 public class Application extends Controller {
 
 	public static String[] feedLinks = {"http://feeds.feedburner.com/TechCrunch/"};
 	public static String[] feedCategories = {"Technology"};
 	
     public static void index() {
     	//generateFeeds();
     	if (Session.current().contains("user")) {
     		RecommendationEngine.index();
     	} else {
     		render();
     	}
     }
 
     public static boolean getUserLikes(){
     	User loggedInUser = User.findById(Long.parseLong(Session.current().get("user"))); 
     	try {
     		String userName = loggedInUser.userName;
     		StringBuffer queryPart = new StringBuffer(userName+"/likes");
 			JsonArray userLikes = FbGraph.getConnection(queryPart.toString(), Parameter.with("limit", "1000").parameters());
 			
 			loggedInUser.addAllLikes(userLikes);
     	} catch (FbGraphException e) {
 			e.printStackTrace();
 		}
     	return true;
     }
     
     public static void displayFriends(){
     	//Get User Likes
     	getUserLikes();
     	render();
     }
     
     /* Facebook Session Stuff */
     public static void facebookLogin() {
         try {
             JsonObject profile = FbGraph.getObject("me"); // fetch the logged in user
             System.out.println(profile);
             //System.out.println(profile.get("email"));
             //String email = profile.get("email").getAsString(); // retrieve the email
             List<User> userList = User.find("byUserId", profile.get("id")).fetch();
             User user;
             
             if (userList == null || userList.size() == 0) {
 	             user = new User(profile.get("name").toString().replaceAll("\"", ""), profile.get("username").toString().replaceAll("\"", ""), profile.get("id").toString().replaceAll("\"", ""));
 	             user.save();
             } else {
             	user = userList.get(0);
             }
             
             Session.current().put("user", user.id);
             // do useful things
             Session.current().put("username", "xxx"); // put the email into the session (for the Secure module)
         } catch (FbGraphException fbge) {
             flash.error(fbge.getMessage());
             if (fbge.getType() != null && fbge.getType().equals("OAuthException")) {
                 Session.current().remove("username");
             }
         }
         RecommendationEngine.index();
     }
 
     public static void facebookLogout() {
     	System.out.println("Logging out");
         Session.current().remove("username");
         FbGraph.destroySession();
         index();
     }
 
     public static void login(String username, String password){
     	System.out.println(username);
     	JsonObject js = new JsonObject();
     	js.addProperty("status","ok");
     	renderJSON(js.toString());
     }
 
     public static void login(String username){
     	System.out.println(username);
     	JsonObject js = new JsonObject();
     	js.addProperty("status","ok");
     	renderJSON(js.toString());
     }
 
     public static JsonObject getUserInformation(String userName, String authenticationToken){
     	try {
 			JsonObject user = FbGraph.getObject(userName, Parameter.with("access_token", authenticationToken).parameters());
 		} catch (FbGraphException e) {
 			System.out.println("There was an error in the getUserInformationMethod");
 			e.printStackTrace();
 		}
     	JsonObject obj = new JsonObject();
     	obj.addProperty("test", "Is Extracting");
     	return obj;
     }
     
    public static void processRequest(int topic) {
    	index();
     /* Database Stuff */
     public static boolean generateFeeds() {
     	clearAll();
     	
     	for (int i = 0; i < feedLinks.length; i++) {
     		Feed feed = new Feed(feedLinks[i]);
     		feed.tags.add(feedCategories[i]);
     		feed.lastUpdate = new Date(0);
     		feed.save();
     	}
     	return true;
     }
     
     public static boolean clearAll() {
     	List<Choice> choices = Choice.findAll();
     	Iterator<Choice> choiceItor = choices.iterator();
     	while (choiceItor.hasNext()) {
     		choiceItor.next().delete();
     	}
     	List<Recommendation> recommendations = Recommendation.findAll();
     	Iterator<Recommendation> recItor = recommendations.iterator();
     	while (recItor.hasNext()) {
     		recItor.next().delete();
     	}
     	List<Topic> topics = Topic.findAll();
     	Iterator<Topic> topicItor = topics.iterator();
     	while (topicItor.hasNext()) {
     		topicItor.next().delete();
     	}
     	List<Feed> feeds = Feed.findAll();
     	Iterator<Feed> feedItor = feeds.iterator();
     	while (feedItor.hasNext()) {
     		feedItor.next().delete();
     	}
     	return true;
     }
     
     public void testRequest(){
     	renderJSON("Please Place Json String here");
     }
 }
