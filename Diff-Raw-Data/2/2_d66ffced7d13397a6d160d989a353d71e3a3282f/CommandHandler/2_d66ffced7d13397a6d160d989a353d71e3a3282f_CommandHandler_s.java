 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  * @date Apr 30, 2011
  * @author Techjar
  * @version 
  */
 
 
 package com.simplechat.server;
 
 
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.List;
 import java.util.Map;
 import com.simplechat.protocol.*;
 
 public class CommandHandler {
     private ClientData client;
     private List clients;
     private DatagramSocket socket;
     private DataManager dm;
 
 
     public CommandHandler(ClientData client, List clients, DatagramSocket socket, DataManager dm) {
         this.client = client;
         this.clients = clients;
         this.socket = socket;
         this.dm = dm;
     }
 
     public void parseCommand(String cmd, String[] args) {
         PacketHandler ph = new PacketHandler();
         Map<String, String> cfg = new ConfigManager().load();
 
         if(cmd.equalsIgnoreCase("help")) {
             ph.sendPacket(new Packet5Message("/quit [message] - Disconnects you from the server."), client, this.socket);
             ph.sendPacket(new Packet5Message("/stop - Stops the server. (Op-only)"), client, this.socket);
             ph.sendPacket(new Packet5Message("/say <message> - Broadcasts a server message."), client, this.socket);
             ph.sendPacket(new Packet5Message("/ping - Ping! Pong!"), client, this.socket);
             ph.sendPacket(new Packet5Message("/kill <name> - Kills a user."), client, this.socket);
             ph.sendPacket(new Packet5Message("/nuke - NUKE THE CHAT!!!!! (Op-only)"), client, this.socket);
             ph.sendPacket(new Packet5Message("/whois <name> - Gets information on a user."), client, this.socket);
             ph.sendPacket(new Packet5Message("/list - Lists users in the chat."), client, this.socket);
             ph.sendPacket(new Packet5Message("/me <message> - Makes you do an action."), client, this.socket);
             ph.sendPacket(new Packet5Message("/nick <name> [password] - Changes your name!"), client, this.socket);
             ph.sendPacket(new Packet5Message("/password <set|remove> [password] - Set or remove your password."), client, this.socket);
             ph.sendPacket(new Packet5Message("/op <name> - Ops a user. (Op-only)"), client, this.socket);
             ph.sendPacket(new Packet5Message("/deop <name> - De-ops a user. (Op-only)"), client, this.socket);
             ph.sendPacket(new Packet5Message("/kick <name> - Kicks a user. (Op-only)"), client, this.socket);
             ph.sendPacket(new Packet5Message("/ban <name> - Bans a user. (Op-only)"), client, this.socket);
             ph.sendPacket(new Packet5Message("/unban <name> - Unbans a user. (Op-only)"), client, this.socket);
             ph.sendPacket(new Packet5Message("/banip <ip> - Bans an IP. (Op-only)"), client, this.socket);
             ph.sendPacket(new Packet5Message("/unbanip <ip> - Unbans an IP. (Op-only)"), client, this.socket);
         }
         else if(cmd.equalsIgnoreCase("quit")) {
             String msg = "";
             for(int i = 0; i < args.length; i++) msg += args[i] + " ";
             msg = msg.trim();
 
             System.out.println(client.getUsername() + " quit. Reason: " + msg);
             ph.sendPacket(new Packet4Kick("Quitting. Reason: " + (msg.equals("") ? "None." : msg)), client, this.socket);
             ph.sendAllExcludePacket(new Packet5Message(client.getUsername() + " quit. (" + (msg.equals("") ? "None." : msg) + ")"), clients, client, this.socket);
             clients.remove(client);
             client.stopKeepAliveThread();
             client.stopKeepAliveSendThread();
         }
         else if(cmd.equalsIgnoreCase("stop")) {
             if(dm.isOp(client.getUsername())) {
                 System.out.println(client.getUsername() + " stopped the server.");
                 System.exit(0);
             }
             else {
                 ph.sendPacket(new Packet5Message("You are not an op."), client, this.socket);
             }
         }
         else if(cmd.equalsIgnoreCase("say")) {
             if(dm.isOp(client.getUsername())) {
                 String msg = "";
                 for(int i = 0; i < args.length; i++) msg += args[i] + " ";
                 msg = msg.trim();
 
                 ph.sendAllPacket(new Packet5Message("[Server] " + msg), clients, this.socket);
             }
             else {
                 ph.sendPacket(new Packet5Message("You are not an op."), client, this.socket);
             }
         }
         else if(cmd.equalsIgnoreCase("ping")) {
             ph.sendPacket(new Packet5Message("Pong!"), client, this.socket);
         }
         else if(cmd.equalsIgnoreCase("kill")) {
             if(args.length < 1) {
                 ph.sendPacket(new Packet5Message("Not enough paramters."), client, this.socket);
             }
             else {
                 ph.sendAllExcludePacket(new Packet5Message(client.getUsername() + " was kicked for killing " + args[0] + ". " + args[0] + " will be missed. :("), clients, client, this.socket);
                 ph.sendPacket(new Packet4Kick("YOU MURDERER, YOU KILLED " + args[0].toUpperCase() + "! GET OUT!!!!!"), client, this.socket);
                 clients.remove(client);
                 client.stopKeepAliveThread();
                 client.stopKeepAliveSendThread();
             }
         }
         else if(cmd.equalsIgnoreCase("nuke")) {
             if(!dm.isOp(client.getUsername())) {
                 ph.sendPacket(new Packet5Message("You are not an op."), client, this.socket);
             }
             else if(!Boolean.parseBoolean(cfg.get("enable-nuke"))) {
                 ph.sendPacket(new Packet5Message("Nuke command is disabled"), client, this.socket);
             }
             else {
                 try {
                     System.out.println("Nuke started!");
                     ph.sendAllPacket(new Packet5Message("IT'S NUKE TIME OH BOY!!!!!"), clients, this.socket);
                     Thread.sleep(1000);
                     Packet5Message packet = new Packet5Message("NUKE NUKE NUKE NUKE NUKE NUKE NUKE NUKE NUKE NUKE NUKE NUKE NUKE");
                     for(int i = 0; i < 1000; i++) {
                         if((i % 100) == 0) System.out.println("Packets left: " + (1000 - i));
                         ph.sendAllPacket(packet, clients, this.socket);
                         Thread.sleep(10);
                     }
                     System.out.println("Nuke ended!");
                     ph.sendAllPacket(new Packet5Message("Phew, now that the nuke is over, continue chatting!"), clients, this.socket);
                 }
                 catch(InterruptedException e) {
                     System.out.println("Nuke command thread was interrupted.");
                 }
             }
         }
         else if(cmd.equalsIgnoreCase("whois")) {
             if(args.length < 1) {
                 ph.sendPacket(new Packet5Message("Not enough paramters."), client, this.socket);
             }
             else {
                 ClientData client2 = findClient(args[0]);
                 if(client2 != null) {
                     ph.sendPacket(new Packet5Message("IP: " + client2.getIP().getHostAddress()), client, this.socket);
                     ph.sendPacket(new Packet5Message("Port: " + client2.getPort()), client, this.socket);
                     ph.sendPacket(new Packet5Message("Hostname: " + client2.getIP().getCanonicalHostName()), client, this.socket);
                 }
                 else {
                     ph.sendPacket(new Packet5Message("User not found."), client, this.socket);
                 }
             }
         }
         else if(cmd.equalsIgnoreCase("list")) {
             String msg = "";
             int i = 0;
             for(i = 0; i < clients.size(); i++) {
                 ClientData client2 = (ClientData)clients.get(i);
                 msg += client2.getUsername() + ", ";
             }
 
             ph.sendPacket(new Packet5Message("Online Users (" + i + "): " + msg.substring(0, msg.length() - 2)), client, this.socket);
         }
         else if(cmd.equalsIgnoreCase("me")) {
             String msg = "";
             for(int i = 0; i < args.length; i++) msg += args[i] + " ";
             msg = msg.trim();
 
             System.out.println(client.getUsername() + " did action: " + msg);
             ph.sendAllPacket(new Packet5Message("*" + client.getUsername() + " " + msg), clients, this.socket);
         }
         else if(cmd.equalsIgnoreCase("nick")) {
             if(args.length < 1) {
                 ph.sendPacket(new Packet5Message("Not enough paramters."), client, this.socket);
             }
             else if(args[0].equalsIgnoreCase(client.getUsername())) {
                 ph.sendPacket(new Packet5Message("Your name is already " + args[0] + "."), client, this.socket);
             }
             else if(nameTaken(args[0]))  {
                 ph.sendPacket(new Packet5Message("That name is taken."), client, this.socket);
             }
             else if(dm.isOp(args[0]) && ((!dm.isOp(client.getUsername()) && Boolean.parseBoolean(cfg.get("ops-login")) && args.length < 2) || (!dm.isOp(client.getUsername()) && args.length < 2))) {
                 ph.sendPacket(new Packet5Message("You can't /nick to an op's name if you aren't an op or don't have the password."), client, this.socket);
             }
             else {
                if(dm.getUser(args[0]) != null && !dm.getUser(args[0]).equalsIgnoreCase("")) {
                     if(args.length < 2) {
                         ph.sendPacket(new Packet5Message("The password was invalid."), client, this.socket);
                         return;
                     }
                     
                     if(dm.checkUser(args[0], args[1])) {
                         ph.sendPacket(new Packet8PasswordChange(args[1]), client, this.socket);
                     }
                     else {
                         ph.sendPacket(new Packet5Message("The password was invalid."), client, this.socket);
                         return;
                     }
                 }
                 System.out.println(client.getUsername() + " has changed name to " + args[0]);
                 ph.sendPacket(new Packet6NameChange(args[0]), client, this.socket);
                 ph.sendAllPacket(new Packet5Message(client.getUsername() + " is now known as " + args[0]), clients, this.socket);
                 clients.remove(client);
                 client.setUsername(args[0]);
                 clients.add(client);
             }
         }
         else if(cmd.equalsIgnoreCase("password")) {
             if(args.length < 1) {
                 ph.sendPacket(new Packet5Message("Not enough paramters."), client, this.socket);
             }
             else if(args[0].equalsIgnoreCase("set")) {
                 if(args.length < 2) {
                     ph.sendPacket(new Packet5Message("Not enough paramters."), client, this.socket);
                 }
                 else {
                     client.setPass(args[1]);
                     dm.setUser(client.getUsername(), args[1]);
                     ph.sendPacket(new Packet8PasswordChange(args[1]), client, this.socket);
                     ph.sendPacket(new Packet5Message("Your password has been set."), client, this.socket);
                 }
             }
             else if(args[0].equalsIgnoreCase("remove")) {
                 client.setPass("");
                 dm.removeUser(client.getUsername());
                 ph.sendPacket(new Packet8PasswordChange(" "), client, this.socket);
                 ph.sendPacket(new Packet5Message("Your password has been removed."), client, this.socket);
             }
             else {
                 ph.sendPacket(new Packet5Message("Invalid parameter, please specify either set or remove."), client, this.socket);
             }
         }
         else if(cmd.equalsIgnoreCase("op")) {
             if(dm.isOp(client.getUsername())) {
                 if(args.length < 1) {
                     ph.sendPacket(new Packet5Message("Not enough paramters."), client, this.socket);
                 }
                 else if(args[0].equalsIgnoreCase(client.getUsername())) {
                     ph.sendPacket(new Packet5Message("You can not op yourself."), client, this.socket);
                 }
                 else if(dm.isOp(args[0])) {
                     ph.sendPacket(new Packet5Message("That user is already an op."), client, this.socket);
                 }
                 else {
                     dm.addOp(args[0]);
                     System.out.println(client.getUsername() + " opped " + args[0] + ".");
                     ph.sendPacket(new Packet5Message(args[0] + " has been opped."), client, this.socket);
                     ClientData client2 = findClient(args[0]);
                     if(client2 != null) ph.sendPacket(new Packet5Message("You are now an op!"), client2, this.socket);
                 }
             }
             else {
                 ph.sendPacket(new Packet5Message("You are not an op."), client, this.socket);
             }
         }
         else if(cmd.equalsIgnoreCase("deop")) {
             if(dm.isOp(client.getUsername())) {
                 if(args.length < 1) {
                     ph.sendPacket(new Packet5Message("Not enough paramters."), client, this.socket);
                 }
                 else if(args[0].equalsIgnoreCase(client.getUsername())) {
                     ph.sendPacket(new Packet5Message("You can not deop yourself."), client, this.socket);
                 }
                 else if(!dm.isOp(args[0])) {
                     ph.sendPacket(new Packet5Message("That user is not an op."), client, this.socket);
                 }
                 else {
                     dm.removeOp(args[0]);
                     System.out.println(client.getUsername() + " deopped " + args[0] + ".");
                     ph.sendPacket(new Packet5Message(args[0] + " has been deopped."), client, this.socket);
                     ClientData client2 = findClient(args[0]);
                     if(client2 != null) ph.sendPacket(new Packet5Message("You are no longer an op!"), client2, this.socket);
                 }
             }
             else {
                 ph.sendPacket(new Packet5Message("You are not an op."), client, this.socket);
             }
         }
         else if(cmd.equalsIgnoreCase("kick")) {
             if(dm.isOp(client.getUsername())) {
                 if(args.length < 1) {
                     ph.sendPacket(new Packet5Message("Not enough paramters."), client, this.socket);
                 }
                 else if(args[0].equalsIgnoreCase(client.getUsername())) {
                     ph.sendPacket(new Packet5Message("You can not kick yourself."), client, this.socket);
                 }
                 else if(findClient(args[0]) == null) {
                     ph.sendPacket(new Packet5Message("That user isn't in the chat."), client, this.socket);
                 }
                 else {
                     String msg = "";
                     for(int i = 1; i < args.length; i++) msg += args[i] + " ";
                     msg = msg.trim();
                     
                     System.out.println(client.getUsername() + " kicked " + args[0] + " with reason: " + msg);
                     ClientData client2 = findClient(args[0]);
                     ph.sendPacket(new Packet4Kick("You were kicked: " + (msg.equals("") ? "No reason." : msg)), client2, this.socket);
                     ph.sendAllPacket(new Packet5Message(args[0] + " has been kicked. (" + (msg.equals("") ? "No reason." : msg) + ")"), clients, this.socket);
                     clients.remove(client2);
                     client2.stopKeepAliveThread();
                     client2.stopKeepAliveSendThread();
                 }
             }
             else {
                 ph.sendPacket(new Packet5Message("You are not an op."), client, this.socket);
             }
         }
         else if(cmd.equalsIgnoreCase("ban")) {
             if(dm.isOp(client.getUsername())) {
                 if(args.length < 1) {
                     ph.sendPacket(new Packet5Message("Not enough paramters."), client, this.socket);
                 }
                 else if(args[0].equalsIgnoreCase(client.getUsername())) {
                     ph.sendPacket(new Packet5Message("You can not ban yourself."), client, this.socket);
                 }
                 else if(dm.isBanned(args[0])) {
                     ph.sendPacket(new Packet5Message("That user is already banned."), client, this.socket);
                 }
                 else {
                     String msg = "";
                     for(int i = 1; i < args.length; i++) msg += args[i] + " ";
                     msg = msg.trim();
 
                     dm.addBan(args[0]);
                     System.out.println(client.getUsername() + " banned " + args[0] + " with reason: " + msg);
                     ph.sendAllPacket(new Packet5Message(args[0] + " has been banned. (" + (msg.equals("") ? "No reason." : msg) + ")"), clients, this.socket);
                     ClientData client2 = findClient(args[0]);
                     if(client2 != null) {
                         ph.sendPacket(new Packet4Kick("You have been banned: " + (msg.equals("") ? "No reason." : msg)), client2, this.socket);
                         clients.remove(client2);
                         client2.stopKeepAliveThread();
                         client2.stopKeepAliveSendThread();
                     }
                 }
             }
             else {
                 ph.sendPacket(new Packet5Message("You are not an op."), client, this.socket);
             }
         }
         else if(cmd.equalsIgnoreCase("unban")) {
             if(dm.isOp(client.getUsername())) {
                 if(args.length < 1) {
                     ph.sendPacket(new Packet5Message("Not enough paramters."), client, this.socket);
                 }
                 else if(args[0].equalsIgnoreCase(client.getUsername())) {
                     ph.sendPacket(new Packet5Message("You can not unban yourself."), client, this.socket);
                 }
                 else if(!dm.isBanned(args[0])) {
                     ph.sendPacket(new Packet5Message("That user is not banned."), client, this.socket);
                 }
                 else {
                     dm.removeBan(args[0]);
                     System.out.println(client.getUsername() + " unbanned " + args[0] + ".");
                     ph.sendPacket(new Packet5Message(args[0] + " has been unbanned."), client, this.socket);
                 }
             }
             else {
                 ph.sendPacket(new Packet5Message("You are not an op."), client, this.socket);
             }
         }
         else if(cmd.equalsIgnoreCase("banip")) {
             if(dm.isOp(client.getUsername())) {
                 if(args.length < 1) {
                     ph.sendPacket(new Packet5Message("Not enough paramters."), client, this.socket);
                 }
                 else {
                     InetAddress ip = null;
                     try {
                         ip = InetAddress.getByName(args[0]);
                     }
                     catch(UnknownHostException e) {
                         //System.err.println("An invalid IP was entered.");
                     }
 
                     if(ip == null) {
                         ph.sendPacket(new Packet5Message("The IP is invalid."), client, this.socket);
                     }
                     else if(ip.getHostAddress().equalsIgnoreCase("127.0.0.1")) {
                         ph.sendPacket(new Packet5Message("You can not ban the local IP."), client, this.socket);
                     }
                     else if(ip.getHostAddress().equalsIgnoreCase(client.getIP().getHostAddress())) {
                         ph.sendPacket(new Packet5Message("You can not ban your own IP."), client, this.socket);
                     }
                     else if(dm.isIPBanned(ip.getHostAddress())) {
                         ph.sendPacket(new Packet5Message("That IP is already banned."), client, this.socket);
                     }
                     else {
                         String msg = "";
                         for(int i = 1; i < args.length; i++) msg += args[i] + " ";
                         msg = msg.trim();
                         
                         dm.addIPBan(ip.getHostAddress());
                         System.out.println(client.getUsername() + " banned the IP " + ip.getHostAddress() + " with reason: " + msg);
                         ph.sendAllPacket(new Packet5Message("The IP " + ip.getHostAddress() + " has been banned. (" + (msg.equals("") ? "No reason." : msg) + ")"), clients, this.socket);
                         ClientData client2 = findClient(ip);
                         if(client2 != null) {
                             ph.sendPacket(new Packet4Kick("Your IP has been banned: " + (msg.equals("") ? "No reason." : msg)), client2, this.socket);
                             clients.remove(client2);
                             client2.stopKeepAliveThread();
                             client2.stopKeepAliveSendThread();
                         }
                     }
                 }
             }
             else {
                 ph.sendPacket(new Packet5Message("You are not an op."), client, this.socket);
             }
         }
         else if(cmd.equalsIgnoreCase("unbanip")) {
             if(dm.isOp(client.getUsername())) {
                 if(args.length < 1) {
                     ph.sendPacket(new Packet5Message("Not enough paramters."), client, this.socket);
                 }
                 else {
                     InetAddress ip = null;
                     try {
                         ip = InetAddress.getByName(args[0]);
                     }
                     catch(UnknownHostException e) {
                         //System.err.println("An invalid IP was entered.");
                     }
 
                     if(ip == null) {
                         ph.sendPacket(new Packet5Message("The IP is invalid."), client, this.socket);
                     }
                     else if(ip.getHostAddress().equalsIgnoreCase(client.getIP().getHostAddress())) {
                         ph.sendPacket(new Packet5Message("You can not unban your own IP."), client, this.socket);
                     }
                     else if(!dm.isIPBanned(ip.getHostAddress())) {
                         ph.sendPacket(new Packet5Message("That IP is not banned."), client, this.socket);
                     }
                     else {
                         dm.removeIPBan(ip.getHostAddress());
                         System.out.println(client.getUsername() + " unbanned the IP " + ip.getHostAddress() + ".");
                         ph.sendPacket(new Packet5Message("The IP " + ip.getHostAddress() + " has been unbanned."), client, this.socket);
                     }
                 }
             }
             else {
                 ph.sendPacket(new Packet5Message("You are not an op."), client, this.socket);
             }
         }
 
         else {
             System.out.println("Command \"" + cmd + "\" not found.");
             ph.sendPacket(new Packet5Message("Unknown command."), client, this.socket);
         }
     }
 
     private boolean nameTaken(String name) {
         for(int i = 0; i < clients.size(); i++) {
             ClientData client2 = (ClientData)clients.get(i);
             if(client2.getUsername().equalsIgnoreCase(name)) return true;
         }
         return false;
     }
 
     private ClientData findClient(String name) {
         for(int i = 0; i < clients.size(); i++) {
             ClientData client2 = (ClientData)clients.get(i);
             if(client2.getUsername().equalsIgnoreCase(name)) return client2;
         }
         return null;
     }
 
     private ClientData findClient(InetAddress ip) {
         for(int i = 0; i < clients.size(); i++) {
             ClientData client2 = (ClientData)clients.get(i);
             if(client2.getIP().getHostAddress().equals(ip.getHostAddress())) return client2;
         }
         return null;
     }
 }
