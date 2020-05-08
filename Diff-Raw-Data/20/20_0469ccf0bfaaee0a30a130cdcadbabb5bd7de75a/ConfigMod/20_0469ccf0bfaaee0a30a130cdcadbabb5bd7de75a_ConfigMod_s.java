 package net.lomeli.magiks.core.config;
 
 import java.io.File;
 
 import net.lomeli.magiks.lib.Ints;
 import net.minecraftforge.common.Configuration;
 
 
 public class ConfigMod
 {
     public static boolean capesEnabled;
     public static boolean disablePiras;
 
     public static void configureItemID(String loc)
     {
         Configuration config = new Configuration(new File(loc,
                 "MechroMagikIDs.cfg"));
 
         config.load();
 
         Ints.flyingRingID = config.get("Items", "FlyingRing", 7000)
                 .getInt(7000);
         Ints.enchantedGemID = config.get("Items", "EnchantedGem", 7001).getInt(
                 7001);
         Ints.enchantedDiamondID = config.get("Items", "EnchantedDiamond", 7002)
                 .getInt(7002);
         Ints.ironBandID = config.get("Items", "IronBand", 7003).getInt(7003);
         Ints.ironStickID = config.get("Items", "IronStick", 7004).getInt(7004);
         Ints.ingotStamaticID = config.get("Ingots", "IngotStamatic", 7005)
                 .getInt(7005);
         Ints.ingotIgniousID = config.get("Ingots", "IngotIgnious", 7006).getInt(
                 7006);
         Ints.darkMatterID = config.get("Items", "darkMatter", 7007)
                 .getInt(7007);
         Ints.deprivedDustID = config.get("Items", "deprivedLightStone", 7008)
                 .getInt(7008);
         Ints.mistPanelID = config.get("Items", "mistPanel", 7009).getInt(7009);
         Ints.burningUpgradeID = config.get("Items", "burningUpgrades", 7010)
                 .getInt(7010);
         Ints.ironPlateID = config.get("Items", "ironPlate", 7011).getInt(711);
 
         Ints.empoweredPickID = config.get("Tools", "EmpoweredPick", 7020)
                 .getInt(7020);
         Ints.pirasVarinhaID = config.get("Wands", "FireWand", 7021)
                 .getInt(7021);
         Ints.diggersWandID = config.get("Wands", "MiningWand-Level0", 7022)
                 .getInt(7022);
 
         Ints.basicWandID = config.get("Wands", "basicwand", 7023).getInt(7023);
         Ints.chemistWandID = config.get("Wands", "chemistwand", 7024).getInt(
                 7024);
         Ints.alchemistWandID = config.get("Wands", "alchemistwand", 7025)
                 .getInt(7025);
         Ints.wandID = config.get("Wands", "wand", 7026).getInt(7026);
         
         Ints.ingotCopperID = config.get("Ingots", "IngotCopper", 7030).getInt(7030);
         Ints.ingotTinID = config.get("Ingots", "IngotTin", 7031).getInt(7031);
         Ints.ingotSilverID = config.get("Ingots", "IngotSilver", 7032).getInt(7032);
 
         Ints.enchantedOreID = config.get("Blocks", "EnchantedOre", 700).getInt(
                 100);
         Ints.enchantedBlockID = config.get("Blocks", "EnchantedBlock", 701)
                 .getInt(701);
         Ints.kineticGenID = config.get("Machines", "KineticGen", 702).getInt(
                 702);
         Ints.manceryBlockID = config.get("Blocks", "ManceryBlock", 703).getInt(
                 703);
         Ints.manceryGlassID = config.get("Blocks", "ManceryGlass", 704).getInt(
                 704);
         Ints.stamaticOreID = config.get("Blocks", "OreStamatic", 705).getInt(
                 705);
         Ints.igniousOreID = config.get("Blocks", "OreIgnious", 706).getInt(706);
         Ints.solarGenID = config.get("Machines", "SolarCollector", 707).getInt(
                 707);
         Ints.mistCrafterID = config.get("Blocks", "MistCrafter", 708).getInt(
                 708);
         Ints.dupeFurnaceID = config.get("Machines", "DupeFurnace", 709).getInt(
                 709);
         Ints.dupeDummyID = config.get("Blocks", "BuringStone", 710).getInt(710);
         Ints.manceryBrickID = config.get("Blocks", "ManceryBrick", 711).getInt(
                 711);
         Ints.obsidianStairsID = config.get("Blocks", "ObsidianStairs", 712).getInt(712);
         Ints.manceryStoneStairsID = config.get("Blocks", "ManceryStoneStairs", 713).getInt(713);
         Ints.manceryBrickStairsID = config.get("Blocks", "ManceryBrickStairs", 714).getInt(714);
         Ints.burningBrickStairsID = config.get("Blocks", "BurningStoneStairs", 715).getInt(715);
         Ints.netherIgniousID = config.get("Blocks", "NetherIgnious", 716).getInt(716);
         Ints.copperOreID = config.get("Blocks", "copperOre", 717).getInt(717);
         Ints.tinOreID = config.get("Blocks", "tinOre", 718).getInt(718);
         Ints.silverOreID = config.get("Blocks", "silverOre", 719).getInt(719);
         Ints.smallCoilID = config.get("Machines", "smallCoil", 720).getInt(720);
 
         config.save();
     }
 
     public static void configureOptions(String loc)
     {
         Configuration config = new Configuration(new File(loc,
                 "MechroMagikOptions.cfg"));
 
         config.load();
 
         capesEnabled = config.get(null, "capesEnabled", true).getBoolean(true);
         disablePiras = config.get(null, "disablePiras", false)
                 .getBoolean(false);
 
         config.save();
     }
 }
