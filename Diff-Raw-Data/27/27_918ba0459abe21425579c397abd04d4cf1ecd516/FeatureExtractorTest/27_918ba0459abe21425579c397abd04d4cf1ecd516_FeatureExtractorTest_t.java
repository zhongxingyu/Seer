 package feature_extractor;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.BitSet;
 
 import org.junit.Test;
 
 import arimaa3.ArimaaMove;
 import arimaa3.Constants;
 import arimaa3.GameState;
 
 public class FeatureExtractorTest implements Constants, FeatureConstants {
 
 	private static String tests[] = {
 		"12w %13 +-----------------+%138|                 |%137|                 |%136|   R             |%135|                 |%134|                 |%133|         D   r   |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
 		"12w %13 +-----------------+%138|                 |%137|                 |%136|   R             |%135|                 |%134|                 |%133|             r   |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
 		"2w %13 +-----------------+%138| r r   H r r r r |%137|   e   C r   r   |%136|   d X     X     |%135|   d R M c       |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
 	};
 	
 	@Test
 	public void testMovementFeatures1() {
 		
 		// data from first game in "games" relation
 	    String white = "1w Ee2 Md2 Ha2 Hh2 Db2 Dg2 Cf2 Cc1 Ra1 Rb1 Rd1 Re1 Rf1 Rg1 Rh1 Rc2"; 
 	    String black = "1b ee7 md7 ch8 ca8 dc7 hb7 hg7 df7 ra7 rh7 rb8 rc8 rd8 re8 rf8 rg8";
 	    GameState startState = new GameState(white,black);
 	    FeatureExtractor fe = new FeatureExtractor(startState, null);
	    BitSet bs = fe.extractFeatures(new ArimaaMove("Db2n Ha2n Ha3n Hh2n"));
 	    
 //	    System.out.println(fe.featureVector.toString());

	    assertTrue(bs.get(620));
	    assertTrue(bs.get(68));
	    assertTrue(bs.get(101));
	    assertTrue(bs.get(586));
	    assertTrue(bs.get(590));
 	    
 //	    System.out.println(startState.toBoardString());
 //	    System.out.println(fe.curr.toBoardString());
 	}
 	
 	@Test
 	public void testMovementFeatures2() {
 	    GameState startState = new GameState(tests[2]);
 	    FeatureExtractor fe = new FeatureExtractor(startState, null);
	    BitSet bs = fe.extractFeatures(new ArimaaMove("db6e dc6x ce5s ce4s ce3s"));
 
	    assertTrue(bs.get(339));
	    assertTrue(bs.get(341));
	    assertTrue(bs.get(849));
	    assertTrue(bs.get(874));
 	    
 //	    System.out.println(fe.featureVector.toString());
 //	    System.out.println(startState.toBoardString());
 //	    System.out.println(fe.curr.toBoardString());
 	}
 	
 	@Test
 	public void testLocationMappings(){
 
 		assertTrue(FeatureExtractor.getLocation(0) == 0); 
 		assertTrue(FeatureExtractor.getLocation(5) == 2);
 		assertTrue(FeatureExtractor.getLocation(10) == 6);
 		assertTrue(FeatureExtractor.getLocation(15) == 4);
 		assertTrue(FeatureExtractor.getLocation(57) == 29);
 	}
 	
 	
 	@Test
 	public void testElephantTouchingTraps() {
 		long neighboringTrapSpots = 0;
 		for (int trap = 0; trap < TOUCH_TRAP.length; trap++) 
 			neighboringTrapSpots |= TOUCH_TRAP[trap];
 		
 		GameState gsWhite = new GameState();
 		GameState gsBlack = new GameState();
 		
 		for (int board = 0; board < 64; board++) { //check 64 boards, elephant in different spot each time
 			gsWhite.piece_bb[PT_WHITE_ELEPHANT] = 1L << board;
 			gsBlack.piece_bb[PT_BLACK_ELEPHANT] = 1L << board;
 			
 			boolean whiteEleph = false, blackEleph = false;
 			for (int trap = 0; trap < 4; trap++) {
 				whiteEleph = whiteEleph || TrapExtractor.isElephantTouchingTrap(gsWhite, trap, PL_WHITE);
 				blackEleph = blackEleph || TrapExtractor.isElephantTouchingTrap(gsBlack, trap, PL_BLACK);
 			}
 			
 			boolean onTrap = (neighboringTrapSpots & (1L << board)) != 0;
 			assertTrue(onTrap == whiteEleph);
 			assertTrue(onTrap == blackEleph);
 		}
 	}
 	
 	@Test
 	public void testGetTrapStatus() {
 		// String array containing whiteMove, blackMove pairs (indices 2k, 2k+1)
 		String[] whitesBlacks = {  
 				"Eb3 Rc2", "ed3 rc4", //expect TWO_E == 4, //expect TWO_E == 4
 				"Rf6 Df5 Cf7", "rf6 df5 cf7", //expect TWO_NE == 3, //expect TWO_NE == 3
 				"Ec7 Dd6 Cc5", "rb6" //expect THREE_E == 6, //expect ONE_NE == 1
 		};
 		assertTrue(whitesBlacks.length % 2 == 0);
 		
 		byte[][] whitesBlacksStatuses = {
 				{TrapStatus.TWO_E, TrapStatus.ZERO, TrapStatus.ZERO, TrapStatus.ZERO}, {TrapStatus.TWO_E, TrapStatus.ZERO, TrapStatus.ZERO, TrapStatus.ZERO},
 				{TrapStatus.ZERO, TrapStatus.ZERO, TrapStatus.ZERO, TrapStatus.TWO_NE}, {TrapStatus.ZERO, TrapStatus.ZERO, TrapStatus.ZERO, TrapStatus.TWO_NE},
 				{TrapStatus.ZERO, TrapStatus.THREE_E, TrapStatus.ZERO, TrapStatus.ZERO}, {TrapStatus.ZERO, TrapStatus.ONE_NE, TrapStatus.ZERO, TrapStatus.ZERO}
 		};
 		assertTrue(whitesBlacksStatuses.length == whitesBlacks.length);
 		
 		ArrayList<GameState> gsArr = new ArrayList<GameState>();
 		for (int i = 0; i < whitesBlacks.length / 2; i++)
 			gsArr.add(new GameState(whitesBlacks[i * 2], whitesBlacks[i * 2 + 1]));
 		
 		int game = 0;
 		for (GameState gs : gsArr) {
 			int gameW = game * 2;
 			int gameB = gameW + 1;
 			for (int trap = 0; trap < 4; trap++) {
 				byte wStatus = TrapExtractor.getTrapStatus(gs, trap, PL_WHITE);
 				byte bStatus = TrapExtractor.getTrapStatus(gs, trap, PL_BLACK);
 				
 				assertTrue(wStatus == whitesBlacksStatuses[gameW][trap]);
 				assertTrue(bStatus == whitesBlacksStatuses[gameB][trap]);
 			}
 			game++;
 		}
 	}
 	
 	@Test
 	public void testBitCodes() { //TODO: Set the bit indices for the games--try different games?
 		BitSet bitset = new BitSet(FeatureRange.TRAP_STATUS_END + 1);
 		
 		String[] whitesBlacks = {  
 				"Eb3 Rc2", "ed3 rc4", //expect TWO_E == 4, //expect TWO_E == 4
 				"Rf6 Df5 Cf7", "rf6 df5 cf7", //expect TWO_NE == 3, //expect TWO_NE == 3
 				"Ec7 Dd6 Cc5", "rb6" //expect THREE_E == 6, //expect ONE_NE == 1
 		};
 		assertTrue(whitesBlacks.length % 2 == 0);
 		
 		int[][] bitIndices = { //each int[] should have 8 bits set, //white, black in each row
 				{/* 8 indices */}, {/* 8 indices */}, //DO MATH for transition from game 1 to game 2
 				{/* 8 indices */}, {/* 8 indices */}  //DO MATH for transition from game 2 to game 3
 		};
 		assertTrue(bitIndices.length == whitesBlacks.length - 2);
 				
 		ArrayList<GameState> gsArr = new ArrayList<GameState>();
 		for (int i = 0; i < whitesBlacks.length / 2; i++)
 			gsArr.add(new GameState(whitesBlacks[i * 2], whitesBlacks[i * 2 + 1]));
 		
 		
 		int bitOffset = FeatureRange.TRAP_STATUS_START;
 		int game = 0;
 		GameState prev = null;
 		for (GameState gs : gsArr) {
 			//if prev is not set, use the first game and start back at the top
 			if (prev == null) {
 				prev = gs;
 				continue;
 			}
 			int gameW = game * 2;
 			int gameB = gameW + 1;
 			
 			TrapExtractor te = new TrapExtractor(prev, gs);
 			te.updateBitSet(bitset);
 			assertTrue(bitset.cardinality() == 8);
 			
 			//check whiteBits
 			for (int bit = 0; bit < bitIndices[gameW].length; bit++) {
 				assertTrue(bitset.get(bitOffset + bitIndices[gameW][bit]));
 			}
 			//check blackBits
 			for (int bit = 0; bit < bitIndices[gameB].length; bit++) {
 				assertTrue(bitset.get(bitOffset + bitIndices[gameB][bit]));
 			}
 			
 			//handle increments / resets
 			bitset.clear();
 			prev = gs;
 			game++;
 		}
 		
 	}
 
 
 }
