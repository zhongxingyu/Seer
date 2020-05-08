 package com.github.kpacha.jkata.pokerhand.test;
 
 import junit.framework.TestCase;
 
 import org.junit.Test;
 
 import com.github.kpacha.jkata.pokerhand.PokerCard;
 import com.github.kpacha.jkata.pokerhand.PokerHand;
 
 public class PokerHandTest extends TestCase {
 
     @Test
     public void testGetFiveAsNumericValue() {
 	PokerCard card = new PokerCard("5H");
 	assertEquals(5, card.getNumericValue());
     }
 
     @Test
     public void testGetNineAsNumericValue() {
 	PokerCard card = new PokerCard("9H");
 	assertEquals(9, card.getNumericValue());
     }
 
     @Test
     public void testGetJackAsNumericValue() {
 	PokerCard card = new PokerCard("JH");
	assertEquals(10, card.getNumericValue());
     }
 
     @Test
     public void testGetQueenAsNumericValue() {
 	PokerCard card = new PokerCard("QH");
	assertEquals(11, card.getNumericValue());
     }
 
     @Test
     public void testGetKingAsNumericValue() {
 	PokerCard card = new PokerCard("KH");
	assertEquals(12, card.getNumericValue());
     }
 
     @Test
     public void testGetAceAsNumericValue() {
 	PokerCard card = new PokerCard("AH");
	assertEquals(13, card.getNumericValue());
     }
 
     @Test
     public void testGetHigherCard() {
 	PokerCard higherCard = new PokerCard("AH");
 	PokerCard lowerCard = new PokerCard("5H");
 	assertTrue(higherCard.compareTo(lowerCard) > 0);
     }
 
     @Test
     public void testFindHigherCardOfAHand() {
 	PokerHand hand = new PokerHand("AH", "5H", "9S", "3S", "2C");
 	assertEquals("High Card : A", hand.findHand());
     }
 
     @Test
     public void testDetectAPair() {
 	PokerHand hand = new PokerHand("AH", "5H", "9S", "3S", "9C");
 	assertEquals("Pair : 9", hand.findHand());
     }
 
     @Test
     public void testDetectTwoPairs() {
 	PokerHand hand = new PokerHand("AH", "5H", "9S", "5S", "9C");
 	assertEquals("Two Pairs : 9&5", hand.findHand());
     }
 
     @Test
     public void testDetectThreeOfAKind() {
 	PokerHand hand = new PokerHand("AH", "5H", "5C", "5S", "9C");
 	assertEquals("Three Of A Kind : 5", hand.findHand());
     }
 
     @Test
     public void testDetectFourOfAKind() {
 	PokerHand hand = new PokerHand("AH", "5H", "5C", "5S", "5D");
 	assertEquals("Poker : 5", hand.findHand());
     }
 
     @Test
     public void testDetectStraight() {
 	PokerHand hand = new PokerHand("2H", "5H", "4C", "3S", "6D");
 	assertEquals("Straight : 2-6", hand.findHand());
     }
 }
