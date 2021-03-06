 /*
  * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc;
 
 import com.dmdirc.actions.ActionManager;
 import com.dmdirc.actions.CoreActionType;
 import com.dmdirc.commandparser.CommandManager;
 import com.dmdirc.commandparser.CommandWindow;
 import com.dmdirc.identities.ConfigManager;
 import com.dmdirc.identities.ConfigSource;
 import com.dmdirc.logger.ErrorLevel;
 import com.dmdirc.logger.Logger;
 import com.dmdirc.parser.ChannelInfo;
 import com.dmdirc.parser.ClientInfo;
 import com.dmdirc.parser.IRCParser;
 import com.dmdirc.parser.MyInfo;
 import com.dmdirc.parser.ParserError;
 import com.dmdirc.parser.ServerInfo;
 import com.dmdirc.parser.callbacks.CallbackNotFound;
 import com.dmdirc.parser.callbacks.interfaces.IAwayState;
 import com.dmdirc.parser.callbacks.interfaces.IAwayStateOther;
 import com.dmdirc.parser.callbacks.interfaces.IChannelSelfJoin;
 import com.dmdirc.parser.callbacks.interfaces.IConnectError;
 import com.dmdirc.parser.callbacks.interfaces.IErrorInfo;
 import com.dmdirc.parser.callbacks.interfaces.IGotNetwork;
 import com.dmdirc.parser.callbacks.interfaces.IMOTDEnd;
 import com.dmdirc.parser.callbacks.interfaces.IMOTDLine;
 import com.dmdirc.parser.callbacks.interfaces.IMOTDStart;
 import com.dmdirc.parser.callbacks.interfaces.INickInUse;
 import com.dmdirc.parser.callbacks.interfaces.INoticeAuth;
 import com.dmdirc.parser.callbacks.interfaces.INumeric;
 import com.dmdirc.parser.callbacks.interfaces.IPingFailed;
 import com.dmdirc.parser.callbacks.interfaces.IPingSuccess;
 import com.dmdirc.parser.callbacks.interfaces.IPost005;
 import com.dmdirc.parser.callbacks.interfaces.IPrivateAction;
 import com.dmdirc.parser.callbacks.interfaces.IPrivateCTCP;
 import com.dmdirc.parser.callbacks.interfaces.IPrivateCTCPReply;
 import com.dmdirc.parser.callbacks.interfaces.IPrivateMessage;
 import com.dmdirc.parser.callbacks.interfaces.IPrivateNotice;
 import com.dmdirc.parser.callbacks.interfaces.ISocketClosed;
 import com.dmdirc.parser.callbacks.interfaces.IUserModeChanged;
 import com.dmdirc.ui.MainFrame;
 import com.dmdirc.ui.ServerFrame;
 import com.dmdirc.ui.input.TabCompleter;
 import com.dmdirc.ui.messages.Formatter;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.ImageIcon;
 import javax.swing.JInternalFrame;
 import javax.swing.SwingUtilities;
 
 /**
  * The Server class represents the client's view of a server. It maintains
  * a list of all channels, queries, etc, and handles parser callbacks pertaining
  * to the server.
  * @author chris
  */
 public final class Server extends FrameContainer implements IChannelSelfJoin,
         IPrivateMessage, IPrivateAction, IErrorInfo, IPrivateCTCP,
         IPrivateCTCPReply, ISocketClosed, IPrivateNotice, IMOTDStart,
         IMOTDLine, IMOTDEnd, INumeric, IGotNetwork, IPingFailed, IPingSuccess,
         IAwayState, IConnectError, IAwayStateOther, INickInUse, IPost005,
         INoticeAuth, IUserModeChanged {
     
     /** The callbacks that should be registered for server instances. */
     private final static String[] callbacks = {
         "OnChannelSelfJoin", "OnErrorInfo", "OnPrivateMessage", "OnPingSuccess",
         "OnPrivateAction", "OnPrivateCTCP", "OnPrivateNotice", "OnConnectError",
         "OnPrivateCTCPReply", "OnSocketClosed", "OnGotNetwork", "OnNumeric",
         "OnMOTDStart", "OnMOTDLine", "OnMOTDEnd", "OnPingFailed", "OnAwayState",
         "OnAwayStateOther", "OnNickInUse", "OnPost005", "OnNoticeAuth",
         "OnUserModeChanged"
     };
     
     /** Open channels that currently exist on the server. */
     private final Map<String, Channel> channels  = new Hashtable<String, Channel>();
     /** Open query windows on the server. */
     private final Map<String, Query> queries = new Hashtable<String, Query>();
     
     /** The IRC Parser instance handling this server. */
     private IRCParser parser;
     /** The raw frame used for this server instance. */
     private Raw raw;
     /** The ServerFrame corresponding to this server. */
     private ServerFrame frame;
     
     /** The name of the server we're connecting to. */
     private String server;
     /** The port we're connecting to. */
     private int port;
     /** The password we're using to connect. */
     private String password;
     /** Whether we're using SSL or not. */
     private boolean ssl;
     /** The profile we're using. */
     private ConfigSource profile;
     
     /**
      * Used to indicate that this server is in the process of closing all of its
      * windows, and thus requests for individual ones to be closed should be
      * ignored.
      */
     private boolean closing;
     
     /** The tabcompleter used for this server. */
     private final TabCompleter tabCompleter = new TabCompleter();
     /** The last activated internal frame for this server. */
     private FrameContainer activeFrame = this;
     /** The config manager for this server. */
     private ConfigManager configManager;
     
     /** Whether we're marked as away or not. */
     private boolean away;
     /** Our reason for being away, if any. */
     private String awayMessage;
     /** Whether we should attempt to reconnect or not. */
     private boolean reconnect = true;
     
     /**
      * Creates a new instance of Server.
      * @param server The hostname/ip of the server to connect to
      * @param port The port to connect to
      * @param password The server password
      * @param ssl Whether to use SSL or not
      * @param profile The profile to use
      */
     public Server(final String server, final int port, final String password,
             final boolean ssl, final ConfigSource profile) {
         super();
         
         this.server = server;
         
         ServerManager.getServerManager().registerServer(this);
         
         configManager = new ConfigManager("", "", server);
         
         frame = new ServerFrame(this);
         frame.setTitle(server + ":" + port);
         frame.setTabCompleter(tabCompleter);
         frame.addInternalFrameListener(this);
         MainFrame.getMainFrame().addChild(frame);
         
         frame.open();
         
         tabCompleter.addEntries(CommandManager.getServerCommandNames());
         tabCompleter.addEntries(CommandManager.getGlobalCommandNames());
         
         new Timer().scheduleAtFixedRate(new TimerTask() {
             public void run() {
                 for (Channel channel : channels.values()) {
                     channel.checkWho();
                 }
             }
         }, 0, Config.getOptionInt("general", "whotime", 60000));
         
         connect(server, port, password, ssl, profile);
     }
     
     /**
      * Connects to a new server with the specified details.
      * @param server The hostname/ip of the server to connect to
      * @param port The port to connect to
      * @param password The server password
      * @param ssl Whether to use SSL or not
      * @param profile The profile to use
      */
     public void connect(final String server, final int port, final String password,
             final boolean ssl, final ConfigSource profile) {
         if (closing) {
             Logger.error(ErrorLevel.WARNING, "Attempted to connect to a server while frame is closing.");
             return;
         }
         
         reconnect = true;
         
         if (parser != null && parser.getSocketState() == parser.STATE_OPEN) {
             disconnect(configManager.getOption("general", "quitmessage"));
         }
         
         this.server = server;
         this.port = port;
         this.password = password;
         this.ssl = ssl;
         this.profile = profile;
         
         configManager = new ConfigManager("", "", server);
         
         final ClassLoader cldr = this.getClass().getClassLoader();
         URL imageURL;
         if (ssl) {
             imageURL = cldr.getResource("com/dmdirc/res/secure-server.png");
         } else {
             imageURL = cldr.getResource("com/dmdirc/res/server.png");
         }
         imageIcon = new ImageIcon(imageURL);
         frame.setFrameIcon(imageIcon);
         
         addLine("serverConnecting", server, port);
         sendNotification();
         
         final MyInfo myInfo = new MyInfo();
         myInfo.setNickname(profile.getOption("profile", "nickname"));
         myInfo.setRealname(profile.getOption("profile", "realname"));
         
         if (profile.hasOption("profile", "ident")) {
             myInfo.setUsername(profile.getOption("profile", "ident"));
         }
         
         final ServerInfo serverInfo = new ServerInfo(server, port, password);
         serverInfo.setSSL(ssl);
         parser = new IRCParser(myInfo, serverInfo).setCreateFake(true);
         
         if (raw == null && Config.getOptionBool("general", "showrawwindow")) {
             raw = new Raw(this);
             MainFrame.getMainFrame().getFrameManager().addRaw(this, raw);
         }
         
         try {
             for(String callback : callbacks) {
                 parser.getCallbackManager().addCallback(callback, this);
             }
         } catch (CallbackNotFound ex) {
             Logger.error(ErrorLevel.FATAL, "Unable to register server event handlers", ex);
         }
         
         for (Query query : queries.values()) {
             query.reregister();
         }
         
         away = false;
         awayMessage = null;
         frame.setAway(false);
         
         try {
             new Thread(parser).start();
         } catch (IllegalThreadStateException ex) {
             Logger.error(ErrorLevel.FATAL, "Unable to start IRC Parser", ex);
         }
         
         updateIgnoreList();
     }
     
     /**
      * Reconnects to the IRC server with a specified reason
      * @param reason The quit reason to send
      */
     public void reconnect(final String reason) {
         disconnect(reason);
         connect(server, port, password, ssl, profile);
     }
     
     /** Reconnects to the IRC server. */
     public void reconnect() {
         reconnect(Config.getOption("general", "reconnectmessage"));
     }
     
     /**
      * Updates the ignore list for this server.
      */
     public void updateIgnoreList() {
         parser.getIgnoreList().clear();
         
         if (configManager.hasOption("network", "ignorelist")) {
             for (String line : configManager.getOption("network", "ignorelist").split("\n")) {
                 parser.getIgnoreList().add(line);
             }
         }
     }
     
     /**
      * Determines whether the server knows of the specified channel.
      * @param channel The channel to be checked
      * @return True iff the channel is known, false otherwise
      */
     public boolean hasChannel(final String channel) {
         return channels.containsKey(channel.toLowerCase(Locale.getDefault()));
     }
     
     /**
      * Retrieves the specified channel belonging to this server.
      * @param channel The channel to be retrieved
      * @return The appropriate channel object
      */
     public Channel getChannel(final String channel) {
         return channels.get(channel.toLowerCase(Locale.getDefault()));
     }
     
     /**
      * Retrieves a list of channel names belonging to this server.
      * @return list of channel names belonging to this server
      */
     public List<String> getChannels() {
         final ArrayList<String> res = new ArrayList<String>();
         
         for (String channel : channels.keySet()) {
             res.add(channel);
         }
         
         return res;
     }
     
     /**
      * Determines whether the server knows of the specified query.
      * @param query The query to be checked
      * @return True iff the query is known, false otherwise
      */
     public boolean hasQuery(final String query) {
         return queries.containsKey(query.toLowerCase(Locale.getDefault()));
     }
     
     /**
      * Retrieves the specified query belonging to this server.
      * @param query The query to be retrieved
      * @return The appropriate query object
      */
     public Query getQuery(final String query) {
         return queries.get(query.toLowerCase(Locale.getDefault()));
     }
     
     /**
      * Retrieves a list of queries belonging to this server.
      * @return list of queries belonging to this server
      */
     public List<String> getQueries() {
         final ArrayList<String> res = new ArrayList<String>();
         
         for (String query : queries.keySet()) {
             res.add(query);
         }
         
         return res;
     }
     
     /**
      * Retrieves the raw window associated with this server.
      * @return The raw window associated with this server.
      */
     public Raw getRaw() {
         return raw;
     }
     
     /**
      * Retrieves the parser used for this connection.
      * @return IRCParser this connection's parser
      */
     public IRCParser getParser() {
         return parser;
     }
     
     /**
      * Retrieves the profile that's in use for this server.
      * @return The profile in use by this server
      */
     public ConfigSource getProfile() {
         return profile;
     }
     
     /**
      * Retrieves the name of this server.
      * @return The name of this server
      */
     public String getName() {
         return this.server;
     }
     
     /**
      * Retrieves the name of this server's network.
      * @return The name of this server's network
      */
     public String getNetwork() {
         return parser.getNetworkName();
     }
     
     /**
      * Retrieves the name of this server's IRCd.
      * @return The name of this server's IRCd
      */
     public String getIrcd() {
         return parser.getIRCD(true);
     }
     
     /**
      * Returns the current away status.
      * @return True if the client is marked as away, false otherwise
      */
     public boolean isAway() {
         return away;
     }
     
     /**
      * Gets the current away message.
      * @return Null if the client isn't away, or a textual away message if it is
      */
     public String getAwayMessage() {
         return awayMessage;
     }
     
     /**
      * Returns the tab completer for this connection.
      * @return The tab completer for this server
      */
     public TabCompleter getTabCompleter() {
         return tabCompleter;
     }
     
     /** {@inheritDoc} */
     public CommandWindow getFrame() {
         return frame;
     }
     
     /** {@inheritDoc} */
     @Override
     public ConfigManager getConfigManager() {
         return configManager;
     }
     
     /**
      * closes this server connection and associated windows.
      * @param reason reason for closing
      */
     public void close(final String reason) {
         closing = true;
         
         if (parser != null) {
             // Unregister parser callbacks
             parser.getCallbackManager().delAllCallback(this);
         }
         // Unregister frame callbacks
         frame.removeInternalFrameListener(this);
         // Disconnect from the server
         disconnect(reason);
         // Close all channel windows
         closeChannels();
         // Close all query windows
         closeQueries();
         // Close the raw window
         if (raw != null) {
             raw.close();
         }
         // Unregister ourselves with the server manager
         ServerManager.getServerManager().unregisterServer(this);
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 // Close our own window
                 frame.setVisible(false);
                 MainFrame.getMainFrame().delChild(frame);
                 frame = null;
             }
         });
         // Ditch the parser
         parser = null;
     }
     
     /** {@inheritDoc} */
     public void close() {
         close(configManager.getOption("general", "quitmessage"));
     }
     
     /**
      * Disconnects from the server.
      * @param reason disconnect reason
      */
     public void disconnect(final String reason) {
         reconnect = false;
         
         if (parser != null && parser.isReady()) {
             parser.disconnect(reason);
             
             if (configManager.getOptionBool("general", "closechannelsonquit")) {
                 closeChannels();
             } else {
                 clearChannels();
             }
             
             if (configManager.getOptionBool("general", "closequeriesonquit")) {
                 closeQueries();
             }
         }
     }
     
     /**
      * closes all open channel windows associated with this server.
      */
     private void closeChannels() {
         boolean wasClosing = closing;
         closing = true;
         for (Channel channel : channels.values()) {
             channel.closeWindow();
         }
         channels.clear();
         closing = wasClosing;
     }
     
     /**
      * Clears the nicklist of all open channels.
      */
     private void clearChannels() {
         for (Channel channel : channels.values()) {
             channel.resetWindow();
         }
     }
     
     /**
      * closes all open query windows associated with this server.
      */
     private void closeQueries() {
         boolean wasClosing = closing;
         closing = true;
         for (Query query : queries.values()) {
             query.close();
         }
         queries.clear();
         closing = wasClosing;
     }
     
     /**
      * Removes our reference to the raw object (presumably after it has been
      * closed).
      */
     public void delRaw() {
         MainFrame.getMainFrame().getFrameManager().delRaw(this, raw);
         raw = null;
     }
     
     /**
      * Removes a specific channel and window from this server.
      * @param chan channel to remove
      */
     public void delChannel(final String chan) {
         tabCompleter.removeEntry(chan);
         MainFrame.getMainFrame().getFrameManager().delChannel(
                 this, channels.get(chan.toLowerCase(Locale.getDefault())));
         if (!closing) {
             channels.remove(chan.toLowerCase(Locale.getDefault()));
         }
     }
     
     /**
      * Adds a specific channel and window to this server.
      * @param chan channel to add
      */
     private void addChannel(final ChannelInfo chan) {
         final Channel newChan = new Channel(this, chan);
         
         tabCompleter.addEntry(chan.getName());
         channels.put(chan.getName().toLowerCase(), newChan);
         MainFrame.getMainFrame().getFrameManager().addChannel(this, newChan);
         
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 newChan.show();
             }
         });
     }
     
     /**
      * Adds a query query to this server.
      * @param host host of the remote client being queried
      */
     public void addQuery(final String host) {
         final Query newQuery = new Query(this, host);
         
         tabCompleter.addEntry(ClientInfo.parseHost(host));
         queries.put(ClientInfo.parseHost(host).toLowerCase(), newQuery);
         MainFrame.getMainFrame().getFrameManager().addQuery(this, newQuery);
     }
     
     /**
      * Deletes a query from this server.
      * @param host host of the remote client being queried
      */
     public void delQuery(final String host) {
         tabCompleter.removeEntry(ClientInfo.parseHost(host));
         MainFrame.getMainFrame().getFrameManager().delQuery(this,
                 queries.get(ClientInfo.parseHost(host).toLowerCase()));
         if (!closing) {
             queries.remove(ClientInfo.parseHost(host).toLowerCase());
         }
     }
     
     /** {@inheritDoc} */
     @Override
     public boolean ownsFrame(final JInternalFrame target) {
         // Check if it's our server frame
         if (frame != null && frame.equals(target)) { return true; }
         // Check if it's the raw frame
         if (raw != null && raw.ownsFrame(target)) { return true; }
         // Check if it's a channel frame
         for (Channel channel : channels.values()) {
             if (channel.ownsFrame(target)) { return true; }
         }
         // Check if it's a query frame
         for (Query query : queries.values()) {
             if (query.ownsFrame(target)) { return true; }
         }
         return false;
     }
     
     /**
      * Sets the specified frame as the most-recently activated.
      * @param source The frame that was activated
      */
     public void setActiveFrame(final FrameContainer source) {
         activeFrame = source;
     }
     
     /**
      * Passes the arguments to the most recently activated frame for this
      * server. If the frame isn't know, or isn't visible, use this frame
      * instead.
      * @param messageType The type of message to send
      * @param args The arguments for the message
      */
     public void addLineToActive(final String messageType, final Object... args) {
         if (activeFrame == null || !activeFrame.getFrame().isVisible()) {
             activeFrame = this;
         }
         
         activeFrame.getFrame().addLine(messageType, args);
         activeFrame.sendNotification();
     }
     
     /**
      * Passes the arguments to all frames for this server.
      * @param messageType The type of message to send
      * @param args The arguments of the message
      */
     public void addLineToAll(final String messageType, final Object... args) {
         for (Channel channel : channels.values()) {
             channel.addLine(messageType, args);
             channel.sendNotification();
         }
         for (Query query : queries.values()) {
             query.addLine(messageType, args);
             query.sendNotification();
         }
         addLine(messageType, args);
         sendNotification();
     }
     
     /**
      * Handles general server notifications (i.e., ones note tied to a
      * specific window). The user can select where the notifications should
      * go in their config.
      * @param messageType The type of message that is being sent
      * @param args The arguments for the message
      */
     public void handleNotification(final String messageType, final Object... args) {
         String target = "server";
         if (configManager.hasOption("notifications", messageType)) {
             final String newTarget = configManager.getOption("notifications", messageType);
             if ("server".equals(newTarget) || "all".equals(newTarget) || "active".equals(newTarget)) {
                 target = newTarget;
             }
         }
         if ("server".equals(target)) {
             addLine(messageType, args);
             sendNotification();
         } else if ("all".equals(target)) {
             addLineToAll(messageType, args);
         } else if ("active".equals(target)) {
             addLineToActive(messageType, args);
         }
     }
     
     /** {@inheritDoc} */
     public void onChannelSelfJoin(final IRCParser tParser, final ChannelInfo cChannel) {
         if (hasChannel(cChannel.getName())) {
             getChannel(cChannel.getName()).setChannelInfo(cChannel);
             getChannel(cChannel.getName()).selfJoin();
         } else {
             addChannel(cChannel);
         }
     }
     
     /** {@inheritDoc} */
     public void onPrivateMessage(final IRCParser tParser, final String sMessage,
             final String sHost) {
         if (!queries.containsKey(ClientInfo.parseHost(sHost).toLowerCase())) {
             addQuery(sHost);
         }
     }
     
     /** {@inheritDoc} */
     public void onPrivateAction(final IRCParser tParser, final String sMessage,
             final String sHost) {
         if (!queries.containsKey(ClientInfo.parseHost(sHost).toLowerCase())) {
             addQuery(sHost);
         }
     }
     
     /** {@inheritDoc} */
     public void onPrivateCTCP(final IRCParser tParser, final String sType,
             final String sMessage, final String sHost) {
         final String[] parts = ClientInfo.parseHostFull(sHost);
         
         handleNotification("privateCTCP", parts[0], parts[1], parts[2], sType, sMessage);
         
         sendCTCPReply(parts[0], sType, sMessage);
     }
     
     /**
      * Replies to an incoming CTCP message.
      * @param source The source of the message
      * @param type The CTCP type
      * @param args The CTCP arguments
      */
     public void sendCTCPReply(final String source, final String type, final String args) {
         if (type.equalsIgnoreCase("VERSION")) {
             parser.sendCTCPReply(source, "VERSION", "DMDirc " + Main.VERSION
                     + " - http://www.dmdirc.com/");
         } else if (type.equalsIgnoreCase("PING")) {
             parser.sendCTCPReply(source, "PING", args);
         } else if (type.equalsIgnoreCase("CLIENTINFO")) {
             parser.sendCTCPReply(source, "CLIENTINFO", "VERSION PING CLIENTINFO");
         }
     }
     
     /** {@inheritDoc} */
     public void onPrivateCTCPReply(final IRCParser tParser, final String sType,
             final String sMessage, final String sHost) {
         final String[] parts = ClientInfo.parseHostFull(sHost);
         handleNotification("privateCTCPreply", parts[0], parts[1], parts[2], sType, sMessage);
     }
     
     /** {@inheritDoc} */
     public void onPrivateNotice(final IRCParser tParser, final String sMessage,
             final String sHost) {
         final String[] parts = ClientInfo.parseHostFull(sHost);
         handleNotification("privateNotice", parts[0], parts[1], parts[2], sMessage);
     }
     
     /** {@inheritDoc} */
     public void onNickInUse(final IRCParser tParser, final String nickname) {
         final String lastNick = tParser.getMyNickname();
         
         // If our last nick is still valid, ignore the in use message
         if (!lastNick.equalsIgnoreCase(nickname)) {
             return;
         }
         
         String newNick = null;
         
         if (profile.hasOption("profile", "altnicks")) {
             final String[] alts = profile.getOption("profile", "altnicks").split("\n");
             int offset = -1;
             
             if (!lastNick.equalsIgnoreCase(profile.getOption("profile", "nickname"))) {
                 for (String alt : alts) {
                     offset++;
                     if (alt.equalsIgnoreCase(lastNick)) {
                         break;
                     }
                 }
             }
             
             if (offset + 1 < alts.length) {
                 newNick = alts[offset + 1];
             }
         }
         
         if (newNick == null) {
             newNick = lastNick + (int) (Math.random()*10);
         }
         
         parser.setNickname(newNick);
     }
     
     /** {@inheritDoc} */
     public void onGotNetwork(final IRCParser tParser, final String networkName,
             final String ircdVersion, final String ircdType) {
         configManager = new ConfigManager(ircdType, networkName, this.server);
         
         updateIgnoreList();
     }
     
     /** {@inheritDoc} */
     public void onMOTDStart(final IRCParser tParser, final String sData) {
         addLine("motdStart", sData);
         sendNotification();
     }
     
     /** {@inheritDoc} */
     public void onMOTDLine(final IRCParser tParser, final String sData) {
         addLine("motdLine", sData);
         sendNotification();
     }
     
     /** {@inheritDoc} */
     public void onMOTDEnd(final IRCParser tParser, final boolean noMOTD) {
         addLine("motdEnd", "End of server's MOTD.");
         sendNotification();
     }
     
     /** {@inheritDoc} */
     public void onNumeric(final IRCParser tParser, final int numeric,
             final String[] token) {
         final String withIrcd = "numeric_" + tParser.getIRCD(true) + "_" + numeric;
         final String sansIrcd = "numeric_" + numeric;
         String target = null;
         
         if (Formatter.hasFormat(withIrcd)) {
             target = withIrcd;
         } else if (Formatter.hasFormat(sansIrcd)) {
             target = sansIrcd;
         } else if (Formatter.hasFormat("numeric_unknown")) {
             target = "numeric_unknown";
         }
         
         if (target != null) {
             handleNotification(target, (Object[]) token);
         }
         
         ActionManager.processEvent(CoreActionType.SERVER_NUMERIC, null, this,
                 Integer.valueOf(numeric), token);
     }
     
     /** {@inheritDoc} */
     public void onNoticeAuth(IRCParser tParser, String sData) {
         handleNotification("authNotice", sData);
         
         ActionManager.processEvent(CoreActionType.SERVER_AUTHNOTICE, null, this,
                 sData);
     }
     
     /** {@inheritDoc} */
     public void onUserModeChanged(IRCParser tParser, ClientInfo cClient, String sSetBy, String sModes) {
        // Knowing what modes have changed may help marginally.
        
        /*if (!cClient.equals(parser.getMyself())) {
             return;
         }
         
        final String[] setter = ClientInfo.parseHostFull(sSetBy);
         
         final StringBuffer format = new StringBuffer("userModeChanged");
         
         ActionManager.processEvent(CoreActionType.SERVER_USERMODES, format,
                this, , );*/
     }
     
     /** {@inheritDoc} */
     public void onAwayState(final IRCParser tParser, final boolean currentState,
             final String reason) {
         if (currentState) {
             away = true;
             awayMessage = reason;
             
             ActionManager.processEvent(CoreActionType.SERVER_AWAY, null, this, awayMessage);
         } else {
             away = false;
             awayMessage = null;
             
             ActionManager.processEvent(CoreActionType.SERVER_BACK, null, this);
         }
         
         frame.setAway(away);
     }
     
     /** {@inheritDoc} */
     public void onSocketClosed(final IRCParser tParser) {
         if (!reconnect) {
             // This has been triggered via .discconect()
             return;
         }
         
         handleNotification("socketClosed", this.server);
         
         if (configManager.getOptionBool("general", "closechannelsondisconnect")) {
             closeChannels();
         }
         
         if (configManager.getOptionBool("general", "closequeriesondisconnect")) {
             closeQueries();
         }
         
         if (reconnect && Config.getOptionBool("general", "reconnectondisconnect")) {
             reconnect();
         }
     }
     
     /** {@inheritDoc} */
     public void onConnectError(final IRCParser tParser, final ParserError errorInfo) {
         String description = "";
         
         if (errorInfo.getException() == null) {
             description = errorInfo.getData();
         } else {
             final Exception ex = errorInfo.getException();
             
             if (ex instanceof java.net.UnknownHostException) {
                 description = "Unknown host (unable to resolve)";
             } else if (ex instanceof java.net.NoRouteToHostException) {
                 description = "No route to host";
             } else if (ex instanceof java.net.SocketException) {
                 description = ex.getMessage();
             } else {
                 Logger.error(ErrorLevel.TRIVIAL, "Unknown socket error", ex);
                 description = "Unknown error: " + ex.getMessage();
             }
         }
         
         handleNotification("connectError", server, description);
         
         if (Config.getOptionBool("general", "reconnectonconnectfailure")) {
             final int delay = Config.getOptionInt("general", "reconnectdelay", 5);
             
             handleNotification("connectRetry", server, delay);
             
             new Timer().schedule(new TimerTask() {
                 public void run() {
                     reconnect();
                 }
             }, delay * 1000);
         }
     }
     
     /** {@inheritDoc} */
     public void onPingFailed(final IRCParser tParser) {
         MainFrame.getMainFrame().getStatusBar().setMessage("No ping reply from "
                 + this.server + " for over "
                 + Math.floor(parser.getPingTime(false) / 1000.0) + " seconds.", null, 10);
         
         ActionManager.processEvent(CoreActionType.SERVER_NOPING, null, this,
                 Long.valueOf(parser.getPingTime(false)));
         
         if (parser.getPingTime(false) >= configManager.getOptionInt("server", "pingtimeout", 60000)) {
             handleNotification("stonedServer", server);
             reconnect();
         }
     }
     
     /** {@inheritDoc} */
     public void onPingSuccess(final IRCParser tParser) {
         ActionManager.processEvent(CoreActionType.SERVER_GOTPING, null, this,
                 Long.valueOf(parser.getServerLag()));
     }
     
     /** {@inheritDoc} */
     public void onAwayStateOther(final IRCParser tParser,
             final ClientInfo client, final boolean state) {
         for (Channel chan : channels.values()) {
             chan.onAwayStateOther(tParser, client, state);
         }
     }
     
     /** {@inheritDoc} */
     public void onPost005(final IRCParser tParser) {
         ActionManager.processEvent(CoreActionType.SERVER_CONNECTED, null, this);
         
         if (configManager.hasOption("general", "rejoinchannels")) {
             for (Channel chan : channels.values()) {
                 chan.join();
             }
         }
     }
     
     /** {@inheritDoc} */
     public void onErrorInfo(final IRCParser tParser, final ParserError errorInfo) {
         ErrorLevel errorLevel;
         if (errorInfo.isFatal()) {
             errorLevel = ErrorLevel.FATAL;
         } else if (errorInfo.isError()) {
             errorLevel = ErrorLevel.ERROR;
         } else if (errorInfo.isWarning()) {
             errorLevel = ErrorLevel.WARNING;
         } else {
             Logger.error(ErrorLevel.WARNING,
                     "Unknown error level for parser error: " + errorInfo.getData());
             return;
         }
         
         if (errorInfo.isException()) {
             Logger.error(errorLevel, errorInfo.getData(), errorInfo.getException());
         } else {
             Logger.error(errorLevel, errorInfo.getData());
         }
     }
     
     /**
      * Returns this server's name.
      * @return A string representation of this server (i.e., its name)
      */
     public String toString() {
         return this.server;
     }
     
     /**
      * Returns the server instance associated with this frame.
      *
      * @return the associated server connection
      */
     public Server getServer() {
         return this;
     }
 }
