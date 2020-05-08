 package com.chaman.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.chaman.dao.Dao;
 import com.google.appengine.api.memcache.MemcacheService;
 import com.google.appengine.api.memcache.MemcacheServiceFactory;
 import com.googlecode.objectify.Query;
 import com.restfb.DefaultFacebookClient;
 import com.restfb.Facebook;
 import com.restfb.FacebookClient;
 import com.restfb.exception.FacebookException;
 
 public class Friend extends Model {
 
 	@Facebook
 	long uid;
 	@Facebook
 	long uid2;
 	@Facebook
 	String first_name;
 	@Facebook
 	String last_name;
 	@Facebook
 	String pic;
 	
 	String picture;
 	
 	Boolean isFollowed;
 	
 	public Friend() {
 		super();
 	}
 	
 	public static ArrayList<Model> Get(String accessToken, String userID) throws FacebookException {
 		
 		ArrayList<Model> result = new ArrayList<Model>();
 		
 		FacebookClient client 	= new DefaultFacebookClient(accessToken);
 		String properties 		= "uid, first_name, last_name, pic";
 		String query 			= "SELECT " + properties + " FROM user WHERE uid IN (SELECT uid2 FROM friend WHERE uid1 = " + userID + ") ORDER BY last_name";
 		List<Friend> friends 	= client.executeQuery(query, Friend.class);
 		
 		for (Friend f : friends) {
 			
 			f.picture = f.pic;
 			f.setIsFollowed(userID);
 			
 			result.add(f);
 		}
  	
 		return result;
 	}
 	
 	private void setIsFollowed(String userID) {
 
 		/*Query<Following> q = dao.ofy().query(Following.class);
     	q.filter("userID", Long.parseLong(userID));
 		q.filter("friendID", this.uid);
 		
 		this.isFollowed = (q.iterator() != null && q.iterator().hasNext() && q.iterator().next() != null); // Flo needs to explain this one!*/
     	
 		Following fcache;
 	
 	    MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
 	    fcache = (Following) syncCache.get(userID + String.valueOf(this.uid)); // read from Following cache
 	    if (fcache == null) {	
 	    	
 	    	Dao dao = new Dao();
 	    	Query<Following> q = dao.ofy().query(Following.class);
	    	q.filter("id", userID + String.valueOf(this.uid)); // TODO: fetch for one value
 	    	this.isFollowed = q.count() != 0 ?  true : false;
 	    } else {
 	    	
 	    	this.isFollowed = true;
 	    }
 	    
 	}
 	
 	public long getUid() {
 		
 		return this.uid;
 	}
 	
 	public long getUid2() {
 		
 		return this.uid2;
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
 	
 	public Boolean getIsFollowed() {
 		
 		return this.isFollowed;
 	}
 }
