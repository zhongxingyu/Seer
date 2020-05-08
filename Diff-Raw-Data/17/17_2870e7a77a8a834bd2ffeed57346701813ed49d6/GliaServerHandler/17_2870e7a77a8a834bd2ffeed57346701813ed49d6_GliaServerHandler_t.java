 package com.reversemind.glia.server;
 
 import org.jboss.netty.channel.*;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Date: 4/24/13
  * Time: 2:34 PM
  *
  * @author konilovsky
  * @since 1.0
  */
 public class GliaServerHandler extends SimpleChannelUpstreamHandler {
 
     private static final Logger LOG = Logger.getLogger(GliaServerHandler.class.getName());
 
     private IGliaPayloadProcessor gliaPayloadWorker;
     private boolean dropClientConnection = false;
     private Metrics metrics;
 
     public GliaServerHandler(IGliaPayloadProcessor gliaPayloadWorker, Metrics metrics, boolean dropClientConnection){
         this.gliaPayloadWorker = gliaPayloadWorker;
         this.dropClientConnection = dropClientConnection;
         this.metrics = metrics;
     }
 
     @Override
     public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
         if (e instanceof ChannelStateEvent && ((ChannelStateEvent) e).getState() != ChannelState.INTEREST_OPS) {
             LOG.info(e.toString());
         }
         super.handleUpstream(ctx, e);
     }
 
     @Override
     public void messageReceived(ChannelHandlerContext context, MessageEvent messageEvent) {
 
         // TODO what about delay + very long messages???
         long beginTime = System.currentTimeMillis();
         Object object = this.gliaPayloadWorker.process(messageEvent.getMessage());
         if(this.metrics != null){
             this.metrics.addRequest((System.currentTimeMillis() - beginTime));
         }
         // send object to the client
         ChannelFuture channelFuture = messageEvent.getChannel().write(object);
 
        if(!this.dropClientConnection){
             // see here - http://netty.io/3.6/guide/
             // Close connection right after sending
             channelFuture.addListener(new ChannelFutureListener() {
                 public void operationComplete(ChannelFuture future) {
                     Channel channel = future.getChannel();
                     channel.close();
                     }
             });
         }
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
