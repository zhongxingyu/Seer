 /*
  * Copyright (c) 2006-2011 DMDirc Developers
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
 
 package com.dmdirc.addons.parser_twitter;
 
 import com.dmdirc.addons.parser_twitter.api.TwitterAPI;
 import com.dmdirc.addons.parser_twitter.api.TwitterErrorHandler;
 import com.dmdirc.addons.parser_twitter.api.TwitterException;
 import com.dmdirc.addons.parser_twitter.api.TwitterMessage;
 import com.dmdirc.addons.parser_twitter.api.TwitterRawHandler;
 import com.dmdirc.addons.parser_twitter.api.TwitterStatus;
 import com.dmdirc.addons.parser_twitter.api.TwitterUser;
 import com.dmdirc.config.ConfigManager;
 import com.dmdirc.config.IdentityManager;
 import com.dmdirc.interfaces.ConfigChangeListener;
 import com.dmdirc.logger.ErrorManager;
 import com.dmdirc.parser.common.CallbackManager;
 import com.dmdirc.parser.common.ChannelJoinRequest;
 import com.dmdirc.parser.common.CompositionState;
 import com.dmdirc.parser.common.DefaultStringConverter;
 import com.dmdirc.parser.common.IgnoreList;
 import com.dmdirc.parser.common.QueuePriority;
import com.dmdirc.parser.common.ThreadedParser;
 import com.dmdirc.parser.interfaces.ChannelClientInfo;
 import com.dmdirc.parser.interfaces.ChannelInfo;
 import com.dmdirc.parser.interfaces.ClientInfo;
 import com.dmdirc.parser.interfaces.LocalClientInfo;
 import com.dmdirc.parser.interfaces.StringConverter;
 import com.dmdirc.parser.interfaces.callbacks.AuthNoticeListener;
 import com.dmdirc.parser.interfaces.callbacks.ChannelJoinListener;
 import com.dmdirc.parser.interfaces.callbacks.ChannelMessageListener;
 import com.dmdirc.parser.interfaces.callbacks.ChannelModeChangeListener;
 import com.dmdirc.parser.interfaces.callbacks.ChannelNamesListener;
 import com.dmdirc.parser.interfaces.callbacks.ChannelNickChangeListener;
 import com.dmdirc.parser.interfaces.callbacks.ChannelNoticeListener;
 import com.dmdirc.parser.interfaces.callbacks.ChannelSelfJoinListener;
 import com.dmdirc.parser.interfaces.callbacks.ChannelTopicListener;
 import com.dmdirc.parser.interfaces.callbacks.DataInListener;
 import com.dmdirc.parser.interfaces.callbacks.DataOutListener;
 import com.dmdirc.parser.interfaces.callbacks.DebugInfoListener;
 import com.dmdirc.parser.interfaces.callbacks.MotdEndListener;
 import com.dmdirc.parser.interfaces.callbacks.MotdLineListener;
 import com.dmdirc.parser.interfaces.callbacks.MotdStartListener;
 import com.dmdirc.parser.interfaces.callbacks.NetworkDetectedListener;
 import com.dmdirc.parser.interfaces.callbacks.NickChangeListener;
 import com.dmdirc.parser.interfaces.callbacks.NumericListener;
 import com.dmdirc.parser.interfaces.callbacks.PrivateMessageListener;
 import com.dmdirc.parser.interfaces.callbacks.PrivateNoticeListener;
 import com.dmdirc.parser.interfaces.callbacks.ServerReadyListener;
 import com.dmdirc.parser.interfaces.callbacks.SocketCloseListener;
 import com.dmdirc.parser.interfaces.callbacks.UnknownMessageListener;
 import com.dmdirc.parser.interfaces.callbacks.UserModeDiscoveryListener;
 import com.dmdirc.ui.messages.Styliser;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Twitter Parser for DMDirc.
  */
