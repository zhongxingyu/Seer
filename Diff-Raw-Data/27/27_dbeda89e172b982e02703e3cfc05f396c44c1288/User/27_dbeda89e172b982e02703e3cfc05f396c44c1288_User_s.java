 package com.chaman.model;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.Id;
 
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 
 import com.chaman.dao.Dao;
 import com.googlecode.objectify.ObjectifyService;
 import com.googlecode.objectify.Query;
 import com.googlecode.objectify.annotation.Entity;
 import com.googlecode.objectify.annotation.Unindexed;
 import com.googlecode.objectify.annotation.NotSaved;
 import com.restfb.DefaultFacebookClient;
 import com.restfb.Facebook;
 import com.restfb.FacebookClient;
 import com.restfb.json.JsonObject;
 import com.google.appengine.api.memcache.MemcacheService;
 import com.google.appengine.api.memcache.MemcacheServiceFactory;
 
 @Entity
 @Unindexed
 public class User extends Model implements Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 4211657780516168572L;
 	
 	@Id
 	@Facebook
 	long uid;
 	@Facebook
 	String first_name;
 	@Facebook
 	String last_name;
 	@NotSaved
 	@Facebook
 	String pic;
 	@Facebook
 	String email;
 	@NotSaved
 	@Facebook
 	String birthday_date;
 	@NotSaved
 	@Facebook
 	String sex;
 	@NotSaved
 	@Facebook
 	String relationship_status;
 	
 	@NotSaved
 	String picture;
 	String access_token;
 
 	@NotSaved
 	int nb_of_events;
 	int nb_of_followers;
 	int nb_of_following;
	@NotSaved
	float searchRadius;
 	
 	public User() {
 		
 		super();
 	}
 	
 	static {
 		try {
 			ObjectifyService.register(User.class);
 		} catch (Exception ex) {
 			System.out.println(ex.getMessage());
 		}
 	}
 	
 	public static User Get(long userID) {
 		
 		return new User();
 	}
 	
 	public static ArrayList<Model> Get(String accessToken, String userID, String appuser) {
 		
 		ArrayList<Model> result = new ArrayList<Model>();
 		
 		FacebookClient client 	= new DefaultFacebookClient(accessToken);
		String properties 		= "uid, first_name, last_name, pic, email, sex, birthday_date, relationship_status";
 		String userQuery 		= "SELECT " + properties + " FROM user WHERE uid  = " + userID;
 		
 		List<User> users 		= client.executeQuery(userQuery, User.class);
 		
 		String eventQuery = "SELECT eid from event_member where uid = ";
 		
 		Dao dao = new Dao();
 		
 		User ucache;
 
 	    MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
 		
 		if (users != null) {
 			
 			User u = (User) users.get(0);
 			
 			u.picture = u.pic;
 			
 			//Prepare a timestamp to filter the facebook DB on the upcoming events
 			DateTimeZone PST = DateTimeZone.forID("America/Los_Angeles");
 			DateTime now = new DateTime(PST);
 			now.plusMinutes(PST.getOffset(now));
 			String TAS = String.valueOf(now.getMillis() / 1000);
 			
 			List<JsonObject> event_member = client.executeQuery(eventQuery + u.uid + " AND start_time > " + TAS, JsonObject.class);
 			
 			u.nb_of_events = event_member.size();
 			
     	    ucache = (User) syncCache.get(u.uid); // read from User cache
     	    if (ucache == null) {
 
     	    	Query<Following> qfollowings = dao.ofy().query(Following.class);
     	    	qfollowings.filter("userID", u.uid);
     	    	u.nb_of_following = qfollowings.count();
         	
     	    	Query<Following> qfollowers = dao.ofy().query(Following.class);
     	    	qfollowers.filter("friendID", u.uid);
     	    	u.nb_of_followers = qfollowers.count();
     	    	u.access_token = accessToken;
    	    	if (appuser == "yes") {dao.ofy().put(u);} //add the user to the data store
     	    	syncCache.put(u.uid, u, null); // populate User cache
     	    	
     	    } else {
     	    	
     	    	u.nb_of_following = ucache.nb_of_following;
     	    	u.nb_of_followers = ucache.nb_of_followers;
     	    }
         	
 			result.add(u);
 		}
  	
 		return result;
 	}
 	
 	public static ArrayList<Model> GetFacebookUserInfo(String accessToken, String userID) {
 		
 		ArrayList<Model> result = new ArrayList<Model>();
 		
 		FacebookClient client 	= new DefaultFacebookClient(accessToken);
 		String properties 		= "uid, first_name, last_name, pic";
 		String userQuery 		= "SELECT " + properties + " FROM user WHERE uid  = " + userID;
 		
 		List<User> users 		= client.executeQuery(userQuery, User.class);
 		
 		if (users != null) {
 			
 			User u = users.get(0);
 			
 			u.picture = u.pic;
 	
 			result.add(u);
 		}
  	
 		return result;
 	}
 	
 	public static ArrayList<Model> GetMultiples(String accessToken, ArrayList<Model> dbUsers, MemcacheService syncCache) {
 		
 		ArrayList<Model> result = new ArrayList<Model>();
 		User ucache = null;
 		
 		if (dbUsers.size() == 0) {
 			
 			return result;
 		}
 		
 		String strUids = "";
 		
 		for (int i = 0; i < dbUsers.size(); i++) {
 			
 			strUids += "uid = " + ((User)dbUsers.get(i)).uid;
 			
 			if (i < dbUsers.size() - 1) {
 				
 				strUids += " OR ";
 			}
 		}
 		
 		FacebookClient client 	= new DefaultFacebookClient(accessToken);
 		String properties 		= "uid, first_name, last_name, pic";
 		String query 			= "SELECT " + properties + " FROM user WHERE " + strUids + " ORDER BY last_name";
 		List<User> users 		= client.executeQuery(query, User.class);
 		
 		//Prepare a timestamp to filter the facebook DB on the upcoming events
 		DateTimeZone PST = DateTimeZone.forID("America/Los_Angeles");
 		DateTime now = new DateTime(PST);
 		now.plusMinutes(PST.getOffset(now));
 		String TAS = String.valueOf(now.getMillis() / 1000);
 		
 		String eventQuery = "SELECT eid from event_member where uid = ";
 		
 		for (User u : users) {
 		
 			u.picture = u.pic;
 			
 			ucache = (User) syncCache.get(u.uid);
 			
 			if (ucache == null) {
 				
 				List<JsonObject> event_member = client.executeQuery(eventQuery + u.uid + " AND start_time > " + TAS, JsonObject.class);
 
 				u.nb_of_events = event_member.size();
 						
 				User dbu = User.getUserByUID(u.uid, dbUsers);
 				
 				if (dbu != null) {
 					
 					u.nb_of_followers = dbu.nb_of_followers;
 					u.nb_of_following = dbu.nb_of_following;
 					syncCache.put(u.uid, u, null); // populate User cache
 				}
 			} else {
 				
 				u.nb_of_events = ucache.nb_of_events;
 				u.nb_of_followers = ucache.nb_of_followers;
 				u.nb_of_following = ucache.nb_of_following;
 			}
 			result.add(u);
 		}
  	
 		return result;
 	}
 	
 	public static User getUserByUID(long uid, ArrayList<Model> users) {
 		
 		User result = null;
 		
 		for (Model m : users) {
 			
 			User u = (User)m;
 			
 			if (u.uid == uid) {
 				
 				result = u;
 				break;
 			}
 		}
 		
 		return result;
 	}
 	
 	public long getUid() {
 		
 		return this.uid;
 	}
 	
 	public String getFirst_name() {
 		
 		return this.first_name;
 	}
 	
 	public String getLast_name() {
 		
 		return this.last_name;
 	}
 	
 	public String getPicture() {
 		
 		return this.picture;
 	}
 	
 	public String getEmail() {
 		
 		return this.email;
 	}
 	
 	public String getBirthday_date() {
 		
 		return this.birthday_date;
 	}
 	
 	public String getSex() {
 		
 		return this.sex;
 	}
 	
 	public String getRelationship_status() {
 		
 		return this.relationship_status;
 	}
 	
	public float getSearchRadius() {
		
		return this.searchRadius;
	}
	
 	public int getNb_of_events() {
 		
 		return this.nb_of_events;
 	}
 	
 	public int getNb_of_followers() {
 		
 		return this.nb_of_followers;
 	}
 
 	public int getNb_of_following() {
 	
 		return this.nb_of_following;
 	}
 	
 	public void setNb_of_events(int nb_of_events) {
 		
 		this.nb_of_events = nb_of_events;
 	}
 	
 	public void setNb_of_followers(int nb_of_followers) {
 		
 		this.nb_of_followers = nb_of_followers;
 	}
 
 	public void setNb_of_following(int nb_of_following) {
 	
 		this.nb_of_following = nb_of_following;
 	}
 
 	public String getAccess_token() {
 		return access_token;
 	}
 
 	public void setAccess_token(String access_token) {
 		this.access_token = access_token;
 	}
 }
