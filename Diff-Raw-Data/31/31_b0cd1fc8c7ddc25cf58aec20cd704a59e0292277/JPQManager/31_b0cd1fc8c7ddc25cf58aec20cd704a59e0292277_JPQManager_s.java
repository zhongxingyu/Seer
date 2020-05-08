 /*
  * Copyright (c) 2006-2015 DMDirc Developers
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
 
 package com.dmdirc.addons.jpq;
 
 import com.dmdirc.DMDircMBassador;
 import com.dmdirc.config.prefs.PreferencesSetting;
 import com.dmdirc.config.prefs.PreferencesType;
 import com.dmdirc.events.ChannelSelfJoinEvent;
 import com.dmdirc.events.ChannelSelfPartEvent;
 import com.dmdirc.events.GroupChatPrefsRequestedEvent;
 import com.dmdirc.events.ServerConnectedEvent;
 import com.dmdirc.events.ServerDisconnectedEvent;
 import com.dmdirc.interfaces.Connection;
 import com.dmdirc.interfaces.ConnectionManager;
 import com.dmdirc.interfaces.GroupChat;
 import com.dmdirc.plugins.PluginDomain;
 
 import com.google.common.annotations.VisibleForTesting;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.inject.Inject;
 
 import net.engio.mbassy.listener.Handler;
 
 /**
  * Provides the ability to hide joins, parts and quits from a {@link GroupChat}.
  */
 public class JPQManager {
 
     private final String domain;
     private final ConnectionManager connectionManager;
     private final GroupChatHandlerFactory groupChatHandlerFactory;
     private final DMDircMBassador eventBus;
     private final Map<GroupChat, GroupChatHandler> groupChatHandlers;
 
     @Inject
     public JPQManager(
             @PluginDomain(JPQPlugin.class) final String domain,
             final ConnectionManager connectionManager,
             final GroupChatHandlerFactory groupChatHandlerFactory,
             final DMDircMBassador eventBus) {
         this.domain = domain;
         this.connectionManager = connectionManager;
         this.groupChatHandlerFactory = groupChatHandlerFactory;
         this.eventBus = eventBus;
         groupChatHandlers = new HashMap<>();
     }
 
     /**
      * Unloads this manager, removing required listeners and bindings.
      */
     public void load() {
         connectionManager.getConnections().forEach(this::addGroupChatHandler);
         eventBus.subscribe(this);
     }
 
     /**
      * Loads this manager, adding required listeners and bindings.
      */
     public void unload() {
         connectionManager.getConnections().forEach(this::removeGroupChatHandler);
         eventBus.unsubscribe(this);
     }
 
     @VisibleForTesting
     @Handler
     void handleConnectionAdded(final ServerConnectedEvent event) {
         addGroupChatHandler(event.getConnection());
     }
 
     @VisibleForTesting
     @Handler
     void handleConnectionRemoved(final ServerDisconnectedEvent event) {
         removeGroupChatHandler(event.getConnection());
     }
     
     @VisibleForTesting
     @Handler
     void handleGroupChatAdded(final ChannelSelfJoinEvent event) {
         addGroupChatHandler(event.getChannel());
     }
     
     @VisibleForTesting
     @Handler
     void handleGroupChatRemoved(final ChannelSelfPartEvent event) {
        addGroupChatHandler(event.getChannel());
     }
 
     @VisibleForTesting
     @Handler
     void handleGroupChatPrefs(final GroupChatPrefsRequestedEvent event) {
         event.getCategory().addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                 domain, "hidejpq", "Hide J/P/Q", "Hides joins, parts and quits in a group chat.",
                 event.getConfig(), event.getIdentity()));
     }
 
     private void addGroupChatHandler(final Connection connection) {
         connection.getGroupChatManager().getChannels().forEach(this::addGroupChatHandler);
     }
 
     private void addGroupChatHandler(final GroupChat groupChat) {
         groupChatHandlers.computeIfAbsent(groupChat, groupChatHandlerFactory::get);
     }
 
     private void removeGroupChatHandler(final Connection connection) {
         connection.getGroupChatManager().getChannels().forEach(this::removeGroupChatHandler);
     }
 
     private void removeGroupChatHandler(final GroupChat groupChat) {
         final GroupChatHandler groupChatHandler = groupChatHandlers.remove(groupChat);
         if (groupChatHandler != null) {
             groupChatHandler.unload();
         }
     }
 }
