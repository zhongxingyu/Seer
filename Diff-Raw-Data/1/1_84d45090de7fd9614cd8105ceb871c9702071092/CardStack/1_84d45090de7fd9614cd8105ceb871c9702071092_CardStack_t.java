 package swimGame.table;
 
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.Random;
 
 /**
  * A card stack as part of the game. This is initially the full set of cards
  * available in the game.
  * 
  * <pre>
  * The CardStack byte array as reference:
  * 
  *    7  8  9 10  J  Q  K  A
  * ♦ 00 01 02 03 04 05 06 07
  * ♥ 08 09 10 11 12 13 14 15
  * ♠ 16 17 18 19 20 21 22 23
  * ♣ 24 25 26 27 28 29 30 31
  * </pre>
  * 
  * @author Jens Bertram <code@jens-bertram.net>
  * 
  */
 public class CardStack {
     // one random number generator for all stacks should be enough
     private static Random random = null;
     // is this an empty stack?8
     private boolean empty = true;
 
     // one-based card array bounds, just for ease of use / readability
     public static final int CARDS_MAX_COLOR = 4;
     public static final int CARDS_MAX_CARD = 8;
     public static final int CARDS_MAX = 32;
     private static final int STACK_SIZE = (CardStack.CARDS_MAX_CARD * CardStack.CARDS_MAX_COLOR);
     // the minimum of the reachable points
     public static final int STACKVALUE_MIN = 24; // 7 + 8 + 9
     // the maximum of the reachable points
     public static final int STACKVALUE_MAX = 31; // A + B + D
     // flags for the card-stack array
     public static final byte FLAG_HAS_CARD = 1;
     public static final byte FLAG_NO_CARD = 0;
     public static final byte FLAG_UNINITIALIZED = -1;
 
     // Card names for pretty printing
     public static final char[] CARD_SYMBOLS = { '♦', '♥', '♠', '♣' };
     public static final String[] CARD_NAMES = { "7", "8", "9", "10", "J", "Q",
 	    "K", "A" };
     private static String[] cardStackStr;
 
     // Card stack of this table
     private byte[] cardStack = new byte[CardStack.STACK_SIZE];
 
     // card counter for this stack
     private byte cardsCount = 0;
 
     // nested classes
     public Card card = new Card();
 
     private void buildStringArray() {
 	CardStack.cardStackStr = new String[CardStack.CARDS_MAX];
 	int idx = 0;
 	for (int color = 0; color < CardStack.CARDS_MAX_COLOR; color++) {
 	    for (int card = 0; card < CardStack.CARDS_MAX_CARD; card++) {
 		CardStack.cardStackStr[idx++] = CardStack.CARD_SYMBOLS[color]
 			+ CardStack.CARD_NAMES[card];
 	    }
 	}
     }
 
     /**
      * Empty constructor
      */
     public CardStack() {
 	this.buildStringArray();
     }
 
     /**
      * Constructor
      * 
      * @param filled
      *            If true, the card stack will be initially full (i.e. all cards
      *            are on the stack)
      */
     public CardStack(final boolean filled) {
 	this.buildStringArray();
 	if (filled == true) {
 	    // The card stack will be initially full
 	    this.cardStack = CardStack.getNewStack(true);
 	    this.empty = false;
 	}
     }
 
     /**
      * Constructor
      * 
      * @param initialCards
      *            An initial set of three cards to initialize this set with
      * @throws Exception
      *             Thrown if you specify less or more than three cards
      */
     public CardStack(final byte[] initialCards) {
 	this.buildStringArray();
 	if (initialCards.length < 3) {
 	    throw new IllegalArgumentException(
 		    "You must give three cards to initialize a CardStack!");
 	}
 	this.card.add(initialCards);
     }
 
     public class Card {
 	/**
 	 * Get the color number for a card.
 	 * 
 	 * @param card
 	 *            The card to check
 	 * @return The card color: 0=♦; 1=♥; 2=♠; 3=♣
 	 */
 	public int getColor(int card) {
 	    CardStack.checkCard(card);
 	    return card / CardStack.CARDS_MAX_CARD;
 	}
 
 	/**
 	 * Get a string representation of the given card.
 	 * 
 	 * @return A string that looks like [♥A] for heart-ace
 	 */
 	public String toString(final int card) {
 	    CardStack.checkCard(card);
 	    return "[" + CardStack.cardStackStr[card] + "]";
 	}
 
 	/** Get the type for a card */
 	public int getType(int card) {
 	    CardStack.checkCard(card);
 	    return (card < CardStack.CARDS_MAX_CARD) ? card
 		    : (card % CardStack.CARDS_MAX_CARD);
 	}
 
 	/** Gets the stored value for a card */
 	public byte getValue(int card) {
 	    CardStack.checkCard(card);
 	    return CardStack.this.cardStack[card];
 	}
 
 	/**
 	 * Sets a custom value instead of the predefined flags for a card. This
 	 * passes by the check if a card is available (set
 	 * CardStack#FLAG_HAS_CARD). If you want to add a card use addCard() or
 	 * addCards() instead.
 	 */
 	public void setValue(int card, byte value) {
 	    CardStack.checkCard(card);
 	    CardStack.this.cardStack[card] = value;
 	}
 
 	/**
 	 * Get a random card out of the stack
 	 * 
 	 * @return Byte array representing the card
 	 * @see swimGame.cards.CardUtils#initCardStack
 	 */
 	public byte getRandom() {
 	    if (CardStack.this.empty) {
 		throw new IllegalArgumentException(
 			"Unable to get a card. Stack is empty!");
 	    }
 
 	    if (CardStack.random == null) {
 		CardStack.random = new Random();
 	    }
 
 	    // try to find a random card that's still on the stack
 	    // TODO: make this aware of available cards to be more intelligent
 	    while (true) {
 		int card = CardStack.random.nextInt(CardStack.CARDS_MAX_CARD
 			* CardStack.CARDS_MAX_COLOR);
 		if (CardStack.this.cardStack[card] == CardStack.FLAG_HAS_CARD) {
 		    // card is there .. take it
 		    CardStack.this.cardStack[card] = CardStack.FLAG_NO_CARD;
 		    return (byte) card;
 		}
 	    }
 	}
 
 	/**
 	 * Removes a card from this stack
 	 * 
 	 * @param card
 	 *            The card to remove
 	 * @return True if it got removed
 	 */
 	public boolean remove(final int card) {
 	    if (CardStack.this.cardStack[card] == CardStack.FLAG_NO_CARD) {
 		return false;
 	    }
 	    CardStack.this.cardStack[card] = CardStack.FLAG_NO_CARD;
 	    CardStack.this.cardsCount--;
 	    return true;
 	}
 
 	/**
 	 * Adds a card to this stack
 	 * 
 	 * @param card
 	 *            The card to add
 	 * @return True if it was added
 	 */
 	public boolean add(int card) {
 	    if (CardStack.this.cardStack[card] == CardStack.FLAG_HAS_CARD) {
 		return false;
 	    }
 	    CardStack.this.cardStack[card] = CardStack.FLAG_HAS_CARD;
 	    CardStack.this.empty = false;
 	    CardStack.this.cardsCount++;
 	    return true;
 	}
 
 	/** Add a bunch of cards */
 	public void add(byte[] cards) {
 	    for (int card : cards) {
 		this.add(card);
 	    }
 	    CardStack.this.empty = false;
 	}
 
 	/** Get the value of a card as calculated at the end of the game */
 	public byte getWorth(int card) {
 	    CardStack.checkCard(card);
 	    int positionValue = 0;
 	    if (card < CardStack.CARDS_MAX_CARD) {
 		positionValue = card;
 	    }
 	    positionValue = (card - ((card / CardStack.CARDS_MAX_CARD) * CardStack.CARDS_MAX_CARD));
 	    if (positionValue > 2) {
 		return (byte) (positionValue < 7 ? 10 : 11);
 	    } else {
 		return (byte) (positionValue + 7);
 	    }
 	}
     }
 
     /**
      * Returns a string representation of the cards available in this stack
      */
     @Override
     public String toString() {
 	if (this.empty) {
 	    return "";
 	}
 	String cards = "";
 	for (byte card : this.getCards()) {
 	    cards += this.card.toString(card);
 	}
 	return cards;
     }
 
     /**
      * Get the current card stack as array
      * 
      * @return
      */
     public byte[] asArray() {
 	return this.cardStack.clone();
     }
 
     /**
      * Get a full card stack
      * 
      * @param full
      * @return
      */
     private static byte[] getNewStack(final boolean full) {
 	byte[] cardStack = new byte[CardStack.CARDS_MAX_CARD
 		* CardStack.CARDS_MAX_COLOR];
 	if (full) {
 	    // this stack will contain all cards
 	    for (int i = 0; i < cardStack.length; i++) {
 		cardStack[i] = 1;
 	    }
 	}
 	return cardStack;
     }
 
     /** Fills the card-stack with the given value */
     public void fill(byte value) {
 	Arrays.fill(this.cardStack, value);
     }
 
     /** Check if a card type is in the legal range */
     public static void checkCardType(final int cardType) {
 	if ((cardType < 0) || (cardType > CardStack.CARDS_MAX_CARD)) {
 	    throw new IllegalArgumentException(String.format(
 		    "Card type %d out of bounds (%d-%d)", cardType, 0,
 		    CardStack.CARDS_MAX_CARD));
 	}
     }
 
     /** Check if a card color is in the legal range */
     public static void checkCardColor(final int cardColor) {
 	if ((cardColor < 0) || (cardColor >= CardStack.CARDS_MAX_COLOR)) {
 	    throw new IllegalArgumentException(String.format(
 		    "Card color %d out of bounds (%d-%d)", cardColor, 0,
 		    CardStack.CARDS_MAX_COLOR));
 	}
     }
 
     /** Check if a cards position in the stack is a legal one */
     public static void checkCard(final int card) {
 	if ((card >= CardStack.STACK_SIZE) || (card < 0)) {
 	    throw new IllegalArgumentException(String.format(
 		    "Card type %d out of bounds (%d-%d)", card, 0,
 		    CardStack.STACK_SIZE));
 	}
     }
 
     /** Get all cards for a specific card-type (7,8,9,10,J,Q,K,A) */
     public byte[] getCardsByType(int cardType) {
 	CardStack.checkCardType(cardType);
 	byte[] typeCards = new byte[CardStack.CARDS_MAX_COLOR];
 
 	int offset = cardType;
 	for (int i = 0; i < CardStack.CARDS_MAX_COLOR; i++) {
 	    typeCards[i] = (byte) (offset + (i * CardStack.CARDS_MAX_CARD));
 	}
 	return typeCards;
     }
 
     /**
      * Get all cards for a specific card-color (♦, ♥, ♠, ♣). This returns all
      * cards, regardless if they owned or not.
      */
     public byte[] getCardsByColor(int cardColor) {
 	CardStack.checkCardColor(cardColor);
 	byte[] colorCards = new byte[CardStack.CARDS_MAX_CARD];
 
 	int offset = cardColor * CardStack.CARDS_MAX_CARD;
 	for (int i = 0; i < CardStack.CARDS_MAX_CARD; i++) {
 	    colorCards[i] = (byte) (offset + i);
 	}
 	return colorCards;
     }
 
     /**
      * Iterator that steps from top left to right through the stack-array
      * 
      * @author Jens Bertram <code@jens-bertram.net>
      * 
      */
     public class CardIterator implements Iterator<Integer> {
 	private int pointer = 0;
 
 	public CardIterator() {
 	    this.pointer = 0;
 	}
 
 	@Override
 	public boolean hasNext() {
 	    return ((this.pointer + 1) < CardStack.this.cardStack.length) ? true
 		    : false;
 	}
 
 	@Override
 	public Integer next() {
 	    if (this.hasNext()) {
 		return Integer
 			.valueOf(CardStack.this.cardStack[this.pointer++]);
 	    }
 	    throw new IllegalStateException("You tried to step out of bounds.");
 	}
 
 	@Override
 	public void remove() throws IllegalStateException {
 	    throw new IllegalStateException("Operation not supported.");
 	}
 
 	/** Get the current card */
 	public int getCard() {
 	    // be careful we're one ahead here
 	    return this.pointer - 1;
 	}
 
 	/** Get the current card type */
 	public int getCardType() {
 	    return CardStack.this.card.getType(this.getCard());
 	}
 
 	/** Get the color of the current card */
 	public int getCardColor() {
 	    if (this.pointer < CardStack.CARDS_MAX_CARD) {
 		return 0;
 	    }
 	    return CardStack.this.card.getColor(this.getCard());
 	}
     }
 
     /**
      * Get an array with all cards currently in this stack. This will only find
      * cards witch were added with the appropriate add functions.
      * 
      * @return A byte array containing only available cards
      */
     public byte[] getCards() {
 	byte[] cards = new byte[this.cardsCount];
 	int currentCard = 0;
 	for (int i = 0; i < this.cardStack.length; i++) {
 	    if (currentCard > this.cardsCount) {
 		// we've found all
 		break;
 	    }
 	    if (this.hasCard(i)) {
 		cards[currentCard++] = (byte) i;
 	    }
 	}
 	return cards;
     }
 
     public double getValue() {
 	int value = 0;
 	// three of a color?
 	for (byte color = 0; color < CardStack.CARDS_MAX_COLOR; color++) {
 	    int newValue = 0;
 	    for (byte card : this.getCardsByColor(color)) {
 		if (this.hasCard(card)) {
 		    newValue = newValue + this.card.getWorth(card);
 		}
 	    }
 	    value = (newValue > value) ? newValue : value;
 	}
 
 	// three of a type?
 	for (byte type = 0; type < CardStack.CARDS_MAX_COLOR; type++) {
 	    int newValue = 0;
 	    int count = 0;
 	    for (byte card : this.getCardsByType(type)) {
 		if (this.hasCard(card)) {
 		    newValue = newValue + this.card.getWorth(card);
 		    count++;
 		}
 	    }
 	    if (count == 3) {
 		return Table.WORTH_THREE_OF_SAME_TYPE;
 	    }
 	}
 
 	return value;
     }
 
     /** Checks if this card is in this stack */
     public boolean hasCard(int card) {
 	CardStack.checkCard(card);
 	return (this.cardStack[card] == CardStack.FLAG_HAS_CARD) ? true : false;
     }
 
     /** Dump the current stack as nicely formatted table */
     public StringBuffer dump() {
 	StringBuffer dumpStr = new StringBuffer();
 
 	String separator = "\n-+----+----+----+----+----+----+----+----+";
 	String content = "\n%s|%+4d|%+4d|%+4d|%+4d|%+4d|%+4d|%+4d|%+4d|";
 
 	dumpStr.append(String.format(
 		" |   7|   8|   9|  10|   %s|   %s|   %s|   %s|",
 		CardStack.CARD_NAMES[4], CardStack.CARD_NAMES[5],
 		CardStack.CARD_NAMES[6], CardStack.CARD_NAMES[7]));
 	dumpStr.append(separator);
 	for (int i = 0; i < CardStack.CARDS_MAX_COLOR; i++) {
 	    int offset = (i * CardStack.CARDS_MAX_CARD);
 	    dumpStr.append(String.format(content, CardStack.CARD_SYMBOLS[i],
 		    this.cardStack[offset + 0], this.cardStack[offset + 1],
 		    this.cardStack[offset + 2], this.cardStack[offset + 3],
 		    this.cardStack[offset + 4], this.cardStack[offset + 5],
 		    this.cardStack[offset + 6], this.cardStack[offset + 7]));
 	}
 	dumpStr.append(separator);
 	return dumpStr;
     }
 }
