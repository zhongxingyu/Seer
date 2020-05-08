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
		if (state.getPlayers().size() == 1)
			return -1;
		else
			return normalize((double)(state.getPlayers().size() - 1) / (double)(state.getNumberOfPlayers() - 1), 0, 1);
 	}
 }
