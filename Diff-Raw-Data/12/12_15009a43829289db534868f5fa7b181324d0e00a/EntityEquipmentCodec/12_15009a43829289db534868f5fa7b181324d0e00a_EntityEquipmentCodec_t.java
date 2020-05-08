 package com.zml.tripcraft.packets;
 
 import net.glowstone.msg.EntityEquipmentMessage;
 import net.glowstone.net.codec.MessageCodec;
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.buffer.ChannelBuffers;
 
 import java.io.IOException;
 
 public final class EntityEquipmentCodec extends MessageCodec<EntityEquipmentMessage> {
 
     public EntityEquipmentCodec() {
         super(EntityEquipmentMessage.class, 0x05);
     }
 
     @Override
     public EntityEquipmentMessage decode(ChannelBuffer buffer) throws IOException {
        System.out.println("Received msg");
         int id = buffer.readInt();
         int slot = buffer.readUnsignedShort();
         int item = buffer.readUnsignedShort();
         int damage = buffer.readUnsignedByte();
         return new EntityEquipmentMessage(id, slot, item, damage);
     }
 
     @Override
     public ChannelBuffer encode(EntityEquipmentMessage message) throws IOException {
        ChannelBuffer buffer = ChannelBuffers.buffer(10);
         buffer.writeInt(message.getId());
         buffer.writeShort(message.getSlot());
        buffer.writeShort(0);
        buffer.writeShort(0);
        System.out.println("EntityEquiptmentPacket" + message.getId() + ":" + message.getSlot() + ", " + message.getItem() + ", " + message.getDamage());
         return buffer;
     }
 
 }
