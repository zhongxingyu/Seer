 
 package net.specialattack.modjam;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.packet.Packet;
 import net.minecraft.network.packet.Packet250CustomPayload;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.server.management.PlayerInstance;
 import net.minecraft.server.management.PlayerManager;
 import net.minecraft.world.World;
 import net.minecraft.world.WorldServer;
 import net.minecraft.world.chunk.Chunk;
 import net.specialattack.modjam.tileentity.TileEntityLight;
 
 import com.google.common.io.ByteArrayDataInput;
 import com.google.common.io.ByteStreams;
 
 import cpw.mods.fml.common.network.IPacketHandler;
 import cpw.mods.fml.common.network.Player;
 
 public class PacketHandler implements IPacketHandler {
 
     @Override
     public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {
         ByteArrayDataInput in = ByteStreams.newDataInput(packet.data);
 
         int type = in.readUnsignedByte();
 
         switch (type) {
         case 1: {
             TileEntityLight tile = (TileEntityLight) ((EntityPlayer) player).worldObj.getBlockTileEntity(in.readInt(), in.readInt(), in.readInt());
             tile.color = in.readInt();
             tile.prevPitch = tile.pitch = in.readFloat();
             tile.prevYaw = tile.yaw = in.readFloat();
         }
         break;
         }
     }
 
     public static Packet250CustomPayload createPacket(int id, Object... data) {
         ByteArrayOutputStream bos = new ByteArrayOutputStream(32767);
         DataOutputStream dos = new DataOutputStream(bos);
 
         try {
             dos.writeByte(id);
 
             switch (id) {
             case 1: {
                 TileEntityLight tile = (TileEntityLight) data[0];
                 dos.writeInt(tile.xCoord);
                 dos.writeInt(tile.yCoord);
                 dos.writeInt(tile.zCoord);
                 dos.writeInt(tile.color);
                 dos.writeFloat(tile.pitch);
                 dos.writeFloat(tile.yaw);
             }
             break;
             }
         }
         catch (IOException e) {
             e.printStackTrace();
         }
 
         Packet250CustomPayload packet = new Packet250CustomPayload();
         packet.channel = Constants.MOD_CHANNEL;
         packet.data = bos.toByteArray();
         packet.length = packet.data.length;
         return packet;
     }
 
     public static void sendPacketToPlayersWatchingBlock(Packet packet, World world, int x, int z) {
         if (packet == null) {
             return;
         }
 
        MinecraftServer server = MinecraftServer.getServer();

        if (server != null) {
            for (WorldServer worldServer : server.worldServers) {
                if (worldServer.provider.dimensionId == world.getWorldInfo().getVanillaDimension()) {
                    Chunk chunk = world.getChunkFromBlockCoords(x, z);
                    PlayerManager manager = worldServer.getPlayerManager();
                    PlayerInstance instance = manager.getOrCreateChunkWatcher(chunk.xPosition, chunk.zPosition, false);
 
                    if (instance != null) {
                        instance.sendToAllPlayersWatchingChunk(packet);
                    }
                }
            }
        }
     }
 
     public static void sendPacketToPlayersWatchingChunk(Packet packet, int dimensionId, int chunkX, int chunkZ) {
         if (packet == null) {
             return;
         }
 
         MinecraftServer server = MinecraftServer.getServer();
 
         if (server != null) {
             for (WorldServer world : server.worldServers) {
                 if (world.provider.dimensionId == dimensionId) {
                     PlayerManager manager = world.getPlayerManager();
                     PlayerInstance instance = manager.getOrCreateChunkWatcher(chunkX, chunkZ, false);
 
                     if (instance != null) {
                         instance.sendToAllPlayersWatchingChunk(packet);
                     }
                 }
             }
         }
     }
 
 }
