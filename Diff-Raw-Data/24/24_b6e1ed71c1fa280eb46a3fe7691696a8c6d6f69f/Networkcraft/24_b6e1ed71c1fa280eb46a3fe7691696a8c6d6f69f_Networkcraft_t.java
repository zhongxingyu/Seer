 package ghost606.networkcraft;
 
 import ghost606.networkcraft.blocks.Blocks;
 import ghost606.networkcraft.configuration.ConfigHandler;
 import ghost606.networkcraft.core.EventHandlerCore;
 import ghost606.networkcraft.gui.GuiHandler;
 import ghost606.networkcraft.information.ModInfo;
 import ghost606.networkcraft.network.PacketHandler;
 import ghost606.networkcraft.proxies.CommonProxy;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraftforge.common.MinecraftForge;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 @Mod(modid=ModInfo.ModId, name=ModInfo.ModName, version=ModInfo.ModVersion)
 @NetworkMod(channels= {ModInfo.ModChannel}, clientSideRequired=true, serverSideRequired=false, packetHandler = PacketHandler.class)
 public class Networkcraft
 {
 	@Instance(ModInfo.ModId)
 	public static Networkcraft instance;
 	
 	@SidedProxy(clientSide = "ghost606.networkcraft.proxies.ClientProxy", serverSide = "ghost606.networkcraft.proxies.CommonProxy")
 	public static CommonProxy proxy;
 	
 	public static CreativeTabs tabNetworkcraft = new CreativeTabs("Networkcraft");
 	
 	public static ConfigHandler config;
 	
 	@EventHandler
 	public void preInit(FMLPreInitializationEvent fml)
 	{
 		ConfigHandler.init(fml.getSuggestedConfigurationFile());
 		Blocks.initBlocks();
 	}
 	@EventHandler
 	public void init(FMLInitializationEvent fml)
 	{
		MinecraftForge.EVENT_BUS.register(new EventHandlerCore());
 		LanguageRegistry.instance().addStringLocalization("itemGroup.Networkcraft", "en_US", "Networkcraft");
 		Blocks.addNames();
 		Blocks.registerTileEntities();
 		new GuiHandler();
 	}
 	@EventHandler
 	public void postInit(FMLPostInitializationEvent fml)
 	{
 		
 	}
 
 }
