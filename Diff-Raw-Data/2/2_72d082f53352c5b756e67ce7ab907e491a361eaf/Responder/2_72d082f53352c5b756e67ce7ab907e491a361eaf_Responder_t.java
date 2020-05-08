 /*  Ahmet Aktay and Nathan Griffith
  *  DarkChat
  *  CS435: Final Project
  */
 import java.io.*;
 import java.net.*;
 import java.util.*;
 
 
 class Responder implements Runnable {
   private Queue<Socket> q;
   private BufferedInputStream bin;
   private Socket socket;
   private UserList knownUsers;
   private String name;
   private Message pm;
   User toUser;
   User fromUser;
   
   public Responder(Queue<Socket> q, UserList knownUsers, Message pm, String name){
     this.q = q;
     this.knownUsers = knownUsers;
     this.name = name;
     this.pm = pm;
   }
 
 
   public void run() {
     try {
     MyUtils.dPrintLine(String.format("Launching thread %s", name));
     //Wait for an item to enter the socket queue
       while(true) {
         socket = null;
         while (socket==null) {
           synchronized(q) {
             while (q.isEmpty()) {
               try {
                 q.wait(500);
               }
               catch (InterruptedException e) {
                 MyUtils.dPrintLine("Connection interrupted");
               }
             }
             socket = q.poll();
           }
         }
         MyUtils.dPrintLine(String.format("Connection from %s:%s", socket.getInetAddress().getHostName(),socket.getPort()));
         
         // create read stream to get input
         BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         String ln = inFromClient.readLine();
         
         int port = socket.getPort();
         if (ln.equals("ONL")) //fromUser is online
         { 
           ln = inFromClient.readLine();
           port = Integer.parseInt(inFromClient.readLine());
           String state = inFromClient.readLine();
           System.out.println(String.format("'%s' is online", ln));
 
           //DEAL WITH USER
           synchronized (knownUsers) {
             fromUser = knownUsers.get(ln,true); //only get if exists
           }
           synchronized (fromUser) {
             if (fromUser != null) {
               fromUser.putSession(new InetSocketAddress(socket.getInetAddress(),port));
               if (state.equals("INIT")) {
                 synchronized (pm) {
                   pm.declareOnline(fromUser, false);
                 }
               }
             }
           }
         }
         else if (ln.equals("CHT")) //fromUser is chatting with you!
         {
           String fromUsername = inFromClient.readLine();
           String toUsername = inFromClient.readLine();
           port = Integer.parseInt(inFromClient.readLine());
           ln = inFromClient.readLine();
           synchronized (knownUsers) {
             fromUser = knownUsers.get(fromUsername,true); //only get if exists
             toUser   = knownUsers.get(toUsername,  true);
           }
           synchronized (fromUser) {
            if (!toUser.name.equals(pm.localUser.name)) {
               MyUtils.dPrintLine("Recieved chat with incorrect user fields:");
               MyUtils.dPrintLine(String.format("%s to %s: %s", fromUser.name,toUser.name,ln));
             }
             else if (fromUser != null) {
               System.out.println(String.format("%s: %s", fromUser.name,ln));
               fromUser.putSession(new InetSocketAddress(socket.getInetAddress(),port));
             }
             else {
               MyUtils.dPrintLine("Recieved chat from unknown user:");
               MyUtils.dPrintLine(String.format("%s: %s", fromUser.name,ln));
             }
           }
         }
         else
           MyUtils.dPrintLine("Unrecognized message format");
         socket.close();
       }
     }
     catch (Exception e) {
       MyUtils.dPrintLine(String.format("%s",e));
       //some sort of exception
     }
   }
 }
