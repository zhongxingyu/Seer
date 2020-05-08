 package edu.berkeley.cs.cs162;
 
 import java.sql.SQLException;
 import java.util.*;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 class UserManager {
     public final int maxLoggedInUsers;
     public final int maxQueuedUsers;
 
     private ChatServer chatServer;
     private DatabaseManager databaseManager;
     private HashMap<String, ChatUser> loggedInUsers;
     private LinkedList<ChatUser> loginQueue;
     private ReentrantReadWriteLock rwLock;
 
     public UserManager(ChatServer chatServer, int maxLoggedInUsers, int maxGroupUsers) {
         this.maxLoggedInUsers = maxLoggedInUsers;
         this.maxQueuedUsers = maxLoggedInUsers / 10;
         this.chatServer = chatServer;
         this.databaseManager = new DatabaseManager();
         this.loggedInUsers = new HashMap<String, ChatUser>();
         this.loginQueue = new LinkedList<ChatUser>();
         this.rwLock = new ReentrantReadWriteLock();
     }
 
     public void shutdown() {
        for (ChatUser user : loggedInUsers.values()) {
             user.shutdown();
         }
        for (ChatUser user : loggedInUsers.values()) {
             try {
                 user.join();
             } catch (InterruptedException e) {
             }
         }
         databaseManager.shutdown();
     }
 
     public DatabaseManager getDatabaseManager() {
         return databaseManager;
     }
 
     private boolean hasName(String name) throws SQLException {
         return databaseManager.getReceiver(name) != null;
     }
 
     private boolean hasUser(String userName) throws SQLException {
         return databaseManager.getUser(userName) != null;
     }
 
     public boolean hasLoggedInUser(String userName) {
         boolean result;
         rwLock.readLock().lock();
         result = loggedInUsers.containsKey(userName);
         rwLock.readLock().unlock();
         return result;
     }
 
     public ChatUser getLoggedInUser(String userName) {
         ChatUser result;
         rwLock.readLock().lock();
         result = loggedInUsers.get(userName);
         rwLock.readLock().unlock();
         return result;
     }
 
     private boolean hasGroup(String groupName) throws SQLException {
         return databaseManager.getGroup(groupName) != null;
     }
 
     public ChatServerResponse addUser(String userName, String password) {
         try {
             if (hasUser(userName)) {
                 return new ChatServerResponse(ResponseType.NAME_CONFLICT);
             } else {
                 databaseManager.addUser(userName, password);
                 return new ChatServerResponse(ResponseType.USER_ADDED);
             }
         } catch (SQLException e) {
             return new ChatServerResponse(ResponseType.DATABASE_FAILURE);
         }
     }
 
     public ChatServerResponse loginUser(ChatUser user, String password) {
         try {
             HashMap<String, Object> userEntry = databaseManager.getUser(user.getUserName());
             if (userEntry != null && ((String)userEntry.get("name")).equals(user.getUserName()) && ((String)userEntry.get("password")).equals(password)) {
                 ChatServerResponse result;
                 rwLock.writeLock().lock();
                 if (loggedInUsers.size() >= maxLoggedInUsers) {
                     if (loginQueue.size() >= maxQueuedUsers) {
                         result = new ChatServerResponse(ResponseType.USER_CAPACITY_REACHED);
                     } else {
                         if (!loginQueue.contains(user)) {loginQueue.add(user);}
                         result = new ChatServerResponse(ResponseType.USER_QUEUED);
                     }
                 } else {
                     if (loggedInUsers.get(user.getUserName()) == null) {
                         loggedInUsers.put(user.getUserName(), user);
                         result = new ChatServerResponse(ResponseType.USER_LOGGED_IN);
                     } else {
                         result = new ChatServerResponse(ResponseType.USER_ALREADY_LOGGED_IN);
                     }
                 }
                 rwLock.writeLock().unlock();
                 return result;
             } else {
                 return new ChatServerResponse(ResponseType.INVALID_NAME_OR_PASSWORD);
             }
         } catch (SQLException e) {
             return new ChatServerResponse(ResponseType.DATABASE_FAILURE);
         }
     }
 
     public ChatServerResponse logoutUser(ChatUser user) {
         ChatServerResponse result;
         rwLock.writeLock().lock();
         if (loggedInUsers.containsKey(user.getUserName())) {
             loggedInUsers.remove(user.getUserName());
             result = new ChatServerResponse(ResponseType.USER_LOGGED_OUT);
             try {
                 ChatUser queuedUser = loginQueue.remove();
                 loggedInUsers.put(queuedUser.getUserName(), queuedUser);
                 queuedUser.loggedIn();
             } catch (NoSuchElementException e) {}
         } else if (loginQueue.remove(user)) {
             result = new ChatServerResponse(ResponseType.USER_LOGGED_OUT);
         } else {
             result = new ChatServerResponse(ResponseType.USER_NOT_FOUND);
         }
         rwLock.writeLock().unlock();
         return result;
     }
 
     public ChatServerResponse addUserToGroup(ChatUser user, String groupName) {
         rwLock.readLock().lock();
         boolean loggedIn = loggedInUsers.containsKey(user.getUserName());
         rwLock.readLock().unlock();
         if (loggedIn) {
             try {
                 HashMap<String, Object> receiverEntry = databaseManager.getReceiver(groupName);
                 if (receiverEntry == null) {
                     databaseManager.addGroup(groupName);
                     databaseManager.addUserToGroup(user.getUserName(), groupName);
                     return new ChatServerResponse(ResponseType.USER_ADDED_TO_NEW_GROUP);
                 } else if (((String)receiverEntry.get("type")).equals("group")) {
                     if (databaseManager.getGroupUserList(groupName).contains(user.getUserName())) {
                         return new ChatServerResponse(ResponseType.USER_ALREADY_MEMBER_OF_GROUP);
                     } else {
                         databaseManager.addUserToGroup(user.getUserName(), groupName);
                         return new ChatServerResponse(ResponseType.USER_ADDED_TO_GROUP);
                     }
                 } else { // "user"
                     return new ChatServerResponse(ResponseType.NAME_CONFLICT);
                 }
             } catch (SQLException e) {
                 return new ChatServerResponse(ResponseType.DATABASE_FAILURE);
             }
         } else {
             return new ChatServerResponse(ResponseType.USER_NOT_LOGGED_IN);
         }
     }
 
     public ChatServerResponse removeUserFromGroup(ChatUser user, String groupName) {
         rwLock.readLock().lock();
         boolean loggedIn = loggedInUsers.containsKey(user.getUserName());
         rwLock.readLock().unlock();
         if (loggedIn) {
             try {
                 if (hasGroup(groupName)) {
                     TreeSet<String> groupUserList = databaseManager.getGroupUserList(groupName);
                     if (groupUserList.contains(user.getUserName())) {
                         databaseManager.removeUserFromGroup(user.getUserName(), groupName);
                         if (groupUserList.size() == 1) {
                             databaseManager.removeGroup(groupName);
                         }
                         return new ChatServerResponse(ResponseType.USER_REMOVED_FROM_GROUP);
                     } else {
                         return new ChatServerResponse(ResponseType.USER_NOT_MEMBER_OF_GROUP);
                     }
                 } else {
                     return new ChatServerResponse(ResponseType.GROUP_NOT_FOUND);
                 }
             } catch (SQLException e) {
                 return new ChatServerResponse(ResponseType.DATABASE_FAILURE);
             }
         } else {
             return new ChatServerResponse(ResponseType.USER_NOT_FOUND);
         }
     }
 }
