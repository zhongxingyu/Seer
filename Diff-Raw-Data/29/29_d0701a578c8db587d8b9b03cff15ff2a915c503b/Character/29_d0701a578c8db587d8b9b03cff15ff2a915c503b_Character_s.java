 package cluedo.cards;
 
 public class Character implements CharacterI{
 
 	
 	private String card;
 	
 	
	public Character(int i){
		this.card = CharacterI.CHARACTERS[i];
 		
 	}
 	
 	@Override
 	public String getCard() {
 		// TODO Auto-generated method stub
 		return card;
 	}
 
 }
