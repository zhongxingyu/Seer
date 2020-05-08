 package entanglecraft;
 
 import java.io.File;
 
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.SoundManager;
 import net.minecraft.src.World;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.network.IGuiHandler;
 import entanglecraft.blocks.ContainerGenericDestination;
 import entanglecraft.blocks.ContainerLambdaMiner;
 import entanglecraft.blocks.TileEntityGenericDestination;
 import entanglecraft.blocks.TileEntityLambdaMiner;
 import entanglecraft.gui.EnumGui;
 import entanglecraft.gui.GuiGenericDestination;
 import entanglecraft.gui.GuiLambdaMiner;
 import net.minecraftforge.common.MinecraftForge;
 
 public class CommonProxy implements IGuiHandler
 {
 	public static boolean isServer = true;
	public static void registerClientSide(){
 	}
 	
 	public static void registerDistanceSaver(DistanceHandler dh) {
 		MinecraftForge.EVENT_BUS.register(dh);
 	}
 	
 	public File getWorldSaveDir(World world){
 		File workingDir = new File(".\\\\world\\");
 		return workingDir;
 	}
 	
 	@Override
 	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
 	  {
 		if (ID == EnumGui.GenericDestination.getIndex()) {
 			TileEntityGenericDestination tileEntityGD = (TileEntityGenericDestination) world.getBlockTileEntity(x, y, z);
 			return new ContainerGenericDestination(player.inventory, tileEntityGD);
 		
 		}
 		
 		else if (ID == EnumGui.LambdaMiner.getIndex()){
 			TileEntityLambdaMiner tileEntitylM = (TileEntityLambdaMiner) world.getBlockTileEntity(x, y, z);
 			return new ContainerLambdaMiner(player.inventory, tileEntitylM);
 		}
 		else return null;
 	  }
 
 	@Override
 	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
 	  {
 		
 		if (ID == EnumGui.GenericDestination.getIndex()) {
 			TileEntityGenericDestination tileEntityGD = (TileEntityGenericDestination) world.getBlockTileEntity(x, y, z);
 			return new GuiGenericDestination(player.inventory, tileEntityGD);
 		}
 		
 		else if (ID == EnumGui.LambdaMiner.getIndex()){
 			TileEntityLambdaMiner tileEntitylM = (TileEntityLambdaMiner) world.getBlockTileEntity(x, y, z);
 			return new GuiLambdaMiner(player.inventory, tileEntitylM);
 		}
 		else return null;	
 	  }
 
 
 }
