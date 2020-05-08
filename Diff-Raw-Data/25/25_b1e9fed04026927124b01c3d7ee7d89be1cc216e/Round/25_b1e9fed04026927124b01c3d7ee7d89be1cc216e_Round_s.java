 package org.zac.games.mahjong.core.round;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.zac.games.mahjong.core.action.Action;
 import org.zac.games.mahjong.core.game.Wind;
 import org.zac.games.mahjong.core.rule.BrokenRuleException;
 import org.zac.games.mahjong.core.tile.Tile;
 
 public class Round
 {
 
 	private Stock stock;
 	
 	private Map<Wind, Hand> hands;
 	
 	private Wind currentPlayer;
 	
 	public Round()
 		throws BrokenRuleException
 	{
 		TileSet set = new TileSet();
 		
 		// Build walls
 		stock = new Stock(set);
 		
 		// Builds hands
 		hands = dealHands();
 		
 		currentPlayer = Wind.EAST;
 	}
 	
 	private Map<Wind, Hand> dealHands()
 	{
 		Map<Wind, List<Tile>> tiles = new HashMap<Wind, List<Tile>>();
 		
 		for (Wind wind : Wind.values())
 		{
 			tiles.put(wind, new ArrayList<Tile>());
 		}
 		
 		Wind currentHand = Wind.EAST;
 		for (int i = 0; i < 3 * Wind.values().length; ++i)
 		{
 			tiles.get(currentHand).addAll(stock.getNextTiles(4));
 			currentHand = currentHand.next();
 		}
 		for (int i = 0; i < Wind.values().length; ++i)
 		{
 			tiles.get(currentHand).add(stock.getNextTile());
 			currentHand = currentHand.next();
 		}
 		
 		Map<Wind, Hand> hands = new HashMap<Wind, Hand>();
 		for (Wind wind : Wind.values())
 		{
 			hands.put(wind, new Hand(tiles.get(wind)));
 		}
 		
 		return hands;
 	}
 	
 	public void doAction(Action action)
 		throws BrokenRuleException
 	{
 		action.apply(currentPlayer, null, hands.get(action.getActingWind()));
 	}
 	
 }
