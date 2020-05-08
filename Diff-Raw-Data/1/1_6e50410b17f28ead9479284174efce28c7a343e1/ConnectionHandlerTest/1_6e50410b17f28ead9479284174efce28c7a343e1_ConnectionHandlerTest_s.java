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
 import com.dmdirc.config.ConfigBinder;
 import com.dmdirc.events.ChannelUserAwayEvent;
 import com.dmdirc.events.DisplayProperty;
 import com.dmdirc.events.ServerNumericEvent;
 import com.dmdirc.interfaces.Connection;
 import com.dmdirc.interfaces.ConnectionManager;
 import com.dmdirc.interfaces.GroupChat;
 import com.dmdirc.interfaces.GroupChatManager;
 import com.dmdirc.interfaces.GroupChatUser;
 import com.dmdirc.interfaces.User;
 import com.dmdirc.interfaces.WindowModel;
 import com.dmdirc.interfaces.config.AggregateConfigProvider;
 
 import com.google.common.collect.Lists;
 
 import java.util.Optional;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Captor;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.anyLong;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.never;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 @RunWith(MockitoJUnitRunner.class)
 public class ConnectionHandlerTest {
 
     @Mock private AggregateConfigProvider config;
     @Mock private ConfigBinder configBinder;
     @Mock private WindowModel windowModel;
     @Mock private DMDircMBassador eventBus;
     @Mock private ScheduledExecutorService scheduledExecutorService;
     @Mock private ScheduledFuture scheduledFuture;
     @Mock private ConnectionManager connectionManager;
     @Mock private Connection connection;
     @Mock private GroupChat groupChat;
     @Mock private GroupChatUser groupChatUser;
     @Mock private User user;
     @Mock private GroupChatManager groupChatManager;
     @Mock private ServerNumericEvent serverNumericEvent;
     @Mock private ChannelUserAwayEvent channelUserAwayEvent;
     @Captor private ArgumentCaptor<ChannelUserAwayEvent> eventArgumentCaptor;
     private ConnectionHandler instance;
 
     @Before
     public void setUp() throws Exception {
         when(scheduledExecutorService.scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(),
                 any())).thenReturn(scheduledFuture);
         when(windowModel.getEventBus()).thenReturn(eventBus);
         when(connection.getWindowModel()).thenReturn(windowModel);
         when(config.getBinder()).thenReturn(configBinder);
         when(connectionManager.getConnections()).thenReturn(Lists.newArrayList(connection));
         when(connection.getGroupChatManager()).thenReturn(groupChatManager);
         when(groupChatManager.getChannels()).thenReturn(Lists.newArrayList(groupChat));
         when(groupChat.getWindowModel()).thenReturn(windowModel);
         when(groupChat.getConnection()).thenReturn(Optional.of(connection));
         when(configBinder.withDefaultDomain("domain")).thenReturn(configBinder);
         when(windowModel.getConfigManager()).thenReturn(config);
         when(groupChatUser.getNickname()).thenReturn("nickname");
         when(channelUserAwayEvent.getUser()).thenReturn(groupChatUser);
         when(channelUserAwayEvent.getChannel()).thenReturn(groupChat);
         when(groupChatUser.getUser()).thenReturn(user);
         instance = new ConnectionHandler(config, scheduledExecutorService, connectionManager,
                 "domain", connection);
         instance.handleWhoInterval(5);
     }
 
     @Test
     public void testLoad() throws Exception {
         instance.load();
         verify(configBinder).bind(instance, ConnectionHandler.class);
         verify(eventBus).subscribe(instance);
         verify(scheduledExecutorService).scheduleAtFixedRate(any(Runnable.class), eq(5l), eq(5l),
                 eq(TimeUnit.MILLISECONDS));
     }
 
     @Test
     public void testUnload() throws Exception {
         instance.unload();
         verify(configBinder).unbind(instance);
         verify(scheduledExecutorService).shutdown();
         verify(eventBus).unsubscribe(instance);
     }
 
     @Test
     public void testHandleWhoInterval() throws Exception {
         instance.handleWhoInterval(10);
         verify(scheduledFuture).cancel(false);
         verify(scheduledExecutorService).scheduleAtFixedRate(any(Runnable.class), eq(5l), eq(5l),
                 eq(TimeUnit.MILLISECONDS));
         verify(scheduledExecutorService).scheduleAtFixedRate(any(Runnable.class), eq(10l), eq(10l),
                 eq(TimeUnit.MILLISECONDS));
     }
 
     @Test
     public void testCheckWho_True() throws Exception {
         when(config.getOptionBool("domain", "sendwho")).thenReturn(true);
         instance.checkWho();
         verify(config).getOptionBool("domain", "sendwho");
         verify(groupChat).requestUsersInfo();
     }
 
     @Test
     public void testCheckWho_False() throws Exception {
         when(config.getOptionBool("domain", "sendwho")).thenReturn(false);
         instance.checkWho();
         verify(config).getOptionBool("domain", "sendwho");
         verify(groupChat, never()).requestUsersInfo();
     }
 
     @Test
     public void testHandleAwayEvent_WithReason() throws Exception {
         when(channelUserAwayEvent.getReason()).thenReturn(Optional.ofNullable("reason"));
         instance.load();
         instance.handleAwayEvent(channelUserAwayEvent);
         verify(channelUserAwayEvent, never())
                 .setDisplayProperty(DisplayProperty.DO_NOT_DISPLAY, true);
         verify(connection, never()).requestUserInfo(any());
     }
 
     @Test
     public void testHandleAwayEvent_WithoutReason() throws Exception {
         when(channelUserAwayEvent.getReason()).thenReturn(Optional.empty());
         instance.load();
         instance.handleAwayEvent(channelUserAwayEvent);
         verify(channelUserAwayEvent).setDisplayProperty(DisplayProperty.DO_NOT_DISPLAY, true);
         verify(connection).requestUserInfo(any());
     }
 
     @Test
     public void testHandleServerNumericEvent_301() throws Exception {
         when(serverNumericEvent.getNumeric()).thenReturn(301);
         when(serverNumericEvent.getArgs()).thenReturn(
                 new String[]{"", "", "", "nickname", "reason"});
         instance.load();
         when(channelUserAwayEvent.getReason()).thenReturn(Optional.empty());
         when(groupChatUser.getGroupChat()).thenReturn(groupChat);
         instance.handleAwayEvent(channelUserAwayEvent);
         instance.handleServerNumericEvent(serverNumericEvent);
         verify(eventBus).publishAsync(eventArgumentCaptor.capture());
         assertEquals("nickname", eventArgumentCaptor.getValue().getUser().getNickname());
         assertEquals("reason", eventArgumentCaptor.getValue().getReason().get());
     }
 
     @Test
     public void testHandleServerNumericEvent_101() throws Exception {
         when(serverNumericEvent.getNumeric()).thenReturn(101);
         when(channelUserAwayEvent.getReason()).thenReturn(Optional.empty());
         instance.load();
         instance.handleAwayEvent(channelUserAwayEvent);
         instance.handleServerNumericEvent(serverNumericEvent);
         verify(serverNumericEvent, never()).getArgs();
         verify(eventBus, never()).publishAsync(any());
     }
 }
