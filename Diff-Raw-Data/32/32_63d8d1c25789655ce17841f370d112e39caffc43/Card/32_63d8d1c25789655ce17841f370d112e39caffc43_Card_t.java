 package com.me.Roguish.Model;
 
import com.badlogic.gdx.utils.Json;

 public abstract class Card {
	//Json 
	
 	private String name;		// Unique flavor based card name
 	private int rarity;			// Rarity 
 
	public void loadData(String cardName){
		
	}
	
	public String getName(){
		return name;
 	}
 
 	public int getRarity(){
		return this.rarity;
 	}
 	
 
 }
