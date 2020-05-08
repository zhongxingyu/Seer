 package model;
 
 import java.awt.Dimension;
 import java.util.ArrayList;
 import java.util.Iterator;
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
 public class Turtle extends Sprite implements Paintable {
 
     private static final Pixmap DEFAULT_IMAGE = new Pixmap("turtle.gif");
     private static final Dimension DEFAULT_DIMENSION = new Dimension(30, 30);
     private static final double PRECISION_LEVEL = 0.0000001;
 
     private boolean myPenDown = true;
     private boolean myTurtleShowing = true;
     private Line myLine = new Line();
     private Dimension myCanvasBounds;
     private int myCenterXValue;
     private int myCenterYValue;
 
     /**
      * Creates a turtle sprite.
      * 
      * @param image image to use
      * @param center center of turtle
      * @param size size of turtle
      * @param canvasBounds bounds of canvas that turtle uses
      */
     public Turtle (Pixmap image, Location center, Dimension size, Dimension canvasBounds) {
         super(image, center, size);
         myCanvasBounds = canvasBounds;
         myCenterXValue = (int) myCanvasBounds.getWidth() / 2;
         myCenterYValue = (int) myCanvasBounds.getHeight() / 2;
     }
 
     /**
      * Uses default values for constructor, except for canvasBounds.
      * 
      * @param canvasBounds bounds to use.
      */
     public Turtle (Dimension canvasBounds) {
         this(DEFAULT_IMAGE,
              new Location(canvasBounds.getWidth() / 2, canvasBounds.getHeight() / 2),
              DEFAULT_DIMENSION, canvasBounds);
     }
 
     /**
      * Move forward or backward by number of pixels.
      * 
      * @param pixels to move by
      * @return command return value
      */
     public int move (int pixels) {
         int pixelsToMove = pixels;
         // ensure that moveRecursiveHelper doesn't take a negative argument
         if (Math.abs(pixels) != pixels) {
             pixelsToMove = -pixelsToMove;
             turn(HALF_TURN_DEGREES);
         }
         moveRecursiveHelper(pixelsToMove);
         if (Math.abs(pixels) != pixels) {
             turn(HALF_TURN_DEGREES);
         }
         return Math.abs(pixels);
     }
 
     /**
      * Recursive helper for move. Uses its own helper methods, overrunsTop, Bottom, Right, and Left
      * to calculate next move. Calls itself recursively when turtle hits the edge of the screen
      * (overruns).
      * 
      * @param pixels changes every time with recursive call until it is less than 0 (because of
      *        possible rounding errors with trig functions)
      */
     private void moveRecursiveHelper (double pixels) {
         if (pixels <= 0 + PRECISION_LEVEL) return;
 
         Location currentLocation = getLocation();
         Location nextLocation = getLocation();
         Location nextCenter = nextLocation;
         nextLocation.translate(new Vector(getHeading(), pixels));
         Location[] replacements = { nextLocation, nextCenter };
 
         if (nextLocation.getY() < 0) {
             // top
             replacements = overrunsTop();
         }
         else if (nextLocation.getY() > myCanvasBounds.getHeight()) {
             // bottom
             replacements = overrunBottom();
         }
         else if (nextLocation.getX() > myCanvasBounds.getWidth()) {
             // right
             replacements = overrunRight();
         }
         else if (nextLocation.getX() < 0) {
             // left
             replacements = overrunLeft();
         }
 
         nextLocation = replacements[0];
         nextCenter = replacements[1];
 
         setCenter(nextCenter);
         double newPixels = pixels - (Vector.distanceBetween(currentLocation, nextLocation));
         if (myPenDown) {
             myLine.addLineSegment(currentLocation, nextLocation);
         }
         moveRecursiveHelper(newPixels);
 
     }
 
     /**
      * Calculates next Center for turtle and next Location for line ending/pixel calculations if
      * Turtle overruns to top on move.
      */
     private Location[] overrunsTop () {
         Location nextLocation;
         Location nextCenter;
 
         // exact calculation for exact 90 degree heading directions (don't want trig functions
         // handling this)
         if (getHeading() == THREE_QUARTER_TURN_DEGREES) {
             nextLocation = new Location(getX(), 0);
             nextCenter = new Location(getX(), myCanvasBounds.getHeight());
             return new Location[] { nextLocation, nextCenter };
         }
 
         double angle = FULL_TURN_DEGREES - getHeading();
         if (getHeading() < THREE_QUARTER_TURN_DEGREES) {
             angle = -(getHeading() - HALF_TURN_DEGREES);
         }
         angle = Math.toRadians(angle);
         nextLocation = new Location(getX() + getY() / Math.tan(angle), 0);
         nextCenter = new Location(getX() + getY() / Math.tan(angle),
                                   myCanvasBounds.getHeight());
 
         // eliminates race condition - if next location overruns left/right AND top/bottom it checks
         // to see which is overrun first and corrects
         if (nextLocation.getX() > myCanvasBounds.getWidth())
             // right
             return overrunRight();
         else if (nextLocation.getX() < 0) // left
             return overrunLeft();
 
         return new Location[] { nextLocation, nextCenter };
     }
 
     /**
      * Calculates next Center for turtle and next Location for line ending/pixel calculations if
      * Turtle overruns to bottom on move.
      */
     private Location[] overrunBottom () {
         Location nextLocation;
         Location nextCenter;
 
         // exact calculation for exact 90 degree heading directions (don't want trig functions
         // handling this)
         if (getHeading() == ONE_QUARTER_TURN_DEGREES) {
             nextLocation = new Location(getX(), myCanvasBounds.getHeight());
             nextCenter = new Location(getX(), 0);
             return new Location[] { nextLocation, nextCenter };
         }
 
         double angle = getHeading();
         if (getHeading() > ONE_QUARTER_TURN_DEGREES) {
             angle = -(HALF_TURN_DEGREES - getHeading());
         }
         angle = Math.toRadians(angle);
         nextLocation =
                 new Location(getX() + (myCanvasBounds.getHeight() - getY()) / Math.tan(angle),
                              myCanvasBounds.getHeight());
         nextCenter = new Location(getX() + (myCanvasBounds.getHeight() - getY()) / Math.tan(angle),
                                   0);
 
         // eliminates race condition - if next location overruns left/right AND top/bottom it checks
         // to see which is overrun first and corrects
         if (nextLocation.getX() > myCanvasBounds.getWidth())
             // right
             return overrunRight();
         else if (nextLocation.getX() < 0) // left
             return overrunLeft();
         return new Location[] { nextLocation, nextCenter };
     }
 
     /**
      * Calculates next Center for turtle and next Location for line ending/pixel calculations if
      * Turtle overruns to right on move.
      */
     private Location[] overrunRight () {
         Location nextLocation;
         Location nextCenter;
         // exact calculation for exact 90 degree heading directions (don't want trig functions
         // handling this)
         if (getHeading() == ONE_QUARTER_TURN_DEGREES) {
             nextLocation = new Location(myCanvasBounds.getWidth(), getY());
             nextCenter = new Location(0, getY());
             return new Location[] { nextLocation, nextCenter };
         }
 
        double angle = ONE_QUARTER_TURN_DEGREES - getHeading();
         if (getHeading() > HALF_TURN_DEGREES) {
             angle = -(getHeading() - THREE_QUARTER_TURN_DEGREES);
         }
         angle = Math.toRadians(angle);
         nextLocation =
                 new Location(myCanvasBounds.getWidth(), getY() +
                                                         (myCanvasBounds.getWidth() - getX()) /
                                                         Math.tan(angle));
         nextCenter =
                 new Location(0, getY() + (myCanvasBounds.getWidth() - getX()) / Math.tan(angle));
 
         return new Location[] { nextLocation, nextCenter };
     }
 
     /**
      * Calculates next Center for turtle and next Location for line ending/pixel calculations if
      * Turtle overruns to left on move.
      */
     private Location[] overrunLeft () {
         Location nextLocation;
         Location nextCenter;
         // exact calculation for exact 90 degree heading directions (don't want trig functions
         // handling this)
         if (getHeading() == THREE_QUARTER_TURN_DEGREES) {
             nextLocation = new Location(0, getY());
             nextCenter = new Location(myCanvasBounds.getWidth(), getY());
             return new Location[] { nextLocation, nextCenter };
         }
 
         double angle = getHeading() - ONE_QUARTER_TURN_DEGREES;
         if (getHeading() > HALF_TURN_DEGREES) {
             angle = -(THREE_QUARTER_TURN_DEGREES - getHeading());
         }
         angle = Math.toRadians(angle);
         nextLocation = new Location(0, getY() + getX() / Math.tan(angle));
         nextCenter = new Location(myCanvasBounds.getWidth(), getY() + getX() /
                                                              Math.tan(angle));
 
         return new Location[] { nextLocation, nextCenter };
     }
 
     /**
      * Turns turtle by given degrees.
      * 
      * @param degrees to turn by
      * @return command return value
      */
     public double turn (double degrees) {
         setHeading(getHeading() + degrees);
         return Math.abs(degrees);
     }
 
     /**
      * sets current heading.
      * 
      * @param heading to set
      * @return current heading
      */
     public double setHeading (double heading) {
         double oldHeading = getHeading();
         setMyHeading(heading);
         return Math.abs(heading - oldHeading);
     }
 
     /**
      * Sets heading to go towards location **AS IN VIEW'S COORDINATES**
      * 
      * @param location location to set heading towards
      * @return distance of turn
      */
     public double towards (Location location) {
         Location convertedLocation = convertFromViewCoordinates(location);
         double turnDistance =
                 180 -
                         (-1) *
                         Vector.angleBetween(convertFromViewCoordinates(new Location(getX(), getY())),
                                             convertedLocation);
         turn(turnDistance);
         return turnDistance;
     }
 
     /**
      * Moves turtle to location **AS IN VIEW'S COORDINATES**
      * 
      * @param location to move to
      * @return distance of move
      */
     public int setLocation (Location location) {
         Location locationToMove = location;
         // double heading = getHeading();
         towards(locationToMove);
         int distance = (int) Vector.distanceBetween(location, getLocation());
         // setHeading(heading);
         move(distance);
         return distance;
     }
 
     /**
      * Sets turtle to showing.
      * 
      * @return command value
      */
     public int showTurtle () {
         myTurtleShowing = true;
         return 1;
     }
 
     /**
      * Sets turtle to hiding.
      * 
      * @return command value
      */
     public int hideTurtle () {
         myTurtleShowing = false;
         return 0;
     }
 
     /**
      * Sets lines to show up on new moves.
      * 
      * @return command value
      */
     public int showPen () {
         myPenDown = true;
         return 1;
     }
 
     /**
      * Sets lines to not show up on new moves.
      * 
      * @return command value
      */
     public int hidePen () {
         myPenDown = false;
         return 0;
     }
 
     /**
      * Moves turtle to center and original heading.
      * 
      * @return
      */
     public int home () {
         Location center = new Location(myCenterXValue, myCenterYValue);
         int distance = (int) Vector.distanceBetween(getLocation(), center);
         setLocation(center);
         resetHeading();
         return distance;
     }
 
     /**
      * Clears all lines and moves turtle home.
      * 
      * @return
      */
     public int clearScreen () {
         int distance = home();
         myLine.clear();
         return distance;
     }
 
     /**
      * Gets if turtle is showing or not.
      * 
      * @return 1 if turtle is showing, 0 if not
      */
     public int isTurtleShowing () {
         if (myTurtleShowing) return 1;
         return 0;
     }
 
     /**
      * Gets if pen is down or not.
      * 
      * @return 1 if pen is down, 0 if not
      */
     public int isPenDown () {
         if (myPenDown) return 1;
         return 0;
     }
 
     private Location convertFromViewCoordinates (Location location) {
         return new Location(location.getX() - myCenterXValue, myCenterYValue - location.getY());
     }
 
     /**
      * Gets all paintable objects currently showing and returns them in an iterator.
      * 
      * @return iterator of paintables
      */
     public Iterator<Paintable> getPaintableIterator () {
         ArrayList<Paintable> paintList = new ArrayList<Paintable>();
         if (myTurtleShowing) {
             paintList.add(this);
         }
         paintList.add(myLine);
         return paintList.iterator();
     }
 
     /**
      * 
      * @return current turtle position.
      */
     public Location getTurtlePosition () {
         return convertFromViewCoordinates(getLocation());
     }
 
 }
