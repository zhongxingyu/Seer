 /*
  *  Copyright 2013 JMX Daemon contributors
  * 
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package org.hibnet.jmxdaemon;
 
 import java.net.SocketAddress;
 import java.util.concurrent.Executor;
 import java.util.concurrent.Executors;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import org.jboss.netty.bootstrap.ClientBootstrap;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelFuture;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ChannelPipeline;
 import org.jboss.netty.channel.ChannelPipelineFactory;
 import org.jboss.netty.channel.Channels;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
 import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
 import org.jboss.netty.handler.codec.string.StringDecoder;
 import org.jboss.netty.handler.codec.string.StringEncoder;
 
 public class JmxDaemonClient {
 
     private ClientBootstrap bootstrap;
 
     private JmxDaemonClientHandler handler;
 
    private SocketAddress serverAdd;

     private static class JmxDaemonClientHandler extends SimpleChannelUpstreamHandler {
 
         private LinkedBlockingQueue<String> responses = new LinkedBlockingQueue<>();
 
         @Override
         public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
             String response = (String) e.getMessage();
             responses.add(response);
             super.messageReceived(ctx, e);
         }
     }
 
     public JmxDaemonClient(SocketAddress serverAdd) {
        this.serverAdd = serverAdd;
         Executor bossPool = Executors.newCachedThreadPool();
         Executor workerPool = Executors.newCachedThreadPool();
         bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(bossPool, workerPool));
         handler = new JmxDaemonClientHandler();
         bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
             public ChannelPipeline getPipeline() throws Exception {
                 return Channels.pipeline(new StringDecoder(), new StringEncoder(), handler);
             };
         });
     }
 
     public String send(String cmd) {
        ChannelFuture cf = bootstrap.connect(serverAdd);
        cf.awaitUninterruptibly();
        Channel ch = cf.getChannel();
        ch.write(cmd + "\n");
         try {
             return handler.responses.take();
         } catch (InterruptedException e) {
             throw new RuntimeException(e);
         }
     }
 
     public void close() {
         bootstrap.releaseExternalResources();
     }
 }
