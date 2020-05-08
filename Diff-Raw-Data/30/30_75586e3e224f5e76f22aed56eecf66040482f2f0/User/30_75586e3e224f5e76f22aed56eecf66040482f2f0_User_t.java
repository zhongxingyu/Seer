 package edu.berkeley.cs.cs162;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 import java.util.Set;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 public class User extends BaseUser {
 	
 	private ChatServer server;
 	private String username;
 	private List<String> groupsJoined;
 	private Map<String, ChatLog> chatlogs;
 	private Queue<Message> toRecv;
 	private Queue<MessageJob> toSend;
 	private ReentrantReadWriteLock recvLock, sendLock;
 	private int sqn;
 	private boolean loggedOff;
 	
 	public User(ChatServer server, String username) {
 		this.server = server;
 		this.username = username;
 		groupsJoined = new LinkedList<String>();
 		chatlogs = new HashMap<String, ChatLog>();
 		toRecv = new LinkedList<Message>();
 		toSend = new LinkedList<MessageJob>();
 		recvLock = new ReentrantReadWriteLock(true);
 		sendLock = new ReentrantReadWriteLock(true);
 		sqn = 0;
 	}
 	
 	public void getAllGroups() { 
 		Set<String> groups = server.getGroups();
 		// Do something with group list
 	}
 	
 	public void getAllUsers() {
 		Set<String> users = server.getUsers();
 		// Do something with users list
 	}
 	
 	public int getNumGroups() {
 		return server.getNumGroups();
 	}
 	
 	public int getNumUsers() {
 		return server.getNumUsers();
 	}
 	
 	public List<String> getUserGroups() {
 		return groupsJoined;
 	}
 	
 	public String getUsername() {
 		return username;
 	}
 	
 	public ChatLog getLog(String name){
 		if(chatlogs.containsKey(name)){
 			return chatlogs.get(name);
 		}
 		return null;
 	}
 	
 	public void send(String dest, String msg) {
 		MessageJob pair = new MessageJob(dest, msg);
 		sendLock.writeLock().lock();
 		toSend.add(pair);
 		sendLock.writeLock().unlock();
 	}
 	
 	public void enqueueMsg(Message msg) {
 		recvLock.writeLock().lock();
 		toRecv.add(msg);	
 		recvLock.writeLock().unlock();
 	}
 	
 	@Override
 	public void msgReceived(String msg) {
 		System.out.println(username + " received: " + msg);
 	}
 
 	private void logRecvMsg(Message msg) {
 		// Add to chatlog
 		ChatLog log;
 		String reference;
 		
 		if (msg.isFromGroup())
 			reference = msg.getDest();
 		else
 			reference = msg.getSource();
 
 		if (chatlogs.containsKey(reference))
 			log = chatlogs.get(reference);
 		else {
 			if (msg.isFromGroup())
 				log = new ChatLog(msg.getSource(), this, msg.getDest());
 			else
 				log = new ChatLog(msg.getSource(), this);
 			
 			chatlogs.put(reference, log);
 		}
 		
 		log.add(msg);
 	}
 	
 	public void logoff(){
 		loggedOff = true;
 	}
 	
 	public void run() {
 		while(true){
 			sendLock.writeLock().lock();
 			if(!toSend.isEmpty()) {
 				MessageJob pair = toSend.poll();
 				MsgSendError msgStatus = server.processMessage(username, pair.dest, pair.msg, sqn);
 				sqn++;
 				// Do something with message send error
 			}
 			sendLock.writeLock().unlock();
 			recvLock.writeLock().lock();
 			if(!toRecv.isEmpty()) {
 				Message msg = toRecv.poll();
 				logRecvMsg(msg);
 				if(!msg.getSource().equals(username)){ //only if not from self
 					TestChatServer.logUserMsgRecvd(username, msg.toString(), new Date());
 				}
 				msgReceived(msg.toString());
 			}
 			recvLock.writeLock().unlock();
 		}
 	}
 }
