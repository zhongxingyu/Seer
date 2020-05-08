 package es.udc.santiago.model;
 	import java.util.concurrent.ConcurrentHashMap;
 
 import es.udc.santiago.ClientHandler;
 
 /**
  * This singleton will store some data about users.
  * @author Santiago Munín González
  *
  */
 public class DataStore {
 	private ConcurrentHashMap<String, ClientHandler> nickList;
 	private static DataStore instance;
 	
 	private DataStore() {
 		this.nickList = new ConcurrentHashMap<String, ClientHandler>();
 	}
 	
 	public static DataStore getInstance() {
 		if (instance == null) {
 			instance = new DataStore();
 		}
 		return instance;
 	}
 	/**
 	 * Checks if nick list already contains given string.
 	 * @param nick
 	 * @return
 	 */
 	public boolean existNick(String nick) {
 		return this.nickList.containsKey(nick);
 	}
 	/**
 	 * Removes, if exists, the given nick from the nick list.
 	 * @param nick
 	 */
 	public void removeNick(String nick) {
 		if (this.nickList.containsKey(nick)) {
 			this.nickList.remove(nick);
 		}
 	}
 	/**
 	 * Adds a nick.
 	 * @param nick
 	 * @param socket
 	 * @return
 	 */
 	public boolean addNick(String nick, ClientHandler cHandler) {
 		if (this.existNick(nick)) {
 			return false;
 		}
 		this.nickList.put(nick, cHandler);
 		return true;
 	}
 	
 	/**
 	 * Changes a client's nick.
 	 * @param nick
 	 * @param newNick
 	 * @return
 	 */
 	public boolean changeNick(String nick, String newNick) {
		if (nickList.contains(nick)) {
			if (!nickList.contains(newNick)) {
				ClientHandler cH = this.nickList.get(nick);
				this.nickList.remove(nick);
				this.nickList.put(newNick, cH);
				return true;
			}
			else {
				return false;
			}
 		} else {
 			return false;
 		}
 	}
 	/**
 	 *
 	 * @return List of clients.
 	 */
 	public ConcurrentHashMap<String, ClientHandler> getAllClients() {
 		return this.nickList;
 	}
 }
