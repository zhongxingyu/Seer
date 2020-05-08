 package dk.stacktrace.risk.game_logic.continents;
 
 import java.util.ArrayList;
 
 import android.util.Log;
 
 import dk.stacktrace.risk.game_logic.Board;
 import dk.stacktrace.risk.game_logic.Player;
 import dk.stacktrace.risk.game_logic.Territory;
 
 public abstract class Continent {
 	ArrayList<Territory> territories;
 	int reinforcementBonus;
 	Board board;
 	
 	public boolean ownedBy(Player player){
		return territories.containsAll(board.getTerritoriesOwnedBy(player));
 	}
 	
 	public int getReinforcementBonus() {
 		return reinforcementBonus;
 	}
 	
 	protected abstract void createTerritories();
 	public abstract void setNeighbours();
 	
 	public ArrayList<Territory> getTerritories()
 	{
 		return territories;
 	}
 	
 
 }
