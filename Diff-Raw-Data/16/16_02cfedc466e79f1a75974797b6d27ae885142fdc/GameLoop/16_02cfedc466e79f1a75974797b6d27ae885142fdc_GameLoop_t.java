 package rky.portfolio;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import rky.portfolio.gambles.Gamble;
 import rky.portfolio.gambles.Gambles;
 import rky.portfolio.gambles.Luck;
 import rky.portfolio.gambles.Return;
 import rky.portfolio.io.GameData;
 import rky.portfolio.io.GameData.ClassFavorabilityMap;
 import rky.util.SetMap;
 
 public class GameLoop implements Runnable
 {
 	static final double PRECISION = 0.00001;
 	
 	final Map<Integer, Gamble>   gambles;
 	final Map<Gamble, Integer>   ids;
 	final SetMap<Gamble, Gamble> links;
 	final ClassFavorabilityMap   classes;
 	final Map<Gamble, Integer>   gambleClasses;
 	final Set<Player>            players;
 	
 	final Map<Player, String> playerErrors = new HashMap<Player, String>();
 
 	final ScoreBoard.GameMode gameMode;
 	final ScoreBoard scoreBoard;
 	final int numberOfTurns;
 	int currentTurn;
 	
 	public GameLoop( GameData gameData, Set<Player> players, ScoreBoard.GameMode gameMode )
 	{
 		this.gambles       = gameData.gambles;
 		this.ids           = gameData.ids;
 		this.links         = gameData.links;
 		this.classes       = gameData.classFavorability;
 		this.gambleClasses = gameData.gambleClasses;
 		this.players       = players;
 		this.numberOfTurns = gameMode == ScoreBoard.GameMode.mode1 ? 5 : 200;
 		this.gameMode      = gameMode;
 		
 		scoreBoard = new ScoreBoard(gameMode, numberOfTurns, players);
 	}
 	
 	/**
 	 * @param gamblesInOrder order in which to play the gambles
 	 * @return a mapping of each gamble to its return
 	 */
 	public Map<Gamble, Return> playGambles(List<Gamble> gamblesInOrder)
 	{
 		Map<Gamble, Return> played = new HashMap<Gamble, Return>();
 		
 		for( Gamble g : gamblesInOrder )
 		{
 			Luck classLuck = classes.get( currentTurn /*round*/, gambleClasses.get(g) /*class id*/);
 			Return ret = Gambles.playGamble( g, classLuck, played, links.get(g) );
 			played.put( g, ret );
 		}
 		
 		return played;
 	}
 	
 	/**
 	 * Plays gambles in random order.
 	 * @return a mapping of each gamble to its return
 	 */
 	public Map<Gamble, Return> playGambles()
 	{
 		ArrayList<Gamble> gambleOrder = new ArrayList<Gamble>(ids.keySet());
 		Collections.shuffle(gambleOrder);
 		return playGambles(gambleOrder);
 	}
 
 	public void run()
 	{
 		
 		for( currentTurn = 0; currentTurn < numberOfTurns; currentTurn++ )
 		{	
 			// mapping for each player of their money distributions
 			Map<Player, Map<Integer, Double>> playerMoneyDistributions = getDistributionsFromPlayers();
 			
 			for( Player player : playerErrors.keySet() )
 				disqualifyPlayer( player );
 			
 			Map<Gamble, Return> gambleReturns = playGambles();
			System.out.println( "Gamble returns: " + gambleReturns );
 			
 			for( Player player : playerMoneyDistributions.keySet() )
 			{
 				double profit = computeProfit( gambleReturns, playerMoneyDistributions.get(player) );
 				scoreBoard.add( currentTurn, player, profit );
 			}
 		}
 	}
 
 	private void disqualifyPlayer(Player player) {
 		// TODO Auto-generated method stub
 		
 		throw new RuntimeException(player + " broke something");
 	}
 
 	private double computeProfit(Map<Gamble, Return> gambleReturns, Map<Integer, Double> investments)
 	{
 		double profit = 0;
 		for( Integer gambleId : investments.keySet() )
 		{
 			Gamble g = gambles.get( gambleId );
 			profit += g.getV( gambleReturns.get(g) ) * investments.get(gambleId);
 		}
 		return profit;
 	}
 
 	private Map<Player, Map<Integer, Double>> getDistributionsFromPlayers()
 	{
 		playerErrors.clear();
 		
 		Map<Player, Map<Integer, Double>> distributions = new HashMap<Player, Map<Integer, Double>>();
 		// first send the requests to all
 		for( Player player : players )
 		{
 			sendDistributionRequest(player);
 		}
 		for( Player player : players )
 		{
 			Map<Integer, Double> moneyDistrib = getPlayerMoneyDistribution(player);
 			if( ! isValidMoneyDistribution(moneyDistrib) )
 				playerErrors.put( player, "Submitted an invalid money distribution" );
 			else
 				distributions.put( player, moneyDistrib );
 		}
 		return distributions;
 	}
 
 	private boolean isValidMoneyDistribution(Map<Integer, Double> moneyDistrib)
 	{
 		boolean valid = true;
 		
 		double sum = 0;
 		for( Integer gambleId : moneyDistrib.keySet() )
 		{
 			if( !gambles.containsKey(gambleId) ) {
 				valid = false;
 				break;
 			}
 			
 			sum += moneyDistrib.get(gambleId);
 		}
 		if( sum > 1.0 + PRECISION )
 			valid = false;
 		
 		return valid;
 	}
 
 	// receives the distribution from the client
 	private Map<Integer, Double> getPlayerMoneyDistribution(Player player)
 	{
 		// TODO Auto-generated method stub
 		Map<Integer, Double> distribMap = new HashMap<Integer, Double>();
 		
 		// TODO: remove this test code
 		double distribute = Math.random();
 		for( Integer gId : gambles.keySet() ) {
 			if( Math.random() > 0.7 ) {
 				double val = distribute * Math.random();
 				distribute -= val;
 				distribMap.put(gId, val);
 			}
 		}
 		return distribMap;
 	}
 
 	// sends the request to player (the amount of money he has, or what not)
 	private void sendDistributionRequest(Player player)
 	{
 		double currentBudget = scoreBoard.getStartBudget(currentTurn, player, gameMode);
 		// TODO send the player starting value "currentBudget"
 	}
 }
