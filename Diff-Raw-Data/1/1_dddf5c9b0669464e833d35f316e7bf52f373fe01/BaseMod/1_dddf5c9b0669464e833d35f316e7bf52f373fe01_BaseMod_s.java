 package com.github.marvinside;
 
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
 
 @Mod(modid="mod_TestMod1", name="Test Mod 1", version="Alpha 1.0.0")
 @NetworkMod(clientSideRequired=true, serverSideRequired=false)
 public class BaseMod {
 
         // The instance of your mod that Forge uses.
         @Instance("tst_mod1")
         public static BaseMod instance;
         
         // Says where the client and server 'proxy' code is loaded.
         @SidedProxy(clientSide="com.github.marvinside.client.ClientProxy", serverSide="com.github.marvinside.CommonProxy")
         public static CommonProxy proxy;
         
         @PreInit
         public void preInit(FMLPreInitializationEvent event) {
         	// Stub Method
         	// some git testing
         	// some more testing
         	// mooooore :D
         	// test :D
         	// haha, lol ich kann schreiben xp ich kann sogar noch mehr schreiben trololoo o0
         }
         
         @Init
         public void load(FMLInitializationEvent event) {
                 proxy.registerRenderers();
         }
         
         @PostInit
         public void postInit(FMLPostInitializationEvent event) {
                 // Stub Method
         }
 }
