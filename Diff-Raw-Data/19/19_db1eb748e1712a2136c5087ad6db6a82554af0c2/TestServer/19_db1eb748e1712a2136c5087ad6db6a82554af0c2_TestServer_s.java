 /*
  * Copyright 2012 The Netty Project
  *
  * The Netty Project licenses this file to you under the Apache License,
  * version 2.0 (the "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at:
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations
  * under the License.
  */
 package de.jpaw.bonaparte.netty.testServer;
 
 import io.netty.bootstrap.ServerBootstrap;
 import io.netty.channel.ChannelFuture;
 import io.netty.channel.ChannelOption;
 import io.netty.channel.socket.nio.NioEventLoopGroup;
 import io.netty.channel.socket.nio.NioServerSocketChannel;
 import io.netty.handler.logging.LogLevel;
 import io.netty.handler.logging.LoggingHandler;
 
 import java.net.InetSocketAddress;
 
 import de.jpaw.bonaparte.netty.BonaparteNettyPipelineFactory;
 
 /**
  * Implements a simple server for testing purposes, every request will return a response with some values copied from the request, plus some data obtained from the server.
  */
 public class TestServer {
 
     private final int port;
 
     public TestServer(int port) {
         this.port = port;
     }
 
     public void run() throws Exception {
         // Configure the server.
         ServerBootstrap b = new ServerBootstrap();
         try {
             b.group(new NioEventLoopGroup(), new NioEventLoopGroup())
             .channel(NioServerSocketChannel.class)
             .option(ChannelOption.SO_BACKLOG, 100)
             .localAddress(new InetSocketAddress(port))
             .childOption(ChannelOption.TCP_NODELAY, true)
             .handler(new LoggingHandler(LogLevel.INFO))
             .childHandler(new BonaparteNettyPipelineFactory(1000, new TestServerHandler()));
 
             // Start the server.
             ChannelFuture f = b.bind().sync();
 
             // Wait until the server socket is closed.
             f.channel().closeFuture().sync();
         } finally {
             // Shut down all event loops to terminate all threads.
             b.shutdown();
         }
     }
 
     public static void main(String[] args) throws Exception {
         int port = 8077;
         if (args.length > 0) {
             port = Integer.parseInt(args[0]);
         }
         System.out.println("Starting a test server on port " + port);
         new TestServer(port).run();
     }
 }