public class Twitter extends ThreadedParser implements TwitterErrorHandler, TwitterRawHandler,
         ConfigChangeListener {
 
     /** Number of loops between clearing of the status cache. */
     private static final long PRUNE_COUNT = 20;
     /** Maximum age of items to leave in the status cache when pruning. */
     private static final long PRUNE_TIME = 3600 * 1000;
 
     /** A map of this parser's implementations of common interfaces. */
     protected static final Map<Class<?>, Class<?>> IMPL_MAP = new HashMap<Class<?>, Class<?>>();
 
     static {
         IMPL_MAP.put(ChannelClientInfo.class, TwitterChannelClientInfo.class);
         IMPL_MAP.put(ChannelInfo.class, TwitterChannelInfo.class);
         IMPL_MAP.put(ClientInfo.class, TwitterClientInfo.class);
         IMPL_MAP.put(LocalClientInfo.class, TwitterClientInfo.class);
     }
 
     /** Are we connected? */
     private boolean connected = false;
 
     /** Our owner plugin. */
     private final TwitterPlugin myPlugin;
 
     /** Twitter API. */
     private TwitterAPI api = new TwitterAPI("", "", "", false, -1, false);
 
     /** Channels we are in. */
     private final Map<String, TwitterChannelInfo> channels
             = new HashMap<String, TwitterChannelInfo>();
 
     /** Clients we know. */
     private final Map<String, TwitterClientInfo> clients
             = new HashMap<String, TwitterClientInfo>();
 
     /** When did we last query the API? */
     private long lastQueryTime = 0;
 
     /** Username for twitter. */
     private final String myUsername;
 
     /** Password for twitter if not able to use oauth. */
     private final String myPassword;
 
     /** Callback Manager for Twitter. */
     private final CallbackManager myCallbackManager = new CallbackManager(this, IMPL_MAP);
 
     /** String Convertor. */
     private final DefaultStringConverter myStringConverter = new DefaultStringConverter();
 
     /** Ignore list (unused). */
     private IgnoreList myIgnoreList = new IgnoreList();
 
     /** Myself. */
     private TwitterClientInfo myself = null;
 
     /** List of currently active twitter parsers. */
     protected static final List<Twitter> PARSERS = new ArrayList<Twitter>();
 
     /** Are we waiting for authentication? */
     private boolean wantAuth = false;
 
     /** Server we are connecting to. */
     private final String myServerName;
 
     /** API Address to use. */
     private final String apiAddress;
 
     /** Are we using API Versioning? */
     private boolean apiVersioning = false;
 
     /** What API Version do we want? */
     private int apiVersion = -1;
 
     /** Address that created us. */
     private final URI myAddress;
 
     /** Main Channel Name. */
     private final String mainChannelName;
 
     /** Config Manager for this parser. */
     private ConfigManager myConfigManager = null;
 
     /** Map to store misc stuff in. */
     private final Map<Object, Object> myMap = new HashMap<Object, Object>();
 
     /** Debug enabled. */
     private boolean debugEnabled;
 
     /** Automatically leave & channels? */
     private boolean autoLeaveMessageChannel;
 
     /** Save last IDs. */
     private boolean saveLastIDs;
 
     /** Last Reply ID. */
     private long lastReplyId = -1;
 
     /** Last TimeLine ID. */
     private long lastTimelineId = -1;
 
     /** Last DM ID. */
     private long lastDirectMessageId = -1;
 
     /** Last IDs in searched hashtag channels. */
     private final Map<TwitterChannelInfo, Long> lastSearchIds
             = new HashMap<TwitterChannelInfo, Long>();
 
     /** Status count. */
     private int statusCount;
 
     /** Get sent messages. */
     private boolean getSentMessage;
 
     /** Number of API calls to use. */
     private int apicalls;
 
     /** Auto append @ to nicknames. */
     private boolean autoAt;
 
     /** Replace opening nickname. */
     private boolean replaceOpeningNickname;
 
     /** hide 500 errors. */
     private boolean hide500Errors;
 
     /**
      * Create a new Twitter Parser!
      *
      * @param address The address of the server to connect to
      * @param myPlugin Plugin that created this parser
      */
     protected Twitter(final URI address, final TwitterPlugin myPlugin) {
         final String[] bits;
         if (address.getUserInfo() == null) {
             bits = new String[]{};
         } else {
             bits = address.getUserInfo().split(":");
         }
         myUsername = bits.length == 0 ? "" : bits[0];
         myPassword = bits.length > 1 ? bits[1] : "";
 
         this.myPlugin = myPlugin;
         myServerName = address.getHost().toLowerCase();
 
         myAddress = address;
 
         resetState(true);
 
         if (getConfigManager().hasOptionString(myPlugin.getDomain(), "api.address." + myServerName)) {
             apiAddress = getConfigManager().getOption(myPlugin.getDomain(), "api.address." + myServerName);
         } else {
             apiAddress = myServerName + address.getPath();
         }
 
         if (getConfigManager().hasOptionBool(myPlugin.getDomain(), "api.versioned." + myServerName)) {
             apiVersioning = getConfigManager().getOptionBool(myPlugin.getDomain(), "api.versioned." + myServerName);
             if (getConfigManager().hasOptionInt(myPlugin.getDomain(), "api.version." + myServerName)) {
                 apiVersion = getConfigManager().getOptionInt(myPlugin.getDomain(), "api.version." + myServerName);
             }
         }
 
         mainChannelName = "&twitter";
     }
 
     /** {@inheritDoc} */
     @Override
     public void disconnect(final String message) {
        super.disconnect(message);
         connected = false;
         PARSERS.remove(this);
         api = new TwitterAPI("", "", "", false, -1, false);
 
         getCallbackManager().getCallbackType(SocketCloseListener.class).call();
     }
 
     /** {@inheritDoc} */
     @Override
     public void joinChannel(final String channel) {
         joinChannels(new ChannelJoinRequest(channel));
     }
 
     /** {@inheritDoc} */
     @Override
     public void joinChannel(final String channel, final String key) {
         joinChannels(new ChannelJoinRequest(channel, key));
     }
 
     /** {@inheritDoc} */
     @Override
     public void joinChannels(final ChannelJoinRequest... channels) {
         for (final ChannelJoinRequest request : channels) {
             final String channel = request.getName().trim();
 
             if (channel.equalsIgnoreCase(mainChannelName)) {
                 // The client is always in this channel, so ignore joins for it
                 continue;
             }
 
             if (isValidChannelName(channel) && getChannel(channel) == null) {
                 final TwitterChannelInfo newChannel = new TwitterChannelInfo(channel, this);
                 newChannel.addChannelClient(new TwitterChannelClientInfo(newChannel, myself));
                 if (channel.matches("^&[0-9]+$")) {
                     try {
                         final long id = Long.parseLong(channel.substring(1));
                         final TwitterStatus status = api.getStatus(id);
                         if (status == null) {
                             newChannel.setLocalTopic("Unknown status, or you do not have access to see it.");
                         } else {
                             if (status.getReplyTo() > 0) {
                                 newChannel.setLocalTopic(status.getText() + " [Reply to: &" + status.getReplyTo() + "]");
                             } else {
                                 newChannel.setLocalTopic(status.getText());
                             }
                             newChannel.setTopicSetter(status.getUser().getScreenName());
                             newChannel.setTopicTime(status.getTime());
                             final TwitterClientInfo client = (TwitterClientInfo) getClient(status.getUser().getScreenName());
                             if (client.isFake()) {
                                 client.setFake(false);
                                 clients.put(client.getNickname().toLowerCase(), client);
                             }
                             newChannel.addChannelClient(new TwitterChannelClientInfo(newChannel, client));
                         }
 
                         synchronized (this.channels) {
                             this.channels.put(channel, newChannel);
                         }
                     } catch (final NumberFormatException nfe) {
                     }
                 } else if (channel.charAt(0) == '#') {
                     newChannel.setLocalTopic("Search results for " + channel);
                     synchronized (this.channels) {
                         this.channels.put(channel, newChannel);
                     }
                 }
 
                 doJoinChannel(newChannel);
             } else {
                 sendNumericOutput(474, new String[]{":" + myServerName, "474", myself.getNickname(), channel, "Cannot join channel - name is not valid, or you are already there."});
             }
         }
     }
 
     /**
      * Remove a channel from the known channels list.
      *
      * @param channel The channel to part
      */
     protected void partChannel(final ChannelInfo channel) {
         if (channel == null) { return; }
         if (channel.getName().equalsIgnoreCase(mainChannelName)) {
             doJoinChannel(channel);
         } else {
             synchronized (channels) {
                 channels.remove(channel.getName());
             }
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public ChannelInfo getChannel(final String channel) {
         synchronized (channels) {
             return channels.containsKey(channel.toLowerCase()) ? channels.get(channel.toLowerCase()) : null;
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public Collection<? extends ChannelInfo> getChannels() {
         return new ArrayList<TwitterChannelInfo>(channels.values());
     }
 
     /** {@inheritDoc} */
     @Override
     public void setBindIP(final String ip) {
         // Ignore
     }
 
     /** {@inheritDoc} */
     @Override
     public int getMaxLength(final String type, final String target) {
         return 140;
     }
 
     /** {@inheritDoc} */
     @Override
     public LocalClientInfo getLocalClient() {
         return myself;
     }
 
     /** {@inheritDoc} */
     @Override
     public ClientInfo getClient(final String details) {
         final String client = TwitterClientInfo.parseHost(details);
         return clients.containsKey(client.toLowerCase()) ? clients.get(client.toLowerCase()) : new TwitterClientInfo(details, this).setFake(true);
     }
 
     /**
      * Tokenise a line.
      * splits by " " up to the first " :" everything after this is a single token
      *
      * @param line Line to tokenise
      * @return Array of tokens
      */
     public static String[] tokeniseLine(final String line) {
         if (line == null) {
             return new String[]{""};
         }
 
         final int lastarg = line.indexOf(" :");
         String[] tokens;
 
         if (lastarg > -1) {
             final String[] temp = line.substring(0, lastarg).split(" ");
             tokens = new String[temp.length + 1];
             System.arraycopy(temp, 0, tokens, 0, temp.length);
             tokens[temp.length] = line.substring(lastarg + 2);
         } else {
             tokens = line.split(" ");
         }
 
         return tokens;
     }
 
     /** {@inheritDoc} */
     @Override
     public void sendRawMessage(final String message) {
         sendRawMessage(message, QueuePriority.NORMAL);
     }
 
     /** {@inheritDoc} */
     @Override
     public void sendInvite(final String channel, final String user) {
         // TODO: Handle this properly here instead of faking IRC messages
         sendRawMessage("INVITE " + user + " " + channel);
     }
 
     /** {@inheritDoc} */
     @Override
     public void sendRawMessage(final String message, final QueuePriority priority) {
         // TODO: Parse some lines in order to fake IRC.
         final String[] bits = tokeniseLine(message);
 
         if (bits[0].equalsIgnoreCase("JOIN") && bits.length > 1) {
             joinChannel(bits[1]);
         } else if (bits[0].equalsIgnoreCase("WHOIS") && bits.length > 1) {
             if (bits[1].equalsIgnoreCase(myServerName)) {
                 sendNumericOutput(311, new String[]{":" + myServerName, "311", myself.getNickname(), bits[1], "user", myServerName, "*", "Psuedo-User for DMDirc " + myServerName + " plugin"});
                 sendNumericOutput(312, new String[]{":" + myServerName, "312", myself.getNickname(), bits[1], myServerName, "DMDirc " + myServerName + " plugin"});
             } else {
                 final boolean forced = bits.length > 2 && bits[1].equalsIgnoreCase(bits[2]);
                 final TwitterUser user = forced ? api.getUser(bits[1], true) : api.getCachedUser(bits[1]);
 
                 if (user == null) {
                     final String reason = "No such user found" + (forced ? ", see" : "in cache, try /WHOIS " + bits[1] + " " + bits[1] + " to poll twitter (uses 1 API call) or try") + " http://" + myAddress.getHost() + "/" + bits[1];
                     getCallbackManager().getCallbackType(NumericListener.class).call(401, new String[]{":" + myServerName, "401", myself.getNickname(), bits[1], reason});
                 } else {
                     // Time since last update
                     final long secondsIdle = (user.getStatus() == null ? user.getRegisteredTime() : System.currentTimeMillis() - user.getStatus().getTime()) / 1000;
                     final long signonTime = user.getRegisteredTime() / 1000;
 
                     getCallbackManager().getCallbackType(NumericListener.class).call(311, new String[]{":" + myServerName, "311", myself.getNickname(), bits[1], "user", myServerName, "*", user.getRealName() + " (http://" + myAddress.getHost() + "/" + user.getScreenName() + ")"});
 
                     final TwitterClientInfo client = (TwitterClientInfo) getClient(bits[1]);
                     if (client != null) {
                         final StringBuilder channelList = new StringBuilder();
 
                         for (final ChannelClientInfo cci : client.getChannelClients()) {
                             if (channelList.length() > 0) {
                                 channelList.append(' ');
                             }
 
                             channelList.append(cci.getImportantModePrefix());
                             channelList.append(cci.getChannel().getName());
                         }
 
                         if (channelList.length() > 0) {
                             getCallbackManager().getCallbackType(NumericListener.class).call(319, new String[]{":" + myServerName, "319", myself.getNickname(), bits[1], channelList.toString()});
                         }
                     }
 
                     // AWAY Message Abuse!
                     getCallbackManager().getCallbackType(NumericListener.class).call(301, new String[]{":" + myServerName, "301", myself.getNickname(), bits[1], "URL: " + user.getURL()});
                     getCallbackManager().getCallbackType(NumericListener.class).call(301, new String[]{":" + myServerName, "301", myself.getNickname(), bits[1], "Bio: " + user.getDescription()});
                     getCallbackManager().getCallbackType(NumericListener.class).call(301, new String[]{":" + myServerName, "301", myself.getNickname(), bits[1], "Location: " + user.getLocation()});
                     getCallbackManager().getCallbackType(NumericListener.class).call(301, new String[]{":" + myServerName, "301", myself.getNickname(), bits[1], "Status: " + user.getStatus().getText()});
                     if (bits[1].equalsIgnoreCase(myself.getNickname())) {
                         final long[] apiCalls = api.getRemainingApiCalls();
                         getCallbackManager().getCallbackType(NumericListener.class).call(301, new String[]{":" + myServerName, "301", myself.getNickname(), bits[1], "API Allowance: " + apiCalls[1]});
                         getCallbackManager().getCallbackType(NumericListener.class).call(301, new String[]{":" + myServerName, "301", myself.getNickname(), bits[1], "API Allowance Remaining: " + apiCalls[0]});
                         getCallbackManager().getCallbackType(NumericListener.class).call(301, new String[]{":" + myServerName, "301", myself.getNickname(), bits[1], "API Calls Used: " + apiCalls[3]});
                     }
 
                     getCallbackManager().getCallbackType(NumericListener.class).call(312, new String[]{":" + myServerName, "312", myself.getNickname(), bits[1], myServerName, "DMDirc " + myServerName + " plugin"});
                     getCallbackManager().getCallbackType(NumericListener.class).call(317, new String[]{":" + myServerName, "317", myself.getNickname(), bits[1], Long.toString(secondsIdle), Long.toString(signonTime), "seconds idle, signon time"});
                 }
             }
 
             getCallbackManager().getCallbackType(NumericListener.class).call(318, new String[]{":" + myServerName, "318", myself.getNickname(), bits[1], "End of /WHOIS list."});
         } else if (bits[0].equalsIgnoreCase("NAMES") && bits.length > 2) {
             if (bits[2].equalsIgnoreCase(mainChannelName)) {
                 final TwitterChannelInfo channel = (TwitterChannelInfo) getChannel(bits[2]);
                 if (channel != null) {
                     getCallbackManager().getCallbackType(ChannelNamesListener.class).call(channel);
                 }
             }
         } else if (bits[0].equalsIgnoreCase("INVITE") && bits.length > 2) {
             if (bits[2].equalsIgnoreCase(mainChannelName)) {
                 final TwitterUser user = api.addFriend(bits[1]);
                 final TwitterChannelInfo channel = (TwitterChannelInfo) getChannel(bits[2]);
                 if (channel != null && user != null) {
                     final TwitterClientInfo ci = new TwitterClientInfo(user.getScreenName(), this);
                     clients.put(ci.getNickname().toLowerCase(), ci);
                     final TwitterChannelClientInfo cci = new TwitterChannelClientInfo(channel, ci);
 
                     channel.addChannelClient(cci);
                     getCallbackManager().getCallbackType(ChannelJoinListener.class).call(channel, cci);
                 }
             } else {
                 getCallbackManager().getCallbackType(NumericListener.class).call(474, new String[]{":" + myServerName, "482", myself.getNickname(), bits[1], "You can't do that here."});
             }
         } else if (bits[0].equalsIgnoreCase("KICK") && bits.length > 2) {
             if (bits[1].equalsIgnoreCase(mainChannelName)) {
                 final TwitterChannelInfo channel = (TwitterChannelInfo) getChannel(bits[1]);
                 if (channel != null) {
                     final TwitterChannelClientInfo cci = (TwitterChannelClientInfo) channel.getChannelClient(bits[2]);
                     if (cci != null) {
                         cci.kick("Bye");
                     }
                 }
             } else {
                 getCallbackManager().getCallbackType(NumericListener.class).call(474, new String[]{":" + myServerName, "482", myself.getNickname(), bits[1], "You can't do that here."});
             }
         } else {
             getCallbackManager().getCallbackType(NumericListener.class).call(474, new String[]{":" + myServerName, "421", myself.getNickname(), bits[0], "Unknown Command - " + message});
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public boolean isValidChannelName(final String name) {
         return name.matches("^&[0-9]+$") || name.equalsIgnoreCase(mainChannelName) || name.charAt(0) == '#';
     }
 
     /** {@inheritDoc} */
     @Override
     public String getServerName() {
         return myServerName + "/" + myself.getNickname();
     }
 
     /** {@inheritDoc} */
     @Override
     public String getNetworkName() {
         return myServerName + "/" + myself.getNickname();
     }
 
     /** {@inheritDoc} */
     @Override
     public String getServerSoftware() {
         return myServerName;
     }
 
     /** {@inheritDoc} */
     @Override
     public String getServerSoftwareType() {
         return "twitter";
     }
 
     /** {@inheritDoc} */
     @Override
     public int getMaxTopicLength() {
         return 140;
     }
 
     /** {@inheritDoc} */
     @Override
     public String getBooleanChannelModes() {
         return "";
     }
 
     /** {@inheritDoc} */
     @Override
     public String getListChannelModes() {
         return "b";
     }
 
     /** {@inheritDoc} */
     @Override
     public int getMaxListModes(final char mode) {
         return 0;
     }
 
     /** {@inheritDoc} */
     @Override
     public boolean isUserSettable(final char mode) {
         return mode == 'b';
     }
 
     /** {@inheritDoc} */
     @Override
     public String getParameterChannelModes() {
         return "";
     }
 
     /** {@inheritDoc} */
     @Override
     public String getDoubleParameterChannelModes() {
         return "";
     }
 
     /** {@inheritDoc} */
     @Override
     public String getUserModes() {
         return "";
     }
 
     /** {@inheritDoc} */
     @Override
     public String getChannelUserModes() {
         return "ov";
     }
 
     /** {@inheritDoc} */
     @Override
     public CallbackManager getCallbackManager() {
         return myCallbackManager;
     }
 
     /** {@inheritDoc} */
     @Override
     public long getServerLatency() {
         return 0;
     }
 
     /** {@inheritDoc} */
     @Override
     public String getBindIP() {
         return "";
     }
 
     /** {@inheritDoc} */
     @Override
     public URI getProxy() {
         return null;
     }
 
     /** {@inheritDoc} */
     @Override
     public void setProxy(final URI proxy) {
         // TODO: Not supported (yet)
     }
 
     /** {@inheritDoc} */
     @Override
     public Map<Object, Object> getMap() {
         return myMap;
     }
 
     /** {@inheritDoc} */
     @Override
     public URI getURI() {
         return myAddress;
     }
 
     /** {@inheritDoc} */
     @Override
     public String getChannelPrefixes() {
         return "#&";
     }
 
     /** {@inheritDoc} */
     @Override
     public boolean compareURI(final URI uri) {
         return myAddress.equals(uri);
     }
 
     /** {@inheritDoc} */
     @Override
     public void sendCTCP(final String target, final String type, final String message) {
         if (wantAuth) {
             sendPrivateNotice("DMDirc has not been authorised to use this account yet.");
         } else if (target.matches("^&[0-9]+$")) {
             try {
                 final long id = Long.parseLong(target.substring(1));
                 if (type.equalsIgnoreCase("retweet") || type.equalsIgnoreCase("rt")) {
                     final TwitterStatus status = api.getStatus(id);
                     if (status == null) {
                         sendPrivateNotice("Invalid Tweet ID.");
                     } else {
                         sendPrivateNotice("Retweeting: <" + status.getUser().getScreenName() + "> " + status.getText());
                         if (api.retweetStatus(status)) {
                             sendPrivateNotice("Retweet was successful.");
                             final TwitterChannelInfo channel = (TwitterChannelInfo) getChannel(mainChannelName);
                             checkTopic(channel, myself.getUser().getStatus());
                         } else {
                             sendPrivateNotice("Retweeting Failed.");
                         }
                     }
                 } else if (type.equalsIgnoreCase("delete") || type.equalsIgnoreCase("del")) {
                     final TwitterStatus status = api.getStatus(id);
                     if (status == null) {
                         sendPrivateNotice("Invalid Tweet ID.");
                     } else {
                         sendPrivateNotice("Deleting: <" + status.getUser().getScreenName() + "> " + status.getText());
                         if (api.deleteStatus(status)) {
                             sendPrivateNotice("Deleting was successful, deleted tweets will still be accessible for some time.");
                             final TwitterChannelInfo channel = (TwitterChannelInfo) getChannel(mainChannelName);
                             checkTopic(channel, myself.getUser().getStatus());
                         } else {
                             sendPrivateNotice("Deleting Failed.");
                         }
                     }
                 }
             } catch (final NumberFormatException nfe) {
                 sendPrivateNotice("Invalid Tweet ID.");
             }
         } else if (target.equalsIgnoreCase(mainChannelName) || target.charAt(0) == '#') {
             if (type.equalsIgnoreCase("update") || type.equalsIgnoreCase("refresh")) {
                 final TwitterChannelInfo channel = (TwitterChannelInfo) getChannel(target);
                 sendChannelNotice(channel, "Refreshing...");
                 if (channel != null && !getUpdates(channel)) {
                     sendChannelNotice(channel, "No new items found.");
                 }
             }
         } else {
             sendPrivateNotice("This parser does not support CTCPs.");
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void sendCTCPReply(final String target, final String type, final String message) {
         sendPrivateNotice("This parser does not support CTCP replies.");
     }
 
     /** {@inheritDoc} */
     @Override
     public void sendMessage(final String target, final String message) {
         if (target.equalsIgnoreCase(mainChannelName)) {
             if (wantAuth) {
                 final TwitterChannelInfo channel = (TwitterChannelInfo) getChannel(target);
                 final String[] bits = message.split(" ");
                 if (bits[0].equalsIgnoreCase("usepw")) {
                     sendChannelMessage(channel, "Switching to once-off password authentication, please enter your password.");
                     api.setUseOAuth(false);
                     return;
                 }
                 try {
                     if (api.useOAuth()) {
                         api.setAccessPin(bits[0]);
                         if (api.isAllowed(true)) {
                             IdentityManager.getConfigIdentity().setOption(myPlugin.getDomain(), "token-" + myServerName + "-" + myUsername, api.getToken());
                             IdentityManager.getConfigIdentity().setOption(myPlugin.getDomain(), "tokenSecret-" + myServerName + "-" + myUsername, api.getTokenSecret());
                             sendChannelMessage(channel, "Thank you for authorising DMDirc.");
                             updateTwitterChannel();
                             wantAuth = false;
                         } else {
                             sendChannelMessage(channel, "Authorising DMDirc failed, please try again: " + api.getOAuthURL());
                         }
                     } else {
                         api.setPassword(message);
                         if (api.isAllowed(true)) {
                             sendChannelMessage(channel, "Password accepted. Please note you will need to do this every time unless your password is given in the URL.");
                             updateTwitterChannel();
                             wantAuth = false;
                         } else {
                             sendChannelMessage(channel, "Password seems incorrect, please try again.");
                         }
 
                     }
                 } catch (final TwitterException te) {
                     sendChannelMessage(channel, "There was a problem authorising DMDirc (" + te.getCause().getMessage() + ").");
                     sendChannelMessage(channel, "Please try again: " + api.getOAuthURL());
                 }
             } else {
                 if (setStatus(message)) {
                     sendPrivateNotice("Setting status ok.");
                 } else {
                     sendPrivateNotice("Setting status failed.");
                 }
             }
         } else if (wantAuth) {
             sendPrivateNotice("DMDirc has not been authorised to use this account yet.");
         } else if (target.matches("^&[0-9]+$")) {
             try {
                 if (setStatus(message, Long.parseLong(target.substring(1)))) {
                     sendPrivateNotice("Setting status ok.");
                     if (autoLeaveMessageChannel) {
                         partChannel(getChannel(target));
                     }
                 } else {
                     sendPrivateNotice("Setting status failed.");
                 }
             } catch (final NumberFormatException nfe) {
             }
         } else if (target.matches("^#.+$")) {
             sendPrivateNotice("Messages to '" + target + "' are not currently supported.");
         } else {
             if (api.newDirectMessage(target, message)) {
                 sendPrivateNotice("Sending Direct Message to '" + target + "' was successful.");
             } else {
                 sendPrivateNotice("Sending Direct Message to '" + target + "' failed.");
             }
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void sendNotice(final String target, final String message) {
         sendPrivateNotice("This parser does not support notices.");
     }
 
     /** {@inheritDoc} */
     @Override
     public void sendAction(final String target, final String message) {
         sendPrivateNotice("This parser does not support CTCPs.");
     }
 
     /** {@inheritDoc} */
     @Override
     public String getLastLine() {
         return "";
     }
 
     /** {@inheritDoc} */
     @Override
     public String[] parseHostmask(final String hostmask) {
         return TwitterClientInfo.parseHostFull(hostmask, myPlugin, this);
     }
 
     /** {@inheritDoc} */
     @Override
     public int getLocalPort() {
         return api.getPort();
     }
 
     /** {@inheritDoc} */
     @Override
     public long getPingTime() {
         return System.currentTimeMillis() - lastQueryTime;
     }
 
     /** {@inheritDoc} */
     @Override
     public void setPingTimerInterval(final long newValue) {
         /* Do Nothing. */
     }
 
     /** {@inheritDoc} */
     @Override
     public long getPingTimerInterval() {
         return -1;
     }
 
     /** {@inheritDoc} */
     @Override
     public void setPingTimerFraction(final int newValue) {
         /* Do Nothing. */
     }
 
     /** {@inheritDoc} */
     @Override
     public int getPingTimerFraction() {
         return -1;
     }
 
     /**
      * Send a notice to the client.
      *
      * @param message Message to send.
      */
     protected void sendPrivateNotice(final String message) {
         getCallbackManager().getCallbackType(PrivateNoticeListener.class).call(message, myServerName);
     }
 
     /**
      * Send a PM to the client.
      *
      * @param message Message to send.
      * @param hostname Who is the message from?
      * @param target Who is the message to?
      */
     private void sendPrivateMessage(final String message, final String hostname, final String target) {
         if (hostname.equalsIgnoreCase(myUsername)) {
             getCallbackManager().getCallbackType(UnknownMessageListener.class).call(message, target, hostname);
         } else {
             getCallbackManager().getCallbackType(PrivateMessageListener.class).call(message, hostname);
         }
     }
 
     /**
      * Send a message to the given channel.
      *
      * @param channel Channel to send message to
      * @param message Message to send.
      */
     private void sendChannelMessage(final ChannelInfo channel, final String message) {
         sendChannelMessage(channel, message, myServerName);
     }
 
     /**
      * Send a message to the given channel.
      *
      * @param channel Channel to send message to
      * @param message Message to send.
      * @param hostname Hostname that the message is from.
      */
     private void sendChannelMessage(final ChannelInfo channel, final String message, final String hostname) {
         sendChannelMessage(channel, new Date(), message, null, hostname);
     }
 
     /**
      * Send a message to the given channel.
      *
      * @param channel Channel to send message to
      * @param date The timestamp to be used for the message
      * @param message Message to send.
      * @param cci Channel Client to send from
      * @param hostname Hostname that the message is from.
      */
     private void sendChannelMessage(final ChannelInfo channel, final Date date, final String message, final ChannelClientInfo cci, final String hostname) {
         getCallbackManager().getCallbackType(ChannelMessageListener.class).call(date, channel, cci, message, hostname);
     }
 
     /**
      * Send a notice to the given channel.
      *
      * @param channel Channel to send notice to
      * @param notice Notice to send.
      */
     private void sendChannelNotice(final ChannelInfo channel, final String notice) {
         sendChannelNotice(channel, notice, myServerName);
     }
 
     /**
      * Send a notice to the given channel.
      *
      * @param channel Channel to send notice to
      * @param notice Notice to send.
      * @param hostname Hostname that the notice is from.
      */
     private void sendChannelNotice(final ChannelInfo channel, final String notice, final String hostname) {
         sendChannelNotice(channel, new Date(), notice, null, hostname);
     }
 
     /**
      * Send a notice to the given channel.
      *
      * @param channel Channel to send notice to
      * @param date The timestamp to be used for the notice
      * @param notice Notice to send.
      * @param cci Channel Client to send from
      * @param hostname Hostname that the notice is from.
      */
     private void sendChannelNotice(final ChannelInfo channel, final Date date, final String notice, final ChannelClientInfo cci, final String hostname) {
         getCallbackManager().getCallbackType(ChannelNoticeListener.class).call(date, channel, cci, notice, hostname);
     }
 
     /**
      * Show the user an ascii failwhale!
      */
     public void showFailWhale() {
         final String prefix = Character.toString(Styliser.CODE_FIXED)
                 + Character.toString(Styliser.CODE_HEXCOLOUR);
         sendPrivateNotice(prefix + "EB5405,71C5C5                        ");
         sendPrivateNotice(prefix + "EB5405,71C5C5  W     W      W        ");
         sendPrivateNotice(prefix + "EB5405,71C5C5  W        W  W     W   ");
         sendPrivateNotice(prefix + "FFFFFF,71C5C5                '." + Styliser.CODE_HEXCOLOUR + "EB5405,71C5C5  W   ");
         sendPrivateNotice(prefix + "FFFFFF,71C5C5    .-\"\"-._     \\ \\.--| ");
         sendPrivateNotice(prefix + "FFFFFF,71C5C5   /       \"-..__) .-'  ");
         sendPrivateNotice(prefix + "FFFFFF,71C5C5  |     _         /     ");
         sendPrivateNotice(prefix + "FFFFFF,71C5C5  \\'-.__,   .__.,'      ");
         sendPrivateNotice(prefix + "FFFFFF,71C5C5   `'----'._\\--'        ");
         sendPrivateNotice(prefix + "FFFFFF,71C5C5V" + Styliser.CODE_HEXCOLOUR + "EB5405,71C5C5V" + Styliser.CODE_HEXCOLOUR + "FFFFFF,71C5C5V" + Styliser.CODE_HEXCOLOUR + "EB5405,71C5C5V" + Styliser.CODE_HEXCOLOUR + "FFFFFF,71C5C5V" + Styliser.CODE_HEXCOLOUR + "EB5405,71C5C5V" + Styliser.CODE_HEXCOLOUR + "FFFFFF,71C5C5V" + Styliser.CODE_HEXCOLOUR + "EB5405,71C5C5V" + Styliser.CODE_HEXCOLOUR + "FFFFFF,71C5C5V" + Styliser.CODE_HEXCOLOUR + "EB5405,71C5C5V" + Styliser.CODE_HEXCOLOUR + "FFFFFF,71C5C5V" + Styliser.CODE_HEXCOLOUR + "EB5405,71C5C5V" + Styliser.CODE_HEXCOLOUR + "FFFFFF,71C5C5V" + Styliser.CODE_HEXCOLOUR + "EB5405,71C5C5V" + Styliser.CODE_HEXCOLOUR + "FFFFFF,71C5C5V" + Styliser.CODE_HEXCOLOUR + "EB5405,71C5C5V" + Styliser.CODE_HEXCOLOUR + "FFFFFF,71C5C5V" + Styliser.CODE_HEXCOLOUR + "EB5405,71C5C5V" + Styliser.CODE_HEXCOLOUR + "FFFFFF,71C5C5V" + Styliser.CODE_HEXCOLOUR + "EB5405,71C5C5V" + Styliser.CODE_HEXCOLOUR + "FFFFFF,71C5C5V" + Styliser.CODE_HEXCOLOUR + "EB5405,71C5C5V" + Styliser.CODE_HEXCOLOUR + "FFFFFF,71C5C5V" + Styliser.CODE_HEXCOLOUR + "EB5405,71C5C5V");
     }
 
     /**
      * Check if the given user is known on the channel, and add them if they
      * are not.
      *
      * @param user User to check
      * @return true if user was already on the channel, false if they were added.
      */
     private boolean checkUserOnChannel(final TwitterUser user) {
         final TwitterChannelInfo channel = (TwitterChannelInfo) getChannel(mainChannelName);
 
         if (channel == null) {
             doDebug(Debug.stateError, "Tried to check user (" + user.getScreenName() + "), but channel is null.");
             return false;
         }
 
         if (channel.getChannelClient(user.getScreenName()) == null) {
             // User not found, perhaps a rename?
             for (final ChannelClientInfo cci : channel.getChannelClients()) {
                 final TwitterClientInfo ci = (TwitterClientInfo) cci.getClient();
                 if (ci.getUserID() == user.getID()) {
                     final String oldName = ci.getNickname();
                     ci.setUser(user);
                     renameClient(ci, oldName);
                 }
             }
 
             final TwitterClientInfo ci = new TwitterClientInfo(user.getScreenName(), this);
             clients.put(ci.getNickname().toLowerCase(), ci);
             final TwitterChannelClientInfo cci = new TwitterChannelClientInfo(channel, ci);
 
             channel.addChannelClient(cci);
             getCallbackManager().getCallbackType(ChannelJoinListener.class).call(channel, cci);
 
             return false;
         } else {
             return true;
         }
     }
 
     /**
      * Send a Debug Message using the parser debug api.
      *
      * @param code Debug Code for the message.
      * @param message Content of the message.
      */
     private void doDebug(final Debug code, final String message) {
         if (debugEnabled) {
             getCallbackManager().getCallbackType(DebugInfoListener.class).call(code.ordinal(), message);
         }
     }
 
     /**
      * Run the twitter parser.
      */
     @Override
     public void run() {
         resetState();
 
         if (myUsername.isEmpty()) {
             sendPrivateNotice("Unable to connect to " + myServerName + " without a username. Disconnecting.");
             getCallbackManager().getCallbackType(SocketCloseListener.class).call();
             return;
         }
 
         // Get the consumerKey and consumerSecret for this server if known
         // else default to our twitter key and secret
         final String consumerKey;
         final String consumerSecret;
         if (getConfigManager().hasOptionString(myPlugin.getDomain(), "consumerKey-" + myServerName)) {
             consumerKey = getConfigManager().getOption(myPlugin.getDomain(), "consumerKey-" + myServerName);
         } else {
             consumerKey = "qftK3mAbLfbWWHf8shiyjw";
         }
         if (getConfigManager().hasOptionString(myPlugin.getDomain(), "consumerSecret-" + myServerName)) {
             consumerSecret = getConfigManager().getOption(myPlugin.getDomain(), "consumerSecret-" + myServerName);
         } else {
             consumerSecret = "flPr2TJGp4795DeTu4VkUlNLX8g25SpXWXZ7SKW0Bg";
         }
 
         final String token;
         final String tokenSecret;
 
         if (getConfigManager().hasOptionString(myPlugin.getDomain(), "token-" + myServerName + "-" + myUsername)) {
             token = getConfigManager().getOption(myPlugin.getDomain(), "token-" + myServerName + "-" + myUsername);
         } else {
             token = "";
         }
         if (getConfigManager().hasOptionString(myPlugin.getDomain(), "tokenSecret-" + myServerName + "-" + myUsername)) {
             tokenSecret = getConfigManager().getOption(myPlugin.getDomain(), "tokenSecret-" + myServerName + "-" + myUsername);
         } else {
             tokenSecret = "";
         }
 
         api = new TwitterAPI(myUsername, myPassword, apiAddress, "", consumerKey, consumerSecret, token, tokenSecret, apiVersioning, apiVersion, getConfigManager().getOptionBool(myPlugin.getDomain(), "autoAt"));
         api.setSource("DMDirc");
         PARSERS.add(this);
         api.addErrorHandler(this);
         api.addRawHandler(this);
         api.setDebug(debugEnabled);
 
         getConfigManager().addChangeListener(myPlugin.getDomain(), this);
 
         connected = api.checkConnection();
         if (!connected) {
             sendPrivateNotice("Unable to connect to " + myServerName + ". Disconnecting.");
             getCallbackManager().getCallbackType(SocketCloseListener.class).call();
             return;
         }
 
         final TwitterChannelInfo channel = new TwitterChannelInfo(mainChannelName, this);
         synchronized (channels) {
             channels.put(mainChannelName, channel);
         }
         channel.addChannelClient(new TwitterChannelClientInfo(channel, myself));
 
         // Fake 001
         getCallbackManager().getCallbackType(ServerReadyListener.class).call();
         // Fake 005
         getCallbackManager().getCallbackType(NetworkDetectedListener.class).call(getNetworkName(), getServerSoftware(), getServerSoftwareType());
         // Fake MOTD
         getCallbackManager().getCallbackType(AuthNoticeListener.class).call("Welcome to " + myServerName + ".");
         getCallbackManager().getCallbackType(MotdStartListener.class).call("- " + myServerName + " Message of the Day -");
         getCallbackManager().getCallbackType(MotdLineListener.class).call("- This is an experimental parser, to allow DMDirc to use " + myServerName);
         getCallbackManager().getCallbackType(MotdLineListener.class).call("- ");
         getCallbackManager().getCallbackType(MotdLineListener.class).call("- Your timeline appears in " + mainChannelName + " (topic is your last status)");
         getCallbackManager().getCallbackType(MotdLineListener.class).call("- All messages sent to this channel (or topics set) will cause the status to be set.");
         getCallbackManager().getCallbackType(MotdLineListener.class).call("- ");
         getCallbackManager().getCallbackType(MotdLineListener.class).call("- Messages can be replied to using /msg &<messageid> <reply>");
         getCallbackManager().getCallbackType(MotdLineListener.class).call("- Messages can be retweeted by using /ctcp &<messageid> RT or /ctcp &<messageid> RETWEET");
         getCallbackManager().getCallbackType(MotdLineListener.class).call("- ");
         getCallbackManager().getCallbackType(MotdEndListener.class).call(false, "End of /MOTD command");
         // Fake some more on-connect crap
         getCallbackManager().getCallbackType(UserModeDiscoveryListener.class).call(myself, "");
 
         channel.setLocalTopic("No status known.");
         doJoinChannel(channel);
 
         sendChannelMessage(channel, "Checking to see if we have been authorised to use the account \"" + api.getLoginUsername() + "\"...");
 
         if (api.isAllowed(false)) {
             sendChannelMessage(channel, "DMDirc has been authorised to use the account \"" + api.getLoginUsername() + "\"");
             updateTwitterChannel();
         } else {
             wantAuth = true;
             if (api.useOAuth()) {
                 sendChannelMessage(channel, "Sorry, DMDirc has not been authorised to use the account \"" + api.getLoginUsername() + "\"");
                 sendChannelMessage(channel, "");
                 sendChannelMessage(channel, "Before you can use DMDirc with " + myServerName + " you need to authorise it.");
                 sendChannelMessage(channel, "");
                 sendChannelMessage(channel, "To do this, please visit: " + api.getOAuthURL());
                 sendChannelMessage(channel, "and then type the PIN here.");
             } else {
                 sendChannelMessage(channel, "Sorry, You did not provide DMDirc with a password for the account \"" + api.getLoginUsername() + "\" and the server \"" + myServerName + "\" does not support OAuth or is not accepting our key.");
                 sendChannelMessage(channel, "");
                 sendChannelMessage(channel, "Before you can use DMDirc with " + myServerName + " you need to provide a password.");
                 sendChannelMessage(channel, "");
                 sendChannelMessage(channel, "To do this, please type the password here, or set it correctly in the URL (twitter://" + myUsername + ":your_password@" + myServerName + myAddress.getPath() + ").");
             }
         }
 
         if (saveLastIDs) {
             if (getConfigManager().hasOptionString(myPlugin.getDomain(), "lastReplyId-" + myServerName + "-" + myUsername)) {
                 lastReplyId = TwitterAPI.parseLong(getConfigManager().getOption(myPlugin.getDomain(), "lastReplyId-" + myServerName + "-" + myUsername), -1);
             }
             if (getConfigManager().hasOptionString(myPlugin.getDomain(), "lastTimelineId-" + myServerName + "-" + myUsername)) {
                 lastTimelineId = TwitterAPI.parseLong(getConfigManager().getOption(myPlugin.getDomain(), "lastTimelineId-" + myServerName + "-" + myUsername), -1);
             }
             if (getConfigManager().hasOptionString(myPlugin.getDomain(), "lastDirectMessageId-" + myServerName + "-" + myUsername)) {
                 lastDirectMessageId = TwitterAPI.parseLong(getConfigManager().getOption(myPlugin.getDomain(), "lastDirectMessageId-" + myServerName + "-" + myUsername), -1);
             }
         }
 
         boolean first = true; // Used to let used know if there was no new items.
 
         int count = 0;
         while (connected) {
             final int startCalls = wantAuth ? 0 : api.getUsedCalls();
 
             // Get Updates
             if (!wantAuth) {
                 final boolean foundUpdates = getUpdates(channel);
                 if (first && !foundUpdates) {
                     sendChannelMessage(channel, "No new items found.");
                 }
                 first = false;
 
                 // Store last IDs
                 IdentityManager.getConfigIdentity().setOption(myPlugin.getDomain(), "lastReplyId-" + myServerName + "-" + myUsername, Long.toString(lastReplyId));
                 IdentityManager.getConfigIdentity().setOption(myPlugin.getDomain(), "lastTimelineId-" + myServerName + "-" + myUsername, Long.toString(lastTimelineId));
                 IdentityManager.getConfigIdentity().setOption(myPlugin.getDomain(), "lastDirectMessageId-" + myServerName + "-" + myUsername, Long.toString(lastDirectMessageId));
 
                 // Get Updates for search channels
                 for (final TwitterChannelInfo searchChannel : channels.values()) {
                     if (searchChannel.getName().startsWith("#")) {
                         getUpdates(searchChannel);
                     }
                 }
             }
 
             // Calculate number of calls remaining.
             final int endCalls = wantAuth ? 0 : api.getUsedCalls();
             final long[] apiCalls = wantAuth ? new long[]{0L, 0L, System.currentTimeMillis(), (long) api.getUsedCalls()} : api.getRemainingApiCalls();
             doDebug(Debug.apiCalls, "Twitter calls Remaining: " + apiCalls[0]);
             // laconica doesn't rate limit, so time to reset is always 0, in this case
             // we will assume the time of the next hour.
             final Calendar cal = Calendar.getInstance();
             cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), cal.get(Calendar.HOUR_OF_DAY) + 1, 0, 0);
 
             final long timeLeft = (apiCalls[2] > 0 ? apiCalls[2] : cal.getTimeInMillis()) - System.currentTimeMillis();
             final long sleepTime;
             if (wantAuth) {
                 // When waiting for auth, sleep for less time so that when the
                 // auth happens, we can quickly start twittering!
                 sleepTime = 5 * 1000;
             } else if (!api.isAllowed()) {
                 // If we aren't allowed, but aren't waiting for auth, then
                 // sleep for 1 minute.
                 sleepTime = 60 * 1000;
             } else if (apiCalls[1] == 0L) {
                 // Twitter has said we have no API Calls in total, so sleep for
                 // 10 minutes and try again.
                 // (This will also happen if twitter didn't respond for some reason)
                 sleepTime = 10 * 60 * 1000;
                 // Also alert the user.
                 twitterFail("Unable to communicate with twitter or no API calls allowed, retrying in 10 minutes.");
             } else if (api.getUsedCalls() > apicalls) {
                 // Sleep for the rest of the hour, we have done too much!
                 sleepTime = timeLeft;
             } else {
                 // Else work out how many calls we have left.
                 // Whichever is less between the number of calls we want to make
                 // and the number of calls twitter is going to allow us to make.
                 final long callsLeft = Math.min(apicalls - api.getUsedCalls(), apiCalls[0]);
                 // How many calls do we make each time?
                 // If this is less than 0 (If there was a time reset between
                 // calculating the start and end calls used) then assume 3.
                 final long callsPerTime = (endCalls - startCalls) > 0 ? endCalls - startCalls : 3;
 
                 doDebug(Debug.apiCalls, "\tCalls Remaining: " + callsLeft);
                 doDebug(Debug.apiCalls, "\tCalls per time: " + callsPerTime);
 
                 // And divide this by the number of calls we make each time to
                 // see how many times we have to sleep this hour.
                 final long sleepsRequired = callsLeft / callsPerTime;
 
                 doDebug(Debug.sleepTime, "\tSleeps Required: " + sleepsRequired);
                 doDebug(Debug.sleepTime, "\tTime Left: " + timeLeft);
 
                 // Then finally discover how long we need to sleep for.
                 sleepTime = (sleepsRequired > 0) ? timeLeft / sleepsRequired : timeLeft;
             }
 
             doDebug(Debug.sleepTime, "Sleeping for: " + sleepTime);
 
             // Sleep for sleep time,
             // If we have a negative sleep time, use 5 minutes.
             try {
                 Thread.sleep(sleepTime > 0 ? sleepTime : 5 * 60 * 1000);
             } catch (final InterruptedException ex) {
             }
 
             if (++count > PRUNE_COUNT) {
                 api.pruneStatusCache(System.currentTimeMillis() - PRUNE_TIME);
             }
         }
     }
 
     /**
      * Get updates from twitter.
      *
      * @param channel Channel that this is being used for.
      * @return True if any items are found, else false.
      */
     private boolean getUpdates(final TwitterChannelInfo channel) {
         boolean foundItems = false;
         if (!wantAuth && api.isAllowed()) {
             lastQueryTime = System.currentTimeMillis();
 
             if (channel.getName().startsWith("#")) {
                 long lastId = lastSearchIds.containsKey(channel) ? lastSearchIds.get(channel) : -1;
                 final List<TwitterStatus> statuses = api.getSearchResults(channel.getName(), lastId);
 
                 foundItems = !statuses.isEmpty();
                 for (final TwitterStatus status : statuses) {
                     final ChannelClientInfo cci = channel.getChannelClient(status.getUserName(), true);
                     sendChannelMessage(channel, new Date(status.getTime()), status.getText(), cci, status.getUserName());
                     lastId = Math.max(lastId, status.getID());
                 }
 
                 lastSearchIds.put(channel, lastId);
             } else {
                 final int statusesPerAttempt = Math.min(200, statusCount);
                 final List<TwitterStatus> statuses = new ArrayList<TwitterStatus>();
                 for (final TwitterStatus status : api.getReplies(lastReplyId, statusesPerAttempt)) {
                     statuses.add(status);
                     if (status.getRetweetId() > lastReplyId) {
                         lastReplyId = status.getRetweetId();
                     }
                 }
 
                 for (final TwitterStatus status : api.getFriendsTimeline(lastTimelineId, statusesPerAttempt)) {
                     if (!statuses.contains(status)) {
                         statuses.add(status);
                     }
                     // Add new friends that may have been added elsewhere.
                     if (status.isRetweet()) {
                         checkUserOnChannel(status.getRetweetUser());
                     } else {
                         checkUserOnChannel(status.getUser());
                     }
 
                     if (status.getRetweetId() > lastTimelineId) {
                         lastTimelineId = status.getRetweetId();
                     }
                 }
 
                 Collections.sort(statuses);
 
                 for (final TwitterStatus status : statuses) {
                     foundItems = true;
                     final ChannelClientInfo cci = channel.getChannelClient(status.getUser().getScreenName());
                     String message = String.format("%s    %c15 &%d", status.getText(), Styliser.CODE_COLOUR, status.getID());
                     if (status.getReplyTo() > 0) {
                         message += String.format(" %cin reply to &%d %1$c", Styliser.CODE_ITALIC, status.getReplyTo());
                     }
                     if (status.isRetweet()) {
                         message += String.format("    %c%c15[Retweet by %s]%1$c", Styliser.CODE_BOLD, Styliser.CODE_COLOUR, status.getRetweetUser().getScreenName());
                     }
 
                     final String hostname = status.getUser().getScreenName();
                     sendChannelMessage(channel, new Date(status.getTime()), message, cci, hostname);
                 }
 
                 final List<TwitterMessage> directMessages = new ArrayList<TwitterMessage>();
                 for (final TwitterMessage directMessage : api.getDirectMessages(lastDirectMessageId)) {
                     directMessages.add(directMessage);
                     if (directMessage.getID() > lastDirectMessageId) {
                         lastDirectMessageId = directMessage.getID();
                     }
                 }
 
                 if (getSentMessage) {
                     for (final TwitterMessage directMessage : api.getSentDirectMessages(lastDirectMessageId)) {
                         directMessages.add(directMessage);
                         if (directMessage.getID() > lastDirectMessageId) {
                             lastDirectMessageId = directMessage.getID();
                         }
                     }
                 }
                 Collections.sort(directMessages);
 
                 for (final TwitterMessage dm : directMessages) {
                     sendPrivateMessage(dm.getText(), dm.getSenderScreenName(), dm.getTargetScreenName());
                 }
 
                 if (myself != null && myself.getUser() != null) {
                     checkTopic(channel, myself.getUser().getStatus());
                 }
             }
         }
 
         return foundItems;
     }
 
     /**
      * Reset the state of the parser.
      */
     private void resetState() {
         resetState(false);
     }
 
     /**
      * Reset the state of the parser.
      *
      * @param simpleMyself Don't check the config when setting myself if true.
      */
     private void resetState(final boolean simpleMyself) {
         connected = false;
         synchronized (channels) {
             channels.clear();
         }
         clients.clear();
 
         if (!simpleMyself && autoAt) {
             myself = new TwitterClientInfo("@" + myUsername, this);
         } else {
             myself = new TwitterClientInfo(myUsername, this);
         }
         setCachedSettings();
     }
 
     /**
      * Get the Twitter API Object.
      *
      * @return The Twitter API Object
      */
     public TwitterAPI getApi() {
         return api;
     }
 
     /** {@inheritDoc} */
     @Override
     public StringConverter getStringConverter() {
         return myStringConverter;
     }
 
     /** {@inheritDoc} */
     @Override
     public void setIgnoreList(final IgnoreList ignoreList) {
         myIgnoreList = ignoreList;
     }
 
     /** {@inheritDoc} */
     @Override
     public IgnoreList getIgnoreList() {
         return myIgnoreList;
     }
 
     /**
      * Set the twitter status.
      *
      * @param message Status to use.
      * @return True if status was updated, else false.
      */
     public boolean setStatus(final String message) {
         return setStatus(message, -1);
     }
 
     /**
      * Set the twitter status.
      *
      * @param message Status to use.
      * @param replyToId The ID this status is in reply to, or -1
      * @return True if status was updated, else false.
      */
     private boolean setStatus(final String message, final long replyToId) {
         final StringBuffer newStatus = new StringBuffer(message);
         final TwitterChannelInfo channel = (TwitterChannelInfo) getChannel(mainChannelName);
 
         if (channel != null && replaceOpeningNickname) {
             final String[] bits = message.split(" ");
             if (bits[0].charAt(bits[0].length() - 1) == ':') {
                 final String name = bits[0].substring(0, bits[0].length() - 1);
 
                 final ChannelClientInfo cci = channel.getChannelClient(name);
                 if (cci != null) {
                     if (cci.getClient().getNickname().charAt(0) == '@') {
                         bits[0] = cci.getClient().getNickname();
                     } else {
                         bits[0] = "@" + cci.getClient().getNickname();
                     }
 
                     newStatus.setLength(0);
                     for (final String bit : bits) {
                         if (newStatus.length() > 0) {
                             newStatus.append(' ');
                         }
                         newStatus.append(bit);
                     }
                 }
             }
         }
 
         if (api.setStatus(newStatus.toString(), replyToId)) {
             checkTopic(channel, myself.getUser().getStatus());
             return true;
         }
 
         return false;
     }
 
     /**
      * Rename the given client from the given name.
      *
      * @param client Client to rename
      * @param old Old nickname
      */
     public void renameClient(final TwitterClientInfo client, final String old) {
         clients.remove(old.toLowerCase());
         clients.put(client.getNickname().toLowerCase(), client);
 
         getCallbackManager().getCallbackType(NickChangeListener.class).call(client, old);
         for (final ChannelClientInfo cci : client.getChannelClients()) {
             getCallbackManager().getCallbackType(ChannelNickChangeListener.class).call(client, cci.getChannel(), old);
         }
     }
 
     @Override
     public int getMaxLength() {
         return 140;
     }
 
     /**
      * Make the core think a channel was joined.
      *
      * @param channel Channel to join.
      */
     private void doJoinChannel(final ChannelInfo channel) {
         // Fake Join Channel
         getCallbackManager().getCallbackType(ChannelSelfJoinListener.class).call(channel);
         getCallbackManager().getCallbackType(ChannelTopicListener.class).call(channel, true);
         getCallbackManager().getCallbackType(ChannelNamesListener.class).call(channel);
         getCallbackManager().getCallbackType(ChannelModeChangeListener.class).call(channel, null, "", "");
     }
 
     /**
      * Update the users and topic of the main channel.
      */
     private void updateTwitterChannel() {
         final TwitterChannelInfo channel = (TwitterChannelInfo) getChannel(mainChannelName);
         checkTopic(channel, myself.getUser().getStatus());
 
         channel.clearChannelClients();
         channel.addChannelClient(new TwitterChannelClientInfo(channel, myself));
 
         for (final TwitterUser user : api.getFriends()) {
             final TwitterClientInfo ci = new TwitterClientInfo(user.getScreenName(), this);
             clients.put(ci.getNickname().toLowerCase(), ci);
             final TwitterChannelClientInfo cci = new TwitterChannelClientInfo(channel, ci);
 
             channel.addChannelClient(cci);
         }
         api.getFollowers();
         getCallbackManager().getCallbackType(ChannelNamesListener.class).call(channel);
     }
 
     /**
      * Check if the topic in the given channel has been changed, and if it has
      * fire the callback.
      *
      * @param channel channel to check.
      * @param status Status to use to update the topic with.
      */
     private void checkTopic(final TwitterChannelInfo channel, final TwitterStatus status) {
         if (channel == null || status == null) {
             return;
         }
         final String oldStatus = channel.getTopic();
         final String newStatus = (status.isRetweet()) ? status.getRetweetText() : status.getText();
 
         if (!newStatus.equalsIgnoreCase(oldStatus)) {
             channel.setTopicSetter(status.getUser().getScreenName());
             channel.setTopicTime(status.getTime() / 1000);
             channel.setLocalTopic(newStatus);
             getCallbackManager().getCallbackType(ChannelTopicListener.class).call(channel, false);
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void handleTwitterError(final TwitterAPI api, final Throwable t, final String source, final String twitterInput, final String twitterOutput, final String message) {
         final boolean showError = !debugEnabled && hide500Errors;
         if (showError && message.matches("^\\(50[0-9]\\).*")) {
             return;
         }
         try {
             if (message.isEmpty()) {
                 twitterFail("Recieved an error: " + source);
             } else if (debugEnabled) {
                 twitterFail("Recieved an error from twitter: " + message + (debugEnabled ? " [" + source + "]" : ""));
             }
             if (t != null) {
                 doDebug(Debug.twitterError, t.getClass().getSimpleName() + ": " + t + " -> " + t.getMessage());
             }
 
             // And give more information:
             doDebug(Debug.twitterErrorMore, "Source: " + source);
             doDebug(Debug.twitterErrorMore, "Input: " + twitterInput);
             doDebug(Debug.twitterErrorMore, "Output: ");
             for (final String out : twitterOutput.split("\n")) {
                 doDebug(Debug.twitterErrorMore, "                " + out);
             }
             doDebug(Debug.twitterErrorMore, "");
             doDebug(Debug.twitterErrorMore, "Exception:");
 
             final String[] trace = ErrorManager.getTrace(t);
             for (final String out : trace) {
                 doDebug(Debug.twitterErrorMore, "                " + out);
             }
 
             doDebug(Debug.twitterErrorMore, "==================================");
         } catch (final Exception t2) {
             doDebug(Debug.twitterError, "wtf? (See Console for stack trace) " + t2);
             t2.printStackTrace();
         }
     }
 
     /**
      * This method will send data to the NumericListener and the DataInListener
      *
      * @param numeric Numeric
      * @param token Tokenised Representation.
      */
     private void sendNumericOutput(final int numeric, final String[] token) {
         getCallbackManager().getCallbackType(NumericListener.class).call(numeric, token);
         final StringBuffer output = new StringBuffer();
         for (final String bit : token) {
             output.append(' ');
             output.append(bit);
         }
         getCallbackManager().getCallbackType(DataInListener.class).call(output.toString().trim());
     }
 
     /** {@inheritDoc} */
     @Override
     public void handleRawTwitterInput(final TwitterAPI api, final String data) {
         doDebug(Debug.dataIn, "-------------------------");
         getCallbackManager().getCallbackType(DataInListener.class).call("-------------------------");
         for (final String line : data.split("\n")) {
             doDebug(Debug.dataIn, line);
             getCallbackManager().getCallbackType(DataInListener.class).call(line);
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void handleRawTwitterOutput(final TwitterAPI api, final String data) {
         doDebug(Debug.dataOut, "-------------------------");
         getCallbackManager().getCallbackType(DataOutListener.class).call("-------------------------", true);
         for (final String line : data.split("\n")) {
             doDebug(Debug.dataOut, line);
             getCallbackManager().getCallbackType(DataOutListener.class).call(line, true);
         }
     }
 
     /**
      * Let the user know twitter failed in some way.
      *
      * @param message Message to send to the user
      */
     private void twitterFail(final String message) {
         if (Math.random() <= 0.10) {
             showFailWhale();
         }
         doDebug(Debug.twitterError, message);
         sendPrivateNotice(message);
     }
 
     /**
      * Returns the TwitterPlugin that owns us.
      *
      * @return The TwitterPlugin that owns us.
      */
     public TwitterPlugin getMyPlugin() {
         return myPlugin;
     }
 
     /** {@inheritDoc} */
     @Override
     public void configChanged(final String domain, final String key) {
         setCachedSettings();
         if (domain.equalsIgnoreCase(myPlugin.getDomain())) {
             if (key.equalsIgnoreCase("debugEnabled")) {
                 api.setDebug(debugEnabled);
             } else if (key.equalsIgnoreCase("autoAt")) {
                 sendPrivateNotice("'autoAt' setting was changed, reconnect needed.");
                 disconnect("'autoAt' setting was changed, reconnect needed.");
             }
         }
     }
 
     /**
      * Get the config manager for this parser instance.
      *
      * @return the ConfigManager for this parser.
      */
     protected ConfigManager getConfigManager() {
         if (myConfigManager == null) {
             myConfigManager = new ConfigManager(myAddress.getScheme(), getServerSoftwareType(), getNetworkName(), getServerName());
         }
 
         return myConfigManager;
     }
 
     /**
      * Get settings from config for cache variables.
      */
     private void setCachedSettings() {
         saveLastIDs = getConfigManager().getOptionBool(myPlugin.getDomain(), "saveLastIDs");
         statusCount = getConfigManager().getOptionInt(myPlugin.getDomain(), "statuscount");
         getSentMessage = getConfigManager().getOptionBool(myPlugin.getDomain(), "getSentMessages");
         apicalls = getConfigManager().getOptionInt(myPlugin.getDomain(), "apicalls");
         autoAt = getConfigManager().getOptionBool(myPlugin.getDomain(), "autoAt");
         replaceOpeningNickname = getConfigManager().getOptionBool(myPlugin.getDomain(), "replaceOpeningNickname");
         hide500Errors = getConfigManager().getOptionBool(myPlugin.getDomain(), "hide500Errors");
         debugEnabled = getConfigManager().getOptionBool(myPlugin.getDomain(), "debugEnabled");
         autoLeaveMessageChannel = getConfigManager().getOptionBool(myPlugin.getDomain(), "autoLeaveMessageChannel");
     }
 
     /** {@inheritDoc} */
     @Override
     public Collection<? extends ChannelJoinRequest> extractChannels(final URI uri) {
         return new ArrayList<ChannelJoinRequest>();
     }
 
     /** {@inheritDoc} */
     @Override
     public List<String> getServerInformationLines() {
         return Arrays.asList(new String[]{"Twitter IRC parser: " + getServerName()});
     }
 
     /** {@inheritDoc} */
     @Override
     public void setCompositionState(final String host, final CompositionState state) {
         // Do nothing
     }
 
     /** {@inheritDoc} */
     @Override
     public void requestGroupList(final String searchTerms) {
         // Do nothing
     }
 
 }
