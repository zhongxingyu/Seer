 package fr.utc.nf28.moka.environment;
 
 import fr.utc.nf28.moka.BuildConfig;
 import fr.utc.nf28.moka.environment.items.MokaItem;
 import fr.utc.nf28.moka.environment.users.User;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * A class that holds all the models of the running Moka platform
  * All MokaAgents have a reference to their MokaEnvironment
  */
 public final class MokaEnvironment {
 	private static MokaEnvironment sInstance = null;
	private static int sItemIdGenCurrentIndex = 0;
 	private final LinkedList<HistoryEntry> mHistoryEntries = new LinkedList<HistoryEntry>();
 	private HashMap<String, User> mUsers = new HashMap<String, User>();
 	private HashMap<Integer, MokaItem> mItems = new HashMap<Integer, MokaItem>();
 
 	private MokaEnvironment() {
 	}
 
 	public static MokaEnvironment getInstance() {
 		if (sInstance == null) {
 			sInstance = new MokaEnvironment();
 		}
 		return sInstance;
 	}
 
 	public int generateNewId() {
 		return sItemIdGenCurrentIndex++;
 	}
 
 	public void setItemIdGenCurrentIndex(int index) {
 		sItemIdGenCurrentIndex = index;
 	}
 
 	public void clearItems() {
 		mItems.clear();
 	}
 
 	public void clearHistory() {
 		mHistoryEntries.clear();
 	}
 
 	public void addHistoryEntry(HistoryEntry historyEntry) {
 		mHistoryEntries.addFirst(historyEntry);
 	}
 
 	public void addItem(MokaItem item) {
 		final int id = item.getId();
 		if (mItems.put(id, item) == null) {
 			if (BuildConfig.DEBUG) System.out.println("item with id " + id + " added");
 		} else {
 			if (BuildConfig.DEBUG) System.out.println("item with id " + id + " replaced");
 		}
 		final User locker = item.getLocker();
 		if (locker != null) {
 			addHistoryEntry(new HistoryEntry(locker.makePseudo() + " a ajouté " + item.getType() + " " + id));
 		}
 		if (BuildConfig.DEBUG) System.out.println(toString());
 	}
 
 	/**
 	 * use to unlock item
 	 *
 	 * @param itemId item id
 	 */
 	public void unlockItem(int itemId) {
 		final MokaItem item = mItems.get(itemId);
 		if (item != null) {
 			item.unlock();
 			if (BuildConfig.DEBUG) System.out.println("item  " + item.getId() + " unlock.");
 		}
 	}
 
 	public User lockItem(int itemId, String userAID) {
 		final MokaItem item = mItems.get(itemId);
 		if (item == null) {
 			return null;
 		}
 		if (!item.isLocked()) {
 			item.lock(getUserByAID(userAID));
 			if (BuildConfig.DEBUG) System.out.println(item.getType() + " " + item.getId() + " locked by " + item.getLocker().makePseudo());
 		} else {
 			if (BuildConfig.DEBUG) System.out.println(item.getType() + " " + item.getId() + " already locked by " + item.getLocker().makePseudo());
 		}
 		return item.getLocker();
 	}
 
 	public void addUser(User user) {
 		final String ip = user.getIp();
 		if (mUsers.put(ip, user) == null) {
 			if (BuildConfig.DEBUG) System.out.println("user with ip " + ip + " added");
 		} else {
 			if (BuildConfig.DEBUG) System.out.println("user with ip " + ip + " replaced");
 		}
 		addHistoryEntry(new HistoryEntry(user.makePseudo() + " s'est connecté"));
 		if (BuildConfig.DEBUG) System.out.println(toString());
 	}
 
 	public HashMap<String, User> getUsers() {
 		return mUsers;
 	}
 
 	public User getUserByAID(String userAID) {
 		User found = null;
 		for (User u : mUsers.values()) {
 			if (u.getAID().equals(userAID)) {
 				found = u;
 				break;
 			}
 		}
 		return found;
 	}
 
 	public void removeItem(int itemId) {
 		final MokaItem item = mItems.get(itemId);
 		if (mItems.remove(itemId) == null) {
 			if (BuildConfig.DEBUG) System.out.println("no item with id " + itemId);
 		} else {
 			if (BuildConfig.DEBUG) System.out.println("Item " + itemId + " removed");
 			addHistoryEntry(new HistoryEntry(item.getLocker().makePseudo()
 					+ " a supprimé " + item.getType() + " " + item.getId()));
 		}
 		if (BuildConfig.DEBUG) System.out.println(toString());
 	}
 
 	public void removeUser(String ip) {
 		if (mUsers.remove(ip) == null) {
 			if (BuildConfig.DEBUG) System.out.println("no user with ip " + ip);
 		} else {
 			if (BuildConfig.DEBUG) System.out.println("User " + ip + " removed");
 			addHistoryEntry(new HistoryEntry("Un utilisateur s'est déconnecté"));
 		}
 		if (BuildConfig.DEBUG) System.out.println(toString());
 	}
 
 	public MokaItem moveItem(int itemId, int direction, int velocity) {
 		//TODO implement getById !
 		final MokaItem res = mItems.get(itemId);
 		final int dd = (5 * velocity);
 		if (res != null && dd < 100) {
 			if (direction % 10 == 1) {
 				res.setX(res.getX() + dd);
 			} else if (direction % 10 == 2) {
 				res.setX(res.getX() - dd);
 			}
 
 			if (direction >= 20) {
 				res.setY(res.getY() - dd);
 			} else if (direction >= 10) {
 				res.setY(res.getY() + dd);
 			}
 		}
 
 		return res;
 	}
 
 	public MokaItem rotateItem(int itemId, int direction) {
 		final MokaItem res = mItems.get(itemId);
 		final int ddZ = 10;
 		final int ddXY = 20;
 		if (res != null) {
 			if (direction == 100) {
 				res.setRotateZ(res.getRotateZ() - ddZ);
 			} else if (direction == 200) {
 				res.setRotateZ(res.getRotateZ() + ddZ);
 			} else {
 				if (direction % 10 == 1) {
 					res.setRotateY(res.getRotateY() - ddXY);
 				} else if (direction % 10 == 2) {
 					res.setRotateY(res.getRotateY() + ddXY);
 				}
 
 				if (direction >= 20) {
 					res.setRotateX(res.getRotateX() + ddXY);
 				} else if (direction >= 10) {
 					res.setRotateX(res.getRotateX() - ddXY);
 				}
 			}
 		}
 		return res;
 	}
 
 	public MokaItem resizeItem(int itemId, int direction) {
 		final MokaItem res = mItems.get(itemId);
 		final int dd = 30;
 		if (res != null) {
 			if (direction % 10 == 1) {
 				res.setWidth(res.getWidth() + dd);
 			} else if (direction % 10 == 2) {
 				res.setWidth(res.getWidth() - dd);
 			}
 
 			if (direction >= 20) {
 				res.setHeight(res.getHeight() - dd);
 			} else if (direction >= 10) {
 				res.setHeight(res.getHeight() + dd);
 			}
 		}
 
 		return res;
 	}
 
 	public MokaItem editItem(int itemId, String field, String newValue) {
 		final MokaItem res = mItems.get(itemId);
 		res.update(field, newValue);
 		return res;
 	}
 
 	public void updateItem(MokaItem newValue) {
 		// TODO correctly implement udpate
 	}
 
 	public List<HistoryEntry> getHistory() {
 		return mHistoryEntries;
 	}
 
 	public HashMap<Integer, MokaItem> getItems() {
 		return mItems;
 	}
 
 	public String toString() {
 		String s = "";
 		s += "== Users ==\n";
 		for (User u : mUsers.values()) {
 			s += u.toString() + "\n";
 		}
 		s += "== Items ==\n";
 		for (MokaItem mi : mItems.values()) {
 			s += mi.toString() + "\n";
 		}
 		return s;
 	}
 
 }
