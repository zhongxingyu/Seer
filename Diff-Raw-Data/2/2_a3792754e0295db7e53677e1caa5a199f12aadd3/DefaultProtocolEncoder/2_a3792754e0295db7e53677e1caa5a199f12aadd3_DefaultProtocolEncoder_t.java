 package org.rs2.net.pipeline;
 
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.buffer.ChannelBuffers;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
 import org.rs2.net.msg.Message;
 import org.rs2.net.msg.Message.PacketType;
 import org.rs2.net.msg.MessageBuilder;
 
 /**
  * 508 Base
  * @author Harry Andreas
  */
 public class DefaultProtocolEncoder extends OneToOneEncoder {
 
 	/**
 	 * Encodes the protocol
 	 */
 	@Override
 	protected Object encode(ChannelHandlerContext ctx, Channel chan, Object msg) throws Exception {
		if(chan == null || !chan.isConnected()) {
 			return null;
 		}
 		if(msg instanceof ChannelBuffer) {
 			MessageBuilder bldr = new MessageBuilder();
 			bldr.writeBytes(((ChannelBuffer) msg));
 			msg = bldr;
 		}
 		Message packet = null;
 		if(msg instanceof MessageBuilder) {
 			packet = ((MessageBuilder) msg).toMessage();
 		} else {
 			packet = (Message) msg;
 		}
 		if(!packet.isRaw()) {
 			int packetLength = 1 + packet.getLength() + packet.getType().getSize();
 			ChannelBuffer response = ChannelBuffers.buffer(packetLength);
 			response.writeByte((byte) packet.getOpcode());
 			if(packet.getType() == PacketType.VAR_BYTE) {
 				response.writeByte((byte) packet.getLength());
 			} else if(packet.getType() == PacketType.VAR_SHORT) {
 				response.writeShort((short) packet.getLength());
 			}
 			response.writeBytes(packet.getBuffer());
 			return response;
 		}
 		return packet.getBuffer();
 	}
 
 }
