 package descriptors;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import game.Country;
 import game.GameState;
 import game.Player;
 import game.RandomPlayer;
 
 public class EnemyDiceBalance extends Descriptor
 {
 	/**
 	 * Describe how equally the dices are distributed among the enemy players.
 	 */
 	public double describe(GameState state, Player player)
 	{
 		// Count the number of countries for each player.
 		Map<Player, Integer> numberOfDice = new HashMap<Player, Integer>();
 		for (Country country : state.getCountries())
 			if (!country.getPlayer().equals(player))
 			{
 				numberOfDice.put(country.getPlayer(),
 						numberOfDice.containsKey(country.getPlayer())
 						? numberOfDice.get(country.getPlayer()) + country.getDice()
 								: country.getDice());
 			}
 		
		int playerCount = 1;
 		while (numberOfDice.size() < state.getNumberOfPlayers())
 			numberOfDice.put(new RandomPlayer("empty"+playerCount++), 0);
 
 		return normalize((double) variance(numberOfDice.values()), 0, 1);
 	}
 
 	private double variance(Collection<Integer> numbers)
 	{
 		int sum = 0;
 		for (Integer number : numbers)
 			sum += number;
 
 		double mean = (double) sum / numbers.size();
 
 		double difference = 0;
 		for (Integer number : numbers)
 			difference += Math.abs(number - mean);
 		
 		double maxvariance = mean * (numbers.size()-2) + sum;
 		
 		if (maxvariance > 0)
 			return (double) difference / maxvariance;
 		else
 			return 1;
 	}
 }
