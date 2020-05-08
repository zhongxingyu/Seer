 package de.blogspot.debukkitsblog;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.EnumToolMaterial;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.EnumHelper;
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
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import de.blogspot.debukkitsblog.proxies.BasisCommonProxy;
 
 @Mod(modid = "basis", name = "Basis Mod", version = "1.0.0")
 @NetworkMod(clientSideRequired = true, serverSideRequired = false)
 public class Basis {
 
 	/*
 	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 	 * Variables
 	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 	 */
 
 	public static Item testItem;
 	/*
 	 * see http://minecraft.gamepedia.com/Data_values for valid data values for
 	 * blocks and items
 	 */
 	public int testItemID = 15000;
 
 	public static ItemBasis testItem2;
 	public int testItem2ID = 15001;
 
 	public static ItemFoodBasis aeCake;
 	public int aeCakeID = 15002;
 	public String aeCakeName = "phaenovum cake";
 
	public static ItemPickaxeDirt dirtyPickAxe;
	public int dirtyPickAxeID = 15003;
	public String dirtyPickAxeName = "dirty Pickaxe";
	private static EnumToolMaterial dirtyPickAxeMaterial = EnumHelper
 			.addToolMaterial(Block.dirt.getUnlocalizedName(), 4, 2500, 14F, 5,
 					22);
 
 	public static Block testBlock;
 	public int testBlockID = 180;
 	public boolean dirtBlockSmelt = true;
 
 	@Instance("tutorial")
 	public static Basis instance;
 
 	@SidedProxy(clientSide = "de.blogspot.debukkitsblog.proxies.BasisClientProxy", serverSide = "de.blogspot.debukkitsblog.proxies.BasisCommonProxy")
 	public static BasisCommonProxy proxy;
 
 	@PreInit
 	public void preInit(FMLPreInitializationEvent event) {
 		Configuration config = new Configuration(
 				event.getSuggestedConfigurationFile());
 		config.load();
 
 		testItemID = config.get(Configuration.CATEGORY_ITEM, "testItemID",
 				15000).getInt();
 		testItem2ID = config.get(Configuration.CATEGORY_ITEM, "testItem2ID",
 				15001).getInt();
 		aeCakeID = config.get(Configuration.CATEGORY_ITEM, "aeCakeID", 15002)
 				.getInt();
 		testBlockID = config.get(Configuration.CATEGORY_BLOCK, "testBlockID",
 				180).getInt();
 
 		aeCakeName = config.get(Configuration.CATEGORY_GENERAL, "aeCakeName",
 				"phaenovum cake").getString();
 		dirtBlockSmelt = config.get("Recipes", "dirtBlockSmelt", true)
 				.getBoolean(true);
 
 		config.save();
 	}
 
 	@Init
 	public void init(FMLInitializationEvent event) {
 
 		testBlock = new BlockBasis(testBlockID, Material.rock);
 		testBlock.setCreativeTab(CreativeTabs.tabMaterials);
 		testBlock.setLightValue(1.0F);
 		testBlock.setStepSound(Block.soundStoneFootstep);
 		testBlock.setUnlocalizedName("testBlock");
 
 		testItem = new ItemBasis(testItemID);
 		testItem.setUnlocalizedName("testItem");
 		testItem.setCreativeTab(CreativeTabs.tabDecorations);
 
 		testItem2 = new ItemBasis(testItem2ID);
 		testItem2.setUnlocalizedName("testItem2");
 		testItem2.setCreativeTab(CreativeTabs.tabDecorations);
 		testItem2.setIconFile("Basis:me");
 
 		aeCake = new ItemFoodBasis(aeCakeID, 10, 0.6F, false);
 		aeCake.setUnlocalizedName("aeCake");
 		aeCake.setCreativeTab(CreativeTabs.tabFood);
 		aeCake.setMaxStackSize(16);
 		aeCake.setIconFile("Basis:ae_cake");
 
		dirtyPickAxe = new ItemPickaxeDirt(dirtyPickAxeID, dirtyPickAxeMaterial);
		dirtyPickAxe.setUnlocalizedName("diryPickAxe");
		dirtyPickAxe.setCreativeTab(CreativeTabs.tabTools);
		dirtyPickAxe.setMaxStackSize(1);
		dirtyPickAxe.setIconFile("Basis:dirty_pickaxe");
 
 		registerBlocks();
 		registerItems();
 		updateLanguage();
 		initCraftingRecipes();
 		initSmeltingRecipes();
 
 		GameRegistry.registerWorldGenerator(new Generator());
 	}
 
 	@PostInit
 	public static void postInit(FMLPostInitializationEvent event) {
 
 	}
 
 	/*
 	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 	 * private methods
 	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 	 */
 
 	/**
 	 * add crafting recipes
 	 */
 	private void initCraftingRecipes() {
 		ItemStack dirtStack = new ItemStack(Block.dirt);
 
 		GameRegistry.addRecipe(new ItemStack(Item.feather),
 		// 3x3 crafting table
 				"#XU", // upper row
 				"#XX", // middle row
 				"###", // lower row
 				// definition of single characters (undefined chars are empty)
 				'#', Block.dirt, 'U', Item.ingotGold);
 
 		GameRegistry.addShapelessRecipe(new ItemStack(Item.diamond, 8),
 				dirtStack, dirtStack, new ItemStack(Block.sand));
 		/* For testing cake in survival mode */
 		GameRegistry.addShapelessRecipe(new ItemStack(aeCake, 1), dirtStack);
		GameRegistry.addRecipe(new ItemStack(dirtyPickAxe),
 		// 3x3 crafting table
 				"###", // upper row
 				"XIX", // middle row
 				"XIX", // lower row
 				// definition of single characters (undefined chars are empty)
 				'#', Block.dirt, 'I', Item.stick);
 	}
 
 	/**
 	 * add melting recipes
 	 */
 	private void initSmeltingRecipes() {
 		GameRegistry.addSmelting(testBlock.blockID, new ItemStack(testItem),
 				1.0F);
 		if (dirtBlockSmelt) {
 			GameRegistry.addSmelting(Block.dirt.blockID, new ItemStack(
 				Item.diamond, 64), 1.0F);
 		}
 	}
 
 	/**
 	 * add blocks
 	 */
 	private void registerBlocks() {
 		GameRegistry.registerBlock(testBlock, "phaenovum Rock");
 	}
 
 	/**
 	 * add items
 	 */
 	private void registerItems() {
 		GameRegistry.registerItem(testItem, "phaenovum");
 		GameRegistry.registerItem(testItem2, "lars");
 		GameRegistry.registerItem(aeCake, "aeCake");
		GameRegistry.registerItem(dirtyPickAxe, "diryPickAxe");
 	}
 
 	/**
 	 * add names for blocks and items
 	 */
 	private void updateLanguage() {
 		LanguageRegistry.addName(testBlock, "phaenovum Rock");
 		LanguageRegistry.addName(testItem, "phaenovum");
 		LanguageRegistry.addName(testItem2, "Lars");
 		LanguageRegistry.addName(aeCake, "phaenovum Cake");
 		LanguageRegistry.addName(aeCake, aeCakeName);
		LanguageRegistry.addName(dirtyPickAxe, "dirty Pickaxe");
 	}
 }
