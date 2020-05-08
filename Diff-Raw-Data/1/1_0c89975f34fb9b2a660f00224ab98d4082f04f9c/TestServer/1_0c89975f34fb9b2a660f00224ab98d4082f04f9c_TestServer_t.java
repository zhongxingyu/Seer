 package org.blockout.network.server;
 
 import java.net.InetSocketAddress;
 import java.util.HashSet;
 
 import org.blockout.network.channel.IChannel;
 import org.blockout.network.server.IConnectionListener;
 import org.blockout.network.server.Server;
 import org.jboss.netty.bootstrap.ServerBootstrap;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ChannelPipeline;
 import org.jboss.netty.channel.ChannelPipelineFactory;
 import org.jboss.netty.channel.ChannelStateEvent;
 import org.jboss.netty.channel.SimpleChannelHandler;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Mockito.*;
 
 public class TestServer {
 	protected ServerBootstrap bootstrap; 
 	protected Server server;
 	protected ChannelPipeline pipeline; 
 	
 	@Before
 	public void setUp(){
 		this.bootstrap = mock(ServerBootstrap.class);
 		this.pipeline = mock(ChannelPipeline.class);
 		when(this.bootstrap.getPipeline()).thenReturn(pipeline);
 		
 		this.server = new Server(bootstrap);
 	}
 	
 	@Test
 	public void startupTest(){
 		verify(this.bootstrap).setPipelineFactory(any(ChannelPipelineFactory.class));
 		verify(this.bootstrap).bind(any(InetSocketAddress.class));
 	}
 	
 	@Test
 	public void addHandlerTest(){
 		SimpleChannelHandler handler1 = mock(SimpleChannelHandler.class);
 		SimpleChannelHandler handler2 = mock(SimpleChannelHandler.class);		
 		
 		
 		this.server.addHandler(handler1);
 		this.server.addHandler(handler2);
 		
 		
 		verify(this.bootstrap).getPipeline();
 		verify(pipeline).addBefore("newConnection", "additionalHandler0", handler1);
 		verify(pipeline).addBefore("newConnection", "additionalHandler1", handler2);
 		
 	}
 	
 	@Test
 	public void addListenerTest(){
 		IConnectionListener listener = mock(IConnectionListener.class);
 		this.server.addListener(listener);
 		
 		assertEquals(1, this.server.listeners.size());
 		assertTrue(this.server.listeners.contains(listener));
 		
 	}
 	
 	@Test
 	public void addListenersTest(){
 		IConnectionListener listener1 = mock(IConnectionListener.class);
 		IConnectionListener listener2 = mock(IConnectionListener.class);
 		HashSet<IConnectionListener> set = new HashSet<IConnectionListener>();
 		set.add(listener1);
 		set.add(listener2);
 		
 		this.server.addListener(set);
 		
 		assertEquals(2, this.server.listeners.size());
 		assertTrue(this.server.listeners.contains(listener1));
 		assertTrue(this.server.listeners.contains(listener2));
 	}
 	
 	@Test
 	public void notifyListenersTest() throws Exception{
 		ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
 		ChannelStateEvent e = mock(ChannelStateEvent.class);
 		IConnectionListener listener = mock(IConnectionListener.class);
 		ArgumentCaptor<IChannel> arg = ArgumentCaptor.forClass(IChannel.class);
 		Channel inner_channel = mock(Channel.class);
 		
 		when(ctx.getChannel()).thenReturn(inner_channel);
		when(inner_channel.getPipeline()).thenReturn(pipeline);
 		
 		
 		this.server.addListener(listener);
 		this.server.newConnectionHandler.channelConnected(ctx, e);
 		
 		
 		verify(listener).notify(arg.capture());
 		assertEquals(inner_channel, arg.getValue().getChannel());
 	} 
 }
