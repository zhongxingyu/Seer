 package clueGameTests;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 
 import junit.framework.Assert;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import clueGame.Board;
 import clueGame.BoardCell;
 import clueGame.Card;
 import clueGame.ComputerPlayer;
 import clueGame.HumanPlayer;
 import clueGame.Player;
 import clueGame.Solution;
 import clueGame.Suggestion;
 
 public class GameActionsTests {
 	
 	private static Board board;
 	private static Card mustardCard;
 	private static Card mrGreenCard;
 	private static Card leadPipeCard;
 	private static Card knifeCard;
 	private static Card hallCard;
 	private static Card kitchenCard;
 	
 	@BeforeClass
 	public static void setUp(){
 		board = new Board();
 		mustardCard = new Card("Colonel Mustard", Card.CardType.PERSON);
 		mrGreenCard = new Card("Mr. Green", Card.CardType.PERSON);
 		leadPipeCard = new Card("Lead Pipe", Card.CardType.WEAPON);
 		knifeCard = new Card("Knife", Card.CardType.WEAPON);
 		hallCard = new Card("Hall", Card.CardType.ROOM);
 		kitchenCard = new Card("Kitchen", Card.CardType.ROOM);
 	}
 	
 	//Test make an accusation
 	@Test
 	public void testAccusation() {
 		//Set the "answer"
 		Solution testAnswer = new Solution("Mr. Green","Kitchen", "Lead Pipe");
 		board.setSolution(testAnswer);
 		
 		//Test when accusation is correct
 		Assert.assertTrue(board.checkAccusation("Mr. Green", "Kitchen", "Lead Pipe"));
 		
 		//Test when accusation is incorrect with wrong room
 		Assert.assertFalse(board.checkAccusation("Mr. Green", "Library", "Lead Pipe"));
 
 		//Test when accusation is incorrect with wrong person
 		Assert.assertFalse(board.checkAccusation("Mrs. White", "Kitchen", "Lead Pipe"));
 
 		//Test when accusation is incorrect with wrong weapon
 		Assert.assertFalse(board.checkAccusation("Mr. Green", "Kitchen", "Revolver"));
 		
 		//Test when accusation is incorrect with all three wrong
 		Assert.assertFalse(board.checkAccusation("Colonel Mustard", "Billiard Room", "Knife"));
 	}
 	
 	//Test selecting a target location
 	//Room Preference Tests
 	@Test
 	public void testTargetRoomPreference(){
 		ComputerPlayer player = new ComputerPlayer();
 		//Test1
 		board.calcTargets(board.calcIndex(14, 4), 2);
 		int loc_Room = 0;
 		//Run the test 100 times
 		for(int i=0; i<100; i++){
 			player.pickLocation(board.getTargets());
 			BoardCell selected = board.getCells().get(player.getLocation());
 			if(selected == board.getCells().get(board.calcIndex(13, 4))){
 				loc_Room++;
 			}
 		}
 		//Assert that the room was chosen each time
 		assertEquals(100, loc_Room);
 	
 		//Test2
 		board.calcTargets(board.calcIndex(6, 15), 3);
 		loc_Room = 0;
 		//Run the test 100 times
 		for(int i=0; i<100; i++){
 			player.pickLocation(board.getTargets());
 			BoardCell selected = board.getCells().get(player.getLocation());
 			if(selected == board.getCells().get(board.calcIndex(5, 15))){
 				loc_Room++;
 			}
 		}
 		//Assert that the room was chosen each time
 		assertEquals(100, loc_Room);
 		
 		//Test3
 		board.calcTargets(board.calcIndex(13, 16), 4);
 		loc_Room = 0;
 		//Run the test 100 times
 		for(int i=0; i<100; i++){
 			player.pickLocation(board.getTargets());
 			BoardCell selected = board.getCells().get(player.getLocation());
 			if(selected == board.getCells().get(board.calcIndex(10, 17))){
 				loc_Room++;
 			}
 		}
 		//Assert that the room was chosen each time
 		assertEquals(100, loc_Room);
 	}
 	
 	//Random choice tests
 	@Test
 	public void testTargetRandomSelection(){
 		ComputerPlayer player = new ComputerPlayer();
 		// Pick a location with no rooms in target, just three targets
 		board.calcTargets(board.calcIndex(14, 0), 2);
 		int loc_12_0Tot = 0;
 		int loc_14_2Tot = 0;
 		int loc_15_1Tot = 0;
 		// Run the test 100 times
 		for (int i=0; i<100; i++) {
 			player.pickLocation(board.getTargets());
 			BoardCell selected = board.getCells().get(player.getLocation());
 			if (selected == board.getCells().get(board.calcIndex(12, 0))){
 				loc_12_0Tot++;
 			}
 			else if (selected == board.getCells().get(board.calcIndex(14, 2))){
 				loc_14_2Tot++;
 			}
 			else if (selected == board.getCells().get(board.calcIndex(15, 1))){
 				loc_15_1Tot++;
 			}
 			else{
 				fail("Invalid target selected");
 			}
 		}
 		// Ensure we have 100 total selections (fail should also ensure)
 		assertEquals(100, loc_12_0Tot + loc_14_2Tot + loc_15_1Tot);
 		// Ensure each target was selected more than once
 		assertTrue(loc_12_0Tot > 10);
 		assertTrue(loc_14_2Tot > 10);
 		assertTrue(loc_15_1Tot > 10);
 		
 		//Location with no rooms in target, just 5 targets
 		board.calcTargets(board.calcIndex(5, 0), 3);
 		int loc_8_0Tot = 0;
 		int loc_5_3Tot = 0;
 		int loc_6_0Tot = 0;
 		int loc_6_2Tot = 0;
 		int loc_5_1Tot = 0;
 		// Run the test 100 times
 		for (int i=0; i<100; i++) {
 			player.pickLocation(board.getTargets());
 			BoardCell selected = board.getCells().get(player.getLocation());
 			if (selected == board.getCells().get(board.calcIndex(8, 0))){
 				loc_8_0Tot++;
 			}
 			else if (selected == board.getCells().get(board.calcIndex(5, 3))){
 				loc_5_3Tot++;
 			}
 			else if (selected == board.getCells().get(board.calcIndex(6, 0))){
 				loc_6_0Tot++;
 			}
 			else if (selected == board.getCells().get(board.calcIndex(6, 2))){
 				loc_6_2Tot++;
 			}
 			else if (selected == board.getCells().get(board.calcIndex(5, 1))){
 				loc_5_1Tot++;
 			}
 			else{
 				fail("Invalid target selected");
 			}
 		}
 		// Ensure we have 100 total selections (fail should also ensure)
 		assertEquals(100, loc_8_0Tot + loc_5_3Tot + loc_6_0Tot + loc_6_2Tot + loc_5_1Tot);
 		// Ensure each target was selected more than once
 		assertTrue(loc_8_0Tot > 5);
 		assertTrue(loc_5_3Tot > 5);
 		assertTrue(loc_6_0Tot > 5);
 		assertTrue(loc_6_2Tot > 5);
 		assertTrue(loc_5_1Tot > 5);
 	}
 	
 	//Test a random choice is made when the room is the last visited
 	@Test
 	public void testTargetRandomSelectionRoom(){
 		ComputerPlayer player = new ComputerPlayer();
 		//Test1
 		// Pick a location with last visited room in target, six targets
 		board.calcTargets(board.calcIndex(4, 4), 2);
 		int loc_4_3Tot = 0;
 		int loc_4_6Tot = 0;
 		int loc_3_5Tot = 0;
 		int loc_6_4Tot = 0;
 		int loc_5_3Tot = 0;
 		int loc_5_5Tot = 0;
 		// Run the test 100 times
 		for (int i=0; i<100; i++) {
 			//Set the last room visited to C
 			player.setLastRoomVisited('C');
 			player.pickLocation(board.getTargets());
 			BoardCell selected = board.getCells().get(player.getLocation());
 			if (selected == board.getCells().get(board.calcIndex(4, 3))){
 				loc_4_3Tot++;
 			}
 			else if (selected == board.getCells().get(board.calcIndex(4, 6))){
 				loc_4_6Tot++;
 			}
 			else if (selected == board.getCells().get(board.calcIndex(3, 5))){
 				loc_3_5Tot++;
 			}
 			else if (selected == board.getCells().get(board.calcIndex(6, 4))){
 				loc_6_4Tot++;
 			}
 			else if (selected == board.getCells().get(board.calcIndex(5, 3))){
 				loc_5_3Tot++;
 			}
 			else if (selected == board.getCells().get(board.calcIndex(5, 5))){
 				loc_5_5Tot++;
 			}
 			else{
 				fail("Invalid target selected");
 			}
 		}
 		// Ensure we have 100 total selections (fail should also ensure)
 		assertEquals(100, loc_4_3Tot + loc_4_6Tot + loc_3_5Tot + loc_6_4Tot + loc_5_3Tot + loc_5_5Tot);
 		// Ensure each target was selected more than once
 		assertTrue(loc_4_3Tot > 5);
 		assertTrue(loc_4_6Tot > 5);
 		assertTrue(loc_3_5Tot > 5);
 		assertTrue(loc_6_4Tot > 5);
 		assertTrue(loc_5_3Tot > 5);
 		assertTrue(loc_5_5Tot > 5);
 		
 		//Test2
 		// Pick a location with last visited room in target, six targets
 		board.calcTargets(board.calcIndex(5, 8), 2);
 		int loc_5_6Tot = 0;
 		int loc_6_7Tot = 0;
 		int loc_4_8Tot = 0;
 		int loc_7_8Tot = 0;
 		int loc_6_9Tot = 0;
 		int loc_5_10Tot = 0;
 		// Run the test 100 times
 		for (int i=0; i<100; i++) {
 			//Set the last room visited to C
 			player.setLastRoomVisited('R');
 			player.pickLocation(board.getTargets());
 			BoardCell selected = board.getCells().get(player.getLocation());
 			if (selected == board.getCells().get(board.calcIndex(5, 6))){
 				loc_5_6Tot++;
 			}
 			else if (selected == board.getCells().get(board.calcIndex(6, 7))){
 				loc_6_7Tot++;
 			}
 			else if (selected == board.getCells().get(board.calcIndex(4, 8))){
 				loc_4_8Tot++;
 			}
 			else if (selected == board.getCells().get(board.calcIndex(7, 8))){
 				loc_7_8Tot++;
 			}
 			else if (selected == board.getCells().get(board.calcIndex(6, 9))){
 				loc_6_9Tot++;
 			}
 			else if (selected == board.getCells().get(board.calcIndex(5, 10))){
 				loc_5_10Tot++;
 			}
 			else{
 				fail("Invalid target selected");
 			}
 		}
 		// Ensure we have 100 total selections (fail should also ensure)
 		assertEquals(100, loc_5_6Tot + loc_6_7Tot + loc_4_8Tot + loc_7_8Tot + loc_6_9Tot + loc_5_10Tot);
 		// Ensure each target was selected more than once
 		assertTrue(loc_5_6Tot > 5);
 		assertTrue(loc_6_7Tot > 5);
 		assertTrue(loc_4_8Tot > 5);
 		assertTrue(loc_7_8Tot > 5);
 		assertTrue(loc_6_9Tot > 5);
 		assertTrue(loc_5_10Tot > 5);
 	}
 	
 	//Test disproving a suggestion
 	//Test disproving suggestion with one player and one card in possible match
 	@Test
 	public void testDisprovingSuggestionOnePlayerOneCard(){
 		//create a player
 		Player player = new Player();
 		Card returnCard = new Card();
 		//Deal the six cards to the player
 		player.getMyCards().add(mustardCard);
 		player.getMyCards().add(mrGreenCard);
 		player.getMyCards().add(leadPipeCard);
 		player.getMyCards().add(knifeCard);
 		player.getMyCards().add(hallCard);
 		player.getMyCards().add(kitchenCard);
 
 		//Test for one player, one correct match
 		//Test that it returns correct person
 		returnCard = player.disproveSuggestion("Colonel Mustard", "Study", "Revolver");
 		assertEquals("Colonel Mustard", returnCard.getName());
 		
 		//Test that it returns correct room
 		returnCard = player.disproveSuggestion("Mrs. White", "Hall", "Revolver");
 		assertEquals("Hall", returnCard.getName());
 		
 		//Test that it returns correct weapon
 		returnCard = player.disproveSuggestion("Mrs. White", "Study", "Knife");
 		assertEquals("Knife", returnCard.getName());
 		
 		//Test that it returns correct person
 		returnCard = player.disproveSuggestion("Mr. Green", "Study", "Revolver");
 		assertEquals("Mr. Green", returnCard.getName());
 		
 		//Test that it returns correct room
 		returnCard = player.disproveSuggestion("Mrs. White", "Kitchen", "Revolver");
 		assertEquals("Kitchen", returnCard.getName());
 		
 		//Test that it returns correct weapon
 		returnCard = player.disproveSuggestion("Mrs. White", "Study", "Lead Pipe");
 		assertEquals("Lead Pipe", returnCard.getName());
 		
 		//Test that it returns null
 		returnCard = player.disproveSuggestion("Mrs. White", "Study", "Revolver");
 		assertEquals(null, returnCard);
 	}
 	
 	//Test disproving suggestion with one player and multiple cards in possible match
 	@Test
 	public void testDisprovingSuggestionOnePlayerMultCard(){
 		Card returnCard = new Card();
 		Player player = new Player();
 		//Deal the six cards to the player
 		player.getMyCards().add(mustardCard);
 		player.getMyCards().add(mrGreenCard);
 		player.getMyCards().add(leadPipeCard);
 		player.getMyCards().add(knifeCard);
 		player.getMyCards().add(hallCard);
 		player.getMyCards().add(kitchenCard);
 		
 		//Test for one player, multiple possible matches
 		//Test with three possible matches
 		//Test one
 		int mustardCard = 0;
 		int hallCard = 0;
 		int leadPipeCard = 0;
 		// Run the test 100 times
 		for (int i=0; i<100; i++) {
 			returnCard = player.disproveSuggestion("Colonel Mustard", "Hall", "Lead Pipe");
 			if (returnCard.getName().equals("Colonel Mustard")){
 				mustardCard++;
 			}
 			else if (returnCard.getName().equals("Hall")){
 				hallCard++;
 			}
 			else if (returnCard.getName().equals("Lead Pipe")){
 				leadPipeCard++;
 			}
 			else
 				fail("Card not part of suggestion");
 		}
 		// Ensure we have 100 total selections (fail should also ensure)
 		assertEquals(100, mustardCard + hallCard + leadPipeCard);
 		// Ensure each possibility was returned more than once
 		assertTrue(mustardCard > 10);
 		assertTrue(hallCard > 10);
 		assertTrue(leadPipeCard > 10);
 
 		//Test 2
 		int mrGreenCard = 0;
 		int kitchenCard = 0;
 		int knifeCard = 0;
 		// Run the test 100 times
 		for (int i=0; i<100; i++) {
 			returnCard = player.disproveSuggestion("Mr. Green", "Kitchen", "Knife");
 			if (returnCard.getName().equals("Mr. Green")){
 				mrGreenCard++;
 			}
 			else if (returnCard.getName().equals("Kitchen")){
 				kitchenCard++;
 			}
 			else if (returnCard.getName().equals("Knife")){
 				knifeCard++;
 			}
 			else
 				fail("Card not part of suggestion");
 		}
 		// Ensure we have 100 total selections (fail should also ensure)
 		assertEquals(100, mrGreenCard + kitchenCard + knifeCard);
 		// Ensure each possibility was returned more than once
 		assertTrue(mrGreenCard > 10);
 		assertTrue(kitchenCard > 10);
 		assertTrue(knifeCard > 10);
 		
 		//Test with two possible choices
 		//Test with person and room as possible choices
 		mrGreenCard = 0;
 		kitchenCard = 0;
 		// Run the test 100 times
 		for (int i=0; i<100; i++) {
 			returnCard = player.disproveSuggestion("Mr. Green", "Kitchen", "Revolver");
 			if (returnCard.getName().equals("Mr. Green")){
 				mrGreenCard++;
 			}
 			else if (returnCard.getName().equals("Kitchen")){
 				kitchenCard++;
 			}
 			else
 				fail("Card not part of suggestion");
 		}
 		// Ensure we have 100 total selections (fail should also ensure)
 		assertEquals(100, mrGreenCard + kitchenCard);
 		// Ensure each possibility was selected more than once
 		assertTrue(mrGreenCard > 10);
 		assertTrue(kitchenCard > 10);
 		
 		//Test room and weapon as possible matches
 		hallCard = 0;
 		leadPipeCard = 0;
 		// Run the test 100 times
 		for (int i=0; i<100; i++) {
 			returnCard = player.disproveSuggestion("Mrs. White", "Hall", "Lead Pipe");
 			if (returnCard.getName().equals("Hall")){
 				hallCard++;
 			}
 			else if (returnCard.getName().equals("Lead Pipe")){
 				leadPipeCard++;
 			}
 			else
 				fail("Card not part of suggestion");
 		}
 		// Ensure we have 100 total selections (fail should also ensure)
 		assertEquals(100, hallCard + leadPipeCard);
 		// Ensure each possibility was returned more than once
 		assertTrue(hallCard > 10);
 		assertTrue(leadPipeCard > 10);
 }
 	
 	@Test
 	public void testDisprovingSuggestionMultiPlayer(){
 		Card returnCard = new Card();
 		//Create array of players and human player
 		ArrayList<ComputerPlayer> computers = new ArrayList<ComputerPlayer>();
 		HumanPlayer hplayer = new HumanPlayer();
 		//Deal card to human player
 		hplayer.getMyCards().add(kitchenCard);
 		//Deal cards to the computer players
 		ComputerPlayer player1 = new ComputerPlayer();
 		player1.getMyCards().add(mustardCard);
 		computers.add(player1);
 		ComputerPlayer player2 = new ComputerPlayer();
 		player2.getMyCards().add(leadPipeCard);
 		computers.add(player2);
 		ComputerPlayer player3 = new ComputerPlayer();
 		player3.getMyCards().add(mrGreenCard);
 		computers.add(player3);
 		ComputerPlayer player4 = new ComputerPlayer();
 		player4.getMyCards().add(knifeCard);
 		computers.add(player4);
 		ComputerPlayer player5 = new ComputerPlayer();
 		player5.getMyCards().add(hallCard);
 		computers.add(player5);
 		
 		board.setComputerPlayers(computers);
 		board.setHumanPlayer(hplayer);
 		
 		//Make suggestion that no players can disprove
 		returnCard = board.handleSuggestion("Mrs. White", "Rope", "Study");
 		//Assert that it returns null
 		assertEquals(null, returnCard);
 		
 		//Tests when a suggestion is made that only the human can disprove
 		returnCard = board.handleSuggestion("Mrs. White", "Rope", "Kitchen");
 		//Assert that it return correct card
 		assertEquals("Kitchen", returnCard.getName());
 		
 		//Test that the computer player whose turn it is, will not return a card
 		//Set turn to player1
 		board.setWhoseTurn(player1);
 		//Make suggestion that only player1 can disprove
 		returnCard = board.handleSuggestion("Colonel Mustard", "Rope", "Study");
 		//Assert that it returns null
 		assertEquals(null, returnCard);
 		
 		//Test if it is the human player's turn, it will not return a card
 		//Set turn to human player
 		board.setWhoseTurn(hplayer);
 		//Make suggestion that only hplayer can disprove
 		returnCard = board.handleSuggestion("Mrs. White", "Rope", "Kitchen");
 		//Assert that it returns null
 		assertEquals(null, returnCard);
 		//Reset whose turn it is
 		board.setWhoseTurn(null);
 		
 		//Test players are not queried in the same order each time
 		//Suggestion that two computers players can disprove
 		int mrGreenCard = 0;
 		int leadPipeCard = 0;
 		// Run the test 100 times
 		for (int i=0; i<100; i++) {
 			returnCard = board.handleSuggestion("Mr. Green", "Lead Pipe", "Study");
 			if (returnCard.getName().equals("Mr. Green")){
 				mrGreenCard++;
 			}
 			else if (returnCard.getName().equals("Lead Pipe")){
 				leadPipeCard++;
 			}
 			else
 				fail("Card not part of suggestion");
 		}
 		// Ensure we have 100 total selections (fail should also ensure)
 		assertEquals(100, mrGreenCard + leadPipeCard);
 		// Ensure each possibility was selected more than once
 		assertTrue(mrGreenCard > 10);
 		assertTrue(leadPipeCard > 10);
 
 		//Test players are not queried in the same order each time
 		//Suggestion that human player and one computer player can disprove
 		int kitchenCard = 0;
 		leadPipeCard = 0;
 		// Run the test 100 times
 		for (int i=0; i<100; i++) {
 			returnCard = board.handleSuggestion("Mrs. White", "Lead Pipe", "Kitchen");
 			if (returnCard.getName().equals("Kitchen")){
 				kitchenCard++;
 			}
 			else if (returnCard.getName().equals("Lead Pipe")){
 				leadPipeCard++;
 			}
 			else
 				fail("Card not part of suggestion");
 		}
 		// Ensure we have 100 total selections (fail should also ensure)
 		assertEquals(100, kitchenCard + leadPipeCard);
 		// Ensure each possibility was selected more than once
 		assertTrue(kitchenCard > 10);
 		assertTrue(leadPipeCard > 10);
 	}
 	
 	@Test
 	public void testMakingSuggestion(){
 		ComputerPlayer player = new ComputerPlayer();
 		Suggestion testSuggestion = new Suggestion();
 		
 		//Update seen cards
		player.getSeenCards().add(mrGreenCard);
		player.getSeenCards().add(leadPipeCard);
 		//Update location
 		player.setLocationX(17);
 		player.setLocationY(5);
 		player.setLocation(board.calcIndex(17, 5));
 		//Make suggestion
 		testSuggestion = player.createSuggestion(board.getCards(), board.getCells());
 		//Test suggested room is correct
 		assertEquals("Kitchen", testSuggestion.getRoom());
 		//Test suggested person is not included in seen cards
 		Assert.assertFalse(testSuggestion.getPerson().equals("Mr. Green"));
 		//Test suggested weapon is not included in seen cards
 		Assert.assertFalse(testSuggestion.getWeapon().equals("Lead Pipe"));
 		
 		//Update seen cards
		player.getSeenCards().add(mustardCard);
		player.getSeenCards().add(knifeCard);
 		//Update location
 		player.setLocationX(5);
 		player.setLocationY(15);
 		player.setLocation(board.calcIndex(5, 15));
 		//Make suggestion
 		testSuggestion = player.createSuggestion(board.getCards(), board.getCells());
 		//Test suggested room is correct
 		assertEquals("Library", testSuggestion.getRoom());
 		//Test suggested person is not included in seen cards
 		Assert.assertFalse(testSuggestion.getPerson().equals("Colonel Mustard"));
 		//Test suggested weapon is not included in seen cards
 		Assert.assertFalse(testSuggestion.getWeapon().equals("Knife"));
 
 	}
 
 }
