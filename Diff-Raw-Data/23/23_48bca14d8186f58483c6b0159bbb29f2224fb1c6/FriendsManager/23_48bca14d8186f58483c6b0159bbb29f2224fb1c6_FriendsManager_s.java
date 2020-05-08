 package ch.unibe.ese.shopnote.core;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import android.content.ContentResolver;
 import android.database.Cursor;
 import android.os.Handler;
 import android.provider.ContactsContract;
 import ch.unibe.ese.shopnote.share.SyncManager;
 import ch.unibe.ese.shopnote.share.requests.FriendRequest;
 
 /**
  * The FriendsManager provides all information about Friends locally stored on your phone<br>
  * He knows which friend is assigned to which shoppinglist.
  *
  */
 public class FriendsManager {
 	private List<Friend> friendsList;
 	private PersistenceManager persistenceManager;
 	private SyncManager syncManager;
 	private BaseActivity baseActivity;
 	private ArrayList<Friend> friendsInPhoneBook;
 
 	public FriendsManager(PersistenceManager persistenceManager, SyncManager syncManager, BaseActivity baseActivity) {
 		this.persistenceManager = persistenceManager;
 		friendsList = persistenceManager.getFriends();
 		this.syncManager = syncManager;
 		this.baseActivity = baseActivity;
 	}
 
 	/**
 	 * Checks if friend is on the server and adds him to the friendlist when
 	 * successful
 	 * 
 	 * @param phoneNr
 	 * @param name
 	 * @return friend which is now in the db, if friend has not the app, null
 	 */
 	public Friend addFriend(Friend friend) {
 		Friend compare = checkIfDouble(friend);
 		if (compare != null) return compare;
 		
 		// Save friend to database to get id
 		persistenceManager.save(friend);
 		
 		if(baseActivity != null && !friend.hasTheApp())
 			checkIfFriendHasApp(friend);
 		
 		friend = getFriendFromDB(friend.getId());
 		if(friend.hasTheApp()) {
 			friendsList.add(friend);
 			return friend;
 		}
 		
 		persistenceManager.removeFriend(friend);
 		return null;
 	}
 
 	private void checkIfFriendHasApp(Friend friend) {
 		FriendRequest fr = new FriendRequest(friend);
 		syncManager.addRequest(fr);	
 		syncManager.synchroniseAndWaitForTaskToEnd(baseActivity);
 	}
 
 	private Friend checkIfDouble(Friend friend) {
 		for(Friend compare: friendsList) 
 			if(compare.getPhoneNr().equals(friend.getPhoneNr())) return compare;
 		return null;
 	}
 
 	/**
 	 * Gets a list of all friends added (also friends who do not have the app)
 	 * @return List of all added friends, unmodifiable
 	 */
 	public List<Friend> getFriendsList() {
 		Collections.sort(friendsList, Comparators.FRIEND_COMPARATOR);
 		return Collections.unmodifiableList(friendsList);
 	}
 	
 	/**
 	 * Gets a list of all friends which have the app
 	 * @return List of all friends with the app, unmodifiable
 	 */
 	public List<Friend> getFriendsWithApp() {
 		List<Friend> list = new ArrayList<Friend>();
 		for(Friend friend: friendsList)
 			if(friend.hasTheApp())
 				list.add(friend);
 		return Collections.unmodifiableList(list);
 	}
 
 	/**
 	 * Permits to update the name of a friend
 	 * 
 	 * @param updated
 	 *            friend
 	 */
 	public void update(Friend friend) {
 		persistenceManager.save(friend);
 	}
 
 	/**
 	 * Removes friend only on the phone, no access to server is needed.
 	 * 
 	 * @param friend
 	 *            to remove
 	 * @return if successful
 	 */
 	public boolean removeFriend(Friend friend) {
 		friend = getFriend(friend.getId());
 		if(friend != null) {
 			this.persistenceManager.removeFriend(friend);
 			return this.friendsList.remove(friend);
 		}
 		return false;
 	}
 
 	/**
 	 * Returns the friend to which the number belongs, if no friend found,
 	 * returns null
 	 * 
 	 * @param PhoneNr
 	 * @return
 	 */
 	public Friend getFriend(long id) {
 		for (Friend friend : friendsList) {
 			if (friend.getId() == id)
 				return friend;
 		}
 		return null;
 	}
 	
 	public Friend getFriendFromDB(long id) {
 		List<Friend> friends = persistenceManager.getFriends();
 		for(Friend friend: friends)
 			if(friend.getId() == id)
 				return friend;
 		return null;
 	}
 	
 	/**
 	 * Gets the friend with the specific phoneNr
 	 * @param phoneNr - format matters!
 	 * @return friend if found, else null
 	 */
 	public Friend getFriendWithPhoneNr(String phoneNr) {
 		for(Friend friend: friendsList)
 			if(friend.getPhoneNr().equals(phoneNr))
 				return friend;
 		return null;
 	}
 	
 	/**
 	 * Get all friends which are assigned to this shoppinglist
 	 * @param list
 	 * @return
 	 */
 	public List<Friend> getSharedFriends(ShoppingList list) {
 		return persistenceManager.getSharedFriends(list);
 	}
 	
 	/**
 	 * Get all friends which are assigned to this recipe
 	 * @param recipe
 	 * @return
 	 */
 	public List<Friend> getSharedFriends(Recipe recipe) {
 		return persistenceManager.getSharedFriends(recipe);
 	}
 	
 	/**
 	 * Adds a friend to a synchronized shopping list
 	 * @param friend
 	 * @param list
 	 */
 	public void addFriendToList(ShoppingList list, Friend friend) {
 		persistenceManager.save(list, friend);
 	}
 	
 	/**
 	 * Adds a friend to a synchronized recipe
 	 * @param friend
 	 * @param recipe
 	 */
 	public void addFriendToRecipe(Recipe recipe, Friend friend) {
 		//TODO: send the information to the server and add the friend to the list on the server
 		persistenceManager.save(recipe, friend);
 	}
 	
 	/**
 	 * Deletes a friend from a synchronized shopping list
 	 * @param list
 	 * @param friend
 	 */
 	public void removeFriendFromList(ShoppingList list, Friend friend) {
 		persistenceManager.remove(list, friend);
 	}
 	
 	/**
 	 * Deletes a friend from a synchronized recipe
 	 * @param recipe
 	 * @param friend
 	 */
 	public void removeFriendFromRecipe(Recipe recipe, Friend friend) {
 		//TODO: send the information to the server and remove the friend on the server
 		//(just the entry of the shared recipe, not the friend as a onject of course^^)
 		persistenceManager.remove(recipe, friend);
 	}
 	
 	/**
 	 * Mark a friend as app-owner (used by the Syncmanager) or adds it to the libary if called from checkPhoneBookForFriends
 	 * @param friendId
 	 */
 	public void setFriendHasApp(long friendId, boolean hasApp) {
 		if(friendId >= 0) {
 			Friend friend = getFriendFromDB(friendId);
 			if(friend != null) {
 				friend.setHasApp(hasApp);
 				persistenceManager.save(friend);
 			}
 		} else {
 			//for search all the phonebook directly add friend if he has the app
			for(Friend friend: friendsInPhoneBook) {
				if(hasApp == true && friend.getId() == friendId && checkIfDouble(friend) == null) {
					friend.setHasApp(true);
					addFriend(friend);
				}
 			}
 
 		}
 	}
 	
 	public String toString() {
 		StringBuilder result = new StringBuilder();
 		for (Friend friend : friendsList)
 			result.append(friend).append("\n");
 		return result.toString();
 	}
 	
 	/**
 	 * Gets all Contacts from phone book and checks if someone has the app and adds him to the friendslibrary
 	 */
 	public void checkPhoneBookForFriends(BaseActivity activity, Handler handler) {
 		friendsInPhoneBook = new ArrayList<Friend>();
 		getListWithFriendsFromPhoneBook(friendsInPhoneBook);
         
 		for(Friend friend: friendsInPhoneBook) {
 			FriendRequest fr = new FriendRequest(friend);
 			syncManager.addRequest(fr);
 		}
 		syncManager.synchroniseAndWaitForTaskToEnd(activity);
 		handler.sendEmptyMessage(0);
 	}
 
 
 	private void getListWithFriendsFromPhoneBook(ArrayList<Friend> friendsInPhoneBook) {
 		ContentResolver cr = baseActivity.getContentResolver();
         Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
         long idCounter = -1;
         
         if (cur.getCount() > 0) {
             while (cur.moveToNext()) {
                   String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                   String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                   
                   if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
 	                     Cursor pCur = cr.query(
 	                               ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
 	                               ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
 	                               new String[]{id}, null);
 	                     while (pCur.moveToNext()) {
 	                         String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
 	                         Friend friend = new Friend(phoneNo, name);
 	                         friend.setId(idCounter--);
 	                         friendsInPhoneBook.add(friend);
 	                     }
 	                     pCur.close();
                   }
             }			
         }
 	}
 }
