 /*
  Copyright (c) 2012 Serge Humphrey<bobtheblueberry@gmail.com>
 
 Permission is hereby granted, free of charge, to any person obtaining a copy of this software
  and associated documentation files (the "Software"), to deal in the Software without restriction,
  including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
  and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
  subject to the following conditions:
 
 The above copyright notice and this permission notice shall be included in all copies or
  substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
  FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 package com.btbb.irc;
 
 import java.io.*;
 import java.net.*;
 import java.util.*;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 import java.util.regex.Matcher;
 
 /**
  * IRC Bot
  * 
  * 
  * @author Serge Humphrey
  * 
  */
 public class IRCBot {
 
     /**
      * @param args
      */
 
     static String server;
     final String NICK;
     static String password;
 
     static String nick;
 
     // static String mcBot = "BeeblBot";
 
     static ArrayList<String> foods;
     static ArrayList<String> jokes;
     static ArrayList<String> insults;
     static ArrayList<String> twain;
 
     static String markov;
     static File logDir;
 
     static int port;
     static IRC JavaBot;
 
     static boolean joined = false;
     static boolean invited = false;
 
     static boolean isMSG = false;
     static boolean isPM = false;
     static String message = null;
     static String sender = null;
 
     long ping = 0;
 
     private Socket irc = null;
     private BufferedWriter bw = null;
     private BufferedReader br = null;
 
     static IRCBot bot;
 
     final String MANDATORY_OWNER;
     String person;         // person.. or channel
     String lastChannel;
     String pmChannel;
     ArrayList<String> channels;
 
     ArrayList<String> choices;
     int choiceType = -1;
     final int MOCK = 0;
     final int QUOTE = 1;
     final int SEARCH = 2;
     String searchQuery;
     boolean waitforModed;
 
     /**
      * Set just before joining the new channel
      */
     String newchannel;
 
     /** Minecraft valid name */
     public static boolean validName(String name) {
         return name.length() > 1 && name.length() < 17 && !name.matches("(?i).*[^a-z0-9_].*");
     }
 
     public IRCBot(HashMap<String, String> settings)
         {
             bot = this;
             logDir = new File(settings.get("logs"));
             logDir.mkdirs();
             server = settings.get("server");
             String c = settings.get("channel");
             channels = new ArrayList<String>();
             if (c.contains(","))
                 for (String s : c.split(","))
                     channels.add(s);
             else
                 channels.add(c);
             nick = NICK = settings.get("nick");
             password = settings.get("password");
             port = Integer.parseInt(settings.get("port"));
             markov = settings.get("markov");
             MANDATORY_OWNER = settings.get("owner");
             choices = new ArrayList<String>();
             String s = settings.get("modewait");
             waitforModed = (s == null) ? true : s.equals("1");
 
             System.out.println("Loading Foods");
             loadFile(foods = new ArrayList<String>(), "foods");
             System.out.println("Loading Jokes");
             loadFile(jokes = new ArrayList<String>(), "jokes");
             System.out.println("Loading Insults");
             loadFile(insults = new ArrayList<String>(), "insults");
             System.out.println("Loading Twain Quotes");
             loadFile(twain = new ArrayList<String>(), "twain");
 
             setupReconnect();
 
             while (true) {
                 run();
                 System.out.println("ERROR! Disconnected!");
                 // Reset variables
                 joined = false;
                 invited = false;
                 isMSG = false;
                 isPM = false;
                 message = null;
                 sender = null;
                 nick = NICK;
                 newchannel = null;
             }
         }
 
     private void run() {
 
         try {
             System.out.println("Starting IRC Bot");
             // our socket we're connected with
             irc = new Socket(server, port);
             // out output stream
             bw = new BufferedWriter(new OutputStreamWriter(irc.getOutputStream()));
             // our input stream
             br = new BufferedReader(new InputStreamReader(irc.getInputStream()));
 
             System.out.println("Joining Server");
             // create a new instance of the JavaBot
             JavaBot = new IRC(bw);
 
             System.out.println("Logging in");
 
             // authenticate the JavaBot with the server
             JavaBot.login(nick);
 
             String currLine = null;
             while ((currLine = br.readLine()) != null) {
                 try {
                     mainloop(currLine);
                 } catch (Exception exc) {
                     exc.printStackTrace();
                 }
             }
         } catch (UnknownHostException e) {
             System.err.println("No such host");
         } catch (IOException e) {
             System.err.println("There was an error connecting to the host");
             e.printStackTrace();
         }
     }
 
     private void mainloop(String currLine) throws IOException {
         boolean justghosted = false;
         boolean moded = !waitforModed;
         ping = System.currentTimeMillis();
         String arg;
         String[] args;
         Request request = new Request(currLine);
         sender = request.getSender();
         if (!moded && request.isMode()) {
             moded = true;
         }
 
         isMSG = request.isMSG();
         isPM = isMSG && request.isPM();
         lastChannel = request.getChannel();
         if (isPM) {
             person = sender;
         } else {
             person = lastChannel;
         }
 
         message = request.getMessage();
 
         if (request.isPing()) {
             JavaBot.pong(request.getPingMessage());
             System.out.println("Pong");
             return;
         }
         System.out.println(currLine);
 
         Matcher m = request.getKick();
         if (m.matches()) {
             JavaBot.join(m.group(4));
             return;
         }
         
         if (request.isInvite()) {
             // INVITE
             // newchannel = request.getInviteMessage();
             // invited = true;
             String chan;
             JavaBot.join( chan = request.getInviteMessage());
             channels.add(chan);
         }
 
         // Check for nickname is already in use
         if (!joined) {
             if (!justghosted && request.isError("433")) {
                 System.out.println("Ghosting nick...");
                 // Login again
                 int n = (int) (Math.random() * 20);
                 nick = nick + n;
                 JavaBot.nick(nick);
                 // Ghost nick
                 JavaBot.ghost(NICK, password);
                 justghosted = true;
                 return;
             }
         }
 
         if (justghosted && !nick.equals(NICK) && request.isGhostedMessage()) {
             System.out.println("Changing nickname");
             JavaBot.nick(NICK);
             nick = NICK;
             justghosted = false;
             return;
         }
         // JOIN CHANNEL
         if (invited || (!joined && moded)) {
 
             if (!invited) {
                 // identify with nick serv
                 System.out.println("Identifying with NickServ");
                 JavaBot.identify(NICK, password);
             }
             for (String chan : channels) {
                 if (joined) {
                     JavaBot.say("Joining " + chan);
                 }
                 System.out.println("Entering Channel " + chan);
                 JavaBot.join(chan);
                 joined = true;
                 invited = false;
             }
             return;
         }
 
         // LOG
         if (!request.isCommand()) {
             // TODO : NOTE : Changed "." for command to "!"
             if (isMSG && !isPM) {
                 if (message != null && message.length() > 0 && !request.getSender().equals(nick)
                         && !message.startsWith("!") && !message.startsWith("@")) {
                     // Add phrase to the log files
                     File log = new File(logDir, sender);
                     BufferedWriter out = new BufferedWriter(new FileWriter(log, true));
                     out.write(message.replaceAll("\n", "") + "\n");
                     out.close();
                 }
             } else if (isPM) {
                 if (request.hasOwnership()) { // Talking through bot
                     IRCBot.isPM = false;
                     if (message.toLowerCase().startsWith("chan=")) {
                         pmChannel = message.substring(5);
                         if (!pmChannel.startsWith("#"))
                             pmChannel = "#" + pmChannel;
                     }
                     String chan = (pmChannel != null) ? pmChannel : lastChannel;
                     JavaBot.say(message, chan);
                 } else {
                     JavaBot.say(message, sender);
                 }
             }
 
             return;
         }
         int reqisNum = request.isNumber();
         if (reqisNum > 0) {
             if (reqisNum > choices.size()) {
                 if (reqisNum == 69) {
                     String s = "no";
                     switch ((int) (Math.random() * 5)) {
                         case 0:
                             s = "Yes";
                             break;
                         case 1:
                             s = "Okay";
                             break;
                         case 2:
                             s = "sure";
                             break;
                         case 3:
                             s = "anytime";
                             break;
                         case 4:
                             s = "sure why not?";
                             break;
                     }
                     JavaBot.say(s);
                 } else
                     JavaBot.say("no");
                 return;
             }
             String s = choices.get(reqisNum - 1);
             if (choiceType == MOCK) {
                 mock(s);
             } else if (choiceType == QUOTE) {
                 quote(s);
             } else if (choiceType == SEARCH) {
                 search(new String[]
                     { s, searchQuery });
             }
             return;
         }
         if ((arg = request.matchArg("echo")) != null || (arg = request.matchArg("$echo")) != null) {
             JavaBot.say(arg);
             return;
         } else if (request.match("echo")) {
             JavaBot.say("Rats live on no evil star");
             return;
         }
         if (request.match("time")) {
             Date d = new Date();
             String msg = "The date is " + d;
             JavaBot.say(msg);
             return;
         } else if (request.match("help")) {
             // JavaBot.say("Commands: !help !time !define [arg] !greet (arg?) !taste [arg] "
             // +
             // "!mock [arg] !on !off !joke !cynic !quote [arg] !search [pl] [arg]");
             JavaBot.say("Commands: $help $time $define [arg] $greet (arg?) $taste [arg] "
                     + "$mock [arg] $joke $cynic $twain $quote [arg] $search [pl] [arg] $google [arg] $g [arg] $channels");
 
             return;
         } else if (request.match("joke")) {
             JavaBot.say(getRandom(jokes));
             return;
 
         } else if (request.match("cynic")) {
             JavaBot.say(getRandom(insults));
             return;
 
         } else if (request.match("twain")) {
             JavaBot.say(getRandom(twain));
             return;
 
         } else if (request.match("channels")) {
             String s = "";
             for (String c : channels)
                 s += c + " ";
 
             JavaBot.say(s);
             return;
 
         } else if ((arg = request.matchArg("taste")) != null) {
             taste(arg);
             return;
         } else if ((arg = request.matchArg("mock")) != null) {
             mock(arg);
             return;
         } else if (request.match("greet")) {
             JavaBot.say(getGreeting(sender));
             return;
         } else if ((arg = request.matchArg("g")) != null) {
             if (arg == null || arg.matches("\\s"))
                 JavaBot.say("http://tiny.cc/PAIN");
             else
                 JavaBot.say("http://www.google.com/search?q=" + arg.replaceAll(" ", "+"));
             return;
         } else if (request.match("g")) {
             JavaBot.say("http://tiny.cc/PAIN");
             return;
         } else if ((arg = request.matchArg("google")) != null) {
             if (arg == null || arg.matches("\\s"))
                 JavaBot.say("http://tiny.cc/PAIN");
             else
                 JavaBot.say("http://lmgtfy.com/?q=" + arg.replaceAll(" ", "%20"));
             return;
         } else if (request.match("google")) {
             JavaBot.say("http://tiny.cc/PAIN");
             return;
         } else if ((arg = request.matchArg("greet")) != null) {
 
             JavaBot.say(getGreeting(arg));
             return;
         } else if ((arg = request.matchArg("quote")) != null) {
             quote(arg);
             return;
         } else if ((arg = request.matchArg("define")) != null) {
             JavaBot.say(define(arg));
         } else if ((args = request.matchArgName1("search")) != null) {
             search(args);
             return;
         } else if ((arg = request.matchArg("help")) != null) {
             if (arg.split(" ").length > 1) {
                 int i = arg.indexOf(" ");
                 String msg = arg.substring(i + 1);
                 JavaBot.say(getIdiotMessage(msg));
                 return;
             } else {
                 JavaBot.say("Busy");
                 return;
             }
         } else if (request.match("colors")) {
             sayColors();
             return;
         }
 
         if (request.hasOwnership()) {
             // check to see if the owner has given the !exit command
             if ((arg = request.matchArg("part")) != null) {
                 if (!arg.startsWith("#")) {
                     arg = "#" + arg;
                 }
                 arg = arg.toLowerCase();
                 boolean found = false;
                 for (String s : channels)
                     if (s.equalsIgnoreCase(arg)) {
                         found = true;
                         break;
                     }
                 channels.remove(arg);
                 if (!found) {
                     JavaBot.say("I'm not there!");
                     return;
                 }
                 JavaBot.say("Outta this bitch", arg);
                 JavaBot.part(arg);
                 return;
             } else if (request.match("part")) {
                 JavaBot.say("Bye");
                 JavaBot.part(lastChannel);
                 channels.remove(lastChannel);
                 return;
             }
 
             if ((arg = request.matchArg("join")) != null) {
                 if (!arg.startsWith("#"))
                     arg = "#" + arg;
                 arg = arg.toLowerCase();
                 JavaBot.say("I'm going to go over to " + arg + " and see what they're up to over there.");
                 JavaBot.join(arg);
                 channels.add(arg);
                 JavaBot.say("Hey guys!", arg);
                 return;
             }
             
             JavaBot.say("Unknown command " + request.getMessage());
 
         }
     }
 
     /**
      * Automatic reconnect
      * 
      */
     public void setupReconnect() {
         Timer timer = new Timer();
         timer.scheduleAtFixedRate(new TimerTask() {
             @Override
             public void run() {
                 // if bot hasn't contacted the server for 4 minutes
                 if (irc != null && (ping + 60 * 4 * 1000) < System.currentTimeMillis()) {
                     try {
                         System.out.println("Closing socket!");
                         // Hopefully this will cause an exception or something
                         irc.close();
                     } catch (IOException e) {
                         System.out.println("Error killing bot connection!");
                         e.printStackTrace();
                     }
                 }
             }
         }, 30 * 1000, 60 * 1000);
     }
 
     public static String getRandom(ArrayList<String> list) {
         int i = (int) (Math.random() * list.size());
         return list.get(i);
     }
 
     public static void loadFile(ArrayList<String> list, String name) {
         try {
             URL url = IRCBot.class.getClassLoader().getResource(name);
             BufferedReader in;
             if (url == null) {
                 in = new BufferedReader(new FileReader(name));
             } else {
                 URLConnection connection = url.openConnection();
                 in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
             }
             String line;
             while ((line = in.readLine()) != null) {
                 if (line.length() > 0)
                     list.add(line);
             }
             in.close();
         } catch (FileNotFoundException e) {
             System.out.println("Can't load " + name);
             e.printStackTrace();
         } catch (IOException e) {
             System.out.println("Can not load " + name);
             e.printStackTrace();
         }
     }
 
     private String getDidYouMean(File[] files) {
         String msg = "Do you mean [";
         choices.clear();
         for (int i = 0; i < files.length; i++) {
             choices.add(files[i].getName());
             msg = msg + files[i].getName() + ((i < files.length - 1) ? ", " : "");
         }
         return msg + "] ?";
     }
 
     private static String getIdiotMessage(String baseMsg) {
         int i = (int) (Math.random() * 8);
         switch (i) {
             case 0:
                 return "Why Should I " + baseMsg + "?";
             case 1:
                 return "Go " + baseMsg + " by yourself!";
             case 2:
                 return "Time will " + baseMsg;
             case 3:
                 return "It's too much work to " + baseMsg;
             case 4:
                 return "What will happen when you " + baseMsg + "?";
             case 5:
                 return "Didn't I just hear you say you wanted to " + baseMsg + "?";
             case 6:
                 return "How come you didn't want to " + baseMsg + " yesterday?";
             case 7:
                 return "I found a new idea, it's to go and " + baseMsg + "!";
         }
         return "You are a moron, because you want to " + baseMsg;
     }
 
     private static String getGreeting(String name) {
         int i = (int) (Math.random() * 12);
         switch (i) {
             case 0:
                 return "Sup? " + name;
             case 1:
                 return "Get a life, " + name;
             case 2:
                 return "What's goin onnn?";
             case 3:
                 return "hallo " + name;
             case 4:
                 return "Merry Christmas " + name;
             case 5:
                 return "Hi I'm ralph. What's a, " + name;
             case 6:
                 return "hi, " + name;
             case 7:
                 return "Beware of the commies, " + name;
             case 8:
                 return "Who were you " + name + "?";
             case 9:
                 return "Go outside " + name;
             case 10:
                 return "You are a moron, " + name;
             case 11:
                 ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
                 scheduler.schedule(new Messanger("wait"), 1, TimeUnit.SECONDS);
                 scheduler.schedule(new Messanger("nevermind"), 3, TimeUnit.SECONDS);
                 return "Wait";
 
         }
         return "NUUU";
     }
 
     public static File[] searchForPlayer(String s) {
         File file = new File(logDir, s);
         if (file.exists()) {
             return new File[]
                 { file };
         }
         File[] files = logDir.listFiles();
         ArrayList<File> fl = new ArrayList<File>();
         for (File f : files) {
             if (f.getName().equalsIgnoreCase(s))
                 fl.add(f);
             else if (f.getName().toLowerCase().contains(s.toLowerCase())) {
                 fl.add(f);
             }
         }
         if (fl.size() > 0)
             return fl.toArray(new File[fl.size()]);
         return new File[0];
     }
 
     private static String randomLine(String file) {
         try {
             ArrayList<String> lines = new ArrayList<String>();
             BufferedReader r = new BufferedReader(new FileReader(file));
             String line;
             while ((line = r.readLine()) != null)
                 lines.add(line);
             r.close();
             int index = (int) (Math.random() * lines.size());
             return lines.get(index);
         } catch (IOException e1) {
             e1.printStackTrace();
         }
         return null;
     }
 
     /**
      * Thanks Ism
      * 
      * @param phrase
      * @return
      */
     public static String define(String phrase) {
         String uphrase = phrase;
         try {
             uphrase = URLEncoder.encode(uphrase, "UTF-8");
         } catch (UnsupportedEncodingException e) {
         }
         // String url = "http://api.wordnik.com/api/word.xml/" + phrase +
         // "/definitions?api_key=" + key;
         String url = "http://www.merriam-webster.com/dictionary/" + uphrase;
         try {
             HttpURLConnection conn = (HttpURLConnection) (new URL(url).openConnection());
 
             // Set up a request.
             conn.setConnectTimeout(5000); // 10 sec
             conn.setReadTimeout(5000); // 10 sec
             conn.setInstanceFollowRedirects(true);
             conn.setRequestProperty("User-agent", "spider");
 
             // Send the request.
             conn.connect();
             InputStream is = conn.getInputStream();
             Scanner sc = new Scanner(is);
             sc.useDelimiter("<strong>:</strong>");
             sc.next();
             sc.useDelimiter("<(?:br/|/p|/div)>");
             String def = sc.next();
             sc.close();
             // IrcBot.toLog(def);
             def = def.replaceAll("<strong>:</strong>", ":").replaceAll("\n", " ");
             def = def.replaceAll("<.*?>", "--").replaceAll("\\&.*?;", "").trim();
             return phrase + def;
         } catch (NoSuchElementException e) {
             return "I don't have a definition for " + phrase;
         } catch (SocketTimeoutException e) {
             return "That definition is taking too long.";
         } catch (IOException e) {
             e.printStackTrace();
             return "I've encountered an internet error.";
         }
     }
 
     private static class Messanger implements Runnable {
 
         String msg;
 
         public Messanger(String msg)
             {
                 this.msg = msg;
             }
 
         @Override
         public void run() {
             try {
                 JavaBot.say(msg);
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
 
     }
 
     private static void sayColors() {
         char c = 3;
         String s = "Colors ";
         for (int i = 0; i < 15; i++) {
             s = s + c + i + "  color " + i;
         }
         try {
             JavaBot.say(s);
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     private void search(String[] args) {
         try {
             String key = args[1];
             searchQuery = key;
             /*      if (!key.matches("\\w+")) {
                       JavaBot.say("Bad keyword!");
                       return;
                   }*/
             File f = new File(logDir, args[0]);
             if (!f.exists()) {
                 if (!validName(args[0])) {
                     JavaBot.say("Bad name!");
                     return;
                 } else {
                     File[] files = searchForPlayer(args[0]);
                     if (files.length > 1) {
                         choiceType = SEARCH;
                         JavaBot.say(getDidYouMean(files));
                         return;
                     } else if (files.length > 0) {
                         f = files[0];
                     }
                 }
             }
             if (f.exists()) {
                 ArrayList<String> lines = new ArrayList<String>();
                 BufferedReader r = new BufferedReader(new FileReader(f));
                 String line;
                 key = key.toLowerCase();
                 while ((line = r.readLine()) != null)
                     if (line.toLowerCase().contains(key))
                         lines.add(line);
                 r.close();
                 String s = "";
                 char c = 3;
                 int x = (int) (Math.random() * 3) + 3;
 
                 if (lines.size() < 1) {
                     JavaBot.say(c + "4No results!");
                     return;
                 }
                 for (int i = 0; i < Math.min(x, lines.size()); i++) {
                     int n;
                     if (lines.size() <= x) {
                         n = i;
                     } else {
                         n = (int) (Math.random() * lines.size());
                     }
                     line = lines.get(n);
                     String space = "";
                     if (i > 0) {
                         space = " ";
                     }
                     // s = s + space + c + "9\"" + c + "14" + line + c +
                     // "9\"";
                     s = s + space + "\"" + line + "\"";
                 }
                 // JavaBot.say(c + "13" + f.getName() + c + "1 said " +
                 // s);
                 JavaBot.say("(" + lines.size() + ") " + c + "6" + f.getName() + c + "1 said " + c + "2" + s);
             } else {
                 JavaBot.say("I haven't heard of " + args[0]);
             }
         } catch (Exception exc) {
             System.err.println("Search function failed");
             exc.printStackTrace();
         }
     }
 
     private void mock(String arg) throws IOException {
         if (!validName(arg) && !(new File(logDir, arg).exists())) {
             JavaBot.say("Invalid name");
             return;
         } else {
 
             File[] files = searchForPlayer(arg);
 
             if (files.length < 1) {
                 JavaBot.say("I haven't heard of " + arg);
                 return;
             } else if (files.length > 1) {
                 choiceType = MOCK;
                 JavaBot.say(getDidYouMean(files));
                 return;
             } else {
                 File f = files[0];
 
                 ProcessBuilder pb = new ProcessBuilder("bash", "-c", markov + " -d 1 -n 20 < \"" + f.getAbsolutePath()
                         + "\" | tr '\n' ' ' | tr -d '\r' > .out");
                 Process p = pb.start();
                 try {
                     p.waitFor();
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
                 BufferedReader in = new BufferedReader(new FileReader(".out"));
                 String phrase = in.readLine();
                 in.close();
                 char c = 3;
                 if (phrase == null) {
                     JavaBot.say("huh?");
                 } else if (phrase.length() > 2) {
                     JavaBot.say(c + "2\"" + phrase.substring(0, phrase.length() - 2) + "\"" + c + "6 -- " + f.getName());
                     return;
                 } else {
                     JavaBot.say("I don't know " + f.getName() + " well enough.");
                     return;
                 }
             }
         }
     }
 
     private void quote(String arg) throws IOException {
         File f = new File(logDir, arg);
         if (!f.exists()) {
             if (!validName(arg)) {
                 JavaBot.say("You scum!");
                 return;
             } else {
                 File[] files = searchForPlayer(arg);
                 if (files.length > 1) {
                     choiceType = QUOTE;
                     JavaBot.say(getDidYouMean(files));
                     return;
                 } else if (files.length > 0) {
                     f = files[0];
                 }
             }
         }
         if (f.exists()) {
             String quote = "";
             // Try to get a good quote
             for (int i = 0; i < 10; i++) {
                 quote = randomLine(f.getAbsolutePath());
                 // This is the check to see if it's a good one
                 if (quote != null && quote.contains(" ")) {
                     break;
                 }
             }
             char c = 3;
             JavaBot.say(c + "3\"" + quote + "\"" + c + "10 -- " + f.getName());
             return;
         } else {
             JavaBot.say("I haven't heard of " + arg);
             return;
         }
     }
 
     private void taste(String arg) throws IOException {
         if (Math.random() * 4 > 3) {
             String food1, food2;
             food1 = getRandom(foods);
             food2 = getRandom(foods);
             while (food1.equals(food2)) {
                 food1 = getRandom(foods);
             }
             JavaBot.say(arg + ", you taste like " + food1 + ", or " + food2);
             return;
         } else {
             JavaBot.say(arg + ", you taste like " + getRandom(foods));
             return;
         }
     }
 }
