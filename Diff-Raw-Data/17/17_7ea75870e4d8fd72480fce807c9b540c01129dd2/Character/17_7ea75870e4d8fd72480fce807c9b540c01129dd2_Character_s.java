 package cluedo.cards;
 
 /**
  * 
  * @author Michael
  *
  */
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
 
 	@Override
 	/**
 	 * @return the first letter and the letter after the underscore converted to lower case
 	 */
 	public String getSymbol() {
 		return card.charAt(0) + "" + java.lang.Character.toLowerCase(card.charAt(card.lastIndexOf(" ") + 1));
 	}
 
 }
