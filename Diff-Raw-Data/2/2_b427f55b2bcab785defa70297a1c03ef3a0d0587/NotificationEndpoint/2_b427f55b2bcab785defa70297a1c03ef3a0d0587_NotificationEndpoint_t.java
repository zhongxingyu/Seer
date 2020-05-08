 package com.gatech.faceme.endpoints;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.inject.Named;
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 
 import com.gatech.faceme.entity.NotificationEntity;
 import com.gatech.faceme.entity.PairTableEntity;
 import com.gatech.faceme.mediastore.PMF;
 import com.google.api.server.spi.config.Api;
 import com.google.api.server.spi.config.ApiMethod;
 
 @Api(name = "notificationendpoint", description = 
 "NotificationAPI", version = "v1")
 public class NotificationEndpoint {
 	
 	@ApiMethod(httpMethod = "GET", name = "notification.list", path = "notification/list")
 	@SuppressWarnings({ "cast", "unchecked" })
 	public List<NotificationEntity> listNotification() {
 		PersistenceManager mgr = PMF.get().getPersistenceManager();
 		List<NotificationEntity> result = new ArrayList<NotificationEntity>();
 		try {
 			Query query = mgr.newQuery(NotificationEntity.class);
 			for (Object obj : (List<Object>) query.execute()) {
 				result.add(((NotificationEntity) obj));
 			}
 		} finally {
 			mgr.close();
 		}
 		return result;
 	}
 	
 	@ApiMethod(httpMethod = "GET", name = "notification.getfromuser", 
 			path = "notification/getallnotification/{userid}")
 	@SuppressWarnings({ "cast", "unchecked" })
 	public ArrayList<NotificationEntity> getNotNotifiedById(@Named("userid") String userID) {
 		PersistenceManager mgr = getPersistenceManager();
 		ArrayList<NotificationEntity> result = new ArrayList<NotificationEntity>();
 		try {
 //			Query query = mgr.newQuery("select from PairTableEntity " +
 //										"where activeUser ==  mainuserid"+
 //										"parameters String mainuserid "
 //										);
 			Query query = mgr.newQuery(NotificationEntity.class, 
					"userName ==userID");
 		
 			for (Object obj : (List<Object>) query.execute()) {
 				result.add(((NotificationEntity) obj));
 			}
 			
 		} finally {
 			mgr.close();
 		}
 		return result;
 	}
 	
 //	@ApiMethod(httpMethod = "GET", name = "pairtable.get", 
 //			path = "pairtable/getallpairtable/{userid}")
 //	@SuppressWarnings({ "cast", "unchecked" })
 //	public ArrayList<PairTableEntity> getallpairtableById(@Named("userid") String userID) {
 //		PersistenceManager mgr = getPersistenceManager();
 //		ArrayList<PairTableEntity> result = new ArrayList<PairTableEntity>();
 //		try {
 //
 //			Query query = mgr.newQuery(PairTableEntity.class, 
 //					"activeUser ==userID");
 //			
 //			for (Object obj : (List<Object>) query.execute()) {
 //				result.add(((PairTableEntity) obj));
 //			}
 //			
 //		} finally {
 //			mgr.close();
 //		}
 //		return result;
 //	}
 //	
 //	@ApiMethod(httpMethod = "GET", name = "twoparameters.get", 
 //			path = "pairtable/gettwoparameters/{userid}/{username}")
 //	@SuppressWarnings({ "cast", "unchecked" })
 //	public ArrayList<PairTableEntity> tryTwoParameters(
 //			@Named("userid") String userID, @Named("username") String userName) {
 //		PersistenceManager mgr = getPersistenceManager();
 //		ArrayList<PairTableEntity> result = new ArrayList<PairTableEntity>();
 //		try {
 //			Query query = mgr.newQuery(PairTableEntity.class, 
 //					"(activeUser ==userID) && (key ==userName)");
 //			
 //			for (Object obj : (List<Object>) query.execute()) {
 //				result.add(((PairTableEntity) obj));
 //			}
 //			
 //		} finally {
 //			mgr.close();
 //		}
 //		return result;
 //	}
 	
 	@ApiMethod(httpMethod = "POST", name = "notification.insert",
 			path = "notification/insert")
 	//curl -H 'Content-Type: application/json' -d '{ "activeUser": activeUserkey,"otherUsers": [{userKey1},{userKey2}],
 	//"activeUserFace": "activeUserFaceKey", "otherUserFace":[{keyFaceKey1},{keyFaceKey2}], "ifNotified": false}' 
 	// http://localhost:8888/_ah/api/pairtableendpoint/v1/pairtable/insert
 	public NotificationEntity addNotificationEntity(NotificationEntity notification) {
 		PersistenceManager pm = getPersistenceManager();
 		pm.makePersistent(notification);
 		pm.close();
 		return notification;
 	}
 	
 	private static PersistenceManager getPersistenceManager() {
 		return PMF.get().getPersistenceManager();
 	}
 }
