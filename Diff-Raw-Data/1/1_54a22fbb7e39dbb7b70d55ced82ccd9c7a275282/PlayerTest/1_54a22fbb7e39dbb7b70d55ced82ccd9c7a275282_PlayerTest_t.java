 package test;
 
 import java.util.ArrayList;
 
 import se.chalmers.dat255.risk.model.Card;
 import se.chalmers.dat255.risk.model.Card.CardType;
 import se.chalmers.dat255.risk.model.Deck;
 import se.chalmers.dat255.risk.model.Player;
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 
 public class PlayerTest {
 
 	@Test
 	public void testAddCard(){
 		ArrayList<String> provinces = new ArrayList<String>();
 		provinces.add("Hej");
 		provinces.add("Hejhopp");
 		provinces.add("Hejmamma");
 		Deck deck = Deck.getInstance(provinces, 3);
 		Player player = new Player(1, "Testare");
 		player.addCard();
 		assertTrue(player.getCards().size() == 1);
 	}
 	
 	@Test
 	public void testProvinces(){
 		Player player = new Player(1, "Testare");
 		player.gainProvince();
 		player.gainProvince();
 		player.gainProvince();
 		player.loseProvince();
 		player.loseProvince();
 		assertTrue(player.getNrOfProvinces() == 1);
 	}
 	
 	@Test
 	public void testExhangeCard(){
 		ArrayList<String> provinces = new ArrayList<String>();
 		provinces.add("Hej");
 		provinces.add("Hejhopp");
 		provinces.add("Hejmamma");
		Deck deck = Deck.getInstance(provinces, 2);
 		Player player = new Player(1, "Testare");
 		player.addCard();
 		player.addCard();
 		player.addCard();
 		Card card1 = player.getCards().get(2);
 		Card card2 = player.getCards().get(1);
 		Card card3 = player.getCards().get(0);
 		assertTrue(player.exchangeCard(card1, card2, card3));
 	}
 
 }
