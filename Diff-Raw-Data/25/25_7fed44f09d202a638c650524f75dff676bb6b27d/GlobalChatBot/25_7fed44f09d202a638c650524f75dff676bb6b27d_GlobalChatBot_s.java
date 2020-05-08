 package net.mcforge.globalchat;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.Socket;
 import java.net.URL;
 import java.util.List;
 import java.util.Locale;
 import java.util.Random;
 
 import net.mcforge.iomodel.Player;
 import net.mcforge.server.Server;
 import net.mcforge.util.WebUtils;
 
 
 public class GlobalChatBot implements Runnable {
     protected final IRCHandler handler;
     protected Server s;
 
     protected Socket socket;
     private BufferedReader reader;
     private BufferedWriter writer;
 
     protected final String REALNAME = "MCForge GC Bot";
     protected final String CONSTNAME = "MCFServer";
     protected String username;	
     protected String consoleNick = "Console";
 
     private String server = "irc.mcforge.net";
     private String channel = "#GlobalChat";
     private String quitMessage = "Server shutting down...";
     protected final int port = 6667;
 
     protected volatile boolean isRunning;
     protected volatile boolean connected;
     private Thread botThread;
 
     protected final static String outgoing = "&6<[Global]";
     protected final static String incoming = "&6>[Global]";
 
 
     public GlobalChatBot(Server server, String username, String quitMessage) {
         this.username = username;
         this.quitMessage = quitMessage;
         s = server;
         handler = new IRCHandler(this);
         URL url;
         String gcData;
         try {
             url = new URL("http://server.mcforge.net/gcdata");
             gcData = WebUtils.readContentsToArray(url)[0];
         }
         catch (IOException e) {
             e.printStackTrace();
             return;
         }
         String[] info = gcData.split("&");
         this.server = info[0];
         channel = info[1];
     }
 
     public void startBot() throws IOException {
         socket = new Socket(server, port);
 
         if (username.startsWith("ForgeBot")) {
             s.Log("You're currently using the default Global Chat bot nickname!");
             s.Log("Consider changing your bot's nickname in the server properties!");
         }
 
         botThread = new Thread(this);
         botThread.start();
     }
     @Override
     public void run() {	
        s.Log("Runningthisthread");

         String line;
         try  {
             isRunning = true;
             reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
 
             handler.setNick(username);
             handler.sendUserMsg(true);
 
             while (isRunning) {
                 line = reader.readLine();
                 if (line == null) {
                     continue;
                 }
                 if (handler.hasCode(line, "004")) {
                     handler.joinChannel(channel);
                     s.Log("Bot joined the Global Chat!");
                     break;
                 }
                 else if (handler.hasCode(line, "433")) {
                     s.Log("Nickname already in use! Randomizing..");
                     username = "ForgeBot" + new Random(System.currentTimeMillis()).nextInt(1000000000);
                     handler.setNick(username);
                     s.Log("New Global Chat nickname: " + username);
                 }
                 else if (line.startsWith("PING ")) {
                     handler.pong(line);
                 }
             }
             connected = true;
 
             while (isRunning) {
                 line = reader.readLine();
                 if (line == null) {
                     continue;
                 }
                 if (line.startsWith("PING ")) {
                     handler.pong(line);
                 }
                 else if (line.toLowerCase(Locale.ENGLISH).contains("privmsg " + channel.toLowerCase(Locale.ENGLISH)) && 
                         !handler.hasCode(line, "005")) {
                     String message = handler.getMessage(line);
                     if (message.startsWith("\u0001") && message.endsWith("\u0001")) {
                         continue;
                     }
                     if (message.startsWith("^")) {
                         if (message.startsWith("^UGCS"))
                             GCCPBanService.updateBanList();
                         else if (message.startsWith("^GETINFO ")) {
                             if (message.split(" ").length > 1 && message.split(" ")[1].equals(username)) {
                                 handler.sendMessage("^Name: " + s.Name);
                                 handler.sendMessage("^Description: " + s.description);
                                 handler.sendMessage("^MoTD: " + s.MOTD);
                                 handler.sendMessage("^Version: MCForge " + Server.CORE_VERSION);
                                 handler.sendMessage("^URL: http://minecraft.net/classic/play/" + s.hash);
                                 handler.sendMessage("^Players: " + s.getPlayers().size() + "/" + s.MaxPlayers);
                             }
                         }
                         else if (message.startsWith("^SENDRULES ")) {
                             Player who = s.findPlayer(message.split(" ")[1]);
                             if (who != null)
                                 s.getCommandHandler().execute(who, "gcrules", new String[0]);
                         }
                         else if (message.startsWith("^GETIP ") || message.startsWith("^IPGET ")) {
                             List<Player> players = s.getPlayers();
                             for (int i = 0; i < players.size(); i++) {
                                 Player p = players.get(i);
                                 if (p.username.equals(message.split(" ")[1])) {
                                     handler.sendMessage("^Username: " + p.username + " IP: " + p.getIP());
                                 }
                             }
                             players = null;
                         }
                         else if (message.startsWith("^ISONLINE")) {
                             List<Player> players = s.getPlayers();
                             for (int i = 0; i < players.size(); i++) {
                                 Player p = players.get(i);
                                 if (p.username.equals(message.split(" ")[1])) {
                                     handler.sendMessage("^" + p.username + " is online on " + s.Name);
                                 }
                             }
                             players = null;
                         }
                         else if (message.startsWith("^ISSERVER ")) {
                             if (message.split(" ").length > 1 && message.split(" ")[1].equals(username))
                                 handler.sendMessage("^IMASERVER");
                         }
                         continue;
                     }
                     String toSend = incoming + handler.getSender(line) + ": &f" +  message;
                     handler.messagePlayers(toSend);
                 }
                 else if (line.split(":")[1].contains("PRIVMSG " + username)) {
                     if (handler.getMessage(line).equals("\u0001" + "VERSION" + "\u0001")) {
                         handler.sendNotice(handler.getSender(line), "\u0001" + "VERSION MCForge " + Server.CORE_VERSION + " : " + System.getProperty("os.name") + "\u0001");
                     }
                 }
                 else if (handler.hasCode(line, "474")) {
                     String providedReason = handler.getMessage(line);
                     String banReason = providedReason.equals("Cannot join channel (+b)") ? "You're banned" : providedReason;
                     s.Log("You're banned from the Global Chat! Reason: " + banReason);
                     disposeBot();
                     return;
                 }
             }
            System.out.println("OUTOFYERLOOP YAAAY!");
         }
         catch(Exception ex) {
         }
     }
     public String getChannel() {
         return channel;
     }
 
     public BufferedReader getReader() {
         return reader;
     }
     public BufferedWriter getWriter() {
         return writer;
     }
 
     public void disposeBot() {
         handler.sendPart(quitMessage);
         handler.sendQuit(quitMessage);
 
         try {
             if (reader != null)
                 reader.close();
             if (writer != null)
                 writer.close();
 
             if (socket != null)
                 socket.close();
         }
         catch (IOException e) {
             e.printStackTrace();
         }
         isRunning = false;
         connected = false;
     }
 }
