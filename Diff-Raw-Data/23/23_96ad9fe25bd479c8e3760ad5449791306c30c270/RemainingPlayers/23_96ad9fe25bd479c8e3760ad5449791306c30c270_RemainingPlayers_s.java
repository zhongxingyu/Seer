 package descriptors;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import game.Country;
 import game.GameState;
 import game.Player;
 import game.RandomPlayer;
 
 public class RemainingPlayers extends Descriptor
 {
 	/**
 	 * Describe how many players are remaining compared to the start of the game
 	 */
 	public double describe(GameState state, Player player)
 	{
		return normalize((double)(state.getPlayers().size()) / (double)(state.getNumberOfPlayers()), 0, 1);
 	}
 }
