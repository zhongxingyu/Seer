 package mods.minecraft.darth.dc;
 
 
 import net.minecraft.creativetab.CreativeTabs;
 
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PostInit;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.GameRegistry;
 
 import mods.minecraft.darth.dc.block.ModBlocks;
 import mods.minecraft.darth.dc.core.handlers.LocalizationHandler;
 import mods.minecraft.darth.dc.core.proxy.CommonProxy;
 import mods.minecraft.darth.dc.creativetabs.CreativeTabDC;
 import mods.minecraft.darth.dc.item.ModItems;
 import mods.minecraft.darth.dc.lib.Reference;
import mods.minecraft.darth.dc.world.OreGeneration;
 
 
 /**
  * DiscoveryCraft
  * 
  * @author Darth_Feder
  * 
  * 
  */
 
 
 @Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION, dependencies = Reference.DEPENDENCIES, acceptedMinecraftVersions = Reference.MC_VERSIONS, modExclusionList = Reference.EXCLUSION_MODS)
 @NetworkMod(clientSideRequired = true, serverSideRequired = false)
 public class DiscoveryCraft
 {
 
         @Instance(Reference.MOD_ID)
         public static DiscoveryCraft instance;
         
         @SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
         public static CommonProxy proxy;
         
         
         public static CreativeTabs tabDC = new CreativeTabDC(Reference.MOD_ID);
 
         
         @PreInit
         public void preInit(FMLPreInitializationEvent event)
         {
             
             //Load Language Localization Files
             LocalizationHandler.loadLanguages();
             
             //Initialize Blocks
             ModBlocks.init();
             
             //Initialize Items
             ModItems.init();
             
         }
     
     
         @Init
         public void load(FMLInitializationEvent event)
         {
            //Register Ore Generation
            GameRegistry.registerWorldGenerator(new OreGeneration());
             
         }
         
         
         @PostInit
         public void postInit(FMLPostInitializationEvent event)
         {
             
             
         }
     
 }
