 package controllers;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.codehaus.jackson.JsonNode;
 
 import play.*;
 import models.User;
 import play.db.ebean.Model;
 import play.libs.WS;
 import play.libs.F.Promise;
 import play.libs.WS.Response;
 import play.mvc.*;
 import static play.libs.Json.toJson;
 
 import views.html.*;
 
 public class Application extends Controller {
   
     public static Result index() {
         return ok(index.render("Your new application is ready."));
     }
     
     public static Result getUsers() {
     	List<User> users = new Model.Finder(String.class, User.class).where().orderBy("name DESC").findList();
         System.out.println(toJson(users));
         if(users.size() > 10)
         {
         	users = users.subList(0, 10);
         }
         return ok(toJson(users));
     }
   
     public static Result addUser() {
         String topTweet = "";
 		int totalCarlinTweets = 0;
 		int highestCarlinCount = 0;
		int carlinIndex = 0;
		
         final Map<String, String[]> values = request().body().asFormUrlEncoded();
         String name = (String)values.get("name")[0];
         Promise<Response> response =  WS.url("https://api.twitter.com/1.1/statuses/user_timeline.json")
         		   .setHeader("Authorization", "Bearer AAAAAAAAAAAAAAAAAAAAAOkUQgAAAAAA9bzlYuX3WxYe2XJWNq2Jb9h5sME%3DwT0AeQkNAkAJuRjG0Hx3ynjFU8VFoJ7LGnhsSqmVZsE")
         		   .setQueryParameter("screen_name", name)
         		   .setQueryParameter("count", "200")
         		   .get();
         		    
         JsonNode node = response.get().asJson();
         if(node != null)
        { 
         	List<JsonNode> tweets = node.findValues("text");
         	if(tweets != null)
         	{
         		Iterator<JsonNode> tweetsIterator = tweets.iterator();
         		while(tweetsIterator.hasNext())
         		{
         			String tweet = tweetsIterator.next().asText();
         			String[] words = tweet.toLowerCase().split("\\s+");
         			int carlinCount = 0;
         			for (String word : words) {
         			    if(word.equalsIgnoreCase("cocksucker") || word.equalsIgnoreCase("motherfucker")
         			    		|| word.equalsIgnoreCase("fuck") || word.equalsIgnoreCase("shit")
         			    		|| word.equalsIgnoreCase("cunt") || word.equalsIgnoreCase("cock")
         			    		|| word.equalsIgnoreCase("pussy"))
         			    {
         			    	carlinCount++;
         			    }
         			}
         			if(carlinCount > highestCarlinCount)
         			{
         				highestCarlinCount = carlinCount;
         				topTweet = tweet;
         			}
         			if(carlinCount > 0)
         			{
         				totalCarlinTweets++;
         			}
         		}
         		if(tweets.size() > 0)
         		{
         			Logger.info(totalCarlinTweets + "/" + tweets.size());
        			carlinIndex = (int)((double)totalCarlinTweets / (double)tweets.size() * 100);
         		}
         	}
         }
         User newUser = new User(name,carlinIndex,topTweet);
         if(new Model.Finder(String.class, User.class).where().eq("name", name).findRowCount() > 0)
         {
         	 newUser.update();
         }
         else
         {
             newUser.save();
         }
        List<User> users = new Model.Finder(String.class, User.class).where().orderBy("carlinIndex DESC").findList();
         Logger.info(toJson(users).asText());
         if(users.size() > 10)
         {
         	users = users.subList(0, 10);
         }
        Logger.info(toJson(users).asText());
         return ok(toJson(users));
     }
 }
