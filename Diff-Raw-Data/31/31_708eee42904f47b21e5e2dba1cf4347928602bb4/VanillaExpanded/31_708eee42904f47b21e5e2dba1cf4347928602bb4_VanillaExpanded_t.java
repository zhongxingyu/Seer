 package nexustools.vanillaexpanded;
 
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.client.MinecraftForgeClient;
 import net.minecraftforge.common.Configuration;
 import nexustools.vanillaexpanded.block.BlockButtonObsidian;
 import nexustools.vanillaexpanded.block.BlockDoorObsidian;
 import nexustools.vanillaexpanded.block.BlockFenceObsidian;
 import nexustools.vanillaexpanded.block.BlockLeverObsidian;
 import nexustools.vanillaexpanded.block.BlockPressurePlateObsidian;
 import nexustools.vanillaexpanded.block.BlockStairsObsidian;
 import nexustools.vanillaexpanded.item.ItemDoorObsidian;
 import nexustools.vanillaexpanded.item.ItemStickObsidian;
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 @Mod(modid = "VanillaExpanded")
 @NetworkMod(clientSideRequired = true)
 public class VanillaExpanded {
 	public static boolean blockButtonObsidianEnabled = true, blockStairObsidianEnabled = true, blockDoorObsidianEnabled = true, blockFenceObsidianEnabled = true, blockLeverObsidianEnabled = true, blockPressurePlateObsidianEnabled = true;
 	public static int blockButtonObsidianID, blockStairObsidianID, blockDoorObsidianID, blockFenceObsidianID, blockLeverObsidianID, blockPressurePlateObsidianID;
 	public static Block blockButtonObsidian, blockStairObsidian, blockDoorObsidian, blockFenceObsidian, blockLeverObsidian, blockPressurePlateObsidian;
 
 	public static int itemDoorObsidianID;
 	public static Item itemDoorObsidian;
 
 	public static int itemStickObsidianID;
 	public static Item itemStickObsidian;
 
 	public static double obisidanAdditionHardness;
 
 	@PreInit
 	public void preload(FMLPreInitializationEvent iEvent) {
 		if(FMLCommonHandler.instance().getSide().isClient()) {
 			MinecraftForgeClient.preloadTexture("/nexustools/vanillaexpanded/images/block/block.png");
 			MinecraftForgeClient.preloadTexture("/nexustools/vanillaexpanded/images/item/item.png");
 		}
 		Configuration conf = new Configuration(iEvent.getSuggestedConfigurationFile());
 		conf.load();
 		blockButtonObsidianID = conf.getBlock("blockButtonObsidianID", 675).getInt();
 		blockStairObsidianID = conf.getBlock("blockStairObsidianID", 676).getInt();
 		blockDoorObsidianID = conf.getBlock("blockDoorObsidianID", 677).getInt();
 		blockFenceObsidianID = conf.getBlock("blockFenceObsidianID", 678).getInt();
 		blockLeverObsidianID = conf.getBlock("blockLeverObsidianID", 679).getInt();
 		blockPressurePlateObsidianID = conf.getBlock("blockPressurePlateObsidianID", 680).getInt();
 
 		itemDoorObsidianID = conf.getItem("itemDoorObsidianID", 6550).getInt();
 		itemStickObsidianID = conf.getItem("itemStickObsidian", 6551).getInt();
 
 		blockButtonObsidianEnabled = conf.get("Obsidian Additions", "blockButtonObsidianEnabled", blockButtonObsidianEnabled).getBoolean(true);
 		blockStairObsidianEnabled = conf.get("Obsidian Additions", "blockStairObsidianEnabled", blockStairObsidianEnabled).getBoolean(true);
 		blockDoorObsidianEnabled = conf.get("Obsidian Additions", "blockDoorObsidianEnabled", blockDoorObsidianEnabled).getBoolean(true);
 		blockFenceObsidianEnabled = conf.get("Obsidian Additions", "blockFenceObsidianEnabled", blockFenceObsidianEnabled).getBoolean(true);
 		blockLeverObsidianEnabled = conf.get("Obsidian Additions", "blockLeverObsidianEnabled", blockLeverObsidianEnabled).getBoolean(true);
 		blockPressurePlateObsidianEnabled = conf.get("Obsidian Additions", "blockPressurePlateObsidianEnabled", blockPressurePlateObsidianEnabled).getBoolean(true);
 
 		// TODO: This seems to init with 0? Programming problem, the value seems to be 50.0 even at this point though.
 		obisidanAdditionHardness = conf.get("Obsidian Additions", "obisidanAdditionHardness", obisidanAdditionHardness).getDouble(Block.obsidian.getBlockHardness(null, 0, 0, 0) /* The arguments are irrelevant, not used and is very odd that there's no statically available method... */);
 		conf.save();
 	}
 
 	@Init
 	public void load(FMLInitializationEvent iEvent) {
 		if(blockFenceObsidianEnabled || blockLeverObsidianEnabled) { // Sticks are needed, initialize them.
 			itemStickObsidian = new ItemStickObsidian(itemStickObsidianID).setItemName("itemStickObsidian").setCreativeTab(CreativeTabs.tabMaterials);
 			GameRegistry.addRecipe(new ItemStack(itemStickObsidian, 4), "OXX", "OXX", "XXX", 'O', new ItemStack(Block.obsidian));
 			GameRegistry.addRecipe(new ItemStack(itemStickObsidian, 4), "XOX", "XOX", "XXX", 'O', new ItemStack(Block.obsidian));
 			GameRegistry.addRecipe(new ItemStack(itemStickObsidian, 4), "XXO", "XXO", "XXX", 'O', new ItemStack(Block.obsidian));
 			GameRegistry.addRecipe(new ItemStack(itemStickObsidian, 4), "XXX", "OXX", "OXX", 'O', new ItemStack(Block.obsidian));
 			GameRegistry.addRecipe(new ItemStack(itemStickObsidian, 4), "XXX", "XOX", "XOX", 'O', new ItemStack(Block.obsidian));
 			GameRegistry.addRecipe(new ItemStack(itemStickObsidian, 4), "XXX", "XXO", "XXO", 'O', new ItemStack(Block.obsidian));
 			LanguageRegistry.addName(itemStickObsidian, "Obsidian Stick");
 		}
 
 		if(blockButtonObsidianEnabled) {
			blockButtonObsidian = new BlockButtonObsidian(blockButtonObsidianID).setBlockName("blockButtonObsidian").setHardness((float) obisidanAdditionHardness).setRequiresSelfNotify();
 			GameRegistry.registerBlock(blockButtonObsidian, "blockButtonObsidian");
 			GameRegistry.addShapelessRecipe(new ItemStack(blockButtonObsidian), new ItemStack(Block.obsidian));
 			LanguageRegistry.addName(blockButtonObsidian, "Obsidian Button");
 		}
 
 		if(blockStairObsidianEnabled) {
			blockStairObsidian = new BlockStairsObsidian(blockStairObsidianID, Block.obsidian).setBlockName("blockStairObsidian").setHardness((float) obisidanAdditionHardness).setRequiresSelfNotify();
 			GameRegistry.registerBlock(blockStairObsidian, "blockStairObsidian");
 			GameRegistry.addRecipe(new ItemStack(blockStairObsidian), "OXX", "OOX", "OOO", 'O', new ItemStack(Block.obsidian));
 			LanguageRegistry.addName(blockStairObsidian, "Obsidian Stairs");
 		}
 
 		if(blockDoorObsidianEnabled) {
			blockDoorObsidian = new BlockDoorObsidian(blockDoorObsidianID).setBlockName("blockDoorObsidian").setHardness((float) obisidanAdditionHardness).setRequiresSelfNotify();
 			GameRegistry.registerBlock(blockDoorObsidian, "blockDoorObsidian");
 			LanguageRegistry.addName(blockDoorObsidian, "Obsidian Door");
 			
 			itemDoorObsidian = new ItemDoorObsidian(itemDoorObsidianID).setItemName("itemDoorObsidianID");
 			GameRegistry.addRecipe(new ItemStack(itemDoorObsidian), "OOX", "OOX", "OOX", 'O', new ItemStack(Block.obsidian));
 			GameRegistry.addRecipe(new ItemStack(itemDoorObsidian), "XOO", "XOO", "XOO", 'O', new ItemStack(Block.obsidian));
 			LanguageRegistry.addName(itemDoorObsidian, "Obsidian Door");
 		}
 
 		if(blockFenceObsidianEnabled) {
			blockFenceObsidian = new BlockFenceObsidian(blockFenceObsidianID).setBlockName("blockFenceObsidian").setHardness((float) obisidanAdditionHardness).setRequiresSelfNotify();
 			GameRegistry.registerBlock(blockFenceObsidian, "blockFenceObsidian");
 			GameRegistry.addRecipe(new ItemStack(blockFenceObsidian), "SSS", "SSS", "XXX", 'S', new ItemStack(itemStickObsidian));
 			GameRegistry.addRecipe(new ItemStack(blockFenceObsidian), "XXX", "SSS", "SSS", 'S', new ItemStack(itemStickObsidian));
 			LanguageRegistry.addName(blockFenceObsidian, "Obsidian Fence");
 		}
 
 		if(blockLeverObsidianEnabled) {
			blockLeverObsidian = new BlockLeverObsidian(blockLeverObsidianID).setBlockName("blockLeverObsidian").setHardness((float) obisidanAdditionHardness).setRequiresSelfNotify();
 			GameRegistry.registerBlock(blockLeverObsidian, "blockLeverObsidian");
 			//Is this really neccessary?
 			GameRegistry.addRecipe(new ItemStack(blockLeverObsidian), "SXX", "OXX", "XXX", 'O', new ItemStack(Block.obsidian), 'S', new ItemStack(itemStickObsidian));
 			GameRegistry.addRecipe(new ItemStack(blockLeverObsidian), "XSX", "XOX", "XXX", 'O', new ItemStack(Block.obsidian), 'S', new ItemStack(itemStickObsidian));
 			GameRegistry.addRecipe(new ItemStack(blockLeverObsidian), "XXS", "XXO", "XXX", 'O', new ItemStack(Block.obsidian), 'S', new ItemStack(itemStickObsidian));
 			GameRegistry.addRecipe(new ItemStack(blockLeverObsidian), "XXX", "SXX", "OXX", 'O', new ItemStack(Block.obsidian), 'S', new ItemStack(itemStickObsidian));
 			GameRegistry.addRecipe(new ItemStack(blockLeverObsidian), "XXX", "XSX", "XOX", 'O', new ItemStack(Block.obsidian), 'S', new ItemStack(itemStickObsidian));
 			GameRegistry.addRecipe(new ItemStack(blockLeverObsidian), "XXX", "XXS", "XXO", 'O', new ItemStack(Block.obsidian), 'S', new ItemStack(itemStickObsidian));
 			LanguageRegistry.addName(blockLeverObsidian, "Obsidian Lever");
 		}
 		
 		if(blockPressurePlateObsidianEnabled) {
			blockPressurePlateObsidian = new BlockPressurePlateObsidian(blockPressurePlateObsidianID).setBlockName("blockPressurePlateObsidian").setHardness((float) obisidanAdditionHardness).setRequiresSelfNotify();
 			GameRegistry.registerBlock(blockPressurePlateObsidian, "blockPressurePlateObsidian");
 			LanguageRegistry.addName(blockPressurePlateObsidian, "Obsidian Pressure Plate");
 		}
 	}
 }
