 package com.dustyneuron.bitprivacy.exchanger;
 
 
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ChannelStateEvent;
 import org.jboss.netty.channel.ExceptionEvent;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.channel.SimpleChannelHandler;
 
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.ClientMessage;
 import com.google.protobuf.InvalidProtocolBufferException;
 
 
 public class ServerHandler extends SimpleChannelHandler {
 	
 	final private MixServer mixServer;
 	
 	public ServerHandler (MixServer mx) {
 		mixServer = mx;
 	}
 	
 	@Override
 	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
 		mixServer.allChannels.add(e.getChannel());
 	}
 		
 	@Override
 	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws InvalidProtocolBufferException {
 		 ChannelBuffer buf = (ChannelBuffer) e.getMessage();
 		 int numBytes = buf.readInt();
 		 byte[] data = new byte[numBytes];
 		 buf.readBytes(data);
 		 
 		 ClientMessage msg = ClientMessage.parseFrom(data);
 		 
		 System.out.println("Server received msg:\n" + msg.toString());		 
 		 System.out.flush();
 		 
 		 mixServer.handleMessage(msg, e.getChannel());
 	}
  
 	@Override
 	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
 		e.getCause().printStackTrace(); 
 		Channel ch = e.getChannel();
 		ch.close();
 	}
 }
