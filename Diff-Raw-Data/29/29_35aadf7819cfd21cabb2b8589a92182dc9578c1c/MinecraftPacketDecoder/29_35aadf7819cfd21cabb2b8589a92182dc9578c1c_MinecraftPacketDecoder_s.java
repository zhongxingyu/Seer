 package net.theunnameddude.mcclient.netty;
 
 import io.netty.buffer.ByteBuf;
 import io.netty.channel.ChannelHandlerContext;
 import io.netty.handler.codec.ReplayingDecoder;
 import net.theunnameddude.mcclient.protocol.PacketUtils;
 import net.theunnameddude.mcclient.protocol.packets.BasePacket;
 
 import java.util.List;
 
 public class MinecraftPacketDecoder extends ReplayingDecoder<Void> {
     @Override
     protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> objects) throws Exception {
         while ( true ) {
             int startIndex = buf.readerIndex();
             short packetId = buf.readUnsignedByte();
             BasePacket packet = PacketUtils.readPacket( packetId, buf );
             checkpoint();
             if ( packet != null ) {
                 objects.add( packet );
             }
         }
     }
 }
