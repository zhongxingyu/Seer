 package MMC.neocraft.registry.proxy;
 
 import MMC.neocraft.block.NCblock;
 import MMC.neocraft.block.renderers.ItemRendererFermenter;
 import MMC.neocraft.tileentity.TileEntityFermenterBottom;
 import MMC.neocraft.tileentity.TileEntityFermenterTop;
 import MMC.neocraft.tileentity.TileEntityFermenterWhole;
 import MMC.neocraft.tileentity.renderers.TileEntityRendererFermenterBottom;
 import MMC.neocraft.tileentity.renderers.TileEntityRendererFermenterTop;
 import MMC.neocraft.tileentity.renderers.TileEntityRendererFermenterWhole;
 import cpw.mods.fml.client.registry.ClientRegistry;
 import net.minecraftforge.client.MinecraftForgeClient;
 
 public class ClientProxy extends CommonProxy
 {
 	public void registerRenderers()
 	{
 		
 		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFermenterBottom.class, new TileEntityRendererFermenterBottom());
 		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFermenterTop.class, new TileEntityRendererFermenterTop());
 		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFermenterWhole.class, new TileEntityRendererFermenterWhole());
 		MinecraftForgeClient.registerItemRenderer(NCblock.fermenterWhole.blockID, new ItemRendererFermenter());
 	}
 }
