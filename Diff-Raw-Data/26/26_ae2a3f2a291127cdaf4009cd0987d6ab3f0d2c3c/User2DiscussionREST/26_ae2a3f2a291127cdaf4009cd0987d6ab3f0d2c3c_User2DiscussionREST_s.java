 package controllers;
 
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
 		if (user2disc == null) {
 			return badRequest(Constants.DISCUSSIONS_EMPTY.toString());
 		}
 		List<Discussion> discussions = user2disc.all();
 		if (discussions.size() == 0) {
 			return badRequest(Constants.DISCUSSIONS_EMPTY.toString());
 		} else {
			ObjectNode response = Json.newObject();
			response.put("discussions", Json.toJson(Discussion.discussionsToObjectNodes(discussions)));
 			
 			// Return the response
			return ok(response);
 		}
 	}
 	
 	
 	public static Result getUnreadDiscussions(){
 		final User user = Application.getLocalUser(session());
 		if (user == null){
 			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
 		}
 		User2Discussion user2disc = MorphiaObject.datastore.get(User2Discussion.class, user.id.toString());
 		if (user2disc == null) {
 			return badRequest(Constants.DISCUSSIONS_EMPTY.toString());
 		}
 		List<Discussion> discussions = user2disc.unread();
 		if (discussions.size() == 0) {
 			return badRequest(Constants.DISCUSSIONS_EMPTY.toString());
 		} else {
 			ObjectNode response = Json.newObject();
 			response.put("discussions", Json.toJson(Discussion.discussionsToObjectNodes(discussions)));
 			
 			// Return the response
 			return ok(response);
 		}
 	}
 
 	
 	public static Result getDiscussion(String discussionId) {
 		final User user = Application.getLocalUser(session());
 		if (user == null){
 			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
 		}
 		User2Discussion user2disc = MorphiaObject.datastore.get(User2Discussion.class, user.id.toString());
 		if (user2disc == null) {
 			return badRequest(Constants.DISCUSSIONS_EMPTY.toString());
 		}
 		Discussion discussion = user2disc.findDiscussionById(discussionId);
 		if (discussion == null) {
 			return badRequest(Constants.DISCUSSIONS_EMPTY.toString());
 		}
 
 		user2disc.setReadTimeStamp(discussion.id.toString());
 		
 		ObjectNode response = Json.newObject();
 		response.put("discussion", Json.toJson(Discussion.discussionToObjectNode(discussion)));
 
 		// Return the response
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
 		
 		// Return a copy of the message
 		return ok(Json.toJson(Message.messageToObjectNode(message)));
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
 		discussion.messageIds = new ArrayList<String>();
 		discussion.subject = json.findPath("subject").getTextValue();
 
 		Message message = new Message();		// Create message
 		message.message = json.findPath("message").getTextValue();
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
 
 		//Return a copy of the discussion
 		return ok(Json.toJson(Discussion.discussionToObjectNode(discussion)));
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
 		message.message = json.findPath("message").getTextValue();
 		message.writerId = user.id.toString();
 		message.save();
 		
 		discussion.addMessage(message);
 		discussion.save(); // Save discussion
 		
 		// Return a copy of the discussion
 		return ok(Json.toJson(Discussion.discussionToObjectNode(discussion)));
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
 		
 		// If the message replying to its really from this discussion
 		Message msg = discussion.findMessageById(msgId);
 		if(msg != null){
 			Message message = new Message();
 			message.message = json.findPath("message").getTextValue();
 			message.replyToMsg = msg.id.toString();
 			message.writerId = user.id.toString();
 			message.save(); // Save Message
 			
 			discussion.addMessage(message); // Add message to its discussion
 			discussion.save(); // Save Discussion
 			
 			// Return a copy of the discussion
 			return ok(Discussion.discussionToObjectNode(discussion));
 		}
 		return badRequest(Constants.MESSAGES_EMPTY.toString());
 	}
 }
