 package BL2.client;
 
 import java.awt.Color;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.EOFException;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.util.Iterator;
 import java.util.zip.GZIPInputStream;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.src.Entity;
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.EntityPlayerMP;
 import net.minecraft.src.INetworkManager;
 import net.minecraft.src.Item;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.ModLoader;
 import net.minecraft.src.Packet250CustomPayload;
 import net.minecraft.src.TileEntity;
 import net.minecraft.src.World;
 import net.minecraft.src.WorldClient;
 import BL2.common.NetworkHandler;
 import BL2.common.ShieldFX;
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.common.network.Player;
 
 public class NetworkHandlerClient extends NetworkHandler
 {
 	/*public void sendParticlePacket(World world, double x, double y, double z, int inventoryIndex)
 	{
 		if(FMLCommonHandler.instance().getEffectiveSide().isServer())
 		{
 			super.sendParticlePacket(world, x, y, z, inventoryIndex);
 		}else
 		{
 			EntityPlayer player = ModLoader.getMinecraftInstance().thePlayer;
 			 ShieldFX fx = new ShieldFX(world, player, player.getCurrentArmor(inventoryIndex), x, y, z, Color.CYAN);
 	         ModLoader.getMinecraftInstance().effectRenderer.addEffect(fx);
 		}
 	}*/
 	
 	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player p)
     {
 		System.out.println("recieved packet");
         ByteArrayInputStream in = new ByteArrayInputStream(packet.data, 1, packet.data.length - 1);
 
         try
         {
 
             switch (packet.data[0])
             {
                 case NetworkHandler.particlePacketID:
                 {
                 	System.out.println("spawned");
                	 DataInputStream din = new DataInputStream(in);
                 	int dimention = din.readInt();
                 	int index = din.readInt();
                     double x = din.readDouble();
                     double y = din.readDouble();
                     double z = din.readDouble();
                     double px = din.readDouble();
                     double py = din.readDouble();
                     double pz = din.readDouble();
                     WorldClient world = Minecraft.getMinecraft().theWorld;
 
                     if (world.provider.dimensionId != dimention)
                     {
                         return;
                     }
                    EntityPlayer player = world.getClosestPlayer(px, py, pz, 16);
                    ShieldFX fx = new ShieldFX(world, player, player.getCurrentArmor(index), x, y, z, Color.CYAN);
                    ModLoader.getMinecraftInstance().effectRenderer.addEffect(fx);
                 }
             }
         }
         catch (IOException var22)
         {
             var22.printStackTrace();
         }
     }
 }
