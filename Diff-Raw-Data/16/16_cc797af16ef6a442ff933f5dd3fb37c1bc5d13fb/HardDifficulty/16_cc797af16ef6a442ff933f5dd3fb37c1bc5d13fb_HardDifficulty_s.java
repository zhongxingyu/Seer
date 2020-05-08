 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package ca.couchware.wezzle2d.difficulty;
 
 import ca.couchware.wezzle2d.Refactorer.RefactorSpeed;
 
 /**
  *
  * @author kgrad
  */
 public class HardDifficulty implements IGameDifficulty
 {
     /** The minimum drop. */
     final private int MINIMUM_DROP = 1;
 
     /** The level at which the difficulty begins to increase. */
     final private int MINIMUM_LEVEL = 2;
 
     /** The number of levels before the difficulty level increases. */
     final private int LEVEL_INTERVAL = 1;
 
     /** The percentage of tiles to maintain. */
     final private int TILE_RATIO = 80;
 
     /** The maximum number of tiles to drop in. */
     final private int MAXIMUM_TOTAL_DROP_AMOUNT = 8;
 
     /**
      * The timer upper bound, in ms.
      */
     final private int timeUpper = 10000;
 
     /**
      * The timer lower bound, im ms.
      */
     final private int timeLower = 500;
 
 
 
 
     public RefactorSpeed getRefactorSpeed()
     {
        return RefactorSpeed.NORMAL;
     }
 
 
 
     public int getDropAmount(int numberOfTiles, int numberOfCells, int level,
             int pieceSize)
     {
 
         // The number of tiles for the current level.
         int levelDrop = (level / this.LEVEL_INTERVAL);
 
         // Check for difficulty ramp up.
         if (level > this.MINIMUM_LEVEL)
         {
             levelDrop = (this.MINIMUM_LEVEL / this.LEVEL_INTERVAL);
         }
 
         // The percent of the board to readd.
        int  boardPercentage = (int) ((numberOfCells - numberOfTiles) * 0.1f);
 
         // The drop amount.
         int dropAmount = -1;
 
         // We are low. drop in a percentage of the tiles, increasing if there
         // are fewer tiles.
         if ((numberOfTiles / numberOfCells) * 100 < this.TILE_RATIO)
         {
             // If we are past the level ramp up point, drop in more.
             if (level > this.MINIMUM_LEVEL)
             {
                 dropAmount = pieceSize + levelDrop
                         + (level - this.MINIMUM_LEVEL)
                         + boardPercentage + this.MINIMUM_DROP;
 
                 if (dropAmount > this.MAXIMUM_TOTAL_DROP_AMOUNT + pieceSize)
                 {
                     dropAmount = this.MAXIMUM_TOTAL_DROP_AMOUNT + pieceSize;
                 }
             }
             else
             {
                 dropAmount = pieceSize + levelDrop + boardPercentage
                         + this.MINIMUM_DROP;
 
                 if (dropAmount > this.MAXIMUM_TOTAL_DROP_AMOUNT + pieceSize)
                 {
                     dropAmount = this.MAXIMUM_TOTAL_DROP_AMOUNT + pieceSize;
                 }
             }
         }
         else
         {
             // If we are past the level ramp up point, drop in more.
             if (level > this.MINIMUM_LEVEL)
             {
                 dropAmount = pieceSize + levelDrop
                         + (level - this.MINIMUM_LEVEL)
                         + this.MINIMUM_DROP;
 
                 if (dropAmount > this.MAXIMUM_TOTAL_DROP_AMOUNT + pieceSize)
                 {
                     dropAmount = this.MAXIMUM_TOTAL_DROP_AMOUNT + pieceSize;
                 }
             }
             else
             {
                 dropAmount = pieceSize + levelDrop + this.MINIMUM_DROP;
 
                 if (dropAmount > this.MAXIMUM_TOTAL_DROP_AMOUNT + pieceSize)
                 {
                     dropAmount = this.MAXIMUM_TOTAL_DROP_AMOUNT + pieceSize;
                 }
             }
         } // end if
 
         // See if the drop amount is -1.  If it is, then something broke.
         if (dropAmount == -1)
         {
             throw new IllegalStateException("The drop amount was not set properly.");
         }
 
         // Otherwise, update the total drop amount.
         return dropAmount;
 
     }
 
     public int getMaxTime()
     {
         return this.timeUpper;
     }
 
     /**
      * Determine the time limit for the given level.
      * @param level
      * @return
      */
     public int determineTimeForLevel(int level)
     {
         return Math.max(timeUpper - (level*2 - 2) * 1000, timeLower);
     }
 
     
 
 }
