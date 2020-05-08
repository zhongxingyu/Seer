 package edu.berkeley.cs.cs162;
 
 import java.util.NoSuchElementException;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.TreeSet;
 
 class UserManager {
     public final int maxUsers;
     public final int maxQueuedUsers;
     public final int maxGroupUsers;
 
     public ChatServer chatServer;
     private HashMap<String, ChatUser> users;
     private LinkedList<ChatUser> userQueue;
     private HashMap<String, ChatGroup> groups;
     private ReentrantReadWriteLock rwLock;
 
     public UserManager(ChatServer chatServer, int maxUsers, int maxGroupUsers) {
         this.maxUsers = maxUsers;
         this.maxQueuedUsers = maxUsers / 10;
         this.maxGroupUsers = maxGroupUsers;
         this.chatServer = chatServer;
         this.users = new HashMap<String, ChatUser>();
         this.userQueue = new LinkedList<ChatUser>();
         this.groups = new HashMap<String, ChatGroup>();
         this.rwLock = new ReentrantReadWriteLock();
     }
 
     public void shutdown() {
         for (ChatUser user : users.values()) {
             user.shutdown();
         }
         for (ChatUser user : users.values()) {
             try {
                 user.join();
             } catch (InterruptedException e) {
             }
         }
     }
 
     public boolean hasName(String name) {
         boolean result;
         rwLock.readLock().lock();
         result = hasGroup(name) || hasUser(name);
         rwLock.readLock().unlock();
         return result;
     }
 
     public ChatUser getUser(String userName) {
         ChatUser result;
         rwLock.readLock().lock();
         result = users.get(userName);
         rwLock.readLock().unlock();
         return result;
     }
 
     public boolean hasUser(String userName) {
         boolean result;
         rwLock.readLock().lock();
         result = users.containsKey(userName);
         rwLock.readLock().unlock();
         return result;
     }
 
     public ChatServerResponse getUserCount(ChatUser user) {
         ChatServerResponse result;
         rwLock.readLock().lock();
         if (users.containsKey(user.getUserName())) {
             result = new ChatServerResponse(ResponseType.DATA_SENT, users.size());
         } else {
             result = new ChatServerResponse(ResponseType.USER_NOT_FOUND);
         }
         rwLock.readLock().unlock();
         return result;
     }
 
     public ChatServerResponse getUserList(ChatUser user) {
         ChatServerResponse result;
         rwLock.readLock().lock();
         if (users.containsKey(user.getUserName())) {
             result = new ChatServerResponse(ResponseType.DATA_SENT, new TreeSet<String>(users.keySet()));
         } else {
             result = new ChatServerResponse(ResponseType.USER_NOT_FOUND);
         }
         rwLock.readLock().unlock();
         return result;
     }
 
     public ChatGroup getGroup(String groupName) {
         ChatGroup result;
         rwLock.readLock().lock();
         result = groups.get(groupName);
         rwLock.readLock().unlock();
         return result;
     }
 
     public boolean hasGroup(String groupName) {
         boolean result;
         rwLock.readLock().lock();
         result = groups.containsKey(groupName);
         rwLock.readLock().unlock();
         return result;
     }
 
     public ChatServerResponse getGroupCount(ChatUser user) {
         ChatServerResponse result;
         rwLock.readLock().lock();
         if (users.containsKey(user.getUserName())) {
             result = new ChatServerResponse(ResponseType.DATA_SENT, groups.size());
         } else {
             result = new ChatServerResponse(ResponseType.USER_NOT_FOUND);
         }
         rwLock.readLock().unlock();
         return result;
     }
 
     public ChatServerResponse getGroupList(ChatUser user) {
         ChatServerResponse result;
         rwLock.readLock().lock();
         if (users.containsKey(user.getUserName())) {
             result = new ChatServerResponse(ResponseType.DATA_SENT, new TreeSet<String>(groups.keySet()));
         } else {
             result = new ChatServerResponse(ResponseType.USER_NOT_FOUND);
         }
         rwLock.readLock().unlock();
         return result;
     }
 
    public TreeSet<String> getGroupUsers(String groupName) {
        TreeSet<String> result;
        rwLock.readLock().lock();
        ChatGroup targetGroup = groups.get(groupName);
        if (targetGroup == null) {
            result = null;
        } else {
            result = new TreeSet<String>(targetGroup.users.keySet());
        }
        rwLock.readLock().unlock();
        return result;
    }

     public boolean groupHasUser(String groupName, String userName) {
         boolean result = false;
         rwLock.readLock().lock();
         ChatGroup targetGroup = groups.get(groupName);
         if (targetGroup != null) {
             result = targetGroup.users.containsKey(userName);
         }
         rwLock.readLock().unlock();
         return result;
     }
 
     public ChatServerResponse getGroupUserCount(ChatUser user, String groupName) {
         ChatServerResponse result;
         rwLock.readLock().lock();
         ChatGroup targetGroup = groups.get(groupName);
         if (users.containsKey(user.getUserName())) {
             if (targetGroup == null) {
                 result = new ChatServerResponse(ResponseType.GROUP_NOT_FOUND);
             } else {
                 result = new ChatServerResponse(ResponseType.DATA_SENT, targetGroup.users.size());
             }
         } else {
             result = new ChatServerResponse(ResponseType.USER_NOT_FOUND);
         }
         rwLock.readLock().unlock();
         return result;
     }
 
     public ChatServerResponse getGroupUserList(ChatUser user, String groupName) {
         ChatServerResponse result;
         rwLock.readLock().lock();
         ChatGroup targetGroup = groups.get(groupName);
         if (users.containsKey(user.getUserName())) {
             if (targetGroup == null) {
                 result = new ChatServerResponse(ResponseType.GROUP_NOT_FOUND);
             } else {
                 result = new ChatServerResponse(ResponseType.DATA_SENT, new TreeSet<String>(targetGroup.users.keySet()));
             }
         } else {
             result = new ChatServerResponse(ResponseType.USER_NOT_FOUND);
         }
         rwLock.readLock().unlock();
         return result;
     }
 
     public ChatServerResponse addUser(ChatUser user) {
         ChatServerResponse result;
         rwLock.writeLock().lock();
         if (users.size() >= maxUsers) {
             if (userQueue.size() >= maxQueuedUsers) {
                 result = new ChatServerResponse(ResponseType.USER_CAPACITY_REACHED);
             } else {
                 userQueue.add(user);
                 result = new ChatServerResponse(ResponseType.USER_QUEUED);
             }
         } else if (users.containsKey(user.getUserName()) || groups.containsKey(user.getUserName())) {
             result = new ChatServerResponse(ResponseType.NAME_CONFLICT);
         } else {
             users.put(user.getUserName(), user);
             result = new ChatServerResponse(ResponseType.USER_ADDED);
         }
         rwLock.writeLock().unlock();
         return result;
     }
 
     public ChatServerResponse removeUser(ChatUser user) {
         ChatServerResponse result;
         rwLock.writeLock().lock();
         if (users.containsKey(user.getUserName())) {
             TreeSet<String> groupsToRemove = new TreeSet<String>();
             for (ChatGroup group : groups.values()) {
                 group.users.remove(user.getUserName());
                 if (group.users.size() == 0) {
                     groupsToRemove.add(group.name);
                 }
             }
             for (String groupName : groupsToRemove) {
                 groups.remove(groupName);
             }
             users.remove(user.getUserName());
             result = new ChatServerResponse(ResponseType.USER_REMOVED);
             try {
                 ChatUser queuedUser = userQueue.remove();
                 users.put(queuedUser.getUserName(), queuedUser);
                 queuedUser.loggedIn();
             } catch (NoSuchElementException e) {
             }
         } else if (userQueue.remove(user)) {
             result = new ChatServerResponse(ResponseType.USER_REMOVED);
         } else {
             result = new ChatServerResponse(ResponseType.USER_NOT_FOUND);
         }
         rwLock.writeLock().unlock();
         return result;
     }
 
     public ChatServerResponse addUserToGroup(ChatUser user, String groupName) {
         ChatServerResponse result;
         rwLock.writeLock().lock();
         if (users.containsKey(user.getUserName())) {
             ChatGroup targetGroup = groups.get(groupName);
             if (targetGroup == null) {
                 targetGroup = new ChatGroup(groupName);
                 groups.put(groupName, targetGroup);
                 targetGroup.users.put(user.getUserName(), user);
                 result = new ChatServerResponse(ResponseType.USER_ADDED_TO_NEW_GROUP);
             } else {
                 if (targetGroup.users.containsKey(user.getUserName())) {
                     result = new ChatServerResponse(ResponseType.USER_ALREADY_MEMBER_OF_GROUP);
                 } else {
                     if (targetGroup.users.size() >= maxGroupUsers) {
                         result = new ChatServerResponse(ResponseType.GROUP_CAPACITY_REACHED);
                     } else {
                         targetGroup.users.put(user.getUserName(), user);
                         result = new ChatServerResponse(ResponseType.USER_ADDED_TO_GROUP);
                     }
                 }
             }
         } else {
             result = new ChatServerResponse(ResponseType.USER_NOT_FOUND);
         }
         rwLock.writeLock().unlock();
         return result;
     }
 
     public ChatServerResponse removeUserFromGroup(ChatUser user, String groupName) {
         ChatServerResponse result;
         rwLock.writeLock().lock();
         if (users.containsKey(user.getUserName())) {
             ChatGroup targetGroup = groups.get(groupName);
             if (targetGroup == null) {
                 result = new ChatServerResponse(ResponseType.GROUP_NOT_FOUND);
             } else {
                 if (targetGroup.users.remove(user.getUserName()) == null) {
                     result = new ChatServerResponse(ResponseType.USER_NOT_MEMBER_OF_GROUP);
                 } else {
                     result = new ChatServerResponse(ResponseType.USER_REMOVED_FROM_GROUP);
                     if (targetGroup.users.size() == 0) {
                         groups.remove(targetGroup.name);
                     }
                 }
             }
         } else {
             result = new ChatServerResponse(ResponseType.USER_NOT_FOUND);
         }
         rwLock.writeLock().unlock();
         return result;
     }
 }
