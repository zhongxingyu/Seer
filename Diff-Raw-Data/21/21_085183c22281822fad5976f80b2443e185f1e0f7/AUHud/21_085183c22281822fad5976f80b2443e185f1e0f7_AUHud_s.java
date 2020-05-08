 package com.qzx.au.hud;
 
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
 import cpw.mods.fml.common.network.NetworkRegistry;
 
 import com.qzx.au.hud.Cfg;
 import com.qzx.au.hud.GuiHandler;
 
 //NetworkMod(clientSideRequired = true, serverSideRequired = true, channels = { "AUHudRandom" }, packetHandler = PacketHandler.class)
 
@Mod(modid="AUHud", name="Altered Unification HUD", version="1aug2013-r1")
@NetworkMod(clientSideRequired = true, serverSideRequired = true)
 public class AUHud {
 	@Instance("AUHud")
 	public static AUHud instance;
 
 	@SidedProxy(clientSide="com.qzx.au.hud.ClientProxy", serverSide="com.qzx.au.hud.CommonProxy")
 	public static CommonProxy proxy;
 
 	@PreInit
 	public void preInit(FMLPreInitializationEvent event){
 		Cfg.init(event);
 	}
 
 	@Init
 	public void load(FMLInitializationEvent event){
 		proxy.registerRenderers();
 		proxy.registerHandlers();
 		NetworkRegistry.instance().registerGuiHandler(this, new GuiHandler());
 	}
 
 	@PostInit
 	public void postInit(FMLPostInitializationEvent event){}
 }
