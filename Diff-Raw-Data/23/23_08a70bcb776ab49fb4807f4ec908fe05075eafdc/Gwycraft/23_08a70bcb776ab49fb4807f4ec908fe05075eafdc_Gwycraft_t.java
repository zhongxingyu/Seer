 package gwydion0917.gwycraft;
 
 // This Import list will grow longer with each additional tutorial.
 // It's not pruned between full class postings, unlike other tutorial code.
 
 import gwydion0917.gwycraft.CommonProxy;
 import gwydion0917.gwycraft.ConfigGwycraft;
 import gwydion0917.gwycraft.blocks.BlockGemCompressed;
 import gwydion0917.gwycraft.blocks.BlockGemOre;
 import gwydion0917.gwycraft.blocks.BlockDyedBookcase;
 import gwydion0917.gwycraft.blocks.BlockDyedBrick;
 import gwydion0917.gwycraft.blocks.BlockDyedClayblock;
 import gwydion0917.gwycraft.blocks.BlockDyedGlass;
 import gwydion0917.gwycraft.blocks.BlockDyedLeaf;
 import gwydion0917.gwycraft.blocks.BlockDyedLog1;
 import gwydion0917.gwycraft.blocks.BlockDyedLog2;
 import gwydion0917.gwycraft.blocks.BlockDyedLog3;
 import gwydion0917.gwycraft.blocks.BlockDyedLog4;
 import gwydion0917.gwycraft.blocks.BlockDyedMudbrick;
 import gwydion0917.gwycraft.blocks.BlockDyedMudbrickStairs;
 import gwydion0917.gwycraft.blocks.BlockDyedPlank;
 import gwydion0917.gwycraft.blocks.BlockDyedSand;
 import gwydion0917.gwycraft.blocks.BlockDyedStone;
 import gwydion0917.gwycraft.blocks.BlockDyedCobble;
 import gwydion0917.gwycraft.blocks.BlockDyedStoneSlab1;
 import gwydion0917.gwycraft.blocks.BlockDyedStoneSlab2;
 import gwydion0917.gwycraft.blocks.BlockDyedStonebrick;
 import gwydion0917.gwycraft.blocks.BlockGlowyWool;
 import gwydion0917.gwycraft.blocks.ItemDyedBookshelf;
 import gwydion0917.gwycraft.blocks.ItemDyedBrick;
 import gwydion0917.gwycraft.blocks.ItemDyedClayblock;
 import gwydion0917.gwycraft.blocks.ItemDyedGlass;
 import gwydion0917.gwycraft.blocks.ItemDyedLeaf;
 import gwydion0917.gwycraft.blocks.ItemDyedLog1;
 import gwydion0917.gwycraft.blocks.ItemDyedLog2;
 import gwydion0917.gwycraft.blocks.ItemDyedLog3;
 import gwydion0917.gwycraft.blocks.ItemDyedLog4;
 import gwydion0917.gwycraft.blocks.ItemDyedMudbrick;
 import gwydion0917.gwycraft.blocks.ItemDyedPlank;
 import gwydion0917.gwycraft.blocks.ItemDyedSand;
 import gwydion0917.gwycraft.blocks.ItemDyedStone;
 import gwydion0917.gwycraft.blocks.ItemDyedStonePaver1;
 import gwydion0917.gwycraft.blocks.ItemDyedStonebrick;
 import gwydion0917.gwycraft.blocks.ItemDyedCobble;
 import gwydion0917.gwycraft.blocks.ItemGemOre;
 import gwydion0917.gwycraft.blocks.ItemGlowyWool;
 import gwydion0917.gwycraft.blocks.ItemGemCompressed;
 import gwydion0917.gwycraft.blocks.BlockDyedStonePaver1;
 import gwydion0917.gwycraft.blocks.BlockDyedStonePaver2;
 import gwydion0917.gwycraft.items.ItemFlawedGems;
 
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockBookshelf;
 import net.minecraft.block.material.Material;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemSlab;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.FurnaceRecipes;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PostInit;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 @Mod(modid = "GwyCraft", name = "GwyCraft", version = "1.0.0")
 @NetworkMod(clientSideRequired = true, serverSideRequired = false)
 public class Gwycraft {
 
 	public static final String[] gwyColorNames = { "White", "Orange", "Magenta", "Light Blue", "Yellow", "Light Green", "Pink", "Dark Grey", "Light Grey", "Cyan", "Purple", "Blue", "Brown", "Green", "Red", "Black" };
     public static final String[] gwyColorLog1Names = { "White", "Orange", "Magenta", "Light Blue" };
     public static final String[] gwyColorLog2Names = { "Yellow", "Light Green", "Pink", "Dark Grey"};
     public static final String[] gwyColorLog3Names = { "Light Grey", "Cyan", "Purple", "Blue"};
     public static final String[] gwyColorLog4Names = { "Brown", "Green", "Red", "Black" };
     public static final String[] gwyColorSlab1Names = { "White", "Orange", "Magenta", "Light Blue", "Yellow", "Light Green", "Pink", "Dark Grey"};
     public static final String[] gwyColorSlab2Names = { "Light Grey", "Cyan", "Purple", "Blue", "Brown", "Green", "Red", "Black"};
     public static final String[] gwyGemNames = { "Flawed Agate", "Flawed Amethyst", "Flawed Aquamarine", "Flawed Citrine", "Flawed Emerald", "Flawed Garnet", "Flawed Hematite", "Flawed Lapis Lazuli", "Flawed Onyx", "Flawed Quartz", "Flawed Ruby", "Flawed Sapphire", "Flawed Tanzanite", "Flawed Tigerseye", "Flawed Topaz", "Flawed Moonstone"};
     
     public static GwycraftTab tabs = new GwycraftTab("GwyCraft");
     
     public static Block glowyWool;
     public static Block dyedStone;
     public static BlockDyedStoneSlab1 dyedStoneSlab1;
     public static BlockDyedStoneSlab1 dyedStoneDoubleSlab1;
     public static BlockDyedStoneSlab2 dyedStoneSlab2;
     public static BlockDyedStoneSlab2 dyedStoneDoubleSlab2;
     public static BlockBookshelf dyedBookcase;
     public static Block dyedBrick;
     public static Block dyedClayblock;
     public static Block dyedGlass;
     public static Block dyedLeaf;
     public static Block dyedLog1;
     public static Block dyedLog2;
     public static Block dyedLog3;
     public static Block dyedLog4;
     public static Block dyedMudbrick;
     public static Block dyedPlank;
     public static Block dyedSand;
     public static Block dyedStonebrick;
     public static Block dyedStonecobble;
     public static Block dyedMudbrickStairs;
     public static Block blockGemOre;
     public static Block blockGemCompressed;
     public static Block blockDyedStonePaver1;
     public static BlockDyedStonePaver2 blockDyedStonePaver2;
 
     public static Item itemFlawedGems;
     public static Item itemDyedClay;
     public static Item itemDyedClayBricks;
     public static Item itemDyedMud;
     public static Item itemDyedMudBricks;
 
 	@Instance("Gwycraft")
 	public static Gwycraft instance;
 
 	@SidedProxy(clientSide = "gwydion0917.gwycraft.client.ClientProxy", serverSide = "gwydion0917.gwycraft.CommonProxy")
 	public static CommonProxy proxy;
 
 	@PreInit
 	public void preInit(FMLPreInitializationEvent event) {
         ConfigGwycraft.initConfig();
         
 		glowyWool = new BlockGlowyWool(ConfigGwycraft.glowyWoolID, Material.cloth).setHardness(0.8F).setStepSound(Block.cloth.stepSound).setUnlocalizedName("Gwycraft:glowyWool").setLightValue(1f).setCreativeTab(tabs);
 		dyedStone = new BlockDyedStone(ConfigGwycraft.dyedStoneID).setHardness(1.5F).setResistance(10.0F).setStepSound(Block.stone.stepSound).setUnlocalizedName("Gwycraft:dyedStone").setCreativeTab(tabs);
 		dyedStoneSlab1 = (BlockDyedStoneSlab1) new BlockDyedStoneSlab1(ConfigGwycraft.dyedStoneSlab1ID, false, Material.rock).setUnlocalizedName("Gwycraft:DyedStoneSlab1").setHardness(2.0F).setCreativeTab(tabs);
 		dyedStoneDoubleSlab1 = (BlockDyedStoneSlab1) new BlockDyedStoneSlab1(ConfigGwycraft.dyedStoneDoubleSlab1ID, true, Material.rock).setUnlocalizedName("Gwycraft:DyedStoneDoubleSlab1").setHardness(2.0F);
 		dyedStoneSlab2 = (BlockDyedStoneSlab2) new BlockDyedStoneSlab2(ConfigGwycraft.dyedStoneSlab2ID, false, Material.rock).setUnlocalizedName("Gwycraft:DyedStoneSlab2").setHardness(2.0F).setCreativeTab(tabs);
 		dyedStoneDoubleSlab2 = (BlockDyedStoneSlab2) new BlockDyedStoneSlab2(ConfigGwycraft.dyedStoneDoubleSlab2ID, true, Material.rock).setUnlocalizedName("Gwycraft:DyedStoneDoubleSlab2").setHardness(2.0F);
 		dyedBookcase = (BlockBookshelf) new BlockDyedBookcase(ConfigGwycraft.dyedBookcaseID, Material.wood).setHardness(1.5F).setStepSound(Block.wood.stepSound).setUnlocalizedName("Gwycraft:dyedBookcase");
 		dyedBrick = new BlockDyedBrick(ConfigGwycraft.dyedBrickID);
 		dyedClayblock = new BlockDyedClayblock(ConfigGwycraft.dyedClayblockID);
 		dyedGlass = new BlockDyedGlass(ConfigGwycraft.dyedGlassID);
 		dyedLeaf = new BlockDyedLeaf(ConfigGwycraft.dyedLeafID);
 		dyedMudbrick = new BlockDyedMudbrick(ConfigGwycraft.dyedMudbrickID);
 		dyedPlank = new BlockDyedPlank(ConfigGwycraft.dyedPlankID).setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundWoodFootstep).setUnlocalizedName("Gwycraft:dyedPlank");
 		dyedSand = new BlockDyedSand(ConfigGwycraft.dyedSandID);
 		dyedStonebrick = new BlockDyedStonebrick(ConfigGwycraft.dyedStonebrickID);
 		dyedStonecobble = new BlockDyedCobble(ConfigGwycraft.dyedStonecobbleID, Material.rock).setHardness(2.0F).setResistance(10.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("Gwycraft:dyedStonecobble").setCreativeTab(tabs);
 		dyedMudbrickStairs = new BlockDyedMudbrickStairs(ConfigGwycraft.dyedMudbrickStairsID, dyedMudbrick, 0).setUnlocalizedName("Gwycraft:DyedMudbrickStairs");
         dyedLog1 = new BlockDyedLog1(ConfigGwycraft.dyedLog1ID);
         dyedLog2 = new BlockDyedLog2(ConfigGwycraft.dyedLog2ID);
         dyedLog3 = new BlockDyedLog3(ConfigGwycraft.dyedLog3ID);
         dyedLog4 = new BlockDyedLog4(ConfigGwycraft.dyedLog4ID);
         blockGemOre = new BlockGemOre(ConfigGwycraft.blockGemOreID, Material.rock).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("gemOre").setCreativeTab(tabs);
         blockGemCompressed = new BlockGemCompressed(ConfigGwycraft.blockGemCompressedID, Material.rock).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundMetalFootstep).setUnlocalizedName("blockGemCompressed").setCreativeTab(tabs);
         blockDyedStonePaver1 = new BlockDyedStonePaver1(ConfigGwycraft.blockDyedStonePaver1ID).setUnlocalizedName("Gwycraft:BlockDyedStonePaver1").setHardness(2.0F).setCreativeTab(tabs);
         blockDyedStonePaver2 = (BlockDyedStonePaver2) new BlockDyedStonePaver2(ConfigGwycraft.blockDyedStonePaver2ID, false, Material.rock).setUnlocalizedName("Gwycraft:DyedStonePaver2").setHardness(2.0F).setCreativeTab(tabs);
         
         itemFlawedGems = new ItemFlawedGems(ConfigGwycraft.itemFlawedGemsID).setUnlocalizedName("Gwycraft:itemFlawedGems").setCreativeTab(tabs);
         itemDyedClay = new ItemFlawedGems(ConfigGwycraft.itemDyedClayID).setUnlocalizedName("Gwycraft:itemDyedClay").setCreativeTab(tabs);
         itemDyedClayBricks = new ItemFlawedGems(ConfigGwycraft.itemDyedClayBricksID).setUnlocalizedName("Gwycraft:itemDyedClayBricks").setCreativeTab(tabs);
         itemDyedMud = new ItemFlawedGems(ConfigGwycraft.itemDyedMudID).setUnlocalizedName("Gwycraft:itemMud").setCreativeTab(tabs);
         itemDyedMudBricks = new ItemFlawedGems(ConfigGwycraft.itemDyedMudBricksID).setUnlocalizedName("Gwycraft:itemMudBricks").setCreativeTab(tabs);
 
         CommonProxy.registerRenderers();
 
         //  GameRegistry Register Blocks
         GameRegistry.registerBlock(glowyWool, ItemGlowyWool.class, "glowyWool");
         GameRegistry.registerBlock(dyedStone, ItemDyedStone.class, "dyedStone");
         GameRegistry.registerBlock(dyedStoneSlab1, "dyedStoneSlab1");
         GameRegistry.registerBlock(dyedStoneSlab2, "dyedStoneSlab2");
         GameRegistry.registerBlock(dyedBookcase, ItemDyedBookshelf.class, "dyedBookcase");
         GameRegistry.registerBlock(dyedPlank, ItemDyedPlank.class, "dyedPlank");
         GameRegistry.registerBlock(dyedBrick, ItemDyedBrick.class, "dyedBrick");
         GameRegistry.registerBlock(dyedClayblock, ItemDyedClayblock.class, "dyedClayblock");
         GameRegistry.registerBlock(dyedGlass, ItemDyedGlass.class, "dyedGlass");
         GameRegistry.registerBlock(dyedLeaf, ItemDyedLeaf.class, "dyedLeaf");
         GameRegistry.registerBlock(dyedLog1, ItemDyedLog1.class, "dyedLog1");
         GameRegistry.registerBlock(dyedLog2, ItemDyedLog2.class, "dyedLog2");
         GameRegistry.registerBlock(dyedLog3, ItemDyedLog3.class, "dyedLog3");
         GameRegistry.registerBlock(dyedLog4, ItemDyedLog4.class, "dyedLog4");
         GameRegistry.registerBlock(dyedMudbrick, ItemDyedMudbrick.class, "dyedMudbrick");
         GameRegistry.registerBlock(dyedSand, ItemDyedSand.class, "dyedSand");
         GameRegistry.registerBlock(dyedStonebrick, ItemDyedStonebrick.class,"dyedStonebrick");
         GameRegistry.registerBlock(dyedStonecobble, ItemDyedCobble.class, "dyedStonecobble");
         GameRegistry.registerBlock(blockGemCompressed, ItemGemCompressed.class, "blockGemCompressed");
         GameRegistry.registerBlock(blockGemOre, ItemGemOre.class, "blockGemOre");
         GameRegistry.registerBlock(blockDyedStonePaver1, ItemDyedStonePaver1.class, "blockDyedStonePaver1");
         GameRegistry.registerBlock(blockDyedStonePaver2, "blockDyedStonePaver2");
 		
         Item.itemsList[ConfigGwycraft.dyedStoneSlab1ID] = (new ItemSlab (ConfigGwycraft.dyedStoneSlab1ID - 256, dyedStoneSlab1, dyedStoneDoubleSlab1, false)).setUnlocalizedName("woodSlab");
 //        Item.itemsList[dyedStoneDoubleSlab1.blockID] = (new ItemSlab (ConfigGwycraft.dyedStoneDoubleSlab1ID - 256, dyedStoneSlab1, dyedStoneDoubleSlab1, true)).setUnlocalizedName("woodSlab");
         Item.itemsList[ConfigGwycraft.dyedStoneSlab2ID] = (new ItemSlab (ConfigGwycraft.dyedStoneSlab2ID - 256, dyedStoneSlab2, dyedStoneDoubleSlab2, false)).setUnlocalizedName("woodSlab");
 //        Item.itemsList[dyedStoneDoubleSlab2.blockID] = (new ItemSlab (ConfigGwycraft.dyedStoneDoubleSlab2ID - 256, dyedStoneSlab2, dyedStoneDoubleSlab2, true)).setUnlocalizedName("woodSlab");
         Item.itemsList[ConfigGwycraft.itemFlawedGemsID] = (new Item (ConfigGwycraft.itemFlawedGemsID - 256)).setUnlocalizedName("itemFlawedGems");
         Item.itemsList[ConfigGwycraft.itemDyedClayID] = (new Item (ConfigGwycraft.itemDyedClayID - 256)).setUnlocalizedName("itemDyedClay");
         Item.itemsList[ConfigGwycraft.itemDyedClayBricksID] = (new Item (ConfigGwycraft.itemDyedClayBricksID - 256)).setUnlocalizedName("itemDyedClayBricks");
         Item.itemsList[ConfigGwycraft.itemDyedMudID] = (new Item (ConfigGwycraft.itemDyedMudID - 256)).setUnlocalizedName("itemDyedMud");
         Item.itemsList[ConfigGwycraft.itemDyedMudBricksID] = (new Item (ConfigGwycraft.itemDyedMudBricksID - 256)).setUnlocalizedName("itemDyedMudBricks");
 
         // Language Registry
         // 16 Meta
         for (int i = 0; i < 16; i++) {
 			ItemStack cloth = new ItemStack(Block.cloth, 1, i);
 			ItemStack glowyWoolStack = new ItemStack(glowyWool, 1, i);
 			ItemStack gsDust = new ItemStack(Item.lightStoneDust, 1);
             ItemStack dyedStoneStack = new ItemStack(dyedStone, 1, i);
             ItemStack dyedBookcaseStack = new ItemStack(dyedBookcase, 1, i);
             ItemStack dyedPlankStack = new ItemStack(dyedPlank, 1, i);
             ItemStack dyedBrickStack = new ItemStack(dyedBrick, 1, i);
             ItemStack dyedClayblockStack = new ItemStack(dyedClayblock, 1, i);
             ItemStack dyedGlassStack = new ItemStack(dyedGlass, 1, i);
             ItemStack dyedLeafStack = new ItemStack(dyedLeaf, 1, i);
             ItemStack dyedMudbrickStack = new ItemStack(dyedMudbrick, 1, i);
             ItemStack dyedSandStack = new ItemStack(dyedSand, 1, i);
             ItemStack dyedStonebrickStack = new ItemStack(dyedStonebrick, 1, i);
             ItemStack dyedStonecobbleStack = new ItemStack(dyedStonecobble, 1, i);
             ItemStack blockGemCompressedStack = new ItemStack(blockGemCompressed, 1, i);
             ItemStack blockGemOreStack = new ItemStack(blockGemOre, 1, i);
             ItemStack blockDyedStonePaver1Stack = new ItemStack(blockDyedStonePaver1, 1, i);
             ItemStack itemFlawedGemsStack = new ItemStack(itemFlawedGems, 1, i);
             ItemStack itemDyedClayStack = new ItemStack(itemDyedClay, 1, i);
             ItemStack itemDyedClayBricksStack = new ItemStack(itemDyedClayBricks, 1, i);
             ItemStack itemDyedMudStack = new ItemStack(itemDyedMud, 1, i);
             ItemStack itemDyedMudBricksStack = new ItemStack(itemDyedMudBricks, 1, i);
 			
             // Language Registry
 			LanguageRegistry.addName(glowyWoolStack, "Glowy " + gwyColorNames[glowyWoolStack.getItemDamage()] + " Wool");
 			LanguageRegistry.addName(dyedStoneStack, gwyColorNames[dyedStoneStack.getItemDamage()] + " Stone");
 		    LanguageRegistry.addName(dyedBookcaseStack, gwyColorNames[dyedBookcaseStack.getItemDamage()] + " Bookcase");
             LanguageRegistry.addName(dyedPlankStack, gwyColorNames[dyedPlankStack.getItemDamage()] + " Plank");
 			LanguageRegistry.addName(dyedBrickStack, gwyColorNames[dyedBrickStack.getItemDamage()] + " Brick");
 			LanguageRegistry.addName(dyedClayblockStack, gwyColorNames[dyedClayblockStack.getItemDamage()] + " Clay Block");
 			LanguageRegistry.addName(dyedGlassStack, gwyColorNames[dyedGlassStack.getItemDamage()] + " Glass");
 			LanguageRegistry.addName(dyedLeafStack, gwyColorNames[dyedLeafStack.getItemDamage()] + " Leaf");
             LanguageRegistry.addName(dyedMudbrickStack, gwyColorNames[dyedMudbrickStack.getItemDamage()] + " Mud Brick Block");
             LanguageRegistry.addName(dyedSandStack, gwyColorNames[dyedSandStack.getItemDamage()] + " Sand");
             LanguageRegistry.addName(dyedStonebrickStack, gwyColorNames[dyedStonebrickStack.getItemDamage()] + " Stone Brick");
             LanguageRegistry.addName(dyedStonecobbleStack, gwyColorNames[dyedStonecobbleStack.getItemDamage()] + " Cobblestone");
             LanguageRegistry.addName(blockGemCompressedStack, gwyGemNames[blockGemCompressedStack.getItemDamage()] + " Block");
             LanguageRegistry.addName(blockGemOreStack, gwyGemNames[blockGemOreStack.getItemDamage()] + " Ore");
             LanguageRegistry.addName(blockDyedStonePaver1Stack, gwyColorNames[blockDyedStonePaver1Stack.getItemDamage()] + " Stone Paver");
             LanguageRegistry.addName(itemFlawedGemsStack, gwyGemNames[i]);
             LanguageRegistry.addName(itemFlawedGemsStack, gwyColorNames[i] + " Clay");
             LanguageRegistry.addName(itemFlawedGemsStack, gwyColorNames[i] + " Clay Bricks");
             LanguageRegistry.addName(itemFlawedGemsStack, gwyColorNames[i] + " Mud");
             LanguageRegistry.addName(itemFlawedGemsStack, gwyColorNames[i] + " Mud Bricks");
 
             //  Add Recipes
             GameRegistry.addRecipe(new ItemStack(dyedBookcase, 1, i), "###", "XXX", "###", '#', dyedPlankStack, 'X', Item.book);
             GameRegistry.addRecipe(new ItemStack(dyedStonebrick, 4, i), "xx", "xx", 'x', dyedStoneStack);
             GameRegistry.addRecipe(glowyWoolStack, " x ", "xyx", " x ", 'y', cloth, 'x', gsDust);
             GameRegistry.addShapelessRecipe(glowyWoolStack, new ItemStack(Block.glowStone), cloth);
             FurnaceRecipes.smelting().addSmelting(dyedStonecobble.blockID, i, new ItemStack(dyedStone, 1, i ), 0.1F);
             FurnaceRecipes.smelting().addSmelting(dyedSand.blockID, i, new ItemStack(dyedGlass, 1, i ), 0.1F);
         }
         
         // 8 Meta
         for (int i = 0; i < 8; i++) {
 
             // Item Stacks
             ItemStack dyedStoneSlab1Stack = new ItemStack(dyedStoneSlab1, 1, i);
             ItemStack dyedStoneSlab2Stack = new ItemStack(dyedStoneSlab2, 1, i);
             ItemStack dyedStone1Stack = new ItemStack(dyedStone, 8, i);
             ItemStack dyedStone2Stack = new ItemStack(dyedStone, 8, i+8);
             ItemStack blockDyedStonePaver2Stack = new ItemStack(blockDyedStonePaver2, 1, i);
 
             // Language Registry
             LanguageRegistry.addName(dyedStoneSlab1Stack, gwyColorSlab1Names[dyedStoneSlab1Stack.getItemDamage()] + " Stone Slab");
             LanguageRegistry.addName(dyedStoneSlab2Stack, gwyColorSlab2Names[dyedStoneSlab2Stack.getItemDamage()] + " Stone Slab");
             LanguageRegistry.addName(blockDyedStonePaver2Stack, gwyColorSlab2Names[blockDyedStonePaver2Stack.getItemDamage()] + " Stone Paver2");
 
             //  Add Recipes
             GameRegistry.addRecipe(new ItemStack(dyedStoneSlab1, 6, i), "XXX", 'X', dyedStone1Stack);
             GameRegistry.addRecipe(new ItemStack(dyedStoneSlab2, 6, i), "XXX", 'X', dyedStone2Stack);
             GameRegistry.addRecipe(new ItemStack(blockDyedStonePaver2, 12, i), "XXX", 'X', dyedStoneSlab2Stack);
         }        
    
         // 4 Meta
         for (int i = 0; i < 4; i++) {
 
             // Item Stacks
             ItemStack dyedLog1Stack = new ItemStack(dyedLog1, 1, i);
             ItemStack dyedLog2Stack = new ItemStack(dyedLog2, 1, i);
             ItemStack dyedLog3Stack = new ItemStack(dyedLog3, 1, i);
             ItemStack dyedLog4Stack = new ItemStack(dyedLog4, 1, i);
 
             // Language Registry
             LanguageRegistry.addName(dyedLog1Stack, gwyColorLog1Names[dyedLog1Stack.getItemDamage()] + " Log");
             LanguageRegistry.addName(dyedLog2Stack, gwyColorLog2Names[dyedLog2Stack.getItemDamage()] + " Log");
             LanguageRegistry.addName(dyedLog3Stack, gwyColorLog3Names[dyedLog3Stack.getItemDamage()] + " Log");
             LanguageRegistry.addName(dyedLog4Stack, gwyColorLog4Names[dyedLog4Stack.getItemDamage()] + " Log");
 
             //  Add Recipes
             GameRegistry.addShapelessRecipe(new ItemStack(dyedPlank, 4, i), dyedLog1Stack);
             GameRegistry.addShapelessRecipe(new ItemStack(dyedPlank, 4, i+4), dyedLog2Stack);
             GameRegistry.addShapelessRecipe(new ItemStack(dyedPlank, 4, i+8), dyedLog3Stack);
             GameRegistry.addShapelessRecipe(new ItemStack(dyedPlank, 4, i+12), dyedLog4Stack);
         }
 
         // Items
         // Language Registry
 
 //        GameRegistry.addRecipe(new ItemStack(blockGemCompressed, 1, 0), "XXX", "XXX", "XXX", 'X', flawedAgate);
 //        GameRegistry.addRecipe(new ItemStack(blockGemCompressed, 1, 1), "XXX", "XXX", "XXX", 'X', flawedAmethyst);
 //        GameRegistry.addRecipe(new ItemStack(blockGemCompressed, 1, 2), "XXX", "XXX", "XXX", 'X', flawedAquamarine);
 //        GameRegistry.addRecipe(new ItemStack(blockGemCompressed, 1, 3), "XXX", "XXX", "XXX", 'X', flawedCitrine);
 //        GameRegistry.addRecipe(new ItemStack(blockGemCompressed, 1, 4), "XXX", "XXX", "XXX", 'X', flawedEmerald);
 //        GameRegistry.addRecipe(new ItemStack(blockGemCompressed, 1, 5), "XXX", "XXX", "XXX", 'X', flawedGarnet);
 //        GameRegistry.addRecipe(new ItemStack(blockGemCompressed, 1, 6), "XXX", "XXX", "XXX", 'X', flawedHematite);
 //        GameRegistry.addRecipe(new ItemStack(blockGemCompressed, 1, 7), "XXX", "XXX", "XXX", 'X', flawedLapis);
 //        GameRegistry.addRecipe(new ItemStack(blockGemCompressed, 1, 8), "XXX", "XXX", "XXX", 'X', flawedOnyx);
 //        GameRegistry.addRecipe(new ItemStack(blockGemCompressed, 1, 9), "XXX", "XXX", "XXX", 'X', flawedQuartz);
 //        GameRegistry.addRecipe(new ItemStack(blockGemCompressed, 1, 10), "XXX", "XXX", "XXX", 'X', flawedRuby);
 //        GameRegistry.addRecipe(new ItemStack(blockGemCompressed, 1, 11), "XXX", "XXX", "XXX", 'X', flawedSapphire);
 //        GameRegistry.addRecipe(new ItemStack(blockGemCompressed, 1, 12), "XXX", "XXX", "XXX", 'X', flawedTanzanite);
 //        GameRegistry.addRecipe(new ItemStack(blockGemCompressed, 1, 13), "XXX", "XXX", "XXX", 'X', flawedTigerseye);
 //        GameRegistry.addRecipe(new ItemStack(blockGemCompressed, 1, 14), "XXX", "XXX", "XXX", 'X', flawedTopaz);
 //        GameRegistry.addRecipe(new ItemStack(blockGemCompressed, 1, 15), "XXX", "XXX", "XXX", 'X', flawedMoonstone);
     }
 	
     @Init 
     public void init(FMLInitializationEvent event) {
 
 
 	}
 
 	@PostInit
 	public void postInit(FMLPostInitializationEvent event) {
 		// Stub Method
 	}
 }
