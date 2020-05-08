 package sobiohazardous.minestrappolation.extradecor;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 
 import sobiohazardous.minestrappolation.extradecor.block.*;
 import sobiohazardous.minestrappolation.extradecor.bridge.BridgeRecipes;
 import sobiohazardous.minestrappolation.extradecor.gen.EDOreGenerator;
 import sobiohazardous.minestrappolation.extradecor.handler.ClientPacketHandler;
 import sobiohazardous.minestrappolation.extradecor.handler.EDGuiHandler;
 import sobiohazardous.minestrappolation.extradecor.handler.ServerPacketHandler;
 import sobiohazardous.minestrappolation.extradecor.item.EDItem;
 import sobiohazardous.minestrappolation.extradecor.item.ItemBlockPlacer;
 import sobiohazardous.minestrappolation.extradecor.lib.EDBlockRegistry;
 import sobiohazardous.minestrappolation.extradecor.lib.EDNameManager;
 import sobiohazardous.minestrappolation.extradecor.lib.EDRecipeManager;
 import sobiohazardous.minestrappolation.extradecor.proxy.CommonProxy;
 import sobiohazardous.minestrappolation.extradecor.tileentity.TileEntityCrate;
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockGlass;
 import net.minecraft.block.BlockGlowStone;
 import net.minecraft.block.BlockHalfSlab;
 import net.minecraft.block.BlockObsidian;
 import net.minecraft.block.BlockPane;
 import net.minecraft.block.BlockStairs;
 import net.minecraft.block.BlockWood;
 import net.minecraft.block.material.MapColor;
 import net.minecraft.block.material.Material;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemMultiTextureTile;
 import net.minecraft.item.ItemSlab;
 import net.minecraft.item.ItemStack;
 import net.minecraft.potion.Potion;
 import net.minecraft.util.WeightedRandomChestContent;
 import net.minecraftforge.common.ChestGenHooks;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.MinecraftForge;
 import cpw.mods.fml.client.registry.RenderingRegistry;
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
 import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.EntityRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import sobiohazardous.minestrappolation.extradecor.material.*;
