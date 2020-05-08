 package adanaran.mods.bfr;
 
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.EnumToolMaterial;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.CraftingManager;
 import net.minecraft.item.crafting.FurnaceRecipes;
 import net.minecraft.item.crafting.IRecipe;
 import net.minecraft.item.crafting.ShapedRecipes;
 import net.minecraftforge.common.Configuration;
 import adanaran.mods.bfr.blocks.BlockMill;
 import adanaran.mods.bfr.blocks.BlockStove;
 import adanaran.mods.bfr.entities.TileEntityMill;
 import adanaran.mods.bfr.entities.TileEntityStove;
 import adanaran.mods.bfr.items.ItemCakePan;
 import adanaran.mods.bfr.items.ItemFlour;
 import adanaran.mods.bfr.items.ItemMillstone;
 import adanaran.mods.bfr.items.ItemPan;
 import adanaran.mods.bfr.items.ItemPot;
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
 
 /**
  * 
  * @author Adanaran
  * @author Demitreus
  */
 @Mod(modid = "bfr", name = "Better Food Recipes", version = "0.1")
 @NetworkMod(clientSideRequired = true, serverSideRequired = false)
 public class BFR {
 
 	// Mod Instace
 	@Instance("bfr")
 	public static BFR instance;
 
 	// Proxy
 	@SidedProxy(clientSide = "adanaran.mods.bfr.ClientProxy", serverSide = "adanaran.mods.bfr.Proxy")
 	public static Proxy proxy;
 
 	// Log
 	public static Logger logger;
 
 	// ID-Section
 	public static int idBlockStove;
 	public static int idBlockStoveActive;
 	public static int idItemPotIron;
 	public static int idItemPotStone;
 	public static int idItemPotGold;
 	public static int idItemPotDiamond;
 	public static int idItemPanIron;
 	public static int idItemPanStone;
 	public static int idItemPanGold;
 	public static int idItemPanDiamond;
 	public static int idItemCakePanIron;
 	public static int idItemCakePanStone;
 	public static int idItemCakePanGold;
 	public static int idItemCakePanDiamond;
 	public static int idItemMillstone;
 	public static int idBlockMill;
 	public static int idBlockMillActive;
 	public static int idItemFlour;
 
 	// Block-Section
 	public static BlockStove blockStove;
 	public static BlockStove blockStoveOn;
 	public static BlockMill blockMill;
 	public static BlockMill blockMillOn;
 
 	// Item-Section
 	public static ItemPot itemPotIron;
 	public static ItemPot itemPotStone;
 	public static ItemPot itemPotGold;
 	public static ItemPot itemPotDiamond;
 	public static ItemPan itemPanIron;
 	public static ItemPan itemPanStone;
 	public static ItemPan itemPanGold;
 	public static ItemPan itemPanDiamond;
 	public static ItemCakePan itemCakePanIron;
 	public static ItemCakePan itemCakePanStone;
 	public static ItemCakePan itemCakePanGold;
 	public static ItemCakePan itemCakePanDiamond;
 	public static ItemMillstone itemMillstone;
 	public static ItemFlour itemFlour;
 
 	// Init-Section
 	@EventHandler
 	public static void preInit(FMLPreInitializationEvent event) {
 		logger = event.getModLog();
 		Configuration cfg = new Configuration(
 				event.getSuggestedConfigurationFile());
 		try {
 			cfg.load();
 			// Block
 			idBlockStove = cfg.getBlock("blockStove", 3010).getInt(3010);
 			idBlockStoveActive = cfg.getBlock("blockStoveOn", 3011)
 					.getInt(3011);
 			idBlockMill = cfg.getBlock("blockMill", 3012).getInt(3012);
 			idBlockMillActive = cfg.getBlock("blockMillOn", 3013).getInt(3013);
 			// Item
 			idItemPotIron = cfg.getItem("itemPot", 3850).getInt(3850);
 			idItemPotStone = cfg.getItem("itemPotStone", 3851).getInt(3851);
 			idItemPotGold = cfg.getItem("itemPotGold", 3852).getInt(3852);
 			idItemPotDiamond = cfg.getItem("itemPotDiamond", 3853).getInt(3853);
 			idItemPanIron = cfg.getItem("itemPan", 3854).getInt(3854);
 			idItemPanStone = cfg.getItem("itemPanStone", 3855).getInt(3855);
 			idItemPanGold = cfg.getItem("itemPanGold", 3856).getInt(3856);
 			idItemPanDiamond = cfg.getItem("itemPanDiamond", 3857).getInt(3857);
 			idItemCakePanIron = cfg.getItem("itemCakePan", 3858).getInt(3858);
 			idItemCakePanStone = cfg.getItem("itemCakePanStone", 3859).getInt(
 					3859);
 			idItemCakePanGold = cfg.getItem("itemCakePanGold", 3860).getInt(
 					3860);
 			idItemCakePanDiamond = cfg.getItem("itemCakePanDiamond", 3861)
 					.getInt(3861);
 			idItemMillstone = cfg.getItem("itemMillstone", 3862).getInt(3862);
 			idItemFlour = cfg.getItem("itemFlour", 3863).getInt(3863);
 		} catch (Exception e) {
 			logger.log(Level.WARNING,
 					"Could not load config for Better Food Recipes!", e);
 		} finally {
 			if (cfg.hasChanged()) {
 				cfg.save();
 			}
 		}
 	}
 
 	@EventHandler
 	public static void init(FMLInitializationEvent event) {
 		// Items registrieren
 		NetworkRegistry.instance().registerGuiHandler(instance, proxy);
 		registerStove();
 		registerCookware();
 		registerMill();
		registerFood();
 	}
 
 	@EventHandler
 	public static void postInit(FMLPostInitializationEvent event) {
 		// Vanilla Rezepte entfernen
 		removeRecipe(new ItemStack(Item.bread));
 		removeRecipe(new ItemStack(Item.bowlSoup));
 		removeRecipe(new ItemStack(Item.sugar));
 		removeRecipe(new ItemStack(Item.cake));
 		removeRecipe(new ItemStack(Item.cookie));
 		removeRecipe(new ItemStack(Item.pumpkinPie));
 		removeFurnaceRecipes(Item.porkRaw);
 		removeFurnaceRecipes(Item.beefRaw);
 		removeFurnaceRecipes(Item.chickenRaw);
 		removeFurnaceRecipes(Item.fishRaw);
 		removeFurnaceRecipes(Item.potato);
 	}
 
 	// Recipe removal functions
 	private static void removeRecipe(ItemStack is) {
 		List<IRecipe> l = CraftingManager.getInstance().getRecipeList();
 		for (int i = 0; i < l.size(); i++) {
 			IRecipe r = l.get(i);
 			if (r instanceof ShapedRecipes) {
 				ShapedRecipes sr = (ShapedRecipes) r;
 				ItemStack res = sr.getRecipeOutput();
 				if (ItemStack.areItemStacksEqual(is, res)) {
 					l.remove(i--);
 				}
 			}
 		}
 	}
 
 	private static void removeFurnaceRecipes(Item item) {
 		FurnaceRecipes.smelting().getSmeltingList().remove(item.itemID);
 	}
 
 	// Registration Section
 	private static void registerStove() {
 		blockStove = new BlockStove(idBlockStove, false);
 		blockStoveOn = new BlockStove(idBlockStoveActive, true);
 		blockStove.setUnlocalizedName("Stove");
 		blockStoveOn.setUnlocalizedName("activeStove");
 		blockStove.setCreativeTab(CreativeTabs.tabDecorations);
 		GameRegistry.registerBlock(blockStove, blockStove.getUnlocalizedName());
 		GameRegistry.registerBlock(blockStoveOn,
 				blockStoveOn.getUnlocalizedName());
 		GameRegistry.addRecipe(new ItemStack(blockStove), new Object[] { "CCC",
 				"C C", "CCC", Character.valueOf('C'), Block.stone });
 		LanguageRegistry.addName(blockStove, "Stove");
 		LanguageRegistry.addName(blockStoveOn, "Stove");
 
 		GameRegistry.registerTileEntity(TileEntityStove.class,
 				"TileEntityStove");
 	}
 
 	private static void registerCookware() {
 		itemPotIron = new ItemPot(idItemPotIron, EnumToolMaterial.IRON);
 		itemPotStone = new ItemPot(idItemPotStone, EnumToolMaterial.STONE);
 		itemPotGold = new ItemPot(idItemPotGold, EnumToolMaterial.GOLD);
 		itemPotDiamond = new ItemPot(idItemPotDiamond, EnumToolMaterial.EMERALD);
 		itemPotIron.setUnlocalizedName("Pot");
 		itemPotStone.setUnlocalizedName("Stonepot");
 		itemPotGold.setUnlocalizedName("Goldpot");
 		itemPotDiamond.setUnlocalizedName("Diamondpot");
 		itemPotIron.setCreativeTab(CreativeTabs.tabTools);
 		itemPotStone.setCreativeTab(CreativeTabs.tabTools);
 		itemPotGold.setCreativeTab(CreativeTabs.tabTools);
 		itemPotDiamond.setCreativeTab(CreativeTabs.tabTools);
 		GameRegistry
 				.registerItem(itemPotIron, itemPotIron.getUnlocalizedName());
 		GameRegistry.registerItem(itemPotStone,
 				itemPotStone.getUnlocalizedName());
 		GameRegistry
 				.registerItem(itemPotGold, itemPotGold.getUnlocalizedName());
 		GameRegistry.registerItem(itemPotDiamond,
 				itemPotDiamond.getUnlocalizedName());
 
 		GameRegistry.addRecipe(new ItemStack(itemPotIron), new Object[] {
 				"S S", "C C", "CCC", Character.valueOf('S'), Item.stick,
 				Character.valueOf('C'), Item.ingotIron });
 		GameRegistry.addRecipe(new ItemStack(itemPotStone), new Object[] {
 				"S S", "C C", "CCC", Character.valueOf('S'), Item.stick,
 				Character.valueOf('C'), Block.stone });
 		GameRegistry.addRecipe(new ItemStack(itemPotGold), new Object[] {
 				"S S", "C C", "CCC", Character.valueOf('S'), Item.stick,
 				Character.valueOf('C'), Item.ingotGold });
 		GameRegistry.addRecipe(new ItemStack(itemPotDiamond), new Object[] {
 				"S S", "C C", "CCC", Character.valueOf('S'), Item.stick,
 				Character.valueOf('C'), Item.diamond });
 
 		LanguageRegistry.addName(itemPotIron, "Pot");
 		LanguageRegistry.addName(itemPotStone, "Stonepot");
 		LanguageRegistry.addName(itemPotGold, "Goldpot");
 		LanguageRegistry.addName(itemPotDiamond, "Diamondpot");
 
 		itemPanIron = new ItemPan(idItemPanIron, EnumToolMaterial.IRON);
 		itemPanStone = new ItemPan(idItemPanStone, EnumToolMaterial.STONE);
 		itemPanGold = new ItemPan(idItemPanGold, EnumToolMaterial.GOLD);
 		itemPanDiamond = new ItemPan(idItemPanDiamond, EnumToolMaterial.EMERALD);
 		itemPanIron.setUnlocalizedName("Pan");
 		itemPanStone.setUnlocalizedName("Stonepan");
 		itemPanGold.setUnlocalizedName("Goldpan");
 		itemPanDiamond.setUnlocalizedName("Diamondpan");
 		itemPanIron.setCreativeTab(CreativeTabs.tabTools);
 		itemPanStone.setCreativeTab(CreativeTabs.tabTools);
 		itemPanGold.setCreativeTab(CreativeTabs.tabTools);
 		itemPanDiamond.setCreativeTab(CreativeTabs.tabTools);
 		GameRegistry
 				.registerItem(itemPanIron, itemPanIron.getUnlocalizedName());
 		GameRegistry.registerItem(itemPanStone,
 				itemPanStone.getUnlocalizedName());
 		GameRegistry
 				.registerItem(itemPanGold, itemPanGold.getUnlocalizedName());
 		GameRegistry.registerItem(itemPanDiamond,
 				itemPanDiamond.getUnlocalizedName());
 
 		GameRegistry.addRecipe(new ItemStack(itemPanIron),
 				new Object[] { "S  ", "CCC", Character.valueOf('S'),
 						Item.stick, Character.valueOf('C'), Item.ingotIron });
 		GameRegistry.addRecipe(new ItemStack(itemPanStone),
 				new Object[] { "S  ", "CCC", Character.valueOf('S'),
 						Item.stick, Character.valueOf('C'), Block.stone });
 		GameRegistry.addRecipe(new ItemStack(itemPanGold),
 				new Object[] { "S  ", "CCC", Character.valueOf('S'),
 						Item.stick, Character.valueOf('C'), Item.ingotGold });
 		GameRegistry.addRecipe(new ItemStack(itemPanDiamond),
 				new Object[] { "S  ", "CCC", Character.valueOf('S'),
 						Item.stick, Character.valueOf('C'), Item.diamond });
 
 		LanguageRegistry.addName(itemPanIron, "Pan");
 		LanguageRegistry.addName(itemPanStone, "Stonepan");
 		LanguageRegistry.addName(itemPanGold, "Goldpan");
 		LanguageRegistry.addName(itemPanDiamond, "Diamondpan");
 
 		itemCakePanIron = new ItemCakePan(idItemCakePanIron,
 				EnumToolMaterial.IRON);
 		itemCakePanStone = new ItemCakePan(idItemCakePanStone,
 				EnumToolMaterial.STONE);
 		itemCakePanGold = new ItemCakePan(idItemCakePanGold,
 				EnumToolMaterial.GOLD);
 		itemCakePanDiamond = new ItemCakePan(idItemCakePanDiamond,
 				EnumToolMaterial.EMERALD);
 		itemCakePanIron.setUnlocalizedName("Cakepan");
 		itemCakePanStone.setUnlocalizedName("Stonecakepan");
 		itemCakePanGold.setUnlocalizedName("Goldcakepan");
 		itemCakePanDiamond.setUnlocalizedName("Diamondcakepan");
 		itemCakePanIron.setCreativeTab(CreativeTabs.tabTools);
 		itemCakePanStone.setCreativeTab(CreativeTabs.tabTools);
 		itemCakePanGold.setCreativeTab(CreativeTabs.tabTools);
 		itemCakePanDiamond.setCreativeTab(CreativeTabs.tabTools);
 		GameRegistry.registerItem(itemCakePanIron,
 				itemCakePanIron.getUnlocalizedName());
 		GameRegistry.registerItem(itemCakePanStone,
 				itemCakePanStone.getUnlocalizedName());
 		GameRegistry.registerItem(itemCakePanGold,
 				itemCakePanGold.getUnlocalizedName());
 		GameRegistry.registerItem(itemCakePanDiamond,
 				itemCakePanDiamond.getUnlocalizedName());
 
 		GameRegistry
 				.addRecipe(new ItemStack(itemPotIron), new Object[] { "I I",
 						"IBI", Character.valueOf('I'), Item.ingotIron,
 						Character.valueOf('I'), Block.blockIron });
 		GameRegistry.addRecipe(new ItemStack(itemPotStone), new Object[] {
 				"C C", "CCC", Character.valueOf('C'), Block.stone });
 		GameRegistry.addRecipe(new ItemStack(itemPotGold), new Object[] {
 				"C C", "CCC", Character.valueOf('C'), Item.ingotGold });
 		GameRegistry.addRecipe(new ItemStack(itemPotDiamond), new Object[] {
 				"C C", "CCC", Character.valueOf('C'), Item.diamond });
 
 		LanguageRegistry.addName(itemCakePanIron, "Cakepan");
 		LanguageRegistry.addName(itemCakePanStone, "Stonecakepan");
 		LanguageRegistry.addName(itemCakePanGold, "Goldcakepan");
 		LanguageRegistry.addName(itemCakePanDiamond, "Diamondcakepan");
 	}
 
 	private static void registerMill() {
 		// Millstone
 		itemMillstone = new ItemMillstone(idItemMillstone);
 		itemMillstone.setUnlocalizedName("Millstone");
 		itemMillstone.setCreativeTab(CreativeTabs.tabTools);
 		GameRegistry.registerItem(itemMillstone,
 				itemMillstone.getUnlocalizedName());
 		GameRegistry.addRecipe(new ItemStack(itemMillstone), " C ", "CSC",
 				" C ", Character.valueOf('S'), Item.stick,
 				Character.valueOf('C'), Block.cobblestone);
 		LanguageRegistry.addName(itemMillstone, "Millstone");
 
 		// Mill
 		blockMill = new BlockMill(idBlockMill);
 		blockMillOn = new BlockMill(idBlockMillActive);
 		blockMill.setUnlocalizedName("Mill");
 		blockMillOn.setUnlocalizedName("activeMill");
 		blockMill.setCreativeTab(CreativeTabs.tabDecorations);
 		GameRegistry.registerBlock(blockMill, blockMill.getUnlocalizedName());
 		GameRegistry.registerBlock(blockMillOn,
 				blockMillOn.getUnlocalizedName());
 		GameRegistry
 				.addRecipe(new ItemStack(blockMill), new Object[] { "C C",
 						"CCC", "SSS", Character.valueOf('S'), Block.stone,
 						Character.valueOf('C'), Block.cobblestone });
 		LanguageRegistry.addName(blockMill, "Mill");
 		LanguageRegistry.addName(blockMillOn, "Mill");
 
 		GameRegistry.registerTileEntity(TileEntityMill.class, "TileEntityMill");
 	}
 
 	private static void registerFood() {
 		itemFlour = new ItemFlour(idItemFlour);
		itemFlour.setUnlocalizedName("Flour");
 		itemFlour.setCreativeTab(CreativeTabs.tabMaterials);
 		GameRegistry.registerItem(itemFlour, itemFlour.getUnlocalizedName());
 		LanguageRegistry.addName(itemFlour, "Flour");
 	}
 }
