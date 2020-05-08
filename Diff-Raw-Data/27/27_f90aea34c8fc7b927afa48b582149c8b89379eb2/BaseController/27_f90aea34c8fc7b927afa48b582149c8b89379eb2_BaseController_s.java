 package controllers;
 
 import java.util.ArrayList;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 
 import models.Alias;
 import models.User;
 import play.libs.WS;
 import play.mvc.Controller;
 import play.mvc.Http.StatusCode;
 import siena.Model;
 
 public class BaseController extends Controller {
 	public static User getUser() {
 		String token = request.headers.get("authorization").values.get(0);
 		User owner = Model.all(User.class).filter("token", token.replaceAll("\"", "")).get();
 		return owner;
 	}
 	
 	public static void badRequest(String msg) {
 		response.status = StatusCode.BAD_REQUEST;
 		renderJSON(msg);
 	}
 	
 	public static User getAliases(User user) {
 		if (!StringUtils.isBlank(user.singlyAccessToken)) {
 			if (user.aliases == null) {
 				user.aliases = new ArrayList<Alias>();
 			}
 			try {
 				JsonElement singlyProfile = WS.url("https://api.singly.com/profile?access_token=" + user.singlyAccessToken).get().getJson();
 				JsonObject asJsonObject = singlyProfile.getAsJsonObject();
 				JsonObject asJsonObject2 = asJsonObject.get("services").getAsJsonObject();
 				Set<Entry<String, JsonElement>> entrySet = asJsonObject2.entrySet();
 				for (Entry<String, JsonElement> entry : entrySet) {
 					Alias alias = new Alias();
 					alias.service = entry.getKey();
 					alias.name = asJsonObject2.get(entry.getKey()).getAsJsonObject().get("name").getAsString();
 					if (user.aliases == null) {
 						user.aliases = new ArrayList<Alias>();
 					}
 					user.aliases.add(alias);
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			try {
 				JsonElement tumblrProfile = WS.url("https://api.singly.com/profiles/tumblr?access_token="+ user.singlyAccessToken).get().getJson();
 				JsonElement blogs = tumblrProfile.getAsJsonObject().get("data").getAsJsonObject().get("blogs");
 				if (blogs != null) {
 					JsonArray blogArray = blogs.getAsJsonArray();
 					System.out.println(blogArray.size());
 					System.out.println(blogArray.get(0).getAsJsonObject().get("name").getAsString());
 					if (blogArray.size() > 0) {
 						Alias alias = new Alias();
 						alias.service = "tumblr";
 						alias.name = blogArray.get(0).getAsJsonObject().get("name").getAsString();
 						if (user.aliases == null) {
 							user.aliases = new ArrayList<Alias>();
 						}
 						user.aliases.add(alias);
 					}
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		return user;
 	}
 }
