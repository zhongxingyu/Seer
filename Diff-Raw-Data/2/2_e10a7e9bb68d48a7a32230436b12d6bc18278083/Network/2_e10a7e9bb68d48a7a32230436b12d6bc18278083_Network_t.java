 /*   _______ __ __                    _______                    __   
  *  |     __|__|  |.--.--.-----.----.|_     _|.----.-----.--.--.|  |_ 
  *  |__     |  |  ||  |  |  -__|   _|  |   |  |   _|  _  |  |  ||   _|
  *  |_______|__|__| \___/|_____|__|    |___|  |__| |_____|_____||____|
  * 
  *  Copyright 2008 - Gustav Tiger, Henrik Steen and Gustav "Gussoh" Sohtell
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
 
 import java.io.IOException;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import java.util.Collection;
 
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.Semaphore;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import silvertrout.settings.NetworkSettings;
 
 /**
  * Network class that handles connection to the network. Sending and
  * 
  * 
  */
 public class Network {
 
     private static abstract class PluginCallback {
         String callbackName;
         
         PluginCallback(String callbackName) {
           this.callbackName = callbackName;
         }
         
         abstract void   callback(Plugin plugin);
         String getCallbackName() {
             return callbackName;
         }
     }
 
     private final NetworkSettings networkSettings;
     private final User me;
     private final Map<String, Channel> channels = new HashMap<String, Channel>();
     private final Map<String, Channel> unjoinedChannels = new HashMap<String, Channel>();
     private final Map<String, User> users = new HashMap<String, User>();
     private final Map<String, Plugin> plugins = new ConcurrentHashMap<String, Plugin>();
     private final IRC irc;
     private final IRCConnection connection; // do not instantiate here, let constructior decide what kind of IRCConnection we want
     private WorkerThread workerThread;
     private String motd = new String();
     /** Indicator of exception frequenzy. 0 is good. Every exception adds one and one is removed every tick() */
     private int exceptionFrequenzy = 0;
     private int tick;
     private boolean connected = false;
 
     /**
      * Create and connect to a new Network,
      *
      * @param irc
      * @param networkSettings the settings to use for the IRCConnection
      * @param me   - Contains settings for nickname, username, realname
      * @throws IOException if connection could not be established to network
      */
     public Network(IRC irc, NetworkSettings networkSettings, User me) throws IOException {
         this.irc = irc;
         this.networkSettings = networkSettings;
         this.me = me;
 
 
         // Load plugins:
         for (Entry<String, Map<String, String>> plugin : getIrc().getSettings().getPluginsFor(networkSettings.getName()).entrySet()) {
             // settings = entry.getValue();
             if (loadPlugin(plugin.getKey())) {
                 System.out.println("Plugin loaded: " + plugin.getKey());
             } else {
                 System.out.println("Unable to load plugin: " + plugin.getKey());
             }
         }
 
         // Connect to server
         if (networkSettings.isSecure()) {
             connection = new SecureIRCConnection(this);
         } else {
             connection = new IRCConnection(this);
         }
 
         for (Map.Entry<String, String> entry : getIrc().getSettings().getChannelsFor(networkSettings.getName()).entrySet()) {
             Channel c = new Channel(entry.getKey(),this);
             c.password = entry.getValue();
             unjoinedChannels.put(entry.getKey(),c);
         }
         System.err.println("Creating worker thread");
         createWorkerThread();
     }
 
     private void createWorkerThread() {
         if (workerThread != null && !workerThread.isStop()) {
             throw new IllegalStateException("Can only start one instance of workerthread for network \"" + getNetworkSettings().getName() + "\"!");
         }
 
         workerThread = new WorkerThread();
         workerThread.setName("Network \"" + getNetworkSettings().getName() + "\"  worker thread");
 
         workerThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
 
             @Override
             public void uncaughtException(Thread t, Throwable e) {
                 workerThread.cancel();
                 exceptionFrequenzy++;
                 System.out.println("*** Exception occured in network \"" + getNetworkSettings().getName() + "\": " + e);
                 System.out.println("*** Exception frequenzy: " + exceptionFrequenzy);
 
                 e.printStackTrace();
 
                 if(exceptionFrequenzy >= 3) { // number greater than 1. Number found by injecting errors by messages. 3 seem to be a good number.
                     System.out.println("*** Too many exceptions! Closing network \"" + getNetworkSettings().getName() + "\"!");
                     getConnection().forceClose();
                     return;
                 }
 
                 System.out.println("*** Creating new worker thread...");
 
                 createWorkerThread();
 
                 System.out.println("*** New worker thread for network \"" + getNetworkSettings().getName() + "\" started!");
             }
         });
 
         System.err.println(" - Starting thread");
         workerThread.start();
     }
 
     /**
      * Connection notifies that it is unable to fullfill its job any more.
      * Something went wrong and the connection to the IRC server failed.
      * This method is not thread safe (not an exception) and should be invoked by the network thread
      * What happens if a thread cancels itself? exception?
      */
     void onDisconnect() {
         connected = false;
         // stop worker thread
         workerThread.cancel();
         
         // notify plugins
         callPlugin(new PluginCallback("onDisconnected") {
             void callback(Plugin plugin) { plugin.onDisconnected(); };
         });
 
         removeUser(me.getNickname());
         // destroy this Network instance. how? If we want to reconnect the easiest way is to create a new Network
     }
 
     void onNoMotd() {
         onEndOfMotd();
     }
 
     void onEndOfMotd() {
         onConnect();
     }
 
     void onMotd(String motdLine) {
         motd += motdLine + "\n";
     }
 
     /**
      * Someone did a +mode on user in a channel
      * @param channel
      * @param user
      * @param mode
      */
     void onGiveMode(final User giver, final Channel channel, final User receiver, final char mode) {
         channel.getUsers().get(receiver).giveMode(mode);
 
         callPlugin(new PluginCallback("onGiveMode") {
             void callback(Plugin plugin) { plugin.onGiveMode(giver, channel, receiver, mode); };
         });
     }
 
     /**
      * Invite received from known user
      * @param channel
      */
     void onInvite(final User user, final String channel) {
         callPlugin(new PluginCallback("onInvite") {
             void callback(Plugin plugin) { plugin.onInvite(user, channel); };
         });
     }
 
     /**
      * Invite received from unknown user
      * @param channel
      */
     void onInvite(String nickname, String channel) {
         onInvite(new User(nickname, this), channel);
     }
 
     /**
      * Someone did a kick
      * @param kicker
      * @param channel
      * @param receiver
      * @param message
      */
     void onKick(final User kicker, final Channel channel, final User receiver, final String message) {
         callPlugin(new PluginCallback("onKick") {
             void callback(Plugin plugin) { plugin.onKick(kicker, channel, receiver, message); };
         });
     }
 
     /**
      * Notice to channel received from known user
      * @param channel
      * @param message
      */
     void onNotice(final User user, final Channel channel, final String message) {
         callPlugin(new PluginCallback("onNotice") {
             void callback(Plugin plugin) { plugin.onNotice(user, channel, message); };
         });
     }
 
     /**
      * private notice from known user to us received
      * @param user sender
      * @param message
      */
     void onNotice(final User user, final String message) {
         callPlugin(new PluginCallback("onNotice") {
             void callback(Plugin plugin) { plugin.onNotice(user, message); };
         });
     }
 
     /**
      * notice to channel from unknown user (the user is not in the channel)
      * @param nickname
      * @param channel
      * @param message
      */
     void onNotice(String nickname, Channel channel, String message) {
         onNotice(new User(nickname, this), channel, message);
     }
 
     /**
      * private notive from unknown user
      * @param nickname
      * @param message
      */
     void onNotice(String nickname, String message) {
         onNotice(new User(nickname, this), message);
     }
 
     /**
      * We parted a channel
      * @param channel
      * @param message
      */
     void onPart(final Channel channel, final String message) {
         removeChannel(channel.getName());
         
         callPlugin(new PluginCallback("onPart") {
             void callback(Plugin plugin) { plugin.onPart(channel, message); };
         });
     }
 
     /**
      * Someone leaves a channel
      * @param user
      * @param channel
      * @param message
      */
     void onPart(final User user, final Channel channel, final String message) {
         channel.delUser(user);
 
         callPlugin(new PluginCallback("onPart") {
             void callback(Plugin plugin) { plugin.onPart(user, channel, message); };
         });
     }
 
     /**
      * Ping question received
      * @param target
      */
     void onPing(final String target) {
         callPlugin(new PluginCallback("onPing") {
             void callback(Plugin plugin) { plugin.onPing(target); };
         });
     }
 
     /**
      * Private message from unknown user
      * @param nickname
      * @param message
      */
     void onPrivmsg(String from, String to, final String message) {
         /* ignore everything we send to ourself to prevent sending loops */
         if(from == to) return;
         
         // Private message 
         if (to.equals(getMyUser().getNickname())) {
             final User user = getUser(from) != null ? getUser(from): new User(from, this);
             callPlugin(new PluginCallback("onPrivms") {
                 void callback(Plugin plugin) { plugin.onPrivmsg(user, message); };
             });
         } else if(from.equals(getMyUser().getNickname())){
             final User user = getUser(to) != null ? getUser(to): new User(to, this);
             callPlugin(new PluginCallback("onSendmsg") {
                 void callback(Plugin plugin) { plugin.onSendmsg(user, message); };
             });
         // To channel
         } else {
             final Channel channel = getChannel(to);
             final User user = getUser(from);
 
             if (user != null && channel != null) {
                 callPlugin(new PluginCallback("onPrivmsg") {
                     void callback(Plugin plugin) { plugin.onPrivmsg(user, channel, message); };
                 });
             } else {
                 // TODO: should not happend
                 System.out.println("Message from unkown person or to unknown channel");
                 return;
             }
         }
     }
 
     /**
      * Someone quits the network
      * @param user
      * @param message
      */
     void onQuit(final User user, final String message) {
         /* remove the user from all channels */
         for (Channel channel : channels.values()) {
             channel.delUser(user);
         }
 
         /* remove user from user list */
         removeUser(user.getNickname());
 
         /* run the onQuit in plugins */
         callPlugin(new PluginCallback("onQuit") {
             void callback(Plugin plugin) { plugin.onQuit(user, message); };
         });
     }
 
     /**
      * Called when someone removes a mode from a user in a channel
      * @param channel
      * @param user
      * @param mode
      */
     void onTakeMode(final User taker, final Channel channel, final User affectedUser, final char mode) {
         channel.getUsers().get(affectedUser).takeMode(mode);
 
         callPlugin(new PluginCallback("onTakeMode") {
             void callback(Plugin plugin) { plugin.onTakeMode(taker, channel, affectedUser, mode); };
         });
     }
 
     /**
      * User joined a channel
      * @param user
      * @param channel
      */
     void onJoin(String nickname, String channelName) {
 
         User user = getUser(nickname);
         Channel channel = getChannel(channelName);
 
         // Its'a me mario! (It's me joining)
        if (user.equals(getMyUser())) {
             System.out.println("Half-joining channel " + channelName);
             // Add channel to unjoined channels
             channel = new Channel(channelName, this);
             unjoinedChannels.put(channelName, channel);
         // Other user is joining
         } else {
 
             // Uknown channel (should not happend at all)
             if (channel == null) {
                 // FAAAAIIL: TODO: error check, this should not happend
                 System.out.println("JOIN: Channel was null! " + channelName + ", user: " + nickname);
                 return;
             // Known channel
             } else {
 
                 // Have not seen this user before
                 if (user == null) {
                     user = new User(nickname, this);
                     addUser(user);
                     System.out.println("New unknown user joining channel " + channelName);
                 }
                 System.out.println("New known user " + user + " joining channel " + channel);
                 // Add user 
                 channel.addUser(user, new Modes());
                 // Tell our fine plugins about this joyus occation and let them
                 // rejoice and be happy.
                 final User    finalUser    = user;
                 final Channel finalChannel = channel;
                 callPlugin(new PluginCallback("onJoin") {
                     void callback(Plugin plugin) { plugin.onJoin(finalUser, finalChannel); };
                 });
             }
         }
     }
 
     /**
      * Called when someone changes nickname
      * @param nickname
      * @param newNickname
      */
     void onNick(String nickname, String newNickname) {
 
         // TODO: can this happend without a valid user?
         final User user = getUser(nickname);
         final String oldNickname = user.getNickname();
 
         // Move user in user hash map
         users.remove(oldNickname);
         users.put(newNickname, user);
         
         // Update channels
 
         user.setNickname(newNickname);
 
         callPlugin(new PluginCallback("onNick") {
             void callback(Plugin plugin) { plugin.onNick(user, oldNickname); };
         });
     }
 
     /**
      * Called on topic change
      * @param channel
      * @param newTopic
      */
     void onTopic(String channelName, String newTopic) {
 
         System.out.println("New topic for " + channelName + " is " + newTopic);
 
         final Channel channel = getChannel(channelName);
 
         if (channel != null) {
             final String oldTopic = channel.getTopic();
             channel.setTopic(newTopic);
 
             callPlugin(new PluginCallback("onTopic") {
                 void callback(Plugin plugin) { plugin.onTopic(me, channel, oldTopic); };
             });
         } else {
             // TODO: check unavaible list?
             Channel unjoinedChannel = unjoinedChannels.get(channelName);
             if (unjoinedChannel != null) {
                 unjoinedChannel.setTopic(newTopic);
             } else {
                 System.out.println("Error: Topic for non joined and non unjoined channel " + channelName);
             }
         }
     }
 
     /**
      * Called on connection successful
      */
     void onConnect() {
         connected = true;
         addUser(me);
         
         callPlugin(new PluginCallback("onConnected") {
             void callback(Plugin plugin) { plugin.onConnected(); };
         });
 
         for (Map.Entry<String, Channel> entry : unjoinedChannels.entrySet()) {
             getConnection().join(entry.getKey(), entry.getValue().password);
         }
     }
 
     /**
      * Called when receiving a list of names from join or names command
      * @param channel
      * @param nicksWithModes
      */
     void onNames(String channelName, String[] nicksWithModes) {
         Channel channel = getChannel(channelName);
 
         if (channel == null) {
             channel = unjoinedChannels.get(channelName);
         }
 
         for (int i = 0; i < nicksWithModes.length; i++) {
             String nickname = nicksWithModes[i];
 
             if (nickname.startsWith("+") || nickname.startsWith("@") || nickname.startsWith("&") || nickname.startsWith("~")) {
                 if (!existsUser(nickname.substring(1))) {
                     addUser(new User(nickname.substring(1), this));
                 }
                 if (nickname.startsWith("+")) {
                     channel.addUser(getUser(nickname.substring(1)), new Modes("v"));
                 } else if (nickname.startsWith("@") || nickname.startsWith("&") || nickname.startsWith("~")) {
                     channel.addUser(getUser(nickname.substring(1)), new Modes("o"));
                 }
             } else {
                 if (!existsUser(nickname)) {
                     addUser(new User(nickname, this));
                 }
                 channel.addUser(getUser(nickname), new Modes());
             }
         //System.out.println("*** Added user " + user + " to c " + c.getName() + ".");
         }
 
     // Notify plugins?
     }
 
     void onEndOfNames(String channelName) {
 
         // We are done joining
         if (unjoinedChannels.containsKey(channelName)) {
             System.out.println("Done half-joining channel " + channelName);
             Channel channel = unjoinedChannels.remove(channelName);
             channels.put(channelName, channel);
 
             for (Plugin plugin : plugins.values()) {
                 try {
                     plugin.onJoin(channel);
                 } catch (Exception e) {
                     System.out.println("Plugin " + plugin + "crashed in onJoin handler:");
                     System.out.println(e.getMessage());
                     e.printStackTrace();
                 }
             }
         }
 
     // TODO: fix
     }
 
     // TODO: User Manager? {
     /**
      * Search for a user with a specified nickname
      *
      * @param nickname - The nickname of the user to search for
      * @return true iff the user with the specified nick exist
      */
     public boolean existsUser(String nickname) {
         return users.containsKey(nickname);
     }
 
     /**
      * Fetch a User with a specified nickname
      *
      * @param nickname - The nickname of the user to fetch
      * @return The user with the specified nickname
      */
     public User getUser(String nickname) {
         return users.get(nickname);
     }
 
     /**
      * Remove a User from the user list
      *
      * @param nickname - The nickname of the user to delete
      */
     private void removeUser(String nickname) {
         users.remove(nickname);
     }
 
     /**
      * Fetch a map of the users known on the Network
      *
      * @return A map containing all known users on the Network. With nickname as key and user object as value.
      */
     public Map<String, User> getUsers() {
         return users;
     }
 
     /**
      * Add a user to the Network
      *
      * @param u the user object
      */
     void addUser(User u) {
         users.put(u.getNickname(), u);
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
      * Fetch a collection containing all joined channels on the Network
      *
      * @return all known channels on the Network
      */
     public Collection<Channel> getChannels() {
         return channels.values();
     }
 
     /**
      * Get a reference to the top level.
      *
      * This is where the settings manage is available
      *
      * @return the IRC object
      */
     public IRC getIrc() {
         return irc;
     }
 
     /**
      * Get the network settings for this connection.
      *
      * This includes the name of the network, hostname and port of server and
      * so on...
      *
      * @return the network settings
      */
     public NetworkSettings getNetworkSettings() {
         return networkSettings;
     }
 
     /**
      * Get the configured plugins for this network
      *
      * @return  the plugins
      */
     public Map<String, Plugin> getPlugins() {
         return plugins;
     }
 
     /**
      * Check whether silvertrout is in a specified channel.
      *
      * @param   name  The name of the channel to search for
      * @return        True if the channel with the specified name is a channel
      *                that silvertrout is in.
      */
     public boolean isInChannel(String name) {
         return channels.containsKey(name);
     }
 
     /**
      * Returns a channel with a specified name
      *
      * @param  name The name of the channel to search fo
      * @return      The channel with the specified name, orr null if the 
      *              channel does not exist
      */
     public Channel getChannel(String name) {
         return channels.get(name);
     }
 
     /**
      * Add a channel in the Network.
      *
      * @param channel - The the channel to add.
      */
     void addChannel(Channel channel) {
         if (!isInChannel(channel.getName())) {
             channels.put(channel.getName(), channel);
         }
     }
 
     /**
      * Remove a channel from the Network.
      *
      * @param channel - The name of the channel to remove from.
      */
     void removeChannel(String channel) {
         if (channels.remove(channel) != null) {
             System.out.println("Parting from channel " + channel);
         }
     }
     // } END CHANNEL MANAGER (TODO ? )
 
     /**
      * Unload a plugin with the spicified name.
      *
      * @param  name  The name of the plugin to unload
      * @return       True if the plugin was unloaded, otherwise false
      */
     public synchronized boolean unloadPlugin(String name) {
         synchronized (plugins) {
             if (plugins.containsKey(name)) {
                 plugins.get(name).onUnload();
                 plugins.remove(name);
 
                 // Clean up after us a bit (to allow reload)
                 // XXX: Is this really neccessary?
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
      * @return       True if the plugin was loaded, otherwise false
      */
     public synchronized boolean loadPlugin(String name) {
         if (!plugins.containsKey(name)) {
             synchronized (plugins) {
                 try {
                     PluginClassLoader pcl = new PluginClassLoader();
                     Class<?> c = pcl.findClass("silvertrout.plugins." 
                             + name.toLowerCase() + "." + name);
                     if (Plugin.class.isAssignableFrom(c)) {
                         Plugin p = (Plugin) c.newInstance();
                         p.setNetwork(this);
                         p.onLoad(getIrc().getSettings().getPluginSettingsFor(
                                 getNetworkSettings().getName(), name));
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
      * Call the callback in the PluginCallback for every loaded plugin.
      *
      * TODO: logging, exception handeling, exception frequency, etc
      *
      * @param  pcb  Plugin callback to call for every loaded plugin
      */
     public void callPlugin(PluginCallback pcb) {
           for (Plugin plugin : plugins.values()) {
             try {
                 pcb.callback(plugin);
             } catch (Exception e) {
                 System.out.println("Plugin " + plugin + "crashed in " 
                         + pcb.getCallbackName() + " handler:");
                 System.out.println(e.getMessage());
                 e.printStackTrace();
             }
         }
     }
 
     /**
      * Get an indicator of the number of exceptions occuring in this network's working thread.
      * 0 is good. Every exception adds one and every tick() removes one.
      *
      * @return The exception frequencey.
      */
     public int getExceptionFrequenzy() {
         return exceptionFrequenzy;
     }
 
     /** 
      * Get the current tick.
      *
      * @return  the current tick (i.e. seconds since silvertrout started)
      */
     public int getTick(){
         return tick;
     }
     
     /**
      * 
      *
      */
     private void onTick(final int ticks) {
     
         if(!connected) {
             return;
         }
     
         tick = ticks;
         if(exceptionFrequenzy > 0) {
             exceptionFrequenzy--;
         }
         
         callPlugin(new PluginCallback("onTick") {
             void callback(Plugin plugin) { plugin.onTick(ticks); };
         });
     }
 
     /**
      * Get a handle for the worker thread. This should be used by caution since
      * this exposes the inner "pricate" class workerthread.
      * This is used by IRCconnection to add new incommin messages for handeling.
      *
      * @return  the worker thread
      */
     WorkerThread getWorkerThread() {
         return workerThread;
     }
 
     /**
      * Get the connection to this network.
      * Use this to send messages and other stuff
      *
      * @return  the connection to the network
      */
     public IRCConnection getConnection() {
         return connection;
     }
 
     /**
      *
      */
     public class WorkerThread extends Thread {
 
         private final ConcurrentLinkedQueue<Runnable> tasks = new ConcurrentLinkedQueue<Runnable>();
         private final Semaphore taskSempaphore = new Semaphore(0);
         private final IRCProcessor processor = new IRCProcessor(Network.this);
         private final Timer tickTimer = new Timer("Tick timer");
         private int ticks = 0;
         private boolean stop = false;
 
         /**
          * Put something in queue to be invoked by the network thread.
          *
          * @param  task  thing to do
          */
         public void invokeLater(Runnable task) {
             tasks.add(task);
             taskSempaphore.release();
         }
 
         /**
          * Process an incoming message from the IRCConnection
          *
          * @param  msg  message to process
          */
         public void process(final Message msg) {
             invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     processor.process(msg);
                 }
             });
         }
 
         /**
          * Terminates the worker thread and the tick timer.
          * Does not let the workerthread process its queue.
          * This method is not thread safe and should be invoked by the network thread.
          */
         public void cancel() {
             stop = true;
             tickTimer.cancel();
             workerThread.interrupt(); // will this work? I dont think so. Cancel is run by the workerThread. Doesn't matter.
         }
 
         /**
          * Checks if the thread stopped.
          *
          * @return  True if the thread is stopped
          */
         public boolean isStop() {
             return stop;
         }
 
         @Override
         public void run() { // Wroker thread started!
 
             TimerTask ticker = new TimerTask() {
 
                 @Override
                 public void run() { // Executed by timer
 
                     invokeLater(new Runnable() { // Give network thread a new task
 
                         @Override
                         public void run() { // Executed by network thread
                             onTick(ticks);
                             ticks++;
                         }
                     });
                 }
             };
             tickTimer.schedule(ticker, 1000, 1000); // wait one second before first tick to make the exceptionFrequenzy work.
 
             for (;;) {
                 try {
                     if (!stop) {
                         taskSempaphore.acquire();
                         tasks.poll().run();
                     } else {
                         return;
                     }
                 } catch (InterruptedException ex) {
                     Logger.getLogger(Network.class.getName()).log(Level.SEVERE, null, ex);
                     cancel(); // stop the thread and die...
                 }
             }
         }
     }
 }
 
