 package mtgscraper.http;
 
 import static mtgscraper.http.HttpTestUtils.*;
 
 import mtgscraper.entities.Card;
 
 import org.jsoup.nodes.Document;
 import org.junit.Assert;
 import org.junit.Test;
 
 public class CardProcessorTest {
 
 	@Test
 	public void testProcess() throws Exception {
 		CardLink cardLink = new CardLink("http://magiccards.info/rtr/en/1.html", "Creature — Angel", "5/6", null, "4WWW", "Mythic Rare", "Aleksi Briclot");
 		
 		Document document = loadDocumentFromResource("card.html");
 		Card card = (new CardProcessor(cardLink)).process(document);
 		Assert.assertNotNull("Process should never return null", card);
 		Assert.assertEquals("Expected the card name to be found from the page", "Angel of Serenity", card.getName());
 		Assert.assertEquals("Expected the card index to be found from the page", "1", card.getIndex());
 		Assert.assertEquals("Expected the url to be found from the page", "http://magiccards.info/rtr/en/1.html", card.getUrl());
 		Assert.assertEquals("Expected the imageUrl to be found from the page", "http://magiccards.info/scans/en/rtr/1.jpg", card.getImageUrl());
 		Assert.assertEquals("Expected the typeLine to be found from the page", "Creature — Angel", card.getTypeLine());
 		Assert.assertEquals("Expected the casting cost to be found from the page", "4WWW", card.getCastingCost());
 		Assert.assertEquals("Expected the power/toughness to be found from the page",  "5/6", card.getPowerToughness());
		Assert.assertEquals("Expected the rarity to be specified", "Mythic Rare", card.getRarity());
		Assert.assertEquals("Expected the artist to be specified", "Aleksi Briclot", card.getArtist());
		
		String expectedBody = "Flying\n\nWhen Angel of Serenity enters the battlefield, you may exile up to three other target creatures from the battlefield and/or creature cards from graveyards.\n\nWhen Angel of Serenity leaves the battlefield, return the exiled cards to their owners' hands.";
		Assert.assertEquals("Expected the body text to be specified", expectedBody, card.getBodyText());
 	}
 	
	@Test
	public void testProcessWithHtmlBody() throws Exception {
		CardLink cardLink = new CardLink("http://magiccards.info/rtr/en/1.html", "Creature — Angel", "5/6", null, "4WWW", "Mythic Rare", "Aleksi Briclot");
		
		Document document = loadDocumentFromResource("cardWithHtmlBody.html");
		Card card = (new CardProcessor(cardLink)).process(document);
		Assert.assertNotNull("Process should never return null", card);
		
		String expectedBody = "This body contains \"escaped\" characters. Everything should be unescaped & unencoded when read.\n\nLorem ipsum";
		Assert.assertEquals("Expected the body text to be specified", expectedBody, card.getBodyText());
	}
 }
