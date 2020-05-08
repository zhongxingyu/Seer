 package edu.berkeley.cs.cs162;
 
 import java.sql.SQLException;
 import java.util.*;
 import java.util.concurrent.LinkedBlockingQueue;
 
class MessageDispatcher extends Thread {
     private UserManager userManager;
     private DatabaseManager databaseManager;
     private PeerServerManager peerServerManager;
     private LinkedBlockingQueue<Message> messages;
     private boolean shuttingDown;
 
     MessageDispatcher(ChatServer chatServer) {
         this.userManager = chatServer.getUserManager();
         this.databaseManager = userManager.getDatabaseManager();
         this.peerServerManager = chatServer.getPeerServerManager();
         this.messages = new LinkedBlockingQueue<Message>();
         this.shuttingDown = false;
     }
 
     public void shutdown() {
         shuttingDown = true;
         interrupt();
     }
 
     public ChatServerResponse enqueue(Message message) {
         if (message.sender.getUserName().equals(message.receiver)) {
             return new ChatServerResponse(ResponseType.RECEIVER_SAME_AS_SENDER);
         }
         if (userManager.hasLocalUser(message.sender.getUserName())) {
             try {
                 HashMap<String, Object> receiver = databaseManager.getUser(message.receiver);
                 TreeSet<String> groupUsers = databaseManager.getGroupUserList(message.receiver);
                 if (receiver != null) {
                     message.receivingUsers = new TreeSet<String>();
                     message.receivingUsers.add(message.receiver);
                     routeMessage(message);
                     return new ChatServerResponse(ResponseType.MESSAGE_ENQUEUED);
                 } else if (groupUsers != null) {
                     if (groupUsers.contains(message.sender.getUserName())) {
                         message.receivingUsers = groupUsers;
                         routeMessage(message);
                         return new ChatServerResponse(ResponseType.MESSAGE_ENQUEUED);
                     } else {
                         return new ChatServerResponse(ResponseType.USER_NOT_MEMBER_OF_GROUP);
                     }
                 } else {
                     return new ChatServerResponse(ResponseType.RECEIVER_NOT_FOUND);
                 }
             } catch (SQLException e) {
                 return new ChatServerResponse(ResponseType.DATABASE_FAILURE);
             }
         } else {
             return new ChatServerResponse(ResponseType.SENDER_NOT_FOUND);
         }
     }
 
     private void routeMessage(Message message) {
         HashSet<PeerServer> servers = peerServerManager.getServers();
         HashMap<String, TreeSet<String>> routingList = new HashMap<String, TreeSet<String>>();
         routingList.put(peerServerManager.getServerName(), new TreeSet<String>());
         for (PeerServer server : servers) {
             routingList.put(server.getServerName(), new TreeSet<String>());
         }
         for (String user : message.receivingUsers) {
             TreeSet<String> bucket = routingList.get(peerServerManager.findUser(user));
             if (bucket != null) {
                 bucket.add(user);
             }
         }
 
         TreeSet<String> localReceivers = routingList.get(peerServerManager.getServerName());
         if (!localReceivers.isEmpty()) {
             message.receivingUsers = localReceivers;
             messages.offer(message);
         }
 
         TreeSet<String> remoteUsers;
         for (PeerServer server : servers) {
             remoteUsers = routingList.get(server.getServerName());
             if (!remoteUsers.isEmpty()) {
                 server.sendMessage(message.senderName, message.receiver, remoteUsers, message.date, message.sqn, message.text);
             }
         }
     }
 
     public void deliver(Message message) {
         boolean hasFailed = false;
         for (String receivingUserName : message.receivingUsers) {
             try {
                 ChatUser receivingUser = userManager.getLocalUser(receivingUserName);
                 HashMap<String, Object> databaseUser = databaseManager.getUser(receivingUserName);
                 if (receivingUser != null) {
                     receivingUser.receiveMessage(message);
                 } else if (databaseUser != null) {
                     if (!(Boolean)databaseUser.get("logged_in")) {
                         databaseManager.logMessage(receivingUserName, message);
                     }
                 } else {
                     hasFailed = true;
                 }
             } catch (SQLException e) {
                 hasFailed = true;
             }
         }
         if (hasFailed) {
             if (message.sender != null) {
                 message.sender.receiveSendFailure(message);
             }
         }
     }
 
     public void deliverOfflineMessages(ChatUser user) {
         try {
             LinkedList<HashMap<String, Object>> messages = databaseManager.getOfflineMessages(user.getUserName());
             HashMap<String, Object> m = messages.poll();
             while (m != null) {
                 Message message = new Message((Date)m.get("timestamp"), (String)m.get("sender"), (String)m.get("receiver"), (Integer)m.get("sqn"), (String)m.get("text"));
                 user.receiveMessage(message);
                 m = messages.poll();
             }
         } catch (SQLException e) {}
     }
 
     public void run() {
         while (!shuttingDown || messages.size() > 0) {
             try {
                 deliver(messages.take());
             } catch (InterruptedException e) {
             }
         }
     }
 }
