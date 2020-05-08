 package zensiert1997.realphysics.core.proxy;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.world.World;
 import net.minecraftforge.client.EnumHelperClient;
 import net.minecraftforge.client.MinecraftForgeClient;
 import zensiert1997.realphysics.core.RealPhysics;
 import zensiert1997.realphysics.core.config.CoreConfigs;
 import zensiert1997.realphysics.core.gui.GuiWritingDesk;
 import zensiert1997.realphysics.core.models.RenderWritingDesk;
 import zensiert1997.realphysics.core.models.RenderingHandler;
 import zensiert1997.realphysics.core.registry.BlockRegistry;
 import zensiert1997.realphysics.core.registry.CoreLanguagePack;
 import zensiert1997.realphysics.core.registry.GuiRegistry;
 import zensiert1997.realphysics.core.research.BaseInvention;
 import zensiert1997.realphysics.core.research.BaseInvention.EnumColor;
 import zensiert1997.realphysics.core.tile.TileWritingDesk;
 import cpw.mods.fml.client.registry.ClientRegistry;
 import cpw.mods.fml.client.registry.RenderingRegistry;
 
 public class ClientProxy extends CommonProxy {
 	
 	@Override
 	public void registerRenderThings() {
 		RealPhysics.renderId = RenderingRegistry.getNextAvailableRenderId();
 		preloadTextures();
 		registerRenderers();
 		RenderingRegistry.registerBlockHandler(RealPhysics.renderId, RenderingHandler.instance());
 		registerEnumConstants();
 	}
 	
 	private void preloadTextures() {
 		MinecraftForgeClient.preloadTexture(RealPhysics.blockTextures);
 		MinecraftForgeClient.preloadTexture(RealPhysics.itemTextures);
 		MinecraftForgeClient.preloadTexture(RealPhysics.modelWritingDesk);
 		MinecraftForgeClient.preloadTexture(RealPhysics.guiWritingDesk);
 		MinecraftForgeClient.preloadTexture(RealPhysics.guiTexture);
 	}
 	
 	private void registerRenderers() {
 		RenderWritingDesk writingDeskRenderer = new RenderWritingDesk();
 		ClientRegistry.bindTileEntitySpecialRenderer(TileWritingDesk.class, writingDeskRenderer);
 		RenderingHandler.instance().registerInventoryRenderer(BlockRegistry.blockWritingDesk.blockID, writingDeskRenderer);
 	}
 	
 	private void registerEnumConstants() {
 		for(int i = 0; i < BaseInvention.EnumColor.values().length; i++) {
 			EnumColor color = BaseInvention.EnumColor.values()[i];
 			EnumHelperClient.addRarity(color.getName(), color.col, color.friendlyName);
 		}
 	}
 	
 	@Override
 	public void registerLanguage() {
 		CoreLanguagePack.load();
 	}
 	
 	@Override
 	public void registerGuis() {
		super.registerGuis();
 		GuiRegistry.instance().registerClientGui(CoreConfigs.guiIdWritingDesk, new GuiWritingDesk.GuiHandler());
 	}
 	
 	@Override
 	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
 		return GuiRegistry.instance().getClientGui(ID, player, world, x, y, z);
 	}
 	
 }
