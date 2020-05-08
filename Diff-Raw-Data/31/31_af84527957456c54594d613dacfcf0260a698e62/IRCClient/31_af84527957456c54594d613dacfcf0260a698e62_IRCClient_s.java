 package com.ben.ber;
 
 //import org.schwering.irc.lib.IRCConnection;
import org.schwering.irc.lib.IRCConnection;
import org.schwering.irc.lib.IRCEventListener;
import org.schwering.irc.lib.IRCModeParser;
import org.schwering.irc.lib.IRCUser;
 import org.schwering.irc.lib.ssl.SSLIRCConnection;
 import org.schwering.irc.lib.ssl.SSLTrustManager;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.IOException;
 import java.security.cert.X509Certificate;
 import java.util.Hashtable;
 
 
 public class IRCClient extends Thread {
     private static GUI gui;
     /**
      * Reads input from the console.
      */
     private BufferedReader in;
 
     /**
      * The IRC connection.
      */
     private static IRCConnection conn;
 
     /**
      * The current default target of PRIVMSGs (a channel or nickname).
      */
     private static String target;
     private static String nickservPass;
     private static String channelToJoin;
 
     /**
      * Parses the arguments and starts the client.
      */
     public static void startLogin(String[] args) {
         Hashtable ht = null;
         try {
             ht = getHashtable(args);
         } catch (IllegalArgumentException exc) {
             printHelp();
             return;
         }
         String host = (String) ht.get("host");
         int port = new Integer((String) ht.get("port"));
         String pass = (String) ht.get("pass");
         String nick = (String) ht.get("nick");
         String user = (String) ht.get("user");
         String name = (String) ht.get("name");
         boolean ssl = (Boolean) ht.get("ssl");
         try {
             new IRCClient(host, port, pass, nick, user, name, ssl);
         } catch (IOException exc) {
             exc.printStackTrace();
             printHelp();
         }
     }
 
 
     public static void main(String[] args) {
 
         Thread thread = new Thread(gui = GUI.getGUIObject());
         new ApiInfo();
 
 
     }
 
     /**
      * Returns a hashtable with settings like host, port, nick etc..
      */
     private static Hashtable getHashtable(String[] args) {
         Hashtable<String, Object> ht = new Hashtable<String, Object>();
         String serverPort = (String) getParam(args, "server");
         int colon = serverPort.indexOf(':');
         ht.put("host", serverPort.substring(0, colon));
         ht.put("port", serverPort.substring(colon + 1));
         ht.put("pass", getParam(args, "pass", ""));
         ht.put("nick", getParam(args, "nick"));
         ht.put("user", getParam(args, "user", ht.get("nick")));
         ht.put("name", getParam(args, "name", ht.get("user")));
         ht.put("ssl", getParam(args, "ssl", false));
         return ht;
     }
 
     /**
      * Returns a value of a key in the arguments.
      */
     private static Object getParam(String[] args, Object key) {
         return getParam(args, key, null);
     }
 
     /**
      * Returns a value of a key in the arguments. If a key without a value is
      * found, a Boolean object with true is returned. If no key is found, the
      * default value is returned.
      */
     private static Object getParam(String[] args, Object key, Object def) {
         for (int i = 0; i < args.length; i++) {
             if (args[i].equalsIgnoreCase("-" + key)) {
                 if (i + 1 < args.length) {
                     String value = args[i + 1];
                     if (value.charAt(0) == '-')
                         return new Boolean(true);
                     else
                         return value;
                 } else {
                     return new Boolean(true);
                 }
             }
         }
         if (def != null)
             return def;
         else
             throw new IllegalArgumentException("No value for " + key + " found.");
     }
 
     /**
      * Prints some help.
      */
     private static void printHelp() {
         print("A simple command-line IRC client based on IRClib.");
         print("");
         print("Use it as follows:");
         print("java Client -server <server:port> [-pass <server-password<] -nick " +
                 "<nickname> [-user <username>] [-name <realname>] [-ssl]");
         print("");
         print("Note that you need the IRClib classes in your classpath.");
         print("You can get IRClib from http://moepii.sourceforge.net.");
         print("");
         print("Copyright (C) 2003, 2004, 2005, 2006 Christoph Schwering");
     }
 
     /**
      * A shorthand for the System.out.println method.
      */
     private static void print(Object o) {
         System.out.println(o);
     }
 
     /**
      * Checks wether a string starts with another string (case insensitive).
      */
     private static boolean startsWith(String s1, String s2) {
         return (s1.length() >= s2.length()) ?
                 s1.substring(0, s2.length()).equalsIgnoreCase(s2) : false;
     }
 
     /**
      * Creates a new IRCConnection instance and starts the thread.
      * <p/>
      * If you get confused by the two setDaemon()s: The conn.setDaemon(false) marks the
      * IRCConnection thread as user thread and the setDaemon(true) marks this class's thread
      * (which just listens for keyboard input) as daemon thread. Thus, if the IRCConnection
      * breaks, this console application shuts down, because due to the setDaemon(true) it
      * will no longer wait for keyboard input (no input would make sense without being
      * connected to a server).
      */
     public IRCClient(String host, int port, String pass, String nick, String user,
                      String name, boolean ssl) throws IOException {
         in = new BufferedReader(new InputStreamReader(System.in));
         if (conn != null && conn.isConnected()) {
             conn.close();
         }
 
         if (!ssl) {
             conn = new IRCConnection(host, new int[]{port}, pass, nick, user,
                     name);
         } else {
             conn = new SSLIRCConnection(host, new int[]{port}, pass, nick, user,
                     name);
            ((SSLIRCConnection) conn).addTrustManager(new TrustManager());
         }
 
         conn.addIRCEventListener(new Listener());
         conn.setEncoding("UTF-8");
         conn.setPong(true);
         conn.setDaemon(false);
         conn.setColors(false);
         conn.connect();
         setDaemon(true);
         start();
         doNickservPassword();
         conn.send("join " + channelToJoin);
         target = channelToJoin;
     }
 
     // Rizon nickserv login
     public static void doNickservPassword() {
         if (nickservPass != null) {
             conn.doPrivmsg("NickServ", "IDENTIFY " + nickservPass);
         }
 
     }
 
     public static void setNickservPass(String pass) {
         nickservPass = pass;
     }
 
     public static void setChannel(String channel) {
         channelToJoin = channel;
     }
 
     /**
      * The thread waits for input.
      */
     public void run() {
       /*  while (true) {
             try {
                 shipInput();
             } catch (Exception exc) {
                 exc.printStackTrace();
             }
         }  */
     }
 
     /**
      * Parses the input and sends it to the IRC server.
      *
      * Broken as fuck, not accepting some commands with multiple parameters
      */
     public static void shipInput(String input) throws Exception {
         if (input == null || input.length() == 0){
             return;
         }
 
         if (input.charAt(0) == '/') {
             if (startsWith(input, "/TARGET")) {
                 target = input.substring(8);
                 return;
             } else if (startsWith(input, "/JOIN")) {
                 target = input.substring(6);
            } else if (startsWith(input, "/MSG")) {
                input = input.substring(4);
                print("Exec: PRIVMSG " + input);
                conn.send("PRIVMSG " +input);
                 return;
             }
             input = input.substring(1);
             print("Exec: " + input);
             conn.send(input);
         } else {
             if (input.equals("getTimeout")) {
                 print(Integer.toString(conn.getTimeout()));
             }
             conn.doPrivmsg(target, input);
             // conn.send(input);
             print(target + "> " + input);
         }
     }
     // not used
     public class TrustManager implements SSLTrustManager {
         private X509Certificate[] chain;
 
         public X509Certificate[] getAcceptedIssuers() {
             return chain != null ? chain : new X509Certificate[0];
         }
 
         public boolean isTrusted(X509Certificate[] chain) {
             System.out.println("Do you want to trust? [yes/no]");
             String s;
             try {
                 s = in.readLine();
             } catch (Exception exc) {
                 exc.printStackTrace();
                 return false;
             }
             if (s.equalsIgnoreCase("yes")) {
                 this.chain = chain;
                 return true;
             } else {
                 return false;
             }
         }
     }
 
     /**
      * Treats IRC events. The most of them are just printed.
      */
     public class Listener implements IRCEventListener {
 
         public void onRegistered() {
             print("Connected");
         }
 
         public void onDisconnected() {
             print("Disconnected");
         }
 
         public void onError(String msg) {
             print("Error: " + msg);
         }
 
         public void onError(int num, String msg) {
             print("Error #" + num + ": " + msg);
         }
 
         public void onInvite(String chan, IRCUser u, String nickPass) {
             print(chan + "> " + u.getNick() + " invites " + nickPass);
         }
 
         public void onJoin(String chan, IRCUser u) {
             print(chan + "> " + u.getNick() + " joins");
         }
 
         public void onKick(String chan, IRCUser u, String nickPass, String msg) {
             print(chan + "> " + u.getNick() + " kicks " + nickPass);
         }
 
         public void onMode(IRCUser u, String nickPass, String mode) {
             print("Mode: " + u.getNick() + " sets modes " + mode + " " +
                     nickPass);
         }
 
         public void onMode(String chan, IRCUser u, IRCModeParser mp) {
             print(chan + "> " + u.getNick() + " sets mode: " + mp.getLine());
         }
 
         public void onNick(IRCUser u, String nickNew) {
             print("Nick: " + u.getNick() + " is now known as " + nickNew);
         }
 
         public void onNotice(String target, IRCUser u, String msg) {
             print(target + "> " + u.getNick() + " (notice): " + msg);
         }
 
         public void onPart(String chan, IRCUser u, String msg) {
             print(chan + "> " + u.getNick() + " parts");
         }
 
         public void onPrivmsg(String chan, IRCUser u, String msg) {
             if (startsWith(chan, "#")) {
                 print(u.getNick() + "> " + ": " + msg);
             } else {
                 print("PRV MSG from " + u.getNick() + "> " + msg);
                 if (msg.equals("hej")) {
                     conn.doPrivmsg(u.getNick(), "no jaha");
                 }
             }
         }
 
         public void onQuit(IRCUser u, String msg) {
             print("Quit: " + u.getNick());
         }
 
         public void onReply(int num, String value, String msg) {
             print("Reply #" + num + ": " + value + " " + msg);
         }
 
         public void onTopic(String chan, IRCUser u, String topic) {
             print(chan + "> " + u.getNick() + " changes topic into: " + topic);
         }
 
         public void onPing(String p) {
 
         }
 
         public void unknown(String a, String b, String c, String d) {
             print("UNKNOWN: " + a + " b " + c + " " + d);
         }
     }
 
     // appending main text area with irc messages
     public static void print(String str) {
         gui.appendTextArea(str);
     }
 
     public static IRCConnection getConnection() {
         return conn;
     }
 
     public static String getTarget() {
         return target;
     }
 
 }
 
