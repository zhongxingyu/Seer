 package bspkrs.crystalwing.fml;
 
 import bspkrs.bspkrscore.fml.bspkrsCoreMod;
 import bspkrs.crystalwing.CWSettings;
 import bspkrs.util.Const;
 import bspkrs.util.ModVersionChecker;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.Metadata;
 import cpw.mods.fml.common.ModMetadata;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 
@Mod(name = "CrystalWing", modid = "CrystalWing", version = CWSettings.VERSION_NUMBER, dependencies = "required-after:bspkrsCore", useMetadata = true)
 public class CrystalWingMod
 {
     public static ModVersionChecker versionChecker;
     private String                  versionURL = Const.VERSION_URL + "/Minecraft/" + Const.MCVERSION + "/crystalWingForge.version";
     private String                  mcfTopic   = "http://www.minecraftforum.net/topic/1009577-";
     
     @Metadata(value = "CrystalWing")
     public static ModMetadata       metadata;
     
     @SidedProxy(clientSide = "bspkrs.crystalwing.fml.ClientProxy", serverSide = "bspkrs.crystalwing.fml.CommonProxy")
     public static CommonProxy       proxy;
     
     @Instance(value = "CrystalWing")
     public static CrystalWingMod    instance;
     
     @EventHandler
     public void preInit(FMLPreInitializationEvent event)
     {
         metadata = event.getModMetadata();
         CWSettings.loadConfig(event.getSuggestedConfigurationFile());
         
         CWSettings.registerStuff();
         
         if (bspkrsCoreMod.instance.allowUpdateCheck)
         {
             versionChecker = new ModVersionChecker(metadata.name, metadata.version, versionURL, mcfTopic);
             versionChecker.checkVersionWithLogging();
         }
     }
     
     @EventHandler
     public void init(FMLInitializationEvent event)
     {
         proxy.registerTickHandler();
     }
 }
