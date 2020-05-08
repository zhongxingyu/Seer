 package feature_extractor;
 
 import java.util.BitSet;
 
import arimaa3.ArimaaMove;
 import arimaa3.GameState;
 
 public class SteppingOnTrapsExtractor extends AbstractExtractor {
 
 	/** 0 = lower left, 1 = upper left, 2 = lower right, 3 = upper right <br>
 	 * areTrapsSafe[k] contains whether the k-th trap is safe (true == safe) */
 	private boolean[] areTrapsSafe;
 	
 	/** The bit-boards of moved pieces (1s at locations where new pieces showed up)<br> 
 	 * <i>[This description is awkward to emphasize that movedPieces does not sense (as currently
 	 * implemented) replacements on a given square by a piece of the same type]</i>*/
 	private long[] movedPieces;
 	
 	/** Stores the Wu-piece-type (defined by Wu) for each Arimaa piece type 
 	 * (see FeatureConstants.SteppingOnTraps) */
 	private byte[] pieceTypes;
 	
 	/* Excerpt from Wu on Stepping On Traps
 	 * ------------------------------------
 	 * Voluntarily stepping on a trap square can be dangerous when the trap has too few defenders.
 	 * However, it can also be good to place a piece on a trap square because it allows that piece in
 	 * the future to access any of the four surrounding squares for defending that trap, and a piece on
 	 * a trap is hard to push away.
 	 * 
 	 * We heuristically define a "safe" trap for a player as one that is guarded either by two defenders
 	 * of that player, or the elephant. Otherwise, the trap is "unsafe."
 	 * 
 	 * 		UNSAFE STEP ON TRAP(trap,type): Piece of type type (0-7) stepped on unsafe trap
 	 * 			trap (0-3). (32 features)
 	 * 		SAFE STEP ON TRAP(trap,type): Piece of type type (0-7) stepped on safe trap
 	 * 			trap (0-3). (32 features)
 	 */
 
 	/** @param pieceTypesToCopy there should be a Wu-piece-type for all (12) of the piece types.
 	 * It probably makes sense that this be the pieceTypes for curr, rather than for prev */
 	public SteppingOnTrapsExtractor(GameState prev, GameState curr, byte[] pieceTypesToCopy) {
 		int player = prev.player; //player who played the move
 		
 		areTrapsSafe = initTrapSafety(player, curr);
 		movedPieces = getMovedPieces(player, prev.piece_bb, curr.piece_bb);
 		pieceTypes = getPieceTypesForPlayer(player, pieceTypesToCopy);
 	}
 	
 	
 	/** The portion of the BitSet that will be updated is STEPPING_TRAP_END - STEPPING_TRAP_START
 	 *  + 1 (== 64) bits. These bits will be split up as follows:
 	 *  <br> - Bits 0 - 31 cover unsafe trap-stepping
 	 *  <br> - Bits 32-63 cover safe trap-stepping
 	 *  <br> - Within each 32 bits, every 8 bits will be dedicated to a trap.
 	 *  <br> - -> The 8 bits cover the type [type0, type1, ..., type7]
 	 *  <br> - -> e.g. if type3 steps unsafely on the upper left trap, then the bit set is:
 	 *  <br> - ->      0 * 32 + 1 * 8 + 3 = 11 [where 1 is the number of the upper left trap]
 	 *  */
 	@Override
 	public void updateBitSet(BitSet bitset) {
 		int numPieceTypes = 8; //according to David Wu's piece type hierarchy/definition
 		
 		for (int trap = 0; trap < TRAP.length; trap++) {
 			int trapOffset = trap * numPieceTypes;
 			int safetyOffset = areTrapsSafe[trap] ? 32 : 0; //safe traps are offset by 32 bits
 			int totalOffset = startOfRange() + safetyOffset + trapOffset; 
 					//the base bit number (i.e. correct for type0) 
 			
 			for (int board = 0; board < movedPieces.length; board++) {
 				boolean onTrap = (movedPieces[board] & TRAP[trap]) != 0; //a moved-piece lives on a trap
 				if (onTrap) {
 					int pieceType = pieceTypes[board];
 					bitset.set(pieceType + totalOffset);
 					
 					int numFeatures = endOfRange() - startOfRange() + 1;
 					assert(pieceType + trapOffset + safetyOffset < numFeatures); //this should never go off...
 				}
 			}
 		}
 	}
 
 	@Override
 	public int startOfRange() {
 		return FeatureRange.STEPPING_TRAP_START;
 	}
 
 	
 	@Override
 	public int endOfRange() {
 		return FeatureRange.STEPPING_TRAP_END;
 	}
 	
 	/** Returns a board with 1s where moved pieces are.
 	 * (See movedPieces' comment in class for more info.) */
 	public long getMovedPieces() { //for testing
 		return getPieceBB(movedPieces);
 	}
 	
 	/** Returns safety of the trap at index */
 	public boolean getTrapSafety(int index) { //for testing
 		return areTrapsSafe[index];
 	}
 	
 	
 	/** Determines trap safeties for a player and returns a boolean array 
 	 * correspondingly (true == safe) */
 	private boolean[] initTrapSafety(int player, GameState curr) {
 		boolean[] trapSafety = new boolean[TRAP.length];
 		for (int trap = 0; trap < trapSafety.length; trap++) {
 			// using curr, since safety should be judged at end of move (agreed?)
 			trapSafety[trap] = getTrapStatus(curr, trap, player) >= TrapStatus.ONE_E; 
 			//TODO: merge with TrapExtractor's method?
 		}
 		
 		return trapSafety;
 	}
 	
 	/** Returns bit-boards representing (for a player) locations of only moved pieces */
 	private long[] getMovedPieces(int player, final long[] preMoveBBs, final long[] postMoveBBs) {
 		long[] movedPiecesBB = new long[6]; //12 / 2 -- num of player's boards
 		
 		for (int i = player; i < 12; i += 2) {
 			/* Find the difference the move caused (~pieceBBBeforeMove has 1s exactly where
 		     * there weren't pieces before)--this is per piece*/
 			long pieceBBBeforeMove = preMoveBBs[i];
 		    long pieceBBAfterMove = postMoveBBs[i];
 		    
 		    //NOTE: this does not capture whether a given piece type replaces that same type on the trap
 			movedPiecesBB[i/2] = pieceBBAfterMove & ~pieceBBBeforeMove;
 		}
 		
 		return movedPiecesBB;
 	}
 	
 	/** Copies into a new array only the pieceTypes relevant to player player */
 	private byte[] getPieceTypesForPlayer(int player, byte[] pieceTypesToCopy) {
 		byte[] pieceTypes = new byte[pieceTypesToCopy.length / 2];
 		for (int i = player; i < 12; i += 2) 
 			pieceTypes[i / 2] = pieceTypesToCopy[i];
 		
 		return pieceTypes;
 	}
 	
 	/** Returns a byte containing one of the TrapStatus.NUM_STATUSES possible
 	 * trap statuses (number of pieces on trap, elephant?, etc).
 	 * @param state The current board
 	 * @param trapNum Lower left, upper left, lower right, upper right
 	 * @param playerNum PL_WHITE, PL_BLACK */
 	private byte getTrapStatus(GameState state, int trapNum, int playerNum) {
 		long player_trap_bb = TOUCH_TRAP[trapNum] & state.colour_bb[playerNum]; // all white or black pieces touching trap #trapNum
 		byte numPieces = FeatureExtractor.countOneBits(player_trap_bb);
 		assert(numPieces <= 4); //shouldn't be more than 4 pieces touching a trap...
 		
 		boolean elephant = isElephantTouchingTrap(state, trapNum, playerNum);
 		return TrapStatus.convertToStatus(numPieces, elephant);
 	}
 	
 	/** Returns whether an elephant for the given player (in the current game state)
 	 * is on any of the four spaces adjacent to the given trap. */
 	private boolean isElephantTouchingTrap(GameState state, int trapNum, int playerNum) { //remove static once done testing, make private
 		int boardNum = playerNum == PL_WHITE ? PT_WHITE_ELEPHANT : PT_BLACK_ELEPHANT;
 		long elephant_bb = state.piece_bb[boardNum];
 	
 		return (elephant_bb & TOUCH_TRAP[trapNum]) != 0;
 	}
 	
 	
 	/** Returns a bit-board with 1s in positions where any bitboard has a piece */
 	private long getPieceBB(long[] bitboards) {
 		long bitboard = 0L;
 		for (int i = 0; i < bitboards.length; i++) //look only at bitboards for the player
 			bitboard |= bitboards[i];
 		
 		return bitboard;
 	}
 }
