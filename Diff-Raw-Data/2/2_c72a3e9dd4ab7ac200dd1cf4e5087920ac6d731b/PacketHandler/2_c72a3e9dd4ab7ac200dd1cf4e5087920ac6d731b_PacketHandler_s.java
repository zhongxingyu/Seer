 package de.minestar.protocol.newpackets;
 
 import java.nio.ByteBuffer;
 import java.nio.charset.Charset;
 import java.util.Arrays;
 
 import net.md_5.bungee.api.connection.Server;
 import de.minestar.protocol.newpackets.packets.ChatPacket;
 
 public class PacketHandler {
 
     private static final String BROADCAST = "ALL";
 
     private static final int MAX_PACKET_SIZE = 32766;
 
     private final ByteBuffer BUFFER;
 
     public PacketHandler() {
         BUFFER = ByteBuffer.allocate(MAX_PACKET_SIZE);
     }
 
     public void send(NetworkPacket packet, Server server, String channel) {
         this.send(packet, server, channel, BungeeSubChannel.FORWARD, BROADCAST);
     }
 
     public void send(NetworkPacket packet, Server server, String channel, BungeeSubChannel subChannel, String targetServer) {
         if (packet instanceof MultiPacket) {
             MultiPacket multiPacket = (MultiPacket) packet;
             for (NetworkPacket innerPacket : multiPacket) {
                 sendPacket(innerPacket, server, channel, subChannel, targetServer);
             }
         } else {
             sendPacket(packet, server, channel, subChannel, targetServer);
         }
     }
 
     public void send(NetworkPacket packet, Server server, String channel, BungeeSubChannel subChannel) {
         if (packet instanceof MultiPacket) {
             MultiPacket multiPacket = (MultiPacket) packet;
             for (NetworkPacket innerPacket : multiPacket) {
                 sendPacket(innerPacket, server, channel, subChannel, null);
             }
         } else {
             sendPacket(packet, server, channel, subChannel, null);
         }
     }
 
     public final static Charset UFT8 = Charset.forName("UTF-8");
 
     private void sendPacket(NetworkPacket packet, Server server, String channel, BungeeSubChannel subChannel, String targetServer) {
 
         // Packet Head:
         //
         // | BUNGEE HEAD |
         // ------------------------------------
         // BungeeChannel (Forward, Connect etc.) - Command to Bungee what to do
         // with the packet
         // TargetServer (ALL or servername) - Receiver of the message
         // ------------------------------------
         // | BUKKIT HEAD |
         // Channel (Own defined plugin channel) - Channel between two plugins
         // DataLength (Length of the data without any head length)
         // Data (Array of bytes - Must be long as defined in DataLength
         //
 
         // Create Head
         BUFFER.clear();
         // BungeeChannel
         BUFFER.put(subChannel.getName().getBytes(UFT8));
         // TargetServer
         BUFFER.put(targetServer.getBytes(UFT8));
 
         // Channel
         BUFFER.put(channel.getBytes(UFT8));
 
         // Placeholder
         int pos1 = BUFFER.position();
         BUFFER.putInt(0);
         int pos2 = BUFFER.position();
         packet.pack(BUFFER);
         BUFFER.putInt(pos1, BUFFER.position() - pos2);
 
         BUFFER.rewind();
 
         // Dirty -.-
         server.sendData(channel, Arrays.copyOf(BUFFER.array(), BUFFER.limit()));
         BUFFER.clear();
     }
 
     public NetworkPacket extractPacket(byte[] data) {
         BUFFER.clear();
         BUFFER.put(data);
        BUFFER.reset();
 
         PacketType type = PacketType.get(BUFFER.getInt());
         switch (type) {
             case CHAT :
                 return new ChatPacket(BUFFER);
             default :
                 return null;
         }
     }
 }