import sobiohazardous.minestrappolation.extraores.gen.EOOreGenerator;
 
 /**
  * 
  * 
  * 
  * @author SoBiohazardous
  */
 @NetworkMod(clientSideRequired = true, serverSideRequired = false,
 clientPacketHandlerSpec = @SidedPacketHandler(channels = {"extradecorChan"}, packetHandler = ClientPacketHandler.class),
 serverPacketHandlerSpec = @SidedPacketHandler(channels = {"extradecorChanS"}, packetHandler = ServerPacketHandler.class))
 @Mod ( modid = "ExtraDecor", name="Extrappolated Decor", version="B1.0")
 public class ExtraDecor 
 {
 	@SidedProxy(clientSide = "sobiohazardous.minestrappolation.extradecor.proxy.ClientProxy", serverSide = "sobiohazardous.minestrappolation.extradecor.proxy.CommonProxy")
     public static CommonProxy proxy;
 	
 	@Instance("ExtraDecor")
 	public static ExtraDecor instance;
 
 	public static int
 	stoneBlockRefinedId,
 	stonePillarId,
 	stoneTileId,
 	stoneLampId,
 	edgeStoneBrickId,
 	edgeStoneLeftId,
 	edgeStoneBrickRightId,
 	brickPatternId,
 	obsidianTileId,
 	snowBrickId,
 	endstoneSmoothId,
 	endstoneRefinedId,
 	endstoneBrickId,
 	glassRefinedId,
 	glassRefinedPaneId,
 	woodPanelId,
 	woodBeveledId,
 	flintBlockId,
 	gunpowderBlockId,
 	ropeId,
 	itemRopeId,
 	ropeCoilId,
 	oozeSlimeId,
 	netherBrickPatternId,
 	sandstoneBrickId,
 	sandstonePillarId,
 	woodBoardsId,
 	flintTileId,
 	netherQuartzTileId,
 	sugarBlockId,
 	meatBlockId,
 	magmaOozeId,
 	enderBlockId,
 	crateId,
 	barrelId,
 	cardboardId,
 	cardboardItemId,
 	cardboardBlockId,
 	cardboardWetId,
 	sandstoneBrickItemId,
 	stoneBrickItemId,
 	checkerTileId,
 	woodBoardsStairsOakId,
 	woodBoardsStairsBirchId,
 	woodBoardsStairsSpruceId,
 	woodBoardsStairsJungleId,
 	woodBoardsSingleSlabId,
 	woodBoardsDoubleSlabId,
 	checkerTileStairsId;
 	
 	public static Block stoneBlockRefined;
 	public static Block stonePillar;
 	public static Block stoneTile;
 	public static Block stoneLamp;
 	
 	public static Block edgeStoneBrick;
 	public static Block edgeStoneBrickLeft;
 	public static Block edgeStoneBrickRight;
 	
 	public static Block brickPattern;
 	
 	public static Block obsidianTile;
 	
 	public static Block snowBrick;
 	
 	public static Block endstoneSmooth;
 	public static Block endstoneRefined;
 	public static Block endstoneBrick;
 	
 	public static Block glassRefined;
 	public static Block glassRefinedPane;
 	
 	public static Block woodPanel;
 	
 	public static Block woodBeveled;
 
 	public static Block flintBlock;
 	
 	public static Block gunpowderBlock;
 	
 	public static Block rope;
 	public static Item itemRope;
 	public static Block ropeCoil;
 	
 	public static Block oozeSlime;
 	
 	public static Block netherBrickPattern;
 	
 	public static Block sandstoneBricks;	
 	public static Block sandstonePillar;
 	
 	public static Block woodBoards;
 	
 	public static Block flintTile;
 	
 	public static Block netherQuartzTile;
 	
 	public static Block sugarBlock;
 	public static Block meatBlock;
 	
 	public static Block magmaOoze;
 	
 	public static Block enderBlock;
 	
 	public static Block crate;
 	public static Block barrel;
 	
 	public static Block cardboard;
 	public static Item cardboardItem;	
 	public static Block cardboardBlock;
 	public static Block cardboardWet;
 	
 	public static Item sandstoneBrickItem;
 	public static Item stoneBrickItem;
 	
 	public static Block checkerTile;
 	
 	public static Block woodBoardsStairsOak;
 	public static Block woodBoardsStairsBirch;
 	public static Block woodBoardsStairsSpruce;
 	public static Block woodBoardsStairsJungle;
 	public static BlockHalfSlab woodBoardsSingleSlab;
 	public static BlockHalfSlab woodBoardsDoubleSlab;
 	
 	public static Block checkerTileStairs;
 	
 	public static CreativeTabs tabDecorBlocks = new CreativeTabExtraDecorBlocks(CreativeTabs.getNextID(),"Extrappolated Decor");
 	
 	public static int paneRenderId = RenderingRegistry.getNextAvailableRenderId();
 	public static int ropeRenderId = RenderingRegistry.getNextAvailableRenderId();
 	
 	public static final Material materialOoze = new MaterialOoze(MapColor.foliageColor);
 	
 	@Mod.EventHandler
 	public void preLoad(FMLPreInitializationEvent event)
 	{
 		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
 		config.load();
 		//blocks 700
 		//items 2500
 		stoneBlockRefinedId = config.getBlock("Stone Block Refined", 700).getInt();
 		stonePillarId = config.getBlock("Stone Pillar", 701).getInt();
 		stoneTileId = config.getBlock("Stone Tile", 702).getInt();
 		stoneLampId = config.getBlock("Stone Lamp", 703).getInt();
 		edgeStoneBrickId = config.getBlock("Edge Stone Brick", 704).getInt();
 		edgeStoneLeftId = config.getBlock("Left Edgestone Brick", 705).getInt();
 		edgeStoneBrickRightId = config.getBlock("Rigt Edgestone Brick", 706).getInt();
 		brickPatternId = config.getBlock("Pattern Brick", 707).getInt();
 		obsidianTileId = config.getBlock("Obsidian Tile", 708).getInt();
 		snowBrickId = config.getBlock("Snow Brick", 709).getInt();
 		endstoneSmoothId = config.getBlock("Smooth Endstone", 710).getInt();
 		endstoneRefinedId = config.getBlock("Refined Endstone", 711).getInt();
 		endstoneBrickId = config.getBlock("Endstone Brick", 712).getInt();
 		glassRefinedId = config.getBlock("Refined Glass", 713).getInt();
 		glassRefinedPaneId = config.getBlock("Refined Glass Pane", 714).getInt();
 		woodPanelId = config.getBlock("Wood Panel", 715).getInt();
 		woodBeveledId = config.getBlock("Beveled Wood", 716).getInt();
 		flintBlockId = config.getBlock("Flint Block", 717).getInt();
 		gunpowderBlockId = config.getBlock("Gunpowder Block", 718).getInt();
 		ropeId = config.getBlock("Rope", 719).getInt();
 		itemRopeId = config.getItem("Rope Item", 25000).getInt();
 		ropeCoilId = config.getBlock("Rope Coil", 721).getInt();
 		oozeSlimeId = config.getBlock("Slime Oooze", 722).getInt();
 		netherBrickPatternId = config.getBlock("Patterned Nether Brick", 723).getInt();
 		sandstoneBrickId = config.getBlock("Sandstone Brick", 724).getInt();
 		sandstonePillarId = config.getBlock("Sandstone Pillar", 725).getInt();
 		woodBoardsId = config.getBlock("Wood Boards", 726).getInt();
 		flintTileId = config.getBlock("Flint Tile", 727).getInt();
 		netherQuartzTileId = config.getBlock("Nether Quartz Tile", 728).getInt();
 		sugarBlockId = config.getBlock("Sugar Block", 729).getInt();
 		meatBlockId = config.getBlock("Meat Block", 730).getInt();
 		magmaOozeId = config.getBlock("Magma Ooze", 731).getInt();
 		enderBlockId = config.getBlock("Ender Block", 732).getInt();
 		crateId = config.getBlock("Crate", 733).getInt();
 		barrelId = config.getBlock("Barrel", 734).getInt();
 		cardboardId = config.getBlock("Cardboard", 735).getInt();
 		cardboardItemId = config.getItem("Cardboard Placer", 25001).getInt();
 		cardboardBlockId = config.getBlock("Cardboard Block", 736).getInt();
 		cardboardWetId = config.getBlock("Wet Cardboard", 737).getInt();
 		sandstoneBrickItemId = config.getItem("Sandstone Brick Item", 25002).getInt();
 		stoneBrickItemId = config.getItem("Stone Brick Item", 25003).getInt();
 		checkerTileId = config.getBlock("Checker Tile", 738).getInt();
 		woodBoardsStairsOakId = config.getBlock("Oak Board Stairs", 739).getInt();
 		woodBoardsStairsBirchId = config.getBlock("Birch Boards Stairs", 740).getInt();
 		woodBoardsStairsSpruceId = config.getBlock("Spruce Boards Stairs", 741).getInt();
 		woodBoardsStairsJungleId = config.getBlock("Jungle Boards Stairs", 742).getInt();
 		woodBoardsSingleSlabId = config.getBlock("Wood Boards Single Slab", 743).getInt();
 		woodBoardsDoubleSlabId = config.getBlock("Wood Boards Double Slab", 744).getInt();
 		checkerTileStairsId = config.getBlock("Checker Tile Stairs", 745).getInt();
 		
 		config.save();		
 		
 		stoneBlockRefined = (new EDBlock(stoneBlockRefinedId, Material.rock, "block_StoneRefined")).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundStoneFootstep).setCreativeTab(tabDecorBlocks).setUnlocalizedName("stoneBlockRefined");
 		stonePillar = (new BlockPillar(stonePillarId, "block_StonePillar", "block_StoneRefined")).setHardness(1.5F).setResistance(10F).setCreativeTab(tabDecorBlocks).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("stonePillar");
 		stoneTile = (new EDBlock(stoneTileId, Material.rock, "block_StoneTile")).setHardness(1.5F).setResistance(10F).setCreativeTab(tabDecorBlocks).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("stoneTile");
 		stoneLamp = (new BlockStoneLamp(stoneLampId)).setHardness(1.5F).setResistance(8F).setCreativeTab(tabDecorBlocks).setStepSound(Block.soundStoneFootstep).setLightValue(1.0F).setUnlocalizedName("stoneLamp");
 		
 		edgeStoneBrick = (new BlockEdgeStoneBrick(edgeStoneBrickId)).setHardness(1.5F).setResistance(10F).setCreativeTab(tabDecorBlocks).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("edgeStoneBrick");
 		edgeStoneBrickLeft = (new BlockEdgeStoneBrickLeft(edgeStoneLeftId)).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("edgeStoneBrickLeft");
 		edgeStoneBrickRight = (new BlockEdgeStoneBrickRight(edgeStoneBrickRightId)).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("edgeStoneBrickRight");
 		
 		brickPattern = (new EDBlock(brickPatternId, Material.rock, "block_PatternBrick")).setHardness(2.0F).setResistance(10.0F).setCreativeTab(tabDecorBlocks).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("brickPattern");
 		
 		obsidianTile = (new EDBlock(obsidianTileId, Material.rock, "block_ObsidianTile")).setHardness(50.0F).setResistance(2000.0F).setCreativeTab(tabDecorBlocks).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("obsidianTile");
 		
 		snowBrick = (new EDBlock(snowBrickId, Material.snow, "block_SnowBrick")).setHardness(0.3F).setCreativeTab(tabDecorBlocks).setStepSound(Block.soundSnowFootstep).setUnlocalizedName("snowBrick");
 		
 		endstoneSmooth = (new EDBlock(endstoneSmoothId, Material.rock, "block_EndstoneSmooth")).setHardness(3.0F).setResistance(15.0F).setCreativeTab(tabDecorBlocks).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("endstoneSmooth");
 		endstoneRefined = (new EDBlock(endstoneRefinedId, Material.rock, "block_EndstoneRefined")).setHardness(3.5F).setResistance(15.0F).setCreativeTab(tabDecorBlocks).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("endstoneRefined");
 		endstoneBrick = (new EDBlock(endstoneBrickId, Material.rock, "block_EndstoneBrick")).setHardness(3.5F).setResistance(15.0F).setCreativeTab(tabDecorBlocks).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("endstoneBrick");
 		
 		glassRefined = (new BlockGlassRefined(glassRefinedId, Material.glass, false)).setHardness(0.3F).setStepSound(Block.soundGlassFootstep).setCreativeTab(tabDecorBlocks).setUnlocalizedName("glassRefined");
 		glassRefinedPane = (new EDBlockPane(glassRefinedPaneId, "block_ClearGlass", "block_ClearGlassTop", Material.glass, false)).setHardness(0.3F).setStepSound(Block.soundGlassFootstep).setCreativeTab(tabDecorBlocks).setUnlocalizedName("glassRefinedPane");
 		
 		flintBlock = (new EDBlock(flintBlockId, Material.rock, "block_FlintBlock")).setHardness(3F).setResistance(10.0F).setStepSound(Block.soundMetalFootstep).setUnlocalizedName("flintBlock");
 		
 		gunpowderBlock = (new BlockGunpowderBlock(gunpowderBlockId, Material.ground, "block_GunpowderBlock")).setHardness(0.6F).setStepSound(Block.soundGravelFootstep).setUnlocalizedName("gunpowderBlock");
 		
 		rope = (new BlockRope(ropeId)).setHardness(0.9F).setStepSound(Block.soundClothFootstep).setUnlocalizedName("rope");
 		itemRope = (new ItemBlockPlacer(itemRopeId, "item_Rope", rope)).setUnlocalizedName("itemRope").setCreativeTab(tabDecorBlocks);
 		ropeCoil = (new BlockRopeCoil(ropeCoilId)).setHardness(0.9F).setCreativeTab(tabDecorBlocks).setStepSound(Block.soundClothFootstep).setUnlocalizedName("ropeCoil");
 		
 		oozeSlime = (new BlockOoze(oozeSlimeId, this.materialOoze, "block_SlimeOoze")).setHardness(1F).setResistance(2000F).setStepSound(Block.soundClothFootstep).setUnlocalizedName("oozeSlime");
 		
 		woodPanel = (new BlockWoodPanel(woodPanelId)).setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundWoodFootstep).setUnlocalizedName("woodPanel");
 			
 		woodBeveled = (new BlockWoodBeveled(woodBeveledId)).setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundWoodFootstep).setUnlocalizedName("woodBeveled");
 		
 		netherBrickPattern = (new EDBlock(netherBrickPatternId, Material.rock, "block_PatternBrickNether")).setHardness(2.0F).setResistance(10.0F).setCreativeTab(tabDecorBlocks).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("netherBrickPattern");
 		
 		sandstoneBricks = (new BlockSandstoneBrick(sandstoneBrickId)).setHardness(1F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("sandstoneBrick");
 		sandstonePillar = new BlockPillar(sandstonePillarId, "block_SandstonePillarSide", "block_SandstonePillarTop").setUnlocalizedName("sandstonePillar").setHardness(1F).setStepSound(Block.soundStoneFootstep);
 		
 		woodBoards = new BlockWoodBoards(woodBoardsId).setHardness(2.0F).setResistance(5.0F).setUnlocalizedName("woodBoards").setStepSound(Block.soundWoodFootstep);
 		//TODO add the rest of the boards after Extrapolated Nature
 		
 		flintTile = new EDBlock(flintTileId, Material.rock, "block_FlintTile").setUnlocalizedName("flintTile").setHardness(3F).setResistance(10.0F).setCreativeTab(tabDecorBlocks);
 		
 		netherQuartzTile = new EDBlock(netherQuartzTileId, Material.rock, "block_NetherTile").setHardness(0.8F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("netherQuartzTile");
 	
 		sugarBlock = new BlockSugarBlock(sugarBlockId, "block_SugarBlock").setHardness(0.6F).setStepSound(Block.soundGravelFootstep).setUnlocalizedName("sugarBlock");
 		meatBlock = new BlockMeatBlock(meatBlockId, "block_MeatBlock").setHardness(0.8F).setStepSound(Block.soundClothFootstep).setUnlocalizedName("meatBlock");
 		
 		magmaOoze = new BlockOoze(magmaOozeId, this.materialOoze, "block_MagmaOoze").setHardness(1F).setResistance(2000F).setStepSound(Block.soundClothFootstep).setUnlocalizedName("magmaOoze");
 		
 		enderBlock = new BlockEnderblock(enderBlockId).setHardness(3.0F).setResistance(4.0F).setUnlocalizedName("enderBlock").setStepSound(Block.soundGlassFootstep).setCreativeTab(tabDecorBlocks);
 		
 		crate = new BlockCrate(crateId).setHardness(2.5F).setStepSound(Block.soundWoodFootstep).setCreativeTab(tabDecorBlocks).setUnlocalizedName("crate");
 		barrel = new BlockBarrel(barrelId).setHardness(3F).setResistance(6.0F).setStepSound(Block.soundWoodFootstep).setCreativeTab(tabDecorBlocks).setUnlocalizedName("barrel");
 		
 		cardboard = new EDBlockPane(cardboardId, "block_CardboardBlock", "block_CardboardEdge", Material.cloth, true).setHardness(0.3F).setUnlocalizedName("cardboard");
 		cardboardItem = new ItemBlockPlacer(cardboardItemId, "item_Cardboard", cardboard).setCreativeTab(tabDecorBlocks).setUnlocalizedName("cardboardItem");
 		cardboardBlock = new BlockCardboard(cardboardBlockId, Material.cloth, "block_CardboardBlock").setHardness(0.4F).setUnlocalizedName("cardboardBlock").setCreativeTab(tabDecorBlocks);
 		cardboardWet = new BlockCardboardWet(cardboardWetId, Material.cloth).setCreativeTab(tabDecorBlocks).setHardness(0.2F).setResistance(0.8F).setUnlocalizedName("cardboardWet");
 		
 		sandstoneBrickItem = new EDItem(sandstoneBrickItemId, "item_SandstoneBrick").setUnlocalizedName("sandstoneBrickItem");
 		stoneBrickItem = new EDItem(stoneBrickItemId, "item_StoneBrick").setUnlocalizedName("stoneBrickItem");
 		
 		checkerTile = new EDBlock(checkerTileId, Material.rock, "block_CheckerTile").setUnlocalizedName("checkerTile").setHardness(3F).setResistance(10.0F).setStepSound(Block.soundStoneFootstep);
 		
 		woodBoardsStairsOak = new EDBlockStairs(woodBoardsStairsOakId, woodBoards, 0).setUnlocalizedName("woodBoardsStairsOak").setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundWoodFootstep);
 		woodBoardsStairsBirch = new EDBlockStairs(woodBoardsStairsBirchId, woodBoards, 1).setUnlocalizedName("woodBoardsStairsBirch").setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundWoodFootstep);
 		woodBoardsStairsSpruce = new EDBlockStairs(woodBoardsStairsSpruceId, woodBoards, 2).setUnlocalizedName("woodBoardsStairsSpruce").setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundWoodFootstep);
 		woodBoardsStairsJungle = new EDBlockStairs(woodBoardsStairsJungleId, woodBoards, 3).setUnlocalizedName("woodBoardsStairsJungle").setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundWoodFootstep);
 		woodBoardsSingleSlab = (BlockHalfSlab) new BlockWoodBoardSlab(woodBoardsSingleSlabId, false).setUnlocalizedName("woodBoardsSingleSlab").setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundWoodFootstep);
 		woodBoardsDoubleSlab = (BlockHalfSlab) new BlockWoodBoardSlab(woodBoardsDoubleSlabId, true).setUnlocalizedName("woodBoardsSingleSlab").setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundWoodFootstep);
 	
 		checkerTileStairs = new EDBlockStairs(checkerTileStairsId, checkerTile, 0).setUnlocalizedName("checkerTileStairs").setHardness(3F).setResistance(10.0F).setStepSound(Block.soundStoneFootstep);
 		
 		EDBlockRegistry.registerBlocks();
 		EDNameManager.registerNames();
 		EDRecipeManager.loadAllRecipes();
 			
 	}
 	
 	
 	@Mod.EventHandler
 	public void load(FMLInitializationEvent event)
 	{
 		proxy.registerRenderThings();
 				
 		NetworkRegistry.instance().registerGuiHandler(this, new EDGuiHandler());
 		GameRegistry.registerTileEntity(TileEntityCrate.class, "tileEntityCrate");
 		
 		MinecraftForge.setToolClass(Item.shears, "shears", 0);
 		
 		MinecraftForge.setBlockHarvestLevel(snowBrick, "shovel", 0);
 		MinecraftForge.setBlockHarvestLevel(flintBlock, "pickaxe", 1);
 		MinecraftForge.setBlockHarvestLevel(obsidianTile, "pickaxe", 3);
 		MinecraftForge.setBlockHarvestLevel(gunpowderBlock, "shovel", 0);
 		MinecraftForge.setBlockHarvestLevel(sugarBlock, "shovel", 0);
 		MinecraftForge.setBlockHarvestLevel(ropeCoil, "shears", 0);
 		MinecraftForge.setBlockHarvestLevel(rope, "shears", 0);
 		MinecraftForge.setBlockHarvestLevel(flintTile, "pickaxe", 1);
 		MinecraftForge.setBlockHarvestLevel(oozeSlime, "shovel", 0);
 		MinecraftForge.setBlockHarvestLevel(magmaOoze, "shovel", 0);
 		
 		GameRegistry.registerWorldGenerator(new EDOreGenerator());
 		
 	}
 
 	@Mod.EventHandler
     public void postLoad(FMLPostInitializationEvent evt)
 	{
 		Item.itemsList[stoneLamp.blockID] = (new ItemMultiTextureTile(stoneLamp.blockID - 256, stoneLamp, BlockStoneLamp.lampType)).setUnlocalizedName("stoneLamp");
 		Item.itemsList[woodPanel.blockID] = (new ItemMultiTextureTile(woodPanel.blockID - 256, woodPanel, BlockWoodPanel.woodType)).setUnlocalizedName("woodPanel");
 		Item.itemsList[woodBeveled.blockID] = (new ItemMultiTextureTile(woodBeveled.blockID - 256, woodBeveled, BlockWoodBeveled.woodType)).setUnlocalizedName("woodBeveled");
 		Item.itemsList[sandstoneBricks.blockID] = (new ItemMultiTextureTile(sandstoneBricks.blockID - 256, sandstoneBricks, BlockSandstoneBrick.sandType)).setUnlocalizedName("sandstoneBrick");
 		Item.itemsList[woodBoards.blockID] = (new ItemMultiTextureTile(woodBoards.blockID - 256, woodBoards, BlockWoodBoards.woodType)).setUnlocalizedName("woodBoards");
 		
 		//TODO Forms of adding slabs
 		Item.itemsList[this.woodBoardsSingleSlab.blockID] = (new ItemSlab(this.woodBoardsSingleSlab.blockID - 256, (BlockHalfSlab)woodBoardsSingleSlab, (BlockHalfSlab)woodBoardsDoubleSlab, false));
 		
 		try 
 		{
 			BridgeRecipes.loadBridgeRecipes();
 		} catch (Exception e) 
 		{
 			System.err.println("ExtraDecor: Could not load bridge recipes. Heres why: ");
 			e.printStackTrace();
 		}
 	}
 }
