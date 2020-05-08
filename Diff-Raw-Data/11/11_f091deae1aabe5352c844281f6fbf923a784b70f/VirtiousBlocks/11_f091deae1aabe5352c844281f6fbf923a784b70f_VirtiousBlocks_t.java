 package teamm.mods.virtious.lib;
 
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.common.Property;
 import teamm.mods.virtious.Config;
 import teamm.mods.virtious.Virtious;
 import teamm.mods.virtious.block.BlockLeavesAmber;
 import teamm.mods.virtious.block.BlockLeavesVirtian;
 import teamm.mods.virtious.block.BlockLogAmber;
 import teamm.mods.virtious.block.BlockLogVirtian;
 import teamm.mods.virtious.block.BlockSaplingAmber;
 import teamm.mods.virtious.block.BlockSaplingVirtian;
 import teamm.mods.virtious.block.BlockVirtianGrass;
 import teamm.mods.virtious.block.DeepStone;
 import teamm.mods.virtious.block.NightwhiskerBlock;
 import teamm.mods.virtious.block.PortalBlock;
 import teamm.mods.virtious.block.VirtiousAcid;
 import teamm.mods.virtious.block.VirtiousBlock;
 
 public class VirtiousBlocks {
 	public static Block deepStone;
 	public static Block portalBlock;
 	public static Block virtianstone;
 	public static Block virtiancobblestone;
 	public static Block virtianGrass;
 	public static Block virtianSoil;
 	public static Block deepStoneMossy;
 	public static Block oreVIron;
 	public static Block oreTak;
 	public static Block oreBrazeum;
 	public static Block oreAquieus;
 	public static Block orePluthorium;
 	public static Block oreIlluminous;
 	public static Block oreDeepTak;
 	public static Block oreDeepIron;
 	public static Block oreDeepIlluminous;
 	public static Block blockTak;
 	public static Block blockNightwhisker;
 	public static Block logAmber;
 	public static Block leavesAmber;
 	public static Block saplingAmber;
 	public static Block logVirtian;
 	public static Block leavesVirtian;
 	public static Block saplingVirtian;
 	public static Block plankAmber;
 	public static Block plankVirtian;
 	public static Block canyonstone;
 	public static Block virtiousAcid;
 
 	/**
 	 * Loads all block objects
 	 */
 	public VirtiousBlocks()
 	{
 		Property idDeepStone = Virtious.config.getTerrainBlock("worldgen", "idDeepStone", Config.idDeepStone, null);
 		int deepStoneID = idDeepStone.getInt();
 		deepStone = new DeepStone(deepStoneID, Material.rock).setHardness(5.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("DeepStone");
 		registerBlock(deepStone, "Deep Stone");
 		
 //		Property idPortalBlock = Virtious.config.getBlock("idPortalBlock", Config.idPortal);
 //		int portalBlockID = idPortalBlock.getInt();
 //		portalBlock = new PortalBlock(portalBlockID).setHardness(1.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("vPortal");
 //		registerBlock(portalBlock, "Portal");
 		
 		Property idVirtianstone = Virtious.config.getTerrainBlock("worldgen", "Virtianstone Id", Config.idVirtianstone, null);
 		int virtianstoneID = idVirtianstone.getInt();
 		virtianstone = new VirtiousBlock(virtianstoneID, Material.rock).setHardness(1.5F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("Virtianstone");
 		registerBlock(virtianstone, "Virtianstone");
 		
 		Property idVirtiancobblestone = Virtious.config.getBlock("Virtian Cobblestone Id", Config.idvirtiancobblestone);
 		int virtiancobblestoneId = idVirtiancobblestone.getInt();
 		virtiancobblestone = new VirtiousBlock(virtiancobblestoneId, Material.rock).setHardness(2.0F).setResistance(10.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("CobbledVirtianstone");
 		registerBlock(virtiancobblestone, "Virtian Cobblestone");
 		
 		Property idVirtianGrass = Virtious.config.getTerrainBlock("worldgen", "Virtian Grass ID", Config.idVirtianGrass, null);
 		int virtianGrassId = idVirtianGrass.getInt();
 		virtianGrass = new BlockVirtianGrass(virtianGrassId).setHardness(0.6F).setStepSound(Block.soundGrassFootstep).setUnlocalizedName("virtianGrass");
 		registerBlock(virtianGrass, "Virtian Grass");
 		
 		Property idVirtianSoil = Virtious.config.getTerrainBlock("worldgen", "Virtian Soil ID", Config.idVirtianSoil, null);
 		int virtianSoilId = idVirtianSoil.getInt();
 		virtianSoil = new VirtiousBlock(virtianSoilId, Material.ground).setHardness(0.6F).setUnlocalizedName("VirtianSoil").setStepSound(Block.soundGravelFootstep);
 		registerBlock(virtianSoil, "Virtian Soil");
 		
 		Property iddeepStoneMossy = Virtious.config.getTerrainBlock("worldgen", "Mossy Deep Stone ID", Config.iddeepStoneMossy, null);
 		int deepStoneMossyId = iddeepStoneMossy.getInt();
 		deepStoneMossy = new VirtiousBlock(deepStoneMossyId, Material.rock).setStepSound(Block.soundStoneFootstep).setHardness(5.0F).setUnlocalizedName("MossyDeepStone");
 		registerBlock(deepStoneMossy, "Mossy Deep Stone");
 		
 		Property idoreVIron = Virtious.config.getBlock("Virtian Iron Ore ID", Config.idoreVIron);
 		int oreVIronId = idoreVIron.getInt();
 		oreVIron = new VirtiousBlock(oreVIronId, Material.rock).setStepSound(Block.soundStoneFootstep).setHardness(3.0F).setResistance(5.0F).setUnlocalizedName("VirtianIronOre");
 		registerBlock(oreVIron, "Virtian Iron Ore");
 		
 		Property idoreTak = Virtious.config.getBlock("Tak Ore Id", Config.idoreTak);
 		int oreTakId = idoreTak.getInt();
 		oreTak = new VirtiousBlock(oreTakId, Material.rock).setStepSound(Block.soundStoneFootstep).setHardness(3.2F).setResistance(5.2F).setUnlocalizedName("TakOre");
 		registerBlock(oreTak,"Tak Ore");
 		
 		Property idoreBrazeum = Virtious.config.getBlock("Brazeum Ore Id", Config.idoreBrazeum);
 		int oreBrazeumId = idoreBrazeum.getInt();
 		oreBrazeum = new VirtiousBlock(oreBrazeumId, Material.rock).setStepSound(Block.soundStoneFootstep).setHardness(3.0F).setResistance(5.0F).setUnlocalizedName("BrazeumOre");
 		registerBlock(oreBrazeum, "Brazeum Ore");
 		
 		Property idoreAquieus = Virtious.config.getBlock("Auieus Ore Id", Config.idoreAquieus);
 		int oreAquieusId = idoreAquieus.getInt();
 		oreAquieus = new VirtiousBlock(oreAquieusId, Material.rock).setStepSound(Block.soundStoneFootstep).setHardness(4.0F).setResistance(6.0F).setUnlocalizedName("AquieusOre").setLightValue(0.4F);
 		registerBlock(oreAquieus, "Aquieus Ore");
 		
 		Property idorePluthorium = Virtious.config.getBlock("Pluthorium Ore Id", Config.idorePluthorium);
 		int orePluthoriumId = idorePluthorium.getInt();
 		orePluthorium = new VirtiousBlock(orePluthoriumId, Material.rock).setStepSound(Block.soundStoneFootstep).setHardness(4.0F).setResistance(6.0F).setUnlocalizedName("PluthoriumOre");
 		registerBlock(orePluthorium, "Pluthorium Ore");
 		
 		Property idoreIlluminous = Virtious.config.getBlock("Illuminous Ore Id", Config.idoreIlluminous);
 		int oreIlluminousId = idoreIlluminous.getInt();
 		oreIlluminous = new VirtiousBlock(oreIlluminousId, Material.rock).setStepSound(Block.soundStoneFootstep).setHardness(3.0F).setLightValue(0.8F).setUnlocalizedName("IlluminousOre");
 		registerBlock(oreIlluminous, "Illuminous Ore");
 		
 		Property idoreDeepTak = Virtious.config.getBlock("Deep Tak Ore Id", Config.idoreDeepTak);
 		int oreDeepTakId = idoreDeepTak.getInt();
 		oreDeepTak = new VirtiousBlock(oreDeepTakId, Material.rock).setStepSound(Block.soundStoneFootstep).setHardness(5.0F).setResistance(0.8F).setUnlocalizedName("DeepTakOre");
 		registerBlock(oreDeepTak, "Deep Tak Ore");
 		
 		Property idoreDeepIron = Virtious.config.getBlock("Deep Iron Ore Id", Config.idoreDeepIron);
 		int oreDeepIronId = idoreDeepIron.getInt();
 		oreDeepIron = new VirtiousBlock(oreDeepIronId, Material.rock).setStepSound(Block.soundStoneFootstep).setHardness(5.5F).setResistance(8.5F).setUnlocalizedName("DeepIronOre");
 		registerBlock(oreDeepIron, "Deep Iron Ore");
 		
 		Property idoreDeepIlluminous = Virtious.config.getBlock("Deep Illuminous Ore Id", Config.idoreDeepIlluminous);
 		int oreDeepIlluminousId = idoreDeepIlluminous.getInt();
 		oreDeepIlluminous = new VirtiousBlock(oreDeepIlluminousId, Material.rock).setStepSound(Block.soundStoneFootstep).setHardness(5.0F).setResistance(8.0F).setLightValue(0.8F).setUnlocalizedName("DeepIlluminousOre");
 		registerBlock(oreDeepIlluminous,"Deep Illuminous Ore");
 		
 		Property idblockTak = Virtious.config.getBlock("Tak Block Id", Config.idblockTak);
 		int blockTakId = idblockTak.getInt();
 		blockTak = new VirtiousBlock(blockTakId, Material.rock).setHardness(5.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("TakBlock");
 		registerBlock(blockTak, "Tak Block");
 		
 		Property idNightwhisker = Virtious.config.getBlock("Nightwhisker Block Id", Config.idblockNightwhisker);
 		blockNightwhisker = new NightwhiskerBlock(idNightwhisker.getInt()).setHardness(0.0F).setResistance(0.0F).setUnlocalizedName("Nightwhisker");
 		registerBlock(blockNightwhisker, "Nightwhisker");
 		
 		Property idlogAmber = Virtious.config.getBlock("Amber Log Id", Config.idlogAmber);
 		logAmber = new BlockLogAmber(idlogAmber.getInt(), Material.wood).setHardness(2.0F).setStepSound(Block.soundWoodFootstep).setUnlocalizedName("logAmber");
 		registerBlock(logAmber, "Amber Log");
 		
 		Property idleavesAmber = Virtious.config.getBlock("Amber Leaves Id", Config.idleavesAmber);
 		leavesAmber = new BlockLeavesAmber(idleavesAmber.getInt(), Material.leaves).setHardness(0.2F).setLightOpacity(1).setStepSound(Block.soundGrassFootstep).setUnlocalizedName("leavesAmber").setLightValue(0.5F);
 		registerBlock(leavesAmber, "Amber Leaves");
 		
 		Property idsaplingAmber = Virtious.config.getBlock("Amber Sapling Id", Config.idsaplingAmber);
 		saplingAmber = new BlockSaplingAmber(idsaplingAmber.getInt()).setHardness(0.0F).setStepSound(Block.soundGrassFootstep).setUnlocalizedName("AmberWoodSapling");
 		registerBlock(saplingAmber, "Amber Sapling");
 				
 		Property idlogVirtian = Virtious.config.getBlock("Virtianwood Log Id", Config.idlogVirtian);
 		logVirtian = new BlockLogVirtian(idlogVirtian.getInt(), Material.wood).setHardness(2.0F).setStepSound(Block.soundWoodFootstep).setUnlocalizedName("logVirtian");
 		registerBlock(logVirtian, "Virtian Log");
 		
 		Property idleavesVirtian = Virtious.config.getBlock("Virtian Leaves Id", Config.idleavesVirtian);
 		leavesVirtian = new BlockLeavesVirtian(idleavesVirtian.getInt(), Material.leaves).setHardness(0.2F).setLightOpacity(1).setStepSound(Block.soundGrassFootstep).setUnlocalizedName("leavesVirtian");
 		registerBlock(leavesVirtian,"Virtian Leaves");
 		
 		Property idsaplingVirtian = Virtious.config.getBlock("Virtian Sapling ID", Config.idsaplingVirtian);
 		saplingVirtian = new BlockSaplingVirtian(idsaplingVirtian.getInt()).setHardness(0.0F).setStepSound(Block.soundGrassFootstep).setUnlocalizedName("VirtianwoodSapling");
 		registerBlock(saplingVirtian, "Virtian Sapling");
 		
 		Property idplanksAmber = Virtious.config.getBlock("Amber Wood Planks Id", Config.idplankAmber);
 		plankAmber = new VirtiousBlock(idplanksAmber.getInt(), Material.wood).setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundWoodFootstep).setUnlocalizedName("AmberWoodPlanks");
 		registerBlock(plankAmber, "Amber Wood Planks");
 		
 		Property idplanksVirtian = Virtious.config.getBlock("Virtian Wood Planks Id", Config.idplankVirtian);
 		plankVirtian = new VirtiousBlock(idplanksVirtian.getInt(), Material.wood).setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundWoodFootstep).setUnlocalizedName("VirtianWoodPlanks");
 		registerBlock(plankVirtian, "Virtian Wood Planks");
 		
 		Property idcanyonstone = Virtious.config.getBlock("Canyonstone", Config.idcanyonstone);
 		canyonstone = new VirtiousBlock(idcanyonstone.getInt(), Material.rock).setHardness(2.0F).setResistance(0.3F).setUnlocalizedName("Canyonstone");
 		registerBlock(canyonstone, "Canyonstone");
 		
		Property idvirtiousAcid = Virtious.config.getTerrainBlock("worldgen", "Virtious Acid Id", Config.idvirtiousAcid, null);
 		virtiousAcid = new VirtiousAcid(idvirtiousAcid.getInt()).setHardness(100.0F).setLightOpacity(3).setUnlocalizedName("virtiousAcid");
 		registerBlock(virtiousAcid, "Virtious Acid");
 		
 		MinecraftForge.setBlockHarvestLevel(oreTak, "pickaxe", 1);
 		MinecraftForge.setBlockHarvestLevel(oreBrazeum, "pickaxe", 1);
 		MinecraftForge.setBlockHarvestLevel(deepStone, "pickaxe", 2);
 		MinecraftForge.setBlockHarvestLevel(deepStoneMossy, "pickaxe", 2);
 		MinecraftForge.setBlockHarvestLevel(orePluthorium, "pickaxe", 3);
 		MinecraftForge.setBlockHarvestLevel(oreDeepIlluminous, "pickaxe", 2);
 		MinecraftForge.setBlockHarvestLevel(oreDeepIron, "pickaxe", 2);
 		MinecraftForge.setBlockHarvestLevel(oreDeepTak, "pickaxe", 2);
 		
 		//TODO add all block objects here
 	}
 
 	public void registerBlock(Block block, String name) {
 		GameRegistry.registerBlock(block, name);
 		LanguageRegistry.addName(block, name);
 	}
 }
