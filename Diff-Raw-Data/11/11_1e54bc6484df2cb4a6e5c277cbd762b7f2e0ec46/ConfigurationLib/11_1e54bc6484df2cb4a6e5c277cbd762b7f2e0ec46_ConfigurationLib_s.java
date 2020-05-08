 package slimevoid.tmf.core.lib;
 
 import net.minecraftforge.common.Configuration;
 import slimevoid.lib.data.Logger;
 import slimevoid.lib.util.XMLLoader;
 import slimevoid.lib.util.XMLRecipeLoader;
 import slimevoid.tmf.core.LoggerTMF;
 import slimevoid.tmf.core.TMFCore;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class ConfigurationLib {
 	
 	public static final int ITEMID_OFFSET = 256;
 	@SideOnly(Side.CLIENT)
 	public static int motionSensorMaxEntityDistance;
 	@SideOnly(Side.CLIENT)
 	public static int motionSensorMaxGameTicks;
 	@SideOnly(Side.CLIENT)
 	public static boolean motionSensorDrawRight;
 
 	@SideOnly(Side.CLIENT)
 	public static void ClientConfig() {
 		TMFCore.configuration.load();
 		
 		loadMotionSensorClient();
 		
 		TMFCore.configuration.save();
 	}
 	
 	public static void CommonConfig() {
 		TMFCore.configuration.load();
 		
 		loadLogger();
 		loadMotionSensorCommon();
 		loadMiningHelmet();
 		loadToolBelt();
 		loadMinerals();
 		loadDusts();
 		loadOres();
 		loadMachines();
 		
 		TMFCore.configuration.save();
 	}
 	
 	private static void loadMotionSensorClient() {
 		motionSensorDrawRight = Boolean.valueOf(TMFCore.configuration.get(
 				Configuration.CATEGORY_GENERAL,
 				"motionSensorDrawRight",
 				motionSensorDrawRight).value);
 		
 		motionSensorMaxEntityDistance = 20;
 		motionSensorMaxGameTicks = 40;
 		motionSensorDrawRight = true;
 	}
 	private static void loadMotionSensorCommon() {
 		TMFCore.motionSensorId = Integer.valueOf(TMFCore.configuration.get(
 				Configuration.CATEGORY_ITEM,
 				"motionSensor",
 				15003).value);
 		
		XMLLoader.addXmlVariable("$motionSensor", TMFCore.motionSensorId+256);
 	}
 	private static void loadMiningHelmet() {
 		TMFCore.miningHelmetIronId = Integer.valueOf(TMFCore.configuration.get(
 				Configuration.CATEGORY_ITEM,
 				"ironMinersHelmet",
 				15000).value);
 		TMFCore.miningHelmetGoldId = Integer.valueOf(TMFCore.configuration.get(
 				Configuration.CATEGORY_ITEM,
 				"goldMinersHelmet",
 				15001).value);
 		TMFCore.miningHelmetDiamondId = Integer.valueOf(TMFCore.configuration.get(
 				Configuration.CATEGORY_ITEM,
 				"diamondMinersHelmet",
 				15002).value);
 		TMFCore.miningHelmetLampId = Integer.valueOf(TMFCore.configuration.get(
 				Configuration.CATEGORY_ITEM,
 				"helmetLamp",
 				15004).value);
 		
		XMLLoader.addXmlVariable("$ironMinersHelmet",		TMFCore.miningHelmetIronId+256);
		XMLLoader.addXmlVariable("$goldMinersHelmet",		TMFCore.miningHelmetGoldId+256);
		XMLLoader.addXmlVariable("$diamondMinersHelmet",	TMFCore.miningHelmetDiamondId+256);
		XMLLoader.addXmlVariable("$helmetLamp", 			TMFCore.miningHelmetLampId+256);
 	}
 	private static void loadToolBelt() {
 		TMFCore.miningToolBeltId = Integer.valueOf(TMFCore.configuration.get(
 				Configuration.CATEGORY_ITEM,
 				"toolBelt",
 				15005).value);
 		
 		XMLLoader.addXmlVariable("$toolBelt", TMFCore.miningToolBeltId);
 	}
 	private static void loadMinerals() {
 		TMFCore.mineralAcxiumId = Integer.valueOf(TMFCore.configuration.get(
 				Configuration.CATEGORY_ITEM,
 				"mineralAcxium",
 				15010).value);
 		TMFCore.mineralBisogenId = Integer.valueOf(TMFCore.configuration.get(
 				Configuration.CATEGORY_ITEM,
 				"mineralBisogen",
 				15011).value);
 		TMFCore.mineralCydrineId = Integer.valueOf(TMFCore.configuration.get(
 				Configuration.CATEGORY_ITEM,
 				"mineralCydrine",
 				15012).value);
 		
 		XMLLoader.addXmlVariable("$mineralAcxium", 		TMFCore.mineralAcxiumId+256);
 		XMLLoader.addXmlVariable("$mineralBisogen", 	TMFCore.mineralBisogenId+256);
 		XMLLoader.addXmlVariable("$mineralCydrine", 	TMFCore.mineralCydrineId+256);
 	}
 	private static void loadDusts() {
 		TMFCore.dustAcxiumId = Integer.valueOf(TMFCore.configuration.get(
 				Configuration.CATEGORY_ITEM,
 				"dustAcxium",
 				15020).value);
 		TMFCore.dustBisogenId = Integer.valueOf(TMFCore.configuration.get(
 				Configuration.CATEGORY_ITEM,
 				"dustBisogen",
 				15021).value);
 		TMFCore.dustCydrineId = Integer.valueOf(TMFCore.configuration.get(
 				Configuration.CATEGORY_ITEM,
 				"dustCydrine",
 				15022).value);
 		TMFCore.dustMixedId = Integer.valueOf(TMFCore.configuration.get(
 				Configuration.CATEGORY_ITEM,
 				"dustMixed",
 				15023).value);
 		
 		XMLLoader.addXmlVariable("$dustAcxium", 	TMFCore.dustAcxiumId+256);
 		XMLLoader.addXmlVariable("$dustBisogen", 	TMFCore.dustBisogenId+256);
 		XMLLoader.addXmlVariable("$dustCydrine", 	TMFCore.dustCydrineId+256);
 		XMLLoader.addXmlVariable("$dustMixed", 		TMFCore.dustMixedId+256);
 	}
 	private static void loadOres() {
 		TMFCore.arkiteOreId = Integer.valueOf(TMFCore.configuration.get(
 				Configuration.CATEGORY_BLOCK,
 				"arkiteOre",
 				1025).value);
 		TMFCore.bistiteOreId = Integer.valueOf(TMFCore.configuration.get(
 				Configuration.CATEGORY_BLOCK,
 				"bistiteOre",
 				1026).value);
 		TMFCore.crokereOreId = Integer.valueOf(TMFCore.configuration.get(
 				Configuration.CATEGORY_BLOCK,
 				"crokereOre",
 				1027).value);
 		TMFCore.derniteOreId = Integer.valueOf(TMFCore.configuration.get(
 				Configuration.CATEGORY_BLOCK,
 				"derniteOre",
 				1028).value);
 		TMFCore.egioclaseOreId = Integer.valueOf(TMFCore.configuration.get(
 				Configuration.CATEGORY_BLOCK,
 				"egioclaseOre",
 				1029).value);
 		
 		XMLLoader.addXmlVariable("$arkiteOre", 		TMFCore.arkiteOreId);
 		XMLLoader.addXmlVariable("$bistiteOre", 	TMFCore.bistiteOreId);
 		XMLLoader.addXmlVariable("$crokereOre", 	TMFCore.crokereOreId);
 		XMLLoader.addXmlVariable("$derniteOre", 	TMFCore.derniteOreId);
 		XMLLoader.addXmlVariable("$egioclaseOre", 	TMFCore.egioclaseOreId);
 	}
 	private static void loadMachines() {
 		TMFCore.refineryIdleId = Integer.valueOf(TMFCore.configuration.get(
 				Configuration.CATEGORY_BLOCK,
 				"refineryIdle",
 				1100).value);
 		TMFCore.refineryActiveId = Integer.valueOf(TMFCore.configuration.get(
 				Configuration.CATEGORY_BLOCK,
 				"refineryActive",
 				1101).value);
 		XMLLoader.addXmlVariable("$refineryIdle", 		TMFCore.refineryIdleId);
 		XMLLoader.addXmlVariable("$refineryActive", 	TMFCore.refineryActiveId);
 		
 		TMFCore.grinderIdleId = Integer.valueOf(TMFCore.configuration.get(
 				Configuration.CATEGORY_BLOCK,
 				"grinderIdle",
 				1102).value);
 		TMFCore.grinderActiveId = Integer.valueOf(TMFCore.configuration.get(
 				Configuration.CATEGORY_BLOCK,
 				"grinderActive",
 				1103).value);
 		XMLLoader.addXmlVariable("$grinderIdle", 		TMFCore.grinderIdleId);
 		XMLLoader.addXmlVariable("$grinderActive", 		TMFCore.grinderActiveId);
 		
 		TMFCore.geoEquipIdleId = Integer.valueOf(TMFCore.configuration.get(
 				Configuration.CATEGORY_BLOCK,
 				"geoEquipIdle",
 				1104).value);
 		TMFCore.geoEquipActiveId = Integer.valueOf(TMFCore.configuration.get(
 				Configuration.CATEGORY_BLOCK,
 				"geoEquipActive",
 				1105).value);
 		XMLLoader.addXmlVariable("$geoEquipIdle", 		TMFCore.geoEquipIdleId);
 		XMLLoader.addXmlVariable("$geoEquipActive", 	TMFCore.geoEquipActiveId);
 		
 	}
 	private static void loadLogger() {
 		TMFCore.loggerLevel = String.valueOf(TMFCore.configuration.get(
 				Configuration.CATEGORY_GENERAL,
 				"loggerLevel",
 				TMFCore.loggerLevel).value);
 		
 		LoggerTMF.getInstance(
 				Logger.filterClassName(
 						TMFCore.class.toString()
 				)
 		).setFilterLevel(
 				TMFCore.loggerLevel
 		);
 	}
 }
