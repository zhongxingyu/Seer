 package mods.themike.modjam.proxy;
 
 import mods.themike.modjam.handler.SimpleBlockRenderingHandler;
 import mods.themike.modjam.render.RenderCarvingStone;
 import mods.themike.modjam.tile.TileEntityCarvingStone;
 import cpw.mods.fml.client.registry.ClientRegistry;
 import cpw.mods.fml.client.registry.RenderingRegistry;
 
 public class ClientProxy implements IProxy {
 	
 	public static SimpleBlockRenderingHandler handler = new SimpleBlockRenderingHandler();

 	@Override
 	public void init() {
		RenderingRegistry.registerBlockHandler(handler);
 		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCarvingStone.class, new RenderCarvingStone());
 	}
 
 }
