 package com.virtualdisk.client;
 
 import com.virtualdisk.network.*;
 import com.virtualdisk.network.request.*;
 
 import org.jboss.netty.bootstrap.*;
 import org.jboss.netty.channel.*;
 import org.jboss.netty.channel.socket.nio.*;
 
 import java.net.*;
 import java.util.*;
 import java.util.concurrent.*;
 
 public class Client
 {
     private static final int DEFAULT_BLOCK_SIZE = 1024;
     private static int blockSize = DEFAULT_BLOCK_SIZE;
     private static int requestId = 0;
 
     private String host;
     private int port;
 
     private Channel channel;
     private ChannelFactory channelFactory;
 
     public Client(String host, int port)
     {
         this.host = host;
         this.port = port;
     }
 
     public int getBlockSize()
     {
         return blockSize;
     }
 
     public void connect()
     {
         ChannelFactory channelFactory = new NioClientSocketChannelFactory
             ( Executors.newCachedThreadPool()
             , Executors.newCachedThreadPool()
             );
 
         ClientBootstrap bootstrap = new ClientBootstrap(channelFactory);
         bootstrap.setPipelineFactory(new ClientPipelineFactory());
 
         ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));
         future.awaitUninterruptibly();
         if (future.isDone() && future.isSuccess())
         {
             channel = future.getChannel();
             System.out.println("Connected to the coordinator.");
         }
         else
         {
             System.out.println("Error connecting to the coordinator.");
             System.exit(1);
         }
     }
 
     public void createVolume(int volumeId)
     {
         ++requestId;
         CreateVolumeRequest request = new CreateVolumeRequest(requestId, volumeId);
         channel.write(request);
     }
 
     public void deleteVolume(int volumeId)
     {
         ++requestId;
         DeleteVolumeRequest request = new DeleteVolumeRequest(requestId, volumeId);
         channel.write(request);
     }
 
     public void write(int volumeId, long logicalOffset, byte[] block)
     {
         ++requestId;
         WriteRequest request = new WriteRequest(requestId, volumeId, logicalOffset, new Date(), block);
         channel.write(request);
        System.out.println("Sent request " + requestId);
     }
 
     public void read(int volumeId, long logicalOffset)
     {
         ++requestId;
         ReadRequest request = new ReadRequest(requestId, volumeId, logicalOffset);
         channel.write(request);
     }
 
     public void disconnect()
     {
         try
         {
             channel.close().awaitUninterruptibly();
             channelFactory.releaseExternalResources();
         }
         catch (Exception e)
         {
             System.exit(0);
         }
     }
 }
 
