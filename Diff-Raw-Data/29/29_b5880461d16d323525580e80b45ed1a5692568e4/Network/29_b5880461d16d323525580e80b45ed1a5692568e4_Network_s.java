 /*   _______ __ __                    _______                    __   
  *  |     __|__|  |.--.--.-----.----.|_     _|.----.-----.--.--.|  |_ 
  *  |__     |  |  ||  |  |  -__|   _|  |   |  |   _|  _  |  |  ||   _|
  *  |_______|__|__| \___/|_____|__|    |___|  |__| |_____|_____||____|
  * 
  *  Copyright 2008 - Gustav Tiger, Henrik Steen and Gustav Sothell
  * 
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  * 
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package silvertrout;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.LinkedList;
 
 import java.net.SocketException;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.io.BufferedReader;
 
 /**
  * Network class that handles connection to the network. Sending and
  * 
  * 
  */
 public class Network implements Runnable {
 
     public enum State {
 
         CONNECTED, DISCONNECTED
     };
     public String name;
     private String host;
     private int port;
     public User me;
     public ArrayList<Channel> channels;
     public ArrayList<User> users;
     private Socket socket;
     private PrintWriter out;
     private BufferedReader in;
     public LinkedList<String> outputQueue;
     public Timer timer;
     public int ticks;
     public ConcurrentHashMap<String, Plugin> plugins;
     public State state;
 
     /**
      * Create and connect to a new Network,
      *
      * @param name - The name of the Network
      * @param host - The server's ip
      * @param port - The port to connect to
      */
     public Network(String name, String host, int port) {
 
         // Init variables:
         this.plugins = new ConcurrentHashMap<String, Plugin>();
         this.outputQueue = new LinkedList<String>();
 
         // Load default plugins:
         loadPlugin("AdminBoy");
         loadPlugin("KeepAlive");
         loadPlugin("Quizmaster");
         loadPlugin("TitleGiver");
         loadPlugin("Reloader");
 
         // Ticks executes once every second:
         ticks = 0;
         timer = new Timer();
         TimerTask ticker = new TimerTask() {
 
             @Override
             public void run() {
                 Network.this.tick();
             }
         };
         timer.schedule(ticker, 0, 1000);
 
         // Connect to server
         connect(name, host, port);
     }
 
     public void connect(String name, String host, int port) {
 
         this.name = name;
         this.host = host;
         this.port = port;
         this.channels = new ArrayList<Channel>();
         this.users = new ArrayList<User>();
         this.me = new User("jbt214"); // TODO: fix name
         this.state = State.DISCONNECTED;
 
         this.users.add(this.me);
 
         // Connect to Server:
         try {
             socket = new Socket(host, port);
             out = new PrintWriter(socket.getOutputStream(), true);
             in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         } catch (UnknownHostException e) {
             System.err.println("Don't know about host: " + host + ":" + port);
             e.printStackTrace();
             System.exit(1);
         } catch (IOException e) {
             System.err.println("Couldn't get I/O for the connection.");
             e.printStackTrace();
             System.exit(1);
         }
 
         // Login (TODO: fix name and stuff)
         sendRaw("NICK " + this.me.getNickname());
         sendRaw("USER " + this.me.getNickname() + " 0 * :java irc bot #214");
 
         // Start listening thread
         new Thread(this).start();
     }
 
     public void disconnect() {
         // Set state to disconnected:
         state = State.DISCONNECTED;
         try {
             in.close();
             out.close();
             socket.close();
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     // TODO: User Manager? {
     /**
      * Search for a user with a specified nickname
      *
      * @param nickname - The nickname of the user to search for
      * @return true iff the user with the specified nick exist
      */
     public boolean existsUser(String nickname) {
         for (User u : users) {
             if (u.getNickname().equals(nickname)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Fetch a User with a specified nickname
      *
      * @param nickname - The nickname of the user to fetch
      * @return The user with the specified nickname
      */
     public User getUser(String nickname) {
         for (User u : users) {
             if (u.getNickname().equals(nickname)) {
                 return u;
             }
         }
         return null;
     }
 
     /**
      * Fetch a list of the users known on the Network
      *
      * @return A List containing all known users on the Network.
      */
     public List<User> getUsers() {
         return users;
     }
 
     /**
      * Add a user to the Network
      *
      * @param nickname - The nickname of the user to add
      */
     public void addUser(String nickname) {
         users.add(new User(nickname));
     }
 
     /**
      * Return yourself
      *
      * @return the user representing yourself
      */
     public User getMyUser() {
         return me;
     }
     // } END User Manager
 
     // TODO: Channel Manager? {
     /**
      * Fetch a ArrayList containing all known channels on the Network
      *
      * @return all known channels on the Network
      */
     public ArrayList<Channel> getChannels() {
         return channels;
     }
 
     /**
      * Search for a channel with the specified name
      *
      * @param name - The name of the channel to search for
      * @return ture iff the channel with the specified name is known on this Network
      */
     public boolean existsChannel(String name) {
         for (Channel c : channels) {
             if (c.getName().equals(name)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Returns a Channel with a specified name
      *
      * @param name - The name of the channel to search fo
      * @return The channel with the specified name, if the Channel does not exist returns null
      */
     public Channel getChannel(String name) {
         for (Channel c : channels) {
             if (c.getName().equals(name)) {
                 return c;
             }
         }
         return null;
     }
 
     /**
      * Set the topic of a channel with a specified name
      *
      * @param channel - The name of the channel to change topic in
      * @param topic - The topic to set in the channel
      */
     public void setChannelTopic(String channel, String topic) {
         for (Channel c : channels) {
             if (c.getName().equals(channel)) {
                 c.setTopic(topic);
                 break;
             }
         }
     }
 
     /**
      * Add and join a channel in the NetWork. If the channel previously been joined, no action is taking place.
      *
      * @param channel - The name of the channel to join.
      */
     public void addChannel(String channel) {
         if (!existsChannel(channel)) {
             channels.add(new Channel(channel, this));
             System.out.println("Trying to join channel " + channel);
         }
     }
 
     /**
      * Remove and part a channel from the Network. If the user is not on the channel, no action is taking place.
      *
      * @param channel - The name of the channel to part from.
      */
     public void removeChannel(String channel) {
         for (Channel c : channels) {
             if (c.getName().equals(channel)) {
                 channels.remove(c);
                 System.out.println("Parting from channel " + channel);
                 break;
             }
         }
     }
     // } END CHANNEL MANAGER (TODO ? )
 
     /**
      * Unload a plugin with the spicified name
      *
      * @param name - The name of the plugin to load
      */
     public synchronized boolean unloadPlugin(String name) {
         synchronized (plugins) {
             if (plugins.containsKey(name)) {
                 plugins.get(name).onUnload();
                 plugins.remove(name);
 
                 // Clean up after us a bit (to allow reload)
                 Runtime.getRuntime().runFinalization();
                 while (true) {
                     long freeMemory = Runtime.getRuntime().freeMemory();
                     Runtime.getRuntime().gc();
                     if (freeMemory == Runtime.getRuntime().freeMemory()) {
                         break;
                     }
                 }
 
                 return true;
             } else {
                 return false;
             }
         }
     }
 
     /**
      * Load plugin with the specified name from a file.
      *
      * @param  name  Name of the plugin to load
      */
     public synchronized boolean loadPlugin(String name) {
         if (!plugins.containsKey(name)) {
             synchronized (plugins) {
                 try {
                     PluginClassLoader pcl = new PluginClassLoader();
                    Class<?> c = pcl.findClass("jbt.plugins." + name);
                     if (Plugin.class.isAssignableFrom(c)) {
                         Plugin p = (Plugin) c.newInstance();
                         p.setNetwork(this);
                         p.onLoad();
                         plugins.put(name, p);
                         return true;
                     }
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
         }
         return false;
     }
 
     /**
      *Send a raw message to the network.
      *
      * @param message - the string to send to the Network
      */
     public synchronized void sendRaw(String message) {
         //System.out.println("Sent packet: " + message);
         outputQueue.add(message + "\r\n");
     }
 
     // Move a most of these to Channel och User: TODO TODO
     /**
      * Send a private message to either a user or a channel.
      *
      * @param to - The nick/name of the user/channel to send to
      * @param message - The string to send
      */
     public synchronized void sendPrivmsg(String to, String message) {
         sendRaw("PRIVMSG " + to + " :" + message);
     }
 
     /**
      * Send an action to either a user or a channel. Trigged on the command /me action
      *
      * @param to - The nick/name of the user/channel to send to
      * @param message - The action the user (you) are performing
      */
     public synchronized void sendAction(String to, String message) {
         sendPrivmsg(to, "ACTION " + message + "");
     }
 
     /**
      * Kick a user from a channel
      *
      * @param channel - The name of the Channel
      * @param who - The nick of the user to kick from the channel
      * @param message - The reason why the user is kicked
      */
     public synchronized void kick(String channel, String who, String message) {
         sendRaw("KICK " + channel + " " + who + " :" + message);
     }
 
     /**
      * Join a channel with the specified name
      *
      * @param channel - The name of the channel to join
      */
     public synchronized void join(String channel) {
         sendRaw("JOIN " + channel);
     }
 
     /**
      * Part from a channel with the specified name
      *
      * @param channel - The name of the channel to part from
      */
     public synchronized void part(String channel) {
         sendRaw("PART " + channel);
     }
 
     /**
      * Process a message to decide if the message is a commandline or a
      * message to a channel.
      *
      * @param msg - The message to process.
      */
     public synchronized void process(Message msg) {
 
         String cmd = msg.command;
         User usr = getUser(msg.nickname);
 
         // Handle replies / error (Possible TODO: move error handeling):
         if (msg.isReply()) {
             switch (msg.reply) {
                 case Message.RPL_TOPIC: {
                     setChannelTopic(msg.params.get(1), msg.params.get(2));
                     break;
                 }
                 case Message.RPL_ENDOFMOTD:
                 case Message.ERR_NOMOTD: {
                     System.out.println("!!! We are connected");
                     // Change state of network to connected:
                     state = State.CONNECTED;
                     for (Plugin p : plugins.values()) {
                         p.onConnected();
                     }
                     break;
                 }
                 case Message.RPL_NAMREPLY: {
                     String[] namlist = msg.params.get(3).split("\\s");
 
                     for (int i = 0; i < namlist.length; i++) {
                         Channel channel = getChannel(msg.params.get(2));
                         if (channel == null) {
                             System.out.println("channel is null: " + msg.params.get(2) + " - " + namlist[i]);
                             continue;
                         }
                         String user = namlist[i];
 
                         if (user.startsWith("+") || user.startsWith("@")) {
                             if (!existsUser(user.substring(1))) {
                                 addUser(user.substring(1));
                             }
                             if (user.startsWith("+")) {
                                 channel.addUser(getUser(user.substring(1)), new Modes("v"));
                             } else if (user.startsWith("@")) {
                                 channel.addUser(getUser(user.substring(1)), new Modes("o"));
                             }
                         } else {
                             if (!existsUser(user)) {
                                 addUser(user);
                             }
                             channel.addUser(getUser(user), new Modes());
                         }
                         System.out.println("*** Added user " + user + " to channel " + channel.getName() + ".");
                     }
                     break;
                 }
             }
             return;
 
         // Handle commands:
         } else if (msg.isCommand()) {
             try {
                 if (cmd.equals("TOPIC")) {
                     Channel channel = getChannel(msg.params.get(0));
                     String oldTopic = channel.getTopic();
                     channel.setTopic(msg.params.get(1));
                     for (Plugin p : plugins.values()) {
                         p.onTopic(usr, channel, oldTopic);
                     }
                     setChannelTopic(msg.params.get(0), msg.params.get(1));
                 } else if (cmd.equals("PING")) {
                     for (Plugin p : plugins.values()) {
                         p.onPing(msg.params.get(0));
                     }
                 } else if (cmd.equals("NOTICE")) {
                     for (Plugin p : plugins.values()) {
                         p.onNotice(usr, getChannel(msg.params.get(0)), msg.params.get(1));
                     }
                 // Private msg handler:
                 } else if (cmd.equals("PRIVMSG")) {
                     for (Plugin p : plugins.values()) {
                         p.onPrivmsg(usr, getChannel(msg.params.get(0)), msg.params.get(1));
                     }
                 // Invite handler:
                 } else if (cmd.equals("INVITE")) {
                     for (Plugin p : plugins.values()) {
                         p.onInvite(usr, msg.params.get(1));
                     }
                 // Kick handler:
                 } else if (cmd.equals("KICK")) {
                     for (Plugin p : plugins.values()) {
                         p.onKick(usr, getChannel(msg.params.get(0)),
                                 getUser(msg.params.get(1)), msg.params.get(2));
                     }
                 // Join handler:
                 } else if (cmd.equals("JOIN")) {
                     if (usr == getMyUser()) {
                         addChannel(msg.params.get(0));
                     } else {
                         if (usr == null) {
                             addUser(msg.nickname);
                         }
                         getChannel(msg.params.get(0)).
                                 addUser(getUser(msg.nickname), new Modes());
                     }
                     for (Plugin p : plugins.values()) {
                         p.onJoin(getUser(msg.nickname), getChannel(msg.params.get(0)));
                     }
                 // Part handler:
                 } else if (cmd.equals("PART")) {
                     for (Plugin p : plugins.values()) {
                         if (msg.params.size() > 1) {
                             p.onPart(usr, getChannel(msg.params.get(0)), msg.params.get(1));
                         } else {
                             p.onPart(usr, getChannel(msg.params.get(0)), new String());
                         }
                     }
                     if (usr == getMyUser()) {
                         removeChannel(msg.params.get(0));
                     } else {
                         getChannel(msg.params.get(0)).delUser(usr);
                     }
                 // Quit handler:
                 } else if (cmd.equals("QUIT")) {
                     for (Plugin p : plugins.values()) {
                         p.onQuit(usr, msg.params.get(0));
                     }
                     for (Channel c : channels) {
                         c.delUser(usr);
                     }
                     users.remove(usr);
                 // Nick handler
                 } else if (cmd.equals("NICK")) {
                     String oldNickname = usr.getNickname();
                     usr.setNickname(msg.params.get(0));
                     for (Plugin p : plugins.values()) {
                         p.onNick(usr, oldNickname);
                     }
 
                 // TODO order, args, callback first or not?
                 } else if (cmd.equals("MODE")) {
 
                     // Channel
                     if (existsChannel(msg.params.get(0))) {
                         Channel channel = getChannel(msg.params.get(0));
                         for (int i = 1; i < msg.params.size(); i++) {
                             if (msg.params.get(i).startsWith("+") || msg.params.get(i).startsWith("-")) {
 
                                 String modes = msg.params.get(i);
                                 int affects = modes.length() - 1;
                                 char sign = modes.charAt(0);
 
                                 for (int j = 0; j + i + 1 < msg.params.size() && j < modes.length() - 1; j++) {
 
                                     char mode = modes.charAt(j + 1);
                                     User user = getUser(msg.params.get(i + j + 1));
 
                                     System.out.println("trying to give " + msg.params.get(i + j + 1) + " " + sign + " " + mode + " on channel " + channel.getName() + "(" + i + "," + j + ")");
 
                                     if (sign == '+') {
                                         channel.getUsers().get(user).giveMode(mode);
                                     } else if (sign == '-') {
                                         channel.getUsers().get(user).takeMode(mode);
                                     }
 
                                 }
                             }
                         }
                     // User:
                     } else {
                         System.out.println(msg.params.get(0) + " is not a valid " + "channel?");
                     // TODO.. or not?
                     }
                 // Unknown command found
                 } else {
                     System.out.println("********* Unknown command: " + cmd);
                     System.out.println("Params count is " + msg.params.size());
                     for (String param : msg.params) {
                         System.out.println(param);
                     }
                 }
             } catch (Exception e) {
                 System.out.println("Plugin crashed in " + cmd + " handler:");
                 e.getMessage();
                 e.printStackTrace();
             }
         }
     }
 
     private synchronized void tick() {
 
 
         // Process output queue:
         if (!outputQueue.isEmpty() && out != null) {
             out.write(outputQueue.pop());
             out.flush();
         }
 
         // Call on tick handler in plugins
         for (Plugin p : plugins.values()) {
             try {
                 p.onTick(ticks);
             } catch (Exception e) {
                 System.out.println("Plugin crashed in onTick handler:");
                 e.getMessage();
                 e.printStackTrace();
             }
         }
         ticks++;
     }
 
     /**
      * SPRING!!!
      *
      *
      */
     @Override
     public void run() {
         try {
 
             String tmp;
             while ((tmp = in.readLine()) != null) {
                 synchronized (this) {
                     this.process(new Message(tmp));
                 }
             }
         //System.out.println("Disconnected okayly :) - We or the server ended the communication");
 
         } catch (SocketException e) {
             System.out.println("Disconnected by SocketException");
             System.out.println(e.getMessage());
             e.printStackTrace();
         } catch (IOException e) {
             System.out.println("Disconnected by IOException");
             System.out.println(e.getMessage());
             e.printStackTrace();
         } catch (Exception e) {
             System.out.println("Disconnected by other Exception");
             e.printStackTrace();
         }
         synchronized (this) {
             disconnect();
             // Clear channels and users:
             this.channels = new ArrayList<Channel>();
             this.users = new ArrayList<User>();
             // Tell all plugins:
             for (Plugin p : plugins.values()) {
                 p.onDisconnected();
             }
         }
     }
 }
 
