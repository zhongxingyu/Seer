 package gaarnik.bsa.common;
 
 import gaarnik.bsa.common.block.BSABlocks;
 import gaarnik.bsa.common.item.BSAItems;
 
 import java.util.Random;
 
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.world.World;
 import net.minecraftforge.common.Configuration;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.event.FMLServerStartingEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 @Mod(modid = "BSAMod")
 @NetworkMod(clientSideRequired = true, serverSideRequired = true)
 public class BSAMod {
 	// *******************************************************************
 	@SidedProxy(clientSide = "gaarnik.bsa.client.BSAClientProxy", serverSide = "gaarnik.bsa.server.BSAServerProxy")
 	public static BSACommonProxy proxy;
 	
 	@Instance("BSAMod")
 	public static BSAMod instance = new BSAMod();
 	
 	public static Configuration config;
 	
 	public static CreativeTabs tabs = new BSACreativeTabs("bsaTab");
 
 	public static Random rand = new Random();
 	
 	private BSAGuiHandler engGuiHandler = new BSAGuiHandler();
 
 	// *******************************************************************
 	@EventHandler
 	public void preload(FMLPreInitializationEvent event) {
 		BSAMod.config = new Configuration(event.getSuggestedConfigurationFile());
 		BSAMod.config.load();
 	}
 
 	@EventHandler
 	public void load(FMLInitializationEvent event) {
 		proxy.registerClientTickHandler();
 		proxy.registerDrawBlockHighlightEvent();
 		
 		LanguageRegistry.instance().addStringLocalization("itemGroup.bsaTab", "en_US", "BSA Mod");
 		
 		BSAItems.registry();
 		BSABlocks.registry();
 		
 		NetworkRegistry.instance().registerGuiHandler(this, engGuiHandler);
 	}
 
 	@EventHandler
 	public void postload(FMLPostInitializationEvent event) {
 		BSAMod.config.save();
 	}
 
 	@EventHandler
 	public void serverStarting(FMLServerStartingEvent event) {
 		
 	}
 
 	// *******************************************************************
 	public static boolean explodeMachineAt(World world, int x, int y, int z) {
 		try {
 			Class<?> mainIC2Class = Class.forName("IC2");
 			mainIC2Class.getMethod("explodeMachineAt", World.class, Integer.TYPE, Integer.TYPE, Integer.TYPE).invoke(null, world, x, y, z);
 			return true;
 		}
 		catch (Exception e) {
 			return false;
 		}
     }
 }
