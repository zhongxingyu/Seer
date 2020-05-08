 package mtgscraper.http;
 
 import java.io.IOException;
 
 import mtgscraper.entities.Card;
 
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 public class CardProcessor implements Http.Processor<Document, Card> {
 	private CardLink cardLink;
 	
 	public CardProcessor(CardLink cardLink) {
 		this.cardLink = cardLink;
 	}
 	
 	@Override
 	public Card process(Document document) throws IOException {
 		
 		Element mainTable = document.getElementsByTag("table").get(3);
 		Element mainTableRow = mainTable.getElementsByTag("tr").first();
 		
 		String printingsText = null;
 		Elements thirdColumnBolds = mainTableRow.child(2).getElementsByTag("b");
 		for(int index = 0; index < thirdColumnBolds.size(); index++) {
 			if(thirdColumnBolds.get(index).ownText().startsWith("Printings:")) {
 				printingsText = thirdColumnBolds.get(index + 1).ownText();
 			}
 		}
 		String cardIndex = printingsText.substring(1, printingsText.indexOf(' '));
 		
 		Element nameLink = mainTable.getElementsByTag("span").first().getElementsByTag("a").first();
 		String name = nameLink.ownText();
 		String url = nameLink.absUrl("href");
 		
 		Element scanImg = mainTableRow.getElementsByTag("img").first();
 		String imgUrl = scanImg.absUrl("src");
 		
 		//Many attributes of a card are found on one line within the magiccards.info line, which
 		//makes parsing really difficult. The sitemap however uses a nice table to display these 
 		//attributes so we will use the values from there instead of the values on this page.
 		String castingCost = cardLink.getCastingCost();
 		String typeLine = cardLink.getTypeLine();
 		String powerToughness = cardLink.getPowerToughness();
 		String loyalty = cardLink.getLoyalty();
 		String rarity = cardLink.getRarity();
 		String artist = cardLink.getArtist();
 		
		String bodyText = mainTableRow.getElementsByClass("ctext").first().getElementsByTag("b").html().replaceAll("<br />", "\n");
 		
 		return new Card(cardIndex, name, url, imgUrl, typeLine, castingCost, powerToughness, loyalty, rarity, artist, bodyText);
 	}
 }
