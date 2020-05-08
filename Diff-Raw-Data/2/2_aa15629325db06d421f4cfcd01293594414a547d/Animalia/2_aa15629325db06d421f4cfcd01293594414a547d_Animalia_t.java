 package animalia.common;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.EnumToolMaterial;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemAxe;
 import net.minecraft.item.ItemBlock;
 import net.minecraft.item.ItemHoe;
 import net.minecraft.item.ItemPickaxe;
 import net.minecraft.item.ItemSpade;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.ItemSword;
 import net.minecraft.item.ItemFood;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.event.ForgeSubscribe;
 import animalia.client.ClientTickHandler;
 import animalia.common.block.Block4DCrystalOre;
 import animalia.common.block.BlockFossil;
 import animalia.common.block.BlockLatePaleozoicLeaves;
 import animalia.common.block.BlockLatePaleozoicLog;
 import animalia.common.block.BlockLatePaleozoicPlanks;
 import animalia.common.block.BlockLatePaleozoicSapling;
 import animalia.common.block.BlockPermafrost;
 import animalia.common.block.ItemBlockMetadata;
 import animalia.common.config.ConfigHandler;
 import animalia.common.config.ConfigSettings;
 import animalia.common.event.EventManager;
 import animalia.common.item.ItemArtificialEgg;
 import animalia.common.item.ItemChisel;
 import animalia.common.item.ItemCrystal4D;
 import animalia.common.item.ItemFossil;
 import animalia.common.item.ItemMammothHair;
 import animalia.common.item.ItemMammothTrunk;
 import animalia.common.item.ItemOlivineArmor;
 import animalia.common.machine.extractor.BlockExtractor;
 import animalia.common.network.PacketHandler;
 import animalia.common.ref.ArmorEnums;
 import animalia.common.ref.Reference;
 import animalia.common.ref.ToolEnums;
 import cpw.mods.fml.common.ICraftingHandler;
 import cpw.mods.fml.common.IFuelHandler;
 import cpw.mods.fml.common.IPickupNotifier;
 import cpw.mods.fml.common.IPlayerTracker;
 import cpw.mods.fml.common.IWorldGenerator;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import cpw.mods.fml.common.registry.TickRegistry;
 import cpw.mods.fml.relauncher.Side;
 
 @Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION)
 @NetworkMod(clientSideRequired = true, serverSideRequired = false, channels = { Reference.MAIN_CHANNEL }, packetHandler = PacketHandler.class)
 public class Animalia
 {
 	// Retrieve the Constructed Mod Instance from Forge
 	@Instance(Reference.MOD_ID)
 	public static Animalia instance;
 	
 	private final Thread updateThread = new Thread(new UpdateThread());
 
 	// Retrieve Correct Proxy based on which Side this code is being run on.
 	@SidedProxy(clientSide = "animalia.client.ClientProxy", serverSide = "animalia.common.CommonProxy")
 	public static CommonProxy proxy;
 
 	public static final int UNCHECKED = -2, ERROR = -1, FALSE = 0, TRUE = 1;
 	public static int isCurrentVersion = UNCHECKED;
 	public static String latestModVersion;
 
 	public static CreativeTabAnimalia tabBlock;
 	public static CreativeTabAnimalia tabMaterial;
 	public static CreativeTabAnimalia tabTools;
 	public static CreativeTabAnimalia tabWeapons;
 	public static CreativeTabAnimalia tabArmors;
 	public static CreativeTabAnimalia tabMachine;
 	public static CreativeTabAnimalia tabDeco;
 
 	// EP is an Abbreviation for Early Paleozoic
 	public static Block fossilBlock;
 	
 	//Ore Blocks
 	public static Block olivineBlock;
 
 	// LP is an Abbreviation for Late Paleozoic
 	public static Block leavesLP;
 	public static Block logLP;
 	public static Block saplingLP;
 	public static Block planksLP;
 
 	// Crystal Ore
 	public static Block crystal4DOre;
 	public static Block crystal4DOreGlowing;
 	
 	// Permafrost
 	public static Block permafrost;
 
 	// Item Crystal
 	public static Item crystal4D;
 
 	// Item Chisel
 	public static Item chiselItem;
 	
 	// Item Fossil
 	public static Item fossilItem;
 	
 	// Item Syringes
 	public static Item syringeItem;
 	
 	// Item Artificial Egg
 	public static Item artificialEgg;
 	
 	// Mammoth Items
 	public static Item mammothHair;
 	public static Item mammothTrunkFrozen;
 	public static Item mammothTrunkCooked;
 	
 	/*
 	 * Olivine Tools
 	 */
 	public static Item olivineGem;
 	public static Item olivinePickaxe;
 	public static Item olivineAxe;
 	public static Item olivineShovel;
 	public static Item olivineHoe;
 	public static Item olivineSword;
 
 	/*
 	 * Olivine Armors
 	 */
 	public static Item olivineHelmet;
 	public static Item olivineChestplate;
 	public static Item olivineLeggings;
 	public static Item olivineBoots;
 
 	/*
 	 * Machines
 	 */
 	public static Block extractorOff;
 	public static Block extractorOn;
 
 	@EventHandler
 	public void loadPre(FMLPreInitializationEvent event)
 	{
 		ConfigHandler.initConfig(new File(event.getModConfigurationDirectory() + "/Animalia.cfg"));
 		updateThread.start();
 	}
 
 	@EventHandler
 	public void load(FMLInitializationEvent event)
 	{
 		initObjects();
 		initCreativeTabs();
 		finishCreativeTabInit();
 		registerBlocks();
 		registerItems();
 		registerLocalizations();
 		registerRecipes();
 		registerHarvestLevels();
 		Animalia.proxy.registerTileEntities();
 		Animalia.proxy.registerTextureInfo();
 		Animalia.proxy.registerRenders();
 		NetworkRegistry.instance().registerGuiHandler(instance, proxy);
 		registerEventManager(new EventManager());
 		registerTickHandlers();
 
 		isCurrentVersion = isNewestRecommendedBuild();
 	}
 
 	/** Converts a String Array to int array, preserving array size. Fails fast on number format
 	 * exception to handle appropriately.
 	 * 
 	 * @param buildStrings String array with Integer parse-able values.
 	 * @return int array of parsed string values.
 	 * @throws NumberFormatException */
 	private static int[] convertVersionNumber(final String[] buildStrings) throws NumberFormatException
 	{
 	    if(buildStrings == null)
 	        return null;
 	    
 		int[] buildInt = new int[buildStrings.length];
 		for (int i = 0; i < buildInt.length; i++)
 		{
 			buildInt[i] = Integer.parseInt(buildStrings[i]);
 		}
 		return buildInt;
 	}
 
 	@EventHandler
 	public void loadPost(FMLPostInitializationEvent event)
 	{
 
 	}
 
 	private void initObjects()
 	{
 		leavesLP = new BlockLatePaleozoicLeaves(ConfigSettings.leavesLPProp.getInt()).setHardness(0.2F).setLightOpacity(1).setStepSound(Block.soundGrassFootstep).setUnlocalizedName("animalia:leaves_late_paleo");
 		logLP = new BlockLatePaleozoicLog(ConfigSettings.logsLPProp.getInt()).setHardness(2.0F).setStepSound(Block.soundWoodFootstep).setUnlocalizedName("animalia:logs_late_paleo");
 		saplingLP = new BlockLatePaleozoicSapling(ConfigSettings.saplingLPProp.getInt()).setHardness(0.0F).setStepSound(Block.soundGrassFootstep).setUnlocalizedName("animalia:sapling_late_paleo");
 		planksLP = new BlockLatePaleozoicPlanks(ConfigSettings.planksLPProp.getInt()).setHardness(2.0F).setStepSound(Block.soundWoodFootstep).setUnlocalizedName("animalia:planks_late_paleo");
 		
 		fossilBlock = new BlockFossil(ConfigSettings.fossilEPProp.getInt(), Material.rock).setHardness(1F).setResistance(100).setUnlocalizedName("animalia:fossil_early_paleo");
 
 		crystal4DOre = new Block4DCrystalOre(ConfigSettings.crystalOreProp.getInt()).setHardness(1F).setResistance(100).setUnlocalizedName("animalia:crystal_ore").func_111022_d("animalia:crystal_ore");
 		crystal4DOreGlowing = new Block4DCrystalOre(ConfigSettings.crystalOreProp.getInt() + 1).setHardness(1F).setResistance(100).setUnlocalizedName("animalia:crystal_ore").setLightValue(1.0F).func_111022_d("animalia:crystal_ore");
 
 		extractorOff = new BlockExtractor(ConfigSettings.extractorProp.getInt(), false).setHardness(1F).setResistance(100);
 		extractorOn = new BlockExtractor(ConfigSettings.extractorProp.getInt() + 1, true).setHardness(1F).setResistance(100).setLightValue(1.0F);
 		
 		olivineBlock = new Block(ConfigSettings.olivineBlock.getInt() + 1, Material.iron).setHardness(5.0F).setResistance(10.0F).setUnlocalizedName("animalia:olivineBlock").setStepSound(Block.soundMetalFootstep).func_111022_d("animalia:olivineBlock");
 
 		permafrost = new BlockPermafrost(4054).setHardness(0.4F).setResistance(1.0F).setUnlocalizedName("animalia:permafrost").func_111022_d("animalia:permafrost");
 		
 		
 		
 		// Items
 		crystal4D = new ItemCrystal4D(ConfigSettings.crystalGemProp.getInt()).setUnlocalizedName("animalia:crystal").func_111206_d("animalia:crystal");
 		olivineGem = new Item(5006).setUnlocalizedName("animalia:olivine_gem").func_111206_d("animalia:olivine_gem");
 
 		olivinePickaxe = new ItemPickaxe(5001, ToolEnums.OLIVINE).setUnlocalizedName("animalia:olivine_pickaxe").func_111206_d("animalia:tools/olivine_pickaxe");
 		olivineAxe = new ItemAxe(5002, ToolEnums.OLIVINE).setUnlocalizedName("animalia:olivine_axe").func_111206_d("animalia:tools/olivine_axe");
 		olivineShovel = new ItemSpade(5003, ToolEnums.OLIVINE).setUnlocalizedName("animalia:olivine_spade").func_111206_d("animalia:tools/olivine_spade");
 		olivineHoe = new ItemHoe(5004, ToolEnums.OLIVINE).setUnlocalizedName("animalia:olivine_hoe").func_111206_d("animalia:tools/olivine_hoe");
 		olivineSword = new ItemSword(5005, ToolEnums.OLIVINE).setUnlocalizedName("animalia:olivine_sword").func_111206_d("animalia:weapons/olivine_sword");
 
 		olivineHelmet = new ItemOlivineArmor(6000, ArmorEnums.OLIVINEARMOR, Constants.OLIVINE_ARMOR_RENDER, 0).setUnlocalizedName("animalia:olivine_helmet");
 		olivineChestplate = new ItemOlivineArmor(6001, ArmorEnums.OLIVINEARMOR, Constants.OLIVINE_ARMOR_RENDER, 1).setUnlocalizedName("animalia:olivine_chestplate");
 		olivineLeggings = new ItemOlivineArmor(6002, ArmorEnums.OLIVINEARMOR, Constants.OLIVINE_ARMOR_RENDER, 2).setUnlocalizedName("animalia:olivine_leggings");
 		olivineBoots = new ItemOlivineArmor(6003, ArmorEnums.OLIVINEARMOR, Constants.OLIVINE_ARMOR_RENDER, 3).setUnlocalizedName("animalia:olivine_boots");
 
 		chiselItem = new ItemChisel(6050, EnumToolMaterial.IRON).setUnlocalizedName("animalia:chisel").func_111206_d("animalia:tools/chisel");
 		fossilItem = new ItemFossil(6051).setUnlocalizedName("animalia:fossil_item").func_111206_d("animalia:fossil_item");
 		
 		mammothHair = new ItemMammothHair(6052).setUnlocalizedName("animalia:mammothHair").func_111206_d("animalia:mammothHair");
 		mammothTrunkFrozen = new ItemMammothTrunk(6053, 4, false).setUnlocalizedName("animalia:mammothTrunkFrozen").func_111206_d("animalia:mammothTrunkFrozen");
 		mammothTrunkCooked = (new ItemFood(109, 2, 0.3F, true)).setUnlocalizedName("animalia:mammothTrunkCooked").func_111206_d("animalia:mammothTrunkCooked");
 		
 		artificialEgg = new ItemArtificialEgg(6054, Block.dirt).setUnlocalizedName("animalia:artficialEgg").func_111206_d("animalia:artificialEgg");
 	}
 
 	private void initCreativeTabs()
 	{
 		tabBlock = new CreativeTabAnimalia(CreativeTabs.getNextID(), "animaliaBlocks").setIcon(fossilBlock.blockID);
 		tabMaterial = new CreativeTabAnimalia(CreativeTabs.getNextID(), "animaliaMaterials").setIcon(crystal4D.itemID);
 		tabTools = new CreativeTabAnimalia(CreativeTabs.getNextID(), "animaliaTools").setIcon(olivineAxe.itemID);
 		tabWeapons = new CreativeTabAnimalia(CreativeTabs.getNextID(), "animaliaWeapons").setIcon(olivineSword.itemID);
 		tabArmors = new CreativeTabAnimalia(CreativeTabs.getNextID(), "animaliaArmors").setIcon(olivineChestplate.itemID);
 		tabDeco = new CreativeTabAnimalia(CreativeTabs.getNextID(), "animaliaDecorations").setIcon(saplingLP.blockID);
 		tabMachine = new CreativeTabAnimalia(CreativeTabs.getNextID(), "animaliaMachines").setIcon(extractorOff.blockID);
 	}
 
 	private void finishCreativeTabInit()
 	{
 	    fossilBlock.setCreativeTab(tabBlock);
 	    
 		leavesLP.setCreativeTab(tabBlock);
 		logLP.setCreativeTab(tabBlock);
 		saplingLP.setCreativeTab(tabDeco);
 		planksLP.setCreativeTab(tabBlock);
 		
 		olivineBlock.setCreativeTab(tabBlock);
 
 		crystal4DOre.setCreativeTab(tabBlock);
 		
 		permafrost.setCreativeTab(tabBlock);
 
 		crystal4D.setCreativeTab(tabMaterial);
 		olivineGem.setCreativeTab(tabMaterial);
 
 		olivinePickaxe.setCreativeTab(tabTools);
 		olivineAxe.setCreativeTab(tabTools);
 		olivineShovel.setCreativeTab(tabTools);
 		olivineHoe.setCreativeTab(tabTools);
 
 		olivineSword.setCreativeTab(tabWeapons);
 
 		olivineHelmet.setCreativeTab(tabArmors);
 		olivineChestplate.setCreativeTab(tabArmors);
 		olivineLeggings.setCreativeTab(tabArmors);
 		olivineBoots.setCreativeTab(tabArmors);
 
 		extractorOff.setCreativeTab(tabMachine);
 		extractorOn.setCreativeTab(null);
 		
 		chiselItem.setCreativeTab(tabTools);
 		fossilItem.setCreativeTab(tabMaterial);
 		
 		mammothHair.setCreativeTab(tabMaterial);
 		mammothTrunkFrozen.setCreativeTab(tabMaterial);
 		mammothTrunkCooked.setCreativeTab(tabMaterial);
 	
 		artificialEgg.setCreativeTab(tabMaterial);
 	}
 
 	private void registerBlocks()
 	{
 		//Fossil Blocks
 	    registerMetadataBlock(fossilBlock, "FossilBlock");
 	    
 		//tree blocks
 		registerMetadataBlock(logLP, "LogLP");
 		registerMetadataBlock(leavesLP, "LeavesLP");
 		registerMetadataBlock(saplingLP, "SaplingLP");
 		registerMetadataBlock(planksLP, "PlanksLP");
 
 		// Crystal Ore Blocks
 		registerBlock(crystal4DOre, "CrystalOre");
 		registerBlock(crystal4DOreGlowing, "CrystalOreGlowing");
 		
 		//Gem Blocks
 		registerBlock(olivineBlock, "OlivineBlock");
 		
 	
 
 		registerBlock(extractorOff, "ExtractorOff");
 		registerBlock(extractorOn, "ExtractorOn");
 		
 		//Permafrost
 		registerBlock(permafrost, "PermafrostBlock");
 	}
 
 	private static void registerBlock(Block block, String name)
 	{
 		GameRegistry.registerBlock(block, ItemBlock.class, name, Reference.MOD_ID);
 	}
 	
 	private static void registerMetadataBlock(Block block, String name)
 	{
 		GameRegistry.registerBlock(block, ItemBlockMetadata.class, name, Reference.MOD_ID);
 	}
 
 	private void registerItems()
 	{
 		// Gems
 		registerItem(crystal4D, "itemCrystal4D");
 		registerItem(olivineGem, "itemOlivineGem");
 
 		// Olivine Tools
 		registerItem(olivineAxe, "itemOlivineAxe");
 		registerItem(olivineHoe, "itemOlivineHoe");
 		registerItem(olivinePickaxe, "itemOlivinePickaxe");
 		registerItem(olivineShovel, "itemOlivineShovel");
 		registerItem(olivineSword, "itemOlivineSword");
 
 		// Olivine Armor
 		registerItem(olivineHelmet, "itemOlivineHelmet");
 		registerItem(olivineChestplate, "itemOlivineChestplate");
 		registerItem(olivineLeggings, "itemOlivineLeggings");
 		registerItem(olivineBoots, "itemOlivineBoots");
 		
 		registerItem(chiselItem, "itemChisel");
 		registerItem(fossilItem, "itemFossil");
 		
 		registerItem(mammothHair, "itemMammothHair");
 		registerItem(mammothTrunkFrozen, "itemMammothTrunkFrozen");
 		registerItem(mammothTrunkCooked, "itemMammothTrunkCooked");
 		
 		registerItem(artificialEgg, "itemArtificialEgg");
 	}
 
 	private static void registerItem(Item item, String name)
 	{
 		GameRegistry.registerItem(item, name, Reference.MOD_ID);
 	}
 	
 	private void registerLocalizations()
 	{
 		// Block Localizations
 		LanguageRegistry.addName(new ItemStack(fossilBlock, 1, 0), "Early Paleozoic Fossil");
 		LanguageRegistry.addName(new ItemStack(fossilBlock, 1, 1), "Late Paleozoic Fossil");
 		LanguageRegistry.addName(new ItemStack(fossilBlock, 1, 2), "Mesozoic Fossil");
 
 		LanguageRegistry.addName(crystal4DOre, "4D Crystal Ore");
 
 		LanguageRegistry.addName(extractorOff, "Extractor");
 		LanguageRegistry.addName(olivineBlock, "Block of Olivine");
 		
 		LanguageRegistry.addName(permafrost, "Permafrost");
 		
 		LanguageRegistry.addName(new ItemStack(logLP, 1, 0), "Sigillaria Log");
 		LanguageRegistry.addName(new ItemStack(logLP, 1, 3), "Lepidodendron Log");
 		LanguageRegistry.addName(new ItemStack(logLP, 1, 6), "Cordaites Log");
 		
 		LanguageRegistry.addName(new ItemStack(leavesLP, 1, 0), "Sigillaria Leaves");
 		LanguageRegistry.addName(new ItemStack(leavesLP, 1, 1), "Lepidodendron Leaves");
 		LanguageRegistry.addName(new ItemStack(leavesLP, 1, 2), "Cordaites Leaves");
 		
 		LanguageRegistry.addName(new ItemStack(saplingLP, 1, 0), "Sigillaria Sapling");
 		LanguageRegistry.addName(new ItemStack(saplingLP, 1, 3), "Lepidodendron Sapling");
 		LanguageRegistry.addName(new ItemStack(saplingLP, 1, 6), "Cordaites Sapling");
 		
 		LanguageRegistry.addName(new ItemStack(planksLP, 1, 0), "Sigillaria Planks");
 		LanguageRegistry.addName(new ItemStack(planksLP, 1, 1), "Lepidodendron Planks");
 		LanguageRegistry.addName(new ItemStack(planksLP, 1, 2), "Cordaites Planks");
 
 		// Item Localizations
         LanguageRegistry.addName(new ItemStack(fossilItem, 1, 0), "Early Paleozoic Fossil");
         LanguageRegistry.addName(new ItemStack(fossilItem, 1, 1), "Late Paleozoic Fossil");
         LanguageRegistry.addName(new ItemStack(fossilItem, 1, 2), "Mesozoic Fossil");   
         
 		LanguageRegistry.addName(crystal4D, "4D Crystal");
 		
 		LanguageRegistry.addName(chiselItem, "Chisel");
 		
 		LanguageRegistry.addName(mammothHair, "Mammoth Hair");
 		LanguageRegistry.addName(mammothTrunkFrozen, "Frozen Mammoth Trunk");
 		LanguageRegistry.addName(mammothTrunkCooked, "Cooked Mammoth Trunk");
 		
 		LanguageRegistry.addName(artificialEgg, "Artificial Egg");
 
 		LanguageRegistry.addName(olivineGem, "Olivine Gem");
 		LanguageRegistry.addName(olivineAxe, "Olivine Axe");
 		LanguageRegistry.addName(olivineHoe, "Olivine Hoe");
 		LanguageRegistry.addName(olivinePickaxe, "Olivine Pickaxe");
 		LanguageRegistry.addName(olivineShovel, "Olivine Shovel");
 		LanguageRegistry.addName(olivineSword, "Olivine Sword");
 
 		LanguageRegistry.addName(olivineHelmet, "Olivine Helmet");
 		LanguageRegistry.addName(olivineChestplate, "Olivine Chestplate");
 		LanguageRegistry.addName(olivineLeggings, "Olivine Leggings");
 		LanguageRegistry.addName(olivineBoots, "Olivine Boots");
 
 		// General Localizations
 		LanguageRegistry.instance().addStringLocalization("itemGroup.animaliaBlocks", Language.ENGLISHUS.getLangCode(), "Animalia Blocks");
 		LanguageRegistry.instance().addStringLocalization("itemGroup.animaliaMaterials", Language.ENGLISHUS.getLangCode(), "Animalia Materials");
 		LanguageRegistry.instance().addStringLocalization("itemGroup.animaliaTools", Language.ENGLISHUS.getLangCode(), "Animalia Tools");
 		LanguageRegistry.instance().addStringLocalization("itemGroup.animaliaWeapons", Language.ENGLISHUS.getLangCode(), "Animalia Weapons");
 		LanguageRegistry.instance().addStringLocalization("itemGroup.animaliaArmors", Language.ENGLISHUS.getLangCode(), "Animalia Armors");
 		LanguageRegistry.instance().addStringLocalization("itemGroup.animaliaMachines", Language.ENGLISHUS.getLangCode(), "Animalia Machines");
 		LanguageRegistry.instance().addStringLocalization("itemGroup.animaliaDecorations", Language.ENGLISHUS.getLangCode(), "Animalia Decorations");
 		LanguageRegistry.instance().addStringLocalization("animalia.container.extractor", Language.ENGLISHUS.getLangCode(), "Extractor");
 	}
 
 	private void registerRecipes()
 	{
 		GameRegistry.addRecipe(new ItemStack(olivineAxe), new Object[] { "XX ", "XS ", " S ", Character.valueOf('X'), olivineGem, Character.valueOf('S'), Item.stick });
 		GameRegistry.addRecipe(new ItemStack(olivineAxe), new Object[] { " XX", " SX", " S ", Character.valueOf('X'), olivineGem, Character.valueOf('S'), Item.stick });
 		GameRegistry.addRecipe(new ItemStack(olivineHoe), new Object[] { "XX", " S", " S", Character.valueOf('X'), olivineGem, Character.valueOf('S'), Item.stick });
 		GameRegistry.addRecipe(new ItemStack(olivineHoe), new Object[] { "XX", "S ", "S ", Character.valueOf('X'), olivineGem, Character.valueOf('S'), Item.stick });
 		GameRegistry.addRecipe(new ItemStack(olivinePickaxe), new Object[] { "XXX", " S ", " S ", Character.valueOf('X'), olivineGem, Character.valueOf('S'), Item.stick });
 		GameRegistry.addRecipe(new ItemStack(olivineShovel), new Object[] { "X", "S", "S", Character.valueOf('X'), olivineGem, Character.valueOf('S'), Item.stick });
 		GameRegistry.addRecipe(new ItemStack(olivineSword), new Object[] { "X", "X", "S", Character.valueOf('X'), olivineGem, Character.valueOf('S'), Item.stick });
 		GameRegistry.addRecipe(new ItemStack(olivineBlock), new Object[] { "XXX", "XXX", "XXX", Character.valueOf('X'), olivineGem });
 		GameRegistry.addRecipe(new ItemStack(chiselItem), new Object[] {"X", "S", Character.valueOf('X'), Item.ingotIron, Character.valueOf('S'), Item.stick});
 		
 		GameRegistry.addShapelessRecipe(new ItemStack (planksLP, 4, 0), new ItemStack(logLP, 1, 0));
 		GameRegistry.addShapelessRecipe(new ItemStack (planksLP, 4, 1), new ItemStack(logLP, 1, 3));
 		GameRegistry.addShapelessRecipe(new ItemStack (planksLP, 4, 2), new ItemStack(logLP, 1, 6));
 		GameRegistry.addShapelessRecipe(new ItemStack(olivineGem, 9, 0), new ItemStack(olivineBlock));
 		
		GameRegistry.addSmelting(mammothTrunkFrozen.itemID, new ItemStack(mammothTrunkCooked), 1.0F);
 	}
 
 	private void registerHarvestLevels()
 	{
 		MinecraftForge.setBlockHarvestLevel(fossilBlock, "pickaxe", 2);
 
 		MinecraftForge.setBlockHarvestLevel(crystal4DOre, "pickaxe", 3);
 		MinecraftForge.setBlockHarvestLevel(crystal4DOreGlowing, "pickaxe", 3);
 	}
 
 	private void registerTickHandlers()
 	{
 		TickRegistry.registerTickHandler(new ClientTickHandler(), Side.CLIENT);
 		// TickRegistry.registerTickHandler(new ServerTickHandler(), Side.SERVER);
 	}
 
 	private void registerEventManager(EventManager eventManager)
 	{
 		if (eventManager instanceof ICraftingHandler)
 			GameRegistry.registerCraftingHandler(eventManager);
 		if (eventManager instanceof IFuelHandler)
 			GameRegistry.registerFuelHandler(eventManager);
 		if (eventManager instanceof IWorldGenerator)
 			GameRegistry.registerWorldGenerator(eventManager);
 		if (eventManager instanceof IPlayerTracker)
 			GameRegistry.registerPlayerTracker(eventManager);
 		if (eventManager instanceof IPickupNotifier)
 			GameRegistry.registerPickupHandler(eventManager);
 		if (eventManager.getClass().isAnnotationPresent(ForgeSubscribe.class))
 			MinecraftForge.EVENT_BUS.register(eventManager);
 	}
 
 	public static int isNewestRecommendedBuild()
 	{
 		if (isCurrentVersion != UNCHECKED)
 		{
 			//Prevent checking for updates multiple times
 			return isCurrentVersion;
 		}
 		
 		//The next few lines have been changed to bypass the NullPointerException thrown by the getCurrentRecommendedBuild method returning null (even though it shouldn't)
 		String[] currBuildStrings = null;
 		String[] newBuildStrings = null;
 		try
 		{
 		    currBuildStrings = Reference.MOD_VERSION.split("\\.");
 		    newBuildStrings = instance.getCurrentRecommendedBuild().split("\\.");
 		}
 		catch(NullPointerException e)
 		{
 		    
 		}
 		//Default to error in case failure occurs.
 		int isMostRecentVer = ERROR;
 
 		try
 		{
 			isMostRecentVer = isSameVersion(convertVersionNumber(currBuildStrings), convertVersionNumber(newBuildStrings));
 		}
 		catch (NumberFormatException nil)
 		{
 		}
 
 		return isMostRecentVer;
 	}
 
 	private static int isSameVersion(int[] currInstallVer, int[] mostRecentVer)
 	{
 	    if(currInstallVer == null || mostRecentVer == null)
 	        return ERROR;
 	    
 		for (int index = 0; index < currInstallVer.length; index++)
 		{
 			if (currInstallVer[index] != mostRecentVer[index])
 			{
 				return FALSE;
 			}
 		}
 		return TRUE;
 	}
 
 	public String getCurrentRecommendedBuild()
 	{
 		//If update check isnt done, wait on it.
 		while (latestModVersion == null)
 		{
 			synchronized (Thread.currentThread())
 			{
 				try
 				{
 					wait(1);
 				}
 				catch (Exception ex)
 				{
 					//This break is to prevent hard lock on main window close.
 				}
 			}
 		}
 		return latestModVersion;
 	}
 
 	private class UpdateThread implements Runnable
 	{
 		@Override
 		public void run()
 		{
 			StringBuilder buildableString = new StringBuilder();
 			HttpURLConnection connection = null;
 			try
 			{
 				connection = (HttpURLConnection) new URL("http://dl.dropbox.com/u/38453115/Animalia_Version.txt").openConnection();
 				connection.connect();
 				if (connection.getResponseCode() == 200)
 				{
 					BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
 					while (bis.available() > 0)
 					{
 						buildableString.append(Character.valueOf((char) bis.read()));
 					}
 				}
 			}
 			catch (MalformedURLException e)
 			{
 				e.printStackTrace();
 			}
 			catch (IOException e)
 			{
 				e = new IOException(e.getLocalizedMessage() + " Unable to contact Update URL");
 				e.printStackTrace();
 			}
 			finally
 			{
 				if (connection != null)
 					connection.disconnect();
 			}
 			latestModVersion = buildableString.toString().trim();
 		}
 	}
 }
