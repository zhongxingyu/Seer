 /**
  * Simple in memory database for chat server
  *
  * Thread safe but not optimal - still experimental
  */
 
 package server;
 
 import java.util.*;
 
 public class Storage {
 
 	private static Storage instance = null;
 
 	public static Storage getInstance() {
 		if(instance == null) {
 			instance = new Storage();
 		}
 		return instance;
 	}
 
 
 	private int sessionIdCounter = 0;
 	private int messageIdCounter = 0;
 
 	private List<Message> messages = new LinkedList<Message>();
 	private Map<Integer, String> sessions = new HashMap<Integer, String>();
 
 	private synchronized int generateMessageId() {
 		return ++messageIdCounter;
 	}
 
 	private synchronized int generateSessionId() {
 		return ++sessionIdCounter;
 	}
 
 	public synchronized boolean putMessage(int sid, String msg) {
 		if(!validSessionId(sid)) {
 			return false;
 		}
 		int id = generateMessageId();
 		messages.add(new Message(id, sessions.get(sid), msg));
 		return true;
 	}
 
 	public synchronized List<Message> getMessages() {
 		return messages;
 	}
 
 	public synchronized List<Message> getMessages(int minId) {
 		List<Message> ret = new LinkedList<Message>();
 		for(Message m : messages) {
			if(m.getId() > minId) {
 				ret.add(m);
 			}
 		}
 		return ret;
 	}
 
 	public synchronized int startSession(String name) {
 		if(sessions.containsValue(name)) {
 			return 0;
 		}
 		int id = generateSessionId();
 		sessions.put(id, name);
 		return id;
 	}
 
 	public synchronized boolean endSession(int id) {
 		sessions.remove(id);
 		return true;
 	}
 
 	public synchronized boolean validSessionId(int id) {
 		return sessions.containsKey(id);
 	}
 
 	public synchronized List<String> getUsers() {
 		List<String> ret = new LinkedList<String>();
 		for(String name : sessions.values()) {
 			ret.add(name);
 		}
 		return ret;
 	}
 
 }
 
