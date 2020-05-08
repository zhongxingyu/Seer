 package mods.ifw.aurus.common;
 
 import java.util.EnumSet;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.entity.EnumCreatureType;
 import net.minecraft.world.biome.BiomeGenBase;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.MinecraftForge;
 import cpw.mods.fml.common.ITickHandler;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.PostInit;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.TickType;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.EntityRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import cpw.mods.fml.common.registry.TickRegistry;
 import cpw.mods.fml.relauncher.Side;
 
 @Mod(modid = Aurus.modid, name = "Aurus", version = "0.1a")
 @NetworkMod(clientSideRequired = true, serverSideRequired = false)
 public class Aurus implements ITickHandler {
 	public static final String modid = "ifw_aurus";
 	public static Aurus instance;
 	public static Configuration config;
 
 	@SidedProxy(clientSide = "mods.ifw.aurus.client.ClientProxy", serverSide = "mods.ifw.aurus.common.CommonProxy")
 	public static CommonProxy proxy;
 
 	// public static Block blockOfWeakness;
 	public static Block blockBogWaterFlowing;
 	public static Block blockBogWaterStationary;
 
 	public static Block blockNest;
 
 	public static Block blockBogGas;
 	// public static Block testBlock;
 
 	public static Block pathFinder;
 
 	public static int startingBlockID = 500;
 	public int lastBlockID = startingBlockID;
 
 	public static int bogWaterUpdates;
 	private int ticksSincePrint;
 
 	public static Block pathMarker;
 
 	public static ItemDragonBlood dragonBlood;
 	public static ItemDragonBloodVial dragonBloodVial;
 
 	public static int bogWaterFlowingID;
 	public static int bogWaterStationaryID;
 	public static int bogGasID;
 
 	public static int nestID;
 
 	private static int dragonBloodID;
 	private static int dragonBloodVialID;
 
 	// public static int testFluidFlowingID;
 	// public static int testFluidStationaryID;
 
 	public int addConfigBlock(Configuration config, String name) {
 		return config.getBlock(name, ++lastBlockID).getInt();
 	}
 
 	@PreInit
 	public void preInit(FMLPreInitializationEvent event) {
 
 		instance = this;
 		config = new Configuration(event.getSuggestedConfigurationFile());
 		config.load();
 
 		bogWaterFlowingID = addConfigBlock(config, "Bog Water Source");
 		bogWaterStationaryID = addConfigBlock(config, "Bog Water Stationary");
 		bogGasID = addConfigBlock(config, "Bog Gas");
 
 		nestID = addConfigBlock(config, "Nest");
 
 		dragonBloodID = config.getItem("dragonBlood", 5000).getInt();
 		dragonBloodVialID = config.getItem("dragonBloodVial", 5001).getInt();
 
 		// testFluidFlowingID = addConfigBlock(config, "Test Fluid Source");
 		// testFluidStationaryID = addConfigBlock(config,
 		// "Test Fluid Stationary");
 
 		// int randomItemID = config.getItem("RandomItem", 20000).getInt();
 
 		// Since this flag is a boolean, we can read it into the variable
 		// directly from the config.
 		// someConfigFlag = config.get(Configuration.CATEGORY_GENERAL,
 		// "SomeConfigFlag", false).getBoolean(false);
 
 		config.save();
 
 		MinecraftForge.EVENT_BUS.register(new EventSounds());
 		MinecraftForge.EVENT_BUS.register(new CustomEvents());
 	}
 
 	@Init
 	public void init(FMLInitializationEvent event) {
 		proxy.registerRenderers();
 
 		TickRegistry.registerTickHandler(this.instance, Side.SERVER);
 
 		this.registerBlocks();
 
 		this.registerItems();
 
 		this.registerEntities();
 
 		this.registerMobs();
 
 		this.registerEtc();
 
 	}
 
 	private void registerItems() {
 		dragonBlood = new ItemDragonBlood(this.dragonBloodID);
 		LanguageRegistry.addName(dragonBlood, "Aurus' Blood");
 		dragonBloodVial = new ItemDragonBloodVial(this.dragonBloodVialID);
 		LanguageRegistry.addName(dragonBloodVial, "Vial of Aurus' Blood");
 	}
 
 	public void registerBlocks() {
 		// blockOfWeakness = new BlockOfWeakness(538, 0)
 		// .setUnlocalizedName("blockOfWeakness");
 		// LanguageRegistry.addName(blockOfWeakness, "Block of Weakness");
 		// MinecraftForge.setBlockHarvestLevel(blockOfWeakness, "pickaxe", 2);
 		// GameRegistry.registerBlock(blockOfWeakness);
 
 		blockBogWaterFlowing = new BlockBogWaterFlowing(bogWaterFlowingID)
 				.setUnlocalizedName("blockBogWaterFlowing");
 		LanguageRegistry.addName(blockBogWaterFlowing, "Bog Water Flow");
 		GameRegistry.registerBlock(blockBogWaterFlowing);
 
 		blockBogWaterStationary = new BlockBogWaterStationary(
 				bogWaterStationaryID)
 				.setUnlocalizedName("blockBogWaterStationary");
 		LanguageRegistry.addName(blockBogWaterStationary, "Bog Water Still");
 		GameRegistry.registerBlock(blockBogWaterStationary);
 
 		// blockTestFluidFlowing = new BlockTestFluidFlowing(testFluidFlowingID)
 		// .setUnlocalizedName("blockTestFluidFlowing");
 		// LanguageRegistry
 		// .addName(blockTestFluidFlowing, "blockTestFluidFlowing");
 		// GameRegistry.registerBlock(blockTestFluidFlowing);
 		//
 		// blockTestFluidStationary = new BlockTestFluidStationary(
 		// testFluidStationaryID).setUnlocalizedName("blockTestFluidStationary");
 		// LanguageRegistry.addName(blockTestFluidStationary,
 		// "blockTestFluidStationary");
 		// GameRegistry.registerBlock(blockTestFluidStationary);
 
 		blockBogGas = new BlockBogGas(bogGasID)
 				.setUnlocalizedName("blockBogGas");
 		LanguageRegistry.addName(blockBogGas, "Bog Gas");
 		GameRegistry.registerBlock(blockBogGas);
 
 		blockNest = new BlockNest(nestID, "").setUnlocalizedName(modid
 				+ "blockNest");
 		LanguageRegistry.addName(blockNest, "Nest Shell");
 		GameRegistry.registerBlock(blockNest, blockNest.getUnlocalizedName2());
 
 		pathMarker = new BlockPathMarker(997, Material.cloth)
 				.setUnlocalizedName("pathMarker");
 		LanguageRegistry.addName(pathMarker, "Path Finder");
 		GameRegistry.registerBlock(pathMarker);
 
 		pathFinder = new BlockPathFinder(998, Material.cloth)
 				.setUnlocalizedName("pathFinder");
 		LanguageRegistry.addName(pathFinder, "Path Finder");
 		GameRegistry.registerBlock(pathFinder);
 
 		// testBlock = new TestBlock(999).setUnlocalizedName("testBlock");
 		// LanguageRegistry.addName(testBlock, "Test Block");
 		// GameRegistry.registerBlock(testBlock);
 
 	}
 
 	public void registerEntities() {
 		EntityRegistry.registerModEntity(EntityProjectile.class, "Projectile",
 				12, this, 128, 1, true);
 
 		EntityRegistry.registerModEntity(EntityFieryProjectileSmall.class,
 				"FieryProjectileSmall", 13, this, 128, 1, true);
 
 		EntityRegistry.registerModEntity(EntityIcyProjectileSmall.class,
 				"IcyProjectileSmall", 14, this, 128, 1, true);
 
 		EntityRegistry.registerModEntity(EntityToxicProjectileSmall.class,
 				"ToxicProjectileSmall", 15, this, 128, 1, true);
 
 		EntityRegistry.registerModEntity(EntitySparkyProjectileSmall.class,
 				"SparkyProjectileSmall", 16, this, 128, 1, true);
 
 	}
 
 	public void registerMobs() {
 		// LanguageRegistry.instance().addStringLocalization(
 		// "entity.jumpPointer.name", "en_US", "Jump Pointer");
 		// EntityRegistry.registerGlobalEntityID(EntityJumpPointer.class,
 		// "jumpPointer", EntityRegistry.findGlobalUniqueEntityId(),
 		// 0x000000, 0x666666);
 
 		LanguageRegistry.instance().addStringLocalization("entity.auru.name",
 				"en_US", "Auru");
 		EntityRegistry.registerGlobalEntityID(EntityAuru.class, "auru",
 				EntityRegistry.findGlobalUniqueEntityId(), 0x999999, 0x666666);
 		EntityRegistry.addSpawn(EntityAuru.class, 1, 1, 1,
 				EnumCreatureType.creature, BiomeGenBase.beach,
 				BiomeGenBase.desert, BiomeGenBase.desertHills,
 				BiomeGenBase.extremeHills, BiomeGenBase.extremeHillsEdge,
 				BiomeGenBase.forest, BiomeGenBase.forestHills,
 				BiomeGenBase.frozenOcean, BiomeGenBase.frozenRiver,
 				/* BiomeGenBase.hell, */BiomeGenBase.iceMountains,
 				BiomeGenBase.icePlains, BiomeGenBase.jungle,
 				BiomeGenBase.jungleHills, BiomeGenBase.mushroomIsland,
 				BiomeGenBase.mushroomIslandShore, BiomeGenBase.ocean,
 				BiomeGenBase.plains, BiomeGenBase.river, BiomeGenBase.sky,
 				BiomeGenBase.swampland, BiomeGenBase.taiga,
 				BiomeGenBase.taigaHills);
 
 		LanguageRegistry.instance().addStringLocalization(
 				"entity.auruBog.name", "en_US", "Bog Auru");
 		EntityRegistry.registerGlobalEntityID(EntityAuruBog.class, "auruBog",
 				EntityRegistry.findGlobalUniqueEntityId(), 0xCC6666, 0x99CC66);
 
 		LanguageRegistry.instance().addStringLocalization(
 				"entity.auruCloud.name", "en_US", "Cloud Auru");
 		EntityRegistry.registerGlobalEntityID(EntityAuruCloud.class,
 				"auruCloud", EntityRegistry.findGlobalUniqueEntityId(),
 				0x666666, 0xFFFF66);
 
 		LanguageRegistry.instance().addStringLocalization(
 				"entity.auruFlame.name", "en_US", "Flame Auru");
 		EntityRegistry.registerGlobalEntityID(EntityAuruFlame.class,
 				"auruFlame", EntityRegistry.findGlobalUniqueEntityId(),
 				0xFFCC66, 0xCC6666);
 
 		LanguageRegistry.instance().addStringLocalization(
 				"entity.auruIce.name", "en_US", "Ice Auru");
 		EntityRegistry.registerGlobalEntityID(EntityAuruIce.class, "auruIce",
 				EntityRegistry.findGlobalUniqueEntityId(), 0x66CCFF, 0xFFFFFF);
 
 		LanguageRegistry.instance().addStringLocalization("entity.eyedas.name",
 				"en_US", "Eyedas");
 		EntityRegistry.registerGlobalEntityID(EntityEye.class, "eyedas",
 				EntityRegistry.findGlobalUniqueEntityId(), 0x333333, 0x3366CC);
		EntityRegistry.addSpawn(EntityEye.class, 1, 1, 2,
 				EnumCreatureType.monster, BiomeGenBase.beach,
 				BiomeGenBase.desert, BiomeGenBase.desertHills,
 				BiomeGenBase.extremeHills, BiomeGenBase.extremeHillsEdge,
 				BiomeGenBase.forest, BiomeGenBase.forestHills,
 				BiomeGenBase.frozenOcean, BiomeGenBase.frozenRiver,
 				/* BiomeGenBase.hell, */BiomeGenBase.iceMountains,
 				BiomeGenBase.icePlains, BiomeGenBase.jungle,
 				BiomeGenBase.jungleHills, BiomeGenBase.mushroomIsland,
 				BiomeGenBase.mushroomIslandShore, BiomeGenBase.ocean,
 				BiomeGenBase.plains, BiomeGenBase.river, BiomeGenBase.sky,
 				BiomeGenBase.swampland, BiomeGenBase.taiga,
 				BiomeGenBase.taigaHills);
 	}
 
 	public void registerEtc() {
 		// GameRegistry.registerWorldGenerator(new WorldGenFireDragonNest());
 		// GameRegistry.registerWorldGenerator(new WorldGenBogDragonNest());
 		// GameRegistry.registerWorldGenerator(new WorldGenMobNest());
 
 	}
 
 	@PostInit
 	public static void postInit(FMLPostInitializationEvent event) {
 	}
 
 	@Override
 	public void tickStart(EnumSet<TickType> type, Object... tickData) {
 	}
 
 	@Override
 	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
 		ticksSincePrint++;
 		if (ticksSincePrint > 40) {
 			// System.out.println(this.bogWaterUpdates);
 			this.ticksSincePrint = 0;
 			this.bogWaterUpdates = 0;
 		}
 	}
 
 	@Override
 	public EnumSet<TickType> ticks() {
 		return EnumSet.of(TickType.SERVER);
 	}
 
 	@Override
 	public String getLabel() {
 		return null;
 	}
 
 }
