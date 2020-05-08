 import org.pokersource.game.Deck;
 
 import com.google.common.annotations.VisibleForTesting;
 import com.google.common.base.Strings;
 
 public class LongOfcHand extends CachedValueOfcHand {
 
 	@VisibleForTesting
 	long front;
 	@VisibleForTesting
 	long middle;
 	@VisibleForTesting
 	long back;
 	
 	public LongOfcHand() {
 		super();
 		front = 0;
 		middle = 0;
 		back = 0;
 	}
 	
 	private LongOfcHand(LongOfcHand source) {
 		super(source);
 		this.front = source.front;
 		this.middle = source.middle;
 		this.back = source.back;
 	}
 	
 	@Override
 	public LongOfcHand copy() {
 		return new LongOfcHand(this);
 	}
 
 	@Override
 	public int getBackSize() {
 		return Long.bitCount(back);
 	}
 	
 	@Override
 	public int getMiddleSize() {
 		return Long.bitCount(middle);
 	}
 	
 	@Override
 	public int getFrontSize() {
 		return Long.bitCount(front);
 	}
 
 	@Override
 	public long getBackMask() {
 		return back;
 	}
 	
 	@Override
 
 	public long getMiddleMask() {
 		return middle;
 	}
 	
 	@Override
 	public long getFrontMask() {
 		return front;
 	}
 
 	@Override
 	public void addBack(OfcCard card) {
 		if (getBackSize() >= BACK_SIZE) {
 			throw new IllegalStateException("Back already full");
 		}
 		if ((back & card.getMask()) != 0) {
 			throw new IllegalArgumentException("Card already in back");
 		}
 		back |= card.getMask();
 		
 		if (getBackSize() == BACK_SIZE) {
 			completeBack();
 		}
 	}
 
 	@Override
 	public void addMiddle(OfcCard card) {
 		if (getMiddleSize() >= MIDDLE_SIZE) {
 			throw new IllegalStateException("Middle already full");
 		}
 		if ((middle & card.getMask()) != 0) {
 			throw new IllegalArgumentException("Card already in middle");
 		}
 		middle |= card.getMask();
 
 		if (getMiddleSize() == MIDDLE_SIZE) {
 			completeMiddle();
 		}
 	}
 
 	@Override
 	public void addFront(OfcCard card) {
 		if (getFrontSize() >= FRONT_SIZE) {
 			throw new IllegalStateException("Front already full");
 		}
 		if ((front & card.getMask()) != 0) {
 			throw new IllegalArgumentException("Card already in front");
 		}
 		front |= card.getMask();
 
 		if (getFrontSize() == FRONT_SIZE) {
 			completeFront();
 		}
 	}
 
 	@Override
 	public boolean isComplete() {
 		return getFrontSize() == FRONT_SIZE &&
 				getMiddleSize() == MIDDLE_SIZE &&
 				getBackSize() == BACK_SIZE;
 	}
 
 	@Override
 	public boolean willBeFouled() {
 		return willBeFouled;
 	}
 
 	@Override
 	public boolean isFouled() {
 		if (!isComplete()) {
 			throw new IllegalStateException("Hand not complete");
 		}
 		// TODO: Be damn sure about this.  Making an assumption that when the hand is complete, willBeFouled is always populated
 		// via the completeXXX methods
 		return willBeFouled;
 	}
 
 	/**
 	 * Fill the ranks and suits arrays with the value of the hand.
 	 */
 	@VisibleForTesting
 	static void convertForEval(long mask, int[] ranks, int[] suits, int numCards) {
 		if (numCards != Long.bitCount(mask)) {
 			throw new IllegalArgumentException("Hand isn't complete");
 		}
 
 		// NOTE: There's a hidden requirement here that the cards are sorted in descending order
 		CardSetUtils.convertToArrays(mask, ranks, suits);
 	}
 	
 	@Override
 	public long getFrontRank() {
 		if (frontValue == UNSET) {
 			if (getFrontSize() != FRONT_SIZE) {
 				throw new IllegalStateException("Front not complete");
 			}
 			int[] ranks = new int[FRONT_SIZE];
 			int[] suits = new int[FRONT_SIZE];
 			convertForEval(front, ranks, suits, FRONT_SIZE);
 			frontValue = StupidEval.eval3(ranks);
 		}
 		return frontValue;
 	}
 
 	@Override
 	public long getMiddleRank() {
 		if (middleValue == UNSET) {
 			if (getMiddleSize() != MIDDLE_SIZE) {
 				throw new IllegalStateException("Middle not complete");
 			}
 			int[] ranks = new int[MIDDLE_SIZE];
 			int[] suits = new int[MIDDLE_SIZE];
 			convertForEval(middle, ranks, suits, MIDDLE_SIZE);
 			middleValue = StupidEval.eval(ranks, suits);
 		}
 		return middleValue;
 	}
 
 	@Override
 	public long getBackRank() {
 		if (backValue == UNSET) {
 			if (getBackSize() != BACK_SIZE) {
 				throw new IllegalStateException("Back not complete");
 			}
 			int[] ranks = new int[BACK_SIZE];
 			int[] suits = new int[BACK_SIZE];
 			convertForEval(back, ranks, suits, BACK_SIZE);
 			backValue = StupidEval.eval(ranks, suits);
 		}
 		return backValue;
 	}
 
 	@Override
 	public int getStreet() {
 		return getFrontSize() + getMiddleSize() + getBackSize() + 1;
 	}
 
 	@Override
 	public String toKeyString() {
 		StringBuilder sb = new StringBuilder();
 /*
 		sb.append(Strings.padStart(Long.toHexString(front), 16, '0'));
 		sb.append("-");
 		sb.append(Strings.padStart(Long.toHexString(middle), 16, '0'));
 		sb.append("-");
 		sb.append(Strings.padStart(Long.toHexString(back), 16, '0'));
 */
 		if (willBeFouled()) {
			long combined = front | middle | back;
 			sb.append(Deck.cardMaskString(combined, ""));
 		} else {
 			sb.append(Deck.cardMaskString(front, ""));
 			sb.append("-");
 			sb.append(Deck.cardMaskString(middle, ""));
 			sb.append("-");
 			sb.append(Deck.cardMaskString(back, ""));
 		}
 
 		
 		
 		return sb.toString();
 	}
 
 	public static LongOfcHand fromKeyString(String s) {
 		String[] hands = s.split("-");
 		if (hands.length != 3) {
 			throw new IllegalArgumentException("format incorrect");
 		}
 		LongOfcHand hand = new LongOfcHand();
 		int index = 0;
 		while (index < hands[0].length()) {
 			hand.addFront(new OfcCard(hands[0].substring(index, index + 2)));
 			index += 2;
 		}
 		index = 0;
 		while (index < hands[1].length()) {
 			hand.addMiddle(new OfcCard(hands[1].substring(index, index + 2)));
 			index += 2;
 		}
 		index = 0;
 		while (index < hands[2].length()) {
 			hand.addBack(new OfcCard(hands[2].substring(index, index + 2)));
 			index += 2;
 		}
 		return hand;
 	}
 	
 	private String maskToString(long mask) {
 		if (mask == 0) {
 			return "[]\n";
 		}
 		return Deck.cardMaskString(mask) + "\n";
 	}
 	
 	// convenient factory for tests
 	static OfcHand create(String handString) {
 		LongOfcHand hand = new LongOfcHand();
 		OfcDeck deck = new OfcDeck();
 		deck.initialize();
 		hand.setHand(handString, deck);
 		return hand;
 	}
 	
 	public void setHand(String handString, OfcDeck deck) {
 		String[] hands = handString.split("/");
 		if (hands.length != 3) {
 			throw new IllegalArgumentException("Must have 3 hands");
 		}
 		int index = 0;
 		while (index < hands[0].length()) {
 			String cardString = hands[0].substring(index, index + 2);
 			addFront(new OfcCard(cardString));
 			deck.removeCard(cardString);
 			index += 2;
 		}
 		index = 0;
 		while (index < hands[1].length()) {
 			String cardString = hands[1].substring(index, index + 2);
 			addMiddle(new OfcCard(cardString));
 			deck.removeCard(cardString);
 			index += 2;
 		}
 		index = 0;
 		while (index < hands[2].length()) {
 			String cardString = hands[2].substring(index, index + 2);
 			addBack(new OfcCard(cardString));
 			deck.removeCard(cardString);
 			index += 2;
 		}
 	}
 	
 	@Override
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		sb.append(maskToString(front));
 		sb.append(maskToString(middle));
 		sb.append(maskToString(back));
 		return sb.toString();
 	}
 	
 	@Override
 	public int hashCode() {
 	/*
 		if (true) {
 			return toKeyString().hashCode();
 		}
 		*/
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + (int) (back ^ (back >>> 32));
 		result = prime * result + (int) (front ^ (front >>> 32));
 		result = prime * result + (int) (middle ^ (middle >>> 32));
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		LongOfcHand other = (LongOfcHand) obj;
 /*
 		if (true) {
 			return other.toKeyString().equals(toKeyString());
 		}
 */
 		
 		if (back != other.back)
 			return false;
 		if (front != other.front)
 			return false;
 		if (middle != other.middle)
 			return false;
 		return true;
 	}
 
 }
