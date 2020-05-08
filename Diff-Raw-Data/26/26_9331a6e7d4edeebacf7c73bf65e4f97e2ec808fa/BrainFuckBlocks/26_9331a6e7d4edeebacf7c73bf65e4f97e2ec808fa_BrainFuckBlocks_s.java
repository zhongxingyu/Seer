 package com.dmillerw.brainFuckBlocks;
 
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraftforge.common.Configuration;
 
 import com.dmillerw.brainFuckBlocks.block.BlockHandler;
 import com.dmillerw.brainFuckBlocks.block.BlockIDs;
 import com.dmillerw.brainFuckBlocks.core.CommonProxy;
 import com.dmillerw.brainFuckBlocks.core.CreativeTabBrainFuck;
 import com.dmillerw.brainFuckBlocks.helper.LogHelper;
 import com.dmillerw.brainFuckBlocks.lib.ModInfo;
 import com.dmillerw.brainFuckBlocks.network.BFPacketHandler;
 
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 @Mod(modid=ModInfo.MOD_ID, name=ModInfo.MOD_NAME, version=ModInfo.MOD_VERSION)
 @NetworkMod(serverSideRequired=false, clientSideRequired=true, packetHandler=BFPacketHandler.class)
 public class BrainFuckBlocks {
 	@Instance(ModInfo.MOD_ID)
 	public static BrainFuckBlocks instance;
 	@SidedProxy(serverSide="com.dmillerw.brainFuckBlocks.CommonProxy", clientSide="com.dmillerw.brainFuckBlocks.client.ClientProxy")
 	public static CommonProxy proxy;
 	
 	public static CreativeTabs creativeTabBF = new CreativeTabBrainFuck("brainFuck");
 	
 	@PreInit
 	public void preInit(FMLPreInitializationEvent e) {
 		//Config stuffs
 		Configuration config = new Configuration(e.getSuggestedConfigurationFile());
 		try {
 			config.load();
 			
 			BlockIDs.brainFuckCodeBlockID = config.getBlock("brainFuckCodeBlockID", BlockIDs.brainFuckCodeBlockDefaultID).getInt();
 		} catch(Exception ex) {
 			LogHelper.log("Failed to load config. Assuming defaults!");
 			BlockIDs.brainFuckCodeBlockID = BlockIDs.brainFuckCodeBlockDefaultID;
 		} finally {
 			if (config.hasChanged()) {
 				config.save();
 			}
 		}
 	}
 	
 	@Init
 	public void init(FMLInitializationEvent e) {
 		//Adds proper localization string for creative tab
 		LanguageRegistry.instance().addStringLocalization("itemGroup.brainFuck", "BrainFuck Blocks");
 		
 		//Initializes blocks
 		BlockHandler.init();
 		
 		//Initializes TileEntities
 		proxy.registerTileEntities();
 	}
 	
 }
