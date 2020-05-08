 
 package hawksmachinery;
 
 import hawksmachinery.blocks.*;
 import hawksmachinery.items.*;
 import hawksmachinery.misc.*;
 import hawksmachinery.tileentity.*;
 import hawksmachinery.HawkProcessingRecipes.HawkEnumProcessing;
 import java.io.File;
 import java.util.List;
 import com.google.common.collect.ObjectArrays;
 import universalelectricity.UniversalElectricity;
 import universalelectricity.BasicComponents;
 import universalelectricity.basiccomponents.ItemBattery;
 import universalelectricity.network.PacketManager;
 import universalelectricity.recipe.CraftingRecipe;
 import universalelectricity.recipe.RecipeManager;
 import vazkii.um.client.ModReleaseType;
 import vazkii.um.client.ModType;
 import vazkii.um.common.ModConverter;
 import vazkii.um.common.UpdateManager;
 import vazkii.um.common.UpdateManagerMod;
 import vazkii.um.common.checking.CheckingMethod;
 import net.minecraft.src.Block;
 import net.minecraft.src.CreativeTabs;
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.EnumToolMaterial;
 import net.minecraft.src.IInventory;
 import net.minecraft.src.Item;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.StepSound;
 import net.minecraft.src.World;
 import net.minecraftforge.common.AchievementPage;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.DungeonHooks;
 import net.minecraftforge.common.EnumHelper;
 import net.minecraftforge.common.ForgeChunkManager;
 import net.minecraftforge.common.ForgeChunkManager.Ticket;
 import net.minecraftforge.oredict.OreDictionary;
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.common.ICraftingHandler;
 import cpw.mods.fml.common.Loader;
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
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.VillagerRegistry;
 
 /**
  * 
  * The mod file for Hawk's Machinery.
  * 
  * @author Elusivehawk
  */
 @Mod(modid = "HawksMachinery", name = "Hawk's Machinery", version = "Alpha v1.4.0", dependencies = "after:BasicComponents")
 @NetworkMod(channels = {"HawksMachinery"}, clientSideRequired = true, serverSideRequired = false, packetHandler = PacketManager.class)
 public class HawksMachinery implements ICraftingHandler
 {
 	@Instance("HawksMachinery")
 	private static HawksMachinery INSTANCE;
 	
 	@SidedProxy(clientSide = "hawksmachinery.HMClientProxy", serverSide = "hawksmachinery.HMCommonProxy")
 	public static HMCommonProxy PROXY;
 	
 	public static RecipeManager RECIPE_GIVER;
 	public static HawkProcessingRecipes PROCESS_RECIPES;
 	public static HawkEnumProcessing CRUSH = HawkProcessingRecipes.HawkEnumProcessing.CRUSHING;
 	public static HawkEnumProcessing WASH = HawkProcessingRecipes.HawkEnumProcessing.WASHING;
 	
 	public static int crusherID;
 	public static int oreID;
 	public static int metalStorageID;
 	public static int washerID;
 	public static int verticalDrillID;
 	public static int smeltingLaserID;
 	public static int chunkloaderID;
 	
 	public static int dustRawID;
 	public static int dustRefinedID;
 	public static int ingotsID;
 	public static int partsID;
 	public static int platingID;
 	public static int drillID;
 	public static int blueprintID;
 	public static int endiumAlloyID;
 	
 	public static int ACHprospector;
 	public static int ACHtimeToCrush;
 	public static int ACHcompactCompact;
 	public static int ACHminerkiin;
 	public static int ACHwash;
 	
 	public static int crusherTicks;
 	public static int washerTicks;
 	public static int maxChunksLoaded = 100;
 	
 	public static boolean generateTitanium;
 	public static boolean generateAluminum;
 	public static boolean generateSilver;
 	public static boolean generateEndium;
 	public static boolean enableUpdateChecking;
 	public static boolean enableAutoDL;
 	public static boolean enableChunkloader;
 	
 	public static final String GUI_PATH = "/hawksmachinery/textures/gui";
 	public static final String BLOCK_TEXTURE_FILE = "/hawksmachinery/textures/blocks.png";
 	public static final String ITEM_TEXTURE_FILE = "/hawksmachinery/textures/items.png";
 	public static final String TEXTURE_PATH = "/hawksmachinery/textures";
 	
 	public static Configuration HMConfig = new Configuration(new File(Loader.instance().getConfigDir(), "HawksMachinery/HMConfig.cfg"));
 	
 	public static Block blockCrusher;
 	public static Block blockOre;
 	public static Block blockMetalStorage;
 	public static Block blockWasher;
 	public static Block blockChunkloader;
 	
 	/**
 	 * Raw dusts! 0 - Coal, 1 - Iron, 2 - Gold, 3 - Copper, 4 - Tin, 5 - Titanium, 6 - Aluminum, 7 - Silver, 8 - Obsidian, 9 - Endium.
 	 */
 	public static Item dustRaw;
 	/**
 	 * Refined dusts! 0 - Diamond, 1 - Ender, 2 - Glass, 3 - Iron, 4 - Gold, 5 - Copper, 6 - Tin, 7 - Titanium, 8 - Aluminum, 9 - Silver, 10 - Emerald, 11 - Nether Star, 12 - Endium.
 	 */
 	public static Item dustRefined;
 	public static Item ingots;
 	public static Item parts;
 	public static Item plating;
 	public static Item blueprints;
 	public static Item endiumAlloy;
 	
 	@PreInit
 	public void preInit(FMLPreInitializationEvent event)
 	{
 		INSTANCE = this;
 		
 		HMConfig.load();
 		
 		crusherID = HMConfig.getBlock("Crusher", 3960).getInt(3960);
 		oreID = HMConfig.getBlock("Ores", 3961).getInt(3961);
 		metalStorageID = HMConfig.getBlock("Metal Storage Blocks", 3962).getInt(3962);
 		washerID = HMConfig.getBlock("Washer", 3963).getInt(3963);
 		smeltingLaserID = HMConfig.getBlock("Smelting Laser", 3964).getInt(3964);
 		
 		generateTitanium = HMConfig.get(Configuration.CATEGORY_GENERAL,"Generate Titanium", true).getBoolean(true);
 		generateAluminum = HMConfig.get(Configuration.CATEGORY_GENERAL, "Generate Aluminum", true).getBoolean(true);
 		generateSilver = HMConfig.get(Configuration.CATEGORY_GENERAL, "Generate Silver", true).getBoolean(true);
 		generateEndium = HMConfig.get(Configuration.CATEGORY_GENERAL, "Generate Endium", true).getBoolean(true);
 		enableUpdateChecking = HMConfig.get(Configuration.CATEGORY_GENERAL, "Enable Update Checking", true).getBoolean(true);
 		enableAutoDL = HMConfig.get(Configuration.CATEGORY_GENERAL, "Enable Auto DL", true).getBoolean(true);
 		enableChunkloader = HMConfig.get(Configuration.CATEGORY_GENERAL, "Enable Chunkloader Block", true).getBoolean(true);
 		
 		if (enableChunkloader)
 		{
 			chunkloaderID = HMConfig.getBlock("Endium Chunkloader", 3966).getInt(3966);
 			maxChunksLoaded = HMConfig.get("Max Chunks Loaded", Configuration.CATEGORY_GENERAL, 25).getInt(25);
 			
 		}
 		
 		dustRawID = HMConfig.get(Configuration.CATEGORY_ITEM, "Raw Dusts", 24150).getInt(24150);
 		dustRefinedID = HMConfig.get(Configuration.CATEGORY_ITEM, "Refined Dusts", 24151).getInt(24151);
 		ingotsID = HMConfig.get(Configuration.CATEGORY_ITEM, "Ingots", 24152).getInt(24152);
 		partsID = HMConfig.get(Configuration.CATEGORY_ITEM, "Parts", 24153).getInt(24153);
 		platingID = HMConfig.get(Configuration.CATEGORY_ITEM, "Plating", 24154).getInt(24154);
 		blueprintID = HMConfig.get(Configuration.CATEGORY_ITEM, "Blueprints", 24155).getInt(24155);
 		endiumAlloyID = HMConfig.get(Configuration.CATEGORY_ITEM, "Endium Alloy", 24156).getInt(24156);
 		
 		ACHprospector = HMConfig.get(Configuration.CATEGORY_GENERAL, "ACH Prospector", 1500).getInt(1500);
 		ACHtimeToCrush = HMConfig.get(Configuration.CATEGORY_GENERAL, "ACH Time To Crush", 1501).getInt(1501);
 		ACHcompactCompact = HMConfig.get(Configuration.CATEGORY_GENERAL, "ACH Compact Compact", 1502).getInt(1502);
 		ACHminerkiin = HMConfig.get(Configuration.CATEGORY_GENERAL, "ACH Minerkiin", 1503).getInt(1503);
 		ACHwash = HMConfig.get(Configuration.CATEGORY_GENERAL, "ACH Wash", 1504).getInt(1504);
 		
 		if (FMLCommonHandler.instance().getSide().isServer())
 		{
 			HMConfig.addCustomCategoryComment("advanced_settings", "Advanced server OP settings, don't be a moron with them.");
 			crusherTicks = HMConfig.get("advanced_settings", "Crusher Ticks", 180).getInt(180);
 			washerTicks = HMConfig.get("advanced_settings", "Washer Ticks", 100).getInt(100);
 			
 		}
 		
 		HMConfig.save();
 		
 		blockCrusher = new HawkBlockGrinder(crusherID).setStepSound(Block.soundMetalFootstep);
 		blockOre = new HawkBlockOre(oreID).setStepSound(Block.soundStoneFootstep);
 		blockMetalStorage = new HawkBlockMetalStorage(metalStorageID).setStepSound(Block.soundMetalFootstep);
 		blockWasher = new HawkBlockWasher(washerID).setStepSound(Block.soundMetalFootstep);
 		if (enableChunkloader)
 		{
 			blockChunkloader = new HawkBlockChunkloader(chunkloaderID);
 			ForgeChunkManager.setForcedChunkLoadingCallback(this, new HawkChunkHandler(this, maxChunksLoaded));
 			
 		}
 		
 		dustRaw = new HawkItemRawDust(dustRawID - 256);
 		dustRefined = new HawkItemRefinedDust(dustRefinedID - 256);
 		ingots = new HawkItemIngots(ingotsID - 256);
 		parts = new HawkItemParts(partsID - 256);
 		plating = new HawkItemPlating(platingID - 256);
 		blueprints = new HawkItemBlueprints(blueprintID - 256);
 		endiumAlloy = new HawkItem(endiumAlloyID - 256).setIconIndex(2).setItemName("endiumAlloy").setCreativeTab(CreativeTabs.tabMaterials);
 		
 		NetworkRegistry.instance().registerGuiHandler(this, this.PROXY);
 		GameRegistry.registerWorldGenerator(new HawkOreGenerator());
 		GameRegistry.registerCraftingHandler(this);
 		AchievementPage.registerAchievementPage(HawkAchievements.HAWKSPAGE);
 		NetworkRegistry.instance().registerConnectionHandler(new HawkConnectionHandler());
 		VillagerRegistry.instance().registerVillageTradeHandler(0, new HawkVillagerTrades());
 		VillagerRegistry.instance().registerVillageTradeHandler(1, new HawkVillagerTrades());
 		VillagerRegistry.instance().registerVillageTradeHandler(2, new HawkVillagerTrades());
 		VillagerRegistry.instance().registerVillageTradeHandler(3, new HawkVillagerTrades());
 		VillagerRegistry.instance().registerVillageTradeHandler(4, new HawkVillagerTrades());
 		
 		DungeonHooks.addDungeonLoot(new ItemStack(ingots, 1, 0), 075, 1, 4);
 		DungeonHooks.addDungeonLoot(new ItemStack(ingots, 1, 1), 075, 1, 4);
 		DungeonHooks.addDungeonLoot(new ItemStack(ingots, 1, 2), 075, 1, 4);
 		
 		GameRegistry.registerTileEntity(HawkTileEntityGrinder.class, "HMCrusher");
 		GameRegistry.registerTileEntity(HawkTileEntityWasher.class, "HMWasher");
 		if (enableChunkloader)
 		{
 			GameRegistry.registerTileEntity(HawkTileEntityChunkloader.class, "HMChunkloader");
 			
 		}
 		
 		OreDictionary.registerOre("ingotTitanium", new ItemStack(ingots, 1, 0));
 		OreDictionary.registerOre("ingotAluminum", new ItemStack(ingots, 1, 1));
 		OreDictionary.registerOre("ingotSilver", new ItemStack(ingots, 1, 2));
 		OreDictionary.registerOre("ingotEndium", new ItemStack(ingots, 1, 3));
 		
 		OreDictionary.registerOre("oreTitanium", new ItemStack(blockOre, 1, 0));
 		OreDictionary.registerOre("oreAluminum", new ItemStack(blockOre, 1, 1));
 		OreDictionary.registerOre("oreBauxium", new ItemStack(blockOre, 1, 1));
 		OreDictionary.registerOre("oreSilver", new ItemStack(blockOre, 1, 2));
 		OreDictionary.registerOre("oreEndium", new ItemStack(blockOre, 1, 3));
 		
 	}
 	
 	@Init
 	public void load(FMLInitializationEvent event)
 	{
 		PROXY.registerRenderInformation();
 		loadRecipes();
 		new HMUpdateHandler(ModConverter.getMod(getClass()));
 		
 	}
 	
 	@PostInit
 	public void modsLoaded(FMLPostInitializationEvent event)
 	{
 		loadProcessingRecipes();
 		
 	}
 	
 	public static HawksMachinery instance()
 	{
 		return INSTANCE;
 	}
 	
 	/**
 	 * Loads all of the recipes for Hawk's Machinery, including regular crafting recipes.
 	 */
 	public static void loadRecipes()
 	{
 		
 		RECIPE_GIVER.addRecipe(new ItemStack(blockCrusher, 1), new Object[]{"TPT", "TMT", "TBT", 'T', new ItemStack(plating, 1, 0), 'P', Item.pickaxeSteel, 'M', BasicComponents.itemMotor, 'B', ((ItemBattery)BasicComponents.itemBattery).getUnchargedItemStack()});
 		RECIPE_GIVER.addRecipe(new ItemStack(blockWasher, 1), new Object[]{"AWA", "BPA", "AbD", 'A', new ItemStack(plating, 1, 1), 'W', Block.cloth, 'P', new ItemStack(parts, 1, 0), 'D', Block.dispenser, 'B', ((ItemBattery)BasicComponents.itemBattery).getUnchargedItemStack(), 'b', Item.bucketEmpty});
 		if (enableChunkloader)
 		{
 			RECIPE_GIVER.addRecipe(new ItemStack(blockChunkloader, 1), new Object[]{"EBE", "BIB", "EBE", 'E', "ingotEndium", 'B', BasicComponents.itemBronzePlate, 'I', Item.eyeOfEnder});
 			RECIPE_GIVER.addShapelessRecipe(new ItemStack(ingots, 4, 3), new Object[]{blockChunkloader});
 			
 		}
 		
 		RECIPE_GIVER.addRecipe(new ItemStack(BasicComponents.itemBattery), new Object[]{" x ", "xrx", "xcx", 'x', BasicComponents.itemTinIngot, 'c', new ItemStack(dustRaw, 1, 0), 'r', Item.redstone});
 		RECIPE_GIVER.addRecipe(new ItemStack(Block.torchWood, 4), new Object[]{"c", "s", 'c', new ItemStack(dustRaw, 1, 0), 's', Item.stick});
 		RECIPE_GIVER.addRecipe(new ItemStack(Block.glass, 1), new Object[]{"GG", "GG", 'G', new ItemStack(dustRefined, 1, 2)});
 		
 		RECIPE_GIVER.addRecipe(new ItemStack(blockMetalStorage, 1, 0), new Object[]{"MMM", "MMM", "MMM", 'M', "ingotTitanium"});
 		RECIPE_GIVER.addRecipe(new ItemStack(blockMetalStorage, 1, 1), new Object[]{"MMM", "MMM", "MMM", 'M', "ingotAluminum"});
 		RECIPE_GIVER.addRecipe(new ItemStack(blockMetalStorage, 1, 2), new Object[]{"MMM", "MMM", "MMM", 'M', "ingotSilver"});
 		RECIPE_GIVER.addRecipe(new ItemStack(blockMetalStorage, 1, 3), new Object[]{"MMM", "MMM", "MMM", 'M', "ingotEndium"});
 		
 		RECIPE_GIVER.addRecipe(new ItemStack(parts, 1, 0), new Object[]{" B ", "PSM", " B ", 'P', BasicComponents.itemSteelPlate, 'S', BasicComponents.itemSteelIngot, 'M', BasicComponents.itemMotor, 'B', Item.blazePowder});
 		RECIPE_GIVER.addRecipe(new ItemStack(parts, 1, 1), new Object[]{" B ", "RGR", "SLS", 'B', Item.blazePowder, 'R', Item.redstone, 'G', Block.glass, 'S', "ingotSteel", 'L', new ItemStack(parts, 1, 3)});
 		RECIPE_GIVER.addRecipe(new ItemStack(parts, 1, 2), new Object[]{" T ", "TET", " T ", 'T', "ingotTitanium", 'E', Item.enderPearl});
 		RECIPE_GIVER.addRecipe(new ItemStack(parts, 1, 3), new Object[]{" G ", "GBG", "cCc", 'G', Block.thinGlass, 'B', Item.blazeRod, 'c', "ingotCopper", 'C', BasicComponents.blockCopperWire});
 		RECIPE_GIVER.addRecipe(new ItemStack(parts, 1, 4), new Object[]{"CC", "CC", 'C', BasicComponents.blockCopperWire});
 		RECIPE_GIVER.addRecipe(new ItemStack(parts, 1, 5), new Object[]{"ici", 'i', "ingotIron", 'c', new ItemStack(parts, 1, 4)});
 		
 		RECIPE_GIVER.addRecipe(new ItemStack(plating, 1, 0), new Object[]{"TT", "TT", 'T', "ingotTitanium"});
 		RECIPE_GIVER.addRecipe(new ItemStack(plating, 1, 1), new Object[]{"AA", "AA", 'A', "ingotAluminum"});
 		RECIPE_GIVER.addRecipe(new ItemStack(plating, 1, 2), new Object[]{"SS", "SS", 'S', "ingotSilver"});
 		RECIPE_GIVER.addRecipe(new ItemStack(plating, 1, 3), new Object[]{"EE", "EE", 'E', "ingotEndium"});
 		
 		RECIPE_GIVER.addShapelessRecipe(BasicComponents.itemSteelDust, new Object[]{new ItemStack(dustRaw, 1, 0), new ItemStack(dustRefined, 1, 3)});
 		RECIPE_GIVER.addShapelessRecipe(new ItemStack(Item.fireballCharge, 3), new Object[]{Item.blazePowder, Item.gunpowder, new ItemStack(dustRaw, 1, 0)});
 		RECIPE_GIVER.addShapelessRecipe(new ItemStack(ingots, 9, 0), new Object[]{new ItemStack(blockMetalStorage, 1, 0)});
 		RECIPE_GIVER.addShapelessRecipe(new ItemStack(ingots, 9, 1), new Object[]{new ItemStack(blockMetalStorage, 1, 1)});
 		RECIPE_GIVER.addShapelessRecipe(new ItemStack(ingots, 9, 2), new Object[]{new ItemStack(blockMetalStorage, 1, 2)});
 		RECIPE_GIVER.addShapelessRecipe(new ItemStack(endiumAlloy, 1), new Object[]{new ItemStack(dustRefined, 1, 12), new ItemStack(dustRefined, 1, 11), new ItemStack(dustRefined, 1, 9)});
 		
 		RECIPE_GIVER.addSmelting(new ItemStack(dustRefined, 1, 2), new ItemStack(Block.thinGlass));
 		RECIPE_GIVER.addSmelting(new ItemStack(dustRefined, 1, 3), new ItemStack(Item.ingotIron));
 		RECIPE_GIVER.addSmelting(new ItemStack(dustRefined, 1, 4), new ItemStack(Item.ingotGold));
 		RECIPE_GIVER.addSmelting(new ItemStack(dustRefined, 1, 5), new ItemStack(BasicComponents.itemCopperIngot));
 		RECIPE_GIVER.addSmelting(new ItemStack(dustRefined, 1, 6), new ItemStack(BasicComponents.itemTinIngot));
 		RECIPE_GIVER.addSmelting(new ItemStack(dustRefined, 1, 7), new ItemStack(ingots, 1, 0));
 		RECIPE_GIVER.addSmelting(new ItemStack(dustRefined, 1, 8), new ItemStack(ingots, 1, 1));
 		RECIPE_GIVER.addSmelting(new ItemStack(dustRefined, 1, 9), new ItemStack(ingots, 1, 2));
 		RECIPE_GIVER.addSmelting(new ItemStack(dustRaw, 1, 8), new ItemStack(Block.obsidian));
 		RECIPE_GIVER.addSmelting(new ItemStack(blockCrusher, 1, 0), new ItemStack(plating, 3, 0));
 		
 		RECIPE_GIVER.addSmelting(new ItemStack(blockOre, 1, 0), new ItemStack(ingots, 1, 0));
 		RECIPE_GIVER.addSmelting(new ItemStack(blockOre, 1, 1), new ItemStack(ingots, 1, 1));
 		RECIPE_GIVER.addSmelting(new ItemStack(blockOre, 1, 2), new ItemStack(ingots, 1, 2));
 		
 		RECIPE_GIVER.addSmelting(new ItemStack(plating, 1, 0), new ItemStack(ingots, 3, 0));
 		RECIPE_GIVER.addSmelting(new ItemStack(plating, 1, 1), new ItemStack(ingots, 3, 1));
 		RECIPE_GIVER.addSmelting(new ItemStack(plating, 1, 2), new ItemStack(ingots, 3, 2));
 		RECIPE_GIVER.addSmelting(new ItemStack(plating, 1, 3), new ItemStack(ingots, 3, 3));
 		RECIPE_GIVER.addSmelting(new ItemStack(blockMetalStorage, 1, 0), new ItemStack(ingots, 9, 0));
 		RECIPE_GIVER.addSmelting(new ItemStack(blockMetalStorage, 1, 1), new ItemStack(ingots, 9, 1));
 		RECIPE_GIVER.addSmelting(new ItemStack(blockMetalStorage, 1, 2), new ItemStack(ingots, 9, 2));
 		RECIPE_GIVER.addSmelting(new ItemStack(blockMetalStorage, 1, 3), new ItemStack(ingots, 9, 3));
 		
 	}
 	
 	public static void loadProcessingRecipes()
 	{
 		
 		PROCESS_RECIPES.addHawkProcessingRecipe(Item.diamond, new ItemStack(dustRefined, 1, 0), CRUSH);
 		PROCESS_RECIPES.addHawkProcessingRecipe(Item.enderPearl, new ItemStack(dustRefined, 1, 1), CRUSH);
 		PROCESS_RECIPES.addHawkProcessingRecipe(Block.glass, new ItemStack(dustRefined, 4, 2), CRUSH);
 		PROCESS_RECIPES.addHawkProcessingRecipe(BasicComponents.itemCopperIngot, new ItemStack(dustRaw, 1, 3), CRUSH);
 		PROCESS_RECIPES.addHawkProcessingRecipe(BasicComponents.itemTinIngot, new ItemStack(dustRaw, 1, 4), CRUSH);
 		PROCESS_RECIPES.addHawkProcessingRecipe(Item.blazeRod, new ItemStack(Item.blazePowder, 2), CRUSH);
 		PROCESS_RECIPES.addHawkProcessingRecipe(Item.bone, new ItemStack(Item.dyePowder, 4, 15), CRUSH);
 		PROCESS_RECIPES.addHawkProcessingRecipe(Block.stone, new ItemStack(Block.gravel), CRUSH);
 		PROCESS_RECIPES.addHawkProcessingRecipe(Block.cobblestone, new ItemStack(Block.sand), CRUSH);
 		PROCESS_RECIPES.addHawkProcessingRecipe(Block.gravel, new ItemStack(Item.flint, 2), CRUSH);
 		
 		PROCESS_RECIPES.addHawkProcessingRecipe(Item.eyeOfEnder, new ItemStack(dustRefined, 1, 1), CRUSH);
 		PROCESS_RECIPES.addHawkProcessingRecipe(Block.pistonBase, new ItemStack(dustRefined, 1, 3), CRUSH);
 		PROCESS_RECIPES.addHawkProcessingRecipe(Block.pistonStickyBase, new ItemStack(dustRefined, 1, 3), CRUSH);
 		PROCESS_RECIPES.addHawkProcessingRecipe(Block.dispenser, new ItemStack(Item.bow), CRUSH);
 		PROCESS_RECIPES.addHawkProcessingRecipe(Block.stoneOvenIdle, new ItemStack(Block.cobblestone, 8), CRUSH);
 		PROCESS_RECIPES.addHawkProcessingRecipe(Block.thinGlass, new ItemStack(dustRefined, 1, 2), CRUSH);
 		PROCESS_RECIPES.addHawkProcessingRecipe(Block.glowStone, new ItemStack(Item.lightStoneDust, 4), CRUSH);
 		PROCESS_RECIPES.addHawkProcessingRecipe(Block.redstoneLampIdle, new ItemStack(Item.lightStoneDust, 4), CRUSH);
 		PROCESS_RECIPES.addHawkProcessingRecipe(Block.enchantmentTable, new ItemStack(Item.diamond, 2), CRUSH);
 		PROCESS_RECIPES.addHawkProcessingRecipe(Item.brewingStand, new ItemStack(Item.blazeRod, 1), CRUSH);
 		PROCESS_RECIPES.addHawkProcessingRecipe(Block.sandStone, new ItemStack(Block.sand, 4), CRUSH);
 		PROCESS_RECIPES.addHawkProcessingRecipe(Block.obsidian, new ItemStack(dustRaw, 1, 8), CRUSH);
 		PROCESS_RECIPES.addHawkProcessingRecipe(Item.glassBottle, new ItemStack(dustRefined, 4, 2), CRUSH);
 		PROCESS_RECIPES.addHawkProcessingRecipe(Block.thinGlass, new ItemStack(dustRefined, 1, 2), CRUSH);
 		
 		PROCESS_RECIPES.addHawkProcessingRecipe(Item.minecartEmpty, new ItemStack(dustRefined, 5, 3), CRUSH);
 		PROCESS_RECIPES.addHawkProcessingRecipe(Item.minecartPowered, new ItemStack(dustRefined, 5, 3), CRUSH);
 		PROCESS_RECIPES.addHawkProcessingRecipe(Item.minecartCrate, new ItemStack(dustRefined, 5, 3), CRUSH);
 		PROCESS_RECIPES.addHawkProcessingRecipe(Item.doorSteel, new ItemStack(dustRefined, 6, 3), CRUSH);
 		PROCESS_RECIPES.addHawkProcessingRecipe(Item.bucketEmpty, new ItemStack(dustRefined, 3, 3), CRUSH);
 		PROCESS_RECIPES.addHawkProcessingRecipe(Item.compass, new ItemStack(dustRefined, 4, 3), CRUSH);
 		PROCESS_RECIPES.addHawkProcessingRecipe(Item.pocketSundial, new ItemStack(dustRefined, 4, 4), CRUSH);
 		PROCESS_RECIPES.addHawkProcessingRecipe(Item.cauldron, new ItemStack(dustRefined, 7, 3), CRUSH);
 		PROCESS_RECIPES.addHawkProcessingRecipe(Block.plantRed, new ItemStack(Item.dyePowder, 2, 1), CRUSH);
 		PROCESS_RECIPES.addHawkProcessingRecipe(Block.plantYellow, new ItemStack(Item.dyePowder, 2, 11), CRUSH);
 		
 		PROCESS_RECIPES.addHawkProcessingRecipe(Item.emerald, new ItemStack(dustRefined, 1, 10), CRUSH);
 		PROCESS_RECIPES.addHawkProcessingRecipe(new ItemStack(Item.coal, 1, 0), new ItemStack(dustRaw, 1, 0), CRUSH);
 		PROCESS_RECIPES.addHawkProcessingRecipe(new ItemStack(Item.coal, 1, 1), new ItemStack(dustRaw, 1, 0), CRUSH);
 		PROCESS_RECIPES.addHawkProcessingRecipe(new ItemStack(Block.stoneBrick, 1, 0), new ItemStack(Block.stoneBrick, 1), CRUSH);
 		PROCESS_RECIPES.addHawkProcessingRecipe(new ItemStack(Block.stoneBrick, 1, 1), new ItemStack(Block.cobblestone), CRUSH);
 		PROCESS_RECIPES.addHawkProcessingRecipe(new ItemStack(Block.stoneBrick, 1, 2), new ItemStack(Block.cobblestoneMossy), CRUSH);
 		PROCESS_RECIPES.addHawkProcessingRecipe(new ItemStack(Block.stoneBrick, 1, 3), new ItemStack(Block.cobblestone), CRUSH);
 		
 		PROCESS_RECIPES.addHawkFoDProcessingRecipe("oreIron", new ItemStack(dustRaw, 2, 1), CRUSH);
 		PROCESS_RECIPES.addHawkFoDProcessingRecipe("oreGold", new ItemStack(dustRaw, 2, 2), CRUSH);
 		PROCESS_RECIPES.addHawkFoDProcessingRecipe("oreCopper", new ItemStack(dustRaw, 2, 3), CRUSH);
 		PROCESS_RECIPES.addHawkFoDProcessingRecipe("oreTin", new ItemStack(dustRaw, 2, 4), CRUSH);
 		PROCESS_RECIPES.addHawkFoDProcessingRecipe("oreAluminum", new ItemStack(dustRaw, 2, 5), CRUSH);
 		PROCESS_RECIPES.addHawkFoDProcessingRecipe("oretitanium", new ItemStack(dustRaw, 2, 6), CRUSH);
 		PROCESS_RECIPES.addHawkFoDProcessingRecipe("oreSilver", new ItemStack(dustRaw, 2, 7), CRUSH);
 		PROCESS_RECIPES.addHawkFoDProcessingRecipe("oreEndium", new ItemStack(dustRaw, 2, 9), CRUSH);
 		
 		PROCESS_RECIPES.addHawkFoDProcessingRecipe("ingotCopper", new ItemStack(dustRefined, 1, 5), CRUSH);
 		PROCESS_RECIPES.addHawkFoDProcessingRecipe("ingotTin", new ItemStack(dustRefined, 1, 6), CRUSH);
 		PROCESS_RECIPES.addHawkFoDProcessingRecipe("ingotTitanium", new ItemStack(dustRefined, 1, 7), CRUSH);
 		PROCESS_RECIPES.addHawkFoDProcessingRecipe("ingotAluminum", new ItemStack(dustRefined, 1, 8), CRUSH);
 		PROCESS_RECIPES.addHawkFoDProcessingRecipe("ingotSilver", new ItemStack(dustRefined, 1, 9), CRUSH);
 		PROCESS_RECIPES.addHawkFoDProcessingRecipe("ingotEndium", new ItemStack(dustRefined, 1, 12), CRUSH);
 		PROCESS_RECIPES.addHawkFoDProcessingRecipe("ingotSteel", new ItemStack(BasicComponents.itemSteelDust), CRUSH);
 		PROCESS_RECIPES.addHawkFoDProcessingRecipe("ingotBronze", new ItemStack(BasicComponents.itemBronzeDust), CRUSH);
 		
 		//WASHING RECIPES
 		PROCESS_RECIPES.addHawkProcessingRecipe(new ItemStack(dustRaw, 1, 1), new ItemStack(dustRefined, 1, 3), WASH);
 		PROCESS_RECIPES.addHawkProcessingRecipe(new ItemStack(dustRaw, 1, 2), new ItemStack(dustRefined, 1, 4), WASH);
 		PROCESS_RECIPES.addHawkProcessingRecipe(new ItemStack(dustRaw, 1, 3), new ItemStack(dustRefined, 1, 5), WASH);
 		PROCESS_RECIPES.addHawkProcessingRecipe(new ItemStack(dustRaw, 1, 4), new ItemStack(dustRefined, 1, 6), WASH);
 		PROCESS_RECIPES.addHawkProcessingRecipe(new ItemStack(dustRaw, 1, 5), new ItemStack(dustRefined, 1, 7), WASH);
 		PROCESS_RECIPES.addHawkProcessingRecipe(new ItemStack(dustRaw, 1, 6), new ItemStack(dustRefined, 1, 8), WASH);
 		PROCESS_RECIPES.addHawkProcessingRecipe(new ItemStack(dustRaw, 1, 7), new ItemStack(dustRefined, 1, 9), WASH);
 		PROCESS_RECIPES.addHawkProcessingRecipe(new ItemStack(dustRaw, 1, 9), new ItemStack(dustRefined, 1, 12), WASH);
 		
 	}
 	
 	@Override
 	public void onCrafting(EntityPlayer player, ItemStack item, IInventory craftMatrix)
 	{
 		if (item.itemID == blockCrusher.blockID)
 		{
 			player.addStat(HawkAchievements.timeToCrush, 1);
 		}
 		
 		if (item.itemID == blockMetalStorage.blockID)
 		{
 			player.addStat(HawkAchievements.compactCompact, 1);
 		}
 		
 	}
 	
 	@Override
 	public void onSmelting(EntityPlayer player, ItemStack item){}
 	
 	public class HMUpdateHandler extends UpdateManagerMod
 	{
 		public HMUpdateHandler(Mod m)
 		{
 			super(m);
 		}
 		
 		@Override
 		public String getModURL()
 		{
 			return "http://bit.ly/UeISkn";
 		}
 		
 		@Override
 		public String getUpdateURL()
 		{
 			return "https://dl.dropbox.com/u/100525141/HawksMachineryVersion.txt";
 		}
 		
 		@Override
 		public ModType getModType()
 		{
 			return ModType.ADDON;
 		}
 		
 		@Override
 		public String getModName()
 		{
 			return "Hawk's Machinery";
 		}
 		
 		@Override
 		public String getChangelogURL()
 		{
 			return "https://dl.dropbox.com/u/100525141/HawksMachineryAlphav133Changelog.txt";
 		}
 		
 		@Override
 		public String getDirectDownloadURL()
 		{
 			return "https://dl.dropbox.com/u/100525141/HawksMachneryDLLink.txt";
 		}
 		
 		@Override
 		public String getDisclaimerURL()
 		{
 			return "https://dl.dropbox.com/u/100525141/HawksMachineryDisclaimer.txt";
 		}
 		
 		@Override
 		public boolean enableAutoDownload()
 		{
 			return enableAutoDL;
 		}
 		
 		@Override
 		public CheckingMethod getCheckingMethod()
 		{
 			return CheckingMethod.LEXICOGRAPHICAL;
 		}
 		
 		@Override
 		public boolean disableChecks()
 		{
 			return UpdateManager.isDebug ? true : !enableUpdateChecking;
 		}
 		
 		@Override
 		public boolean srcOnly()
 		{
 			return false;
 		}
 		
 		@Override
 		public ModReleaseType getReleaseType()
 		{
			return ModReleaseType.DEVBUILD;
 		}
 		
 		@Override
 		public ItemStack getIconStack()
 		{
 			return new ItemStack(blockCrusher);
 		}
 		
 		@Override
 		public String getSpecialButtonName()
 		{
 			return "Forge Forum";
 		}
 		
 		@Override
 		public void onSpecialButtonClicked()
 		{
 			UpdateManager.openWebpage("http://bit.ly/TbyBoC");
 			
 		}
 		
 	}
 	
 	/**
 	 * 
 	 * Convenience function for sending a message to a player in SSP.
 	 * 
 	 * @param message The message being sent.
 	 */
 	public static void chatToSSPPlayer(String message)
 	{
 		PROXY.chatToSSPPlayer(message);
 	}
 	
 }
