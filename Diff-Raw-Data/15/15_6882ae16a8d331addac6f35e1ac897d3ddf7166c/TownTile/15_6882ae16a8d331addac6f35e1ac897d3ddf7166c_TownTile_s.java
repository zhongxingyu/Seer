 /**
  * A subclass of tile representing the town
  *
  * @author Kennon Bittick
  * @version 11 | 21 | 2013
  */
 public class TownTile extends Tile {
 
     /**
      * A constructor that just calls the super constructor
      **/
     public TownTile() {
         super("town");
     }
 
     /**
      * Give the provided player the resources for the tile, which is known for the town
      *
      * @param player the player to whom the resources will be added
      **/
     public void collectResources(Player player) {
     	
     }
 
     /**
      * Return the image string for the tile
      *
      * @return null, just use the background of the map
<<<<<<< HEAD
      */
=======
     **/
>>>>>>> 9c97b2714310fbe52c94ee7445730bfa9f0e68df
     public String image() {
         return null;
     }
 }
