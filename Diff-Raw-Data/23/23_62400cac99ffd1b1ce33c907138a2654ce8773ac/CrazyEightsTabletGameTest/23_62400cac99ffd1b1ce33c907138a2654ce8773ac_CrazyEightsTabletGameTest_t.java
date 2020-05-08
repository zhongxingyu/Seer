 package com.worthwhilegames.cardgames.crazyeights.test;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.worthwhilegames.cardgames.crazyeights.C8Constants;
 import com.worthwhilegames.cardgames.crazyeights.CrazyEightsTabletGame;
 import com.worthwhilegames.cardgames.shared.Card;
 import com.worthwhilegames.cardgames.shared.Player;
 
 /**
  * This class will test the game logic behind the crazy eights game
  */
 public class CrazyEightsTabletGameTest {
 
 	private CrazyEightsTabletGame game;
 	private Iterator<Card> iter;
 
 	/**
 	 * This method will be performed before every test
 	 */
 	@Before
 	public void setup(){
 		Player p1 = new Player();
 		Player p2 = new Player();
 
 		game = CrazyEightsTabletGame.getInstance();
 
 		game.addPlayer(p1);
 		game.addPlayer(p2);
 	}
 
 	/**
 	 * Method to set up the initial game
 	 */
 	public void setupGame(){
 		game.setup();
 		iter = game.getShuffledDeck().iterator();
 	}
 
 	/**
 	 * Test the number of players in the game
 	 */
 	@Test
 	public void testNumberOfPlayersTwo(){
 		setupGame();
 		assertEquals("Testing number of players.", 2, game.getPlayers().size());
 	}
 
 	/**
 	 * Test the number of players in the game
 	 */
 	@Test
 	public void testNumberOfPlayersThree(){
 		Player player3 = new Player();
 		game.addPlayer(player3);
 		setupGame();
 		assertEquals("Testing number of players.", 3, game.getPlayers().size());
 	}
 
 	/**
 	 * Test the number of players in the game
 	 */
 	@Test
 	public void testNumberOfPlayersFour(){
 		Player player3 = new Player();
 		Player player4 = new Player();
 		game.addPlayer(player3);
 		game.addPlayer(player4);
 		setupGame();
 		assertEquals("Testing number of players.", 4, game.getPlayers().size());
 	}
 
 	/**
 	 * Test the number of cards in the discard pile after the deal
 	 */
 	@Test
 	public void testSizeOfDiscardPile(){
 		setupGame();
 		assertEquals("Testing size of the discard pile.", 1,game.getDiscardPile().size());
 	}
 
 	/**
 	 * Test the size of the shuffled deck after the deal with two players
 	 */
 	@Test
 	public void testShuffleDeckSizeTwoPlayers(){
 		setupGame();
 		assertEquals("Testing size of the shuffled deck.", 43, game.getShuffledDeck().size());
 	}
 
 	/**
 	 * Test the size of the shuffled deck after the deal with three players
 	 */
 	@Test
 	public void testShuffleDeckSizeThreePlayers(){
 		Player player3 = new Player();
 		game.addPlayer(player3);
 		setupGame();
 		assertEquals("Testing size of the shuffled deck.", 38, game.getShuffledDeck().size());
 	}
 
 	/**
 	 * Test the size of the shuffled deck after the deal with four players
 	 */
 	@Test
 	public void testShuffleDeckSizeFourPlayers(){
 		Player player3 = new Player();
 		Player player4 = new Player();
 		game.addPlayer(player3);
 		game.addPlayer(player4);
 		setupGame();
 		assertEquals("Testing size of the shuffled deck.", 33, game.getShuffledDeck().size());
 	}
 
 	/**
 	 * Test the size of player 1's hand after the deal
 	 */
 	@Test
 	public void testPlayer1NumberOfCard(){
 		setupGame();
 		assertEquals("Testing size of Player 1's hand.", C8Constants.NUMBER_OF_CARDS_PER_HAND, game.getPlayers().get(0).getCards().size());
 	}
 
 	/**
 	 * Test the size of player 2's hand after the deal
 	 */
 	@Test
 	public void testPlayer2NumberOfCard(){
 		setupGame();
 		assertEquals("Testing size of Player 2's hand.", C8Constants.NUMBER_OF_CARDS_PER_HAND, game.getPlayers().get(1).getCards().size());
 	}
 
 	/**
 	 * Test the iterator of the shuffled deck
 	 */
 	@Test
 	public void testIterNextAndRemove(){
 		setupGame();
 		iter.next();
 		iter.remove();
 
 		assertEquals("Testing iter.next().", 42, game.getShuffledDeck().size());
 	}
 
 	/**
 	 * Test the discard method
 	 */
 	@Test
 	public void testDiscard(){
 		setupGame();
 		Player p = game.getPlayers().get(0);
 		Card toDiscard = p.getCards().get(0);
 
 		assertEquals("Testing size of players hand", 5, p.getCards().size());
 		assertEquals("Testing size of players hand", 5, p.getNumCards());
 		assertEquals("Testing size of the discard pile", 1, game.getDiscardPile().size());
 
 		game.discard(p, toDiscard);
 
 
 		assertEquals("Testing size of players hand", 4, p.getNumCards());
 		assertEquals("Testing size of players hand", 4, p.getCards().size());
 		assertEquals("Testing size of the discard pile", 2, game.getDiscardPile().size());
 
 	}
 
 	/**
 	 * Test the draw method
 	 */
 	@Test
 	public void testDraw(){
 		setupGame();
 		Player p = game.getPlayers().get(0);
 
 		assertEquals("Testing size of players hand", 5, p.getCards().size());
 		assertEquals("Testing size of players hand", 5, p.getNumCards());
 		assertEquals("Testing size of the discard pile", 1, game.getDiscardPile().size());
 		assertEquals("Testing size of the shuffled deck", 43, game.getShuffledDeck().size());
 
 		Card c = game.draw(p);
 
 		assertEquals("Testing size of players hand", 6, p.getCards().size());
 		assertEquals("Testing size of players hand", 6, p.getNumCards());
 		assertEquals("Testing size of the discard pile", 1, game.getDiscardPile().size());
 		assertEquals("Testing size of the shuffled deck", 42, game.getShuffledDeck().size());
 		assertEquals("Testing card returned", c, p.getCards().get(5));
 	}
 
 	/**
 	 * Test to see if the game is over based on a players hand
 	 */
 	@Test
 	public void testIsGameOver(){
 		setupGame();
 		Player p = game.getPlayers().get(0);
 		List<Card> cards = p.getCards();
 
 		assertEquals("Testing size of players hand", 5, p.getCards().size());
 		assertEquals("Testing size of players hand", 5, p.getNumCards());
 
 		p.removeCard(cards.get(0));
 		assertEquals("Testing size of players hand", 4, p.getCards().size());
 
 		p.removeCard(cards.get(0));
 		assertEquals("Testing size of players hand", 3, p.getCards().size());
 
 		p.removeCard(cards.get(0));
 		assertEquals("Testing size of players hand", 2, p.getCards().size());
 
 		p.removeCard(cards.get(0));
 		assertEquals("Testing size of players hand", 1, p.getCards().size());
 
 		p.removeCard(cards.get(0));
 		assertEquals("Testing size of players hand", 0, p.getCards().size());
 		assertEquals("Testing size of players hand", 0, p.getNumCards());
 
 		boolean isGameOver = game.isGameOver(p);
 
 		assertEquals("Testing if the game is over", true, isGameOver);
 
 	}
 
 	/**
 	 * Test the deal method of the game with two players
 	 */
 	@Test
 	public void testDealTwoPlayers(){
 		setupGame();
 		Player p = game.getPlayers().get(0);
 		Player p2 = game.getPlayers().get(1);
 
 		assertEquals("Testing size of player 1's hand", 5, p.getCards().size());
 		assertEquals("Testing size of player 1's hand", 5, p.getNumCards());
 		assertEquals("Testing size of player 2'a hand", 5, p2.getCards().size());
 		assertEquals("Testing size of player 2's hand", 5, p2.getNumCards());
 		assertEquals("Testing size of the discard pile", 1, game.getDiscardPile().size());
 		assertEquals("Testing size of the shuffled deck", 43, game.getShuffledDeck().size());
 
 	}
 
 	/**
 	 * Test the deal method of the game with three players
 	 */
 	@Test
 	public void testDealThreePlayers(){
 
 		Player player3 = new Player();
 		game.addPlayer(player3);
 
 		Player p = game.getPlayers().get(0);
 		Player p2 = game.getPlayers().get(1);
 		Player p3 = game.getPlayers().get(2);
 
 		setupGame();
 
 		assertEquals("Testing size of player 1's hand", 5, p.getCards().size());
 		assertEquals("Testing size of player 1's hand", 5, p.getNumCards());
 		assertEquals("Testing size of player 2'a hand", 5, p2.getCards().size());
 		assertEquals("Testing size of player 2's hand", 5, p2.getNumCards());
 		assertEquals("Testing size of player 3'a hand", 5, p3.getCards().size());
 		assertEquals("Testing size of player 3's hand", 5, p3.getNumCards());
 		assertEquals("Testing size of the discard pile", 1, game.getDiscardPile().size());
 		assertEquals("Testing size of the shuffled deck", 38, game.getShuffledDeck().size());
 
 	}
 
 	/**
 	 * Test the deal method of the game with four players
 	 */
 	@Test
 	public void testDealFourPlayers(){
 		Player player3 = new Player();
 		Player player4 = new Player();
 		game.addPlayer(player3);
 		game.addPlayer(player4);
 
 		Player p = game.getPlayers().get(0);
 		Player p2 = game.getPlayers().get(1);
 		Player p3 = game.getPlayers().get(2);
 		Player p4 = game.getPlayers().get(3);
 
 		setupGame();
 
 		assertEquals("Testing size of player 1's hand", 5, p.getCards().size());
 		assertEquals("Testing size of player 1's hand", 5, p.getNumCards());
 		assertEquals("Testing size of player 2'a hand", 5, p2.getCards().size());
 		assertEquals("Testing size of player 2's hand", 5, p2.getNumCards());
 		assertEquals("Testing size of player 3's hand", 5, p3.getCards().size());
 		assertEquals("Testing size of player 3's hand", 5, p3.getNumCards());
 		assertEquals("Testing size of player 4'a hand", 5, p4.getCards().size());
 		assertEquals("Testing size of player 4's hand", 5, p4.getNumCards());
 		assertEquals("Testing size of the discard pile", 1, game.getDiscardPile().size());
 		assertEquals("Testing size of the shuffled deck", 33, game.getShuffledDeck().size());
 
 	}
 
 	/**
 	 * Test the shuffle of the discard pile
 	 */
 	@Test
 	public void testShuffleDiscardPile(){
 		setupGame();
 		Player p = game.getPlayers().get(0);
 
 		assertEquals("Testing size of players hand", 5, p.getCards().size());
 		assertEquals("Testing size of players hand", 5, p.getNumCards());
 		assertEquals("Testing size of the discard pile", 1, game.getDiscardPile().size());
 		assertEquals("Testing size of the shuffled deck", 43, game.getShuffledDeck().size());
 
 		game.draw(p);
 		assertEquals("Testing size of players hand", 6, p.getCards().size());
 		assertEquals("Testing size of players hand", 6, p.getNumCards());
 		game.discard(p, p.getCards().get(0));
 		assertEquals("Testing size of players hand", 5, p.getCards().size());
 		assertEquals("Testing size of players hand", 5, p.getNumCards());
 		assertEquals("Testing size of the discard pile", 2, game.getDiscardPile().size());
 		assertEquals("Testing size of the shuffled deck", 42, game.getShuffledDeck().size());
 
 		game.draw(p);
 		assertEquals("Testing size of players hand", 6, p.getCards().size());
 		assertEquals("Testing size of players hand", 6, p.getNumCards());
 		game.discard(p, p.getCards().get(0));
 		assertEquals("Testing size of players hand", 5, p.getCards().size());
 		assertEquals("Testing size of players hand", 5, p.getNumCards());
 		assertEquals("Testing size of the discard pile", 3, game.getDiscardPile().size());
 		assertEquals("Testing size of the shuffled deck", 41, game.getShuffledDeck().size());
 
 		game.draw(p);
 		assertEquals("Testing size of players hand", 6, p.getCards().size());
 		assertEquals("Testing size of players hand", 6, p.getNumCards());
 		game.discard(p, p.getCards().get(0));
 		assertEquals("Testing size of players hand", 5, p.getCards().size());
 		assertEquals("Testing size of players hand", 5, p.getNumCards());
 		assertEquals("Testing size of the discard pile", 4, game.getDiscardPile().size());
 		assertEquals("Testing size of the shuffled deck", 40, game.getShuffledDeck().size());
 
 		game.shuffleDiscardPile();
 		assertEquals("Testing size of the discard pile", 1, game.getDiscardPile().size());
 		assertEquals("Testing size of the shuffled deck", 43, game.getShuffledDeck().size());
 
 	}
 
 	/**
 	 * Test the shuffling of the deck
 	 */
 	@Test
 	public void testShuffleDeck(){
 		assertEquals("Testing size of the discard pile", 0, game.getDiscardPile().size());
 		assertEquals("Testing size of the shuffled deck", 54, game.getShuffledDeck().size());
 
 		game.shuffleDeck();
 
 		assertEquals("Testing size of the discard pile", 0, game.getDiscardPile().size());
 		assertEquals("Testing size of the shuffled deck", 54, game.getShuffledDeck().size());
 	}
 
 	/**
 	 * Test the setup method
 	 */
 	@Test
 	public void testSetup(){
 		setupGame();
 		assertEquals("Testing number of players.", 2, game.getPlayers().size());
 		assertEquals("Testing size of the discard pile.", 1,game.getDiscardPile().size());
 		assertEquals("Testing size of the shuffled deck.", 43, game.getShuffledDeck().size());
 	}
 
 	/**
 	 * Test the drop player method
 	 */
 	@Test
 	public void testDropPlayer(){
 		setupGame();
 		Player p = game.getPlayers().get(0);
 		game.getPlayers().get(0).setId("Mac Address");
 
 		assertEquals("Testing size of players hand", 5, p.getCards().size());
 		assertEquals("Testing size of players hand", 5, p.getNumCards());
 		assertEquals("Testing size of the discard pile", 1, game.getDiscardPile().size());
 		assertEquals("Testing size of the shuffled deck", 43, game.getShuffledDeck().size());
 
 		game.dropPlayer("Mac Address");
 
 		assertEquals("Testing size of players hand", 5, p.getCards().size());
 		assertEquals("Testing size of players hand", 5, p.getNumCards());
 		assertEquals("Testing size of the discard pile", 1, game.getDiscardPile().size());
 		assertEquals("Testing size of the shuffled deck", 43, game.getShuffledDeck().size());
 		assertEquals("Testing size of players", 2, game.getPlayers().size());
 	}
 
 	@Test
 	public void getTopOfDiscardPile(){
 		setupGame();
 		Player p = game.getPlayers().get(0);
 
 		Card c = p.getCards().get(0);
 
 		game.discard(p, c);
 
 		assertEquals("Testing discard pile top", c, game.getDiscardPileTop());
 
 	}
 
 	@Test
 	public void getNumPlayersTest(){
		setupGame();
 
 		assertEquals("Testing number of players", 2, game.getNumPlayers());
 	}
 
 	@Test
 	public void testDropGetNumPlayers(){
		setupGame();
 
 		game.getPlayers().get(0).setId("Mac Address");
 
 		game.dropPlayer("Mac Address");
 
 		assertEquals("Testing number of players", 2, game.getNumPlayers());
 	}
 
 	/**
 	 * Tear down
 	 */
 	@After
 	public void tearDown(){
		CrazyEightsTabletGame.clearInstance();
 	}
 }
