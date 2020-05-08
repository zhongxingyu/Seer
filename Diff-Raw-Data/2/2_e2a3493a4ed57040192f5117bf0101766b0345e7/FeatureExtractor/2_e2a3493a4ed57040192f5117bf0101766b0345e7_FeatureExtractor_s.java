 package naive_bayes;
 import java.util.BitSet;
 
 import ai_util.LogFile;
 import arimaa3.*;
 
 public class FeatureExtractor{
 	
 	private static final int NUM_FEATURES = 1040; //TODO update this as you add more features
 	private static final int NUM_SRC_MVMT = 512;
 	private static final int NUM_LOCATIONS = 32;
 	private static final int NUM_PIECE_TYPES = 8;
 	private static final int DEFAULT_CAPTURE_LOCATION = 32;
 	
 	/* Move for which we are extracting features. This changes every time extractFeatures() is called. */
 	private ArimaaMove current_move;
 	
 	/* Game state resulting from current_move. This changes every time extractFeatures() is called. */
 	private GameState curr;
 	
 	/* Starting game state from which we play current_move */
 	private GameState prev;
 	
 	private GameState prev_prev;
 	
 	/* Piece id to type mapping for the current game state (curr). E.g. if piece_types[3] = 4, this means
 	 * that piece id 3 (black cat) is of piece type 4 for the current GameState. This changes every time 
 	 * extractFeatures() is called. */
 	private byte piece_types[];
 	
 	/* The feature vector corresponding to current_move. */ 
 	private BitSet featureVector;
 		
 	public FeatureExtractor(GameState prev, GameState prev_prev) {
 		this.prev = prev;
 		this.prev_prev = null;
 		curr = null;
 		featureVector = null;
 		current_move = null;
 		piece_types = null; 
 	}
 	
 	/*
 	 * Extracts features for the resulting board after playing a possible legal move.
 	 * current_board is the resulting board after playing current_move on prev game state.
 	 * 
 	 * NOTE: none of this has been tested
 	 */
 	public BitSet extractFeatures(ArimaaMove current_move){
 		featureVector = new BitSet(NUM_FEATURES);
 		piece_types = new byte[12];
 
 		this.current_move = current_move;
 
 		// Generate the current game state by applying move on the previous game state
 		curr = new GameState();
 		curr.play(current_move, prev);
 		calculatePieceTypes();
 
 		// feature extraction subroutine calls here
 		
 		generateMovementFeatures();
 		return featureVector;
 	}
 	
 
 	private void generateMovementFeatures() {
 		
 		long[] move_bb = current_move.piece_bb;
 		for(int i = 0; i < 12; i++) {
 			long source = curr.piece_bb[i] & move_bb[i]; // this long encodes all the source locations of pieces that moved this turn
 			long dest = source ^ move_bb[i]; // this long encodes all destination locations of pieces that moved this turn
 			int player = (i % 2==0) ? 0 : 1; // player is 0 (white) if piece_id is even
 			updateBitSetMovementFeatures(source, dest, piece_types[i], player);
 		}
 	}
 
 	private void updateBitSetMovementFeatures(long source, long dest, byte piece_type, int player) {
 		// higher rows are at the most significant bits		
 		// Loop across each of the 64 bits in source and dest, and set features on featureVector accordingly. 
 		for(int i = 0; i < Long.SIZE; i++) {
 			if((source & (1L << i)) > 0) { // a piece moved from location 'i'
 				setSrcMovementFeature(player, piece_type, getLocation(i));
 			}
 			if((dest & (1L << i)) > 0) { // a piece moved to location 'i'
 				setDestMovementFeature(player, piece_type, getLocation(i));
 			}
 		}
 		if(countOneBits(source) > countOneBits(dest)) { // piece has been captured
 			setDestMovementFeature(player, piece_type, DEFAULT_CAPTURE_LOCATION);
 		}
 	}
 	
 	private void setSrcMovementFeature(int player, int piece_type, int location) {
 		int index = player*NUM_PIECE_TYPES*NUM_LOCATIONS + piece_type*NUM_LOCATIONS + location;
 		featureVector.set(index);
 	}
 	
 	private void setDestMovementFeature(int player, int piece_type, int location) {
 		int index = NUM_SRC_MVMT + player*NUM_PIECE_TYPES*(NUM_LOCATIONS+1) + piece_type*(NUM_LOCATIONS+1) + location;
 		featureVector.set(index);
 	}
 	
 	/*
 	 * Maps from board index between 0-63 to board index between 0-31, leveraging 
 	 * the vertical symmetry of the board. 
 	 */
 	private static int getLocation(int index) {
 		int row = index >> 3;
 		index = index & 0x07; //reduces all rows to the same indices as first row
 		index = (index > 3) ? 7 - index : index;
		return index + row * 4;
 	}
 
 	// Calculates the piece type (e.g. 3) for each piece id (e.g. black dog) for the current game state.
 	private void calculatePieceTypes(){
 		
 		for (int i = 0; i < 2; i++){ // calculate for rabbits 
 			byte numStronger = countOneBits(curr.stronger_enemy_bb[i]);
 			if (numStronger < 5)
 				piece_types[i] = 7;
 			else if (numStronger < 7)
 				piece_types[i] = 6;
 			else
 				piece_types[i] = 5;
 		}
 		
 		for (int i = 2; i < 12; i++){ // calculate for non-rabbits
 			byte numStronger = countOneBits(curr.stronger_enemy_bb[i]);
 			switch (numStronger) {
 				case 0: piece_types[i] = 0; break;
 				case 1: piece_types[i] = 1; break;
 				case 2: piece_types[i] = 2; break;
 				case 3: case 4: piece_types[i] = 3; break;
 				default: piece_types[i] = 4; break;
 			}
 		}
 	}
 	
 	private static byte countOneBits(long n) {
 		// Algorithm adapted from http://graphics.stanford.edu/~seander/bithacks.html#CountBitsSetKernighan
 		byte c; // c accumulates the total bits set in v
 		for (c = 0; n != 0; c++)
 			n &= n - 1; // clear the least significant bit set
 		return c;
 	}
 	
 	
 	private static String tests[] = {
 		"12w %13 +-----------------+%138|                 |%137|                 |%136|   R             |%135|                 |%134|                 |%133|         D   r   |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
 		"12w %13 +-----------------+%138|                 |%137|                 |%136|   R             |%135|                 |%134|                 |%133|             r   |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
 		"2w %13 +-----------------+%138| r r   H r r r r |%137|   e   C r   r   |%136|   d X     X     |%135|   d R M c       |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
 	};
 
 
 	private static void testMovementFeatures(){
 		
 		// data from first game in "games" relation
 	    String white = "1w Ee2 Md2 Ha2 Hh2 Db2 Dg2 Cf2 Cc1 Ra1 Rb1 Rd1 Re1 Rf1 Rg1 Rh1 Rc2"; 
 	    String black = "1b ee7 md7 ch8 ca8 dc7 hb7 hg7 df7 ra7 rh7 rb8 rc8 rd8 re8 rf8 rg8";
 	    GameState startState = new GameState(white,black);
 	    System.out.println(startState.toBoardString());
 
 	    FeatureExtractor fe = new FeatureExtractor(startState, null);
 
 	    fe.extractFeatures(new ArimaaMove("Db2n Ha2n Ha3n Hh2n"));	
 	    System.out.println(fe.featureVector.toString());
 	    
 	    System.out.println(fe.curr.toBoardString());
 
 
 	    
 //	    for (String text : tests) {
 //		      GameState position = new GameState(text);
 //		      System.out.println(position.toBoardString());
 ////		      fe.extractFeatures(position);
 //	    }
 	}
 	
 	private static void testLocationMappings(){
 		// VM needs argument -ea for asserts to be enabled
 		assert(getLocation(0) == 0); 
 		assert(getLocation(5) == 2);
 		assert(getLocation(10) == 6);
 		assert(getLocation(15) == 4);
 		assert(getLocation(57) == 29);
 	}
 	
 	public static void main(String[] args){
 		testLocationMappings();
 		testMovementFeatures();
 
 	}
 	
 }
