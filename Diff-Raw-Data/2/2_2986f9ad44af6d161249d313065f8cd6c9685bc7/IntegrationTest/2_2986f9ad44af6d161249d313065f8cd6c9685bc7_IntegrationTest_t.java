 package com.fenrissoftwerks.loki.integtest;
 
 import junit.framework.TestCase;
 import org.jboss.netty.bootstrap.ClientBootstrap;
 import org.jboss.netty.buffer.BigEndianHeapChannelBuffer;
 import org.jboss.netty.buffer.ChannelBuffers;
 import org.jboss.netty.channel.*;
 import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
 import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
 import org.jboss.netty.handler.codec.http.HttpResponse;
 import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
 import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
 import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
 import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
 import org.jboss.netty.handler.codec.http.websocketx.WebSocketVersion;
 
 import java.net.InetSocketAddress;
 import java.net.URI;
 import java.util.concurrent.Executors;
 
 public class IntegrationTest extends TestCase {
 
     private static String INTEG_TEST_ENVIRONMENT_HOSTNAME = "localhost";
     private static Integer INTEG_TEST_ENVIRONMENT_PORT = 5000;
     private static Integer INTEG_TEST_ENVIRONMENT_WS_PORT = 5001;
 
     static {
         String hostnameFromEnv = System.getenv("LOKISERVER_INTEG_TEST_ENVIRONMENT_HOSTNAME");
         if(hostnameFromEnv != null && !"".equals(hostnameFromEnv)) {
             INTEG_TEST_ENVIRONMENT_HOSTNAME = hostnameFromEnv;
         }
 
         String portFromEnv = System.getenv("LOKISERVER_INTEG_TEST_ENVIRONMENT_PORT");
         if(portFromEnv != null && !"".equals(portFromEnv)) {
             try {
                 INTEG_TEST_ENVIRONMENT_PORT = Integer.valueOf(portFromEnv);
             } catch (NumberFormatException e) {
                 // Just use the default
             }
         }
 
         String wsPortFromEnv = System.getenv("LOKISERVER_INTEG_TEST_ENVIRONMENT_WS_PORT");
         if(wsPortFromEnv != null && !"".equals(wsPortFromEnv)) {
             try {
                 INTEG_TEST_ENVIRONMENT_WS_PORT = Integer.valueOf(portFromEnv);
             } catch (NumberFormatException e) {
                 // Just use the default
             }
         }
     }
 
     private static class IntegrationTestTextClientHandler extends SimpleChannelUpstreamHandler {
         public static String messageReceived;
 
         @Override
         public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
             // Record what we got back
             messageReceived = ((BigEndianHeapChannelBuffer)e.getMessage()).toString("UTF-8");
         }
     }
 
     private static class IntegrationTestWSClientHandler extends SimpleChannelUpstreamHandler {
         public static String messageReceived;
         private WebSocketClientHandshaker handshaker;
 
         private IntegrationTestWSClientHandler(WebSocketClientHandshaker handshaker) {
             this.handshaker = handshaker;
         }
 
         @Override
         public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
             // Record what we got back
             if(!handshaker.isHandshakeComplete()) {
                 handshaker.finishHandshake(ctx.getChannel(), (HttpResponse) e.getMessage());
             } else {
                 messageReceived = ((TextWebSocketFrame)e.getMessage()).getText();
             }
         }
     }
 
     public void testCanEchoOnNormalPort() throws Exception {
         ClientBootstrap bootstrap;
 
         // Configure the client.
         bootstrap = new ClientBootstrap(
                 new NioClientSocketChannelFactory(
                         Executors.newCachedThreadPool(),
                         Executors.newCachedThreadPool()));
 
         // Set up the pipeline factory.
         bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
             public ChannelPipeline getPipeline() throws Exception {
                 return Channels.pipeline(
                         new IntegrationTestTextClientHandler());
             }
         });
 
         ChannelFuture future = bootstrap.connect(new InetSocketAddress(INTEG_TEST_ENVIRONMENT_HOSTNAME,
                 INTEG_TEST_ENVIRONMENT_PORT));
         Channel channel = future.awaitUninterruptibly().getChannel();
         String messageStr = "{\"commandName\":\"echo\",\"commandArgs\":[\"sup\"]}\0";
         channel.write(ChannelBuffers.wrappedBuffer(messageStr.getBytes("UTF-8")));
         Thread.sleep(1000);
         assertEquals("{\"commandName\":\"ack\",\"commandArgs\":[\"sup\"]}\1", IntegrationTestTextClientHandler.messageReceived);
     }
 
     public void testCanEchoOnWSPort() throws Exception {
         ClientBootstrap bootstrap;
 
         final WebSocketClientHandshaker handshaker =
                             new WebSocketClientHandshakerFactory().newHandshaker(
                                     new URI("ws://" + INTEG_TEST_ENVIRONMENT_HOSTNAME),
                                     WebSocketVersion.V13, null, false, null);
         // Configure the client.
         bootstrap = new ClientBootstrap(
                 new NioClientSocketChannelFactory(
                         Executors.newCachedThreadPool(),
                         Executors.newCachedThreadPool()));
 
         // Set up the pipeline factory.
         bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
             public ChannelPipeline getPipeline() throws Exception {
                 return Channels.pipeline(
                         new HttpResponseDecoder(),
                         new HttpRequestEncoder(),
                         new IntegrationTestWSClientHandler(handshaker));
             }
         });
 
         ChannelFuture future = bootstrap.connect(new InetSocketAddress(INTEG_TEST_ENVIRONMENT_HOSTNAME,
                 INTEG_TEST_ENVIRONMENT_WS_PORT));
         future.syncUninterruptibly();
         Channel channel = future.getChannel();
         handshaker.handshake(channel).syncUninterruptibly();
 
         String messageStr = "{\"commandName\":\"echo\",\"commandArgs\":[\"sup\"]}";
         channel.write(new TextWebSocketFrame(messageStr));
         Thread.sleep(1000);
         assertEquals("{\"commandName\":\"ack\",\"commandArgs\":[\"sup\"]}\1", IntegrationTestWSClientHandler.messageReceived);
     }
 
     public void testPingGameObject() throws Exception {
         ClientBootstrap bootstrap;
         ClientBootstrap wsBootstrap;
 
         // Configure the normal client.
         bootstrap = new ClientBootstrap(
                 new NioClientSocketChannelFactory(
                         Executors.newCachedThreadPool(),
                         Executors.newCachedThreadPool()));
 
         // Set up the pipeline factory.
         bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
             public ChannelPipeline getPipeline() throws Exception {
                 return Channels.pipeline(
                         new IntegrationTestTextClientHandler());
             }
         });
 
         // Connect
         ChannelFuture future = bootstrap.connect(new InetSocketAddress(INTEG_TEST_ENVIRONMENT_HOSTNAME,
                 INTEG_TEST_ENVIRONMENT_PORT));
         Channel channel = future.awaitUninterruptibly().getChannel();
 
         final WebSocketClientHandshaker handshaker =
                             new WebSocketClientHandshakerFactory().newHandshaker(
                                     new URI("ws://" + INTEG_TEST_ENVIRONMENT_HOSTNAME),
                                     WebSocketVersion.V13, null, false, null);
         // Configure the client.
         wsBootstrap = new ClientBootstrap(
                 new NioClientSocketChannelFactory(
                         Executors.newCachedThreadPool(),
                         Executors.newCachedThreadPool()));
 
         // Set up the pipeline factory.
         wsBootstrap.setPipelineFactory(new ChannelPipelineFactory() {
             public ChannelPipeline getPipeline() throws Exception {
                 return Channels.pipeline(
                         new HttpResponseDecoder(),
                         new HttpRequestEncoder(),
                         new IntegrationTestWSClientHandler(handshaker));
             }
         });
 
         // Connect the WS
         ChannelFuture wsFuture = wsBootstrap.connect(new InetSocketAddress(INTEG_TEST_ENVIRONMENT_HOSTNAME,
                 INTEG_TEST_ENVIRONMENT_WS_PORT));
         wsFuture.syncUninterruptibly();
         Channel wsChannel = wsFuture.getChannel();
         handshaker.handshake(wsChannel).syncUninterruptibly();
 
         // OK, have both clients watch the same object
         String messageStr = "{\"commandName\":\"watchSomething\",\"commandArgs\":[\"sup\"]}\0";
         channel.write(ChannelBuffers.wrappedBuffer(messageStr.getBytes("UTF-8")));
         String wsMessageStr = "{\"commandName\":\"watchSomething\",\"commandArgs\":[\"sup\"]}";
         wsChannel.write(new TextWebSocketFrame(wsMessageStr));
 
         Thread.sleep(1000);
 
         // Now have one client ping the object
         messageStr = "{\"commandName\":\"pingSomething\",\"commandArgs\":[\"sup\"]}\0";
         channel.write(ChannelBuffers.wrappedBuffer(messageStr.getBytes("UTF-8")));
 
        Thread.sleep(5000);
 
         // Validate that both clients received the ping on the watched object
         assertEquals("{\"commandName\":\"pingResponse\",\"commandArgs\":[\"Hey we got a ping on: sup\"]}\1",
                 IntegrationTestTextClientHandler.messageReceived);
         assertEquals("{\"commandName\":\"pingResponse\",\"commandArgs\":[\"Hey we got a ping on: sup\"]}\1",
                 IntegrationTestWSClientHandler.messageReceived);
 
         // And the other client
         IntegrationTestTextClientHandler.messageReceived = null;
         IntegrationTestWSClientHandler.messageReceived = null;
         messageStr = "{\"commandName\":\"pingSomething\",\"commandArgs\":[\"sup\"]}";
         wsChannel.write(new TextWebSocketFrame(messageStr));
 
         Thread.sleep(1000);
 
         // Validate that both clients received the ping on the watched object
         assertEquals("{\"commandName\":\"pingResponse\",\"commandArgs\":[\"Hey we got a ping on: sup\"]}\1",
                 IntegrationTestTextClientHandler.messageReceived);
         assertEquals("{\"commandName\":\"pingResponse\",\"commandArgs\":[\"Hey we got a ping on: sup\"]}\1",
                 IntegrationTestWSClientHandler.messageReceived);
     }
 
 }
