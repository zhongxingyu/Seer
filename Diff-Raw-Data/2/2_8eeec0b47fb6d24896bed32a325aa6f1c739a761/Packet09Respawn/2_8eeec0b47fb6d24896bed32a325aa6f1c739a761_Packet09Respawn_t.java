 package net.theunnameddude.mcclient.protocol.packets;
 
 import io.netty.buffer.ByteBuf;
 import io.netty.channel.ChannelHandlerContext;
 import net.theunnameddude.mcclient.protocol.PacketHandler;
 
 public class Packet09Respawn extends BasePacket {
 
     public Packet09Respawn() {
         super( 0x09 );
     }
 
     int dimention;
     byte difficulty;
     byte gamemode;
     short worldHeight;
     String levelType;
 
     @Override
     public ByteBuf getPacket(ByteBuf buf) {
         return buf;
     }
 
     @Override
     public void onPacket(ByteBuf buf) {
         dimention = buf.readInt();
         difficulty = buf.readByte();
         gamemode = buf.readByte();
        worldHeight = buf.readShort();
         levelType = readString( buf );
     }
 
     @Override
     public void handle(PacketHandler handler, ChannelHandlerContext ctx) {
         handler.handle( this );
     }
 
     public int getDimention() {
         return dimention;
     }
 
     public byte getDifficulty() {
         return difficulty;
     }
 
     public byte getGamemode() {
         return gamemode;
     }
 
     public short getWorldHeight() {
         return worldHeight;
     }
 
     public String getLevelType() {
         return levelType;
     }
 }
