 package gwydion0917.gwycraft;
 
 import gwydion0917.gwycraft.blocks.BlockDyedBookcase;
 import gwydion0917.gwycraft.blocks.BlockDyedCobbleWalls;
 import gwydion0917.gwycraft.blocks.BlockDyedFences;
 import gwydion0917.gwycraft.blocks.BlockDyedGlass;
 import gwydion0917.gwycraft.blocks.BlockDyedLeaf;
 import gwydion0917.gwycraft.blocks.BlockDyedLog1;
 import gwydion0917.gwycraft.blocks.BlockDyedLog2;
 import gwydion0917.gwycraft.blocks.BlockDyedLog3;
 import gwydion0917.gwycraft.blocks.BlockDyedLog4;
 import gwydion0917.gwycraft.blocks.BlockDyedSand;
 import gwydion0917.gwycraft.blocks.BlockDyedSandstone;
 import gwydion0917.gwycraft.blocks.BlockDyedTorch;
 import gwydion0917.gwycraft.blocks.BlockGemCompressed;
 import gwydion0917.gwycraft.blocks.BlockGemOre;
 import gwydion0917.gwycraft.blocks.BlockGlowyWool;
 import gwydion0917.gwycraft.blocks.BlockGwyGenericBlock;
 import gwydion0917.gwycraft.blocks.BlockGwyGenericPaver;
 import gwydion0917.gwycraft.blocks.ItemDyedBookshelf;
 import gwydion0917.gwycraft.blocks.ItemDyedCobbleWalls;
 import gwydion0917.gwycraft.blocks.ItemDyedFences;
 import gwydion0917.gwycraft.blocks.ItemDyedGlass;
 import gwydion0917.gwycraft.blocks.ItemDyedLeaf;
 import gwydion0917.gwycraft.blocks.ItemDyedLog1;
 import gwydion0917.gwycraft.blocks.ItemDyedLog2;
 import gwydion0917.gwycraft.blocks.ItemDyedLog3;
 import gwydion0917.gwycraft.blocks.ItemDyedLog4;
 import gwydion0917.gwycraft.blocks.ItemDyedPlank;
 import gwydion0917.gwycraft.blocks.ItemGemCompressed;
 import gwydion0917.gwycraft.blocks.ItemGemOre;
 import gwydion0917.gwycraft.blocks.ItemGlowyDyedGlass;
 import gwydion0917.gwycraft.blocks.ItemGlowyWool;
 import gwydion0917.gwycraft.blocks.ItemGwyGeneric;
 import gwydion0917.gwycraft.items.ItemDyedClay;
 import gwydion0917.gwycraft.items.ItemDyedClayBricks;
 import gwydion0917.gwycraft.items.ItemDyedMud;
 import gwydion0917.gwycraft.items.ItemDyedMudBricks;
 import gwydion0917.gwycraft.items.ItemDyedSticks;
 import gwydion0917.gwycraft.items.ItemEnchantedGems;
 import gwydion0917.gwycraft.items.ItemGemHatchet;
 import gwydion0917.gwycraft.items.ItemGemHoe;
 import gwydion0917.gwycraft.items.ItemGemPickaxe;
 import gwydion0917.gwycraft.items.ItemGemShears;
 import gwydion0917.gwycraft.items.ItemGemShovel;
 import gwydion0917.gwycraft.items.ItemGemSword;
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockBookshelf;
 import net.minecraft.block.material.Material;
 import net.minecraft.enchantment.Enchantment;
 import net.minecraft.init.Blocks;
 import net.minecraft.init.Items;
 import net.minecraft.item.Item;
 import net.minecraft.item.Item.ToolMaterial;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.util.EnumHelper;
 import net.minecraftforge.oredict.OreDictionary;
 import net.minecraftforge.oredict.ShapedOreRecipe;
 import net.minecraftforge.oredict.ShapelessOreRecipe;
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.registry.GameRegistry;
 
 
@Mod(modid = "gwycraft", name = "GwyCraft", version = "0.1.7b", guiFactory = "gwydion0917.gwycraft.client.GwyCraftGuiFactory", dependencies = "required-after:Forge@[10.12.2.1147]")
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
     public static Block blockDyedStone;
     public static Block glowyblockDyedStone;
     public static BlockBookshelf blockDyedBookcase;
     public static BlockBookshelf glowyblockDyedBookcase;
     public static Block blockDyedBrick;
     public static Block glowyblockDyedBrick;
     public static Block blockDyedClayblock;
     public static Block glowyblockDyedClayblock;
     public static Block blockDyedGlass;
     public static Block glowyblockDyedGlass;
     public static Block blockDyedLeaf;
     public static Block glowyblockDyedLeaf;
     public static Block blockDyedLog1;
     public static Block blockDyedLog2;
     public static Block blockDyedLog3;
     public static Block blockDyedLog4;
     public static Block glowyblockDyedLog1;
     public static Block glowyblockDyedLog2;
     public static Block glowyblockDyedLog3;
     public static Block glowyblockDyedLog4;
     public static Block blockDyedMudBrick;
     public static Block glowyblockDyedMudBrick;
     public static Block blockDyedMudBrickPaver;
     public static Block blockDyedPlank;
     public static Block glowyblockDyedPlank;
     public static Block blockDyedSand;
     public static Block glowyblockDyedSand;
     public static Block blockDyedSandstone;
     public static Block glowyblockDyedSandstone;
     public static Block blockDyedStoneBrick;
     public static Block glowyblockDyedStoneBrick;    
     public static Block blockDyedStoneCobble;
     public static Block glowyblockDyedStoneCobble;
     public static Block blockDyedStoneCobblePaver;
     public static Block blockGemOre;
     public static Block blockGemCompressed;
     public static Block blockDyedStonePaver;
     public static Block blockDyedBrickPaver;
     public static Block blockDyedStoneBrickPaver;
     public static Block blockDyedFences;
     public static Block glowyblockDyedFences;
     public static Block blockDyedCobbleWalls;
     public static Block glowyblockDyedCobbleWalls;
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
     
     public static Item itemEnchantedGems;
     public static Item itemDyedClay;
     public static Item itemDyedClayBricks;
     public static Item itemDyedMud;
     public static Item itemDyedMudBricks;
     public static Item itemDyedSticks;
     public static Item itemGemShears;
     public static Item itemGemHatchet;
     public static Item itemGemHoe;
     public static Item itemGemPickaxe;
     public static Item itemGemShovel;
     public static Item itemGemSword;
     
     public static ToolMaterial GWYCRAFT_MATERIAL = EnumHelper.addToolMaterial("GWYCRAFT_MATERIAL", 2, 500, 8.0F, 2.0F, 22);
     
 	@Instance("Gwycraft")
 	public static Gwycraft instance;
 
 	@SidedProxy(clientSide = "gwydion0917.gwycraft.client.ClientProxy", serverSide = "gwydion0917.gwycraft.CommonProxy")
 	public static CommonProxy proxy;
 
 	@EventHandler
 	public void preInit(FMLPreInitializationEvent event) {
 		FMLCommonHandler.instance().bus().register( new ConfigGwycraft());
         ConfigGwycraft.initConfig(event);
         
 // Wool        
 		glowyWool = new BlockGlowyWool(Material.cloth).setHardness(0.8F).setStepSound(Block.soundTypeCloth).setBlockName("gwycraft.glowyWool").setLightLevel(1f).setCreativeTab(tabs);
 // Stone
 		blockDyedStone = new BlockGwyGenericBlock(Material.rock).setHardness(1.5F).setResistance(10.0F).setStepSound(Block.soundTypeStone).setBlockName("gwycraft.blockDyedStone").setCreativeTab(tabs).setBlockTextureName("gwycraft:stone");
         glowyblockDyedStone = new BlockGwyGenericBlock(Material.rock).setLightLevel(1F).setHardness(1.5F).setResistance(10.0F).setStepSound(Block.soundTypeStone).setBlockName("gwycraft.glowyblockDyedStone").setCreativeTab(tabs).setBlockTextureName("gwycraft:stone");
         blockDyedStonePaver = new BlockGwyGenericPaver().setBlockName("gwycraft.blockDyedStonePaver").setHardness(2.0F).setCreativeTab(tabs).setBlockTextureName("gwycraft:stone");
 // Bookshelves
         blockDyedBookcase = (BlockBookshelf) new BlockDyedBookcase(Material.wood).setHardness(1.5F).setStepSound(Block.soundTypeWood).setBlockName("gwycraft.blockDyedBookcase").setCreativeTab(tabs);
         glowyblockDyedBookcase = (BlockBookshelf) new BlockDyedBookcase(Material.wood).setLightLevel(1F).setHardness(1.5F).setStepSound(Block.soundTypeWood).setBlockName("gwycraft.glowyblockDyedBookcase").setCreativeTab(tabs);
 // Brick
         blockDyedBrick = new BlockGwyGenericBlock(Material.rock).setHardness(2F).setStepSound(Block.soundTypeStone).setResistance(10F).setBlockName("gwycraft.blockDyedBrick").setCreativeTab(tabs).setBlockTextureName("gwycraft:brick");
 		glowyblockDyedBrick = new BlockGwyGenericBlock(Material.rock).setLightLevel(1F).setHardness(2F).setStepSound(Block.soundTypeStone).setResistance(10F).setBlockName("gwycraft.glowyblockDyedBrick").setCreativeTab(tabs).setBlockTextureName("gwycraft:brick");
         blockDyedBrickPaver = new BlockGwyGenericPaver().setBlockName("gwycraft.blockDyedBrickPaver").setHardness(2.0F).setCreativeTab(tabs).setBlockTextureName("gwycraft:brick");
 // Clay
 		blockDyedClayblock = new BlockGwyGenericBlock(Material.clay).setHardness(0.6F).setStepSound(Block.soundTypeStone).setBlockName("gwycraft.blockDyedClayblock").setCreativeTab(tabs).setBlockTextureName("gwycraft:clayblock");
 		glowyblockDyedClayblock = new BlockGwyGenericBlock(Material.clay).setLightLevel(1F).setHardness(0.6F).setStepSound(Block.soundTypeStone).setBlockName("gwycraft.glowyblockDyedClayblock").setCreativeTab(tabs).setBlockTextureName("gwycraft:clayblock");
 // Glass
 		blockDyedGlass = new BlockDyedGlass().setHardness(0.6F).setStepSound(Block.soundTypeStone).setBlockName("gwycraft.blockDyedGlass").setCreativeTab(tabs).setBlockTextureName("gwycraft:glass");
         glowyblockDyedGlass = new BlockDyedGlass().setLightLevel(1F).setHardness(0.6F).setStepSound(Block.soundTypeStone).setBlockName("gwycraft.glowyblockDyedGlass").setCreativeTab(tabs).setBlockTextureName("gwycraft:glass");
 // Leaf
         blockDyedLeaf = new BlockDyedLeaf().setCreativeTab(tabs).setBlockName("gwycraft.blockDyedLeaf");
         glowyblockDyedLeaf = new BlockDyedLeaf().setCreativeTab(tabs).setLightLevel(1F).setBlockName("gwycraft.glowyblockDyedLeaf");
 // Mud Brick
         blockDyedMudBrick = new BlockGwyGenericBlock(Material.rock).setHardness(1.5f).setStepSound(Block.soundTypeStone).setBlockName("gwycraft.blockDyedMudBrick").setCreativeTab(tabs).setBlockTextureName("gwycraft:mudbrick");
 		glowyblockDyedMudBrick = new BlockGwyGenericBlock(Material.rock).setLightLevel(1F).setHardness(1.5f).setStepSound(Block.soundTypeStone).setBlockName("gwycraft.glowyblockDyedMudBrick").setCreativeTab(tabs).setBlockTextureName("gwycraft:mudbrick");
         blockDyedMudBrickPaver = new BlockGwyGenericPaver().setBlockName("gwycraft.blockDyedMudBrickPaver").setHardness(2.0F).setCreativeTab(tabs).setBlockTextureName("gwycraft:mudbrick");
 // Planks
 		blockDyedPlank = new BlockGwyGenericBlock(Material.wood).setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundTypeWood).setBlockName("gwycraft.blockDyedPlank").setCreativeTab(tabs).setBlockTextureName("gwycraft:plank");
 		glowyblockDyedPlank = new BlockGwyGenericBlock(Material.wood).setLightLevel(1F).setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundTypeWood).setBlockName("gwycraft.glowyblockDyedPlank").setCreativeTab(tabs).setBlockTextureName("gwycraft:plank");
         blockDyedFences = new BlockDyedFences("blockDyedFence" , Material.wood).setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundTypeWood).setBlockName("gwycraft.blockDyedFence").setCreativeTab(tabs);
         glowyblockDyedFences = new BlockDyedFences("glowyblockDyedFence" , Material.wood).setLightLevel(1F).setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundTypeWood).setBlockName("gwycraft.glowyblockDyedFence").setCreativeTab(tabs);
 // Sand
 		blockDyedSand = new BlockDyedSand().setHardness(0.5F).setStepSound(Block.soundTypeSand).setBlockName("gwycraft.blockDyedSand").setCreativeTab(tabs);
 		glowyblockDyedSand = new BlockDyedSand().setLightLevel(1F).setHardness(0.5F).setStepSound(Block.soundTypeSand).setBlockName("gwycraft.glowyblockDyedSand").setCreativeTab(tabs);
 		blockDyedSandstone = new BlockDyedSandstone(Material.rock).setHardness(0.5F).setStepSound(Block.soundTypeStone).setBlockName("gwycraft.blockDyedSandstone").setCreativeTab(tabs);
 		glowyblockDyedSandstone = new BlockDyedSandstone(Material.rock).setLightLevel(1F).setHardness(0.5F).setStepSound(Block.soundTypeStone).setBlockName("gwycraft.glowyblockDyedSandstone").setCreativeTab(tabs);
 // Stone Brick
 		blockDyedStoneBrick = new BlockGwyGenericBlock(Material.rock).setLightLevel(1F).setHardness(2f).setStepSound(Block.soundTypeStone).setBlockName("gwycraft.blockDyedStoneBrick").setCreativeTab(tabs).setBlockTextureName("gwycraft:stonebrick");
 		glowyblockDyedStoneBrick = new BlockGwyGenericBlock(Material.rock).setLightLevel(1F).setHardness(2f).setStepSound(Block.soundTypeStone).setBlockName("gwycraft.glowyblockDyedStoneBrick").setCreativeTab(tabs).setBlockTextureName("gwycraft:stonebrick");
         blockDyedStoneBrickPaver = new BlockGwyGenericPaver().setBlockName("gwycraft.blockDyedStoneBrickPaver").setHardness(2.0F).setCreativeTab(tabs).setBlockTextureName("gwycraft:stonebrick");
 // Stone Cobble
 		blockDyedStoneCobble = new BlockGwyGenericBlock(Material.rock).setHardness(2.0F).setResistance(10.0F).setStepSound(Block.soundTypeStone).setBlockName("gwycraft.blockDyedStoneCobble").setCreativeTab(tabs).setBlockTextureName("gwycraft:stonecobble");
 		glowyblockDyedStoneCobble = new BlockGwyGenericBlock(Material.rock).setLightLevel(1F).setHardness(2.0F).setResistance(10.0F).setStepSound(Block.soundTypeStone).setBlockName("gwycraft.glowyblockDyedStoneCobble").setCreativeTab(tabs).setBlockTextureName("gwycraft:stonecobble");
         blockDyedCobbleWalls = new BlockDyedCobbleWalls(blockDyedStoneCobble).setHardness(2.0F).setResistance(10.0F).setStepSound(Block.soundTypeStone).setBlockName("gwycraft.blockDyedCobbleWalls").setCreativeTab(tabs);
         glowyblockDyedCobbleWalls = new BlockDyedCobbleWalls(blockDyedStoneCobble).setLightLevel(1F).setHardness(2.0F).setResistance(10.0F).setStepSound(Block.soundTypeStone).setBlockName("gwycraft.glowyblockDyedCobbleWalls").setCreativeTab(tabs);
         blockDyedStoneCobblePaver = new BlockGwyGenericPaver().setBlockName("gwycraft.blockDyedStonePaver").setHardness(2.0F).setCreativeTab(tabs).setBlockTextureName("gwycraft:stonecobble");
 // Logs
         blockDyedLog1 = new BlockDyedLog1().setCreativeTab(tabs).setBlockName("gwycraft.blockDyedLog1").setBlockTextureName("gwycraft:logside").setCreativeTab(tabs).setHardness(2.0F).setStepSound(Block.soundTypeWood);
         blockDyedLog2 = new BlockDyedLog2().setCreativeTab(tabs).setBlockName("gwycraft.blockDyedLog2").setBlockTextureName("gwycraft:logside").setCreativeTab(tabs).setHardness(2.0F).setStepSound(Block.soundTypeWood);
         blockDyedLog3 = new BlockDyedLog3().setCreativeTab(tabs).setBlockName("gwycraft.blockDyedLog3").setBlockTextureName("gwycraft:logside").setCreativeTab(tabs).setHardness(2.0F).setStepSound(Block.soundTypeWood);
         blockDyedLog4 = new BlockDyedLog4().setCreativeTab(tabs).setBlockName("gwycraft.blockDyedLog4").setBlockTextureName("gwycraft:logside").setCreativeTab(tabs).setHardness(2.0F).setStepSound(Block.soundTypeWood);
         glowyblockDyedLog1 = new BlockDyedLog1().setCreativeTab(tabs).setBlockName("gwycraft.glowyblockDyedLog1").setLightLevel(1F).setBlockTextureName("gwycraft:logside").setCreativeTab(tabs).setHardness(2.0F).setStepSound(Block.soundTypeWood);
         glowyblockDyedLog2 = new BlockDyedLog2().setCreativeTab(tabs).setBlockName("gwycraft.glowyblockDyedLog2").setLightLevel(1F).setBlockTextureName("gwycraft:logside").setCreativeTab(tabs).setHardness(2.0F).setStepSound(Block.soundTypeWood);
         glowyblockDyedLog3 = new BlockDyedLog3().setCreativeTab(tabs).setBlockName("gwycraft.glowyblockDyedLog3").setLightLevel(1F).setBlockTextureName("gwycraft:logside").setCreativeTab(tabs).setHardness(2.0F).setStepSound(Block.soundTypeWood);
         glowyblockDyedLog4 = new BlockDyedLog4().setCreativeTab(tabs).setBlockName("gwycraft.glowyblockDyedLog4").setLightLevel(1F).setBlockTextureName("gwycraft:logside").setCreativeTab(tabs).setHardness(2.0F).setStepSound(Block.soundTypeWood);
 // Gems
         blockGemOre = new BlockGemOre(Material.rock).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundTypeStone).setBlockName("gemOre").setCreativeTab(tabs);
         blockGemCompressed = new BlockGemCompressed(Material.rock).setLightLevel(1F).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundTypeMetal).setBlockName("blockGemCompressed").setCreativeTab(tabs);
 // Torches        
         blockWhiteDyedTorch = new BlockDyedTorch("torch_white").setHardness(0.0F).setLightLevel(0.9375F).setStepSound(Block.soundTypeWood).setBlockName("gwycraft.torch.white").setCreativeTab(tabs);
         blockOrangeDyedTorch = new BlockDyedTorch("torch_orange").setHardness(0.0F).setLightLevel(0.9375F).setStepSound(Block.soundTypeWood).setBlockName("gwycraft.torch.orange").setCreativeTab(tabs);
         blockMagentaDyedTorch = new BlockDyedTorch("torch_magenta").setHardness(0.0F).setLightLevel(0.9375F).setStepSound(Block.soundTypeWood).setBlockName("gwycraft.torch.magenta").setCreativeTab(tabs);
         blockLBlueDyedTorch = new BlockDyedTorch("torch_lblue").setHardness(0.0F).setLightLevel(0.9375F).setStepSound(Block.soundTypeWood).setBlockName("gwycraft.torch.lblue").setCreativeTab(tabs);
         blockYellowDyedTorch = new BlockDyedTorch("torch_yellow").setHardness(0.0F).setLightLevel(0.9375F).setStepSound(Block.soundTypeWood).setBlockName("gwycraft.torch.yellow").setCreativeTab(tabs);
         blockLGreenDyedTorch = new BlockDyedTorch("torch_lgreen").setHardness(0.0F).setLightLevel(0.9375F).setStepSound(Block.soundTypeWood).setBlockName("gwycraft.torch.lgreen").setCreativeTab(tabs);
         blockPinkDyedTorch = new BlockDyedTorch("torch_pink").setHardness(0.0F).setLightLevel(0.9375F).setStepSound(Block.soundTypeWood).setBlockName("gwycraft.torch.pink").setCreativeTab(tabs);
         blockDGrayDyedTorch = new BlockDyedTorch("torch_dgray").setHardness(0.0F).setLightLevel(0.9375F).setStepSound(Block.soundTypeWood).setBlockName("gwycraft.torch.dgray").setCreativeTab(tabs);
         blockLGrayDyedTorch = new BlockDyedTorch("torch_lgray").setHardness(0.0F).setLightLevel(0.9375F).setStepSound(Block.soundTypeWood).setBlockName("gwycraft.torch.lgray").setCreativeTab(tabs);
         blockCyanDyedTorch = new BlockDyedTorch("torch_cyan").setHardness(0.0F).setLightLevel(0.9375F).setStepSound(Block.soundTypeWood).setBlockName("gwycraft.torch.cyan").setCreativeTab(tabs);
         blockPurpleDyedTorch = new BlockDyedTorch("torch_purple").setHardness(0.0F).setLightLevel(0.9375F).setStepSound(Block.soundTypeWood).setBlockName("gwycraft.torch.purple").setCreativeTab(tabs);
         blockBlueDyedTorch = new BlockDyedTorch("torch_blue").setHardness(0.0F).setLightLevel(0.9375F).setStepSound(Block.soundTypeWood).setBlockName("gwycraft.torch.blue").setCreativeTab(tabs);
         blockBrownDyedTorch = new BlockDyedTorch("torch_brown").setHardness(0.0F).setLightLevel(0.9375F).setStepSound(Block.soundTypeWood).setBlockName("gwycraft.torch.brown").setCreativeTab(tabs);
         blockGreenDyedTorch = new BlockDyedTorch("torch_green").setHardness(0.0F).setLightLevel(0.9375F).setStepSound(Block.soundTypeWood).setBlockName("gwycraft.torch.green").setCreativeTab(tabs);
         blockRedDyedTorch = new BlockDyedTorch("torch_red").setHardness(0.0F).setLightLevel(0.9375F).setStepSound(Block.soundTypeWood).setBlockName("gwycraft.torch.red").setCreativeTab(tabs);
         blockBlackDyedTorch = new BlockDyedTorch("torch_black").setHardness(0.0F).setLightLevel(0.9375F).setStepSound(Block.soundTypeWood).setBlockName("gwycraft.torch.black").setCreativeTab(tabs);
 // Items
         itemEnchantedGems = new ItemEnchantedGems().setUnlocalizedName("Gwycraft:itemEnchantedGems").setCreativeTab(tabs);
         itemDyedClay = new ItemDyedClay().setUnlocalizedName("Gwycraft:itemDyedClay").setCreativeTab(tabs);
         itemDyedClayBricks = new ItemDyedClayBricks().setUnlocalizedName("Gwycraft:itemDyedClayBricks").setCreativeTab(tabs);
         itemDyedMud = new ItemDyedMud().setUnlocalizedName("Gwycraft:itemMud").setCreativeTab(tabs);
         itemDyedMudBricks = new ItemDyedMudBricks().setUnlocalizedName("Gwycraft:itemMudBricks").setCreativeTab(tabs);
         itemDyedSticks = new ItemDyedSticks().setUnlocalizedName("Gwycraft:itemDyedSticks").setCreativeTab(tabs);
         itemGemShears = new ItemGemShears().setUnlocalizedName("Gwycraft:gemshears").setCreativeTab(tabs);
         itemGemHatchet = new ItemGemHatchet(GWYCRAFT_MATERIAL).setUnlocalizedName("Gwycraft:gemhatchet").setCreativeTab(tabs);
         itemGemHoe = new ItemGemHoe(GWYCRAFT_MATERIAL).setUnlocalizedName("Gwycraft:gemhoe").setCreativeTab(tabs);
         itemGemPickaxe = new ItemGemPickaxe(GWYCRAFT_MATERIAL).setUnlocalizedName("Gwycraft:gempickaxe").setCreativeTab(tabs);
         itemGemShovel = new ItemGemShovel(GWYCRAFT_MATERIAL).setUnlocalizedName("Gwycraft:gemshovel").setCreativeTab(tabs);
         itemGemSword = new ItemGemSword(GWYCRAFT_MATERIAL).setUnlocalizedName("Gwycraft:gemsword").setCreativeTab(tabs);
         
         CommonProxy.registerRenderers();
 
         //  GameRegistry Register Blocks
      // Wool        
         GameRegistry.registerBlock(glowyWool, ItemGlowyWool.class, "glowyWool");
      // Stone
         GameRegistry.registerBlock(blockDyedStone, ItemGwyGeneric.class, "blockDyedStone");
         GameRegistry.registerBlock(glowyblockDyedStone, ItemGwyGeneric.class, "glowyblockDyedStone");
         GameRegistry.registerBlock(blockDyedStonePaver, ItemGwyGeneric.class, "blockDyedStonePaver");
      // Bookshelves
         GameRegistry.registerBlock(blockDyedBookcase, ItemDyedBookshelf.class, "blockDyedBookcase");
         GameRegistry.registerBlock(glowyblockDyedBookcase, ItemDyedBookshelf.class, "glowyblockDyedBookcase");
      // Brick
         GameRegistry.registerBlock(blockDyedBrick, ItemGwyGeneric.class, "blockDyedBrick");
         GameRegistry.registerBlock(glowyblockDyedBrick, ItemGwyGeneric.class, "glowyblockDyedBrick");
         GameRegistry.registerBlock(blockDyedBrickPaver, ItemGwyGeneric.class, "blockDyedBrickPaver");
      // Clay
         GameRegistry.registerBlock(blockDyedClayblock, ItemGwyGeneric.class, "blockDyedClayblock");
         GameRegistry.registerBlock(glowyblockDyedClayblock, ItemGwyGeneric.class, "glowyblockDyedClayblock");
      // Glass
         GameRegistry.registerBlock(blockDyedGlass, ItemDyedGlass.class, "blockDyedGlass");
         GameRegistry.registerBlock(glowyblockDyedGlass, ItemGlowyDyedGlass.class, "glowyblockDyedGlass");
      // Leaf
         GameRegistry.registerBlock(blockDyedLeaf, ItemDyedLeaf.class, "blockDyedLeaf");
         GameRegistry.registerBlock(glowyblockDyedLeaf, ItemDyedLeaf.class, "glowyblockDyedLeaf");
      // Mud Brick
         GameRegistry.registerBlock(blockDyedMudBrick, ItemGwyGeneric.class, "blockDyedMudBrick");
         GameRegistry.registerBlock(glowyblockDyedMudBrick, ItemGwyGeneric.class, "glowyblockDyedMudBrick");
         GameRegistry.registerBlock(blockDyedMudBrickPaver, ItemGwyGeneric.class, "blockDyedMudBrickPaver");
      // Planks
         GameRegistry.registerBlock(blockDyedPlank, ItemDyedPlank.class, "blockDyedPlank");
         GameRegistry.registerBlock(glowyblockDyedPlank, ItemGwyGeneric.class, "glowyblockDyedPlank");
         GameRegistry.registerBlock(blockDyedFences, ItemDyedFences.class, "blockDyedFences");
         GameRegistry.registerBlock(glowyblockDyedFences, ItemDyedFences.class, "glowyblockDyedFences");
      // Sand
         GameRegistry.registerBlock(blockDyedSand, ItemGwyGeneric.class, "blockDyedSand");
         GameRegistry.registerBlock(glowyblockDyedSand, ItemGwyGeneric.class, "glowyblockDyedSand");
         GameRegistry.registerBlock(blockDyedSandstone, ItemGwyGeneric.class, "blockDyedSandstone");
         GameRegistry.registerBlock(glowyblockDyedSandstone, ItemGwyGeneric.class, "glowyblockDyedSandstone");
      // Stone Brick
         GameRegistry.registerBlock(blockDyedStoneBrick, ItemGwyGeneric.class,"blockDyedStoneBrick");
         GameRegistry.registerBlock(glowyblockDyedStoneBrick, ItemGwyGeneric.class, "glowyblockDyedStoneBrick");
         GameRegistry.registerBlock(blockDyedStoneBrickPaver, ItemGwyGeneric.class, "blockDyedStoneBrickPaver");
      // Stone Cobble
         GameRegistry.registerBlock(blockDyedStoneCobble, ItemGwyGeneric.class, "blockDyedStoneCobble");
         GameRegistry.registerBlock(glowyblockDyedStoneCobble, ItemGwyGeneric.class, "glowyblockDyedStoneCobble");
         GameRegistry.registerBlock(blockDyedCobbleWalls, ItemDyedCobbleWalls.class, "blockDyedCobbleWalls");
         GameRegistry.registerBlock(glowyblockDyedCobbleWalls, ItemDyedCobbleWalls.class, "glowyblockDyedCobbleWalls");
         GameRegistry.registerBlock(blockDyedStoneCobblePaver, ItemGwyGeneric.class, "blockDyedStoneCobblePaver");
      // Logs
         GameRegistry.registerBlock(blockDyedLog1, ItemDyedLog1.class, "blockDyedLog1");
         GameRegistry.registerBlock(blockDyedLog2, ItemDyedLog2.class, "blockDyedLog2");
         GameRegistry.registerBlock(blockDyedLog3, ItemDyedLog3.class, "blockDyedLog3");
         GameRegistry.registerBlock(blockDyedLog4, ItemDyedLog4.class, "blockDyedLog4");
         GameRegistry.registerBlock(glowyblockDyedLog1, ItemDyedLog1.class, "glowyblockDyedLog1");
         GameRegistry.registerBlock(glowyblockDyedLog2, ItemDyedLog2.class, "glowyblockDyedLog2");
         GameRegistry.registerBlock(glowyblockDyedLog3, ItemDyedLog3.class, "glowyblockDyedLog3");
         GameRegistry.registerBlock(glowyblockDyedLog4, ItemDyedLog4.class, "glowyblockDyedLog4");
      // Gems
         GameRegistry.registerBlock(blockGemCompressed, ItemGemCompressed.class, "blockGemCompressed");
         GameRegistry.registerBlock(blockGemOre, ItemGemOre.class, "blockGemOre");
      // Torches        
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
      // Items
         GameRegistry.registerItem(itemEnchantedGems, "itemEnchantedGems", null);
         GameRegistry.registerItem(itemDyedClay, "itemDyedClay", null);
         GameRegistry.registerItem(itemDyedClayBricks, "itemDyedClayBricks", null);
         GameRegistry.registerItem(itemDyedMud, "itemDyedMud", null)   ;
         GameRegistry.registerItem(itemDyedMudBricks, "itemDyedMudBricks", null);
         GameRegistry.registerItem(itemDyedSticks, "itemDyedSticks", null);
         GameRegistry.registerItem(itemGemShears, "itemGemShears", null);
         GameRegistry.registerItem(itemGemHatchet, "itemGemHatchet", null);
         GameRegistry.registerItem(itemGemHoe, "itemGemHoe", null);
         GameRegistry.registerItem(itemGemPickaxe, "itemGemPickaxe", null);
         GameRegistry.registerItem(itemGemShovel, "itemGemShovel", null);
         GameRegistry.registerItem(itemGemSword, "itemGemSword", null);
 
         // Shears Enchant
         ItemStack enchantedGemShears = new ItemStack(Gwycraft.itemGemShears);
         enchantedGemShears.addEnchantment(Enchantment.silkTouch, 1);
         
         // Language Registry
         // 16 Meta
         for (int i = 0; i < 16; i++) {
         	// Dye is inverted compared to wool
         	ItemStack dye = new ItemStack(Items.dye, 1, 15 - i);
         	
 			ItemStack cloth = new ItemStack(Blocks.wool, 1, i);
 			ItemStack glowyWoolStack = new ItemStack(glowyWool, 1, i);
             ItemStack blockDyedStoneStack = new ItemStack(blockDyedStone, 1, i);
             ItemStack glowyblockDyedStoneStack = new ItemStack(glowyblockDyedStone, 1, i);
             ItemStack blockDyedBookcaseStack = new ItemStack(blockDyedBookcase, 1, i);
             ItemStack glowyblockDyedBookcaseStack = new ItemStack(glowyblockDyedBookcase, 1, i);
             ItemStack blockDyedPlankStack = new ItemStack(blockDyedPlank, 1, i);
             ItemStack glowyblockDyedPlankStack = new ItemStack(glowyblockDyedPlank, 1, i);
             ItemStack blockDyedBrickStack = new ItemStack(blockDyedBrick, 1, i);
             ItemStack glowyblockDyedBrickStack = new ItemStack(glowyblockDyedBrick, 1, i);
             ItemStack blockDyedClayblockStack = new ItemStack(blockDyedClayblock, 1, i);
             ItemStack glowyblockDyedClayblockStack = new ItemStack(glowyblockDyedClayblock, 1, i);
             ItemStack blockDyedGlassStack = new ItemStack(blockDyedGlass, 1, i);
             ItemStack glowyblockDyedGlassStack = new ItemStack(glowyblockDyedGlass, 1, i);
             ItemStack blockDyedLeafStack = new ItemStack(blockDyedLeaf, 1, i);
             ItemStack glowyblockDyedLeafStack = new ItemStack(glowyblockDyedLeaf, 1, i);
             ItemStack blockDyedMudBrickStack = new ItemStack(blockDyedMudBrick, 1, i);
             ItemStack glowyblockDyedMudBrickStack = new ItemStack(glowyblockDyedMudBrick, 1, i);
             ItemStack blockDyedSandStack = new ItemStack(blockDyedSand, 1, i);
             ItemStack glowyblockDyedSandStack = new ItemStack(glowyblockDyedSand, 1, i);
             ItemStack blockDyedSandstoneStack = new ItemStack(blockDyedSandstone, 1, i);
             ItemStack glowyblockDyedSandstoneStack = new ItemStack(glowyblockDyedSandstone, 1, i);
             ItemStack blockDyedStoneBrickStack = new ItemStack(blockDyedStoneBrick, 1, i);
             ItemStack glowyblockDyedStoneBrickStack = new ItemStack(glowyblockDyedStoneBrick, 1, i);
             ItemStack blockDyedStonecobbleStack = new ItemStack(blockDyedStoneCobble, 1, i);
             ItemStack glowyblockDyedStonecobbleStack = new ItemStack(glowyblockDyedStoneCobble, 1, i);
             ItemStack blockGemCompressedStack = new ItemStack(blockGemCompressed, 1, i);
             ItemStack blockGemOreStack = new ItemStack(blockGemOre, 1, i);
             ItemStack itemEnchantedGemsStack = new ItemStack(itemEnchantedGems, 1, i);
             ItemStack itemDyedClayStack = new ItemStack(itemDyedClay, 1, i);
             ItemStack itemDyedClayBricksStack = new ItemStack(itemDyedClayBricks, 1, i);
             ItemStack itemDyedMudStack = new ItemStack(itemDyedMud, 1, i);
             ItemStack itemDyedMudBricksStack = new ItemStack(itemDyedMudBricks, 1, i);
             ItemStack blockDyedFencesStack = new ItemStack(blockDyedFences, 1, i);
             ItemStack glowyblockDyedFencesStack = new ItemStack(glowyblockDyedFences, 1, i);
             ItemStack blockDyedCobbleWallsStack = new ItemStack(blockDyedCobbleWalls, 1, i);
             ItemStack glowyblockDyedCobbleWallsStack = new ItemStack(glowyblockDyedCobbleWalls, 1, i);
             ItemStack itemDyedSticksStack = new ItemStack(itemDyedSticks, 1, i);
             ItemStack blockDyedStonePaverStack = new ItemStack(blockDyedStonePaver, 1, i);
             ItemStack blockDyedBrickPaverStack = new ItemStack(blockDyedBrickPaver, 1, i);
             ItemStack blockDyedMudBrickPaverStack = new ItemStack(blockDyedMudBrickPaver, 1, i);
             ItemStack blockDyedStoneBrickPaverStack = new ItemStack(blockDyedStoneBrickPaver, 1, i);
             ItemStack blockDyedStoneCobblePaverStack = new ItemStack(blockDyedStoneCobblePaver, 1, i);            
 
             //  Add Recipes
             GameRegistry.addRecipe(new ItemStack(blockDyedBookcase, 1, i), "###", "XXX", "###", '#', blockDyedPlankStack, 'X', Items.book);
             GameRegistry.addRecipe(new ItemStack(glowyblockDyedBookcase, 1, i), "###", "XXX", "###", '#', glowyblockDyedPlankStack, 'X', Items.book);
             GameRegistry.addRecipe(new ItemStack(itemDyedSticks, 4, i), "X", "X", 'X', blockDyedPlankStack);
             GameRegistry.addRecipe(new ItemStack(blockGemCompressed, 1, i), "XX", "XX", 'X', itemEnchantedGemsStack);
             GameRegistry.addRecipe(new ItemStack(itemEnchantedGems, 4, i), "X", 'X', blockGemCompressedStack);
             GameRegistry.addRecipe(new ItemStack(blockDyedStoneBrick, 4, i), "xx", "xx", 'x', blockDyedStoneStack);
             GameRegistry.addRecipe(new ItemStack(glowyblockDyedStoneBrick, 4, i), "XX", "XX", 'X', glowyblockDyedStoneStack);
             GameRegistry.addRecipe(new ItemStack(blockDyedFences, 2, i), "XXX", "XXX", 'X', itemDyedSticksStack);
             GameRegistry.addRecipe(new ItemStack(blockDyedMudBrick, 4, i), "XX", "XX", 'X', itemDyedMudBricksStack);
             GameRegistry.addRecipe(new ItemStack(blockDyedClayblock, 1, i), "XX", "XX", 'X', itemDyedClayStack);
             GameRegistry.addRecipe(new ItemStack(blockDyedBrick, 1, i), "XX", "XX", 'X', itemDyedClayBricksStack);
             GameRegistry.addRecipe(new ItemStack(blockDyedCobbleWalls, 1, i), "XXX", "XXX", 'X', blockDyedStonecobbleStack);
             GameRegistry.addRecipe(new ItemStack(glowyblockDyedCobbleWalls, 1, i), "XXX", "XXX", 'X', glowyblockDyedStonecobbleStack);
             GameRegistry.addRecipe(new ItemStack(blockDyedStonePaver, 3, i), "XX", 'X', blockDyedStoneStack);
             GameRegistry.addRecipe(new ItemStack(blockDyedBrickPaver, 3, i), "XX", 'X', blockDyedBrickStack);
             GameRegistry.addRecipe(new ItemStack(blockDyedMudBrickPaver, 3, i), "XX", 'X', blockDyedMudBrickStack);
             GameRegistry.addRecipe(new ItemStack(blockDyedStoneCobblePaver, 3, i), "XX", 'X', blockDyedStonecobbleStack);
             
             // Add Shapeless Recipes
             GameRegistry.addShapelessRecipe(glowyWoolStack, new ItemStack(Blocks.glowstone), cloth);
             GameRegistry.addShapelessRecipe(new ItemStack(blockDyedSand, 8, i), dye, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.sand);
             GameRegistry.addShapelessRecipe(new ItemStack(glowyblockDyedSand, 8, i), itemEnchantedGemsStack, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.sand);
             GameRegistry.addShapelessRecipe(new ItemStack(blockDyedSandstone, 8, i), dye, Blocks.sandstone, Blocks.sandstone, Blocks.sandstone, Blocks.sandstone, Blocks.sandstone, Blocks.sandstone, Blocks.sandstone, Blocks.sandstone);
             GameRegistry.addShapelessRecipe(new ItemStack(glowyblockDyedSandstone, 8, i), itemEnchantedGemsStack, Blocks.sandstone, Blocks.sandstone, Blocks.sandstone, Blocks.sandstone, Blocks.sandstone, Blocks.sandstone, Blocks.sandstone, Blocks.sandstone);
             GameRegistry.addShapelessRecipe(new ItemStack(blockDyedPlank, 8, i), dye, Blocks.planks, Blocks.planks, Blocks.planks, Blocks.planks, Blocks.planks, Blocks.planks, Blocks.planks, Blocks.planks);
             GameRegistry.addShapelessRecipe(new ItemStack(glowyblockDyedPlank, 8, i), itemEnchantedGemsStack, Blocks.planks, Blocks.planks, Blocks.planks, Blocks.planks, Blocks.planks, Blocks.planks, Blocks.planks, Blocks.planks);
             GameRegistry.addShapelessRecipe(new ItemStack(Items.dye, 4, 15-i), itemEnchantedGemsStack, Items.gunpowder, Items.glass_bottle );
             GameRegistry.addShapelessRecipe(new ItemStack(itemDyedMud, 16, i), Blocks.dirt, Blocks.dirt, Blocks.dirt, Blocks.dirt, Blocks.dirt, Blocks.dirt, Items.clay_ball, Items.potionitem, dye);
             GameRegistry.addShapelessRecipe(new ItemStack(itemDyedClay, 8, i), Items.clay_ball, Items.clay_ball, Items.clay_ball, Items.clay_ball, Items.clay_ball, Items.clay_ball, Items.clay_ball, Items.clay_ball, dye);
             GameRegistry.addShapelessRecipe(new ItemStack(glowyblockDyedClayblock, 8, i), itemEnchantedGemsStack, Blocks.clay, Blocks.clay, Blocks.clay, Blocks.clay, Blocks.clay, Blocks.clay, Blocks.clay, Blocks.clay);
             GameRegistry.addShapelessRecipe(new ItemStack(glowyblockDyedBrick, 8, i), itemEnchantedGemsStack, Blocks.brick_block, Blocks.brick_block, Blocks.brick_block, Blocks.brick_block, Blocks.brick_block, Blocks.brick_block, Blocks.brick_block, Blocks.brick_block);
             GameRegistry.addShapelessRecipe(new ItemStack(glowyblockDyedFences, 8, i), itemEnchantedGemsStack, Blocks.fence, Blocks.fence, Blocks.fence, Blocks.fence, Blocks.fence, Blocks.fence, Blocks.fence, Blocks.fence);
             GameRegistry.addShapelessRecipe(new ItemStack(glowyblockDyedMudBrick, 8, i), Blocks.glowstone, blockDyedMudBrickStack, blockDyedMudBrickStack, blockDyedMudBrickStack, blockDyedMudBrickStack, blockDyedMudBrickStack, blockDyedMudBrickStack, blockDyedMudBrickStack, blockDyedMudBrickStack);
             
             // Add Furnace Recipes
             GameRegistry.addSmelting(new ItemStack(blockDyedStoneCobble, 1, i ), new ItemStack(blockDyedStone, 1, i ), 0.1F);
             GameRegistry.addSmelting(new ItemStack(glowyblockDyedStoneCobble, 1, i), new ItemStack(glowyblockDyedStone, 1, i ), 0.1F);
             GameRegistry.addSmelting(new ItemStack(blockDyedSand, 1, i), new ItemStack(blockDyedGlass, 1, i ), 0.1F);
             GameRegistry.addSmelting(new ItemStack(glowyblockDyedSand, 1, i), new ItemStack(glowyblockDyedGlass, 1, i ), 0.1F);
             GameRegistry.addSmelting(new ItemStack(itemDyedMud, 1, i), new ItemStack(itemDyedMudBricks, 1, i), 0.1F);
             GameRegistry.addSmelting(new ItemStack(itemDyedClay, 1, i), new ItemStack(itemDyedClayBricks, 1, i), 0.1F);
 
             // Ore Dictionary
             GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(blockDyedStoneCobble, 8, i), dye, "cobblestone", "cobblestone", "cobblestone", "cobblestone", "cobblestone", "cobblestone", "cobblestone", "cobblestone"));
             GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(glowyblockDyedStoneCobble, 8, i), itemEnchantedGemsStack, "cobblestone", "cobblestone", "cobblestone", "cobblestone", "cobblestone", "cobblestone", "cobblestone", "cobblestone"));
 
             // Recipes for Dyed Leaves
             for (int j = 0; j < 4; j++) {
                 ItemStack leaf = new ItemStack(Blocks.leaves, 1, j);
                 GameRegistry.addShapelessRecipe(new ItemStack(blockDyedLeaf, 8, i), dye, leaf, leaf, leaf, leaf, leaf, leaf, leaf, leaf);
                 GameRegistry.addShapelessRecipe(new ItemStack(glowyblockDyedLeaf, 8, i), itemEnchantedGemsStack, leaf, leaf, leaf, leaf, leaf, leaf, leaf, leaf);
             }
 
             // Recipes for Dyed Torches
             if ( i == 0)
                 GameRegistry.addRecipe(new ItemStack(blockWhiteDyedTorch, 4), "X", "#", 'X', Items.coal, '#', itemDyedSticksStack);
             else if ( i == 1)
                 GameRegistry.addRecipe(new ItemStack(blockOrangeDyedTorch, 4), "X", "#", 'X', Items.coal, '#', itemDyedSticksStack);
             else if ( i == 2)
                 GameRegistry.addRecipe(new ItemStack(blockMagentaDyedTorch, 4), "X", "#", 'X', Items.coal, '#', itemDyedSticksStack);
             else if ( i == 3)
                 GameRegistry.addRecipe(new ItemStack(blockLBlueDyedTorch, 4), "X", "#", 'X', Items.coal, '#', itemDyedSticksStack);
             else if ( i == 4)
                 GameRegistry.addRecipe(new ItemStack(blockYellowDyedTorch, 4), "X", "#", 'X', Items.coal, '#', itemDyedSticksStack);
             else if ( i == 5)
                 GameRegistry.addRecipe(new ItemStack(blockLGreenDyedTorch, 4), "X", "#", 'X', Items.coal, '#', itemDyedSticksStack);
             else if ( i == 6)
                 GameRegistry.addRecipe(new ItemStack(blockPinkDyedTorch, 4), "X", "#", 'X', Items.coal, '#', itemDyedSticksStack);
             else if ( i == 7)
                 GameRegistry.addRecipe(new ItemStack(blockDGrayDyedTorch, 4), "X", "#", 'X', Items.coal, '#', itemDyedSticksStack);
             else if ( i == 8)
                 GameRegistry.addRecipe(new ItemStack(blockLGrayDyedTorch, 4), "X", "#", 'X', Items.coal, '#', itemDyedSticksStack);
             else if ( i == 9)
                 GameRegistry.addRecipe(new ItemStack(blockCyanDyedTorch, 4), "X", "#", 'X', Items.coal, '#', itemDyedSticksStack);
             else if ( i == 10)
                 GameRegistry.addRecipe(new ItemStack(blockPurpleDyedTorch, 4), "X", "#", 'X', Items.coal, '#', itemDyedSticksStack);
             else if ( i == 11)
                 GameRegistry.addRecipe(new ItemStack(blockBlueDyedTorch, 4), "X", "#", 'X', Items.coal, '#', itemDyedSticksStack);
             else if ( i == 12)
                 GameRegistry.addRecipe(new ItemStack(blockBrownDyedTorch, 4), "X", "#", 'X', Items.coal, '#', itemDyedSticksStack);
             else if ( i == 13)
                 GameRegistry.addRecipe(new ItemStack(blockGreenDyedTorch, 4), "X", "#", 'X', Items.coal, '#', itemDyedSticksStack);
             else if ( i ==14)
                 GameRegistry.addRecipe(new ItemStack(blockRedDyedTorch, 4), "X", "#", 'X', Items.coal, '#', itemDyedSticksStack);
             else if ( i ==15)
             	GameRegistry.addRecipe(new ItemStack(blockBlackDyedTorch, 4), "X", "#", 'X', Items.coal, '#', itemDyedSticksStack);
         }
 
         // 4 Meta
         for (int i = 0; i < 4; i++) {
 
             // Item Stacks
             ItemStack blockDyedLog1Stack = new ItemStack(blockDyedLog1, 1, i);
             ItemStack blockDyedLog2Stack = new ItemStack(blockDyedLog2, 1, i);
             ItemStack blockDyedLog3Stack = new ItemStack(blockDyedLog3, 1, i);
             ItemStack blockDyedLog4Stack = new ItemStack(blockDyedLog4, 1, i);
             ItemStack dye1 = new ItemStack(Items.dye, 1, 15 - i);
             ItemStack dye2 = new ItemStack(Items.dye, 1, 11 - i);
             ItemStack dye3 = new ItemStack(Items.dye, 1, 7 - i);
             ItemStack dye4 = new ItemStack(Items.dye, 1, 3 - i);
             ItemStack glowyblockDyedLog1Stack = new ItemStack(glowyblockDyedLog1, 1, i);
             ItemStack glowyblockDyedLog2Stack = new ItemStack(glowyblockDyedLog2, 1, i);
             ItemStack glowyblockDyedLog3Stack = new ItemStack(glowyblockDyedLog3, 1, i);
             ItemStack glowyblockDyedLog4Stack = new ItemStack(glowyblockDyedLog4, 1, i);
             ItemStack itemEnchantedGemsStack1 = new ItemStack(itemEnchantedGems, 1, i);
             ItemStack itemEnchantedGemsStack2 = new ItemStack(itemEnchantedGems, 1, i+4);
             ItemStack itemEnchantedGemsStack3 = new ItemStack(itemEnchantedGems, 1, i+8);
             ItemStack itemEnchantedGemsStack4 = new ItemStack(itemEnchantedGems, 1, i+12);
 
             // Language Registry
          /*   LanguageRegistry.addName(blockDyedLog1Stack, gwyColorLog1Names[blockDyedLog1Stack.getItemDamage()] + " Log");
             LanguageRegistry.addName(blockDyedLog2Stack, gwyColorLog2Names[blockDyedLog2Stack.getItemDamage()] + " Log");
             LanguageRegistry.addName(blockDyedLog3Stack, gwyColorLog3Names[blockDyedLog3Stack.getItemDamage()] + " Log");
             LanguageRegistry.addName(blockDyedLog4Stack, gwyColorLog4Names[blockDyedLog4Stack.getItemDamage()] + " Log");
             LanguageRegistry.addName(glowyblockDyedLog1Stack, "Glowy " + gwyColorLog1Names[glowyblockDyedLog1Stack.getItemDamage()] + " Log");
             LanguageRegistry.addName(glowyblockDyedLog2Stack, "Glowy " + gwyColorLog2Names[glowyblockDyedLog2Stack.getItemDamage()] + " Log");
             LanguageRegistry.addName(glowyblockDyedLog3Stack, "Glowy " + gwyColorLog3Names[glowyblockDyedLog3Stack.getItemDamage()] + " Log");
             LanguageRegistry.addName(glowyblockDyedLog4Stack, "Glowy " + gwyColorLog4Names[glowyblockDyedLog4Stack.getItemDamage()] + " Log");*/
 
             //  Add Recipes
             GameRegistry.addShapelessRecipe(new ItemStack(blockDyedPlank, 4, i), blockDyedLog1Stack);
             GameRegistry.addShapelessRecipe(new ItemStack(blockDyedPlank, 4, i+4), blockDyedLog2Stack);
             GameRegistry.addShapelessRecipe(new ItemStack(blockDyedPlank, 4, i+8), blockDyedLog3Stack);
             GameRegistry.addShapelessRecipe(new ItemStack(blockDyedPlank, 4, i+12), blockDyedLog4Stack);
             GameRegistry.addShapelessRecipe(new ItemStack(glowyblockDyedPlank, 4, i), glowyblockDyedLog1Stack);
             GameRegistry.addShapelessRecipe(new ItemStack(glowyblockDyedPlank, 4, i+4), glowyblockDyedLog2Stack);
             GameRegistry.addShapelessRecipe(new ItemStack(glowyblockDyedPlank, 4, i+8), glowyblockDyedLog3Stack);
             GameRegistry.addShapelessRecipe(new ItemStack(glowyblockDyedPlank, 4, i+12), glowyblockDyedLog4Stack);
 
             // Recipes for Dyed Leaves
             for (int j = 0; j < 4; j++) {
                 ItemStack log = new ItemStack(Blocks.log, 1, j);
 
                 GameRegistry.addShapelessRecipe(new ItemStack(blockDyedLog1, 8, i), dye1, log, log, log, log, log, log, log, log);
                 GameRegistry.addShapelessRecipe(new ItemStack(blockDyedLog2, 8, i), dye2, log, log, log, log, log, log, log, log);
                 GameRegistry.addShapelessRecipe(new ItemStack(blockDyedLog3, 8, i), dye3, log, log, log, log, log, log, log, log);
                 GameRegistry.addShapelessRecipe(new ItemStack(blockDyedLog4, 8, i), dye4, log, log, log, log, log, log, log, log);
                 GameRegistry.addShapelessRecipe(new ItemStack(glowyblockDyedLog1, 8, i), itemEnchantedGemsStack1, log, log, log, log, log, log, log, log);
                 GameRegistry.addShapelessRecipe(new ItemStack(glowyblockDyedLog2, 8, i), itemEnchantedGemsStack2, log, log, log, log, log, log, log, log);
                 GameRegistry.addShapelessRecipe(new ItemStack(glowyblockDyedLog3, 8, i), itemEnchantedGemsStack3, log, log, log, log, log, log, log, log);
                 GameRegistry.addShapelessRecipe(new ItemStack(glowyblockDyedLog4, 8, i), itemEnchantedGemsStack4, log, log, log, log, log, log, log, log);
             }
         }
         
         // Ore Dictionary
         OreDictionary.registerOre("gemGwycraft", new ItemStack(itemEnchantedGems, 1, OreDictionary.WILDCARD_VALUE));
 
         // Recipes
         if (ConfigGwycraft.toolsEnabled) {       
 		    GameRegistry.addRecipe(new ShapedOreRecipe(enchantedGemShears, " X", "X ", 'X', "gemGwycraft"));
 		    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemGemHatchet, 1), "XX ", "XY ", " Y ", 'X', "gemGwycraft", 'Y', "stickWood"));
 		    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemGemHatchet, 1), " XX", " YX", " Y ", 'X', "gemGwycraft", 'Y', "stickWood"));
 		    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemGemHoe, 1), "XX ", " Y ", " Y ", 'X', "gemGwycraft", 'Y', "stickWood"));
 		    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemGemHoe, 1), " XX", " Y ", " Y ", 'X', "gemGwycraft", 'Y', "stickWood"));
 		    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemGemPickaxe, 1), "XXX", " Y ", " Y ", 'X', "gemGwycraft", 'Y', "stickWood"));
 		    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemGemShovel, 1), " X ", " Y ", " Y ", 'X', "gemGwycraft", 'Y', "stickWood"));
 		    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemGemSword, 1), "X  ", "X  ", "Y  ", 'X', "gemGwycraft", 'Y', "stickWood"));
 		    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemGemSword, 1), " X ", " X ", " Y ", 'X', "gemGwycraft", 'Y', "stickWood"));
 		    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemGemSword, 1), "  X", "  X", "  Y", 'X', "gemGwycraft", 'Y', "stickWood"));
         }
         
     }
 	
 	@EventHandler 
     public void init(FMLInitializationEvent event) {
         GameRegistry.registerWorldGenerator(worldGen, 0);
         InterModCommunication.initIMC();
     }
 	
 	@EventHandler
 	public void postInit(FMLPostInitializationEvent event) {
 		// Stub Method
 
 	}
 	
 
 }
