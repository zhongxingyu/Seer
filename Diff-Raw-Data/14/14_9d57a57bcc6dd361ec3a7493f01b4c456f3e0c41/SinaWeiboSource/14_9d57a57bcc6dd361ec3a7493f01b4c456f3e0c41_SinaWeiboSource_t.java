 /* vi:set noet: */
 package com.usns.sources;
 
 import com.usns.DB;
 import com.usns.entities.Post;
 import java.util.ArrayList;
 
 import weibo4j.Oauth;
 import weibo4j.Timeline;
 import weibo4j.http.AccessToken;
 import weibo4j.model.Paging;
 import weibo4j.model.Status;
 import weibo4j.model.StatusWapper;
 import weibo4j.model.WeiboException;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBCollection;
 import com.mongodb.DBObject;
 
 public class SinaWeiboSource {
 	public static String SOURCE_NAME = "SinaWeibo";
 
 	public static ArrayList<Post> getTokenPostsSince(String owner, String token,
 			String sinceId) {
 		Timeline tm = new Timeline();
 		tm.client.setToken(token);
 		ArrayList<Post> result = null;
 		try {
 			Paging paging = sinceId == null ? new Paging(1, 100) : new Paging(
 					1, 100, Long.parseLong(sinceId));
 			StatusWapper sw = tm.getHomeTimeline(0, 0, paging);
 			result = new ArrayList<Post>(sw.getStatuses().size());
 			for (Status status : sw.getStatuses()) {
 				Post post = new Post();
 				post.owner = owner;
 				post.uid = status.getUser().getId();
 				post.user = status.getUser().getName();
 				post.source = SOURCE_NAME;
 				post.sourceId = status.getId();
 				post.date = status.getCreatedAt();
 				post.text = status.getText();
 				result.add(post);
 			}
 		} catch (WeiboException e) {
 			// If anything, we need to invalidate this token. It fails!
 			// XXX Maybe use refresh token?
 			DB.getCollection("tokens").remove(
 					new BasicDBObject("access-token", token).append("source",
 							SOURCE_NAME));
 
 			e.printStackTrace();
 		}
 		//System.out.println("get " + result.size() + " new tweets from Weibo");
 		return result;
 	}
 
 	public static ArrayList<Post> getUserPostsSince(String user, String sinceId) {
 		DBCollection tokens = DB.getCollection("tokens");
 		DBObject token = tokens.findOne(new BasicDBObject("user", user).append(
 				"source", SOURCE_NAME));
 		if (token == null)
 			return null;
 		return getTokenPostsSince(user, (String) token.get("access-token"), sinceId);
 	}
 
 	public static ArrayList<Post> getUserPosts(String user) {
 		return getUserPostsSince(user, null);
 	}
 
 	public static void putNewUserToken(String user, String code) {
 		DBCollection tokens = DB.getCollection("tokens");
 		AccessToken at;
 		try {
 			at = new Oauth().getAccessTokenByCode(code);
			tokens.update(new BasicDBObject("user", user), 
					new BasicDBObject("user", user)
 					.append("source", SOURCE_NAME)
 					.append("access-token", at.getAccessToken())
					.append("refresh-token", at.getRefreshToken()), 
					true, false);
 		} catch (WeiboException e) {
 			e.printStackTrace();
 		}
 	}
 }
