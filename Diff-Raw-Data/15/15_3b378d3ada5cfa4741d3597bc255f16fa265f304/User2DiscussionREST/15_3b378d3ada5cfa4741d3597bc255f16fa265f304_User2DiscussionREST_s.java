 package controllers;
 
 import static play.libs.Json.toJson;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import models.Discussion;
 import models.Message;
 import models.User;
 import models.User2Discussion;
 
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.node.ObjectNode;
 
 import play.libs.Json;
 import play.mvc.Controller;
 import play.mvc.Result;
 
 public class User2DiscussionREST extends Controller {
 
 	public static Result getDiscussions() {
 		final User user = Application.getLocalUser(session());
 		if (user == null){
 			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
 		}
 		User2Discussion user2disc = MorphiaObject.datastore.get(User2Discussion.class, user.id.toString());
 		List<Discussion> discussions = user2disc.all();
 		if (discussions.size() == 0) {
 			return badRequest(Constants.DISCUSSIONS_EMPTY.toString());
 		} else {
 			
 			ObjectNode response = Json.newObject();
 			response.put("discussions", Json.toJson(discussions));
 			
 			return ok(response);
 		}
 	}
 	
 	public static Result getDiscussion(String discussionId) {
 		final User user = Application.getLocalUser(session());
 		if (user == null){
 			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
 		}
 		User2Discussion user2disc = MorphiaObject.datastore.get(User2Discussion.class, user.id.toString());
 		Discussion discussion = user2disc.findDiscussionById(discussionId);
 		if (discussion == null) {
 			return badRequest(Constants.DISCUSSIONS_EMPTY.toString());
 		} 
 		ObjectNode discussionNode = Json.newObject();
 		discussionNode.put("id", discussion.id.toString());
 		discussionNode.put("subject", discussion.subject);
 		discussionNode.put("messages", Json.toJson(discussion.getMessages()));
 
 		ObjectNode response = Json.newObject();
 		response.put("discussion", Json.toJson(discussionNode));
 
 		return ok(response);
 	}
 	
 	public static Result getMessage(String discussionId, String messageId) {
 		final User user = Application.getLocalUser(session());
 		if (user == null){
 			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
 		}
 		User2Discussion user2disc = MorphiaObject.datastore.get(User2Discussion.class, user.id.toString());
 		Discussion discussion = user2disc.findDiscussionById(discussionId);
 		if (discussion == null) {
 			return badRequest(Constants.DISCUSSIONS_EMPTY.toString());
 		}
 		Message message = discussion.findMessageById(messageId);
 		if (message == null){
 			return badRequest(Constants.MESSAGES_EMPTY.toString());
 		}
 		return ok(Json.toJson(message));
 	}
 	
 	public static Result addDiscussion() {
 		final User user = Application.getLocalUser(session());
 		if (user == null){
 			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
 		}
 		JsonNode json = request().body().asJson();
 		if(json == null) {
 			return badRequest(Constants.JSON_EMPTY.toString());
 		}
 		Discussion discussion = new Discussion();		// Create discussion
 		discussion.messages = new ArrayList<Message>();
 		discussion.subject = json.findPath("subject").getTextValue();
 		
 		Message message = new Message();		// Create message
 		message.body = json.findPath("body").getTextValue();
 		message.writerId = user.id.toString();
 		message.save(); // Save Message
 		
 		discussion.addMessage(message); // Add message to discussion
 		discussion.save(); // Save discussion
 		
 		// Add discussion to all readers
 		Iterator<JsonNode> userIds = json.findPath("receiver_users").getElements();
 		while(userIds.hasNext()){
 			String userId = userIds.next().toString();
 			User2Discussion user2disc = MorphiaObject.datastore.get(User2Discussion.class, userId);
 			// If is users first discussion, create new User2Discussion
 			if (user2disc == null){
 				user2disc = new User2Discussion();
 				user2disc.userId = userId;
 				user2disc.discussionIds = new ArrayList<String>();
 			}
 			user2disc.discussionIds.add(discussion.id.toString());
 			user2disc.save();
 		}
 		
 		// Add discussion to sender
 		User2Discussion user2disc = MorphiaObject.datastore.get(User2Discussion.class, user.id.toString());
 		// If is users first discussion, create new User2Discussion
 		if (user2disc == null){
 			user2disc = new User2Discussion();
 			user2disc.userId = user.id.toString();
 			user2disc.discussionIds = new ArrayList<String>();
 		}
 		user2disc.discussionIds.add(discussion.id.toString());
 		user2disc.save();
 		
 		return ok("New discussion " + discussion.id + " created");
 	}
 	
 	public static Result reply(String id) {
 		final User user = Application.getLocalUser(session());
 		if (user == null){
 			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
 		}
 		JsonNode json = request().body().asJson();
 		if(json == null) {
 			return badRequest(Constants.JSON_EMPTY.toString());
 		}
 		Discussion discussion = Discussion.findById(id);
 		if (discussion == null){
 			return badRequest(Constants.DISCUSSIONS_EMPTY.toString());
 		}
 		Message message = new Message();
 		message.body = json.findPath("body").getTextValue();
 		message.writerId = user.id.toString();
 		message.save();
 		
 		discussion.addMessage(message);
 		discussion.save(); // Save discussion
 		
 		return ok("New message " + message.id + " created");
 	}
 	
 	public static Result replyToMessage(String id, String msgId){
 		final User user = Application.getLocalUser(session());
 		if (user == null){
 			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
 		}
 		JsonNode json = request().body().asJson();
 		if(json == null) {
 			return badRequest(Constants.JSON_EMPTY.toString());
 		}
 		Discussion discussion = Discussion.findById(id);
 		if (discussion == null){
 			return badRequest(Constants.DISCUSSIONS_EMPTY.toString());
 		}
 		Message msg = Message.findById(msgId);
 		if(discussion.messages.contains(msg)){
 			Message message = new Message();
 			message.body = json.findPath("body").getTextValue();
 			message.replyToMsg = msg.id.toString();
 			message.writerId = user.id.toString();
 			message.save();
 			discussion.addMessage(message);
 			discussion.save();
 			return ok(toJson(discussion));
 		}
 		return badRequest(Constants.MESSAGES_EMPTY.toString());
 	}
 	
 }
