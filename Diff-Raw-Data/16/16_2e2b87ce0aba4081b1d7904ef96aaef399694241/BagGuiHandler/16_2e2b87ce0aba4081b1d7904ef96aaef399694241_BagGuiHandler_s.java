 package taigore.inventorysaver;
 
 import java.util.List;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.world.World;
 import taigore.inventorysaver.client.GuiBag;
 import cpw.mods.fml.common.network.IGuiHandler;
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.common.network.Player;
 
 public class BagGuiHandler implements IGuiHandler
 {
 	@Override
 	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
 	{
 		if(ID == 1)
 		{
 			List<EntityBag> bagsInTheBlock = world.getEntitiesWithinAABB(EntityBag.class, AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1, z + 1));
 			
 			for(EntityBag toCheck : bagsInTheBlock)
 			{
 				if(toCheck.clicker == player)
 				{
					PacketDispatcher.sendPacketToPlayer(new Packet250BagSync(toCheck), (Player)player);
 					return new ContainerBag(player.inventory, toCheck);
 				}
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
 	{
 		if(ID == 1)
 		{
 			List<EntityBag> bagsInTheBlock = world.getEntitiesWithinAABB(EntityBag.class, AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1, z + 1));
 			
 			for(EntityBag toCheck : bagsInTheBlock)
 			{
 				if(toCheck.clicker == player)
 				{
 					toCheck.clicker = null;
 					
 					return new GuiBag(player.inventory, toCheck);
 				}
 			}
 		}
 		return null;
 	}
 }
