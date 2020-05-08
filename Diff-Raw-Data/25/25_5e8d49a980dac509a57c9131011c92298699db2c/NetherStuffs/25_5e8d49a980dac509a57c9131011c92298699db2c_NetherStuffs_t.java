 package bstramke.NetherStuffs;
 
 import java.util.HashMap;
 import java.util.List;
 
 import mods.tinker.tconstruct.library.TConstructRegistry;
 import mods.tinker.tconstruct.library.crafting.LiquidCasting;
 import mods.tinker.tconstruct.library.crafting.Smeltery;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.EnumCreatureType;
 import net.minecraft.entity.monster.EntityBlaze;
 import net.minecraft.entity.monster.EntitySkeleton;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.CraftingManager;
 import net.minecraft.item.crafting.FurnaceRecipes;
 import net.minecraft.item.crafting.ShapedRecipes;
 import net.minecraft.world.biome.BiomeGenBase;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.liquids.LiquidContainerRegistry;
 import net.minecraftforge.liquids.LiquidDictionary;
 import net.minecraftforge.liquids.LiquidStack;
 import net.minecraftforge.oredict.OreDictionary;
 import net.minecraftforge.oredict.ShapedOreRecipe;
 import net.minecraftforge.oredict.ShapelessOreRecipe;
 import thaumcraft.api.EnumTag;
 import thaumcraft.api.ObjectTags;
 import thaumcraft.api.ThaumcraftApi;
 import bstramke.NetherStuffs.Blocks.BlockRegistry;
 import bstramke.NetherStuffs.Blocks.LeafItemBlock;
 import bstramke.NetherStuffs.Blocks.Ore;
 import bstramke.NetherStuffs.Blocks.OreItemBlock;
 import bstramke.NetherStuffs.Blocks.Plank;
 import bstramke.NetherStuffs.Blocks.PlankItemBlock;
 import bstramke.NetherStuffs.Blocks.Sapling;
 import bstramke.NetherStuffs.Blocks.SaplingItemBlock;
 import bstramke.NetherStuffs.Blocks.Wood;
 import bstramke.NetherStuffs.Blocks.WoodItemBlock;
 import bstramke.NetherStuffs.Blocks.decorative.NetherDoubleSlabItemBlock;
 import bstramke.NetherStuffs.Blocks.decorative.NetherFence;
 import bstramke.NetherStuffs.Blocks.decorative.NetherFenceGate;
 import bstramke.NetherStuffs.Blocks.decorative.NetherHalfSlabItemBlock;
 import bstramke.NetherStuffs.Blocks.decorative.NetherSlab;
 import bstramke.NetherStuffs.Blocks.decorative.NetherStairs;
 import bstramke.NetherStuffs.Blocks.demonicFurnace.DemonicFurnaceRecipes;
 import bstramke.NetherStuffs.Blocks.demonicFurnace.TileDemonicFurnace;
 import bstramke.NetherStuffs.Blocks.puddles.NetherWoodPuddleItemBlock;
 import bstramke.NetherStuffs.Blocks.puddles.TileNetherWoodPuddle;
 import bstramke.NetherStuffs.Blocks.soulBlocker.SoulBlocker;
 import bstramke.NetherStuffs.Blocks.soulBlocker.SoulBlockerItemBlock;
 import bstramke.NetherStuffs.Blocks.soulBlocker.TileSoulBlocker;
 import bstramke.NetherStuffs.Blocks.soulBomb.EntitySoulBombPrimed;
 import bstramke.NetherStuffs.Blocks.soulBomb.SoulBombItemBlock;
 import bstramke.NetherStuffs.Blocks.soulCondenser.SoulCondenser;
 import bstramke.NetherStuffs.Blocks.soulCondenser.SoulCondenserItemBlock;
 import bstramke.NetherStuffs.Blocks.soulCondenser.TileSoulCondenser;
 import bstramke.NetherStuffs.Blocks.soulDetector.SoulDetector;
 import bstramke.NetherStuffs.Blocks.soulDetector.SoulDetectorItemBlock;
 import bstramke.NetherStuffs.Blocks.soulDetector.TileSoulDetector;
 import bstramke.NetherStuffs.Blocks.soulEngine.SoulEngine;
 import bstramke.NetherStuffs.Blocks.soulEngine.SoulEngineFuel;
 import bstramke.NetherStuffs.Blocks.soulEngine.TileSoulEngine;
 import bstramke.NetherStuffs.Blocks.soulFurnace.TileSoulFurnace;
 import bstramke.NetherStuffs.Blocks.soulSiphon.SoulSiphon;
 import bstramke.NetherStuffs.Blocks.soulSiphon.SoulSiphonItemBlock;
 import bstramke.NetherStuffs.Blocks.soulSiphon.TileSoulSiphon;
 import bstramke.NetherStuffs.Blocks.soulWorkBench.SoulWorkBenchRecipes;
 import bstramke.NetherStuffs.Blocks.soulWorkBench.TileSoulWorkBench;
 import bstramke.NetherStuffs.Client.ClientPacketHandler;
 import bstramke.NetherStuffs.Common.CommonProxy;
 import bstramke.NetherStuffs.Common.GuiHandler;
 import bstramke.NetherStuffs.Common.NetherStuffsFuel;
 import bstramke.NetherStuffs.Common.ServerPacketHandler;
 import bstramke.NetherStuffs.Common.Materials.NetherMaterials;
 import bstramke.NetherStuffs.Items.DemonicGear;
 import bstramke.NetherStuffs.Items.EntityTorchArrow;
 import bstramke.NetherStuffs.Items.ItemRegistry;
 import bstramke.NetherStuffs.Items.NetherCharcoal;
 import bstramke.NetherStuffs.Items.PotionBottle;
 import bstramke.NetherStuffs.Items.PotionBowl;
 import bstramke.NetherStuffs.Items.SoulEnergyBottle;
 import bstramke.NetherStuffs.Liquids.LiquidItemBlock;
 import bstramke.NetherStuffs.Liquids.LiquidTextureLogic;
 import bstramke.NetherStuffs.WorldGen.WorldGenDefaultMinable;
 import bstramke.NetherStuffs.WorldGen.WorldGenNetherStuffsTrees;
 import cpw.mods.fml.common.DummyModContainer;
 import cpw.mods.fml.common.FMLLog;
 import cpw.mods.fml.common.Loader;
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
 import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.EntityRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
