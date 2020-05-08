 package controllers;
 
 import java.lang.System.*;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.UUID;
 
 import jobs.ClientGraph;
 import jobs.Start;
 import models.Comment;
 import models.Item;
 import models.Reply;
 import models.ReplyJson;
 import models.Tweet;
 import models.TweetJson;
 import models.User;
 import models.UserGraph;
 import models.Visitor;
 import play.Logger;
 import play.Play;
 import play.cache.Cache;
 import play.mvc.Controller;
 import play.mvc.With;
 import twitter.TwitterProxy;
 import twitter.TwitterProxyFactory;
 import util.FileUtils;
 import util.LinkShortener;
 import util.UserLookup;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.ning.http.client.websocket.WebSocket;
 
 import exception.NoAvailableTokenException;
 import exception.TSEException;
 import exception.UserProtectedException;
 
 @With({ Auth.class })
 public class Application extends Controller {
 	public static void index() {
 		List<Item> items = Item.findAll();
 		Long userId = Cache.get(session.getId(), Long.class);
 		if (userId != null) {
 			renderArgs.put("user", User.findById(userId));
 		}
 		render(items);
 	}
 
 	public static void showItem(Long itemId) {
 		Long userId = Cache.get(session.getId(), Long.class);
 		User user = null;
 		if (userId != null) {
 			user = User.findById(userId);
 			renderArgs.put("user", user);
 		}
 		Item product = Item.findById(itemId);
 		if (user != null && product != null) {
 			new Visitor(product, user).save();
 		}
 		render(product);
 	}
 
 	public static void addItem(String description, File picture) {
 		Long userId = Cache.get(session.getId(), Long.class);
 		User user = null;
 		if (userId == null) {
 			index();
 			return;
 		}
 		
 		user = User.findById(userId);
 		renderArgs.put("user", user);
 		
 		String fullUrl = null;
 		String generatedFileName = util.Codec.sha1_hex(UUID.randomUUID()
 				+ String.valueOf(Calendar.getInstance().getTimeInMillis()));
 		String fileName = generatedFileName + ".jpg";
 		try {
 			FileUtils.moveFile(picture, new File(Start.getImagePath()
 					+ fileName));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		fullUrl = request.current().getBase() + "/image/" + fileName;
 		Item item = new Item(description, fullUrl, user).save();
 		try{
 			String shortLink = LinkShortener.shorten(Play.configuration.getProperty("application.baseUrl")+"/item/"+item.id);
 			item.shortLink = shortLink == null ? Play.configuration.getProperty("application.baseUrl")+"/item/"+item.id : shortLink;
 		}catch (IOException e) {
 			item.shortLink = Play.configuration.getProperty("application.baseUrl")+"/item/"+item.id;
 		}
 		item.save();
 		index();
 	}
 
 	public static void updateItem(Long id, String description,
 			File picture) {
 		Long userId = Cache.get(session.getId(), Long.class);
 		User user = null;
 		if (userId != null) {
 			user = User.findById(userId);
 			renderArgs.put("user", user);
 		}
 		Item product = Item.findById(id);
 		String fullUrl = null;
 		if (picture != null) {
 			String generatedFileName = util.Codec.sha1_hex(UUID.randomUUID()
 					+ String.valueOf(Calendar.getInstance().getTimeInMillis()));
 			String fileName = generatedFileName + ".jpg";
 			try {
 				FileUtils.moveFile(picture, new File(Start.getImagePath()
 						+ fileName));
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			fullUrl = request.current().getBase() + "/image/" + fileName;
 		}
 		if (product != null) {
 			product.update(description, fullUrl);
 		}
 		profile(userId);
 	}
 
 	public static void itemData(Long id) {
 		Item item = Item.findById(id);
 		String error = null;
 		if (item == null) {
 			error = "This product does not exist";
 			render(error);
 		} else {
 			render(item);
 		}
 	}
 
 	public static void displayImage(String imageName) {
 		File file = new File(Start.getImagePath() + imageName);
 		renderBinary(file);
 	}

	public static void renderGraphDataJSON(String ownerId) {
		File file = new File(Start.getGraphJSONDataPath() + ownerId);
		try {
			renderJSON(readFileAsString(Start.getGraphJSONDataPath() + ownerId+".json"));
		} catch (IOException e) {
			Logger.error(e, e.getMessage());
		}
 	}
 	public static String readFileAsString(String filePath) throws java.io.IOException
 	{
 	    BufferedReader reader = new BufferedReader(new FileReader(filePath));
 	    String line, results = "";
 	    while( ( line = reader.readLine() ) != null)
 	    {
 	        results += line;
 	    }
 	    reader.close();
 	    return results;
 	}
 
 	public static void search(String query) {
 		Long userId = Cache.get(session.getId(), Long.class);
 		if (userId != null) {
 			renderArgs.put("user", User.findById(userId));
 		}
 		List<Item> items = new ArrayList<Item>();
 		if (query == null) {
 			render("application/index.html", items);
 		}
 		// get only first word
 		query = query.trim().split(" ")[0];
 		if (query == "") {
 			render("application/index.html", items);
 		}
 		items = Item.searchTitle(query);
 		render("application/index.html", items);
 	}
 	
 	public static void get(String query) {
 		User user = UserLookup.getUser(query);
 		UserGraph ug = UserGraph.getByOwnerId(user.twitterId);
 		if(ug==null){
 			ug = new UserGraph(user.twitterId);
 			ug.save();
 			renderJSON(constructBasicGraphJSON(user.twitterId));
 		}else{
			renderGraphDataJSON(String.valueOf(user.twitterId));
 		}
 	    
 	}
 
 	public static String constructBasicGraphJSON(Long ownerId){
 		Set<String> visibleLinks = new HashSet<String>();
 		TwitterProxy twitter = null;
 		try {
 			twitter = TwitterProxyFactory.defaultInstance();
 		} catch (TSEException e1) {
 			Logger.error(e1, e1.getMessage());
 		}
 		
 		List<Long> followings = null;
 		try {
 			followings = twitter.getFollowingIds(ownerId);
 		} catch (NoAvailableTokenException e) {
 			Logger.error(e, e.getMessage());
 		} catch (UserProtectedException e) {
 			Logger.error(e, e.getMessage());
 		}
 		if(followings.size()>0){
 			
 			for(Long following : followings){
 				visibleLinks.add(ownerId+"-"+following);
 				
 			}
 			
 		}
 		
 		List<Long> ownerAndFollowings = new ArrayList<Long>();
 		ownerAndFollowings.add(ownerId);
 		ownerAndFollowings.addAll(followings);
 		
 		Set<User> visibleUsers = UserLookup.getUsers(new HashSet(ownerAndFollowings));
 		
 		ClientGraph cg = new ClientGraph(ownerId, followings.size(), 0, visibleLinks, new ArrayList<User>(visibleUsers) );
 		Gson gson = new GsonBuilder().setPrettyPrinting().create();
 		String content = gson.toJson(cg, ClientGraph.class);
 			
 		return content;
 	}
 	public static void profile(Long profileId) {
 		Long userId = Cache.get(session.getId(), Long.class);
 		User user = null;
 		if (userId != null) {
 			user = User.findById(userId);
 			renderArgs.put("user", user);
 			
 		}
 		User profile = User.findById(profileId);
 		List<Item> items = Item.findItemsByUser(profile);
 		render(profile, items);
 	}
 
 	public static void deleteItem(Long itemId) {
 		Long userId = Cache.get(session.getId(), Long.class);
 		Item item = Item.findById(itemId);
 		User user = null;
 		if (userId != null) {
 			user = User.findById(userId);
 			renderArgs.put("user", user);
 		}
 		if (item.owner.id.equals(userId)) {
 			item.delete();
 		}
 		List<Item> items = Item.findItemsByUser(item.owner);
 		User profile = item.owner;
 		render("application/profile.html", profile, items);
 	}
 
 	public static void addComment(Long itemId, String text) {
 		String error;
 		Long userId = Cache.get(session.getId(), Long.class);
 		if (userId == null) {
 			error = "you need to sign up";
 			render(error);
 		}
 		User user = User.findById(userId);
 		if (user == null) {
 			error = "you need to sign up";
 			render(error);
 		}
 		renderArgs.put("user", user);
 		Item item = Item.findById(itemId);
 		if (item == null) {
 			error = "you need to sign up";
 			render(error);
 		}
 		Comment comment = new Comment(user, text, item).save();
 		render(comment);
 	}
 	
 
 	public static void showCustomers(){
 		Long userId = Cache.get(session.getId(), Long.class);
 		if (userId != null) {
 			renderArgs.put("user", User.findById(userId));
 			User user = User.findById(userId);
 			List<Item> itemList = Item.findItemsByUser(user);
 			render(itemList);
 		}
 		else{
 			index();
 		}
 	}
 	
 	public static void showProductTweets(Long productId){
 		Long userId = Cache.get(session.getId(), Long.class);
 		Item item = Item.findById(productId);
 		if(item != null){
 			if(item.owner.id == userId){
 				List<Tweet> tweetList = item.tweets;
 				renderJSON(TweetJson.toTweetJsonList(tweetList));
 			}
 			else{
 				String error = "You are not allowed to execute this request";
 				render(error);
 			}
 		}
 		else{
 			String error = "This product does not exist.";
 			render(error);
 		}
 	}
 	
 	public static void showReplies(Long tweetid){
 	    renderTemplate("tags/showReplies.html");
 	}
 	
 	public static void replyTweet(Long tweetId, String text){
 		Long userId = Cache.get(session.getId(), Long.class);
 		if(userId != null){
 			User user = User.findById(userId);
 			Tweet tweet = Tweet.findByTwitterId(tweetId);
 			Reply reply = new Reply();
 			reply.source = tweet;
 			reply.tweet = text + " @an4me";
 			reply.save();
 			tweet.save();
 			TwitterProxy proxy = TwitterProxyFactory.newInstance(user);
 			//proxy.reply(reply);
 			renderJSON(new ReplyJson(reply));
 		}
 		else{
 			
 		}		
 		
 	}
 	
 	public static void deleteComment(Long commentId){
 	    Comment comment = Comment.findById(commentId);
 	    if(comment!=null){
 	        Item product = comment.item;
 	        User user = Auth.getCurrentUser();
 	        if(comment.owner == user){
 	            comment.delete();
 	            product.refresh();
 	            renderTemplate("Application/showItem.html",product,user);
 	        }else{
 	            //TODO:you are not authorized
 	            renderTemplate("Application/showItem.html",product,user);
 	        } 
 	    }
 	    index();
 	}
 }
