 package cluedo.cards;
 /**
  * 
  * @author Michael
  *
  */
 public class Room implements RoomI {
 
 	private String room;
 	
 	
 	public Room(String s){
 		this.room = s;
 		
 	}
 	
 	
 	
 	@Override
 	public String getCard() {
		// TODO Auto-generated method stub
		return null;
 	}
 
 
 
 	@Override
 	/**
 	 * @ retrun the first 2 letters capitalised
 	 */
 	public String getSymbol() {
 		return room.charAt(0) + "" + java.lang.Character.toUpperCase(room.charAt(1));
 	}
 
 }
