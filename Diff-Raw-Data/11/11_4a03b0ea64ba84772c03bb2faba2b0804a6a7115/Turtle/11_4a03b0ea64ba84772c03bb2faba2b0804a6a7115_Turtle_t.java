 package model;
 
 import java.awt.Dimension;
 import java.util.ArrayList;
 import java.util.Iterator;
 import util.DataSource;
 import util.Location;
 import util.Paintable;
 import util.Pixmap;
 import util.Sprite;
 import util.Vector;
 
 
 /**
  * Represents the turtle on the canvas. Can be painted. Can be called with its custom methods by
  * commands. Also implements dataSource and can be accessed to give information about itself.
  * 
  * @author David Winegar
  * @author Zhen Gou
  */
 public class Turtle extends Sprite implements DataSource, Paintable {
 
     private boolean myPenDown = true;
     private boolean myTurtleShowing = true;
     private double myHeading;
    public static final Pixmap DEFAULT_IMAGE = new Pixmap("../images/turtle.gif"); 
     public static final Dimension DEFAULT_DIMENSION = new Dimension(30,30);
     private final double DEFAULT_HEADING = 270;
     private final int CENTER_X_VALUE;
     private final int CENTER_Y_VALUE;
 
     private Line myLine;
     private Dimension myCanvasBounds;
 
     private int myReturnValue;
     private String myMessage;
 
     public Turtle (Pixmap image, Location center, Dimension size, Dimension canvasBounds) {
         super(image, center, size);
         myCanvasBounds = canvasBounds;
         CENTER_X_VALUE = (int) myCanvasBounds.getWidth() / 2;
         CENTER_Y_VALUE = (int) myCanvasBounds.getHeight() / 2;
         myHeading = DEFAULT_HEADING;
     }
     public Turtle( Location center , Dimension canvasBounds){
         super(DEFAULT_IMAGE, center, DEFAULT_DIMENSION);
        myCanvasBounds = canvasBounds;
         CENTER_X_VALUE = (int) myCanvasBounds.getWidth() / 2;
         CENTER_Y_VALUE = (int) myCanvasBounds.getHeight() / 2;
         myHeading = DEFAULT_HEADING;
     }
     
 
     public int move (int pixels) {
         moveRecursiveHelper(pixels);
         return pixels;
     }
 
     private void moveRecursiveHelper (int pixels) {
         if (pixels == 0) return;
         Location currentLocation = getLocation();  // might be buggy here, does getLocation gives a
                                                   // new object or just a pointer to myCenter, if
                                                   // just pointer then this location is changed when
                                                   // it translates
         Location nextLocation = getLocation();
         nextLocation.translate(new Vector(getVelocity().getDirection(), pixels));
         // top
         if (nextLocation.getY() < 0) {
             nextLocation = new Location(getX() + getY() / Math.tan(myHeading), 0);
             setCenter(new Location(getX() + getY() / Math.tan(myHeading),
                                    myCanvasBounds.getHeight()));
             // bottom
         }
         else if (nextLocation.getY() > myCanvasBounds.getHeight()) {
             nextLocation =
                     new Location(getX() + getY() / Math.tan(myHeading), myCanvasBounds.getHeight());
             setCenter(new Location(getX() + getY() / Math.tan(myHeading), 0));
             // right
         }
         else if (nextLocation.getX() > myCanvasBounds.getWidth()) {
             nextLocation =
                     new Location(myCanvasBounds.getWidth(), getY() + getX() / Math.tan(myHeading));
             setCenter(new Location(0, getY() + getX() / Math.tan(myHeading)));
             // left
         }
         else if (nextLocation.getX() < 0) {
             nextLocation = new Location(0, getY() + getX() / Math.tan(myHeading));
             setCenter(new Location(myCanvasBounds.getWidth(), getY() + getX() / Math.tan(myHeading)));
         }
 
         pixels -= (int) Vector.distanceBetween(currentLocation, nextLocation) * Math.signum(pixels);
 
         myLine.addLineSegment(currentLocation, nextLocation);
 
         moveRecursiveHelper(pixels);
     }
 
     public double turn (double degrees) {
         setVelocity(getVelocity().getDirection() + degrees, 0);
         return degrees;
     }
 
     public double setHeading (double heading) {
         setVelocity(heading, 0);
         return heading;
     }
 
     public double towards (Location location) {
         location = convertFromViewCoordinates(location);
         double turnDistance = Vector.angleBetween(new Location(getX(), getY()), location);
         turn(turnDistance);
         return turnDistance;
     }
 
     public int setLocation (Location location) {
         location = convertFromViewCoordinates(location);
         double heading = getHeading();
         towards(location);
         int distance = (int) Vector.distanceBetween(location, getLocation());
         setHeading(heading);
         return distance;
     }
 
     public int showTurtle () {
         myTurtleShowing = true;
         return 1;
     }
 
     public int hideTurtle () {
         myTurtleShowing = false;
         return 0;
     }
 
     public int showPen () {
         myPenDown = true;
         return 1;
     }
 
     public int hidePen () {
         myPenDown = false;
         return 0;
     }
 
     public int home () {
         Location center = new Location(CENTER_X_VALUE, CENTER_Y_VALUE);
         int distance = (int) Vector.distanceBetween(getLocation(), center);
         setLocation(center);
         setHeading(UP_DIRECTION);
         return distance;
     }
 
     public int clearScreen () {
         int distance = home();
         myLine.clear();
         return distance;
     }
 
     public int isTurtleShowing () {
         if (myTurtleShowing) return 1;
         return 0;
     }
 
     public int isPenDown () {
         if (myPenDown) return 1;
         return 0;
     }
 
     public double getHeading () {
         return getVelocity().getDirection();
     }
 
     @Override
     public void update (double elapsedTime, Dimension bounds) {
 
     }
 
     private Location convertFromViewCoordinates (Location location) {
         return new Location(location.getX() - CENTER_X_VALUE, location.getY() - CENTER_Y_VALUE);
     }
 
     // implementing DataSource methods
 
     @Override
     public Iterator<Paintable> getPaintableIterator () {
         ArrayList<Paintable> paintList = new ArrayList<Paintable>();
         paintList.add(this);
         paintList.add(myLine);
         return paintList.iterator();
     }
 
     @Override
     public int getReturnValue () {
         return myReturnValue;
     }
 
     @Override
     public Location getTurtlePosition () {
         return convertFromViewCoordinates(getLocation());
 
     }
 
     @Override
     public int getTurtleHeading () {
         return (int) myHeading;
     }
 
     @Override
     public String showMessage () {
         return myMessage;
     }
 }
