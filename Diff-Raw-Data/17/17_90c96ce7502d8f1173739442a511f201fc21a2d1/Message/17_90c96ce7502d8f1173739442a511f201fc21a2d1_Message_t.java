 package cse405.message;
 
 import java.security.SecureRandom;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.datastore.Query.FilterOperator;
 import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Text;
 
 // The application design is that authenticated users have zero or one messages.
 //
 // This class is not thread safe because it is possible to create 2 messages for
 // a single user under the right conditions.  Also, the static initialization
 // code is not thread safe either.  These problems can be solved using transactions. 
 //
 // All application messages are placed under a single parent entity.
 public class Message {
 	
 	private static final String parentKind = "messageParent";
 	
 	private static final String entityKind = "message";
 	private static final String userIdPropertyName = "userId";
 	private static final String nicknamePropertyName = "nickname";
 	private static final String textPropertyName = "text";
 	
 	private static final String csrfTokenPropertyName = "csrfToken";
 	
 	private static Key parentKey = null;
 	
 	private static SecureRandom secureRandom = new SecureRandom();
 	
 	// Check if message parent exists; if not, then create it.
 	static {
 		Query query = new Query(parentKind);
 		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 		Entity parent = datastore.prepare(query).asSingleEntity();
 		if (parent == null) {
 			parent = new Entity(parentKind);
 			datastore.put(parent);
 		}
 		parentKey = parent.getKey();
 	}
 	
 	private Entity entity = null;
 
 	private Message(Entity entity) {
 		this.entity = entity;
 	}
 	
 	public Long getID() {
 		return (Long) entity.getKey().getId();
 	}
 	
 	public String getText() {
		return ((Text) entity.getProperty(textPropertyName)).getValue();
 	}
 	
 	public String getNickname() {
 		return (String) entity.getProperty(nicknamePropertyName);
 	}
 	
 	private void setNickname(String nickname) {
 		entity.setProperty(nicknamePropertyName, nickname);
 	}
 	
 	private void setText(String text) {
		entity.setProperty(textPropertyName, new Text(text));
 	}
 	
 	public String getCsrfToken() {
 		return (String) entity.getProperty(csrfTokenPropertyName);
 	}
 	
 	private void save() {
 		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 		datastore.put(entity);		
 	}
 	
 	public void delete() {
 		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 		datastore.delete(entity.getKey());	
 	}
 
 	public static Message createOrUpdate(String userId, String nickname, String text) {
 		Message message = getByUserId(userId);
 		if (message != null) {
 			message.setNickname(nickname);
 			message.setText(text);
 		} else {
 			Entity entity = new Entity(entityKind, parentKey);
 			entity.setProperty(userIdPropertyName, userId);
 			entity.setProperty(nicknamePropertyName, nickname);
			entity.setProperty(textPropertyName, new Text(text));
 			entity.setProperty(csrfTokenPropertyName, "" + secureRandom.nextLong());
 			message = new Message(entity);
 		}
 		message.save();
 		return message;
 	}
 
 	public static List<Message> getAll() {
 		Query query = new Query(entityKind, parentKey).addSort(nicknamePropertyName, SortDirection.ASCENDING);
 		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 		Iterator<Entity> entityIter = datastore.prepare(query).asIterator();
 		List<Message> messageList = new LinkedList<Message>();
 		while (entityIter.hasNext()) {
 			messageList.add(new Message(entityIter.next()));
 		}
 		return messageList;
 	}
 	
 	public static Message getByUserId(String userId) {
 		Query query = new Query(entityKind);
 		query.addFilter(userIdPropertyName, FilterOperator.EQUAL, userId);
 		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 		Entity entity = datastore.prepare(query).asSingleEntity();
 		if (entity == null) {
 			return null;
 		} else {
 			return new Message(entity);
 		}
 	}
 }
