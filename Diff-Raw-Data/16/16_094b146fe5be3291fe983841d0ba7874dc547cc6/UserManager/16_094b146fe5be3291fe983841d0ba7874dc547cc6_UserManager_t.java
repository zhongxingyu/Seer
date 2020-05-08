 package edu.berkeley.cs.cs162;
 
 import java.sql.SQLException;
 import java.util.*;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
public class UserManager {
 
     private ChatServer chatServer;
     private DatabaseManager databaseManager;
     private HashMap<String, ChatUser> localUsers;
     private ReentrantReadWriteLock rwLock;
 
     public UserManager(ChatServer chatServer) {
         this.chatServer = chatServer;
         this.databaseManager = new DatabaseManager();
         this.localUsers = new HashMap<String, ChatUser>();
         this.rwLock = new ReentrantReadWriteLock();
     }
 
     public void shutdown() {
         rwLock.readLock().lock();
         HashSet<ChatUser> users = new HashSet<ChatUser>(localUsers.values());
         rwLock.readLock().unlock();
         for (ChatUser user : users) {
             user.shutdown();
         }
         for (ChatUser user : users) {
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
         return hasUser(name) || hasGroup(name);
     }
 
     private boolean hasUser(String userName) throws SQLException {
         return databaseManager.getUser(userName) != null;
     }
 
     public boolean hasLocalUser(String userName) {
         boolean result;
         rwLock.readLock().lock();
         result = localUsers.containsKey(userName);
         rwLock.readLock().unlock();
         return result;
     }
 
     public ChatUser getLocalUser(String userName) {
         ChatUser result;
         rwLock.readLock().lock();
         result = localUsers.get(userName);
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
                 if (localUsers.get(user.getUserName()) == null) {
                     localUsers.put(user.getUserName(), user);
                     databaseManager.loginUser(user.getUserName());
                     result = new ChatServerResponse(ResponseType.USER_LOGGED_IN);
                 } else {
                     result = new ChatServerResponse(ResponseType.USER_ALREADY_LOGGED_IN);
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
         try {
             ChatServerResponse result;
             rwLock.writeLock().lock();
             if (localUsers.containsKey(user.getUserName())) {
                 localUsers.remove(user.getUserName());
                 databaseManager.logoutUser(user.getUserName());
                 result = new ChatServerResponse(ResponseType.USER_LOGGED_OUT);
             } else {
                 result = new ChatServerResponse(ResponseType.USER_NOT_FOUND);
             }
             rwLock.writeLock().unlock();
             return result;
         } catch (SQLException e) {
             return new ChatServerResponse(ResponseType.DATABASE_FAILURE);
         }
     }
 
     public ChatServerResponse migrateUser(ChatUser user) {
         ChatServerResponse result;
         rwLock.writeLock().lock();
         if (localUsers.containsKey(user.getUserName())) {
             localUsers.remove(user.getUserName());
             result = new ChatServerResponse(ResponseType.MIGRATE);
         } else {
             result = new ChatServerResponse(ResponseType.USER_NOT_FOUND);
         }
         rwLock.writeLock().unlock();
         return result;
     }
 
     public ChatServerResponse addUserToGroup(ChatUser user, String groupName) {
         rwLock.readLock().lock();
         boolean loggedIn = localUsers.containsKey(user.getUserName());
         rwLock.readLock().unlock();
         if (loggedIn) {
             try {
                 if (hasUser(groupName)) {
                     return new ChatServerResponse(ResponseType.NAME_CONFLICT);
                 } else if (hasGroup(groupName)) {
                     if (databaseManager.getGroupUserList(groupName).contains(user.getUserName())) {
                         return new ChatServerResponse(ResponseType.USER_ALREADY_MEMBER_OF_GROUP);
                     } else {
                         databaseManager.addUserToGroup(user.getUserName(), groupName);
                         return new ChatServerResponse(ResponseType.USER_ADDED_TO_GROUP);
                     }
                 } else {
                     databaseManager.addGroup(groupName);
                     databaseManager.addUserToGroup(user.getUserName(), groupName);
                     return new ChatServerResponse(ResponseType.USER_ADDED_TO_NEW_GROUP);
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
         boolean loggedIn = localUsers.containsKey(user.getUserName());
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
