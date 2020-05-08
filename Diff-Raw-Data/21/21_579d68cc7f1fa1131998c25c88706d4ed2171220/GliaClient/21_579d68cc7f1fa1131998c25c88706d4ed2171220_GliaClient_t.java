 package com.reversemind.glia.client;
 
 import com.esotericsoftware.kryo.Kryo;
 import com.reversemind.glia.GliaPayload;
 import com.reversemind.glia.GliaPayloadBuilder;
 import com.reversemind.glia.GliaPayloadStatus;
 import com.reversemind.glia.serialization.KryoObjectDecoder;
 import com.reversemind.glia.serialization.KryoObjectEncoder;
 import com.reversemind.glia.serialization.KryoSerializer;
 import com.reversemind.glia.serialization.KryoSettings;
 import org.jboss.netty.bootstrap.ClientBootstrap;
 import org.jboss.netty.channel.*;
 import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
 import org.jboss.netty.handler.codec.serialization.ClassResolvers;
 import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
 import org.jboss.netty.handler.codec.serialization.ObjectEncoder;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.net.InetSocketAddress;
 import java.util.*;
 import java.util.concurrent.*;
 
 /**
  * Copyright (c) 2013 Eugene Kalinin
  * <p/>
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * <p/>
  * http://www.apache.org/licenses/LICENSE-2.0
  * <p/>
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 public class GliaClient implements IGliaClient, Serializable {
 
     private final static Logger LOG = LoggerFactory.getLogger(GliaClient.class);
 
     private static final long SERVER_CONNECTION_TIMEOUT = 20000;    // 30 sec
     private static final long EXECUTOR_TIME_OUT = 60000;            // 1 min
     private static final long FUTURE_TASK_TIME_OUT = 30000;         // 30 sec
     private ExecutorService executor;
     private FutureTask<GliaPayload> futureTask;
 
     private GliaPayload gliaPayload;
     private boolean received = false;
 
     protected String host;
     protected int port;
 
     private ClientBootstrap clientBootstrap;
     private Channel channel;
     private ChannelFuture channelFuture;
     private ChannelFactory channelFactory;
     private long futureTaskTimeOut = FUTURE_TASK_TIME_OUT;
     private boolean running = false;
     private boolean occupied = false;
 
     private final Kryo kryo = new KryoSettings().getKryo();
     private KryoSerializer kryoSerializer;
 
     protected GliaClient() {
         this.port = 7000;
         this.host = "localhost";
         this.kryoSerializer = new KryoSerializer(kryo);
     }
 
     public GliaClient(String host, int port) {
         this.host = host;
         this.port = port;
         this.gliaPayload = null;
         this.executor = this.getExecutor();
         LOG.warn("\n\n GliaClient started \n for server:" + host + ":" + port + "\n\n");
         this.kryoSerializer = new KryoSerializer(kryo);
     }
 
     public GliaClient(String host, int port, long timeout) {
         this.host = host;
         this.port = port;
         this.gliaPayload = null;
 
         if (timeout > 0) {
             this.futureTaskTimeOut = timeout;
         } else {
             this.futureTaskTimeOut = FUTURE_TASK_TIME_OUT;
         }
 
         this.executor = this.getExecutor();
         LOG.warn("\n\n GliaClient started \n for server:" + host + ":" + port + "\n\n");
         this.kryoSerializer = new KryoSerializer(kryo);
     }
 
     /**
      * Set client timeout in ms - if zero or less than 0 - that assign a default value FUTURE_TASK_TIME_OUT = 30 sec
      *
      * @param timeOut
      */
     public void setClientTimeOut(long timeOut) {
         LOG.warn("Going to set client timeout for:" + timeOut + " ms");
         if (timeOut > 0) {
             this.futureTaskTimeOut = timeOut;
         } else {
             this.futureTaskTimeOut = FUTURE_TASK_TIME_OUT;
         }
     }
 
     public boolean isRunning() {
         return this.running;
     }
 
     @Override
     public int getPort() {
         return this.port;
     }
 
     @Override
     public String getHost() {
         return this.host;
     }
 
     /**
      * @param object
      */
     private void serverListener(Object object) {
         if (object instanceof GliaPayload) {
             LOG.info("SERVER LISTENER = arrived from server");
             this.gliaPayload = ((GliaPayload) object);
             if (this.futureTask != null) {
                 this.futureTask.cancel(true);
                 this.shutDownExecutor();
             }
             return;
         }
 
         LOG.info("Received object from is not a GliaPayload");
         this.futureTask.cancel(true);
         this.shutDownExecutor();
         this.setGliaPayload(null);
     }
 
     /**
      * Will wait maximum for a this.futureTaskTimeOut ms
      *
      * @return
      */
     public GliaPayload getGliaPayload() {
         Throwable _throwable = null;
 
         if (this.futureTask != null) {
             try {
                 this.setGliaPayload(this.futureTask.get(this.futureTaskTimeOut, TimeUnit.MILLISECONDS));
             } catch (TimeoutException e) {
                 LOG.warn("TimeoutException futureTask == HERE", e);
                 _throwable = new TimeoutException("TimeoutException futureTask == HERE");
                 this.futureTask.cancel(true);
             } catch (InterruptedException e) {
                 LOG.warn("InterruptedException futureTask == HERE", e);
             } catch (ExecutionException e) {
                 LOG.warn("ExecutionException futureTask == HERE", e);
             } catch (CancellationException ce) {
                 LOG.warn("Future task was Canceled - YES !!!");
             } catch (Exception e) {
                 LOG.warn("GENERAL Exception futureTask == HERE", e);
             }
         }
 
         this.occupied = false;
 
         if (this.gliaPayload != null) {
             return this.gliaPayload;
         }
 
         return GliaPayloadBuilder.buildErrorPayload(GliaPayloadStatus.ERROR_SERVER_TIMEOUT, _throwable);
     }
 
     @Override
     public boolean isOccupied() {
         return this.occupied;
     }
 
     private void setGliaPayload(GliaPayload inGliaPayload) {
         this.gliaPayload = inGliaPayload;
     }
 
     /**
      * Send to server GliaPayload
      *
      * @param gliaPayloadSend
      * @throws IOException
      * @see GliaPayload
      */
     public void send(GliaPayload gliaPayloadSend) throws IOException {
         this.setGliaPayload(null);
 
         // clean & start a timer
         if (this.channel != null && this.channel.isConnected()) {
             LOG.info("Connected:" + this.channel.isConnected());
 
             if (gliaPayloadSend != null) {
                 LOG.info("Send from GliaClient gliaPayload:" + gliaPayloadSend.toString());
                 gliaPayloadSend.setClientTimestamp(System.currentTimeMillis());
 
                 // client is occupied
                 this.occupied = true;
                if(this.kryoSerializer != null){
                    this.channel.write(this.kryoSerializer.serialize(gliaPayloadSend));
                }
 //                this.channel.write(gliaPayloadSend);
 
                 this.shutDownExecutor();
                 this.executor = this.getExecutor();
                 this.executor.execute(this.createFutureTask());
 
                 return;
             }
         }
         throw new IOException("Channel is closed or even not opened");
     }
 
     /**
      * Shutdown a client
      */
     @Override
     public void shutdown() {
         this.shutDownExecutor();
 
         try {
             if (this.channelFuture != null) {
                 this.channelFuture.getChannel().close().awaitUninterruptibly();
                 this.channelFactory.releaseExternalResources();
                 this.channelFactory.shutdown();
             }
 
             if (this.clientBootstrap != null) {
                 this.clientBootstrap.releaseExternalResources();
                 this.clientBootstrap.shutdown();
             }
         } catch (Exception ex) {
             LOG.warn("Could not to shutdown channelFuture && clientBootstrap");
         }
 
         this.channelFuture = null;
         this.clientBootstrap = null;
 
         this.running = false;
         this.occupied = false;
     }
 
     @Override
     public void restart() throws Exception {
         this.shutdown();
 
         this.gliaPayload = null;
         this.setClientTimeOut(FUTURE_TASK_TIME_OUT);
         this.executor = this.getExecutor();
         LOG.warn("\n\n GliaClient started \n for server:" + host + ":" + port + "\n\n");
 
         this.start();
     }
 
     @Override
     public void restart(String serverHost, int serverPort, long clientTimeOut) throws Exception {
 
         this.shutdown();
 
         this.host = serverHost;
         this.port = serverPort;
         this.gliaPayload = null;
 
         this.setClientTimeOut(clientTimeOut);
         this.executor = this.getExecutor();
         LOG.warn("\n\n GliaClient RE-started \n for server:" + host + ":" + port + "\n\n");
 
         this.start();
     }
 
     private void shutDownChannelFuture() {
         channelFuture.getCause().printStackTrace();
         channelFactory.releaseExternalResources();
         channelFuture = null;
         running = false;
     }
 
     private void shutDownChannelFuture(ChannelFuture channelFutureLocal) {
         channelFutureLocal.getCause().printStackTrace();
         channelFactory.releaseExternalResources();
         channelFutureLocal = null;
         running = false;
     }
 
     /**
      * Start a GliaClient
      * <p/>
      * for that case use
      *
      * @throws Exception
      */
     @Override
     public void start() throws Exception {
 
         if (this.running) {
             throw new InstantiationException("Glia client is running");
         }
 
         // Configure the client.
         // TODO make it more robust & speedy implements Kryo serializer
         this.channelFactory = new NioClientSocketChannelFactory(
                 // TODO implement other executors
                 Executors.newCachedThreadPool(),
                 Executors.newCachedThreadPool());
 
         this.clientBootstrap = new ClientBootstrap(channelFactory);
 
         ChannelPipelineFactory channelPipelineFactory = new ChannelPipelineFactory() {
             public ChannelPipeline getPipeline() throws Exception {
                 return Channels.pipeline(
                         new KryoObjectEncoder(),
                         new KryoObjectDecoder(ClassResolvers.cacheDisabled(getClass().getClassLoader()))
                         ,
                         new SimpleChannelUpstreamHandler() {
 
                             @Override
                             public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
                                 if (e instanceof ChannelStateEvent && ((ChannelStateEvent) e).getState() != ChannelState.INTEREST_OPS) {
                                     LOG.info(e.toString());
                                 }
                                 super.handleUpstream(ctx, e);
                             }
 
                             @Override
                             public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent event) {
                                 channel = event.getChannel();
                                 // Send the first message
                                 // channel.write(firstMessage);
                             }
 
                             @Override
                             public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
                                 // Get
                                 serverListener(e.getMessage());
                                 ctx.sendUpstream(e);
 
                                 //e.getChannel().write(e.getStatus());
                                 //e.getChannel().close();
                             }
 
                             @Override
                             public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
                                 LOG.warn("Unexpected exception from downstream.", e.getCause());
                                 e.getChannel().close();
                             }
                         }
                 );
             }
         };
 
         // Set up the pipeline factory.
         this.clientBootstrap.setPipelineFactory(channelPipelineFactory);
 
         // Start the connection attempt.
         // ChannelFuture channelFuture = this.clientBootstrap.connect(new InetSocketAddress(host, setPort));
         this.channelFuture = this.clientBootstrap.connect(new InetSocketAddress(host, port));
 
         // INFO
 
         // correct shut down a client
         // see http://netty.io/3.6/guide/#start.12 - 9.  Shutting Down Your Application
 
         // http://stackoverflow.com/questions/10911988/shutting-down-netty-server-when-client-connections-are-open
         //    Netty Server Shutdown
         //
         //    Close server channel
         //    Shutdown boss and worker executor
         //    Release server bootstrap resource
         //    Example code
         //
         //    ChannelFuture cf = serverChannel.close();
         //    cf.awaitUninterruptibly();
         //    bossExecutor.shutdown();
         //    workerExecutor.shutdown();
         //    thriftServer.releaseExternalResources();
 
 
         /*
 
 
             BEFORE version 1.8.0-SNAPSHOT
 
          */
         // !!! see also - http://massapi.com/class/cl/ClientBootstrap.html
 //        LOG.info("1");
         // just wait for server connection for 3sec.
 //        channelFuture.await(SERVER_CONNECTION_TIMEOUT);
 //        if (!channelFuture.isSuccess()) {
 //            channelFuture.getCause().printStackTrace();
 //            channelFactory.releaseExternalResources();
 //            this.running = false;
 //        }else{
 //            this.running = true;
 //        }
 
 
         // if need to disconnect right after server response
         //  channelFuture.getChannel().getCloseFuture().awaitUninterruptibly();
         //  channelFactory.releaseExternalResources();
 
 
 //        channelFuture.addListener(new ChannelFutureListener() {
 //            public void operationComplete(ChannelFuture future) throws Exception {
 //                if (future.isSuccess()) {
 //                    // Connection attempt succeeded:
 //                    // Begin to accept incoming traffic.
 //                    //inboundChannel.setReadable(true);
 //                    LOG.info("Client connected");
 //                    running = true;
 //                } else {
 //                    LOG.info("Failed connect to server");
 //                    // Close the connection if the connection attempt has failed.
 //                    //inboundChannel.close();
 //                    //shutDownChannelFuture();
 //                    LOG.info("Shutdown all");
 //                    shutDownChannelFuture(future);
 //                }
 //            }
 //        });
 
 //       if(channelFuture != null && !channelFuture.isSuccess()){
 //           shutDownChannelFuture();
 //       }
 
         /*
 
 
             AFTER VERSION 1.8.0-SNAPSHOT
 
 
          */
         // SOURCE !!! http://massapi.com/class/cl/ClientBootstrap.html
 
 
         // Start the connection attempt.
 //        channelFuture.awaitUninterruptibly();
 
 
         // Need to repair it
 
         boolean goAway = false;
         long countGoAway = 0;
         final long stepGoAway = 100; //ms
 
         LOG.warn("Warming up 1.8.9-SNAPSHOT ...");
         while (goAway == false | countGoAway < (SERVER_CONNECTION_TIMEOUT / stepGoAway)) {
 
             Thread.sleep(stepGoAway);
             if (!channelFuture.isSuccess()) {
                 this.running = false;
                 goAway = true;
             } else {
                 this.running = true;
                 goAway = true;
                 countGoAway = (SERVER_CONNECTION_TIMEOUT / stepGoAway) + 10;
             }
             countGoAway++;
             LOG.warn("Count down for connection tomeout:" + countGoAway * stepGoAway + ":ms future:" + channelFuture.isSuccess() + " running:" + this.running);
         }
 
         if (this.running == false) {
             LOG.warn("After ");
             channelFuture.getCause().printStackTrace();
             channelFactory.releaseExternalResources();
             channelFuture = null;
         }
 
 
 //        CountDownLatch latch;
 //
 //        LOG.warn("Warming up 1.8.0-SNAPSHOT ...");
 //        for (long i = 0; i < 10000; i++) {
 //            latch = new CountDownLatch(1);
 //            try {
 //                latch.await();
 //            } catch (InterruptedException e) {
 //                e.printStackTrace();
 //            }
 //            if(i % 1000 == 0){
 //                LOG.info("i:" + i);
 //            }
 //        }
 //
 //        Thread.sleep(100);
 //        LOG.warn("Warmed up 1.8.0-SNAPSHOT ");
 
     }
 
     /**
      * Creates a thread pool that creates new threads as needed, but
      * will reuse previously constructed threads when they are
      * available.
      * <p/>
      * For purpose of GliaClient - just enough a single thread
      * <p/>
      * These pools will typically improve the performance
      * of programs that execute many short-lived asynchronous tasks.
      * <p/>
      * Calls to <tt>execute</tt> will reuse previously constructed
      * threads if available. If no existing thread is available, a new
      * thread will be created and added to the pool.
      * <p/>
      * Threads that have
      * not been used for sixty seconds are terminated and removed from
      * the cache.
      * <p/>
      * Thus, a pool that remains idle for long enough will
      * not consume any resources. Note that pools with similar
      * properties but different details (for example, timeout parameters)
      * may be created using {@link ThreadPoolExecutor} constructors.
      *
      * @return ExecutorService
      * @see Executors
      */
     private ExecutorService getExecutor() {
         if (this.executor == null) {
             // Something like a @see Executors.newCachedThreadPool() but exactly the one thread
             return this.executor = new ThreadPoolExecutor(0, 1, EXECUTOR_TIME_OUT, TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>());
         }
 
         if (this.executor.isShutdown() | this.executor.isTerminated()) {
             this.executor = null;
             return this.executor = new ThreadPoolExecutor(0, 1, EXECUTOR_TIME_OUT, TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>());
         }
 
         return this.executor;
     }
 
     /**
      * Shutdown a waiting thread from server payload result
      */
     private void shutDownExecutor() {
         this.shutDownFutureTask();
         if (this.executor != null && !this.executor.isShutdown()) {
             try {
                 this.executor.shutdown();
             } catch (Exception ex) {
                 // TODO make it more accurate
                 ex.printStackTrace();
             }
             this.executor = null;
         }
     }
 
     /**
      *
      */
     private void shutDownFutureTask() {
         if (this.futureTask != null) {
             this.futureTask.cancel(true);
         }
         this.futureTask = null;
     }
 
     private FutureTask<GliaPayload> createFutureTask() {
         return this.futureTask = new FutureTask<GliaPayload>(new PayloadCallable(this.gliaPayload));
     }
 
     /**
      *
      */
     private class PayloadCallable implements Callable<GliaPayload> {
 
         private GliaPayload callablePayload;
 
         PayloadCallable(GliaPayload gliaPayload) {
             this.callablePayload = gliaPayload;
         }
 
         @Override
         public GliaPayload call() throws Exception {
             while (this.callablePayload == null) {
                 Thread.sleep(2);
             }
             return this.callablePayload;
         }
     }
 }
