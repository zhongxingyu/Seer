 /*
  * To change this template, choose Tools | Templates and open the template in
  * the editor.
  */
 package arcanefantasy.common.core.managers;
 
 import arcanefantasy.common.item.EnumAFToolMaterial;
 import arcanefantasy.common.item.ItemAFAxe;
 import arcanefantasy.common.item.ItemAFChalk;
 import arcanefantasy.common.item.ItemAFGem;
 import arcanefantasy.common.item.ItemAFHoe;
 import arcanefantasy.common.item.ItemAFMetal;
 import arcanefantasy.common.item.ItemAFPickaxe;
 import arcanefantasy.common.item.ItemAFSalt;
 import arcanefantasy.common.item.ItemAFSlateFlake;
 import arcanefantasy.common.item.ItemAFSpade;
 import arcanefantasy.common.item.ItemAFSword;
 import arcanefantasy.common.lib.ItemIds;
 
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 import net.minecraft.src.CreativeTabs;
 import net.minecraft.src.Item;
 import net.minecraft.src.ItemStack;
 
 import java.util.Locale;
 
 
 /**
  * DOCUMENT ME!
  *
  * @author  HMPerson1
  */
 @SuppressWarnings("PublicField" /* NOI18N */)
 public class ItemManager {
 
     /* Item name constants */
     public static final String NAME_GEM     = "gem";
     public static final String NAME_METAL   = "metal";
     public static final String NAME_SLATEF  = "slatef";
     public static final String NAME_CHALK   = "chalk";
     public static final String NAME_SALT    = "salt";
     public static final String NAME_AXE     = "axes";
     public static final String NAME_HOE     = "hoes";
     public static final String NAME_PICKAXE = "pickaxes";
     public static final String NAME_SPADE   = "spades";
     public static final String NAME_SWORD   = "swords";
 
     /* Item instances */
     public static Item                  gem;
     public static Item                  metal;
     public static Item                  slateFlake;
     public static Item                  chalk;
     public static Item                  salt;
     public static final ItemAFAxe[]     axes     =
         new ItemAFAxe[EnumAFToolMaterial.values().length];
     public static final ItemAFHoe[]     hoes     =
         new ItemAFHoe[EnumAFToolMaterial.values().length];
     public static final ItemAFPickaxe[] pickaxes =
         new ItemAFPickaxe[EnumAFToolMaterial.values().length];
     public static final ItemAFSpade[]   spades   =
         new ItemAFSpade[EnumAFToolMaterial.values().length];
     public static final ItemAFSword[]   swords   =
         new ItemAFSword[EnumAFToolMaterial.values().length];
 
     private ItemManager() { }
 
     /**
      * Initializes items.
      */
     public static void init() {
 
 //J-
         // add the items
         gem        = new ItemAFGem(ItemIds.ID_ITEM_GEM)
                 .setIconCoord(0, 0)
                 .setItemName(NAME_GEM)
                 .setCreativeTab(CreativeTabs.tabMaterials);
         metal      = new ItemAFMetal(ItemIds.ID_ITEM_METAL)
                 .setIconCoord(2, 0)
                 .setItemName(NAME_METAL)
                 .setCreativeTab(CreativeTabs.tabMaterials);
         slateFlake = new ItemAFSlateFlake(ItemIds.ID_ITEM_SLATEF)
                .setIconCoord(1, 7)
                 .setItemName(NAME_SLATEF)
                 .setCreativeTab(CreativeTabs.tabMaterials);
         chalk      = new ItemAFChalk(ItemIds.ID_ITEM_CHALK)
                .setIconCoord(0, 6)
                 .setItemName(NAME_CHALK)
                 .setCreativeTab(CreativeTabs.tabMaterials);
         salt       = new ItemAFSalt(ItemIds.ID_ITEM_SALT)
                .setIconCoord(0, 7)
                 .setItemName(NAME_SALT)
                 .setCreativeTab(CreativeTabs.tabMaterials);
 
         int i = 0;
 
         // tools
         for (final EnumAFToolMaterial material : EnumAFToolMaterial.values()) {
 
             // easier to loop than to go through EVERY SINGLE TOOL
             final String name = material.name().toLowerCase(Locale.US);
 
             //axes
             axes[i]     = (ItemAFAxe) new ItemAFAxe(ItemIds.ID_ITEM_AXE[i], material)
                    .setIconCoord(i, 2).setItemName("axe." /* NOI18N */ + name)
                     .setCreativeTab(CreativeTabs.tabTools);
 
             // hoes
             hoes[i]     = (ItemAFHoe) new ItemAFHoe(ItemIds.ID_ITEM_HOE[i], material)
                     .setIconCoord(i, 5).setItemName("hoe." /* NOI18N */ + name)
                     .setCreativeTab(CreativeTabs.tabTools);
 
             // pickaxes
             pickaxes[i] = (ItemAFPickaxe) new ItemAFPickaxe(ItemIds.ID_ITEM_PICKAXE[i], material)
                     .setIconCoord(i, 3).setItemName("pickaxe." /* NOI18N */ + name)
                     .setCreativeTab(CreativeTabs.tabTools);
 
             // spades
             spades[i]   = (ItemAFSpade) new ItemAFSpade(ItemIds.ID_ITEM_SPADE[i], material)
                    .setIconCoord(i, 4).setItemName("spade." /* NOI18N */ + name)
                     .setCreativeTab(CreativeTabs.tabTools);
 
             // swords
             swords[i]   = (ItemAFSword) new ItemAFSword(ItemIds.ID_ITEM_SWORD[i], material)
                     .setIconCoord(i, 1).setItemName("sword." /* NOI18N */ + name)
                     .setCreativeTab(CreativeTabs.tabCombat);
 
             // while we're in here, we might as well add the names
             LanguageRegistry.addName(
                 axes[i],
                 new StringBuilder().append(name.substring(0, 1).toUpperCase(Locale.US))
                                    .append(name.substring(1))
                                    .append(" Axe")
                                    .toString());
             LanguageRegistry.addName(
                 hoes[i],
                 new StringBuilder().append(name.substring(0, 1).toUpperCase(Locale.US))
                                    .append(name.substring(1))
                                    .append(" Hoe")
                                    .toString());
             LanguageRegistry.addName(
                 pickaxes[i],
                 new StringBuilder().append(name.substring(0, 1).toUpperCase(Locale.US))
                                    .append(name.substring(1))
                                    .append(" Pickaxe")
                                    .toString());
             LanguageRegistry.addName(
                 spades[i],
                 new StringBuilder().append(name.substring(0, 1).toUpperCase(Locale.US))
                                    .append(name.substring(1))
                                    .append(" Spade")
                                    .toString());
             LanguageRegistry.addName(
                 swords[i],
                 new StringBuilder().append(name.substring(0, 1).toUpperCase(Locale.US))
                                    .append(name.substring(1))
                                    .append(" Sword")
                                    .toString());
             ++i;
         }
 //J+
 
         // add ALL the names
         LanguageRegistry.addName(new ItemStack(gem, 1, 0), "Quartz");
         LanguageRegistry.addName(new ItemStack(gem, 1, 1), "Amethyst");
         LanguageRegistry.addName(new ItemStack(metal, 1, 0), "Copper Ingot");
         LanguageRegistry.addName(new ItemStack(metal, 1, 1), "Pyrite Ingot");
         LanguageRegistry.addName(new ItemStack(metal, 1, 2), "Magneze Ingot");
         LanguageRegistry.addName(new ItemStack(metal, 1, 3), "Silver Ingot");
         LanguageRegistry.addName(new ItemStack(metal, 1, 4), "Magilith Ingot");
         LanguageRegistry.addName(new ItemStack(metal, 1, 5), "Dragon Bone Ingot");
         LanguageRegistry.addName(new ItemStack(metal, 1, 6), "Platinum Ingot");
         LanguageRegistry.addName(new ItemStack(metal, 1, 7), "Paladinium Ingot");
         LanguageRegistry.addName(new ItemStack(metal, 1, 8), "Firium Ingot");
         LanguageRegistry.addName(new ItemStack(metal, 1, 9), "Glacium Ingot");
         LanguageRegistry.addName(new ItemStack(metal, 1, 10), "Foudrium Ingot");
         LanguageRegistry.addName(new ItemStack(slateFlake, 1, 0), "Slate Flake");
         LanguageRegistry.addName(new ItemStack(slateFlake, 1, 1), "Slate Double-flake");
         LanguageRegistry.addName(new ItemStack(salt, 1, 0), "Salt");
         LanguageRegistry.addName(new ItemStack(chalk, 1, 0), "Black Chalk");
         LanguageRegistry.addName(new ItemStack(chalk, 1, 1), "Red Chalk");
         LanguageRegistry.addName(new ItemStack(chalk, 1, 2), "Green Chalk");
         LanguageRegistry.addName(new ItemStack(chalk, 1, 3), "Brown Chalk");
         LanguageRegistry.addName(new ItemStack(chalk, 1, 4), "Blue Chalk");
         LanguageRegistry.addName(new ItemStack(chalk, 1, 5), "Purple Chalk");
         LanguageRegistry.addName(new ItemStack(chalk, 1, 6), "Cyan Chalk");
         LanguageRegistry.addName(new ItemStack(chalk, 1, 7), "Light Gray Chalk");
         LanguageRegistry.addName(new ItemStack(chalk, 1, 8), "Gray Chalk");
         LanguageRegistry.addName(new ItemStack(chalk, 1, 9), "Pink Chalk");
         LanguageRegistry.addName(new ItemStack(chalk, 1, 10), "Lime Chalk");
         LanguageRegistry.addName(new ItemStack(chalk, 1, 11), "Yellow Chalk");
         LanguageRegistry.addName(new ItemStack(chalk, 1, 12), "Light Blue Chalk");
         LanguageRegistry.addName(new ItemStack(chalk, 1, 13), "Magenta Chalk");
         LanguageRegistry.addName(new ItemStack(chalk, 1, 14), "Orange Chalk");
         LanguageRegistry.addName(new ItemStack(chalk, 1, 15), "White Chalk");
     }
 }
