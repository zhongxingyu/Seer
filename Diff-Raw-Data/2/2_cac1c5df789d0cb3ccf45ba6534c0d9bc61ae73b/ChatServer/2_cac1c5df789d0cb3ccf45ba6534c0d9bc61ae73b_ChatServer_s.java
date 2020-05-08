 package edu.berkeley.cs.cs162;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 /**
  * This is the core of the chat server.  Put the management of groups
  * and users in here.  You will need to control all of the threads,
  * and respond to requests from the test harness.
  *
  * It must implement the ChatServerInterface Interface, and you should
  * not modify that interface; it is necessary for testing.
  */
 
 public class ChatServer extends Thread implements ChatServerInterface {
 
 	private Map<String, User> users;
 	private Map<String, ChatGroup> groups;
 	private Set<String> allNames;
 	private ReentrantReadWriteLock lock;
 	private volatile boolean isDown;
 	private final static int MAX_USERS = 100;
 	
 	public ChatServer() {
 		users = new HashMap<String, User>();
 		groups = new HashMap<String, ChatGroup>();
 		allNames = new HashSet<String>();
 		lock = new ReentrantReadWriteLock(true);
 		isDown = false;
 	}
 	
 	@Override
 	public BaseUser getUser(String username) {
 		BaseUser u;
 		lock.readLock().lock();
 		u = users.get(username);
 		lock.readLock().unlock();
 		return u;
 	}
 	
 	public ChatGroup getGroup(String groupname) {
 		ChatGroup group;
 		lock.readLock().lock();
 		group = groups.get(groupname);
 		lock.readLock().unlock();
 		return group;
 	}
 	
 	public Set<String> getGroups() {
 		Set<String> groupNames;
 		lock.readLock().lock();
 		groupNames = this.groups.keySet();
 		lock.readLock().unlock();
 		return groupNames;
 	}
 	
 	public Set<String> getUsers() {
 		Set<String> userNames;
 		lock.readLock().lock();
 		userNames = users.keySet();
 		lock.readLock().unlock();
 		return userNames;
 	}
 	
 	public int getNumUsers(){
 		int num;
 		lock.readLock().lock();
 		num = users.size();
 		lock.readLock().unlock();
 		return num;
 	}
 	
 	public int getNumGroups(){
 		int num;
 		lock.readLock().lock();
 		num = groups.size();
 		lock.readLock().unlock();
 		return num;
 	}
 	
 	@Override
 	public LoginError login(String username) {
 		lock.writeLock().lock();
 		if(isDown){
 			TestChatServer.logUserLoginFailed(username, new Date(), LoginError.USER_REJECTED);
 			lock.writeLock().unlock();
 			return LoginError.USER_REJECTED;
 		}
 		if (allNames.contains(username)) {
 			lock.writeLock().unlock();
 			TestChatServer.logUserLoginFailed(username, new Date(), LoginError.USER_REJECTED);
 			return LoginError.USER_REJECTED;
 		}
 		if (users.size() >= MAX_USERS) {
 			lock.writeLock().unlock();
 			TestChatServer.logUserLoginFailed(username, new Date(), LoginError.USER_DROPPED);
 			return LoginError.USER_DROPPED;
 		}
 		User newUser = new User(this, username);
 		users.put(username, newUser);
 		allNames.add(username);
 		newUser.connected();
 		lock.writeLock().unlock();
 		TestChatServer.logUserLogin(username, new Date());
 		return LoginError.USER_ACCEPTED;
 	}
 
 	@Override
 	public boolean logoff(String username) {
 		// TODO Auto-generated method stub
 		lock.writeLock().lock();
 		if(!users.containsKey(username)){
 			lock.writeLock().unlock();
 			return false;
 		}
 		List <String> userGroups = users.get(username).getUserGroups();
 		Iterator<String> it = userGroups.iterator();
 		while(it.hasNext()){
 			groups.get(it.next()).leaveGroup(username);
 		}
 		users.get(username).logoff();
 		allNames.remove(username);
 		users.remove(username);
 		lock.writeLock().unlock();	
 		return true;
 	}
 
 	@Override
 	public boolean joinGroup(BaseUser baseUser, String groupname) {
 		// TODO Auto-generated method stub
 		lock.writeLock().lock();
 		ChatGroup group;
 		User user = (User) baseUser;
 		boolean success = false;
 		if(groups.containsKey(groupname)) {
 			group = groups.get(groupname);
 			success = group.joinGroup(user.getUsername(), user);
			user.addToGroups("groupname");
 			TestChatServer.logUserJoinGroup(groupname, user.getUsername(), new Date());
 			lock.writeLock().unlock();
 			return success;
 		}
 		else {
 			if(allNames.contains(groupname)){
 				lock.writeLock().unlock();
 				return false;
 			}
 			group = new ChatGroup(groupname);
 			groups.put(groupname, group);
 			success = group.joinGroup(user.getUsername(), user);
 			user.addToGroups("groupname");
 			TestChatServer.logUserJoinGroup(groupname, user.getUsername(), new Date());
 			lock.writeLock().unlock();
 			return success;
 		}
 	}
 
 	@Override
 	public boolean leaveGroup(BaseUser baseUser, String groupname) {
 		// TODO Auto-generated method stub
 		User user = (User) baseUser;
 		lock.writeLock().lock();
 		ChatGroup group = groups.get(groupname);
 		if (group == null){
 			lock.writeLock().unlock();
 			return false;
 		}
 		if(group.leaveGroup(user.getUsername())) {
 			if(group.getNumUsers() <= 0) { groups.remove(groupname); }
 			user.removeFromGroups(groupname);
 			TestChatServer.logUserLeaveGroup(groupname, user.getUsername(), new Date());
 			lock.writeLock().unlock();
 			return true;
 		}
 		lock.writeLock().unlock();
 		return false;
 	}
 
 	@Override
 	public void shutdown() {
 		lock.writeLock().lock();
 		Set<String> userNames = users.keySet();
 		for(String name: userNames){
 			users.get(name).logoff();
 		}
 		users.clear();
 		groups.clear();
 		isDown = true;
 		lock.writeLock().unlock();
 	}
 
 	public MsgSendError processMessage(String source, String dest, String msg, int sqn, String timestamp) {	
 		Message message = new Message(timestamp, source, dest, msg);
 		message.setSQN(sqn);
 		lock.readLock().lock();
 		if (users.containsKey(source)) {
 			if (users.containsKey(dest)) {
 				User destUser = users.get(dest);
 				destUser.acceptMsg(message);
 			} else if (groups.containsKey(dest)) {
 				message.setIsFromGroup();
 				ChatGroup group = groups.get(dest);
 				if (!group.forwardMessage(message)) {
 					TestChatServer.logChatServerDropMsg(message.toString(), new Date());
 					lock.readLock().unlock();
 					return MsgSendError.NOT_IN_GROUP;
 				}
 				
 			} else {
 				TestChatServer.logChatServerDropMsg(message.toString(), new Date());
 				lock.readLock().unlock();
 				return MsgSendError.INVALID_DEST;
 			}
 			
 		} else {
 			TestChatServer.logChatServerDropMsg(message.toString(), new Date());
 			lock.readLock().unlock();
 			return MsgSendError.INVALID_SOURCE;
 		}
 		
 		lock.readLock().unlock();
 		return MsgSendError.MESSAGE_SENT;
 	}
 	
 	@Override
 	public void run(){
 		System.out.println("Server started.");
 		while(!isDown){
 		}
 		System.out.println("Server down.");
 	}
 }
