 package de.htwg.se.dog.models;
 
 
 public class Gamefield {
	private final int NOOWNER = 0;
 	
 	Field[] gamefield;
 	private final int fieldsTillHouse;
 	private final int playerCount;
 	private final int houseCount;
 	
 	public Gamefield(int fieldsTilHouse, int playerCount, int houseCount){
 		this.fieldsTillHouse = fieldsTilHouse;
 		this.houseCount = houseCount;
 		this.playerCount = playerCount;
 		
 		gamefield = new Field[(fieldsTilHouse+houseCount)*playerCount];
 		generateGamefield();
 	}
 
 	public void generateGamefield(){
 		//System.out.println(this.fieldsTillHouse +" " + this.playerCount + " " + this.houseCount);
 		for(int i = 0; i < playerCount; i++){
 			int startfield = (fieldsTillHouse + houseCount ) *  i;
 			for (int j = startfield; j < (startfield + fieldsTillHouse); j++){
 				gamefield[j] = new Field(NOOWNER);
 			}
 			
 			for (int j = startfield + fieldsTillHouse;
 				 j < startfield + fieldsTillHouse + houseCount; j++){
 				gamefield[j] = new Field(i+1);
 			}
 			
 		}
 	}
 	
 	public int getOwner(int fieldNr){
 		return this.gamefield[fieldNr].getOwner();
 	}
 	
 }
