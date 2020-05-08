 package se.mah.k3.pokergame.model;
 
 public class Card implements Comparable<Card>{
 	private int cardFace;
 	private int cardBack;
 	private Suits suit; //if spade etc 
 	private SpecialNames specialName; //if spade etc 
 	private int rank; //1 to 13, ace 1 (14), jack 11, queen 12, king 13,
 	private boolean faceUp=true;
 	
 	//Enums som tillhr kort s de kan vara i denna klassen
 	public static enum Suits {SPADE,HEART,DIMOND,CLUB} ;
 	public static enum SpecialNames {ACE,JACK,QUEEN,KING,JOKER,NONE};
 	
 	public Card(int rank, Suits suit, int cardFace,int cardBack, SpecialNames specialName ){
 		//Instantiate local variables
 		this.rank = rank;
 		this.suit = suit;
 		this.cardFace = cardFace;
 		this.cardBack=cardBack;
 		this.specialName = specialName;
 	}
 	
 	public int getCardFace() {
 		return cardFace;
 	}
 	
 	public int getCardBack() {
 		return cardBack;
 	}
 	
 	public int getCardImage(){
 		if (faceUp){
 			return cardFace;
 		}else{
 			return cardBack;
 		}
 	}
	public SpecialNames getSpecialName() {
 		return specialName;
 	}
 	
 	public int getRank() {
 		return rank;
 	}
 	
 	public void turnCard(){
 		faceUp =! faceUp;  //ndrar vrdet till det andra......
 	}
 	
 	public void turnfaceDown(){
 		faceUp = false;
 	}
 	
 	public boolean isFaceUp(){
 		return faceUp;
 	}
 	
 	@Override
 	public int compareTo(Card another) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 	@Override
 	public String toString() {
 		// TODO Auto-generated method stub
 		return "Debug info";
 	}
 	
 }
