 /*
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version. This program is distributed in the hope that it will be
  * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
  * Public License for more details. You should have received a copy of the GNU
  * Lesser General Public License along with this program. If not, see
  * <http://www.gnu.org/licenses/>
  */
 package slimevoid.tmf.core;
 
 import java.io.File;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.EnumArmorMaterial;
 import net.minecraft.item.Item;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.MinecraftForge;
 import slimevoid.tmf.blocks.ores.BlockTMFOre;
 import slimevoid.tmf.core.creativetabs.CreativeTabTMF;
 import slimevoid.tmf.core.lib.BlockLib;
 import slimevoid.tmf.core.lib.EnumMachine;
 import slimevoid.tmf.core.lib.ItemLib;
 import slimevoid.tmf.core.lib.LocalizationLib;
 import slimevoid.tmf.core.lib.ResourceLib;
 import slimevoid.tmf.core.world.WorldGeneration;
 import slimevoid.tmf.fuel.MineralFuelHandler;
 import slimevoid.tmf.fuel.MixedDustNameRegistry;
 import slimevoid.tmf.machines.JSONGrinderRecipesLoader;
 import slimevoid.tmf.machines.JSONRefineryRecipesLoader;
 import slimevoid.tmf.machines.blocks.BlockMachineBase;
 import slimevoid.tmf.minerals.items.ItemMineral;
 import slimevoid.tmf.minerals.items.ItemMineralDust;
 import slimevoid.tmf.minerals.items.ItemMineralMixedDust;
 import slimevoid.tmf.minerals.items.ItemMineralMixedDustRecipe;
 import slimevoid.tmf.tools.items.ItemMiningHelmet;
 import slimevoid.tmf.tools.items.ItemMiningLamp;
 import slimevoid.tmf.tools.items.ItemMiningToolBelt;
 import slimevoid.tmf.tools.items.ItemMotionSensor;
 import slimevoidlib.blocks.BlockBase;
 import slimevoidlib.items.ItemBlockBase;
 import slimevoidlib.util.FileReader;
 import slimevoidlib.util.xml.XMLRecipeLoader;
 import cpw.mods.fml.common.registry.GameRegistry;
 
 public class TMFCore {
 
 	// CONFIG
 	public static Configuration	configuration;
 
 	public static String		loggerLevel	= "INFO";
 
 	// TOOLS
 	public static int			miningHelmetLampId, miningHelmetIronId,
 			miningHelmetGoldId, miningHelmetDiamondId;
 	public static Item			miningHelmetLamp, miningHelmetIron,
 			miningHelmetGold, miningHelmetDiamond;
 
 	public static Item			motionSensor;
 	public static int			motionSensorId;
 
 	public static Item			miningToolBelt;
 	public static int			miningToolBeltId;
 
 	// MINERALS
 	public static Item			mineralAcxium, mineralBisogen, mineralCydrine;
 	public static int			mineralAcxiumId, mineralBisogenId,
 			mineralCydrineId;
 
 	// MINERAL DUSTS
 	public static Item			dustAcxium, dustBisogen, dustCydrine,
 			dustMixed;
 	public static int			dustAcxiumId, dustBisogenId, dustCydrineId,
 			dustMixedId;
 
 	// ======== ITEM REGISTRATION ========
 	public static void registerItems() {
 		registerTools();
 		registerMinerals();
 		registerDusts();
 	}
 
 	private static void registerTools() {
 		registerMiningHelmets();
 		registerMotionSensor();
 		registerToolBelt();
 	}
 
 	private static void registerMiningHelmets() {
		miningHelmetLamp = new ItemMiningLamp(miningHelmetLampId).setUnlocalizedName(ItemLib.MINING_HELMET_LAMP);
		miningHelmetIron = new ItemMiningHelmet(miningHelmetIronId, EnumArmorMaterial.IRON, 2, 0).setUnlocalizedName(ItemLib.MINING_HELMET_IRON);
		miningHelmetGold = new ItemMiningHelmet(miningHelmetGoldId, EnumArmorMaterial.GOLD, 4, 0).setUnlocalizedName(ItemLib.MINING_HELMET_GOLD);
		miningHelmetDiamond = new ItemMiningHelmet(miningHelmetDiamondId, EnumArmorMaterial.DIAMOND, 3, 0).setUnlocalizedName(ItemLib.MINING_HELMET_DIAMOND);
 	}
 
 	private static void registerMotionSensor() {
 		motionSensor = new ItemMotionSensor(motionSensorId).setUnlocalizedName(ItemLib.MOTION_SENSOR).setTextureName(ResourceLib.MOTION_SENSOR);
 	}
 
 	private static void registerToolBelt() {
 		miningToolBelt = new ItemMiningToolBelt(miningToolBeltId).setUnlocalizedName(ItemLib.MINING_TOOLBELT).setTextureName(ResourceLib.MINING_TOOLBELT);
 	}
 
 	private static void registerMinerals() {
 		mineralAcxium = new ItemMineral(mineralAcxiumId).setBurnTime(2400).setUnlocalizedName(ItemLib.MINERAL_AXCIUM).setTextureName(ResourceLib.MINERAL_AXCIUM);
 		mineralBisogen = new ItemMineral(mineralBisogenId).setBurnSpeed(150).setUnlocalizedName(ItemLib.MINERAL_BISOGEN).setTextureName(ResourceLib.MINERAL_BISOGEN);
 		mineralCydrine = new ItemMineral(mineralCydrineId).setBurnWidth(1).setUnlocalizedName(ItemLib.MINERAL_CYDRINE).setTextureName(ResourceLib.MINERAL_CYDRINE);
 	}
 
 	private static void registerDusts() {
 		dustAcxium = new ItemMineralDust(dustAcxiumId).setBurnTime(3200).setUnlocalizedName(ItemLib.DUST_AXCIUM).setTextureName(ResourceLib.DUST_AXCIUM);
 		dustBisogen = new ItemMineralDust(dustBisogenId).setBurnSpeed(100).setUnlocalizedName(ItemLib.DUST_BISOGEN).setTextureName(ResourceLib.DUST_BISOGEN);
 		dustCydrine = new ItemMineralDust(dustCydrineId).setBurnWidth(1).setUnlocalizedName(ItemLib.DUST_CYDRINE).setTextureName(ResourceLib.DUST_CYDRINE);
 		dustMixed = new ItemMineralMixedDust(dustMixedId).setUnlocalizedName(ItemLib.DUST_MIXED).setTextureName(ResourceLib.DUST_MIXED);
 
 		ItemMineralMixedDust.script = FileReader.readFile(new File(TMFCore.class.getResource(ResourceLib.DUST_LIB).getFile()));
 		ItemMineralMixedDust.script_burnTime = "getBurnTime()";
 		ItemMineralMixedDust.script_burnSpeed = "getBurnSpeed()";
 		ItemMineralMixedDust.script_burnWidth = "getBurnWidth()";
 
 		// TODO :: MixedDustNameRegistry : Move to or something
 		MixedDustNameRegistry.addName(	1,
 										1,
 										0,
 										"Monoaxogen");
 		MixedDustNameRegistry.addName(	1,
 										0,
 										1,
 										"Monoaxicyde");
 		MixedDustNameRegistry.addName(	0,
 										1,
 										1,
 										"Monogencyde");
 	}
 
 	// ======== FUEL REGISTRATION ========
 	public static void registerFuels() {
 		GameRegistry.registerFuelHandler(new MineralFuelHandler());
 	}
 
 	// ORES
 	public static int		arkiteOreId, bistiteOreId, crokereOreId,
 			derniteOreId, egioclaseOreId;
 	public static Block		arkiteOre, bistiteOre, crokereOre, derniteOre,
 			egioclaseOre;
 
 	// MACHINES
 	public static int		blockMachineBaseId;
 	public static BlockBase	blockMachineBase;
 
 	// ======== BLOCK REGISTRATION ========
 	public static void registerBlocks() {
 
 		BlockLib.init();
 
 		registerOres();
 
 		GameRegistry.registerWorldGenerator(new WorldGeneration());
 
 		registerMachines();
 	}
 
 	private static void registerOres() {
 		// BlockTMFOre(int id, int texture, int spawnLevel, int spawnRate, int
 		// spawnSize, int veinSize, int lightLevel)
 		arkiteOre = new BlockTMFOre(arkiteOreId, 60, 100, 5, 0.2F).setUnlocalizedName(BlockLib.ORE_ARKITE).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setCreativeTab(CreativeTabTMF.tabTMF);
 		bistiteOre = new BlockTMFOre(bistiteOreId, 36, 100, 5, 0.3F).setUnlocalizedName(BlockLib.ORE_BISTITE).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setCreativeTab(CreativeTabTMF.tabTMF);
 		crokereOre = new BlockTMFOre(crokereOreId, 30, 100, 5, 0.4F).setUnlocalizedName(BlockLib.ORE_CROKERE).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setCreativeTab(CreativeTabTMF.tabTMF);
 		derniteOre = new BlockTMFOre(derniteOreId, 26, 100, 5, 0.5F).setUnlocalizedName(BlockLib.ORE_DERNITE).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setCreativeTab(CreativeTabTMF.tabTMF);
 		egioclaseOre = new BlockTMFOre(egioclaseOreId, 20, 100, 5, 0.6F).setUnlocalizedName(BlockLib.ORE_EGIOCLASE).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setCreativeTab(CreativeTabTMF.tabTMF);
 
 		GameRegistry.registerBlock(	arkiteOre,
 									BlockLib.ORE_ARKITE);
 		GameRegistry.registerBlock(	bistiteOre,
 									BlockLib.ORE_BISTITE);
 		GameRegistry.registerBlock(	crokereOre,
 									BlockLib.ORE_CROKERE);
 		GameRegistry.registerBlock(	derniteOre,
 									BlockLib.ORE_DERNITE);
 		GameRegistry.registerBlock(	egioclaseOre,
 									BlockLib.ORE_EGIOCLASE);
 
 		MinecraftForge.setBlockHarvestLevel(arkiteOre,
 											"pickaxe",
 											2);
 		MinecraftForge.setBlockHarvestLevel(bistiteOre,
 											"pickaxe",
 											2);
 		MinecraftForge.setBlockHarvestLevel(crokereOre,
 											"pickaxe",
 											2);
 		MinecraftForge.setBlockHarvestLevel(derniteOre,
 											"pickaxe",
 											2);
 		MinecraftForge.setBlockHarvestLevel(egioclaseOre,
 											"pickaxe",
 											2);
 	}
 
 	private static void registerMachines() {
 		// MACHINE BASE
 		blockMachineBase = new BlockMachineBase(blockMachineBaseId);
 		GameRegistry.registerBlock(	blockMachineBase,
 									ItemBlockBase.class,
 									BlockLib.BLOCK_MACHINE_BASE);
 
 		EnumMachine.registerMachines();
 
 		// REFINERY
 		JSONRefineryRecipesLoader.loadFile(new File(TMFCore.class.getResource(ResourceLib.RECIPES_REFINERY).getFile()));
 
 		// GRINDER
 		JSONGrinderRecipesLoader.loadFile(new File(TMFCore.class.getResource(ResourceLib.RECIPES_GRINDER).getFile()));
 
 		// GEOLOGICAL EQUIPMENT
 
 		// AUTOMATIC MIXING TABLE
 	}
 
 	// ======== NAME REGISTRATION ========
 	public static void registerNames() {
 		LocalizationLib.registerLanguages();
 	}
 
 	// ======= RECIPE REGISTRATION =======
 	public static void registerRecipes() {
 		XMLRecipeLoader.loadDefaults(new File(TMFCore.class.getResource(ResourceLib.RECIPE_PATH).getFile()));
 		XMLRecipeLoader.loadFolder(new File(TheMinersFriend.proxy.getMinecraftDir()
 											+ "/config/TMFRecipes"));
 
 		GameRegistry.addRecipe(new ItemMineralMixedDustRecipe());
 	}
 }
