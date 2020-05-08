 package com.atlast.MegaLike.Lib;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 import java.util.concurrent.ConcurrentHashMap;
 
 import com.atlast.MegaLike.FacebookLogic.DataManager;
 import com.atlast.MegaLike.FacebookLogic.FQLFriend;
 import com.atlast.MegaLike.FacebookLogic.Photo;
 
 public class FacebookData {
 	public static DataManager mDataManager;
 	private final Map<String, List<FQLFriend>> mSearchSuggestionsDict = new ConcurrentHashMap<String, List<FQLFriend>>();
 	public List<FQLFriend> mFriends; //TODO change to private
 
 	public FacebookData(String accessToken) {
 		mDataManager = new DataManager(accessToken);
 		if(mFriends == null) loadFriends();
 	}
 
 	private void loadFriends() {
 		mFriends = mDataManager.getFriends();
 		for (FQLFriend friend : mFriends)
 			addFriend(friend);
 	}
 
 	private void addFriend(FQLFriend friend) {
 		String name = friend.name.toLowerCase();
 		int len = name.length();
 		for (int i = 0; i < len; i++) {
 			String prefix = name.substring(0, len - i);
 			addMatch(prefix, friend);
 		}
 	}
 
 	private void addMatch(String query, FQLFriend friend) {
 		List<FQLFriend> matches = mSearchSuggestionsDict.get(query);
 		if (matches == null) {
 			matches = new ArrayList<FQLFriend>();
 			mSearchSuggestionsDict.put(query, matches);
 		}
 		matches.add(friend);
 	}
 	
 	public List<FQLFriend> getMatches(String query) {
         List<FQLFriend> list = mSearchSuggestionsDict.get(query);
         return list == null ? (List<FQLFriend>) Collections.EMPTY_LIST : list;
     }
 
 	public Vector<Photo> getPhotosAll(String friendUID) {
 		return mDataManager.getAllUserCombinedPhotosByPart(friendUID);
 	}
 
 	public Vector<Photo> getPhotosTagged(String friendUID) {
 		return mDataManager.getAllUserTaggedPhotos(friendUID);
 	}
 
 	public Vector<Photo> getPhotosUploaded(String friendUID) {
 		return mDataManager.getAllUserPhotos(friendUID);
 	}
 
 	public Vector<Photo> getPhotosStarred() {
		return new Vector<Photo>();
 	}
 
 	public String[] getLinks(int userID) {
 		return null;
 	}
 	
 	public Vector<Photo> getPhotos(int index, String friendUID) {
 		switch (index) {
 		case 0:
 			return getPhotosAll(friendUID);
 		case 1:
 			return getPhotosTagged(friendUID);
 		case 2:
 			return getPhotosUploaded(friendUID);
 		case 3:
 			return getPhotosStarred();
 		default:
 			return getPhotosAll(friendUID);
 		}
 	}
 }
