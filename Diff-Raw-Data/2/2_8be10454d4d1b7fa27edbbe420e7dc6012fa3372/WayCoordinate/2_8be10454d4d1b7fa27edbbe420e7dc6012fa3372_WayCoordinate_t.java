 package game.strategy.tom;
 
 import game.Command;
 
 public class WayCoordinate implements Comparable<WayCoordinate>{
 
   public int steps;
   public int row;
   public int col;
   public Command steppedHereWith;
   
   private int cmp;
 
  public WayCoordinate(int col, int row, int destCol, int destRow, int steps, Command lastStep) {
     this.col = col;
     this.row = row;
     this.steps = steps;
     this.steppedHereWith = lastStep;
     
     cmp = steps + Math.abs(destCol - col) + Math.abs(destRow - row);
   }
 
   @Override
   public int compareTo(WayCoordinate other) {
     return Integer.compare(cmp, other.cmp);
   }
 
 }
