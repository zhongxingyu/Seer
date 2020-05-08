 package de.htwg.se.dog.models;
 
 import java.util.Stack;
 
 public class CardStack {
 
 	private Stack<Card> cardstack;
 	
 	public CardStack(){
 		cardstack = new Stack<>();
 	}
 	
 	public static Card[] generateCardArray(){
 		Card[] cardArray = new Card[55];
 		for(int i = 0; i <= 3 ; i++){
 			for(int j = 0; j <= 12; j++){
 				cardArray[(i*13) + j] = new Card(j+1);
 			}
 		}
 		cardArray[52] = new Card(14);
 		cardArray[53] = new Card(14);
 		cardArray[54] = new Card(14);
 		
 		return cardArray;
 	}
 	
 }
