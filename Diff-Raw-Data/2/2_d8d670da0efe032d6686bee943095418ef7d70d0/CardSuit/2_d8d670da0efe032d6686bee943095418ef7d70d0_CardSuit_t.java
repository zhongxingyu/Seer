 package cards.api;
 
 public enum CardSuit {
 	
	CLUB("♣"),
 	DIAMOND("♥"),
 	HEART("♦"),
 	SPADE("♠");
 	
 	private String representation;
 	
 	CardSuit(String representation) {
 		this.representation = representation;
 	}
 	
 	public String getRepresentation() {
 		return this.representation;
 	}
 	
 }
