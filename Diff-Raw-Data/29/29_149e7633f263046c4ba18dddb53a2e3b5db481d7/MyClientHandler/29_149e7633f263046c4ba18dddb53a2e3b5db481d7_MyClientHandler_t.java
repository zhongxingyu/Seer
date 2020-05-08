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
 package info.chenliang.myclient;
 
 import info.chenliang.myserver.messages.MyMessages.MessageBase;
 
 import info.chenliang.myserver.messages.MyMessages.MessageBase.MessageType;
 import info.chenliang.myserver.messages.MyMessages.VersionRequest;
 
 import java.util.concurrent.TimeUnit;

 
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelEvent;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ChannelStateEvent;
 import org.jboss.netty.channel.ExceptionEvent;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class MyClientHandler extends SimpleChannelUpstreamHandler {
 
     private static final Logger logger = LoggerFactory.getLogger(MyClientHandler.class);
 
     // Stateful properties
     private volatile Channel channel;
     
 
     @Override
     public void handleUpstream(
             ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
         if (e instanceof ChannelStateEvent) {
            logger.info("upstream event " + e.toString());
         }
         super.handleUpstream(ctx, e);
     }
 
     @Override
 	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
 			throws Exception {
 		// TODO Auto-generated method stub
 		super.channelConnected(ctx, e);
		logger.info("client connected");
 		while(true)
 		{
 			logger.info("requesting...");
 			
 			VersionRequest versionRequest = VersionRequest.newBuilder().build();
 	        MessageBase mb = MessageBase.newBuilder().setType(MessageType.VERSION_REQUEST).setExtension(VersionRequest.id, versionRequest).build();
 	        
 	        channel.write(mb);
 	        
 	        TimeUnit.SECONDS.sleep(1);
 		}
 		
 		
 	}
 
 	@Override
     public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)
             throws Exception {
         channel = e.getChannel();
         super.channelOpen(ctx, e);
     }
 
     @Override
     public void messageReceived(
             ChannelHandlerContext ctx, final MessageEvent e) {
         MessageBase mb = (MessageBase) e.getMessage();
         switch(mb.getType())
         {
         case VERSION_RESPONSE:
         	logger.warn("version response received.");
         	break;
         default:
         	break;
         }
     }
 
     @Override
     public void exceptionCaught(
             ChannelHandlerContext ctx, ExceptionEvent e) {
         logger.info(
                 "Unexpected exception from downstream.",
                 e.getCause().toString());
         e.getChannel().close();
     }
 }
