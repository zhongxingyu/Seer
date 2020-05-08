 package Test;
 
 
 
 import static org.junit.Assert.*;
 
 
 import java.util.ArrayList;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import clueGame.Board;
 import clueGame.BoardCell;
 import clueGame.Card;
 import clueGame.Card.CardType;
 import clueGame.ComputerPlayer;
 import clueGame.Player;
 import clueGame.RoomCell;
 
 
 public class GameSetupTests {
 	private static Board board;
 	
 	@BeforeClass
 	public static void setUp() {
 		board = new Board();
 	}
 	
 	@Test
 	public void testLoadingPeople() {
 		Map<String, Player> players = board.getPlayers();
 		Assert.assertEquals(6, players.size());
 		
 		Player humanPlayer = board.getPlayer("Professor Plum");
 		Assert.assertNotNull(humanPlayer);
 		Assert.assertEquals(16, humanPlayer.getX());
 		Assert.assertEquals(19, humanPlayer.getY());
 		
 		Player computerPlayer = board.getPlayer("Miss Scarlett");
 		Assert.assertNotNull(humanPlayer);
 		Assert.assertEquals(5, computerPlayer.getX());
 		Assert.assertEquals(0, computerPlayer.getY());
 		
 		Player computerPlayer2 = board.getPlayer("Mrs. White");
 		Assert.assertNotNull(humanPlayer);
 		Assert.assertEquals(0, computerPlayer2.getX());
 		Assert.assertEquals(12, computerPlayer2.getY());
 		
 	}
 	
 	@Test
 	public void testLoadingcards() {
 		Map<String, Card> suspect = board.getCards("suspect");
 		Map<String, Card> weapons = board.getCards("weapon");
 		Map<String, Card> rooms = board.getCards("room");
 		
 		int totalSize = suspect.size() + weapons.size() + rooms.size();
 		Assert.assertEquals(21, totalSize);
 
 		Assert.assertEquals(6, suspect.size());
 		Assert.assertEquals(6, weapons.size());
 		Assert.assertEquals(9, rooms.size());
 		
		Card c = board.getCard("suspect", "Miss Scarlett");
 		Assert.assertNotNull(c);
		c = board.getCard("weapon", "Candlestick");
 		Assert.assertNotNull(c);
		c = board.getCard("room", "Kitchen");
 		Assert.assertNotNull(c);
 	}
 	
 	@Test
 	public void testUniqueAndFairCardDistribution() {
 		board.deal();
 		Map<String, Player> players = board.getPlayers();
 		
 		int numCards = 3;
 		boolean sameNumberOfCards = true;
 		List<Card> seenCards = new LinkedList<Card>();
 		boolean seenACard = false;;
 		
 		for (Player p : players.values()) {
 			numCards += 3;
 			if (p.getCards().length != 3)
 				sameNumberOfCards = false;
 			for (Card c : p.getCards()) {
 				if (seenCards.contains(c)) {
 					seenACard = true;
 				} else {
 					seenCards.add(c);
 				}
 			}
 		}
 		
 		//6 players, 3 cards each + 1 3 card solution = 21
 		Assert.assertEquals(21, numCards);
 		//Set to false if a player does not have 3 cards
 		Assert.assertTrue(sameNumberOfCards);
 		//Set to true if we have seen a card
 		Assert.assertFalse(seenACard);
 	}
 	
 	@Test
 	public void testAccusation() {
 		Card suspect = board.getCard("Miss Scarlett", "suspect");
 		Card weapon = board.getCard("Candlestick", "weapon");
 		Card room = board.getCard("Kitchen", "room");
 		board.setAnswer(suspect, room, weapon);
 		
 		Assert.assertTrue(board.checkAccusation(suspect, room, weapon));
 		
 		suspect = board.getCard("Mrs. Peacock", "suspect");
 		Assert.assertFalse(board.checkAccusation(suspect, room, weapon));
 		
 		weapon = board.getCard("Dagger", "weapon");
 		Assert.assertFalse(board.checkAccusation(suspect, room, weapon));
 		
 		room = board.getCard("Dining Hall", "room");
 		Assert.assertFalse(board.checkAccusation(suspect, room, weapon));
 	}
 	
 	@Test
 	public void testDisprovingSuggestion() {
 		//One player, One Correct Match
 		Card suspect1 = board.getCard("Miss Scarlett", "suspect");
 		Card weapon1 = board.getCard("Candlestick", "weapon");
 		Card room1 = board.getCard("Kitchen", "room");
 		
 		Card suspect2 = board.getCard("Mrs. Peacock", "suspect");
 		Card weapon2 = board.getCard("Dagger", "weapon");
 		Card room2 = board.getCard("Dining Hall", "room");
 		
 		Card suspect3 = board.getCard("Mrs. White", "suspect");
 		Card weapon3 = board.getCard("Lead Pipe", "weapon");
 		Card room3 = board.getCard("Ballroom", "room");
 		
 		Player p = new Player("Mrs. White", 0, 0);
 		p.addCard(suspect1);
 		p.addCard(weapon1);
 		p.addCard(room1);
 		
 		//suspect is right
 		Assert.assertNotNull(p.disproveSuggestion(suspect1, weapon2, room2));
 		//weapon is right
 		Assert.assertNotNull(p.disproveSuggestion(suspect2, weapon1, room2));
 		//room is right
 		Assert.assertNotNull(p.disproveSuggestion(suspect2, weapon2, room1));
 		//none are right
 		Assert.assertNull(p.disproveSuggestion(suspect2, weapon2, room2));
 		
 		
 		//One player, multiple matches
 		int suspectDisproves = 0;
 		int weaponDisproves = 0;
 		int roomDisproves = 0;
 		
 		Card c;
 		for (int i = 0; i < 100; ++i) {
 			c = p.disproveSuggestion(suspect1, weapon1, room1);
 			if (c != null) {
 				switch (c.getCardType()) {
 				case SUSPECT:
 					suspectDisproves++;
 					break;
 				case WEAPON:
 					weaponDisproves++;
 					break;
 				case ROOM:
 					roomDisproves++;
 					break;
 				}
 			}
 		}
 		Assert.assertTrue(suspectDisproves != 0);
 		Assert.assertTrue(weaponDisproves != 0);
 		Assert.assertTrue(roomDisproves != 0);
 		
 		//Test all players are queried
 		//Only human can disprove the suspect
 		//Both computers have matching weapon
 		Player humanPlayer = board.getPlayer("Professor Plum");
 		Player computerPlayer = board.getPlayer("Miss Scarlett");
 		Player computerPlayer2 = board.getPlayer("Mrs. White");
 		
 		humanPlayer.addCard(suspect1);
 		computerPlayer.addCard(suspect2);
 		computerPlayer2.addCard(suspect2);
 		
 		humanPlayer.addCard(weapon2);
 		computerPlayer.addCard(weapon1);
 		computerPlayer2.addCard(weapon1);
 		
 		humanPlayer.addCard(room2);
 		computerPlayer.addCard(room2);
 		computerPlayer2.addCard(room2);
 		
 		List<Player> players = new ArrayList<Player>();
 		players.add(humanPlayer);
 		players.add(computerPlayer);
 		players.add(computerPlayer2);
 		
 		//check no matches
 		int matches = 0;
 		for (Player player : players) {
 			if (player.disproveSuggestion(suspect3, weapon3, room3) != null)
 				matches++;
 		}
 		Assert.assertEquals(0, matches);
 		
 		//check if human. Check if trying to find suspect1, since human is current player, should be 0
 		board.setCurrentPlayer(humanPlayer);
 		matches = 0;
 		for (Player player : players) {
 			if (player.disproveSuggestion(suspect1, weapon3, room3) != null)
 				matches++;
 		}
 		Assert.assertEquals(0, matches);
 		
 		
 		//check that handleSuggestion does not enforce an order
 		int comp1Matches = 0;
 		int comp2Matches = 0;
 		for (int i = 0; i < 100 ; i++) {
 			Player matchedPlayer = board.handleSuggestion(suspect1, room2, weapon2);
 			if (matchedPlayer.equals(computerPlayer)) {
 				comp1Matches++;
 			} else if (matchedPlayer.equals(computerPlayer2)) {
 				comp2Matches++;
 			}
 		}
 		Assert.assertTrue(comp1Matches != 0);
 		Assert.assertTrue(comp2Matches != 0);
 	}
 	
 	@Test
 	public void testSelectingTarget() {
 		//test room preference
 		ComputerPlayer testComp = new ComputerPlayer("Mrs. White", 1, 5);
 		// Test that given the choice of entering a room or moving to a walkway the computer chooses the room.
 		for(int i = 0; i < 100; i++) {
 			board.calcTargets(board.calcIndex(1, 5), 3);
 			Assert.assertTrue(testComp.chooseMove(board.getTargets()).isDoorway());
 		}
 		
 		//test random room selection
 		board.calcTargets(board.calcIndex(3, 5), 2);
 		int loc_1_5Tot = 0;
 		int loc_5_5Tot = 0;
 		int loc_2_4Tot = 0;
 		int loc_4_4Tot = 0;
 		// Run the test 100 times
 		
 		for (int i=0; i<100; i++) {
 			BoardCell selected = testComp.chooseMove(board.getTargets());
 			if (selected == board.getCellAt(board.calcIndex(1, 5)))
 				loc_1_5Tot++;
 			else if (selected == board.getCellAt(board.calcIndex(5, 5)))
 				loc_5_5Tot++;
 			else if (selected == board.getCellAt(board.calcIndex(2, 4)))
 				loc_2_4Tot++;
 			else if(selected == board.getCellAt(board.calcIndex(4, 4)))
 				loc_4_4Tot++;
 			else
 				fail("Invalid target selected");
 		}
 		// Ensure we have 100 total selections (fail should also ensure)
 		assertEquals(100, loc_1_5Tot + loc_5_5Tot + loc_2_4Tot + loc_4_4Tot);
 		// Ensure each target was selected more than once
 		assertTrue(loc_1_5Tot > 10);
 		assertTrue(loc_5_5Tot > 10);
 		assertTrue(loc_2_4Tot > 10);	
 		assertTrue(loc_4_4Tot > 10);
 		
 	}
 	
 	@Test
 	public void testMakingSuggestion() {
 		ComputerPlayer testComp = new ComputerPlayer("Mrs. White", 0, 3);
 		// add some cards to the seen ArrayList
 		board.getSeen().add(new Card("Knife", CardType.WEAPON));
 		board.getSeen().add(new Card("Professor Plum", CardType.SUSPECT));
 		board.getSeen().add(new Card("Mrs. White", CardType.SUSPECT));
 		board.getSeen().add(new Card("Rope", CardType.WEAPON));
 		board.getSeen().add(new Card("Miss Scarlett", CardType.SUSPECT));
 		board.getSeen().add(new Card("Candlestick", CardType.WEAPON));
 		
 		Card[] suggestion = testComp.makeSuggestion();
 		
 		// Check for each element in seen that it does not equal one of our guesses.
 		for(int i  = 0; i < board.getSeen().size(); i++ ) {
 			for(int j = 0; j < suggestion.length; j++) {
 				assertFalse(board.getSeen().get(i).equals(suggestion[j]));
 			}
 		}
 		
 		
 	}
 	
 }
