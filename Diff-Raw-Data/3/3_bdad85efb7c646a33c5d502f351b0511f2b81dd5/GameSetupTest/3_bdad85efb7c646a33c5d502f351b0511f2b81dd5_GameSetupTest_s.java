 package tests;
 
 import static org.junit.Assert.fail;
 
 import java.util.ArrayList;
 
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import clue.Board;
 import clue.Card;
 import clue.Player;
 
 public class GameSetupTest {
 	private static ArrayList<Player> players;
 	static Board testBoard;
 	@BeforeClass
 	public static void setUp() throws Exception {
 		testBoard = new Board("NKLayout.txt", "NKLegend.txt", "Players.txt", "Weapons.txt");
 		players = new ArrayList<Player>();
 		players = testBoard.getPlayers();
 	}
 
 	@Test
 	public void testLoadingPlayers() {
 		Player hp = players.get(0);
 		Assert.assertEquals(hp.getName(), "Craig");
 		Assert.assertEquals(hp.getColor(), "Blue");
 		Assert.assertEquals(hp.getStartingLocation(), testBoard.calcIndex(1, 4));
 		
 		Player cp1 = players.get(1);
 		Assert.assertEquals(cp1.getName(), "Lars");
 		Assert.assertEquals(cp1.getColor(), "Red");
 		Assert.assertEquals(cp1.getStartingLocation(), testBoard.calcIndex(1, 14));
 		
 		Player cp2 = players.get(5);
 		Assert.assertEquals(cp2.getName(), "Panda");
 		Assert.assertEquals(cp2.getColor(), "Black");
 		Assert.assertEquals(cp2.getStartingLocation(), testBoard.calcIndex(11, 16));
 		
 	}
 
 	@Test
 	public void testLoadingCards() {
 		ArrayList<Card> cards = testBoard.getCards();
 		// check total number of cards
 		Assert.assertEquals(cards.size(), 21);
 		
 		// check number of types of cards
 		int numberOfWeaponCards = 0;
 		int numberOfRoomCards = 0;
 		int numberOfPersonCards = 0;
 		for( Card card : cards ) {
 			if( card.getType() == Card.CardType.WEAPON ) numberOfWeaponCards++;
 			if( card.getType() == Card.CardType.ROOM ) numberOfRoomCards++;
 			if( card.getType() == Card.CardType.PERSON ) numberOfPersonCards++;
 		}
 		Assert.assertEquals(numberOfWeaponCards, 6);
 		Assert.assertEquals(numberOfPersonCards, 6);
 		Assert.assertEquals(numberOfRoomCards, 9);
 		
 		// check if specific cards are in the deck
 		Card weapon = new Card("pile of dirt", Card.CardType.WEAPON);
 		Assert.assertTrue(cards.contains(weapon));
 		
 	}
 	
 	@Test
 	public void testDealingCards() {
 		fail("failing");
 	}
 }
