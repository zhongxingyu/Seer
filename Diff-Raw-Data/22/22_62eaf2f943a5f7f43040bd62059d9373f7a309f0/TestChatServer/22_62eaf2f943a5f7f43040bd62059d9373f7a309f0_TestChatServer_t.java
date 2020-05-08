 package edu.berkeley.cs.cs162;
 
 import java.io.*;
 import java.net.*;
 import java.sql.SQLException;
 import java.util.*;
 
 public class TestChatServer {
 
     public static void printSet(Collection<String> set) {
         for (String string : set) {
             System.out.println(string);
         }
     }
 
 	public static void main(String[] args) throws Exception {
         // Database Tests
         //testDatabaseEmptyDatabase();
         //testDatabaseAddUsersAndGroups();
         //testDatabaseOfflineMessages();
 
         // Non-Networked Tests
         //testBasic();
         //testLogout();
         //testUserNameUniqueness();
         //testServerCapacity();
         //testLoginQueue();
         //testUserJoinsMultipleGroups();
         //testUnicastMessages();
         //testBroadcastMessages();
         //testSelfUnicast();
         //testNonMemberBroadcast();
         //testServerShutdown();
         //testOfflineMessages();
 
         // Networked Tests
         //testNetworkLogin();
         //testNetworkLoginTimeout();
         //testNetworkUnexpectedDisconnectBeforeLogin();
         //testNetworkUnexpectedDisconnectAfterLogin();
         //testNetworkLoginQueue();
         //testNetworkSendReceive();
         //testNetworkReadlog();
 
         // Client-Server Tests
         //testClientBasic();
         //testClientLogout();
         //testClientDisconnect();
         //testClientReconnect();
         //testClientTimeout();
        testClientLoginQueue();
         //testClientDisconnectsWhileInLoginWaitQueue();
         //testClientDoesNotNormallyTimeout();
         //testClientDisconnectHandledAfterLogoff();
         //testTimerRestartedAfterEveryFailedLoginAttempt();
         //testCertainClientCommandsRejectedWhenNotConnectedOrLoggedIn();
         //testInvalidClientCommandsAreSkipped();
         //testClientAdduserLogin();
         //testClientMessaging();
        //testClientReadlog();
 	}
 
     // Database Tests
 
     public static void testDatabaseEmptyDatabase() throws SQLException {
         System.out.println("=== BEGIN DATABASE TEST Empty Database  ===");
         DatabaseManager databaseManager = new DatabaseManager();
         databaseManager.emptyDatabase();
 
         System.out.println("User Count: " + databaseManager.getUserCount());
         System.out.println("Group Count: " + databaseManager.getGroupCount());
         System.out.println("\nUser List");
         printSet(databaseManager.getUserList());
         System.out.println("\nGroup List");
         printSet(databaseManager.getGroupList());
 
         databaseManager.emptyDatabase();
         System.out.println("=== END DATABASE TEST Empty Database  ===\n");
     }
 
     public static void testDatabaseAddUsersAndGroups() throws SQLException {
         System.out.println("=== BEGIN DATABASE TEST Add Users and Groups  ===");
         DatabaseManager databaseManager = new DatabaseManager();
         databaseManager.emptyDatabase();
 
         databaseManager.addUser("user1", "password");
         HashMap<String, Object> user1 = databaseManager.getUser("user1");
         System.out.println("Added User: " + user1.get("name") + ", " + user1.get("password"));
         databaseManager.addUser("user2", "password");
         HashMap<String, Object> user2 = databaseManager.getUser("user2");
         System.out.println("Added User: " + user2.get("name") + ", " + user2.get("password"));
 
         databaseManager.addGroup("group1");
         HashMap<String, Object> group1 = databaseManager.getGroup("group1");
         System.out.println("Added Group: " + group1.get("name"));
         databaseManager.addGroup("group2");
         HashMap<String, Object> group2 = databaseManager.getGroup("group2");
         System.out.println("Added Group: " + group2.get("name"));
 
         databaseManager.addUserToGroup("user1", "group1");
         databaseManager.addUserToGroup("user2", "group1");
         databaseManager.addUserToGroup("user1", "group2");
 
         System.out.println("User Count: " + databaseManager.getUserCount());
         System.out.println("Group Count: " + databaseManager.getGroupCount());
         System.out.println("group1 User Count: " + databaseManager.getGroupUserCount("group1"));
         System.out.println("group2 User Count: " + databaseManager.getGroupUserCount("group2"));
 
         System.out.println("\nUser List");
         printSet(databaseManager.getUserList());
         System.out.println("\nGroup List");
         printSet(databaseManager.getGroupList());
         System.out.println("\ngroup1 User List");
         printSet(databaseManager.getGroupUserList("group1"));
         System.out.println("\ngroup2 User List");
         printSet(databaseManager.getGroupUserList("group2"));
 
         System.out.println("\nRemoving user1 from group2");
         databaseManager.removeUserFromGroup("user1", "group2");
         System.out.println("User List");
         printSet(databaseManager.getUserList());
         System.out.println("\nGroup List");
         printSet(databaseManager.getGroupList());
         System.out.println("\ngroup1 User List");
         printSet(databaseManager.getGroupUserList("group1"));
         System.out.println("\ngroup2 User List");
 
         System.out.println("\nRemoving user1");
         databaseManager.removeUser("user1");
         System.out.println("User List");
         printSet(databaseManager.getUserList());
         System.out.println("\nGroup List");
         printSet(databaseManager.getGroupList());
         System.out.println("\ngroup1 User List");
         printSet(databaseManager.getGroupUserList("group1"));
         System.out.println("\ngroup2 User List");
 
         System.out.println("\nRemoving user2");
         databaseManager.removeUser("user2");
         System.out.println("User List");
         printSet(databaseManager.getUserList());
         System.out.println("\nGroup List");
         printSet(databaseManager.getGroupList());
         System.out.println("\ngroup1 User List");
         printSet(databaseManager.getGroupUserList("group1"));
         System.out.println("\ngroup2 User List");
 
         System.out.println("\nRemoving group1");
         databaseManager.removeGroup("group1");
         System.out.println("Group List");
         printSet(databaseManager.getGroupList());
 
         System.out.println("\nGroup Count: " + databaseManager.getGroupCount());
 
         databaseManager.emptyDatabase();
         System.out.println("=== END DATABASE TEST Add Users and Groups  ===\n");
     }
 
     public static void testDatabaseOfflineMessages() throws Exception {
         System.out.println("=== BEGIN DATABASE TEST Database Offline Messages  ===");
         DatabaseManager databaseManager = new DatabaseManager();
         databaseManager.emptyDatabase();
 
         databaseManager.addUser("user1", "password");
         HashMap<String, Object> user1 = databaseManager.getUser("user1");
         System.out.println("Added User: " + user1.get("name") + ", " + user1.get("password"));
         databaseManager.addUser("user2", "password");
         HashMap<String, Object> user2 = databaseManager.getUser("user2");
         System.out.println("Added User: " + user2.get("name") + ", " + user2.get("password"));
 
         databaseManager.addGroup("group1");
         HashMap<String, Object> group1 = databaseManager.getGroup("group1");
         System.out.println("Added Group: " + group1.get("name"));
         databaseManager.addGroup("group2");
         HashMap<String, Object> group2 = databaseManager.getGroup("group2");
         System.out.println("Added Group: " + group2.get("name"));
         databaseManager.addUserToGroup("user1", "group1");
         databaseManager.addUserToGroup("user2", "group1");
 
         ChatUser chatUser2 = new ChatUser(null);
         chatUser2.setUserName("user2");
         databaseManager.logMessage("user1", new Message(chatUser2, "user1", 1, "Message 1"));
         Thread.currentThread().sleep(2000);
         databaseManager.logMessage("user1", new Message(chatUser2, "group1", 2, "Message 2"));
 
         System.out.println("\nuser1 Offline Messages");
         LinkedList<HashMap<String, Object>> messages = databaseManager.getOfflineMessages("user1");
         HashMap<String, Object> messageProperties = messages.poll();
         while (messageProperties != null) {
             System.out.println(messageProperties.get("text"));
             messageProperties = messages.poll();
         }
 
         databaseManager.emptyDatabase();
         System.out.println("=== END DATABASE TEST Database Offline Messages  ===\n");
     }
 
     // Non-Networked Server Tests
 
     public static void testBasic() throws Exception {
         System.out.println("=== BEGIN TEST Basic Test ===");
         ChatServer chatServer = new ChatServer();
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.start();
 
         ChatUser user1 = new ChatUser(chatServer);
         ChatUser user2 = new ChatUser(chatServer);
         ChatUser user3 = new ChatUser(chatServer);
 
         user1.addUser("user1", "password");
         user2.addUser("user2", "password");
         user3.addUser("user3", "password");
 
         user1.login("user1", "password");
         user2.login("user2", "password");
         user3.login("user3", "password");
 
         user1.sendMessage("user2", 1, "Message 1");
         Thread.currentThread().sleep(50);
         user2.sendMessage("user1", 1, "Message 2");
         Thread.currentThread().sleep(50);
         user1.sendMessage("user2", 2, "Message 3");
         Thread.currentThread().sleep(50);
         user2.sendMessage("user1", 2, "Message 4");
         Thread.currentThread().sleep(50);
 
         user1.joinGroup("group1");
         user2.joinGroup("group1");
         user3.joinGroup("group1");
 
         user1.sendMessage("group1", 3, "Message 5");
         Thread.currentThread().sleep(50);
         user2.sendMessage("group1", 3, "Message 6");
         Thread.currentThread().sleep(50);
         user3.sendMessage("group1", 1, "Message 7");
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
         chatServer.getDatabaseManager().emptyDatabase();
         System.out.println("=== END TEST Basic test ===\n");
     }
 
     public static void testLogout() throws Exception {
         System.out.println("=== BEGIN TEST Logout ===");
         ChatServer chatServer = new ChatServer();
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.start();
 
         ChatUser user1 = new ChatUser(chatServer);
         ChatUser user2 = new ChatUser(chatServer);
         user1.addUser("user1", "password");
         user2.addUser("user2", "password");
         user1.login("user1", "password");
         user2.login("user2", "password");
         user1.joinGroup("group1");
         user2.joinGroup("group1");
         user1.joinGroup("group2");
         user1.joinGroup("group3");
         user1.logout();
 
         System.out.println("\n== BEGIN LOG user1 ==");
         user1.printLog();
         System.out.println("== END LOG user1 ==");
 
         chatServer.shutdown();
         chatServer.getDatabaseManager().emptyDatabase();
         System.out.println("=== END TEST Logout ===\n");
     }
 
     public static void testUserNameUniqueness() throws Exception {
         System.out.println("=== BEGIN TEST User Name Uniqueness ===");
         ChatServer chatServer = new ChatServer();
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.start();
 
         ChatUser user1 = new ChatUser(chatServer);
         ChatUser user2 = new ChatUser(chatServer);
 
         user1.addUser("name", "password");
         user2.addUser("Name", "password");
 
         System.out.println("\n== BEGIN LOG user1 ==");
         user1.printLog();
         System.out.println("== END LOG user1 ==");
         System.out.println("\n== BEGIN LOG user2 ==");
         user2.printLog();
         System.out.println("== END LOG user2 ==");
 
         chatServer.shutdown();
         chatServer.getDatabaseManager().emptyDatabase();
         System.out.println("=== END TEST User Name Uniqueness ===\n");
     }
 
     public static void testServerCapacity() throws Exception {
         System.out.println("=== BEGIN TEST Server Capacity ===");
         ChatServer chatServer = new ChatServer();
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.start();
 
         ChatUser[] users = new ChatUser[112];
         for (int i = 1; i <= 111; i++) {
             users[i] = new ChatUser(chatServer);
             users[i].addUser("user" + Integer.toString(i), "password");
             users[i].login("user" + Integer.toString(i), "password");
         }
 
         for (int i = 1; i <= 111; i++) {
             System.out.println("\n== BEGIN LOG user[" + Integer.toString(i) + "] ==");
             users[i].printLog();
             System.out.println("== END LOG user[" + Integer.toString(i) + "] ==");
         }
 
         chatServer.shutdown();
         chatServer.getDatabaseManager().emptyDatabase();
         System.out.println("=== END TEST Server Capacity ===\n");
     }
 
     public static void testLoginQueue() throws Exception {
         System.out.println("=== BEGIN TEST Login Queue ===");
         ChatServer chatServer = new ChatServer();
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.start();
 
         ChatUser[] users = new ChatUser[111];
         for (int i = 1; i <= 110; i++) {
             users[i] = new ChatUser(chatServer);
             users[i].addUser("user" + Integer.toString(i), "password");
             users[i].login("user" + Integer.toString(i), "password");
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
         chatServer.getDatabaseManager().emptyDatabase();
         System.out.println("=== END TEST Login Queue ===\n");
     }
 
     public static void testUserJoinsMultipleGroups() throws Exception {
         System.out.println("=== BEGIN TEST User Joins Multiple Groups ===");
         ChatServer chatServer = new ChatServer();
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.start();
 
         ChatUser user1 = new ChatUser(chatServer);
         user1.addUser("user1", "password");
         user1.login("user1", "password");
         user1.joinGroup("group1");
         user1.joinGroup("group2");
         user1.joinGroup("group3");
 
         user1.printLog();
 
         chatServer.shutdown();
         chatServer.getDatabaseManager().emptyDatabase();
         System.out.println("=== END TEST User Joins Multiple Groups ===\n");
     }
 
     public static void testUnicastMessages() throws Exception {
         System.out.println("=== BEGIN TEST Unicast Messages ===");
         ChatServer chatServer = new ChatServer();
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.start();
 
         ChatUser user1 = new ChatUser(chatServer);
         ChatUser user2 = new ChatUser(chatServer);
         user1.addUser("user1", "password");
         user1.login("user1", "password");
         user2.addUser("user2", "password");
         user2.login("user2", "password");
 
         for (int i = 1; i <= 10; i++) {
             user1.sendMessage("user2", i, "Message " + Integer.toString(i));
             Thread.currentThread().sleep(50);
             user2.sendMessage("user1", i, "Response " + Integer.toString(i));
             Thread.currentThread().sleep(50);
         }
 
         System.out.println("\n== BEGIN LOG user1 ==");
         user1.printLog();
         System.out.println("== END LOG user1 ==");
         System.out.println("\n== BEGIN LOG user2 ==");
         user2.printLog();
         System.out.println("== END LOG user2 ==");
 
         chatServer.shutdown();
         chatServer.getDatabaseManager().emptyDatabase();
         System.out.println("=== END TEST Unicast Messages ===\n");
     }
 
     public static void testBroadcastMessages() throws Exception {
         System.out.println("=== BEGIN TEST Broadcast Messages ===");
         ChatServer chatServer = new ChatServer();
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.start();
 
         ChatUser user1 = new ChatUser(chatServer);
         ChatUser user2 = new ChatUser(chatServer);
         ChatUser user3 = new ChatUser(chatServer);
         user1.addUser("user1", "password");
         user1.login("user1", "password");
         user2.addUser("user2", "password");
         user2.login("user2", "password");
         user3.addUser("user3", "password");
         user3.login("user3", "password");
         user1.joinGroup("group1");
         user2.joinGroup("group1");
         user3.joinGroup("group1");
 
         for (int i = 1; i <= 10; i++) {
             user1.sendMessage("group1", i, "Message " + Integer.toString(i));
             Thread.currentThread().sleep(50);
             user2.sendMessage("group1", i, "Response " + Integer.toString(i));
             Thread.currentThread().sleep(50);
             user3.sendMessage("group1", i, "Another Response " + Integer.toString(i));
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
         chatServer.getDatabaseManager().emptyDatabase();
         System.out.println("=== END TEST Broadcast Messages ===\n");
     }
 
     public static void testSelfUnicast() throws Exception {
         System.out.println("=== BEGIN TEST Self Unicast ===");
         ChatServer chatServer = new ChatServer();
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.start();
 
         ChatUser user1 = new ChatUser(chatServer);
         user1.addUser("user1", "password");
         user1.login("user1", "password");
         user1.sendMessage("user1", 1, "Message 1");
 
         System.out.println("\n== BEGIN LOG user1 ==");
         user1.printLog();
         System.out.println("== END LOG user1 ==");
 
         chatServer.shutdown();
         chatServer.getDatabaseManager().emptyDatabase();
         System.out.println("=== END TEST Self Unicast ===\n");
     }
 
     public static void testNonMemberBroadcast() throws Exception {
         System.out.println("=== BEGIN TEST Non-Member Broadcast ===");
         ChatServer chatServer = new ChatServer();
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.start();
 
         ChatUser user1 = new ChatUser(chatServer);
         ChatUser user2 = new ChatUser(chatServer);
         user1.addUser("user1", "password");
         user1.login("user1", "password");
         user2.addUser("user2", "password");
         user2.login("user2", "password");
         user2.joinGroup("group1");
         user1.sendMessage("group1", 1, "Message 1");
 
         System.out.println("\n== BEGIN LOG user1 ==");
         user1.printLog();
         System.out.println("== END LOG user1 ==");
         System.out.println("\n== BEGIN LOG user2 ==");
         user2.printLog();
         System.out.println("== END LOG user2 ==");
 
         chatServer.shutdown();
         chatServer.getDatabaseManager().emptyDatabase();
         System.out.println("=== END TEST Non-Member Broadcast ===\n");
     }
 
     public static void testServerShutdown() throws Exception {
         System.out.println("=== BEGIN TEST Server Shutdown ===");
         ChatServer chatServer = new ChatServer();
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.start();
 
         ChatUser user1 = new ChatUser(chatServer);
         ChatUser user2 = new ChatUser(chatServer);
         ChatUser user3 = new ChatUser(chatServer);
         user1.addUser("user1", "password");
         user1.login("user1", "password");
         user1.addUser("user2", "password");
         user2.login("user2", "password");
         user3.addUser("user3", "password");
         user2.joinGroup("group2");
 
         chatServer.shutdown();
         user1.sendMessage("user2", 1, "Message 1");
         user1.joinGroup("group1");
         user3.login("user3", "password");
 
         user1.logout();
         user2.leaveGroup("group2");
 
         System.out.println("\n== BEGIN LOG user1 ==");
         user1.printLog();
         System.out.println("== END LOG user1 ==");
         System.out.println("\n== BEGIN LOG user2 ==");
         user2.printLog();
         System.out.println("== END LOG user2 ==");
         System.out.println("\n== BEGIN LOG user3 ==");
         user3.printLog();
         System.out.println("== END LOG user3 ==");
 
         chatServer.getDatabaseManager().emptyDatabase();
         System.out.println("=== END TEST Server Shutdown ===\n");
     }
 
     public static void testOfflineMessages() throws Exception {
         System.out.println("=== BEGIN TEST Offline Messages ===");
         ChatServer chatServer = new ChatServer();
         chatServer.getDatabaseManager().emptyDatabase();
         MessageDispatcher messageDispatcher = chatServer.getMessageDispatcher();
         chatServer.start();
 
         ChatUser user1 = new ChatUser(chatServer);
         ChatUser user2 = new ChatUser(chatServer);
         user1.addUser("user1", "password");
         user1.login("user1", "password");
         user2.addUser("user2", "password");
         user1.sendMessage("user2", 1, "Message 1");
         user1.sendMessage("user2", 2, "Message 2");
 
         Thread.currentThread().sleep(100);
         user2.login("user2", "password");
         user2.readLog();
         Thread.currentThread().sleep(100);
 
         System.out.println("\n== BEGIN LOG user1 ==");
         user1.printLog();
         System.out.println("== END LOG user1 ==");
         System.out.println("\n== BEGIN LOG user2 ==");
         user2.printLog();
         System.out.println("== END LOG user2 ==\n");
 
         chatServer.shutdown();
         chatServer.getDatabaseManager().emptyDatabase();
         System.out.println("=== END TEST Offline Messages ===\n");
     }
 
     // Networked Server Tests (Project 2)
 
     public static void testNetworkLogin() throws Exception {
         System.out.println("=== BEGIN TEST Remote Login ===");
         ChatServer chatServer = new ChatServer(8080);
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.start();
 
         Socket user1Socket = new Socket("localhost", 8080);
         ObjectOutputStream user1Requests = new ObjectOutputStream(user1Socket.getOutputStream());
         ObjectInputStream user1Responses = new ObjectInputStream(user1Socket.getInputStream());
 
         user1Requests.writeObject(new ChatClientCommand(CommandType.ADDUSER, "user1", "password"));
         user1Requests.flush();
         Thread.currentThread().sleep(50);
         System.out.println("-> " + CommandType.ADDUSER);
         Thread.currentThread().sleep(50);
         System.out.println("<- " + ((ChatServerResponse)user1Responses.readObject()).responseType);
         Thread.currentThread().sleep(50);
 
         user1Requests.writeObject(new ChatClientCommand(CommandType.LOGIN, "user1", "password"));
         user1Requests.flush();
         Thread.currentThread().sleep(50);
         System.out.println("-> " + CommandType.LOGIN);
         Thread.currentThread().sleep(50);
         System.out.println("<- " + ((ChatServerResponse)user1Responses.readObject()).responseType);
         Thread.currentThread().sleep(50);
 
         Thread.currentThread().sleep(100);
         ChatUser user1 = chatServer.getUserManager().getLoggedInUser("user1");
 
         user1Requests.writeObject(new ChatClientCommand(CommandType.JOIN_GROUP, "group1"));
         user1Requests.flush();
         Thread.currentThread().sleep(50);
         System.out.println("-> " + CommandType.JOIN_GROUP);
         Thread.currentThread().sleep(50);
         System.out.println("<- " + ((ChatServerResponse)user1Responses.readObject()).responseType);
         Thread.currentThread().sleep(50);
 
         user1Requests.writeObject(new ChatClientCommand(CommandType.LEAVE_GROUP, "group1"));
         user1Requests.flush();
         Thread.currentThread().sleep(50);
         System.out.println("-> " + CommandType.LEAVE_GROUP);
         Thread.currentThread().sleep(50);
         System.out.println("<- " + ((ChatServerResponse)user1Responses.readObject()).responseType);
         Thread.currentThread().sleep(50);
 
         user1Requests.writeObject(new ChatClientCommand(CommandType.LOGOUT));
         user1Requests.flush();
         Thread.currentThread().sleep(50);
         System.out.println("-> " + CommandType.LOGOUT);
         Thread.currentThread().sleep(50);
         System.out.println("<- " + ((ChatServerResponse)user1Responses.readObject()).responseType);
         Thread.currentThread().sleep(50);
 
         user1Requests.writeObject(new ChatClientCommand(CommandType.DISCONNECT));
         user1Requests.flush();
         Thread.currentThread().sleep(50);
         System.out.println("-> " + CommandType.DISCONNECT);
         Thread.currentThread().sleep(50);
         System.out.println("<- " + ((ChatServerResponse)user1Responses.readObject()).responseType);
         Thread.currentThread().sleep(50);
 
         user1Requests.close();
         user1Responses.close();
         user1Socket.close();
 
         System.out.println("\n== BEGIN LOG user1 ==");
         user1.printLog();
         System.out.println("== END LOG user1 ==\n");
 
         chatServer.shutdown();
         chatServer.getDatabaseManager().emptyDatabase();
         System.out.println("=== END TEST Remote Login ===\n");
     }
 
     public static void testNetworkLoginTimeout() throws Exception {
         System.out.println("=== BEGIN TEST Login Timeout ===");
         ChatServer chatServer = new ChatServer(8080);
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.start();
 
         Socket user1Socket = new Socket("localhost", 8080);
         ObjectOutputStream user1Requests = new ObjectOutputStream(user1Socket.getOutputStream());
         ObjectInputStream user1Responses = new ObjectInputStream(user1Socket.getInputStream());
 
         user1Requests.writeObject(new ChatClientCommand(CommandType.ADDUSER, "user1", "password"));
         user1Requests.flush();
         Thread.currentThread().sleep(50);
         System.out.println("-> " + CommandType.ADDUSER);
         Thread.currentThread().sleep(50);
         System.out.println("<- " + ((ChatServerResponse)user1Responses.readObject()).responseType);
         Thread.currentThread().sleep(50);
 
         user1Requests.writeObject(new ChatClientCommand(CommandType.LOGIN, "user1", "password"));
         user1Requests.flush();
         Thread.currentThread().sleep(50);
         System.out.println("-> " + CommandType.LOGIN);
         Thread.currentThread().sleep(50);
         System.out.println("<- " + ((ChatServerResponse)user1Responses.readObject()).responseType);
         Thread.currentThread().sleep(50);
 
         Thread.currentThread().sleep(100);
         ChatUser user1 = chatServer.getUserManager().getLoggedInUser("user1");
 
         user1Requests.writeObject(new ChatClientCommand(CommandType.LOGOUT));
         user1Requests.flush();
         Thread.currentThread().sleep(50);
         System.out.println("-> " + CommandType.LOGOUT);
         Thread.currentThread().sleep(50);
         System.out.println("<- " + ((ChatServerResponse)user1Responses.readObject()).responseType);
         Thread.currentThread().sleep(50);
 
         Thread.currentThread().sleep(21000);
         System.out.println("<- " + ((ChatServerResponse)user1Responses.readObject()).responseType);
         user1Requests.close();
         user1Responses.close();
         user1Socket.close();
 
         System.out.println("\n== BEGIN LOG user1 ==");
         user1.printLog();
         System.out.println("== END LOG user1 ==\n");
 
         chatServer.shutdown();
         chatServer.getDatabaseManager().emptyDatabase();
         System.out.println("=== END TEST Login Timeout ===\n");
     }
 
     public static void testNetworkUnexpectedDisconnectBeforeLogin() throws Exception {
         System.out.println("=== BEGIN TEST Unexpected Disconnect Before Login ===");
         ChatServer chatServer = new ChatServer(8080);
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.start();
 
         Socket user1Socket = new Socket("localhost", 8080);
         ObjectOutputStream user1Requests = new ObjectOutputStream(user1Socket.getOutputStream());
         ObjectInputStream user1Responses = new ObjectInputStream(user1Socket.getInputStream());
 
         user1Requests.writeObject(new ChatClientCommand(CommandType.ADDUSER, "user1", "password"));
         user1Requests.flush();
         Thread.currentThread().sleep(50);
         System.out.println("-> " + CommandType.ADDUSER);
         Thread.currentThread().sleep(50);
         System.out.println("<- " + ((ChatServerResponse)user1Responses.readObject()).responseType);
         Thread.currentThread().sleep(50);
 
         user1Requests.writeObject(new ChatClientCommand(CommandType.LOGIN, "user1", "password"));
         user1Requests.flush();
         Thread.currentThread().sleep(50);
         System.out.println("-> " + CommandType.LOGIN);
         Thread.currentThread().sleep(50);
         System.out.println("<- " + ((ChatServerResponse)user1Responses.readObject()).responseType);
         Thread.currentThread().sleep(50);
 
         Thread.currentThread().sleep(100);
         ChatUser user1 = chatServer.getUserManager().getLoggedInUser("user1");
 
         user1Requests.writeObject(new ChatClientCommand(CommandType.LOGOUT));
         user1Requests.flush();
         Thread.currentThread().sleep(50);
         System.out.println("-> " + CommandType.LOGOUT);
         Thread.currentThread().sleep(50);
         System.out.println("<- " + ((ChatServerResponse)user1Responses.readObject()).responseType);
         Thread.currentThread().sleep(50);
 
         user1Requests.close();
         user1Responses.close();
         user1Socket.close();
         Thread.currentThread().sleep(50);
 
         System.out.println("\n== BEGIN LOG user1 ==");
         user1.printLog();
         System.out.println("== END LOG user1 ==\n");
 
         chatServer.shutdown();
         chatServer.getDatabaseManager().emptyDatabase();
         System.out.println("=== END TEST Unexpected Disconnect Before Login ===\n");
     }
 
     public static void testNetworkUnexpectedDisconnectAfterLogin() throws Exception {
         System.out.println("=== BEGIN TEST Unexpected Disconnect After Login ===");
         ChatServer chatServer = new ChatServer(8080);
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.start();
 
         Socket user1Socket = new Socket("localhost", 8080);
         ObjectOutputStream user1Requests = new ObjectOutputStream(user1Socket.getOutputStream());
         ObjectInputStream user1Responses = new ObjectInputStream(user1Socket.getInputStream());
 
         user1Requests.writeObject(new ChatClientCommand(CommandType.ADDUSER, "user1", "password"));
         user1Requests.flush();
         Thread.currentThread().sleep(50);
         System.out.println("-> " + CommandType.ADDUSER);
         Thread.currentThread().sleep(50);
         System.out.println("<- " + ((ChatServerResponse)user1Responses.readObject()).responseType);
         Thread.currentThread().sleep(50);
 
         user1Requests.writeObject(new ChatClientCommand(CommandType.LOGIN, "user1", "password"));
         user1Requests.flush();
         Thread.currentThread().sleep(50);
         System.out.println("-> " + CommandType.LOGIN);
         Thread.currentThread().sleep(50);
         System.out.println("<- " + ((ChatServerResponse)user1Responses.readObject()).responseType);
         Thread.currentThread().sleep(50);
 
         Thread.currentThread().sleep(100);
         ChatUser user1 = chatServer.getUserManager().getLoggedInUser("user1");
 
         user1Requests.close();
         user1Responses.close();
         user1Socket.close();
         Thread.currentThread().sleep(50);
 
         System.out.println("\n== BEGIN LOG user1 ==");
         user1.printLog();
         System.out.println("== END LOG user1 ==\n");
 
         chatServer.shutdown();
         chatServer.getDatabaseManager().emptyDatabase();
         System.out.println("=== END TEST Unexpected Disconnect After Login ===\n");
     }
 
     public static void testNetworkLoginQueue() throws Exception {
         System.out.println("=== BEGIN TEST Login Queue ===");
         ChatServer chatServer = new ChatServer(8080);
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.start();
 
         ChatUser[] users = new ChatUser[101];
         for (int i = 1; i <= 100; i++) {
             users[i] = new ChatUser(chatServer);
             users[i].addUser("user" + Integer.toString(i), "password");
             users[i].login("user" + Integer.toString(i), "password");
         }
 
         Socket user1Socket = new Socket("localhost", 8080);
         ObjectOutputStream user1Requests = new ObjectOutputStream(user1Socket.getOutputStream());
         ObjectInputStream user1Responses = new ObjectInputStream(user1Socket.getInputStream());
 
         System.out.println("\n== BEGIN Simulated Client ==");
         user1Requests.writeObject(new ChatClientCommand(CommandType.ADDUSER, "user101", "password"));
         user1Requests.flush();
         System.out.println("-> " + CommandType.ADDUSER);
         Thread.currentThread().sleep(50);
         System.out.println("<- " + ((ChatServerResponse)user1Responses.readObject()).responseType);
 
         user1Requests.writeObject(new ChatClientCommand(CommandType.LOGIN, "user101", "password"));
         user1Requests.flush();
         System.out.println("-> " + CommandType.LOGIN);
         Thread.currentThread().sleep(50);
         System.out.println("<- " + ((ChatServerResponse)user1Responses.readObject()).responseType);
 
         users[1].logout();
         Thread.currentThread().sleep(50);
         System.out.println("<- " + ((ChatServerResponse)user1Responses.readObject()).responseType);
 
         ChatUser user101 = chatServer.getUserManager().getLoggedInUser("user101");
 
         user1Requests.writeObject(new ChatClientCommand(CommandType.DISCONNECT));
         user1Requests.flush();
         System.out.println("-> " + CommandType.DISCONNECT);
         Thread.currentThread().sleep(50);
 
         user1Requests.close();
         user1Responses.close();
         user1Socket.close();
         Thread.currentThread().sleep(50);
         System.out.println("== END Simulated Client ==\n");
 
         for (int i = 1; i <= 100; i++) {
             System.out.println("\n== BEGIN LOG user[" + Integer.toString(i) + "] ==");
             users[i].printLog();
             System.out.println("== END LOG user[" + Integer.toString(i) + "] ==");
         }
         System.out.println("\n== BEGIN LOG user101 ==");
         user101.printLog();
         System.out.println("== END LOG user101 ==");
 
         chatServer.shutdown();
         chatServer.getDatabaseManager().emptyDatabase();
         System.out.println("=== END TEST Login Queue ===\n");
     }
 
     public static void testNetworkSendReceive() throws Exception {
         System.out.println("=== BEGIN TEST Send and Receive ===");
         ChatServer chatServer = new ChatServer(8080);
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.start();
 
         Socket user1Socket = new Socket("localhost", 8080);
         ObjectOutputStream user1Requests = new ObjectOutputStream(user1Socket.getOutputStream());
         ObjectInputStream user1Responses = new ObjectInputStream(user1Socket.getInputStream());
 
         System.out.println("\n== BEGIN Simulated Client ==");
         user1Requests.writeObject(new ChatClientCommand(CommandType.ADDUSER, "user1", "password"));
         user1Requests.flush();
         System.out.println("-> " + CommandType.ADDUSER);
         Thread.currentThread().sleep(50);
         System.out.println("<- " + ((ChatServerResponse)user1Responses.readObject()).responseType);
         Thread.currentThread().sleep(50);
 
         user1Requests.writeObject(new ChatClientCommand(CommandType.LOGIN, "user1", "password"));
         user1Requests.flush();
         System.out.println("-> " + CommandType.LOGIN);
         Thread.currentThread().sleep(50);
         System.out.println("<- " + ((ChatServerResponse)user1Responses.readObject()).responseType);
         Thread.currentThread().sleep(50);
 
         Thread.currentThread().sleep(100);
         ChatUser user1 = chatServer.getUserManager().getLoggedInUser("user1");
 
         ChatUser user2 = new ChatUser(chatServer);
         user2.addUser("user2", "password");
         user2.login("user2", "password");
 
         user2.sendMessage("user1", 1, "Unicast 1");
         Thread.currentThread().sleep(50);
         System.out.println("<- " + ((ChatServerResponse)user1Responses.readObject()).responseType);
         Thread.currentThread().sleep(50);
 
         user1Requests.writeObject(new ChatClientCommand(CommandType.SEND_MESSAGE, "user2", 1, "Unicast 2"));
         user1Requests.flush();
         System.out.println("-> " + CommandType.SEND_MESSAGE);
         Thread.currentThread().sleep(50);
         System.out.println("<- " + ((ChatServerResponse)user1Responses.readObject()).responseType);
         Thread.currentThread().sleep(50);
 
         user1Requests.writeObject(new ChatClientCommand(CommandType.JOIN_GROUP, "group1"));
         user1Requests.flush();
         System.out.println("-> " + CommandType.JOIN_GROUP);
         Thread.currentThread().sleep(50);
         System.out.println("<- " + ((ChatServerResponse)user1Responses.readObject()).responseType);
         Thread.currentThread().sleep(50);
 
         user1Requests.writeObject(new ChatClientCommand(CommandType.SEND_MESSAGE, "group1", 1, "Broadcast 1"));
         user1Requests.flush();
         System.out.println("-> " + CommandType.SEND_MESSAGE);
         Thread.currentThread().sleep(50);
         System.out.println("<- " + ((ChatServerResponse)user1Responses.readObject()).responseType);
         Thread.currentThread().sleep(50);
         System.out.println("<- " + ((ChatServerResponse)user1Responses.readObject()).responseType);
         Thread.currentThread().sleep(50);
 
         user1Requests.writeObject(new ChatClientCommand(CommandType.LEAVE_GROUP, "group1"));
         user1Requests.flush();
         System.out.println("-> " + CommandType.LEAVE_GROUP);
         Thread.currentThread().sleep(50);
         System.out.println("<- " + ((ChatServerResponse)user1Responses.readObject()).responseType);
         Thread.currentThread().sleep(50);
 
         user1Requests.writeObject(new ChatClientCommand(CommandType.SEND_MESSAGE, "group1", 1, "Broadcast 1"));
         user1Requests.flush();
         System.out.println("-> " + CommandType.SEND_MESSAGE);
         Thread.currentThread().sleep(50);
         System.out.println("<- " + ((ChatServerResponse)user1Responses.readObject()).responseType);
         Thread.currentThread().sleep(50);
 
         user1Requests.writeObject(new ChatClientCommand(CommandType.LOGOUT));
         user1Requests.flush();
         System.out.println("-> " + CommandType.LOGOUT);
         Thread.currentThread().sleep(50);
         System.out.println("<- " + ((ChatServerResponse)user1Responses.readObject()).responseType);
         Thread.currentThread().sleep(50);
 
         user1Requests.writeObject(new ChatClientCommand(CommandType.DISCONNECT));
         user1Requests.flush();
         System.out.println("-> " + CommandType.DISCONNECT);
         Thread.currentThread().sleep(50);
 
         user1Requests.close();
         user1Responses.close();
         user1Socket.close();
         Thread.currentThread().sleep(50);
         System.out.println("== END Simulated Client ==\n");
 
         System.out.println("== BEGIN LOG user1 ==");
         user1.printLog();
         System.out.println("== END LOG user1 ==\n");
         System.out.println("== BEGIN LOG user2 ==");
         user2.printLog();
         System.out.println("== END LOG user2 ==\n");
 
         chatServer.shutdown();
         chatServer.getDatabaseManager().emptyDatabase();
         System.out.println("=== END TEST Send and Receive ===\n");
     }
 
     public static void testNetworkReadlog() throws Exception {
         System.out.println("=== BEGIN TEST readlog ===");
         ChatServer chatServer = new ChatServer(8080);
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.start();
 
         Socket user2Socket = new Socket("localhost", 8080);
         ObjectOutputStream user2Requests = new ObjectOutputStream(user2Socket.getOutputStream());
         ObjectInputStream user2Responses = new ObjectInputStream(user2Socket.getInputStream());
 
         System.out.println("\n== BEGIN Simulated Client ==");
         ChatUser user1 = new ChatUser(chatServer);
         user1.addUser("user1", "password");
         user1.login("user1", "password");
 
         user2Requests.writeObject(new ChatClientCommand(CommandType.ADDUSER, "user2", "password"));
         user2Requests.flush();
         System.out.println("-> " + CommandType.ADDUSER);
         Thread.currentThread().sleep(50);
         System.out.println("<- " + ((ChatServerResponse)user2Responses.readObject()).responseType);
         Thread.currentThread().sleep(50);
 
         user1.sendMessage("user2", 1, "Unicast 1");
         user1.sendMessage("user2", 2, "Unicast 2");
         Thread.currentThread().sleep(2000);
 
         user2Requests.writeObject(new ChatClientCommand(CommandType.LOGIN, "user2", "password"));
         user2Requests.flush();
         System.out.println("-> " + CommandType.LOGIN);
         Thread.currentThread().sleep(50);
         System.out.println("<- " + ((ChatServerResponse)user2Responses.readObject()).responseType);
         Thread.currentThread().sleep(50);
 
         user2Requests.writeObject(new ChatClientCommand(CommandType.READLOG));
         user2Requests.flush();
         System.out.println("-> " + CommandType.READLOG);
         Thread.currentThread().sleep(1000);
         System.out.println("<- " + ((ChatServerResponse)user2Responses.readObject()).responseType);
         Thread.currentThread().sleep(50);
         System.out.println("<- " + ((ChatServerResponse)user2Responses.readObject()).responseType);
         Thread.currentThread().sleep(50);
 
         user2Requests.close();
         user2Responses.close();
         user2Socket.close();
         Thread.currentThread().sleep(50);
         System.out.println("== END Simulated Client ==\n");
 
         chatServer.shutdown();
         //chatServer.getDatabaseManager().emptyDatabase();
         System.out.println("=== END TEST readlog ===\n");
     }
 
     public static void testClientBasic() throws Exception {
         System.out.println("=== BEGIN TEST Client Basic ===");
         ChatServer chatServer = new ChatServer(8080);
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.start();
 
         String commands = "" +
             "connect localhost:8080\n" +
             "adduser user1 password\n" +
             "login user1 password\n" +
             "join group1\n" +
             "send group1 1 \"Message\"\n" +
             "leave group1\n" +
             "join user1\n" +
             "leave user1\n" +
             "logout\n" +
             "disconnect";
         BufferedReader input = new BufferedReader(new StringReader(commands));
         PrintWriter output = new PrintWriter(System.out, true);
         ChatClient chatClient = new ChatClient(input, output);
         chatClient.start();
         Thread.currentThread().sleep(5000);
 
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.shutdown();
         System.out.println("=== END TEST Client Basic ===\n");
     }
 
     public static void testClientLogout() throws Exception {
         System.out.println("=== BEGIN TEST Client Logout ===");
         ChatServer chatServer = new ChatServer(8080);
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.start();
 
         String commands = "" +
             "connect localhost:8080\n" +
             "adduser user1 password\n" +
             "login user1 password\n" +
             "join group1\n" +
             "join group2\n" +
             "join group3\n" +
             "logout";
         BufferedReader input = new BufferedReader(new StringReader(commands));
         PrintWriter output = new PrintWriter(System.out, true);
         ChatClient chatClient = new ChatClient(input, output);
         chatClient.start();
         Thread.currentThread().sleep(2000);
 
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.shutdown();
         System.out.println("=== END TEST Client Logout ===\n");
     }
 
     public static void testClientDisconnect() throws Exception {
         System.out.println("=== BEGIN TEST Client Disconnect ===");
         ChatServer chatServer = new ChatServer(8080);
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.start();
 
         String commands = "" +
             "connect localhost:8080\n" +
             "adduser user1 password\n" +
             "login user1 password\n" +
             "join group1\n" +
             "join group2\n" +
             "join group3\n" +
             "disconnect";
         BufferedReader input = new BufferedReader(new StringReader(commands));
         PrintWriter output = new PrintWriter(System.out, true);
         ChatClient chatClient = new ChatClient(input, output);
         chatClient.start();
         Thread.currentThread().sleep(2000);
 
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.shutdown();
         System.out.println("=== END TEST Disconnect ===\n");
     }
 
     public static void testClientReconnect() throws Exception {
         System.out.println("=== BEGIN TEST Client Reconnect ===");
         ChatServer chatServer = new ChatServer(8080);
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.start();
 
         String commands = "" +
             "connect localhost:8080\n" +
             "adduser user1 password\n" +
             "login user1 password\n" +
             "join group1\n" +
             "leave group1\n" +
             "logout\n" +
             "disconnect\n" +
             "connect localhost:8080\n" +
             "login user1 password\n" +
             "join group1\n" +
             "logout\n" +
             "disconnect";
         BufferedReader input = new BufferedReader(new StringReader(commands));
         PrintWriter output = new PrintWriter(System.out, true);
         ChatClient chatClient = new ChatClient(input, output);
         chatClient.start();
         Thread.currentThread().sleep(5000);
 
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.shutdown();
         System.out.println("=== END TEST Client Reconnect ===\n");
     }
 
     public static void testClientTimeout() throws Exception {
         System.out.println("=== BEGIN TEST Client Basic ===");
         ChatServer chatServer = new ChatServer(8080);
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.start();
 
         String commands = "" +
             "connect localhost:8080\n" +
             "sleep 21000";
         BufferedReader input = new BufferedReader(new StringReader(commands));
         PrintWriter output = new PrintWriter(System.out, true);
         ChatClient chatClient = new ChatClient(input, output);
         chatClient.start();
         Thread.currentThread().sleep(22000);
 
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.shutdown();
         System.out.println("=== END TEST Client Basic ===\n");
     }
 
     public static void testClientLoginQueue() throws Exception {
         System.out.println("=== BEGIN TEST Client Login Queue ===");
         ChatServer chatServer = new ChatServer(8080);
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.start();
 
         ChatUser[] users = new ChatUser[101];
         for (int i = 1; i <= 100; i++) {
             users[i] = new ChatUser(chatServer);
             users[i].addUser("user" + Integer.toString(i), "password");
             users[i].login("user" + Integer.toString(i), "password");
         }
 
         System.out.println("\n== BEGIN user101 ==");
         String commands = "" +
             "connect localhost:8080\n" +
             "adduser user101 password\n" +
             "login user101 password\n" +
             "sleep 2000";
         BufferedReader input = new BufferedReader(new StringReader(commands));
         PrintWriter output = new PrintWriter(System.out, true);
         ChatClient chatClient = new ChatClient(input, output);
         chatClient.start();
         Thread.currentThread().sleep(1000);
 
         users[1].logout();
         Thread.currentThread().sleep(2000);
         System.out.println("== END user101 ==");
 
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.shutdown();
         System.out.println("=== END TEST Client Login Queue ===\n");
     }
 
     public static void testClientDisconnectsWhileInLoginWaitQueue() throws Exception {
         System.out.println("=== BEGIN TEST Client Disconnects While In Login Wait Queue ===");
         ChatServer chatServer = new ChatServer(8080);
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.start();
 
         ChatUser[] users = new ChatUser[101];
         for (int i = 1; i <= 100; i++) {
             users[i] = new ChatUser(chatServer);
             users[i].addUser("user" + Integer.toString(i), "password");
             users[i].login("user" + Integer.toString(i), "password");
         }
 
         Socket user1Socket = new Socket("localhost", 8080);
         ObjectOutputStream user1Requests = new ObjectOutputStream(user1Socket.getOutputStream());
         ObjectInputStream user1Responses = new ObjectInputStream(user1Socket.getInputStream());
 
         System.out.println("\n== BEGIN Simulated Client ==");
         user1Requests.writeObject(new ChatClientCommand(CommandType.ADDUSER, "user101", "password"));
         user1Requests.flush();
         System.out.println("-> " + CommandType.ADDUSER);
         Thread.currentThread().sleep(50);
         System.out.println("<- " + ((ChatServerResponse)user1Responses.readObject()).responseType);
 
         user1Requests.writeObject(new ChatClientCommand(CommandType.LOGIN, "user101", "password"));
         user1Requests.flush();
         System.out.println("-> " + CommandType.LOGIN);
         Thread.currentThread().sleep(50);
         System.out.println("<- " + ((ChatServerResponse)user1Responses.readObject()).responseType);
 
         user1Requests.close();
         user1Responses.close();
         user1Socket.close();
 
         Thread.currentThread().sleep(50);
         users[99].logout();
 
         Thread.currentThread().sleep(100);
         ChatUser user101 = chatServer.getUserManager().getLoggedInUser("user101");
         if (user101 == null)
             System.out.println("user101 was NOT added to chat server.  Hooray, test passed!");
 
         Thread.currentThread().sleep(50);
         System.out.println("== END Simulated Client ==\n");
 
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.shutdown();
         System.out.println("=== END TEST Client Disconnects While In Login Wait Queue ===");
     }
 
     public static void testClientDoesNotNormallyTimeout() throws Exception {
         System.out.println("=== BEGIN TEST Client Does Not Normally Timeout ===");
         ChatServer chatServer = new ChatServer(8080);
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.start();
 
         ChatUser[] users = new ChatUser[101];
         for (int i = 1; i <= 100; i++) {
             users[i] = new ChatUser(chatServer);
             users[i].addUser("user" + Integer.toString(i), "password");
             users[i].login("user" + Integer.toString(i), "password");
         }
 
         String commands = "" +
             "connect localhost:8080\n" +
             "adduser user101 password\n" +
             "login user101 password\n" +
             "sleep 25000\n" +
             "join group1\n" +
             "sleep 25000\n";
         BufferedReader input = new BufferedReader(new StringReader(commands));
         PrintWriter output = new PrintWriter(System.out, true);
         ChatClient chatClient = new ChatClient(input, output);
         chatClient.start();
 
         Thread.currentThread().sleep(3000);
 
         users[100].logout();
 
         Thread.currentThread().sleep(55000);
 
         System.out.println("\n== BEGIN LOG user101 ==");
         ChatUser user101 = chatServer.getUserManager().getLoggedInUser("user101");
         user101.printLog();
         System.out.println("== END LOG user101 ==\n");
 
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.shutdown();
         System.out.println("=== END TEST Client Does Not Normally Timeout ===\n");
     }
 
     public static void testClientDisconnectHandledAfterLogoff() throws Exception {
         System.out.println("=== BEGIN TEST Client Disconnect is Handled After Logout ===");
         ChatServer chatServer = new ChatServer(8080);
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.start();
 
         String commands = "" +
             "connect localhost:8080\n" +
             "adduser user1 password\n" +
             "login user1 password\n" +
             "logout\n";
         BufferedReader input = new BufferedReader(new StringReader(commands));
         PrintWriter output = new PrintWriter(System.out, true);
         ChatClient chatClient = new ChatClient(input, output);
         chatClient.start();
 
         Thread.currentThread().sleep(21000);
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.shutdown();
         System.out.println("=== END TEST Client Disconnect is Handled After Logout ===");
     }
 
     public static void testTimerRestartedAfterEveryFailedLoginAttempt() throws Exception {
         System.out.println("=== BEGIN TEST Timer is Restarted After Every Failed Login Attempt ===");
         ChatServer chatServer = new ChatServer(8080);
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.start();
 
         ChatUser user;
         for(int i=1; i<=3; i++) {
             user = new ChatUser(chatServer);
             user.addUser("user" + Integer.toString(i), "password");
             user.login("user" + Integer.toString(i), "password");
         }
 
         String commands = "" +
             "connect localhost:8080\n" +
             "sleep 19000\n" +
             "login user1 wrongpassword\n" +
             "sleep 19000\n" +
             "login user2 wrongpassword\n" +
             "sleep 19000\n" +
             "login user3 wrongpassword\n" +
             "login user4 wrongpassword\n" +
             "logout\n";
         BufferedReader input = new BufferedReader(new StringReader(commands));
         PrintWriter output = new PrintWriter(System.out, true);
         ChatClient chatClient = new ChatClient(input, output);
         chatClient.start();
 
         Thread.currentThread().sleep(60000);
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.shutdown();
         System.out.println("=== END TEST Timer is Restarted After Every Failed Login Attempt ===");
     }
 
     public static void testCertainClientCommandsRejectedWhenNotConnectedOrLoggedIn() throws Exception {
         System.out.println("=== BEGIN TEST Reject Certain Client Commands When Disconnected or Not Logged In ===");
         ChatServer chatServer = new ChatServer(8080);
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.start();
 
         ChatUser user1 = new ChatUser(chatServer);
         user1.addUser("user1", "password");
         user1.login("user1", "password");
 
         String commands = "" +
             "adduser user2 password\n" +
             "login user2 password\n" +
             "join group1\n" +
             "leave group1\n" +
             "send user1 1 \"Hello user1!\"\n" +
             "disconnect\n" +
             "sleep 500\n" +
             "connect localhost:8080\n" +
             "adduser user1 password\n" +
             "login user1 password\n" +
             "join group1\n" +
             "leave group1\n" +
             "send user2 1 \"Hello user1!\"\n" +
             "disconnect\n";
         BufferedReader input = new BufferedReader(new StringReader(commands));
         PrintWriter output = new PrintWriter(System.out, true);
         ChatClient chatClient = new ChatClient(input, output);
         chatClient.start();
 
         Thread.currentThread().sleep(20000);
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.shutdown();
         System.out.println("=== END TEST Reject Certain Client Commands When Disconnected or Not Logged In ===");
 
     }
 
     public static void testInvalidClientCommandsAreSkipped() throws Exception {
         System.out.println("=== BEGIN TEST Invalid Client Commands Are Gracefully Skipped ===");
         ChatServer chatServer = new ChatServer(8080);
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.start();
 
         String commands = "" +
             "connect localhost:8080\n" +
             "adduser user1 password\n" +
             "login user1 password\n" +
             "joing group1\n" +
             "leeve group1\n" +
             "joing group1\n" +
             "join group1\n" +
             "loggouts\n" +
             "Chuck Norris\n" +
             "logout\n" +
             "disconnect\n";
         BufferedReader input = new BufferedReader(new StringReader(commands));
         PrintWriter output = new PrintWriter(System.out, true);
         ChatClient chatClient = new ChatClient(input, output);
         chatClient.start();
 
         Thread.currentThread().sleep(5000);
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.shutdown();
         System.out.println("=== BEGIN TEST Invalid Client Commands Are Gracefully Skipped ===");
     }
 
     public static void testClientAdduserLogin() throws Exception {
         System.out.println("=== BEGIN TEST Client adduser login ===");
         ChatServer chatServer = new ChatServer(8080);
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.start();
 
         ChatUser user1 = new ChatUser(chatServer);
         user1.addUser("user1", "password");
         user1.login("user1", "password");
 
         String commands = "" +
             "connect localhost:8080\n" +
             "adduser user1 password\n" +
             "login user1 password\n" +
             "login user2 password\n" +
             "adduser user2 password\n" +
             "login user2 wrongpassword\n" +
             "login user2 password\n" +
             "join group1\n" +
             "adduser group1 password\n" +
             "logout\n" +
             "disconnect";
         BufferedReader input = new BufferedReader(new StringReader(commands));
         PrintWriter output = new PrintWriter(System.out, true);
         ChatClient chatClient = new ChatClient(input, output);
         chatClient.start();
         Thread.currentThread().sleep(5000);
 
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.shutdown();
         System.out.println("=== END TEST Client adduser login ===\n");
     }
 
     public static void testClientMessaging() throws Exception {
         System.out.println("=== BEGIN TEST Client Messaging ===");
         ChatServer chatServer = new ChatServer(8080);
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.start();
 
         ChatUser user1 = new ChatUser(chatServer);
         user1.addUser("user1", "password");
         user1.login("user1", "password");
         user1.joinGroup("group1");
 
         String commands = "" +
             "connect localhost:8080\n" +
             "adduser user2 password\n" +
             "login user2 password\n" +
             "join group1\n" +
             "sleep 4000\n" +
             "logout\n" +
             "disconnect";
         BufferedReader input = new BufferedReader(new StringReader(commands));
         PrintWriter output = new PrintWriter(System.out, true);
         ChatClient chatClient = new ChatClient(input, output);
         chatClient.start();
         Thread.currentThread().sleep(2000);
 
         user1.sendMessage("user2", 1, "Unicast 1");
         user1.sendMessage("group1", 1, "Broadcast 1");
         Thread.currentThread().sleep(5000);
 
         System.out.println("\n== BEGIN user1 log ==") ;
         user1.printLog();
         System.out.println("== END user1 log ==\n");
 
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.shutdown();
         System.out.println("=== END TEST Client Messaging ===\n");
     }
 
     public static void testClientReadlog() throws Exception {
         System.out.println("=== BEGIN TEST Client readlog ===");
         ChatServer chatServer = new ChatServer(8080);
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.start();
 
         ChatUser user1 = new ChatUser(chatServer);
         user1.addUser("user1", "password");
         user1.login("user1", "password");
         user1.joinGroup("group1");
 
         String commands = "" +
             "connect localhost:8080\n" +
             "adduser user2 password\n" +
             "sleep 3000\n" +
             "login user2 password\n" +
             "readlog\n" +
             "join group1\n" +
             "logout\n" +
             "disconnect\n" +
             "sleep 3000\n" +
             "connect localhost:8080\n" +
             "login user2 password\n" +
             "readlog\n" +
             "readlog\n" +
             "disconnect";
         BufferedReader input = new BufferedReader(new StringReader(commands));
         PrintWriter output = new PrintWriter(System.out, true);
         ChatClient chatClient = new ChatClient(input, output);
         chatClient.start();
         Thread.currentThread().sleep(1000);
 
         user1.sendMessage("user2", 1, "Unicast 1");
         user1.sendMessage("user2", 2, "Unicast 2");
         Thread.currentThread().sleep(3000);
         user1.sendMessage("group1", 3, "Broadcast 1");
         user1.sendMessage("user2", 4, "Unicast 3");
         Thread.currentThread().sleep(5000);
 
         System.out.println("\n== BEGIN user1 log ==") ;
         user1.printLog();
         System.out.println("== END user1 log ==\n");
 
         chatServer.getDatabaseManager().emptyDatabase();
         chatServer.shutdown();
         System.out.println("=== END TEST Client readlog ===\n");
     }
 }
