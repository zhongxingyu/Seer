 package net.minecraft.src;
 //TODO core should interact with piece
 public class BlockReversiCore extends Block {
 	public static int FIELD_X = mod_Reversi.FIELD_X;
 	public static int FIELD_Z = mod_Reversi.FIELD_Z;
 	public static int WOOL_ID = Block.cloth.blockID; // FIELD BASE
 	public static int WOOL_META_LIME = 5;
 	public static int CORE_ID = mod_Reversi.BLOCK_CORE_ID;
 	public static int BLACK_ID = mod_Reversi.BLOCK_BLACK_ID;
 	public static int WHITE_ID = mod_Reversi.BLOCK_WHITE_ID;
 	public static int AIR_ID = mod_Reversi.AIR_ID;
 	public static int NONE = -1;
 	private int[][] gameMap;
 	private int Xcore;
 	private int Ycore;
 	private int Zcore;
 	// ==gameMap==
 	// 0 : No piece
 	// 1 : Black piece
 	// 2 : White piece
 
 	protected BlockReversiCore(int par1, int par2) {
 		super(par1, par2, Material.wood);
 	}
 
 	@Override
 	public void onBlockAdded(World world, int Xb, int Yb, int Zb) {
 		arrangeBlocks(world, Xb, Yb, Zb, WOOL_ID, WOOL_META_LIME);
 		int zeroCoordX = Xb - FIELD_X / 2;
 		int zeroCoordY = Yb + 1;
 		int zeroCoordZ = Zb - FIELD_Z / 2;
 		Xcore = Xb;
 		Ycore = Yb;
 		Zcore = Zb;
 
 		gameMap = new int[][] { { 0, 0, 0, 0, 0, 0, 0, 0 },
 				{ 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0 },
 				{ 0, 0, 0, 2, 1, 0, 0, 0 }, { 0, 0, 0, 1, 2, 0, 0, 0 },
 				{ 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0 },
 				{ 0, 0, 0, 0, 0, 0, 0, 0 } };
 
 		instanciateMap(world, gameMap, zeroCoordX, zeroCoordY, zeroCoordZ);
 	}
 
 	@Override
 	public void onBlockRemoval(World world, int Xb, int Yb, int Zb) {
 		arrangeBlocks(world, Xb, Yb, Zb, AIR_ID, NONE);
 		arrangeBlocks(world, Xb, Yb + 1, Zb, AIR_ID, NONE);
 	}
 	
 	public static void onPiecePlaced(World world, int pieceID, int x, int y, int z ){
 		if( ! BlockReversiCore.reversePieceFrom(world, pieceID, x, y, z) ){
 			world.setBlock(x, y, z, mod_Reversi.AIR_ID);
 		}
 	}
 	
 	// reversePieces as if a (black or white) piece is added at (world,x,y,z).
 	// return false if there are no pieces to reverse.
 	public static boolean reversePieceFrom(World world, int blockID, int x, int y, int z){
 		if( blockID != BLACK_ID && blockID != WHITE_ID){
 			return false;
 		}
 		boolean ret = false;
 		for( int i=-1 ; i <= 1; i++ ){
 			for( int j=-1 ; j <= 1 ; j++){
 				if( i== 0 && j == 0 ) continue;
				else if( conductLine(world, blockID, x, y, z, j, 0, i, 8) ){
 					ret = true;
 				}
 			}
 		}
 		return ret;
 	}
 	
 	// search to one direction whether pieces on line can be reversed.
 	// If so, reverse them and return true.
 	private static boolean conductLine(World world, int blockID, int Xfrom, int Yfrom, int Zfrom, int Xto, int Yto, int Zto, int dist){
		if( dist <= 0 ){
 			return false;
 		}
 		int blockNext = world.getBlockId(Xfrom+Xto, Yfrom+Yto, Zfrom+Zto);
 		if( blockNext != BLACK_ID && blockNext != WHITE_ID ){
 			return false;
 		}
 		else if(  blockNext == blockID ){
			return true;
 		}
		else if( conductLine(world, blockID, Xfrom+Xto, Yfrom+Yto, Zfrom+Zto, Xto, Yto, Zto, dist-1)){
 			world.setBlock(Xfrom+Xto, Yfrom+Yto, Zfrom+Zto, blockID);
 			return true;
 		}
 		else{
 			return false;
 		}
 	}
 	
 	private void instanciateMap(World world, int[][] map, int _zeroCoordX,
 			int _zeroCoordY, int _zeroCoordZ) {
 		for (int i = 0; i < FIELD_Z; i++) {
 			for (int j = 0; j < FIELD_X; j++) {
 				int x = _zeroCoordX + j;
 				int y = _zeroCoordY;
 				int z = _zeroCoordZ + i;
 				if (isRelatedToTheMod(world, x, y, z)) {
 					if (map[i][j] == 1) {
 						world.setBlock(x, y, z, BLACK_ID);
 					}
 					if (map[i][j] == 2) {
 						world.setBlock(x, y, z, WHITE_ID);
 					}
 				}
 			}
 		}
 	}
 
 	// Arrange blocks in a plane.
 	// if no need metaNumber , set minus number such as NONE.
 	private void arrangeBlocks(World world, int Xb, int Yb, int Zb,
 			int blockID, int metaNumber) {
 		for (int i = 0; i < FIELD_Z; i++) {
 			for (int j = 0; j < FIELD_X; j++) {
 				int Xa = Xb - FIELD_X / 2 + j;
 				int Ya = Yb;
 				int Za = Zb - FIELD_Z / 2 + i;
 				if ((Xb != Xcore || Yb != Ycore || Zb != Zcore)
 						&& isRelatedToTheMod(world, Xa, Ya, Za)) {
 					if (metaNumber >= 0) {
 						world.setBlockAndMetadata(Xa, Ya, Za, blockID,
 								metaNumber);
 					} else {
 						world.setBlock(Xa, Ya, Za, blockID);
 					}
 				}
 			}
 		}
 	}
 
 	// Whether block on (world,x,y,z) is related to the module.
 	private boolean isRelatedToTheMod(World world, int x, int y, int z) {
 		int blockID = world.getBlockId(x, y, z);
 		if (blockID == 0 || blockID == WOOL_ID || blockID == BLACK_ID
 				|| blockID == WHITE_ID) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 }
