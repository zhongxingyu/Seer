 package org.ojim.logic.rules;
 
 import org.ojim.logic.state.Field;
 import org.ojim.logic.state.GameState;
 import org.ojim.logic.state.Player;
 import org.ojim.logic.state.Street;
 
 public class GameRules {
 
 	private static GameState state;
 	
 	public GameRules(GameState state) {
 		this.state = state;
 	}
 	
	public static boolean upgradeStreet(int playerID, int position, int levelChange) {

		if(!canPlayerModifyStreet(playerID, position)) {
 			return false;
 		}
		
 		//then try to upgrade/downgrade the Street
		return ((Street)state.getFieldByID(position)).upgrade(levelChange);
 	}
 	
 	private static boolean canPlayerModifyStreet(Player player, Field street) {
 		
 		//Is the player on turn?
 		if(player != state.getActivePlayer()) {
 			return false;
 		}
 		
 		//Is the Field at position a Street?
 		if(!(street instanceof Street)) {
 			return false;
 		}
 		
 		//Is the player on turn owner of the street?
 		if(state.getActivePlayer() != ((Street)street).getOwner()) {
 			return false;
 		}
 		
 		return true;
 	}
 
 	public static boolean changeMortgage(Player player, Field street, boolean mortgaged) {
 		
 		if(!canPlayerModifyStreet(player, street)) {
 			return false;
 		}
 		
 		return true;
 	}
 	
 	public static boolean changeMortgage(int playerID, int position, boolean mortgaged) {
 		
 		Street street = (Street)state.getFieldByID(position);
 		Player player = state.getPlayerByID(playerID);
 			
 		return changeMortgage(player, street, mortgaged);
 	}
 }
