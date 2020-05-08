 package com.rushdevo.twittaddict.twitter;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class TwitterStatus {
 	private Long id;
 	private String text;
 	private TwitterUser user;
 	private boolean valid;
 	
 	public TwitterStatus(JSONObject hash) {
 		try {
			this.text = hash.getString("text");
 			this.id = hash.getLong("id");
 			this.user = new TwitterUser(hash.getJSONObject("user"));
 			this.valid = true;
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			this.valid = false;
 		}
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setText(String text) {
 		this.text = text;
 	}
 
 	public String getText() {
 		return text;
 	}
 
 	public void setUser(TwitterUser user) {
 		this.user = user;
 	}
 
 	public TwitterUser getUser() {
 		return user;
 	}
 	
 	public boolean isValid() {
 		return this.valid;
 	}
 
 }
