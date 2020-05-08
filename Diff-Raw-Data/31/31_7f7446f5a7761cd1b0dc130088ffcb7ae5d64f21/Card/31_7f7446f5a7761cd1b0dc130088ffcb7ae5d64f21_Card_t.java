 package de.htwg.se.dog.models;
 
 
 public class Card {
 	
 	private final int value;
	private static final int ZERO = 0;
	private static final int FOURTEEN = 14;
 	
 	protected Card(int value){
 		if (value > ZERO && value <= FOURTEEN) {
 			this.value = value;
 		} else {
 			throw new IllegalArgumentException("Failed to create card with value:" + value);
 		}
 	}
 	public int getValue() {
 		return this.value;
 	}
 	
 	public String getCardName(int value) {
 		switch (value) {
 		case 1:
 			return "Ass";
 		case 11:
 			return "Bube"; 
 		case 12:
 			return "Dame";
 		case 13: 
 			return "Knig";
 		case 14:
 			return "JOKER";
 		default:
 			return ""+value; 
 		}
 		
 	}
 }
 
