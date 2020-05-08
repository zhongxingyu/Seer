 package com.reversemind.glia.server;
 
 import com.reversemind.glia.GliaPayload;
 import org.apache.commons.lang3.StringUtils;
 import org.jboss.netty.bootstrap.ServerBootstrap;
 import org.jboss.netty.channel.ChannelPipeline;
 import org.jboss.netty.channel.ChannelPipelineFactory;
 import org.jboss.netty.channel.Channels;
 import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
 import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
 import org.jboss.netty.handler.codec.serialization.ClassResolvers;
 import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
 import org.jboss.netty.handler.codec.serialization.ObjectEncoder;
 
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.util.UUID;
 import java.util.concurrent.Executors;
 
 /**
  * Date: 4/24/13
  * Time: 10:07 AM
  *
  * @author konilovsky
  * @since 1.0
  */
 public abstract class GliaServer implements IGliaServer, Serializable {
 
     private String name;
     private String instanceName;
 
     private boolean running = false;
 
     private ServerBootstrap serverBootstrap;
     private SimpleChannelUpstreamHandler handler;
     protected Metrics metrics;
 
     private int port;
     private boolean keepClientAlive = false;
 
     private GliaPayload gliaPayload;
     private IGliaPayloadProcessor gliaPayloadWorker;
 
     /**
      *
      * @param builder
      */
     public GliaServer(GliaServerFactory.Builder builder){
         if(builder == null){
             throw new RuntimeException("Builder is empty");
         }
 
         // parameter setAutoSelectPort has more priority than assigned setPort value
         if (builder.isAutoSelectPort()) {
             this.port = this.detectFreePort();
         } else if (builder.port() < 0) {
             this.port = this.detectFreePort();
         } else {
             this.port = builder.port();
         }
 
         if(builder.getPayloadWorker() == null){
             throw new RuntimeException("Assign a setPayloadWorker to server!");
         }
        this.gliaPayloadWorker = builder.getPayloadWorker();
 
         // drop connection from client or not
         this.keepClientAlive = builder.isKeepClientAlive();
 
         this.name = StringUtils.isEmpty(builder.getName()) ?  UUID.randomUUID().toString() : builder.getName();
         this.instanceName = StringUtils.isEmpty(builder.getInstanceName()) ? UUID.randomUUID().toString() : builder.getInstanceName();
 
         this.metrics = new Metrics();
     }
 
     /**
      * Detect free setPort on System
      *
      * @return
      */
     private int detectFreePort(){
         try {
             ServerSocket serverSocket = new ServerSocket(0);
             if (serverSocket.getLocalPort() == -1) {
                 System.exit(-100);
                 throw new RuntimeException("\n\nCould not start GliaServer 'cause no any available free port in system");
             }
 
             int detectedPortNumber = serverSocket.getLocalPort();
 
             serverSocket.close();
             int count = 0;
             while (!serverSocket.isClosed()) {
                 if (count++ > 10) {
                     throw new RuntimeException("Could not start GliaServer");
                 }
                 try {
                     Thread.sleep(100);
                     System.out.println("Waiting for closing autodiscovered socket try number#" + count);
                 } catch (InterruptedException e) {
                     System.exit(-100);
                     throw new RuntimeException("Could not start GliaServer");
                 }
             }
             serverSocket = null;
 
 
             return detectedPortNumber;
         } catch (IOException e) {
             e.printStackTrace();
         }
         throw new RuntimeException("\n\nCould not start GliaServer 'cause no any available free port in system");
     }
 
     /**
      * Get server setName
      *
      * @return
      */
     public String getName() {
         return name;
     }
 
     /**
      * Instance setName mostly prefer to use a UUID
      *
      * @return
      */
     public String getInstanceName() {
         return instanceName;
     }
 
     /**
      * Get setPort number of GliaServer
      *
      * @return
      */
     public int getPort() {
         return port;
     }
 
     /**
      * Get server metrics
      *
      * @return
      */
     public Metrics getMetrics() {
         return this.metrics;
     }
 
     /**
      *
      */
     public void start() {
         // Configure the server.
         this.serverBootstrap = new ServerBootstrap(
                 new NioServerSocketChannelFactory(
                         Executors.newCachedThreadPool(),
                         Executors.newCachedThreadPool()));
 
         this.handler = new GliaServerHandler(gliaPayloadWorker, metrics, keepClientAlive);
 
         // Set up the pipeline factory
         // TODO add Kryo serializer
         this.serverBootstrap.setPipelineFactory(new ChannelPipelineFactory() {
             public ChannelPipeline getPipeline() throws Exception {
                 return Channels.pipeline(
                         new ObjectEncoder(),
                         new ObjectDecoder(
                                 ClassResolvers.cacheDisabled(getClass().getClassLoader())),
                         handler);
             }
         });
 
         // Bind and start to accept incoming connections.
         this.serverBootstrap.bind(new InetSocketAddress(port));
 
         // TODO use CORRECT LOGGGING
         System.out.println(this.toString());
         System.out.println("\n\nServer started\n\n");
 
         this.running = true;
     }
 
     public boolean isRunning() {
         return this.running;
     }
 
     /**
      * Shutdown server
      */
     public void shutdown() {
         if (this.serverBootstrap != null) {
             this.serverBootstrap.releaseExternalResources();
             this.serverBootstrap.shutdown();
         }
     }
 
     public String getHost(){
         // TODO remove from test implementation code
         return "localhost";
     }
 
     @Override
     public String toString(){
         return "\n\n\n" +
                 " GliaServer " +
                 "\n-------------------" +
                 "\n setName:" + this.name +
                 "\n instance:" + this.instanceName +
                 "\n port:" + this.port +
                 "\n metrics:" + this.metrics +
                 "  \n\n\n";
     }
 
 }
