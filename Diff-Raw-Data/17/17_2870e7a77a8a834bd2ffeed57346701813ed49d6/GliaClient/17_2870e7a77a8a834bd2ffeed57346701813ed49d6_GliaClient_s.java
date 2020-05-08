 package com.reversemind.glia.client;
 
 import com.reversemind.glia.GliaPayloadBuilder;
 import com.reversemind.glia.GliaPayloadStatus;
 import org.jboss.netty.bootstrap.ClientBootstrap;
 import org.jboss.netty.channel.*;
 import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
 import org.jboss.netty.handler.codec.serialization.ClassResolvers;
 import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
 import org.jboss.netty.handler.codec.serialization.ObjectEncoder;
 import com.reversemind.glia.GliaPayload;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.net.InetSocketAddress;
 import java.util.concurrent.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Date: 4/24/13
  * Time: 10:08 AM
  *
  * @author konilovsky
  * @since 1.0
  */
 public class GliaClient implements IGliaClient, Serializable {
 
     // TODO replace logger for SLF4J
     private static final Logger LOG = Logger.getLogger(GliaClient.class.getName());
 
     private static final long SERVER_CONNECTION_TIMEOUT = 3000;     // 3 sec
     private static final long EXECUTOR_TIME_OUT = 60000;            // 1 min
     private static final long FUTURE_TASK_TIME_OUT = 2000;          // 2 sec
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
 
     protected GliaClient() {
         this.port = 7000;
         this.host = "localhost";
     }
 
     public GliaClient(String host, int port) {
         this.host = host;
         this.port = port;
         this.gliaPayload = null;
         this.executor = this.getExecutor();
         // TODO what about LOG no console
         System.out.println("\n\n GliaClient started \n for server:" + host + ":" + port + "\n\n");
     }
 
     public GliaClient(String host, int port, long timeout) {
         this.host = host;
         this.port = port;
         this.gliaPayload = null;
         this.futureTaskTimeOut = timeout;
         this.executor = this.getExecutor();
         System.out.println("\n\n GliaClient started \n for server:" + host + ":" + port + "\n\n");
     }
 
     public boolean isRunning() {
         return running;
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
      *
      * @param object
      */
     private void serverListener(Object object) {
         if(object instanceof GliaPayload){
 
             LOG.info("SERVER LISTENER = arrived from server:" + ((GliaPayload) object).toString());
 
             this.gliaPayload = ((GliaPayload) object);
             if(this.futureTask != null){
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
      * Will wait maximum for a 2 seconds
      * @return
      */
     public GliaPayload getGliaPayload() {
 
         if(this.futureTask != null){
 
             try {
 
                 this.setGliaPayload(this.futureTask.get(this.futureTaskTimeOut, TimeUnit.MILLISECONDS));
 
             } catch (TimeoutException e) {
                 LOG.log(Level.WARNING,"TimeoutException futureTask == HERE");
                 this.futureTask.cancel(true);
                 //e.printStackTrace();
             } catch (InterruptedException e) {
                 LOG.log(Level.WARNING,"InterruptedException futureTask == HERE");
                 //e.printStackTrace();
             } catch (ExecutionException e) {
                 LOG.log(Level.WARNING,"ExecutionException futureTask == HERE");
                 //e.printStackTrace();
             }catch(CancellationException ce){
                 LOG.log(Level.WARNING,"Future task was Canceled - YES!!!");
             } catch (Exception e) {
                 LOG.log(Level.WARNING,"GENERAL Exception futureTask == HERE");
                 e.printStackTrace();
             }
         }
 
         if(this.gliaPayload != null){
             return this.gliaPayload;
         }
 
         return GliaPayloadBuilder.buildErrorPayload(GliaPayloadStatus.ERROR_SERVER_TIMEOUT);
     }
 
     private void setGliaPayload(GliaPayload inGliaPayload) {
         this.gliaPayload = inGliaPayload;
     }
 
     /**
      * Send to server GliaPayload
      *
      * @see GliaPayload
      * @param gliaPayloadSend
      * @throws IOException
      */
     public void send(GliaPayload gliaPayloadSend) throws IOException {
         this.setGliaPayload(null);
 
         // clean & start a timer
         if(this.channel != null && this.channel.isConnected()){
             LOG.info("Connected:" + this.channel.isConnected());
 
             if(gliaPayloadSend != null){
                 LOG.info("Send from GliaClient gliaPayload:" + gliaPayloadSend.toString());
                 gliaPayloadSend.setClientTimestamp(System.currentTimeMillis());
                 this.channel.write(gliaPayloadSend);
 
                 this.shutDownExecutor();
                 this.executor = this.getExecutor();
                 this.executor.execute(this.createFutureTask());
 
                 return;
             }
         }
         throw new IOException("Channel is closed or even not opened");
     }
 
     /**
      *  Shutdown a client
      */
     @Override
     public void shutdown(){
         this.shutDownExecutor();
 
         this.channelFuture.getChannel().close().awaitUninterruptibly();
         this.channelFactory.releaseExternalResources();
         this.channelFactory.shutdown();
 
         this.clientBootstrap.releaseExternalResources();
         this.clientBootstrap.shutdown();
 
         this.running = false;
     }
 
     /**
      * Start a GliaClient
      *
      * for that case use
      *
      * @throws Exception
      */
     @Override
     public void start() throws Exception {
 
         if(this.running){
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
                         new ObjectEncoder(),
                         new ObjectDecoder(ClassResolvers.cacheDisabled(getClass().getClassLoader()))
                         ,
                         new SimpleChannelUpstreamHandler(){
 
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
                                 LOG.log(
                                         Level.WARNING,
                                         "Unexpected exception from downstream.",
                                         e.getCause());
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
 
         // !!! see also - http://massapi.com/class/cl/ClientBootstrap.html
        System.out.println("1");
         // just wait for server connection for 3sec.
         channelFuture.await(SERVER_CONNECTION_TIMEOUT);
         if (!channelFuture.isSuccess()) {
             channelFuture.getCause().printStackTrace();
             channelFactory.releaseExternalResources();
             this.running = false;
         }else{
             this.running = true;
         }
 
         // if need to disconnect right after server response
         //  channelFuture.getChannel().getCloseFuture().awaitUninterruptibly();
         //  channelFactory.releaseExternalResources();
 
     }
 
     /**
      * Creates a thread pool that creates new threads as needed, but
      * will reuse previously constructed threads when they are
      * available.
      *
      * For purpose of GliaClient - just enough a single thread
      *
      * These pools will typically improve the performance
      * of programs that execute many short-lived asynchronous tasks.
      *
      * Calls to <tt>execute</tt> will reuse previously constructed
      * threads if available. If no existing thread is available, a new
      * thread will be created and added to the pool.
      *
      * Threads that have
      * not been used for sixty seconds are terminated and removed from
      * the cache.
      *
      * Thus, a pool that remains idle for long enough will
      * not consume any resources. Note that pools with similar
      * properties but different details (for example, timeout parameters)
      * may be created using {@link ThreadPoolExecutor} constructors.
      *
      * @see Executors
      * @return ExecutorService
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
     private void shutDownExecutor(){
         this.shutDownFutureTask();
         if(this.executor != null && !this.executor.isShutdown()){
             try{
                 this.executor.shutdown();
             }catch(Exception ex){
                 // TODO make it more accurate
                 ex.printStackTrace();
             }
             this.executor = null;
         }
     }
 
     /**
      *
      */
     private void shutDownFutureTask(){
         if(this.futureTask != null){
             this.futureTask.cancel(true);
             this.futureTask = null;
         }
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
