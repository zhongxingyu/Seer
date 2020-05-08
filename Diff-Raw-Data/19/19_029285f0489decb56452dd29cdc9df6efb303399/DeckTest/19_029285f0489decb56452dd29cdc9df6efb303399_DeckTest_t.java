 package card;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 
 public class DeckTest {
 
 	@Test
 	public void init52Cards() throws Exception {
 		Deck deck = Deck.init52Cards();
 
 		Assert.assertEquals(52, deck.size());
 		Assert.assertEquals(Card.of(Value._2, Color.Heart), deck.getCards().get(0));
 		Assert.assertEquals(Card.of(Value._3, Color.Heart), deck.getCards().get(1));
 	}
 
 	@Test
 	public void init32Cards() throws Exception {
 		Deck deck = Deck.init32Cards();
 
 		Assert.assertEquals(32, deck.size());
 		Assert.assertFalse(deck.getCards().contains(Card.of(Value._6, Color.Heart)));
 		Assert.assertTrue(deck.getCards().contains(Card.of(Value._7, Color.Heart)));
 	}
 
 	@Test
 	public void shuffle() throws Exception {
 		Deck deck =
 				new Deck(Arrays.asList(Card.of(Value._7, Color.Heart), Card.of(Value._8, Color.Heart),
 									   Card.of(Value._9, Color.Heart)));
 
 		Assert.assertEquals(3, deck.size());
 
 		deck.shuffle();
 
 		Assert.assertEquals(3, deck.size());
 	}
 
 	@Test
 	public void distribute() throws Exception {
 		Deck deck =
 				new Deck(new ArrayList<Card>(Arrays.asList(Card.of(Value._7, Color.Heart),
 														   Card.of(Value._8, Color.Heart),
 														   Card.of(Value._9, Color.Heart))));
 
 		Assert.assertEquals(3, deck.size());
 		Assert.assertEquals(Card.of(Value._7, Color.Heart), deck.distribute());
 		Assert.assertEquals(2, deck.size());
 		Assert.assertEquals(Card.of(Value._8, Color.Heart), deck.distribute());
 		Assert.assertEquals(1, deck.size());
 	}
 
 	@Test(expected = NoMoreCardException.class)
 	public void distributeWithNoMoreCardException() throws Exception {
 		Deck deck = new Deck(new ArrayList<Card>(Arrays.asList(Card.of(Value._7, Color.Heart))));
 
 		Assert.assertEquals(1, deck.size());
 		deck.distribute();
 		Assert.assertEquals(0, deck.size());
 
 		deck.distribute();
 	}
 }
