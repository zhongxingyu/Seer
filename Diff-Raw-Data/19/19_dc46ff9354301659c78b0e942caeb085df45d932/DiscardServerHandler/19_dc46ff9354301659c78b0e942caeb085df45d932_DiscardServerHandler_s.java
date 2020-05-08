 package com.musicbox.server;
 
 import org.jboss.netty.channel.*;
 
 public class DiscardServerHandler extends SimpleChannelHandler {

     @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
     }
 
     @Override
     public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
         e.getCause().printStackTrace();
        
        Channel ch = e.getChannel();
        ch.close();
     }
 }
