 package org.itaio.utilities;
 
 import org.itaio.utilities.interfaces.Condition;
 import org.powerbot.game.api.methods.Calculations;
 import org.powerbot.game.api.methods.interactive.NPCs;
 import org.powerbot.game.api.methods.tab.Inventory;
 import org.powerbot.game.api.wrappers.Tile;
 import org.powerbot.game.api.wrappers.interactive.NPC;
 import org.powerbot.game.api.wrappers.map.TilePath;
 
 public class Constants {
 
     public static final int COPPER_ORE = 436;
     public static final int TIN_ORE = 438;
 
     public static final int CRAYFISH_SPOT = 6267;
     public static final int CRAYFISH = 13435;
 
     public static final int TROUT_SPOT = 328;
     public static final int TROUT = 335;
     public static final int SALMON = 331;
 
     public static final int STILES = 11267;
     public static final int LOBSTER_SPOT = 324;
     public static final int LOBSTER = 377;
     public static TilePath PATH_TO_STILES = new TilePath(new Tile[]{new Tile(2923, 3173, 0), new Tile(2907, 3173, 0),
             new Tile(2892, 3164, 0), new Tile(2876, 3157, 0), new Tile(2863, 3150, 0), new Tile(2853, 3144, 0)});
    public static TilePath PATH_TO_LOBSTERS = new TilePath(new Tile[]{new Tile(2860, 3147, 0), new Tile(2868, 3150, 0),
            new Tile(2876, 3157, 0), new Tile(2885, 3161, 0), new Tile(2892, 3164, 0), new Tile(2897, 3170, 0),
            new Tile(2908, 3172, 0), new Tile(2917, 3175, 0), new Tile(2923, 3173, 0)});
     public static final Condition LOBSTER_CONDITION_1 = new Condition() {
         @Override
         public boolean activate() {
             final NPC stiles = NPCs.getNearest(STILES);
            return Inventory.isFull() && (stiles == null || Calculations.distanceTo(stiles) > 6);
         }
     };
     public static final Condition LOBSTER_CONDITION_2 = new Condition() {
         @Override
         public boolean activate() {
             final NPC lobster = NPCs.getNearest(LOBSTER_SPOT);
            return !Inventory.isFull() && (lobster == null || Calculations.distanceTo(lobster) > 6);
         }
     };
 
 
 }
