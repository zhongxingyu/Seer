 /**
  * CCM Modding, Compression
  */
 package ccm.compression;
 
 import static ccm.compression.utils.lib.Archive.MOD_ID;
import static ccm.compression.utils.lib.Archive.MOD_NAME;
 import static ccm.compression.utils.lib.Locations.CLIENT_PROXY;
 import static ccm.compression.utils.lib.Locations.SERVER_PROXY;
 import static ccm.nucleum.omnium.utils.lib.Archive.MOD_CHANNEL;
 import ccm.compression.block.CompressedType;
 import ccm.compression.block.ModBlocks;
 import ccm.compression.proxy.CommonProxy;
 import ccm.compression.utils.handler.config.CompresstionConfig;
 import ccm.nucleum.network.PacketHandler;
 import ccm.nucleum.omnium.CCMMod;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 
@Mod(modid = MOD_ID, name = MOD_NAME, useMetadata = true, dependencies = "required-after:nucleum_omnium")
 @NetworkMod(clientSideRequired = true, serverSideRequired = false, channels = MOD_CHANNEL, packetHandler = PacketHandler.class)
 public class Compression extends CCMMod
 {
     @Instance(MOD_ID)
     public static Compression instance;
 
     @SidedProxy(serverSide = SERVER_PROXY, clientSide = CLIENT_PROXY)
     public static CommonProxy proxy;
 
     @EventHandler
     public void preInit(final FMLPreInitializationEvent event)
     {
         loadMod(this, event, new CompresstionConfig());
         ModBlocks.init();
     }
 
     @EventHandler
     public void init(final FMLInitializationEvent event)
     {
         proxy.registerRenders();
         proxy.registerGUI();
 
         for (CompressedType type : CompressedType.values())
         {
             GameRegistry.addRecipe(type.getRecipe());
         }
     }
 }
