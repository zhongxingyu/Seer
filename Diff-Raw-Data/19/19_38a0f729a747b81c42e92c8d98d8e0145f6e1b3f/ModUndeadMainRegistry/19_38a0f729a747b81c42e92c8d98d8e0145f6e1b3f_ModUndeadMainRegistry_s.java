 package techguy.mods.undead.common;
 
 import java.io.File;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockHalfSlab;
 import net.minecraft.block.BlockStep;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.Minecraft;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.EnumCreatureType;
 import net.minecraft.item.EnumArmorMaterial;
 import net.minecraft.item.EnumToolMaterial;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemFood;
 import net.minecraft.item.ItemStack;
 import net.minecraft.potion.Potion;
 import net.minecraft.src.ModLoader;
 import net.minecraft.stats.Achievement;
 import net.minecraft.world.biome.BiomeGenBase;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.DimensionManager;
 import net.minecraftforge.common.EnumHelper;
 import net.minecraftforge.common.MinecraftForge;
 import techguy.mods.undead.client.ModUndeadClientProxy;
 import techguy.mods.undead.client.ModUndeadClientTickHandler;
 import techguy.mods.undead.client.ModUndeadPacketHandler;
 import techguy.mods.undead.client.ModUndeadRenderSummoningTable;
 import cpw.mods.fml.client.registry.ClientRegistry;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.PostInit;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.EntityRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import cpw.mods.fml.common.registry.TickRegistry;
 import cpw.mods.fml.relauncher.Side;
 
 @Mod(modid="UndeadPlus", name="Undead+", version="0.9.8")
 @NetworkMod
 (
 		clientSideRequired = true,
 		serverSideRequired = false,
 		channels = {"undead_plus"},
 		packetHandler = ModUndeadPacketHandler.class 
 		//connectionHandler = ConnectionHandler.class,TODO
 		)
 
 public class ModUndeadMainRegistry 
 {
 	//@SidedProxy(clientSide = "techguy.mods.undead.client.ModUndeadClientProxy", serverSide = "techguy.mods.undead.common.ModUndeadCommonProxy")
 
 	static Configuration config = new Configuration(new File(Minecraft.getMinecraftDir(), "/config/UndeadPlusConfig.cfg"));
 
 	static int portalFrameID = configProp();
 	static int cordycepsFungusBlockID;
 	static int summoningTableID;
 	static int gravestoneID;
 	static int dreadWoodID;
 	static int dreadPlanksID;
 	static int dreadPlankStairsID;
 	static int dreadPlankSlabsID;
 	static int reinforcedDoorID;
 	static int blueFlamesID;
 	static int immortiumOreID;
 	static int volatiteOreID;
 	static int inferniteOreID;
 	static int charcoalOreID;
 	static int endoreOreID;
 	static int cyriteOreID;
 	static int dimStoneID;
 	static int dimGrassID;
 	static int dimDirtID;
 	static int thornsID;
 	static int krelickBushID;
 	static int pyroseID;
 	static int necrobaneFlowerID;
 	static int necrobaneTrailID;
 	static int portalBlockID;
 
 	static int debugItemID;
 	static int necronomiconID;
 	static int cordycepsFungusID;
 	static int crowbarID;
 	static int reinforcedDoorItemID;
 	static int rottenBoneID;
 	static int immortiumShardID;
 	static int boneBladeID;
 	static int boneBowID;
 	static int immortiumArrowID;
 	static int volatiteIngotID;
 	static int cyriteIngotID;
 	static int krelickBerryID;
 	static int skullFragmentID;
 	static int infernalLighterID;
 	static int maggotMeatRawID;
 	static int maggotMeatCookedID;
 	static int volatiteHelmetID;
 	static int volatiteChestplateID;
 	static int volatiteLeggingsID;
 	static int volatiteBootsID;
 	static int cyriteHelmetID;
 	static int cyriteChestplateID;
 	static int cyriteLeggingsID;
 	static int cyriteBootsID;
 	static int cyritePickaxeID;
 	static int cyriteAxeID;
 	static int cyriteShovelID;
 	static int cyriteSwordID;
 	static int cyriteHoeID;
 	static int necrobaneAshID;
 	
 
 
 
 	public static CreativeTabs undeadCreativeTab = new ModUndeadCreativeTab("undeadPlusCreativeTab");
 	public static final Block portalFrame = new ModUndeadBlockGraveDimensionPortalFrame(portalFrameID, 3).setBlockName("portalFrameUDP").setCreativeTab(undeadCreativeTab).setHardness(1.5F).setResistance(10F);
 	public static final Block cordycepsFungusBlock = new ModUndeadBlockCordycepsFungus(cordycepsFungusBlockID, 47).setBlockName("cordycepsFungusBlockUDP");
 	public static final Block summoningTable = new ModUndeadBlockSummoningTable(summoningTableID, 0).setBlockName("summoningTableUDP").setCreativeTab(undeadCreativeTab).setHardness(4.2F).setResistance(10F);
	//public static final Block gravestone = new ModUndeadBlockGravestone(gravestoneID, 0, net.minecraft.src.ModUndeadTileEntityGravestone.class).setBlockName("gravestoneUDP").setCreativeTab(tabBlock);
 	public static final Block dreadWood = new ModUndeadBlockDreadWood(dreadWoodID, 0).setBlockName("dreadWoodUDP").setHardness(1.5F).setStepSound(Block.soundWoodFootstep).setCreativeTab(undeadCreativeTab);
 	public static final Block dreadPlanks = new ModUndeadBlockDreadPlanks(dreadPlanksID, 32).setBlockName("dreadPlanksUDP").setCreativeTab(undeadCreativeTab).setHardness(1F).setStepSound(Block.soundWoodFootstep);
 	public static final Block dreadPlankStairs = new ModUndeadBlockDreadPlankStairs(dreadPlankStairsID, dreadPlanks, 0).setBlockName("dreadPlankStairsUDP").setHardness(1F).setCreativeTab(undeadCreativeTab);
 	public static final Block dreadPlankSlabs = new ModUndeadBlockHalfSlab(dreadPlankSlabsID, false).setBlockName("dreadPlankSlabsUDP").setCreativeTab(undeadCreativeTab);
 	public static final Block reinforcedDoor = new ModUndeadBlockReinforcedDoor(reinforcedDoorID, Material.rock).setBlockName("reinforcedDoorUDP").setRequiresSelfNotify();
 	public static final Block infernoFlames = new ModUndeadBlockBlueInfernoFire(blueFlamesID, 31).setBlockName("infernoFlamesUDP").setLightValue(1F).setCreativeTab(undeadCreativeTab);
 	public static final Block immortiumOre = new ModUndeadBlockImmortiumOre(immortiumOreID, 1).setBlockName("immortiumOreUDP").setCreativeTab(undeadCreativeTab).setHardness(2F);
 	public static final Block volatiteOre = new ModUndeadBlockVolatiteOre(volatiteOreID, 17).setBlockName("volatiteOreUDP").setCreativeTab(undeadCreativeTab).setHardness(1.8F).setResistance(10F);
 	public static final Block inferniteOre = new ModUndeadBlockInferniteOre(inferniteOreID, 33).setBlockName("inferniteOreUDP").setCreativeTab(undeadCreativeTab).setHardness(3F).setResistance(10F);
 	public static final Block charcoalOre = new ModUndeadBlockCharcoalOre(charcoalOreID, 49).setBlockName("charcoalOreUDP").setCreativeTab(undeadCreativeTab).setHardness(1.2F).setResistance(10F);
 	public static final Block endoreOre = new ModUndeadBlockEndoreOre(endoreOreID, 65).setBlockName("endoreOreUDP").setCreativeTab(undeadCreativeTab).setHardness(2.7F).setResistance(10F);
 	public static final Block cyriteOre = new ModUndeadBlockCyriteOre(cyriteOreID, 81).setBlockName("cyriteOreUDP").setCreativeTab(undeadCreativeTab).setHardness(1.35F).setResistance(10F);
 	public static final Block dimStone = new ModUndeadBlockDimStone(dimStoneID, 48).setBlockName("dimStoneUDP").setCreativeTab(undeadCreativeTab).setHardness(1.5F).setStepSound(Block.soundStoneFootstep);
 	public static final Block dimGrass = new ModUndeadBlockDimGrass(dimGrassID).setBlockName("dimGrassUDP").setCreativeTab(undeadCreativeTab).setStepSound(Block.soundGrassFootstep);
 	public static final Block dimDirt = new ModUndeadBlockDimDirt(dimDirtID, 64).setBlockName("dimDirtUDP").setCreativeTab(undeadCreativeTab).setStepSound(Block.soundGravelFootstep);
 	public static final Block thorns = new ModUndeadBlockThorns(thornsID, 63).setBlockName("thornsUDP").setStepSound(Block.soundGrassFootstep).setCreativeTab(undeadCreativeTab);
 	public static final Block thornsBerry = new ModUndeadBlockThornsBerry(krelickBushID, 79).setBlockName("thornsBerryUDP").setStepSound(Block.soundGrassFootstep).setCreativeTab(undeadCreativeTab);
 	public static final Block pyrose = new ModUndeadBlockPyrose(pyroseID, 95).setBlockName("pyroseUDP").setLightValue(0.856F).setStepSound(Block.soundGrassFootstep).setCreativeTab(undeadCreativeTab);
 	public static final Block necrobaneFlower = new ModUndeadBlockNecrobane(necrobaneFlowerID, 111).setBlockName("necrobaneFlowerUDP").setStepSound(Block.soundGrassFootstep).setCreativeTab(undeadCreativeTab);
 	public static final Block necrobaneTrail = new ModUndeadBlockNecrobaneTrail(necrobaneTrailID, 14).setBlockName("necrobaneTrailID").setCreativeTab(undeadCreativeTab);
 
 	public static final BiomeGenBase grave = new ModUndeadBiomeGenGrave(65).setBiomeName("Graveyard");   
 	public static final ModUndeadBlockGravePortal dimPortalBlock = (ModUndeadBlockGravePortal) new ModUndeadBlockGravePortal(portalBlockID, 50).setHardness(-1.0F).setStepSound(Block.soundGlassFootstep).setLightValue(0.75F).setBlockName("UDPGravePortal").setCreativeTab(undeadCreativeTab);
 
 	static EnumToolMaterial toolCrowbar = EnumHelper.addToolMaterial("CROWBAR", 1, 250, 100F, 2, 5);
 	static EnumToolMaterial toolBone = EnumHelper.addToolMaterial("BONE", 1, 150, 12F, 0, 22);
 	public static final Item necronomicon = new ModUndeadItemNecronomicon(necronomiconID).setIconIndex(7).setItemName("necromoniconUDP").setCreativeTab(undeadCreativeTab);
 	public static final Item cordycepsFungus = new ModUndeadItemCordycepsFungus(cordycepsFungusID, 0, false).setPotionEffect(Potion.hunger.id, 10, 0, 1F).setIconIndex(3).setItemName("cordycepsFungusUDP").setCreativeTab(undeadCreativeTab);
 	public static final Item crowbar = new ModUndeadItemCrowbar(crowbarID, toolCrowbar).setItemName("crowbarUDP").setIconIndex(4).setCreativeTab(undeadCreativeTab);
 	public static final Item reinforcedDoorItem = new ModUndeadItemReinforcedDoor(reinforcedDoorItemID, Material.iron).setItemName("reinforcedDoorItemUDP").setIconIndex(10).setCreativeTab(undeadCreativeTab);
 	public static final Item rottenBone = new ModUndeadItemRottenBone(rottenBoneID).setItemName("rottenBoneUDP").setIconIndex(8).setCreativeTab(undeadCreativeTab);
 	public static final Item immortiumShard = new ModUndeadItemImmortiumShard(immortiumShardID).setItemName("immortiumShardUDP").setIconIndex(6).setCreativeTab(undeadCreativeTab);
 	public static final Item boneBlade = new ModUndeadItemBoneBlade(boneBladeID, toolBone).setItemName("boneBladeUDP").setIconIndex(66).setCreativeTab(undeadCreativeTab);
 	public static final Item boneBow = new ModUndeadItemBoneBow(boneBowID).setItemName("boneBowUDP").setIconIndex(2).setCreativeTab(undeadCreativeTab);
 	public static final Item immortiumArrow = new ModUndeadItemImmortiumArrow(immortiumArrowID).setItemName("immortiumArrowUDP").setIconIndex(5).setCreativeTab(undeadCreativeTab);
 	public static final Item volatiteIngot = new ModUndeadItemVolatiteIngot(volatiteIngotID).setItemName("volatiteIngotUDP").setIconIndex(64).setCreativeTab(undeadCreativeTab);
 	public static final Item cyriteIngot = new ModUndeadItemCyriteIngot(cyriteIngotID).setItemName("cyriteIngotUDP").setIconIndex(65).setCreativeTab(undeadCreativeTab);
 	public static final Item krelickBerry = new ModUndeadItemKrelickBerry(krelickBerryID, 2, false).setItemName("krelickBerryUDP").setIconIndex(11).setCreativeTab(undeadCreativeTab);
 	public static final Item skullFragment = new ModUndeadItemSkullFragment(skullFragmentID).setItemName("skullFragmentUDP").setIconIndex(13).setCreativeTab(undeadCreativeTab);
 	public static final Item infernalLighter = new ModUndeadItemInfernalLighter(infernalLighterID).setItemName("infernalLighterUDP").setIconIndex(29).setCreativeTab(undeadCreativeTab);
 	public static final Item maggotMeatRaw = new ModUndeadItemMaggotMeatRaw(maggotMeatRawID, 2, false).setItemName("maggotMeatRawUDP").setIconIndex(27).setCreativeTab(undeadCreativeTab);
 	public static final Item maggotMeatCooked = new ModUndeadItemMaggotMeatCooked(maggotMeatCookedID, 4, false).setItemName("maggotMeatCookedUDP").setIconIndex(43).setCreativeTab(undeadCreativeTab);
 	public static final Item necrobaneAsh = new ModUndeadItemNecrobaneAsh(necrobaneAshID).setItemName("necrobaneAshUDP").setCreativeTab(undeadCreativeTab).setIconIndex(15);
 	public static final Item debugItem = new ModUndeadItemDebug(debugItemID).setItemName("debugItemUDP");
 
 	static EnumArmorMaterial armourVolatite = EnumHelper.addArmorMaterial("VOLATITE", 350, new int[] {3, 8, 6, 3}, 10);
 	public static final Item volatiteHelmet = (new ModUndeadItemAmourVolatite(volatiteHelmetID, armourVolatite, 4, 0)).setIconIndex(0).setItemName("volatiteArmourHelmetUDP").setCreativeTab(undeadCreativeTab);
 	public static final Item volatiteChestplate = (new ModUndeadItemAmourVolatite(volatiteChestplateID, armourVolatite, 4, 1)).setIconIndex(16).setItemName("volatiteArmourPlateUDP").setCreativeTab(undeadCreativeTab);
 	public static final Item volatiteLeggings = (new ModUndeadItemAmourVolatite(volatiteLeggingsID, armourVolatite, 4, 2)).setIconIndex(32).setItemName("volatiteArmourLegsUDP").setCreativeTab(undeadCreativeTab);
 	public static final Item volatiteBoots = (new ModUndeadItemAmourVolatite(volatiteBootsID, armourVolatite, 4, 3)).setIconIndex(48).setItemName("volatiteArmourBootsUDP").setCreativeTab(undeadCreativeTab);
 
 	static EnumArmorMaterial armourCyrite = EnumHelper.addArmorMaterial("CYRITE", 350, new int[] {2, 7, 5, 2}, 9);
 	public static final Item cyriteHelmet = (new ModUndeadItemAmourCyrite(cyriteHelmetID, armourCyrite, 4, 0)).setIconIndex(1).setItemName("cyriteArmourHelmetUDP").setCreativeTab(undeadCreativeTab);
 	public static final Item cyriteChestplate = (new ModUndeadItemAmourCyrite(cyriteChestplateID, armourCyrite, 4, 1)).setIconIndex(17).setItemName("cyriteArmourPlateUDP").setCreativeTab(undeadCreativeTab);
 	public static final Item cyriteLeggings = (new ModUndeadItemAmourCyrite(cyriteLeggingsID, armourCyrite, 4, 2)).setIconIndex(33).setItemName("cyriteArmourLegsUDP").setCreativeTab(undeadCreativeTab);
 	public static final Item cyriteBoots = (new ModUndeadItemAmourCyrite(cyriteBootsID, armourCyrite, 4, 3)).setIconIndex(49).setItemName("cyriteArmourBootsUDP").setCreativeTab(undeadCreativeTab);
 
 	static EnumToolMaterial toolCyrite = EnumHelper.addToolMaterial("CYRITE", 5, 350, 15F, 1, 10);
 	public static final Item cyritePickaxe = new ModUndeadItemCyritePickaxe(cyritePickaxeID, toolCyrite).setItemName("cyritePickaxeUDP").setIconIndex(113).setCreativeTab(undeadCreativeTab);
 	public static final Item cyriteAxe = new ModUndeadItemCyriteAxe(cyriteAxeID, toolCyrite).setItemName("cyriteAxeUDP").setIconIndex(129).setCreativeTab(undeadCreativeTab);
 	public static final Item cyriteShovel = new ModUndeadItemCyriteShovel(cyriteShovelID, toolCyrite).setItemName("cyriteShovelUDP").setIconIndex(97).setCreativeTab(undeadCreativeTab);
 	public static final Item cyriteSword = new ModUndeadItemCyriteSword(cyriteSwordID, toolCyrite).setItemName("cyriteSwordUDP").setIconIndex(81).setCreativeTab(undeadCreativeTab).setCreativeTab(undeadCreativeTab);
 	public static final Item cyriteHoe = new ModUndeadItemCyriteHoe(cyriteHoeID, toolCyrite).setItemName("cyriteHoeUDP").setIconIndex(145).setCreativeTab(undeadCreativeTab).setCreativeTab(undeadCreativeTab);
 
 	public static final Achievement install = new Achievement(1600, "undeadInstall", 15, 0, Item.diamond, null).setSpecial().setIndependent().registerAchievement();
 	public static final Achievement hellHoundTame = new Achievement(1601, "undeadHoundTame", 13, 0, rottenBone, install).registerAchievement();
 	public static final Achievement thinkerDoor = new Achievement(1602, "undeadThinkerDoor", 17, -6, Item.doorWood, install).registerAchievement();
 	public static final Achievement scorcherKill = new Achievement(1603, "undeadScorcher", 17, -4, Item.bucketWater, install).registerAchievement();
 	public static final Achievement rotterKill = new Achievement(1604, "undeadRotter", 17, -2, Item.rottenFlesh, install).registerAchievement();
 	public static final Achievement bruteKill = new Achievement(1605, "undeadBrute", 17, 0, Item.diamond, install).registerAchievement();
 	public static final Achievement mummyKill = new Achievement(1606, "undeadMummy", 17, 2, Item.diamond, install).registerAchievement();
 	public static final Achievement frostbiteKill = new Achievement(1607, "undeadFrostbite", 17, 4, Item.snowball, install).registerAchievement();
 	public static final Achievement cordieKill = new Achievement(1608, "undeadCordie", 17, 6, cordycepsFungus, install).registerAchievement();
 	public static final Achievement ventKill = new Achievement(1609, "undeadVent", 17, 8, new ItemStack(debugItem, 1, 1), install).registerAchievement();
 
 	public static final Achievement crowbarCraft = new Achievement(1610, "undeadCrowbarCraft", 13, -6, crowbar, install).registerAchievement();
 	public static final Achievement immortiumMine = new Achievement(1611, "undeadImmortiumMine", 13, 2, immortiumShard, install).registerAchievement();
 	public static final Achievement summoningCraft = new Achievement(1612, "undeadSummoningCraft", 13, 4, summoningTable, immortiumMine).registerAchievement();
 	public static final Achievement rottenFlesh = new Achievement(1613, "undeadRottenFlesh", 13, -2, volatiteChestplate, install).registerAchievement();
 	
 	//public static final Achievement oneMobDown = new Achievement(1614, "undeadOneMobDown", 15, 7, Item.diamond, null).registerAchievement();
 	//public static final Achievement allMobDown = new Achievement(1615, "undeadAllMobDown", 15, 7, Item.diamond, null).registerAchievement();
 
 	//public static AchievementPage undeadPlusAchievements = new AchievementPage("Undead+", nameHere);
 
 
 	@Init
 	public void load(FMLInitializationEvent event)
 	{
 
 		Item.rottenFlesh = new ModUndeadItemRottenFleshReplacement(111, 4, 0.1F, true).setPotionEffect(Potion.hunger.id, 30, 0, 0.8F).setIconCoord(11, 5).setItemName("rottenFlesh");
 		DimensionManager.registerProviderType(70, ModUndeadGraveDimensionWorldProvider.class, true);
 		DimensionManager.registerDimension(70, 70);
 		ModUndeadClientProxy.registerRenderInformation();
 		TickRegistry.registerTickHandler(new ModUndeadClientTickHandler(), Side.CLIENT);
 		GameRegistry.registerCraftingHandler(new ModUndeadCraftingHandler());
 		GameRegistry.registerPickupHandler(new ModUndeadPickupHandler());
 		//AchievementPage.registerAchievementPage(undeadPlusAchievements);
 		//RenderingRegistry.instance().registerBlockHandler(new ModUndeadRenderGravestone());
 
 		Block.setBurnProperties(dreadPlanks.blockID, 2, 2);
 		Block.setBurnProperties(dreadWood.blockID, 2, 2);
 
 
 		EntityRegistry.registerGlobalEntityID(ModUndeadEntityInfectedZombie.class, "UDInfected", EntityRegistry.findGlobalUniqueEntityId(), 0xb9c200, 0xaf0914); 
 		EntityRegistry.addSpawn(ModUndeadEntityInfectedZombie.class, 101, 5, 6, EnumCreatureType.monster);
 
 		EntityRegistry.registerGlobalEntityID(ModUndeadEntityCoolZombie.class, "UDCool", EntityRegistry.findGlobalUniqueEntityId(), 0x1f1f1f, 0xe6e6e6); 
 		EntityRegistry.addSpawn(ModUndeadEntityCoolZombie.class, 10, 5, 6, EnumCreatureType.monster);
 
 		EntityRegistry.registerGlobalEntityID(ModUndeadEntityFeralZombie.class, "UDFeral", EntityRegistry.findGlobalUniqueEntityId(), 0x30551e, 0x9f1600); 
 		EntityRegistry.addSpawn(ModUndeadEntityFeralZombie.class, 10, 5, 6, EnumCreatureType.monster); 
 
 		EntityRegistry.registerGlobalEntityID(ModUndeadEntityKnight.class, "UDKnight", EntityRegistry.findGlobalUniqueEntityId(), 0xc5c5c5, 0xebebeb); 
 		EntityRegistry.addSpawn(ModUndeadEntityKnight.class, 10, 5, 6, EnumCreatureType.monster);
 
 		EntityRegistry.registerGlobalEntityID(ModUndeadEntityThinker.class, "UDThinker", EntityRegistry.findGlobalUniqueEntityId(), 0x1d3a14, 0xc87691);
 		EntityRegistry.addSpawn(ModUndeadEntityThinker.class, 4, 1, 3, EnumCreatureType.monster);
 
 		EntityRegistry.registerGlobalEntityID(ModUndeadEntityNecromancer.class, "UDNecromancer", EntityRegistry.findGlobalUniqueEntityId(), 0x100F10, 0X7f7f7f);
 		EntityRegistry.addSpawn(ModUndeadEntityNecromancer.class, 2, 0, 1, EnumCreatureType.monster);
 
 		EntityRegistry.registerGlobalEntityID(ModUndeadEntityVent.class, "UDVent", EntityRegistry.findGlobalUniqueEntityId(), 0x616b2c, 0x364b0c);
 		EntityRegistry.addSpawn(ModUndeadEntityVent.class, 4, 0, 4, EnumCreatureType.monster);
 
 		EntityRegistry.registerGlobalEntityID(ModUndeadEntityBrute.class, "UDBrute", EntityRegistry.findGlobalUniqueEntityId(), 0x00afaf, 0x676f65);
 		EntityRegistry.addSpawn(ModUndeadEntityBrute.class, 4, 2, 3, EnumCreatureType.monster);
 
 		EntityRegistry.registerGlobalEntityID(ModUndeadEntityZombrine.class, "UDZombrine", EntityRegistry.findGlobalUniqueEntityId(), 0x00afaf, 0xe4e0d3);
 		EntityRegistry.addSpawn(ModUndeadEntityZombrine.class, 2, 0, 1, EnumCreatureType.monster);
 
 		EntityRegistry.registerGlobalEntityID(ModUndeadEntityWidower.class, "UDWidower", EntityRegistry.findGlobalUniqueEntityId(), 0x1e1813, 0x00c10c);
 		EntityRegistry.addSpawn(ModUndeadEntityWidower.class, 2, 1, 2, EnumCreatureType.monster);
 
 		EntityRegistry.registerGlobalEntityID(ModUndeadEntityRotter.class, "UDRotter", EntityRegistry.findGlobalUniqueEntityId(), 0x3c3f13, 0xab8b8b);
 		EntityRegistry.addSpawn(ModUndeadEntityRotter.class, 100, 50, 60, EnumCreatureType.monster);
 
 		EntityRegistry.registerGlobalEntityID(ModUndeadEntityCrawler.class, "UDCrawler", EntityRegistry.findGlobalUniqueEntityId(), 0xffffff, 0x000000);
 
 		//EntityRegistry.registerGlobalEntityID(ModUndeadEntityTamedWidower.class, "UDTamedWidower", EntityRegistry.findGlobalUniqueEntityId(), 0x000000, 0xffffff);
 
 		EntityRegistry.registerGlobalEntityID(ModUndeadEntityInferno.class, "UDInferno", EntityRegistry.findGlobalUniqueEntityId(), 0x018383, 0xd4d3cf);
 		EntityRegistry.addSpawn(ModUndeadEntityInferno.class, 200, 0, 2, EnumCreatureType.monster, new BiomeGenBase []
 				{
 			BiomeGenBase.hell
 				});
 
 		EntityRegistry.registerGlobalEntityID(ModUndeadEntityMaggot.class, "UDMaggot", EntityRegistry.findGlobalUniqueEntityId(), 0xb4b485, 0xab0501);
 		EntityRegistry.addSpawn(ModUndeadEntityMaggot.class, 4, 2, 4, EnumCreatureType.monster, new BiomeGenBase[]
 				{
 			BiomeGenBase.plains,
 			BiomeGenBase.forest
 				});
 
 		EntityRegistry.registerGlobalEntityID(ModUndeadEntityFrostbite.class, "UDFrostbite", EntityRegistry.findGlobalUniqueEntityId(), 0x007487, 0x0031b3);
 		EntityRegistry.addSpawn(ModUndeadEntityFrostbite.class, 10, 5, 6, EnumCreatureType.monster,  new BiomeGenBase[] 
 				{   
 			BiomeGenBase.icePlains,
 			BiomeGenBase.iceMountains,
 			BiomeGenBase.frozenOcean,
 			BiomeGenBase.frozenRiver
 				});
 
 		EntityRegistry.registerGlobalEntityID(ModUndeadEntityBuccaneer.class, "UDBuccaneer", EntityRegistry.findGlobalUniqueEntityId(), 0x382101, 0xc1111a);
 
 		EntityRegistry.registerGlobalEntityID(ModUndeadEntityMummy.class, "UDMummy", EntityRegistry.findGlobalUniqueEntityId(), 0xf9bb3d, 0x2a2300);
 		EntityRegistry.addSpawn(ModUndeadEntityMummy.class, 10, 5, 6, EnumCreatureType.monster,  new BiomeGenBase[] 
 				{   
 			BiomeGenBase.desert
 				});
 
 		EntityRegistry.registerGlobalEntityID(ModUndeadEntityMudman.class, "UDMudman", EntityRegistry.findGlobalUniqueEntityId(), 0x16776b, 0x24311c);
 		EntityRegistry.addSpawn(ModUndeadEntityMudman.class, 10, 5, 6, EnumCreatureType.monster,  new BiomeGenBase[] 
 				{   
 			BiomeGenBase.swampland
 				});
 
 		EntityRegistry.registerGlobalEntityID(ModUndeadEntityCordie.class, "UDCordie", EntityRegistry.findGlobalUniqueEntityId(), 0x004807, 0x411447);
 		EntityRegistry.addSpawn(ModUndeadEntityCordie.class, 10, 1, 4, EnumCreatureType.monster,  new BiomeGenBase[] 
 				{   
 			BiomeGenBase.mushroomIsland,
 			BiomeGenBase.jungle,
 			BiomeGenBase.jungleHills
 				});
 
 		EntityRegistry.registerGlobalEntityID(ModUndeadEntityFlare.class, "UDFlare", EntityRegistry.findGlobalUniqueEntityId(), 0x964646, 0xb80000);
 		EntityRegistry.addSpawn(ModUndeadEntityFlare.class, 200, 1, 3, EnumCreatureType.monster, new BiomeGenBase[]
 				{
 			BiomeGenBase.hell
 				});
 
 		EntityRegistry.registerGlobalEntityID(ModUndeadEntityScorcher.class, "UDScorcher", EntityRegistry.findGlobalUniqueEntityId(), 0xa42c00, 0x350000);
 		EntityRegistry.addSpawn(ModUndeadEntityScorcher.class, 200, 1, 3, EnumCreatureType.monster, new BiomeGenBase[]
 				{
 			BiomeGenBase.hell	
 				});
 
 		EntityRegistry.registerGlobalEntityID(ModUndeadEntityHellHound.class, "UDHellHound", EntityRegistry.findGlobalUniqueEntityId(), 0x350000, 0xa5da03);
 		EntityRegistry.addSpawn(ModUndeadEntityHellHound.class, 200, 1, 7, EnumCreatureType.monster, new BiomeGenBase [] 
 				{
 			BiomeGenBase.hell
 				});
 
 		EntityRegistry.registerGlobalEntityID(ModUndeadEntityEndermark.class, "UDEndermark", EntityRegistry.findGlobalUniqueEntityId(), 0x000000, 0xd800f8);
 		EntityRegistry.addSpawn(ModUndeadEntityEndermark.class, 2, 0, 4, EnumCreatureType.monster, new BiomeGenBase [] 
 				{
 			BiomeGenBase.sky
 				});
 
 		/*EntityRegistry.registerGlobalEntityID(ModUndeadEntityZkuba.class, "UDZkuba", EntityRegistry.findGlobalUniqueEntityId(), 0xe4e0d3, 0xd0b600);
 		EntityRegistry.addSpawn(ModUndeadEntityZkuba.class, 3, 0, 3, EnumCreatureType.waterCreature, new BiomeGenBase [] 
 				{
 			BiomeGenBase.river,
 			BiomeGenBase.ocean
 				});*/
 
 		EntityRegistry.registerGlobalEntityID(ModUndeadEntitySurvivor.class, "UDSurvivor", EntityRegistry.findGlobalUniqueEntityId(), 0xe10000, 0xebebeb);
 		EntityRegistry.registerGlobalEntityID(ModUndeadEntityGhoul.class, "UDGhoul", EntityRegistry.findGlobalUniqueEntityId(), 0x84b568, 0x84b568);
 		EntityRegistry.registerGlobalEntityID(ModUndeadEntityTamedZombie.class, "UDTamedZombie", EntityRegistry.findGlobalUniqueEntityId());
 		EntityRegistry.registerModEntity(ModUndeadEntityImmortiumArrow.class, "UDImmortiumArrow", 1, this, 64, 20, false);
 		EntityRegistry.registerModEntity(ModUndeadEntityTeleportationArrow.class, "UDTeleportationArrow", 2, this, 64, 20, false);
 		EntityRegistry.registerModEntity(ModUndeadEntityFlamingArrow.class, "UDFlamingArrow", 3, this, 64, 20, false);
 		EntityRegistry.registerModEntity(ModUndeadEntityPoisonousBall.class, "UDPoisonBall", 4, this, 64, 120, true);
 		EntityRegistry.registerModEntity(ModUndeadEntityNecromancerFireball.class, "UDNecromancerBall", 5, this, 64, 120, true);
 
 
 
 		//TODO GameRegistry.registerBlock(portalFrame);
 		LanguageRegistry.addName(portalFrame, "Portal Frame");
 		GameRegistry.addRecipe(new ItemStack(portalFrame, 1), new Object [] {"#$#", "$$$", "#$#", '#', Item.bone, '$', Item.rottenFlesh});
 
 		LanguageRegistry.addName(necronomicon, "Necronomicon");
 		LanguageRegistry.addName(cordycepsFungus, "Cordyceps Fungus");
 
 		GameRegistry.registerBlock(summoningTable);
 		LanguageRegistry.addName(summoningTable, "Summoning Table");
 		GameRegistry.addRecipe(new ItemStack(summoningTable, 1), new Object [] {" # ", "%^%", "&%&", '#', necronomicon, '%', Block.obsidian, '^', Item.diamond, '&', immortiumShard});
 
 		ModUndeadRenderSummoningTable summoningRender = new ModUndeadRenderSummoningTable();
 		ClientRegistry.registerTileEntity(ModUndeadTileEntitySummoningTable.class, "summoningTableAndRenderer", summoningRender);
 
 
 		//GameRegistry.registerBlock(gravestone);
 		//LanguageRegistry.addName(gravestone, "Gravestone");
 		ModUndeadTileEntityGravestoneRenderer gravestoneRender = new ModUndeadTileEntityGravestoneRenderer();
 		//ClientRegistry.registerTileEntity(ModUndeadTileEntityGravestone.class, "gravestoneAndRenderer", gravestoneRender);
 
 
 		LanguageRegistry.addName(debugItem, "Undead+ Debug Item");
 		LanguageRegistry.addName(crowbar, "Crowbar");
 		GameRegistry.addRecipe(new ItemStack(crowbar, 1), new Object [] {"  #", " # ", "#  ", '#', Item.ingotIron});
 
 		GameRegistry.registerBlock(dreadWood);
 		LanguageRegistry.addName(dreadWood, "Dread Wood");
 
 		GameRegistry.registerBlock(dreadPlanks);
 		LanguageRegistry.addName(dreadPlanks, "Dread Planks");
 		GameRegistry.addRecipe(new ItemStack(dreadPlanks, 4), new Object [] { "#", '#', dreadWood});
 
 		GameRegistry.registerBlock(dreadPlankStairs);
 		LanguageRegistry.addName(dreadPlankStairs, "Dread Plank Stairs");
 		GameRegistry.addRecipe(new ItemStack(dreadPlankStairs, 4), new Object [] {"  #", " ##", "###", '#', dreadPlanks});
 		GameRegistry.addRecipe(new ItemStack(dreadPlankStairs, 4), new Object [] {"#  ", "## ", "###", '#', dreadPlanks});
 
 		GameRegistry.registerBlock(dreadPlankSlabs);
 		LanguageRegistry.addName(dreadPlankSlabs, "Dread Plank Slab");
 		GameRegistry.addRecipe(new ItemStack(dreadPlankSlabs, 6), new Object [] {"###", '#', dreadPlanks});
 
 
 		LanguageRegistry.addName(reinforcedDoorItem, "Reinforced Door");
 		GameRegistry.registerBlock(reinforcedDoor);
 		GameRegistry.addRecipe(new ItemStack(reinforcedDoorItem, 1), new Object [] {"$##", "###", "$##", '#', dreadPlanks, '$', Item.ingotIron});
 
 
 		LanguageRegistry.addName(rottenBone, "Rotten Bone");
 		GameRegistry.addRecipe(new ItemStack(rottenBone, 1), new Object [] { " # ", "#$#", " # ", '#', Item.rottenFlesh, '$', Item.bone});
 		GameRegistry.addRecipe(new ItemStack(Item.rottenFlesh, 4), new Object [] {"#", '#', rottenBone});
 
 
 		//TODO GameRegistry.registerBlock(infernoFlames);
 		LanguageRegistry.addName(infernoFlames, "Inferno Flames");
 
 		LanguageRegistry.addName(boneBlade, "Bone Blade");
 		GameRegistry.addRecipe(new ItemStack(boneBlade, 1), new Object [] {" # ", "%#%", " ^ ", '#', immortiumShard, '%', volatiteIngot, '^', Item.bone});
 
 		LanguageRegistry.addName(boneBow, "Bone Bow");
 		GameRegistry.addRecipe(new ItemStack(boneBow, 1), new Object [] {"#%^", "% ^", "#%^", '#', volatiteIngot, '%', Item.bone, '^', Item.silk});
 
 		LanguageRegistry.addName(immortiumArrow, "Immortium Arrow");
 		GameRegistry.addRecipe(new ItemStack(immortiumArrow, 4), new Object [] {"#", "^", "#", '#', immortiumShard, '^', Item.blazeRod});
 
 		GameRegistry.registerBlock(immortiumOre);
 		LanguageRegistry.addName(immortiumOre, "Immortium Ore");
 		LanguageRegistry.addName(immortiumShard, "Immortium Shard");
 		MinecraftForge.setBlockHarvestLevel(immortiumOre, "pickaxe", 2);
 
 		GameRegistry.registerBlock(volatiteOre);
 		LanguageRegistry.addName(volatiteOre, "Volatite Ore");
 		LanguageRegistry.addName(volatiteIngot, "Volatite Ingot");
 		MinecraftForge.setBlockHarvestLevel(volatiteOre, "pickaxe", 3);
 
 		GameRegistry.registerBlock(inferniteOre);
 		LanguageRegistry.addName(inferniteOre, "Infernite Ore");
 		MinecraftForge.setBlockHarvestLevel(inferniteOre, "pickaxe", 2);
 
 		GameRegistry.registerBlock(charcoalOre);
 		LanguageRegistry.addName(charcoalOre, "Charcoal Ore");
 		MinecraftForge.setBlockHarvestLevel(charcoalOre, "pickaxe", 0);
 
 		GameRegistry.registerBlock(endoreOre);
 		LanguageRegistry.addName(endoreOre, "Endore Ore");
 		MinecraftForge.setBlockHarvestLevel(endoreOre, "pickaxe", 3);
 
 		GameRegistry.registerBlock(cyriteOre);
 		LanguageRegistry.addName(cyriteOre, "Cyrite Ore");
 		LanguageRegistry.addName(cyriteIngot, "Cyrite Ingot");
 		MinecraftForge.setBlockHarvestLevel(cyriteOre, "pickaxe", 1);
 
 		GameRegistry.registerBlock(dimStone);
 		LanguageRegistry.addName(dimStone, "Stone");
 
 		GameRegistry.registerBlock(dimGrass);
 		LanguageRegistry.addName(dimGrass, "Grass");
 
 		GameRegistry.registerBlock(dimDirt);
 		LanguageRegistry.addName(dimDirt, "Dirt");
 
 		GameRegistry.registerBlock(thorns);
 		LanguageRegistry.addName(thorns, "Thorns");
 
 		GameRegistry.registerBlock(thornsBerry);
 		LanguageRegistry.addName(thornsBerry, "Krelick Berry Bush");
 		LanguageRegistry.addName(krelickBerry, "Krelick Berry");
 
 		GameRegistry.registerBlock(pyrose);
 		LanguageRegistry.addName(pyrose, "Pyrose");
 		GameRegistry.registerBlock(necrobaneFlower);
 		GameRegistry.registerBlock(necrobaneTrail);
 		LanguageRegistry.addName(necrobaneFlower, "Necrobane");
 		LanguageRegistry.addName(necrobaneAsh, "Necrobane Ash");
 
 
 		//TODO:GameRegistry.registerBlock(dimPortalBlock);
 		LanguageRegistry.addName(dimPortalBlock, "Grave Portal");
 
 		LanguageRegistry.addName(skullFragment, "Skull Fragment");
 		LanguageRegistry.addName(infernalLighter, "Infernal Lighter");
 		ModLoader.addShapelessRecipe(new ItemStack(infernalLighter, 1), new Object [] {skullFragment, cyriteIngot});
 
 		LanguageRegistry.addName(maggotMeatRaw, "Raw Maggot Meat");
 		LanguageRegistry.addName(maggotMeatCooked, "Cooked Maggot Meat");
 
 		LanguageRegistry.addName(volatiteHelmet, "Volatite Helmet");
 		LanguageRegistry.addName(volatiteChestplate, "Volatite Chestplate");
 		LanguageRegistry.addName(volatiteLeggings, "Volatite Leggings");
 		LanguageRegistry.addName(volatiteBoots, "Volatite Boots");
 		GameRegistry.addRecipe(new ItemStack(volatiteHelmet, 1), new Object [] { "###", "# #", '#', volatiteIngot});
 		GameRegistry.addRecipe(new ItemStack(volatiteChestplate, 1), new Object [] { "# #", "###", "###", '#', volatiteIngot});
 		GameRegistry.addRecipe(new ItemStack(volatiteLeggings, 1), new Object [] { "###", "# #", "# #", '#', volatiteIngot});
 		GameRegistry.addRecipe(new ItemStack(volatiteBoots, 1), new Object [] { "# #", "# #", '#', volatiteIngot});
 
 		LanguageRegistry.addName(cyriteHelmet, "Cyrite Helmet");
 		LanguageRegistry.addName(cyriteChestplate, "Cyrite Chestplate");
 		LanguageRegistry.addName(cyriteLeggings, "Cyrite Leggings");
 		LanguageRegistry.addName(cyriteBoots, "Cyrite Boots");
 		GameRegistry.addRecipe(new ItemStack(cyriteHelmet, 1), new Object [] { "###", "# #", '#', cyriteIngot});
 		GameRegistry.addRecipe(new ItemStack(cyriteChestplate, 1), new Object [] { "# #", "###", "###", '#', cyriteIngot});
 		GameRegistry.addRecipe(new ItemStack(cyriteLeggings, 1), new Object [] { "###", "# #", "# #", '#', cyriteIngot});
 		GameRegistry.addRecipe(new ItemStack(cyriteBoots, 1), new Object [] { "# #", "# #", '#', cyriteIngot});
 
 		LanguageRegistry.addName(cyritePickaxe, "Cyrite Pickaxe");
 		LanguageRegistry.addName(cyriteAxe, "Cyrite Axe");
 		LanguageRegistry.addName(cyriteShovel, "Cyrite Shovel");
 		LanguageRegistry.addName(cyriteSword, "Cyrite Sword");
 		LanguageRegistry.addName(cyriteHoe, "Cyrite Hoe");
 		GameRegistry.addRecipe(new ItemStack(cyritePickaxe, 1), new Object [] {"###", " % ", " % ", '#', cyriteIngot, '%', Item.stick});
 		GameRegistry.addRecipe(new ItemStack(cyriteAxe, 1), new Object [] {"##", "#%", " %", '#', cyriteIngot, '%', Item.stick});
 		GameRegistry.addRecipe(new ItemStack(cyriteShovel, 1), new Object [] {"#", "%", "%", '#', cyriteIngot, '%', Item.stick});
 		GameRegistry.addRecipe(new ItemStack(cyriteSword, 1), new Object [] {"#", "#", "%", '#', cyriteIngot, '%', Item.stick});
 		GameRegistry.addRecipe(new ItemStack(cyriteHoe, 1), new Object [] {"##", " %", " %", '#', cyriteIngot, '%', Item.stick});
 
 
 		GameRegistry.addRecipe(new ItemStack(Item.stick, 4), new Object [] { "#", "#", '#', dreadPlanks});
 		GameRegistry.addRecipe(new ItemStack(Item.pickaxeWood, 1), new Object [] { "###", " $ ", " $ ", '#', dreadPlanks, '$', Item.stick});
 		GameRegistry.addRecipe(new ItemStack(Item.shovelWood, 1), new Object [] { "#", "$", "$", '#', dreadPlanks, '$', Item.stick});
 		GameRegistry.addRecipe(new ItemStack(Item.axeWood, 1), new Object [] { "##", "#$", " $", '#', dreadPlanks, '$', Item.stick});
 		GameRegistry.addRecipe(new ItemStack(Item.axeWood, 1), new Object [] { "##", "$#", "$ ", '#', dreadPlanks, '$', Item.stick});
 		GameRegistry.addRecipe(new ItemStack(Item.hoeWood, 1), new Object [] { "##", " $", " $", '#', dreadPlanks, '$', Item.stick});
 		GameRegistry.addRecipe(new ItemStack(Item.axeWood, 1), new Object [] { "##", "$ ", "$ ", '#', dreadPlanks, '$', Item.stick});
 		GameRegistry.addRecipe(new ItemStack(Item.swordWood, 1), new Object [] { "#", "#", "$", '#', dreadPlanks, '$', Item.stick});
 		GameRegistry.addRecipe(new ItemStack(Item.bowlEmpty, 1), new Object [] { "# #", " # ", '#', dreadPlanks});
 		GameRegistry.addRecipe(new ItemStack(Block.workbench, 1), new Object [] {"##", "##", '#', dreadPlanks});
 		GameRegistry.addRecipe(new ItemStack(Item.redstone, 4), new Object [] {"#", '#', pyrose});
 		GameRegistry.addSmelting(dreadWood.blockID, new ItemStack(Item.coal, 2, 1), 0.4F);
 		GameRegistry.addSmelting(volatiteOre.blockID, new ItemStack(volatiteIngot, 1), 0.6F);
 		GameRegistry.addSmelting(cyriteOre.blockID, new ItemStack(cyriteIngot, 1), 0.8F);
 		GameRegistry.addSmelting(maggotMeatRaw.itemID, new ItemStack(maggotMeatCooked, 1), 0.3F);
 		GameRegistry.addSmelting(necrobaneFlower.blockID, new ItemStack(necrobaneAsh, 4), 0.2F);
 
 		//GameRegistry.addRecipe(new ItemStack(cyriteHelmet, 2), new Object [] {"#", '#', Block.dirt});
 		//GameRegistry.addRecipe(new ItemStack(cyriteChestplate, 2), new Object [] {"##", '#', Block.dirt});
 
 		LanguageRegistry.instance().addStringLocalization("itemGroup.undeadPlusCreativeTab", "Undead+");
 
 		LanguageRegistry.instance().addStringLocalization("achievement.undeadInstall", "Awesome!");
 		LanguageRegistry.instance().addStringLocalization("achievement.undeadInstall.desc", "Install Undead+");
 		LanguageRegistry.instance().addStringLocalization("achievement.undeadHoundTame", "Sit! Stay!");
 		LanguageRegistry.instance().addStringLocalization("achievement.undeadHoundTame.desc", "Tame a Hell Hound");
 		LanguageRegistry.instance().addStringLocalization("achievement.undeadThinkerDoor", "Knock, Knock");
 		LanguageRegistry.instance().addStringLocalization("achievement.undeadThinkerDoor.desc", "Kill a Thinker After He Opens A Door");
 
 		LanguageRegistry.instance().addStringLocalization("achievement.undeadScorcher", "Extinguisher");
 		LanguageRegistry.instance().addStringLocalization("achievement.undeadScorcher.desc", "Kill a Scorcher");
 
 		LanguageRegistry.instance().addStringLocalization("achievement.undeadRotter", "Need a Hand?");
 		LanguageRegistry.instance().addStringLocalization("achievement.undeadRotter.desc", "Kill a Rotter");
 
 		LanguageRegistry.instance().addStringLocalization("achievement.undeadBrute", "The Harder They Fall");
 		LanguageRegistry.instance().addStringLocalization("achievement.undeadBrute.desc", "Kill a Brute");
 
 		LanguageRegistry.instance().addStringLocalization("achievement.undeadMummy", "Reverse The Curse");
 		LanguageRegistry.instance().addStringLocalization("achievement.undeadMummy.desc", "Kill a Mummy");
 
 		LanguageRegistry.instance().addStringLocalization("achievement.undeadFrostbite", "Chill Out!");
 		LanguageRegistry.instance().addStringLocalization("achievement.undeadFrostbite.desc", "Kill a Frostbite");
 
 		LanguageRegistry.instance().addStringLocalization("achievement.undeadVent", "That Smelly Smell...");
 		LanguageRegistry.instance().addStringLocalization("achievement.undeadVent.desc", "Kill a Vent");
 
 		LanguageRegistry.instance().addStringLocalization("achievement.undeadCordie", "Fun Guy");
 		LanguageRegistry.instance().addStringLocalization("achievement.undeadCordie.desc", "Kill a Cordie");
 
 		LanguageRegistry.instance().addStringLocalization("achievement.undeadCrowbarCraft", "A Free Man");
 		LanguageRegistry.instance().addStringLocalization("achievement.undeadCrowbarCraft.desc", "Craft a Crowbar");
 
 		LanguageRegistry.instance().addStringLocalization("achievement.undeadImmortiumMine", "Shard of Life");
 		LanguageRegistry.instance().addStringLocalization("achievement.undeadImmortiumMine.desc", "Collect an Immortium Shard");
 
 		LanguageRegistry.instance().addStringLocalization("achievement.undeadSummoningCraft", "Necromancer");
 		LanguageRegistry.instance().addStringLocalization("achievement.undeadSummoningCraft.desc", "Craft a Summoning Table");
 
 		LanguageRegistry.instance().addStringLocalization("achievement.undeadRottenFlesh", "Acquired Taste");
 		LanguageRegistry.instance().addStringLocalization("achievement.undeadRottenFlesh.desc", "Eat Rotten Flesh While in Full Volatite Armor");
 
 		//LanguageRegistry.instance().addStringLocalization("achievement.undeadOneMobDown", "One Down...");
 		//LanguageRegistry.instance().addStringLocalization("achievement.undeadOneMobDown.desc", "Kill One New Mob");
 
 		//LanguageRegistry.instance().addStringLocalization("achievement.undeadAllMobDown", "Slayer");
 		//LanguageRegistry.instance().addStringLocalization("achievement.undeadAllMobDown.desc", "Kill One of Each New Mob");
 
 
 
 		LanguageRegistry.instance().addStringLocalization("entity.UDInfected.name", "Infected Zombie");
 		LanguageRegistry.instance().addStringLocalization("entity.UDCool.name", "Cool Zombie");
 		LanguageRegistry.instance().addStringLocalization("entity.UDFeral.name", "Feral Zombie");
 		LanguageRegistry.instance().addStringLocalization("entity.UDKnight.name", "Knight");
 		LanguageRegistry.instance().addStringLocalization("entity.UDThinker.name", "Thinker");
 		LanguageRegistry.instance().addStringLocalization("entity.UDNecromancer.name", "Necromancer");
 		LanguageRegistry.instance().addStringLocalization("entity.UDVent.name", "Vent");
 		LanguageRegistry.instance().addStringLocalization("entity.UDBrute.name", "Brute");
 		LanguageRegistry.instance().addStringLocalization("entity.UDSkeletonWarrior.name", "Skeleton Warrior");
 		LanguageRegistry.instance().addStringLocalization("entity.UDZombrine.name", "Zombrine");
 		LanguageRegistry.instance().addStringLocalization("entity.UDWidower.name", "Widower");
 		LanguageRegistry.instance().addStringLocalization("entity.UDRotter.name", "Rotter");
 		LanguageRegistry.instance().addStringLocalization("entity.UDInferno.name", "Inferno");
 		LanguageRegistry.instance().addStringLocalization("entity.UDMaggot.name", "Maggot");
 		LanguageRegistry.instance().addStringLocalization("entity.UDFrostbite.name", "Frostbite");
 		LanguageRegistry.instance().addStringLocalization("entity.UDBuccaneer.name", "Buccaneer");
 		LanguageRegistry.instance().addStringLocalization("entity.UDMummy.name", "Mummy");
 		LanguageRegistry.instance().addStringLocalization("entity.UDMudman.name", "Mudman");
 		LanguageRegistry.instance().addStringLocalization("entity.UDCordie.name", "Cordie");
 		LanguageRegistry.instance().addStringLocalization("entity.UDFlare.name", "Flare");
 		LanguageRegistry.instance().addStringLocalization("entity.UDGhoul.name", "Ghoul");
 		LanguageRegistry.instance().addStringLocalization("entity.UDScorcher.name", "Scorcher");
 		LanguageRegistry.instance().addStringLocalization("entity.UDHellHound.name", "Hell Hound");
 		LanguageRegistry.instance().addStringLocalization("entity.UDCrawler.name", "Crawler");
 		LanguageRegistry.instance().addStringLocalization("entity.UDEndermark.name", "Endermark");
 		LanguageRegistry.instance().addStringLocalization("entity.UDZkuba.name", "Zkuba");
 		LanguageRegistry.instance().addStringLocalization("entity.Survivor.name", "Survivor");
 
 
 	}
 
 	public static int configProp() {
 
 		config.load();
 
 		portalFrameID = Integer.parseInt(config.getBlock( "Grave Dimension Portal Frame" , 300).value);
 		cordycepsFungusBlockID = Integer.parseInt(config.getBlock( "Cordyceps Fungus Block" , 301).value);
 		summoningTableID = Integer.parseInt(config.getBlock( "Summoning Table" , 302).value);
 		gravestoneID = Integer.parseInt(config.getBlock( "Gravestone" , 303).value);
 		dreadWoodID = Integer.parseInt(config.getBlock( "Dread Wood" , 304).value);
 		dreadPlanksID = Integer.parseInt(config.getBlock( "Dread Planks" , 305).value);
 		dreadPlankStairsID = Integer.parseInt(config.getBlock( "Dread Plank Stairs" , 306).value);
 		dreadPlankSlabsID = Integer.parseInt(config.getBlock( "Dread Plank Slabs" , 307).value);		
 		reinforcedDoorID = Integer.parseInt(config.getBlock( "Reinforced Door" , 308).value);
 		blueFlamesID = Integer.parseInt(config.getBlock( "Blue Flames" , 309).value);
 		immortiumOreID = Integer.parseInt(config.getBlock( "Immortium Ore" , 310).value);
 		volatiteOreID = Integer.parseInt(config.getBlock( "Volatite Ore" , 311).value);
 		inferniteOreID = Integer.parseInt(config.getBlock( "Infernite Ore" , 312).value);
 		charcoalOreID = Integer.parseInt(config.getBlock( "Charcoal Ore" , 313).value);
 		endoreOreID = Integer.parseInt(config.getBlock( "Endore Ore" , 314).value);
 		cyriteOreID = Integer.parseInt(config.getBlock( "Cyrite Ore" , 315).value);
 		dimStoneID = Integer.parseInt(config.getBlock( "Grave Dimension Stone" , 316).value);
 		dimGrassID = Integer.parseInt(config.getBlock( "Grave Dimension Grass" , 317).value);
 		dimDirtID = Integer.parseInt(config.getBlock( "Grave Dimension Dirt" , 318).value);
 		thornsID = Integer.parseInt(config.getBlock( "Thorns" , 319).value);
 		krelickBushID = Integer.parseInt(config.getBlock( "Krelick Berry Bush" , 320).value);
 		pyroseID = Integer.parseInt(config.getBlock( "Pyrose" , 321).value);
 		necrobaneFlowerID = Integer.parseInt(config.getBlock("Necrobane", 322).value);
 		portalBlockID = Integer.parseInt(config.getBlock( "Grave Dimension Portal" , 323).value);
 
 		debugItemID = Integer.parseInt(config.getItem("DebugItem",  "item", 11999).value);
 		necronomiconID = Integer.parseInt(config.getItem( "Necronomicon", "item", 12000).value);
 		cordycepsFungusID = Integer.parseInt(config.getItem( "Cordyceps Fungus", "item", 12001).value);
 		crowbarID = Integer.parseInt(config.getItem( "Crowbar", "item", 12002).value);
 		reinforcedDoorItemID = Integer.parseInt(config.getItem( "Reinforced Door Item", "item", 12003).value);
 		rottenBoneID = Integer.parseInt(config.getItem( "Rotten Bone", "item", 12004).value);
 		immortiumShardID = Integer.parseInt(config.getItem( "Immortium Shard", "item", 12005).value);
 		boneBladeID = Integer.parseInt(config.getItem( "Bone Blade", "item", 12006).value);
 		boneBowID = Integer.parseInt(config.getItem( "Bone Bow", "item", 12007).value);
 		immortiumArrowID = Integer.parseInt(config.getItem( "Immortium Arrow", "item", 12008).value);
 		volatiteIngotID = Integer.parseInt(config.getItem( "Volatite Ingot", "item", 12009).value);
 		cyriteIngotID = Integer.parseInt(config.getItem( "Cyrite Ingot", "item", 12010).value);
 		krelickBerryID = Integer.parseInt(config.getItem( "Krelick Berry", "item", 12011).value);
 		skullFragmentID = Integer.parseInt(config.getItem( "Skull Fragment", "item", 12025).value);
 		infernalLighterID = Integer.parseInt(config.getItem( "Infernal Lighter", "item", 12026).value);
 		maggotMeatRawID = Integer.parseInt(config.getItem("Raw Maggot Meat", "item", 12027).value);
 		maggotMeatCookedID = Integer.parseInt(config.getItem("Cooked Maggot Meat", "item", 12028).value);
 		necrobaneAshID = Integer.parseInt(config.getItem("Necrobane Ash", "item", 12029).value);
 
 		volatiteHelmetID = Integer.parseInt(config.getItem( "Volatite Helmet", "item", 12012).value);
 		volatiteChestplateID = Integer.parseInt(config.getItem( "Volatite Chestplate", "item", 12013).value);
 		volatiteLeggingsID = Integer.parseInt(config.getItem( "Volatite Leggings", "item", 12014).value);
 		volatiteBootsID = Integer.parseInt(config.getItem( "Volatite Boots", "item", 12015).value);
 		cyriteHelmetID = Integer.parseInt(config.getItem( "Cyrite Helmet", "item", 12016).value);
 		cyriteChestplateID = Integer.parseInt(config.getItem( "Cyrite Chestplate", "item", 12017).value);
 		cyriteLeggingsID = Integer.parseInt(config.getItem( "Cyrite Leggings", "item", 12018).value);
 		cyriteBootsID = Integer.parseInt(config.getItem( "Cyrite Boots", "item", 12019).value);
 		cyritePickaxeID = Integer.parseInt(config.getItem( "Cyrite Pickaxe", "item", 12020).value);
 		cyriteAxeID = Integer.parseInt(config.getItem( "Cyrite Axe", "item", 12021).value);
 		cyriteShovelID = Integer.parseInt(config.getItem( "Cyrite Shovel", "item", 12022).value);
 		cyriteSwordID = Integer.parseInt(config.getItem( "Cyrite Sword", "item", 12023).value);
 		cyriteHoeID = Integer.parseInt(config.getItem( "Cyrite Hoe", "item", 12024).value);
 
 		ModUndeadClientTickHandler.updatesEnabled = Boolean.parseBoolean(config.get( "Enable Update Check", "general", true).value);
 		//graveBiomeID = Integer.parseInt(config.getItem( "Grave Biome   ID must be below 256", "general", 25).value);
 		//graveDimensionID = Integer.parseInt(config.getItem( "Grave Dimension   ID must be below 256", "general", 70).value);
 
 		config.save();
 
 		return portalFrameID;
 	}
 
 	@PreInit
 	public void preLoad(FMLPreInitializationEvent evt)
 	{
 		addAudio();
 	}
 
 	@PostInit
 	public void postLoad(FMLPostInitializationEvent evt)
 	{
 		//new as well! How about that stuff after the mods are loaded/initialized?
 	}
 
 	public void addAudio()
 	{
 		mc.installResource("sound3/undeadPlusAudio/mobs/brute/die.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/brute/brutedie.mus"));
 		mc.installResource("sound3/undeadPlusAudio/mobs/brute/growl.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/brute/brutegrowl.mus"));
 		mc.installResource("sound3/undeadPlusAudio/mobs/brute/hit.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/brute/brutehit.mus"));
 
 		mc.installResource("sound3/undeadPlusAudio/mobs/buccaneer/die.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/buccaneer/buccaneerdie.mus"));
 		mc.installResource("sound3/undeadPlusAudio/mobs/buccaneer/growl.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/buccaneer/buccaneerliving.mus"));
 		mc.installResource("sound3/undeadPlusAudio/mobs/buccaneer/hit.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/buccaneer/buccaneerhit.mus"));
 
 		mc.installResource("sound3/undeadPlusAudio/mobs/feral/die.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/feral/feraldie.mus"));
 		mc.installResource("sound3/undeadPlusAudio/mobs/feral/growl.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/feral/feralgrowl.mus"));
 		mc.installResource("sound3/undeadPlusAudio/mobs/feral/hit.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/feral/feralhit.mus"));
 
 		mc.installResource("sound3/undeadPlusAudio/mobs/frostbite/die.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/frostbite/frostbitedie.mus"));
 		mc.installResource("sound3/undeadPlusAudio/mobs/frostbite/growl.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/frostbite/frostbitemoan.mus"));
 		mc.installResource("sound3/undeadPlusAudio/mobs/frostbite/hit.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/frostbite/frostbitehit.mus"));
 
 		mc.installResource("sound3/undeadPlusAudio/mobs/hellhound/die.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/hellhound/hellhounddie.mus"));
 		mc.installResource("sound3/undeadPlusAudio/mobs/hellhound/growl.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/hellhound/hellhoundgrowl.mus"));
 		mc.installResource("sound3/undeadPlusAudio/mobs/hellhound/hit.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/hellhound/hellhoundhit.mus"));
 		mc.installResource("sound3/undeadPlusAudio/mobs/hellhound/bark.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/hellhound/hellhoundbark.mus"));
 
 		mc.installResource("sound3/undeadPlusAudio/mobs/knight/die.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/knight/knightdie.mus"));
 		mc.installResource("sound3/undeadPlusAudio/mobs/knight/growl.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/knight/knightmoan.mus"));
 		mc.installResource("sound3/undeadPlusAudio/mobs/knight/hit.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/knight/knighthit.mus"));
 
 		mc.installResource("sound3/undeadPlusAudio/mobs/mudman/die.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/mudman/mudmandie.mus"));
 		mc.installResource("sound3/undeadPlusAudio/mobs/mudman/growl.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/mudman/mudmanmoan.mus"));
 		mc.installResource("sound3/undeadPlusAudio/mobs/mudman/hit.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/mudman/mudmanhit.mus"));
 
 		mc.installResource("sound3/undeadPlusAudio/mobs/mummy/die.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/mummy/mummydie.mus"));
 		mc.installResource("sound3/undeadPlusAudio/mobs/mummy/growl.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/mummy/mummymoan.mus"));
 		mc.installResource("sound3/undeadPlusAudio/mobs/mummy/hit.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/mummy/mummyhit.mus"));
 
 		mc.installResource("sound3/undeadPlusAudio/mobs/necromancer/die.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/necromancer/necromancerdie.mus"));
 		mc.installResource("sound3/undeadPlusAudio/mobs/necromancer/growl.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/necromancer/necromancergroan.mus"));
 		mc.installResource("sound3/undeadPlusAudio/mobs/necromancer/hit.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/necromancer/necromancerhit.mus"));
 
 		mc.installResource("sound3/undeadPlusAudio/mobs/rotter/die.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/rotter/rotterdie.mus"));
 		mc.installResource("sound3/undeadPlusAudio/mobs/rotter/growl.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/rotter/rottermoan.mus"));
 		mc.installResource("sound3/undeadPlusAudio/mobs/rotter/hit.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/rotter/rotterhit.mus"));
 
 		mc.installResource("sound3/undeadPlusAudio/mobs/scorcher/die.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/scorcher/scorcherdie.mus"));
 		mc.installResource("sound3/undeadPlusAudio/mobs/scorcher/growl.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/scorcher/scorcherliving.mus"));
 		mc.installResource("sound3/undeadPlusAudio/mobs/scorcher/hit.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/scorcher/scorcherhit.mus"));
 
 		mc.installResource("sound3/undeadPlusAudio/mobs/thinker/die.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/thinker/thinkerdie.mus"));
 		mc.installResource("sound3/undeadPlusAudio/mobs/thinker/growl.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/thinker/thinkermoan.mus"));
 		mc.installResource("sound3/undeadPlusAudio/mobs/thinker/hit.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/thinker/thinkerhit.mus"));
 
 		mc.installResource("sound3/undeadPlusAudio/mobs/vent/die.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/vent/ventdie.mus"));
 		mc.installResource("sound3/undeadPlusAudio/mobs/vent/growl.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/vent/ventbreath.mus"));
 		mc.installResource("sound3/undeadPlusAudio/mobs/vent/hit.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/vent/venthit.mus"));
 
 		mc.installResource("sound3/undeadPlusAudio/mobs/zombrine/growl.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/zombrine/zombrinegrowl.mus"));
 		mc.installResource("sound3/undeadPlusAudio/mobs/zombrine/vanish.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/zombrine/zombrinevanish.mus"));
 
 		mc.installResource("sound3/undeadPlusAudio/mobs/cordie/die.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/cordie/cordiedie.mus"));
 		mc.installResource("sound3/undeadPlusAudio/mobs/cordie/growl.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/cordie/cordiemoan.mus"));
 		mc.installResource("sound3/undeadPlusAudio/mobs/cordie/hit.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/cordie/cordiehit.mus"));
 
 		mc.installResource("sound3/undeadPlusAudio/mobs/flare/die.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/flare/flaredie.mus"));
 		mc.installResource("sound3/undeadPlusAudio/mobs/flare/growl.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/flare/flaregrowl.mus"));
 		mc.installResource("sound3/undeadPlusAudio/mobs/flare/hit.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/flare/flarehit.mus"));
 
 		mc.installResource("sound3/undeadPlusAudio/mobs/ghoul/die.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/ghoul/ghouldie.mus"));
 		mc.installResource("sound3/undeadPlusAudio/mobs/ghoul/growl.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/ghoul/ghoulcry.mus"));
 		mc.installResource("sound3/undeadPlusAudio/mobs/ghoul/hit.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/ghoul/ghoulhit.mus"));
 
 		mc.installResource("sound3/undeadPlusAudio/mobs/inferno/die.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/inferno/infernodie.mus"));
 		mc.installResource("sound3/undeadPlusAudio/mobs/inferno/growl.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/inferno/infernogrowl.mus"));
 		mc.installResource("sound3/undeadPlusAudio/mobs/inferno/hit.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/inferno/infernohit.mus"));
 
 		mc.installResource("sound3/undeadPlusAudio/mobs/zkuba/die.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/zkuba/zkubadie.mus"));
 		mc.installResource("sound3/undeadPlusAudio/mobs/zkuba/living.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/zkuba/zkubaliving.mus"));
 		mc.installResource("sound3/undeadPlusAudio/mobs/zkuba/hit.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/mobs/zkuba/zkubahit.mus"));
 
 		mc.installResource("sound3/undeadPlusAudio/misc/summon.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/misc/summon.mus"));
 		mc.installResource("sound3/undeadPlusAudio/misc/crowbar.ogg", new File(mc.mcDataDir,"mods/undeadPlusAudio/misc/crowbarBreak.mus"));
 		mc.installResource("sound3/undeadPlusAudio/misc/portal.ogg", new File(mc.mcDataDir, "mods/undeadPlusAudio/misc/portal.mus"));
 	}
 
 
 	/*public void renderInvBlock(RenderBlocks var1, Block var2, int var3, int var4)
 	{
 		if (var4 == gravestone.getRenderType())
 		{
 			TileEntityRenderer.instance.renderTileEntityAt(new ModUndeadTileEntityGravestone(), 0.0D, 0.0D, 0.0D, 0.0F);
 		}
 	}*/
 
 	protected Minecraft mc = Minecraft.getMinecraft();
 
 
 }
