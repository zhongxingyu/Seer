 /**
  * Copyright (c) 2011 Red Hat, Inc.
  *
  * This software is licensed to you under the GNU General Public License,
  * version 2 (GPLv2). There is NO WARRANTY for this software, express or
  * implied, including the implied warranties of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
  * along with this software; if not, see
  * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
  *
  * Red Hat trademarks are not licensed under GPLv2. No permission is
  * granted to use or replicate Red Hat trademarks that are incorporated
  * in this software or its documentation.
  */
 package org.candlepin.thumbslug;
 
 import static org.jboss.netty.handler.codec.http.HttpHeaders.*;
 
 import java.net.InetSocketAddress;
 
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelFactory;
 import org.jboss.netty.channel.ChannelFuture;
 import org.jboss.netty.channel.ChannelFutureListener;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ChannelStateEvent;
 import org.jboss.netty.channel.ExceptionEvent;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
 import org.jboss.netty.handler.codec.http.HttpChunk;
 import org.jboss.netty.handler.codec.http.HttpRequest;
 
 /**
  * HttpRequestHandler
  */
 public class HttpRequestHandler extends SimpleChannelUpstreamHandler {
 
     private HttpRequest request;
     private boolean readingChunks;
     private Channel cdnChannel;
   
     private Config config;
     private ChannelFactory channelFactory;
     private HttpClientPipelineFactory clientFactory;
 
 
     public HttpRequestHandler(Config config, ChannelFactory channelFactory,
         HttpClientPipelineFactory clientFactory) {
         this.config = config;
         this.channelFactory = channelFactory;
         this.clientFactory = clientFactory;
     }
     
     @Override
     public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
         // Add to the global list of open channels for graceful shutdown
         Main.ALL_CHANNELS.add(ctx.getChannel());
     }
     
     @Override
     public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
         throws Exception {
         if (!readingChunks) {
             requestStartReceived(e);
         }
         else {
             requestChunkReceived(e);
         }
     }
 
     private void requestStartReceived(MessageEvent e) throws Exception {
         this.request = (HttpRequest) e.getMessage();
         final HttpRequest request = this.request;
         if (config.getBoolean("sendTSheader")) {
             request.addHeader("X-Forwarded-By", "Thumbslug v1.0");
         }
 
        // XXX: can we use channel.getSink() and attach here to join the two channels
        // instead?
         cdnChannel = channelFactory.newChannel(
             clientFactory.getPipeline(e.getChannel(),
                 config.getBoolean("cdn.ssl"),
                 isKeepAlive(request)));
         
         ChannelFuture future = cdnChannel.connect(
             new InetSocketAddress(config.getProperty("cdn.host"),
                 config.getInt("cdn.port")));
         future.addListener(new ChannelFutureListener() {
             public void operationComplete(final ChannelFuture future)
                 throws Exception {
                 future.getChannel().write(request);
             }
         });
 
         if (request.isChunked()) {
             readingChunks = true;
         }
     }
 
     private void requestChunkReceived(MessageEvent e) {
         HttpChunk chunk = (HttpChunk) e.getMessage();
         if (chunk.isLast()) {
             readingChunks = false;
         }
         else {
             cdnChannel.write(chunk);
         }
     }
 
     @Override
     public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
         throws Exception {
         e.getCause().printStackTrace();
         e.getChannel().close();
     }
 }
