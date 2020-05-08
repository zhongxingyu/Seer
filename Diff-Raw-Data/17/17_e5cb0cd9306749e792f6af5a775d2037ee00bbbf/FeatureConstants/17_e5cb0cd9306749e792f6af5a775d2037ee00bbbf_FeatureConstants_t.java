 package feature_extractor;
 
 import arimaa3.Constants;
 
 /** Extends Constants to add more constants (for the Feature Extractor in particular) */
public interface FeatureConstants extends Constants {
 	
 	/**
 	 * Status number -> trap "condition"
 	 * <br> 0		 -> 0 pieces around a trap
 	 * <br> 1		 -> 1 piece touching trap (`No `Elephant)
 	 * <br>	2		 -> 1 piece touching trap (`Elephant)
 	 * <br>	3		 -> 2 pieces touching trap (`No `Elephant)
 	 * <br>	4		 -> 2 pieces touching trap (`Elephant)
 	 * <br>	5		 -> 3 pieces touching trap (`No `Elephant)
 	 * <br>	6		 -> 3 pieces touching trap (`Elephant)
 	 * <br>	7		 -> 4 pieces touching trap (indifferent to presence of elephant)
 	 * @author Neema
 	 */
 	public static class TrapStatus {
 		/** 0 pieces around a trap */
 		public static final byte ZERO	  = 0;
 		/** 1 piece touching trap (`No `Elephant) */
 		public static final byte ONE_NE   = 1;
 		/** 1 piece touching trap (`Elephant) */
 		public static final byte ONE_E    = 2;
 		/** 2 pieces touching trap (`No `Elephant) */
 		public static final byte TWO_NE   = 3;
 		/** 2 pieces touching trap (`Elephant) */
 		public static final byte TWO_E    = 4;
 		/** 3 pieces touching trap (`No `Elephant) */
 		public static final byte THREE_NE = 5;
 		/** 3 pieces touching trap (`Elephant) */
 		public static final byte THREE_E  = 6;
 		/** 4 pieces touching trap (indifferent to presence of elephant) */
 		public static final byte FOUR     = 7;
 		/** The number of possible TrapStatus-es for a given player and trap */
 		public static final byte NUM_STATUSES = 8;
 		
 		/** Converts the number of pieces touching (the trap) + presence of an elephant
 		 * to one of the NUM_STATUSES statuses. (See TrapStatus class' comment for an
 		 * explanation.)*/
 		public static byte convertToStatus(byte numAdjacent, boolean elephant) {
 			if (numAdjacent == 0) return ZERO;
 			if (numAdjacent == 4) return FOUR;
 			
 			return (byte)(2 * numAdjacent - (elephant ? 0 : 1));
 		}
 	}
 	
 	/** Contains the constants used in SteppingOnTrapsExtractor
 	 * @author Neema */
 	public static class SteppingOnTraps {
 		
 		/** These are the piece types for the bit-boards long[6] stored in SteppingOnTrapsExtractor */
 		/* These don't seem to be used just yet... update this comment if this changes... Really
 		 * just here for the sake of clarity */
 		public static final byte RABBIT =	PT_WHITE_RABBIT / 2,
 								 CAT = 		PT_WHITE_CAT / 2,
 								 DOG = 		PT_WHITE_DOG / 2,
 								 HORSE = 	PT_WHITE_HORSE / 2,
 								 CAMEL = 	PT_WHITE_CAMEL / 2,
 								 ELEPHANT = PT_WHITE_ELEPHANT / 2;
 		
 	}
 	
 	/** FeatureRange contains the constants describing the <i>start</i> and <i>end</i>
 	 *  indices (in the FeatureExtractor's BitSet) of the different AbstractExtractor subclasses. */
 	public static class FeatureRange {
 		
 		/** There are 1040 Position Movement features set by the PositionMovementExtractor class. */
 		public static final int POS_MVMT_START = 0, POS_MVMT_END = 1039;
 		
 		/** There are 512 Trap Status features set by the TrapExtractor class. */
 		public static final int TRAP_STATUS_START = 1040, TRAP_STATUS_END = 1551;
 		
 		/** There are 160 Freezing features set by the FreezingExtractor class. */
 		public static final int FREEZING_START = 1552, FREEZING_END = 1711;
 		
 		/** There are 64 features set by the SteppingOnTrapsExtractor class. */
 		public static final int STEPPING_TRAP_START = 1712, STEPPING_TRAP_END = 1775;
 	}
 	
 	
 	/** The total number of features in our feature vector. UPDATE this as we add more features. */
 	public static final int NUM_FEATURES = FeatureRange.STEPPING_TRAP_END + 1;
 	
 	/** There are 32 locations on the board taking left-right symmetry into account. */
 	public static final int NUM_LOCATIONS = 32;
 	
 	/** There are 8 piece types as described in David Wu's paper pg 25. */
 	public static final int NUM_PIECE_TYPES = 8;
 }
