 package teamm.mods.virtious.proxy;
 
 import net.minecraftforge.client.MinecraftForgeClient;
 import teamm.mods.virtious.entity.EntityBurfalaunt;
 import teamm.mods.virtious.entity.EntityLaser;
 import teamm.mods.virtious.entity.EntityNative;
 import teamm.mods.virtious.entity.EntityNativeSkeleton;
 import teamm.mods.virtious.entity.item.EntityStickyBomb;
 import teamm.mods.virtious.entity.item.EntityVirtiousFishHook;
 import teamm.mods.virtious.lib.VirtiousItems;
 import teamm.mods.virtious.model.ModelBurfalaunt;
 import teamm.mods.virtious.model.ModelNative;
 import teamm.mods.virtious.renderer.RenderBurfalaunt;
 import teamm.mods.virtious.renderer.RenderLaser;
 import teamm.mods.virtious.renderer.RenderNative;
 import teamm.mods.virtious.renderer.RenderNativeSkeleton;
 import teamm.mods.virtious.renderer.RenderStickyBomb;
 import teamm.mods.virtious.renderer.RenderVirtiousFish;
 import teamm.mods.virtious.renderer.StickyBombItemRenderer;
 import cpw.mods.fml.client.registry.RenderingRegistry;
 
 public class ClientProxy extends CommonProxy
 {
 	@Override
 	public void registerRenderThings()
 	{
 		RenderingRegistry.registerEntityRenderingHandler(EntityStickyBomb.class, new RenderStickyBomb());
 		RenderingRegistry.registerEntityRenderingHandler(EntityVirtiousFishHook.class, new RenderVirtiousFish());
 		
 		MinecraftForgeClient.registerItemRenderer(VirtiousItems.itemStickyBomb.itemID, new StickyBombItemRenderer());
 
 		RenderingRegistry.registerEntityRenderingHandler(EntityBurfalaunt.class, new RenderBurfalaunt(new ModelBurfalaunt(), 1F));
 		RenderingRegistry.registerEntityRenderingHandler(EntityNative.class, new RenderNative(new ModelNative(), 1F));
 		RenderingRegistry.registerEntityRenderingHandler(EntityNativeSkeleton.class, new RenderNativeSkeleton(new ModelNative(), 1F));
 	
 		RenderingRegistry.registerEntityRenderingHandler(EntityLaser.class, new RenderLaser());
 	}
 }
