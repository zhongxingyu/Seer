 package ch.unibe.ese.shopnote.core;
 
 import java.util.Collections;
 import java.util.List;
 
 public class FriendsManager {
 	private List<Friend> friendsList;
 	private PersistenceManager persistenceManager;
 
 	public FriendsManager(PersistenceManager persistenceManager) {
 		this.persistenceManager = persistenceManager;
 		friendsList = persistenceManager.getFriends();
 	}
 
 	/**
 	 * Checks if friend is on the server and adds him to the friendlist when
 	 * successful
 	 * 
 	 * @param phoneNr
 	 * @param name
 	 * @return id of friend in long, -1 when failure 
 	 */
 	public long addFriend(Friend friend) {
 		long id = checkIfDouble(friend);
 		if (id >= 0) return id;
 		
 		// Add the friend
 		friendsList.add(friend);
 
 		// Save friend to database
 		return persistenceManager.save(friend);
 	}
 
 	private long checkIfDouble(Friend friend) {
 		for(Friend compare: friendsList) 
 			if(compare.getPhoneNr().equals(friend.getPhoneNr())) return compare.getId();
 		return -1l;
 	}
 
 	/**
 	 * @return List of all added friends, unmodifiable
 	 */
 	public List<Friend> getFriendsList() {
 		Collections.sort(friendsList, Comparators.FRIEND_COMPARATOR);
 		return Collections.unmodifiableList(friendsList);
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
 		persistenceManager.removeFriend(friend);
 		return friendsList.remove(friend);
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
 	
 	public List<Friend> getSharedFriends(ShoppingList list) {
 		return persistenceManager.getSharedFriends(list);
 	}
 	
 	/**
 	 * Adds a friend to a synchronized shopping list
 	 * @param friend
 	 * @param list
 	 */
 	public void addFriendToList(ShoppingList list, Friend friend) {
 		//TODO: send the information to the server and add the friend to the list on the server
 		persistenceManager.save(list, friend);
 	}
 	
 	/**
 	 * Deletes a friend from a synchronized shopping list
 	 * @param list
 	 * @param friend
 	 */
 	public void removeFriendOfList(ShoppingList list, Friend friend) {
 		//TODO: send the information to the server and remove the friend on the server
 		//(just the entry of the shared Shoppinglist, not the friend as a onject of course^^)
 		persistenceManager.remove(list, friend);
 	}
 	
 	public void setFriendHasApp(long friendId) {
 		getFriend(friendId).setHasApp();
 	}
 	
 	public String toString() {
 		StringBuilder result = new StringBuilder();
 		for (Friend friend : friendsList)
 			result.append(friend).append("\n");
 		return result.toString();
 	}
 }
