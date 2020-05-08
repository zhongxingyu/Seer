 // Copyright 2011 Nathaniel Harward
 //
 // This file is part of commons-j.
 //
 // commons-j is free software: you can redistribute it and/or modify
 // it under the terms of the GNU General Public License as published by
 // the Free Software Foundation, either version 3 of the License, or
 // (at your option) any later version.
 //
 // commons-j is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 // GNU General Public License for more details.
 //
 // You should have received a copy of the GNU General Public License
 // along with commons-j. If not, see <http://www.gnu.org/licenses/>.
 
 package us.harward.commons.net.pubsub;
 
 import java.net.InetSocketAddress;
 import java.net.SocketAddress;
 import java.nio.ByteBuffer;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 
 import org.jboss.netty.bootstrap.ClientBootstrap;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelFactory;
 import org.jboss.netty.channel.ChannelPipeline;
 import org.jboss.netty.channel.ChannelPipelineFactory;
 import org.jboss.netty.channel.Channels;
 import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
 
 import com.google.common.base.Preconditions;
 
 public final class PubSubClient {
 
     /**
      * Application callback interface, subscribers to a topic must implement this interface to receive messages.
      */
     public static interface MessageCallback {
 
         void onMessage(final ByteBuffer message) throws Exception;
 
     }
 
     /**
      * Network connection lifecycle callback, applications can optionally implement this interface to take action(s) when a network
      * connection goes up/down.
      */
     public static interface NetworkConnectionLifecycleCallback {
 
         void connectionUp(final SocketAddress endpoint);
 
         void connectionDown(final SocketAddress endpoint);
 
     }
 
     private final ClientMessageHandler       clientHandler;
     private final RoundRobinReconnectHandler reconnectHandler;
 
     private final ChannelFactory             factory;
     private final ClientBootstrap            bootstrap;
 
     public PubSubClient(final ExecutorService service, final int retryDelay, final TimeUnit retryUnits,
             final InetSocketAddress... servers) {
         this(service, null, retryDelay, retryUnits, servers);
     }
 
     public PubSubClient(final ExecutorService service, final NetworkConnectionLifecycleCallback lifecycleCallback,
             final int retryDelay, final TimeUnit retryUnits, final InetSocketAddress... servers) {
         Preconditions.checkNotNull(service, "ExecutorService cannot be null");
         Preconditions.checkNotNull(servers, "Must give at least one server address to connect to");
         clientHandler = new ClientMessageHandler(service);
         factory = new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
         bootstrap = new ClientBootstrap(factory);
         reconnectHandler = new RoundRobinReconnectHandler(bootstrap, retryDelay, retryUnits, lifecycleCallback, servers);
         bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
 
             @Override
             public ChannelPipeline getPipeline() {
                 return Channels.pipeline(reconnectHandler, MessageCodec.decoder(), MessageCodec.encoder(), clientHandler);
             }
 
         });
         bootstrap.setOption("tcpNoDelay", true);
         bootstrap.setOption("keepAlive", true);
     }
 
     public void start() {
         reconnectHandler.enable();
     }
 
     public void stop() throws InterruptedException {
         reconnectHandler.disable();
         reconnectHandler.shutdown();
         factory.releaseExternalResources();
     }
 
     void subscribe(final String topic, final MessageCallback... callbacks) {
         clientHandler.subscribe(topic, callbacks);
     }
 
     void unsubscribe(final String topic, final MessageCallback... callbacks) {
         clientHandler.unsubscribe(topic, callbacks);
     }
 
     Future<Boolean> publish(final byte[] message, final String topic) {
         return publish(ByteBuffer.wrap(message), topic);
     }
 
     Future<Boolean> publish(final byte[] message, final int offset, final int length, final String topic) {
         return publish(ByteBuffer.wrap(message, offset, length), topic);
     }
 
     Future<Boolean> publish(final ByteBuffer message, final String topic) {
         Preconditions.checkNotNull(message, "Message can be empty but not null");
         Preconditions.checkNotNull(topic, "Topic can be empty but not null");
         final Channel channel = reconnectHandler.channel();
         return channel != null ? new NettyToJDKFuture(channel.write(new ApplicationMessage(message, topic)))
                 : NettyToJDKFuture.WRITE_FAILED;
     }
 
 }
