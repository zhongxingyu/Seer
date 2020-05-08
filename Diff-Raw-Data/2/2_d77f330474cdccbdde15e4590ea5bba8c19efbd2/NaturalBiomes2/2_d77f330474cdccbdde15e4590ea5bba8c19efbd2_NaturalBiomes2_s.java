 package NaturalBiomes2;
 
 import NaturalBiomes2.Config;
 import NaturalBiomes2.Biomes.BiomeHandler;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
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
 
 @Mod(modid="NaturalBiomes2", name="Natural Biomes 2", version="0.5")
 @NetworkMod(clientSideRequired=true, serverSideRequired=false)
 public class NaturalBiomes2 {
 
         @Instance("NaturalBiomes2")
         public static NaturalBiomes2 instance;
     
         @EventHandler
         public void preInit(FMLPreInitializationEvent event) {
         	Config.initialize(event.getSuggestedConfigurationFile());
     		
     		Config.save();
     		
     		BiomeHandler.init();
         }
         
         @EventHandler
         public void load(FMLInitializationEvent event) {   
     		
         }
         
         @EventHandler
         public void postInit(FMLPostInitializationEvent event) {
        	//ForestryAPIManager.init();
         }
 }
