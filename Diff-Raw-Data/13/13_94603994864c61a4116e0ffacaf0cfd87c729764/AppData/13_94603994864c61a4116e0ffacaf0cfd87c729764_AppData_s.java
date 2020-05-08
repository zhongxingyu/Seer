 package com.intalker.borrow.data;
 
 import java.util.ArrayList;
 
 public class AppData {
 	private ArrayList<BookInfo> mBooks = null;
 	private ArrayList<FriendInfo> mFriends = null;
 	private static AppData instance = null;
 
 	public static AppData getInstance() {
 		if (null == instance) {
 			instance = new AppData();
 		}
 		return instance;
 	}
 
 	public AppData() {
 		mBooks = new ArrayList<BookInfo>();
 		mFriends = new ArrayList<FriendInfo>();
 	}
 
 	public ArrayList<BookInfo> getBooks() {
 		return mBooks;
 	}
 
 	public ArrayList<FriendInfo> getFriends() {
 		return mFriends;
 	}
 
 	public void addBook(BookInfo bookInfo) {
 		mBooks.add(bookInfo);
 	}
 
 	public void removeFriend(FriendInfo friendInfo) {
 		if (mFriends.contains(friendInfo))
 			mFriends.remove(friendInfo);
 	}
 
 	public void removeFriend(String friendId) {
 		int indexToRemove = -1;
 		int count = mFriends.size();
 		for (int i = 0; i < count; ++i) {
 			FriendInfo friendInfo = mFriends.get(i);
 			if (friendId.compareTo(friendInfo.getUserInfo().getId()) == 0) {
 				indexToRemove = i;
 				break;
 			}
 		}
 
 		if (indexToRemove >= 0) {
 			mFriends.remove(indexToRemove);
 		}
 	}
 
 	public void removeBook(BookInfo bookInfo) {
 		if (mBooks.contains(bookInfo)) {
 			mBooks.remove(bookInfo);
 		}
 	}
 
 	public void removeBook(String isbn) {
 		int indexToRemove = -1;
 		int count = mBooks.size();
 		for (int i = 0; i < count; ++i) {
 			BookInfo bookInfo = mBooks.get(i);
 			if (isbn.compareTo(bookInfo.getISBN()) == 0) {
 				indexToRemove = i;
 				break;
 			}
 		}
 
 		if (indexToRemove >= 0) {
 			mBooks.remove(indexToRemove);
 		}
 	}
 
 	public void clearBooks() {
 		mBooks.clear();
 	}
 
 	public void clearFriends() {
 		mFriends.clear();
 	}
 
 	// [TODO] User another implementation to improve the performance later
 	public boolean containsBook(String isbn) {
 		for (BookInfo bookInfo : mBooks) {
 			if (bookInfo.getISBN().compareTo(isbn) == 0) {
 				return true;
 			}
 		}
 		return false;
 	}
 }
