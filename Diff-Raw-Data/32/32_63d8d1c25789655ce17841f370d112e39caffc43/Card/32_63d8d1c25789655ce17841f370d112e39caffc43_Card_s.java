 package com.me.Roguish.Model;
 
 public abstract class Card {
 	private String name;		// Unique flavor based card name
	private int cardNo;  		// Card No in the series
 	private int rarity;			// Rarity 
	// private int setNo;		// Set the card is from
 
	public int getCardNo(){
		return this.cardNo;
 	}
 
 	public int getRarity(){
		return this.cardNo;
 	}
 	
	public String getName(){
		return this.name;
	}
 
 }
