 package org.freecode.irc.votebot;
 
 import org.freecode.irc.CtcpRequest;
 import org.freecode.irc.CtcpResponse;
 import org.freecode.irc.IrcConnection;
 import org.freecode.irc.Privmsg;
 import org.freecode.irc.event.CtcpRequestListener;
 import org.freecode.irc.event.JoinListener;
 import org.freecode.irc.event.NumericListener;
 import org.freecode.irc.event.PrivateMessageListener;
 import org.freecode.irc.votebot.api.AdminModule;
 import org.freecode.irc.votebot.api.FVBModule;
 import org.freecode.irc.votebot.dao.PollDAO;
 import org.freecode.irc.votebot.dao.VoteDAO;
 import org.freecode.irc.votebot.entity.Poll;
 import org.freecode.irc.votebot.entity.Vote;
 import org.freecode.irc.votebot.modules.admin.LoadModules;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 
 /**
  * User: Shivam
  * Date: 17/06/13
  * Time: 00:05
  */
 public class FreeVoteBot implements PrivateMessageListener, JoinListener {
     public static final String CHANNEL_SOURCE = "#freecode";
 
     private String[] channels;
     private String nick, realName, serverHost, user;
     private int port;
     private ScriptModuleLoader sml;
     private IrcConnection connection;
     private String version;
 
     private ExpiryQueue<String> expiryQueue = new ExpiryQueue<>(1500L);
     private LinkedList<FVBModule> moduleList = new LinkedList<>();
     private PollDAO pollDAO;
     private VoteDAO voteDAO;
 
     public void init() {
         connectToIRCServer();
         NoticeFilter.setFilterQueue(connection, 5000L);
         addNickInUseListener();
         registerUser();
         addCTCPRequestListener();
         identifyToNickServ();
         joinChannels();
         sml = new ScriptModuleLoader(this);
         AdminModule mod = new LoadModules();
         mod.setFvb(this);
         moduleList.add(mod);
         try {
             for (Poll poll : pollDAO.getOpenPolls()) {
                 long expiry = poll.getExpiry();
                 int id = poll.getId();
                 PollExpiryAnnouncer announcer = new PollExpiryAnnouncer(expiry, id, this);
                 ScheduledFuture future = pollDAO.executor.scheduleAtFixedRate(announcer, 500L, 500L, TimeUnit.MILLISECONDS);
                 announcer.setFuture(future);
             }
         } catch (SQLException e) {
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
        NumericListener nickInUse = new NumericListener(connection) {
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
                             request.getNick(), "VERSION", "FreeVoteBot " + version + " by " + CHANNEL_SOURCE + " on irc.rizon.net"));
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
 
     public void setVersion(String version) {
         this.version = version;
     }
 
     public boolean addModule(final FVBModule module) {
         return moduleList.add(module);
     }
 
     public void addModules(final Collection<? extends FVBModule> module) {
         moduleList.addAll(module);
     }
 
     public boolean removeModule(final FVBModule module) {
         return moduleList.remove(module);
     }
 
     public void removeModules(final Collection<? extends FVBModule> module) {
         moduleList.removeAll(module);
     }
 
     public ScriptModuleLoader getScriptModuleLoader() {
         return sml;
     }
 
     public void sendMsg(String s) {
         for (String channel : channels) {
             connection.sendMessage(channel, s);
         }
     }
 
     public void setPollDAO(PollDAO pollDAO) {
         this.pollDAO = pollDAO;
     }
 
     public PollDAO getPollDAO() {
         return pollDAO;
     }
 
     public void setVoteDAO(VoteDAO voteDAO) {
         this.voteDAO = voteDAO;
     }
 
     public VoteDAO getVoteDAO() {
         return voteDAO;
     }
 
     private DateFormat getDateFormatter() {
         DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.UK);
         dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/London"));
         return dateFormat;
     }
 
     static class PollVotes implements Comparable<PollVotes> {
         int votes;
         String question;
 
         PollVotes(int votes, String question) {
             this.votes = votes;
             this.question = question;
         }
 
         public int compareTo(PollVotes o) {
             return votes - o.votes;
         }
     }
 
     @Override
     public void onJoin(String channel, String nick, String mask) {
         try {
             Poll[] openPolls = pollDAO.getOpenPolls();
             PollVotes[] pollVotes = new PollVotes[openPolls.length];
             for (int i = 0; i < openPolls.length; i++) {
                 Poll poll = openPolls[i];
                 String question = poll.getQuestion();
                 int id = poll.getId();
                 long expiry = poll.getExpiry();
                 Date date = new Date(expiry);
                 Vote[] votes = voteDAO.getVotesOnPoll(id);
                 String msg = String.format("Open poll #%d: \"%s\", ends: %s, votes: %d", id, question, getDateFormatter().format(date), votes.length);
                 pollVotes[i] = new PollVotes(votes.length, msg);
             }
             Arrays.sort(pollVotes);
             if (pollVotes.length > 3) {
                 connection.sendNotice(nick, "Trending polls list:");
                 connection.sendNotice(nick, pollVotes[0].question);
                 connection.sendNotice(nick, pollVotes[1].question);
                 connection.sendNotice(nick, pollVotes[2].question);
             } else if (pollVotes.length > 0) {
                 connection.sendNotice(nick, "Trending polls list:");
 
                 for (PollVotes p : pollVotes) {
                     connection.sendNotice(nick, p.question);
 
                 }
             } else {
                 connection.sendNotice(nick, "No current polls!");
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
 }
