 package models;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.bson.types.ObjectId;
 import org.codehaus.jackson.node.ObjectNode;
 
 import play.Logger;
 import play.libs.Json;
 
 import com.feth.play.module.pa.user.AuthUser;
 import com.google.code.morphia.annotations.Embedded;
 import com.google.code.morphia.annotations.Entity;
 import com.google.code.morphia.annotations.Id;
 
 import controllers.MorphiaObject;
 
 /**
  * User: a.pijoan
  * Date: 16/03/13
  * Time: 12:34
  */
 @Entity
 public class Discussion {
 
 	@Id
 	public ObjectId id;
 
 	public String subject;
 
	public List<String> messageIds;
 
 	public Date timeStamp;
 	
 	public static List<Discussion> all() {
 		if (MorphiaObject.datastore != null) {
 			return MorphiaObject.datastore.find(Discussion.class).asList();
 		} else {
 			return new ArrayList<Discussion>();
 		}
 	}
 	
 	public ObjectId save() {
 		timeStamp = new Date();
 		MorphiaObject.datastore.save(this);
 		return this.id;
 	}
 	
 	public static Discussion findById(String id) {
 		Discussion discussion = MorphiaObject.datastore.get(Discussion.class, new ObjectId(id));
 		if (discussion == null){
 			return null;
 		} else {
 			return discussion;
 		}
 	}
 	
 	public static Discussion findById(ObjectId id) {
 		Discussion discussion = MorphiaObject.datastore.get(Discussion.class, id);
 		if (discussion == null){
 			return null;
 		} else {
 			return discussion;
 		}
 	}
 	
 	public Message findMessageById(String id) {
 		if (this.messageIds.contains(id)){
 			Message message = MorphiaObject.datastore.get(Message.class, new ObjectId(id));
 			return message;
 		} else {
 			return null;
 		}
 	}
 	
 	public Message findMessageById(ObjectId id) {
 		if (this.messageIds.contains(id.toString())){
 			Message message = MorphiaObject.datastore.get(Message.class, id);
 			return message;
 		} else {
 			return null;
 		}
 	}
 	
 	public List<Message> getMessages() {
 		List<Message> messages = new ArrayList<Message>();
 		for(String id : this.messageIds){
 			Message message = MorphiaObject.datastore.get(Message.class, new ObjectId(id));
 			messages.add(message);
 		}
 		return messages;
 	}
 	
 	public void addMessage(Message message) {
 		this.messageIds.add(message.id.toString());
 		this.save();
 	}
 	
 	/** Parses a discussion list and prepares it for exporting to JSON
 	 * @param dscs Discussion list
 	 * @return List of ObjectNodes ready for use in toJson
 	 */
 	public static List<ObjectNode> discussionsToObjectNodes (List<Discussion> dscs){
 		List<ObjectNode> discussions = new ArrayList<ObjectNode>();
 			for(Discussion discussion : dscs){
 				discussions.add(discussionToObjectNode(discussion));
 			}
 			return discussions;
 		}
 	
 	/** Parses a discussion and prepares it for exporting to JSON
 	 * @param discussion A discussion
 	 * @return ObjectNode ready for use in toJson
 	 */
 	public static ObjectNode discussionToObjectNode (Discussion discussion){
 		ObjectNode discussionNode = Json.newObject();
 		discussionNode.put("id", discussion.id.toString());
 		discussionNode.put("subject", discussion.subject);
 		discussionNode.put("timeStamp", discussion.id.getTime());
 		discussionNode.put("messages", Json.toJson(Message.messagesToObjectNodes(discussion.getMessages())));
 		return discussionNode;
 	}
 }
