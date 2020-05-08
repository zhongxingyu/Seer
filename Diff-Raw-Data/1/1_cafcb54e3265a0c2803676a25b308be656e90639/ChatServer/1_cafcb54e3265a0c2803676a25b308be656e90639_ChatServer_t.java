 package edu.berkeley.cs.cs162;
 
 
 import java.io.EOFException;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 
 /**
  * This is the core of the chat server.  Put the management of groups
  * and users in here.  You will need to control all of the threads,
  * and respond to requests from the test harness.
  *
  * It must implement the ChatServerInterface Interface, and you should
  * not modify that interface; it is necessary for testing.
  */
 
 public class ChatServer extends Thread implements ChatServerInterface {
 
 	private BlockingQueue<String> waiting_users;
 	private Map<String, User> users;
 	private Map<String, ChatGroup> groups;
 	private Set<String> allNames;
 	private ReentrantReadWriteLock lock;
 	private volatile boolean isDown;
 	private final static int MAX_USERS = 100;
 	private final static int MAX_WAITING_USERS = 10;
 	private final static long TIMEOUT = 5;
 	private ServerSocket mySocket;
 	private ExecutorService pool;
 	private Map<String, SocketParams> waiting_sockets;
 	
 	public ChatServer() {
 		users = new HashMap<String, User>();
 		groups = new HashMap<String, ChatGroup>();
 		allNames = new HashSet<String>();
 		lock = new ReentrantReadWriteLock(true);
 		waiting_users = new ArrayBlockingQueue<String>(MAX_WAITING_USERS);
 		isDown = false;
 		
 	}
 	
 	public ChatServer(int port) throws IOException {
 		users = new HashMap<String, User>();
 		groups = new HashMap<String, ChatGroup>();
 		allNames = new HashSet<String>();
 		lock = new ReentrantReadWriteLock(true);
 		waiting_users = new ArrayBlockingQueue<String>(MAX_WAITING_USERS);
 		isDown = false;
 		pool = Executors.newFixedThreadPool(100);
 		try {
 			mySocket = new ServerSocket(port);
 		} catch (Exception e) {
 			throw new IOException("Server socket creation failed");
 		}
 		waiting_sockets = new ConcurrentHashMap<String, SocketParams>();
 		this.start();
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
 		if (users.size() >= MAX_USERS) {		//exceeds capacity
 			lock.writeLock().unlock();
 			if(waiting_users.offer(username))	//attempt to add to waiting queue
 				return LoginError.USER_QUEUED;
 			else {								//else drop user
 				TestChatServer.logUserLoginFailed(username, new Date(), LoginError.USER_DROPPED);
 				return LoginError.USER_DROPPED;				
 			}
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
 			ChatGroup group = groups.get(it.next());
 			if(group.leaveGroup(username)){
 				if(group.getNumUsers() <= 0) { 
 					groups.remove(group.getName()); 
 					allNames.remove(group.getName());
 				}
 			}
 		}
 		users.get(username).logoff();
 		allNames.remove(username);
 		users.remove(username);
 		
 		// Check for waiting users
 		String uname = waiting_users.poll();
 		if(uname != null) {							//add to ChatServer
 			User newUser = new User(this, uname);
 			SocketParams socket = waiting_sockets.get(uname);
 			newUser.setSocket(socket.getMySocket(), socket.getInputStream(), socket.getOutputStream());
 			waiting_sockets.remove(uname);
 			users.put(uname, newUser);
 			allNames.add(uname);
 			TransportObject reply = new TransportObject(Command.login, ServerReply.OK);
 			newUser.queueReply(reply);
 			newUser.connected();
 			TestChatServer.logUserLogin(uname, new Date());
 		}
 		
 		lock.writeLock().unlock();	
 		return true;
 	}
 	
 	public void joinAck(User user, String gname, ServerReply reply) {
 		TransportObject toSend = new TransportObject(Command.join,gname,reply);
 		user.queueReply(toSend);
 	}
 	
 	public void leaveAck(User user, String gname, ServerReply reply) {
 		TransportObject toSend = new TransportObject(Command.leave,gname,reply);
 		user.queueReply(toSend);
 	}
 
 	public void startNewTimer(SocketParams params) throws IOException {
 		List<Handler> task = new ArrayList<Handler>();
 		try {
 			task.add(new Handler(params));
 			ObjectOutputStream sent = params.getOutputStream();
 			List<Future<Handler>> futures = pool.invokeAll(task, TIMEOUT, TimeUnit.SECONDS);
 			if (futures.get(0).isCancelled()) {
 				
 				TransportObject sendObject = new TransportObject(ServerReply.timeout);
 				sent.writeObject(sendObject);
 			}
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 		@Override
 	public boolean joinGroup(BaseUser baseUser, String groupname) {
 		// TODO Auto-generated method stub
 		lock.writeLock().lock();
 		ChatGroup group;
 		User user = (User) baseUser;
 		boolean success = false;
 		if (!users.keySet().contains(user.getUsername())) {
 			lock.writeLock().unlock();
 			return false;
 		}
 		if(groups.containsKey(groupname)) {
 			group = groups.get(groupname);
 			success = group.joinGroup(user.getUsername(), user);
 			if(user.getAllGroups().contains(groupname)){
 				joinAck(user,groupname,ServerReply.ALREADY_MEMBER);
 				return false;
 			}
 			user.addToGroups(groupname);
 			TestChatServer.logUserJoinGroup(groupname, user.getUsername(), new Date());
 			if(success)
 				joinAck(user,groupname,ServerReply.OK_JOIN);
 			else
 				joinAck(user,groupname,ServerReply.FAIL_FULL);
 			lock.writeLock().unlock();
 			return success;
 		}
 		else {
 			if(allNames.contains(groupname)){
 				joinAck(user,groupname,ServerReply.BAD_GROUP);
 				lock.writeLock().unlock();
 				return false;
 			}
 			group = new ChatGroup(groupname);
 			groups.put(groupname, group);
 			success = group.joinGroup(user.getUsername(), user);
 			user.addToGroups(groupname);
 			TestChatServer.logUserJoinGroup(groupname, user.getUsername(), new Date());
 			if(success)
 				joinAck(user,groupname,ServerReply.OK_CREATE);
 			else
 				System.out.println("why can't i create?");
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
 			leaveAck(user,groupname,ServerReply.BAD_GROUP);
 			lock.writeLock().unlock();
 			return false;
 		}
 		if(group.leaveGroup(user.getUsername())) {
 			leaveAck(user,groupname,ServerReply.OK);
 			if(group.getNumUsers() <= 0) { 
 				groups.remove(group.getName()); 
 				allNames.remove(group.getName());
 			}
 			user.removeFromGroups(groupname);
 			TestChatServer.logUserLeaveGroup(groupname, user.getUsername(), new Date());
 			lock.writeLock().unlock();
 			return true;
 		}
 		else {
 			leaveAck(user,groupname,ServerReply.NOT_MEMBER);
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
 				MsgSendError sendError = group.forwardMessage(message);
 				if (sendError==MsgSendError.NOT_IN_GROUP) {
 					TestChatServer.logChatServerDropMsg(message.toString(), new Date());
 					lock.readLock().unlock();
 					return sendError;
 				} else if(sendError==MsgSendError.MESSAGE_FAILED)
 					return sendError;
 				
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
 		while(!isDown){
 			List<Handler> task = new ArrayList<Handler>();
 			Socket newSocket;
 			try {
 				newSocket = mySocket.accept();
 				Handler handler = new Handler(newSocket);
 				task.add(handler);
 				System.out.println("new socket request received");
 				List<Future<Handler>> futures = pool.invokeAll(task, TIMEOUT, TimeUnit.SECONDS);
 				if (futures.get(0).isCancelled()) {
 					ObjectOutputStream sent = handler.sent;
 					//ObjectInputStream received = new ObjectInputStream(newSocket.getInputStream());
 					
 					TransportObject sendObject = new TransportObject(ServerReply.timeout);
 					sent.writeObject(sendObject);
 					System.out.println("client timing out");
 				}
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 	
 	class Handler implements Callable<ChatServer.Handler>, Runnable {
 		private final Socket socket;
 		    Handler(Socket socket) throws IOException { 
 		    	this.socket = socket;
 		    	received = new ObjectInputStream(socket.getInputStream());
 				sent = new ObjectOutputStream(socket.getOutputStream());
 		    }
 		    
 		    Handler(SocketParams params) {
 		    	this.socket = params.getMySocket();
 		    	received = params.getInputStream();
 		    	sent = params.getOutputStream();
 		    }
 		    private ObjectInputStream received;
 			private ObjectOutputStream sent;
 		    public void run() {
 		    		
 		    }
 			@Override
 			public Handler call() throws Exception {
 				System.out.println("starting run method of new handler");
 		    	TransportObject recObject = null;
 		    	while(recObject == null) {
 			    	System.out.println("polling for login command");
 					try {
 						recObject = (TransportObject) received.readObject();
 					} catch (EOFException e) {
 						System.out.println("user disconnected");
 						return null;
 					} catch (Exception e) {
 						e.printStackTrace();
 						return null;
 					}
 					if (recObject != null) {
 						
 						Command type = recObject.getCommand();
 						System.out.println("received first command " + type.toString());
 						if (type == Command.login) {
 							String username = recObject.getUsername();
 							LoginError loginError = login(username);
 							TransportObject sendObject;
 							if (loginError == LoginError.USER_ACCEPTED) {
 								sendObject = new TransportObject(Command.login, ServerReply.OK);
 								System.out.println("created new transport object");
 								User newUser = (User) getUser(username);
 								System.out.println("got new user");
 								newUser.setSocket(socket, received, sent);
 								System.out.println("just set socket on new user");
 							} else if (loginError == LoginError.USER_QUEUED) {
 								sendObject = new TransportObject(Command.login, ServerReply.QUEUED);
 								waiting_sockets.put(username, new SocketParams(socket, received, sent));
 							} else {
 								sendObject = new TransportObject(ServerReply.error);
 								recObject = null;
 							}
 							try {
 								System.out.println("sending new object woot " + sendObject);
 								sent.writeObject(sendObject);
 							} catch (IOException e) {
 								// TODO Auto-generated catch block
 								e.printStackTrace();
 							}
 							
 						}
 					}
 		    	}
 		    	return null;
 			
 			}
 	}
 	
 	public static void main(String[] args) throws Exception{
 		if (args.length != 1) {
 			throw new Exception("Invalid number of args to command");
 		}
 		int port = Integer.parseInt(args[0]);
 		ChatServer chatServer = new ChatServer(port);
 	}
 }
