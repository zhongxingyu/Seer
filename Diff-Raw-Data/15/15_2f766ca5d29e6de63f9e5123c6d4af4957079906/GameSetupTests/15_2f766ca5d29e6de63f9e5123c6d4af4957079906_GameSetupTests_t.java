 package boardTests;
 
 import static org.junit.Assert.*;
 
import java.awt.Color;
 import java.util.ArrayList;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import clueGame.Board;
 import clueGame.Card;
 import clueGame.ClueGame;
 import clueGame.Player;
 
 public class GameSetupTests {
 	private static ClueGame game;
 	
 	@BeforeClass
 	public static void setUp() {
 		game = new ClueGame("PlayerData.txt", "cardConfig.txt", "BoardLayout.csv", "legend.txt");
 		//game.deal();
 	}	
 	
 	@Test
 	public void LoadingPlayersTest(){
 		game.loadConfigFiles();
 		assertEquals(game.getPlayers().size(),6);
 		assertEquals(game.getPlayers().get(0).getName(),"Miss Scarlet");
 		assertEquals(game.getPlayers().get(1).getName(),"Colonel Mustard");
 		assertEquals(game.getPlayers().get(4).getName(),"Professor Plum");
		assertEquals(game.getPlayers().get(0).getColor(),Color.red);
		assertEquals(game.getPlayers().get(1).getColor(),Color.yellow);
		assertEquals(game.getPlayers().get(4).getColor(),game.convertColor("purple"));
 		assertEquals(game.getPlayers().get(0).getStartingLocation() , 7);
 		assertEquals(game.getPlayers().get(1).getStartingLocation() , 296);
 		assertEquals(game.getPlayers().get(4).getStartingLocation() , 18);
 	}
 	
 	@Test
 	public void LoadingCardsTest(){
 		game.loadConfigFiles();
 		ArrayList<Card> tempCards = game.getDeck();
 		
 		// Test we have all the cards
 		assertEquals(tempCards.size(),21);
 		
 		// Test we we have enouhg of each type
 		int w = 0;
 		int s = 0;
 		int r = 0;
 		for (Card card : tempCards) {
 			if (card.getType().equals(Card.CardType.WEAPON))
 				w++;
 			else if (card.getType().equals(Card.CardType.SUSPECT))
 				s++;
 			else if (card.getType().equals(Card.CardType.ROOM))
 				r++;
 		}
 		assertEquals(w,6);
 		assertEquals(s,6);
 		assertEquals(r,9);
 		
 		// Test the name feature is working
 		assertEquals(tempCards.get(2).getName(),"Warrior's SwordStaff");
 		assertEquals(tempCards.get(10).getName(),"Professor Plum");
 		assertEquals(tempCards.get(17).getName(),"Study");
 	}
 	
 	@Test
 	public void DealingCardsTest(){
 		game.loadConfigFiles();
 		game.deal();
 		
 		// Test all cards were dealt
 		assertEquals(game.getDeck().size(),0);
 		
 		// Test all players have about same number of cards
 		for (Player p : game.getPlayers())
 			for (Player q : game.getPlayers())
 				if (Math.abs(p.getCards().size()-q.getCards().size()) > 1)
 					fail("Difference greater than 1");
 		
 		// Test all players have unique cards
 		for(Player p : game.getPlayers())
 			for(Card c: p.getCards())
 				for(Player q : game.getPlayers())
 					if(p != q)
 						for(Card d : q.getCards())
 							if(d.equals(c))
 								fail("Two players have same card.");
 	}
 }
