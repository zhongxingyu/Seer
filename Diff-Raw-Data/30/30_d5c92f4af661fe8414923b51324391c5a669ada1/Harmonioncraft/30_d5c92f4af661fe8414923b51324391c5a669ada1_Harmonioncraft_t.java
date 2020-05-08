 package Harmonioncraft.common;
 
 import net.minecraft.src.CommandHandler;
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.ItemStack;
 import net.minecraftforge.common.DimensionManager;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.entity.living.LivingDeathEvent;
 import net.minecraftforge.event.world.WorldEvent.Save;
 import cpw.mods.fml.common.*;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PostInit;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.Mod.ServerStarted;
 import cpw.mods.fml.common.Mod.ServerStarting;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.event.FMLServerStartingEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.network.NetworkMod.NULL;
 import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;
 import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
 import Harmonioncraft.client.network.NetworkManagerClient;
 import Harmonioncraft.common.block.ModBlocks;
 import Harmonioncraft.common.commands.CommandHMCV;
 import Harmonioncraft.common.core.CommonProxy;
 import Harmonioncraft.common.core.handlers.AddonHandler;
 import Harmonioncraft.common.core.handlers.ConfigurationHandler;
 import Harmonioncraft.common.core.handlers.LocalizationHandler;
import Harmonioncraft.common.core.handlers.VersionCheckTickHandler;
 import Harmonioncraft.common.core.helper.LogHelper;
 import Harmonioncraft.common.core.helper.VersionHelper;
 import Harmonioncraft.common.dimension.WorldProviderHarmonioncraft;
 import Harmonioncraft.common.item.ModItems;
 import Harmonioncraft.common.lib.ConfigurationSettings;
 import Harmonioncraft.common.lib.Reference;
 import Harmonioncraft.common.network.NetworkManager;
 
 /**
  * HarmonionCraft
  * 
  * Main mod class for the Minecraft mod HarmonionCraft
  * 
  * @author Alexbegt,DJP
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  * 
  */
 @Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION)
 @NetworkMod(
         clientSideRequired = true,
         clientPacketHandlerSpec =       @SidedPacketHandler(
                 channels = {Reference.CHANNEL_NAME},
                 packetHandler = NetworkManagerClient.class
                 ),
         serverPacketHandlerSpec =       @SidedPacketHandler(
                 channels = {Reference.CHANNEL_NAME},
                 packetHandler = NetworkManager.class
                 )
 )
 public class Harmonioncraft {
 	
 	@Instance(Reference.MOD_ID)
 	public static Harmonioncraft instance;
 	
 	@SidedProxy(
 			clientSide = Reference.CLIENT_PROXY_CLASS,
 			serverSide = Reference.SERVER_PROXY_CLASS
 	)
     public static CommonProxy proxy;
 	
 	@SidedProxy(
 			clientSide = Reference.CLIENT_NETWORK_CLASS,
 			serverSide = Reference.SERVER_NETWORK_CLASS
 	)
     public static NetworkManager network;
 	
 	@PreInit
 	public void preInit(FMLPreInitializationEvent event) {	
 		
 		// Initialize the configuration
         ConfigurationHandler.init(event.getSuggestedConfigurationFile());
 		
      // Conduct the version check and log the result
         if (ConfigurationSettings.ENABLE_VERSION_CHECK) {
         	VersionHelper.checkVersion();
         }
     	VersionHelper.logResult();
     	
     	// Initialize the log helper
     	LogHelper.init();
    	
    	 // Initialize the Version Check Tick Handler (Client only)
        TickRegistry.registerTickHandler(new VersionCheckTickHandler(), Side.CLIENT);
 		
 		// Load the localization files into the LanguageRegistry
     	LocalizationHandler.loadLanguages();
 
         // Register the KeyBinding Handler (Client only)
         proxy.registerKeyBindingHandler();
 
         // Register the Sound Handler (Client only)
         proxy.registerSoundHandler();
         
 	}
 	
 	@Init
 	public void load(FMLInitializationEvent evt) {
 		
 		/* Initialize the custom item rarity types */
         proxy.initCustomRarityTypes();
 
         /* Register the GUI Handler */
         NetworkRegistry.instance().registerGuiHandler(instance, proxy);
 
         /* Initialize mod blocks */
         ModBlocks.init();
         
         /* Initialize mod items */
         ModItems.init();
         
         /* Initialize mod tile entities */
         proxy.initTileEntities();
         
         /* Initialize custom rendering and pre-load textures (Client only) */
         proxy.initRenderingAndTextures();
         
         /* Block Smelting */
         ModBlocks.initBlockSmelting();
         ModBlocks.initBlockRecipes();
         
         /* Biome Adding */
         DimensionManager.registerProviderType(8, WorldProviderHarmonioncraft.class, true);
         DimensionManager.registerDimension(8, 8);
 		
 	}
 	
 	@PostInit
 	public void modsLoaded(FMLPostInitializationEvent evt) {
 		
 		// Initialize the Addon Handler
         AddonHandler.init(); 
         
 	}
 	
 	@ServerStarting
 	public void serverStarting(FMLServerStartingEvent event)
 	{
 		proxy.serverStarting(event.getServer());
 	}
 	
 }
