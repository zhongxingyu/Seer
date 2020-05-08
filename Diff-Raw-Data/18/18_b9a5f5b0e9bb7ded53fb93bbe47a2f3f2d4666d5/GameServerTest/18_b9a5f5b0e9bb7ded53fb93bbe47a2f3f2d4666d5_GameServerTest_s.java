 package com.fenrissoftwerks.loki.gameserver;
 
 import com.fenrissoftwerks.loki.Command;
 import com.google.gson.Gson;
 import junit.framework.TestCase;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelFuture;
 import org.junit.Test;
 
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.List;
 
 import static org.easymock.EasyMock.*;
 
 public class GameServerTest extends TestCase {
 
     @Test
     public void testWatchObject() throws Exception {
         Channel channel = createNiceMock(Channel.class);
         GameServer gs = GameServer.getInstance();
         Object indexObj = new Object();
         gs.addWatcherForObject(indexObj, channel);
         List<Channel> fetchChannels = gs.getWatchersForObject(indexObj);
         assertNotNull(fetchChannels);
         assertEquals(1, fetchChannels.size());
         assertTrue(fetchChannels.contains(channel));
     }
 
     @Test
     public void testSendCommandToWatchers() throws Exception {
         Channel channel = createNiceMock(Channel.class);
         InetSocketAddress addr = InetSocketAddress.createUnresolved("foo", 5001);
 
         GameServer gs = GameServer.getInstance();
         Object indexObj = new Object();
         gs.addWatcherForObject(indexObj, channel);
         List<Object> list = new ArrayList<Object>();
         list.add(indexObj);
         Command command = new Command();
         command.setCommandName("foo");
         Gson gson = new Gson();
         String commandAsJson = gson.toJson(command);
         expect(channel.getLocalAddress()).andStubReturn(addr);
         expect(channel.write(commandAsJson)).andStubReturn(createNiceMock(ChannelFuture.class));
         replay(channel);
         gs.sendCommandToWatchers(command, indexObj);
         verify(channel);
     }
 
 }
