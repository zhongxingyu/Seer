 package nl.starapple.starterbot;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import nl.starapple.poker.Card;
 import nl.starapple.poker.Hand;
 
 public class PokerState {
 	
 	private int round, smallBlind, bigBlind;
 	
 	private boolean onButton;
 	
 	private int yourStack, opponentStack;
 	
 	private int pot;
 	
 	private String opponentAction;
 	
 	private int currentBet;
 	
 	private Hand hand;
 	
 	private Card[] table;
 	
 	private Map<String,String> settings = new HashMap<String,String>();
 	
 	private String myName = "";
 	
 	private int[] sidepots;
 	
 	protected void updateSetting(String key, String value) {
 		settings.put(key, value);
 		if( key.equals("yourBot") ) {
 			myName = value;
 		}
 	}
 
 	protected void updateMatch(String key, String value) {
 		if( key.equals("round") ) {
 			round = Integer.valueOf(value);
            table = new Card[0];
 		} else if( key.equals("smallBlind") ) {
 			smallBlind = Integer.valueOf(value);
 		} else if( key.equals("bigBlind") ) {
 			bigBlind = Integer.valueOf(value);
 		} else if( key.equals("onButton") ) {
 			onButton = value.equals(myName);
 		} else if( key.equals("pot") ) {
 			pot = Integer.valueOf(value);
 		} else if( key.equals("table") ) {
 			table = parseCards(value);
 		} else if( key.equals("sidepots") ) {
 			sidepots = parsePots(value);
            currentBet = ( sidepots.length > 0 ) ? sidepots[0] : 0;
 		} else {
 			System.err.printf("Unknown match command: %s %s\n", key, value);
 		}
 	}
 
 	protected void updateMove(String bot, String move, String amount) {
 		if( bot.equals(myName) ) {
 			if( move.equals("stack") ) {
 				yourStack = Integer.valueOf(amount);
 			} else if( move.equals("seat") ) {
 				// ignored for now
 			} else if( move.equals("hand") ) {
 				Card[] cards = parseCards(amount);
 				assert( cards.length == 2 ) : String.format("Did not receive two cards, instead: ``%s''", amount);
 				hand = new Hand(cards[0], cards[1]);
 			} else {
 				// assume someone made some move
 			}
 		} else {
 			// assume it's the (only) villain
 			if( move.equals("stack") ) {
 				opponentStack = Integer.valueOf(amount);
 			} else {
                opponentAction = move;
 			}
 		}
 	}
 
 	private int[] parsePots(String value) {
 		if( value.endsWith("]") ) { value = value.substring(0, value.length()-1); }
 		if( value.startsWith("[") ) { value = value.substring(1); }
 		if( value.length() == 0 ) { return new int[0]; }
 		String[] parts = value.split(",");
 		int[] pots = new int[parts.length];
 		for( int i = 0; i < parts.length; ++i ) {
 			pots[i] = Integer.valueOf(parts[i]);
 		}
 		return pots;
 	}
 
 	private Card[] parseCards(String value) {
 		if( value.endsWith("]") ) { value = value.substring(0, value.length()-1); }
 		if( value.startsWith("[") ) { value = value.substring(1); }
 		if( value.length() == 0 ) { return new Card[0]; }
 		String[] parts = value.split(",");
 		Card[] cards = new Card[parts.length];
 		for( int i = 0; i < parts.length; ++i ) {
 			cards[i] = Card.getCard(parts[i]);
 		}
 		return cards;
 	}
 
 	public int getRound() {
 		return round;
 	}
 
 	public int getSmallBlind() {
 		return smallBlind;
 	}
 
 	public int getBigBlind() {
 		return bigBlind;
 	}
 
 	public boolean onButton() {
 		return onButton;
 	}
 
 	public int getYourStack() {
 		return yourStack;
 	}
 
 	public int getOpponentStack() {
 		return opponentStack;
 	}
 	
 	public int getPot() {
 		return pot;
 	}
 	
 	public String getOpponentAction() {
 		return opponentAction;
 	}
 	
 	public int getCurrentBet() {
 		return currentBet;
 	}
 
 	public Hand getHand() {
 		return hand;
 	}
 
 	public Card[] getTable() {
 		return table;
 	}
 	
 	public String getSetting(String key) {
 		return settings.get(key);
 	}
 
 	public int[] getSidepots() {
 		return sidepots;
 	}
 
 }
