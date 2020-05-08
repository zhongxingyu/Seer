 package cha.domain;
 
 import cha.domain.Categories.Category;
 
 public class Tile {
 	
 	/**
 	 * @uml.property  name="type"
 	 */
 	Category type;
 	boolean challenge;
 	
 	public Tile(Category c, boolean challenge){
 		this.type = c;
 		this.challenge = challenge;
 	}
 	
 	
	public Category getCategory( ){
 		return type;
 	}
 	
 	public boolean isChallenge(){
 		return challenge;
 	}
 	
 	@Override
 	public String toString() {
 		return type.toString();
 	}
 	
 }
