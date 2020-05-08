 package se.citerus.crazysnake;
 
 /**
  * A Square is a specific cell on the board with its content.
  */
 public interface Square {
 
     /**
      * Returns true if the square does not contain a wall or a snake-part, otherwise false.
      */
     boolean isUnoccupied();
 
     /**
      * Returns the content of the Square.
      */
     SquareContent getContent();
 
     /**
      * Tells whether this Square is occupied by a snake
      */
     boolean containsSnake();
 
     /**
      * Tells whether this Square is occupied by a fruit
      */
     boolean containsFruit();
 
     /**
      * Tells whether this Square is occupied by a wall
      */
     boolean containsWall();
 
     /**
      * Returns true if the square does not contain a wall or a snake-part, otherwise false.
       */
      boolean hasSolidContent();
 
 }
