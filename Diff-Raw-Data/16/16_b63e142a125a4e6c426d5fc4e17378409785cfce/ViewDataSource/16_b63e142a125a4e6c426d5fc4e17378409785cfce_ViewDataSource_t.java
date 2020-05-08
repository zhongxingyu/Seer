 package vooga.fighter.controller.interfaces;
 
 import java.awt.Dimension;
 import java.util.List;
 
 import util.Location;
import util.Paintable;
 
 /**
  * Defines what information the View recieves.
  * 
  * @author Jack Matteucci
  * @author Wayne You
  *
  */
 public interface ViewDataSource {
 
     /**
      * @return Number of Paintable objects
      */
     public int ObjectNumber ();
 
     /**
      * @param index
      * @return The Paintable object with given index
      */
     public Paintable getPaintable (int index);
 
     /**
      * @param index
      * @return The Location for the Paintable object with given index
      */
     public Location getLocation (int index);
 
     /**
      * @param index
      * @return The size as a Dimension for the Paintable object with given index.
      */
     public Dimension getSize (int index);
 
     /**
      * @param index
      * @return A list of the given effects of an object with the given index.
      */
     public List<Integer> getImageEffects (int index);
 }
