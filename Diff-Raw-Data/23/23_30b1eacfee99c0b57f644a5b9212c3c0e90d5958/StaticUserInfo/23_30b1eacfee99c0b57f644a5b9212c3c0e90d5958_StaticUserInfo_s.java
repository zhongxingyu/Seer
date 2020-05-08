 package com.hoos.around;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 
 import com.facebook.android.FacebookError;
 import com.facebook.android.Util;
 import com.loopj.android.http.JsonHttpResponseHandler;
 
 public class StaticUserInfo {
 	static private int user_id; //user id stored in mysql db
 	static private String fb_id; //facebook's user id for logged in user
 	static private HashSet<String> fb_friends = new HashSet<String>(); //list of facebook friends of user
 	static private Boolean logged_in = false;
 	static private Boolean Error = false;
 	
 	static Boolean isLoggedIn() {
 		return logged_in;
 	}
 	
 	static void isLoggedIn(Boolean status) {
 		logged_in = status;
 	}
 	
 	static void setUserID(int id) {
 		logged_in = true;
 		user_id = id;
 	}
 	
 	static int getUserID() {
 		return user_id;
 	}
 	
 	static void unSetID() {
 		user_id = 0;
 		logged_in = false;
 	}
 
 	public static String getFbID() {
 		return fb_id;
 	}
 
 	public static void setFbID(String fb_id) {
 		StaticUserInfo.fb_id = fb_id;
 
 	}
 
 	public static HashSet<String> getFbFriends() {
 		return fb_friends;
 	}
 
 	public static void setFbFriends(final HashSet<String> friends) {
 		RestClient.get("/users/view/", null, null, new JsonHttpResponseHandler() {
 			@Override
 			public void onSuccess(JSONArray rsp) {
 				for (int i=0; i<rsp.length(); i++) {
				try {
					JSONObject user = rsp.getJSONObject(i);
					String fb_id = user.optJSONObject("User").getString("fb_id");
					System.out.println("checking " + fb_id);
					if (friends.contains(fb_id)) {
						System.out.println("f: " + fb_id);
						fb_friends.add(fb_id);
 					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
 				}
 			}
 			@Override
 			public void onFailure(Throwable e, String rsp) {
 				
 			}
 		});
 	}
 	
 	//checks to see if friend is HoosAround user and if so adds them
 	public static void addFbFriend(String friend) {
 		final String fr = friend; //needs to be final to be used in inner class
 		RestClient.get("/users/fb_id/"+friend, null, null, new JsonHttpResponseHandler() {
 			@Override
 			public void onSuccess(JSONArray rsp) {
 				if (rsp.length()>0) {
 					//friend exists in HoosAround db
 					fb_friends.add(fr);
 				}
 				else {
 					//friend is not HoosAround user
 				}
 			}
 			@Override
 			public void onFailure(Throwable e, String rsp) {
 				Log.d("JSON", rsp);
 				Log.d("JSON", RestClient.getAbsoluteUrl("/users/fb_id/"+fr));
 			}
 		});
 	}
 }
