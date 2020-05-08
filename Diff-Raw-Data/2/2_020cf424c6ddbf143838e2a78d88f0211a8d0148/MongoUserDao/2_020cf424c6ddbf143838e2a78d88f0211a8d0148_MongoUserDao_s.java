 package edu.wm.werewolf.dao;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import edu.wm.werewolf.domain.Player;
 import edu.wm.werewolf.domain.User;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.mongodb.core.MongoOperations;
 import org.springframework.data.mongodb.core.mapping.Document;
 import org.springframework.data.mongodb.core.query.Criteria;
 import org.springframework.data.mongodb.core.query.Query;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 
 //@Document(collection = "users")
 public class MongoUserDao implements IUserDAO{
 	
 	private static final Logger logger = LoggerFactory.getLogger(MongoUserDao.class);
 	
 	@Autowired DB db;
 		
 	@Override
 	public void createUser(User user) 
 	{
 		DBCollection table = db.getCollection("user");
 		table.drop();
 		
 //		DBCollection table = db.getCollection("user");
 //		BasicDBObject documentDetail = new BasicDBObject();
 //		documentDetail.put("firstName", user.getFirstName());
 //		documentDetail.put("lastName", user.getLastName());
 //		documentDetail.put("password", user.getHashedPassword());
 //		documentDetail.put("id", user.getId());
 //		documentDetail.put("score", user.getScore());
 //		documentDetail.put("image", user.getImageURL());
 //		documentDetail.put("username", user.getUsername());
 //		documentDetail.put("isAdmin", user.isAdmin());
 //		table.insert(documentDetail);
 	}
 
 	@Override
 	public User getUserbyID (String id) {
 		DBCollection table = db.getCollection("user");
 		BasicDBObject query = new BasicDBObject("id", id);
 		DBCursor cursor = table.find(query);
 		User user = null;
 		while (cursor.hasNext())
 		{
 			DBObject userObject = cursor.next();
 			user = new User((String)userObject.get("id"), (String)userObject.get("firstName"),
 					(String)userObject.get("lastName"), (String)userObject.get("username"), (String)userObject.get("password"),
 					(String)userObject.get("image"), (boolean)userObject.get("isAdmin"));
 		
 		}
 		return user;
 	}
 	
 	@Override
 	public User getUserByName (String name) {
 		DBCollection table = db.getCollection("user");
 		BasicDBObject query = new BasicDBObject("firstName", name);
 		DBCursor cursor = table.find(query);
 		User user = null;
 		while (cursor.hasNext())
 		{
 			DBObject userObject = cursor.next();
 			user = new User((String)userObject.get("id"), (String)userObject.get("firstName"),
 				(String)userObject.get("lastName"), (String)userObject.get("username"), (String)userObject.get("password"),
 				(String)userObject.get("image"), (boolean)userObject.get("isAdmin"));
 	
 		}
 		return user;
 	}
 
 	@Override
 	public List<User> getAllUsers() {
 		DBCollection table = db.getCollection("user");
 		DBCursor cursor = table.find();
 		List <User> users = new ArrayList<>();
 		while (cursor.hasNext())
 		{
 			DBObject userObject = cursor.next();
 			User user = new User((String)userObject.get("id"), (String)userObject.get("firstName"),
 					(String)userObject.get("lastName"), (String)userObject.get("username"), (String)userObject.get("password"),
 					(String)userObject.get("image"), (boolean)userObject.get("isAdmin"));
 		
 			
 			users.add(user);
 		}
 		return users;
 	}
 }
