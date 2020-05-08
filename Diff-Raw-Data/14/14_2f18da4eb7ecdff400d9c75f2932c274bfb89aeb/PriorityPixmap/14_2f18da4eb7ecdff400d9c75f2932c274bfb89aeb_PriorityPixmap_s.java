 package drawing;
 
 import util.Pixmap;
 
 /**
  * Represents a Pixmap that can also have an order of precedence when painting
  * by calling Collections.sort() on a collection of PriorityPixmaps
  * 
  * A lower myPriority value corresponds to a higher priority
  * 
  * @author Ellango
  *
  */
 public class PriorityPixmap extends Pixmap {
     private int myPriority;
 
     /**
      * Constructs a PriorityPixmap with a name of the filepath for the image and
      * a number for its priority in printing.  Smaller numbers represent higher
      * priority
      * 
      * 0 should be used for highest priority
      * 
      * @param filename is the image's filename
      * @param priority determines order in which the pixmap is painted. 
      * The lower the number, the first it will get painted.
      */
     public PriorityPixmap (String filename, int priority) {
         super(filename);
     }
     
     /**
      * Returns this PriorityPixmap's priority
      * 
      * @return int of priority
      */
     public int getPriority() {
         return myPriority;
     }
 }
