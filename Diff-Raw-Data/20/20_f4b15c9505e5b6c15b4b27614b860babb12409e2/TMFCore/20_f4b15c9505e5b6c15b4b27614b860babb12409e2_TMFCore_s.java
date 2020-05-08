 package slimevoid.tmf.core;
 
 import java.io.File;
 
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.EnumArmorMaterial;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.Configuration;
 import slimevoid.tmf.blocks.ores.BlockTMFOre;
 import slimevoid.tmf.core.lib.BlockLib;
 import slimevoid.tmf.core.lib.NamingLib;
 import slimevoid.tmf.fuel.MineralFuelHandler;
 import slimevoid.tmf.items.ItemMineral;
 import slimevoid.tmf.items.ItemMineralDust;
 import slimevoid.tmf.items.ItemMineralMixedDust;
 import slimevoid.tmf.items.ItemMiningHelmet;
 import slimevoid.tmf.items.ItemMiningLamp;
 import slimevoid.tmf.items.ItemMiningToolBelt;
 import slimevoid.tmf.items.ItemMotionSensor;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 public class TMFCore {
 	public static final String packetChannel = "TMF";
 	
 	// CONFIG
 	public static File configFile;
 	public static Configuration configuration;
 	
 	// MINING EQUIPMENT
 	public static Item miningHelmetLamp;
 	public static Item miningHelmetIron;
 	public static Item miningHelmetGold;
 	public static Item miningHelmetDiamond;
 	public static int miningHelmetLampId, miningHelmetIronId, miningHelmetGoldId, miningHelmetDiamondId;
 	public static Item motionSensor;
 	public static int motionSensorId;
 	public static Item miningToolBelt;
 	public static int miningToolBeltId;
 	
 	// MINERALS
 	public static Item mineralAcxium;
 	public static Item mineralBisogen;
 	public static Item mineralCydrine;
 	public static int mineralAcxiumId,mineralBisogenId,mineralCydrineId;
 	// MINERAL DUSTS
 	public static Item dustAcxium;
 	public static Item dustBisogen;
 	public static Item dustCydrine;
 	public static Item dustMixed;
 	public static int dustAcxiumId,dustBisogenId,dustCydrineId,dustMixedId;
 	
 	public static String loggerLevel = "INFO";
 
 	// ======== ITEM REGISTRATION ========
 	
 	public static void addItems() {
 		miningHelmetLamp = new ItemMiningLamp(miningHelmetLampId).setItemName("miningHelmetLamp").setIconCoord(4, 0);
 		miningHelmetIron = new ItemMiningHelmet(miningHelmetIronId, EnumArmorMaterial.IRON, 2, 0).setItemName("ironMiningHelmet").setIconCoord(0, 0);
 		miningHelmetGold = new ItemMiningHelmet(miningHelmetGoldId, EnumArmorMaterial.GOLD, 4, 0).setItemName("goldMiningHelmet").setIconCoord(1, 0);
 		miningHelmetDiamond = new ItemMiningHelmet(miningHelmetDiamondId, EnumArmorMaterial.DIAMOND, 3, 0).setItemName("diamondMiningHelmet").setIconCoord(2, 0);
 		motionSensor = new ItemMotionSensor(motionSensorId).setItemName("motionSensor").setIconCoord(0, 1);
 		miningToolBelt = new ItemMiningToolBelt(miningToolBeltId).setItemName("miningToolBelt").setIconCoord(0, 2);
 		
 		mineralAcxium = new ItemMineral(mineralAcxiumId).setBurnTime(3200).setItemName("mineralAcxium").setIconCoord(0, 1);
 		mineralBisogen = new ItemMineral(mineralBisogenId).setBurnTime(1600).setItemName("mineralBisogen").setIconCoord(1, 1);
 		mineralCydrine = new ItemMineral(mineralCydrineId).setBurnTime(1600).setItemName("mineralCydrine").setIconCoord(2, 1);
 		
 		dustAcxium = new ItemMineralDust(dustAcxiumId).setBurnTime(3200).setItemName("dustAcxium").setIconCoord(0, 2);
 		dustBisogen = new ItemMineralDust(dustBisogenId).setBurnTime(1600).setItemName("dustBisogen").setIconCoord(1, 2);
 		dustCydrine = new ItemMineralDust(dustCydrineId).setBurnTime(1600).setItemName("dustCydrine").setIconCoord(2, 2);
 		dustMixed = new ItemMineralMixedDust(dustMixedId).setItemName("dustMixed").setIconCoord(3, 2);
 	}
 	
 	public static void addFuels() {
 		GameRegistry.registerFuelHandler(new MineralFuelHandler());
 	}
 
 	public static void addItemNames() {
 		LanguageRegistry.addName(miningHelmetLamp, "Mining Helmet Lamp");
 		LanguageRegistry.addName(miningHelmetIron, "Iron Mining Helmet");
 		LanguageRegistry.addName(miningHelmetGold, "Gold Mining Helmet");
 		LanguageRegistry.addName(miningHelmetDiamond, "Diamond Mining Helmet");
 		
 		LanguageRegistry.addName(motionSensor, "Motion Sensor");
 		
 		LanguageRegistry.addName(miningToolBelt, "Miner's ToolBelt");
 		
 		LanguageRegistry.addName(mineralAcxium, "Acxium");
 		LanguageRegistry.addName(mineralBisogen, "Bisogen");
 		LanguageRegistry.addName(mineralCydrine, "Cydrine");
 		
 		LanguageRegistry.addName(dustAcxium, "Acxium Dust");
 		LanguageRegistry.addName(dustBisogen, "Bisogen Dust");
 		LanguageRegistry.addName(dustCydrine, "Cydrine Dust");
 		LanguageRegistry.addName(dustMixed, "Mixed Dust");
 	}
 
 	public static void addItemRecipes() {
 		GameRegistry.addRecipe(
 				new ItemStack(miningHelmetIron),
 				new Object[] {
 					"X",
 					"Y",
 					Character.valueOf('X'),
 					miningHelmetLamp,
 					Character.valueOf('Y'),
 					Item.helmetSteel
 				}
 		);
 		GameRegistry.addRecipe(
 				new ItemStack(miningHelmetGold),
 				new Object[] {
 					"X",
 					"Y",
 					Character.valueOf('X'),
 					miningHelmetLamp,
 					Character.valueOf('Y'),
 					Item.helmetGold
 				}
 		);
 		GameRegistry.addRecipe(
 				new ItemStack(miningHelmetDiamond),
 				new Object[] {
 					"X",
 					"Y",
 					Character.valueOf('X'),
 					miningHelmetLamp,
 					Character.valueOf('Y'),
 					Item.helmetDiamond
 				}
 		);
 		GameRegistry.addRecipe(
 				new ItemStack(miningToolBelt),
 				new Object[] {
 					"X",
 					Character.valueOf('X'),
 					Block.dirt
 				}
 		);
 		
 		addDustRecipes();
 	}
 	private static void addDustRecipes() {
 		GameRegistry.addRecipe(
				new ItemStack(dustMixed).splitStack(2),
 				new Object[] {
 					"XY",
 					Character.valueOf('X'),
 					dustAcxium,
 					Character.valueOf('Y'),
 					dustBisogen
 				}
 		);
 		GameRegistry.addRecipe(
				new ItemStack(dustMixed).splitStack(2),
 				new Object[] {
 					"XY",
 					Character.valueOf('X'),
 					dustAcxium,
 					Character.valueOf('Y'),
 					dustCydrine
 				}
 		);
 		GameRegistry.addRecipe(
				new ItemStack(dustMixed).splitStack(2),
 				new Object[] {
 					"XY",
 					Character.valueOf('X'),
 					dustBisogen,
 					Character.valueOf('Y'),
 					dustCydrine
 				}
 		);
 		
 		GameRegistry.addRecipe(
				new ItemStack(dustMixed).splitStack(2),
 				new Object[] {
 					"XY",
 					Character.valueOf('X'),
 					dustMixed,
 					Character.valueOf('Y'),
 					dustAcxium
 				}
 		);
 		GameRegistry.addRecipe(
				new ItemStack(dustMixed).splitStack(2),
 				new Object[] {
 					"XY",
 					Character.valueOf('X'),
 					dustMixed,
 					Character.valueOf('Y'),
 					dustBisogen
 				}
 		);
 		GameRegistry.addRecipe(
				new ItemStack(dustMixed).splitStack(2),
 				new Object[] {
 					"XY",
 					Character.valueOf('X'),
 					dustMixed,
 					Character.valueOf('Y'),
 					dustCydrine
 				}
 		);
 		
 		GameRegistry.addRecipe(
				new ItemStack(dustMixed).splitStack(2),
 				new Object[] {
 					"XX",
 					Character.valueOf('X'),
 					dustMixed
 				}
 		);
 		
 		GameRegistry.registerCraftingHandler((ItemMineralMixedDust)dustMixed);
 	}
 	
 	// ORES
 	public static int
 		arkiteOreId,
 		bistiteOreId,
 		crokereOreId,
 		derniteOreId,
 		egioclaseOreId;
 	
 	public static Block
 		arkiteOre,
 		bistiteOre,
 		crokereOre,
 		derniteOre,
 		egioclaseOre;
 	
 	// MACHINES
 	
 	
 	// BLOCKS
 
 	// ======== BLOCK REGISTRATION ========
 	public static void registerBlocks() {
 		
 		BlockLib.init();
 		
 		// BlockTMFOre(int id, int spawnLevel, int spawnRate, int spawnSize, int veinSize, int lightLevel)
 		arkiteOre = new BlockTMFOre(arkiteOreId, 0, 60, 100, 5, 1).setBlockName("arkiteOre").setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setCreativeTab(CreativeTabs.tabBlock);
 		bistiteOre = new BlockTMFOre(bistiteOreId, 1, 36, 100, 5, 1).setBlockName("bistiteOre").setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setCreativeTab(CreativeTabs.tabBlock);;
 		crokereOre = new BlockTMFOre(crokereOreId, 2, 30, 100, 5, 1).setBlockName("crokereOre").setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setCreativeTab(CreativeTabs.tabBlock);;
 		derniteOre = new BlockTMFOre(derniteOreId, 3, 26, 100, 5, 1).setBlockName("derniteOre").setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setCreativeTab(CreativeTabs.tabBlock);;
 		egioclaseOre = new BlockTMFOre(egioclaseOreId, 4, 20, 100, 5, 1).setBlockName("egioclaseOre").setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setCreativeTab(CreativeTabs.tabBlock);;
 
 		GameRegistry.registerBlock(arkiteOre, "arkiteOre");
 		GameRegistry.registerBlock(bistiteOre, "bistiteOre");
 		GameRegistry.registerBlock(crokereOre, "crokereOre");
 		GameRegistry.registerBlock(derniteOre, "derniteOre");
 		GameRegistry.registerBlock(egioclaseOre, "egioclaseOre");
 
 		LanguageRegistry.addName(arkiteOre, NamingLib.ORE_ARKITE);
 		LanguageRegistry.addName(bistiteOre, NamingLib.ORE_BISTITE);
 		LanguageRegistry.addName(crokereOre, NamingLib.ORE_CROKERE);
 		LanguageRegistry.addName(derniteOre, NamingLib.ORE_DERNITE);
 		LanguageRegistry.addName(egioclaseOre, NamingLib.ORE_EGIOCLASE);
 	}
 }
