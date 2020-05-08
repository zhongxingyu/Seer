 package com.saucebot.twitch;
 
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import com.saucebot.net.Connection;
 import com.saucebot.net.ConnectionListener;
 import com.saucebot.twitch.message.IrcCode;
 import com.saucebot.twitch.message.IrcMessage;
 import com.saucebot.twitch.message.Message;
 import com.saucebot.twitch.message.SystemMessage;
 import com.saucebot.util.IRCUtils;
 
 public class TMIClient implements ConnectionListener {
 
     private final String channelName;
 
     private Set<User> ops;
 
     private Connection connection;
 
     private final String accountName;
     private final String accountPassword;
 
     private Map<IrcCode, Method> codeHandlers;
 
     private TMIListener listener;
 
     public TMIClient(final String channelName, final String accountName, final String accountPassword) {
         this.channelName = channelName;
         this.accountName = accountName;
         this.accountPassword = accountPassword;
 
         setupHandlers();
 
         ops = new HashSet<User>();
 
         connection = new Connection(Twitch.getAddressForChannel(channelName), Twitch.CHAT_PORT);
         connection.setConnectionListener(this);
        connection.setInactivityTimeLimit(45 * 60 * 1000);
         connection.connect();
     }
 
     private void setupHandlers() {
         codeHandlers = new HashMap<IrcCode, Method>();
         for (Method method : getClass().getMethods()) {
             IrcHandler handlerAnnotation = method.getAnnotation(IrcHandler.class);
             if (handlerAnnotation != null) {
                 IrcCode code = handlerAnnotation.value();
                 if (codeHandlers.put(code, method) != null) {
                     throw new IllegalStateException("Multiple handlers for " + code);
                 }
             }
         }
 
     }
 
     public void setTMIListener(final TMIListener listener) {
         this.listener = listener;
     }
 
     public void close() {
         connection.close();
     }
 
     public String getUsername() {
         return accountName;
     }
 
     public String getChannelName() {
         return channelName;
     }
 
     public String getIrcChannelName() {
         return '#' + getChannelName();
     }
 
     public Set<User> getOps() {
         return ops;
     }
 
     public boolean isOp(final User user) {
         return ops.contains(user);
     }
 
     public void addOp(final User user) {
         ops.add(user);
     }
 
     public void removeOp(final User user) {
         ops.remove(user);
     }
 
     @Override
     public void onConnected() {
         String name = getUsername();
 
         send("PASS", this.accountPassword);
         send("NICK", name);
     }
 
     @Override
     public void onDisconnected() {
         listener.onPart(this);
     }
 
     @IrcHandler(IrcCode.Endofmotd)
     public void handleEndofmotd(final IrcMessage message) {
         String chan = getIrcChannelName();
 
         send("JOIN", chan);
         send("JTVROOMS", chan);
         send("JTVCLIENT", chan);
 
         listener.onJoin(this);
     }
 
     @IrcHandler(IrcCode.Privmsg)
     public void handlePrivmsg(final IrcMessage message, final String channel, final String text) {
         String username = message.getUser();
 
         if (Twitch.SYSTEM_MESSAGE_USER.equals(username)) {
             handleSystemMessage(text);
             return;
         }
 
         User user = Users.get(username);
         listener.onMessage(this, user, isOp(user), text);
     }
 
     private void handleSystemMessage(final String line) {
         SystemMessage message = SystemMessage.parse(line);
         if (message.getType().isSystem()) {
             processMessage(message);
         } else {
             listener.onPrivateMessage(this, line);
         }
     }
 
     @IrcHandler(IrcCode.Specialuser)
     public void handleSpecialuser(final SystemMessage message, final String username, final String type) {
         SpecialUserType specialUserType = SpecialUserType.valueOf(type);
         if (!specialUserType.isChannelSpecific()) {
             Users.get(username).addSpecialUserType(specialUserType);
         }
     }
 
     @IrcHandler(IrcCode.Usercolor)
     public void handleUsercolor(final SystemMessage message, final String username, final String color) {
         Users.get(username).setColor(color);
     }
 
     @IrcHandler(IrcCode.Emoteset)
     public void handleEmoteset(final SystemMessage message, final String username, final String emoteset) {
 
     }
 
     @IrcHandler(IrcCode.Join)
     public void handleJoin(final IrcMessage message) {
         String user = message.getUser();
         if (user.equalsIgnoreCase(this.accountName)) {
             send("WHO", getIrcChannelName());
         }
     }
 
     @IrcHandler(IrcCode.Mode)
     public void handleMode(final IrcMessage message, final String channel, final String mode, final String target) {
         System.out.printf("MODE[%s] on %s\n", mode, target);
         switch (mode) {
 
         case "+o": // op
             addOp(Users.get(target));
             break;
 
         case "-o": // deop
             removeOp(Users.get(target));
             break;
 
         default:
             System.out.println("Unknown mode: " + message.getRawLine());
         }
     }
 
     @IrcHandler(IrcCode.Ping)
     public void handlePing() {
         sendRaw("PONG");
     }
 
     @IrcHandler(IrcCode.Unknown)
     public void handleOther(final Message message) {
         IrcCode type = message.getType();
         System.out.println(type.name() + ": " + message);
     }
 
     @Override
     public void onMessageReceived(final String line) {
         IrcMessage message = IrcMessage.parse(line);
         processMessage(message);
     }
 
     private void processMessage(final Message message) {
         IrcCode type = message.getType();
         Method method = codeHandlers.get(type);
         if (method != null) {
             invokeHandlerMethod(message, method);
         } else {
             handleOther(message);
         }
     }
 
     private void invokeHandlerMethod(final Message message, final Method method) {
         try {
             Class<?>[] parameters = method.getParameterTypes();
             if (parameters.length == 0) {
                 method.invoke(this);
             } else if (parameters.length == 1) {
                 method.invoke(this, message);
             } else {
                 invokeMethodWithParameters(message, method, parameters.length);
             }
         } catch (Exception e) {
             System.err.println("Error invoking handler method: " + e + " for " + message);
         }
     }
 
     private void invokeMethodWithParameters(final Message message, final Method method, final int numParameters)
             throws Exception {
         Object[] parameters = new Object[numParameters];
         parameters[0] = message;
 
         int numExtraParameters = Math.min(numParameters - 1, message.getNumArgs());
 
         for (int i = 0; i < numExtraParameters; i++) {
             parameters[i + 1] = message.getArg(i);
         }
 
         method.invoke(this, parameters);
     }
 
     private void send(final String code, final Object... args) {
         connection.send(IRCUtils.format(code, args));
     }
 
     private void sendRaw(final String line) {
         connection.send(line + '\r');
     }
 
     public void sendMessage(final String line) {
         send("PRIVMSG", getIrcChannelName(), line);
     }
 
 }
