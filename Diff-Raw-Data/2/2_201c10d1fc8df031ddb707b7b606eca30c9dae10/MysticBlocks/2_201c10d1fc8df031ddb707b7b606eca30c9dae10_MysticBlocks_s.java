 package net.minecraft4455.mysticrpg.core.blocks;
 
 import net.minecraft.block.Block;
 import net.minecraft4455.mysticrpg.core.configs.MysticConfig;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 public class MysticBlocks {
 
     public static Block BlackMercy;
     public static Block MysticStone;
     public static Block YirawiaDirt;
     public static Block YirawiaGrass;
     public static Block MysticSand;
     public static Block YirawiaPortal;
     public static Block YirawiaBlock;
     public static Block ViriaFire;
 
     public static void init() {
 
         initBlocks();
         registerBlocks();
         // addNames();
 
     }
 
     private static void initBlocks() {
 
         BlackMercy = new BlackMercy(MysticConfig.BlackMercyID)
                 .setUnlocalizedName("BlackMercy");
         MysticStone = new MysticStone(200).setUnlocalizedName("MysticStone");
         YirawiaDirt = new YirawiaDirt(201).setUnlocalizedName("YirawiaDirt");
         YirawiaGrass = new YirawiaGrass(202).setUnlocalizedName("YirawiaGrass");
         MysticSand = new MysticSand(203).setUnlocalizedName("MysticSand");
         YirawiaPortal = new YirawiaPortal(MysticConfig.YirawiaPortalID)
                 .setUnlocalizedName("YirawiaPortal");
         YirawiaBlock = new YirawiaBlock(MysticConfig.YirawiaBlockID)
                 .setUnlocalizedName("YirawiaBlock");
         /*
          * TODO disabling this because it's causing crashes and that's annoying
          * while testing some things ViriaFire = new
          * ViriaFire(MysticConfig.ViriaFireID) .setUnlocalizedName("ViriaFire");
          */
 
     }
 
     private static void registerBlocks() {
 
         GameRegistry.registerBlock(BlackMercy, "BlackMercy");
         GameRegistry.registerBlock(MysticStone, "MysticStone");
         GameRegistry.registerBlock(YirawiaDirt, "YirawiaDirt");
         GameRegistry.registerBlock(YirawiaGrass, "YirawiaGrass");
         GameRegistry.registerBlock(MysticSand, "MysticSand");
         GameRegistry.registerBlock(YirawiaPortal, "YirawiaPortal");
         GameRegistry.registerBlock(YirawiaBlock, "YirawiaBlock");
        GameRegistry.registerBlock(ViriaFire, "ViriaFire");
 
     }
 
     @SuppressWarnings("unused")
     private static void addNames() {
 
         // LanguageRegistry.addName(BlackMercy, "Black Mercy");
         // LanguageRegistry.addName(MysticStone, "Stone");
         // LanguageRegistry.addName(YirawiaDirt, "Yirawia Dirt");
         // LanguageRegistry.addName(YirawiaGrass, "Yirawia Grass");
         // LanguageRegistry.addName(MysticSand, "Sand");
         // LanguageRegistry.addName(YirawiaPortal, "Yirawia Portal");
         // LanguageRegistry.addName(YirawiaBlock, "Yirawia Block");
         // LanguageRegistry.addName(ViriaFire, "Viria Fire");
 
     }
 
 }
