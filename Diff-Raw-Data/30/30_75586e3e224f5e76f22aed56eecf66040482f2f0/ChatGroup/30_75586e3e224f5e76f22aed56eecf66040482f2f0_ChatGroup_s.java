 package edu.berkeley.cs.cs162;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 
 public class ChatGroup {
 	private String name;
 	private HashMap<String, User> userlist;
 	private final static int MAX_USERS = 10;
 	
 	ChatGroup(String initname) {
 		name = initname;
 		userlist = new HashMap<String, User>();
 	}
 	
 	public HashMap<String, User> getUserList() {
 		return userlist;
 	}
 	
 	public int getNumUsers() {
 		return userlist.size();
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public boolean onCreate() {
 		return true;
 	}
 	
 	public boolean onDelete() {
 		return true;
 	}
 	
 	public boolean joinGroup(String user, User userObj) {
 		if(userlist.containsKey(user))			//user already in group
 			return false;
 		if(userlist.size() + 1 > MAX_USERS)		//adding user would exceed capacity
 			return false;
 		userlist.put(user, userObj);			//add user to hashmap
 		return true;
 	}
 	
 	public boolean leaveGroup(String user) {
 		if(!userlist.containsKey(user))			//user was not registered with group
 			return false;
 		userlist.remove(user);					//remove user from hashmap
 		return true;
 	}
 	
 	public synchronized boolean forwardMessage(Message msg) {
		if (! userlist.containsValue(msg.getSource()))
 			return false;
 		Collection<User> users = userlist.values();
 		Iterator<User> it = users.iterator();
 		User user;
 		while(it.hasNext()) {
 			user = it.next();
 			user.enqueueMsg(msg);
 		}
 		return true;
 	}
 }
