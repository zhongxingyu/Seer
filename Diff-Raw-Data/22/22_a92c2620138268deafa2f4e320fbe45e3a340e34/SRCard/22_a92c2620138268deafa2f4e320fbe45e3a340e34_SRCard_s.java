 import java.util.Arrays;
 
 /**
 * This class will create the cards to be used in the game.  Each card will inherit 
 * rules related to that particular card from the SRRule class, it will also display 
 * the text from each card to the user.
 * 
 * @author Sam Brown, Taylor Krammen, and Yucan Zhang
 */
 
 public class SRCard {
 	//override cardNum for testing
 	public boolean override = false;
	public int overrideDist =-4;
 	
 	//The following methods will set both the cardNum and the pictureName.
 	public int cardNum;
 	public int numMoves;
 	private String pictureName;
 	boolean canSplitMoves;
 	boolean canStartPawn;
 	boolean isSorry;
 	public SRRule [] rules;
 	
 	public SRCard(int cardNum) {
 		this.cardNum = cardNum;
 		if (override){
 			this.cardNum = overrideDist;
 			cardNum = overrideDist;
 		}
 		
 		//sort out the images
 		if(cardNum == 0)
 			pictureName = "cardSorry.jpg";
 		else
 			pictureName = "card" + cardNum + ".jpg";
 		
 		//now set the rules
 		//SRRule[] rules;
 		if (cardNum == 1) {
 			rules = new SRRule [2];
 			rules[0] = new SRRule("START");
 			rules[1] = new SRRule(1);
 		}
 		else if (cardNum == 2) {
 			rules = new SRRule [3];
 			rules[0] = new SRRule(2);
 			rules[1] = new SRRule("START");
 			rules[2] = new SRRule("DRAW");
 		}
 		else if (cardNum == 3) {
 			rules = new SRRule [1];
 			rules[0] = new SRRule(3);
 		}
 		else if (cardNum == 4) {
 			rules = new SRRule [1];
 			rules[0] = new SRRule(-4);
 		}
 		else if (cardNum == 5) {
 			rules = new SRRule [1];
 			rules[0] = new SRRule(5);
 		}
 		else if (cardNum == 7) {
 			rules = new SRRule [1];
 			rules[0] = new SRRule(7);
 		}
 		else if (cardNum == 8) {
 			rules = new SRRule [1];
 			rules[0] = new SRRule(8);
 		}
 		else if (cardNum == 10) {
 			rules = new SRRule [1];
 			rules[0] = new SRRule(10);
 		}
 		else if (cardNum == 11) {
 			rules = new SRRule [2];
 			rules[0] = new SRRule("SWAP");
 			rules[1] = new SRRule(11);
 		}
 		else if (cardNum == 12) {
 			rules = new SRRule [1];
 			rules[0] = new SRRule(12);
 		}
 		else if (cardNum == 0) {
 			rules = new SRRule [1];
 			rules[0] = new SRRule("SORRY");
 		}
 		else {
 		//	throw new Error("Uh oh... card number "+cardNum+ " does not exist!");
 			rules = new SRRule [1];
 			rules[0] = new SRRule(cardNum);
 		}
 		
 	}
 	
 	public void setcardNum(int cardNum) {
 		this.cardNum = cardNum;
 	}
 	
 	public int getcardNum() {
 		return cardNum;
 	}
 	
 	public boolean canSplitMoves(){
 		return this.canSplitMoves;
 	}
 	
 	public boolean canStartPawn(){
 		return this.canStartPawn;
 	}
 	
 	public boolean isSorry(){
 		return this.isSorry;
 	}
 	
 	public void setPictureName(String pictureName) {
 		this.pictureName = pictureName;
 	}
 	
 	public String getPictureName() {
 		return pictureName;
 	}
 	
 	//Will return a string representation of each card
 	//Will help when later reffering to pictures for each card in the GUI
 	public String toString() {
 		if(cardNum == 0)
 			return "Sorry!";
 		else 
 			return "" + cardNum;
 	}
 	
 	/**
 	 * This function will return the text build into each card
 	 * 
 	 * @param
 	 * @return string Text
 	 */
 	public String getText() {
 		if (cardNum == 1)
 			return "You drew a 1, you may now move a pawn from Start or move a pawn 1 space forward.";
 		else if (cardNum == 2)
 			return "You drew a 2, you may now move a pawn from Start or move a pawn 2 spaces forward.  Then please draw another card.";
 		else if (cardNum == 4)
 			return "You drew a 4, you must now move a pawn 4 spaces backwards.";
 		else if (cardNum == 7)
 			return "You drew a 7, you may now move a pawn 7 spaces forward, or split the 7 spaces between two pawns";
 		else if (cardNum ==10)
 			return "You drew a 10, you may now move a pawn 10 spaces forward or 1 space backward.";
 		else if (cardNum == 11)
 			return "You drew an 11, you may now move a pawn 11 spaces forward, or swap places with one opposing pawn.";
 		else if (cardNum == 0)
 			return "You drew a Sorry! card, you may now replace any opponents pawn with your own, thus sending the opponents pawn back to Start.";
 		else 
 			return "You drew a " + cardNum + ", you may now move a pawn " + cardNum + " spaces forward.";
 	}	
 	
 	/**
 	 * Take the rules associated with the card drawn and will display
 	 * the game-play choices to the user.
 	 * 
 	 * @param
 	 * @return Rule rule
 	 */
 	 public String[] getRules() {
 		SRRule rules = new SRRule();
 		String[] cardRule = rules.types(cardNum);
 		return cardRule;
 	}
 	/*
 	//Test function
 	public static void main(String[] args){
 		SRRule rule = new SRRule();
 		//Simple loop to create each of the 13 different cards
 		for(int i=0;i<13;i++){
 			SRCard card = new SRCard(i);
 			System.out.println(card); //Tests the toString function
 			System.out.println(card.getText()); //Tests the getText function
 			System.out.println(Arrays.toString(card.getRules())); //Tests the getRules function
 		}
 	}
 	*/
 	 
 }
