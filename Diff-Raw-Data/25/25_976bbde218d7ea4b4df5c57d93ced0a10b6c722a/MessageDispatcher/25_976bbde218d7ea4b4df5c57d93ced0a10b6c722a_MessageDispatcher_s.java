 package edu.berkeley.cs.cs162;
 
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.Calendar;
 import java.util.TreeSet;
 
 class MessageDispatcher extends Thread {
     private UserManager userManager;
     private LinkedBlockingQueue<Message> messages;
     private boolean shuttingDown;
 
     MessageDispatcher(ChatServer chatServer){
         this.userManager = chatServer.getUserManager();
         this.messages = new LinkedBlockingQueue<Message>();
         this.shuttingDown = false;
     }
 
     public void shutdown() {
         shuttingDown = true;
         interrupt();
     }
 
     public ChatServerResponse enqueue(Message message) {
         if (userManager.hasUser(message.sender.getUserName())) {
             if (userManager.hasUser(message.receiver)) {
                 message.receivingUsers = new TreeSet<String>();
                 message.receivingUsers.add(message.receiver);
                 if (messages.offer(message)) {
                     return new ChatServerResponse(ResponseType.MESSAGE_ENQUEUED);
                 } else {
                     return new ChatServerResponse(ResponseType.MESSAGE_BUFFER_FULL);
                 }
            } else if (userManager.hasGroup(message.receiver)) {
                ChatGroup targetGroup = userManager.getGroup(message.receiver);
                message.receivingUsers = new TreeSet<String>(targetGroup.users.keySet());
                 if (messages.offer(message)) {
                     return new ChatServerResponse(ResponseType.MESSAGE_ENQUEUED);
                 } else {
                     return new ChatServerResponse(ResponseType.MESSAGE_BUFFER_FULL);
                 }
             } else {
                 return new ChatServerResponse(ResponseType.RECEIVER_NOT_FOUND);
             }
         } else {
             return new ChatServerResponse(ResponseType.SENDER_NOT_FOUND);
         }
     }
 
     private void deliver(Message message) {
         boolean hasFailed = false;
         for (String receivingUserName : message.receivingUsers) {
             ChatUser receivingUser = userManager.getUser(receivingUserName);
             if (receivingUser == null) {
                 hasFailed = true;
             } else {
                 message.date = Calendar.getInstance().getTime();
                 receivingUser.receiveMessage(message);
             }
         }
         if (hasFailed) {
             message.sender.receiveSendFailure(message);
         }
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
