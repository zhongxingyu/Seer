 package mods.minecraft.darth.dc.item;
 
 import mods.minecraft.darth.dc.DiscoveryCraft;
 import mods.minecraft.darth.dc.lib.ItemIDs;
 import mods.minecraft.darth.dc.lib.Strings;
 
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.MinecraftForge;
 
 
 public class ModItems
 {
     
     //Item Instances
     public static Item sciNotebook;
     public static Item unknownDust;
     public static Item monocleLens;
     public static Item notebookLock;
     public static Item sonicScrewdriver;
     public static Item debugTool;
     public static Item craftingUpgrade;
     public static Item knife;
     public static Item unknownChunks;
     public static Item shovelFlint;
     public static Item pickaxeFlint;
     public static Item axeFlint;
     public static Item dirtPellet;
     
     
     public static void init()
     {
         
         //Initialize each Regular Item
         sciNotebook = new ItemSciNotebook(ItemIDs.SCI_NOTEBOOK);
         unknownDust = new ItemDC(ItemIDs.UNKNOWN_DUST).setUnlocalizedName(Strings.UNKNOWN_DUST_NAME).setCreativeTab(DiscoveryCraft.tabDC).setMaxStackSize(64);
         unknownChunks = new ItemDC(ItemIDs.UNKNOWN_CHUNKS).setUnlocalizedName(Strings.UNKNOWN_CHUNKS_NAME).setCreativeTab(DiscoveryCraft.tabDC).setMaxStackSize(64);
         sonicScrewdriver = new ItemSonicScrewdriver(ItemIDs.SONIC_SCREWDRIVER);
         debugTool = new ItemDebug(ItemIDs.DEBUG_TOOL);
         craftingUpgrade = new ItemCraftingUpgrade(ItemIDs.CRAFTING_UPGRADE);
         knife = new ItemKnife(ItemIDs.KNIFE);
         shovelFlint = new ItemDC(ItemIDs.FLINT_SHOVEL).setUnlocalizedName(Strings.FLINT_SHOVEL_NAME).setCreativeTab(DiscoveryCraft.tabDC);
         pickaxeFlint = new ItemDC(ItemIDs.FLINT_PICKAXE).setUnlocalizedName(Strings.FLINT_PICKAXE_NAME).setCreativeTab(DiscoveryCraft.tabDC);
         axeFlint = new ItemDC(ItemIDs.FLINT_AXE).setUnlocalizedName(Strings.FLINT_AXE_NAME).setCreativeTab(DiscoveryCraft.tabDC);
         
         
         //Initialize each Crafting Ingredient Item
         notebookLock = new ItemCrafting(ItemIDs.NOTEBOOK_LOCK).setUnlocalizedName(Strings.NOTEBOOK_LOCK_NAME);
        dirtPellet = new ItemDC(ItemIDs.DIRT_PELLET).setUnlocalizedName(Strings.DIRT_PELLET_NAME);
         monocleLens = new ItemCrafting(ItemIDs.MONOCLE_LENS).setUnlocalizedName(Strings.MONOCLE_LENS_NAME);
         
         
         registry();
     }
     
     
     private static void registry()
     {
        MinecraftForge.setToolClass(shovelFlint, "shovel", 0);
        MinecraftForge.setToolClass(pickaxeFlint, "pickaxe", 0);
        MinecraftForge.setToolClass(axeFlint, "axe", 0);
         
         MinecraftForge.addGrassSeed(new ItemStack(dirtPellet), 4);
     }
     
     
 }
