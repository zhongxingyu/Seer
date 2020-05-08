 package mods.elysium;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import mods.elysium.api.Plants;
 import mods.elysium.block.*;
 import mods.elysium.dimension.*;
 import mods.elysium.dimension.portal.ElysiumBlockPortalCore;
 import mods.elysium.dimension.portal.ElysiumTileEntityPortal;
 import mods.elysium.dimension.portal.ElysiumTileEntityPortalRenderer;
 import mods.elysium.gen.WorldGenElysium;
 import mods.elysium.handlers.BonemealHandler;
 import mods.elysium.items.*;
 import mods.elysium.proxy.ClientProxy;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.EnumToolMaterial;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.biome.BiomeGenBase;
 import net.minecraftforge.common.DimensionManager;
 import net.minecraftforge.common.EnumHelper;
 import net.minecraftforge.common.ForgeHooks;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.common.Property;
 import net.minecraftforge.event.EventBus;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 @Mod(name="The Elysium", version="1.0", useMetadata = false, modid = "TheElysium", dependencies="required-after:Forge@[7.8.0,)")
 //@NetworkMod(channels = {DefaultProps.NET_CHANNEL_NAME}, packetHandler = PacketHandler.class, clientSideRequired = true, serverSideRequired = true)
 public class Elysium
 {
	@Instance("The Elysium")
 	public static Elysium instance;
 	
 	public static ElysiumConfiguration mainConfiguration;
 	
 	public static final CreativeTabs tabElysium = new ElysiumTab(12, "The Elysium");
 		
 	/** Dimension ID **/
 	public static int DimensionID;
 	
 	//Blocks
 	
 	public static Block paleStone;
 	/** It means elysian dirt. **/
 	public static Block soilBlock;
 	/** It means elysian grass. **/
 	public static Block grassBlock;
 	public static Block LeucosandBlock;
 	public static Block RiltBlock;
 	public static Block FostimberLogBlock;
 	public static Block FostimberLeavesBlock;
 	public static Block GastroShellBlock;
 	public static Block FostimberSaplingBlock;
 	public static Block WoodBlock;
 	public static Block FlowerBlock;
 	/** It means elysian grass overlay. **/
 	public static Block CurlgrassBlock;
 	public static Block SulphurOreBlock;
 	public static Block CobaltOreBlock;
 	public static Block IridiumOreBlock;
 	public static Block SiliconOreBlock;
 	public static Block JadeOreBlock;
 	public static Block TourmalineOreBlock;
 	public static Block BerylOreBlock;
 	public static Block waterStill;
 	public static ElysiumBlockFluid waterMoving;
 	public static Block shellFloatingBlock;
 	public static Block conchFloatingBlock;
 //	public static Block LeucosandBlock;
 //	public static Block LeucosandBlock;
 //	public static Block LeucosandBlock;
 	
 	public static Block portalCore;
 	
 
 	//Items
 	
 	public static Item GracePrismItem;
 
 
 	public static Item WhistleItem;
 	public static Item PepperSeedItem;
 	public static Item AsphodelPetalsItem;
 
 	public static Item SwordFostimberItem;
 	public static Item PickaxeFostimberItem;
 	public static Item AxeFostimberItem;
 	public static Item ShovelFostimberItem;
 	public static Item HoeFostimberItem;
 	
 	public static Item SwordStoneItem;
 	public static Item PickaxeStoneItem;
 	public static Item AxeStoneItem;
 	public static Item ShovelStoneItem;
 	public static Item HoeStoneItem;
 
 	public static Item OverKillItem;
 	public static Item DebugItem;
 	
 	
 	/** Biome's **/
 	public static BiomeGenBase ElysiumPlainBiome = null;
 
 	
 	@PreInit
 	public void loadConfiguration(FMLPreInitializationEvent evt) {
 //		NetworkRegistry.instance().registerGuiHandler(this, guiHandler);
 //		GameRegistry.registerTileEntity(TileMixer.class, "Mixer");
 //		GameRegistry.registerTileEntity(TileCandyMaker.class, "Candy Maker");
 
 
 //		GameRegistry.addBiome(Halloween);
 
 //		Version.versionCheck();
 
 
 		mainConfiguration = new ElysiumConfiguration(new File(evt.getModConfigurationDirectory(), "Elysium.cfg"));
 		try
 		{
 			mainConfiguration.load();
 
 
 			Property idDim = Elysium.mainConfiguration.get("dimensionID", "dim", DefaultProps.DimensionID, "This is the id of the dimension change if needed!");
 			DimensionID = idDim.getInt();
 			
 			// Block Registry
 			
 			Property idPalestoneBlock = Elysium.mainConfiguration.getTerrainBlock("terrainGen", "palestone.id", DefaultProps.idPalestoneBlock, null);
 			paleStone = (new PalestoneBlock(idPalestoneBlock.getInt(), Material.rock)).setHardness(1.5F).setResistance(10.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("Palestone");
 			ClientProxy.proxy.registerBlock(paleStone);
 			LanguageRegistry.addName(paleStone, "Palestone");
 			
 			Property idSoilBlock = Elysium.mainConfiguration.getTerrainBlock("terrainGen", "ElysiumDirt.id", DefaultProps.idSoilBlock, null);
 			soilBlock = (new SoilBlock(idSoilBlock.getInt(), Material.ground)).setHardness(0.5F).setStepSound(Block.soundGravelFootstep).setUnlocalizedName("Gammasoil");
 			ClientProxy.proxy.registerBlock(soilBlock);
 			LanguageRegistry.addName(soilBlock, "Elysian Soil");
 
 			Property idGrassBlock = Elysium.mainConfiguration.getTerrainBlock("terrainGen", "ElysiumGrass.id", DefaultProps.idGrassBlock, null);
 			grassBlock = (new GrassBlock(idGrassBlock.getInt(), Material.ground)).setHardness(0.6F).setStepSound(Block.soundGrassFootstep).setUnlocalizedName("Gammagrass");
 			ClientProxy.proxy.registerBlock(grassBlock);
 			LanguageRegistry.addName(grassBlock, "Elysian Grass");
 
 			Property idLeucosandBlock = Elysium.mainConfiguration.getTerrainBlock("terrainGen", "Leucogrit.id", DefaultProps.idLeucosandBlock, null);
 			LeucosandBlock = (new LeucosandBlock(idLeucosandBlock.getInt(), Material.sand)).setHardness(0.5F).setStepSound(Block.soundSandFootstep).setUnlocalizedName("Leucogrit");
 			ClientProxy.proxy.registerBlock(LeucosandBlock);
 			LanguageRegistry.addName(LeucosandBlock, "Leucosand");
 
 			Property idRiltBlock = Elysium.mainConfiguration.getTerrainBlock("terrainGen", "Rilt.id", DefaultProps.idRiltBlock, null);
 			RiltBlock = (new RiltBlock(idRiltBlock.getInt(), Material.sand)).setHardness(0.6F).setStepSound(Block.soundGravelFootstep).setUnlocalizedName("Rilt");
 			ClientProxy.proxy.registerBlock(RiltBlock);
 			LanguageRegistry.addName(RiltBlock, "Rilt Block");
 
 			Property idFostimberSaplingBlock = Elysium.mainConfiguration.getBlock("FostimberSaplingBlock.id", DefaultProps.idFostimberSaplingBlock);
 			FostimberSaplingBlock = (new FostimberSaplingBlock(idFostimberSaplingBlock.getInt())).setHardness(0F).setUnlocalizedName("fostimber_sapling");
 			ClientProxy.proxy.registerBlock(FostimberSaplingBlock);
 			LanguageRegistry.addName(FostimberSaplingBlock, "Fostimber Sapling");
 			
 			Property idFostimberLogBlock = Elysium.mainConfiguration.getBlock("FostimberLog.id", DefaultProps.idFostimberLogBlock);
 			FostimberLogBlock = (new FostimberLogBlock(idFostimberLogBlock.getInt(), Material.wood)).setHardness(2.0F).setStepSound(Block.soundWoodFootstep).setUnlocalizedName("Fostimber Log Top");
 			ClientProxy.proxy.registerBlock(FostimberLogBlock);
 			LanguageRegistry.addName(FostimberLogBlock, "Fostimber Log");
 
 			Property idFostimberLeavesBlock = Elysium.mainConfiguration.getBlock("FostimberLeavesBlock.id", DefaultProps.idFostimberLeavesBlock);
 			FostimberLeavesBlock = (new FostimberLeavesBlock(idFostimberLeavesBlock.getInt(), Material.leaves)).setLightOpacity(1).setHardness(0.2F).setStepSound(Block.soundGrassFootstep).setUnlocalizedName("fostimber_leaves");
 			ClientProxy.proxy.registerBlock(FostimberLeavesBlock);
 			LanguageRegistry.addName(FostimberLeavesBlock, "Fostimber Leaves");
 
 			Property idWoodBlock = Elysium.mainConfiguration.getBlock("idWoodBlock.id", DefaultProps.idWoodBlock);
 			WoodBlock = (new ElysiumBlock(idWoodBlock.getInt(), Material.wood)).setHardness(0.2F).setStepSound(Block.soundWoodFootstep).setUnlocalizedName("fostimber_planks");
 			ClientProxy.proxy.registerBlock(WoodBlock);
 			LanguageRegistry.addName(WoodBlock, "Wooden Planks");
 			
 			Property idGastroShellBlock = Elysium.mainConfiguration.getBlock("idGastroShellBlock.id", DefaultProps.idGastroShellBlock);
 			GastroShellBlock = (new GastroShellBlock(idGastroShellBlock.getInt(), Material.leaves)).setHardness(0.2F).setStepSound(Block.soundGrassFootstep).setUnlocalizedName("gastroshellTop");
 			ClientProxy.proxy.registerBlock(GastroShellBlock);
 			LanguageRegistry.addName(GastroShellBlock, "Gastro Shell");
 
 			Property idAsphodelFlowerBlock = Elysium.mainConfiguration.getBlock("idAsphodelFlowerBlock.id", DefaultProps.idAsphodelFlowerBlock);
 			FlowerBlock = (new ElysiumFlowerBlock(idAsphodelFlowerBlock.getInt())).setHardness(0.0F).setStepSound(Block.soundGrassFootstep).setUnlocalizedName("asphodel_flower");
 			ClientProxy.proxy.registerBlock(FlowerBlock);
 			LanguageRegistry.addName(FlowerBlock, "Asphodel Flower");
 
 			Property idCurlgrassBlock = Elysium.mainConfiguration.getBlock("idCurlgrassBlock.id", DefaultProps.idCurlgrassBlock);
 			CurlgrassBlock = new CurlgrassBlock(idCurlgrassBlock.getInt()).setHardness(0.0F).setStepSound(Block.soundGrassFootstep).setUnlocalizedName("Curlgrass");
 			ClientProxy.proxy.registerBlock(CurlgrassBlock);
 			LanguageRegistry.addName(CurlgrassBlock, "Curlgrass");
 
 			Property idOreSulphurBlock = Elysium.mainConfiguration.getBlock("idOreSulphurBlock.id", DefaultProps.idOreSulphurBlock);
 			SulphurOreBlock = new SulphurOreBlock(idOreSulphurBlock.getInt(), Material.rock).setHardness(0.2F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("oreSulphur");
 			ClientProxy.proxy.registerBlock(SulphurOreBlock);
 			LanguageRegistry.addName(SulphurOreBlock, "Sulphur Ore");
 			
 
 			Property idOreCobaltBlock = Elysium.mainConfiguration.getBlock("idOreCobaltBlock.id", DefaultProps.idOreCobaltBlock);
 	/**/	CobaltOreBlock = new SulphurOreBlock(idOreCobaltBlock.getInt(), Material.rock).setHardness(0.2F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("oreCobalt");
 			ClientProxy.proxy.registerBlock(CobaltOreBlock);
 			LanguageRegistry.addName(CobaltOreBlock, "Cobalt Ore");
 
 			Property idOreIridiumBlock = Elysium.mainConfiguration.getBlock("idOreIridiumBlock.id", DefaultProps.idOreIridiumBlock);
 			IridiumOreBlock = new SulphurOreBlock(idOreIridiumBlock.getInt(), Material.rock).setHardness(0.2F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("oreIridium");
 			ClientProxy.proxy.registerBlock(IridiumOreBlock);
 			LanguageRegistry.addName(IridiumOreBlock, "Iridium Ore");
 			
 			Property idOreSiliconBlock = Elysium.mainConfiguration.getBlock("idOreSiliconBlock.id", DefaultProps.idOreSiliconBlock);
 			SiliconOreBlock = new SulphurOreBlock(idOreSiliconBlock.getInt(), Material.rock).setHardness(0.2F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("oreSilicon");
 			ClientProxy.proxy.registerBlock(SiliconOreBlock);
 			LanguageRegistry.addName(SiliconOreBlock, "Silicon Ore");
 			
 			Property idOreJadeBlock = Elysium.mainConfiguration.getBlock("idOreJadeBlock.id", DefaultProps.idOreJadeBlock);
 			JadeOreBlock = new SulphurOreBlock(idOreJadeBlock.getInt(), Material.rock).setHardness(0.2F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("oreJade");
 			ClientProxy.proxy.registerBlock(JadeOreBlock);
 			LanguageRegistry.addName(JadeOreBlock, "Jade Ore");
 
 			Property idOreTourmalineBlock = Elysium.mainConfiguration.getBlock("idOreTourmalineBlock.id", DefaultProps.idOreTourmalineBlock);
 			TourmalineOreBlock = new SulphurOreBlock(idOreTourmalineBlock.getInt(), Material.rock).setHardness(0.2F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("oreTourmaline");
 			ClientProxy.proxy.registerBlock(TourmalineOreBlock);
 			LanguageRegistry.addName(TourmalineOreBlock, "Tourmaline Ore");
 
 			Property idOreBerylBlock = Elysium.mainConfiguration.getBlock("idOreBerylBlock.id", DefaultProps.idOreBerylBlock);
 			BerylOreBlock = new SulphurOreBlock(idOreBerylBlock.getInt(), Material.rock).setHardness(0.2F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("oreBeryl");
 			ClientProxy.proxy.registerBlock(BerylOreBlock);
 			LanguageRegistry.addName(BerylOreBlock, "Beryl Ore");
 			
 
 			Property idWaterBlock = Elysium.mainConfiguration.getTerrainBlock("terrainGen", "idWaterBlock.id", DefaultProps.idWaterBlock, null);
 			waterStill = new ElysiumBlockStationary(idWaterBlock.getInt(), Material.water).setHardness(100.0F).setLightOpacity(3).setUnlocalizedName("elysian_water");
 			ClientProxy.proxy.registerBlock(waterStill);
 			LanguageRegistry.addName(waterStill, "Elysium Water Still");
 			
 			Property idWaterFlowingBlock = Elysium.mainConfiguration.getTerrainBlock("terrainGen", "idWaterFlowingBlock.id", DefaultProps.idWaterFlowingBlock, null);
 			waterMoving = (ElysiumBlockFluid) new ElysiumBlockFlowing(idWaterFlowingBlock.getInt(), Material.water).setHardness(100.0F).setLightOpacity(3).setUnlocalizedName("elysian_water_flow");
 			ClientProxy.proxy.registerBlock(waterMoving);
 			LanguageRegistry.addName(waterMoving, "Elysium Water Flowing");
 			
 			Property idPortalCoreBlock = Elysium.mainConfiguration.getBlock("idPortalCoreBlock.id", DefaultProps.idPortalCoreBlock);
 			portalCore = new ElysiumBlockPortalCore(idPortalCoreBlock.getInt(), Material.glass).setHardness(5F).setStepSound(Block.soundGlassFootstep).setUnlocalizedName("portalCore");
 			ClientProxy.proxy.registerBlock(portalCore);
 			LanguageRegistry.addName(portalCore, "Elysian Portal Block");
 
 			Property idShellsBlock = Elysium.mainConfiguration.getBlock("idShellsBlock.id", DefaultProps.idShellsBlock);
 			shellFloatingBlock = new BlockShells(idShellsBlock.getInt()).setHardness(0.0F).setStepSound(Block.soundGlassFootstep).setUnlocalizedName("shell");
 			ClientProxy.proxy.registerBlock(shellFloatingBlock);
 			LanguageRegistry.addName(shellFloatingBlock, "Shell");
 
 			Property idConchBlock = Elysium.mainConfiguration.getBlock("idConchBlock.id", DefaultProps.idConchBlock);
 			conchFloatingBlock = new BlockShells(idConchBlock.getInt()).setHardness(0.0F).setStepSound(Block.soundGlassFootstep).setUnlocalizedName("conch");
 			ClientProxy.proxy.registerBlock(conchFloatingBlock);
 			LanguageRegistry.addName(conchFloatingBlock, "Conch");
 
 			
 			
 			Block.dragonEgg.setCreativeTab(tabElysium);
 
 	        MinecraftForge.setToolClass(PickaxeFostimberItem, "pickaxe", 0);
 	        MinecraftForge.setToolClass(AxeFostimberItem, "axe", 0);
 	        MinecraftForge.setToolClass(ShovelFostimberItem, "shovel", 0);
 	        MinecraftForge.setToolClass(PickaxeStoneItem, "pickaxe", 1);
 	        MinecraftForge.setToolClass(AxeStoneItem, "axe", 1);
 	        MinecraftForge.setToolClass(ShovelStoneItem, "shovel", 1);
 
 	 		// Item Registry
 			Property idGracePrismItem = Elysium.mainConfiguration.getItem("idGracePrismItem.id", DefaultProps.idGracePrismItem);
 			GracePrismItem = new ItemGracePrism(idGracePrismItem.getInt()).setUnlocalizedName("gracecrystal");
 			LanguageRegistry.addName(GracePrismItem, "Grace Prism");
 
 			Property idWhistleItem = Elysium.mainConfiguration.getItem("idWhistleItem.id", DefaultProps.idWhistleItem);
 			WhistleItem = new ItemWhistle(idWhistleItem.getInt()).setUnlocalizedName("enderdrum");
 			LanguageRegistry.addName(WhistleItem, "Sin titulo");
 		
 			Property idPepperSeedItem = Elysium.mainConfiguration.getItem("idPepperSeedItem.id", DefaultProps.idPepperSeedItem);
 			PepperSeedItem = new ElysiumItem(idPepperSeedItem.getInt()).setUnlocalizedName("seeds_pepper");
 		
 			Property idOverkillItem = Elysium.mainConfiguration.getItem("idOverkillItem.id", DefaultProps.idOverkillItem);
 			OverKillItem = new OverkillItem(idOverkillItem.getInt()).setUnlocalizedName("asd");
 			LanguageRegistry.addName(OverKillItem, "Overkill Item");
 		
 			Property idAsphodelPetalsItem = Elysium.mainConfiguration.getItem("idAsphodelPetalsItem.id", DefaultProps.idAsphodelPetalsItem);
 			AsphodelPetalsItem = new ElysiumItem(idAsphodelPetalsItem.getInt()).setUnlocalizedName("asphodelpetal");
 			LanguageRegistry.addName(AsphodelPetalsItem, "Asphodel Petals");
 			
 			Property idDebugItem = Elysium.mainConfiguration.getItem("idDebugItem.id", DefaultProps.idDebugItem);
 			DebugItem = new ItemDebug(idDebugItem.getInt()).setUnlocalizedName("debug");
 			LanguageRegistry.addName(DebugItem, "Modder Item");
 			
 			//Tools
 			EnumToolMaterial FOSTIMBER_MAT = EnumHelper.addToolMaterial("FOSTIMBER", 0, 59, 2.0F, 0, 15);
 
 			Property idWoodSwordItem = Elysium.mainConfiguration.getItem("idWoodSwordItem.id", DefaultProps.idWoodSwordItem);
 			SwordFostimberItem = new ElysiumSword(idWoodSwordItem.getInt(), FOSTIMBER_MAT).setUnlocalizedName("swordFostimber");
 			LanguageRegistry.addName(SwordFostimberItem, "Fostimber Sword");
 
 			Property idWoodPickaxeItem = Elysium.mainConfiguration.getItem("idWoodPickaxeItem.id", DefaultProps.idWoodPickaxeItem);
 			PickaxeFostimberItem = new ElysiumPickaxe(idWoodPickaxeItem.getInt(), FOSTIMBER_MAT).setUnlocalizedName("pickaxeFostimber");
 			LanguageRegistry.addName(PickaxeFostimberItem, "Fostimber Pickaxe");
 
 			Property idWoodAxeItem = Elysium.mainConfiguration.getItem("idWoodAxeItem.id", DefaultProps.idWoodAxeItem);
 			AxeFostimberItem = new ElysiumAxe(idWoodAxeItem.getInt(), FOSTIMBER_MAT).setUnlocalizedName("axeFostimber");
 			LanguageRegistry.addName(AxeFostimberItem, "Fostimber Axe");
 
 			Property idWoodShovelItem = Elysium.mainConfiguration.getItem("idWoodShovelItem.id", DefaultProps.idWoodShovelItem);
 			ShovelFostimberItem = new ElysiumShovel(idWoodShovelItem.getInt(), FOSTIMBER_MAT).setUnlocalizedName("shovelFostimber");
 			LanguageRegistry.addName(ShovelFostimberItem, "Fostimber Shovel");
 			
 			Property idWoodHoeItem = Elysium.mainConfiguration.getItem("idWoodHoeItem.id", DefaultProps.idWoodHoeItem);
 			HoeFostimberItem = new ElysiumHoe(idWoodHoeItem.getInt(), FOSTIMBER_MAT).setUnlocalizedName("hoeFostimber");
 			LanguageRegistry.addName(HoeFostimberItem, "Fostimber Hoe");
 
 			EnumToolMaterial STONE_MAT = EnumHelper.addToolMaterial("PALESTONE", 1, 131, 4.0F, 1, 5);
 
 			Property idStoneSwordItem = Elysium.mainConfiguration.getItem("idStoneSwordItem.id", DefaultProps.idStoneSwordItem);
 			SwordStoneItem = new ElysiumSword(idStoneSwordItem.getInt(), STONE_MAT).setUnlocalizedName("swordPalestone");
 			LanguageRegistry.addName(SwordStoneItem, "Palestone Sword");
 
 			Property idStonePickaxeItem = Elysium.mainConfiguration.getItem("idStonePickaxeItem.id", DefaultProps.idStonePickaxeItem);
 			PickaxeStoneItem = new ElysiumPickaxe(idStonePickaxeItem.getInt(), STONE_MAT).setUnlocalizedName("pickaxePalestone");
 			LanguageRegistry.addName(PickaxeStoneItem, "Palestone Pickaxe");
 
 			Property idStoneAxeItem = Elysium.mainConfiguration.getItem("idStoneAxeItem.id", DefaultProps.idStoneAxeItem);
 			AxeStoneItem = new ElysiumAxe(idStoneAxeItem.getInt(), STONE_MAT).setUnlocalizedName("axePalestone");
 			LanguageRegistry.addName(AxeStoneItem, "Palestone Axe");
 
 			Property idStoneShovelItem = Elysium.mainConfiguration.getItem("idStoneShovelItem.id", DefaultProps.idStoneShovelItem);
 			ShovelStoneItem = new ElysiumShovel(idStoneShovelItem.getInt(), STONE_MAT).setUnlocalizedName("shovelPalestone");
 			LanguageRegistry.addName(ShovelStoneItem, "Palestone Shovel");
 			
 			Property idStoneHoeItem = Elysium.mainConfiguration.getItem("idStoneHoeItem.id", DefaultProps.idStoneHoeItem);
 			HoeStoneItem = new ElysiumHoe(idStoneHoeItem.getInt(), STONE_MAT).setUnlocalizedName("hoePalestone");
 			LanguageRegistry.addName(HoeStoneItem, "Palestone Hoe");
 			
 			
 			
 			// Crafting Registry
 			GameRegistry.addRecipe(new ItemStack(GracePrismItem), new Object[] {"SMS","MDM","SMS", Character.valueOf('S'), Block.whiteStone, Character.valueOf('M'), Item.bucketMilk, Character.valueOf('D'), Item.diamond});
 			GameRegistry.addShapelessRecipe(new ItemStack(AsphodelPetalsItem, 2), new Object[] {FlowerBlock});
 
 			// Entity Registry
 			GameRegistry.registerTileEntity(ElysiumTileEntityPortal.class, "ElysiumTileEntityPortal");
 			
 //			MinecraftForge.setBlockHarvestLevel(ash, "shovel", 0);
 //		 	MinecraftForge.setBlockHarvestLevel(blockAsh, "shovel", 0);
 			
 			ClientProxy.proxy.RegisterRenders();
 		}
 		finally
 		{
 			mainConfiguration.save();
 		}
 	}
 	@Init
 	public void initialize(FMLInitializationEvent evt) {
 		
 		MinecraftForge.EVENT_BUS.register(new BonemealHandler());
 		
 		Plants.addGrassPlant(CurlgrassBlock, 0, 30);
 		Plants.addGrassPlant(FlowerBlock, 0, 10);
 		Plants.addGrassSeed(new ItemStack(PepperSeedItem), 10);
 		
 //		new LiquidStacks();
 //		CoreProxy.proxy.addAnimation();
 //		LiquidManager.liquids.add(new LiquidData(LiquidStacks.rawCandy, new ItemStack(rawCandyBucket), new ItemStack(Item.bucketEmpty)));
 //		LiquidManager.liquids.add(new LiquidData(LiquidStacks.milk, new ItemStack(Item.bucketMilk), new ItemStack(Item.bucketEmpty)));
 
 //		CoreProxy.proxy.initializeRendering();
 //		CoreProxy.proxy.initializeEntityRendering();
 		
 	
 		/** Register WorldProvider for Dimension **/
 		DimensionManager.registerProviderType(DimensionID, WorldProviderElysium.class, true);
 		DimensionManager.registerDimension(DimensionID, DimensionID);
 
 		
 		ElysiumPlainBiome = new BiomeGenElysium(25);
 		
 		
 		GameRegistry.registerWorldGenerator(new WorldGenElysium());
 
 	}
 
 	
 }
