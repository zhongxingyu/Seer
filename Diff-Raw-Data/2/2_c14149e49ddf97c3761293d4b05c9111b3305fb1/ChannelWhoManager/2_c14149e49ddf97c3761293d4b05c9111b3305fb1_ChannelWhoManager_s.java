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
 
 package com.dmdirc.addons.channelwho;
 
 import com.dmdirc.DMDircMBassador;
 import com.dmdirc.config.prefs.PreferencesCategory;
 import com.dmdirc.config.prefs.PreferencesSetting;
 import com.dmdirc.config.prefs.PreferencesType;
 import com.dmdirc.events.ClientPrefsOpenedEvent;
 import com.dmdirc.events.GroupChatPrefsRequestedEvent;
 import com.dmdirc.events.ServerConnectingEvent;
 import com.dmdirc.events.ServerDisconnectedEvent;
 import com.dmdirc.interfaces.Connection;
 import com.dmdirc.interfaces.ConnectionManager;
 import com.dmdirc.plugins.PluginDomain;
 import com.dmdirc.util.validators.NumericalValidator;
 
 import com.google.common.annotations.VisibleForTesting;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.inject.Inject;
 
 import net.engio.mbassy.listener.Handler;
 
 /**
  * Provides channel who support in DMDirc.
  */
 public class ChannelWhoManager {
 
     private final String domain;
     private final ConnectionHandlerFactory connectionHandlerFactory;
     private final ConnectionManager connectionManager;
     private final DMDircMBassador eventBus;
     private final Map<Connection, ConnectionHandler> connectionHandlers;
 
     @Inject
     public ChannelWhoManager(
             @PluginDomain(ChannelWhoPlugin.class) final String domain,
             final ConnectionHandlerFactory connectionHandlerFactory,
             final ConnectionManager connectionManager,
             final DMDircMBassador eventBus) {
         this.domain = domain;
         this.connectionHandlerFactory = connectionHandlerFactory;
         this.connectionManager = connectionManager;
         this.eventBus = eventBus;
         connectionHandlers = new HashMap<>();
     }
 
     public void load() {
         eventBus.subscribe(this);
         connectionManager.getConnections().forEach(this::addConnectionHandler);
     }
 
     public void unload() {
         connectionManager.getConnections().forEach(this::removeConnectionHandler);
         eventBus.unsubscribe(this);
     }
 
     private void addConnectionHandler(final Connection connection) {
         connectionHandlers.computeIfAbsent(connection, connectionHandlerFactory::get);
     }
 
     private void removeConnectionHandler(final Connection connection) {
         final ConnectionHandler connectionHandler = connectionHandlers.remove(connection);
         if (connectionHandler != null) {
             connectionHandler.unload();
         }
     }
 
     @VisibleForTesting
     @Handler
     void handleGroupChatPrefsRequestedEvent(final GroupChatPrefsRequestedEvent event) {
         event.getCategory().addSetting(new PreferencesSetting(PreferencesType.BOOLEAN, domain,
                 "sendWho", "Send Who Requests", "Should we send who requests to the channel?",
                 event.getConfig(), event.getIdentity()));
     }
 
     @VisibleForTesting
     @Handler
     void handlePrefsDialog(final ClientPrefsOpenedEvent event) {
         final PreferencesCategory category = new PreferencesCategory("Channel Who", "Provides " +
                "support for sendinw WHO requests to channels at regular intervals");
         category.addSetting(new PreferencesSetting(PreferencesType.DURATION,
                 new NumericalValidator(0, Integer.MAX_VALUE), domain, "whointerval",
                 "Who Interval", "The interval WHO requests will be sent to channels",
                 event.getModel().getConfigManager(), event.getModel().getIdentity()));
     }
 
     @VisibleForTesting
     @Handler
     void handleServerConnectingEvent(final ServerConnectingEvent event) {
         addConnectionHandler(event.getConnection());
     }
 
     @VisibleForTesting
     @Handler
     void handleServerDisconnectedEvent(final ServerDisconnectedEvent event) {
         removeConnectionHandler(event.getConnection());
     }
 
 }
