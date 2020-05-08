 package de.pokerdude;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 
 import org.junit.Test;
 
 public class CardSetTest {
 
 	private void assertCompareToEquals(Powerrating o, Powerrating o2) {		
 		assertEquals(0, o.compareTo(o2));
 	}
 	
 	@Test
 	public void testGetHighestRating() {
 		ArrayList<Card> cards = new ArrayList<Card>();
 		for(int i=2;i<=6;i+=2) {
 			cards.add(new Card(Suite.CLUBS, i));
 		}
 		for(int i=8;i<=14;i+=2) {
 			cards.add(new Card(Suite.DIAMONDS, i));
 		}
 		CardSet cardSet = new CardSet(cards);
 		assertCompareToEquals(new Powerrating(new int[] {1, 14, 12, 10, 8}), cardSet.evaluate());
 	}
 
 	@Test
 	public void testGetPairRating() {
 		ArrayList<Card> cards = new ArrayList<Card>();
 		cards.add(new Card(Suite.DIAMONDS, 2));
 		cards.add(new Card(Suite.CLUBS, 2));
 		for(int i=4;i<=8;i+=2) {
 			cards.add(new Card(Suite.CLUBS, i));
 		}
 		for(int i=10;i<=12;i+=2) {
 			cards.add(new Card(Suite.DIAMONDS, i));
 		}
 		CardSet cardSet = new CardSet(cards);
 		assertCompareToEquals(new Powerrating(new int[] {2, 2, 12, 10, 8}), cardSet.evaluate());
 	}
 
 	@Test
 	public void testGetDoublePairRating() {
 		ArrayList<Card> cards = new ArrayList<Card>();
 		cards.add(new Card(Suite.DIAMONDS, 2));
 		cards.add(new Card(Suite.CLUBS, 2));
 		cards.add(new Card(Suite.DIAMONDS, 4));
 		cards.add(new Card(Suite.CLUBS, 4));
 		for(int i=10;i<=14;i+=2) {
 			cards.add(new Card(Suite.SPADES, i));
 		}
 		CardSet cardSet = new CardSet(cards);
 		assertCompareToEquals(new Powerrating(new int[] {3, 4, 2, 14, 12}), cardSet.evaluate());
		
		cards.clear();
		cards.add(new Card(Suite.DIAMONDS, 14));
		cards.add(new Card(Suite.CLUBS, 6));
		cards.add(new Card(Suite.CLUBS, 12));
		cards.add(new Card(Suite.CLUBS, 14));
		cards.add(new Card(Suite.DIAMONDS, 11));
		cards.add(new Card(Suite.HEARTS, 6));
		cards.add(new Card(Suite.HEARTS, 8));
		cardSet = new CardSet(cards);
		assertCompareToEquals(new Powerrating(new int[] {3, 14, 6, 12, 11}), cardSet.evaluate());
		
 	}
 
 	@Test
 	public void testGetThreeOfAKindRating() {
 		ArrayList<Card> cards = new ArrayList<Card>();
 		cards.add(new Card(Suite.SPADES, 4));
 		cards.add(new Card(Suite.DIAMONDS, 4));
 		cards.add(new Card(Suite.CLUBS, 4));
 		for(int i=8;i<=14;i+=2) {
 			cards.add(new Card(Suite.HEARTS, i));
 		}
 		CardSet cardSet = new CardSet(cards);
 		assertCompareToEquals(new Powerrating(new int[] {4, 4}), cardSet.evaluate());
 	}
 
 	@Test
 	public void testGetStraightRating() {
 		ArrayList<Card> cards = new ArrayList<Card>();
 		cards.add(new Card(Suite.SPADES, 4));
 		cards.add(new Card(Suite.DIAMONDS, 5));
 		cards.add(new Card(Suite.CLUBS, 6));
 		cards.add(new Card(Suite.HEARTS, 7));
 		cards.add(new Card(Suite.CLUBS, 8));
 		for(int i=12;i<=14;i+=2) {
 			cards.add(new Card(Suite.SPADES, i));
 		}
 		CardSet cardSet = new CardSet(cards);
 		assertCompareToEquals(new Powerrating(new int[] {5, 8}), cardSet.evaluate());
 	}
 
 	@Test
 	public void testGetFlushRating() {
 		ArrayList<Card> cards = new ArrayList<Card>();
 		cards.add(new Card(Suite.SPADES, 4));
 		cards.add(new Card(Suite.DIAMONDS, 5));
 		for(int i=6;i<=14;i+=2) {
 			cards.add(new Card(Suite.CLUBS, i));
 		}
 		CardSet cardSet = new CardSet(cards);
 		assertCompareToEquals(new Powerrating(new int[] {6, 14}), cardSet.evaluate());
 	}
 
 	@Test
 	public void testGetFullHouseRating() {
 		ArrayList<Card> cards = new ArrayList<Card>();
 		cards.add(new Card(Suite.SPADES, 4));
 		cards.add(new Card(Suite.DIAMONDS, 4));
 		cards.add(new Card(Suite.HEARTS, 4));
 		cards.add(new Card(Suite.HEARTS, 10));
 		cards.add(new Card(Suite.CLUBS, 10));
 		for(int i=12;i<=14;i+=2) {
 			cards.add(new Card(Suite.CLUBS, i));
 		}
 		CardSet cardSet = new CardSet(cards);
 		assertCompareToEquals(new Powerrating(new int[] {7, 4, 10}), cardSet.evaluate());
 	}
 
 	@Test
 	public void testGetFourOfAKindRating() {
 		ArrayList<Card> cards = new ArrayList<Card>();
 		cards.add(new Card(Suite.SPADES, 4));
 		cards.add(new Card(Suite.DIAMONDS, 4));
 		cards.add(new Card(Suite.CLUBS, 4));
 		cards.add(new Card(Suite.HEARTS, 4));
 		for(int i=10;i<=14;i+=2) {
 			cards.add(new Card(Suite.HEARTS, i));
 		}
 		CardSet cardSet = new CardSet(cards);
 		assertCompareToEquals(new Powerrating(new int[] {8, 4}), cardSet.evaluate());
 	}
 
 	@Test
 	public void testGetStraightFlushRating() {
 		ArrayList<Card> cards = new ArrayList<Card>();
 		for(int i=6;i<=10;i+=1) {
 			cards.add(new Card(Suite.HEARTS, i));
 		}
 		for(int i=2;i<=4;i+=2) {
 			cards.add(new Card(Suite.DIAMONDS, i));
 		}
 		CardSet cardSet = new CardSet(cards);
 		assertCompareToEquals(new Powerrating(new int[] {9, 10}), cardSet.evaluate());
 	}
 
 }
