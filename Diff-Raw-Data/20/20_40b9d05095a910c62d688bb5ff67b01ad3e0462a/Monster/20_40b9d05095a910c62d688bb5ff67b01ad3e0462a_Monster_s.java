 package com.wap.battle.client;
 
 import android.os.*;
 import java.util.Iterator;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class Monster implements Parcelable {
 	public static final String NAME = "com.wap.battle.client.Monster";
 	
	private int mData;
 	private String id;
 	private String title;
 	private String gravatar;
 	
 	public Monster(JSONObject monster) {
 		try {
 			Iterator<?> keys = monster.keys();
 			while (keys.hasNext()) {
 				String key = (String) keys.next();
 				if (key.equals("id")) {
 					this.id = monster.getString(key);
 				}
 				if (key.equals("title")) {
 					this.title = monster.getString(key);
 				}
 				if (key.equals("gravatar")) {
 					this.gravatar = monster.getString(key);
 				}
 			}
 		} catch(JSONException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public String getId() {
 		return this.id;
 	}
 	
 	public String getGravatar() {
 		return this.gravatar;
 	}
 	
 	public String getTitle() {
 		return this.title;
 	}
 	
 	public String toString() {
 		return this.title;
 	}
 	
 	public int describeContents() {
 		return 0;
 	}
 	
 	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(mData);
 	}
 	
 	public static final Parcelable.Creator<Monster> CREATOR  = new Parcelable.Creator<Monster>() {
 		public Monster createFromParcel(Parcel in) {
 			return new Monster(in);
 		}
 		
 		 public Monster[] newArray(int size) {
 			 return new Monster[size];
 		 }
 	};
	
	private Monster(Parcel in) {
		mData = in.readInt();
	}
 }