@Mod(name = "NetherStuffs", version = "0.17.6", modid = "NetherStuffs",
 		dependencies = "after:Thaumcraft;after:Forestry;after:BuildCraft|Core;after:TConstruct;after:HarkenScythe_Core;"
 )
 @NetworkMod(clientSideRequired = true, serverSideRequired = false, clientPacketHandlerSpec = @SidedPacketHandler(channels = { "NetherStuffs" }, packetHandler = ClientPacketHandler.class), serverPacketHandlerSpec = @SidedPacketHandler(channels = { "NetherStuffs" }, packetHandler = ServerPacketHandler.class))
 public class NetherStuffs extends DummyModContainer {
 	@Instance
 	public static NetherStuffs instance = new NetherStuffs();
 
 	private GuiHandler guiHandler = new GuiHandler();
 
 	@SidedProxy(clientSide = "bstramke.NetherStuffs.Client.ClientProxy", serverSide = "bstramke.NetherStuffs.Common.CommonProxy")
 	public static CommonProxy proxy;
 
 	public static boolean bBuildcraftAvailable = false;
 	public static boolean bHarkenScytheAvailable = false;
 	public static boolean bThaumcraftAvailable = false;
 	public static boolean bTConstructAvailable = false;
 
 	public static boolean ShowOreDistributions = false;
 	public static boolean DevSetCoreModAvailable = false;
 
 	public static int NetherOreBlockId;
 	public static int NetherWoodBlockId;
 	public static int NetherPlankBlockId;
 	public static int NetherDemonicFurnaceBlockId;
 	public static int NetherLeavesBlockId;
 	public static int NetherSaplingBlockId;
 	public static int NetherSoulGlassBlockid;
 	public static int NetherSoulGlassPaneBlockid;
 	public static int NetherPuddleBlockId;
 	public static int SoulWorkBenchBlockId;
 	public static int NetherSoulBombBlockId;
 	public static int NetherWoodPuddleBlockId;
 
 	public static int SoulEngineBlockId = 0;
 
 	public static int NetherObsidianSwordAcidItemId;
 	public static int NetherObsidianSwordDeathItemId;
 
 	public static int NetherOreIngotItemId;
 	public static int NetherWoodCharcoalItemId;
 	public static int NetherDemonicBarHandleItemId;
 	public static int NetherObsidianSwordItemId;
 	public static int NetherDiamondSwordItemId;
 	public static int NetherDiamondSwordAcidItemId;
 	public static int NetherDiamondSwordDeathItemId;
 	public static int NetherWoodStickItemId;
 	public static int NetherStoneBowlItemId;
 	public static int NetherStonePotionBowlItemId;
 	public static int NetherSoulGlassBottleItemId;
 	public static int NetherPotionBottleItemId;
 
 	public static int SoulEnergyBottleItemId;
 
 	public static int NetherSoulglassSwordItemId;
 	public static int NetherSoulglassSwordAcidItemId;
 	public static int NetherSoulglassSwordDeathItemId;
 	public static int NetherSoulglassSwordHellfireItemId;
 
 	public static int NetherBowItemId;
 	public static int TorchArrowItemId;
 	public static int NetherGearItemId;
 
 	public static int NetherSoulDetectorBlockId;
 	public static int NetherSoulBlockerBlockId;
 	public static int NetherSoulFurnaceBlockId;
 	public static int NetherSoulSiphonBlockId;
 	public static int NetherStairHellfireBlockId;
 	public static int NetherStairAcidBlockId;
 	public static int NetherStairDeathBlockId;
 	public static int NetherHalfSlabBlockId;
 	public static int NetherDoubleSlabBlockId;
 	public static int SoulCondenserBlockId = 0;
 	public static int NetherFenceGateNetherBricksBlockId;
 	public static int NetherFenceGateHellfireBlockId;
 	public static int NetherFenceGateAcidBlockId;
 	public static int NetherFenceGateDeathBlockId;
 	public static int NetherFenceHellfireBlockId;
 	public static int NetherFenceAcidBlockId;
 	public static int NetherFenceDeathBlockId;
 
 	public static int LiquidBlockFlowingId;
 	public static int LiquidBlockStillId;
 	
 	//public static int NetherLampBlockId;
 
 	private static boolean SpawnSkeletonsAwayFromNetherFortresses;
 	private static boolean IncreaseNetherrackHardness;
 	private static boolean SpawnBlazesNaturally;
 	public static boolean bShowCoreModMissingWarning;
 	private static boolean bUseSoulEngineBlock;
 	private static boolean bUseHarkenScytheCondenserBlock;
 	private static boolean bUseThaumcraft;
 	public static boolean bUseForestry;
 	public static boolean bUseTConstruct;
 	public static boolean bUseStairBlocks;
 	public static boolean bUseSlabBlocks;
 	public static boolean bUseFenceGates;
 	public static boolean bUseFences;
 
 	public static boolean bUseNetherOreDemonic;
 	public static boolean bUseNetherOreCoal;
 	public static boolean bUseNetherOreIron;
 	public static boolean bUseNetherOreGold;
 	public static boolean bUseNetherOreDiamond;
 	public static boolean bUseNetherOreEmerald;
 	public static boolean bUseNetherOreRedstone;
 	public static boolean bUseNetherOreObsidian;
 	public static boolean bUseNetherOreLapis;
 	public static boolean bUseNetherOreCobblestone;
 
 	public static LiquidStack SoulEnergyLiquid;
 	public static LiquidStack DemonicIngotLiquid;
 
 	public static CreativeTabs tabNetherStuffs = new CreativeTabs("tabNetherStuffs") {
 		public ItemStack getIconItemStack() {
 			return new ItemStack(BlockRegistry.SoulWorkBench, 1, 0);
 		}
 	};
 	
 	@PreInit
 	public void PreLoad(FMLPreInitializationEvent event) {
 		FMLLog.info("[NetherStuffs] PreLoad");		
 		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
 		config.load();
 
 		NetherOreBlockId = config.getBlock(Configuration.CATEGORY_BLOCK, "NetherOre", 1230).getInt(1230);
 		NetherWoodBlockId = config.getBlock(Configuration.CATEGORY_BLOCK, "NetherWood", 1231).getInt(1231);
 		NetherPlankBlockId = config.getBlock(Configuration.CATEGORY_BLOCK, "NetherPlank", 1232).getInt(1232);
 		NetherDemonicFurnaceBlockId = config.getBlock(Configuration.CATEGORY_BLOCK, "DemonicFurnace", 1233).getInt(1233);
 		NetherLeavesBlockId = config.getBlock(Configuration.CATEGORY_BLOCK, "Leaves", 1234).getInt(1234);
 		NetherSaplingBlockId = config.getBlock(Configuration.CATEGORY_BLOCK, "Saplings", 1235).getInt(1235);
 		NetherSoulGlassBlockid = config.getBlock(Configuration.CATEGORY_BLOCK, "Glass", 1236).getInt(1236);
 		NetherSoulGlassPaneBlockid = config.getBlock(Configuration.CATEGORY_BLOCK, "GlassPane", 1237).getInt(1237);
 		NetherPuddleBlockId = config.getBlock(Configuration.CATEGORY_BLOCK, "Puddle", 1238).getInt(1238);
 		SoulWorkBenchBlockId = config.getBlock(Configuration.CATEGORY_BLOCK, "SoulWorkbench", 1239).getInt(1239);
 		NetherSoulBombBlockId = config.getBlock(Configuration.CATEGORY_BLOCK, "SoulBomb", 1240).getInt(1240);
 		NetherSoulDetectorBlockId = config.getBlock(Configuration.CATEGORY_BLOCK, "SoulDetector", 1241).getInt(1241);
 		NetherSoulBlockerBlockId = config.getBlock(Configuration.CATEGORY_BLOCK, "SoulBlocker", 1242).getInt(1242);
 		NetherSoulFurnaceBlockId = config.getBlock(Configuration.CATEGORY_BLOCK, "SoulFurnace", 1243).getInt(1243);
 		NetherSoulSiphonBlockId = config.getBlock(Configuration.CATEGORY_BLOCK, "SoulSiphon", 1244).getInt(1244);
 		NetherWoodPuddleBlockId = config.getBlock(Configuration.CATEGORY_BLOCK, "NetherWoodPuddles", 1246).getInt(1246);
 
 		config.addCustomCategoryComment(
 				"ModCompatibility",
 				"Here you can set if you want to use compatibility Blocks and or APIs inside NetherStuffs.\nDont worry, if these mods are not found the Blocks/APIs wont be loaded anyways even if they are set to true.\nBuildCraft = Soul Engine\nHarken Scythe = SoulenergyCondenser");
 		bUseThaumcraft = config.get("ModCompatibility", "UseThaumcraft", true).getBoolean(true);
 		bUseSoulEngineBlock = config.get("ModCompatibility", "UseBuildcraft", true).getBoolean(true);
 		bUseHarkenScytheCondenserBlock = config.get("ModCompatibility", "UseHarkenScythe", true).getBoolean(true);
 		bUseForestry = config.get("ModCompatibility", "UseForestry", true).getBoolean(true);
 		bUseTConstruct = config.get("ModCompatibility", "Use Tinkers Construct", true).getBoolean(true);
 
 		if (bUseSoulEngineBlock)
 			SoulEngineBlockId = config.getBlock(Configuration.CATEGORY_BLOCK, "SoulEngine", 1247).getInt(1247);
 
 		if (bUseHarkenScytheCondenserBlock) {
 			SoulCondenserBlockId = config.getBlock(Configuration.CATEGORY_BLOCK, "SoulenergyCondenser", 1248).getInt(1248);
 			TileSoulCondenser.nToHarkenScytheRate = config.get("ModCompatibility", "NetherStuffs SoulEnergy to Harken Scythe Souls", 250).getInt(250);
 			TileSoulCondenser.nFromHarkenScytheRate = config.get("ModCompatibility", "HarkenScythe Souls to SoulEnergy Rate", 150).getInt(150);
 		}
 
 		bUseStairBlocks = config.get(Configuration.CATEGORY_GENERAL, "Use Stair Blocks", true).getBoolean(true);
 		bUseSlabBlocks = config.get(Configuration.CATEGORY_GENERAL, "Use Slab Blocks", true).getBoolean(true);
 		bUseFenceGates = config.get(Configuration.CATEGORY_GENERAL, "Use new Fence Gate Blocks", true).getBoolean(true);
 		bUseFences = config.get(Configuration.CATEGORY_GENERAL, "Use new Fence Blocks", true).getBoolean(true);
 
 		if (bUseStairBlocks) {
 			NetherStairHellfireBlockId = config.getBlock(Configuration.CATEGORY_BLOCK, "HellfireStair", 1249).getInt(1249);
 			NetherStairAcidBlockId = config.getBlock(Configuration.CATEGORY_BLOCK, "AcidStair", 1250).getInt(1250);
 			NetherStairDeathBlockId = config.getBlock(Configuration.CATEGORY_BLOCK, "DeathStair", 1251).getInt(1251);
 		}
 
 		if (bUseSlabBlocks) {
 			NetherHalfSlabBlockId = config.getBlock(Configuration.CATEGORY_BLOCK, "HalfSlab BlockId", 1252).getInt(1252);
 			NetherDoubleSlabBlockId = config.getBlock(Configuration.CATEGORY_BLOCK, "DoubleSlab BlockId", 1253).getInt(1253);
 		}
 
 		if (bUseFenceGates) {
 			NetherFenceGateNetherBricksBlockId = config.getBlock(Configuration.CATEGORY_BLOCK, "NetherBrick Fence Gate", 1254).getInt(1254);
 			NetherFenceGateHellfireBlockId = config.getBlock(Configuration.CATEGORY_BLOCK, "Hellfire Fence Gate", 1255).getInt(1255);
 			NetherFenceGateAcidBlockId = config.getBlock(Configuration.CATEGORY_BLOCK, "Acid Fence Gate", 1256).getInt(1256);
 			NetherFenceGateDeathBlockId = config.getBlock(Configuration.CATEGORY_BLOCK, "Death Fence Gate", 1257).getInt(1257);
 		}
 
 		if (bUseFences) {
 			NetherFenceHellfireBlockId = config.getBlock(Configuration.CATEGORY_BLOCK, "Hellfire Fence", 1258).getInt(1258);
 			NetherFenceAcidBlockId = config.getBlock(Configuration.CATEGORY_BLOCK, "Acid Fence", 1259).getInt(1259);
 			NetherFenceDeathBlockId = config.getBlock(Configuration.CATEGORY_BLOCK, "Death Fence", 1260).getInt(1260);
 		}
 
 		LiquidBlockFlowingId = config.getBlock(Configuration.CATEGORY_BLOCK, "LiquidFlowing", 1261).getInt(1261);
 		LiquidBlockStillId = config.getBlock(Configuration.CATEGORY_BLOCK, "LiquidStill", 1262).getInt(1262);
 		
 		//NetherLampBlockId = config.getBlock(Configuration.CATEGORY_BLOCK, "NetherLamp", 1263).getInt(1263);
 
 		NetherOreIngotItemId = config.getItem(Configuration.CATEGORY_ITEM, "NetherIngots", 5200).getInt(5200);
 		NetherDemonicBarHandleItemId = config.getItem(Configuration.CATEGORY_ITEM, "DemonicSwordHandle", 5201).getInt(5201);
 		NetherObsidianSwordItemId = config.getItem(Configuration.CATEGORY_ITEM, "ObsidianSword", 5202).getInt(5202);
 		NetherWoodStickItemId = config.getItem(Configuration.CATEGORY_ITEM, "NetherWoodStick", 5203).getInt(5203);
 		NetherStoneBowlItemId = config.getItem(Configuration.CATEGORY_ITEM, "NetherStoneBowl", 5204).getInt(5204);
 		NetherSoulGlassBottleItemId = config.getItem(Configuration.CATEGORY_ITEM, "SoulGlassBottle", 5205).getInt(5205);
 
 		NetherObsidianSwordAcidItemId = config.getItem(Configuration.CATEGORY_ITEM, "ObsidianSwordAcid", 5206).getInt(5206);
 		NetherObsidianSwordDeathItemId = config.getItem(Configuration.CATEGORY_ITEM, "ObsidianSwordDeath", 5207).getInt(5207);
 
 		NetherPotionBottleItemId = config.getItem(Configuration.CATEGORY_ITEM, "PotionBottle", 5208).getInt(5208);
 		NetherStonePotionBowlItemId = config.getItem(Configuration.CATEGORY_ITEM, "PotionBowl", 5209).getInt(5209);
 
 		NetherSoulglassSwordItemId = config.getItem(Configuration.CATEGORY_ITEM, "SoulglassSword", 5210).getInt(5210);
 		NetherSoulglassSwordAcidItemId = config.getItem(Configuration.CATEGORY_ITEM, "SoulglassSwordAcid", 5211).getInt(5211);
 		NetherSoulglassSwordDeathItemId = config.getItem(Configuration.CATEGORY_ITEM, "SoulglassSwordDeath", 5212).getInt(5212);
 		NetherSoulglassSwordHellfireItemId = config.getItem(Configuration.CATEGORY_ITEM, "SoulglassSwordHellfire", 5213).getInt(5213);
 
 		NetherWoodCharcoalItemId = config.getItem(Configuration.CATEGORY_ITEM, "NetherWoodCharcoal", 5214).getInt(5214);
 		SoulEnergyBottleItemId = config.getItem(Configuration.CATEGORY_ITEM, "SoulEnergyPotion", 5215).getInt(5215);
 		NetherBowItemId = config.getItem(Configuration.CATEGORY_ITEM, "NetherBow", 5216).getInt(5216);
 		TorchArrowItemId = config.getItem(Configuration.CATEGORY_ITEM, "TorchArrow", 5217).getInt(5217);
 
 		NetherDiamondSwordItemId = config.getItem(Configuration.CATEGORY_ITEM, "DiamondSword", 5219).getInt(5219);
 		NetherDiamondSwordAcidItemId = config.getItem(Configuration.CATEGORY_ITEM, "DiamondSwordAcid", 5220).getInt(5220);
 		NetherDiamondSwordDeathItemId = config.getItem(Configuration.CATEGORY_ITEM, "DiamondSwordDeath", 5221).getInt(5221);
 
 		NetherGearItemId = config.getItem(Configuration.CATEGORY_ITEM, "Gears", 5222).getInt(5222);
 
 		SpawnSkeletonsAwayFromNetherFortresses = config.get(Configuration.CATEGORY_GENERAL, "SpawnSkeletonsAwayFromNetherFortresses", true).getBoolean(true);
 		IncreaseNetherrackHardness = config.get(Configuration.CATEGORY_GENERAL, "IncreaseNetherrackHardness", true).getBoolean(true);
 		SpawnBlazesNaturally = config.get(Configuration.CATEGORY_GENERAL, "SpawnBlazesNaturally", false).getBoolean(false);
 
 		bUseNetherOreDemonic = config.get("NetherOreGeneration", "Demonic", true).getBoolean(true);
 		bUseNetherOreCoal = config.get("NetherOreGeneration", "NetherCoal", true).getBoolean(true);
 		bUseNetherOreIron = config.get("NetherOreGeneration", "Iron", true).getBoolean(true);
 		bUseNetherOreGold = config.get("NetherOreGeneration", "Gold", true).getBoolean(true);
 		bUseNetherOreDiamond = config.get("NetherOreGeneration", "Diamond", true).getBoolean(true);
 		bUseNetherOreEmerald = config.get("NetherOreGeneration", "Emerald", true).getBoolean(true);
 		bUseNetherOreRedstone = config.get("NetherOreGeneration", "Redstone", true).getBoolean(true);
 		bUseNetherOreObsidian = config.get("NetherOreGeneration", "Obsidian", true).getBoolean(true);
 		bUseNetherOreLapis = config.get("NetherOreGeneration", "Lapis", true).getBoolean(true);
 		bUseNetherOreCobblestone = config.get("NetherOreGeneration", "Cobblestone", true).getBoolean(true);
 
 		NetherStuffsEventHook.nDetectRadius = config.get(Configuration.CATEGORY_GENERAL, "SoulBlockerRadius", 8).getInt();
 
 		NetherStuffsEventHook.SpawnSkeletonsOnlyOnNaturalNetherBlocks = config.get(Configuration.CATEGORY_GENERAL, "SpawnSkeletonsOnlyOnNaturalNetherBlocks", false)
 				.getBoolean(false);
 		// Add the Toggleable Spawn Allowing Blocks here (aka. Natural Nether
 		// Blocks)
 		NetherStuffsEventHook.lAllowedSpawnNetherBlockIds.add(Block.netherrack.blockID);
 		NetherStuffsEventHook.lAllowedSpawnNetherBlockIds.add(Block.slowSand.blockID);
 		NetherStuffsEventHook.lAllowedSpawnNetherBlockIds.add(Block.glowStone.blockID);
 		NetherStuffsEventHook.lAllowedSpawnNetherBlockIds.add(Block.netherBrick.blockID);
 		NetherStuffsEventHook.lAllowedSpawnNetherBlockIds.add(Block.netherFence.blockID);
 		NetherStuffsEventHook.lAllowedSpawnNetherBlockIds.add(Block.stairsNetherBrick.blockID);
 		NetherStuffsEventHook.lAllowedSpawnNetherBlockIds.add(Block.gravel.blockID);
 
 		String tmpString = config.get(
 				Configuration.CATEGORY_GENERAL,
 				"BlockIDNetherSpawningBlacklist",
 				NetherLeavesBlockId + "," + NetherSoulDetectorBlockId + "," + NetherSoulBlockerBlockId + "," + NetherSoulFurnaceBlockId + "," + SoulWorkBenchBlockId + ","
 						+ NetherDemonicFurnaceBlockId + "," + NetherSoulGlassBlockid + "," + NetherSoulGlassPaneBlockid + "," + NetherSoulBombBlockId + "," + SoulEngineBlockId + ","
 						+ SoulCondenserBlockId).getString();
 		String[] tmp = tmpString.split(",");
 		for (int i = 0; i < tmp.length; i++) {
 			int intVal = Integer.parseInt(tmp[i].trim());
 			if (intVal > 0 && intVal < 4096)
 				NetherStuffsEventHook.lBlockSpawnListForced.add(intVal);
 		}
 
 		FMLLog.info("[NetherStuffs] Blocked Nether Spawns on Block IDs: %s", NetherStuffsEventHook.lBlockSpawnListForced.toString());
 		config.save();
 
 		if (ShowOreDistributions) {
 			WorldGenDefaultMinable.arrMap.put(new String(BlockRegistry.netherOre.blockID + ":" + Ore.demonicOre), 1);
 			WorldGenDefaultMinable.arrMap.put(new String(BlockRegistry.netherOre.blockID + ":" + Ore.netherOreCoal), 1);
 			WorldGenDefaultMinable.arrMap.put(new String(BlockRegistry.netherOre.blockID + ":" + Ore.netherOreIron), 1);
 			WorldGenDefaultMinable.arrMap.put(new String(BlockRegistry.netherOre.blockID + ":" + Ore.netherOreGold), 1);
 			WorldGenDefaultMinable.arrMap.put(new String(BlockRegistry.netherOre.blockID + ":" + Ore.netherOreDiamond), 1);
 			WorldGenDefaultMinable.arrMap.put(new String(BlockRegistry.netherOre.blockID + ":" + Ore.netherOreEmerald), 1);
 			WorldGenDefaultMinable.arrMap.put(new String(BlockRegistry.netherOre.blockID + ":" + Ore.netherOreRedstone), 1);
 			WorldGenDefaultMinable.arrMap.put(new String(BlockRegistry.netherOre.blockID + ":" + Ore.netherOreObsidian), 1);
 			WorldGenDefaultMinable.arrMap.put(new String(BlockRegistry.netherOre.blockID + ":" + Ore.netherOreLapis), 1);
 			WorldGenDefaultMinable.arrMap.put(new String(BlockRegistry.netherOre.blockID + ":" + Ore.netherOreCobblestone), 1);
 		}
 	}
 
 	private void initHarkenScytheCompatibility() {
 		if (bUseHarkenScytheCondenserBlock == false)
 			return;
 
 		FMLLog.info("[NetherStuffs] adding Stuff for HarkenScythe use");
 		// 90 SoulEnergy should be 1 Soul, but as its working differently i use 250 Energy = 1 Soul
 		int nSoulVessel = 0;
 		int nSoulKeeper = 0;
 		int nEssenceVessel = 0;
 		int nEssenceKeeper = 0;
 		for (int itemID = 255; itemID < Item.itemsList.length; itemID++) {
 			if (Item.itemsList[itemID] != null) {
 				if (Item.itemsList[itemID].getUnlocalizedName().equalsIgnoreCase("item.HSSoulkeeper"))
 					nSoulKeeper = itemID;
 				else if (Item.itemsList[itemID].getUnlocalizedName().equalsIgnoreCase("item.HSSoulVessel"))
 					nSoulVessel = itemID;
 				else if (Item.itemsList[itemID].getUnlocalizedName().equalsIgnoreCase("item.HSEssenceKeeper"))
 					nEssenceKeeper = itemID;
 				else if (Item.itemsList[itemID].getUnlocalizedName().equalsIgnoreCase("item.HSEssenceVessel"))
 					nEssenceVessel = itemID;
 
 				if (nSoulVessel != 0 && nSoulKeeper != 0 && nEssenceKeeper != 0 && nEssenceVessel != 0)
 					break;
 			}
 		}
 
 		if (nSoulVessel == 0 || nSoulKeeper == 0 || nEssenceKeeper == 0 || nEssenceVessel == 0) {
 			FMLLog.warning("[NetherStuffs] Could not find HarkenScythe items - Soulenergy Condenser disabled");
 			return;
 		}
 
 		Block SoulCondenser = new SoulCondenser(SoulCondenserBlockId, nSoulKeeper, nSoulVessel, nEssenceKeeper, nEssenceVessel).setUnlocalizedName("SoulCondenser").setHardness(3.5F)
 				.setResistance(5.0F);
 		GameRegistry.registerBlock(SoulCondenser, SoulCondenserItemBlock.class, "SoulCondenser");
 		GameRegistry.registerTileEntity(TileSoulCondenser.class, "tileEntitySoulCondenser");
 
 		SoulWorkBenchRecipes.instance.addRecipe(new ItemStack(SoulCondenser, 1, 0), 250, new Object[] { "III", "G G", "GPG", 'I', new ItemStack(ItemRegistry.NetherOreIngot, 1, 0),
 				'G', new ItemStack(Block.ice, 1, 0), 'P', new ItemStack(Block.pistonBase, 1, 0) });
 
 		SoulWorkBenchRecipes.instance.addRecipe(new ItemStack(SoulCondenser, 1, 1), 250, new Object[] { "III", "F F", "GPG", 'I', new ItemStack(ItemRegistry.NetherOreIngot, 1, 0),
 				'F', new ItemStack(Item.flintAndSteel), 'G', new ItemStack(nEssenceKeeper, 1, 0), 'P', new ItemStack(Block.pistonBase, 1, 0) });
 	}
 
 	private void initTConstruct() {
 		if (bUseTConstruct == false)
 			return;
 
 		FMLLog.info("[NetherStuffs] Trying to register Ores for TConstruct Smelter Recipes");
 
 		LiquidStack liquid = LiquidDictionary.getLiquid("Molten Iron", 216); // actually 1.5 ingots
 		if (liquid != null)
 			Smeltery.instance.addMelting(BlockRegistry.netherOre, Ore.netherOreIron, 800, liquid);
 
 		liquid = LiquidDictionary.getLiquid("Molten Gold", 216); // actually 1.5 ingots
 		if (liquid != null)
 			Smeltery.instance.addMelting(BlockRegistry.netherOre, Ore.netherOreGold, 800, liquid);
 
 		liquid = LiquidDictionary.getLiquid("Molten Obsidian", 216); // actually 1.5 ingots
 		if (liquid != null)
 			Smeltery.instance.addMelting(BlockRegistry.netherOre, Ore.netherOreObsidian, 800, liquid);
 
 		liquid = LiquidDictionary.getLiquid("Molten DemonicIngot", 216); // actually 1.5 ingots
 		if (liquid != null) {
 			int nIngotPatternItemId = 0;
 			for (int itemID = 255; itemID < Item.itemsList.length; itemID++) {
 				if (Item.itemsList[itemID] != null) {
 					if (Item.itemsList[itemID].getUnlocalizedName().equalsIgnoreCase("item.tconstruct.MetalPattern")) {
 						Smeltery.instance.addMelting(BlockRegistry.netherOre, Ore.demonicOre, 800, liquid);
 						nIngotPatternItemId = itemID;
 						liquid = LiquidDictionary.getLiquid("Molten DemonicIngot", 144);
 						TConstructRegistry.getTableCasting().addCastingRecipe(new ItemStack(ItemRegistry.NetherOreIngot, 1, 0), liquid, new ItemStack(nIngotPatternItemId, 1, 0), 100);
 						break;
 					}
 				}
 			}
 		}
 	}
 
 	private void initDecorativeBlocks() {
 
 		if (bUseFences) {
 			int nNetherFenceId = Block.netherFence.blockID;
 			Block.blocksList[nNetherFenceId] = null;
 			(new NetherFence(nNetherFenceId, "netherBrick", Material.rock)).setHardness(2.0F).setResistance(10.0F).setStepSound(Block.soundStoneFootstep)
 					.setUnlocalizedName("netherFence");
 			BlockRegistry.FenceHellfire = (new NetherFence(NetherFenceHellfireBlockId, "netherBrick", NetherMaterials.netherWood)).setUnlocalizedName("NetherFenceHellfire");
 			BlockRegistry.FenceAcid = (new NetherFence(NetherFenceAcidBlockId, "netherBrick", NetherMaterials.netherWood)).setUnlocalizedName("NetherFenceAcid");
 			BlockRegistry.FenceDeath = (new NetherFence(NetherFenceDeathBlockId, "netherBrick", NetherMaterials.netherWood)).setUnlocalizedName("NetherFenceDeath");
 			GameRegistry.registerBlock(BlockRegistry.FenceHellfire, "NetherFenceHellfireItemBlock");
 			GameRegistry.registerBlock(BlockRegistry.FenceAcid, "NetherFenceAcidItemBlock");
 			GameRegistry.registerBlock(BlockRegistry.FenceDeath, "NetherFenceDeathItemBlock");
 		}
 
 		if (bUseFenceGates) {
 			BlockRegistry.FenceGateNetherBricks = new NetherFenceGate(NetherFenceGateNetherBricksBlockId).setUnlocalizedName("NetherBrickFenceGate");
 			BlockRegistry.FenceGateHellfire = new NetherFenceGate(NetherFenceGateHellfireBlockId).setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundWoodFootstep)
 					.setUnlocalizedName("HellfireFenceGate");
 			BlockRegistry.FenceGateAcid = new NetherFenceGate(NetherFenceGateAcidBlockId).setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundWoodFootstep)
 					.setUnlocalizedName("AcidFenceGate");
 			BlockRegistry.FenceGateDeath = new NetherFenceGate(NetherFenceGateDeathBlockId).setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundWoodFootstep)
 					.setUnlocalizedName("DeathFenceGate");
 
 			GameRegistry.registerBlock(BlockRegistry.FenceGateNetherBricks, "NetherFenceGateBrickItemBlock");
 			GameRegistry.registerBlock(BlockRegistry.FenceGateHellfire, "NetherFenceGateHellfireItemBlock");
 			GameRegistry.registerBlock(BlockRegistry.FenceGateAcid, "NetherFenceGateAcidItemBlock");
 			GameRegistry.registerBlock(BlockRegistry.FenceGateDeath, "NetherFenceGateDeathItemBlock");
 			LanguageRegistry.instance().addStringLocalization("tile.NetherBrickFenceGate.name", "Netherbrick Fence Gate");
 			LanguageRegistry.instance().addStringLocalization("tile.HellfireFenceGate.name", "Hellfirewood Fence Gate");
 			LanguageRegistry.instance().addStringLocalization("tile.AcidFenceGate.name", "Acidwood Fence Gate");
 			LanguageRegistry.instance().addStringLocalization("tile.DeathFenceGate.name", "Deathwood Fence Gate");
 		}
 
 		if (bUseStairBlocks) {
 			BlockRegistry.StairAcid = new NetherStairs(NetherStairAcidBlockId, BlockRegistry.netherPlank, Plank.acid).setUnlocalizedName("StairAcid");
 			BlockRegistry.StairHellfire = new NetherStairs(NetherStairHellfireBlockId, BlockRegistry.netherPlank, Plank.hellfire).setUnlocalizedName("StairHellfire");
 			BlockRegistry.StairDeath = new NetherStairs(NetherStairDeathBlockId, BlockRegistry.netherPlank, Plank.death).setUnlocalizedName("StairDeath");
 			GameRegistry.registerBlock(BlockRegistry.StairAcid, "NetherStairAcidItemBlock");
 			GameRegistry.registerBlock(BlockRegistry.StairHellfire, "NetherStairHellfireItemBlock");
 			GameRegistry.registerBlock(BlockRegistry.StairDeath, "NetherStairDeathItemBlock");
 
 			MinecraftForge.setBlockHarvestLevel(BlockRegistry.StairAcid, "axe", 1);
 			MinecraftForge.setBlockHarvestLevel(BlockRegistry.StairHellfire, "axe", 1);
 			MinecraftForge.setBlockHarvestLevel(BlockRegistry.StairDeath, "axe", 1);
 
 			GameRegistry.addRecipe(new ItemStack(BlockRegistry.StairAcid, 4), new Object[] { "#  ", "## ", "###", '#', new ItemStack(BlockRegistry.netherPlank, 1, Plank.acid) });
 			GameRegistry.addRecipe(new ItemStack(BlockRegistry.StairHellfire, 4), new Object[] { "#  ", "## ", "###", '#',
 					new ItemStack(BlockRegistry.netherPlank, 1, Plank.hellfire) });
 			GameRegistry
 					.addRecipe(new ItemStack(BlockRegistry.StairDeath, 4), new Object[] { "#  ", "## ", "###", '#', new ItemStack(BlockRegistry.netherPlank, 1, Plank.death) });
 
 			LanguageRegistry.instance().addStringLocalization("tile.StairAcid.name", "Acid Wood Stairs");
 			LanguageRegistry.instance().addStringLocalization("tile.StairHellfire.name", "Hellfire Wood Stairs");
 			LanguageRegistry.instance().addStringLocalization("tile.StairDeath.name", "Death Wood Stairs");
 
 			OreDictionary.registerOre("stairWood", BlockRegistry.StairAcid);
 			OreDictionary.registerOre("stairWood", BlockRegistry.StairHellfire);
 			OreDictionary.registerOre("stairWood", BlockRegistry.StairDeath);
 		}
 
 		if (bUseSlabBlocks) {
 
 			BlockRegistry.HalfSlab = (NetherSlab) new NetherSlab(NetherHalfSlabBlockId, false).setUnlocalizedName("HalfSlab");
 			BlockRegistry.DoubleSlab = (NetherSlab) new NetherSlab(NetherDoubleSlabBlockId, true).setUnlocalizedName("DoubleSlab");
 
 			GameRegistry.registerBlock(BlockRegistry.HalfSlab, NetherHalfSlabItemBlock.class, "NetherSlabHalfItemBlock");
 			GameRegistry.registerBlock(BlockRegistry.DoubleSlab, NetherDoubleSlabItemBlock.class, "NetherSlabDoubleItemBlock");
 
 			MinecraftForge.setBlockHarvestLevel(BlockRegistry.HalfSlab, "axe", 1);
 			MinecraftForge.setBlockHarvestLevel(BlockRegistry.DoubleSlab, "axe", 1);
 
 			OreDictionary.registerOre("slabWood", new ItemStack(BlockRegistry.HalfSlab, 1, OreDictionary.WILDCARD_VALUE));
 
 			for (int i = 0; i < PlankItemBlock.blockNames.length; i++) {
 				GameRegistry.addRecipe(new ItemStack(BlockRegistry.HalfSlab, 6, i), new Object[] { "###", '#', new ItemStack(BlockRegistry.netherPlank, 1, i) });
 
 				MinecraftForge.setBlockHarvestLevel(BlockRegistry.HalfSlab, i, "axe", 1);
 				MinecraftForge.setBlockHarvestLevel(BlockRegistry.DoubleSlab, i, "axe", 1);
 
 				LanguageRegistry.instance().addStringLocalization("tile.HalfSlab." + PlankItemBlock.blockNames[i] + ".name", PlankItemBlock.blockNames[i] + " Slab");
 				LanguageRegistry.instance()
 						.addStringLocalization("tile.DoubleSlab." + PlankItemBlock.blockNames[i] + ".name", PlankItemBlock.blockNames[i] + " Doubleslab");
 			}
 		}
 
 	}
 
 	private void initThaumcraftAPI() {
 		if (bUseThaumcraft == false)
 			return;
 
 		FMLLog.info("[NetherStuffs] adding Stuff for Thaumcraft use");
 
 		ThaumcraftApi.registerObjectTag(BlockRegistry.netherOre.blockID, Ore.demonicOre, (new ObjectTags()).add(EnumTag.ROCK, 1).add(EnumTag.DESTRUCTION, 1)
 				.add(EnumTag.EVIL, 4).add(EnumTag.METAL, 2).add(EnumTag.FLUX, 2));
 		ThaumcraftApi.registerObjectTag(BlockRegistry.netherOre.blockID, Ore.netherOreCoal,
 				(new ObjectTags()).add(EnumTag.ROCK, 1).add(EnumTag.DESTRUCTION, 1).add(EnumTag.FIRE, 3));
 		ThaumcraftApi.registerObjectTag(BlockRegistry.netherOre.blockID, Ore.netherOreIron,
 				(new ObjectTags()).add(EnumTag.ROCK, 1).add(EnumTag.DESTRUCTION, 1).add(EnumTag.METAL, 6));
 		ThaumcraftApi.registerObjectTag(BlockRegistry.netherOre.blockID, Ore.netherStone, (new ObjectTags()).add(EnumTag.ROCK, 2));
 		ThaumcraftApi.registerObjectTag(BlockRegistry.netherOre.blockID, Ore.netherOreGold,
 				(new ObjectTags()).add(EnumTag.ROCK, 1).add(EnumTag.METAL, 6).add(EnumTag.VALUABLE, 4));
 		ThaumcraftApi.registerObjectTag(BlockRegistry.netherOre.blockID, Ore.netherOreDiamond,
 				(new ObjectTags()).add(EnumTag.ROCK, 1).add(EnumTag.DESTRUCTION, 1).add(EnumTag.VALUABLE, 4));
 		ThaumcraftApi.registerObjectTag(BlockRegistry.netherOre.blockID, Ore.netherOreEmerald,
 				(new ObjectTags()).add(EnumTag.ROCK, 1).add(EnumTag.DESTRUCTION, 1).add(EnumTag.VALUABLE, 2));
 		ThaumcraftApi.registerObjectTag(BlockRegistry.netherOre.blockID, Ore.netherOreRedstone,
 				(new ObjectTags()).add(EnumTag.ROCK, 1).add(EnumTag.DESTRUCTION, 1).add(EnumTag.POWER, 1).add(EnumTag.MECHANISM, 1));
 		ThaumcraftApi.registerObjectTag(BlockRegistry.netherOre.blockID, Ore.netherOreObsidian,
 				(new ObjectTags()).add(EnumTag.ROCK, 5).add(EnumTag.DESTRUCTION, 1).add(EnumTag.DARK, 1));
 		ThaumcraftApi.registerObjectTag(BlockRegistry.netherOre.blockID, Ore.netherOreLapis,
 				(new ObjectTags()).add(EnumTag.ROCK, 1).add(EnumTag.DESTRUCTION, 1).add(EnumTag.VALUABLE, 4));
 		ThaumcraftApi.registerObjectTag(BlockRegistry.netherOre.blockID, Ore.netherOreCobblestone, (new ObjectTags()).add(EnumTag.ROCK, 1).add(EnumTag.DESTRUCTION, 1));
 
 		ThaumcraftApi.registerObjectTag(BlockRegistry.netherWood.blockID, Wood.hellfire, (new ObjectTags()).add(EnumTag.FIRE, 4).add(EnumTag.WOOD, 8).add(EnumTag.EVIL, 1));
 		ThaumcraftApi.registerObjectTag(BlockRegistry.netherWood.blockID, Wood.death, (new ObjectTags()).add(EnumTag.DEATH, 4).add(EnumTag.WOOD, 8).add(EnumTag.EVIL, 1));
 		ThaumcraftApi.registerObjectTag(BlockRegistry.netherWood.blockID, Wood.acid, (new ObjectTags()).add(EnumTag.POISON, 4).add(EnumTag.WOOD, 8).add(EnumTag.EVIL, 1));
 
 		ThaumcraftApi.registerComplexObjectTag(BlockRegistry.SoulGlass.blockID, -1, (new ObjectTags()).add(EnumTag.CRYSTAL, 3).add(EnumTag.SPIRIT, 1));
 		ThaumcraftApi.registerComplexObjectTag(BlockRegistry.SoulGlassPane.blockID, -1, (new ObjectTags()).add(EnumTag.CRYSTAL, 1));
 
 		ThaumcraftApi.registerObjectTag(BlockRegistry.Sapling.blockID, Sapling.hellfire, (new ObjectTags()).add(EnumTag.FIRE, 1).add(EnumTag.WOOD, 2).add(EnumTag.EVIL, 1));
 		ThaumcraftApi.registerObjectTag(BlockRegistry.Sapling.blockID, Sapling.death, (new ObjectTags()).add(EnumTag.DEATH, 1).add(EnumTag.WOOD, 2).add(EnumTag.EVIL, 1));
 		ThaumcraftApi.registerObjectTag(BlockRegistry.Sapling.blockID, Sapling.acid, (new ObjectTags()).add(EnumTag.POISON, 1).add(EnumTag.WOOD, 2).add(EnumTag.EVIL, 1));
 
 		ThaumcraftApi.registerComplexObjectTag(ItemRegistry.NetherOreIngot.itemID, -1, (new ObjectTags()).add(EnumTag.EVIL, 1).add(EnumTag.METAL, 4).add(EnumTag.FLUX, 1));
 
 		ThaumcraftApi.registerObjectTag(ItemRegistry.NetherStonePotionBowl.itemID, 0, (new ObjectTags()).add(EnumTag.EVIL, 1).add(EnumTag.FIRE, 5));
 		ThaumcraftApi.registerObjectTag(ItemRegistry.NetherStonePotionBowl.itemID, 1, (new ObjectTags()).add(EnumTag.EVIL, 1).add(EnumTag.POISON, 5));
 		ThaumcraftApi.registerObjectTag(ItemRegistry.NetherStonePotionBowl.itemID, 2, (new ObjectTags()).add(EnumTag.EVIL, 1).add(EnumTag.DEATH, 5));
 
 		ThaumcraftApi.registerObjectTag(ItemRegistry.NetherPotionBottle.itemID, PotionBottle.hellfire, (new ObjectTags()).add(EnumTag.EVIL, 2).add(EnumTag.FIRE, 10));
 		ThaumcraftApi.registerObjectTag(ItemRegistry.NetherPotionBottle.itemID, PotionBottle.acid, (new ObjectTags()).add(EnumTag.EVIL, 2).add(EnumTag.POISON, 10));
 		ThaumcraftApi.registerObjectTag(ItemRegistry.NetherPotionBottle.itemID, PotionBottle.death, (new ObjectTags()).add(EnumTag.EVIL, 2).add(EnumTag.DEATH, 10));
 
 		ThaumcraftApi.registerObjectTag(ItemRegistry.SoulEnergyBottle.itemID, SoulEnergyBottle.small,
 				(new ObjectTags()).add(EnumTag.EVIL, 2).add(EnumTag.POWER, 2).add(EnumTag.SPIRIT, 4));
 		ThaumcraftApi.registerObjectTag(ItemRegistry.SoulEnergyBottle.itemID, SoulEnergyBottle.medium,
 				(new ObjectTags()).add(EnumTag.EVIL, 3).add(EnumTag.POWER, 3).add(EnumTag.SPIRIT, 8));
 		ThaumcraftApi.registerObjectTag(ItemRegistry.SoulEnergyBottle.itemID, SoulEnergyBottle.large,
 				(new ObjectTags()).add(EnumTag.EVIL, 4).add(EnumTag.POWER, 4).add(EnumTag.SPIRIT, 16));
 
 		ThaumcraftApi.registerComplexObjectTag(ItemRegistry.NetherSoulGlassBottleItem.itemID, -1, (new ObjectTags()).add(EnumTag.CRYSTAL, 1).add(EnumTag.VOID, 1));
 		ThaumcraftApi.registerComplexObjectTag(ItemRegistry.NetherStoneBowl.itemID, -1, (new ObjectTags()).add(EnumTag.VOID, 1));
 
 		ThaumcraftApi.registerObjectTag(BlockRegistry.SoulFurnace.blockID, -1, (new ObjectTags()).add(EnumTag.TRAP, 2).add(EnumTag.EVIL, 5).add(EnumTag.SPIRIT, 5));
 		ThaumcraftApi.registerObjectTag(BlockRegistry.DemonicFurnace.blockID, -1, (new ObjectTags()).add(EnumTag.TRAP, 1).add(EnumTag.EVIL, 2).add(EnumTag.SPIRIT, 2));
 
 		ThaumcraftApi.registerObjectTag(BlockRegistry.SoulBomb.blockID, -1, (new ObjectTags()).add(EnumTag.TRAP, 5).add(EnumTag.EVIL, 5).add(EnumTag.SPIRIT, 5).add(EnumTag.DEATH, 3));
 
 		ThaumcraftApi.registerComplexObjectTag(BlockRegistry.SoulDetector.blockID, SoulDetector.mk1,
 				(new ObjectTags()).add(EnumTag.MECHANISM, 2).add(EnumTag.CONTROL, 2).add(EnumTag.VISION, 1));
 		ThaumcraftApi.registerComplexObjectTag(BlockRegistry.SoulDetector.blockID, SoulDetector.mk2,
 				(new ObjectTags()).add(EnumTag.MECHANISM, 4).add(EnumTag.CONTROL, 4).add(EnumTag.VISION, 2));
 		ThaumcraftApi.registerComplexObjectTag(BlockRegistry.SoulDetector.blockID, SoulDetector.mk3,
 				(new ObjectTags()).add(EnumTag.MECHANISM, 6).add(EnumTag.CONTROL, 6).add(EnumTag.VISION, 3));
 		ThaumcraftApi.registerComplexObjectTag(BlockRegistry.SoulDetector.blockID, SoulDetector.mk4,
 				(new ObjectTags()).add(EnumTag.MECHANISM, 8).add(EnumTag.CONTROL, 8).add(EnumTag.VISION, 4));
 
 		ThaumcraftApi.registerComplexObjectTag(BlockRegistry.SoulSiphon.blockID, SoulSiphon.mk1, (new ObjectTags()).add(EnumTag.TRAP, 3).add(EnumTag.WEAPON, 3).add(EnumTag.SPIRIT, 3)
 				.add(EnumTag.VOID, 1));
 		ThaumcraftApi.registerComplexObjectTag(BlockRegistry.SoulSiphon.blockID, SoulSiphon.mk2, (new ObjectTags()).add(EnumTag.TRAP, 5).add(EnumTag.WEAPON, 5).add(EnumTag.SPIRIT, 6)
 				.add(EnumTag.VOID, 2));
 		ThaumcraftApi.registerComplexObjectTag(BlockRegistry.SoulSiphon.blockID, SoulSiphon.mk3, (new ObjectTags()).add(EnumTag.TRAP, 7).add(EnumTag.WEAPON, 7).add(EnumTag.SPIRIT, 9)
 				.add(EnumTag.VOID, 3));
 		ThaumcraftApi.registerComplexObjectTag(BlockRegistry.SoulSiphon.blockID, SoulSiphon.mk4,
 				(new ObjectTags()).add(EnumTag.TRAP, 10).add(EnumTag.WEAPON, 10).add(EnumTag.SPIRIT, 12).add(EnumTag.VOID, 4));
 	}
 
 	private void initBuildcraftStuff() {
 		if (bUseSoulEngineBlock == false)
 			return;
 
 		FMLLog.info("[NetherStuffs] adding Stuff for Buildcraft use");
 		Block SoulEngine = new SoulEngine(NetherStuffs.SoulEngineBlockId).setUnlocalizedName("NetherSoulEngine").setHardness(3.5F).setResistance(5.0F);
 		Item NetherGear = new DemonicGear(NetherGearItemId).setUnlocalizedName("NetherGear");
 
 		GameRegistry.registerBlock(SoulEngine, "SoulEngine");
 		SoulEngineFuel.fuels.add(new SoulEngineFuel(new LiquidStack(BlockRegistry.LiquidStill.blockID, 1, 0), 1, 10000)); // 20000 = Lava
 		GameRegistry.registerTileEntity(TileSoulEngine.class, "tileEntitySoulEnergyEngine");
 
 		SoulWorkBenchRecipes.instance.addRecipe(new ItemStack(NetherGear, 1, 0), 50, " S ", "SIS", " S ", 'S', new ItemStack(ItemRegistry.NetherWoodStick, 1, 0), 'I', new ItemStack(
 				ItemRegistry.NetherOreIngot, 1, 0));
 
 		SoulWorkBenchRecipes.instance.addRecipe(new ItemStack(SoulEngine, 1, 0), 250, new Object[] { "III", " S ", "GPG", 'I', new ItemStack(ItemRegistry.NetherOreIngot, 1, 0), 'S',
 				new ItemStack(BlockRegistry.SoulGlass, 1, 0), 'G', new ItemStack(NetherGear, 1, 0), 'P', new ItemStack(Block.pistonBase, 1, 0) });
 	}
 
 	@Init
 	public void load(FMLInitializationEvent event) {
 		bBuildcraftAvailable = Loader.isModLoaded("BuildCraft|Core") && Loader.isModLoaded("BuildCraft|Energy");
 		bHarkenScytheAvailable = Loader.isModLoaded("HarkenScythe_Core");
 		bThaumcraftAvailable = Loader.isModLoaded("Thaumcraft");
 		bTConstructAvailable = Loader.isModLoaded("TConstruct");
 
 		ItemRegistry.init();
 
 		GameRegistry.registerBlock(BlockRegistry.SoulGlass, "NetherSoulGlass");
 		GameRegistry.registerBlock(BlockRegistry.SoulGlassPane, "NetherSoulGlassPane");
 		GameRegistry.registerBlock(BlockRegistry.DemonicFurnace, "NetherDemonicFurnace");
 		GameRegistry.registerBlock(BlockRegistry.SoulFurnace, "NetherSoulFurnace");
 		GameRegistry.registerBlock(BlockRegistry.SoulWorkBench, "NetherSoulWorkBench");
 
 		//GameRegistry.registerBlock(BlockRegistry.Lamp, "NetherLamp");
 		
 		GameRegistry.registerBlock(BlockRegistry.netherOre, OreItemBlock.class, "NetherOreItemBlock");
 		GameRegistry.registerBlock(BlockRegistry.netherWood, WoodItemBlock.class, "NetherWoodItemBlock");
 		GameRegistry.registerBlock(BlockRegistry.netherPlank, PlankItemBlock.class, "NetherPlankItemBlock");
 		GameRegistry.registerBlock(BlockRegistry.netherLeaves, LeafItemBlock.class, "NetherLeavesItemBlock");
 		GameRegistry.registerBlock(BlockRegistry.Sapling, SaplingItemBlock.class, "NetherSaplingItemBlock");
 		GameRegistry.registerBlock(BlockRegistry.SoulBomb, SoulBombItemBlock.class, "NetherSoulBombItemBlock");
 		GameRegistry.registerBlock(BlockRegistry.SoulDetector, SoulDetectorItemBlock.class, "NetherSoulDetectorItemBlock");
 		GameRegistry.registerBlock(BlockRegistry.SoulBlocker, SoulBlockerItemBlock.class, "NetherSoulBlockerItemBlock");
 		GameRegistry.registerBlock(BlockRegistry.SoulSiphon, SoulSiphonItemBlock.class, "NetherSoulSiphonItemBlock");
 		GameRegistry.registerBlock(BlockRegistry.netherWoodPuddle, NetherWoodPuddleItemBlock.class, "NetherWoodPuddleItemBlock");
 
 		GameRegistry.registerBlock(BlockRegistry.LiquidFlowing, LiquidItemBlock.class, "fuelFlow");
 		GameRegistry.registerBlock(BlockRegistry.LiquidStill, LiquidItemBlock.class, "fuelStill");
 		GameRegistry.registerTileEntity(LiquidTextureLogic.class, "LiquidTextureLogic");
 
 		// set required Stuff to Gather
 		MinecraftForge.setBlockHarvestLevel(BlockRegistry.netherOre, Ore.netherOreCobblestone, "pickaxe", 0);
 		MinecraftForge.setBlockHarvestLevel(BlockRegistry.netherOre, Ore.netherStone, "pickaxe", 1);
 		MinecraftForge.setBlockHarvestLevel(BlockRegistry.netherOre, Ore.netherOreCoal, "pickaxe", 1);
 		MinecraftForge.setBlockHarvestLevel(BlockRegistry.netherOre, Ore.netherOreIron, "pickaxe", 1);
 		MinecraftForge.setBlockHarvestLevel(BlockRegistry.netherOre, Ore.demonicOre, "pickaxe", 2);
 		MinecraftForge.setBlockHarvestLevel(BlockRegistry.netherOre, Ore.netherOreDiamond, "pickaxe", 2);
 		MinecraftForge.setBlockHarvestLevel(BlockRegistry.netherOre, Ore.netherOreEmerald, "pickaxe", 2);
 		MinecraftForge.setBlockHarvestLevel(BlockRegistry.netherOre, Ore.netherOreGold, "pickaxe", 2);
 		MinecraftForge.setBlockHarvestLevel(BlockRegistry.netherOre, Ore.netherOreRedstone, "pickaxe", 2);
 		MinecraftForge.setBlockHarvestLevel(BlockRegistry.netherOre, Ore.netherOreLapis, "pickaxe", 2);
 		MinecraftForge.setBlockHarvestLevel(BlockRegistry.netherOre, Ore.netherOreObsidian, "pickaxe", 3);
 
 		MinecraftForge.setBlockHarvestLevel(BlockRegistry.DemonicFurnace, "pickaxe", 1);
 		MinecraftForge.setBlockHarvestLevel(BlockRegistry.SoulBlocker, "pickaxe", 1);
 		MinecraftForge.setBlockHarvestLevel(BlockRegistry.SoulDetector, "pickaxe", 1);
 		MinecraftForge.setBlockHarvestLevel(BlockRegistry.SoulSiphon, "pickaxe", 1);
 
 		MinecraftForge.setBlockHarvestLevel(BlockRegistry.SoulWorkBench, "axe", 0);
 		MinecraftForge.setBlockHarvestLevel(BlockRegistry.netherWood, "axe", 2);
 		MinecraftForge.setBlockHarvestLevel(BlockRegistry.netherWoodPuddle, "axe", 2);
 
 		MinecraftForge.setBlockHarvestLevel(BlockRegistry.netherWood, Wood.acid, "axe", 2);
 		MinecraftForge.setBlockHarvestLevel(BlockRegistry.netherWood, Wood.hellfire, "axe", 2);
 		MinecraftForge.setBlockHarvestLevel(BlockRegistry.netherWood, Wood.death, "axe", 2);
 
 		MinecraftForge.setBlockHarvestLevel(BlockRegistry.netherPlank, "axe", 1);
 
 		MinecraftForge.setBlockHarvestLevel(BlockRegistry.netherPlank, Plank.acid, "axe", 1);
 		MinecraftForge.setBlockHarvestLevel(BlockRegistry.netherPlank, Plank.hellfire, "axe", 1);
 		MinecraftForge.setBlockHarvestLevel(BlockRegistry.netherPlank, Plank.death, "axe", 1);
 
 		MinecraftForge.removeBlockEffectiveness(BlockRegistry.netherOre, "pickaxe");
 
 		GameRegistry.registerTileEntity(TileDemonicFurnace.class, "tileEntityNetherStuffsDemonicFurnace");
 		GameRegistry.registerTileEntity(TileSoulFurnace.class, "tileEntityNetherStuffsSoulFurnace");
 		GameRegistry.registerTileEntity(TileSoulWorkBench.class, "tileEntityNetherStuffsSoulWorkBench");
 		GameRegistry.registerTileEntity(TileSoulDetector.class, "tileEntityNetherStuffsSoulDetector");
 		GameRegistry.registerTileEntity(TileSoulBlocker.class, "tileEntityNetherStuffsSoulBlocker");
 		GameRegistry.registerTileEntity(TileSoulSiphon.class, "tileEntityNetherStuffsSoulSiphon");
 		GameRegistry.registerTileEntity(TileNetherWoodPuddle.class, "tileEntityNetherWood");
 
 		GameRegistry.registerFuelHandler(new NetherStuffsFuel());
 		EntityRegistry.registerModEntity(EntityTorchArrow.class, "TorchArrow", 1, instance, 128, 3, true);
 		EntityRegistry.registerModEntity(EntitySoulBombPrimed.class, "SoulBomb", 2, instance, 160, 10, true);
 
 		OreDictionary.registerOre("oreDemonic", new ItemStack(BlockRegistry.netherOre, 1, Ore.demonicOre));
 		OreDictionary.registerOre("ingotDemonic", new ItemStack(ItemRegistry.NetherOreIngot));
 		OreDictionary.registerOre("logWood", new ItemStack(BlockRegistry.netherWood, 1, OreDictionary.WILDCARD_VALUE));
 		OreDictionary.registerOre("plankWood", new ItemStack(BlockRegistry.netherPlank, 1, OreDictionary.WILDCARD_VALUE));
 
 		SoulEnergyLiquid = LiquidDictionary.getOrCreateLiquid("SoulEnergy", new LiquidStack(BlockRegistry.LiquidStill.blockID, LiquidContainerRegistry.BUCKET_VOLUME, 0));
 		DemonicIngotLiquid = LiquidDictionary.getOrCreateLiquid("Molten DemonicIngot", new LiquidStack(BlockRegistry.LiquidStill.blockID, LiquidContainerRegistry.BUCKET_VOLUME, 1));
 		
 		registerWorldGenerators();
 		initRecipes();
 		initDecorativeBlocks();
 
 		initLanguageRegistry();
 		proxy.registerRenderThings();
 		NetworkRegistry.instance().registerGuiHandler(this, guiHandler);
 
 		MinecraftForge.EVENT_BUS.register(new NetherStuffsEventHook());
 
 		// set Netherrack hardness to different values
 		if (IncreaseNetherrackHardness) {
 			Block.netherrack.setHardness(1.5F);
 			Block.netherrack.setResistance(2.0F);
 		}
 		/*
 		 * This lets Skeletons Spawn away from NetherFortresses. Actual Idea by ErasmoGnome, made on the MinecraftForums: http://www.minecraftforum.net
 		 * /topic/1493398-move-wither-skeletons-out- of-fortresses-over-125-supporters/
 		 */
 		if (SpawnSkeletonsAwayFromNetherFortresses)
 			EntityRegistry.addSpawn(EntitySkeleton.class, 40, 4, 4, EnumCreatureType.monster, BiomeGenBase.hell);
 		if (SpawnBlazesNaturally)
 			EntityRegistry.addSpawn(EntityBlaze.class, 5, 1, 1, EnumCreatureType.monster, BiomeGenBase.hell);
 	}
 
 	private void registerWorldGenerators() {
 		if (bUseNetherOreDemonic)
 			GameRegistry.registerWorldGenerator(new WorldGenDefaultMinable(BlockRegistry.netherOre.blockID, 8, Ore.demonicOre, 10));
 		if (bUseNetherOreCoal)
 			GameRegistry.registerWorldGenerator(new WorldGenDefaultMinable(BlockRegistry.netherOre.blockID, 16, Ore.netherOreCoal, 10));
 		if (bUseNetherOreIron)
 			GameRegistry.registerWorldGenerator(new WorldGenDefaultMinable(BlockRegistry.netherOre.blockID, 8, Ore.netherOreIron, 15));
 		if (bUseNetherOreGold)
 			GameRegistry.registerWorldGenerator(new WorldGenDefaultMinable(BlockRegistry.netherOre.blockID, 8, Ore.netherOreGold, 4));
 		if (bUseNetherOreDiamond)
 			GameRegistry.registerWorldGenerator(new WorldGenDefaultMinable(BlockRegistry.netherOre.blockID, 7, Ore.netherOreDiamond, 1));
 		if (bUseNetherOreEmerald)
 			GameRegistry.registerWorldGenerator(new WorldGenDefaultMinable(BlockRegistry.netherOre.blockID, 7, Ore.netherOreEmerald, 1));
 		if (bUseNetherOreRedstone)
 			GameRegistry.registerWorldGenerator(new WorldGenDefaultMinable(BlockRegistry.netherOre.blockID, 7, Ore.netherOreRedstone, 6));
 		if (bUseNetherOreObsidian)
 			GameRegistry.registerWorldGenerator(new WorldGenDefaultMinable(BlockRegistry.netherOre.blockID, 8, Ore.netherOreObsidian, 8));
 		if (bUseNetherOreLapis)
 			GameRegistry.registerWorldGenerator(new WorldGenDefaultMinable(BlockRegistry.netherOre.blockID, 8, Ore.netherOreLapis, 1));
 		if (bUseNetherOreCobblestone)
 			GameRegistry.registerWorldGenerator(new WorldGenDefaultMinable(BlockRegistry.netherOre.blockID, 13, Ore.netherOreCobblestone, 10));
 
 		GameRegistry.registerWorldGenerator(new WorldGenNetherStuffsTrees(false, 4, false));
 	}
 
 	private void initRecipes() {
 		initDefaultMinecraftRecipes();
 
 		DemonicFurnaceRecipes.smelting().addSmelting(BlockRegistry.netherOre.blockID, Ore.demonicOre, new ItemStack(ItemRegistry.NetherOreIngot, 1, 0), 1.0F);
 		DemonicFurnaceRecipes.smelting().addSmelting(Block.slowSand.blockID, 0, new ItemStack(BlockRegistry.SoulGlass, 1, 0), 0.25F);
 		DemonicFurnaceRecipes.smelting().addSmelting(BlockRegistry.netherWood.blockID, Wood.hellfire, new ItemStack(ItemRegistry.NetherWoodCharcoal), 0.25F);
 		DemonicFurnaceRecipes.smelting().addSmelting(BlockRegistry.netherOre.blockID, Ore.netherOreCoal,
 				new ItemStack(ItemRegistry.NetherWoodCharcoal, 1, NetherCharcoal.charcoal), 0.25F);
 		DemonicFurnaceRecipes.smelting().addSmelting(BlockRegistry.netherOre.blockID, Ore.netherOreIron, new ItemStack(Item.ingotIron, 1), 0.25F);
 		DemonicFurnaceRecipes.smelting().addSmelting(BlockRegistry.netherOre.blockID, Ore.netherOreGold, new ItemStack(Item.ingotGold, 1), 0.25F);
 		DemonicFurnaceRecipes.smelting().addSmelting(BlockRegistry.netherOre.blockID, Ore.netherOreDiamond, new ItemStack(Item.diamond, 1), 0.5F);
 		DemonicFurnaceRecipes.smelting().addSmelting(BlockRegistry.netherOre.blockID, Ore.netherOreEmerald, new ItemStack(Item.emerald, 1), 0.5F);
 		DemonicFurnaceRecipes.smelting().addSmelting(BlockRegistry.netherOre.blockID, Ore.netherOreRedstone, new ItemStack(Item.redstone, 4), 0.25F);
 		DemonicFurnaceRecipes.smelting().addSmelting(BlockRegistry.netherOre.blockID, Ore.netherOreObsidian, new ItemStack(Block.obsidian, 1, 0), 0.25F);
 		DemonicFurnaceRecipes.smelting().addSmelting(BlockRegistry.netherOre.blockID, Ore.netherOreLapis, new ItemStack(Item.dyePowder, 4, 4), 0.25F);
 
 		CraftingManager
 				.getInstance()
 				.getRecipeList()
 				.add(new ShapedOreRecipe(new ItemStack(BlockRegistry.SoulWorkBench, 1), true, new Object[] { "I#I", "#W#", "I#I", '#', new ItemStack(Item.netherrackBrick, 1), 'W',
 						new ItemStack(Block.workbench), 'I', "ingotDemonic" }));
 
 		GameRegistry.addRecipe(new ItemStack(BlockRegistry.SoulWorkBench, 1), new Object[] { "I#I", "#W#", "I#I", '#', new ItemStack(Item.netherrackBrick, 1), 'W',
 				new ItemStack(Block.workbench), 'I', new ItemStack(ItemRegistry.NetherOreIngot, 1, 0) });
 		GameRegistry.addRecipe(new ItemStack(BlockRegistry.SoulWorkBench, 1), new Object[] { "#I#", "IWI", "#I#", '#', new ItemStack(Item.netherrackBrick, 1), 'W',
 				new ItemStack(Block.workbench), 'I', new ItemStack(ItemRegistry.NetherOreIngot, 1, 0) });
 
 		GameRegistry.addRecipe(new ItemStack(BlockRegistry.DemonicFurnace, 1), new Object[] { "NNN", "N N", "NNN", 'N', new ItemStack(Item.netherrackBrick, 1) });
 
 		GameRegistry.addShapelessRecipe(new ItemStack(ItemRegistry.torchArrow, 1), new Object[] { new ItemStack(Item.arrow, 1), new ItemStack(Block.torchWood, 1, 0) });
 
 		CraftingManager
 				.getInstance()
 				.getRecipeList()
 				.add(new ShapedOreRecipe(new ItemStack(BlockRegistry.SoulWorkBench, 1), new Object[] { "#I#", "IWI", "#I#", '#', Item.netherrackBrick, 'W',
 						new ItemStack(Block.workbench), 'I', "ingotDemonic" }));
 
 		CraftingManager.getInstance().getRecipeList()
 				.add(new ShapedOreRecipe(new ItemStack(BlockRegistry.DemonicFurnace, 1), new Object[] { "NNN", "N N", "NNN", 'N', Item.netherrackBrick }));
 
 		CraftingManager
 				.getInstance()
 				.getRecipeList()
 				.add(new ShapedOreRecipe(new ItemStack(BlockRegistry.SoulBlocker, 1, SoulBlocker.NetherSoulBlocker), new Object[] { "IPI", "SBS", "XXX", 'I', "ingotDemonic", 'P',
 						BlockRegistry.netherPlank, 'S', ItemRegistry.NetherWoodStick, 'B', ItemRegistry.NetherPotionBottle, 'X', Item.netherrackBrick }));
 
 		CraftingManager
 				.getInstance()
 				.getRecipeList()
 				.add(new ShapelessOreRecipe(new ItemStack(ItemRegistry.SoulEnergyBottle, 1, 0), new Object[] {
 						new ItemStack(ItemRegistry.NetherPotionBottle, 1, PotionBottle.hellfire), new ItemStack(ItemRegistry.NetherPotionBottle, 1, PotionBottle.acid),
 						new ItemStack(ItemRegistry.NetherPotionBottle, 1, PotionBottle.death), ItemRegistry.NetherSoulGlassBottleItem, "ingotDemonic" }));
 
 		CraftingManager
 				.getInstance()
 				.getRecipeList()
 				.add(new ShapelessOreRecipe(new ItemStack(ItemRegistry.SoulEnergyBottle, 1, 0), new Object[] {
 						new ItemStack(ItemRegistry.NetherStonePotionBowl, 1, PotionBowl.hellfire),
 						new ItemStack(ItemRegistry.NetherStonePotionBowl, 1, PotionBowl.acid),
 						new ItemStack(ItemRegistry.NetherStonePotionBowl, 1, PotionBowl.death),
 						new ItemStack(ItemRegistry.NetherStonePotionBowl, 1, PotionBowl.hellfire),
 						new ItemStack(ItemRegistry.NetherStonePotionBowl, 1, PotionBowl.acid),
 						new ItemStack(ItemRegistry.NetherStonePotionBowl, 1, PotionBowl.death), ItemRegistry.NetherSoulGlassBottleItem, "ingotDemonic" }));
 
 		GameRegistry.addShapelessRecipe(new ItemStack(ItemRegistry.SoulEnergyBottle, 1, 0), new Object[] {
 				new ItemStack(ItemRegistry.NetherPotionBottle, 1, PotionBottle.hellfire), new ItemStack(ItemRegistry.NetherPotionBottle, 1, PotionBottle.acid),
 				new ItemStack(ItemRegistry.NetherPotionBottle, 1, PotionBottle.death), ItemRegistry.NetherSoulGlassBottleItem, new ItemStack(ItemRegistry.NetherOreIngot, 1, 0) });
 
 		GameRegistry.addShapelessRecipe(new ItemStack(ItemRegistry.SoulEnergyBottle, 1, 0), new Object[] {
 				new ItemStack(ItemRegistry.NetherStonePotionBowl, 1, PotionBowl.hellfire), new ItemStack(ItemRegistry.NetherStonePotionBowl, 1, PotionBowl.acid),
 				new ItemStack(ItemRegistry.NetherStonePotionBowl, 1, PotionBowl.death), new ItemStack(ItemRegistry.NetherStonePotionBowl, 1, PotionBowl.hellfire),
 				new ItemStack(ItemRegistry.NetherStonePotionBowl, 1, PotionBowl.acid), new ItemStack(ItemRegistry.NetherStonePotionBowl, 1, PotionBowl.death),
 				ItemRegistry.NetherSoulGlassBottleItem, new ItemStack(ItemRegistry.NetherOreIngot, 1, 0) });
 
 		GameRegistry.addShapelessRecipe(new ItemStack(ItemRegistry.SoulEnergyBottle, 1, 1), new Object[] { new ItemStack(ItemRegistry.SoulEnergyBottle, 1, 0),
 				new ItemStack(ItemRegistry.SoulEnergyBottle, 1, 0), new ItemStack(ItemRegistry.SoulEnergyBottle, 1, 0), new ItemStack(ItemRegistry.SoulEnergyBottle, 1, 0),
 				new ItemStack(ItemRegistry.SoulEnergyBottle, 1, 0), new ItemStack(ItemRegistry.SoulEnergyBottle, 1, 0), new ItemStack(ItemRegistry.SoulEnergyBottle, 1, 0),
 				new ItemStack(ItemRegistry.SoulEnergyBottle, 1, 0), new ItemStack(ItemRegistry.SoulEnergyBottle, 1, 0) });
 		GameRegistry.addShapelessRecipe(new ItemStack(ItemRegistry.SoulEnergyBottle, 1, 2), new Object[] { new ItemStack(ItemRegistry.SoulEnergyBottle, 1, 1),
 				new ItemStack(ItemRegistry.SoulEnergyBottle, 1, 1), new ItemStack(ItemRegistry.SoulEnergyBottle, 1, 1), new ItemStack(ItemRegistry.SoulEnergyBottle, 1, 1),
 				new ItemStack(ItemRegistry.SoulEnergyBottle, 1, 1), new ItemStack(ItemRegistry.SoulEnergyBottle, 1, 1), new ItemStack(ItemRegistry.SoulEnergyBottle, 1, 1),
 				new ItemStack(ItemRegistry.SoulEnergyBottle, 1, 1), new ItemStack(ItemRegistry.SoulEnergyBottle, 1, 1) });
 
 		// Netherwood --> Netherlogs
 		GameRegistry.addRecipe(new ItemStack(BlockRegistry.netherPlank, 4, Plank.hellfire), new Object[] { "#", '#',
 				new ItemStack(BlockRegistry.netherWood, 1, Wood.hellfire) });
 		GameRegistry.addRecipe(new ItemStack(BlockRegistry.netherPlank, 4, Plank.acid), new Object[] { "#", '#', new ItemStack(BlockRegistry.netherWood, 1, Wood.acid) });
 		GameRegistry.addRecipe(new ItemStack(BlockRegistry.netherPlank, 4, Plank.death), new Object[] { "#", '#', new ItemStack(BlockRegistry.netherWood, 1, Wood.death) });
 
 		// Torches out of Netherwood Sticks
 		GameRegistry.addRecipe(new ItemStack(Block.torchWood, 6), new Object[] { "X", "#", 'X', Item.coal, '#', ItemRegistry.NetherWoodStick });
 		GameRegistry.addRecipe(new ItemStack(Block.torchWood, 6), new Object[] { "X", "#", 'X', new ItemStack(Item.coal, 1, 1), '#', ItemRegistry.NetherWoodStick });
 
 		GameRegistry.addRecipe(new ItemStack(Block.torchWood, 8), new Object[] { "X", "#", 'X', new ItemStack(ItemRegistry.NetherWoodCharcoal), '#', Item.stick });
 		GameRegistry.addRecipe(new ItemStack(Block.torchWood, 8), new Object[] { "X", "#", 'X', new ItemStack(ItemRegistry.NetherWoodCharcoal), '#', Item.stick });
 
 		GameRegistry.addRecipe(new ItemStack(Block.torchWood, 10), new Object[] { "X", "#", 'X', new ItemStack(ItemRegistry.NetherWoodCharcoal, 1, NetherCharcoal.charcoal), '#',
 				ItemRegistry.NetherWoodStick });
 		GameRegistry.addRecipe(new ItemStack(Block.torchWood, 10), new Object[] { "X", "#", 'X', new ItemStack(ItemRegistry.NetherWoodCharcoal, 1, NetherCharcoal.coal), '#',
 				ItemRegistry.NetherWoodStick });
 
 		GameRegistry.addShapelessRecipe(new ItemStack(ItemRegistry.NetherWoodCharcoal, 1), new Object[] { new ItemStack(Item.coal, 1, 1), new ItemStack(Item.gunpowder, 1) });
 
 		CraftingManager
 				.getInstance()
 				.getRecipeList()
 				.add(new ShapedOreRecipe(new ItemStack(ItemRegistry.NetherDemonicBarHandle, 1), new Object[] { "NIN", " S ", 'N', Item.netherrackBrick, 'I', "ingotDemonic", 'S',
 						ItemRegistry.NetherWoodStick }));
 		CraftingManager.getInstance().getRecipeList()
 				.add(new ShapedOreRecipe(new ItemStack(ItemRegistry.NetherStoneBowl, 3), new Object[] { "N N", " N ", 'N', Item.netherrackBrick }));
 
 		GameRegistry.addRecipe(new ItemStack(BlockRegistry.SoulGlassPane, 16), new Object[] { "###", "###", '#', BlockRegistry.SoulGlass });
 		GameRegistry.addRecipe(new ItemStack(ItemRegistry.NetherSoulGlassBottleItem, 3), new Object[] { "# #", " # ", '#', BlockRegistry.SoulGlass });
 		GameRegistry.addRecipe(new ItemStack(Item.flintAndSteel, 1), new Object[] { "BB", "QQ", 'B', Item.netherrackBrick, 'Q', Item.netherQuartz });
 
 		List recipes = CraftingManager.getInstance().getRecipeList();
 		addShapedRecipeFirst(recipes, new ItemStack(ItemRegistry.NetherWoodStick, 4), new Object[] { "#", "#", '#', BlockRegistry.netherPlank });
 
 		FurnaceRecipes.smelting().addSmelting(BlockRegistry.netherOre.blockID, Ore.netherOreCoal, new ItemStack(Item.coal, 1), 0.2F);
 
 		/**
 		 * Swords
 		 */
 		initSwordRecipes();
 
 	}
 
 	public static void addShapedRecipeFirst(ItemStack itemstack, Object... objArray) {
 		List recipes = CraftingManager.getInstance().getRecipeList();
 		addShapedRecipeFirst(recipes, itemstack, objArray);
 	}
 
 	/**
 	 * @author mDiyo
 	 */
 	public static void addShapedRecipeFirst(List recipeList, ItemStack itemstack, Object... objArray) {
 		String var3 = "";
 		int var4 = 0;
 		int var5 = 0;
 		int var6 = 0;
 
 		if (objArray[var4] instanceof String[]) {
 			String[] var7 = (String[]) ((String[]) objArray[var4++]);
 
 			for (int var8 = 0; var8 < var7.length; ++var8) {
 				String var9 = var7[var8];
 				++var6;
 				var5 = var9.length();
 				var3 = var3 + var9;
 			}
 		} else {
 			while (objArray[var4] instanceof String) {
 				String var11 = (String) objArray[var4++];
 				++var6;
 				var5 = var11.length();
 				var3 = var3 + var11;
 			}
 		}
 
 		HashMap var12;
 
 		for (var12 = new HashMap(); var4 < objArray.length; var4 += 2) {
 			Character var13 = (Character) objArray[var4];
 			ItemStack var14 = null;
 
 			if (objArray[var4 + 1] instanceof Item) {
 				var14 = new ItemStack((Item) objArray[var4 + 1]);
 			} else if (objArray[var4 + 1] instanceof Block) {
 				var14 = new ItemStack((Block) objArray[var4 + 1], 1, -1);
 			} else if (objArray[var4 + 1] instanceof ItemStack) {
 				var14 = (ItemStack) objArray[var4 + 1];
 			}
 
 			var12.put(var13, var14);
 		}
 
 		ItemStack[] var15 = new ItemStack[var5 * var6];
 
 		for (int var16 = 0; var16 < var5 * var6; ++var16) {
 			char var10 = var3.charAt(var16);
 
 			if (var12.containsKey(Character.valueOf(var10))) {
 				var15[var16] = ((ItemStack) var12.get(Character.valueOf(var10))).copy();
 			} else {
 				var15[var16] = null;
 			}
 		}
 
 		ShapedRecipes var17 = new ShapedRecipes(var5, var6, var15, itemstack);
 		recipeList.add(0, var17);
 	}
 
 	private void initSwordRecipes() {
 		/**
 		 * Obsidian Sword & Obsidian Acid/Death Sword
 		 */
 		if (ItemRegistry.NetherObsidianSword != null) {
 			GameRegistry.addRecipe(new ItemStack(ItemRegistry.NetherObsidianSword, 1),
 					new Object[] { " O ", " O ", " H ", 'O', Block.obsidian, 'H', ItemRegistry.NetherDemonicBarHandle });
 
 			GameRegistry.addRecipe(new ItemStack(ItemRegistry.NetherObsidianSwordAcid, 1), new Object[] { "#S#", '#',
 					new ItemStack(ItemRegistry.NetherPotionBottle, 1, PotionBottle.acid), 'S', ItemRegistry.NetherObsidianSword });
 			GameRegistry.addRecipe(new ItemStack(ItemRegistry.NetherObsidianSwordDeath, 1), new Object[] { "#S#", '#',
 					new ItemStack(ItemRegistry.NetherPotionBottle, 1, PotionBottle.death), 'S', ItemRegistry.NetherObsidianSword });
 
 			GameRegistry.addRecipe(new ItemStack(ItemRegistry.NetherObsidianSwordAcid, 1), new Object[] { " # ", "#S#", " # ", '#',
 					new ItemStack(ItemRegistry.NetherStonePotionBowl, 1, PotionBowl.acid), 'S', ItemRegistry.NetherObsidianSword });
 			GameRegistry.addRecipe(new ItemStack(ItemRegistry.NetherObsidianSwordDeath, 1), new Object[] { " # ", "#S#", " # ", '#',
 					new ItemStack(ItemRegistry.NetherStonePotionBowl, 1, PotionBowl.death), 'S', ItemRegistry.NetherObsidianSword });
 		}
 		/**
 		 * Soulglass Swords
 		 */
 		if (ItemRegistry.NetherSoulglassSword != null) {
 			GameRegistry.addRecipe(new ItemStack(ItemRegistry.NetherSoulglassSword, 1), new Object[] { " # ", " # ", " H ", '#', BlockRegistry.SoulGlass, 'H',
 					ItemRegistry.NetherDemonicBarHandle });
 
 			GameRegistry.addRecipe(new ItemStack(ItemRegistry.NetherSoulglassSwordHellfire, 1), new Object[] { "#S#", '#',
 					new ItemStack(ItemRegistry.NetherPotionBottle, 1, PotionBottle.hellfire), 'S', ItemRegistry.NetherSoulglassSword });
 			GameRegistry.addRecipe(new ItemStack(ItemRegistry.NetherSoulglassSwordDeath, 1), new Object[] { "#S#", '#',
 					new ItemStack(ItemRegistry.NetherPotionBottle, 1, PotionBottle.death), 'S', ItemRegistry.NetherSoulglassSword });
 			GameRegistry.addRecipe(new ItemStack(ItemRegistry.NetherSoulglassSwordAcid, 1), new Object[] { "#S#", '#',
 					new ItemStack(ItemRegistry.NetherPotionBottle, 1, PotionBottle.acid), 'S', ItemRegistry.NetherSoulglassSword });
 			GameRegistry.addRecipe(new ItemStack(ItemRegistry.NetherSoulglassSwordDeath, 1), new Object[] { "#S#", '#',
 					new ItemStack(ItemRegistry.NetherPotionBottle, 1, PotionBottle.death), 'S', ItemRegistry.NetherSoulglassSword });
 
 			GameRegistry.addRecipe(new ItemStack(ItemRegistry.NetherSoulglassSwordHellfire, 1), new Object[] { " # ", "#S#", " # ", '#',
 					new ItemStack(ItemRegistry.NetherStonePotionBowl, 1, PotionBowl.hellfire), 'S', ItemRegistry.NetherSoulglassSword });
 			GameRegistry.addRecipe(new ItemStack(ItemRegistry.NetherSoulglassSwordAcid, 1), new Object[] { " # ", "#S#", " # ", '#',
 					new ItemStack(ItemRegistry.NetherStonePotionBowl, 1, PotionBowl.acid), 'S', ItemRegistry.NetherSoulglassSword });
 			GameRegistry.addRecipe(new ItemStack(ItemRegistry.NetherSoulglassSwordDeath, 1), new Object[] { " # ", "#S#", " # ", '#',
 					new ItemStack(ItemRegistry.NetherStonePotionBowl, 1, PotionBowl.death), 'S', ItemRegistry.NetherSoulglassSword });
 		}
 		/**
 		 * Diamond Swords
 		 */
 		if (ItemRegistry.NetherDiamondSword != null) {
 			GameRegistry.addRecipe(new ItemStack(ItemRegistry.NetherDiamondSword, 1), new Object[] { " D ", " D ", " H ", 'D', Item.diamond, 'H', ItemRegistry.NetherDemonicBarHandle });
 
 			GameRegistry.addRecipe(new ItemStack(ItemRegistry.NetherDiamondSwordAcid, 1), new Object[] { "#S#", '#',
 					new ItemStack(ItemRegistry.NetherPotionBottle, 1, PotionBottle.acid), 'S', ItemRegistry.NetherDiamondSword });
 			GameRegistry.addRecipe(new ItemStack(ItemRegistry.NetherDiamondSwordDeath, 1), new Object[] { "#S#", '#',
 					new ItemStack(ItemRegistry.NetherPotionBottle, 1, PotionBottle.death), 'S', ItemRegistry.NetherDiamondSword });
 
 			GameRegistry.addRecipe(new ItemStack(ItemRegistry.NetherDiamondSwordAcid, 1), new Object[] { " # ", "#S#", " # ", '#',
 					new ItemStack(ItemRegistry.NetherStonePotionBowl, 1, PotionBowl.acid), 'S', ItemRegistry.NetherDiamondSword });
 			GameRegistry.addRecipe(new ItemStack(ItemRegistry.NetherDiamondSwordDeath, 1), new Object[] { " # ", "#S#", " # ", '#',
 					new ItemStack(ItemRegistry.NetherStonePotionBowl, 1, PotionBowl.death), 'S', ItemRegistry.NetherDiamondSword });
 		}
 	}
 
 	/**
 	 * Returns Default Minecraft Items from Mod Items
 	 */
 	private void initDefaultMinecraftRecipes() {
 		GameRegistry.addShapelessRecipe(new ItemStack(Item.netherrackBrick), new Object[] { new ItemStack(BlockRegistry.netherOre, 1, Ore.netherStone) });
 		GameRegistry.addShapelessRecipe(new ItemStack(BlockRegistry.netherOre, 1, Ore.netherStone), new Object[] { Item.netherrackBrick });
 		GameRegistry.addShapelessRecipe(new ItemStack(Item.stick), new Object[] { new ItemStack(ItemRegistry.NetherWoodStick) });
 	}
 
 	private void initLanguageRegistry() {
 		LanguageRegistry.instance().addStringLocalization("itemGroup.tabNetherStuffs", "en_US", "NetherStuffs");
 	}
 
 	@PostInit
 	public void postInit(FMLPostInitializationEvent event) {
 		/**
 		 * Init Mod Compatibility
 		 */
 
 		if (bBuildcraftAvailable)
 			initBuildcraftStuff();
 
 		if (bHarkenScytheAvailable)
 			initHarkenScytheCompatibility();
 
 		if (bThaumcraftAvailable)
 			initThaumcraftAPI();
 
 		if (bTConstructAvailable)
 			initTConstruct();
 	}
 }
