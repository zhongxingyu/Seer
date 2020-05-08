 package cluedo.cards;
 
 public class Character implements CharacterI{
 
 	
 	private String card;
 	
 	
	public Character(String s){
		this.card = s;
 		
 	}
 	
 	@Override
 	public String getCard() {
 		// TODO Auto-generated method stub
 		return card;
 	}
 
 }
