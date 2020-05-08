 /**
  * 
  */
 package rky.portfolio;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * @author stoked
  *
  */
 public class ScoreBoard
 {
 	static class BoardCell
 	{
 		public BoardCell(double startBudget, double profit)
 		{
 			this.startBudget = startBudget;
 			this.profit = profit;
 		}
 		
 		public double startBudget;
 		public double profit;
 	}
 	
 	Set<Player> players = new HashSet<Player>();
 	final GameMode mode;
 	ArrayList<Map<Player, BoardCell>> budgets;   //array is indexed by turn number
 	
 	public enum GameMode { mode1, mode2 };
 	
 	public ScoreBoard(GameMode mode, int numTurns, Set<Player> players)
 	{
 		this.mode = mode;
 		this.players = players;
 		budgets = new ArrayList<Map<Player, BoardCell>>(numTurns+1);
		for( int i = 0; i < numTurns; i++ ) {
 			budgets.add(new HashMap<Player, BoardCell>());
 		}
 		for( Player p : players ) {
 			budgets.get(0).put(p, new BoardCell(1.0, 0));
 		}
 	}
 	
 	public void add( int turnNumber, Player player, double profit )
 	{
 		BoardCell cell = budgets.get(turnNumber).get(player);
 		cell.profit = profit;
 		budgets.get(turnNumber+1).put(player, new BoardCell(cell.startBudget + profit, 0));
 	}
 
 	public double getBudget(int turnNumber, Player player)
 	{
 		return budgets.get(turnNumber).get(player).startBudget;
 	}
 	
 	public double getStartBudget( int turnNumber, Player player, GameMode mode )
 	{
 		if( mode == GameMode.mode1 )
 			return 1.0;
 		
 		return getBudget( turnNumber, player );
 	}
 }
