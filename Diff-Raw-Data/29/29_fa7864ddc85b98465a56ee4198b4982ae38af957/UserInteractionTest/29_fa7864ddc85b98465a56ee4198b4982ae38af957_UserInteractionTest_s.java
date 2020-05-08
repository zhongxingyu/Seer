 
 
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.XPathExpressionException;
 
 import org.junit.Test;
 import org.xml.sax.SAXException;
 
 
 public class UserInteractionTest {
 
 	@Test
 	public void testForSearchByName() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
 		UserInteraction testUI = new UserInteraction();
 		ArrayList<MTGCard> results = testUI.search(testUI.getParser(), "name", "Rancor");
 		assertEquals("Rancor", results.get(0).name);
 	}
 
 	@Test
 	public void testForDisplayOutput() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException{
 		UserInteraction testUI = new UserInteraction();
 		ArrayList<MTGCard> results = testUI.search(testUI.getParser(), "name", "Voidwalk");
 		testUI.setSearchResults(results);
		assertEquals("Result number 1\nName Voidwalk\nCastingCost 3U\nType Sorcery\nPower n/a\nToughness n/a\nRules Exile target creature. Return it to the battlefield under its owner's control at the beginning of the next end step.\nCipher (Then you may exile this spell card encoded on a creature you control. Whenever that creature deals combat damage to a player, its controller may cast a copy of the encoded card without paying its mana cost.)\nSets Gatecrash Uncommon\n\n", testUI.displayCommand());
 	}
 
 	@Test
 	public void testAddCardCommand() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException{
 		UserInteraction testUI = new UserInteraction();
 		Deck testDeck = new Deck();
 		ArrayList<MTGCard> results = testUI.search(testUI.getParser(), "name", "Rancor");
 		assertEquals(1, testUI.addCards(1, "Rancor"));
 	}
 	@Test
 	public void testSaveCardCommandandCreateFile() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException{
 		UserInteraction testUI = new UserInteraction();
 		Deck testDeck = new Deck();
 		Scanner input = new Scanner(System.in);
 		ArrayList<MTGCard> results = testUI.search(testUI.getParser(), "name", "Rancor");
 		testDeck.addCardToDeck(results.get(0));
 		//testUI.createNewFile(input);
 		testUI.saveDeck(testDeck, "FooFile");
 	}
 		
 	
 }
 
