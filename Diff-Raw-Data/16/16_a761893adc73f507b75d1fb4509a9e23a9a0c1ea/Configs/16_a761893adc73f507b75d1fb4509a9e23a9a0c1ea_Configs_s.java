 /**
  * 
  */
 package dokutoku.lead.zotonic.lib;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.logging.Level;
 
 import powercrystals.minefactoryreloaded.api.FarmingRegistry;
 import powercrystals.minefactoryreloaded.api.IFactoryFertilizable;
 import powercrystals.minefactoryreloaded.api.IFactoryHarvestable;
 import powercrystals.minefactoryreloaded.api.IFactoryPlantable;
 
 import thermalexpansion.api.crafting.CraftingManagers;
 import thermalexpansion.api.crafting.ICrucibleManager;
 import thermalexpansion.api.item.ItemRegistry;
 
 import cpw.mods.fml.common.Loader;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import dokutoku.lead.zotonic.crop.EnumCropType;
 import dokutoku.lead.zotonic.crop.PolyCrop;
 import dokutoku.lead.zotonic.crop.seed.PlantableCropPlant;
 import dokutoku.lead.zotonic.crop.seed.PlantableStandard;
 import dokutoku.lead.zotonic.crop.seed.PolySeeds;
 import dokutoku.lead.zotonic.item.MagicBucket;
 import dokutoku.lead.zotonic.item.MagicStem;
 import dokutoku.lead.zotonic.util.LeadLogger;
 import forestry.api.core.ForestryAPI;
 import forestry.api.core.GlobalManager;
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemBlock;
 import net.minecraft.item.ItemBucket;
 import net.minecraft.item.ItemSeeds;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.FurnaceRecipes;
 import net.minecraft.util.WeightedRandomChestContent;
 import net.minecraftforge.common.ChestGenHooks;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.DungeonHooks;
 import net.minecraftforge.common.EnumPlantType;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.liquids.LiquidDictionary;
 import net.minecraftforge.liquids.LiquidStack;
 import net.minecraftforge.oredict.OreDictionary;
 import net.minecraftforge.oredict.ShapedOreRecipe;
 import net.minecraftforge.oredict.ShapelessOreRecipe;
 
 /**
  * Codename: Lead Zotonic
  *
  * Configs
  *
  * @author Atomfusion/DokuToku
  * @license MIT License (http://opensource.org/licenses/MIT)
  */
 public class Configs {
 
 		public static Configuration config;
 		
 		public static TabLeadZotonic cTab = new TabLeadZotonic(CreativeTabs.getNextID(), "Golden Thumb Crops");
 
 		/** Crops **/
 		// Common resources
 		public static Block cropIron;
 		public static int   cropIronID;
 		
 		public static Block cropGold;
 		public static int   cropGoldID;
 		
 		public static Block cropClay;
 		public static int   cropClayID;
 		
 		// Underground stuff
 		public static Block cropRedstone;
 		public static int   cropRedstoneID;
 		
 		public static Block cropCoal;
 		public static int   cropCoalID;
 		
 		// Nether resources
 		public static Block cropNetherrack;
 		public static int   cropNetherrackID;
 		
 		public static Block cropGlowstone;
 		public static int   cropGlowstoneID;
 		
 		public static Block cropQuartz;
 		public static int   cropQuartzID;
 		
 		public static Block cropSoulsand;
 		public static int   cropSoulsandID;
 		
 		// Ender resources
 		public static Block cropPearl;
 		public static int   cropPearlID;
 		
 		public static Block cropEndstone;
 		public static int   cropEndstoneID;
 		
 		// Extra items
 		public static Block cropTin;
 		public static int   cropTinID;
 		
 		public static Block cropCopper;
 		public static int   cropCopperID;
 		
 		public static Block cropSilver;
 		public static int   cropSilverID;
 		
 		public static Block cropLead;
 		public static int   cropLeadID;
 		
 		public static Block cropNickel;
 		public static int   cropNickelID;
 		
 		// Meh, I'll make it
 		public static Block cropLavaCrystal;
 		public static int   cropLavaCrystalID;
 		
 		
 		/** Seeds **/
 		// Common resources
 		public static Item seedIron;
 		public static int  seedIronID;
 		
 		public static Item seedGold;
 		public static int  seedGoldID;
 		
 		public static Item seedClay;
 		public static int  seedClayID;
 		
 		// Underground stuff
 		public static Item seedRedstone;
 		public static int  seedRedstoneID;
 		
 		public static Item seedCoal;
 		public static int  seedCoalID;
 		
 		// Nether resources
 		public static Item seedNetherrack;
 		public static int  seedNetherrackID;
 		
 		public static Item seedGlowstone;
 		public static int  seedGlowstoneID;
 		
 		public static Item seedQuartz;
 		public static int  seedQuartzID;
 		
 		public static Item seedSoulsand;
 		public static int  seedSoulsandID;
 		
 		// Ender resources
 		public static Item seedPearl;
 		public static int  seedPearlID;
 		
 		public static Item seedEndstone;
 		public static int  seedEndstoneID;
 		
 		// Extra items
 		public static Item seedTin;
 		public static int  seedTinID;
 		
 		public static Item seedCopper;
 		public static int  seedCopperID;
 		
 		public static Item seedSilver;
 		public static int  seedSilverID;
 		
 		public static Item seedLead;
 		public static int  seedLeadID;
 		
 		public static Item seedNickel;
 		public static int  seedNickelID;
 		
 		// Meh, I'll make it
 		public static Item seedLavaCrystal;
 		public static int  seedLavaCrystalID;
 		
 		// Need some stems up in her'
 		public static Item magicalStem;
 		public static int  magicalStemID;
 		
 		/* Magical Items */
 		public static Item magicBucket;
 		public static int  magicBucketID;
 		
 		public static Item magicHelmet;
 		public static int  magicHelmetID;
 		
 		public static Item magicWings;
 		public static int  magicWingsID;
 		
 		public static Item magicLeggings;
 		public static int  magicLegginsID;
 		
 		public static Item magicBoots;
 		public static int  magicBootsID;
 
 		/* Crop Collection */
 		public static ArrayList<Block> crops = new ArrayList<Block>();
 
 		/* Seeds Collection */
 		public static ArrayList<Item> seeds = new ArrayList<Item>();
 		
 		public static void init(FMLPreInitializationEvent event) {
 			config = new Configuration(event.getSuggestedConfigurationFile());
 			
 			config.load();
 			
 			/* Crops */
 			cropIronID = config.getBlock("crop", "Iron Crop ID", 2800).getInt(cropIronID);
 			cropGoldID = config.getBlock("crop", "Gold Crop ID", 2801).getInt(cropGoldID);
 			cropClayID = config.getBlock("crop", "Clay Crop ID", 2802).getInt(cropClayID);
 			
 			cropRedstoneID = config.getBlock("crop", "Redstone Crop ID", 2803).getInt(cropRedstoneID);
 			cropCoalID = config.getBlock("crop", "Coal Crop ID", 2804).getInt(cropCoalID);
 			
 			cropNetherrackID = config.getBlock("crop", "Netherrack Crop ID", 2805).getInt(cropNetherrackID);
 			cropGlowstoneID = config.getBlock("crop", "Glowstone Crop ID", 2806).getInt(cropGlowstoneID);
 			cropQuartzID = config.getBlock("crop", "Quartz Crop ID", 2807).getInt(cropQuartzID);
 			cropSoulsandID = config.getBlock("crop", "Soulsand Crop ID", 2808).getInt(cropSoulsandID);
 			
 			cropPearlID = config.getBlock("crop", "Ender Pearl Crop ID", 2809).getInt(cropPearlID);
 			cropEndstoneID = config.getBlock("crop", "Endstone Crop ID", 2810).getInt(cropEndstoneID);
 			
 			cropTinID = config.getBlock("crop", "Tin Crop ID", 2811).getInt(cropTinID);
 			cropCopperID = config.getBlock("crop", "Copper Crop ID", 2812).getInt(cropCopperID);
 			cropSilverID = config.getBlock("crop", "Silver Crop ID", 2813).getInt(cropSilverID);
 			cropLeadID = config.getBlock("crop", "Lead Crop ID", 2814).getInt(cropLeadID);
 			cropNickelID = config.getBlock("crop", "Nickel Crop ID", 2816).getInt(cropNickelID);
 			
 			cropLavaCrystalID = config.getBlock("crop", "Lava Crystal Crop ID", 2815).getInt(cropLavaCrystalID);
 			
 			/* Seeds */
 			seedIronID = config.getItem("seed", "Iron Seed ID", 5300).getInt(seedIronID);
 			seedGoldID = config.getItem("seed", "Gold Seed ID", 5301).getInt(seedGoldID);
 			seedClayID = config.getItem("seed", "Clay Seed ID", 5302).getInt(seedClayID);
 			
 			seedRedstoneID = config.getItem("seed", "Redstone Seed ID", 5303).getInt(seedRedstoneID);
 			seedCoalID = config.getItem("seed", "Coal Seed ID", 5304).getInt(seedCoalID);
 			
 			seedNetherrackID = config.getItem("seed", "Netherrack Seed ID", 5305).getInt(seedNetherrackID);
 			seedGlowstoneID = config.getItem("seed", "Glowstone Seed ID", 5306).getInt(seedGlowstoneID);
 			seedQuartzID = config.getItem("seed", "Quartz Seed ID", 5307).getInt(seedQuartzID);
 			seedSoulsandID = config.getItem("seed", "Soulsand Seed ID", 5308).getInt(seedSoulsandID);
 			
 			seedPearlID = config.getItem("seed", "Ender Pearl Seed ID", 5309).getInt(seedPearlID);
 			seedEndstoneID = config.getItem("seed", "Endstone Seed ID", 5310).getInt(seedEndstoneID);
 			
 			seedTinID = config.getItem("seed", "Tin Seed ID", 5311).getInt(seedTinID);
 			seedCopperID = config.getItem("seed", "Copper Seed ID", 5312).getInt(seedCopperID);
 			
 			seedSilverID = config.getItem("seed", "Silver Seed ID", 5313).getInt(seedSilverID);
 			seedLeadID = config.getItem("seed", "Lead Seed ID", 5314).getInt(seedLeadID);
 			
 			seedNickelID = config.getItem("seed", "Nickel Seed ID", 5318).getInt(seedNickelID);
 			
 			seedLavaCrystalID = config.getItem("seed", "Lava Crystal Seed ID", 5315).getInt(seedLavaCrystalID);
 			
 			
 			/* Crafting Items */
 			magicalStemID = config.getItem("item", "Magical Stem ID", 5316).getInt(magicalStemID);
 			
 			/* Special Items */
 			magicBucketID = config.getItem("item", "Magic Infinite Bucket", 5317).getInt(magicBucketID);
 			
 			
 			
 			config.save();
 			
 		}
 		
 		public static void load(FMLInitializationEvent event) {
 			
 			// ITEM REGISTRY
 			
 			ItemStack tin    = null;
 			ItemStack copper = null;
 			ItemStack silver = null;
 			ItemStack lead   = null;
 			ItemStack nickel = null;
 			
 			ArrayList<ItemStack> tins    = new ArrayList<ItemStack>();
 			ArrayList<ItemStack> coppers = new ArrayList<ItemStack>();
 			ArrayList<ItemStack> silvers = new ArrayList<ItemStack>();
 			ArrayList<ItemStack> leads   = new ArrayList<ItemStack>();  
 			ArrayList<ItemStack> nickels = new ArrayList<ItemStack>();
 			
 			
 			
 			// Try to get TE's ingots first. Personal preference.
 			if(ItemRegistry.getItem("ingotElectrum", 1) != null)
 			{
 				
 				tin    = ItemRegistry.getItem("oreTin", 1);
 				copper = ItemRegistry.getItem("oreCopper", 1);
 				silver = ItemRegistry.getItem("oreSilver", 1);
 				lead   = ItemRegistry.getItem("oreLead", 1);
 				nickel = ItemRegistry.getItem("oreNickel", 1);
 				
 				// Satisfy checks
 				tins.add(tin);
 				coppers.add(copper);
 				silvers.add(silver);
 				leads.add(lead);
 				nickels.add(nickel);
 				
 				
 			} else {
 			
 				tins    = OreDictionary.getOres("oreTin");
 				coppers = OreDictionary.getOres("oreCopper");
 				silvers = OreDictionary.getOres("oreSilver");
 				leads   = OreDictionary.getOres("oreLead");
 				nickels = OreDictionary.getOres("oreNickel");
 				
 				if(!tins.isEmpty())
 					tin = tins.get(0);
 				if(!coppers.isEmpty())
 					copper = coppers.get(0);
 				if(!silvers.isEmpty())
 					silver = silvers.get(0);
 				if(!leads.isEmpty())
 					lead = leads.get(0);
 				if(!nickels.isEmpty())
 					nickel = nickels.get(0);
 			
 			}
 			
 			/// METALS
 			
 			seedIron = new PolySeeds(seedIronID, cropIronID, Block.tilledField.blockID, new ItemStack(Block.oreIron), EnumCropType.OVERWORLD)
 					.setType("Iron").setUnlocalizedName("seeds.iron");
 			seeds.add(seedIron);
 			cropIron = new PolyCrop(cropIronID, (ItemSeeds) seedIron, 3).setFXType(FXType.IRON);
 			crops.add(cropIron);
 			
 			seedGold = new PolySeeds(seedGoldID, cropGoldID, Block.tilledField.blockID, new ItemStack(Block.oreGold), EnumCropType.OVERWORLD)
 					.setType("Gold").setUnlocalizedName("seeds.gold");
 			seeds.add(seedGold);
 			cropGold = new PolyCrop(cropGoldID, (ItemSeeds) seedGold, 4).setFXType(FXType.GOLD);
 			crops.add(cropGold);
 			
 			if(!tins.isEmpty()) {
 			seedTin = new PolySeeds(seedTinID, cropTinID, Block.tilledField.blockID, tin, EnumCropType.OVERWORLD)
 					.setType("Tin").setUnlocalizedName("seeds.tin");
 			seeds.add(seedTin);
 			cropTin = new PolyCrop(cropTinID, (ItemSeeds) seedTin, 2).setFXType(FXType.TIN);
 			crops.add(cropTin);
 			}
 			
 			if(!coppers.isEmpty()) {
 			seedCopper = new PolySeeds(seedCopperID, cropCopperID, Block.tilledField.blockID, copper, EnumCropType.OVERWORLD)
 					.setType("Copper").setUnlocalizedName("seeds.copper");
 			seeds.add(seedCopper);
 			cropCopper = new PolyCrop(cropCopperID, (ItemSeeds) seedCopper, 1).setFXType(FXType.COPPER);
 			crops.add(cropCopper);
 			}
 			
 			if(!silvers.isEmpty()) {
 			seedSilver = new PolySeeds(seedSilverID, cropSilverID, Block.tilledField.blockID, silver, EnumCropType.OVERWORLD)
 					.setType("Silver").setUnlocalizedName("seeds.silver");
 			seeds.add(seedSilver);
 			cropSilver = new PolyCrop(cropSilverID, (ItemSeeds) seedSilver, 4).setFXType(FXType.SILVER);
 			crops.add(cropSilver);
 			}
 			
 			if(!leads.isEmpty()) {
 			seedLead = new PolySeeds(seedLeadID, cropLeadID, Block.tilledField.blockID, lead, EnumCropType.OVERWORLD)
 					.setType("Lead").setUnlocalizedName("seeds.lead");
 			seeds.add(seedLead);
 			cropLead = new PolyCrop(cropLeadID, (ItemSeeds) seedLead, 3).setFXType(FXType.LEAD);
 			crops.add(cropLead);
 			}
 			
 			if(!nickels.isEmpty()) {
 			seedNickel = new PolySeeds(seedNickelID, cropNickelID, Block.tilledField.blockID, nickel, EnumCropType.OVERWORLD)
 					.setType("Nickel").setUnlocalizedName("seeds.nickel");
 			seeds.add(seedNickel);
 			cropNickel = new PolyCrop(cropNickelID, (ItemSeeds) seedNickel, 3).setFXType(FXType.NICKEL);
 			crops.add(cropNickel);
 			}
 			
 			
 			/// RESOURCES
 
 			
 			seedClay = new PolySeeds(seedClayID, cropClayID, 
 					Block.tilledField.blockID, new ItemStack(Item.clay, 3), EnumCropType.OVERWORLD)
 					.setType("Clay").setUnlocalizedName("seeds.clay");
 			seeds.add(seedClay);
 			cropClay = new PolyCrop(cropClayID, (ItemSeeds) seedClay, 2).setFXType(FXType.CLAY);
 			crops.add(cropClay);
 			
 			seedRedstone = new PolySeeds(seedRedstoneID, cropRedstoneID,
 					Block.tilledField.blockID, new ItemStack(Item.redstone, 3), EnumCropType.OVERWORLD)
 				    .setType("Redstone").setUnlocalizedName("seeds.redstone");
 			seeds.add(seedRedstone);
 			cropRedstone = new PolyCrop(cropRedstoneID, (ItemSeeds) seedRedstone, 2).setFXType(FXType.REDSTONE);
 			crops.add(cropRedstone);
 	
 			seedCoal = new PolySeeds(seedCoalID, cropCoalID,
 					Block.tilledField.blockID, new ItemStack(Item.coal), EnumCropType.OVERWORLD)
 					.setType("Coal").setUnlocalizedName("seeds.coal");
 			seeds.add(seedCoal);
 			cropCoal = new PolyCrop(cropCoalID, (ItemSeeds) seedCoal, 2).setFXType(FXType.COAL);
 			crops.add(cropCoal);
 			
 			seedNetherrack = new PolySeeds(seedNetherrackID, cropNetherrackID,
 					Block.slowSand.blockID, new ItemStack(Block.netherrack), EnumCropType.NETHER)
 					.setType("Hell").setUnlocalizedName("seeds.netherrack");
 			seeds.add(seedNetherrack);
 			cropNetherrack = new PolyCrop(cropNetherrackID, (ItemSeeds) seedNetherrack, 3).setFXType(FXType.HELL);
 			crops.add(cropNetherrack);
 	
 			seedGlowstone = new PolySeeds(seedGlowstoneID, cropGlowstoneID,
 					Block.slowSand.blockID, new ItemStack(Item.lightStoneDust, 3), EnumCropType.NETHER)
 					.setType("Glow").setUnlocalizedName("seeds.glowstone");
 			seeds.add(seedGlowstone);
 			cropGlowstone = new PolyCrop(cropGlowstoneID, (ItemSeeds) seedGlowstone, 4).setFXType(FXType.GLOW).setLightValue(1.2f);
 			crops.add(cropGlowstone);
 	
 			seedQuartz = new PolySeeds(seedQuartzID, cropQuartzID,
 					Block.slowSand.blockID, new ItemStack(Item.netherQuartz), EnumCropType.NETHER)
 					.setType("Quartz").setUnlocalizedName("seeds.quartz");
 			seeds.add(seedQuartz);
 			cropQuartz = new PolyCrop(cropQuartzID, (ItemSeeds) seedQuartz, 3).setFXType(FXType.QUARTZ);
 			crops.add(cropQuartz);
 	
 			seedSoulsand = new PolySeeds(seedSoulsandID, cropSoulsandID,
 					Block.slowSand.blockID, new ItemStack(Block.slowSand), EnumCropType.NETHER)
 					.setType("Soul").setUnlocalizedName("seeds.soulsand");
 			seeds.add(seedSoulsand);
 			cropSoulsand = new PolyCrop(cropSoulsandID, (ItemSeeds) seedSoulsand, 4).setFXType(FXType.SOUL);
 			crops.add(cropSoulsand);
 	
 			seedPearl = new PolySeeds(seedPearlID, cropPearlID,
 					Block.tilledField.blockID, new ItemStack(Item.enderPearl), EnumCropType.END)
 					.setType("Pearl").setUnlocalizedName("seeds.enderpearl");
 			seeds.add(seedPearl);
 			cropPearl = new PolyCrop(cropPearlID, (ItemSeeds) seedPearl, 5).setFXType(FXType.PEARL);
 			crops.add(cropPearl);
 
 			seedEndstone = new PolySeeds(seedEndstoneID, cropEndstoneID,
 					Block.tilledField.blockID, new ItemStack(Block.whiteStone), EnumCropType.END)
 					.setType("End").setUnlocalizedName("seeds.endstone");
 			seeds.add(seedEndstone);
 			cropEndstone = new PolyCrop(cropEndstoneID, (ItemSeeds) seedEndstone, 5).setFXType(FXType.END);
 			crops.add(cropEndstone);
 	
 			seedLavaCrystal = new PolySeeds(seedLavaCrystalID, cropLavaCrystalID,
 					Block.tilledField.blockID, new ItemStack(Item.bucketLava), EnumCropType.LAVA)
 					.setType("Lava").setUnlocalizedName("seeds.lavacrystal");
 			seeds.add(seedLavaCrystal);
 			cropLavaCrystal = new PolyCrop(cropLavaCrystalID, (ItemSeeds) seedLavaCrystal, 4).setFXType(FXType.LAVA);
 			crops.add(cropLavaCrystal);
 			
 			magicBucket = new MagicBucket(magicBucketID, Block.waterMoving.blockID).setUnlocalizedName("magic.bucket")
 					.setContainerItem(Item.bucketEmpty).setCreativeTab(cTab);
 			
 			
 			// MAGIC CRAFTING RESOURCES
 			
 			magicalStem = new MagicStem(magicalStemID).setCreativeTab(cTab);
 			
 			ItemStack stem = new ItemStack(magicalStem);
 			
 			for(Item seed : seeds)
 			{
 				GameRegistry.addShapelessRecipe(new ItemStack(seed),
 				stem,
 				new ItemStack(((PolySeeds)seed).getProduct().getItem()));
 			}
 			
 			if(!tins.isEmpty())
 			GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(seedTin),
 			stem,
 			"oreTin"));
 			
 			if(!coppers.isEmpty())
 			GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(seedCopper),
 			stem,
 			"oreCopper"));
 			
 			if(!silvers.isEmpty())
 			GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(seedSilver),
 			stem,
 			"oreSilver"));
 			
 			if(!leads.isEmpty())
 			GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(seedLead),
 			stem,
 			"oreLead"));
 			
 			if(!nickels.isEmpty())
 			GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(seedNickel),
 			stem,
 			"oreNickel"));
 
 			
 			// LANGUAGE REGISTRY
 			
 			LanguageRegistry.addName(seedIron, "Iron Seeds");
 			LanguageRegistry.addName(cropIron, "Iron crop");
 			
 			LanguageRegistry.addName(seedGold, "Gold Seeds");
 			LanguageRegistry.addName(cropGold, "Gold crop");
 			
 			if(!tins.isEmpty()) {
 			LanguageRegistry.addName(seedTin, "Tin Seeds");
 			LanguageRegistry.addName(cropTin, "Tin crop");
 			}
 			
 			if(!coppers.isEmpty()) {
 			LanguageRegistry.addName(seedCopper, "Copper Seeds");
 			LanguageRegistry.addName(cropCopper, "Copper crop");
 			}
 			
 			if(!silvers.isEmpty()) {
 			LanguageRegistry.addName(seedSilver, "Silver Seeds");
 			LanguageRegistry.addName(cropSilver, "Silver crop");
 			}
 			
 			if(!leads.isEmpty()) {
 			LanguageRegistry.addName(seedLead, "Lead Seeds");
 			LanguageRegistry.addName(cropLead, "Lead crop");
 			}
 			
 			if(!nickels.isEmpty()) {
 			LanguageRegistry.addName(seedNickel, "Nickel Seeds");
 			LanguageRegistry.addName(cropNickel, "Nickel crop");
 			}
 			
 
 			LanguageRegistry.addName(seedClay, "Clay Seeds");
 			LanguageRegistry.addName(cropClay, "Clay crop");
 			
 			LanguageRegistry.addName(seedEndstone, "End Stone Seeds");
 			LanguageRegistry.addName(cropEndstone, "End Stone crop");
 			
 			LanguageRegistry.addName(seedGlowstone, "Glowstone Seeds");
 			LanguageRegistry.addName(cropGlowstone, "Glowstone crop");
 			
 			LanguageRegistry.addName(seedNetherrack, "Netherrack Seeds");
 			LanguageRegistry.addName(cropNetherrack, "Netherrack crop");
 			
 			LanguageRegistry.addName(seedLavaCrystal, "Lava Crystal Seeds");
 			LanguageRegistry.addName(cropLavaCrystal, "Lava Crystal crop");
 			
 			LanguageRegistry.addName(seedPearl, "Ender Pearl Seeds");
 			LanguageRegistry.addName(cropPearl, "Ender Pearl crop");
 			
 			LanguageRegistry.addName(seedQuartz, "Quartz Seeds");
 			LanguageRegistry.addName(cropQuartz, "Quartz crop");
 			
 			LanguageRegistry.addName(seedRedstone, "Redstone Seeds");
 			LanguageRegistry.addName(cropRedstone, "Redstone crop");
 			
 			LanguageRegistry.addName(seedSoulsand, "Soul Seeds");
 			LanguageRegistry.addName(cropSoulsand, "Soul crop");
 			
 			LanguageRegistry.addName(seedCoal, "Coal Seeds");
 			LanguageRegistry.addName(cropCoal, "Coal crop");
 			
 			LanguageRegistry.addName(magicalStem, "Magical Stem");
 			
 			LanguageRegistry.addName(magicBucket, "Magical Bucket");
 			
 			
 			// SMELTING RECIPES
 			
 			/* GameRegistry.addSmelting(seedIron.itemID, ((PolySeeds) seedIron).getProduct(), 0.0f);
 			GameRegistry.addSmelting(seedGold.itemID, ((PolySeeds) seedGold).getProduct(), 0.0f);
 			
 			if(!tins.isEmpty())    GameRegistry.addSmelting(seedTin.itemID, ((PolySeeds) seedTin).getProduct(), 0.0f);
 			if(!coppers.isEmpty()) GameRegistry.addSmelting(seedCopper.itemID, ((PolySeeds) seedCopper).getProduct(), 0.0f);
 			if(!silvers.isEmpty()) GameRegistry.addSmelting(seedSilver.itemID, ((PolySeeds) seedSilver).getProduct(), 0.0f);
 			if(!leads.isEmpty())   GameRegistry.addSmelting(seedLead.itemID, ((PolySeeds) seedLead).getProduct(), 0.0f);
 			if(!nickels.isEmpty()) GameRegistry.addSmelting(seedNickel.itemID, ((PolySeeds) seedNickel).getProduct(), 0.0f);
 			*/
 			
 			// CRAFTING RECIPES
 			
 			for (Item seed : seeds) {
 				GameRegistry.addShapelessRecipe(((PolySeeds) seed).getProduct(), new ItemStack(seed));
 			}
 			
			/* 
			GameRegistry.addShapelessRecipe(((PolySeeds) seedClay).getProduct(), new ItemStack(seedClay));
			GameRegistry.addShapelessRecipe(((PolySeeds) seedCoal).getProduct(), new ItemStack(seedCoal));
			GameRegistry.addShapelessRecipe(((PolySeeds) seedRedstone).getProduct(), new ItemStack(seedRedstone));
			GameRegistry.addShapelessRecipe(((PolySeeds) seedNetherrack).getProduct(), new ItemStack(seedNetherrack));
			GameRegistry.addShapelessRecipe(((PolySeeds) seedGlowstone).getProduct(), new ItemStack(seedGlowstone));
			GameRegistry.addShapelessRecipe(((PolySeeds) seedQuartz).getProduct(), new ItemStack(seedQuartz));
			GameRegistry.addShapelessRecipe(((PolySeeds) seedSoulsand).getProduct(), new ItemStack(seedSoulsand));
			GameRegistry.addShapelessRecipe(((PolySeeds) seedPearl).getProduct(), new ItemStack(seedPearl));
			GameRegistry.addShapelessRecipe(((PolySeeds) seedEndstone).getProduct(), new ItemStack(seedEndstone));
 			GameRegistry.addShapelessRecipe(((PolySeeds) seedLavaCrystal).getProduct(), new ItemStack(seedLavaCrystal), new ItemStack(Item.bucketEmpty));
			*/
 			
 			GameRegistry.addShapelessRecipe(new ItemStack(magicBucket), new ItemStack(Item.bucketWater), new ItemStack(magicalStem));
 			
 			addMiscRecipes();
 			
 			// GRASS DROPS
 			
 			// MinecraftForge.addGrassSeed(new ItemStack(magicalStem), 1); // Exceedingly rare. I think a wizard dropped it :3
 			
 			// DUNGEON DROPS
 			
 			ChestGenHooks.addItem(ChestGenHooks.DUNGEON_CHEST, new WeightedRandomChestContent(new ItemStack(magicalStem), 1, 3, 5));
 			ChestGenHooks.addItem(ChestGenHooks.PYRAMID_JUNGLE_CHEST, new WeightedRandomChestContent(new ItemStack(magicalStem), 1, 3, 5));
 			ChestGenHooks.addItem(ChestGenHooks.PYRAMID_DESERT_CHEST, new WeightedRandomChestContent(new ItemStack(magicalStem), 1, 3, 5));
 			ChestGenHooks.addItem(ChestGenHooks.MINESHAFT_CORRIDOR, new WeightedRandomChestContent(new ItemStack(magicalStem), 1, 3, 5));
 			ChestGenHooks.addItem(ChestGenHooks.STRONGHOLD_CORRIDOR, new WeightedRandomChestContent(new ItemStack(magicalStem), 1, 3, 5));
 			ChestGenHooks.addItem(ChestGenHooks.STRONGHOLD_LIBRARY, new WeightedRandomChestContent(new ItemStack(magicalStem), 1, 3, 5));
 			ChestGenHooks.addItem(ChestGenHooks.STRONGHOLD_CROSSING, new WeightedRandomChestContent(new ItemStack(magicalStem), 1, 3, 5));
 			
 			
 			
 		}
 		
 		/**
 		 * 
 		 */
 		private static void addMiscRecipes() {
 			
 			// Zombie Flesh to Leather
 			
 			GameRegistry.addSmelting(Item.rottenFlesh.itemID, new ItemStack(Item.leather), 0.2f);
 			
 		}
 
 		public static void postLoad(FMLPostInitializationEvent event)
 		{
 			
 			// TE HOOKS
 			
 			if(Loader.isModLoaded("ThermalExpansion")) {
 				try {
 					CraftingManagers.crucibleManager.addRecipe(100, new ItemStack(seedLavaCrystal), LiquidDictionary.getLiquid("Lava", 1000));
 				} catch(Exception e) {
 					LeadLogger.log(Level.SEVERE, "Could not load Thermal Expansion recipes");
 					e.printStackTrace(System.err);
 				}
 			}
 			
 			// MFR HOOKS
 			
 			if(Loader.isModLoaded("MineFactoryReloaded")) {
 				try {
 					LeadLogger.log(Level.INFO, "Loading MFR Support.");
 					
 					for(Block crop : crops) {
 						FarmingRegistry.registerHarvestable((IFactoryHarvestable)crop);
 						FarmingRegistry.registerFertilizable((IFactoryFertilizable)crop);
 						FarmingRegistry.registerPlantable(new PlantableCropPlant(((PolyCrop)crop).getSeedItem(), ((PolyCrop)crop).getCropItem()));
 					}
 					
 				} catch(Exception e) {
 					LeadLogger.log(Level.SEVERE, "Could not load MFR integration.");
 					e.printStackTrace(System.err);
 				}
 			}
 			
 			
 		}
 
 }
