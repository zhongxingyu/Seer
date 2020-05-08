 package shadow.mods.metallurgy.fantasy;
 
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
 import shadow.mods.metallurgy.fantasy.*;
 
 public class ClientProxy extends CommonProxy{
 
 	public void addArmor()
 	{
 		CoreClientProxy.addArmorToSet(mod_MetallurgyFantasy.alloys);
 		CoreClientProxy.addArmorToSet(mod_MetallurgyFantasy.ores);
 	}
 	
 	public void addNames()
 	{
 		CoreClientProxy.addNamesToSet(mod_MetallurgyFantasy.alloys);
 		CoreClientProxy.addNamesToSet(mod_MetallurgyFantasy.ores);
 		
		ModLoader.addLocalization("tile.MetalFantasyFurnace.PrometheumFurnace.name", "Prometheum Abstractor");
		ModLoader.addLocalization("tile.MetalFantasyFurnace.DeepIronFurnace.name", "Deep Iron Abstractor");
		ModLoader.addLocalization("tile.MetalFantasyFurnace.BlackSteelFurnace.name", "Black Steel Abstractor");
		ModLoader.addLocalization("tile.MetalFantasyFurnace.OureclaseFurnace.name", "Oureclase Abstractor");
		ModLoader.addLocalization("tile.MetalFantasyFurnace.AredriteFurnace.name", "Aredrite Abstractor");
		ModLoader.addLocalization("tile.MetalFantasyFurnace.MithrilFurnace.name", "Mithril Abstractor");
		ModLoader.addLocalization("tile.MetalFantasyFurnace.HaderothFurnace.name", "Haderoth Abstractor");
		ModLoader.addLocalization("tile.MetalFantasyFurnace.OrichalcumFurnace.name", "Orichalcum Abstractor");
		ModLoader.addLocalization("tile.MetalFantasyFurnace.AdamantineFurnace.name", "Adamantine Abstractor");
		ModLoader.addLocalization("tile.MetalFantasyFurnace.AtlarusFurnace.name", "Atlarus Abstractor");
		ModLoader.addLocalization("tile.MetalFantasyFurnace.TartariteFurnace.name", "Tartarite Abstractor");
				
 
 	}
 	
 	@Override
 	public void registerRenderInformation()
 	{
 		MinecraftForgeClient.preloadTexture("/shadow/MetallurgyFurnaces.png");
 		MinecraftForgeClient.preloadTexture("/shadow/MetallurgyFantasyMetals.png");
 		MinecraftForgeClient.preloadTexture("/shadow/MetallurgyFantasyAlloys.png");
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
