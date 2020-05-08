 package com.hopper.session;
 
 import com.hopper.GlobalConfiguration;
 import com.hopper.cache.CacheManager;
 import com.hopper.future.DefaultLatchFuture;
 import com.hopper.future.LatchFuture;
 import com.hopper.lifecycle.LifecycleException;
 import com.hopper.lifecycle.LifecycleProxy;
 import com.hopper.server.ComponentManager;
 import com.hopper.server.ComponentManagerFactory;
 import com.hopper.server.Endpoint;
 import com.hopper.stage.Stage;
 import com.hopper.stage.StageManager;
 import org.jboss.netty.bootstrap.ClientBootstrap;
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.buffer.ChannelBuffers;
 import org.jboss.netty.channel.*;
 import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
 import org.jboss.netty.handler.timeout.TimeoutException;
 import org.jboss.netty.util.internal.DeadLockProofWorker;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.net.ConnectException;
 import java.net.InetSocketAddress;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.TimeUnit;
 
 /**
  * {@link NettyConnection} is used to identify the connection between client and
  * server(NIO connection) with Netty component.
  *
  * @author chenguoqing
  */
 public class NettyConnection extends LifecycleProxy implements Connection {
     private static Logger logger = LoggerFactory.getLogger(NettyConnection.class);
     /**
      * ComponentManager reference
      */
     private final ComponentManager componentManager = ComponentManagerFactory.getComponentManager();
 
     /**
      * Inject the {@link com.hopper.GlobalConfiguration} instance
      */
     private final GlobalConfiguration config = componentManager.getGlobalConfiguration();
     /**
      * Singleton CacheManager instance
      */
     private final CacheManager cacheManager = componentManager.getCacheManager();
     /**
      * Stage manager
      */
     private final StageManager stageManager = componentManager.getStageManager();
 
     /**
      * Source endpoint
      */
     private Endpoint source;
     /**
      * Destination endpoint
      */
     private Endpoint dest;
     /**
      * The connected channel future
      */
     private Channel channel;
 
     private ClientBootstrap bootstrap;
 
     /**
      * Local associated session
      */
     private Session session;
 
     @Override
     protected void doStart() {
         connect();
     }
 
     @Override
     public String getInfo() {
         return "S2S outgoing connection";
     }
 
     @Override
     public void setSession(Session session) {
         this.session = session;
     }
 
     public void setSourceEndpoint(Endpoint source) {
         this.source = source;
     }
 
     @Override
     public Endpoint getSourceEndpoint() {
         return source;
     }
 
     public void setDestEndpoint(Endpoint dest) {
         this.dest = dest;
     }
 
     @Override
     public Endpoint getDestEndpoint() {
         return dest;
     }
 
     @Override
     public Channel getChannel() {
         return channel;
     }
 
     @Override
     public void connect() {
         // Configure the client.
         this.bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(stageManager.getThreadPool(Stage
                 .CLIENT_BOSS), stageManager.getThreadPool(Stage.CLIENT_WORKER)));
 
         // set customs pipeline factory
         bootstrap.setPipelineFactory(new SenderPipelineFactory());
 
         // Start the connection attempt.
         ChannelFuture future = bootstrap.connect(new InetSocketAddress(dest.address, dest.port));
 
         this.channel = future.getChannel();
 
         // If the method is invoked by non-io thread, it should wait until complete
         if (DeadLockProofWorker.PARENT.get() == null) {
             future.awaitUninterruptibly(config.getRpcTimeout(), TimeUnit.MILLISECONDS);
 
             Throwable t = future.getCause();
             throwRuntimeException(t);
 
         } else {
             // Otherwise, the new connection should not block the current IO thread
             future.addListener(new ChannelFutureListener() {
                 @Override
                 public void operationComplete(ChannelFuture future) throws Exception {
                     if (future.isSuccess()) {
                         logger.debug("Connected to server:{}", dest);
                     } else {
                         if (future.isDone()) {
                             logger.debug("Failed to connect to {}", dest, future.getCause());
                         }
                     }
                 }
             });
         }
     }
 
     @Override
     protected void doShutdown() {
         if (channel != null && bootstrap != null) {

             final Runnable shutdownTask = new ShutdownTask(channel, bootstrap);

            // If the invocation is within IO thread
            if (DeadLockProofWorker.PARENT.get() != null) {
                componentManager.getStageManager().getThreadPool(Stage.SHUTDOWN_OUTGOING_CONNECTION).execute(shutdownTask);

                // If the invocation from user codes
            } else {
                shutdownTask.run();
            }

             channel = null;
             bootstrap = null;
         }
     }
 
     @Override
     public void reconnect() {
         // if the connection is running, shutdown it
         if (getState() != LifecycleState.SHUTDOWN && getState() != LifecycleState.NEW) {
             // invoke the super shutdown and notifies all listener
             shutdown();
         }
 
         try {
             start();
         } catch (LifecycleException e) {
             throw new RuntimeException(e);
         }
 
     }
 
     @Override
     public void close() {
         shutdown();
     }
 
     @Override
     public boolean validate() {
         return channel != null && channel.isConnected();
     }
 
     @Override
     public boolean isSecure() {
         return false;
     }
 
     @Override
     public void sendOneway(final Message message) {
 
         if (!validate()) {
             throw new IllegalStateException("Channel is not open or has been closed.");
         }
 
         ChannelFuture channelFuture = this.channel.write(message);
 
         channelFuture.addListener(new ChannelFutureListener() {
             @Override
             public void operationComplete(ChannelFuture callbackFuture) throws Exception {
                 if (callbackFuture.getCause() != null) {
                     logger.error("Failed to send message {} to {}.", new Object[]{message, dest,
                             callbackFuture.getCause()});
                 }
             }
         });
     }
 
     @Override
     public void sendOnewayUntilComplete(Message message) {
         if (!validate()) {
             throw new IllegalStateException("Channel is not open or has been closed.");
         }
 
         ChannelFuture channelFuture = this.channel.write(message);
 
         // waiting for complete
         channelFuture.awaitUninterruptibly(config.getRpcTimeout(), TimeUnit.MILLISECONDS);
 
         if (!channelFuture.isDone()) {
             throw new TimeoutException();
         }
 
         throwRuntimeException(channelFuture.getCause());
     }
 
     @Override
     public LatchFuture<Message> send(final Message message) {
 
         if (!validate()) {
             throw new IllegalStateException("Channel is not open or has been closed.");
         }
 
         ChannelFuture channelFuture = channel.write(message);
 
         DefaultLatchFuture<Message> future = new DefaultLatchFuture<Message>();
 
         cacheManager.put(message.getId(), future, config.getRpcTimeout());
 
         channelFuture.addListener(new ChannelFutureListener() {
 
             @Override
             public void operationComplete(ChannelFuture callbackFuture) throws Exception {
                 // if exception occurs when sending message,set the exception to MesageFuture for quick unlocking the
                 // blocking threads on MessageFuture
                 if (callbackFuture.getCause() != null) {
 
                     // if sending message failure, remove the future from cache
                     DefaultLatchFuture cacheFuture = cacheManager.remove(message.getId());
 
                     if (cacheFuture != null) {
                         // set the exception for unlocking the threads waiting
                         cacheFuture.setException(callbackFuture.getCause());
                     }
                 }
             }
         });
 
         return future;
     }
 
     private static class SenderPipelineFactory implements ChannelPipelineFactory {
         @Override
         public ChannelPipeline getPipeline() throws Exception {
             // Create a default pipeline implementation.
             ChannelPipeline pipeline = Channels.pipeline();
 
             // put command decoder
             pipeline.addLast("encoder", new MessageEncoder());
 //            pipeline.addLast("exception", new ExceptionHandler());
             return pipeline;
         }
     }
 
     private static class ExceptionHandler extends SimpleChannelHandler {
         @Override
         public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
 
             Throwable t = e.getCause();
 
             if (t instanceof ConnectException) {
                 logger.error("Failed to connect..", e.getCause());
             } else {
                 logger.error("Unknown exception occurred.", e);
             }
         }
     }
 
     private static class MessageEncoder extends SimpleChannelDownstreamHandler {
         @Override
         public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
 
             if (e.getMessage() instanceof Message) {
                 Message message = (Message) e.getMessage();
                 ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(message.serialize());
                 e.getChannel().write(buffer);
             } else {
                 ctx.sendDownstream(e);
             }
         }
     }
 
     private void throwRuntimeException(Throwable t) {
 
         if (t == null) {
             return;
         }
 
         if (t instanceof ExecutionException) {
             t = t.getCause();
         }
 
         if (t instanceof RuntimeException) {
             throw (RuntimeException) t;
         }
 
         throw new RuntimeException(t);
     }
 
     /**
      * Shutdown current connection and release resources with a non-worker thread
      */
     private class ShutdownTask implements Runnable {
         final Channel channel;
         final ClientBootstrap bootstrap;
 
         private ShutdownTask(Channel channel, ClientBootstrap bootstrap) {
             this.channel = channel;
             this.bootstrap = bootstrap;
         }
 
         @Override
         public void run() {
 
             // Wait until the connection is closed or the connection attempt fails.
             channel.close().awaitUninterruptibly();
 
             // Shut down thread pools to exit.
             bootstrap.releaseExternalResources();
         }
     }
 }
