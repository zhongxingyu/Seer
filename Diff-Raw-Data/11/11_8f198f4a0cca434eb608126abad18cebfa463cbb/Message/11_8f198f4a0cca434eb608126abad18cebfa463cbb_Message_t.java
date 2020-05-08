 /*  Ahmet Aktay and Nathan Griffith
  *  DarkChat
  *  CS435: Final Project
  */
 import java.io.*;
 import java.net.*;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Date;
 
 
 public class Message {
   public User localUser;
   
   private InetSocketAddress address;
   private Socket socketOut;
   private DataOutputStream out;
   private int port;
   
   public Message(User localUser, int port)
   {
     this.localUser = localUser;
     this.port = port;
   }
   
   public Boolean declareOnline(User toUser) throws Exception {
     return declareOnline(localUser, toUser, true);
   }
   public Boolean declareOnline(User toUser, boolean init) throws Exception {
     return declareOnline(localUser, toUser, init);
   }
   public Boolean declareOnline(User fromUser, User toUser) throws Exception {
     return declareOnline(fromUser, toUser, true);
   }
   public Boolean declareOnline(User fromUser, User toUser, boolean init) throws Exception {
     //Message format: <online,from_username,returnPort>
     String message = String.format("ONL\n%s\n%s\n",fromUser.name,port);
     if (init)
       message += "INIT\n";
     else //it is a response
       message += "RESP\n";
     MyUtils.dPrintLine( String.format("'%s' attempts to notify '%s' of online state", fromUser.name, toUser.name) );
    return contactUser(toUser,message,true);
   }
   
   public Boolean declareOffline(User toUser) throws Exception {
     return declareOffline(localUser,toUser);
   }
   public Boolean declareOffline(User fromUser, User toUser) throws Exception {
     String message = String.format("OFL\n%s\n%s\n",fromUser.name,port);
     MyUtils.dPrintLine( String.format("'%s' attempts to notify '%s' of offline state", fromUser.name, toUser.name) );
     return contactUser(toUser,message);
   }
   public Boolean declareOffline(UserList knownUsers) throws Exception {
     for (User user : knownUsers.userHash.values()) {
       if (user.isOnline()) {
         declareOffline(user);
       }
     }
     return true;
   }
   
   public Boolean requestUserList(User toUser) throws Exception { //request your own list
     return requestUserList(localUser, toUser);
   }
   public Boolean requestUserList(User ofUser, User toUser) throws Exception { //request someone elses
     String message = String.format("REQ\n%s\n%s\n%s\n",localUser.name,port,ofUser.name);
     MyUtils.dPrintLine( String.format("'%s' requests known users of '%s' (from '%s')", localUser.name, ofUser.name, toUser.name) );
     return contactUser(toUser,message);
   }
   public Boolean contactUser(User toUser, String message) throws Exception{
    return contactUser(toUser,message,false);
  }
  public Boolean contactUser(User toUser, String message, boolean offline) throws Exception{
     boolean contacted = false;
     for (Session session : toUser.sessions.values()) {
      if (offline||session.online) {
         try {
           //Create an output stream
     
           address = session.address;
           socketOut = new Socket(address.getHostName(), address.getPort());
           DataOutputStream out = streamOut(socketOut);
           
           out.writeBytes(message);
           out.flush();
           
           MyUtils.dPrintLine(String.format("Contacting %s:%s", session.address.getHostName(),session.address.getPort()));
           socketOut.close();
           
           session.lastValid = new Date();
           contacted = true;
         }
         catch (ConnectException e) {
           //connection refused
           session.online = false;
           MyUtils.dPrintLine(String.format("connection refused at %s:%s", session.address.getHostName(),session.address.getPort()));
         }
       }
     }
     //TODO: only return true if at least one session did not fail.
     return contacted;
   }
   
   private DataOutputStream streamOut(Socket socketOut) throws Exception {
     DataOutputStream out = new DataOutputStream( new BufferedOutputStream( socketOut.getOutputStream() ) );
     return out;
   }
   
 } // end of class
 