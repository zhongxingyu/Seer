 package net;
 
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.sql.ResultSet;
 import java.util.HashMap;
 import log.AccessLogger;
 import log.ErrorLogger;
 import mail.Mailer;
 import sql.Query;
 
 /**
  * Contains the methods for handling requests from connected administrator
  * clients. Server will be off by default.
  * 
  * @author Stephen Fahy
  */
 public class Server extends Thread {
     private static Server INSTANCE = new Server();
     public static final int AUTH_PORT = 579;
     
     private ServerSocket authPort;
     private HashMap<String, AdminServerSocket> sockets;
     //integers represent checks to sockets without any activity
     private HashMap<String, Integer> activity;
     private Authenticator auth;
     
     /**
      * Constructor.
      */
     private Server() {
         try {
             authPort = new ServerSocket(AUTH_PORT, 0, InetAddress.getLocalHost());
             System.out.println(authPort.getInetAddress().getHostAddress());
             sockets = new HashMap<String, AdminServerSocket>();
             activity = new HashMap<String, Integer>();
             auth = new Authenticator();
             new Thread(auth).start();
         }
         catch(Exception e) {
             ErrorLogger.get().log(e.toString());
             e.printStackTrace();
             System.exit(1);
         }
     }
     
     /**
      * Adds a socket to it's list.
      * 
      * @param socket 
      */
     public void addSocket(AdminServerSocket socket) {
         sockets.put(socket.getUsername(), socket);
         activity.put(socket.getUsername(), 0);
     }
     
     /**
      * Handles a message.
      * 
      * @param msg A message from a user.
      * @param username That user's username.
      */
     public void handleMessage(Message msg, String username) {
         if(msg instanceof QueryMessage) {
             //SQL query handling
             QueryMessage qmsg = (QueryMessage) msg;
             String query = (String) qmsg.getContent().get(QueryMessage.QUERY);
             ResultSet results = Query.query(query);
             
             if(results != null) {
                 ResultMessage reply;
                 
                 try {
                     results.last();
                     if(results.getRow() == 0) {
                         reply = new ResultMessage(qmsg.getId(), null);
                     }
                     else {
                         results.beforeFirst();
                         reply = new ResultMessage(qmsg.getId(), results);
                     }
                     reply.send(sockets.get(username).getOut());
                 }
                 catch(Exception e) {
                     ErrorLogger.get().log(e.toString());
                     e.printStackTrace();
                     reply = new ResultMessage(qmsg.getId(), null);
                     reply.send(sockets.get(username).getOut());
                 }
             }
             
             AccessLogger.get().log(username + " sent SQL query " + 
                     query + ".");
             
             //send emails if the sql statement was an update on courses
             String action = query.split(" ")[0];
             String table = query.split(" ")[1];
             
             if(action.toLowerCase().equals("update") &&
                     table.toLowerCase().equals("courses")) {
                 String code = query.split("code = '")[1].split("';")[0];
                 String emailQuery = "SELECT email FROM MailingList WHERE code = '" +
                         code + "';";
                 ResultSet emailResults = Query.query(emailQuery);
                 
                 try {
                     while(emailResults.next()) {
                         String emailAddr = emailResults.getString(1);
                         Mailer.mail(emailAddr, "Changes to course " + code,
                                 "There have been changes made to the course you are " +
                                 "registered for. Please check the website for further " +
                                 "details at your earliest convenience.");
                     }
                 }
                 catch(Exception e) {
                     ErrorLogger.get().log(e.toString());
                     e.printStackTrace();
                 }
             }
         }
         else if(msg instanceof AckMessage) {
             //receiving acknowledgements
             boolean bool = (Boolean)((AckMessage) msg).getContent().get(AckMessage.VALUE);
             
             if(bool) {
                 AccessLogger.get().log(username + " sent acknowledgement.");
             }
             else {
                 AccessLogger.get().log(username + " sent unacknowledgement.");
             }
         }
         else if(msg instanceof UserReplyMessage) {
             //trying to reply to user messages
             int originalMessageId = (Integer) 
                     msg.getContent().get(UserReplyMessage.ORIGINAL_MESSAGE_ID);
             String replyText = msg.getContent().get(UserReplyMessage.REPLY_TEXT).toString();
             
             //check if message has been replied to
             boolean repliedTo = true;
             String query = "SELECT * FROM Messages WHERE replyId = " 
                     + originalMessageId + ";";
             ResultSet rs = Query.query(query);
             
             try {
                 rs.last();
                repliedTo = (rs.getRow() == 0);
             }
             catch(Exception e) {
                 ErrorLogger.get().log(e.toString());
                 e.printStackTrace();
             }
             
             if(repliedTo) {
                 //message was replied to already or there was a problem in accessing the DB
                 AckMessage nack = new AckMessage(msg.getId(), false);
                 nack.send(sockets.get(username).getOut());
                 
                 AccessLogger.get().log(username + " sent reply to already replied message ID#" + 
                         originalMessageId + ".");
             }
             else {
                 //message hasn't been replied to, so add this reply
                 query = "SELECT MAX(id) AS max FROM Messages;";
                 rs = Query.query(query);
                 
                 try {
                     rs.next();
                     int max = rs.getInt(1);
                     
                     query = "UPDATE Messages SET replyId = " + (max + 1) +
                            "WHERE id = " + originalMessageId;
                     Query.query(query);
                     query = "INSERT INTO Messages(username, content, replyId) VALUES ('"
                            + username + "', '" + replyText + "', NULL);";
                     Query.query(query);
                     
                     AckMessage ack = new AckMessage(msg.getId(), true);
                     ack.send(sockets.get(username).getOut());
                     
                     AccessLogger.get().log(username + " sent reply to message ID#" + 
                         originalMessageId + " successfully.");
                 }
                 catch(Exception e) {
                     ErrorLogger.get().log(e.toString());
                     e.printStackTrace();
                     
                     AccessLogger.get().log(username + " sent reply to message ID#" + 
                         originalMessageId + ", but there was a server fault.");
                 }
             }
         }
     }
     
     //getters
     public static Server get() {
         return INSTANCE;
     }
     
     /**
     * Checks the authorization port for activity in a loop.
     * 
     * @author Stephen Fahy
     */
     private class Authenticator implements Runnable {
         /**
          * Constructor.
          */
         private Authenticator() {
         }
         
         /**
          * Loops as long as the server runs, checking for messages/connections.
          * Must be called when the server is turned on.
          */
         @Override
         public void run() {
             while(true) {
                 try {
                     handleAuthRequest(authPort.accept());
                 }
                 catch(Exception e) {
                     ErrorLogger.get().log(e.toString());
                     e.printStackTrace();
                 }
             }
         }
        
        /**
         * Handles a request from a client wishing to connect to the server.
         * All other messages on this port will be ignored.
         */
        private void handleAuthRequest(Socket client) throws Exception {
            ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(client.getInputStream());
            
            Message msg = (Message) in.readObject();
            
            if(msg instanceof AuthMessage) {
                AuthMessage authMsg = (AuthMessage) msg;
                String username = authMsg.getContent().get(AuthMessage.USERNAME).toString();
                String passhash = authMsg.getContent().get(AuthMessage.PASSWORD).toString();
                
                boolean valid = authenticate(username, passhash);
 
                if(valid) {
                    ConnectionMessage con = new ConnectionMessage(msg.getId(), 
                            AdminServerSocket.getNextFreePort());
                    con.send(out);
                    out.flush();
                    
                    AdminServerSocket serverSocket = new AdminServerSocket(username);
                    addSocket(serverSocket);
                    new Thread(serverSocket).start();
                    
                    AccessLogger.get().log(username + " connected.");
                }
                else {
                    //invalid login details
                    AckMessage ack = new AckMessage(msg.getId(), false);
                    ack.send(out);
                }
            }
        }
        
        /**
         * Authenticates a user wishing to connect to the system.
         * 
         * @param username The user's username.
         * @param passhash The hash of the user's password.
         * @return True if the given username/password hash combo are on the DB as admin.
         */
        private boolean authenticate(String username, String passhash) {
            boolean output = false;
            System.out.println("Username: " + username);
            System.out.println("Passhash: " + passhash);
 
            ResultSet results = Query.query("SELECT * FROM Users WHERE isAdmin = TRUE " +
                    "AND username = '" + username + "' " +
                    "AND password = '" + passhash + "';");
 
            try {
                results.last();
                int resultSize = results.getRow();
                System.out.println("Result Size: " + resultSize);
                if(resultSize == 1) {
                    output = true;
                }
            }
            catch(Exception e) {
                ErrorLogger.get().log(e.toString());
                e.printStackTrace();
            }
 
            return output;
        }
     }
 }
