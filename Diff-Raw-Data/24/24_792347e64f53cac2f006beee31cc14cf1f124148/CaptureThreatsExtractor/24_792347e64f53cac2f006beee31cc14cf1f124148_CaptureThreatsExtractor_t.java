 package feature_extractor;
 
 import java.util.BitSet;
 
 import utilities.helper_classes.Utilities;
 import ai_util.Util;
 import arimaa3.ArimaaMove;
 import arimaa3.GameState;
 import arimaa3.GenCaptures;
 import arimaa3.MoveList;
 
 /** Make this class as efficient as possible--I can totally see this being a bottleneck. */
 public class CaptureThreatsExtractor extends AbstractExtractor {
 	
 	private GameState prev, curr;
 	
 	/* Excerpt from Wu on Capture Threats
 	 * ----------------------------------
 	 * Threatening and defending the capture of a piece plays a key role in many tactics.
 	 *		THREATENS CAP(type,s,trap): Threatens capture of opposing piece of type type (0-7)
 	 *			in s (1-4) steps , in trap trap (0-3). (128 features)
 	 *		INVITES CAP MOVED(type,s,trap): Moves own piece of type type (0-7) so that it can be
 	 *		 	captured by opponent in s (1-4) steps , in trap trap (0-3). (128 features)
 	 *		INVITES CAP UNMOVED(type,s,trap): Own piece of type type (0-7) can now be captured by
 	 *			opponent in s (1-4) steps, in trap trap (0-3), but it was not itself moved. 
 	 *			(128 features)
 	 *		PREVENTS CAP(type,loc): Removes the threat of capture from own piece of type type (0-7)
 	 *			at location loc (0-31). (256 features)
 	 *			
 	 *	The way in which one defends the capture of a piece is also important.
 	 *		CAP DEF ELE(trap,s): Defends own piece otherwise capturable in s (1-4) steps in trap 
 	 *			trap (0-3) by using the elephant as a trap defender. (16 features)
 	 *		CAP DEF OTHER(trap,s): Defends own piece otherwise capturable in s (1-4) steps in trap
 	 *			trap (0-3) by using a non-elephant piece as a trap defender. (16 features)
 	 *		CAP DEF RUNAWAY(trap,s): Defends own piece otherwise capturable in s (1-4) steps in trap
 	 *			trap (0-3) by making the threatened piece run away. (16 features)
 	 *		CAP DEF INTERFERE(trap,s): Defends own piece otherwise capturable in s (1-4) steps in trap
 	 *			trap (0-3) by freezing or blocking the threatening piece. (16 features)
 	 */
 	
 	/** Extracts Capture Threats information, using prev and curr 
 	 * @param prev The previous game state (leading to curr)
 	 * @param curr The current game state to be analyzed */
 	public CaptureThreatsExtractor(GameState prev, GameState curr) {
 		this.prev = prev;
 		this.curr = curr;
 	}
 	
 	
 	/** The portion of the BitSet that will be updated is CAPTURE_THREATS_END - CAPTURE_THREATS_START
 	 *  + 1 (== 704) bits. These bits will be split up into categories according to the constants
 	 *  in `FeatureConstants.CaptureThreats`.
 	 *  */
 	@Override
 	public void updateBitSet(BitSet bitset) {
 		//might need to have some more information passed around later on...
 		threatensCap(bitset); 
		System.gc(); //suggest to the JVM that it clean up this huge moveList...
 	}
 
 	@Override
 	public int startOfRange() {
 		return FeatureRange.CAPTURE_THREATS_START;
 	}
 
 	
 	@Override
 	public int endOfRange() {
 		return FeatureRange.CAPTURE_THREATS_END;
 	}
 	
 	
 	
 	
 	
 	
 	// ===========================================================================================
 	// ========================== PRIVATE METHODS -- Read no further! :D =========================
 	// ===========================================================================================
 	
	private static final int NUM_THREAT_CAP_FEATURES = CaptureThreats.INVITES_CAP_MOVED_OFFSET - CaptureThreats.THREATENS_CAP_OFFSET;
 	
 	/* *************************************************
 	 * ********** START Threatens Cap Methods ********** 
 	 * ************************************************* */
 	/** Overloaded method-wrapper to give default bitset offset
 	 * (See other method below for more comments). */
 	private void threatensCap(BitSet bitset) {
 		threatensCap(curr, bitset, CaptureThreats.THREATENS_CAP_OFFSET);
 	}
 	
 	/** Updates the bitset with the THREATENS CAP features 
 	 * @param curr is the GameState after the move is made (will be used to determine threats) 
 	 * @param bitset is the bitset to be updated (as per recordMove)
 	 * @param offset is the offset into the bit-vector where the bits will be updated <br>
 	 *   <i>[Added mainly to support using this method later to update small bitsets (offset 0)
 	 *   to pass information to other methods.]</i>*/
 	private void threatensCap(GameState curr, BitSet bitset, int offset) {
 		int neemaSize = 4000; //try Neema's size first -- to save memory and computation time!
 		int jeffBachersSize = 400000; //upper bound provided by Jeff
 		
 		boolean completeTurn = false; //allows us to get captures with fewer than 4 moves
 		MoveList moveList = new MoveList(neemaSize); //will be populated with all capture threats
 		
 		GenCaptures captures = new GenCaptures();
 		
 		/* Play a pass-move for the opponent in order to see threats made by original move 
 		 * (we need to check possible captures for the player who just played) */
 		int opponent = curr.player;
 		GameState currOppPass = new GameState();
 		currOppPass.playPASS(curr); //copies curr into currPassOpp before playing pass
		
 		try {
 			captures.genCaptures(currOppPass, moveList, completeTurn); //fills moveList with capture threats
 		}
 		catch (ArrayIndexOutOfBoundsException e) {
 			moveList = new MoveList(jeffBachersSize);
 			captures.genCaptures(currOppPass, moveList, completeTurn); //fills moveList with capture threats
 		}
 		
 		
 		/* We want types before a potential capture (we're "threatening" to capture these types);
 		 * do it once outside the loop and pass in */
 		byte[] oppPieceTypes = SteppingOnTrapsExtractor.getPieceTypesForPlayer(
 									opponent, Utilities.calculatePieceTypes(curr)
 								); 
 		
 		for (ArimaaMove move : moveList) {//seems to avoid null moves...
 			GameState postCapture = new GameState();
 			postCapture.play(move, currOppPass); //was playFullClear -- no longer
 			recordCaptureMove(curr, postCapture, move.steps, bitset, offset, opponent, oppPieceTypes);
 		}
 	}
 	
	
 	 /** The bitset is updated as follows: <br>
 	 *  ->For each trap, for each step, the piece type. (32 * trap + 8 * step + type) <br> 
 	 *  -><i>[32 is the number of bits taken by all (step,type) combinations. 8 is the number</i>
 	 *  of bits taken by all types.] 
 	 *  @param preCapture the GameState before any capture (the "threatening, original" game state)
 	 *  @param postCapture the GameState after a capturing move has been played
 	 *  @param numSteps the number of steps in the move from pre to postCapture
 	 *  @param toUpdate the BitSet that will be updated (as above)
 	 *  @param bitOffset the offset into the BitSet relative to which point bits will be set
 	 *  @param opponent PL_WHITE or PL_BLACK (the opponent, whose piece are potentially captured)
 	 *  @param oppPieceTypes the Wu-piece-type array of <i>opponent</i> piece types
 	 *  */
 	/* Sorry for all of these parameters...but I didn't want to make assumptions and mess up
 	 * any recalculations of parameters, etc. 
 	 * Also, pardon the cryptic bit manipulations... */
 	private void recordCaptureMove(GameState preCapture, GameState postCapture, int numSteps,
						BitSet toUpdate, int bitOffset, int opponent, byte[] oppPieceTypes) {
		
		final int TRAP_SIZE = NUM_THREAT_CAP_FEATURES / TRAP.length;
		final int STEP_SIZE = TRAP_SIZE / 4; //4 is the max number of steps per move
 		
 		int capturesAndTraps = getCapturesFromStates(preCapture, postCapture,	opponent, oppPieceTypes);
 		
 		short captured = (short)capturesAndTraps; //truncate to get captures
 		short traps = (short)(capturesAndTraps >>> Short.SIZE); //shift to get traps
 		
 		while (captured != 0) { //keep "popping" most-significant ones until there aren't any left.			
 			int type = Util.FirstOne(captured);
 			captured ^= (1 << type); //turn off the type-bit just considered (faster than & ~?)
 			int trap = Util.FirstOne(traps);
 			traps ^= (1 << trap);
 			
 			if (type >= Byte.SIZE) type -= Byte.SIZE; // 0 <= type < Byte.Size (8)
 			if (trap >= Byte.SIZE) trap -= Byte.SIZE;
 			
			int bitToSet = bitOffset + TRAP_SIZE * trap + STEP_SIZE * (numSteps - 1) + type;
 			toUpdate.set(bitToSet);
 		}
 		
 	}
 	
 	
 	/** @return an int containing information as follows: <p>
 	 * The most-significant short contains the trap information. The least-significant
 	 * short contains the captures information.<br>
 	 * Within each short, the least-significant byte has the 1st capture (trap / piece type),
 	 * and the most-significant byte has the 2nd capture (if it exists). <br>
 	 * Piece types are set as follows: e.g. if a piece of "Type 2: Non-rabbit, 2 opponent
 	 * pieces stronger" is captured, then 1 << 2 (the 3rd bit) is set. Note that the two 
 	 * is successfully recovered by Util.FirstOne(int/long).<br>
 	 * Traps are also set intuitively: 1 << trap is set if trap \in {0, 1, 2, 3} was "used"
 	 * for the capture. ("Used" in quotes because of our approximation.) <p>
 	 * <i>(Note: impossible to have more than 2 captures)</i>
 	 * */
 	private int getCapturesFromStates(GameState preCapture, GameState postCapture,
 													int opponent, byte[] oppPieceTypes) {
 		// the idea is to store the traps and the captures, and then merge
 		// them into an integer before returning
 		short captures = 0;
 		short traps = 0;
 		
 		boolean alreadyCaptured = false;
 		for (int i = opponent; i < oppPieceTypes.length * 2; i += 2) {
 			int preCaptureCount = Util.PopCnt(preCapture.piece_bb[i]);
 			int postCaptureCount = Util.PopCnt(postCapture.piece_bb[i]);
 			int numCaptures = preCaptureCount - postCaptureCount; //numCaptures >= 0
 			
 			int pieceType = oppPieceTypes[i / 2];
 			
 			switch (numCaptures) {
 				case 0:
 					break;
 					
 				case 2:
 					captures |= (1 << (pieceType + Byte.SIZE)); 
 					int trap = getTrapNumber(preCapture, postCapture, i, false);
 					traps |= (1 << (trap + Byte.SIZE));
 					//fall through
 					
 				case 1:
 					trap = getTrapNumber(preCapture, postCapture, i, true);
 					if (alreadyCaptured) { //use the second byte
 						pieceType += Byte.SIZE;
 						trap += Byte.SIZE;
 					}
 					
 					captures |= (1 << pieceType);
 					traps |= (1 << trap);
 					alreadyCaptured = true;
 					break;
 			}
 		}
 		
 		return captures | (traps << Short.SIZE);
 	}
 	
 	/** Deduces the trap number used in capturing ... this seems impossible 
 	 * So we approximate: assume the closest trap to the capture! */
 	/* Fix this approximation and test it once we have the chance */
 	private int getTrapNumber(GameState preCapture, GameState postCapture, 
 										int arimaaPieceType, boolean firstPiece) {
 		
 		long preBB = preCapture.piece_bb[arimaaPieceType];
 		int indexOfCap = Util.FirstOne(preBB);
 		preBB ^= (1L << indexOfCap); //turn off that bit
 		if (firstPiece) assert(preBB == 0);
 		
 		if (!firstPiece) { //assume there were two captures, and get the second one
 			int indexOfCap2 = Util.FirstOne(preBB);
 			return nearestTrap(indexOfCap2);
 		}
 		
 		return nearestTrap(indexOfCap);
 	}
 	
 	/** Returns the nearest trap for a board index (0 - 63)*/
 	static int nearestTrap(int boardIndex) {
 		long index = 1L << boardIndex;
 		for (int trap = 0; trap < QUADRANT.length; trap++)
 			if ((QUADRANT[trap] & index) != 0) return trap;
 		
 		assert(false);
 		return -1;
 	}
 	/* *************************************************
 	 * *********** END Threatens Cap Methods *********** 
 	 * ************************************************* */
 	
 	// ===========================================================================================
 	
 	
 	/* *************************************************
	 * *********** START Invites Cap Methods *********** 
 	 * ************************************************* */
 
 	
 	//TODO: Implement more methods! :D
 	
 	
 	/* *************************************************
	 * ************ END Invites Cap Methods ************ 
 	 * ************************************************* */
 	
 	
 }
