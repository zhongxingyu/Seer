 package tektor.minecraft.chalith;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.oredict.OreDictionary;
 import tektor.minecraft.chalith.blocks.AvaeaOre;
 import tektor.minecraft.chalith.blocks.BlockTrapRune;
 import tektor.minecraft.chalith.blocks.LorynOre;
 import tektor.minecraft.chalith.items.AvaeaIngot;
 import tektor.minecraft.chalith.items.BaseRune;
 import tektor.minecraft.chalith.items.TrapRune;
 import tektor.minecraft.chalith.items.LorynIngot;
 import tektor.minecraft.chalith.items.RecallRune;
 import tektor.minecraft.chalith.items.RuneSymbol;
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
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
@Mod(modid = "Chalith", name = "Chalith", version = "0.3.0")
 @NetworkMod(clientSideRequired = true, serverSideRequired = false)
 public class ChalithBase {
 
 	// The instance of your mod that Forge uses.
 	@Instance("ChalithBase")
 	public static ChalithBase instance;
 
 	// blocks
 	public static int blockID1, blockID2, blockID3;
 	public static Block avaeaOre;
 	public static Block lorynOre;
 	public static Block fireTrapRune;
 
 	// items
 	public static int itemID1, itemID2, itemID3, itemID4, itemID5, itemID6;
 	public static Item avaeaIngot;
 	public static Item lorynIngot;
 	public static Item recallRune;
 	public static Item fireTrapRune2;
 	public static Item baseRune;
 	public static Item runeSymbol;
 
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
 		smeltingRecipes();
 		runeCrafting();
 		GameRegistry.registerWorldGenerator(new ChalithWorldGen());
 	}
 
 	private void initializeID() {
 		// blocks
 		avaeaOre = new AvaeaOre(blockID1);
 		lorynOre = new LorynOre(blockID2);
 		fireTrapRune = new BlockTrapRune(blockID3);
 		// items
 		avaeaIngot = new AvaeaIngot(itemID1);
 		lorynIngot = new LorynIngot(itemID2);
 		recallRune = new RecallRune(itemID3);
 		fireTrapRune2 = new TrapRune(itemID6);
 		baseRune = new BaseRune(itemID4);
 		runeSymbol = new RuneSymbol(itemID5);
 	}
 
 	private void registerTileEntities() {
 		GameRegistry.registerTileEntity(
 				tektor.minecraft.chalith.entity.TrapRuneTileEntity.class,
 				"Fire Trap Rune");
 
 	}
 
 	private void runeCrafting() {
 		ItemStack diStack = new ItemStack(this.runeSymbol, 1, 0);
 		ItemStack inStack = new ItemStack(this.runeSymbol, 1, 1);
 		ItemStack nomStack = new ItemStack(this.runeSymbol, 1, 2);
 		ItemStack xenStack = new ItemStack(this.runeSymbol, 1, 3);
 		ItemStack borStack = new ItemStack(this.runeSymbol, 1, 6);
 		ItemStack cesStack = new ItemStack(this.runeSymbol, 1, 7);
 		ItemStack hirStack = new ItemStack(this.runeSymbol, 1, 9);
 		ItemStack voStack = new ItemStack(this.runeSymbol, 1, 13);
 		ItemStack avaeaIngotStack = new ItemStack(this.avaeaIngot, 1);
 		ItemStack lorynIngotStack = new ItemStack(this.lorynIngot, 1);
 		ItemStack baseRuneStack = new ItemStack(this.baseRune, 1);
 		ItemStack goldIngotStack = new ItemStack(Item.ingotGold, 1);
 
 		GameRegistry.addShapedRecipe(new ItemStack(this.baseRune, 1),
 				new Object[] { "XXX", "X X", "XXX", 'X', avaeaIngotStack });
 		//Recall
 		GameRegistry.addShapedRecipe(new ItemStack(this.recallRune, 1),
 				new Object[] { "ABC", " X ", "   ", 'A', diStack, 'B', inStack,
 						'C', nomStack, 'X', baseRuneStack });
 		//FireTrap
		GameRegistry.addShapedRecipe(new ItemStack(this.fireTrapRune2, 1),
 				new Object[] { "ABC", " X ", "   ", 'A', xenStack, 'B',
 						voStack, 'C', borStack, 'X', baseRuneStack });
 		//IceTrap
		GameRegistry.addShapedRecipe(new ItemStack(this.fireTrapRune2, 1),
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
 		// Bor
 		GameRegistry.addShapedRecipe(new ItemStack(this.runeSymbol, 1, 6),
 				new Object[] { "A  ", "AA ", "A  ", 'A', lorynIngotStack });
 		// Ces
 		GameRegistry.addShapedRecipe(new ItemStack(this.runeSymbol, 1, 7),
 				new Object[] { "  A", " A ", "  A", 'A', lorynIngotStack });
 		// Hir
 		GameRegistry.addShapedRecipe(new ItemStack(this.runeSymbol, 1, 9),
 				new Object[] { "A A", " A ", "A A", 'A', lorynIngotStack });
 		// Vo
 		GameRegistry.addShapedRecipe(new ItemStack(this.runeSymbol, 1, 13),
 				new Object[] { "A A", "A A", " A ", 'A', lorynIngotStack });
 
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
 		LanguageRegistry.addName(recallRune, "Recall Rune");
 		GameRegistry.registerItem(recallRune, "recallRune");
 		// FireTrap
 		LanguageRegistry.addName(fireTrapRune2, "Fire Trap Rune");
 		GameRegistry.registerItem(fireTrapRune2, "fireTrapRune");
 		GameRegistry.registerBlock(fireTrapRune, "fireTrapRuneBlock");
 		LanguageRegistry.addName(new ItemStack(fireTrapRune,1,0), "Fire Trap Rune");
 		//Ice Trap
 		LanguageRegistry.addName(new ItemStack(fireTrapRune,1,1), "Ice Trap Rune");
 
 	}
 
 	private void registerIngots() {
 		// avaea
 		LanguageRegistry.addName(avaeaIngot, "Avaea Ingot");
 		GameRegistry.registerItem(avaeaIngot, "avaeaIngot");
 		OreDictionary.registerOre("ingotAvaea", new ItemStack(avaeaIngot));
 		// loryn
 		LanguageRegistry.addName(lorynIngot, "Loryn Ingot");
 		GameRegistry.registerItem(lorynIngot, "lorynIngot");
 		OreDictionary.registerOre("ingotLoryn", new ItemStack(lorynIngot));
 
 	}
 
 	private void smeltingRecipes() {
 		GameRegistry.addSmelting(ChalithBase.avaeaOre.blockID, new ItemStack(
 				ChalithBase.avaeaIngot), 0.4f);
 		GameRegistry.addSmelting(ChalithBase.lorynOre.blockID, new ItemStack(
 				ChalithBase.lorynIngot), 0.4f);
 
 	}
 
 	private void registerOres() {
 		// avaea
 		LanguageRegistry.addName(avaeaOre, "Avaea Ore");
 		MinecraftForge.setBlockHarvestLevel(avaeaOre, "pickaxe", 1);
 		GameRegistry.registerBlock(avaeaOre, "avaeaOre");
 		OreDictionary.registerOre("oreAvaea", new ItemStack(avaeaOre));
 		// loryn
 		LanguageRegistry.addName(lorynOre, "Loryn Ore");
 		MinecraftForge.setBlockHarvestLevel(lorynOre, "pickaxe", 1);
 		GameRegistry.registerBlock(lorynOre, "lorynOre");
 		OreDictionary.registerOre("oreLoryn", new ItemStack(lorynOre));
 
 	}
 
 	@EventHandler
 	public void postInit(FMLPostInitializationEvent event) {
 		// Stub Method
 	}
 }
