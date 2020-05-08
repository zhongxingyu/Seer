 package org.drooms.api;
 
 import java.io.IOException;
 import java.io.Writer;
 
 /**
  * Tracks progress of the game.
  * 
  */
 public interface GameProgressListener {
 
     public void collectibleAdded(Collectible c);
 
     public void collectibleCollected(Collectible c, Player p, int points);
 
     public void collectibleRemoved(Collectible c);
 
     public void nextTurn();
 
     public void playerCrashed(Player p);
 
     public void playerDeactivated(Player p);
 
    public void playerMoved(Player p, Action m, Node... nodes);
 
     public void playerSurvived(Player p, int points);
 
     /**
      * Write a report of the current state.
      * 
      * @param w
      *            Where to write.
      * @throws IOException
      *             When it cannot be written.
      */
     public void write(Writer w) throws IOException;
 
 }
