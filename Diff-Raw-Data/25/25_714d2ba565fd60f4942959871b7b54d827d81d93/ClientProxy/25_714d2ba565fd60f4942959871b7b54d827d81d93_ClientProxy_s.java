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
 		
 
		ModLoader.addLocalization("tile.FantasyBrickMetals.PrometheumBrick.name", "Prometheum Brick");
		ModLoader.addLocalization("tile.FantasyBrickMetals.DeepIronBrick.name", "Deep Iron Brick");
		ModLoader.addLocalization("tile.FantasyBrickMetals.InfuscoliumBrick.name", "Infuscoliums Brick");
		ModLoader.addLocalization("tile.FantasyBrickMetals.OureclaseBrick.name", "Oureclase Brick");
		ModLoader.addLocalization("tile.FantasyBrickMetals.AredriteBrick.name", "Aredrite Brick");
		ModLoader.addLocalization("tile.FantasyBrickMetals.AstralSilverBrick.name", "Astral Silver Brick");
		ModLoader.addLocalization("tile.FantasyBrickMetals.CarmotBrick.name", "Carmot Brick");
		ModLoader.addLocalization("tile.FantasyBrickMetals.MithrilBrick.name", "Mithril Brick");
		ModLoader.addLocalization("tile.FantasyBrickMetals.RubraciumBrick.name", "Rubracium Brick");
		ModLoader.addLocalization("tile.FantasyBrickMetals.OrichalcumBrick.name", "Orichalcum Brick");
		ModLoader.addLocalization("tile.FantasyBrickMetals.AdamantineBrick.name", "Adamantine Brick");
		ModLoader.addLocalization("tile.FantasyBrickMetals.AtlarusBrick.name", "Atlarus Brick");
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
