 /*
  * Copyright (c) 2006-2013 DMDirc Developers
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
 
 package com.dmdirc.addons.relaybot;
 
 import com.dmdirc.Channel;
 import com.dmdirc.Server;
 import com.dmdirc.actions.ActionManager;
 import com.dmdirc.actions.CoreActionType;
 import com.dmdirc.addons.ui_swing.UIUtilities;
 import com.dmdirc.config.IdentityManager;
 import com.dmdirc.config.prefs.PluginPreferencesCategory;
 import com.dmdirc.config.prefs.PreferencesCategory;
 import com.dmdirc.config.prefs.PreferencesDialogModel;
 import com.dmdirc.config.prefs.PreferencesSetting;
 import com.dmdirc.config.prefs.PreferencesType;
 import com.dmdirc.interfaces.ActionListener;
 import com.dmdirc.interfaces.ConfigChangeListener;
 import com.dmdirc.interfaces.actions.ActionType;
 import com.dmdirc.parser.interfaces.ChannelClientInfo;
 import com.dmdirc.parser.interfaces.Parser;
 import com.dmdirc.parser.irc.IRCParser;
 import com.dmdirc.plugins.implementations.BasePlugin;
 import com.dmdirc.plugins.PluginInfo;
 import com.dmdirc.plugins.PluginManager;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.Callable;
 
 /**
  * This plugin makes certain relay bots less obnoxious looking.
  */
 public class RelayBotPlugin extends BasePlugin implements ActionListener, ConfigChangeListener {
 
     /** Known RelayChannelHandlers. */
    private final Map<Channel, RelayChannelHandler> handlers = new HashMap<Channel, RelayChannelHandler>();
     /** This plugin's plugin info. */
     private final PluginInfo pluginInfo;
 
     /**
      * Creates a new instance of this plugin.
      *
      * @param pluginInfo This plugin's plugin info
      */
    public RelayBotPlugin(final PluginInfo pluginInfo) {
        super();
         this.pluginInfo = pluginInfo;
     }
 
     /** {@inheritDoc} */
     @Override
     public void onLoad() {
         ActionManager.getActionManager().registerListener(this,
                 CoreActionType.CHANNEL_OPENED);
         ActionManager.getActionManager().registerListener(this,
                 CoreActionType.CHANNEL_CLOSED);
         ActionManager.getActionManager().registerListener(this,
                 CoreActionType.SERVER_CONNECTED);
         ActionManager.getActionManager().registerListener(this,
                 CoreActionType.SERVER_DISCONNECTED);
         ActionManager.getActionManager().registerListener(this,
                 CoreActionType.CHANNEL_QUIT);
         IdentityManager.getIdentityManager().getGlobalConfiguration()
                 .addChangeListener(getDomain(), this);
 
         // Add ourself to all currently known channels that we should be
         // connected with.
        for (Server server : getPluginManager().getMain().getServerManager().getServers()) {
             final Parser parser = server.getParser();
             if (parser instanceof IRCParser && !(parser.getCallbackManager() instanceof RelayCallbackManager)) {
                 new RelayCallbackManager(this, (IRCParser) parser);
             }
             for (String channel : server.getChannels()) {
                 getHandler(server.getChannel(channel));
             }
         }
     }
 
     /**
      * Get our plugin manager.
      *
      * @return Our plugin manager.
      */
     protected PluginManager getPluginManager() {
         return pluginInfo.getMetaData().getManager();
     }
 
     /** {@inheritDoc} */
     @Override
     public void onUnload() {
         ActionManager.getActionManager().unregisterListener(this);
 
         // Remove RelayCallbackManagers
        for (Server server : getPluginManager().getMain().getServerManager().getServers()) {
             final Parser parser = server.getParser();
             if (parser instanceof IRCParser && parser.getCallbackManager() instanceof RelayCallbackManager) {
                 ((RelayCallbackManager) parser.getCallbackManager()).onSocketClosed(parser, new Date());
             }
         }
 
         // Remove from all channels.
         synchronized (handlers) {
            for (RelayChannelHandler handler : new ArrayList<RelayChannelHandler>(handlers.values())) {
                 handler.restoreCoreChannelHandler();
             }
             handlers.clear();
         }
     }
 
     /**
      * Process an event of the specified type.
      *
      * @param type The type of the event to process
      * @param format Format of messages that are about to be sent. (May be null)
      * @param arguments The arguments for the event
      */
     @Override
     public void processEvent(final ActionType type, final StringBuffer format, final Object... arguments) {
         if (type == CoreActionType.CHANNEL_OPENED) {
             getHandler((Channel) arguments[0]);
         } else if (type == CoreActionType.CHANNEL_CLOSED) {
             removeHandler((Channel) arguments[0]);
         } else if (type == CoreActionType.CHANNEL_QUIT) {
             final Channel chan = (Channel) arguments[0];
             final Parser parser = chan.getServer().getParser();
             final ChannelClientInfo cci = (ChannelClientInfo) arguments[1];
             final String channelName = parser.getStringConverter().toLowerCase(chan.getName());
 
             if (IdentityManager.getIdentityManager().getGlobalConfiguration().hasOptionString(getDomain(), channelName)) {
                 final String botName = IdentityManager.getIdentityManager().getGlobalConfiguration().getOption(getDomain(), channelName);
                 if (parser.getStringConverter().equalsIgnoreCase(botName, cci.getClient().getNickname())) {
                     // The bot quit :(
                     final RelayChannelHandler handler = getHandler(chan);
                     if (handler != null) {
                         handler.updateNames();
                     }
                 }
             }
         } else if (type == CoreActionType.SERVER_CONNECTED) {
             final Server server = (Server) arguments[0];
 
             final Parser parser = server.getParser();
             if (parser instanceof IRCParser && !(parser.getCallbackManager() instanceof RelayCallbackManager)) {
                 new RelayCallbackManager(this, (IRCParser) parser);
             }
         } else if (type == CoreActionType.SERVER_DISCONNECTED) {
             final Server server = (Server) arguments[0];
 
             // RelayCallbackManager will revert itself when this happens.
 
             // Unset any listeners for channels of this server
             for (String channel : server.getChannels()) {
                 removeHandler(server.getChannel(channel));
             }
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void configChanged(final String domain, final String key) {
         final boolean wasUnset = !IdentityManager.getIdentityManager().getGlobalConfiguration().hasOptionString(domain, key);
 
        for (Server server : getPluginManager().getMain().getServerManager().getServers()) {
             if (server.hasChannel(key)) {
                 final Channel chan = server.getChannel(key);
                 if (wasUnset) {
                     removeHandler(chan);
                 } else {
                     getHandler(chan);
                 }
             }
         }
     }
 
     /**
      * Do we have an intercept for the given channel object?
      * If we have one, we will return true.
      * If we should have one (as determined by checking the config) we will add
      * one and return true.
      *
      * @param channel Channel to check
      * @return true or false
      */
     public boolean isListening(final Channel channel) {
         return (getHandler(channel) != null);
     }
 
     /**
      * Get the RelayChannelHandler for a given Channel.
      * If we have one, we will return it.
      * If we should have one (as determined by checking the config) we will
      * create and return it.
      * Otherwise we will return null.
      *
      * @param channel Channel to get Handler for.
      * @return ChannelHandler or null
      */
     public RelayChannelHandler getHandler(final Channel channel) {
         synchronized (handlers) {
             if (handlers.containsKey(channel)) {
                 return handlers.get(channel);
             } else {
                 final String channelName = channel.getServer().getParser().getStringConverter().toLowerCase(channel.getName());
                 if (IdentityManager.getIdentityManager().getGlobalConfiguration().hasOptionString(getDomain(), channelName)) {
                     final RelayChannelHandler handler = new RelayChannelHandler(this, channel);
                     handlers.put(channel, handler);
                     return handler;
                 }
             }
         }
 
         return null;
     }
 
     /**
      * Remove the RelayChannelHandler for a given Channel.
      * If we have one already, we will return it and remove it.
      * Otherwise we will return null.
      *
      * @param channel Channel to remove Handler for.
      * @return Handler that we removed, or null.
      */
     public RelayChannelHandler removeHandler(final Channel channel) {
         synchronized (handlers) {
             if (handlers.containsKey(channel)) {
                 final RelayChannelHandler handler = handlers.get(channel);
                 handler.unset();
                 handlers.remove(channel);
 
                 return handler;
             }
         }
 
         return null;
     }
 
     /**
      * Reads the relay bot channel data from the config.
      *
      * @return A multi-dimensional array of channel data.
      */
     public String[][] getData() {
         final Map<String, String> settings
                 = IdentityManager.getIdentityManager().getGlobalConfiguration()
                 .getOptions(getDomain());
         int i = 0;
         for (Map.Entry<String, String> entry : settings.entrySet()) {
             if (entry.getKey().charAt(0) == '#') {
                 i++;
             }
         }
         final String[][] data = new String[i][2];
         i = 0;
         for (Map.Entry<String, String> entry : settings.entrySet()) {
             if (entry.getKey().charAt(0) == '#') {
                 data[i] = new String[]{entry.getKey(), entry.getValue(), };
                 i++;
             }
         }
         return data;
     }
 
     /** {@inheritDoc} */
     @Override
     public void showConfig(final PreferencesDialogModel manager) {
         final PreferencesCategory general = new PluginPreferencesCategory(
                 pluginInfo, "Relay Bot",
                 "General configuration for the Relay bot plugin.");
 
         final PreferencesCategory colours = new PluginPreferencesCategory(
                 pluginInfo, "Channels",
                 "Identifies where and who the bot is in channels.",
                 UIUtilities.invokeAndWait(
                 new Callable<RelayChannelPanel>() {
 
             /** {@inheritDoc} */
             @Override
             public RelayChannelPanel call() {
                 return new RelayChannelPanel(getPluginManager()
                         .getPluginInfoByName("ui_swing").getPlugin(),
                         RelayBotPlugin.this);
             }
         }));
         colours.setInline().setInlineAfter();
         general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                 getDomain(), "joinOnDiscover", "Join on discover",
                 "Do you want fake clients to join the channel?",
                 manager.getConfigManager(), manager.getIdentity()));
         general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                 getDomain(), "colourFullName", "Colour full name",
                 "Do you want to colour the full name?",
                 manager.getConfigManager(), manager.getIdentity()));
         manager.getCategory("Plugins").addSubCategory(general);
         general.addSubCategory(colours);
     }
 }
