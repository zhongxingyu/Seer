 package com.frienemy.requests;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.util.Log;
 
 import com.facebook.android.FacebookError;
 import com.facebook.android.AsyncFacebookRunner.RequestListener;
 import com.frienemy.models.Friend;
 
 public class FriendsRequestListener implements RequestListener {
 
 	public interface FriendRequestListenerResponder {
 		public void friendRequestDidFinish(int totalFriends);
 		public void friendRequestDidFail();
 	}
 
 	private static final String TAG = FriendsRequestListener.class.getSimpleName();
 	private Context context;
 	private FriendRequestListenerResponder responder;
 	private List<Friend> friends;
 	private static String FrienemiesListString = "";
 
 
 	public FriendsRequestListener(Context context, FriendRequestListenerResponder responder) {
 		this.context = context;
 		this.responder = responder;
 	}
 
 	public void setFriends(List<Friend> friends) {
 		this.friends = friends;
 	}
 
 	public List<Friend> getFriends() {
 		return friends;
 	}
 
 	public void onComplete(String response, Object state) {
 		try {
 			final JSONObject json = new JSONObject(response);
 			JSONArray d = json.getJSONArray("data");
 			int l = (d != null ? d.length() : 0);
 			ArrayList<Friend> fetchedFriends = Friend.query(context, Friend.class, null, "isCurrentUsersFriend==1");
 			if (fetchedFriends.size() < 1) {
 				saveFirstFriendsList(d);
 				return;
 			}
 			FrienemiesListString="";
 			for (int f=0; f<fetchedFriends.size(); f++) {
 				boolean exists = false;
 				Friend friend = fetchedFriends.get(f);
 				for (int i=0; i<l; i++) {
 					JSONObject o = d.getJSONObject(i);
 					if (friend.uid.matches(o.getString("id"))) {
 						exists = true;
 						friend.frienemyStatus = 0;
 						break;
 					}
 				}
 				if (!exists && friend.frienemyStatus != 1) {
 					friend.frienemyStatus = 1;
 					FrienemiesListString += friend.name + "-";
 					friend.frienemyStatusChanged = true;
 					friend.save();
 				}
 			}
 			for (int i=0; i<l; i++) {
 				JSONObject o = d.getJSONObject(i);
 				Log.d(TAG, o.toString());
 				Friend friend = Friend.friendInContextForJSONObject(context, o);
 				if (friend.frienemyStatus == 2) {
 					friend.frienemyStatus = 0;
 					friend.isCurrentUsersFriend = true;
 					friend.save();
 					fetchedFriends.add(friend);
 				}
 			}
 			setFriends(fetchedFriends);
 			if (responder != null) {
 				responder.friendRequestDidFinish(l);
 			}
 		} catch (JSONException e) {
 			if (responder != null) {
 				responder.friendRequestDidFail();
 			}
 		}
 	}
 
 	private void saveFirstFriendsList(JSONArray array) {
 		FrienemiesListString="";
 		int l = (array != null ? array.length() : 0);
 		for (int i=0; i<l; i++) {
 			try {
 				JSONObject o = array.getJSONObject(i);
 				Friend friend = new Friend(context, o);
 				friend.isCurrentUsersFriend = true;
 				friend.save();
 			} catch (JSONException e) {
 
 			}
 		}
 		if (responder != null) {
 			responder.friendRequestDidFinish(l);
 		}
 	}
 
 	public void onIOException(IOException e, Object state) {
 		if (responder != null) {
 			responder.friendRequestDidFail();
 		}
 	}
 
 	public void onFileNotFoundException(FileNotFoundException e, Object state) {
 		if (responder != null) {
 			responder.friendRequestDidFail();
 		}
 	}
 
 	public void onMalformedURLException(MalformedURLException e, Object state) {
 		if (responder != null) {
 			responder.friendRequestDidFail();
 		}
 	}
 
 	public void onFacebookError(FacebookError e, Object state) {
 		if (responder != null) {
 			responder.friendRequestDidFail();
 		}
 	}
 
 	public static String[] getList()
 	{
 		String [] list = FrienemiesListString.split("-");
 		return list;
 	}
 
 }
