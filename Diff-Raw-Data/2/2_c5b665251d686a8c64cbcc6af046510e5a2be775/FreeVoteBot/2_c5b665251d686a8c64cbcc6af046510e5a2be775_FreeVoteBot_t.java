 package org.freecode.irc.votebot;
 
 import org.freecode.irc.CtcpRequest;
 import org.freecode.irc.CtcpResponse;
 import org.freecode.irc.IrcConnection;
 import org.freecode.irc.Privmsg;
 import org.freecode.irc.event.CtcpRequestListener;
 import org.freecode.irc.event.NumericListener;
 import org.freecode.irc.event.PrivateMessageListener;
 import org.freecode.irc.votebot.api.FVBModule;
 import org.freecode.irc.votebot.dao.PollDAO;
 import org.freecode.irc.votebot.dao.VoteDAO;
 
 import javax.script.ScriptException;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.Locale;
 import java.util.TimeZone;
 
 /**
  * User: Shivam
  * Date: 17/06/13
  * Time: 00:05
  */
 public class FreeVoteBot implements PrivateMessageListener {
    public static final double VERSION = 1.076D;
     public static final String CHANNEL_SOURCE = "#freecode";
 
     private PollDAO pollDAO;
     private VoteDAO voteDAO;
     private String[] channels;
     private String nick, realName, serverHost, user;
     private int port;
     private ScriptModuleLoader sml;
     private IrcConnection connection;
 
     private ExpiryQueue<String> expiryQueue = new ExpiryQueue<>(1500L);
     private LinkedList<FVBModule> moduleList = new LinkedList<>();
     public static final SimpleDateFormat SDF;
 
 
     static {
         SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.UK);
         SDF.setTimeZone(TimeZone.getTimeZone("Europe/London"));
     }
 
     public void init() {
         connectToIRCServer();
         NoticeFilter.setFilterQueue(connection, 5000L);
         addNickInUseListener();
         registerUser();
         addCTCPRequestListener();
         identifyToNickServ();
         joinChannels();
         sml = new ScriptModuleLoader(this);
         try {
             moduleList.add(sml.loadFromFile(getClass().getResourceAsStream("/TestMod.py"), "TestMod.py"));
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     private void registerUser() {
         try {
             connection.register(nick, user, realName);
         } catch (IOException e) {
             e.printStackTrace();
         }
         connection.addListener(this);
     }
 
     private void addNickInUseListener() {
         NumericListener nickInUse = new NumericListener() {
             public int getNumeric() {
                 return IrcConnection.ERR_NICKNAMEINUSE;
             }
 
             public void execute(String rawLine) {
                 FreeVoteBot.this.nick = FreeVoteBot.this.nick + "_";
                 try {
                     connection.sendRaw("NICK " + FreeVoteBot.this.nick);
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         };
         connection.addListener(nickInUse);
     }
 
     private void addCTCPRequestListener() {
         connection.addListener(new CtcpRequestListener() {
             public void onCtcpRequest(CtcpRequest request) {
                 if (request.getCommand().equals("VERSION")) {
                     request.getIrcConnection().send(new CtcpResponse(request.getIrcConnection(),
                             request.getNick(), "VERSION", "FreeVoteBot " + VERSION + " by " + CHANNEL_SOURCE + " on irc.rizon.net"));
                 } else if (request.getCommand().equals("PING")) {
                     request.getIrcConnection().send(new CtcpResponse(request.getIrcConnection(),
                             request.getNick(), "PING", request.getArguments()));
                 }
             }
         });
     }
 
     private void connectToIRCServer() {
         try {
             connection = new IrcConnection(serverHost, port);
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     private void identifyToNickServ() {
         File pass = new File("password.txt");
         if (pass.exists()) {
             try {
                 BufferedReader read = new BufferedReader(new FileReader(pass));
                 String s = read.readLine();
                 if (s != null) {
                     connection.send(new Privmsg("NickServ", "identify " + s, connection));
                 }
                 read.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
 
     private void joinChannels() {
         for (String channel : channels) {
             connection.joinChannel(channel);
         }
     }
 
 
     public void onPrivmsg(final Privmsg privmsg) {
         if (privmsg.getNick().equalsIgnoreCase(nick)) {
             return;
         }
 
         String sender = privmsg.getNick().toLowerCase();
         if (expiryQueue.contains(sender) || !expiryQueue.insert(sender)) {
             return;
         }
 
         for (FVBModule module : moduleList) {
             try {
                 if (module.isEnabled() && module.canRun(privmsg)) {
                     module.process(privmsg);
                     return;
                 }
             } catch (Exception e) {
                 privmsg.send(e.getMessage());
             }
         }
 
     }
 
 
     public static void askChanServForUserCreds(Privmsg privmsg) {
         privmsg.getIrcConnection().send(new Privmsg("ChanServ", "WHY " + FreeVoteBot.CHANNEL_SOURCE + " " + privmsg.getNick(), privmsg.getIrcConnection()));
     }
 
     public void setPollDAO(PollDAO pollDAO) {
         this.pollDAO = pollDAO;
     }
 
     public void setVoteDAO(VoteDAO voteDAO) {
         this.voteDAO = voteDAO;
     }
 
     public void setNick(String nick) {
         this.nick = nick;
     }
 
     public void setRealName(String realName) {
         this.realName = realName;
     }
 
     public void setServerHost(String serverHost) {
         this.serverHost = serverHost;
     }
 
     public void setUser(String user) {
         this.user = user;
     }
 
     public void setPort(String port) {
         this.port = Integer.parseInt(port);
     }
 
     public void setChannels(String channels) {
         this.channels = channels.split(",");
     }
 
     public void setModules(final FVBModule[] modules) {
         moduleList.clear();
         moduleList.addAll(Arrays.asList(modules));
 
     }
 
     public PollDAO getPollDAO() {
         return pollDAO;
     }
 
     public VoteDAO getVoteDAO() {
         return voteDAO;
     }
 }
