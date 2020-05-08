 package modJam;
 
 import java.util.logging.Level;
 
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.EnumArmorMaterial;
 import net.minecraft.item.EnumToolMaterial;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.CraftingManager;
 import net.minecraft.item.crafting.FurnaceRecipes;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.EnumHelper;
 import net.minecraftforge.common.ForgeDirection;
 import net.minecraftforge.oredict.OreDictionary;
 import net.minecraftforge.oredict.ShapedOreRecipe;
 import cpw.mods.fml.common.FMLLog;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 @Mod(modid="fuj1n.modJam", name=CommonProxyModJam.modName, version=CommonProxyModJam.version)
 @NetworkMod(clientSideRequired=true, serverSideRequired=false)
 
 public class ModJam {
 	@SidedProxy(serverSide="modJam.CommonProxyModJam", clientSide="modJam.ClientProxyModJam")
 	public static CommonProxyModJam proxy;
 	public static Configuration config;
 	
 	//Config values
 	//Blocks
 	public static int oreAwesomeID = 1024;
 	public static int[] woodChairIDs = {
 		1025, 1026, 1027, 1028
 	};
 	public static int[] stoneChairIDs = {
 		1029, 1030, 1031, 1032
 	};
 	public static int woodTableID = 1033;
 	public static int stoneTableID = 1034;
 	//Items
 	public static int ingotAwesomeID = 3240;
 	public static int woodChairID = 3241;
 	public static int stoneChairID = 3242;
 	public static int awesomeArmorHelmetID = 3243;
 	public static int awesomeArmorChestplateID = 3244;
 	public static int awesomeArmorLeggingsID = 3245;
 	public static int awesomeArmorBootsID = 3246;
 	public static int awesomeToolPickaxeID = 3247;
 	public static int awesomeToolShovelID = 3248;
 	public static int awesomeToolSwordID = 3249;
 	public static int awesomeToolAxeID = 3250;
 	public static int awesomeToolHoeID = 3251;
 	public static int darkExtractID = 3252;
 	//End Config values
 	//Blocks
 	public static Block awesomeOre;
 	public static Block woodChairNorth;
 	public static Block woodChairEast;
 	public static Block woodChairSouth;
 	public static Block woodChairWest;
 	public static Block stoneChairNorth;
 	public static Block stoneChairEast;
 	public static Block stoneChairSouth;
 	public static Block stoneChairWest;
 	public static Block woodTable;
 	public static Block stoneTable;
 	//Items
 	public static Item awesomeIngot;
 	public static Item woodChair;
 	public static Item stoneChair;
 	public static Item awesomeHelmet;
 	public static Item awesomeChestplate;
 	public static Item awesomeLeggings;
 	public static Item awesomeBoots;
 	public static Item awesomePickaxe;
 	public static Item awesomeShovel;
 	public static Item awesomeSword;
 	public static Item awesomeAxe;
 	public static Item awesomeHoe;
 	public static Item darkExtract;
 	//Materials
 	public static EnumArmorMaterial awesomeArmorMaterial;
 	public static EnumToolMaterial awesomeToolMaterial;
 	//CreativeTabs
 	public static CreativeTabs modJamCreativeTab;
 	//Sub Names
 	private static final String[] awesomeColors = { 
 		"White", "Orange", "Magenta",
 		"Light-Blue", "Yellow", "Lime", "Pink", "Gray", "Light-Gray", "Cyan",
 		"Purple", "Blue", "Brown", "Green", "Red", "Black"
 	};
 	
 	@PreInit
 	public void PreInit(FMLPreInitializationEvent event){
 		config = new Configuration(event.getSuggestedConfigurationFile());
 		config.load();
 		//Blocks
 		oreAwesomeID = config.getBlock("Awesome Ore ID", oreAwesomeID).getInt();
 		woodChairIDs[0] = config.getBlock("Wooden Chair ID Set(of 4)", woodChairIDs[0]).getInt();
 		stoneChairIDs[0] = config.getBlock("Stone Chair ID Set(of 4)", stoneChairIDs[0]).getInt();
 		woodTableID = config.getBlock("Wooden Table ID", woodTableID).getInt();
 		stoneTableID = config.getBlock("Stone Table ID", stoneTableID).getInt();
 		refreshChairIDs();
 		//Items
 		ingotAwesomeID = config.getItem("Awesome Ingot ID", ingotAwesomeID).getInt();
 		woodChairID = config.getItem("Wooden Chair Item ID", woodChairID).getInt();
 		stoneChairID = config.getItem("Stone Chair Item ID", stoneChairID).getInt();
 		awesomeArmorHelmetID = config.getItem("Awesome Helmet ID", awesomeArmorHelmetID).getInt();
 		awesomeArmorChestplateID = config.getItem("Awesome Chestplate ID", awesomeArmorChestplateID).getInt();
 		awesomeArmorLeggingsID = config.getItem("Awesome Leggings ID", awesomeArmorLeggingsID).getInt();
 		awesomeArmorBootsID = config.getItem("Awesome Boots ID", awesomeArmorBootsID).getInt();
 		awesomeToolPickaxeID = config.getItem("Awesome Pickaxe ID", awesomeToolPickaxeID).getInt();
 		awesomeToolShovelID = config.getItem("Awesome Shovel ID", awesomeToolShovelID).getInt();
 		awesomeToolSwordID = config.getItem("Awesome Sword ID", awesomeToolSwordID).getInt();
 		awesomeToolAxeID = config.getItem("Awesome Axe ID", awesomeToolAxeID).getInt();
 		awesomeToolHoeID = config.getItem("Awesome Hoe ID", awesomeToolHoeID).getInt();
 		darkExtractID = config.getItem("Dark Extract ID", darkExtractID).getInt();
 		config.save();
 	} 
 	
 	public void refreshChairIDs(){
 		woodChairIDs[1] = woodChairIDs[0] + 1;
 		woodChairIDs[2] = woodChairIDs[1] + 1;
 		woodChairIDs[3] = woodChairIDs[2] + 1;
 		stoneChairIDs[1] = stoneChairIDs[0] + 1;
 		stoneChairIDs[2] = stoneChairIDs[1] + 1;
 		stoneChairIDs[3] = stoneChairIDs[2] + 1;
 	}
 	
 	@Init
 	public void Init(FMLInitializationEvent event){
 		proxy.handler();
 		registerCreativeTab();
 		initAllMaterials();
 		initAllItems();
 		initAllBlocks();
 		registerAllBlocks();
 		addAllNames();
 		addAllCrafting();
 		addAllSmelting();
 		registerAllOreDictionary();
 		registerAllWorldGenerators();
 	}
 	
 	public void initAllMaterials(){
 		awesomeArmorMaterial = EnumHelper.addArmorMaterial("AWESOME", 13, new int[]{3, 7, 5, 2}, 25);
 		awesomeToolMaterial = EnumHelper.addToolMaterial("AWESOME", 1300, 250, 7.0F, 2, 22);
 	}
 	
 	public void initAllBlocks(){
 		awesomeOre = new BlockAwesomeOre(oreAwesomeID).setHardness(5F).setResistance(5F).setCreativeTab(modJamCreativeTab).setUnlocalizedName("fuj1n.modJam.AwesomeOre");
 		woodChairNorth = new BlockChair(woodChairIDs[0], ForgeDirection.NORTH, Block.planks, woodChair.itemID).setHardness(0.3F).setUnlocalizedName("fuj1n.modJam.tileChair");
 		woodChairEast = new BlockChair(woodChairIDs[1], ForgeDirection.EAST, Block.planks, woodChair.itemID).setHardness(0.3F).setUnlocalizedName("fuj1n.modJam.tileChair");
 		woodChairSouth = new BlockChair(woodChairIDs[2], ForgeDirection.SOUTH, Block.planks, woodChair.itemID).setHardness(0.3F).setUnlocalizedName("fuj1n.modJam.tileChair");
 		woodChairWest = new BlockChair(woodChairIDs[3], ForgeDirection.WEST, Block.planks, woodChair.itemID).setHardness(0.3F).setUnlocalizedName("fuj1n.modJam.tileChair");
 		stoneChairNorth = new BlockChair(stoneChairIDs[0], ForgeDirection.NORTH, Block.stone, stoneChair.itemID).setHardness(0.3F).setUnlocalizedName("fuj1n.modJam.tileChair");
 		stoneChairEast = new BlockChair(stoneChairIDs[1], ForgeDirection.EAST, Block.stone, stoneChair.itemID).setHardness(0.3F).setUnlocalizedName("fuj1n.modJam.tileChair");
 		stoneChairSouth = new BlockChair(stoneChairIDs[2], ForgeDirection.SOUTH, Block.stone, stoneChair.itemID).setHardness(0.3F).setUnlocalizedName("fuj1n.modJam.tileChair");
 		stoneChairWest = new BlockChair(stoneChairIDs[3], ForgeDirection.WEST, Block.stone, stoneChair.itemID).setHardness(0.3F).setUnlocalizedName("fuj1n.modJam.tileChair");
 		woodTable = new BlockTable(woodTableID, Block.planks).setHardness(0.3F).setCreativeTab(modJamCreativeTab).setUnlocalizedName("fuj1n.modJam.woodTable");
 		stoneTable = new BlockTable(stoneTableID, Block.stone).setHardness(0.3F).setCreativeTab(modJamCreativeTab).setUnlocalizedName("fuj1n.modJam.stoneTable");
 	}
 	
 	public void initAllItems(){
 		awesomeIngot = new ItemAwesomeIngot(ingotAwesomeID).setCreativeTab(modJamCreativeTab);
 		woodChair = new ItemChair(woodChairID, 0, this.woodChairIDs[0]).setCreativeTab(modJamCreativeTab).setUnlocalizedName("woodChair");
 		stoneChair = new ItemChair(stoneChairID, 1, this.stoneChairIDs[0]).setCreativeTab(modJamCreativeTab).setUnlocalizedName("stoneChair");
 		awesomeHelmet = new ItemAwesomeArmor(awesomeArmorHelmetID, awesomeArmorMaterial, CommonProxyModJam.awesomeArmorID, 0, "awesomeMod:fuj1n.AwesomeMod.awesomeArmor").setUnlocalizedName("fuj1n.AwesomeMod.awesomeArmor");
 		awesomeChestplate = new ItemAwesomeArmor(awesomeArmorChestplateID, awesomeArmorMaterial, CommonProxyModJam.awesomeArmorID, 1, "awesomeMod:fuj1n.AwesomeMod.awesomeArmor").setUnlocalizedName("fuj1n.AwesomeMod.awesomeArmor");
 		awesomeLeggings = new ItemAwesomeArmor(awesomeArmorLeggingsID, awesomeArmorMaterial, CommonProxyModJam.awesomeArmorID, 2, "awesomeMod:fuj1n.AwesomeMod.awesomeArmor").setUnlocalizedName("fuj1n.AwesomeMod.awesomeArmor");
 		awesomeBoots = new ItemAwesomeArmor(awesomeArmorBootsID, awesomeArmorMaterial, CommonProxyModJam.awesomeArmorID, 3, "awesomeMod:fuj1n.AwesomeMod.awesomeArmor").setUnlocalizedName("fuj1n.AwesomeMod.awesomeArmor");
 		awesomePickaxe = new ItemAwesomePickaxe(awesomeToolPickaxeID, awesomeToolMaterial, "awesomeMod:fuj1n.AwesomeMod.awesomePickaxe").setUnlocalizedName("fuj1n.AwesomeMod.awesomePickaxe");
 		awesomeShovel = new ItemAwesomeShovel(awesomeToolShovelID, awesomeToolMaterial, "awesomeMod:fuj1n.AwesomeMod.awesomeShovel").setUnlocalizedName("fuj1n.AwesomeMod.awesomeShovel");
 		awesomeSword = new ItemAwesomeSword(awesomeToolSwordID, awesomeToolMaterial, "awesomeMod:fuj1n.AwesomeMod.awesomeSword").setUnlocalizedName("fuj1n.AwesomeMod.awesomeSword");
 		awesomeAxe = new ItemAwesomeAxe(awesomeToolAxeID, awesomeToolMaterial, "awesomeMod:fuj1n.AwesomeMod.awesomeAxe").setUnlocalizedName("fuj1n.AwesomeMod.awesomeAxe");
 		awesomeHoe = new ItemAwesomeHoe(awesomeToolHoeID, awesomeToolMaterial, "awesomeMod:fuj1n.AwesomeMod.awesomeHoe").setUnlocalizedName("fuj1n.AwesomeMod.awesomeHoe");
 		darkExtract = new ItemDarkExtract(darkExtractID).setCreativeTab(modJamCreativeTab).setUnlocalizedName("fuj1n.AwesomeMod.darkExtract");
 	}
 	
 	public void registerAllBlocks(){
 		GameRegistry.registerBlock(awesomeOre, ItemAwesomeOre.class, "fuj1n.modJam.awesomeOre");
 		GameRegistry.registerBlock(woodChairNorth, "fuj1n.modJam.woodChairNorth");
 		GameRegistry.registerBlock(woodChairEast, "fuj1n.modJam.woodChairEast");
 		GameRegistry.registerBlock(woodChairSouth, "fuj1n.modJam.woodChairSouth");
 		GameRegistry.registerBlock(woodChairWest, "fuj1n.modJam.woodChairWest");
 		GameRegistry.registerBlock(stoneChairNorth, "fuj1n.modJam.stoneChairNorth");
 		GameRegistry.registerBlock(stoneChairEast, "fuj1n.modJam.stoneChairEast");
 		GameRegistry.registerBlock(stoneChairSouth, "fuj1n.modJam.stoneChairSouth");
 		GameRegistry.registerBlock(stoneChairWest, "fuj1n.modJam.stoneChairWest");
 		GameRegistry.registerBlock(woodTable, ItemTable.class, "fuj1n.modJam.woodTable");
 		GameRegistry.registerBlock(stoneTable, ItemTable.class, "fuj1n.modJam.stoneTable");
 	}
 	
 	public void addAllNames(){
 		for (int i = 0; i < 16; i++) {
 			LanguageRegistry.addName(new ItemStack(awesomeOre, 1, i), awesomeColors[new ItemStack(awesomeOre, 1, i).getItemDamage()] + " Awesome Ore");
 			LanguageRegistry.addName(new ItemStack(awesomeIngot, 1, i), awesomeColors[new ItemStack(awesomeIngot, 1, i).getItemDamage()] + " Awesome Ingot");
 			LanguageRegistry.addName(new ItemStack(woodChair, 1, i), awesomeColors[new ItemStack(woodChair, 1, i).getItemDamage()] + " Glowing Wooden Chair");
 			LanguageRegistry.addName(new ItemStack(stoneChair, 1, i), awesomeColors[new ItemStack(stoneChair, 1, i).getItemDamage()] + " Glowing Stone Chair");
 			LanguageRegistry.addName(new ItemStack(woodTable, 1, i), awesomeColors[new ItemStack(woodTable, 1, i).getItemDamage()] + " Glowing Wooden Table");
 			LanguageRegistry.addName(new ItemStack(stoneTable, 1, i), awesomeColors[new ItemStack(stoneTable, 1, i).getItemDamage()] + " Glowing Stone Table");
 			//LanguageRegistry.instance().addStringLocalization(awesomeHelmet.getUnlocalizedName(ItemAwesomeArmor.getItemStackForNaming(awesomeHelmet.itemID, i)), awesomeColors[i] + " Awesome Helmet");
 		}
 		LanguageRegistry.addName(new ItemStack(awesomeHelmet), "Awesome Helmet");
 		LanguageRegistry.addName(new ItemStack(awesomeChestplate), "Awesome Chestplate");
 		LanguageRegistry.addName(new ItemStack(awesomeLeggings), "Awesome Leggings");
 		LanguageRegistry.addName(new ItemStack(awesomeBoots), "Awesome Boots");
 		LanguageRegistry.addName(awesomePickaxe, "Awesome Pickaxe");
 		LanguageRegistry.addName(awesomeShovel, "Awesome Shovel");
 		LanguageRegistry.addName(awesomeSword, "Awesome Sword");
 		LanguageRegistry.addName(awesomeAxe, "Awesome Axe");
 		LanguageRegistry.addName(awesomeHoe, "Awesome Hoe");
 		LanguageRegistry.addName(darkExtract, "Dark Extract");
 	}
 	
 	public void registerCreativeTab(){
 		modJamCreativeTab = new CreativeTabModJam("fuj1n.modJam");
 		LanguageRegistry.instance().addStringLocalization("itemGroup." + modJamCreativeTab.getTabLabel(), CommonProxyModJam.modName);
 	}
 	
 	public void addAllCrafting(){
 		for(int i = 0; i < 15; i++){
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(woodChair, 1, i), new Object[]{
 				"PXX", "PPP", "PXP", Character.valueOf('P'), Block.planks, Character.valueOf('X'), "ingotAwesome" + awesomeColors[i]
 			}));
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(woodChair, 1, i), new Object[]{
 				"XXP", "PPP", "PXP", Character.valueOf('P'), Block.planks, Character.valueOf('X'), "ingotAwesome" + awesomeColors[i]
 			}));
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(stoneChair, 1, i), new Object[]{
 				"SXX", "SSS", "SXS", Character.valueOf('S'), Block.stone, Character.valueOf('X'), "ingotAwesome" + awesomeColors[i]
 			}));
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(stoneChair, 1, i), new Object[]{
 				"XXS", "SSS", "SXS", Character.valueOf('S'), Block.stone, Character.valueOf('X'), "ingotAwesome" + awesomeColors[i]
 			}));
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(woodTable, 1, i), new Object[]{
 				"XXX", "PPP", "PXP", Character.valueOf('P'), Block.planks, Character.valueOf('X'), "ingotAwesome" + awesomeColors[i]
 			}));
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(stoneTable, 1, i), new Object[]{
 				"XXX", "SSS", "SXS", Character.valueOf('S'), Block.stone, Character.valueOf('X'), "ingotAwesome" + awesomeColors[i]
 			}));
			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(awesomeIngot, 1, 15), new Object[]{
 				"XXX", "XDX", "XXX", Character.valueOf('D'), "extractDark", Character.valueOf('X'), "ingotAwesome" + awesomeColors[i]
 			}));
 		}
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(awesomeHelmet, 1, 0), new Object[]{
 			" X ", "XHX", " X ", Character.valueOf('H'), Item.helmetSteel, Character.valueOf('X'), "ingotAwesomeBlack"
 		}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(awesomeChestplate, 1, 0), new Object[]{
 			" X ", "XCX", " X ", Character.valueOf('C'), Item.plateSteel, Character.valueOf('X'), "ingotAwesomeBlack"
 		}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(awesomeLeggings, 1, 0), new Object[]{
 			" X ", "XLX", " X ", Character.valueOf('L'), Item.legsSteel, Character.valueOf('X'), "ingotAwesomeBlack"
 		}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(awesomeBoots, 1, 0), new Object[]{
 			" X ", "XBX", " X ", Character.valueOf('B'), Item.bootsSteel, Character.valueOf('X'), "ingotAwesomeBlack"
 		}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(awesomePickaxe, 1, 0), new Object[]{
 			" X ", "XPX", " X ", Character.valueOf('P'), Item.pickaxeSteel, Character.valueOf('X'), "ingotAwesomeBlack"
 		}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(awesomeShovel, 1, 0), new Object[]{
 			" X ", "XSX", " X ", Character.valueOf('S'), Item.shovelSteel, Character.valueOf('X'), "ingotAwesomeBlack"
 		}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(awesomeSword, 1, 0), new Object[]{
 			" X ", "XSX", " X ", Character.valueOf('S'), Item.swordSteel, Character.valueOf('X'), "ingotAwesomeBlack"
 		}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(awesomeAxe, 1, 0), new Object[]{
 			" X ", "XAX", " X ", Character.valueOf('A'), Item.axeSteel, Character.valueOf('X'), "ingotAwesomeBlack"
 		}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(awesomeHoe, 1, 0), new Object[]{
 			" X ", "XHX", " X ", Character.valueOf('H'), Item.hoeSteel, Character.valueOf('X'), "ingotAwesomeBlack"
 		}));
 	}
 	
 	public void addAllSmelting(){
 		for (int i = 0; i < 15; i++){
 			FurnaceRecipes.smelting().addSmelting(oreAwesomeID, i, new ItemStack(awesomeIngot, 1, i), 0.1F);
 		}
 		FurnaceRecipes.smelting().addSmelting(Item.coal.itemID, new ItemStack(darkExtract, 3, 0), 0.1F);
 	}
 	
 	public void registerAllOreDictionary(){
 		for (int i = 0; i < 16; i++) {
 			OreDictionary.registerOre("oreAwesome" + awesomeColors[i], new ItemStack(awesomeOre, 1, i));
 			OreDictionary.registerOre("ingotAwesome" + awesomeColors[i], new ItemStack(awesomeIngot, 1, i));
 		}
 		OreDictionary.registerOre("extractDark", darkExtract);
 		
 	}
 	
 	public void registerAllWorldGenerators(){
 		GameRegistry.registerWorldGenerator(new WorldGeneratorModJam());
 	}
 	
     public static <var> void log(var s, Level level){
         FMLLog.log(level, "[Awesome Mod] %s", s);
     }
 }
