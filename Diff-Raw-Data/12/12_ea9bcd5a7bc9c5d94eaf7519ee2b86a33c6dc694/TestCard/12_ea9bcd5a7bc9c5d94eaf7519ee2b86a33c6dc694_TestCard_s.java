 package com.blackjack.testcases;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.blackjack.cards.Card;
 import com.blackjack.cards.Card.Rank;
 import com.blackjack.cards.Card.Suit;
 
 public class TestCard {
 	ArrayList<Card> deck1;
 	ArrayList<Card> deck2;
 
 	@Before
 	public void setUp() throws Exception {
 
 	}
 
 	private void makeDecks() {
 		deck1 = Card.newDeck();
 		deck2 = Card.newDeck();
 	}
 
 	@Test
 	public void testRank() {
 
 		makeDecks();
 		Card c1 = deck1.get(0);
 		Card c2 = deck2.get(0);
 		assertTrue(c1.equals(c2));
 		c2 = deck2.get(1);
 		assertFalse(c1.equals(c2));
 	}
 	
 	@Test
 	public void testIsRank() {
 		Card c = Card.makeCard(Rank.ACE, Suit.CLUBS);
 		assertTrue(c.isRank(Rank.ACE));
 		assertFalse(c.isRank(Rank.EIGHT));
 	}
 	
 	@Test
 	public void testIsNotRank() {
 		Card c = Card.makeCard(Rank.ACE, Suit.CLUBS);
 		assertTrue(c.isNotRank(Rank.TWO));
 		assertFalse(c.isNotRank(Rank.ACE));
 	}
 
 
 	@Test
 	public void testSuit() {
 		makeDecks();
 		Iterator<Card> cardIter = deck1.iterator();
 		System.out.println(deck1.toString());
 		for (Suit suit : Card.Suit.values()) {
 			for (Rank rank : Card.Rank.values()) {
 				Card c = cardIter.next();
 				Rank r = c.rank();
 				int faceValue = c.faceValue();
 				assertTrue(c.rank().equals(rank));
 			}
 		}
 
 	}
 
 	@Test
 	public void testToString() {
 		makeDecks();
 		Iterator<Card> cardIter = deck1.iterator();
 		for (Suit suit : Card.Suit.values()) {
 			for (Rank rank : Card.Rank.values()) {
 				Card c = cardIter.next();
 				assertTrue(c.toString().equals(
 						c.rank().toString() + " of " + c.suit().toString()));
 			}
 		}
 	}
 
 	@Test
 	public void testNewDeck() {
 		makeDecks();
 		assertTrue(deck1.equals(deck2));
 		deck2.remove(0);
 		assertFalse(deck1.equals(deck2));
 	}
 
 	@Test
 	public void testCompareTo() {
 		makeDecks();
 		Iterator<Card> cardIter = deck1.iterator();
 		System.out.println(deck1.toString());
 		Collections.shuffle(deck1);
 		System.out.println(deck1.toString());
 		Collections.sort(deck1);
 		System.out.println(deck1.toString());
 		for (Rank r: Card.Rank.values()) {
 			for (Suit s:Card.Suit.values()) {
 				Card c = cardIter.next();
 				assertTrue(c.rank().equals(r));
 				assertTrue(c.suit().equals(s));
 			}
 		}
 	}
 }
