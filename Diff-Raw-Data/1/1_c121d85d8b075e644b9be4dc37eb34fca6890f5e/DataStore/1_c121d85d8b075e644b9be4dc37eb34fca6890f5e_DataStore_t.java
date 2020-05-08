 package com.lordMap.datastore;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.jsp.ah.datastoreViewerBody_jsp;
 
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.FetchOptions;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.datastore.Query;
 import com.lordMap.models.Land;
 import com.lordMap.models.User;
 
 public class DataStore {
 
 	private DatastoreService datastore;
 	private final static double RADIUS = 1;
 	private final static double PRECISION = 0.0000001;
 
 	public DataStore() {
 		datastore = DatastoreServiceFactory.getDatastoreService();
 	}
 	
 	//check whether userId exists
 	public boolean checkUserId(String userId) {
 		Key key = KeyFactory.createKey("user", "default");
 		Query query = new Query(userId, key);
 		List<Entity> users = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
 		return !users.isEmpty();
 	}
 	
 	//check whether password is correct
 	public boolean checkUserPwd(String userId, String userPwd) {
 		Key key = KeyFactory.createKey("user", "default");
 		Query query = new Query(userId, key);
 		List<Entity> users = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
 		Entity user = users.get(0);
 		String pwd = (String) user.getProperty("userPwd");
 		return pwd.equals(userPwd);
 	}
 	
 	// store user on google datastore
 	public void storeUser(User newUser) {
 		Key key = KeyFactory.createKey("user", "default");
 		Entity user = new Entity(newUser.getUserId(), key);
 		user.setProperty("userId", newUser.getUserId());
 		user.setProperty("userPwd", newUser.getUserPwd());
 		datastore.put(user);		
 	}
 	
 	public void storeLand(Land newLand) {
 		Key key = KeyFactory.createKey("land", "default");
 		Entity land = new Entity(newLand.getOwner(), key);
 		land.setProperty("owner", newLand.getOwner());
 		//land.setProperty("id", newLand.getId());
 		land.setProperty("lat0", newLand.getLats()[0]);
 		land.setProperty("lat1", newLand.getLats()[1]);
 		land.setProperty("long0", newLand.getLongs()[0]);
 		land.setProperty("long1", newLand.getLongs()[1]);
 		//land.setProperty("price", newLand.getPrice());
 		//land.setProperty("defence", newLand.getDefence());
 		newLand.setId(findAllLands().size());
 		land.setProperty("id", newLand.getId());
 
 		datastore.put(land);
 
 	}
 	
 	//check whether two squares overlap
 	public boolean checkOverlap(double[] lats1, double[] longs1, double[] lats2, double[] longs2) {
 		if (lats1[0] > lats2[1] || lats1[1] < lats2[0] || longs1[0] < longs2[1] || longs1[	1] > longs2[0]) 
 			return false;
 		else
 			return true;
 	}
 	
 	//check whether a land overlap with all other lands buyed
 	public boolean checkOverlaps(double[] lats, double[] longs) {
 		ArrayList<Land> lands = findAllLands();
 		for (Land l : lands) {
 			if (checkOverlap(l.getLats(), l.getLongs(), lats, longs))
 				return true;
 		}
 		return false;
 	}
 	
 	public ArrayList<Land> showLands(String userId) {
 		Key key = KeyFactory.createKey("land", "default");
 		ArrayList<Land> lands = new ArrayList<Land>();
 		Query query = new Query(userId, key);
 		List<Entity> ls = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
 		for (Entity l : ls) {
 			Land nl = new Land();
 			nl.setOwner(userId);
 			//nl.setId((Integer) l.getProperty("id"));
 			//nl.setPrice((Integer) l.getProperty("price"));
 			//nl.setDefence((Integer) l.getProperty("defence"));
 			double[] lats = new double[2];
 			double[] longs = new double[2];
 			lats[0] = (Double) l.getProperty("lat0");
 			lats[1] = (Double) l.getProperty("lat1");
 			longs[0] = (Double) l.getProperty("long0");
 			longs[1] = (Double) l.getProperty("long1");
 			nl.setLats(lats);
 			nl.setLongs(longs);
 			
 			lands.add(nl);
 		}
 		return lands;
 	}
 	
 	public ArrayList<Land> findAllLands() {
 		Key key = KeyFactory.createKey("land", "default");
 		ArrayList<Land> lands = new ArrayList<Land>();
 		Query query = new Query(key);
 		List<Entity> ls = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
 		for (Entity l : ls) {
 			Land nl = new Land();
 			nl.setOwner((String) l.getProperty("owner"));
 			//nl.setId((Integer) l.getProperty("id"));
 			//nl.setPrice((Integer) l.getProperty("price"));
 			//nl.setDefence((Integer) l.getProperty("defence"));
 			double[] lats = new double[2];
 			double[] longs = new double[2];
 			lats[0] = (Double) l.getProperty("lat0");
 			lats[1] = (Double) l.getProperty("lat1");
 			longs[0] = (Double) l.getProperty("long0");
 			longs[1] = (Double) l.getProperty("long1");
 			nl.setLats(lats);
 			nl.setLongs(longs);
 			lands.add(nl);
 		}
 	    return lands;
 	}
 	
 	public ArrayList<Land> findLands(double lat, double lng) {
 //		Key key = KeyFactory.createKey("land", "default");
 //		ArrayList<Land> lands = new ArrayList<Land>();
 //		Query query = new Query(key);
 //		List<Entity> ls = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
 //		// Get the results and process them, casting to the required types 
 //	    for (Entity ent : ls) {
 //	    	double[] lats = new double[2];
 //	    	double[] lngs = new double[2];
 //	    	double[] center = new double[2];
 //	    	center[0] = lat;
 //	    	center[1] = lng;
 //	    	lats[0] = (Double)ent.getProperty("lat0");
 //	    	lngs[0] = (Double)ent.getProperty("long0");
 //	    	lats[1] = (Double)ent.getProperty("lat1");
 //	    	lngs[1] = (Double)ent.getProperty("long1");	    	
 //	    	if (rectAndCircleAreOverlap(lats, lngs, center)) {
 //	    		Land land = new Land();
 //	    		land.setLats(lats);
 //	    		land.setLongs(lngs);
 //	    		land.setOwner((String)ent.getProperty("userId"));
 //	    		land.setId((Integer) ent.getProperty("id"));
 //	    		land.setPrice((Integer) ent.getProperty("price"));
 //	    		land.setDefence((Integer) ent.getProperty("defence"));
 //	    		lands.add(land);
 //	    	}
 //	    	
 //	    }
 		ArrayList<Land> allLands = findAllLands();
 		ArrayList<Land> lands = new ArrayList<Land>();
 		for (Land land: allLands) {
 	    	double[] center = new double[2];
 	    	center[0] = lat;
 	    	center[1] = lng;
 	    	if (rectAndCircleAreOverlap(land.getLats(), land.getLongs(), center)) {
 	    		lands.add(land);
 	    	}
 //	    	lands.add(land);
 		}
 		
 	    return lands;
 	}
 	
 	private void initRect(double[] rectLats, double[] rectLngs, double[] lats, double[] lngs) {
 		rectLats[0] = lats[0];
 		rectLngs[0] = lngs[0];		
 		rectLats[1] = lats[1];
 		rectLngs[1] = lngs[1];
 		rectLats[2] = lats[1];
 		rectLngs[2] = lngs[0];
 		rectLats[3] = lats[0];
 		rectLngs[3] = lngs[1];
 	}
 	
 	private void initCircle(double[] circleLats, double[] circleLngs, double[] center) {
 		circleLats[0] = center[0];
 		circleLngs[0] = center[1] + RADIUS;
 		circleLats[1] = center[0] + RADIUS;
 		circleLngs[1] = center[1];
 		circleLats[2] = center[0];
 		circleLngs[2] = center[1] - RADIUS;
 		circleLats[3] = center[0] - RADIUS;
 		circleLngs[3] = center[1];
 	}
 	
 	private boolean rectAndCircleAreOverlap(double[] lats, double[] lngs, double[] center) {
 		double[] rectLats = new double[4];
 		double[] rectLngs = new double[4];
 		double[] circleLats = new double[4];
 		double[] circleLngs = new double[4];
 		
 		initRect(rectLats, rectLngs, lats, lngs);
 		initCircle(circleLats, circleLngs, center);
 		
 		if (cornerIsInsideCircle(rectLats, rectLngs, center) 
 				|| cornerIsInsideRect(circleLats, circleLngs, rectLats, rectLngs))
 			return true;
 		
 		return false;
 	}
 	
 	private boolean cornerIsInsideCircle(double[] rectLats, double[] rectLngs, double[] center) {
 		for (int i = 0; i < 4; i++) {
 			double[] point = new double[2];
 			point[0] = rectLats[i];
 			point[1] = rectLngs[i];
 			if (pointIsInsideCircle(point, center))
 				return true;
 		}
 		return false;
 	}
 	
 	private boolean cornerIsInsideRect(double[] circleLats, double[] circleLngs, double[] rectLats, double[] rectLngs) {
 		for (int i = 0; i < 4; i++) {
 			double[] point = new double[2];
 			point[0] = circleLats[i];
 			point[1] = circleLngs[i];
 			if (pointIsInsideRect(point, rectLats, rectLngs))
 				return true;
 		}
 		
 		return false;
 	}
 	
 	private boolean pointIsInsideRect(double[] point, double[] rectLats, double[] rectLngs) {
 		if (point[0] - rectLats[0] > PRECISION
 				&& rectLats[1] - point[0] > PRECISION
 				&& point[1] - rectLngs[1] > PRECISION
 				&& rectLngs[0] - point[1] > PRECISION)
 			return true;
 		
 		return false;
 	}
 	
 	private boolean pointIsInsideCircle(double[] point, double[] center) {
 		double dis = (point[0] - center[0]) * (point[0] - center[0]) + (point[1] - center[1]) * (point[1] - center[1]);    
 		if (Math.sqrt(dis) - RADIUS < PRECISION)
 			return true;
 		
 		return false;
 	}
 	
 	//check whether request before
 	public boolean checkRequest(String userId1, String userId2) {
 		Key key = KeyFactory.createKey("request", "default");
 		Query query = new Query(userId1, key);
 		List<Entity> rs = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
 		for (Entity r : rs) {
 			String requester = (String) r.getProperty("requester");
 			if (requester.equals(userId2))
 				return true;
 		}
 		return false;
 	}
 	
 	//add the add friend reqeust to datastore
 	public void addRequest(String userId1, String userId2) {
 		Key key = KeyFactory.createKey("request", "default");
 		Entity request = new Entity(userId1, key);
 		request.setProperty("requester", userId2);
		datastore.put(request);
 	}
 	
 	//add friend relationship to datastore
 	public void confirmRequest(String userId1, String userId2) {
 		Key key = KeyFactory.createKey("friend", "default");
 		//add to userId1's friend list
 		Entity friend1 = new Entity(userId1, key);
 		friend1.setProperty("friend", userId2);
 		datastore.put(friend1);
 		//add to userId2's friend list
 		Entity friend2 = new Entity(userId2, key);
 		friend2.setProperty("friend", userId1);
 		datastore.put(friend2);
 		//remove from userId1's request list
 		Key rkey = KeyFactory.createKey("request", "default");
 		Query query = new Query(userId1, rkey);
 		List<Entity> rs = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
 		for (Entity r : rs) {
 			String requester = (String) r.getProperty("requester");
 			if (requester.equals(userId2))
 				datastore.delete(r.getKey());
 		}
 	}
 	
 	//show all friend requests
 	public ArrayList<String> showRequests(String userId) {
 		Key key = KeyFactory.createKey("request", "default");
 		Query query = new Query(userId, key);
 		ArrayList<String> friends = new ArrayList<String>();
 		List<Entity> rs = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
 		for (Entity r : rs) {
 			String requester = (String) r.getProperty("requester");
 			friends.add(requester);
 		}
 		return friends;
 	}
 	
 	//show all friends
 	public ArrayList<String> showFriends(String userId) {
 		Key key = KeyFactory.createKey("friend", "default");
 		Query query = new Query(userId, key);
 		List<Entity> rs = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
 		ArrayList<String> friends = new ArrayList<String>();
 		for (Entity ent : rs) {
 			friends.add((String)ent.getProperty("friend"));
 		}
 		
 		return friends;
 	}
 }
