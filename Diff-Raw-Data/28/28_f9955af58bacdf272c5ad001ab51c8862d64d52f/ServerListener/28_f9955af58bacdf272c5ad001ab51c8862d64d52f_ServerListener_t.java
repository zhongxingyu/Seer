 package com.test9.irc.engine;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 public class ServerListener extends Thread implements Runnable {
  
     private String input = null;
     private BufferedReader in = null;
     private List<Channel> channels;
     private final Server server;
 
     ServerListener(Server server) throws IOException {
         this.in = new BufferedReader(new InputStreamReader(server.getSocket()
                 .getInputStream()));
         this.server = server;
         this.channels = server.getChannels();
 
     }
 
     @Override
     public void run() {
         // Keep reading lines from the server.
 
         while (true) {
           // System.out.println("in serverlistener run()	");
             try {
                 this.input = this.in.readLine();
                 System.out.println("Server Listener incoming: " + this.input);
                 if (this.input != null) {
                     this.processInput(this.input);
                 }
             } catch (IOException e) {
                 e.getStackTrace();
             }
 
             Thread.yield();
         }
     }
 
     private void processInput(String input) {
    	System.out.println("INPUT IS:\"" + input + "\"");
         if (input.startsWith("PING")) {
             // We must respond to PINGs to avoid being disconnected.
             System.out.println("ping request made");
 
             this.server.sendPong(input.substring(5));
 
         } else {
             try {
                 String[] arguments = input.split("[ ]+");
                 
                 if (arguments[1].equals("353")) {
                     String[] two_parts = input.split("353");
                     String channel = two_parts[1].substring(1).split("=")[1]
                             .substring(1).split(" ")[0];
                     String user_list = two_parts[1].split(":")[1];
 
                     //this.channels.add(new Channel(channel));
                     this.generateUserList(user_list, channel);
                 } else if(arguments[1].equals("004")){
                 	
                 } else if(arguments[1].equals("MODE")){
                 	System.out.println("We have connected!!!");
                 	server.isConnected=true;
                 	server.sendInitialMessage();
                 }
                 else if(arguments[1].equals("433")){
                 	System.out.println("Name taken. Try again.");
                 } else if (arguments[1].equals("366")) {
                     // This event is useless
                 } else if (arguments[1].equals("PRIVMSG")) {
                     this.receivePrivMsg(input);
                 } else if (arguments[1].equals("QUIT")) {
                     String[] two_parts = input.split("QUIT");
                     String quit_reason = two_parts[1].substring(1);
                     String quit_user = two_parts[0].split("!")[0].substring(1);
                     String quit_channel = two_parts[1].substring(1);
 
                     this.userQuitMsg(quit_user, quit_reason, quit_channel);
                 } else if (arguments[1].equals("JOIN")) {
                     String[] two_parts = input.split("JOIN");
                     String join_channel = two_parts[1].substring(2);
                     String join_user = two_parts[0].split("!")[0].substring(1);
 
                     this.userJoinMsg(join_user, join_channel);
                 } else if (arguments[1].equals("PART")) {
                     String[] two_parts = input.split("PART");
                     String part_channel = two_parts[1].substring(1);
                     String part_user = two_parts[0].split("!")[0].substring(1);
 
                     this.userPartMsg(part_user, part_channel);
                 } else if (arguments[1].equals("NICK")) {
                     String[] two_parts = input.split("NICK");
                     String nick_original_user = two_parts[0].split("!")[0]
                             .substring(1);
                     String nick_change = two_parts[1].split(":")[1]
                             .substring(0);
 
                     this.userNickChange(nick_original_user, nick_change);
                 } else {
 //                    System.out.println("Processed input not recgonized:\""
 //                            + (input));
                 }
             } catch (ArrayIndexOutOfBoundsException e) {
                 this.channels.get(0).newMessage(input + "\n");
             }
         }
     }
 
     private void userPartMsg(String user, String channel) {
         this.channels.get(this.findChannel(channel)).newMessage(
                 "[" + user + "] has left " + channel + "\n");
         this.channels.get(this.findChannel(channel)).getChanUserList()
                 .userParted(user);
 
     }
 
     private void userJoinMsg(String user, String channel) {
         try {
             this.channels.get(this.findChannel(channel)).newMessage(
                     "[" + user + "] has joined " + channel + "\n");
             this.channels.get(this.findChannel(channel)).getChanUserList()
                     .userJoined(user);
         } catch (Exception e) {
             System.out.println("List has not yet been initialized. Cannot "
                     + "add joined user yet.");
         }
 
     }
 
     private void userQuitMsg(String user, String reason, String channel) {
         this.channels.get(this.findChannel(channel)).newMessage(
                 "[" + user + "] has quit (" + reason + ")\n");
         this.channels.get(this.findChannel(channel)).getChanUserList()
                 .userParted(user);
     }
 
     private void userNickChange(String original_nick, String change) {
         for (int i = 0; i < this.channels.size(); i++) {
             this.channels.get(i).getChanUserList()
                     .userNickChange(original_nick, change);
             this.channels.get(i).newMessage(
                     "[" + original_nick + "] has changed their nick to ["
                             + change + "]\n");
         }
     }
 
     private void receivePrivMsg(String input) {
         String[] input_split = input.split("PRIVMSG");
         String serverSource = input_split[1].split(":")[0]
                 .replaceAll("\\s", "");
         String buffer = "[" + serverSource + "]";
         buffer += " <" + input_split[0].split("!")[0].substring(1) + "> ";
         buffer += input_split[1].substring(input_split[1].indexOf(":") + 1)
                 + "\n";
         this.channels.get(this.findChannel(serverSource)).newMessage(buffer);
     }
 
     private void generateUserList(String userList, String channel) {
         this.channels.get(this.findChannel(channel)).newMessage(userList);
         this.channels.get(this.findChannel(channel)).generateChannelUserList(
                 userList, null);
     }
 
     private int findChannel(String channel) {
         for (int i = 0; i < this.channels.size(); i++) {
             if (this.channels.get(i).getName().equals(channel)) {
                 return i;
             }
         }
         System.err.println("Could not find the correct channel: return -1");
         return -1;
     }
 
     /**
      * @return the channels
      */
     public List<Channel> getChannels() {
         return this.channels;
     }
 
     /**
      * @param channels
      *            the channels to set
      */
     public void setChannels(ArrayList<Channel> channels) {
         System.out.println("setting channels in serverlistener.java");
         this.channels = channels;
     }
 
 }
