 package co.whitejack.api;
 
 import java.net.URL;
 
 import javax.swing.ImageIcon;
 
 /**
  * Card is a superclass of Deck. Card contains the methods of retrieving
  * attributes per instantialized card.
  * 
  * @author kevin
  * 
  */
 public class Card {
 
 	private int cardID;
 	private int value;
 	protected String[] suits = { "Spades", "Hearts", "Diamonds", "Clubs" };
 	protected String[] ranks = { "Ace", "2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack",
 			"Queen", "King" };
 	private String suit;
 	private String rank;
 	private String path;
 	private URL url;
 	private ImageIcon icon;
 
 	public Card() {
 	}
 	
 	@Override
 	public String toString() {
 		return getRank()+" of "+getSuit();
 	}
 
 	public Card(int i) {
 		this.suit = suits[i / 13];
 		this.rank = ranks[i % 13];
 
 	}
 
 	public void setPath(String string) {
 		this.path = string;
 	}
 
 	public void setURL() {
		path = "/com/whitejack/images/Cards/";
 		path += rank + suit;
 		url = Card.class.getResource(path);
 		setIcon(new ImageIcon(url));
 	}
 
 	public URL getURL() {
 		return url;
 	}
 
 	public String getSuit() {
 		String suit = suits[cardID / 13];
 		return suit;
 	}
 
 	public void setSuit() {
 	}
 
 	public String getRank() {
 		return ranks[cardID % 13];
 	}
 
 	public void setCardID(int num) {
 		cardID = num;
 	}
 
 	public int getCardID() {
 		return cardID;
 	}
 
 	public int getNumber() {
 		return cardID;
 	}
 
 	public void setSuit(String suit) {
 		this.suit = suit;
 	}
 
 	public void displayImage(int cardID) {
 		// pull the card image from url and show on a gui
 	}
 
 	public void hideImage() {
 
 	}
 
 	public int getValue() {
 		return value;
 	}
 
 	public void setValue(int value) {
 		this.value = value;
 	}
 
 	public ImageIcon getIcon() {
 		return icon;
 	}
 
 	public void setIcon(ImageIcon icon) {
 		this.icon = icon;
 	}
 }
