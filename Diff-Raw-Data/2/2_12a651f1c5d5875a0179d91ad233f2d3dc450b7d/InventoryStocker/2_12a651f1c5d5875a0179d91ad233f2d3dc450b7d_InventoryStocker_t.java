 /*******************************************************************************
  * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
  * Licensed as open source with restrictions. Please see attached LICENSE.txt.
  ******************************************************************************/
 
 package com.kaijin.InventoryStocker;
 
 import net.minecraft.src.Block;
 import net.minecraft.src.CreativeTabs;
 import net.minecraft.src.Item;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.Material;
 import net.minecraftforge.common.Configuration;
 import cpw.mods.fml.common.FMLLog;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 
 @Mod(modid = Info.MOD_ID, name=Info.MOD_NAME, version=Info.VERSION, dependencies = Info.MOD_DEPENDENCIES)
 @NetworkMod(clientSideRequired = true, serverSideRequired = false,
 clientPacketHandlerSpec = @SidedPacketHandler(channels = {Info.PACKET_CHANNEL}, packetHandler = ClientPacketHandler.class),
 serverPacketHandlerSpec = @SidedPacketHandler(channels = (Info.PACKET_CHANNEL), packetHandler = ServerPacketHandler.class))
 public class InventoryStocker
 {
 	@SidedProxy(clientSide = Info.PROXY_CLIENT, serverSide = Info.PROXY_SERVER)
 	public static CommonProxy proxy; //This object will be populated with the class that you choose for the environment
 	
 	@Instance(Info.MOD_ID)
 	public static InventoryStocker instance; //The instance of the mod that will be defined, populated, and callable
 
 	@PreInit
 	public static void preInit(FMLPreInitializationEvent event)
 	{
 		try
 		{
 			Configuration configuration = new Configuration(event.getSuggestedConfigurationFile());
 			configuration.load();
 			Info.blockIDInventoryStocker = configuration.getBlock("InventoryStocker", 2490).getInt();
 			Info.isDebugging = Boolean.parseBoolean((configuration.get(configuration.CATEGORY_GENERAL, "debug", false).value));
 			configuration.save();
 		}
 		catch (Exception var1)
 		{
 			FMLLog.getLogger().info("[" + Info.MOD_NAME + "] Error while trying to access configuration!");
 			throw new RuntimeException(var1);
 		}
 	}
 
 	@Init
 	public void load(FMLInitializationEvent event)
 	{
		Info.blockInventoryStocker = new BlockInventoryStocker(Info.blockIDInventoryStocker, 0, Material.ground).setHardness(0.75F).setResistance(5F).setStepSound(Block.soundWoodFootstep).setBlockName("kaijin.invStocker").setCreativeTab(CreativeTabs.tabDecorations);
 		GameRegistry.registerBlock(Info.blockInventoryStocker);
 
 		GameRegistry.registerTileEntity(TileEntityInventoryStocker.class, "InventoryStocker");
 		GameRegistry.registerTileEntity(TileEntityInventoryStocker.class, "kaijin.inventoryStocker"); // Better TE reg key
 
 		GameRegistry.addRecipe(new ItemStack(Info.blockInventoryStocker, 1), new Object[] {"RIR", "PCP", "RIR", 'C', Block.chest, 'I', Item.ingotIron, 'P', Block.pistonBase, 'R', Item.redstone});
 
 		NetworkRegistry.instance().registerGuiHandler(this.instance, proxy);
 		proxy.load();
 
 		if (proxy.isServer())
 		{
 			FMLLog.getLogger().info(Info.MOD_NAME + " loaded.");
 		}
 
 		if (Info.isDebugging)
 		{
 			FMLLog.getLogger().info(Info.MOD_NAME + " debugging enabled.");
 		}
 
 		Info.registerStrings();
 	}
 }
