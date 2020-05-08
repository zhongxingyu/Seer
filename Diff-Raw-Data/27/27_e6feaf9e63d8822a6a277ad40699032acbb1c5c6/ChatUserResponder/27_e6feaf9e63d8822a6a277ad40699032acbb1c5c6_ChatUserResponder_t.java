 package edu.berkeley.cs.cs162;
 
 import java.io.*;
 import java.net.*;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.concurrent.LinkedBlockingQueue;
 
 public class ChatUserResponder extends Thread {
 
     private ChatUser chatUser;
     private LinkedBlockingQueue<String> log;
     private SimpleDateFormat dateFormatter;
     private LinkedBlockingQueue<ChatServerResponse> pendingResponses;
     private Socket socket;
     private ObjectOutputStream output;
     private boolean shuttingDown;
 
     public ChatUserResponder(ChatUser chatUser, Socket socket, LinkedBlockingQueue<ChatServerResponse> pendingResponses, LinkedBlockingQueue<String> log) {
         this.chatUser = chatUser;
         this.log = log;
         this.dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
         this.socket = socket;
         try {
             this.output = new ObjectOutputStream(socket.getOutputStream());
         } catch (IOException e) {
             try {
                 this.socket.close();
             } catch (IOException f) {
             }
         }
         this.pendingResponses = pendingResponses;
         this.shuttingDown = false;
     }
 
     public void shutdown() {
         shuttingDown = true;
        interrupt();
     }
 
     public void run() {
         ChatServerResponse response = null;
         Message message = null;
         try {
             while (!shuttingDown || pendingResponses.size() > 0) {
                try {
                    response = pendingResponses.take();
                } catch (InterruptedException e) {}
                 if (response.responseType == ResponseType.MESSAGE_RECEIVED || response.responseType == ResponseType.MESSAGE_DELIVERY_FAILURE) {
                     message = response.message;
                     response.messageDate = response.message.date;
                     response.messageSender = response.message.senderName;
                     response.messageReceiver = response.message.receiver;
                     response.messagesqn = response.message.sqn;
                     response.messageText = response.message.text;
                     response.message = null;
                 }
                 output.writeObject(response);
                 output.flush();
                 response = null;
             }
         } catch (Exception e) {
         } finally {
             try {
                 output.close();
             } catch (Exception f) {
             }
             ChatServerResponse pendingResponse = response;
             while (pendingResponse != null) {
                 log.offer(dateFormatter.format(Calendar.getInstance().getTime()) + " | Failed to send Response: " + pendingResponse.responseType);
                 if (pendingResponse.responseType == ResponseType.MESSAGE_RECEIVED && message != null && message.sender != null) {
                     try {
                         chatUser.getChatServer().getDatabaseManager().logMessage(chatUser.getUserName(), message);
                     } catch (SQLException e) {
                         message.sender.receiveSendFailure(pendingResponse.message);
                     }
                 }
                 pendingResponse = pendingResponses.poll();
             }
             try {
                 socket.close();
             } catch (Exception e) {
             }
         }
     }
 }
