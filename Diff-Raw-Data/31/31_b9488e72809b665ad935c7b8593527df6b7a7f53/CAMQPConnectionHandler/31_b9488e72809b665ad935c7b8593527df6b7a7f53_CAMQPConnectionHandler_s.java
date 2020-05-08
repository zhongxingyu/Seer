 /**
  * Copyright 2012 Tejeswar Das
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * You may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 package net.dovemq.transport.connection;
 
 import net.dovemq.transport.frame.CAMQPFrame;
 import net.dovemq.transport.frame.CAMQPFrameHeader;
 import net.dovemq.transport.frame.CAMQPHandshakeFrame;
 import net.dovemq.transport.protocol.CAMQPSyncDecoder;
 import net.dovemq.transport.protocol.data.CAMQPControlClose;
 import net.dovemq.transport.protocol.data.CAMQPControlOpen;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelEvent;
 import org.jboss.netty.channel.ChannelHandler;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ChannelStateEvent;
 import org.jboss.netty.channel.ExceptionEvent;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
 
 /**
  * Handler class that compliments AMQPConnection on the incoming side.
  *
  *    ==>> CAMQPConnection ==>>
  * <<== CAMQPConnectionHandler <<==
  *
  * It is added twice in the Netty incoming pipeline.
  * See {@link CAMQPConnectionPipelineFactory}.
  *
  * The bottom-most interceptor does AMQP handshake processing
  * and is a passthru once the handshake is complete.
  *
  * The second interceptor decodes and dispatches the connection
  * frames, or dispatches session/link frames to the attached
  * ChannelHandler.
  *
  * @author tejdas
  *
  */
 class CAMQPConnectionHandler extends SimpleChannelUpstreamHandler
 {
     private static final Logger log = Logger.getLogger(CAMQPConnectionHandler.class);
 
     private final CAMQPConnectionStateActor stateActor;
     private volatile CAMQPConnection connection = null;
 
     void registerConnection(CAMQPConnection connection)
     {
         this.connection = connection;
     }
 
     CAMQPConnectionStateActor getStateActor()
     {
         return stateActor;
     }
 
     CAMQPConnectionHandler(boolean isInitiator, CAMQPConnectionProperties connectionProps)
     {
         stateActor = new CAMQPConnectionStateActor(isInitiator, connectionProps);
     }
 
     @Override
     public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
     {
         Channel channel = ctx.getChannel();
         assert (channel != null);
         stateActor.setChannel(channel);
         ctx.sendUpstream(e);
 
         CAMQPFrameDecoder frameDecoder = channel.getPipeline().get(CAMQPFrameDecoder.class);
         frameDecoder.setConnectionStateActor(stateActor);
     }
 
     @Override
     public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception
     {
         stateActor.disconnectReceived();
         super.channelDisconnected(ctx, e);
     }
 
     @Override
     public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception
     {
         super.handleUpstream(ctx, e);
     }
 
     /**
      * Invoked when an exception was raised by an I/O thread or a
      * {@link ChannelHandler}.
      */
     @Override
     public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
     {
         String connectionKey = (connection != null)? connection.getKey().toString() : StringUtils.EMPTY;
         log.warn("CAMQPConnectionHandler.exceptionCaught: " + connectionKey);
         StackTraceElement[] elems = Thread.currentThread().getStackTrace();
         for (StackTraceElement elem : elems)
         {
            log.warn("StackTrace: " + elem.toString());
         }
     }
 
     @Override
     public void messageReceived(ChannelHandlerContext ctx, final MessageEvent e)
     {
         Object message = e.getMessage();
         if (message instanceof CAMQPHandshakeFrame)
         {
             handshakeFrameReceived((CAMQPHandshakeFrame) message);
         }
         else if (message instanceof CAMQPFrame)
         {
             frameReceived((CAMQPFrame) message);
         }
         else
         {
             ctx.sendUpstream(e);
         }
     }
 
     private void handshakeFrameReceived(CAMQPHandshakeFrame frame)
     {
         stateActor.connectionHeaderBytesReceived(frame.getBody());
     }
 
     /**
      * Process incoming AMQP frames
      * @param frame
      */
     private void frameReceived(CAMQPFrame frame)
     {
         ChannelBuffer frameBody = frame.getBody();
         if (frameBody == null)
         {
             /*
              * Heart-Beat control frame
              */
             stateActor.heartbeatReceived();
             return;
         }
 
         CAMQPFrameHeader frameHeader = frame.getHeader();
         int channelNumber = frameHeader.getChannelNumber();
         if (channelNumber == 0)
         {
             /*
              * connection frame
              */
             CAMQPSyncDecoder decoder = CAMQPSyncDecoder.createCAMQPSyncDecoder();
             decoder.take(frameBody);
             String controlName = decoder.readSymbol();
             if (controlName.equalsIgnoreCase(CAMQPControlOpen.descriptor))
             {
                 CAMQPControlOpen peerConnectionProps = CAMQPControlOpen.decode(decoder);
 
                 stateActor.openControlReceived(peerConnectionProps);
             }
             else if (controlName.equalsIgnoreCase(CAMQPControlClose.descriptor))
             {
                 CAMQPControlClose closeContext = CAMQPControlClose.decode(decoder);
                 stateActor.closeControlReceived(closeContext);
             }
         }
 
         else
         {
             /*
              * session/link frame
              */
             connection.frameReceived(channelNumber, frame);
         }
     }
 }
