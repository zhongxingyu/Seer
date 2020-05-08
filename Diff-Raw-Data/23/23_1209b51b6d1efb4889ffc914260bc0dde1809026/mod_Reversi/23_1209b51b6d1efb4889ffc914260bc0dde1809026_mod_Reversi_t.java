 package net.minecraft.src;
 public class mod_Reversi extends BaseMod {
 
 	public static int BLOCK_CORE_ID = 245;
 	public static int BLOCK_BLACK_ID = 246;
 	public static int BLOCK_WHITE_ID = 247;
 	public static int AIR_ID = 0;
 	public static int FIELD_X = 8;
 	public static int FIELD_Z = 8;
 	public static Block blockReversiCore;
 	public static Block blockReversiBlack;
 	public static Block blockReversiWhite;
 	
 	public mod_Reversi(){
 		super();
 	}
 
 	@Override
 	public String getVersion() {
 		return "1.0.0";
 	}
 
 	@Override
 	public void load() {
 		addReversiCore();
 		addReversiPieceB();
 		addReversiPieceW();
 	}
 
 	private void addReversiCore() {
 		Block cBlock = new BlockReversiCore(
 				BLOCK_CORE_ID,
 				ModLoader.addOverride("/terrain.png", "/reversi/blockReversi.png")
 				);
 		setPieceConfig(cBlock, "Reversi Core");
 		ModLoader.addRecipe(new ItemStack(cBlock, 1), new Object[] { "XY","YX","ZZ",
 				Character.valueOf('X'), Block.stone, 
 				Character.valueOf('Y'), Block.planks,
 				Character.valueOf('Z'), Item.redstone});
 		blockReversiCore = cBlock;
 	}
 
 	private void addReversiPieceB() {
 		Block bBlock = new BlockReversiPieceB(
 				BLOCK_BLACK_ID,
 				ModLoader.addOverride("/terrain.png", "/reversi/blockReversiB.png")
 				);
 		setPieceConfig(bBlock, "Reversi Black Piece");
 
 		ModLoader.addRecipe(new ItemStack(bBlock, 1), new Object[] { "XX",
 			Character.valueOf('X'), Block.dirt });
 		blockReversiBlack = bBlock;
 	}
 
 	private void addReversiPieceW(){
 		Block wBlock = new BlockReversiPieceW(
 				BLOCK_WHITE_ID,
 				ModLoader.addOverride("/terrain.png", "/reversi/blockReversiW.png")
 				);
 		setPieceConfig(wBlock, "Reversi White Piece");
 
 		ModLoader.addRecipe(new ItemStack(wBlock, 1), new Object[] { "X","X",
 			Character.valueOf('X'), Block.dirt });
 		blockReversiWhite = wBlock;
 	}
 
 	private void setPieceConfig(Block block, String blockName) {
 		block.setHardness(2F);
 		block.setBlockName(blockName);
 		block.setResistance(2F);
 		block.setStepSound(Block.soundWoodFootstep);
 		block.setLightValue(1F);
 		ModLoader.registerBlock(block);
 		ModLoader.addName(block, blockName);
 	}
 
 
 
 }
