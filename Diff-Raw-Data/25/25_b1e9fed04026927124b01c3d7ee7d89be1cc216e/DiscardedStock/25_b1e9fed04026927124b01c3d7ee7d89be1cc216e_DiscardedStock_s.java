 package org.zac.games.mahjong.core.round;
 
public DiscardedStock
 {
     
     private Map<Wind, Stack<Tile>> discardedStacks;
     
     private Wind lastDiscarding;
     
     public DiscardedStock()
     {
         discardedStacks = new HashMap<Wind, Stack<Tile>>();
        for (Wind wind in Wind.values())
         {
             discardedStacks.put(wind, new Stack<Tile>());
         }
         
         lastDiscarding = null;
     }
     
     public void discard(Wind discardingWind, Tile discardedTile)
     {
         lastDiscarding = discardingWind;
        discardedStacks.push(discardedTile);
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
