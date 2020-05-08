 package de.htwg.se.dog.models;
 
 public class Figure {
	private final int ZERO = 0;
	private final int FOUR = 4;
 	private final int owner;
 	private final int fignr;
 	
 	public Figure(int owner, int fignr){
 		if(owner > ZERO){
 			this.owner = owner;
 		} else {
 			throw new IllegalArgumentException("Failed to create Figure with Owner:" + owner);
 		}
 		if ( fignr > ZERO && fignr <= FOUR){
 			this.fignr = fignr;	
 		} else  {
 			throw new IllegalArgumentException("Failed to create Figrue with Fignr:" + fignr);
 		}
 		
 	}
 	
 	public int getOwner(){
 		return this.owner;
 	}
 	public int getFignr(){
 		return this.fignr;
 	}
 	
 }
