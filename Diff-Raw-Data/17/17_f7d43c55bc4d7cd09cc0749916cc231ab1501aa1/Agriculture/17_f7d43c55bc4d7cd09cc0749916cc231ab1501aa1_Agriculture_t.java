 package com.teammetallurgy.agriculture;
 
 import com.teammetallurgy.agriculture.worldgen.WorldGenSalt;
 import com.teammetallurgy.agriculture.worldgen.WorldGenSpice;
 
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.event.FMLServerStartingEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
 import cpw.mods.fml.common.registry.TickRegistry;
 import cpw.mods.fml.relauncher.Side;
 
 @Mod(name = Agriculture.MODNAME, modid = Agriculture.MODID, version = Agriculture.VERSION)
 @NetworkMod(channels =
 { Agriculture.MODID }, packetHandler = PacketHandler.class)
 public class Agriculture
 {
 	public static final String MODID = "Agriculture";
 	public static final String MODNAME = "Agriculture";
 	public static final String VERSION = "1.0.0";
 
 	@Instance(MODID)
 	public static Agriculture instance;
 
 	@SidedProxy(serverSide = "com.teammetallurgy.agriculture.CommonProxy", clientSide = "com.teammetallurgy.agriculture.ClientProxy")
 	public static CommonProxy proxy;
 
 	public static AgricultureTab tab;
 
 	@EventHandler
 	public void commands(FMLServerStartingEvent event)
 	{
 
 	}
 
 	@EventHandler
 	public void init(FMLInitializationEvent event)
 	{
 		tab = new AgricultureTab("Agriculture");
		LanguageRegistry.instance().addStringLocalization("itemGroup.Agriculture", "en_US", "Agriculture");
 		AgricultureItems.init();
 		AgricultureBlocks.init();
 		tab.setItemID(AgricultureBlocks.oven.blockID);
 
 		AgricultureItems.setupItems();
 
 		GameRegistry.registerWorldGenerator(new WorldGenSalt(4));
 		GameRegistry.registerWorldGenerator(new WorldGenSpice(AgricultureBlocks.cinnamon.blockID));
 		GameRegistry.registerWorldGenerator(new WorldGenSpice(AgricultureBlocks.vanilla.blockID));
 
 		TickRegistry.registerTickHandler(new TickHandler(), Side.SERVER);
 		NetworkRegistry.instance().registerGuiHandler(instance, proxy);
 		proxy.registerEventHandlers();
 		proxy.registerRenderers();
 	}
 
 	@EventHandler
 	public void postInit(FMLPostInitializationEvent event)
 	{
 
 	}
 
 	@EventHandler
 	public void preInit(FMLPreInitializationEvent event)
 	{
 
 	}
 }
