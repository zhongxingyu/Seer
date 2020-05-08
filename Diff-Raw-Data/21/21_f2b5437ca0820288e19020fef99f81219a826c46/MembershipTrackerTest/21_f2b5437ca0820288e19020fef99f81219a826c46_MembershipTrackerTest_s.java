 /**
  *    Copyright 2012 meltmedia
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package com.meltmedia.cadmium.core.messaging;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Mockito.*;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Vector;
 
 import org.jgroups.Address;
 import org.jgroups.stack.IpAddress;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.meltmedia.cadmium.core.commands.SyncRequest;
 import com.meltmedia.cadmium.core.config.ConfigManager;
 import com.meltmedia.cadmium.core.messaging.jgroups.DummyJChannel;
 import com.meltmedia.cadmium.core.messaging.jgroups.JGroupsMessageSender;
 
 public class MembershipTrackerTest {
 
   ConfigManager configManager;
   
   @Before
   public void setupConfigManager() throws Exception {
     configManager = mock(ConfigManager.class);      
 
     when(configManager.getDefaultProperties()).thenReturn(new Properties());
   }
   
   @Test
   public void testNewMember() throws Exception {
     Address me = new IpAddress(12345);
     Address other = new IpAddress(54321);
     Address other2 = new IpAddress(345234);
     Address other3 = new IpAddress(36554);
     Vector<Address> viewMems = new Vector<Address>();
     viewMems.add(other2);
     viewMems.add(me);
     viewMems.add(other3);
     
     DummyJChannel channel = new DummyJChannel(me, viewMems);
     
     List<ChannelMember> members = new ArrayList<ChannelMember>();
     ChannelMember oldMember = new ChannelMember(other, true, false);
     members.add(oldMember);
     
     
     JGroupsMessageSender sender = new JGroupsMessageSender();
     MessageConverter converter = new MessageConverter();
     Map<String, Class<?>> commandToBodyMap = new HashMap<String, Class<?>>();
     commandToBodyMap.put(ProtocolMessage.CURRENT_STATE, Void.class);
     commandToBodyMap.put(ProtocolMessage.SYNC, SyncRequest.class);
     converter.setCommandToBodyMapping(commandToBodyMap);
     sender.setConverter(converter);
     
     sender.setChannel(channel);
     
    new MembershipTracker(sender, channel, members, configManager, null, null);
     
     Thread.sleep(1000l);
     
     List<org.jgroups.Message> msgs = channel.getMessageList();
     
     assertEquals("Member not added", 3, members.size());
     
     assertEquals("Member1 address is wrong", other2.toString(), members.get(0).getAddress().toString());
     assertTrue("Member1 not coordinator", members.get(0).isCoordinator());
     assertTrue("Member1 not me", !members.get(0).isMine());
     
     assertEquals("Member2 address is wrong", me.toString(), members.get(1).getAddress().toString());
     assertTrue("Member2 not coordinator", !members.get(1).isCoordinator());
     assertTrue("Member2 not me", members.get(1).isMine());
 
     assertEquals("Member3 address is wrong", other3.toString(), members.get(2).getAddress().toString());
     assertTrue("Member3 not coordinator", !members.get(2).isCoordinator());
     assertTrue("Member3 not me", !members.get(2).isMine());
     
     assertEquals("Wrong number of messages sent", 4, msgs.size());
     
     assertEquals("Wrong msg1 dest address", other2.toString(), msgs.get(0).getDest().toString());
     assertTrue("Wrong msg1 msg", new String(msgs.get(0).getBuffer(), "UTF-8").contains(ProtocolMessage.CURRENT_STATE));
 
     assertEquals("Wrong msg2 dest address", me.toString(), msgs.get(1).getDest().toString());
     assertTrue("Wrong msg2 msg", new String(msgs.get(1).getBuffer(), "UTF-8").contains(ProtocolMessage.CURRENT_STATE));
 
     assertEquals("Wrong msg2 dest address", other3.toString(), msgs.get(2).getDest().toString());
     assertTrue("Wrong msg2 msg", new String(msgs.get(2).getBuffer(), "UTF-8").contains(ProtocolMessage.CURRENT_STATE));
     
     assertEquals("Wrong msg3 dest address", other2.toString(), msgs.get(3).getDest().toString());
     assertTrue("Wrong msg3 msg", new String(msgs.get(3).getBuffer(), "UTF-8").contains(ProtocolMessage.SYNC));
   }
 }
