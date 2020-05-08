 // Copyright 2011 Nathaniel Harward
 //
 // This file is part of commons-j.
 //
 // commons-j is free software: you can redistribute it and/or modify
 // it under the terms of the GNU General Public License as published by
 // the Free Software Foundation, either version 3 of the License, or
 // (at your option) any later version.
 //
 // commons-j is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 // GNU General Public License for more details.
 //
 // You should have received a copy of the GNU General Public License
 // along with commons-j. If not, see <http://www.gnu.org/licenses/>.
 
 package us.harward.commons.net.pubsub;
 
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.buffer.ChannelBuffers;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelDownstreamHandler;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ChannelUpstreamHandler;
 import org.jboss.netty.channel.ExceptionEvent;
 import org.jboss.netty.handler.codec.frame.FrameDecoder;
 import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
 
 import com.google.common.base.Preconditions;
 
 final class MessageCodec {
 
     private static final class Decoder extends FrameDecoder {
 
         private final Message.Builder builder = Message.newBuilder();
 
         @Override
         protected Object decode(final ChannelHandlerContext ctx, final Channel channel, final ChannelBuffer buffer)
                 throws Exception {
             return builder.add(buffer);
         }
 
         @Override
         public void exceptionCaught(final ChannelHandlerContext ctx, final ExceptionEvent e) throws Exception {
             if (e.getCause() instanceof MessageFormatException)
                 e.getChannel().close();
             else
                 super.exceptionCaught(ctx, e);
         }
     }
 
     private static final class Encoder extends OneToOneEncoder {
 
         @Override
         protected Object encode(final ChannelHandlerContext ctx, final Channel channel, final Object msg) throws Exception {
             if (msg instanceof Message) {
                 final Message m = (Message) msg;
                final ChannelBuffer rv = ChannelBuffers.dynamicBuffer(m.headerSize() + m.estimatedBodySize());
                 m.marshall(rv);
                 return ChannelBuffers.unmodifiableBuffer(rv);
             } else
                 return msg;
         }
 
     }
 
     private MessageCodec() {
         Preconditions.checkArgument(false, "Please use the encoder()/decoder() methods instead");
     }
 
     static ChannelDownstreamHandler encoder() {
         return new Encoder();
     }
 
     static ChannelUpstreamHandler decoder() {
         return new Decoder();
     }
 
 }
