 package polly.core;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.apache.log4j.Logger;
 import org.jibble.pircbot.IrcException;
 import org.jibble.pircbot.NickAlreadyInUseException;
 import org.jibble.pircbot.PircBot;
 import org.jibble.pircbot.User;
 
 import de.skuzzle.polly.sdk.AbstractDisposable;
 import de.skuzzle.polly.sdk.Disposable;
 import de.skuzzle.polly.sdk.IrcManager;
 import de.skuzzle.polly.sdk.eventlistener.ChannelEvent;
 import de.skuzzle.polly.sdk.eventlistener.ChannelModeEvent;
 import de.skuzzle.polly.sdk.eventlistener.ChannelModeListener;
 import de.skuzzle.polly.sdk.eventlistener.IrcUser;
 import de.skuzzle.polly.sdk.eventlistener.JoinPartListener;
 import de.skuzzle.polly.sdk.eventlistener.MessageEvent;
 import de.skuzzle.polly.sdk.eventlistener.MessageListener;
 import de.skuzzle.polly.sdk.eventlistener.NickChangeEvent;
 import de.skuzzle.polly.sdk.eventlistener.NickChangeListener;
 import de.skuzzle.polly.sdk.eventlistener.QuitEvent;
 import de.skuzzle.polly.sdk.eventlistener.QuitListener;
 import de.skuzzle.polly.sdk.exceptions.DisposingException;
 
 import polly.PollyConfiguration;
 import polly.events.EventProvider;
 import polly.telnet.TelnetConnection;
 import polly.util.WrapIterator;
 
 
 /**
  * 
  * @author Simon
  * @version 27.07.2011 ae73250
  */
 public class IrcManagerImpl extends AbstractDisposable implements IrcManager, Disposable {
     
     private static Logger logger = Logger.getLogger(IrcManagerImpl.class.getName());
     
     private PircBot bot = new PircBot() {
         @Override
         protected void onConnect() {
             
         };
         
         
         
         protected void onDisconnect() {
             IrcManagerImpl.this.retry();
         };
 
         
         
         @Override
         protected void onNickChange(String oldNick, String login, String hostname,
                 String newNick) {
             String oldIrcName = IrcManagerImpl.this.stripNickname(oldNick);
             String newIrcName = IrcManagerImpl.this.stripNickname(newNick);
             
             IrcUser oldUser = new IrcUser(oldIrcName, login, hostname);
             IrcUser newUser = new IrcUser(newIrcName, login, hostname);
             NickChangeEvent e = new NickChangeEvent(
                     IrcManagerImpl.this, oldUser, newUser);
             
             IrcManagerImpl.this.onlineUsers.remove(oldIrcName);
             IrcManagerImpl.this.onlineUsers.add(newIrcName);
             IrcManagerImpl.this.fireNickChange(e);
         }
         
         
         
         @Override
         protected void onJoin(String channel, String sender, String login,
                 String hostname) {
 
             String nickName = IrcManagerImpl.this.stripNickname(sender);
             IrcUser user = new IrcUser(nickName, login, hostname);
             ChannelEvent e = new ChannelEvent(IrcManagerImpl.this, user, channel);
             
             IrcManagerImpl.this.onlineUsers.add(nickName);
             IrcManagerImpl.this.fireJoin(e);
         }
         
         
         
         @Override
         protected synchronized void onPart(String channel, String sender, String login,
                 String hostname) {
             
             String nickName = IrcManagerImpl.this.stripNickname(sender);
             IrcUser user = new IrcUser(nickName, login, hostname);
             ChannelEvent e = new ChannelEvent(IrcManagerImpl.this, user, channel);
             
             /* ISSUE: 0000002 && 0000026*/
             boolean known = false;
             for (String c : this.getChannels()) {
                 for (User u : this.getUsers(c)) {
                     String uStripped = IrcManagerImpl.this.stripNickname(u.getNick());
                     
                     if (uStripped.equals(nickName)) {
                         known = true;
                         break;
                     }
                 }
             }
             if (!known) {
                 IrcManagerImpl.this.onlineUsers.remove(sender);
             }
             
             IrcManagerImpl.this.topics.remove(channel);
             IrcManagerImpl.this.firePart(e);
         }
         
         
         
         @Override
         protected void onQuit(String sourceNick, String sourceLogin,
                 String sourceHostname, String reason) {
             
             String nickName = IrcManagerImpl.this.stripNickname(sourceNick);
             IrcUser user = new IrcUser(nickName, sourceLogin, sourceHostname);
             QuitEvent e = new QuitEvent(IrcManagerImpl.this, user, reason);
             
             IrcManagerImpl.this.onlineUsers.remove(nickName);
             IrcManagerImpl.this.fireQuit(e);
         }
         
         
         
         @Override
         protected void onMessage(String channel, String sender, String login,
                 String hostname, String message) {
             
             String nickName = IrcManagerImpl.this.stripNickname(sender);
             IrcUser user = new IrcUser(nickName, login, hostname);
             MessageEvent e = new MessageEvent(IrcManagerImpl.this, user, 
                     channel, message);
             
             IrcManagerImpl.this.firePublicMessageEvent(e);
         }
         
         
         
         @Override
         protected void onPrivateMessage(String sender, String login, String hostname,
                 String message) {
             
             String nickName = IrcManagerImpl.this.stripNickname(sender);
             IrcUser user = new IrcUser(nickName, login, hostname);
             MessageEvent e = new MessageEvent(IrcManagerImpl.this, user, 
                     nickName, message);
             
             IrcManagerImpl.this.firePrivateMessageEvent(e);
         }
         
         
         
         @Override
         protected void onAction(String sender, String login, 
                 String hostname, String target, String action) {
             
             String nickName = IrcManagerImpl.this.stripNickname(sender);
             IrcUser user = new IrcUser(nickName, login, hostname);
             // TODO: target
             MessageEvent e = new MessageEvent(IrcManagerImpl.this, user,
                     nickName, action);
             
             IrcManagerImpl.this.fireActionMessageEvent(e);
         }
         
         
         
         @Override
         protected void onMode(String channel, String sourceNick, String sourceLogin, 
                 String sourceHostname, String mode) {
             
             String nickName = IrcManagerImpl.this.stripNickname(sourceNick);
             IrcUser user = new IrcUser(nickName, sourceLogin, sourceHostname);
             ChannelModeEvent e = new ChannelModeEvent(IrcManagerImpl.this, user, 
                 channel, mode);
             
             IrcManagerImpl.this.fireChannelModeEvent(e);
         };
         
         
         
         protected void onUserList(String channel, org.jibble.pircbot.User[] users) {
             for (int i = 0; i < users.length; ++i) {
                 String nickName = IrcManagerImpl.this.stripNickname(users[i].getNick());
                 IrcManagerImpl.this.onlineUsers.add(nickName);
             }
         }
         
         
         
         protected void onChannelInfo(String channel, int userCount, String topic) {
             IrcManagerImpl.this.topics.put(channel, topic);
         }
     };
     
     
     private EventProvider eventProvider;
     private Set<String> onlineUsers;
     private Map<String, String> topics;
     private PollyConfiguration config;
     private boolean disconnect;
     private TelnetConnection connection;
     private BotConnectionSettings recent;
     
     
     
     public IrcManagerImpl(String ircName, EventProvider eventProvider, 
             PollyConfiguration config) {
         this.config = config;
         this.onlineUsers = new TreeSet<String>();
         this.topics = new HashMap<String, String>();
         this.eventProvider = eventProvider;
         this.bot.changeNick(ircName);
         try {
             this.bot.setEncoding(config.getEncodingName());
         } catch (UnsupportedEncodingException e) {
             logger.error("Encoding exception", e);
         }
         this.bot.setMessageDelay(0);
         
         /* 
          * IRC Verbose output
          */
         this.bot.setVerbose(false);
     }
     
     
     
     private String stripNickname(String nickName) {
         char c = nickName.toCharArray()[0];
         switch (c) {
         case '*':
         case '!':
         case ':':
         case '%':
             nickName = nickName.substring(1);
         }
         return nickName;
     }
     
     
     
     public void setTelnetConnection(TelnetConnection connection) {
         this.connection = connection;
     }
     
     
 
 	public void connect(BotConnectionSettings e) 
             throws NickAlreadyInUseException, IOException, IrcException {
 
 	    if (this.isConnected()) {
 	        return;
 	    }
         this.bot.connect(e.getHostName(), e.getPort());
         
         this.bot.sendMessage("nickserv", "ghost " + e.getNickName() + " " + e.getIdentity());
         this.bot.changeNick(e.getNickName());
         this.bot.identify(e.getIdentity());
         this.joinChannels(e.getChannels());
         this.recent = e;
     }
 	
 	
 	
 	public String getNickName() {
 	    return this.bot.getNick();
 	}
     
     
     
 
     @Override
 	public void quit(String message) {
         if (this.isConnected()) {
             this.disconnect = true;
             this.bot.quitServer(message);
         }
     }
     
     
 
     @Override
 	public void quit() {
     	this.quit("");
     }
     
     
 
     @Override
 	public boolean isOnline(String nickName) {
         return this.onlineUsers.contains(nickName);
     }
     
     
     
     @Override
     public List<String> getChannelUser(String channel) {
        List<String> result = new LinkedList<String>();
         User[] users = this.bot.getUsers(channel);
         for (User user : users) {
             result.add(this.stripNickname(user.getNick()));
         }
        return result;
     }
     
     
 
     @Override
 	public Set<String> getOnlineUsers() {
         return this.onlineUsers;
     }
     
     
 
     @Override
 	public void disconnect() {
         this.disconnect = true;
         this.bot.disconnect();
     }
     
     
     
     @Override
 	public boolean isConnected() {
         return this.bot.isConnected();
     }
     
     
     
     @Override
 	public List<String> getChannels() {
         return Arrays.asList(this.bot.getChannels());
     }
     
     
     
     @Override
 	public boolean isOnChannel(String channel, String nickName) {       
         User[] users = this.bot.getUsers(channel);
         for (User user : users) {
             if (this.stripNickname(user.getNick()).equals(nickName)) {
                 return true;
             }
         }
         return false;
     }
     
     
     
     @Override
 	public void joinChannel(String channel, String password) {
         if (!channel.startsWith("#")) {
             throw new IllegalArgumentException("Channel must be preceded with '#'");
         }
         if (this.isConnected()) {
             this.bot.joinChannel(channel, password);
         }
     }
     
     
     
     @Override
     public void joinChannels(String...channels) {
         for (String channel : channels) {
             this.joinChannel(channel, "");
         }
     }
     
     
     
     @Override
 	public void partChannel(String channel, String message) {
         if (this.isConnected()) {
             this.bot.partChannel(channel, message);
         }
     }
     
     
     
     @Override
 	public void kick(String channel, String nickName, String reason) {
     	logger.info("Kicking user " + nickName + " from " + channel + 
     			". Reason: " + reason);
     	this.bot.kick(channel, nickName, reason);
     }
     
     
     
     @Override
 	public void op(String channel, String nickName) {
     	this.bot.op(channel, nickName);
     }
     
     
     
     @Override
 	public void deop(String channel, String nickName) {
     	this.bot.deOp(channel, nickName);
     }
    
     
     
     @Override
 	public synchronized void sendMessage(String channel, String message) {
         if (this.isConnected()) {
             for (String line : new WrapIterator(message, this.config.getLineLength())) {
                 this.bot.sendMessage(channel, line);
             }
             if (this.connection != null) {
                 this.connection.send(message);
             }
         }
     }
     
     
     
     @Override
 	public synchronized void sendAction(String channel, String message) {
         if (this.isConnected()) {
             for (String line : new WrapIterator(message, this.config.getLineLength())) {
                 this.bot.sendAction(channel, line);
             }
             if (this.connection != null) {
                 this.connection.send(message);
             }
         }
     }
     
     
     
     @Override
     public void setTopic(String channel, String topic) {
         if (this.isConnected()) {
             this.bot.setTopic(channel, topic);
         }
     }
     
     
     
     @Override
     public String getTopic(String channel) {
         if (this.isConnected()) {
             return this.topics.get(channel) == null ? "" : this.topics.get(channel);
         }
         return "";
     }
 
     
     
     public void retry() {
         if (this.disconnect) {
             return;
         }
         
         int attempts = 1;
         logger.warn("IRC Connection lost. Trying to reconnect...");
         while (!this.isConnected()) {
             try {
                 this.connect(this.recent);
                 this.disconnect = false;
                 logger.info("IRC Connection reestablished.");
             } catch (Exception e) {
                 logger.warn("Reconnect failed. Starting attempt " + ++attempts +"...", e);
                 try {
                     Thread.sleep(this.config.getReconnectDelay());
                 } catch (InterruptedException e1) {
                     logger.error("", e1);
                 }
             }
             
         }
     }
     
     
 
     @Override
 	public void addNickChangeListener(NickChangeListener listener) {
         this.eventProvider.addListener(NickChangeListener.class, listener);
     }
     
     
 
     @Override
 	public void removeNickChangeListener(NickChangeListener listener) {
         this.eventProvider.removeListener(NickChangeListener.class, listener);
     }
     
     
 
     @Override
 	public void addJoinPartListener(JoinPartListener listener) {
         this.eventProvider.addListener(JoinPartListener.class, listener);
     }
     
     
 
     @Override
 	public void removeJoinPartListener(JoinPartListener listener) {
         this.eventProvider.removeListener(JoinPartListener.class, listener);
     }
     
     
 
     @Override
 	public void addQuitListener(QuitListener listener) {
         this.eventProvider.addListener(QuitListener.class, listener);
     }
     
     
 
     @Override
 	public void removeQuitListener(QuitListener listener) {
         this.eventProvider.removeListener(QuitListener.class, listener);
     }
     
     
 
     @Override
 	public void addMessageListener(MessageListener listener) {
         this.eventProvider.addListener(MessageListener.class, listener);
     }
     
     
 
     @Override
 	public void removeMessageListener(MessageListener listener) {
         this.eventProvider.removeListener(MessageListener.class, listener);
     }
     
     
     
     @Override
     public void addChannelModeListener(ChannelModeListener listener) {
         this.eventProvider.addListener(ChannelModeListener.class, listener);
     }
     
     
     
     @Override
     public void removeChannelModeListener(ChannelModeListener listener) {
         this.eventProvider.removeListener(ChannelModeListener.class, listener);
     }
     
    
     
     protected void fireNickChange(final NickChangeEvent e) {
         final List<NickChangeListener> listeners = 
             this.eventProvider.getListeners(NickChangeListener.class);
     
         Runnable r = new Runnable() {
             @Override
             public void run() {
                 for (NickChangeListener listener : listeners) {
                     listener.nickChanged(e);
                 }
             }
         };
         this.eventProvider.dispatchEvent(r);
     }
     
     
     
     protected void fireJoin(final ChannelEvent e) {
         final List<JoinPartListener> listeners = 
             this.eventProvider.getListeners(JoinPartListener.class);
     
         Runnable r = new Runnable() {
             @Override
             public void run() {
                 for (JoinPartListener listener : listeners) {
                     listener.channelJoined(e);
                 }
             }
         };
         this.eventProvider.dispatchEvent(r);
     }
     
     
     
     protected void firePart(final ChannelEvent e) {
         final List<JoinPartListener> listeners = 
             this.eventProvider.getListeners(JoinPartListener.class);
     
         Runnable r = new Runnable() {
             @Override
             public void run() {
                 for (JoinPartListener listener : listeners) {
                     listener.channelParted(e);
                 }
             }
         };
         this.eventProvider.dispatchEvent(r);
     }
     
     
     
     protected void fireQuit(final QuitEvent e) {
         final List<QuitListener> listeners = 
             this.eventProvider.getListeners(QuitListener.class);
         
         Runnable r = new Runnable() {
             @Override
             public void run() {
                 for (QuitListener listener : listeners) {
                     listener.quited(e);
                 }
             }
         };
         this.eventProvider.dispatchEvent(r);
     }
     
     
     
     protected void firePublicMessageEvent(final MessageEvent e) {
         final List<MessageListener> listeners = 
             this.eventProvider.getListeners(MessageListener.class);
 
         Runnable r = new Runnable() {
             @Override
             public void run() {
                 for (MessageListener listener : listeners) {
                     listener.publicMessage(e);
                 }
             }
         };
         this.eventProvider.dispatchEvent(r);
     }
     
     
     
     protected void firePrivateMessageEvent(final MessageEvent e) {
         final List<MessageListener> listeners = 
             this.eventProvider.getListeners(MessageListener.class);
         
         Runnable r = new Runnable() {
             @Override
             public void run() {
                 for (MessageListener listener : listeners) {
                     listener.privateMessage(e);
                 }
             }
         };
         this.eventProvider.dispatchEvent(r);
     }
     
     
     
     protected void fireActionMessageEvent(final MessageEvent e) {
         final List<MessageListener> listeners = 
             this.eventProvider.getListeners(MessageListener.class);
         
         Runnable r = new Runnable() {
             @Override
             public void run() {
                 for (MessageListener listener : listeners) {
                     listener.actionMessage(e);
                 }
             }
         };
         this.eventProvider.dispatchEvent(r);
     }
     
     
     
     protected void fireChannelModeEvent(final ChannelModeEvent e) {
         final List<ChannelModeListener> listeners = 
             this.eventProvider.getListeners(ChannelModeListener.class);
         
         Runnable r = new Runnable() {
             @Override
             public void run() {
                 for (ChannelModeListener listener : listeners) {
                     listener.channelModeChanged(e);
                 }
             }
         };
         this.eventProvider.dispatchEvent(r);
     }
 
 
 
     @Override
     protected void actualDispose() throws DisposingException {
         if (this.isConnected()) {
             logger.debug("Closing irc connection using default quit message.");
             this.quit();
         } else {
             logger.debug("Irc connection already closed.");
         }
         logger.trace("Shutting down irc bot.");
         this.bot.dispose();
     }
 }
