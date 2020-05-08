 package com.merono.g;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 
 public class Post {
 	private String body;
 	private String name;
 	private String time;
 	private String id;
 	private String imgUrl;
 	private String fullImgUrl;
 
 	private static final String TAG = "Post";
 
 	Post(JSONObject postJSON, String boardName) {
		body = "";
		name = "no name";
		time = "now";
		id = "ぬるぽ";

 		try {
 			body = postJSON.getString("com").replaceAll("<br>", "\n");
 			body = body.replaceAll("&quot;", "\"");
 			body = body.replaceAll("</?span.*?>", "");
 			body = body.replaceAll("</?a.*?>", "");
 			body = body.replaceAll("&gt;", ">");
 			body = body.replaceAll("&lt;", "<");
 			body = body.replaceAll("&amp;", "&");
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 
 		try {
 			name = postJSON.getString("name");
 			time = postJSON.getString("now");
 			id = "No." + postJSON.getInt("no");
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 
 		boolean hasImage = true;
 		try {
 			imgUrl = "http://thumbs.4chan.org/" + boardName + "/thumb/"
 					+ postJSON.getLong("tim") + "s.jpg";
 			fullImgUrl = "http://images.4chan.org/" + boardName + "/src/"
 					+ postJSON.getLong("tim") + postJSON.getString("ext");
 		} catch (JSONException e) {
 			imgUrl = "";
 			fullImgUrl = "";
 			hasImage = false;
 		}
 
 		if (hasImage) {
 			name = name + " <img>";
 		}
 
 	}
 
 	public String getText() {
 		return body;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public String getTime() {
 		return time;
 	}
 
 	public String getId() {
 		return id;
 	}
 
 	public String getImgURL() {
 		return imgUrl;
 	}
 
 	public String getFullImgUrl() {
 		return fullImgUrl;
 	}
 
 	@Override
 	public String toString() {
 		return body;
 	}
 }
