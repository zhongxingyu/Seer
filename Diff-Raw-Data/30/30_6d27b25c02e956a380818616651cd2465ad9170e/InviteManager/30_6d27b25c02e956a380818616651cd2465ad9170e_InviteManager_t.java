 package com.foodcirclesserver.circles;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import com.foodcirclesserver.user.User;
 import com.foodcirclesserver.user.UserManager;
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.EntityNotFoundException;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.datastore.Query;
 
 public class InviteManager  {
 
 	public static final String TYPE = "circle_invite";
 
 	public static final String CIRCLE_ID = CircleManager.CIRCLE_ID;
 	public static final String SENDER_ID = "sender_id";
 	public static final String RECEIVER_ID = "receiver_id";
 
 	public static final String WAS_RECEIVED = "was_received";
 
 	public static Key saveInvite(Long circleID, String senderID, String receiverID, DatastoreService ds) {
 		//first check for repeat invite
 		//if invite already exists, then set "was_received" to false
 		Query q = new Query(TYPE).addFilter(CIRCLE_ID, Query.FilterOperator.EQUAL, circleID);
 		q.addFilter(SENDER_ID, Query.FilterOperator.EQUAL, senderID);
 		q.addFilter(RECEIVER_ID, Query.FilterOperator.EQUAL, receiverID);
 
 		Entity result = ds.prepare(q).asSingleEntity();
 
 		if (result != null) {
 			result.setProperty(WAS_RECEIVED, true);
 		} else {
 			if (UserManager.getUser(receiverID, ds) == null) {
 				//TODO: user hasn't joined! send em an invite!
 			}
 			result = new Entity(TYPE);
 			result.setProperty(CIRCLE_ID, circleID);
 			result.setProperty(RECEIVER_ID, receiverID);
 			result.setProperty(SENDER_ID, senderID);
			result.setProperty(WAS_RECEIVED, false);
 		}
 
 		ds.put(result);
 		return result.getKey();
 	}
 
 
 	public static void markReceived(Long keyID, DatastoreService ds) {
 		Key k = KeyFactory.createKey(TYPE, keyID);
 
 		try {
 			Entity result = ds.get(k);
 			result.setProperty(WAS_RECEIVED, true);
 			ds.put(result);
 		} catch (EntityNotFoundException e) {
 			return;
 		}
 
 	}
 
 /*	public static void markReceived(Long circleID, String senderID, String receiverID,
 			DatastoreService ds) {
 		Query q = new Query(TYPE).addFilter(CIRCLE_ID, Query.FilterOperator.EQUAL, circleID);
 		q.addFilter(SENDER_ID, Query.FilterOperator.EQUAL, senderID);
 		q.addFilter(RECEIVER_ID, Query.FilterOperator.EQUAL, receiverID);
 
 		Entity result = ds.prepare(q).asSingleEntity();
 
 		if (result != null) {
 			result.setProperty(WAS_RECEIVED, true);
 			ds.put(result);
 		}
 	}*/
 
 	public static List<CircleInvite> getInvitesForUser(String userID, DatastoreService ds) {
 
 		Query q = new Query(TYPE).addFilter(RECEIVER_ID, Query.FilterOperator.EQUAL, userID);
		q.addFilter(WAS_RECEIVED, Query.FilterOperator.EQUAL, false);
 
 		Iterator<Entity> result = ds.prepare(q).asIterator();
 
 		List<CircleInvite> invites = new LinkedList<CircleInvite>();
 
 		while(result.hasNext()) {
 			CircleInvite i = inviteFromEntity(result.next(), ds);
 			if (i != null)
 				invites.add(i);
 		}
 
 		return invites;
 	}
 
 	public static CircleInvite inviteFromEntity(Entity e, DatastoreService ds) {
 		Long circleID = (Long) e.getProperty(CIRCLE_ID);
 		Circle c = CircleManager.getCircleByID(circleID, ds);
 
 		Long keyID = e.getKey().getId();
 
 		String senderID = (String) e.getProperty(SENDER_ID);
 		User u = UserManager.getUser(senderID, ds);
 
 		return new CircleInvite(c.name, keyID, u.name);
 	}
 
 }
