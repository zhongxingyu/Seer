 package co.raawr.tempest.core;
 
 import java.io.IOException;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 
 /**
  *
  * @author Raawr Tempest Labs
  */
 public class Core {
 
     private Out out;
     private In in;
     private Socket sock;
     private String nick = "EDI";
     private String version = "1.0";
     private String name = "EDI";
     private ArrayList<String> channels;
     private boolean connected;
     private boolean verbose = true;
     public volatile boolean listening;
 
     public Core() {
         print("Initializing core system functions...");
         channels = new ArrayList<>();
         print("Awaiting core commands...");
     }
 
     public void print(String s) {
         if (verbose) {
             String d = "[" + (new Timestamp((new java.util.Date()).getTime())) + "]";
             System.out.println(d + " " + s);
         }
     }
 
     public void err(String s) {
         String d = "[" + (new Timestamp((new java.util.Date()).getTime())) + "]";
         System.out.println(d + " WARNING: " + s);
     }
 
     public void onConnection() {
     }
 
     public boolean isConnected() {
         return connected;
     }
 
     public boolean isVerbose() {
         return verbose;
     }
 
     public void setVerbose(boolean b) {
         verbose = b;
     }
 
     public void connect(String server, int port) {
         try {
             print("Attempting to connect...");
 
             sock = new Socket(server, port);
             listening = true;
             out = new Out(sock, this);
             out.start();
 
             print("Connection to server (" + server + ":" + port + ") established.");
             print("Attempting to register identity...");
 
             out.forceLine("NICK " + nick);
             out.forceLine("USER " + name + " 8 * :" + version);
 
             in = new In(sock, this);
             in.start();
 
             print("Host and identity registered.");
 
         } catch (UnknownHostException ex) {
             err("Unable to resolve hostname.");
         } catch (IOException ex) {
             err("I/O exception.");
         }
     }
 
     public void disconnect() {
         try {
 
             print("Initiating disconnection sequence...");
             out.forceLine("QUIT");
             listening = false;
             connected = false;
             sock.close();
             print("Disconnected successfully.");
             onDisconnect();
 
         } catch (IOException ex) {
             err("I/O exception.");
         }
     }
 
     public void disconnect(String reason) {
         try {
 
             print("Initiating disconnection sequence...");
             out.forceLine("QUIT :" + reason);
             listening = false;
             connected = false;
             sock.close();
             print("Disconnected successfully.");
             onDisconnect();
 
         } catch (IOException ex) {
             err("I/O exception.");
         }
     }
 
     public void onDisconnect() {
     }
 
     public void kick(String channel, String nick) {
         out.forceLine("KICK " + channel + " " + nick);
     }
 
     public void kick(String channel, String nick, String reason) {
         out.forceLine("KICK " + channel + " " + nick + " :" + reason);
     }
 
     public void onKick(String channel, String sourceNick, String targetNick, String reason) {
     }
 
     public void ban(String channel, String nick) {
         setMode(channel, "+b", new String[]{nick});
     }
 
     public void setMode(String channel, String mode, String[] args) {
         String arguments = "";
         for (String s : args) {
             arguments = arguments.concat(" " + s);
         }
         String a = "MODE " + channel + " " + mode + " " + arguments;
         out.forceLine(a.trim());
     }
 
     public void setNick(String nick) {
         this.nick = nick.trim();
     }
 
     public void setName(String name) {
         this.name = name.trim();
     }
 
     public void setVersion(String version) {
         this.version = version.trim();
     }
 
     public String getNick() {
         return nick;
     }
 
     public String getName() {
         return name;
     }
 
     public String getVersion() {
         return version;
     }
 
     public void handleLine(String line) {
         print(line);
 
         String[] parse = line.split(" ");
 
         //Check for misc commands
         switch (parse[0]) {
             case "PING":
                 onPing(parse[1]);
                 return;
             case "ERROR":
                 err("Critical IRC error.");
                 return;
         }
 
         String mask;
         if (line.contains(":")) {
             //Extract command source mask
             mask = line.substring(line.indexOf(":") + 1, line.indexOf(" "));
         } else {
             //Unknown line, halt
             err("Unknown line received.");
             return;
         }
 
         //Check for numeric status codes
         if (isInteger(parse[1])) {
             switch (Integer.parseInt(parse[1])) {
                 case 1:
                     onConnection();
                     connected = true;
                     break;
             }
             return;
         }
 
         //No numeric code if here
         //Parse command
         if (line.startsWith(":")) {
             switch (parse[1]) {
                 case "PRIVMSG":
                     handleMessage(mask, line);
                     break;
                 case "NOTICE":
                     handleNotice(mask, line);
                     break;
                 case "MODE":
                     handleMode(mask, line);
                     break;
                 case "KICK":
                     handleKick(mask, line);
                     break;
                 case "JOIN":
                     handleJoin(mask, line);
                     break;
                 case "PART":
                     handlePart(mask, line);
                     break;
                 default:
                     //Unhandled command
                     break;
             }
         }
     }
 
     public static boolean isInteger(String s) {
         try {
             Integer.parseInt(s);
         } catch (NumberFormatException e) {
             return false;
         }
         // only got here if we didn't return false
         return true;
     }
 
     public void joinChannel(String channel) {
         if (channel.startsWith("#")) {
             out.queueLine("JOIN " + channel);
             channels.add(channel);
         }
         print("Joined channel successfully.");
     }
 
     public void onPing(String server) {
         out.queueLine("PONG " + server);
     }
 
     private void handleMessage(String hostmask, String line) {
         String[] parse = line.split(" ");
         String message = line.substring(line.indexOf(":", 1) + 1);
         String nickname = hostmask.substring(0, hostmask.indexOf("!"));
         String realname = hostmask.substring(hostmask.indexOf("!") + 1, hostmask.indexOf("@"));
         String hostname = hostmask.substring(hostmask.indexOf("@") + 1);
         String channel = parse[2];
 
         if (message.startsWith("\001")) {
             switch (message.replaceAll("\001", "")) {
                 case "VERSION":
                     sendNotice(nickname, "\001VERSION " + version + "\001");
                     break;
             }
        } else if (channel.equals(getNick())) {
             onPrivateMessage(nickname, message);
         } else {
             onMessage(nickname, channel, message);
         }
 
     }
 
     private void handleMode(String hostmask, String line) {
         String[] parse = line.split(" ");
         String message = line.substring(line.indexOf(":", 1) + 1);
         String nickname, realname, hostname = "";
         if (hostmask.contains("!")) {
             nickname = hostmask.substring(0, hostmask.indexOf("!"));
             realname = hostmask.substring(hostmask.indexOf("!") + 1, hostmask.indexOf("@"));
             hostname = hostmask.substring(hostmask.indexOf("@") + 1);
         } else {
             String servermask = hostmask.replace(":", "");
             return;
         }
         String channel = parse[2];
 
         //Disregard if target is not a channel
         if (!parse[2].startsWith("#")) {
             return;
         }
 
         //:hostmask MODE channel +mode list
         String modes = parse[3];
         boolean plus = false;
         int mc = 1;
         for (int i = 0; i < modes.length(); i++) {
             String c = String.valueOf(modes.charAt(i));
             switch (c) {
                 case "+":
                     plus = true;
                     break;
                 case "-":
                     plus = false;
                     break;
                 default:
                     if (parse.length >= mc + 3) {
                         onChannelUserMode(nickname, channel, (plus ? "+" : "-") + c, parse[mc + 3]);
                         mc++;
                     }
                     break;
             }
         }
     }
 
     public void onPart(String nick, String channel, String message) {
 
     }
 
     public void onChannelUserMode(String sourceNick, String channel, String mode, String target) {
     }
 
     public void onChannelMode(String sourceNick, String channel, String mode, String parameter) {
     }
 
     public void onMessage(String sender, String channel, String message) {
     }
 
     public void onPrivateMessage(String sender, String message) {
     }
 
     public void onNotice(String sender, String message) {
     }
 
     public void sendLine(String line) {
         out.queueLine(line);
     }
 
     public void forceSendLine(String line) {
         out.forceLine(line);
     }
 
     public void sendMessage(String target, String message) {
         out.queueLine("PRIVMSG " + target + " :" + message);
     }
 
     public void sendNotice(String target, String message) {
         out.queueLine("NOTICE " + target + " :" + message);
     }
 
     public void identify(String password) {
         sendMessage("NickServ", "IDENTIFY " + password);
     }
 
     public void register(String email, String password) {
         sendMessage("NickServ", "REGISTER " + password + " " + email);
     }
 
     public void confirmRegistration(String code) {
         sendMessage("NickServ", "CONFIRM " + code);
     }
 
     private void handleNotice(String hostmask, String line) {
         String message = line.substring(line.indexOf(":", 1) + 1);
         onNotice(getNick(hostmask), message);
     }
 
     private void handleJoin(String hostmask, String line) {
         String[] parse = line.split(" ");
         onJoin(getNick(hostmask), parse[2].replaceFirst(":", ""));
     }
 
     private void handlePart(String hostmask, String line) {
         String[] parse = line.split(" ");
         String reason = parse.length == 4 ? line.substring(line.indexOf(":")) : "" ;
         onPart(getNick(hostmask), parse[2], reason);
     }
 
     public void onJoin(String nick, String channel) {
     }
 
     private String getNick(String hostmask) {
         if (hostmask.matches(".+!.+@.+")) {
             String nickname = hostmask.substring(0, hostmask.indexOf("!"));
             return nickname;
         } else {
             return hostmask;
         }
     }
 
     private void handleKick(String mask, String line) {
         String[] parse = line.split(" ");
         String nick = getNick(mask);
         String channel = parse[2];
         String target = parse[3];
         String reason = "";
         if (parse.length > 3) {
             reason = line.substring(line.indexOf(":", 1) + 1);
         }
         onKick(channel, nick, target, reason);
     }
 }
