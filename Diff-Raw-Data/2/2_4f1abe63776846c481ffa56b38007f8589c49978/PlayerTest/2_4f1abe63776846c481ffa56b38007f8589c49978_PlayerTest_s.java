 package de.htwg.se.dog.models;
 
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ExpectedException;
 
 import de.htwg.se.dog.models.Card;
 
 public class PlayerTest {
 		
 		Player p1;
 		Card card2;
 		int playerNr=1;
 		int figCount=4;
 		Figure temp;
 		
 		@Rule
 		public ExpectedException expected = ExpectedException.none();
 		
 		@Before
 		public void setUp() {
 			card2 = new Card(2);
 			p1 = new Player(playerNr, figCount);
 		}
 		
 		@Test
 		public void testAddCard() {
 			assertEquals(false, p1.getCardList().contains(card2));
 			p1.addCard(card2);
 			assertEquals(true, p1.getCardList().contains(card2));
 		}
 		
 		@Test
 		public void testRemoveCard() {
 			p1.addCard(card2);
 			assertEquals(true, p1.getCardList().contains(card2));
 			assertEquals(true, p1.removeCard(card2));
 		}
 		@Test
 		public void testRemoveFigure() {
 			temp = p1.removeFigure();
 			assertEquals(p1.figure.size(),3);
 		}
 		@Test
 		public void testAddFigure() {
 			p1.addFigure(temp);
			assertEquals(p1.figure.size(),4);
 		}
 		@Test
 		public void testGetPlayerId() {
 			assertEquals(playerNr,p1.getPlayerID());
 		}
 }
