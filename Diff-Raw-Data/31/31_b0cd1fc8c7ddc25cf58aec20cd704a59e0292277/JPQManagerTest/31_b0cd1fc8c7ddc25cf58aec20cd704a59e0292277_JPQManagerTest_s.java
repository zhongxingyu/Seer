 package com.dmdirc.addons.jpq;
 
 import com.dmdirc.DMDircMBassador;
 import com.dmdirc.config.prefs.PreferencesCategory;
 import com.dmdirc.config.prefs.PreferencesSetting;
 import com.dmdirc.config.prefs.PreferencesType;
 import com.dmdirc.events.GroupChatPrefsRequestedEvent;
 import com.dmdirc.events.ServerConnectedEvent;
 import com.dmdirc.events.ServerDisconnectedEvent;
 import com.dmdirc.interfaces.Connection;
 import com.dmdirc.interfaces.ConnectionManager;
 import com.dmdirc.interfaces.GroupChat;
 import com.dmdirc.interfaces.GroupChatManager;
 import com.dmdirc.interfaces.config.AggregateConfigProvider;
 
 import com.google.common.collect.Lists;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Captor;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 @RunWith(MockitoJUnitRunner.class)
 public class JPQManagerTest {
 
     @Mock private DMDircMBassador eventBus;
     @Mock private ConnectionManager connectionManager;
     @Mock private GroupChatHandlerFactory groupChatHandlerFactory;
     @Mock private ServerConnectedEvent serverConnectedEvent;
     @Mock private ServerDisconnectedEvent serverDisconnectedEvent;
     @Mock private GroupChatPrefsRequestedEvent groupChatPrefsRequestedEvent;
     @Mock private PreferencesCategory prefsCategory;
     @Mock private AggregateConfigProvider configProvider;
     @Captor private ArgumentCaptor<PreferencesSetting> preferencesSetting;
     @Mock private Connection connection1;
     @Mock private Connection connection2;
     @Mock private GroupChat groupChat1;
     @Mock private GroupChat groupChat2;
     @Mock private GroupChat groupChat3;
     @Mock private GroupChat groupChat4;
     @Mock private GroupChatHandler groupChatHandler1;
     @Mock private GroupChatHandler groupChatHandler2;
     @Mock private GroupChatHandler groupChatHandler3;
     @Mock private GroupChatHandler groupChatHandler4;
     @Mock private GroupChatManager groupChatManager1;
     @Mock private GroupChatManager groupChatManager2;
     private JPQManager instance;
 
     @Before
     public void setUp() throws Exception {
         when(groupChatHandlerFactory.get(groupChat1)).thenReturn(groupChatHandler1);
         when(groupChatHandlerFactory.get(groupChat2)).thenReturn(groupChatHandler2);
         when(groupChatHandlerFactory.get(groupChat3)).thenReturn(groupChatHandler3);
         when(groupChatHandlerFactory.get(groupChat4)).thenReturn(groupChatHandler4);
         when(serverConnectedEvent.getConnection()).thenReturn(connection1);
         when(serverDisconnectedEvent.getConnection()).thenReturn(connection1);
         when(connection1.getGroupChatManager()).thenReturn(groupChatManager1);
         when(connection2.getGroupChatManager()).thenReturn(groupChatManager2);
         when(groupChatManager1.getChannels())
                 .thenReturn(Lists.newArrayList(groupChat1, groupChat2));
         when(groupChatManager2.getChannels())
                 .thenReturn(Lists.newArrayList(groupChat3, groupChat4));
         when(connectionManager.getConnections())
                 .thenReturn(Lists.newArrayList(connection1, connection2));
         when(groupChatPrefsRequestedEvent.getCategory()).thenReturn(prefsCategory);
         when(groupChatPrefsRequestedEvent.getConfig()).thenReturn(configProvider);
         when(configProvider.getOption(anyString(), anyString())).thenReturn("true");
         instance = new JPQManager("domain", connectionManager, groupChatHandlerFactory, eventBus);
     }
 
     @Test
     public void testLoad() throws Exception {
         instance.load();
         verify(groupChatHandlerFactory).get(groupChat1);
         verify(groupChatHandlerFactory).get(groupChat2);
         verify(groupChatHandlerFactory).get(groupChat3);
         verify(groupChatHandlerFactory).get(groupChat4);
         verify(eventBus).subscribe(instance);
     }
 
     @Test
     public void testUnload() throws Exception {
         instance.load();
         instance.unload();
         verify(groupChatHandler1).unload();
         verify(groupChatHandler2).unload();
         verify(groupChatHandler3).unload();
         verify(groupChatHandler4).unload();
         verify(eventBus).unsubscribe(instance);
     }
 
     @Test
     public void testHandlePrefsEvent() throws Exception {
         instance.handleGroupChatPrefs(groupChatPrefsRequestedEvent);
         verify(prefsCategory).addSetting(preferencesSetting.capture());
         assertEquals(PreferencesType.BOOLEAN, preferencesSetting.getValue().getType());
         assertEquals("true", preferencesSetting.getValue().getValue());
     }
 
     @Test
     public void testHandleConnectionAdded() throws Exception {
         instance.handleConnectionAdded(serverConnectedEvent);
         verify(groupChatHandlerFactory).get(groupChat1);
         verify(groupChatHandlerFactory).get(groupChat2);
     }
 
     @Test
     public void testHandleConnectionRemoved() throws Exception {
         instance.handleConnectionAdded(serverConnectedEvent);
         instance.handleConnectionRemoved(serverDisconnectedEvent);
         verify(groupChatHandler1).unload();
         verify(groupChatHandler2).unload();
     }
 }
