 /**
  * This class stores an x and y coordinate.
  */
 public class Position{
   /**
    * The x coordinate.
    */
   private int x;
 
   /**
    * The y coordinate.
    */
   private int y;
 
   /**
    * Sets the x and y variables.
    * @param x the x position
    * @param y the y position
    */
   Position(int x, int y){
     this.x = x;
     this.y = y;
   }
 
   /**
    * Copy constructor
    * @param old the old position.
    */
   Position(Position old){
     this.x = old.x;
     this.y = old.y;
   }
 
   /**
    * x getter
    * @return the x coordinate.
    */
   public int getX(){
     return x;
   }
 
   /**
    * y getter
    * @return the y coordinate.
    */
   public int getY(){
     return y;
   }
 
   /**
    * Converts to a string.
    */
   public String toString(){
     return "(" + x + ", " + y + ")";
   }
 
   /**
    * Tests if this position is equal to another object.
   * @param object the object we are comparing to.
    * @return true if the object is equivalent.
    */
   public boolean equals(Object other){
     if(this == other) return true;
     if(!(other instanceof Position)) return false;
     Position pos = (Position)other;
     return (this.x == pos.x && this.y == pos.y);
   }
 
   /**
    * Hashes the Position. Used to put these positions into a hashtable.
    */
   public int hashCode(){
     return (x*17) ^ y;
   }
 }
