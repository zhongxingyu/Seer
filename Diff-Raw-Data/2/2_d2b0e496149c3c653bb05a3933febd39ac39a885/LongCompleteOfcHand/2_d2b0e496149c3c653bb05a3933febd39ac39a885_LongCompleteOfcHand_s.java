 import org.pokersource.game.Deck;
 
 import com.google.common.annotations.VisibleForTesting;
 import com.google.common.base.Strings;
 import java.util.HashMap;
 
 public class LongCompleteOfcHand extends LongOfcHand
 	implements CompleteOfcHand {
 
 	static HashMap<Long, Long> evalCache = new HashMap<Long, Long>();
 
 	public LongCompleteOfcHand(LongOfcHand source, OfcCard card) {
 		super(source);
 		if (getBackSize() < BACK_SIZE) {
			addFront(card);
 		} else if (getMiddleSize() < MIDDLE_SIZE) {
 			addMiddle(card);
 		} else if (getFrontSize() < FRONT_SIZE) {
 			addFront(card);
 		} 
 		if (!super.isComplete()) {
 			throw new IllegalStateException("Hand too empty.");
 		}
 	}
 
 	public LongCompleteOfcHand(LongOfcHand source) {
 		super(source);
 		if (!source.isComplete()) {
 			throw new IllegalStateException("Hand too empty.");
 		}
 	}
 	
 	// convenient factory for tests
 	static CompleteOfcHand createComplete(String handString) {
 		return new LongCompleteOfcHand(LongOfcHand.create(handString));
 	}
 	
 	@Override
 	public boolean isComplete() {
 		throw new RuntimeException("Why are you asking?!");
 		//return true;
 	}
 
 	@Override
 	public boolean isFouled() {
 		// TODO: Be damn sure about this.  Making an assumption that when the hand is complete, willBeFouled is always populated
 		// via the completeXXX methods
 		return willBeFouled;
 	}
 
 	
 	@Override
 	public long getFrontRank() {
 		if (frontValue == UNSET) {
 			Long cached = evalCache.get(front);
 			if (cached == null) {
 				int[] ranks = new int[FRONT_SIZE];
 				int[] suits = new int[FRONT_SIZE];
 				convertForEval(front, ranks, suits, FRONT_SIZE);
 				frontValue = StupidEval.eval3(ranks);
 				evalCache.put(front, frontValue);
 			} else {
 				frontValue = cached;
 			}
 		}
 		return frontValue;
 	}
 
 	@Override
 	public long getMiddleRank() {
 		if (middleValue == UNSET) {
 			Long cached = evalCache.get(middle);
 			if (cached == null) {
 				int[] ranks = new int[MIDDLE_SIZE];
 				int[] suits = new int[MIDDLE_SIZE];
 				convertForEval(middle, ranks, suits, MIDDLE_SIZE);
 				middleValue = StupidEval.eval(ranks, suits);
 				evalCache.put(middle, middleValue);
 			} else {
 				middleValue = cached;
 			}
 		}
 		return middleValue;
 	}
 
 	@Override
 	public long getBackRank() {
 		if (backValue == UNSET) {
 			Long cached = evalCache.get(back);
 			if (cached == null) {
 				int[] ranks = new int[BACK_SIZE];
 				int[] suits = new int[BACK_SIZE];
 				convertForEval(back, ranks, suits, BACK_SIZE);
 				backValue = StupidEval.eval(ranks, suits);
 				evalCache.put(back, backValue);
 			} else {
 				backValue = cached;
 			}
 		}
 		return backValue;
 	}
 
 	@Override
 	public int getStreet() {
 		return 14;
 	}
 
 	/* 
 	 * TODO: this really belongs in AbstractOfcHand
 	 * @see OfcHand#getRoyaltyValue()
 	 */
 	@Override
 	public int getRoyaltyValue() {
 		if (isFouled()) {
 			return 0;
 		}
 		int value = 0;
 		
 		// Stupid integer division hack to zero out all the insignificant digits so we can use a map to look up
 		// royalty values
 		long rank = getBackRank() / StupidEval.ONE_PAIR * StupidEval.ONE_PAIR;
 		if (backRoyaltyMap.containsKey(rank)) {
 			value += backRoyaltyMap.get(rank);
 		}
 		rank = getMiddleRank() / StupidEval.ONE_PAIR * StupidEval.ONE_PAIR;
 		if (backRoyaltyMap.containsKey(rank)) {
 			value += backRoyaltyMap.get(rank) * 2;
 		}
 		
 		rank = getFrontRank();
 		if (rank >= StupidEval.TRIPS) {
 			rank -= StupidEval.TRIPS;
 			// StupidEval implementation is to leave only the rank of the card here.  Deuce = 0, per Deck constants
 			// Yes, this is super lame. 15 points for 222, one more for every higher rank.
 			value += 15 + rank;
 		} else if (rank >= StupidEval.ONE_PAIR) {
 			// More stupid implementation dependent details.  Subtract out the ONE_PAIR constant, integer divide
 			// the kickers away, get left with the rank of the pair based on Deck constants.  66 = 5.
 			rank -= StupidEval.ONE_PAIR;
 			rank /= StupidEval.PAIR_CONSTANT;
 			if (rank >= 5) {
 				value += rank - 4;
 			}
 		}
 	
 		return value;
 	}
 
 	@Override
 	public int getFantasylandValue() {
 		if (getFrontRank() > StupidEval.FANTASYLAND_THRESHOLD) {
 			return FANTASYLAND_VALUE;
 		}
 		return 0;
 	}
 	
 	/*
 	 * TODO: this also belongs in AbstractOfcHand
 	 * @see OfcHand#scoreAgainst(OfcHand)
 	 */
 	@Override
 	public Score scoreAgainst(CompleteOfcHand other) {
 		if (isFouled()) {
 			if (other.isFouled()) {
 				return Score.ZERO;
 			}
 			int score = -6 - other.getRoyaltyValue();
 			return new Score(score, score - other.getFantasylandValue());
 		}
 		if (other.isFouled()) {
 			int score = 6 + getRoyaltyValue();
 			return new Score(score, getFantasylandValue());
 		}
 		int wins = 0;
 		if (getBackRank() > other.getBackRank()) {
 			wins++;
 		}
 		if (getMiddleRank() > other.getMiddleRank()) {
 			wins++;
 		}
 		if (getFrontRank() > other.getFrontRank()) {
 			wins++;
 		}
 		
 		int score = getRoyaltyValue() - other.getRoyaltyValue();
 		int flValue = getFantasylandValue() - other.getFantasylandValue();
 		switch (wins) {
 			case 0:
 				score -= 6;
 				break;
 			case 1:
 				score -= 1;
 				break;
 			case 2:
 				score += 1;
 				break;
 			case 3:
 				score += 6;
 				break;
 			default:
 				throw new IllegalStateException("wtf");
 		}
 		return new Score(score, score + flValue);
 
 	}
 	
 
 }
