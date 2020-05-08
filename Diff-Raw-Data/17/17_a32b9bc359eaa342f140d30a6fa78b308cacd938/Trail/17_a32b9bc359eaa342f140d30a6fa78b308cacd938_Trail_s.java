 package util;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Rectangle;
 
 
 /**
  * Trail object contains information that the TurtleDrawer uses to draw the trails.
  * 
  * @author Danny Goodman
  * 
  */
 public class Trail {
 
     protected Location myStart;
     protected Location myEnd;
     protected Color myColor;
     protected Rectangle myBounds;
 
     /**
      * Constructor needs start, end locations create trail. penColor is used to differ between
      * penDown and the invisible penUp trails. bounds used for calculating whether
      * a trail point is within the bounds.
      * 
      * @param start - Location
      * @param end - Location
      * @param penColor - color to paint this trail
      * @param bounds - bounds of TurtleView
      */
     public Trail (Location start, Location end, Color penColor, Rectangle bounds) {
         myStart = start;
         myEnd = end;
         myColor = penColor;
         myBounds = bounds;
     }
 
     /**
      * Trail paints itself based on its myColor variable.
      * 
      * @param pen - used to paint in TurtleView.
      */
     public void paint (Graphics pen) {
         pen.setColor(myColor);
         drawLine(pen, myStart, myEnd);
     }
 
     /**
      * Returns the Start Location of the trail.
      * 
      * @return myStart
      */
     public Location getStart () {
         return myStart;
     }
 
     /**
      * Returns the End Location of the trail.
      * 
      * @return myEnd
      */
     public Location getEnd () {
         return myEnd;
     }
 
     /**
      * Used to convert an unwarped trail into a warped trail. called on all trails in a list when
      * WarpTurtleDrawer is constructed.
      * 
      * @return WarpedTrail object with same start and end values.
      */
     public Trail warp () {
         return new WarpedTrail(myStart, myEnd, myColor, myBounds);
     }
 
     /**
      * returns the number of warps in each direction that the current object has undergone. For this
      * superclass, it returns all zeros as no warps have occurred within this trail object.
      * 
      * @return int[] warps
      */
     public int[] getWarps () {
         return new int[] { 0, 0, 0, 0 };
     }
 
     /**
      * Draws a line between the start and end. does not take into account warping for this
      * superclass.
      * 
      * @param pen
      * @param start
      * @param finish
      */
     protected void drawLine (Graphics pen, Location start, Location finish) {
         start = translateCoordinates(start);
         finish = translateCoordinates(finish);
         pen.drawLine((int) start.getX(), (int) start.getY(),
                      (int) finish.getX(), (int) finish.getY());
     }
 
     /**
      * TranslateCoordinates based on the TurtleView bounds passed into the constructor.
      * 
      * @param point
      * @return new translated coordinate.
      */
     protected Location translateCoordinates (Location point) {
         int centerX = (int) myBounds.getWidth() / 2;
         int centerY = (int) myBounds.getHeight() / 2;
         return new Location(centerX + point.getX(), centerY - point.getY());
     }
 
 }
