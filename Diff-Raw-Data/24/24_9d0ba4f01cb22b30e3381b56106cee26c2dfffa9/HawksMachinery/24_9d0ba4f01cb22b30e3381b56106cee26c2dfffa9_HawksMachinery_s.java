 
 package hawksmachinery.common;
 
 import hawksmachinery.common.api.HMContent;
 import hawksmachinery.common.api.HMRecipes;
 import hawksmachinery.common.api.HMRecipes.HMEnumProcessing;
 import hawksmachinery.common.block.*;
 import hawksmachinery.common.item.*;
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.FurnaceRecipes;
 import net.minecraftforge.common.ForgeChunkManager;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.oredict.OreDictionary;
 import net.minecraftforge.oredict.ShapedOreRecipe;
 import net.minecraftforge.oredict.ShapelessOreRecipe;
 import universalelectricity.prefab.network.ConnectionHandler;
 import universalelectricity.prefab.network.PacketManager;
 import universalelectricity.prefab.ore.OreGenerator;
 import updatemanager.client.ModReleaseType;
 import updatemanager.client.ModType;
 import updatemanager.common.ModConverter;
 import updatemanager.common.UpdateManager;
 import updatemanager.common.UpdateManagerMod;
 import updatemanager.common.checking.CheckingMethod;
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
 @Mod(modid = "HawksMachinery", name = "Hawk's Machinery", version = HawksMachinery.VERSION, useMetadata = true)
 @NetworkMod(channels = {"HawksMachinery"}, clientSideRequired = true, serverSideRequired = false, connectionHandler = ConnectionHandler.class, packetHandler = PacketManager.class)
 public class HawksMachinery
 {
 	public static final String VERSION = "Beta v1.0.0 Prerelease 5";
 	
 	@Instance("HawksMachinery")
 	private static HawksMachinery INSTANCE;
 	
 	@SidedProxy(clientSide = "hawksmachinery.client.HMClientProxy", serverSide = "hawksmachinery.common.HMCommonProxy")
 	public static HMCommonProxy PROXY;
 	
 	public HMManager MANAGER;
 	
 	public CreativeTabs tab = new HMCreativeTab("HMTab");
 	
 	public static HMRecipes PROCESS_RECIPES;
 	
 	public static final String GUI_PATH = "/hawksmachinery/client/resources/gui";
 	public static final String BLOCK_TEXTURE_FILE = "/hawksmachinery/client/resources/textures/blocks.png";
 	public static final String ITEM_TEXTURE_FILE = "/hawksmachinery/client/resources/textures/items.png";
 	public static final String TEXTURE_PATH = "/hawksmachinery/client/resources/textures";
 	public static final String SOUND_PATH = "/hawksmachinery/client/resources/sounds";
 	public static final String LANG_PATH = "/hawksmachinery/client/resources/lang";
 	
 	@PreInit
 	public void preInit(FMLPreInitializationEvent event)
 	{
 		INSTANCE = this;
 		this.MANAGER = new HMManager();
 		
 		this.MANAGER.loadConfig();
 
 		HMBlock.crusher = new HMBlockCrusher(this.MANAGER.getBlockID("Crusher", 2550));
 		HMBlock.ore = new HMBlockOre(this.MANAGER.getBlockID("Ore", 2551));
 		HMBlock.washer = new HMBlockWasher(this.MANAGER.getBlockID("Washer", 2552));
 		HMBlock.endiumChunkloader = new HMBlockEndiumChunkloader(this.MANAGER.getBlockID("Endium Chunkloader", 2553));
 		HMBlock.endiumTeleporter = new HMBlockEndiumTeleporter(this.MANAGER.getBlockID("Endium Teleporter", 2554));
 		HMBlock.fisher = new HMBlockFisher(this.MANAGER.getBlockID("Fisher", 2555));
 		HMBlock.metalBlock = new HMBlockMetalStorage(this.MANAGER.getBlockID("Metal Block", 2556));
 		HMBlock.starForge = new HMBlockStarForge(this.MANAGER.getBlockID("Star Forge", 2557));
 		HMBlock.starForgeTechnical = new HMBlockMulti(this.MANAGER.getBlockID("Star Forge Technical", 2558));
 		HMBlock.sinterer = new HMBlockSinterer(this.MANAGER.getBlockID("Sinterer", 2559));
 		//HMBlock.fireBlock = new HMBlockFireBlock(this.MANAGER.getBlockID("Fire Block", 2560));
 		
 		HMItem.dustRaw = new HMItemRawDust(this.MANAGER.getItemID("Raw Dusts", 24150)).setMaxDmg(8);
 		HMItem.dustRefined = new HMItemRefinedDust(this.MANAGER.getItemID("Refined Dusts", 24151)).setMaxDmg(10);
 		HMItem.parts = new HMItemParts(this.MANAGER.getItemID("Parts", 24152)).setMaxDmg(6);
 		HMItem.blueprints = new HMItemBlueprints(this.MANAGER.getItemID("Blueprints", 24153)).setMaxDmg(8);
 		HMItem.plating = new HMItemPlating(this.MANAGER.getItemID("Plating", 24154)).setMaxDmg(1);
 		HMItem.rivets = new HMItemRivets(this.MANAGER.getItemID("Rivets", 24155)).setMaxDmg(6);
 		HMItem.rivetGun = new HMItemRivetGun(this.MANAGER.getItemID("Rivet Gun", 24156));
 		HMItem.ingots = new HMItemIngots(this.MANAGER.getItemID("Ingots", 24157)).setMaxDmg(1);
 		HMItem.fishFood = new HMItem(this.MANAGER.getItemID("Fish Food", 24158)).setIconIndex(104).setItemName("HMFishFood").setCreativeTab(this.tab);
 		HMItem.machineMeter = new HMItemMeter(this.MANAGER.getItemID("Machine Meter", 24159));
 		HMItem.cobaltBone = new HMItemWolfTamer(this.MANAGER.getItemID("Cobalt Bone", 24160)).setItemName("HMCobaltBone").setCreativeTab(this.tab).setIconIndex(105);
 		HMItem.testTube = new HMItem(this.MANAGER.getItemID("Test Tube", 24161)).setItemName("HMTestTube").setCreativeTab(this.tab).setIconIndex(72);
 		HMItem.nanites = new HMItemNanite(this.MANAGER.getItemID("Nanites", 24162)).setMaxDmg(15);
 		
 		new HMContent();
 		NetworkRegistry.instance().registerGuiHandler(this, PROXY);
 		GameRegistry.registerCraftingHandler(this.MANAGER);
 		NetworkRegistry.instance().registerConnectionHandler(PROXY);
 		VillagerRegistry.instance().registerVillageTradeHandler(0, this.MANAGER);
 		VillagerRegistry.instance().registerVillageTradeHandler(1, this.MANAGER);
 		VillagerRegistry.instance().registerVillageTradeHandler(2, this.MANAGER);
 		VillagerRegistry.instance().registerVillageTradeHandler(3, this.MANAGER);
 		VillagerRegistry.instance().registerVillageTradeHandler(4, this.MANAGER);
 		ForgeChunkManager.setForcedChunkLoadingCallback(this, this.MANAGER);
 		MinecraftForge.EVENT_BUS.register(this.MANAGER);
 		
 	}
 	
 	@Init
 	public void load(FMLInitializationEvent event)
 	{
 		new HMUpdateHandler(ModConverter.getMod(this.getClass()));
 		OreGenerator.addOre(new HMEndiumOreGen());
 		OreGenerator.addOre(new HMCobaltOreGen());
 		PROXY.registerRenderInformation();
 		
 	}
 	
 	@PostInit
 	public void modsLoaded(FMLPostInitializationEvent event)
 	{
 		this.loadRecipes();
 		this.loadProcessingRecipes();
 		
 	}
 	
 	public static HawksMachinery instance()
 	{
 		return INSTANCE;
 	}
 	
 	/**
 	 * Loads all of the vMC recipes for Hawk's Machinery.
 	 */
 	public void loadRecipes()
 	{
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(HMBlock.crusher), new Object[]{"IPI", "IEI", "ICI", 'I', "ingotCobalt", 'P', Item.pickaxeSteel, 'E', new ItemStack(HMItem.parts, 1, 6), 'C', Item.ingotGold}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(HMBlock.washer), new Object[]{"iBi", "iWi", "IEI", 'i', "ingotCobalt", 'B', Item.bucketEmpty, 'I', Block.blockSteel, 'W', Block.cloth, 'E', new ItemStack(HMItem.parts, 1, 6)}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(((HMItemRivetGun)HMItem.rivetGun).getUncharged(), new Object[]{"CLC", "IBI", "IPI", 'C', "ingotCobalt", 'I', Item.ingotIron, 'L', Block.lever, 'B', Item.ingotGold, 'P', Block.pistonBase}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(HMBlock.fisher), new Object[]{"CEC", "CFC", "CHC", 'C', "ingotCobalt", 'E', new ItemStack(HMItem.parts, 1, 6), 'F', Item.fishingRod, 'H', Block.chest}));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(HMBlock.sinterer), new Object[]{"CHC", "CFC", "CHC", 'C', "ingotCobalt", 'H', new ItemStack(HMItem.parts, 1, 4)}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(HMBlock.starForge), new Object[]{" N ", "CSC", "GGG", 'N', Item.netherStar, 'C', new ItemStack(HMItem.plating, 1, 1), 'S', HMBlock.sinterer, 'G', Item.ingotGold}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(HMItem.machineMeter), new Object[]{"BCg", "ECs", "GsI", 'B', Item.blazeRod, 'C', "ingotCobalt", 'g', Item.goldNugget, 'E', Item.eyeOfEnder, 's', Item.silk, 'i', new ItemStack(HMItem.dustRefined, 1, 3)}));
 		
 		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(HMBlock.crusher), new Object[]{"ingotCobalt", "ingotCobalt", Item.pickaxeSteel, new ItemStack(HMItem.blueprints), new ItemStack(HMItem.parts, 1, 6)}));
 		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(HMBlock.washer), new Object[]{Item.ingotIron, Item.ingotIron, Item.ingotIron, Item.ingotIron, Item.bucketEmpty, new ItemStack(HMItem.blueprints, 1, 1), Block.cloth, new ItemStack(HMItem.parts, 1, 6)}));
 		GameRegistry.addRecipe(new ShapelessOreRecipe(((HMItemRivetGun)HMItem.rivetGun).getUncharged(), new Object[]{"ingotCobalt", Item.ingotIron, Block.lever, new ItemStack(HMItem.plating, 1, 1), Item.goldNugget, new ItemStack(HMItem.blueprints, 1, 8), Block.pistonBase}));
 		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(HMBlock.fisher), new Object[]{"ingotCobalt", "ingotCobalt", new ItemStack(HMItem.blueprints, 1, 3), Block.chest, Item.fishingRod, new ItemStack(HMItem.parts, 1, 6)}));
 		
 		if (this.MANAGER.enableChunkloader)
 		{
 			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(HMBlock.endiumChunkloader), new Object[]{"ECE", "C@C", "ECE", 'E', new ItemStack(HMItem.ingots), 'C', new ItemStack(HMItem.plating, 1, 1), '@', Item.eyeOfEnder}));
 			GameRegistry.addShapelessRecipe(new ItemStack(HMItem.ingots, 4, 0), HMBlock.endiumChunkloader);
 			
 		}
 		
 		GameRegistry.addRecipe(new ItemStack(Block.torchWood, 4), new Object[]{"c", "s", 'c', new ItemStack(HMItem.dustRaw, 1, 0), 's', Item.stick});
 		GameRegistry.addRecipe(new ItemStack(Block.glass, 1), new Object[]{"GG", "GG", 'G', new ItemStack(HMItem.dustRefined, 1, 2)});
 		
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(HMItem.parts, 1, 0), new Object[]{"IBG", "ICP", "IBG", 'I', Item.ingotIron, 'C', "ingotCobalt", 'P', Block.pistonBase, 'B', Item.blazePowder, 'G', Item.ingotGold}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(HMItem.parts, 1, 1), new Object[]{" B ", "RGR", "CLC", 'B', Item.blazePowder, 'R', Item.redstone, 'G', Block.glass, 'C', "ingotCobalt", 'L', new ItemStack(HMItem.parts, 1, 3)}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(HMItem.parts, 6, 2), new Object[]{"C", "C", "C", 'C', "ingotCobalt"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(HMItem.parts, 1, 3), new Object[]{" G ", "GBG", "CCC", 'G', Block.thinGlass, 'B', Item.blazeRod, 'C', Item.goldNugget}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(HMItem.parts, 1, 4), new Object[]{"CCC", "BCB", 'C', Block.fenceIron, 'B', Item.blazePowder}));
 		GameRegistry.addRecipe(new ItemStack(HMItem.parts, 1, 5), new Object[]{"ici", 'i', Item.ingotIron, 'c', new ItemStack(HMItem.parts, 1, 4)});
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(HMItem.parts, 1, 6), new Object[]{"OOC", "BPg", "OOC", 'O', Block.obsidian, 'C', "ingotCobalt", 'B', Item.blazePowder, 'P', new ItemStack(HMItem.parts), 'g', Item.goldNugget}));
 		
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(HMItem.rivets, 8, 0), new Object[]{"CCC", " C ", " B ", 'C', "ingotCopper", 'B', Item.blazeRod}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(HMItem.rivets, 8, 1), new Object[]{"BBB", " B ", " b ", 'B', "ingotBronze", 'b', Item.blazeRod}));
 		GameRegistry.addRecipe(new ItemStack(HMItem.rivets, 8, 2), new Object[]{"III", " I ", " B ", 'I', Item.ingotIron, 'B', Item.blazePowder});
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(HMItem.rivets, 8, 3), new Object[]{"SSS", " S ", " B ", 'S', "ingotSteel", 'B', Item.blazeRod}));
 		GameRegistry.addRecipe(new ItemStack(HMItem.rivets, 8, 4), new Object[]{"GGG", " G ", " B ", 'G', Item.ingotGold, 'B', Item.blazePowder});
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(HMItem.rivets, 8, 5), new Object[]{"EEE", " E ", " B ", 'E', "ingotEndium", 'B', Item.blazeRod}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(HMItem.rivets, 8, 6), new Object[]{"CCC", " C ", " B ", 'C', "ingotCobalt", 'B', Item.blazeRod}));
 		
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(HMBlock.metalBlock, 1, 0), new Object[]{"EEE", "EEE", "EEE", 'E', "ingotEndium"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(HMBlock.metalBlock, 1, 1), new Object[]{"CCC", "CCC", "CCC", 'C', "ingotCobalt"}));
 		
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(HMItem.plating, 1, 0), new Object[]{"MM", "MM", 'M', "ingotEndium"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(HMItem.plating, 1, 1), new Object[]{"MM", "MM", 'M', "ingotCobalt"}));
 		
 		GameRegistry.addShapelessRecipe(new ItemStack(HMItem.ingots, 9, 0), new ItemStack(HMBlock.metalBlock, 1, 0));
 		GameRegistry.addShapelessRecipe(new ItemStack(HMItem.ingots, 9, 1), new ItemStack(HMBlock.metalBlock, 1, 1));
 		
 		GameRegistry.addShapelessRecipe(new ItemStack(Item.fireballCharge, 3), new Object[]{Item.blazePowder, Item.gunpowder, new ItemStack(HMItem.dustRaw, 1, 0)});
 		GameRegistry.addShapelessRecipe(new ItemStack(HMItem.fishFood, 4), new Object[]{Item.bread, Item.rottenFlesh});
 		
 		FurnaceRecipes.smelting().addSmelting(HMItem.dustRefined.itemID, 2, new ItemStack(Block.thinGlass), 0.0F);
 		FurnaceRecipes.smelting().addSmelting(HMItem.dustRefined.itemID, 3, new ItemStack(Item.ingotIron), 0.0F);
 		FurnaceRecipes.smelting().addSmelting(HMItem.dustRefined.itemID, 4, new ItemStack(Item.ingotGold), 0.0F);
 		FurnaceRecipes.smelting().addSmelting(HMItem.dustRaw.itemID, 8, new ItemStack(Block.obsidian), 0.0F);
 		FurnaceRecipes.smelting().addSmelting(HMItem.plating.itemID, 0, new ItemStack(HMItem.ingots, 4, 0), 0.0F);
 		FurnaceRecipes.smelting().addSmelting(HMItem.plating.itemID, 1, new ItemStack(HMItem.ingots, 4, 1), 0.0F);
 		
 		if (!OreDictionary.getOres("ingotCopper").isEmpty())
 		{
 			FurnaceRecipes.smelting().addSmelting(HMItem.dustRefined.itemID, 5, OreDictionary.getOres("ingotCopper").get(0), 0);
 			
 		}
 		
 		if (!OreDictionary.getOres("ingotTin").isEmpty())
 		{
 			FurnaceRecipes.smelting().addSmelting(HMItem.dustRefined.itemID, 6, OreDictionary.getOres("ingotTin").get(0), 0);
 			
 		}
 		
 	}
 	
 	public void loadProcessingRecipes()
 	{
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Item.diamond), new ItemStack(HMItem.dustRefined, 1, 0), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Item.enderPearl), new ItemStack(HMItem.dustRefined, 1, 1), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Block.glass), new ItemStack(HMItem.dustRefined, 4, 2), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Item.blazeRod), new ItemStack(Item.blazePowder, 2), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Item.bone), new ItemStack(Item.dyePowder, 4, 15), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Block.stone), new ItemStack(Block.gravel), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Block.cobblestone), new ItemStack(Block.sand), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Block.gravel), new ItemStack(Item.flint, 2), HMEnumProcessing.CRUSHING);
 		
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Item.eyeOfEnder), new ItemStack(HMItem.dustRefined, 1, 1), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Block.pistonBase), new ItemStack(HMItem.dustRefined, 1, 3), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Block.pistonStickyBase), new ItemStack(HMItem.dustRefined, 1, 3), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Block.dispenser), new ItemStack(Item.bow), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Block.stoneOvenIdle), new ItemStack(Block.cobblestone, 8), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Block.thinGlass), new ItemStack(HMItem.dustRefined, 1, 2), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Block.glowStone), new ItemStack(Item.lightStoneDust, 4), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Block.redstoneLampIdle), new ItemStack(Item.lightStoneDust, 4), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Item.brewingStand), new ItemStack(Item.blazeRod, 1), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Block.sandStone), new ItemStack(Block.sand, 4), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Block.obsidian), new ItemStack(HMItem.dustRaw, 1, 5), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Item.glassBottle), new ItemStack(HMItem.dustRefined, 4, 2), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Block.thinGlass), new ItemStack(HMItem.dustRefined, 1, 2), HMEnumProcessing.CRUSHING);
 		
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Item.minecartEmpty), new ItemStack(HMItem.dustRefined, 5, 3), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Item.minecartPowered), new ItemStack(HMItem.dustRefined, 5, 3), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Item.minecartCrate), new ItemStack(HMItem.dustRefined, 5, 3), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Item.doorSteel), new ItemStack(HMItem.dustRefined, 6, 3), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Item.bucketEmpty), new ItemStack(HMItem.dustRefined, 3, 3), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Item.compass), new ItemStack(HMItem.dustRefined, 4, 3), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Item.pocketSundial), new ItemStack(HMItem.dustRefined, 4, 4), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Item.cauldron), new ItemStack(HMItem.dustRefined, 7, 3), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Block.plantRed), new ItemStack(Item.dyePowder, 2, 1), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Block.plantYellow), new ItemStack(Item.dyePowder, 2, 11), HMEnumProcessing.CRUSHING);
 		
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Item.emerald), new ItemStack(HMItem.dustRefined, 1, 7), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Item.coal, 1, 0), new ItemStack(HMItem.dustRaw, 1, 0), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Item.coal, 1, 1), new ItemStack(HMItem.dustRaw, 1, 0), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Block.stoneBrick, 1, 0), new ItemStack(Block.stoneBrick, 1), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Block.stoneBrick, 1, 1), new ItemStack(Block.cobblestone), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Block.stoneBrick, 1, 2), new ItemStack(Block.cobblestoneMossy), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Block.stoneBrick, 1, 3), new ItemStack(Block.cobblestone), HMEnumProcessing.CRUSHING);
 		
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Item.helmetSteel), new ItemStack(HMItem.dustRefined, 5, 3), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Item.plateSteel), new ItemStack(HMItem.dustRefined, 8, 3), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Item.legsSteel), new ItemStack(HMItem.dustRefined, 7, 3), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Item.bootsSteel), new ItemStack(HMItem.dustRefined, 4, 3), HMEnumProcessing.CRUSHING);
 		
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Item.swordSteel), new ItemStack(HMItem.dustRefined, 2, 3), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Item.pickaxeSteel), new ItemStack(HMItem.dustRefined, 3, 3), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Item.shovelSteel), new ItemStack(HMItem.dustRefined, 1, 3), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Item.axeSteel), new ItemStack(HMItem.dustRefined, 3, 3), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Item.hoeSteel), new ItemStack(HMItem.dustRefined, 2, 3), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Item.netherStar), new ItemStack(HMItem.dustRefined, 3, 8), HMEnumProcessing.CRUSHING);
 		
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Block.oreIron), new ItemStack(HMItem.dustRaw, 2, 1), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Block.oreGold), new ItemStack(HMItem.dustRaw, 2, 2), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Item.ingotIron), new ItemStack(HMItem.dustRefined, 1, 3), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(Item.ingotGold), new ItemStack(HMItem.dustRefined, 1, 4), HMEnumProcessing.CRUSHING);
 		
 		PROCESS_RECIPES.addFoDProcessingRecipe("oreCopper", new ItemStack(HMItem.dustRaw, 2, 3), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addFoDProcessingRecipe("oreTin", new ItemStack(HMItem.dustRaw, 2, 4), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addFoDProcessingRecipe("oreEndium", new ItemStack(HMItem.dustRaw, 2, 6), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addFoDProcessingRecipe("oreCobalt", new ItemStack(HMItem.dustRaw, 2, 7), HMEnumProcessing.CRUSHING);
 		
 		PROCESS_RECIPES.addFoDProcessingRecipe("ingotCopper", new ItemStack(HMItem.dustRefined, 1, 5), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addFoDProcessingRecipe("ingotTin", new ItemStack(HMItem.dustRefined, 1, 6), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addFoDProcessingRecipe("ingotEndium", new ItemStack(HMItem.dustRefined, 1, 9), HMEnumProcessing.CRUSHING);
 		PROCESS_RECIPES.addFoDProcessingRecipe("ingotCobalt", new ItemStack(HMItem.dustRefined, 1, 10), HMEnumProcessing.CRUSHING);
 		
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(HMItem.dustRaw, 1, 1), new ItemStack(HMItem.dustRefined, 1, 3), HMEnumProcessing.WASHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(HMItem.dustRaw, 1, 2), new ItemStack(HMItem.dustRefined, 1, 4), HMEnumProcessing.WASHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(HMItem.dustRaw, 1, 3), new ItemStack(HMItem.dustRefined, 1, 5), HMEnumProcessing.WASHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(HMItem.dustRaw, 1, 4), new ItemStack(HMItem.dustRefined, 1, 6), HMEnumProcessing.WASHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(HMItem.dustRaw, 1, 6), new ItemStack(HMItem.dustRefined, 1, 9), HMEnumProcessing.WASHING);
 		PROCESS_RECIPES.addProcessingRecipe(new ItemStack(HMItem.dustRaw, 1, 7), new ItemStack(HMItem.dustRefined, 1, 10), HMEnumProcessing.WASHING);
 		
 		PROCESS_RECIPES.addForgeRecipe(new ShapelessOreRecipe(new ItemStack(HMItem.ingots, 1, 0), new Object[]{"dustEndium", "dustStar"}));
 		
 	}
 	
 	public class HMUpdateHandler extends UpdateManagerMod
 	{
 		public HMUpdateHandler(Mod m)
 		{
 			super(m);
 		}
 		
 		@Override
 		public String getModURL()
 		{
 			return "http://bit.ly/TFrC6z";
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
 			return "https://dl.dropbox.com/u/100525141/HawksMachineryBetav100Changelog.txt"; //"https://dl.dropbox.com/u/100525141/HawksMachinery" + VERSION.replace(".", "").replace(" ", "") + "Changelog.txt";
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
 			return MANAGER.enableAutoDL;
 		}
 		
 		@Override
 		public CheckingMethod getCheckingMethod()
 		{
 			return CheckingMethod.LEXICOGRAPHICAL;
 		}
 		
 		@Override
 		public boolean disableChecks()
 		{
 			return UpdateManager.isDebug ? true : !MANAGER.enableUpdateChecking;
 		}
 		
 		@Override
 		public boolean srcOnly()
 		{
 			return false;
 		}
 		
 		@Override
 		public ModReleaseType getReleaseType()
 		{
 			return ModReleaseType.BETA;
 		}
 		
 		@Override
 		public ItemStack getIconStack()
 		{
 			return new ItemStack(HMBlock.crusher);
 		}
 		
 	}
 	
 }
