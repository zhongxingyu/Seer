 package shadow.mods.metallurgy.nether;
 
 import java.io.File;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.Item;
 import net.minecraft.src.ModLoader;
 import net.minecraft.src.TileEntity;
 import net.minecraft.src.World;
 import net.minecraftforge.client.MinecraftForgeClient;
 import cpw.mods.fml.client.FMLClientHandler;
 import cpw.mods.fml.client.registry.RenderingRegistry;
 import cpw.mods.fml.common.network.IGuiHandler;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import shadow.mods.metallurgy.*;
 import shadow.mods.metallurgy.precious.mod_MetallurgyPrecious;
 
 public class ClientProxy extends shadow.mods.metallurgy.nether.CommonProxy{
 	
 	public void addNames()
 	{
 		CoreClientProxy.addNamesToSet(mod_MetallurgyNether.alloys);
 		CoreClientProxy.addNamesToSet(mod_MetallurgyNether.ores);
 		
 		
 		for(String name : NF_GuiNetherForge.names)
			LanguageRegistry.instance().addStringLocalization("tile.NetherForge." + name + "Forge.name",  name + " Smelter");
 		
 		LanguageRegistry.instance().addStringLocalization("container.netherforge", "Nether Smelter");
 	}
 	
 	@Override
 	public void registerRenderInformation()
 	{
 		MinecraftForgeClient.preloadTexture("/shadow/MetallurgyNetherForges.png");
 		MinecraftForgeClient.preloadTexture("/shadow/MetallurgyNetherMetals.png");
 		MinecraftForgeClient.preloadTexture("/shadow/MetallurgyNetherAlloys.png");
 	}
 	
 	@Override
 	public World getClientWorld() {
 		return FMLClientHandler.instance().getClient().theWorld;
 	}
 
 	@Override
 	public File getMinecraftDir()
 	{
 		return Minecraft.getMinecraftDir();
 	}
 }
