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
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.concurrent.Executors;
 
 import org.jboss.netty.bootstrap.ServerBootstrap;
 import org.jboss.netty.channel.ChannelFactory;
 import org.jboss.netty.channel.ChannelPipeline;
 import org.jboss.netty.channel.ChannelPipelineFactory;
 import org.jboss.netty.channel.Channels;
 import org.jboss.netty.channel.group.ChannelGroup;
 import org.jboss.netty.channel.group.ChannelGroupFuture;
 import org.jboss.netty.channel.group.DefaultChannelGroup;
 import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public final class PubSubServer {
 
     public static final InetSocketAddress       DEFAULT_ADDRESS = new InetSocketAddress(9000);
 
     private static final Logger                 logger          = LoggerFactory.getLogger(PubSubServer.class);
 
     private final Collection<InetSocketAddress> listenAddresses;
     private final ChannelFactory                factory;
     private final ServerBootstrap               bootstrap;
     private final ChannelGroup                  openChannels;
 
     public PubSubServer(final InetSocketAddress... listenAddresses) {
         final Collection<InetSocketAddress> laddrs = new LinkedList<InetSocketAddress>();
         if (listenAddresses != null && listenAddresses.length > 0)
             for (final InetSocketAddress address : listenAddresses)
                 laddrs.add(address);
         else
             laddrs.add(DEFAULT_ADDRESS);
         this.listenAddresses = Collections.unmodifiableCollection(laddrs);
         openChannels = new DefaultChannelGroup(getClass().getName());
         factory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
         bootstrap = new ServerBootstrap(factory);
         final ServerMessageHandler sharedMessageHandler = new ServerMessageHandler();
         bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
 
            @Override
             public ChannelPipeline getPipeline() {
                 return Channels.pipeline(MessageCodec.decoder(), MessageCodec.encoder(), sharedMessageHandler);
             }
 
         });
         bootstrap.setOption("child.tcpNoDelay", true);
         bootstrap.setOption("child.keepAlive", true);
     }
 
     public void start() {
         for (final InetSocketAddress address : listenAddresses) {
             logger.info("Starting listener on {}", address);
             openChannels.add(bootstrap.bind(address));
         }
         logger.info("Server startup complete");
     }
 
     public void stop() throws InterruptedException {
         logger.info("Server shutting down...");
         final ChannelGroupFuture future = openChannels.close();
         try {
             future.await();
         } finally {
             factory.releaseExternalResources();
         }
         logger.info("Server shut down.");
     }
 
     /**
      * @param args
      *            host:port pairs to listen on, if none given then the default of port 9000 on all interfaces is used.
      * @throws Throwable
      *             if unable to parse command line arguments as host:port pairs
      */
     public static void main(final String[] args) throws Throwable {
         final InetSocketAddress[] listenAddrs = new InetSocketAddress[args.length];
         for (int pos = 0; pos < args.length; ++pos) {
             try {
                 final String[] host_port = args[pos].split(":");
                 if (host_port == null || host_port.length != 2)
                     throw new Exception("Expected host:port pair, got '" + args[pos] + "' instead");
                 listenAddrs[pos] = new InetSocketAddress(host_port[0], Integer.parseInt(host_port[1]));
             } catch (final Throwable t) {
                 System.err.println("Unable to process argument " + (pos + 1) + " '" + args[pos] + "' as a host:port pair");
                 t.printStackTrace(System.err);
                 throw t;
             }
         }
         final PubSubServer pss = new PubSubServer(listenAddrs);
         Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
 
             @Override
             public void run() {
                 try {
                     pss.stop();
                 } catch (final InterruptedException ie) {
                     logger.error("Interrupted while waiting for server to shut down", ie);
                 }
             }
 
         }));
         pss.start();
     }
 
 }
