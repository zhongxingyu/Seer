 package unittest;
 
 import java.util.ArrayList;
 
 import game.Card;
 import game.Deck;
 import game.Hand;
 import game.enumeration.CardValue;
 import game.enumeration.Suit;
 import junit.framework.TestCase;
 import org.junit.Assert;
 
 public class HandTest extends TestCase {
 
 	public void testHand() {
 		Deck d = new Deck();
 		for(int i = 0; i < 4; ++i) {
 			try {
 				Hand h = new Hand(d);
 			} catch (Exception e) {
 				Assert.fail();
 			}
 		}
 		
 		try {
 			Hand h = new Hand(d);
 			Assert.fail();
 		} catch (Exception e) {
 			
 		}
 	}
 
 	public void testGetSetGameSuit() throws Exception {
 		Deck d = new Deck();
 		Hand h = new Hand(d);
 		h.setGameSuit(Suit.CLUBS);
 		Assert.assertEquals(h.getGameSuit(), Suit.CLUBS);
 		Assert.assertNotSame(h.getGameSuit(), Suit.SPADES);
 		Assert.assertNotSame(h.getGameSuit(), Suit.HEARTS);
 		Assert.assertNotSame(h.getGameSuit(), Suit.DIAMONDS);
 		Assert.assertNotSame(h.getGameSuit(), Suit.NONE);
 	}
 
 	public void testGetSetCards() throws Exception {
 		Deck d = new Deck();
 		Hand h = new Hand(d);
 		ArrayList<Card> cards = new ArrayList<Card>(10);
 		cards.add(new Card(Suit.SPADES, CardValue.EIGHT));
 		cards.add(new Card(Suit.DIAMONDS, CardValue.ACE));
 		cards.add(new Card(Suit.HEARTS, CardValue.FOUR));
 		cards.add(new Card(Suit.CLUBS, CardValue.EIGHT));
 		cards.add(new Card(Suit.SPADES, CardValue.NINE));
 		cards.add(new Card(Suit.DIAMONDS, CardValue.KING));
 		cards.add(new Card(Suit.CLUBS, CardValue.SIX));
 		cards.add(new Card(Suit.COLOR, CardValue.JOKER));
 		cards.add(new Card(Suit.SPADES, CardValue.TWO));
 		cards.add(new Card(Suit.HEARTS, CardValue.NINE));
 		h.setCards(cards);
 		
 		Card[] tabCards = h.getCards();
 		for(int i = 0; i < tabCards.length; ++i) {
 			boolean ret = cards.contains(tabCards[i]);
 			Assert.assertTrue(ret);
 			if(ret) {
 				cards.remove(tabCards[i]);
 			}
 		}
 	}
 
 	public void testPlayCard() throws Exception {
 		Deck d = new Deck();
 		Hand h = new Hand(d);
 		ArrayList<Card> cards = new ArrayList<Card>(10);
 		cards.add(new Card(Suit.SPADES, CardValue.EIGHT));
 		cards.add(new Card(Suit.DIAMONDS, CardValue.ACE));
 		cards.add(new Card(Suit.HEARTS, CardValue.FOUR));
 		cards.add(new Card(Suit.CLUBS, CardValue.EIGHT));
 		cards.add(new Card(Suit.SPADES, CardValue.NINE));
 		cards.add(new Card(Suit.DIAMONDS, CardValue.KING));
 		cards.add(new Card(Suit.CLUBS, CardValue.SIX));
 		cards.add(new Card(Suit.COLOR, CardValue.JOKER));
 		cards.add(new Card(Suit.SPADES, CardValue.TWO));
 		cards.add(new Card(Suit.HEARTS, CardValue.NINE));
 		h.setCards(cards);
 		
 		Assert.assertTrue(h.playCard(new Card(Suit.SPADES, CardValue.EIGHT)));
 		Assert.assertFalse(h.playCard(new Card(Suit.SPADES, CardValue.EIGHT)));
 		
 		Assert.assertTrue(h.playCard(new Card(Suit.DIAMONDS, CardValue.KING)));
 		Assert.assertFalse(h.playCard(new Card(Suit.DIAMONDS, CardValue.KING)));
 		
 		cards.remove(new Card(Suit.SPADES, CardValue.EIGHT));
 		cards.remove(new Card(Suit.DIAMONDS, CardValue.KING));
 		
 		for(int i = 0; i < 8; ++i) {
 			Assert.assertTrue(h.playCard(cards.get(i)));
 			Assert.assertFalse(h.playCard(cards.get(i)));
 		}
 	}
 
 	public void testGetPlayableCard() throws Exception {
 		Deck d = new Deck();
 		Hand h = new Hand(d);
 		h.setGameSuit(Suit.CLUBS);
 		ArrayList<Card> cards = new ArrayList<Card>(10);
 		cards.add(new Card(Suit.SPADES, CardValue.EIGHT));
 		cards.add(new Card(Suit.DIAMONDS, CardValue.ACE));
 		cards.add(new Card(Suit.HEARTS, CardValue.FOUR));
 		cards.add(new Card(Suit.CLUBS, CardValue.EIGHT));
 		cards.add(new Card(Suit.SPADES, CardValue.NINE));
 		cards.add(new Card(Suit.DIAMONDS, CardValue.KING));
 		cards.add(new Card(Suit.CLUBS, CardValue.SIX));
 		cards.add(new Card(Suit.COLOR, CardValue.JOKER));
 		cards.add(new Card(Suit.SPADES, CardValue.TWO));
 		cards.add(new Card(Suit.HEARTS, CardValue.NINE));
 		h.setCards(cards);
 		
 		ArrayList<Card> playables;
 		
 		playables = h.getPlayableCard(Suit.HEARTS);
 		Assert.assertEquals(playables.size(), 2);
 		Assert.assertTrue(playables.contains(new Card(Suit.HEARTS, CardValue.FOUR)));
 		Assert.assertTrue(playables.contains(new Card(Suit.HEARTS, CardValue.NINE)));
 		
 		h.playCard(new Card(Suit.HEARTS, CardValue.FOUR));
 		h.playCard(new Card(Suit.HEARTS, CardValue.NINE));
 		
 		playables = h.getPlayableCard(Suit.HEARTS);
 		Assert.assertEquals(playables.size(), 8);
 		
 		playables = h.getPlayableCard(Suit.SPADES);
 		Assert.assertEquals(playables.size(), 3);
 		Assert.assertTrue(playables.contains(new Card(Suit.SPADES, CardValue.EIGHT)));
 		Assert.assertTrue(playables.contains(new Card(Suit.SPADES, CardValue.NINE)));
 		Assert.assertTrue(playables.contains(new Card(Suit.SPADES, CardValue.TWO)));
 		
 		playables = h.getPlayableCard(Suit.DIAMONDS);
 		Assert.assertTrue(playables.contains(new Card(Suit.DIAMONDS, CardValue.ACE)));
 		Assert.assertTrue(playables.contains(new Card(Suit.DIAMONDS, CardValue.KING)));
 		
 		playables = h.getPlayableCard(Suit.HEARTS);
 		Assert.assertEquals(playables.size(), 8);
 	}
 
 	public void testGetNumberOfCard() throws Exception {
 		Card[] playables;
 		Deck d = new Deck();
 		Hand h = new Hand(d);
 				
 		Assert.assertEquals(h.getNumberOfCard(), 10);
 		for(int i = 9; i >= 0; --i) {
 			playables = h.getCards();
 			h.playCard(playables[0]);
 			Assert.assertEquals(h.getNumberOfCard(), i);
 		}
 		
 		ArrayList<Card> cards = new ArrayList<Card>(10);
 		cards.add(new Card(Suit.SPADES, CardValue.EIGHT));
 		cards.add(new Card(Suit.DIAMONDS, CardValue.ACE));
 		cards.add(new Card(Suit.HEARTS, CardValue.FOUR));
 		cards.add(new Card(Suit.CLUBS, CardValue.EIGHT));
 		cards.add(new Card(Suit.SPADES, CardValue.NINE));
 		cards.add(new Card(Suit.DIAMONDS, CardValue.KING));
 		cards.add(new Card(Suit.CLUBS, CardValue.SIX));
 		cards.add(new Card(Suit.COLOR, CardValue.JOKER));
 		cards.add(new Card(Suit.SPADES, CardValue.TWO));
 		cards.add(new Card(Suit.HEARTS, CardValue.NINE));
 		h.setCards(cards);
 		
 		Assert.assertEquals(h.getNumberOfCard(), 10);
 		for(int i = 9; i >= 0; --i) {
 			playables = h.getCards();
 			h.playCard(playables[0]);
 			Assert.assertEquals(h.getNumberOfCard(), i);
 		}
 	}
 
 }
