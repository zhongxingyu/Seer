 package org.phicluster.nettyserver;
 
 import org.jboss.netty.bootstrap.ServerBootstrap;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelFactory;
 import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.net.InetSocketAddress;
 import java.util.concurrent.Executors;
 
 public class ClusterHttpServer {
     protected static final Logger logger = LoggerFactory.getLogger(ClusterHttpServer.class);
 
     private final int port;
     private Channel channel;
 
     public ClusterHttpServer(int port) {
         this.port = port;
     }
 
     public void run() {
         ChannelFactory factory = new NioServerSocketChannelFactory(
                 Executors.newCachedThreadPool(),
                 Executors.newCachedThreadPool());
 
         ServerBootstrap bootstrap = new ServerBootstrap(factory);
 
         bootstrap.setPipelineFactory(new ClusterHttpServerPipelineFactory());
 
         channel = bootstrap.bind(new InetSocketAddress(port));
 
         logger.info("http server started on port {}", port);
     }
 
     public void stop() {
        channel.close().awaitUninterruptibly();
 
         logger.info("http server stopped");
     }
 
     public static void main(String[] args) throws Exception {
         int port;
         if (args.length > 0) {
             port = Integer.parseInt(args[0]);
         } else {
             port = 8080;
         }
         new ClusterHttpServer(port).run();
     }
 }
