 package gwydion0917.gwycraft;
 
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.Property;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 
 public class ConfigGwycraft {
 	// Glow blocks start at 1800 (100 less than standard)
 	public static int glowyWoolID = 1801;
 	public static int glowydyedStoneID = 1802;
 	// public static int glowydyedStoneSlab1ID = 1803;
 	// public static int glowydyedStoneDoubleSlab1ID = 1804;
 	// public static int glowydyedStoneSlab2ID = 1805;
 	// public static int glowydyedStoneDoubleSlab2ID = 1806;
 	// public static int glowydyedBookcaseID = 1807;
 	public static int glowydyedBrickID = 1808;
 	public static int glowydyedClayblockID = 1809;
 	public static int glowydyedGlassID = 1810;
 	// public static int glowydyedLeafID = 1811;
 	// public static int glowydyedLog1ID = 1812;
 	// public static int glowydyedLog2ID = 1813;
 	// public static int glowydyedLog3ID = 1814;
 	// public static int glowydyedLog4ID = 1815;
 	public static int glowyBlockDyedMudBrickID = 1816;
 	public static int glowydyedPlankID = 1817;
 	public static int glowydyedSandID = 1818;
 	public static int glowydyedStonebrickID = 1819;
 	public static int glowydyedStonecobbleID = 1820;
	// public static int glowydyedMudbrickStairsID = 1821;
	// public static int glowyblockGemOreID = 1822;
	// public static int glowyblockGemCompressedID = 1823;
 	// public static int glowyblockDyedStonePaver1ID = 1824;
 	// public static int glowyblockDyedStonePaver2ID = 1825;
 	// public static int glowyblockDyedStoneBrickSlab1ID = 1826;
 	// public static int glowyblockDyedStoneBrickDoubleSlab1ID = 1827;
 	// public static int glowyblockDyedStoneBrickSlab2ID = 1828;
 	// public static int glowyblockDyedStoneBrickDoubleSlab2D = 1829;
 	// public static int glowyblockDyedSandstoneID = 1830;
 	// public static int glowyblockDyedSandstoneChiseledID = 1831;
 	// public static int glowyblockDyedSandstoneSmoothID = 1832;
 	// public static int glowyblockDyedCobbleSlab1ID = 1833;
 	// public static int glowyblockDyedCobbleDoubleSlab1ID = 1834;
 	// public static int glowyblockDyedCobbleSlab2ID = 1835;
 	// public static int glowyblockDyedCobbleDoubleSlab2D = 1836;
 	// public static int glowyblockDyedStoneBrickSlab1ID = 1837;
 	// public static int glowyblockDyedStoneBrickDoubleSlab1ID = 1838;
 	// public static int glowyblockDyedStoneBrickSlab2ID = 1839;
 	// public static int glowyblockDyedStoneBrickDoubleSlab2D = 1840;
 	// public static int glowyblockDyedLogSlab1ID = 1841;
 	// public static int glowyblockDyedLogDoubleSlab1ID = 1842;
 	// public static int glowyblockDyedLogSlab2ID = 1843;
 	// public static int glowyblockDyedLogDoubleSlab2D = 1844;
 	// public static int glowyblockDyedMudBrickSlab1ID = 1845;
 	// public static int glowyblockDyedMudBrickDoubleSlab1ID = 1846;
 	// public static int glowyblockDyedMudBrickSlab2ID = 1847;
 	// public static int glowyblockDyedMudBrickDoubleSlab2D = 1848;
 	// public static int glowyblockDyedPlankSlab1ID = 1849;
 	// public static int glowyblockDyedPlankDoubleSlab1ID = 1850;
 	// public static int glowyblockDyedPlankSlab2ID = 1851;
 	// public static int glowyblockDyedPlankDoubleSlab2D = 1852;
 	// public static int glowyblockGemCompressedSlab1ID = 1853;
 	// public static int glowyblockGemCompressedDoubleSlab1ID = 1854;
 	// public static int glowyblockGemCompressedSlab2ID = 1855;
 	// public static int glowyblockGemCompressedDoubleSlab2D = 1856;
 	// public static int glowyblockGlowyWoolSlab1ID = 1857;
 	// public static int glowyblockGlowyWoolDoubleSlab1ID = 1858;
 	// public static int glowyblockGlowyWoolSlab2ID = 1859;
 	// public static int glowyblockGlowyWoolDoubleSlab2D = 1860;
 	// public static int glowyblockDyedFencesID = 1861;
 	// public static int glowyblockDyedCobbleWallsID = 1862;
 
 	// Standard (non-glow) Blocks
 	public static int dyedStoneID = 1902;
 	public static int dyedStoneSlab1ID = 1903;
 	public static int dyedStoneDoubleSlab1ID = 1904;
 	public static int dyedStoneSlab2ID = 1905;
 	public static int dyedStoneDoubleSlab2ID = 1906;
 	public static int dyedBookcaseID = 1907;
 	public static int dyedBrickID = 1908;
 	public static int dyedClayblockID = 1909;
 	public static int dyedGlassID = 1910;
 	public static int dyedLeafID = 1911;
 	public static int dyedLog1ID = 1912;
 	public static int dyedLog2ID = 1913;
 	public static int dyedLog3ID = 1914;
 	public static int dyedLog4ID = 1915;
 	public static int blockDyedMudBrickID = 1916;
 	public static int dyedPlankID = 1917;
 	public static int dyedSandID = 1918;
 	public static int dyedStonebrickID = 1919;
 	public static int dyedStonecobbleID = 1920;
 	public static int dyedMudbrickStairsID = 1921;
 	public static int blockGemOreID = 1922;
 	public static int blockGemCompressedID = 1923;
 	public static int blockDyedStonePaver1ID = 1924;
 	public static int blockDyedStonePaver2ID = 1925;
 	public static int blockDyedStoneBrickSlab1ID = 1926;
 	public static int blockDyedStoneBrickDoubleSlab1ID = 1927;
 	public static int blockDyedStoneBrickSlab2ID = 1928;
 	public static int blockDyedStoneBrickDoubleSlab2ID = 1929;
 	// public static int blockDyedSandstoneID = 1930;
 	// public static int blockDyedSandstoneChiseledID = 1931;
 	// public static int blockDyedSandstoneSmoothID = 1932;
 	// public static int blockDyedCobbleSlab1ID = 1933;
 	// public static int blockDyedCobbleDoubleSlab1ID = 1934;
 	// public static int blockDyedCobbleSlab2ID = 1935;
 	// public static int blockDyedCobbleDoubleSlab2D = 1936;
 	// public static int blockDyedBrickSlab1ID = 1937;
 	// public static int blockDyedBrickDoubleSlab1ID = 1938;
 	// public static int blockDyedBrickSlab2ID = 1939;
 	// public static int blockDyedBrickDoubleSlab2D = 1940;
 	// public static int blockDyedLogSlab1ID = 1941;
 	// public static int blockDyedLogDoubleSlab1ID = 1942;
 	// public static int blockDyedLogSlab2ID = 1943;
 	// public static int blockDyedLogDoubleSlab2D = 1944;
 	// public static int blockDyedMudBrickSlab1ID = 1945;
 	// public static int blockDyedMudBrickDoubleSlab1ID = 1946;
 	// public static int blockDyedMudBrickSlab2ID = 1947;
 	// public static int blockDyedMudBrickDoubleSlab2D = 1948;
 	// public static int blockDyedPlankSlab1ID = 1949;
 	// public static int blockDyedPlankDoubleSlab1ID = 1950;
 	// public static int blockDyedPlankSlab2ID = 1951;
 	// public static int blockDyedPlankDoubleSlab2D = 1952;
 	// public static int blockGemCompressedSlab1ID = 1953;
 	// public static int blockGemCompressedDoubleSlab1ID = 1954;
 	// public static int blockGemCompressedSlab2ID = 1955;
 	// public static int blockGemCompressedDoubleSlab2D = 1956;
 	// public static int blockGlowyWoolSlab1ID = 1957;
 	// public static int blockGlowyWoolDoubleSlab1ID = 1958;
 	// public static int blockGlowyWoolSlab2ID = 1959;
 	// public static int blockGlowyWoolDoubleSlab2D = 1960;
 	public static int blockDyedFencesID = 1961;
 	public static int blockDyedCobbleWallsID = 1962;
 	public static int blockWhiteDyedTorchID = 1963;
 	public static int blockOrangeDyedTorchID = 1964;
 	public static int blockMagentaDyedTorchID = 1965;
 	public static int blockLBlueDyedTorchID = 1966;
 	public static int blockYellowDyedTorchID = 1967;
 	public static int blockLGreenDyedTorchID = 1968;
 	public static int blockPinkDyedTorchID = 1969;
 	public static int blockDGrayDyedTorchID = 1970;
 	public static int blockLGrayDyedTorchID = 1971;
 	public static int blockCyanDyedTorchID = 1972;
 	public static int blockPurpleDyedTorchID = 1973;
 	public static int blockBlueDyedTorchID = 1974;
 	public static int blockBrownDyedTorchID = 1975;
 	public static int blockGreenDyedTorchID = 1976;
 	public static int blockRedDyedTorchID = 1977;
 	public static int blockBlackDyedTorchID = 1978;
 
 	public static int itemFlawedGemsID = 5001;
 	public static int itemDyedClayID = 5002;
 	public static int itemDyedClayBricksID = 5003;
 	public static int itemDyedMudID = 5004;
 	public static int itemDyedMudBricksID = 5005;
 	public static int itemDyedSticksID = 5006;
 
 	// public static int itemIronSawBladeID = 5007;
 	// public static int itemDiamondSawBladeID = 5008;
 	// public static int itemIronRouterTipID = 5009;
 	// public static int itemDiamondRouterTipID = 5010;
 	// public static int itemDyedFuel1ID = 5011;
 	// public static int itemDyedFuel2ID = 5012;
 	// public static int itemDyedFuel3ID = 5013;
 	// public static int itemDyedFuel4ID = 5014;
 	// public static int itemDyedFuel5ID = 5015;
 	// public static int itemDyedFuel6ID = 5016;
 	// public static int itemDyedFuel7ID = 5017;
 	// public static int itemDyedFuel8ID = 5018;
 	// public static int itemDyedFuel9ID = 5019;
 	// public static int itemDyedFuel10ID = 5020;
 	// public static int itemDyedFuel11ID = 5021;
 	// public static int itemDyedFuel12ID = 5022;
 	// public static int itemDyedFuel13ID = 5023;
 	// public static int itemDyedFuel14ID = 5024;
 	// public static int itemDyedFuel15ID = 5025;
 	// public static int itemDyedFuel16ID = 5026;
     public static int itemGemShearsID = 5011;
     // public static int itemPick2ID = 5012;
     // public static int itemPick3ID = 5013;
     // public static int itemPick4ID = 5014;
     // public static int itemPick5ID = 5015;
     // public static int itemPick6ID = 5016;
     // public static int itemPick7ID = 5017;
     // public static int itemPick8ID = 5018;
     // public static int itemPick9ID = 5019;
     // public static int itemPick10ID = 5020;
     // public static int itemPick11ID = 5021;
     // public static int itemPick12ID = 5022;
     // public static int itemPick13ID = 5023;
     // public static int itemPick14ID = 5024;
     // public static int itemPick15ID = 5025;
     // public static int itemPick16ID = 5026;
     // public static int itemShovel1ID = 5011;
     // public static int itemShovel2ID = 5012;
     // public static int itemShovel3ID = 5013;
     // public static int itemShovel4ID = 5014;
     // public static int itemShovel5ID = 5015;
     // public static int itemShovel6ID = 5016;
     // public static int itemShovel7ID = 5017;
     // public static int itemShovel8ID = 5018;
     // public static int itemShovel9ID = 5019;
     // public static int itemShovel10ID = 5020;
     // public static int itemShovel11ID = 5021;
     // public static int itemShovel12ID = 5022;
     // public static int itemShovel13ID = 5023;
     // public static int itemShovel14ID = 5024;
     // public static int itemShovel15ID = 5025;
     // public static int itemShovel16ID = 5026;
     // public static int itemAxe1ID = 5011;
     // public static int itemAxe2ID = 5012;
     // public static int itemAxe3ID = 5013;
     // public static int itemAxe4ID = 5014;
     // public static int itemAxe5ID = 5015;
     // public static int itemAxe6ID = 5016;
     // public static int itemAxe7ID = 5017;
     // public static int itemAxe8ID = 5018;
     // public static int itemAxe9ID = 5019;
     // public static int itemAxe10ID = 5020;
     // public static int itemAxe11ID = 5021;
     // public static int itemAxe12ID = 5022;
     // public static int itemAxe13ID = 5023;
     // public static int itemAxe14ID = 5024;
     // public static int itemAxe15ID = 5025;
     // public static int itemAxe16ID = 5026;
     // public static int itemHoe1ID = 5011;
     // public static int itemHoe2ID = 5012;
     // public static int itemHoe3ID = 5013;
     // public static int itemHoe4ID = 5014;
     // public static int itemHoe5ID = 5015;
     // public static int itemHoe6ID = 5016;
     // public static int itemHoe7ID = 5017;
     // public static int itemHoe8ID = 5018;
     // public static int itemHoe9ID = 5019;
     // public static int itemHoe10ID = 5020;
     // public static int itemHoe11ID = 5021;
     // public static int itemHoe12ID = 5022;
     // public static int itemHoe13ID = 5023;
     // public static int itemHoe14ID = 5024;
     // public static int itemHoe15ID = 5025;
     // public static int itemHoe16ID = 5026;
     // public static int itemSword1ID = 5011;
     // public static int itemSword2ID = 5012;
     // public static int itemSword3ID = 5013;
     // public static int itemSword4ID = 5014;
     // public static int itemSword5ID = 5015;
     // public static int itemSword6ID = 5016;
     // public static int itemSword7ID = 5017;
     // public static int itemSword8ID = 5018;
     // public static int itemSword9ID = 5019;
     // public static int itemSword10ID = 5020;
     // public static int itemSword11ID = 5021;
     // public static int itemSword12ID = 5022;
     // public static int itemSword13ID = 5023;
     // public static int itemSword14ID = 5024;
     // public static int itemSword15ID = 5025;
     // public static int itemSword16ID = 5026;
 	
 	// General Settings
 	public static boolean genGemsEnabled = true;
 	public static int genGemsVeins = 8;
 	public static int genGemsNumber = 4;
 
 	public static void initConfig(FMLPreInitializationEvent event) {
 
 		Configuration config = new Configuration(
 				event.getSuggestedConfigurationFile());
 		config.load();
 
 		glowyWoolID = config.getBlock("Glow Wool", glowyWoolID).getInt();
 		dyedStoneID = config.getBlock("dyedStone", dyedStoneID).getInt();
 		glowydyedStoneID = config.getBlock("glowydyedStone", glowydyedStoneID)
 				.getInt();
 		dyedStoneSlab1ID = config.getBlock("dyedStoneSlab1", dyedStoneSlab1ID)
 				.getInt();
 		dyedStoneSlab2ID = config.getBlock("dyedStoneSlab2", dyedStoneSlab2ID)
 				.getInt();
 		dyedBookcaseID = config.getBlock("dyedBookcase", dyedBookcaseID)
 				.getInt();
 		dyedBrickID = config.getBlock("dyedBrick", dyedBrickID).getInt();
 		glowydyedBrickID = config.getBlock("glowydyedBrick", glowydyedBrickID)
 				.getInt();
 		dyedClayblockID = config.getBlock("dyedClayblock", dyedClayblockID)
 				.getInt();
 		glowydyedClayblockID = config.getBlock("glowydyedClayblock",
 				glowydyedClayblockID).getInt();
 		dyedGlassID = config.getBlock("dyedGlass", dyedGlassID).getInt();
         glowydyedGlassID = config.getBlock("glowydyedGlass", glowydyedGlassID).getInt();
 		dyedLeafID = config.getBlock("dyedLeaf", dyedLeafID).getInt();
 		blockDyedMudBrickID = config.getBlock("blockDyedMudBrick",
 				blockDyedMudBrickID).getInt();
 		glowyBlockDyedMudBrickID = config.getBlock("glowyblockDyedMudBrick",
 				glowyBlockDyedMudBrickID).getInt();
 		dyedPlankID = config.getBlock("dyedPlank", dyedPlankID).getInt();
 		glowydyedPlankID = config.getBlock("glowydyedPlank", glowydyedPlankID)
 				.getInt();
 		dyedSandID = config.getBlock("dyedSand", dyedSandID).getInt();
 		glowydyedSandID = config.getBlock("glowydyedSand", glowydyedSandID)
 				.getInt();
 		dyedStonebrickID = config.getBlock("dyedStonebrick", dyedStonebrickID)
 				.getInt();
 		glowydyedStonebrickID = config.getBlock("glowydyedStonebrick",
 				glowydyedStonebrickID).getInt();
 		dyedStonecobbleID = config.getBlock("dyedStonecobble",
 				dyedStonecobbleID).getInt();
 		glowydyedStonecobbleID = config.getBlock("glowydyedStonecobble",
 				glowydyedStonecobbleID).getInt();
 		dyedMudbrickStairsID = config.getBlock("dyedMudbrickStairs",
 				dyedMudbrickStairsID).getInt();
 		blockWhiteDyedTorchID = config.getBlock("blockWhiteDyedTorch",
 				blockWhiteDyedTorchID).getInt();
 		blockOrangeDyedTorchID = config.getBlock("blockOrangeDyedTorch",
 				blockOrangeDyedTorchID).getInt();
 		blockMagentaDyedTorchID = config.getBlock("blockMagentaDyedTorch",
 				blockMagentaDyedTorchID).getInt();
 		blockLBlueDyedTorchID = config.getBlock("blockLBlueDyedTorch",
 				blockLBlueDyedTorchID).getInt();
 		blockYellowDyedTorchID = config.getBlock("blockYellowDyedTorch",
 				blockYellowDyedTorchID).getInt();
 		blockLGreenDyedTorchID = config.getBlock("blockLGreenDyedTorch",
 				blockLGreenDyedTorchID).getInt();
 		blockPinkDyedTorchID = config.getBlock("blockPinkDyedTorch",
 				blockPinkDyedTorchID).getInt();
 		blockDGrayDyedTorchID = config.getBlock("blockDGrayDyedTorch",
 				blockDGrayDyedTorchID).getInt();
 		blockLGrayDyedTorchID = config.getBlock("blockLGrayDyedTorch",
 				blockLGrayDyedTorchID).getInt();
 		blockCyanDyedTorchID = config.getBlock("blockCyanDyedTorch",
 				blockCyanDyedTorchID).getInt();
 		blockPurpleDyedTorchID = config.getBlock("blockPurpleDyedTorch",
 				blockPurpleDyedTorchID).getInt();
 		blockBlueDyedTorchID = config.getBlock("blockBlueDyedTorch",
 				blockBlueDyedTorchID).getInt();
 		blockBrownDyedTorchID = config.getBlock("blockBrownDyedTorch",
 				blockBrownDyedTorchID).getInt();
 		blockGreenDyedTorchID = config.getBlock("blockGreenDyedTorch",
 				blockGreenDyedTorchID).getInt();
 		blockRedDyedTorchID = config.getBlock("blockRedDyedTorch",
 				blockRedDyedTorchID).getInt();
 		blockBlackDyedTorchID = config.getBlock("blockBlackDyedTorch",
 				blockBlackDyedTorchID).getInt();
 		dyedLog1ID = config.getBlock("dyedLog1", dyedLog1ID).getInt();
 		dyedLog2ID = config.getBlock("dyedLog2", dyedLog2ID).getInt();
 		dyedLog3ID = config.getBlock("dyedLog3", dyedLog3ID).getInt();
 		dyedLog4ID = config.getBlock("dyedLog4", dyedLog4ID).getInt();
 		dyedStoneDoubleSlab1ID = config.getBlock("dyedStoneDoubleSlab1",
 				dyedStoneDoubleSlab1ID).getInt();
 		dyedStoneDoubleSlab2ID = config.getBlock("dyedStoneDoubleSlab2",
 				dyedStoneDoubleSlab2ID).getInt();
 		blockGemOreID = config.getBlock("blockGemOre", blockGemOreID).getInt();
 		blockGemCompressedID = config.getBlock("blockGemCompressed",
 				blockGemCompressedID).getInt();
 		blockDyedStonePaver1ID = config.getBlock("blockDyedStonePaver1",
 				blockDyedStonePaver1ID).getInt();
 		blockDyedStonePaver2ID = config.getBlock("blockDyedStonePaver2",
 				blockDyedStonePaver2ID).getInt();
 		blockDyedStoneBrickSlab1ID = config.getBlock(
 				"blockDyedStoneBrickSlab1", blockDyedStoneBrickSlab1ID)
 				.getInt();
 		blockDyedStoneBrickSlab2ID = config.getBlock(
 				"blockDyedStoneBrickSlab2", blockDyedStoneBrickSlab2ID)
 				.getInt();
 		blockDyedFencesID = config.getBlock("blockDyedFences",
 				blockDyedFencesID).getInt();
 		blockDyedCobbleWallsID = config.getBlock("blockDyedCobbleWalls",
 				blockDyedCobbleWallsID).getInt();
 		blockDyedStoneBrickDoubleSlab1ID = config.getBlock(
 				"blockDyedStoneBrickDoubleSlab1",
 				blockDyedStoneBrickDoubleSlab1ID).getInt();
 		blockDyedStoneBrickDoubleSlab2ID = config.getBlock(
 				"blockDyedStoneBrickDoubleSlab2",
 				blockDyedStoneBrickDoubleSlab2ID).getInt();
 
 		itemFlawedGemsID = config.getItem("Flawed Gems", itemFlawedGemsID)
 				.getInt();
 		itemDyedClayID = config.getItem("Dyed Clay", itemDyedClayID).getInt();
 		itemDyedClayBricksID = config.getItem("Dyed Clay Bricks",
 				itemDyedClayBricksID).getInt();
 		itemDyedMudID = config.getItem("Dyed Mud", itemDyedMudID).getInt();
 		itemDyedMudBricksID = config.getItem("Dyed Mud Bricks",
 				itemDyedMudBricksID).getInt();
         itemDyedSticksID = config.getItem("Dyed Sticks", itemDyedSticksID).getInt();
         itemGemShearsID = config.getItem("Gem Shears", itemGemShearsID).getInt();
 		
 		// General Section
 		Property genGemsEnabledProperty = config.get(Configuration.CATEGORY_GENERAL, "genGemsEnabled", true);
 		genGemsEnabledProperty.comment = "Should gems generate in the world";
 		genGemsEnabled = genGemsEnabledProperty.getBoolean(true);
 		
 		Property genGemsVeinsProperty = config.get(Configuration.CATEGORY_GENERAL, "genGemsVeins", 8);
 		genGemsVeinsProperty.comment = "Attempt to generate X veins per chunk";
 		genGemsVeins = genGemsVeinsProperty.getInt(8);
 		
 		Property genGemsNumberProperty = config.get(Configuration.CATEGORY_GENERAL, "genGemsNumber", 4);
 		genGemsNumberProperty.comment = "Attemp to generate X ores per vein";
 		genGemsNumber = genGemsNumberProperty.getInt(4);
 		
 		if (config.hasChanged()) {
 			config.save();
 		}
 	}
 }
