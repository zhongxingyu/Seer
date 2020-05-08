 package blackjack;
 
 import java.util.Vector;
 import java.util.Random;
 
 
 public class Deck {
 	private Vector<Card> playingDeck;
 
 	public Deck() {
 		playingDeck = new Vector<Card>();
 		shuffleDeck();
 	}
 
 	public void shuffleDeck() {
 		playingDeck.clear();
 
 		for (int i = 0; i < 4; i++) {
 			String suit = "";
 			switch (i) {
 			case 0: suit = "Diamond";
 			break;
 			case 1: suit = "Heart";
 			break;
 			case 2: suit = "Spade";
 			break;
 			case 3: suit = "Club";
 			break;
 			default:
 				break;
 			}
 
 			for (int j = 0; j < 13; j++) {
 				String cardType = "";
 				if (j == 0)
 					cardType = "Ace";
 				else if (j > 0 && j < 10)
 					cardType = Integer.toString(j + 1);
 				else if (j == 10)
 					cardType = "Jack";
 				else if (j == 11)
 					cardType = "Queen";
 				else if (j == 12)
 					cardType = "King";
 				else
 					cardType = null;
 
 				playingDeck.add(new Card(cardType, suit));
 			}
 		}
 	}
 
 	public Card getNextCard() {
 		Random rand = new Random();
 		int randomCard = rand.nextInt(playingDeck.size());
 
 		Card nextCard = playingDeck.get(randomCard);
 		playingDeck.remove(randomCard);
 
 		return nextCard;
 	}
 
 	public void deal(Game game) {
 		Hand hand;
		for (Player p : Game.getPlayerList()) {
 			hand = new Hand(getNextCard());
 			p.addHand(hand);
 		}	
 		
 		hand = new Hand(getNextCard());
 		game.getDealer().addHand(hand);
 		
		for (Player p : Game.getPlayerList()) {
 			p.getHand().addCard(getNextCard());
 		}
 		
 		game.getDealer().getHand().addCard(getNextCard());
 	}
 }
