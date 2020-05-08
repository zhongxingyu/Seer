 package modJam;
 
 import java.io.ByteArrayInputStream;
 import java.io.DataInputStream;
 import java.io.IOException;
 
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.packet.Packet250CustomPayload;
 import net.minecraft.world.World;
 import cpw.mods.fml.common.network.IPacketHandler;
 import cpw.mods.fml.common.network.Player;
 
 public class PacketHandler implements IPacketHandler{
 
 	@Override
 	public void onPacketData(INetworkManager manager,
 			Packet250CustomPayload packet, Player player) {
 		int x, y, z, meta;
 		System.out.println("Packet Get");
		if(packet.channel == "fuj1nAMetaPacket"){
 			System.out.println("Packet Recognize");
 	        DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(packet.data));
 	        try {
                 meta = inputStream.readShort();
                 x = inputStream.readInt();
                 y = inputStream.readInt();
                 z = inputStream.readInt();
         } catch (IOException e) {
                 e.printStackTrace();
                 return;
         }
 	        EntityPlayerMP playerMP = (EntityPlayerMP)player;
 			World world = playerMP.worldObj;
 			world.setBlockMetadataWithNotify(x, y, z, meta, 2);
 			System.out.println("Packet Run");
 			world.updateAllLightTypes(x, y, z);
 	    	world.updateAllLightTypes(x, y - 1, z);
 	    	world.updateAllLightTypes(x, y + 1, z);
 	    	world.updateAllLightTypes(x - 1, y, z);
 	    	world.updateAllLightTypes(x + 1, y, z);
 	    	world.updateAllLightTypes(x, y, z - 1);
 	    	world.updateAllLightTypes(x, y, z + 1);
 		}
 		
 	}
 
 }
