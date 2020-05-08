 package poker.arturka;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import message.data.Card;
 import message.data.Player;
 import message.data.Card.Rank;
 import message.data.Card.Suit;
 
 public class HandEvaluatorTest {
 
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		Player p1 = new Player(1, "bob");
 		Player p2 = new Player(2, "bob");
 		Player p3 = new Player(3, "bob");
 
 		Card c1 = new Card(Suit.CLUBS, Rank.FOUR);
 		Card c2 = new Card(Suit.SPADES, Rank.KING);
 		Card c3 = new Card(Suit.DIAMONDS, Rank.FOUR);
 		Card c4 = new Card(Suit.DIAMONDS, Rank.EIGHT);
 		Card c5 = new Card(Suit.HEARTS, Rank.NINE);
 
 		Card c6 = new Card(Suit.CLUBS, Rank.THREE);
 		Card c7 = new Card(Suit.SPADES, Rank.JACK);
 		Card[] playerHand = { c6, c7 };
 		p1.hand = playerHand;
 
 		Card c66 = new Card(Suit.SPADES, Rank.QUEEN);
 		Card c77 = new Card(Suit.CLUBS, Rank.ACE);
 		Card[] playerHand2 = { c66, c77 };
 		p2.hand = playerHand2;
 
 		Card c666 = new Card(Suit.DIAMONDS, Rank.THREE);
 		Card c777 = new Card(Suit.HEARTS, Rank.QUEEN);
 		Card[] playerHand3 = { c666, c777 };
 		p3.hand = playerHand3;
 
 		List<Player> players = new ArrayList<Player>();
 		players.add(p1);
 		players.add(p2);
 		players.add(p3);
 		List<Card> cards = new ArrayList<Card>();
 		cards.add(c1);
 		cards.add(c2);
 		cards.add(c3);
 		cards.add(c4);
 		cards.add(c5);
 		HandEvaluator evaluator = new HandEvaluator(players, cards);
 		List<PlayerHand> playerPositions = evaluator.getPlayerHandEvaluation();
 		for (PlayerHand entry : playerPositions) {
 			System.out.println(entry.getPosition() + "position - PlayerID:"
 					+ entry.getPlayer().getId() + " with " + entry.getHand()
					+ "| SCORE1: " + entry.getHandScore() + "| SCORE2: "
					+ entry.getHandScore2());
 		}
 	}
 
 }
