 package edu.berkeley.cs.cs162;
 
 import java.io.*;
 import java.net.*;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.concurrent.LinkedBlockingQueue;
 
 public class ChatUser extends Thread {
     private ChatServer chatServer;
     private ChatUserResponder responder;
     private Socket socket;
     private ObjectInputStream input;
     private boolean networked;
     private String loginName;
     private boolean loggedIn;
     private int sendCount;
     private LinkedBlockingQueue<ChatServerResponse> pendingResponses;
     private LinkedBlockingQueue<String> log;
     private SimpleDateFormat dateFormatter;
     private LoginTimeout loginTimeout;
 
     public ChatUser(ChatServer chatServer) {
         this.chatServer = chatServer;
         this.networked = false;
         this.loginName = null;
         this.loggedIn = false;
         this.pendingResponses = new LinkedBlockingQueue<ChatServerResponse>();
         this.log = new LinkedBlockingQueue<String>();
         this.dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
         this.sendCount = 0;
     }
 
     public ChatUser(ChatServer chatServer, Socket socket) {
         this(chatServer);
         this.networked = true;
         this.socket = socket;
         this.responder = new ChatUserResponder(this, socket, pendingResponses);
         try {
             this.input = new ObjectInputStream(socket.getInputStream());
         } catch (IOException e) {
             try {
                 socket.close();
             } catch(IOException f) {
             }
         }
         if (networked) {
             loginTimeout = new LoginTimeout(this, 20);
         }
     }
 
     public void start() {
         responder.start();
         super.start();
     }
 
     public void shutdown() {
         log.offer(dateFormatter.format(Calendar.getInstance().getTime()) + " | Shutting Down");
         if (networked) {
             responder.shutdown();
         }
     }
 
     public void run() {
         try {
             while (true) {
                 executeCommand((ChatClientCommand)input.readObject());
             }
         } catch (Exception e) {
         } finally {
             if (loggedIn) {
                 logout();
             }
             try {
                 input.close();
                 socket.close();
             } catch (Exception f) {
             }
         }
     }
 
     public String getUserName() {
         return loginName;
     }
 
     public void disconnect() {
         log.offer(dateFormatter.format(Calendar.getInstance().getTime()) + " | Disconnected");
     }
 
     public void executeCommand(ChatClientCommand command) {
         ChatServerResponse response;
         switch (command.commandType) {
             case LOGIN:
                 response = login(command.string);
                 break;
             case LOGOUT:
                 response = logout();
                 break;
             case JOIN_GROUP:
                 response = joinGroup(command.string);
                 break;
             case LEAVE_GROUP:
                 response = leaveGroup(command.string);
                 break;
             case SEND_MESSAGE:
                 response = sendMessage(command.message);
                 break;
             default:
                 response = new ChatServerResponse(ResponseType.COMMAND_NOT_FOUND);
                 break;
         }
         response.command = command;
         pendingResponses.offer(response);
     }
 
     public ChatServerResponse login(String userName) {
         Date time = Calendar.getInstance().getTime();
         if (networked && loginTimeout != null) {
             loginTimeout.cancel();
             loginTimeout = null;
         }
         if (loggedIn) {
             logout();
         }
         loginName = userName;
         ChatServerResponse response = chatServer.login(this);
         switch (response.responseType) {
             case SHUTTING_DOWN:
                 TestChatServer.logUserLoginFailed(userName, time, LoginError.USER_REJECTED);
                 log.offer(dateFormatter.format(time) + " | Login Failure | ChatServer is shutting down.");
                 break;
             case USER_QUEUED:
                 log.offer(dateFormatter.format(time) + " | Login Queued | Placed on waiting queue.");
                 break;
             case USER_CAPACITY_REACHED:
                 TestChatServer.logUserLoginFailed(userName, time, LoginError.USER_DROPPED);
                 log.offer(dateFormatter.format(time) + " | Login Failure | ChatServer is full.");
                 break;
             case NAME_CONFLICT:
                 TestChatServer.logUserLoginFailed(userName, time, LoginError.USER_REJECTED);
                 log.offer(dateFormatter.format(time) + " | Login Failure | Name already taken.");
                 break;
             case USER_ADDED:
                 loggedIn = true;
                 TestChatServer.logUserLogin(userName, time);
                 log.offer(dateFormatter.format(time) + " | Login Success | Logged in as " + userName);
                 break;
         }
         return response;
     }
 
     public void loggedIn() {
         loggedIn = true;
         Date time = Calendar.getInstance().getTime();
         TestChatServer.logUserLogin(loginName, time);
         log.offer(dateFormatter.format(time) + " | Login Success | Logged in as " + loginName);
         pendingResponses.offer(new ChatServerResponse(ResponseType.USER_ADDED));
     }
 
     public ChatServerResponse logout() {
         Date time = Calendar.getInstance().getTime();
        if (loggedIn) {
             ChatServerResponse response = chatServer.logoff(this);
             switch (response.responseType) {
                 case USER_NOT_FOUND:
                     log.offer(dateFormatter.format(time) + " | Logout Failure | " + loginName + " is not logged in.");
                     break;
                 case USER_REMOVED:
                     TestChatServer.logUserLogout(loginName, time);
                     log.offer(dateFormatter.format(time) + " | Logout Success | " + loginName + " has been logged out.");
                     break;
             }
             loggedIn = false;
             if (networked) {
                 loginTimeout = new LoginTimeout(this, 20);
             }
             return response;
         } else {
             log.offer(dateFormatter.format(time) + " | Logout Failure | Not logged in.");
             return new ChatServerResponse(ResponseType.USER_NOT_FOUND);
         }
     }
 
     public ChatServerResponse joinGroup(String groupName) {
         Date time = Calendar.getInstance().getTime();
         if (loggedIn) {
             ChatServerResponse response = chatServer.joinGroup(this, groupName);
             switch (response.responseType) {
                 case SHUTTING_DOWN:
                     log.offer(dateFormatter.format(time) + " | Join Group Failure | ChatServer is shutting down.");
                     break;
                 case USER_NOT_FOUND:
                     log.offer(dateFormatter.format(time) + " | Join Group Failure | " + loginName + " is not logged in.");
                     loggedIn = false;
                     break;
                 case USER_ADDED_TO_NEW_GROUP:
                     TestChatServer.logUserJoinGroup(groupName, loginName, time);
                     log.offer(dateFormatter.format(time) + " | Join Group Success | " + loginName + " has joined newly created group " + groupName + ".");
                     break;
                 case USER_ALREADY_MEMBER_OF_GROUP:
                     log.offer(dateFormatter.format(time) + " | Join Group Failure | " + loginName + " has already joined " + groupName + ".");
                     break;
                 case GROUP_CAPACITY_REACHED:
                     log.offer(dateFormatter.format(time) + " | Join Group Failure | " + groupName + " is full.");
                     break;
                 case USER_ADDED_TO_GROUP:
                     TestChatServer.logUserJoinGroup(groupName, loginName, time);
                     log.offer(dateFormatter.format(time) + " | Join Group Success | " + loginName + " has joined group " + groupName + ".");
                     break;
             }
             return response;
         } else {
             log.offer(dateFormatter.format(time) + " | Join Group Failure | Not logged in.");
             return new ChatServerResponse(ResponseType.USER_NOT_FOUND);
         }
     }
 
     public ChatServerResponse leaveGroup(String groupName) {
         Date time = Calendar.getInstance().getTime();
         if (loggedIn) {
             ChatServerResponse response = chatServer.leaveGroup(this, groupName);
             switch (response.responseType) {
                 case USER_NOT_FOUND:
                     log.offer(dateFormatter.format(time) + " | Leave Group Failure | " + loginName + " is not logged in.");
                     break;
                 case GROUP_NOT_FOUND:
                     log.offer(dateFormatter.format(time) + " | Leave Group Failure | group " + groupName + " does not exist.");
                     break;
                 case USER_NOT_MEMBER_OF_GROUP:
                     log.offer(dateFormatter.format(time) + " | Leave Group Failure | " + loginName + " is not a member of " + groupName + ".");
                     break;
                 case USER_REMOVED_FROM_GROUP:
                     TestChatServer.logUserLeaveGroup(groupName, loginName, time);
                     log.offer(dateFormatter.format(time) + " | Leave Group success | " + loginName + " has left group " + groupName + ".");
                     break;
             }
             return response;
         } else {
             log.offer(dateFormatter.format(time) + " | Leave Group Failure | Not logged in.");
             return new ChatServerResponse(ResponseType.USER_NOT_FOUND);
         }
     }
 	
     public ChatServerResponse sendMessage(Message message) {
         Date time = Calendar.getInstance().getTime();
         log.offer(dateFormatter.format(time) + " | Sending Message | " + loginName + " (" + Integer.toString(sendCount) + ") -> " + message.receiver + " | " + message.text);
         if (loggedIn) {
             ChatServerResponse response = chatServer.send(message);
             switch (response.responseType) {
                 case SHUTTING_DOWN:
                     log.offer(dateFormatter.format(time) + " | Message Queue Failure | " + loginName + " (" + Integer.toString(sendCount) + ") -> " + message.receiver + " | ChatServer is shutting down.");
                     break;
                 case MESSAGE_ENQUEUED:
                     log.offer(dateFormatter.format(time) + " | Message Queue Success | " + loginName + " (" + Integer.toString(sendCount) + ") -> " + message.receiver + ".");
                     break;
                 case MESSAGE_BUFFER_FULL:
                     log.offer(dateFormatter.format(time) + " | Message Queue Failure | " + loginName + " (" + Integer.toString(sendCount) + ") -> " + message.receiver + " | Buffer full.");
                     break;
                 case SENDER_NOT_FOUND:
                     log.offer(dateFormatter.format(time) + " | Message Queue Failure | " + loginName + " (" + Integer.toString(sendCount) + ") -> " + message.receiver + " | Sender not found.");
                     break;
                 case RECEIVER_NOT_FOUND:
                     log.offer(dateFormatter.format(time) + " | Message Queue Failure | " + loginName + " (" + Integer.toString(sendCount) + ") -> " + message.receiver + " | Receiver not found.");
                     break;
             }
             TestChatServer.logUserSendMsg(loginName, message.toString());
             return response;
         } else {
             log.offer(dateFormatter.format(time) + " | Message Queue Failure | Not logged in.");
             TestChatServer.logUserSendMsg(loginName, message.toString());
             return new ChatServerResponse(ResponseType.USER_NOT_FOUND);
         }
     }
 
     /* Legacy method for testing of Project 1 */
     public ChatServerResponse sendMessage(String receiver, String messageText) {
         return sendMessage(new Message(this, receiver, ++sendCount, messageText));
     }
 	
     public void receiveMessage(Message message) {
         Date time = Calendar.getInstance().getTime();
         TestChatServer.logUserMsgRecvd(loginName, message.toString(), time);
         log.offer(dateFormatter.format(time) + " | Receiving Message | " + message.sender.getUserName() + " (" + Integer.toString(message.sqn) + ") -> " + message.receiver + " | " + message.text);
         pendingResponses.offer(new ChatServerResponse(ResponseType.MESSAGE_RECEIVED, message));
     }
 
     public void receiveSendFailure(Message message) {
         Date time = Calendar.getInstance().getTime();
         log.offer(dateFormatter.format(time) + " | Message Send Failure | " + message.sender.getUserName() + " (" + Integer.toString(message.sqn) + ") -> " + message.receiver + ".");
         pendingResponses.offer(new ChatServerResponse(ResponseType.MESSAGE_DELIVERY_FAILURE, message));
     }
 
     public ChatServerResponse getUserCount() {
         Date time = Calendar.getInstance().getTime();
         if (loggedIn) {
             ChatServerResponse response = chatServer.getUserCount(this);
             switch (response.responseType) {
                 case USER_NOT_FOUND:
                     log.offer(dateFormatter.format(time) + " | Get User Count Failure | " + loginName + " is not logged in.");
                     break;
                 case DATA_SENT:
                     log.offer(dateFormatter.format(time) + " | Get User Count Success | Server has " + Integer.toString(response.number) + " users.");
                     break;
             }
             return response;
         } else {
             log.offer(dateFormatter.format(time) + " | Get User Count Failure | Not logged in.");
             return new ChatServerResponse(ResponseType.USER_NOT_FOUND);
         }
     }
 
     public ChatServerResponse getUserList() {
         Date time = Calendar.getInstance().getTime();
         if (loggedIn) {
             ChatServerResponse response = chatServer.getUserList(this);
             switch (response.responseType) {
                 case USER_NOT_FOUND:
                     log.offer(dateFormatter.format(time) + " | Get User List Failure | " + loginName + " is not logged in.");
                     break;
                 case DATA_SENT:
                     String users = "";
                     for (String user : response.treeSet) {
                         users += user + ", ";
                     }
                     log.offer(dateFormatter.format(time) + " | Get User Count Success | Users: " + users);
                     break;
             }
             return response;
         } else {
             log.offer(dateFormatter.format(time) + " | Get User Count Failure | Not logged in.");
             return new ChatServerResponse(ResponseType.USER_NOT_FOUND);
         }
     }
 
     public ChatServerResponse getGroupCount() {
         Date time = Calendar.getInstance().getTime();
         if (loggedIn) {
             ChatServerResponse response = chatServer.getGroupCount(this);
             switch (response.responseType) {
                 case USER_NOT_FOUND:
                     log.offer(dateFormatter.format(time) + " | Get Group Count Failure | " + loginName + " is not logged in.");
                     break;
                 case DATA_SENT:
                     log.offer(dateFormatter.format(time) + " | Get Group Count Success | Server has " + Integer.toString(response.number) + " groups.");
                     break;
             }
             return response;
         } else {
             log.offer(dateFormatter.format(time) + " | Get Group Count Failure | Not logged in.");
             return new ChatServerResponse(ResponseType.USER_NOT_FOUND);
         }
     }
 
     public ChatServerResponse getGroupList() {
         Date time = Calendar.getInstance().getTime();
         if (loggedIn) {
             ChatServerResponse response = chatServer.getGroupList(this);
             switch (response.responseType) {
                 case USER_NOT_FOUND:
                     log.offer(dateFormatter.format(time) + " | Get Group List Failure | " + loginName + " is not logged in.");
                     break;
                 case DATA_SENT:
                     String groups = "";
                     for (String group : response.treeSet) {
                         groups += group + ", ";
                     }
                     log.offer(dateFormatter.format(time) + " | Get Group List Success | Groups: " + groups);
                     break;
             }
             return response;
         } else {
             log.offer(dateFormatter.format(time) + " | Get Group List Failure | Not logged in.");
             return new ChatServerResponse(ResponseType.USER_NOT_FOUND);
         }
     }
 
     public ChatServerResponse getGroupUserCount(String groupName) {
         Date time = Calendar.getInstance().getTime();
         if (loggedIn) {
             ChatServerResponse response = chatServer.getGroupUserCount(this, groupName);
             switch (response.responseType) {
                 case USER_NOT_FOUND:
                     log.offer(dateFormatter.format(time) + " | Get Group User Count Failure | " + loginName + " is not logged in.");
                     break;
                 case GROUP_NOT_FOUND:
                     log.offer(dateFormatter.format(time) + " | Get Group User Count Failure | " + groupName + " not found.");
                     break;
                 case DATA_SENT:
                     log.offer(dateFormatter.format(time) + " | Get Group User Count Success | " + groupName + " has " + Integer.toString(response.number) + " users.");
                     break;
             }
             return response;
         } else {
             log.offer(dateFormatter.format(time) + " | Get Group User Count Failure | Not logged in.");
             return new ChatServerResponse(ResponseType.USER_NOT_FOUND);
         }
     }
 
     public ChatServerResponse getGroupUserList(String groupName) {
         Date time = Calendar.getInstance().getTime();
         if (loggedIn) {
             ChatServerResponse response = chatServer.getGroupUserList(this, groupName);
             switch (response.responseType) {
                 case USER_NOT_FOUND:
                     log.offer(dateFormatter.format(time) + " | Get Group User List Failure | " + loginName + " is not logged in.");
                     break;
                 case GROUP_NOT_FOUND:
                     log.offer(dateFormatter.format(time) + " | Get Group User List Failure | " + groupName + " not found.");
                     break;
                 case DATA_SENT:
                     String users = "";
                     for (String user : response.treeSet) {
                         users += user + ", ";
                     }
                     log.offer(dateFormatter.format(time) + " | Get Group User List Success | " + groupName + " Users: " + users);
                     break;
             }
             return response;
         } else {
             log.offer(dateFormatter.format(time) + " | Get Group User List Failure | Not logged in.");
             return new ChatServerResponse(ResponseType.USER_NOT_FOUND);
         }
     }
 
     public void printLog() {
         for (String logMessage : log) {
             System.out.println(logMessage);
         }
     }
 }
