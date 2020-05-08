 package mod.culegooner;
 
 import mod.culegooner.proxy.CommonProxy;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 
 
 @Mod(modid = CreeperBurnCoreLib.MOD_ID, name = CreeperBurnCoreLib.MOD_NAME, version = CreeperBurnCoreLib.VERSION_NUMBER)
 //, dependencies = CreeperBurnModLib.DEPENDENCIES, certificateFingerprint = CreeperBurnModLib.FINGERPRINT)
 @NetworkMod(clientSideRequired = true, serverSideRequired = false)
 
 public class CreeperBurnCoreBase {
 
 	
 	@SidedProxy(
 			clientSide = CreeperBurnCoreLib.CLIENT_PROXY_CLASS,
 			serverSide = CreeperBurnCoreLib.SERVER_PROXY_CLASS)
 	public static CommonProxy proxy;
 	
 	
 	
 	
 	@EventHandler
     public void preModInit(FMLPreInitializationEvent event) {
     }
  
 	@EventHandler
     public void modInit(FMLInitializationEvent event) {
     	
     }
     
 	@EventHandler
     public void modsLoaded(FMLPostInitializationEvent event) {
     	
     }
 	
     
 }
