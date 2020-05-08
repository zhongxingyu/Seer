 package com.voxeo.ozone.websockets;
 
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.handler.codec.http.websocket.DefaultWebSocketFrame;
 import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;
 
 import com.voxeo.ozone.web.client.Message;
 import com.voxeo.ozone.web.client.MessagesQueue;
 
 public class IQProcessor {
 
 	public void handleRequest(final ChannelHandlerContext ctx, final WebSocketFrame frame) {
 		
 		try {
 
 			if (!frame.isText())
 				return;
 
 			showIQs(ctx.getChannel());
 		} catch (final Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void showIQs(final Channel channel) {
 		
 		new Thread() {
 			StringBuffer buf = null;
 
 			@Override
 			public void run() {
 				if (channel == null)
 					return;
 				buf = new StringBuffer();
 				try {
 					while (channel.isConnected()) {
 
 						Message message = MessagesQueue.poll();
 						if (message != null) {
 							buf.append("<div class=\"row");
 							if (message.getType() == Message.Type.IN) {
 								buf.append("-in\">");
							} else {
 								buf.append("-out\">");				
 							}
 								
 							buf.append(message.getMessage());
 							buf.append("</div>");	
 	
 							if (channel.isOpen())
 								channel.write(new DefaultWebSocketFrame(buf
 										.toString()));
 	
 							buf.setLength(0);
 						} else {
 							Thread.sleep(100);
 						}
 					}
 				} catch (final Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}.start();
 	}
 }
