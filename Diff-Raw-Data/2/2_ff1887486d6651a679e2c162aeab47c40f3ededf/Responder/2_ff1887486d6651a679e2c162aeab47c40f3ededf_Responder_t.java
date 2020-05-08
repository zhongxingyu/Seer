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
   User ofUser;
   
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
 
           //DEAL WITH USER
           synchronized (knownUsers) {
             fromUser = knownUsers.get(ln,true); //only get if exists
           }
           synchronized (fromUser) {
             if (fromUser != null) {
               fromUser.putSession(new InetSocketAddress(socket.getInetAddress(),port));
               if (!fromUser.name.equals(pm.localUser.name)) {
                 System.out.println(String.format("'%s' is online", fromUser.name));
                 if (state.equals("INIT")) {
                   synchronized (pm) {
                     pm.declareOnline(fromUser, false);
                   }
                 }
               }
             }
           }
         }
         else if (ln.equals("RUP")) {
           String fromUserName = inFromClient.readLine();
           int portIn = Integer.parseInt(inFromClient.readLine());
           String ofUserName = inFromClient.readLine();
           String message = String.format("RU2\n%s\n%s\n%s\n%s\n",fromUserName,socket.getInetAddress(),portIn,ofUserName);
           User ofUser;
           synchronized (knownUsers) {
             ofUser = knownUsers.get(ofUserName,true); //only get if exists
           }
           if (ofUser == null) {
             MyUtils.dPrintLine("Don't know how to forward message!!!");
           }
           else {
             pm.contactUser(ofUser,message);
           }
         }
         else if (ln.equals("RU2")) {
           String fromUserName = inFromClient.readLine();
           String ip = inFromClient.readLine();
           int portIn = Integer.parseInt(inFromClient.readLine());
           String ofUserName = inFromClient.readLine();
           synchronized (knownUsers) {
             fromUser = knownUsers.get(fromUserName,true); //only get if exists
           }
           if (fromUser != null) {
            if (!pm.localUser.name.equals(ofUserName))
               MyUtils.dPrintLine("Recieved update request for a different user");
             else {
               fromUser.putSession(new InetSocketAddress(ip,portIn));
               pm.declareOnline(fromUser, false);
             }
           }
           else
             MyUtils.dPrintLine("Recieved update request from unknown user");
         }
         else if (ln.equals("OFL")) {
           ln = inFromClient.readLine();
           port = Integer.parseInt(inFromClient.readLine());
 
           //DEAL WITH USER
           synchronized (knownUsers) {
             fromUser = knownUsers.get(ln,true); //only get if exists
           }
           synchronized (fromUser) {
             if (fromUser != null) {
               fromUser.putSession(new InetSocketAddress(socket.getInetAddress(),port));
               if (!fromUser.name.equals(pm.localUser.name)) {
                 System.out.println(String.format("'%s' is offline", fromUser.name));
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
         else if (ln.equals("REQ")) // someone is requesting known users
         {
           String fromUserName = inFromClient.readLine();
           port = Integer.parseInt(inFromClient.readLine());
           String ofUserName = inFromClient.readLine();
           synchronized (knownUsers) {
             fromUser = knownUsers.get(fromUserName,true);
             ofUser          = knownUsers.get(ofUserName,true);
           }
           if (fromUser == null)
           {
             MyUtils.dPrintLine("Recieved knowns REQuest from unknown user:");
             MyUtils.dPrintLine(String.format("%s: %s", fromUser.name,ln));
             // do not respond
           }
           else if (ofUser == null)
           {
             MyUtils.dPrintLine("Recieved knowns REQuest of unknown user:");
             MyUtils.dPrintLine(String.format("%s: %s", ofUser.name,ln));
             pm.deliverFakeKnownList(ofUserName, fromUser);
           }
           else if (!fromUser.knowUser(ofUser))
           {
             MyUtils.dPrintLine("Recieved REQuest where users haven't met.");
             MyUtils.dPrintLine(String.format("%s wanted %s's knowns but hadn't met them. delivering empty list",fromUser.name,ofUser.name));
             pm.deliverFakeKnownList(ofUserName, fromUser);
           }
           else
           {
             MyUtils.dPrintLine("Received valid REQuest where users have met.");
             MyUtils.dPrintLine(String.format("%s wanted %s's knowns, delivering them via BUD.", fromUser, ofUser));
             pm.deliverKnownList(ofUser, fromUser);
           }
         }
         else if (ln.equals("BUD")) // someone is delivering known users
         {
         // TODO: check if we requested one
 /*        String message = String.format("BUD\n%s\n%s\n", localUser.name, port, ofUserName);
           String fromUserName = inFromClient.readLine();
           String ofUserName = inFromClient.readLine();
           port = Integer.parseInt(inFromClient.readLine());
           Boolean friends = false;
           synchronized (knownUsers) {
           fromUser = knownUsers.get(fromUserName,true);
           ofUser          = knownUsers.get(ofUserName,true);
           }*/
           String fromUserName = inFromClient.readLine();
           port = Integer.parseInt(inFromClient.readLine());
           String ofUserName = inFromClient.readLine();
           synchronized (knownUsers) {
             fromUser = knownUsers.get(fromUserName,true);
             ofUser          = knownUsers.get(ofUserName,true);
             if (fromUserName.equals("server")) {
 		fromUser = knownUsers.get("server");
 	    }
           }
           if (fromUser == null)
           {
             MyUtils.dPrintLine("Recieved BUDs from unknown user:");
             MyUtils.dPrintLine(String.format("%s: %s", fromUser.name,ln));
           }
           else if (ofUser == null)
           {
             MyUtils.dPrintLine("Recieved BUDs of unknown user:");
             MyUtils.dPrintLine(String.format("%s: %s", ofUserName,ln));
           }
           else
           {
 
             MyUtils.dPrintLine("Received valid BUDs from server");
             MyUtils.dPrintLine(String.format("%s sent %s's knowns, delivering them via BUD.", fromUser.name, ofUser.name));
             synchronized(ofUser)
             {
               synchronized(knownUsers)
               {
                 String budName = inFromClient.readLine();
                 while(!budName.equals(""))
                 {
                   User budUser = knownUsers.get(budName);
                   synchronized (budUser) {
                     ofUser.meetUser(budUser);
                   }
                   budName = inFromClient.readLine();
                 }
 
               }
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
