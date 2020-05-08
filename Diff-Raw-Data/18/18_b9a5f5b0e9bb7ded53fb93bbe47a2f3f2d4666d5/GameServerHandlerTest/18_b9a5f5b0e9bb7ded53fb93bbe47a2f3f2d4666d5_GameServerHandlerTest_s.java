 package com.fenrissoftwerks.loki.gameserver;
 
 import com.fenrissoftwerks.loki.Command;
 import com.fenrissoftwerks.loki.GameEngine;
 import com.fenrissoftwerks.loki.gameserver.channelhandler.GameServerHandler;
 import com.google.gson.Gson;
 import junit.framework.TestCase;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.UpstreamMessageEvent;
 import org.junit.Test;
 import static org.easymock.classextension.EasyMock.*;
 
 
 public class GameServerHandlerTest extends TestCase {
 
    private class MockGameEngine extends GameEngine {
 
         private Command commandProcessed;
 
         public void processCommand(Command command, ChannelHandlerContext ctx) {
             commandProcessed = command;
         }
 
         public Command getCommandProcessed() {
             return commandProcessed;
         }
     }
 
     @Test
     public void testMessageReceived() throws Exception {
         MockGameEngine engine = new MockGameEngine();
         Gson gson = new Gson();
         GameServerHandler gsh = new GameServerHandler(GameServer.getInstance(), engine, gson);
         ChannelHandlerContext ctx = createNiceMock(ChannelHandlerContext.class);
         UpstreamMessageEvent e = createNiceMock(UpstreamMessageEvent.class);
         Command command = new Command("foo", new Object[0]);
         String commandAsJson = gson.toJson(command);
         expect(e.getMessage()).andStubReturn(commandAsJson);
         replay(e);
         gsh.messageReceived(ctx, e);
         verify(e);
         assertEquals(command, engine.getCommandProcessed());
     }
 }
