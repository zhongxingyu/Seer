 package persistence.dao.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import models.ExternalLink;
 import models.User;
 
 import org.apache.log4j.Logger;
 
 import persistence.dao.api.IUserDao;
 
 import com.mongodb.BasicDBList;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBObject;
 
 public class UserDaoImpl extends BaseDao implements IUserDao {
 
 	private static final Logger LOG = Logger.getLogger(UserDaoImpl.class);
 
 	public void store(User user) {
 
 		DB db = getDatabase();
 		DBCollection coll = db.getCollection(COLLECTION_USERS);
 
 		BasicDBObject mongoUser = new BasicDBObject();
 
 		mongoUser.put(FIELD_USER_AVATARURL, user.getAvatarUrl());
 		mongoUser.put(FIELD_USER_DISPLAYNAME, user.getDisplayname());
 		mongoUser.put(FIELD_USER_FULLNAME, user.getFullname());
 		mongoUser.put(FIELD_USER_NICKNAME, user.getNickname());
 		mongoUser.put(FIELD_USER_OPENID, user.getOpenId());
 		mongoUser.put(FIELD_USER_POSTCOUNT, user.getPostCount());
 		mongoUser.put(FIELD_USER_TIMEZONE, user.getTimezone());
 
 		// store external links
 		List<ExternalLink> externalLinks = user.getExternalLinks();
 		int sizeExternalLinks = (externalLinks != null) ? externalLinks.size()
 				: 0;
 		if (sizeExternalLinks > 0) {
 
 			DBObject list = new BasicDBList();
 
 			for (int i = 0; i < sizeExternalLinks; i++) {
 
 				ExternalLink extlnk = externalLinks.get(i);
 				BasicDBObject dbExtLnk = new BasicDBObject();
 				dbExtLnk.put(FIELD_USER_EXTERNALLINKS_NAME, extlnk.getName());
 				dbExtLnk.put(FIELD_USER_EXTERNALLINKS_URL, extlnk.getUrl());
 
 				list.put(String.valueOf(i), dbExtLnk);
 			}
 
 			mongoUser.put(FIELD_USER_EXTERNALLINKS, list);
 		}
 
 		// Check if user exists
 		if (exists(user.getOpenId())) {
 
 			BasicDBObject query = new BasicDBObject();
 			query.put(FIELD_USER_OPENID, user.getOpenId());
 
 			coll.update(query, mongoUser);
 
 		} else {
 			coll.insert(mongoUser);
 		}
 
 	}
 
 	public boolean exists(String openId) {
 
 		DB db = getDatabase();
 		DBCollection coll = db.getCollection(COLLECTION_USERS);
 		BasicDBObject result;
 
 		BasicDBObject query = new BasicDBObject();
 		query.put(FIELD_USER_OPENID, openId);
 		result = (BasicDBObject) coll.findOne(query);
 		if (result != null) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public User getByOpenId(String openId) {
 		User returnUser = null;
 
		DB db = getDatabase();
		DBCollection coll = db.getCollection(COLLECTION_USERS);
		BasicDBObject result;

 		BasicDBObject query = new BasicDBObject();
 		query.put(FIELD_USER_OPENID, openId);
		result = (BasicDBObject) coll.findOne(query);
 		if (result != null) {
 
 			returnUser = new User();
 			returnUser.setAvatarUrl(result.getString(FIELD_USER_AVATARURL));
 			returnUser.setDisplayname(result.getString(FIELD_USER_DISPLAYNAME));
 			returnUser.setFullname(result.getString(FIELD_USER_FULLNAME));
 			returnUser.setNickname(result.getString(FIELD_USER_NICKNAME));
 			returnUser.setOpenId(result.getString(FIELD_USER_OPENID));
 			returnUser.setPostCount(result.getInt(FIELD_USER_POSTCOUNT));
 			returnUser.setTimezone(result.getString(FIELD_USER_TIMEZONE));
 
 			BasicDBList list = (BasicDBList) result
 					.get(FIELD_USER_EXTERNALLINKS);
 			int sizeExternalLinks = (list != null) ? list.size() : 0;
 			if (sizeExternalLinks > 0) {
 
 				List<ExternalLink> externalLinks = new ArrayList<ExternalLink>(
 						sizeExternalLinks);
 
 				for (int i = 0; i < sizeExternalLinks; i++) {
 					BasicDBObject dbExtLnk = (BasicDBObject) list.get(i);
 
 					ExternalLink newExtLnk = new ExternalLink();
 					newExtLnk.setName(dbExtLnk
 							.getString(FIELD_USER_EXTERNALLINKS_NAME));
 					newExtLnk.setUrl(dbExtLnk
 							.getString(FIELD_USER_EXTERNALLINKS_URL));
 
 					externalLinks.add(newExtLnk);
 				}
 
 				returnUser.setExternalLinks(externalLinks);
 			}
 
 			if (LOG.isDebugEnabled()) {
 				LOG.debug("UserDaoImpl.getByOpenId() returned" + returnUser);
 			}
 		}
 		return returnUser;
 	}
 
 }
