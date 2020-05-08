 package pokefenn.vineacraft;
 
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.crafting.CraftingManager;
 import net.minecraftforge.common.MinecraftForge;
 
 import java.io.File;
 import java.util.Arrays;
 
 import pokefenn.proxy.CommonProxy;
 import pokefenn.block.ModBlocks;
 import pokefenn.configuration.ConfigurationHandler;
 import pokefenn.creativetab.CreativeTabVineac;
 import pokefenn.handlers.AddonHandler;
 import pokefenn.item.ModItems;
 import pokefenn.lib.Reference;
 
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PostInit;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLFingerprintViolationEvent;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLInterModComms;
 import cpw.mods.fml.common.event.FMLInterModComms.IMCEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.event.FMLServerStartingEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.TickRegistry;
 import cpw.mods.fml.relauncher.Side;
 
 
 
@SuppressWarnings("unused")
 
 @Mod( modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION)
 @NetworkMod(clientSideRequired=true, serverSideRequired=false)
 public class Vineacraft {
 
     @Instance(Reference.MOD_ID)
     public static Vineacraft instance;
     
     @SidedProxy(clientSide = "pokefenn.proxy.ClientProxy", serverSide = "pokefenn.proxy.CommonProxy")
     public static CommonProxy proxy;
     
     public static CreativeTabs tabsVineac = new CreativeTabVineac(
             CreativeTabs.getNextID(), Reference.MOD_ID);
 
     
     @PreInit
     public void preInit(FMLPreInitializationEvent event) {
        
      // Initialize the configuration
         ConfigurationHandler.init(new File(event.getModConfigurationDirectory()
                 .getAbsolutePath()
                 + File.separator
                 + Reference.CHANNEL_NAME
                 + File.separator + Reference.MOD_ID + ".cfg"));
 
         // Initialize the Render Tick Handler (Client only)
         proxy.registerRenderers();
 
         // Register the Sound Handler (Client only)
         proxy.registerSoundHandler();
 
         // Initialize mod blocks
         ModBlocks.init();
 
         // Initialize mod items
         ModItems.init();
         
     }
     
     @Init
     public void init(FMLInitializationEvent event){
         
         
      // Initialize mod tile entities
         proxy.registerTileEntities();
 
      // Initialize custom rendering and pre-load textures (Client only)
         proxy.initRenderingAndTextures();
         
         
         
         
     }
     
     @PostInit
     public void postInit(FMLPostInitializationEvent event) {
     
     
      // Initialize the Addon Handler
         AddonHandler.init();
         
     }
     
 
 }
