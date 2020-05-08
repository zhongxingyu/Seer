 import static org.junit.Assert.*;
 
 import java.awt.Color;
 import java.util.ArrayList;
 
 import org.junit.Test;
 
 
 public class PlayerTest {
 
 	@Test
 	public void testPlayerInitiate() {
 		assertNotNull(new Player());
 	}
 
 	@Test
 	public void testInitiatedValues() {
 		Player play = new Player();
 		assertEquals(1, play.blackTurns);
 		assertEquals(0, play.money);
 	}
 	
 	@Test
 	public void testNewTurn() {
 		Player player = new Player();
 		assertEquals(1, player.blackTurns);
 		assertEquals(0, player.money);
 	}
 	
 	@Test
 	public void testPlayerSetup() {
 		Player player = new Player();
 		player.setup();
 		
 		assertEquals(5, player.hand.size());
 		assertEquals(4, player.getBag().size());
 		assertEquals(0, player.discard.size());
 	}
 	
 	@Test
 	public void testEndTurn() {
 		Player player = new Player();
 		player.setup();
 		player.endTurn();
 		assertEquals(5, player.hand.size());
 		assertEquals(4, player.bag.size());
 		assertEquals(0, player.discard.size());
 	}
 	
 	@Test
 	public void testMultipleEndTurns() {
 		Player player = new Player();
 		player.setup();
 		player.endTurn();
 		
 		player.drawFromBag(3);
 		assertEquals(8, player.hand.size());
 		assertEquals(1, player.getBag().size());
 		assertEquals(0, player.discard.size());
 		
 		player.endTurn();
 		assertEquals(5, player.hand.size());
 		assertEquals(0, player.discard.size());
 		
 		player.endTurn();
 		assertEquals(5, player.hand.size());
 		assertEquals(4, player.bag.size());
 		assertEquals(0, player.discard.size());
 	}
 	
 	@Test
 	public void testTotalGemValue() {
 		Player player = new Player();
 		player.setup();
 		player.gemPile[1] += 1;
 		player.gemPile[2] += 1;
 		player.gemPile[3] += 1;
 		assertEquals(10, player.totalGemValue());
 	}
 	
 	@Test
 	public void testCanUseCard() {
 		Player player = new Player();
 		ArrayList<CardColor> w = new ArrayList<CardColor>();
 		w.add(CardColor.PURPLE);
 		ArrayList<Integer> e = new ArrayList<Integer>();
 		e.add(23);
 		Card card = new Card("CrashGem", w, 1, CardType.CIRCLE, e, false, 1, 1, 1, 0);
 		assertTrue(player.canUseCard(card));
 		
 		player.blackTurns = 0;
 		assertFalse(player.canUseCard(card));
 	}
 	
 	@Test
 	public void testCanUsePurpleCard() {
 		Player player = new Player();
 		player.blackTurns = 0;
 		player.purpleTurns = 1;
 		ArrayList<CardColor> w = new ArrayList<CardColor>();
 		w.add(CardColor.PURPLE);
 		ArrayList<Integer> e = new ArrayList<Integer>();
 		e.add(23);
 		Card card = new Card("CrashGem", w, 1, CardType.CIRCLE, e, false, 1, 1, 1, 0);
 		assertTrue(player.canUseCard(card));
		
		card = new Card();
		assertFalse(player.canUseCard(card));
 	}
 	
 	@Test
 	public void testTotalMoney() {
 		Player player = new Player();
 		Card gem = new Card();
 		player.hand.add(gem);
 		player.hand.add(gem);
 		assertEquals(2, player.totalMoney());
 		
 		ArrayList<CardColor> w = new ArrayList<CardColor>();
 		w.add(CardColor.GREEN);
 		ArrayList<Integer> e = new ArrayList<Integer>();
 		e.add(13);
 		Card gem3 = new Card("3Gem", w, 1, CardType.CIRCLE, e, false, 3);
 		player.hand.add(gem3);
 		assertEquals(5, player.totalMoney());
 	}
 	
 	@Test
 	public void testTotalMoneyWithDrawFromBag() {
 		Player player = new Player();
 		player.setup();
 		
 		assertTrue(player.totalMoney() > 0);
 	}
 	
 	@Test
 	public void testAddToDiscard() {
 		Player player = new Player();
 		Card card = new Card();
 		assertEquals(0, player.discard.size());
 		player.addToDiscard(card);
 		assertEquals(1, player.discard.size());
 		player.addToDiscard(card);
 		assertEquals(2, player.discard.size());
 	}
 	
 	@Test
 	public void testAddToUse() {
 		Player player = new Player();
 		assertEquals(0, player.toUse.size());
 		player.addToUse(10);
 		assertEquals(1, player.toUse.size());
 		player.addToUse(2);
 		assertEquals(2, player.toUse.size());
 	}
 }
