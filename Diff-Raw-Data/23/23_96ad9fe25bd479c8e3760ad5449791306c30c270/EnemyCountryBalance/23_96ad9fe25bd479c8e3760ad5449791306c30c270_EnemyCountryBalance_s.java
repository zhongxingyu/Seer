 package descriptors;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import game.Country;
 import game.GameState;
 import game.Player;
 import game.RandomPlayer;
 
 public class EnemyCountryBalance extends Descriptor
 {
 	/**
 	 * Describe how equally the countries are divided among the enemy players.
 	 */
 	public double describe(GameState state, Player player)
 	{
 		// Count the number of countries for each player.
 		Map<Player, Integer> numberOfCountries = new HashMap<Player, Integer>();
		int numCountries = 0;
 
 		for (Country country : state.getCountries())
 			if(!country.getPlayer().equals(player))
 			{
 				numberOfCountries.put(country.getPlayer(),
 						numberOfCountries.containsKey(country.getPlayer())
 						? numberOfCountries.get(country.getPlayer()) + 1
 								: 1);
				numCountries++;
 			}
 		
		int playerCount = 0;
 		while ( numberOfCountries.size() < state.getNumberOfPlayers())
 			numberOfCountries.put(new RandomPlayer("empty"+playerCount++), 0);
 
 		return normalize((double) variance(numberOfCountries.values()), 0, 1);
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
