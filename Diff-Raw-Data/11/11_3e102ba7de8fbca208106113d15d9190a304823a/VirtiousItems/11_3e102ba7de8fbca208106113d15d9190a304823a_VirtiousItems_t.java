 package teamm.mods.virtious.lib;
 
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import teamm.mods.virtious.Config;
 import teamm.mods.virtious.Virtious;
 import teamm.mods.virtious.item.EnumVirtiousToolMaterial;
 import teamm.mods.virtious.item.ItemCytoidDoor;
 import teamm.mods.virtious.item.ItemStickyBomb;
 import teamm.mods.virtious.item.TeleporterItem;
 import teamm.mods.virtious.item.VirtiousAxe;
 import teamm.mods.virtious.item.VirtiousBucketItem;
 import teamm.mods.virtious.item.VirtiousFood;
 import teamm.mods.virtious.item.VirtiousHoe;
 import teamm.mods.virtious.item.VirtiousItem;
 import teamm.mods.virtious.item.VirtiousPickaxe;
 import teamm.mods.virtious.item.VirtiousShovel;
 import teamm.mods.virtious.item.VirtiousSword;
 import net.minecraft.item.Item;
 import net.minecraftforge.common.Property;
 import net.minecraftforge.fluids.ItemFluidContainer;
 
 public class VirtiousItems 
 {
 	public static Item ingotBrazeum;
 	public static Item shardAquieus;
 	public static Item teleportWand;
 	public static Item gemPluthorium;
 	public static Item tak;
 	public static Item gemIlluminous;
 	public static Item amberwoodPickaxe;
 	public static Item amberwoodAxe;
 	public static Item amberwoodShovel;
 	public static Item amberwoodHoe;
 	public static Item amberwoodSword;
 	public static Item virtianwoodPickaxe;
 	public static Item virtianwoodAxe;
 	public static Item virtianwoodShovel;
 	public static Item virtianwoodHoe;
 	public static Item virtianwoodSword;
 	public static Item virtianstonePickaxe;
 	public static Item virtianstoneAxe;
 	public static Item virtianstoneShovel;
 	public static Item virtianstoneHoe;
 	public static Item virtianstoneSword;
 	public static Item deepstonePickaxe;
 	public static Item deepstoneAxe;
 	public static Item deepstoneShovel;
 	public static Item deepstoneHoe;
 	public static Item deepstoneSword;
 	public static Item brazeumPickaxe;
 	public static Item brazeumAxe;
 	public static Item brazeumShovel;
 	public static Item brazeumHoe;
 	public static Item brazeumSword;
 	public static Item aquieusPickaxe;
 	public static Item aquieusAxe;
 	public static Item aquieusShovel;
 	public static Item aquieusHoe;
 	public static Item aquieusSword;
 	public static Item pluthoriumPickaxe;
 	public static Item pluthoriumAxe;
 	public static Item pluthoriumShovel;
 	public static Item pluthoriumHoe;
 	public static Item pluthoriumSword;
 	public static Item cytoidDoor;
 	public static Item itemStickyBomb;
 	public static Item bucketAcid;
 	public static Item cookedBurhanch;
 	public static Item rawBurhanch;
 	public static Item dyeVeer;
 	public static Item seedVeer;
 	public static Item foodVois;
 	
 	/**
 	 * Loads all item objects
 	 */
 	public VirtiousItems()
 	{
 		Property idIngotBrazeum = Virtious.config.getItem("Brazeum Ingot ID", Config.idIngotBrazeum);
 		int ingotBrazeumId = idIngotBrazeum.getInt();
 		ingotBrazeum = new VirtiousItem(ingotBrazeumId).setUnlocalizedName("BrazeumIngot");
 		LanguageRegistry.addName(ingotBrazeum, "Brazeum Ingot");
 
 		Property idShardAquieus = Virtious.config.getItem("Virtious Shard ID", Config.idShardAquieus);
 		int shardAquieusId = idShardAquieus.getInt();
 		shardAquieus = new VirtiousItem(shardAquieusId).setUnlocalizedName("AquieusShard");
 		LanguageRegistry.addName(shardAquieus, "Aquieus Shard");
 		
 		Property idTeleporterWand = Virtious.config.getItem("idTeleporterWand", Config.idTeleporterWand);
 		int TeleporterWandID = idTeleporterWand.getInt();
		teleportWand = new TeleporterItem(TeleporterWandID).setUnlocalizedName("TeleportWand").setCreativeTab(null);
 		LanguageRegistry.addName(teleportWand, "TeleportWand");
 		
 		Property idGemPluthorium = Virtious.config.getItem("Pluthorium Gem", Config.idGemPluthorium);
 		int gemPluthoriumId = idGemPluthorium.getInt();
 		gemPluthorium = new VirtiousItem(gemPluthoriumId).setUnlocalizedName("PluthoriumGem");
 		LanguageRegistry.addName(gemPluthorium, "Pluthorium Gem");
 		
 		Property idTak = Virtious.config.getItem("Tak ID", Config.idTak);
 		int takId = idTak.getInt();
 		tak =new VirtiousItem(takId).setUnlocalizedName("Tak");
 		LanguageRegistry.addName(tak, "Tak");
 		
 		Property idgemIlluminous = Virtious.config.getItem("Illuminous Gem Id", Config.idGemIlluminous);
 		int gemIlluminousId = idgemIlluminous.getInt();
 		gemIlluminous = new VirtiousItem(gemIlluminousId).setUnlocalizedName("IlluminousGem");
 		LanguageRegistry.addName(gemIlluminous, "Illuminous Gem");
 		
 		Property idamberwoodPickaxe = Virtious.config.getItem("Amber Wood Pickaxe Id", Config.idamberwoodPickaxe);
 		amberwoodPickaxe = new VirtiousPickaxe(idamberwoodPickaxe.getInt(), EnumVirtiousToolMaterial.AMBERWOOD).setUnlocalizedName("AmberWoodPickaxe");
 		LanguageRegistry.addName(amberwoodPickaxe, "Amber Wood Pickaxe");
 		
 		Property idamberwoodAxe = Virtious.config.getItem("Amber Wood Axe Id", Config.idamberwoodAxe);
 		amberwoodAxe = new VirtiousAxe(idamberwoodAxe.getInt(), EnumVirtiousToolMaterial.AMBERWOOD).setUnlocalizedName("AmberWoodAxe");
 		LanguageRegistry.addName(amberwoodAxe, "Amber Wood Axe");
 		
 		Property idamberwoodShovel = Virtious.config.getItem("Amber Wood Shovel Id", Config.idamberwoodShovel);
 		amberwoodShovel = new VirtiousShovel(idamberwoodShovel.getInt(), EnumVirtiousToolMaterial.AMBERWOOD).setUnlocalizedName("AmberWoodShovel");
 		LanguageRegistry.addName(amberwoodShovel, "Amber Wood Shovel");
 		
 		Property idamberwoodHoe = Virtious.config.getItem("Amber Wood Hoe Id", Config.idamberwoodHoe);
 		amberwoodHoe = new VirtiousHoe(idamberwoodHoe.getInt(), EnumVirtiousToolMaterial.AMBERWOOD).setUnlocalizedName("AmberWoodHoe");
 		LanguageRegistry.addName(amberwoodHoe, "Amber Wood Hoe");
 		
 		Property idamberwoodSword = Virtious.config.getItem("Amber Wood Sword Id", Config.idamberwoodSword);
 		amberwoodSword = new VirtiousSword(idamberwoodSword.getInt(), EnumVirtiousToolMaterial.AMBERWOOD).setUnlocalizedName("AmberWoodSword");
 		LanguageRegistry.addName(amberwoodSword, "Amber Wood Sword");
 		
 		Property idvirtianwoodPickaxe = Virtious.config.getItem("Virtian Wood Pickaxe Id", Config.idvirtianwoodPickaxe);
 		virtianwoodPickaxe = new VirtiousPickaxe(idvirtianwoodPickaxe.getInt(), EnumVirtiousToolMaterial.VIRTIANWOOD).setUnlocalizedName("VirtianWoodPickaxe");
 		LanguageRegistry.addName(virtianwoodPickaxe, "Virtian Wood Pickaxe");
 		
 		Property idvirtianwoodAxe = Virtious.config.getItem("Virtian Wood Axe Id", Config.idvirtianwoodAxe);
 		virtianwoodAxe = new VirtiousAxe(idvirtianwoodAxe.getInt(), EnumVirtiousToolMaterial.VIRTIANWOOD).setUnlocalizedName("VirtianWoodAxe");
 		LanguageRegistry.addName(virtianwoodAxe, "Virtian Wood Axe");
 		
 		Property idvirtianwoodShovel = Virtious.config.getItem("Virtian Wood Shovel Id", Config.idvirtianwoodShovel);
 		virtianwoodShovel = new VirtiousShovel(idvirtianwoodShovel.getInt(), EnumVirtiousToolMaterial.VIRTIANWOOD).setUnlocalizedName("VirtianWoodShovel");
 		LanguageRegistry.addName(virtianwoodShovel, "Virtian Wood Shovel");
 		
 		Property idvirtianwoodHoe = Virtious.config.getItem("Virtian Wood Hoe Id", Config.idvirtianwoodHoe);
 		virtianwoodHoe = new VirtiousHoe(idvirtianwoodHoe.getInt(), EnumVirtiousToolMaterial.VIRTIANWOOD).setUnlocalizedName("VirtianWoodHoe");
 		LanguageRegistry.addName(virtianwoodHoe, "Virtian Wood Hoe");
 		
 		Property idvirtianwoodSword = Virtious.config.getItem("Virtian Wood Sword Id", Config.idvirtianwoodSword);
 		virtianwoodSword = new VirtiousSword(idvirtianwoodSword.getInt(), EnumVirtiousToolMaterial.VIRTIANWOOD).setUnlocalizedName("VirtianWoodSword");
 		LanguageRegistry.addName(virtianwoodSword, "Virtian Wood Sword");
 
 		Property idvirtianstonePickaxe = Virtious.config.getItem("Virtian Stone Pickaxe Id", Config.idvirtianstonePickaxe);
 		virtianstonePickaxe = new VirtiousPickaxe(idvirtianstonePickaxe.getInt(), EnumVirtiousToolMaterial.VIRTIANSTONE).setUnlocalizedName("VirtianStonePickaxe");
 		LanguageRegistry.addName(virtianstonePickaxe, "Virtian Stone Pickaxe");
 		
 		Property idvirtianstoneAxe = Virtious.config.getItem("Virtian Stone Axe Id", Config.idvirtianstoneAxe);
 		virtianstoneAxe = new VirtiousAxe(idvirtianstoneAxe.getInt(), EnumVirtiousToolMaterial.VIRTIANSTONE).setUnlocalizedName("VirtianStoneAxe");
 		LanguageRegistry.addName(virtianstoneAxe, "Virtian Stone Axe");
 		
 		Property idvirtianstoneShovel = Virtious.config.getItem("Virtian Stone Shovel Id", Config.idvirtianstoneShovel);
 		virtianstoneShovel = new VirtiousShovel(idvirtianstoneShovel.getInt(), EnumVirtiousToolMaterial.VIRTIANSTONE).setUnlocalizedName("VirtianStoneShovel");
 		LanguageRegistry.addName(virtianstoneShovel, "Virtian Stone Shovel");
 		
 		Property idvirtianstoneHoe = Virtious.config.getItem("Virtian Stone Hoe Id", Config.idvirtianstoneHoe);
 		virtianstoneHoe = new VirtiousHoe(idvirtianstoneHoe.getInt(), EnumVirtiousToolMaterial.VIRTIANSTONE).setUnlocalizedName("VirtianStoneHoe");
 		LanguageRegistry.addName(virtianstoneHoe, "Virtian Stone Hoe");
 		
 		Property idvirtianstoneSword = Virtious.config.getItem("Virtian Stone Sword Id", Config.idvirtianstoneSword);
 		virtianstoneSword = new VirtiousSword(idvirtianstoneSword.getInt(), EnumVirtiousToolMaterial.VIRTIANSTONE).setUnlocalizedName("VirtianStoneSword");
 		LanguageRegistry.addName(virtianstoneSword, "Virtian Stone Sword");
 
 		Property iddeepstonePickaxe = Virtious.config.getItem("Deep Stone Pickaxe Id", Config.iddeepstonePickaxe);
 		deepstonePickaxe = new VirtiousPickaxe(iddeepstonePickaxe.getInt(), EnumVirtiousToolMaterial.DEEPSTONE).setUnlocalizedName("DeepstonePickaxe");
 		LanguageRegistry.addName(deepstonePickaxe, "Deep Stone Pickaxe");
 		
 		Property iddeepstoneAxe = Virtious.config.getItem("Deep Stone Axe Id", Config.iddeepstoneAxe);
 		deepstoneAxe = new VirtiousAxe(iddeepstoneAxe.getInt(), EnumVirtiousToolMaterial.DEEPSTONE).setUnlocalizedName("DeepstoneAxe");
 		LanguageRegistry.addName(deepstoneAxe, "Deep Stone Axe");
 		
 		Property iddeepstoneShovel = Virtious.config.getItem("Deep Stone Shovel Id", Config.iddeepstoneShovel);
 		deepstoneShovel = new VirtiousShovel(iddeepstoneShovel.getInt(), EnumVirtiousToolMaterial.DEEPSTONE).setUnlocalizedName("deepstoneShovel");
 		LanguageRegistry.addName(deepstoneShovel, "Deep Stone Shovel");
 		
 		Property iddeepstoneHoe = Virtious.config.getItem("Deep Stone Hoe Id", Config.iddeepstoneHoe);
 		deepstoneHoe = new VirtiousHoe(iddeepstoneHoe.getInt(), EnumVirtiousToolMaterial.DEEPSTONE).setUnlocalizedName("DeepstoneHoe");
 		LanguageRegistry.addName(deepstoneHoe, "Deep Stone Hoe");
 		
 		Property iddeepstoneSword = Virtious.config.getItem("Deep Stone Sword Id", Config.iddeepstoneSword);
 		deepstoneSword = new VirtiousSword(iddeepstoneSword.getInt(), EnumVirtiousToolMaterial.DEEPSTONE).setUnlocalizedName("DeepstoneSword");
 		LanguageRegistry.addName(deepstoneSword, "Deep Stone Sword");
 		
 		Property idbrazeumPickaxe = Virtious.config.getItem("Brazeum Pickaxe Id", Config.idbrazeumPickaxe);
 		brazeumPickaxe = new VirtiousPickaxe(idbrazeumPickaxe.getInt(), EnumVirtiousToolMaterial.BRAZEUM).setUnlocalizedName("BrazeumPickaxe");
 		LanguageRegistry.addName(brazeumPickaxe, "Brazeum Pickaxe");
 		
 		Property idbrazeumAxe = Virtious.config.getItem("Brazeum Axe Id", Config.idbrazeumAxe);
 		brazeumAxe = new VirtiousAxe(idbrazeumAxe.getInt(), EnumVirtiousToolMaterial.BRAZEUM).setUnlocalizedName("BrazeumAxe");
 		LanguageRegistry.addName(deepstoneAxe, "Deep Stone Axe");
 		
 		Property idbrazeumShovel = Virtious.config.getItem("Brazeum Shovel Id", Config.idbrazeumShovel);
 		brazeumShovel = new VirtiousShovel(idbrazeumShovel.getInt(), EnumVirtiousToolMaterial.BRAZEUM).setUnlocalizedName("BrazeumShovel");
 		LanguageRegistry.addName(brazeumShovel, "Brazeum Shovel");
 		
 		Property idbrazeumHoe = Virtious.config.getItem("Brazeum Hoe Id", Config.idbrazeumHoe);
 		brazeumHoe = new VirtiousHoe(idbrazeumHoe.getInt(), EnumVirtiousToolMaterial.BRAZEUM).setUnlocalizedName("BrazeumHoe");
 		LanguageRegistry.addName(brazeumHoe, "Brazeum Hoe");
 		
 		Property idbrazeumSword = Virtious.config.getItem("Brazeum Sword Id", Config.idbrazeumSword);
 		brazeumSword = new VirtiousSword(idbrazeumSword.getInt(), EnumVirtiousToolMaterial.BRAZEUM).setUnlocalizedName("BrazeumSword");
 		LanguageRegistry.addName(brazeumSword, "Brazeum Sword");
 		
 		Property idaquieusPickaxe = Virtious.config.getItem("Aquieus Pickaxe Id", Config.idaquieusPickaxe);
 		aquieusPickaxe = new VirtiousPickaxe(idaquieusPickaxe.getInt(), EnumVirtiousToolMaterial.AQUIEUS).setUnlocalizedName("AquieusPickaxe");
 		LanguageRegistry.addName(aquieusPickaxe, "Aquieus Pickaxe");
 		
 		Property idaquieusAxe = Virtious.config.getItem("Aquieus Axe Id", Config.idaquieusAxe);
 		aquieusAxe = new VirtiousAxe(idaquieusAxe.getInt(), EnumVirtiousToolMaterial.AQUIEUS).setUnlocalizedName("AquieusAxe");
 		LanguageRegistry.addName(aquieusAxe, "Aquieus Axe");
 		
 		Property idaquieusShovel = Virtious.config.getItem("Aquieus Shovel Id", Config.idaquieusShovel);
 		aquieusShovel = new VirtiousShovel(idaquieusShovel.getInt(), EnumVirtiousToolMaterial.AQUIEUS).setUnlocalizedName("AquieusShovel");
 		LanguageRegistry.addName(aquieusShovel, "Aquieus Shovel");
 		
 		Property idaquieusHoe = Virtious.config.getItem("Aquieus Hoe Id", Config.idaquieusHoe);
 		aquieusHoe = new VirtiousHoe(idaquieusHoe.getInt(), EnumVirtiousToolMaterial.AQUIEUS).setUnlocalizedName("AquieusHoe");
 		LanguageRegistry.addName(aquieusHoe, "Aquieus Hoe");
 		
 		Property idaquieusSword = Virtious.config.getItem("Aquieus Sword Id", Config.idaquieusSword);
 		aquieusSword = new VirtiousSword(idaquieusSword.getInt(), EnumVirtiousToolMaterial.AQUIEUS).setUnlocalizedName("AquieusSword");
 		LanguageRegistry.addName(aquieusSword, "Aquieus Sword");
 		
 		Property idpluthoriumPickaxe = Virtious.config.getItem("Pluthorium Pickaxe Id", Config.idpluthoriumPickaxe);
 		pluthoriumPickaxe = new VirtiousPickaxe(idpluthoriumPickaxe.getInt(), EnumVirtiousToolMaterial.PLUTHORIUM).setUnlocalizedName("PluthoriumPickaxe");
 		LanguageRegistry.addName(pluthoriumPickaxe, "Pluthorium Pickaxe");
 		
 		Property idpluthoriumAxe = Virtious.config.getItem("Pluthorium Axe Id", Config.idpluthoriumAxe);
 		pluthoriumAxe = new VirtiousAxe(idpluthoriumAxe.getInt(), EnumVirtiousToolMaterial.PLUTHORIUM).setUnlocalizedName("PluthoriumAxe");
 		LanguageRegistry.addName(pluthoriumAxe, "Pluthorium Axe");
 		
 		Property idpluthoriumShovel = Virtious.config.getItem("Pluthorium Shovel Id", Config.idpluthoriumShovel);
 		pluthoriumShovel = new VirtiousShovel(idpluthoriumShovel.getInt(), EnumVirtiousToolMaterial.PLUTHORIUM).setUnlocalizedName("PluthoriumShovel");
 		LanguageRegistry.addName(pluthoriumShovel, "Pluthorium Shovel");
 		
 		Property idpluthoriumHoe = Virtious.config.getItem("Pluthorium Hoe Id", Config.idpluthoriumHoe);
 		pluthoriumHoe = new VirtiousHoe(idpluthoriumHoe.getInt(), EnumVirtiousToolMaterial.PLUTHORIUM).setUnlocalizedName("PluthoriumHoe");
 		LanguageRegistry.addName(pluthoriumHoe, "Pluthorium Hoe");
 		
 		Property idpluthoriumSword = Virtious.config.getItem("Pluthorium Sword Id", Config.idpluthoriumSword);
 		pluthoriumSword = new VirtiousSword(idpluthoriumSword.getInt(), EnumVirtiousToolMaterial.AQUIEUS).setUnlocalizedName("PluthoriumSword");
 		LanguageRegistry.addName(pluthoriumSword, "Pluthorium Sword");
 
 		Property idCytoidDoor = Virtious.config.getItem("Cytoid Door Item Id", Config.idCytoidDoorItem);
		cytoidDoor = new ItemCytoidDoor(idCytoidDoor.getInt()).setUnlocalizedName("CytoidDoorItem");
 		LanguageRegistry.addName(cytoidDoor, "Cytoid Door");
 
 		Property idStickyBomb = Virtious.config.getItem("Sticky Bomb Item Id", Config.idStickyBomb);
 		itemStickyBomb = new ItemStickyBomb(idStickyBomb.getInt());
 		LanguageRegistry.addName(itemStickyBomb, "Sticky Bomb");
 		
 		Property idAcidBucket = Virtious.config.getItem("Acid Bucket Item Id", Config.idAcidBucket);
 		bucketAcid = new VirtiousBucketItem(idAcidBucket.getInt(), VirtiousBlocks.virtiousAcid.blockID).setUnlocalizedName("BucketofAcid");
 		LanguageRegistry.addName(bucketAcid, "Acid Bucket");
 
 		Property idCookedBurhanch = Virtious.config.getItem("Cooked Burhanch Item Id", Config.idCookedBurhanch);
 		cookedBurhanch = new VirtiousFood(idCookedBurhanch.getInt(), 4, 0.4F).setUnlocalizedName("CookedBurhanch");
 		LanguageRegistry.addName(cookedBurhanch, "Cooked Burhanch");
 
 		Property idRawBurhanch = Virtious.config.getItem("Raw Burhanch Item Id", Config.idRawBurhanch);
 		rawBurhanch = new VirtiousFood(idRawBurhanch.getInt(), 9, 0.8F).setUnlocalizedName("RawBurhaunch");
 		LanguageRegistry.addName(rawBurhanch, "Raw Burhanch");
 		
 		Property idDyeVeer = Virtious.config.getItem("Dye Veer Item Id", Config.idDyeVeer);
 		dyeVeer = new VirtiousFood(idDyeVeer.getInt(), (int) 1.5, 0.3F).setUnlocalizedName("Veer");
 		LanguageRegistry.addName(dyeVeer, "Veer");
 		
 		Property idVeerSeed = Virtious.config.getItem("Veer Seeds Item Id", Config.idVeerSeed);
 		seedVeer = new VirtiousItem(idVeerSeed.getInt()).setUnlocalizedName("VeerSeeds");
 		LanguageRegistry.addName(seedVeer, "Veer Seeds");
 		
 		Property idVois = Virtious.config.getItem("Vois Item Id", Config.idVois);
 		foodVois = new VirtiousFood(idVois.getInt(), 5, 2.5F).setUnlocalizedName("Vois");
 		LanguageRegistry.addName(foodVois, "Vois");
 				
 		//TODO load all item objects
 	}
 }
