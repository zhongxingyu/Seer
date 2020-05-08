 package tektor.minecraft.chalith;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.FurnaceRecipes;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.oredict.OreDictionary;
 import tektor.minecraft.chalith.blocks.ChalithStoneBase;
 import tektor.minecraft.chalith.blocks.BlockTrapRune;
 import tektor.minecraft.chalith.blocks.ChalithOreBase;
 import tektor.minecraft.chalith.blocks.ChalithWorkplaces;
 import tektor.minecraft.chalith.blocks.GateBlock;
 import tektor.minecraft.chalith.gui.ChalithGuiHandler;
 import tektor.minecraft.chalith.items.BaseRune;
 import tektor.minecraft.chalith.items.ChalithOreItemBlock;
 import tektor.minecraft.chalith.items.ChalithStoneItemBlock;
 import tektor.minecraft.chalith.items.HerbalByProducts;
 import tektor.minecraft.chalith.items.SeedBase;
 import tektor.minecraft.chalith.items.TrapRune;
 import tektor.minecraft.chalith.items.ChalithIngotBase;
 import tektor.minecraft.chalith.items.UtilRune;
 import tektor.minecraft.chalith.items.RuneSymbol;
 import tektor.minecraft.chalith.plants.PlantBase;
 import cpw.mods.fml.common.IWorldGenerator;
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
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
@Mod(modid = "Chalith", name = "Chalith", version = "0.6.9")
 @NetworkMod(channels={"Chalith"}, packetHandler = ChalithPacketHandler.class, clientSideRequired = true)
 public class ChalithBase {
 
 	// instance
 	@Instance("Chalith")
 	public static ChalithBase instance;
 
 	// blocks
 	public static int blockID1, blockID2, blockID3, blockID4, blockID5, blockID6;
 	public static Block bloodstone;
 	public static Block chalithBaseOre;
 	public static Block trapRune;
 	public static Block gateBlock;
 	public static Block plantBase;
 	public static Block workbench;
 
 	// items
 	public static int itemID1, itemID2, itemID3, itemID4, itemID5, itemID6,
 			itemID7, itemID8;
 	public static Item avaeaIngot;
 	public static Item lorynIngot;
 	public static Item utilRune;
 	public static Item trapRuneItem;
 	public static Item baseRune;
 	public static Item runeSymbol;
 	public static Item seedBase;
 	public static Item herbalByProduct;
 
 	// Says where the client and server 'proxy' code is loaded.
 	@SidedProxy(clientSide = "tektor.minecraft.chalith.client.ChalithClientProxy", serverSide = "tektor.minecraft.chalith.ChalithCommonProxy")
 	public static ChalithCommonProxy proxy;
 
 	@EventHandler
 	public void preInit(FMLPreInitializationEvent event) {
 		Configuration config = new Configuration(
 				event.getSuggestedConfigurationFile());
 		config.load();
 		blockID1 = config.get(Configuration.CATEGORY_BLOCK, "blockID1", 980)
 				.getInt();
 		blockID2 = config.get(Configuration.CATEGORY_BLOCK, "blockID2", 981)
 				.getInt();
 		blockID3 = config.get(Configuration.CATEGORY_BLOCK, "blockID3", 982)
 				.getInt();
 		blockID4 = config.get(Configuration.CATEGORY_BLOCK, "blockID4", 983)
 				.getInt();
 		blockID5 = config.get(Configuration.CATEGORY_BLOCK, "blockID5", 984)
 				.getInt();
 		blockID6 = config.get(Configuration.CATEGORY_BLOCK, "blockID6", 985)
 				.getInt();
 
 		itemID1 = config.get(Configuration.CATEGORY_ITEM, "itemID1", 7000)
 				.getInt();
 		itemID2 = config.get(Configuration.CATEGORY_ITEM, "itemID2", 7001)
 				.getInt();
 		itemID3 = config.get(Configuration.CATEGORY_ITEM, "itemID3", 7002)
 				.getInt();
 		itemID4 = config.get(Configuration.CATEGORY_ITEM, "itemID4", 7003)
 				.getInt();
 		itemID5 = config.get(Configuration.CATEGORY_ITEM, "itemID5", 7004)
 				.getInt();
 		itemID6 = config.get(Configuration.CATEGORY_ITEM, "itemID6", 7005)
 				.getInt();
 		itemID7 = config.get(Configuration.CATEGORY_ITEM, "itemID7", 7006)
 				.getInt();
 		itemID8 = config.get(Configuration.CATEGORY_ITEM, "itemID8", 7007)
 				.getInt();
 
 		config.save();
 	}
 
 	@EventHandler
 	public void load(FMLInitializationEvent event) {
 		initializeID();
 		proxy.registerRenderers();
 		registerOres();
 		registerIngots();
 		registerRunes();
 		registerTileEntities();
 		registerPlants();
 		registerMisc();
 		smeltingRecipes();
 		runeCrafting();
 		GameRegistry.registerWorldGenerator(new ChalithWorldGen());
 		NetworkRegistry.instance().registerGuiHandler(this, new ChalithGuiHandler());
 	}
 
 	private void registerMisc() {
 		//bloodstone
 		MinecraftForge.setBlockHarvestLevel(bloodstone, "pickaxe", 1);
 		GameRegistry.registerBlock(bloodstone, ChalithStoneItemBlock.class,
 				"bloodstone");
 		LanguageRegistry.addName(new ItemStack(bloodstone, 1, 0), "Bloodstone");
 		LanguageRegistry.addName(new ItemStack(bloodstone, 1, 1), "Bloodstone Cobble");
 		LanguageRegistry.addName(new ItemStack(bloodstone, 1, 2), "Corinnstone");
 		LanguageRegistry.addName(new ItemStack(bloodstone, 1, 3), "Corinnstone Cobble");
 		
 		GameRegistry.registerBlock(workbench, "runeWorkbench");
 		LanguageRegistry.addName(new ItemStack(workbench,1,0), "Rune Workbench");
 	}
 
 	private void registerPlants() {
 		// Israk
 		GameRegistry.registerBlock(plantBase, "plantBase");
 		LanguageRegistry.addName(new ItemStack(plantBase, 1, 0), "Israk Root");
 		GameRegistry.registerItem(seedBase, "seedBase");
 		LanguageRegistry.addName(new ItemStack(seedBase, 1, 0), "Israk Root");
 		MinecraftForge.addGrassSeed(new ItemStack(seedBase, 1, 0), 7);
 		GameRegistry.registerItem(herbalByProduct, "herbalByProduct");
 
 	}
 
 	private void initializeID() {
 		// blocks
 		bloodstone = new ChalithStoneBase(blockID1);
 		chalithBaseOre = new ChalithOreBase(blockID2);
 		trapRune = new BlockTrapRune(blockID3);
 		gateBlock = new GateBlock(blockID4);
 		plantBase = new PlantBase(blockID5);
 		workbench = new ChalithWorkplaces(blockID6);
 		// items
 		seedBase = new SeedBase(itemID7);
 		herbalByProduct = new HerbalByProducts(itemID8);
 		lorynIngot = new ChalithIngotBase(itemID2);
 
 		utilRune = new UtilRune(itemID3);
 		trapRuneItem = new TrapRune(itemID6);
 
 		baseRune = new BaseRune(itemID4);
 		runeSymbol = new RuneSymbol(itemID5);
 	}
 
 	private void registerTileEntities() {
 		GameRegistry.registerTileEntity(
 				tektor.minecraft.chalith.entity.TrapRuneTileEntity.class,
 				"Fire Trap Rune");
 		GameRegistry.registerTileEntity(
 				tektor.minecraft.chalith.entity.GateBlockTileEntity.class,
 				"Gate Block");
 		GameRegistry.registerTileEntity(
 				tektor.minecraft.chalith.entity.ChalithWorkplaceTileEntity.class,
 				"Workplace");
 
 	}
 
 	private void runeCrafting() {
 		ItemStack diStack = new ItemStack(this.runeSymbol, 1, 0);
 		ItemStack diStack2 = new ItemStack(this.runeSymbol, 1, 0);
 		ItemStack inStack = new ItemStack(this.runeSymbol, 1, 1);
 		ItemStack nomStack = new ItemStack(this.runeSymbol, 1, 2);
 		ItemStack xenStack = new ItemStack(this.runeSymbol, 1, 3);
 		ItemStack alStack = new ItemStack(this.runeSymbol, 1, 5);
 		ItemStack borStack = new ItemStack(this.runeSymbol, 1, 6);
 		ItemStack cesStack = new ItemStack(this.runeSymbol, 1, 7);
 		ItemStack hirStack = new ItemStack(this.runeSymbol, 1, 9);
 		ItemStack onStack = new ItemStack(this.runeSymbol, 1, 11);
 		ItemStack voStack = new ItemStack(this.runeSymbol, 1, 13);
 		ItemStack welStack = new ItemStack(this.runeSymbol, 1, 15);
 		ItemStack avaeaIngotStack = new ItemStack(this.lorynIngot, 1, 2);
 		ItemStack lorynIngotStack = new ItemStack(this.lorynIngot, 1, 0);
 		ItemStack sorfynIngotStack = new ItemStack(this.lorynIngot, 1, 1);
 		ItemStack baseRuneStack = new ItemStack(this.baseRune, 1, 0);
 		ItemStack wildRuneStack = new ItemStack(this.baseRune, 1, 1);
 		ItemStack switchingRuneStack = new ItemStack(this.baseRune, 1, 2);
 		ItemStack goldIngotStack = new ItemStack(Item.ingotGold, 1);
 
 		GameRegistry.addShapedRecipe(new ItemStack(this.baseRune, 1, 0),
 				new Object[] { "XXX", "X X", "XXX", 'X', avaeaIngotStack });
 		GameRegistry.addShapedRecipe(new ItemStack(this.baseRune, 1, 1),
 				new Object[] { "XXX", "X X", "XXX", 'X', sorfynIngotStack });
 		GameRegistry.addShapedRecipe(new ItemStack(this.baseRune, 1, 2),
 				new Object[] { "XY", 'X', baseRuneStack, 'Y', wildRuneStack });
 		// Recall
 		GameRegistry.addShapedRecipe(new ItemStack(this.utilRune, 1, 0),
 				new Object[] { "ABC", " X ", "   ", 'A', diStack, 'B', inStack,
 						'C', nomStack, 'X', baseRuneStack });
 		// Recall Gate
 		GameRegistry.addShapedRecipe(new ItemStack(this.utilRune, 1, 3),
 				new Object[] { "ABC", " X ", "   ", 'A', diStack, 'B', alStack,
 						'C', nomStack, 'X', wildRuneStack });
 		// Rune of Inventory
 		GameRegistry.addShapedRecipe(new ItemStack(this.utilRune, 1, 1),
 				new Object[] { "ABC", " X ", "   ", 'A', welStack, 'B',
 						onStack, 'C', alStack, 'X', switchingRuneStack });
 
 		// FireTrap
 		GameRegistry.addShapedRecipe(new ItemStack(this.trapRuneItem, 1, 0),
 				new Object[] { "ABC", " X ", "   ", 'A', xenStack, 'B',
 						voStack, 'C', borStack, 'X', baseRuneStack });
 		// IceTrap
 		GameRegistry.addShapedRecipe(new ItemStack(this.trapRuneItem, 1, 1),
 				new Object[] { "ABC", " X ", "   ", 'A', xenStack, 'B',
 						cesStack, 'C', hirStack, 'X', baseRuneStack });
 
 		// Di
 		GameRegistry.addShapedRecipe(new ItemStack(this.runeSymbol, 1, 0),
 				new Object[] { "AA ", "  A", "AA ", 'A', lorynIngotStack });
 		// In
 		GameRegistry.addShapedRecipe(new ItemStack(this.runeSymbol, 1, 1),
 				new Object[] { "A  ", " A ", "  A", 'A', lorynIngotStack });
 		// Nom
 		GameRegistry.addShapedRecipe(new ItemStack(this.runeSymbol, 1, 2),
 				new Object[] { "AAA", "  A", "  A", 'A', lorynIngotStack });
 		// Xen
 		GameRegistry.addShapedRecipe(new ItemStack(this.runeSymbol, 1, 3),
 				new Object[] { "A A", " B ", "A A", 'A', lorynIngotStack, 'B',
 						goldIngotStack });
 		// Yok
 		GameRegistry.addShapedRecipe(new ItemStack(this.runeSymbol, 1, 4),
 				new Object[] { "A A", " A ", "A  ", 'A', lorynIngotStack });
 		// Al
 		GameRegistry.addShapedRecipe(new ItemStack(this.runeSymbol, 1, 5),
 				new Object[] { "   ", " A ", "A A", 'A', lorynIngotStack });
 		// Bor
 		GameRegistry.addShapedRecipe(new ItemStack(this.runeSymbol, 1, 6),
 				new Object[] { "A  ", "AA ", "A  ", 'A', lorynIngotStack });
 		// Ces
 		GameRegistry.addShapedRecipe(new ItemStack(this.runeSymbol, 1, 7),
 				new Object[] { "  A", " A ", "  A", 'A', lorynIngotStack });
 		// Hir
 		GameRegistry.addShapedRecipe(new ItemStack(this.runeSymbol, 1, 9),
 				new Object[] { "A A", " A ", "A A", 'A', lorynIngotStack });
 		// On
 		GameRegistry.addShapedRecipe(new ItemStack(this.runeSymbol, 1, 11),
 				new Object[] { "AAA", "A A", "AAA", 'A', lorynIngotStack });
 		// Vo
 		GameRegistry.addShapedRecipe(new ItemStack(this.runeSymbol, 1, 13),
 				new Object[] { "A A", "A A", " A ", 'A', lorynIngotStack });
 
 		// Wel
 		GameRegistry.addShapedRecipe(new ItemStack(this.runeSymbol, 1, 15),
 				new Object[] { "  A", " AA", "AA ", 'A', lorynIngotStack });
 
 	}
 
 	private void registerRunes() {
 		// Symbols
 		for (int ix = 0; ix < 16; ix++) {
 			ItemStack runeSymbolStack = new ItemStack(runeSymbol, 1, ix);
 			LanguageRegistry
 					.addName(runeSymbolStack,
 							RuneSymbol.runeSymbolNames[runeSymbolStack
 									.getItemDamage()]);
 		}
 		// Base
 		LanguageRegistry.addName(baseRune, "Base Rune");
 		GameRegistry.registerItem(baseRune, "baseRune");
 
 		// Recall
 		LanguageRegistry.addName(utilRune, "Recall Rune");
 		GameRegistry.registerItem(utilRune, "recallRune");
 		// FireTrap
 		GameRegistry.registerItem(trapRuneItem, "fireTrapRune");
 		GameRegistry.registerBlock(trapRune, "fireTrapRuneBlock");
 		LanguageRegistry.addName(new ItemStack(trapRune, 1, 0),
 				"Fire Trap Rune");
 		// Ice Trap
 		LanguageRegistry
 				.addName(new ItemStack(trapRune, 1, 1), "Ice Trap Rune");
 
 		// Gate Rune
 		GameRegistry.registerBlock(gateBlock, "goalBlock");
 
 	}
 
 	private void registerIngots() {
 
 		// loryn
 
 		GameRegistry.registerItem(lorynIngot, "lorynIngot");
 		LanguageRegistry
 				.addName(new ItemStack(lorynIngot, 1, 0), "Loryn Ingot");
 
 		OreDictionary
 				.registerOre("ingotLoryn", new ItemStack(lorynIngot, 1, 0));
 		// sorfyn
 		LanguageRegistry.addName(new ItemStack(lorynIngot, 1, 1),
 				"Sorfyn Ingot");
 		OreDictionary.registerOre("ingotSorfyn",
 				new ItemStack(lorynIngot, 1, 1));
 		// avaea
 		LanguageRegistry
 				.addName(new ItemStack(lorynIngot, 1, 2), "Avaea Ingot");
 		OreDictionary
 				.registerOre("ingotAvaea", new ItemStack(lorynIngot, 1, 2));
 	}
 
 	private void smeltingRecipes() {
 		FurnaceRecipes.smelting().addSmelting(
 				ChalithBase.chalithBaseOre.blockID, 0,
 				new ItemStack(ChalithBase.lorynIngot, 1, 0), 0.4F);
 		FurnaceRecipes.smelting().addSmelting(
 				ChalithBase.chalithBaseOre.blockID, 1,
 				new ItemStack(ChalithBase.lorynIngot, 1, 1), 0.6F);
 		FurnaceRecipes.smelting().addSmelting(
 				ChalithBase.chalithBaseOre.blockID, 2,
 				new ItemStack(ChalithBase.lorynIngot, 1, 2), 0.3F);
 		FurnaceRecipes.smelting().addSmelting(
 				ChalithBase.bloodstone.blockID, 1,
 				new ItemStack(ChalithBase.bloodstone, 1, 0), 0.2F);
 		FurnaceRecipes.smelting().addSmelting(
 				ChalithBase.bloodstone.blockID, 3,
 				new ItemStack(ChalithBase.bloodstone, 1, 2), 0.2F);
 
 	}
 
 	private void registerOres() {
 
 		// loryn
 
 		MinecraftForge.setBlockHarvestLevel(chalithBaseOre, "pickaxe", 1);
 		GameRegistry.registerBlock(chalithBaseOre, ChalithOreItemBlock.class,
 				"lorynOre");
 
 		OreDictionary.registerOre("oreLoryn", new ItemStack(chalithBaseOre, 1,
 				0));
 		LanguageRegistry.addName(new ItemStack(chalithBaseOre, 1, 0),
 				"Loryn Ore");
 		// sorfyn
 		OreDictionary.registerOre("oreSorfyn", new ItemStack(chalithBaseOre, 1,
 				1));
 		LanguageRegistry.addName(new ItemStack(chalithBaseOre, 1, 1),
 				"Sorfyn Ore");
 		// avaea
 		LanguageRegistry.addName(new ItemStack(chalithBaseOre, 1, 2),
 				"Avaea Ore");
 		OreDictionary.registerOre("oreAvaea", new ItemStack(chalithBaseOre, 1,
 				2));
 
 	}
 
 	@EventHandler
 	public void postInit(FMLPostInitializationEvent event) {
 		// Stub Method
 	}
 }
