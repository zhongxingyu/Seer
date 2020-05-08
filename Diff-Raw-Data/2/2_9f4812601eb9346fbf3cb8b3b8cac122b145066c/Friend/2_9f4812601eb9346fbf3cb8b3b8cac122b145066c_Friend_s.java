 package com.frienemy.models;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.util.Log;
 
 import com.activeandroid.ActiveRecordBase;
 import com.activeandroid.annotation.Column;
 import com.activeandroid.annotation.Table;
 
 @Table(name = "Friend")
 public class Friend extends ActiveRecordBase<Friend> {
 
 	private static final String TAG = Friend.class.getSimpleName();
 	private static String RelationshipChangedListString = "";
 
 	@Column(name = "uid")
 	public String uid;
 	public String encryptedUid;
 
 	@Column(name = "name")
 	public String name;
 	public String encryptedName;
 
 	@Column(name = "relationshipStatus")
 	public String relationshipStatus = "NA";
 	public String encryptedRelationshipStatus;
 
 	@Column(name = "frienemyStatus")
 	public int frienemyStatus;
 
 	@Column(name = "relationshipChanged")
 	public Boolean relationshipStatusChanged;
 
 	@Column(name = "frienemyStatusChanged")
 	public Boolean frienemyStatusChanged;
 
 	@Column(name = "stalkerRank")
 	public int stalkerRank;
 
 	@Column(name = "isCurrentUser")
 	public Boolean isCurrentUser;
 
 	public static ArrayList<Friend> allFriends(Context context) {
 		ArrayList<Friend> allFriends = Friend.query(context, Friend.class);
 		return allFriends;
 	}
 	
 	public ArrayList<StalkerRelationship> stalkers() {
		ArrayList<StalkerRelationship> stalkers = StalkerRelationship.query(getContext(), StalkerRelationship.class, null, "toFriend== " + getId() + "AND fromFriend!=" + getId(), "rank DESC");
 		return stalkers;
 	}
 
 	public ArrayList<Comment> comments() {
 		ArrayList<Comment> comments = Comment.query(getContext(), Comment.class, null, "friend = " + getId(), "createdTime ASC");
 		return comments;
 	}
 
 	public ArrayList<Post> posts() {
 		ArrayList<Post> posts = Post.query(getContext(), Post.class, null, "from = " + getId(), "updatedTime ASC");
 		return posts;
 	}
 
 	public Friend(Context context) {
 		super(context);
 	}
 
 	public Friend(Context context, JSONObject object) {
 		super(context);
 		try {
 			this.name = object.getString("name");
 			this.uid = object.getString("id");
 
 			this.frienemyStatus = 0;
 			this.isCurrentUser = false;
 			try {
 				String relationshipStatus = object.getString("relationship_status");
 				this.relationshipStatusChanged = false;
 				this.relationshipStatus = relationshipStatus;
 			} catch (JSONException e) {
 
 			}
 		} catch (JSONException e) {
 
 		}
 	}
 
 	public static Friend friendInContextForKeyWithStringValue(Context context, String key, String value) {
 		return querySingle(context, Friend.class, null, String.format("%s = %s", key, value));
 	}
 
 	public static Friend friendInContextForJSONObject(Context context, JSONObject object) {
 		Friend friend = null;
 		RelationshipChangedListString="";
 		try {
 			String n = object.getString("name");
 			String uid = object.getString("id");
 			friend = friendInContextForKeyWithStringValue(context, "uid", uid);
 			if (friend == null) {
 				friend = new Friend(context);
 				friend.uid = uid;
 				friend.name = n;
 				friend.frienemyStatus = 0;
 				friend.isCurrentUser = false;
 			}
 			try {
 				String relationshipStatus = object.getString("relationship_status");
 				if (relationshipStatus.equals(friend.relationshipStatus) == false) {
 					friend.relationshipStatusChanged = true;
 					RelationshipChangedListString+=friend.name + "-";
 				}
 				friend.relationshipStatus = relationshipStatus;
 			} catch (JSONException e) {
 
 			}
 			friend.save();
 		} catch (JSONException e) {
 
 		}
 		return friend;
 	}
 
 	public URL getProfileImageURL() {
 		URL url = null;
 		try {
 			url = new URL(String.format("https://graph.facebook.com/%s/picture", uid));
 		} catch (MalformedURLException e) {
 
 		}
 		return url;
 	}
 	public static String[] getList()
 	{
 		String [] list = RelationshipChangedListString.split("-");
 		return list;
 	}
 
 }
