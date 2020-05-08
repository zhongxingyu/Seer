 package compactMobs;
 
 import java.io.File;
 import java.util.logging.Logger;
 
 import compactMobs.Items.CompactMobsItems;
 import compactMobs.Blocks.BlockBreeder;
 import compactMobs.Blocks.BlockCompactor;
 import compactMobs.Blocks.BlockDecompactor;
 import compactMobs.Blocks.BlockIncubator;
 import compactMobs.TileEntity.TileEntityBreeder;
 import compactMobs.TileEntity.TileEntityCompactor;
 import compactMobs.TileEntity.TileEntityDecompactor;
 import compactMobs.TileEntity.TileEntityIncubator;
 
 import net.minecraft.src.Block;
 import net.minecraft.src.CraftingManager;
 import net.minecraft.src.Item;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.RenderGlobal;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.DungeonHooks;
 import net.minecraftforge.common.Property;
 import cpw.mods.fml.client.registry.ClientRegistry;
 import cpw.mods.fml.client.registry.RenderingRegistry;
 import cpw.mods.fml.common.FMLLog;
 import cpw.mods.fml.common.Loader;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 import net.minecraft.src.Material;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 
 
 @NetworkMod(clientSideRequired = true, serverSideRequired = false)
@Mod(modid = "CM", name = "CompactMobs", version = "1.0.2")
 public class CompactMobsCore {
 	
 	@Instance 
 	public static CompactMobsCore instance;
 	
 	//hardcoded id of block
 	public static int BlockID = 3391;
 	
 	public boolean useFullTagCompound = false;
 	
 	public static Block blocks;
 	public static Block blockCompactor;
 	public static Block blockDecompactor;
 	public static Block blockBreeder;
 	public static Block blockIncubator;
 	
 	public static Configuration mainConfig;
 	
 	public static Logger cmLog = Logger.getLogger("CompactMobs");
 	
 	
 	public Property compatorId;// = CompactMobsCore.mainConfig.getOrCreateBlockIdProperty("CompactorId", 3391);
 	public Property decompatorId;// = CompactMobsCore.mainConfig.getOrCreateBlockIdProperty("DecompactorId", 3392);
 	public Property breederId;// = CompactMobsCore.mainConfig.getOrCreateBlockIdProperty("BreederId", 3393);
 	public Property incubatorId;// = CompactMobsCore.mainConfig.getOrCreateBlockIdProperty("IncubatorId", 3394);
 	public Property emptyMobHolderId;// = CompactMobsCore.mainConfig.getOrCreateBlockIdProperty("EmptyMobHolderId", 3395);
 	public Property fullMobHolderId;
 	
 	@SidedProxy(clientSide = "compactMobs.ClientProxyCompactMobs", serverSide = "compactMobs.CommonProxyCompactMobs")
 	public static CommonProxyCompactMobs proxy;
 	
 	
 	@PreInit
 	public void loadConfiguration(FMLPreInitializationEvent evt) {
 		cmLog.setParent(FMLLog.getLogger());
		cmLog.info("Starting CompactMobs v1.0.2");
 		
 		mainConfig = new Configuration(new File(evt.getModConfigurationDirectory(), "CompactMobs.cfg"));
 		try
 		{
 			mainConfig.load();
 			compatorId = CompactMobsCore.mainConfig.getOrCreateBlockIdProperty("CompactorId", 3391);
 			decompatorId = CompactMobsCore.mainConfig.getOrCreateBlockIdProperty("DecompactorId", 3392);
 			breederId = CompactMobsCore.mainConfig.getOrCreateBlockIdProperty("BreederId", 3393);
 			incubatorId = CompactMobsCore.mainConfig.getOrCreateBlockIdProperty("IncubatorId", 3394);
 			emptyMobHolderId = CompactMobsCore.mainConfig.getOrCreateIntProperty("EmptyMobHolderId",mainConfig.CATEGORY_ITEM, 3395);
 			fullMobHolderId = CompactMobsCore.mainConfig.getOrCreateIntProperty("FullMobHolderId",mainConfig.CATEGORY_ITEM, 3396);
 			
 		}
 		finally
 		{
 			mainConfig.save();
 		}
 	}
 	
 	@Init
 	public void load(FMLInitializationEvent event)
 	{
 		
 		CompactMobsItems.createInstance();
 		
 		NetworkRegistry.instance().registerGuiHandler(this, this.proxy);
 		
 		blockCompactor = new BlockCompactor(compatorId.getInt(), Material.iron).setStepSound(Block.soundMetalFootstep).setHardness(3F).setResistance(1.0F).setBlockName("blockCompactor");
 		GameRegistry.registerBlock(blockCompactor);
 		LanguageRegistry.addName(blockCompactor, "Compactor"); 
 		blockDecompactor = new BlockDecompactor(decompatorId.getInt(), Material.iron).setStepSound(Block.soundMetalFootstep).setHardness(3F).setResistance(1.0F).setBlockName("blockDecompactor");
 		GameRegistry.registerBlock(blockDecompactor);
 		LanguageRegistry.addName(blockDecompactor, "Decompactor");
 		blockBreeder = new BlockBreeder(breederId.getInt(), Material.iron).setStepSound(Block.soundMetalFootstep).setHardness(3F).setResistance(1.0F).setBlockName("blockBreeder");
 		GameRegistry.registerBlock(blockBreeder);
 		LanguageRegistry.addName(blockBreeder, "Breeder");
 		blockIncubator = new BlockIncubator(incubatorId.getInt(), Material.iron).setStepSound(Block.soundMetalFootstep).setHardness(3F).setResistance(1.0F).setBlockName("blockIncubator");
 		GameRegistry.registerBlock(blockIncubator);
 		LanguageRegistry.addName(blockIncubator, "Incubator");
 		
 		GameRegistry.registerTileEntity(TileEntityCompactor.class,"tileEntityCompactor");
 		GameRegistry.registerTileEntity(TileEntityDecompactor.class, "tileEntityDecompactor");
 		GameRegistry.registerTileEntity(TileEntityBreeder.class, "tileEntityBreeder");
 		GameRegistry.registerTileEntity(TileEntityIncubator.class, "tileEntityIncubator");
 		
 		CompactMobsItems.getInstance().instantiateItems();
 		CompactMobsItems.getInstance().nameItems();
 		
 		
 		
 		
 	}
 	
 	@Init
 	public void initialize(FMLInitializationEvent evt) {
 		Item ironGear = Item.ingotIron;
 		Item goldGear = Item.ingotGold;
 		
 		try {
 			cmLog.info("Adding Compactmob Recipes");
 			ironGear = (Item)Class.forName("buildcraft.BuildCraftCore").getField("ironGearItem").get(null);
 			goldGear = (Item)Class.forName("buildcraft.BuildCraftCore").getField("goldGearItem").get(null);
 			GameRegistry.addRecipe(new ItemStack(blockCompactor, 1), new Object[] {"ipi", "lol", "gpg", 'i', ironGear, 'p', Block.pistonBase, 'l', new ItemStack(Item.dyePowder, 1, 4), 'o', Block.obsidian, 'g', goldGear});
 			GameRegistry.addRecipe(new ItemStack(blockDecompactor, 1), new Object[] {"oro", "ild", "grg", 'o', ironGear, 'r', Item.redstone, 'i', Item.ingotIron, 'l', Block.blockLapis, 'd', Block.dispenser, 'g', goldGear});
 			GameRegistry.addRecipe(new ItemStack(CompactMobsItems.mobHolder, 2), new Object[] {"hhh", "ibi", "sss", 'h', new ItemStack(Block.stoneSingleSlab, 1, 0), 'i', Item.ingotIron, 'b', Block.fenceIron, 's', Block.stone});
 			GameRegistry.addRecipe(new ItemStack(blockBreeder, 1), new Object[] {"oho", "iwi", "gag", 'o', ironGear, 'h', CompactMobsItems.mobHolder, 'i', Item.ingotIron, 'w', Item.wheat, 'g', goldGear, 'a', new ItemStack(Item.appleGold, 1, 0)});
 			GameRegistry.addRecipe(new ItemStack(blockIncubator, 1), new Object[] {"oco","ifi","gbg", 'o', ironGear, 'c', Block.chest, 'i', Item.ingotIron, 'f', Block.stoneOvenIdle, 'g', goldGear, 'b', Item.blazePowder});
 			
 					
 		} catch (Exception ex)
 		{
 			cmLog.info("NO BUILDCRAFT FOUND!!! NEEDED FOR COMPACTMOBS");
 		}
 		
 	}
 	
 	public String getPriorities() {
 	    return "after:mod_IC2;after:mod_BuildCraftCore;after:mod_BuildCraftEnergy;after:mod_BuildCraftFactory;after:mod_BuildCraftSilicon;after:mod_BuildCraftTransport;after:mod_RedPowerWorld";
 	  }
 }
