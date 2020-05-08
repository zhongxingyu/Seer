 package edu.berkeley.cs.cs162;
 
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.Date;
 import java.util.Iterator;
 
 public class TestChatServer {
 
 	public static void main(String[] args) throws InterruptedException {
         testBasic();
         testLogout();
         testUserNameUniqueness();
         testServerCapacity();
         testLoginQueue();
         testGroupCapacity();
         testUserJoinsMultipleGroups();
         testUnicastMessages();
         testBroadcastMessages();
         testServerShutdown();
         testUserManager();
 	}
 
     public static void testBasic() throws InterruptedException {
         System.out.println("=== BEGIN TEST Basic Test ===");
         ChatServer chatServer = new ChatServer();
         chatServer.start();
         ExecutorService threadPool = Executors.newFixedThreadPool(10);
 
         ChatUser user1 = new ChatUser(chatServer);
         ChatUser user2 = new ChatUser(chatServer);
         ChatUser user3 = new ChatUser(chatServer);
 
         user1.login("User 1");
         user2.login("User 2");
         user3.login("User 3");
 
         user1.sendMessage("User 2", "Message 1");
         Thread.currentThread().sleep(50);
         user2.sendMessage("User 1", "Message 2");
         Thread.currentThread().sleep(50);
         user1.sendMessage("User 2", "Message 3");
         Thread.currentThread().sleep(50);
         user2.sendMessage("User 1", "Message 4");
         Thread.currentThread().sleep(50);
 
         user1.joinGroup("Group 1");
         user2.joinGroup("Group 1");
         user3.joinGroup("Group 1");
 
         user1.sendMessage("Group 1", "Message 5");
         Thread.currentThread().sleep(50);
         user2.sendMessage("Group 1", "Message 6");
         Thread.currentThread().sleep(50);
         user3.sendMessage("Group 1", "Message 7");
         Thread.currentThread().sleep(50);
 
         user1.logout();
         user2.logout();
         user3.logout();
 
         System.out.println("\n== BEGIN LOG user1 ==");
         user1.printLog();
         System.out.println("== END LOG user1 ==");
         System.out.println("\n== BEGIN LOG user2 ==");
         user2.printLog();
         System.out.println("== END LOG user2 ==");
         System.out.println("\n== BEGIN LOG user3 ==");
         user3.printLog();
         System.out.println("== END LOG user3 ==");
 
         chatServer.shutdown();
         threadPool.shutdown();
         System.out.println("=== END TEST Basic test ===\n");
     }
 
     public static void testLogout() throws InterruptedException {
         System.out.println("=== BEGIN TEST Logout ===");
         ChatServer chatServer = new ChatServer();
         chatServer.start();
         ExecutorService threadPool = Executors.newFixedThreadPool(10);
 
         ChatUser user1 = new ChatUser(chatServer);
         ChatUser user2 = new ChatUser(chatServer);
         user1.login("User 1");
         user2.login("User 2");
         user1.joinGroup("Group 1");
         user2.joinGroup("Group 1");
         user1.joinGroup("Group 2");
         user1.joinGroup("Group 3");
         user1.logout();
 
         System.out.println("\n== BEGIN LOG user1 ==");
         user1.printLog();
         System.out.println("== END LOG user1 ==");
 
         chatServer.shutdown();
         threadPool.shutdown();
         System.out.println("=== END TEST Logout ===\n");
     }
 
     public static void testUserNameUniqueness() throws InterruptedException {
         System.out.println("=== BEGIN TEST User Name Uniqueness ===");
         ChatServer chatServer = new ChatServer();
         chatServer.start();
         ExecutorService threadPool = Executors.newFixedThreadPool(10);
 
         ChatUser user1 = new ChatUser(chatServer);
         ChatUser user2 = new ChatUser(chatServer);
 
         user1.login("Name");
         user2.login("Name");
 
         System.out.println("\n== BEGIN LOG user1 ==");
         user1.printLog();
         System.out.println("== END LOG user1 ==");
         System.out.println("\n== BEGIN LOG user2 ==");
         user2.printLog();
         System.out.println("== END LOG user2 ==");
 
         chatServer.shutdown();
         threadPool.shutdown();
         System.out.println("=== END TEST User Name Uniqueness ===\n");
     }
 
     public static void testServerCapacity() throws InterruptedException {
         System.out.println("=== BEGIN TEST Server Capacity ===");
         ChatServer chatServer = new ChatServer();
         chatServer.start();
         ExecutorService threadPool = Executors.newFixedThreadPool(10);
 
         ChatUser[] users = new ChatUser[112];
         for (int i = 1; i <= 111; i++) {
             users[i] = new ChatUser(chatServer);
             users[i].login("User " + Integer.toString(i));
         }
 
         for (int i = 1; i <= 111; i++) {
             System.out.println("\n== BEGIN LOG user[" + Integer.toString(i) + "] ==");
             users[i].printLog();
             System.out.println("== END LOG user[" + Integer.toString(i) + "] ==");
         }
 
         chatServer.shutdown();
         threadPool.shutdown();
         System.out.println("=== END TEST Server Capacity ===\n");
     }
 
     public static void testLoginQueue() throws InterruptedException {
         System.out.println("=== BEGIN TEST Login Queue ===");
         ChatServer chatServer = new ChatServer();
         chatServer.start();
         ExecutorService threadPool = Executors.newFixedThreadPool(10);
 
         ChatUser[] users = new ChatUser[111];
         for (int i = 1; i <= 110; i++) {
             users[i] = new ChatUser(chatServer);
             users[i].login("User " + Integer.toString(i));
         }
 
        users[110].logout();

         for (int i = 1; i <= 10; i++) {
             users[i].logout();
         }
 
         for (int i = 1; i <= 110; i++) {
             System.out.println("\n== BEGIN LOG user[" + Integer.toString(i) + "] ==");
             users[i].printLog();
             System.out.println("== END LOG user[" + Integer.toString(i) + "] ==");
         }
 
         chatServer.shutdown();
         threadPool.shutdown();
         System.out.println("=== END TEST Login Queue ===\n");
     }
 
     public static void testGroupCapacity() throws InterruptedException {
         System.out.println("=== BEGIN TEST Group Capacity ===");
         ChatServer chatServer = new ChatServer();
         chatServer.start();
         ExecutorService threadPool = Executors.newFixedThreadPool(10);
 
         ChatUser[] users = new ChatUser[12];
         for (int i = 1; i <= 11; i++) {
             users[i] = new ChatUser(chatServer);
             users[i].login("User " + Integer.toString(i));
             users[i].joinGroup("Group 1");
         }
 
         for (int i = 1; i <= 11; i++) {
             System.out.println("\n== BEGIN LOG user[" + Integer.toString(i) + "] ==");
             users[i].printLog();
             System.out.println("== END LOG user[" + Integer.toString(i) + "] ==");
         }
 
         chatServer.shutdown();
         threadPool.shutdown();
         System.out.println("=== END TEST Group Capacity ===\n");
     }
 
     public static void testUserJoinsMultipleGroups() throws InterruptedException {
         System.out.println("=== BEGIN TEST User Joins Multiple Groups ===");
         ChatServer chatServer = new ChatServer();
         chatServer.start();
         ExecutorService threadPool = Executors.newFixedThreadPool(10);
 
         ChatUser user1 = new ChatUser(chatServer);
         user1.login("User 1");
         user1.joinGroup("Group 1");
         user1.joinGroup("Group 2");
         user1.joinGroup("Group 3");
 
         user1.printLog();
 
         chatServer.shutdown();
         threadPool.shutdown();
         System.out.println("=== END TEST User Joins Multiple Groups ===\n");
     }
 
     public static void testUnicastMessages() throws InterruptedException {
         System.out.println("=== BEGIN TEST Unicast Messages ===");
         ChatServer chatServer = new ChatServer();
         chatServer.start();
         ExecutorService threadPool = Executors.newFixedThreadPool(10);
 
         ChatUser user1 = new ChatUser(chatServer);
         ChatUser user2 = new ChatUser(chatServer);
         user1.login("User 1");
         user2.login("User 2");
 
         for (int i = 1; i <= 10; i++) {
             user1.sendMessage("User 2", "Message " + Integer.toString(i));
             Thread.currentThread().sleep(50);
             user2.sendMessage("User 1", "Response " + Integer.toString(i));
             Thread.currentThread().sleep(50);
         }
 
         System.out.println("\n== BEGIN LOG user1 ==");
         user1.printLog();
         System.out.println("== END LOG user1 ==");
         System.out.println("\n== BEGIN LOG user2 ==");
         user2.printLog();
         System.out.println("== END LOG user2 ==");
 
         chatServer.shutdown();
         threadPool.shutdown();
         System.out.println("=== END TEST Unicast Messages ===\n");
     }
 
     public static void testBroadcastMessages() throws InterruptedException {
         System.out.println("=== BEGIN TEST Broadcast Messages ===");
         ChatServer chatServer = new ChatServer();
         chatServer.start();
         ExecutorService threadPool = Executors.newFixedThreadPool(10);
 
         ChatUser user1 = new ChatUser(chatServer);
         ChatUser user2 = new ChatUser(chatServer);
         ChatUser user3 = new ChatUser(chatServer);
         user1.login("User 1");
         user2.login("User 2");
         user3.login("User 3");
         user1.joinGroup("Group 1");
         user2.joinGroup("Group 1");
         user3.joinGroup("Group 1");
 
         for (int i = 1; i <= 10; i++) {
             user1.sendMessage("Group 1", "Message " + Integer.toString(i));
             Thread.currentThread().sleep(50);
             user2.sendMessage("Group 1", "Response " + Integer.toString(i));
             Thread.currentThread().sleep(50);
             user3.sendMessage("Group 1", "Another Response " + Integer.toString(i));
             Thread.currentThread().sleep(50);
         }
 
         System.out.println("\n== BEGIN LOG user1 ==");
         user1.printLog();
         System.out.println("== END LOG user1 ==");
         System.out.println("\n== BEGIN LOG user2 ==");
         user2.printLog();
         System.out.println("== END LOG user2 ==");
         System.out.println("\n== BEGIN LOG user3 ==");
         user3.printLog();
         System.out.println("== END LOG user3 ==");
 
         chatServer.shutdown();
         threadPool.shutdown();
         System.out.println("=== END TEST Broadcast Messages ===\n");
     }
 
     public static void testServerShutdown() throws InterruptedException {
         System.out.println("=== BEGIN TEST Server Shutdown ===");
         ChatServer chatServer = new ChatServer();
         chatServer.start();
         ExecutorService threadPool = Executors.newFixedThreadPool(10);
 
         ChatUser user1 = new ChatUser(chatServer);
         ChatUser user2 = new ChatUser(chatServer);
         ChatUser user3 = new ChatUser(chatServer);
         user1.login("User 1");
         user2.login("User 2");
         user2.joinGroup("Group 2");
 
         chatServer.shutdown();
         user1.sendMessage("User 2", "Message 1");
         user1.joinGroup("Group 1");
         user3.login("User 3");
 
         user1.logout();
         user2.leaveGroup("Group 2");
 
         System.out.println("\n== BEGIN LOG user1 ==");
         user1.printLog();
         System.out.println("== END LOG user1 ==");
         System.out.println("\n== BEGIN LOG user2 ==");
         user2.printLog();
         System.out.println("== END LOG user2 ==");
         System.out.println("\n== BEGIN LOG user3 ==");
         user3.printLog();
         System.out.println("== END LOG user3 ==");
 
         threadPool.shutdown();
         System.out.println("=== END TEST Server Shutdown ===\n");
     }
 
     public static void testUserManager() throws InterruptedException {
         System.out.println("=== BEGIN TEST UserManager ===");
         ChatServer chatServer = new ChatServer();
         chatServer.start();
         ExecutorService threadPool = Executors.newFixedThreadPool(10);
 
         ChatUser user1 = new ChatUser(chatServer);
         ChatUser user2 = new ChatUser(chatServer);
         ChatUser user3 = new ChatUser(chatServer);
         ChatUser user4 = new ChatUser(chatServer);
         user1.login("User 1");
         user2.login("User 2");
         user3.login("User 3");
 
         user4.getUserCount();
         user4.getUserList();
         user4.getGroupCount();
         user4.getGroupList();
 
         user4.login("User 4");
         user4.logout();
         user4.getUserCount();
         user4.getUserList();
         user4.getGroupCount();
         user4.getGroupList();
 
         user1.getGroupUserList("Group 1");
         user1.leaveGroup("Group 1");
         user1.getGroupUserList("Group 1");
         user2.joinGroup("Group 1");
         user1.leaveGroup("Group 1");
         user1.getGroupUserList("Group 1");
         user2.leaveGroup("Group 1");
         user1.getGroupUserList("Group 1");
 
         user2.joinGroup("Group 1");
         user2.joinGroup("Group 2");
         user2.joinGroup("Group 3");
         user1.getGroupUserList("Group 1");
         user1.getGroupUserList("Group 2");
         user1.getGroupUserList("Group 3");
         user2.logout();
         user1.getGroupUserList("Group 1");
         user1.getGroupUserList("Group 2");
         user1.getGroupUserList("Group 3");
 
         System.out.println("\n== BEGIN LOG user1 ==");
         user1.printLog();
         System.out.println("== END LOG user1 ==");
         System.out.println("\n== BEGIN LOG user2 ==");
         user2.printLog();
         System.out.println("== END LOG user2 ==");
         System.out.println("\n== BEGIN LOG user3 ==");
         user3.printLog();
         System.out.println("== END LOG user3 ==");
         System.out.println("\n== BEGIN LOG user4 ==");
         user4.printLog();
         System.out.println("== END LOG user4 ==");
 
         chatServer.shutdown();
         threadPool.shutdown();
         System.out.println("=== END TEST UserManager ===\n");
     }
 
 	/**
 	 * Logs the events of a user logging into the ChatServer.  This should only be called AFTER
 	 * the user has been accepted by the ChatServer.
 	 *
 	 * @param username user logging the event.
 	 * @param time time of the event.
 	 */
 	public static void logUserLogin(String username, Date time) {
 	}
 
 	/**
 	 *  Logs a login-failed event.  Should be called AFTER you are certain that the user has been rejected by
 	 *  by the ChatServer.
 	 *
 	 * @param username user logging the event.
 	 * @param time time of the event.
 	 * @param e login error
 	 */
 	public static void logUserLoginFailed(String username, Date time, LoginError e) {
 	}
 
 	/**
 	 * Logs the logout event.  This should only be called AFTER the user has been released by the
 	 * ChatServer successfully.
 	 *
 	 * @param username user logging the event
 	 * @param time time of the event
 	 */
 	public static void logUserLogout(String username, Date time) {
 	}
 
 	/**
 	 * Logs the events of a user logging into the group.  This should only be called AFTER
 	 * the user has been accepted by the group.
 	 *
 	 * @param groupname name of the group.
 	 * @param username user logging the event.
 	 * @param time time of the event.
 	 */
 	public static void logUserJoinGroup(String groupname, String username, Date time) {
 	}
 
 	/**
 	 * Logs the events of a user logging out of the group.
 	 *
 	 * @param groupname name of the group.
 	 * @param username user logging the event.
 	 * @param time time of the event.
 	 */
 	public static void logUserLeaveGroup(String groupname, String username, Date time) {
 	}
 
 
 	/**
 	 * This should be called when the user attempts to send a message to the chat server
 	 * (after the call is made).
 	 *
 	 * @param username the name of the user
 	 * @param msg the string representation of the message.
 	 * 			SRC DST TIMESTAMP_UNIXTIME SQN
 	 * 		example: alice bob 1298489721 23
 	 */
 	public static void logUserSendMsg(String username, String msg) {
 	}
 
 	/**
 	 * If, for any reason, the ChatServer determines that the message cannot be delivered.  This
 	 * message should be called to log that event.
 	 *
 	 * @param msg string representation of the message
 	 * 			SRC DST TIMESTAMP_UNIXTIME SQN
 	 * 		example: alice bob 1298489721 23
 	 * @param time time when the event occurred.
 	 */
 	public static void logChatServerDropMsg(String msg, Date time) {
 	}
 
 	/**
 	 * When the user receives a message, this method should be called.
 	 *
 	 * @parapm username name of the user
 	 * @param msg string representation of the message.
 	 * 			SRC DST TIMESTAMP_UNIXTIME SQN
 	 * 		example: alice bob 1298489721 23
 	 * @param time time when the event occurred.
 	 */
 	public static void logUserMsgRecvd(String username, String msg, Date time) {
 	}
 }
