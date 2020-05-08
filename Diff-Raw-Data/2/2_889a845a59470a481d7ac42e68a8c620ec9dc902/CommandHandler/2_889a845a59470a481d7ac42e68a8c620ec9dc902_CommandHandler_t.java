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
 import com.simplechat.protocol.*;
 
 public class CommandHandler {
     private ClientData client;
     private List clients;
     private DatagramSocket socket;
 
 
     public CommandHandler(ClientData client, List clients, DatagramSocket socket) {
         this.client = client;
         this.clients = clients;
         this.socket = socket;
     }
 
     public void parseCommand(String cmd, String[] args) {
         PacketHandler ph = new PacketHandler();
         DataManager dm = new DataManager();
         
         if(cmd.equalsIgnoreCase("quit")) {
             String msg = "";
             for(int i = 0; i < args.length; i++) msg += args[i] + " ";
             msg = msg.trim();
 
             System.out.println(client.getUsername() + " quit. Reason: " + msg) ;
             Packet4Kick packet = new Packet4Kick("Quitting. Reason: " + msg);
             Packet5Message packet2 = new Packet5Message(client.getUsername() + " quit. (" + msg + ")");
             ph.sendPacket(packet, client, this.socket);
             client.stopKeepAliveThread();
             clients.remove(client);
             ph.sendAllPacket(packet2, clients, this.socket);
         }
         else if(cmd.equalsIgnoreCase("stop")) {
             if(dm.isOp(client.getUsername())) {
                 System.out.println(client.getUsername() + " stopped the server.");
                 System.exit(0);
             }
             else {
                 Packet5Message packet = new Packet5Message("You are not an op.");
                 ph.sendPacket(packet, client, this.socket);
             }
         }
         else if(cmd.equalsIgnoreCase("say")) {
             if(dm.isOp(client.getUsername())) {
                 String msg = "";
                 for(int i = 0; i < args.length; i++) msg += args[i] + " ";
                 msg = msg.trim();
 
                 Packet5Message packet = new Packet5Message("[Server] " + msg);
                 ph.sendAllPacket(packet, clients, this.socket);
             }
             else {
                 Packet5Message packet = new Packet5Message("You are not an op.");
                 ph.sendPacket(packet, client, this.socket);
             }
         }
         else if(cmd.equalsIgnoreCase("ping")) {
             Packet5Message packet = new Packet5Message("Pong!");
             ph.sendPacket(packet, client, this.socket);
         }
         else if(cmd.equalsIgnoreCase("list")) {
             String msg = "Online Users: ";
             for(int i = 0; i < clients.size(); i++) {
                 ClientData client2 = (ClientData)clients.get(i);
                 msg += client2.getUsername() + ", ";
             }
 
            Packet5Message packet = new Packet5Message(msg.substring(0, Math.min(msg.length() - 2, 500)));
             ph.sendPacket(packet, client, this.socket);
         }
         else if(cmd.equalsIgnoreCase("me")) {
             String msg = "";
             for(int i = 0; i < args.length; i++) msg += args[i] + " ";
             msg = msg.trim();
 
             System.out.println(client.getUsername() + " did action: " + msg);
             Packet5Message packet = new Packet5Message("*" + client.getUsername() + " " + msg);
             ph.sendAllPacket(packet, clients, this.socket);
         }
         else if(cmd.equalsIgnoreCase("nick")) {
             if(args.length < 1) {
                 Packet5Message packet = new Packet5Message("Not enough paramters.");
                 ph.sendPacket(packet, client, this.socket);
             }
             else if(args[0].equalsIgnoreCase(client.getUsername())) {
                 Packet5Message packet = new Packet5Message("Your name is already " + args[0] + ".");
                 ph.sendPacket(packet, client, this.socket);
             }
             else if(nameTaken(args[0]))  {
                 Packet5Message packet = new Packet5Message("That name is taken.");
                 ph.sendPacket(packet, client, this.socket);
             }
             else if(dm.isOp(args[0]) && !dm.isOp(client.getUsername())) {
                 Packet5Message packet = new Packet5Message("You can't /nick to an op's name if you aren't an op.");
                 ph.sendPacket(packet, client, this.socket);
             }
             else {
                 System.out.println(client.getUsername() + " has changed name to " + args[0]);
                 Packet5Message packet = new Packet5Message(client.getUsername() + " is now known as " + args[0]);
                 clients.remove(client);
                 client.setUsername(args[0]);
                 clients.add(client);
                 Packet6NameChange packet2 = new Packet6NameChange(args[0]);
                 ph.sendAllPacket(packet, clients, this.socket);
                 ph.sendPacket(packet2, client, this.socket);
             }
         }
         else if(cmd.equalsIgnoreCase("op")) {
             if(dm.isOp(client.getUsername())) {
                 if(args.length < 1) {
                     Packet5Message packet = new Packet5Message("Not enough paramters.");
                     ph.sendPacket(packet, client, this.socket);
                 }
                 else if(args[0].equalsIgnoreCase(client.getUsername())) {
                     Packet5Message packet = new Packet5Message("You can not op yourself.");
                     ph.sendPacket(packet, client, this.socket);
                 }
                 else if(dm.isOp(args[0])) {
                     Packet5Message packet = new Packet5Message("That user is already an op.");
                     ph.sendPacket(packet, client, this.socket);
                 }
                 else {
                     dm.addOp(args[0]);
                     System.out.println(client.getUsername() + " opped " + args[0] + ".");
                     Packet5Message packet = new Packet5Message(args[0] + " has been opped.");
                     Packet5Message packet2 = new Packet5Message("You are now an op!");
                     ph.sendPacket(packet, client, this.socket);
                     ClientData client2 = findClient(args[0]);
                     if(client2 != null) ph.sendPacket(packet2, client2, this.socket);
                 }
             }
             else {
                 Packet5Message packet = new Packet5Message("You are not an op.");
                 ph.sendPacket(packet, client, this.socket);
             }
         }
         else if(cmd.equalsIgnoreCase("deop")) {
             if(dm.isOp(client.getUsername())) {
                 if(args.length < 1) {
                     Packet5Message packet = new Packet5Message("Not enough paramters.");
                     ph.sendPacket(packet, client, this.socket);
                 }
                 else if(args[0].equalsIgnoreCase(client.getUsername())) {
                     Packet5Message packet = new Packet5Message("You can not deop yourself.");
                     ph.sendPacket(packet, client, this.socket);
                 }
                 else if(!dm.isOp(args[0])) {
                     Packet5Message packet = new Packet5Message("That user is not an op.");
                     ph.sendPacket(packet, client, this.socket);
                 }
                 else {
                     dm.removeOp(args[0]);
                     System.out.println(client.getUsername() + " deopped " + args[0] + ".");
                     Packet5Message packet = new Packet5Message(args[0] + " has been deopped.");
                     Packet5Message packet2 = new Packet5Message("You are no longer an op!");
                     ph.sendPacket(packet, client, this.socket);
                     ClientData client2 = findClient(args[0]);
                     if(client2 != null) ph.sendPacket(packet2, client2, this.socket);
                 }
             }
             else {
                 Packet5Message packet = new Packet5Message("You are not an op.");
                 ph.sendPacket(packet, client, this.socket);
             }
         }
         else if(cmd.equalsIgnoreCase("kick")) {
             if(dm.isOp(client.getUsername())) {
                 if(args.length < 1) {
                     Packet5Message packet = new Packet5Message("Not enough paramters.");
                     ph.sendPacket(packet, client, this.socket);
                 }
                 else if(args[0].equalsIgnoreCase(client.getUsername())) {
                     Packet5Message packet = new Packet5Message("You can not kick yourself.");
                     ph.sendPacket(packet, client, this.socket);
                 }
                 else if(findClient(args[0]) == null) {
                     Packet5Message packet = new Packet5Message("That user isn't in the chat.");
                     ph.sendPacket(packet, client, this.socket);
                 }
                 else {
                     String msg = "";
                     for(int i = 1; i < args.length; i++) msg += args[i] + " ";
                     msg = msg.trim();
                     
                     System.out.println(client.getUsername() + " kicked " + args[0] + " with reason: " + msg);
                     Packet5Message packet = new Packet5Message(args[0] + " has been kicked. (" + (msg.equals("") ? "No reason." : msg) + ")");
                     Packet4Kick packet2 = new Packet4Kick("You were kicked: " + (msg.equals("") ? "No reason." : msg));
                     ClientData client2 = findClient(args[0]);
                     ph.sendPacket(packet2, client2, this.socket);
                     client2.stopKeepAliveThread();
                     clients.remove(client2);
                     ph.sendAllPacket(packet, clients, this.socket);
                 }
             }
             else {
                 Packet5Message packet = new Packet5Message("You are not an op.");
                 ph.sendPacket(packet, client, this.socket);
             }
         }
         else if(cmd.equalsIgnoreCase("ban")) {
             if(dm.isOp(client.getUsername())) {
                 if(args.length < 1) {
                     Packet5Message packet = new Packet5Message("Not enough paramters.");
                     ph.sendPacket(packet, client, this.socket);
                 }
                 else if(args[0].equalsIgnoreCase(client.getUsername())) {
                     Packet5Message packet = new Packet5Message("You can not ban yourself.");
                     ph.sendPacket(packet, client, this.socket);
                 }
                 else if(dm.isBanned(args[0])) {
                     Packet5Message packet = new Packet5Message("That user is already banned.");
                     ph.sendPacket(packet, client, this.socket);
                 }
                 else {
                     String msg = "";
                     for(int i = 1; i < args.length; i++) msg += args[i] + " ";
                     msg = msg.trim();
 
                     dm.addBan(args[0]);
                     System.out.println(client.getUsername() + " banned " + args[0] + " with reason: " + msg);
                     Packet5Message packet = new Packet5Message(args[0] + " has been banned. (" + (msg.equals("") ? "No reason." : msg) + ")");
                     Packet4Kick packet2 = new Packet4Kick("You have been banned: " + (msg.equals("") ? "No reason." : msg));
                     ClientData client2 = findClient(args[0]);
                     if(client2 != null) {
                         ph.sendPacket(packet2, client2, this.socket);
                         client2.stopKeepAliveThread();
                         clients.remove(client2);
                     }
                     ph.sendAllPacket(packet, clients, this.socket);
                 }
             }
             else {
                 Packet5Message packet = new Packet5Message("You are not an op.");
                 ph.sendPacket(packet, client, this.socket);
             }
         }
         else if(cmd.equalsIgnoreCase("unban")) {
             if(dm.isOp(client.getUsername())) {
                 if(args.length < 1) {
                     Packet5Message packet = new Packet5Message("Not enough paramters.");
                     ph.sendPacket(packet, client, this.socket);
                 }
                 else if(args[0].equalsIgnoreCase(client.getUsername())) {
                     Packet5Message packet = new Packet5Message("You can not unban yourself.");
                     ph.sendPacket(packet, client, this.socket);
                 }
                 else if(!dm.isBanned(args[0])) {
                     Packet5Message packet = new Packet5Message("That user is not banned.");
                     ph.sendPacket(packet, client, this.socket);
                 }
                 else {
                     dm.removeBan(args[0]);
                     System.out.println(client.getUsername() + " unbanned " + args[0] + ".");
                     Packet5Message packet = new Packet5Message(args[0] + " has been unbanned.");
                     ph.sendPacket(packet, client, this.socket);
                 }
             }
             else {
                 Packet5Message packet = new Packet5Message("You are not an op.");
                 ph.sendPacket(packet, client, this.socket);
             }
         }
         else if(cmd.equalsIgnoreCase("banip")) {
             if(dm.isOp(client.getUsername())) {
                 if(args.length < 1) {
                     Packet5Message packet = new Packet5Message("Not enough paramters.");
                     ph.sendPacket(packet, client, this.socket);
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
                         Packet5Message packet = new Packet5Message("The IP is invalid.");
                         ph.sendPacket(packet, client, this.socket);
                     }
                     else if(ip == client.getIP()) {
                         Packet5Message packet = new Packet5Message("You can not ban your own IP.");
                         ph.sendPacket(packet, client, this.socket);
                     }
                     else if(dm.isIPBanned(ip.getHostAddress())) {
                         Packet5Message packet = new Packet5Message("That IP is already banned.");
                         ph.sendPacket(packet, client, this.socket);
                     }
                     else {
                         String msg = "";
                         for(int i = 1; i < args.length; i++) msg += args[i] + " ";
                         msg = msg.trim();
                         
                         dm.addIPBan(ip.getHostAddress());
                         System.out.println(client.getUsername() + " banned the IP " + ip.getHostAddress() + " with reason: " + msg);
                         Packet5Message packet = new Packet5Message("The IP " + ip.getHostAddress() + " has been banned. (" + (msg.equals("") ? "No reason." : msg) + ")");
                         Packet4Kick packet2 = new Packet4Kick("Your IP has been banned: " + (msg.equals("") ? "No reason." : msg));
                         ClientData client2 = findClient(ip);
                         if(client2 != null) {
                             ph.sendPacket(packet2, client2, this.socket);
                             client2.stopKeepAliveThread();
                             clients.remove(client2);
                         }
                         ph.sendAllPacket(packet, clients, this.socket);
                     }
                 }
             }
             else {
                 Packet5Message packet = new Packet5Message("You are not an op.");
                 ph.sendPacket(packet, client, this.socket);
             }
         }
         else if(cmd.equalsIgnoreCase("unbanip")) {
             if(dm.isOp(client.getUsername())) {
                 if(args.length < 1) {
                     Packet5Message packet = new Packet5Message("Not enough paramters.");
                     ph.sendPacket(packet, client, this.socket);
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
                         Packet5Message packet = new Packet5Message("The IP is invalid.");
                         ph.sendPacket(packet, client, this.socket);
                     }
                     else if(ip == client.getIP()) {
                         Packet5Message packet = new Packet5Message("You can not unban your own IP.");
                         ph.sendPacket(packet, client, this.socket);
                     }
                     else if(!dm.isIPBanned(ip.getHostAddress())) {
                         Packet5Message packet = new Packet5Message("That IP is not banned.");
                         ph.sendPacket(packet, client, this.socket);
                     }
                     else {
                         dm.removeIPBan(ip.getHostAddress());
                         System.out.println(client.getUsername() + " unbanned the IP " + ip.getHostAddress() + ".");
                         Packet5Message packet = new Packet5Message("The IP " + ip.getHostAddress() + " has been unbanned.");
                         ph.sendPacket(packet, client, this.socket);
                     }
                 }
             }
             else {
                 Packet5Message packet = new Packet5Message("You are not an op.");
                 ph.sendPacket(packet, client, this.socket);
             }
         }
 
         else {
             System.out.println("Command \"" + cmd + "\" not found.");
             Packet5Message packet = new Packet5Message("Unknown command.");
             ph.sendPacket(packet, client, this.socket);
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
             if(client2.getIP() == ip) return client2;
         }
         return null;
     }
 }
