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
 import play.mvc.Result;
 
 public class User2DiscussionREST extends ItemREST {
 
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
 			// Return the response
 			return ok(Json.toJson(Discussion.discussionsToObjectNodes(discussions)));
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
 			// Return the response
 			return ok(Json.toJson(Discussion.discussionsToObjectNodes(discussions)));
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
 
 		user2disc.setRead(discussion.id.toString(), true);
 
 		// Return the response
 		return ok(Json.toJson(Discussion.discussionToFullObjectNode(discussion)));
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
 		
 		user2disc.setRead(discussion.id.toString(), true);
 		
 		// Return a copy of the message
 		return ok(Json.toJson(Message.messageToObjectNode(message)));
 	}
 	
 	public static Result addDiscussion() {
		System.out.println("Add discussion");

 		final User user = Application.getLocalUser(session());

 		if (user == null){
 			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
 		}
 		JsonNode json = request().body().asJson();
 		if(json == null) {
			System.out.println("JSON EMpty");
 			return badRequest(Constants.JSON_EMPTY.toString());
 		}
 		
 		Discussion discussion = new Discussion();		// Create discussion
 		discussion.messageIds = new ArrayList<String>();
 		discussion.userIds = new ArrayList<String>();
 		discussion.subject = json.findPath("subject").getTextValue();
 
 		Message message = new Message();		// Create message
 		message.message = json.findPath("message").getTextValue();
 		message.writerId = user.id.toString();
 		message.save(); // Save Message
 		
 		discussion.addMessage(message); // Add message to discussion
 		discussion.save(); // Save discussion
 		
 		// Add discussion to all readers
		Iterator<JsonNode> userIds = json.findPath("users").getElements();
 		while(userIds.hasNext()){
 			String userId = userIds.next().toString();
 			User2Discussion user2disc = MorphiaObject.datastore.get(User2Discussion.class, userId);
 			// If is users first discussion, create new User2Discussion
 			if (user2disc == null){
 				user2disc = new User2Discussion();
 				user2disc.userId = userId;
 				discussion.userIds.add(userId);
 				user2disc.discussionIds = new ArrayList<String>();
 				user2disc.unread = new ArrayList<String>();
 				user2disc.save();
 			}
 			
 			user2disc.addDiscussion(discussion.id.toString()); // Add discussions id to this user
 			user2disc.save(); // Save user2discussion
 		}
 		
 		// Add discussion to sender
 		User2Discussion user2disc = MorphiaObject.datastore.get(User2Discussion.class, user.id.toString());
 		// If is users first discussion, create new User2Discussion
 		if (user2disc == null){
 			user2disc = new User2Discussion();
 			user2disc.userId = user.id.toString();
 			discussion.userIds.add(user.id.toString());
 			user2disc.discussionIds = new ArrayList<String>();
 			user2disc.unread = new ArrayList<String>();
 		}
 		
 		user2disc.addDiscussion(discussion.id.toString());
 		user2disc.save();
 
 		//Return a copy of the discussion
 		return ok(Json.toJson(Discussion.discussionToFullObjectNode(discussion)));
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
 		if (discussion == null || !discussion.userIds.contains(user.id.toString())){
 			return badRequest(Constants.DISCUSSIONS_EMPTY.toString());
 		}
 		
 		Message message = new Message();
 		message.message = json.findPath("message").getTextValue();
 		message.writerId = user.id.toString();
 		message.save();
 		
 		discussion.addMessage(message);
 		discussion.save(); // Save discussion
 		
 		// Create a list of all the users except me
 		List <String> otherUsers = discussion.userIds;
 		otherUsers.remove(user.id.toString());
 		
 		setUsersDiscussionUnread(discussion.id.toString(), otherUsers);
 
 		
 		// Return a copy of the discussion
 		return ok(Json.toJson(Discussion.discussionToFullObjectNode(discussion)));
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
 		if (discussion == null || !discussion.userIds.contains(user.id.toString())){
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
 			
 			// Create a list of all the users except me
 			List <String> otherUsers = discussion.userIds;
 			otherUsers.remove(user.id.toString());
 			
 			setUsersDiscussionUnread(discussion.id.toString(), otherUsers);
 			
 			// Return a copy of the discussion
 			return ok(Discussion.discussionToFullObjectNode(discussion));
 		}
 		return badRequest(Constants.MESSAGES_EMPTY.toString());
 	}
 	
 	/** Change discussion to unread for this users
 	 * @param discussionId
 	 * @param userIds
 	 */
 	public static void setUsersDiscussionUnread(String discussionId, List<String> userIds){
 		
 		for (String userId : userIds){
 			User2Discussion user2disc = MorphiaObject.datastore.get(User2Discussion.class, userId.toString());
 			
 			// If is users first discussion, create new User2Discussion
 			if (user2disc == null){
 				user2disc = new User2Discussion();
 				user2disc.userId = userId.toString();
 				user2disc.discussionIds = new ArrayList<String>();
 				user2disc.unread = new ArrayList<String>();
 			}
 			user2disc.setRead(discussionId, false);
 		}
 	}
 }
