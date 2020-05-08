 package com.musicbox.server;
 
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
 import org.jboss.netty.channel.*;
 
 public class DiscardServerHandler extends SimpleChannelHandler {
     @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
        Channel ch = e.getChannel();
        
        ChannelBuffer time = ChannelBuffers.buffer(4);
        time.writeInt((int) (System.currentTimeMillis() / 1000));
        
        ChannelFuture f = ch.write(time);
        
     }
 
     @Override
     public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
         e.getCause().printStackTrace();
        e.getChannel().close();
     }
 }
