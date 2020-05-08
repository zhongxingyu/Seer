 package denoflionsx.DenPipes.Core;
 
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import denoflionsx.DenPipes.API.DenPipesAPI;
 import denoflionsx.DenPipes.API.Interfaces.IDenPipeAddon;
 import denoflionsx.DenPipes.Proxy.ProxyCommon;
 
@Mod(modid = "@NAME@", name = "@NAME@", version = "@VERSION@", dependencies = "@DEPENDS@")
 @NetworkMod(clientSideRequired = true, serverSideRequired = true)
 public class DenPipesCore {
 
     @Mod.Instance("@NAME@")
     public static Object instance;
     @SidedProxy(clientSide = "@PROXYCLIENT@", serverSide = "@PROXYSERVER@")
     public static ProxyCommon proxy;
 
     @Mod.PreInit
     public void preLoad(FMLPreInitializationEvent event) {
         proxy.setupConfig(event);
         proxy.findInternalAddons(event.getSourceFile());
         for (IDenPipeAddon a : DenPipesAPI.addons) {
             a.preinit(event);
         }
     }
 
     @Mod.Init
     public void load(FMLInitializationEvent event) {
         for (IDenPipeAddon a : DenPipesAPI.addons) {
             a.init(event);
         }
     }
 
     @Mod.PostInit
     public void modsLoaded(FMLPostInitializationEvent evt) {
         for (IDenPipeAddon a : DenPipesAPI.addons) {
             a.postinit(evt);
         }
     }
 }
