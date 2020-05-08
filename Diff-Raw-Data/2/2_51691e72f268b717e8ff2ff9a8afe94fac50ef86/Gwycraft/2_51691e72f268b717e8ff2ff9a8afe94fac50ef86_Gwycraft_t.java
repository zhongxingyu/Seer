 package gwydion0917.gwycraft;
 
 // This Import list will grow longer with each additional tutorial.
 // It's not pruned between full class postings, unlike other tutorial code.
 
 import gwydion0917.gwycraft.CommonProxy;
 import gwydion0917.gwycraft.ConfigGwycraft;
 import gwydion0917.gwycraft.blocks.BlockDyedFences;
 import gwydion0917.gwycraft.blocks.BlockDyedStoneBrickSlab1;
 import gwydion0917.gwycraft.blocks.BlockDyedStoneBrickSlab2;
 import gwydion0917.gwycraft.blocks.BlockDyedTorchWhite;
 import gwydion0917.gwycraft.blocks.BlockDyedTorchOrange;
 import gwydion0917.gwycraft.blocks.BlockDyedTorchMagenta;
 import gwydion0917.gwycraft.blocks.BlockDyedTorchLBlue;
 import gwydion0917.gwycraft.blocks.BlockDyedTorchYellow;
 import gwydion0917.gwycraft.blocks.BlockDyedTorchLGreen;
 import gwydion0917.gwycraft.blocks.BlockDyedTorchPink;
 import gwydion0917.gwycraft.blocks.BlockDyedTorchGray;
 import gwydion0917.gwycraft.blocks.BlockDyedTorchLGray;
 import gwydion0917.gwycraft.blocks.BlockDyedTorchCyan;
 import gwydion0917.gwycraft.blocks.BlockDyedTorchPurple;
 import gwydion0917.gwycraft.blocks.BlockDyedTorchBlue;
 import gwydion0917.gwycraft.blocks.BlockDyedTorchBrown;
 import gwydion0917.gwycraft.blocks.BlockDyedTorchGreen;
 import gwydion0917.gwycraft.blocks.BlockDyedTorchRed;
 import gwydion0917.gwycraft.blocks.BlockDyedTorchBlack;
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
 import gwydion0917.gwycraft.blocks.BlockDyedMudBrickStairs;
 import gwydion0917.gwycraft.blocks.BlockDyedPlank;
 import gwydion0917.gwycraft.blocks.BlockDyedSand;
 import gwydion0917.gwycraft.blocks.BlockDyedStone;
 import gwydion0917.gwycraft.blocks.BlockDyedCobble;
 import gwydion0917.gwycraft.blocks.BlockDyedStoneSlab1;
 import gwydion0917.gwycraft.blocks.BlockDyedStoneSlab2;
 import gwydion0917.gwycraft.blocks.BlockDyedStonebrick;
 import gwydion0917.gwycraft.blocks.BlockGlowyDyedGlass;
 import gwydion0917.gwycraft.blocks.BlockGlowyWool;
 import gwydion0917.gwycraft.blocks.BlockGwyGeneric;
 import gwydion0917.gwycraft.blocks.ItemDyedBookshelf;
 import gwydion0917.gwycraft.blocks.ItemDyedBrick;
 import gwydion0917.gwycraft.blocks.ItemDyedClayblock;
 import gwydion0917.gwycraft.blocks.ItemDyedFences;
 import gwydion0917.gwycraft.blocks.ItemDyedGlass;
 import gwydion0917.gwycraft.blocks.ItemDyedLeaf;
 import gwydion0917.gwycraft.blocks.ItemDyedLog1;
 import gwydion0917.gwycraft.blocks.ItemDyedLog2;
 import gwydion0917.gwycraft.blocks.ItemDyedLog3;
 import gwydion0917.gwycraft.blocks.ItemDyedLog4;
 import gwydion0917.gwycraft.blocks.ItemDyedMudBrick;
 import gwydion0917.gwycraft.blocks.ItemDyedMudBrickStairs;
 import gwydion0917.gwycraft.blocks.ItemDyedPlank;
 import gwydion0917.gwycraft.blocks.ItemDyedSand;
 import gwydion0917.gwycraft.blocks.ItemDyedStone;
 import gwydion0917.gwycraft.blocks.ItemDyedStonePaver1;
 import gwydion0917.gwycraft.blocks.ItemDyedStonePaver2;
 import gwydion0917.gwycraft.blocks.ItemDyedStonebrick;
 import gwydion0917.gwycraft.blocks.ItemDyedCobble;
 import gwydion0917.gwycraft.blocks.ItemGemOre;
 import gwydion0917.gwycraft.blocks.ItemGlowyDyedGlass;
 import gwydion0917.gwycraft.blocks.ItemGlowyWool;
 import gwydion0917.gwycraft.blocks.ItemGemCompressed;
 import gwydion0917.gwycraft.blocks.BlockDyedStonePaver1;
 import gwydion0917.gwycraft.blocks.BlockDyedStonePaver2;
 import gwydion0917.gwycraft.blocks.ItemDyedCobbleWalls;
 import gwydion0917.gwycraft.blocks.BlockDyedCobbleWalls;
 import gwydion0917.gwycraft.blocks.ItemGwyGeneric;
 import gwydion0917.gwycraft.items.ItemDyedClay;
 import gwydion0917.gwycraft.items.ItemDyedClayBricks;
 import gwydion0917.gwycraft.items.ItemDyedMud;
 import gwydion0917.gwycraft.items.ItemDyedMudBricks;
 import gwydion0917.gwycraft.items.ItemDyedSticks;
 import gwydion0917.gwycraft.items.ItemEnchantedGems;
 import gwydion0917.gwycraft.items.ItemGemShears;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockBookshelf;
 import net.minecraft.block.BlockHalfSlab;
 import net.minecraft.block.material.Material;
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
 
 @Mod(modid = "GwyCraft", name = "GwyCraft", version = "1.0.0", dependencies = "required-after:Forge@[7.7.1.650,)" )
 @NetworkMod(clientSideRequired = true, serverSideRequired = false)
 public class Gwycraft {
 
 	public static final String[] gwyColorNames = { "White", "Orange", "Magenta", "Light Blue", "Yellow", "Light Green", "Pink", "Dark Grey", "Light Grey", "Cyan", "Purple", "Blue", "Brown", "Green", "Red", "Black" };
     public static final String[] gwyColorLog1Names = { "White", "Orange", "Magenta", "Light Blue" };
     public static final String[] gwyColorLog2Names = { "Yellow", "Light Green", "Pink", "Dark Grey"};
     public static final String[] gwyColorLog3Names = { "Light Grey", "Cyan", "Purple", "Blue"};
     public static final String[] gwyColorLog4Names = { "Brown", "Green", "Red", "Black" };
     public static final String[] gwyColorSlab1Names = { "White", "Orange", "Magenta", "Light Blue", "Yellow", "Light Green", "Pink", "Dark Grey"};
     public static final String[] gwyColorSlab2Names = { "Light Grey", "Cyan", "Purple", "Blue", "Brown", "Green", "Red", "Black"};
     public static final String[] gwyGemNames = { "Enchanted Quartz", "Enchanted Citrine", "Enchanted Tanzanite", "Enchanted Sapphire", "Enchanted Topaz", "Enchanted Agate", "Enchanted Garnet",  "Enchanted Moonstone", "Enchanted Hematite", "Enchanted Aquamarine", "Enchanted Amethyst",  "Enchanted Lapis Lazuli",  "Enchanted Tigerseye",  "Enchanted Emerald", "Enchanted Ruby", "Enchanted Onyx"};
 
     public static GwycraftWorldGenerator worldGen = new GwycraftWorldGenerator();
     
     public static GwycraftTab tabs = new GwycraftTab("GwyCraft");
     
     public static Block glowyWool;
     public static Block dyedStone;
     public static Block glowydyedStone;
     public static BlockDyedStoneSlab1 dyedStoneSlab1;
     public static BlockDyedStoneSlab1 dyedStoneDoubleSlab1;
     public static BlockDyedStoneSlab2 dyedStoneSlab2;
     public static BlockDyedStoneSlab2 dyedStoneDoubleSlab2;
     public static BlockBookshelf dyedBookcase;
     public static Block dyedBrick;
     public static Block glowydyedBrick;
     public static Block dyedClayblock;
     public static Block glowydyedClayblock;
     public static Block dyedGlass;
     public static Block glowydyedGlass;
     public static Block dyedLeaf;
     public static Block dyedLog1;
     public static Block dyedLog2;
     public static Block dyedLog3;
     public static Block dyedLog4;
     public static Block blockDyedMudBrick;
     public static Block glowyblockDyedMudBrick;
     public static Block dyedPlank;
     public static Block glowydyedPlank;
     public static Block dyedSand;
     public static Block glowydyedSand;
     public static Block dyedStonebrick;
     public static Block glowydyedStonebrick;    
     public static Block dyedStonecobble;
     public static Block glowydyedStonecobble;
     public static Block blockDyedMudBrickStairs;
     public static Block blockGemOre;
     public static Block blockGemCompressed;
     public static Block blockDyedStonePaver1;
     public static Block blockDyedStonePaver2;
     public static BlockHalfSlab blockDyedStoneBrickSlab1;
     public static BlockHalfSlab blockDyedStoneBrickDoubleSlab1;
     public static BlockHalfSlab blockDyedStoneBrickSlab2;
     public static BlockHalfSlab blockDyedStoneBrickDoubleSlab2;
     public static Block blockDyedFences;
     public static Block blockDyedCobbleWalls;
     public static Block blockWhiteDyedTorch;
     public static Block blockOrangeDyedTorch;
     public static Block blockMagentaDyedTorch;
     public static Block blockLBlueDyedTorch;
     public static Block blockYellowDyedTorch;
     public static Block blockLGreenDyedTorch;
     public static Block blockPinkDyedTorch;
     public static Block blockDGrayDyedTorch;
     public static Block blockLGrayDyedTorch;
     public static Block blockCyanDyedTorch;
     public static Block blockPurpleDyedTorch;
     public static Block blockBlueDyedTorch;
     public static Block blockBrownDyedTorch;
     public static Block blockGreenDyedTorch;
     public static Block blockRedDyedTorch;
     public static Block blockBlackDyedTorch;
     
     public static Item itemFlawedGems;
     public static Item itemDyedClay;
     public static Item itemDyedClayBricks;
     public static Item itemDyedMud;
     public static Item itemDyedMudBricks;
     public static Item itemDyedSticks;
     public static Item itemGemShears;
     
 	@Instance("Gwycraft")
 	public static Gwycraft instance;
 
 	@SidedProxy(clientSide = "gwydion0917.gwycraft.client.ClientProxy", serverSide = "gwydion0917.gwycraft.CommonProxy")
 	public static CommonProxy proxy;
 
 	@PreInit
 	public void preInit(FMLPreInitializationEvent event) {
         ConfigGwycraft.initConfig(event);
         
 		glowyWool = new BlockGlowyWool(ConfigGwycraft.glowyWoolID, Material.cloth).setHardness(0.8F).setStepSound(Block.cloth.stepSound).setUnlocalizedName("Gwycraft:glowyWool").setLightValue(1f).setCreativeTab(tabs);
 		dyedStone = new BlockDyedStone(ConfigGwycraft.dyedStoneID).setHardness(1.5F).setResistance(10.0F).setStepSound(Block.stone.stepSound).setUnlocalizedName("Gwycraft:dyedStone").setCreativeTab(tabs);
 		glowydyedStone = new BlockGwyGeneric(ConfigGwycraft.glowydyedStoneID, Material.rock, "stone").setLightValue(1F).setHardness(1.5F).setResistance(10.0F).setStepSound(Block.stone.stepSound).setUnlocalizedName("Gwycraft:glowydyedStone").setCreativeTab(tabs);
 		dyedStoneSlab1 = (BlockDyedStoneSlab1) new BlockDyedStoneSlab1(ConfigGwycraft.dyedStoneSlab1ID, false, Material.rock).setUnlocalizedName("Gwycraft:DyedStoneSlab1").setHardness(2.0F).setCreativeTab(tabs);
 		dyedStoneDoubleSlab1 = (BlockDyedStoneSlab1) new BlockDyedStoneSlab1(ConfigGwycraft.dyedStoneDoubleSlab1ID, true, Material.rock).setUnlocalizedName("Gwycraft:DyedStoneDoubleSlab1").setHardness(2.0F);
 		dyedStoneSlab2 = (BlockDyedStoneSlab2) new BlockDyedStoneSlab2(ConfigGwycraft.dyedStoneSlab2ID, false, Material.rock).setUnlocalizedName("Gwycraft:DyedStoneSlab2").setHardness(2.0F).setCreativeTab(tabs);
 		dyedStoneDoubleSlab2 = (BlockDyedStoneSlab2) new BlockDyedStoneSlab2(ConfigGwycraft.dyedStoneDoubleSlab2ID, true, Material.rock).setUnlocalizedName("Gwycraft:DyedStoneDoubleSlab2").setHardness(2.0F);
 		dyedBookcase = (BlockBookshelf) new BlockDyedBookcase(ConfigGwycraft.dyedBookcaseID, Material.wood).setHardness(1.5F).setStepSound(Block.wood.stepSound).setUnlocalizedName("Gwycraft:dyedBookcase").setCreativeTab(tabs);
 		dyedBrick = new BlockDyedBrick(ConfigGwycraft.dyedBrickID).setCreativeTab(tabs);
 		glowydyedBrick = new BlockGwyGeneric(ConfigGwycraft.glowydyedBrickID, Material.rock, "brick").setLightValue(1F).setHardness(2F).setStepSound(Block.stone.stepSound).setResistance(10F).setUnlocalizedName("Gwycraft:glowydyedBrick").setCreativeTab(tabs);
 		dyedClayblock = new BlockDyedClayblock(ConfigGwycraft.dyedClayblockID).setCreativeTab(tabs);
 		glowydyedClayblock = new BlockGwyGeneric(ConfigGwycraft.glowydyedClayblockID, Material.rock, "clayblock").setLightValue(1F).setHardness(0.6F).setStepSound(Block.stone.stepSound).setUnlocalizedName("Gwycraft:glowydyedClayblock").setCreativeTab(tabs);
 		dyedGlass = new BlockDyedGlass(ConfigGwycraft.dyedGlassID).setCreativeTab(tabs);
         glowydyedGlass = new BlockGlowyDyedGlass(ConfigGwycraft.glowydyedGlassID).setLightValue(1F).setHardness(0.6F).setStepSound(Block.stone.stepSound).setUnlocalizedName("Gwycraft:glowydyedGlass").setCreativeTab(tabs);
 		dyedLeaf = new BlockDyedLeaf(ConfigGwycraft.dyedLeafID).setCreativeTab(tabs);
 		blockDyedMudBrick = new BlockDyedMudbrick(ConfigGwycraft.blockDyedMudBrickID).setCreativeTab(tabs);
 		glowyblockDyedMudBrick = new BlockGwyGeneric(ConfigGwycraft.glowyBlockDyedMudBrickID, Material.rock, "mudbrick").setLightValue(1F).setHardness(1.5f).setStepSound(Block.stone.stepSound).setUnlocalizedName("Gwycraft:dyedMudbrick").setCreativeTab(tabs);
 		dyedPlank = new BlockDyedPlank(ConfigGwycraft.dyedPlankID).setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundWoodFootstep).setUnlocalizedName("Gwycraft:dyedPlank").setCreativeTab(tabs);
 		glowydyedPlank = new BlockGwyGeneric(ConfigGwycraft.glowydyedPlankID, Material.wood, "plank").setLightValue(1F).setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundWoodFootstep).setUnlocalizedName("Gwycraft:glowydyedPlank").setCreativeTab(tabs);
 		dyedSand = new BlockDyedSand(ConfigGwycraft.dyedSandID).setCreativeTab(tabs);
 		glowydyedSand = new BlockGwyGeneric(ConfigGwycraft.glowydyedSandID, Material.sand, "sand").setLightValue(1F).setHardness(0.5F).setStepSound(Block.soundSandFootstep).setUnlocalizedName("Gwycraft:glowydyedSand").setCreativeTab(tabs);
 		dyedStonebrick = new BlockDyedStonebrick(ConfigGwycraft.dyedStonebrickID).setCreativeTab(tabs);
 		glowydyedStonebrick = new BlockGwyGeneric(ConfigGwycraft.glowydyedStonebrickID, Material.rock, "stonebrick").setLightValue(1F).setHardness(2f).setStepSound(Block.stone.stepSound).setUnlocalizedName("Gwycraft:glowydyedStonebrick").setCreativeTab(tabs);
 		dyedStonecobble = new BlockDyedCobble(ConfigGwycraft.dyedStonecobbleID, Material.rock).setHardness(2.0F).setResistance(10.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("Gwycraft:dyedStonecobble").setCreativeTab(tabs);
		glowydyedStonecobble = new BlockGwyGeneric(ConfigGwycraft.glowydyedStonecobbleID, Material.rock, "stonecobble").setLightValue(1F).setHardness(2.0F).setResistance(10.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("Gwycraft:glowydyedStonecobble").setCreativeTab(tabs);
 		blockDyedCobbleWalls = new BlockDyedCobbleWalls(ConfigGwycraft.blockDyedCobbleWallsID, dyedStonecobble).setHardness(2.0F).setResistance(10.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("Gwycraft:blockDyedCobbleWalls").setCreativeTab(tabs);
 		blockDyedMudBrickStairs = new BlockDyedMudBrickStairs(ConfigGwycraft.dyedMudbrickStairsID, blockDyedMudBrick, 0).setUnlocalizedName("Gwycraft:DyedMudbrickStairs").setCreativeTab(tabs);
         dyedLog1 = new BlockDyedLog1(ConfigGwycraft.dyedLog1ID).setCreativeTab(tabs);
         dyedLog2 = new BlockDyedLog2(ConfigGwycraft.dyedLog2ID).setCreativeTab(tabs);
         dyedLog3 = new BlockDyedLog3(ConfigGwycraft.dyedLog3ID).setCreativeTab(tabs);
         dyedLog4 = new BlockDyedLog4(ConfigGwycraft.dyedLog4ID).setCreativeTab(tabs);
         blockGemOre = new BlockGemOre(ConfigGwycraft.blockGemOreID, Material.rock).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("gemOre").setCreativeTab(tabs);
         blockGemCompressed = new BlockGemCompressed(ConfigGwycraft.blockGemCompressedID, Material.rock).setLightValue(1F).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundMetalFootstep).setUnlocalizedName("blockGemCompressed").setCreativeTab(tabs);
         blockDyedStonePaver1 = new BlockDyedStonePaver1(ConfigGwycraft.blockDyedStonePaver1ID).setUnlocalizedName("Gwycraft:BlockDyedStonePaver1").setHardness(2.0F).setCreativeTab(tabs);
         blockDyedStonePaver2 = new BlockDyedStonePaver2(ConfigGwycraft.blockDyedStonePaver2ID).setUnlocalizedName("Gwycraft:BlockDyedStonePaver2").setHardness(2.0F).setCreativeTab(tabs);
         blockDyedStoneBrickSlab1 = (BlockDyedStoneBrickSlab1) new BlockDyedStoneBrickSlab1(ConfigGwycraft.blockDyedStoneBrickSlab1ID, false, Material.rock).setUnlocalizedName("Gwycraft:DyedStoneBrickSlab1").setHardness(2.0F).setCreativeTab(tabs);
         blockDyedStoneBrickDoubleSlab1 = (BlockDyedStoneBrickSlab1) new BlockDyedStoneBrickSlab1(ConfigGwycraft.blockDyedStoneBrickDoubleSlab1ID, true, Material.rock).setUnlocalizedName("Gwycraft:DyedStoneBrickDoubleSlab1").setHardness(2.0F);
         blockDyedStoneBrickSlab2 = (BlockDyedStoneBrickSlab2) new BlockDyedStoneBrickSlab2(ConfigGwycraft.blockDyedStoneBrickSlab2ID, false, Material.rock).setUnlocalizedName("Gwycraft:DyedStoneBrickSlab2").setHardness(2.0F).setCreativeTab(tabs);
         blockDyedStoneBrickDoubleSlab2 = (BlockDyedStoneBrickSlab2) new BlockDyedStoneBrickSlab2(ConfigGwycraft.blockDyedStoneBrickDoubleSlab2ID, true, Material.rock).setUnlocalizedName("Gwycraft:DyedStoneBrickDoubleSlab2").setHardness(2.0F);
         blockDyedFences = new BlockDyedFences(ConfigGwycraft.blockDyedFencesID, null , Material.wood).setCreativeTab(tabs);
         blockWhiteDyedTorch = new BlockDyedTorchWhite(ConfigGwycraft.blockWhiteDyedTorchID).setHardness(0.0F).setLightValue(0.9375F).setStepSound(Block.soundWoodFootstep).setUnlocalizedName("Gwycraft:torch_white").setCreativeTab(tabs);
         blockOrangeDyedTorch = new BlockDyedTorchOrange(ConfigGwycraft.blockOrangeDyedTorchID).setHardness(0.0F).setLightValue(0.9375F).setStepSound(Block.soundWoodFootstep).setUnlocalizedName("Gwycraft:torch_orange").setCreativeTab(tabs);
         blockMagentaDyedTorch = new BlockDyedTorchMagenta(ConfigGwycraft.blockMagentaDyedTorchID).setHardness(0.0F).setLightValue(0.9375F).setStepSound(Block.soundWoodFootstep).setUnlocalizedName("Gwycraft:torch_magenta").setCreativeTab(tabs);
         blockLBlueDyedTorch = new BlockDyedTorchLBlue(ConfigGwycraft.blockLBlueDyedTorchID).setHardness(0.0F).setLightValue(0.9375F).setStepSound(Block.soundWoodFootstep).setUnlocalizedName("Gwycraft:torch_lblue").setCreativeTab(tabs);
         blockYellowDyedTorch = new BlockDyedTorchYellow(ConfigGwycraft.blockYellowDyedTorchID).setHardness(0.0F).setLightValue(0.9375F).setStepSound(Block.soundWoodFootstep).setUnlocalizedName("Gwycraft:torch_yellow").setCreativeTab(tabs);
         blockLGreenDyedTorch = new BlockDyedTorchLGreen(ConfigGwycraft.blockLGreenDyedTorchID).setHardness(0.0F).setLightValue(0.9375F).setStepSound(Block.soundWoodFootstep).setUnlocalizedName("Gwycraft:torch_lgreen").setCreativeTab(tabs);
         blockPinkDyedTorch = new BlockDyedTorchPink(ConfigGwycraft.blockPinkDyedTorchID).setHardness(0.0F).setLightValue(0.9375F).setStepSound(Block.soundWoodFootstep).setUnlocalizedName("Gwycraft:torch_pink").setCreativeTab(tabs);
         blockDGrayDyedTorch = new BlockDyedTorchGray(ConfigGwycraft.blockDGrayDyedTorchID).setHardness(0.0F).setLightValue(0.9375F).setStepSound(Block.soundWoodFootstep).setUnlocalizedName("Gwycraft:torch_dgray").setCreativeTab(tabs);
         blockLGrayDyedTorch = new BlockDyedTorchLGray(ConfigGwycraft.blockLGrayDyedTorchID).setHardness(0.0F).setLightValue(0.9375F).setStepSound(Block.soundWoodFootstep).setUnlocalizedName("Gwycraft:torch_lgray").setCreativeTab(tabs);
         blockCyanDyedTorch = new BlockDyedTorchCyan(ConfigGwycraft.blockCyanDyedTorchID).setHardness(0.0F).setLightValue(0.9375F).setStepSound(Block.soundWoodFootstep).setUnlocalizedName("Gwycraft:torch_cyan").setCreativeTab(tabs);
         blockPurpleDyedTorch = new BlockDyedTorchPurple(ConfigGwycraft.blockPurpleDyedTorchID).setHardness(0.0F).setLightValue(0.9375F).setStepSound(Block.soundWoodFootstep).setUnlocalizedName("Gwycraft:torch_purple").setCreativeTab(tabs);
         blockBlueDyedTorch = new BlockDyedTorchBlue(ConfigGwycraft.blockBlueDyedTorchID).setHardness(0.0F).setLightValue(0.9375F).setStepSound(Block.soundWoodFootstep).setUnlocalizedName("Gwycraft:torch_blue").setCreativeTab(tabs);
         blockBrownDyedTorch = new BlockDyedTorchBrown(ConfigGwycraft.blockBrownDyedTorchID).setHardness(0.0F).setLightValue(0.9375F).setStepSound(Block.soundWoodFootstep).setUnlocalizedName("Gwycraft:torch_brown").setCreativeTab(tabs);
         blockGreenDyedTorch = new BlockDyedTorchGreen(ConfigGwycraft.blockGreenDyedTorchID).setHardness(0.0F).setLightValue(0.9375F).setStepSound(Block.soundWoodFootstep).setUnlocalizedName("Gwycraft:torch_green").setCreativeTab(tabs);
         blockRedDyedTorch = new BlockDyedTorchRed(ConfigGwycraft.blockRedDyedTorchID).setHardness(0.0F).setLightValue(0.9375F).setStepSound(Block.soundWoodFootstep).setUnlocalizedName("Gwycraft:torch_red").setCreativeTab(tabs);
         blockBlackDyedTorch = new BlockDyedTorchBlack(ConfigGwycraft.blockBlackDyedTorchID).setHardness(0.0F).setLightValue(0.9375F).setStepSound(Block.soundWoodFootstep).setUnlocalizedName("Gwycraft:torch_black").setCreativeTab(tabs);
 
         itemFlawedGems = new ItemEnchantedGems(ConfigGwycraft.itemFlawedGemsID).setUnlocalizedName("Gwycraft:itemFlawedGems").setCreativeTab(tabs);
         itemDyedClay = new ItemDyedClay(ConfigGwycraft.itemDyedClayID).setUnlocalizedName("Gwycraft:itemDyedClay").setCreativeTab(tabs);
         itemDyedClayBricks = new ItemDyedClayBricks(ConfigGwycraft.itemDyedClayBricksID).setUnlocalizedName("Gwycraft:itemDyedClayBricks").setCreativeTab(tabs);
         itemDyedMud = new ItemDyedMud(ConfigGwycraft.itemDyedMudID).setUnlocalizedName("Gwycraft:itemMud").setCreativeTab(tabs);
         itemDyedMudBricks = new ItemDyedMudBricks(ConfigGwycraft.itemDyedMudBricksID).setUnlocalizedName("Gwycraft:itemMudBricks").setCreativeTab(tabs);
         itemDyedSticks = new ItemDyedSticks(ConfigGwycraft.itemDyedSticksID).setUnlocalizedName("Gwycraft:itemDyedSticks").setCreativeTab(tabs);
         itemGemShears = new ItemGemShears(ConfigGwycraft.itemGemShearsID).setUnlocalizedName("Gwycraft:itemgemshears").setCreativeTab(tabs);
 
         CommonProxy.registerRenderers();
 
         //  GameRegistry Register Blocks
         GameRegistry.registerBlock(glowyWool, ItemGlowyWool.class, "glowyWool");
         GameRegistry.registerBlock(dyedStone, ItemDyedStone.class, "dyedStone");
         GameRegistry.registerBlock(glowydyedStone, ItemGwyGeneric.class, "glowydyedStone");
         GameRegistry.registerBlock(dyedStoneSlab1, "dyedStoneSlab1");
         GameRegistry.registerBlock(dyedStoneSlab2, "dyedStoneSlab2");
         GameRegistry.registerBlock(dyedBookcase, ItemDyedBookshelf.class, "dyedBookcase");
         GameRegistry.registerBlock(dyedPlank, ItemDyedPlank.class, "dyedPlank");
         GameRegistry.registerBlock(glowydyedPlank, ItemGwyGeneric.class, "glowydyedPlank");
         GameRegistry.registerBlock(dyedBrick, ItemDyedBrick.class, "dyedBrick");
         GameRegistry.registerBlock(glowydyedBrick, ItemGwyGeneric.class, "glowydyedBrick");
         GameRegistry.registerBlock(dyedClayblock, ItemDyedClayblock.class, "dyedClayblock");
         GameRegistry.registerBlock(glowydyedClayblock, ItemGwyGeneric.class, "glowydyedClayblock");
         GameRegistry.registerBlock(dyedGlass, ItemDyedGlass.class, "dyedGlass");
         GameRegistry.registerBlock(glowydyedGlass, ItemGlowyDyedGlass.class, "glowydyedGlass");
         GameRegistry.registerBlock(dyedLeaf, ItemDyedLeaf.class, "dyedLeaf");
         GameRegistry.registerBlock(dyedLog1, ItemDyedLog1.class, "dyedLog1");
         GameRegistry.registerBlock(dyedLog2, ItemDyedLog2.class, "dyedLog2");
         GameRegistry.registerBlock(dyedLog3, ItemDyedLog3.class, "dyedLog3");
         GameRegistry.registerBlock(dyedLog4, ItemDyedLog4.class, "dyedLog4");
         GameRegistry.registerBlock(blockDyedMudBrick, ItemDyedMudBrick.class, "blockDyedMudBrick");
         GameRegistry.registerBlock(glowyblockDyedMudBrick, ItemGwyGeneric.class, "glowyblockDyedMudBrick");
         GameRegistry.registerBlock(blockDyedMudBrickStairs, "blockDyedMudBrickStairs");
         GameRegistry.registerBlock(dyedSand, ItemDyedSand.class, "dyedSand");
         GameRegistry.registerBlock(glowydyedSand, ItemGwyGeneric.class, "glowydyedSand");
         GameRegistry.registerBlock(dyedStonebrick, ItemDyedStonebrick.class,"dyedStonebrick");
         GameRegistry.registerBlock(glowydyedStonebrick, ItemGwyGeneric.class, "glowydyedStonebrick");
         GameRegistry.registerBlock(dyedStonecobble, ItemDyedCobble.class, "dyedStonecobble");
         GameRegistry.registerBlock(glowydyedStonecobble, ItemGwyGeneric.class, "glowydyedStonecobble");
         GameRegistry.registerBlock(blockGemCompressed, ItemGemCompressed.class, "blockGemCompressed");
         GameRegistry.registerBlock(blockGemOre, ItemGemOre.class, "blockGemOre");
         GameRegistry.registerBlock(blockDyedStonePaver1, ItemDyedStonePaver1.class, "blockDyedStonePaver1");
         GameRegistry.registerBlock(blockDyedStonePaver2, ItemDyedStonePaver2.class, "blockDyedStonePaver2");
         GameRegistry.registerBlock(blockDyedStoneBrickSlab1, "blockDyedStoneBrickSlab1");
         GameRegistry.registerBlock(blockDyedStoneBrickSlab2, "blockDyedStoneBrickSlab2");
         GameRegistry.registerBlock(blockDyedFences, ItemDyedFences.class, "blockDyedFences");
         GameRegistry.registerBlock(blockDyedCobbleWalls, ItemDyedCobbleWalls.class, "blockDyedCobbleWalls");
         GameRegistry.registerBlock(blockWhiteDyedTorch, "blockWhiteDyedTorch");
         GameRegistry.registerBlock(blockOrangeDyedTorch, "blockOrangeDyedTorch");
         GameRegistry.registerBlock(blockMagentaDyedTorch, "blockMagentaDyedTorch");
         GameRegistry.registerBlock(blockLBlueDyedTorch, "blockLBlueDyedTorch");
         GameRegistry.registerBlock(blockYellowDyedTorch, "blockYellowDyedTorch");
         GameRegistry.registerBlock(blockLGreenDyedTorch, "blockLGreenDyedTorch");
         GameRegistry.registerBlock(blockPinkDyedTorch, "blockPinkDyedTorch");
         GameRegistry.registerBlock(blockDGrayDyedTorch, "blockDGrayDyedTorch");
         GameRegistry.registerBlock(blockLGrayDyedTorch, "blockLGrayDyedTorch");
         GameRegistry.registerBlock(blockCyanDyedTorch, "blockCyanDyedTorch");
         GameRegistry.registerBlock(blockPurpleDyedTorch, "blockPurpleDyedTorch");
         GameRegistry.registerBlock(blockBlueDyedTorch, "blockBlueDyedTorch");
         GameRegistry.registerBlock(blockBrownDyedTorch, "blockBrownDyedTorch");
         GameRegistry.registerBlock(blockGreenDyedTorch, "blockGreenDyedTorch");
         GameRegistry.registerBlock(blockRedDyedTorch, "blockRedDyedTorch");
         GameRegistry.registerBlock(blockBlackDyedTorch, "blockBlackDyedTorch");
 
         GameRegistry.registerItem(itemFlawedGems, "itemFlawedGems", null);
         GameRegistry.registerItem(itemDyedClay, "itemDyedClay", null);
         GameRegistry.registerItem(itemDyedClayBricks, "itemDyedClayBricks", null);
         GameRegistry.registerItem(itemDyedMud, "itemDyedMud", null)   ;
         GameRegistry.registerItem(itemDyedMudBricks, "itemDyedMudBricks", null);
         GameRegistry.registerItem(itemDyedSticks, "itemDyedSticks", null);
         GameRegistry.registerItem(itemGemShears, "itemGemShears", null);
         
         LanguageRegistry.addName(blockWhiteDyedTorch, "White Torch");
         LanguageRegistry.addName(blockOrangeDyedTorch, "Orange Torch");
         LanguageRegistry.addName(blockMagentaDyedTorch, "Magenta Torch");
         LanguageRegistry.addName(blockLBlueDyedTorch, "Light Blue Torch");
         LanguageRegistry.addName(blockYellowDyedTorch, "Yellow Torch");
         LanguageRegistry.addName(blockLGreenDyedTorch, "Light Green Torch");
         LanguageRegistry.addName(blockPinkDyedTorch, "Pink Torch");
         LanguageRegistry.addName(blockDGrayDyedTorch, "Gray Torch");
         LanguageRegistry.addName(blockLGrayDyedTorch, "Light Gray Torch");
         LanguageRegistry.addName(blockCyanDyedTorch, "Cyan Torch");
         LanguageRegistry.addName(blockPurpleDyedTorch, "Purple Torch");
         LanguageRegistry.addName(blockBlueDyedTorch, "Blue Torch");
         LanguageRegistry.addName(blockBrownDyedTorch, "Brown Torch");
         LanguageRegistry.addName(blockGreenDyedTorch, "Green Torch");
         LanguageRegistry.addName(blockRedDyedTorch, "Red Torch");
         LanguageRegistry.addName(blockBlackDyedTorch, "Black Torch");
         LanguageRegistry.addName(itemGemShears, "Gem Shears");
                 
         Item.itemsList[ConfigGwycraft.dyedStoneSlab1ID] = (new ItemSlab (ConfigGwycraft.dyedStoneSlab1ID - 256, dyedStoneSlab1, dyedStoneDoubleSlab1, false)).setUnlocalizedName("blockDyedStoneSlab1");
         Item.itemsList[ConfigGwycraft.dyedStoneSlab2ID] = (new ItemSlab (ConfigGwycraft.dyedStoneSlab2ID - 256, dyedStoneSlab2, dyedStoneDoubleSlab2, false)).setUnlocalizedName("blockDyedStoneSlab2");
         Item.itemsList[ConfigGwycraft.itemFlawedGemsID] = (new ItemEnchantedGems (ConfigGwycraft.itemFlawedGemsID - 256)).setUnlocalizedName("Gwycraft:itemFlawedGems");
         Item.itemsList[ConfigGwycraft.itemDyedClayID] = (new ItemDyedClay (ConfigGwycraft.itemDyedClayID - 256)).setUnlocalizedName("itemDyedClay");
         Item.itemsList[ConfigGwycraft.itemDyedClayBricksID] = (new ItemDyedClayBricks (ConfigGwycraft.itemDyedClayBricksID - 256)).setUnlocalizedName("itemDyedClayBricks");
         Item.itemsList[ConfigGwycraft.itemDyedMudID] = (new ItemDyedMud (ConfigGwycraft.itemDyedMudID - 256)).setUnlocalizedName("itemDyedMud");
         Item.itemsList[ConfigGwycraft.itemDyedMudBricksID] = (new ItemDyedMudBricks (ConfigGwycraft.itemDyedMudBricksID - 256)).setUnlocalizedName("itemDyedMudBricks");
         Item.itemsList[ConfigGwycraft.blockDyedStoneBrickSlab1ID] = (new ItemSlab (ConfigGwycraft.blockDyedStoneBrickSlab1ID - 256, blockDyedStoneBrickSlab1, blockDyedStoneBrickDoubleSlab1, false)).setUnlocalizedName("blockDyedStoneBrickSlab1");
         Item.itemsList[ConfigGwycraft.blockDyedStoneBrickSlab2ID] = (new ItemSlab (ConfigGwycraft.blockDyedStoneBrickSlab2ID - 256, blockDyedStoneBrickSlab2, blockDyedStoneBrickDoubleSlab2, false)).setUnlocalizedName("blockDyedStoneBrickSlab2");
         Item.itemsList[ConfigGwycraft.itemDyedSticksID] = (new ItemDyedSticks (ConfigGwycraft.itemDyedSticksID - 256)).setUnlocalizedName("itemDyedSticks");
         Item.itemsList[ConfigGwycraft.itemGemShearsID] = (new ItemGemShears (ConfigGwycraft.itemGemShearsID - 256)).setUnlocalizedName("Gwycraft:itemgemshears");
 
         // Language Registry
         // 16 Meta
         for (int i = 0; i < 16; i++) {
         	// Dye is inverted compared to wool
         	ItemStack dye = new ItemStack(Item.dyePowder, 1, 15 - i);
         	
 			ItemStack cloth = new ItemStack(Block.cloth, 1, i);
 			ItemStack glowyWoolStack = new ItemStack(glowyWool, 1, i);
             ItemStack dyedStoneStack = new ItemStack(dyedStone, 1, i);
             ItemStack glowydyedStoneStack = new ItemStack(glowydyedStone, 1, i);
             ItemStack dyedBookcaseStack = new ItemStack(dyedBookcase, 1, i);
             ItemStack dyedPlankStack = new ItemStack(dyedPlank, 1, i);
             ItemStack glowydyedPlankStack = new ItemStack(glowydyedPlank, 1, i);
             ItemStack dyedBrickStack = new ItemStack(dyedBrick, 1, i);
             ItemStack glowydyedBrickStack = new ItemStack(glowydyedBrick, 1, i);
             ItemStack dyedClayblockStack = new ItemStack(dyedClayblock, 1, i);
             ItemStack glowydyedClayblockStack = new ItemStack(glowydyedClayblock, 1, i);
             ItemStack dyedGlassStack = new ItemStack(dyedGlass, 1, i);
             ItemStack glowydyedGlassStack = new ItemStack(glowydyedGlass, 1, i);
             ItemStack dyedLeafStack = new ItemStack(dyedLeaf, 1, i);
             ItemStack dyedMudbrickStack = new ItemStack(blockDyedMudBrick, 1, i);
             ItemStack glowydyedMudbrickStack = new ItemStack(glowyblockDyedMudBrick, 1, i);
             ItemStack dyedSandStack = new ItemStack(dyedSand, 1, i);
             ItemStack glowydyedSandStack = new ItemStack(glowydyedSand, 1, i);
             ItemStack dyedStonebrickStack = new ItemStack(dyedStonebrick, 1, i);
             ItemStack glowydyedStonebrickStack = new ItemStack(glowydyedStonebrick, 1, i);
             ItemStack dyedStonecobbleStack = new ItemStack(dyedStonecobble, 1, i);
             ItemStack glowydyedStonecobbleStack = new ItemStack(glowydyedStonecobble, 1, i);
             ItemStack blockGemCompressedStack = new ItemStack(blockGemCompressed, 1, i);
             ItemStack blockGemOreStack = new ItemStack(blockGemOre, 1, i);
             ItemStack itemFlawedGemsStack = new ItemStack(itemFlawedGems, 1, i);
             ItemStack itemDyedClayStack = new ItemStack(itemDyedClay, 1, i);
             ItemStack itemDyedClayBricksStack = new ItemStack(itemDyedClayBricks, 1, i);
             ItemStack itemDyedMudStack = new ItemStack(itemDyedMud, 1, i);
             ItemStack itemDyedMudBricksStack = new ItemStack(itemDyedMudBricks, 1, i);
             ItemStack blockDyedFencesStack = new ItemStack(blockDyedFences, 1, i);
             ItemStack blockDyedCobbleWallsStack = new ItemStack(blockDyedCobbleWalls, 1, i);
             ItemStack itemDyedSticksStack = new ItemStack(itemDyedSticks, 1, i);
             
             // Language Registry
 			LanguageRegistry.addName(glowyWoolStack, "Glowy " + gwyColorNames[glowyWoolStack.getItemDamage()] + " Wool");
 			LanguageRegistry.addName(dyedStoneStack, gwyColorNames[dyedStoneStack.getItemDamage()] + " Stone");
             LanguageRegistry.addName(glowydyedStoneStack, "Glowy " + gwyColorNames[glowydyedStoneStack.getItemDamage()] + " Stone");
 			LanguageRegistry.addName(dyedBookcaseStack, gwyColorNames[dyedBookcaseStack.getItemDamage()] + " Bookcase");
             LanguageRegistry.addName(dyedPlankStack, gwyColorNames[dyedPlankStack.getItemDamage()] + " Plank");
             LanguageRegistry.addName(glowydyedPlankStack, "Glowy " + gwyColorNames[glowydyedPlankStack.getItemDamage()] + " Plank");
             LanguageRegistry.addName(dyedBrickStack, gwyColorNames[dyedBrickStack.getItemDamage()] + " Brick");
             LanguageRegistry.addName(glowydyedBrickStack, "Glowy " + gwyColorNames[glowydyedBrickStack.getItemDamage()] + " Brick");
 			LanguageRegistry.addName(dyedClayblockStack, gwyColorNames[dyedClayblockStack.getItemDamage()] + " Clay Block");
 			LanguageRegistry.addName(glowydyedClayblockStack, "Glowy " + gwyColorNames[glowydyedClayblockStack.getItemDamage()] + " Clay Block");
 			LanguageRegistry.addName(dyedGlassStack, gwyColorNames[dyedGlassStack.getItemDamage()] + " Glass");
             LanguageRegistry.addName(glowydyedGlassStack, gwyColorNames[glowydyedGlassStack.getItemDamage()] + " Glass");
 			LanguageRegistry.addName(dyedLeafStack, gwyColorNames[dyedLeafStack.getItemDamage()] + " Leaf");
             LanguageRegistry.addName(dyedMudbrickStack, gwyColorNames[dyedMudbrickStack.getItemDamage()] + " Mud Brick Block");
             LanguageRegistry.addName(glowydyedMudbrickStack, "Glowy " + gwyColorNames[glowydyedMudbrickStack.getItemDamage()] + " Mud Brick Block");
             LanguageRegistry.addName(dyedSandStack, gwyColorNames[dyedSandStack.getItemDamage()] + " Sand");
             LanguageRegistry.addName(glowydyedSandStack, "Glowy " + gwyColorNames[glowydyedSandStack.getItemDamage()] + " Sand");
             LanguageRegistry.addName(dyedStonebrickStack, gwyColorNames[dyedStonebrickStack.getItemDamage()] + " Stone Brick");
             LanguageRegistry.addName(glowydyedStonebrickStack, "Glowy " + gwyColorNames[glowydyedStonebrickStack.getItemDamage()] + " Stone Brick");
             LanguageRegistry.addName(dyedStonecobbleStack, gwyColorNames[dyedStonecobbleStack.getItemDamage()] + " Cobblestone");
             LanguageRegistry.addName(glowydyedStonecobbleStack, "Glowy " + gwyColorNames[glowydyedStonecobbleStack.getItemDamage()] + " Cobblestone");
             LanguageRegistry.addName(blockGemCompressedStack, gwyGemNames[blockGemCompressedStack.getItemDamage()] + " Block");
             LanguageRegistry.addName(blockGemOreStack, gwyGemNames[blockGemOreStack.getItemDamage()] + " Ore");
             LanguageRegistry.addName(itemFlawedGemsStack, gwyGemNames[i]);
             LanguageRegistry.addName(itemDyedClayStack, gwyColorNames[i] + " Clay");
             LanguageRegistry.addName(itemDyedClayBricksStack, gwyColorNames[i] + " Clay Bricks");
             LanguageRegistry.addName(itemDyedMudStack, gwyColorNames[i] + " Mud");
             LanguageRegistry.addName(itemDyedMudBricksStack, gwyColorNames[i] + " Mud Bricks");
             LanguageRegistry.addName(blockDyedFencesStack, gwyColorNames[i] + " Fence");
             LanguageRegistry.addName(blockDyedCobbleWallsStack, gwyColorNames[i] + " Cobble Walls");
             LanguageRegistry.addName(itemDyedSticksStack, gwyColorNames[i] + " Stick");
 
             //  Add Recipes
             GameRegistry.addRecipe(new ItemStack(dyedBookcase, 1, i), "###", "XXX", "###", '#', dyedPlankStack, 'X', Item.book);
             GameRegistry.addRecipe(new ItemStack(itemDyedSticks, 4, i), "X", "X", 'X', dyedPlankStack);
             GameRegistry.addRecipe(new ItemStack(blockGemCompressed, 1, i), "XX", "XX", 'X', itemFlawedGemsStack);
             GameRegistry.addRecipe(new ItemStack(dyedStonebrick, 4, i), "xx", "xx", 'x', dyedStoneStack);
             GameRegistry.addRecipe(new ItemStack(blockDyedFences, 2, i), "XXX", "XXX", 'X', itemDyedSticksStack);
             GameRegistry.addShapelessRecipe(glowyWoolStack, new ItemStack(Block.glowStone), cloth);
             GameRegistry.addShapelessRecipe(new ItemStack(dyedSand, 8, i), dye, Block.sand, Block.sand, Block.sand, Block.sand, Block.sand, Block.sand, Block.sand, Block.sand);
             GameRegistry.addShapelessRecipe(new ItemStack(glowydyedSand, 8, i), itemFlawedGemsStack, Block.sand, Block.sand, Block.sand, Block.sand, Block.sand, Block.sand, Block.sand, Block.sand);
             GameRegistry.addShapelessRecipe(new ItemStack(dyedStonecobble, 8, i), dye, Block.cobblestone, Block.cobblestone, Block.cobblestone, Block.cobblestone, Block.cobblestone, Block.cobblestone, Block.cobblestone, Block.cobblestone);
             GameRegistry.addShapelessRecipe(new ItemStack(glowydyedStonecobble, 8, i), itemFlawedGemsStack, Block.cobblestone, Block.cobblestone, Block.cobblestone, Block.cobblestone, Block.cobblestone, Block.cobblestone, Block.cobblestone, Block.cobblestone);
             GameRegistry.addShapelessRecipe(new ItemStack(dyedPlank, 8, i), dye, Block.planks, Block.planks, Block.planks, Block.planks, Block.planks, Block.planks, Block.planks, Block.planks);
             GameRegistry.addShapelessRecipe(new ItemStack(glowydyedPlank, 8, i), itemFlawedGemsStack, Block.planks, Block.planks, Block.planks, Block.planks, Block.planks, Block.planks, Block.planks, Block.planks);
             GameRegistry.addShapelessRecipe(new ItemStack(Item.dyePowder, 8, 15-i), itemFlawedGemsStack, Item.redstone, Item.glassBottle );
             FurnaceRecipes.smelting().addSmelting(dyedStonecobble.blockID, i, new ItemStack(dyedStone, 1, i ), 0.1F);
             FurnaceRecipes.smelting().addSmelting(glowydyedStonecobble.blockID, i, new ItemStack(glowydyedStone, 1, i ), 0.1F);
             FurnaceRecipes.smelting().addSmelting(dyedSand.blockID, i, new ItemStack(dyedGlass, 1, i ), 0.1F);
             FurnaceRecipes.smelting().addSmelting(glowydyedSand.blockID, i, new ItemStack(glowydyedGlass, 1, i ), 0.1F);
             GameRegistry.addRecipe(new ItemStack(itemGemShears, 1, 0), " X", "X ", 'X', itemFlawedGemsStack);
 
             // Recipes for Dyed Torches
             if ( i == 0)
                 GameRegistry.addRecipe(new ItemStack(blockWhiteDyedTorch, 4), "X", "#", 'X', Item.coal, '#', itemDyedSticksStack);
             else if ( i == 1)
                 GameRegistry.addRecipe(new ItemStack(blockOrangeDyedTorch, 4), "X", "#", 'X', Item.coal, '#', itemDyedSticksStack);
             else if ( i == 2)
                 GameRegistry.addRecipe(new ItemStack(blockMagentaDyedTorch, 4), "X", "#", 'X', Item.coal, '#', itemDyedSticksStack);
             else if ( i == 3)
                 GameRegistry.addRecipe(new ItemStack(blockLBlueDyedTorch, 4), "X", "#", 'X', Item.coal, '#', itemDyedSticksStack);
             else if ( i == 4)
                 GameRegistry.addRecipe(new ItemStack(blockYellowDyedTorch, 4), "X", "#", 'X', Item.coal, '#', itemDyedSticksStack);
             else if ( i == 5)
                 GameRegistry.addRecipe(new ItemStack(blockLGreenDyedTorch, 4), "X", "#", 'X', Item.coal, '#', itemDyedSticksStack);
             else if ( i == 6)
                 GameRegistry.addRecipe(new ItemStack(blockPinkDyedTorch, 4), "X", "#", 'X', Item.coal, '#', itemDyedSticksStack);
             else if ( i == 7)
                 GameRegistry.addRecipe(new ItemStack(blockDGrayDyedTorch, 4), "X", "#", 'X', Item.coal, '#', itemDyedSticksStack);
             else if ( i == 8)
                 GameRegistry.addRecipe(new ItemStack(blockLGrayDyedTorch, 4), "X", "#", 'X', Item.coal, '#', itemDyedSticksStack);
             else if ( i == 9)
                 GameRegistry.addRecipe(new ItemStack(blockCyanDyedTorch, 4), "X", "#", 'X', Item.coal, '#', itemDyedSticksStack);
             else if ( i == 10)
                 GameRegistry.addRecipe(new ItemStack(blockPurpleDyedTorch, 4), "X", "#", 'X', Item.coal, '#', itemDyedSticksStack);
             else if ( i == 11)
                 GameRegistry.addRecipe(new ItemStack(blockBlueDyedTorch, 4), "X", "#", 'X', Item.coal, '#', itemDyedSticksStack);
             else if ( i == 12)
                 GameRegistry.addRecipe(new ItemStack(blockBrownDyedTorch, 4), "X", "#", 'X', Item.coal, '#', itemDyedSticksStack);
             else if ( i == 13)
                 GameRegistry.addRecipe(new ItemStack(blockGreenDyedTorch, 4), "X", "#", 'X', Item.coal, '#', itemDyedSticksStack);
             else if ( i ==14)
                 GameRegistry.addRecipe(new ItemStack(blockRedDyedTorch, 4), "X", "#", 'X', Item.coal, '#', itemDyedSticksStack);
             GameRegistry.addRecipe(new ItemStack(blockBlackDyedTorch, 4), "X", "#", 'X', Item.coal, '#', itemDyedSticksStack);
         }
         
         // 8 Meta
         for (int i = 0; i < 8; i++) {
 
             // Item Stacks
             ItemStack dyedStoneSlab1Stack = new ItemStack(dyedStoneSlab1, 1, i);
             ItemStack dyedStoneSlab2Stack = new ItemStack(dyedStoneSlab2, 1, i);
             ItemStack dyedStone1Stack = new ItemStack(dyedStone, 8, i);
             ItemStack dyedStone2Stack = new ItemStack(dyedStone, 8, i+8);
             ItemStack blockDyedStonePaver2Stack = new ItemStack(blockDyedStonePaver2, 1, i);
             ItemStack blockDyedStoneBrickSlab1Stack = new ItemStack(blockDyedStoneBrickSlab1, 1, i);
             ItemStack blockDyedStoneBrickSlab2Stack = new ItemStack(blockDyedStoneBrickSlab2, 1, i);
             ItemStack blockDyedStonePaver1Stack = new ItemStack(blockDyedStonePaver1, 1, i);
 
             // Language Registry
             LanguageRegistry.addName(dyedStoneSlab1Stack, gwyColorSlab1Names[dyedStoneSlab1Stack.getItemDamage()] + " Stone Slab");
             LanguageRegistry.addName(dyedStoneSlab2Stack, gwyColorSlab2Names[dyedStoneSlab2Stack.getItemDamage()] + " Stone Slab");
             LanguageRegistry.addName(blockDyedStonePaver1Stack, gwyColorSlab1Names[blockDyedStonePaver1Stack.getItemDamage()] + " Stone Paver");
             LanguageRegistry.addName(blockDyedStonePaver2Stack, gwyColorSlab2Names[blockDyedStonePaver2Stack.getItemDamage()] + " Stone Paver2");
             LanguageRegistry.addName(blockDyedStoneBrickSlab1Stack, gwyColorSlab1Names[blockDyedStoneBrickSlab1Stack.getItemDamage()] + " Stone Brick Slab");
             LanguageRegistry.addName(blockDyedStoneBrickSlab2Stack, gwyColorSlab2Names[blockDyedStoneBrickSlab2Stack.getItemDamage()] + " Stone Brick Slab");
 
             //  Add Recipes
             GameRegistry.addRecipe(new ItemStack(dyedStoneSlab1, 6, i), "XXX", 'X', dyedStone1Stack);
             GameRegistry.addRecipe(new ItemStack(dyedStoneSlab2, 6, i), "XXX", 'X', dyedStone2Stack);
             GameRegistry.addRecipe(new ItemStack(blockDyedStonePaver1, 12, i), "XXX", 'X', dyedStoneSlab1Stack);
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
         // Recipes
 
     }
 	
     @Init 
     public void init(FMLInitializationEvent event) {
         GameRegistry.registerWorldGenerator(worldGen);   // Add this in your @Init method. If you haven't already, import cpw.mods.fml.common.registry.GameRegistry.      
     }
 	
     @PostInit
 	public void postInit(FMLPostInitializationEvent event) {
 		// Stub Method
 	}
 }
