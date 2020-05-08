 package uk.ac.york.minesweeper;
 
 /**
  * Event fired when the state of the game changes
  */
 public interface MinefieldStateChangeListener
 {
     /**
      * Called when the game state of the minefield changes
      *
     * @param minefield the minefield that changed state
      */
     public void stateChanged(MinefieldStateChangeEvent event);
 }
