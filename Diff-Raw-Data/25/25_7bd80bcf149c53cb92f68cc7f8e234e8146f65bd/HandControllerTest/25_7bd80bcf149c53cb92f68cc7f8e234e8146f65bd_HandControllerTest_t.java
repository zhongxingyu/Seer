 /**
  * 
  */
 package test.client.controller;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.ArrayList;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import client.controller.HandController;
 import client.model.Card;
 import client.model.CommunityCards;
 import client.model.Deck;
 import client.model.Player;
 
 /**
  * @author Vernon Schwehr
  *
  */
 public class HandControllerTest {
 
 	private static final int STARTING_POT = 500;
 	private static HandController testHc;
 	private static ArrayList<Player> testPlayers;
 	private static CommunityCards cards;
 	
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		testPlayers = new ArrayList<Player>();
 		testPlayers.add(new Player("TestPlayer1",STARTING_POT));
 		testPlayers.add(new Player("TestPlayer2",STARTING_POT));
 		testPlayers.add(new Player("TestPlayer3",STARTING_POT));
 		testPlayers.add(new Player("TestPlayer4",STARTING_POT));
 		testPlayers.add(new Player("TestPlayer5",STARTING_POT));
 		
 		cards = new CommunityCards(new Deck(), 0);
 	}
 	
 	@Test
 	public void testRoyalFlush() {
 		cards.clear();
 		cards.add(new Card(Deck.HEART, 1));
 		cards.add(new Card(Deck.HEART, 13));
 		cards.add(new Card(Deck.HEART, 12));
 		cards.add(new Card(Deck.HEART, 11));
 		cards.add(new Card(Deck.HEART, 10));
 		
 		testHc = new HandController(testPlayers, cards);
 		
 		testHc.doAnalyzeCCards();
 		assertEquals(HandController.ROYAL_FLUSH, testHc.getcCardPoints());
 	}
 	
 	@Test
 	public void testStraightFlush() {
 		cards.clear();
 		cards.add(new Card(Deck.HEART, 10));
 		cards.add(new Card(Deck.HEART, 9));
 		cards.add(new Card(Deck.HEART, 8));
 		cards.add(new Card(Deck.HEART, 7));
 		cards.add(new Card(Deck.HEART, 6));
 		
 		testHc = new HandController(testPlayers, cards);
 		
 		testHc.doAnalyzeCCards();
 		assertEquals(HandController.STRAIGHT_FLUSH, testHc.getcCardPoints());
 	}
 	
 	@Test
 	public void testFour() {
 		cards.clear();
 		cards.add(new Card(Deck.HEART, 6));
 		cards.add(new Card(Deck.CLUB, 6));
 		cards.add(new Card(Deck.SPADE, 6));
 		cards.add(new Card(Deck.DIAMOND, 6));
 		cards.add(new Card(Deck.HEART, 12));
 		
 		testHc = new HandController(testPlayers, cards);
 		
 		testHc.doAnalyzeCCards();
 		assertEquals(HandController.FOUR_OF_A_KIND, testHc.getcCardPoints());
 	}
 	
 	@Test
 	public void testFullHouse() {
 		cards.clear();
 		cards.add(new Card(Deck.HEART, 12));
 		cards.add(new Card(Deck.SPADE, 12));
 		cards.add(new Card(Deck.CLUB, 12));
 		cards.add(new Card(Deck.HEART, 11));
 		cards.add(new Card(Deck.DIAMOND, 11));
 		
 		testHc = new HandController(testPlayers, cards);
 		
 		testHc.doAnalyzeCCards();
 		assertEquals(HandController.FULL_HOUSE, testHc.getcCardPoints());
 	}
 
 	@Test
 	public void testFlush() {
 		cards.clear();
 		cards.add(new Card(Deck.HEART, 2));
 		cards.add(new Card(Deck.HEART, 3));
 		cards.add(new Card(Deck.HEART, 12));
 		cards.add(new Card(Deck.HEART, 11));
 		cards.add(new Card(Deck.HEART, 10));
 		
 		testHc = new HandController(testPlayers, cards);
 		
 		testHc.doAnalyzeCCards();
 		assertEquals(HandController.FLUSH, testHc.getcCardPoints());
 	}
 	
 	@Test
 	public void testStraight() {
 		cards.clear();
 		cards.add(new Card(Deck.CLUB, 4));
 		cards.add(new Card(Deck.DIAMOND, 5));
 		cards.add(new Card(Deck.SPADE, 6));
 		cards.add(new Card(Deck.HEART, 7));
 		cards.add(new Card(Deck.DIAMOND, 8));
 		
 		testHc = new HandController(testPlayers, cards);
 		
 		testHc.doAnalyzeCCards();
 		assertEquals(HandController.STRAIGHT, testHc.getcCardPoints());
 	}
 	
 	@Test
 	public void testThree() {
 		cards.clear();
 		cards.add(new Card(Deck.HEART, 10));
 		cards.add(new Card(Deck.SPADE, 10));
 		cards.add(new Card(Deck.DIAMOND, 10));
 		cards.add(new Card(Deck.DIAMOND, 3));
 		cards.add(new Card(Deck.DIAMOND, 13));
 		
 		testHc = new HandController(testPlayers, cards);
 		
 		testHc.doAnalyzeCCards();
 		assertEquals(HandController.THREE_OF_A_KIND, testHc.getcCardPoints());
 	}
 	
 	@Test
 	public void testTwoPair() {
 		cards.clear();
 		cards.add(new Card(Deck.HEART, 4));
 		cards.add(new Card(Deck.SPADE, 4));
 		cards.add(new Card(Deck.DIAMOND, 7));
 		cards.add(new Card(Deck.HEART, 7));
 		cards.add(new Card(Deck.HEART, 3));
 		
 		testHc = new HandController(testPlayers, cards);
 		
 		testHc.doAnalyzeCCards();
 		assertEquals(HandController.TWO_PAIR, testHc.getcCardPoints());
 	}
 	
 	@Test
 	public void testPair() {
 		cards.clear();
		cards.add(new Card(Deck.HEART, 13));
		cards.add(new Card(Deck.DIAMOND, 13));
 		cards.add(new Card(Deck.HEART, 12));
 		cards.add(new Card(Deck.HEART, 3));
 		cards.add(new Card(Deck.HEART, 4));
 		
 		testHc = new HandController(testPlayers, cards);
 		
 		testHc.doAnalyzeCCards();
 		assertEquals(HandController.PAIR, testHc.getcCardPoints());
 	}
 	
 	@Test
 	public void testHigh() {
 		cards.clear();
 		cards.add(new Card(Deck.HEART, 3));
 		cards.add(new Card(Deck.DIAMOND, 13));
 		cards.add(new Card(Deck.SPADE, 1));
 		cards.add(new Card(Deck.SPADE, 11));
 		cards.add(new Card(Deck.HEART, 10));
 		
 		testHc = new HandController(testPlayers, cards);
 		
 		testHc.doAnalyzeCCards();
 		assertEquals(HandController.HIGH_CARD, testHc.getcCardPoints());
 	}
 
 }
