 package org.pixotic.xcraft.core.proxy;
 
 import net.minecraftforge.client.MinecraftForgeClient;
 
import org.pixotic.xcraft.client.TileEntityTest3DBlockRenderer;
import org.pixotic.xcraft.client.renderer.item.ItemTest3DBlockRenderer;
import org.pixotic.xcraft.tileentity.TileTest3DBlock;
 
 import cpw.mods.fml.client.registry.ClientRegistry;
 import cpw.mods.fml.client.registry.RenderingRegistry;
 
 
 public class ClientProxy extends CommonProxy
 {
     public static final int renderID = RenderingRegistry.getNextAvailableRenderId();
     
     @Override
     public void InitRendering()
     {
        ClientRegistry.bindTileEntitySpecialRenderer(TileTest3DBlock.class, new TileEntityTest3DBlockRenderer());
        MinecraftForgeClient.registerItemRenderer(2223, new ItemTest3DBlockRenderer());
     }
 }
