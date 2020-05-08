 package game.fitness;
 
 import game.Board;
 import game.Cell;
 import game.State;
 import game.StaticConfig;
 import game.ai.Fitness;
 
 /**
  * A reachable lift is good.
  * Only looks at the 8-neighborhood of lift. 
  * 
  * @author seba
  *
  */
 public class LiftReachable implements Fitness {
 
   private final StaticConfig sconfig;
   
   public LiftReachable(StaticConfig sconfig) {
     this.sconfig = sconfig;
   }
   
   @Override
   public int evaluate(State state) {
     if (liftReachable(state.board, sconfig.liftx, sconfig.lifty) > 0)
       return 1000000;
     return 0;
   }
   
   /**
    * Returns value > 0 if lift is reachable.
    */
   public static int liftReachable(Board board, int liftx, int lifty) {
     Cell up = board.get(liftx, lifty + 1);
     Cell down = board.get(liftx, lifty - 1);
     Cell left = board.get(liftx - 1, lifty);
     Cell right = board.get(liftx + 1, lifty);
     return cell2int(up) + cell2int(down) + cell2int(left) + cell2int(right);
   }
   
   private static int cell2int(Cell c) {
     switch (c) {
     case Empty:
     case Earth:
     case Lambda:
       return 1;
     default:
       return 0;
     }
   }
 
 }
