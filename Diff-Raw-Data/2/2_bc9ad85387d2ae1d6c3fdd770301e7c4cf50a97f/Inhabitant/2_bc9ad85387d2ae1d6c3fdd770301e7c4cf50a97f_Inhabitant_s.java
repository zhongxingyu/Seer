 package ogo.spec.game.model;
 
 public class Inhabitant
 {
 
     protected Tile currentTile;
 
     /**
      * Set the tile.
      *
      * This method should only be called by Tile.setInhabitant.
      */
     public void setCurrentTile(Tile tile)
     {
        assert !tile.hasInhabitant();
         currentTile = tile;
     }
 
     /**
      * Get the current tile.
      */
     public Tile getCurrentTile()
     {
         return currentTile;
     }
 }
