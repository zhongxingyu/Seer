 package BlueAtrium.InfiniteRealms.Team.Main;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.world.World;
 import cpw.mods.fml.client.registry.RenderingRegistry;
 import cpw.mods.fml.common.network.IGuiHandler;
 
 public class CommonProxyInfiniteRealms implements IGuiHandler{
 
 	public int addArmour(String armour)
 	{
 		return RenderingRegistry.addNewArmourRendererPrefix(armour);
 	}
 	
 	@Override
 	public Object getServerGuiElement(int ID, EntityPlayer player, World world,
 			int x, int y, int z) {
		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Object getClientGuiElement(int ID, EntityPlayer player, World world,
 			int x, int y, int z) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
