 package org.pixotic.xcraft.core.proxy;
 
 import net.minecraftforge.client.MinecraftForgeClient;
 
import org.pixotic.xcraft.client.TileEntityAlienPowerSourceRenderer;
import org.pixotic.xcraft.client.renderer.item.ItemAlienPowerSourceRenderer;
import org.pixotic.xcraft.tileentity.TileEntityAlienPowerSource;
 
 import cpw.mods.fml.client.registry.ClientRegistry;
 import cpw.mods.fml.client.registry.RenderingRegistry;
 
 
 public class ClientProxy extends CommonProxy
 {
     public static final int renderID = RenderingRegistry.getNextAvailableRenderId();
     
     @Override
     public void InitRendering()
     {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAlienPowerSource.class, new TileEntityAlienPowerSourceRenderer());
        MinecraftForgeClient.registerItemRenderer(2223, new ItemAlienPowerSourceRenderer());
     }
 }
