 package com.intalker.borrow.data;
 
 import java.util.ArrayList;
 
 import android.graphics.Bitmap;
 
 import com.intalker.borrow.util.DBUtil;
 
 public class AppData {
 	private ArrayList<BookInfo> mBooks = null;
 	private ArrayList<BookInfo> mOthersBooks = null;
 	private ArrayList<UserInfo> mAllUsersOnServer = null;
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
 		mOthersBooks = new ArrayList<BookInfo>();
 		mAllUsersOnServer = new ArrayList<UserInfo>();
 		mFriends = new ArrayList<FriendInfo>();
 	}
 	
 	public void initialize() {
 		mBooks.clear();
 		mOthersBooks.clear();
 		mAllUsersOnServer.clear();
 		mFriends.clear();
 		DBUtil.initialize();
 	}
 
 	public ArrayList<BookInfo> getBooks() {
 		return mBooks;
 	}
 	
 	public ArrayList<BookInfo> getOthersBooks() {
 		return mOthersBooks;
 	}
 	
 	public boolean isUnfollowed(String userId) {
 		if (UserInfo.getCurLoggedinUser().getId().compareTo(userId) == 0) {
 			return false;
 		}
 		for (FriendInfo friendInfo : mFriends) {
 			if (friendInfo.getUserInfo().getId().compareTo(userId) == 0) {
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	public ArrayList<UserInfo> getAllUsers() {
 		return mAllUsersOnServer;
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
 	
 	private void recycleCoverImages(ArrayList<BookInfo> books) {
		int count = books.size();
		for(int i = 0; i < count; ++i)
		{
			BookInfo bookInfo = books.get(i);
			Bitmap image = bookInfo.getCoverImage();
			if (null != image && !image.isRecycled()) {
				bookInfo.getCoverImage().recycle();
			}
		}
 	}
 
 	public void clearBooks() {
 		recycleCoverImages(mBooks);
 		mBooks.clear();
 	}
 	
 	public void clearOthersBooks() {
 		recycleCoverImages(mOthersBooks);
 		mOthersBooks.clear();
 	}
 	
 	public void clearAllUsers() {
 		mAllUsersOnServer.clear();
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
 	
 	public boolean containsOthersBook(String isbn) {
 		for (BookInfo bookInfo : mOthersBooks) {
 			if (bookInfo.getISBN().compareTo(isbn) == 0) {
 				return true;
 			}
 		}
 		return false;
 	}
 }
