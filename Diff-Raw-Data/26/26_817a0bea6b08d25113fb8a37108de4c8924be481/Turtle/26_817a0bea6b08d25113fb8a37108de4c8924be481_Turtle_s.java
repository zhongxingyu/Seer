 package object;
 
 import java.awt.Dimension;
 import java.util.ResourceBundle;
 import util.Animal;
 import util.Location;
 import util.Pixmap;
 import view.Canvas;
 
 
 /**
  * 
  * @author Richard Yang
  * @author Leonard
  * @author Alan Ni
  */
 
 public class Turtle extends Animal {
 
     /**
      * Default dimension size
      */
     public static final Dimension TURTLESIZE = new Dimension(50, 50);
     private static final String TURTLE = "turtle.gif";
     private static final Pixmap TURTLE_IMAGE = new Pixmap(TURTLE);
     private static final String DEFAULT_RESOURCE_PACKAGE = "resources.";
     private double myAngle;
     private boolean myLeftTrail;
     private boolean myVisible;
     private Dimension myTurtleSize;
     private ResourceBundle mySizes;
     private Trail myTrail;
     private Trail myUndoneTrails;
     private int myIndex;
 
     /**
      * Constructs turtle object
      * 
      * @param myLocation myLocation
      * @param angle myAngle
      */
     public Turtle (Location myLocation, double angle) {
         super(TURTLE_IMAGE, myLocation, TURTLESIZE);
         myAngle = angle;
         setProperties();
     }
 
     /**
      * Constructs turtle object with image
      */
     public Turtle () {
         super(TURTLE_IMAGE, new Location(Canvas.TURTLE_AREA_SIZE.width / 2,
                                          Canvas.TURTLE_AREA_SIZE.height / 2), TURTLESIZE);
         myAngle = 0;
         setProperties();
     }
 
     private void setProperties () {
         myTrail = new Trail();
         myUndoneTrails = new Trail();
         ResourceBundle.getBundle(DEFAULT_RESOURCE_PACKAGE + "English");
         mySizes = ResourceBundle.getBundle(DEFAULT_RESOURCE_PACKAGE + "Turtles");
         myTurtleSize = TURTLESIZE;
         myIndex = 2;
     }
 
     /**
      * initialize turtle at center
      */
     public void initialize () {
         super.setCenter(new Location(0, 0));
     }
 
     /**
      * add a trail
      */
     public void addTrail () {
         myTrail.addTrail(new Location(getX(), getY()));
     }
 
     /**
      * Are there trails left?
      */
     public void leftTrail () {
         myLeftTrail = true;
     }
 
     /**
      * avoid trail
      */
     public void avoidTrail () {
         myLeftTrail = false;
     }
 
     /**
      * clear all trails
      */
     public void clearTrail () {
         myTrail.clearTrail();
     }
 
     /**
      * set visible
      */
     public void setVisible () {
         myVisible = true;
     }
 
     /**
      * set to invisible
      */
     public void setInvisible () {
         myVisible = false;
     }
 
     /**
      * get angle
      * 
      * @return myAngle
      */
     public double getAngle () {
         return myAngle;
     }
 
     /**
      * Set angle
      * 
      * @param angle angle to set to
      */
     public void setAngle (double angle) {
         myAngle = angle;
     }
 
     /**
      * Does it leave a trail?
      * 
      * @return leftTrail
      */
     public boolean isLeaveTrail () {
         return myLeftTrail;
     }
 
     /**
      * is it visible?
      * 
      * @return my visible
      */
     public boolean isVisible () {
         return myVisible;
     }
 
     /**
      * double to string
      * 
      * @param num number
      * @return to string
      */
     public String toString (double num) {
         return Double.toString(num);
     }
 
     /**
      * Return trail
      * 
      * @return
      */
     public Trail getTrail () {
         return myTrail;
     }
 
     /**
      * 
      * @return size of Image
      */
     public Dimension getSize () {
         return myTurtleSize;
     }
 
     /**
      * 
      * @param image Turtle's image
      */
     public void changeTurtleImage (String image) {
         setView(new Pixmap(image));
     }
 
     /**
      * 
      * Moves turtle back to previous location
      */
     public void undoMove () {
         System.out.println(myTrail.getTrails().size());
         if (myTrail.getTrails().size() > 1) {
             Location lastTrail = myTrail.getTrails().get(myTrail.getTrails().size() - 1);
             myUndoneTrails.addTrail(lastTrail);
             myTrail.removeTrail(lastTrail);
             System.out.println(myTrail.getTrails().get(myTrail.getTrails().size() - 1));
             setCenter(myTrail.getTrails().get(myTrail.getTrails().size() - 1));
         }
     }
 
     /**
      * 
      * Moves turtle back to last undone location
      */
     public void redoMove () {
         if (myUndoneTrails.getTrails().size() > 0) {
             Location lastUndoneTrail =
                     myUndoneTrails.getTrails().get(myUndoneTrails.getTrails().size() - 1);
             myTrail.addTrail(lastUndoneTrail);
             myUndoneTrails.removeTrail(lastUndoneTrail);
             setCenter(lastUndoneTrail);
         }
     }
 
     /**
      * 
      * @return
      *         turtle image
      */
     public Pixmap getTurtleImage () {
         return TURTLE_IMAGE;
     }
 
     /**
      * 
      * @param index index of shape in resource file
      */
     public void setTurtleShapeSize (int index) {
         String size = mySizes.getString(Integer.toString(index));
         if (size != null) {
             myIndex = index;
             String[] values = size.split(",");
             myTurtleSize = new Dimension(Integer.parseInt(values[0]), Integer.parseInt(values[1]));
             setSize(Integer.parseInt(values[0]), Integer.parseInt(values[1]));
         }
     }
 
     /**
      * 
      * @return the turtle's shape index
      */
     public int getTurtleShapeIndex () {
         return myIndex;
     }
 }
