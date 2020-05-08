 package ictrobot.gems;
 
 import java.io.File;
 
 import ictrobot.core.helper.config.ConfigHelper;
 import ictrobot.core.proxy.*;
 import ictrobot.gems.module.*;
 import ictrobot.gems.*;
 import ictrobot.gems.module.compat.*;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 
 @SuppressWarnings("unused")
 @Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION_NUMBER)
 @NetworkMod(clientSideRequired = true, serverSideRequired = false, channels={"GemsJetpack"}, packetHandler = GemsPacketHandler.class)
 public class Gems {
   @Instance(Reference.MOD_ID)
   public static Gems instance;
 
   public boolean LapisGemEnable;
   public boolean VanillaPaxelEnable;
   public boolean ColouredGemsEnable;
   public boolean MagneticModuleEnable;
   public boolean CompatOreEnable;
   public boolean OtherIngotsEnable;
   
   //FOR CONFIG HELPER
   public static int ModulesRegistered = -1;
   public static File configfile;
   public static File configdir;
   
   @SidedProxy(clientSide = "ictrobot.core.proxy.ClientProxy", serverSide = "ictrobot.core.proxy.CommonProxy")
   public static CommonProxy proxy;
 
   @EventHandler
   public void preInit(FMLPreInitializationEvent event) {
     configfile = event.getSuggestedConfigurationFile();
     configdir = event.getModConfigurationDirectory() ;
     
     ConfigHelper.file("Main");
     
     LapisGemEnable = ConfigHelper.module("LapisGemEnable", true);
     VanillaPaxelEnable = ConfigHelper.module("VanillaPaxelEnable", true);
     ColouredGemsEnable = ConfigHelper.module("ColouredGemsEnable", true);
     MagneticModuleEnable = ConfigHelper.module("MagneticModuleEnable", true);
     CompatOreEnable = ConfigHelper.module("CompatOreEnable", false);
     OtherIngotsEnable = ConfigHelper.module("OtherIngotsEnable", true);
 
     ConfigHelper.save();
       
     if (LapisGemEnable==true) {
       LapisGemModule.Config();
     }
     if (OtherIngotsEnable==true) {
     	OtherIngotsModule.Config(configfile);
     }
     if (VanillaPaxelEnable==true) {
       VanillaPaxelModule.Config();
     }
     if (ColouredGemsEnable==true) {
       ColourGemsModule.Config();
     }
     if (MagneticModuleEnable==true) {
       MagneticModule.Config();
     }
     if (CompatOreEnable==true) {
      CompatOreModule.Config();
     }
   }
 
   @EventHandler
   public void load(FMLInitializationEvent event) {
     proxy.registerRenderers();
     
     if (LapisGemEnable==true) {
       LapisGemModule.Settings();
     }
     if (OtherIngotsEnable==true) {
     	OtherIngotsModule.Settings();
     }
     if (VanillaPaxelEnable==true) {
         VanillaPaxelModule.Settings();
     }
     if (ColouredGemsEnable==true) {
       ColourGemsModule.Settings();
       ColourGemsModule.WorldGen();
     }
     if (MagneticModuleEnable==true) {
       MagneticModule.Settings();
       MagneticModule.WorldGen();
     }
     if (CompatOreEnable==true) {
       CompatOreModule.Settings();
       CompatOreModule.WorldGen();
     }
   }
 
   @EventHandler
   public void postInit(FMLPostInitializationEvent event) {
 
     if (LapisGemEnable==true) {
       LapisGemModule.Register();
     }
     if (OtherIngotsEnable==true) {
     	OtherIngotsModule.Register();
     }
     if (VanillaPaxelEnable==true) {
       VanillaPaxelModule.Register();
     }
     if (ColouredGemsEnable==true) {
       ColourGemsModule.Register();
     }
     if (MagneticModuleEnable==true) {
       MagneticModule.Register();
     }
     if (CompatOreEnable==true) {
       CompatOreModule.Register();
     }
   }
   
 }
