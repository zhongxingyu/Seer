 package edu.wm.werewolf.dao;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.authority.GrantedAuthorityImpl;
 
 import com.mongodb.BasicDBList;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.MongoClient;
 
 import edu.wm.werewolf.exceptions.NoPlayerFoundException;
 import edu.wm.werewolf.model.Player;
 import edu.wm.werewolf.model.WerewolfUser;
 
 public class MongoUserDAO implements IUserDAO {
 //	@Autowired private MongoClient mongo;
 	@Autowired DB db;
 	@Override
 	public void createUser(WerewolfUser user) {
 		// TODO Auto-generated method stub
 //		//		db = mongo.getDB("werewolf");
 		DBCollection table = db.getCollection("User");
 		BasicDBObject document = new BasicDBObject();
 		document.put("_id", user.getId());
 		document.put("password", user.getHashedPassword());
 		document.put("first", user.getFirstName());
 		document.put("last", user.getLastName());
 		document.put("username", user.getUsername());
 		document.put("img", user.getImageURL());
 		document.put("isAdmin", user.isAdmin());
 		table.insert(document);
 	}
 	
 	public WerewolfUser getUserByUsername(String username)
 	{
 		//		db = mongo.getDB("werewolf");
 		DBCollection table = db.getCollection("User");
 		BasicDBObject query = new BasicDBObject("username", username);
 		System.out.println(username);
		System.out.println(table.find().toString());
 		DBObject cursor = table.findOne(query);
 		Collection<GrantedAuthorityImpl> auth = new ArrayList<GrantedAuthorityImpl>();
 		auth.add(new GrantedAuthorityImpl("ROLE_USER"));
 		//TODO: Fix to allow users to be set as admins
 		if(cursor == null) {
 			System.out.println("Shoot");
 			return null;}
 		WerewolfUser user = new WerewolfUser((String)cursor.get("_id"), (String)cursor.get("first"), (String)cursor.get("lastName"), username, (String)cursor.get("password"), (String)cursor.get("imageURL"));
 		if((boolean)cursor.get("isAdmin")) {
 			user.setAdmin(true);
 		}
 		return user;
 	}
 
 	@Override
 	public List<WerewolfUser> getAllUsers() {
 		//		db = mongo.getDB("werewolf");
 		DBCollection table = db.getCollection("User");
 		DBCursor cursor = table.find();
 		WerewolfUser alive;
 		List<WerewolfUser> players = new ArrayList<>();
 		Collection<GrantedAuthorityImpl> auth = new ArrayList<GrantedAuthorityImpl>();
 		auth.add(new GrantedAuthorityImpl("ROLE_USER"));
 		// TODO: Fix to allow users to be set as admins
 		try {
 		while (cursor.hasNext()) {
 			DBObject item = cursor.next();
 			alive = new WerewolfUser((String)item.get("_id"), (String)item.get("first"), (String)item.get("lastName"), (String)item.get("username"), (String)item.get("password"), (String)item.get("imageURL"));
 			players.add(alive);
 		}
 		}
 		finally {
 			cursor.close();
 		}
 		return players;
 	}
 
 	@Override
 	public void update(WerewolfUser user) {
 		// TODO Auto-generated method stub
 		DBCollection table = db.getCollection("User");
 		BasicDBObject document = new BasicDBObject();
 		document.put("_id", user.getId());
 		document.put("password", user.getHashedPassword());
 		document.put("first", user.getFirstName());
 		document.put("last", user.getLastName());
 		document.put("username", user.getUsername());
 		document.put("img", user.getImageURL());
 		document.put("isAdmin", user.isAdmin());
 		table.save(document);
 	}
 
 }
