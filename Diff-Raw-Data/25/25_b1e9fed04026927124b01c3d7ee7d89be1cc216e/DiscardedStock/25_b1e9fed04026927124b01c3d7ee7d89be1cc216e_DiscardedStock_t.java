 package org.zac.games.mahjong.core.round;
 
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.zac.games.mahjong.core.game.Wind;
import org.zac.games.mahjong.core.tile.Tile;

public class DiscardedStock
 {
     
     private Map<Wind, Stack<Tile>> discardedStacks;
     
     private Wind lastDiscarding;
     
     public DiscardedStock()
     {
         discardedStacks = new HashMap<Wind, Stack<Tile>>();
        for (Wind wind : Wind.values())
         {
             discardedStacks.put(wind, new Stack<Tile>());
         }
         
         lastDiscarding = null;
     }
     
     public void discard(Wind discardingWind, Tile discardedTile)
     {
         lastDiscarding = discardingWind;
        discardedStacks.get(discardingWind).push(discardedTile);
     }
     
     public Tile getLastDiscardedTile()
     {
         if (lastDiscarding != null)
         {
             discardedStacks.get(lastDiscarding).pop();
             lastDiscarding = null;
         }
         
         return null;
     }
     
 }
