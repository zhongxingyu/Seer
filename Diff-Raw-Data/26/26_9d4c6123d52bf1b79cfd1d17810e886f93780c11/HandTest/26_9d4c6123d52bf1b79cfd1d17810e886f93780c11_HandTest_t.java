 /*******************************************************************************
  * CS544 Computer Networks Spring 2013
  * 5/26/2013 - HandTest.java
  * Group Members
  * o Jennifer Lautenschlager
  * o Constantine Lazarakis
  * o Carol Greco
  * o Duc Anh Nguyen
  * 
  * Purpose: Unit test of Hand.java.
  ******************************************************************************/
 package drexel.edu.blackjack.test.cards;
 
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.mock;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Test;
 
 import drexel.edu.blackjack.cards.Card;
 import drexel.edu.blackjack.cards.DealtCard;
 import drexel.edu.blackjack.cards.Hand;
 import drexel.edu.blackjack.server.game.User;
 
 public class HandTest {
 	
 	@Test
 	public void testThreeCardsNoAce(){
 		User user = mock(User.class);
 		Hand hand = new Hand(user);
 		DealtCard c1 = new DealtCard(new Card(Card.RANK.EIGHT, Card.SUIT.CLUBS), true);
 		DealtCard c2 = new DealtCard(new Card(Card.RANK.JACK, Card.SUIT.CLUBS), true);
 		DealtCard c3 = new DealtCard(new Card(Card.RANK.SIX, Card.SUIT.CLUBS), true);
 		hand.receiveCard(c1);
 		hand.receiveCard(c2);
 		hand.receiveCard(c3);
 		List<Integer> vs = hand.getPossibleValues();
 		assertEquals(1, vs.size());
 		assertEquals(8+10+6, (int) vs.get(0));
 		assertEquals(true, hand.getIsBusted());
 		assertEquals(false, hand.getIsBlackJack());
 	}
 	
 	@Test
	public void testFourCards() {
		User user = mock(User.class);
		Hand hand = new Hand(user);
		DealtCard c1 = new DealtCard(new Card(Card.RANK.SEVEN, Card.SUIT.HEARTS), true);
		DealtCard c2 = new DealtCard(new Card(Card.RANK.JACK, Card.SUIT.CLUBS), true);
		DealtCard c3 = new DealtCard(new Card(Card.RANK.TWO, Card.SUIT.CLUBS), true);
		hand.receiveCard(c1);
		hand.receiveCard(c2);
		hand.receiveCard(c3);
		assertFalse(hand.getDealerShouldHit(null));
	}
	
	@Test
 	public void testTwoCardsOneAce() {
 		User user = mock(User.class);
 		Hand hand = new Hand(user);
 		DealtCard c1 = new DealtCard(new Card(Card.RANK.ACE, Card.SUIT.CLUBS), true);
 		DealtCard c2 = new DealtCard(new Card(Card.RANK.FIVE, Card.SUIT.CLUBS), true);
 		hand.receiveCard(c1);
 		hand.receiveCard(c2);
 		List<Integer> vs = hand.getPossibleValues(); //6, 16
 		assertEquals(2, vs.size());
 		assertEquals(6*16, (int) vs.get(0)*vs.get(1));
 		assertEquals(false, hand.getIsBusted());
 		assertEquals(false, hand.getIsBlackJack());
		assertTrue(hand.getDealerShouldHit(null));
 		ArrayList<String> rules = new ArrayList<String>();
 		rules.add("Dealer must reach soft 16 to stand");
		assertFalse(hand.getDealerShouldHit(rules));
 	}
 	
 	@Test
 	public void testThreeCardsTwoAces() {
 		User user = mock(User.class);
 		Hand hand = new Hand(user);
 		DealtCard c1 = new DealtCard(new Card(Card.RANK.ACE, Card.SUIT.CLUBS), true);
 		DealtCard c2 = new DealtCard(new Card(Card.RANK.ACE, Card.SUIT.CLUBS), true);
 		DealtCard c3 = new DealtCard(new Card(Card.RANK.SIX, Card.SUIT.CLUBS), true);
 		hand.receiveCard(c1);
 		hand.receiveCard(c2);
 		hand.receiveCard(c3);
 		List<Integer> vs = hand.getPossibleValues(); //8, 18, 28
 		assertEquals(3, vs.size());
 		assertEquals(8*18*28, (int) vs.get(0)*vs.get(1)*vs.get(2));
 		assertEquals(false, hand.getIsBusted());
 		assertEquals(false, hand.getIsBlackJack());
 	}
 	
 	@Test
 	public void testBlackJack() {
 		User user = mock(User.class);
 		Hand hand = new Hand(user);
 		DealtCard c1 = new DealtCard(new Card(Card.RANK.ACE, Card.SUIT.CLUBS), true);
 		DealtCard c2 = new DealtCard(new Card(Card.RANK.JACK, Card.SUIT.CLUBS), true);
 		hand.receiveCard(c1);
 		hand.receiveCard(c2);
 		List<Integer> vs = hand.getPossibleValues(); //11, 21
 		assertEquals(2, vs.size());
 		assertEquals(11*21, (int) vs.get(0)*vs.get(1));
 		assertEquals(false, hand.getIsBusted());
 		assertEquals(true, hand.getIsBlackJack());
 	}
 }
