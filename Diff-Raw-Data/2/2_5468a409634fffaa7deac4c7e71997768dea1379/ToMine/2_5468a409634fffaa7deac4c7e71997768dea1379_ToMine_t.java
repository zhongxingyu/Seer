 package org.chaos.task;
 
 import org.chaos.Miner;
 import org.powerbot.script.wrappers.Tile;
 import org.powerbot.script.wrappers.TilePath;
 
 /**
  * @author chaos_
  * @since 1.1 <2:55 PM - 10/11/13>
  */
 public class ToMine extends Task {
 
     private static final Tile[] TO_MINE = new Tile[] {
         //TODO: generate a path from Varrock West Mine to Varrock Bank
     };
 
     private final TilePath minePath;
 
     /**
      * In the constructor of ToSpot is the Mining script,
      * with a variable name of "script" for ease of reading.
      * From the script we extract the MethodContext so that the
      * requirement can be fulfilled when extending MethodProvider.
      *
      * Through the MethodContext we can access useful things
      * such as the Objects of the game, or the Npcs of the game.
      * The method context is basically the most important thing
      * when interacting directly with the clients code. All data
      * that you need directly from the client can be found here.
      *
      * @since 1.1   Changed the Task constructor from - Task(MethodContext ctx)
      *              to - Task(Miner script); this change was made to accommodate
      *              future expansion and configuration located in the Miner class.
      *
      * @param script    The MethodContext required when extending "MethodProvider"
      *                  is passed down from the Script when you initialize and put
      *                  the Tasks in the TaskList.
      */
     public ToMine(Miner script) {
         super(script);
         minePath = new TilePath(script.getContext(), TO_MINE);
     }
 
     private boolean invEmpty() {
         return ctx.backpack.select().isEmpty();
     }
 
     private boolean atMine() {
         final Tile endTile = TO_MINE[TO_MINE.length - 1];
        return ctx.players.local().getLocation().distanceTo(endTile) < 5;
     }
 
     @Override
     public boolean activate() {
         return invEmpty() && !atMine();
     }
 
     @Override
     public void execute() {
         minePath.traverse();
     }
 
 }
