 package clueGame;
 
 public class Card {
 	
 	public enum CardType {
 		SUSPECT,
 		WEAPON,
 		ROOM;
 	}
 	
 	private String name;
 	private CardType cardType;
 	
 	public Card(String name, CardType ct) {
 		this.name = name;
 		this.cardType = ct;
 	}
 	
 	
 	@Override
 	public boolean equals(Object toCompare) {
		/*boolean equal = false;
 		if(toCompare instanceof Card ) {
 			Card tocompare = (Card) toCompare;
			equal = this.cardType == tocompare.cardType && this.name.equalsIgnoreCase(tocompare.name)
 		}
 		return equal;
		*/
		return false;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public CardType getCardType() {
 		return cardType;
 	}
 
 	public void setCardType(CardType cardType) {
 		this.cardType = cardType;
 	}
 	
 	
 
 }
