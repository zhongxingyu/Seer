 package net.lomeli.magiks.items;
 
 import net.lomeli.magiks.api.libs.MagiksArrays;
 import net.lomeli.magiks.items.magik.ItemAmulets;
 import net.lomeli.magiks.items.magik.ItemFlyingRing;
 import net.lomeli.magiks.items.magik.ItemPirasVarinha;
 import net.lomeli.magiks.items.magik.ItemWands;
 import net.lomeli.magiks.items.tools.ItemEmpoweredPick;
 import net.lomeli.magiks.items.tools.ItemLevelingSword;
 import net.lomeli.magiks.items.tools.ItemMiningWands;
 import net.lomeli.magiks.lib.Ints;
 
 import net.minecraft.item.EnumToolMaterial;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.oredict.OreDictionary;
 
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 public class ModItemsMagiks
 {
     public static Item flyingRing;
     public static Item neoniteGem;
     public static Item neonitePick;
     public static Item enchantedDiamond;
     public static Item levelingSword;
     public static Item ironBand;
     public static Item ironStick;
     public static Item emeraldAmulet;
     public static Item darkMatter;
     public static Item mistPanel;
     public static Item deprivedDust;
     public static Item burningUpgrade;
     public static Item ironPlate;
     
     // ores
     public static Item ingotStamatic;
     public static Item ingotIgnious;
     public static Item ingotCopper;
     public static Item ingotTin;
     public static Item ingotSilver;
     
     // wands
     public static Item basicWand;
     public static Item chemistWand;
     public static Item alchemistWand;
     public static Item pirasVarinha;
     public static Item diggersWand;
 
     public static Item wand;
 
     public static void registerItems()
     {
         flyingRing = new ItemFlyingRing(Ints.flyingRingID, "flyingring")
                 .setUnlocalizedName("flyingring");
         neoniteGem = new ItemGeneric(Ints.enchantedGemID, "neoniteGem", true)
                 .setUnlocalizedName("enchantedGem");
         enchantedDiamond = new ItemGeneric(Ints.enchantedDiamondID,
                 "empowereddiamond", true)
                 .setUnlocalizedName("enchantedDiamond");
         ironBand = new ItemGeneric(Ints.ironBandID, "ironband", false)
                 .setUnlocalizedName("ironBand");
         ironStick = new ItemGeneric(Ints.ironStickID, "ironstick", false)
                 .setUnlocalizedName("ironStick");
        emeraldAmulet = new ItemAmulets(Ints.emeraldAmuletID, "amuletGold", true, 50000)
                 .setUnlocalizedName("amulet");
         ingotStamatic = new ItemGeneric(Ints.ingotStamaticID, "ingotstamatic",
                 false).setUnlocalizedName("ingotstamatic");
         ingotIgnious = new ItemGeneric(Ints.ingotIgniousID, "ingotignious",
                 false).setUnlocalizedName("ingotignious");
         mistPanel = new ItemGeneric(Ints.mistPanelID, "lighthungryplates",
                 false).setUnlocalizedName("mistPanel");
         darkMatter = new ItemGeneric(Ints.darkMatterID, "darkmatter", true)
                 .setUnlocalizedName("darkmatter");
         deprivedDust = new ItemGeneric(Ints.deprivedDustID,
                 "deprivedglowstone", false).setUnlocalizedName("deprivedDust");
         burningUpgrade = new ItemGeneric(Ints.burningUpgradeID,
                 "burningupgrade", false).setUnlocalizedName("burningupgrade")
                 .setMaxStackSize(2);
         ironPlate = new ItemGeneric(Ints.ironPlateID, "ironplate", false)
                 .setUnlocalizedName("ironplate");
         ingotCopper = new ItemGeneric(Ints.ingotCopperID, "ingotCopper", false)
         		.setUnlocalizedName("ingotCopper");
         ingotTin = new ItemGeneric(Ints.ingotTinID, "ingotTin", false)
 		.setUnlocalizedName("ingotTin");
         ingotSilver = new ItemGeneric(Ints.ingotSilverID, "ingotSilver", false)
 		.setUnlocalizedName("ingotSilver");
         
         wand = new ItemGeneric(Ints.wandID, "magicianwand", false)
             .setCreativeTab(null).setUnlocalizedName("wand");
 
         basicWand = new ItemWands(Ints.basicWandID, "basicwand", false, 50)
                 .setUnlocalizedName("basicwand");
         chemistWand = new ItemWands(Ints.chemistWandID, "chemistwand", false,
                 100).setUnlocalizedName("chemistwand");
         alchemistWand = new ItemWands(Ints.alchemistWandID, "alchemistwand",
                 true, 300).setUnlocalizedName("alchemistwand");
 
         neonitePick = new ItemEmpoweredPick(Ints.empoweredPickID,
                 EnumToolMaterial.IRON, "pickaxeEmpowered")
                 .setUnlocalizedName("empoweredPick");
        levelingSword = new ItemLevelingSword(Ints.spencerSwordID, EnumToolMaterial.IRON,
                 "levelingsword").setUnlocalizedName("lvlSword");
         pirasVarinha = new ItemPirasVarinha(Ints.pirasVarinhaID,
                 "pirasvarinha", false).setUnlocalizedName("pirasvarinha");
         diggersWand = new ItemMiningWands(Ints.diggersWandID, "diggerswand",
                 false, 300, 0).setUnlocalizedName("diggerswand");
 
         LanguageRegistry.addName(flyingRing, "ShadowWing's Gale");
         LanguageRegistry.addName(neoniteGem, "Neonite Gem");
         LanguageRegistry.addName(neonitePick, "Neonite Pickaxe");
         LanguageRegistry.addName(enchantedDiamond, "Enchanted Diamond");
         LanguageRegistry.addName(levelingSword, "Sir Spencer's Blade");
         LanguageRegistry.addName(ironBand, "Iron Band");
         LanguageRegistry.addName(ironStick, "Iron Stick");
         LanguageRegistry.addName(emeraldAmulet, "Emerald Amulet");
         LanguageRegistry.addName(ingotStamatic, "Stamatic Ingot");
         LanguageRegistry.addName(ingotIgnious, "Ignious Ingot");
         LanguageRegistry.addName(pirasVarinha, "Piras Varinha");
         LanguageRegistry.addName(diggersWand, "Digger's Wand");
         LanguageRegistry.addName(mistPanel, "Light Hungry Plates");
         LanguageRegistry.addName(darkMatter, "Dark Matter");
         LanguageRegistry.addName(deprivedDust, "Deprived Glowstone");
         LanguageRegistry.addName(basicWand, "Basic Wand");
         LanguageRegistry.addName(chemistWand, "Chemist's Wand");
         LanguageRegistry.addName(alchemistWand, "Alchemist's Wand");
         LanguageRegistry.addName(ironPlate, "Iron Plate");
         LanguageRegistry.addName(burningUpgrade, "Burning Upgrade");
         LanguageRegistry.addName(wand, "Magician's Wand");
         LanguageRegistry.addName(ingotCopper, "Copper Ingot");
         LanguageRegistry.addName(ingotTin, "Tin Ingot");
         LanguageRegistry.addName(ingotSilver, "Silver Ingot");
         
         OreDictionary.registerOre("ingotCopper", new ItemStack(ingotCopper));
         OreDictionary.registerOre("ingotTin", new ItemStack(ingotTin));
         OreDictionary.registerOre("ingotSilver", new ItemStack(ingotSilver));
     }
 
     public static void addChargeableItems()
     {
         MagiksArrays.rechargeableItems.add(new ItemStack(flyingRing));
         MagiksArrays.rechargeableItems.add(new ItemStack(pirasVarinha));
         MagiksArrays.rechargeableItems.add(new ItemStack(emeraldAmulet));
         MagiksArrays.rechargeableItems.add(new ItemStack(diggersWand));
         MagiksArrays.rechargeableItems.add(new ItemStack(basicWand));
         MagiksArrays.rechargeableItems.add(new ItemStack(chemistWand));
         MagiksArrays.rechargeableItems.add(new ItemStack(alchemistWand));
 
         MagiksArrays.damageOnCraft.add(new ItemStack(basicWand));
         MagiksArrays.damageOnCraft.add(new ItemStack(chemistWand));
         MagiksArrays.damageOnCraft.add(new ItemStack(alchemistWand));
 
         MagiksArrays.wands.add(new ItemStack(basicWand));
         MagiksArrays.wands.add(new ItemStack(chemistWand));
         MagiksArrays.wands.add(new ItemStack(alchemistWand));
     }
 }
