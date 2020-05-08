 import static org.junit.Assert.*;
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 
 public class DeckTest {
 
 	@Test
 	public void testDeckNotNull() {
 		Deck test = new Deck();
 		Assert.assertNotNull(test);
 	}
 	
 	@Test
 	public void testDefualtDeckIsBasic(){
 		Deck test = new Deck();
 		RuleSet comp = new BasicRuleSet();
 		Assert.assertEquals(comp.getClass(), test.rules.getClass());
 	}
 	
 	@Test
 	public void testDeckCreationCanMakeBasic(){
 		Deck test = new Deck("Basic");
 		RuleSet comp = new BasicRuleSet();
 		Assert.assertEquals(comp.getClass(), test.rules.getClass());
 	}
 	
 	@Test
 	public void testDeckCanAddCard(){
 		Deck test = new Deck();
 		MTGCard testCard = new MTGCard("test", "4", "4", "4", "4", "Testing", "4");
 		Assert.assertTrue(test.cards.isEmpty());
 		test.addCardToDeck(testCard);
 		assertEquals(testCard, test.cards.get(0));
 	}
 	
 	@Test
 	public void testDeckCannotAddCardsOver60(){
 		Deck test = new Deck();
 		MTGCard testCard = new MTGCard("test", "4", "4", "4", "4", "Testing", "4");
 		MTGCard testCard1 = new MTGCard("test1", "4", "4", "4", "4", "Testing", "4");
 		MTGCard testCard2 = new MTGCard("test2", "4", "4", "4", "4", "Testing", "4");
 		MTGCard testCard3 = new MTGCard("test3", "4", "4", "4", "4", "Testing", "4");
 		MTGCard testCard4 = new MTGCard("test4", "4", "4", "4", "4", "Testing", "4");
 		MTGCard testCard5 = new MTGCard("test5", "4", "4", "4", "4", "Testing", "4");
 		MTGCard testCard6 = new MTGCard("test6", "4", "4", "4", "4", "Testing", "4");
 		MTGCard testCard7 = new MTGCard("test7", "4", "4", "4", "4", "Testing", "4");
 		MTGCard testCard8 = new MTGCard("test8", "4", "4", "4", "4", "Testing", "4");
 		MTGCard testCard9 = new MTGCard("test9", "4", "4", "4", "4", "Testing", "4");
 		MTGCard testCard10 = new MTGCard("test10", "4", "4", "4", "4", "Testing", "4");
 		MTGCard testCard11 = new MTGCard("test11", "4", "4", "4", "4", "Testing", "4");
 		MTGCard testCard12 = new MTGCard("test12", "4", "4", "4", "4", "Testing", "4");
 		MTGCard testCard13 = new MTGCard("test13", "4", "4", "4", "4", "Testing", "4");
 		MTGCard testCard14 = new MTGCard("test14", "4", "4", "4", "4", "Testing", "4");
 		for(int i = 0; i < 4; i++){
 			test.addCardToDeck(testCard);
 			test.addCardToDeck(testCard1);
 			test.addCardToDeck(testCard2);
 			test.addCardToDeck(testCard3);
 			test.addCardToDeck(testCard4);
 			test.addCardToDeck(testCard5);
 			test.addCardToDeck(testCard6);
 			test.addCardToDeck(testCard7);
 			test.addCardToDeck(testCard8);
 			test.addCardToDeck(testCard9);
 			test.addCardToDeck(testCard10);
 			test.addCardToDeck(testCard11);
 			test.addCardToDeck(testCard12);
 			test.addCardToDeck(testCard13);
 			test.addCardToDeck(testCard14);
 		}
 		MTGCard testCard15 = new MTGCard("test15", "4", "4", "4", "4", "Testing", "4");
 		Assert.assertEquals(60, test.cards.size());
 		test.addCardToDeck(testCard15);
 		Assert.assertEquals(60, test.cards.size());
 		
 	}
 	
 	@Test
 	public void DeckCannotHaveMoreThen4OfTheSameNonLandCards(){
 		Deck test = new Deck();
 		MTGCard testCard14 = new MTGCard("test14", "4", "4", "4", "4", "Testing", "4");
 		for(int i = 0; i < 4; i++){
 			test.addCardToDeck(testCard14);
 		}
 		Assert.assertEquals(4, test.cards.size());
 		test.addCardToDeck(testCard14);
 		Assert.assertEquals(4, test.cards.size());
 	}
 	
 	@Test 
 	public void DeckCanHaveMoreThen4NonLandCards(){
 		Deck test = new Deck();
		MTGCard testCard14 = new MTGCard("test14", "4", "Basic Land", "4", "4", "Land", "4");
 		for(int i = 0; i < 5; i++){
 			test.addCardToDeck(testCard14);
 		}
 		Assert.assertEquals(5, test.cards.size());
 	}
 	
 	@Test
 	public void DeckDisplay(){
 		Deck test = new Deck();
 		MTGCard testCard = new MTGCard("test", "test", "test", "3", "3", "test", "test");
 		test.addCardToDeck(testCard);
 		System.out.println(test.cards.toString());
 		String result = testCard.toString();
 		Assert.assertEquals(result, test.displayDeck());
 	}
 	
 	@Test
 	public void DeckRemove(){
 		Deck test = new Deck();
 		MTGCard testCard = new MTGCard("test", "test", "test", "3", "3", "test", "test");
 		test.addCardToDeck(testCard);
 		test.removeCardFromDeck(testCard);
 		Assert.assertTrue(test.cards.isEmpty());
 	}
 	
 	@Test
 	public void DeckRemoveOnlyRemoves1(){
 		Deck test = new Deck();
 		MTGCard testCard = new MTGCard("test", "test", "test", "3", "3", "test", "test");
 		test.addCardToDeck(testCard);
 		test.addCardToDeck(testCard);
 		test.removeCardFromDeck(testCard);
 		Assert.assertFalse(test.cards.isEmpty());
 	}
 	
 	
 
 }
