 package logic;
 import java.util.ArrayList;
 import java.util.List;
 
 import requests.FriendRequest;
 import requests.xml.XMLSerializable;
 
 
 public class User extends XMLSerializable {
 
 	private boolean isOnline;
 	
 	/**
 	 * Empty constructor for 
 	 */
 	public User() {}
 	
 	public User(long ID, String username) {
 		this(ID, username, new ArrayList<User>());
 	}
 	
 	public User(String username) {
 		this(-1, username);
 	}
 	
 	public User(long ID, String username, List<User> friends) {	
 		setVariable("id", ID);
 		setVariable("username", username);
 		setVariable("pendingDebts", new ArrayList<Debt>());
 		setVariable("confirmedDebts", new ArrayList<Debt>());
 		setVariable("friendRequests", new ArrayList<FriendRequest>());
 		setVariable("friends", friends);
 	}
 	
 	/**
 	 * User identification
 	 * 
 	 * @return
 	 */
 	public long getId() {
 		return (Long) getVariable("id");
 	}
 	
 	/**
 	 * Adds the given friend request to this user
 	 * @param req	The friend request to add
 	 */
 	public synchronized void addFriendRequest(FriendRequest req) {
 		((List<FriendRequest>) getVariable("friendRequest")).add(req);
 	}
 	
 	/**
 	 * Finds the given friend request's index
 	 * @param req	The friend request
 	 * @return		The index of the given friend request, or -1 if not found
 	 */
 	public synchronized int indexOfFriendRequest(FriendRequest req) {
 		// We assume that only the user that can respond to the request has it saved 
 		if(!req.getFriendUsername().equals(this.getUsername())) return -1;
 		// Find the given request among ours
 		for (int i = 0; i < this.getNumberOfFriendRequests(); i++) {
 			if(req.getFromUser().equals(this.getFriendRequest(i).getFromUser())) {
 				return i;
 			}
 		}
 		return -1;
 	}
 	
 	/**
 	 * Checks if this user has the given friend request
 	 * @param req	The friend request to check
 	 * @return		True if this user has the given friend request, false if not
 	 */
 	public synchronized boolean hasFriendRequest(FriendRequest req) {
 		return indexOfFriendRequest(req) != -1;
 	}
 	
 	/**
 	 * Finds and returns the FriendRequest from the user with the given username
 	 * @param username	The username of the user to look for
 	 * @return			The corresponding friend request or null if none is found
 	 */
 	public synchronized FriendRequest getFriendRequestFrom(String username) {
 		for (int i = 0; i < getNumberOfFriendRequests(); i++) {
 			if(getFriendRequest(i).getFromUser().getUsername().equals(username)) {
 				return getFriendRequest(i); 
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Removes the given friend request (recognized by fromUser and friendUsername)
 	 * @param req	The friend request to remove
 	 * @return		True if the request was present, false if not
 	 */
 	public synchronized boolean removeFriendRequest(FriendRequest req) {
 		int i = indexOfFriendRequest(req);
 		if(i == -1) return false;
 		getFriendRequests().remove(i);
 		return true;
 	}
 	
 	/**
 	 * @return	This user's friend requests
 	 */
 	private synchronized List<FriendRequest> getFriendRequests() {
		return (List<FriendRequest>) getVariable("friendRequest");
 	}
 	
 	/**
 	 * @param i	The index
 	 * @return	The ith friend request
 	 */
 	public synchronized FriendRequest getFriendRequest(int i) {
 		return getFriendRequest(i);
 	}
 	
 	/**
 	 * @return	The number of pending friend requests
 	 */
 	public synchronized int getNumberOfFriendRequests() {
 		return getFriendRequests().size();
 	}
 	
 	/**
 	 * Returns a sendable version of this object (avoiding infinite loops with friends list for example).
 	 * @param fromServer	If the returned object will be sent from the server (true) or not (false). If false debts will not be included.
 	 * @return				A sendable version of this object.
 	 */
 	/*public Sendable toSendable(boolean fromServer) {
 		List<String> friendUsernames = new ArrayList<String>();
 		if(friends != null) {
 			for (User f : friends) {
 				friendUsernames.add(f.getUsername());
 			}
 		}
 		List<Debt> pd = new ArrayList<Debt>();
 		List<Debt> cd = new ArrayList<Debt>();
 		if(fromServer) {
 			for (Debt d : pendingDebts) {
 				pd.add((Debt) d.toSendable(false));
 //				pd.add(new Debt(d.getId(), d.getAmount(), d.getWhat(), from, to, comment, requestedBy))
 			}
 			for (Debt d : confirmedDebts) {
 				cd.add((Debt) d.toSendable(false));
 			}
 		}
 		return new User(username, password, friendUsernames, (fromServer ? pd : null), (fromServer ? cd : null));
 	}*/
 	
 	public synchronized List<Debt> getPendingDebts() {
 		return (List<Debt>) getVariable("pendingDebts");
 	}
 	
 	public synchronized List<Debt> getConfirmedDebts() {
 		return (List<Debt>) getVariable("confirmedDebts");
 	}
 	
 	public synchronized boolean removePendingDebt(Debt d) {
 		return getPendingDebts().remove(d);
 	}
 	
 	public synchronized int getNumberOfWaitingDebts() {
 		int c = 0;
 		for (Debt d : getPendingDebts()) {
 			if(d.getRequestedBy() != this) c++;
 		}
 		return c;
 	}
 	
 	public synchronized int getNumberOfPendingDebts() {
 		return getPendingDebts().size();
 	}
 	
 	public synchronized int getNumberOfConfirmedDebts() {
 		return getConfirmedDebts().size();
 	}
 	
 	public synchronized Debt getPendingDebt(int i) {
 		return getPendingDebts().get(i);
 	}
 	
 	public synchronized Debt getConfirmedDebt(int i) {
 		return getConfirmedDebts().get(i);
 	}
 	
 	public synchronized void addPendingDebt(Debt d) {
 		getPendingDebts().add(d);
 	}
 	
 	public synchronized void addConfirmedDebt(Debt d) {
 		getConfirmedDebts().add(d);
 	}
 	
 	public synchronized int getNumberOfTotalDebts() {
 		return getNumberOfPendingDebts() + getNumberOfConfirmedDebts();
 	}
 
 	public synchronized User getFriend(String username) {
 		for (int i = 0; i < getNumberOfFriends(); i++) {
 			if(getFriend(i).getUsername().equalsIgnoreCase(username)) return getFriend(i);
 		}
 		return null;
 	}
 	
 	private synchronized List<User> getFriends() {
 		return (List<User>) getVariable("friends");
 	}
 	
 	public synchronized int getNumberOfFriends() {
 		return getFriends().size();
 	}
 	
 	public synchronized User getFriend(int i) {
 		return getFriends().get(i);
 	}
 	
 	public String getUsername() {
 		return (String) getVariable("username");
 	}
 	
 	public boolean isOnline() {
 		return isOnline;
 	}
 	
 	public void setIsOnline(boolean isOnline) {
 		this.isOnline = isOnline;
 	}
 
 	public synchronized Debt removeConfirmedDebt(int i) {
 		return getConfirmedDebts().remove(i);
 	}
 	
 	public synchronized Debt removePendingDebt(int i) {
 		return getPendingDebts().remove(i);
 	}
 	
 	public synchronized void addFriend(User friend) {
 		if(getFriends() == null) {
 			setVariable("friends", new ArrayList<User>());
 		}
 		getFriends().add(friend);
 	}
 	
 	@Override
 	public boolean equals(Object o) {
 		if(!(o instanceof User)) return false;
 		return getUsername().equals(((User)o).getUsername());
 	}
 	
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		sb.append("Username: " + getUsername());
 		if(getFriends() != null) {
 			sb.append(", friends = [");
 			for (int i = 0; i < getFriends().size(); i++) {
 				User f = getFriends().get(i);
 				sb.append(f.toString());
 				if(i < getFriends().size() - 1) {
 					sb.append(", ");
 				}
 			}
 			sb.append("]");
 		}
 		return sb.toString();
 	}
 }
