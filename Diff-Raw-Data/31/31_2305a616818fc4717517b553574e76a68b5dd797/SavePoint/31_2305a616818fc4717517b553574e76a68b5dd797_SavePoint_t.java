 package dungeon.models;
 
import java.awt.geom.Rectangle2D;

 /**
  * A save point.
  *
  * If the player interacts with it (touching or attacking), it will activate and set the player's save point to it's
  * current position.
  */
public class SavePoint implements Spatial {
   public static final int SIZE = 500;
 
   /**
    * Where is the save point?
    */
   private final Position position;
 
   public SavePoint (Position position) {
     this.position = position;
   }
 
   public Position getPosition () {
     return this.position;
   }

  @Override
  public Rectangle2D space () {
    return new Rectangle2D.Float(this.position.getX(), this.position.getY(), SIZE, SIZE);
  }
 }
