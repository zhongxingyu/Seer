 /**
  * 2013.06.17 - Update for Minecraft 1.5.2 and MinecraftFortge 7.8.1.737 by 
  * Cyanobacterium (Minecraft user Synechocystis)
  */
 package TombStone;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import TombStone.client.ClientProxy;
 
 import net.minecraft.block.Block;
 import net.minecraft.command.ICommand;
 import net.minecraft.command.ICommandManager;
 import net.minecraft.command.ServerCommandManager;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.src.ModLoader;
 import net.minecraftforge.common.MinecraftForge;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PostInit;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.Mod.ServerStarting;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.event.FMLServerStartingEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.EntityRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import net.minecraftforge.common.Configuration;
 
@Mod(modid="TombStone", name="TombStone", version="0.5.0")
 @NetworkMod(clientSideRequired=true, serverSideRequired=false)
 public class TombStone {
 	public static int tombStoneBlockId;
 	
 	public static TombStoneBlock tombStoneBlock;
 	
 	//Keeps track of the existing tombs
 	public static List<TombStoneTileEntity> tombList = new ArrayList<TombStoneTileEntity>();
 	
 	// The instance of your mod that Forge uses.
 	@Instance("TombStone")
 	public static TombStone instance;
 	
 	// Says where the client and server 'proxy' code is loaded.
 	@SidedProxy(clientSide="TombStone.client.ClientProxy", serverSide="TombStone.CommonProxy")
 	public static CommonProxy proxy;
 	
 	@PreInit
 	public void preInit(FMLPreInitializationEvent event) {
 		// Stub Method
 		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
 		config.load();
 		tombStoneBlockId = config.getBlock("RandomBlock", 3000).getInt();
 		tombStoneBlock = new TombStoneBlock(tombStoneBlockId);
 		config.save();
 	}
 	
 	@Init
 	public void load(FMLInitializationEvent event) {
 		proxy.registerRenderers();
 		
 		if(event.getSide().isClient())
 			ClientProxy.setCustomRenderers();
 		
 		//Register the tombstone block
 		GameRegistry.registerBlock(tombStoneBlock, "tombStoneBlock");
 		LanguageRegistry.addName(tombStoneBlock, "TombStone Block");
 		MinecraftForge.setBlockHarvestLevel(tombStoneBlock, "pickaxe", 0);
 		
 		//Register the death hook
 		MinecraftForge.EVENT_BUS.register(new DeathEventHook());
 		
 		//Register the tombstone tile entity
 		GameRegistry.registerTileEntity(TombStoneTileEntity.class, "tombStoneTileEntity");
 
 		//Register the tombstone gui
 		NetworkRegistry.instance().registerGuiHandler(this, new TombStoneGUIHandler());
 		    
 		//Item stack (ID, Count, Meta)
 		ItemStack stoneStack = new ItemStack(Block.stone);
 		ItemStack signStack = new ItemStack(Item.sign);
 		
 		//3x3 shaped crafting
 		GameRegistry.addRecipe(new ItemStack(tombStoneBlock), " x ", "xyx", "xxx",
 		    'x', stoneStack, 'y', signStack);    
 	}
 	
 	@PostInit
 	public void postInit(FMLPostInitializationEvent event) {
 		// Stub Method
 	}
 	
 	@ServerStarting
 	public void onServerStarting(FMLServerStartingEvent event)
 	{
 		event.registerServerCommand(new ChatHandler());
 	}
 }
