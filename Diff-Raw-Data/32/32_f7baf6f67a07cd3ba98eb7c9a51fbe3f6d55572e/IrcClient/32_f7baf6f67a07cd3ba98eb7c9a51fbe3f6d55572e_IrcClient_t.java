 package uk.org.hackspace.ircensus;
 
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jibble.pircbot.PircBot;
 import org.jibble.pircbot.User;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class IrcClient extends PircBot {
 
     private static final Logger LOG = LoggerFactory.getLogger(IrcClient.class);
 
     private static final String COMMAND_PREFIX = "!";
 
     private final String nickname;
 
     private List<IrcHandler> handlers;
 
     private final Channel defaultChannel;
 
     private final String serverAddress;
 
     private final int serverPort;
 
     private final String serverPassword;
 
     private boolean disconnecting;
 
     IrcClient(String nickname, Channel defaultChannel, String serverAddress, int serverPort, String serverPassword)
             throws Exception {
         handlers = new ArrayList<IrcHandler>();
         this.nickname = nickname;
         this.defaultChannel = defaultChannel;
         this.serverAddress = serverAddress;
         this.serverPort = serverPort;
         this.serverPassword = serverPassword;
         initialise();
     }
 
     void start() {
         connectToServer();
     }
 
     @Override
     protected void onConnect() {
         for (IrcHandler handler : handlers) {
             handler.onConnect();
         }
     }
 
     @Override
     protected void onDisconnect() {
         for (IrcHandler handler : handlers) {
             handler.onDisconnect();
         }
         if (!disconnecting) {
             connectToServer();
         }
     }
 
     // PM was sent to us on irc
     @Override
     public void onPrivateMessage(String sender, String login, String hostname, String message) {
         if (message.startsWith(COMMAND_PREFIX)) {
             if (!isTrustedUser(sender)) {
                 LOG.info("Ignored command request '{}' from untrusted user: <{}>", new Object[] { message, sender });
                 return;
             }
             for (IrcHandler handler : handlers) {
                 String method = message.substring(1).trim();
                 String response = handleCommand(handler, method, sender);
                 if (response != null) {
                     sendMessage(sender, response);
                     LOG.info("Command: '{}' from <{}>, replied: {}", new Object[] { message, sender, response });
                    break;
                 }
             }
         }
     }
 
     @Override
     public void onMessage(String channel, String sender, String login, String hostname, String message) {
         if (!message.startsWith(nickname)) {
             // someone said something not directed to us
             for (IrcHandler handler : handlers) {
                 handler.onMessage(channel, sender, login, hostname, message);
             }
         }
     }
 
     @Override
     public void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
         for (IrcHandler handler : handlers) {
             handler.onQuit(sourceNick, sourceLogin, sourceHostname, reason);
         }
     }
 
     @Override
     public void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname,
             String recipientNick, String reason) {
         for (IrcHandler handler : handlers) {
             handler.onKick(channel, kickerNick, kickerLogin, kickerHostname, recipientNick, reason);
         }
     }
 
     /*
      * Basic built-in command processing allows you to instruct the bot at
      * runtime to join/leave channels etc
      */
     private String handleCommand(IrcHandler handler, String cmd, String sender) {
         String tokens[] = cmd.split(" ");
         String method = tokens[0];
         return handler.executeCommand(method, tokens, sender);
     }
 
     private void connectToServer() {
         try {
             int tries = 0;
             while (!isConnected()) {
                 tries++;
                 LOG.info("Connecting to server '{}' [try {}]:", serverAddress, tries);
                 connect(serverAddress, serverPort, serverPassword);
                 if (tries > 1) {
                     Thread.sleep(10000);
                 }
             }
             LOG.info("Connected to: {}:{}", serverAddress, serverPort);
         } catch (Exception e) {
             LOG.error("Error connecting to server", e);
         }
     }
 
     void shutdown() {
         disconnecting = true;
         try {
             disconnect();
         } finally {
             dispose();
         }
     }
 
     // is this nick trusted? (are they in the default channel)
     public boolean isTrustedUser(String nickName) {
         User[] users = getUsers(defaultChannel.getIdentifier());
         for (User user : users) {
             if (nickName.equalsIgnoreCase(user.getNick())) {
                 return true;
             }
         }
         return false;
     }
 
     void setHandlers(List<IrcHandler> handlers) {
         this.handlers = handlers;
     }
 
     private void initialise() throws UnsupportedEncodingException {
         setEncoding("UTF8");
         setName(nickname);
         setLogin(nickname);
         setMessageDelay(getMessageDelay());
         setFinger(getFinger());
     }
 
 }
