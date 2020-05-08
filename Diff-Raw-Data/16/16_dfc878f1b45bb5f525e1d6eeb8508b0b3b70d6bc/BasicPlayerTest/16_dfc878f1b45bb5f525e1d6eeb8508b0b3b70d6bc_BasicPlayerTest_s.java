 package tests;
 
 import static org.junit.Assert.*;
 
 import manager.Round;
 
 import org.junit.Test;
 
 import cards.*;
 import player.*;
 import player.Action.Type;
 
 import player.Phase1Player;
 
 public class BasicPlayerTest {
 
	//TODO: Fix the testAction() test
 	
 	@Test
 	public void testAction() {
 		Phase1Player player = new Phase1Player(100, 30, 25, 25);
 		Pile communityCards = Deck.getPile("As9h2d");
 		player.addCard(new Card("Ac"));
 		player.addCard(new Card("5d"));
 		//Player has 1 pair on flop and should call!
 		assertTrue(player.act(Round.FLOP, communityCards, 4, 0, 6).getType() == Type.CALL);
 		
 		//Player has a set and should raise the flop.
 		player.clearHand();
 		player.addCard(new Card("Ac"));
 		player.addCard(new Card("As"));
 		assertTrue(player.act(Round.FLOP, communityCards, 4, 0, 6).getType() == Type.RAISE);
 	}
 
 	@Test
 	public void testAddCard() {
 		Phase1Player player = new Phase1Player(100, 30, 25, 25);
 		player.addCard(Deck.getCard("5d"));
 		player.addCard(Deck.getCard("9s"));
 		assertEquals(Suit.DIAMONDS,player.getHand().getCard(0).getSuit());
 		assertEquals(Suit.SPADES,player.getHand().getCard(1).getSuit());
 		assertEquals(Value.FIVE,player.getHand().getCard(0).getValue());
 		assertEquals(Value.NINE,player.getHand().getCard(1).getValue());
 	}
 	
 	@Test
 	public void testStack() {
 		Phase1Player player = new Phase1Player(100, 30, 25, 25);
 		assertEquals(100, player.getStackSize());
 		player.updateStack(11);
 		assertEquals(111, player.getStackSize());
 		player.updateStack(-21);
 		assertEquals(90, player.getStackSize());
 	}
 
 }
