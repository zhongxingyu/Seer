 package zuul;
 
 /**
  * Direction class represents all the ways that a room can be placed relative to
  * another room. Effectively, this also represents all the ways that a person
  * can move while in a particular room.
  * 
  * @author Ryan Seys
  * @version 2012.11.02
  */
 public enum Direction {
   // All directions possible
   NORTH, EAST, SOUTH, WEST;
 
   /**
    * Useful for undo commands, getting the opposite direction of the command
    * returns the direction if you were to turn your player 180 degrees around.
    * 
    * @return The opposite direction of one specified
    */
   public Direction getOpposite() {
     if (equals(Direction.NORTH)) return Direction.SOUTH;
     else if (equals(Direction.EAST)) return Direction.WEST;
     else if (equals(Direction.SOUTH)) return Direction.NORTH;
     else if (equals(Direction.WEST)) return Direction.EAST;
     else return null;
   }
 }
