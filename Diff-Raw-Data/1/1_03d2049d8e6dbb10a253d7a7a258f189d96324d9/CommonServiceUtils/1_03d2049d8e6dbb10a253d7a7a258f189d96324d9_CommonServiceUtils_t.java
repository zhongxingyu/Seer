 package com.orange.place.api.service;
 
 import java.util.List;
 
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 
 import com.orange.place.constant.DBConstants;
 import com.orange.place.constant.ServiceConstant;
 import com.orange.place.dao.App;
 import com.orange.place.dao.Message;
 import com.orange.place.dao.Place;
 import com.orange.place.dao.Post;
 import com.orange.place.dao.User;
 
 public class CommonServiceUtils {
 
 	public static JSONObject postToJSON(Post post) {
 
 		JSONObject json = new JSONObject();
 		json.put(ServiceConstant.PARA_POSTID, post.getPostId());
 		json.put(ServiceConstant.PARA_USERID, post.getUserId());
 		json.put(ServiceConstant.PARA_PLACEID, post.getPlaceId());
 		json.put(ServiceConstant.PARA_LONGTITUDE, post.getLongitude());
 		json.put(ServiceConstant.PARA_LATITUDE, post.getLatitude());
 		json.put(ServiceConstant.PARA_USER_LONGITUDE, post.getUserLongitude());
 		json.put(ServiceConstant.PARA_USER_LATITUDE, post.getUserLatitude());
 		json.put(ServiceConstant.PARA_TEXT_CONTENT, post.getTextContent());
 		json.put(ServiceConstant.PARA_CONTENT_TYPE, post.getContentType());
 		json.put(ServiceConstant.PARA_IMAGE_URL, post.getImageURL());
 		json.put(ServiceConstant.PARA_TOTAL_VIEW, post.getTotalView());
 		json.put(ServiceConstant.PARA_TOTAL_FORWARD, post.getTotalForward());
 		json.put(ServiceConstant.PARA_TOTAL_QUOTE, post.getTotalQuote());
 		json.put(ServiceConstant.PARA_TOTAL_REPLY, post.getTotalReply());
 		json.put(ServiceConstant.PARA_CREATE_DATE, post.getCreateDate());
 		json.put(ServiceConstant.PARA_SRC_POSTID, post.getSrcPostId());
 		json.put(ServiceConstant.PARA_NICKNAME, post.getUserNickName());
 		json.put(ServiceConstant.PARA_AVATAR, post.getUserAvatar());
 		json
 				.put(ServiceConstant.PARA_TOTAL_RELATED, post
 						.getTotalRelatedPost());
 		json.put(ServiceConstant.PARA_IMAGE_URL, post.getImageURL());
 		json.put(ServiceConstant.PARA_NAME, post.getPlaceName());
 		json.put(ServiceConstant.PARA_GENDER, post.getUserGender());
 		
 		// action type is a special parameter, so use DBConstants instead of defining a parameter
 		json.put(DBConstants.F_ACTION_LIKE_COUNT, post.getActionLikeCount());
 		return json;
 	}
 
 	private static JSONArray postListToJSON(List<Post> postList) {
 		// TODO if JSON value is null, don't put into the JSON object
 		// set result data, return postArray
 		JSONArray obj = new JSONArray();
 		for (Post post : postList) {
 			obj.add(postToJSON(post));
 		}
 
 		return obj;
 	}
 
 	public static JSONArray postListToJSON(List<Post> postList,
 			String excludePostId) {
 
 		if (excludePostId == null || excludePostId.length() == 0) {
 			return postListToJSON(postList);
 		}
 
 		boolean found = false;
 		JSONArray obj = new JSONArray();
 		for (Post post : postList) {
 
 			if (!found) {
 				String postId = post.getPostId();
 				if (excludePostId.equalsIgnoreCase(postId)) {
 					found = true;
 					continue;
 				}
 			}
 
 			obj.add(postToJSON(post));
 		}
 
 		return obj;
 	}
 
 	private static JSONArray postListToJSON(List<Post> postList,
 			String excludePostId, boolean ignoreFirstPost) {
 		JSONArray obj = new JSONArray();
 		int count = 0;
 		for (Post post : postList) {
 			count++;
 			if (count == 1 && ignoreFirstPost) {
 				continue;
 			}
 
 			String postId = post.getPostId();
 			if (excludePostId != null && excludePostId.equalsIgnoreCase(postId))
 				continue;
 
 			obj.add(postToJSON(post));
 		}
 
 		return obj;
 	}
 
 	public static JSONObject placeToJSON(Place place) {
 		JSONObject json = new JSONObject();
 		json.put(ServiceConstant.PARA_CREATE_USERID, place.getCreateUserId());
 		json.put(ServiceConstant.PARA_PLACEID, place.getPlaceId());
 		json.put(ServiceConstant.PARA_LONGTITUDE, place.getLongitude());
 		json.put(ServiceConstant.PARA_LATITUDE, place.getLatitude());
 		json.put(ServiceConstant.PARA_NAME, place.getName());
 		json.put(ServiceConstant.PARA_DESC, place.getDesc());
 		json.put(ServiceConstant.PARA_RADIUS, place.getRadius());
 		json.put(ServiceConstant.PARA_POSTTYPE, place.getPostType());
 		json.put(ServiceConstant.PARA_CREATE_DATE, place.getCreateDate());
 		return json;
 	}
 
 	private static JSONArray placeListToJSON(List<Place> placeList) {
 		// TODO if JSON value is null, don't put into the JSON object
 		// set result data, return postArray
 		JSONArray obj = new JSONArray();
 		for (Place place : placeList) {
 			obj.add(placeToJSON(place));
 		}
 		return obj;
 	}
 
 	public static JSONArray placeListToJSON(List<Place> placeList,
 			String excludePlaceId) {
 
 		if (excludePlaceId == null || excludePlaceId.length() == 0) {
 			return placeListToJSON(placeList);
 		}
 
 		boolean found = false;
 		JSONArray obj = new JSONArray();
 		for (Place place : placeList) {
 
 			if (!found) {
 				String placeId = place.getPlaceId();
 				if (excludePlaceId.equalsIgnoreCase(placeId)) {
 					found = true;
 					continue;
 				}
 			}
 
 			obj.add(placeToJSON(place));
 		}
 
 		return obj;
 	}
 
 	public static JSONObject userToJSON(User user) {
 		JSONObject obj = new JSONObject();
 		obj.put(ServiceConstant.PARA_USERID, user.getUserId());
 		obj.put(ServiceConstant.PARA_NICKNAME, user.getNickName());
 		obj.put(ServiceConstant.PARA_LOGINID, user.getLoginId());
 		obj.put(ServiceConstant.PARA_SINA_ACCESS_TOKEN, user
 				.getSinaAccessToken());
 		obj.put(ServiceConstant.PARA_SINA_ACCESS_TOKEN_SECRET, user
 				.getSinaAccessTokenSecret());
 		obj.put(ServiceConstant.PARA_QQ_ACCESS_TOKEN, user.getQQAccessToken());
 		obj.put(ServiceConstant.PARA_QQ_ACCESS_TOKEN_SECRET, user
 				.getQQAccessTokenSecret());
 		obj.put(ServiceConstant.PARA_QQID, user.getQQId());
 		obj.put(ServiceConstant.PARA_SINAID, user.getSinaId());
 		obj.put(ServiceConstant.PARA_RENRENID, user.getRenrenId());
 		obj.put(ServiceConstant.PARA_FACEBOOKID, user.getFacebookId());
 		obj.put(ServiceConstant.PARA_TWITTERID, user.getTwitterId());
 		obj.put(ServiceConstant.PARA_GENDER, user.getGender());
 
 		return obj;
 	}
 
 	public static JSONObject messageToJSON(Message message) {
 		JSONObject json = new JSONObject();
 		json.put(ServiceConstant.PARA_MESSAGE_ID, message.getMessageId());
 		json.put(ServiceConstant.PARA_USERID, message.getFromUserId());
 		json.put(ServiceConstant.PARA_TO_USERID, message.getToUserId());
 		json.put(ServiceConstant.PARA_MESSAGETEXT, message.getMessageContent());
 		json.put(ServiceConstant.PARA_CREATE_DATE, message.getCreateDate());
 		json.put(ServiceConstant.PARA_MESSAGE_TYPE, message.getMessageType());
 		json.put(ServiceConstant.PARA_AVATAR, message.getUserAvatar());
 		json.put(ServiceConstant.PARA_NICKNAME, message.getUserNickName());
 		return json;
 	}
 
 	public static JSONArray messageListToJSON(List<Message> messageList) {
 		JSONArray obj = new JSONArray();
 		for (Message message : messageList) {
 			JSONObject json = messageToJSON(message);
 			obj.add(json);
 		}
 
 		return obj;
 	}
 
 	public static JSONArray messageListToJSON(List<Message> messageList,
 			String excludeMessageId) {
 		if (excludeMessageId == null || excludeMessageId.length() == 0) {
 			return messageListToJSON(messageList);
 		}
 		boolean found = false;
 		JSONArray obj = new JSONArray();
 		for (Message message : messageList) {
 			if (!found) {
 				String messageId = message.getMessageId();
 				if (excludeMessageId.equalsIgnoreCase(messageId)) {
 					found = true;
 					continue;
 				}
 			}
 			obj.add(messageToJSON(message));
 		}
 		return obj;
 	}
 
 	public static JSONArray appListToJSON(List<App> appList) {
 		JSONArray obj = new JSONArray();
 		for (App app : appList) {
 			JSONObject json = new JSONObject();
 			json.put(ServiceConstant.PARA_APPID, app.getAppId());
 			json.put(ServiceConstant.PARA_APPURL, app.getAppUrl());
 			json.put(ServiceConstant.PARA_NAME, app.getAppName());
 			json.put(ServiceConstant.PARA_DESC, app.getAppDesc());
 			json.put(ServiceConstant.PARA_ICON, app.getAppIcon());
 			obj.add(json);
 		}
 
 		return obj;
 	}
 
 }
