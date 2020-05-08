 
 package net.minecraft.src;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Random;
 import java.util.List;
 import java.util.Iterator;
 
 import net.minecraft.src.forge.EnumHelperClient;
 import net.minecraft.src.forge.IBonemealHandler;
 import net.minecraft.src.forge.IDestroyToolHandler;
 import net.minecraft.src.forge.ISoundHandler;
 import net.minecraft.src.forge.MinecraftForge;
 import net.minecraft.src.forge.MinecraftForgeClient;
 import net.minecraft.src.vazkii.updatemanager.IUMAdvanced;
 import net.minecraft.src.vazkii.updatemanager.IUpdateManager;
 import net.minecraft.src.vazkii.updatemanager.ModType;
 import net.minecraft.src.vazkii.updatemanager.UMCore;
 import net.minecraft.client.Minecraft;
 
 public class mod_Ebon extends BaseMod implements IUpdateManager, IUMAdvanced
 {
 	public void load(){
     	
     	new ForgeHooks();
     	
     	UMCore.addMod(this);
 
     	ModLoader.setInGameHook(this, true, true);
     	ModLoader.setInGUIHook(this, true, true);
     	
         ModLoader.registerEntityID(net.minecraft.src.EntityEbonGhost.class, "EbonGhost", ModLoader.getUniqueEntityId());
         ModLoader.registerEntityID(net.minecraft.src.EntityEbonGhostFrg.class, "EbonGhostFRG", ModLoader.getUniqueEntityId());
         ModLoader.registerEntityID(net.minecraft.src.EntityPhantomChestSpawner.class, "PhantomChestSpawner", ModLoader.getUniqueEntityId());
         
         ModLoader.addSpawn("EbonGhost", 24, 1, 5, EnumCreatureType.monster, new BiomeGenBase[] {
             BiomeGenBase.hell
         });
         ModLoader.addSpawn("PhantomChestSpawner", phantomChestRarity, 1, 1, EnumCreatureType.monster);
 
         ModLoader.registerTileEntity(TileEntityPhantomChest.class, "TilePhantomChest", new RenderPhantomChest());
         phantomChestRenderID = ModLoader.getUniqueBlockModelID(this, true);
         
         quicksand = (new BlockQuicksand(quicksandID, 3)).setHardness(0.5F).setStepSound(Block.soundSandFootstep).setBlockName("quicksand");
         ebonstone = (new BlockEbonStone(ebonstoneID, 5)).setHardness(2.0F).setResistance(10F).setStepSound(Block.soundStoneFootstep).setBlockName("ebonstone");
         ebonblock = (new BlockOreStorageEbon(ebonblockID, 1)).setHardness(2.0F).setResistance(10F).setLightValue(0.8F).setStepSound(Block.soundMetalFootstep).setBlockName("ebonblock");
         ebontorch = (new BlockEbonTorch(ebontorchID, 6)).setHardness(0.0F).setLightValue(1.0F).setStepSound(Block.soundWoodFootstep).setBlockName("ebontorch").setRequiresSelfNotify();
         ebonglow = (new BlockEbonGlowstone(ebonglowID, 0, Material.rock)).setHardness(0.3F).setStepSound(Block.soundGlassFootstep).setLightValue(1.0F).setBlockName("ebonglow");
         darkobsidian = (new BlockEbonObsidian(darkobsidianID, 4)).setHardness(10F).setResistance(2000F).setLightValue(0.8F).setStepSound(Block.soundStoneFootstep).setBlockName("darkobsidian");
         soulgemblock = (new BlockOreStorageEbon(soulgemblockID, 2)).setHardness(2.0F).setResistance(10F).setLightValue(0.8F).setStepSound(Block.soundMetalFootstep).setBlockName("soulgemblock");
         bloodCrops = (new BlockBloodLeaf(bloodCropsID, 248)).setHardness(0.0F).setStepSound(Block.soundGrassFootstep).setBlockName("bloodCrops").disableStats().setRequiresSelfNotify();
         phantomChest = (new BlockPhantomChest(phantomChestID, Material.rock)).setLightValue(0.6F).setBlockUnbreakable().setBlockName("phantomChest");
         soulVase = (new BlockVaseOfSouls(soulVaseID)).setHardness(0.9F).setStepSound(Block.soundGravelFootstep).setBlockName("soulVase");
         
         soul = (new ItemEbonMod(soulID)).setIconCoord(13, 5).setItemName("soul");
         soulsub = (new ItemSoulPowder(soulsubID)).setIconCoord(14, 5).setItemName("soulsub");
         ebonstaffc = (new ItemEbonStaff(ebonstaffcID, 9)).setMaxStackSize(1).setIconCoord(1, 6).setItemName("ebonstaffc");
         ebonstaffu = (new Ebon3DItems(ebonstaffuID, EnumRarity.rare, "Uncharged")).setMaxStackSize(1).setIconCoord(0, 6).setItemName("ebonstaffu");
         staffshard = (new ItemEbonMod(staffshardID)).setIconCoord(15, 5).setMaxStackSize(1).setItemName("soulshard");
         darkfert = (new ItemZombieRemains(darkfertID)).setIconCoord(11, 5).setItemName("darkfert");
         ebonpick = (new ItemEbonPick(ebonpickID, EnumToolMaterial.EMERALD)).setIconCoord(5, 5).setItemName("ebonpick");
         ebonspade = (new ItemEbonSpade(ebonspadeID, EnumToolMaterial.EMERALD)).setIconCoord(6, 5).setItemName("ebonspade");
         ebonax = (new ItemEbonAx(ebonaxID, EnumToolMaterial.EMERALD)).setIconCoord(7, 5).setItemName("ebonaxe");
         corpsedust = (new ItemCorpseDust(corpsedustID)).setIconCoord(0, 5).setItemName("corpsedust");
         deaddust = (new ItemEbonMod(deaddustID)).setIconCoord(1, 5).setItemName("deaddust");
         ebongem = (new ItemEbonMod(ebongemID)).setIconCoord(3, 5).setItemName("ebongem");
         ebonsword = (new ItemEbonSword(ebonswordID, EnumToolMaterial.EMERALD)).setIconCoord(4, 5).setItemName("ebonsword");
         dustpile = (new ItemEbonMod(dustpileID)).setIconCoord(2, 5).setItemName("dustpile");
         glowdustEbon = (new ItemEbonMod(glowdustEbonID)).setIconCoord(10, 5).setItemName("glowdustEbon");
         eboncoal = (new ItemEbonMod(eboncoalID)).setIconCoord(9, 5).setItemName("eboncoal");
        ebonapple = new ItemEbonApple(ebonappleID, 10, 1.2F, false).setAlwaysEdible().setPotionEffect(Potion.regeneration.id, 120, 0, 1.0F).setIconCoord(12, 5).setItemName("ebonapple");
         ebonbow = (new ItemEbonBow(ebonbowID)).setIconCoord(8, 5).setItemName("ebonbow");
         soulorb = (new ItemOrbOfSouls(soulorbID, 20)).setIconCoord(2, 6).setItemName("soulorb");
         soulorbc = (new ItemEbonMod(soulorbcID, "Charged")).setMaxStackSize(1).setIconCoord(3, 6).setItemName("soulorbc");
         soulstaffc = (new ItemStaffOfSouls(soulstaffcID, 9)).setIconCoord(5, 6).setItemName("soulstaffc");
         soulstaffu = (new Ebon3DItems(soulstaffuID, EnumRarity.rare, "Uncharged")).setMaxStackSize(1).setIconCoord(4, 6).setItemName("soulstaffu");
         sould = (new ItemEbonMod(souldID)).setIconCoord(6, 6).setItemName("sould");
         soulgem = (new ItemEbonMod(soulgemID)).setIconCoord(7, 6).setItemName("soulgem");
         ebonscepter = (new ItemEbonScepter(ebonscepterID, EnumRarity.rare)).setMaxStackSize(1).setIconCoord(8, 6).setItemName("ebonscepter");
         tsEgg = (new ItemTSEgg(tsEggID, "EbonGhost", 0xB3B3B3)).setIconCoord(9, 9).setItemName("tsEgg");
        fsEgg = (new ItemTSEgg(fsEggID, "Strange Ghost", 0x230B0B)).setIconCoord(9, 9).setItemName("fsEgg");
         mortarPestle = (new ItemGrindstone(mortarPestleID)).setMaxStackSize(1).setIconCoord(9, 6).setItemName("mortarPestle");  
         bloodPowder = (new ItemEbonMod(bloodPowderID)).setIconCoord(11, 6).setItemName("bloodPowder");
         bloodSeeds = (new ItemBloodSeed(bloodSeedsID, bloodCrops.blockID, quicksand.blockID)).setIconCoord(12, 6).setItemName("bloodSeeds");
         bloodLeaf = (new ItemEbonMod(bloodLeafID)).setIconCoord(13, 6).setPotionEffect("+4").setItemName("bloodLeaf");
         necroTome = (new ItemNecroTome(necroTomeID)).setIconCoord(14, 6).setItemName("necroTome");
         ebonscepterVoid = (new ItemEbonScepterVoid(ebonscepterVoidID, EnumRarity.rare)).setIconCoord(1, 7).setItemName("ebonscepterVoid");
         ebonscepterZero = (new ItemEbonScepterZero(ebonscepterZeroID, EnumRarity.rare)).setIconCoord(0, 7).setItemName("ebonscepterZero");
         ebonscepterInfinity = (new ItemEbonScepterInfinity(ebonscepterInfinityID, EnumRarity.rare)).setIconCoord(15, 6).setItemName("ebonscepterInfinity");
         ebonArmor_Hood = (new ItemEbonArmor(ebonArmor_HoodID, EnumArmorMaterial.IRON, ModLoader.addArmor("ebon"), 0)).setIconCoord(2, 7).setItemName("ebonArmor_Hood");
         ebonArmor_RobeTop = (new ItemEbonArmor(ebonArmor_RobeTopID, EnumArmorMaterial.IRON, ModLoader.addArmor("ebon"), 1)).setIconCoord(3, 7).setItemName("ebonArmor_RobeTop");
         ebonArmor_RobeBottom = (new ItemEbonArmor(ebonArmor_RobeBottomID, EnumArmorMaterial.IRON , ModLoader.addArmor("ebon"), 2)).setIconCoord(4, 7).setItemName("ebonArmor_RobeBottom");
         ebonArmor_Shoes = (new ItemEbonArmor(ebonArmor_ShoesID, EnumArmorMaterial.IRON, ModLoader.addArmor("ebon"), 3)).setIconCoord(5, 7).setItemName("ebonArmor_Shoes");
         soulStone = (new ItemSoulStone(soulStoneID)).setIconCoord(6, 7).setMaxStackSize(1).setItemName("soulStone");
         ebonCloth = (new ItemEbonMod(ebonClothID)).setIconCoord(7, 7).setItemName("ebonCloth");
         wandOfRetrieval = (new ItemWandOfRetrieval(wandOfRetrievalID).setIconCoord(10, 6)).setItemName("wandOfRetrieval");
         mobSpawnerItem = (new ItemMobSpawnerItem(mobSpawnerItemID)).setIconCoord(1, 4).setItemName("mobSpawnerItem");
         phantomKey = (new ItemEbonMod(phantomKeyID)).setIconCoord(8, 7).setMaxStackSize(8).setItemName("phantomKey");
         altarBlueprint = (new ItemEbonAltarBlueprint(altarBlueprintID)).setIconCoord(12, 3).setItemName("altarBlueprint");
         plusiumCharm = (new ItemPlusiumCharm(plusiumCharmID)).setIconCoord(9, 7).setItemName("plusiumCharm");
         miniumCharm = (new ItemMiniumCharm(miniumCharmID)).setIconCoord(10, 7).setItemName("miniumCharm");
         soulVaseItem = (new ItemSoulVaseItem(soulVaseItemID, soulVase)).setIconCoord(13, 7).setItemName("soulVaseItem");
         gemOfDespair = (new ItemEbonMod(gemOfDespairID)).setIconCoord(11, 7).setItemName("gemOfDespair");
         lockWand = (new ItemWandOfImprisonment(lockWandID)).setIconCoord(12, 7).setItemName("lockWand");
 
         magicExhaustion  = new Potion(magicExhaustionPotionID, false, 0x65007b).setPotionName("Magical Exhaustion").setIconIndex(0, 1);
         
         mortarPestle.setContainerItem(mortarPestle);
         
         ModLoader.registerBlock(ebonstone);
         ModLoader.registerBlock(quicksand);
         ModLoader.registerBlock(ebonblock);
         ModLoader.registerBlock(ebonglow);
         ModLoader.registerBlock(ebontorch);
         ModLoader.registerBlock(darkobsidian);
         ModLoader.registerBlock(soulgemblock);
         ModLoader.registerBlock(phantomChest);
         ModLoader.registerBlock(soulVase);
         
         ModLoader.addName(ebonpick, "Ebon Pick");
         ModLoader.addName(ebonspade, "Ebon Spade");
         ModLoader.addName(ebonax, "Ebon Hatchet");
         ModLoader.addName(ebontorch, "Ebon Torch");
         ModLoader.addName(corpsedust, "Corpse Dust");
         ModLoader.addName(deaddust, "Undead Essence");
         ModLoader.addName(ebongem, "Ebon Gem");
         ModLoader.addName(quicksand, "Quicksand");
         ModLoader.addName(ebonsword, "Ebon Blade");
         ModLoader.addName(ebonblock, "Ebon Block");
         ModLoader.addName(ebonstone, "Ebon Stone");
         ModLoader.addName(dustpile, "Concentrated Undead Essence");
         ModLoader.addName(ebonglow, "Ebon Glowstone");
         ModLoader.addName(glowdustEbon, "Ebon Glowdust");
         ModLoader.addName(eboncoal, "Ebon Coal");
         ModLoader.addName(darkobsidian, "Ebon Obsidian");
         ModLoader.addName(ebonapple, "Ebon Apple");
         ModLoader.addName(soul, "Soul");
         ModLoader.addName(soulsub, "Soul Powder");
         ModLoader.addName(staffshard, "Ebon Shard");
         ModLoader.addName(ebonstaffu, "Ebon Staff");
         ModLoader.addName(ebonstaffc, "Ebon Staff");
         ModLoader.addName(ebonbow, "Ebon Bow");
         ModLoader.addName(darkfert, "Zombie Remains");
         ModLoader.addName(soulorb, "Orb Of Souls");
         ModLoader.addName(soulorbc, "Orb Of Souls");
         ModLoader.addName(soulstaffc, "Staff Of Souls");
         ModLoader.addName(soulstaffu, "Staff Of Souls");
         ModLoader.addName(sould, "Demonic Soul");
         ModLoader.addName(soulgem, "Soul Gem");
         ModLoader.addName(soulgemblock, "Soul Gem Block");
         ModLoader.addName(ebonscepter, "Ebon Scepter");
         ModLoader.addName(tsEgg, "Spawn Tormented Soul");
         ModLoader.addName(fsEgg, "Spawn Forgotten Soul");
         ModLoader.addName(mortarPestle, "Grindstone");
         ModLoader.addName(bloodPowder, "Blood Powder");
         ModLoader.addName(bloodSeeds, "Blood Seeds");
         ModLoader.addName(bloodLeaf, "Blood Leaf");
         ModLoader.addName(necroTome, "Necromancer's Lexicon");
         ModLoader.addName(ebonscepterVoid, "Void Scepter");
         ModLoader.addName(ebonscepterZero, "Zero Scepter");
         ModLoader.addName(ebonscepterInfinity, "Infinity Scepter");
         ModLoader.addName(ebonArmor_Hood, "Ebon Robe Hood");
         ModLoader.addName(ebonArmor_RobeTop, "Ebon Robe Top");
         ModLoader.addName(ebonArmor_RobeBottom, "Ebon Robe Bottom");
         ModLoader.addName(ebonArmor_Shoes, "Ebon Robe Shoes");
         ModLoader.addName(soulStone, "Soul Stone");
         ModLoader.addName(ebonCloth, "Ebon Cloth");
         ModLoader.addName(wandOfRetrieval, "Wand Of Retrieval");
         ModLoader.addName(mobSpawnerItem, "Mob Spawner");
         ModLoader.addName(phantomChest, "Phantom Chest");
         ModLoader.addName(phantomKey, "Phantom Key");
         ModLoader.addName(altarBlueprint, "Ancient Scroll");
         ModLoader.addName(plusiumCharm, "Plusium Charm");
         ModLoader.addName(miniumCharm, "Minium Charm");
         ModLoader.addName(soulVase, "Vase of Souls (Block)");
         ModLoader.addName(soulVaseItem, "Vase of Souls");
        ModLoader.addName(gemOfDespair, "Gem of Despair");
         ModLoader.addName(lockWand, "Wand of Imprisonment");
         
 		ModLoader.addLocalization("enchantment.level.11", "XI");
 		ModLoader.addLocalization("enchantment.level.12", "XII");
 		ModLoader.addLocalization("enchantment.level.13", "XII");
 		ModLoader.addLocalization("enchantment.level.14", "XIV");
 		ModLoader.addLocalization("enchantment.level.15", "XV");
 		ModLoader.addLocalization("enchantment.level.16", "XVI");
 		ModLoader.addLocalization("enchantment.level.17", "XVII");
 		ModLoader.addLocalization("enchantment.level.18", "XVIII");
 		ModLoader.addLocalization("enchantment.level.19", "XIX");
 		ModLoader.addLocalization("enchantment.level.20", "XX");
 		ModLoader.addLocalization("enchantment.level.21", "XXI");
 		ModLoader.addLocalization("enchantment.level.22", "XXII");
 		ModLoader.addLocalization("enchantment.level.23", "XXIII");
 		ModLoader.addLocalization("enchantment.level.24", "XXIV");
 		ModLoader.addLocalization("enchantment.level.25", "XXV");
 		ModLoader.addLocalization("enchantment.level.26", "XXVI");
 		ModLoader.addLocalization("enchantment.level.27", "XXVII");
 		ModLoader.addLocalization("enchantment.level.28", "XXVIII");
 		ModLoader.addLocalization("enchantment.level.29", "XXIX");
 		ModLoader.addLocalization("enchantment.level.30", "XXX");
 		ModLoader.addLocalization("enchantment.level.31", "XXXI");
 		ModLoader.addLocalization("enchantment.level.32", "XXXII");
 		ModLoader.addLocalization("enchantment.level.33", "XXXIII");
 		ModLoader.addLocalization("enchantment.level.34", "XXXIV");
 		ModLoader.addLocalization("enchantment.level.35", "XXXV");
 		ModLoader.addLocalization("enchantment.level.36", "XXXVI");
 		ModLoader.addLocalization("enchantment.level.37", "XXXVII");
 		ModLoader.addLocalization("enchantment.level.38", "XXXVIII");
 		ModLoader.addLocalization("enchantment.level.39", "XXXIX");
 		ModLoader.addLocalization("enchantment.level.40", "XL");
 		ModLoader.addLocalization("enchantment.level.41", "XLI");
 		ModLoader.addLocalization("enchantment.level.42", "XLII");
 		ModLoader.addLocalization("enchantment.level.43", "XLIII");
 		ModLoader.addLocalization("enchantment.level.44", "XLIV");
 		ModLoader.addLocalization("enchantment.level.45", "XLV");
 		ModLoader.addLocalization("enchantment.level.46", "XLVI");
 		ModLoader.addLocalization("enchantment.level.47", "XLVII");
 		ModLoader.addLocalization("enchantment.level.48", "XLVIII");
 		ModLoader.addLocalization("enchantment.level.49", "XLIX");
 		ModLoader.addLocalization("enchantment.level.50", "L");
         
         recipes : {
         	
         ModLoader.addRecipe(new ItemStack(ebonstaffu, 1), new Object[] {
             "  T", " S ", "G  ", Character.valueOf('S'), Item.stick, Character.valueOf('T'), staffshard, Character.valueOf('G'), ebongem
         });
         ModLoader.addRecipe(new ItemStack(ebonstaffc, 1), new Object[] {
             "CCC", "CSC", "CCC", Character.valueOf('C'), corpsedust, Character.valueOf('S'), ebonstaffu
         });
         ModLoader.addRecipe(new ItemStack(ebonax, 1), new Object[] {
             "GG ", "GB ", " B ", Character.valueOf('G'), ebongem, Character.valueOf('B'), Item.bone
         });
         ModLoader.addRecipe(new ItemStack(ebonpick, 1), new Object[] {
             "GGG", " B ", " B ", Character.valueOf('G'), ebongem, Character.valueOf('B'), Item.bone
         });
         ModLoader.addRecipe(new ItemStack(ebonspade, 1), new Object[] {
             " G ", " B ", " B ", Character.valueOf('G'), ebongem, Character.valueOf('B'), Item.bone
         });
         ModLoader.addRecipe(new ItemStack(ebontorch, 2), new Object[] {
             "C", "B", Character.valueOf('C'), eboncoal, Character.valueOf('B'), Item.bone
         });
         ModLoader.addRecipe(new ItemStack(Block.web, 1), new Object[] {
             "S S", " S ", "S S", Character.valueOf('S'), Item.silk
         });
         ModLoader.addRecipe(new ItemStack(ebongem, 1), new Object[] {
             " E ", "EGE", " E ", Character.valueOf('E'), deaddust, Character.valueOf('G'), Item.diamond
         });
         ModLoader.addRecipe(new ItemStack(deaddust, 1), new Object[] {
             "CCC", "BBB", "GGG", Character.valueOf('C'), corpsedust, Character.valueOf('B'), Item.bone, Character.valueOf('G'), Item.gunpowder
         });
         ModLoader.addRecipe(new ItemStack(ebonblock, 1), new Object[] {
             "EEE", "EEE", "EEE", Character.valueOf('E'), ebongem
         });
         ModLoader.addRecipe(new ItemStack(ebonsword, 1), new Object[] {
             "E", "E", "B", Character.valueOf('E'), ebongem, Character.valueOf('B'), Item.bone
         });
         ModLoader.addRecipe(new ItemStack(ebonstone, 1), new Object[] {
             " E ", "EME", " E ", Character.valueOf('E'), deaddust, Character.valueOf('M'), Block.cobblestoneMossy
         });
         ModLoader.addRecipe(new ItemStack(dustpile, 1), new Object[] {
             "DD", "DD", Character.valueOf('D'), deaddust
         });
         ModLoader.addRecipe(new ItemStack(quicksand, 2), new Object[] {
             " D ", "DSD", " D ", Character.valueOf('D'), deaddust, Character.valueOf('S'), Block.sand
         });
         ModLoader.addRecipe(new ItemStack(darkfert, 1), new Object[] {
             "M", "B", Character.valueOf('M'), Item.rottenFlesh, Character.valueOf('S'), new ItemStack(Item.dyePowder, 1, 15)
         });
         ModLoader.addRecipe(new ItemStack(ebonbow, 1), new Object[] {
             "  G", " GB", "G  ", Character.valueOf('G'), ebongem, Character.valueOf('B'), Item.bow
         });
         ModLoader.addShapelessRecipe(new ItemStack(Item.dyePowder, 1, 1), new Object[] {
             darkfert
         });
         ModLoader.addShapelessRecipe(new ItemStack(deaddust, 4), new Object[] {
             dustpile
         });
         ModLoader.addShapelessRecipe(new ItemStack(ebongem, 9), new Object[] {
             ebonblock
         });
         ModLoader.addShapelessRecipe(new ItemStack(glowdustEbon, 1), new Object[] {
             deaddust, Item.lightStoneDust
         });
         ModLoader.addShapelessRecipe(new ItemStack(Block.slowSand, 1), new Object[] {
             Block.sand, soul
         });
         ModLoader.addRecipe(new ItemStack(ebonglow, 1), new Object[] {
             "DD", "DD", Character.valueOf('D'), glowdustEbon
         });
         ModLoader.addRecipe(new ItemStack(ebonglow, 1), new Object[] {
             " E ", "EGE", " E ", Character.valueOf('E'), deaddust, Character.valueOf('G'), Block.glowStone
         });
         ModLoader.addRecipe(new ItemStack(eboncoal, 1), new Object[] {
             " E ", "EGE", " E ", Character.valueOf('E'), deaddust, Character.valueOf('G'), Item.coal
         });
         ModLoader.addRecipe(new ItemStack(darkobsidian, 1), new Object[] {
             " E ", "EOE", " E ", Character.valueOf('E'), deaddust, Character.valueOf('O'), Block.obsidian
         });
         ModLoader.addRecipe(new ItemStack(ebonapple, 1), new Object[] {
             "EEE", "EAE", "EEE", Character.valueOf('E'), ebonblock, Character.valueOf('A'), Item.appleRed
         });
         ModLoader.addRecipe(new ItemStack(ebonbow, 1), new Object[] {
             "  G", " GB", "G  ", Character.valueOf('G'), ebongem, Character.valueOf('B'), Item.bow
         });
         ModLoader.addRecipe(new ItemStack(soulorb, 1), new Object[] {
         	"SEG", "EPE", "GES", Character.valueOf('G'), ebongem, Character.valueOf('P'), Item.eyeOfEnder, Character.valueOf('E'), deaddust, Character.valueOf('S'), soul
         });
         ModLoader.addRecipe(new ItemStack(soulorb, 1), new Object[] {
             "GES", "EPE", "SEG", Character.valueOf('G'), ebongem, Character.valueOf('P'), Item.eyeOfEnder, Character.valueOf('E'), deaddust, Character.valueOf('S'), soul
         });
         ModLoader.addRecipe(new ItemStack(soulstaffu, 1), new Object[] {
             " SH", " PS", "O  ", Character.valueOf('S'), soul, Character.valueOf('H'), staffshard, Character.valueOf('P'), Item.blazeRod, Character.valueOf('O'), soulorbc
         });
         ModLoader.addRecipe(new ItemStack(soulstaffc, 1), new Object[] {
             "SSS", "SXS", "SSS", Character.valueOf('S'), soul, Character.valueOf('X'), soulstaffu
         });
         ModLoader.addRecipe(new ItemStack(soulgem, 1), new Object[] {
             " E ", "EGE", " E ", Character.valueOf('E'), sould, Character.valueOf('G'), ebongem
         });
         ModLoader.addRecipe(new ItemStack(soulgemblock, 1), new Object[] {
             "EEE", "EEE", "EEE", Character.valueOf('E'), soulgem
         });
         ModLoader.addShapelessRecipe(new ItemStack(soulgem, 9), new Object[] {
             soulgemblock
         });
         ModLoader.addRecipe(new ItemStack(ebonscepter, 1), new Object[] {
             " SH", " PS", "O  ", Character.valueOf('S'), sould, Character.valueOf('H'), soulgem, Character.valueOf('P'), Item.blazeRod, Character.valueOf('O'), Item.goldNugget
         });
         ModLoader.addShapelessRecipe(new ItemStack(Item.lightStoneDust, 4), new Object[] {
         	mortarPestle, Block.glowStone
         });
         ModLoader.addShapelessRecipe(new ItemStack(glowdustEbon, 4), new Object[] {
         	mortarPestle, ebonglow
         });
         ModLoader.addShapelessRecipe(new ItemStack(corpsedust, 1), new Object[] {
         	mortarPestle, Item.rottenFlesh
         });
         ModLoader.addShapelessRecipe(new ItemStack(corpsedust, 2), new Object[] {
         	mortarPestle, Item.rottenFlesh, Item.rottenFlesh
         });
         ModLoader.addShapelessRecipe(new ItemStack(corpsedust, 3), new Object[] {
         	mortarPestle, Item.rottenFlesh, Item.rottenFlesh, Item.rottenFlesh
         });
         ModLoader.addShapelessRecipe(new ItemStack(corpsedust, 4), new Object[] {
         	mortarPestle, Item.rottenFlesh, Item.rottenFlesh, Item.rottenFlesh, Item.rottenFlesh
         });
         ModLoader.addShapelessRecipe(new ItemStack(corpsedust, 5), new Object[] {
         	mortarPestle, Item.rottenFlesh, Item.rottenFlesh, Item.rottenFlesh, Item.rottenFlesh, Item.rottenFlesh
         });
         ModLoader.addShapelessRecipe(new ItemStack(corpsedust, 6), new Object[] {
         	mortarPestle, Item.rottenFlesh, Item.rottenFlesh, Item.rottenFlesh, Item.rottenFlesh, Item.rottenFlesh, Item.rottenFlesh
         });
         ModLoader.addShapelessRecipe(new ItemStack(corpsedust, 7), new Object[] {
         	mortarPestle, Item.rottenFlesh, Item.rottenFlesh, Item.rottenFlesh, Item.rottenFlesh, Item.rottenFlesh, Item.rottenFlesh, Item.rottenFlesh
         });
         ModLoader.addShapelessRecipe(new ItemStack(corpsedust, 8), new Object[] {
         	mortarPestle, Item.rottenFlesh, Item.rottenFlesh, Item.rottenFlesh, Item.rottenFlesh, Item.rottenFlesh, Item.rottenFlesh, Item.rottenFlesh, Item.rottenFlesh
         });
         ModLoader.addShapelessRecipe(new ItemStack(soulsub, 1), new Object[] {
         	mortarPestle, soul, soul, soul
         });
         ModLoader.addShapelessRecipe(new ItemStack(bloodPowder, 2), new Object[] {
         	mortarPestle, soulgem
         });
         ModLoader.addRecipe(new ItemStack(bloodSeeds, 8), new Object[] {
             "SSS", "SPS", "SSS",  Character.valueOf('S'), Item.seeds, Character.valueOf('P'), bloodPowder
         });
         ModLoader.addShapelessRecipe(new ItemStack(soul, 1), new Object[] {
         	mortarPestle, bloodLeaf
         });
         ModLoader.addRecipe(new ItemStack(necroTome, 1), new Object[] {
             "PEP", "EBE", "PEP",  Character.valueOf('P'), bloodPowder, Character.valueOf('E'), deaddust, Character.valueOf('B'), Item.book
         });
         ModLoader.addRecipe(new ItemStack(necroTome, 1), new Object[] {
             "EPE", "PBP", "EPE",  Character.valueOf('P'), bloodPowder, Character.valueOf('E'), deaddust, Character.valueOf('B'), Item.book
         });
         ModLoader.addRecipe(new ItemStack(necroTome, 1, 128), new Object[] {
             "SSS", "SBS", "SSS",  Character.valueOf('S'), soul, Character.valueOf('B'), (new ItemStack(necroTome, 1, 256))
         });
         ModLoader.addRecipe(new ItemStack(necroTome, 1), new Object[] {
             "SSS", "SBS", "SSS",  Character.valueOf('S'), sould, Character.valueOf('B'), (new ItemStack(necroTome, 1, 256))
         });
         ModLoader.addRecipe(new ItemStack(ebonscepterVoid, 1), new Object[] {
             "DID", "DSD", "DDD",  Character.valueOf('D'), deaddust, Character.valueOf('I'), Item.rottenFlesh, Character.valueOf('S'), ebonscepter
         });
         ModLoader.addRecipe(new ItemStack(ebonscepterZero, 1), new Object[] {
             "DID", "DSD", "DDD",  Character.valueOf('D'), deaddust, Character.valueOf('I'), Item.gunpowder, Character.valueOf('S'), ebonscepter
         });
         ModLoader.addRecipe(new ItemStack(ebonscepterInfinity, 1), new Object[] {
             "DID", "DSD", "DDD",  Character.valueOf('D'), deaddust, Character.valueOf('I'), Item.bone, Character.valueOf('S'), ebonscepter
         });
         ModLoader.addRecipe(new ItemStack(soulStone, 1), new Object[] {
             "UEU", "EOE", "UEU",  Character.valueOf('U'), deaddust, Character.valueOf('O'), Block.obsidian, Character.valueOf('E'), Block.whiteStone
         });
         ModLoader.addRecipe(new ItemStack(ebonCloth, 4), new Object[] {
             "SES", "ECE", "SES",  Character.valueOf('E'), deaddust, Character.valueOf('S'), sould, Character.valueOf('C'), Block.cloth
         });
         ModLoader.addRecipe(new ItemStack(ebonArmor_Hood, 1), new Object[] {
             "CGC", "CSC", "C C",  Character.valueOf('C'), ebonCloth, Character.valueOf('G'), soulgem, Character.valueOf('S'), soulStone
         });
         ModLoader.addRecipe(new ItemStack(ebonArmor_RobeTop, 1), new Object[] {
             "CGC", "CSC", "CCC",  Character.valueOf('C'), ebonCloth, Character.valueOf('G'), soulgem, Character.valueOf('S'), soulStone
         });
         ModLoader.addRecipe(new ItemStack(ebonArmor_RobeBottom, 1), new Object[] {
             "CGC", "CCC", "C C",  Character.valueOf('C'), ebonCloth, Character.valueOf('G'), soulgem, Character.valueOf('S'), soulStone
         });
         ModLoader.addRecipe(new ItemStack(ebonArmor_Shoes, 1), new Object[] {
             "C C", "CGC",  Character.valueOf('C'), ebonCloth, Character.valueOf('G'), soulgem, Character.valueOf('S'), soulStone
         });
         ModLoader.addRecipe(new ItemStack(wandOfRetrieval, 1), new Object[] {
             "  G", " S ", "B  ",  Character.valueOf('B'), Item.blazeRod, Character.valueOf('G'), soulgem, Character.valueOf('S'), soulStone
         });
         ModLoader.addRecipe(new ItemStack(phantomKey, 1), new Object[] {
             " I", "G ",  Character.valueOf('I'), Item.ingotIron, Character.valueOf('G'), Item.goldNugget
         });
         ModLoader.addRecipe(new ItemStack(miniumCharm, 1), new Object[]{
         	"PSS", "BIS", "BBP", Character.valueOf('P'), bloodPowder, Character.valueOf('S'), Item.silk, Character.valueOf('B'), Item.expBottle, Character.valueOf('I'), darkobsidian
         });
         ModLoader.addRecipe(new ItemStack(plusiumCharm, 1), new Object[]{
         	"PSS", "BIS", "BBP", Character.valueOf('P'), bloodPowder, Character.valueOf('S'), Item.silk, Character.valueOf('B'), Item.expBottle, Character.valueOf('I'), ebonglow
         });
         ModLoader.addRecipe(new ItemStack(lockWand, 1), new Object[]{
             "  G", " S ", "P  ", Character.valueOf('S'), Item.blazeRod, Character.valueOf('G'), gemOfDespair, Character.valueOf('P'), new ItemStack(Block.mobSpawner, 1, -1)
         });
         ModLoader.addRecipe(new ItemStack(lockWand, 1), new Object[]{
         	"  G", " S ", "P  ", Character.valueOf('S'), Item.blazeRod, Character.valueOf('G'), gemOfDespair, Character.valueOf('P'), new ItemStack(mobSpawnerItem, 1, -1)
        });
         ModLoader.addRecipe(new ItemStack(soulVaseItem, 2), new Object[]{
         	"CSC", "CGC", "CDC", Character.valueOf('C'), Block.blockClay, Character.valueOf('S'), soulStone, Character.valueOf('G'), bloodPowder, Character.valueOf('D'), Item.cauldron
         });
         
         if(!canReforgeArmor) break recipes;
         
         startArmorReforgeRecipes();
         }  
         
         //Ebon Staff Events
     	EbonAPI.addSimpleEbonStaffEvent(Block.cobblestoneMossy.blockID, ebonstone.blockID);
     	EbonAPI.addSimpleEbonStaffEvent(Block.stone.blockID, Block.netherrack.blockID);
     	EbonAPI.addSimpleEbonStaffEvent(Block.cobblestone.blockID, Block.netherrack.blockID);
     	EbonAPI.addSimpleEbonStaffEvent(Block.dirt.blockID, Block.slowSand.blockID);
     	EbonAPI.addSimpleEbonStaffEvent(Block.grass.blockID, Block.slowSand.blockID);
     	EbonAPI.addSimpleEbonStaffEvent(Block.mycelium.blockID, Block.slowSand.blockID);
     	EbonAPI.addSimpleEbonStaffEvent(Block.gravel.blockID, Block.slowSand.blockID);
     	EbonAPI.addSimpleEbonStaffEvent(Block.glass.blockID, Block.glowStone.blockID);
     	EbonAPI.addSimpleEbonStaffEvent(Block.torchWood.blockID, ebontorch.blockID);
     	EbonAPI.addSimpleEbonStaffEvent(Block.brick.blockID, Block.netherBrick.blockID);
     	EbonAPI.addEbonStaffEvent(bloodCrops.blockID, new EbonStaffEventBloodCrops());
     	EbonAPI.addEbonStaffEvent(Block.bookShelf.blockID, new EbonStaffEventAltarBlueprint());
     	EbonAPI.addEbonStaffEvent(Block.stoneBrick.blockID, new EbonStaffEventStoneBricks());
     	
     	//Staff of Souls Events:
     	EbonAPI.addSimpleStaffOfSoulsEvent("Chicken", "Silverfish");
     	EbonAPI.addSimpleStaffOfSoulsEvent("Silverfish", "Chicken");
     	EbonAPI.addSimpleStaffOfSoulsEvent("Cow", "MushroomCow");
     	EbonAPI.addSimpleStaffOfSoulsEvent("MushroomCow", "Cow");
     	EbonAPI.addSimpleStaffOfSoulsEvent("PigZombie", "Pig");
     	EbonAPI.addSimpleStaffOfSoulsEvent("Pig", "PigZombie");
     	EbonAPI.addSimpleStaffOfSoulsEvent("Sheep", "Skeleton");
     	EbonAPI.addSimpleStaffOfSoulsEvent("Skeleton", "Sheep");
     	EbonAPI.addSimpleStaffOfSoulsEvent("Spider", "CaveSpider");
     	EbonAPI.addSimpleStaffOfSoulsEvent("CaveSpider", "Spider");
     	EbonAPI.addSimpleStaffOfSoulsEvent("Blaze", "Enderman");
     	EbonAPI.addSimpleStaffOfSoulsEvent("Enderman", "Blaze");
     	EbonAPI.addSimpleStaffOfSoulsEvent("Wolf", "Ozelot");
     	EbonAPI.addSimpleStaffOfSoulsEvent("Ozelot", "Wolf");
     	EbonAPI.addSimpleStaffOfSoulsEvent("LavaSlime", "Slime");
     	EbonAPI.addSimpleStaffOfSoulsEvent("Slime", "LavaSlime");
     	EbonAPI.addSimpleStaffOfSoulsEvent("Creeper", "Ghast");
     	EbonAPI.addSimpleStaffOfSoulsEvent("Ghast", "Creeper");
     	EbonAPI.addSimpleStaffOfSoulsEvent("Zombie", "Squid");
     	EbonAPI.addSimpleStaffOfSoulsEvent("Squid", "Zombie");
    	EbonAPI.addSimpleStaffOfSoulsEvent("EbonGhost", "Strange Ghost");
    	EbonAPI.addSimpleStaffOfSoulsEvent("Strange Ghost", "EbonGhost");
     	EbonAPI.addSimpleStaffOfSoulsEvent("SnowMan", "Villager");
     	EbonAPI.addSimpleStaffOfSoulsEvent("Villager", "SnowMan");
     	
     	// Scepters
     	EbonAPI.addScepter(new ItemStack(ebonscepterInfinity, 1));
     	EbonAPI.addScepter(new ItemStack(ebonscepterZero, 1));
     	EbonAPI.addScepter(new ItemStack(ebonscepterVoid, 1));
     	
     	//Phantom Chest Loot
     	EbonAPI_PhantomChestLoot loot;
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Block.wood), 1); loot.setMaxQtd(12); loot.setMinQtd(12); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Block.tnt), 1); loot.setMaxQtd(1); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Block.torchWood), 1); loot.setMaxQtd(32); loot.setMinQtd(16); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Item.appleRed), 1); loot.setMaxQtd(12); loot.setMinQtd(4); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Item.compass), 1); loot.setMaxQtd(1); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Item.bed), 1); loot.setMaxQtd(1); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(corpsedust), 1); loot.setMaxQtd(12); loot.setMinQtd(4); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Item.cake), 1); loot.setMaxQtd(1); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Item.bone), 1); loot.setMaxQtd(12); loot.setMinQtd(4); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Item.arrow), 1); loot.setMaxQtd(32); loot.setMinQtd(16); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Item.snowball), 1); loot.setMaxQtd(4); loot.setMinQtd(1); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Item.leather), 1); loot.setMaxQtd(12); loot.setMinQtd(4); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Block.sapling), 1); loot.setMaxQtd(4); loot.setMinQtd(1); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Item.fireballCharge), 1); loot.setMaxQtd(12); loot.setMinQtd(4); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Item.dyePowder, 1, 3), 1); loot.setMaxQtd(12); loot.setMinQtd(4); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Block.glowStone), 1); loot.setMaxQtd(12); loot.setMinQtd(4); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Item.bowlSoup), 1); loot.setMaxQtd(1); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Item.enderPearl), 1); loot.setMaxQtd(4); loot.setMinQtd(1); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Item.fishingRod), 1); loot.setMaxQtd(1); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Item.bucketWater), 1); loot.setMaxQtd(1); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Item.diamond), 2); loot.setMaxQtd(1); loot.setChance(60); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Item.beefCooked), 2); loot.setMaxQtd(12); loot.setMinQtd(4); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Item.potion, 1, 8195), 2); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Item.potion, 1, 8226), 2); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Item.potion, 1, 8265), 2); loot.setMaxQtd(12); loot.setMinQtd(4); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Item.eyeOfEnder), 2); loot.setMaxQtd(4); loot.setMinQtd(1); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Item.slimeBall), 2); loot.setMaxQtd(12); loot.setMinQtd(4); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Block.stoneBrick, 1, 3), 2); loot.setMaxQtd(32); loot.setMinQtd(16); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Item.pickaxeGold, 1, 32), 2); loot.addEnchantments(new Enchantment[]{Enchantment.fortune}, new int[]{6}); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Block.bookShelf), 2); loot.setMaxQtd(12); loot.setMinQtd(4); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Item.bow), 2); loot.setMaxQtd(1); loot.addEnchantments(new Enchantment[]{Enchantment.power}, new int[]{1}); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Item.saddle), 2); loot.setMaxQtd(1); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Item.potion, 1, 16420), 2); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Item.cookie), 2); loot.setMaxQtd(32); loot.setMinQtd(16); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Block.ladder), 2); loot.setMaxQtd(32); loot.setMinQtd(16); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Item.redstone), 2); loot.setMaxQtd(32); loot.setMinQtd(16); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Item.stick), 2); loot.setMaxQtd(1); loot.addEnchantments(new Enchantment[]{Enchantment.knockback}, new int[]{3}); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Item.ghastTear), 2); loot.setMaxQtd(4); loot.setMinQtd(1); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Item.hoeGold), 3); loot.setMaxQtd(1); loot.addEnchantments(new Enchantment[]{Enchantment.knockback}, new int[]{7}); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Item.swordGold), 3); loot.setMaxQtd(1); loot.addEnchantments(new Enchantment[]{Enchantment.silkTouch}, new int[]{1}); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Item.bootsGold, 1, 33), 3); loot.addEnchantments(new Enchantment[]{Enchantment.featherFalling}, new int[]{20}); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Item.pickaxeWood), 3); loot.setMaxQtd(1); loot.addEnchantments(new Enchantment[]{Enchantment.efficiency}, new int[]{25}); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Item.pickaxeStone), 3); loot.setMaxQtd(1); loot.addEnchantments(new Enchantment[]{Enchantment.unbreaking}, new int[]{45}); loot.setChance(35); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Item.swordSteel), 3); loot.setMaxQtd(1); loot.addEnchantments(new Enchantment[]{Enchantment.fireAspect, Enchantment.knockback}, new int[]{10, 2}); loot.setChance(40); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Item.swordDiamond, 1, 1537), 3); loot.addEnchantments(new Enchantment[]{Enchantment.knockback}, new int[]{45}); loot.setChance(10); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Item.bow, 1, 360), 3); loot.addEnchantments(new Enchantment[]{Enchantment.flame, Enchantment.power}, new int[]{10, 10}); loot.setChance(10); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Item.monsterPlacer, 1, 90), 3); loot.setMaxQtd(4); loot.setMinQtd(1); loot.setChance(60); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Item.monsterPlacer, 1, 91), 3); loot.setMaxQtd(4); loot.setMinQtd(1); loot.setChance(60); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Item.monsterPlacer, 1, 92), 3); loot.setMaxQtd(4); loot.setMinQtd(1); loot.setChance(60); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Item.monsterPlacer, 1, 93), 3); loot.setMaxQtd(4); loot.setMinQtd(1); loot.setChance(60); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Item.monsterPlacer, 1, 96), 3); loot.setMaxQtd(4); loot.setMinQtd(1); loot.setChance(15); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Item.monsterPlacer, 1, 98), 3); loot.setMaxQtd(4); loot.setMinQtd(1); loot.setChance(15); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Item.monsterPlacer, 1, 95), 3); loot.setMaxQtd(4); loot.setMinQtd(1); loot.setChance(15); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Item.record11), 3); loot.setMaxQtd(1); loot.setChance(10); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(gemOfDespair), 3); loot.setMaxQtd(1); loot.setChance(5); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Block.bedrock), 3); loot.setMaxQtd(12); loot.setMinQtd(4); loot.setChance(50); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Block.sponge), 3); loot.setMaxQtd(12); loot.setMinQtd(4); EbonAPI.addPhantomLoot(loot);
     	loot = new EbonAPI_PhantomChestLoot(new ItemStack(Block.fire), 3); loot.setMaxQtd(8); loot.setMinQtd(4); EbonAPI.addPhantomLoot(loot);
 
     	//Blacklistd Spawners
     	EbonAPI.blacklistSpawner("VillagerGolem");
     	EbonAPI.blacklistSpawner("EbonGhost");
     	EbonAPI.blacklistSpawner("EbonGhostFRG");
     	EbonAPI.blacklistSpawner("Slime");
     	EbonAPI.blacklistSpawner("SnowMan");
     	EbonAPI.blacklistSpawner("MushroomCow");
     	
     	getblade = (new Achievement(91, "getblade", 8, 1, ebongem, AchievementList.killEnemy)).registerAchievement();
         getblock = (new Achievement(92, "getblock", 8, 3, ebonblock, getblade)).registerAchievement();
         
         ModLoader.addAchievementDesc(getblade, "First Blood", "Make an Ebon Gem.");
         ModLoader.addAchievementDesc(getblock, "Killing Floor", "Make an Ebon Block.");
     }
 
 
     private void startArmorReforgeRecipes(){
     	for(int i=4;i>0;i--){
         ModLoader.addRecipe(new ItemStack(ebonArmor_Hood, 1), new Object[] {
             " S ", "DAD", " D ",  Character.valueOf('S'), soulStone, Character.valueOf('D'), sould, Character.valueOf('A'), new ItemStack(ebonArmor_Hood, 1, i)
         });
         ModLoader.addRecipe(new ItemStack(ebonArmor_RobeTop, 1), new Object[] {
         	" S ", "DAD", " D ",  Character.valueOf('S'), soulStone, Character.valueOf('D'), sould, Character.valueOf('A'), new ItemStack(ebonArmor_RobeTop, 1, i)
         });
     	}
     }
 
     public static boolean setLivingBomb(EntityLiving entityliving){
     	if(currentLivingBomb != null)
     		return false;
     	
     	currentLivingBomb = entityliving;
     		return true;
     }
     
     public static EntityLiving getLivingBomb(){
     	return currentLivingBomb;
     }
     
     public static boolean hasMultipleScepters(EntityPlayer entityplayer){
     	int count = 0;
     	Iterator iterator = EbonAPI.scepterList.iterator();
     	while(iterator.hasNext()){
     		if(entityplayer.inventory.hasItemStack((ItemStack)iterator.next()))
     			count++;
     	}
     	return count >= 2;
     }
 
 	public int addFuel(int i)
     {
         if(i == eboncoal.shiftedIndex)
         	return 6400; // 32 Items
         else if(i == sould.shiftedIndex)
         	return 3200; //16 Items
         else return 0;
     }
 
     public void takenFromCrafting(EntityPlayer entityplayer, ItemStack itemstack, IInventory iinventory)
     {
     	Random rand = new Random();
     	int dam = 0;
     	
         if(itemstack.itemID == ebongem.shiftedIndex)
         {
             entityplayer.addStat(getblade, 1);
         } else
         if(itemstack.itemID == ebonblock.blockID)
         {
             entityplayer.addStat(getblock, 1);
         }else 
         if (itemstack.itemID == ebonArmor_Hood.shiftedIndex || itemstack.itemID == ebonArmor_RobeTop.shiftedIndex){
         	
         	do dam = rand.nextInt(5); while(dam == 0);
         	itemstack.setItemDamage(dam);
         }
     }
     
     public boolean onTickInGame(float f, Minecraft minecraft)
     {
     	Random rand = new Random();
     	float foodExaustion;
     	int playerF = MathHelper.floor_double((double)((mc.thePlayer.rotationYaw * 4F) / 360F) + 0.5D) & 3;
     	ItemStack helm = mc.thePlayer.inventory.armorInventory[3];
     	ItemStack chest = mc.thePlayer.inventory.armorInventory[2];
     	boolean fireImmuneArmor = (helm != null && helm.itemID == ebonArmor_Hood.shiftedIndex && helm.getItemDamage() == 1);   	
     	if(hasJumpTicked && mc.thePlayer.onGround){
     		mc.thePlayer.fallDistance = 0.0F;
     		hasJumpTicked = false;
     	}
 	
     	//Marked Entity Clearance handler
     	for(Entity e : markedEntities){
     		if(e.isDead)
     			markedEntitiesForRemoval.add(e);
     	}
     	for(Entity e : markedEntitiesForRemoval){
     		if(markedEntities.contains(e))
     			markedEntities.remove(e);
     	}
     	markedEntitiesForRemoval.clear();
     	
     	//Exhaustion Clearance handler
     	exNow = minecraft.thePlayer.activePotionsMap.containsKey(new PotionEffect(mod_Ebon.magicExhaustion.id, 1, 0).getPotionID());
     	if(exLast && !exNow)
     	minecraft.thePlayer.worldObj.playSoundAtEntity(minecraft.thePlayer, "vazkii.ebonmod.clearExhaustion", 1.0F, 1.0F);
     	exLast = minecraft.thePlayer.activePotionsMap.containsKey(new PotionEffect(mod_Ebon.magicExhaustion.id, 1, 0).getPotionID());
     	
     	//Armor Souls Tick
     	if(!mc.thePlayer.inventory.hasItemStack(new ItemStack(soulStone, 1, 1)) && !EbonAPI.doesPlayerHaveMagicExhaustion()){
     	
     	if(chest != null && chest.itemID == ebonArmor_RobeTop.shiftedIndex){
     	/**Nature Soul**/
     	if(chest.getItemDamage() == 1 && mc.theWorld.getBlockId((int)mc.thePlayer.posX, (int)mc.thePlayer.posY-2, (int)mc.thePlayer.posZ) == Block.dirt.blockID)
     		mc.theWorld.setBlockWithNotify((int)mc.thePlayer.posX, (int)mc.thePlayer.posY-2, (int)mc.thePlayer.posZ, Block.grass.blockID);
     	/*****/
     	
     	/**Respiration Soul**/
     	if(chest.getItemDamage() == 2 && mc.thePlayer.getAir() == -19 && !hasAirTicked){
     		mc.thePlayer.setAir(301);
     		hasAirTicked = true;
     	}
     	if(hasAirTicked && !mc.thePlayer.isInsideOfMaterial(Material.water))
     		hasAirTicked = false;
     	/*****/
     	
     	/**Frost Soul**/
     	if(chest.getItemDamage() == 3){
     	if(playerF == 0){  //posZ is a little derpy when it comes to handling block placement so I just left it alone :)
     	if(mc.theWorld.getBlockId((int)mc.thePlayer.posX, (int)mc.thePlayer.posY-2, (int)mc.thePlayer.posZ) == Block.waterStill.blockID)
     		mc.theWorld.setBlockWithNotify((int)mc.thePlayer.posX, (int)mc.thePlayer.posY-2, (int)mc.thePlayer.posZ, Block.ice.blockID);
     	if(mc.theWorld.getBlockId((int)mc.thePlayer.posX, (int)mc.thePlayer.posY-2, (int)mc.thePlayer.posZ) == Block.lavaStill.blockID)
     		mc.theWorld.setBlockWithNotify((int)mc.thePlayer.posX, (int)mc.thePlayer.posY-2, (int)mc.thePlayer.posZ, Block.obsidian.blockID);
     	}
     	else if(playerF == 1){
     	if(mc.theWorld.getBlockId((int)mc.thePlayer.posX-1, (int)mc.thePlayer.posY-2, (int)mc.thePlayer.posZ) == Block.waterStill.blockID)
     		mc.theWorld.setBlockWithNotify((int)mc.thePlayer.posX-1, (int)mc.thePlayer.posY-2, (int)mc.thePlayer.posZ, Block.ice.blockID);
     	if(mc.theWorld.getBlockId((int)mc.thePlayer.posX-1, (int)mc.thePlayer.posY-2, (int)mc.thePlayer.posZ) == Block.lavaStill.blockID)
     		mc.theWorld.setBlockWithNotify((int)mc.thePlayer.posX-1, (int)mc.thePlayer.posY-2, (int)mc.thePlayer.posZ, Block.obsidian.blockID);
     	}
     	else if(playerF == 2){
     	if(mc.theWorld.getBlockId((int)mc.thePlayer.posX, (int)mc.thePlayer.posY-2, (int)mc.thePlayer.posZ) == Block.waterStill.blockID)
     		mc.theWorld.setBlockWithNotify((int)mc.thePlayer.posX, (int)mc.thePlayer.posY-2, (int)mc.thePlayer.posZ, Block.ice.blockID);
     	if(mc.theWorld.getBlockId((int)mc.thePlayer.posX, (int)mc.thePlayer.posY-2, (int)mc.thePlayer.posZ) == Block.lavaStill.blockID)
     		mc.theWorld.setBlockWithNotify((int)mc.thePlayer.posX, (int)mc.thePlayer.posY-2, (int)mc.thePlayer.posZ, Block.obsidian.blockID);
     	}
     	else if(playerF == 3){
     	if(mc.theWorld.getBlockId((int)mc.thePlayer.posX+1, (int)mc.thePlayer.posY-2, (int)mc.thePlayer.posZ) == Block.waterStill.blockID)
     		mc.theWorld.setBlockWithNotify((int)mc.thePlayer.posX+1, (int)mc.thePlayer.posY-2, (int)mc.thePlayer.posZ, Block.ice.blockID);
     	if(mc.theWorld.getBlockId((int)mc.thePlayer.posX+1, (int)mc.thePlayer.posY-2, (int)mc.thePlayer.posZ) == Block.lavaStill.blockID)
     		mc.theWorld.setBlockWithNotify((int)mc.thePlayer.posX+1, (int)mc.thePlayer.posY-2, (int)mc.thePlayer.posZ, Block.obsidian.blockID);
     	}
     	}
     	/*****/
     	
     	/**Velocity Soul**/
     	if(chest.getItemDamage() == 4)
     		mc.thePlayer.landMovementFactor += (double)mc.thePlayer.speedOnGround * 0.29999999999999999D;
     	/*****/
     	}
     	
     	if(helm != null && helm.itemID == ebonArmor_Hood.shiftedIndex){
     		
     	/**Obsidian Skin Soul**/
     	if(helm.getItemDamage() == 1 && !mc.thePlayer.isImmuneToFire()){
     		mc.thePlayer.isImmuneToFire = true;
     	}
     	/*****/
     	
      	/**Revitalization Soul**/
     	if(helm.getItemDamage() == 2 && ticksFT == 90 && mc.thePlayer.getFoodStats().getFoodLevel() < 21F){
     		mc.thePlayer.foodStats.addStats(1, 0F);
     		ticksFT = 0;
     	}
     	if(ticksFT != 90)
     		ticksFT++;
     	/*****/
     	
      	/**Regeneration Soul**/
     	if(helm.getItemDamage() == 3 && ticksRT == 40){
     		mc.thePlayer.heal(1);
     		ticksRT = 0;
     	}
     	if(ticksRT != 40)
     		ticksRT++;
     	/*****/
     	
      	/**Leaping Soul**/
     	if(helm.getItemDamage() == 4 && mc.thePlayer.isJumping && mc.thePlayer.isSneaking() && !hasJumpTicked){
     		mc.thePlayer.motionY = 0.83999997377395630D;
     		mc.thePlayer.fallDistance = -4F;
     		for(int i=0; i<20; i++)
     		mc.theWorld.spawnParticle("explode", mc.thePlayer.posX + (rand.nextDouble() - 0.5D) * (double)mc.thePlayer.width, mc.thePlayer.posY, mc.thePlayer.posZ + (rand.nextDouble() - 0.5D) * (double)mc.thePlayer.width, 0.0D, 0.0D, 0.0D);
     		hasJumpTicked = true;
     	}
     	/*****/
     		
     	}
     	}
         
     	//Multiple Scepters or Charms
     	if(hasMultipleScepters(minecraft.thePlayer)){
     		if(ticksMS == 20 && mc.theWorld.worldInfo.getGameType() != 1){
     			mc.theWorld.playSoundAtEntity(mc.thePlayer, "vazkii.ebonmod.fail", 1.0F, 1.0F);
     			mc.thePlayer.attackEntityFrom(DamageSource.magic, 2);
     			ticksMS = 0;
     		}
     		ticksMS++;
     	}
     	if(minecraft.thePlayer.inventory.hasItemStack(new ItemStack(plusiumCharm, 1)) && minecraft.thePlayer.inventory.hasItemStack(new ItemStack(miniumCharm, 1))){
     		if(ticksMC == 20 && mc.theWorld.worldInfo.getGameType() != 1){
     			mc.theWorld.playSoundAtEntity(mc.thePlayer, "vazkii.ebonmod.fail", 1.0F, 1.0F);
     			mc.thePlayer.attackEntityFrom(DamageSource.magic, 2);
     			ticksMC = 0;
     		}
     		ticksMC++;
     	}
     	
     	//Living Bomb
     	if(currentLivingBomb != null){
     		if(currentLivingBomb.isEntityAlive()){
     	mc.theWorld.spawnParticle("reddust", currentLivingBomb.posX + (rand.nextDouble() - 0.5D) * (double)currentLivingBomb.width, (currentLivingBomb.posY + rand.nextDouble() * (double)currentLivingBomb.height) - 0.25D, currentLivingBomb.posZ + (rand.nextDouble() - 0.5D) * (double)currentLivingBomb.width, 0.0D, 0.0D, 0.0D);
     	if(ticksLB >= 80)
     	mc.theWorld.spawnParticle("flame", currentLivingBomb.posX + (rand.nextDouble() - 0.5D) * (double)currentLivingBomb.width, (currentLivingBomb.posY + rand.nextDouble() * (double)currentLivingBomb.height) - 0.25D, currentLivingBomb.posZ + (rand.nextDouble() - 0.5D) * (double)currentLivingBomb.width, 0.0D, 0.0D, 0.0D);
     	
     	ticksLB++;
     	if(ticksLB == 20 || ticksLB == 40 || ticksLB == 60 || ticksLB == 80)
     		mc.theWorld.playSoundEffect(currentLivingBomb.posX, currentLivingBomb.posY, currentLivingBomb.posZ, "random.click", 1.0F, 1.0F);
     	if(ticksLB == 100){
             mc.theWorld.createExplosion(currentLivingBomb, currentLivingBomb.posX, currentLivingBomb.posY, currentLivingBomb.posZ, 4F);
             ticksLB = 0;
             currentLivingBomb = null;
     		}
     	}else{
     		currentLivingBomb = null;
     		ticksLB = 0; 
     	}
     	}
     	
     	if(mc.thePlayer.isImmuneToFire && !fireImmuneArmor){
     		mc.thePlayer.isImmuneToFire = false;
     	}
     	
     	return true;
     }
     
     public boolean onTickInGUI(float f, Minecraft minecraft, GuiScreen guiscreen)
     {
         if((guiscreen instanceof GuiContainerCreative) && !(lastGuiOpen instanceof GuiContainerCreative) && !minecraft.theWorld.isRemote)
         {
             Container container = ((GuiContainer)guiscreen).inventorySlots;
             List itemList = ((ContainerCreative)container).itemList;
             	itemList.add(new ItemStack(mod_Ebon.darkobsidian, 1));
             	itemList.add(new ItemStack(mod_Ebon.quicksand, 1));
             	itemList.add(new ItemStack(mod_Ebon.ebonstone, 1));
             	itemList.add(new ItemStack(mod_Ebon.ebonblock, 1));
             	itemList.add(new ItemStack(mod_Ebon.ebontorch, 1));
             	itemList.add(new ItemStack(mod_Ebon.ebonglow, 1));
             	itemList.add(new ItemStack(mod_Ebon.soulgemblock, 1));
             	itemList.add(new ItemStack(mod_Ebon.phantomChest, 1));
             	for(int i1=4;i1>0;i1--){
             		itemList.add(new ItemStack(mod_Ebon.ebonArmor_Hood, 1, i1));
             		itemList.add(new ItemStack(mod_Ebon.ebonArmor_RobeTop, 1, i1));
             	}
             	if(!ModLoader.isModLoaded("mod_NotEnoughItems") && creativeSpawnersEnabled){
             		for(int i=0; i<spawnersList.length; i++)
             	itemList.add(new ItemStack(mod_Ebon.mobSpawnerItem, 1, spawnersList[i]));
             	} 
         }
         lastGuiOpen = guiscreen;
         return true;
     }
     
     public void addRenderer(Map map)
     {
         map.put(net.minecraft.src.EntityEbonGhost.class, new RenderBiped(new ModelBiped(), 0.5F));
         map.put(net.minecraft.src.EntityEbonGhostFrg.class, new RenderBiped(new ModelBiped(), 0.5F));
     }
     
     public void renderInvBlock(RenderBlocks var1, Block var2, int var3, int var4){
         if (var4 == phantomChest.getRenderType())
         {
          TileEntityRenderer.instance.renderTileEntityAt(new TileEntityPhantomChest(), 0.0D, 0.0D, 0.0D, 0.0F);
         }
 }
 
     public String getVersion()
     {
        return "by Vazkii. Version [3.2.2] for 1.2.5. API Version " + EbonAPI.getAPIVersion() + ".";
     }
 
     private static Minecraft mc = ModLoader.getMinecraftInstance();
     
     public int[] spawnersList = new int[]{
     		50, 51, 52, 54, 53, 55, 56, 57, 58, 59, 60, 61, 62, 90, 91, 92, 93, 94, 95, 96, 98, 120, 97, 99
     };
     
 	protected static List<Entity> markedEntities = new LinkedList<Entity>();
 	private static List<Entity> markedEntitiesForRemoval = new LinkedList<Entity>();
     
     private static boolean exLast;
     private static boolean exNow;
     
     private static EntityLiving currentLivingBomb = null;
 	int ticksLB = 0; //Living Bomb
     int ticksMS = 0; //Multiple Scepters
 	int ticksFT = 0; //Food Tick
 	int ticksRT = 0; //Regeneration Tick
 	int ticksMC = 0; //Multiple Charms
 	
 	boolean hasAirTicked = false;
 	boolean hasJumpTicked = false;
 	
 	private static GuiScreen lastGuiOpen;
     
     public static Block darkobsidian;
     public static Block quicksand;
     public static Block ebonstone;
     public static Block ebonblock;
     public static Block ebontorch;
     public static Block ebonglow;
     public static Block soulgemblock;
     public static Block bloodCrops;
     public static Block phantomChest;
     public static Block soulVase;
     
     public static int phantomChestRenderID;
 
     public static Item corpsedust;
     public static Item deaddust;
     public static Item ebongem;
     public static Item ebonsword;
     public static Item dustpile;
     public static Item glowdustEbon;
     public static Item eboncoal;
     public static Item ebonapple;
     public static Item ebonpick;
     public static Item ebonspade;
     public static Item ebonax;
     public static Item ebonstaffc;
     public static Item ebonstaffu;
     public static Item soul;
     public static Item soulsub;
     public static Item staffshard;
     public static Item darkfert;
     public static Item ebonbow;
     public static Item soulorb;
     public static Item soulorbc;
     public static Item soulstaffc;
     public static Item soulstaffu;
     public static Item sould;
     public static Item soulgem;
     public static Item ebonscepter;
     public static Item tsEgg;
     public static Item fsEgg;
     public static Item wandOfRetrieval;
     public static Item mortarPestle;
     public static Item bloodPowder;
     public static Item bloodSeeds;
     public static Item bloodLeaf;
     public static Item necroTome;
     public static Item ebonscepterVoid;
     public static Item ebonscepterZero;
     public static Item ebonscepterInfinity;
     public static Item ebonArmor_Hood;
     public static Item ebonArmor_RobeTop;
     public static Item ebonArmor_RobeBottom;
     public static Item ebonArmor_Shoes;
     public static Item soulStone;
     public static Item ebonCloth;
     public static Item mobSpawnerItem;
     public static Item phantomKey;
     public static Item altarBlueprint;
     public static Item plusiumCharm;
     public static Item miniumCharm;
     public static Item soulVaseItem;
     public static Item gemOfDespair;
     public static Item lockWand;
     
     public static Potion magicExhaustion;
     
     public static Achievement getblade;
     public static Achievement getblock;
     
     @MLProp public static int quicksandID = 189;
     @MLProp public static int ebonstoneID = 188;
     @MLProp public static int ebonblockID = 187;
     @MLProp public static int ebontorchID = 192;
     @MLProp public static int ebonglowID = 190;
     @MLProp public static int darkobsidianID = 191;
     @MLProp public static int phantomChestID = 195;
     @MLProp public static int soulVaseID = 196;
     
     @MLProp public static int corpsedustID = 3128;
     @MLProp public static int deaddustID = 3129;
     @MLProp public static int ebongemID = 3130;
     @MLProp public static int ebonswordID = 3131;
     @MLProp public static int dustpileID = 3132;
     @MLProp public static int glowdustEbonID = 3234;
     @MLProp public static int eboncoalID = 3235;
     @MLProp public static int ebonappleID = 3136;
     @MLProp public static int ebonpickID = 3137;
     @MLProp public static int ebonspadeID = 3138;
     @MLProp public static int ebonaxID = 3139;
     @MLProp public static int ebonstaffcID = 3144;
     @MLProp public static int ebonstaffuID = 3143;
     @MLProp public static int soulID = 3140;
     @MLProp public static int soulsubID = 3141;
     @MLProp public static int staffshardID = 3142;
     @MLProp public static int darkfertID = 3145; 
     @MLProp public static int ebonbowID = 3146;
     @MLProp public static int soulorbID = 3147;
     @MLProp public static int soulorbcID = 3148;
     @MLProp public static int soulstaffcID = 3150;
     @MLProp public static int soulstaffuID = 3149;
     @MLProp public static int souldID = 3151;
     @MLProp public static int soulgemID = 3152;
     @MLProp public static int soulgemblockID = 193;
     @MLProp public static int ebonscepterID = 3153;
     @MLProp public static int tsEggID = 8912;
     @MLProp public static int fsEggID = 8913;
     @MLProp public static int wandOfRetrievalID = 3154;
     @MLProp public static int mortarPestleID = 3155;
     @MLProp public static int bloodPowderID = 3156;
     @MLProp public static int bloodSeedsID = 3157;
     @MLProp public static int bloodLeafID = 3158;
     @MLProp public static int bloodCropsID = 194;
     @MLProp public static int necroTomeID = 3159;
     @MLProp public static int ebonscepterVoidID = 3160;
     @MLProp public static int ebonscepterZeroID = 3161;
     @MLProp public static int ebonscepterInfinityID = 3162;
     @MLProp public static int ebonArmor_HoodID = 3163;
     @MLProp public static int ebonArmor_RobeTopID = 3164;
     @MLProp public static int ebonArmor_RobeBottomID = 3165;
     @MLProp public static int ebonArmor_ShoesID = 3166;
     @MLProp public static int soulStoneID = 3167;
     @MLProp public static int ebonClothID = 3168;
     @MLProp public static int mobSpawnerItemID = 3169;
     @MLProp public static int phantomKeyID = 3170;
     @MLProp public static int altarBlueprintID = 3171;
     @MLProp public static int plusiumCharmID = 3172;
     @MLProp public static int miniumCharmID = 3173;
     @MLProp public static int soulVaseItemID = 3174;
     @MLProp public static int gemOfDespairID = 3175;
     @MLProp public static int lockWandID = 3176;
     
     @MLProp public static int magicExhaustionPotionID = 27;
     
     @MLProp public static boolean phantomChestsChunkLoading = true;
     @MLProp public static boolean canReforgeArmor = true;
     @MLProp public static boolean creativeSpawnersEnabled = true;
     @MLProp public static int phantomChestRarity = 2;
     
     private class ForgeHooks implements IBonemealHandler, IDestroyToolHandler, ISoundHandler{
     	
     	private ForgeHooks(){
         	MinecraftForgeClient.preloadTexture("/vazkii/ebonmod/sprites.png");
         	MinecraftForge.registerBonemealHandler(this);
         	MinecraftForge.registerDestroyToolHandler(this);
         	MinecraftForgeClient.registerSoundHandler(this);
 			}
     	
     	public boolean onUseBonemeal(World world, int bid, int i, int j, int k) {
 			if(world.getBlockId(i, j, k) == bloodCrops.blockID){
 				((BlockBloodLeaf) bloodCrops).fertilize(world, i, j, k);
 			return true;
 			}
 			return false;
     	}
 
 		public void onDestroyCurrentItem(EntityPlayer player, ItemStack orig) {
 			if(orig.itemID == necroTome.shiftedIndex)
 				player.addChatMessage("How in the world did you manage to break that? You should tell Vazkii about it...");
 		}
 
 		public void onSetupAudio(SoundManager soundManager) {
 		}
 
 		public void onLoadSoundSettings(SoundManager soundManager) {
 			File file = ModLoader.getMinecraftInstance().getMinecraftDir();
 			soundManager.getSoundsPool().addSound("vazkii/ebonmod/absorb.ogg", mod_Ebon.class.getResource("/vazkii/ebonmod/sfx/absorb.ogg"));
 			soundManager.getSoundsPool().addSound("vazkii/ebonmod/clearExhaustion.ogg", mod_Ebon.class.getResource("/vazkii/ebonmod/sfx/clearExhaustion.ogg"));
 			soundManager.getSoundsPool().addSound("vazkii/ebonmod/fail.ogg", mod_Ebon.class.getResource("/vazkii/ebonmod/sfx/fail.ogg"));
 			soundManager.getSoundsPool().addSound("vazkii/ebonmod/pcClose.ogg", mod_Ebon.class.getResource("/vazkii/ebonmod/sfx/pcClose.ogg"));
 			soundManager.getSoundsPool().addSound("vazkii/ebonmod/pcOpen.ogg", mod_Ebon.class.getResource("/vazkii/ebonmod/sfx/pcOpen.ogg"));
 			soundManager.getSoundsPool().addSound("vazkii/ebonmod/retrieval.ogg", mod_Ebon.class.getResource("/vazkii/ebonmod/sfx/retrieval.ogg"));
 			soundManager.getSoundsPool().addSound("vazkii/ebonmod/smite.ogg", mod_Ebon.class.getResource("/vazkii/ebonmod/sfx/smite.ogg"));
 			soundManager.getSoundsPool().addSound("vazkii/ebonmod/spell.ogg", mod_Ebon.class.getResource("/vazkii/ebonmod/sfx/spell.ogg"));
 			soundManager.getSoundsPool().addSound("vazkii/ebonmod/spNormal.ogg", mod_Ebon.class.getResource("/vazkii/ebonmod/sfx/spNormal.ogg"));
 			soundManager.getSoundsPool().addSound("vazkii/ebonmod/spRare.ogg", mod_Ebon.class.getResource("/vazkii/ebonmod/sfx/spRare.ogg"));
 			soundManager.getSoundsPool().addSound("vazkii/ebonmod/tome.ogg", mod_Ebon.class.getResource("/vazkii/ebonmod/sfx/tome.ogg"));
 			soundManager.getSoundsPool().addSound("vazkii/ebonmod/tool.ogg", mod_Ebon.class.getResource("/vazkii/ebonmod/sfx/tool.ogg"));
 			soundManager.getSoundsPool().addSound("vazkii/ebonmod/transmute.ogg", mod_Ebon.class.getResource("/vazkii/ebonmod/sfx/transmute.ogg"));
 			soundManager.getSoundsPool().addSound("vazkii/ebonmod/tsDeath.ogg", mod_Ebon.class.getResource("/vazkii/ebonmod/sfx/tsDeath.ogg"));
 			soundManager.getSoundsPool().addSound("vazkii/ebonmod/vase.ogg", mod_Ebon.class.getResource("/vazkii/ebonmod/sfx/vase.ogg"));
 			soundManager.getSoundsPool().addSound("vazkii/ebonmod/minium.ogg", mod_Ebon.class.getResource("/vazkii/ebonmod/sfx/minium.ogg"));
 		}
 
 		public SoundPoolEntry onPlayBackgroundMusic(SoundManager soundManager,
 				SoundPoolEntry entry) {
 			return entry;
 		}
 
 		public SoundPoolEntry onPlayStreaming(SoundManager soundManager,
 				SoundPoolEntry entry, String soundName, float x, float y,
 				float z) {
 			return entry;
 		}
 
 		public SoundPoolEntry onPlaySound(SoundManager soundManager,
 				SoundPoolEntry entry, String soundName, float x, float y,
 				float z, float volume, float pitch) {
 			return entry;
 		}
 
 		public SoundPoolEntry onPlaySoundEffect(SoundManager soundManager,
 				SoundPoolEntry entry, String soundName, float volume,
 				float pitch) {
 			return entry;
 		}
     }
 

 	public String getModName() {
 		return "The Ebon Mod";
 	}
 
 	public String getChangelogURL() {
 		return "https://dl.dropbox.com/u/34938401/Mods/On%20Topic/Mods/Ebon/Changelog.txt";
 	}
 
 	public String getUpdateURL() {
 		return "https://dl.dropbox.com/u/34938401/Mods/On%20Topic/Mods/Ebon/Version.txt";
 	}
 
 	public String getModURL() {
 		return "http://www.minecraftforum.net/topic/528166-123-mlforge-vazkiis-mods-ebonapi-last-updated-12512/";
 	}
 
 	public ModType getModType() {
 		return ModType.UNDEFINED;
 	}
 }
