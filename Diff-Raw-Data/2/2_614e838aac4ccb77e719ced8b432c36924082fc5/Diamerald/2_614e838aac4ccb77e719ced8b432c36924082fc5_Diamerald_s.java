 package jmm.mods.Diamerald;
 
 import ic2.api.recipe.Recipes;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.item.EnumArmorMaterial;
 import net.minecraft.item.EnumToolMaterial;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemFood;
 import net.minecraft.item.ItemBow;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.ItemSword;
 import net.minecraft.util.WeightedRandomChestContent;
 import net.minecraftforge.common.ChestGenHooks;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.EnumHelper;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.oredict.OreDictionary;
 import cpw.mods.fml.client.registry.RenderingRegistry;
 import cpw.mods.fml.common.Loader;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 @Mod(modid = "Diamerald", name = "Diamerald", version = "1.0")
 @NetworkMod(clientSideRequired = true, serverSideRequired = false)
 public class Diamerald {
 	
 	//Enum ToolMaterial//
 	
 	public static EnumToolMaterial DIAMERALD = EnumHelper.addToolMaterial("DIAMERALD", 3, 2000, 12.0f, 6.0f, 10);
 	public static EnumToolMaterial BLACKDIAMERALD = EnumHelper.addToolMaterial("BLACKDIAMERALD", 3, 2500, 16.0f, 14.0f, 10);
 	
 	//Enum ArmorMaterial//
 	
 	public static EnumArmorMaterial diamerald = EnumHelper.addArmorMaterial("diamerald", 33, new int[] { 4, 9, 7, 4 }, 10);
 	public static EnumArmorMaterial blackdiamerald = EnumHelper.addArmorMaterial("blackdiamerald", 33, new int[] { 5, 10, 8, 5 }, 10);
 	
 
 	// Blocks and Items //
 
 	public static Block Diameraldore;
 	public static Block GSTorch;
 	public static Block BlockDirtchest;
 	public static Item Diameraldgem;
 	public static Item Diameraldsword;
 	public static Item Diameraldpickaxe;
 	public static Item Diameraldaxe;
 	public static Item Diameraldshovel;
 	public static Item Diameraldhoe;
 	public static Item Diameraldhelmet;
 	public static Item Diameraldplate;
 	public static Item Diameraldlegs;
 	public static Item Diameraldboots;
 	public static ItemBow Diameraldbow;
 	public static Item Roughgem;
 	public static Item blackRoughgem;
 	public static Item blackDiameraldgem;
 	public static Item blackDiameraldsword;
 	public static Item blackDiameraldpickaxe;
 	public static Item blackDiameraldhelmet;
 	public static Item Diameralddust;
 
 	// Config intIDs//
 
 	public int DiameraldoreID;
 	public int GSTorchID;
 	public int RoughgemID;
 	public int DiameraldgemID;
 	public int DiameraldswordID;
 	public int DiameraldpickaxeID;
 	public int DiameraldaxeID;
 	public int DiameraldshovelID;
 	public int DiameraldhoeID;
 	public int DiameraldbowID;
 	public int DiameraldhelmetID;
 	public int DiameraldplateID;
 	public int DiameraldlegsID;
 	public int DiameraldbootsID;
 	public int DirtchestID;
 	public int blackRoughgemID;
 	public int blackDiameraldgemID;
 	public int blackDiameraldswordID;
 	public int blackDiameraldpickaxeID;
 	public int blackDiameraldhelmetID;
 	public int DiameralddustID;
 
	@SidedProxy(clientSide = "misskidd.mods.Diamerald.DiameraldClient", serverSide = "misskidd.mods.Diamerald.DiameraldProxy")
 	public static DiameraldProxy proxy;
 
 	@EventHandler
 	public void PreLoad(FMLPreInitializationEvent event) {
 		Configuration config = new Configuration(
 				event.getSuggestedConfigurationFile());
 
 		config.load();
 
 		DiameraldoreID = config.getBlock("Diameraldore ID",
 				Configuration.CATEGORY_BLOCK, 500).getInt();
 		GSTorchID = config.getBlock("GSTorch ID", Configuration.CATEGORY_BLOCK,
 				512).getInt();
 		RoughgemID = config.getItem("Roughgem ID", Configuration.CATEGORY_ITEM,
 				3852).getInt();
 		DiameraldgemID = config.getItem("Diameraldgem ID",
 				Configuration.CATEGORY_ITEM, 3841).getInt();
 		DiameraldswordID = config.getItem("Diameraldsword ID",
 				Configuration.CATEGORY_ITEM, 3842).getInt();
 		DiameraldpickaxeID = config.getItem("Diameraldpickaxe ID",
 				Configuration.CATEGORY_ITEM, 3843).getInt();
 		DiameraldaxeID = config.getItem("Diameraldaxe ID",
 				Configuration.CATEGORY_ITEM, 3844).getInt();
 		DiameraldshovelID = config.getItem("Diameraldshovel ID",
 				Configuration.CATEGORY_ITEM, 3845).getInt();
 		DiameraldhoeID = config.getItem("Diameraldhoe ID",
 				Configuration.CATEGORY_ITEM, 3846).getInt();
 		DiameraldbowID = config.getItem("Diameraldbow ID",
 				Configuration.CATEGORY_ITEM, 3851).getInt();
 		DiameraldhelmetID = config.getItem("Diameraldhelmet ID",
 				Configuration.CATEGORY_ITEM, 3847).getInt();
 		DiameraldplateID = config.getItem("Diameraldplate ID",
 				Configuration.CATEGORY_ITEM, 3848).getInt();
 		DiameraldlegsID = config.getItem("Diameraldlegs ID",
 				Configuration.CATEGORY_ITEM, 3849).getInt();
 		DiameraldbootsID = config.getItem("Diameraldboots ID",
 				Configuration.CATEGORY_ITEM, 3850).getInt();
 		DirtchestID = config.getBlock("Dirtchest ID",
 				Configuration.CATEGORY_BLOCK, 513).getInt();
 		blackRoughgemID = config.getItem("blackRoughgem ID",
 				Configuration.CATEGORY_ITEM, 3853).getInt();
 		blackDiameraldgemID = config.getItem("blackDiameraldgem ID",
 				Configuration.CATEGORY_ITEM, 3854).getInt();
 		blackDiameraldswordID = config.getItem("blackDiameraldsword ID",
 				Configuration.CATEGORY_ITEM, 3855).getInt();
 		blackDiameraldpickaxeID = config.getItem("blackDiameraldpickaxe ID",
 				Configuration.CATEGORY_ITEM, 3856).getInt();
 		blackDiameraldhelmetID = config.getItem("blackDiameraldhelmet ID",
 				Configuration.CATEGORY_ITEM, 3857).getInt();
 		DiameralddustID = config.getItem("Diameralddust ID",
 				Configuration.CATEGORY_ITEM, 3859).getInt();
 
 		config.save();
 
 	}
 
 	@EventHandler
 	public void load(FMLInitializationEvent event) {
 
 		proxy.registerRenderInformation();
 
 		Diameraldore = (new Diameraldore(DiameraldoreID))
 				.setUnlocalizedName("Diameraldore");
 		MinecraftForge.setBlockHarvestLevel(Diameraldore, "pickaxe", 2);
 		GSTorch = (new GSTorch(GSTorchID, Material.glass))
 				.setUnlocalizedName("GStorch").setHardness(0.0f)
 				.setLightValue(1.0f);
 		Diameraldgem = (new Diameraldgem(DiameraldgemID))
 				.setUnlocalizedName("Diameraldgem");
 		Roughgem = (new Roughgem(RoughgemID)).setUnlocalizedName("Roughgem");
 		Diameraldsword = (new Diameraldsword(DiameraldswordID,
 				DIAMERALD)).setUnlocalizedName("Diameraldsword");
 		Diameraldpickaxe = (new Diameraldpickaxe(DiameraldpickaxeID,
 				DIAMERALD)).setUnlocalizedName("Diameraldpick");
 		Diameraldaxe = (new Diameraldaxe(DiameraldaxeID,
 				DIAMERALD)).setUnlocalizedName("Diameraldaxe");
 		Diameraldshovel = (new Diameraldshovel(DiameraldshovelID,
 				DIAMERALD)).setUnlocalizedName("Diameraldshovel");
 		Diameraldhoe = (new Diameraldhoe(DiameraldhoeID,
 				DIAMERALD)).setUnlocalizedName("Diameraldhoe");
 		Diameraldbow = (ItemBow) (new Diameraldbow(DiameraldbowID,
 				DIAMERALD)).setUnlocalizedName("Dbow");
 		Diameraldhelmet = (new Diameraldhelmet(DiameraldhelmetID,
 				diamerald, 3, 0)
 				.setUnlocalizedName("Diameraldhelmet"));
 		Diameraldplate = (new Diameraldplate(DiameraldplateID,
 				diamerald, 3, 1)
 				.setUnlocalizedName("Diameraldplate"));
 		Diameraldlegs = (new Diameraldlegs(DiameraldlegsID,
 				diamerald, 3, 2)
 				.setUnlocalizedName("Diameraldlegs"));
 		Diameraldboots = (new Diameraldboots(DiameraldbootsID,
 				diamerald, 3, 3)
 				.setUnlocalizedName("Diameraldboots"));
 		BlockDirtchest = (new BlockDirtchest(DirtchestID, 0))
 				.setUnlocalizedName("Dirtchest");
 		blackDiameraldgem = (new blackDiameraldgem(blackDiameraldgemID))
 				.setUnlocalizedName("blackDiameraldgem");
 		blackRoughgem = (new blackRoughgem(blackRoughgemID))
 				.setUnlocalizedName("blackRoughgem");
 		blackDiameraldsword = (new blackDiameraldsword(blackDiameraldswordID,
 				BLACKDIAMERALD))
 				.setUnlocalizedName("blackDiameraldsword");
 		blackDiameraldpickaxe = (new blackDiameraldpickaxe(
 				blackDiameraldpickaxeID, BLACKDIAMERALD))
 				.setUnlocalizedName("blackDiameraldpick");
 		blackDiameraldhelmet = (new blackDiameraldhelmet(
 				blackDiameraldhelmetID, blackdiamerald, 3, 0)
 				.setUnlocalizedName("blackDiameraldhelmet"));
 
 		// Registering things//
 
 		OreDictionary.registerOre(DiameraldgemID, Diameraldgem);
 		GameRegistry.registerBlock(Diameraldore, "Diameraldore");
 
 		GameRegistry.registerWorldGenerator(new WorldGeneratorDiamerald());
 		LanguageRegistry.addName(Diameraldore, "Diamerald ore");
 		LanguageRegistry.addName(Diameraldgem, "Diamerald");
 		LanguageRegistry.addName(blackDiameraldgem, "Black Diamerald");
 		LanguageRegistry.addName(Roughgem, "Rough Gem");
 		LanguageRegistry.addName(blackRoughgem, "Black Rough Gem");
 		GameRegistry.addSmelting(Diamerald.Roughgem.itemID, new ItemStack(
 				Diamerald.Diameraldgem, 1), 500.0f);
 		GameRegistry.addSmelting(Diamerald.blackRoughgem.itemID, new ItemStack(
 				Diamerald.blackDiameraldgem, 1), 500.0f);
 		GameRegistry.addSmelting(Diamerald.Diameraldore.blockID, new ItemStack(
 				Diamerald.Diameraldgem, 1), 500.0f);
 		LanguageRegistry.addName(Diameraldsword, "Diamerald sword");
 		LanguageRegistry.addName(Diameraldpickaxe, "Diamerald pickaxe");
 		LanguageRegistry.addName(blackDiameraldsword, "Black Diamerald sword");
 		LanguageRegistry.addName(blackDiameraldpickaxe,
 				"Black Diamerald pickaxe");
 		LanguageRegistry.addName(Diameraldhoe, "Diamerald hoe");
 		LanguageRegistry
 				.addName(blackDiameraldhelmet, "Black Diamerald Helmet");
 		LanguageRegistry.addName(Diameraldaxe, "Diamerald axe");
 		LanguageRegistry.addName(Diameraldshovel, "Diamerald shovel");
 		LanguageRegistry.addName(Diameraldhelmet, "Diamerald Helmet");
 		LanguageRegistry.addName(Diameraldplate, "Diamerald Chestpiece");
 		LanguageRegistry.addName(Diameraldlegs, "Diamerald Legs");
 		LanguageRegistry.addName(Diameraldboots, "Diamerald Boots");
 		LanguageRegistry.addName(Diameraldbow, "Diamerald Bow");
 		GameRegistry.registerBlock(GSTorch, "Glowstone Torch");
 		LanguageRegistry.addName(GSTorch, "Glowstone Torch");
 		GameRegistry.registerBlock(BlockDirtchest, "Dirtchest");
 		LanguageRegistry.addName(BlockDirtchest, "Dirtchest");
 		GameRegistry.registerTileEntity(TileEntityChestDC.class,
 				"TileEntityChestDC");
 
 		// Loot generation//
 
 		ChestGenHooks.getInfo(ChestGenHooks.DUNGEON_CHEST).addItem(
 				new WeightedRandomChestContent(new ItemStack(
 						Diamerald.Diameraldgem), 1, 3, 20));
 		ChestGenHooks.getInfo(ChestGenHooks.MINESHAFT_CORRIDOR).addItem(
 				new WeightedRandomChestContent(new ItemStack(
 						Diamerald.Diameraldgem), 1, 3, 20));
 		ChestGenHooks.getInfo(ChestGenHooks.VILLAGE_BLACKSMITH).addItem(
 				new WeightedRandomChestContent(new ItemStack(
 						Diamerald.Diameraldgem), 1, 3, 20));
 		ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_DESERT_CHEST).addItem(
 				new WeightedRandomChestContent(new ItemStack(
 						Diamerald.Diameraldgem), 1, 3, 20));
 		ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_JUNGLE_CHEST).addItem(
 				new WeightedRandomChestContent(new ItemStack(
 						Diamerald.Diameraldgem), 1, 3, 20));
 		ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_LIBRARY).addItem(
 				new WeightedRandomChestContent(new ItemStack(
 						Diamerald.Diameraldgem), 1, 3, 20));
 		ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CORRIDOR).addItem(
 				new WeightedRandomChestContent(new ItemStack(
 						Diamerald.Diameraldgem), 1, 3, 20));
 		ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CROSSING).addItem(
 				new WeightedRandomChestContent(new ItemStack(
 						Diamerald.Diameraldgem), 1, 3, 20));
 
 		// Recipes//
 
 		GameRegistry.addRecipe(new ItemStack(Diameraldsword, 1), new Object[] {
 				" D ", " D ", " S ", 'D', Diamerald.Diameraldgem, 'S',
 				Item.stick });
 		GameRegistry.addRecipe(new ItemStack(Diameraldpickaxe, 1),
 				new Object[] { "DDD", " S ", " S ", 'D',
 						Diamerald.Diameraldgem, 'S', Item.stick });
 		GameRegistry.addRecipe(new ItemStack(blackDiameraldsword, 1),
 				new Object[] { " B ", " S ", " R ", 'B',
 						Diamerald.blackDiameraldgem, 'S',
 						new ItemStack(Item.skull, 1, 1), 'R', Item.blazeRod });
 		GameRegistry.addRecipe(new ItemStack(blackDiameraldpickaxe, 1),
 				new Object[] { "BSB", " R ", " R ", 'B',
 						Diamerald.blackDiameraldgem, 'S',
 						new ItemStack(Item.skull, 1, 1), 'R', Item.blazeRod });
 		GameRegistry.addRecipe(new ItemStack(Diameraldaxe, 1), new Object[] {
 				"DD ", "DS ", " S ", 'D', Diamerald.Diameraldgem, 'S',
 				Item.stick });
 		GameRegistry.addRecipe(new ItemStack(Diameraldshovel, 1), new Object[] {
 				" D ", " S ", " S ", 'D', Diamerald.Diameraldgem, 'S',
 				Item.stick });
 		GameRegistry.addRecipe(new ItemStack(Diameraldhoe, 1), new Object[] {
 				"DD ", " S ", " S ", 'D', Diamerald.Diameraldgem, 'S',
 				Item.stick });
 		GameRegistry.addRecipe(new ItemStack(Diameraldhelmet, 1), new Object[] {
 				"DDD", "D D", 'D', Diamerald.Diameraldgem });
 		GameRegistry.addRecipe(new ItemStack(blackDiameraldhelmet, 1),
 						new Object[] { "BBB", "B B", 'B',
 								Diamerald.blackDiameraldgem });
 		GameRegistry.addRecipe(new ItemStack(Diameraldplate, 1), new Object[] {
 				"D D", "DDD", "DDD", 'D', Diamerald.Diameraldgem });
 		GameRegistry.addRecipe(new ItemStack(Diameraldlegs, 1), new Object[] {
 				"DDD", "D D", "D D", 'D', Diamerald.Diameraldgem });
 		GameRegistry.addRecipe(new ItemStack(Diameraldboots, 1), new Object[] {
 				"D D", "D D", 'D', Diamerald.Diameraldgem });
 		GameRegistry.addRecipe(new ItemStack(Diameraldbow, 1), new Object[] {
 				" DW", "B W", " DW", 'D', Diamerald.Diameraldgem, 'W',
 				Item.silk, 'B', Item.blazeRod });
 		GameRegistry.addRecipe(new ItemStack(GSTorch, 16), new Object[] { " G",
 				" G", 'G', Block.glowStone });
 		GameRegistry.addShapelessRecipe(new ItemStack(Roughgem, 1),
 				new Object[] { Item.diamond, Item.emerald });
 		GameRegistry.addShapelessRecipe(new ItemStack(blackRoughgem, 1),
 				new Object[] { Diamerald.Roughgem, Item.dyePowder,
 						Item.dyePowder });
 		GameRegistry.addRecipe(new ItemStack(BlockDirtchest, 1), new Object[] {
 				"AAA", "ACA", "AAA", 'A', Block.dirt, 'C', Block.chest });
 
 		if (Loader.isModLoaded("IC2")) {
 
 			Diameralddust = (new Diameralddust(DiameralddustID))
 					.setUnlocalizedName("Diameralddust");
 			Recipes.macerator.addRecipe(new ItemStack(Diameraldore, 1),
 					new ItemStack(Diameralddust, 2));
 			Recipes.compressor.addRecipe(new ItemStack(Diameralddust, 1),
 					new ItemStack(Diameraldgem, 1));
 			Recipes.compressor.addRecipe(new ItemStack(Item.skull, 1, 1),
 					new ItemStack(blackDiameraldgem, 1));
 			LanguageRegistry.addName(Diameralddust, "Diamerald Dust");
 
 		}
 
 	}
 
 }
