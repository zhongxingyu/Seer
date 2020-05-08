 package poker;
 
 public enum Suit {
 	DIAMONDS("\4"), // "\4" == ♦,
 	CLUBS("\5"), // "\5" == ♣,
 	HEARTS("\3"), // "\3" == ♥,
 	SPADES("\6"); // "\6" == ♠, ♠
 	// The string outputs the correct symbol in the command line
 	
 	private String suitCharacter;
 	
 	private Suit(String suitCharacter){
 		this.suitCharacter = suitCharacter;
 	}
 	
 	public String getSuitCharacter(){
 		return suitCharacter;
 	}
 	
 }
